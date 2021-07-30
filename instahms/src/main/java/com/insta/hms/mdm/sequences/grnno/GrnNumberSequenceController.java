package com.insta.hms.mdm.sequences.grnno;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.sequences.SequenceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class GrnNumberSequenceController.
 */
@Controller
@RequestMapping(URLRoute.GRN_NUMBER_SEQUENCE_PATH)
public class GrnNumberSequenceController extends SequenceMasterController {

  /**
   * Instantiates a new grn number sequence controller.
   *
   * @param service the service
   */
  public GrnNumberSequenceController(GrnNumberSequenceService service) {
    super(service, MasterResponseRouter.GRN_NUMBER_SEQUENCE_ROUTER);
  }
}
