package com.insta.hms.mdm.hospitalprintfiles;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * The Class HospitalPrintFileRepository.
 */
@Repository
public class HospitalPrintFileRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new hospital print file repository.
   */
  public HospitalPrintFileRepository() {
    super("hosp_print_master_files", "center_id");
  }

  /** The Constant GET_SCREEN_LOGO_SIZE. */
  public static final String GET_SCREEN_LOGO_SIZE =
      "SELECT length(screen_logo) as screen_logo_size "
      + " FROM hosp_print_master_files where center_id = 0";

  /**
   * Gets the file size.
   *
   * @return the file size
   */
  public BasicDynaBean getFileSize() {
    return DatabaseHelper.queryToDynaBean(GET_SCREEN_LOGO_SIZE);
  }
}
