package shortestpath.transport;

import shortestpath.CsvReader;
import shortestpath.ItemVariations;
import shortestpath.Util;
import shortestpath.WorldPointUtil;

import java.io.IOException;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

@Slf4j
public class TransportLoader {
    private static final int UNDEFINED_ORIGIN = WorldPointUtil.UNDEFINED;
    private static final int UNDEFINED_DESTINATION = WorldPointUtil.UNDEFINED;

    private static final String DELIM_SPACE = " ";
    private static final String DELIM_MULTI = ";";
    private static final String DELIM_STATE = "=";
    private static final String DELIM_AND = "&";
    private static final String DELIM_OR = "|";

    private static final int LOCATION_PERMUTATION = WorldPointUtil.packWorldPoint(-1, -1, 1);

    private static void addTransports(Map<Integer, Set<Transport>> transports, String path,
            TransportType transportType) {
        addTransports(transports, path, transportType, 0);
    }

    private static void addTransports(
            Map<Integer, Set<Transport>> transports,
            String path,
            TransportType transportType,
            int radiusThreshold) {
        try {
            Set<Transport> newTransports = new HashSet<>();

            List<Map<String, String>> rows = CsvReader.readResource(path);
            for (Map<String, String> fieldMap : rows) {
                Transport transport = TransportLoader.parseRow(fieldMap, transportType);
                newTransports.add(transport);
            }

            Set<Transport> transportOrigins = new HashSet<>();
            Set<Transport> transportDestinations = new HashSet<>();
            for (Transport transport : newTransports) {
                int origin = transport.getOrigin();
                int destination = transport.getDestination();
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
                    if (WorldPointUtil.distanceBetween2D(origin.getOrigin(),
                            destination.getDestination()) > radiusThreshold) {
                        transports.computeIfAbsent(origin.getOrigin(), k -> new HashSet<>())
                                .add(new Transport(origin, destination));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Transport parseRow(Map<String, String> fieldMap, TransportType transportType) {
        String value;

        int origin = UNDEFINED_ORIGIN;
        int destination = UNDEFINED_DESTINATION;
        int[] skillLevels = new int[Skill.values().length];
        TransportItems itemRequirements = null;
        Queue<String> quests = new LinkedList<>();
        int duration = 0;
        String displayInfo = null;
        boolean isConsumable = false;
        int maxWildernessLevel = -1;
        TransportType type = transportType;

        // Ensure 'varbits' and 'varPlayers' are declared in the correct scope
        // Declare them at the beginning of the parseRow method
        Set<TransportVarbit> varbits = new HashSet<>();
        Set<TransportVarPlayer> varPlayers = new HashSet<>();

        // If the origin field is null the transport is a teleportation item or spell
        // If the origin field has 3 elements it is a coordinate of a transport
        // Otherwise it is a transport that needs to be expanded into all permutations
        // (e.g. fairy ring)
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
                    String skillName = levelAndSkill[1];

                    Skill[] skills = Skill.values();
                    for (int i = 0; i < skills.length; i++) {
                        if (skills[i].getName().equals(skillName)) {
                            skillLevels[i] = level;
                            break;
                        }
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
                            multipleItems[k] = itemVariations == null
                                    ? new int[] { Integer.parseInt(itemVariationAndQuantity[0]) }
                                    : itemVariations.getIds();
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
            quests = findQuests(value);
        }

        if ((value = fieldMap.get("Duration")) != null && !value.isEmpty()) {
            try {
                duration = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.error("Invalid tick duration: " + value);
            }
        }
        if (TransportType.isTeleport(transportType)) {
            // Teleports should always have a non-zero wait,
            // so the pathfinder doesn't calculate the cost by distance
            duration = Math.max(duration, 1);
        }

        if ((value = fieldMap.get("Display info")) != null) {
            displayInfo = value;
        }

        if ((value = fieldMap.get("Consumable")) != null) {
            isConsumable = "T".equals(value) || "yes".equals(value.toLowerCase());
        }

        if ((value = fieldMap.get("Wilderness level")) != null && !value.isEmpty()) {
            try {
                maxWildernessLevel = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.error("Invalid wilderness level: " + value);
            }
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
                    if (varbitParts == null || varbitParts.length != 2) {
                        throw new IllegalArgumentException("Invalid varbit id and value: '" + varbitRequirement + "'");
                    }
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
                    if (varPlayerParts == null || varPlayerParts.length != 2) {
                        throw new IllegalArgumentException(
                                "Invalid VarPlayer id and value: '" + varPlayerRequirement + "'");
                    }
                }
            } catch (NumberFormatException e) {
                log.error("Invalid VarPlayer id and value: " + value);
            }
        }

        type = transportType;
        if (TransportType.AGILITY_SHORTCUT.equals(transportType) &&
                (getRequiredLevel(Skill.RANGED, skillLevels) > 1
                        || getRequiredLevel(Skill.STRENGTH, skillLevels) > 1)) {
            type = TransportType.GRAPPLE_SHORTCUT;
        }

        Set<Quest> questSet = new HashSet<>();
        for (String questName : quests) {
            Quest quest = Quest.valueOf(questName.toUpperCase());
            if (quest != null) {
                questSet.add(quest);
            }
        }

        return new Transport(
                origin,
                destination,
                skillLevels,
                itemRequirements,
                questSet,
                duration,
                displayInfo,
                isConsumable,
                maxWildernessLevel,
                type);
    }

    /** The skill level required to use this transport */
    public static int getRequiredLevel(Skill skill, int[] skillLevels) {
        return skillLevels[skill.ordinal()];
    }

    public static Queue<String> findQuests(String questNamesCombined) {
        String[] questNames = questNamesCombined.split(";");
        Queue<String> quests = new LinkedList<>();
        for (String questName : questNames) {
            quests.add(questName);
        }
        return quests;
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
        addTransports(transports, "/transports/magic_mushtrees.tsv", TransportType.MAGIC_MUSHTREE, 5);
        addTransports(transports, "/transports/minecarts.tsv", TransportType.MINECART);
        addTransports(transports, "/transports/quetzals.tsv", TransportType.QUETZAL);
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
