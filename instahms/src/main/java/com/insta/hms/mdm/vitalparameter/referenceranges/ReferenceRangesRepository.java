package com.insta.hms.mdm.vitalparameter.referenceranges;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.vitalparameters.VitalParameterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ReferenceRangesRepository.
 *
 * @author sonam
 */
@Repository
public class ReferenceRangesRepository extends MasterRepository<Integer> {
  @LazyAutowired
  VitalParameterRepository vitalRepository;

  public ReferenceRangesRepository() {
    super("vital_reference_range_master", "range_id");
  }

  /**
   * Update reference range.
   *
   * @param params
   *          the params
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  public boolean updateReferenceRange(Map params) {
    Map<String, Object> keys = new HashMap<String, Object>();
    boolean success = false;
    boolean status = true;
    String paramId = ((String[]) params.get("param_id"))[0];
    List<BasicDynaBean> insertAllRanges = new ArrayList<BasicDynaBean>();
    String[] rangeId = (String[]) params.get("range_id");
    if (paramId != null && rangeId != null) {
      Integer paramIdInt = Integer.parseInt(paramId);
      for (int i = 0; i < rangeId.length; i++) {
        if (rangeId[i] != null && !rangeId[i].equals("")) {
          if (findByKey("range_id", Integer.parseInt(rangeId[i])) != null) {
            BasicDynaBean updateRangesBean = getBean();
            ConversionUtils.copyIndexToDynaBean(params, i, updateRangesBean);
            keys.put("range_id", (Integer) updateRangesBean.get("range_id"));
            updateRangesBean.set("param_id", paramIdInt);
            update(updateRangesBean, keys);
          }
        } else {
          BasicDynaBean insertRangesBean = getBean();
          ConversionUtils.copyIndexToDynaBean(params, i, insertRangesBean);
          int range = getNextSequence();
          insertRangesBean.set("range_id", range);
          insertRangesBean.set("param_id", paramIdInt);
          insertAllRanges.add(insertRangesBean);
          success = true;
        }
      }
      if (success) {
        batchInsert(insertAllRanges);
      }
    }
    return status;

  }

  private static final String GET_EXACT_REFERENCE_RANGE = " SELECT range_id,"
      + " patient_gender, min_patient_age, max_patient_age, range_for_all,"
      + " age_unit, max_improbable_value,"
      + " max_critical_value, max_normal_value, min_improbable_value, min_critical_value, "
      + " min_normal_value, priority, param_id, reference_range_txt"
      + " FROM vital_reference_range_master "
      + " WHERE ( range_for_all = 'N'  AND "
      + " ( (min_patient_age IS NULL OR "
      + "     min_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end) <= "
      + "   ( SELECT (current_date - COALESCE(dateofbirth, expected_dob))::integer "
      + "     FROM patient_details WHERE mr_no = ? ) )"
      + "  AND"
      + "   (max_patient_age IS NULL OR ( "
      + "         SELECT (current_date - COALESCE(dateofbirth, expected_dob))::integer "
      + "     FROM patient_details WHERE mr_no = ? ) <= "
      + "     max_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end) )) "
      + " AND (patient_gender = ? OR patient_gender = 'N')) ORDER BY priority ";

  public List<BasicDynaBean> getExactReferenceRange(String mrNo, String gender) {

    return DatabaseHelper.queryToDynaList(GET_EXACT_REFERENCE_RANGE, mrNo, mrNo, gender);
  }

}
