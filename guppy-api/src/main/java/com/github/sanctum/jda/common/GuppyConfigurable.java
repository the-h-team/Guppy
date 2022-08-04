package com.github.sanctum.jda.common;

import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.recursive.Service;

public final class GuppyConfigurable implements Service {

	Configurable configurable;

	public Configurable get() {
		return configurable;
	}

	public void set(Configurable configurable) {
		this.configurable = configurable;
	}
}
