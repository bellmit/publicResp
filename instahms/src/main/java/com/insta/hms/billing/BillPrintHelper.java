package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.NumberToWordFormat;
import com.bob.hms.common.RequestContext;
import com.bob.hms.diag.incomingsamplependingbills.IncomingSamplePendingBillDAO;
import com.bob.hms.diag.ohsampleregistration.IncomingPatientDAO;
import com.insta.hms.Registration.VisitCaseRateDetailDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.master.BillPrintTemplate.BillPrintTemplateDAO;
import com.insta.hms.master.DynaPackage.DynaPackageDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.stores.MedicineSalesDAO;

import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Helper class to provide common functions for bill prints: used by printing from the UI
 * as well as bill prints backup.
 */
public class BillPrintHelper {

	private static String[] operationChargeHeads = { "SUOPE", "TCOPE",
			"ANAOPE", "ASUOPE", "AANOPE", "COSOPE", "SACOPE", "CONOPE", "EQOPE" };

	private static String[] labChargeHeads = { "LTDIA", "RTDIA", };

	private static String[] serviceChargeHeads = { "SERSNP", };

	private static String[] pharmacyChargeHeads = { "PHCMED", "PHCRET",
			"PHMED", "PHRET", };

	private static String[] allChargeGroups = { "ALL", };

	private static HashMap displayChargeheadsCorrespondingChargeGroupMap;
	
	private static String webBasedBillLaterTemplate="Web Based Bill Later Print Template";
	private static String webBasedBillNowTemplate ="Web Based Bill Now Print Template";

	static {
		displayChargeheadsCorrespondingChargeGroupMap = new HashMap();
		displayChargeheadsCorrespondingChargeGroupMap.put("SNP",
				serviceChargeHeads);
		displayChargeheadsCorrespondingChargeGroupMap.put("OPE",
				operationChargeHeads);
		displayChargeheadsCorrespondingChargeGroupMap
				.put("DIA", labChargeHeads);
		displayChargeheadsCorrespondingChargeGroupMap.put("MED",
				pharmacyChargeHeads);
		displayChargeheadsCorrespondingChargeGroupMap.put("ALL",
				allChargeGroups);
	}
	
	static BillBO billBo =  new BillBO();
	static VisitCaseRateDetailDAO visitCaseRateDetDAO = new VisitCaseRateDetailDAO();
	static DynaPackageDAO dynaPkgDAO = new DynaPackageDAO();

	public static Map getBillJrxmlParams(String billNo, String detailed,
			String groupOption, String userId) throws SQLException {

		GenericPreferencesDTO dto = GenericPreferencesDAO
				.getGenericPreferences();

		HashMap params = new HashMap();

		params.put("billNo", billNo);
		params.put("detailed", detailed);
		params.put("loggeduser", userId);

		params.put("printUserName", dto.getUserNameInBillPrint());
		params.put("currencySymbol", dto.getCurrencySymbol());
		params.put("whole", dto.getWhole());
		params.put("decimal", dto.getDecimal());

		if ((groupOption == null) || groupOption.equals(""))
			groupOption = "ALL";

		params.put("chargeGroup", groupOption);
		params.put("chargeHead", displayChargeheadsCorrespondingChargeGroupMap
				.get(groupOption));

		return params;
	}

