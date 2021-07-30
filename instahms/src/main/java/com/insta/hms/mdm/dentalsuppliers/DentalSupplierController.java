package com.insta.hms.mdm.dentalsuppliers;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * The Class is for dental supplier master.
 * @author amolbagde
 *
 */
@Controller
@RequestMapping(URLRoute.DENTAL_SUPPLIER_MASTER)
public class DentalSupplierController extends MasterController {

  public DentalSupplierController(DentalSupplierService service) {
    super(service, MasterResponseRouter.DENTAL_SUPPLIER_MASTER_ROUTER);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((DentalSupplierService) getService()).getListPageData();
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((DentalSupplierService) getService()).getAddEditPageData();
  }
}
