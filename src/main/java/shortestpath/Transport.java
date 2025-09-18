package shortestpath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import lombok.extern.slf4j.Slf4j;

/**
 * This class represents a travel point between two WorldPoints.
 */
@Slf4j
public class Transport {
    public static final int UNDEFINED_ORIGIN = WorldPointUtil.UNDEFINED;
    public static final int UNDEFINED_DESTINATION = WorldPointUtil.UNDEFINED;
    /** A location placeholder different from null to use for permutation transports */
    private static final int LOCATION_PERMUTATION = WorldPointUtil.packWorldPoint(-1, -1, 1);
    private static final String DELIM_SPACE = " ";
    private static final String DELIM_MULTI = ";";
    private static final String DELIM_STATE = "=";
    private static final String DELIM_AND = "&";
    private static final String DELIM_OR = "|";

    /** The starting point of this transport */
    @Getter
    private int origin = UNDEFINED_ORIGIN;

    /** The ending point of this transport */
    @Getter
    private int destination = UNDEFINED_DESTINATION;

    /** The skill levels, total level, combat level and quest points required to use this transport */
    @Getter
    private final int[] skillLevels = new int[Skill.values().length + 3];

    /** The quests required to use this transport */
    @Getter
    private Set<Quest> quests = new HashSet<>();

    /** The item requirements to use this transport */
    @Getter
    private TransportItems itemRequirements;

    /** The type of transport */
    @Getter
    private TransportType type;

    /** The travel waiting time in number of ticks */
    @Getter
    private int duration;

    /** Info to display for this transport. For spirit trees, fairy rings,
     * and others, this is the destination option to pick. */
    @Getter
    private String displayInfo = null;

    /** If this is an item transport, this tracks if it is consumable (as opposed to having infinite uses) */
    @Getter
    private boolean isConsumable = false;

    /** The maximum wilderness level that the transport can be used in */
    @Getter
    private int maxWildernessLevel = -1;

    /** Object information for this transport */
    @Getter
    private String objectInfo = null;

    /** Any varbits to check for the transport to be valid. All must pass for a transport to be valid */
    @Getter
    private final Set<TransportVarbit> varbits = new HashSet<>();

    /** Any varplayers to check for the transport to be valid. All must pass for a transport to be valid */
    @Getter
    private final Set<TransportVarPlayer> varPlayers = new HashSet<>();

    /** Creates a new transport from an origin-only transport
     * and a destination-only transport, and merges requirements */
    Transport(Transport origin, Transport destination) {
        this.origin = origin.origin;
        this.destination = destination.destination;

        for (int i = 0; i < skillLevels.length; i++) {
            this.skillLevels[i] = Math.max(
                origin.skillLevels[i],
                destination.skillLevels[i]);
        }

        this.quests.addAll(origin.quests);
        this.quests.addAll(destination.quests);

        this.itemRequirements = destination.itemRequirements;

        this.type = origin.type;

        this.duration = Math.max(
            origin.duration,
            destination.duration);

        this.displayInfo = destination.displayInfo;

        this.isConsumable |= origin.isConsumable;
        this.isConsumable |= destination.isConsumable;

        this.maxWildernessLevel = Math.max(
            origin.maxWildernessLevel,
            destination.maxWildernessLevel);

        this.objectInfo = origin.objectInfo;

        this.varbits.addAll(origin.varbits);
        this.varbits.addAll(destination.varbits);

        this.varPlayers.addAll(origin.varPlayers);
        this.varPlayers.addAll(destination.varPlayers);
    }

