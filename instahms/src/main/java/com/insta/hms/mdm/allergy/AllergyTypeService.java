package com.insta.hms.mdm.allergy;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Allergy type service.
 */
@Service
public class AllergyTypeService extends MasterService {

  @LazyAutowired
  private AllergyTypeRepository allergyTypeRepository;

  /**
   * Instantiates a new master service.
   * 
   * @param allergyTypeRepository the repository
   * @param allergyTypeValidator the validator
   */
  public AllergyTypeService(AllergyTypeRepository allergyTypeRepository,
      AllergyTypeValidator allergyTypeValidator) {
    super(allergyTypeRepository, allergyTypeValidator);
  }

  /**
   * List all.
   *
   * @param columns the columns
   * @param filterBy the filter by
   * @param filterValue the filter value
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue) {
    return allergyTypeRepository.listAll(columns, filterBy, filterValue);
  }
}
