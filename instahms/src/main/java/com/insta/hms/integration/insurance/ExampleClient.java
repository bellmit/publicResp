package com.insta.hms.integration.insurance;

/**
 * The Class ExampleClient.
 */
public class ExampleClient {

  /**
   * Go.
   */
  private void go() {

    InsurancePluginManager manager = new InsurancePluginManager();
    InsuranceCaseDetails caseDetails = new InsuranceCaseDetails();

    caseDetails.setPayer("");
    caseDetails.setTPA("");
    caseDetails.setProvider("");
    caseDetails.setHealthAuthority("");

    InsurancePlugin plugin = manager.getPlugin(caseDetails);

    if (null != plugin) {

      // Get patient details given minimal patient identity
      PatientIdentity pi = new PatientIdentity();
      pi.setPatientName("");
      pi.setPatientPhoneNumber("");
      MembershipDetails patientDetails = plugin.getMembershipDetails(pi);

      // Get Eligibility given patient details
      MemberEligibility eligibility = plugin.getEligibility(patientDetails);

      // create service details for all items to be authorized
      PriorAuthDocument priorAuthDocument = getApprovalXML();
      // PriorAuthorization[] authorizations = plugin.getPriorAuthorization(patientDetails,
      // priorAuthDocument);

      ClaimDocument claimDocument = generateXMLClaim();
      // ClaimSubmissionResult result = plugin.submitClaim(claimDocument);

    }

  }

  /**
   * Generate XML claim.
   *
   * @return the claim document
   */
  private ClaimDocument generateXMLClaim() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Gets the approval XML.
   *
   * @return the approval XML
   */
  private PriorAuthDocument getApprovalXML() {
    // TODO Auto-generated method stub
    return null;
  }
}
