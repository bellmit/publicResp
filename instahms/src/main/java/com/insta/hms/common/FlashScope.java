package com.insta.hms.common;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * A FlashScope is an object that can be used to store objects and make them available as request
 * parameters during this request cycle and the next one. It is extremely useful when implementing
 * the redirect-after-post pattern in which an ActionBean receives a POST, does some processing and
 * then redirects to a JSP to display the outcome. FlashScopes make temporary use of session to
 * store themselves briefly between two requests.
 * To make values available to the subsequent request a parameter must be included in the redirect
 * URL parameter that identifies the flash scope to use (this avoids collisions where two concurrent
 * requests in the same session might otherwise cause problems for one another).
 * ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
 * FlashScope flash = FlashScope.getScope(request);
 * if (flash != null) {
 *   flash.put("Error", "My message");
 *   redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
 * }
 * return redirect;
 * To ensure that orphaned FlashScopes do not consume increasing amounts of HttpSession memory, the
 * FlashFilter, after each request, checks to see if any FlashScopes have recently expired. A
 * FlashScope is expired when the length of time from the end of the request that created the
 * FlashScope is greater than the timeout set on the FlashScope. The default timeout is 120 seconds
 * (or two minutes), and can be varied by calling setTimeout(int). Since the timer starts when a
 * request completes, and FlashScopes are only meant to live from the time of scope creation to the
 * beginning of a subsequent request this value is set quite low.
 *
 */
@SuppressWarnings("serial")
public class FlashScope extends HashMap<String, Object> implements Serializable {

  /** The default timeout for a flash scope. */
  public static final int DEFAULT_TIMEOUT_IN_SECONDS = 120;

  /** The Constant FLASH_SCOPE_CONTEXT. */
  public static final String FLASH_SCOPE_CONTEXT = "flash-scope-context";

  /** The Constant FLASH_SCOPE. */
  public static final String FLASH_SCOPE = "flash-scope";

  /** The Constant FLASH_KEY. */
  public static final String FLASH_KEY = "prgkey";

  /** The Constant random. */
  private static final SecureRandom random = new SecureRandom();

  /** The start time. */
  private long startTime;

  /** The timeout. */
  private int timeout = DEFAULT_TIMEOUT_IN_SECONDS;

  /** The key. */
  private String key;

  /** The log. */
  private static Logger log = LoggerFactory.getLogger(FlashScope.class);

  /**
   * Protected constructor to prevent random creation of FlashScopes. Uses the request to generate a
   * key under which the flash scope will be stored, and can be identified by later.
   *
   * @param key the key by which this flash scope can be looked up in the map
   */
  protected FlashScope(String key) {
    this.key = key;
  }

  /**
   * Returns the timeout in seconds after which the flash scope will be discarded.
   *
   * @return the timeout
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * Sets the timeout in seconds after which the flash scope will be discarded.
   *
   * @param timeout the new timeout
   */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  /**
   * Returns the key used to store this flash scope in the collection of flash scopes.
   *
   * @return the string
   */
  public String key() {
    return key;
  }

  /**
   * Get a flash scope for the given request. If a scope is already associated with the request it
   * will be returned else a new one will be created and returned. Null would be returned if there
   * is no session associated with the request or the session has been invalidated.
   * It is assumed that the request object will be used by only one thread so access to the request
   * is not synchronized. Access to the scopes map that is stored in the session and the static that
   * is used to generate the keys for the map is synchronized.
   *
   * @param req the current request
   * @return the FlashScope
   */
  public static FlashScope getScope(HttpServletRequest req) {
    Map<String, FlashScope> scopes = getContainer(req);
    FlashScope scope = (FlashScope) req.getAttribute(FLASH_SCOPE);
    if (scope == null) {
      String key = "";
      synchronized (random) {
        do {
          byte[] result = new byte[32];
          random.nextBytes(result);
          key = Hex.encodeHexString(result);
        } while (key.isEmpty() || scopes.containsKey(key));
        scope = new FlashScope(key);
        scope.startTime = System.currentTimeMillis();
        scopes.put(scope.key(), scope);
      }
      req.setAttribute(FLASH_SCOPE, scope);
    }

    return scope;
  }

  /**
   * Get the flash scope identified by the prg key.
   * NOTE: calling this method has the side-affect of removing the flash scope from the set of
   * managed flash scopes!
   *
   * @param req    the req
   * @param prgkey the prgkey
   * @return the scope
   */
  public static FlashScope getScope(HttpServletRequest req, String prgkey) {
    Map<String, FlashScope> scopes = getContainer(req);
    return (scopes != null) ? scopes.remove(prgkey) : null;
  }

