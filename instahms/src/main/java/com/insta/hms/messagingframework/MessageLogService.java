package com.insta.hms.messagingframework;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.messaging.MessageActionService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MessageLogService.
 *
 * @author pranaysahota
 */
@Service
public class MessageLogService {

  /** The message logs repo. */
  @LazyAutowired
  private MessageLogRepository messageLogRepository;

  /** The message action service. */
  private MessageActionService messageActionService;

  /** The messaging service. */
  @LazyAutowired
  private MessagingService messagingService;

  /** The Constant OPTIONS. */
  private static final String OPTIONS = "options";

  /**
   * Gets the user notification metadata.
   *
   * @param userId
   *          the user id
   * @return the user notification metadata
   * @throws SQLException
   *           the SQL exception
   */
  public NotificationMetadata getNotificationMetadata(String userId) throws SQLException {
    List<BasicDynaBean> beans = messageLogRepository.getNotificationCount(userId);
    messageActionService = new MessageActionService();
    Map<String, Map<String, Map<String, Object>>> notifDetailsMap = new HashMap<>();
    String messageCategory = "message_category_name";
    String count = "count";
    String messageTypeId = "message_type_id";
    String messageTypeName = "message_type_name";
    String messageTypeDescription = "message_type_description";
    String messageActions = "actions";
    String messageActionTypes = "message_action_types";
    String messageActionTypeList = "message_action_type_list";
    long totalMessageCount = 0;

    for (BasicDynaBean bean : beans) {
      Map<String, Object> msgActionTypeMap = getMessageActionsList(
          (String) bean.get(messageTypeId));
      if (!notifDetailsMap.containsKey(bean.get(messageCategory))) {
        Map<String, Object> countActionsMap = new HashMap<>();
        countActionsMap.put(count, bean.get(count));
        countActionsMap.put(messageTypeName, bean.get(messageTypeName));
        countActionsMap.put(messageTypeDescription, bean.get(messageTypeDescription));
        countActionsMap.put(messageActions, msgActionTypeMap.get(OPTIONS));
        countActionsMap.put(messageActionTypes, msgActionTypeMap.get(messageActionTypeList));
        Map<String, Map<String, Object>> categoryMap = new HashMap<>();
        categoryMap.put((String) bean.get(messageTypeId), countActionsMap);
        notifDetailsMap.put((String) bean.get(messageCategory), categoryMap);
        totalMessageCount += (Long) bean.get(count);
      } else {
        Map<String, Map<String, Object>> categoryMap = notifDetailsMap
            .get(bean.get(messageCategory));
        if (!categoryMap.containsKey(bean.get(messageTypeId))) {
          Map<String, Object> countActionsMap = new HashMap<>();
          countActionsMap.put(count, bean.get(count));
          countActionsMap.put(messageTypeName, bean.get(messageTypeName));
          countActionsMap.put(messageActions, msgActionTypeMap.get(OPTIONS));
          countActionsMap.put(messageActionTypes, msgActionTypeMap.get(messageActionTypeList));
          categoryMap.put((String) bean.get(messageTypeId), countActionsMap);
          totalMessageCount += (Long) bean.get(count);
        }
      }
    }
    return new NotificationMetadata(totalMessageCount, notifDetailsMap);
  }

  /**
   * Gets the message actions list.
   *
   * @param messageType
   *          the message type
   * @return the message actions list
   * @throws SQLException
   *           the SQL exception
   */
  private Map<String, Object> getMessageActionsList(String messageType) throws SQLException {
    List<BasicDynaBean> messageActionsBeans = messageActionService.getMessageActions(messageType);
    String[] actionOptions = new String[] {};
    List<String> actionTypeList = new ArrayList<>();
    for (BasicDynaBean bean : messageActionsBeans) {
      actionOptions = ((String) bean.get(OPTIONS)).split(",");
      actionTypeList.add((String) bean.get("message_action_type"));
    }
    List<String> actionOptionsList = new ArrayList<>();
    for (String s : actionOptions) {
      actionOptionsList.add(s);
    }
    Map<String, Object> messageActionTypeMap = new HashMap<>();
    messageActionTypeMap.put(OPTIONS, actionOptionsList);
    messageActionTypeMap.put("message_action_type_list", actionTypeList);
    return messageActionTypeMap;
  }
}
