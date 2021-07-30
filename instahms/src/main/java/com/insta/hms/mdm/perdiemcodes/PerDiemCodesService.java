package com.insta.hms.mdm.perdiemcodes;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class PerDiemCodesService.
 *
 * @author sonam
 */
@Service
public class PerDiemCodesService extends MasterService {

  /** The per diem codes repository. */
  @LazyAutowired
  PerDiemCodesRepository perDiemCodesRepository;

  /**
   * Instantiates a new per diem codes service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   */
  public PerDiemCodesService(PerDiemCodesRepository repository, PerDiemCodesValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the per diem codes.
   *
   * @return the per diem codes
   */
  public List<BasicDynaBean> getPerDiemCodes() {
    return (perDiemCodesRepository.getPerDiemCodes());
  }

}
