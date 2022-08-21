package com.github.sanctum.jda.common;

import org.jetbrains.annotations.NotNull;

public enum Permission {

	// General Server / Channel Permissions
	MANAGE_CHANNEL("Manage Channels"),
	MANAGE_SERVER("Manage Server"),
	VIEW_AUDIT_LOGS("View Audit Logs"),
	VIEW_CHANNEL("View Channel(s)"),
	VIEW_GUILD_INSIGHTS("View Server Insights"),
	MANAGE_ROLES("Manage Roles"),
	MANAGE_PERMISSIONS("Manage Permissions"),
	MANAGE_WEBHOOKS( "Manage Webhooks"),
	MANAGE_EMOJIS_AND_STICKERS("Manage Emojis and Stickers"),

	// Membership Permissions
	CREATE_INSTANT_INVITE("Create Instant Invite"),
	KICK_MEMBERS("Kick Members"),
	BAN_MEMBERS("Ban Members"),
	NICKNAME_CHANGE("Change Nickname"),
	NICKNAME_MANAGE("Manage Nicknames"),
	MODERATE_MEMBERS("Timeout Members"),

	// Text Permissions
	MESSAGE_ADD_REACTION("Add Reactions"),
	MESSAGE_SEND("Send Messages"),
	MESSAGE_TTS("Send TTS Messages"),
	MESSAGE_MANAGE("Manage Messages"),
	MESSAGE_EMBED_LINKS("Embed Links"),
	MESSAGE_ATTACH_FILES("Attach Files"),
	MESSAGE_HISTORY("Read History"),
	MESSAGE_MENTION_EVERYONE("Mention Everyone"),
	MESSAGE_EXT_EMOJI("Use External Emojis"),
	USE_APPLICATION_COMMANDS("Use Application Commands"),
	MESSAGE_EXT_STICKER("Use External Stickers"),

	// Thread Permissions
	MANAGE_THREADS("Manage Threads"),
	CREATE_PUBLIC_THREADS("Create Public Threads"),
	CREATE_PRIVATE_THREADS("Create Private Threads"),
	MESSAGE_SEND_IN_THREADS("Send Messages in Threads"),

	// Voice Permissions
	PRIORITY_SPEAKER("Priority Speaker"),
	VOICE_STREAM("Video"),
	VOICE_CONNECT("Connect"),
	VOICE_SPEAK("Speak"),
	VOICE_MUTE_OTHERS("Mute Members"),
	VOICE_DEAF_OTHERS("Deafen Members"),
	VOICE_MOVE_OTHERS("Move Members"),
	VOICE_USE_VAD("Use Voice Activity"),
	VOICE_START_ACTIVITIES("Launch Activities in Voice Channels"),

	// Stage Channel Permissions
	REQUEST_TO_SPEAK("Request to Speak"),

	// Advanced Permissions
	ADMINISTRATOR("Administrator"),


	UNKNOWN("Unknown");

	final String description;

	Permission(@NotNull String description) {
		this.description = description;
	}

}
