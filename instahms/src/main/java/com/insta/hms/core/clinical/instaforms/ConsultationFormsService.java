package com.insta.hms.core.clinical.instaforms;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ConsultationFormsService.
 *
 * @author anup vishwas
 */

@Service
public class ConsultationFormsService extends InstaFormsService {

  /** The doctor consultation service. */
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;

  /** The patient section details service. */
  @LazyAutowired
  PatientSectionDetailsService patientSectionDetailsService;

  /** The form components service. */
  @LazyAutowired
  private FormComponentsService formComponentsService;

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.core.clinical.instaforms.InstaFormsService#getComponents(java.util.Map)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public BasicDynaBean getComponents(Map params) {

    int consultationId = Integer.parseInt(ConversionUtils.getParamValue(params, "consultation_id",
        "0"));
    List<BasicDynaBean> list = patientSectionDetailsService.getSectionFormDetails(consultationId,
        "Form_CONS");
    if (list == null || list.isEmpty()) {
      BasicDynaBean consbean = doctorConsultationService.getDoctorConsultDetails(consultationId);
      if (consbean != null) {
        String deptId = (String) consbean.get("dept_id");
        list = formComponentsService.formComponentDetails(deptId, "Form_CONS", null);
      }
      if (list == null || list.isEmpty()) {
        list = formComponentsService.formComponentDetails("-1", "Form_CONS", null);
      }
    }
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("sections");
    builder.add("form_id", Integer.class);
    builder.add("form_name");
    BasicDynaBean bean = builder.build();

    String sections = "";
    int formId = 0;
    boolean first = true;
    String formName = "";

    for (BasicDynaBean b : list) {
      if (!first) {
        sections += ",";
      }
      sections += (Integer) b.get("section_id");
      formId = (Integer) b.get("form_id");
      formName = (String) b.get("form_name");

      first = false;
    }
    bean.set("sections", sections);
    bean.set("form_id", formId);
    bean.set("form_name", formName);

    return bean;

  }

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.core.clinical.instaforms.InstaFormsService#getKeys()
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Map getKeys() {
    Map map = new HashMap();
    map.put("form_type", "Form_CONS");
    map.put("item_type", "CONS");
    map.put("section_item_id", "consultation_id");
    return map;
  }

}
