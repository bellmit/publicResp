package com.insta.hms.mdm.sequences.vouchernosequences;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.sequences.SequenceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class VoucherNoSequenceController.
 */
@Controller
@RequestMapping(URLRoute.VOUCHER_NUMBER_SEQUENCE_PATH)
public class VoucherNoSequenceController extends SequenceMasterController {

  /**
   * Instantiates a new voucher no sequence controller.
   *
   * @param service the service
   */
  public VoucherNoSequenceController(VoucherNoSequenceService service) {
    super(service, MasterResponseRouter.VOUCHER_NUMBER_SEQUENCE_ROUTER);
  }
}
