package com.meo209.banthemall.client

import com.meo209.banthemall.CommonUtils
import com.meo209.banthemall.ModListPayload
import com.mojang.logging.LogUtils
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler

class BanThemAllClient : ClientModInitializer {

    private val logger = LogUtils.getLogger()
    private val modIndex: MutableList<String> = mutableListOf()

    override fun onInitializeClient() {
        logger.info("Initializing BanThemAll...")
        val modDir = FabricLoader.getInstance().gameDir.resolve("mods").toFile()
        logger.info("Generating mod data...")
        modIndex += CommonUtils.generateModIndex(modDir)

        ClientPlayConnectionEvents.JOIN.register { _: ClientPlayNetworkHandler, packetSender: PacketSender, _: MinecraftClient ->
            packetSender.sendPacket(ModListPayload(modIndex))
        }

        logger.info("BanThemAll Client initialized.")
    }
}
