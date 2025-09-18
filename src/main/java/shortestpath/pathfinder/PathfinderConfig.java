package shortestpath.pathfinder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static shortestpath.transport.TransportType.AGILITY_SHORTCUT;
import static shortestpath.transport.TransportType.BOAT;
import static shortestpath.transport.TransportType.CANOE;
import static shortestpath.transport.TransportType.CHARTER_SHIP;
import static shortestpath.transport.TransportType.FAIRY_RING;
import static shortestpath.transport.TransportType.GNOME_GLIDER;
import static shortestpath.transport.TransportType.GRAPPLE_SHORTCUT;
import static shortestpath.transport.TransportType.HOT_AIR_BALLOON;
import static shortestpath.transport.TransportType.MAGIC_CARPET;
import static shortestpath.transport.TransportType.MAGIC_MUSHTREE;
import static shortestpath.transport.TransportType.MINECART;
import static shortestpath.transport.TransportType.QUETZAL;
import static shortestpath.transport.TransportType.SEASONAL_TRANSPORTS;
import static shortestpath.transport.TransportType.SHIP;
import static shortestpath.transport.TransportType.SPIRIT_TREE;
import static shortestpath.transport.TransportType.TELEPORTATION_BOX;
import static shortestpath.transport.TransportType.TELEPORTATION_ITEM;
import static shortestpath.transport.TransportType.TELEPORTATION_LEVER;
import static shortestpath.transport.TransportType.TELEPORTATION_MINIGAME;
import static shortestpath.transport.TransportType.TELEPORTATION_PORTAL;
import static shortestpath.transport.TransportType.TELEPORTATION_SPELL;
import static shortestpath.transport.TransportType.WILDERNESS_OBELISK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import shortestpath.TeleportationItem;
import shortestpath.ShortestPathConfig;
import shortestpath.ShortestPathPlugin;
import shortestpath.Destination;
import shortestpath.ItemVariations;
import shortestpath.PrimitiveIntHashMap;
import shortestpath.WorldPointUtil;
import shortestpath.transport.Transport;
import shortestpath.transport.TransportItems;
import shortestpath.transport.TransportLoader;
import shortestpath.transport.TransportType;
import shortestpath.transport.TransportVarPlayer;
import shortestpath.transport.TransportVarbit;

public class PathfinderConfig {
    private static final List<Integer> RUNE_POUCHES = Arrays.asList(
        ItemID.BH_RUNE_POUCH, ItemID.BH_RUNE_POUCH_TROUVER,
        ItemID.DIVINE_RUNE_POUCH, ItemID.DIVINE_RUNE_POUCH_TROUVER
    );
    private static final int[] RUNE_POUCH_RUNE_VARBITS = {
        VarbitID.RUNE_POUCH_TYPE_1, VarbitID.RUNE_POUCH_TYPE_2, VarbitID.RUNE_POUCH_TYPE_3, VarbitID.RUNE_POUCH_TYPE_4,
        VarbitID.RUNE_POUCH_TYPE_5, VarbitID.RUNE_POUCH_TYPE_6
	};
    private static final int[] RUNE_POUCH_AMOUNT_VARBITS = {
        VarbitID.RUNE_POUCH_QUANTITY_1, VarbitID.RUNE_POUCH_QUANTITY_2, VarbitID.RUNE_POUCH_QUANTITY_3, VarbitID.RUNE_POUCH_QUANTITY_4,
        VarbitID.RUNE_POUCH_QUANTITY_5, VarbitID.RUNE_POUCH_QUANTITY_6
	};
    private static final Set<Integer> CURRENCIES = Set.of(
        ItemID.COINS, ItemID.VILLAGE_TRADE_STICKS, ItemID.ECTOTOKEN, ItemID.WARGUILD_TOKENS);
    private static final TransportItems DRAMEN_STAFF = new TransportItems(
        new int[][]{null},
        new int[][]{ItemVariations.DRAMEN_STAFF.getIds()},
        new int[][]{null},
        new int[]{1});

    private final SplitFlagMap mapData;
    private final ThreadLocal<CollisionMap> map;
    /** All transports by origin. The WorldPointUtil.UNDEFINED key is used for transports centered on the player. */
    private final Map<Integer, Set<Transport>> allTransports;
    private final Set<Transport> usableTeleports;
    private final Map<String, Set<Integer>> allDestinations;
    private final Map<String, Set<Integer>> filteredDestinations;
    private final Map<Integer, Integer> itemsAndQuantities = new HashMap<>(28 + 11 + 500);
    private final List<Integer> filteredTargets = new ArrayList<>(4);

