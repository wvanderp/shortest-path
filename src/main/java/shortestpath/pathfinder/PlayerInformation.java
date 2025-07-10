package shortestpath.pathfinder;

import static shortestpath.TransportType.TELEPORTATION_LEVER;
import static shortestpath.TransportType.TELEPORTATION_MINIGAME;
import static shortestpath.TransportType.TELEPORTATION_PORTAL;
import static shortestpath.TransportType.TELEPORTATION_SPELL;
import static shortestpath.TransportType.WILDERNESS_OBELISK;
import static shortestpath.TransportType.AGILITY_SHORTCUT;
import static shortestpath.TransportType.GRAPPLE_SHORTCUT;
import static shortestpath.TransportType.BOAT;
import static shortestpath.TransportType.CANOE;
import static shortestpath.TransportType.CHARTER_SHIP;
import static shortestpath.TransportType.SHIP;
import static shortestpath.TransportType.FAIRY_RING;
import static shortestpath.TransportType.GNOME_GLIDER;
import static shortestpath.TransportType.HOT_AIR_BALLOON;
import static shortestpath.TransportType.MINECART;
import static shortestpath.TransportType.QUETZAL;
import static shortestpath.TransportType.SPIRIT_TREE;
import static shortestpath.TransportType.TELEPORTATION_ITEM;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import shortestpath.TeleportationItem;
import shortestpath.Transport;
import shortestpath.TransportItems;
import shortestpath.TransportType;
import shortestpath.TransportVarPlayer;
import shortestpath.TransportVarbit;

import java.util.Map;
import java.util.Set;

public class PlayerInformation {
        private static final List<Integer> RUNE_POUCHES = Arrays.asList(
        ItemID.RUNE_POUCH, ItemID.RUNE_POUCH_L,
        ItemID.DIVINE_RUNE_POUCH, ItemID.DIVINE_RUNE_POUCH_L
    );
    private static final int[] RUNE_POUCH_RUNE_VARBITS = {
        Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3, Varbits.RUNE_POUCH_RUNE4,
        Varbits.RUNE_POUCH_RUNE5, Varbits.RUNE_POUCH_RUNE6
	};
    private static final int[] RUNE_POUCH_AMOUNT_VARBITS = {
        Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3, Varbits.RUNE_POUCH_AMOUNT4,
        Varbits.RUNE_POUCH_AMOUNT5, Varbits.RUNE_POUCH_AMOUNT6
	};
    private static final Set<Integer> CURRENCIES = Set.of(
        ItemID.COINS_995, ItemID.TRADING_STICKS, ItemID.ECTOTOKEN, ItemID.WARRIOR_GUILD_TOKEN);


    private final Client client;
    private final int[] boostedLevels;
    public Map<Quest, QuestState> questStates;
    public Map<Integer, Integer> varbitValues;
    public Map<Integer, Integer> varPlayerValues;

    private final Map<Integer, Integer> itemsAndQuantities = new HashMap<>(28 + 11 + 4, 1.0f);

    private RoutingConfig routingConfig;

    public PlayerInformation(Client client, RoutingConfig routingConfig) {
        this.client = client;
        this.boostedLevels = new int[Skill.values().length];
        this.questStates = new HashMap<>();
        this.varbitValues = new HashMap<>();
        this.varPlayerValues = new HashMap<>();
        this.routingConfig = routingConfig;
    }

    public void refresh() {
        if (client.getGameState() == net.runelite.api.GameState.LOGGED_IN) {
            for (int i = 0; i < Skill.values().length; i++) {
                boostedLevels[i] = client.getBoostedSkillLevel(Skill.values()[i]);
            }
        }
    }

    /** Checks if the player has all the required skill levels for the transport */
    public boolean hasRequiredLevels(Transport transport) {
        int[] requiredLevels = transport.getSkillLevels();
        for (int i = 0; i < boostedLevels.length; i++) {
            int boostedLevel = boostedLevels[i];
            int requiredLevel = requiredLevels[i];
            if (boostedLevel < requiredLevel) {
                return false;
            }
        }
        return true;
    }

      /** Checks if the player has all the required equipment and inventory items for the transport */
    public boolean hasRequiredItems(Transport transport) {
        if ((TeleportationItem.ALL.equals(routingConfig.useTeleportationItems) ||
            TeleportationItem.ALL_NON_CONSUMABLE.equals(routingConfig.useTeleportationItems)) &&
            TransportType.TELEPORTATION_ITEM.equals(transport.getType())) {
            return true;
        }
        if (TeleportationItem.NONE.equals(routingConfig.useTeleportationItems) &&
            TransportType.TELEPORTATION_ITEM.equals(transport.getType())) {
            return false;
        }
        itemsAndQuantities.clear();
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
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
        TransportItems transportItems = transport.getItemRequirements();
        if (transportItems == null) {
            return true;
        }
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


    public boolean useTransport(Transport transport) {
        final boolean isQuestLocked = transport.isQuestLocked();

        if (!hasRequiredLevels(transport)) {
            return false;
        }

        TransportType type = transport.getType();

        if (AGILITY_SHORTCUT.equals(type) && !routingConfig.useAgilityShortcuts) {
            return false;
        } else if (GRAPPLE_SHORTCUT.equals(type) && !routingConfig.useGrappleShortcuts) {
            return false;
        } else if (BOAT.equals(type) && !routingConfig.useBoats) {
            return false;
        } else if (CANOE.equals(type) && !routingConfig.useCanoes) {
            return false;
        } else if (CHARTER_SHIP.equals(type) && !routingConfig.useCharterShips) {
            return false;
        } else if (SHIP.equals(type) && !routingConfig.useShips) {
            return false;
        } else if (FAIRY_RING.equals(type) && !routingConfig.useFairyRings) {
            return false;
        } else if (GNOME_GLIDER.equals(type) && !routingConfig.useGnomeGliders) {
            return false;
        } else if (HOT_AIR_BALLOON.equals(type) && !routingConfig.useHotAirBalloons) {
            return false;
        } else if (MINECART.equals(type) && !routingConfig.useMinecarts) {
            return false;
        } else if (QUETZAL.equals(type) && !routingConfig.useQuetzals) {
            return false;
        } else if (SPIRIT_TREE.equals(type) && !routingConfig.useSpiritTrees) {
            return false;
        } else if (TELEPORTATION_ITEM.equals(type)) {
            switch (routingConfig.useTeleportationItems) {
                case TeleportationItem.ALL:
                case TeleportationItem.INVENTORY:
                    break;
                case TeleportationItem.NONE:
                    return false;
                case TeleportationItem.INVENTORY_NON_CONSUMABLE:
                case TeleportationItem.ALL_NON_CONSUMABLE:
                    if (transport.isConsumable()) {
                        return false;
                    }
                    break;
            }
        } else if (TELEPORTATION_LEVER.equals(type) && !routingConfig.useTeleportationLevers) {
            return false;
        } else if (TELEPORTATION_MINIGAME.equals(type) && !routingConfig.useTeleportationMinigames) {
            return false;
        } else if (TELEPORTATION_PORTAL.equals(type) && !routingConfig.useTeleportationPortals) {
            return false;
        } else if (TELEPORTATION_SPELL.equals(type) && !routingConfig.useTeleportationSpells) {
            return false;
        } else if (WILDERNESS_OBELISK.equals(type) && !routingConfig.useWildernessObelisks) {
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


}
