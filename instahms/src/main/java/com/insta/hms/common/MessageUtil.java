package com.insta.hms.common;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * The Class MessageUtil.
 */
@Component
public class MessageUtil implements MessageSourceAware {

  /** The message source. */
  private MessageSource messageSource;

  /**
   * Gets the message.
   *
   * @param key the key
   * @return the message
   */
  public String getMessage(String key) {
    return getMessage(key, null);
  }

  /**
   * Gets the message.
   *
   * @param key    the key
   * @param params the params
   * @return the message
   */
  public String getMessage(String key, Object[] params) {
    try {

      Locale locale = LocaleContextHolder.getLocale();
      String message = messageSource.getMessage(key, params, locale);
      return message;
    } catch (NoSuchMessageException exception) {
      return "Unresolved key: " + key;
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.context.MessageSourceAware#setMessageSource(
   * org.springframework.context.MessageSource)
   */
  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

}
