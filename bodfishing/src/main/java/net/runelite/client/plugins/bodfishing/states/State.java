package net.runelite.client.plugins.bodfishing.states;

import net.runelite.api.events.AnimationChanged;

public abstract class State<T>
{
	T plugin;
	String name;

	public State(T plugin){
		this.plugin = plugin;
		this.name = getName();
	}

	public abstract boolean condition();
	public abstract String getName();
	public abstract void loop();
	public abstract void onAnimationChanged(AnimationChanged event);
}