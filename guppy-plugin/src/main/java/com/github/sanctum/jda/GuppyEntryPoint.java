package com.github.sanctum.jda;

import com.github.sanctum.jda.common.Channel;
import com.github.sanctum.jda.common.Command;
import com.github.sanctum.jda.common.EmbeddedMessage;
import com.github.sanctum.jda.common.Guppy;
import com.github.sanctum.jda.common.GuppyConfigurable;
import com.github.sanctum.jda.common.JDAController;
import com.github.sanctum.jda.common.Reaction;
import com.github.sanctum.jda.common.Role;
import com.github.sanctum.jda.event.GuppyMessageReactEvent;
import com.github.sanctum.jda.listener.GuppyCommandProcessor;
import com.github.sanctum.jda.listener.GuppyListenerAdapter;
import com.github.sanctum.jda.loading.DockingAgent;
import com.github.sanctum.jda.ui.api.ConsoleCommand;
import com.github.sanctum.jda.ui.api.JDAInput;
import com.github.sanctum.jda.ui.content.MainPanel;
import com.github.sanctum.jda.ui.content.StopConsoleCommand;
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
import com.github.sanctum.panther.event.Subscribe;
import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.event.VentMap;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.JsonConfiguration;
import com.github.sanctum.panther.recursive.ServiceFactory;
import com.github.sanctum.panther.util.Deployable;
import com.github.sanctum.panther.util.DeployableMapping;
import com.github.sanctum.panther.util.PantherLogger;
import com.github.sanctum.panther.util.SimpleAsynchronousTask;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;
import javax.swing.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
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

	public @NotNull Guppy.Message newMessage(@NotNull Message message) {
		return new Guppy.Message() {
			@Override
			public @NotNull String getText() {
				return message.getContentDisplay();
			}

			@Override
			public @NotNull Channel getChannel() {
				return new Channel() {
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
					public @NotNull String getName() {
						return message.getChannel().getName();
					}

					@Override
					public Deployable<Guppy.Message> sendMessage(@NotNull String m) {
						return GuppyEntryPoint.newDeployable(() -> newMessage(message.getChannel().sendMessage(m).submit().join()));
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
					if (m.getAuthor() != null) builder.setAuthor(GuppyAPI.getInstance().getGuppy(m.getAuthor().getName(), false));
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

			final Link link;

			{
				// search in data for user link.

				this.link = null;
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
			public @NotNull Role[] getRoles() {
				return jda.getGuildById(570063954118836245L).getRoles().stream().map(GuppyEntryPoint.this::newRole).toArray(Role[]::new);
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
			public Deployable<Message> sendMessage(@NotNull String message) {
				return newDeployable(() -> {
					;
					return newMessage(u.openPrivateChannel().map(privateChannel -> privateChannel.sendMessage(message).submit().join()).submit().join());
				});
			}
		};
	}

	public @NotNull Channel newChannel(@NotNull TextChannel t) {
		return new Channel() {

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
				return t.getThreadChannels().stream().map(threadChannel -> new Thread() {
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
					public Deployable<Guppy.Message> sendMessage(@NotNull String message) {
						return newDeployable(() -> newMessage(threadChannel.sendMessage(message).submit().join()));
					}

					@Override
					public Deployable<EmbeddedMessage> sendEmbeddedMessage(@NotNull EmbeddedMessage m) {
						return newDeployable(() -> {
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
						});
					}
				}).collect(PantherCollectors.toImmutableList());
			}

			@Override
			public boolean isPrivate() {
				return false;
			}

			@Override
			public Deployable<Guppy.Message> sendMessage(@NotNull String message) {
				return newDeployable(() -> newMessage(t.sendMessage(message).submit().join()));
			}

			@Override
			public Deployable<EmbeddedMessage> sendEmbeddedMessage(@NotNull EmbeddedMessage m) {
				return newDeployable(() -> {
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
				});
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
		final JDAController controller = new JDAController() {
			@Override
			public boolean isRunning() {
				return false;
			}

			@Override
			public Deployable<JDAController> stop() {
				return newDeployable(() -> {
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
				});
			}

			@Override
			public Deployable<JDAController> start() {
				return newDeployable(() -> {
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
				});
			}

			@Override
			public Deployable<JDAController> restart() {
				return newDeployable(() -> {
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
				});
			}

			@Override
			public Deployable<JDAController> restartAfter(long wait) {
				return newDeployable(() -> {
					SimpleAsynchronousTask.runLater(restart()::deploy, wait);
					return this;
				});
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
		public @NotNull PantherCollection<Channel> getChannels() {
			return jda.getTextChannels().stream().map(GuppyEntryPoint.this::newChannel).collect(PantherCollectors.toImmutableList());
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
			return newDeployable(() -> {
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
			});
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
		public @NotNull <T> Deployable<T> newDeployable(Supplier<T> supplier) {
			return GuppyEntryPoint.newDeployable(supplier);
		}

	}

	public static PantherCollection<ConsoleCommand> getConsoleCommands() {
		return commands.values();
	}

	public static @NotNull("Main panel not loaded!") MainPanel getMainPanel() {
		return main;
	}

	public static <T> Deployable<T> newDeployable(Supplier<T> supplier) {
		return new Deployable<T>() {

			T element;

			@Override
			public Deployable<T> deploy() {
				element = supplier.get();
				return this;
			}

			@Override
			public Deployable<T> deploy(Consumer<? super T> consumer) {
				deploy();
				consumer.accept(element);
				return this;
			}

			@Override
			public Deployable<T> queue() {
				element = submit().join();
				return this;
			}

			@Override
			public Deployable<T> queue(Consumer<? super T> consumer, long timeout) {
				return queue();
			}

			@Override
			public Deployable<T> queue(Consumer<? super T> consumer, Date date) {
				return queue();
			}

			@Override
			public <O> DeployableMapping<O> map(Function<? super T, ? extends O> mapper) {
				throw new IllegalStateException("Mapping not supported.");
			}

			@Override
			public Deployable<T> queue(long timeout) {
				return queue();
			}

			@Override
			public Deployable<T> queue(Date date) {
				return queue();
			}

			@Override
			public CompletableFuture<T> submit() {
				return CompletableFuture.supplyAsync(supplier);
			}

			@Override
			public T get() {
				return this.element != null ? this.element : Deployable.super.get();
			}

			@Override
			public T complete() {
				deploy();
				return this.element;
			}
		};

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
					builder.addEventListeners(new GuppyListenerAdapter());
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
