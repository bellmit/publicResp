package com.insta.hms.mdm.genericclassifications;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * 
 * @author irshadmohammed.
 *
 */
@Service
public class GenericClassificationService extends MasterService {

  public GenericClassificationService(GenericClassificationRepository gr,
      GenericClassificationValidator gv) {
    super(gr, gv);
  }
}
