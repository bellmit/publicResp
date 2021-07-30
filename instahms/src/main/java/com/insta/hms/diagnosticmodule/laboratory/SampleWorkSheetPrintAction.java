package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.lowagie.text.DocumentException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import flexjson.JSONSerializer;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class SampleWorkSheetPrintAction extends BaseAction {

	static public Logger log = LoggerFactory.getLogger(SampleWorkSheetPrintAction.class);

	public enum return_type {PDF, PDF_BYTES, TEXT_BYTES};
	JSONSerializer js = new JSONSerializer().exclude("class");
	PrintTemplatesDAO printTemplateDAO = new PrintTemplatesDAO();

	@IgnoreConfidentialFilters
	public ActionForward printSampleWorkSheet(ActionMapping mapping,ActionForm form,HttpServletRequest request,
			HttpServletResponse response) throws SQLException,IOException,Exception {

    String patientId = request.getParameter("patient_id");
		String actionId = request.getParameter("actionId");
		String bulkWorkSheetPrint = request.getParameter("bulkWorkSheetPrint");
		String sampleCollectionIds = request.getParameter("sampleCollectionIds");
		BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_SAMPLE_WORK_SHEET);
        String printMode = "P";
        if (printPref.get("print_mode") != null) {
            printMode = (String) printPref.get("print_mode");
        }
        String userName = (String) request.getSession(false).getAttribute("userid");

        if (printMode.equals("P")) {
            response.setContentType("application/pdf");
            OutputStream os = response.getOutputStream();
            getSampleWorkSheet(patientId, actionId, sampleCollectionIds, bulkWorkSheetPrint, return_type.PDF, printPref, os, userName, request.getParameterMap());
            os.close();

        } else {
            String textReport = new String(getSampleWorkSheet(patientId, actionId, sampleCollectionIds, bulkWorkSheetPrint,
            		return_type.TEXT_BYTES, printPref, null, userName, request.getParameterMap()));
            request.setAttribute("textReport", textReport);
            request.setAttribute("textColumns", printPref.get("text_mode_column"));
			request.setAttribute("printerType", "DMP");
            return mapping.findForward("textPrintApplet");
        }

		return null;
	}

	public byte[] getSampleWorkSheet(String patientId, String actionId, String sampleCollectionIds, String bulkWorkSheetPrint, return_type enumType, BasicDynaBean printPref,
			OutputStream os, String userName, Map requestParameters) throws SQLException, DocumentException,
			TemplateException, IOException, XPathExpressionException, TransformerException, ParseException {

		byte[] bytes = null;
		List<BasicDynaBean> normalResultsList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> internalLabResultsList = new ArrayList<BasicDynaBean>();
		List internalLabTestIdsList = new ArrayList();
		List normalTestIdsList = new ArrayList();

		List<BasicDynaBean> testDetails = LaboratoryDAO.getWorkSheetDetailsList(patientId, actionId, sampleCollectionIds, bulkWorkSheetPrint);
		
		for(int i=0; i<testDetails.size(); i++) {
			BasicDynaBean testDetailsBean = testDetails.get(i);
			String testId = (String) testDetailsBean.get("test_id");
			String cflag = (String) testDetailsBean.get("cflag");
			int prescId = (Integer) testDetailsBean.get("prescribed_id");
			int conductionCenterId = LaboratoryDAO.getConductionCenterId(testId, prescId);

			//skip to add the duplicate results label of the same test in the lists
			if (conductionCenterId == 0) {
				if(normalTestIdsList.contains(testId)) {
					continue;
				} else {
					normalTestIdsList.add(testId);
				}				
			} else {
				if(internalLabTestIdsList.contains(testId)) {
					continue;
				} else {
					internalLabTestIdsList.add(testId);
				}				
			}
			//for outhouse sample we take conduction center id as logged in center			
			if (conductionCenterId == 0 && patientId == null)  
				conductionCenterId = RequestContext.getCenterId();
			else if (conductionCenterId == 0) 
				conductionCenterId = VisitDetailsDAO.getCenterId(patientId);

			List<BasicDynaBean> resultsList = LaboratoryDAO.getTestResults(patientId, sampleCollectionIds, bulkWorkSheetPrint, testId, conductionCenterId);
			if (cflag != null && cflag.equals("N")) {
				normalResultsList.addAll(resultsList);				
			} else {
				internalLabResultsList.addAll(resultsList);				
			}
		}
				
		PrintTemplate template = PrintTemplate.SampleWorkSheet;
        String templateContent = printTemplateDAO.getCustomizedTemplate(template);

        Template t = null;
        if(templateContent == null || templateContent.equals("")){
       		t = AppInit.getFmConfig().getTemplate(template.getFtlName() + ".ftl");
        }else{
        	StringReader reader = new StringReader(templateContent);
        	t = new Template(null, reader, AppInit.getFmConfig());
        }

		Map ftlParams = new HashMap();
		Map modulesActivatedMap = ((Preferences) RequestContext.getSession().getAttribute("preferences")).getModulesActivatedMap();
    ftlParams.put("requestArguments", requestParameters);
		ftlParams.put("modules_activated", modulesActivatedMap);
		ftlParams.put("testDetails", testDetails);
    Map testDetailsGroupMap = ConversionUtils.listBeanToMapListBean(testDetails, "sample_no");
    ftlParams.put("testDetailsSampleNoGroups", testDetailsGroupMap.keySet());
    ftlParams.put("testDetailsGroupMap", testDetailsGroupMap);
    ftlParams.put("testDetailsSampleNoGroups", testDetailsGroupMap.keySet());
    Map testDetailsDeptAndStatusGroupMap = ConversionUtils.listBeanToMapMapListMap(testDetails, "ddept_name", "conducted");
    ftlParams.put("testDetailsDeptAndStatusGroupMap", testDetailsDeptAndStatusGroupMap);

		Map testResultsTestIdGroupMap = ConversionUtils.listBeanToMapListBean(normalResultsList, "test_id");
		ftlParams.put("testResultsTestIdGroupMap", testResultsTestIdGroupMap);
		ftlParams.put("testResultsTestIdGroup", testResultsTestIdGroupMap.keySet());

		Map internalLabResultsTestIdGroupMap = ConversionUtils.listBeanToMapListBean(internalLabResultsList, "test_id");
		ftlParams.put("internalLabResultsTestIdGroupMap", internalLabResultsTestIdGroupMap);
		ftlParams.put("internalLabResultsTestIdGroup", internalLabResultsTestIdGroupMap.keySet());

		StringWriter writer = new StringWriter();
		try {
			t.process(ftlParams, writer);
		} finally {
			log.debug("Exception raised while processing the patient header for patient Id : "+patientId);
		}
		HtmlConverter hc = new HtmlConverter();
		StringBuilder printContent = new StringBuilder();
		printContent.append(writer.toString());

		Boolean repeatPHeader = ((String)printPref.get("repeat_patient_info")).equals("Y");
		if (enumType.equals(return_type.PDF)) {
			hc.writePdf(os, printContent.toString(), "Sample Work Sheet", printPref, false,
					repeatPHeader, true, true, true, false);
			os.close();

		} else if (enumType.equals(return_type.PDF_BYTES)) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			hc.writePdf(stream, printContent.toString(), "Sample Work Sheet", printPref, false,
					repeatPHeader, true, true, true, false);
			bytes = stream.toByteArray();
			stream.close();

		} else if (enumType.equals(return_type.TEXT_BYTES)) {
			bytes = hc.getText(printContent.toString(), "Sample Work Sheet", printPref, true, true);

		} else {

		}
		return bytes;
	}

}

