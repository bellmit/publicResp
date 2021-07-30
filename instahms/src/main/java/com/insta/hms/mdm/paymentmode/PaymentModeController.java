package com.insta.hms.mdm.paymentmode;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
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
 * The Class PaymentModeController.
 */
@Controller
@RequestMapping(URLRoute.PAYMENT_MODE_PATH)
public class PaymentModeController extends MasterController {

  /** The payment mode service. */
  @Autowired
  PaymentModeService paymentModeService;

  /**
   * Instantiates a new payment mode controller.
   *
   * @param service
   *          the service
   */
  public PaymentModeController(PaymentModeService service) {
    super(service, MasterResponseRouter.PAYMENT_MODE_ROUTER);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((PaymentModeService) getService()).getAddEditPageData(params);
  }

  @RequestMapping(value = "/update", method = RequestMethod.POST)
  @Override
  public ModelAndView update(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes redirect) {

    Map<String, String[]> parameters = req.getParameterMap();
    boolean success = paymentModeService.updatePaymentMode(parameters);
    MessageUtil messageutil = ApplicationContextProvider.getBean(MessageUtil.class);

    String modeId = parameters.get("mode_id")[0];
    if (success) {
      redirect.addFlashAttribute("info",
          messageutil.getMessage("flash.updated.successfully", null));
    } else {
      redirect.addFlashAttribute("info", messageutil.getMessage("flash.update.failed", null));
    }
    
    int paymodeId = Integer.parseInt(modeId);
    BasicDynaBean paymentmodeBean = paymentModeService.getPaymentMode("mode_id", paymodeId);

    Map paymentModeMap = paymentmodeBean.getMap();
    redirect.mergeAttributes(paymentModeMap);
    return new ModelAndView(URLRoute.PAYMENT_MODE_REDIRECT_TO_SHOW);
  }

}
