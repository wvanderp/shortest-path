package shortestpath;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.worldmap.WorldMap;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import org.junit.Test;

/**
 * Table-driven tests for {@link MapPointMapper} so additional cases (from logs)
 * can be appended easily.
 */
public class MapPointMapperTableTest {
    private static class Case {
        final String name;
        final float zoom;
        final Point mapPos;
        final Rectangle widgetBounds;
        final int mouseX, mouseY;
        final int lastMenuX, lastMenuY;
        final int expectedFromLastMenu;
        final int expectedFromMouse;

        Case(String name, float zoom, Point mapPos, Rectangle widgetBounds,
                int mouseX, int mouseY, int lastMenuX, int lastMenuY,
                int expectedFromLastMenu, int expectedFromMouse) {
            this.name = name;
            this.zoom = zoom;
            this.mapPos = mapPos;
            this.widgetBounds = widgetBounds;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.lastMenuX = lastMenuX;
            this.lastMenuY = lastMenuY;
            this.expectedFromLastMenu = expectedFromLastMenu;
            this.expectedFromMouse = expectedFromMouse;
        }
    }

    @Test
    public void tableDrivenMapPointTests() {
        List<Case> cases = new ArrayList<>();

        cases.add(new Case(
                "Lumbridge fully zoomed in",
                8.0f,
                new Point(3232, 3232),
                new Rectangle(178, 8, 1455, 798),
                816, 559,
                827, 511,
                105483414,
                105286805));

        cases.add(new Case(
                "Lumbridge half zoomed in",
                4.0f,
                new Point(3241, 3232),
                new Rectangle(178, 8, 1455, 798),
                832, 502,
                832, 463,
                105450646,
                105122966));

                cases.add(new Case(
            "Lumbridge small window",
            1.0f,
            new Point(3231, 3235),
            new Rectangle(178, 8, 331, 299),
            332, 214,
            335, 177,
                105352343,
                104139924
        ));

        cases.add(new Case(
                "Lumbridge zoomed out",
                1.0f,
                new Point(3232, 3232),
                new Rectangle(178, 8, 1455, 798),
                884, 462,
                895, 422,
                105385110,
                104074379));

        cases.add(new Case(
                "kurend castle",
                8.0f,
                new Point(1682, 3689),
                new Rectangle(178, 8, 1455, 798),
                574, 577,
                568, 532,
                120358504,
                120194664));

                cases.add(new Case(
            "mage bank",
            8.0f,
            new Point(3115, 10361),
            new Rectangle(178, 8, 1213, 798),
            780, 492,
            782, 451,
                339315754,
                339151914
        ));

        // Add more cases here following the same pattern.

        for (Case c : cases) {
            Client client = mock(Client.class);
            WorldMap worldMap = mock(WorldMap.class);
            Widget widget = mock(Widget.class);

            when(client.getWorldMap()).thenReturn(worldMap);
            when(worldMap.getWorldMapZoom()).thenReturn(c.zoom);
            when(worldMap.getWorldMapPosition()).thenReturn(c.mapPos);

            when(client.getWidget(ComponentID.WORLD_MAP_MAPVIEW)).thenReturn(widget);
            when(widget.getBounds()).thenReturn(c.widgetBounds);

            int calculatedFromLast = MapPointMapper.calculateMapPoint(client, c.lastMenuX, c.lastMenuY);
            assertEquals(c.name + " - lastMenuOpenedPoint", c.expectedFromLastMenu, calculatedFromLast);

            int calculatedFromMouse = MapPointMapper.calculateMapPoint(client, c.mouseX, c.mouseY);
            assertEquals(c.name + " - mouseCanvasPosition", c.expectedFromMouse, calculatedFromMouse);
        }
    }
}
