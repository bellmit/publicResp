package com.insta.hms.mdm.icdcodes;

import com.google.common.base.Joiner;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class IcdCodesRepository.
 */
@Repository
public class IcdCodesRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new ICD codes repository.
   */
  public IcdCodesRepository() {
    super(new String[] {"code", "code_type"}, null, "mrd_codes_master", "mrd_code_id");
  }
  
  private static final String SEARCH_PATIENT_PROBLEM_QUERY = "SELECT"
      + " mrd_code_id AS patient_problem_id, code_type, code AS patient_problem_code,"
      + " code_desc AS patient_problem_desc"
      + " FROM mrd_codes_master"
      + " WHERE status=?";
  
  /**
   * Get Patient Problem List.
   * 
   * @param searchInput the search input
   * @param codeTypeLst the ICD codes and should be same as diagnosisCode
   * @return list of beans
   */
  public List<BasicDynaBean> getPatientProblemList(String searchInput, List<String> codeTypeLst) {
    StringBuilder query = new StringBuilder(SEARCH_PATIENT_PROBLEM_QUERY);
    String[] searchWord = searchInput.split(" ");
    Object[] parameters = new Object[(searchWord.length * 2) + codeTypeLst.size() + 1];
    int inc = 0;
    parameters[inc++] = 'A';
    
    if (!codeTypeLst.isEmpty()) {
      List<String> paramHolders = new ArrayList<>();
      for (String s : codeTypeLst) {
        paramHolders.add("?");
        parameters[inc++] = s;
      }
      query.append(" AND code_type IN (" + Joiner.on(",").join(paramHolders) + ")");
    }
    for (int j = 0; j < searchWord.length; j++) {
      query.append(" AND (code ILIKE ? OR code_desc ILIKE ?)");
      parameters[inc++] = "%" + searchWord[j] + "%";
      parameters[inc++] = "%" + searchWord[j] + "%";
    }
    query.append(" LIMIT 100");
    return DatabaseHelper.queryToDynaList(query.toString(), parameters);
  }
}
