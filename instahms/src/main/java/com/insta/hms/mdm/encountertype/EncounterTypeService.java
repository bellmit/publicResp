package com.insta.hms.mdm.encountertype;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class EncounterTypeService.
 *
 * @author deepak_kk
 */
@Service
public class EncounterTypeService extends MasterService {

  private static final String ENCOUNTER_TYPE_ID = "encounter_type_id";

  /** The encounter type repository. */
  private EncounterTypeRepository encounterTypeRepository;

  /**
   * Instantiates a new encounter type service.
   *
   * @param repo
   *          the repo
   * @param validator
   *          the validator
   */
  public EncounterTypeService(EncounterTypeRepository repo, EncounterTypeValidator validator) {
    super(repo, validator);
    this.encounterTypeRepository = repo;
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<>();
    referenceMap.put("encounterTypeDetails",
        encounterTypeRepository.listAll(null, null, null, ENCOUNTER_TYPE_ID));
    return referenceMap;
  }

  /**
   * Gets the visit default encounter.
   *
   * @param visitType
   *          the visit type
   * @param isDaycare
   *          the is daycare
   * @return the visit default encounter
   */
  public BasicDynaBean getVisitDefaultEncounter(String visitType, boolean isDaycare) {
    return encounterTypeRepository.getVisitDefaultEncounter(visitType, isDaycare);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Integer update(BasicDynaBean bean) {

    int success = 0;
    String opEncounterDefault = (String) bean.get("op_encounter_default");
    String ipEncounterDefault = (String) bean.get("ip_encounter_default");
    String daycareEncounterDefault = (String) bean.get("daycare_encounter_default");

    Integer encounterTypeId = (Integer) bean.get(ENCOUNTER_TYPE_ID);
    Map<String, Integer> keys = new HashMap<>();
    keys.put(ENCOUNTER_TYPE_ID, encounterTypeId);
    success = encounterTypeRepository.update(bean, keys);
    if (success > 0) {
      if ("Y".equals(opEncounterDefault)) {
        encounterTypeRepository.updateUndefaultValues("o", encounterTypeId, "N");
      }
      if ("Y".equals(ipEncounterDefault)) {
        encounterTypeRepository.updateUndefaultValues("i", encounterTypeId, "N");
      }
      if ("Y".equals(daycareEncounterDefault)) {
        encounterTypeRepository.updateUndefaultValues("d", encounterTypeId, "N");
      }
      return 1;
    }

    return 0;
  }

  /**
   * Get ip op applicable encounter types.
   * @param isOpApplicable boolean op applicable
   * @param isIpApplicable boolean ip applicable
   * @return list of basic dyanbeans
   */
  public  List<BasicDynaBean> getOpIpApplicableEncounterTypes(boolean isOpApplicable,
      boolean isIpApplicable) {
    return encounterTypeRepository.getOpIpApplicableEncounterTypes(isOpApplicable, isIpApplicable);
  }
}
