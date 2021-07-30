package com.insta.hms.mdm.ivinfusionsites;

import com.insta.hms.common.annotations.LazyAutowired;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class IvInfusionsitesService.
 */
@Service
public class IvInfusionsitesService {

  /** The repo. */
  @LazyAutowired
  private IvInfusionsitesRepository repo;

  /**
   * List all.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAll() {
    return repo.listAll(null, "status", "A");
  }

}
