package com.meo209.banthemall

import com.mojang.brigadier.CommandDispatcher
import com.mojang.logging.LogUtils
import io.netty.buffer.ByteBuf
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.server.command.CommandManager.RegistrationEnvironment
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier


class BanThemAllServer : DedicatedServerModInitializer {

    private val logger = LogUtils.getLogger()
    private val enableDebugInfo = true
    private val modIndex = mutableListOf<String>()

    companion object {
        val MOD_LIST_PACKET_ID: Identifier = Identifier.of("mod_list")

        val MOD_LIST_PACKET_CODEC = object : PacketCodec<ByteBuf, List<String>> {

            override fun decode(buf: ByteBuf?): List<String> {
                val byteArray = PacketByteBuf.readByteArray(buf)
                return byteArray.toString(Charsets.UTF_8).split(";")
            }

            override fun encode(buf: ByteBuf?, value: List<String>?) {
                if (buf == null || value == null) return

                val joinedString = value.joinToString(";")
                val byteArray = joinedString.toByteArray(Charsets.UTF_8)
                PacketByteBuf.writeByteArray(buf, byteArray)
            }

        }
    }

    override fun onInitializeServer() {
        logger.info("Initializing BanThemAll...")

        val directory = FabricLoader.getInstance().configDir.resolve("banthemall-mods").toFile()
        if (!directory.exists())
            directory.mkdir()

        logger.info("Generating mod data...")
        modIndex += CommonUtils.generateModIndex(directory)

        if (enableDebugInfo)
            logger.info(modIndex.toString())

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, registryAccess: CommandRegistryAccess?, environment: RegistrationEnvironment? ->
            dispatcher.register(
                literal("banthemall")
                    .requires { it.hasPermissionLevel(3) }
                .then(literal("reload").executes { ctx ->
                    modIndex.clear()
                    modIndex += CommonUtils.generateModIndex(directory)

                    if (enableDebugInfo)
                        logger.info(modIndex.toString())

                    ctx.source.sendFeedback({ Text.literal("Refreshed mod verification index.") }, true)
                    1
                }))
        })

        PayloadTypeRegistry.playS2C().register(ModListPayload.ID, ModListPayload.CODEC)
        PayloadTypeRegistry.playC2S().register(ModListPayload.ID, ModListPayload.CODEC)

        ServerPlayNetworking.registerGlobalReceiver(
            ModListPayload.ID
        ) { payload: ModListPayload, context: ServerPlayNetworking.Context ->
            context.server().execute {
                logger.info("Client \"${context.player().nameForScoreboard}\" connected.")

                if (enableDebugInfo)
                    logger.info("ModIndex: ${payload.modIndex}")

                logger.info("Checking mods...")
                if (verifyMods(payload))
                    logger.info("Client passed.")
                else {
                    logger.info("Client failed. Disconnecting")
                    context.player().networkHandler.disconnect(Text.literal("You do not meet the mod requirements for this server. If you think this is an error please contact the administrator(s)."))
                }
            }
        }
        logger.info("BanThemAll initialized.")
    }

    private fun verifyMods(payLoad: ModListPayload): Boolean =
        payLoad.modIndex.all { mod -> modIndex.contains(mod) }

}
