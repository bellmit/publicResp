package com.insta.hms.core.patient.registration;

import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.validation.ActionRightsACLRule;
import com.insta.hms.exception.AccessDeniedException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Class RegistrationACL.
 */
@Component
public class RegistrationACL {

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The action rights ACL rule. */
  @LazyAutowired
  private ActionRightsACLRule actionRightsACLRule;

  /** The custom fields service. */
  @LazyAutowired
  private RegistrationCustomFieldsService customFieldsService;

  /**
   * Authenticate patient demography.
   *
   * @param newBean
   *          the new bean
   * @param existingBean
   *          the existing bean
   */
  @SuppressWarnings("unchecked")
  public void authenticatePatientDemography(BasicDynaBean newBean, BasicDynaBean existingBean) {
    Map<String, String> urlRightsMap = (Map<String, String>) securityService
        .getSecurityAttributes().get("urlRightsMap");
    if (!"A".equals(urlRightsMap.get("reg_general"))) {
      throw new AccessDeniedException("exception.access.denied.update",
          new String[] { "basic and additional info" });
    }

    checkEditFirstName(newBean, existingBean);

    checkEditCustomfields(newBean, existingBean);
  }

  /**
   * Check edit first name.
   *
   * @param newBean
   *          the new bean
   * @param existingBean
   *          the existing bean
   */
  public void checkEditFirstName(BasicDynaBean newBean, BasicDynaBean existingBean) {
    List<String> violations = new ArrayList<String>();
    List<String> fields = Arrays.asList("patient_name", "dateofbirth", "patient_gender");
    if (!actionRightsACLRule.apply(newBean, 
        existingBean, fields, "edit_first_name", violations)) {
      throw new AccessDeniedException("exception.access.denied.update.fields",
          new String[] { StringUtil
          .join(violations.toArray(new String[violations.size()]), ",") });
    }
  }

  /**
   * Check edit customfields.
   *
   * @param newBean
   *          the new bean
   * @param existingBean
   *          the existing bean
   */
  public void checkEditCustomfields(BasicDynaBean newBean, BasicDynaBean existingBean) {
    // patient custom fields edit.
    List<BasicDynaBean> customFieldsBean = customFieldsService.listAllCustomFields("P");
    List<String> fields = new ArrayList<String>();
    List<String> violations = new ArrayList<String>();

    for (BasicDynaBean bean : customFieldsBean) {
      fields.add((String) bean.get("name"));
    }

    if (!actionRightsACLRule.apply(newBean, 
        existingBean, fields, "edit_custom_fields", violations)) {
      throw new AccessDeniedException("exception.access.denied.update.fields",
          new String[] { StringUtil
          .join(violations.toArray(new String[violations.size()]), ",") });
    }
  }

  /**
   * Authenticate visit info.
   *
   * @param newBean
   *          the new bean
   * @param existingBean
   *          the existing bean
   */
  @SuppressWarnings("unchecked")
  public void authenticateVisitInfo(BasicDynaBean newBean, BasicDynaBean existingBean) {
    Map<String, String> urlRightsMap = (Map<String, String>) securityService
        .getSecurityAttributes().get("urlRightsMap");
    if (!securityService.isAdministrator() 
        && !"A".equals(urlRightsMap.get("edit_visit_details"))) {
      throw new AccessDeniedException("exception.access.denied.update",
          new String[] { "visit info" });
    }
    checkEditRegDate(newBean, existingBean);
  }

  /**
   * Check allow new reg.
   */
  @SuppressWarnings("unchecked")
  public void checkAllowNewReg() {
    Map<String, String> actionRightsMap = (Map<String, String>) securityService
        .getSecurityAttributes().get("actionRightsMap");
    if ("N".equals(actionRightsMap.get("allow_new_registration"))) {
      throw new AccessDeniedException("exception.access.denied");
    }
  }

  /**
   * Check edit reg date.
   *
   * @param newBean
   *          the new bean
   * @param existingBean
   *          the existing bean
   */
  public void checkEditRegDate(BasicDynaBean newBean, BasicDynaBean existingBean) {
    List<String> violations = new ArrayList<String>();
    List<String> fields = Arrays.asList("reg_date");
    if (!actionRightsACLRule.apply(newBean, existingBean, fields, "allow_backdate", violations)) {
      throw new AccessDeniedException("exception.access.denied.update.fields",
          new String[] { StringUtil
          .join(violations.toArray(new String[violations.size()]), ",") });
    }
  }

  /**
   * Check edit category.
   *
   * @param newBean
   *          the new bean
   * @param existingBean
   *          the existing bean
   */
  public void checkEditCategory(BasicDynaBean newBean, BasicDynaBean existingBean) {
    List<String> violations = new ArrayList<String>();
    List<String> fields = Arrays.asList("patient_category_id");
    if (!actionRightsACLRule.apply(newBean, existingBean, fields, "patient_category_change",
        violations)) {
      throw new AccessDeniedException("exception.access.denied.update.fields",
          new String[] { StringUtil.join(violations
              .toArray(new String[violations.size()]), ",") });
    }
  }

  /**
   * Authenticate patient demography new visit.
   *
   * @param newBean
   *          the new bean
   * @param existingBean
   *          the existing bean
   */
  @SuppressWarnings("unchecked")
  public void authenticatePatientDemographyNewVisit(BasicDynaBean newBean,
      BasicDynaBean existingBean) {
    Map<String, String> urlRightsMap = (Map<String, String>) securityService
        .getSecurityAttributes().get("urlRightsMap");
    if (!"A".equals(urlRightsMap.get("new_op_registration"))) {
      throw new AccessDeniedException("exception.access.denied.update",
          new String[] { "basic and additional info" });
    }

    if (existingBean != null) {
      checkEditFirstName(newBean, existingBean);
      checkEditCustomfields(newBean, existingBean);
      checkEditCategory(newBean, existingBean);
    } else {
      checkAllowNewReg();
    }
  }

}
