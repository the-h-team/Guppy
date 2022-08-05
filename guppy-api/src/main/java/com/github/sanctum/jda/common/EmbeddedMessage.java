package com.github.sanctum.jda.common;

import java.awt.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EmbeddedMessage {

	@Nullable String getHeader();

	@Nullable Color getColor();

	@Nullable Image getImage();

	@Nullable Thumbnail getThumbnail();

	@Nullable String getDescription();

	@Nullable Guppy getAuthor();

	@Nullable Footer getFooter();

	@NotNull Field[] getFields();

	interface Field extends Nameable {

		@NotNull String getValue();

		boolean inline();

		static @NotNull Field of(@NotNull String name, @NotNull String value, boolean inline) {
			return new Field() {
				@Override
				public @NotNull String getValue() {
					return value;
				}

				@Override
				public boolean inline() {
					return inline;
				}

				@Override
				public @NotNull String getName() {
					return name;
				}
			};
		}

	}

	interface Footer {

		@Nullable String getIconUrl();

		@NotNull String getText();

	}

	interface Thumbnail {

		@NotNull String getUrl();

	}

	interface Image {

		@NotNull String getUrl();

	}

	class Builder {
		String header, footer, footerUrl, description, imageUrl, thumbnailUrl;
		Color color;
		Guppy author;
		Field[] fields;

		public Builder setHeader(@NotNull String header) {
			this.header = header;
			return this;
		}

		public Builder setDescription(@NotNull String description) {
			this.description = description;
			return this;
		}

		public Builder setImage(@NotNull String url) {
			this.imageUrl = url;
			return this;
		}

		public Builder setThumbnail(@NotNull String url) {
			this.thumbnailUrl = url;
			return this;
		}

		public Builder setColor(@NotNull Color color) {
			this.color = color;
			return this;
		}

		public Builder setFooter(@NotNull String footer) {
			this.footer = footer;
			return this;
		}

		public Builder setFooterImage(@NotNull String url) {
			this.footerUrl = url;
			return this;
		}

		public Builder setAuthor(@NotNull Guppy guppy) {
			this.author = guppy;
			return this;
		}

		public Builder setFields(@NotNull Field... fields) {
			this.fields = fields;
			return this;
		}

		public EmbeddedMessage build() {
			return new EmbeddedMessage() {
				@Override
				public @Nullable String getHeader() {
					return header;
				}

				@Override
				public @Nullable Color getColor() {
					return color;
				}

				@Override
				public @Nullable Image getImage() {
					return imageUrl != null ? () -> imageUrl : null;
				}

				@Override
				public @Nullable Thumbnail getThumbnail() {
					return thumbnailUrl != null ? () -> thumbnailUrl : null;
				}

				@Override
				public @Nullable String getDescription() {
					return description;
				}

				@Override
				public @Nullable Guppy getAuthor() {
					return author;
				}

				@Override
				public @Nullable Footer getFooter() {
					return footer != null ? new Footer() {
						@Override
						public @Nullable String getIconUrl() {
							return footerUrl;
						}

						@Override
						public @NotNull String getText() {
							return footer;
						}
					} : null;
				}

				@Override
				public @NotNull Field[] getFields() {
					return fields != null ? fields : new Field[0];
				}
			};
		}

	}

}
