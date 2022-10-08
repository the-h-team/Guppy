package com.github.sanctum.jda.listener;

import com.github.sanctum.jda.GuppyAPI;
import com.github.sanctum.jda.GuppyEntryPoint;
import com.github.sanctum.jda.common.Channel;
import com.github.sanctum.jda.common.Command;
import com.github.sanctum.jda.common.Dialogue;
import com.github.sanctum.jda.common.EmbeddedMessage;
import com.github.sanctum.jda.common.EphemeralResponse;
import com.github.sanctum.jda.common.Guppy;
import com.github.sanctum.jda.common.util.GuppyVentCall;
import com.github.sanctum.jda.event.BotMessageReceivedEvent;
import com.github.sanctum.jda.event.GuppyContextActionEvent;
import com.github.sanctum.jda.event.GuppyDialogueInteractEvent;
import com.github.sanctum.jda.event.GuppyMessageContextActionEvent;
import com.github.sanctum.jda.event.GuppyMessageReactEvent;
import com.github.sanctum.jda.event.GuppyMessageSentEvent;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.util.TypeAdapter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JDAListenerAdapter extends ListenerAdapter {

	final GuppyEntryPoint entryPoint = GuppyEntryPoint.getInstance();
	final GuppyAPI api = GuppyAPI.getInstance();

	boolean isFromTypes(ChannelType type, ChannelType... types) {
		return Arrays.asList(types).contains(type);
	}

	@Override
	public void onUserContextInteraction(UserContextInteractionEvent event) {
		Member m = event.getMember();
		if (m != null) {
			Guppy match = api.getGuppy(m.getIdLong());
			if (match != null) {
				Command action = api.getCommands().getContext(event.getName());
				if (action != null) {
					if (event.getChannelType() != ChannelType.PRIVATE) {
						GuppyContextActionEvent e = new GuppyVentCall<>(new GuppyContextActionEvent(match, api.getGuppy(event.getTarget().getIdLong()), action.onContext(match, new Command.ContextVariable() {
							@Override
							public Guppy.Message getAsMessage() {
								return null;
							}

							@Override
							public Guppy getAsGuppy() {
								return api.getGuppy(event.getTarget().getIdLong());
							}
						}), event.getName())).run();
						EphemeralResponse response = e.getResponse();
						if (response.getExtra() != null) {
							EmbeddedMessage extra = response.getExtra();
							EmbedBuilder builder = new EmbedBuilder();
							if (extra.getAuthor() != null) {
								builder.setAuthor(extra.getAuthor().getTag(), extra.getAuthor().getAvatarUrl(), extra.getAuthor().getAvatarUrl());
							}
							if (extra.getHeader() != null) builder.setTitle(extra.getHeader());
							if (extra.getFooter() != null) {
								if (extra.getFooter().getIconUrl() != null) {
									builder.setFooter(extra.getFooter().getText(), extra.getFooter().getIconUrl());
								} else {
									builder.setFooter(extra.getFooter().getText());
								}
							}
							if (extra.getColor() != null) builder.setColor(extra.getColor());
							if (extra.getThumbnail() != null) {
								builder.setThumbnail(extra.getThumbnail().getUrl());
							}
							if (extra.getDescription() != null) builder.setDescription(extra.getDescription());
							if (extra.getImage() != null) {
								builder.setImage(extra.getImage().getUrl());
							}
							for (EmbeddedMessage.Field f : extra.getFields()) {
								builder.addField(new MessageEmbed.Field(f.getName(), f.getValue(), f.inline()));
							}
							event.getHook().sendMessageEmbeds(builder.build()).queue();
						} else {
							if (response.isNegated()) {
								event.reply(response.get()).setEphemeral(false).queue();
							} else {
								event.reply(response.get()).setEphemeral(true).queue();
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onMessageContextInteraction(MessageContextInteractionEvent event) {
		Member m = event.getMember();
		if (m != null) {
			Guppy match = api.getGuppy(m.getIdLong());
			if (match != null) {
				Command action = api.getCommands().getContext(event.getName());
				if (action != null) {
					Guppy.Message t;
					if (event.getChannelType() == ChannelType.PRIVATE) {
						t = entryPoint.wrapPrivateMessage(event.getTarget());
					} else {
						t = entryPoint.wrapPublicMessage(event.getTarget());
					}
					GuppyMessageContextActionEvent e = new GuppyVentCall<>(new GuppyMessageContextActionEvent(match, t, action.onContext(match, new Command.ContextVariable() {
						@Override
						public Guppy.Message getAsMessage() {
							return t;
						}

						@Override
						public Guppy getAsGuppy() {
							return null;
						}
					}), event.getName())).run();
					EphemeralResponse response = e.getResponse();
					if (response.getExtra() != null) {
						EmbeddedMessage extra = response.getExtra();
						EmbedBuilder builder = new EmbedBuilder();
						if (extra.getAuthor() != null) {
							builder.setAuthor(extra.getAuthor().getTag(), extra.getAuthor().getAvatarUrl(), extra.getAuthor().getAvatarUrl());
						}
						if (extra.getHeader() != null) builder.setTitle(extra.getHeader());
						if (extra.getFooter() != null) {
							if (extra.getFooter().getIconUrl() != null) {
								builder.setFooter(extra.getFooter().getText(), extra.getFooter().getIconUrl());
							} else {
								builder.setFooter(extra.getFooter().getText());
							}
						}
						if (extra.getColor() != null) builder.setColor(extra.getColor());
						if (extra.getThumbnail() != null) {
							builder.setThumbnail(extra.getThumbnail().getUrl());
						}
						if (extra.getDescription() != null) builder.setDescription(extra.getDescription());
						if (extra.getImage() != null) {
							builder.setImage(extra.getImage().getUrl());
						}
						for (EmbeddedMessage.Field f : extra.getFields()) {
							builder.addField(new MessageEmbed.Field(f.getName(), f.getValue(), f.inline()));
						}
						event.getHook().sendMessageEmbeds(builder.build()).queue();
					} else {
						if (response.isNegated()) {
							event.reply(response.get()).setEphemeral(false).queue();
						} else {
							event.reply(response.get()).setEphemeral(true).queue();
						}
					}
				}
			}
		}
	}

	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		Member m = event.getMember();
		if (m != null) {
			Guppy match = api.getGuppy(m.getIdLong());
			if (match != null) {
				Modal modal = entryPoint.modals.get(match);
				if (modal != null) {
					Dialogue d = new Dialogue() {
						@Override
						public @NotNull String getId() {
							return event.getModalId();
						}

						@Override
						public @NotNull String getTitle() {
							return modal.getTitle();
						}

						@Override
						public @NotNull PantherCollection<Row> getRows() {
							return new PantherList<>();
						}

						@Override
						public @Nullable String getData(@NotNull String rowId) {
							ModalMapping mapping = event.getValue(rowId);
							if (mapping != null) {
								return mapping.getAsString();
							}
							return null;
						}
					};
					GuppyDialogueInteractEvent e = new GuppyVentCall<>(new GuppyDialogueInteractEvent(match, d)).run();
					EphemeralResponse response = e.getResponse();
					if (response.getExtra() != null) {
						EmbeddedMessage extra = response.getExtra();
						EmbedBuilder builder = new EmbedBuilder();
						if (extra.getAuthor() != null) {
							builder.setAuthor(extra.getAuthor().getTag(), extra.getAuthor().getAvatarUrl(), extra.getAuthor().getAvatarUrl());
						}
						if (extra.getHeader() != null) builder.setTitle(extra.getHeader());
						if (extra.getFooter() != null) {
							if (extra.getFooter().getIconUrl() != null) {
								builder.setFooter(extra.getFooter().getText(), extra.getFooter().getIconUrl());
							} else {
								builder.setFooter(extra.getFooter().getText());
							}
						}
						if (extra.getColor() != null) builder.setColor(extra.getColor());
						if (extra.getThumbnail() != null) {
							builder.setThumbnail(extra.getThumbnail().getUrl());
						}
						if (extra.getDescription() != null) builder.setDescription(extra.getDescription());
						if (extra.getImage() != null) {
							builder.setImage(extra.getImage().getUrl());
						}
						for (EmbeddedMessage.Field f : extra.getFields()) {
							builder.addField(new MessageEmbed.Field(f.getName(), f.getValue(), f.inline()));
						}
						event.getHook().sendMessageEmbeds(builder.build()).queue();
					} else {
						if (response.isNegated()) {
							event.reply(response.get()).setEphemeral(false).queue();
						} else {
							event.reply(response.get()).setEphemeral(true).queue();
						}
					}
				}
			}
		}
	}

	@Override
	public void onMessageReactionAdd(@NotNull MessageReactionAddEvent e) {
		final Message message = e.retrieveMessage().submit().join();
		if (e.getUser() != null && !e.getUser().isBot()) {
			final Guppy guppy = entryPoint.getGuppy(e.getUser());
			Guppy.Message m;
			if (isFromTypes(e.getChannelType(), ChannelType.PRIVATE)) {
				m = entryPoint.wrapPrivateMessage(message);
			} else {
				m = entryPoint.wrapPublicMessage(message);
			}
			GuppyMessageReactEvent ev = new GuppyVentCall<>(new GuppyMessageReactEvent(guppy, m, e.getEmoji().getFormatted(), GuppyMessageReactEvent.ReactionResult.ADD)).schedule().join();
			if (ev.isCancelled()) {
				message.getReaction(e.getEmoji()).removeReaction(e.getUser()).queueAfter(2, TimeUnit.MILLISECONDS);
			}
		}
	}

	@Override
	public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent e) {
		final Message message = e.retrieveMessage().submit().join();
		if (e.getUser() != null && !e.getUser().isBot()) {
			final Guppy guppy = entryPoint.getGuppy(e.getUser());
			Guppy.Message m;
			if (isFromTypes(e.getChannelType(), ChannelType.PRIVATE)) {
				m = entryPoint.wrapPrivateMessage(message);
			} else {
				m = entryPoint.wrapPublicMessage(message);
			}
			new GuppyVentCall<>(new GuppyMessageReactEvent(guppy, m, e.getEmoji().getFormatted(), GuppyMessageReactEvent.ReactionResult.REMOVE)).schedule().join();
		}
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent e) {
		final Message message = e.getMessage();
		final Guppy guppy = entryPoint.getGuppy(e.getAuthor());
		// This part is responsible for handling what happens when a user messages the bot.

		if (e.isFromType(ChannelType.PRIVATE)) {
			if (!e.getAuthor().isBot()) {
				Guppy.Message m;
				if (isFromTypes(e.getChannelType(), ChannelType.PRIVATE)) {
					m = entryPoint.wrapPrivateMessage(message);
				} else {
					m = entryPoint.wrapPublicMessage(message);
				}
				new GuppyVentCall<>(new BotMessageReceivedEvent(guppy, m)).schedule().join();
			}
		}
		if (isFromTypes(e.getChannelType(), ChannelType.TEXT, ChannelType.NEWS, ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_PRIVATE_THREAD, ChannelType.GUILD_NEWS_THREAD, ChannelType.FORUM)) {
			Guppy.Message m = entryPoint.wrapPublicMessage(message);
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
		Command test = api.getCommands().get(e.getName());
		if (test != null && (test.getType() == Command.Type.SLASH || test.getType() == Command.Type.MULTI_MESSAGE || test.getType() == Command.Type.MULTI_USER)) {
			Command.Options options = test.getOptions();
			if (options.get().size() > 0) {
				final PantherMap<Class<?>, PantherCollection<Object>> classMap = new PantherEntryMap<>();
				for (Command.Option o : options.get()) {
					OptionMapping mapping = e.getOption(o.getName());
					if (mapping != null) {
						switch (o.getType()) {
							case USER:
								User u = mapping.getAsUser();
								classMap.computeIfAbsent(Guppy.class, new PantherList<>()).add(entryPoint.getGuppy(u));
								break;
							case INTEGER:
								int i = mapping.getAsInt();
								classMap.computeIfAbsent(Integer.class, new PantherList<>()).add(i);
								break;
							case NUMBER:
								Number n = mapping.getAsDouble();
								classMap.computeIfAbsent(Number.class, new PantherList<>()).add(n);
								break;
							case ROLE:
								Role r = mapping.getAsRole();
								classMap.computeIfAbsent(com.github.sanctum.jda.common.Role.class, new PantherList<>()).add(api.getRole(r.getIdLong()));
								break;
							case STRING:
								String s = mapping.getAsString();
								classMap.computeIfAbsent(String.class, new PantherList<>()).add(s);
								break;
						}
					}
				}
				// complete variable.
				Command.Variable variable = new Command.Variable() {
					@Override
					public @NotNull Channel getChannel() {
						return GuppyAPI.getInstance().getChannel(e.getChannel().getIdLong());
					}

					@Override
					public <T> @NotNull T get(@NotNull TypeAdapter<T> typeFlag, int index) {
						return (T) classMap.get(typeFlag.getType()).get(index);
					}

					@Override
					public <T> @NotNull T get(@NotNull Class<T> typeFlag, int index) {
						return (T) classMap.get(typeFlag).get(index);
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
					public <T> int size(@NotNull Class<T> typeFlag) {
						return classMap.get(typeFlag).size();
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
				// empty variable
				EphemeralResponse response = test.onExecuted(entryPoint.getGuppy(e.getUser()), new Command.Variable() {

					@Override
					public @NotNull Channel getChannel() {
						return GuppyAPI.getInstance().getChannel(e.getChannel().getIdLong());
					}

					@Override
					public <T> @NotNull T get(@NotNull TypeAdapter<T> typeFlag, int index) {
						return (T) new Object();
					}

					@Override
					public <T> @NotNull T get(@NotNull Class<T> typeFlag, int index) {
						return (T) new Object();
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
					public <T> int size(@NotNull Class<T> typeFlag) {
						return 0;
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
