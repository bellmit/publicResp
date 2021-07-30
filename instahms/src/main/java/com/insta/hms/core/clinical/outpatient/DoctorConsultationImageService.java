package com.insta.hms.core.clinical.outpatient;

import com.insta.hms.common.annotations.LazyAutowired;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class DoctorConsultationImageService.
 *
 * @author anup vishwas
 */
@Service
public class DoctorConsultationImageService {

  /** The doctor consultation image repo. */
  @LazyAutowired
  private DoctorConsultationImageRepository doctorConsultationImageRepo;

  /**
   * List all.
   *
   * @param imageColumnList the image column list
   * @param filterBy the filter by
   * @param filterValue the filter value
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> imageColumnList, String filterBy,
      Object filterValue) {

    return doctorConsultationImageRepo.listAll(imageColumnList, filterBy, filterValue);
  }

}
