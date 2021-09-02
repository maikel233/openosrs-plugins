package net.runelite.client.plugins.bodfishing;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.bodfishing.enums.FishingChoice;
import net.runelite.client.plugins.bodfishing.enums.TickManipulation;

@ConfigGroup("BodFishing")
public interface BodFishingConfig extends Config
{
	@ConfigItem(
		keyName = "enableOverlay",
		name = "Enable overlay",
		description = "Enable drawing of the overlay",
		position = 0
	)
	default boolean enableOverlay()
	{
		return false;
	}

	@ConfigItem(
		keyName = "enableTickManipulation",
		name = "Enable 3T",
		description = "Enable 3t fishing",
		position = 1
	)
	default boolean enableTickManipulation()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tickManipulationChoice",
		name = "3T Method",
		description = "Select the tick manipulation method you would like to perform",
		hidden = true,
		unhide = "enableTickManipulation",
		unhideValue = "true",
		position = 2
	)
	default TickManipulation tickManipulationChoice()
	{
		return TickManipulation.TEAK_KNIFE;
	}

	@ConfigItem(
		keyName = "fishingChoice",
		name = "Fishing Place",
		description = "Select the fishing place you would like to fish at",
		position = 3
	)
	default FishingChoice fishingChoice()
	{
		return FishingChoice.BARBARIAN_OUTPOST;
	}

	@ConfigItem(
		keyName = "bankFishChoice",
		name = "Bank fish?",
		description = "Select whether you would like to bank fish.",
		hidden = true,
		unhide = "fishingChoice",
		unhideValue = "Barbarian Village",
		position = 4
	)
	default boolean bankFishChoice()
	{
		return false;
	}

	@ConfigItem(
		keyName = "bankCookedFishChoice",
		name = "Do you want them cooked?",
		description = "Select whether you would like the fish to be cooked.",
		hidden = true,
		unhide = "bankFishChoice",
		unhideValue = "true",
		position = 5
	)
	default boolean bankCookedFishChoice()
	{
		return false;
	}

	@ConfigItem(
		keyName = "cookedFishChoice",
		name = "Do you want to cook fish before dropping?",
		description = "Select whether you would like the fish to be cooked before dropping.",
		hidden = true,
		unhide = "bankFishChoice",
		unhideValue = "false",
		position = 6
	)
	default boolean cookedFishChoice()
	{
		return true;
	}


	@ConfigItem(
		keyName = "dropClueScrolls",
		name = "Drop clues?",
		description = "Enable dropping of clues",
		position = 7
	)
	default boolean dropClueScrolls()
	{
		return true;
	}

	@ConfigSection(
		keyName = "delayConfig",
		name = "Sleep Delay Configuration",
		description = "Configure how the bot handles sleep delays. This is for debug only.",
		position = 50,
		closedByDefault = true
	)
	String delayConfig = "delayConfig";

	@ConfigItem(
		keyName = "minSleepBeforeNewSpot",
		name = "New spot MIN",
		description = "Minimum sleep before going to new spot",
		position = 51,
		section = "delayConfig",
		hide = "enableTickManipulation",
		unhideValue = "true"
	)
	default int minSleepBeforeNewSpot()
	{
		return 1800;
	}

	@ConfigItem(
		keyName = "maxSleepBeforeNewSpot",
		name = "New spot MAX",
		description = "Maximum sleep before going to new spot",
		position = 52,
		section = "delayConfig",
		hide = "enableTickManipulation",
		unhideValue = "true"
	)
	default int maxSleepBeforeNewSpot()
	{
		return 2400;
	}

	@ConfigItem(
		keyName = "minSleepBefore3t",
		name = "Before 3t min",
		description = "Minimum sleep before starting 3t anim",
		position = 53,
		section = "delayConfig",
		hidden = true,
		unhide = "enableTickManipulation",
		unhideValue = "true"
	)
	default int minSleepBefore3t()
	{
		return 650;
	}

	@ConfigItem(
		keyName = "maxSleepBefore3t",
		name = "Before 3t max",
		description = "Maximum sleep before starting 3t anim",
		position = 54,
		section = "delayConfig",
		hidden = true,
		unhide = "enableTickManipulation",
		unhideValue = "true"
	)
	default int maxSleepBefore3t()
	{
		return 800;
	}

	@ConfigItem(
		keyName = "minSleepBeforeDrop",
		name = "Before drop min",
		description = "Minimum sleep before dropping fish",
		position = 55,
		section = "delayConfig",
		hidden = true,
		unhide = "enableTickManipulation",
		unhideValue = "true"
	)
	default int minSleepBeforeDrop()
	{
		return 200;
	}

	@ConfigItem(
		keyName = "maxSleepBeforeDrop",
		name = "Before drop max",
		description = "Maximum sleep before dropping fish",
		position = 56,
		section = "delayConfig",
		hidden = true,
		unhide = "enableTickManipulation",
		unhideValue = "true"
	)
	default int maxSleepBeforeDrop()
	{
		return 400;
	}

	@ConfigItem(
		keyName = "startButton",
		name = "Start",
		description = "Start",
		position = 101
	)
	default Button startButton()
	{
		return new Button();
	}

	@ConfigItem(
		keyName = "stopButton",
		name = "Stop",
		description = "Stop",
		position = 102
	)
	default Button stopButton()
	{
		return new Button();
	}
}
