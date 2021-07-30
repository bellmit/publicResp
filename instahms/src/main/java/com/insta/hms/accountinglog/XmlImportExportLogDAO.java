package com.insta.hms.accountinglog;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.focus.FocusSyncReader.ErrorCodes;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class XmlImportExportLogDAO.
 *
 * @author krishna
 */
public class XmlImportExportLogDAO extends GenericDAO {

  /**
   * Instantiates a new xml import export log DAO.
   */
  public XmlImportExportLogDAO() {
    super("accounting_xml_export_import_log");
  }

  /** The Constant SEARCH_LOG_FIELDS. */
  private static final String SEARCH_LOG_FIELDS = " select *, schedule_name, "
      + " (SELECT count(*) FROM accounting_voucher_details avd "
      + " WHERE avd.export_no=log.export_no) as total_vouchers ";

  /** The Constant FROM_TABLES. */
  private static final String FROM_TABLES = " FROM accounting_xml_export_import_log as log "
      + " LEFT JOIN scheduled_export_prefs as pref ON (log.schedule_id=pref.schedule_id) ";

  /** The Constant COUNT. */
  private static final String COUNT = "select count(file_name) ";

  /**
   * Search logs.
   *
   * @param params
   *          the params
   * @param listing
   *          the listing
   * @return the paged list
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public PagedList searchLogs(Map params, Map listing) throws SQLException, ParseException {
    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder sb = null;
    try {
      sb = new SearchQueryBuilder(con, SEARCH_LOG_FIELDS, COUNT, FROM_TABLES, listing);
      sb.addFilterFromParamMap(params);
      sb.build();
      return sb.getDynaPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (sb != null) {
        sb.close();
      }
    }

  }

  /** The Constant GET_VOUCHERS. */
  private static final String GET_VOUCHERS = "SELECT voucher_no, "
      + "voucher_type, export_no FROM accounting_voucher_details WHERE export_no=?";

  /**
   * Gets the vouchers.
   *
   * @param exportId
   *          the export id
   * @return the vouchers
   * @throws SQLException
   *           the SQL exception
   */
  public List getVouchers(int exportId) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_VOUCHERS, exportId);
  }

  /**
   * Gets the vouchers.
   *
   * @param voucherList
   *          the voucher list
   * @param voucherType
   *          the voucher type
   * @param className
   *          the class name
   * @return the vouchers
   */
  public List getVouchers(List<BasicDynaBean> voucherList, String voucherType, Class className) {
    if (voucherList == null) {
      return null;
    }
    List<Map> list = new ArrayList<Map>();
    for (BasicDynaBean bean : voucherList) {
      if (bean.get("voucher_type").equals(voucherType)) {
        Map map = new HashMap(bean.getMap());
        String voucherNo = (String) map.get("voucher_no");

        map.put("voucher_no", ConvertUtils.convert(voucherNo, className));
        list.add(map);
      }
    }
    return list;
  }

  /**
   * Gets the all logs.
   *
   * @param con
   *          the con
   * @return the all logs
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getAllLogs(Connection con) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement("SELECT * FROM accounting_xml_export_import_log");
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Update status.
   *
   * @param con
   *          the con
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean updateStatus(Connection con) throws SQLException, IOException {
    List<BasicDynaBean> importlogList = getAllLogs(con);
    String[] folders = { "input", "done", "error" };

    for (BasicDynaBean log : importlogList) {

      if (log.get("status").equals("Empty")) {
        continue; // ignore the empty voucher statuses
      }

      String dbFileName = (String) log.get("file_name");
      for (String folder : folders) {
        File dir = new File(log.get("export_dir") + File.separator + folder);

        String[] files = dir.list(new FileNameStartsWithFilter(dbFileName));
        if (files != null && files.length != 0) {
          String message = "";
          String status = "";

          for (String fileName : files) {
            /*
             * here chance of coming one original file and one or more error files. since we dont
             * know the order of files, the status column may filled up in the first section or in
             * the second if section for Empty statuses. Once the status is filled up with the
             * 'Empty' do not override it with the other statuses even though it is in done folder.
             */
            if (dbFileName.equals(fileName) && !status.equals("Empty")) {
              if (folder.equals("input")) {
                status = "Pending";
              } else if (folder.equals("done")) {
                status = "Success";
              } else if (folder.equals("error")) {
                status = "Error";
              }
            }
            // looking for export details file, if found get the error code from the file name
            // and save the error message for that error code.
            if (fileName.startsWith(dbFileName + "_")) {
              String errorCode = fileName.replace(dbFileName + "_", "");
              if (Integer.parseInt(errorCode) == ErrorCodes.EMPTY_XML.getErrorCode()) {
                status = "Empty"; // update status with Empty when no vouchers found in the xml.
              }
              for (ErrorCodes code : ErrorCodes.values()) {
                if (code.getErrorCode() == Integer.parseInt(errorCode)) {
                  message = code.getMsg();
                }
              }

            }
          }
          // if file found in dir, updating the status and message.
          if (!status.equals("")) {
            BasicDynaBean importStatusBean = getBean(con);
            importStatusBean.set("status", status);
            importStatusBean.set("message", message);
            if (update(con, importStatusBean.getMap(), "export_no", log.get("export_no")) == 0) {
              return false;
            }
            break; // found the file in this folder and update, so break the folders for loop
          }
        }
      }
    }
    return true;
  }

  /** The Constant DELETE_LOG. */
  private static final String DELETE_LOG = "DELETE FROM accounting_xml_export_import_log "
      + " WHERE export_no=?";

  /** The Constant DELETE_VOUCHER_DETAILS. */
  private static final String DELETE_VOUCHER_DETAILS = "DELETE FROM accounting_voucher_details "
      + " WHERE export_no=?";

  /**
   * Delete details.
   *
   * @param con
   *          the con
   * @param exportNo
   *          the export no
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean deleteDetails(Connection con, int exportNo) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(DELETE_LOG);
      ps.setInt(1, exportNo);
      if (ps.executeUpdate() != 0) {
        ps = con.prepareStatement(DELETE_VOUCHER_DETAILS);
        ps.setInt(1, exportNo);
        return ps.executeUpdate() != 0;
      } else {
        return false;
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

}
