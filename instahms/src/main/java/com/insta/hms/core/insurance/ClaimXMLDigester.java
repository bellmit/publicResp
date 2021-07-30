package com.insta.hms.core.insurance;

import org.apache.commons.digester.Digester;

/**
 * The Class ClaimXMLDigester.
 */
public class ClaimXMLDigester extends Digester {

  /**
   * Instantiates a new claim XML digester.
   */
  public ClaimXMLDigester() {
    super();
    setValidating(false);

    addObjectCreate("Claim.Submission", "com.insta.hms.core.insurance.ClaimSubmission");

    addObjectCreate("Claim.Submission/Header",
        "com.insta.hms.core.insurance.ClaimSubmissionHeader");
    addBeanPropertySetter("Claim.Submission/Header/SenderID", "senderID");
    addBeanPropertySetter("Claim.Submission/Header/ReceiverID", "receiverID");
    addBeanPropertySetter("Claim.Submission/Header/DispositionFlag", "dispositionFlag");
    addBeanPropertySetter("Claim.Submission/Header/TransactionDate", "transactionDate");
    addBeanPropertySetter("Claim.Submission/Header/RecordCount", "recordCount");
    addSetNext("Claim.Submission/Header", "addHeader");

    addObjectCreate("Claim.Submission/Claim", "com.insta.hms.core.insurance.ClaimSubmissionClaim");
    addBeanPropertySetter("Claim.Submission/Claim/ID", "claimID");
    addBeanPropertySetter("Claim.Submission/Claim/IDPayer", "idPayer");
    addBeanPropertySetter("Claim.Submission/Claim/ProviderID", "providerID");

    addBeanPropertySetter("Claim.Submission/Claim/Gross", "gross");
    addBeanPropertySetter("Claim.Submission/Claim/Net", "net");
    addBeanPropertySetter("Claim.Submission/Claim/VAT", "vat");
    addBeanPropertySetter("Claim.Submission/Claim/PatientShare", "patientShare");
    addBeanPropertySetter("Claim.Submission/Claim/MemberID", "memberId");
    addSetNext("Claim.Submission/Claim", "addClaim");

    addObjectCreate("Claim.Submission/Claim/Resubmission",
        "com.insta.hms.core.insurance.ClaimSubmissionResubmission");
    addBeanPropertySetter("Claim.Submission/Claim/Resubmission/Type", "type");
    addBeanPropertySetter("Claim.Submission/Claim/Resubmission/Comment", "comment");
    addSetNext("Claim.Submission/Claim/Resubmission", "addResubmission");

    addObjectCreate("Claim.Submission/Claim/Activity",
        "com.insta.hms.core.insurance.ClaimSubmissionActivity");
    addBeanPropertySetter("Claim.Submission/Claim/Activity/ID", "activityID");
    addBeanPropertySetter("Claim.Submission/Claim/Activity/Start", "start");
    addBeanPropertySetter("Claim.Submission/Claim/Activity/Type", "type");
    addBeanPropertySetter("Claim.Submission/Claim/Activity/Code", "code");
    addBeanPropertySetter("Claim.Submission/Claim/Activity/Quantity", "quantity");
    addBeanPropertySetter("Claim.Submission/Claim/Activity/Net", "net");
    addBeanPropertySetter("Claim.Submission/Claim/Activity/VAT", "vat");
    addBeanPropertySetter("Claim.Submission/Claim/Activity/VATPercent", "vatPercent");
    addBeanPropertySetter("Claim.Submission/Claim/Activity/Clinician", "clinician");
    addBeanPropertySetter("Claim.Submission/Claim/Activity/OrderingClinician", "orderingClinician");
    addSetNext("Claim.Submission/Claim/Activity", "addActivity");

    addObjectCreate("Claim.Import", "com.insta.hms.core.insurance.ClaimSubmission");
    addObjectCreate("Claim.Import/Header", "com.insta.hms.core.insurance.ClaimSubmissionHeader");
    addBeanPropertySetter("Claim.Import/Header/TransactionDate", "transactionDate");
    addBeanPropertySetter("Claim.Import/Header/ProviderID", "providerID");
    addSetNext("Claim.Import/Header", "addHeader");

    addObjectCreate("Claim.Import/Claim", "com.insta.hms.core.insurance.ClaimSubmissionClaim");
    addBeanPropertySetter("Claim.Import/Claim/ID", "claimID");
    addBeanPropertySetter("Claim.Import/Claim/ReceiverID", "receiverID");
    addBeanPropertySetter("Claim.Import/Claim/PayerID", "payerID");
    addBeanPropertySetter("Claim.Import/Claim/Gross", "gross");
    addBeanPropertySetter("Claim.Import/Claim/Net", "net");
    addBeanPropertySetter("Claim.Import/Claim/VAT", "vat");
    addBeanPropertySetter("Claim.Import/Claim/PatientShare", "patientShare");
    addBeanPropertySetter("Claim.Import/Claim/Patient/MemberID", "memberId");
    addSetNext("Claim.Import/Claim", "addClaim");

    addObjectCreate("Claim.Import/Claim/Activity",
        "com.insta.hms.core.insurance.ClaimSubmissionActivity");
    addBeanPropertySetter("Claim.Import/Claim/Activity/ID", "activityID");
    addBeanPropertySetter("Claim.Import/Claim/Activity/Start", "start");
    addBeanPropertySetter("Claim.Import/Claim/Activity/Type", "type");
    addBeanPropertySetter("Claim.Import/Claim/Activity/Code", "code");
    addBeanPropertySetter("Claim.Import/Claim/Activity/Quantity", "quantity");
    addBeanPropertySetter("Claim.Import/Claim/Activity/Net", "net");
    addBeanPropertySetter("Claim.Import/Claim/Activity/VAT", "vat");
    addBeanPropertySetter("Claim.Import/Claim/Activity/VATPercent", "vatPercent");
    addBeanPropertySetter("Claim.Import/Claim/Activity/ClinicianName", "clinician");
    addBeanPropertySetter("Claim.Import/Claim/Activity/OrderingClinician", "orderingClinician");
    addBeanPropertySetter("Claim.Import/Claim/Activity/List", "list");
    addSetNext("Claim.Import/Claim/Activity", "addActivity");

  }

}
