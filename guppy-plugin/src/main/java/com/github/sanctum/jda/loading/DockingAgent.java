package com.github.sanctum.jda.loading;

import com.github.sanctum.jda.GuppyAPI;
import com.github.sanctum.jda.GuppyEntryPoint;
import com.github.sanctum.jda.common.Command;
import com.github.sanctum.jda.listener.JDAListenerAdapter;
import com.github.sanctum.jda.util.OptionTypeConverter;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;
import java.util.Locale;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

public final class DockingAgent {

	final PantherCollection<Procedure> procedures = new PantherList<>();


	public DockingAgent consume(@NotNull Procedure procedure) {
		procedures.add(procedure);
		return this;
	}

	public @NotNull JDA deploy() throws LoginException {
		JDABuilder builder = JDABuilder.create(GatewayIntent.GUILD_PRESENCES);
		procedures.forEach(procedure -> {
			try {
				procedure.onConstruct(builder);
			} catch (Exception e) {
				throw new IllegalStateException("Unable to append builder options on procedure " + procedure.getClass().getSimpleName(), e.getCause());
			}
		});
		JDA instance = builder.build();
		procedures.forEach(procedure -> {
			try {
				procedure.onFinalize(instance);
			} catch (Exception e) {
				throw new IllegalStateException("Unable to append finalization on procedure " + procedure.getClass().getSimpleName(), e);
			}
		});
		return instance;
	}

	public interface Procedure {

		default void onConstruct(@NotNull JDABuilder builder) {
		}

		default void onFinalize(@NotNull JDA instance) {
		}

		class Token implements Procedure {
			final GuppyAPI api = GuppyAPI.getInstance();
			@Override
			public void onConstruct(@NotNull JDABuilder builder) {
				String token = api.getConfig().getNode("token").toPrimitive().getString();
				builder.setToken(token);
				builder.enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES);
				// Set activity (like "playing Something")
				Activity action;
				String activity = api.getConfig().getNode("activity.style").toPrimitive().getString();
				String info = api.getConfig().getNode("activity.info").toPrimitive().getString();
				switch (activity.toLowerCase(Locale.ROOT)) {
					case "watching":
						action = Activity.watching(info);
						break;
					case "competing":
						action = Activity.competing(info);
						break;
					case "playing":
						action = Activity.playing(info);
						break;
					default:
						action = Activity.listening(info);
						break;
				}
				builder.setActivity(action);
				builder.addEventListeners(new JDAListenerAdapter());
			}
		}

		class Memory implements Procedure {
			final GuppyEntryPoint entryPoint = GuppyEntryPoint.getInstance();
			final GuppyAPI api = GuppyAPI.getInstance();
			@Override
			public void onConstruct(@NotNull JDABuilder builder) {
				builder.enableCache(CacheFlag.ACTIVITY);
				builder.enableCache(CacheFlag.EMOJI);
				builder.enableCache(CacheFlag.VOICE_STATE);
				builder.setChunkingFilter(ChunkingFilter.ALL);
				builder.setMemberCachePolicy(MemberCachePolicy.ALL);
				int limit = api.getConfig().getNode("estimated-size").toPrimitive().getInt();
				builder.setLargeThreshold(limit + 100);
			}

			@Override
			public void onFinalize(@NotNull JDA instance) {
				instance.getUserCache().forEach(u -> {
					if (!u.isBot()) {
						entryPoint.getGuppy(u);
					}
				});
				PantherCollection<SlashCommandData> data = new PantherList<>();
				for (Command c : GuppyAPI.getInstance().getCommands()) {
					SlashCommandData slashCommand = Commands.slash(c.getLabel(), c.getDescription());
					for (Command.Option o : c.getOptions().get()) {
						OptionType type = OptionTypeConverter.get(o);
						slashCommand.addOption(type, o.getName(), o.getDescription());
					}
					slashCommand.setGuildOnly(true);
					data.add(slashCommand);
				}
				// clear cache.
				instance.updateCommands().queue();
				instance.updateCommands().addCommands(data.stream().toArray(CommandData[]::new)).queue();
			}
		}
	}
}
