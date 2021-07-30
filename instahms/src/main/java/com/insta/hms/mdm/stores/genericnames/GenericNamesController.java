package com.insta.hms.mdm.stores.genericnames;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * The Class GenericNamesController.
 *
 * @author yashwant
 */
@Controller("genericNamesController")
@RequestMapping(URLRoute.GENERIC_NAMES_PATH)
public class GenericNamesController extends MasterController {

  /**
   * Instantiates a new generic names controller.
   *
   * @param service the service
   */
  public GenericNamesController(GenericNamesService service) {
    super(service, MasterResponseRouter.GENERIC_NAMES);
  }
}
