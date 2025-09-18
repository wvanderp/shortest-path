package shortestpath;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the current state of the player character, including all relevant
 * information needed for pathfinding and transport calculations.
 * This class serves as a data container and cache for player information.
 */
@Data
public class Player {
    
    // Game state
    @Getter @Setter
    private GameState gameState = GameState.UNKNOWN;
    
    // Player location
    @Getter @Setter
    private WorldPoint worldLocation;
    
    // Skill levels - using array index matching Skill enum ordinals
    private final int[] boostedSkillLevels = new int[Skill.values().length];
    private final int[] realSkillLevels = new int[Skill.values().length];
    
    // Additional computed values that are commonly needed
    // `totalLevel` is computed on-the-fly from `realSkillLevels`.
    public int getTotalLevel()
    {
        int total = 0;
        for (int lvl : realSkillLevels)
        {
            total += lvl;
        }
        return total;
    }
    
    @Getter @Setter
    private int questPoints;
    
    // Quest completion states
    private final Map<Quest, QuestState> questStates = new HashMap<>();
    
    // Varbits and VarPlayers for transport and requirement checking
    private final Map<Integer, Integer> varbitValues = new HashMap<>();
    private final Map<Integer, Integer> varPlayerValues = new HashMap<>();
    
    // Item containers
    @Getter @Setter
    private ItemContainer inventory;
    
    @Getter @Setter
    private ItemContainer equipment;
    
    @Getter @Setter
    private ItemContainer bank;
    
    // Convenience method to get boosted skill level
    public int getBoostedSkillLevel(Skill skill) {
        return boostedSkillLevels[skill.ordinal()];
    }
    
    // Convenience method to set boosted skill level
    public void setBoostedSkillLevel(Skill skill, int level) {
        boostedSkillLevels[skill.ordinal()] = level;
    }
    
    // Convenience method to get real skill level
    public int getRealSkillLevel(Skill skill) {
        return realSkillLevels[skill.ordinal()];
    }
    
    // Convenience method to set real skill level
    public void setRealSkillLevel(Skill skill, int level) {
        realSkillLevels[skill.ordinal()] = level;
    }
    
    // Quest state management
    public QuestState getQuestState(Quest quest) {
        return questStates.getOrDefault(quest, QuestState.NOT_STARTED);
    }
    
    public void setQuestState(Quest quest, QuestState state) {
        if (state == null) {
            questStates.remove(quest);
        } else {
            questStates.put(quest, state);
        }
    }
    
    public Map<Quest, QuestState> getQuestStates() {
        return new HashMap<>(questStates);
    }
    
    public void setQuestStates(Map<Quest, QuestState> states) {
        questStates.clear();
        if (states != null) {
            questStates.putAll(states);
        }
    }
    
    // Varbit management
    public int getVarbitValue(int varbitId) {
        return varbitValues.getOrDefault(varbitId, 0);
    }
    
    public void setVarbitValue(int varbitId, int value) {
        varbitValues.put(varbitId, value);
    }
    
    public Map<Integer, Integer> getVarbitValues() {
        return new HashMap<>(varbitValues);
    }
    
    public void setVarbitValues(Map<Integer, Integer> values) {
        varbitValues.clear();
        if (values != null) {
            varbitValues.putAll(values);
        }
    }
    
    // VarPlayer management
    public int getVarPlayerValue(int varPlayerId) {
        return varPlayerValues.getOrDefault(varPlayerId, 0);
    }
    
    public void setVarPlayerValue(int varPlayerId, int value) {
        varPlayerValues.put(varPlayerId, value);
    }
    
    public Map<Integer, Integer> getVarPlayerValues() {
        return new HashMap<>(varPlayerValues);
    }
    
    public void setVarPlayerValues(Map<Integer, Integer> values) {
        varPlayerValues.clear();
        if (values != null) {
            varPlayerValues.putAll(values);
        }
    }
    
    // Utility methods for inventory checks
    public boolean hasItem(int itemId) {
        if (inventory != null) {
            for (Item item : inventory.getItems()) {
                if (item.getId() == itemId) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean hasItemInEquipment(int itemId) {
        if (equipment != null) {
            for (Item item : equipment.getItems()) {
                if (item.getId() == itemId) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean hasItemInBank(int itemId) {
        if (bank != null) {
            for (Item item : bank.getItems()) {
                if (item.getId() == itemId) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public int getItemQuantity(int itemId) {
        int quantity = 0;
        if (inventory != null) {
            for (Item item : inventory.getItems()) {
                if (item.getId() == itemId) {
                    quantity += item.getQuantity();
                }
            }
        }
        return quantity;
    }
    
    public int getItemQuantityInEquipment(int itemId) {
        int quantity = 0;
        if (equipment != null) {
            for (Item item : equipment.getItems()) {
                if (item.getId() == itemId) {
                    quantity += item.getQuantity();
                }
            }
        }
        return quantity;
    }
    
    public int getItemQuantityInBank(int itemId) {
        int quantity = 0;
        if (bank != null) {
            for (Item item : bank.getItems()) {
                if (item.getId() == itemId) {
                    quantity += item.getQuantity();
                }
            }
        }
        return quantity;
    }

    /**
     * Calculates the combat level based on current skill levels.
     * This mirrors the calculation in PathfinderConfig.
     */
    public int getCombatLevel() {
        int attack = this.getRealSkillLevel(Skill.ATTACK);
        int strength = this.getRealSkillLevel(Skill.STRENGTH);
        int defence = this.getRealSkillLevel(Skill.DEFENCE);
        int hitpoints = this.getRealSkillLevel(Skill.HITPOINTS);
        int magic = this.getRealSkillLevel(Skill.MAGIC);
        int ranged = this.getRealSkillLevel(Skill.RANGED);
        int prayer = this.getRealSkillLevel(Skill.PRAYER);
        
        double base = 0.25 * (defence + hitpoints + (prayer / 2.0));
        double melee = (13 * (attack + strength)) / 40.0;
        double range = (13 * ((3 * ranged) / 2.0)) / 40.0;
        double mage = (13 * ((3 * magic) / 2.0)) / 40.0;
        
        return (int) Math.floor(base + Math.max(Math.max(melee, range), Math.max(melee, mage)));
    }
    
    // Check if player is logged in
    public boolean isLoggedIn() {
        return GameState.LOGGED_IN.equals(gameState);
    }
    
    // Reset all player data (useful for logout/login)
    public void reset() {
        gameState = GameState.UNKNOWN;
        worldLocation = null;
        
        // Reset skill levels
        for (int i = 0; i < boostedSkillLevels.length; i++) {
            boostedSkillLevels[i] = 0;
            realSkillLevels[i] = 0;
        }
        
        // totalLevel is computed dynamically; nothing to reset.
        questPoints = 0;
        
        // Clear collections
        questStates.clear();
        varbitValues.clear();
        varPlayerValues.clear();
        
        // Clear item containers
        inventory = null;
        equipment = null;
        bank = null;
    }
}