	public static String[] processBillTemplate(StringWriter writer,
			String billNo, String templateName, String userId)
			throws SQLException, TemplateException, IOException {
		/*
		 * Get the template for the print from the name of the template
		 */
		FtlReportGenerator ftlGen = null;
		String templateMode = null;
		String downloadExtn = null;
		String downloadCType = null;

		if (templateName.equals("BUILTIN_HTML") || templateName.equals("CUSTOM-BUILTIN_HTML")) {
			ftlGen = new FtlReportGenerator("BillPrintTemplate");
			templateMode = "H";

		} else if (templateName.equals("BUILTIN_TEXT") || templateName.equals("CUSTOM-BUILTIN_TEXT")) {
			ftlGen = new FtlReportGenerator("BillPrintTextTemplate");
			templateMode = "T";

		} else {
			BillPrintTemplateDAO templateDao = new BillPrintTemplateDAO();
			BasicDynaBean tmpBean = templateDao
					.getTemplateContent(templateName);

			if (tmpBean == null) {
				// couldn't find the template in the db, bail out with error.
				return null;
			} else if(tmpBean.get("bill_template_content") == null || ((String)tmpBean.get("bill_template_content")).trim().isEmpty() ){
				templateMode = (String) tmpBean.get("template_mode");
				
				if(templateName.equals(webBasedBillLaterTemplate) || templateName.equals(webBasedBillNowTemplate)) {					
					String builtinTemplateName = (templateMode.equals("H")) ? "BillPrintTemplate" : "BillPrintTextTemplate";
					ftlGen = new FtlReportGenerator(builtinTemplateName);
					
					// Update the contents of builtinTemplate to DB					
					Connection con =  null;
					int  success = 0;
					FileInputStream fis = null;
					try{
						BasicDynaBean bean = templateDao.getBean();
						con = DataBaseUtil.getConnection();
						con.setAutoCommit(false);
						bean.set("template_name", templateName);
						bean.set("template_mode",templateMode);
						String realPath = RequestContext.getRequest().getServletContext().getRealPath("");
						fis = new FileInputStream(new File(realPath+"/WEB-INF/templates/" + builtinTemplateName +".ftl"));
						bean.set("bill_template_content",new String(DataBaseUtil.readInputStream(fis)) );	
						Map<String, String> additionalDetailsKeys = new HashMap<String, String>();
						additionalDetailsKeys.put("template_name", templateName);
						success = templateDao.update(con,bean.getMap(),additionalDetailsKeys);
					} finally{
						DataBaseUtil.commitClose(con, success > 0);
					}					
				} else {
					StringReader reader = new StringReader("");
					ftlGen = new FtlReportGenerator("BillPrintTextTemplate", reader);
					downloadExtn = (String) tmpBean.get("download_extn");
					downloadCType = (String) tmpBean.get("download_content_type");
				}
			} else {
				String templateContent = (String) tmpBean
						.get("bill_template_content");
				templateMode = (String) tmpBean.get("template_mode");

				StringReader reader = new StringReader(templateContent);
				ftlGen = new FtlReportGenerator("BillPrintTextTemplate", reader);
				downloadExtn = (String) tmpBean.get("download_extn");
				downloadCType = (String) tmpBean.get("download_content_type");
			}
			
		}

		// params to be passed to the template processor
		Map params = new HashMap();

		GenericPreferencesDTO prefsDto = GenericPreferencesDAO
				.getGenericPreferences();

		/*
		 * Get the bill and patient details
		 */
		BasicDynaBean bill = BillDAO.getBillBeanWithoutDynaPkgCode(billNo);
		
		int dynaPkgID = (Integer) bill.get("dyna_package_id");
		String billRatePlanID = (String) bill.get("bill_rate_plan_id");
		
		BasicDynaBean dynaPkgBean = dynaPkgDAO.getDynaPackageDetailsBean(dynaPkgID, billRatePlanID);
		
		if (null != dynaPkgBean &&  null != dynaPkgBean.get("dyna_package_name")) {
		    String dynaPkgCode = (String) dynaPkgBean.get("item_code");
	        String dynaPkgName = (String) dynaPkgBean.get("dyna_package_name");
	        
	        bill.set("dyna_package_name", dynaPkgName);
	        bill.set("dyna_pkg_rate_plan_code", dynaPkgCode);
		}
		
		String visitId = (String) bill.get("visit_id");
		Map patientDetails;
		if (bill.get("visit_type").equals("t"))
			patientDetails = IncomingPatientDAO.getPatientVisitDetails(visitId);
		else
			patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);

		List diagnosisDetails = bill.get("visit_type").equals("t") ? Collections.EMPTY_LIST
				: MRDDiagnosisDAO.getAllDiagnosisDetails(visitId);
		
		BasicDynaBean creditNoteBean= new GenericDAO("bill_credit_notes").findByKey("credit_note_bill_no", billNo);
		if(null != creditNoteBean )
			params.put("original_bill_no", creditNoteBean.get("bill_no"));

		BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();

		params.put("user", userId);
		params.put("currentDateTime", new Date());
		params.put("currencySymbol", prefsDto.getCurrencySymbol());
		params.put("genPrefs", genPrefs);

		params.put("bill", bill);
		params.put("patient", patientDetails);
		params.put("diagnosisDetails", diagnosisDetails);
		
		/*
		 * Get the primary sponsor amount and secondary sponsor amount in bill prints
		 */
		
