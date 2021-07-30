package com.insta.hms.core.billing;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.taxation.GenericBillingTaxCalculator;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxCalculator;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.core.inventory.issues.PatientIssueReturnsService;
import com.insta.hms.core.inventory.issues.StockIssueDetailsService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.mdm.anaesthesia.AnaesthesiaTypeService;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.commoncharges.CommonChargesService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.diagnostics.DiagnosticTestService;
import com.insta.hms.mdm.dietary.DietaryService;
import com.insta.hms.mdm.drgcodesmaster.DrgCodesMasterService;
import com.insta.hms.mdm.equipment.EquipmentService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.operations.OperationsService;
import com.insta.hms.mdm.packages.PackagesService;
import com.insta.hms.mdm.services.ServicesService;
import com.insta.hms.mdm.theatre.TheatreService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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

@Component
public class BillHelper {
	
	Map<String, TaxCalculator> calculators = new HashMap<String, TaxCalculator>();
	
	@LazyAutowired
	private ServicesService serService;
	
	@LazyAutowired
	private DiagnosticTestService diagnosticService;
	
	@LazyAutowired
	private InsurancePlanService insurancePlanService;
	
	@LazyAutowired DrgCodesMasterService drgCodesMasterService;
	
	@LazyAutowired
	private ConsultationTypesService consultationTypeService;

	@LazyAutowired
	private BedTypeService bedTypeService;
	
	@LazyAutowired
	private PackagesService packageService;
	
	@LazyAutowired
	private CommonChargesService commonChargesService;
	
	@LazyAutowired
	private OperationsService operationService;
	
	@LazyAutowired
	private AnaesthesiaTypeService anaesthesiaTypeService;
	
	@LazyAutowired
	private TheatreService theatreService;
	
	@LazyAutowired
	private DietaryService dietaryService;
	
	@LazyAutowired
	private EquipmentService equipmentService;
	
	@LazyAutowired
	private BillChargeTaxService billChargeTaxService;
	
	@LazyAutowired
	private BillTaxCalculator billTaxCalculator;
	
	@LazyAutowired
	private RedisTemplate<String, Object> redisTemplate;
	
	@LazyAutowired
	private RegistrationService regService;

	@LazyAutowired
	private BillService billService;
	
	@Autowired
	public ChargeHeadsService chargeHeadService;
	
	@LazyAutowired
	private BillChargeService billChargeService;
	
	@LazyAutowired
	private StockIssueDetailsService stockIssueDetailsService;
	
	@LazyAutowired
	private PatientIssueReturnsService patientIssueReturnsService;
	
	public static final String PHIL_HEALTH_CONSTANT_VAL ="philhealth:philhealth";
	
	private Map<String, BasicDynaBean> chargeHeadMap = new HashMap<String, BasicDynaBean>();
	
