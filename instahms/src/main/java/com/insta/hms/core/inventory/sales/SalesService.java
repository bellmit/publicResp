package com.insta.hms.core.inventory.sales;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxCalculator;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.billing.StoreSalesDetailsService;
import com.insta.hms.core.clinical.dischargemedication.DischargeMedicationService;
import com.insta.hms.core.clinical.prescriptions.PatientMedicinePrescriptionsService;
import com.insta.hms.core.inventory.stockmgmt.StockService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;
import com.insta.hms.mdm.item.StoreItemDetailsService;
import com.insta.hms.mdm.storeitemrates.StoreItemRatesRepository;
import com.insta.hms.mdm.stores.StoreService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;
import com.insta.hms.mdm.tpas.TpaService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * The Class SalesService.
 */
@Service
public class SalesService {
	
	static Logger logger = LoggerFactory.getLogger(SalesService.class);
	
	Map<String, TaxCalculator> calculators = new HashMap<String, TaxCalculator>();
	
	/** The Stock Service. */
	@LazyAutowired StockService  stockService;
	
	/** The Store Service. */
	@LazyAutowired StoreService  storeService;
	
	/** The Sales Main repository. */
	@LazyAutowired SalesRepository  salesRepository;
	
	/** The Sales Details repository. */
	@LazyAutowired SalesDetailsRepository  salesDetailsRepository;
	
	/** The Store Item repository. */
	@LazyAutowired StoreItemRatesRepository  storeItemRatesRepository;
	
	/** The scd repository. */
	@LazyAutowired SalesClaimDetailsRepository  scdRepository;

	/** Item Service */
	@LazyAutowired StoreItemDetailsService itemService;
	
	@LazyAutowired
	TaxGroupService itemGroupService;

	@LazyAutowired
	TaxSubGroupService itemSubGroupService;
	
	@LazyAutowired
	private SessionService sessionService;
	
	@LazyAutowired
	private HospitalCenterService centerService;
	
	@LazyAutowired
	private PatientDetailsService patientDetailsService;
	
	@LazyAutowired
  private RegistrationService regService;
	
	@LazyAutowired
	private BillService billService;
	
	@LazyAutowired
	private TpaService tpaService;
	
	@LazyAutowired
	private StoreSalesTaxRepository storeSalesTaxRepository;
	
	@LazyAutowired
	private BillChargeTaxService billChargeTaxService;
	
	@LazyAutowired
    private SalesClaimTaxRepository salesClaimTaxRepository;
	
	@LazyAutowired
	private HealthAuthorityPreferencesService healthAuthorityPreferencesService;
	
	@LazyAutowired
	private DischargeMedicationService dischargeMedicationService;
	
	@LazyAutowired
	private PatientMedicinePrescriptionsService patientMedicinePrescriptionsService;
	
	@LazyAutowired
	private InterfaceEventMappingService interfaceEventMappingService;
	
	@LazyAutowired
	private StoreSalesDetailsService storeSalesDetailsService;
	
	@Autowired
	public void setTaxCalculators(List<TaxCalculator> calculators) {
		for (TaxCalculator calculator : calculators) {
			if (calculator instanceof SalesTaxCalculator) {
				String[] supportedGroups = ((SalesTaxCalculator)calculator).getSupportedGroups();
				for (String group : supportedGroups) {
					this.calculators.put(group, calculator);
				}
			}
		}
	}
	
	public TaxCalculator getTaxCalculator(String groupCode) { //- will look something like below
		if (null == groupCode || null == calculators || calculators.isEmpty()) return null;
		return calculators.get(groupCode.trim().toUpperCase());
	}

	public Map getSupportedTaxGroups() {
		String[] groupCodes = null;
		Map groupMap = new HashMap();
		Map subGroupMap = new HashMap();
		Map result = new HashMap();
		if (null != calculators && calculators.size() > 0) {
			groupCodes = calculators.keySet().toArray(new String[0]);
			if (null != groupCodes) {
				List<BasicDynaBean> groups = itemGroupService.findByGroupCodes(groupCodes);
				groupMap = ConversionUtils.listBeanToMapMap(groups, "item_group_id");
				for (BasicDynaBean group : groups) {
					Integer groupId = (Integer)group.get("item_group_id");
					Map m = new HashMap();
					m.put("item_group_id", groupId);
					List<BasicDynaBean> l = itemSubGroupService.findByCriteria(m);
					Map subgroups = (null != l && l.size() > 0) ? ConversionUtils.listBeanToMapListMap(l, "item_group_id") : emptyMap(groupId);
					subGroupMap.putAll(subgroups);
				}
			}
		}
		result.put("item_groups", groupMap);
		result.put("item_subgroups", subGroupMap);
		return result;
	}
	
