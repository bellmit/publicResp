package com.insta.hms.mdm.ward;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class WardRepository.
 *
 * @author sonam
 */
@Repository
public class WardRepository extends MasterRepository<Integer> {

  public WardRepository() {
    super("ward_names", "ward_no", "ward_name");
  }

}
