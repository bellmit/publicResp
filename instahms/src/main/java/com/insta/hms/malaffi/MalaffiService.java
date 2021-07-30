package com.insta.hms.malaffi;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.xml.security.utils.Base64;
import org.joda.time.DateTime;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

@Service
public class MalaffiService {

  @LazyAutowired
  private CenterService centerService;

  @LazyAutowired
  private DoctorService doctorService;

  @LazyAutowired
  private MessageUtil messageUtil;

  @LazyAutowired
  private UserService userService;

  private static final char[] PASSWORD = "password".toCharArray();
  private static final String CERTIFICATE_ALIAS_NAME = "selfsigned";
  private static final Logger logger = LoggerFactory.getLogger(MalaffiService.class);
  private static final List<String> REQUIRED_CENTER_ATTRIBUTES = Arrays.asList(
      "hospital_center_service_reg_no");

  private static final List<String> REQUIRED_USER_ATTRIBUTES = Arrays.asList("user_first_name",
      "user_middle_name", "user_last_name", "email_id", "mobile_no", "user_gender", "employee_id",
      "profession", "employee_category", "employee_major");

  private static final String REQUIRED_ERROR_KEY = "exception.single.field.required.placeholder";
  private static final String INCOMPLETE_CONFIG_KEY = "ui.message.malaffi.configuration.incomplete";
  
  private static final String PRIMARY_PROVIDER = "Primary Provider";
  private static final String SECONDARY_PROVIDER = "Secondary Provider";
  private static final String TERTIARY_PROVIDER = "Tertiary Provider";

  @PostConstruct
  public void initializeOpensaml() {
    try {
      InitializationService.initialize();
    } catch (InitializationException initializationException) {
      logger.error("Couldn't initialize OpenSaml", initializationException);
    }
  }

  public String getEndpointUrl() {
    return getSamlConfig().getSsoEndpointUrl();
  }
  public String getXml(String mrNo, List<String> errors) {
    BasicDynaBean user = userService.getLoggedUser();
    Integer centerId = RequestContext.getCenterId();
    BasicDynaBean center = centerService.findByKey(centerId);
    if (!validate(errors, mrNo, user, center)) {
      return null;
    }
    SamlConfig samlConfig = getSamlConfig();
    Map<String, String> attributes = getAttributes(mrNo, user, center, samlConfig);
    Response response = buildSamlResponse(attributes, samlConfig);
    Element xml = signAssertion(samlConfig, response);
    return SamlUtil.stringify(xml);
  }

  private SamlConfig getSamlConfig() {
    Integer centerId = RequestContext.getCenterId();
    Map<String,Object> centerMalaffiConfig = (Map<String, Object>) centerService
        .getReportingMeta(centerId).get("settings_malaffiae");
    SamlConfig samlConfig = new SamlConfig();
    samlConfig.setConfig(centerMalaffiConfig);
    return samlConfig;
  }
  private boolean validateUserFields(List<String> errors, BasicDynaBean user) {
    boolean valid = true;
    for (String field : REQUIRED_USER_ATTRIBUTES) {
      String value = (String) user.get(field);
      if (value == null || value.trim().equals("")) {
        errors.add(messageUtil.getMessage(REQUIRED_ERROR_KEY,
            new String[] { StringUtil.prettyName(field) }));
        valid = false;
      }
    }
    if (userService.getUserMalaffiRole((String) user.get("emp_username")) == null) {
      errors.add(messageUtil.getMessage(REQUIRED_ERROR_KEY,
          new String[] { StringUtil.prettyName("role") }));
      valid = false;
    }
    String username = (String) user.get("emp_username");
    List<Integer> userRoleIds = userService.getUserHospitalRoleIds(username);
    if (userRoleIds == null) {
      errors.add(messageUtil.getMessage(REQUIRED_ERROR_KEY,
          new String[] { StringUtil.prettyName("hospital role") }));
      valid = false;
    } else if (userRoleIds.contains(-1)) { // Doctor
      String doctorId = (String) user.get("doctor_id");
      BasicDynaBean doctor = doctorService.getDoctorById(doctorId);
      if (doctor == null) {
        errors.add(messageUtil.getMessage(REQUIRED_ERROR_KEY,
            new String[] { StringUtil.prettyName("doctor_id") }));
        valid = false;
      } else if (doctor.get("doctor_license_number") == null
          || doctor.get("doctor_license_number").equals("")) {
        errors.add(messageUtil.getMessage(REQUIRED_ERROR_KEY,
            new String[] { StringUtil.prettyName("doctor_license_number") }));
        valid = false;
      }
    }
    if (userService.getUserMalaffiRole(username) == null) {
      errors.add(messageUtil.getMessage(REQUIRED_ERROR_KEY,
          new String[] { StringUtil.prettyName("malaffi_role") }));
      valid = false;
    }
    
    return valid;
  }

