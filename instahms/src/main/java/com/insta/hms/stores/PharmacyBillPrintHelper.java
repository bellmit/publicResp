package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.NumberToWordFormat;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDetails;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.master.CountryMaster.CountryMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.StoreMaster.StoreMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class PharmacyBillPrintHelper {

	static Logger log = LoggerFactory.getLogger(PharmacyBillPrintHelper.class);
	
	public static String processPharmacyBillTemplate(HttpServletRequest req,BasicDynaBean main,
			String saleId,String billNo,Map paramMap, StringWriter writer, boolean isWebSharing) throws Exception{
		BillBO bo = new BillBO();
		RetailCustomerDAO rDao = new RetailCustomerDAO();
		BillDetails billDetails = bo.getBillDetails(billNo);
		CountryMasterDAO countryMasterDAO = new CountryMasterDAO(); 
		GenericDAO govtIdMaster = new GenericDAO("govt_identifier_master");
		
		paramMap.put("bill", billDetails.getBill());
		paramMap.put("refund", billDetails.getRefunds());
		paramMap.put("receipt", billDetails.getReceipts());
		paramMap.put("sales_remarks", billDetails.getCharges()== null? "": billDetails.getCharges().get(0)==null? "":((ChargeDTO)billDetails.getCharges().get(0)).getUserRemarks());

		String visitType = billDetails.getBill().getVisitType();
		String visitId = billDetails.getBill().getVisitId();


		if (visitType.equals(Bill.BILL_VISIT_TYPE_RETAIL)) {
			paramMap.put("customer", rDao.getRetailCustomer(visitId));
		} else {
			paramMap.put("patient", VisitDetailsDAO.getPatientVisitDetailsBean(visitId));
			paramMap.put("allocated_ward_for_sale",  MedicineSalesDAO.getWardNameForHospiatlPatient(req.getParameter("saleId")));
		}
		//Used to set country name and goverment id type.
		if (visitType.equals(Bill.BILL_VISIT_TYPE_RETAIL)) {
			Map customerBean = new HashMap(((BasicDynaBean)paramMap.get("customer")).getMap());
			//Set country Name.
			if(customerBean.get("nationality_id") != null) {
				BasicDynaBean countryBean = countryMasterDAO.findByKey("country_id", customerBean.get("nationality_id"));
				if(countryBean != null)
					customerBean.put("nationality_name", countryBean.get("country_name"));
			}
			//Set govt. id
			if(customerBean.get("identifier_id") != null) {
				BasicDynaBean govtIdBean = govtIdMaster.findByKey("identifier_id", customerBean.get("identifier_id"));
				if(govtIdBean != null)
					customerBean.put("govt_type", govtIdBean.get("remarks"));
			}
			paramMap.put("customer", customerBean);
			
		}
		
		List<BasicDynaBean> saleDetails = MedicineSalesDAO.getSalesList1(saleId);
		paramMap.put("items",  saleDetails);
		
		paramMap.put("taxLabel", GenericPreferencesDAO.getPrefsBean().getMap());

		List<BasicDynaBean> saleAgtRetDetails = MedicineSalesDAO.getSalesAgtRetList(saleId);
		paramMap.put("returns",  saleAgtRetDetails);
		
		/** Taxation Details */
		List<BasicDynaBean> itemsSaleTaxDetails = MedicineSalesDAO.getItemsSaleTaxDetails(saleId);
		paramMap.put("itemsSaleTaxDetails",  itemsSaleTaxDetails);
		BigDecimal taxTotal = BigDecimal.ZERO;
		for (BasicDynaBean istd : itemsSaleTaxDetails) {
			BigDecimal taxAmt = (BigDecimal) istd.get("tax_amt") == null ? BigDecimal.ZERO : (BigDecimal) istd.get("tax_amt");
			taxTotal = taxTotal.add(taxAmt);
		}
		paramMap.put("taxTotal", taxTotal);
		
		List<BasicDynaBean> itemsReturnTaxDetails = MedicineSalesDAO.getItemsReturnTaxDetails(saleId);
		paramMap.put("itemsReturnTaxDetails",  itemsReturnTaxDetails);
		
		List<BasicDynaBean> claimSaleTaxDetails = MedicineSalesDAO.getClaimSaleTaxDetails(saleId);
		paramMap.put("claimSaleTaxDetails",  claimSaleTaxDetails);
		
		List<BasicDynaBean> claimReturnTaxDetails = MedicineSalesDAO.getClaimReturnTaxDetails(saleId);
		paramMap.put("claimReturnTaxDetails",  claimReturnTaxDetails);
		 
		/** Code not in Use. Once taxation code completes then we will see how to use these methods */
		/*List<BasicDynaBean> salesTaxDetails = MedicineSalesDAO.getSalesTaxDetails(saleId);
		paramMap.put("salesTaxDetails",  salesTaxDetails);
		List<BasicDynaBean> salesReturnTaxDetails = MedicineSalesDAO.getSalesReturnTaxDetails(saleId);
		paramMap.put("salesReturnTaxDetails",  salesReturnTaxDetails);
		*/

		HashMap<String, BigDecimal> retvatDetails = new HashMap<String, BigDecimal>();
		BigDecimal totalRetAmt = BigDecimal.ZERO;
		for (BasicDynaBean b: saleAgtRetDetails) {
			String rate = b.get("tax_rate").toString();
			BigDecimal taxAmt = (BigDecimal) b.get("tax");
			BigDecimal totalTax = retvatDetails.get(rate);
			if (totalTax == null) {
				retvatDetails.put(rate, taxAmt);
			} else {
				retvatDetails.put(rate, taxAmt.add(totalTax));
			}
			totalRetAmt = totalRetAmt.add((BigDecimal)b.get("amount"));
		}
		paramMap.put("retvatDetails",  retvatDetails);

		/*
		 * Calculate the VAT for each rate of VAT, also check if discounts are being used
		 */
		HashMap<String, BigDecimal> vatDetails = new HashMap<String, BigDecimal>();
		boolean hasDiscounts = false;
		BigDecimal totalSaleAmt = BigDecimal.ZERO;
		BigDecimal totalBillDiscount = BigDecimal.ZERO;
		BigDecimal roundoff = BigDecimal.ZERO;
		BigDecimal priSponsorTaxAmt = BigDecimal.ZERO;
		BigDecimal secSponsorTaxAmt = BigDecimal.ZERO;
		BigDecimal totalPriSponsorTaxAmt = BigDecimal.ZERO;
		BigDecimal totalSecSponsorTaxAmt = BigDecimal.ZERO;
		BigDecimal patientTaxAmt = BigDecimal.ZERO;
		BigDecimal totalPatientTaxAmt = BigDecimal.ZERO;
		for (BasicDynaBean b: saleDetails) {
			String rate = b.get("tax_rate").toString();
			BigDecimal taxAmt = (BigDecimal) b.get("tax");
			
			priSponsorTaxAmt = (BigDecimal) ((BigDecimal)b.get("pri_sponsor_tax_amt") == null ? BigDecimal.ZERO : (BigDecimal)b.get("pri_sponsor_tax_amt"));
			secSponsorTaxAmt = (BigDecimal) ((BigDecimal)b.get("sec_sponsor_tax_amt") == null ? BigDecimal.ZERO : (BigDecimal)b.get("sec_sponsor_tax_amt"));
			totalPriSponsorTaxAmt = totalPriSponsorTaxAmt.add(priSponsorTaxAmt);
			totalSecSponsorTaxAmt = totalSecSponsorTaxAmt.add(secSponsorTaxAmt);
			
			patientTaxAmt = (taxAmt.subtract(priSponsorTaxAmt.add(secSponsorTaxAmt)));
			totalPatientTaxAmt = totalPatientTaxAmt.add(patientTaxAmt);
			
			BigDecimal totalTax = vatDetails.get(rate);
			if (totalTax == null) {
				vatDetails.put(rate, taxAmt);
			} else {
				vatDetails.put(rate, taxAmt.add(totalTax));
			}
			BigDecimal discount = (BigDecimal) b.get("discount");
			if (discount.compareTo(BigDecimal.ZERO) != 0)
				hasDiscounts = true;
			totalSaleAmt = totalSaleAmt.add((BigDecimal)b.get("amount"));

			BigDecimal bill_discount = (BigDecimal) b.get("bill_discount");
			totalBillDiscount = bill_discount;
			roundoff = (BigDecimal) b.get("round_off");
		}
		
		paramMap.put("totalPriSponsorTaxAmt", totalPriSponsorTaxAmt);
		paramMap.put("totalSecSponsorTaxAmt", totalSecSponsorTaxAmt);
		paramMap.put("totalPatientTaxAmt", totalPatientTaxAmt);

		Bill bill = billDetails.getBill();
		BigDecimal netAmount = BigDecimal.ZERO;
		netAmount = totalSaleAmt.subtract(totalBillDiscount).subtract(bill.getDepositSetOff()).add(roundoff).add(totalRetAmt);
		paramMap.put("netAmount", netAmount);
		paramMap.put("netAmountWords", NumberToWordFormat.wordFormat().toRupeesPaise(netAmount));
		paramMap.put("saleAmtWords", NumberToWordFormat.wordFormat().toRupeesPaise(totalSaleAmt));
		paramMap.put("retAmtWords", NumberToWordFormat.wordFormat().toRupeesPaise(totalRetAmt));
		paramMap.put("billDiscountWords", NumberToWordFormat.wordFormat().toRupeesPaise(totalBillDiscount));
		paramMap.put("vatDetails",  vatDetails);
		paramMap.put("hasDiscounts",  hasDiscounts);
		paramMap.put("NumberToStringConversion", NumberToWordFormat.wordFormat());

		if(saleDetails != null && saleDetails.size() > 0)
			paramMap.put("doctorName", saleDetails.get(0).get("doctor_name"));
		else
			paramMap.put("doctorName", "");

		paramMap.put("duplicate", Boolean.parseBoolean(req.getParameter("duplicate")));

		// store wise template selection
		int storeId = (Integer) main.get("store_id");
		BasicDynaBean store = new StoreMasterDAO().findByKey("dept_id", storeId);
		String templateName=(String) main.get("template_name");;
		if (isWebSharing == false) {
			templateName = (String) store.get("template_name");
		} else{
			templateName = (String) store.get("web_template_name");
		}
//		int storeId = (Integer) main.get("store_id");
//		BasicDynaBean store = new StoreMasterDAO().findByKey("dept_id", storeId);
		String templateMode = null;
		String templateContent = null;
		FtlReportGenerator ftlGen = null;

		if (templateName == null || templateName.equals("") || templateName.equals("BUILTIN_HTML")) {
			ftlGen = new FtlReportGenerator("PharmacySalesPrint");
			templateMode = "H";
		} else if (templateName.equals("BUILTIN_TEXT")) {
			ftlGen = new FtlReportGenerator("PharmacySalesTextPrint");
			templateMode = "T";
		} else {
			if (isWebSharing) {
				GenericDAO templateDao = new GenericDAO("store_print_template");
				BasicDynaBean tmpBean = templateDao.findByKey("template_name",templateName);

				if (tmpBean == null) {
					// couldn't find the template in the db, bail out with error.
					return null;
				} else if (tmpBean.get("pharmacy_template_content") == null
						|| ((String) tmpBean.get("pharmacy_template_content")).trim().isEmpty()) {
					templateMode = (String) tmpBean.get("template_mode");

					String builtinTemplateName = (templateMode.equals("H")) ? "PharmacySalesPrint" : "PharmacySalesTextPrint";
					ftlGen = new FtlReportGenerator(builtinTemplateName);

					Connection con = null;
					int success = 0;
					FileInputStream fis = null;
					try {
						BasicDynaBean bean = templateDao.getBean();
						con = DataBaseUtil.getConnection();
						con.setAutoCommit(false);
						bean.set("template_name", templateName);
						String realPath = RequestContext.getRequest().getServletContext().getRealPath("");
						fis = new FileInputStream(new File(realPath + "/WEB-INF/templates/" + builtinTemplateName + ".ftl"));
						bean.set("pharmacy_template_content", new String(DataBaseUtil.readInputStream(fis)));
						Map<String, String> additionalDetailsKeys = new HashMap<String, String>();
						additionalDetailsKeys.put("template_name", templateName);
						success = templateDao.update(con, bean.getMap(),additionalDetailsKeys);
					} finally {
						DataBaseUtil.commitClose(con, success > 0);
					}

				}
			} 
			String templateCodeQuery = "SELECT pharmacy_template_content, template_mode FROM store_print_template "
					+ " WHERE template_name=?";
			List printTemplateList = DataBaseUtil.queryToDynaList(
					templateCodeQuery, templateName);
			for (Object obj : printTemplateList) {
				BasicDynaBean templateBean = (BasicDynaBean) obj;
				templateContent = (String) templateBean
						.get("pharmacy_template_content");
				log.debug("templateContent=" + templateContent);
				templateMode = (String) templateBean.get("template_mode");
			}

			StringReader reader = new StringReader(templateContent);
			ftlGen = new FtlReportGenerator("CustomTemplate", reader);
		}
		ftlGen.setReportParams(paramMap);
		ftlGen.process(writer);

		return templateMode;
		
	}
	
	public static String processPharmacyBillTemplate(HttpServletRequest req,BasicDynaBean main,
			String saleId,String billNo,Map paramMap, StringWriter writer) throws Exception{
		
		return processPharmacyBillTemplate( req, main, saleId, billNo, paramMap,  writer, false);

		
	}
}