package shortestpath;

import com.google.inject.Inject;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class PathMapTooltipOverlay extends Overlay {
    private static final int TOOLTIP_OFFSET_HEIGHT = 25;
    private static final int TOOLTIP_OFFSET_WIDTH = 15;
    private static final int TOOLTIP_PADDING_HEIGHT = 1;
    private static final int TOOLTIP_PADDING_WIDTH = 2;
    private static final int TOOLTIP_TEXT_OFFSET_HEIGHT = -2;

    private final Client client;
    private final ShortestPathPlugin plugin;

    @Inject
    private PathMapTooltipOverlay(Client client, ShortestPathPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(Overlay.PRIORITY_HIGHEST);
        setLayer(OverlayLayer.MANUAL);
        drawAfterInterface(InterfaceID.WORLD_MAP);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.drawMap || client.getWidget(ComponentID.WORLD_MAP_MAPVIEW) == null) {
            return null;
        }

        if (plugin.getPathfinder() != null) {
            List<Integer> path = plugin.getPathfinder().getPath();
            Point cursorPos = client.getMouseCanvasPosition();
            for (int i = 0; i < path.size(); i++) {
                int nextPoint = WorldPointUtil.UNDEFINED;
                if (path.size() > i + 1) {
                    nextPoint = path.get(i + 1);
                }
                if (drawTooltip(graphics, cursorPos, path.get(i), nextPoint, i + 1)) {
                    return null;
                }
            }
        }

        return null;
    }

    private boolean drawTooltip(Graphics2D graphics, Point cursorPos, int point, int nextPoint, int n) {
        Point start = plugin.mapWorldPointToGraphicsPoint(point);
        Point end = plugin.mapWorldPointToGraphicsPoint(WorldPointUtil.dxdy(point, 1, -1));

        if (start == null || end == null) {
            return false;
        }

        int width = end.getX() - start.getX();

        if (cursorPos.getX() < (start.getX() - width / 2) || cursorPos.getX() > (end.getX() - width / 2) ||
            cursorPos.getY() < (start.getY() - width / 2) || cursorPos.getY() > (end.getY() - width / 2)) {
            return false;
        }

        List<String> rows = new ArrayList<>(Arrays.asList("Shortest path:", "Step " + n + " of " + plugin.getPathfinder().getPath().size()));
        if (nextPoint != WorldPointUtil.UNDEFINED) {
            for (Transport transport : plugin.getTransports().getOrDefault(point, new HashSet<>())) {
                if (nextPoint == transport.getDestination()
                    && transport.getDisplayInfo() != null && !transport.getDisplayInfo().isEmpty()) {
                    rows.add(transport.getDisplayInfo());
                    break;
                }
            }
        }

        graphics.setFont(FontManager.getRunescapeFont());
        FontMetrics fm = graphics.getFontMetrics();
        int tooltipHeight = fm.getHeight();
        int tooltipWidth = rows.stream().map(fm::stringWidth).max(Integer::compareTo).get();

        int clippedHeight = tooltipHeight * rows.size() + TOOLTIP_PADDING_HEIGHT * 2;
        int clippedWidth = tooltipWidth + TOOLTIP_PADDING_WIDTH * 2;

        Rectangle worldMapBounds = client.getWidget(ComponentID.WORLD_MAP_MAPVIEW).getBounds();
        int worldMapRightBoundary = worldMapBounds.width + worldMapBounds.x;
        int worldMapBottomBoundary = worldMapBounds.height + worldMapBounds.y;

        int drawPointX = start.getX() + TOOLTIP_OFFSET_WIDTH;
        int drawPointY = start.getY();
        if (drawPointX + clippedWidth > worldMapRightBoundary) {
            drawPointX = worldMapRightBoundary - clippedWidth;
        }
        if (drawPointY + clippedHeight > worldMapBottomBoundary) {
            drawPointY = start.getY() - clippedHeight;
        }
        drawPointY += TOOLTIP_OFFSET_HEIGHT;

        Rectangle tooltipRect = new Rectangle(
            drawPointX - TOOLTIP_PADDING_WIDTH,
            drawPointY - TOOLTIP_PADDING_HEIGHT,
            clippedWidth,
            clippedHeight);

        graphics.setColor(JagexColors.TOOLTIP_BACKGROUND);
        graphics.fillRect(tooltipRect.x, tooltipRect.y, tooltipRect.width, tooltipRect.height);

        graphics.setColor(JagexColors.TOOLTIP_BORDER);
        graphics.drawRect(tooltipRect.x, tooltipRect.y, tooltipRect.width, tooltipRect.height);

        graphics.setColor(JagexColors.TOOLTIP_TEXT);
        for (int i = 0; i < rows.size(); i++) {
            graphics.drawString(rows.get(i), drawPointX, drawPointY + TOOLTIP_TEXT_OFFSET_HEIGHT + (i + 1) * tooltipHeight);
        }

        return true;
    }
}
