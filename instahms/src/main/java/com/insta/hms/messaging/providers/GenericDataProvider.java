package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class GenericDataProvider.
 */
public abstract class GenericDataProvider implements MessageDataProvider {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(GenericDataProvider.class);

  /** The criteria filter. */
  protected Map criteriaFilter = new HashMap();

  /** The sec sort col list. */
  List<String> secSortColList = new ArrayList<String>();

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.providers.MessageDataProvider#getMessageDataList(com.insta.hms.
   * messaging.MessageContext)
   */
  public abstract List<Map> getMessageDataList(MessageContext ctx)
      throws SQLException, ParseException;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.providers.MessageDataProvider#addRecipientFilter(java.util.List)
   */
  @Override
  public void addRecipientFilter(List<String> filterIdList) {
    if (logger.isDebugEnabled()) {
      logger.debug("Entering addRecipientFilter : " + filterIdList);
    }
    if (null != filterIdList && filterIdList.size() > 0) {
      criteriaFilter.put("key", filterIdList.toArray(new String[filterIdList.size()]));
      criteriaFilter.put("key@type", new String[] { "text" });
      criteriaFilter.put("key@cast", new String[] { "y" });
    }
  }

  /**
   * Adds the secondary sort.
   *
   * @param sortOrder
   *          the sort order
   */
  public void addSecondarySort(String sortOrder) {
    secSortColList.add(sortOrder);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.providers.MessageDataProvider#addCriteriaFilter(java.util.Map)
   */
  public void addCriteriaFilter(Map filter) {
    criteriaFilter.putAll(filter);
  }
}
