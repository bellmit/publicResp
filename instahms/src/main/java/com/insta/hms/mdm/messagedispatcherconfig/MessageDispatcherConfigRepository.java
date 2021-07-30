package com.insta.hms.mdm.messagedispatcherconfig;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * MessageDispatcherConfig Repository.
 */
@Repository
public class MessageDispatcherConfigRepository extends MasterRepository<String> {

  public MessageDispatcherConfigRepository() {
    super("message_dispatcher_config", "message_mode", "display_name");
  }
}
