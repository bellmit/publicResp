package com.insta.hms.mdm.practitionertypes;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PractitionerTypeService extends MasterService {

  @LazyAutowired private PractitionerTypeRepository repository;

  @LazyAutowired private PractitionerTypeMappingsRepository mappingRepository;

  /**
   * constructor.
   * @param repository repository object
   * @param validator validator object
   */
  public PractitionerTypeService(PractitionerTypeRepository repository, 
      PractitionerTypeValidator validator) {
    super(repository, validator);
    // TODO Auto-generated constructor stub
  }

  /**
   * return practitioner types.
   * @return list
   */
  public List<BasicDynaBean> getPractitionerTypes() {
    List<String> columns = new ArrayList<String>();
    columns.add("practitioner_id");
    columns.add("practitioner_name");
    List<BasicDynaBean> result = repository.listAll(columns, "status", "A");
    return result;
  }
  
  /**
   * Insert.
   *
   * @param practitionerTypeName the practitioner type name
   * @param status the status
   * @return the int
   */
  public int insert(String practitionerTypeName, String status) {
    BasicDynaBean bean = repository.getBean();
    bean.set("practitioner_id", repository.getNextId());
    bean.set("practitioner_name", practitionerTypeName);
    bean.set("status", status);
    int success = repository.insert(bean);
    if (success <= 0) {
      throw new EntityNotFoundException(new String[] { practitionerTypeName });
    }
    
    return (int) bean.get("practitioner_id");
  }

  /**
   * Check If practitioner type is active.
   * @param practitionerType practitioner type
   * @return boolean
   */
  public boolean checkIfPractitionerTypeIsActive(Integer practitionerType) {
    BasicDynaBean basicDynaBean = repository.findByKey("practitioner_id", practitionerType);
    if (basicDynaBean.get("status").equals("A")) {
      return true;
    }
    return false;
  }
}