    @Getter
    private final Map<Integer, Set<Transport>> transports;
    // Copy of transports with packed positions for the hotpath; lists are not copied and are the same reference in both maps
    @Getter
    private final PrimitiveIntHashMap<Set<Transport>> transportsPacked;
    /** Reference that points to either allDestinations or filteredDestinations */
    private Map<String, Set<Integer>> destinations;

    private final Client client;
    private final ShortestPathConfig config;

    @Getter
    private long calculationCutoffMillis;
    @Getter
    private boolean avoidWilderness;
    private boolean useAgilityShortcuts,
        useGrappleShortcuts,
        useBoats,
        useCanoes,
        useCharterShips,
        useShips,
        useFairyRings,
        useGnomeGliders,
        useHotAirBalloons,
        useMagicCarpets,
        useMagicMushtrees,
        useMinecarts,
        useQuetzals,
        useSeasonalTransports,
        useSpiritTrees,
        useTeleportationBoxes,
        useTeleportationLevers,
        useTeleportationMinigames,
        useTeleportationPortals,
        useTeleportationSpells,
        useWildernessObelisks;
    private TeleportationItem useTeleportationItems;
    private int currencyThreshold;
    private final int[] boostedSkillLevelsAndMore = new int[Skill.values().length + 3];
    private Map<Quest, QuestState> questStates = new HashMap<>();
    private Map<Integer, Integer> varbitValues = new HashMap<>();
    private Map<Integer, Integer> varPlayerValues = new HashMap<>();

    public ItemContainer bank = null;

    public PathfinderConfig(Client client, ShortestPathConfig config) {
        this.client = client;
        this.config = config;
        this.mapData = SplitFlagMap.fromResources();
        this.map = ThreadLocal.withInitial(() -> new CollisionMap(mapData));
        this.allTransports = TransportLoader.loadAllFromResources();
        this.usableTeleports = new HashSet<>(allTransports.size() / 20);
        this.transports = new HashMap<>(allTransports.size() / 2);
        this.transportsPacked = new PrimitiveIntHashMap<>(allTransports.size() / 2);
        this.allDestinations = Destination.loadAllFromResources();
        this.filteredDestinations = filterDestinations(allDestinations);
        this.destinations = allDestinations;
    }

    public CollisionMap getMap() {
        return map.get();
    }

    public boolean hasDestination(String destinationType) {
        return destinations.containsKey(destinationType);
    }

    public Set<Integer> getDestinations(String destinationType) {
        return destinations.get(destinationType);
    }

