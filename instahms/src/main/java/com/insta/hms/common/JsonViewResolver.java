package com.insta.hms.common;

import org.springframework.util.PatternMatchUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;

/**
 * The Class JsonViewResolver.
 *
 * @author aditya To test whether a json view resolver is a better fit than JsonView
 */
public class JsonViewResolver implements ViewResolver {

  /** The view names. */
  private String[] viewNames;

  /*
   * (non-Javadoc) checks if the viewName can be handled and Creates a Json view @see
   * com.insta.common.JsonView
   * 
   * @see org.springframework.web.servlet.ViewResolver#resolveViewName(java.lang.String,
   * java.util.Locale)
   */
  @Override
  public View resolveViewName(String viewName, Locale locale) throws Exception {
    if (!canHandle(viewName, locale)) {
      return null;
    }
    JsonView view = new JsonView();
    view.setPrettyPrint(true);
    return view;
  }

  /**
   * Can handle.
   *
   * @param viewName the view name
   * @param locale   the locale
   * @return true, if successful
   */
  protected boolean canHandle(String viewName, Locale locale) {
    String[] viewNames = getViewNames();
    return (viewNames == null || PatternMatchUtils.simpleMatch(viewNames, viewName));
  }

  /**
   * Sets the view names.
   *
   * @param viewNames the new view names
   */
  public void setViewNames(String... viewNames) {
    this.viewNames = viewNames;
  }

  /**
   * Gets the view names.
   *
   * @return the view names
   */
  protected String[] getViewNames() {
    return this.viewNames;
  }
}
