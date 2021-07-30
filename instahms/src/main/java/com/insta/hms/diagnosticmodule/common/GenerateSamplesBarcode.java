package com.insta.hms.diagnosticmodule.common;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.master.SampleBarcodePrintTemplate.SampleBarcodePrintTemplateDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GenerateSamplesBarcode extends DispatchAction {
	static Logger logger = LoggerFactory.getLogger(GenerateSamplesBarcode.class);
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException,SQLException, ParseException, TemplateException, DocumentException {

		String plainText = null;
		String templateContent = null;
		FtlReportGenerator fGen = null;
		String mrNo = null;

		//		 Samplecollection barcode
		String sampleNos = request.getParameter("sampleNo");
		String visitId = request.getParameter("visitId");
		String template_name = request.getParameter("template_name");
		StringBuffer sampleBarCodePlainText = new StringBuffer();
		SampleBarcodePrintTemplateDAO sampleDAO = new SampleBarcodePrintTemplateDAO();

		List<BasicDynaBean > sampleDetails = new SampleCollectionDAO().getSampleCollectionDetails(sampleNos,visitId);
		BasicDynaBean sampleCollectionBean = null;
		BasicDynaBean patientDetailsBean = null;
		
		if (null != sampleDetails && !sampleDetails.isEmpty() && sampleDetails.size() > 0) {
			mrNo = (String)sampleDetails.get(0).get("mr_no");
			if (null == mrNo || mrNo.equals("")) {
				patientDetailsBean = PatientDetailsDAO.getIncomingPatientDetails(visitId);
			} else {
				patientDetailsBean = com.insta.hms.Registration.PatientDetailsDAO.getPatientGeneralDetailsBean(mrNo);
				if (patientDetailsBean.get("visit_id") == null) {
					patientDetailsBean.set("visit_id", visitId);
				}
			}
		}

		for (int i = 0; i < sampleDetails.size(); i++) {
			Map paramMap = new HashMap();
			sampleCollectionBean = sampleDetails.get(i);
		
			paramMap.put("patient", patientDetailsBean);
			Object obj = sampleCollectionBean.get("sample_qty");
			int qty = 1;
			if (null != obj && !obj.equals("")) {
				qty = Integer.parseInt(obj.toString());
			}

			templateContent = sampleDAO.getCustomizedTemplate(template_name);
			if (templateContent == null || templateContent.equals("")) {
				fGen = new FtlReportGenerator("SampleCollectionBarCodeTextTemplate");
			} else {
				StringReader reader = new StringReader(templateContent);
				fGen = new FtlReportGenerator("SampleCollectionBarCodeTextTemplate",reader);
			}
			paramMap.put("sample_no", sampleCollectionBean.get("sample_sno"));
			paramMap.put("sample_date", sampleCollectionBean.get("sample_date"));
			paramMap.put("sample_type", sampleCollectionBean.get("sample_type"));
			paramMap.put("taken_by", sampleCollectionBean.get("user_name"));
			paramMap.put("sample_container", sampleCollectionBean.get("sample_container"));
			paramMap.put("labno", SampleCollectionDAO.getConcatenatedLabIds((String)sampleCollectionBean.get("sample_sno")));
			paramMap.put("orderdate", SampleCollectionDAO.getConcatenatedOrderDate((String)sampleCollectionBean.get("sample_sno")));
			paramMap.putAll(SampleCollectionDAO.testDetailsOfSample((String)sampleCollectionBean.get("sample_sno")));
			plainText = fGen.getPlainText(paramMap);
			for (int count=0; count<qty; count++) {
				sampleBarCodePlainText.append(plainText);
			}
		}
		request.setAttribute("textReport", sampleBarCodePlainText);
		request.setAttribute("printerType", "BARCODE");
		return mapping.findForward("textPrintApplet");
	}
}
