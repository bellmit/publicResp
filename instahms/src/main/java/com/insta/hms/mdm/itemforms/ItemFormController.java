package com.insta.hms.mdm.itemforms;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class ItemFormController.
 * @author irshadmohammed
 */
@Controller
@RequestMapping(URLRoute.ITEM_FORM_PATH)
public class ItemFormController extends MasterController {

  /**
   * Instantiates a new item form controller.
   *
   * @param service the ItemFormService
   */
  public ItemFormController(ItemFormService service) {
    super(service, MasterResponseRouter.ITEM_FORM_ROUTER);
  }

}
