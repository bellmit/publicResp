package com.insta.hms.common;

import com.insta.hms.exception.AccessDeniedException;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.InvalidFileFormatException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.jobs.ex.ExpressionValidationException;
import com.insta.hms.master.URLRoute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class GenericControllerAdvice.
 */
@ControllerAdvice
public class GenericControllerAdvice {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(GenericControllerAdvice.class);

  /** The locale. */
  Locale locale;

  /** The exception message source. */
  @Autowired
  private MessageSource exceptionMessageSource;

  /**
   * Handle access denied exception.
   *
   * @param accessException the access exception
   * @param request         the request
   * @param response        the response
   * @return the model and view
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ModelAndView handleAccessDeniedException(AccessDeniedException accessException,
      HttpServletRequest request, HttpServletResponse response) {

    logger.debug("Access Denied Excception StackTrace:", accessException);

    ModelAndView mav = prepareModelAndView(accessException);
    mav.setViewName(com.insta.hms.common.URLRoute.INSUFFICIENT_PERMISSIONS_PAGE);

    response.setStatus(accessException.getStatus().value());

    return mav;
  }

  /**
   * Handle conversion exception.
   *
   * @param conversionException the conversion exception
   * @param request             the request
   * @param response            the response
   * @param redirect            the redirect
   * @return the model and view
   * @throws URISyntaxException the URI syntax exception
   */
  @ExceptionHandler(ConversionException.class)
  public ModelAndView handleConversionException(ConversionException conversionException,
      HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirect)
      throws URISyntaxException {

    logger.debug("Conversion Exception StackTrace:", conversionException);

    ModelAndView mav = prepareModelAndView(conversionException);

    // redirectToReferer
    mav.setViewName(UrlUtil.redirectToReferer(request));

    // queryParams into the map
    String queryString = getQueryString(request);
    redirect.mergeAttributes(UrlUtil.paramsToMap(queryString));

    // show error fields
    mav.addObject("errorFields", conversionException.getFields());

    // Set response status
    response.setStatus(conversionException.getStatus().value());

    return mav;

  }

  /**
   * Handle duplicate entity exception.
   *
   * @param dex      the dex
   * @param request  the request
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   * @throws URISyntaxException the URI syntax exception
   */
  @ExceptionHandler(DuplicateEntityException.class)
  public ModelAndView handleDuplicateEntityException(DuplicateEntityException dex,
      HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirect)
      throws URISyntaxException {

    logger.debug("Duplicate Entity Exception StackTrace:", dex);

    ModelAndView mav = prepareModelAndView(dex);
    @SuppressWarnings("unused")
    FlashMap flashMap = prepareFlashMap(request, dex);

    mav.addObject("duplicate", dex.getDuplicateObject());
    mav.setViewName(UrlUtil.redirectToReferer(request));

    String queryString = getQueryString(request);
    redirect.mergeAttributes(UrlUtil.paramsToMap(queryString));

    response.setStatus(dex.getStatus().value());
    return mav;
  }

  /**
   * Handle duplicate key exception.
   *
   * @param keyException the key exception
   * @param response     the response
   * @param request      the request
   * @param redirect     the redirect
   * @return the model and view
   * @throws URISyntaxException the URI syntax exception
   */
  @ExceptionHandler(DuplicateKeyException.class)
  public ModelAndView handleDuplicateKeyException(DuplicateKeyException keyException,
      HttpServletResponse response, HttpServletRequest request, RedirectAttributes redirect)
      throws URISyntaxException {

    logger.error("Duplicate Key Exception StackTrace:", keyException);

    HttpStatus status = HttpStatus.BAD_REQUEST;
    String key = "exception.duplicate.primary.key";

    ModelAndView mav = prepareModelAndView(status, key, null);

    @SuppressWarnings("unused")
    FlashMap flashMap = prepareFlashMap(request, keyException);
    mav.setViewName(UrlUtil.redirectToReferer(request));
    String queryString = getQueryString(request);
    redirect.mergeAttributes(UrlUtil.paramsToMap(queryString));

    response.setStatus(status.value());

    return mav;
  }

