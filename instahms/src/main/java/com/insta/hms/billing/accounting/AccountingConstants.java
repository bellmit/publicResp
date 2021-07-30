package com.insta.hms.billing.accounting;

import java.util.Arrays;
import java.util.List;

/**
 * The Class AccountingConstants.
 */
public final class AccountingConstants {


  /** The Constant TRANSACTION_TYPE_N. */
  public static final Character TRANSACTION_TYPE_N = 'N';

  /** The Constant TRANSACTION_TYPE_R. */
  public static final Character TRANSACTION_TYPE_R = 'R';

  /** The Constant IS_TPA_Y. */
  public static final Character IS_TPA_Y = 'Y';

  /** The Constant IS_TPA_N. */
  public static final Character IS_TPA_N = 'N';

  /** The Constant PHARMACY_CHARGEHEAD_PHMED. */
  public static final String PHARMACY_CHARGEHEAD_PHMED = "PHMED";

  /** The Constant PHARMACY_CHARGEHEAD_PHRET. */
  public static final String PHARMACY_CHARGEHEAD_PHRET = "PHRET";

  /** The Constant PHARMACY_CHARGEHEAD_PHCMED. */
  public static final String PHARMACY_CHARGEHEAD_PHCMED = "PHCMED";

  /** The Constant PHARMACY_CHARGEHEAD_PHCRET. */
  public static final String PHARMACY_CHARGEHEAD_PHCRET = "PHCRET";

  /** The Constant PHARMACY_CHARGEHEAD_HOSPITAL_OR_ISSUE_ITEM. */
  public static final String PHARMACY_CHARGEHEAD_HOSPITAL_OR_ISSUE_ITEM = "HOSPITAL_OR_ISSUE_ITEM";

  /** The Constant SALE_TYPE_R. */
  public static final String SALE_TYPE_R = "R";

  /** The Constant SALE_TYPE_S. */
  public static final String SALE_TYPE_S = "S";
  
  /** The Constant BILL_STATUS_CANCELLED. */
  public static final String BILL_STATUS_CANCELLED = "X";
  
  /** Valid bill states for accounting. */
  public static final List<String> VALID_BILL_STATUSES = Arrays.asList("F", "C", "X");
  
}
