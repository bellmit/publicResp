package com.insta.hms.insurance;

import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.ClaimDAO;
import com.insta.hms.billing.ClaimSubmissionDAO;
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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * The Class RemittanceXmlProvider.
 *
 * @author lakshmi
 */

public class RemittanceXmlProvider {

  /** The digester. */
  public static final Digester digester;

  static {
    digester = new Digester();
    digester.setValidating(false);

    digester.addObjectCreate("Remittance.Advice", "com.insta.hms.insurance.RemittanceAdvice");

    digester.addObjectCreate("Remittance.Advice/Header",
        "com.insta.hms.insurance.RemittanceAdviceHeader");
    digester.addBeanPropertySetter("Remittance.Advice/Header/SenderID", "senderID");
    digester.addBeanPropertySetter("Remittance.Advice/Header/ReceiverID", "receiverID");
    digester.addBeanPropertySetter("Remittance.Advice/Header/DispositionFlag", "dispositionFlag");
    digester.addBeanPropertySetter("Remittance.Advice/Header/TransactionDate", "transactionDate");
    digester.addBeanPropertySetter("Remittance.Advice/Header/RecordCount", "recordCount");
    digester.addSetNext("Remittance.Advice/Header", "addHeader");

    digester.addObjectCreate("Remittance.Advice/Claim",
        "com.insta.hms.insurance.RemittanceAdviceClaim");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/ID", "claimID");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/IDPayer", "idPayer");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/ProviderID", "providerID");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/DenialCode", "denialCode");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/PaymentReference", "paymentReference");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/DateSettlement", "dateSettlement");

    digester.addObjectCreate("Remittance.Advice/Claim/Encounter",
        "com.insta.hms.insurance.RemittanceAdviceEncounter");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Encounter/FacilityID", "facilityID");
    digester.addSetNext("Remittance.Advice/Claim/Encounter", "addEncounter");

    digester.addObjectCreate("Remittance.Advice/Claim/Activity",
        "com.insta.hms.insurance.RemittanceAdviceActivity");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/ID", "activityID");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/Start", "start");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/Type", "type");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/Code", "code");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/Quantity", "quantity");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/Net", "net");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/List", "list");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/Clinician", "clinician");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/PriorAuthorizationID",
        "priorAuthorizationID");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/Gross", "gross");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/PatientShare", "patientShare");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/PaymentAmount",
        "paymentAmount");
    digester.addBeanPropertySetter("Remittance.Advice/Claim/Activity/DenialCode",
        "activityDenialCode");

    digester.addSetNext("Remittance.Advice/Claim/Activity", "addActivity");
    digester.addSetNext("Remittance.Advice/Claim", "addClaim");
  }
  
  private static final GenericDAO billClaimDAO = new GenericDAO("bill_claim");
  private static final GenericDAO billChargeDAO = new GenericDAO("bill_charge");
  private static final GenericDAO insuranceDenialCodesDAO =
      new GenericDAO("insurance_denial_codes");
  private static final GenericDAO storeSalesDetailsDAO = new GenericDAO("store_sales_details");
  
  /**
   * Instantiates a new remittance xml provider.
   */
  public RemittanceXmlProvider() {
  }

  /**
   * Gets the report desc for stream.
   *
   * @param is the is
   * @return the report desc for stream
   * @throws IOException  Signals that an I/O exception has occurred. * @throws SAXException the SAX
   *                      exception
   * @throws SQLException the SQL exception
   */
  public RemittanceAdvice getReportDescForStream(InputStream is)
      throws IOException, org.xml.sax.SAXException, SQLException {
    RemittanceAdvice desc = (RemittanceAdvice) digester.parse(is);
    is.close();
    return desc;
  }

  /**
   * Validate xml for xsd conformance.
   *
   * @param desc      the desc
   * @param remitBean the remit bean
   * @return the string
   * @throws Exception the exception
   */
  public String validateXmlForXsdConformance(RemittanceAdvice desc, BasicDynaBean remitBean)
      throws Exception {
    String errorMsg = "";
    SimpleDateFormat dtFmt = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    String acountServRegNo = null;
    int remAccGrp = 0;

    ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();
    HttpSession session = RequestContext.getSession();

    ArrayList hdrz = desc.getHeader();
    if (hdrz == null || hdrz.isEmpty() || hdrz.size() < 1) {
      errorMsg = "XML parsing failed: Required Header element missing...";
      return errorMsg;
    } else {
      if (hdrz.size() > 1) {
        errorMsg = "XML parsing failed: More than 1 Header element found...";
        return errorMsg;
      } else {
        RemittanceAdviceHeader hdr = (RemittanceAdviceHeader) hdrz.get(0);
        String transactionDate = hdr.getTransactionDate();

        remAccGrp = remitBean.get("account_group") != null
            ? (Integer) remitBean.get("account_group")
            : 0;
        if (remAccGrp != 0) {
          BasicDynaBean accbean = new AccountingGroupMasterDAO().findByKey("account_group_id",
              remAccGrp);
          acountServRegNo = accbean != null ? (String) accbean.get("account_group_service_reg_no")
              : null;
        } else {
          int remCenter = remitBean.get("center_id") != null ? (Integer) remitBean.get("center_id")
              : 0;
          BasicDynaBean centerbean = new CenterMasterDAO().findByKey("center_id", remCenter);
          acountServRegNo = centerbean != null
              ? (String) centerbean.get("hospital_center_service_reg_no")
              : null;
        }

        if (transactionDate.trim().length() <= 10) {
          transactionDate = transactionDate + " 00:00";
        }

        String senderId = hdr.getSenderID();
        if (senderId == null || senderId.equals("")) {
          errorMsg = "XML parsing failed: SenderID not found in Header...";
          return errorMsg;
        }

        if (!TpaMasterDAO.getTpaPayerID((String) remitBean.get("tpa_id")).get("payer_id")
            .equals(senderId)) {
          errorMsg = "XML parsing failed: TPA selected not same as the remittance advice sender...";
          return errorMsg;
        }

        String receiverID = hdr.getReceiverID();
        if (receiverID == null || receiverID.equals("")) {
          errorMsg = "XML parsing failed: ReceiverID not found in Header...";
          return errorMsg;
        }

        if (acountServRegNo == null || !(acountServRegNo).equals(receiverID)) {
          if (remAccGrp != 0) {
            errorMsg = "XML parsing failed: "
                + "Receiver ID not same as the account service registration no. ...";
          } else {
            errorMsg = "XML parsing failed: "
                + "Receiver ID not same as the center service registration no. ...";
          }
          return errorMsg;
        }

        if (transactionDate == null || transactionDate.equals("")) {
          errorMsg = "XML parsing failed: TransactionDate not found in Header...";
          return errorMsg;
        } else if (dtFmt.parse(transactionDate) == null) {
          errorMsg = "XML parsing failed: TransactionDate is not a valid date...";
          return errorMsg;
        }

        int recordCount = hdr.getRecordCount();
        if (recordCount < 0) {
          errorMsg = "XML parsing failed: RecordCount is not valid...";
          return errorMsg;
        }

        String dispositionFlag = hdr.getDispositionFlag();
        if (dispositionFlag == null || dispositionFlag.equals("")) {
          errorMsg = "XML parsing failed: DispositionFlag not found in Header...";
          return errorMsg;
        } else if (!dispositionFlag.equalsIgnoreCase("PRODUCTION")
            && !dispositionFlag.equalsIgnoreCase("TEST")) {
          errorMsg = "XML parsing failed: DispositionFlag value is not valid...";
          return errorMsg;
        }
      }
    } // End of header validation

    ArrayList claims = desc.getClaim();
    if (claims == null || claims.isEmpty() || claims.size() < 1) {
      errorMsg = "XML parsing failed: No claim elements found...";
      return errorMsg;
    } else if (claims.size() >= 1) {
      for (int claimIndex = 0; claimIndex < claims.size(); claimIndex++) {
        RemittanceAdviceClaim claim = (RemittanceAdviceClaim) claims.get(claimIndex);
        String claimID = claim.getClaimID();
        ArrayList encounters = claim.getEncounter(); // encounter facility ID validation?

        if (claimID == null || claimID.equals("")) {
          errorMsg = "XML parsing failed: ID not found for Claim...";
          return errorMsg;
        }

        BasicDynaBean claimBean = new ClaimDAO().findClaimById(claimID);

        if (claimBean == null) {
          errorMsg = "XML parsing failed: Invalid Claim ID (or) No Claim exists with Claim ID: "
              + claim.getClaimID();
          return errorMsg;
        }

        int resubmissionCount = claimBean.get("resubmission_count") != null
            ? (Integer) claimBean.get("resubmission_count")
            : 0;
        String isResubmission = resubmissionCount > 0 ? "Y" : "N";
        BasicDynaBean batchBean = new ClaimSubmissionDAO().getLatestSubmissionBatch(claimID,
            isResubmission);
        String batchStatus = (batchBean != null && batchBean.get("status") != null)
            ? (String) batchBean.get("status")
            : null;
        String batchId = (batchBean != null && batchBean.get("submission_batch_id") != null)
            ? (String) batchBean.get("submission_batch_id")
            : null;

        if (batchBean == null || batchId == null) {
          errorMsg = "XML parsing failed: Invalid Claim ID " + claim.getClaimID()
              + ". No Submission Batch ID found for the Claim...";
          return errorMsg;
        }

        if (batchStatus == null || !batchStatus.equals("S")) {
          errorMsg = "XML parsing failed: Submission Batch " + batchId
              + " is not marked as Sent. Please mark it as Sent and Upload again...";
          return errorMsg;
        }

        String idPayer = claim.getIdPayer();
        if (idPayer == null || idPayer.equals("")) {
          errorMsg = "XML parsing failed: IDPayer not found for Claim " + claim.getClaimID()
              + "...";
          return errorMsg;
        }

        String providerId = claim.getProviderID();
        if (providerId == null || providerId.equals("")) {
          errorMsg = "XML parsing failed: ProviderID not found for Claim " + claim.getClaimID()
              + "...";
          return errorMsg;
        }

        if (acountServRegNo == null || !(acountServRegNo).equals(providerId)) {
          if (remAccGrp != 0) {
            errorMsg = "XML parsing failed: "
                + "Provider ID not same as the account service registration no....";
          } else {
            errorMsg = "XML parsing failed: "
                + "Provider ID not same as the center service registration no. ...";
          }
          return errorMsg;
        }

        String paymentReference = claim.getPaymentReference();
        if (paymentReference == null || paymentReference.equals("")) {
          errorMsg = "XML parsing failed: Payment Reference not found for Claim "
              + claim.getClaimID() + "...";
          return errorMsg;
        }

        String dateSettlement = claim.getDateSettlement();
        if (dateSettlement != null && (dtFmt.parse(dateSettlement) == null
            || (new java.util.Date()).before(dtFmt.parse(dateSettlement)))) {
          errorMsg = "XML parsing failed: DateSettlement value for Claim " + claim.getClaimID()
              + " is invalid...";
          return errorMsg;
        }
        String claimDenialCode = claim.getDenialCode();
        if (claimDenialCode != null && !claimDenialCode.trim().equals("")
            && insuranceDenialCodesDAO.findByKey("denial_code", claimDenialCode) == null) {
          errorMsg = "XML parsing failed: Denial Code value not valid for Claim "
              + claim.getClaimID() + "...";
          return errorMsg;
        }

        ArrayList activities = claim.getActivities();
        if (activities == null || activities.isEmpty() || activities.size() < 1) {
          errorMsg = "XML parsing failed: No Activity found for Claim " + claim.getClaimID()
              + "....";
          return errorMsg;
        } else {
          for (int activityIndex = 0; activityIndex < activities.size(); activityIndex++) {
            RemittanceAdviceActivity activity = (RemittanceAdviceActivity) activities
                .get(activityIndex);
            String activityId = activity.getActivityID();

            if ((activityId.split("-")).length < 2 || (!(activityId.split("-")[0]).equals("A")
                && !(activityId.split("-")[0]).equals("P"))) {
              errorMsg = "XML parsing failed: Invalid activity found. Activity Id : " + activityId
                  + " is not prefixed with A or P or not a valid activity id.";
              return errorMsg;
            }

            if ((activityId.split("-")[0]).equals("A") && (activityId.split("-")).length > 2) {
              String resubmissionID = activityId.split("-")[2];
              boolean exists = submitdao.exist("submission_batch_id", resubmissionID);
              if (!exists) {
                errorMsg = "XML parsing failed: Invalid activity found. Resubmitted Activity Id : "
                    + activityId + " is not suffixed a valid resubmission batch id.";
                return errorMsg;
              }
            }

            if ((activityId.split("-")[0]).equals("P") && (activityId.split("-")).length > 3) {
              String resubmissionID = activityId.split("-")[3];
              boolean exists = submitdao.exist("submission_batch_id", resubmissionID);
              if (!exists) {
                errorMsg = "XML parsing failed: Invalid activity found. Resubmitted Activity Id : "
                    + activityId + " is not suffixed a valid resubmission batch id.";
                return errorMsg;
              }
            }

            if (activityId == null || activityId.equals("")) {
              errorMsg = "XML parsing failed: ID not found for Activity in Claim  "
                  + claim.getClaimID() + "...";
              return errorMsg;
            } else {
              if (activityId.startsWith("A-")) {
                String chargeActyId = activityId.split("-")[1];
                BasicDynaBean chargeBean = billChargeDAO.findByKey("charge_id",
                    chargeActyId);
                if (chargeBean == null) {
                  errorMsg = "XML parsing failed: ID for Activity not valid in Claim "
                      + claim.getClaimID() + "...";
                  return errorMsg;
                }

                Map<String, Object> claimRef = new HashMap<String, Object>();
                claimRef.put("bill_no", chargeBean.get("bill_no"));
                claimRef.put("claim_id", claimID);
                BasicDynaBean billBean = billClaimDAO.findByKey(claimRef);

                if (billBean == null || billBean.get("claim_id") == null
                    || !billBean.get("claim_id").equals(claimID)) {
                  errorMsg = "XML parsing failed: Remittance Claim ID: " + claim.getClaimID()
                      + " does not match with Claim Id: " + billBean.get("claim_id")
                      + " for Bill No: " + chargeBean.get("bill_no") + " : Charge Id: "
                      + chargeActyId;
                  return errorMsg;
                }

              } else if (activityId.startsWith("P-")) {

                if ((activityId.split("-")).length < 3) {
                  errorMsg = "XML parsing failed: Invalid activity found, Activity Id : "
                      + activityId + " prefixed with P does not have sale item id.";
                  return errorMsg;
                }
                String medChargeId = activityId.split("-")[1];
                Integer medSaleitemId = new Integer(activityId.split("-")[2]);
                BasicDynaBean chargeBean = billChargeDAO.findByKey("charge_id",
                    medChargeId);
                if (chargeBean == null
                    || storeSalesDetailsDAO.findByKey("sale_item_id", medSaleitemId) == null) {
                  errorMsg = "XML parsing failed: ID for Activity not valid in Claim "
                      + claim.getClaimID() + "...";
                  return errorMsg;
                }

                Map<String, Object> claimRef = new HashMap<String, Object>();
                claimRef.put("bill_no", chargeBean.get("bill_no"));
                claimRef.put("claim_id", claimID);
                BasicDynaBean billBean = billClaimDAO.findByKey(claimRef);

                if (billBean == null || billBean.get("claim_id") == null
                    || !billBean.get("claim_id").equals(claimID)) {
                  errorMsg = "XML parsing failed: Remittance Claim ID: " + claim.getClaimID()
                      + " does not match with Claim Id: " + billBean.get("claim_id")
                      + " for Bill No: " + chargeBean.get("bill_no") + " : Charge Id: "
                      + medChargeId;
                  return errorMsg;
                }
              }
            }
            String start = activity.getStart();
            if (start.trim().length() <= 10) {
              start = start + " 00:00";
            }
            if (start == null || start.equals("")) {
              errorMsg = "XML parsing failed: Start date not found for Activity " + activityId
                  + " in Claim " + claim.getClaimID() + "...";
              return errorMsg;
            } else if (dtFmt.parse(start) == null
                || (new java.util.Date()).before(dtFmt.parse(start))) {
              errorMsg = "XML parsing failed: Start date value not valid for Activity " + activityId
                  + " in Claim " + claim.getClaimID() + "...";
              return errorMsg;
            }

            String type = activity.getType();
            if (type == null || type.equals("")) {
              errorMsg = "XML parsing failed: Type value not found for Activity " + activityId
                  + " in Claim " + claim.getClaimID() + "...";
              return errorMsg;
            }

            String code = activity.getCode();
            if (code == null || code.equals("")) {
              errorMsg = "XML parsing failed: Code value not found for Activity " + activityId
                  + " in Claim " + claim.getClaimID() + "...";
              return errorMsg;
            }

            BigDecimal quantity = activity.getQuantity();
            if (quantity == null) {
              errorMsg = "XML parsing failed: Quantity value not found for Activity " + activityId
                  + " in Claim " + claim.getClaimID() + "...";
              return errorMsg;
            }
            BigDecimal net = activity.getNet();
            if (net == null) {
              errorMsg = "XML parsing failed: Net value not found for Activity " + activityId
                  + " in Claim " + claim.getClaimID() + "...";
              return errorMsg;
            }

            String clinician = activity.getClinician();
            if (clinician == null || clinician.equals("")) {
              errorMsg = "XML parsing failed: Clinician value not found for Activity " + activityId
                  + " in Claim " + claim.getClaimID() + "...";
              return errorMsg;
            }

            BigDecimal paymentAmount = activity.getPaymentAmount();
            if (paymentAmount == null) {
              errorMsg = "XML parsing failed: PaymentAmount value not found for Activity "
                  + activityId + " in Claim " + claim.getClaimID() + "...";
              return errorMsg;
            }

            String activityDenialCode = activity.getActivityDenialCode();
            if (activityDenialCode != null && !activityDenialCode.trim().equals("")
                && insuranceDenialCodesDAO.findByKey("denial_code", activityDenialCode) == null) {
              errorMsg = "XML parsing failed: Denial Code value not valid for Activity in Claim "
                  + claim.getClaimID() + "...";
              return errorMsg;
            }

          }

        } // end of activity validation

      }
    } // end of claim validation

    if (claims.size() != ((RemittanceAdviceHeader) hdrz.get(0)).getRecordCount()) {
      errorMsg = "XML parsing failed: Total claims found "
          + "do not match with the Header's RecordCount...";
      return errorMsg;
    }
    return errorMsg;
  }
}
