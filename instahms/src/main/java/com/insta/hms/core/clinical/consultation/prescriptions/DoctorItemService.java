package com.insta.hms.core.clinical.consultation.prescriptions;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.prescriptionfavourites.PrescriptionFavouritesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DoctorItemService extends PrescriptionItem {

  @LazyAutowired
  private PrescriptionFavouritesService prescriptionFavouritesService;

  public DoctorItemService(DoctorPrescriptionsRepository repo) {
    super("prescription_id", repo);
  }

  @Override
  public BasicDynaBean copyToBean(Map<String, Object> prescription, BasicDynaBean mbean,
      BasicDynaBean bean, String operation) {
    ConversionUtils.copyToDynaBean(prescription, bean);
    bean.set("cons_remarks", prescription.get("item_remarks"));
    bean.set("username", mbean.get("username"));
    bean.set("mod_time", DateUtil.getCurrentTimestamp());
    if (operation.equals("insert")) {
      if (prescription.get("presc_activity_type") != null
          && prescription.get("presc_activity_type").equals("DEPT")) {
        bean.set("doctor_id", null);
        bean.set("dept_id", prescription.get("item_id"));
      } else {
        bean.set("dept_id", null);
        bean.set("doctor_id", prescription.get("item_id"));
      }
    }
    return bean;
  }

  @Override
  public BasicDynaBean insert(Map<String, Object> prescription, BasicDynaBean mbean,
      ValidationErrorMap errMap) {
    BasicDynaBean bean = super.insert(prescription, mbean, errMap);
    if (bean != null && prescription.get("add_to_favourite") != null
        && (Boolean) prescription.get("add_to_favourite")) {
      prescriptionFavouritesService.insertPresDoctorFavourite(bean, prescription, errMap);
    }
    return bean;
  }

}
