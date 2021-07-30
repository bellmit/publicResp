package com.insta.hms.mdm.genericsubclassifications;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.genericclassifications.GenericClassificationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenericSubClassificationService extends MasterService {

  @LazyAutowired
  GenericClassificationService genericClassificationService;

  public GenericSubClassificationService(GenericSubClassificationRepository repository,
      GenericSubClassificationValidator validator) {
    super(repository, validator);
  }

  public List<BasicDynaBean> getClassificationdetails() {
    return genericClassificationService.lookup(false);
  }
}
