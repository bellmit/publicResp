package com.insta.hms.mdm.generalmessagetypes;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;


@Repository
public class GeneralMessageTypesRepository extends GenericRepository {
  
  public GeneralMessageTypesRepository() {
    super("message_types");
  }

}