  private boolean validate(List<String> errors, String mrNo, BasicDynaBean user,
      BasicDynaBean center) {
    Boolean valid = true;
    if (mrNo == null || mrNo.equals("")) {
      errors.add(messageUtil.getMessage(REQUIRED_ERROR_KEY,
          new String[] { StringUtil.prettyName("mr_no") }));
      valid = false;
    }
    if (center == null) {
      errors.add(messageUtil.getMessage(REQUIRED_ERROR_KEY,
          new String[] { StringUtil.prettyName("center") }));
      valid = false;
    } else {
      for (String field : REQUIRED_CENTER_ATTRIBUTES) {
        String value = (String) center.get(field);
        if (value == null || value.equals("")) {
          errors.add(messageUtil.getMessage(REQUIRED_ERROR_KEY,
              new String[] { StringUtil.prettyName(field) }));
          valid = false;
        }
      }
      SamlConfig samlConfig = getSamlConfig();
      if (!samlConfig.hasCompleteConfiguration()) {
        errors.add(messageUtil.getMessage(INCOMPLETE_CONFIG_KEY));
        valid = false;
      }
    }

    return valid && validateUserFields(errors, user);
  }

  private Map<String, String> getAttributes(String mrNo, BasicDynaBean user, BasicDynaBean center,
      SamlConfig samlConfig) {
    String username = (String) user.get("emp_username");
    List<Integer> userRoleIds = userService.getUserHospitalRoleIds(username);
    String federationId = ((String) center.get("hospital_center_service_reg_no")) + "-"
        + ((String) user.get("employee_id"));
    String userMalaffiRole = userService.getUserMalaffiRole(username);
    if (userMalaffiRole.equals(PRIMARY_PROVIDER) || userMalaffiRole.equals(SECONDARY_PROVIDER)
        || userMalaffiRole.equals(TERTIARY_PROVIDER)) {
      federationId = (String) user.get("employee_id");
    }
    if (userRoleIds.contains(-1)) { // Doctor
      String doctorId = (String) user.get("doctor_id");
      BasicDynaBean doctor = doctorService.getDoctorById(doctorId);
      federationId = (String) doctor.get("doctor_license_number");
    }
    Map<String, String> attributes = new HashMap<>();
    attributes.put("federation_id", federationId);
    attributes.put("urn:oasis:names:tc:xacml:2.0:subject:role", userMalaffiRole);
    attributes.put("urn:malaffi:firstname", (String) user.get("user_first_name"));
    attributes.put("urn:malaffi:lastname", (String) user.get("user_last_name"));
    attributes.put("urn:malaffi:fullname", userService.getUserFullName(user));
    attributes.put("urn:malaffi:emailaddress", (String) user.get("email_id"));
    attributes.put("urn:malaffi:workphone", (String) user.get("mobile_no"));
    attributes.put("urn:malaffi:major", (String) user.get("employee_major"));
    attributes.put("urn:malaffi:profession", (String) user.get("profession"));
    attributes.put("urn:malaffi:category", (String) user.get("employee_category"));
    attributes.put("urn:malaffi:gender", userService.getUserGender(user));
    attributes.put("urn:malaffi:facilitylicense",
        (String) center.get("hospital_center_service_reg_no"));
    attributes.put("urn:malaffi:organization", samlConfig.getSamlResponseOrganization());
    attributes.put("urn:oasis:names:tc:xacml:2.0:resource:resource-id", 
        mrNo + "^^^&" + samlConfig.getSamlResponseOid() + "&ISO");
    return attributes;
  }