    public void refresh() {
        calculationCutoffMillis = config.calculationCutoff() * Constants.GAME_TICK_LENGTH;
        avoidWilderness = ShortestPathPlugin.override("avoidWilderness", config.avoidWilderness());
        useAgilityShortcuts = ShortestPathPlugin.override("useAgilityShortcuts", config.useAgilityShortcuts());
        useGrappleShortcuts = ShortestPathPlugin.override("useGrappleShortcuts", config.useGrappleShortcuts());
        useBoats = ShortestPathPlugin.override("useBoats", config.useBoats());
        useCanoes = ShortestPathPlugin.override("useCanoes", config.useCanoes());
        useCharterShips = ShortestPathPlugin.override("useCharterShips", config.useCharterShips());
        useShips = ShortestPathPlugin.override("useShips", config.useShips());
        useFairyRings = ShortestPathPlugin.override("useFairyRings", config.useFairyRings());
        useGnomeGliders = ShortestPathPlugin.override("useGnomeGliders", config.useGnomeGliders());
        useHotAirBalloons = ShortestPathPlugin.override("useHotAirBalloons", config.useHotAirBalloons());
        useMagicCarpets = ShortestPathPlugin.override("useMagicCarpets", config.useMagicCarpets());
        useMagicMushtrees = ShortestPathPlugin.override("useMagicMushtrees", config.useMagicMushtrees());
        useMinecarts = ShortestPathPlugin.override("useMinecarts", config.useMinecarts());
        useQuetzals = ShortestPathPlugin.override("useQuetzals", config.useQuetzals());
        useSeasonalTransports = ShortestPathPlugin.override("useSeasonalTransports", config.useSeasonalTransports());
        useSpiritTrees = ShortestPathPlugin.override("useSpiritTrees", config.useSpiritTrees());
        useTeleportationItems = ShortestPathPlugin.override("useTeleportationItems", config.useTeleportationItems());
        useTeleportationBoxes = ShortestPathPlugin.override("useTeleportationBoxes", config.useTeleportationBoxes());
        useTeleportationLevers = ShortestPathPlugin.override("useTeleportationLevers", config.useTeleportationLevers());
        useTeleportationMinigames = ShortestPathPlugin.override("useTeleportationMinigames", config.useTeleportationMinigames());
        useTeleportationPortals = ShortestPathPlugin.override("useTeleportationPortals", config.useTeleportationPortals());
        useTeleportationSpells = ShortestPathPlugin.override("useTeleportationSpells", config.useTeleportationSpells());
        useWildernessObelisks = ShortestPathPlugin.override("useWildernessObelisks", config.useWildernessObelisks());
        currencyThreshold = ShortestPathPlugin.override("currencyThreshold", config.currencyThreshold());

        if (GameState.LOGGED_IN.equals(client.getGameState())) {
            int i = 0;
            for (; i < Skill.values().length; i++) {
                boostedSkillLevelsAndMore[i] = client.getBoostedSkillLevel(Skill.values()[i]);
            }
            boostedSkillLevelsAndMore[i++] = client.getTotalLevel(); // skill total level
            boostedSkillLevelsAndMore[i++] = getCombatLevel(); // combat level
            boostedSkillLevelsAndMore[i++] = client.getVarpValue(VarPlayerID.QP); // quest points

            refreshTransports();
        }

        refreshDestinations();
    }

    /** Specialized method for only updating player-held item and spell transports */
    public void refreshTeleports(int packedLocation, int wildernessLevel) {
        Set<Transport> usableWildyTeleports = new HashSet<>(usableTeleports.size());

        for (Transport teleport : usableTeleports) {
            if (wildernessLevel <= teleport.getMaxWildernessLevel()) {
                usableWildyTeleports.add(teleport);
            }
        }

        if (!usableWildyTeleports.isEmpty()) {
            Set<Transport> oldTransports = transports.getOrDefault(packedLocation, new HashSet<>());
            oldTransports.addAll(usableWildyTeleports);
            transports.put(packedLocation, oldTransports);
            transportsPacked.put(packedLocation, usableWildyTeleports); // appends for collections
        }
    }

    private void refreshDestinations() {
        destinations = avoidWilderness ? filteredDestinations : allDestinations;
    }

    /** Changes to the config might have invalidated some locations, e.g. those in the wilderness */
    public void filterLocations(Set<Integer> locations, boolean canReviveFiltered) {
        if (avoidWilderness) {
            locations.removeIf(location -> {
                boolean inWilderness = WildernessChecker.isInWilderness(location);
                if (inWilderness) {
                    filteredTargets.add(location);
                }
                return inWilderness;
            });
            // If we ended up with no valid locations we re-include the filtered locations
            if (locations.isEmpty()) {
                locations.addAll(filteredTargets);
                filteredTargets.clear();
            }
        } else if (canReviveFiltered) { // Re-include previously filtered locations
            locations.addAll(filteredTargets);
            filteredTargets.clear();
        }
    }

    private Map<String, Set<Integer>> filterDestinations(Map<String, Set<Integer>> allDestinations) {
        Map<String, Set<Integer>> filteredDestinations = new HashMap<>(allDestinations.size());
        for (Map.Entry<String, Set<Integer>> entry : allDestinations.entrySet()) {
            String destinationType = entry.getKey();
            Set<Integer> usableDestinations = new HashSet<>(entry.getValue().size());
            boolean isDifferent = false;
            for (Integer destination : entry.getValue()) {
                // We filter based on whether the destination is inside or outside wilderness
                if (!WildernessChecker.isInWilderness(destination)) {
                    usableDestinations.add(destination);
                    isDifferent = true;
                }
            }
            // If all destinations of a destination type have been filtered away then we don't add the entry
            if (!usableDestinations.isEmpty()) {
                // If no destinations of a destination type have been filtered away then we re-use the same set reference
                filteredDestinations.put(destinationType, isDifferent ? usableDestinations : entry.getValue());
            }
        }
        return filteredDestinations;
    }