	private Map emptyMap(Integer itemGroupId) {
		Map m = new HashMap();
		m.put(itemGroupId, Collections.EMPTY_MAP);
		return m;
	}

	/**
	 * Update remittance charges.
	 *
	 * @param remittanceId the remittance id
	 */
	public void updateRemittanceCharges(Integer remittanceId) {
		scdRepository.updateCharges(remittanceId);		
	}
	
	/**
	 * Update remittance status.
	 *
	 * @param remittanceId the remittance id
	 */
	public void updateRemittanceStatus(Integer remittanceId) {
		
		scdRepository.updateStatus(remittanceId);
	}

	public void updateRemitRecoveryCharges(Integer remittanceId) {
		
		scdRepository.updateRecoveryCharges(remittanceId);				
	}

	/**
	 * Update remit charges based on the generic preference : aggregate_amt_on_remittance
	 *
	 * @param remittanceId the remittance id
	 */
	public void updateAggRemitCharges(Integer remittanceId) {
		
		scdRepository.updateAggRemitCharges(remittanceId);
	}

	/**
	 * This method is used to process the selling price expression and return processed selling price value.
	 * 
	 * @param itemBatchId
	 * @param qty
	 * @param storeId
	 * @param centerId
	 * @param bedType
	 * @param discount
	 * @return
	 * @throws Exception
	 */
	public BigDecimal getSellingPrice(int itemBatchId, BigDecimal qty, int storeId, int centerId, int visitStoreRatePlanId, double mrp, int medicineId, boolean isVisitStoreRatePlanId) throws Exception {
		BigDecimal maxPackageCP = BigDecimal.ZERO;
		BigDecimal avgPackageCP = BigDecimal.ZERO;
		Map<String, Object> results = new HashMap<String, Object>();
		BigDecimal expressionValue = BigDecimal.ZERO;
		BigDecimal orgMRP = BigDecimal.ZERO;
		BigDecimal itemSellingPrice = BigDecimal.ZERO;

		BasicDynaBean itemStock = stockService.getCPDetails(storeId, itemBatchId);
		Map<String, Object> params = new HashMap<>();
		params.put("medicine_id", medicineId);
		BasicDynaBean itemBean = itemService.findByPk(params);

		maxPackageCP = (itemStock != null && itemStock.get("max_package_cp") != null) ? 
							(BigDecimal)itemStock.get("max_package_cp") : BigDecimal.ZERO;
  
		avgPackageCP = (itemStock != null && itemStock.get("avg_package_cp") != null) ? 
							(BigDecimal)itemStock.get("avg_package_cp") : BigDecimal.ZERO;
							
		orgMRP = (itemStock != null && itemStock.get("mrp") != null) ? 
				(BigDecimal)itemStock.get("mrp") : BigDecimal.valueOf(mrp);
				
		itemSellingPrice = (itemBean != null && itemBean.get("item_selling_price") != null) ? 
		    (BigDecimal)itemBean.get("item_selling_price") : BigDecimal.ZERO;
  
		results.put("max_cp", maxPackageCP);
		results.put("average_cp", avgPackageCP);
		results.put("mrp", orgMRP);
		results.put("center_id", centerId);
		results.put("store_id", storeId);
		results.put("item_selling_price", itemSellingPrice);
		if(isVisitStoreRatePlanId){
			expressionValue = getExpressionValue(medicineId, visitStoreRatePlanId, results);
		} else if(!isVisitStoreRatePlanId) {
			Map<String, Integer> paramMap = new HashMap<String, Integer>();
			paramMap.put("dept_id", storeId);
			BasicDynaBean storeBean = storeService.findByPk(paramMap);
			expressionValue = getExpressionValue(medicineId, (Integer)storeBean.get("store_rate_plan_id"), results);
		}
		
		return expressionValue;
	}
	
