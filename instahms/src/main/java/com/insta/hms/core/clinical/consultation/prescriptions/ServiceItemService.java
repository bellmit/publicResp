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
public class ServiceItemService extends PrescriptionItem {

  @LazyAutowired
  private PrescriptionFavouritesService prescriptionFavouritesService;

  public ServiceItemService(ServicePrescriptionsRepository repo) {
    super("op_service_pres_id", repo);
  }

  @Override
  public BasicDynaBean copyToBean(Map<String, Object> prescription, BasicDynaBean mbean,
      BasicDynaBean bean, String operation) {
    ConversionUtils.copyToDynaBean(prescription, bean);
    bean.set("username", mbean.get("username"));
    bean.set("service_remarks", prescription.get("item_remarks"));
    bean.set("tooth_unv_number", prescription.get("tooth_unv_number"));
    bean.set("tooth_fdi_number", prescription.get("tooth_fdi_number"));
    bean.set("qty", prescription.get("prescribed_qty"));
    bean.set("mod_time", DateUtil.getCurrentTimestamp());
    if (operation.equals("insert")) {
      bean.set("service_id", prescription.get("item_id"));
    }
    return bean;
  }

  @Override
  public BasicDynaBean insert(Map<String, Object> prescription, BasicDynaBean mbean,
      ValidationErrorMap errMap) {
    BasicDynaBean bean = super.insert(prescription, mbean, errMap);
    if (bean != null && prescription.get("add_to_favourite") != null
        && (Boolean) prescription.get("add_to_favourite")) {
      prescriptionFavouritesService.insertPresServiceFavourite(bean,
          (String) prescription.get("doctor_id"), (String) prescription.get("special_instr"),
          errMap);
    }
    return bean;
  }
}
