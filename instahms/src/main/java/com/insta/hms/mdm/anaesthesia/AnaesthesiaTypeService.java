package com.insta.hms.mdm.anaesthesia;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class AnaesthesiaTypeService.
 */
@Service
public class AnaesthesiaTypeService extends MasterService {

  /**
   * Instantiates a new anaesthesia type service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   */
  public AnaesthesiaTypeService(AnaesthesiaTypeRepository repository,
      AnaesthesiaTypeValidator validator) {
    super(repository, validator);
  }

  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue,
      String sortColumn) {
    return ((AnaesthesiaTypeRepository) getRepository()).listAll(columns, filterBy, filterValue,
        sortColumn);
  }

  /**
   * Gets the anaesthesia type sub group tax details.
   *
   * @param actDescriptionId
   *          the act description id
   * @return the anaesthesia type sub group tax details
   */
  public List<BasicDynaBean> getAnaesthesiaTypeSubGroupTaxDetails(String actDescriptionId) {
    return ((AnaesthesiaTypeRepository) getRepository())
        .getAnaesthesiaTypeSubGroupTaxDetails(actDescriptionId);
  }
}
