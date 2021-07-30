package com.insta.hms.mdm.sequences.claimidsequences;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.sequences.SequenceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class ClaimIdSequenceController.
 */
@Controller
@RequestMapping(URLRoute.CLAIM_ID_SEQUENCE_PATH)
public class ClaimIdSequenceController extends SequenceMasterController {

  /**
   * Instantiates a new claim id sequence controller.
   *
   * @param service the service
   */
  public ClaimIdSequenceController(ClaimIdSequenceService service) {
    super(service, MasterResponseRouter.CLAIM_ID_SEQUENCE_ROUTER);
  }
}
