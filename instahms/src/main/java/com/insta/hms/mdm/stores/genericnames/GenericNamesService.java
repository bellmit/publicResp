package com.insta.hms.mdm.stores.genericnames;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class GenericNamesService.
 *
 * @author yashwant
 */
@Service
public class GenericNamesService extends MasterService {

  /**
   * Instantiates a new generic names service.
   *
   * @param genericNamesRepository the generic names repository
   * @param genericNamesValidator the generic names validator
   */
  public GenericNamesService(
      GenericNamesRepository genericNamesRepository, GenericNamesValidator genericNamesValidator) {
    super(genericNamesRepository, genericNamesValidator);
  }

  public List<BasicDynaBean> getGenericsBYNames(List<String> genericNames) {
    return ((GenericNamesRepository) getRepository()).getGenericsBYNames(genericNames);
  }

}
