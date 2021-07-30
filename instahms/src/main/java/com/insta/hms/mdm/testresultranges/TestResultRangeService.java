package com.insta.hms.mdm.testresultranges;

import com.insta.hms.diagnosticsmasters.Result;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 
 * @author anil.n
 *
 */
@Service
public class TestResultRangeService extends MasterService {

  public TestResultRangeService(TestResultRangeRepository trr, TestResultRangeValidator trv) {
    super(trr, trv);
  }

  /**
   * Delete result ranges.
   *
   * @param deletedResultsRanges the deleted results ranges
   * @return true, if successful
   */
  public boolean deleteResultRanges(ArrayList<Result> deletedResultsRanges) {

    boolean success = true;

    for (Result modifedResultRange : deletedResultsRanges) {
      BasicDynaBean bean = getRepository().findByKey("resultlabel_id",
          new Integer(modifedResultRange.getResultlabel_id()));
      if (bean != null) {
        success &= getRepository().delete("resultlabel_id",
            new Integer(modifedResultRange.getResultlabel_id())) > 0;
      }
    }
    return success;
  }
}
