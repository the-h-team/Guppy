package com.github.sanctum.jda;

import com.github.sanctum.jda.common.Channel;
import com.github.sanctum.jda.common.Command;
import com.github.sanctum.jda.common.EmbeddedMessage;
import com.github.sanctum.jda.common.Guppy;
import com.github.sanctum.jda.common.GuppyConfigurable;
import com.github.sanctum.jda.common.JDAController;
import com.github.sanctum.jda.common.MusicPlayer;
import com.github.sanctum.jda.common.Reaction;
import com.github.sanctum.jda.common.Role;
import com.github.sanctum.jda.listener.GuppyCommandProcessor;
import com.github.sanctum.jda.listener.JDAListenerAdapter;
import com.github.sanctum.jda.loading.DockingAgent;
import com.github.sanctum.jda.ui.api.ConsoleCommand;
import com.github.sanctum.jda.ui.api.JDAInput;
import com.github.sanctum.jda.ui.content.MainPanel;
import com.github.sanctum.jda.ui.content.StopConsoleCommand;
import com.github.sanctum.jda.util.DefaultAudioListener;
import com.github.sanctum.jda.util.DefaultAudioSendHandler;
import com.github.sanctum.jda.util.InvalidGuppyStateException;
import com.github.sanctum.jda.util.OptionTypeConverter;
import com.github.sanctum.panther.annotation.Comment;
import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.container.ImmutablePantherCollection;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherCollectors;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.event.VentMap;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.JsonConfiguration;
import com.github.sanctum.panther.recursive.ServiceFactory;
import com.github.sanctum.panther.util.Check;
import com.github.sanctum.panther.util.Deployable;
import com.github.sanctum.panther.util.DeployableMapping;
import com.github.sanctum.panther.util.PantherLogger;
import com.github.sanctum.panther.util.ParsedTimeFormat;
import com.github.sanctum.panther.util.SimpleAsynchronousTask;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;
import javax.swing.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.NewsChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GuppyEntryPoint implements Vent.Host {

	public GuppyEntryPoint(Logger logger) {
		this.logger = logger;
	}

	static final PantherMap<String, ConsoleCommand> commands = new PantherEntryMap<>();
	static GuppyEntryPoint entryPoint;
	static MainPanel main;
	JDA jda;
	Guild guild;
	GuppyAPI api;
	boolean active;
	final Logger logger;
	ImmutablePantherCollection.Builder<Guppy> guppySupplier = ImmutablePantherCollection.builder();
	ImmutablePantherCollection.Builder<Command> commandSupplier = ImmutablePantherCollection.builder();

	public void enable(@Nullable DockingAgent dockingAgent) throws InterruptedException, InvalidGuppyStateException {
		if (active) throw new InvalidGuppyStateException("Guppy already running!");
		active = true;
		if (entryPoint == null) entryPoint = this;
		this.api = new Api();
		ServiceFactory.getInstance().newLoader(GuppyAPI.class).supply(api);
		VentMap.getInstance().subscribeAll(this, new GuppyCommandProcessor(), this);
		try {
			if (dockingAgent == null) {
				this.jda = new DockingAgent()
						.consume(new DockingAgent.Procedure.Token())
						.consume(new DockingAgent.Procedure.Memory())
						.deploy();
			} else {
				this.jda = dockingAgent.deploy();
			}
		} catch (LoginException e) {
			logger.severe("Unable to verify bot token.");
		}

		jda.awaitReady();
		final Configurable config = new JsonConfiguration(new File("common"), "config", null);
		GuppyConfigurable g = ServiceFactory.getInstance().getService(GuppyConfigurable.class);
		if (g == null) {
			GuppyConfigurable n = new GuppyConfigurable();
			n.set(config);
			ServiceFactory.getInstance().getLoader(GuppyConfigurable.class).supply(n);
		}
		this.guild = jda.getGuildById(config.getNode("guild").toPrimitive().getLong());
	}

	public void disable() throws InvalidGuppyStateException {
		if (!active) throw new InvalidGuppyStateException("Guppy not active!");
		active = false;
		jda.shutdown();
		guppySupplier = ImmutablePantherCollection.builder();
		commandSupplier = ImmutablePantherCollection.builder();
	}

	@NotNull
	public JDA getJDA() {
		return jda;
	}

	@NotNull
	public Guild getGuild() {
		return guild;
	}

	public @NotNull Guppy.Message wrapPrivateMessage(@NotNull Message message) {
		return new Guppy.Message() {
			@Override
			public @NotNull String getText() {
				return message.getContentDisplay();
			}

			@Override
			public @NotNull Channel getChannel() {
				return new Channel() {
					@Override
					public Deployable<Void> open() {
						return null;
					}

					@Override
					public Deployable<Void> close() {
						return null;
					}

					@Override
					public long getId() {
						return message.getChannel().getIdLong();
					}

					@Override
					public @Nullable Thread getThread(@NotNull String name) {
						return null;
					}

					@Override
					public @Nullable Thread getThread(long id) {
						return null;
					}

					@Override
					public @NotNull PantherCollection<Thread> getThreads() {
						return new PantherList<>();
					}

					@Override
					public boolean isPrivate() {
						return true;
					}

					@Override
					public boolean isNews() {
						return false;
					}

					@Override
					public boolean isVoice() {
						return false;
					}

					@Override
					public void setName(@NotNull String newName) {
						((TextChannel)message.getChannel()).getManager().setName(newName);
					}

					@Override
					public void delete() {
						message.getChannel().delete().queueAfter(2, TimeUnit.MILLISECONDS);
					}

					@Override
					public @NotNull String getName() {
						return message.getChannel().getName();
					}

					@Override
					public Deployable<Guppy.Message> sendMessage(@NotNull String m) {
						return Deployable.of(() -> wrapPrivateMessage(message.getChannel().sendMessage(m).submit().join()), 1);
					}

					@Override
					public Deployable<EmbeddedMessage> sendEmbeddedMessage(@NotNull EmbeddedMessage m) {
						return Deployable.of(() -> {
							EmbedBuilder builder = new EmbedBuilder();
							if (m.getAuthor() != null) {
								builder.setAuthor(m.getAuthor().getTag(), m.getAuthor().getAvatarUrl(), m.getAuthor().getAvatarUrl());
							}
							if (m.getHeader() != null) builder.setTitle(m.getHeader());
							if (m.getFooter() != null) {
								if (m.getFooter().getIconUrl() != null) {
									builder.setFooter(m.getFooter().getText(), m.getFooter().getIconUrl());
								} else {
									builder.setFooter(m.getFooter().getText());
								}
							}
							if (m.getColor() != null) builder.setColor(m.getColor());
							if (m.getThumbnail() != null) {
								builder.setThumbnail(m.getThumbnail().getUrl());
							}
							if (m.getDescription() != null) builder.setDescription(m.getDescription());
							if (m.getImage() != null) {
								builder.setImage(m.getImage().getUrl());
							}
							for (EmbeddedMessage.Field f : m.getFields()) {
								builder.addField(new MessageEmbed.Field(f.getName(), f.getValue(), f.inline()));
							}
							message.getChannel().sendMessageEmbeds(builder.build()).queue();
							return m;
						}, 1);
					}
				};
			}

			@Override
			public @Nullable Channel.Thread getThread() {
				return null;
			}

			@Override
			public @NotNull Reaction[] getReactions() {
				return message.getReactions().stream().map(entryPoint::newReaction).toArray(Reaction[]::new);
			}

			@Override
			public @NotNull EmbeddedMessage[] getAttached() {
				return message.getEmbeds().stream().map(m -> {
					EmbeddedMessage.Builder builder = new EmbeddedMessage.Builder();
					if (m.getAuthor() != null) builder.setAuthor(GuppyAPI.getInstance().getGuppy(m.getAuthor().getName().split("#")[0], false));
					if (m.getTitle() != null) builder.setHeader(m.getTitle());
					if (m.getFooter() != null) builder.setFooter(m.getFooter().getText());
					if (m.getColor() != null) builder.setColor(m.getColor());
					if (m.getThumbnail() != null) builder.setThumbnail(m.getThumbnail().getUrl());
					if (m.getDescription() != null) builder.setDescription(m.getDescription());
					if (m.getImage() != null) builder.setImage(m.getImage().getUrl());
					return builder.build();
				}).toArray(EmbeddedMessage[]::new);
			}

			@Override
			public @Nullable Reaction getReaction(@NotNull String code) {
				return Arrays.stream(getReactions()).filter(r -> r.get().equals(code)).findFirst().orElse(null);
			}

			@Override
			public void add(@NotNull Reaction reaction) {
				Object r = reaction.get();
				if (r instanceof String) {
					message.addReaction(Emoji.fromFormatted((String) r)).queue();
				} else if (r instanceof com.github.sanctum.jda.common.Emoji) {
					message.addReaction(Emoji.fromFormatted(((com.github.sanctum.jda.common.Emoji) r).getFormat())).queue();
				}
			}

			@Override
			public void take(@NotNull Reaction reaction) {
				Object r = reaction.get();
				if (r instanceof String) {
					message.removeReaction(Emoji.fromFormatted((String) r)).queue();
				} else if (r instanceof com.github.sanctum.jda.common.Emoji) {
					message.removeReaction(Emoji.fromFormatted(((com.github.sanctum.jda.common.Emoji) r).getFormat())).queue();
				}
			}

			@Override
			public void delete() {
				SimpleAsynchronousTask.runLater(() -> message.delete().queue(), 4);
			}
		};
	}

	public @NotNull Guppy newGuppy(@NotNull User u) {
		return new Guppy() {

			Link link;
			final Voice voice;

			{
				// search in data for user link.

				this.link = null;
				this.voice = new Voice() {
					final Member m = guild.getMember(u);
					@Override
					public boolean isSelfMuted() {
						return getState().isSelfMuted();
					}

					@NotNull GuildVoiceState getState() {
						assert m != null;
						GuildVoiceState state = m.getVoiceState();
						assert state != null;
						return state;
					}

					@Override
					public boolean isSelfDeafened() {
						return getState().isSelfDeafened();
					}

					@Override
					public boolean isMuted() {
						return getState().isGuildMuted();
					}

					@Override
					public boolean isDeafened() {
						return getState().isGuildDeafened();
					}

					@Override
					public boolean setMuted(boolean muted) {
						if (getState().isSelfMuted()) return false;
						if (muted) getState().declineSpeaker().queue();
						if (!muted) getState().approveSpeaker().queue();
						return true;
					}

					@Override
					public @NotNull Channel getChannel() {
						return new Channel() {
							@Override
							public Deployable<Void> open() {
								return null;
							}

							@Override
							public Deployable<Void> close() {
								return null;
							}

							@Override
							public @Nullable Thread getThread(@NotNull String name) {
								return null;
							}

							@Override
							public @Nullable Thread getThread(long id) {
								return null;
							}

							@Override
							public @NotNull PantherCollection<Thread> getThreads() {
								return new PantherList<>();
							}

							@Override
							public boolean isPrivate() {
								return false;
							}

							@Override
							public boolean isNews() {
								return false;
							}

							@Override
							public boolean isVoice() {
								return true;
							}

							@Override
							public void setName(@NotNull String newName) {
								getState().getChannel().getManager().setName(newName).queue();
							}

							@Override
							public void delete() {
								getState().getChannel().delete().queueAfter(2, TimeUnit.MILLISECONDS);
							}

							@Override
							public long getId() {
								return getState().getChannel().getIdLong();
							}

							@Override
							public Deployable<Message> sendMessage(@NotNull String message) {
								return Check.forNull(null, "Messages cannot be sent to voice channels!");
							}

							@Override
							public @NotNull String getName() {
								return getState().getChannel().getName();
							}
						};
					}
				};
			}

			@Override
			public @NotNull String getName() {
				return u.getName();
			}

			@Override
			public @NotNull String getTag() {
				return u.getAsTag();
			}

			@Override
			public @NotNull String getAsMention() {
				return u.getAsMention();
			}

			@Override
			public @NotNull String getAvatarUrl() {
				return u.getEffectiveAvatarUrl();
			}

			@Override
			public long getId() {
				return u.getIdLong();
			}

			@Override
			public @Nullable Link getLink() {
				return link;
			}

			@Override
			public @NotNull Voice getVoice() {
				return voice;
			}

			@Override
			public @NotNull Role[] getRoles() {
				return jda.getGuildById(ServiceFactory.getInstance().getService(GuppyConfigurable.class).get().getNode("guild").toPrimitive().getLong()).getRoles().stream().map(GuppyEntryPoint.this::newRole).toArray(Role[]::new);
			}

			@Override
			public @Nullable Role getRole(@NotNull String name) {
				return Arrays.stream(getRoles()).filter(r -> r.getName().equals(name)).findFirst().orElse(null);
			}

			@Override
			public @Nullable Role getRole(long id) {
				return Arrays.stream(getRoles()).filter(r -> r.getId() == id).findFirst().orElse(null);
			}

			@Override
			public Deployable<Void> setLink(@NotNull Link link) {
				return Deployable.of(() -> {
					this.link = link;
				}, 0);
			}

			@Override
			public Deployable<Message> sendMessage(@NotNull String message) {
				return Deployable.of(() -> wrapPrivateMessage(u.openPrivateChannel().map(privateChannel -> privateChannel.sendMessage(message).submit().join()).submit().join()), 1);
			}

			@Override
			public Deployable<EmbeddedMessage> sendEmbeddedMessage(@NotNull EmbeddedMessage m) {
				return Deployable.of(() -> {
					EmbedBuilder builder = new EmbedBuilder();
					if (m.getAuthor() != null) {
						builder.setAuthor(m.getAuthor().getTag(), m.getAuthor().getAvatarUrl(), m.getAuthor().getAvatarUrl());
					}
					if (m.getHeader() != null) builder.setTitle(m.getHeader());
					if (m.getFooter() != null) {
						if (m.getFooter().getIconUrl() != null) {
							builder.setFooter(m.getFooter().getText(), m.getFooter().getIconUrl());
						} else {
							builder.setFooter(m.getFooter().getText());
						}
					}
					if (m.getColor() != null) builder.setColor(m.getColor());
					if (m.getThumbnail() != null) {
						builder.setThumbnail(m.getThumbnail().getUrl());
					}
					if (m.getDescription() != null) builder.setDescription(m.getDescription());
					if (m.getImage() != null) {
						builder.setImage(m.getImage().getUrl());
					}
					for (EmbeddedMessage.Field f : m.getFields()) {
						builder.addField(new MessageEmbed.Field(f.getName(), f.getValue(), f.inline()));
					}
					u.openPrivateChannel().map(privateChannel -> privateChannel.sendMessageEmbeds(builder.build()).submit().join()).queue();
					return m;
				}, 1);
			}

		};
	}

	public @NotNull Channel newChannel(@NotNull MessageChannel t) {
		return new Channel() {

			@Override
			public Deployable<Void> open() {
				return null;
			}

			@Override
			public Deployable<Void> close() {
				return null;
			}

			@Override
			public @NotNull String getName() {
				return t.getName();
			}

			@Override
			public long getId() {
				return t.getIdLong();
			}

			@Override
			public @Nullable Thread getThread(@NotNull String name) {
				return getThreads().stream().filter(t1 -> t1.getName().equals(name)).findFirst().orElse(null);
			}

			@Override
			public @Nullable Thread getThread(long id) {
				return getThreads().stream().filter(t1 -> t1.getId() == id).findFirst().orElse(null);
			}

			@Override
			public @NotNull PantherCollection<Thread> getThreads() {
				if (!(t instanceof TextChannel)) return new PantherList<>();
				return ((TextChannel)t).getThreadChannels().stream().map(threadChannel -> new Thread() {
					@Override
					public @NotNull String getName() {
						return threadChannel.getName();
					}

					@Override
					public long getId() {
						return threadChannel.getIdLong();
					}

					@Override
					public boolean isOwned() {
						return !threadChannel.isOwner();
					}

					@Override
					public @Nullable Guppy getOwner() {
						if (!isOwned()) return null;
						return api.getGuppy(threadChannel.getOwner().getIdLong());
					}

					@Override
					public @NotNull Channel getParent() {
						Channel c = api.getChannel(threadChannel.getParentChannel().getIdLong());
						assert c != null;
						return c;
					}

					@Override
					public void delete() {
						threadChannel.delete().queueAfter(2, TimeUnit.MILLISECONDS);
					}

					@Override
					public Deployable<Guppy.Message> sendMessage(@NotNull String message) {
						return Deployable.of(() -> wrapPrivateMessage(threadChannel.sendMessage(message).submit().join()), 1);
					}

					@Override
					public Deployable<EmbeddedMessage> sendEmbeddedMessage(@NotNull EmbeddedMessage m) {
						return Deployable.of(() -> {
							EmbedBuilder builder = new EmbedBuilder();
							if (m.getAuthor() != null) {
								builder.setAuthor(m.getAuthor().getTag(), m.getAuthor().getAvatarUrl(), m.getAuthor().getAvatarUrl());
							}
							if (m.getHeader() != null) builder.setTitle(m.getHeader());
							if (m.getFooter() != null) {
								if (m.getFooter().getIconUrl() != null) {
									builder.setFooter(m.getFooter().getText(), m.getFooter().getIconUrl());
								} else {
									builder.setFooter(m.getFooter().getText());
								}
							}
							if (m.getColor() != null) builder.setColor(m.getColor());
							if (m.getThumbnail() != null) {
								builder.setThumbnail(m.getThumbnail().getUrl());
							}
							if (m.getDescription() != null) builder.setDescription(m.getDescription());
							if (m.getImage() != null) {
								builder.setImage(m.getImage().getUrl());
							}
							for (EmbeddedMessage.Field f : m.getFields()) {
								builder.addField(new MessageEmbed.Field(f.getName(), f.getValue(), f.inline()));
							}
							threadChannel.sendMessageEmbeds(builder.build()).queue();
							return m;
						}, 1);
					}
				}).collect(PantherCollectors.toImmutableList());
			}

			@Override
			public boolean isPrivate() {
				return false;
			}

			@Override
			public boolean isNews() {
				return t instanceof NewsChannel;
			}

			@Override
			public boolean isVoice() {
				return false;
			}

			@Override
			public void setName(@NotNull String newName) {
				if (t instanceof TextChannel) ((TextChannel)t).getManager().setName(newName).queue();
			}

			@Override
			public void delete() {
				t.delete().queueAfter(2, TimeUnit.MILLISECONDS);
			}

			@Override
			public Deployable<Guppy.Message> sendMessage(@NotNull String message) {
				return Deployable.of(() -> wrapPrivateMessage(t.sendMessage(message).submit().join()), 1);
			}

			@Override
			public Deployable<EmbeddedMessage> sendEmbeddedMessage(@NotNull EmbeddedMessage m) {
				return Deployable.of(() -> {
					EmbedBuilder builder = new EmbedBuilder();
					if (m.getAuthor() != null) {
						builder.setAuthor(m.getAuthor().getTag(), m.getAuthor().getAvatarUrl(), m.getAuthor().getAvatarUrl());
					}
					if (m.getHeader() != null) builder.setTitle(m.getHeader());
					if (m.getFooter() != null) {
						if (m.getFooter().getIconUrl() != null) {
							builder.setFooter(m.getFooter().getText(), m.getFooter().getIconUrl());
						} else {
							builder.setFooter(m.getFooter().getText());
						}
					}
					if (m.getColor() != null) builder.setColor(m.getColor());
					if (m.getThumbnail() != null) {
						builder.setThumbnail(m.getThumbnail().getUrl());
					}
					if (m.getDescription() != null) builder.setDescription(m.getDescription());
					if (m.getImage() != null) {
						builder.setImage(m.getImage().getUrl());
					}
					for (EmbeddedMessage.Field f : m.getFields()) {
						builder.addField(new MessageEmbed.Field(f.getName(), f.getValue(), f.inline()));
					}
					t.sendMessageEmbeds(builder.build()).queue();
					return m;
				}, 1);
			}
		};
	}

	public @NotNull Channel newChannel(@NotNull VoiceChannel t) {
		return new Channel() {

			@Override
			public Deployable<Void> open() {
				return Deployable.of(() -> {
					final MusicPlayer.SendHandler handler = GuppyAPI.getInstance().getPlayer().getSendHandler();
					getGuild().getAudioManager().setSendingHandler(handler instanceof AudioSendHandler ? (AudioSendHandler) handler : new AudioSendHandler() {
						@Override
						public boolean canProvide() {
							return handler.canProvide();
						}

						@Nullable
						@Override
						public ByteBuffer provide20MsAudio() {
							return handler.provide20MsAudio();
						}

						@Override
						public boolean isOpus() {
							return handler.isOpus();
						}
					});
					getGuild().getAudioManager().openAudioConnection(getJDA().getVoiceChannelById(getId()));
				}, 1);
			}

			@Override
			public Deployable<Void> close() {
				return Deployable.of(() -> getGuild().getAudioManager().closeAudioConnection(), 1);
			}

			@Override
			public @NotNull String getName() {
				return t.getName();
			}

			@Override
			public long getId() {
				return t.getIdLong();
			}

			@Override
			public @Nullable Thread getThread(@NotNull String name) {
				return getThreads().stream().filter(t1 -> t1.getName().equals(name)).findFirst().orElse(null);
			}

			@Override
			public @Nullable Thread getThread(long id) {
				return getThreads().stream().filter(t1 -> t1.getId() == id).findFirst().orElse(null);
			}

			@Override
			public @NotNull PantherCollection<Thread> getThreads() {
				return new PantherList<>();
			}

			@Override
			public boolean isPrivate() {
				return false;
			}

			@Override
			public boolean isNews() {
				return false;
			}

			@Override
			public boolean isVoice() {
				return true;
			}

			@Override
			public void setName(@NotNull String newName) {
				t.getManager().setName(newName).queueAfter(2, TimeUnit.MILLISECONDS);
			}

			@Override
			public void delete() {
				t.delete().queueAfter(2, TimeUnit.MILLISECONDS);
			}

			@Override
			public Deployable<Guppy.Message> sendMessage(@NotNull String message) {
				return Deployable.of(() -> {
					return null;
				}, 1);
			}

			@Override
			public Deployable<EmbeddedMessage> sendEmbeddedMessage(@NotNull EmbeddedMessage m) {
				return Deployable.of(() -> {
					return m;
				}, 1);
			}
		};
	}

	public Role newRole(@NotNull net.dv8tion.jda.api.entities.Role role) {
		return new Role() {

			@Override
			public @NotNull String getName() {
				return role.getName();
			}

			@Override
			public Deployable<Guppy.Message> sendMessage(@NotNull String message) {
				return null;
			}

			@Override
			public long getId() {
				return role.getIdLong();
			}
		};
	}

	public Reaction newReaction(@NotNull MessageReaction reaction) {
		return new Reaction() {
			@Override
			public @NotNull String get() {
				return reaction.getEmoji().getFormatted();
			}

			@Override
			public long count() {
				return reaction.getCount();
			}

			@Override
			public void remove(@NotNull Guppy guppy) {
				reaction.removeReaction(entryPoint.getJDA().getUserById(guppy.getId())).queueAfter(2, TimeUnit.MILLISECONDS);
			}

			@Override
			public @NotNull Guppy[] getGuppies() {
				return reaction.retrieveUsers().stream().map(entryPoint::getGuppy).toArray(Guppy[]::new);
			}
		};
	}

	@Note("Used to load commands into cache.")
	public void newCommand(@NotNull Command command) {
		commandSupplier.add(command);
	}

	public @NotNull Guppy getGuppy(@NotNull User u) {
		return Optional.ofNullable(api.getGuppy(u.getName(), false)).orElse(newGuppy(u));
	}

	public static GuppyEntryPoint getInstance() {
		return entryPoint;
	}

	class Api implements GuppyAPI {

		boolean loaded = true;
		final MusicPlayer player = new MusicPlayer() {

			private final AudioPlayer player;
			private final Controller controller;
			private final Queue queue;
			private final DefaultAudioSendHandler sendWrapper;
			private final AudioPlayerManager audioPlayerManager;

			{
				this.audioPlayerManager = new DefaultAudioPlayerManager();
				this.player = audioPlayerManager.createPlayer();
				this.controller = new Controller() {
					@Override
					public Track getPlaying() {
						return new Track() {
							AudioTrack handle = player.getPlayingTrack();
							@Override
							public @NotNull String getAuthor() {
								return handle.getInfo().author;
							}

							@Override
							public void setPosition(long position) {
								handle.setPosition(position);
							}

							@Override
							public void stop() {
								handle.stop();
							}

							@Override
							public long getDuration() {
								return handle.getDuration();
							}

							@Override
							public @NotNull Object getHandle() {
								return handle;
							}

							@Override
							public @NotNull String getName() {
								return handle.getInfo().title;
							}
						};
					}

					@Override
					public boolean start(Track track, boolean interrupt) {
						return player.startTrack((AudioTrack) track.getHandle(), !interrupt);
					}

					@Override
					public void play(Track track) {
						player.playTrack((AudioTrack) track.getHandle());
					}

					@Override
					public void stop() {
						player.stopTrack();
					}

					@Override
					public int getVolume() {
						return player.getVolume();
					}

					@Override
					public void setVolume(int volume) {
						player.setVolume(volume);
					}

					@Override
					public boolean isPaused() {
						return player.isPaused();
					}

					@Override
					public void setPaused(boolean value) {
						player.setPaused(value);
					}

					@Override
					public void destroy() {
						player.destroy();
					}

					@Override
					public void cleanup(long threshold) {
						player.checkCleanup(threshold);
					}

					@Override
					public boolean isConnected() {
						return getGuild().getAudioManager().isConnected();
					}
				};
				this.queue = new Queue(controller);
				this.player.addListener(new DefaultAudioListener(getChannel(ServiceFactory.getInstance().getService(GuppyConfigurable.class).get().getNode("channels").getNode("commands").getNode("id").toPrimitive().getLong()), this));
				this.sendWrapper = new DefaultAudioSendHandler(player);
				AudioSourceManagers.registerRemoteSources(audioPlayerManager);
			}


			@Override
			public @NotNull Controller getController() {
				return controller;
			}

			@Override
			public @NotNull Queue getQueue() {
				return queue;
			}

			@Override
			public @NotNull SendHandler getSendHandler() {
				return sendWrapper;
			}

			@Override
			public void stop() {
				player.destroy();
			}

			@Override
			public void load(@NotNull Channel channel, @NotNull String url) {
				audioPlayerManager.loadItemOrdered(this, url, new AudioLoadResultHandler() {
					@Override
					public void trackLoaded(AudioTrack track) {
						queue.add(new Track() {
							final AudioTrack handle = track;
							@Override
							public @NotNull String getAuthor() {
								return handle.getInfo().author;
							}

							@Override
							public void setPosition(long position) {
								handle.setPosition(position);
							}

							@Override
							public void stop() {
								handle.stop();
							}

							@Override
							public long getDuration() {
								return handle.getDuration();
							}

							@Override
							public @NotNull Object getHandle() {
								return handle;
							}

							@Override
							public @NotNull String getName() {
								return handle.getInfo().title;
							}
						});
						ParsedTimeFormat timeFormat = ParsedTimeFormat.of(TimeUnit.MILLISECONDS.toSeconds(track.getInfo().length));
						channel.sendMessage("Queued song **" + track.getInfo().title + "** by **" + track.getInfo().author + "** with duration **" + timeFormat.getHours() + ":" + timeFormat.getMinutes() + ":" + timeFormat.getSeconds() + "**").queue();
					}

					@Override
					public void playlistLoaded(AudioPlaylist playlist) {
						List<AudioTrack> tracks = playlist.getTracks();
						if (!tracks.isEmpty()) {
							// add possible filter to attempt hd vids first
							AudioTrack track = tracks.get(0);
							queue.add(new Track() {
								final AudioTrack handle = track;
								@Override
								public @NotNull String getAuthor() {
									return handle.getInfo().author;
								}

								@Override
								public void setPosition(long position) {
									handle.setPosition(position);
								}

								@Override
								public void stop() {
									handle.stop();
								}

								@Override
								public long getDuration() {
									return handle.getDuration();
								}

								@Override
								public @NotNull Object getHandle() {
									return handle;
								}

								@Override
								public @NotNull String getName() {
									return handle.getInfo().title;
								}
							});
							ParsedTimeFormat timeFormat = ParsedTimeFormat.of(TimeUnit.MILLISECONDS.toSeconds(track.getInfo().length));
							channel.sendMessage("Queued song **" + track.getInfo().title + "** by **" + track.getInfo().author + "** with duration **" + timeFormat.getHours() + ":" + timeFormat.getMinutes() + ":" + timeFormat.getSeconds() + "**").queue();
						}
					}

					@Override
					public void noMatches() {
						channel.sendMessage("Sorry {0} Unable to play track " + url).queue();					}

					@Override
					public void loadFailed(FriendlyException exception) {
						exception.printStackTrace();
						channel.sendMessage("Sorry {0} Unable to play track " + url).queue();
					}
				});
			}

			@Override
			public void load(@NotNull Guppy guppy, @NotNull Channel channel, @NotNull String url) {
				audioPlayerManager.loadItemOrdered(this, url, new AudioLoadResultHandler() {
					@Override
					public void trackLoaded(AudioTrack track) {
						queue.add(new Track() {
							final AudioTrack handle = track;
							@Override
							public @NotNull String getAuthor() {
								return handle.getInfo().author;
							}

							@Override
							public void setPosition(long position) {
								handle.setPosition(position);
							}

							@Override
							public void stop() {
								handle.stop();
							}

							@Override
							public long getDuration() {
								return handle.getDuration();
							}

							@Override
							public @NotNull Object getHandle() {
								return handle;
							}

							@Override
							public @NotNull String getName() {
								return handle.getInfo().title;
							}
						});
						ParsedTimeFormat timeFormat = ParsedTimeFormat.of(TimeUnit.MILLISECONDS.toSeconds(track.getInfo().length));
						channel.sendMessage(guppy.getAsMention() + " queued song **" + track.getInfo().title + "** by **" + track.getInfo().author + "** with duration **" + timeFormat.getHours() + ":" + timeFormat.getMinutes() + ":" + timeFormat.getSeconds() + "**").queue();
					}

					@Override
					public void playlistLoaded(AudioPlaylist playlist) {
						List<AudioTrack> tracks = playlist.getTracks();
						if (!tracks.isEmpty()) {
							// add possible filter to attempt hd vids first
							AudioTrack track = tracks.get(0);
							queue.add(new Track() {
								final AudioTrack handle = track;
								@Override
								public @NotNull String getAuthor() {
									return handle.getInfo().author;
								}

								@Override
								public void setPosition(long position) {
									handle.setPosition(position);
								}

								@Override
								public void stop() {
									handle.stop();
								}

								@Override
								public long getDuration() {
									return handle.getDuration();
								}

								@Override
								public @NotNull Object getHandle() {
									return handle;
								}

								@Override
								public @NotNull String getName() {
									return handle.getInfo().title;
								}
							});
							ParsedTimeFormat timeFormat = ParsedTimeFormat.of(TimeUnit.MILLISECONDS.toSeconds(track.getInfo().length));
							channel.sendMessage(guppy.getAsMention() + " queued song **" + track.getInfo().title + "** by **" + track.getInfo().author + "** with duration **" + timeFormat.getHours() + ":" + timeFormat.getMinutes() + ":" + timeFormat.getSeconds() + "**").queue();
						}
					}

					@Override
					public void noMatches() {
						channel.sendMessage(MessageFormat.format("Sorry {0} Unable to find track ", guppy.getAsMention()) + url).queue();
					}

					@Override
					public void loadFailed(FriendlyException exception) {
						exception.printStackTrace();
						channel.sendMessage(MessageFormat.format("Sorry {0} Unable to play track ", guppy.getAsMention()) + url).queue();
					}
				});
			}
		};
		final JDAController controller = new JDAController() {
			@Override
			public boolean isRunning() {
				return false;
			}

			@Override
			public Deployable<JDAController> stop() {
				return Deployable.of(() -> {
					if (Api.this.loaded) {
						SimpleAsynchronousTask.runNow(() -> {
							try {
								disable();
							} catch (InvalidGuppyStateException e) {
								e.printStackTrace();
							}
						});
						Api.this.loaded = false;
					}
					return null;
				}, 1);
			}

			@Override
			public Deployable<JDAController> start() {
				return Deployable.of(() -> {
					if (!Api.this.loaded) {
						SimpleAsynchronousTask.runNow(() -> {
							try {
								enable(null);
							} catch (InterruptedException | InvalidGuppyStateException e) {
								e.printStackTrace();
							}
						});
						Api.this.loaded = true;
					}
					return this;
				}, 1);
			}

			@Override
			public Deployable<JDAController> restart() {
				return Deployable.of(() -> {
					logger.warning("---------------------------------");
					logger.warning("Guppy is now attempting a bot restart..");
					logger.warning("---------------------------------");
					SimpleAsynchronousTask.runNow(() -> {
						try {
							disable();
						} catch (InvalidGuppyStateException e) {
							e.printStackTrace();
						}
					});
					SimpleAsynchronousTask.runLater(() -> {
						try {
							enable(null);
							logger.warning("---------------------------------");
							logger.warning("Guppy bot provider restarted");
							logger.warning("---------------------------------");
						} catch (InterruptedException | InvalidGuppyStateException e) {
							logger.severe("---------------------------------");
							logger.severe("Guppy bot provider failed to restart: " + e.getMessage());
							logger.severe("---------------------------------");
							e.printStackTrace();
						}
					}, 180);
					return this;
				}, 1);
			}

			@Override
			public Deployable<JDAController> restartAfter(long wait) {
				return Deployable.of(() -> {
					SimpleAsynchronousTask.runLater(restart()::deploy, wait);
					return this;
				}, 1);
			}
		};

		@Override
		public @Nullable Guppy getGuppy(@NotNull String name, boolean isMention) {
			Stream<Guppy> guppieStream;
			if (isMention) {
				guppieStream = getGuppies().stream().filter(g -> g.getAsMention().equalsIgnoreCase(name));
			} else {
				guppieStream = getGuppies().stream().filter(g -> g.getName().equalsIgnoreCase(name));
			}
			return guppieStream.findFirst().orElse(null);
		}

		@Override
		public @Nullable Guppy getGuppy(long id) {
			return getGuppies().stream().filter(g -> g.getId() == id).findFirst().orElse(null);
		}

		@Override
		public @NotNull PantherCollection<Guppy> getGuppies() {
			return jda.getUserCache().stream().map(GuppyEntryPoint.this::newGuppy).collect(PantherCollectors.toSet());
		}

		@Override
		public @Nullable Channel getChannel(@NotNull String name) {
			return getChannels().stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
		}

		@Override
		public @Nullable Channel getChannel(long id) {
			return getChannels().stream().filter(c -> c.getId() == id).findFirst().orElse(null);
		}

		@Override
		public @NotNull Channel newChannel(@NotNull String name, long categoryId) {
			Channel test = getChannel(name);
			if (test != null) return test;
			Guild guild = getJDA().getGuildById(ServiceFactory.getInstance().getService(GuppyConfigurable.class).get().getNode("guild").toPrimitive().getLong());
			if (categoryId > 0) {
				return GuppyEntryPoint.this.newChannel(guild.createTextChannel(name, getJDA().getCategoryById(categoryId)).addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
						.submit().join());
			} else {
				return GuppyEntryPoint.this.newChannel(guild.createTextChannel(name).addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
						.submit().join());
			}
		}

		public Channel memberInitialize(Guild guild, User user, String name, long categoryId) {
			Member member = guild.getMember(user);
			if (categoryId > 0) {
				return GuppyEntryPoint.this.newChannel(guild.createTextChannel(name, getJDA().getCategoryById(categoryId))
						.addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
						.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
						.addPermissionOverride(getJDA().getRoleById(751689293721895013L), Arrays.asList(Permission.values()), null)
						.addPermissionOverride(getJDA().getRoleById(570087140457840640L), Arrays.asList(Permission.values()), null)
						.submit().join());
			} else {
				return GuppyEntryPoint.this.newChannel(guild.createTextChannel(name)
						.addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
						.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
						.addPermissionOverride(getJDA().getRoleById(751689293721895013L), Arrays.asList(Permission.values()), null)
						.addPermissionOverride(getJDA().getRoleById(570087140457840640L), Arrays.asList(Permission.values()), null)
						.submit().join());
			}
		}

		@Override
		public @NotNull Channel newChannel(@NotNull Guppy guppy, @NotNull String name, long categoryId) {
			Channel test = getChannel(name);
			if (test != null) return test;
			User match = getJDA().getUserById(guppy.getId());
			Guild guild = getJDA().getGuildById(ServiceFactory.getInstance().getService(GuppyConfigurable.class).get().getNode("guild").toPrimitive().getLong());
			return memberInitialize(guild, match, name, categoryId);
		}

		@Override
		public @NotNull PantherCollection<Channel> getChannels() {
			PantherCollection<Channel> channels = jda.getTextChannels().stream().map(GuppyEntryPoint.this::newChannel).collect(PantherCollectors.toList());
			channels.addAll(jda.getNewsChannels().stream().map(GuppyEntryPoint.this::newChannel).collect(Collectors.toList()));
			channels.addAll(jda.getVoiceChannels().stream().map(GuppyEntryPoint.this::newChannel).collect(Collectors.toList()));
			return channels;
		}

		@Override
		public @Nullable Command getCommand(@NotNull String label) {
			return getCommands().stream().filter(c -> c.getLabel().equalsIgnoreCase(label)).findFirst().orElse(null);
		}

		@Override
		public @NotNull PantherCollection<Command> getCommands() {
			return commandSupplier.build();
		}

		@Override
		public @NotNull Deployable<Void> updateCommands() {
			return Deployable.of(() -> {
				PantherCollection<SlashCommandData> data = new PantherList<>();
				for (Command c : getCommands()) {
					SlashCommandData slashCommand = Commands.slash(c.getLabel(), c.getDescription());
					for (Command.Option o : c.getOptions().get()) {
						OptionType type = OptionTypeConverter.get(o);
						slashCommand.addOption(type, o.getName(), o.getDescription());
					}
					slashCommand.setGuildOnly(true);
					data.add(slashCommand);
				}
				// clear cache.
				jda.updateCommands().queue();
				jda.updateCommands().addCommands(data.stream().toArray(CommandData[]::new)).queue();
				return null;
			}, 1);
		}

		@Override
		public @Nullable Role getRole(@NotNull String name) {
			return getRoles().stream().filter(r -> r.getName().equals(name)).findFirst().orElse(null);
		}

		@Override
		public @Nullable Role getRole(long id) {
			return getRoles().stream().filter(r -> r.getId() == id).findFirst().orElse(null);
		}

		@Override
		public @NotNull PantherCollection<Role> getRoles() {
			return jda.getRoleCache().stream().map(GuppyEntryPoint.this::newRole).collect(PantherCollectors.toImmutableList());
		}

		@Override
		public com.github.sanctum.jda.common.@Nullable Emoji getEmoji(@NotNull String name) {
			return getEmojis().stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null);
		}

		@Override
		public com.github.sanctum.jda.common.@Nullable Emoji getEmoji(long id) {
			return getEmojis().stream().filter(e -> e.getId() == id).findFirst().orElse(null);
		}

		@Override
		public @NotNull PantherCollection<com.github.sanctum.jda.common.Emoji> getEmojis() {
			return jda.getEmojiCache().stream().map(e -> new com.github.sanctum.jda.common.Emoji() {
				@Override
				public @NotNull String getFormat() {
					return e.getFormatted();
				}

				@Override
				public long getId() {
					return e.getIdLong();
				}

				@Override
				public @NotNull String getName() {
					return e.getName();
				}
			}).collect(PantherCollectors.toList());
		}

		@Override
		public @NotNull JDAController getController() {
			return controller;
		}

		@Override
		public @NotNull("Config not initialized!") Configurable getConfig() {
			return ServiceFactory.getInstance().getService(GuppyConfigurable.class).get();
		}

		@Override
		public @NotNull Vent.Host getHost() {
			return GuppyEntryPoint.this;
		}

		@Override
		public @NotNull MusicPlayer getPlayer() {
			return null;
		}

	}

	public static PantherCollection<ConsoleCommand> getConsoleCommands() {
		return commands.values();
	}

	public static @NotNull("Main panel not loaded!") MainPanel getMainPanel() {
		return main;
	}

	@Comment("This method handles application stuff.")
	public static void main(String[] args) {
		commands.put("stop", new StopConsoleCommand());

		// open gui
		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(true);
		window.setUndecorated(true);
		window.setTitle("Guppy Bot");

		MainPanel m = (main = new MainPanel(window) {
		});
		m.start();
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		JDAInput input = new JDAInput();
		String test = JOptionPane.showInputDialog("Enter your bot token.");
		if (test == null || test.isEmpty())
			test = JOptionPane.showInputDialog("Bot token required! Please provide one");
		input.setToken(test);
		String[] s = new String[]{"A.) Watching", "B.) Competing", "C.) Playing", "D.) Listening"};
		input.setActivity(s[JOptionPane.showOptionDialog(null,
				"Now select a status type.",
				"Good....",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE,
				null,
				s,
				s[0])]);
		input.setActivityMessage(JOptionPane.showInputDialog("Now enter a status message."));
		Activity activity = Activity.watching("Aqua Teen Hunger Force.");
		switch (input.getActivity().toLowerCase(Locale.ROOT)) {
			case "a.) watching":
				activity = Activity.watching(input.getActivityMessage());
				break;
			case "b.) competing":
				activity = Activity.competing(input.getActivityMessage());
				break;
			case "c.) playing":
				activity = Activity.playing(input.getActivityMessage());
				break;
			case "d.) listening":
				activity = Activity.listening(input.getActivityMessage());
				break;
		}

		SimpleAsynchronousTask.runLater(() -> {
			main.getConsole().sendMessage("Say " + '"' + "stop" + '"' + " or " + '"' + "exit" + '"' + " to close this application.");
		}, TimeUnit.SECONDS.toMillis(3));

		SimpleAsynchronousTask.runLater(() -> {
			main.getConsole().sendMessage("---------------------------");
		}, TimeUnit.SECONDS.toMillis(4));

		entryPoint = new GuppyEntryPoint(PantherLogger.getInstance().getLogger());
		try {
			Activity finalActivity = activity;
			entryPoint.enable(new DockingAgent().consume(new DockingAgent.Procedure() {
				@Override
				public void onConstruct(@NotNull JDABuilder builder) {
					builder.setToken(input.getToken());
					builder.enableIntents(Arrays.asList(GatewayIntent.values()));
					builder.setActivity(finalActivity);
					builder.addEventListeners(new JDAListenerAdapter());
					builder.enableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.VOICE_STATE, CacheFlag.ONLINE_STATUS);

					builder.setChunkingFilter(ChunkingFilter.ALL);
					builder.setMemberCachePolicy(MemberCachePolicy.ALL);
					builder.setLargeThreshold(300);
				}

				@Override
				public void onFinalize(@NotNull JDA instance) {
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
			}));
			main.setJda(entryPoint.jda);
		} catch (InterruptedException | InvalidGuppyStateException e) {
			e.printStackTrace();
			main.getConsole().sendMessage("Bot failed to activate.");
			main.getConsole().sendMessage("Reason: " + e.getMessage());
			main.getConsole().sendMessage("---------------------------");
			return;
		}

		SimpleAsynchronousTask.runLater(() -> {
			main.getConsole().sendMessage("Bot active.");
			main.getConsole().sendMessage("---------------------------");
		}, TimeUnit.SECONDS.toMillis(5));
	}

}
