package com.insta.hms.mdm.sequences.labnumbersequences;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.sequences.SequenceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class LabNumberSequenceController.
 */
@Controller
@RequestMapping(URLRoute.LAB_NUMBER_SEQUENCE_PATH)
public class LabNumberSequenceController extends SequenceMasterController {

  /**
   * Instantiates a new lab number sequence controller.
   *
   * @param service the service
   */
  public LabNumberSequenceController(LabNumberSequenceService service) {
    super(service, MasterResponseRouter.LAB_NUMBER_SEQUENCE_ROUTER);
  }
}
