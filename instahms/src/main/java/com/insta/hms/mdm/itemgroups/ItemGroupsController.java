package com.insta.hms.mdm.itemgroups;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/** The Class ItemGroupsController. */
@Controller
@RequestMapping(URLRoute.BILLING_GROUP_MASTER)
public class ItemGroupsController extends MasterRestController {

  /**
   * Instantiates a new item groups controller.
   *
   * @param service the service
   */
  public ItemGroupsController(ItemGroupsService service) {
    super(service);
  }

  /**
   * Gets the billing group index page.
   *
   * @return the billing group index page
   */
  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getBillingGroupIndexPage() {
    return renderMasterUi("Master", "billingPreferences");
  }
}
