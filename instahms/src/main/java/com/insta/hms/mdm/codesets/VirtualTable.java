package com.insta.hms.mdm.codesets;

import com.amazonaws.util.StringUtils;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.StringUtil;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vakulsaxena.
 *
 */
public class VirtualTable {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(VirtualTable.class);

  private static CodeSetsRepository coderepo =
      ApplicationContextProvider.getApplicationContext().getBean(CodeSetsRepository.class);

  public static final String STR_SEPERATOR = ":";

  public String tablename = "";

  public String entityname = "";

  public String entityid = "";

  public List<Map<String, Object>> masterList = new ArrayList<Map<String, Object>>();

  public List<Integer> entityIds = new ArrayList<Integer>();

  private static HashMap<String, String> entityClassMap = new HashMap<String, String>();

  static {
    /*
     * entityClassMap.put("<Table Name in Code System Categories table>",
     * "<A class that extends VirtualTable and implement helper interface>");
     */
    entityClassMap.put("language_master", "LanguageMaster");
  }

  /**
   * Get instance for a virtual table based on details.
   * 
   * @param details entity details passed by constructor
   * @return VirtualTable object
   */
  public static VirtualTable getInstance(String details) {
    String[] entity = !StringUtils.isNullOrEmpty(details) ? details.split(STR_SEPERATOR) : null;
    if (!StringUtil.isNullOrEmpty(entity[1])) {
      try {
        Class<?> entityClass =
            Class.forName("com.insta.hms.mdm.codesets." + entityClassMap.get(entity[1]));
        Constructor[] entityClassConsArr = entityClass.getConstructors();
        return (VirtualTable) entityClassConsArr[0].newInstance(entity[1], entity[2], entity[3]);
      } catch (Exception exception) {
        logger.error(exception.getMessage());
        logger.error("Error creating virtual table for entity name : " + entity[1], exception);
      }
    }
    return null;
  }
  
  /**
   * Constructor for virtual table.
   * 
   * @param tablename master table name
   * @param entityname label for the master table entity
   * @param entityid identifier for the master table entity
   */
  public VirtualTable(String tablename, String entityname, String entityid) {
    super();
    this.tablename = tablename;
    this.entityname = entityname;
    this.entityid = entityid;
  }
  
  /**
   * Method to get sorted master list.
   * 
   * @return List of Master data
   */
  public List<Map<String, Object>> getMasterList() {
    return masterList;
  }
  
  /**
   * Setter for master list.
   * 
   * @param masterList set master list map
   */
  public void setMasterList(List<Map<String, Object>> masterList) {
    this.masterList = masterList;
  }

  /**
   * Get list of entity ids.
   * 
   * @return List of entity ids 
   */
  public List<Integer> getEntityIds() {
    return entityIds;
  }

  /**
   * Set the list for entity IDs.
   * 
   * @param entityIds List of entity ids 
   */
  public void setEntityIds(List<Integer> entityIds) {
    this.entityIds = entityIds;
  }

  /**
   * Get Master Data map.
   * 
   * @param entityName entity name of the virtual table
   * @param entityId entity id of the virtual entity
   * @return HashMap of master data
   */
  public HashMap<String, Object> getMasterMap(String entityName, Integer entityId) {
    HashMap<String, Object> masterMap = new HashMap<String, Object>();
    masterMap.put("entity_id", entityId);
    masterMap.put("entity_name", entityName);
    return masterMap;
  }

  /**
   * Get Mapped Paged List of the Master list data.
   * 
   * @param params params map 
   * @return PageList object for master data
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public PagedList getMappedPagedList(Map<String, Object> params) {
    PagedList pl = new PagedList();
    List dynaList = coderepo.getVirtualCodeSets(params, getEntityIds());
    if (dynaList != null) {
      List<Map<String, Object>> codeSetList = ConversionUtils.copyListDynaBeansToMap(dynaList);
      List<Map<String, Object>> resultList = getResultMapList(getMasterList(), codeSetList);
      int pageNum =
          params.get("pageNum") != null && NumberUtils.isDigits(((String) params.get("pageNum")))
              ? Integer.parseInt((String) params.get("pageNum"))
              : 1;
      int pageSize = pl.getPageSize();
      List<Map<String, Object>> dtoList = new ArrayList<Map<String, Object>>();
      for (int i = ((pageNum - 1) * pageSize); 
          i < Integer.min((pageNum * pageSize), masterList.size()); i++) {
        dtoList.add(resultList.get(i));
      }
      pl.setDtoList(dtoList);
      pl.setPageNumber(pageNum);
      pl.setTotalRecords(masterList.size());
      Map countmap = new HashMap();
      countmap.put("count", masterList.size());
      pl.setCountInfo(countmap);
    }
    return pl;
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getResultMapList(List<Map<String, Object>> masterList,
      List codeSetList) {
    List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
    int idxMaster = 0;
    int idxCodelist = 0;
    if (codeSetList != null && codeSetList.size() > 0) {
      while (idxMaster < masterList.size()) {
        Map<String, Object> masterMap = masterList.get(idxMaster);
        int masterEntity = (Integer) masterMap.get("entity_id");
        Map<String, Object> codeMap = new HashMap<String, Object>();
        int codesetEntity = Integer.MAX_VALUE;
        if (codeSetList.size() > idxCodelist) {
          codeMap = (Map<String, Object>) codeSetList.get(idxCodelist);
          codesetEntity = (Integer) codeMap.get("entity_id");
        }
        if (masterEntity == codesetEntity) {
          resultList.add(getResultMap((Integer) codeMap.get("code_set_id"),
              (Integer) codeMap.get("code_system_category_id"),
              (Integer) codeMap.get("code_system_id"), masterEntity, (String) codeMap.get("code"),
              (String) codeMap.get("code_description"), (String) masterMap.get("entity_name")));
          idxMaster++;
          idxCodelist++;
        } else if (masterEntity > codesetEntity) {
          idxCodelist++;
        } else if (masterEntity < codesetEntity) {
          resultList.add(getResultMap(null, 0, 0, (Integer) masterMap.get("entity_id"), "", "",
              (String) masterMap.get("entity_name")));
          idxMaster++;
        }
      }
    } else if (codeSetList != null && codeSetList.size() == 0) {
      for (Map<String, Object> mas : masterList) {
        resultList.add(getResultMap(null, 0, 0, (Integer) mas.get("entity_id"), "", "",
            (String) mas.get("entity_name")));
      }
    }
    return resultList;
  }

  private HashMap<String, Object> getResultMap(Integer codeSetId, Integer codeSystemCategoryId,
      Integer codeSystemId, Integer entityId, String code, String codeDescription,
      String entityName) {
    HashMap<String, Object> resultMap = new HashMap<String, Object>();
    resultMap.put("entity_id", entityId);
    resultMap.put("entity_name", entityName);
    resultMap.put("code_set_id", codeSetId);
    resultMap.put("code_system_category_id", codeSystemCategoryId);
    resultMap.put("code_system_id", codeSystemId);
    resultMap.put("code_description", codeDescription);
    resultMap.put("code", code);
    return resultMap;

  }


}
