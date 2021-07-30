package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.GenericRepository;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.HashMap;
import java.util.Map;

public abstract class PrescriptionItem {

  protected String key;
  protected GenericRepository repo;

  public PrescriptionItem(String key, GenericRepository repo) {
    this.key = key;
    this.repo = repo;
  }

  /**
   * Insert prescription.
   * @param prescription the map
   * @param mbean the bean
   * @param errMap the ValidationErrorMap
   * @return the basic dyna bean
   */
  public BasicDynaBean insert(Map<String, Object> prescription, BasicDynaBean mbean,
      ValidationErrorMap errMap) {
    BasicDynaBean bean = copyToBean(prescription, mbean, repo.getBean(), "insert");
    bean.set(key, mbean.get("patient_presc_id"));
    if ((repo.insert(bean)) == 1) {
      repo.insertAuditLog(bean);
      return bean;
    }
    return null;
  }

  /**
   * Update presc item.
   * @param prescription the map
   * @param mbean the bean
   * @param errMap the ValidationErrorMap
   * @return basic dyna bean
   */
  public BasicDynaBean update(Map<String, Object> prescription, BasicDynaBean mbean,
      ValidationErrorMap errMap) {
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put(key, prescription.get("item_prescribed_id"));
    BasicDynaBean bean = copyToBean(prescription, mbean, repo.getBean(), "update");
    if ((repo.update(bean, keys)) == 1) {
      repo.updateAuditLog(bean, keys);
      return bean;
    }
    return null;
  }

  public boolean delete(Integer presId) {
    repo.deleteAuditLog(key, presId);
    return (repo.delete(key, presId)) == 1;
  }

  public abstract BasicDynaBean copyToBean(Map<String, Object> prescription, BasicDynaBean mbean,
      BasicDynaBean bean, String operation);

}
