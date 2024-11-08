package shortestpath;

import lombok.Getter;

@Getter
public class TransportVarPlayer {
    final int id;
    final int value;

    public TransportVarPlayer(int id, int value) {
        this.id = id;
        this.value = value;
    }
}
