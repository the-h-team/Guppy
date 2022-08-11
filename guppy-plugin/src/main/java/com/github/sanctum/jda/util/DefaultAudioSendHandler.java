package com.github.sanctum.jda.util;

import com.github.sanctum.jda.common.MusicPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultAudioSendHandler implements AudioSendHandler, MusicPlayer.SendHandler {
	private final AudioPlayer player;
	private final ByteBuffer byteBuffer;
	private final MutableAudioFrame frame;

	public DefaultAudioSendHandler(@NotNull AudioPlayer player) {
		this.player = player;
		this.byteBuffer = ByteBuffer.allocate(1024);
		this.frame = new MutableAudioFrame();
		this.frame.setBuffer(byteBuffer);
	}

	@Override
	public boolean canProvide() {
		return player.provide(this.frame);
	}

	@Nullable
	@Override
	public ByteBuffer provide20MsAudio() {
		Buffer buffer = ((Buffer) this.byteBuffer).flip();
		return (ByteBuffer) buffer;
	}

	@Override
	public boolean isOpus() {
		return true;
	}
}
