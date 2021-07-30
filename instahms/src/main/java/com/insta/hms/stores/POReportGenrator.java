package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.NumberToWordFormat;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class POReportGenrator{
	
	static Logger log = LoggerFactory.getLogger(POReportGenrator.class);

	public String generatePOReport(String templateName,String[] po_No,String printType)throws Exception{
		HashMap<String,Object> params = new HashMap<String, Object>();
		
		String poNo = po_No[0];
		String indentNo="";
		String indentDate="";
		String indentCenter = "";
		
		Connection con = null;
		StringWriter writer = new StringWriter();
		
		try{

			con = DataBaseUtil.getReadOnlyConnection();
			GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
			List<BasicDynaBean>poOrderList=PurchaseOrderDAO.getPurchaseOrderList(con,poNo);
			List<BasicDynaBean> indentDetails=PurchaseOrderDAO.getIndentNo(poNo);
			BasicDynaBean poBean = new GenericDAO("store_po_main").findByKey(con, "po_no", poNo);
			BasicDynaBean suppBean = PurchaseOrderDAO.getSupplierDetails(con,poNo);
			String printerId = printType;
			
			params.put("items", poOrderList);
			params.put("hospital_tin",dto.getHospitalTin());
			params.put("hospital_pan",dto.getHospitalPan());
			params.put("hospital_service_regn_no",dto.getHospitalServiceRegnNo());
			params.put("poBean", poBean);
			params.put("suppBean", suppBean);
			BasicDynaBean deptBean = new GenericDAO("department").findByKey("dept_id", poBean.get("dept_id"));
			params.put("dept_name", deptBean != null ? deptBean.get("dept_name") : null);
			params.put("NumberToStringConversion", NumberToWordFormat.wordFormat());
			if(indentDetails != null &&  indentDetails.size()>0){
				Iterator it = indentDetails.iterator();
				while(it.hasNext()){
					BasicDynaBean bean = (BasicDynaBean) it.next();
					indentNo=indentNo+bean.get("indent_no").toString()+',';
					indentDate=indentDate+bean.get("indent_date").toString()+',';
					indentCenter = indentCenter+bean.get("indent_center_name")+',';
					//authDate=(Timestamp) bean.get("approved_time");
				}
				indentNo=indentNo.substring(0, indentNo.length()-1);
				indentDate=indentDate.substring(0, indentDate.length()-1);
				params.put("indentNo",indentNo);
				params.put("indentDate",indentDate);
				params.put("indentCenter", indentCenter);
				log.debug("indentCenter****"+indentCenter);
				//params.put("auth_date",authDate);
	
			}
			HashMap<String, BigDecimal> vatDetails = new HashMap<String, BigDecimal>();
			for (BasicDynaBean b: poOrderList) {
				String rate = b.get("vat_rate").toString();
				BigDecimal taxAmt = (BigDecimal) b.get("vat");
				BigDecimal totalTax = vatDetails.get(rate);
				if (totalTax == null) {
					vatDetails.put(rate, taxAmt);
				} else {
					vatDetails.put(rate, taxAmt.add(totalTax));
				}
			}
			params.put("vatDetails",  vatDetails);
			FtlReportGenerator ftlGen=null;
			String templateMode = null;
			String templateContent = null;
			
			/*BasicDynaBean printprefs = null;
			
			if ( printerId != null )
				printprefs = PrintConfigurationsDAO.getPageOptions(
							PrintConfigurationsDAO.PRINT_TYPE_STORE, Integer.parseInt(printerId));
			if ( printprefs == null )
				printprefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_STORE);
	
			
			FtlReportGenerator ftlGen=null;
			
			PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
			String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.approve_indent_print);
			try{
				if (templateContent == null || templateContent.equals("")) {
					ftlGen = new FtlReportGenerator(templateName.getFtlName());
				}else{
					StringReader reader = new StringReader(templateContent);
					ftlGen = new FtlReportGenerator(templateName.getFtlName(),reader);
				}*/
			
			if (templateName == null || templateName.equals("") || templateName.equals("BUILTIN_HTML")) {
				//	t = AppInit.getFmConfig().getTemplate("PurchaseOrderPrint.ftl");
					ftlGen = new FtlReportGenerator("PurchaseOrderPrint");
					templateMode = "H";
				} else if (templateName.equals("BUILTIN_TEXT")) {
				//	t = AppInit.getFmConfig().getTemplate("PurchaseOrderTextPrint.ftl");
					ftlGen = new FtlReportGenerator("PurchaseOrderTextPrint");
					templateMode = "T";
				} else {
					String templateCodeQuery =
						"SELECT pharmacy_template_content, template_mode FROM po_print_template " +
						" WHERE template_name=?";
					List printTemplateList  = DataBaseUtil.queryToDynaList(templateCodeQuery,templateName);
					for (Object obj: printTemplateList){
						BasicDynaBean templateBean = (BasicDynaBean) obj;
						templateContent = (String)templateBean.get("pharmacy_template_content");
						log.debug("templateContent="+ templateContent);
						templateMode = (String) templateBean.get("template_mode");
					}

					StringReader reader = new StringReader(templateContent);
				//	t = new Template("CustomTemplate.ftl", reader, AppInit.getFmConfig());
					ftlGen = new FtlReportGenerator("CustomTemplate",reader);
				}

		        ftlGen.setReportParams(params);
		        ftlGen.process(writer);
			}catch(Exception e){
				log.debug("POReportGenrator class Exception=="+e);
			}
     
		 finally{
			DataBaseUtil.closeConnections(con, null);
		}
		
		return writer.toString();
    }
	
	public static byte[] generatePOReportPDF(String report,int printerId) throws Exception{
		
		int center_id = RequestContext.getCenterId();
		BasicDynaBean printprefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PHARMACY,printerId, center_id);
		HtmlConverter hc = new HtmlConverter();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
	    hc.writePdf(os, report, "PurchaseOrderPrint", printprefs, false, false, true, true, true, false,center_id);
	    
		return os.toByteArray();
	
	}
}