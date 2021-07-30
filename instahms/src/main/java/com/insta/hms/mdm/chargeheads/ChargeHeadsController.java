package com.insta.hms.mdm.chargeheads;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(URLRoute.CHARGE_HEAD_PATH)
public class ChargeHeadsController extends MasterController {

  @LazyAutowired
  ChargeHeadsService chargeHeadsService;

  public ChargeHeadsController(ChargeHeadsService service) {
    super(service, MasterResponseRouter.CHARGE_HEADS_ROUTER);
    this.chargeHeadsService = service;
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return chargeHeadsService.getAddEditPageData(params);
  }

}
