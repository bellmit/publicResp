package com.insta.hms.mdm.deathreasons;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class {@link DeathReasonService}.
 * 
 * @author deepak_kk
 *
 */
@Service
public class DeathReasonService extends MasterService {

  public DeathReasonService(DeathReasonRepository deathReasonRepository,
      DeathReasonValidator deathReasonValidator) {
    super(deathReasonRepository, deathReasonValidator);
  }
}
