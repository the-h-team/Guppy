package com.github.sanctum.jda.common;

import java.awt.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EmbeddedMessage {

	@Nullable String getHeader();

	@Nullable Color getColor();

	@Nullable String getImage();

	@Nullable String getThumbnail();

	@Nullable String getDescription();

	@Nullable Guppy getAuthor();

	@Nullable String getFooter();

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

	class Builder {
		String header, footer, description, imageUrl, thumbnailUrl;
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
				public @Nullable String getImage() {
					return imageUrl;
				}

				@Override
				public @Nullable String getThumbnail() {
					return thumbnailUrl;
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
				public @Nullable String getFooter() {
					return footer;
				}

				@Override
				public @NotNull Field[] getFields() {
					return fields != null ? fields : new Field[0];
				}
			};
		}

	}

}
