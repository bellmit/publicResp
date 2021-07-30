package com.insta.hms.mdm.phrasesuggestions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/** @author Sonam.
 **/
@Repository
public class PhraseSuggestionsRepository extends MasterRepository<Integer> {
  private static final String PHRASE_SUGGESTIONS_SEARCH =
      " FROM (SELECT psm.phrase_suggestions_id, psm.phrase_suggestions_desc, "
          + " psm.status as phrasestatus, psm.dept_id,dept.dept_name, "
          + " psm.phrase_suggestions_category_id, pscm.phrase_suggestions_category "
          + " FROM phrase_suggestions_master psm "
          + " LEFT OUTER JOIN phrase_suggestions_category_master pscm "
          + " USING (phrase_suggestions_category_id) "
          + " LEFT JOIN department dept USING(dept_id) "
          + " ) AS foo ";

  public PhraseSuggestionsRepository() {
    super("phrase_suggestions_master", "phrase_suggestions_id", "phrase_suggestions_desc");
  }

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(PHRASE_SUGGESTIONS_SEARCH);
  }

  private String getPhrase =
      " SELECT phrase_suggestions_id,phrase_suggestions_desc,phrase_suggestions_category_id "
          + " FROM  phrase_suggestions_master "
          + " WHERE (dept_id=? OR COALESCE(dept_id, '') = '') AND status = 'A' "
          + " ORDER BY phrase_suggestions_desc ";

  public List<BasicDynaBean> getPhraseSuggestionsDeptWise(String deptId) {

    return DatabaseHelper.queryToDynaList(getPhrase, new Object[] {deptId});
  }
}