  public String getBase64EncodedXml(String mrNo, List<String> errors) {
    String xml = getXml(mrNo, errors);
    if (xml == null) {
      return null;
    }
    return Base64.encode(xml.getBytes());
  }

  private Element signAssertion(SamlConfig samlConfig, Response response) {
    Credential signingCredential = null;
    try {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      InputStream inputStream = new FileInputStream(samlConfig.getKeyStorePath());
      keyStore.load(inputStream, PASSWORD);
      logger.info("Key Store loaded");
      KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore
          .getEntry(CERTIFICATE_ALIAS_NAME, new KeyStore.PasswordProtection(PASSWORD));
      PrivateKey pk = pkEntry.getPrivateKey();
      X509Certificate certificate = (X509Certificate) pkEntry.getCertificate();
      signingCredential = new BasicX509Credential(certificate, pk);
      logger.info("Private Key loaded");
    } catch (Exception exception) {
      logger.error("Failed to Load the KeyStore", exception);
      return null;
    }

    Signature signature = (Signature) SamlUtil.buildXmlObject(Signature.DEFAULT_ELEMENT_NAME);
    signature.setSigningCredential(signingCredential);
    signature.setCanonicalizationAlgorithm(SignatureConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
    signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA);

    Assertion assertion = response.getAssertions().get(0);
    assertion.setSignature(signature);
    try {
      Element plain = SamlUtil.marshallResponse(response);
      Signer.signObject(signature);
      return plain;
    } catch (SignatureException signatureException) {
      logger.error("Couldn't sign SAML", signatureException);
    }
    return null;
  }
  
