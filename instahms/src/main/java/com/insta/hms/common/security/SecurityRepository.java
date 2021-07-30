package com.insta.hms.common.security;

import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

/**
 * The Class SecurityRepository to get access control attributes from the database.
 * 
 * @author tanmay.k
 */
@Repository("securityRepository")
public class SecurityRepository extends GenericRepository {

  /** The columns. */
  private String[] columns = new String[] { "module_id" };

  /**
   * Instantiates a new security repository.
   */
  public SecurityRepository() {
    super("modules_activated");
  }

  /**
   * Gets the activated modules.
   *
   * @return the activated modules
   */
  public List<BasicDynaBean> getActivatedModules() {
    return super.listAll(Arrays.asList(columns), "activation_status", "Y");
  }
}
