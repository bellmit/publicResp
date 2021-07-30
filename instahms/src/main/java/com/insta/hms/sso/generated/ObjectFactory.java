
package com.insta.hms.sso.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.insta.hms.sso.generated package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Logout_QNAME = new QName("http://www.instahealth.com/sso/", "logout");
    private final static QName _InvalidSessionException_QNAME = new QName("http://www.instahealth.com/sso/", "InvalidSessionException");
    private final static QName _LoginResponse_QNAME = new QName("http://www.instahealth.com/sso/", "loginResponse");
    private final static QName _ValidationException_QNAME = new QName("http://www.instahealth.com/sso/", "ValidationException");
    private final static QName _AuthenticationException_QNAME = new QName("http://www.instahealth.com/sso/", "AuthenticationException");
    private final static QName _Login_QNAME = new QName("http://www.instahealth.com/sso/", "login");
    private final static QName _InternalException_QNAME = new QName("http://www.instahealth.com/sso/", "InternalException");
    private final static QName _LogoutResponse_QNAME = new QName("http://www.instahealth.com/sso/", "logoutResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.insta.hms.sso.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link InternalException }
     * 
     */
    public InternalException createInternalException() {
        return new InternalException();
    }

    /**
     * Create an instance of {@link ServiceException }
     * 
     */
    public ServiceException createServiceException() {
        return new ServiceException();
    }

    /**
     * Create an instance of {@link LogoutInput }
     * 
     */
    public LogoutInput createLogoutInput() {
        return new LogoutInput();
    }

    /**
     * Create an instance of {@link LoginInput }
     * 
     */
    public LoginInput createLoginInput() {
        return new LoginInput();
    }

    /**
     * Create an instance of {@link AuthenticationException }
     * 
     */
    public AuthenticationException createAuthenticationException() {
        return new AuthenticationException();
    }

    /**
     * Create an instance of {@link LogoutOutput }
     * 
     */
    public LogoutOutput createLogoutOutput() {
        return new LogoutOutput();
    }

    /**
     * Create an instance of {@link InvalidSessionException }
     * 
     */
    public InvalidSessionException createInvalidSessionException() {
        return new InvalidSessionException();
    }

    /**
     * Create an instance of {@link ValidationException }
     * 
     */
    public ValidationException createValidationException() {
        return new ValidationException();
    }

    /**
     * Create an instance of {@link LoginOutput }
     * 
     */
    public LoginOutput createLoginOutput() {
        return new LoginOutput();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LogoutInput }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.instahealth.com/sso/", name = "logout")
    public JAXBElement<LogoutInput> createLogout(LogoutInput value) {
        return new JAXBElement<LogoutInput>(_Logout_QNAME, LogoutInput.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InvalidSessionException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.instahealth.com/sso/", name = "InvalidSessionException")
    public JAXBElement<InvalidSessionException> createInvalidSessionException(InvalidSessionException value) {
        return new JAXBElement<InvalidSessionException>(_InvalidSessionException_QNAME, InvalidSessionException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoginOutput }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.instahealth.com/sso/", name = "loginResponse")
    public JAXBElement<LoginOutput> createLoginResponse(LoginOutput value) {
        return new JAXBElement<LoginOutput>(_LoginResponse_QNAME, LoginOutput.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidationException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.instahealth.com/sso/", name = "ValidationException")
    public JAXBElement<ValidationException> createValidationException(ValidationException value) {
        return new JAXBElement<ValidationException>(_ValidationException_QNAME, ValidationException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AuthenticationException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.instahealth.com/sso/", name = "AuthenticationException")
    public JAXBElement<AuthenticationException> createAuthenticationException(AuthenticationException value) {
        return new JAXBElement<AuthenticationException>(_AuthenticationException_QNAME, AuthenticationException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoginInput }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.instahealth.com/sso/", name = "login")
    public JAXBElement<LoginInput> createLogin(LoginInput value) {
        return new JAXBElement<LoginInput>(_Login_QNAME, LoginInput.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InternalException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.instahealth.com/sso/", name = "InternalException")
    public JAXBElement<InternalException> createInternalException(InternalException value) {
        return new JAXBElement<InternalException>(_InternalException_QNAME, InternalException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LogoutOutput }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.instahealth.com/sso/", name = "logoutResponse")
    public JAXBElement<LogoutOutput> createLogoutResponse(LogoutOutput value) {
        return new JAXBElement<LogoutOutput>(_LogoutResponse_QNAME, LogoutOutput.class, null, value);
    }

}
