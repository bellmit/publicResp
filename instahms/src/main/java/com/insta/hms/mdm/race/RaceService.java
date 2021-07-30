package com.insta.hms.mdm.race;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class RaceService extends MasterService {

  public RaceService(RaceRepository repository, RaceValidator validatior) {
    super(repository, validatior);
  }

}