  /**
   * Handle entity not found exception.
   *
   * @param notFoundException the not found exception
   * @param response          the response
   * @param request           the request
   * @return the model and view
   */
  @ExceptionHandler(EntityNotFoundException.class)
  public ModelAndView handleEntityNotFoundException(EntityNotFoundException notFoundException,
      HttpServletResponse response, HttpServletRequest request) {

    logger.debug("Entity not found StackTrace:", notFoundException);

    ModelAndView mav = prepareModelAndView(notFoundException);
    response.setStatus(notFoundException.getStatus().value());
    return mav;
  }

  /**
   * Handle invalid file format exception.
   *
   * @param invalidFileException the invalid file exception
   * @param request              the request
   * @param response             the response
   * @param redirect             the redirect
   * @return the model and view
   * @throws URISyntaxException the URI syntax exception
   */
  @ExceptionHandler(InvalidFileFormatException.class)
  public ModelAndView handleInvalidFileFormatException(
      InvalidFileFormatException invalidFileException, HttpServletRequest request,
      HttpServletResponse response, RedirectAttributes redirect) throws URISyntaxException {

    logger.debug("Invalid File format stackTrace:", invalidFileException);

    ModelAndView mav = prepareModelAndView(invalidFileException);
    @SuppressWarnings("unused")
    FlashMap flashMap = prepareFlashMap(request, invalidFileException);

    mav.setViewName(UrlUtil.redirectToReferer(request));

    String queryString = getQueryString(request);
    redirect.mergeAttributes(UrlUtil.paramsToMap(queryString));

    response.setStatus(invalidFileException.getStatus().value());
    return mav;

  }

  /**
   * Handle missing servlet request parameter exception.
   *
   * @param exception the exception
   * @param response  the response
   * @param request   the request
   * @return the model and view
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ModelAndView handleMissingServletRequestParameterException(
      MissingServletRequestParameterException exception, HttpServletResponse response,
      HttpServletRequest request) {

    logger.debug("Parameter not passed StackTrace:", exception);
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String key = "exception.parameters.missing";

    ModelAndView mav = prepareModelAndView(status, key, null);
    mav.setViewName(UrlUtil.redirectToReferer(request));
    response.setStatus(status.value());
    return mav;
  }

  /**
   * Handle missing file exception.
   *
   * @param missingFileException the missing file exception
   * @param request              the request
   * @param response             the response
   * @param redirect             the redirect
   * @return the model and view
   * @throws URISyntaxException the URI syntax exception
   */
  @ExceptionHandler(MissingServletRequestPartException.class)
  public ModelAndView handleMissingFileException(
      MissingServletRequestPartException missingFileException, HttpServletRequest request,
      HttpServletResponse response, RedirectAttributes redirect) throws URISyntaxException {

    logger.debug("Invalid File format stackTrace:", missingFileException);

    HttpStatus status = HttpStatus.BAD_REQUEST;
    String key = "exception.file.missing";

    ModelAndView mav = prepareModelAndView(status, key, null);
    @SuppressWarnings("unused")
    FlashMap flashMap = prepareFlashMap(request, missingFileException);

    mav.setViewName(UrlUtil.redirectToReferer(request));

    String queryString = getQueryString(request);
    redirect.mergeAttributes(UrlUtil.paramsToMap(queryString));

    response.setStatus(status.value());
    return mav;

  }

