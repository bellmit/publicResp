
package com.insta.hms.mdm.vitalparameters;

import com.insta.hms.common.PagedList;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class VitalParameterController.
 *
 * @author sonam
 */
@Controller
@RequestMapping(URLRoute.VITAL_PARAMETER_PATH)
public class VitalParameterController extends MasterController {

  /**
   * Instantiates a new vital parameter controller.
   *
   * @param service
   *          the service
   */
  public VitalParameterController(VitalParameterService service) {
    super(service, MasterResponseRouter.VITAL_PARAMETER_ROUTER);
  }


  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = { "/list", "" }, method = RequestMethod.GET)
  public ModelAndView list(HttpServletRequest req, HttpServletResponse resp) {

    ModelAndView modelView = new ModelAndView();
    Map paramMap = new HashMap<String, String[]>();
    if (req.getParameterMap() != null) {
      paramMap.putAll(req.getParameterMap());
    }
    paramMap.put("param_container", new String[]{"I", "O"});
    PagedList pagedList = getService().search(paramMap);
    modelView.addObject("pagedList", pagedList);
    addReferenceData(getFilterLookups(paramMap), modelView);
    modelView.setViewName(router.route("list"));
    return modelView;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterController#getFilterLookupLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((VitalParameterService) getService()).getListPageData();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((VitalParameterService) getService()).getAddEditPageData(params);
  }

}
