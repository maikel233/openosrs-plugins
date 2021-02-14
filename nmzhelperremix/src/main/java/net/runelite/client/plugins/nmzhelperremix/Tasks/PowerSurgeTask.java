package net.runelite.client.plugins.nmzhelperremix.Tasks;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.ObjectID;
import net.runelite.api.QueryResults;
import net.runelite.api.events.GameTick;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.plugins.nmzhelperremix.MiscUtils;
import net.runelite.client.plugins.nmzhelperremix.NMZHelperConfig;
import net.runelite.client.plugins.nmzhelperremix.NMZHelperPlugin;
import net.runelite.client.plugins.nmzhelperremix.Task;

public class PowerSurgeTask extends Task
{
	public PowerSurgeTask(NMZHelperPlugin plugin, Client client, NMZHelperConfig config)
	{
		super(plugin, client, config);
	}

	@Override
	public boolean validate()
	{
		if (!config.powerSurge())
			return false;

		//in the nightmare zone
		if (!MiscUtils.isInNightmareZone(client))
			return false;

		QueryResults<GameObject> results = new GameObjectQuery()
			.idEquals(ObjectID.POWER_SURGE)
			.result(client);

		if (results == null || results.isEmpty())
		{
			return false;
		}

		GameObject obj = results.first();

		if (obj == null)
		{
			return false;
		}

		return true;
	}

	@Override
	public String getTaskDescription()
	{
		return "Power Surge";
	}

	@Override
	public void onGameTick(GameTick event)
	{
		QueryResults<GameObject> results = new GameObjectQuery()
			.idEquals(ObjectID.POWER_SURGE)
			.result(client);

		if (results == null || results.isEmpty())
		{
			return;
		}

		GameObject obj = results.first();

		if (obj == null)
		{
			return;
		}

		entry = new MenuEntry("Activate", "<col=ffff>Power surge", ObjectID.POWER_SURGE, MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(), obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY(), false);
		click();
	}
}