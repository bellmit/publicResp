package com.insta.hms.mdm.histoimpressions;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class HistoImpressionService.
 */
@Service
public class HistoImpressionService extends MasterService {

  /**
   * Instantiates a new histo impression service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public HistoImpressionService(HistoImpressionRepository repo, 
        HistoImpressionValidator validator) {
    super(repo, validator);
  }
}
