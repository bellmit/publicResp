package com.insta.hms.customreports;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;

import flexjson.JSONSerializer;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * The Class CustomReportsAddAction.
 */
public class CustomReportsAddAction extends DispatchAction {
  
  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(CustomReportsAddAction.class);

  /**
   * Adds the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the SQL exception
   */
  public ActionForward add(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {
    List allReportNames = CustomReportsDAO.getAllReportNames();
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("allReportNamesJSON",
        js.deepSerialize(ConversionUtils.listBeanToListMap(allReportNames)));
    return mapping.findForward("add");
  }

  /**
   * Creates the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the SQL exception
   */
  public ActionForward create(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    CustomReportsForm crForm = (CustomReportsForm) form;
    ArrayList errors = new ArrayList();
    List<BasicDynaBean> reportBeans = new ArrayList<BasicDynaBean>();

    CustomReportsDAO reportsdao = new CustomReportsDAO();
    int reportid = reportsdao.generatedId();

    String metadata = "";
    String fileName = "";
    if (crForm.getReport_metadata() != null) {
      metadata = new String(crForm.getReport_metadata().getFileData());
      fileName = crForm.getReport_metadata().getFileName();
    }

    try {
      BasicDynaBean reportBean = buildReportBean(request, metadata, errors, "");
      Boolean isDuplicate = reportsdao
          .checkIfDuplicate(reportBean.get(Constants.REPORT_NAME).toString());
      if (isDuplicate) {
        request.setAttribute(Constants.ERROR,
            "The Report Name- \"" + reportBean.get(Constants.REPORT_NAME)
                + "\" already exists....Please try another");
      }
      if (!errors.isEmpty()) {
        request.setAttribute(Constants.ERROR,
            "Incorrectly formatted values supplied");
      } else {
        reportBean.set(Constants.FILE_NAME, fileName);
        reportBean.set(Constants.REPORT_ID, reportid);
        reportBeans.add(reportBean);

        // subreport1
        String subdata1 = new String(crForm.getSubreport1().getFileData());
        String sfileName1 = crForm.getSubreport1().getFileName();
        if (subdata1 != null && !subdata1.equals("")) {
          BasicDynaBean subreport = buildReportBean(request, subdata1, errors,
              "subreport1_");
          subreport.set(Constants.REPORT_ID, reportsdao.generatedId());
          subreport.set(Constants.PARENT_ID, reportid);
          subreport.set(Constants.FILE_NAME, sfileName1);
          reportBeans.add(subreport);
        }
        // subreport2
        String subdata2 = new String(crForm.getSubreport2().getFileData());
        String sfileName2 = crForm.getSubreport2().getFileName();
        if (subdata2 != null && !subdata2.equals("")) {
          BasicDynaBean subreport = buildReportBean(request, subdata2, errors,
              "subreport2_");
          subreport.set(Constants.REPORT_ID, reportsdao.generatedId());
          subreport.set(Constants.PARENT_ID, reportid);
          subreport.set(Constants.FILE_NAME, sfileName2);
          reportBeans.add(subreport);
        }
        // subreport3
        String subdata3 = new String(crForm.getSubreport3().getFileData());
        String sfileName3 = crForm.getSubreport3().getFileName();
        if (subdata3 != null && !subdata3.equals("")) {
          BasicDynaBean subreport = buildReportBean(request, subdata3, errors,
              "subreport3_");
          subreport.set(Constants.REPORT_ID, reportsdao.generatedId());
          subreport.set(Constants.PARENT_ID, reportid);
          subreport.set(Constants.FILE_NAME, sfileName3);
          reportBeans.add(subreport);
        }
        // subreport4
        String subdata4 = new String(crForm.getSubreport4().getFileData());
        String sfileName4 = crForm.getSubreport4().getFileName();
        if (subdata4 != null && !subdata4.equals("")) {
          BasicDynaBean subreport = buildReportBean(request, subdata4, errors,
              "subreport4_");
          subreport.set(Constants.REPORT_ID, reportsdao.generatedId());
          subreport.set(Constants.PARENT_ID, reportid);
          subreport.set(Constants.FILE_NAME, sfileName4);
          reportBeans.add(subreport);
        }
        // subreport5
        String subdata5 = new String(crForm.getSubreport5().getFileData());
        String sfileName5 = crForm.getSubreport5().getFileName();
        if (subdata5 != null && !subdata5.equals("")) {
          BasicDynaBean subreport = buildReportBean(request, subdata5, errors,
              "subreport5_");
          subreport.set(Constants.REPORT_ID, reportsdao.generatedId());
          subreport.set(Constants.PARENT_ID, reportid);
          subreport.set(Constants.FILE_NAME, sfileName5);
          reportBeans.add(subreport);
        }
        // subreport6
        String subdata6 = new String(crForm.getSubreport6().getFileData());
        String sfileName6 = crForm.getSubreport6().getFileName();
        if (subdata6 != null && !subdata6.equals("")) {
          BasicDynaBean subreport = buildReportBean(request, subdata6, errors,
              "subreport6_");
          subreport.set(Constants.REPORT_ID, reportsdao.generatedId());
          subreport.set(Constants.PARENT_ID, reportid);
          subreport.set(Constants.FILE_NAME, sfileName6);
          reportBeans.add(subreport);
        }
        // subreport7
        String subdata7 = new String(crForm.getSubreport7().getFileData());
        String sfileName7 = crForm.getSubreport7().getFileName();
        if (subdata7 != null && !subdata7.equals("")) {
          BasicDynaBean subreport = buildReportBean(request, subdata7, errors,
              "subreport7_");
          subreport.set(Constants.REPORT_ID, reportsdao.generatedId());
          subreport.set(Constants.PARENT_ID, reportid);
          subreport.set(Constants.FILE_NAME, sfileName7);
          reportBeans.add(subreport);
        }
        // subreport8
        String subdata8 = new String(crForm.getSubreport8().getFileData());
        String sfileName8 = crForm.getSubreport8().getFileName();
        if (subdata8 != null && !subdata8.equals("")) {
          BasicDynaBean subreport = buildReportBean(request, subdata8, errors,
              "subreport8_");
          subreport.set(Constants.REPORT_ID, reportsdao.generatedId());
          subreport.set(Constants.PARENT_ID, reportid);
          subreport.set(Constants.FILE_NAME, sfileName8);
          reportBeans.add(subreport);
        }
        // subreport9
        String subdata9 = new String(crForm.getSubreport9().getFileData());
        String sfileName9 = crForm.getSubreport9().getFileName();
        if (subdata9 != null && !subdata9.equals("")) {
          BasicDynaBean subreport = buildReportBean(request, subdata9, errors,
              "subreport9_");
          subreport.set(Constants.REPORT_ID, reportsdao.generatedId());
          subreport.set(Constants.PARENT_ID, reportid);
          subreport.set(Constants.FILE_NAME, sfileName9);
          reportBeans.add(subreport);
        }
        // subreport10
        String subdata10 = new String(crForm.getSubreport10().getFileData());
        String sfileName10 = crForm.getSubreport10().getFileName();
        if (subdata10 != null && !subdata10.equals("")) {
          BasicDynaBean subreport = buildReportBean(request, subdata10, errors,
              "subreport10_");
          subreport.set(Constants.REPORT_ID, reportsdao.generatedId());
          subreport.set(Constants.PARENT_ID, reportid);
          subreport.set(Constants.FILE_NAME, sfileName10);
          reportBeans.add(subreport);
        }
      }
    } catch (JRException jrExp) {
      request.setAttribute(Constants.ERROR, "Invalid report metadata file uploaded");
    }

    if (request.getAttribute(Constants.ERROR) == null) {
      Connection con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      try {
        boolean success = reportsdao.insertAll(con, reportBeans);
        List<BasicDynaBean> beans = new ArrayList<>();

        int count = Integer.parseInt(request.getParameter("var_count"));
        for (int pointer = 1; pointer <= count; pointer++) {
          String variable = request.getParameter(Constants.REPORT_VAR + pointer);
          if (variable != null && !(variable.trim().equals(""))) {
            BasicDynaBean bean = getVariableBean();
            bean.set(Constants.REPORT_ID, reportid);
            bean.set(Constants.REPORT_VAR, variable);
            bean.set(Constants.REPORT_VAR_LABEL,
                request.getParameter(Constants.REPORT_VAR_LABEL + pointer));
            bean.set(Constants.REPORT_VAR_TYPE,
                request.getParameter(Constants.REPORT_VAR_TYPE + pointer));
            beans.add(bean);
          }
        }

        if (!beans.isEmpty()) {
          GenericDAO reportvariablesdao = new GenericDAO("custom_report_variables");
          success &= reportvariablesdao.insertAll(con, beans);
        }
        if (success) {
          con.commit();

          FlashScope flash = FlashScope.getScope(request);
          flash.success("Custom Report Added Successfully");

          ActionRedirect redirect = new ActionRedirect(
              mapping.findForward("listRedirect"));
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

          return redirect;
        } else {
          con.rollback();
          request.setAttribute(Constants.ERROR, "Failed to add Custom Report");
        }
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
    }
    List allReportNames = CustomReportsDAO.getAllReportNames();
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("allReportNamesJSON",
        js.deepSerialize(ConversionUtils.listBeanToListMap(allReportNames)));
    return mapping.findForward("add");
  }

  /**
   * Compile report.
   *
   * @param sc the sc
   * @param baseDir the base dir
   * @param reportname the reportname
   * @param reportMetadata the report metadata
   * @param timestamp the timestamp
   * @return the string
   * @throws JRException the JR exception
   */
  private static synchronized String compileReport(ServletContext sc,
      String baseDir, String reportname, String reportMetadata, long timestamp)
      throws JRException {
    String basepath = Constants.REPORT_BASE_DIR + "/" + baseDir;
    File basedir = new File(sc.getRealPath(basepath));
    basedir.mkdirs();

    String jasperPath = sc.getRealPath(basepath + "/" + reportname + ".jasper");
    File jasperFile = new File(jasperPath);

    if ((jasperFile.lastModified() == 0)
        || (timestamp > jasperFile.lastModified())) {

      logger.debug("Compiling Custom Report to: " + jasperFile);
      try (FileOutputStream out = new FileOutputStream(jasperFile)) {
        InputStream is = new ByteArrayInputStream(reportMetadata.getBytes());
        JasperCompileManager.compileReportToStream(is, out);
        logger.debug("Finished compilation.");
      } catch (FileNotFoundException fex) {
        logger.info(fex.getMessage());
      } catch (IOException ioEx) {
        logger.info(ioEx.getMessage());
      } catch (JRException jex) {
        jasperFile.delete(); // cleanup the file as it is invalid
        throw jex; // rethrow exception
      }
    } else {
      logger.debug("Using pre-compiled report: " + jasperPath);
    }

    return jasperPath;
  }

  /**
   * Builds the report bean.
   *
   * @param request the request
   * @param metadata the metadata
   * @param errors the errors
   * @param prefix the prefix
   * @return the basic dyna bean
   * @throws JRException the JR exception
   */
  private BasicDynaBean buildReportBean(HttpServletRequest request,
      String metadata, List errors, String prefix) throws JRException {
    Map parameters = request.getParameterMap();

    BasicDynaBean bean = getReportBean();
    java.util.Date now = new java.util.Date();
    bean.set("timestamp", new java.sql.Date(now.getTime()));
    ConversionUtils.copyToDynaBean(parameters, bean, errors);

    String name = prefix + (String) bean.get(Constants.REPORT_NAME);
    bean.set(Constants.REPORT_NAME, name);

    bean.set("report_metadata", metadata);
    HttpSession session = request.getSession(false);
    String userId = (String) session.getAttribute("userid");
    bean.set("user_name", userId);

    if ("jrxml".equals(request.getParameter("report_type"))) {
      ServletContext sc = session.getServletContext();
      String hospital = (String) session.getAttribute("sesHospitalId");
      compileReport(sc, hospital, name, metadata, now.getTime());
    }

    return bean;
  }

  /**
   * Gets the report bean.
   *
   * @return the report bean
   */
  private BasicDynaBean getReportBean() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add(Constants.REPORT_ID, Integer.class).add(Constants.REPORT_NAME)
        .add("report_desc").add("report_metadata")
        .add(Constants.PARENT_ID, Integer.class).add("timestamp", Date.class)
        .add("report_type").add("csv_view_name").add("ftl_csv_supported")
        .add(Constants.FILE_NAME).add("user_name");

    return builder.build();
  }

  /**
   * Gets the variable bean.
   *
   * @return the variable bean
   */
  private BasicDynaBean getVariableBean() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add(Constants.REPORT_ID, Integer.class).add(Constants.REPORT_VAR)
        .add(Constants.REPORT_VAR_LABEL).add(Constants.REPORT_VAR_TYPE);

    return builder.build();
  }
}
