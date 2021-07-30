package com.insta.hms.mdm.sequences.radiologynumbersequences;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.sequences.SequenceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class RadiologyNumberSequenceController.
 */
@Controller
@RequestMapping(URLRoute.RADIOLOGY_NUMBER_SEQUENCE_PATH)
public class RadiologyNumberSequenceController extends SequenceMasterController {

  /**
   * Instantiates a new radiology number sequence controller.
   *
   * @param service the service
   */
  public RadiologyNumberSequenceController(RadiologyNumberSequenceService service) {
    super(service, MasterResponseRouter.RADIOLOGY_NUMBER_SEQUENCE_ROUTER);
  }
}
