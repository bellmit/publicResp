package com.insta.hms.core.medicalrecords.codification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class MrdCodificationController.
 */
@Controller("mrdObservationController")
@RequestMapping(URLRoute.MRD_CODIFICATION)
public class MrdCodificationController extends BaseController {

  /** The mrd obs service. */
  @LazyAutowired
  private MRDObservationsService mrdObsService;

  /** The Constant DOC ID. */
  private static final String DOC_ID = "doc_id";

  /**
   * Saves treatment codes observations. Takes a list of multipart files for
   * upload in patient documents.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @param files
   *          the files
   * @return the model and view
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  @RequestMapping(value = { "/trtObservations" }, method = RequestMethod.POST)
  public ModelAndView saveTreatMentObs(HttpServletRequest request,
      ModelMap mmap, HttpServletResponse response, RedirectAttributes redirect,
      @RequestPart("uploadFile") List<MultipartFile> files)
      throws IOException, SQLException {

    ModelAndView mav = new ModelAndView();

    ObjectMapper mapper = new ObjectMapper();
    MrdObservationsModel[] dto = mapper.readValue(
        request.getParameter("obsObj"), MrdObservationsModel[].class);
    Map<String, String[]> parameters = request.getParameterMap();
    if (dto.length > 0) {
      MrdObservationsModel[] result = mrdObsService.saveMrdObservations(dto,
          files);
      if (result == null) {
        mav.addObject("error", "Exception occured when saving observations. "
            + "Check logs for more information.");
      } else {
        mav.addObject("trtObservations", result);
      }
    } else if (parameters.get("obsChgId").length > 0) {
      mrdObsService.deleteObservations(parameters.get("obsChgId")[0],
          new HashSet<Integer>());
    }

    mav.setView(new MappingJackson2JsonView());
    mav.setStatus(HttpStatus.OK);
    return mav;
  }

  /**
   * Download document.
   *
   * @param request the request
   * @param mmap the mmap
   * @param response the response
   * @param redirect the redirect
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = { "/downloadDocument" }, method = RequestMethod.GET)
  public void downloadDocument(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect)
      throws IOException {
    Map<String, String[]> parameters = request.getParameterMap();
    String mrNo = null;
    if (parameters.get("mr_no") != null && parameters.get("mr_no").length > 0
        && (parameters.get("mr_no"))[0] != null) {
      mrNo = (parameters.get("mr_no"))[0];
    }
    if (parameters.get("doc_id") != null && parameters.get("doc_id").length > 0
        && (parameters.get("doc_id"))[0] != null && mrNo != null) {
      int docId = Integer.parseInt((parameters.get("doc_id"))[0]);
      File attachedDoc = mrdObsService.getAttachDocumentFile(docId, mrNo);
      try (FileInputStream stream = new FileInputStream(attachedDoc)) {
        String mimeType = "application/octet-stream";
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition",
            "attachment; filename=\"" + attachedDoc.getName() + "\"");
        StreamUtils.copy(stream, response.getOutputStream());
      } catch (FileNotFoundException exception) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested file doesn't exist");
      } catch (NullPointerException exception) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
      }
    } else {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }
}
