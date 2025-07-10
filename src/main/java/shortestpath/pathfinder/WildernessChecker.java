package shortestpath.pathfinder;

import java.util.Set;

import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import shortestpath.WorldPointUtil;

public class WildernessChecker {

    private static final WorldArea WILDERNESS_ABOVE_GROUND = new WorldArea(2944, 3525, 448, 448, 0);
    private static final WorldArea WILDERNESS_ABOVE_GROUND_LEVEL_20 = new WorldArea(2944, 3680, 448, 448, 0);
    private static final WorldArea WILDERNESS_ABOVE_GROUND_LEVEL_30 = new WorldArea(2944, 3760, 448, 448, 0);
    private static final WorldArea WILDERNESS_UNDERGROUND = new WorldArea(2944, 9918, 320, 442, 0);
    private static final WorldArea WILDERNESS_UNDERGROUND_LEVEL_20 = new WorldArea(2944, 10075, 320, 442, 0);
    private static final WorldArea WILDERNESS_UNDERGROUND_LEVEL_30 = new WorldArea(2944, 10155, 320, 442, 0);
    private static final WorldArea FEROX_ENCLAVE_1 = new WorldArea(3123, 3622, 2, 10, 0);
    private static final WorldArea FEROX_ENCLAVE_2 = new WorldArea(3125, 3617, 16, 23, 0);
    private static final WorldArea FEROX_ENCLAVE_3 = new WorldArea(3138, 3636, 18, 10, 0);
    private static final WorldArea FEROX_ENCLAVE_4 = new WorldArea(3141, 3625, 14, 11, 0);
    private static final WorldArea FEROX_ENCLAVE_5 = new WorldArea(3141, 3619, 7, 6, 0);
    private static final WorldArea NOT_WILDERNESS_1 = new WorldArea(2997, 3525, 34, 9, 0);
    private static final WorldArea NOT_WILDERNESS_2 = new WorldArea(3005, 3534, 21, 10, 0);
    private static final WorldArea NOT_WILDERNESS_3 = new WorldArea(3000, 3534, 5, 5, 0);
    private static final WorldArea NOT_WILDERNESS_4 = new WorldArea(3031, 3525, 2, 2, 0);

    public static boolean isInWilderness(WorldPoint p) {
        return WILDERNESS_ABOVE_GROUND.distanceTo(p) == 0
                && FEROX_ENCLAVE_1.distanceTo(p) != 0
                && FEROX_ENCLAVE_2.distanceTo(p) != 0
                && FEROX_ENCLAVE_3.distanceTo(p) != 0
                && FEROX_ENCLAVE_4.distanceTo(p) != 0
                && FEROX_ENCLAVE_5.distanceTo(p) != 0
                && NOT_WILDERNESS_1.distanceTo(p) != 0
                && NOT_WILDERNESS_2.distanceTo(p) != 0
                && NOT_WILDERNESS_3.distanceTo(p) != 0
                && NOT_WILDERNESS_4.distanceTo(p) != 0
                || WILDERNESS_UNDERGROUND.distanceTo(p) == 0;
    }

    public static boolean isInWilderness(int packedPoint) {
        WorldPoint p = WorldPointUtil.unpackWorldPoint(packedPoint);
        return isInWilderness(p);
    }

    public static boolean isInWilderness(Set<Integer> packedPoints) {
        for (int packedPoint : packedPoints) {
            if (isInWilderness(packedPoint)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInLevel20Wilderness(int packedPoint) {
        return WorldPointUtil.distanceToArea(packedPoint, WILDERNESS_ABOVE_GROUND_LEVEL_20) == 0
                || WorldPointUtil.distanceToArea(packedPoint, WILDERNESS_UNDERGROUND_LEVEL_20) == 0;
    }

    public static boolean isInLevel30Wilderness(int packedPoint) {
        return WorldPointUtil.distanceToArea(packedPoint, WILDERNESS_ABOVE_GROUND_LEVEL_30) == 0
                || WorldPointUtil.distanceToArea(packedPoint, WILDERNESS_UNDERGROUND_LEVEL_30) == 0;

    }

}
