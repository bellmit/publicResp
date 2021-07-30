package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class MapDataProvider.
 */
public class MapDataProvider extends GenericDataProvider {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MapDataProvider.class);

  /** The token list. */
  private List<String> tokenList = new ArrayList<String>();

  /* (non-Javadoc)
   * @see com.insta.hms.messaging.providers.MessageDataProvider#getName()
   */
  public String getName() {
    return "Mapper";
  }

  /* (non-Javadoc)
   * @see com.insta.hms.messaging.providers.GenericDataProvider
   * #getMessageDataList(com.insta.hms.messaging.MessageContext)
   */
  @Override
  public List<Map> getMessageDataList(MessageContext thisCtx) throws ParseException, SQLException {
    List<Map> dataList = new ArrayList<Map>();
    if (null != thisCtx) {
      Map eventData = thisCtx.getEventData();
      if (null != eventData) {
        dataList.add(eventData);
        tokenList.addAll(eventData.keySet());
      }
    }
    return dataList;
  }

  /* (non-Javadoc)
   * @see com.insta.hms.messaging.providers.MessageDataProvider#getTokens()
   */
  public List<String> getTokens() throws SQLException {
    return tokenList;
  }
}
