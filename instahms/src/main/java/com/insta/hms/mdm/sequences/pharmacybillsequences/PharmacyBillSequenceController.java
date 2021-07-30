package com.insta.hms.mdm.sequences.pharmacybillsequences;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.sequences.SequenceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class PharmacyBillSequenceController.
 */
@Controller
@RequestMapping(URLRoute.PHARMACY_BILL_ID_SEQUENCE_PATH)
public class PharmacyBillSequenceController extends SequenceMasterController {

  /**
   * Instantiates a new pharmacy bill sequence controller.
   *
   * @param service the service
   */
  public PharmacyBillSequenceController(PharmacyBillSequenceService service) {
    super(service, MasterResponseRouter.PHARMACY_BILL_SEQUENCE_ROUTER);
  }
}
