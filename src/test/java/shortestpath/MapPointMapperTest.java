package shortestpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.worldmap.WorldMap;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import org.junit.Test;

public class MapPointMapperTest {

    @Test
    public void testNullWorldMap() {
        Client client = mock(Client.class);
        when(client.getWorldMap()).thenReturn(null);

        int packed = MapPointMapper.calculateMapPoint(client, 10, 10);
        assertEquals(WorldPointUtil.UNDEFINED, packed);

        int x = MapPointMapper.mapWorldPointToGraphicsPointX(client, WorldPointUtil.packWorldPoint(0, 0, 0));
        int y = MapPointMapper.mapWorldPointToGraphicsPointY(client, WorldPointUtil.packWorldPoint(0, 0, 0));
        assertEquals(Integer.MIN_VALUE, x);
        assertEquals(Integer.MIN_VALUE, y);
    }

    @Test
    public void testCalculateMapPointIdentityAtCenter() {
        Client client = mock(Client.class);
        WorldMap worldMap = mock(WorldMap.class);
        Widget widget = mock(Widget.class);
        Point mapPos = new Point(0, 0);

        when(client.getWorldMap()).thenReturn(worldMap);
        when(worldMap.getWorldMapZoom()).thenReturn(1.0f);
        when(worldMap.getWorldMapPosition()).thenReturn(mapPos);

        when(client.getWidget(ComponentID.WORLD_MAP_MAPVIEW)).thenReturn(widget);
        Rectangle rect = new Rectangle(100, 200, 64, 64);
        when(widget.getBounds()).thenReturn(rect);

        int basePacked = WorldPointUtil.packWorldPoint(0, 0, 0);
        int middleX = MapPointMapper.mapWorldPointToGraphicsPointX(client, basePacked);
        int middleY = MapPointMapper.mapWorldPointToGraphicsPointY(client, basePacked);

        // When we ask for the map point at the exact middle, we should get the same
        // base point
        int calculated = MapPointMapper.calculateMapPoint(client, middleX, middleY);

        assertEquals(basePacked, calculated);
    }
}