	/**
	 * This method will process the expression and return the value.
	 * 
	 * @param tableName
	 * @param whereMap
	 * @param expressionColumn
	 * @param paramMap
	 * @return
	 * @throws SQLException
	 */
	private BigDecimal getExpressionValue(int medicineId, int storeRatePlanId, Map<String, Object> paramMap) throws SQLException {
		String expr = null;
		BigDecimal expressionValue  = null;
		Map<String, Object> keyValueMap = new HashMap<String, Object>();
		keyValueMap.put("medicine_id", medicineId);
		keyValueMap.put("store_rate_plan_id", storeRatePlanId);
		BasicDynaBean storeItemRatesBean = storeItemRatesRepository.findByKey(keyValueMap);
		if(storeItemRatesBean != null)
			expr = (String)storeItemRatesBean.get("selling_price_expr");
		if(expr != null) {
			StringWriter writer = new StringWriter();
			String expression = "<#setting number_format=\"##.##\">\n" + expr;
			try {
				Template expressionTemplate = new Template("expression", new StringReader(expression), new Configuration());
				expressionTemplate.process(paramMap, writer);
			} catch (TemplateException e) {
				logger.error("", e);
				return BigDecimal.ZERO;
			}catch (ArithmeticException e) {
				logger.error("", e);
				return BigDecimal.ZERO;
			}catch(Exception e){
				logger.error("", e);
				return BigDecimal.ZERO;
			}
			String exprProcessValue = "0";
			
			exprProcessValue = writer.toString().trim();
			if(exprProcessValue != null && !exprProcessValue.isEmpty()) {
				expressionValue = new BigDecimal(exprProcessValue);
			}
		}
		return expressionValue;
	}
	
	public BasicDynaBean toDetailsBean(Map reqMap) {
		BasicDynaBean salesDetailsBean = salesDetailsRepository.getBean();
		ConversionUtils.copyToDynaBean(reqMap, salesDetailsBean, null, true);
		return salesDetailsBean;
	}
	
	public BasicDynaBean toClaimDetailsBean(Map reqMap) {
		BasicDynaBean salesClaimDetailsBean = scdRepository.getBean();
		ConversionUtils.copyToDynaBean(reqMap, salesClaimDetailsBean, null, true);
		return salesClaimDetailsBean;
	}
	
	public BasicDynaBean toBillBean(Map reqMap) {
		BasicDynaBean billBean = billService.getBean();
		ConversionUtils.copyToDynaBean(reqMap, billBean, null, true);
		return billBean;
	}
	
	public BasicDynaBean toPatientBean(Map reqMap) {
		BasicDynaBean patientBean = patientDetailsService.getBean();
		ConversionUtils.copyToDynaBean(reqMap, patientBean, null, true);
		return patientBean;
	}
	
	public BasicDynaBean toTPABean(Map reqMap) {
		BasicDynaBean tpaBean = tpaService.getBean();
		ConversionUtils.copyToDynaBean(reqMap, tpaBean, null, true);
		return tpaBean;
	}
	
	public BasicDynaBean toVisitBean(Map reqMap) {
    BasicDynaBean tpaBean = regService.getBean();
    ConversionUtils.copyToDynaBean(reqMap, tpaBean, null, true);
    return tpaBean;
  }
	
