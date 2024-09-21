package com.meo209.banthemall

import com.mojang.brigadier.CommandDispatcher
import com.mojang.logging.LogUtils
import io.netty.buffer.ByteBuf
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
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
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class BanThemAllServer : DedicatedServerModInitializer {

    private val logger = LogUtils.getLogger()
    private val enableDebugInfo = true
    private val modIndex = mutableListOf<String>()
    private val pendingPlayers = mutableMapOf<String, Boolean>()

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

        ServerPlayNetworking.registerGlobalReceiver(
            ModListPayload.ID
        ) { payload: ModListPayload, context: ServerPlayNetworking.Context ->
            context.server().execute {
                logger.info("Client \"${context.player().nameForScoreboard}\" connected.")

                if (enableDebugInfo)
                    logger.info("ModIndex: ${payload.modIndex}")

                logger.info("Checking mods...")
                if (verifyMods(payload)) {
                    logger.info("Client passed.")
                    pendingPlayers[context.player().uuidAsString] = true // Mark as passed
                }
                else {
                    logger.info("Client failed. Disconnecting")
                    context.player().networkHandler.disconnect(Text.literal("You do not meet the mod requirements for this server. If you think this is an error please contact the administrator(s)."))
                }
            }
        }

        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            val player = handler.player
            logger.info("Player \"${player.name.string}\" joined. Waiting for mod list...")

            pendingPlayers[player.uuidAsString] = false

            val executor = Executors.newSingleThreadScheduledExecutor()
            executor.schedule({
                server.execute {
                    if (pendingPlayers[player.uuidAsString] == false) {
                        logger.info("Mod list not received in time for \"${player.name.string}\". Disconnecting.")
                        player.networkHandler.disconnect(Text.literal("Mod list not received."))
                    }
                }
            }, 1, TimeUnit.SECONDS)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, server ->
            val player = handler.player
            logger.info("Player \"${player.name.string}\" disconnected.")
            pendingPlayers.remove(player.uuidAsString) // Clean up the pending map
        }

        logger.info("BanThemAll initialized.")
    }

    private fun verifyMods(payLoad: ModListPayload): Boolean =
        payLoad.modIndex.all { mod -> modIndex.contains(mod) }

}
