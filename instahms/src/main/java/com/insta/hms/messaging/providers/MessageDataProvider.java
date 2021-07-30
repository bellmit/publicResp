package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageContext;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface MessageDataProvider {

  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException;

  public List<String> getTokens() throws SQLException;

  public String getName();

  public void addRecipientFilter(List<String> recipientId);

  public void addCriteriaFilter(Map filter);

}
