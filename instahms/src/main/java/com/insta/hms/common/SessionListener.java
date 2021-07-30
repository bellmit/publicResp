package com.insta.hms.common;

import com.bob.hms.common.RequestContext;
import com.insta.hms.core.clinical.ipemr.IpEmrFormService;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * The listener interface for receiving session events. The class that is interested in processing a
 * session event implements this interface, and the object created with that class is registered
 * with a component using the component's <code>addSessionListener</code> method. When the session
 * event occurs, that object's appropriate method is invoked.
 *
 * @see SessionEvent
 */
public class SessionListener implements HttpSessionListener {

  /**
   * Invoked when session is created.
   *
   * @param sessionEvent the session event
   */
  @Override
  public void sessionCreated(HttpSessionEvent sessionEvent) {}

  /**
   * Current implementation: releasing section lock for IPEMR when the session
   * expires for the current user.
   *
   * @param sessionEvent the session event
   */
  @Override
  public void sessionDestroyed(HttpSessionEvent sessionEvent) {
    IpEmrFormService ipemrService = ApplicationContextProvider.getBean(IpEmrFormService.class);
    HttpSession session = sessionEvent.getSession();
    RequestContext.setConnectionDetails(
        new String[] {null, "", (String) session.getAttribute("sesHospitalId"), "", "", ""});
    String userName = (String) session.getAttribute("userId");
    ipemrService.deleteSectionLock(userName);
  }

}
