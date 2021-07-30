package com.insta.hms.integration.insurance.pbmauthorization;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Service;

/**
 * The Class PBMService.
 */
@Service
public class PBMService extends BusinessService {

  /** The pbm prescription repository. */
  @LazyAutowired
  private PBMMedicinePrescriptionsRepository pbmPrescriptionRepository;

  /**
   * Update PBM prescription id.
   *
   * @param consId the cons id
   * @param pbmPrescId the pbm presc id
   * @return true, if successful
   */
  public boolean updatePBMPrescriptionId(Object consId, Integer pbmPrescId) {
    return pbmPrescriptionRepository.updatePBMPrescriptionId(consId, pbmPrescId);
  }
}
