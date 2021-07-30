package com.insta.hms.mdm.genericsubclassifications;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * 
 * @author irshadmohammed.
 *
 */
@Repository
public class GenericSubClassificationRepository extends MasterRepository<Integer> {

  public GenericSubClassificationRepository() {
    super("generic_sub_classification_master", "sub_classification_id", "sub_classification_name");
  }
}
