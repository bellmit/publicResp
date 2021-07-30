package com.insta.hms.integration.insurance.remittance;

import com.insta.hms.mdm.centers.CenterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.digester.Digester;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class XMLRemittanceDigester.
 */
public class XMLRemittanceDigester {

  /** The digester map. */
  static Map<String, Digester> digesterMap = new HashMap<String, Digester>();
  
  static {
    digesterMap.put("DHA", new XMLRemittanceDigester.DHARemittanceXMLDigester());
    digesterMap.put("HAAD", new XMLRemittanceDigester.HAADRemittanceXMLDigester());
  }

  /**
   * Gets the digester.
   *
   * @param centerId the center id
   * @param centerService the center service
   * @return the digester
   */
  public Digester getDigester(Integer centerId, CenterService centerService) {
    // check for center id and corresponding health authority.
    // set the digester based on health authority
    Map<String, Integer> keyMap = new HashMap<String, Integer>();
    keyMap.put("center_id", centerId);
    BasicDynaBean centerBean = centerService.findByPk(keyMap);
    if (centerBean == null) {
      return null;
    }
    return digesterMap.get(centerBean.get("health_authority"));
  }

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

      addObjectCreate("Remittance.Advice/Header",
          "com.insta.hms.insurance.RemittanceAdviceHeader");
      addBeanPropertySetter("Remittance.Advice/Header/SenderID", "senderID");
      addBeanPropertySetter("Remittance.Advice/Header/ReceiverID", "receiverID");
      addBeanPropertySetter("Remittance.Advice/Header/DispositionFlag", "dispositionFlag");
      addBeanPropertySetter("Remittance.Advice/Header/TransactionDate", "transactionDate");
      addBeanPropertySetter("Remittance.Advice/Header/RecordCount", "recordCount");
      addSetNext("Remittance.Advice/Header", "addHeader");

      addObjectCreate("Remittance.Advice/Claim",
          "com.insta.hms.insurance.RemittanceAdviceClaim");
      addBeanPropertySetter("Remittance.Advice/Claim/ID", "claimID");
      addBeanPropertySetter("Remittance.Advice/Claim/IDPayer", "idPayer");
      addBeanPropertySetter("Remittance.Advice/Claim/ProviderID", "providerID");
      addBeanPropertySetter("Remittance.Advice/Claim/DenialCode", "denialCode");
      addBeanPropertySetter("Remittance.Advice/Claim/PaymentReference", "paymentReference");
      addBeanPropertySetter("Remittance.Advice/Claim/DateSettlement", "dateSettlement");

      addObjectCreate("Remittance.Advice/Claim/Resubmission",
          "com.insta.hms.insurance.RemittanceAdviceResubmission");
      addBeanPropertySetter("Remittance.Advice/Claim/Resubmission/Type", "type");
      addBeanPropertySetter("Remittance.Advice/Claim/Resubmission/Comment", "comment");
      addSetNext("Remittance.Advice/Claim/Resubmission", "addResubmission");

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
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/DenialCode",
          "activityDenialCode");

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

      addObjectCreate("Remittance.Advice/Header",
          "com.insta.hms.insurance.RemittanceAdviceHeader");
      addBeanPropertySetter("Remittance.Advice/Header/SenderID", "senderID");
      addBeanPropertySetter("Remittance.Advice/Header/ReceiverID", "receiverID");
      addBeanPropertySetter("Remittance.Advice/Header/DispositionFlag", "dispositionFlag");
      addBeanPropertySetter("Remittance.Advice/Header/TransactionDate", "transactionDate");
      addBeanPropertySetter("Remittance.Advice/Header/RecordCount", "recordCount");
      addSetNext("Remittance.Advice/Header", "addHeader");

      addObjectCreate("Remittance.Advice/Claim",
          "com.insta.hms.insurance.RemittanceAdviceClaim");
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
      addBeanPropertySetter("Remittance.Advice/Claim/Activity/DenialCode",
          "activityDenialCode");

      addObjectCreate("Remittance.Advice/Claim/Resubmission",
          "com.insta.hms.insurance.RemittanceAdviceResubmission");
      addBeanPropertySetter("Remittance.Advice/Claim/Resubmission/Type", "type");
      addBeanPropertySetter("Remittance.Advice/Claim/Resubmission/Comment", "comment");
      addSetNext("Remittance.Advice/Claim/Resubmission", "addResubmission");

      addSetNext("Remittance.Advice/Claim/Activity", "addActivity");
      addSetNext("Remittance.Advice/Claim", "addClaim");
    }
  }

}
