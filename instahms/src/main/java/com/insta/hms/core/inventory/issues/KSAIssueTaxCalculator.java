package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxContext;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class KSAIssueTaxCalculator extends IssueTaxCalculator {

  private static final String[] SUPPORTED_GROUPS = new String[] { "KSACEX" };

  public KSAIssueTaxCalculator() {
    super(SUPPORTED_GROUPS);
  }

  @Override
  protected BigDecimal getBasePrice(ItemTaxDetails itemTaxDetails) {
    if (null != itemTaxDetails) {
      return itemTaxDetails.getMrp();
    }
    return BigDecimal.ZERO;
  }

  @Override
  protected void setNetAmount(ItemTaxDetails itemTaxDetails, BigDecimal taxAmount) {
    // Let the base net amount be set by the super class
    super.setNetAmount(itemTaxDetails, taxAmount);
    // Add tax amount to it
    itemTaxDetails.setNetAmount(itemTaxDetails.getNetAmount().add(taxAmount));
  }

  @Override
  protected BigDecimal getDiscountBasePrice(ItemTaxDetails itemTaxDetails) {
    return (null != itemTaxDetails) ? itemTaxDetails.getMrp() : BigDecimal.ZERO;
  }

  protected BigDecimal applyTaxRate(ItemTaxDetails itemTaxDetails, BigDecimal taxRate,
      TaxContext taxContext) {
    BigDecimal taxAmt = super.applyTaxRate(itemTaxDetails, taxRate, true);
    BasicDynaBean patientBean = taxContext.getPatientBean();
    BasicDynaBean billBean = taxContext.getBillBean();
    BasicDynaBean centerBean = taxContext.getCenterBean();
    BasicDynaBean tpaBean = taxContext.getTpaBean();

    setOriginalTaxAmount(itemTaxDetails, taxAmt);
    // KSA TPA - when insurance tax is not payable
    if (null != billBean && null != billBean.getMap() && !billBean.getMap().isEmpty()
        && (boolean) billBean.get("is_tpa") && null != tpaBean && null != tpaBean.getMap()
        && !tpaBean.getMap().isEmpty()
        && (boolean) tpaBean.get("claim_amount_includes_tax").equals("N")) {
      // Kludge : We will subtract the net amount since the net tax amount in this case is 0, but
      // the call to the super
      // method added the tax amount anyway.
      itemTaxDetails.setNetAmount(itemTaxDetails.getNetAmount().subtract(taxAmt));
      taxAmt = BigDecimal.ZERO;
      // KSA cash patient
    } else if (null != billBean && !billBean.getMap().isEmpty() && !(boolean) billBean.get("is_tpa")
        && null != patientBean && !patientBean.getMap().isEmpty()
        && null != patientBean.get("nationality_id")
        && patientBean.get("nationality_id").equals(centerBean.get("country_id"))) {
      // Kludge : We will subtract the net amount since the net tax amount in this case is 0, but
      // the call to the super
      // method added the tax amount anyway.
      itemTaxDetails.setNetAmount(itemTaxDetails.getNetAmount().subtract(taxAmt));
      taxAmt = BigDecimal.ZERO;
    }
    return taxAmt;
  }

  private void setOriginalTaxAmount(ItemTaxDetails itemTaxDetails, BigDecimal taxAmt) {
    if (null != itemTaxDetails && null != taxAmt) {
      BigDecimal originalTax = itemTaxDetails.getOriginalTax();
      BigDecimal totalOriginalTax = (null != originalTax) ? originalTax.add(taxAmt) : taxAmt;
      itemTaxDetails.setOriginalTax(totalOriginalTax);
    }
  }
}
