package com.insta.hms.mdm.dailyrecurrences;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The controller for Recurrence Daily Master.
 * @author sainathbatthala 
 */

@Controller
@RequestMapping(URLRoute.RECURRENCE_DAILY_MASTER_PATH)
public class RecurrenceDailyController extends MasterController {

  public RecurrenceDailyController(RecurrenceDailyService service) {

    super(service, MasterResponseRouter.RECURRENCE_DAILY_MASTER);
  }

  /**
   * This method handles delete.
   * 
   * @param req The request parameter
   * @param resp The response parameter
   * @param attribs The Redirect Attributes
   * @return Returns ModelAndView
   */
  @Override
  @RequestMapping(path = "/delete", method = RequestMethod.POST)
  protected ModelAndView delete(HttpServletRequest req, HttpServletResponse resp,
      Boolean hardDelete, RedirectAttributes attribs) {
    
    Map<String, String[]> params = req.getParameterMap();
    String[] deleteIds =  params.get("deleteRecord");

    ((RecurrenceDailyService) this.getService()).delete(deleteIds);
    attribs.addAttribute("sortOrder", "display_name");
    attribs.addAttribute("sortReverse", "false");
    attribs.addAttribute("status", "A");
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName(router.route("delete"));
    return modelAndView;
  }

  /**
   * Overriding with required implementation.
   * 
   * @param params The params parameter
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((RecurrenceDailyService) getService()).getAddEditPageData(params);
  }
}