  private Assertion buildSamlResponseAssertion(Map<String, String> attributes,
      SamlConfig samlConfig) {
    try {
      // Create the NameIdentifier
      NameID nameId = (NameID) SamlUtil.buildXmlObject(NameID.DEFAULT_ELEMENT_NAME);
      nameId.setValue(attributes.get("federation_id"));
      attributes.remove("federation_id");
      nameId.setFormat(NameID.UNSPECIFIED);

      // Create the SubjectConfirmation
      SubjectConfirmationData confirmationMethod = (SubjectConfirmationData) SamlUtil
          .buildXmlObject(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
      DateTime now = new DateTime();
      confirmationMethod.setRecipient(samlConfig.getSamlResponseRecipient());
      confirmationMethod
          .setNotOnOrAfter(now.plusSeconds(samlConfig.getSamlResponseNotOnOrAfterSeconds()));

      SubjectConfirmation subjectConfirmation = (SubjectConfirmation) SamlUtil
          .buildXmlObject(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
      subjectConfirmation.setSubjectConfirmationData(confirmationMethod);
      subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);

      // Create the Subject
      Subject subject = (Subject) SamlUtil.buildXmlObject(Subject.DEFAULT_ELEMENT_NAME);
      subject.setNameID(nameId);
      subject.getSubjectConfirmations().add(subjectConfirmation);

      // Create Authentication Statement
      AuthnStatement authnStatement = (AuthnStatement) SamlUtil
          .buildXmlObject(AuthnStatement.DEFAULT_ELEMENT_NAME);
      authnStatement.setAuthnInstant(now);
      String id = UUID.randomUUID().toString();
      authnStatement.setSessionIndex(id);

      AuthnContext authnContext = (AuthnContext) SamlUtil
          .buildXmlObject(AuthnContext.DEFAULT_ELEMENT_NAME);
      AuthnContextClassRef authnContextClassRef = (AuthnContextClassRef) SamlUtil
          .buildXmlObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
      authnContextClassRef.setAuthnContextClassRef(AuthnContext.UNSPECIFIED_AUTHN_CTX);
      authnContext.setAuthnContextClassRef(authnContextClassRef);
      authnStatement.setAuthnContext(authnContext);

      // Create the attribute statement
      AttributeStatement attrStatement = (AttributeStatement) SamlUtil
          .buildXmlObject(AttributeStatement.DEFAULT_ELEMENT_NAME);
      Set<String> keySet = attributes.keySet();
      for (String key : keySet) {
        Attribute attribute = SamlUtil.buildStringAttribute(key, attributes.get(key));
        attrStatement.getAttributes().add(attribute);
      }

      AudienceRestriction condition = (AudienceRestriction) SamlUtil
          .buildXmlObject(AudienceRestriction.DEFAULT_ELEMENT_NAME);

      Audience audience = (Audience) SamlUtil.buildXmlObject(Audience.DEFAULT_ELEMENT_NAME);
      audience.setAudienceURI(samlConfig.getSamlResponseAudience());
      condition.getAudiences().add(audience);

      Conditions conditions = (Conditions) SamlUtil.buildXmlObject(Conditions.DEFAULT_ELEMENT_NAME);
      conditions.getConditions().add(condition);
      conditions.setNotBefore(now.minusSeconds(samlConfig.getSamlResponseNotBeforeSeconds()));
      conditions.setNotOnOrAfter(now.plusSeconds(samlConfig.getSamlResponseNotOnOrAfterSeconds()));

      // Create Issuer
      Issuer issuer = (Issuer) SamlUtil.buildXmlObject(Issuer.DEFAULT_ELEMENT_NAME);
      issuer.setValue(samlConfig.getSamlResponseIssuer());

      // Create the assertion
      Assertion assertion = (Assertion) SamlUtil.buildXmlObject(Assertion.DEFAULT_ELEMENT_NAME);
      assertion.setIssuer(issuer);
      assertion.setIssueInstant(now);
      assertion.setID(id);
      assertion.setVersion(SAMLVersion.VERSION_20);
      assertion.setSubject(subject);
      assertion.setConditions(conditions);
      assertion.getAttributeStatements().add(attrStatement);
      assertion.getAuthnStatements().add(authnStatement);

      return assertion;
    } catch (Exception e) {
      logger.error("Couldn't generate assertion");
    }
    return null;
  }
  
  private Response buildSamlResponse(Map<String, String> attributes, SamlConfig samlConfig) {
    // Create Response
    Response response = (Response) SamlUtil.buildXmlObject(Response.DEFAULT_ELEMENT_NAME);
    Assertion assertion = buildSamlResponseAssertion(attributes, samlConfig);
    response.getAssertions().add(assertion);
    response.setIssueInstant(assertion.getIssueInstant());
    response.setID(assertion.getID());
    response.setVersion(assertion.getVersion());
    // Create Issuer
    Issuer issuer = (Issuer) SamlUtil.buildXmlObject(Issuer.DEFAULT_ELEMENT_NAME);
    issuer.setValue(assertion.getIssuer().getValue());
    response.setIssuer(issuer);
    // Create Status
    Status status = (Status) SamlUtil.buildXmlObject(Status.DEFAULT_ELEMENT_NAME);
    StatusCode statusCode = (StatusCode) SamlUtil.buildXmlObject(StatusCode.DEFAULT_ELEMENT_NAME);
    statusCode.setValue(StatusCode.SUCCESS);
    status.setStatusCode(statusCode);
    response.setStatus(status);
    return response;
  }

}
