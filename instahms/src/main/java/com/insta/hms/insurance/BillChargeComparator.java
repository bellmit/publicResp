package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.Comparator;

public class BillChargeComparator implements Comparator<BasicDynaBean> {

  // RC Anupama : NUll comparison is incorrect. First check for both null and return 0
  /*
   * (non-Javadoc)
   * 
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  // If only one item is null, should return 1 if o1 is not null and -1 if o1 is null
  /**
   * Compare.
   *
   * @param o1 the o 1
   * @param o2 the o 2
   * @return the int
   */
  public int compare(BasicDynaBean o1, BasicDynaBean o2) {
    if (o1 == null && o2 == null) {
      return 0;
    }
    if (o1 == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }

    BigDecimal o1AmtIncluded = null != o1.get("amount_included")
        ? (BigDecimal) o1.get("amount_included")
        : BigDecimal.ZERO;
    BigDecimal o2AmtIncluded = null != o2.get("amount_included")
        ? (BigDecimal) o2.get("amount_included")
        : BigDecimal.ZERO;

    if (o1AmtIncluded.compareTo(o2AmtIncluded) == 0) {
      if (o1.get("charge_id") == null && o2.get("charge_id") == null) {
        return 0;
      }

      if (o1.get("charge_id") == null) {
        return -1;
      }
      if (o2.get("charge_id") == null) {
        return 1;
      }

      String o1ChargeID = (String) o1.get("charge_id");
      String o2ChargeID = (String) o2.get("charge_id");

      return o1ChargeID.compareTo(o2ChargeID);
    } else {
      return o2AmtIncluded.compareTo(o1AmtIncluded);
    }
  }

}
