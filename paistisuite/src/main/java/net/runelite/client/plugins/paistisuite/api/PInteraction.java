package net.runelite.client.plugins.paistisuite.api;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.paistisuite.PaistiSuite;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class PInteraction {
    public static boolean tileObject(TileObject to, String... actions) {
        if (to == null) return false;
        ObjectDefinition def = PObjects.getObjectDef(to);
        if (def == null) return false;

        String[] possibleActions = def.getActions();
        List<String> desiredActions = Arrays.asList(actions);
        int actionIndex = -1;
        String action = "";
        int i = 0;
        boolean found = false;
        for (String a : possibleActions) {
            if (desiredActions.contains(a)) {
                action = a;
                actionIndex = i;
                found = true;
                break;
            }
            i++;
        }
        if (!found) return false;
        MenuOpcode actionOp = null;
        switch (actionIndex) {
            case 0:
                actionOp = MenuOpcode.GAME_OBJECT_FIRST_OPTION;
                break;
            case 1:
                actionOp = MenuOpcode.GAME_OBJECT_SECOND_OPTION;
                break;
            case 2:
                actionOp = MenuOpcode.GAME_OBJECT_THIRD_OPTION;
                break;
            case 3:
                actionOp = MenuOpcode.GAME_OBJECT_FOURTH_OPTION;
                break;
            case 4:
                actionOp = MenuOpcode.GAME_OBJECT_FIFTH_OPTION;
                break;
            default:
                return false;
        }

        /*
        MenuInterceptor.setNextEntry(
                new MenuEntry(
                        action,
                        "",
                        go.getId(),
                        actionOp.getId(),
                        go.getSceneMinLocation().getX(),
                        go.getSceneMinLocation().getY(),
                        false
                )
        );

        PMouse.clickShape(go.getConvexHull());
         */

        if (to instanceof GameObject){
            MenuOpcode finalActionOp = actionOp;
            PaistiSuite.getInstance().clientExecutor.schedule(() -> {
                PUtils.getClient().invokeMenuAction(
                        "",
                        "",
                        to.getId(),
                        finalActionOp.getId(),
                        ((GameObject)to).getSceneMinLocation().getX(),
                        ((GameObject)to).getSceneMinLocation().getY());
            }, "interact_gameObject");
        } else if (to instanceof WallObject){
            MenuOpcode finalActionOp = actionOp;
            PaistiSuite.getInstance().clientExecutor.schedule(() -> {
                PUtils.getClient().invokeMenuAction(
                        "",
                        "",
                        to.getId(),
                        finalActionOp.getId(),
                        ((WallObject)to).getWorldLocation().getX() - PUtils.getClient().getBaseX(),
                        ((WallObject)to).getWorldLocation().getY() - PUtils.getClient().getBaseY()
                );
            }, "interact_gameObject");
        } else {
            return false;
        }

        return true;
    }

    public static boolean useItemOnItem(WidgetItem item, WidgetItem target){
        if (item.equals(target)) return false;

        PaistiSuite.getInstance().clientExecutor.schedule(() -> {
            PUtils.getClient().setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
            PUtils.getClient().setSelectedItemSlot(item.getIndex());
            PUtils.getClient().setSelectedItemID(item.getId());
            PUtils.getClient().invokeMenuAction(
                    "",
                    "",
                    target.getId(),
                    MenuOpcode.ITEM_USE_ON_WIDGET_ITEM.getId(),
                    target.getIndex(),
                    9764864);
        }, "interact_useItemOnItem");


        return true;
    }

    public static boolean useItemOnGameObject(WidgetItem item, TileObject to){
        if (item == null || to == null) return false;

        if (to instanceof GameObject){
            PaistiSuite.getInstance().clientExecutor.schedule(() -> {
                PUtils.getClient().setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
                PUtils.getClient().setSelectedItemSlot(item.getIndex());
                PUtils.getClient().setSelectedItemID(item.getId());
                PUtils.getClient().invokeMenuAction(
                        "",
                        "",
                        to.getId(),
                        MenuOpcode.ITEM_USE_ON_GAME_OBJECT.getId(),
                        ((GameObject)to).getSceneMinLocation().getX(),
                        ((GameObject)to).getSceneMinLocation().getY());
            }, "interact_useItemOnItem");
        } else if (to instanceof WallObject){
            PaistiSuite.getInstance().clientExecutor.schedule(() -> {
                PUtils.getClient().setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
                PUtils.getClient().setSelectedItemSlot(item.getIndex());
                PUtils.getClient().setSelectedItemID(item.getId());
                PUtils.getClient().invokeMenuAction(
                        "",
                        "",
                        to.getId(),
                        MenuOpcode.ITEM_USE_ON_GAME_OBJECT.getId(),
                        ((WallObject)to).getWorldLocation().getX() - PUtils.getClient().getBaseX(),
                        ((WallObject)to).getWorldLocation().getY() - PUtils.getClient().getBaseY());
            }, "interact_useItemOnItem");
        } else {
            return false;
        }



        return true;
    }

    public static boolean npc(NPC npc, String... actions) {
        NPCDefinition def = npc.getDefinition();
        if(def == null) return false;
        String[] possibleActions = def.getActions();
        List<String> desiredActions = Arrays.asList(actions);
        int actionIndex = -1;
        String action = "";
        int i = 0;
        boolean found = false;
        for (String a : possibleActions) {
            if (desiredActions.contains(a)) {
                action = a;
                actionIndex = i;
                found = true;
                break;
            }
            i++;
        }
        if (!found) return false;

        MenuOpcode actionOp = null;
        switch (actionIndex) {
            case 0:
                actionOp = MenuOpcode.NPC_FIRST_OPTION;
                break;
            case 1:
                actionOp = MenuOpcode.NPC_SECOND_OPTION;
                break;
            case 2:
                actionOp = MenuOpcode.NPC_THIRD_OPTION;
                break;
            case 3:
                actionOp = MenuOpcode.NPC_FOURTH_OPTION;
                break;
            case 4:
                actionOp = MenuOpcode.NPC_FIFTH_OPTION;
                break;
            default:
                return false;
        }

        MenuOpcode finalActionOp = actionOp;
        PaistiSuite.getInstance().clientExecutor.schedule(() -> {
            PUtils.getClient().invokeMenuAction(
                    "",
                    "",
                    npc.getIndex(),
                    finalActionOp.getId(),
                    0,
                    0);
        }, "interact_npc");

        return true;
    }

    public static boolean widget(Widget widget, String... actions) {
        String[] possibleActions = widget.getActions();
        List<String> desiredActions = Arrays.asList(actions);
        int actionIndex = -1;
        String action = "";
        int i = 0;
        boolean found = false;
        for (String a : possibleActions) {
            if (desiredActions.contains(a)) {
                action = a;
                actionIndex = i;
                found = true;
                break;
            }
            i++;
        }

        if (!found) return false;
        int finalActionIndex = actionIndex + 1;
        MenuOpcode finalActionOp = MenuOpcode.CC_OP;
        PaistiSuite.getInstance().clientExecutor.schedule(() -> {
            PUtils.getClient().invokeMenuAction(
                    "",
                    "",
                    finalActionIndex,
                    finalActionOp.getId(),
                    0,
                    0);
        }, "interact_widget");

        return true;
    }

    public static boolean item(WidgetItem item, String ...actions){
        throw new NotImplementedException();
    }
}