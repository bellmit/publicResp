package com.insta.hms.core.clinical.order.master;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Timestamp;
import java.util.Comparator;

public class OrderBillSelectionComparator implements Comparator<BasicDynaBean> {

  private static final String IS_TPA = "is_tpa";
  private static final String BILL_TYPE = "bill_type";
  private static final String OPEN_DATE = "open_date";

  @Override
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

    if ((Boolean) o1.get(IS_TPA).equals((Boolean) o2.get(IS_TPA))) {
      if (o1.get(BILL_TYPE).equals(o2.get(BILL_TYPE))) {
        if (((Timestamp) o1.get(OPEN_DATE)).before((Timestamp) o2.get(OPEN_DATE))) {
          return -1;
        } else if (((Timestamp) o1.get(OPEN_DATE)).after((Timestamp) o2.get(OPEN_DATE))) {
          return 1;
        }
        return 0;
      }
      if (o1.get(BILL_TYPE).equals("C")) {
        return -1;
      }
      return 1;
    }

    if ((Boolean) o1.get(IS_TPA)) {
      return -1;
    }

    return 1;

  }
}
