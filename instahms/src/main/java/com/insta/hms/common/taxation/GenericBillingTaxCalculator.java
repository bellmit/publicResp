package com.insta.hms.common.taxation;

import com.insta.hms.common.ConversionUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericBillingTaxCalculator.
 */
public abstract class GenericBillingTaxCalculator extends BaseTaxCalculator {
  
  static Logger logger = LoggerFactory.getLogger(GenericBillingTaxCalculator.class);

  /** The supported groups. */
  List<String> supportedGroups = new ArrayList<String>();

  /**
   * Instantiates a new generic billing tax calculator.
   *
   * @param supportedGroups
   *          the supported groups
   */
  protected GenericBillingTaxCalculator(String[] supportedGroups) {
    this.supportedGroups.addAll(Arrays.asList(supportedGroups));
  }

  /**
   * Gets the supported groups.
   *
   * @return the supported groups
   */
  public String[] getSupportedGroups() {
    if (null == supportedGroups || supportedGroups.isEmpty()) {
      return null;
    }
    return supportedGroups.toArray(new String[0]);
  }

  /**
   * This method will calculate the sponsor amount from aggregate of sponsor
   * amount and tax amount itemTaxDetailsDto - property : insClaimAmt - should
   * contain sponsor amount which is inclusive of sponsor tax aggregateTaxPer -
   * sum of all applicable subgroups tax percentages formula to get only sponsor
   * amount
   * insClaimAmt/(1 + (aggregateTaxPer/100) ).
   *
   * @param itemTaxDetailsDto
   *          the item tax details dto
   * @param aggregateTaxPer
   *          the aggregate tax per
   * @return the base claim amount
   */
  protected BigDecimal getBaseClaimAmount(ItemTaxDetails itemTaxDetailsDto,
      BigDecimal aggregateTaxPer) {
    BigDecimal sponsorAmount = BigDecimal.ZERO;
    BigDecimal itemSponsorAmt = itemTaxDetailsDto.getAmount();

    BigDecimal denomi = aggregateTaxPer.divide(new BigDecimal("100"));
    denomi = (BigDecimal.ONE).add(denomi);

    sponsorAmount = ConversionUtils.divideHighPrecision(itemSponsorAmt, denomi);

    return sponsorAmount;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.common.taxation.BaseTaxCalculator#isTaxApplicable(org.apache.
   * commons.beanutils.BasicDynaBean, com.insta.hms.common.taxation.TaxContext)
   */
  protected boolean isTaxApplicable(BasicDynaBean taxParameter, TaxContext taxContext) {

    if (null == taxParameter) {
      return false; // No tax parameters available, we assume it is not applicable
    }
    String transactionId = taxContext.getTransactionId();
    if (null == transactionId) {
      return super.isTaxApplicable(taxParameter, taxContext);
    } else {
      return true;
    }

  }

  /**
   * Gets the aggregate tax perc.
   *
   * @param itemTaxDetailsDto
   *          the item tax details dto
   * @param subgroupCodes
   *          the subgroup codes
   * @param taxContext
   *          the tax context
   * @return the aggregate tax perc
   */
  protected BigDecimal getAggregateTaxPerc(ItemTaxDetails itemTaxDetailsDto,
      List<BasicDynaBean> subgroupCodes, TaxContext taxContext) {

    BigDecimal aggregateTaxPer = BigDecimal.ZERO;
    for (BasicDynaBean subGrpCodesBean : subgroupCodes) {
      Integer subGroupId = (Integer) subGrpCodesBean.get("item_subgroup_id");
      Map<String, Object> subGroupKeyMap = new HashMap<String, Object>();
      subGroupKeyMap.put("item_subgroup_id", subGroupId);

      BasicDynaBean taxParameter = getTaxParameters(subGroupKeyMap, taxContext);

      try { 
        if (isTaxApplicable(taxParameter, taxContext)) {
          if (taxParameter != null) {
            // Consider tax rate as 0 in case of exempt tax sub group
            BigDecimal taxRate = getTaxRate(taxParameter, taxContext);
            aggregateTaxPer = aggregateTaxPer.add(taxRate);
          }
        }
      } catch (Exception exp) {
        logger.error("Error in getAggregateTaxPerc " + exp.getMessage());
      }
    }
    return aggregateTaxPer;
  }

  /*
   * public Map<String, Object> calculateSponsorTaxes(ItemTaxDetails
   * itemTaxDetails, TaxContext taxContext) throws SQLException { Map<String,
   * Object> sponsorTaxMap = new HashMap<String, Object>(); Map<Integer, Object>
   * subgrpMap = new HashMap<Integer, Object>(); List<BasicDynaBean> subGroupCodes
   * = taxContext.getSubgroups(); BigDecimal aggregateTaxPerc =
   * getAggregateTaxPerc(itemTaxDetails, subGroupCodes, taxContext); BigDecimal
   * baseClaimAmt = getBaseClaimAmount(itemTaxDetails, aggregateTaxPerc);
   * itemTaxDetails.setAmount(baseClaimAmt); sponsorTaxMap.put("sponsorAmount",
   * baseClaimAmt); for (BasicDynaBean subgroup : subGroupCodes) {
   * itemTaxDetails.setSugbroupId((Integer)subgroup.get("item_subgroup_id"));
   * Map<Integer, Object> map = calculateTaxes(itemTaxDetails, taxContext);
   * subgrpMap.putAll(map); } sponsorTaxMap.put("subGrpSponTaxDetailsMap",
   * subgrpMap); return sponsorTaxMap; }
   */
  @Override
  public Map<Integer, Object> calculateTaxes(ItemTaxDetails itemTaxDetails, TaxContext taxContext) {
    if (null != itemTaxDetails && null != itemTaxDetails.getTaxBasis()
        && "A".equalsIgnoreCase(itemTaxDetails.getTaxBasis())) {
      List<BasicDynaBean> subGroupCodes = taxContext.getSubgroups();
      BigDecimal aggregateTaxPerc = getAggregateTaxPerc(itemTaxDetails, subGroupCodes, taxContext);
      BigDecimal baseClaimAmt = getBaseClaimAmount(itemTaxDetails, aggregateTaxPerc);
      itemTaxDetails.setAdjPrice(baseClaimAmt);
    }
    return super.calculateTaxes(itemTaxDetails, taxContext);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.common.taxation.BaseTaxCalculator#getBasePrice(com.insta.hms.
   * common.taxation.ItemTaxDetails)
   */
  @Override
  protected BigDecimal getBasePrice(ItemTaxDetails itemTaxDetails) {
    if (null != itemTaxDetails && null != itemTaxDetails.getTaxBasis()
        && "A".equalsIgnoreCase(itemTaxDetails.getTaxBasis())) {
      BigDecimal amount = itemTaxDetails.getAdjPrice();
      return (null != amount) ? amount : BigDecimal.ZERO;
    }
    return super.getBasePrice(itemTaxDetails);
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
  
  private Map getTaxExpressionParams(TaxContext taxContext) throws SQLException {
      
    Map<String, Object> dataMap = new HashMap<String, Object>();
    dataMap.putAll(taxContext.getCenterBean().getMap());
    dataMap.putAll(taxContext.getPatientBean().getMap());
    dataMap.putAll(taxContext.getVisitBean().getMap());
    return dataMap;
  }

}
