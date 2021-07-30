package com.insta.hms.mdm.generalmessagetypes;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;


@Service
public class GeneralMessageTypesService {

  @LazyAutowired
  private GeneralMessageTypesRepository generalMessageTypesRepo;
  
  public BasicDynaBean findByKey(String keyColumn, String identifier) {
    return generalMessageTypesRepo.findByKey(keyColumn, identifier);
  }
  
}
