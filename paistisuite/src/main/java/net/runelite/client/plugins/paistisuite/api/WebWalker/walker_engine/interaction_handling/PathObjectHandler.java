package net.runelite.client.plugins.paistisuite.api.WebWalker.walker_engine.interaction_handling;

import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.paistisuite.api.PInventory;
import net.runelite.client.plugins.paistisuite.api.PObjects;
import net.runelite.client.plugins.paistisuite.api.PPlayer;
import net.runelite.client.plugins.paistisuite.api.PUtils;
import net.runelite.client.plugins.paistisuite.api.WebWalker.walker_engine.WaitFor;
import net.runelite.client.plugins.paistisuite.api.WebWalker.walker_engine.bfs.BFS;
import net.runelite.client.plugins.paistisuite.api.WebWalker.walker_engine.local_pathfinding.PathAnalyzer;
import net.runelite.client.plugins.paistisuite.api.WebWalker.walker_engine.local_pathfinding.Reachable;
import net.runelite.client.plugins.paistisuite.api.WebWalker.walker_engine.real_time_collision.RealTimeCollisionTile;
import net.runelite.client.plugins.paistisuite.api.WebWalker.wrappers.RSTile;
import net.runelite.client.plugins.paistisuite.api.WebWalker.wrappers.RSVarBit;

