package com.insta.hms.core.inventory.stockmgmt;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.ftl.FtlPrintService;
import com.insta.hms.documents.PrintConfigurationRepository;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StockTakePrintService extends FtlPrintService {

  @LazyAutowired
  public PrintConfigurationRepository printConfigurationRepo;

  @LazyAutowired
  public StockTakeService stockTakeService;

  /** The Print Parameters for Stock Take Print. */
  private static final String STOCK_TAKE_PRINT_TITLE = "Stock Take";
  private static final String STOCK_TAKE_DEFAULT_HTML_TEMPLATE = "StockTakePrint";
  private static final String STOCK_TAKE_DEFAULT_TEXT_TEMPLATE = "StockTakeTextPrint";

  public enum ReportFormat {
    HTML, TEXT,
  }

  public StockTakePrintService() {
    super(PrintConfigurationRepository.PRINT_TYPE_STORE, STOCK_TAKE_PRINT_TITLE,
        STOCK_TAKE_DEFAULT_HTML_TEMPLATE, STOCK_TAKE_DEFAULT_TEXT_TEMPLATE);
  }

  @Override
  public Map<String, Object> getTemplateDataMap(Map<String, Object> documentParams) {
    String stockTakeId = (String) documentParams.get("stock_take_id");
    Map filterMap = (Map) documentParams.get("items_filter");
    String action = (String)documentParams.get("stock_take_action");
    Map<String, Object> templateDataMap = stockTakeService
        .getPrintData(stockTakeId, filterMap, action);
    return templateDataMap;
  }
}
