package net.runelite.client.plugins.bodfishing.states;

import java.util.List;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.api.events.AnimationChanged;
import net.runelite.client.plugins.bodfishing.BodFishingPlugin;
import net.runelite.client.plugins.paistisuite.api.PBanking;
import net.runelite.client.plugins.paistisuite.api.PInteraction;
import net.runelite.client.plugins.paistisuite.api.PInventory;
import net.runelite.client.plugins.paistisuite.api.PObjects;
import net.runelite.client.plugins.paistisuite.api.PUtils;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.DaxWalker;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.models.RunescapeBank;
import net.runelite.client.plugins.paistisuite.api.WebWalker.wrappers.Keyboard;
import net.runelite.client.plugins.paistisuite.api.types.Filters;
import net.runelite.client.plugins.paistisuite.api.types.PItem;
import net.runelite.client.plugins.paistisuite.api.types.PTileObject;

@Slf4j
public class ProcessItemState extends State<BodFishingPlugin>
{
	BodFishingPlugin plugin;
	boolean isDropping = false;
	boolean isCooking = false;
	boolean isBanking = false;

	public ProcessItemState(BodFishingPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
	}

	@Override
	public boolean condition()
	{
		return PInventory.isFull();
	}

	@Override
	public String getName()
	{
		return "Processing";
	}

	@Override
	public void loop()
	{
		PUtils.sleepNormal(70, 130);

		switch (plugin.fishingChoice)
		{
			case BARBARIAN_VILLAGE:
				if (plugin.bankFishChoice)
				{
					if (plugin.bankCookedFishChoice)
					{
						cookFish();
						if (isCooking) {
							return;
						}
						dropItems(Filters.Items.nameContains("Burnt"));
					}

					if (PInventory.isFull()) {
						bankItems();
					}
				}
				else
				{
					if (plugin.cookedFishChoice) {
						cookFish();
					}
					if (isCooking) {
						return;
					}
					dropItems(Filters.Items.nameContains("Trout", "Salmon"));
				}
				break;
			case BARBARIAN_OUTPOST:
				dropItems(Filters.Items.nameContains("Leaping"));
				break;
		}
	}

	@Override
	public void onAnimationChanged(AnimationChanged event)
	{
	}

	private void dropItems(Predicate<PItem> predicate)
	{
		if (isDropping)
		{
			return;
		}

		Predicate<PItem> itemsToDrop = predicate;

		if (plugin.dropClueScrolls)
		{
			itemsToDrop = itemsToDrop.or(Filters.Items.nameContains("Clue bottle"));
		}

		List<PItem> items = PInventory.findAllItems(itemsToDrop);

		if (items.size() > 0)
		{
			isDropping = true;
			items.forEach(item ->
			{
				PInteraction.item(item, "Drop");
				PUtils.sleepNormal(200, 400);
			});

			isDropping = false;
		}
	}

	private void bankItems() {
		if (isBanking)
		{
			return;
		}

		if (!PBanking.isBankOpen())
		{
			isBanking = true;
			if (!DaxWalker.walkToBank(RunescapeBank.EDGEVILLE))
			{
				log.info("Unable to find the bank");
			}
			return;
		}

		if (!PUtils.waitCondition(6000, () -> PBanking.isBankOpen()))
		{
			log.info("Bank never got opened!");
		}

		PItem trout = PInventory.findItem(Filters.Items.nameContains("trout"));
		PItem salmon = PInventory.findItem(Filters.Items.nameContains("salmon"));
		PInteraction.item(trout, "Deposit-all");
		PUtils.sleepNormal(2000, 4000);
		PInteraction.item(salmon, "Deposit-all");
		PUtils.sleepNormal(600, 1200);
		PBanking.closeBank();
		isBanking = false;
	}

	private void cookFish() {
		if (isCooking) {
			return;
		}

		PTileObject fire = PObjects.findObject(Filters.Objects.idEquals(ObjectID.FIRE_26185));
		if (fire != null) {
			PItem trout = PInventory.findItem(Filters.Items.idEquals(ItemID.RAW_TROUT));
			if (trout != null && !isCooking) {
				isCooking = true;
				PInteraction.useItemOnTileObject(trout, fire);
				Keyboard.pressSpacebar();
				isCooking = !PUtils.waitCondition(15000, () -> PInventory.findItem(Filters.Items.idEquals(ItemID.RAW_TROUT)) != null);
			}

			PItem salmon = PInventory.findItem(Filters.Items.idEquals(ItemID.RAW_SALMON));
			if (salmon != null && !isCooking) {
				isCooking = true;
				PInteraction.useItemOnTileObject(salmon, fire);
				Keyboard.pressSpacebar();
				isCooking = !PUtils.waitCondition(15000, () -> PInventory.findItem(Filters.Items.idEquals(ItemID.RAW_SALMON)) != null);
			}
		}
	}
}