    Transport(Map<String, String> fieldMap, TransportType transportType) {
        String value;

        // If the origin field is null the transport is a teleportation item or spell
        // If the origin field has 3 elements it is a coordinate of a transport
        // Otherwise it is a transport that needs to be expanded into all permutations (e.g. fairy ring)
        if ((value = fieldMap.get("Origin")) != null) {
            String[] originArray = value.split(DELIM_SPACE);
            origin = originArray.length == 3 ? WorldPointUtil.packWorldPoint(
                Integer.parseInt(originArray[0]),
                Integer.parseInt(originArray[1]),
                Integer.parseInt(originArray[2])) : LOCATION_PERMUTATION;
        }

        if ((value = fieldMap.get("Destination")) != null) {
            String[] destinationArray = value.split(DELIM_SPACE);
            destination = destinationArray.length == 3 ? WorldPointUtil.packWorldPoint(
                Integer.parseInt(destinationArray[0]),
                Integer.parseInt(destinationArray[1]),
                Integer.parseInt(destinationArray[2])) : LOCATION_PERMUTATION;
        }

        if ((value = fieldMap.get("Skills")) != null) {
            String[] skillRequirements = value.split(DELIM_MULTI);

            try {
                for (String requirement : skillRequirements) {
                    if (requirement.isEmpty()) {
                        continue;
                    }
                    String[] levelAndSkill = requirement.split(DELIM_SPACE);
                    assert levelAndSkill.length == 2 : "Invalid level and skill: '" + requirement + "'";

                    int level = Integer.parseInt(levelAndSkill[0]);
                    String skillName = levelAndSkill[1] == null ? "" : levelAndSkill[1];

                    Skill[] skills = Skill.values();
                    int i = 0;
                    for (; i < skills.length; i++) {
                        if (skills[i].getName().equals(skillName)) {
                            skillLevels[i] = level;
                        }
                    }
                    if (skillName.toLowerCase().startsWith("total")) {
                        skillLevels[i] = level;
                    }
                    i++;
                    if (skillName.toLowerCase().startsWith("combat")) {
                        skillLevels[i] = level;
                    }
                    i++;
                    if (skillName.toLowerCase().startsWith("quest")) {
                        skillLevels[i] = level;
                    }
                }
            } catch (NumberFormatException e) {
                log.error("Invalid level and skill: " + value);
            }
        }

        if ((value = fieldMap.get("Items")) != null && !value.isEmpty()) {
            value = value.replace(DELIM_SPACE, "");
            value = value.replace(DELIM_AND + DELIM_AND, DELIM_AND);
            value = value.replace(DELIM_OR + DELIM_OR, DELIM_OR);
            value = value.toUpperCase();
            String[] itemVariationAndQuantityList = value.split(DELIM_AND);
            try {
                int n = itemVariationAndQuantityList.length;
                int[][] items = new int[n][];
                int[][] staves = new int[n][];
                int[][] offhands = new int[n][];
                int[] quantities = new int[n];
                for (int i = 0; i < n; i++) {
                    int maxQuantity = -1;
                    String[] itemVariationsAndQuantities = itemVariationAndQuantityList[i].split("\\" + DELIM_OR);
                    int[][] multipleItems = new int[itemVariationsAndQuantities.length][];
                    int[][] multipleStaves = new int[itemVariationsAndQuantities.length][];
                    int[][] multipleOffhands = new int[itemVariationsAndQuantities.length][];
                    for (int k = 0; k < itemVariationsAndQuantities.length; k++) {
                        String[] itemVariationAndQuantity = itemVariationsAndQuantities[k].split(DELIM_STATE);
                        if (itemVariationAndQuantity.length == 2) {
                            ItemVariations itemVariations = ItemVariations.fromName(itemVariationAndQuantity[0]);
                            multipleItems[k] = itemVariations == null ? new int[]{Integer.parseInt(itemVariationAndQuantity[0])} : itemVariations.getIds();
                            multipleStaves[k] = ItemVariations.staves(itemVariations);
                            multipleOffhands[k] = ItemVariations.offhands(itemVariations);
                            maxQuantity = Math.max(maxQuantity, Integer.parseInt(itemVariationAndQuantity[1]));
                        } else {
                            throw new NumberFormatException(itemVariationAndQuantityList[i]);
                        }
                    }
                    items[i] = Util.concatenate(multipleItems);
                    staves[i] = Util.concatenate(multipleStaves);
                    offhands[i] = Util.concatenate(multipleOffhands);
                    quantities[i] = maxQuantity;
                }
                itemRequirements = new TransportItems(items, staves, offhands, quantities);
            } catch (NumberFormatException e) {
                log.error("Invalid item or quantity: " + value);
            }
        }

        if ((value = fieldMap.get("Quests")) != null) {
            this.quests = findQuests(value);
        }

        if ((value = fieldMap.get("Duration")) != null && !value.isEmpty()) {
            try {
                this.duration = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.error("Invalid tick duration: " + value);
            }
        }
        if (TransportType.isTeleport(transportType)) {
            // Teleports should always have a non-zero wait,
            // so the pathfinder doesn't calculate the cost by distance
            this.duration = Math.max(this.duration, 1);
        }

        if ((value = fieldMap.get("Display info")) != null) {
            this.displayInfo = value;
        }

        if ((value = fieldMap.get("Consumable")) != null) {
            this.isConsumable = "T".equals(value) || "yes".equals(value.toLowerCase());
        }

        if ((value = fieldMap.get("Wilderness level")) != null && !value.isEmpty()) {
            try {
                this.maxWildernessLevel =  Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.error("Invalid wilderness level: " + value);
            }
        }

        if ((value = fieldMap.get("menuOption menuTarget objectID")) != null) {
            this.objectInfo = value;
        }

        if ((value = fieldMap.get("Varbits")) != null) {
            try {
                for (String varbitRequirement : value.split(DELIM_MULTI)) {
                    if (varbitRequirement.isEmpty()) {
                        continue;
                    }
                    String[] varbitParts = null;
                    for (TransportVarCheck check : TransportVarCheck.values()) {
                        varbitParts = varbitRequirement.split(check.getCode());
                        if (varbitParts.length == 2) {
                            int varbitId = Integer.parseInt(varbitParts[0]);
                            int varbitValue = Integer.parseInt(varbitParts[1]);
                            varbits.add(new TransportVarbit(varbitId, varbitValue, check));
                            break;
                        }
                    }
                    assert varbitParts.length == 2 : "Invalid varbit id and value: '" + varbitRequirement + "'";
                }
            } catch (NumberFormatException e) {
                log.error("Invalid varbit id and value: " + value);
            }
        }

        if ((value = fieldMap.get("VarPlayers")) != null) {
            try {
                for (String varPlayerRequirement : value.split(DELIM_MULTI)) {
                    if (varPlayerRequirement.isEmpty()) {
                        continue;
                    }
                    String[] varPlayerParts = null;
                    for (TransportVarCheck check : TransportVarCheck.values()) {
                        varPlayerParts = varPlayerRequirement.split(check.getCode());
                        if (varPlayerParts.length == 2) {
                            int varPlayerId = Integer.parseInt(varPlayerParts[0]);
                            int varPlayerValue = Integer.parseInt(varPlayerParts[1]);
                            varPlayers.add(new TransportVarPlayer(varPlayerId, varPlayerValue, check));
                            break;
                        }
                    }
                    assert varPlayerParts.length == 2 : "Invalid VarPlayer id and value: '" + varPlayerRequirement + "'";
                }
            } catch (NumberFormatException e) {
                log.error("Invalid VarPlayer id and value: " + value);
            }
        }

        this.type = transportType;
        if (TransportType.AGILITY_SHORTCUT.equals(transportType) &&
            (getRequiredLevel(Skill.RANGED) > 1 || getRequiredLevel(Skill.STRENGTH) > 1)) {
            this.type = TransportType.GRAPPLE_SHORTCUT;
        }
    }

