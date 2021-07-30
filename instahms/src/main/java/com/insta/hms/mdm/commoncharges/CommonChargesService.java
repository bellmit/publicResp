package com.insta.hms.mdm.commoncharges;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommonChargesService extends MasterService {

  public CommonChargesService(CommonChargesRepository repo, CommonChargesValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the common charge.
   *
   * @param chargeName the charge name
   * @return the common charge
   */
  public BasicDynaBean getCommonCharge(String chargeName) {

    Map<String, String> params = new HashMap<String, String>();
    params.put("charge_name", chargeName);
    return ((CommonChargesRepository) getRepository()).findByPk(params);
  }

  public List<BasicDynaBean> getCommonChargesItemSubGroupTaxDetails(String actDescriptionId) {
    return ((CommonChargesRepository) getRepository())
        .getCommonChargesItemSubGroupTaxDetails(actDescriptionId);
  }
}
