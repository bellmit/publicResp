package com.insta.hms.emr;


import com.insta.hms.services.ServiceDocumentsDAO;
import com.insta.hms.services.ServicesDAO;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceProviderBOImpl implements EMRInterface {

	PatientHeaderTemplateDAO phTemplateDAO = new PatientHeaderTemplateDAO();

	public byte[] getPDFBytes(String docid, int printerId) throws Exception {
		ServiceDocumentsDAO servicedocdao = new ServiceDocumentsDAO();
		BasicDynaBean servicedocbean = servicedocdao.findByKey("doc_id", Integer.parseInt(docid));
		Integer consultationId = (Integer) servicedocbean.get("prescription_id");
		ServicesDAO consultDAO = new ServicesDAO();
		String patientId = (String) consultDAO.findByKey("prescription_id", consultationId).get("patient_id");
		return GenericDocumentsDAO.getDocumentBytes(docid, false, null, patientId, printerId);
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		return Collections.emptyList();
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_SERVICE);
			List<EMRDoc> listOfEMRdocs = populateEMRDocs(ServicesDAO.getSignedOffServiceReports(null, mrNo, true), printpref);
			List<EMRDoc> listOfEMRServices = populateEMRServices(ServicesDAO.getCompletedServiceReports(null, mrNo, true), printpref);
			List<EMRDoc> merageList = new ArrayList<EMRDoc>(listOfEMRdocs);
			merageList.addAll(listOfEMRServices);

		return merageList;
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_SERVICE);
		List<EMRDoc> listOfEMRdocs = populateEMRDocs(ServicesDAO.getSignedOffServiceReports(visitId, null, false), printpref);
		List<EMRDoc> listOfEMRServices = populateEMRServices(ServicesDAO.getCompletedServiceReports(visitId, null, false), printpref);
		List<EMRDoc> merageList = new ArrayList<EMRDoc>(listOfEMRdocs);
		merageList.addAll(listOfEMRServices);

	return merageList;

	}


	public List<EMRDoc> populateEMRDocs(List<BasicDynaBean> visitInvestigations, BasicDynaBean printpref) throws SQLException {
		List<EMRDoc> docs = new ArrayList<EMRDoc>();
		for (BasicDynaBean bean : visitInvestigations) {
			EMRDoc doc = new EMRDoc();

			Integer docId = (Integer) bean.get("doc_id");
			doc.setDocid(docId+"");
			doc.setTitle((String) bean.get("report_name"));
			doc.setDoctor((bean.get("pres_doctor_name") == null) ? "" : (String)bean.get("pres_doctor_name"));
			
			int printerId = (Integer) printpref.get("printer_id");
			doc.setPrinterId(printerId);


			String accessRights = (String) bean.get("access_rights");
			String userName = (String) bean.get("report_user_name");
			doc.setAuthorized(Helper.getAuthorized(userName, accessRights));
			doc.setUpdatedBy(userName);

			String format = (String) bean.get("doc_format");
			if (format.equals("doc_rtf_templates")) {
				doc.setPdfSupported(false);
			} else if (format.equals("doc_fileupload")) {
				String contentType = (String) bean.get("content_type");
				contentType = contentType == null ? "" : contentType;

				if (contentType.equals("application/pdf") || contentType.split("/")[0].equals("image"))
					doc.setPdfSupported(true);
				else doc.setPdfSupported(false);
			} else {
				doc.setPdfSupported(true);
			}
			String displayUrl = "/Service/ServiceReportsPrint.do?_method=print&forcePdf=true&printerId="+printerId;
			displayUrl += "&doc_id=" + docId;
			if (format.equals("doc_hvf_templates"))
				displayUrl += "&allFields=Y";

			if (format.equals("doc_link")) {
                doc.setDisplayUrl((String) bean.get("doc_location"));
                doc.setExternalLink(true);
            } else {
                doc.setDisplayUrl(displayUrl);
            }

			doc.setType("SYS_ST");
			doc.setDate((java.sql.Date)bean.get("doc_date"));
			doc.setVisitid((String) bean.get("patient_id"));
			doc.setProvider(EMRInterface.Provider.ServiceProvider);
			doc.setVisitDate((java.sql.Date)bean.get("reg_date"));

			docs.add(doc);
		}
		return docs;
	}
	public List<EMRDoc> populateEMRServices(List<BasicDynaBean> visitInvestigations, BasicDynaBean printpref) throws SQLException {
		List<EMRDoc> docs = new ArrayList<EMRDoc>();
		for (BasicDynaBean bean : visitInvestigations) {
			EMRDoc doc = new EMRDoc();

			Integer docId = (Integer) bean.get("prescription_id");
			Integer prescribedId=(Integer)bean.get("prescription_id");
			doc.setDocid(docId+"");
			doc.setTitle((String)bean.get("service_name"));
			doc.setUpdatedBy((String)bean.get("user_name"));
			doc.setDoctor((bean.get("pres_doctor_name") == null) ? "" : (String)bean.get("pres_doctor_name"));
			int printerId = (Integer) printpref.get("printer_id");
			doc.setPrinterId(printerId);
			doc.setPdfSupported(true);
			doc.setAuthorized(true);

			String displayUrl = "/Service/ServicesConductionPrint.do?_method=print&prescription_id="+prescribedId+"&printerId="+printerId;

			doc.setDisplayUrl(displayUrl);
			doc.setType("SYS_ST");
			doc.setDate((java.sql.Date)bean.get("conducted_date"));
			doc.setVisitid((String)bean.get("patient_id").toString());
			doc.setProvider(EMRInterface.Provider.ServiceProvider);
			doc.setVisitDate((java.sql.Date)bean.get("reg_date"));

			docs.add(doc);
		}
		return docs;
	}

}
