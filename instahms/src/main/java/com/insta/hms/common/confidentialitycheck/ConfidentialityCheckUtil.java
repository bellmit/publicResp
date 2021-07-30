package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.mdm.breaktheglass.UserMrnoAssociationService;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

@Component
public class ConfidentialityCheckUtil {

  private static Logger logger = LoggerFactory.getLogger(ConfidentialityCheckUtil.class);

  @LazyAutowired
  SessionService sessionService;
  @LazyAutowired
  PatientDetailsService patientDetailService;
  @LazyAutowired
  UserMrnoAssociationService userMrnoAssociationService;
  @LazyAutowired
  BillService billService;
  @LazyAutowired
  ConfidentialityValidatorFactory validatorFactory;

  /**
   * Validate query params using the queryValidatorMap constructed in
   * ConfidentialityValidatorFactory. Atleast one query parameter should pass through for the url to
   * be allowed through.
   *
   * @param requestParams
   *          the request params
   * @return the boolean
   */
  public Boolean validateQueryParams(Map<String, String[]> requestParams,
      HttpServletRequest request) {
    Map<String, Set<Class<? extends ConfidentialityInterface>>> queryParamValidatorMap =
        validatorFactory.getQueryParamValidatoryMap();
    requestParams = sanatizeParamMap(requestParams);
    boolean allowed = false; // basically to check that atleast one requestParam is in the
    // queryParamValidatorMap
    if (requestParams.containsKey("mrno")) {
      allowed = true;
      List<String> mrnos = Arrays.asList(requestParams.get("mrno"));
      if (!checkForMrNoConfidentiality(mrnos)) {
        return false;
      }
    }
    for (String queryParam : queryParamValidatorMap.keySet()) {
      if (requestParams.containsKey(queryParam)) {
        allowed = true;
        Class<? extends ConfidentialityInterface> validatorClassName =
            getValidatingClass(queryParam, queryParamValidatorMap, request);
        if (validatorClassName == null) {
          logger.error("No class found which could handle " + queryParam + "for the url :"
              + request.getRequestURL());
          return false;
        }
        ConfidentialityInterface validatorBean = getBeanClassName(validatorClassName);
        if (validatorBean == null) {
          logger.error("No bean with name:" + validatorClassName.getName());
          return false;
        }
        String[] parameter = requestParams.get(queryParam);
        List<String> parameterList = Arrays.asList(parameter);
        List<String> validParameters = new ArrayList<String>();
        for (String param : parameterList) {
          if (validatorBean.isValidParameter(param)) {
            validParameters.add(param);
          }
        }
        if (validParameters.isEmpty()) {
          return true;
        }
        List<String> associatedMrnos = validatorBean.getAssociatedMrNo(validParameters);
        if (associatedMrnos != null && !associatedMrnos.isEmpty()
            && associatedMrnos.get(0).equals("ISR")) {
          return true;
        } else if (associatedMrnos != null && !associatedMrnos.isEmpty()
            && associatedMrnos.get(0).equals("retail")) {
          return true;
        } else if (associatedMrnos != null && !associatedMrnos.isEmpty()
            && associatedMrnos.get(0).equals("APPOINTMENT")) {
          return true;
        } else if (associatedMrnos != null && !associatedMrnos.isEmpty()
            && associatedMrnos.get(0).equals("NEWDOCUPLOAD")) {
          return true;
        } else if (!checkForMrNoConfidentiality(associatedMrnos)) {
          return false;
        }
      }
    }
    return allowed;
  }

