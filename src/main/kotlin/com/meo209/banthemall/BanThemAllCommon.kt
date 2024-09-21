package com.meo209.banthemall

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

class BanThemAllCommon: ModInitializer {

    override fun onInitialize() {
        PayloadTypeRegistry.playC2S().register(ModListPayload.ID, ModListPayload.CODEC)
    }

}