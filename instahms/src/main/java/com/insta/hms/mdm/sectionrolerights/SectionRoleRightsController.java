package com.insta.hms.mdm.sectionrolerights;

import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;

/**
 * The Class SectionRoleRightsController.
 */
@Controller("sectionRoleRightsController")
public class SectionRoleRightsController extends MasterController {

  /**
   * Instantiates a new section role rights controller.
   *
   * @param service the service
   */
  public SectionRoleRightsController(SectionRoleRigthsService service) {
    super(service, MasterResponseRouter.SECTION_ROLE_RIGHTS_ROUTER);
  }
}