import javax.inject.Singleton;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class PathObjectHandler {
    private static PathObjectHandler instance;
    private final TreeSet<String> sortedOptions, sortedBlackList, sortedBlackListOptions, sortedHighPriorityOptions;

    private PathObjectHandler(){
        sortedOptions = new TreeSet<>(
                Arrays.asList("Enter", "Cross", "Pass", "Open", "Close", "Walk-through", "Use", "Pass-through", "Exit",
                        "Walk-Across", "Go-through", "Walk-across", "Climb", "Climb-up", "Climb-down", "Climb-over", "Climb over", "Climb-into", "Climb-through",
                        "Board", "Jump-from", "Jump-across", "Jump-to", "Squeeze-through", "Jump-over", "Pay-toll(10gp)", "Step-over", "Walk-down", "Walk-up","Walk-Up", "Travel", "Get in",
                        "Investigate", "Operate", "Climb-under","Jump","Crawl-down","Crawl-through","Activate","Push","Squeeze-past","Walk-Down",
                        "Swing-on", "Climb up", "Ascend", "Descend","Channel","Teleport","Pass-Through","Jump-up","Jump-down","Swing across"));

        sortedBlackList = new TreeSet<>(Arrays.asList("Coffin","Drawers","null"));
        sortedBlackListOptions = new TreeSet<>(Arrays.asList("Chop down"));
        sortedHighPriorityOptions = new TreeSet<>(Arrays.asList("Pay-toll(10gp)","Squeeze-past"));
    }

    private static PathObjectHandler getInstance(){
        return instance != null ? instance : (instance = new PathObjectHandler());
    }

    private enum SpecialObject {
        WEB("Web", "Slash", null, new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return PObjects.getAllObjectsWithDefs()
                        .stream()
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceToHypotenuse(PPlayer.location()) < 15)
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceTo(destinationDetails.getAssumed().toWorldPoint()) <= 1)
                        .filter(pair -> pair.getSecond().getName().equalsIgnoreCase("Web"))
                        .anyMatch(
                                pair -> Arrays.stream(pair.getSecond().getActions())
                                        .anyMatch(a -> a.equalsIgnoreCase("Slash")));
            }
        }),
        ROCKFALL("Rockfall", "Mine", null, new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return PObjects.getAllObjectsWithDefs()
                        .stream()
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceToHypotenuse(PPlayer.location()) < 15)
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceTo(destinationDetails.getAssumed().toWorldPoint()) <= 1)
                        .filter(pair -> pair.getSecond().getName().equalsIgnoreCase("Rockfall"))
                        .anyMatch(
                                pair -> Arrays.stream(pair.getSecond().getActions())
                                        .filter(Objects::nonNull)
                                        .anyMatch(a -> a.equalsIgnoreCase("Mine")));
            }
        }),
        ROOTS("Roots", "Chop", null, new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return PObjects.getAllObjectsWithDefs()
                        .stream()
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceToHypotenuse(PPlayer.location()) < 15)
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceTo(destinationDetails.getAssumed().toWorldPoint()) <= 1)
                        .filter(pair -> pair.getSecond().getName().equalsIgnoreCase("Roots"))
                        .anyMatch(
                                pair -> Arrays.stream(pair.getSecond().getActions())
                                        .filter(Objects::nonNull)
                                        .anyMatch(a -> a.equalsIgnoreCase("Chop")));
            }
        }),
        ROCK_SLIDE("Rockslide", "Climb-over", null, new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return PObjects.getAllObjectsWithDefs()
                        .stream()
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceToHypotenuse(PPlayer.location()) < 15)
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceTo(destinationDetails.getAssumed().toWorldPoint()) <= 1)
                        .filter(pair -> pair.getSecond().getName().equalsIgnoreCase("Rockslide"))
                        .anyMatch(
                                pair -> Arrays.stream(pair.getSecond().getActions())
                                        .filter(Objects::nonNull)
                                        .anyMatch(a -> a.equalsIgnoreCase("Climb-over")));
            }
        }),
        ROOT("Root", "Step-over", null, new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return PObjects.getAllObjectsWithDefs()
                        .stream()
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceToHypotenuse(PPlayer.location()) < 15)
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceTo(destinationDetails.getAssumed().toWorldPoint()) <= 1)
                        .filter(pair -> pair.getSecond().getName().equalsIgnoreCase("Root"))
                        .anyMatch(
                                pair -> Arrays.stream(pair.getSecond().getActions())
                                        .filter(Objects::nonNull)
                                        .anyMatch(a -> a.equalsIgnoreCase("Step-over")));
            }
        }),
        BRIMHAVEN_VINES("Vines", "Chop-down", null, new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return PObjects.getAllObjectsWithDefs()
                        .stream()
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceToHypotenuse(PPlayer.location()) < 15)
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceTo(destinationDetails.getAssumed().toWorldPoint()) <= 1)
                        .filter(pair -> pair.getSecond().getName().equalsIgnoreCase("Vines"))
                        .anyMatch(
                                pair -> Arrays.stream(pair.getSecond().getActions())
                                .filter(Objects::nonNull)
                                .anyMatch(a -> a.equalsIgnoreCase("Chop-down")));
            }
        }),
        AVA_BOOKCASE ("Bookcase", "Search", new WorldPoint(3097, 3359, 0), new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return destinationDetails.getDestination().getX() >= 3097 && destinationDetails.getAssumed().toWorldPoint().equals(new WorldPoint(3097, 3359, 0));
            }
        }),
        AVA_LEVER ("Lever", "Pull", new WorldPoint(3096, 3357, 0), new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return destinationDetails.getDestination().getX() < 3097 && destinationDetails.getAssumed().toWorldPoint().equals(new WorldPoint(3097, 3359, 0));
            }
        }),
        ARDY_DOOR_LOCK_SIDE("Door", "Pick-lock", new WorldPoint(2565, 3356, 0), new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return PPlayer.location().getX() >= 2565 && PPlayer.location().distanceTo(new WorldPoint(2565, 3356, 0)) < 3;
            }
        }),
        ARDY_DOOR_UNLOCKED_SIDE("Door", "Open", new WorldPoint(2565, 3356, 0), new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return PPlayer.location().getX() < 2565 && PPlayer.location().distanceTo(new WorldPoint(2565, 3356, 0)) < 3;
            }
        }),
        YANILLE_DOOR_LOCK_SIDE("Door", "Pick-lock", new WorldPoint(2601, 9482, 0), new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return PPlayer.location().getY() <= 9481 && PPlayer.location().distanceTo(new WorldPoint(2601, 9482, 0)) < 3;
            }
        }),
        YANILLE_DOOR_UNLOCKED_SIDE("Door", "Open", new WorldPoint(2601, 9482, 0), new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return PPlayer.location().getY() > 9481 && PPlayer.location().distanceTo(new WorldPoint(2601, 9482, 0)) < 3;
            }
        }),
        EDGEVILLE_UNDERWALL_TUNNEL("Underwall tunnel", "Climb-into", new WorldPoint(3138, 3516, 0), new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return destinationDetails.getAssumed().toWorldPoint().equals(new WorldPoint(3138, 3516, 0));
            }
        }),
        VARROCK_UNDERWALL_TUNNEL("Underwall tunnel", "Climb-into", new WorldPoint(3141, 3513, 0), new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return destinationDetails.getAssumed().toWorldPoint().equals(new WorldPoint(3141, 3513, 0 ));
            }
        }),
        GAMES_ROOM_STAIRS("Stairs", "Climb-down", new WorldPoint(2899, 3565, 0), new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return destinationDetails.getDestination().getRSTile().toWorldPoint().equals(new WorldPoint(2899, 3565, 0)) &&
                        destinationDetails.getAssumed().toWorldPoint().equals(new WorldPoint(2205, 4934, 1));
            }
        }),
        CANIFIS_BASEMENT_WALL("Wall", "Search", new WorldPoint(3480, 9836, 0),new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return destinationDetails.getDestination().getRSTile().toWorldPoint().equals(new WorldPoint(3480, 9836, 0)) ||
                        destinationDetails.getAssumed().toWorldPoint().equals(new WorldPoint(3480, 9836, 0));
            }
        }),
        BRINE_RAT_CAVE_BOULDER("Cave", "Exit", new WorldPoint(2690, 10125, 0), new SpecialCondition() {
            @Override
            boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails) {
                return destinationDetails.getDestination().getRSTile().toWorldPoint().equals(new WorldPoint(2690, 10125, 0))
                        && new NPCQuery()
                        .nameEquals("Boulder")
                        .filter((npc) -> Arrays.asList(npc.getDefinition().getActions()).contains("Roll"))
                        .result(PUtils.getClient())
                        .size() > 0;

            }
        });

        private String name, action;
        private WorldPoint location;
        private SpecialCondition specialCondition;

        SpecialObject(String name, String action, WorldPoint location, SpecialCondition specialCondition){
            this.name = name;
            this.action = action;
            this.location = location;
            this.specialCondition = specialCondition;
        }

        public String getName() {
            return name;
        }

        public String getAction() {
            return action;
        }

        public WorldPoint getLocation() {
            return location;
        }

        public boolean isSpecialCondition(PathAnalyzer.DestinationDetails destinationDetails){
            return specialCondition.isSpecialLocation(destinationDetails);
        }

        public static SpecialObject getValidSpecialObjects(PathAnalyzer.DestinationDetails destinationDetails){
            for (SpecialObject object : values()){
                if (object.isSpecialCondition(destinationDetails)){
                    return object;
                }
            }
            return null;
        }

    }

    private abstract static class SpecialCondition {
        abstract boolean isSpecialLocation(PathAnalyzer.DestinationDetails destinationDetails);
    }

    public static boolean handle(PathAnalyzer.DestinationDetails destinationDetails, List<RSTile> path){
        RealTimeCollisionTile start = destinationDetails.getDestination(), end = destinationDetails.getNextTile();

        List<Pair<TileObject, ObjectDefinition>> interactiveObjects = null;

        String action = null;
        SpecialObject specialObject = SpecialObject.getValidSpecialObjects(destinationDetails);
        if (specialObject == null) {
            if ((interactiveObjects = getInteractiveObjects(start.getX(), start.getY(), start.getZ(), destinationDetails)).size() < 1 && end != null) {
                interactiveObjects = getInteractiveObjects(end.getX(), end.getY(), end.getZ(), destinationDetails);
            }
        } else {
            action = specialObject.getAction();
            Predicate<Pair<TileObject, ObjectDefinition>> specialObjectFilter = (Pair<TileObject, ObjectDefinition> objDefPair) -> {
                    ObjectDefinition def = objDefPair.getSecond();
                    if (def == null) return false;
                    return def.getName().equals(specialObject.getName()) &&
                            Arrays.asList(def.getActions()).contains(specialObject.getAction()) &&
                            objDefPair.getFirst().getWorldLocation().distanceToHypotenuse(specialObject.getLocation() != null ? specialObject.getLocation() : destinationDetails.getAssumed().toWorldPoint()) <= 1.5;
            };
            /*
            Filter<GameObject> specialObjectFilter = Filters.Objects.nameEquals(specialObject.getName())
                    .combine(Filters.Objects.actionsContains(specialObject.getAction()), true)
                    .combine(Filters.Objects.inArea(new RSArea(specialObject.getLocation() != null ? specialObject.getLocation() : destinationDetails.getAssumed(), 1)), true);


            interactiveObjects = Objects.findNearest(15, specialObjectFilter);
             */
            Client client = PUtils.getClient();
            interactiveObjects = PObjects.getAllObjectsWithDefs()
                    .stream()
                    .filter(pair -> pair.getFirst().getWorldLocation().distanceToHypotenuse(PPlayer.location()) <= 15)
                    .filter(specialObjectFilter)
                    .collect(Collectors.toList());
        }

        if (interactiveObjects.size() == 0) {
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder("Sort Order: ");
        interactiveObjects.forEach(objDefPair -> stringBuilder.append(objDefPair.getSecond().getName()).append(" ").append(
                Arrays.asList(objDefPair.getSecond().getActions())).append(", "));
        log.info(stringBuilder.toString());

        return handle(path, interactiveObjects.get(0), destinationDetails, action, specialObject);
    }

    private static boolean handle(List<RSTile> path, Pair<TileObject, ObjectDefinition> objDefPair, PathAnalyzer.DestinationDetails destinationDetails, String action, SpecialObject specialObject){
        PathAnalyzer.DestinationDetails current = PathAnalyzer.furthestReachableTile(path);

        if (current == null){
            return false;
        }

        RealTimeCollisionTile currentFurthest = current.getDestination();

        // TODO:
        /* Perhaps unnecessary to check in oprs
        if (!PPlayer.isMoving() && !object.isClickable()){
            if (!WalkerEngine.getInstance().clickMinimap(destinationDetails.getDestination())){
                return false;
            }
        }

        if (WaitFor.condition(General.random(5000, 8000), () -> object.isOnScreen() && object.isClickable() ? WaitFor.Return.SUCCESS : WaitFor.Return.IGNORE) != WaitFor.Return.SUCCESS) {
            return false;
        }
        */

        boolean successfulClick = false;

        if (specialObject != null) {
            log.info("Detected Special Object: " + specialObject);
            Client client = PUtils.getClient();
            switch (specialObject){
                case WEB:
                    List<Pair<TileObject, ObjectDefinition>> webs = PObjects.getAllObjectsWithDefs()
                            .stream()
                            .filter(pair -> pair.getFirst().getWorldLocation().equals(objDefPair.getFirst().getWorldLocation()))
                            .filter(pair -> Arrays.asList(pair.getSecond().getActions())
                                    .stream()
                                    .anyMatch(a -> a.equalsIgnoreCase("Slash")))
                            .collect(Collectors.toList());
                    int iterations = 0;
                    while (webs.size() > 0){
                        TileObject web = webs.get(0).getFirst();
                        if (canLeftclickWeb()) {
                            InteractionHelper.click(web, "Slash");
                        } else {
                            useBladeOnWeb(web);
                        }

                        /*
                        if(Game.isUptext("->")){
                            Walking.blindWalkTo(PPlayer.location());
                        }
                         */

                        if (web.getWorldLocation().distanceTo(PPlayer.location()) <= 1) {
                            WaitFor.milliseconds((int)PUtils.randomNormal(50, 800, 250, 150));
                        } else {
                            WaitFor.milliseconds(2000, 4000);
                        }
                        webs = PObjects.getAllObjectsWithDefs()
                                .stream()
                                .filter(pair -> pair.getFirst().getWorldLocation().equals(objDefPair.getFirst().getWorldLocation()))
                                .filter(pair -> Arrays.stream(pair.getSecond().getActions())
                                        .anyMatch(a -> a.equalsIgnoreCase("Slash")))
                                .collect(Collectors.toList());
                        if (Reachable.getMap().getParent(destinationDetails.getAssumedX(), destinationDetails.getAssumedY()) != null && (webs == null || webs.size() == 0) ){
                            successfulClick = true;
                            break;
                        }
                        if (iterations++ > 5){
                            break;
                        }
                    }
                    break;
                case ARDY_DOOR_LOCK_SIDE:
                case YANILLE_DOOR_LOCK_SIDE:
                    for (int i = 0; i < PUtils.random(15, 25); i++) {
                        if (!clickOnObject(objDefPair, specialObject.getAction())){
                            continue;
                        }
                        if (PPlayer.location().distanceTo(specialObject.getLocation()) > 1){
                            WaitFor.condition(PUtils.random(3000, 4000), () -> PPlayer.location().distanceTo(specialObject.getLocation()) <= 1 ? WaitFor.Return.SUCCESS : WaitFor.Return.IGNORE);
                        }
                        if (PPlayer.location().equals(new WorldPoint(2564, 3356, 0))){
                            successfulClick = true;
                            break;
                        }
                    }
                    break;
                case VARROCK_UNDERWALL_TUNNEL:
                    if(!clickOnObject(objDefPair,specialObject.getAction())){
                        return false;
                    }
                    successfulClick = true;
                    WaitFor.condition(10000, () ->
                            SpecialObject.EDGEVILLE_UNDERWALL_TUNNEL.getLocation().equals(PPlayer.location()) ?
                                    WaitFor.Return.SUCCESS : WaitFor.Return.IGNORE);
                    break;
                case EDGEVILLE_UNDERWALL_TUNNEL:
                    if(!clickOnObject(objDefPair,specialObject.getAction())){
                        return false;
                    }
                    successfulClick = true;
                    WaitFor.condition(10000, () ->
                            SpecialObject.VARROCK_UNDERWALL_TUNNEL.getLocation().equals(PPlayer.location()) ?
                                    WaitFor.Return.SUCCESS : WaitFor.Return.IGNORE);
                    break;
                case BRINE_RAT_CAVE_BOULDER:
                    NPC boulder = new NPCQuery()
                            .nameEquals("Boulder")
                            .filter((npc) -> Arrays.asList(npc.getDefinition().getActions()).contains("Roll"))
                            .result(client)
                            .first();
                    if (boulder == null) return false;
                    if(InteractionHelper.click(boulder, "Roll")){
                        if(WaitFor.condition(12000,
                                () -> new NPCQuery()
                                        .nameEquals("Boulder")
                                        .filter((npc) -> Arrays.asList(npc.getDefinition().getActions()).contains("Roll"))
                                        .result(client)
                                        .size() > 0 ?
                                        WaitFor.Return.SUCCESS : WaitFor.Return.IGNORE) == WaitFor.Return.SUCCESS){
                            WaitFor.milliseconds(3500, 6000);
                        }
                    }
                    break;
            }
        }

        if (!successfulClick){
            String[] validOptions = action != null ? new String[]{action} : getViableOption(
                    Arrays.stream(objDefPair.getSecond().getActions()).filter(Objects::nonNull).filter(getInstance().sortedOptions::contains).collect(
                            Collectors.toList()), destinationDetails);
            if (!clickOnObject(objDefPair, validOptions)) {
                return false;
            }
        }

        boolean strongholdDoor = isStrongholdDoor(objDefPair);

        if (strongholdDoor){
            if (WaitFor.condition(PUtils.random(6700, 7800), () -> {
                WorldPoint playerPosition = PPlayer.location();
                if (BFS.isReachable(RealTimeCollisionTile.get(playerPosition.getX(), playerPosition.getY(), playerPosition.getPlane()), destinationDetails.getNextTile(), 50)) {
                    WaitFor.milliseconds(500, 1000);
                    return WaitFor.Return.SUCCESS;
                }
                if (NPCInteraction.isConversationWindowUp()) {
                    handleStrongholdQuestions();
                    return WaitFor.Return.SUCCESS;
                }
                return WaitFor.Return.IGNORE;
            }) != WaitFor.Return.SUCCESS){
                return false;
            }
        }

        WaitFor.condition(PUtils.random(8500, 11000), () -> {
            DoomsToggle.handleToggle();
            PathAnalyzer.DestinationDetails destinationDetails1 = PathAnalyzer.furthestReachableTile(path);
            if (NPCInteraction.isConversationWindowUp()) {
                NPCInteraction.handleConversation(NPCInteraction.GENERAL_RESPONSES);
            }
            if (destinationDetails1 != null) {
                if (!destinationDetails1.getDestination().equals(currentFurthest)){
                    return WaitFor.Return.SUCCESS;
                }
            }
            return WaitFor.Return.IGNORE;
        });
        if (strongholdDoor){
            WaitFor.milliseconds(800, 1200);
        }
        return true;
    }

    public static List<Pair<TileObject, ObjectDefinition>> getInteractiveObjects(int x, int y, int z, PathAnalyzer.DestinationDetails destinationDetails){
        Client client = PUtils.getClient();
        List<Pair<TileObject, ObjectDefinition>> objects = PObjects.getAllObjectsWithDefs()
                .stream()
                .filter(pair -> pair.getFirst().getWorldLocation().distanceToHypotenuse(PPlayer.location()) <= 25)
                .filter(interactiveObjectFilter(x, y, z, destinationDetails))
                .collect(Collectors.toList());

        final WorldPoint base = new WorldPoint(x, y, z);
        objects.sort((o1, o2) -> {
            int c = Integer.compare(
                    Math.round(o1.getFirst().getWorldLocation().distanceToHypotenuse(base)),
                    Math.round(o2.getFirst().getWorldLocation().distanceToHypotenuse(base))
            );
            int assumedZ = destinationDetails.getAssumedZ(), destinationZ = destinationDetails.getDestination().getZ();
            List<String> actions1 = Arrays.asList(o1.getSecond().getActions());
            List<String> actions2 = Arrays.asList(o2.getSecond().getActions());

            if (assumedZ > destinationZ){
                if (actions1.contains("Climb-up")){
                    return -1;
                }
                if (actions2.contains("Climb-up")){
                    return 1;
                }
            } else if (assumedZ < destinationZ){
                if (actions1.contains("Climb-down")){
                    return -1;
                }
                if (actions2.contains("Climb-down")){
                    return 1;
                }
            } else if(destinationDetails.getAssumed().distanceTo(destinationDetails.getDestination().getRSTile()) >= 20){
                if(actions1.contains("Climb-up") || actions1.contains("Climb-down")){
                    return -1;
                } else if(actions2.contains("Climb-up") || actions2.contains("Climb-down")){
                    return 1;
                }
            } else if(actions1.contains("Climb-up") || actions1.contains("Climb-down")){
                return 1;
            } else if(actions2.contains("Climb-up") || actions2.contains("Climb-down")){
                return -1;
            }
            return c;
        });
        StringBuilder a = new StringBuilder("Detected: ");
        objects.forEach(object -> a.append(object.getSecond().getName()).append(" "));
        log.info(a.toString());

        return objects;
    }

    /**
     * Filter that accepts only interactive objects to progress in path.
     *
     * @param x
     * @param y
     * @param z
     * @param destinationDetails context where destination is at
     * @return
     */
    private static Predicate<Pair<TileObject, ObjectDefinition>> interactiveObjectFilter(int x, int y, int z, PathAnalyzer.DestinationDetails destinationDetails){
        final WorldPoint position = new WorldPoint(x, y, z);
        return (Pair<TileObject, ObjectDefinition> pair) -> {
            ObjectDefinition def = pair.getSecond();
            if (def == null){
                return false;
            }
            String name = def.getName();
            if (getInstance().sortedBlackList.contains(name)) {
                return false;
            }
            List<String> actions = Arrays.asList(def.getActions());
            if (actions.stream().filter(Objects::nonNull).anyMatch(s -> getInstance().sortedBlackListOptions.contains(s))){
                return false;
            }
            if (pair.getFirst().getWorldLocation().distanceToHypotenuse(destinationDetails.getDestination().getRSTile().toWorldPoint()) > 5) {
                return false;
            }

            /* TODO:
            if (Arrays.stream(obj.getAllTiles()).noneMatch(rsTile -> rsTile.distanceTo(position) <= 2)) {
                return false;
            }
            */

            List<String> options = Arrays.asList(def.getActions());
            return options.stream().filter(Objects::nonNull).anyMatch(getInstance().sortedOptions::contains);

        };
    }

    private static String[] getViableOption(Collection<String> collection, PathAnalyzer.DestinationDetails destinationDetails){
        Set<String> set = new HashSet<>(collection);
        if (set.retainAll(getInstance().sortedHighPriorityOptions) && set.size() > 0){
            return set.toArray(new String[set.size()]);
        }
        if (destinationDetails.getAssumedZ() > destinationDetails.getDestination().getZ()){
            if (collection.contains("Climb-up")){
                return new String[]{"Climb-up"};
            }
        }
        if (destinationDetails.getAssumedZ() < destinationDetails.getDestination().getZ()){
            if (collection.contains("Climb-down")){
                return new String[]{"Climb-down"};
            }
        }
        if (destinationDetails.getAssumedY() > 5000 && destinationDetails.getDestination().getZ() == 0 && destinationDetails.getAssumedZ() == 0){
            if (collection.contains("Climb-down")){
                return new String[]{"Climb-down"};
            }
        }
        String[] options = new String[collection.size()];
        collection.toArray(options);
        return options;
    }

    private static boolean clickOnObject(Pair<TileObject, ObjectDefinition> objDefPair, String... options){
        boolean result;

        if (isClosedTrapDoor(objDefPair, options)){
            result = handleTrapDoor(objDefPair.getFirst());
        } else {
            result = InteractionHelper.click(objDefPair.getFirst(), options);
            log.info("Interacting with (" +  objDefPair.getSecond().getName() + ") at " + objDefPair.getFirst().getWorldLocation() + " with options: " + Arrays.toString(options) + " " + (result ? "SUCCESS" : "FAIL"));
            WaitFor.milliseconds(250,800);
        }

        return result;
    }

    private static boolean isStrongholdDoor(Pair<TileObject, ObjectDefinition> object){
        List<String> doorNames = Arrays.asList("Gate of War", "Rickety door", "Oozing barrier", "Portal of Death");
        return  doorNames.contains( object.getSecond().getName());
    }



    private static void handleStrongholdQuestions() {
        NPCInteraction.handleConversation("Use the Account Recovery System.",
                "No, you should never buy an account.",
                "Nobody.",
                "Don't tell them anything and click the 'Report Abuse' button.",
                "Decline the offer and report that player.",
                "Me.",
                "Only on the RuneScape website.",
                "Report the incident and do not click any links.",
                "Authenticator and two-step login on my registered email.",
                "No way! You'll just take my gold for your own! Reported!",
                "No.",
                "Don't give them the information and send an 'Abuse Report'.",
                "Don't give them my password.",
                "The birthday of a famous person or event.",
                "Through account settings on runescape.com.",
                "Secure my device and reset my RuneScape password.",
                "Report the player for phishing.",
                "Don't click any links, forward the email to reportphishing@jagex.com.",
                "Inform Jagex by emailing reportphishing@jagex.com.",
                "Don't give out your password to anyone. Not even close friends.",
                "Politely tell them no and then use the 'Report Abuse' button.",
                "Set up 2 step authentication with my email provider.",
                "No, you should never buy a RuneScape account.",
                "Do not visit the website and report the player who messaged you.",
                "Only on the RuneScape website.",
                "Don't type in my password backwards and report the player.",
                "Virus scan my device then change my password.",
                "No, you should never allow anyone to level your account.",
                "Don't give out your password to anyone. Not even close friends.",
                "Report the stream as a scam. Real Jagex streams have a 'verified' mark.",
                "Report the stream as a scam. Real Jagex streams have a 'verified' mark",
                "Read the text and follow the advice given.",
                "No way! I'm reporting you to Jagex!",
                "Talk to any banker in RuneScape.",
                "Secure my device and reset my RuneScape password.",
                "Secure my device and reset my password.",
                "Delete it - it's a fake!",
                "Use the account management section on the website.",
                "Politely tell them no and then use the 'Report Abuse' button.",
                "Through account setting on oldschool.runescape.com",
                "Through account setting on oldschool.runescape.com.",
                "Nothing, it's a fake.",
                "Only on the Old School RuneScape website.",
                "Don't share your information and report the player.");
    }


    private static boolean isClosedTrapDoor(Pair<TileObject, ObjectDefinition> object, String[] options){
        return  (object.getSecond().getName().equals("Trapdoor") && Arrays.asList(options).contains("Open"));
    }

    private static boolean handleTrapDoor(TileObject object){
        Client client = PUtils.getClient();
        if (getActions(object).contains("Open")){
            if (!InteractionHelper.click(object, "Open", () -> {
                var result = PObjects.getAllObjectsWithDefs().stream()
                        .filter(
                                pair -> Arrays.asList(pair.getSecond().getActions())
                                        .stream()
                                        .filter(Objects::nonNull)
                                        .anyMatch(a -> a.equalsIgnoreCase("Climb-down"))
                        )
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceToHypotenuse(object.getWorldLocation()) <= 2)
                        .collect(Collectors.toList());

                if (result.size() > 0){
                    return WaitFor.Return.SUCCESS;
                }
                return WaitFor.Return.IGNORE;
            })){
                return false;
            } else {
                var result = PObjects.getAllObjectsWithDefs().stream()
                        .filter(
                                pair -> Arrays.asList(pair.getSecond().getActions())
                                        .stream()
                                        .filter(Objects::nonNull)
                                        .anyMatch(a -> a.equalsIgnoreCase("Climb-down"))
                        )
                        .filter(pair -> pair.getFirst().getWorldLocation().distanceToHypotenuse(object.getWorldLocation()) <= 2)
                        .collect(Collectors.toList());
                return result.size() > 0 && handleTrapDoor(result.get(0).getFirst());
            }
        }
        log.info("Interacting with (" + PObjects.getObjectDef(object).getName() + ") at " + object.getWorldLocation() + " with option: Climb-down");
        return InteractionHelper.click(object, "Climb-down");
    }

    public static List<String> getActions(TileObject object){
        List<String> list = new ArrayList<>();
        if (object == null){
            return list;
        }
        ObjectDefinition objectDefinition = PObjects.getObjectDef(object);
        if (objectDefinition == null){
            return list;
        }
        String[] actions = objectDefinition.getActions();
        if (actions == null){
            return list;
        }
        return Arrays.asList(actions);
    }

    private static List<Integer> SLASH_WEAPONS = new ArrayList<>(Arrays.asList(1,4,9,10,12,17,20,21));

    private static boolean canLeftclickWeb(){
        RSVarBit weaponType = RSVarBit.get(357);
        boolean haveKnife = PInventory.getAllItemsWithDefs()
                .stream()
                .filter(pair -> pair.getSecond().getName().contains("Knife"))
                .collect(Collectors.toList())
                .size() > 0;
        return (weaponType != null && SLASH_WEAPONS.contains(weaponType.getValue())) || haveKnife;
    }

    private static boolean useBladeOnWeb(TileObject web){
        List<String> slashItemNames = Arrays.asList("whip", "sword", "dagger", "claws", "scimitar", " axe", "knife", "halberd", "machete", "rapier");
        var slashItems = PInventory.getAllItemsWithDefs()
                .stream()
                .filter(pair -> slashItemNames.stream().anyMatch(slashItemName -> pair.getSecond().getName().contains(slashItemName)))
                .collect(Collectors.toList());
        if(slashItems == null || slashItems.size() == 0) return false;
        return InteractionHelper.useItemOnObject(slashItems.get(0).getFirst(), web);
    }

}