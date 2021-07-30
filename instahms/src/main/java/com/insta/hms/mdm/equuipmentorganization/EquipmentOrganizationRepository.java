package com.insta.hms.mdm.equuipmentorganization;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.Map;

/** The Class EquipmentOrganizationRepository.*/
@Repository
public class EquipmentOrganizationRepository extends MasterRepository<String> {

  /** The equipmentt organization service. */
  @LazyAutowired
  private EquipmentOrganizationService equipmenttOrganizationService;

  /** Instantiates a new equipment organization repository. */
  public EquipmentOrganizationRepository() {
     super("equip_org_details", "equip_id");
  }

  /**
   * Updateorg.
   *
   * @param orgDetails the org details
   * @param key the key
   * @return true, if successful
   */
  public boolean updateorg(BasicDynaBean orgDetails, Map key) {
    return update(orgDetails, key) > 0;
  }

}
