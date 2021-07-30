package com.insta.instaapi.customer.diagnostics;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.SampleType.SampleTypeDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class PrefixSampleIdGenerator {

  /**
   * Generate prefix based sample ID.
   * @param con               database connection
   * @param testId            Test for which sample id is needed
   * @param isPackage         is part of package ?
   * @param originalSampleNo  original sample number
   * @param sampleId          sample id
   * @param sampleTypeId      sample type id
   * @param centerId          center id
   * @return                  Map containing Sample ID 
   * @throws Exception        exception
   */
  public static Map<String, String> generatePrefixBasedSampleId(Connection con, String[] testId,
      String[] isPackage, String[] originalSampleNo, String[] sampleId, String[] sampleTypeId,
      Integer centerId) throws Exception {

    BasicDynaBean testBean = null;
    GenericDAO diagDAO = new GenericDAO("diagnostics");
    GenericDAO sampleCollectionDAO = new GenericDAO("sample_collection");

    Map<String, String> sampNotoTypemap = new HashMap<String, String>();

    for (int i = 0; i < testId.length; i++) {
      if (isPackage[i].equalsIgnoreCase("n")) {
        testBean = diagDAO.findByKey("test_id", testId[i]);
        if (testBean.get("sample_needed").equals("n")) {
          // we allow sample not needed tests for incoming sample reg.
          // Such tests doesn't need sample no.
          continue;
        }
        boolean isOriginalSampleNoExists = false;
        String existingSampleId = null;
        boolean check = (i == 0) || (i == 1 && isPackage[0].equalsIgnoreCase("y"));
        if (!check) {
          for (int j = 0; j < i; j++) {
            if (isPackage[j].equalsIgnoreCase("n")) {
              if (originalSampleNo[j].equals(originalSampleNo[i])
                  && sampleTypeId[j].equals(sampleTypeId[i])) {
                isOriginalSampleNoExists = true;
                existingSampleId = sampleId[j];
                break;
              }
            }
          }
        }
        String sampleTypeID = sampleTypeId[i];
        if (!isOriginalSampleNoExists && isPackage[i].equalsIgnoreCase("n")) {
          sampleId[i] = SampleTypeDAO.getNextSampleNumber(Integer.parseInt(sampleTypeID), centerId);
          BasicDynaBean existingSampleBean = sampleCollectionDAO.findByKey("sample_sno",
              sampleId[i]);
          if (existingSampleBean != null) {
            // TODO return errorResponse(request, response, "Duplicate Sample Id "+sampleId[i]);
            throw new Exception("Duplicate Sample Id " + sampleId[i]);
          }
          sampNotoTypemap.put(sampleId[i], sampleTypeID);
        } else {
          sampleId[i] = existingSampleId;
        }
      }
    }
    return sampNotoTypemap;
  }

}
