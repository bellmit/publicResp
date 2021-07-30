package com.insta.hms.mdm.salutations;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SalutationService extends MasterService {

  @LazyAutowired
  SalutationRepository salutationRepository;

  public SalutationService(SalutationRepository repository, SalutationValidator validator) {
    super(repository, validator);
  }

  public BasicDynaBean getSalutationId(Map<String, Object> params) {
    return salutationRepository.findByKey(params);
  }

  public String getSalutation(String salutationName) {
    return salutationRepository.getSalutation(salutationName);
  }

}
