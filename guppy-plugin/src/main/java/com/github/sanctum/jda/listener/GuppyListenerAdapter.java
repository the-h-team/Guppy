package com.github.sanctum.jda.listener;

import com.github.sanctum.jda.GuppyAPI;
import com.github.sanctum.jda.GuppyEntryPoint;
import com.github.sanctum.jda.common.Channel;
import com.github.sanctum.jda.common.Command;
import com.github.sanctum.jda.common.EmbeddedMessage;
import com.github.sanctum.jda.common.EphemeralResponse;
import com.github.sanctum.jda.common.Guppy;
import com.github.sanctum.jda.common.Reaction;
import com.github.sanctum.jda.event.BotMessageReceivedEvent;
import com.github.sanctum.jda.event.GuppyMessageReactEvent;
import com.github.sanctum.jda.event.GuppyMessageSentEvent;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.util.Deployable;
import com.github.sanctum.panther.util.PantherString;
import com.github.sanctum.panther.util.SimpleAsynchronousTask;
import com.github.sanctum.panther.util.TypeAdapter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GuppyListenerAdapter extends ListenerAdapter {

	final GuppyEntryPoint entryPoint = GuppyEntryPoint.getInstance();
	final GuppyAPI api = GuppyAPI.getInstance();

	boolean isFromTypes(ChannelType type, ChannelType... types) {
		return Arrays.asList(types).contains(type);
	}

	@Override
	public void onMessageReactionAdd(@NotNull MessageReactionAddEvent e) {
		final Message message = e.retrieveMessage().submit().join();
		// This part is responsible for handling what happens when a user messages the bot.
		if (e.getUser() != null && !e.getUser().isBot()) {
			final Guppy guppy = entryPoint.getGuppy(e.getUser());
			Guppy.Message m = new Guppy.Message() {
				@Override
				public @NotNull String getText() {
					return message.getContentDisplay();
				}

				@Override
				public @NotNull Channel getChannel() {
					return new Channel() {
						@Override
						public long getId() {
							return e.getPrivateChannel().getIdLong();
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
						public void delete() {
							e.getPrivateChannel().delete().queueAfter(2, TimeUnit.MILLISECONDS);
						}

						@Override
						public @NotNull String getName() {
							return e.getPrivateChannel().getName();
						}

						@Override
						public Deployable<Guppy.Message> sendMessage(@NotNull String message) {
							return GuppyEntryPoint.newDeployable(() -> {
								e.getPrivateChannel().sendMessage(message).queue();
								return null;
							});
						}

						@Override
						public Deployable<EmbeddedMessage> sendEmbeddedMessage(@NotNull EmbeddedMessage m) {
							return GuppyEntryPoint.newDeployable(() -> {
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
							});
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
					} else if (r instanceof com.github.sanctum.jda.common.Emoji){
						message.addReaction(Emoji.fromFormatted(((com.github.sanctum.jda.common.Emoji) r).getFormat())).queue();
					}
				}

				@Override
				public void take(@NotNull Reaction reaction) {
					Object r = reaction.get();
					if (r instanceof String) {
						message.removeReaction(Emoji.fromFormatted((String) r)).queue();
					} else if (r instanceof com.github.sanctum.jda.common.Emoji){
						message.removeReaction(Emoji.fromFormatted(((com.github.sanctum.jda.common.Emoji) r).getFormat())).queue();
					}
				}

				@Override
				public void delete() {
					SimpleAsynchronousTask.runLater(() -> message.delete().queue(), 4);
				}
			};
			GuppyMessageReactEvent ev = new GuppyVentCall<>(new GuppyMessageReactEvent(guppy, m, e.getEmoji().getFormatted(), GuppyMessageReactEvent.ReactionResult.ADD)).schedule().join();
			if (ev.isCancelled()) {
				message.getReaction(e.getEmoji()).removeReaction(e.getUser()).queue();
			}
		}
	}

	@Override
	public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent e) {
		final Message message = e.retrieveMessage().submit().join();
		// This part is responsible for handling what happens when a user messages the bot.
		if (e.getUser() != null && !e.getUser().isBot()) {
			final Guppy guppy = entryPoint.getGuppy(e.getUser());
			Guppy.Message m = new Guppy.Message() {
				@Override
				public @NotNull String getText() {
					return message.getContentDisplay();
				}

				@Override
				public @NotNull Channel getChannel() {
					return new Channel() {
						@Override
						public long getId() {
							return e.getPrivateChannel().getIdLong();
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
						public void delete() {
							e.getPrivateChannel().delete().queueAfter(2, TimeUnit.MILLISECONDS);
						}

						@Override
						public @NotNull String getName() {
							return e.getPrivateChannel().getName();
						}

						@Override
						public Deployable<Guppy.Message> sendMessage(@NotNull String message) {
							return GuppyEntryPoint.newDeployable(() -> {
								e.getPrivateChannel().sendMessage(message).queue();
								return null;
							});
						}

						@Override
						public Deployable<EmbeddedMessage> sendEmbeddedMessage(@NotNull EmbeddedMessage m) {
							return GuppyEntryPoint.newDeployable(() -> {
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
							});
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
					} else if (r instanceof com.github.sanctum.jda.common.Emoji){
						message.addReaction(Emoji.fromFormatted(((com.github.sanctum.jda.common.Emoji) r).getFormat())).queue();
					}
				}

				@Override
				public void take(@NotNull Reaction reaction) {
					Object r = reaction.get();
					if (r instanceof String) {
						message.removeReaction(Emoji.fromFormatted((String) r)).queue();
					} else if (r instanceof com.github.sanctum.jda.common.Emoji){
						message.removeReaction(Emoji.fromFormatted(((com.github.sanctum.jda.common.Emoji) r).getFormat())).queue();
					}
				}

				@Override
				public void delete() {
					SimpleAsynchronousTask.runLater(() -> message.delete().queue(), 4);
				}
			};
			GuppyMessageReactEvent ev = new GuppyVentCall<>(new GuppyMessageReactEvent(guppy, m, e.getEmoji().getFormatted(), GuppyMessageReactEvent.ReactionResult.REMOVE)).schedule().join();
			if (ev.isCancelled()) message.getReaction(e.getEmoji()).removeReaction(e.getUser()).queueAfter(2, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent e) {
		final Message message = e.getMessage();
		// This part is responsible for handling what happens when a user messages the bot.
		final Guppy guppy = entryPoint.getGuppy(e.getAuthor());
		if (e.isFromType(ChannelType.PRIVATE)) {
			if (!e.getAuthor().isBot()) {
				Guppy.Message m = new Guppy.Message() {
					@Override
					public @NotNull String getText() {
						return message.getContentDisplay();
					}

					@Override
					public @NotNull Channel getChannel() {
						return new Channel() {
							@Override
							public long getId() {
								return e.getPrivateChannel().getIdLong();
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
							public void delete() {
								e.getPrivateChannel().delete().queueAfter(2, TimeUnit.MILLISECONDS);
							}

							@Override
							public @NotNull String getName() {
								return e.getPrivateChannel().getName();
							}

							@Override
							public Deployable<Guppy.Message> sendMessage(@NotNull String message) {
								return GuppyEntryPoint.newDeployable(() -> {
									e.getPrivateChannel().sendMessage(message).queue();
									return null;
								});
							}

							@Override
							public Deployable<EmbeddedMessage> sendEmbeddedMessage(@NotNull EmbeddedMessage m) {
								return GuppyEntryPoint.newDeployable(() -> {
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
								});
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
						} else if (r instanceof com.github.sanctum.jda.common.Emoji){
							message.addReaction(Emoji.fromFormatted(((com.github.sanctum.jda.common.Emoji) r).getFormat())).queue();
						}
					}

					@Override
					public void take(@NotNull Reaction reaction) {
						Object r = reaction.get();
						if (r instanceof String) {
							message.removeReaction(Emoji.fromFormatted((String) r)).queue();
						} else if (r instanceof com.github.sanctum.jda.common.Emoji){
							message.removeReaction(Emoji.fromFormatted(((com.github.sanctum.jda.common.Emoji) r).getFormat())).queue();
						}
					}

					@Override
					public void delete() {
						SimpleAsynchronousTask.runLater(() -> message.delete().queue(), 4);
					}
				};
				new GuppyVentCall<>(new BotMessageReceivedEvent(guppy, m)).schedule().join();
			}
		}
		if (isFromTypes(e.getChannelType(), ChannelType.TEXT, ChannelType.NEWS, ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_PRIVATE_THREAD, ChannelType.GUILD_NEWS_THREAD)) {
			Channel.Thread thread = null;
			if (new PantherString(e.getChannelType().name()).contains("thread")) {
				thread = api.getChannels().stream().filter(c -> c.getThread(e.getThreadChannel().getIdLong()) != null).map(c -> c.getThread(e.getThreadChannel().getIdLong())).findFirst().orElse(null);
			}
			Channel.Thread finalThread = thread;
			Guppy.Message m = new Guppy.Message() {
				@Override
				public @NotNull String getText() {
					return message.getContentDisplay();
				}

				@Override
				public @Nullable Channel getChannel() {
					return api.getChannel(e.getChannel().getIdLong());
				}

				@Override
				public @Nullable Channel.Thread getThread() {
					return finalThread;
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
					} else if (r instanceof com.github.sanctum.jda.common.Emoji){
						message.addReaction(Emoji.fromFormatted(((com.github.sanctum.jda.common.Emoji) r).getFormat())).queue();
					}
				}

				@Override
				public void take(@NotNull Reaction reaction) {
					Object r = reaction.get();
					if (r instanceof String) {
						message.removeReaction(Emoji.fromFormatted((String) r)).queue();
					} else if (r instanceof com.github.sanctum.jda.common.Emoji){
						message.removeReaction(Emoji.fromFormatted(((com.github.sanctum.jda.common.Emoji) r).getFormat())).queue();
					}
				}

				@Override
				public void delete() {
					SimpleAsynchronousTask.runLater(() -> message.delete().queue(), 4);
				}
			};
			new GuppyVentCall<>(new GuppyMessageSentEvent(guppy, m, false)).schedule().join();
		}
	}

	@Override
	public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent e) {
		if (e.getOldOnlineStatus() == OnlineStatus.UNKNOWN || e.getOldOnlineStatus() == OnlineStatus.OFFLINE || e.getOldOnlineStatus() == OnlineStatus.INVISIBLE) {
			entryPoint.getGuppy(e.getUser());
		}
	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
		Command test = api.getCommand(e.getName());
		if (test != null) {
			Command.Options options = test.getOptions();
			if (options.get().size() > 0) {
				final Map<Class<?>, Object> classMap = new HashMap<>();
				for (Command.Option o : options.get()) {
					OptionMapping mapping = e.getOption(o.getName());
					if (mapping != null) {
						switch (o.getType()) {
							case USER:
								User u = mapping.getAsUser();
								classMap.put(Guppy.class, entryPoint.getGuppy(u));
								break;
							case INTEGER:
								int i = mapping.getAsInt();
								classMap.put(Integer.class, i);
								break;
							case NUMBER:
								Number n = mapping.getAsDouble();
								classMap.put(Number.class, n);
								break;
							case ROLE:
								Role r = mapping.getAsRole();
								classMap.put(com.github.sanctum.jda.common.Role.class, api.getRole(r.getIdLong()));
								break;
							case STRING:
								String s = mapping.getAsString();
								classMap.put(String.class, s);
								break;
						}
					}
				}
				Command.Variable variable = new Command.Variable() {
					@Override
					public <T> @NotNull T get(@NotNull TypeAdapter<T> typeFlag) {
						return (T) classMap.get(typeFlag.getType());
					}

					@Override
					public <T> @NotNull T get(@NotNull Class<T> typeFlag) {
						return (T) classMap.get(typeFlag);
					}

					@Override
					public <T> boolean contains(@NotNull TypeAdapter<T> typeFlag) {
						return classMap.containsKey(typeFlag.getType());
					}

					@Override
					public <T> boolean contains(@NotNull Class<T> typeFlag) {
						return classMap.containsKey(typeFlag);
					}

					@Override
					public boolean isEmpty() {
						return classMap.isEmpty();
					}
				};
				EphemeralResponse response = test.onExecuted(entryPoint.getGuppy(e.getUser()), variable);
				if (response.isNegated()) {
					e.deferReply().queue();
					if (response.getExtra() != null) {
						EmbeddedMessage m = response.getExtra();
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
						e.getHook().sendMessageEmbeds(builder.build()).queue();
					} else {
						e.getHook().sendMessage(response.get().isEmpty() ? "***No info to display.***" : response.get()).queue();
					}
				} else {
					e.reply(response.get()).setEphemeral(true).queue();
				}
			} else {
				EphemeralResponse response = test.onExecuted(entryPoint.getGuppy(e.getUser()), new Command.Variable() {
					@Override
					public <T> @NotNull T get(@NotNull TypeAdapter<T> typeFlag) {
						return null;
					}

					@Override
					public <T> @NotNull T get(@NotNull Class<T> typeFlag) {
						return null;
					}

					@Override
					public <T> boolean contains(@NotNull TypeAdapter<T> typeFlag) {
						return false;
					}

					@Override
					public <T> boolean contains(@NotNull Class<T> typeFlag) {
						return false;
					}

					@Override
					public boolean isEmpty() {
						return true;
					}
				});
				if (response.isNegated()) {
					e.deferReply().queue();
					if (response.getExtra() != null) {
						EmbeddedMessage m = response.getExtra();
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
						e.getHook().sendMessageEmbeds(builder.build()).queue();
					} else {
						e.getHook().sendMessage(response.get()).queue();
					}
				} else {
					e.reply(response.get()).setEphemeral(true).queue();
				}
			}
		}
	}
}
