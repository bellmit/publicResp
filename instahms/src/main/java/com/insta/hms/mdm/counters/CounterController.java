package com.insta.hms.mdm.counters;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(URLRoute.COUNTER_PATH)
public class CounterController extends MasterController {
  public CounterController(CounterService service) {
    super(service, MasterResponseRouter.COUNTER_ROUTER);
  }

  @SuppressWarnings({ "rawtypes" })
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((CounterService) getService()).getListPageData(params);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((CounterService) getService()).getAddEditPageData(params);
  }

  /**
   * Gets the all active counters.
   *
   * @param request the request
   * @return the all active counters
   * @throws Exception the exception
   */
  @RequestMapping(value = "/getActiveCounters", method = RequestMethod.GET)
  public ModelMap getAllActiveCounters(
        HttpServletRequest request) throws Exception {
    Map<String, String[]> parameters = request.getParameterMap();
    Integer centerId = (null != parameters && parameters.containsKey("centerId"))
        ? Integer.valueOf(parameters.get("centerId")[0]) : 0;
    List<BasicDynaBean> counterList = ((CounterService) getService())
        .getAllActiveCounters(centerId);
    ModelMap modelMap = new ModelMap();
    modelMap.addAttribute("dtoList", ConversionUtils.listBeanToListMap(counterList));
    modelMap.addAttribute("listSize", counterList.size());
    return modelMap;
  } 
}
