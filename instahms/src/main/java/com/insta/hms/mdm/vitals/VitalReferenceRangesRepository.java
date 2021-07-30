package com.insta.hms.mdm.vitals;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class VitalReferenceRangesRepository.
 *
 * @author yashwant
 */
@Repository
public class VitalReferenceRangesRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new vital reference ranges repository.
   */
  public VitalReferenceRangesRepository() {
    super("vital_reference_range_master", "range_id");
  }
}
