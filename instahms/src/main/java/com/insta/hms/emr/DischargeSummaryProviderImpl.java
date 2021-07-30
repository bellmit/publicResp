package com.insta.hms.emr;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.dischargesummary.DischargeSummaryDAOImpl;
import com.insta.hms.dischargesummary.DischargeSummaryReportHelper;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This provider is for Discharge Summary documents (one per patient, applicable to
 * both IP as well as OP).
 */
public class DischargeSummaryProviderImpl implements EMRInterface {

    static Logger logger =
		LoggerFactory.getLogger(DischargeSummaryProviderImpl.class);


	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return populateEmrDocs(DischargeSummaryDAOImpl.getAllVisitDocs(null, mrNo, true));
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return populateEmrDocs(DischargeSummaryDAOImpl.getAllVisitDocs(visitId, null, false));

	}

	public List populateEmrDocs(List<BasicDynaBean> l) throws SQLException {
		List<EMRDoc> el = new ArrayList<EMRDoc>();
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE);
		int printerId = (Integer) printpref.get("printer_id");
		for (BasicDynaBean b : l) {
			EMRDoc d = new EMRDoc();

			String visitId = (String) b.get("patient_id");
			String format = (String) b.get("discharge_format");
			int docid = (Integer) b.get("discharge_doc_id");

			d.setDocid(format + "-" + docid);
			d.setTitle((String)b.get("name"));
			d.setDate((java.util.Date)b.get("disch_date_for_disch_summary"));
			d.setDoctor((String)b.get("doctor_name")== null ? " ":(String)b.get("doctor_name"));
			d.setUpdatedBy((String)b.get("discharge_finalized_user"));
			d.setPrinterId(printerId);
			String displayUrl = "/dischargesummary/dischargesummaryPrint.do?_method=print&patient_id="
				+visitId+"&docid="+docid+"&forcePdf=true&printerId="+printerId;
			d.setDisplayUrl(displayUrl);

			boolean authorized = false;
			if (format.equals("U")) {
				String contentType = (String) b.get("content_type");
				if (contentType.equals("application/pdf")) d.setPdfSupported(true);
				authorized = true;
			} else if (format.equals("P")) {
				d.setPdfSupported(true);
				authorized = true;
			} else {
				d.setPdfSupported(true);
				authorized = Helper.getAuthorized((String) b.get("username"),
						(String) b.get("access_rights"));
			}
			d.setAuthorized(authorized);
			d.setVisitid(visitId);
			d.setProvider(EMRInterface.Provider.DischargeSummaryProvider);
			d.setType("SYS_DS");
			d.setVisitDate((java.util.Date)b.get("reg_date"));

			el.add(d);
		}
		return el;
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		// not applicable: discharge summary is always based on visits.
		return Collections.emptyList();
	}

	public byte[] getPDFBytes(String docidFormat, int printerId) throws Exception {
		String parsed[] = docidFormat.split("-");
		String format = parsed[0];
		String docIdStr = parsed[1];

		int docid = Integer.parseInt(docIdStr);
		BasicDynaBean prefs =  PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printerId);

		DischargeSummaryReportHelper reportHelper = new DischargeSummaryReportHelper();
		if (format.equals("F")) {
			return reportHelper.getDischargeSummaryReport(docid, DischargeSummaryReportHelper.FormatType.HVF,
					DischargeSummaryReportHelper.ReturnType.PDF_BYTES, prefs,
					(Preferences) RequestContext.getSession().getAttribute("preferences"),
					null);
		} else if (format.equals("T")) {
			return reportHelper.getDischargeSummaryReport(docid, DischargeSummaryReportHelper.FormatType.RICH_TEXT,
					DischargeSummaryReportHelper.ReturnType.PDF_BYTES, prefs, null, null);
		} else if (format.equals("P")) {
			return reportHelper.getDischargeSummaryReport(docid, DischargeSummaryReportHelper.FormatType.PDF,
					DischargeSummaryReportHelper.ReturnType.PDF_BYTES, prefs, null, null);
		} else if (format.equals("U")) {
			return DischargeSummaryDAOImpl.getUploadedFileBytes(docid);
		} else {
			logger.error("Unrecognized doc format: " + format);
		}

		return null;
	}

}

