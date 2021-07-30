/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.insurance.AdvanceInsuranceCalculator;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.DynaPackage.DynaPackageDAO;
import com.insta.hms.master.DynaPackageCategory.DynaPackageCategoryMasterDAO;
import com.insta.hms.master.DynaPackageRules.DynaPackageRulesMasterDAO;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;
import com.insta.hms.stores.SalesClaimDetailsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * @author lakshmi
 *
 */
public class DynaPackageProcessor {

	private Logger log = LoggerFactory.getLogger(DynaPackageProcessor.class);

	DynaPackageRulesMasterDAO ruledao  = new DynaPackageRulesMasterDAO();
	DynaPackageCategoryMasterDAO catdao = new DynaPackageCategoryMasterDAO();
	ChargeHeadsDAO chHeadDAO = new ChargeHeadsDAO();
	GenericDAO chargedao = new GenericDAO("bill_charge");
	GenericDAO billdao = new GenericDAO("bill");
	GenericDAO saleitemdao = new GenericDAO("store_sales_details");
	GenericDAO saledao = new GenericDAO("store_sales_main");
	GenericDAO storeitemdao = new GenericDAO("store_item_details");
	GenericDAO issuedao = new GenericDAO("stock_issue_main");
	GenericDAO issueitemdao = new GenericDAO("stock_issue_details");
	GenericDAO bednamesdao = new GenericDAO("bed_names");
	final GenericDAO issueRefChargesDAO = new GenericDAO("patient_issue_returns_issue_charge_details");
	SalesClaimDetailsDAO saleClaimDetailsDao = new SalesClaimDetailsDAO();
	VisitDetailsDAO visitDetailsDao = new VisitDetailsDAO();

	ServiceSubGroupDAO subgrpdao = new ServiceSubGroupDAO();
	BillBO billBO = new BillBO();

	public DynaPackageProcessor() {}

	private static Map<Integer, BigDecimal> pkgLimitMap = new HashMap<Integer, BigDecimal>();
	private static Map<Integer, BigDecimal> pkgQuantityMap = new HashMap<Integer, BigDecimal>();


	// Map of each category and amount, considered if included.
	private void createDynaPkgLimitsMap(List<BasicDynaBean> dynaPkgDetails) {
		for (BasicDynaBean bean : dynaPkgDetails) {
			if (((String)bean.get("limit_type")).equals("A") && ((String)bean.get("pkg_included")).equals("Y"))
				pkgLimitMap.put((Integer)bean.get("dyna_pkg_cat_id"), (BigDecimal)bean.get("amount_limit"));
		}
	}

	// Map of each category and qty, considered if included.
	private void createDynaPkgQuantityMap(List<BasicDynaBean> dynaPkgDetails) {
		for (BasicDynaBean bean : dynaPkgDetails) {
			if (((String)bean.get("limit_type")).equals("Q") && ((String)bean.get("pkg_included")).equals("Y"))
				pkgQuantityMap.put((Integer)bean.get("dyna_pkg_cat_id"), (BigDecimal)bean.get("qty_limit"));
		}
	}

	public BasicDynaBean getPackageCategoryBean(BasicDynaBean processbean, List<BasicDynaBean> dynaPkgDetails,
			List<BasicDynaBean> rules, String centerid) throws SQLException {
		int servSubGrp = (Integer)processbean.get("service_sub_group_id");
		BasicDynaBean subgrpbean = subgrpdao.findByKey("service_sub_group_id", servSubGrp);
		String serviceSubGrp = "*", serviceGrp = "*";
		if (subgrpbean != null) {
			serviceSubGrp = ((Integer)subgrpbean.get("service_sub_group_id")).toString();
			serviceGrp = ((Integer)subgrpbean.get("service_group_id")).toString();
		}

		String chargeId = (String)processbean.get("charge_id");
		String chargeGroup = (String)processbean.get("charge_group");
		String chargeHead = (String)processbean.get("charge_head");
		String actDescId = processbean.get("act_description_id") != null ? (String)processbean.get("act_description_id") : null;
		String actDesc = processbean.get("act_description") != null ? (String)processbean.get("act_description") : null;

		log.debug("Find charge rule: charge group: " + chargeGroup + "; charge head: " + chargeHead
				+ "; service group: " + serviceGrp + "; service sub group: " + serviceSubGrp
				+ "; activity id: " + actDescId	+ "; activity: " + actDesc);

		BasicDynaBean chargeRule = null;
		BasicDynaBean pkgCategory = null;

		for (BasicDynaBean rule : rules) {

			if (!isChargeGroupMatch(rule, chargeGroup)) {
				continue;
			}
			if (!isChargeHeadMatch(rule, chargeHead)) {
				continue;
			}
			if (!isChargeServiceGroupMatch(rule, serviceGrp)) {
				continue;
			}
			if (!isChargeServiceSubGroupMatch(rule, serviceSubGrp)) {
				continue;
			}
			if (!isChargeActivityTypeMatch(rule, actDesc)) {
				continue;
			}
			if (!isChargeActivityIdMatch(rule, actDescId)) {
				continue;
			}
			if (!isCenterMatch(rule, centerid)) {
				continue;
			}
			chargeRule = rule;
			break;
		}

		if (chargeRule != null) {

			log.debug("Found rule with priority: "+chargeRule.get("priority")+" for charge id: "+(String)processbean.get("charge_id"));

			int chargeCategory = (Integer)chargeRule.get("dyna_pkg_cat_id");

			for (BasicDynaBean pkgCatBean : dynaPkgDetails) {
				if ((Integer)pkgCatBean.get("dyna_pkg_cat_id") == chargeCategory) {
					pkgCategory = pkgCatBean;
					break;
				}
			}
		}else {
			log.warn("No rule found for: charge id: " + chargeId
				+ "; charge group: " + chargeGroup + "; charge head: " + chargeHead
				+ "; service group: " + serviceGrp + "; service sub group: " + serviceSubGrp
				+ "; activity id: " + actDescId	+ "; activity: " + actDesc);
		}
		return pkgCategory;
	}

	private boolean isChargeGroupMatch(BasicDynaBean rule, String chargeGroup) {
		String categoryGrp = (String) rule.get("chargegroup_id");
		if ("*".equals(categoryGrp) || categoryGrp.equals(chargeGroup)) {
			return true;
		}
		return false;
	}

	private boolean isChargeHeadMatch(BasicDynaBean rule, String chargeHead) {
		String categoryHead = (String) rule.get("chargehead_id");
		if ("*".equals(categoryHead) || categoryHead.equals(chargeHead)) {
			return true;
		}
		return false;
	}

	private boolean isChargeServiceGroupMatch(BasicDynaBean rule, String serviceGroup) {
		String categoryServGrp = (String) rule.get("service_group_id");
		if ("*".equals(categoryServGrp) || categoryServGrp.equals(serviceGroup)) {
			return true;
		}
		return false;
	}

	private boolean isChargeServiceSubGroupMatch(BasicDynaBean rule, String serviceSubGroup) {
		String categoryServSubGrp = (String) rule.get("service_sub_group_id");
		if ("*".equals(categoryServSubGrp) || categoryServSubGrp.equals(serviceSubGroup)) {
			return true;
		}
		return false;
	}

	private boolean isChargeActivityTypeMatch(BasicDynaBean rule, String activityType) {
		String categoryActType = (String) rule.get("activity_type");
		categoryActType = categoryActType.startsWith("_ALL_") ? "*" : categoryActType;
		if ("*".equals(categoryActType) || categoryActType.equals(activityType)) {
			return true;
		}
		return false;
	}

	private boolean isChargeActivityIdMatch(BasicDynaBean rule, String activityId) {
		String categoryActId = (String) rule.get("activity_id");
		if ("*".equals(categoryActId) || categoryActId.equals(activityId)) {
			return true;
		}
		return false;
	}

	private boolean isCenterMatch(BasicDynaBean rule, String centerId) {
		String categoryCenter = (String) rule.get("center_id");
		if ("*".equals(categoryCenter) || categoryCenter.equals(centerId)) {
			return true;
		}
		return false;
	}

