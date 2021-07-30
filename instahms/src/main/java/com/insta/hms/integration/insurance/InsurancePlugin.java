package com.insta.hms.integration.insurance;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Interface InsurancePlugin.
 */
public interface InsurancePlugin {

  /**
   * Gets the matcher.
   *
   * @return the matcher
   */
  PluginMatcher getMatcher();

  /**
   * Gets the claim context.
   *
   * @return the claim context
   */
  ClaimContext getClaimContext();

  /**
   * Gets the configuration.
   *
   * @return the configuration
   */
  HashMap<String, String> getConfiguration();

  /**
   * Sets the configuration.
   *
   * @param configMap the config map
   */
  void setConfiguration(HashMap<String, String> configMap);

  /**
   * Gets the membership details.
   *
   * @param pi the pi
   * @return the membership details
   */
  MembershipDetails getMembershipDetails(PatientIdentity pi);

  /**
   * Gets the eligibility.
   *
   * @param memberDetails the member details
   * @return the eligibility
   */
  MemberEligibility getEligibility(MembershipDetails memberDetails);

  /**
   * Send prior auth request.
   *
   * @param priorAuthDocument the prior auth document
   * @return the prior auth request results
   * @throws ConnectException the connect exception
   */
  PriorAuthRequestResults sendPriorAuthRequest(PriorAuthDocument priorAuthDocument)
      throws ConnectException;

  /**
   * Prior auth approval file list.
   *
   * @return the prior auth request results
   * @throws ConnectException the connect exception
   */
  PriorAuthRequestResults priorAuthApprovalFileList() throws ConnectException;

  /**
   * Prior auth approval file.
   *
   * @param fileId the file id
   * @param markAsDownloaded the mark as downloaded
   * @return the prior auth request results
   * @throws ConnectException the connect exception
   */
  PriorAuthRequestResults priorAuthApprovalFile(String fileId, boolean markAsDownloaded)
      throws ConnectException;

  /**
   * Submit claim.
   *
   * @param claimDocument the claim document
   * @param context the context
   * @return the claim submission result
   * @throws ConnectException the connect exception
   */
  ClaimSubmissionResult submitClaim(ClaimDocument claimDocument, ClaimContext context)
      throws ConnectException;

  /**
   * Gets the remittance.
   *
   * @param payerInfo the payer info
   * @param filter the filter
   * @param reference the reference
   * @param context the context
   * @param listOnly the list only
   * @return the remittance
   * @throws ConnectException the connect exception
   */
  ClaimRemittance getRemittance(PayerDetails payerInfo, RemittanceFilter filter,
      ClaimReference reference, ClaimContext context, boolean listOnly)
      throws ConnectException;

  /**
   * Download file.
   *
   * @param filter the filter
   * @param context the context
   * @param markAsDownloaded the mark as downloaded
   * @return the claim remittance
   * @throws ConnectException the connect exception
   */
  ClaimRemittance downloadFile(RemittanceFilter filter, ClaimContext context,
      boolean markAsDownloaded) throws ConnectException;

  /**
   * Gets the webservices host.
   *
   * @return the webservices host
   */
  public String getWebservicesHost();
  
  /**
   * Get Prior auth approval file.
   *
   * @param fileId the file id
   * @return the prior auth request results
   * @throws ConnectException the connect exception
   */
  PriorAuthRequestResults getPriorAuthApprovalFile(String fileId)
      throws ConnectException;
}
