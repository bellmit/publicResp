package com.insta.hms.mdm.medicineroute;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class MedicineRouteController.
 *
 * @author sonam
 */
@Controller
@RequestMapping(URLRoute.ROUTE_OF_ADMINISTRATION_PATH)
public class MedicineRouteController extends MasterController {

  /**
   * Instantiates a new medicine route controller.
   *
   * @param service the service
   */
  public MedicineRouteController(MedicineRouteService service) {
    super(service, MasterResponseRouter.ROUTE_OF_ADMINISTRATOIN_ROUTER);
  }
}
