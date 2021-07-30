package com.insta.hms.mdm.allergy;

import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Allergen Master service.
 */
@Service
public class AllergenMasterService extends MasterService {

  public AllergenMasterService(AllergenMasterRepository allergenMasterRepository,
      AllergenMasterValidator allergenMasterValidator) {
    super(allergenMasterRepository, allergenMasterValidator);

  }

  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addFilterForLookUp(SearchQueryAssembler qb, String likeValue, String matchField,
      boolean contains, Map<String, String[]> parameters) {
    if (!likeValue.trim().isEmpty()) {
      String filterText = likeValue.trim() + "%";
      if (contains) {
        filterText = "%" + likeValue.trim() + "%";
      }
      qb.addFilter(QueryAssembler.STRING, matchField, "ILIKE", filterText);
    }
    String allergyTypeId = (null != parameters && parameters.containsKey("allergy_type_id"))
        ? parameters.get("allergy_type_id")[0]
        : null;
    if (!StringUtils.isEmpty(allergyTypeId)) {
      qb.addFilter(QueryAssembler.INTEGER, "allergy_type_id", "=", Integer.parseInt(allergyTypeId));
    }
  }

}
