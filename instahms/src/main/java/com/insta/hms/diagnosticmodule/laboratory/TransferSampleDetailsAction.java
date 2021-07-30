/**
 *
 */
package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.common.OutHouseSampleDetails;
import com.insta.hms.diagnosticmodule.internallab.AutomaticSampleRegistration;
import com.insta.hms.diagnosticmodule.internallab.InternalLab;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class TransferSampleDetailsAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(TransferSampleDetailsAction.class);
	JSONSerializer js = new JSONSerializer().exclude("class");

	public TransferSamplesDAO dao = new TransferSamplesDAO();
	public ReceiveSamplesDAO receiveSampleDao = new ReceiveSamplesDAO();
	
	private static final GenericDAO sampleCollectionDAO = new GenericDAO("sample_collection");
	private static final GenericDAO testsPrescribedDAO = new GenericDAO("tests_prescribed");
	
	public ActionForward getTransferSamplesDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {

		String outSourceDestPrescId = request.getParameter("outsourceDestPrescribedId");
		BasicDynaBean internalLabSamplesBean  = null;
		if (outSourceDestPrescId!=null && !"".equals(outSourceDestPrescId)) {
			internalLabSamplesBean = dao.getInternalLabSamplesDetails(Integer.parseInt(outSourceDestPrescId));
		}
		BasicDynaBean bean = dao.findByKey("sample_collection_id", Integer.parseInt(request.getParameter("sampleCollectionId")));
		request.setAttribute("bean", bean);
		request.setAttribute("internalLabSamplesBean", internalLabSamplesBean);
		String transferUser = (String)request.getSession(false).getAttribute("userid");
		BasicDynaBean transferSampleDetailsBean = dao.getTransferSampleDetails(Integer.parseInt(request.getParameter("sampleCollectionId")));
		java.util.Map  patient =  com.insta.hms.Registration.VisitDetailsDAO.getPatientVisitDetailsMap(request.getParameter("patient_id"));
		BasicDynaBean incomingSampleRegistrationBean = receiveSampleDao.getIncomingSampleRegistrationDetails(request.getParameter("patient_id"));
		request.setAttribute("sampleDate", js.deepSerialize(transferSampleDetailsBean.getMap().get("sample_date")));
		request.setAttribute("transferSampleDetailsBean", transferSampleDetailsBean);
		request.setAttribute("incomingSampleRegistrationBean", incomingSampleRegistrationBean);
		request.setAttribute("outSourceList", dao.getAllOutSource());
		request.setAttribute("transferUser", transferUser);
		request.setAttribute("patient", patient);
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		request.setAttribute("diagGenericPref",diagGenericPref);

		return mapping.findForward("getTransferSamplesDetails");
	}

	public ActionForward saveTransferSamplesDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException, Exception {

		java.util.Date parsedDate = new java.util.Date();
		Timestamp transferredTime = new Timestamp(parsedDate.getTime());
		String sampletransferred = request.getParameter("_sample_transferred");
		String transferUser = (String)request.getSession(false).getAttribute("userid");
		String transferDate = request.getParameter("transferDate");
		String transferTime = request.getParameter("transferTime");
		String transferOtherDetails = request.getParameter("transferOtherDetails");
		int sampleCollectionId = Integer.parseInt(request.getParameter("sampleCollectionId"));
		String outsourceDestPrescribedId = request.getParameter("outsourceDestPrescribedId");
		String patientId = request.getParameter("patient_id");
		String outSourceDestId = request.getParameter("outsource_dest_id");
		String[] testIdArray = request.getParameter("sg_test_id").split(",");
		dao.removeSpaceFromArray(testIdArray);
		String[] prescIdArray = request.getParameter("sg_prescribed_id").split(",");
		dao.removeSpaceFromArray(prescIdArray);
		String[] sampleNoArray = request.getParameter("sg_sample_no").split(",");	
		dao.removeSpaceFromArray(sampleNoArray);		
		String[] sampleTypeIdArray = request.getParameter("sg_sample_type_id").split(",");
		dao.removeSpaceFromArray(sampleTypeIdArray);
		int centerId = RequestContext.getCenterId();
		Connection con = null;
		Boolean success = false;
		Boolean allSuccess = false;
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean visitCenterBean = VisitDetailsDAO.gettVisitCenterRelatedFields(con, patientId);
			int visitCenterId = (Integer)visitCenterBean.get("center_id");					
			ChargeBO chargeBo = new ChargeBO();
			OutHouseSampleDetails ohDetails = new OutHouseSampleDetails();			
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("saveTransferSamplesDetailsRedirect"));
			BasicDynaBean sampleCollectionBean = sampleCollectionDAO.findByKey("sample_collection_id",sampleCollectionId);
			BasicDynaBean testPrescBean = testsPrescribedDAO.getBean();	
			Integer isOutSourceDestIdExist = (Integer) sampleCollectionBean.get("outsource_dest_id");			
			transferredTime = DateUtil.parseTimestamp(transferDate+" "+transferTime);
			BasicDynaBean outSourceBean = null;
			String outSourceDestType = null;
			
			if(sampletransferred != null) {
				outSourceBean = dao.getDiagOutSourceDetails(Integer.parseInt(outSourceDestId));
				outSourceDestType = (String) outSourceBean.get("outsource_dest_type");
				
				sampleCollectionBean.set("sample_transfer_status", "T");
				sampleCollectionBean.set("transfer_user", transferUser);				
				String batchId = (String) sampleCollectionBean.get("transfer_batch_id");
				if (batchId == null || batchId.equals("")) {
					String centerCode = (String) new CenterMasterDAO().findByKey("center_id", centerId).get("center_code");
					String newBatchId = centerCode + "-" + DateUtil.currentDate("yyMMddHHmmss");

					sampleCollectionBean.set("transfer_batch_id", newBatchId);
					sampleCollectionBean.set("outsource_dest_id", Integer.parseInt(outSourceDestId));			

					testPrescBean.set("outsource_dest_id", Integer.parseInt(outSourceDestId));
				}
			} else {
				sampleCollectionBean.set("sample_transfer_status", "P");
				sampleCollectionBean.set("transfer_batch_id", ""); // remove the batchid while reverting transfer status.
				sampleCollectionBean.set("transfer_user", "");
			}
			sampleCollectionBean.set("transfer_time", transferredTime);
			sampleCollectionBean.set("transfer_other_details", transferOtherDetails);
			Map<String,Object> keys = new HashMap<String, Object>();
			keys.put("sample_collection_id", sampleCollectionId);
			int j= sampleCollectionDAO.update(con, sampleCollectionBean.getMap(), keys);
			if(j>0) success = true;			
			if (success) {				
				testPrescBean.set("is_outhouse_selected",  !"".equals(outSourceDestId) ? "Y" : "N");
				if ( !"".equals(outSourceDestId))
					testPrescBean.set("prescription_type", "o");
				if (null != outSourceBean && null != outSourceBean.get("protocol") && outSourceBean.get("protocol").equals("hl7"))
					testPrescBean.set("conduction_type", "i");
				Map<String,Object> keys1 = new HashMap<String, Object>();
				keys1.put("sample_collection_id", sampleCollectionId);
				success = testsPrescribedDAO.update(con, testPrescBean.getMap(), keys1) > 0;
			}
			
			if (success) {
				if (outSourceDestType != null && outSourceDestType.equals("C") && isOutSourceDestIdExist == null) {
					AutomaticSampleRegistration incomingSampleReg = new AutomaticSampleRegistration(); 
					Integer conductionCenterId = (Integer) outSourceBean.get("center_id");						 						 
					InternalLab labDto = new InternalLab();
					labDto.setInternalLabTestIds(testIdArray);
					labDto.setInternalLabPrescIds(prescIdArray);
					labDto.setInternalLabSampleNos(sampleNoArray);
					labDto.setInternalLabSampleTypeIds(sampleTypeIdArray);
					labDto.setInternalLabCenterId(conductionCenterId.toString());
					success &= incomingSampleReg.sampleRegistrationInInternalLab(con, labDto, patientId, diagGenericPref);
					
				} else if (outSourceDestType != null && (outSourceDestType.equals("O") || outSourceDestType.equals("IO")) && isOutSourceDestIdExist == null) {
					//insert out house details and update payment	
					 for(int m=0;m<prescIdArray.length;m++) {							 
						 ohDetails.setVisitId(patientId);
						 ohDetails.setPrescribedId(Integer.parseInt(prescIdArray[m].trim()));
						 ohDetails.setSampleNo(sampleNoArray[m]);
						 ohDetails.setTestId(testIdArray[m].trim());
						 ohDetails.setoutSourceId(outSourceDestId.toString());
						 success &= LaboratoryDAO.setSamplesToOuthouse(ohDetails,con);
						 if (success) {								
							 String chargeId = LaboratoryDAO.getOhTestChargeId(con, ohDetails.getPrescribedId(),ChargeDTO.CH_DIAG_LAB);
							 success &= chargeBo.updateOhPayment(con,chargeId, visitCenterId);
						 }
					 }
				}	
			}
			
			if ( outsourceDestPrescribedId != null && !"".equals(outsourceDestPrescribedId))
				redirect.addParameter("outsourceDestPrescribedId", outsourceDestPrescribedId);
			redirect.addParameter("sampleCollectionId", sampleCollectionId);
			redirect.addParameter("patient_id", patientId);

			allSuccess = true;
			allSuccess &= success;
			
			return redirect;

		}finally{
			DataBaseUtil.commitClose(con, allSuccess);
		}
	}

}
