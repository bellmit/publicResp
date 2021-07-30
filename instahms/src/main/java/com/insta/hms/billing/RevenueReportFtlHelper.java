package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FtlHelper;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;
import com.insta.hms.payments.PaymentsDAO;
import com.insta.hms.stores.StoreDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Class to return contents of a report for RevenueReportAction, given a set of
 * params. This is a common place that can be used both from email reports as well
 * as action class.
 */

public class RevenueReportFtlHelper extends FtlHelper {

	private Configuration cfg;

	public RevenueReportFtlHelper(Configuration cfg) {
		this.cfg = cfg;
	}

	public byte[] getDashboardReport(Connection con, Map params, Object out)
			throws SQLException, IOException, TemplateException,
			DocumentException {

		String format = (String) params.get("format");
		java.sql.Date fromDate = (java.sql.Date) params.get("fromDate");
		java.sql.Date toDate = (java.sql.Date) params.get("toDate");

		int centerId = params.get("center_id") == null ? 0 :
			(params.get("center_id") instanceof java.lang.String ?
					Integer.parseInt((String) params.get("center_id")) : (Integer) params.get("center_id"));
		if (centerId != 0) {
			params.put("center_name", DataBaseUtil.getStringValueFromDb(
						"SELECT center_name FROM hospital_center_master WHERE center_id = ?", centerId));
		}

		if (params.get("center_id") == null)
			params.put("center_id", 0);

		String accountGroupStr = (String) params.get("accountGroup");
		int accountGroup = 0;
		if (accountGroupStr != null && !accountGroupStr.equals(""))
			accountGroup = Integer.parseInt(accountGroupStr);

		/*
		 * -----------------------------------------------------------------------------------------
		 * Numbers from various DAOs
		 * -----------------------------------------------------------------------------------------
		 *
		 * Patient Counts
		 */
		params.put("ipRegCount", PatientDetailsDAO.getIpRegCount(fromDate,toDate,centerId));
		params.put("ipDischargeCount", PatientDetailsDAO.getIpDischargeCount(fromDate, toDate, centerId));
		params.put("inPatientCount", PatientDetailsDAO.getActiveInPatientCount(centerId));
		params.put("opRegCount", PatientDetailsDAO.getOpRegCount(fromDate,toDate,centerId));
		params.put("ospRegCount", PatientDetailsDAO.getOspRegCount(fromDate,toDate,centerId));

		/*
		 * Bill Counts
		 */
		params.put("billNowCount", ChargeDAO.getTotalBillNowBills(accountGroup, centerId));
		params.put("billLaterCount", ChargeDAO.getTotalBillLaterBills(accountGroup, centerId));
		params.put("cancelledBillCount", ChargeDAO.getCancelledBillsCount(fromDate, toDate, accountGroup, centerId));
		params.put("closedBillCount", ChargeDAO.getClosedBillsCount(fromDate,toDate, accountGroup, centerId));

		/*
		 * Bill Later receipts, grouped by payment type and receipt type (sum and count)
		 */
		List<BasicDynaBean> creditReceipts =
			ReceiptRelatedDAO.getCreditReceiptsGrouped(fromDate, toDate, accountGroup, centerId);
		params.put("creditReceipts", ConversionUtils.listBeanToMapBean(creditReceipts, "r_type"));

		/*
		 * Consolidated Receipts (sum and count)
		 */
		BasicDynaBean consClaimReceiptsBean =
			ReceiptRelatedDAO.getConsolidatedClaimReceipts(fromDate, toDate, accountGroup, centerId);
		params.put("consClaimReceipts", consClaimReceiptsBean.getMap());

		/*
		 * List of charges grouped by some particular charge heads, and counts
		 * for prepaid bills
		 */
		List prepaidCharges = ChargeDAO.getPrepaidChargesGrouped(fromDate,toDate, accountGroup, centerId);
		params.put("prepaidCharges", ConversionUtils.listBeanToMapMap(prepaidCharges, "charge_type"));

		BigDecimal opTotalAmount = BigDecimal.ZERO;
		BigDecimal opTotalCount = BigDecimal.ZERO;
		for (BasicDynaBean charge : (List<BasicDynaBean>) prepaidCharges) {
			opTotalAmount = opTotalAmount.add((BigDecimal) charge.get("amount"));
			opTotalCount = opTotalCount.add((BigDecimal) charge.get("activities"));
		}
		params.put("opTotalAmount", opTotalAmount);
		params.put("opTotalCount", opTotalCount);

		/*
		 * To get the Bill Now exceptions: extra collections not for bills
		 * within same period. less uncollected amounts for the bill now bills
		 * (collected later, or deposit set off)
		 */
		BasicDynaBean extraReceipts =
			ReceiptRelatedDAO.getBillNowExtraReceipts(fromDate, toDate, accountGroup, centerId);
		BasicDynaBean excessCollection =
			ReceiptRelatedDAO.getBillNowExcessCollection(fromDate, toDate, accountGroup, centerId);
		BasicDynaBean setOffs = DepositsDAO.getDepositSetOffs(fromDate, toDate,	accountGroup, centerId);

		params.put("extraReceipts", extraReceipts);
		params.put("excessCollection", excessCollection);
		params.put("setOffs", setOffs);

		BigDecimal extraReceiptsTotalAmount = BigDecimal.ZERO;
		long extraReceiptsTotalCount = 0;

		if ((BigDecimal.ZERO
				.compareTo((BigDecimal) extraReceipts.get("amount")) != 0)
				|| (BigDecimal.ZERO.compareTo((BigDecimal) excessCollection.get("amount")) != 0)
				|| (BigDecimal.ZERO.compareTo((BigDecimal) setOffs.get("amount")) != 0)) {

			extraReceiptsTotalAmount = ((BigDecimal) extraReceipts
					.get("amount")).add((BigDecimal) excessCollection.get("amount"))
					.subtract((BigDecimal) setOffs.get("amount"));

			extraReceiptsTotalCount = ((Long) extraReceipts.get("count"))
					+ ((Long) excessCollection.get("count"))
					+ ((Long) setOffs.get("count"));
		}

		params.put("extraReceiptsTotalAmount", extraReceiptsTotalAmount);
		params.put("extraReceiptsTotalCount", extraReceiptsTotalCount);

		/*
		 * To get deposit receipts and refunds summary (sum and count)
		 */
		params.put("deposits", DepositsDAO.getDepositSummary(fromDate, toDate, centerId));
		params.put("depositReceipts", DepositsDAO.getDepositReceiptsSummary(fromDate, toDate, centerId));
		params.put("depositRefunds", DepositsDAO.getDepositRefundsSummary(fromDate, toDate, centerId));

		/*
		 * Payments, grouped by payment category
		 */
		List paymentSummary = PaymentsDAO.getPaymentsSummaryTotal(fromDate,toDate, centerId);

		params.put("paymentAmounts", ConversionUtils.listBeanToMapNumeric(
				paymentSummary, "payment_type", "amt"));
		params.put("paymentCounts", ConversionUtils.listBeanToMapNumeric(
				paymentSummary, "payment_type", "count"));

		/*
		 * Doctor expenses sum
		 */
		params.put("pres_amt", PaymentsDAO.getPrescDrChargesTotal(fromDate, toDate, centerId));
		params.put("cond_amt", PaymentsDAO.getConductingDrChargesTotal(fromDate, toDate, centerId));
		params.put("ref_amt", PaymentsDAO.getReferralDrChargesTotal(fromDate, toDate, centerId));
		for (BasicDynaBean paymntSumm : (List<BasicDynaBean>) paymentSummary) {
			if(paymntSumm.get("payment_type").equals("C"))
				params.put("misc_amt", paymntSumm.get("amt"));
		}
		/*
		 * Outhouse expenses sum
		 */
		List outhouseExpenses = PaymentsDAO.getOutHouseChargesTotal(fromDate, toDate, centerId);
		List outhouseNames = new ArrayList();
		params.put("outHouseExpenses", ConversionUtils.listBeanToMapNumeric(
					outhouseExpenses, "oh_name", "oh_amount"));
		for(BasicDynaBean oh : (List<BasicDynaBean>) outhouseExpenses) {
			if(oh.get("oh_amount") != null)
				outhouseNames.add(oh.get("oh_name"));
		}
		params.put("outhouseNames", outhouseNames);

		/*
		 * store expenses
		 */
		StoreDAO sd = new StoreDAO(con);
		List storePurchases = sd.getStorePurchasesTotals(fromDate, toDate, centerId);
		params.put("storeExpenses", ConversionUtils.listBeanToMapNumeric(
					storePurchases, "store_name", "purchase_amt"));
		List storeNames = new ArrayList();
		for (BasicDynaBean store : (List<BasicDynaBean>) storePurchases) {
			storeNames.add(store.get("store_name"));
		}
		params.put("storeNames", storeNames);

		/*
		 * Get the group level Top N count from chargegroup_top_n table: if not
		 * found, then from generic_preferences.
		 */
		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
		int defaultMaxCountPref = (Integer) genericPrefs.get("cfd_max_count");

		List pref = ChargeDAO.getAllFromBillDashboardTopN();
		HashMap prefMap = new LinkedHashMap();

		for (BasicDynaBean bean : (List<BasicDynaBean>) pref) {
			prefMap.put(bean.get("charge_group"), bean.get("display_count"));
		}

		/*
		 * Collections, grouped by payment mode and receipt main_type
		 */
		List cg = ReceiptRelatedDAO.getReceiptsGrouped(fromDate, toDate, accountGroup, centerId);
		params.put("collections", ConversionUtils.listBeanToMapMapNumeric(cg,
				"payment_mode", "main_type", "amount"));

		List modes = new PaymentModeMasterDAO().getPaymentModeFieldsWithTotal();

		/*
		 * Revenue based on posted date/finalized date, grouped by Charge Group +
		 * Descr + Visit Type
		 */
		List revenue = ChargeDAO.getChargesGroupedByCGroupDescr(fromDate,
				toDate, (String) params.get("dateField"), accountGroup, centerId);

		Map revenueMap = ConversionUtils.listBeanToMapMapMapNumeric(revenue,
				"c_group", "descr", "v_type", "amount");
		Map countMap = ConversionUtils.listBeanToMapMapMapNumeric(revenue,
				"c_group", "descr", "v_type", "count");

		List<String> groups = new ArrayList();
		groups.addAll(revenueMap.keySet());
		// TODO: sort should use absolute values so that high discounts, returns
		// appear higher rather than lower.
		Collections.sort(groups, new ConversionUtils.ByTotalTotal(revenueMap));

		Map chargeItemsMap = new HashMap(); // to store final list of items for
		// each charge group

		for (String chargeGroup : groups) {
			int max;

			if (prefMap.containsKey(chargeGroup))
				max = (Integer) prefMap.get(chargeGroup);
			else
				max = defaultMaxCountPref;
			List<String> itemList = new ArrayList();
			Map itemsAmtMap = (Map) revenueMap.get(chargeGroup);
			Map itemsCountMap = (Map) countMap.get(chargeGroup);

			itemList.addAll(itemsAmtMap.keySet());
			itemList.remove("_total"); // don't need total to mess up our topN
			// algo
			Collections.sort(itemList, new ConversionUtils.ByTotal(itemsAmtMap));

			// consolidate

			Boolean isMaxCountNotZero = true;
			int len = itemList.size();
			int maxCount = chargeGroup.equals("Others") ? 1 : max;
			if (maxCount == 0) {
				maxCount = 1;
				isMaxCountNotZero = false;
			} else
				++maxCount;

			if (len > maxCount) {
				// consolidate the items map so that a max of maxCount elements
				// remain
				ConversionUtils.consolidateMapMap(itemsAmtMap, itemList,maxCount);
				// consolidate the count map also using the same list
				ConversionUtils.consolidateMapMap(itemsCountMap, itemList,maxCount);

				// remove all but maxCount-1 from list, add "_others"
				for (int i = len - 1; i >= maxCount - 1; i--) {
					itemList.remove(i);
				}
				if (isMaxCountNotZero)
					itemList.add("_others");
			}
			// put back "total" in the head of the list
			itemList.add(0, "_total");
			if (!isMaxCountNotZero && itemList != null) {
				for (int j = 0; j < itemList.size(); j++)
					if (!itemList.get(j).equals("_total"))
						itemList.remove(j);
			}
			chargeItemsMap.put(chargeGroup, itemList);
		}

		params.put("groups", groups);
		params.put("amounts", revenueMap);
		params.put("counts", countMap);
		params.put("itemList", chargeItemsMap);
		params.put("displayModes", ConversionUtils.listBeanToListMap(modes));
		params.put("curDateTime", DataBaseUtil.timeStampFormatter.format(new java.util.Date()));

		Template t = cfg.getTemplate("RevenueDashboardReport.ftl");

		return getFtlReport(t, params, format, out);
	}

}