  /**
   * Handle expression validation exception.
   *
   * @param expressionValidationException the expression validation exception
   * @param request                       the request
   * @param response                      the response
   * @param redirect                      the redirect
   * @return the model and view
   * @throws URISyntaxException the URI syntax exception
   */
  @ExceptionHandler(ExpressionValidationException.class)
  public ModelAndView handleExpressionValidationException(
      ExpressionValidationException expressionValidationException, HttpServletRequest request,
      HttpServletResponse response, RedirectAttributes redirect) throws URISyntaxException {
    logger.debug("Expression Validation stackTrace:", expressionValidationException);

    ModelAndView mav = prepareModelAndView(expressionValidationException);

    FlashMap flashMap = prepareFlashMap(request, expressionValidationException);
    flashMap.put("error", expressionValidationException.getMessage());

    mav.setViewName(UrlUtil.redirectToReferer(request));

    String queryString = getQueryString(request);
    redirect.mergeAttributes(UrlUtil.paramsToMap(queryString));

    response.setStatus(expressionValidationException.getStatus().value());
    return mav;
  }

  /**
   * Handle validation exception.
   *
   * @param validationException the validation exception
   * @param response            the response
   * @param request             the request
   * @param redirect            the redirect
   * @return the model and view
   * @throws URISyntaxException the URI syntax exception
   */
  @ExceptionHandler(ValidationException.class)
  public ModelAndView handleValidationException(ValidationException validationException,
      HttpServletResponse response, HttpServletRequest request, RedirectAttributes redirect)
      throws URISyntaxException {

    logger.debug("Validation Exception StackTrace:", validationException);

    ModelAndView mav = prepareModelAndView(validationException);

    // put validation errors into the flashMap
    FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
    flashMap.put("error", validationException.getFormattedErrors());

    mav.addObject("validationErrors", validationException.getErrors());

    // redirectToReferer
    mav.setViewName(UrlUtil.redirectToReferer(request));

    // queryParams into the map
    String queryString = getQueryString(request);
    redirect.mergeAttributes(UrlUtil.paramsToMap(queryString));

    // Set response status
    response.setStatus(validationException.getStatus().value());

    return mav;
  }

  /**
   * Handle HMS exception.
   *
   * @param exception the exception
   * @param response  the response
   * @param request   the request
   * @param redirect  the redirect
   * @return the model and view
   */
  @ExceptionHandler(HMSException.class)
  public ModelAndView handleHMSException(HMSException exception, HttpServletResponse response,
      HttpServletRequest request, RedirectAttributes redirect) {
    logger.error("HMS Exception Stacktrace:", exception);

    ModelAndView mav = prepareModelAndView(exception);
    mav.addObject("display_message", exception.toString());
    mav.addObject("stacktrace", exception.getStackTrace());
    mav.setViewName(URLRoute.HMS_EXCEPTION);

    response.setStatus(exception.getStatus().value());
    return mav;
  }

  /**
   * Handle nestable validation exception.
   *
   * @param nestableException the nestable exception
   * @param response          the response
   * @param request           the request
   * @param redirect          the redirect
   * @return the model and view
   * @throws URISyntaxException the URI syntax exception
   */
  @ExceptionHandler(NestableValidationException.class)
  public ModelAndView handleNestableValidationException(
      NestableValidationException nestableException, HttpServletResponse response,
      HttpServletRequest request, RedirectAttributes redirect) throws URISyntaxException {

    ModelAndView mav = prepareModelAndView(nestableException);
    String message = getMessageFromJson(nestableException.getNestedErrorMap());
    FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
    flashMap.put("error", message);

    mav.setViewName(UrlUtil.redirectToReferer(request));

    String queryString = getQueryString(request);
    redirect.mergeAttributes(UrlUtil.paramsToMap(queryString));

    HttpStatus status = nestableException.getStatus();
    response.setStatus(status.value());
    return mav;
  }

  /**
   * Handle exception.
   *
   * @param ex       the ex
   * @param request  the request
   * @param response the response
   * @return the model and view
   */
  @ExceptionHandler(Exception.class)
  public ModelAndView handleException(Exception ex, HttpServletRequest request,
      HttpServletResponse response) {

    logger.error("Exception StackTrace:", ex);

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    ModelAndView mav = prepareModelAndView(status, "exception.internal.error", null);
    mav.addObject("display_message", ex.toString());
    mav.addObject("stacktrace", ex.getStackTrace());
    // FlashMap flashMap = prepareFlashMap(request, ex);

    mav.setViewName(URLRoute.HMS_EXCEPTION);

    response.setStatus(status.value());

    return mav;
  }

