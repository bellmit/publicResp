package com.insta.hms.mdm.medicationservingremarks;

import com.insta.hms.common.annotations.LazyAutowired;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class MedicationServingRemarksService.
 */
@Service
public class MedicationServingRemarksService {

  /** The repo. */
  @LazyAutowired
  private MedicationServingRemarksRepository repo;

  /**
   * List all.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAll() {
    return repo.listAll(null, "status", "A");
  }

}
