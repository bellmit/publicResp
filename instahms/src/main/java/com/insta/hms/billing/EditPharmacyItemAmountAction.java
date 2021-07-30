/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.stores.MedicineSalesBO;
import com.insta.hms.stores.MedicineSalesDAO;
import com.insta.hms.stores.PurchaseOrderDAO;
import com.insta.hms.stores.SalesClaimDetailsDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author lakshmi.p
 *
 */
public class EditPharmacyItemAmountAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(EditPharmacyItemAmountAction.class);
	private static SalesClaimDetailsDAO salesClaimDAO = new SalesClaimDetailsDAO();
	private static PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();
	private static final GenericDAO storeSalesMainDAO = new GenericDAO("store_sales_main");

	public ActionForward editPharmacyItemAmount(ActionMapping mapping, ActionForm form,
            HttpServletRequest req, HttpServletResponse res) throws Exception {

		String billNo = req.getParameter("billNo").trim();
		req.setAttribute("isNewUX", req.getParameter("isNewUX"));
		Integer storeId = 0;
		Integer centerId = -1;
		JSONSerializer js = new JSONSerializer().exclude("class");
		BillBO billBOObj = new BillBO();
		BillDetails billDetails = null;
		Bill bill = null;
		billDetails = billBOObj.getBillDetails(billNo);
		if (billDetails != null) {
			bill = billDetails.getBill();
		} else {
			req.setAttribute("error", "There is no bill with number: " + billNo);
			return mapping.findForward("editPharmacyItemAmount");
		}

		List<BasicDynaBean> saleItemsList = MedicineSalesDAO.getBillSaleItemDetails(billNo);
		BasicDynaBean storeItemMainBean = storeSalesMainDAO.findByKey("sale_id", billNo);

		if(storeItemMainBean != null) {
   		 storeId = (Integer)storeItemMainBean.get("store_id");
   	 	}
	   	BasicDynaBean storeBean = new GenericDAO("stores").findByKey("dept_id", storeId);
	   	if(storeBean != null)
	   		centerId = (Integer)storeBean.get("center_id");
	   	String healthAuthority = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(CenterMasterDAO.getHealthAuthorityForCenter(centerId)).getHealth_authority();
	   	String[] drugCodeTypes = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthority).getDrug_code_type();

		Map itemCatMap = ConversionUtils.listBeanToMapListMap(saleItemsList, "category");
		TreeMap sortedItemCatMap = new TreeMap();
		sortedItemCatMap.putAll(itemCatMap);
		List<String> itemCategories = new ArrayList<String>();
		itemCategories.addAll(sortedItemCatMap.keySet());

		Map itemNameMap = ConversionUtils.listBeanToMapListMap(saleItemsList, "medicine_name");
		TreeMap sortedItemNameMap = new TreeMap();
		sortedItemNameMap.putAll(itemNameMap);
		List<String> itemNames = new ArrayList<String>();
		itemNames.addAll(sortedItemNameMap.keySet());

		Map itemBillMap = ConversionUtils.listBeanToMapListMap(saleItemsList, "sale_id");
		TreeMap sortedItemBillMap = new TreeMap();
		sortedItemBillMap.putAll(itemBillMap);
		List<String> itemBills = new ArrayList<String>();
		itemBills.addAll(sortedItemBillMap.keySet());

		List saleItems = ConversionUtils.listBeanToListMap(saleItemsList);

		Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(bill.getVisitId());
		req.setAttribute("patient", patientDetails);
		req.setAttribute("billDetails", billDetails);
		req.setAttribute("saleItems", saleItems);

