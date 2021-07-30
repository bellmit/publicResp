package com.insta.hms.mdm.contracttypes;

import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

//@Controller("contractTypeController")
//@RequestMapping(URLRoute.CONTRACT_TYPE_PATH)
public class ContractTypeController extends MasterController {

  public ContractTypeController(ContractTypeService service) {
   super(service, MasterResponseRouter.CONTRACT_TYPE_ROUTER);
  }

}
