package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingHospitalBillsExporter.
 *
 * @author krishna
 */
public class AccountingHospitalBillsExporter extends GenericAccountingExporter {

  /** The dept dao. */
  DepartmentMasterDAO deptDao = new DepartmentMasterDAO();

  /** The Constant THIS_VOUCHER_TYPE. */
  private static final String THIS_VOUCHER_TYPE = "HOSPITAL_BILLS";

  /** The dept map. */
  private Map<String, Map> deptMap = new HashMap<String, Map>();

  /** The settings map. */
  private Map<String, Map> settingsMap = new HashMap<String, Map>();

  /**
   * Instantiates a new accounting hospital bills exporter.
   *
   * @param format
   *          the format
   */
  public AccountingHospitalBillsExporter(String format) {
    super(THIS_VOUCHER_TYPE, "bill_no", "voucher_date", "bills", "bill_vtype", "HOSPBILL", format);

  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#export(int, java.util.List,
   *      java.io.OutputStream)
   */
  @Override
  protected void export(int centerId, List voucherList, OutputStream stream) throws SQLException,
      IOException, TemplateException, ClassNotFoundException {

    logger.info("Processing vouchers started @ :" + new Date());

    String voucherNoField = getVoucherNumberField();
    String previousVoucherNo = "";
    BasicDynaBean prevBean = null;
    VoucherProcessor aggregator = null;

    Connection con = DataBaseUtil.getConnection(60);
    Object[] obj = null;
    BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
    ResultSet rs = null;
    try {
      // this is used to improve the performance of the query. enable it while closing the
      // connection.
      if (genPrefs.get("enable_nestloop").equals("N")) {
        DataBaseUtil.setNestedLoops(con, false);
      }
      obj = ChargeDAO.getHospitalBillSummary(con, fromDate, toDate, accountGroup, centerId,
          voucherList);
      rs = (ResultSet) obj[1];
      List<BasicDynaBean> recordsList = (List<BasicDynaBean>) obj[2];
      Iterator<BasicDynaBean> itr = recordsList.iterator();

      while (itr.hasNext()) {
        BasicDynaBean record = itr.next();

        String voucherNo = record.get(voucherNoField).toString();

        // Once we hit a new voucher no - it means the old one is complete
        // So we send the previous voucher to the XML
        if (!previousVoucherNo.equalsIgnoreCase(voucherNo)) {
          if (!previousVoucherNo.equals("") && prevBean != null) {
            sendVoucher(aggregator, prevBean.getMap(), stream);
          }
          // Start a new aggregator for the new voucher so that aggregation can start afresh.
          aggregator = getVoucherProcessor(voucherNo);
        }

        processTransaction(record.getMap(), voucherNoField, aggregator);
        // if it is a last voucher, then process and send the voucher.
        if (!itr.hasNext()) {
          sendVoucher(aggregator, record.getMap(), stream);
        }

        // setup the previous vocuher for comparison in the next loop
        previousVoucherNo = voucherNo;
        prevBean = record;
      }

    } finally {
      PreparedStatement ps = null;
      if (obj != null) {
        ps = (PreparedStatement) obj[0];
      }
      if (genPrefs.get("enable_nestloop").equals("N")) {
        DataBaseUtil.setNestedLoops(con, true);
      }
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    logger.info("Processing vouchers ended @ :" + new Date());
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#getSettingsMap()
   */
  @Override
  protected Map getSettingsMap() throws SQLException {
    String schema = RequestContext.getSchema();
    Map schemaSettingsMap = settingsMap.get(schema);
    if (schemaSettingsMap != null) {
      return schemaSettingsMap;
    }

    String hospDiscAccountHead = ChargeDAO.getAccountHead("BIDIS");
    schemaSettingsMap = new HashMap<String, Object>();
    schemaSettingsMap.put("hospital_discount_account", hospDiscAccountHead);

    settingsMap.put(schema, schemaSettingsMap);

    return schemaSettingsMap;
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#getDeptsMap()
   */
  @Override
  protected Map getDeptsMap() throws SQLException {
    String schema = RequestContext.getSchema();
    Map schemaDeptMap = deptMap.get(schema);

    if (schemaDeptMap != null) {
      return schemaDeptMap;
    }

    List cols = new ArrayList();
    cols.add("dept_id");
    cols.add("dept_name");
    cols.add("cost_center_code");

    List colsList = deptDao.listAll(cols);
    schemaDeptMap = ConversionUtils.listBeanToMapMap(colsList, "dept_id");

    deptMap.put(schema, schemaDeptMap);

    return schemaDeptMap;
  }

  /**
   * @see com.insta.hms.billing.accounting.GenericAccountingExporter#getTransactions(int,
   *      java.util.List)
   */
  @Override
  protected List<BasicDynaBean> getTransactions(int centerId, List voucherList) 
      throws SQLException {
    return null;
  }

}