	public /*List<Map<Integer, Object>>*/ Map<String, Object> getTaxDetails(BasicDynaBean salesDetails, List<BasicDynaBean> subgroupsOverride, 
			BasicDynaBean billBean, BasicDynaBean patientBean, BasicDynaBean tpaBean, BasicDynaBean visitBean) throws Exception {
		List<Map<Integer, Object>> allTaxesList = new ArrayList<Map<Integer, Object>>();
		Map<String, Object> result = new HashMap<String, Object>();
		//Get tax Context
		if(null != salesDetails.get("medicine_id")) {
			//Get list subgroups
			
			List<BasicDynaBean> subGroups = new ArrayList<BasicDynaBean>();
			// TODO: This is surely not done. Clean it up when it is time.
			if (null != subgroupsOverride && subgroupsOverride.size() > 0) {
				Integer sg[] = new Integer[subgroupsOverride.size()];				
				for (int i = 0; i < subgroupsOverride.size(); i++) {
					sg[i] = (Integer)subgroupsOverride.get(i).get("item_subgroup_id");
				}
				Map<String, Object[]> filter = new HashMap<String, Object[]>();
				filter.put("item_subgroup_id", sg);
				subGroups = itemSubGroupService.getSubGroups(filter);
			} else {
				// TODO : Service method should not take raw parameters
				subGroups = itemService.getSubgroups((Integer)salesDetails.get("medicine_id"));
			}
			
			
			ItemTaxDetails itemTaxDetails = getTaxBean(salesDetails);
			//Get center bean
			Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
			Integer centerId = (Integer) sessionAttributes.get("centerId");
			BasicDynaBean centerBean = centerService.findByKey(centerId);
			BasicDynaBean tpa = null;
			if(tpaBean.get("tpa_id") != null) {
				Map<String, Object> tpaMap = new HashMap<String, Object>();
				tpaMap.put("tpa_id", tpaBean.get("tpa_id"));
				tpa = tpaService.findByPk(tpaMap);
			}
			BasicDynaBean patBean = null;
			if(patientBean.get("mr_no") != null)
				patBean = patientDetailsService.findByKey((String)patientBean.get("mr_no"));
			else
				patBean = patientBean;
			
			visitBean = regService.findByKey((String)visitBean.get("patient_id"));
			
			TaxContext taxContext = getTaxContext(salesDetails, subGroups, billBean, patBean, centerBean, tpa, visitBean);
			for (BasicDynaBean subGroup : subGroups) {
				TaxCalculator calculator = getTaxCalculator((String)subGroup.get("group_code"));
				if (null != calculator) {
					itemTaxDetails.setSugbroupId((Integer)subGroup.get("item_subgroup_id"));
					Map<Integer, Object> taxMap = calculator.calculateTaxes(itemTaxDetails, taxContext);
				 	allTaxesList.add(taxMap);
				}
			}
			result.put("medicine_id", salesDetails.get("medicine_id"));
			result.put("net_amount", null!=itemTaxDetails.getNetAmount()?itemTaxDetails.getNetAmount():getNetAmount(itemTaxDetails));
			result.put("discount_amount", null!=itemTaxDetails.getDiscount()?itemTaxDetails.getDiscount():getDiscountAmount(itemTaxDetails));
			if(null != itemTaxDetails.getOriginalTax())
				result.put("original_tax", itemTaxDetails.getOriginalTax());
			result.put("tax_details", allTaxesList);
		}
		return result;
	}
	
	private BigDecimal getNetAmount(ItemTaxDetails itemTaxDetails) {
		BigDecimal netAmt = BigDecimal.ZERO;
		netAmt = ConversionUtils.setScale(itemTaxDetails.getMrp().multiply((ConversionUtils.divideHighPrecision(itemTaxDetails.getQty(), itemTaxDetails.getPkgSize()))));
		return netAmt;
	}
	
	private BigDecimal getDiscountAmount(ItemTaxDetails itemTaxDetails) {
		BigDecimal disountAmt = BigDecimal.ZERO;
		disountAmt = ConversionUtils.setScale(
				itemTaxDetails.getMrp().multiply((ConversionUtils.divideHighPrecision(itemTaxDetails.getQty(), itemTaxDetails.getPkgSize()))).
				multiply(itemTaxDetails.getDiscountPercent()).divide(BigDecimal.valueOf(100)));
		return disountAmt;
	}
	
	public TaxContext getTaxContext(BasicDynaBean salesDetails, List<BasicDynaBean> subGroups) {
		return getTaxContext(salesDetails, subGroups, null, null, null, null, null);
	}
	
