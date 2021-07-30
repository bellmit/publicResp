package com.insta.hms.mdm.deathreasons;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class {@link DeathReasonRepository}.
 * 
 * @author deepak_kk
 *
 */
@Repository
public class DeathReasonRepository extends MasterRepository<Integer> {

  public DeathReasonRepository() {
    super("death_reason_master", "reason_id", "reason");
  }
}
