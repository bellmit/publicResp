package com.insta.hms.mdm.section.fields;

import com.insta.hms.mdm.MasterDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class FieldsService.
 *
 * @author krishnat
 */
@Service
public class FieldsService extends MasterDetailsService {

  /** The option repository. */
  OptionsRepository optionRepository;

  /** The r. */
  FieldsRepository repository;

  /**
   * Instantiates a new fields service.
   *
   * @param repository
   *          the r
   * @param validator
   *          the v
   * @param dr
   *          the dr
   */
  public FieldsService(FieldsRepository repository, FieldsValidator validator,
      OptionsRepository dr) {
    super(repository, validator, dr);
    this.optionRepository = dr;
    this.repository = repository;
  }

  /**
   * Gets the field options.
   *
   * @param fieldId
   *          the field id
   * @return the field options
   */
  public List<BasicDynaBean> getFieldOptions(int fieldId) {
    return optionRepository.getFieldOptions(fieldId);
  }

  /**
   * Gets the field.
   *
   * @param fieldId
   *          the field id
   * @return the field
   */
  public BasicDynaBean getField(int fieldId) {
    return repository.getBean(fieldId);
  }

  /**
   * Gets the fieldsby filter.
   *
   * @param filter the filter
   * @return the fieldsby filter
   */
  public List<BasicDynaBean> getFieldsbyFilter(Map<String, Object> filter) {
    return getRepository().listAll(null, filter, null);
  }

}
