package com.insta.hms.mdm.sequences.receiptnumber;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.sequences.SequenceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class ReceiptNumberSequenceController.
 */
@Controller
@RequestMapping(URLRoute.RECEIPT_NUMBER_SEQUENCE_PATH)
public class ReceiptNumberSequenceController extends SequenceMasterController {

  /**
   * Instantiates a new receipt number sequence controller.
   *
   * @param service the service
   */
  public ReceiptNumberSequenceController(ReceiptNumberSequenceService service) {
    super(service, MasterResponseRouter.RECEIPT_NUMBER_SEQUENCE_ROUTER);
  }
}
