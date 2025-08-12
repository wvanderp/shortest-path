package pathfinder;

import java.util.List;
import java.util.ArrayList;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import shortestpath.ShortestPathPlugin;

public class ShortestPathPluginTest {
    public static void main(String[] args) throws Exception {

        List<String> argList = new ArrayList<>();
        java.util.Collections.addAll(argList, args);
        argList.add("--developer-mode");
        String[] modifiedArgs = argList.toArray(new String[0]);

        ExternalPluginManager.loadBuiltin(ShortestPathPlugin.class);
        RuneLite.main(modifiedArgs);
    }
}
