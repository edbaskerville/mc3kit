package mc3kit;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;
import org.tmatesoft.sqljet.*;
import org.tmatesoft.sqljet.core.*;
import org.tmatesoft.sqljet.core.internal.table.SqlJetTable;
import org.tmatesoft.sqljet.core.table.*;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

public class SqlJetTester {
  File file;
  SqlJetDb db;
  ISqlJetTable table;
  int count;
  
  @Before
  public void setUp() throws Exception {
    file = new File("test.sqlite");
    System.err.printf("file is located at %s\n", file.getCanonicalPath());
    db = new SqlJetDb(file, true);
    db.open();
    db.createTable(
      "CREATE TABLE samples (iteration INTEGER, parameter TEXT, value TEXT)"
     );
    table = db.getTable("samples");
    db.createIndex(
      "CREATE INDEX iterationIndex on samples (iteration)"
    );
  }
  
  @After
  public void tearDown() throws Exception {
    db.close();
    file.delete();
  }
  
  private void populateMany() throws SqlJetException {
    count = 10000;
    db.beginTransaction(SqlJetTransactionMode.WRITE);
    for(int i = 0; i < count; i++) {
      table.insert(i+1, "param1", 0.35);
      table.insert(i+1, "param2", 0.35);
      table.insert(i+1, "param3", 0.35);
    }
    db.commit();
    System.err.printf("File length: %d\n", file.length());
  }
  
  @Test
  public void testPopulate() throws Exception {
    populateMany();
  }
  
  @Test
  public void testRandomAccess() throws Exception {
    Uniform unif = new Uniform(new MersenneTwister());
    
    populateMany();
    
    db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
    ISqlJetCursor cursor = table.open();
    long sampCount = cursor.getRowCount();
    System.err.printf("sampCount = %d\n", sampCount);
    cursor.close();
    
    for(int i = 0; i < count; i++) {
      long iter = unif.nextLongFromTo(1, sampCount / 3);
      ISqlJetCursor entries = table.lookup("iterationIndex", iter);
      assertEquals(3, entries.getRowCount());
      do {
        long iterCheck = entries.getInteger("iteration");
        assertEquals(iterCheck, iter);
        String paramName = entries.getString("parameter");
        String value = entries.getString("value");
//        System.err.printf("%d %s %s\n", iter, paramName, value);
      } while(entries.next());
      entries.close();
    }
  }
}
