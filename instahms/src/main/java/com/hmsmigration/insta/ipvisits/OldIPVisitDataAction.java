/**
 *
 */
package com.hmsmigration.insta.ipvisits;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.documentpersitence.AbstractDocumentPersistence;
import com.insta.hms.genericdocuments.DocumentPrintConfigurationsDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.outpatient.OPPrescriptionFtlHelper;
import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class OldIPVisitDataAction extends DispatchAction {
	static Logger log = LoggerFactory.getLogger(OldIPVisitDataAction.class);

	public ActionForward migrate(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean result = true;
		try {
			txn : {
				List<BasicDynaBean> ipVisits = getIPVisits();
				String template = (String) GenericPreferencesDAO.getAllPrefs().get("default_emr_print_template");
				BasicDynaBean prefs = DocumentPrintConfigurationsDAO.getPrescriptionPrintPreferences(template, null);

				for (BasicDynaBean bean: ipVisits) {
					String userName = (String) bean.get("username");
					String mrNo = (String) bean.get("mr_no");
					String patientId = (String) bean.get("patient_id");
					String doctor = (String) bean.get("doctor_name");
					Integer consId = (Integer) bean.get("consultation_id");
					Date visitedDate = (Date) bean.get("visited_date");

					log.info("generating case sheet for Mr No.: "+mrNo+", patient_id: "+patientId +
								", Consultation Id: "+consId);

					OPPrescriptionFtlHelper ftlHelper = new OPPrescriptionFtlHelper();
					OPPrescriptionFtlHelper.DefaultType templateType = OPPrescriptionFtlHelper.DefaultType.EMR;

					ByteArrayOutputStream os = null;
					// no need to crate the output stream, for pdf bytes, stream created inside the following method.
					byte[] pdfBytes = ftlHelper.getConsultationFtlReport(consId, null, OPPrescriptionFtlHelper.ReturnType.PDF_BYTES,
								prefs, true, os, userName, templateType);

					Map requestParams = new HashMap();
					requestParams.put("format", new String[]{"doc_fileupload"});
					requestParams.put("doc_id", new String[]{""});
					requestParams.put("template_id", new String[]{""});
					requestParams.put("mr_no", new String[]{mrNo});
					requestParams.put("patient_id", new String[]{patientId});
					requestParams.put("doc_name", new String[]{"IP Case Sheet-"+doctor});
					requestParams.put("doc_type", new String[]{"SYS_CONSULT"});
					requestParams.put("fileName", new String[]{"IP Case Sheet-"+doctor+".pdf"});
					requestParams.put("content_type", new String[]{"application/pdf"});
					requestParams.put("doc_content_bytea", new Object[]{new ByteArrayInputStream(pdfBytes)});
					requestParams.put("doc_date", new Object[]{visitedDate});
					requestParams.put("username", new Object[]{userName == null ? "Migrated IP Doc" : userName});

					AbstractDocumentPersistence api = AbstractDocumentPersistence.getInstance(null, false);
					if (!api.create(requestParams, con)) {
						result = false;
						break txn;
					}
					log.info("case sheet inserted into generic docuemnts for Mr No.: "+mrNo+", patient_id: "+patientId +
								", Consultation Id: "+consId);

				}
			}
		} finally {
			DataBaseUtil.commitClose(con, result);
		}

		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		JSONSerializer js = new JSONSerializer().exclude("class");
		js.deepSerialize(result ? "Migration Successful.." : "Failed to migrate", response.getWriter());
		response.flushBuffer();

		return null;
	}

	private static final String GET_CONSULTATION_DOCS =
		"SELECT consultation_id, visited_date::date as visited_date, d.doctor_name, dc.username, dc.patient_id, dc.mr_no FROM doctor_consultation dc " +
		" JOIN patient_registration pr ON (pr.visit_type='i' and pr.patient_id=dc.patient_id) " +
		" JOIN doctors d ON (d.doctor_id = dc.doctor_name) " +
		" WHERE coalesce(dc.cancel_status, '')!='C' AND " ;

	private static final String CONSULTATION_VALUES_EXISTS =
		"	(EXISTS (SELECT image_id FROM doctor_consult_images WHERE consultation_id = dc.consultation_id) " +
		" 	OR (dc.doc_id != 0) " +
		"	OR EXISTS (SELECT consultation_id FROM patient_medicine_prescriptions WHERE consultation_id=dc.consultation_id) " +
		"	OR EXISTS (SELECT consultation_id FROM patient_other_medicine_prescriptions WHERE consultation_id=dc.consultation_id) " +
		"	OR EXISTS (SELECT consultation_id FROM patient_test_prescriptions WHERE consultation_id=dc.consultation_id) " +
		"	OR EXISTS (SELECT consultation_id FROM patient_service_prescriptions WHERE consultation_id=dc.consultation_id) " +
		"	OR EXISTS (SELECT consultation_id FROM patient_consultation_prescriptions WHERE consultation_id=dc.consultation_id) " +
		"	OR EXISTS (SELECT consultation_id FROM patient_operation_prescriptions WHERE consultation_id=dc.consultation_id) " +
		"	OR EXISTS (SELECT consultation_id FROM patient_other_prescriptions WHERE consultation_id=dc.consultation_id) " +
		"	OR EXISTS (SELECT case when ppfv.consultation_id is null then ppfv.mr_no else ppfv.consultation_id||'' end " +
		"			FROM patient_physician_form_values ppfv " +
		"			JOIN (SELECT regexp_split_to_table(consultation_forms, E',') as form_id, consultation_id, mr_no FROM doctor_consultation idc" +
		"					WHERE idc.consultation_id=dc.consultation_id) as phsform " +
		"				ON (phsform.form_id=ppfv.form_id::text) " +
		"			WHERE ppfv.consultation_id=dc.consultation_id or ppfv.mr_no=dc.mr_no) " +
		"	OR EXISTS (SELECT case when ppfg.consultation_id is null then ppfg.mr_no else ppfg.consultation_id||'' end " +
		"			FROM patient_phys_form_image_details ppfg " +
		"			JOIN (SELECT regexp_split_to_table(consultation_forms, E',') as form_id, consultation_id, mr_no FROM doctor_consultation idc" +
		"					WHERE idc.consultation_id=dc.consultation_id) as phsform " +
		"					ON (phsform.form_id=ppfg.form_id::text) " +
		"			WHERE ppfg.consultation_id=dc.consultation_id OR ppfg.mr_no=dc.mr_no))";

	private static List<BasicDynaBean> getIPVisits() throws SQLException {
		List<BasicDynaBean> l = (List<BasicDynaBean>) DataBaseUtil.queryToDynaList(
				GET_CONSULTATION_DOCS + CONSULTATION_VALUES_EXISTS);
		return l;
	}


}
