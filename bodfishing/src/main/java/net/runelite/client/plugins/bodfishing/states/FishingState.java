package net.runelite.client.plugins.bodfishing.states;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.AnimationID;
import net.runelite.api.NPC;
import net.runelite.api.events.AnimationChanged;
import net.runelite.client.plugins.bodfishing.BodFishingPlugin;
import net.runelite.client.plugins.bodfishing.enums.FishingChoice;
import net.runelite.client.plugins.paistisuite.api.PInteraction;
import net.runelite.client.plugins.paistisuite.api.PInventory;
import net.runelite.client.plugins.paistisuite.api.PObjects;
import net.runelite.client.plugins.paistisuite.api.PPlayer;
import net.runelite.client.plugins.paistisuite.api.PUtils;
import net.runelite.client.plugins.paistisuite.api.types.Filters;
import net.runelite.client.plugins.paistisuite.api.types.PItem;

@Slf4j
public class FishingState extends State<BodFishingPlugin>
{
	BodFishingPlugin plugin;

	public FishingState(BodFishingPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
	}

	@Override
	public void onAnimationChanged(AnimationChanged event)
	{

	}

	@Override
	public boolean condition()
	{
		return !PInventory.isFull();
	}

	@Override
	public String getName()
	{
		return "Fishing";
	}

	@Override
	public void loop()
	{
		PUtils.sleepNormal(70, 130);
		if (PInventory.isFull())
		{
			return;
		}

		if (!isFishingAnimation())
		{
			if (!plugin.enableTickManipulation)
			{
				// defaults to 1800 - 2400;
				PUtils.sleepNormal(plugin.minSleepBeforeNewSpot, plugin.maxSleepBeforeNewSpot);
			}

			String actionName = "";
			switch (plugin.fishingChoice)
			{
				case BARBARIAN_OUTPOST:
					actionName = "Use-rod";
					break;
				case BARBARIAN_VILLAGE:
					actionName = "Lure";
					break;
			}


			NPC fishingSpot = PObjects.findNPC(Filters.NPCs.actionsContains(actionName)
				.and(n -> n.getWorldLocation().distanceTo2D(PPlayer.getWorldLocation()) < 20));

			if (!isFishingAnimation()) {
				if (!PInteraction.npc(fishingSpot, actionName))
				{
					PUtils.sendGameMessage("Unable to find fishing spot");
				}
			}

		}

		if (plugin.enableTickManipulation)
		{
			PUtils.waitCondition(7500, () -> isFishingAnimation());
			// defaults to 650 - 800
			PUtils.sleepNormal(plugin.minSleepBefore3t, plugin.maxSleepBefore3t);
			switch (plugin.tickManipulation)
			{
				case GUAM_TAR:
					PItem tar = PInventory.findItem(Filters.Items.nameEquals("Swamp tar"));
					PItem herb = PInventory.findItem(Filters.Items.nameContains("Guam leaf"));
					if (!PInteraction.useItemOnItem(tar, herb))
					{
						PUtils.sendGameMessage("Unable to make guam tar");
					}
					break;
				case TEAK_KNIFE:
					PItem log = PInventory.findItem(Filters.Items.nameEquals("Teak logs"));
					PItem knife = PInventory.findItem(Filters.Items.nameEquals("Knife"));
					if (!PInteraction.useItemOnItem(log, knife))
					{
						PUtils.sendGameMessage("Unable to cut teak log");
					}
			}

			// defaults to 200 - 400
			PUtils.sleepNormal(plugin.minSleepBeforeDrop, plugin.maxSleepBeforeDrop);
			if (plugin.fishingChoice == FishingChoice.BARBARIAN_OUTPOST)
			{
				PItem dropFish = PInventory.findItem(Filters.Items.nameContains("Leaping "));
				PInteraction.item(dropFish, "Drop");
			}
		}

	}

	private boolean isFishingAnimation()
	{
		if (plugin.enableTickManipulation)
		{
			return PPlayer.get().getAnimation() == 622;
		}
		return PPlayer.get().getAnimation() == AnimationID.FISHING_POLE_CAST || PPlayer.get().getAnimation() == 622;
	}
}
