package com.insta.hms.mdm.integration.item;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.validation.MaximumLengthRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.integration.IntegrationValidator;
import com.insta.hms.mdm.packageuom.PackageUomService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class StoreItemDetailsIntegrationValidator extends IntegrationValidator {

  @LazyAutowired
  PackageUomService packageUomService;
  
  @LazyAutowired
  private MessageUtil message;

  private static final String[] MANDATORY_FIELDS = new String[] { "cust_item_code", "medicine_name",
      "medicine_short_name", "med_category_id", "manf_name", "status", "package_uom", "issue_units",
      "batch_no_applicable", "control_type_id", "value", "high_cost_consumable",
      "allow_zero_claim_amount", "prior_auth_required", "consumption_capacity",
      "tax_type" };

  private static final ValidationRule maximumlengthrule1 = new MaximumLengthRule(1);
  private static final String[] maximumlengthfields1 = new String[] { "status", "value",
      "prior_auth_required", "high_cost_consumable", "allow_zero_claim_amount" };
  private static final ValidationRule maximumlengthrule2 = new MaximumLengthRule(20);
  private static final String[] maximumlengthfields2 = new String[] { "cust_item_code" };
  private static final ValidationRule maximumlengthrule3 = new MaximumLengthRule(30);
  private static final String[] maximumlengthfields3 = new String[] { "generic_name" };
  private static final ValidationRule maximumlengthrule4 = new MaximumLengthRule(50);
  private static final String[] maximumlengthfields4 = new String[] { "package_type",
      "item_strength", "bin" };
  private static final ValidationRule maximumlengthrule5 = new MaximumLengthRule(100);
  private static final String[] maximumlengthfields5 = new String[] { "manf_name", "issue_units",
      "supplier_name", "invoice_details", "package_uom", };
  private static final ValidationRule maximumlengthrule6 = new MaximumLengthRule(200);
  private static final String[] maximumlengthfields6 = new String[] { "medicine_name",
      "medicine_short_name" };

  


  private static final String MAPPING_IS_WORNG = "msg.item.mapping.wrong";

  /**
   * Instantiates a new store item details integration validator.
   *
   * @param repository
   *          the repository
   */
  public StoreItemDetailsIntegrationValidator(StoreItemDetailsIntegrationRepository repository) {
    super(repository);
    ItemBarcodeRequiredRule itemBarcodeRequiredRule = new ItemBarcodeRequiredRule();
    PackageUomRule packageUomRule = new PackageUomRule();
    addInsertRule(packageUomRule, null);
    addUpdateRule(packageUomRule, null);
    addInsertRule(itemBarcodeRequiredRule, null);
    addUpdateRule(itemBarcodeRequiredRule, null);
    addInsertRule(maximumlengthrule1, maximumlengthfields1);
    addUpdateRule(maximumlengthrule1, maximumlengthfields1);
    addInsertRule(maximumlengthrule2, maximumlengthfields2);
    addUpdateRule(maximumlengthrule2, maximumlengthfields2);
    addInsertRule(maximumlengthrule3, maximumlengthfields3);
    addInsertRule(maximumlengthrule4, maximumlengthfields4);
    addUpdateRule(maximumlengthrule4, maximumlengthfields4);
    addInsertRule(maximumlengthrule5, maximumlengthfields5);
    addUpdateRule(maximumlengthrule5, maximumlengthfields5);
    addInsertRule(maximumlengthrule6, maximumlengthfields6);
    addUpdateRule(maximumlengthrule6, maximumlengthfields6);
  }

  @Override
  protected String[] getMandatoryFields() {
    return MANDATORY_FIELDS;
  }

  @Override
  protected String[] getNonUpdatableFields() {
    return null;
  }

  private class PackageUomRule extends ValidationRule {

    String messageKey = "exception.csv.invalid.uom.combination";

    @Override
    public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
      String issueUnits = (String) bean.get("issue_units");
      String packageUom = (String) bean.get("package_uom");
      BigDecimal packageSize = packageUomService.getPackageSize(issueUnits, packageUom);
      if (packageSize == null) {
        errorMap.addError("issue_units", messageKey,
            Arrays.asList(new String[] { issueUnits, packageUom }));
        return false;
      }
      return true;
    }

  }

  private class ItemBarcodeRequiredRule extends ValidationRule {

    String messageKey = "exception.notnull.value";

    @Override
    public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
      BasicDynaBean genericPreferences = ApplicationContextProvider
          .getBean(GenericPreferencesService.class).getAllPreferences();
      if (genericPreferences.get("barcode_for_item").equals("Y")
          && StringUtils.isEmpty((String) bean.get("item_barcode_id"))) {
        errorMap.addError("item_barcode_id", this.messageKey,
            Arrays.asList(StringUtil.prettyName("item_barcode_id")));
        return false;
      }
      return true;
    }

  }
  
  // private class InsuranceCategoryRule extends ValidationRule {
  //
  // @Override
  // public boolean apply(BasicDynaBean bean, String[] fields, ValidationErrorMap errorMap) {
  // // TODO Auto-generated method stub
  // return false;
  // }
  //
  // }
  
  /**
   * map valid value for insert item.
   * @param bean the bean
   * @param info the info
   */
  public void mappedValidValueForInsertItem(BasicDynaBean bean, List<String> info) {
    Set<String> valueData = new HashSet<>();
    valueData.add("H");
    valueData.add("M");
    valueData.add("L");

    String value = (String) bean.get("value");
    if (null != value && !"".equals(value) && valueData.contains(value)) {
      bean.set("value", value);
    } else {
      bean.set("value", "M");
    }

    Set<String> taxBasis = new HashSet<>();
    taxBasis.add("MB");
    taxBasis.add("M");
    taxBasis.add("CB");
    taxBasis.add("C");

    String taxType = (String) bean.get("tax_type");
    if (null != taxType && !"".equals(taxType)) {
      if (taxBasis.contains(taxType)) {
        bean.set("tax_type", taxType);
      } else {
        bean.set("tax_type", "M");
        info.add(message.getMessage(MAPPING_IS_WORNG, new Object[] { "tax_type", "M" }));
      }
    }

    Set<String> yesNo = new HashSet<>();
    yesNo.add("Y");
    yesNo.add("N");

    String batchNoApplicable = (String) bean.get("batch_no_applicable");
    if (null != batchNoApplicable && !"".equals(batchNoApplicable)
        && yesNo.contains(batchNoApplicable)) {
      bean.set("batch_no_applicable", batchNoApplicable);
    } else {
      bean.set("batch_no_applicable", "Y");
    }

    String highCostConsumable = (String) bean.get("high_cost_consumable");
    if (null != highCostConsumable && !"".equals(highCostConsumable)
        && yesNo.contains(highCostConsumable)) {
      bean.set("high_cost_consumable", highCostConsumable);
    } else {
      bean.set("high_cost_consumable", "N");
    }

    Set<String> allowClaimValues = new HashSet<>();
    allowClaimValues.add("n");
    allowClaimValues.add("i");
    allowClaimValues.add("o");
    allowClaimValues.add("b");
    String allowZeroClaimAmount = (String) bean.get("allow_zero_claim_amount");
    if (null != allowZeroClaimAmount && !"".equals(allowZeroClaimAmount)) {
      if (allowClaimValues.contains(allowZeroClaimAmount)) {
        bean.set("allow_zero_claim_amount", allowZeroClaimAmount);
      } else {
        bean.set("allow_zero_claim_amount", "n");
        info.add(
            message.getMessage(MAPPING_IS_WORNG, new Object[] { "allow_zero_claim_amount", "n" }));
      }
    } else {
      bean.set("allow_zero_claim_amount", "n");
      info.add(
          message.getMessage(MAPPING_IS_WORNG, new Object[] { "allow_zero_claim_amount", "n" }));
    }

  }
  
  /**
   * map valid value for update item.
   * @param bean the bean
   * @param info the info
   */
  public void mappedValidValueForUpdateItem(BasicDynaBean bean, List<String> info) {
    Set<String> valueData = new HashSet<>();
    valueData.add("H");
    valueData.add("M");
    valueData.add("L");

    String value = (String) bean.get("value");
    if (null != value) {
      if (!"".equals(value) && valueData.contains(value)) {
        bean.set("value", value);
      } else {
        bean.set("value", "M");
      }
    }

    Set<String> taxBasis = new HashSet<>();
    taxBasis.add("MB");
    taxBasis.add("M");
    taxBasis.add("CB");
    taxBasis.add("C");

    String taxType = (String) bean.get("tax_type");
    if (null != taxType) {
      if (!"".equals(taxType) && taxBasis.contains(taxType)) {
        bean.set("tax_type", taxType);
      } else {
        bean.set("tax_type", "M");
        info.add(message.getMessage(MAPPING_IS_WORNG, new Object[] { "tax_type", "M" }));
      }
    }

    Set<String> yesNo = new HashSet<>();
    yesNo.add("Y");
    yesNo.add("N");

    String batchNoApplicable = (String) bean.get("batch_no_applicable");
    if (null != batchNoApplicable) {
      if (!"".equals(batchNoApplicable) && yesNo.contains(batchNoApplicable)) {
        bean.set("batch_no_applicable", batchNoApplicable);
      } else {
        bean.set("batch_no_applicable", "Y");
      }
    }

    String highCostConsumable = (String) bean.get("high_cost_consumable");
    if (null != highCostConsumable) {
      if (!"".equals(highCostConsumable) && yesNo.contains(highCostConsumable)) {
        bean.set("high_cost_consumable", highCostConsumable);
      } else {
        bean.set("high_cost_consumable", "N");
      }
    }

    Set<String> allowClaimValues = new HashSet<>();
    allowClaimValues.add("n");
    allowClaimValues.add("i");
    allowClaimValues.add("o");
    allowClaimValues.add("b");
    String allowZeroClaimAmount = (String) bean.get("allow_zero_claim_amount");
    if (null != allowZeroClaimAmount) {
      if (!"".equals(allowZeroClaimAmount) && allowClaimValues.contains(allowZeroClaimAmount)) {
        bean.set("allow_zero_claim_amount", allowZeroClaimAmount);
      } else {
        bean.set("allow_zero_claim_amount", "n");
        info.add(message.getMessage(MAPPING_IS_WORNG, new Object[] { "tax_type", "M" }));
      }
    }
  }

  
}
