package testng.utils;

import au.com.bytecode.opencsv.CSVReader;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericHibernateRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestRepoInit {
  
  private Logger logger = LoggerFactory.getLogger(TestRepoInit.class);
  
  private GenericHibernateRepository hibernateRepository = null;

  private List<String> inputList = null;
  
  public TestRepoInit() {
    inputList = new ArrayList<String>();
  }

  public TestRepoInit(GenericHibernateRepository hibernateRepository) {
    this();
    this.hibernateRepository = hibernateRepository;
  }

  public void insert(String tableName){
    inputList.add(tableName);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String,Object> initializeRepo() {
    if(null != hibernateRepository) {
      hibernateRepository.startTransaction();
    }
    tearDown();
    Map responseMap = new HashMap();
    for (String tableName:inputList) {
      responseMap.put(tableName,readCsv(tableName));
    }
    return responseMap;
  }
  
  public void tearDown() {
    for (int i = inputList.size()-1;i>=0;i--) {
      String query = "DELETE FROM "+inputList.get(i)+" CASCADE";
      if(null == hibernateRepository) {
        DatabaseHelper.update(query);
      } else {
        hibernateRepository.executeUpdateQuery(query);
      }
    }
  }
  
  public List<Map<String,String>> readCsv(String tableName) {
    
    List<Map<String,String>> tableList = new ArrayList<Map<String,String>>();
    try {
      Path currentRelativePath = Paths.get("");
      String path = currentRelativePath.toAbsolutePath().toString();
      path = path + ("/src/test/java/testng/utils/TableCsvs/");
      CSVReader reader = new CSVReader(new FileReader(path+tableName+".csv"));
      String[] columnRecord = null;
      columnRecord = reader.readNext();
      StringBuilder columnsBuilder = new StringBuilder(" ("+columnRecord[0]);
      for (int i = 1 ; i < columnRecord.length; i++) {
        columnsBuilder.append(","+columnRecord[i]);
      }
      columnsBuilder.append(") ");
      String[] valueRecord = null;
      StringBuilder valuesBuilder = null;
      while ((valueRecord = reader.readNext()) != null) {
        Map<String,String> rowMap = new HashMap<String,String>();
        valuesBuilder = new StringBuilder(" ("+handleString(valueRecord[0]));
        rowMap.put(columnRecord[0],createMapEntry(valueRecord[0]));
        for (int i=1;i<valueRecord.length;i++) {
          valuesBuilder.append(","+handleString(valueRecord[i]));
          rowMap.put(columnRecord[i],createMapEntry(valueRecord[i]));
        }
        valuesBuilder.append(") ");
        String query = "Insert into "+tableName 
            + columnsBuilder.toString() + "values" + valuesBuilder.toString();
        if(null == hibernateRepository) {
          DatabaseHelper.update(query);
        } else {
          hibernateRepository.executeUpdateQuery(query);
        }
        tableList.add(rowMap);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return tableList;
  }
  
  public String createMapEntry(String x){
    return x.matches("'.*'") ? x.substring(1,x.length()-1) : x;
  }
  
  public String handleString(String x) {
  
    if (x.matches("'.*'")) {
      x = x.replace("'","''");
      x = x.substring(1,x.length()-1);
    }
    if(x.isEmpty()) {
      return null;
    }else {
      return x;
    }
  }

  public void rollbackTransaction() {
    if(null != hibernateRepository) {
      hibernateRepository.rollbackTransaction();
    }
  }
}
