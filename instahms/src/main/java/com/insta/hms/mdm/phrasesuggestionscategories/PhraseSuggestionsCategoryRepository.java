/** */

package com.insta.hms.mdm.phrasesuggestionscategories;

import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.springframework.stereotype.Repository;

/** 
 * @author sonam.
 *  */
@Repository
public class PhraseSuggestionsCategoryRepository extends MasterRepository<Integer> {
  private static final String PHRASE_SUGGESTIONS_CATEGORY_FROM =
      " FROM phrase_suggestions_category_master ";

  /**
   * constructor.
   */
  public PhraseSuggestionsCategoryRepository() {
    super(
        "phrase_suggestions_category_master",
        "phrase_suggestions_category_id",
        "phrase_suggestions_category"); // table, primary key
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(PHRASE_SUGGESTIONS_CATEGORY_FROM);
  }
}
