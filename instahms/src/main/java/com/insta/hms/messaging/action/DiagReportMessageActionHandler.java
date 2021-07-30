package com.insta.hms.messaging.action;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.core.clinical.order.testvisitreports.TestVisitReportsRepository;
import com.insta.hms.core.diagnostics.reportreviewhistory.ReportReviewHistoryRepository;
import com.insta.hms.documents.TestDocumentsRepository;
import org.apache.commons.beanutils.BasicDynaBean;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class DiagReportMessageActionHandler.
 */
public class DiagReportMessageActionHandler extends MessageActionHandler {

  private static String THIS_ACTION_TYPE = "custom_diag_notification";

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.action.MessageActionHandler#getActionType()
   */
  @Override
  public String getActionType() {
    return THIS_ACTION_TYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.action.MessageActionHandler#handleAction(java.lang.Integer,
   * java.lang.String, java.util.Map)
   */
  @Override
  @Deprecated
  public boolean handleAction(Integer msgId, String option, Map actionContext) throws Exception {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.action.MessageActionHandler#handleAction(java.util.Map)
   */
  @Override
  public boolean handleAction(Map<String, Object> actionData) {
    int entityId = (int) actionData.get("entity_id");
    Object reviewedBy = actionData.get("userId");
    Object reviewRemarks = actionData.get("review_remarks");
    String isTestDoc = (String) actionData.get("is_test_doc");
    ReportReviewHistoryRepository repRevHisRepo =
        ApplicationContextProvider.getBean(ReportReviewHistoryRepository.class);
    BasicDynaBean reviewBean = repRevHisRepo.getBean();
    reviewBean.set("entity_id", entityId);
    reviewBean.set("reviewed_by", reviewedBy);
    reviewBean.set("remarks", reviewRemarks);
    reviewBean.set("reviewed_date", DateUtil.getCurrentTimestamp());
    reviewBean.set("is_test_doc", isTestDoc);
    return repRevHisRepo.insert(reviewBean) > 0;
  }
}
