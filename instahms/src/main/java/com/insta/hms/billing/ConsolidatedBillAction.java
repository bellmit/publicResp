package com.insta.hms.billing;

import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.master.MasterAction;
import com.insta.hms.master.MasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

/**
 * @author prasanna.kumar
 *
 */

public class ConsolidatedBillAction extends MasterAction{
	
	static Logger log = LoggerFactory.getLogger(ConsolidatedBillAction.class);
	static ConsolidatedBillDAO consolidatedbillDao = new ConsolidatedBillDAO();	
	
	@Override
	public MasterDAO getMasterDao() {
		return consolidatedbillDao;
	}
	
	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping mapping,ActionForm form,
			HttpServletRequest req,HttpServletResponse resp) throws Exception{
		String consolidatedBillNo = req.getParameter("consolidated_bill_no");	
		
		req.setAttribute("cnsldtdBillDetBean",consolidatedbillDao.findByKey("consolidated_bill_no",consolidatedBillNo));
		
		List<BasicDynaBean> consolidatedBillList = consolidatedbillDao.getConsolidatedBillList(consolidatedBillNo); 		
		req.setAttribute("consolidatedBillList", ConversionUtils.listBeanToListMap(consolidatedBillList));
		
		BasicDynaBean consolidatedBillTotals = consolidatedbillDao.getConsolidatedBillTotals(consolidatedBillNo);			
		req.setAttribute("consolidatedBillTotals", consolidatedBillTotals.getMap());
		
		req.setAttribute("availableTemplateList", consolidatedbillDao.getAvailableTemplateList());
		
		return mapping.findForward("addshow");
	}
	
	public ActionForward printConsolidatedBill(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse resp)throws Exception {
		
		String sponsorId = req.getParameter("sponsor_id");
		String consolidatedBillNo = req.getParameter("consolidated_bill_no");
		
		HashMap map = new HashMap();
		
		BasicDynaBean pref= null;
		pref =  PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_BILL,0);
		
		String templateName = req.getParameter("template_name");
		String templateMode = null;
		String templateContent = null;
	//	Template t = null;
		FtlReportGenerator ftlGen = null;
		map.put("sponsor_id", sponsorId);
		map.put("consolidated_bill_no", consolidatedBillNo);
		map.put("format","pdf");
		
		String templateCodeQuery =
			"SELECT bill_template_content, template_mode FROM bill_print_template " +
			" WHERE template_name=?";
		List printTemplateList  = DataBaseUtil.queryToDynaList(templateCodeQuery,templateName);
		for (Object obj: printTemplateList){
			BasicDynaBean templateBean = (BasicDynaBean) obj;
			templateContent = (String)templateBean.get("bill_template_content");
			templateMode = (String) templateBean.get("template_mode");
		}
		StringReader reader = new StringReader(templateContent);
	
		ftlGen = new FtlReportGenerator("Custom Template",reader);
			
		StringWriter writer = new StringWriter();

		ftlGen.setReportParams(map);
		ftlGen.process(writer);
		String printContent = writer.toString();

		HtmlConverter hc = new HtmlConverter();
		if (pref.get("print_mode").equals("P")) {
			OutputStream os = resp.getOutputStream();
			resp.setContentType("application/pdf");
			try {
				if (templateMode!= null &&  templateMode.equals("T")){
					hc.textToPDF(printContent, os, pref);
				}else{
					hc.writePdf(os, printContent, "Consolidated Bill Print", pref, false, false, true, true, true, false);
				}
			} catch (Exception e) {
				resp.reset();
				log.error("Original Template:");
				log.error(templateContent);
				log.error("Generated HTML content:");
				log.error(printContent);
				throw(e);
			}
			os.close();

			return null;
		} else {
			String textReport = null;
			//text mode
			if (templateMode !=null && templateMode.equals("T")){
				textReport = printContent;
			}else{
				textReport = new String(hc.getText(printContent, "Consolidated Bill Print", pref, true, true));
			}
			req.setAttribute("textReport", textReport);
			req.setAttribute("textColumns", pref.get("text_mode_column"));
		   req.setAttribute("printerType", "DMP");
			return mapping.findForward("textPrintApplet");
		}
	}
}
