package com.insta.hms.core.clinical.consultation.prescriptions;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.prescriptionfavourites.PrescriptionFavouritesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InvestigationItemService extends PrescriptionItem {

  @LazyAutowired
  private PrescriptionFavouritesService prescriptionFavouritesService;

  public InvestigationItemService(InvestigationPrescriptionsRepository repo) {
    super("op_test_pres_id", repo);

  }

  @Override
  public BasicDynaBean copyToBean(Map<String, Object> prescription, BasicDynaBean mbean,
      BasicDynaBean bean, String operation) {
    ConversionUtils.copyToDynaBean(prescription, bean);
    bean.set("test_remarks", prescription.get("item_remarks"));
    bean.set("ispackage", prescription.get("is_package"));
    bean.set("username", mbean.get("username"));
    bean.set("mod_time", DateUtil.getCurrentTimestamp());
    if (operation.equals("insert")) {
      bean.set("test_id", prescription.get("item_id"));
    }
    return bean;
  }

  @Override
  public BasicDynaBean insert(Map<String, Object> prescription, BasicDynaBean mbean,
      ValidationErrorMap errMap) {
    BasicDynaBean bean = super.insert(prescription, mbean, errMap);
    if (bean != null && prescription.get("add_to_favourite") != null
        && (Boolean) prescription.get("add_to_favourite")) {
      prescriptionFavouritesService.insertPresTestFavourite(bean,
          (String) prescription.get("doctor_id"), (String) prescription.get("special_instr"),
          errMap);
    }
    return bean;
  }

  /**
   * Get test for prescription.
   * @param mrNo the string
   * @param bedType the string
   * @param orgId the string
   * @param patientType the string
   * @param insPlanId the string
   * @param centerId the integer
   * @param tpaId the string
   * @param searchQuery the string
   * @param deptId the string
   * @param gender the string
   * @param isPrescribable the boolean
   * @param itemLimit the integer
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> getTestsForPrescription(String mrNo, String bedType, String orgId,
      String patientType, Integer insPlanId, Integer centerId, String tpaId, String searchQuery,
      String deptId, Integer age, String ageIn, String gender, Boolean isPrescribable, 
      Integer itemLimit) {
    return ((InvestigationPrescriptionsRepository) repo).getTestsForPrescription(mrNo, bedType,
        orgId, patientType, insPlanId, centerId, tpaId, searchQuery, deptId, age,
        ageIn, gender, isPrescribable,
        itemLimit);
  }

  public List<BasicDynaBean> invConductionDateForIds(String mrNo, List<String> idList) {
    return ((InvestigationPrescriptionsRepository) repo).invConductionDateForIds(mrNo, idList);
  }

}
