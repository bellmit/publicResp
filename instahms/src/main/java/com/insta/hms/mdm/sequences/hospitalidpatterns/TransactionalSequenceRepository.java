package com.insta.hms.mdm.sequences.hospitalidpatterns;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class TransactionalSequenceRepository extends MasterRepository<String> {

  public TransactionalSequenceRepository() {
    super("transactional_sequence", "sequence_name");
  }
  
  @Override
  public boolean supportsAutoId() {
    return false;
  }
  

}
