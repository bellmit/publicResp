package com.insta.hms.mdm.icdsupportedcodes;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class IcdSupportedCodeTypesRepository.
 */
@Repository
public class IcdSupportedCodeTypesRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new icd supported code types repository.
   */
  public IcdSupportedCodeTypesRepository() {
    super("mrd_supported_code_types", "code_type", "code_type");
  }

  /** The Constant SEARCH_CODES_OF_TYPE_QUERY. */
  private static final String SEARCH_CODES_OF_TYPE_QUERY =
      "SELECT code, code ||' '||COALESCE(code_desc,'') AS icd,"
          + " COALESCE(code_desc,'') AS code_desc, code_type, "
          + " is_year_of_onset_mandatory, fn.mrd_code_id "
          + "FROM getItemCodesForCodeType(?, ?) fn "
          + "LEFT JOIN mrd_codes_details mcd on(mcd.mrd_code_id = fn.mrd_code_id::integer) "
          + "WHERE status = 'A' AND code_type = ? ";

  /**
   * Gets the diag code of code type list.
   *
   * @param searchInput the search input
   * @param codeType the code type
   * @return the diag code of code type list
   */
  public List<BasicDynaBean> getDiagCodeOfCodeTypeList(String searchInput, String codeType) {
    int inc = 0;
    String[] searchWord = searchInput.split(" ");
    Object[] parameters = new Object[(searchWord.length * 4) + 3];
    parameters[inc++] = codeType;
    parameters[inc++] = "";
    parameters[inc++] = codeType;
    StringBuilder query = new StringBuilder(SEARCH_CODES_OF_TYPE_QUERY);
    for (int j = 0; j < searchWord.length; j++) {
      query.append(
          " AND (code ILIKE ? OR code_desc ILIKE ? OR code_desc ILIKE ? OR code_desc ILIKE ?)");
      parameters[inc++] = searchWord[j] + "%";
      parameters[inc++] = searchWord[j] + "%";
      parameters[inc++] = "%" + searchWord[j];
      parameters[inc++] = "%" + searchWord[j] + "%";
    }
    query.append(" LIMIT 100");
    return DatabaseHelper.queryToDynaList(query.toString(), parameters);
  }
}
