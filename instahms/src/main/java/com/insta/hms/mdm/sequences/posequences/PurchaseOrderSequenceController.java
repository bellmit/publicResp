package com.insta.hms.mdm.sequences.posequences;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.sequences.SequenceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class PurchaseOrderSequenceController.
 */
@Controller
@RequestMapping(URLRoute.PURCHASE_ORDER_SEQUENCE_PATH)
public class PurchaseOrderSequenceController extends SequenceMasterController {

  /**
   * Instantiates a new purchase order sequence controller.
   *
   * @param service the service
   */
  public PurchaseOrderSequenceController(PurchaseOrderSequenceService service) {
    super(service, MasterResponseRouter.PURCHASE_ORDER_SEQUENCE_ROUTER);
  }
}
