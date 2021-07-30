package com.insta.hms.mdm.genericclassifications;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * 
 * @author irshadmohammed.
 *
 */
@Repository
public class GenericClassificationRepository extends MasterRepository<Integer> {

  public GenericClassificationRepository() {
    super("generic_classification_master", "classification_id", "classification_name");
  }
}
