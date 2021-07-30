package com.insta.hms.mdm.countries;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class CountryRepository extends MasterRepository<String> {

  public CountryRepository() {
    super("country_master", "country_id", "country_name");
  }

  private static final String GET_NATIONALITY_FROM_CODE = "SELECT * FROM country_master "
      + " WHERE alpha3_code = ? OR alpha2_code = ? OR country_name = ? ";

  public BasicDynaBean getNationality(String nationalityCode) {
    return DatabaseHelper.queryToDynaBean(GET_NATIONALITY_FROM_CODE, nationalityCode,
        nationalityCode, nationalityCode);
  }
}
