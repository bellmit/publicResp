
package com.insta.hms.mdm.vitalparameter.referenceranges;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class ReferenceRangesController.
 *
 * @author sonam
 */
@Controller
@RequestMapping(URLRoute.VITAL_PARAMETER_RANGE)
public class ReferenceRangesController extends MasterController {
  @LazyAutowired
  ReferenceRangesService rangesService;

  public ReferenceRangesController(ReferenceRangesService service) {
    super(service, MasterResponseRouter.VITAL_REFERENCE_RANGE_ROUTER);
  }

  @RequestMapping(value = { "/show", "" }, method = RequestMethod.GET)
  @Override
  public ModelAndView show(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName(router.route("show"));
    Map params = req.getParameterMap();
    modelView.addObject("vitalBean", rangesService.getViatlBean(params).getMap());
    modelView.addObject("resultRanges",
        ConversionUtils.copyListDynaBeansToMap(rangesService.getResultRanges(params)));

    return modelView;
  }

  @RequestMapping(value = "/update", method = RequestMethod.POST)
  @Override
  public ModelAndView update(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {
    ModelAndView modelView = new ModelAndView();
    String paramId = req.getParameter("param_id");
    rangesService.updateReferenceRange(req.getParameterMap());
    attribs.addAttribute("param_id", paramId);
    modelView.setViewName(router.route("update"));
    return modelView;
  }
}