		BasicDynaBean priClaimBean = BillChargeClaimDAO
				.getPrimarySecondarySponsorAmountDetailsBean(billNo,1);
		if (priClaimBean == null)
			params.put("primary_sponsor_amount", BigDecimal.ZERO);
		else
			params.put("primary_sponsor_amount", priClaimBean.getMap().get("insurance_claim_amt"));
		
		BasicDynaBean secClaimBean = BillChargeClaimDAO
				.getPrimarySecondarySponsorAmountDetailsBean(billNo,2);
		if (secClaimBean == null)
			params.put("secondary_sponsor_amount", BigDecimal.ZERO);
		else
			params.put("secondary_sponsor_amount", secClaimBean.getMap().get("insurance_claim_amt"));

		List<BasicDynaBean> salesItemsList = MedicineSalesDAO
				.getPharmaBreakupList(billNo);
		params.put("salesItemsList", salesItemsList);

		Map saleIDMap = ConversionUtils.listBeanToMapListBean(salesItemsList,
				"sale_id");
		params.put("saleItemsMap", saleIDMap);
		params.put("saledIDs", saleIDMap.keySet());
		/*
		 * Get the primary tax amount and secondary tax amount in bill prints
		 */
		
		BasicDynaBean pri_sec_tax = BillChargeTaxDAO
				.getPrimarySecondaryPrintTaxDetailsBean(billNo,1);
		if (pri_sec_tax == null)
			params.put("primary_tax_amount", BigDecimal.ZERO);
		else
			params.put("primary_tax_amount", pri_sec_tax.getMap().get("tax_amount"));
		
		pri_sec_tax = BillChargeTaxDAO
				.getPrimarySecondaryPrintTaxDetailsBean(billNo,2);
		if (pri_sec_tax == null)
			params.put("secondary_tax_amount", BigDecimal.ZERO);
		else
			params.put("secondary_tax_amount", pri_sec_tax.getMap().get("tax_amount"));
		/*
		 * Get the Bill charge Tax details of the bill
		 */
		List<BasicDynaBean> bill_charge_tax = BillChargeTaxDAO
				.getPrintTaxChargeDetailsBean(billNo);
		params.put("billChargeTax", bill_charge_tax);
		
		Map billChargeTaxGroupMap = ConversionUtils.listBeanToMapListBean(bill_charge_tax,
				"charge_id");
		params.put("billChargeTaxGroupMap", billChargeTaxGroupMap);
		params.put("billChargeTaxGroups", billChargeTaxGroupMap.keySet());
		/*
		 * Get the charge details of the bill
		 */
		List<BasicDynaBean> charges = ChargeDAO
				.getPrintChargeDetailsBean(billNo);
		Map packageCharges = ConversionUtils
				.listBeanToMapListBean(ChargeDAO.getPackageCharges(billNo), "package_charge_id");
		params.put("charges", charges);
		params.put("packageCharges", packageCharges);

		// organize into Charge Group based map, this will result in a map
		// like REG => [bean1, bean2], DOC => [bean3, bean4], ...
		Map chargeGroupMap = ConversionUtils.listBeanToMapListBean(charges,
				"chargegroup_name");
		params.put("chargeGroupMap", chargeGroupMap);
		params.put("chargeGroups", chargeGroupMap.keySet());

		// organize into Charge Head based map, this will result in a map
		// like GREG => [bean1], LTDIA => [bean2, bean3], ...
		Map chargeHeadMap = ConversionUtils.listBeanToMapListBean(charges,
				"chargehead_name");
		params.put("chargeHeadMap", chargeHeadMap);
		params.put("chargeHeads", chargeHeadMap.keySet());

		// organize into Service Group based map, this will result in a map
		// like Direct Charge => [bean1, bean2], Laboratory => [bean3, bean4],
		// ...																																																																																																																														
		Map serviceGroupMap = ConversionUtils.listBeanToMapListBean(charges,
				"service_group_name");
		params.put("serviceGroupMap", serviceGroupMap);
		params.put("serviceGroups", serviceGroupMap.keySet());

		// organize into Service Sub Group based map, this will result in a map
		// like Dept1 => [bean1], Dept2 => [bean2, bean3], ...
		Map serviceSubGroupMap = ConversionUtils.listBeanToMapListBean(charges,
				"service_sub_group_name");
		params.put("serviceSubGroupMap", serviceSubGroupMap);
		params.put("serviceSubGroups", serviceSubGroupMap.keySet());

