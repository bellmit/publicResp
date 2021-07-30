package com.insta.hms.mdm.billlabel;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class BillLabelService.
 */
@Service
public class BillLabelService extends MasterService {

  /**
   * Instantiates a new bill label service.
   *
   * @param billLabelRepository the bill label repository
   * @param billLabelValidator the bill label validator
   */
  public BillLabelService(BillLabelRepository billLabelRepository,
      BillLabelValidator billLabelValidator) {
    super(billLabelRepository, billLabelValidator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<String, List<BasicDynaBean>>();
    referenceMap.put("namesList", lookup(false));
    return referenceMap;
  }

}
