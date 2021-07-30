package com.insta.hms.billing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BillConstants {

  public class Restrictions {
    public static final String BILL_NO = "bill_no";
    public static final String DEPOSIT_TYPE = "deposit_type";
    public static final String VISIT_TYPE = "visit_type";
    public static final String BILL_TYPE = "bill_type";
    public static final String PAT_PACKAGE_ID = "pat_package_id";
    public static final String PACKAGE_ID = "package_id";
  }

  private static final Integer[] depositPaymentModes = new Integer[] { -6, -7, -8 };

  public static final Set<Integer> depositSetoffPaymentModes = new HashSet<>(
      Arrays.asList(depositPaymentModes));
  
  public static final Set<String> pharmacyChargeHeads = new HashSet<>(
      Arrays.asList("PHMED", "PHCMED"));

}