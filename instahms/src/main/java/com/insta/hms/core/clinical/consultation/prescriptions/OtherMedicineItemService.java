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
public class OtherMedicineItemService extends PrescriptionItem {

  @LazyAutowired
  private PrescriptionFavouritesService prescriptionFavouritesService;

  public OtherMedicineItemService(OtherMedicinePrescriptionsRepository repo) {
    super("prescription_id", repo);
  }

  @Override
  public BasicDynaBean copyToBean(Map<String, Object> prescription, BasicDynaBean mbean,
      BasicDynaBean bean, String operation) {
    ConversionUtils.copyToDynaBean(prescription, bean);
    bean.set("medicine_quantity", prescription.get("prescribed_qty"));
    bean.set("medicine_remarks", prescription.get("item_remarks"));
    bean.set("username", mbean.get("username"));
    bean.set("route_of_admin", prescription.get("route_id"));
    bean.set("mod_time", DateUtil.getCurrentTimestamp());
    if (bean.get("duration") == null) {
      bean.set("duration_units", null);
    }
    if (operation.equals("insert")) {
      bean.set("medicine_name", prescription.get("item_name"));
    }

    return bean;
  }

  @Override
  public BasicDynaBean insert(Map<String, Object> prescription, BasicDynaBean mbean,
      ValidationErrorMap errMap) {
    BasicDynaBean bean = super.insert(prescription, mbean, errMap);
    if (bean != null && prescription.get("add_to_favourite") != null
        && (Boolean) prescription.get("add_to_favourite")) {
      prescriptionFavouritesService.insertPresOtherMedicineFavourite(bean,
          (String) prescription.get("doctor_id"), (String) prescription.get("special_instr"),
          errMap);
    }
    return bean;
  }

}
