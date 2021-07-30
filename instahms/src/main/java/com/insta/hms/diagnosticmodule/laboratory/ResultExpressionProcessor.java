package com.insta.hms.diagnosticmodule.laboratory;

import com.insta.hms.diagnosticsmasters.addtest.TestResultsDAO;

import freemarker.core.InvalidReferenceException;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultExpressionProcessor {
	static Logger logger = LoggerFactory.getLogger(ResultExpressionProcessor.class);

	public boolean validExpr;
	public boolean isValidExpr() {
		return validExpr;
	}
	public void setValidExpr(boolean validExpr) {
		this.validExpr = validExpr;
	}
    
	// Method name can be generic as this method can be used where default value should not be zero.
	public static String processResultExpressionForLAB(List<String> resultlabel,List<String> values,String expression)throws Exception{

		StringWriter writer = new StringWriter();
		expression = "<#setting number_format=\"##.##\">\n" + expression;
		try{
			Template expressionTemplate = new Template("expression", new StringReader(expression),
		               new Configuration());
			HashMap<String, Object> resultParams = new HashMap<String, Object>();
			Map<String, Object> results = new HashMap<String, Object>();
			List<Object> numericValues = new ArrayList<Object>();
			for(int i = 0;i<resultlabel.size();i++){
				try{
					resultParams.put(resultlabel.get(i).trim(), values.get(i).isEmpty() ? "" : new BigDecimal(values.get(i)));
					numericValues.add(values.get(i).isEmpty() ? "": new BigDecimal(values.get(i)));
				}catch(NumberFormatException ne){
					resultParams.put(resultlabel.get(i).trim(), "");
					numericValues.add("");
				}

			}
			results.put("results", resultParams);
			results.put("values", numericValues);
			expressionTemplate.process(results, writer);

		}catch (TemplateException e) {
			logger.error("", e);
			return "";
		}catch (ParseException e) {
			logger.error("", e);
			return "";
		}catch (ArithmeticException e) {
			logger.error("", e);
			return "";
		}catch(Exception e){
			logger.error("", e);
			return "";
		}
		return writer.toString().trim();

	}
	
	

	public static String processResultExpression(List<String> resultlabel,List<String> values,String expression)throws Exception{

		StringWriter writer = new StringWriter();
		expression = "<#setting number_format=\"##.##\">\n" + expression;
		try{
			Template expressionTemplate = new Template("expression", new StringReader(expression),
		               new Configuration());
			HashMap<String, Object> resultParams = new HashMap<String, Object>();
			Map<String, Object> results = new HashMap<String, Object>();
			List<BigDecimal> numericValues = new ArrayList<BigDecimal>();
			for(int i = 0;i<resultlabel.size();i++){
				try{
					resultParams.put(resultlabel.get(i).trim(), new BigDecimal(values.get(i).isEmpty() ?"0":values.get(i)));
					numericValues.add(new BigDecimal(values.get(i).isEmpty()?"0":values.get(i)));
				}catch(NumberFormatException ne){
					resultParams.put(resultlabel.get(i).trim(), new BigDecimal(0));
					numericValues.add(new BigDecimal(0));
				}

			}
			results.put("results", resultParams);
			results.put("values", numericValues);
			expressionTemplate.process(results, writer);

		}catch (TemplateException e) {
			logger.error("", e);
			return "0";
		}catch (ParseException e) {
			logger.error("", e);
			return "0";
		}catch (ArithmeticException e) {
			logger.error("", e);
			return "0";
		}catch(Exception e){
			logger.error("", e);
			return "0";
		}
		return writer.toString().trim();

	}
	
	
	public boolean istExpressionValid(Connection con,String test_id,String expression)throws ArithmeticException,Exception{
		boolean valid = false;
		TestResultsDAO rdao = new TestResultsDAO();
		List<BasicDynaBean> resultsMaster = rdao.getResultsList(con, test_id);
		BasicDynaBean resultMasterBean = null;
		StringWriter writer = new StringWriter();

		try{
			expression = "<#setting number_format=\"##.##\">\n" + expression;
			Template expressionTemplate = new Template("expression", new StringReader(expression),
		               new Configuration());
			HashMap<String, Object> resultParams = new HashMap<String, Object>();
			Map<String, Object> results = new HashMap<String, Object>();
			List values = new ArrayList();
			for(int i = 0;i< resultsMaster.size();i++){
				resultMasterBean = resultsMaster.get(i);
				resultParams.put((String)resultMasterBean.get("resultlabel"), 1);
				values.add(1);
			}
			results.put("results", resultParams);
			results.put("values", values);

				expressionTemplate.process(results, writer);
		}catch (InvalidReferenceException ine) {
			logger.error("", ine);
			return false;
		}catch (TemplateException e) {
			logger.error("", e);
			return false;
		}catch (ParseException e) {
			logger.error("", e);
			return false;
		}catch (ArithmeticException e) {
			logger.error("", e);
			return false;
		}catch(Exception e){
			logger.error("", e);
			return false;
		}
		valid = !writer.toString().contains("[^.\\d]");

		try{
			if(!writer.toString().trim().isEmpty()){
				BigDecimal validNumber = new BigDecimal(writer.toString());
			}
		}catch(NumberFormatException ne){
			logger.error("", ne);
			valid = false;
		}
		return valid;
	}
	
	public boolean istExpressionValid(List<BasicDynaBean> resultsMatser, String test_id,String expression) {
		boolean valid = false;
		//TestResultsDAO rdao = new TestResultsDAO();
		//List<BasicDynaBean> resultsMaster = rdao.getResultsList(con, test_id);
		BasicDynaBean resultMasterBean = null;
		StringWriter writer = new StringWriter();

		try{
			expression = "<#setting number_format=\"##.##\">\n" + expression;
			Template expressionTemplate = new Template("expression", new StringReader(expression),
		               new Configuration());
			HashMap<String, Object> resultParams = new HashMap<String, Object>();
			Map<String, Object> results = new HashMap<String, Object>();
			List values = new ArrayList();
			for(int i = 0;i< resultsMatser.size();i++){
				resultMasterBean = resultsMatser.get(i);
				resultParams.put((String)resultMasterBean.get("resultlabel"), 1);
				values.add(1);
			}
			results.put("results", resultParams);
			results.put("values", values);

				expressionTemplate.process(results, writer);
		}catch (InvalidReferenceException ine) {
			logger.error("", ine);
			return false;
		}catch (TemplateException e) {
			logger.error("", e);
			return false;
		}catch (ParseException e) {
			logger.error("", e);
			return false;
		}catch (ArithmeticException e) {
			logger.error("", e);
			return false;
		}catch(Exception e){
			logger.error("", e);
			return false;
		}
		valid = !writer.toString().contains("[^.\\d]");

		try{
			if(!writer.toString().trim().isEmpty()){
				BigDecimal validNumber = new BigDecimal(writer.toString());
			}
		}catch(NumberFormatException ne){
			logger.error("", ne);
			valid = false;
		}
		return valid;
	}
}
