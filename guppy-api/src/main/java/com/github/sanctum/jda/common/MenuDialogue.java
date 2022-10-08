package com.github.sanctum.jda.common;

import com.github.sanctum.panther.container.PantherCollection;
import org.jetbrains.annotations.NotNull;

public interface MenuDialogue extends Dialogue {

	@NotNull PantherCollection<Dropdown> getMenus();

	interface Dropdown {



	}

}