	/**
	 * This process method is used when user needs dyna package to be processed after bill is saved.
	 * @param con
	 * @param billNo
	 * @throws SQLException
	 * @throws IOException
	 */
	/* Process bill charges if bill has dyna package and calculate package margin */
	public String process(String billNo, boolean reProcess) throws SQLException, IOException {

		DynaPackageDAO dynaDAO = new DynaPackageDAO();
		Connection con = null;
		boolean success = false;
		String err = null;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BasicDynaBean billbean = BillDAO.getBillBean(con, billNo);
			int dynaPkgId = (Integer)billbean.get("dyna_package_id");

			if (dynaPkgId == 0)
				return null;

			Map<String ,List<BasicDynaBean>> issueReturnrefernces = ConversionUtils.listBeanToMapListBean(
				    issueRefChargesDAO.findAllByKey("patient_id",(String)billbean.get("visit_id")), "issue_charge_id");
			List<BasicDynaBean> returnreferenceCharges = new ArrayList<BasicDynaBean>();
			String visitId = (String)billbean.get("visit_id");
			BasicDynaBean visitbean = visitDetailsDao.findByKey(con, "patient_id", visitId);
			String bedType = (String)visitbean.get("bed_type");
			Integer visitCenterId = (Integer)visitbean.get("center_id");

			Timestamp currentTimestamp = DateUtil.getCurrentTimestamp();

			// For Bed charges update user will be auto_update as session is null.
			HttpSession session	= RequestContext.getSession();
			String userid = session == null ? "auto_update" : (String)session.getAttribute("userId");
			Integer center = session == null ? visitCenterId : (Integer)session.getAttribute("centerId");
			userid = userid != null ? userid.toString() : "auto_update";
			String centerid = center != null ? center.toString() : "*";

			// Check if bill has and excluded bed charges (split bed charges) using old way of
			// dyna package process.
			List<String> excludedBedCharges = getExcludedBedCharges(billNo);

			if (excludedBedCharges != null && excludedBedCharges.size() > 0) {

				// Delete excluded bed charges.
				deleteExcludedBedCharges(con, excludedBedCharges);

				OrderBO order = new OrderBO();

				err = order.setBillInfo(con, visitId, billNo, false, userid);
				if (err != null) {
					if (err.startsWith("Patient visit is not active"))
						err = "Patient visit is not active, cannot process dyna package." +" Bill no : "+billNo;
					return err;
				}

				// Recalculate bed charges for old bills if reopened.l
				err = order.recalculateBedCharges(con, visitId);
				if (err != null) {
					err = err +" Bill No : "+billNo;
					return err;
				}
			}

			Map<String, String> filterMap = new HashMap<String, String>();
			filterMap.put("bill_no", billNo);
			filterMap.put("status", "A");

			List<BasicDynaBean> charges = chargedao.listAll(con, null, filterMap, "posted_date");
			Map<String,BasicDynaBean> chargesMap = ConversionUtils.listBeanToMapBean(charges,"charge_id");

			String ratePlan = (String)billbean.get("bill_rate_plan_id");
			BigDecimal packageAmount = (BigDecimal)billbean.get("dyna_package_charge");
			boolean isTpa = (Boolean)billbean.get("is_tpa");
			BasicDynaBean chargeHeadBean = chHeadDAO.findByKey("chargehead_id", ChargeDTO.CH_DYNA_PACKAGE_MARGIN);
			String claimable = (String)chargeHeadBean.get("insurance_payable");

			boolean marginClaimable = isTpa && claimable.equals("Y");

			BasicDynaBean dynabean = dynaDAO.getDynaPackageDetailsBean(dynaPkgId, ratePlan);

			// Bill dyna package (active/inactive) i.e existing or new package details.
			List<BasicDynaBean> dynaPkgDetails = dynaDAO.getBillDynaPackageCharges(ratePlan, bedType, dynaPkgId);
			List<BasicDynaBean> rules = ruledao.listAll(null, "priority");

			if (dynaPkgDetails == null) {
				return "Invalid dyna package id: "+dynaPkgId+" for bill no: "+billNo;
			}
			
			// Initialize the category limits
			createDynaPkgLimitsMap(dynaPkgDetails);

			// Initialize the category quantities
			createDynaPkgQuantityMap(dynaPkgDetails);

			/** All hospitals have Day care beds as separate bed type with separate kind of charges.
				So, if DAY CARE bed type needs to be excluded or included with separate limit then
				we need to define separate category with limit.

				Otherwise, day care beds are processed as normal beds.

			 	All bed charges viz. BBED,NCBED,DDBED,PCBED,BICU,NCICU,DDICU,PCICU,BYBED
			 	are consider for dyna package processing.

			 	LTAX -- First, we will check if this charge head has any category.
			 	If category exists then the rule is applied.
			 	If category does not exists then LTAX is processed along with normal bed charges.
			 */

			int[] planIds = new PatientInsurancePlanDAO().getPlanIds(visitId);
			AdvanceInsuranceCalculator insCalculator = new AdvanceInsuranceCalculator();
			for (BasicDynaBean charge : charges) {

//			  if(charge.get("charge_head").equals("MARPKG"))
//          continue;
				String status = (String)charge.get("status");
				String chargeGroup = (String)charge.get("charge_group");
				String chargeHead = (String)charge.get("charge_head");

				BasicDynaBean chrgbean = new ChargeHeadsDAO().findByKey("chargehead_id",chargeHead);

				boolean isInsurancePayable = chrgbean.get("insurance_payable") != null &&
					((String)chrgbean.get("insurance_payable")).equals("Y") ;

				if (status.equals("X") || chargeGroup.equals(ChargeDTO.CG_DISCOUNTS) ||
						chargeHead.equals(ChargeDTO.CH_CLAIM_SERVICE_TAX)) {

					// Exclude cancelled charges, discounts & claim service tax from package
					// i.e amount and qty included is zero.
					charge.set("amount_included", new BigDecimal(0));
					charge.set("qty_included", new BigDecimal(0));
					charge.set("dyna_package_excluded",null);

					continue;
				}

				if (((String)charge.get("package_finalized")).equals("Y")) {
					continue;
				}

				BasicDynaBean processbean = getProcessingChargeBean();
				processbean.set("margin_claimable", marginClaimable);
				setProcessingAttributes(charge, processbean);
				setActivityBedType(processbean);

				String chargeRef = charge.get("charge_ref") == null ? null :(String)charge.get("charge_ref");

				// BYBED, LTAX and STAX are associated charges of Bed Charge and Service charges.
				// So, we will check if these have any separate categories first.
				// Do not process if main charge category and associated charge categories are same
				// and has limit as Quantity.
				// i.e these will be included along with main charges while processing.

				if (chargeHead.equals(ChargeDTO.CH_BYBED)) {
					// Check if bystander bed can be included with bed charges.
					boolean includeBystanderBed = includeAssociateWithMainCharge(charges, processbean, dynaPkgDetails, rules, centerid);

					if (includeBystanderBed)
						continue;

				}else if (chargeHead.equals(ChargeDTO.CH_LUXURY_TAX)) {
					// Check if Luxury tax can be included with bed charges.
					boolean includeLuxuryTax = includeAssociateWithMainCharge(charges, processbean, dynaPkgDetails, rules, centerid);

					if (includeLuxuryTax)
						continue;

				}else if (chargeHead.equals(ChargeDTO.CH_SERVICE_TAX)) {
					// Check if Service tax can be included with Service charge.
					boolean includeServiceTax = includeAssociateWithMainCharge(charges, processbean, dynaPkgDetails, rules, centerid);

					if (includeServiceTax)
						continue;
				}

				boolean hasActivity = (Boolean)charge.get("hasactivity");
				if (hasActivity && (chargeHead.equals(ChargeDTO.CH_PHARMACY_CREDIT_MEDICINE)
						|| chargeHead.equals(ChargeDTO.CH_PHARMACY_CREDIT_RETURNS)) ) {

					// Process pharmacy items (amounts with returns) according to category and rules.
					processPharmacyItems(con, processbean, dynaPkgDetails, rules, centerid);

					// Copy the amount,qty included to charge
					charge.set("amount_included", (BigDecimal)processbean.get("amount_included"));
					charge.set("qty_included", (BigDecimal)processbean.get("qty_included"));
					charge.set("dyna_package_excluded", null);


				}else if(chargeHead.equals(ChargeDTO.CH_INVENTORY_RETURNS)){
					continue;

				}else if (hasActivity && (chargeHead.equals(ChargeDTO.CH_INVENTORY_ITEM))) {

					// Process inventory items (amounts with returns) according to category and rules.
					BigDecimal amt = (BigDecimal)processbean.get("amount");
					BigDecimal retAmt = (BigDecimal)processbean.get("return_amt");
					BigDecimal qty = (BigDecimal)processbean.get("act_quantity");
					BigDecimal retQty = (BigDecimal)processbean.get("return_qty");

					//amt = amt.add(retAmt);
					//qty = qty.add(retQty);

					processbean.set("amount", amt);
					processbean.set("act_quantity", qty);
					
          BigDecimal claimAmount = BigDecimal.ZERO;

          if (planIds != null && isTpa) { // HMS-19951
            claimAmount = insCalculator.calculateClaim(amt, (BigDecimal) charge.get("discount"),
                billNo, planIds[0], (Boolean) charge.get("first_of_category"), "",
                (Integer) charge.get("insurance_category_id"), isInsurancePayable);
          } else if (isTpa) { // HMS-19951 :If the bill is insurance, then amount is set in
                              // insurance_claim_amount otherwise insurance_claim_amount is zero.
            claimAmount = amt;
          }

          // When we process dyna pkg again, already calulated claim amt for partial inclusion will
          // be taken into consideration,
          // to avoid this we are again setting the claimamount before any re-processing.
          processbean.set("insurance_claim_amount", claimAmount);

					processInventoryItems(con, processbean, dynaPkgDetails, rules, centerid);

					// Copy the amount,qty included to charge
					charge.set("amount_included", (BigDecimal)processbean.get("amount_included"));
					charge.set("qty_included", (BigDecimal)processbean.get("qty_included"));
					charge.set("dyna_package_excluded", null);
					
					//include reference returns
					List<BasicDynaBean> returns = issueReturnrefernces.get(charge.get("charge_id"));
					BigDecimal agnstAmt = BigDecimal.ZERO;
					if ((Boolean)processbean.get("margin_claimable"))
            agnstAmt = (BigDecimal)processbean.get("insurance_claim_amount");
          else
            agnstAmt = (BigDecimal)processbean.get("amount");
					
					BigDecimal percentOfIssuesIncluded  = BigDecimal.ZERO;

          if (agnstAmt.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal issueIncludedAmt = ((BigDecimal)processbean.get("amount_included")).divide(agnstAmt, BigDecimal.ROUND_HALF_UP);
            percentOfIssuesIncluded = issueIncludedAmt.multiply(new BigDecimal(100));
          } 
					
					if ( returns != null ) {
  					for(BasicDynaBean returnedItems : returns ){
  					  
  					      BasicDynaBean returnBean= chargesMap.get(returnedItems.get("return_charge_id"));
  					      if ( returnBean == null ) {//returned to another bill
  					        continue;
  					      }
  					      returnBean.set("amount_included", percentOfIssuesIncluded.divide(new BigDecimal(100)).multiply((BigDecimal)returnBean.get("amount")) );
  					      returnBean.set("dyna_package_excluded", null);
  					      returnreferenceCharges.add(returnBean);
  					  
  					}
					}

				}else {

					// Get the category to which the charge belongs and process according to
					// the limit type (Amount/Quantity/Unlimited)
					BasicDynaBean pkgCatBean = getPackageCategoryBean(processbean, dynaPkgDetails, rules, centerid);
					if (pkgCatBean != null) {
						int categoryId = (Integer)pkgCatBean.get("dyna_pkg_cat_id");

						processbean.set("package_category", categoryId);

						BigDecimal claimAmount = BigDecimal.ZERO;

						if(planIds!=null && isTpa){ //HMS-19951 
							claimAmount = insCalculator.calculateClaim((BigDecimal)charge.get("amount"), (BigDecimal)charge.get("discount"),
									billNo, planIds[0],
									(Boolean)charge.get("first_of_category"), "", (Integer)charge.get("insurance_category_id"), isInsurancePayable);
						}else if(isTpa){ //HMS-19951 :If the bill is insurance, then amount is set in insurance_claim_amount otherwise insurance_claim_amount is zero.
							claimAmount = (BigDecimal)charge.get("amount");
						}

						//When we process dyna pkg again, already calulated claim amt for partial inclusion will be taken into consideration,
						// to avoid this we are again setting the claimamount before any re-processing.
						processbean.set("insurance_claim_amount", claimAmount);

						BigDecimal secClaimAmt = BigDecimal.ZERO;

						if(null != planIds && planIds.length > 1){
							BigDecimal remainingAmt = ((BigDecimal)charge.get("amount")).subtract(claimAmount);
							secClaimAmt = insCalculator.calculateClaim(remainingAmt, BigDecimal.ZERO,
									billNo, planIds[1],
									(Boolean)charge.get("first_of_category"), "", (Integer)charge.get("insurance_category_id"), isInsurancePayable);

							processbean.set("insurance_claim_amount", claimAmount.add(secClaimAmt));
						}


						if (((String)pkgCatBean.get("limit_type")).equals("Q")) {

							if (!chargeHead.equals(ChargeDTO.CH_BYBED)
									&& !chargeHead.equals(ChargeDTO.CH_LUXURY_TAX)
										&& !chargeHead.equals(ChargeDTO.CH_SERVICE_TAX)
											&& chargeRef != null && !chargeRef.trim().equals("")) {
								// Limit type: quantity, do not process charges if charge has charge-ref
								continue;
							}

							// Set the included qty & amount if charge is eligible to be included.
							setProcessChargeIncludedQuantity(processbean);

							// Copy the amount,qty included to charge
							charge.set("amount_included", (BigDecimal)processbean.get("amount_included"));
							charge.set("qty_included", (BigDecimal)processbean.get("qty_included"));
							charge.set("dyna_package_excluded", null);
							// Check for patially Included Item
							if(dynabean.get("excluded_amt_claimable").equals("N") && isTpa &&
									(((BigDecimal) charge.get("amount_included")).compareTo(BigDecimal.ZERO)!=0
									&& charge.get("amount_included")!=processbean.get("amount"))){

								BigDecimal priClaimAmt=(BigDecimal)processbean.get("amount_included");

								BigDecimal amtIncluded = (BigDecimal)processbean.get("amount_included");
								BigDecimal totalClaimAmt = claimAmount.add(secClaimAmt);

								if(null != planIds && planIds.length > 1 && totalClaimAmt.compareTo(amtIncluded) != 0){
									if(!(secClaimAmt.compareTo(BigDecimal.ZERO) == 0)){

										BigDecimal excludedAmt = totalClaimAmt.subtract(amtIncluded);
										BigDecimal amtDeducted = BigDecimal.ZERO;
										if(secClaimAmt.compareTo(excludedAmt) > 0){
											secClaimAmt = secClaimAmt.subtract(excludedAmt);
											priClaimAmt=claimAmount;
										}
										else {
											amtDeducted = secClaimAmt;
											secClaimAmt = BigDecimal.ZERO;
											priClaimAmt = claimAmount.subtract(excludedAmt.subtract(amtDeducted));
										}
									}
								}

								if(null != planIds && planIds.length > 1 && totalClaimAmt.compareTo(amtIncluded) == 0) priClaimAmt = claimAmount;

								UpdateInsAmtForItems(con, charge.getMap(), billNo, priClaimAmt,secClaimAmt,planIds,visitId);
								//For TPA Only,Corporate and National Sponsers
								charge.set("insurance_claim_amount", (BigDecimal)processbean.get("amount_included"));
							}else{
								UpdateInsAmtForItems(con,charge.getMap(),billNo,claimAmount, secClaimAmt, planIds, visitId);
								//For TPA Only,Corporate and National Sponsers
								charge.set("insurance_claim_amount", claimAmount.add(secClaimAmt));
							}
							copyChargeRefsIncudedQuantity(charge, charges, marginClaimable, processbean, dynaPkgDetails, rules, centerid);
							setDynaPkgQuantity(processbean);

						}else if (((String)pkgCatBean.get("limit_type")).equals("A")) {

							//When we process dyna pkg again, already calulated claim amt for partial inclusion will be taken into consideration,
							// to avoid this we are again setting the claimamount before any re-processing.
							processbean.set("insurance_claim_amount", claimAmount.add(secClaimAmt));
							// Set the included amount if charge is eligible to be included.
							setProcessChargeIncludedAmount(processbean);

							// Copy the amount,qty included to charge
							charge.set("amount_included", (BigDecimal)processbean.get("amount_included"));
							charge.set("qty_included", (BigDecimal)processbean.get("qty_included"));
							charge.set("dyna_package_excluded", null);
							//Check for patially Included Item
							if(dynabean.get("excluded_amt_claimable").equals("N") && isTpa &&
									(((BigDecimal) charge.get("amount_included")).compareTo(BigDecimal.ZERO)!=0
									&& charge.get("amount_included")!=processbean.get("amount"))){

								BigDecimal priClaimAmt=(BigDecimal)processbean.get("amount_included");

								BigDecimal amtIncluded = (BigDecimal)processbean.get("amount_included");
								BigDecimal totalClaimAmt = claimAmount.add(secClaimAmt);

								if(null != planIds && planIds.length > 1 && totalClaimAmt.compareTo(amtIncluded) != 0){
									if(!(secClaimAmt.compareTo(BigDecimal.ZERO) == 0)){
										BigDecimal excludedAmt = totalClaimAmt.subtract(amtIncluded);
										BigDecimal amtDeducted = BigDecimal.ZERO;
										if(secClaimAmt.compareTo(excludedAmt) > 0){
											secClaimAmt = secClaimAmt.subtract(excludedAmt);
											priClaimAmt=claimAmount;
										}
										else {
											amtDeducted = secClaimAmt;
											secClaimAmt = BigDecimal.ZERO;
											priClaimAmt = claimAmount.subtract(excludedAmt.subtract(amtDeducted));
										}
									}
								}

								if(null != planIds && planIds.length > 1 && totalClaimAmt.compareTo(amtIncluded) == 0) priClaimAmt = claimAmount;

								UpdateInsAmtForItems(con, charge.getMap(), billNo,priClaimAmt,secClaimAmt,planIds,visitId);
								//	For TPA Only,Corporate and National Sponsers
								charge.set("insurance_claim_amount", (BigDecimal)processbean.get("amount_included"));
							}else{
								UpdateInsAmtForItems(con,charge.getMap(),billNo,claimAmount, secClaimAmt, planIds, visitId);
								//For TPA Only,Corporate and National Sponsers
								charge.set("insurance_claim_amount", claimAmount.add(secClaimAmt));
							}

							setDynaPkgLimits(processbean);

						}else if (((String)pkgCatBean.get("limit_type")).equals("U")) {

							setProcessChargeUnlimitedIncludedQuantity(processbean, dynaPkgDetails, categoryId);

							// Copy the amount,qty included to charge
							charge.set("amount_included", (BigDecimal)processbean.get("amount_included"));
							charge.set("qty_included", (BigDecimal)processbean.get("qty_included"));
							charge.set("dyna_package_excluded", null);
						}

					}else {
						charge.set("amount_included", new BigDecimal(0));
						charge.set("qty_included", new BigDecimal(0));
						charge.set("dyna_package_excluded", null);
					}
				}
			}

			for (BasicDynaBean returnCharge : returnreferenceCharges) {
			  returnCharge.set("mod_time", currentTimestamp);
			  returnCharge.set("username", userid);
        chargedao.updateWithName(con, returnCharge.getMap(), "charge_id");
			}
			// Update charge included amount, qty and user details.
			for (BasicDynaBean charge : charges) {
				charge.set("mod_time", currentTimestamp);
				charge.set("username", userid);
				chargedao.updateWithName(con, charge.getMap(), "charge_id");

				String chargeHead = (String)charge.get("charge_head");

				BasicDynaBean chrgbean = new ChargeHeadsDAO().findByKey("chargehead_id",chargeHead);

				boolean isInsurancePayable = chrgbean.get("insurance_payable") != null &&
					((String)chrgbean.get("insurance_payable")).equals("Y") ;

				if(!chargeHead.equals("MARPKG")){
					if(dynabean.get("excluded_amt_claimable").equals("N") &&
							(((BigDecimal) charge.get("amount_included")).compareTo(BigDecimal.ZERO)==0) && isTpa){
						UpdateInsAmtForItems(con,charge.getMap(),billNo,BigDecimal.ZERO);
					}
					else if(!(((BigDecimal) charge.get("amount_included")).compareTo(BigDecimal.ZERO)!=0
							&& charge.get("amount_included")!=charge.get("amount"))){
						if(!((String)charge.get("charge_head")).equals("INVRET")
								&& !((String)charge.get("package_finalized")).equals("Y")
								&& !((String) charge.get("charge_head")).equals("PHCRET")
								&& !((String) charge.get("charge_head")).equals("PHCMED")){
						//For complete Inclusion
							BigDecimal amount = ((BigDecimal)charge.get("amount")).add((BigDecimal)charge.get("return_amt"));

							BigDecimal claimAmount = BigDecimal.ZERO;
							if(null != planIds && isTpa){ // HMS-19951
								claimAmount = insCalculator.calculateClaim(amount, (BigDecimal)charge.get("discount"),
										billNo, planIds[0],
										(Boolean)charge.get("first_of_category"), "", (Integer)charge.get("insurance_category_id"), isInsurancePayable);
							}else if(isTpa){ // HMS-19951 :If the bill is insurance, then amount is set in insurance_claim_amount otherwise insurance_claim_amount is zero.
								claimAmount = amount;
							}

							BigDecimal secClaimAmt = BigDecimal.ZERO;

							if(null != planIds && planIds.length > 1){
								BigDecimal remainingAmt = amount.subtract(claimAmount);
								secClaimAmt = insCalculator.calculateClaim(remainingAmt, BigDecimal.ZERO,
										billNo, planIds[1],
										(Boolean)charge.get("first_of_category"), "", (Integer)charge.get("insurance_category_id"), isInsurancePayable);
							}
							UpdateInsAmtForItems(con,charge.getMap(),billNo,claimAmount,secClaimAmt,planIds,visitId);
						}
					}
				}
			}


			log.debug("Processed bill charges for bill no: "+billNo);

			// Calculate package margin and update.
			updatePackageMarginAmount(con, charges, packageAmount, marginClaimable, userid);

			updateChargeExcluded(con, billNo);

			billbean = billdao.findByKey(con, "bill_no", billNo);
			billbean.set("dyna_pkg_processed", "Y");
			billbean.set("mod_time", currentTimestamp);
			billbean.set("username", userid);
			billdao.updateWithName(con, billbean.getMap(), "bill_no");

			success = true;

			log.info("Dyna package processing success for bill no: "+billNo);

		}catch (Exception e) {
			success = false;
			err = "Error while processing Dyna Package. Bill No: "+billNo;
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			log.error(stacktrace);
			return err;

		}finally {
			DataBaseUtil.commitClose(con, success);

			if (!reProcess && success && billNo != null && !billNo.equals(""))
				BillDAO.resetTotalsOrReProcess(billNo, false);
		}
		return null;
	}

	private void UpdateInsAmtForItems(Connection con, Map charge, String billNo, BigDecimal insurance_claim_amt) throws SQLException, IOException {

		BasicDynaBean chargeBean=chargedao.getBean();
		chargeBean.set("insurance_claim_amount", insurance_claim_amt);

		BillChargeClaimDAO dao= new BillChargeClaimDAO();
		BasicDynaBean bean=dao.getBean();
		bean.set("insurance_claim_amt", insurance_claim_amt);

		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("charge_id", charge.get("charge_id"));
		keys.put("bill_no", billNo);

		chargedao.update(con, chargeBean.getMap(), keys);
		dao.update(con, bean.getMap(), keys);
	}


	private void UpdateInsAmtForItems(Connection con, Map charge, String billNo, BigDecimal priClaimAmt,
			BigDecimal secClaimAmt, int[] planIds, String visitId) throws SQLException, IOException {

		BasicDynaBean chargeBean=chargedao.getBean();
		String chargeHead = (String)charge.get("charge_head");
		if(!chargeHead.equals("INVRET")){
			chargeBean.set("insurance_claim_amount", priClaimAmt.add(secClaimAmt));

			Map<String, Object> keys = new HashMap<String, Object>();
			keys.put("charge_id", charge.get("charge_id"));
			keys.put("bill_no", billNo);
			chargedao.update(con, chargeBean.getMap(), keys);

			if(null != planIds){
				for(int i=0; i<planIds.length; i++){
					BillChargeClaimDAO dao= new BillChargeClaimDAO();
					BasicDynaBean bean=dao.getBean();
					String claimId = dao.getClaimId(con, planIds[i], billNo, visitId);
					keys.put("claim_id", claimId);
					bean.set("insurance_claim_amt", i==0?priClaimAmt:secClaimAmt);
					dao.update(con, bean.getMap(), keys);
				}
			}
		}
	}

	private boolean includeAssociateWithMainCharge(List<BasicDynaBean> charges, BasicDynaBean processbean,
			List<BasicDynaBean> dynaPkgDetails, List<BasicDynaBean> rules, String centerid) throws SQLException {
		int associateCategory = 0, mainChargeCategory = 0, associateAsOtherCategory = 0;
		boolean include = false;

		String chargeId = (String)processbean.get("charge_id");
		BasicDynaBean chargeBean = getChargeBean(charges, chargeId);
		String chargeRef = null;
    if (null != chargeBean) {
      chargeRef = chargeBean.get("charge_ref") == null ? null : (String) chargeBean
          .get("charge_ref");
    }
		BasicDynaBean mainChargeBean = null;

		if (chargeRef != null && !chargeRef.trim().equals("")) {
			mainChargeBean = getChargeBean(charges, chargeRef);
			if(null != mainChargeBean) {
			  setProcessingAttributes(mainChargeBean, processbean); // Main charge
			}
			setActivityBedType(processbean);

			BasicDynaBean mainChargeCatBean = getPackageCategoryBean(processbean, dynaPkgDetails, rules, centerid);
			if (mainChargeCatBean != null) mainChargeCategory = (Integer)mainChargeCatBean.get("dyna_pkg_cat_id");
		}

		setProcessingAttributes(chargeBean, processbean); // Associated charge
		setActivityBedType(processbean);

		BasicDynaBean assocChargeCatBean = getPackageCategoryBean(processbean, dynaPkgDetails, rules, centerid);
		if (assocChargeCatBean != null) associateAsOtherCategory = (Integer)assocChargeCatBean.get("dyna_pkg_cat_id");

		if (mainChargeBean != null)
			processbean.set("charge_group", (String)mainChargeBean.get("charge_group"));

		assocChargeCatBean = getPackageCategoryBean(processbean, dynaPkgDetails, rules, centerid);
		if (assocChargeCatBean != null) associateCategory = (Integer)assocChargeCatBean.get("dyna_pkg_cat_id");

		include = (associateAsOtherCategory == 0 && (associateCategory != 0 && associateCategory == mainChargeCategory
								&& ((String)assocChargeCatBean.get("limit_type")).equals("Q")));

		processbean.set("charge_group", (String)chargeBean.get("charge_group"));
		return include;
	}

	private BasicDynaBean getChargeBean(List<BasicDynaBean> charges, String chargeId) {
		for (BasicDynaBean charge : charges) {
			String charge_id = (String)charge.get("charge_id");
			if (charge_id.equals(chargeId))
				return charge;
		}
		return null;
	}

	private void setProcessingAttributes(BasicDynaBean charge, BasicDynaBean processbean) {
		processbean.set("charge_id", (String)charge.get("charge_id"));
		processbean.set("charge_group", (String)charge.get("charge_group"));
		processbean.set("charge_head", (String)charge.get("charge_head"));
		processbean.set("act_description_id", charge.get("act_description_id"));
		processbean.set("act_description", charge.get("act_description"));
		processbean.set("service_sub_group_id", (Integer)charge.get("service_sub_group_id"));
		processbean.set("amount", (BigDecimal)charge.get("amount"));
		processbean.set("act_quantity", (BigDecimal)charge.get("act_quantity"));
		processbean.set("insurance_claim_amount", (BigDecimal)charge.get("insurance_claim_amount"));
		processbean.set("return_amt", (BigDecimal)charge.get("return_amt"));
		processbean.set("return_qty", (BigDecimal)charge.get("return_qty"));
		processbean.set("return_insurance_claim_amt", (BigDecimal)charge.get("return_insurance_claim_amt"));
		processbean.set("amount_included", BigDecimal.ZERO);
		processbean.set("qty_included", BigDecimal.ZERO);
	}

	private void setActivityBedType(BasicDynaBean processbean) throws SQLException {
		String chargeGroup = (String)processbean.get("charge_group");
		String chargeHead = (String)processbean.get("charge_head");
		if (chargeGroup.equals(ChargeDTO.CG_BED)
				|| chargeGroup.equals(ChargeDTO.CG_ICU)
					|| chargeHead.equals(ChargeDTO.CH_LUXURY_TAX)) {

			String actDescId = processbean.get("act_description_id") != null ? (String)processbean.get("act_description_id") : null;
			if (actDescId != null && QueryBuilder.isInteger(actDescId)) {
				BasicDynaBean bedBean = bednamesdao.findByKey("bed_id", new Integer(actDescId));
				if (bedBean != null) {
					processbean.set("act_description_id", (String)bedBean.get("bed_type"));
				}
			}
		}
	}

	private boolean processInventoryItems(Connection con, BasicDynaBean processbean,
			List<BasicDynaBean> dynaPkgDetails, List<BasicDynaBean> rules, String centerid) throws IOException, SQLException {
		String chargeId = (String)processbean.get("charge_id");
		String chargeHead = (String)processbean.get("charge_head");

		// TODO: Need to change this as we move charge id to issues tables.
		BillActivityCharge bac = new BillActivityChargeDAO(con).getActivity(chargeId);
		if (bac == null || !bac.getActivityCode().equals("PHI"))
			return true;

		String medicineId = (String)processbean.get("act_description_id");
		int medicine_id = new Integer(medicineId);
		BasicDynaBean storeitembean = storeitemdao.findByKey("medicine_id", medicine_id);
		int med_category_id = (Integer)storeitembean.get("med_category_id");
		String medCategory = new Integer(med_category_id).toString();

		processbean.set("act_description_id", medicineId);
		processbean.set("act_description", medCategory);
		processbean.set("amount", ((BigDecimal)processbean.get("amount")));
		processbean.set("act_quantity", ((BigDecimal)processbean.get("act_quantity")));
		processbean.set("insurance_claim_amount", ((BigDecimal)processbean.get("insurance_claim_amount")));

		// Set package included qty & amount
		setItemIncludedAmountAndQty(processbean, dynaPkgDetails, rules, centerid);

		return true;
	}

	private BasicDynaBean getProcessingChargeBean() {
		DynaBeanBuilder builder = new DynaBeanBuilder();
		builder.add("charge_id");
		builder.add("charge_group");
		builder.add("charge_head");
		builder.add("act_description_id");
		builder.add("act_description");
		builder.add("service_sub_group_id", Integer.class);;
		builder.add("amount", BigDecimal.class);
		builder.add("act_quantity", BigDecimal.class);
		builder.add("insurance_claim_amount", BigDecimal.class);
		builder.add("return_amt", BigDecimal.class);
		builder.add("return_qty", BigDecimal.class);
		builder.add("return_insurance_claim_amt", BigDecimal.class);

		builder.add("margin_claimable", Boolean.class);
		builder.add("package_category", Integer.class);

		builder.add("amount_included", BigDecimal.class);
		builder.add("qty_included", BigDecimal.class);
		builder.add("tax_amt", BigDecimal.class);

		return builder.build();
	}

	private void setItemIncludedAmountAndQty(BasicDynaBean processbean,
			List<BasicDynaBean> dynaPkgDetails, List<BasicDynaBean> rules, String centerid) throws IOException, SQLException {
		// Get the category to which the item belongs and process according to
		// the limit type (Amount/Quantity/Unlimited)
		BasicDynaBean pkgCatBean = getPackageCategoryBean(processbean, dynaPkgDetails, rules, centerid);
		if (pkgCatBean != null) {
			int categoryId = (Integer)pkgCatBean.get("dyna_pkg_cat_id");

			processbean.set("package_category", categoryId);

			if (((String)pkgCatBean.get("limit_type")).equals("Q")) {

				// Set the included qty & amount if item is eligible to be included.
				setProcessChargeIncludedQuantity(processbean);
				setDynaPkgQuantity(processbean);

			}else if (((String)pkgCatBean.get("limit_type")).equals("A")) {
				// Set the included amount if item is eligible to be included.
				setProcessChargeIncludedAmount(processbean);
				setDynaPkgLimits(processbean);

			}else if (((String)pkgCatBean.get("limit_type")).equals("U")) {

				setProcessChargeUnlimitedIncludedQuantity(processbean, dynaPkgDetails, categoryId);
			}

		}else {
			processbean.set("amount_included", new BigDecimal(0));
			processbean.set("qty_included", new BigDecimal(0));
		}
	}

	private boolean processPharmacyItems(Connection con, BasicDynaBean processbean,
			List<BasicDynaBean> dynaPkgDetails, List<BasicDynaBean> rules, String centerid) throws IOException, SQLException {
		String chargeId = (String)processbean.get("charge_id");

		BasicDynaBean sale = saledao.findByKey("charge_id", chargeId);
		if (sale == null)
			return true;

		String saleId = (String)sale.get("sale_id");
		BigDecimal amountIncluded = BigDecimal.ZERO;

		String saletype = (String)sale.get("type");
		List<BasicDynaBean> saleItems = saleitemdao.listAll(null, "sale_id", saleId, "sale_item_id");

		if (saletype.equals("S")) {

			for (BasicDynaBean saleitem: saleItems) {

				if (((String)saleitem.get("package_finalized")).equals("Y")) {
					amountIncluded = amountIncluded.add((BigDecimal)saleitem.get("amount_included"));
					continue;
				}

				int medicine_id = (Integer)saleitem.get("medicine_id");
				BasicDynaBean storeitembean = storeitemdao.findByKey("medicine_id", medicine_id);
				int med_category_id = (Integer)storeitembean.get("med_category_id");
				int service_sub_group_id = (Integer)storeitembean.get("service_sub_group_id");
				String medCategory = new Integer(med_category_id).toString();

				String medicineId = new Integer(medicine_id).toString();
				
				BasicDynaBean saleClaimDetailsBean = (BasicDynaBean) saleClaimDetailsDao.getTaxAmt((Integer)saleitem.get("sale_item_id"));

				processbean.set("act_description_id", medicineId);
				processbean.set("act_description", medCategory);
				processbean.set("service_sub_group_id", service_sub_group_id);
				/**
				 * //TAXATION 
				 * Sales screen amt is inclusive of taxes
				 */
				processbean.set("amount", ((BigDecimal)saleitem.get("amount")).add((BigDecimal)saleitem.get("return_amt")));
				processbean.set("act_quantity", ((BigDecimal)saleitem.get("quantity")).add((BigDecimal)saleitem.get("return_qty")));
				/**
				 * //TAXATION 
				 *  Pkg without taxes consider amt +tax for calculations
				 *  Pkg with taxes consider only amt for calculations
				 * */
				if(null != saleClaimDetailsBean && null != saleClaimDetailsBean.get("insurance_claim_amt") 
						&& null != saleClaimDetailsBean.get("tax_amt")){
					processbean.set("insurance_claim_amount", ((BigDecimal)saleClaimDetailsBean.get("insurance_claim_amt")).
				    add((BigDecimal)saleClaimDetailsBean.get("tax_amt")));
				}else{
					processbean.set("insurance_claim_amount", BigDecimal.ZERO);
				}	

				processbean.set("amount_included", BigDecimal.ZERO);
				processbean.set("qty_included", BigDecimal.ZERO);

				// Set package included qty & amount
				setItemIncludedAmountAndQty(processbean, dynaPkgDetails, rules, centerid);

				// Copy the amount included to sale item
				saleitem.set("amount_included", (BigDecimal)processbean.get("amount_included"));
				saleitem.set("qty_included", (BigDecimal)processbean.get("qty_included"));

				amountIncluded = amountIncluded.add((BigDecimal)processbean.get("amount_included"));
				saleitemdao.updateWithName(con, saleitem.getMap(), "sale_item_id");
			}

		}else {
			// Pharmacy returns amount & qty included is zero.
			for (BasicDynaBean saleitem: saleItems) {
				saleitem.set("amount_included", BigDecimal.ZERO);
				saleitem.set("qty_included", BigDecimal.ZERO);
				saleitemdao.updateWithName(con, saleitem.getMap(), "sale_item_id");
			}
		}

		processbean.set("amount_included", amountIncluded);
		processbean.set("qty_included", BigDecimal.ZERO);

		return true;
	}

	private static final String UPDATE_CHARGE_EXCLUDED =
		" UPDATE bill_charge SET charge_excluded = 'N' WHERE bill_no = ? ";

	private void updateChargeExcluded(Connection con, String billNo) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_CHARGE_EXCLUDED);){
		  ps.setString(1, billNo);
	    ps.executeUpdate();
		}
	}

	private static final String GET_EXCLUDED_BED_CHARGES =
		" SELECT charge_id FROM bill_charge " +
		" WHERE bill_no = ? AND status != 'X' AND charge_group IN ('BED','ICU') " +
		" AND charge_excluded = 'Y' AND hasactivity = false ";

	private List<String> getExcludedBedCharges(String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List<String> excludedBedCharges = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_EXCLUDED_BED_CHARGES);
			ps.setString(1, billNo);
			excludedBedCharges = DataBaseUtil.queryToStringList(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return excludedBedCharges;
	}


	private static final String DELETE_BED_CHARGE =
		" DELETE FROM bill_charge WHERE charge_id  = ? ";

	private boolean deleteExcludedBedCharges(Connection con, List<String> excludedBedCharges) throws SQLException {
		boolean success = true;

		if (excludedBedCharges != null && excludedBedCharges.size() > 0) {
			try(PreparedStatement ps = con.prepareStatement(DELETE_BED_CHARGE);){
			  for (String chargeId : excludedBedCharges) {
	        ps.setString(1, chargeId);
	        ps.addBatch();
	      }
	      ps.executeBatch();
			}
		}
		return success;
	}

	public void updatePackageMarginAmount(Connection con, List<BasicDynaBean> charges,
			BigDecimal packageAmount, boolean marginClaimable, String userid) throws IOException, SQLException {
		BigDecimal pkgMargin = BigDecimal.ZERO;
		BigDecimal pkgIncluded = BigDecimal.ZERO;
		BasicDynaBean pkgMarginChargeBean = null;

		for (BasicDynaBean charge : charges) {
			if (((String)charge.get("charge_head")).equals("MARPKG"))
				pkgMarginChargeBean = charge;
			else
				pkgIncluded = pkgIncluded.add((BigDecimal)charge.get("amount_included"));
		}

		pkgMargin = packageAmount.subtract(pkgIncluded);
		if(null != pkgMarginChargeBean){
		  pkgMarginChargeBean.set("status", "A");
	    pkgMarginChargeBean.set("act_quantity", BigDecimal.ONE);
	    pkgMarginChargeBean.set("act_rate", pkgMargin);
	    pkgMarginChargeBean.set("amount", pkgMargin);
	    pkgMarginChargeBean.set("amount_included", pkgMargin);

	    if (marginClaimable)
	      pkgMarginChargeBean.set("insurance_claim_amount", pkgMargin);
	    else
	      pkgMarginChargeBean.set("insurance_claim_amount", BigDecimal.ZERO);

	    pkgMarginChargeBean.set("mod_time", DateUtil.getCurrentTimestamp());
	    pkgMarginChargeBean.set("username", userid);
	    chargedao.updateWithName(con, pkgMarginChargeBean.getMap(), "charge_id");
	    if(isBillChargeClaimExists(con,pkgMarginChargeBean)){
	      new BillChargeClaimDAO().updatepackageMarginInBillChgClaim(con, pkgMarginChargeBean);
	    }
		}
	}

	private boolean isBillChargeClaimExists(Connection con, BasicDynaBean pkgMarginChargeBean) throws SQLException{

		boolean isBillChgClaimExists = false;
		String chargeId = (String)pkgMarginChargeBean.get("charge_id");
		BasicDynaBean billChgClaimBean = new GenericDAO("bill_charge_claim").findByKey(con,"charge_id",chargeId);
		if(billChgClaimBean != null)
			isBillChgClaimExists = true;

		return isBillChgClaimExists;
	}

	private boolean isUnlimitedCategoryIncluded(List<BasicDynaBean> dynaPkgDetails, int categoryId) {

		for (BasicDynaBean category : dynaPkgDetails) {
			int category_id = (Integer)category.get("dyna_pkg_cat_id");
			if (category_id == categoryId)
				return (((String)category.get("pkg_included")).equals("Y"));
		}
		return false;
	}

	private void setProcessChargeUnlimitedIncludedQuantity(BasicDynaBean processbean, List<BasicDynaBean> dynaPkgDetails, int categoryId) {
		BigDecimal qty_included = BigDecimal.ZERO;
		BigDecimal amount_included = BigDecimal.ZERO;

		boolean included = isUnlimitedCategoryIncluded(dynaPkgDetails, categoryId);

		if (included) {
			qty_included = (BigDecimal)processbean.get("act_quantity");

			if ((Boolean)processbean.get("margin_claimable"))
				amount_included = (BigDecimal)processbean.get("insurance_claim_amount");
			else
				amount_included = (BigDecimal)processbean.get("amount");
		}

		processbean.set("qty_included", qty_included);
		processbean.set("amount_included", amount_included);
	}

	public void setProcessChargeIncludedAmount(BasicDynaBean processbean) throws SQLException {

		// Consider amount included as zero, then set included amount based on eligibility.
		processbean.set("amount_included", new BigDecimal(0));

		// Consider qty included as zero, then set included qty based on eligibility.
		processbean.set("qty_included", new BigDecimal(0));

		BigDecimal catLimit = pkgLimitMap.get((Integer)processbean.get("package_category"));

		BigDecimal includedAmount = getProcessChargeIncludedAmount(catLimit, processbean);
		processbean.set("amount_included", includedAmount);
	}

	public BigDecimal getProcessChargeIncludedAmount(BigDecimal limit,	BasicDynaBean processbean) throws SQLException {
		BigDecimal includedAmt = BigDecimal.ZERO;

		if (limit != null) {
			BigDecimal amount = BigDecimal.ZERO;
			if ((Boolean)processbean.get("margin_claimable"))
				amount = (BigDecimal)processbean.get("insurance_claim_amount");
			else
				amount = (BigDecimal)processbean.get("amount");
			includedAmt = limit.min(amount); // Mimimum of package limit and charge amount
		}
		return includedAmt;
	}

	public void setDynaPkgLimits(BasicDynaBean processbean) {

		BigDecimal amount = (BigDecimal)processbean.get("amount_included");

		BigDecimal limit = pkgLimitMap.get(processbean.get("package_category"));
		if (limit != null)
			pkgLimitMap.put((Integer)processbean.get("package_category"), limit.subtract(amount));
	}

	public void setProcessChargeIncludedQuantity(BasicDynaBean processbean) throws SQLException {

		// Consider amount included as zero, then set included amount based on eligibility.
		processbean.set("amount_included", new BigDecimal(0));

		// Consider qty included as zero, then set included qty based on eligibility.
		processbean.set("qty_included", new BigDecimal(0));

		BigDecimal packageQty = pkgQuantityMap.get((Integer)processbean.get("package_category"));

		BigDecimal includedQty = getProcessChargeIncludedQuantity(packageQty, processbean);

		BigDecimal actQty = (BigDecimal)processbean.get("act_quantity");
		BigDecimal amount = BigDecimal.ZERO;
		BigDecimal amount_included = BigDecimal.ZERO;

		if ((Boolean)processbean.get("margin_claimable"))
			amount = (BigDecimal)processbean.get("insurance_claim_amount");
		else
			amount = (BigDecimal)processbean.get("amount");

		if (includedQty.compareTo(BigDecimal.ZERO) != 0) {
			amount_included = amount.subtract(((actQty.subtract(includedQty)).multiply(amount)).divide(actQty,BigDecimal.ROUND_HALF_UP));
			processbean.set("qty_included", includedQty);
			processbean.set("amount_included", amount_included);
		}
	}

	public BigDecimal getProcessChargeIncludedQuantity(BigDecimal packageQty, BasicDynaBean charge) throws SQLException {
		BigDecimal includedQty = BigDecimal.ZERO;

		if (packageQty != null) {
			BigDecimal qty = (BigDecimal)charge.get("act_quantity");
			includedQty = packageQty.min(qty); // Mimimum of package quantity and charge quantity
		}
		return includedQty;
	}

	public void setDynaPkgQuantity(BasicDynaBean processbean) {

		BigDecimal qty = processbean.get("qty_included") == null
						? BigDecimal.ZERO : (BigDecimal)processbean.get("qty_included");

		BigDecimal packageQty = pkgQuantityMap.get((Integer)processbean.get("package_category"));
		if (packageQty != null)
			pkgQuantityMap.put((Integer)processbean.get("package_category"), packageQty.subtract(qty));
	}

	// Copy the included quantity from main charge to all charge refs.
	private void copyChargeRefsIncudedQuantity(BasicDynaBean maincharge,
			List<BasicDynaBean> charges, boolean marginClaimable, BasicDynaBean processbean,
			List<BasicDynaBean> dynaPkgDetails, List<BasicDynaBean> rules, String centerid) throws SQLException {
		for (BasicDynaBean charge : charges) {
			String chargeHead = (String)charge.get("charge_head");
			String mainChargeId = (String)maincharge.get("charge_id");
			String chargeRef = (String)charge.get("charge_ref");
			BigDecimal includedQty = (BigDecimal)maincharge.get("qty_included");
			BigDecimal mainChargeQty = (BigDecimal)maincharge.get("act_quantity");

			/*  Qty is copied to all charge refs if they are included with main charge.
			 *  Even though BYBED, LTAX and STAX have charge refs, these are considered as spl. cases.
			 *  User can edit qmount/qty in bill screen.
			 */

			if (mainChargeId.equals(chargeRef)) {

				if (chargeHead.equals(ChargeDTO.CH_BYBED)) {
					// Check if bystander bed can be included with bed charges.
					BasicDynaBean bybedprocessbean = getProcessingChargeBean();
					bybedprocessbean.set("margin_claimable", marginClaimable);
					setProcessingAttributes(charge, bybedprocessbean);
					setActivityBedType(bybedprocessbean);
					boolean includeBystanderBed = includeAssociateWithMainCharge(charges, bybedprocessbean, dynaPkgDetails, rules, centerid);

					if (!includeBystanderBed)
						continue;

				}else if (chargeHead.equals(ChargeDTO.CH_LUXURY_TAX)) {
					// Check if Luxury tax can be included with bed charges.
					BasicDynaBean ltaxprocessbean = getProcessingChargeBean();
					ltaxprocessbean.set("margin_claimable", marginClaimable);
					setProcessingAttributes(charge, ltaxprocessbean);
					setActivityBedType(ltaxprocessbean);
					boolean includeLuxuryTax = includeAssociateWithMainCharge(charges, ltaxprocessbean, dynaPkgDetails, rules, centerid);

					if (!includeLuxuryTax)
						continue;

				}else if (chargeHead.equals(ChargeDTO.CH_SERVICE_TAX)) {
					// Check if Service tax can be included with Service charge.
					BasicDynaBean staxprocessbean = getProcessingChargeBean();
					staxprocessbean.set("margin_claimable", marginClaimable);
					setProcessingAttributes(charge, staxprocessbean);
					setActivityBedType(staxprocessbean);
					boolean includeServiceTax = includeAssociateWithMainCharge(charges, staxprocessbean, dynaPkgDetails, rules, centerid);

					if (!includeServiceTax)
						continue;
				}

				BigDecimal actQty = (BigDecimal)charge.get("act_quantity");
				BigDecimal amount = BigDecimal.ZERO;
				BigDecimal amount_included = BigDecimal.ZERO;

				if (marginClaimable)
					amount = (BigDecimal)charge.get("insurance_claim_amount");
				else
					amount = (BigDecimal)charge.get("amount");

				if (includedQty.compareTo(BigDecimal.ZERO) != 0) {

					// Included luxury tax or service tax, included amount is calculated based on main charge included qty.
					if (chargeHead.equals(ChargeDTO.CH_LUXURY_TAX) || chargeHead.equals(ChargeDTO.CH_SERVICE_TAX)) {

						amount_included = ConversionUtils.divide(amount.multiply(includedQty), mainChargeQty);
						if (amount.compareTo(amount_included) == 0)
							charge.set("qty_included", actQty);
						else
							charge.set("qty_included", BigDecimal.ZERO);
						charge.set("amount_included", amount_included);
					}else {
						amount_included = ConversionUtils.divide(amount.multiply(includedQty), actQty);
						charge.set("qty_included", includedQty);
						charge.set("amount_included", amount_included);
					}
				}
			}
		}
	}

	/**
	 * This reset method used in BillBO when bill is saved from UI.
	 * @param con
	 * @param billNo
	 * @param existingPkgId
	 * @param newPkgId
	 * @throws SQLException
	 * @throws IOException
	 */
	public boolean reset(Connection con, String billNo, List updateBillChargeList) throws SQLException, IOException, ParseException {

		// Check if dyna package is changed.
		if (!isDynaPackageChanged(con, billNo))
			return true;

		BasicDynaBean billbean = billdao.findByKey(con, "bill_no", billNo);
		int newPkgId = (Integer)billbean.get("dyna_package_id");
		String visitId = (String)billbean.get("visit_id");

		HttpSession session	= RequestContext.getSession();
		String userid = (String)session.getAttribute("userId");
		Timestamp currentTimestamp = DateUtil.getCurrentTimestamp();
		AdvanceInsuranceCalculator insCalculator = new AdvanceInsuranceCalculator();
		List<BasicDynaBean> planListBean = new GenericDAO("bill_claim").listAll(null, "bill_no", billNo, "priority");
		int planIds[] = null;
		planIds = null!= planListBean && planListBean.size() > 0 ? new int[planListBean.size()] : null;

		for(int i=0; i<planListBean.size(); i++){
					planIds[i] = (Integer)planListBean.get(i).get("plan_id");
		}
		// Reset package process if dyna package added/edited/removed.
		List<BasicDynaBean> charges = chargedao.listAll(con, null, "bill_no", billNo, "posted_date");
		for (BasicDynaBean charge : charges) {

			String chargeId = (String)charge.get("charge_id");
			String chargeHead = (String)charge.get("charge_head");
			boolean hasActivity = (Boolean)charge.get("hasactivity");
			// Reset pharmacy items if bill has pharmacy charges.
			if (hasActivity && (chargeHead.equals(ChargeDTO.CH_PHARMACY_CREDIT_MEDICINE)
					|| chargeHead.equals(ChargeDTO.CH_PHARMACY_CREDIT_RETURNS)) ) {

				resetPharmacyItems(con, chargeId);
			}

			//Reset claim amount when bill is a tpa bill
			if((Boolean)billbean.get("is_tpa")){

				String insPayable = new ChargeDAO(con).getChargeInsurancePayable(chargeHead);
				if(insPayable.equals("Y")){
					if(!(chargeHead.equals(ChargeDTO.CH_PHARMACY_CREDIT_MEDICINE) || chargeHead.equals(ChargeDTO.CH_PHARMACY_CREDIT_RETURNS)
							|| chargeHead.equals(ChargeDTO.CH_INVENTORY_ITEM) || chargeHead.equals(ChargeDTO.CH_INVENTORY_RETURNS))){

						if(null != planIds && planIds.length > 0){
							//Updating claim amount in bill charge claim table when plan exist..
							BigDecimal claimAmount = insCalculator.calculateClaim((BigDecimal)charge.get("amount"), (BigDecimal)charge.get("discount"),
									billNo, planIds[0],(Boolean)charge.get("first_of_category"), "", (Integer)charge.get("insurance_category_id"), insPayable.equals("Y"));
							BigDecimal secClaimAmt=BigDecimal.ZERO;
							if(planIds.length>1){
								BigDecimal remainingAmt = ((BigDecimal)charge.get("amount")).subtract(claimAmount);
								secClaimAmt=insCalculator.calculateClaim(remainingAmt, BigDecimal.ZERO,
										billNo, planIds[1],(Boolean)charge.get("first_of_category"), "", (Integer)charge.get("insurance_category_id"), insPayable.equals("Y"));

							}
							charge.set("insurance_claim_amount", claimAmount.add(secClaimAmt));
							UpdateInsAmtForItems(con, charge.getMap(), billNo, claimAmount ,secClaimAmt,planIds, visitId);
						}else{
							//Updating claim amount in case of corporate/tpa only/national..
							charge.set("insurance_claim_amount", (BigDecimal)charge.get("amount"));
						}
					}
				}
			}

			charge.set("amount_included", BigDecimal.ZERO);
			charge.set("qty_included", BigDecimal.ZERO);
			charge.set("package_finalized", "N");
			charge.set("mod_time", currentTimestamp);
			charge.set("username", userid);
			chargedao.updateWithName(con, charge.getMap(), "charge_id");

			if (newPkgId == 0 && chargeHead.equals(ChargeDTO.CH_DYNA_PACKAGE_MARGIN)) {
				ChargeDAO.cancelChargeUpdateAuditLog(con, chargeId, false, RequestContext.getUserName());
			}
		}

		if((Boolean)billbean.get("is_tpa")){
			Iterator iterator = updateBillChargeList.iterator();
			while (iterator.hasNext()) {
				ChargeDTO chargeDTO = (ChargeDTO) iterator.next();
				String chargeHead = chargeDTO.getChargeHead();
				String insPayable = new ChargeDAO(con).getChargeInsurancePayable(chargeHead);

				if(insPayable.equals("Y")){
					if(!(chargeHead.equals(ChargeDTO.CH_PHARMACY_CREDIT_MEDICINE) || chargeHead.equals(ChargeDTO.CH_PHARMACY_CREDIT_RETURNS)
							|| chargeHead.equals(ChargeDTO.CH_INVENTORY_ITEM) || chargeHead.equals(ChargeDTO.CH_INVENTORY_RETURNS))){

						if(null != planIds && planIds.length > 0){
							//Updating claim amount in bill charge claim table when plan exist..
							BigDecimal claimAmount = insCalculator.calculateClaim(chargeDTO.getAmount(), chargeDTO.getDiscount(),
									billNo, planIds[0],chargeDTO.getFirstOfCategory(), "", chargeDTO.getInsuranceCategoryId(), insPayable.equals("Y"));
							BigDecimal secClaimAmt = BigDecimal.ZERO;
							if(planIds.length > 1){
								BigDecimal remainingAmt = chargeDTO.getAmount().subtract(claimAmount);
								secClaimAmt = insCalculator.calculateClaim(remainingAmt, BigDecimal.ZERO, billNo, planIds[1], chargeDTO.getFirstOfCategory(),
										"", chargeDTO.getInsuranceCategoryId(), insPayable.equals("Y"));
							}
							chargeDTO.setClaimAmounts(planIds.length > 1 ? new BigDecimal[]{claimAmount, secClaimAmt} : new BigDecimal[]{claimAmount});
						}else{
							//Updating claim amount in case of corporate/tpa only/national..
							chargeDTO.setInsuranceClaimAmount(chargeDTO.getAmount());
						}
					}
				}
			}
		}

		billbean.set("dyna_pkg_processed", "N");
		billbean.set("mod_time", currentTimestamp);
		billbean.set("username", userid);
		billdao.updateWithName(con, billbean.getMap(), "bill_no");

		return true;
	}

	public boolean resetPharmacyItems(Connection con, String chargeId) throws IOException, SQLException {

		BasicDynaBean salebean = saledao.findByKey("charge_id", chargeId);
		if (salebean == null)
			return true;

		String saleId = (String)salebean.get("sale_id");
		List<BasicDynaBean> saleItems = saleitemdao.findAllByKey("sale_id", saleId);

		for (BasicDynaBean saleitem: saleItems) {
			saleitem.set("amount_included", BigDecimal.ZERO);
			saleitem.set("qty_included", BigDecimal.ZERO);
			saleitem.set("package_finalized", "N");
			saleitemdao.updateWithName(con, saleitem.getMap(), "sale_item_id");
		}

		return true;
	}

	public boolean isDynaPackageChanged(Connection con, String billNo) throws SQLException {
		BasicDynaBean existingBillbean = billdao.findByKey("bill_no", billNo);
		int existingPkgId = (Integer)existingBillbean.get("dyna_package_id");

		BasicDynaBean billbean = billdao.findByKey(con, "bill_no", billNo);
		int newPkgId = (Integer)billbean.get("dyna_package_id");

		// Do nothing if no change in dyna package or bill has no dyna package.
		if (existingPkgId == newPkgId)
			return false;

		return true;
	}
}
