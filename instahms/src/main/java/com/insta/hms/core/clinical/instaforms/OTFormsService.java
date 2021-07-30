package com.insta.hms.core.clinical.instaforms;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.operationdetails.OperationDetailsService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class OTFormsService.
 *
 * @author anup vishwas
 */

@Service
public class OTFormsService extends InstaFormsService {

  /** The operation details service. */
  @LazyAutowired
  private OperationDetailsService operationDetailsService;

  /** The patient section details service. */
  @LazyAutowired
  PatientSectionDetailsService patientSectionDetailsService;

  /** The form components service. */
  @LazyAutowired
  private FormComponentsService formComponentsService;

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.core.clinical.instaforms.InstaFormsService#getKeys()
   */
  @Override
  public Map<String, String> getKeys() {
    Map<String, String> map = new HashMap<>();
    map.put("form_type", "Form_OT");
    map.put("item_type", "SUR");
    map.put("section_item_id", "operation_proc_id");
    return map;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.core.clinical.instaforms.InstaFormsService#getComponents(java.util.Map)
   */
  @Override
  public BasicDynaBean getComponents(Map params) {
    int operationProcId = Integer.parseInt(ConversionUtils.getParamValue(params,
        "operation_proc_id", "0"));
    BasicDynaBean otBean = operationDetailsService.getOpDetailsByProcId(operationProcId);
    List<BasicDynaBean> formList = patientSectionDetailsService.getSectionFormDetails(
        operationProcId, "Form_OT");
    String operationId = (String) otBean.get("operation_id");
    if (formList == null || formList.isEmpty()) {
      String deptId = (String) otBean.get("dept_id");
      formList = formComponentsService.formComponentDetails(deptId, "Form_OT", operationId);
      if (formList == null || formList.isEmpty()) {
        formList = formComponentsService.formComponentDetails(deptId, "Form_OT", "-1");
        if (formList == null || formList.isEmpty()) {
          formList = formComponentsService.formComponentDetails("-1", "Form_OT", "-1");
        }
      }
    }
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("sections");
    builder.add("form_id", Integer.class);
    builder.add("form_name");

    BasicDynaBean bean = builder.build();
    String sections = "";
    boolean first = true;
    int formId = 0;
    String formName = "";
    for (BasicDynaBean formBean : formList) {
      if (!first) {
        sections += ",";
      }
      sections += (Integer) formBean.get("section_id");
      first = false;
      formId = (Integer) formBean.get("form_id");
      formName = (String) formBean.get("form_name");
    }
    bean.set("sections", sections);
    bean.set("form_id", formId);
    bean.set("form_name", formName);

    return bean;
  }

}
