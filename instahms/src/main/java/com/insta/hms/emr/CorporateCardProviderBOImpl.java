/**
 *
 */
package com.insta.hms.emr;

import com.insta.hms.documentpersitence.CorporateCardDocumentAbstractImpl;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.insurance.InsuranceDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lakshmi
 *
 */
public class CorporateCardProviderBOImpl implements EMRInterface {

	/* (non-Javadoc)
	 * @see com.insta.hms.emr.EMRInterface#getPDFBytes(java.lang.String, int)
	 */
	public byte[] getPDFBytes(String docid, int printerId) throws Exception {
		GenericDocumentsDAO generaldocdao = new GenericDocumentsDAO();
		BasicDynaBean generaldocbean = generaldocdao.findByKey("doc_id", Integer.parseInt(docid));
		String mrNo = (String) generaldocbean.get("mr_no");
		String patientId = (String) generaldocbean.get("patient_id");
		return GenericDocumentsDAO.getDocumentBytes(docid, false, mrNo, patientId, printerId);
	}

	/* (non-Javadoc)
	 * @see com.insta.hms.emr.EMRInterface#listDocumentsByMrno(java.lang.String)
	 */
	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		List<BasicDynaBean> l =
			new CorporateCardDocumentAbstractImpl().getDocumentsList("mr_no", mrNo, false, null);
		return listDocuments(l);
	}

	/* (non-Javadoc)
	 * @see com.insta.hms.emr.EMRInterface#listDocumentsByVisit(java.lang.String)
	 */
	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		List<BasicDynaBean> l =
			new CorporateCardDocumentAbstractImpl().getDocumentsList("patient_id", visitId, false, null);
		return listDocuments(l);
	}

	/**
	 * creates list of EMRDoc objects from a list of DynaBeans representing each document
	 */
	private List<EMRDoc> listDocuments(List<BasicDynaBean> list) throws Exception {
		if (list == null || list.isEmpty())
			return null;

		List<EMRDoc> emrdoclist = new ArrayList<EMRDoc>();

		String documentId = null;
		String documentName = null;
		Date documentDate = null;
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		int printerId = (Integer) printpref.get("printer_id");
		for (BasicDynaBean bean: list) {

			boolean exists = false;

			for (BasicDynaBean b: list) {
				if (documentId != null && b.get("doc_id").toString().equals(documentId)
						&& documentName != null && b.get("doc_name").toString().equals(documentName)
							&& documentDate != null && ((Date)bean.get("doc_date")).compareTo(documentDate) == 0) {
					exists = true;
					break;
				}
			}

			if (exists)
				continue;

			EMRDoc doc = new EMRDoc();
			doc.setTitle(bean.get("doc_name").toString());
			documentName = bean.get("doc_name").toString();
			doc.setDate((Date)bean.get("doc_date"));
			documentDate = (Date)bean.get("doc_date");
			doc.setVisitDate((java.util.Date)bean.get("reg_date"));

			doc.setDoctor("");

			String docId = bean.get("doc_id").toString();
			documentId = docId;
			doc.setDocid(docId);

			if (bean.get("patient_id") != null && !bean.get("patient_id").equals("null")) {
				doc.setVisitid(bean.get("patient_id").toString());
			}

			String userName = (String) bean.get("username");

			String format = (String) bean.get("doc_format");
			if (format.equals("doc_rtf_templates")) {
				doc.setPdfSupported(false);
			} else if (format.equals("doc_fileupload")) {
				String contentType = (String) bean.get("content_type");
				if (contentType.equals("application/pdf") || contentType.split("/")[0].equals("image"))
					doc.setPdfSupported(true);
				else doc.setPdfSupported(false);
			} else {
				doc.setPdfSupported(true);
			}
			doc.setAuthorized(true);
			String docType = (String) bean.get("doc_type");
			doc.setType(docType);
			doc.setUpdatedBy((String) bean.get("username"));
			doc.setPrinterId(printerId);
			doc.setProvider(EMRInterface.Provider.CorporateCardProvider);

			String displayUrl = "/Registration/GeneralRegistrationCorporateCard.do?_method=viewCorporateCardImage&visitId="+doc.getVisitid();
			displayUrl += "&doc_id=" + docId;
			if (format.equals("doc_hvf_templates"))
				displayUrl += "&allFields=Y";

			doc.setDisplayUrl(displayUrl);
			emrdoclist.add(doc);
		}
		return emrdoclist;
	}

	/* (non-Javadoc)
	 * @see com.insta.hms.emr.EMRInterface#listVisitDocumentsForMrNo(java.lang.String)
	 */
	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		List<BasicDynaBean> l =
			InsuranceDAO.getAllVisitForMrNoCorporateCardDocuments(mrNo);
		return listDocuments(l);
	}

}
