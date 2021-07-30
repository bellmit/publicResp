package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.otmasters.opemaster.OperationMasterDAO;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxCalculator;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.core.billing.InsuranceCategoryHelper;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.master.AnaesthesiaTypeMaster.AnaesthesiaTypeMasterDAO;
import com.insta.hms.master.CommonChargesMaster.CommonChargesDAO;
import com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO;
import com.insta.hms.master.DRGCodesMaster.DRGCodesMasterDAO;
import com.insta.hms.master.DietaryMaster.DietaryMasterDAO;
import com.insta.hms.master.EquipmentMaster.EquipmentMasterDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.services.ServicesDAO;
import com.insta.hms.stores.MedicineSalesDTO;
import com.insta.hms.stores.StockPatientIssueReturnsDAO;
import com.insta.hms.stores.StockUserIssueDAO;
import com.insta.hms.stores.StockUserReturnDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class BillingHelper {
	ServicesDAO serviceDAO = new ServicesDAO();
	AnaesthesiaTypeMasterDAO anaesthesiaTypeDAO = new AnaesthesiaTypeMasterDAO();
	TheatreMasterDAO theatreDAO = new TheatreMasterDAO();
	EquipmentMasterDAO equipmentDAO = new EquipmentMasterDAO();
	OperationMasterDAO operationDAO = new OperationMasterDAO();
	AddTestDAOImpl testDAO = new AddTestDAOImpl(null);
	ConsultationTypesDAO consultationTypesDAO = new ConsultationTypesDAO();
	BedMasterDAO bedTypesDAO = new BedMasterDAO();
	CommonChargesDAO commonChargesDAO = new CommonChargesDAO();
	PackageDAO packageDAO = new PackageDAO(null);
	DietaryMasterDAO dietaryDAO = new DietaryMasterDAO();
	DRGCodesMasterDAO drgDAO = new DRGCodesMasterDAO();
	GenericDAO billChargeDAO= new GenericDAO("bill_charge");
	PlanMasterDAO planMasterDAO = new PlanMasterDAO();
	private static final BillBO billBo = new BillBO();
  private static final StockPatientIssueReturnsDAO stockPatientIssueReturnsDAO = new StockPatientIssueReturnsDAO();
  private static final GenericDAO stockPatientIssueDao = new GenericDAO("stock_issue_details");
	
	private static RedisTemplate<String, Object> redisTemplate = 
	        (RedisTemplate) ApplicationContextProvider.getApplicationContext().getBean("redisTemplate");
	public static final String PHIL_HEALTH_CONSTANT_VAL ="philhealth:philhealth";

	static Map<String, TaxCalculator> calculators = new HashMap<String, TaxCalculator>();
	static{ 
		String[] supportedGroups = new BillingTaxCalculator().getSupportedGroups();
		for (String group : supportedGroups) {
			calculators.put(group, new BillingTaxCalculator());
		}
		
		String[] ksaSupportedGroups = new BillingKSATaxCalculator().getSupportedGroups();
		for (String group : ksaSupportedGroups) {
			calculators.put(group, new BillingKSATaxCalculator());
		}
	}
	
	public TaxCalculator getTaxCalculator(String groupCode) { //- will look something like below
		if (null == groupCode || null == calculators || calculators.isEmpty()) return null;
		return calculators.get(groupCode.trim().toUpperCase());
	}
	
	public List<BasicDynaBean> getItemSubgroupCodes(String actDescriptionId, String chargeGroup, String chargeHead, int consultationId, String opId) throws SQLException {
		
		List<BasicDynaBean> itemSubGroupCodes = new ArrayList<BasicDynaBean>();		
		if(chargeGroup.equals("SNP")){		
			itemSubGroupCodes = serviceDAO.getServiceItemSubGroupTaxDetails(actDescriptionId);
		}
		if(chargeGroup.equals("OPE")){
			if(chargeHead.equals("ANATOPE")){				
				itemSubGroupCodes = anaesthesiaTypeDAO.getAnaesthesiaTypeSubGroupTaxDetails(actDescriptionId);				
			}else if (chargeHead.equals("TCOPE")){				
				itemSubGroupCodes = theatreDAO.getTheatreItemSubGroupTaxDetails(actDescriptionId);	
			}else if(chargeHead.equals("EQOPE")){				
				itemSubGroupCodes = equipmentDAO.getEquipmentItemSubGroupTaxDetails(actDescriptionId);
			}else{				
				itemSubGroupCodes = operationDAO.getOperationItemSubGroupTaxDetails(opId);
			}
		}
		if(chargeGroup.equals("DIA")){		
			itemSubGroupCodes = testDAO.getDiagnosticsItemSubGroupTaxDetails (actDescriptionId);
		}
		if(chargeGroup.equals("DOC")){			
			itemSubGroupCodes = consultationTypesDAO.getConsultationTypeItemSubGroupTaxDetails(consultationId);
		}
		if(chargeGroup.equals("BED") || chargeGroup.equals("ICU")){
			
			boolean result = false;
            Pattern pattern = Pattern.compile("[0-9]+"); 
            pattern = Pattern.compile("\\d+"); 
            
            result = pattern.matcher(actDescriptionId).matches();
            if(result){
            	itemSubGroupCodes = bedTypesDAO.getBedItemSubGroupTaxDetails(actDescriptionId);
            }else{
            	itemSubGroupCodes = bedTypesDAO.getBedTypeItemSubGroupTaxDetails(actDescriptionId);
            }
			
		}
		if(chargeGroup.equals("OTC")){
			if(chargeHead.equals("EQUOTC")){
				itemSubGroupCodes = equipmentDAO.getEquipmentItemSubGroupTaxDetails(actDescriptionId);
			}else{				
				itemSubGroupCodes = commonChargesDAO.getCommonChargesItemSubGroupTaxDetails(actDescriptionId);
			}
		}
		if(chargeGroup.equals("PKG")){			
			itemSubGroupCodes = packageDAO.getPackageItemSubGroupTaxDetails(actDescriptionId);
		}
		if(chargeGroup.equals("DIE")){
			itemSubGroupCodes = dietaryDAO.getDietaryItemSubGroupTaxDetails(actDescriptionId);
		}
		if(chargeGroup.equals("DRG")){
			itemSubGroupCodes = drgDAO.getDrgItemSubGroupTaxDetails(actDescriptionId);
		}
		return itemSubGroupCodes;
	}

	
	public Map<Integer,Object> getTaxChargesMap(ItemTaxDetails itemTaxDetails,
			TaxContext taxContext, List<BasicDynaBean> subGroupCodes) throws SQLException {
		Map<Integer, Object> taxChargesMap = new HashMap<Integer, Object>();
		for(BasicDynaBean subGroupCodeBean : subGroupCodes){
			itemTaxDetails.setSugbroupId((Integer)subGroupCodeBean.get("item_subgroup_id"));
			TaxCalculator calculator = getTaxCalculator((String)subGroupCodeBean.get("group_code"));
			if(calculator != null) {
				Map<Integer,Object> taxMap = calculator.calculateTaxes(itemTaxDetails,taxContext);
				taxChargesMap.putAll(taxMap);
			}
		}	
		return taxChargesMap;
	}
	
	/*
	public Map<Integer,Object> getAdjustedTaxChargesMap(ItemTaxDetails itemTaxDetails,
			TaxContext taxContext, List<BasicDynaBean> subGroupCodes) throws SQLException {
		BillingSponsorTaxCalculator taxCalculator = new BillingSponsorTaxCalculator();
		Map<Integer, Object> taxChargesMap = new HashMap<Integer, Object>();
		for(BasicDynaBean subGroupCodeBean : subGroupCodes){
			itemTaxDetails.setSugbroupId((Integer)subGroupCodeBean.get("item_subgroup_id"));
			Map<Integer,Object> taxMap = taxCalculator.calculateTaxes(itemTaxDetails,taxContext);
			taxChargesMap.putAll(taxMap);
		}	
		return taxChargesMap;
	}
	*/
	
	public Map<String,Object> calculateSponsorTaxes(ItemTaxDetails itemTaxDetails,
			TaxContext taxContext, List<BasicDynaBean> subGroupCodes) throws SQLException {
		Map<String, Object> sponsorTaxMap = new HashMap<String, Object>();
		Map<Integer, Object> taxChargesMap = getTaxChargesMap(itemTaxDetails, taxContext, subGroupCodes);
		// TODO taxation : check if the back calculation was not done, then getAdjPrice will return null
		sponsorTaxMap.put("sponsorAmount", (null != itemTaxDetails.getAdjPrice()) ? 
					itemTaxDetails.getAdjPrice().setScale(3, RoundingMode.HALF_DOWN) :
					BigDecimal.ZERO);
		sponsorTaxMap.put("subGrpSponTaxDetailsMap", taxChargesMap);
		return sponsorTaxMap;
	}
/*
	public Map<String,Object> calculateAdjustedSponsorTaxes(ItemTaxDetails itemTaxDetails,
			TaxContext taxContext, List<BasicDynaBean> subGroupCodes) throws SQLException {
		Map<String, Object> sponsorTaxMap = new HashMap<String, Object>();
		Map<Integer, Object> taxChargesMap = getTaxChargesMap(itemTaxDetails, taxContext, subGroupCodes);
		sponsorTaxMap.put("sponsorAmount", itemTaxDetails.getAmount());
		sponsorTaxMap.put("subGrpSponTaxDetailsMap", taxChargesMap);
		return sponsorTaxMap;
	}
*/	

	public List<BasicDynaBean> getItemSubgroupCodes(String chargeId) throws SQLException {
		BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
		return (List<BasicDynaBean>) billChargeTaxDAO.getItemSubgroupCodes(chargeId);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String,Object> getItemMapForInsuranceCategories(List<BasicDynaBean> newCharges) {	
		Map itemMap = new HashMap();
		for(BasicDynaBean bean :newCharges){
			List<Object> items = new ArrayList<Object>();
			String chargeHead = (String)bean.get("charge_head");
			String chargeGroup = (String)bean.get("charge_group");
			
			String key = chargeGroup.concat("-").concat(chargeHead);
			
			if(itemMap.containsKey(key)){
				items = (List<Object>) itemMap.get(key);
				if(chargeGroup.equals("DOC")){
				  items.add(bean.get("consultation_type_id"));
        } else if (chargeGroup.equals("OPE") && !chargeHead.equals("ANATOPE")) {
          items.add(bean.get("op_id"));
        } else if(chargeHead.equals("INVITE")
            || chargeGroup.equals("DIE") || chargeGroup.equals("MED")){
          Integer itemId = Integer.parseInt((String)bean.get("act_description_id"));
          items.add(itemId);
        } else if (chargeGroup.equals("PKG")) {
          items.add((Integer) bean.get("package_id"));
        } else{
				  items.add(bean.get("act_description_id"));
				}	
			}else{
			  if(chargeGroup.equals("DOC")){
				  items.add(bean.get("consultation_type_id"));
        } else if (chargeGroup.equals("OPE") && !chargeHead.equals("ANATOPE")) {
          items.add(bean.get("op_id"));
        } else if(chargeHead.equals("INVITE")
            || chargeGroup.equals("DIE") || chargeGroup.equals("MED")){
          Integer itemId = Integer.parseInt((String)bean.get("act_description_id"));
          items.add(itemId);
        } else if (chargeGroup.equals("PKG")) {
          items.add((Integer) bean.get("package_id"));
        } else{
				  items.add(bean.get("act_description_id"));
			  }
				itemMap.put(key, items);
			}
		}
		return itemMap;
	}

	@SuppressWarnings("unchecked")
	public void getCatIdBasedOnPlanIds(List<BasicDynaBean> newCharges,
			Map<String,Object> itemMap, Set<Integer> planIds, Map<String, BasicDynaBean> details) throws SQLException {
		
		String visitType="o";
		if(!details.isEmpty()){
			visitType = (String)details.get("visit").get("visit_type");
		}
		
		for(Map.Entry<String, Object> itemMapEntry : itemMap.entrySet()){
			List<BasicDynaBean> itemBeanList= new ArrayList<BasicDynaBean>();
			itemBeanList = callRespectiveTypesToGetInsCat(planIds, visitType,
					itemMapEntry.getKey(), itemBeanList, (List<String>)itemMapEntry.getValue());
			setInsCatInTemplate(itemMapEntry, itemBeanList, planIds, visitType);
		}		
	}

	private List<BasicDynaBean> callRespectiveTypesToGetInsCat(
			Set<Integer> planIds, String visitType,
			String keys,
			List<BasicDynaBean> itemBeanList, List<String> listItemIds) throws SQLException {
		
		
	  InsuranceCategoryHelper categoryHelper = new InsuranceCategoryHelper();
	  PlanMasterDAO planDAO = new PlanMasterDAO();
	  
	  String chargeGroup = keys.split("-")[0];
	  String chargeHead = keys.split("-")[1];
	  
	  	if(chargeGroup.equals("DIA")){ 
	  	 itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_DIAG_CATEGORY_ID_BASED_ON_PLAN, 
          listItemIds,planIds,visitType);
	    }else if(chargeGroup.equals("PKG")){
		      itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_PKG_CATEGORY_ID_BASED_ON_PLAN, 
			          listItemIds, planIds, visitType);
		}else if(chargeGroup.equals("SNP")){
	      itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_SERVICE_CATEGORY_ID_BASED_ON_PLAN,
	          listItemIds,planIds,visitType);  
	    }else if(chargeHead.equals("ANATOPE")){
	      itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_ANESTHESIA_CATEGORY_ID_BASED_ON_PLAN,
	          listItemIds,planIds,visitType);
	    }else if(chargeHead.equals("EQOPE")){
	      itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_EQUIPMENT_CATEGORY_ID_BASED_ON_PLAN,
	          listItemIds,planIds,visitType);
	    }else if(chargeGroup.equals("OPE")){
	        itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_OPERATION_CATEGORY_ID_BASED_ON_PLAN,
	            listItemIds,planIds,visitType);
	    }else if(chargeGroup.equals("BED") || chargeGroup.equals("ICU")){
	    	boolean result = false;
            Pattern pattern = Pattern.compile("[0-9]+"); 
            pattern = Pattern.compile("\\d+"); 
                      
            result = pattern.matcher(listItemIds.get(0)).matches();
            if(result){
              List<String> bedTypeIds = bedTypesDAO.getBedTypeList(listItemIds);
            	itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_BED_NAME_CATEGORY_ID_BASED_ON_PLAN, 
            	    bedTypeIds, planIds, visitType);
            }else{
            	itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_BED_NAME_CATEGORY_ID_BASED_ON_PLAN, 
            	          listItemIds, planIds, visitType);
            }	      
	    }else if(chargeGroup.equals("MED") || chargeHead.equals("INVITE")){
	      itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_MED_CATEGORY_ID_BASED_ON_PLAN, 
	          listItemIds, planIds, visitType);
	    }else if(chargeGroup.equals("OTC")){
	      if(chargeHead.equals("EQUOTC")){
	        itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_EQUIPMENT_CATEGORY_ID_BASED_ON_PLAN, 
	            listItemIds, planIds, visitType);
	      }else{
	        itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_OTHERCHARGE_CATEGORY_ID_BASED_ON_PLAN, 
	            listItemIds, planIds, visitType);
	      }
	    }else if(chargeGroup.equals("DOC")){
	        itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_DOCTOR_CATEGORY_ID_BASED_ON_PLAN, 
	            listItemIds, planIds, visitType);
	    }else if(chargeGroup.equals("DIE")){
	    	itemBeanList = planDAO.getCatIdBasedOnPlanIds(categoryHelper.GET_DIETARY_CATEGORY_ID_BASED_ON_PLAN,
	    			listItemIds, planIds, visitType);
		}
		
		return itemBeanList;
	}

	private void setInsCatInTemplate(Entry<String, Object> itemMapEntry,
			List<BasicDynaBean> itemBeanList, Set<Integer> planIds, String visitType) {
		//<schema-PhilHealth-itemId-PlanId-ChargeHead><cat-id>
		String schema =RequestContext.getSchema();
		Object[] planId = planIds.toArray();
		for(BasicDynaBean itemBean : itemBeanList){
			String priRedisKey = "sch:"+schema+";"+PHIL_HEALTH_CONSTANT_VAL +";"+"item:"+itemBean.get("item_id")+";"+
									"plan:"+planId[0]+";"+"visittype:"+visitType+";"+"ch:"+itemMapEntry.getKey();
			redisTemplate.opsForValue().set(priRedisKey,itemBean.get("primary_insurance_category_id"));
			redisTemplate.expire(priRedisKey, 24,TimeUnit.HOURS);
			if(planId.length>1){
				  String secRedisKey = "sch:"+schema+";"+PHIL_HEALTH_CONSTANT_VAL +";"+"item:"+itemBean.get("item_id")+";"+
						  					"plan:"+planId[1]+";"+"visittype:"+visitType+";"+"ch:"+itemMapEntry.getKey();	
				  redisTemplate.opsForValue().set(secRedisKey,itemBean.get("secondary_insurance_category_id"));
				  redisTemplate.expire(secRedisKey, 24,TimeUnit.HOURS);
			}
		}
	}
	
	public String getInsCatIdFromTemplate(BasicDynaBean charge,
			int planId, BasicDynaBean chargeClaimBean, String visitType) {
		String schema = RequestContext.getSchema();
		Object itemId = (Object)charge.get("act_description_id");
		String chargeGroup = (String)charge.get("charge_group");
    String chargeHead = (String) charge.get("charge_head");

    if (chargeGroup.equals("OPE") && !chargeHead.equals("ANATOPE")) {
      itemId = (Object) charge.get("op_id");
    }
		if(chargeGroup.equals("DOC")){
		  itemId = (Object)charge.get("consultation_type_id");
		}
		if(charge.get("charge_group").equals("PKG")){
		  itemId = (Object)charge.get("package_id");
		}

		String key = chargeGroup.concat("-").concat(chargeHead);
		String redisKey = "sch:"+schema+";"+PHIL_HEALTH_CONSTANT_VAL+
				";"+"item:"+itemId+";"+"plan:"+planId+";"+"visittype:"+visitType+";"+"ch:"+key;

		String redisValue = (String) redisTemplate.opsForValue().get(redisKey);	
		if(null != redisValue && !redisValue.equals("")){
		  chargeClaimBean.set("insurance_category_id", Integer.parseInt(redisValue));
		}else{
			chargeClaimBean.set("insurance_category_id", 0);
		}
		return redisValue;
	}
	
  /**
   * Invalidate ins cat id key in redis.
   *
   * @param charge the charge
   * @param planIds the plan ids
   * @param visitType the visit type
   * @throws SQLException the SQL exception
   */
  public void invalidateInsCatIdKeyInRedis(ChargeDTO charge, int planId, String visitType)
      throws SQLException {
    BasicDynaBean chargeBean = convertChargeDtoToBean(charge);
    String schema = RequestContext.getSchema();
    Object itemId = (Object) chargeBean.get("act_description_id");
    String chargeGroup = (String) chargeBean.get("charge_group");
    String chargeHead = (String) chargeBean.get("charge_head");

    if (chargeGroup.equals("OPE") && !chargeHead.equals("ANATOPE")) {
      itemId = (Object) chargeBean.get("op_id");
    }
    if (chargeGroup.equals("DOC")) {
      itemId = (Object) chargeBean.get("consultation_type_id");
    }

    String key = chargeGroup.concat("-").concat(chargeHead);
    String redisKey = "sch:" + schema + ";" + PHIL_HEALTH_CONSTANT_VAL + ";" + "item:" + itemId
        + ";" + "plan:" + planId + ";" + "visittype:" + visitType + ";" + "ch:"
        + key;

    RedisTemplate<String, Object> redisTemplate = (RedisTemplate) ApplicationContextProvider
        .getApplicationContext().getBean("redisTemplate");

    redisTemplate.delete(redisKey);
  }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void checkForInsCatInRedis(Connection con, ChargeDTO charge, int planId) throws SQLException {
		BasicDynaBean chargeBean = convertChargeDtoToBean(charge);
		String redisValue = getInsCatIdFromTemplate(chargeBean,planId,chargeBean,charge.getVisitType());
		if(null == redisValue || redisValue.isEmpty()){
			Set planIds = new HashSet();
			planIds.add(planId);
			List itemIds = new ArrayList();
			if(charge.getChargeGroup().equals("DOC")){
				itemIds.add(charge.getConsultation_type_id()); 
			}else if(charge.getChargeGroup().equals("OPE")){
			  if(charge.getChargeHead().equals("ANATOPE")){
			    itemIds.add(charge.getActDescriptionId());
			  }else{
			    itemIds.add(charge.getOp_id());  
			  }
			} else if (charge.getChargeGroup().equals("PKG")) {
				//for migrated charges
				if (charge.getPackageId() == null && "PKGPKG".equals(charge.getChargeHead())) {
					itemIds.add(Integer.parseInt(charge.getActDescriptionId()));
				} else {
					itemIds.add((Integer) charge.getPackageId());
				}
			} else if(charge.getChargeHead().equals("INVITE")
					    || charge.getChargeGroup().equals("DIE")){
		        String actDescriptionId = charge.getActDescriptionId();
		        if (actDescriptionId.matches("[0-9]+")) {
		          Integer itemId = Integer.parseInt(actDescriptionId);
		          itemIds.add(itemId);
		        }
      		}else{
			    itemIds.add(charge.getActDescriptionId());
			}
			
			List<BasicDynaBean> itemBeanList= new ArrayList<BasicDynaBean>();
			String keys = charge.getChargeGroup().concat("-").concat(charge.getChargeHead());
			
      if (!itemIds.isEmpty()) {
        itemBeanList = callRespectiveTypesToGetInsCat(planIds, charge.getVisitType(), keys,
            itemBeanList, itemIds);
      }
			if(null != itemBeanList && itemBeanList.size() > 0){
			  for(BasicDynaBean itemBean : itemBeanList){
				  charge.setInsuranceCategoryId((Integer)itemBean.get("primary_insurance_category_id"));
			  }
			}else{
			  charge.setInsuranceCategoryId(0);
			  if(charge.getChargeGroup().equals("REG")){
			    Boolean isGeneralCategoryExistsForRegCharges = checkIsGeneralCategoryExistsForRegCharges(con,charge.getVisitId(),charge.getVisitType(), planId);
			    if(isGeneralCategoryExistsForRegCharges){
			      charge.setInsuranceCategoryId(-1);
			    }
			  }
			  if(charge.getChargeGroup().equals("DRG")){
			    charge.setInsuranceCategoryId(-2);
			  }
			}
		}else{
			charge.setInsuranceCategoryId(Integer.parseInt(redisValue));
		}		
	}

  private Boolean checkIsGeneralCategoryExistsForRegCharges(Connection con, String visitId, String visitType, int planId) throws SQLException {
    // TODO Auto-generated method stub
    return planMasterDAO.checkIsGeneralCategoryExistsForRegCharges(con,visitId, planId, visitType);
  }

  private BasicDynaBean convertChargeDtoToBean(ChargeDTO charge) throws SQLException {
		BasicDynaBean bean = billChargeDAO.getBean();
		bean.set("charge_head", charge.getChargeHead());
		bean.set("act_description_id", charge.getActDescriptionId());
		bean.set("insurance_category_id", charge.getInsuranceCategoryId());
		bean.set("op_id", charge.getOp_id());
		bean.set("charge_group", charge.getChargeGroup());
		bean.set("act_description", charge.getActDescription());
		bean.set("consultation_type_id", charge.getConsultation_type_id());
		return bean;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void checkSaleItemsForInsCatInRedis(ChargeDTO charge,
			MedicineSalesDTO saleItem, int planId) throws SQLException {
		BasicDynaBean chargeBean = convertChargeDtoToBean(charge);
		chargeBean.set("act_description_id", saleItem.getMedicineId());
		String redisValue = getInsCatIdFromTemplate(chargeBean,planId,chargeBean, charge.getVisitType());
		if(null == redisValue || redisValue.isEmpty()){
			Set planIds = new HashSet();
			planIds.add(planId);
			List itemIds = new ArrayList();
			if(StringUtils.isNotBlank(saleItem.getMedicineId())){
			  itemIds.add(Integer.parseInt(saleItem.getMedicineId()));
			}
			List<BasicDynaBean> itemBeanList= new ArrayList<BasicDynaBean>();
			String keys = charge.getChargeGroup().concat("-").concat(charge.getChargeHead());
			itemBeanList = callRespectiveTypesToGetInsCat(planIds, charge.getVisitType(), keys, itemBeanList, itemIds);
			for(BasicDynaBean itemBean : itemBeanList){
				saleItem.setInsuranceCategoryId((Integer)itemBean.get("primary_insurance_category_id"));
			}
		}else{
			saleItem.setInsuranceCategoryId(Integer.parseInt(redisValue));
		}
	}

	public void saveBillChargeBillingGroup(List<ChargeDTO> billCharges) {
		for (ChargeDTO billCharge : billCharges) {
			if(null != billCharge){
				if (billCharge.getChargeHead() != null || !"".equals(billCharge.getChargeHead())) {
					String itemId = (String)billCharge.getActDescriptionId();
					if(billCharge.getChargeGroup().equals("OPE")){
						itemId = (String)billCharge.getOp_id();
					}
					if(billCharge.getChargeGroup().equals("DOC")){
					  itemId = Integer.toString(billCharge.getConsultation_type_id());
					}
          if(billCharge.getChargeGroup().equals("PKG")){
             if (billCharge.getPackageId() != null) {
               itemId = Integer.toString(billCharge.getPackageId());
             }
          }
					String redisKey = String.format("schema:%s;user:%s;ch:%s;cg:%s;item%s",
							RequestContext.getSchema(), RequestContext.getUserName(), billCharge.getChargeHead(),
							billCharge.getChargeGroup(),itemId);
					String billingGroupId = "";
					if (billCharge.getBillingGroupId() != null) {
						billingGroupId = Integer.toString(billCharge.getBillingGroupId());
					}
					redisTemplate.opsForValue().set(redisKey, billingGroupId);
					redisTemplate.expire(redisKey, 24,TimeUnit.HOURS);
				}
			}
		}
	}
	

	public boolean resetInventoryCharges(String visitId) throws SQLException{
	   Connection con = DataBaseUtil.getConnection();
	   
	   boolean result = false;
	   try{
	     con.setAutoCommit(false);
	     result = resetInventoryCharges(con, visitId);	     
	     return result;
	   }finally{
	     DataBaseUtil.commitClose(con, result);
	   }
	}
	
  public boolean resetInventoryCharges(Connection con, String visitId) throws SQLException{
    
    ChargeDAO chargeDAO = new ChargeDAO(con);
    List<ChargeDTO> charges = chargeDAO.getVisitCharges(visitId);
    
    for(ChargeDTO charge : charges){
      if(charge.getChargeHead().equals(ChargeDTO.CH_INVENTORY_ITEM)){
        BigDecimal unitDiscount = BigDecimal.ZERO;
        if(charge.getActQuantity().add(charge.getReturnQty()).compareTo(BigDecimal.ZERO)!=0){
          unitDiscount = charge.getDiscount().divide(charge.getActQuantity().add(charge.getReturnQty()), 3, BigDecimal.ROUND_HALF_DOWN);
        }
        BigDecimal returnDiscount = unitDiscount.multiply(charge.getReturnQty().negate());
        charge.setDiscount(charge.getDiscount().add(returnDiscount));
        charge.setAmount(charge.getAmount().subtract(returnDiscount));
        charge.setReturnQty(BigDecimal.ZERO);
        charge.setReturnTaxAmt(BigDecimal.ZERO);
        charge.setReturnAmt(BigDecimal.ZERO);
        charge.setReturnOriginalTaxAmt(BigDecimal.ZERO);
        
        stockPatientIssueReturnsDAO.resetReturnQuantity(con, charge.getChargeId());
      }
    }
    
    return chargeDAO.updateReturnAmounts(charges);

  }
  
  public boolean replayInventoryReturns(String visitId) throws SQLException, IOException{
    Connection con = DataBaseUtil.getConnection();
    
    boolean result = true;
    try{
      con.setAutoCommit(false);
      replayInventoryReturns(con, visitId);      
      return result;
    }finally{
      DataBaseUtil.commitClose(con, result);
    }
 }  
  public void replayInventoryReturns(Connection con, String visitId) throws SQLException, IOException{
    
    StockUserReturnDAO stockUserReturnDao = new StockUserReturnDAO(con);
    ChargeDAO chargeDao = new ChargeDAO(con);
    List<BasicDynaBean> returnCharges = stockUserReturnDao.getIssueReturnCharges(visitId);
    for(BasicDynaBean returnCharge : returnCharges){
      BigDecimal redQty = BigDecimal.ZERO;
      BigDecimal remQty = ((BigDecimal)returnCharge.get("act_quantity")).negate();
      List<BasicDynaBean> issues = stockPatientIssueReturnsDAO.getVisitItemIssues(con,
          (String) returnCharge.get("visit_id"), Integer.parseInt((String)returnCharge.get("medicine_id")),
          (Integer) returnCharge.get("item_batch_id"), (Integer) returnCharge.get("dept_to"),(String) returnCharge.get("charge_id"));
      BigDecimal taxAmtForReturn = BigDecimal.ZERO;
      BigDecimal originalTaxAmtForReturn = BigDecimal.ZERO;
      for (BasicDynaBean issue : issues) {
        
        BigDecimal rate = (BigDecimal) issue.get("act_rate");
        BigDecimal discount = BigDecimal.ZERO; 
        
        if (remQty.compareTo(BigDecimal.ZERO) == 0){
          break; 
        }

        if (((BigDecimal) issue.get("qty")).compareTo((BigDecimal) issue.get("return_qty")) == 0)
          continue;
        
        redQty = remQty.compareTo(
            ((BigDecimal) issue.get("qty")).subtract((BigDecimal) issue.get("return_qty"))) > 0
                ? ((BigDecimal) issue.get("qty")).subtract((BigDecimal) issue.get("return_qty"))
                : remQty;
        remQty = remQty.subtract(redQty);
        
        
        BigDecimal taxAmt = ((BigDecimal) issue.get("tax_amt"))
            .divide(((BigDecimal) issue.get("act_quantity")), 3 , BigDecimal.ROUND_HALF_DOWN
                ).multiply(redQty);
        taxAmtForReturn = taxAmtForReturn.add(taxAmt);
        BigDecimal originalTaxAmt = ((BigDecimal) issue.get("original_tax_amt"))
            .divide(((BigDecimal) issue.get("act_quantity")), 3 , BigDecimal.ROUND_HALF_DOWN
                ).multiply(redQty);
        originalTaxAmtForReturn = originalTaxAmtForReturn.add(originalTaxAmt);
        BasicDynaBean issueBean = StockUserIssueDAO
            .getIssueItemCharge(con, issue.get("item_issue_no").toString());
        
        
        BigDecimal issueRetAmount =(rate.multiply(redQty)).subtract(discount.multiply(redQty));

        
        List<ChargeDTO> updateChargeList = new ArrayList<>();
        StockUserReturnDAO.setIssueItemsForReturns(redQty, issueRetAmount, BigDecimal.ZERO,
            updateChargeList, issueBean);
        
        for (ChargeDTO iss : updateChargeList) {
          iss.setReturnInsuranceClaimAmt(BigDecimal.ZERO);
          iss.setReturnTaxAmt(taxAmt.negate());
          iss.setReturnOriginalTaxAmt(originalTaxAmt.negate());
        }

        chargeDao.updateSaleChargesWithTax(updateChargeList);
        BasicDynaBean patientIssueDetailsBean = stockPatientIssueDao.findByKey(con, "item_issue_no",
            issue.get("item_issue_no"));
        stockPatientIssueDao.update(con,
            Collections.singletonMap("return_qty",
                ((BigDecimal) patientIssueDetailsBean.get("return_qty")).add(redQty)),
            Collections.singletonMap("item_issue_no", issue.get("item_issue_no")));
      }
      ChargeDTO returnChargeDTO = chargeDao.getCharge((String) returnCharge.get("charge_id"));
      returnChargeDTO.setTaxAmt(taxAmtForReturn.negate());
      returnChargeDTO.setOriginalTaxAmt(originalTaxAmtForReturn.negate());
      chargeDao.updateChargeTax(returnChargeDTO);
    }
	}
    
	public void setBillChargeBillingGroup(ChargeDTO billCharge) {
		Integer billingGroupId = null;
		if (billCharge.getBillingGroupId() == null) {
			if (billCharge.getChargeHead() != null || !"".equals(billCharge.getChargeHead())) {
				String itemId = (String)billCharge.getActDescriptionId();
				if(billCharge.getChargeGroup().equals("OPE")){
					itemId = (String)billCharge.getOp_id();
				}
				if(billCharge.getChargeGroup().equals("DOC")){
				  itemId = Integer.toString(billCharge.getConsultation_type_id());
				}
        if( billCharge.getPackageId() !=  null && ("PKG").equals(billCharge.getChargeGroup())){
          itemId = Integer.toString(billCharge.getPackageId());
        }
				String redisKey = String.format("schema:%s;user:%s;ch:%s;cg:%s;item%s",
						RequestContext.getSchema(), RequestContext.getUserName(), billCharge.getChargeHead(),
						billCharge.getChargeGroup(),itemId);
				String redisValue = (String) redisTemplate.opsForValue().get(redisKey);
				if(null != redisValue && !redisValue.equals("")){
					billingGroupId = Integer.parseInt(redisValue);
				}
			}
		} else {
			billingGroupId = billCharge.getBillingGroupId();
		}
		billCharge.setBillingGroupId(billingGroupId);
	}

	public String getRevenueDepartmentFromCharge(Connection con, ChargeDTO billCharge) throws SQLException {
		String revenueDepartmentId = "";
		String visitId = "";
		if (billCharge.getVisitId() != null || !"".equals(billCharge.getVisitId())) {
			visitId = billCharge.getVisitId();
		} else if (billCharge.getBillNo() != null) {
			BillDAO billDAO = new BillDAO();
			Bill bill = billDAO.getBill((String)billCharge.getBillNo());
			if (bill != null) {
				visitId = (String)bill.getVisitId();
			}
		}
		if (!"".equals(visitId)) {
			BasicDynaBean visitDetails = VisitDetailsDAO.getVisitDetails(con, visitId);
			if (visitDetails != null) {
				revenueDepartmentId = (String) visitDetails.get("dept_name");
			}
		}

		return revenueDepartmentId;
	}
}
