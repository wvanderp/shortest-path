package shortestpath;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.GameState;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

/**
 * Service class that manages the Player instance and handles all player data operations.
 * This serves as the single source of truth for player information, acting as an
 * intermediary between the RuneLite client and the plugin components.
 */
@Slf4j
public class PlayerService {
    
    private Client client;
    
    @Getter
    private final Player player;
    
    // Rune pouch varbits for inventory checking
    private static final int[] RUNE_POUCH_RUNE_VARBITS = {
        VarbitID.RUNE_POUCH_TYPE_1,
        VarbitID.RUNE_POUCH_TYPE_2,
        VarbitID.RUNE_POUCH_TYPE_3,
        VarbitID.RUNE_POUCH_TYPE_4
    };
    
    private static final int[] RUNE_POUCH_AMOUNT_VARBITS = {
        VarbitID.RUNE_POUCH_QUANTITY_1,
        VarbitID.RUNE_POUCH_QUANTITY_2,
        VarbitID.RUNE_POUCH_QUANTITY_3,
        VarbitID.RUNE_POUCH_QUANTITY_4
    };
    
    public PlayerService(Client client) {
        this.client = client;
        this.player = new Player();
    }
    
    /**
     * Sets the client instance. This is called by the plugin during injection.
     */
    public void setClient(Client client) {
        this.client = client;
    }
    
    /**
     * Gets the RuneLite client instance safely.
     */
    private Client getClient() {
        return client;
    }
    
    /**
     * Performs a full refresh of all player data from the client.
     * This should be called when the player logs in or when we need
     * to ensure all data is up to date.
     */
    public void refreshAllPlayerData() {
        Client client = getClient();
        if (client == null) {
            log.debug("Client not available for player data refresh");
            return;
        }
        
        // Update basic game state
        player.setGameState(client.getGameState());
        
        if (!player.isLoggedIn()) {
            log.debug("Player not logged in, clearing player data");
            player.reset();
            return;
        }
        
        try {
            // Update player location
            if (client.getLocalPlayer() != null) {
                player.setWorldLocation(client.getLocalPlayer().getWorldLocation());
            }
            
            // Update all skill levels
            refreshSkillLevels();
            
            // Update quest points
            player.setQuestPoints(client.getVarpValue(VarPlayerID.QP));
            
            // Update item containers
            refreshItemContainers();
            
            log.debug("Successfully refreshed all player data");
            
        } catch (Exception e) {
            log.error("Error refreshing player data", e);
        }
    }
    
    /**
     * Refreshes only skill-related data.
     */
    public void refreshSkillLevels() {
        Client client = getClient();
        if (client == null || !player.isLoggedIn()) {
            return;
        }
        
        try {
            // Update individual skill levels
            for (Skill skill : Skill.values()) {
                player.setBoostedSkillLevel(skill, client.getBoostedSkillLevel(skill));
                player.setRealSkillLevel(skill, client.getRealSkillLevel(skill));
            }
            
        } catch (Exception e) {
            log.error("Error refreshing skill levels", e);
        }
    }
    
    /**
     * Refreshes item container data (inventory, equipment, bank).
     */
    public void refreshItemContainers() {
        Client client = getClient();
        if (client == null || !player.isLoggedIn()) {
            return;
        }
        
        try {
            // Update inventory
            player.setInventory(client.getItemContainer(InventoryID.INV));
            
            // Update equipment
            player.setEquipment(client.getItemContainer(InventoryID.WORN));
            
            // Update bank (may be null if not open)
            player.setBank(client.getItemContainer(InventoryID.BANK));
            
        } catch (Exception e) {
            log.error("Error refreshing item containers", e);
        }
    }
    
    /**
     * Updates a specific varbit value in the player data.
     */
    public void updateVarbit(int varbitId, int value) {
        player.setVarbitValue(varbitId, value);
    }
    
    /**
     * Updates a specific varplayer value in the player data.
     */
    public void updateVarPlayer(int varPlayerId, int value) {
        player.setVarPlayerValue(varPlayerId, value);
    }
    
    /**
     * Refreshes a specific varbit from the client.
     */
    public void refreshVarbit(int varbitId) {
        Client client = getClient();
        if (client != null && player.isLoggedIn()) {
            try {
                int value = client.getVarbitValue(varbitId);
                player.setVarbitValue(varbitId, value);
            } catch (Exception e) {
                log.error("Error refreshing varbit {}", varbitId, e);
            }
        }
    }
    
    /**
     * Refreshes a specific varplayer from the client.
     */
    public void refreshVarPlayer(int varPlayerId) {
        Client client = getClient();
        if (client != null && player.isLoggedIn()) {
            try {
                int value = client.getVarpValue(varPlayerId);
                player.setVarPlayerValue(varPlayerId, value);
            } catch (Exception e) {
                log.error("Error refreshing varplayer {}", varPlayerId, e);
            }
        }
    }
    
    /**
     * Updates the quest state for a specific quest.
     */
    public void updateQuestState(Quest quest) {
        Client client = getClient();
        if (client != null && player.isLoggedIn() && quest != null) {
            try {
                QuestState state = quest.getState(client);
                player.setQuestState(quest, state);
            } catch (Exception e) {
                log.error("Error updating quest state for {}", quest, e);
            }
        }
    }
    
    /**
     * Updates the player's location.
     */
    public void updatePlayerLocation() {
        Client client = getClient();
        if (client != null && player.isLoggedIn() && client.getLocalPlayer() != null) {
            player.setWorldLocation(client.getLocalPlayer().getWorldLocation());
        }
    }
    
    /**
     * Updates the game state.
     */
    public void updateGameState(GameState gameState) {
        player.setGameState(gameState);
        
        // If logging out, reset player data
        if (
            gameState != GameState.LOGGED_IN 
            && gameState != GameState.LOADING 
            && gameState != GameState.HOPPING
        ) {
                player.reset();
        }
    }
    
    
    /**
     * Checks if the player has access to rune pouch runes.
     * This mirrors the logic in PathfinderConfig.
     */
    public boolean checkRunePouchRunes() {
        Client client = getClient();
        if (client == null || !player.isLoggedIn()) {
            return false;
        }
        
        try {
            EnumComposition runePouchEnum = client.getEnum(EnumID.RUNEPOUCH_RUNE);
            if (runePouchEnum != null) {
                for (int i = 0; i < RUNE_POUCH_RUNE_VARBITS.length; i++) {
                    int runeAmount = player.getVarbitValue(RUNE_POUCH_AMOUNT_VARBITS[i]);
                    
                    if (runeAmount > 0) {
                        // Could extend this to track specific rune types and quantities
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error checking rune pouch runes", e);
        }
        
        return false;
    }
}
