package com.github.sanctum.jda.common;

import com.github.sanctum.panther.container.ImmutablePantherCollection;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Dialogue {

	@NotNull String getId();

	@NotNull String getTitle();

	@NotNull PantherCollection<Row> getRows();

	@Nullable String getData(@NotNull String rowId);

	static @NotNull Dialogue.Builder of(@NotNull String id, @NotNull String title) {
		return new Builder(id, title);
	}

	interface Row {

		@Nullable String getValue();

		@NotNull String getId();

		@NotNull String getLabel();

		@Nullable String getPlaceholder();

		int getMinCharactersAllowed();

		int getMaxCharactersAllowed();

		boolean isShort();

		boolean isRequired();

		static @NotNull Row.Builder of(@NotNull String id, String label) {
			return new Builder().setId(id).setLabel(label);
		}

		static @NotNull Row of(@Nullable String value, @NotNull String id, @NotNull String label, @Nullable String placeholder, int minCharsAllowed, int maxCharsAllowed, boolean paragraph, boolean required) {
			return new Row() {
				@Override
				public @Nullable String getValue() {
					return value;
				}

				@Override
				public @NotNull String getId() {
					return id;
				}

				@Override
				public @NotNull String getLabel() {
					return label;
				}

				@Override
				public @Nullable String getPlaceholder() {
					return placeholder;
				}

				@Override
				public int getMinCharactersAllowed() {
					return minCharsAllowed;
				}

				@Override
				public int getMaxCharactersAllowed() {
					return maxCharsAllowed;
				}

				@Override
				public boolean isShort() {
					return !paragraph;
				}

				@Override
				public boolean isRequired() {
					return required;
				}
			};
		}

		class Builder {

			String value;
			String id;
			String label;
			String placeholder;
			int min, max;
			boolean paragraph, required;

			public Builder setValue(@NotNull String value) {
				this.value = value;
				return this;
			}

			public Builder setId(@NotNull String id) {
				this.id = id;
				return this;
			}

			public Builder setLabel(@NotNull String label) {
				this.label = label;
				return this;
			}

			public Builder setPlaceholder(@NotNull String value) {
				this.placeholder = value;
				return this;
			}

			public Builder setMinCharacters(int min) {
				this.min = min;
				return this;
			}

			public Builder setMaxCharacters(int max) {
				this.max = max;
				return this;
			}

			public Builder setRequired(boolean required) {
				this.required = required;
				return this;
			}

			public Builder setParagraph(boolean isParagraph) {
				this.paragraph = isParagraph;
				return this;
			}

			public Row build() {
				return Row.of(value, id, label, placeholder, min, max, paragraph, required);
			}

		}

	}

	class Builder {

		final PantherCollection<Row> rows = new PantherList<>(5);
		final String id;
		final String title;

		public Builder(@NotNull String id, @NotNull String title) {
			this.id = id;
			this.title = title;
		}

		public Builder addRow(@NotNull Row... rows) {
			for (Row r : rows) {
				if (this.rows.size() < 5) {
					this.rows.add(r);
				}
			}
			return this;
		}

		public Dialogue build() {
			return new Dialogue() {
				final ImmutablePantherCollection.Builder<Row> r = ImmutablePantherCollection.builder();
				{
					for (Row row : rows) {
						r.add(row);
					}
				}
				@Override
				public @NotNull String getId() {
					return id;
				}

				@Override
				public @NotNull String getTitle() {
					return title;
				}

				@Override
				public @NotNull PantherCollection<Row> getRows() {
					return r.build();
				}

				@Override
				public @Nullable String getData(@NotNull String rowId) {
					return null;
				}
			};
		}

	}

}