  private Class<? extends ConfidentialityInterface> getValidatingClass(String queryParam,
      Map<String, Set<Class<? extends ConfidentialityInterface>>> queryParamValidatorMap,
      HttpServletRequest request) {
    Map<Class<? extends ConfidentialityInterface>, Set<String>> urlValidatorMap =
        validatorFactory.getUrlValidatorMap();
    String servletPath = request.getServletPath();
    Set<Class<? extends ConfidentialityInterface>> setClasses =
        (HashSet<Class<? extends ConfidentialityInterface>>) queryParamValidatorMap
            .get(queryParam);
    if (setClasses.size() == 1) {
      for (Class<? extends ConfidentialityInterface> confidentialityClass : setClasses) {
        if (urlValidatorMap.containsKey(confidentialityClass)) {
          Set<String> handledUrls = urlValidatorMap.get(confidentialityClass);
          for (String url : handledUrls) {
            if (servletPath.contains(url)) {
              return confidentialityClass;
            }
          }
          return null;
        } else {
          return confidentialityClass;
        }
      }
    }
    List<Class<? extends ConfidentialityInterface>> handlingValidatorClasses = new ArrayList<>();
    logger.warn(
        "Found multiple confidentiality handlers for this request: " + request.getRequestURL());
    for (Class<? extends ConfidentialityInterface> confidentialityClass : setClasses) {
      for (String urlContains : urlValidatorMap.get(confidentialityClass)) {
        if (servletPath.contains(urlContains)) {
          logger.warn("Found confidentiality class:" + confidentialityClass + "since it contains:"
              + urlContains);
          handlingValidatorClasses.add(confidentialityClass);
        }
      }
    }
    if (handlingValidatorClasses.size() == 1) {
      return handlingValidatorClasses.get(0);
    } else {
      logger.error("found multiple classes that handle this url:" + request.getRequestURL());
      return null;
    }
  }

  /**
   * Validate rest urls; Extract confidential values from rest url. Uses spring to get the
   * handlermapping and then extracts values on the basis of the handlermapping
   *
   * @param request
   *          the request
   * @return the boolean
   */
  public Boolean validateRestUrls(HttpServletRequest request) {
    Map<String, String> requestEntityValueMap = new HashMap<>();
    Boolean allowed = false;
    // getRequestURI returns URI without decoding it.
    String servletPath = request.getRequestURI().replace(request.getContextPath(), "");
    ConfigurableApplicationContext applicationContext =
        (ConfigurableApplicationContext) ApplicationContextProvider.getApplicationContext();
    Map<String, RequestMappingHandlerMapping> mapRequestHandlerMappingBeans =
        applicationContext.getBeansOfType((RequestMappingHandlerMapping.class));
    RequestMappingHandlerMapping handlerMapping = null;
    for (String name : mapRequestHandlerMappingBeans.keySet()) {
      try {
        handlerMapping = mapRequestHandlerMappingBeans.get(name);
        if (handlerMapping.getOrder() == -1) {
          // our custom requestmappinghandlermapping has order of -1 and does NOT url decode.
          // We have to prevent urldecode because MR number pattern can be defined to have slash in
          // it.
          logger.debug("able to retreive requesthandlermapping bean with name:" + name);
          break;
        }
      } catch (Exception exception) {
        logger.error("Unable to retrieve custom requestHandlerMappingBean");
      }
    }
    HandlerMethod handlerMethod = null;
    try {
      handlerMethod = (HandlerMethod) handlerMapping.getHandler(request).getHandler();
    } catch (NullPointerException exception) {
      logger.warn("Unable to find requestMapping for the requestUrl:" + servletPath);
      return true;
    } catch (Exception exception) {
      logger.error("Exception encountered while trying to get the handlermethod:", exception);
      return false;
    }
    String[] requestMappings = getRequestMappingHandlerMethod(handlerMethod);
    for (String requestMapping : requestMappings) {
      String entityName = null;
      String[] tokensRequestMapping = requestMapping.split("/");
      String[] tokensServletPath = servletPath.split("/");
      int index;
      Pattern pathVariablePattern = Pattern.compile("\\{[a-zA-Z0-9/_%]*\\}");
      for (index = 0; index < tokensRequestMapping.length; index++) {
        String currentToken = tokensRequestMapping[index];
        if (index == 0) {
          entityName = currentToken;
          continue;
        }
        if (pathVariablePattern.matcher(currentToken).matches()) {
          requestEntityValueMap.put(entityName, tokensServletPath[index]);
        } else {
          entityName = currentToken;
        }
      }
    }
    if (requestEntityValueMap.keySet().contains("patient")) {
      String entityValue = requestEntityValueMap.get("patient");
      try {
        entityValue = URLDecoder.decode(entityValue, "UTF-8");
      } catch (UnsupportedEncodingException exception) {
        logger.error("Unable to decode url parameter:" + exception);
        return false;
      }
      List<String> entityValueList = new ArrayList<>();
      entityValueList.add(entityValue);
      allowed = true;
      if (!checkForMrNoConfidentiality(entityValueList)) {
        return false;
      }
    }
    Map<String, Class<? extends ConfidentialityInterface>> entityValidatorMap =
        validatorFactory.getEntityValidatorMap();
    for (String requestEntityName : requestEntityValueMap.keySet()) {
      if (entityValidatorMap.containsKey(requestEntityName)) {
        allowed = true;
        Class<? extends ConfidentialityInterface> validatorClassName =
            entityValidatorMap.get(requestEntityName);
        ConfidentialityInterface validatorBean = getBeanClassName(validatorClassName);
        if (validatorBean == null) {
          logger.error("No bean with name:" + validatorClassName.getName());
          return false;
        }
        String requestEntityValue = requestEntityValueMap.get(requestEntityName);
        try {
          requestEntityValue = URLDecoder.decode(requestEntityValue, "UTF-8");
        } catch (UnsupportedEncodingException exception) {
          logger.error("Unable to decode url parameter:" + exception);
          return false;
        }
        List<String> associatedMrnos =
            validatorBean.getAssociatedMrNo(Arrays.asList(new String[] { requestEntityValue }));
        if (!checkForMrNoConfidentiality(associatedMrnos)) {
          return false;
        }
      }
    }
    return allowed;
  }