		/*
		 * Receipts for this bill
		 */
		List<BasicDynaBean> receipts;
		if(null != creditNoteBean ) {
			receipts= ReceiptRelatedDAO.getReceptRefundList((String) creditNoteBean.get("bill_no"));
		} else {
			receipts= ReceiptRelatedDAO.getReceptRefundList(billNo);
		}

		// organize receipts by main_type
		Map receiptTypeMap = ConversionUtils.listBeanToMapListBean(receipts,
				"payment_type");
		params.put("receiptTypeMap", receiptTypeMap);

		if (bill.get("visit_type").equals("t")) {
			BasicDynaBean incomingPatientDetails = IncomingSamplePendingBillDAO
					.getIncomingPatientDetails(visitId);
			params.put("incomingPatientDetails", incomingPatientDetails);
			if (incomingPatientDetails != null
					&& incomingPatientDetails.get("category") != null) {
				String category = (String) incomingPatientDetails
						.get("category");
				List<BasicDynaBean> sampleDetailsList = IncomingSamplePendingBillDAO
						.getSampleDetailsList(billNo, category);
				Map sampleDetailsMap = new HashMap();
				for (BasicDynaBean bean : sampleDetailsList) {
					HashMap map = new HashMap();
					map.put("orig_sample_no", bean.get("orig_sample_no"));
					map.put("sample_no", bean.get("sample_no"));
					sampleDetailsMap.put(bean.get("charge_id"), map);
				}
				params.put("sampleDetails", sampleDetailsMap);
			}
		}
		// no need of all receipt types, this list is static: R,S,F

		List<Integer> pkgIdList = new ArrayList<Integer>();
		List<String> ratePlanList = new ArrayList<String>();
		List<String> bedTypeList = new ArrayList<String>();

		/*
		 * Some totals: total bill amount, claim amount, total discounts, net
		 * payments
		 */
		BigDecimal totalAmount = BigDecimal.ZERO;
		BigDecimal totalDiscount = BigDecimal.ZERO;
		BigDecimal totalClaimAmount = BigDecimal.ZERO;
		BigDecimal roundOff = BigDecimal.ZERO;
		BigDecimal CommissionAmount = BigDecimal.ZERO;
		BigDecimal CommissionPercentage = BigDecimal.ZERO;
		boolean hasDiscounts = false;

