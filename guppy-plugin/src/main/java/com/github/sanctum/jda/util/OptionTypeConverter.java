package com.github.sanctum.jda.util;

import com.github.sanctum.jda.common.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

public final class OptionTypeConverter {

	public static @NotNull OptionType get(@NotNull Command.Option option) {
		switch (option.getType()) {
			case USER:
				return OptionType.USER;
			case STRING:
				return OptionType.STRING;
			case INTEGER:
				return OptionType.INTEGER;
			case ROLE:
				return OptionType.ROLE;
			case NUMBER:
				return OptionType.NUMBER;
			default:
				return OptionType.UNKNOWN;
		}
	}

}
