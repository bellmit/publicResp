package com.insta.hms.messagingframework;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class NotificationMetadata.
 *
 * @author pranaysahota
 */
public class NotificationMetadata {

  /** The total. */
  @JsonProperty(value = "total_count")
  private long total;

  /** The notification metadata. */
  @JsonProperty(value = "notification_metadata")
  private Map<String, Map<String, Map<String, Object>>> notificationMetadata;

  /**
   * Instantiates a new notification metadata.
   */
  public NotificationMetadata() {
    // Don't use this. This is used by redis to identify no notification for the user
  }

  /**
   * Message Log Response Object. *
   *
   * @param total
   *          the total
   * @param notificationDetsMap
   *          the notification dets map
   */
  public NotificationMetadata(long total,
      Map<String, Map<String, Map<String, Object>>> notificationDetsMap) {
    super();
    this.total = total;
    this.notificationMetadata = notificationDetsMap;
  }

  /**
   * Gets the total.
   *
   * @return the total
   */
  public long getTotal() {
    return total;
  }

  /**
   * Gets the notification metadata.
   *
   * @return the notification metadata
   */
  public Map<String, Map<String, Map<String, Object>>> getNotificationMetadata() {
    return notificationMetadata;
  }

}
