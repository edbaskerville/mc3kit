package mc3kit.util;

import java.util.*;
import static org.junit.Assert.*;
import static mc3kit.util.Utils.*;

import mc3kit.MC3KitException;

import org.junit.*;

public class UtilsTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void flatMap() throws MC3KitException {
    Map<String, Object> flatMap = makeMap(
      "key1", 2, "key2", new double[] {5, 4, 3}, "key3", new LinkedHashMap<String, Object>()
    );
    assertEquals(2, flatMap.get("key1"));
    assertArrayEquals(new double[] {5, 4, 3}, (double[])flatMap.get("key2"), 0.0);
    assertEquals(new LinkedHashMap<String, Object>(), flatMap.get("key3"));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void oneLevelHMap() throws Exception {
    Map<String, Object> flatMap = makeMap(
      "0.mean", 4.5,
      "0.prec", 4.5,
      "0.name", "zero"
    );
    Map<String, Object> hMap = makeHierarchicalMap(flatMap);
    Map<String, Object> obj = (Map<String, Object>)hMap.get("0");
    System.err.printf("%s\n", hMap);
    assertEquals(4.5, obj.get("mean"));
    assertEquals(4.5, obj.get("prec"));
    assertEquals("zero", obj.get("name"));
  }
}
