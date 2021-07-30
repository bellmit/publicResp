package com.insta.hms.core.medicalrecords.codification;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.List;

public class AlternateActivityCodeService {
  @LazyAutowired
  private AlternateActivityCodeRepository alternateActivityCodeRepo;

  public List<BasicDynaBean> getAlternateCodes(String submissionBatchId) {
    return alternateActivityCodeRepo.getAlternateCodes(submissionBatchId);
  }
}
