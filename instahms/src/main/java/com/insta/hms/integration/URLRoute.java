package com.insta.hms.integration;

/**
 * The Class URLRoute.
 */
public class URLRoute {

  /** Remittance Controller. */
  public static final String REMITTANCE_UPLOAD = "/Insurance/RemittanceUpload";
  
  /** The Constant REMITTANCE_UPLOAD_PAGE. */
  public static final String REMITTANCE_UPLOAD_PAGE = "/pages/Insurance/remittanceadvices/add";
  
  /** The Constant REMITTANCE_UPLOAD_PAGE_LIST. */
  public static final String REMITTANCE_UPLOAD_PAGE_LIST =
      "/pages/Insurance/remittanceadvices/list";
  
  /** The Constant REMITTANCE_DOWNLOAD_PAGE_LIST. */
  public static final String REMITTANCE_DOWNLOAD_PAGE_LIST =
      "/pages/Insurance/remittanceadvices/radownloadlist";
  
  /** The Constant REMITTANCE_DOWNLOAD_REDIRECT_TO_LIST. */
  public static final String REMITTANCE_DOWNLOAD_REDIRECT_TO_LIST =
      "redirect:remittanceDownloadList";
  
  /** Practo Integration. */
  public static final String BOOK_INTEGRATION_PATH = "/integrations/book";

  /** Redirections Controller. */
  public static final String REDIRECTIONS_INTEGRATION = "/integrations/redirect";

  /** Insurance Aggregators. */
  public static final String AGGREGATORS = "/insuranceagg";
  
  /** The Constant VIEW_INDEX_URL. */
  public static final String VIEW_INDEX_URL = "/index";

  /** Pine Labs Txn Response. */
  public static final String PINE_LABS = "/pinelabs";

  /** Pine Labs Txn Response. */
  public static final String OHSRS_DOHGOVPH_URL = "/reports/ohsrsdohgovph";

  /** Self Pay Claims Submissions. */
  public static final String SELF_PAY_CLAIM_SUBMISSION = "/billing/selfpay";
  
  /** The Constant SELF_PAY_CLAIM_SUBMISSION_LIST. */
  public static final String SELF_PAY_CLAIM_SUBMISSION_LIST = "/pages/billing/selfpay/list";
  
  /** The Constant SELF_PAY_CLAIM_SUBMISSION_CREATE. */
  public static final String SELF_PAY_CLAIM_SUBMISSION_CREATE = "/pages/billing/selfpay/create";
}