  private String[] getRequestMappingHandlerMethod(HandlerMethod method) {
    Class<?> handlingClass = method.getBean().getClass();
    RequestMapping classRequestMappingAnnotation =
        AnnotationUtils.findAnnotation(handlingClass, RequestMapping.class);
    String[] classRequestMappings = null;
    if (classRequestMappingAnnotation != null) {
      classRequestMappings = classRequestMappingAnnotation.value();
    }
    RequestMapping methodRequestMappingAnnotation = null;
    if (method.hasMethodAnnotation(RequestMapping.class)) {
      methodRequestMappingAnnotation = method.getMethodAnnotation(RequestMapping.class);
    } else if (method.hasMethodAnnotation(GetMapping.class)) {
      GetMapping getMapping = method.getMethodAnnotation(GetMapping.class);
      String[] mappings = getMapping.value();
      methodRequestMappingAnnotation = synthesizeRequestMappingAnnotation(mappings);
    } else if (method.hasMethodAnnotation(DeleteMapping.class)) {
      DeleteMapping deleteMapping = method.getMethodAnnotation(DeleteMapping.class);
      String[] mappings = deleteMapping.value();
      methodRequestMappingAnnotation = synthesizeRequestMappingAnnotation(mappings);
    } else if (method.hasMethodAnnotation(PostMapping.class)) {
      PostMapping postMapping = method.getMethodAnnotation(PostMapping.class);
      String[] mappings = postMapping.value();
      methodRequestMappingAnnotation = synthesizeRequestMappingAnnotation(mappings);
    } else if (method.hasMethodAnnotation(PutMapping.class)) {
      PutMapping putMapping = method.getMethodAnnotation(PutMapping.class);
      String[] mappings = putMapping.value();
      methodRequestMappingAnnotation = synthesizeRequestMappingAnnotation(mappings);
    } else {
      logger.error("Unable to find request mapping for:" + method.toString());
      return null;
    }
    List<String> requestMappings = new ArrayList<>();
    if (methodRequestMappingAnnotation != null) {
      String[] methodRequestMappings = methodRequestMappingAnnotation.value();
      for (String classRequestMapping : classRequestMappings) {
        if (methodRequestMappings.length == 0) {
          requestMappings.add("/" + classRequestMapping);
          return requestMappings.toArray(new String[requestMappings.size()]);
        }
        for (String methodRequestMapping : methodRequestMappings) {
          if (StringUtil.isNullOrEmpty(methodRequestMapping)) {
            requestMappings.add(classRequestMapping);
            continue;
          }
          if (!methodRequestMapping.startsWith("/")) {
            methodRequestMapping = "/" + methodRequestMapping;
          }
          requestMappings.add(classRequestMapping + methodRequestMapping);
        }
      }
    }
    return requestMappings.toArray(new String[requestMappings.size()]);
  }

  private Boolean checkForMrNoConfidentiality(List<String> mrno) {
    if (mrno == null || mrno.isEmpty()) {
      return false;
    }
    logger.debug("Checking confidentiality for mrnos:" + ArrayUtils.toString(mrno));
    Set<String> setMrnos = new HashSet<>(mrno);
    List<Integer> userGroups = getUserGroupsFromSession();
    List<String> validMrnos = new ArrayList<>();
    for (String mrNumber : setMrnos) {
      if (patientDetailService.isMrNumberValid(mrNumber)) {
        validMrnos.add(mrNumber);
      }
    }
    if (validMrnos.isEmpty()) {
      return true;
    }
    Integer lengthAccessibleMrNo = patientDetailService.checkMrNoConfidentiality(validMrnos,
        userGroups, getUserNameFromSession());
    Boolean isAccessAllowed = compareArrayLengths(validMrnos, lengthAccessibleMrNo);
    if (!isAccessAllowed) {
      logger.warn(
          "Access Blocked for unauthorised confidential mrno" + ArrayUtils.toString(validMrnos));
    }
    return isAccessAllowed;

  }

