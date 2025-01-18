package shortestpath;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.HashSet;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import shortestpath.pathfinder.CollisionMap;

public class PathMapOverlay extends Overlay {
    private final Client client;
    private final ShortestPathPlugin plugin;

    @Inject
    private PathMapOverlay(Client client, ShortestPathPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(Overlay.PRIORITY_LOW);
        setLayer(OverlayLayer.MANUAL);
        drawAfterLayer(ComponentID.WORLD_MAP_MAPVIEW);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.drawMap) {
            return null;
        }

        if (client.getWidget(ComponentID.WORLD_MAP_MAPVIEW) == null) {
            return null;
        }

        Area worldMapClipArea = getWorldMapClipArea(client.getWidget(ComponentID.WORLD_MAP_MAPVIEW).getBounds());
        graphics.setClip(worldMapClipArea);

        if (plugin.drawCollisionMap) {
            graphics.setColor(plugin.colourCollisionMap);
            Rectangle extent = getWorldMapExtent(client.getWidget(ComponentID.WORLD_MAP_MAPVIEW).getBounds());
            final CollisionMap map = plugin.getMap();
            final int z = client.getPlane();
            for (int x = extent.x; x < (extent.x + extent.width + 1); x++) {
                for (int y = extent.y - extent.height; y < (extent.y + 1); y++) {
                    if (map.isBlocked(x, y, z)) {
                        drawOnMap(graphics, WorldPointUtil.packWorldPoint(x, y, z), false);
                    }
                }
            }
        }

        if (plugin.drawTransports) {
            graphics.setColor(Color.WHITE);
            for (int a : plugin.getTransports().keySet()) {
                if (a == Transport.UNDEFINED_ORIGIN) {
                    continue; // skip teleports
                }

                Point mapA = plugin.mapWorldPointToGraphicsPoint(a);
                if (mapA == null || !worldMapClipArea.contains(mapA.getX(), mapA.getY())) {
                    continue;
                }

                for (Transport b : plugin.getTransports().getOrDefault(a, new HashSet<>())) {
                    if (b == null
                        || TransportType.TELEPORTATION_ITEM.equals(b.getType())
                        || TransportType.TELEPORTATION_SPELL.equals(b.getType())) {
                        continue; // skip teleports
                    }

                    Point mapB = plugin.mapWorldPointToGraphicsPoint(b.getDestination());
                    if (mapB == null || !worldMapClipArea.contains(mapB.getX(), mapB.getY())) {
                        continue;
                    }

                    graphics.drawLine(mapA.getX(), mapA.getY(), mapB.getX(), mapB.getY());
                }
            }
        }

        if (plugin.getPathfinder() != null) {
            Color colour = plugin.getPathfinder().isDone() ? plugin.colourPath : plugin.colourPathCalculating;
            List<Integer> path = plugin.getPathfinder().getPath();
            for (int i = 0; i < path.size(); i++) {
                graphics.setColor(colour);
                int point = path.get(i);
                int lastPoint = (i > 0) ? path.get(i - 1) : point;
                if (WorldPointUtil.distanceBetween(point, lastPoint) > 1) {
                    drawOnMap(graphics, lastPoint, point, true);
                }
                drawOnMap(graphics, point, true);
            }
        }

        return null;
    }

    private void drawOnMap(Graphics2D graphics, int point, boolean checkHover) {
        drawOnMap(graphics, point, WorldPointUtil.dxdy(point, 1, -1), checkHover);
    }

    private void drawOnMap(Graphics2D graphics, int point, int offsetPoint, boolean checkHover) {
        Point start = plugin.mapWorldPointToGraphicsPoint(point);
        Point end = plugin.mapWorldPointToGraphicsPoint(offsetPoint);

        if (start == null || end == null) {
            return;
        }

        int x = start.getX();
        int y = start.getY();
        final int width = end.getX() - x;
        final int height = end.getY() - y;
        x -= width / 2;
        y -= height / 2;

        if (WorldPointUtil.distanceBetween(point, offsetPoint) > 1) {
            graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            graphics.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
        } else {
            Point cursorPos = client.getMouseCanvasPosition();
            if (checkHover &&
                cursorPos.getX() >= x && cursorPos.getX() <= (end.getX() - width / 2) &&
                cursorPos.getY() >= y && cursorPos.getY() <= (end.getY() - width / 2)) {
                graphics.setColor(graphics.getColor().darker());
            }
            graphics.fillRect(x, y, width, height);
        }
    }

    private Area getWorldMapClipArea(Rectangle baseRectangle) {
        final Widget overview = client.getWidget(ComponentID.WORLD_MAP_OVERVIEW_MAP);
        final Widget surfaceSelector = client.getWidget(ComponentID.WORLD_MAP_SURFACE_SELECTOR);

        Area clipArea = new Area(baseRectangle);

        if (overview != null && !overview.isHidden()) {
            clipArea.subtract(new Area(overview.getBounds()));
        }

        if (surfaceSelector != null && !surfaceSelector.isHidden()) {
            clipArea.subtract(new Area(surfaceSelector.getBounds()));
        }

        return clipArea;
    }

    private Rectangle getWorldMapExtent(Rectangle baseRectangle) {
        int topLeft = plugin.calculateMapPoint(new Point(baseRectangle.x, baseRectangle.y));
        int bottomRight = plugin.calculateMapPoint(
            new Point(baseRectangle.x + baseRectangle.width, baseRectangle.y + baseRectangle.height));
        int topLeftX = WorldPointUtil.unpackWorldX(topLeft);
        int topLeftY = WorldPointUtil.unpackWorldY(topLeft);
        int bottomRightX = WorldPointUtil.unpackWorldX(bottomRight);
        int bottomRightY = WorldPointUtil.unpackWorldY(bottomRight);
        return new Rectangle(topLeftX, topLeftY, bottomRightX - topLeftX, topLeftY - bottomRightY);
    }
}