	@Autowired
	public void setTaxCalculators(List<TaxCalculator> calculators) {
		for (TaxCalculator calculator : calculators) {
			if (calculator instanceof GenericBillingTaxCalculator) {
				String[] supportedGroups = ((GenericBillingTaxCalculator)calculator).getSupportedGroups();
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
	
	public boolean isNotNull(String value) {
		if(value != null && !value.trim().isEmpty() && !value.equals("null") && !value.equals("undefined")){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isNotNull(Object value) {
		if(value != null){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isNotNull(String[] value) {
		if(value != null && !value[0].trim().isEmpty() && !value[0].equals("null") && !value[0].equals("undefined")){
			return true;
		} else {
			return false;
		}
	}

	public List<BasicDynaBean> getItemSubgroupCodes(String actDescriptionId, String chargeGroup, String chargeHead, int consultationId, String opId) {
		
		List<BasicDynaBean> itemSubGroupCodes = new ArrayList<BasicDynaBean>();		
		if(chargeGroup.equals("SNP")){
			itemSubGroupCodes = serService.getServiceItemSubGroupTaxDetails(actDescriptionId);
		}else if(chargeGroup.equals("OPE")){
			if(chargeHead.equals("ANATOPE")){
				itemSubGroupCodes = anaesthesiaTypeService.getAnaesthesiaTypeSubGroupTaxDetails(actDescriptionId);				
			}else if (chargeHead.equals("TCOPE")){
				itemSubGroupCodes = theatreService.getTheatreItemSubGroupTaxDetails(actDescriptionId);	
			}else if(chargeHead.equals("EQOPE")){
				itemSubGroupCodes = equipmentService.getEquipmentItemSubGroupTaxDetails(actDescriptionId);
			}else{
				itemSubGroupCodes = operationService.getOperationItemSubGroupTaxDetails(opId);
			}
		}
		else if(chargeGroup.equals("DIA")){
			itemSubGroupCodes = diagnosticService.getDiagnosticsItemSubGroupTaxDetails(actDescriptionId);
		}
		else if(chargeGroup.equals("DOC")){
			itemSubGroupCodes = consultationTypeService.getConsultationTypeItemSubGroupTaxDetails(consultationId);
		}
		else if(chargeGroup.equals("BED") || chargeGroup.equals("ICU")){
			
			      boolean result = false;
            Pattern pattern = Pattern.compile("[0-9]+"); 
            pattern = Pattern.compile("\\d+"); 
            
            result = pattern.matcher(actDescriptionId).matches();
            if(result){
            	itemSubGroupCodes = bedTypeService.getBedItemSubGroupTaxDetails(actDescriptionId);
            }else{
            	itemSubGroupCodes = bedTypeService.getBedTypeSubGroupTaxDetails(actDescriptionId);
            }
		}
		else if(chargeGroup.equals("OTC")){
			if(chargeHead.equals("EQUOTC")){
				itemSubGroupCodes = equipmentService.getEquipmentItemSubGroupTaxDetails(actDescriptionId);
			}else{
				itemSubGroupCodes = commonChargesService.getCommonChargesItemSubGroupTaxDetails(actDescriptionId);
			}
		}
		else if(chargeGroup.equals("PKG")){
			itemSubGroupCodes = packageService.getPackageItemSubGroupTaxDetails(actDescriptionId);
		}
		else if(chargeGroup.equals("DIE")){
			itemSubGroupCodes = dietaryService.getDietaryItemSubGroupTaxDetails(actDescriptionId);
		}
		else if(chargeGroup.equals("DRG")){
		  itemSubGroupCodes = drgCodesMasterService.getDrgItemSubGroupTaxDetails(actDescriptionId);
		}
		return itemSubGroupCodes;
	}
	
//	public void getTaxDetails(Map<Integer, Object> taxInfo, List<BasicDynaBean> subGroupCodes, Map<Integer, Object> taxInfoMap) {
//		Iterator<BasicDynaBean> subGroupCodesIterator = subGroupCodes.iterator();
//		while(subGroupCodesIterator.hasNext()) {
//			BasicDynaBean subGroupCodesBean = subGroupCodesIterator.next();
//			String subGroupCode = (String)subGroupCodesBean.get("subgroup_code");
//			//String subGroupName = (String)subGroupCodesBean.get("item_subgroup_name");
//			Integer subGroupId = (Integer)subGroupCodesBean.get("item_subgroup_id");
//			if(taxInfo.get(subGroupId) != null){
//				taxInfoMap.put(subGroupId, taxInfo.get(subGroupId));
//			}
//		}
//	}
	
	public Map<Integer,Object> getTaxChargesMap(ItemTaxDetails itemTaxDetails,
			TaxContext taxContext, List<BasicDynaBean> subGroupCodes) {
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

	public Map<String,Object> calculateSponsorTaxes(ItemTaxDetails itemTaxDetails,
			TaxContext taxContext, List<BasicDynaBean> subGroupCodes) {
		Map<String, Object> sponsorTaxMap = new HashMap<String, Object>();
		Map<Integer, Object> taxChargesMap = getTaxChargesMap(itemTaxDetails, taxContext, subGroupCodes);
		sponsorTaxMap.put("sponsorAmount", itemTaxDetails.getAdjPrice());
		sponsorTaxMap.put("subGrpSponTaxDetailsMap", taxChargesMap);
		return sponsorTaxMap;
	}

	public List<BasicDynaBean> getItemSubgroupCodes(String chargeId) {
		// TODO Auto-generated method stub
		return billChargeTaxService.getItemSubgroupCodes(chargeId);
	}

	public Map<String, Object> getItemMapForInsuranceCategories(
			List<BasicDynaBean> newCharges) {	
	  Map<String, Object> itemMap = new HashMap<>();
	  for(BasicDynaBean bean :newCharges){
		List<Object> items = new ArrayList<>();
		
		String chargeHead = (String)bean.get("charge_head");
		String chargeGroup = (String)bean.get("charge_group");
		String keys = (chargeGroup != null) ? chargeGroup.concat("-").concat(chargeHead) : null;
		
		if(itemMap.containsKey(keys)) {
		  items = (List<Object>) itemMap.get(keys);
			if ("DOC".equals(chargeGroup)) {
			  items.add(bean.get("consultation_type_id"));
      } else if ("OPE".equals(chargeGroup) && !"ANATOPE".equals(chargeHead)) {
          items.add(bean.get("op_id"));
        } else if ("PKG".equals(chargeGroup)) {
           if (bean.get("package_id") == null && "PKGPKG".equals(chargeHead)) {
             items.add(Integer.parseInt((String)bean.get("act_description_id")));
           } else {
             items.add((Integer) bean.get("package_id"));
           }
        } else if("INVITE".equals(chargeHead)
            || "DIE".equals(chargeGroup) || "MED".equals(chargeGroup)){
          Integer itemId = Integer.parseInt((String)bean.get("act_description_id"));
          items.add(itemId);
        } else{
			  items.add(bean.get("act_description_id"));
			}
		}else{
			  if("DOC".equals(chargeGroup)) {
				  items.add(bean.get("consultation_type_id"));
        } else if ("OPE".equals(chargeGroup) && !"ANATOPE".equals(chargeHead)) {
          items.add(bean.get("op_id"));
        } else if ("PKG".equals(chargeGroup)) {
          if (bean.get("package_id") == null && "PKGPKG".equals(chargeHead)) {
             items.add(Integer.parseInt((String)bean.get("act_description_id")));
          } else {
             items.add((Integer) bean.get("package_id"));
          }
        } else if ("INVITE".equals(chargeHead)
            || "DIE".equals(chargeGroup) || "MED".equals(chargeGroup)){
          Integer itemId = Integer.parseInt((String)bean.get("act_description_id"));
          items.add(itemId);
        } else {
				  items.add(bean.get("act_description_id"));
			  }
			  itemMap.put(keys, items);
		}
	  }
	  return itemMap;	
	}

	public void getCatIdBasedOnPlanIds(List<BasicDynaBean> newCharges,
			Map<String, Object> itemMap, Set<Integer> planIds,
			Map<String, BasicDynaBean> details) {

		String visitType="o"; 
		if(!details.isEmpty()){
			visitType = (String)details.get("visit").get("visit_type");
		}
		
		for(Map.Entry<String, Object> itemMapEntry : itemMap.entrySet()){
			List<BasicDynaBean> itemBeanList= new ArrayList<BasicDynaBean>();
			itemBeanList = callRespectiveTypesToGetInsCat(planIds, visitType,
					itemMapEntry.getKey(), itemBeanList, (List<String>)itemMapEntry.getValue());
			setInsCatInTemplate(itemMapEntry, itemBeanList, planIds,visitType);
		}				
	}
	
	private List<BasicDynaBean> callRespectiveTypesToGetInsCat(
			Set<Integer> planIds, String visitType,
			String keys,
			List<BasicDynaBean> itemBeanList, List<String> listItemIds){
	  InsuranceCategoryHelper categoryHelper = new InsuranceCategoryHelper();
	  if (keys == null) {
	    return itemBeanList;
	  }
	  
	  String chargeGroup = keys.split("-")[0];
	  String chargeHead = keys.split("-")[1];
	  
		if(chargeGroup.equals("DIA")){ 
			itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_DIAG_CATEGORY_ID_BASED_ON_PLAN, 
			    listItemIds,planIds,visitType);
		}else if(chargeGroup.equals("PKG")) {
			  itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_PKG_CATEGORY_ID_BASED_ON_PLAN,
			          listItemIds, planIds, visitType);
		}else if(chargeGroup.equals("SNP")){
		  itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_SERVICE_CATEGORY_ID_BASED_ON_PLAN,
		      listItemIds,planIds,visitType);  
		}else if(chargeHead.equals("ANATOPE")){
		  itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_ANESTHESIA_CATEGORY_ID_BASED_ON_PLAN,
          listItemIds,planIds,visitType);
		}else if(chargeHead.equals("EQOPE")){
		  itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_EQUIPMENT_CATEGORY_ID_BASED_ON_PLAN,
          listItemIds,planIds,visitType);
		}else if(chargeGroup.equals("OPE")){
		    itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_OPERATION_CATEGORY_ID_BASED_ON_PLAN,
	          listItemIds,planIds,visitType);
		}else if(chargeGroup.equals("BED") || chargeGroup.equals("ICU")){
			boolean result = false;
            Pattern pattern = Pattern.compile("[0-9]+"); 
            pattern = Pattern.compile("\\d+"); 
                      
            result = pattern.matcher(listItemIds.get(0)).matches();
            if(result){
              List<String> bedTypeIds = bedTypeService.getBedTypeList(listItemIds);
            	itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_BED_NAME_CATEGORY_ID_BASED_ON_PLAN, 
            	    bedTypeIds, planIds, visitType);
            }else{
            	itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_BED_NAME_CATEGORY_ID_BASED_ON_PLAN, 
            	          listItemIds, planIds, visitType);
            }
		}else if(chargeGroup.equals("MED")){
		  itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_MED_CATEGORY_ID_BASED_ON_PLAN, 
          listItemIds, planIds, visitType);
		}else if(chargeGroup.equals("OTC")){
			if(chargeHead.equals("EQUOTC")){
				itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_EQUIPMENT_CATEGORY_ID_BASED_ON_PLAN, 
						listItemIds, planIds, visitType);
			}else{
				itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_OTHERCHARGE_CATEGORY_ID_BASED_ON_PLAN, 
						listItemIds, planIds, visitType);
			}
	  }else if(chargeGroup.equals("DOC")){
	      itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_DOCTOR_CATEGORY_ID_BASED_ON_PLAN, 
	          listItemIds, planIds, visitType);
      }else if(chargeGroup.equals("DIE")){
    	itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_DIETARY_CATEGORY_ID_BASED_ON_PLAN,
    			listItemIds, planIds, visitType);
      }else if(chargeGroup.equals("ITE")){
		itemBeanList = insurancePlanService.getCatIdBasedOnPlanIds(categoryHelper.GET_ITE_CATEGORY_ID_BASED_ON_PLAN, 
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
	public String getInsCatIdFromTemplate(BasicDynaBean charge, int planId,
			BasicDynaBean chargeClaimBean, String visitType) {
		String schema = RequestContext.getSchema();
		Object itemId = charge.get("act_description_id");
    String chargeHead = (String) charge.get("charge_head");
    String chargeGroup = (String) charge.get("charge_group");

    if ("OPE".equals(charge.get("charge_group")) && !"ANATOPE".equals(chargeHead)) {
      itemId = charge.get("op_id");
    }
		if ("DOC".equals(charge.get("charge_group"))) {
		  itemId = charge.get("consultation_type_id");
		}
		if ("PKG".equals(charge.get("charge_group"))) {
			if (charge.get("package_id") != null) {
				 itemId = (Object)charge.get("package_id");
			 }
		}
		
		String key = (chargeGroup != null) ? chargeGroup.concat("-").concat(chargeHead) : null;
		String redisKey = "sch:"+schema+";"+PHIL_HEALTH_CONSTANT_VAL+
				";"+"item:"+itemId+";"+"plan:"+planId+";"+"visittype:"+visitType+";"+
				"ch:"+key;
		
		String redisValue = (String) redisTemplate.opsForValue().get(redisKey);	

		if(null != redisValue && !redisValue.equals("")){
		  chargeClaimBean.set("insurance_category_id", Integer.parseInt(redisValue));
		}else{
		  chargeClaimBean.set("insurance_category_id", 0);
		}
		return redisValue;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void checkForInsCatInRedis(BasicDynaBean charge, int planId, String visitId) {
		String visitType = (String) regService.findByKey(visitId).get("visit_type");
		String redisValue = getInsCatIdFromTemplate(charge,planId,charge, visitType);
		
    if (null == redisValue || redisValue.isEmpty()) {
      Set planIds = new HashSet();
      planIds.add(planId);
      List itemIds = new ArrayList();

      String chargeHead = (String) charge.get("charge_head");
      String chargeGroup = (String) charge.get("charge_group");

      if ("DOC".equals(chargeGroup)) {
        itemIds.add(charge.get("consultation_type_id"));
      } else if ("OPE".equals(chargeGroup)) {
        if ("ANATOPE".equals(chargeHead)) {
          itemIds.add(charge.get("act_description_id"));
        } else {
          itemIds.add(charge.get("op_id"));
        }
      } else if ("PKG".equals(chargeGroup)) {
          if (charge.get("package_id") == null && "PKGPKG".equals(chargeHead)) {
            itemIds.add(Integer.parseInt((String) charge.get("act_description_id")));
          } else {
            itemIds.add((Integer) charge.get("package_id"));
          }
      } else if ("INVITE".equals(chargeHead) || "DIE".equals(chargeGroup)) {
        String actDescriptionId = (String) charge.get("act_description_id");
        if (actDescriptionId.matches("[0-9]+")) {
          Integer itemId = Integer.parseInt(actDescriptionId);
          itemIds.add(itemId);
        }
      } else {
        itemIds.add(charge.get("act_description_id"));
      }
      List<BasicDynaBean> itemBeanList = new ArrayList<>();

      String keys = (chargeGroup != null) ? chargeGroup.concat("-").concat(chargeHead) : null;
      if (!itemIds.isEmpty()) {
        itemBeanList = callRespectiveTypesToGetInsCat(planIds, visitType, keys, itemBeanList,
            itemIds);
      }

      if (null != itemBeanList && !itemBeanList.isEmpty()) {
        for (BasicDynaBean itemBean : itemBeanList) {
          charge.set("insurance_category_id", itemBean.get("primary_insurance_category_id"));
        }
      } else {
        charge.set("insurance_category_id", 0);
        if ("REG".equals(chargeGroup)) {
          Boolean isGeneralCategoryExistsForRegCharges = checkIsGeneralCategoryExistsForRegCharges(
              visitId, visitType, planId);
          if (Boolean.TRUE.equals(isGeneralCategoryExistsForRegCharges)) {
            charge.set("insurance_category_id", -1);
          }
        }
        if ("DRG".equals(chargeGroup)) {
          charge.set("insurance_category_id", -2);
        }
      }
    } else {
      charge.set("insurance_category_id", Integer.parseInt(redisValue));
    }
	}

	public void saveBillChargeBillingGroup(Map<String,Object> billCharge) {
		String billingGroupId = "";
		if (billCharge.get("charge_head_id") != null 
		    && !(((String)billCharge.get("charge_head_id")).isEmpty()) ) {
			if(null != billCharge && null != billCharge.get("billing_group_id")){
				billingGroupId =  Integer.toString((int) billCharge.get("billing_group_id"));
			}
			Object itemId = billCharge.get("act_description_id");
			if (billCharge.get("charge_group") != null
			    && StringUtils.isNotBlank(billCharge.get("charge_group").toString())) {
  			switch (billCharge.get("charge_group").toString()) {
  			  case "OPE":
  		      itemId = billCharge.get("op_id");
  			    break;
  			  case "PKG":
				  if (billCharge.get("package_id") == null && "PKGPKG".equals(billCharge.get("charge_head_id"))) {
					itemId = billCharge.get("act_description_id");
				  } else {
					itemId = billCharge.get("package_id");
				  }
  			    break;
  			  case "DOC":
  		      itemId = billCharge.get("consultation_type_id");
  		      break;
  			  default:
  			    itemId = billCharge.get("act_description_id");
  			    break;
  			}
			}
			String redisKey = String.format("schema:%s;user:%s;ch:%s;cg:%s;item%s",
					RequestContext.getSchema(), RequestContext.getUserName(), billCharge.get("charge_head_id"),
					billCharge.get("charge_group"),itemId);
			redisTemplate.opsForValue().set(redisKey,billingGroupId);
			redisTemplate.expire(redisKey, 24,TimeUnit.HOURS);
		}
	}

	public void setBillChargeBillingGroup(BasicDynaBean billCharge) {
		Integer billingGroupId = null;
		if (billCharge.get("billing_group_id") == null) {
			if (billCharge.get("charge_head") != null || !"".equals(billCharge.get("charge_head"))) {
				Object itemId = (Object)billCharge.get("act_description_id");
				if(billCharge.get("charge_group").equals("OPE")){
					itemId = (Object)billCharge.get("op_id");
				}
				if(billCharge.get("charge_group").equals("PKG")){
					 if (billCharge.get("package_id") != null) {
						 itemId = (Object)billCharge.get("package_id");
					}
				}
				if(billCharge.get("charge_group").equals("DOC")){
				  itemId = (Object)billCharge.get("consultation_type_id");
				}
				String redisKey = String.format("schema:%s;user:%s;ch:%s;cg:%s;item%s",
						RequestContext.getSchema(), RequestContext.getUserName(), billCharge.get("charge_head"),
						billCharge.get("charge_group"),itemId);
				String redisValue = (String) redisTemplate.opsForValue().get(redisKey);
				if(null != redisValue && !redisValue.equals("")){
					billingGroupId = Integer.parseInt(redisValue);
				}
			}
		} else {
			billingGroupId = (Integer) billCharge.get("billing_group_id");
		}
		billCharge.set("billing_group_id", billingGroupId);
	}

	public String getRevenueDepartmentFromCharge(BasicDynaBean billCharge) {
		String revenueDepartmentId = "";
		if (billCharge.get("bill_no") != null) {
			BasicDynaBean bill = billService.getBill((String)billCharge.get("bill_no"));
			if (null != bill.get("visit_id")) {
				BasicDynaBean visitDetails = regService.getVisitDetails((String)bill.get("visit_id"));
				if (visitDetails != null) {
					revenueDepartmentId = (String) visitDetails.get("dept_name");
				}
			}
		}

		return revenueDepartmentId;
	}

  private Boolean checkIsGeneralCategoryExistsForRegCharges(String visitId, String visitType, int planId) {
    return insurancePlanService.checkIsGeneralCategoryExistsForRegCharges(visitId, planId, visitType);
  }	
  
  /**
   * Check sale items for ins cat in redis.
   *
   * @param charge the charge
   * @param saleItem the sale item
   * @param planId the plan id
   * @param visitId the visit id
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void checkSaleItemsForInsCatInRedis(BasicDynaBean charge, BasicDynaBean saleItem,
      int planId, String visitId) {
    String visitType = (String) regService.findByKey(visitId).get("visit_type");
    charge.set("act_description_id", saleItem.get("medicine_id").toString());
    String redisValue = getInsCatIdFromTemplate(charge, planId, charge, visitType);
    if (null == redisValue || redisValue.isEmpty()) {
      Set planIds = new HashSet();
      planIds.add(planId);
      String chargeHead = (String) charge.get("charge_head");
      String chargeGroup = (String) charge.get("charge_group");
      List itemIds = new ArrayList();
      itemIds.add((Integer)saleItem.get("medicine_id"));
      List<BasicDynaBean> itemBeanList = new ArrayList<>();
      String keys = chargeGroup.concat("-").concat(chargeHead);
      itemBeanList = callRespectiveTypesToGetInsCat(planIds, visitType, keys, itemBeanList,
          itemIds);
      for (BasicDynaBean itemBean : itemBeanList) {
        saleItem.set("insurance_category_id",
            (Integer) itemBean.get("primary_insurance_category_id"));
      }
    } else {
      saleItem.set("insurance_category_id", Integer.parseInt(redisValue));
    }
  }

  public void resetInventoryCharges(String visitId) {
    List<BasicDynaBean> charges = billChargeService.getVisitBillCharges(visitId);

    for (BasicDynaBean charge : charges) {
      if (charge.get("charge_head").equals(BillChargeService.CH_INVENTORY_ITEM)) {
        BigDecimal unitDiscount = BigDecimal.ZERO;
        if (((BigDecimal) charge.get("act_quantity")).add((BigDecimal) charge.get("return_qty"))
            .compareTo(BigDecimal.ZERO) != 0) {
          unitDiscount = ((BigDecimal) charge.get("discount"))
              .divide(((BigDecimal) charge.get("act_quantity"))
                  .add((BigDecimal) charge.get("return_qty")), 3, BigDecimal.ROUND_HALF_DOWN);
        }
        BigDecimal returnDiscount = unitDiscount
            .multiply(((BigDecimal) charge.get("return_qty")).negate());
        charge.set("discount", (((BigDecimal) charge.get("discount")).add(returnDiscount)));
        charge.set("amount", (((BigDecimal) charge.get("amount")).subtract(returnDiscount)));
        charge.set("return_qty", (BigDecimal.ZERO));
        charge.set("return_tax_amt", (BigDecimal.ZERO));
        charge.set("return_amt", (BigDecimal.ZERO));
        charge.set("return_original_tax_amt", (BigDecimal.ZERO));

        stockIssueDetailsService.resetReturnQuantity((String) charge.get("charge_id"));
      }
    }
    billChargeService.updateChargeAmounts(charges);
  }
  
  public void replayInventoryReturns(String visitId) {
    
    List<BasicDynaBean> returnCharges = patientIssueReturnsService.getIssueReturnCharges(visitId);
    for(BasicDynaBean returnCharge : returnCharges){
      BigDecimal redQty = BigDecimal.ZERO;
      BigDecimal remQty = ((BigDecimal)returnCharge.get("act_quantity")).negate();
      List<BasicDynaBean> issues = patientIssueReturnsService.getVisitItemIssues(
          (String) returnCharge.get("visit_id"),
          Integer.parseInt((String) returnCharge.get("medicine_id")),
          (Integer) returnCharge.get("item_batch_id"), (Integer) returnCharge.get("dept_to"),
          (String) returnCharge.get("charge_id"));

      BigDecimal taxAmtForReturn = BigDecimal.ZERO;
      BigDecimal originalTaxAmtForReturn = BigDecimal.ZERO;
      for(BasicDynaBean issue : issues){
        
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
            .divide(((BigDecimal) issue.get("act_quantity"))
                ,3, BigDecimal.ROUND_HALF_UP).multiply(redQty);
        taxAmtForReturn = taxAmtForReturn.add(taxAmt);
        BigDecimal originalTaxAmt = ((BigDecimal) issue.get("original_tax_amt"))
            .divide(((BigDecimal) issue.get("act_quantity"))
                ,3, BigDecimal.ROUND_HALF_UP).multiply(redQty);
        originalTaxAmtForReturn = originalTaxAmtForReturn.add(originalTaxAmt);
        BasicDynaBean issueBean = stockIssueDetailsService
            .getIssueItemCharge(issue.get("item_issue_no").toString());
        
        
        BigDecimal issueRetAmount =(rate.multiply(redQty)).subtract(discount.multiply(redQty));

        
        List<BasicDynaBean> updateChargeList = new ArrayList<>();
        patientIssueReturnsService.setIssueItemsForReturns(redQty, issueRetAmount, BigDecimal.ZERO,
            updateChargeList, issueBean);
        
        for (BasicDynaBean iss : updateChargeList) {
          iss.set("return_insurance_claim_amt",BigDecimal.ZERO);
          iss.set("return_tax_amt", taxAmt.negate());
          iss.set("return_original_tax_amt", originalTaxAmt.negate());
        }
        
        patientIssueReturnsService.updateChargesForReturns(updateChargeList);        
        BasicDynaBean stockDetailsBean = stockIssueDetailsService.findByKey((Integer)issue.get("item_issue_no"));
        stockDetailsBean.set("return_qty", ((BigDecimal) stockDetailsBean.get("return_qty")).add(redQty));
        stockIssueDetailsService.update(stockDetailsBean, Collections.singletonMap("item_issue_no", issue.get("item_issue_no")));
      }
      
      BasicDynaBean returnChargeBean = billChargeService.getCharge((String)returnCharge.get("charge_id"));
      returnChargeBean.set("tax_amt",taxAmtForReturn.negate());
      returnChargeBean.set("original_tax_amt", originalTaxAmtForReturn.negate());
      billChargeService.update(returnChargeBean, Collections.singletonMap("charge_id", returnChargeBean.get("charge_id")));
    }
  }
}