  @SuppressWarnings("unchecked")
  private List<Integer> getUserGroupsFromSession() {
    Map<String, Object> sessionAttribs =
        sessionService.getSessionAttributes(new String[] { "user_accessible_patient_groups" });
    return (ArrayList<Integer>) sessionAttribs.get("user_accessible_patient_groups");
  }

  private String getUserNameFromSession() {
    Map<String, Object> sessionAttribs =
        sessionService.getSessionAttributes(new String[] { "userId" });
    return (String) sessionAttribs.get("userId");
  }

  private Boolean compareArrayLengths(List<String> mrno, Integer lengthAccessibleMrNo) {
    logger.debug(
        "Number of requested mrnos:" + mrno + " Number of accessibleMrno" + lengthAccessibleMrNo);
    if (lengthAccessibleMrNo != 0 && lengthAccessibleMrNo == mrno.size()) {
      return true;
    }
    return false;
  }

  private RequestMapping synthesizeRequestMappingAnnotation(String[] mapping) {
    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("value", mapping);
    return AnnotationUtils.synthesizeAnnotation(valueMap, RequestMapping.class, null);
  }

  /**
   * Sanatize keys in request parameter map by making all keys lowercase and removing all
   * underscores.
   *
   * @param requestParams
   *          the request params
   * @return the map
   */
  private Map<String, String[]> sanatizeParamMap(Map<String, String[]> requestParams) {
    Map<String, String[]> sanatizedParamMap = new HashMap<>();
    for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
      String key = entry.getKey();
      if (key.startsWith("_")) {
        continue;
      }
      key = key.toLowerCase();
      key = key.replaceAll("_", "");
      if (sanatizedParamMap.containsKey(key)) {
        logger.warn("Duplicate key:" + key);
      }
      List<String> valuesList = new ArrayList<>();
      String[] values = entry.getValue();
      if (values != null && values.length > 0) {
        for (String thisValue : Arrays.asList(values)) {
          if (thisValue != null && !thisValue.isEmpty() && !thisValue.equals("0")) {
            valuesList.add(thisValue);
          }
        }
      }
      if (valuesList.isEmpty()) {
        continue;
      }
      sanatizedParamMap.put(key, valuesList.toArray(new String[0]));
    }
    return sanatizedParamMap;
  }

  private ConfidentialityInterface getBeanClassName(
      Class<? extends ConfidentialityInterface> className) {
    ConfigurableApplicationContext applicationContext =
        (ConfigurableApplicationContext) ApplicationContextProvider.getApplicationContext();
    Map<String, ? extends ConfidentialityInterface> beans =
        applicationContext.getBeansOfType(className);
    ConfidentialityInterface validatorBean = null;
    for (String key : beans.keySet()) {
      if (beans.get(key).getClass().getName().equals(className.getName())) {
        validatorBean = (ConfidentialityInterface) beans.get(key);
        break;
      }
    }
    return validatorBean;
  }

  /**
   * Allow auditlog url to pass through if it doesn't contain any confidential information. If an
   * auditlog url contains a confidential param such as a bill number or mr numebr then it must go
   * through the check for queryparam otherwise the request should be allowed to pass through.
   *
   * @param httpRequest
   *          the request
   * @return true, if successful
   */
  public boolean validateAuditLogUrls(HttpServletRequest httpRequest) {
    Map<String, String[]> queryParams = httpRequest.getParameterMap();
    Set<String> confidentialQueryParams = new HashSet<>();
    confidentialQueryParams.addAll(validatorFactory.getQueryParamValidatoryMap().keySet());
    queryParams = sanatizeParamMap(queryParams);
    Set<String> requestParameters = queryParams.keySet();
    confidentialQueryParams.retainAll(requestParameters);// get intersection of both sets
    if (confidentialQueryParams.isEmpty()) {
      logger.warn("allowing auditlog url through");
      return true;
    }
    return false;
  }
}
