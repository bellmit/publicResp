package com.insta.hms.billing;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import freemarker.template.utility.StringUtil;
import net.sf.jasperreports.engine.JRException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.diag.incomingsamplependingbills.IncomingSamplePendingBillDAO;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PrintPageOptions;
import com.insta.hms.common.ReportPrinter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.master.BillPrintTemplate.BillPrintTemplateDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.stores.MedicineSalesDAO;
import com.insta.hms.stores.PharmacyBillPrintHelper;
import com.insta.hms.stores.RetailCustomerDAO;

public class BillsBackupAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(BillsBackupAction.class);

	@IgnoreConfidentialFilters
	public ActionForward getBillsBackupScreen(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws  Exception {

		req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		BillPrintTemplateDAO templateDao= new BillPrintTemplateDAO();
		req.setAttribute("templateList", templateDao.getBillTemplateList());
		BasicDynaBean printPref = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_BILL);
		req.setAttribute("pref",printPref);

		return mapping.findForward("show");
	}

	@IgnoreConfidentialFilters
	public ActionForward getBillsBackup(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws  Exception {
        Date fromDate =  null;
        Date toDate = null;
        try {
          fromDate = DateUtil.parseDate(req.getParameter("fromDate"));
        	toDate = DateUtil.parseDate(req.getParameter("toDate"));
        } catch (IllegalArgumentException iExp) {
			FlashScope flash = FlashScope.getScope(req);
        	ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			flash.put("error", "Provided from-date or to-date is not valid.");
			return redirect;
        }
		String typeFilter = req.getParameter("billType");
		String patientType = req.getParameter("patientType");
		String printerIdStr = req.getParameter("printerType");

		HttpSession session = req.getSession();
		String userId = (String) session.getAttribute("userid");

		String billNowPrintFormat = req.getParameter("printBillBn");
		String billLaterPrintFormat = req.getParameter("printBillBl");

		List<BasicDynaBean> list = BillDAO.getFinalizedBillList(typeFilter, patientType, fromDate, toDate);

		if (list.size()==0) {
			FlashScope flash = FlashScope.getScope(req);
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			flash.put("info", "There are no bills matching the given selection criteria.");
			return redirect;
		}

	    String zipFileName="Bills_"+req.getParameter("fromDate")+"-"+req.getParameter("toDate")+".zip";
		res.setHeader("Content-Disposition", "attachment; filename="+zipFileName);
		res.setContentType("application/zip");

		OutputStream os=res.getOutputStream();
		ZipOutputStream zout = new ZipOutputStream(os);

		int printerId = 0;
		if ((printerIdStr !=null) && !printerIdStr.equals("")) {
			printerId = Integer.parseInt(printerIdStr);
		}

		if (printerId == 0) {
			FlashScope flash = FlashScope.getScope(req);
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			flash.put("error", "No printer chosen.");
			return redirect;
		}

		BasicDynaBean printPref =
			PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_BILL,	printerId);
		PrintPageOptions opts = new PrintPageOptions(printPref);

		HtmlConverter hc = new HtmlConverter();
		Map params = new HashMap();
		BillBO bo = new BillBO();

		try {
			for (BasicDynaBean billBean:list) {

				String billType = (String) billBean.get("bill_type");
				String billNo = (String) billBean.get("bill_no");
				String visitType=(String) billBean.get("visit_type");

				zout.putNextEntry(new ZipEntry(billNo + "_" + billBean.get("mr_no").toString() + ".pdf"));

				String format = billType.equals("P") ? billNowPrintFormat : billLaterPrintFormat;

				if (format.startsWith("CUSTOM") && !visitType.equals("t") ) {
					String templateName = format.split("-", 2)[1];	// anything after CUSTOM- is name of template

					StringWriter writer = new StringWriter();
					String[] retVals = BillPrintHelper.processBillTemplate(writer, billNo,
							templateName, userId);
					String templateMode = retVals[0];
					boolean isFinalized = !retVals[1].equals("A");
					boolean isDuplicate = !retVals[4].equals("N");

					String billContent = writer.toString();

					if (templateMode.equals("T")){
						// convert text to PDF
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						hc.textToPDF(billContent, bos, printPref);
						bos.writeTo(zout);
					} else {
						// convert html to PDF
						zout.write(hc.getPdfBytes(billContent,"Provisional Bill", printPref,false,false,true,
									isFinalized,isDuplicate));
					}

				} else if (!visitType.equals("t")) {
					String[] components = format.split("-",3);    // eg, BILL-DET-ALL

					params.put("printerId", printerId);
					params = BillPrintHelper.getBillJrxmlParams(billNo, components[1], components[2], userId);
					ReportPrinter.printPdfStream(zout, "BillPrint", params, opts);
					res.setContentType("application/pdf");

				} else if (visitType.equals("t")) {
					String visitId=(String)billBean.get("visit_id");
					String category=(String)billBean.get("category");

					BillDetails billDetails = bo.getBillDetails(billNo);

					params.put("sampleDetails",
							IncomingSamplePendingBillDAO.getSampleDetailsList(billNo, category));
					params.put("billDetails", billDetails);
					params.put("patientDetails",
							IncomingSamplePendingBillDAO.getIncomingPatientDetails(visitId));
					params.put("printMode", "P");
					params.put("billNo", billNo);

				//	Template t = null;
					FtlReportGenerator ftlGen = null;
				//	t = AppInit.getFmConfig().getTemplate("IncomingSampleBill.ftl");
					ftlGen = new  FtlReportGenerator("IncomingSampleBill");

					StringWriter writer = new StringWriter();
				//	t.process(params, writer);
					ftlGen.setReportParams(params);
					ftlGen.process(writer);
					String templateContent = writer.toString();

					zout.write(hc.getPdfBytes(templateContent, "Incoming Sample Bill",
								printPref,false,false,true, true,false));

				}
			}
			zout.closeEntry();
			zout.close();
			os.flush();
			os.close();
			return null;
		} catch (JRException e) {
			if (DataBaseUtil.isReportDesignInvalid(e)) {
				String error = "Unable to generate the print: " +
					" please check the report margins in print definition "+
					" and ensure that the page width/height are sufficient to accomodate the report";
				logger.warn("Unable to print bill: ", e);
				FlashScope flash = FlashScope.getScope(req);
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				flash.put("error", error);
				return redirect;

			} else {
				throw(e);
			}
		}
	}

	@IgnoreConfidentialFilters
	public ActionForward  getPharmacyBillsBackUp(ActionMapping am,ActionForm af,HttpServletRequest
			req,HttpServletResponse res) throws Exception  {

		java.sql.Date fromDate = DataBaseUtil.parseDate(req.getParameter("fromDate"));
		java.sql.Date toDate = DataBaseUtil.parseDate(req.getParameter("toDate"));
		String printerIdStr = req.getParameter("printerType");

		HttpSession session = req.getSession();
		String userId = (String) session.getAttribute("userid");

		List<BasicDynaBean> list = BillDAO.getPharmacyRetailFinalizedBillList(fromDate, toDate);

		if (list.size()==0) {
			FlashScope flash = FlashScope.getScope(req);
			ActionRedirect redirect = new ActionRedirect(am.findForward("showRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			flash.put("info", "There are no bills matching the given selection criteria.");
			return redirect;
		}

		int printerId = 0;
		if ((printerIdStr !=null) && !printerIdStr.equals("")) {
			printerId = Integer.parseInt(printerIdStr);
		}

		if (printerId == 0) {
			FlashScope flash = FlashScope.getScope(req);
			ActionRedirect redirect = new ActionRedirect(am.findForward("showRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			flash.put("error", "No printer chosen.");
			return redirect;
		}

		BasicDynaBean printprefs = PrintConfigurationsDAO.
			getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PHARMACY,printerId);

		OutputStream os=res.getOutputStream();
		ZipOutputStream zout = new ZipOutputStream(os);

		String zipFileName="Bills_"+req.getParameter("fromDate")+"-"+req.getParameter("toDate")+".zip";
		res.setHeader("Content-Disposition", "attachment; filename="+zipFileName);
		res.setContentType("application/zip");

		HtmlConverter hc = new HtmlConverter();
		Map params = new HashMap();
		BillBO bo = new BillBO();
		RetailCustomerDAO rDao = new RetailCustomerDAO();

		for (BasicDynaBean billBean:list) {

			String billNo=(String)billBean.get("bill_no");
			String saleId=(String)billBean.get("sale_id");

			zout.putNextEntry(new ZipEntry(saleId + "_" + billBean.get("mr_no").toString() + ".pdf"));

			BasicDynaBean saleMain = MedicineSalesDAO.getSalesMain(saleId);
			params.put("sale", saleMain);

			StringWriter writer = new StringWriter();

			String templateMode=PharmacyBillPrintHelper.processPharmacyBillTemplate(req, saleMain, saleId,
					billNo, params,writer);

			String printContent = writer.toString();

			if (templateMode.equals("T")){
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				hc.textToPDF(printContent, bos, printprefs);
				bos.writeTo(zout);
			}else{
				boolean isDuplicate = Boolean.parseBoolean(req.getParameter("duplicate"));
				zout.write(hc.getPdfBytes(printContent,"Pharmacy Bill", printprefs,false,false,true, true,isDuplicate));
			}
		}
		zout.closeEntry();
		zout.close();
		os.flush();
		os.close();
		return null;
	}

}

