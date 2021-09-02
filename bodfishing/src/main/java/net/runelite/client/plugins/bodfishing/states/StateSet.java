package net.runelite.client.plugins.bodfishing.states;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import net.runelite.api.events.AnimationChanged;

public class StateSet<T>
{
	T plugin;

	public StateSet(T plugin) {
		this.plugin = plugin;
	}

	public List<State<T>> stateList = new ArrayList<>();

	public StateSet(State<T>... states)
	{
		stateList.addAll(Arrays.asList(states));
	}

	public void addAll(State<T>... states)
	{
		stateList.addAll(Arrays.asList(states));
	}

	public void eachAnimationEvent(AnimationChanged event) {
		stateList.forEach(state -> state.onAnimationChanged(event));
	}

	public void clear()
	{
		stateList.clear();
	}

	/**
	 * Iterates through all the tasks in the set and returns
	 * the highest priority valid task.
	 *
	 * @return The first valid task from the task list or null if no valid task.
	 */
	public State<T> getValidState()
	{
		for (State<T> state : this.stateList)
		{
			if (state.condition())
			{
				return state;
			}
		}
		return null;
	}
}