	public TaxContext getTaxContext(BasicDynaBean salesDetails, List<BasicDynaBean> subGroups, BasicDynaBean billBean, 
			BasicDynaBean patientBean, BasicDynaBean centerBean, BasicDynaBean tpaBean, BasicDynaBean visitBean) {
		TaxContext context = new TaxContext();
		context.setItemBean(salesDetails);
		context.setSubgroups(subGroups);
		context.setBillBean(billBean);
		context.setCenterBean(centerBean);
		context.setPatientBean(patientBean);
		context.setVisitBean(visitBean);
		context.setTpaBean(tpaBean);
		return context;
	}
	/**
	 * This method is used to set the tax details and return it as a bean.
	 * 
	 * 
	 * @return
	 */
	protected ItemTaxDetails getTaxBean(BasicDynaBean salesDetails) {
		BigDecimal costPrice = BigDecimal.ZERO;
		BigDecimal mrp = BigDecimal.ZERO;
		BigDecimal qty = BigDecimal.ZERO;
		BigDecimal bonusQty = BigDecimal.ZERO;
		BigDecimal pkgSize = BigDecimal.ZERO;
		BigDecimal discount = BigDecimal.ZERO;
		BigDecimal adjMrp = BigDecimal.ZERO;
		String discountType = "E";
		String taxBasis = "M";

		if(salesDetails.get("quantity") != null) {
			qty = (BigDecimal)salesDetails.get("quantity");
		}
		if(salesDetails.get("amount") != null) {
			mrp = (BigDecimal)salesDetails.get("amount");
		}
		if(salesDetails.get("package_unit") != null) {
			pkgSize = (BigDecimal)salesDetails.get("package_unit");
		}
		if(salesDetails.get("disc") != null) {
			discount = (BigDecimal)salesDetails.get("disc");
		}
		if(salesDetails.get("discount_type") != null) {
			discountType = (String)salesDetails.get("discount_type");
		}
		if(salesDetails.get("basis") != null) {
			taxBasis = (String)salesDetails.get("basis");
		}
		
		ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
		itemTaxDetails.setCostPrice(costPrice);
		itemTaxDetails.setMrp(mrp);
		itemTaxDetails.setQty(qty);
		itemTaxDetails.setBonusQty(bonusQty);
		itemTaxDetails.setPkgSize(pkgSize);
		itemTaxDetails.setDiscountPercent(discount);
		itemTaxDetails.setTaxBasis(taxBasis);
		itemTaxDetails.setAdjMrp(adjMrp);
		itemTaxDetails.setItemId(salesDetails.get("medicine_id"));
		itemTaxDetails.setDiscountType(discountType);
		
		return itemTaxDetails;
	}
	