  /**
   * Prepare model and view.
   *
   * @param hex the hex
   * @return the model and view
   */
  private ModelAndView prepareModelAndView(HMSException hex) {
    ModelAndView mav = new ModelAndView();

    mav.addObject("error", hex.getErrorResponse());
    mav.setViewName(URLRoute.HMS_EXCEPTION);

    return mav;
  }

  /**
   * Prepare model and view.
   *
   * @param status     the status
   * @param key        the key
   * @param parameters the parameters
   * @return the model and view
   */
  private ModelAndView prepareModelAndView(HttpStatus status, String key, String parameters) {

    ModelAndView mav = new ModelAndView();

    // get locale for internationalization of exception message
    locale = LocaleContextHolder.getLocale();

    // Prepare error response
    String[] param = { parameters };
    mav.addObject("error",
        prepareErrorResponse(status, exceptionMessageSource.getMessage(key, param, locale)));

    // Set view
    mav.setViewName(URLRoute.HMS_EXCEPTION);

    return mav;
  }

  /**
   * Prepare flash map.
   *
   * @param request the request
   * @param ex      the ex
   * @return the flash map
   */
  private FlashMap prepareFlashMap(HttpServletRequest request, Exception ex) {
    FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
    flashMap.put("error", ex.getMessage());
    return flashMap;
  }

  /**
   * Prepare error response.
   *
   * @param status         the status
   * @param displayMessage the display message
   * @return the HMS error response
   */
  private HMSErrorResponse prepareErrorResponse(HttpStatus status, String displayMessage) {
    HMSErrorResponse error = new HMSErrorResponse(status, displayMessage);
    return error;
  }

  /**
   * Prepare error response.
   *
   * @param displayMessage the display message
   * @return the HMS error response
   */
  @SuppressWarnings("unused")
  private HMSErrorResponse prepareErrorResponse(String displayMessage) {
    HMSErrorResponse error = new HMSErrorResponse(displayMessage);
    return error;
  }

  /**
   * Gets the query string.
   *
   * @param request the request
   * @return the query string
   * @throws URISyntaxException the URI syntax exception
   */
  private String getQueryString(HttpServletRequest request) throws URISyntaxException {

    if (request.getHeader("referer") == null) {
      return null;
    }

    URI uri = new URI(request.getHeader("referer"));
    return uri.getQuery();

  }

  /**
   * Gets the message from json.
   *
   * @param map the map
   * @return the message from json
   */
  private String getMessageFromJson(Map map) {

    List<String> messagesList = new ArrayList<String>();
    StringBuilder finalMessageBuilder = new StringBuilder("");
    if (map != null) {
      messageExtractor(map, messagesList);
    }
    for (String message : messagesList) {
      if (!message.equalsIgnoreCase("No error Messages")) {
        finalMessageBuilder.append(message + "</br>");
      }
    }
    String finalMessage = finalMessageBuilder.toString();
    return finalMessage.equals("") ? "No error Messages" : finalMessage;
  }

  /**
   * Message extractor.
   *
   * @param errorObj     the error obj
   * @param messagesList the messages list
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void messageExtractor(Object errorObj, List<String> messagesList) {

    if (errorObj == null) {
      messagesList.add("No error Messages");
      return;
    }
    if (errorObj instanceof Map) {
      Set<String> keySet = ((Map) errorObj).keySet();
      for (String key : keySet) {
        messageExtractor(((Map) errorObj).get(key), messagesList);
      }
    } else if (errorObj instanceof List) {
      for (Object obj : (List) errorObj) {
        messageExtractor(obj, messagesList);
      }
    } else if (errorObj instanceof String) {
      messagesList.add((String) errorObj);
    }
  }

}
