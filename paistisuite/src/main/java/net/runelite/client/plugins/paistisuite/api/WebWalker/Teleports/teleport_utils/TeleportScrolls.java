package net.runelite.client.plugins.paistisuite.api.WebWalker.Teleports.teleport_utils;
import net.runelite.client.plugins.paistisuite.api.WebWalker.shared.helpers.magic.Validatable;
import net.runelite.client.plugins.paistisuite.api.WebWalker.wrappers.RSTile;
import org.apache.commons.lang3.NotImplementedException;

public enum TeleportScrolls implements Validatable {
    NARDAH("Nardah teleport",new RSTile(3419, 2916, 0)),
    DIGSITE("Digsite teleport",new RSTile(3325, 3411, 0)),
    FELDIP_HILLS("Feldip hills teleport",new RSTile(2540, 2924, 0)),
    LUNAR_ISLE("Lunar isle teleport",new RSTile(2095, 3913, 0)),
    MORTTON("Mort'ton teleport",new RSTile(3487, 3287, 0)),
    PEST_CONTROL("Pest control teleport",new RSTile(2658, 2658, 0)),
    PISCATORIS("Piscatoris teleport",new RSTile(2340, 3650, 0)),
    TAI_BWO_WANNAI("Tai bwo wannai teleport",new RSTile(2789,3065,0)),
    ELF_CAMP("Elf camp teleport",new RSTile(2193, 3258, 0)),
    MOS_LE_HARMLESS("Mos le'harmless teleport", new RSTile(3700, 2996, 0)),
    LUMBERYARD("Lumberyard teleport",new RSTile(3302, 3487, 0)),
    ZULLANDRA("Zul-andra teleport",new RSTile(2195, 3055, 0)),
    KEY_MASTER("Key master teleport",new RSTile(1311, 1251, 0)),
    REVENANT_CAVES("Revenant cave teleport",new RSTile(3130, 3832, 0)),
    WATSON("Watson teleport", new RSTile(1645, 3579,0))
    ;
    private String name;
    private RSTile location;
    TeleportScrolls(String name, RSTile location){
        this.name = name;
        this.location = location;
    }

    public int getX(){
        return location.getX();
    }
    public int getY(){
        return location.getY();
    }
    public int getZ(){
        return location.getPlane();
    }

    public boolean teleportTo(boolean shouldWait){
        throw new NotImplementedException();
    }

    public boolean hasScroll(){
        throw new NotImplementedException();
    }

    public RSTile getLocation(){
        return location;
    }

    @Override
    public boolean canUse(){
        throw new NotImplementedException();
    }

    public boolean scrollbookContains(){
        throw new NotImplementedException();
    }

}