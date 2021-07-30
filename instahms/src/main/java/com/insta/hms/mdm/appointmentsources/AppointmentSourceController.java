package com.insta.hms.mdm.appointmentsources;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

// TODO: Auto-generated Javadoc
/**
 * The Class AppointmentSourceController.
 *
 * @author preeti
 */
@Controller
@RequestMapping(URLRoute.APPT_SOURCE_MASTER_PATH)
public class AppointmentSourceController extends MasterController {

  /**
   * Instantiates a new appointment source controller.
   *
   * @param service
   *          the service
   */
  public AppointmentSourceController(AppointmentSourceService service) {
    super(service, MasterResponseRouter.APPT_SOURCE_MASTER_ROUTER);
  }
}
