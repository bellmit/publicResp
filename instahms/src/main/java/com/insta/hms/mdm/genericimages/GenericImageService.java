package com.insta.hms.mdm.genericimages;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenericImageService {

  @LazyAutowired
  private GenericImageRepository repo;

  public List<BasicDynaBean> listAll(List<String> columns) {
    return repo.listAll(columns);
  }

}