  /**
   * Copies all the attributes from the flash scope to the given request.
   *
   * @param request the request
   */
  public void copyToRequest(HttpServletRequest request) {
    for (Map.Entry<String, Object> entry : entrySet()) {
      Object value = entry.getValue();
      request.setAttribute(entry.getKey(), value);
    }
  }

  /**
   * Cleanup.
   *
   * @param request the request
   */
  public static void cleanup(HttpServletRequest request) {
    Map<String, FlashScope> scopes = getContainer(request);
    if (scopes != null && !scopes.isEmpty()) {
      Iterator<FlashScope> iterator = scopes.values().iterator();
      while (iterator.hasNext()) {
        if (iterator.next().isExpired()) {
          iterator.remove();
        }
      }
    }
  }

  /**
   * Age.
   *
   * @return the long
   */
  private long age() {
    if (startTime == 0) {
      return 0;
    } else {
      return (System.currentTimeMillis() - this.startTime) / 1000;
    }
  }

  /**
   * Returns true if the flash scope has expired and should be dereferenced to allow garbage
   * collection. Returns false if the flash scope should be retained.
   *
   * @return true if the flash scope has expired, false otherwise
   */
  public boolean isExpired() {
    return age() > this.timeout;
  }

  /**
   * Stores the provided value both in the flash scope a under the specified name, and in a request
   * attribute with the specified name. Allows flash scope attributes to be accessed seamlessly as
   * request attributes during both the current request and the subsequent request.
   *
   * @param name  the name of the attribute to add to flash scope
   * @param value the value to be added
   * @return the previous object stored with the same name (possibly null)
   */
  @Override
  public Object put(String name, Object value) {
    return super.put(name, value);
  }

  /**
   * Gets the collection of all flash scopes present in the current session.
   *
   * @param req the current request, needed to get access to the session
   * @return a collection of flash scopes. Will return an empty collection if there are no flash
   *         scopes present.
   */
  public static Collection<FlashScope> getAllFlashScopes(HttpServletRequest req) {
    Map<String, FlashScope> scopes = getContainer(req);

    if (scopes == null) {
      return Collections.emptySet();
    } else {
      return scopes.values();
    }
  }

  /**
   * Flash a information message to be displayed to the user.
   *
   * @param message the message
   * @return the object
   */
  public Object info(String message) {
    return super.put("info", message);
  }

  /**
   * Flash a success message to be displayed to the user.
   *
   * @param message the message
   * @return the object
   */
  public Object success(String message) {
    return super.put("success", message);
  }

  /**
   * Flash a warning message to be displayed to the user.
   *
   * @param message the message
   * @return the object
   */
  public Object warning(String message) {
    return super.put("warning", message);
  }

  /**
   * Flash a error message to be displayed to the user.
   *
   * @param message the message
   * @return the object
   */
  public Object error(String message) {
    return super.put("error", message);
  }

  /**
   * Internal helper method to retrieve the scopes container for all the flash scopes in the current
   * session. Will return null if the session for the request is not available. Will also return
   * null if the current session has been invalidated.
   *
   * @param req the current request
   * @return a Map of integer keys to FlashScope objects
   */
  private static Map<String, FlashScope> getContainer(HttpServletRequest req) {
    try {
      HttpSession session = req.getSession(false);
      Map<String, FlashScope> scopes = null;
      if (session != null) {
        scopes = getContainer(session);

        if (scopes == null) {
          synchronized (FlashScope.class) {
            // after obtaining a lock, try looking it up again
            scopes = getContainer(session);

            // if still not there, then create and save it
            if (scopes == null) {
              scopes = new ConcurrentHashMap<>();
              session.setAttribute(FLASH_SCOPE_CONTEXT, scopes);
            }
          }
        }
      }

      return scopes;
    } catch (IllegalStateException ise) {
      // If the session has been invalidated we'll get this exception
      log.warn("An IllegalStateException got thrown trying to create a flash scope.", ise);
      return null;
    }
  }

  /**
   * Internal helper method to retrieve the container for all the flash scopes. Will return null if
   * the container does not exist.
   *
   * @param session the session
   * @return a Map of integer keys to FlashScope objects
   * @throws IllegalStateException if the session has been invalidated
   */
  @SuppressWarnings("unchecked")
  private static Map<String, FlashScope> getContainer(HttpSession session)
      throws IllegalStateException {
    return (Map<String, FlashScope>) session.getAttribute(FLASH_SCOPE_CONTEXT);
  }
}
