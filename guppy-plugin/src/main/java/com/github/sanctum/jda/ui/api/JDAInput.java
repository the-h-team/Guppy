package com.github.sanctum.jda.ui.api;

public final class JDAInput {

	String token;
	String activity;
	String activityMessage;

	public String getActivity() {
		return activity;
	}

	public String getActivityMessage() {
		return activityMessage;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public void setActivityMessage(String activityMessage) {
		this.activityMessage = activityMessage;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

}
