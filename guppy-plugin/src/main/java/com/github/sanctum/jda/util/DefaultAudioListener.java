package com.github.sanctum.jda.util;

import com.github.sanctum.jda.GuppyAPI;
import com.github.sanctum.jda.common.Channel;
import com.github.sanctum.jda.common.MusicPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public final class DefaultAudioListener extends AudioEventAdapter implements MusicPlayer.Listener {

	final MusicPlayer queue;
	MusicPlayer.Listener listener;

	public DefaultAudioListener(@NotNull MusicPlayer.Listener listener) {
		this.queue = null;
		this.listener = listener;
	}

	public DefaultAudioListener(@NotNull MusicPlayer queue) {
		this.queue = queue;
	}

	@Override
	public void onEnd(MusicPlayer.Controller controller, MusicPlayer.Track track, Result result) {
		if (listener == null) {
			if (result.canStartNext()) {
				MusicPlayer.Track next = queue.getQueue().poll();
				if (next != null) {
					controller.start(next, true);
					Channel c = queue.LastAlerted();
					if (c != null)
						c.sendMessage("Now playing track **" + next.getName() + "** by **" + next.getAuthor() + "**").queue();
				} else controller.cleanup(TimeUnit.MINUTES.toMillis(1));
			}
		} else listener.onEnd(controller, track, result);
	}

	@Override
	public final void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (listener == null) {
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
		} else listener.onEnd(queue.getController(), new MusicPlayer.Track() {
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
	public final void onPlayerPause(AudioPlayer player) {
		if (listener == null) {
			onPause(queue.getController());
		} else listener.onPause(queue.getController());
	}

	@Override
	public final void onPlayerResume(AudioPlayer player) {
		if (listener == null) {
			onResume(queue.getController());
		} else listener.onResume(queue.getController());
	}

	@Override
	public final void onTrackStart(AudioPlayer player, AudioTrack track) {
		if (listener == null) {
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
		} else listener.onStart(queue.getController(), new MusicPlayer.Track() {
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
