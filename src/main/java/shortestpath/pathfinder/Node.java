package shortestpath.pathfinder;

import shortestpath.PrimitiveIntList;
import shortestpath.WorldPointUtil;

public class Node {
    public final int packedPosition;
    public final Node previous;
    public final int cost;

    public Node(int packedPosition, Node previous, int cost) {
        this.packedPosition = packedPosition;
        this.previous = previous;
        this.cost = cost;
    }

    public Node(int packedPosition, Node previous) {
        this(packedPosition, previous, cost(packedPosition, previous));
    }

    public PrimitiveIntList getPath() {
        Node node = this;
        int n = 0;

        while (node != null) {
            node = node.previous;
            n++;
        }
        PrimitiveIntList path = new PrimitiveIntList(n, true);
        node = this;
        while (node != null) {
            path.set(--n, node.packedPosition);
            node = node.previous;
        }

        return path;
    }

    private static int cost(int packedPosition, Node previous) {
        int previousCost = 0;
        int travelTime = 0;

        if (previous != null) {
            previousCost = previous.cost;
            // Travel wait time in TransportNode and distance is compared as if the player is walking 1 tile/tick.
            // TODO: reduce the distance if the player is currently running and has enough run energy for the distance?
            travelTime = WorldPointUtil.distanceBetween(previous.packedPosition, packedPosition);
        }

        return previousCost + travelTime;
    }
}