    @Override
    public String toString() {
        return ("(" +
            WorldPointUtil.unpackWorldX(origin) + ", " +
            WorldPointUtil.unpackWorldY(origin) + ", " +
            WorldPointUtil.unpackWorldPlane(origin) + ") to ("+
            WorldPointUtil.unpackWorldX(destination) + ", " +
            WorldPointUtil.unpackWorldY(destination) + ", " +
            WorldPointUtil.unpackWorldPlane(destination) + ")");
    }

    /** The skill level required to use this transport */
    private int getRequiredLevel(Skill skill) {
        return skillLevels[skill.ordinal()];
    }

    /** Whether the transport has one or more quest requirements */
    public boolean isQuestLocked() {
        return !quests.isEmpty();
    }

    private static Set<Quest> findQuests(String questNamesCombined) {
        String[] questNames = questNamesCombined.split(";");
        Set<Quest> quests = new HashSet<>();
        for (String questName : questNames) {
            for (Quest quest : Quest.values()) {
                if (quest.getName().equals(questName)) {
                    quests.add(quest);
                    break;
                }
            }
        }
        return quests;
    }

    private static void addTransports(Map<Integer, Set<Transport>> transports, String path, TransportType transportType, int radiusThreshold) {
        try {
            String s = new String(Util.readAllBytes(ShortestPathPlugin.class.getResourceAsStream(path)), StandardCharsets.UTF_8);
            addTransportsFromContents(transports, s, transportType, radiusThreshold);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void addTransports(Map<Integer, Set<Transport>> transports, String path, TransportType transportType) {
        addTransports(transports, path, transportType, 0);
    }

    public static void addTransportsFromContents(Map<Integer, Set<Transport>> transports, String contents, TransportType transportType, int radiusThreshold) {
        final String DELIM_COLUMN = "\t";
        final String PREFIX_COMMENT = "#";

        Scanner scanner = new Scanner(contents);

        // Header line is the first line in the file and will start with either '#' or '# '
        String headerLine = scanner.nextLine();
        headerLine = headerLine.startsWith(PREFIX_COMMENT + " ") ? headerLine.replace(PREFIX_COMMENT + " ", PREFIX_COMMENT) : headerLine;
        headerLine = headerLine.startsWith(PREFIX_COMMENT) ? headerLine.replace(PREFIX_COMMENT, "") : headerLine;
        String[] headers = headerLine.split(DELIM_COLUMN);

        Set<Transport> newTransports = new HashSet<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.startsWith(PREFIX_COMMENT) || line.isBlank()) {
                continue;
            }

            String[] fields = line.split(DELIM_COLUMN);
            Map<String, String> fieldMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                if (i < fields.length) {
                    fieldMap.put(headers[i], fields[i]);
                }
            }

            Transport transport = new Transport(fieldMap, transportType);
            newTransports.add(transport);

        }
        scanner.close();

