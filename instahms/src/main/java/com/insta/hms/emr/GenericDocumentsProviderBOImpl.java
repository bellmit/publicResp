package com.insta.hms.emr;

import com.insta.hms.documentpersitence.GeneralDocumentAbstractImpl;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class GenericDocumentsProviderBOImpl implements EMRInterface{

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		List<BasicDynaBean> l =
			new GeneralDocumentAbstractImpl().getDocumentsList("patient_id", visitId, false, null);
		return listDocuments(l);
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return listDocuments(GenericDocumentsDAO.getAllVisitsDocsForMrNo(mrNo));
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		List<BasicDynaBean> l =
			new GeneralDocumentAbstractImpl().getDocumentsList("mr_no", mrNo, false, null);
		return listDocuments(l);
	}

	/**
	 * creates list of EMRDoc objects from a list of DyanaBeans representing each document
	 */
	private List<EMRDoc> listDocuments(List<BasicDynaBean> list) throws Exception {
		if (list == null || list.isEmpty())
			return null;

		List<EMRDoc> emrdoclist = new ArrayList<EMRDoc>();
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		int printerId = (Integer) printpref.get("printer_id");
		for (BasicDynaBean bean: list) {
			EMRDoc doc = new EMRDoc();
			doc.setTitle(bean.get("doc_name").toString());
			doc.setDate((Date)bean.get("doc_date"));
			doc.setVisitDate((java.util.Date)bean.get("reg_date"));

			doc.setDoctor("");

			String docId = bean.get("doc_id").toString();
			doc.setDocid(docId);

			if (bean.get("patient_id") != null && !bean.get("patient_id").equals("null")) {
				doc.setVisitid(bean.get("patient_id").toString());
			}

			String accessRights = (String) bean.get("access_rights");
			String userName = (String) bean.get("username");

			String format = (String) bean.get("doc_format");
			if (format.equals("doc_rtf_templates")) {
				doc.setPdfSupported(false);
			} else if (format.equals("doc_fileupload")) {
				String contentType = (String) bean.get("content_type");
				if (contentType.equals("application/pdf") || contentType.split("/")[0].equals("image"))
					doc.setPdfSupported(true);
				else doc.setPdfSupported(false);
			} else if (format.equals("doc_link")) {
				doc.setPdfSupported(false);
			} else {
				doc.setPdfSupported(true);
			}
			doc.setAuthorized(Helper.getAuthorized(userName, accessRights));
			String docType = (String) bean.get("doc_type");
			doc.setType(docType);
			doc.setUpdatedBy((String) bean.get("username"));
			doc.setPrinterId(printerId);
			doc.setProvider(EMRInterface.Provider.GenericDocumentsProvider);

			if (format.equals("doc_link")) {
				doc.setDisplayUrl((String) bean.get("doc_location"));
				doc.setExternalLink(true);
			} else {
				String displayUrl = "/pages/GenericDocuments/GenericDocumentsPrint.do?_method=print&forcePdf=true&printerId="+printerId;
				displayUrl += "&doc_id=" + docId;
				if (format.equals("doc_hvf_templates"))
					displayUrl += "&allFields=Y";

				doc.setDisplayUrl(displayUrl);
			}


			emrdoclist.add(doc);
		}
		return emrdoclist;

	}


	public byte[] getPDFBytes(String docid, int printerId) throws Exception {
		GenericDocumentsDAO generaldocdao = new GenericDocumentsDAO();
		BasicDynaBean generaldocbean = generaldocdao.findByKey("doc_id", Integer.parseInt(docid));
		String mrNo = (String) generaldocbean.get("mr_no");
		String patientId = (String) generaldocbean.get("patient_id");
		return GenericDocumentsDAO.getDocumentBytes(docid, false, mrNo, patientId, printerId);
	}

}
