package com.insta.hms.common.preferences.clinicalpreferences;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.RegexValidationRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ClinicalPreferencesValidator extends MasterValidator {

  private NotNullRule notNullRule = new NotNullRule();

  private RegexValidationRule prescriptionFormat = new RegexValidationRule("^[AB]$",
      "exception.clinicalpreferences.prescriptionFormat");

  private RegexValidationRule booleanString = new RegexValidationRule("^[NY]$",
      "exception.clinicalpreferences.booleanstring");

  private RegexValidationRule nonNegativeNumber = new RegexValidationRule("^[1-9][0-9]{0,2}$",
      "exception.clinicalpreferences.hours");
  
  private RegexValidationRule consultationValidityUnits = new RegexValidationRule("^[DT]$",
      "exception.clinicalpreferences.consultationValidityUnits");
  
  private RegexValidationRule vitalPeriodUnits = new RegexValidationRule("^[DM]$",
      "exception.clinicalpreferences.consultationValidityUnits");

  /**
   * Validate clinical preferences.
   *
   * @param bean
   *          the bean
   * @return true, if successful
   */
  public boolean validateClinicalPreferences(BasicDynaBean bean) {
    Map<ValidationRule, String[]> ruleSet = new HashMap<>();
    List<String> notNullFields = new ArrayList<>();

    if ("Y".equals(bean.get("op_prescription_validity"))) {
      notNullFields.add("op_prescription_validity_period");
    }
    if ("Y".equals(bean.get("op_consultation_auto_closure"))) {
      notNullFields.add("op_consultation_auto_closure_period");
    }

    ruleSet.put(notNullRule, notNullFields.toArray(new String[0]));
    ruleSet.put(nonNegativeNumber,
        new String[] { "op_prescription_validity_period", "op_consultation_auto_closure_period" });
    ruleSet.put(prescriptionFormat,
        new String[] { "op_prescription_format", "ip_prescription_format" });
    ruleSet.put(booleanString, new String[] { "allow_ip_prescription_format_override",
        "op_consultation_auto_closure", 
        "op_prescription_validity", 
        "allow_op_prescription_format_override",
        "op_consultation_edit_across_doctors",
        "ip_cases_across_doctors",
        "nurse_staff_ward_assignments_applicable",
        "op_allow_template",
        "op_allow_template_save_with_data",
        "ip_allow_template",
        "ip_allow_template_save_with_data",
        "triage_allow_template",
        "triage_allow_template_save_with_data"
        });
    ruleSet.put(consultationValidityUnits, new String[]{"consultation_validity_units"});
    ruleSet.put(vitalPeriodUnits, new String[]{"historic_vitals_period_unit"});
    if (!applyRuleSet(ruleSet, bean)) {
      throwErrors();
    }
    return true;
  }

}
