package com.insta.hms.mdm.sequences.billlauditnumbersequences;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.sequences.SequenceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class BillAuditNumberSequenceController.
 */
@Controller
@RequestMapping(URLRoute.BILL_AUDIT_NUMBER_SEQUENCE_PATH)
public class BillAuditNumberSequenceController extends SequenceMasterController {

  /**
   * Instantiates a new bill audit number sequence controller.
   *
   * @param service the service
   */
  public BillAuditNumberSequenceController(BillAuditNumberSequenceService service) {
    super(service, MasterResponseRouter.BILL_AUDIT_NUMBER_SEQUENCE_ROUTER);
  }
}