    private void refreshTransports() {
        if (!Thread.currentThread().equals(client.getClientThread())) {
            return; // Has to run on the client thread; data will be refreshed when path finding commences
        }

        useFairyRings &= ((client.getVarbitValue(VarbitID.FAIRY2_QUEENCURE_QUEST) > 39)
            && (client.getVarbitValue(VarbitID.LUMBRIDGE_DIARY_ELITE_COMPLETE) == 1 || hasRequiredItems(DRAMEN_STAFF)));
        useGnomeGliders &= QuestState.FINISHED.equals(getQuestState(Quest.THE_GRAND_TREE));
        useMagicMushtrees &= QuestState.FINISHED.equals(getQuestState(Quest.BONE_VOYAGE));
        useSpiritTrees &= QuestState.FINISHED.equals(getQuestState(Quest.TREE_GNOME_VILLAGE));

        transports.clear();
        transportsPacked.clear();
        usableTeleports.clear();
        for (Map.Entry<Integer, Set<Transport>> entry : allTransports.entrySet()) {
            int point = entry.getKey();
            Set<Transport> usableTransports = new HashSet<>(entry.getValue().size());
            for (Transport transport : entry.getValue()) {
                for (Quest quest : transport.getQuests()) {
                    try {
                        questStates.put(quest, getQuestState(quest));
                    } catch (NullPointerException ignored) {
                    }
                }

                for (TransportVarbit varbitRequirement : transport.getVarbits()) {
                    varbitValues.put(varbitRequirement.getId(), client.getVarbitValue(varbitRequirement.getId()));
                }
                for (TransportVarPlayer varPlayerRequirement : transport.getVarPlayers()) {
                    varPlayerValues.put(varPlayerRequirement.getId(), client.getVarpValue(varPlayerRequirement.getId()));
                }

                if (useTransport(transport) && hasRequiredItems(transport)) {
                    if (point == WorldPointUtil.UNDEFINED) {
                        usableTeleports.add(transport);
                    } else {
                        usableTransports.add(transport);
                    }
                }
            }

            if (point != WorldPointUtil.UNDEFINED && !usableTransports.isEmpty()) {
                transports.put(point, usableTransports);
                transportsPacked.put(point, usableTransports);
            }
        }
    }

    public boolean avoidWilderness(int packedPosition, int packedNeighborPosition, boolean targetInWilderness) {
        return avoidWilderness
                && !targetInWilderness
                && !WildernessChecker.isInWilderness(packedPosition)
                && WildernessChecker.isInWilderness(packedNeighborPosition);
    }

    public QuestState getQuestState(Quest quest) {
        return quest.getState(client);
    }

    private boolean completedQuests(Transport transport) {
        for (Quest quest : transport.getQuests()) {
            if (!QuestState.FINISHED.equals(questStates.getOrDefault(quest, QuestState.NOT_STARTED))) {
                return false;
            }
        }
        return true;
    }

    public boolean varbitChecks(Transport transport) {
        for (TransportVarbit varbitRequirement : transport.getVarbits()) {
            if (!varbitRequirement.check(varbitValues)) {
                return false;
            }
        }
        return true;
    }

    public boolean varPlayerChecks(Transport transport) {
        for (TransportVarPlayer varPlayerRequirement : transport.getVarPlayers()) {
            if (!varPlayerRequirement.check(varPlayerValues)) {
                return false;
            }
        }
        return true;
    }

