package com.insta.hms.integration.insurance.remittance;

import com.insta.hms.insurance.RemittanceAdvice;
import com.insta.hms.insurance.RemittanceAdviceHeader;
import com.insta.hms.mdm.accounting.AccountingGroupService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.tpas.TpaService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.expression.ParseException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class RemittanceValidation.
 */
public class RemittanceValidation {

  /**
   * Validate XML at claim and activity level.
   *
   * @param remitId the remit id
   * @param irdRepository the ird repository
   * @param iradRepository the irad repository
   * @return true, if successfully validated, ie no errors
   */
  public static boolean validateXML(Integer remitId,
      InsuranceRemittanceDetailsRepository irdRepository,
      InsuranceRemittanceActivityDetailsRepository iradRepository) {

    Integer errorCount = 0;
    // validate claims for remittance
    errorCount += irdRepository.validateClaims(remitId);
    // validate activities for remittance
    errorCount += iradRepository.validateActivities(remitId);

    if (errorCount > 0) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Validate header of XML.
   *
   * @param desc the desc
   * @param remitBean the remit bean
   * @param centerService the center service
   * @param agService the accountgroup service
   * @param tpaService the tpa service
   * @param centerId the center id
   * @return the string
   * @throws ParseException the parse exception
   */
  public static String validateHeaderXML(RemittanceAdvice desc, BasicDynaBean remitBean,
      CenterService centerService, AccountingGroupService agService, TpaService tpaService,
      Integer centerId) throws java.text.ParseException {
    // begin header validation
    String errorMsg = "";
    SimpleDateFormat dtFmt = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    String acountServRegNo = null;
    int remAccGrp = 0;

    Map keyMap = new HashMap<String, Integer>();

    ArrayList hdrz = desc.getHeader();
    if (hdrz == null || hdrz.isEmpty()) {
      errorMsg = "XML parsing failed: Required Header element missing...";
      return errorMsg;
    } else {
      if (hdrz.size() > 1) {
        errorMsg = "XML parsing failed: More than 1 Header element found...";
        return errorMsg;
      } else {
        RemittanceAdviceHeader hdr = (RemittanceAdviceHeader) hdrz.get(0);
        
        
        
        String transactionDate = hdr.getTransactionDate();

        // if account group for claim is not 0 then get service reg no from account group
        // master else from hospital center service
        // this is based on a preference hosp accounting preference
        // (all_centers_same_comp_name)
        remAccGrp = remitBean.get("account_group") != null
            ? (Integer) remitBean.get("account_group") : 0;
        if (remAccGrp != 0) {
          keyMap.clear();
          keyMap.put("account_group_id", remAccGrp);
          BasicDynaBean accBean = agService.findByPk(keyMap);
          acountServRegNo =
              accBean != null ? (String) accBean.get("account_group_service_reg_no") : null;
        } else {
          BasicDynaBean centerBean = null;
          keyMap.clear();
          keyMap.put("center_id", centerId);
          centerBean = centerService.findByPk(keyMap);
          acountServRegNo = centerBean != null
              ? (String) centerBean.get("hospital_center_service_reg_no") : null;
        }

        if (transactionDate.trim().length() <= 10) {
          transactionDate = transactionDate + " 00:00";
        }
        String senderId = hdr.getSenderID();
        if (senderId == null || senderId.equals("")) {
          errorMsg = "XML parsing failed: SenderID not found in Header...";
          return errorMsg;
        }

        // check if sender id is the same as the tpa code provided in ha_tpa_code OR tpa name
        // provided is a valid sender id
        keyMap.clear();
        keyMap.put("center_id", centerId);
        BasicDynaBean centerBean = centerService.findByPk(keyMap);
        String healthAuthority = (String) centerBean.get("health_authority");
        String tpaId = (String) remitBean.get("tpa_id");
        keyMap.clear();
        keyMap.put("tpa_id", tpaId);
        keyMap.put("health_authority", healthAuthority);
        List<BasicDynaBean> tpaBeanList = tpaService.haTpaCodeListAllBy(keyMap);
        String tpaCode = null;
        if (!tpaBeanList.isEmpty()) {
          tpaCode = (String) (tpaBeanList.get(0)).get("tpa_code");
        }
        keyMap.clear();
        keyMap.put("tpa_id", tpaId);
        BasicDynaBean tpaBean = tpaService.findByPk(keyMap);
        String tpaName = null;
        if (null != tpaBean) {
          tpaName = (String) (tpaBean.get("tpa_name"));
        }
        // this checks if the tpa selected in manual remittance upload screen is same as the
        // one in the XML
        if ((!senderId.equals(tpaCode) && tpaBean != null
            && !(senderId.substring(1)).equals(tpaName))) {
          errorMsg =
              "XML parsing failed: TPA selected not same as the remittance advice sender...";
          return errorMsg;
        }
        String receiverID = hdr.getReceiverID();
        if (acountServRegNo == null || !(acountServRegNo).equals(receiverID)) {
          if (remAccGrp != 0) {
            errorMsg = "XML parsing failed: Receiver ID not same as the "
                + "account service registration no. ...";
            return errorMsg;
          } else {
            errorMsg = "XML parsing failed: Receiver ID not same as the "
                + "center service registration no. ...";
          }
          return errorMsg;
        }
        
        if (receiverID == null || receiverID.equals("")) {
          errorMsg = "XML parsing failed: ReceiverID not found in Header...";
          return errorMsg;
        }

        if (null == transactionDate || transactionDate.equals("")) {
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
    return errorMsg;


  }

  /**
   * Sets the account details to remittanceBean.
   *
   * @param desc the desc
   * @param centerOrAccGroup the center or acc group
   * @param remitBean the remit bean
   */
  public static void setAccountDetails(RemittanceAdvice desc, String centerOrAccGroup,
      BasicDynaBean remitBean) {
    int accGrpId = 0;
    int centerId = 0;
    if (null != centerOrAccGroup && !"".equals(centerOrAccGroup)) {
      if (centerOrAccGroup.startsWith("A")) {
        accGrpId = Integer.parseInt(centerOrAccGroup.substring(1, centerOrAccGroup.length()));
      } else if (centerOrAccGroup.startsWith("C")) {
        centerId = Integer.parseInt(centerOrAccGroup.substring(1, centerOrAccGroup.length()));
      }
    }
    remitBean.set("account_group", accGrpId);
    remitBean.set("center_id", centerId);
  }

  /**
   * Do warnings exist.
   *
   * @param remitId the remit id
   * @param irdRepository the ird repository
   * @param iradRepository the irad repository
   * @return true, if successful
   */
  public static boolean doWarningsExist(Integer remitId,
      InsuranceRemittanceDetailsRepository irdRepository,
      InsuranceRemittanceActivityDetailsRepository iradRepository) {
    return (irdRepository.doWarningsExist(remitId) || iradRepository.doWarningsExist(remitId));
  }

}
