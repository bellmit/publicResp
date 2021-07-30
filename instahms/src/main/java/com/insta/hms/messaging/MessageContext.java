package com.insta.hms.messaging;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class MessageContext.
 */
public class MessageContext implements Map, Cloneable {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MessageContext.class);

  /** The Constant MESSAGE_TYPE_KEY. */
  // Pre-defined keys in input data
  private static final String MESSAGE_TYPE_KEY = "_message_type";
  
  /** The Constant EVENT_DATA_KEY. */
  private static final String EVENT_DATA_KEY = "_event_data";
  
  /** The Constant CONFIG_PARAMS_KEY. */
  private static final String CONFIG_PARAMS_KEY = "_config_params";
  
  /** The Constant DISPLAY_PARAMS_KEY. */
  private static final String DISPLAY_PARAMS_KEY = "_display_params";

  /** The input. */
  // Map that stores all the input parameters
  private Map<String, Object> input = new HashMap<String, Object>();

  /** The base. */
  // Map that stores all the message data. This is the delegate for all custom parameters.
  private Map base = new HashMap();

  /**
   * Instantiates a new message context.
   *
   * @param messageTypeParams the message type params
   * @param eventData the event data
   * @param configParams the config params
   * @param displayParams the display params
   */
  private MessageContext(Map messageTypeParams, Map eventData, Map configParams,
      Map displayParams) {
    setMessageType(messageTypeParams);
    setEventData(eventData);
    setConfigParams(configParams);
    setDisplayParams(displayParams);
  }

  /**
   * Instantiates a new message context.
   *
   * @param messageTypeParams the message type params
   * @param eventData the event data
   * @param configParams the config params
   */
  private MessageContext(Map messageTypeParams, Map eventData, Map configParams) {
    this(messageTypeParams, eventData, configParams, null);
  }

  /**
   * Instantiates a new message context.
   *
   * @param messageTypeParams the message type params
   * @param eventData the event data
   */
  private MessageContext(Map messageTypeParams, Map eventData) {
    this(messageTypeParams, eventData, null, null);
  }

  /**
   * Instantiates a new message context.
   *
   * @param messageTypeParams the message type params
   */
  private MessageContext(Map messageTypeParams) {
    this(messageTypeParams, null, null, null);
  }

  /**
   * Sets the display params.
   *
   * @param displayParams the new display params
   */
  public void setDisplayParams(Map displayParams) {
    if (null != displayParams) {
      input.put(DISPLAY_PARAMS_KEY, displayParams);
    }
  }

  /**
   * Sets the message type.
   *
   * @param msgType the new message type
   */
  public void setMessageType(Map msgType) {
    if (null != msgType) {
      input.put(MESSAGE_TYPE_KEY, msgType);
    }
  }

  /**
   * Sets the event data.
   *
   * @param eventData the new event data
   */
  public void setEventData(Map eventData) {
    if (null != eventData) {
      input.put(EVENT_DATA_KEY, eventData);
    }
  }

  /**
   * Sets the config params.
   *
   * @param configParams the new config params
   */
  public void setConfigParams(Object configParams) {
    if (null != configParams) {
      input.put(CONFIG_PARAMS_KEY, configParams);
    }
  }

  /**
   * Instantiates a new message context.
   */
  // private constructor used for cloning
  private MessageContext() {

  }

  /* (non-Javadoc)
   * @see java.util.Map#clear()
   */
  public void clear() {
    base.clear();
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key) {
    return base.containsKey(key);
  }

  /* (non-Javadoc)
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue(Object value) {
    return base.containsValue(value);
  }

  /* (non-Javadoc)
   * @see java.util.Map#entrySet()
   */
  public Set entrySet() {
    return base.entrySet();
  }

  /* (non-Javadoc)
   * @see java.util.Map#get(java.lang.Object)
   */
  public Object get(Object key) {
    return base.get(key);
  }

  /* (non-Javadoc)
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty() {
    return base.isEmpty();
  }

  /* (non-Javadoc)
   * @see java.util.Map#keySet()
   */
  public Set keySet() {
    return base.keySet();
  }

  /* (non-Javadoc)
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  public Object put(Object key, Object value) {
    return base.put(key, value);
  }

  /* (non-Javadoc)
   * @see java.util.Map#putAll(java.util.Map)
   */
  public void putAll(Map map) {
    base.putAll(map);
  }

  /* (non-Javadoc)
   * @see java.util.Map#remove(java.lang.Object)
   */
  public Object remove(Object key) {
    return base.remove(key);
  }

  /* (non-Javadoc)
   * @see java.util.Map#size()
   */
  public int size() {
    return base.size();
  }

  /* (non-Javadoc)
   * @see java.util.Map#values()
   */
  public Collection values() {
    return base.values();
  }

  /**
   * Gets the message type.
   *
   * @return the message type
   */
  public Map getMessageType() {
    return (Map) input.get(MESSAGE_TYPE_KEY);
  }

  /**
   * Gets the event data.
   *
   * @return the event data
   */
  public Map getEventData() {
    return (Map) input.get(EVENT_DATA_KEY);
  }

  /**
   * Gets the config params.
   *
   * @return the config params
   */
  public Map getConfigParams() {
    return (Map) input.get(CONFIG_PARAMS_KEY);
  }

  /**
   * Gets the display params.
   *
   * @return the display params
   */
  public Map getDisplayParams() {
    return (Map) input.get(DISPLAY_PARAMS_KEY);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() {
    MessageContext copy = new MessageContext();
    copy.input.putAll(this.input);
    copy.base.putAll(this.base);
    return copy;
  }

  /**
   * From type.
   *
   * @param messageTypeMap the message type map
   * @return the message context
   * @throws SQLException the SQL exception
   */
  public static MessageContext fromType(Map messageTypeMap) throws SQLException {
    String messageTypeId = null;
    Map<String, String> configParams = null;
    if (null != messageTypeMap) {
      messageTypeId = (String) messageTypeMap.get("message_type_id");
      GenericDAO dao = new GenericDAO("message_config");
      List<BasicDynaBean> messageParams = dao.findAllByKey("message_type_id", messageTypeId);
      for (BasicDynaBean bean : messageParams) {
        String key = bean.get("param_name").toString();
        String value = null;
        if (bean.get("param_value") != null) {
          value = bean.get("param_value").toString();
        }
        if (null == configParams) {
          configParams = new HashMap<String, String>();
        }
        configParams.put(key, value);
      }
    }
    // Create a message context based on the db values
    MessageContext ctx = new MessageContext(messageTypeMap, null, configParams);
    return ctx;

  }

  /**
   * From type.
   *
   * @param messageTypeId the message type id
   * @return the message context
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static MessageContext fromType(String messageTypeId) throws SQLException, IOException {
    GenericDAO messageTypeDao = new GenericDAO("message_types");
    BasicDynaBean messageType = messageTypeDao.findByKey("message_type_id", messageTypeId);
    Map messageTypeParams = new HashMap();
    if (null != messageType) {
      messageTypeParams.putAll(messageType.getMap());
    }

    // Load Attachments
    GenericDAO attachmentDao = new GenericDAO("message_attachments");
    Map filtermap = new HashMap();
    filtermap.put("message_type_id", messageTypeId);
    List<String> columns = new ArrayList<String>();
    columns.add("attachment_id");
    List<BasicDynaBean> attachBeans = attachmentDao.listAll(columns, filtermap, null);
    if (null != attachBeans) {
      for (int i = 0; i < attachBeans.size(); i++) {
        BasicDynaBean attachBean = attachmentDao.getBean();
        attachmentDao.loadByteaRecords(attachBean, "attachment_id",
            attachBeans.get(i).get("attachment_id"));
        Map attachMap = new HashMap();
        attachMap.putAll(attachBean.getMap());
        attachMap.put("attachment_bytes",
            DataBaseUtil.readInputStream((InputStream) attachBean.get("attachment_bytes")));
        List<Map> attachList = (List<Map>) messageTypeParams.get("message_attachments");
        if (null == attachList) {
          attachList = new ArrayList<Map>();
          messageTypeParams.put("message_attachments", attachList);
        }
        attachList.add(attachMap);
      }
    }

    return fromType(messageTypeParams);

  }
}
