package com.insta.hms.mdm.imagemarkers;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class ImageMarkersController.
 */
@Controller
@RequestMapping(URLRoute.IMAGE_MARKERS_PATH)
public class ImageMarkersController extends MasterController {

  /**
   * Instantiates a new image markers controller.
   *
   * @param service the service
   */
  public ImageMarkersController(ImageMarkersService service) {
    super(service, MasterResponseRouter.IMAGE_MARKERS_ROUTER);
  }
}
