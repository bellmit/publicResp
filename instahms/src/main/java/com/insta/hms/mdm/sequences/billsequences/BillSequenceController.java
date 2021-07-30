package com.insta.hms.mdm.sequences.billsequences;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.sequences.SequenceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class BillSequenceController.
 */
@Controller
@RequestMapping(URLRoute.BILL_SEQUENCE_PATH)
public class BillSequenceController extends SequenceMasterController {

  /**
   * Instantiates a new bill sequence controller.
   *
   * @param service the service
   */
  public BillSequenceController(BillSequenceService service) {
    super(service, MasterResponseRouter.BILL_SEQUENCE_ROUTER);
  }
}
