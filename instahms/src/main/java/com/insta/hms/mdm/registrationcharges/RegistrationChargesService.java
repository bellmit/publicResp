package com.insta.hms.mdm.registrationcharges;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegistrationChargesService extends MasterService {

  public RegistrationChargesService(
      RegistrationChargesRepository repo, RegistrationChargesValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the registration charges.
   *
   * @param bedType the bed type
   * @param orgId the org id
   * @return the registration charges
   */
  public BasicDynaBean getRegistrationCharges(String bedType, String orgId) {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("bed_type", bedType);
    filterMap.put("org_id", orgId);
    List regCharges = this.getRepository().listAll(null, filterMap, null);

    if (regCharges != null && !regCharges.isEmpty()) {
      return (BasicDynaBean) regCharges.get(0);
    }
    return null;
  }
}
