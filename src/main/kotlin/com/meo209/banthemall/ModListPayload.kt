package com.meo209.banthemall

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class ModListPayload(val modIndex: List<String>): CustomPayload {

    companion object {
        val ID: CustomPayload.Id<ModListPayload> =
            CustomPayload.Id<ModListPayload>(BanThemAllServer.MOD_LIST_PACKET_ID)

        val CODEC: PacketCodec<ByteBuf, ModListPayload> =
            PacketCodec.tuple(
                BanThemAllServer.MOD_LIST_PACKET_CODEC,
                ModListPayload::modIndex
            ) { mods ->
                ModListPayload(mods)
            }
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> =
        ID

}