	public Map getBaseTaxDetails(BasicDynaBean salesDetails, BasicDynaBean salesClaimDetails) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<Integer, Object>> allTaxesList = new ArrayList<Map<Integer, Object>>();
		List<BasicDynaBean> subGroups = new ArrayList<BasicDynaBean>();
		subGroups = salesDetailsRepository.getSaleItemSubgroups((Integer)salesDetails.get("sale_item_id"), (String)salesClaimDetails.get("claim_id"));
		String adjAmt = "N";
		ItemTaxDetails itemTaxDetails = getTaxBean(salesDetails);
		TaxContext taxContext = getTaxContext(salesDetails, subGroups);
		TaxCalculator calculator = getTaxCalculator("VAT");
		for (BasicDynaBean subGroup : subGroups) {
			if (null != calculator) {
				adjAmt = (String)subGroup.get("adj_amt");
				itemTaxDetails.setSugbroupId((Integer)subGroup.get("item_subgroup_id"));
				Map<Integer, Object> taxMap = calculator.calculateTaxes(itemTaxDetails, taxContext);
			 	allTaxesList.add(taxMap);
			}
		}
		result.put("tax_details", allTaxesList);
		result.put("adj_amt", adjAmt);
		return result;
	}
	
	/**
	 * Sets the items sponsor amount.
	 *
	 * @param billNo String
	 * @return the boolean
	 */
	public Boolean setItemsSponsorAmount(String billNo) {
	  return scdRepository.setItemsSponsorAmount(billNo);
	}

	public BasicDynaBean findByKey(Map<String,Object> filterMap) {
		return salesRepository.findByKey(filterMap);
	}

	public BasicDynaBean findByKey(String keyColumn, Object identifier) {
	  return salesRepository.findByKey(keyColumn, identifier);
	}

	public List<BasicDynaBean> listAllDetails(String filterBy, String filterValue, String sortColumn){		
		return salesDetailsRepository.listAll(null, filterBy, filterValue, sortColumn);
	}

	public int updateDetails(BasicDynaBean saleBean) {
		Map<String,Object> key = new HashMap<String, Object>();
		key.put("sale_item_id", saleBean.get("sale_item_id"));
		return salesDetailsRepository.update(saleBean, key);
	}

	public BasicDynaBean getSaleItems(String chargeId) {

		return salesRepository.findByKey("charge_id", chargeId);
	}

	public List<BasicDynaBean> getSaleItemsDetails(String saleId) {

		return salesDetailsRepository.listAll(null, "sale_id", saleId);
	}

	public BasicDynaBean getSaleItemDetail(String saleItemId) {
	  if (NumberUtils.isParsable(saleItemId)) {
	    return salesDetailsRepository.findByKey("sale_item_id", Integer.parseInt(saleItemId));
	  } else {
	    return null;
	  }
	}

	public BasicDynaBean getSalesClaimBean() {

		return scdRepository.getBean();
	}

	public Boolean deleteSalesClaimDetails(String saleId) {

		return scdRepository.deletesalesClaimDetails(saleId);
	}

	public Boolean insertSalesClaimDetails(List<BasicDynaBean> salesClaimBeanList) {
		return scdRepository.batchInsert(salesClaimBeanList)[0] >= 0;
	}

	public Boolean unlockVisitSaleItems(String visitId, String billStatus) {
		return salesDetailsRepository.unlockVisitSaleItems(visitId, billStatus);
	}

	public Boolean lockVisitSaleItems(String visitId, String billStatus) {
		return salesDetailsRepository.lockVisitSaleItems(visitId, billStatus);
	}

	public List<BasicDynaBean> getSaleBillCharges(String visitId) {
		return salesRepository.getSaleBillCharges(visitId);
	}

	public BigDecimal getInsuranceClaimAmtFromSalesClaimDetails(
			String chargeId, String claimId) {
		return scdRepository.getInsuranceClaimAmtFromSalesClaimDetails(chargeId, claimId);
	}

	public BigDecimal getInsuranceClaimTaxAmtFromSalesClaimDetails(
			String chargeId, String claimId) {
		return scdRepository.getInsuranceClaimTaxAmtFromSalesClaimDetails(chargeId, claimId);
	}

	public List<BasicDynaBean> getSalesTaxDetails(String visitId) {
		return salesDetailsRepository.getSalesTaxDetails(visitId);
	}

	public List<BasicDynaBean> getSalesClaimTaxDetails(String visitId) {
		return scdRepository.getSalesClaimTaxDetails(visitId);
	}

  public BasicDynaBean getSalesClaimDetails(Map<String, Object> keys) {
    return scdRepository.findByKey(keys);
  }

  public Boolean updateSalesClaimBean(BasicDynaBean billChargeClaim, Map<String, Object> keys) {
    return scdRepository.update(billChargeClaim, keys) >= 0;
  }

  public BigDecimal getTaxAmt(Integer saleItemId) {
    BasicDynaBean saleItemBean = scdRepository.getTaxAmt(saleItemId);
    BigDecimal taxAmt = BigDecimal.ZERO;
    if(null != saleItemBean) {
       taxAmt = (BigDecimal) saleItemBean.get("tax_amt");
    }   
    return taxAmt;
  }
  
  /**
   * This method is used to update store_sales_tax_details AND bill_charge_tax tables based on adj_amt flag.
   * Because on Connect and Disconnect insurance sponsor tax amounts are calculated
   * but not updated in sales tables as per KSA rules.
   * @param patientId
   * @throws SQLException
   * @throws IOException
   */
  public void updateTaxDetails(String patientId) {
    List<BasicDynaBean> salesClaimAggTaxList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> salesItemAggTaxList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> salesItemTaxList = new ArrayList<BasicDynaBean>();    
    
    //Update store_sales_tax_details with updated sales_claim_tax_details.
    salesClaimAggTaxList = salesRepository.getSaleItemClaimAggTaxDetails(patientId);
    if(salesClaimAggTaxList != null && salesClaimAggTaxList.size() > 0) {
      Iterator<BasicDynaBean> salesClaimTaxListIterator = salesClaimAggTaxList.iterator();
      while(salesClaimTaxListIterator.hasNext()) {
        BasicDynaBean taxClaimBean = salesClaimTaxListIterator.next();
        Map<String, Object> keys = new HashMap<String,Object>();
        keys.put("sale_item_id", (Integer)taxClaimBean.get("sale_item_id"));
        keys.put("item_subgroup_id", (Integer)taxClaimBean.get("item_subgroup_id"));
        storeSalesTaxRepository.update(taxClaimBean, keys);
      }
    }
     
    //Update store_sales_details tax amount bcz tax split is updated in store_sales_tax_details.
    salesItemAggTaxList = salesRepository.getSaleItemTaxAggDetails(patientId);
    if(salesItemAggTaxList != null && salesItemAggTaxList.size() > 0) {
      Iterator<BasicDynaBean> salesItemTaxListIterator = salesItemAggTaxList.iterator();
      while(salesItemTaxListIterator.hasNext()) {
        BasicDynaBean taxItemBean = salesItemTaxListIterator.next();
        salesDetailsRepository.updateTaxInStoreSalesDetails(taxItemBean);
      }
    }
      
    //Update bill_charge_tax tax amount with latest updated columns in store_sales_tax_details. 
    salesItemTaxList = salesRepository.getSaleItemTaxDetails(patientId);
    if(salesItemTaxList != null && salesItemTaxList.size() > 0) {
      Iterator<BasicDynaBean> salesItemTaxListIterator = salesItemTaxList.iterator();
      while(salesItemTaxListIterator.hasNext()) {
        BasicDynaBean taxItemBean = salesItemTaxListIterator.next();
        billChargeTaxService.updateBillChargeTax(taxItemBean);
      }
    } 
  }

  public List<BasicDynaBean> getItemSubgroupCodes(String chargeId) {
    List<BasicDynaBean> salesTaxList=null;
    if(chargeId.contains("-")) {
      int saleItemId = Integer.parseInt(chargeId.split("\\-")[1]);
      salesTaxList = storeSalesTaxRepository.getItemSubgroupCodes(saleItemId);
    }
    return salesTaxList;
    
  }

  public BasicDynaBean getSalesClaimTaxBean(Map<String, Object> saleClaimTaxKeyMap) {
    return salesClaimTaxRepository.findByKey(saleClaimTaxKeyMap);
  }

  public void updateSalesClaimTaxBean(BasicDynaBean salesClaimBean, Map<String, Object> saleClaimTaxKeyMap) {
    salesClaimTaxRepository.update(salesClaimBean, saleClaimTaxKeyMap);
  }

  public BasicDynaBean getSalesClaimTaxBean() {
    return salesClaimTaxRepository.getBean();
  }

  public Boolean insertSalesClaimTaxBean(BasicDynaBean salesClaimTaxBean) {
    return salesClaimTaxRepository.insert(salesClaimTaxBean) >= 0;
  }

  public Boolean isSaleIdValid(String saleId) {
    return salesRepository.exist("sale_id", saleId);
  }
  public Boolean isSaleItemIdValid(String saleItemId) {
    if (NumberUtils.isParsable(saleItemId)) {
      return salesDetailsRepository.exist("sale_item_id", Integer.parseInt(saleItemId));
    } else {
      return false;
    }
  }

  public Boolean updateSalesClaimOnEditIns(String billNo, String sponsorId, String oldClaimId,
      String newClaimId) {
    return scdRepository.updateSalesClaimOnEditIns(billNo, sponsorId, oldClaimId, newClaimId);
  }
  
  /**
   * Triggers hl7 event for medicines sold.
   * 
   * @param docPrescIdList is prescription sale id
   * @param dischargeMedPrescIdList is the discharge medication sale id
   * @param saleId the sale bill number
   * @param visitId the visit id
   */
  public void triggerEvent(List<Integer> docPrescIdList, List<Integer> dischargeMedPrescIdList,
      String saleId, String visitId) {
    boolean isPrescribeByGenerics = healthAuthorityPreferencesService.isPrescribeByGenerics(RequestContext.getCenterId());
    if (isPrescribeByGenerics && !StringUtils.isEmpty(saleId) && !StringUtils.isEmpty(visitId)) {
      List<Integer> saleItemIds = storeSalesDetailsService.filterMedicineSaleItemId(saleId);
      if (!saleItemIds.isEmpty()) {
        interfaceEventMappingService.medicinePrescDispenseEvent(visitId, saleItemIds);
      }
    } else {
      dischargeMedPrescIdList =
          patientMedicinePrescriptionsService.filterMedicinePrescIds(dischargeMedPrescIdList, false);
      interfaceEventMappingService.medicinePrescDispenseEvent(visitId, dischargeMedPrescIdList);

      docPrescIdList = 
          patientMedicinePrescriptionsService.filterMedicinePrescIds(docPrescIdList, false);
      interfaceEventMappingService.medicinePrescDispenseEvent(visitId, docPrescIdList);
    }
  }
}