    private boolean useTransport(Transport transport) {
        final boolean isQuestLocked = transport.isQuestLocked();

        TransportType type = transport.getType();

        if (AGILITY_SHORTCUT.equals(type) && !useAgilityShortcuts) {
            return false;
        } else if (GRAPPLE_SHORTCUT.equals(type) && !useGrappleShortcuts) {
            return false;
        } else if (BOAT.equals(type) && !useBoats) {
            return false;
        } else if (CANOE.equals(type) && !useCanoes) {
            return false;
        } else if (CHARTER_SHIP.equals(type) && !useCharterShips) {
            return false;
        } else if (SHIP.equals(type) && !useShips) {
            return false;
        } else if (FAIRY_RING.equals(type) && !useFairyRings) {
            return false;
        } else if (GNOME_GLIDER.equals(type) && !useGnomeGliders) {
            return false;
        } else if (HOT_AIR_BALLOON.equals(type) && !useHotAirBalloons) {
            return false;
        } else if (MAGIC_CARPET.equals(type) && !useMagicCarpets) {
            return false;
        } else if (MAGIC_MUSHTREE.equals(type) && !useMagicMushtrees) {
            return false;
        } else if (MINECART.equals(type) && !useMinecarts) {
            return false;
        } else if (QUETZAL.equals(type) && !useQuetzals) {
            return false;
        } else if (SEASONAL_TRANSPORTS.equals(type)) {
            if (!useSeasonalTransports) {
                return false;
            }
            switch (useTeleportationItems) {
                case ALL:
                    return true;
                case ALL_NON_CONSUMABLE:
                    return !transport.isConsumable();
                case UNLOCKED:
                case INVENTORY:
                case INVENTORY_AND_BANK:
                    break;
                case NONE:
                    return false;
                case UNLOCKED_NON_CONSUMABLE:
                case INVENTORY_NON_CONSUMABLE:
                case INVENTORY_AND_BANK_NON_CONSUMABLE:
                    if (transport.isConsumable()) {
                        return false;
                    }
                    break;
            }
        } else if (SPIRIT_TREE.equals(type) && !useSpiritTrees) {
            return false;
        } else if (TELEPORTATION_ITEM.equals(type)) {
            switch (useTeleportationItems) {
                case ALL:
                    return true;
                case ALL_NON_CONSUMABLE:
                    return !transport.isConsumable();
                case UNLOCKED:
                case INVENTORY:
                case INVENTORY_AND_BANK:
                    break;
                case NONE:
                    return false;
                case UNLOCKED_NON_CONSUMABLE:
                case INVENTORY_NON_CONSUMABLE:
                case INVENTORY_AND_BANK_NON_CONSUMABLE:
                    if (transport.isConsumable()) {
                        return false;
                    }
                    break;
            }
        } else if (TELEPORTATION_BOX.equals(type) && !useTeleportationBoxes) {
            return false;
        } else if (TELEPORTATION_LEVER.equals(type) && !useTeleportationLevers) {
            return false;
        } else if (TELEPORTATION_MINIGAME.equals(type) && !useTeleportationMinigames) {
            return false;
        } else if (TELEPORTATION_PORTAL.equals(type) && !useTeleportationPortals) {
            return false;
        } else if (TELEPORTATION_SPELL.equals(type) && !useTeleportationSpells) {
            return false;
        } else if (WILDERNESS_OBELISK.equals(type) && !useWildernessObelisks) {
            return false;
        }

        if (!hasRequiredLevels(transport)) {
            return false;
        }

        if (isQuestLocked && !completedQuests(transport)) {
            return false;
        }

        if (!varbitChecks(transport)) {
            return false;
        }

        if (!varPlayerChecks(transport)) {
            return false;
        }

        return true;
    }

    /** Checks if the player has all the required skill levels for the transport */
    private boolean hasRequiredLevels(Transport transport) {
        int[] requiredLevels = transport.getSkillLevels();
        for (int i = 0; i < boostedSkillLevelsAndMore.length; i++) {
            int boostedLevel = boostedSkillLevelsAndMore[i];
            int requiredLevel = requiredLevels[i];
            if (boostedLevel < requiredLevel) {
                return false;
            }
        }
        return true;
    }

    /** Checks if the player has all the required equipment and inventory items for the transport */
    private boolean hasRequiredItems(Transport transport) {
        if (TransportType.TELEPORTATION_ITEM.equals(transport.getType()) ||
            TransportType.SEASONAL_TRANSPORTS.equals(transport.getType())) {
            switch (useTeleportationItems) {
                case ALL:
                case ALL_NON_CONSUMABLE:
                case UNLOCKED:
                case UNLOCKED_NON_CONSUMABLE:
                    return true;
                case NONE:
                    return false;
                default:
                    break;
            }
        }
        return hasRequiredItems(transport.getItemRequirements());
    }

