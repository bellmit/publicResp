package com.insta.hms.mdm.dischargesummarytemplates;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class DischargeFormatRepository.
 *
 * @author anup vishwas
 */

@Repository
public class DischargeFormatRepository extends GenericRepository {

  /**
   * Instantiates a new discharge format repository.
   */
  public DischargeFormatRepository() {
    super("discharge_format");
  }

}
