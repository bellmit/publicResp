/**
 *
 */
package com.insta.hms.sso;

import com.bob.hms.common.LoginHelper;
import com.insta.hms.sso.generated.AuthenticationException;
import com.insta.hms.sso.generated.AuthenticationException_Exception;
import com.insta.hms.sso.generated.InternalException;
import com.insta.hms.sso.generated.InternalException_Exception;
import com.insta.hms.sso.generated.InvalidSessionException;
import com.insta.hms.sso.generated.InvalidSessionException_Exception;
import com.insta.hms.sso.generated.LoginInput;
import com.insta.hms.sso.generated.LoginOutput;
import com.insta.hms.sso.generated.LogoutInput;
import com.insta.hms.sso.generated.LogoutOutput;
import com.insta.hms.sso.generated.SSOServicePortType;
import com.insta.hms.sso.generated.ValidationException;
import com.insta.hms.sso.generated.ValidationException_Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
/**
 * @author root
 *
 */
@WebService (endpointInterface="com.insta.hms.sso.generated.SSOServicePortType")
public class SSOServicePortImpl implements SSOServicePortType {

    static Logger logger = LoggerFactory.getLogger(SSOServicePortImpl.class);

    @Resource
	WebServiceContext serviceContext;
	/* (non-Javadoc)
	 * @see com.insta.hms.sso.generated.SSOServicePortType#login(com.insta.hms.sso.generated.LoginInput)
	 */
	@Override
	public LoginOutput login(LoginInput login)
			throws AuthenticationException_Exception,
			InternalException_Exception, ValidationException_Exception {

		MessageContext mc = serviceContext.getMessageContext();
		String errMessage = null;
		if (null == login) {
			ValidationException validationFault = new ValidationException();
			validationFault.setMessage("Invalid Input Parameter");
			logger.error("Invalid Input Parameter : login is null");
			throw new ValidationException_Exception("Invalid Input Parameter", validationFault);
		}
		String userName = login.getUserName();
		String realm = login.getRealmName();
		String pwd = login.getPassWord();
		ValidationException validationFault = new ValidationException();

		if (null == userName || userName.isEmpty()) {
			errMessage = "User Name cannot be null / empty";
			logger.error(errMessage);
			validationFault.setMessage(errMessage);
			throw new ValidationException_Exception(errMessage, validationFault);
		}

		if (null == realm || realm.isEmpty()) {
			errMessage = "Realm Name cannot be null / empty";
			logger.error(errMessage);
			validationFault.setMessage(errMessage);
			throw new ValidationException_Exception(errMessage, validationFault);
		}

		if (null == userName || userName.isEmpty()) {
			errMessage = "Password cannot be null / empty";
			logger.error(errMessage);
			validationFault.setMessage(errMessage);
			throw new ValidationException_Exception(errMessage, validationFault);
		}

	    HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST);
	    HttpServletResponse resp = (HttpServletResponse)mc.get(MessageContext.SERVLET_RESPONSE);
	    HttpSession session = req.getSession(true);
		session.setAttribute("sesHospitalId", realm); // This needs to be set before getting a connection
		String msg = null;

		try {
			if (!LoginHelper.login(realm, userName, req, resp) ) {
				// check if the hospital exists
				String login_status = (String) session.getAttribute("sesErr");
				if (null != login_status && !login_status.isEmpty()) {
					req.setAttribute("login_status", login_status);
					msg = login_status;
					logger.debug("login failed for user " + userName + "@" + realm
							+ "with status :" + login_status);
				} else {
					// no match in the db.
					msg = "Invalid User Name or Password";
					req.setAttribute("login_status", msg);
					logger.debug("login failed for user " + userName + "@" + realm
							+ "with status :" + msg);
				}
				AuthenticationException authFault = new AuthenticationException();
				authFault.setMessage("Authentication Failed:" + msg);
				throw new AuthenticationException_Exception(msg, authFault);
			}
		} catch (SQLException sqle) {
			logger.error("SQL Exception while logging in :" + "SQLState=" +
					sqle.getSQLState() + sqle.getMessage());
			InternalException internalFault = new InternalException();
			internalFault.setMessage(sqle.getMessage());
			throw new InternalException_Exception(sqle.getMessage(), internalFault);
		} catch (IOException ioe) {
			logger.error("IO Exception while logging in :" + ioe.getMessage());
			InternalException internalFault = new InternalException();
			internalFault.setMessage(ioe.getMessage());
			throw new InternalException_Exception(ioe.getMessage(), internalFault);
		}
		SSOHelper.registerLoginAction(session, userName);
	    String sessionId = session.getId();
	    // TODO : need to change to calendar and direct JAXB mapping - once we can get xsd mapping to work
	    Date validTo = new Date();

	    XMLGregorianCalendar sessionValidTo = XMLGregorianCalendarConverter.asXMLGregorianCalendar(validTo);

	    LoginOutput output = new LoginOutput();
	    output.setSessionId(sessionId);
	    output.setValidTo(sessionValidTo);
		logger.debug("login response " + output.toString());
	    return output;
	}

	/* (non-Javadoc)
	 * @see com.insta.hms.sso.generated.SSOServicePortType#logout(com.insta.hms.sso.generated.LogoutInput)
	 */
	@Override
	public LogoutOutput logout(LogoutInput logout)
			throws InternalException_Exception,
			InvalidSessionException_Exception, ValidationException_Exception {

		MessageContext mc = serviceContext.getMessageContext();
		HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST);

		String authKey = logout.getSessionId();
		if (authKey == null) {
			logger.error("Missing authKey while logging out:" + authKey);
			ValidationException sessionFault = new ValidationException();
			sessionFault.setMessage("Invalid Session Key: null");
			throw new ValidationException_Exception("Invalid Session Key : null", sessionFault);
		}
		Date loginTime = SSOHelper.registerLogoutAction(req.getSession(), authKey);

		if (null == loginTime) {
			logger.error("Could not match the authKey :" + authKey);
			InvalidSessionException sessionFault = new InvalidSessionException();
			sessionFault.setMessage("Invalid Session : Session not in registry");
			throw new InvalidSessionException_Exception("Invalid Session : Session not in registry", sessionFault);
		}

		LogoutOutput output = new LogoutOutput();
		XMLGregorianCalendar gcLoginTime = XMLGregorianCalendarConverter.asXMLGregorianCalendar(loginTime);
		output.setTimestampOfLastLogin(gcLoginTime);
		logger.debug("logout response " + output.toString());
		return output;
	}
}
