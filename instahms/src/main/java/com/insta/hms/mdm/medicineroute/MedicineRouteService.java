package com.insta.hms.mdm.medicineroute;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class MedicineRouteService.
 *
 * @author sonam
 */
@Service
public class MedicineRouteService extends MasterService {

  @LazyAutowired
  MedicineRouteRepository medicineRouteRepository;

  /**
   * Instantiates a new medicine route service.
   *
   * @param repo the MedicineRouteRepository
   * @param validator the MedicineRouteValidation
   */
  public MedicineRouteService(MedicineRouteRepository repo, MedicineRouteValidation validator) {
    super(repo, validator);
  }

  /**
   * List all.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAll() {
    return medicineRouteRepository.listAll(null, "status", "A");
  }
}
