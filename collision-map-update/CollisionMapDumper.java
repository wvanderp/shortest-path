/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * Copyright (c) 2022, Skretzo <https://github.com/Skretzo>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import net.runelite.cache.definitions.ObjectDefinition;
import net.runelite.cache.fs.Store;
import net.runelite.cache.region.Location;
import net.runelite.cache.region.Position;
import net.runelite.cache.region.Region;
import net.runelite.cache.region.RegionLoader;
import net.runelite.cache.util.KeyProvider;
import net.runelite.cache.util.XteaKeyManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Inferred location/scene object type ids used by the map dumper and region loader.
 *
 * <p>These ids come from the location attributes packed in map data (see
 * {@code LocationsLoader}: {@code type = attributes >> 2}). Names are chosen based
 * on how {@code MapImageDumper} renders them (walls, pillars, diagonal walls,
 * map decorations). For ids not explicitly drawn by the dumper, placeholders are
 * provided to make ranges and semantics obvious.</p>
 */
enum LocationType
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


/**
 * Collision map dumper
 *
 * <p>
 * Tool to export a compact representation of region collision data from a
 * RuneLite/OpenRS2 cache. The output is a BitSet-backed byte array per region
 * which encodes walkable/blocking flags for tile edges and planes. These files
 * are useful for offline analysis or third-party navigation tools that don't
 * need to load the full client.
 * </p>
 *
 * <p>
 * Inputs and usage:
 * Cache and XTEA keys can be downloaded from https://archive.openrs2.org/caches
 * (replace "mapsquare" with "region" and "key" with "keys"). Build with
 * Maven and run the class with the --cachedir, --xteapath and --outputdir
 * arguments as described in the original README.
 * </p>
 */
public class CollisionMapDumper {
	private final RegionLoader regionLoader;
	private final ObjectManager objectManager;

	public CollisionMapDumper(Store store, KeyProvider keyProvider) {
		this(store, new RegionLoader(store, keyProvider));
	}

	/**
	 * Construct a CollisionMapDumper using a store and a key provider.
	 *
	 * @param store       the cache store
	 * @param keyProvider provider for XTEA region keys
	 */

	public CollisionMapDumper(Store store, RegionLoader regionLoader) {
		this.regionLoader = regionLoader;
		this.objectManager = new ObjectManager(store);
	}

	/**
	 * Load required data from the cache: object definitions and regions.
	 *
	 * @return this (for chaining)
	 * @throws IOException on I/O problems while reading cache data
	 */

	public CollisionMapDumper load() throws IOException {
		objectManager.load();
		regionLoader.loadRegions();
		regionLoader.calculateBounds();

		return this;
	}

	/**
	 * Find an object definition by id.
	 *
	 * @param id the object id
	 * @return the object definition, or null if not found
	 */
	private ObjectDefinition findObject(int id) {
		return objectManager.getObject(id);
	}

	/**
	 * Command-line entrypoint. Requires three options:
	 * --cachedir (path to cache), --xteapath (JSON file with XTEA keys),
	 * and --outputdir (destination directory).
	 */
	public static void main(String[] args) throws IOException {
		Options options = new Options();
		options.addOption(Option.builder().longOpt("cachedir").hasArg().required().build());
		options.addOption(Option.builder().longOpt("xteapath").hasArg().required().build());
		options.addOption(Option.builder().longOpt("outputdir").hasArg().required().build());

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException ex) {
			System.err.println("Error parsing command line options: " + ex.getMessage());
			System.exit(-1);
			return;
		}

		final String cacheDirectory = cmd.getOptionValue("cachedir");
		final String xteaJSONPath = cmd.getOptionValue("xteapath");
		final String outputDirectory = cmd.getOptionValue("outputdir");

		XteaKeyManager xteaKeyManager = new XteaKeyManager();
		try (FileInputStream fin = new FileInputStream(xteaJSONPath)) {
			xteaKeyManager.loadKeys(fin);
		}

		File base = new File(cacheDirectory);
		File outDir = new File(outputDirectory);
		outDir.mkdirs();