    /** Checks if the player has all the required equipment and inventory items for the transport */
    private boolean hasRequiredItems(TransportItems transportItems) {
        if (transportItems == null) {
            return true;
        }
        itemsAndQuantities.clear();

        ItemContainer inventory = client.getItemContainer(InventoryID.INV);
        ItemContainer equipment = client.getItemContainer(InventoryID.WORN);

        if (inventory != null) {
            for (Item item : inventory.getItems()) {
                if (item.getId() >= 0 && item.getQuantity() > 0) {
                    itemsAndQuantities.put(item.getId(), item.getQuantity());
                }
            }
        }
        if (equipment != null) {
            for (Item item : equipment.getItems()) {
                if (item.getId() >= 0 && item.getQuantity() > 0) {
                    itemsAndQuantities.put(item.getId(), item.getQuantity());
                }
            }
        }
        if (bank != null
            && (TeleportationItem.INVENTORY_AND_BANK.equals(useTeleportationItems)
            || TeleportationItem.INVENTORY_AND_BANK_NON_CONSUMABLE.equals(useTeleportationItems))) {
            for (Item item : bank.getItems()) {
                if (item.getId() >= 0 && item.getQuantity() > 0) {
                    itemsAndQuantities.put(item.getId(), item.getQuantity());
                }
            }
        }
        if (RUNE_POUCHES.stream().anyMatch(runePouch -> itemsAndQuantities.containsKey(runePouch))) {
            EnumComposition runePouchEnum = client.getEnum(EnumID.RUNEPOUCH_RUNE);
            for (int i = 0; i < RUNE_POUCH_RUNE_VARBITS.length; i++) {
                int runeEnumId = client.getVarbitValue(RUNE_POUCH_RUNE_VARBITS[i]);
                int runeId = runeEnumId > 0 ? runePouchEnum.getIntValue(runeEnumId) : 0;
                int runeAmount = client.getVarbitValue(RUNE_POUCH_AMOUNT_VARBITS[i]);
                if (runeId > 0 && runeAmount > 0) {
                    itemsAndQuantities.put(runeId, runeAmount);
                }
            }
        }
        boolean usingStaff = false;
        boolean usingOffhand = false;
        for (int i = 0; i < transportItems.getItems().length; i++) {
            boolean missing = true;
            if (transportItems.getItems()[i] != null) {
                for (int itemId : transportItems.getItems()[i]) {
                    int quantity = itemsAndQuantities.getOrDefault(itemId, 0);
                    int requiredQuantity = transportItems.getQuantities()[i];
                    if (requiredQuantity > 0 && quantity >= requiredQuantity || requiredQuantity == 0 && quantity == 0) {
                        if (CURRENCIES.contains(itemId) && requiredQuantity > currencyThreshold) {
                            return false;
                        }
                        missing = false;
                        break;
                    }
                }
            }
            if (missing && !usingStaff && transportItems.getStaves()[i] != null) {
                for (int itemId : transportItems.getStaves()[i]) {
                    int quantity = itemsAndQuantities.getOrDefault(itemId, 0);
                    int requiredQuantity = transportItems.getQuantities()[i];
                    if (requiredQuantity > 0 && quantity >= 1 || requiredQuantity == 0 && quantity == 0) {
                        usingStaff = true;
                        missing = false;
                        break;
                    }
                }
            }
            if (missing && !usingOffhand && transportItems.getOffhands()[i] != null) {
                for (int itemId : transportItems.getOffhands()[i]) {
                    int quantity = itemsAndQuantities.getOrDefault(itemId, 0);
                    int requiredQuantity = transportItems.getQuantities()[i];
                    if (requiredQuantity > 0 && quantity >= 1 || requiredQuantity == 0 && quantity == 0) {
                        usingOffhand = true;
                        missing = false;
                        break;
                    }
                }
            }
            if (missing) {
                return false;
            }
        }
        return true;
    }

    /** Calculates the combat level of the player */
    private int getCombatLevel() {
        int attack = client.getRealSkillLevel(Skill.ATTACK);
        int strength = client.getRealSkillLevel(Skill.STRENGTH);
        int defence = client.getRealSkillLevel(Skill.DEFENCE);
        int hitpoints = client.getRealSkillLevel(Skill.HITPOINTS);
        int magic = client.getRealSkillLevel(Skill.MAGIC);
        int ranged = client.getRealSkillLevel(Skill.RANGED);
        int prayer = client.getRealSkillLevel(Skill.PRAYER);
        double base = 0.25 * (defence + hitpoints + (prayer) / 2);
        double melee = (13 * (attack + strength)) / 40.0;
        double range = (13 * ((3 * ranged) / 2)) / 40.0;
        double mage = (13 * ((3 * magic) / 2)) / 40.0;
        int combatLevel = (int) Math.floor(base + Math.max(Math.max(melee, range), Math.max(melee, mage)));
        return combatLevel;
    }
}
