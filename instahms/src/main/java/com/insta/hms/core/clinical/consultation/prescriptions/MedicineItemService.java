package com.insta.hms.core.clinical.consultation.prescriptions;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.prescriptionfavourites.PrescriptionFavouritesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class MedicineItemService extends PrescriptionItem {

  @LazyAutowired
  private PrescriptionFavouritesService prescriptionFavouritesService;
  @LazyAutowired
  private ClinicalPreferencesService clinicalPreferencesService;

  public MedicineItemService(MedicinePrescriptionsRepository repo) {
    super("op_medicine_pres_id", repo);
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
    @SuppressWarnings("unchecked")
    Map<String, Object> clinicalPreferences = clinicalPreferencesService.getClinicalPreferences()
        .getMap();
    if ("Y".equals(prescription.get("apply_prescription_validity"))
        && "Y".equals(clinicalPreferences.get("op_prescription_validity"))) {
      bean.set("expiry_date", new Timestamp(new Date().getTime()
          + (Integer) clinicalPreferences.get("op_prescription_validity_period") * 60 * 60 * 1000));
    } else if ("N".equals(prescription.get("apply_prescription_validity"))) {
      bean.set("expiry_date", null);
    }
    Integer pbmPresId = (Integer) prescription.get("pbm_id");
    if (pbmPresId != 0) {
      bean.set("pbm_presc_id", pbmPresId);
    }
    if (operation.equals("insert")) {
      if (!(Boolean) prescription.get("generics")) {
        bean.set("medicine_id", Integer.parseInt((String) prescription.get("item_id")));
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
      prescriptionFavouritesService.insertPresMedicineFavourite(bean,
          (String) prescription.get("doctor_id"), (String) prescription.get("special_instr"),
          errMap);
    }
    return bean;
  }

  public List<BasicDynaBean> getMedicinesForPrescription(Boolean generics, String orgId,
      String patientype, Integer insPlanId, Boolean nonHosptl, String healthAuthority,
      Integer centerId, String searchQuery, Integer limit) {
    return ((MedicinePrescriptionsRepository) repo).getMedicinesForPrescription(generics, orgId,
        patientype, insPlanId, nonHosptl, healthAuthority, centerId, searchQuery, limit);
  }

  public Integer getPrescriptions(Object consId) {
    return ((MedicinePrescriptionsRepository) repo).getPrescriptions(consId);
  }

  public BasicDynaBean getLatestPBMPresId(Object consId) {
    return ((MedicinePrescriptionsRepository) repo).getLatestPBMPresId(consId);
  }

  public boolean attachPbmToERX(Integer consId, Integer pbmPrescId) {
    return ((MedicinePrescriptionsRepository) repo).attachPbmToERX(consId, pbmPrescId);
  }

  public List<BasicDynaBean> listAll(Map<String, Object> filterMap) {
    return repo.listAll(null, filterMap, null);
  }

  public boolean deAttachPbmFromERX(Object consId) {
    return ((MedicinePrescriptionsRepository) repo).deAttachPbmFromERX(consId);
  }
  
  // In case of IP it is visitId and Op it is consultationId
  public boolean attachPbmToERxDischargeMedication(Object visitId, Integer pbmPrescId) {
    return ((MedicinePrescriptionsRepository) repo).attachPbmToERX(visitId, pbmPrescId, true);
  }

}
