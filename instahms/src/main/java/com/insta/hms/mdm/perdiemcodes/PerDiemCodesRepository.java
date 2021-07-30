package com.insta.hms.mdm.perdiemcodes;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PerDiemCodesRepository.
 *
 * @author sonam
 */
@Repository
public class PerDiemCodesRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new per diem codes repository.
   */
  public PerDiemCodesRepository() {
    super("per_diem_codes_master", "per_diem_code", "per_diem_description");
  }

  /** The Constant GET_PERDIEM_CODES. */
  public static final String GET_PERDIEM_CODES = " SELECT per_diem_description "
      + "|| ' (' || per_diem_code || ') ' AS per_diem_description, per_diem_code "
      + " FROM per_diem_codes_master WHERE status='A' ORDER BY per_diem_description ";

  /**
   * Gets the per diem codes.
   *
   * @return the per diem codes
   */
  public List<BasicDynaBean> getPerDiemCodes() {
    return (DatabaseHelper.queryToDynaList(GET_PERDIEM_CODES));

  }

}
