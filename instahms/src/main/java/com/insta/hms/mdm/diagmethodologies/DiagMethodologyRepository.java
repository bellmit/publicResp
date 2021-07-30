/**
 * 
 */

package com.insta.hms.mdm.diagmethodologies;

import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.springframework.stereotype.Repository;

/**
 * The Class DiagMethodologyRepository.
 *
 * @author anup.v
 */

@Repository
public class DiagMethodologyRepository extends MasterRepository<Integer> {

  /** The diag methodology search tables. */
  private static String DIAG_METHODOLOGY_SEARCH_TABLES = " FROM diag_methodology_master dgm ";

  /**
   * Instantiates a new diag methodology repository.
   */
  public DiagMethodologyRepository() {
    super("diag_methodology_master", "method_id", "method_name");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(DIAG_METHODOLOGY_SEARCH_TABLES);
  }

}
