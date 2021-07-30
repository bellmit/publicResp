package com.insta.hms.mdm.messagedispatcherconfig;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URLRoute.MESSAGE_DISPATCHER_CONFIG_PATH)
public class MessageDispatcherConfigController extends MasterController {

  public MessageDispatcherConfigController(MessageDispatcherConfigService service) {
    super(service, MasterResponseRouter.MESSAGE_DISPATCHER_CONFIG_ROUTER);
  }

}
