package com.insta.hms.common.modulesactivated;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

@Service
public class ModulesActivatedService {

  @LazyAutowired
  ModulesActivatedRepository modulesActivatedRepository;

  /**
   * Checks if is module activated.
   *
   * @param moduleName the module name
   * @return true, if is module activated
   */
  public boolean isModuleActivated(String moduleName) {
    boolean active = false;
    BasicDynaBean bean = modulesActivatedRepository.findByKey("module_id", moduleName);
    if (bean != null && bean.get("activation_status").toString().equals("Y")) {
      active = true;
    }
    return active;
  }
}
