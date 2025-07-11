package shortestpath.pathfinder;

import shortestpath.Destination;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DestinationManager {

    /** Reference that points to either allDestinations or filteredDestinations */
    private Map<String, Set<Integer>> destinations;

    private final Map<String, Set<Integer>> allDestinations;

    private final Map<String, Set<Integer>> filteredDestinations;

    private final RoutingConfig routingConfig;

    public DestinationManager(RoutingConfig routingConfig) {
        this.allDestinations = Destination.loadAllFromResources();
        this.filteredDestinations = filterDestinations(allDestinations);
        this.destinations = allDestinations;

        this.routingConfig = routingConfig;
    }

    public boolean hasDestination(String destinationType) {
        return destinations.containsKey(destinationType);
    }

    public Set<Integer> getDestinations(String destinationType) {
        return destinations.get(destinationType);
    }

    public void refresh() {
        destinations = routingConfig.avoidWilderness ? filteredDestinations : allDestinations;
    }

    private Map<String, Set<Integer>> filterDestinations(Map<String, Set<Integer>> allDestinations) {
        Map<String, Set<Integer>> filteredDestinations = new HashMap<>(allDestinations.size());
        for (Map.Entry<String, Set<Integer>> entry : allDestinations.entrySet()) {
            String destinationType = entry.getKey();
            Set<Integer> usableDestinations = new HashSet<>(entry.getValue().size());
            boolean isDifferent = false;
            for (Integer destination : entry.getValue()) {
                if (!WildernessChecker.isInWilderness(destination)) {
                    usableDestinations.add(destination);
                    isDifferent = true;
                }
            }
            if (!usableDestinations.isEmpty()) {
                filteredDestinations.put(destinationType, isDifferent ? usableDestinations : entry.getValue());
            }
        }
        return filteredDestinations;
    }

}
