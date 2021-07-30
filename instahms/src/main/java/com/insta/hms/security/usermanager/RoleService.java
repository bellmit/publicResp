package com.insta.hms.security.usermanager;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

  @LazyAutowired
  private RoleRepository roleRepository;

  public List<BasicDynaBean> listAll() {
    return roleRepository.listAll();
  }
  
  public List<BasicDynaBean> listAll(String key, String value) {
    return roleRepository.listAll(null, key, value);
  }

}