//		multi-payer : set visit plan details
		setPlanDetails(req,bill.getVisitId());

		req.setAttribute("itemCategories", itemCategories);
		req.setAttribute("itemNames", itemNames);
		req.setAttribute("itemBills", itemBills);
		req.setAttribute("subGroupListJSON", js.serialize(ConversionUtils.listBeanToListMap(PurchaseOrderDAO.getAllSubGroups())));
		req.setAttribute("pharmaCodeTypesJSON", js.serialize(MedicineSalesDAO.getPharmaCodeTypes()));
	    req.setAttribute("pharmaCodesJSON", js.serialize(MedicineSalesDAO.getPharmaCodes(drugCodeTypes)));
	    req.setAttribute("after_decimal_digits",GenericPreferencesDAO.getPrefsBean().get("after_decimal_digits")!=null?GenericPreferencesDAO.getPrefsBean().get("after_decimal_digits"):0);

		return mapping.findForward("editPharmacyItemAmount");
	}


	@SuppressWarnings("unchecked")
	public ActionForward updatePharmacyItemAmount(ActionMapping mapping, ActionForm form,
            HttpServletRequest req, HttpServletResponse res) throws Exception {

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(req);

		HttpSession session = req.getSession();
        String userid = (String)session.getAttribute("userid");
		GenericDAO billdao = new GenericDAO("bill");
		GenericDAO chargedao = new GenericDAO("bill_charge");
		GenericDAO saleitemdao = new GenericDAO("store_sales_details");
		String billNo = req.getParameter("billNo");
		BillBO billBOObj = new BillBO();
		Bill bill = billBOObj.getBill(billNo);
		BillChargeClaimDAO billChargeClaimDAO = new BillChargeClaimDAO();
		boolean success = true;
		Connection con = null;

		String isNewUX = req.getParameter("isNewUX");
		String[] itemCode = req.getParameterValues("itemCode");
		String[] itemCodeType = req.getParameterValues("itemCodeType");
		String[] pri_itemPreAuthId = req.getParameterValues("pri_itemPreAuthId");
		String[] pri_itemPreAuthMode = req.getParameterValues("pri_itemPreAuthMode");
		String[] sec_itemPreAuthId = req.getParameterValues("sec_itemPreAuthId");
		String[] sec_itemPreAuthMode = req.getParameterValues("sec_itemPreAuthMode");

		String[] itemQty = req.getParameterValues("itemQty");
		String[] itemAmount = req.getParameterValues("itemAmount");

		String[] patientAmt = req.getParameterValues("patientAmt");

		String[] pri_insClaimAmt = req.getParameterValues("pri_insClaimAmt");
		String[] sec_insClaimAmt = req.getParameterValues("sec_insClaimAmt");
		
		String[] pri_include_in_claim_calc = req.getParameterValues("pri_include_in_claim_calc");
		String[] sec_include_in_claim_calc = req.getParameterValues("sec_include_in_claim_calc");

		String[] includedQty = req.getParameterValues("qtyIncluded");
		String[] includedAmount = req.getParameterValues("amountIncluded");
		String[] packageFinalized = req.getParameterValues("packageFinalized");

		String[] saleItemId = req.getParameterValues("saleItemId");

		List<String> saleBills = new ArrayList<String>();
		List<String> returnBills = new ArrayList<String>();

		List<BasicDynaBean> returnItemsList = MedicineSalesDAO.getBillReturnItemDetails(billNo);
		Map itemBillMap = ConversionUtils.listBeanToMapListMap(returnItemsList, "sale_id");
		returnBills.addAll(itemBillMap.keySet());

		Map<String, Map<String, BigDecimal>> saleClaimAndIncludedTotals = new HashMap<String, Map<String, BigDecimal>>();

		List<BasicDynaBean> saleCharges = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> returnCharges = new ArrayList<BasicDynaBean>();

		String saleId = null;
		List<String> saleIdList = new ArrayList<>();
    MedicineSalesBO medicineSalesBO = new MedicineSalesBO();
    
		if (saleItemId == null || saleItemId.length == 0) {
			redirect.addParameter("isNewUX", isNewUX);
			redirect.addParameter("billNo", billNo);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		try {
			update: {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				ChargeDAO chargeDAO = new ChargeDAO(con);
				BasicDynaBean salebean = null;

				List<BasicDynaBean> planList = insPlanDAO.getPlanDetails(con, bill.getVisitId());
				int[] planIds = new int[planList.size()];
				Map saleItemIdMap = new HashMap();
				String saleItemIdString = "";


				List<BasicDynaBean> itembeanList = new ArrayList<BasicDynaBean>();

				for (int i = 0; i < saleItemId.length; i++) {

					String item_code = itemCode[i];
					String code_type = itemCodeType[i];
					String prior_auth_id = (pri_itemPreAuthId != null && pri_itemPreAuthId[i] != null) ? pri_itemPreAuthId[i] : null;

					BigDecimal claim_amt = (pri_insClaimAmt != null && pri_insClaimAmt[i] != null) ? new BigDecimal(pri_insClaimAmt[i]) : BigDecimal.ZERO;
					Integer sale_item_id = new Integer(saleItemId[i]);

					BigDecimal included_qty = (includedQty != null && includedQty[i] != null) ? new BigDecimal(includedQty[i]) : BigDecimal.ZERO;
					BigDecimal included_amt = (includedAmount != null && includedAmount[i] != null) ? new BigDecimal(includedAmount[i]) : BigDecimal.ZERO;
					String pkgFinalized = (packageFinalized != null && packageFinalized[i] != null) ? packageFinalized[i] : "N";
					Integer prior_auth_mode_id = (pri_itemPreAuthMode != null && pri_itemPreAuthMode[i] != null && !pri_itemPreAuthMode[i].trim().equals("")) ? new Integer(pri_itemPreAuthMode[i]) : null;

					BasicDynaBean itembean = saleitemdao.findByKey("sale_item_id", sale_item_id);

					saleId = (String)itembean.get("sale_id");
					saleIdList.add(saleId);
					itembean.set("item_code", item_code);
					itembean.set("code_type", code_type);
					itembean.set("prior_auth_id", prior_auth_id);
					itembean.set("prior_auth_mode_id", prior_auth_mode_id);

					itembean.set("insurance_claim_amt", claim_amt);
					itembean.set("return_insurance_claim_amt", BigDecimal.ZERO);

					itembean.set("amount_included", included_amt);
					itembean.set("qty_included", included_qty);
					itembean.set("package_finalized", pkgFinalized);

					itembeanList.add(itembean);

					BigDecimal[] claimAmts = new BigDecimal[planList.size()];
					String[] priAuthIds = new String[planList.size()];
					String[] priAuthModes = new String[planList.size()];
          if (null != pri_insClaimAmt) {
            for (int j = 0; j < planList.size(); j++) {
              if (j == 0) {
                claimAmts[j] = pri_insClaimAmt[i] != null ? new BigDecimal(pri_insClaimAmt[i])
                    : BigDecimal.ZERO;
                priAuthIds[j] = pri_itemPreAuthId[i];
                priAuthModes[j] = pri_itemPreAuthMode[i];
              } else {
                claimAmts[j] = sec_insClaimAmt[i] != null ? new BigDecimal(sec_insClaimAmt[i])
                    : BigDecimal.ZERO;
                priAuthIds[j] = sec_itemPreAuthId[i];
                priAuthModes[j] = sec_itemPreAuthMode[i];
              }
            }
          }

					if (saleBills != null && !saleBills.contains(saleId)) {
						saleBills.add(saleId);
					}

					if (saleClaimAndIncludedTotals.containsKey(saleId)) {

						Map m = (HashMap<String, BigDecimal>)saleClaimAndIncludedTotals.get(saleId);
						BigDecimal claimAmt = (BigDecimal)m.get("claim_amt");
						BigDecimal includedAmt = (BigDecimal)m.get("included_amt");
						saleClaimAndIncludedTotals.remove(saleId);
						Map<String, BigDecimal> smap = new HashMap<String, BigDecimal>();
						smap.put("claim_amt", claimAmt.add(claim_amt));
						smap.put("included_amt", includedAmt.add(included_amt));
						saleClaimAndIncludedTotals.put(saleId, smap);
					}else {
						Map<String, BigDecimal> smap = new HashMap<String, BigDecimal>();
						smap.put("claim_amt", claim_amt);
						smap.put("included_amt", included_amt);
						saleClaimAndIncludedTotals.put(saleId, smap);
					}

					GenericDAO salesClaimDAO = new GenericDAO("sales_claim_details");
					BillChargeClaimDAO billClaimDAO = new BillChargeClaimDAO();

					saleItemIdMap.put("sale_item_id", sale_item_id);
					Map keys = new HashMap();
          for (int j = 0; j < planList.size(); j++) {
            planIds[j] = (Integer) planList.get(j).get("plan_id");
            String sponsorId = insPlanDAO.getSponsorId(con, bill.getVisitId(),
                (Integer) planList.get(j).get("plan_id"));
            String claimId = billClaimDAO.getClaimId(con, (Integer) planList.get(j).get("plan_id"),
                billNo, bill.getVisitId(), sponsorId);

            keys.put("sale_item_id", Integer.parseInt(saleItemId[i]));
            keys.put("claim_id", claimId);
            BasicDynaBean salesSponsorClaim = salesClaimDAO.findByKey(keys);
            if (null != salesSponsorClaim) {
              prior_auth_mode_id = (priAuthModes[j] != null && !priAuthModes[j].trim().equals(""))
                  ? new Integer(priAuthModes[j]) : null;
              salesSponsorClaim.set("insurance_claim_amt", claimAmts[j]);
              salesSponsorClaim.set("ref_insurance_claim_amount", claimAmts[j]);
              salesSponsorClaim.set("prior_auth_id", priAuthIds[j]);
              salesSponsorClaim.set("prior_auth_mode_id", prior_auth_mode_id);
              if (j == 0) {
                boolean val = Boolean.parseBoolean(pri_include_in_claim_calc[i]);
                salesSponsorClaim.set("include_in_claim_calc", val);
              } else if (j == 1) {
                boolean val1 = Boolean.parseBoolean(sec_include_in_claim_calc[i]);
                salesSponsorClaim.set("include_in_claim_calc", val1);
              }
              success &= (salesClaimDAO.update(con, salesSponsorClaim.getMap(),
                  "sales_item_plan_claim_id",
                  (Integer) salesSponsorClaim.get("sales_item_plan_claim_id")) > 0);
            }
          }


				}

				//update bill charge claim

				//get all sales claim details group by claim,charge,billno
				List<BasicDynaBean> salesClaimDetails = salesClaimDAO.getSalesClaimDetails(con, saleItemId);

				for(BasicDynaBean salesClaimDetail : salesClaimDetails){
					ChargeDTO salescharge = chargeDAO.getCharge((String)salesClaimDetail.get("charge_id"));
					salescharge.setInsuranceClaimAmount((BigDecimal)salesClaimDetail.get("total_insurance_claim_amt"));
					//update bill charge claim
					billChargeClaimDAO.updateBillChargeClaim(con, salescharge, bill.getVisitId(), billNo, (String)salesClaimDetail.get("claim_id"));

				}

				for (BasicDynaBean rbean : returnItemsList) {
					Integer sale_item_id = (Integer)rbean.get("sale_item_id");
					BasicDynaBean itembean = saleitemdao.findByKey("sale_item_id", sale_item_id);

					itembean.set("insurance_claim_amt", BigDecimal.ZERO);
					itembean.set("return_insurance_claim_amt", BigDecimal.ZERO);
					itembeanList.add(itembean);
				}

				for (String salebill : saleBills) {
					salebean = storeSalesMainDAO.findByKey("sale_id", salebill);
					String charge_id = salebean.get("charge_id") != null ? (String)salebean.get("charge_id") : null;
					Map<String, BigDecimal> smap = saleClaimAndIncludedTotals.get(salebill) != null ? (HashMap<String, BigDecimal>)saleClaimAndIncludedTotals.get(salebill) : null;
					if (charge_id != null && smap != null) {
						BasicDynaBean chargebean = chargedao.findByKey("charge_id", charge_id);
						chargebean.set("insurance_claim_amount", (BigDecimal)smap.get("claim_amt"));
						chargebean.set("return_insurance_claim_amt", BigDecimal.ZERO);
						chargebean.set("amount_included", (BigDecimal)smap.get("included_amt"));
						chargebean.set("qty_included", BigDecimal.ZERO);
						chargebean.set("username", userid);

						saleCharges.add(chargebean);
					}
				}

				for (String returnbill : returnBills) {
					BasicDynaBean returnbean = storeSalesMainDAO.findByKey("sale_id", returnbill);
					String charge_id = returnbean.get("charge_id") != null ? (String)returnbean.get("charge_id") : null;

					if (charge_id != null) {
						BasicDynaBean chargebean = chargedao.findByKey("charge_id", charge_id);
						chargebean.set("insurance_claim_amount", BigDecimal.ZERO);
						chargebean.set("return_insurance_claim_amt", BigDecimal.ZERO);
						chargebean.set("amount_included", BigDecimal.ZERO);
						chargebean.set("qty_included", BigDecimal.ZERO);
						chargebean.set("username", userid);

						returnCharges.add(chargebean);
					}
				}

				for (BasicDynaBean itembean : itembeanList) {
					int result = saleitemdao.updateWithName(con, itembean.getMap(), "sale_item_id");
					success = (result > 0);
					if (!success)
						break update;
				}

				for (BasicDynaBean charge : saleCharges) {
					int result = chargedao.updateWithName(con, charge.getMap(), "charge_id");
					success = (result > 0);
					if (!success)
						break update;
				}

				for (BasicDynaBean charge : returnCharges) {
					int result = chargedao.updateWithName(con, charge.getMap(), "charge_id");
					success = (result > 0);
					if (!success)
						break update;
				}

				if (bill.getDynaPkgId() != 0) {
					Map<String, String> filterMap = new HashMap<String, String>();
					filterMap.put("bill_no", billNo);
					filterMap.put("status", "A");

					List<BasicDynaBean> charges = chargedao.listAll(con, null, filterMap, "posted_date");
					boolean marginClaimable = bill.getIs_tpa();
					BigDecimal packageAmount = bill.getDynaPkgCharge();

					// Calculate package margin and update.
					new DynaPackageProcessor().updatePackageMarginAmount(con, charges, packageAmount, marginClaimable, userid);
				}

			}// update label

    } finally {
      DataBaseUtil.commitClose(con, success);
      if (success) {
        try {
          // Dyna Pkg & Perdiem are not processed, perdiem is recalculated.
          if (billNo != null && !billNo.equals(""))
            BillDAO.resetTotalsOrReProcess(billNo, false, false, true);
          new SponsorBO().recalculateSponsorAmount(bill.getVisitId());
          con = DataBaseUtil.getConnection();
          con.setAutoCommit(false);
          for (String salesId : saleIdList) {
            success &= medicineSalesBO.insertOrUpdateBillChargeTaxesForSales(con, salesId);
          }
        } finally {
          DataBaseUtil.commitClose(con, success);
        }
      }
    }

		if (!success) {
			flash.error("Pharmacy item amount updation unsuccessful...");
		}
		redirect.addParameter("isNewUX", isNewUX);
		redirect.addParameter("billNo", billNo);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	/**
	 * Sets insurance plan related details in the map
	 * @param resultMap
	 */
	private void setPlanDetails(HttpServletRequest req,String visitId) throws SQLException{
		List<Map> saleItems  = (List<Map>)req.getAttribute("saleItems");
		Map salesClaimDetails = new HashMap();
		for( Map saleItem : saleItems){
			salesClaimDetails.put(saleItem.get("sale_item_id"), ConversionUtils.listBeanToListMap(
					salesClaimDAO.listAll(null,"sale_item_id", saleItem.get("sale_item_id"),"sales_item_plan_claim_id")));
		}
		req.setAttribute("sales_claim_details", salesClaimDetails);

	}
}
