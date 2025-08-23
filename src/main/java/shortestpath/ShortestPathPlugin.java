package shortestpath;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.SpriteID;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.worldmap.WorldMap;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import shortestpath.pathfinder.CollisionMap;
import shortestpath.pathfinder.Pathfinder;
import shortestpath.pathfinder.PathfinderConfig;

@PluginDescriptor(
    name = "Shortest Path",
    description = "Draws the shortest path to a chosen destination on the map<br>" +
        "Right click on the world map or shift right click a tile to use",
    tags = {"pathfinder", "map", "waypoint", "navigation"}
)
public class ShortestPathPlugin extends Plugin {
    protected static final String CONFIG_GROUP = "shortestpath";
    private static final String PLUGIN_MESSAGE_PATH = "path";
    private static final String PLUGIN_MESSAGE_CLEAR = "clear";
    private static final String PLUGIN_MESSAGE_START = "start";
    private static final String PLUGIN_MESSAGE_TARGET = "target";
    private static final String PLUGIN_MESSAGE_CONFIG_OVERRIDE = "config";
    private static final String PLUGIN_MESSAGE_TRANSPORTS = "transports";
    private static final String CLEAR = "Clear";
    private static final String PATH = ColorUtil.wrapWithColorTag("Path", JagexColors.MENU_TARGET);
    private static final String SET = "Set";
    private static final String FIND_CLOSEST = "Find closest";
    private static final String FLASH_ICONS = "Flash icons";
    private static final String START = ColorUtil.wrapWithColorTag("Start", JagexColors.MENU_TARGET);
    private static final String TARGET = ColorUtil.wrapWithColorTag("Target", JagexColors.MENU_TARGET);
    private static final BufferedImage MARKER_IMAGE = ImageUtil.loadImageResource(ShortestPathPlugin.class, "/marker.png");
    private static final Pattern TRANSPORT_OPTIONS_REGEX = Pattern.compile("^(avoidWilderness|currencyThreshold|use\\w+)$");

    @Inject
    private Client client;

    @Getter
    @Inject
    private ClientThread clientThread;

    @Inject
    private ShortestPathConfig config;

    @Inject
    private EventBus eventBus;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PathTileOverlay pathOverlay;

    @Inject
    private PathMinimapOverlay pathMinimapOverlay;

    @Inject
    private PathMapOverlay pathMapOverlay;

    @Inject
    private PathMapTooltipOverlay pathMapTooltipOverlay;

    @Inject
    private DebugOverlayPanel debugOverlayPanel;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private WorldMapPointManager worldMapPointManager;

    boolean drawCollisionMap;
    boolean drawMap;
    boolean drawMinimap;
    boolean drawTiles;
    boolean drawTransports;
    boolean showTransportInfo;
    Color colourCollisionMap;
    Color colourPath;
    Color colourPathCalculating;
    Color colourText;
    Color colourTransports;
    int tileCounterStep;
    TileCounter showTileCounter;
    TileStyle pathStyle;

    private Point lastMenuOpenedPoint;
    private WorldMapPoint marker;
    private int lastLocation = WorldPointUtil.packWorldPoint(0, 0, 0);
    private Shape minimapClipFixed;
    private Shape minimapClipResizeable;
    private BufferedImage minimapSpriteFixed;
    private BufferedImage minimapSpriteResizeable;
    private Rectangle minimapRectangle = new Rectangle();

    private GameState lastGameState = null;
    private GameState lastLastGameState = null;
    private List<PendingTask> pendingTasks = new ArrayList<>(3);

    private ExecutorService pathfindingExecutor = Executors.newSingleThreadExecutor();
    private Future<?> pathfinderFuture;
    private final Object pathfinderMutex = new Object();
    private static final Map<String, Object> configOverride = new HashMap<>(50);
    @Getter
    private Pathfinder pathfinder;
    @Getter
    private PathfinderConfig pathfinderConfig;
    @Getter
    private boolean startPointSet = false;

