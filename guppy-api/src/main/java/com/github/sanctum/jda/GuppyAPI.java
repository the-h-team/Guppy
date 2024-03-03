package com.github.sanctum.jda;

import com.github.sanctum.jda.addon.DiscordExtension;
import com.github.sanctum.jda.common.Channel;
import com.github.sanctum.jda.common.CommandController;
import com.github.sanctum.jda.common.Emoji;
import com.github.sanctum.jda.common.Guppy;
import com.github.sanctum.jda.common.JDAController;
import com.github.sanctum.jda.common.MusicPlayer;
import com.github.sanctum.jda.common.Role;
import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.recursive.Service;
import com.github.sanctum.panther.recursive.ServiceFactory;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The main delegation api for controlling general discord services without touching JDA.
 */
public interface GuppyAPI extends Service {

	static @NotNull GuppyAPI getInstance() {
		return Optional.ofNullable(ServiceFactory.getInstance().getService(GuppyAPI.class)).orElseThrow(RuntimeException::new);
	}

	/**
	 * Convert a known JDA user to a guppy.
	 * If the provided object isn't a JDA user then a new guppy element will still be returned just not valid for use.
	 *
	 * @param user The JDA user to convert.
	 * @return A guppy object.
	 */
	@NotNull Guppy newGuppy(@NotNull Object user);

	/**
	 * Attempt retrieval on known guppy instance.
	 *
	 * @param id        a valid user mention id or user-name.
	 * @param isMention Whether the provided id is a mention id or not.
	 * @return a guppy user if found.
	 */
	@Nullable Guppy getGuppy(@NotNull String id, boolean isMention);

	/**
	 * Attempt retrieval on a known guppy instance.
	 *
	 * @param id a valid user id.
	 * @return a guppy user if found.
	 */
	@Nullable Guppy getGuppy(long id);

	/**
	 * Get all known guppies loaded in cache.
	 *
	 * @return all known guppies.
	 */
	@NotNull PantherCollection<Guppy> getGuppies();

	/**
	 * Get a known channel by name, valid channel types through use of this method are:
	 * - Voice
	 * - News
	 * - Text
	 *
	 * @param name The name of the channel.
	 * @return a channel matching the provided name or null if not found.
	 */
	@Nullable Channel getChannel(@NotNull String name);

	/**
	 * Get a known channel by id, valid channel types through use of this method are:
	 * - Voice
	 * - News
	 * - Text
	 *
	 * @param id The id of the channel.
	 * @return a channel matching the provided id or null if not found.
	 */
	@Nullable Channel getChannel(long id);

	/**
	 * Convert a known JDA channel into a universal guppy channel.
	 * This method will return a fresh channel instance regardless of what object you input, a valid object requires the input to be a JDA channel.
	 *
	 * @param channel the JDA channel to convert.
	 * @return a guppy channel object.
	 */
	@NotNull Channel newChannel(@Note("This is commonly a JDA oriented object.") @NotNull Object channel);

	/**
	 * Create a new private text channel that you can manipulate via roles.
	 *
	 * @param name       the name of the channel.
	 * @param categoryId the id of the category for the channel.
	 * @return a fresh text channel instance.
	 */
	@NotNull Channel newChannel(@NotNull String name, long categoryId, Role.Attachment... attachments);

	/**
	 * Create a new private text channel for a specific user.
	 *
	 * @param guppy      The user to use.
	 * @param name       The name of the channel.
	 * @param categoryId The id of the category for the channel.
	 * @return a fresh text channel instance.
	 */
	@NotNull Channel newChannel(@NotNull String name, long categoryId, @NotNull Guppy guppy, Role.Attachment... attachments);

	/**
	 * Create a new private text channel for a multiple users.
	 *
	 * @param guppies    The users to use.
	 * @param name       The name of the channel.
	 * @param categoryId The id of the category for the channel.
	 * @return a fresh text channel instance.
	 */
	@NotNull Channel newChannel(@NotNull String name, long categoryId, @NotNull Guppy[] guppies, Role.Attachment... attachments);

	/**
	 * Get all known channels.
	 *
	 * @return A collection of channels.
	 */
	@NotNull PantherCollection<Channel> getChannels();

	/**
	 * Get the command controller.
	 *
	 * @return a command controller object.
	 */
	@NotNull CommandController getCommands();

	/**
	 * Get a role from the server by its name.
	 *
	 * @param name the name of the role.
	 * @return a role specified by name or null if not found.
	 */
	@Nullable Role getRole(@NotNull String name);

	/**
	 * Get a role from the server by its id.
	 *
	 * @param id the id of the role.
	 * @return a role specified by id or null if not found.
	 */
	@Nullable Role getRole(long id);

	/**
	 * Get all roles from the server.
	 *
	 * @return all known roles.
	 */
	@NotNull PantherCollection<Role> getRoles();

	/**
	 * Get an emoji by name.
	 *
	 * @param name the name of the server emoji.
	 * @return an emoji specified by name or null if not found.
	 */
	@Nullable Emoji getEmoji(@NotNull String name);

	/**
	 * Get an emoji by id.
	 *
	 * @param id the id of the server emoji.
	 * @return an emoji specified by id or null if not found.
	 */
	@Nullable Emoji getEmoji(long id);

	/**
	 * Get all emoji's on the server.
	 *
	 * @return all known server emoji's
	 */
	@NotNull PantherCollection<Emoji> getEmojis();

	/**
	 * Get the JDA controller for restarting/stopping or just overall modifying the bot.
	 *
	 * @return the bot controller.
	 */
	@NotNull JDAController getController();

	/**
	 * Get the main config instance.
	 *
	 * @return the main config.
	 */
	@NotNull Configurable getConfig();

	/**
	 * Get the vent host.
	 *
	 * @return the vent host.
	 */
	@NotNull Vent.Host getHost();

	/**
	 * Get the music player instance.
	 *
	 * @return a music player instance.
	 */
	@NotNull MusicPlayer getPlayer();

	/**
	 * Get the header of information when the bot loads.
	 *
	 * @return The header for this bot.
	 */
	@NotNull DiscordExtension.Header getHeader();

	/**
	 * Get the footer of information when the bot has finished loading.
	 *
	 * @return The footer for this bot.
	 */
	@NotNull DiscordExtension.Footer getFooter();

	/**
	 * Set the header for this bot.
	 * This contains strings that are used for loading information.
	 *
	 * @param header the header to use.
	 */
	void setHeader(@NotNull DiscordExtension.Header header);

	/**
	 * Set the footer for this bot.
	 * This contains strings that used for loading completion information,
	 * aswell as a format
	 *
	 * @param footer
	 */
	void setFooter(@NotNull DiscordExtension.Footer footer);

	/**
	 * Set the music player instance.
	 *
	 * @param player the custom music player instance.
	 * @return this guppy api instance.
	 */
	@NotNull GuppyAPI setPlayer(@NotNull MusicPlayer player);

}
