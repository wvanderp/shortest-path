package shortestpath;

import org.junit.Assert;
import org.junit.Test;

public class PrimitiveIntListTests {
    @Test
    public void tryAddAndGet() {
        PrimitiveIntList intList = new PrimitiveIntList();
        intList.add(10);
        intList.add(11);
        Assert.assertEquals(2, intList.size());
        Assert.assertEquals(10, intList.get(0));
        Assert.assertEquals(11, intList.get(1));
    }

    @Test
    public void tryGrow() {
        PrimitiveIntList intList = new PrimitiveIntList(4);
        intList.add(10);
        intList.add(11);
        intList.add(12);
        intList.add(13);
        intList.add(14);
        Assert.assertEquals(5, intList.size());
        int sum = 0;
        for (int i = 0; i < intList.size(); i++) {
            sum += intList.get(i);
        }
        Assert.assertEquals(10 + 11 + 12 + 13 + 14, sum);
    }

    @Test
    public void tryInitialize() {
        PrimitiveIntList intList = new PrimitiveIntList(3, true);
        Assert.assertEquals(3, intList.size());
        Assert.assertEquals(0, intList.get(0));
        Assert.assertEquals(0, intList.get(1));
        Assert.assertEquals(0, intList.get(2));
        intList.set(0, 10);
        intList.set(1, 11);
        intList.set(2, 12);
        Assert.assertEquals(10, intList.get(0));
        Assert.assertEquals(11, intList.get(1));
        Assert.assertEquals(12, intList.get(2));
    }

    @Test
    public void tryIterate() {
        int sum = 0;
        PrimitiveIntList intList = new PrimitiveIntList();
        intList.add(10);
        intList.add(11);
        for (int i = 0; i < intList.size(); i++) {
            sum += intList.get(i);
        }
        Assert.assertEquals(10 + 11, sum);
    }

    @Test
    public void tryRemove() {
        PrimitiveIntList intList = new PrimitiveIntList();
        intList.add(10);
        intList.add(11);
        Assert.assertEquals(2, intList.size());
        Assert.assertEquals(10, intList.removeAt(0));
        Assert.assertEquals(true, intList.remove(11));
        Assert.assertEquals(true, intList.isEmpty());
    }
}
