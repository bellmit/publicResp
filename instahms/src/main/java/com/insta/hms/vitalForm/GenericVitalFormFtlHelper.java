/**
 *
 */
package com.insta.hms.vitalForm;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.ftl.FtlMethods;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * @author lakshmi.p
 *
 */
public class GenericVitalFormFtlHelper {
		private Configuration cfg;
		static Logger log = LoggerFactory.getLogger(GenericVitalFormFtlHelper.class);
		private PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();

		public GenericVitalFormFtlHelper(Configuration cfg) {
			this.cfg = cfg;
		}

		public enum return_type {PDF, PDF_BYTES, TEXT_BYTES};

		public byte[] getVitalParameterReport(String patientId, String paramType, return_type enumType, BasicDynaBean prefs,
				OutputStream os) throws SQLException, DocumentException, TemplateException, IOException,
				XPathExpressionException, TransformerException{

			byte[] bytes = null;

			Map pDetails = new HashMap();
			GenericDocumentsFields.copyPatientDetails(pDetails, null, patientId, false);
			Map ftlParamMap = new HashMap();
			ftlParamMap.put("visitdetails", pDetails);
			ftlParamMap.put("modules_activated",
					((Preferences) RequestContext.getSession().getAttribute("preferences")).getModulesActivatedMap());

			
			List<BasicDynaBean> vitalMasterList = genericVitalFormDAO.getVitalParameterMaster(patientId, paramType, null);
			List<BasicDynaBean> vitalReadingIds = genericVitalFormDAO.getVisitVitalReadingIds(patientId, paramType);
			List<BasicDynaBean> vitalReadingList = genericVitalFormDAO.getVisitFormReadings(patientId, paramType);

			List vList = new ArrayList();
			Set<Integer> vitalsParamsWhichHasData = new HashSet<>();

			Map rmap = null;
			for (BasicDynaBean b : vitalReadingIds) {
				Integer rid =  (Integer)b.get("vital_reading_id");
				Timestamp rtime = (Timestamp)b.get("date_time");
				String user = (String)b.get("user_name");
				rmap = new HashMap();
				rmap.put("vital_reading_id",rid);
				rmap.put("date_time",rtime);
				rmap.put("user",user);
				if(b.get("vital_reading_id").equals(rid)) {
					for (BasicDynaBean m : vitalMasterList) {
						Integer pid = (Integer)m.get("param_id");
						for (BasicDynaBean rl : vitalReadingList) {
							if ((pid.equals((Integer)rl.get("param_id")))
									&& (rid.equals((Integer)rl.get("vital_reading_id")))) {
								rmap.put((String)m.get("param_label"),(String)rl.get("param_value"));
								vitalsParamsWhichHasData.add((Integer)m.get("param_id"));
							}
						}
					}
				}
				vList.add(rmap);
			}
			List<BasicDynaBean> vitalMasterListmodified = new ArrayList<>();
			for (BasicDynaBean bean: vitalMasterList) {
			  if (vitalsParamsWhichHasData.contains((Integer)bean.get("param_id"))) {
			    vitalMasterListmodified.add(bean);
			  }
			}
			ftlParamMap.put("paramType", paramType);
			ftlParamMap.put("VitalMasterList", vitalMasterListmodified);
			ftlParamMap.put("VitalReadingList", vList);

			Template t = null;
			String templateContent = new PrintTemplatesDAO().getCustomizedTemplate(PrintTemplate.Vital_Measurements);
			if (templateContent == null || templateContent.equals("")) {
				t = cfg.getTemplate("VitalFormDetailsReport.ftl");
			} else {
				StringReader reader = new StringReader(templateContent);
				t = new Template("VitalFormDetailsFtlReport.ftl", reader, cfg);
			}
			StringWriter writer = new StringWriter();
			t.process(ftlParamMap, writer);
			String patientHeader = (String) phTemplateDao.getPatientHeader(
					new PrintTemplatesDAO().getPatientHeaderTemplateId(PrintTemplate.Vital_Measurements), "Vital");
			StringReader reader = new StringReader(patientHeader);	
			Template pt = new Template("PatientHeader.ftl", reader, AppInit.getFmConfig());		
			Map templateMap = new HashMap();
			StringWriter pwriter = new StringWriter();
			
			try {
				pt.process(templateMap, pwriter);
			} catch (TemplateException te) {
				log.error("", te);
				throw te;
			}
			StringBuilder documentContent = new StringBuilder();
			documentContent.append(pwriter.toString());
			documentContent.append(writer.toString());
			
			HtmlConverter hc = new HtmlConverter();
			boolean repeatPHeader = ((String) prefs.get("repeat_patient_info"))
												.equalsIgnoreCase("Y");
			if (enumType.equals(return_type.PDF)) {
				hc.writePdf(os, writer.toString(), "Vital Parameters", prefs, false,
						repeatPHeader, true, true, true, false);
				os.close();

			} else if (enumType.equals(return_type.PDF_BYTES)) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				hc.writePdf(stream, writer.toString(), "Vital Parameters", prefs, false,
						repeatPHeader, true, true, true, false);
				bytes = stream.toByteArray();
				stream.close();

			} else if (enumType.equals(return_type.TEXT_BYTES)) {
				bytes = hc.getText(writer.toString(), "Vital Parameters",
						prefs, true, true);

			} else {}
			return bytes;
		}


		public byte[] getIntakeOutputParameterReport(String patientId, String paramType, return_type enumType, BasicDynaBean prefs,
				OutputStream os) throws SQLException, DocumentException, TemplateException, IOException,
				XPathExpressionException, TransformerException{

			byte[] bytes = null;

			Map pDetails = new HashMap();
			GenericDocumentsFields.copyPatientDetails(pDetails, null, patientId, false);
			Map ftlParamMap = new HashMap();
			ftlParamMap.put("visitdetails", pDetails);
			ftlParamMap.put("modules_activated",
					((Preferences) RequestContext.getSession().getAttribute("preferences")).getModulesActivatedMap());

			List<BasicDynaBean> vitalReadingList = genericVitalFormDAO.getVisitFormReadings(patientId, paramType, true);

			ftlParamMap.put("paramType", paramType);
      ftlParamMap.put("vitalReadingGroupedMap", ConversionUtils.groupByColumn(vitalReadingList, "vital_reading_id"));

			Template t = cfg.getTemplate("IntakeOutputDetailsReport.ftl");
			StringWriter writer = new StringWriter();
			t.process(ftlParamMap, writer);
			HtmlConverter hc = new HtmlConverter();
			boolean repeatPHeader = ((String) prefs.get("repeat_patient_info"))
												.equalsIgnoreCase("Y");
			if (enumType.equals(return_type.PDF)) {
				hc.writePdf(os, writer.toString(), "Intale Output Parameters", prefs, false,
						repeatPHeader, true, true, true, false);
				os.close();

			} else if (enumType.equals(return_type.PDF_BYTES)) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				hc.writePdf(stream, writer.toString(), "Intake Output Parameters", prefs, false,
						repeatPHeader, true, true, true, false);
				bytes = stream.toByteArray();
				stream.close();

			} else if (enumType.equals(return_type.TEXT_BYTES)) {
				bytes = hc.getText(writer.toString(), "Intake Output Parameters",
						prefs, true, true);

			} else {}
			return bytes;
		}

	}
