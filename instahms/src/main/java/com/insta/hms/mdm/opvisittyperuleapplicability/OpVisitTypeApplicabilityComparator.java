package com.insta.hms.mdm.opvisittyperuleapplicability;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;


@Component
public class OpVisitTypeApplicabilityComparator implements Comparator<BasicDynaBean> {

  private static final String STR_NA = "#";
  private static final int NUM_ALL = -1;
  private static final String STR_ANY = "*";
  /**
   * Ordered list maintaining properties with priorities low to high.
   */
  private static final Set<String> priorityList = new LinkedHashSet<String>() {
    {
      add("doctor_id");
      add("dept_id");
      add("tpa_id");
      add("center_id");
    }
  };
  
  @Override
  public int compare(BasicDynaBean o1, BasicDynaBean o2) {
    Integer localPriority = 1;
    Integer o1Priority = 0;
    Integer o2Priority = 0;
    
    for (String property : priorityList) {
      o1Priority += o1.get(property).equals(STR_ANY) || o1.get(property).equals(NUM_ALL)
          ? 0 : (o1.get(property).equals(STR_NA) ? localPriority : localPriority * 2);
      o2Priority += o2.get(property).equals(STR_ANY) || o2.get(property).equals(NUM_ALL) 
          ? 0 : (o2.get(property).equals(STR_NA) ? localPriority : localPriority * 2);
      
      localPriority *= 10;
    }
    
    return o1Priority.compareTo(o2Priority);
  }
  
}
