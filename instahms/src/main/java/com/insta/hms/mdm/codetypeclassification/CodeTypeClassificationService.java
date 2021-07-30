package com.insta.hms.mdm.codetypeclassification;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeTypeClassificationService {

  @LazyAutowired CodeTypeClassificationRepository repo;

  public List<BasicDynaBean> listAll() {
    return repo.listAll();
  }
}
