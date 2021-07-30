package com.insta.hms.messaging.providers;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.core.diagnostics.reportreviewhistory.ReportReviewHistoryRepository;
import com.insta.hms.messaging.MessageBuilder;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiagNotificationSignedOffBuilder extends MessageBuilder {

  public DiagNotificationSignedOffBuilder() {
    this.addDataProvider(new DiagNotificationSignedOffDataProvider());
  }

  private static final String DIAG_REPORT_PRINT_URL = "/pages/DiagnosticModule/DiagReportPrint.do?"
      + "_method=printReport&forcePdf=true&printerId=2";
  
  private static final String SUPP_TEST_DOC_PRINT_URL = "/Laboratory/TestDocumentsPrint.do?"
      + "_method=print";
  
  @SuppressWarnings("unchecked")
  @Override
  public List<Map<String, Object>> getMessageDetailsList(String messageType, String userId,
      Integer pageNum, Integer pageSize) {
    DiagNotificationSignedOffDataProvider provider = new DiagNotificationSignedOffDataProvider();
    List<BasicDynaBean> entities =
        provider.getMessageEntities(messageType, userId, pageNum, pageSize);
    if (entities == null) {
      return Collections.emptyList();
    }
    List<Map<String, Object>> messageDetailsMap = new ArrayList<>();
    for (BasicDynaBean bean : entities) {
      String docFormat = (String) bean.get("doc_format");
      String printUrl = SUPP_TEST_DOC_PRINT_URL;
      if (StringUtils.isEmpty(docFormat)) {
        printUrl = DIAG_REPORT_PRINT_URL;
      } else {
        if (docFormat.equals("doc_link") && bean.get("doc_location") != null) {
          printUrl = (String) bean.get("doc_location");
        }
      }
      ReportReviewHistoryRepository repoReviewHistoryRepo =
          ApplicationContextProvider.getBean(ReportReviewHistoryRepository.class);
      Map entity = new HashMap();
      entity.putAll(bean.getMap());
      entity.remove("doc_location");
      entity.put("print_url", printUrl);
      entity.put("remarks_history", repoReviewHistoryRepo
          .getReportReviewHistory((int) bean.get("report_id"), (String) bean.get("is_test_doc")));
      messageDetailsMap.add(entity);
    }

    return messageDetailsMap;
  }
}
