package com.insta.hms.mdm.dynapackagerules;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class DynaPackageRulesService. */
@Service
public class DynaPackageRulesService extends MasterService {

  /**
   * Instantiates a new dyna package rules service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public DynaPackageRulesService(
      DynaPackageRulesRepository repo, DynaPackageRulesValidator validator) {
    super(repo, validator);
  }

  /**
   * List all.
   *
   * @param sortColumn the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(String sortColumn) {
    return ((DynaPackageRulesRepository) this.getRepository()).listAll(sortColumn);
  }
}
