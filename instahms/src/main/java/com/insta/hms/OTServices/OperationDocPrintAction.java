/**
 *
 */
package com.insta.hms.OTServices;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.documentpersitence.AbstractDocumentPersistence;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.genericdocuments.PatientDocumentsDAO;
import com.insta.hms.genericdocuments.PatientHVFDocValuesDAO;
import com.insta.hms.imageretriever.ImageRetriever;
import com.insta.hms.imageretriever.PatientImageRetriever;
import com.insta.hms.imageretriever.VisitWiseImageRetriever;
import com.insta.hms.master.HVFPrintTemplate.HVFPrintTemplateDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * @author krishna
 *
 */
public class OperationDocPrintAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(GenericDocumentsDAO.class);
	private static GenericDAO pdftemplatedao = new GenericDAO("doc_pdf_form_templates");
	private static PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();
	private static GenericDAO pdfvaluesdocdao = new GenericDAO("patient_pdf_form_doc_values");
	private static PatientDocumentsDAO patientdocdao = new PatientDocumentsDAO();


	public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)
		throws ServletException, IOException, FileUploadException, SQLException, DocumentException,
						  ParserConfigurationException, JRException, IllegalArgumentException,
						  TemplateException, XPathExpressionException, TransformerException,Exception {

		String docId = request.getParameter("doc_id");
		String fileName ="";
		if (docId == null)
			throw new IllegalArgumentException("docid is null");
		int doc_id = Integer.parseInt(docId);

		BasicDynaBean patientdocbean = patientdocdao.getBean();
		patientdocdao.loadByteaRecords(patientdocbean ,"doc_id", doc_id);

		String format = patientdocbean.get("doc_format").toString();
		String printerId = request.getParameter("printerId");
		BasicDynaBean prefs;
		if ((printerId != null) && !printerId.equals("")) {
			prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
				Integer.parseInt(request.getParameter("printerId")));
		} else {
			// use the default printer
			prefs = PrintConfigurationsDAO.getPatientDefaultPrintPrefs();
		}

		String printMode = "P";
		String forcePdf = request.getParameter("forcePdf");
		if ( (forcePdf == null) || !forcePdf.equals("true")) {
			// use the print mode selected.
			printMode = (String) prefs.get("print_mode");
		}

		AbstractDocumentPersistence persistenceAPI =
			AbstractDocumentPersistence.getInstance(mapping.getProperty("documentType"),
					new Boolean(mapping.getProperty("specialized")));


		if (format.equals("doc_hvf_templates")) {

			String allFields = request.getParameter("allFields");
			allFields = (allFields == null ? "Y" : allFields);
			Map ftlParamMap = new HashMap();
			Map patientDetails = new HashMap();

			Map keyParams = persistenceAPI.getDocKeyParams(doc_id);
			GenericDocumentsFields.copyStandardFields(patientDetails, false);
			persistenceAPI.copyReplaceableFields(patientDetails, keyParams, false);
			persistenceAPI.copyDocumentDetails(doc_id, patientDetails);

			Boolean signedOff = (Boolean) patientDetails.get("signed_off");

			ftlParamMap.put("visitdetails", patientDetails);
			ftlParamMap.put("mr_no", patientDetails.get("mr_no"));
			ftlParamMap.put("mr_no_barcode", "*"+patientDetails.get("mr_no")+"*");
			ftlParamMap.put("patient_id", patientDetails.get("patient_id"));
			ftlParamMap.put("modules_activated",
					((Preferences) RequestContext.getSession().getAttribute("preferences")).getModulesActivatedMap());
			List fieldvalues = PatientHVFDocValuesDAO.getHVFDocValues(doc_id, allFields.equals("Y"));
			ftlParamMap.put("fieldvalues", fieldvalues);
			ftlParamMap.put("patientDocDetails", patientdocbean);
			if (patientDetails.get("patient_id") != null && !patientDetails.get("patient_id").equals("")) {
				ftlParamMap.put("vitals", genericVitalFormDAO.getVitalReadings(patientDetails.get("patient_id").toString(), null));
				ftlParamMap.put("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientDetails.get("patient_id").toString()));
			} else {
				ftlParamMap.put("vitals", null);
				ftlParamMap.put("diagnosis_details", null);
			}
			String templateName = PatientHVFDocValuesDAO.getPrintTemplateName(doc_id);
			Template t = null;
			String templateMode = null;

			StringWriter writer = new StringWriter();

			if(templateName == null || templateName.equals("")) {
				t = AppInit.getFmConfig().getTemplate("PatientHVFDocumentPrint.ftl");
				templateMode = "H";
			}else{
				HVFPrintTemplateDAO templateDao = new HVFPrintTemplateDAO();
				BasicDynaBean tmpBean = templateDao.getTemplateContent(templateName);

				if (tmpBean == null) {
					// couldn't find the template in the db, bail out with error.
					return null;
				}
				templateMode = (String)tmpBean.get("template_mode");
				String templateContent = (String)tmpBean.get("hvf_template_content");

				StringReader reader =  new StringReader(templateContent);
				t = new Template("PatientHVFDocumentPrint.ftl", reader, AppInit.getFmConfig());
			}

			t.process(ftlParamMap, writer);

			ImageRetriever imgretriever = null;
			String patientId = (String) patientDetails.get("patient_id");
			patientId = patientId == null ? "" : patientId;
			if (patientId.equals("")) imgretriever = new PatientImageRetriever();
			else imgretriever = new VisitWiseImageRetriever();

			HtmlConverter hc = new HtmlConverter(imgretriever);
			String hvfContent = writer.toString();

			if (printMode.equals("T")) {
				String textContent = null;
				if (templateMode.equals("T")){
					// write the output as is.
					textContent = hvfContent;
				} else {
					// convert from HTML to text
					textContent = new String(hc.getText(hvfContent, "Patient HVF Document Print", prefs, true, true));
				}
				request.setAttribute("textReport", textContent);
				request.setAttribute("textColumns", prefs.get("text_mode_column"));
				request.setAttribute("printerType", "DMP");
				return mapping.findForward("textPrintApplet");

			} else {
				OutputStream os = response.getOutputStream();
				response.setContentType("application/pdf");
				Boolean repeatPHeader = ((String)prefs.get("repeat_patient_info")).equals("Y");
				try {
					if (templateMode.equals("T")){
						// convert text to PDF
						hc.textToPDF(hvfContent, os, prefs);
					} else {
						// convert html to PDF
						hc.writePdf(os, hvfContent, "Patient HVF Document Print",
								prefs, false, repeatPHeader, true, true, signedOff, false);
					}
				} catch (Exception e) {
					log.error("Generated HTML content:");
					log.error(hvfContent);
					throw(e);
				} finally {
					os.close();
				}
				return null;
			}


		} else if (format.equals("doc_rich_templates")) {

			String content = (String) patientdocbean.get("doc_content_text");
			String pHeaderTemplateType = PatientHeaderTemplate.Documents.getType();
			if (new Boolean(mapping.getProperty("specialized"))
					&& mapping.getProperty("documentType").equals("service"))
				pHeaderTemplateType = PatientHeaderTemplate.Ser.getType();
			String patientHeader = phTemplateDao.getPatientHeader(
					(Integer) patientdocbean.get("pheader_template_id"), pHeaderTemplateType);

			Map ftlParamMap = new HashMap();
			Map patientDetails = new HashMap();

			Map keyParams = persistenceAPI.getDocKeyParams(doc_id);
			GenericDocumentsFields.copyStandardFields(patientDetails, false);
			persistenceAPI.copyReplaceableFields(patientDetails, keyParams, false);
			persistenceAPI.copyDocumentDetails(doc_id, patientDetails);

			Boolean signedOff = (Boolean) patientDetails.get("signed_off");

			ftlParamMap.put("visitdetails", patientDetails);
			ftlParamMap.put("mr_no", patientDetails.get("mr_no"));
			ftlParamMap.put("mr_no_barcode", "*"+patientDetails.get("mr_no")+"*");
			ftlParamMap.put("patient_id", patientDetails.get("patient_id"));
			ftlParamMap.put("modules_activated",
					((Preferences) RequestContext.getSession().getAttribute("preferences")).getModulesActivatedMap());

			StringWriter writer = new StringWriter();
			StringReader reader = new StringReader(patientHeader);
			Template t = new Template("PatientHeader.ftl", reader, AppInit.getFmConfig());
			t.process(ftlParamMap, writer);
			StringBuilder printContent = new StringBuilder();
			printContent.append(writer.toString());
			printContent.append(content);

			String docName = "";
			if (patientDetails.get("doc_name") != null) // some documents doesn't have the name.
				docName = (String) patientDetails.get("doc_name");

			ImageRetriever imgretriever = null;
			String patientId = (String) patientDetails.get("patient_id");
			patientId = patientId == null ? "" : patientId;
			if (patientId.equals("")) imgretriever = new PatientImageRetriever();
			else imgretriever = new VisitWiseImageRetriever();

			HtmlConverter hc = new HtmlConverter(imgretriever);

			if (!printMode.equals("T")) {
				OutputStream os = response.getOutputStream();
				response.setContentType("application/pdf");
				Boolean repeatPHeader = ((String)prefs.get("repeat_patient_info")).equals("Y");
				hc.writePdf(os, printContent.toString(), docName, prefs, false,	repeatPHeader, true, true, signedOff, false);
				os.close();
				return null;
			} else {
				String textReport = new String(hc.getText(printContent.toString(), docName, prefs, true, true));
				request.setAttribute("textReport", textReport);
				request.setAttribute("textColumns", prefs.get("text_mode_column"));
				request.setAttribute("printerType", "DMP");
				return mapping.findForward("textPrintApplet");
			}

		} else if (format.equals("doc_pdf_form_templates")) {
			// prefs are not applicable here, the PDF is pre-formatted.
			BasicDynaBean bean = pdftemplatedao.getBean();
			pdftemplatedao.loadByteaRecords(bean, "template_id", patientdocbean.get("template_id"));
			InputStream pdf = (InputStream)bean.get("template_content");

			Map<String, String> fields = new HashMap<String, String>();

			Map keyParams = persistenceAPI.getDocKeyParams(doc_id);
			GenericDocumentsFields.copyStandardFields(fields, true);
			persistenceAPI.copyReplaceableFields(fields, keyParams, true);

			Map documentDetails = new HashMap();
			persistenceAPI.copyDocumentDetails(doc_id, documentDetails);
			fields.put("_username", (String) documentDetails.get("username"));


			List<BasicDynaBean> fieldslist = pdfvaluesdocdao.listAll(null, "doc_id", doc_id);

			for (BasicDynaBean fieldsBean : fieldslist) {
				fields.put(fieldsBean.get("field_name").toString(), fieldsBean.get("field_value").toString());
			}
			response.setContentType("application/pdf");
			OutputStream os = response.getOutputStream();
			PdfUtils.sendFillableForm(os, pdf, fields, true, null, null, null);

		} else {

			Map docParams = new HashMap();
			persistenceAPI.copyDocumentDetails(doc_id, docParams);

			if (docParams.get("doc_name")!=null && !(docParams.get("doc_name").equals(""))) {
				fileName = docParams.get("doc_name").toString();
				log.debug("Setting file name to: " + fileName);
			} else {
				log.debug("No file name");
			}

			if(patientdocbean.get("original_extension")!=null &&  !(fileName.equals(""))){
				fileName = fileName+"."+patientdocbean.get("original_extension").toString();
				response.setHeader("Content-disposition", "inline; filename=\""+fileName+"\"");
			}

			response.setContentType(patientdocbean.get("content_type").toString());
			OutputStream stream = response.getOutputStream();
			stream.write(DataBaseUtil.readInputStream((java.io.InputStream)patientdocbean.get("doc_content_bytea")));
			stream.flush();
			stream.close();
		}
		return null;
	}

}
