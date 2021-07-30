package com.insta.hms.core.medicalrecords;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class MRDCaseFileIndentService.
 */
@Service
public class MRDCaseFileIndentService {

  /** The mrd case file attr repo. */
  @LazyAutowired
  private MRDCaseFileIndentRepository mrdCaseFileAttrRepo;

  /** The Constant MRD_CASE_FILE_STATUS_ON_DISCHARGE. */
  public static final String MRD_CASE_FILE_STATUS_ON_DISCHARGE = "P"; // Pending

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return mrdCaseFileAttrRepo.getBean();
  }

  /**
   * Exist.
   *
   * @param keycolumn the keycolumn
   * @param identifier the identifier
   * @return the boolean
   */
  public Boolean exist(String keycolumn, String identifier) {
    return mrdCaseFileAttrRepo.exist(keycolumn, identifier);
  }

  /**
   * Insert.
   *
   * @param mrdfileBean the mrdfile bean
   * @return the int
   */
  public int insert(BasicDynaBean mrdfileBean) {
    return mrdCaseFileAttrRepo.insert(mrdfileBean);
  }

  /**
   * Sets the MRD case file status.
   *
   * @param mrno the mrno
   * @param status the status
   * @return true, if successful
   */
  public boolean setMRDCaseFileStatus(String mrno, String status) {
    Boolean exists = exist("mr_no", mrno);
    if (exists) {
      BasicDynaBean mrdBean = getBean();
      mrdBean.set("case_status", status);
      Map<String, Object> keys = new HashMap<>();
      keys.put("mr_no", mrno);

      return update(mrdBean, keys) > 0;
    }
    return true;
  }

  /**
   * Update.
   *
   * @param bean the bean
   * @param keys the keys
   * @return the int
   */
  public int update(BasicDynaBean bean, Map<String, Object> keys) {
    return mrdCaseFileAttrRepo.update(bean, keys);
  }

}