        /*
        * A transport with origin A and destination B is one-way and must
        * be duplicated as origin B and destination A to become two-way.
        * Example: key-locked doors
        * 
        * A transport with origin A and a missing destination is one-way,
        * but can go from origin A to all destinations with a missing origin.
        * Example: fairy ring AIQ -> <blank>
        * 
        * A transport with a missing origin and destination B is one-way,
        * but can go from all origins with a missing destination to destination B.
        * Example: fairy ring <blank> -> AIQ
        * 
        * Identical transports from origin A to destination A are skipped, and
        * non-identical transports from origin A to destination A can be skipped
        * by specifying a radius threshold to ignore almost identical coordinates.
        * Example: fairy ring AIQ -> AIQ
        */
        Set<Transport> transportOrigins = new HashSet<>();
        Set<Transport> transportDestinations = new HashSet<>();
        for (Transport transport : newTransports) {
            int origin = transport.getOrigin();
            int destination = transport.getDestination();
            // Logic to determine ordinary transport vs teleport vs permutation (e.g. fairy ring)
            if ((origin == UNDEFINED_ORIGIN && destination == UNDEFINED_DESTINATION)
                || (origin == LOCATION_PERMUTATION && destination == LOCATION_PERMUTATION)) {
                continue;
            } else if (origin != LOCATION_PERMUTATION && origin != UNDEFINED_ORIGIN
                && destination == LOCATION_PERMUTATION) {
                transportOrigins.add(transport);
            } else if (origin == LOCATION_PERMUTATION
                && destination != LOCATION_PERMUTATION && destination != UNDEFINED_DESTINATION) {
                transportDestinations.add(transport);
            }
            if (origin != LOCATION_PERMUTATION
                && destination != UNDEFINED_DESTINATION && destination != LOCATION_PERMUTATION
                && (origin == UNDEFINED_ORIGIN || origin != destination)) {
                transports.computeIfAbsent(origin, k -> new HashSet<>()).add(transport);
            }
        }
        for (Transport origin : transportOrigins) {
            for (Transport destination : transportDestinations) {
                // The radius threshold prevents transport permutations from including (almost) same origin and destination
                if (WorldPointUtil.distanceBetween2D(origin.getOrigin(), destination.getDestination()) > radiusThreshold) {
                    transports.computeIfAbsent(origin.getOrigin(), k -> new HashSet<>())
                        .add(new Transport(origin, destination));
                }
            }
        }
    }

    public static HashMap<Integer, Set<Transport>> loadAllFromResources() {
        HashMap<Integer, Set<Transport>> transports = new HashMap<>();
        addTransports(transports, "/transports/transports.tsv", TransportType.TRANSPORT);
        addTransports(transports, "/transports/agility_shortcuts.tsv", TransportType.AGILITY_SHORTCUT);
        addTransports(transports, "/transports/boats.tsv", TransportType.BOAT);
        addTransports(transports, "/transports/canoes.tsv", TransportType.CANOE);
        addTransports(transports, "/transports/charter_ships.tsv", TransportType.CHARTER_SHIP);
        addTransports(transports, "/transports/ships.tsv", TransportType.SHIP);
        addTransports(transports, "/transports/fairy_rings.tsv", TransportType.FAIRY_RING);
        addTransports(transports, "/transports/gnome_gliders.tsv", TransportType.GNOME_GLIDER, 6);
        addTransports(transports, "/transports/hot_air_balloons.tsv", TransportType.HOT_AIR_BALLOON, 7);
        addTransports(transports, "/transports/magic_carpets.tsv", TransportType.MAGIC_CARPET);
        addTransports(transports, "/transports/magic_mushtrees.tsv", TransportType.MAGIC_MUSHTREE, 5);
        addTransports(transports, "/transports/minecarts.tsv", TransportType.MINECART);
        addTransports(transports, "/transports/quetzals.tsv", TransportType.QUETZAL);
        addTransports(transports, "/transports/seasonal_transports.tsv", TransportType.SEASONAL_TRANSPORTS);
        addTransports(transports, "/transports/spirit_trees.tsv", TransportType.SPIRIT_TREE, 5);
        addTransports(transports, "/transports/teleportation_items.tsv", TransportType.TELEPORTATION_ITEM);
        addTransports(transports, "/transports/teleportation_boxes.tsv", TransportType.TELEPORTATION_BOX);
        addTransports(transports, "/transports/teleportation_levers.tsv", TransportType.TELEPORTATION_LEVER);
        addTransports(transports, "/transports/teleportation_minigames.tsv", TransportType.TELEPORTATION_MINIGAME);
        addTransports(transports, "/transports/teleportation_portals.tsv", TransportType.TELEPORTATION_PORTAL);
        addTransports(transports, "/transports/teleportation_spells.tsv", TransportType.TELEPORTATION_SPELL);
        addTransports(transports, "/transports/wilderness_obelisks.tsv", TransportType.WILDERNESS_OBELISK);
        return transports;
    }
}
