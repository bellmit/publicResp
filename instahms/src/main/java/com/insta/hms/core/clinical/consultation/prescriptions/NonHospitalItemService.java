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
public class NonHospitalItemService extends PrescriptionItem {

  @LazyAutowired
  private PrescriptionFavouritesService prescriptionFavouritesService;

  public NonHospitalItemService(NonHospitalPrescriptionsRepository repo) {
    super("prescription_id", repo);
  }

  @Override
  public BasicDynaBean copyToBean(Map<String, Object> prescription, BasicDynaBean mbean,
      BasicDynaBean bean, String operation) {
    ConversionUtils.copyToDynaBean(prescription, bean);
    if (bean.get("duration") == null) {
      bean.set("duration_units", null);
    }
    bean.set("username", mbean.get("username"));
    bean.set("medicine_quantity", prescription.get("prescribed_qty"));
    bean.set("mod_time", DateUtil.getCurrentTimestamp());
    return bean;
  }

  @Override
  public BasicDynaBean insert(Map<String, Object> prescription, BasicDynaBean mbean,
      ValidationErrorMap errMap) {
    BasicDynaBean bean = super.insert(prescription, mbean, errMap);
    if (bean != null && prescription.get("add_to_favourite") != null
        && (Boolean) prescription.get("add_to_favourite")) {
      prescriptionFavouritesService.insertPresDocOtherFavourite(bean,
          (String) prescription.get("doctor_id"), (String) prescription.get("special_instr"),
          errMap);
    }
    return bean;
  }
}
