package com.insta.hms.mdm.serviceresources;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class ServiceResourcesService extends MasterService {
  
  @LazyAutowired
  ServiceResourcesRepository repository;

  public ServiceResourcesService(ServiceResourcesRepository repository,
      ServiceResourcesValidator validator) {
      super(repository, validator);
      // TODO Auto-generated constructor stub
  }
  
  public Integer getOverbookLimit(String id) {
    return repository.getOverbookLimit(id);
  }

  public String getEquipmentName(String resourceId) {
    return repository.getServEquipmentName(resourceId);
  }
}
