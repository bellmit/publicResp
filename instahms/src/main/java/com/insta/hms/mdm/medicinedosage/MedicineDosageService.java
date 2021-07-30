package com.insta.hms.mdm.medicinedosage;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class MedicineDosageService.
 */
@Service
public class MedicineDosageService extends MasterService {

  /**
   * Instantiates a new medicine dosage service.
   *
   * @param repo the MedicineDosageRepository
   * @param validator the MedicineDosageValidator
   */
  public MedicineDosageService(MedicineDosageRepository repo, MedicineDosageValidator validator) {
    super(repo, validator);
  }

  /**
   * List all.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAll() {
    return getRepository().listAll();
  }

}
