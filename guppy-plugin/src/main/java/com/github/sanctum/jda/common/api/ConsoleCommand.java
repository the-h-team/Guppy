package com.github.sanctum.jda.common.api;


import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;

public abstract class ConsoleCommand {

	private final String label;
	private PantherCollection<String> aliases;

	protected ConsoleCommand(String label) {
		this.label = label;
		this.aliases = new PantherList<>();
	}

	public abstract void execute(String[] arguments);

	public String getLabel() {
		return label;
	}

	public PantherCollection<String> getAliases() {
		return aliases;
	}

	public void setAliases(PantherCollection<String> aliases) {
		this.aliases = aliases;
	}
}
