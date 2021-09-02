package net.runelite.client.plugins.bodfishing.enums;

public enum TickManipulation
{
	TEAK_KNIFE("Teak -> Knife"),
	GUAM_TAR("Guam -> Tar");

	public String name;

	TickManipulation(String s)
	{
		name = s;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
