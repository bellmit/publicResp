package com.insta.hms.common.preferences.systempreferences;

import com.insta.hms.common.URLRoute;
import com.insta.hms.common.preferences.PreferencesController;
import com.insta.hms.common.preferences.PreferencesResponseRouter;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("systemPreferencesController")
@RequestMapping(URLRoute.SYSTEM_PREFERENCES)
public class SystemPreferencesController extends PreferencesController {

  public SystemPreferencesController(GenericPreferencesService service) {
    super(service, PreferencesResponseRouter.SYSTEM_PREFERENCES_ROUTER);
  }

}
