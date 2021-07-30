/**
 *
 */
package com.insta.hms.pbmauthorization;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.Accounting.AccountingGroupMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.digester.Digester;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.http.HttpSession;

/**
 * @author lakshmi
 *
 */
public class PriorAuthorizationXmlProvider {

	private Digester digester;
	
    private static final GenericDAO insuranceDenialCodesDAO =
        new GenericDAO("insurance_denial_codes");

	public PriorAuthorizationXmlProvider() {
		digester = new Digester();
		digester.setValidating(false);

		digester.addObjectCreate("Prior.Authorization", "com.insta.hms.pbmauthorization.PriorAuthorization");

		// Header
		digester.addObjectCreate("Prior.Authorization/Header", "com.insta.hms.pbmauthorization.PriorAuthorizationHeader");
		digester.addBeanPropertySetter("Prior.Authorization/Header/SenderID","senderID");
		digester.addBeanPropertySetter("Prior.Authorization/Header/ReceiverID", "receiverID");
		digester.addBeanPropertySetter("Prior.Authorization/Header/TransactionDate", "transactionDate");
		digester.addBeanPropertySetter("Prior.Authorization/Header/RecordCount", "recordCount");
		digester.addBeanPropertySetter("Prior.Authorization/Header/DispositionFlag", "dispositionFlag");
		digester.addSetNext("Prior.Authorization/Header","setHeader");


		// Authorization
		digester.addObjectCreate("Prior.Authorization/Authorization", "com.insta.hms.pbmauthorization.PriorAuthAuthorization");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Result","authorizationResult");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/ID", "authorizationID");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/IDPayer", "authorizationIDPayer");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/DenialCode", "denialCode");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Start", "start");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/End", "end");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Limit", "limit");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Comments", "comments");


		// Prior Auth. Unbound Activity
		digester.addObjectCreate("Prior.Authorization/Authorization/Activity", "com.insta.hms.pbmauthorization.PriorAuthorizationActivity");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/ID", "activityID");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Type", "activityType");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Code", "activityCode");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Quantity", "quantity");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Net", "net");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/List", "list");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/PatientShare", "patientShare");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/PaymentAmount", "paymentAmount");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/DenialCode", "activityDenialCode");


		// Activity Unbound Observation
		digester.addObjectCreate("Prior.Authorization/Authorization/Activity/Observation", "com.insta.hms.pbmauthorization.PriorActivityObservation");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Observation/Type", "type");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Observation/Code", "code");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Observation/Value", "value");
		digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Observation/ValueType", "valueType");

		digester.addSetNext("Prior.Authorization/Authorization/Activity/Observation", "addObservation");

		digester.addSetNext("Prior.Authorization/Authorization/Activity", "addActivity");

		digester.addSetNext("Prior.Authorization/Authorization","setAuthorization");
	}

	public PriorAuthorization getPriorAuthorizationMetaDataDescription(InputStream is)
		throws IOException, org.xml.sax.SAXException, SQLException {
		PriorAuthorization desc = (PriorAuthorization) new PriorAuthorizationXmlProvider().digester.parse(is);
		is.close();
		return desc;
	}

	PBMPrescriptionsDAO pbmdao = new PBMPrescriptionsDAO();
	GenericDAO pbmReqDao = new GenericDAO("pbm_request_approval_details");
	GenericDAO prescReqDao = new GenericDAO("pbm_prescription_request");
	GenericDAO pbmMedPrescDAO = new GenericDAO("pbm_medicine_prescriptions");

	public String validatePriorAuthorizationXml(PriorAuthorization desc,
			BasicDynaBean pbmPriorAuthBean, String pbmRequestId) throws Exception {
		SimpleDateFormat dtFmt = new SimpleDateFormat("dd/MM/yyyy hh:mm");
		String errorMsg = "";
		String pbmServiceRegNo = null;
		int pbmAccountGroup = 0;

		HttpSession session = RequestContext.getSession();

		PriorAuthorizationHeader header = desc.getHeader();
		if (header == null) {
			errorMsg = "Prior Auth. XML parsing failed: Prior Authorization Header element missing...";
			return errorMsg;
		} else {
			String senderId = header.getSenderID();
			String receiverID = header.getReceiverID();
			String dispositionFlag = header.getDispositionFlag();
			String transactionDate = header.getTransactionDate();

			pbmAccountGroup = pbmPriorAuthBean.get("account_group") != null ? (Integer)pbmPriorAuthBean.get("account_group") : 0;
			if (pbmAccountGroup != 0) {
				BasicDynaBean accbean = new AccountingGroupMasterDAO().findByKey("account_group_id", pbmAccountGroup);
				pbmServiceRegNo =  accbean != null ? (String)accbean.get("account_group_service_reg_no") : null;
			}else {
				int pbmCenter = pbmPriorAuthBean.get("center_id") != null ? (Integer)pbmPriorAuthBean.get("center_id") : 0;
				BasicDynaBean centerbean = new CenterMasterDAO().findByKey("center_id", pbmCenter);
				pbmServiceRegNo =  centerbean != null ? (String)centerbean.get("hospital_center_service_reg_no") : null;
			}

			if(transactionDate.trim().length() <= 10)
				transactionDate = transactionDate + " 00:00";
			int recordCount = header.getRecordCount();

			if (senderId == null || senderId.equals("")) {
				errorMsg = "Prior Auth. XML parsing failed: SenderID not found in Header...";
				return errorMsg;
			}

			if(!TpaMasterDAO.getTpaPayerID((String)pbmPriorAuthBean.get("tpa_id")).get("payer_id").equals(senderId)) {
				errorMsg = "Prior Auth. XML parsing failed: TPA is not same as the PBM Authorization sender...";
				return errorMsg;
			}

			if (receiverID == null || receiverID.equals("")) {
				errorMsg = "Prior Auth. XML parsing failed: ReceiverID not found in Header...";
				return errorMsg;
			}

			if(pbmServiceRegNo == null || !(pbmServiceRegNo).equals(receiverID)) {
				if (pbmAccountGroup != 0)
					errorMsg = "Prior Auth. XML parsing failed: Receiver ID not same as the account service registration no. ...";
				else
					errorMsg = "Prior Auth. XML parsing failed: Receiver ID not same as the center service registration no. ...";
				return errorMsg;
			}

			if (transactionDate == null || transactionDate.equals("")) {
				errorMsg = "Prior Auth. XML parsing failed: TransactionDate not found in Header...";
				return errorMsg;
			} else if (dtFmt.parse(transactionDate) == null) {
				errorMsg = "Prior Auth. XML parsing failed: TransactionDate is not a valid date...";
				return errorMsg;
			}

			if (recordCount < 0) {
				errorMsg = "Prior Auth. XML parsing failed: RecordCount is not valid...";
				return errorMsg;
			}

			if (dispositionFlag == null || dispositionFlag.equals("")) {
				errorMsg = "Prior Auth. XML parsing failed: DispositionFlag not found in Header...";
				return errorMsg;
			} else if (!dispositionFlag.equalsIgnoreCase("PRODUCTION") && !dispositionFlag.equalsIgnoreCase("TEST")) {
				errorMsg = "Prior Auth. XML parsing failed: DispositionFlag value is not valid...";
				return errorMsg;
			}
		}// End of header validation

		PriorAuthAuthorization priorAuth = desc.getAuthorization();
		if (priorAuth == null) {
			errorMsg = "Prior Auth. XML parsing failed: No Authorization element found...";
			return errorMsg;
		} else {
			String authReqID = priorAuth.getAuthorizationID();
			String authIdPayer = priorAuth.getAuthorizationIDPayer();
			String authDenialCode = priorAuth.getDenialCode();
			String start = priorAuth.getStart();
			String end = priorAuth.getEnd();
			String authResult = priorAuth.getAuthorizationResult();
			BigDecimal limit = priorAuth.getLimit();
			String comments = priorAuth.getComments();

			ArrayList activities = priorAuth.getActivities();

			if (authReqID == null || authReqID.equals("")) {
				errorMsg = "Prior Auth. XML parsing failed: ID not found for Authorization...";
				return errorMsg;
			}

			if (pbmRequestId != null && !authReqID.equals(pbmRequestId)) {
				errorMsg = "Prior Auth. XML parsing failed: Authorization.ID "+authReqID
								+" does not match with PBM Request ID: "+pbmRequestId;
				return errorMsg;
			}

			BasicDynaBean pbmReqBean = pbmReqDao.findByKey("pbm_request_id", authReqID);
			BasicDynaBean prescReqbean = prescReqDao.findByKey("pbm_request_id", authReqID);
			if (pbmReqBean == null || prescReqbean == null) {
				errorMsg = "Prior Auth. XML parsing failed: Invalid Auth. ID (or) No Request exists with ID: "+authReqID;
				return errorMsg;
			}

			String requestType = (String)pbmReqBean.get("pbm_request_type");
			int pbmPrescId = (Integer)prescReqbean.get("pbm_presc_id");
			BasicDynaBean pbmbean = pbmdao.getPBMPresc(pbmPrescId);
			String pbmPresStatus = (String)pbmbean.get("pbm_presc_status");

			if (requestType.equals("Authorization")) {
				if (pbmPresStatus == null || !pbmPresStatus.equals("S")) {
					errorMsg = "Prior Auth. XML parsing failed: PBM Prescription "+pbmPrescId+" is not marked as Sent...";
					return errorMsg;
				}
			}

			if (authIdPayer == null || authIdPayer.equals("")) {
				errorMsg = "Prior Auth. XML parsing failed: IDPayer not found for Authorization...";
				return errorMsg;
			}

			if (start != null && (dtFmt.parse(start) == null || (new java.util.Date()).before(dtFmt.parse(start)))) {
				errorMsg = "Prior Auth. XML parsing failed: Start Date value for Authorization is invalid...";
				return errorMsg;
			}

			/*if (end != null && (dtFmt.parse(end) == null || (new java.util.Date()).before(dtFmt.parse(end)))) {
				errorMsg = "Prior Auth. XML parsing failed: End Date value for Authorization is invalid...";
				return errorMsg;
			}*/

			if (end != null && (dtFmt.parse(end) == null)) {
				errorMsg = "Prior Auth. XML parsing failed: End Date value for Authorization is invalid...";
				return errorMsg;
			}

			if (authDenialCode != null && !authDenialCode.trim().equals("")
					&& insuranceDenialCodesDAO.findByKey("denial_code", authDenialCode)== null){
				errorMsg = "Prior Auth. XML parsing failed: Denial Code value ("+authDenialCode+") not valid for Authorization...";
				return errorMsg;
			}

			if ((requestType.equals("Authorization")) && (activities == null || activities.isEmpty() || activities.size() < 1)) {
				errorMsg = "Prior Auth. XML parsing failed: No Activities found for Authorization with PBM Prescription ID "+pbmPrescId+"...";
				return errorMsg;
			} else {
				for (int j = 0; j < activities.size(); j++) {
					PriorAuthorizationActivity activity = (PriorAuthorizationActivity)activities.get(j);
					String activityId = activity.getActivityID();
					String type = activity.getActivityType();
					String code = activity.getActivityCode();
					BigDecimal net = activity.getNet();
					String activityDenialCode = activity.getActivityDenialCode();
					BigDecimal quantity = activity.getQuantity();
					BigDecimal list = activity.getList();
					BigDecimal patientShare = activity.getPatientShare();
					BigDecimal paymentAmount = activity.getPaymentAmount();

					ArrayList observations = activity.getObservations();

					if (activityId == null || activityId.equals("")) {
						errorMsg = "Prior Auth. XML parsing failed: ID not found for Activity in Authorization with PBM Prescription ID "+pbmPrescId+"...";
						return errorMsg;
					} else {

						String actId = activityId.split("-")[0];
						int medPresId = Integer.parseInt(actId);
						BasicDynaBean activityBean = pbmMedPrescDAO.findByKey("pbm_medicine_pres_id", medPresId);
						if(activityBean == null){
							errorMsg = "Prior Auth. XML parsing failed: ID "+activityId+" for Activity not valid in Authorization with PBM Prescription ID "+pbmPrescId+"...";
							return errorMsg;
						}

						Integer actPbmPrescId = activityBean.get("pbm_presc_id") != null ? (Integer)activityBean.get("pbm_presc_id") : null;
						if(actPbmPrescId == null || actPbmPrescId.intValue() != pbmPrescId){
							errorMsg = "Prior Auth. XML parsing failed: Invalid ID "+activityId+" for Activity found in Authorization with PBM Prescription ID "+pbmPrescId+"...";
							return errorMsg;
						}

						if (type == null || type.equals("")) {
							errorMsg = "Prior Auth. XML parsing failed: Type value not found for Activity "+activityId+" in Authorization with PBM Prescription ID "+pbmPrescId+"...";
							return errorMsg;
						}

						if (code == null || code.equals("")) {
							errorMsg = "Prior Auth. XML parsing failed: Code value not found for Activity "+activityId+" in Authorization with PBM Prescription ID "+pbmPrescId+"...";
							return errorMsg;
						}

						if (net == null) {
							errorMsg = "Prior Auth. XML parsing failed: Net value not found for Activity "+activityId+" in Authorization with PBM Prescription ID "+pbmPrescId+"...";
							return errorMsg;
						}

						if(activityDenialCode != null && !activityDenialCode.trim().equals("")
								&& insuranceDenialCodesDAO.findByKey("denial_code", activityDenialCode)== null){
							errorMsg = "Prior Auth. XML parsing failed: Denial Code value not valid for Activity "+activityId+" in Authorization with PBM Prescription ID "+pbmPrescId+"...";
							return errorMsg;
						}
					}
				}// end of activity validation
			}
		}// end of authorization validation

		return errorMsg;
	}
}
