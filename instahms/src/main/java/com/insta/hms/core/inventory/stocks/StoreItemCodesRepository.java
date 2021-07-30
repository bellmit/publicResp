package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class StoreItemCodesRepository extends GenericRepository {

  public StoreItemCodesRepository() {
    super("store_item_codes");
  }

  public BasicDynaBean getDrugCodeType(Integer medicineId, String[] drugCodeTypes) {
    if (drugCodeTypes == null || drugCodeTypes.length == 0) {
      return null;
    }

    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("medicine_id", medicineId);
    filterMap.put("code_type", Arrays.asList(drugCodeTypes));

    List<BasicDynaBean> codeTypes = findByCriteria(filterMap);
    return codeTypes != null && !codeTypes.isEmpty() ? codeTypes.get(0) : null;
  }
  
  private static final String MAPPED_DRUG_CODE_QUERY = "select hict.code_type, "
      + " COALESCE(sic.item_code,'') item_code, hict.health_authority from ha_item_code_type hict "
      + " left join store_item_codes sic on (sic.medicine_id = hict.medicine_id "
      + " and sic.code_type = hict.code_type) where hict.medicine_id = ?";

  public List<BasicDynaBean> getMappedDrugCode(int medicieneId) {
    return DatabaseHelper.queryToDynaList(MAPPED_DRUG_CODE_QUERY, new Object[] { medicieneId });
  }
}
