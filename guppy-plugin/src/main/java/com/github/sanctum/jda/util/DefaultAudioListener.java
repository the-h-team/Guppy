package com.github.sanctum.jda.util;

import com.github.sanctum.jda.common.Channel;
import com.github.sanctum.jda.common.MusicPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.jetbrains.annotations.NotNull;

public class DefaultAudioListener extends AudioEventAdapter implements MusicPlayer.Listener {

	final MusicPlayer queue;
	final Channel channel;

	public DefaultAudioListener(@NotNull Channel channel, @NotNull MusicPlayer queue) {
		this.queue = queue;
		this.channel = channel;
	}

	@Override
	public void onEnd(MusicPlayer.Controller controller, MusicPlayer.Track track, Result result) {
		if (result.canStartNext()) {
			queue.getQueue().next();
			channel.sendMessage("Now playing track **" + track.getName() + "** by **" + track.getAuthor() + "**").queue();
		}
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		onEnd(queue.getController(), new MusicPlayer.Track() {
			@Override
			public @NotNull String getAuthor() {
				return track.getInfo().author;
			}

			@Override
			public void setPosition(long position) {
				track.setPosition(position);
			}

			@Override
			public void stop() {
				track.stop();
			}

			@Override
			public long getDuration() {
				return track.getDuration();
			}

			@Override
			public @NotNull Object getHandle() {
				return track;
			}

			@Override
			public @NotNull String getName() {
				return track.getInfo().title;
			}
		}, Result.valueOf(endReason.name()));
	}

	@Override
	public void onPlayerPause(AudioPlayer player) {
		onPause(queue.getController());
	}

	@Override
	public void onPlayerResume(AudioPlayer player) {
		onResume(queue.getController());
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		onStart(queue.getController(), new MusicPlayer.Track() {
			@Override
			public @NotNull String getAuthor() {
				return track.getInfo().author;
			}

			@Override
			public void setPosition(long position) {
				track.setPosition(position);
			}

			@Override
			public void stop() {
				track.stop();
			}

			@Override
			public long getDuration() {
				return track.getDuration();
			}

			@Override
			public @NotNull Object getHandle() {
				return track;
			}

			@Override
			public @NotNull String getName() {
				return track.getInfo().title;
			}
		});
	}
}
