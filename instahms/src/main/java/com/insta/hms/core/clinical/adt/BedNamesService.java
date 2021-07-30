package com.insta.hms.core.clinical.adt;

import com.insta.hms.common.annotations.LazyAutowired;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

@Service
public class BedNamesService {

  @LazyAutowired
  private BedNamesRepository bedNamesRepository;

  public BasicDynaBean findByKey(int bedId) {
    return bedNamesRepository.findByKey("bed_id", bedId);
  }
}
