package com.github.sanctum.jda;

import com.github.sanctum.jda.addon.DiscordExtensionManager;
import com.github.sanctum.jda.common.Channel;
import com.github.sanctum.jda.common.Command;
import com.github.sanctum.jda.common.CommandController;
import com.github.sanctum.jda.common.Dialogue;
import com.github.sanctum.jda.common.EmbeddedMessage;
import com.github.sanctum.jda.common.Guppy;
import com.github.sanctum.jda.common.GuppyConfigurable;
import com.github.sanctum.jda.common.JDAController;
import com.github.sanctum.jda.common.MenuDialogue;
import com.github.sanctum.jda.common.MusicPlayer;
import com.github.sanctum.jda.common.Reaction;
import com.github.sanctum.jda.common.Role;
import com.github.sanctum.jda.listener.GuppyCommandProcessor;
import com.github.sanctum.jda.listener.JDAListenerAdapter;
import com.github.sanctum.jda.loading.DockingAgent;
import com.github.sanctum.jda.common.api.ConsoleCommand;
import com.github.sanctum.jda.common.api.JDAInput;
import com.github.sanctum.jda.common.content.CommandAddon;
import com.github.sanctum.jda.common.content.MainPanel;
import com.github.sanctum.jda.common.content.CommandStop;
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
import com.github.sanctum.panther.container.PantherSet;
import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.event.VentMap;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.JsonConfiguration;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.recursive.ServiceFactory;
import com.github.sanctum.panther.recursive.ServiceLoader;
import com.github.sanctum.panther.util.Deployable;
import com.github.sanctum.panther.util.PantherLogger;
import com.github.sanctum.panther.util.ParsedTimeFormat;
import com.github.sanctum.panther.util.SimpleAsynchronousTask;
import com.github.sanctum.panther.util.TaskChain;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.awt.*;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;
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
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GuppyEntryPoint implements Vent.Host {

	static final PantherMap<String, ConsoleCommand> consoleCommands = new PantherEntryMap<>();
	static GuppyEntryPoint entryPoint;
	JDA jda;
	Guild guild;
	GuppyAPI api;
	boolean active;
	final Logger logger;
	final PantherCollection<Command> commandSupplier = new PantherSet<>();
	public final PantherMap<Guppy, Modal> modals = new PantherEntryMap<>();
	ImmutablePantherCollection.Builder<Guppy> guppySupplier = ImmutablePantherCollection.builder();

	public GuppyEntryPoint(Logger logger) {
		this.logger = logger;
	}

	static {
		consoleCommands.put("stop", new CommandStop());
		consoleCommands.put("addon", new CommandAddon());
	}

	public void enable(@Nullable @Note("Passing null insinuates standalone") DockingAgent dockingAgent) throws InterruptedException, InvalidGuppyStateException {
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
				final Configurable config = new JsonConfiguration(new File("common"), "config", null);
				ServiceLoader g = ServiceFactory.getInstance().newLoader(GuppyConfigurable.class);
				GuppyConfigurable n = new GuppyConfigurable();
				n.set(config);
				g.supply(n);
			}
		} catch (LoginException e) {
			logger.severe("Unable to verify bot token.");
		}
		jda.awaitReady();
		this.guild = jda.getGuildById(api.getConfig().getNode("guild").toPrimitive().getLong());
	}

	public void disable() throws InvalidGuppyStateException {
		if (!active) throw new InvalidGuppyStateException("Guppy not active!");
		active = false;
		jda.shutdown();
		guppySupplier = ImmutablePantherCollection.builder();
		commandSupplier.clear();
	}

	@Nullable
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
			public long getId() {
				return message.getIdLong();
			}

			@Override
			public @NotNull Channel getChannel() {
				return newChannel(message.getChannel());
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
					if (m.getAuthor() != null)
						builder.setAuthor(GuppyAPI.getInstance().getGuppy(m.getAuthor().getName().split("#")[0], false));
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

	public @NotNull Modal newModal(@NotNull Dialogue dialogue) {
		if (dialogue instanceof MenuDialogue) {

		}
		PantherCollection<TextInput> inputs = new PantherList<>();
		for (Dialogue.Row r : dialogue.getRows()) {
			TextInput.Builder text = TextInput.create(r.getId(), r.getLabel(), r.isShort() ? TextInputStyle.SHORT : TextInputStyle.PARAGRAPH);
			if (r.getPlaceholder() != null) text.setPlaceholder(r.getPlaceholder());
			if (r.isRequired()) text.setRequired(true);
			text.setRequiredRange(r.getMinCharactersAllowed(), r.getMaxCharactersAllowed());
			inputs.add(text.build());
		}
		Modal modal = Modal.create(dialogue.getId(), dialogue.getTitle())
				.addActionRows(inputs.toArray(new ActionRow[0]))
				.build();
		return modal;
	}

	public @NotNull Guppy.Message wrapPublicMessage(@NotNull Message message) {
		return new Guppy.Message() {
			@Override
			public @NotNull String getText() {
				return message.getContentRaw();
			}

			@Override
			public long getId() {
				return message.getIdLong();
			}

			@Override
			public @NotNull Channel getChannel() {
				return GuppyEntryPoint.this.newChannel(message.getChannel());
			}

			@Override
			public Channel.@NotNull Thread getThread() {
				return newThread(message.getChannel());
			}

			@Override
			public @NotNull Reaction[] getReactions() {
				return message.getReactions().stream().map(GuppyEntryPoint.this::newReaction).toArray(Reaction[]::new);
			}

			@Override
			public @NotNull EmbeddedMessage[] getAttached() {
				return message.getEmbeds().stream().map(messageEmbed -> new EmbeddedMessage() {
					@Override
					public @Nullable String getHeader() {
						return messageEmbed.getTitle();
					}

					@Override
					public @Nullable Color getColor() {
						return messageEmbed.getColor();
					}

					@Override
					public @Nullable Image getImage() {
						return () -> {
							MessageEmbed.ImageInfo info = messageEmbed.getImage();
							if (info != null) {
								String url = info.getUrl();
								if (url != null) {
									return url;
								}
							}
							return null;
						};
					}

					@Override
					public @Nullable Thumbnail getThumbnail() {
						return () -> {
							MessageEmbed.Thumbnail info = messageEmbed.getThumbnail();
							if (info != null) {
								String url = info.getUrl();
								if (url != null) {
									return url;
								}
							}
							return null;
						};
					}

					@Override
					public @Nullable String getDescription() {
						return messageEmbed.getDescription();
					}

					@Override
					public @Nullable Guppy getAuthor() {
						MessageEmbed.AuthorInfo info = messageEmbed.getAuthor();
						if (info != null) {
							String tag = info.getName();
							if (tag != null) {
								return api.getGuppy(tag.split("#")[0], false);
							}
						}
						return null;
					}

					@Override
					public @Nullable Footer getFooter() {
						return new Footer() {
							@Override
							public @Nullable String getIconUrl() {
								return messageEmbed.getFooter().getIconUrl();
							}

							@Override
							public @NotNull String getText() {
								return messageEmbed.getFooter().getText();
							}
						};
					}

					@Override
					public @NotNull Field[] getFields() {
						return messageEmbed.getFields().stream().map(field -> new Field() {
							@Override
							public @NotNull String getValue() {
								return field.getValue();
							}

							@Override
							public boolean inline() {
								return field.isInline();
							}

							@Override
							public @NotNull String getName() {
								return field.getName();
							}
						}).toArray(Field[]::new);
					}
				}).toArray(EmbeddedMessage[]::new);
			}

			@Override
			public @Nullable Reaction getReaction(@NotNull String code) {
				return Arrays.stream(getReactions()).filter(r -> r.get().equals(code)).findFirst().orElse(null);
			}

			@Override
			public void add(@NotNull Reaction reaction) {
				Object r = reaction.get();
				if (r instanceof Emoji) {
					message.addReaction((Emoji) r).queueAfter(2, TimeUnit.MILLISECONDS);
				} else if (r instanceof String) {
					message.addReaction(Emoji.fromFormatted(r.toString())).queueAfter(2, TimeUnit.MILLISECONDS);
				}
			}

			@Override
			public void take(@NotNull Reaction reaction) {
				Object r = reaction.get();
				if (r instanceof Emoji) {
					message.removeReaction((Emoji) r).queueAfter(2, TimeUnit.MILLISECONDS);
				} else if (r instanceof String) {
					message.removeReaction(Emoji.fromFormatted(r.toString())).queueAfter(2, TimeUnit.MILLISECONDS);
				}
			}

			@Override
			public void delete() {
				message.delete().queueAfter(2, TimeUnit.MILLISECONDS);
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

					@Override
					public boolean isSelfMuted() {
						return getState().isSelfMuted();
					}

					@NotNull GuildVoiceState getState() {
						GuildVoiceState state = guild.getMember(u).getVoiceState();
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
					public Channel getChannel() {
						AudioChannel channel = getState().getChannel();
						if (channel == null) return null;
						return newChannel((VoiceChannel) channel);
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
				Member m = getGuild().getMember(u);
				if (m == null) return new Role[0];
				return m.getRoles().stream().map(GuppyEntryPoint.this::newRole).toArray(Role[]::new);
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
			public boolean has(@NotNull Role... roles) {
				for (Role role : roles) {
					if (Arrays.stream(getRoles()).noneMatch(r -> r.getId() == role.getId())) {
						return false;
					}
				}
				return true;
			}

			@Override
			public void inherit(@NotNull Role... roles) {
				for (Role r : roles) {
					getGuild().addRoleToMember(u, jda.getRoleById(r.getId())).queueAfter(2, TimeUnit.MILLISECONDS);
				}
			}

			@Override
			public void revoke(@NotNull Role... roles) {
				for (Role r : roles) {
					getGuild().removeRoleFromMember(u, jda.getRoleById(r.getId())).queueAfter(2, TimeUnit.MILLISECONDS);
				}
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

	public @Nullable Channel.Thread newThread(@NotNull MessageChannel t) {
		if (!(t instanceof ThreadChannel)) return null;
		return new Channel.Thread() {
			@Override
			public boolean isOwned() {
				return t.getName().contains("THREAD") && !((ThreadChannel)t).isOwner();
			}

			@Override
			public @Nullable Guppy getOwner() {
				return api.getGuppy(((ThreadChannel)t).getOwnerIdLong());
			}

			@Override
			public @NotNull Channel getParent() {
				ThreadChannel channel = (ThreadChannel) t;
				return newChannel(channel.getParentMessageChannel());
			}

			@Override
			public void delete() {
				t.delete().queueAfter(2, TimeUnit.MILLISECONDS);
			}

			@Override
			public long getId() {
				return t.getIdLong();
			}

			@Override
			public Deployable<Guppy.Message> sendMessage(@NotNull String mess) {
				return Deployable.of(() -> wrapPrivateMessage(t.sendMessage(mess).submit().join()), 1);
			}

			@Override
			public @NotNull String getName() {
				return t.getName();
			}
		};
	}

	public @NotNull Channel newChannel(@NotNull MessageChannel t) {
		return new Channel() {

			@Override
			public Deployable<Void> open() {
				if (t instanceof AudioChannel) {
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
						PantherLogger.getInstance().getLogger().info("Connecting to voice channel " + getName() + "(" + getId() + ")");
						getGuild().getAudioManager().openAudioConnection(getJDA().getVoiceChannelById(getId()));
					}, 1);
				} else throw new IllegalStateException("The bot can only connect to audio channels!");
			}

			@Override
			public Deployable<Void> close() {
				if (t instanceof VoiceChannel) {
					return Deployable.of(() -> {
						AudioChannel ch = getGuild().getAudioManager().getConnectedChannel();
						if (ch != null) {
							PantherLogger.getInstance().getLogger().info("Leaving voice channel " + ch.getName() + "(" + ch.getIdLong() + ")");
							getGuild().getAudioManager().closeAudioConnection();
						}
					}, 1);
				} else throw new IllegalStateException("The bot can only connect to audio channels!");
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
				return ((TextChannel) t).getThreadChannels().stream().map(threadChannel -> newThread(threadChannel)).collect(PantherCollectors.toImmutableList());
			}

			@Override
			public @Nullable Guppy.Message getMessage(long id) {
				Message message = t.retrieveMessageById(id).submit().join();
				if (message != null) {
					return wrapPublicMessage(message);
				}
				return null;
			}

			@Override
			public @NotNull PantherCollection<Guppy.Message> getHistory() {
				return t.getHistory().retrievePast(100).submit().join().stream().map(message -> new Guppy.Message() {
					@Override
					public @NotNull String getText() {
						return message.getContentRaw();
					}

					@Override
					public long getId() {
						return message.getIdLong();
					}

					@Override
					public @NotNull Channel getChannel() {
						return GuppyEntryPoint.this.newChannel(message.getChannel());
					}

					@Override
					public Channel.@NotNull Thread getThread() {
						return newThread(message.getChannel());
					}

					@Override
					public @NotNull Reaction[] getReactions() {
						return message.getReactions().stream().map(GuppyEntryPoint.this::newReaction).toArray(Reaction[]::new);
					}

					@Override
					public @NotNull EmbeddedMessage[] getAttached() {
						return message.getEmbeds().stream().map(messageEmbed -> new EmbeddedMessage() {
							@Override
							public @Nullable String getHeader() {
								return messageEmbed.getTitle();
							}

							@Override
							public @Nullable Color getColor() {
								return messageEmbed.getColor();
							}

							@Override
							public @NotNull Image getImage() {
								return () -> messageEmbed.getImage().getUrl();
							}

							@Override
							public @Nullable Thumbnail getThumbnail() {
								return () -> messageEmbed.getThumbnail().getUrl();
							}

							@Override
							public @Nullable String getDescription() {
								return messageEmbed.getDescription();
							}

							@Override
							public @Nullable Guppy getAuthor() {
								return api.getGuppy(messageEmbed.getAuthor().getName().split("#")[0], false);
							}

							@Override
							public @Nullable Footer getFooter() {
								return new Footer() {
									@Override
									public @Nullable String getIconUrl() {
										return messageEmbed.getFooter().getIconUrl();
									}

									@Override
									public @NotNull String getText() {
										return messageEmbed.getFooter().getText();
									}
								};
							}

							@Override
							public @NotNull Field[] getFields() {
								return messageEmbed.getFields().stream().map(field -> new Field() {
									@Override
									public @NotNull String getValue() {
										return field.getValue();
									}

									@Override
									public boolean inline() {
										return field.isInline();
									}

									@Override
									public @NotNull String getName() {
										return field.getName();
									}
								}).toArray(Field[]::new);
							}
						}).toArray(EmbeddedMessage[]::new);
					}

					@Override
					public @Nullable Reaction getReaction(@NotNull String code) {
						return Arrays.stream(getReactions()).filter(r -> r.get().equals(code)).findFirst().orElse(null);
					}

					@Override
					public void add(@NotNull Reaction reaction) {
						Object r = reaction.get();
						if (r instanceof Emoji) {
							message.addReaction((Emoji) r).queueAfter(2, TimeUnit.MILLISECONDS);
						} else if (r instanceof String) {
							message.addReaction(Emoji.fromFormatted(r.toString())).queueAfter(2, TimeUnit.MILLISECONDS);
						}
					}

					@Override
					public void take(@NotNull Reaction reaction) {
						Object r = reaction.get();
						if (r instanceof Emoji) {
							message.removeReaction((Emoji) r).queueAfter(2, TimeUnit.MILLISECONDS);
						} else if (r instanceof String) {
							message.removeReaction(Emoji.fromFormatted(r.toString())).queueAfter(2, TimeUnit.MILLISECONDS);
						}
					}

					@Override
					public void delete() {
						message.delete().queueAfter(2, TimeUnit.MILLISECONDS);
					}
				}).collect(PantherCollectors.toList());
			}

			@Override
			public boolean isPrivate() {
				return t instanceof PrivateChannel;
			}

			@Override
			public boolean isNews() {
				return t instanceof NewsChannel;
			}

			@Override
			public boolean isVoice() {
				return t instanceof VoiceChannel;
			}

			@Override
			public void setName(@NotNull String newName) {
				if (t instanceof TextChannel) ((TextChannel) t).getManager().setName(newName).queue();
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

	public Role newRole(@NotNull net.dv8tion.jda.api.entities.Role role) {
		return new Role() {

			@Override
			public com.github.sanctum.jda.common.@NotNull Permission[] getPermissions() {
				Permission[] ar = role.getPermissions().stream().toArray(Permission[]::new);
				com.github.sanctum.jda.common.Permission[] n = new com.github.sanctum.jda.common.Permission[ar.length];
				for (int i = 0; i < ar.length; i++) {
					n[i] = com.github.sanctum.jda.common.Permission.valueOf(ar[i].name());
				}
				return n;
			}

			@Override
			public void permit(com.github.sanctum.jda.common.@NotNull Permission... permissions) {
				final RoleManager manager = role.getManager();
				for (com.github.sanctum.jda.common.Permission permission : permissions) {
					if (!role.hasPermission(Permission.valueOf(permission.name()))) {
						manager.givePermissions(Permission.valueOf(permission.name())).queue();
					}
				}
			}

			@Override
			public void revoke(com.github.sanctum.jda.common.@NotNull Permission... permissions) {
				final RoleManager manager = role.getManager();
				for (com.github.sanctum.jda.common.Permission permission : permissions) {
					if (role.hasPermission(Permission.valueOf(permission.name()))) {
						manager.revokePermissions(Permission.valueOf(permission.name())).queue();
					}
				}
			}

			@Override
			public @NotNull String getName() {
				return role.getName();
			}

			@Override
			public Deployable<Guppy.Message> sendMessage(@NotNull String message) {
				return Deployable.of(() -> {
					for (Guppy g : GuppyAPI.getInstance().getGuppies()) {
						if (g.getRole(getName()) != null) g.sendMessage(message).queue();
					}
					return null;
				}, 1);
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

	public @NotNull Guppy getGuppy(@NotNull User u) {
		return Optional.ofNullable(api.getGuppy(u.getIdLong())).orElse(newGuppy(u));
	}

	public static GuppyEntryPoint getInstance() {
		return entryPoint;
	}

	class Api implements GuppyAPI {

		boolean loaded = true;
		private final CommandController commands;
		MusicPlayer player = new MusicPlayer() {

			private final AudioPlayer player;
			private final Controller controller;
			private final Queue queue;
			private Channel channel, voice;
			private DefaultAudioListener listener;

			private SendHandler sendWrapper;
			private final AudioPlayerManager audioPlayerManager;

			{
				this.audioPlayerManager = new DefaultAudioPlayerManager();
				this.player = audioPlayerManager.createPlayer();
				this.controller = new Controller() {
					@Override
					public Track getPlaying() {
						return new Track() {
							final AudioTrack handle = player.getPlayingTrack();

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
				this.queue = new Queue();
				this.listener = new DefaultAudioListener(this);
				TaskChain.getAsynchronous().wait(() -> this.player.addListener(listener), 88 * 50);
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

			@Nullable
			@Override
			public Channel LastAlerted() {
				return channel;
			}

			@Nullable
			@Override
			public Channel LastSummoned() {
				return voice;
			}

			@Override
			public void setSendHandler(@NotNull SendHandler handler) {
				this.sendWrapper = handler;
			}

			@Override
			public void setListener(@NotNull Listener listener) {
				this.player.removeListener(this.listener);
				this.player.addListener(this.listener = new DefaultAudioListener(listener));
			}

			@Override
			public void resetListener() {
				this.listener = new DefaultAudioListener(this);
			}

			@Override
			public void stop() {
				player.destroy();
			}

			Track transform(AudioTrack track) {
				return new Track() {
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
				};
			}

			@Override
			public void load(@NotNull Channel channel, @NotNull String url) {
				this.channel = channel;
				audioPlayerManager.loadItemOrdered(this, url, new AudioLoadResultHandler() {
					@Override
					public void trackLoaded(AudioTrack track) {
						Track n = transform(track);
						if (!controller.start(n, false)) {
							queue.add(n);
						}
						ParsedTimeFormat timeFormat = ParsedTimeFormat.of(TimeUnit.MILLISECONDS.toSeconds(track.getInfo().length));
						channel.sendMessage("Queued song **" + track.getInfo().title + "** by **" + track.getInfo().author + "** with duration **" + timeFormat.getHours() + ":" + timeFormat.getMinutes() + ":" + timeFormat.getSeconds() + "**").queue();
					}

					@Override
					public void playlistLoaded(AudioPlaylist playlist) {
						List<AudioTrack> tracks = playlist.getTracks();
						if (!tracks.isEmpty()) {
							// add possible filter to attempt hd vids first
							AudioTrack track = tracks.get(0);
							Track n = transform(track);
							if (!controller.start(n, false)) {
								queue.add(n);
							}
							ParsedTimeFormat timeFormat = ParsedTimeFormat.of(TimeUnit.MILLISECONDS.toSeconds(track.getInfo().length));
							channel.sendMessage("Queued song **" + track.getInfo().title + "** by **" + track.getInfo().author + "** with duration **" + timeFormat.getHours() + ":" + timeFormat.getMinutes() + ":" + timeFormat.getSeconds() + "**").queue();
						}
					}

					@Override
					public void noMatches() {
						channel.sendMessage("Sorry unable to play track " + url).queue();
					}

					@Override
					public void loadFailed(FriendlyException exception) {
						exception.printStackTrace();
						channel.sendMessage("Sorry unable to play track " + url).queue();
					}
				});
			}

			@Override
			public void load(@NotNull Guppy guppy, @NotNull Channel channel, @NotNull String url) {
				this.channel = channel;
				this.voice = guppy.getVoice().getChannel();
				audioPlayerManager.loadItemOrdered(this, url, new AudioLoadResultHandler() {
					@Override
					public void trackLoaded(AudioTrack track) {
						Track n = transform(track);
						if (!controller.start(n, false)) {
							queue.add(n);
						}
						ParsedTimeFormat timeFormat = ParsedTimeFormat.of(TimeUnit.MILLISECONDS.toSeconds(track.getInfo().length));
						channel.sendMessage(guppy.getAsMention() + " queued song **" + track.getInfo().title + "** by **" + track.getInfo().author + "** with duration **" + timeFormat.getHours() + ":" + timeFormat.getMinutes() + ":" + timeFormat.getSeconds() + "**").queue();
					}

					@Override
					public void playlistLoaded(AudioPlaylist playlist) {
						List<AudioTrack> tracks = playlist.getTracks();
						if (!tracks.isEmpty()) {
							// add possible filter to attempt hd vids first
							AudioTrack track = tracks.get(0);
							Track n = transform(track);
							if (!controller.start(n, false)) {
								queue.add(n);
							}
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

		{
			this.commands = new CommandController() {
				@Override
				public @Nullable Command get(@NotNull String label) {
					return getAll().stream().filter(c -> c.getLabel().equalsIgnoreCase(label)).findFirst().orElse(null);
				}

				@Override
				public @Nullable Command getContext(@NotNull String message) {
					return getContext().stream().filter(c -> c.getMessage().equalsIgnoreCase(message)).findFirst().orElse(null);
				}

				@Override
				public @NotNull PantherCollection<Command> getAll() {
					return commandSupplier;
				}

				@Override
				public @NotNull PantherCollection<Command> getContext() {
					return getAll().stream().filter(c -> c.getType() == Command.Type.MULTI_MESSAGE || c.getType() == Command.Type.MULTI_USER).collect(PantherCollectors.toList());
				}

				@Override
				public @NotNull Deployable<Void> refresh() {
					return Deployable.of(() -> {
						PantherCollection<CommandData> data = new PantherList<>();
						for (Command c : getAll()) {
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
						for (Command action : getContext()) {
							data.add(Commands.context(OptionTypeConverter.get(action.getType()), action.getName()));
							data.add(Commands.message(action.getMessage()));
						}
						jda.updateCommands().addCommands(data.stream().toArray(CommandData[]::new)).queue();
						return null;
					}, 1);
				}

				@Override
				public @NotNull Deployable<Void> add(@NotNull Command command) {
					return Deployable.of(() -> {
						commandSupplier.add(command);
					}, 1);
				}

				@Override
				public @NotNull Deployable<Void> remove(@NotNull Command command) {
					return Deployable.of(() -> {
						commandSupplier.remove(command);
					}, 1);
				}
			};
		}

		@Override
		public @NotNull Guppy newGuppy(@NotNull Object user) {
			if (user instanceof User) {
				return GuppyEntryPoint.this.getGuppy((User) user);
			}
			return new Guppy() {
				@Override
				public @NotNull String getTag() {
					return null;
				}

				@Override
				public @NotNull String getAsMention() {
					return null;
				}

				@Override
				public @NotNull String getAvatarUrl() {
					return null;
				}

				@Override
				public @Nullable Link getLink() {
					return null;
				}

				@Override
				public @NotNull Voice getVoice() {
					return null;
				}

				@Override
				public @NotNull Role[] getRoles() {
					return new Role[0];
				}

				@Override
				public @Nullable Role getRole(@NotNull String name) {
					return null;
				}

				@Override
				public @Nullable Role getRole(long id) {
					return null;
				}

				@Override
				public boolean has(@NotNull Role... roles) {
					return false;
				}

				@Override
				public void inherit(@NotNull Role... roles) {

				}

				@Override
				public void revoke(@NotNull Role... roles) {

				}

				@Override
				public Deployable<Void> setLink(@NotNull Link link) {
					return null;
				}

				@Override
				public long getId() {
					return 0;
				}

				@Override
				public Deployable<Message> sendMessage(@NotNull String message) {
					return null;
				}

				@Override
				public @NotNull String getName() {
					return null;
				}
			};
		}

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
		public @NotNull Channel newChannel(@NotNull Object channel) {
			if (channel instanceof AudioChannel) {
				return GuppyEntryPoint.this.newChannel((VoiceChannel) channel);
			}
			if (channel instanceof MessageChannel) {
				return GuppyEntryPoint.this.newChannel((MessageChannel) channel);
			}
			return new Channel() {
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
					return null;
				}

				@Override
				public @Nullable Guppy.Message getMessage(long id) {
					return null;
				}

				@Override
				public @NotNull PantherCollection<Guppy.Message> getHistory() {
					return null;
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
					return false;
				}

				@Override
				public void setName(@NotNull String newName) {

				}

				@Override
				public void delete() {

				}

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
					return 0;
				}

				@Override
				public Deployable<Guppy.Message> sendMessage(@NotNull String message) {
					return null;
				}

				@Override
				public @NotNull String getName() {
					return null;
				}
			};
		}

		@Override
		public @NotNull Channel newChannel(@NotNull String name, long categoryId, Role.Attachment... attachments) {
			Channel test = getChannel(name);
			if (test != null) return test;
			Guild guild = getGuild();
			return initialize(guild, name, categoryId, attachments);
		}

		public Channel initialize(Guild guild, String name, long categoryId, Role.Attachment... roles) {
			ChannelAction<TextChannel> action;
			if (categoryId > 0) {
				action = guild.createTextChannel(name, getJDA().getCategoryById(categoryId));

			} else {
				action = guild.createTextChannel(name);
			}
			for (Role.Attachment r : roles) {
				List<Permission> permissions = new ArrayList<>();
				for (com.github.sanctum.jda.common.Permission p : r.getPermissions()) {
					permissions.add(Permission.valueOf(p.name()));
				}
				action.addPermissionOverride(getJDA().getRoleById(r.getRole().getId()), permissions, null);
			}
			return GuppyEntryPoint.this.newChannel(action
					.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
					.submit().join());
		}

		public Channel memberInitialize(Guild guild, User user, String name, long categoryId, Role.Attachment... roles) {
			Member member = guild.getMember(user);
			ChannelAction<TextChannel> action;
			if (categoryId > 0) {
				action = guild.createTextChannel(name, getJDA().getCategoryById(categoryId));

			} else {
				action = guild.createTextChannel(name);
			}
			for (Role.Attachment r : roles) {
				List<Permission> permissions = new ArrayList<>();
				for (com.github.sanctum.jda.common.Permission p : r.getPermissions()) {
					permissions.add(Permission.valueOf(p.name()));
				}
				action.addPermissionOverride(getJDA().getRoleById(r.getRole().getId()), permissions, null);
			}
			return GuppyEntryPoint.this.newChannel(action
					.addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
					.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
					.submit().join());
		}

		public Channel membersInitialize(Guild guild, User[] user, String name, long categoryId, Role.Attachment... roles) {
			Member[] members = new Member[user.length];
			for (int i = 0; i < user.length; i++) {
				members[i] = guild.getMember(user[i]);
			}
			ChannelAction<TextChannel> action;
			if (categoryId > 0) {
				action = guild.createTextChannel(name, getJDA().getCategoryById(categoryId));

			} else {
				action = guild.createTextChannel(name);
			}
			for (Role.Attachment r : roles) {
				List<Permission> permissions = new ArrayList<>();
				for (com.github.sanctum.jda.common.Permission p : r.getPermissions()) {
					permissions.add(Permission.valueOf(p.name()));
				}
				action.addPermissionOverride(getJDA().getRoleById(r.getRole().getId()), permissions, null);
			}
			for (Member m : members) {
				action.addPermissionOverride(m, EnumSet.of(Permission.VIEW_CHANNEL), null);
			}
			return GuppyEntryPoint.this.newChannel(action
					.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
					.submit().join());
		}

		@Override
		public @NotNull Channel newChannel(@NotNull String name, long categoryId, @NotNull Guppy guppy, Role.Attachment... attachments) {
			Channel test = getChannel(name);
			if (test != null) return test;
			User match = getJDA().getUserById(guppy.getId());
			Guild guild = getGuild();
			return memberInitialize(guild, match, name, categoryId, attachments);
		}

		@Override
		public @NotNull Channel newChannel(@NotNull String name, long categoryId, @NotNull Guppy[] guppies, Role.Attachment... attachments) {
			Channel test = getChannel(name);
			if (test != null) return test;
			User[] users = new User[guppies.length];
			for (int i = 0; i < guppies.length; i++) {
				users[i] = getJDA().getUserById(guppies[i].getId());
			}
			Guild guild = getGuild();
			return membersInitialize(guild, users, name, categoryId, attachments);
		}

		@Override
		public @NotNull PantherCollection<Channel> getChannels() {
			PantherCollection<Channel> channels = jda.getTextChannels().stream().map(GuppyEntryPoint.this::newChannel).collect(PantherCollectors.toList());
			channels.addAll(jda.getNewsChannels().stream().map(GuppyEntryPoint.this::newChannel).collect(Collectors.toList()));
			channels.addAll(jda.getVoiceChannels().stream().map(GuppyEntryPoint.this::newChannel).collect(Collectors.toList()));
			return channels;
		}

		@Override
		public @NotNull CommandController getCommands() {
			return commands;
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
			return player;
		}

		@Override
		public @NotNull GuppyAPI setPlayer(@NotNull MusicPlayer player) {
			this.player = player;
			return this;
		}

	}

	public static PantherCollection<ConsoleCommand> getConsoleCommands() {
		return consoleCommands.values();
	}

	@Comment("This method handles application stuff.")
	public static void main(String[] args) {

		System.out.println("Select a boot option:");
		System.out.println(" ");
		System.out.println("1. Start");
		System.out.println("2. Setup");
		System.out.println("3. Exit");
		new Thread(() -> {
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			String line;
			final Configurable configurable = new JsonConfiguration(new File("common"), "config", null) {
			};

			try {
				while ((line = input.readLine()) != null) {
					Activity activity = Activity.watching("Aqua Teen Hunger Force");
					switch (line) {
						case "1":
							if (getInstance() != null && getInstance().active) return;
							String botmsg = configurable.getString("activity.msg");
							switch (configurable.getString("activity.id")) {
								case "1":
									activity = Activity.watching(botmsg);
									break;
								case "2":
									activity = Activity.playing(botmsg);
									break;
								case "3":
									activity = Activity.listening(botmsg);
									break;
								case "4":
									activity = Activity.competing(botmsg);
									break;
							}
							enable(configurable.getNode("token").toPrimitive().getString(), activity);
							break;
						case "2":
							System.out.println("Beginning setup...");
							Console c = System.console();
							c.flush();
							System.out.println("Enter your bot token.");
							String botToken = c.readLine();
							if (botToken != null) {
								JDAInput jdaInput = new JDAInput();
								configurable.getNode("token").set(botToken);
								configurable.save();
								System.out.println("Set bot token to \"" + botToken + "\"");
								System.out.println(" ");
								System.out.println("Enter your discord server id.");
								String guildId = c.readLine();
								if (guildId != null) {
									configurable.getNode("guild").set(Long.parseLong(guildId));
									configurable.save();
									System.out.println(" ");
									System.out.println("Select a bot activity.");
									System.out.println(" ");
									System.out.println("1. Watching");
									System.out.println("2. Playing");
									System.out.println("3. Listening");
									System.out.println("4. Competing");
									String botActivity = c.readLine();
									if (botActivity != null) {
										jdaInput.setActivity(botActivity);
										System.out.println(" ");
										System.out.println("Type a status message...");
										String botMsg = c.readLine();
										if (botMsg != null) {
											switch (jdaInput.getActivity()) {
												case "1":
													activity = Activity.watching(botMsg);
													break;
												case "2":
													activity = Activity.playing(botMsg);
													break;
												case "3":
													activity = Activity.listening(botMsg);
													break;
												case "4":
													activity = Activity.competing(botMsg);
													break;
											}
											Node ac = configurable.getNode("activity");
											ac.getNode("id").set(botActivity);
											ac.getNode("msg").set(botMsg);
											configurable.save();
											System.out.println("Setup complete.");
											c.flush();
											if (activity != null) {
												enable(botToken, activity);
											} else
												getConsoleCommands().stream().filter(cm -> cm.getLabel().equals("stop")).findFirst().ifPresent(cm -> cm.execute(new String[0]));
										}
									}
								}
							}
							break;
						case "3":
							System.out.println("Exit code 3 used, shutting down...");
							System.exit(0);
						default:
							String label = line.replace("/", "").split(" ")[0];
							ConsoleCommand command = getConsoleCommands().stream().filter(cmd -> cmd.getLabel().equalsIgnoreCase(label) || cmd.getAliases().stream().anyMatch(label::equalsIgnoreCase)).findFirst().orElse(null);
							if (command != null) {
								command.execute(Arrays.stream(line.replace("/", "").split(" ")).filter(s -> !s.equalsIgnoreCase(command.getLabel()) && command.getAliases().stream().noneMatch(s::equalsIgnoreCase)).toArray(String[]::new));
							}
							break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	static void enable(@NotNull String token, @NotNull Activity activity) {
		Logger n = Logger.getLogger(Guppy.class.getSimpleName());
		GuppyLoggerFormat formatter = new GuppyLoggerFormat();
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(formatter);
		for (Handler iHandler : n.getParent().getHandlers()) {
			n.getParent().removeHandler(iHandler);
		}
		n.addHandler(consoleHandler);
		PantherLogger.getInstance().setLogger(n);
		Logger logger = PantherLogger.getInstance().getLogger();
		logger.info("---------------------------");
		logger.info("Loading properties...");
		logger.info("---------------------------");
		entryPoint = new GuppyEntryPoint(PantherLogger.getInstance().getLogger());
		try {
			getInstance().enable(new DockingAgent().consume(new DockingAgent.Procedure() {
				@Override
				public void onConstruct(@NotNull JDABuilder builder) {
					builder.setToken(token);
					builder.enableIntents(Arrays.asList(GatewayIntent.values()));
					builder.setActivity(activity);
					builder.addEventListeners(new JDAListenerAdapter());
					builder.enableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.VOICE_STATE, CacheFlag.ONLINE_STATUS);

					builder.setChunkingFilter(ChunkingFilter.ALL);
					builder.setMemberCachePolicy(MemberCachePolicy.ALL);
					builder.setLargeThreshold(300);
				}

				@Override
				public void onFinalize(@NotNull JDA instance) {
					PantherCollection<SlashCommandData> data = new PantherList<>();
					for (Command c : GuppyAPI.getInstance().getCommands().getAll()) {
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
			logger.info("Bot Loaded.");
			logger.info("---------------------------");
		} catch (Exception e) {
			logger.info("Bot loading failure, reason: " + e.getMessage());
			logger.info("---------------------------");
		}
		logger.info("Say " + '"' + "stop" + '"' + " or " + '"' + "exit" + '"' + " to close this application.");
		logger.info("---------------------------");
		File addonFolder = DiscordExtensionManager.getInstance().getAddonFolder();
		if (!addonFolder.exists()) {
			addonFolder.mkdirs();
		}
	}

}
