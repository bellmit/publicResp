/**
 * 
 */

package com.insta.hms.mdm.diagnosiscodefavourites;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class DiagnosisCodeFavouritesRepository.
 *
 * @author anup vishwas
 */

@Repository
public class DiagnosisCodeFavouritesRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new diagnosis code favourites repository.
   */
  public DiagnosisCodeFavouritesRepository() {
    super(new String[] { "code_type", "code", "doctor_id" }, null, "mrd_codes_doctor_master",
        "code_doc_id");
  }

  /** The Constant GET_DIAG_CODE_FAVOURITE. */
  private static final String GET_DIAG_CODE_FAVOURITE = " SELECT mcm.code, mcm.code ||' "
      + " '||COALESCE(code_desc,'') AS icd,  "
      + " COALESCE(code_desc,'') AS code_desc, mcdm.code_type,"
      + " mcd.is_year_of_onset_mandatory "
      + " FROM mrd_codes_doctor_master mcdm "
      + "   JOIN mrd_codes_master mcm ON (mcdm.code=mcm.code AND mcdm.code_type=mcm.code_type) "
      + "   LEFT JOIN mrd_codes_details mcd on(mcd.mrd_code_id = mcm.mrd_code_id) "
      + " WHERE mcdm.code_type=? AND doctor_id=? ";

  /**
   * Gets the diag code fav of code type list.
   *
   * @param searchInput
   *          the search input
   * @param doctorId
   *          the doctor id
   * @param codeType
   *          the code type
   * @return the diag code fav of code type list
   */
  public List<BasicDynaBean> getDiagCodeFavOfCodeTypeList(String searchInput, String doctorId,
      String codeType) {
    int position = 0;
    String[] searchWord = searchInput.split(" ");
    Object[] parameters = new Object[(searchWord.length * 4) + 2];
    parameters[position++] = codeType;
    parameters[position++] = doctorId;
    StringBuilder query = new StringBuilder(GET_DIAG_CODE_FAVOURITE);
    for (int j = 0; j < searchWord.length; j++) {
      query.append(
          " AND (mcm.code ILIKE ? OR code_desc ILIKE ? OR code_desc ILIKE ? OR code_desc ILIKE ?)");
      parameters[position++] = searchWord[j] + "%";
      parameters[position++] = searchWord[j] + "%";
      parameters[position++] = "%" + searchWord[j];
      parameters[position++] = "%" + searchWord[j] + "%";
    }
    query.append(" LIMIT 100");
    return DatabaseHelper.queryToDynaList(query.toString(), parameters);
  }

}
