/**
 *
 */
package com.insta.hms.OTServices;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author krishna.t
 *
 */
public class OTReportsDAO extends GenericDAO {

	public OTReportsDAO() {
		super("operation_documents");
	}

	private static String EMR_CONDUCT_OPERATIONS=
		" SELECT od.doc_id, doc_name, doc_date, doc_format, od.username, bos.patient_id, " +
		"(SELECT doc.doctor_name FROM bed_operation_schedule bps " +
		"left join doctors doc on doc.doctor_id = bps.consultant_doctor" +
		" WHERE bps.prescribed_id=od.prescription_id AND bps.consultant_doctor IS NOT NULL AND bps.consultant_doctor!= '' LIMIT 1) AS pres_doctor,pr.reg_date" +
		" FROM operation_documents od JOIN patient_documents pd using (doc_id) " +
		"	JOIN bed_operation_schedule bos on (bos.prescribed_id=od.prescription_id) "+
		"   JOIN patient_registration pr ON(bos.patient_id = pr.patient_id)";

	public static List<EMRDoc> getConductedoperationsListForEMR(String patientId, String mrNo, boolean allVisitsDocs)
		throws SQLException, ParseException {

		List<EMRDoc> docs = new ArrayList<EMRDoc>();
		List<BasicDynaBean> l = null;
		BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
		String showSignedOff = ((String) genPrefs.get("show_tests_in_emr")).equals("S") ? " signed_off='Y' AND " : "";

		if (allVisitsDocs) {
			l = DataBaseUtil.queryToDynaList(EMR_CONDUCT_OPERATIONS + " WHERE " + showSignedOff +
					" pr.mr_no=? ", mrNo);
		} else {
			l = DataBaseUtil.queryToDynaList(EMR_CONDUCT_OPERATIONS + " WHERE " + showSignedOff +
					" pr.patient_id=? ", patientId);
		}
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG);
		int printerId = (Integer) printpref.get("printer_id");
		for (BasicDynaBean b : l) {
			EMRDoc doc = new EMRDoc();
			String docId = b.get("doc_id").toString();
			doc.setDocid(docId);
			doc.setVisitid((String) b.get("patient_id"));
			doc.setTitle((String)b.get("doc_name"));
			doc.setDoctor((b.get("pres_doctor") == null) ? "" : (String)b.get("pres_doctor"));
			doc.setPrinterId(printerId);
			String format = (String) b.get("doc_format");
			String displayUrl = "/otservices/OperationDocumentsPrint.do?_method=print&doc_id="+docId+"&forcePdf=true&printerId="+printerId;
			if (format.equals("doc_hvf_templates"))
				displayUrl += "&allFields=Y";

			doc.setUpdatedBy((String)b.get("username"));
			doc.setAuthorized(true);
			doc.setDisplayUrl(displayUrl);
			doc.setProvider(EMRInterface.Provider.OperationProvider);


			if (format.equals("doc_rtf_templates")) {
				doc.setPdfSupported(false);
			} else if (format.equals("doc_fileupload")) {
				String contentType = (String) b.get("content_type");
				if (contentType.equals("application/pdf") || contentType.split("/")[0].equals("image"))
					doc.setPdfSupported(true);
				else doc.setPdfSupported(false);
			} else {
				doc.setPdfSupported(true);
			}
			doc.setType("SYS_OT");
			doc.setDate((java.sql.Date)b.get("doc_date"));
			doc.setVisitDate((java.sql.Date)b.get("reg_date"));
			docs.add(doc);

		}
		return docs;
	}

	private static String OPERATION_DOCUMENT_FIELDS = "SELECT dat.template_name, dat.template_id, " +
		" dat.title, (CASE WHEN dat.doc_format IS NULL THEN 'doc_fileupload' ELSE dat.doc_format END) AS doc_format, " +
		" pd.doc_status, od.doc_id, od.prescription_id, od.username, od.doc_name, " +
		" od.doc_date, dat.status, dat.specialized, pd.content_type, dat.access_rights, od.signed_off ";

	private static String OPERATION_DOCUMENT_TABLES = " FROM operation_documents od " +
		" JOIN patient_documents pd ON od.doc_id=pd.doc_id " +
		" JOIN bed_operation_schedule bos ON od.prescription_id=bos.prescribed_id " +
		" JOIN patient_details pad ON (pad.mr_no = bos.mr_no AND " +
		" patient_confidentiality_check(pad.patient_group,pad.mr_no))" +
		" LEFT OUTER JOIN doc_all_templates_view dat on pd.template_id=dat.template_id " +
		"	and pd.doc_format=dat.doc_format ";

	private static String OPERATION_DOCUMENT_COUNT = "SELECT count(od.doc_id) ";


	public static PagedList getOperationDocs(Map listingParams, Map extraParams, Boolean specialized) throws SQLException {

		SearchQueryBuilder qb = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, OPERATION_DOCUMENT_FIELDS, OPERATION_DOCUMENT_COUNT,
					OPERATION_DOCUMENT_TABLES, null, listingParams);
			qb.addFilter(SearchQueryBuilder.INTEGER, "od.prescription_id", "=", Integer.parseInt((String)extraParams.get("prescription_id")));
			qb.build();
			return qb.getDynaPagedList();
		} finally {
			if (qb != null)
				qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

}
