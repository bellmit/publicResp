package com.insta.hms.diagnosticsmasters.addtest;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.laboratory.ResultExpressionProcessor;
import com.insta.hms.diagnosticsmasters.Result;
import com.insta.hms.diagnosticsmasters.ResultRangesDAO;
import com.insta.hms.diagnosticsmasters.Test;
import com.insta.hms.diagnosticsmasters.TestTemplate;
import com.insta.hms.master.DiagCenterResultsApplicability.DiagResultsCenterApplicabilityDAO;

import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class TestBO.
 */
public class TestBO {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(TestBO.class);
  
  /** The diag dao. */
  DiagResultsCenterApplicabilityDAO diagDao = new DiagResultsCenterApplicabilityDAO();

  /**
   * Adds the new test.
   *
   * @param test the test
   * @param results the results
   * @param templateList the template list
   * @param msg the msg
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean addNewTest(Test test, ArrayList<Result> results,
      ArrayList<TestTemplate> templateList, StringBuilder msg) throws Exception {

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    AddTestDAOImpl dao = new AddTestDAOImpl(con);
    boolean success = true;
    ResultExpressionProcessor processor = new ResultExpressionProcessor();
    int centerId = RequestContext.getCenterId();

    try {
      outer: do {
        String genTestId = dao.getNextTestId();
        test.setTestId(genTestId);
        success = dao.insertTest(test);
        if (!success) {
          break;
        }
        success = dao.insertHl7Interfaces(test.getTestId(), test.getInterface_name(),
            test.getItem_type());
        if (!success) {
          break;
        }
        Iterator<Result> it = results.iterator();
        if (null != test.getReportGroup() && test.getReportGroup().equals("V") && it.hasNext()) {
          while (it.hasNext()) {
            Result res = it.next();
            res.setTestId(genTestId);
            List centersList = new ArrayList();
            Integer resultLabelId = Integer.parseInt(res.getResultlabel_id());
            centersList = ResultRangesDAO.CentersForResults(con, res.getTestId(), resultLabelId);

            if (!res.getExpression().isEmpty()) {
              processor.setValidExpr(
                  processor.istExpressionValid(con, res.getTestId(), res.getExpression()));
              success = processor.isValidExpr();

              if (!success) {
                msg = msg.append("The expression for result : " + res.getResultLabel() + " is "
                    + res.getExpression() + " ," + "which is not a valid expression.");
                break;
              }
            }
          }
          success &= dao.insertResults(results);
          success &= diagDao.insertResultsCenter(results, success, con);
          if (!success) {
            break;
          }
        }

        if (null != test.getReportGroup() && test.getReportGroup().equals("V")) {
          processor.setValidExpr(
              processor.istExpressionValid(con, test.getTestId(), test.getResultsValidation()));

          success = processor.isValidExpr();
          if (!success) {
            msg.append("The expression for Results Validation: " + test.getResultsValidation()
                + " is not a valid expression.");
            break;
          }

          Map values = new HashMap<String, String>();
          values.put("results_validation", test.getResultsValidation().trim());

          success &= new GenericDAO("diagnostics").update(con, values, "test_id",
              test.getTestId()) > 0;
          if (!success) {
            break;
          }
        }

        Iterator<TestTemplate> tempit = templateList.iterator();
        if (null != test.getReportGroup() && test.getReportGroup().equals("T")
            && tempit.hasNext()) {
          while (tempit.hasNext()) {
            TestTemplate tt = tempit.next();
            tt.setTestId(genTestId);
            success = dao.insertTemplates(tt);
            if (!success) {
              break outer;
            }
          }
        }

        success &= new TestChargesDAO().initItemCharges(con, genTestId, test.getUserName());
        if (!success) {
          break;
        }
        success &= new TestTATDAO("diag_tat_center_master").addTestTATCenters(con,
            test.getTestId());
        if (!success) {
          msg.append("Failed to add the Tat's for center..");
        }

      } while (false);

      logger.debug("Success value is " + success);
      if (success) {
        dao.updateDiagnosticTimeStamp();
      }

    } catch (Exception ecp) {
      success = false;
      throw (ecp);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /**
   * Update test details.
   *
   * @param test the test
   * @param addedResults the added results
   * @param modifiedResults the modified results
   * @param deletedResults the deleted results
   * @param templateList the template list
   * @param msg the msg
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   * @throws Exception the exception
   */
  public boolean updateTestDetails(Test test, ArrayList<Result> addedResults,
      ArrayList<Result> modifiedResults, ArrayList<Result> deletedResults,
      ArrayList<TestTemplate> templateList, StringBuilder msg)
      throws SQLException, IOException, ParseException, Exception {

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    AddTestDAOImpl dao = new AddTestDAOImpl(con);
    boolean success = true;
    ResultExpressionProcessor processor = new ResultExpressionProcessor();
    int centerId = RequestContext.getCenterId();

    try {
      outer: do {
        if (null != test.getReportGroup() && test.getReportGroup().equals("V")) {
          if (!test.getResultsValidation().isEmpty()) {
            processor.setValidExpr(
                processor.istExpressionValid(con, test.getTestId(), test.getResultsValidation()));
            success = processor.isValidExpr();
            if (!success) {
              msg = msg.append("The expression for Results Validation : "
                  + test.getResultsValidation() + " is not valid.");
              break;
            }
          }
        }

        success = dao.updateTest(test);
        if (!success) {
          break;
        }

        success = dao.updateHl7Interface(test.getTestId(), test.getInterface_name(),
            test.getItem_type(), test.getHl7_mapping_deleted());

        if (!success) {
          break;
        }

        if (null != test.getReportGroup() && test.getReportGroup().equals("V")) {

          success &= dao.updateResults(modifiedResults);
          if (!success) {
            break;
          }

          success &= dao.deleteResults(deletedResults);
          /* result range with out a result label can not exist */
          success &= dao.deleteResultRanges(con, deletedResults);
          success &= diagDao.deleteResultsCenter(deletedResults, success, con);
          if (!success) {
            break;
          }

          success &= dao.insertResults(addedResults);
          success &= diagDao.insertResultsCenter(addedResults, success, con);
          if (!success) {
            break;
          }

          Iterator<Result> it = modifiedResults.iterator();
          Result res = null;

          while (it.hasNext()) {
            res = it.next();
            List centersList = new ArrayList();
            Integer resultLabelId = Integer.parseInt(res.getResultlabel_id());
            centersList = ResultRangesDAO.CentersForResults(con, res.getTestId(), resultLabelId);
            if (!res.getExpression().isEmpty()) {
              processor.setValidExpr(
                  processor.istExpressionValid(con, res.getTestId(), res.getExpression()));
              success = processor.isValidExpr();
              if (!success) {
                msg = msg.append("The expression for result : " + res.getResultLabel() + " is "
                    + res.getExpression() + " ," + "which is not a valid expression.");
                break;
              }
            }
          }
          if (!success) {
            break;
          }

          it = addedResults.iterator();

          while (it.hasNext()) {
            res = it.next();
            List<Integer> centersList = new ArrayList();
            centersList.add(RequestContext.getCenterId());
            if (!res.getExpression().isEmpty()) {
              processor.setValidExpr(
                  processor.istExpressionValid(con, res.getTestId(), res.getExpression()));
              success = processor.isValidExpr();

              if (!success) {
                msg = msg.append("The expression for the result: " + res.getResultLabel() + " is "
                    + res.getExpression() + " ," + " which is not a valid expression.");
                break;
              }
            }
          }
          if (!success) {
            break;
          }

        } else {
          success = dao.deleteTemplates(test.getTestId());
          Iterator<TestTemplate> tempit = templateList.iterator();
          if (null != test.getReportGroup() && test.getReportGroup().equals("T")
              && tempit.hasNext()) {
            while (tempit.hasNext()) {
              TestTemplate tt = tempit.next();
              success = dao.insertTemplates(tt);
              if (!success) {
                break outer;
              }
            }
          }
        }

      } while (false);
      if (success) {
        dao.updateDiagnosticTimeStamp();
      }

      if (success) {
        success = resultsCenterApplicabilityCheck(con, test.getTestId(), msg);
      }

    } catch (SQLException ecp) {
      success = false;
      throw (ecp);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /**
   * Edits the test charges.
   *
   * @param orgId the org id
   * @param testid the testid
   * @return the map
   * @throws SQLException the SQL exception
   */
  public Map editTestCharges(String orgId, String testid) throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    AddTestDAOImpl dao = new AddTestDAOImpl(con);
    Map map = dao.editTestCharges(orgId, testid);
    con.close();

    return map;
  }

  /**
   * Update test charge.
   *
   * @param tclist the tclist
   * @param testId the test id
   * @param orgId the org id
   * @param disabled the disabled
   * @param orgItemCode the org item code
   * @param codeType the code type
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateTestCharge(ArrayList<TestCharge> tclist, String testId, String orgId,
      boolean disabled, String orgItemCode, String codeType) throws SQLException {
    boolean status = false;

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    AddTestDAOImpl dao = new AddTestDAOImpl(con);

    status = dao.addOREditTestCharges(tclist);

    TestCharge itemCode = new TestCharge();
    itemCode.setApplicable(disabled);
    itemCode.setOrgItemCode(orgItemCode);
    itemCode.setOrgId(orgId);
    itemCode.setTestId(testId);
    itemCode.setCodeType(codeType);
    ArrayList<TestCharge> codeList = new ArrayList<>();
    codeList.add(itemCode);
    status = dao.addOREditItemCode(codeList);
    if (status) {
      dao.updateDiagnosticTimeStamp();
    }
    DataBaseUtil.commitClose(con, status);

    return status;
  }

  /**
   * Update test charge list.
   *
   * @param chargeList the charge list
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateTestChargeList(ArrayList<TestCharge> chargeList) throws SQLException {
    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      AddTestDAOImpl dao = new AddTestDAOImpl(con);
      success = dao.updateTestChargeList(chargeList);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /**
   * Result label map.
   *
   * @param con the con
   * @param testId the test id
   * @return the map
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public Map resultLabelMap(Connection con, String testId) throws SQLException, Exception {

    HashMap<String, List<Integer>> resultlabelMap = new HashMap<>();
    List<BasicDynaBean> resultList = new TestResultsDAO().getResultsList(con, testId);
    for (int i = 0; i < resultList.size(); i++) {
      if (resultlabelMap.get(resultList.get(i).get("resultlabel")) != null) {
        List centerList = resultlabelMap.get(resultList.get(i).get("resultlabel"));
        centerList.add(resultList.get(i).get("center_id"));
      } else {
        List<Integer> list = new ArrayList<>();
        list.add((Integer) resultList.get(i).get("center_id"));
        resultlabelMap.put((String) resultList.get(i).get("resultlabel"), list);
      }
    }
    return resultlabelMap;
  }

  /**
   * Results center applicability check.
   *
   * @param con the con
   * @param testID the test ID
   * @param msg the msg
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean resultsCenterApplicabilityCheck(Connection con, String testID, StringBuilder msg)
      throws Exception {
    boolean isValid = true;
    List<BasicDynaBean> resultList = new TestResultsDAO().getResultsList(con, testID);
    for (int i = 0; i < resultList.size(); i++) {
      BasicDynaBean bean = resultList.get(i);
      if (null != bean.get("expr_4_calc_result") && !bean.get("expr_4_calc_result").equals("")) {
        isValid = istExpressionValid(con, testID, (String) bean.get("expr_4_calc_result"),
            (Integer) bean.get("center_id"));
        if (!isValid) {
          msg = msg.append("The result lables which is involved in the expression : "
              + (String) bean.get("expr_4_calc_result") + " OR the resullabel: "
              + (String) bean.get("resultlabel") + " are mapped to different centers.");
          break;
        }
      }
    }
    return isValid;
  }

  /**
   * Checks if is t expression valid.
   *
   * @param con the con
   * @param testId the test id
   * @param expression the expression
   * @param centerID the center ID
   * @return true, if is t expression valid
   * @throws ArithmeticException the arithmetic exception
   * @throws Exception the exception
   */
  public boolean istExpressionValid(Connection con, String testId, String expression, int centerID)
      throws ArithmeticException, Exception {
    boolean valid = false;
    TestResultsDAO rdao = new TestResultsDAO();
    List<BasicDynaBean> resultsMaster = rdao.getResultsListForExpr(con, testId, centerID);
    BasicDynaBean resultMasterBean = null;
    StringWriter writer = new StringWriter();

    try {
      expression = "<#setting number_format=\"##.##\">\n" + expression;
      HashMap<String, Object> resultParams = new HashMap<>();
      Map<String, Object> results = new HashMap<>();
      List values = new ArrayList();
      for (int i = 0; i < resultsMaster.size(); i++) {
        resultMasterBean = resultsMaster.get(i);
        resultParams.put((String) resultMasterBean.get("resultlabel"), 1);
        values.add(1);
      }
      results.put("results", resultParams);
      results.put("values", values);
      Template expressionTemplate = new Template("expression", new StringReader(expression),
          new Configuration());
      expressionTemplate.process(results, writer);
    } catch (InvalidReferenceException ine) {
      logger.error("", ine);
      return false;
    } catch (TemplateException ecp) {
      logger.error("", ecp);
      return false;
    } catch (freemarker.core.ParseException ecp) {
      logger.error("", ecp);
      return false;
    } catch (ArithmeticException ecp) {
      logger.error("", ecp);
      return false;
    } catch (Exception ecp) {
      logger.error("", ecp);
      return false;
    }
    valid = !writer.toString().contains("[^.\\d]");

    try {
      if (!writer.toString().trim().isEmpty()) {
        BigDecimal validNumber = new BigDecimal(writer.toString());
      }
    } catch (NumberFormatException ne) {
      logger.error("", ne);
      valid = false;
    }
    return valid;
  }
}
