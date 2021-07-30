package com.insta.hms.mdm.histoimpressions;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class HistoImpressionRepository.
 */
@Repository
public class HistoImpressionRepository extends MasterRepository<String> {

  /**
   * Instantiates a new histo impression repository.
   */
  public HistoImpressionRepository() {
    super("histo_impression_master", "impression_id");
  }
}
