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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * The Class XMLItemRemittanceProvider.
 *
 * @author lakshmi
 */

public class XMLItemRemittanceProvider extends XMLRemittanceProvider {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(XMLRemittanceProvider.class);

  /** The dt fmt. */
  // public static Digester digester = new DHARemittanceXMLDigester();
  SimpleDateFormat dtFmt = new SimpleDateFormat("dd/MM/yyyy hh:mm");

  /** The digester map. */
  static Map<String, Digester> digesterMap = new HashMap<String, Digester>();

  static {
    digesterMap.put("DHA", new DHARemittanceXMLDigester());
    digesterMap.put("HAAD", new HAADRemittanceXMLDigester());
  }
  
  private static final GenericDAO billChargeDAO = new GenericDAO("bill_charge");
  private static final GenericDAO billClaimDAO = new GenericDAO("bill_claim");
  private static final GenericDAO insuranceDenialCodesDAO =
      new GenericDAO("insurance_denial_codes");
  private static final GenericDAO storeSalesDetailsDAO = new GenericDAO("store_sales_details");
  
  /**
   * Instantiates a new XML item remittance provider.
   */
  public XMLItemRemittanceProvider() {

  }

  /**
   * get remittance advice.
   *
   * @param is        the is
   * @param remitBean the remit bean
   * @param errorMap  the error map
   * @return the remittance advice
   * @throws IOException    Signals that an I/O exception has occurred. //@throws SAXException the
   *                        SAX exception
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public RemittanceAdvice getRemittanceAdvice(InputStream is, BasicDynaBean remitBean, Map errorMap)
      throws IOException, org.xml.sax.SAXException, SQLException, ParseException {

    Digester digester = getDigester(remitBean);
    if (digester == null) {
      errorMap.put("error", "Unknown Health authority cannot parse the remittance advice file");
      return null;
    } else {
      RemittanceAdvice desc = (RemittanceAdvice) digester.parse(is);
      if (null != desc) {
        String errMsg = validateRemittanceXML(desc, remitBean);
        if (null != errMsg && !"".equals(errMsg.trim())) {
          errorMap.put("error", errMsg);
        }
      }
      return desc;
    }
  }

  /**
   * Gets the digester.
   *
   * @param remitBean the remit bean
   * @return the digester
   * @throws SQLException the SQL exception
   */
  private Digester getDigester(BasicDynaBean remitBean) throws SQLException {
    // check for center id and corresponding healthauthority.
    // set the digester based on health authority
    int centerID = (Integer) remitBean.get("center_id");
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerID);
    return digesterMap.get(healthAuthority);
  }

  /**
   * Validate remittance XML.
   *
   * @param desc      the desc
   * @param remitBean the remit bean
   * @return the string
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public String validateRemittanceXML(RemittanceAdvice desc, BasicDynaBean remitBean)
      throws SQLException, ParseException {
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
            errorMsg = "XML parsing failed: Receiver ID not same as the "
                + "account service registration no. ...";
          } else {
            errorMsg = "XML parsing failed: Receiver ID not same as the "
                + "center service registration no. ...";
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
            errorMsg = "XML parsing failed: Provider ID not same as the"
                + " account service registration no. ...";
          } else {
            errorMsg = "XML parsing failed: Provider ID not same as the "
                + "center service registration no. ...";
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

            final String type = activity.getType();
            final String code = activity.getCode();

            if ((activityId.split("-")).length < 2 || (!(activityId.split("-")[0]).equals("A")
                && !(activityId.split("-")[0]).equals("P"))) {
              errorMsg = "XML parsing failed: Invalid activity found. Activity Id : " + activityId
                  + " is not prefixed with A or P or not a valid activity id.";
              return errorMsg;
            }

            boolean isRepeatingItem = activityId.split("-")[1].equals("ACT");

            if ((activityId.split("-")[0]).equals("A")
                && (isRepeatingItem ? ((activityId.split("-")).length > 3)
                    : ((activityId.split("-")).length > 2))) {
              String resubmissionID = isRepeatingItem ? activityId.split("-")[3]
                  : activityId.split("-")[2];
              boolean exists = submitdao.exist("submission_batch_id", resubmissionID);
              if (!exists) {
                errorMsg = "XML parsing failed: Invalid activity found. Resubmitted Activity Id : "
                    + activityId + " is not suffixed a valid resubmission batch id.";
                return errorMsg;
              }
            }

            if ((activityId.split("-")[0]).equals("P")
                && (isRepeatingItem ? ((activityId.split("-")).length > 4)
                    : ((activityId.split("-")).length > 3))) {
              String resubmissionID = isRepeatingItem ? activityId.split("-")[4]
                  : activityId.split("-")[3];
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
                if (chargeActyId.equals("ACT")) {
                  chargeActyId = activityId.split("-")[2];
                }
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
                boolean isRepeatedItem = activityId.split("-")[1].equals("ACT");
                String medChargeId = isRepeatedItem ? activityId.split("-")[2]
                    : activityId.split("-")[1];
                Integer medSaleitemId = new Integer(
                    isRepeatedItem ? activityId.split("-")[3] : activityId.split("-")[2]);

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

            if (type == null || type.equals("")) {
              errorMsg = "XML parsing failed: Type value not found for Activity " + activityId
                  + " in Claim " + claim.getClaimID() + "...";
              return errorMsg;
            } /*
               * else if (type < 0 || type > 9 || type == 7) { errorMsg =
               * "XML parsing failed: Type value for Activity "+activityId+
               * " is not valid in Claim "+claim.getClaimID()+"..."; return errorMsg; }
               */

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
      errorMsg = 
          "XML parsing failed: Total claims found do not match with the Header's RecordCount...";
      return errorMsg;
    }
    return errorMsg;
  }

  /*
   * private boolean validateRemittanceXML(RemittanceAdvice desc, BasicDynaBean remitBean, Map
   * errorMap) throws SQLException, ParseException {
   * 
   * ArrayList hdrz = desc.getHeader(); boolean headerOK = validateHeaderElements(hdrz, remitBean,
   * errorMap); if (!headerOK) return false;
   * 
   * ArrayList claims = desc.getClaim();
   * 
   * if (claims == null || claims.isEmpty() || claims.size() < 1) { errorMap.put("error",
   * "XML parsing failed: No claim elements found..."); return false; }
   * 
   * if (claims.size() != ((RemittanceAdviceHeader) hdrz.get(0)).getRecordCount()) {
   * errorMap.put("error",
   * "XML parsing failed: Total claims found do not match with the Header's RecordCount..."); return
   * false; }
   * 
   * for (int i = 0; i < claims.size(); i++) { RemittanceAdviceClaim claim = (RemittanceAdviceClaim)
   * claims.get(i); boolean claimOK = validateClaim(claim, errorMap); if (!claimOK) return false; }
   * 
   * return true; }
   * 
   * private boolean validateClaim(RemittanceAdviceClaim claim, Map errorMap) throws SQLException,
   * ParseException { String claimID = claim.getClaimID(); String idPayer = claim.getIdPayer();
   * String providerId = claim.getProviderID(); String paymentReference =
   * claim.getPaymentReference(); String dateSettlement = claim.getDateSettlement(); ArrayList
   * encounters = claim.getEncounter(); // encounter facility ID validation? ArrayList activities =
   * claim.getActivities();
   * 
   * SimpleDateFormat dtFmt = new SimpleDateFormat("dd/MM/yyyy hh:mm"); String acountServRegNo =
   * null; int remAccGrp = 0;
   * 
   * if (claimID == null || claimID.equals("")) { errorMap.put("error",
   * "XML parsing failed: ID not found for Claim..."); return false; }
   * 
   * BasicDynaBean claimBean = new ClaimDAO().findClaimById(claimID);
   * 
   * if (claimBean == null) { errorMap.put("error",
   * "XML parsing failed: Invalid Claim ID (or) No Claim exists with Claim ID: "+claimID); return
   * false; }
   * 
   * int resubmissionCount = claimBean.get("resubmission_count") != null ?
   * (Integer)claimBean.get("resubmission_count") : 0; String isResubmission = resubmissionCount > 0
   * ? "Y" : "N"; BasicDynaBean batchBean = new
   * ClaimSubmissionDAO().getLatestSubmissionBatch(claimID, isResubmission); String batchStatus =
   * (batchBean != null && batchBean.get("status") != null) ? (String)batchBean.get("status") :
   * null; String batchId = (batchBean != null && batchBean.get("submission_batch_id") != null) ?
   * (String)batchBean.get("submission_batch_id") : null;
   * 
   * if (batchBean == null || batchId == null) { errorMap.put("error",
   * "XML parsing failed: Invalid Claim ID "+claimID+
   * ". No Submission Batch ID found for the Claim..."); return false; }
   * 
   * if (batchStatus == null || !batchStatus.equals("S")) { errorMap.put("error",
   * "XML parsing failed: Submission Batch "+batchId+
   * " is not marked as Sent. Please mark it as Sent and Upload again..."); return false; }
   * 
   * if (idPayer == null || idPayer.equals("")) { errorMap.put("error",
   * "XML parsing failed: IDPayer not found for Claim "+claimID+"..."); return false; }
   * 
   * if (providerId == null || providerId.equals("")) { errorMap.put("error",
   * "XML parsing failed: ProviderID not found for Claim "+claimID+"..."); return false; }
   * 
   * if(acountServRegNo == null || !(acountServRegNo).equals(providerId)) { if (remAccGrp != 0)
   * errorMap.put("error",
   * "XML parsing failed: Provider ID not same as the account service registration no. ..."); else
   * errorMap.put("error",
   * "XML parsing failed: Provider ID not same as the center service registration no. ..."); return
   * false; }
   * 
   * if (paymentReference == null || paymentReference.equals("")) { errorMap.put("error",
   * "XML parsing failed: Payment Reference not found for Claim "+claimID+"..."); return false; }
   * 
   * if (dateSettlement != null && (dtFmt.parse(dateSettlement) == null || (new
   * java.util.Date()).before(dtFmt .parse(dateSettlement)))) { errorMap.put("error",
   * "XML parsing failed: DateSettlement value for Claim "+claimID+" is invalid..."); return false;
   * } String claimDenialCode = claim.getDenialCode(); if(claimDenialCode!= null &&
   * !claimDenialCode.trim().equals("") && new
   * GenericDAO("insurance_denial_codes").findByKey("denial_code", claimDenialCode)== null){
   * errorMap.put("error", "XML parsing failed: Denial Code value not valid for Claim "
   * +claimID+"..."); return false; } if (activities == null || activities.isEmpty() ||
   * activities.size() < 1) { errorMap.put("error",
   * "XML parsing failed: No Activity found for Claim "+claimID+"...."); return false; }
   * 
   * for (int j = 0; j < activities.size(); j++) { RemittanceAdviceActivity activity =
   * (RemittanceAdviceActivity) activities.get(j); boolean activityOK = validateActivity(claim,
   * activity, errorMap); if (!activityOK) return false; }
   * 
   * return true; }
   * 
   * private boolean validateActivity(RemittanceAdviceClaim claim, RemittanceAdviceActivity
   * activity, Map errorMap) throws SQLException, ParseException { String activityId =
   * activity.getActivityID(); String start = activity.getStart(); String type = activity.getType();
   * String code = activity.getCode(); BigDecimal quantity = activity.getQuantity(); BigDecimal net
   * = activity.getNet(); String clinician = activity.getClinician(); BigDecimal paymentAmount =
   * activity.getPaymentAmount(); SimpleDateFormat dtFmt = new SimpleDateFormat("dd/MM/yyyy hh:mm");
   * 
   * if ((activityId.split("-")).length < 2 || (!(activityId.split("-")[0]).equals("A") &&
   * !(activityId.split("-")[0]).equals("P"))) { errorMap.put("error",
   * "XML parsing failed: Invalid activity found. Activity Id : "+activityId+
   * " is not prefixed with A or P or not a valid activity id."); return false; }
   * 
   * if ((activityId.split("-")[0]).equals("A") && (activityId.split("-")).length > 2) { String
   * resubmissionID = activityId.split("-")[2]; boolean exists =
   * submitdao.exist("submission_batch_id", resubmissionID); if (!exists) { errorMap.put("error",
   * "XML parsing failed: Invalid activity found. Resubmitted Activity Id : "+activityId+
   * " is not suffixed a valid resubmission batch id."); return false; } }
   * 
   * if ((activityId.split("-")[0]).equals("P") && (activityId.split("-")).length > 3) { String
   * resubmissionID = activityId.split("-")[3]; boolean exists =
   * submitdao.exist("submission_batch_id", resubmissionID); if (!exists) { errorMap.put("error",
   * "XML parsing failed: Invalid activity found. Resubmitted Activity Id : "+activityId+
   * " is not suffixed a valid resubmission batch id."); return false; } }
   * 
   * if (activityId == null || activityId.equals("")) { errorMap.put("error",
   * "XML parsing failed: ID not found for Activity in Claim  "+claim.getClaimID()+"..."); return
   * false; }
   * 
   * boolean validItemId = false; if(activityId.startsWith("A-")){ validItemId =
   * validateNonPharmacyItemId(claim, activityId, errorMap); }else if(activityId.startsWith("P-")){
   * validItemId = validatePharmacyItemId(claim, activityId, errorMap); } if (!validItemId) return
   * false;
   * 
   * if(start.trim().length() <= 10) start = start + " 00:00"; if (start == null ||
   * start.equals("")) { errorMap.put("error",
   * "XML parsing failed: Start date not found for Activity "+activityId+" in Claim "
   * +claim.getClaimID()+"..."); return false; }
   * 
   * if (dtFmt.parse(start) == null || (new java.util.Date()).before(dtFmt.parse(start))) {
   * errorMap.put("error", "XML parsing failed: Start date value not valid for Activity "
   * +activityId+" in Claim "+claim.getClaimID()+"..."); return false; }
   * 
   * if (type == null || type.equals("")) { errorMap.put("error",
   * "XML parsing failed: Type value not found for Activity "+activityId+" in Claim "
   * +claim.getClaimID()+"..."); return false; } // else if (type < 0 || type > 9 || type == 7) { //
   * errorMsg = "XML parsing failed: Type value for Activity "+activityId+
   * " is not valid in Claim "+claim.getClaimID()+"..."; // return false; //}
   * 
   * if (code == null || code.equals("")) { errorMap.put("error",
   * "XML parsing failed: Code value not found for Activity "+activityId+" in Claim "
   * +claim.getClaimID()+"..."); return false; }
   * 
   * if (quantity == null || quantity.equals("")) { errorMap.put("error",
   * "XML parsing failed: Quantity value not found for Activity "+activityId+" in Claim "
   * +claim.getClaimID()+"..."); return false; }
   * 
   * if (net == null || net.equals("")) { errorMap.put("error",
   * "XML parsing failed: Net value not found for Activity "+activityId+" in Claim "
   * +claim.getClaimID()+"..."); return false; }
   * 
   * if (clinician == null || clinician.equals("")) { errorMap.put("error",
   * "XML parsing failed: Clinician value not found for Activity "+activityId+" in Claim "
   * +claim.getClaimID()+"..."); return false; }
   * 
   * if (paymentAmount == null || paymentAmount.equals("")) { errorMap.put("error",
   * "XML parsing failed: PaymentAmount value not found for Activity "+activityId+" in Claim "
   * +claim.getClaimID()+"..."); return false; }
   * 
   * String activityDenialCode = activity.getActivityDenialCode(); if(activityDenialCode!= null &&
   * !activityDenialCode.trim().equals("") && new
   * GenericDAO("insurance_denial_codes").findByKey("denial_code", activityDenialCode)== null){
   * errorMap.put("error", "XML parsing failed: Denial Code value not valid for Activity in Claim "
   * +claim.getClaimID()+"..."); return false; } return true;
   * 
   * }
   * 
   * private boolean validateNonPharmacyItemId(RemittanceAdviceClaim claim, String activityId, Map
   * errorMap) throws SQLException { String claimID = claim.getClaimID(); String chargeActyId =
   * activityId.split("-")[1]; BasicDynaBean chargeBean = new
   * GenericDAO("bill_charge").findByKey("charge_id", chargeActyId); if(chargeBean == null){
   * errorMap.put("error", "XML parsing failed: ID for Activity not valid in Claim "
   * +claim.getClaimID()+"..."); return false; }
   * 
   * Map<String, Object> claimRef = new HashMap<String, Object>(); claimRef.put("bill_no",
   * chargeBean.get("bill_no")); claimRef.put("claim_id", claimID); BasicDynaBean billBean = new
   * GenericDAO("bill_claim").findByKey(claimRef);
   * 
   * if (billBean == null || billBean.get("claim_id") == null ||
   * !billBean.get("claim_id").equals(claimID)) { errorMap.put("error",
   * "XML parsing failed: Remittance Claim ID: "+claim.getClaimID() +
   * " does not match with Claim Id: "+billBean.get("claim_id")+" for Bill No: "
   * +chargeBean.get("bill_no")+" : Charge Id: "+chargeActyId); return false; } return true; }
   * 
   * private boolean validatePharmacyItemId(RemittanceAdviceClaim claim, String activityId, Map
   * errorMap) throws SQLException { String claimID = claim.getClaimID(); if
   * ((activityId.split("-")).length < 3) { errorMap.put("error",
   * "XML parsing failed: Invalid activity found, Activity Id : "+activityId+
   * " prefixed with P does not have sale item id."); return false; } String medChargeId =
   * activityId.split("-")[1]; Integer medSaleitemId = new Integer(activityId.split("-")[2]);
   * BasicDynaBean chargeBean = new GenericDAO("bill_charge").findByKey("charge_id", medChargeId);
   * if (chargeBean == null || new GenericDAO("store_sales_details").findByKey("sale_item_id",
   * medSaleitemId) == null){ errorMap.put("error",
   * "XML parsing failed: ID for Activity not valid in Claim " +claim.getClaimID()+"..."); return
   * false; }
   * 
   * Map<String, Object> claimRef = new HashMap<String, Object>(); claimRef.put("bill_no",
   * chargeBean.get("bill_no")); claimRef.put("claim_id", claimID); BasicDynaBean billBean = new
   * GenericDAO("bill_claim").findByKey(claimRef);
   * 
   * if (billBean == null || billBean.get("claim_id") == null ||
   * !billBean.get("claim_id").equals(claimID)) { errorMap.put("error",
   * "XML parsing failed: Remittance Claim ID: "+claim.getClaimID() +
   * " does not match with Claim Id: "+billBean.get("claim_id")+" for Bill No: "
   * +chargeBean.get("bill_no")+" : Charge Id: "+medChargeId); return false; } return true; }
   * 
   * private boolean validateHeaderElements(ArrayList hdrz, BasicDynaBean remitBean, Map errorMap)
   * throws SQLException, ParseException { SimpleDateFormat dtFmt = new
   * SimpleDateFormat("dd/MM/yyyy hh:mm"); String acountServRegNo = null; int remAccGrp = 0;
   * 
   * HttpSession session = RequestContext.getSession(); boolean mod_accumed =
   * (Boolean)session.getAttribute("mod_accumed");
   * 
   * if (hdrz == null || hdrz.isEmpty() || hdrz.size() < 1) { errorMap.put("error",
   * "XML parsing failed: Required Header element missing..."); return false; } if (hdrz.size() > 1)
   * { errorMap.put("error", "XML parsing failed: More than 1 Header element found..."); return
   * false; } RemittanceAdviceHeader hdr = (RemittanceAdviceHeader) hdrz.get(0); String senderId =
   * hdr.getSenderID(); String receiverID = hdr.getReceiverID(); String dispositionFlag =
   * hdr.getDispositionFlag(); String transactionDate = hdr.getTransactionDate();
   * 
   * remAccGrp = remitBean.get("account_group") != null ? (Integer)remitBean.get("account_group") :
   * 0; if (remAccGrp != 0) { BasicDynaBean accbean = new
   * AccountingGroupMasterDAO().findByKey("account_group_id", remAccGrp); acountServRegNo = accbean
   * != null ? (String)accbean.get("account_group_service_reg_no") : null; }else { int remCenter =
   * remitBean.get("center_id") != null ? (Integer)remitBean.get("center_id") : 0; BasicDynaBean
   * centerbean = new CenterMasterDAO().findByKey("center_id", remCenter); acountServRegNo =
   * centerbean != null ? (String)centerbean.get("hospital_center_service_reg_no") : null; }
   * 
   * if(transactionDate.trim().length() <= 10) transactionDate = transactionDate + " 00:00"; int
   * recordCount = hdr.getRecordCount();
   * 
   * if (senderId == null || senderId.equals("")) { errorMap.put("error",
   * "XML parsing failed: SenderID not found in Header..."); return false; }
   * 
   * if(!mod_accumed &&
   * !TpaMasterDAO.getTpaPayerID((String)remitBean.get("tpa_id")).get("payer_id").equals( senderId))
   * { errorMap.put("error",
   * "XML parsing failed: TPA selected not same as the remittance advice sender..."); return false;
   * }
   * 
   * if (receiverID == null || receiverID.equals("")) { errorMap.put("error",
   * "XML parsing failed: ReceiverID not found in Header..."); return false; }
   * 
   * if(acountServRegNo == null || !(acountServRegNo).equals(receiverID)) { if (remAccGrp != 0)
   * errorMap.put("error",
   * "XML parsing failed: Receiver ID not same as the account service registration no. ..."); else
   * errorMap.put("error",
   * "XML parsing failed: Receiver ID not same as the center service registration no. ..."); return
   * false; }
   * 
   * if (transactionDate == null || transactionDate.equals("")) { errorMap.put("error",
   * "XML parsing failed: TransactionDate not found in Header..."); return false; } else if
   * (dtFmt.parse(transactionDate) == null) { errorMap.put("error",
   * "XML parsing failed: TransactionDate is not a valid date..."); return false; }
   * 
   * if (recordCount < 0) { errorMap.put("error",
   * "XML parsing failed: RecordCount is not valid..."); return false; }
   * 
   * if (dispositionFlag == null || dispositionFlag.equals("")) { errorMap.put("error",
   * "XML parsing failed: DispositionFlag not found in Header..."); return false; }
   * 
   * if (!dispositionFlag.equalsIgnoreCase("PRODUCTION") &&
   * !dispositionFlag.equalsIgnoreCase("TEST")) { errorMap.put("error",
   * "XML parsing failed: DispositionFlag value is not valid..."); return false; }
   * 
   * return true; }
   */

  /**
   * The Class DHARemittanceXMLDigester.
   */
  public static class DHARemittanceXMLDigester extends Digester {

    /**
     * Instantiates a new DHA remittance XML digester.
     */
    public DHARemittanceXMLDigester() {
      super();
      setValidating(false);
      addObjectCreate("Remittance.Advice", "com.insta.hms.insurance.RemittanceAdvice");

      addObjectCreate("Remittance.Advice/Header", "com.insta.hms.insurance.RemittanceAdviceHeader");
      addBeanPropertySetter("Remittance.Advice/Header/SenderID", "senderID");
      addBeanPropertySetter("Remittance.Advice/Header/ReceiverID", "receiverID");
      addBeanPropertySetter("Remittance.Advice/Header/DispositionFlag", "dispositionFlag");
      addBeanPropertySetter("Remittance.Advice/Header/TransactionDate", "transactionDate");
      addBeanPropertySetter("Remittance.Advice/Header/RecordCount", "recordCount");
      addSetNext("Remittance.Advice/Header", "addHeader");

      addObjectCreate("Remittance.Advice/Claim", "com.insta.hms.insurance.RemittanceAdviceClaim");
      addBeanPropertySetter("Remittance.Advice/Claim/ID", "claimID");
      addBeanPropertySetter("Remittance.Advice/Claim/IDPayer", "idPayer");
      addBeanPropertySetter("Remittance.Advice/Claim/ProviderID", "providerID");
      addBeanPropertySetter("Remittance.Advice/Claim/DenialCode", "denialCode");
      addBeanPropertySetter("Remittance.Advice/Claim/PaymentReference", "paymentReference");
      addBeanPropertySetter("Remittance.Advice/Claim/DateSettlement", "dateSettlement");

      addObjectCreate("Remittance.Advice/Claim/Encounter",
          "com.insta.hms.insurance.RemittanceAdviceEncounter");
      addBeanPropertySetter("Remittance.Advice/Claim/Encounter/FacilityID", "facilityID");
      addSetNext("Remittance.Advice/Claim/Encounter", "addEncounter");

      addObjectCreate("Remittance.Advice/Claim/Activity",
          "com.insta.hms.insurance.RemittanceAdviceActivity");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/ID", "activityID");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Start", "start");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Type", "type");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Code", "code");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Quantity", "quantity");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Net", "net");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/List", "list");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Clinician", "clinician");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/PriorAuthorizationID",
          "priorAuthorizationID");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Gross", "gross");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/PatientShare", "patientShare");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/PaymentAmount", "paymentAmount");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/DenialCode", "activityDenialCode");

      addSetNext("Remittance.Advice/Claim/Activity", "addActivity");
      addSetNext("Remittance.Advice/Claim", "addClaim");
    }
  }

  /**
   * The Class HAADRemittanceXMLDigester.
   */
  public static class HAADRemittanceXMLDigester extends Digester {

    /**
     * Instantiates a new HAAD remittance XML digester.
     */
    public HAADRemittanceXMLDigester() {
      super();
      setValidating(false);
      addObjectCreate("Remittance.Advice", "com.insta.hms.insurance.RemittanceAdvice");

      addObjectCreate("Remittance.Advice/Header", "com.insta.hms.insurance.RemittanceAdviceHeader");
      addBeanPropertySetter("Remittance.Advice/Header/SenderID", "senderID");
      addBeanPropertySetter("Remittance.Advice/Header/ReceiverID", "receiverID");
      addBeanPropertySetter("Remittance.Advice/Header/DispositionFlag", "dispositionFlag");
      addBeanPropertySetter("Remittance.Advice/Header/TransactionDate", "transactionDate");
      addBeanPropertySetter("Remittance.Advice/Header/RecordCount", "recordCount");
      addSetNext("Remittance.Advice/Header", "addHeader");

      addObjectCreate("Remittance.Advice/Claim", "com.insta.hms.insurance.RemittanceAdviceClaim");
      addBeanPropertySetter("Remittance.Advice/Claim/ID", "claimID");
      addBeanPropertySetter("Remittance.Advice/Claim/IDPayer", "idPayer");
      addBeanPropertySetter("Remittance.Advice/Claim/ProviderID", "providerID");
      addBeanPropertySetter("Remittance.Advice/Claim/DenialCode", "denialCode");
      addBeanPropertySetter("Remittance.Advice/Claim/PaymentReference", "paymentReference");
      addBeanPropertySetter("Remittance.Advice/Claim/DateSettlement", "dateSettlement");

      addObjectCreate("Remittance.Advice/Claim/Encounter",
          "com.insta.hms.insurance.RemittanceAdviceEncounter");
      addBeanPropertySetter("Remittance.Advice/Claim/Encounter/FacilityID", "facilityID");
      addSetNext("Remittance.Advice/Claim/Encounter", "addEncounter");

      addObjectCreate("Remittance.Advice/Claim/Activity",
          "com.insta.hms.insurance.RemittanceAdviceActivity");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/ID", "activityID");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Start", "start");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Type", "type");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Code", "code");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Quantity", "quantity");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Net", "net");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/List", "list");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/OrderingClinician", "clinician");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/PriorAuthorizationID",
          "priorAuthorizationID");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/Gross", "gross");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/PatientShare", "patientShare");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/PaymentAmount", "paymentAmount");
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/DenialCode", "activityDenialCode");

      addSetNext("Remittance.Advice/Claim/Activity", "addActivity");
      addSetNext("Remittance.Advice/Claim", "addClaim");
    }
  }

}
