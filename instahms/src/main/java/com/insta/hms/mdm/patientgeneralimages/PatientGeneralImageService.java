package com.insta.hms.mdm.patientgeneralimages;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class PatientGeneralImageService.
 */
@Service
public class PatientGeneralImageService {

  /** The repo. */
  @LazyAutowired
  private PatientGeneralImageRepository repo;

  /**
   * List all.
   *
   * @param columns the columns
   * @param filterBy the filter by
   * @param filterValue the filter value
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue) {
    return repo.listAll(columns, filterBy, filterValue);
  }

}