		try (Store store = new Store(base)) {
			store.load();

			CollisionMapDumper dumper = new CollisionMapDumper(store, xteaKeyManager);
			dumper.load();

			Collection<Region> regions = dumper.regionLoader.getRegions();

			int n = 0;
			int total = regions.size();

			for (Region region : regions) {
				dumper.makeCollisionMap(region, outputDirectory, ++n, total);
			}
		}
	}

	private void makeCollisionMap(Region region, String outputDirectory, int n, int total) {
		int baseX = region.getBaseX();
		int baseY = region.getBaseY();

		// Create a FlagMap that covers the full coordinate range of this
		// region. FlagMap stores a small set of boolean flags per tile per
		// z-plane indicating whether movement across the north/south or
		// east/west edge of the tile is allowed.
		FlagMap flagMap = new FlagMap(baseX, baseY, baseX + Region.X - 1, baseY + Region.Y - 1);

		// Populate flags for this region and the eight neighbouring regions
		// so that edges on region boundaries are correctly represented.
		addCollisions(flagMap, region);
		addNeighborCollisions(flagMap, region, -1, -1);
		addNeighborCollisions(flagMap, region, -1, 0);
		addNeighborCollisions(flagMap, region, -1, 1);
		addNeighborCollisions(flagMap, region, 0, -1);
		addNeighborCollisions(flagMap, region, 0, 1);
		addNeighborCollisions(flagMap, region, 1, -1);
		addNeighborCollisions(flagMap, region, 1, 0);
		addNeighborCollisions(flagMap, region, 1, 1);

		String name = region.getRegionX() + "_" + region.getRegionY();

		byte[] buf = flagMap.toBytes();
		if (buf.length > 0) {
			try (FileOutputStream out = new FileOutputStream(outputDirectory + "/" + name)) {
				out.write(buf, 0, buf.length);
				System.out.println("Exporting region " + name + " (" + n + " / " + total + ")");
			} catch (IOException e) {
				System.out.println("Unable to write compressed output bytes for " + name + ". " + e);
			}
		}
	}

	private void addNeighborCollisions(FlagMap flagMap, Region region, int dx, int dy) {
		Region neighbor = regionLoader.findRegionForRegionCoordinates(region.getRegionX() + dx,
				region.getRegionY() + dy);
		if (neighbor == null) {
			return;
		}
		addCollisions(flagMap, neighbor);
	}

	private void addCollisions(FlagMap flagMap, Region region) {
		int baseX = region.getBaseX();
		int baseY = region.getBaseY();

		for (int z = 0; z < Region.Z; z++) {
			for (int localX = 0; localX < Region.X; localX++) {
				int regionX = baseX + localX;
				for (int localY = 0; localY < Region.Y; localY++) {
					int regionY = baseY + localY;

					// Check whether the local tile is a bridge tile. Bridge tiles are
					// represented in the client by a setting bit which indicates the
					// visible floor is on the plane above the base plane. When a
					// bridge is present we should compare object positions against the
					// bridge plane (z + 1) instead of the base plane.
					boolean isBridge = (region.getTileSetting(1, localX, localY) & 2) != 0;
					int tileZ = z + (isBridge ? 1 : 0);

					for (Location loc : region.getLocations()) {
						Position pos = loc.getPosition();
						if (pos.getX() != regionX || pos.getY() != regionY || pos.getZ() != tileZ) {
							continue;
						}

						// Default to blocked until proven otherwise. Some objects have
						// overrides listed in the Exclusion enum (for example doors that
						// should be treated as permanently blocking). matches() returns
						// a Boolean when an override exists, or null otherwise.
						boolean tile = FlagMap.TILE_BLOCKED;
						Boolean exclusion = Exclusion.matches(loc.getId());

						// Monitoring / debug for specific object IDs causing unexpected blocking
						final int WATCH_ID_1 = 31366;
						final int WATCH_ID_2 = 31310;
						final int WATCH_ID_3 = 11591;
						final int WATCH_ID_4 = 2639;
						final boolean watch = loc.getId() == WATCH_ID_1 || loc.getId() == WATCH_ID_2 || loc.getId() == WATCH_ID_3 || loc.getId() == WATCH_ID_4;
						if (watch) {
							System.out.println("[CollisionMapDumper] WATCH OBJECT id=" + loc.getId() + " region=(" + region.getRegionX() + "," + region.getRegionY() + ") base=(" + baseX + "," + baseY + ") tile=(" + regionX + "," + regionY + ",z=" + z + ") tileZ=" + tileZ);
							System.out.println("  pos=" + pos + " type=" + loc.getType() + " orient=" + loc.getOrientation() + " exclusion=" + exclusion);
						}

						int X = loc.getPosition().getX();
						int Y = loc.getPosition().getY();
						int Z = z;

						int type = loc.getType();
						int orientation = loc.getOrientation();

						ObjectDefinition object = findObject(loc.getId());

						// The object's bounding size in tiles depends on its orientation;
						// orientations 1 and 3 swap the X/Y dimensions.
						int sizeX = (orientation == 1 || orientation == 3) ? object.getSizeY() : object.getSizeX();
						int sizeY = (orientation == 1 || orientation == 3) ? object.getSizeX() : object.getSizeY();

						// Walls
						if (LocationType.isWallFamily(type)) {
							Z = z != tileZ ? z : loc.getPosition().getZ();

							if (object.getMapSceneID() != -1) {
								if (exclusion != null) {
									tile = exclusion;
								} else if (object.getInteractType() == 0) {
									continue;
								}
								for (int sx = 0; sx < sizeX; sx++) {
									for (int sy = 0; sy < sizeY; sy++) {
										if (watch) System.out.println("[CollisionMapDumper] WRITE wall id=" + loc.getId() + " at=(" + (X + sx) + "," + (Y + sy) + "," + Z + ") north=" + tile + " east=" + tile);
										flagMap.set(X + sx, Y + sy, Z, FlagMap.FLAG_NORTH, tile);
										flagMap.set(X + sx, Y + sy, Z, FlagMap.FLAG_EAST, tile);
										if (watch) System.out.println("[CollisionMapDumper] WRITE wall id=" + loc.getId() + " at=(" + (X + sx) + "," + (Y + sy - 1) + "," + Z + ") north=" + tile);
										flagMap.set(X + sx, Y + sy - 1, Z, FlagMap.FLAG_NORTH, tile);
										if (watch) System.out.println("[CollisionMapDumper] WRITE wall id=" + loc.getId() + " at=(" + (X + sx - 1) + "," + (Y + sy) + "," + Z + ") east=" + tile);
										flagMap.set(X + sx - 1, Y + sy, Z, FlagMap.FLAG_EAST, tile);
									}
								}
							} else {
								boolean door = object.getWallOrDoor() != 0;
								boolean doorway = !door && object.getInteractType() == 0 && type == 0;
								tile = door ? FlagMap.TILE_DEFAULT : FlagMap.TILE_BLOCKED;
								if (exclusion != null) {
									tile = exclusion;
								} else if (doorway) {
									continue;
								}

								if (type == 0 || type == 2) {
									if (orientation == 0) // wall on west
									{
										if (watch) System.out.println("[CollisionMapDumper] WRITE wall edge id=" + loc.getId() + " at=(" + (X - 1) + "," + Y + "," + Z + ") west=" + tile);
										flagMap.set(X - 1, Y, Z, FlagMap.FLAG_WEST, tile);
									} else if (orientation == 1) // wall on north
									{
										if (watch) System.out.println("[CollisionMapDumper] WRITE wall edge id=" + loc.getId() + " at=(" + X + "," + Y + "," + Z + ") north=" + tile);
										flagMap.set(X, Y, Z, FlagMap.FLAG_NORTH, tile);
									} else if (orientation == 2) // wall on east
									{
										if (watch) System.out.println("[CollisionMapDumper] WRITE wall edge id=" + loc.getId() + " at=(" + X + "," + Y + "," + Z + ") east=" + tile);
										flagMap.set(X, Y, Z, FlagMap.FLAG_EAST, tile);
									} else if (orientation == 3) // wall on south
									{
										if (watch) System.out.println("[CollisionMapDumper] WRITE wall edge id=" + loc.getId() + " at=(" + X + "," + (Y - 1) + "," + Z + ") south=" + tile);
										flagMap.set(X, Y - 1, Z, FlagMap.FLAG_SOUTH, tile);
									}
								}

								/*
								 * if (type == 3)
								 * {
								 * if (orientation == 0) // corner north-west
								 * {
								 * flagMap.set(X - 1, Y, Z, FlagMap.FLAG_WEST, tile);
								 * }
								 * else if (orientation == 1) // corner north-east
								 * {
								 * flagMap.set(X, Y, Z, FlagMap.FLAG_NORTH, tile);
								 * }
								 * else if (orientation == 2) // corner south-east
								 * {
								 * flagMap.set(X, Y, Z, FlagMap.FLAG_EAST, tile);
								 * }
								 * else if (orientation == 3) // corner south-west
								 * {
								 * flagMap.set(X, Y - 1, Z, FlagMap.FLAG_SOUTH, tile);
								 * }
								 * }
								 */

								if (type == 2) // double walls
								{
									if (orientation == 3) {
										if (watch) System.out.println("[CollisionMapDumper] WRITE double-wall edge id=" + loc.getId() + " at=(" + (X - 1) + "," + Y + "," + Z + ") west=" + tile);
										flagMap.set(X - 1, Y, Z, FlagMap.FLAG_WEST, tile);
									} else if (orientation == 0) {
										if (watch) System.out.println("[CollisionMapDumper] WRITE double-wall edge id=" + loc.getId() + " at=(" + X + "," + Y + "," + Z + ") north=" + tile);
										flagMap.set(X, Y, Z, FlagMap.FLAG_NORTH, tile);
									} else if (orientation == 1) {
										if (watch) System.out.println("[CollisionMapDumper] WRITE double-wall edge id=" + loc.getId() + " at=(" + X + "," + Y + "," + Z + ") east=" + tile);
										flagMap.set(X, Y, Z, FlagMap.FLAG_EAST, tile);
									} else if (orientation == 2) {
										if (watch) System.out.println("[CollisionMapDumper] WRITE double-wall edge id=" + loc.getId() + " at=(" + X + "," + (Y - 1) + "," + Z + ") south=" + tile);
										flagMap.set(X, Y - 1, Z, FlagMap.FLAG_SOUTH, tile);
									}
								}
							}
						}

						// Diagonal walls
						if (LocationType.isDiagonal(type)) {
							if (object.getMapSceneID() != -1) {
								if (exclusion != null) {
									tile = exclusion;
								}
								for (int sx = 0; sx < sizeX; sx++) {
									for (int sy = 0; sy < sizeY; sy++) {
										if (watch) System.out.println("[CollisionMapDumper] WRITE diag-wall id=" + loc.getId() + " at=(" + (X + sx) + "," + (Y + sy) + "," + Z + ") north/east=" + tile);
										flagMap.set(X + sx, Y + sy, Z, FlagMap.FLAG_NORTH, tile);
										flagMap.set(X + sx, Y + sy, Z, FlagMap.FLAG_EAST, tile);
										if (watch) System.out.println("[CollisionMapDumper] WRITE diag-wall id=" + loc.getId() + " at=(" + (X + sx) + "," + (Y + sy - 1) + "," + Z + ") north=" + tile);
										flagMap.set(X + sx, Y + sy - 1, Z, FlagMap.FLAG_NORTH, tile);
										if (watch) System.out.println("[CollisionMapDumper] WRITE diag-wall id=" + loc.getId() + " at=(" + (X + sx - 1) + "," + (Y + sy) + "," + Z + ") east=" + tile);
										flagMap.set(X + sx - 1, Y + sy, Z, FlagMap.FLAG_EAST, tile);
									}
								}
							} else {
								boolean door = object.getWallOrDoor() != 0;
								tile = door ? FlagMap.TILE_DEFAULT : FlagMap.TILE_BLOCKED;
								if (exclusion != null) {
									tile = exclusion;
								}

								if (orientation != 0 && orientation != 2) // diagonal wall pointing north-east
								{
									flagMap.set(X, Y, Z, FlagMap.FLAG_NORTH, tile);
									flagMap.set(X, Y, Z, FlagMap.FLAG_EAST, tile);
									flagMap.set(X, Y - 1, Z, FlagMap.FLAG_NORTH, tile);
									flagMap.set(X - 1, Y, Z, FlagMap.FLAG_EAST, tile);
								} else // diagonal wall pointing north-west
								{
									flagMap.set(X, Y, Z, FlagMap.FLAG_NORTH, tile);
									flagMap.set(X, Y, Z, FlagMap.FLAG_WEST, tile);
									flagMap.set(X, Y - 1, Z, FlagMap.FLAG_NORTH, tile);
									flagMap.set(X - 1, Y, Z, FlagMap.FLAG_WEST, tile);
								}
							}
						}

						// Remaining objects
						if (LocationType.isDiagonal(type) || LocationType.isMapIconCandidate(type)) {
							if (object.getInteractType() != 0
									&& (object.getWallOrDoor() == 1 || (type >= 10 && type <= 21))) {
								if (exclusion != null) {
									tile = exclusion;
								}

								for (int sx = 0; sx < sizeX; sx++) {
									for (int sy = 0; sy < sizeY; sy++) {
										if (watch) System.out.println("[CollisionMapDumper] WRITE obj id=" + loc.getId() + " at=(" + (X + sx) + "," + (Y + sy) + "," + Z + ") north/east=" + tile);
										flagMap.set(X + sx, Y + sy, Z, FlagMap.FLAG_NORTH, tile);
										flagMap.set(X + sx, Y + sy, Z, FlagMap.FLAG_EAST, tile);
										if (watch) System.out.println("[CollisionMapDumper] WRITE obj id=" + loc.getId() + " at=(" + (X + sx) + "," + (Y + sy - 1) + "," + Z + ") north=" + tile);
										flagMap.set(X + sx, Y + sy - 1, Z, FlagMap.FLAG_NORTH, tile);
										if (watch) System.out.println("[CollisionMapDumper] WRITE obj id=" + loc.getId() + " at=(" + (X + sx - 1) + "," + (Y + sy) + "," + Z + ") east=" + tile);
										flagMap.set(X + sx - 1, Y + sy, Z, FlagMap.FLAG_EAST, tile);
									}
								}
							}
						}
					}

					// Tile without floor / floating in the air ("noclip" tiles, typically found
					// where z > 0)
					int underlayId = region.getUnderlayId(z < 3 ? tileZ : z, localX, localY);
					int overlayId = region.getOverlayId(z < 3 ? tileZ : z, localX, localY);
					boolean noFloor = underlayId == 0 && overlayId == 0;

					// Nomove
					int floorType = region.getTileSetting(z < 3 ? tileZ : z, localX, localY);
					if (floorType == 1 || // water, rooftop wall
							floorType == 3 || // bridge wall
							floorType == 5 || // house wall/roof
							floorType == 7 || // house wall
							noFloor) {
						flagMap.set(regionX, regionY, z, FlagMap.FLAG_NORTH, FlagMap.TILE_BLOCKED);
						flagMap.set(regionX, regionY, z, FlagMap.FLAG_EAST, FlagMap.TILE_BLOCKED);
						flagMap.set(regionX, regionY - 1, z, FlagMap.FLAG_NORTH, FlagMap.TILE_BLOCKED);
						flagMap.set(regionX - 1, regionY, z, FlagMap.FLAG_EAST, FlagMap.TILE_BLOCKED);
					}
				}
			}
		}
	}

	private static class FlagMap {
		/**
		 * The default value of a tile in the compressed collision map
		 */
		public static final boolean TILE_DEFAULT = true;

		/**
		 * The value of a blocked tile in the compressed collision map
		 */
		public static final boolean TILE_BLOCKED = false;

		/**
		 * Number of possible z-planes: 0, 1, 2, 3
		 */
		private static final int PLANE_COUNT = 4;

		/**
		 * Number of possible flags: 0 = north/south, 1 = east/west
		 */
		private static final int FLAG_COUNT = 2;
		public static final int FLAG_NORTH = 0;
		public static final int FLAG_SOUTH = 0;
		public static final int FLAG_EAST = 1;
		public static final int FLAG_WEST = 1;

		/**
		 * BitSet containing FLAG_COUNT bits per tile per plane. The layout is
		 * (plane, y, x, flag) -> single bit index. Use {@link #index} to compute
		 * the index.
		 */
		public final BitSet flags;
		private final int minX;
		private final int minY;
		private final int maxX;
		private final int maxY;
		private final int width;
		private final int height;

		public FlagMap(int minX, int minY, int maxX, int maxY) {
			this(minX, minY, maxX, maxY, TILE_DEFAULT);
		}

		public FlagMap(int minX, int minY, int maxX, int maxY, boolean value) {
			this.minX = minX;
			this.minY = minY;
			this.maxX = maxX;
			this.maxY = maxY;
			width = (maxX - minX + 1);
			height = (maxY - minY + 1);
			flags = new BitSet(width * height * PLANE_COUNT * FLAG_COUNT);
			flags.set(0, flags.size(), value);
		}

		public byte[] toBytes() {
			return flags.toByteArray();
		}

		public void set(int x, int y, int z, int flag, boolean value) {
			if (isValidIndex(x, y, z, flag)) {
				flags.set(index(x, y, z, flag), value);
			}
		}

		private boolean isValidIndex(int x, int y, int z, int flag) {
			return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= 0 && z <= PLANE_COUNT - 1 && flag >= 0
					&& flag <= FLAG_COUNT - 1;
		}

		private int index(int x, int y, int z, int flag) {
			if (isValidIndex(x, y, z, flag)) {
				// Compute a linear index for the 4D coordinate of (z, y, x, flag).
				// Plane-major ordering is used: all tiles for plane 0, then plane 1, ...
				return (z * width * height + (y - minY) * width + (x - minX)) * FLAG_COUNT + flag;
			}
			throw new IndexOutOfBoundsException(x + " " + y + " " + z);
		}
	}

	/*
	 * Normally some objects are not considered blocking, for example doors that can be opened.
	 * But sometimes we want to either force them to be blocking, either because they have requirements to be opened
	 * or because they are just decorative and cannot be opened at all.
	 * This enum contains a list of object IDs that should be treated as blocking (default)
	 * 
	 */
	private enum Exclusion {
		AMETHYST_CRYSTALS_EMPTY_WALL_11393(11393),

		APE_ATOLL_JAIL_DOOR_4800(4800),
		APE_ATOLL_JAIL_DOOR_4801(4801),

		ARDOUGNE_BASEMENT_CELL_DOOR_35795(35795),

		BRIMHAVEN_DUNGEON_EXIT_20878(20878),

		CELL_DOOR_9562(9562),

		COOKING_GUILD_DOOR_10045(10045),
		COOKING_GUILD_DOOR_24958(24958),

		CRAFTING_GUILD_DOOR_14910(14910),

		CRANDOR_WALL_2606(2606),

		DARKMEYER_CELL_DOOR_38014(38014),

		DESERT_MINING_CAMP_PRISON_DOOR_2689(2689),

		DIGSITE_GATE_24560(24560),
		DIGSITE_GATE_24561(24561),

		DRAYNOR_MANOR_LARGE_DOOR_134(134),
		DRAYNOR_MANOR_LARGE_DOOR_135(135),

		DRUIDS_ROBES_4035(4035),
		DRUIDS_ROBES_4036(4036),

		DWARF_CANNON_RAILING_15601(15601), // type = 9 is full blocked diagonal, type = 0 is wall, type = 1 is corner

		EDGEVILLE_DUNGEON_DOOR_1804(1804),

		FALADOR_GRAPPLE_WALL_17049(17049),
		FALADOR_GRAPPLE_WALL_17050(17050),
		FALADOR_GRAPPLE_WALL_17051(17051),
		FALADOR_GRAPPLE_WALL_17052(17052),

		FEROX_ENCLAVE_BARRIER_39652(39652),
		FEROX_ENCLAVE_BARRIER_39653(39653),

		FIGHT_ARENA_PRISON_DOOR_79(79),
		FIGHT_ARENA_PRISON_DOOR_80(80),

		FISHING_TRAWLER_RAIL_41400(41400),

		FORTHOS_DUNGEON_TEMPLE_DOOR_34843(34843),
		FORTHOS_DUNGEON_WALL_34854(34854),

		GOBLIN_TEMPLE_PRISON_DOOR_43457(43457),

		GRAND_EXCHANGE_BOOTH_10060(10060),
		GRAND_EXCHANGE_BOOTH_10061(10061),
		GRAND_EXCHANGE_BOOTH_30390(30390),

		GREAT_KOUREND_CELL_DOOR_41801(41801),

		GRIM_TALES_DOOR_24759(24759),

		HARDWOOD_GROVE_DOORS_9038(9038),
		HARDWOOD_GROVE_DOORS_9039(9039),

		HAUNTED_MINE_DOOR_4963(4963),
		HAUNTED_MINE_DOOR_4964(4964),

		HOSIDIUS_VINES_41814(41814),
		HOSIDIUS_VINES_41815(41815),
		HOSIDIUS_VINES_41816(41816),
		HOSIDIUS_VINES_46380(46380),
		HOSIDIUS_VINES_46381(46381),
		HOSIDIUS_VINES_46382(46382),

		KENDAL_STANDING_SPEARS_5860(5860),

		KRUKS_DUNGEON_WALL_28681(28681),
		KRUKS_DUNGEON_WALL_28798(28798),

		LUMBRIDGE_RECIPE_FOR_DISASTER_DOOR_12348(12348),
		LUMBRIDGE_RECIPE_FOR_DISASTER_DOOR_12349(12349),
		LUMBRIDGE_RECIPE_FOR_DISASTER_DOOR_12350(12350),

		MAGIC_AXE_HUT_DOOR_11726(11726),

		MCGRUBORS_WOOD_GATE_52(52),
		MCGRUBORS_WOOD_GATE_53(53),

		MEIYERDITCH_DOOR_17973(17973),

		MELZARS_MAZE_BLUE_DOOR_2599(2599),
		MELZARS_MAZE_DOOR_2595(2595),
		MELZARS_MAZE_EXIT_DOOR_2602(2602),
		MELZARS_MAZE_GREEN_DOOR_2601(2601),
		MELZARS_MAZE_MAGENTA_DOOR_2600(2600),
		MELZARS_MAZE_ORANGE_DOOR_2597(2597),
		MELZARS_MAZE_RED_DOOR_2596(2596),
		MELZARS_MAZE_YELLOW_DOOR_2598(2598),

		// MEMBERS_GATE_1727(1727), // Taverley, Falador, Brimhaven, Wilderness,
		// Edgeville Dungeon
		// MEMBERS_GATE_1728(1728), // Taverley, Falador, Brimhaven, Wilderness,
		// Edgeville Dungeon

		OLD_SCHOOL_MUSEUM_CURTAIN_31885(31885), // type = 9 is full blocked diagonal, type = 0 is wall

		PATERDOMUS_TEMPLE_CELL_DOOR_3463(3463),

		PEST_CONTROL_WALL_14216(14216),
		PEST_CONTROL_WALL_14217(14217),
		PEST_CONTROL_WALL_14218(14218),
		PEST_CONTROL_WALL_14219(14219),
		PEST_CONTROL_WALL_14225(14225),
		PEST_CONTROL_WALL_14226(14226),
		PEST_CONTROL_WALL_14228(14228),
		PEST_CONTROL_WALL_14229(14229),
		PEST_CONTROL_WALL_25636(25636), // type = 9 is full blocked diagonal, type = 0 is wall

		PORT_SARIM_PRISON_DOOR_9563(9563),
		PORT_SARIM_PRISON_DOOR_9565(9565),

		PRINCE_ALI_RESCUE_PRISON_GATE_2881(2881),

		RANGING_GUILD_DOOR_11665(11665),

		RAT_PITS_RAT_WALL_10335(10335), // type = 9 is full blocked diagonal, type = 2 is wall
		RAT_PITS_RAT_WALL_10337(10337), // type = 9 is full blocked diagonal, type = 2 is wall
		RAT_PITS_RAT_WALL_10342(10342), // type = 9 is full blocked diagonal, type = 2 is wall
		RAT_PITS_RAT_WALL_10344(10344), // type = 9 is full blocked diagonal, type = 2 is wall

		SCRUBFOOTS_CAVE_CREVICE_40889(40889),

		SHANTAY_PASS_PRISON_DOOR_2692(2692),

		TAI_BWO_WANNAI_ROTTEN_VILLAGE_FENCE_9025(9025),
		TAI_BWO_WANNAI_PARTIAL_FENCE_9026(9026),
		TAI_BWO_WANNAI_SHORT_FENCE_9027(9027),
		TAI_BWO_WANNAI_MEDIUM_FENCE_9028(9028),
		TAI_BWO_WANNAI_VILLAGE_FENCE_9029(9029),

		TAVERLEY_DUNGEON_PRISON_DOOR_2143(2143),
		TAVERLEY_DUNGEON_PRISON_DOOR_2144(2144),
		TAVERLEY_DUNGEON_DUSTY_KEY_DOOR_2623(2623),

		TEMPLE_OF_IKOV_DOOR_102(102),

		TEMPLE_OF_MARIMBO_DUNGEON_EXIT_16061(16061),
		TEMPLE_OF_MARIMBO_DUNGEON_EXIT_16100(16100),

		TREE_GNOME_STRONGHOLD_PRISON_DOOR_3367(3367),

		TROLL_STRONGHOLD_CELL_DOOR_3763(3763),
		TROLL_STRONGHOLD_CELL_DOOR_3765(3765),
		TROLL_STRONGHOLD_CELL_DOOR_3767(3767),
		TROLL_STRONGHOLD_EXIT_3772(3772),
		TROLL_STRONGHOLD_EXIT_3773(3773),
		TROLL_STRONGHOLD_EXIT_3774(3774),
		TROLL_STRONGHOLD_PRISON_DOOR_3780(3780),

		VARROCK_FENCE_SHORTCUT_16518(16518),

		VIYELDI_CAVES_CREVICE_2918(2918),

		WATERFALL_DUNGEON_DOOR_2002(2002),

		WEISS_BROKEN_FENCE_46815(46815),
		WEISS_BROKEN_FENCE_46816(46816),
		WEISS_BROKEN_FENCE_46817(46817),

		WILDERNESS_RESOURCE_AREA_GATE_26760(26760),

		YANILLE_DUNGEON_DOOR_11728(11728),
		YANILLE_GRAPPLE_WALL_17047(17047),
		YANILLE_GRAPPLE_WALL_17048(17058),
		YANILLE_MAGIC_GUILD_DOOR_1732(1732),
		YANILLE_MAGIC_GUILD_DOOR_1733(1733),

		ZANARIS_SHED_DOOR_2406(2406),
		;

		/**
		 * The object ID to be excluded
		 */
		private final int id;

		/**
		 * Whether the exclusion tile should be blocked or empty
		 */
		private final boolean tile;

		Exclusion(int id) {
			this(id, FlagMap.TILE_BLOCKED);
		}

		Exclusion(int id, boolean tile) {
			this.id = id;
			this.tile = tile;
		}

		public static Boolean matches(int id) {
			for (Exclusion exclusion : values()) {
				if (exclusion.id == id) {
					return exclusion.tile;
				}
			}
			return null;
		}
	}
}
