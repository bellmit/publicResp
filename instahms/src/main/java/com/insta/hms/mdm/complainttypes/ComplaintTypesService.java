package com.insta.hms.mdm.complainttypes;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class ComplaintTypesService extends MasterService {

  @LazyAutowired private ComplaintTypesRepository complaintTypeRepository;

  public ComplaintTypesService(ComplaintTypesRepository repo, ComplaintTypesValidator validator) {
    super(repo, validator);
  }
}
