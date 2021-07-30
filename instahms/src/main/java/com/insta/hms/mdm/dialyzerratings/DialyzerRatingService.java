package com.insta.hms.mdm.dialyzerratings;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class DialyzerRatingService extends MasterService {
  public DialyzerRatingService(DialyzerRatingRepository dialyzerRatingRepository,
      DialyzerRatingValidator dialyzerRatingValidator) {
    super(dialyzerRatingRepository, dialyzerRatingValidator);
  }
}
