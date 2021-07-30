package com.insta.hms.common.taxation;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.core.inventory.sales.SalesService;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseTaxCalculator.
 *
 * @author irshadmohammed
 */
public abstract class BaseTaxCalculator implements TaxCalculator {
  
  static Logger logger = LoggerFactory.getLogger(BaseTaxCalculator.class);

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.common.taxation.TaxCalculator#calculateTaxes(com.insta.hms.
   * common.taxation.ItemTaxDetails, com.insta.hms.common.taxation.TaxContext)
   */
  @Override
  public Map<Integer, Object> calculateTaxes(ItemTaxDetails taxBean, TaxContext taxContext) {
    Map<Integer, Object> taxMap = new HashMap<Integer, Object>();
    int subGroupId = taxBean.getSugbroupId();
    Map<String, Object> subGroupMap = new HashMap<String, Object>();
    subGroupMap.put("item_subgroup_id", subGroupId);

    BasicDynaBean taxParameter = getTaxParameters(subGroupMap, taxContext);

    try { 
      if (isTaxApplicable(taxParameter, taxContext)) {
  
        BigDecimal taxAmount = BigDecimal.ZERO;
        Map<String, String> taxDetailsMap = new HashMap<String, String>();
  
        if (null != taxParameter) {
  
          BigDecimal taxRate = getTaxRate(taxParameter, taxContext);
          taxAmount = applyTaxRate(taxBean, taxRate, taxContext);
          taxDetailsMap.put("rate", String.valueOf(taxRate));
          taxDetailsMap.put("amount", String.valueOf(taxAmount));
          taxDetailsMap.put("tax_sub_group_id", String.valueOf(subGroupId));
          taxMap.put(subGroupId, taxDetailsMap);
        }
      }
    } catch (Exception exp) {
      logger.error("Error in calculateTaxes " + exp.getMessage());
    }
    return taxMap;
  }

  /**
   * Gets the tax parameters.
   *
   * @param subGroupMap
   *          the sub group map
   * @param taxContext
   *          the tax context
   * @return the tax parameters
   */
  protected abstract BasicDynaBean getTaxParameters(Map<String, Object> subGroupMap,
      TaxContext taxContext);

  /**
   * Apply tax rate.
   *
   * @param itemTaxDetails
   *          the item tax details
   * @param taxRate
   *          the tax rate
   * @return the big decimal
   */
  public BigDecimal applyTaxRate(ItemTaxDetails itemTaxDetails, BigDecimal taxRate) {
    return applyTaxRate(itemTaxDetails, taxRate, true);
  }

  /**
   * Implement formula to get tax amount from tax rate.
   *
   * @param itemTaxDetails
   *          the item tax details
   * @param taxRate
   *          the tax rate
   * @param useDiscountedPrice
   *          the use discounted price
   * @return the big decimal
   */

  protected BigDecimal applyTaxRate(ItemTaxDetails itemTaxDetails, BigDecimal taxRate,
      boolean useDiscountedPrice) {

    BigDecimal taxAmount = BigDecimal.ZERO;
    if (null == taxRate) {
      taxRate = BigDecimal.ZERO;
    }
    BigDecimal taxRatePerc = (taxRate.divide(BigDecimal.valueOf(100)));

    // tax amount = ((price * qty) - discount) * tax percent
    BigDecimal discAmount = (useDiscountedPrice) ? getDiscount(itemTaxDetails) : BigDecimal.ZERO;
    taxAmount = ConversionUtils.setScale(
        ((getTaxBasePrice(itemTaxDetails).multiply(getNetQty(itemTaxDetails))).subtract(discAmount))
            .multiply(taxRatePerc),
        true);

    setNetAmount(itemTaxDetails, taxAmount);
    setDiscountAmount(itemTaxDetails);
    setAdjustedBasePrice(itemTaxDetails);
    return taxAmount;
  }

  /**
   * Apply tax rate.
   *
   * @param itemTaxDetails
   *          the item tax details
   * @param taxRate
   *          the tax rate
   * @param taxContext
   *          the tax context
   * @return the big decimal
   */
  protected BigDecimal applyTaxRate(ItemTaxDetails itemTaxDetails, BigDecimal taxRate,
      TaxContext taxContext) {
    return applyTaxRate(itemTaxDetails, taxRate);
  }

  /**
   * Sets the adjusted base price.
   *
   * @param itemTaxDetails
   *          the new adjusted base price
   */
  protected void setAdjustedBasePrice(ItemTaxDetails itemTaxDetails) {
    if (null != itemTaxDetails) {
      if (null != itemTaxDetails.getAdjMrp()) {
        itemTaxDetails.setAdjPrice(itemTaxDetails.getAdjMrp());
      } else {
        itemTaxDetails.setAdjPrice(getBasePrice(itemTaxDetails));
      }
    }
  }

  /**
   * Sets the discount amount.
   *
   * @param itemTaxDetails
   *          the new discount amount
   */
  protected void setDiscountAmount(ItemTaxDetails itemTaxDetails) {
    if (null == itemTaxDetails || null == itemTaxDetails.getDiscount()
        || BigDecimal.ZERO.compareTo(itemTaxDetails.getDiscount()) == 0) {
      itemTaxDetails.setDiscount(getDiscount(itemTaxDetails));
    }
  }

  /**
   * Sets the net amount.
   *
   * @param itemTaxDetails
   *          the item tax details
   * @param taxAmount
   *          the tax amount
   */
  protected void setNetAmount(ItemTaxDetails itemTaxDetails, BigDecimal taxAmount) {
    if (null == itemTaxDetails.getNetAmount()
        || itemTaxDetails.getNetAmount().compareTo(BigDecimal.ZERO) == 0) {
      BigDecimal netAmount = (null == itemTaxDetails.getMrp()) ? BigDecimal.ZERO
          : itemTaxDetails.getMrp().multiply(getNetQty(itemTaxDetails));
      itemTaxDetails.setNetAmount(netAmount);
    }
  }

  /**
   * Evaluate the expression and get tax amount from tax expression.
   *
   * @param bean
   *          the bean
   * @param taxExpression
   *          the tax expression
   * @return the big decimal
   */
  protected BigDecimal applyTaxExpression(Map<String, Object> bean, String taxExpression) {
    return BigDecimal.valueOf(0.0);
  }

  /**
   * Gets the tax base price.
   *
   * @param itemTaxDetails
   *          the item tax details
   * @return the tax base price
   */
  protected BigDecimal getTaxBasePrice(ItemTaxDetails itemTaxDetails) {
    return getBasePrice(itemTaxDetails);
  }

  /**
   * Gets the base price.
   *
   * @param itemTaxDetails
   *          the item tax details
   * @return the base price
   */
  protected BigDecimal getBasePrice(ItemTaxDetails itemTaxDetails) {

    if (null != itemTaxDetails) {
      String taxBasis = itemTaxDetails.getTaxBasis();
      // We want the adjmrp excluding of total tax not for current tax, because of
      // that we are getting adjmrp from form.
      if (taxBasis != null && !taxBasis.isEmpty()) {
        if ((taxBasis.equalsIgnoreCase("MB")) || (taxBasis.equalsIgnoreCase("M"))) {
          BigDecimal adjMrp = itemTaxDetails.getAdjMrp();
          return (null != adjMrp) ? adjMrp : BigDecimal.ZERO;
        } else if ((taxBasis.equalsIgnoreCase("CB")) || (taxBasis.equalsIgnoreCase("C"))) {
          BigDecimal costPrice = itemTaxDetails.getCostPrice();
          return (null != costPrice) ? costPrice : BigDecimal.ZERO;
        }
      } else {
        BigDecimal amount = itemTaxDetails.getAmount();
        return (null != amount) ? amount : BigDecimal.ZERO;
      }
    }
    return BigDecimal.ZERO;
  }

  /**
   * Gets the net qty.
   *
   * @param itemTaxDetails
   *          the item tax details
   * @return the net qty
   */
  protected BigDecimal getNetQty(ItemTaxDetails itemTaxDetails) {

    BigDecimal netQty = BigDecimal.ONE;
    if (null != itemTaxDetails) {
      netQty = itemTaxDetails.getQty();
      BigDecimal bonusQty = itemTaxDetails.getBonusQty();
      BigDecimal pkgSize = itemTaxDetails.getPkgSize();

      if (null == netQty) {
        netQty = BigDecimal.ONE;
      }
      if (null == pkgSize) {
        pkgSize = BigDecimal.ONE;
      }

      String taxBasis = itemTaxDetails.getTaxBasis();
      if (taxBasis != null && !taxBasis.isEmpty()) {
        if (null == bonusQty) {
          bonusQty = BigDecimal.ZERO;
        }

        if ((taxBasis.equalsIgnoreCase("MB")) || (taxBasis.equalsIgnoreCase("CB"))) {
          netQty = netQty.add(bonusQty);
        }
        // divideHighPrecision(netQty,
        if (null == itemTaxDetails || null == itemTaxDetails.getQtyUom()
            || "I".equalsIgnoreCase(itemTaxDetails.getQtyUom())) {
          netQty = ConversionUtils.divideHighPrecision(netQty, pkgSize);
        }
      }
    }
    return netQty;

  }

  /**
   * Gets the discount.
   *
   * @param itemTaxDetails
   *          the item tax details
   * @return the discount
   */
  protected BigDecimal getDiscount(ItemTaxDetails itemTaxDetails) {

    BigDecimal discount = itemTaxDetails.getDiscount();
    String taxBasis = itemTaxDetails.getTaxBasis();

    if (taxBasis != null && !taxBasis.isEmpty()) {
      // We will add this once we are going to fix GST taxation.
      // if((taxBasis.equalsIgnoreCase("CB")) || (taxBasis.equalsIgnoreCase("C"))) {
      return (null != discount) ? discount : BigDecimal.ZERO;
      // }
    }

    return BigDecimal.ZERO;
  }

  /**
   * Checks if is tax applicable.
   *
   * @param taxParameter
   *          the tax parameter
   * @param taxContext
   *          the tax context
   * @return true, if is tax applicable
   */
  protected boolean isTaxApplicable(BasicDynaBean taxParameter, TaxContext taxContext) {
    java.sql.Date currentDate = DateUtil.getCurrentDate();

    if (null == taxParameter) {
      return false; // No tax parameters available, we assume it is not applicable
    }

    Date validityStartDate = (java.sql.Date) taxParameter.get("validity_start");
    Date validityEndDate = (java.sql.Date) taxParameter.get("validity_end");

    if (null == validityStartDate && null == validityEndDate) {
      return true; // No validity, we assume valid for ever
    }

    boolean valid = true;
    valid = valid
        && ((validityStartDate == null) || (currentDate.getTime() >= validityStartDate.getTime()));
    valid = valid
        && ((validityEndDate == null) || (currentDate.getTime() <= validityEndDate.getTime()));
    return valid;
  }

  private Map getTaxExpressionParams(TaxContext taxContext) throws SQLException {
    
    Map<String, Object> dataMap = new HashMap<String, Object>();
    dataMap.putAll(taxContext.getCenterBean().getMap());
    dataMap.putAll(taxContext.getPatientBean().getMap());
    dataMap.putAll(taxContext.getVisitBean().getMap());
    return dataMap;
  }
  
  private BigDecimal getTaxRate(BasicDynaBean taxParameter, TaxContext taxContext)
      throws SQLException {
    
    if (taxContext.getTransactionId() == null &&  taxParameter.get("tax_rate_expr") != null 
        && !(((String)taxParameter.get("tax_rate_expr")).isEmpty()) ) {
      
      Map params = getTaxExpressionParams(taxContext);
      return (BigDecimal)processTaxExpr(params, (String)taxParameter.get("tax_rate_expr"));
    }
    
    return taxParameter.get("tax_rate") != null 
              ? (BigDecimal)taxParameter.get("tax_rate") : BigDecimal.ZERO ;
    
  }
  
  private BigDecimal processTaxExpr(Map params, String expression) throws SQLException {
    BigDecimal expressionValue  = null;
   
    StringWriter writer = new StringWriter();
    String template = "<#setting number_format=\"##.##\">\n" + expression;
    try { 
      Template expressionTemplate = 
          new Template("taxtemplate", new StringReader(template), new Configuration());
      expressionTemplate.process(params, writer);
    } catch (TemplateException tempExp) { 
      logger.error("", tempExp);
      return BigDecimal.ZERO;
    } catch (ArithmeticException arithExp) { 
      logger.error("", arithExp);
      return BigDecimal.ZERO;
    } catch (Exception exp) { 
      logger.error("", exp);
      return BigDecimal.ZERO;
    }
    String exprProcessValue = "0";
    
    exprProcessValue = writer.toString().trim();
    if (exprProcessValue != null && !exprProcessValue.isEmpty()) {
      expressionValue = new BigDecimal(exprProcessValue);
    }
    return expressionValue;
  }
}
