package com.insta.hms.mdm.stores.genericnames;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class GenericNamesRepository.
 *
 * @author yashwant
 */
@Repository
public class GenericNamesRepository extends MasterRepository<String> {

  /**
   * Instantiates a new generic names repository.
   */
  public GenericNamesRepository() {
    super("generic_name", "generic_code", "generic_name");
  }

  private static final String GET_GENERICS_BY_NAMES =
      "Select * from generic_name where generic_name in (:genericnames)";

  /**
   * Gets the genericsReocrds BY names.
   *
   * @param genericNames the generic names
   * @return the generics
   */
  public List<BasicDynaBean> getGenericsBYNames(List<String> genericNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("genericnames", genericNames);
    return DatabaseHelper.queryToDynaList(GET_GENERICS_BY_NAMES, parameters);
  }
}
