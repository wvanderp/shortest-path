package net.runelite.cache.region;

/**
 * Inferred location/scene object type ids used by the map dumper and region loader.
 *
 * <p>These ids come from the location attributes packed in map data (see
 * {@code LocationsLoader}: {@code type = attributes >> 2}). Names are chosen based
 * on how {@code MapImageDumper} renders them (walls, pillars, diagonal walls,
 * map decorations). For ids not explicitly drawn by the dumper, placeholders are
 * provided to make ranges and semantics obvious.</p>
 */
public enum LocationType
{
    // Boundary/walls (drawn as lines or points)
    WALL_STRAIGHT(0),
    WALL_DECORATION(1),
    WALL_CORNER(2),
    WALL_PILLAR(3),

    // Additional wall decoration variants (not explicitly rendered by MapImageDumper)
    WALL_DECORATION_VAR_4(4),
    WALL_DECORATION_VAR_5(5),
    WALL_DECORATION_VAR_6(6),
    WALL_DECORATION_VAR_7(7),
    WALL_DECORATION_VAR_8(8),

    // Diagonal wall/boundary
    DIAGONAL_WALL(9),

    // Game object families (icon-capable when mapSceneId != -1)
    TYPE_10(10),
    TYPE_11(11),
    TYPE_12(12),
    TYPE_13(13),
    TYPE_14(14),
    TYPE_15(15),
    TYPE_16(16),
    TYPE_17(17),
    TYPE_18(18),
    TYPE_19(19),
    TYPE_20(20),
    TYPE_21(21),

    // Floor/ground decoration (large)
    MAP_DECOR_LARGE(22);

    private final int id;

    LocationType(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    /**
     * Resolve enum by id, or null if unknown.
     */
    public static LocationType of(int id)
    {
        for (LocationType t : values())
        {
            if (t.id == id)
            {
                return t;
            }
        }
        return null;
    }

    /**
     * True for straight/corner/pillar boundary walls (0..3).
     */
    public static boolean isWallFamily(int id)
    {
        return id >= WALL_STRAIGHT.id && id <= WALL_PILLAR.id;
    }

    /**
     * True for diagonal boundary (9).
     */
    public static boolean isDiagonal(int id)
    {
        return id == DIAGONAL_WALL.id;
    }

    /**
     * True for any wall-attached decoration variants (1, 4..8).
     */
    public static boolean isWallDecorationFamily(int id)
    {
        return id == WALL_DECORATION.id || (id >= WALL_DECORATION_VAR_4.id && id <= WALL_DECORATION_VAR_8.id);
    }

    /**
     * True for generic game-object placement types (10..21).
     */
    public static boolean isGameObjectFamily(int id)
    {
        return id >= TYPE_10.id && id <= TYPE_21.id;
    }

    /**
     * True for ground/floor decorations (22).
     */
    public static boolean isGroundDecoration(int id)
    {
        return id == MAP_DECOR_LARGE.id;
    }

    /**
     * Convenience: true for any boundary (straight/corner/pillar) or diagonal.
     */
    public static boolean isAnyWall(int id)
    {
        return isWallFamily(id) || isDiagonal(id);
    }

    /**
     * Broad map icon candidate set (game objects 10..21 or ground decor 22).
     * MapImageDumper currently checks a subset; this captures the general intent.
     */
    public static boolean isMapIconCandidate(int id)
    {
        return isGameObjectFamily(id) || isGroundDecoration(id);
    }
}
