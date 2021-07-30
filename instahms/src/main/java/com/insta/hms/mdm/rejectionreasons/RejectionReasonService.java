package com.insta.hms.mdm.rejectionreasons;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class RejectionReasonService extends MasterService {

  public RejectionReasonService(RejectionReasonRepository rejectionReasonRepository,
      RejectionReasonValidator rejectionReasonValidator) {
    super(rejectionReasonRepository, rejectionReasonValidator);
  }
}
