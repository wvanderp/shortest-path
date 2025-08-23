package shortestpath;

import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.worldmap.WorldMap;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;

public final class MapPointMapper
{
    private MapPointMapper() {}

    public static int calculateMapPoint(Client client, int pointX, int pointY)
    {
        WorldMap worldMap = client.getWorldMap();
        if (worldMap == null)
        {
            return WorldPointUtil.UNDEFINED;
        }
        
        float zoom = worldMap.getWorldMapZoom();
        int mapPoint = WorldPointUtil.packWorldPoint(worldMap.getWorldMapPosition().getX(), worldMap.getWorldMapPosition().getY(), 0);
        int middleX = mapWorldPointToGraphicsPointX(client, mapPoint);
        int middleY = mapWorldPointToGraphicsPointY(client, mapPoint);

        if (pointX == Integer.MIN_VALUE || pointY == Integer.MIN_VALUE ||
            middleX == Integer.MIN_VALUE || middleY == Integer.MIN_VALUE)
        {
            return WorldPointUtil.UNDEFINED;
        }

        final int dx = (int) ((pointX - middleX) / zoom);
        final int dy = (int) ((-(pointY - middleY)) / zoom);

        return WorldPointUtil.dxdy(mapPoint, dx, dy);
    }

    public static int mapWorldPointToGraphicsPointX(Client client, int packedWorldPoint)
    {
        WorldMap worldMap = client.getWorldMap();
        if (worldMap == null)
        {
            return Integer.MIN_VALUE;
        }

        float pixelsPerTile = worldMap.getWorldMapZoom();

        Widget map = client.getWidget(ComponentID.WORLD_MAP_MAPVIEW);
        if (map != null)
        {
            Rectangle worldMapRect = map.getBounds();

            int widthInTiles = (int) Math.ceil(worldMapRect.getWidth() / pixelsPerTile);

            Point worldMapPosition = worldMap.getWorldMapPosition();

            int xTileOffset = WorldPointUtil.unpackWorldX(packedWorldPoint) + widthInTiles / 2 - worldMapPosition.getX();

            int xGraphDiff = ((int) (xTileOffset * pixelsPerTile));
            xGraphDiff += pixelsPerTile - Math.ceil(pixelsPerTile / 2);
            xGraphDiff += (int) worldMapRect.getX();

            return xGraphDiff;
        }
        return Integer.MIN_VALUE;
    }

    public static int mapWorldPointToGraphicsPointY(Client client, int packedWorldPoint)
    {
        WorldMap worldMap = client.getWorldMap();
        if (worldMap == null)
        {
            return Integer.MIN_VALUE;
        }

        float pixelsPerTile = worldMap.getWorldMapZoom();

        Widget map = client.getWidget(ComponentID.WORLD_MAP_MAPVIEW);
        if (map != null)
        {
            Rectangle worldMapRect = map.getBounds();

            int heightInTiles = (int) Math.ceil(worldMapRect.getHeight() / pixelsPerTile);

            Point worldMapPosition = worldMap.getWorldMapPosition();

            int yTileMax = worldMapPosition.getY() - heightInTiles / 2;
            int yTileOffset = (yTileMax - WorldPointUtil.unpackWorldY(packedWorldPoint) - 1) * -1;

            int yGraphDiff = (int) (yTileOffset * pixelsPerTile);
            yGraphDiff -= pixelsPerTile - Math.ceil(pixelsPerTile / 2);
            yGraphDiff = worldMapRect.height - yGraphDiff;
            yGraphDiff += (int) worldMapRect.getY();

            return yGraphDiff;
        }
        return Integer.MIN_VALUE;
    }
}
