package com.insta.hms.mdm;

import java.util.HashMap;
import java.util.Map;

public class BulkDataResponseRouter extends MasterResponseRouter {

  private static final Map<String, String[]> DEFAULT_ACTION_VIEW_MAP = 
      new HashMap<String, String[]>();
  private static final Map<String, Boolean> REDIRECT_VIEW_MAP = new HashMap<String, Boolean>();

  static {
    DEFAULT_ACTION_VIEW_MAP.put("list", new String[] { "list" });
    DEFAULT_ACTION_VIEW_MAP.put("show", new String[] { "show" });
    DEFAULT_ACTION_VIEW_MAP.put("add", new String[] { "add" });
    DEFAULT_ACTION_VIEW_MAP.put("create", new String[] { "show", "list" });
    DEFAULT_ACTION_VIEW_MAP.put("update", new String[] { "show", "show" });
    DEFAULT_ACTION_VIEW_MAP.put("delete", new String[] { "list", "show" });
    DEFAULT_ACTION_VIEW_MAP.put("import", new String[] { "list" });
  }

  static {
    REDIRECT_VIEW_MAP.put("create", Boolean.TRUE);
    REDIRECT_VIEW_MAP.put("update", Boolean.TRUE);
    REDIRECT_VIEW_MAP.put("delete", Boolean.TRUE);
  }

  private BulkDataResponseRouter(String pathElement) {
    super(pathElement);
  }

  @Override
  protected String routeSuccess(String action) {
    String[] targetPages = DEFAULT_ACTION_VIEW_MAP.get(action);

    if (null != targetPages && targetPages.length > 0) {
      if (null != REDIRECT_VIEW_MAP.get(action)) {
        return routeRedirect(targetPages[0]);
      } else {
        return routePage(targetPages[0]);
      }
    }
    return super.routeSuccess(action);
  }

  @Override
  protected String routeError(String action) {
    String[] targetPages = DEFAULT_ACTION_VIEW_MAP.get(action);
    if (null != targetPages && targetPages.length > 0) {
      String targetPage = targetPages[0]; // first page is the default
      if (targetPages.length > 1) {
        targetPage = targetPages[1]; // if an error page is specified, then we use that
      }
      if (null != REDIRECT_VIEW_MAP.get(action)) {
        return routeRedirect(targetPage);
      } else {
        return routePage(targetPage);
      }
    }

    return super.routeError(action);
  }

  public static final ResponseRouter STORE_ROUTER = new BulkDataResponseRouter("store");
  public static final ResponseRouter STOCK_UPLOAD_ROUTER = new BulkDataResponseRouter(
      "stockuploads");

}
