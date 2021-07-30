package com.insta.hms.mdm.equuipmentorganization;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class EquipmentOrganizationService.
 */
@Service
public class EquipmentOrganizationService extends MasterService {

  /** The equipment organization repository. */
  @LazyAutowired
  private EquipmentOrganizationRepository equipmentOrganizationRepository;

  /**
   * Instantiates a new equipment organization service.
   *
   * @param er
   *          the er
   * @param ev
   *          the ev
   */
  public EquipmentOrganizationService(EquipmentOrganizationRepository er,
      EquipmentOrganizationValidation ev) {
    super(er, ev);
  }

  /**
   * Update equipment org details.
   *
   * @param parameters
   *          the parameters
   * @param equipId
   *          the equip id
   * @param orgId
   *          the org id
   * @return true, if successful
   */
  public boolean updateEquipmentOrgDetails(Map<String, String[]> parameters, String equipId,
      String orgId) {
    BasicDynaBean orgDetails = equipmentOrganizationRepository.getBean();
    ConversionUtils.copyToDynaBean(parameters, orgDetails);
    orgDetails.set("equip_id", equipId);
    orgDetails.set("org_id", orgId);
    orgDetails.set("applicable", true);
    Map keys = new HashMap();
    keys.put("equip_id", equipId);
    keys.put("org_id", orgId);
    return equipmentOrganizationRepository.updateorg(orgDetails, keys);
  }

}
