package com.insta.hms.core.inventory.stockmgmt;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.URLRoute;
import com.insta.hms.core.inventory.stockmgmt.StockTakeService.StockTakeAction;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;


/**
 * The Class StockManagementController.
 */
public abstract class StockTakePrintController extends BaseRestController {

  /** The Constant logger. */
  static final Logger logger = LoggerFactory
      .getLogger(StockTakePrintController.class);

  /** The stock take print service. */
  @LazyAutowired
  private StockTakePrintService stockTakePrintService;

  /**
   * Generates the print for a stock take.
   * 
   * @param stockTakeId
   *          The ID of the stock take which should be printed
   * @param templateName
   *          The name of the template to be used to generate the print
   * @param printerId
   *          The printer configuration id to be used to format the print
   * @param response
   *          HttpServletResponse object to send the output to
   * @return Returns the ModelAndView filled with text print content in case of
   *         a text print, empty ModelAndView otherwise
   * @throws SQLException
   *           The {@link SQLException}
   * @throws IOException
   *           The {@link IOException}
   * @throws XPathExpressionException
   *           The {@link XPathExpressionException}
   * @throws DocumentException
   *           The {@link DocumentException}
   * @throws TransformerException
   *           The {@link TransformerException}
   * @throws TemplateException
   *           The {@link TemplateException}
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = URLRoute.STOCK_TAKE_PRINT)
  public ModelAndView printStockTake(
      @RequestParam(value = "stock_take_id",
          required = true) String stockTakeId,
      @RequestParam(value = "_template_name",
          required = true) String templateName,
      @RequestParam(value = "_printer_id", required = true) Integer printerId,
      HttpServletRequest request, HttpServletResponse response)
          throws SQLException, IOException, XPathExpressionException,
          DocumentException, TransformerException, TemplateException {

    ModelAndView mav = new ModelAndView();
    Map<String, Object> responseMap = new HashMap<String, Object>();
    Map<String, Object> documentParams = new HashMap<String, Object>();
    documentParams.put("stock_take_id", stockTakeId);
    documentParams.put("items_filter", request.getParameterMap());
    documentParams.put("stock_take_action", getDelegateAction().toString());

    String contentType = stockTakePrintService.getPrintContentType(printerId);
    // null will be returned if no specific content type is required
    // to be set for this print mode
    if (null != contentType) {
      response.setContentType(contentType);
      try (OutputStream os = response.getOutputStream()) {

        stockTakePrintService.generatePrint(documentParams, printerId,
            templateName, os, responseMap);
      }
    } else { // pdf is not the output type
      stockTakePrintService.generatePrint(documentParams, printerId,
          templateName, null, responseMap);
      mav.addAllObjects(responseMap);
      if (null != responseMap && responseMap.size() > 0) {
        mav.setViewName("/pages/Common/PrintTextReport");
      }
    }
    return mav;
  }

  protected abstract StockTakeAction getDelegateAction();

}
