package shortestpath;

import java.util.Map;
import java.util.Set;
import org.junit.Test;
import shortestpath.transport.Transport;
import shortestpath.transport.TransportLoader;
import shortestpath.transport.TransportType;

import static org.junit.Assert.assertEquals;

public class TransportCountTest {
    private static final Map<Integer, Set<Transport>> transports = TransportLoader.loadAllFromResources();

    @Test
    public void testNumberOfGnomeGliders() {
        int actualCount = 0;
        for (int origin : transports.keySet()) {
            for (Transport transport : transports.get(origin)) {
                if (TransportType.GNOME_GLIDER.equals(transport.getType())) {
                    actualCount++;
                }
            }
        }
        /*
         * Info:
         * NB: Lemanto Andra (Digsite) can only be destination and not origin
         * single_glider_origin_locations * (number_of_gnome_gliders - 1)
         * 1 * 6 // Ta Quir Priw (Gnome Stronghold)
         * + 3 * 6 // Gandius (Karamja)
         * + 3 * 6 // Kar-Hewo (Al-Kharid)
         * + 2 * 6 // Sindarpos (White Wolf Mountain)
         * + 3 * 6 // Lemantolly Undri (Feldip Hills)
         * + 3 * 6 // Ookookolly Undri (Ape Atoll)
         * = 90
         */
        assertEquals(90, actualCount);
    }

    @Test
    public void testNumberOfHotAirBalloons() {
        int actualCount = 0;
        for (int origin : transports.keySet()) {
            for (Transport transport : transports.get(origin)) {
                if (TransportType.HOT_AIR_BALLOON.equals(transport.getType())) {
                    actualCount++;
                }
            }
        }
        /*
         * Info:
         * single_hot_air_balloon_origin_locations * (number_of_hot_air_balloons - 1)
         * 6 * 5 // Entrana
         * + 8 * 5 // Taverley
         * + 8 * 5 // Crafting Guild
         * + 8 * 5 // Varrock
         * + 7 * 5 // Castle Wars
         * + 8 * 5 // Grand Tree
         * = 225
         */
        assertEquals(225, actualCount);
    }

    @Test
    public void testNumberOfMagicMushtrees() {
        int actualCount = 0;
        for (int origin : transports.keySet()) {
            for (Transport transport : transports.get(origin)) {
                if (TransportType.MAGIC_MUSHTREE.equals(transport.getType())) {
                    actualCount++;
                }
            }
        }
        /*
         * Info:
         * single_mushtree_origin_locations * (number_of_magic_mushtrees - 1)
         * 2 * 3 // House on the Hill
         * + 2 * 3 // Verdant Valley
         * + 2 * 3 // Sticky Swamp
         * + 2 * 3 // Mushroom Meadow
         * = 24
         */
        assertEquals(24, actualCount);
    }

    @Test
    public void testNumberOfQuetzals() {
        int actualCount = 0;
        for (int origin : transports.keySet()) {
            for (Transport transport : transports.get(origin)) {
                if (TransportType.QUETZAL.equals(transport.getType())) {
                    actualCount++;
                }
            }
        }
        /*
         * There are 14 quetzal locations, each can travel to 13 others (not itself).
         * - Aldarin
         * - Auburnvale
         * - Civitas illa Fortis
         * - Hunter Guild
         * - Quetzacalli Gorge
         * - Sunset Coast
         * - Tal Teklan
         * - The Teomat
         * - Cam Torum entrance
         * - Colossal Wyrm Remains
         * - Fortis Colosseum
         * - Kastori
         * - Outer Fortis
         * - Salvager Overlook
         * 
         * Calculation: 14 origins * 13 destinations = 182
         * 
         * Plus 2 special Primio routes:
         * - Varrock -> Civitas illa Fortis
         * - Civitas illa Fortis -> Varrock
         * 
         * = 182 + 2
         * = 184 total options
         */
        assertEquals(184, actualCount);
    }

    @Test
    public void testNumberOfSpiritTrees() {
        int actualCount = 0;
        for (int origin : transports.keySet()) {
            for (Transport transport : transports.get(origin)) {
                if (TransportType.SPIRIT_TREE.equals(transport.getType())) {
                    actualCount++;
                }
            }
        }
        /*
         * Info:
         * single_tree_origin_locations * (number_of_spirit_trees - 1)
         * 15 * 11 // Tree Gnome Village
         * + 14 * 11 // Gnome Stronghold
         * + 8 * 11 // Battlefield of Khazard
         * + 8 * 11 // Grand Exchange
         * + 8 * 11 // Feldip Hills
         * + 7 * 11 // Prifddinas
         * + 12 * 11 // Port Sarim
         * + 12 * 11 // Etceteria
         * + 12 * 11 // Brimhaven
         * + 12 * 11 // Hosidius
         * + 12 * 11 // Farming Guild
         * + 0 * 11 // Player-owned house
         * + 12 * 11 // Poison Waste
         * = 1452
         */
        assertEquals(1452, actualCount);
    }

    @Test
    public void testNumberOfCharterShips() {
        int actualCount = 0;
        for (int origin : transports.keySet()) {
            for (Transport transport : transports.get(origin)) {
                if (TransportType.CHARTER_SHIP.equals(transport.getType())) {
                    actualCount++;
                }
            }
        }
        /*
         * Info:
         * There are currently 16 unique charter ship origin/destinations.
         * If every combination was possible then it would be 16^2 = 256.
         * It is impossible to travel from and to the same place, so subtract 16.
         * It is also impossible to travel between certain places, presumably
         * because the distance between them is too small. Currently 12 of these.
         */
        assertEquals(16 * 16 - 16 - 12, actualCount);
    }
}
