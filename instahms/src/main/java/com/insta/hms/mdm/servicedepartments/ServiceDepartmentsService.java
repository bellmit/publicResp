package com.insta.hms.mdm.servicedepartments;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class ServiceDepartmentsService as Service facade for Service Departments.
 *
 * @author tanmay.k
 */
@Service("serviceDepartmentsService")
public class ServiceDepartmentsService extends MasterService {

  /**
   * Instantiates a new service departments service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public ServiceDepartmentsService(
      ServiceDepartmentsRepository repository, ServiceDepartmentsValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the active service depts.
   *
   * @return the active service depts
   */
  public List<BasicDynaBean> getActiveServiceDepts() {
    return ((ServiceDepartmentsRepository) getRepository()).getActiveServiceDepts();
  }
}
