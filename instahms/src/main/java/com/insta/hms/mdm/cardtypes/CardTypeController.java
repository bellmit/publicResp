package com.insta.hms.mdm.cardtypes;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(URLRoute.CARD_TYPE_PATH)
public class CardTypeController extends MasterController {

  public CardTypeController(CardTypeService service) {
    super(service, MasterResponseRouter.CARD_TYPE_ROUTER);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((CardTypeService) getService()).getAddEditPageData(params);
  }

}
