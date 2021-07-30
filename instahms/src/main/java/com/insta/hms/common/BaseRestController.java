package com.insta.hms.common;

import com.insta.hms.exception.NestableValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class BaseRestController.
 *
 * @author aditya for common logic of rest controllers
 */
public class BaseRestController extends BaseController {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(BaseRestController.class);

  /**
   * Handle exception handler.
   *
   * @param nestableException the nestable exception
   * @param request           the request
   * @param response          the response
   * @return the response entity
   */
  @ExceptionHandler(NestableValidationException.class)
  public ResponseEntity<Map<String, Object>> handleExceptionHandler(
      NestableValidationException nestableException, HttpServletRequest request,
      HttpServletResponse response) {

    Map<String, Object> responseObject = new HashMap<String, Object>();
    responseObject.put("error", nestableException.getErrorResponse());
    responseObject.put("validationErrors", nestableException.getErrors());

    response.setStatus(nestableException.getStatus().value());
    return new ResponseEntity<Map<String, Object>>(responseObject, nestableException.getStatus());
  }
}
