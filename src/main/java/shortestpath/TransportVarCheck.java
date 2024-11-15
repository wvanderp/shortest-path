package shortestpath;

import lombok.Getter;

public enum TransportVarCheck {
    EQUAL("="),
    GREATER(">"),
    SMALLER("<"),
    ;

    @Getter
    private final String code;

    TransportVarCheck(String code) {
        this.code = code;
    }
}
