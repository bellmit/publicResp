package com.insta.hms.mdm.role;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.sql.SQLException;
import java.util.Map;

@Controller
@RequestMapping(URLRoute.ROLES_PATH)
public class RoleController extends MasterRestController {
  public RoleController(RoleMasterService service) {
    super(service);
  }

  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getIndexPage() {
    return renderMasterUi("Master", "billingPreferences");
  }
}
