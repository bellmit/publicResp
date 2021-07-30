package com.insta.instaapi.customer.diagnostics;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.SampleType.SampleTypeDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

//RC API : This class should implement an interface SampleIdGenerator

public class BatchSampleIdGenerator {

  /**
   * Generate batch sample ID.
   * @param con           database connection
   * @param testId        Test for which sample id is needed
   * @param isPackage     is part of package ?
   * @param sampleId      sample id
   * @param sampleTypeId  sample type id
   * @param batchBased    part of batch?
   * @param batchBasedNo  batch prefix
   * @return              Map containing Sample ID
   * @throws SQLException Query related exception
   */
  public static Map<String, String> generateBatchBasedSampleId(Connection con, String[] testId,
      String[] isPackage, String[] sampleId, String[] sampleTypeId, String[] batchBased,
      StringBuilder batchBasedNo) throws SQLException {

    BasicDynaBean testBean = null;
    GenericDAO diagDAO = new GenericDAO("diagnostics");
    boolean isNogenerated = false;
    String sampleID = null;

    Map<String, String> sampNotoTypemap = new HashMap<String, String>();

    for (int i = 0; i < testId.length; i++) {
      if (isPackage[i].equalsIgnoreCase("n")) {
        testBean = diagDAO.findByKey("test_id", testId[i]);
        if (testBean.get("sample_needed").equals("n")) {
          batchBased[i] = ""; // To avoid null pointer exception
          // we allow sample not needed tests for incoming sample reg.
          // Such tests doesn't need sample no.
          continue;
        }
        if (!isNogenerated) {
          sampleID = new SampleTypeDAO().getBatchBasedSampleNo(con);
          batchBasedNo.append(sampleID);
          isNogenerated = true;
        }
        sampleId[i] = sampleID;
        batchBased[i] = sampleTypeId[i] + "" + sampleId[i];
        sampNotoTypemap.put(batchBased[i], sampleTypeId[i]);
      } else {
        batchBased[i] = "";
      }
    }
    return sampNotoTypemap;
  }

}
