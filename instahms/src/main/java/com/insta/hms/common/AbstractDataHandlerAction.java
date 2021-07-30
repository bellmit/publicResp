package com.insta.hms.common;

import com.insta.hms.csvutils.TableDataHandler;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class AbstractDataHandlerAction.
 */
public abstract class AbstractDataHandlerAction extends BaseAction {

  /**
   * Gets the data handler.
   *
   * @return the data handler
   */
  protected abstract TableDataHandler getDataHandler();

  /**
   * Import master.
   *
   * @param mapping    the mapping
   * @param actionForm the action form
   * @param req        the req
   * @param res        the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward importMaster(ActionMapping mapping, ActionForm actionForm,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    FlashScope flash = FlashScope.getScope(req);
    String referer = req.getHeader("Referer");
    referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
    ActionRedirect redirect = new ActionRedirect(referer);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    UploadForm uploadForm = (UploadForm) actionForm;
    InputStreamReader isReader = new InputStreamReader(uploadForm.getUploadFile().getInputStream());

    StringBuilder infoMsg = new StringBuilder();
    String error = getDataHandler().importTable(isReader, infoMsg);

    if (error != null) {
      flash.put("error", error);
      return redirect;
    }

    flash.put("info", infoMsg.toString());
    return redirect;
  }

  /**
   * Export master.
   *
   * @param mapping    the mapping
   * @param actionForm the action form
   * @param req        the req
   * @param res        the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward exportMaster(ActionMapping mapping, ActionForm actionForm,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    getDataHandler().exportTable(res);
    return null;
  }

}