    @Provides
    public ShortestPathConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ShortestPathConfig.class);
    }

    @Override
    protected void startUp() {
        cacheConfigValues();

        pathfinderConfig = new PathfinderConfig(client, config);
        if (GameState.LOGGED_IN.equals(client.getGameState())) {
            clientThread.invokeLater(pathfinderConfig::refresh);
        }

        overlayManager.add(pathOverlay);
        overlayManager.add(pathMinimapOverlay);
        overlayManager.add(pathMapOverlay);
        overlayManager.add(pathMapTooltipOverlay);

        if (config.drawDebugPanel()) {
            overlayManager.add(debugOverlayPanel);
        }
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(pathOverlay);
        overlayManager.remove(pathMinimapOverlay);
        overlayManager.remove(pathMapOverlay);
        overlayManager.remove(pathMapTooltipOverlay);
        overlayManager.remove(debugOverlayPanel);

        if (pathfindingExecutor != null) {
            pathfindingExecutor.shutdownNow();
            pathfindingExecutor = null;
        }
    }

    public void restartPathfinding(int start, Set<Integer> ends, boolean canReviveFiltered) {
        synchronized (pathfinderMutex) {
            if (pathfinder != null) {
                pathfinder.cancel();
                pathfinderFuture.cancel(true);
            }

            if (pathfindingExecutor == null) {
                ThreadFactory shortestPathNaming = new ThreadFactoryBuilder().setNameFormat("shortest-path-%d").build();
                pathfindingExecutor = Executors.newSingleThreadExecutor(shortestPathNaming);
            }
        }

        getClientThread().invokeLater(() -> {
            pathfinderConfig.refresh();
            pathfinderConfig.filterLocations(ends, canReviveFiltered);
            synchronized (pathfinderMutex) {
                if (ends.isEmpty()) {
                    setTarget(WorldPointUtil.UNDEFINED);
                } else {
                    pathfinder = new Pathfinder(this, pathfinderConfig, start, ends);
                    pathfinderFuture = pathfindingExecutor.submit(pathfinder);
                }
            }
        });
    }

    public void restartPathfinding(int start, Set<Integer> ends) {
        restartPathfinding(start, ends, true);
    }

    public boolean isNearPath(int location) {
        if (pathfinder == null || pathfinder.getPath() == null || pathfinder.getPath().isEmpty() ||
            config.recalculateDistance() < 0 || lastLocation == (lastLocation = location)) {
            return true;
        }

        for (int point : pathfinder.getPath()) {
            if (WorldPointUtil.distanceBetween(location, point) < config.recalculateDistance()) {
                return true;
            }
        }

        return false;
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!CONFIG_GROUP.equals(event.getGroup())) {
            return;
        }

        cacheConfigValues();

        if ("drawDebugPanel".equals(event.getKey())) {
            if (config.drawDebugPanel()) {
                overlayManager.add(debugOverlayPanel);
            } else {
                overlayManager.remove(debugOverlayPanel);
            }
            return;
        }

        // Transport option changed; rerun pathfinding
        if (TRANSPORT_OPTIONS_REGEX.matcher(event.getKey()).find()) {
            if (pathfinder != null) {
                restartPathfinding(pathfinder.getStart(), pathfinder.getTargets());
            }
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (pathfinderConfig == null
            || !GameState.LOGGING_IN.equals(lastLastGameState)
            || !GameState.LOADING.equals(lastLastGameState = lastGameState)
            || !GameState.LOGGED_IN.equals(lastGameState = event.getGameState())) {
            lastLastGameState = lastGameState;
            lastGameState = event.getGameState();
            return;
        }

        pendingTasks.add(new PendingTask(client.getTickCount() + 1, pathfinderConfig::refresh));
    }

    @Subscribe
    public void onPluginMessage(PluginMessage event) {
        if (!CONFIG_GROUP.equals(event.getNamespace())) {
            return;
        }

        String action = event.getName();
        if (PLUGIN_MESSAGE_PATH.equals(action)) {
            Map<String, Object> data = event.getData();
            Object objStart = data.getOrDefault(PLUGIN_MESSAGE_START, null);
            Object objTarget = data.getOrDefault(PLUGIN_MESSAGE_TARGET, null);
            Object objConfigOverride = data.getOrDefault(PLUGIN_MESSAGE_CONFIG_OVERRIDE, null);

            @SuppressWarnings("unchecked")
            Map<String, Object> configOverride = (objConfigOverride instanceof Map<?,?>) ? ((Map<String, Object>) objConfigOverride) : null;
            if (configOverride != null && !configOverride.isEmpty()) {
                this.configOverride.clear();
                for (String key : configOverride.keySet()) {
                    this.configOverride.put(key, configOverride.get(key));
                }
                cacheConfigValues();
            }

            if (objStart == null && objTarget == null) {
                return;
            }

            int start = (objStart instanceof WorldPoint) ? WorldPointUtil.packWorldPoint((WorldPoint) objStart)
                : ((objStart instanceof Integer) ? ((int) objStart) : WorldPointUtil.UNDEFINED);
            if (start == WorldPointUtil.UNDEFINED) {
                if (client.getLocalPlayer() == null) {
                    return;
                }
                start = WorldPointUtil.packWorldPoint(client.getLocalPlayer().getWorldLocation());
            }

            Set<Integer> targets = new HashSet<>();
            if (objTarget instanceof Integer) {
                int packedPoint = (Integer) objTarget;
                if (packedPoint == WorldPointUtil.UNDEFINED) {
                    return;
                }
                targets.add(packedPoint);
            } else if (objTarget instanceof WorldPoint) {
                int packedPoint = WorldPointUtil.packWorldPoint((WorldPoint) objTarget);
                if (packedPoint == WorldPointUtil.UNDEFINED) {
                    return;
                }
                targets.add(packedPoint);
            } else if (objTarget instanceof Set<?>) {
                @SuppressWarnings("unchecked")
                Set<Object> objTargets = (Set<Object>) objTarget;
                for (Object obj : objTargets) {
                    int packedPoint = WorldPointUtil.UNDEFINED;
                    if (obj instanceof Integer) {
                        packedPoint = (Integer) obj;
                    } else if (obj instanceof WorldPoint) {
                        packedPoint = WorldPointUtil.packWorldPoint((WorldPoint) obj);
                    }
                    if (packedPoint == WorldPointUtil.UNDEFINED) {
                        return;
                    }
                    targets.add(packedPoint);
                }
            }

            boolean useOld = targets.isEmpty() && pathfinder != null;
            restartPathfinding(start, useOld ? pathfinder.getTargets() : targets, useOld);
        } else if (PLUGIN_MESSAGE_CLEAR.equals(action)) {
            this.configOverride.clear();
            cacheConfigValues();
            setTarget(WorldPointUtil.UNDEFINED);
        }
    }

    public void postPluginMessages() {
        if (pathfinder == null) {
            return;
        }
        if (override("postTransports", config.postTransports())) {
            Map<String, Object> data = new HashMap<>();
            List<WorldPoint> transportOrigins = new ArrayList<>();
            List<WorldPoint> transportDestinations = new ArrayList<>();
            List<String> transportObjectInfos = new ArrayList<>();
            List<String> transportDisplayInfos = new ArrayList<>();
            List<Integer> currentPath = pathfinder.getPath();
            for (int i = 1; i < currentPath.size(); i++) {
                int origin = currentPath.get(i-1);
                int destination = currentPath.get(i);
                for (Transport transport : pathfinderConfig.getTransports().getOrDefault(origin, new HashSet<>())) {
                    if (transport.getDestination() == destination) {
                        transportOrigins.add(WorldPointUtil.unpackWorldPoint(origin));
                        transportDestinations.add(WorldPointUtil.unpackWorldPoint(destination));
                        transportObjectInfos.add(transport.getObjectInfo());
                        transportDisplayInfos.add(transport.getDisplayInfo());
                    }
                }
            }
            data.put("origin", transportOrigins);
            data.put("destination", transportDestinations);
            data.put("objectInfo", transportObjectInfos);
            data.put("displayInfo", transportDisplayInfos);
            eventBus.post(new PluginMessage(CONFIG_GROUP, PLUGIN_MESSAGE_TRANSPORTS, data));
        }
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event) {
        lastMenuOpenedPoint = client.getMouseCanvasPosition();
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        for (int i = 0; i < pendingTasks.size(); i++) {
            if (pendingTasks.get(i).check(client.getTickCount())) {
                pendingTasks.remove(i--).run();
            }
        }

        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null || pathfinder == null) {
            return;
        }

        int currentLocation = WorldPointUtil.fromLocalInstance(client, localPlayer.getLocalLocation());
        for (int target : pathfinder.getTargets()) {
            if (WorldPointUtil.distanceBetween(currentLocation, target) < config.reachedDistance()) {
                setTarget(WorldPointUtil.UNDEFINED);
                return;
            }
        }

        if (!startPointSet && !isNearPath(currentLocation)) {
            if (config.cancelInstead()) {
                setTarget(WorldPointUtil.UNDEFINED);
                return;
            }
            restartPathfinding(currentLocation, pathfinder.getTargets());
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (client.isKeyPressed(KeyCode.KC_SHIFT)
            && event.getType() == MenuAction.WALK.getId()) {
            addMenuEntry(event, SET, TARGET, 1);
            if (pathfinder != null) {
                if (pathfinder.getTargets().size() >= 1) {
                    addMenuEntry(event, SET, TARGET + ColorUtil.wrapWithColorTag(" " +
                        (pathfinder.getTargets().size() + 1), JagexColors.MENU_TARGET), 1);
                }
                for (int target : pathfinder.getTargets()) {
                    if (target != WorldPointUtil.UNDEFINED) {
                        addMenuEntry(event, SET, START, 1);
                        break;
                    }
                }
                int selectedTile = getSelectedWorldPoint();
                if (pathfinder.getPath() != null) {
                    for (int tile : pathfinder.getPath()) {
                        if (tile == selectedTile) {
                            addMenuEntry(event, CLEAR, PATH, 1);
                            break;
                        }
                    }
                }
            }
        }

        final Widget map = client.getWidget(ComponentID.WORLD_MAP_MAPVIEW);

        if (map != null) {
            if (map.getBounds().contains(
                client.getMouseCanvasPosition().getX(),
                client.getMouseCanvasPosition().getY())) {
                addMenuEntry(event, SET, TARGET, 0);
                if (pathfinder != null) {
                    if (pathfinder.getTargets().size() >= 1) {
                        addMenuEntry(event, SET, TARGET + ColorUtil.wrapWithColorTag(" " +
                            (pathfinder.getTargets().size() + 1), JagexColors.MENU_TARGET), 0);
                    }
                    for (int target : pathfinder.getTargets()) {
                        if (target != WorldPointUtil.UNDEFINED) {
                            addMenuEntry(event, SET, START, 0);
                            addMenuEntry(event, CLEAR, PATH, 0);
                        }
                    }
                }
            }
            if (event.getOption().equals(FLASH_ICONS) && pathfinderConfig.hasDestination(simplify(event.getTarget()))) {
                addMenuEntry(event, FIND_CLOSEST, event.getTarget(), 1);
            }
        }

        final Shape minimap = getMinimapClipArea();

        if (minimap != null && pathfinder != null
            && minimap.contains(
                client.getMouseCanvasPosition().getX(),
                client.getMouseCanvasPosition().getY())) {
            addMenuEntry(event, CLEAR, PATH, 0);
        }

        if (minimap != null && pathfinder != null
            && ("Floating World Map".equals(Text.removeTags(event.getOption()))
            || "Close Floating panel".equals(Text.removeTags(event.getOption())))) {
            addMenuEntry(event, CLEAR, PATH, 1);
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getContainerId() != InventoryID.BANK) {
            return;
        }
        pathfinderConfig.bank = event.getItemContainer();
    }

    public Map<Integer, Set<Transport>> getTransports() {
        return pathfinderConfig.getTransports();
    }

    public CollisionMap getMap() {
        return pathfinderConfig.getMap();
    }

    public static boolean override(String configOverrideKey, boolean defaultValue) {
        if (!configOverride.isEmpty()) {
            Object value = configOverride.get(configOverrideKey);
            if (value instanceof Boolean) {
                return (boolean) value;
            }
        }
        return defaultValue;
    }

    private Color override(String configOverrideKey, Color defaultValue) {
        if (!configOverride.isEmpty()) {
            Object value = configOverride.get(configOverrideKey);
            if (value instanceof Color) {
                return (Color) value;
            }
        }
        return defaultValue;
    }

    public static int override(String configOverrideKey, int defaultValue) {
        if (!configOverride.isEmpty()) {
            Object value = configOverride.get(configOverrideKey);
            if (value instanceof Integer) {
                return (int) value;
            }
        }
        return defaultValue;
    }

    public static TeleportationItem override(String configOverrideKey, TeleportationItem defaultValue) {
        if (!configOverride.isEmpty()) {
            Object value = configOverride.get(configOverrideKey);
            if (value instanceof String) {
                TeleportationItem teleportationItem = TeleportationItem.fromType((String) value);
                if (teleportationItem != null) {
                    return teleportationItem;
                }
            }
        }
        return defaultValue;
    }

    private TileCounter override(String configOverrideKey, TileCounter defaultValue) {
        if (!configOverride.isEmpty()) {
            Object value = configOverride.get(configOverrideKey);
            if (value instanceof String) {
                TileCounter tileCounter = TileCounter.fromType((String) value);
                if (tileCounter != null) {
                    return tileCounter;
                }
            }
        }
        return defaultValue;
    }

    private TileStyle override(String configOverrideKey, TileStyle defaultValue) {
        if (!configOverride.isEmpty()) {
            Object value = configOverride.get(configOverrideKey);
            if (value instanceof String) {
                TileStyle tileStyle = TileStyle.fromType((String) value);
                if (tileStyle != null) {
                    return tileStyle;
                }
            }
        }
        return defaultValue;
    }

    private void cacheConfigValues() {
        drawCollisionMap = override("drawCollisionMap", config.drawCollisionMap());
        drawMap = override("drawMap", config.drawMap());
        drawMinimap = override("drawMinimap", config.drawMinimap());
        drawTiles = override("drawTiles", config.drawTiles());
        drawTransports = override("drawTransports", config.drawTransports());
        showTransportInfo = override("showTransportInfo", config.showTransportInfo());

        colourCollisionMap = override("colourCollisionMap", config.colourCollisionMap());
        colourPath = override("colourPath", config.colourPath());
        colourPathCalculating = override("colourPathCalculating", config.colourPathCalculating());
        colourText = override("colourText", config.colourText());
        colourTransports = override("colourTransports", config.colourTransports());

        tileCounterStep = override("tileCounterStep", config.tileCounterStep());

        showTileCounter = override("showTileCounter", config.showTileCounter());
        pathStyle = override("pathStyle", config.pathStyle());
    }

    private String simplify(String text) {
        return Text.removeTags(text).toLowerCase()
            .replaceAll("[^a-zA-Z ]", "")
            .replaceAll("[ ]", "_")
            .replace("__", "_");
    }

    private void onMenuOptionClicked(MenuEntry entry) {
        if (entry.getOption().equals(SET) && entry.getTarget().equals(TARGET)) {
            setTarget(getSelectedWorldPoint());
        } else if (entry.getOption().equals(SET) && pathfinder != null && entry.getTarget().equals(TARGET +
            ColorUtil.wrapWithColorTag(" " + (pathfinder.getTargets().size() + 1), JagexColors.MENU_TARGET))) {
            setTarget(getSelectedWorldPoint(), true);
        } else if (entry.getOption().equals(SET) && entry.getTarget().equals(START)) {
            setStart(getSelectedWorldPoint());
        } else if (entry.getOption().equals(CLEAR) && entry.getTarget().equals(PATH)) {
            setTarget(WorldPointUtil.UNDEFINED);
        } else if (entry.getOption().equals(FIND_CLOSEST)) {
            setTargets(pathfinderConfig.getDestinations(simplify(entry.getTarget())), true);
        }
    }

    private int getSelectedWorldPoint() {
        if (client.getWidget(ComponentID.WORLD_MAP_MAPVIEW) == null) {
            if (client.getSelectedSceneTile() != null) {
                return WorldPointUtil.fromLocalInstance(client, client.getSelectedSceneTile().getLocalLocation());
            }
        } else {
            return client.isMenuOpen()
                ? MapPointMapper.calculateMapPoint(client, lastMenuOpenedPoint.getX(), lastMenuOpenedPoint.getY())
                : MapPointMapper.calculateMapPoint(client, client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY());
        }
        return WorldPointUtil.UNDEFINED;
    }

    private void setTarget(int target) {
        setTarget(target, false);
    }

    private void setTarget(int target, boolean append) {
        Set<Integer> targets = new HashSet<>();
        if (target != WorldPointUtil.UNDEFINED) {
            targets.add(target);
        }
        setTargets(targets, append);
    }

    private void setTargets(Set<Integer> targets, boolean append) {
        // Temporary debug logging to help unit tests capture map state when a
        // new route/target is created. Logs world map zoom, map position,
        // widget bounds and computed points to stdout.
        if (targets != null && !targets.isEmpty()) {
            logTargetInfo(targets);
        }
        if (targets == null || targets.isEmpty()) {
            synchronized (pathfinderMutex) {
                if (pathfinder != null) {
                    pathfinder.cancel();
                }
                pathfinder = null;
            }

            worldMapPointManager.removeIf(x -> x == marker);
            marker = null;
            startPointSet = false;
        } else {
            Player localPlayer = client.getLocalPlayer();
            if (!startPointSet && localPlayer == null) {
                return;
            }
            worldMapPointManager.removeIf(x -> x == marker);
            if (targets.size() == 1) {
                marker = new WorldMapPoint(WorldPointUtil.unpackWorldPoint(targets.iterator().next()), MARKER_IMAGE);
                marker.setName("Target");
                marker.setTarget(marker.getWorldPoint());
                marker.setJumpOnClick(true);
                worldMapPointManager.add(marker);
            }

            int start = WorldPointUtil.fromLocalInstance(client, localPlayer.getLocalLocation());
            lastLocation = start;
            if (startPointSet && pathfinder != null) {
                start = pathfinder.getStart();
            }
            Set<Integer> destinations = new HashSet<>(targets);
            if (pathfinder != null && append) {
                destinations.addAll(pathfinder.getTargets());
            }
            restartPathfinding(start, destinations, append);
        }
    }

    // Temporary helper: prints map-related state to stdout for test inspection.
    private void logTargetInfo(Set<Integer> targets) {
        try {
            WorldMap worldMap = client.getWorldMap();
            System.out.println("[TEMP-LOG] setTargets called with targets size=" + (targets == null ? 0 : targets.size()));
            if (worldMap == null) {
                System.out.println("[TEMP-LOG] worldMap=null");
            } else {
                System.out.println("[TEMP-LOG] worldMap.zoom=" + worldMap.getWorldMapZoom());
                Point pos = worldMap.getWorldMapPosition();
                System.out.println("[TEMP-LOG] worldMap.position=" + (pos == null ? "null" : pos.getX() + "," + pos.getY()));
            }

            Widget mapWidget = client.getWidget(ComponentID.WORLD_MAP_MAPVIEW);
            System.out.println("[TEMP-LOG] widget(world_map_mapview)=" + (mapWidget == null ? "null" : mapWidget.getBounds()));

            // Log mouse and last menu points and what they map to
            try {
                Point mouse = client.getMouseCanvasPosition();
                System.out.println("[TEMP-LOG] mouseCanvasPosition=" + (mouse == null ? "null" : mouse.getX() + "," + mouse.getY()));
            } catch (Exception e) {
                System.out.println("[TEMP-LOG] mouseCanvasPosition=error:" + e.getMessage());
            }

            System.out.println("[TEMP-LOG] lastMenuOpenedPoint=" + (lastMenuOpenedPoint == null ? "null" : lastMenuOpenedPoint.getX() + "," + lastMenuOpenedPoint.getY()));

            // Calculate what MapPointMapper would return for those points
            if (lastMenuOpenedPoint != null) {
                int calc = MapPointMapper.calculateMapPoint(client, lastMenuOpenedPoint.getX(), lastMenuOpenedPoint.getY());
                System.out.println("[TEMP-LOG] calculatedFromLastMenuOpenedPoint=" + calc);
            }
            try {
                Point mouse = client.getMouseCanvasPosition();
                if (mouse != null) {
                    int calcMouse = MapPointMapper.calculateMapPoint(client, mouse.getX(), mouse.getY());
                    System.out.println("[TEMP-LOG] calculatedFromMouseCanvasPosition=" + calcMouse);
                }
            } catch (Exception e) {
                System.out.println("[TEMP-LOG] calculateFromMouseCanvasPosition=error:" + e.getMessage());
            }

            for (Integer t : targets) {
                if (t == null) {
                    System.out.println("[TEMP-LOG] target= null");
                    continue;
                }
                int packed = t;
                int wx = WorldPointUtil.unpackWorldX(packed);
                int wy = WorldPointUtil.unpackWorldY(packed);
                int wz = WorldPointUtil.unpackWorldPlane(packed);
                System.out.println("[TEMP-LOG] target.packed=" + packed + " -> world=(" + wx + "," + wy + "," + wz + ")");
            }

            // Also print a copy-pastable Case(...) entry for tests
            try {
                float zoom = (worldMap != null) ? worldMap.getWorldMapZoom() : 0.0f;
                Point mapPos = (worldMap != null) ? worldMap.getWorldMapPosition() : null;
                Rectangle bounds = (mapWidget != null) ? mapWidget.getBounds() : new Rectangle(0, 0, 0, 0);

                Point mousePoint = null;
                try {
                    mousePoint = client.getMouseCanvasPosition();
                } catch (Exception ignored) {
                }

                int mouseX = (mousePoint != null) ? mousePoint.getX() : 0;
                int mouseY = (mousePoint != null) ? mousePoint.getY() : 0;
                int lastX = (lastMenuOpenedPoint != null) ? lastMenuOpenedPoint.getX() : 0;
                int lastY = (lastMenuOpenedPoint != null) ? lastMenuOpenedPoint.getY() : 0;

                int calcMouse = WorldPointUtil.UNDEFINED;
                int calcLast = WorldPointUtil.UNDEFINED;
                try {
                    if (mousePoint != null) {
                        calcMouse = MapPointMapper.calculateMapPoint(client, mouseX, mouseY);
                    }
                } catch (Exception ignored) {
                }
                try {
                    if (lastMenuOpenedPoint != null) {
                        calcLast = MapPointMapper.calculateMapPoint(client, lastX, lastY);
                    }
                } catch (Exception ignored) {
                }

                int t1 = WorldPointUtil.UNDEFINED;
                int t2 = WorldPointUtil.UNDEFINED;
                if (targets != null) {
                    java.util.Iterator<Integer> it = targets.iterator();
                    if (it.hasNext()) {
                        t1 = it.next();
                    }
                    if (it.hasNext()) {
                        t2 = it.next();
                    }
                }

                String desc = (mapPos != null) ? ("Auto-generated: " + mapPos.getX() + "," + mapPos.getY()) : "Auto-generated";

                System.out.println("        cases.add(new Case(");
                System.out.println("            \"" + desc + "\",");
                System.out.println("            " + zoom + "f,");
                System.out.println("            new Point(" + (mapPos != null ? mapPos.getX() : 0) + ", " + (mapPos != null ? mapPos.getY() : 0) + "),");
                System.out.println("            new Rectangle(" + bounds.x + ", " + bounds.y + ", " + bounds.width + ", " + bounds.height + "),");
                System.out.println("            " + mouseX + ", " + mouseY + ",");
                System.out.println("            " + lastX + ", " + lastY + ",");
                System.out.println("            " + calcMouse + ",");
                System.out.println("            " + calcLast);
                System.out.println("        ));");
            } catch (Exception e) {
                System.out.println("[TEMP-LOG] error while printing case: " + e.toString());
            }
        } catch (Throwable ex) {
            // Avoid breaking plugin flow; this is temporary and only prints
            System.out.println("[TEMP-LOG] error while logging target info: " + ex.toString());
        }
    }

    private void setStart(int start) {
        if (pathfinder == null) {
            return;
        }
        startPointSet = true;
        restartPathfinding(start, pathfinder.getTargets());
    }

    private void addMenuEntry(MenuEntryAdded event, String option, String target, int position) {
        List<MenuEntry> entries = new LinkedList<>(Arrays.asList(client.getMenuEntries()));

        if (entries.stream().anyMatch(e -> e.getOption().equals(option) && e.getTarget().equals(target))) {
            return;
        }

        client.createMenuEntry(position)
            .setOption(option)
            .setTarget(target)
            .setParam0(event.getActionParam0())
            .setParam1(event.getActionParam1())
            .setIdentifier(event.getIdentifier())
            .setType(MenuAction.RUNELITE)
            .onClick(this::onMenuOptionClicked);
    }

    private Widget getMinimapDrawWidget() {
        if (client.isResized()) {
            if (client.getVarbitValue(Varbits.SIDE_PANELS) == 1) {
                return client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_MINIMAP_DRAW_AREA);
            }
            return client.getWidget(ComponentID.RESIZABLE_VIEWPORT_MINIMAP_DRAW_AREA);
        }
        return client.getWidget(ComponentID.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);
    }

    private Shape getMinimapClipAreaSimple() {
        Widget minimapDrawArea = getMinimapDrawWidget();

        if (minimapDrawArea == null || minimapDrawArea.isHidden()) {
            return null;
        }

        Rectangle bounds = minimapDrawArea.getBounds();

        return new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    public Shape getMinimapClipArea() {
        Widget minimapWidget = getMinimapDrawWidget();

        if (minimapWidget == null || minimapWidget.isHidden() || !minimapRectangle.equals(minimapRectangle = minimapWidget.getBounds())) {
            minimapClipFixed = null;
            minimapClipResizeable = null;
            minimapSpriteFixed = null;
            minimapSpriteResizeable = null;
        }

        if (minimapWidget == null || minimapWidget.isHidden()) {
            return null;
        }

        if (client.isResized()) {
            if (minimapClipResizeable != null) {
                return minimapClipResizeable;
            }
            if (minimapSpriteResizeable == null) {
                minimapSpriteResizeable = spriteManager.getSprite(SpriteID.RESIZEABLE_MODE_MINIMAP_ALPHA_MASK, 0);
            }
            if (minimapSpriteResizeable != null) {
                minimapClipResizeable = bufferedImageToPolygon(minimapSpriteResizeable);
                return minimapClipResizeable;
            }
            return getMinimapClipAreaSimple();
        }
        if (minimapClipFixed != null) {
            return minimapClipFixed;
        }
        if (minimapSpriteFixed == null) {
            minimapSpriteFixed = spriteManager.getSprite(SpriteID.FIXED_MODE_MINIMAP_ALPHA_MASK, 0);
        }
        if (minimapSpriteFixed != null) {
            minimapClipFixed = bufferedImageToPolygon(minimapSpriteFixed);
            return minimapClipFixed;
        }
        return getMinimapClipAreaSimple();
    }

    private Polygon bufferedImageToPolygon(BufferedImage image) {
        Color outsideColour = null;
        Color previousColour;
        final int width = image.getWidth();
        final int height = image.getHeight();
        List<java.awt.Point> points = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            previousColour = outsideColour;
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int a = (rgb & 0xff000000) >>> 24;
                int r = (rgb & 0x00ff0000) >> 16;
                int g = (rgb & 0x0000ff00) >> 8;
                int b = (rgb & 0x000000ff) >> 0;
                Color colour = new Color(r, g, b, a);
                if (x == 0 && y == 0) {
                    outsideColour = colour;
                    previousColour = colour;
                }
                if (!colour.equals(outsideColour) && previousColour.equals(outsideColour)) {
                    points.add(new java.awt.Point(x, y));
                }
                if ((colour.equals(outsideColour) || x == (width - 1)) && !previousColour.equals(outsideColour)) {
                    points.add(0, new java.awt.Point(x, y));
                }
                previousColour = colour;
            }
        }
        int offsetX = minimapRectangle.x;
        int offsetY = minimapRectangle.y;
        Polygon polygon = new Polygon();
        for (java.awt.Point point : points) {
            polygon.addPoint(point.x + offsetX, point.y + offsetY);
        }
        return polygon;
    }
}
