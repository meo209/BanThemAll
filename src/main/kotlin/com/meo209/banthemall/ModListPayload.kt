package com.meo209.banthemall

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class ModListPayload(val modData: List<String>): CustomPayload {

    companion object {
        val ID: CustomPayload.Id<ModListPayload> =
            CustomPayload.Id<ModListPayload>(BanThemAllServer.MOD_LIST_PACKET_ID)

        val CODEC: PacketCodec<ByteBuf, ModListPayload> =
            PacketCodec.tuple(
                BanThemAllServer.MOD_LIST_PACKET_CODEC,
                ModListPayload::modData
            ) { mods ->
                ModListPayload(mods)
            }
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> =
        ID

}