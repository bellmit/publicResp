package com.insta.hms.mdm.salutations;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SalutationRepository extends MasterRepository<String> {

  public SalutationRepository() {
    super("salutation_master", "salutation_id", "salutation", new String[] { "salutation_id",
        "salutation", "gender" });
  }

  private static final String GET_SALUTATIONS = "select salutation from salutation_master"
      + " where salutation = ?";

  public String getSalutation(String salutationName) {
    return DatabaseHelper.getString(GET_SALUTATIONS, new Object[] { salutationName });
  }
  
  private static final String GET_ALL_SALUTATION_SORTED_BY_LENGTH = "SELECT"
      + " salutation_id,salutation"
      + " FROM salutation_master"
      + " ORDER BY length(salutation) DESC";
  
  public List<BasicDynaBean> getAllSalutationSortedByLength() {
    return DatabaseHelper.queryToDynaList(GET_ALL_SALUTATION_SORTED_BY_LENGTH);
  }
}