		Connection con = null;
		BillActivityCharge activity = null;
		try {
			con = DataBaseUtil.getConnection();

			for (BasicDynaBean charge : charges) {
				String chargeStatus = (String) charge.get("status");
				if (!chargeStatus.equals("X")) {
					if (((String) charge.get("charge_head")).equals("ROF")) {
						roundOff = (BigDecimal) charge.get("amount");
					} else {
						BigDecimal chargeAmount = (BigDecimal) charge
								.get("amount");
						BigDecimal discount = (BigDecimal) charge
								.get("discount");
						BigDecimal claimAmount = (BigDecimal) charge
								.get("insurance_claim_amount");	

						totalAmount = totalAmount.add(chargeAmount);
						totalDiscount = totalDiscount.add(discount);
						totalClaimAmount = totalClaimAmount.add(claimAmount);
						if (discount.compareTo(BigDecimal.ZERO) != 0)
							hasDiscounts = true;

						BillActivityChargeDAO bacDAO = new BillActivityChargeDAO(
								con);
						activity = bacDAO.getActivity((String) charge
								.get("charge_id"));

						// Search if PKGPKG charge exists in the bill charges.
						if (((String) charge.get("charge_head"))
								.equals("PKGPKG")
								&& activity != null) {
							pkgIdList.add(new Integer((String) charge
									.get("act_description_id")));
							ratePlanList.add((String) charge.get("bill_rate_plan_id"));
							bedTypeList.add((String) patientDetails.get("bill_bed_type"));
						}
					}
				}
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

		// multivisit package items
		// the individual items are already in the biil, we just need a package
		// name to print as the header
		GenericDAO mvdao = new GenericDAO("multivisit_bills_view");
		String mvPkgId = billBo.getMultiVisitBillPackageId(billNo).toString();
		boolean isMultiVisitPkgBill = Integer.parseInt(mvPkgId) > 0;
		if (isMultiVisitPkgBill) {
			if (null != mvPkgId) {
				GenericDAO packageDao = new GenericDAO("pack_master");
				BasicDynaBean pbean = packageDao.findByKey("package_id", Integer.parseInt(mvPkgId));
				params.put("multivisitPackage", (null != pbean) ? pbean.getMap() : null);
			}
		}

		params.put("totalAmount", totalAmount);
		params.put("totalAmountWords", NumberToWordFormat.wordFormat()
				.toRupeesPaise(totalAmount));
		params.put("totalDiscount", totalDiscount);
		params.put("totalClaimAmount", totalClaimAmount);
		params.put("totalClaimAmountWords", NumberToWordFormat.wordFormat()
				.toRupeesPaise(totalClaimAmount));
		params.put("hasDiscounts", hasDiscounts);
		params.put("roundOff", roundOff);
		params.put("NumberToStringConversion", NumberToWordFormat.wordFormat());

		// Get package details
		Map pkgDetails = null;
		if (null != pkgIdList && pkgIdList.size() > 0) {
			List pkgComponentDetails = ChargeDAO
					.getPackageComponentsList(pkgIdList, ratePlanList , bedTypeList);
			pkgDetails = ConversionUtils.listBeanToMapListBean(
					pkgComponentDetails, "package_id");
		} else {
			pkgDetails = new HashMap(); // send an empty map so that prints don't error out looking for the token
		}
		params.put("packageDetailsMap", pkgDetails);

		List<BasicDynaBean> receiptsList =  new ArrayList<>();
		BigDecimal netPayments = BigDecimal.ZERO;
		for (BasicDynaBean receipt : receipts) {
			BigDecimal amt = BigDecimal.ZERO;
			boolean isDeposit = (boolean) receipt.get("is_deposit");
			if (isDeposit) {
				amt = (BigDecimal) receipt.get("allocated_amount");
			} else {
				amt = (BigDecimal) receipt.get("amount");
			}
			receipt.set("amount", amt);
			receiptsList.add(receipt);
			netPayments = netPayments.add(amt);
			if(receipt.get("credit_card_commission_amount") != null) {
				CommissionAmount = CommissionAmount.add((BigDecimal) receipt
					.get("credit_card_commission_amount"));
			} if(receipt.get("credit_card_commission_percentage") != null) {
			 CommissionPercentage = CommissionPercentage.add((BigDecimal) receipt
						.get("credit_card_commission_percentage"));
			}
		}
		params.put("receipts", receiptsList);
		params.put("netPayments", netPayments);
		params.put("CommissionAmount", CommissionAmount);
		params.put("CommissionPercentage", CommissionPercentage);
		params.put("netPaymentsWords", NumberToWordFormat.wordFormat()
				.toRupeesPaise(netPayments));

		BasicDynaBean bean = DepositsDAO.getBillDepositDetails(billNo);

		if (bean == null)
			params.put("billDeposits", BigDecimal.ZERO);
		else
			params.put("billDeposits", bean.getMap().get("deposit_set_off"));

		// test results related to this bill.
		List<BasicDynaBean> visitTestsResults = DiagnosticsDAO
				.getTestValuesForBill(billNo);
		params.put("visitTestsResults", visitTestsResults);
		params.put("seviarityColorCodes", GenericPreferencesDAO.getAllPrefs());

		// Consultations related to this bill (mainly for consultation token)
		params.put("visitConsultations", DoctorConsultationDAO
				.getVisitConsultations(visitId));
		
		
    Map<String, Map<String, BigDecimal>> caseRateCaAmtMap =
        billBo.getCaseRateAmountInBill(visitId, billNo);

    List<BasicDynaBean> visitCaseRateDetails = visitCaseRateDetDAO.getVisitCaseRateDetails(visitId);

    params.put(
        "visitCaseRateDetailsMap",
        ConversionUtils.listBeanToMapListBean(visitCaseRateDetails, "case_rate_id"));
    params.put("caseRateCategoryAmountMap", caseRateCaAmtMap);
		
		/*
		 * Process the template and get the output, return the template mode
		 */
		ftlGen.setReportParams(params);
		ftlGen.process(writer);

		return new String[] { templateMode, (String) bill.get("status"),
				downloadCType, downloadExtn, (String) bill.get("bill_printed") };
	}
	
	public static byte[] generateBillReportPDF(String report,int printerId) throws Exception{
		
		int center_id = RequestContext.getCenterId();
		BasicDynaBean printprefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId, center_id);
		HtmlConverter hc = new HtmlConverter();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
	    hc.writePdf(os, report, "BillPrint", printprefs, false, false, true, true, true, false,center_id);
	    
		return os.toByteArray();
	
	}

}
