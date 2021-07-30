package com.insta.hms.core.clinical.vital;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.vitalforms.VitalReadingService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.mdm.vitalparameters.VitalParameterService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class GenericVitalFormService.
 *
 * @author sonam
 */
@Service
public class GenericVitalFormService {

  /** The vital reading service. */
  @LazyAutowired
  private VitalReadingService vitalReadingService;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The vital parameter service. */
  @LazyAutowired
  private VitalParameterService vitalParameterService;

  /**
   * Group by reading id.
   *
   * @param patientId the patient id
   * @param paramContainer the param container
   * @return the list
   */
  @SuppressWarnings({"unchecked", "rawtypes", "unused"})
  public List groupByReadingId(String patientId, String paramContainer) {
    // multiple rows(single row for each parameter) per single single reading id
    String visit = null;
    List<BasicDynaBean> paramWiseReadingList =
        vitalReadingService.getVitalReadings(patientId, paramContainer);
    BasicDynaBean patBean = registrationService.findByKey(patientId);
    if (patBean != null) {
      visit = ((String) patBean.get("visit_type")).toUpperCase();
    }
    List<BasicDynaBean> paramList = vitalParameterService.getAllParams(paramContainer, visit);
    ArrayList readingWiseList = new ArrayList();
    ArrayList records = new ArrayList();
    int readingId = 0;
    Map labelmap = null;
    for (BasicDynaBean bean : paramWiseReadingList) {
      if (readingId != (Integer) bean.get("vital_reading_id")) {
        labelmap = new LinkedHashMap();
        for (BasicDynaBean param : paramList) {
          labelmap.put(param.get("param_label"), "");
        }
        readingWiseList.add(labelmap);
      }
      labelmap.put(bean.get("param_label"), bean.get("param_value"));
      labelmap.putAll(bean.getMap());
      readingId = (Integer) bean.get("vital_reading_id");
    }
    return readingWiseList;
  }

}
