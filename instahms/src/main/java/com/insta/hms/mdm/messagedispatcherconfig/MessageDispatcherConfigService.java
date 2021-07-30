package com.insta.hms.mdm.messagedispatcherconfig;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class MessageDispatcherConfigService extends MasterService {

  @LazyAutowired
  MessageDispatcherConfigRepository messageDispatcherConfigRepository;
  
  public MessageDispatcherConfigService(MessageDispatcherConfigRepository repository,
      MessageDispatcherConfigValidator validator) {
    super(repository, validator);
  }

}
