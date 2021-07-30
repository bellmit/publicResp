package com.insta.hms.mdm.imagemarkers;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class ImageMarkersService.
 */
@Service
public class ImageMarkersService extends MasterService {

  /**
   * Instantiates a new image markers service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public ImageMarkersService(ImageMarkersRepository repo, ImageMarkersValidator validator) {
    super(repo, validator);
  }
}
