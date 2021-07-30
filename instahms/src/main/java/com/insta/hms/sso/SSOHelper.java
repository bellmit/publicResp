package com.insta.hms.sso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SSOHelper {

	private static final String AUTHENTICATION_MAP_KEY = "sso_authentication_map";
	public static final String SESSIONID_PARAMETER_NAME = "sessionKey";
    static Logger logger = LoggerFactory.getLogger(SSOHelper.class);

	public static void registerLoginAction(HttpSession localSession, String userName) {
		if (null == localSession || null == userName) {
			logger.error("Invalid input parameters : localSession=" + localSession + " userName=" + userName);
			return;
		}
		ServletContext context = localSession.getServletContext();
		Map<String, AuthenticationInfo> authenticationMap = getAuthenticationMap(context);
		if (!authenticationMap.containsKey(localSession.getId())) {
			logger.debug("adding session to registry for user " + userName + ":" + localSession.getId());
			AuthenticationInfo authInfo = new AuthenticationInfo(localSession.getId(), userName);
			authInfo.localSession = localSession;
			authenticationMap.put(localSession.getId(), authInfo);
		} else {
			logger.warn("session already in registry for user" + userName + ":" + localSession.getId());
		}
	}

	private static Map<String, AuthenticationInfo> getAuthenticationMap(ServletContext context) {
		if (null == context) {
			logger.error("Invalid input parameters : context=" + context);
			return null;
		}
		Map<String, AuthenticationInfo> map = (Map<String, AuthenticationInfo>)(context.getAttribute(AUTHENTICATION_MAP_KEY));
		if (null == map) {
			logger.debug("creating the registry for sso sessions");
			map = new Hashtable<String, AuthenticationInfo>();
			context.setAttribute(AUTHENTICATION_MAP_KEY, map);
		}
		return map;
	}

	public static Date registerLogoutAction(HttpSession session, String authKey) {
		if (null == session || null == authKey) {
			logger.error("Invalid input parameters : localSession=" + session + " authKey=" + authKey);
			return null;
		}

		ServletContext sc = session.getServletContext();
		Map<String, AuthenticationInfo> authenticationMap = getAuthenticationMap(sc);
		if (authenticationMap.containsKey(authKey)) {
			AuthenticationInfo authInfo = authenticationMap.remove(authKey);
			if (null != authInfo.localSession) {
				logger.debug("Invalidating the local session on logout");
				authInfo.localSession.invalidate();
			}
			logger.debug("Last login time : " + authInfo.loginTime);
			return authInfo.loginTime;
		} else {
			logger.debug("session not found in registry @ logout : " + authKey);
			return null;
		}
	}

	public static boolean isSSOAuthenticated(HttpServletRequest req) {
		if (null == req) {
			logger.error("Invalid input parameters : request=" + req);
			return false;
		}
		HttpSession session = req.getSession();
		String authKey = req.getParameter(SSOHelper.SESSIONID_PARAMETER_NAME);
		ServletContext sc = session.getServletContext();

		if (null == authKey) {
			logger.debug("No sso session indicator in request");
			return false;
		}

		if ((null != authKey)) {
			AuthenticationInfo authInfo = getAuthenticationMap(sc).get(authKey);

			if (null == authInfo) {
				logger.debug("session not found in registry : " + authKey);
				return false;
			}
			if (null == authInfo.localSession) {
				authInfo.localSession = session;
			} else {
				if (authInfo.localSession.getId().equals(authInfo.authKey) &&
						!authInfo.localSession.getId().equals(session.getId())) {
					logger.debug("registering a local session " + session.getId()
							+ " for auth session" + authInfo.localSession.getId());
					copySessionAttributes(session, authInfo.localSession);
					authInfo.localSession = session;
				}

			}
		}
		return true;
	}

	private static boolean copySessionAttributes(HttpSession toSession, HttpSession fromSession) {
		if (null == toSession || null == fromSession) {
			return false;
		}
		Enumeration attribNames = fromSession.getAttributeNames();
		while (attribNames.hasMoreElements()) {
			String attribName = (String)attribNames.nextElement();
			toSession.setAttribute(attribName, fromSession.getAttribute(attribName));
		}
		return true;
	}

	private static class AuthenticationInfo {
		public String authKey = null;
		public String userName = null;
		public Date loginTime = null;
		public HttpSession localSession = null;

		public AuthenticationInfo(String authKey, String userName) {
			this.authKey = authKey;
			this.userName = userName;
			this.loginTime = new Date();
			this.localSession = null;
		}
	}
}
