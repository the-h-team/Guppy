package com.github.sanctum.jda.common;

import com.github.sanctum.panther.container.PantherQueue;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public interface MusicPlayer {

	@NotNull Controller getController();

	@NotNull Queue getQueue();

	@NotNull SendHandler getSendHandler();

	@Nullable Channel LastAlerted();

	@Nullable Channel LastSummoned();

	void setSendHandler(@NotNull SendHandler handler);

	void setListener(@NotNull Listener listener);

	void resetListener();

	void stop();

	void load(@NotNull Channel channel, @NotNull String url);

	void load(@NotNull Guppy guppy, @NotNull Channel channel, @NotNull String url);

	interface SendHandler {

		boolean canProvide();

		@Nullable
		ByteBuffer provide20MsAudio();

		default boolean isOpus() {
			return false;
		}
	}

	interface Track extends Nameable {

		@NotNull String getAuthor();

		void setPosition(long position);

		void stop();

		long getDuration();

		@NotNull Object getHandle();

	}

	interface Listener {

		default void onStart(Controller controller, Track track) {
		}

		default void onEnd(Controller controller, Track track, Result result) {
		}

		default void onPause(Controller controller) {
		}

		default void onResume(Controller controller) {
		}

		enum Result {
			FINISHED(true),
			LOAD_FAILED(true),
			STOPPED(false),
			REPLACED(false),
			CLEANUP(false);

			boolean mayStart;

			Result(boolean mayStart) {
				this.mayStart = mayStart;
			}

			public boolean canStartNext() {
				return mayStart;
			}
		}

	}

	class Queue {

		private final PantherQueue<Track> queue;

		public Queue() {
			this.queue = new PantherQueue<>();
		}

		public Queue add(@NotNull Track track) {
			queue.add(track);
			return this;
		}

		public Track poll() {
			return queue.pollNow();
		}

		public PantherQueue<Track> get() {
			return queue;
		}

	}

	interface Controller {

		/**
		 * @return Currently playing track
		 */
		Track getPlaying();

		/**
		 * @param track     The track to start playing, passing null will stop the current track and return false
		 * @param interrupt whether to play right now or wait and queue.
		 * @return True if the track was started
		 */
		boolean start(Track track, boolean interrupt);

		/**
		 * @param track The track to start playing
		 */
		void play(Track track);

		/**
		 * Stop currently playing track.
		 */
		void stop();

		int getVolume();

		void setVolume(int volume);

		/**
		 * @return Whether the player is paused
		 */
		boolean isPaused();

		/**
		 * @param value True to pause, false to resume
		 */
		void setPaused(boolean value);

		/**
		 * Destroy the player and stop playing track.
		 */
		void destroy();

		/**
		 * Check if the player should be "cleaned up" - stopped due to nothing using it, with the given threshold.
		 *
		 * @param threshold Threshold in milliseconds to use
		 */
		void cleanup(long threshold);

		boolean isConnected();
	}
}
