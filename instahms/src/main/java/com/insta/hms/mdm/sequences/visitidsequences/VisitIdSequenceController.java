package com.insta.hms.mdm.sequences.visitidsequences;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.sequences.SequenceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class VisitIdSequenceController.
 */
@Controller
@RequestMapping(URLRoute.VISIT_ID_SEQUENCE_PATH)
public class VisitIdSequenceController extends SequenceMasterController {

  /**
   * Instantiates a new visit id sequence controller.
   *
   * @param service the service
   */
  public VisitIdSequenceController(VisitIdSequenceService service) {
    super(service, MasterResponseRouter.VISIT_ID_SEQUENCE_ROUTER);
  }
}
