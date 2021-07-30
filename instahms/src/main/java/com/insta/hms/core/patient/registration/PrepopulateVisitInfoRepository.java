package com.insta.hms.core.patient.registration;

import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * The Class PrepopulateVisitInfoRepository.
 */
@Repository
public class PrepopulateVisitInfoRepository extends GenericRepository {

  /**
   * Instantiates a new prepopulate visit info repository.
   */
  public PrepopulateVisitInfoRepository() {
    super("prepopulate_visit_info");
  }

  /**
   * Gets the prepopulate visit info.
   *
   * @param mrNo
   *          the mr no
   * @return the prepopulate visit info
   */
  public BasicDynaBean getPrepopulateVisitInfo(String mrNo) {
    return this.findByKey("mr_no", mrNo);
  }

}
