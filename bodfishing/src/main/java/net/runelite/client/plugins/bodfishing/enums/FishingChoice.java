package net.runelite.client.plugins.bodfishing.enums;

public enum FishingChoice
{
	BARBARIAN_VILLAGE("Barbarian Village"),
	BARBARIAN_OUTPOST("Barbarian Outpost");

	public String name;

	FishingChoice(String s)
	{
		name = s;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
