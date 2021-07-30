package com.insta.hms.master.DietaryMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FtlHelper;
import com.lowagie.text.DocumentException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DietaryFtlHealper extends FtlHelper{

	private Configuration cfg;

	public DietaryFtlHealper (Configuration cfg){
		this.cfg = cfg;
	}

	public byte[] getDietaryTrendReport(Connection con, Map params, Object out)
			throws SQLException, IOException, TemplateException,
			DocumentException {

		String format = (String) params.get("format");
		java.sql.Date fromDate = (java.sql.Date) params.get("fromDate");
		java.sql.Date toDate = (java.sql.Date) params.get("toDate");

		List<BasicDynaBean> countList = new DietaryMasterDAO().getMealsCount(fromDate, toDate);
		List<BasicDynaBean> revenueList = new DietaryMasterDAO().getMealsRevenue(fromDate, toDate);

		Map amountMap = ConversionUtils.listBeanToMapMapNumeric(revenueList,"meal_name", "time", "total_amount");
		Map countMap = ConversionUtils.listBeanToMapMapNumeric(countList,"time", "meal_name", "count");

		List<String> mealNames = new ArrayList<String>();
		mealNames.addAll(amountMap.keySet());

		List<String> mealTimes = new ArrayList<String>();
		mealTimes.addAll(countMap.keySet());

		params.put("mealNames", mealNames);
		params.put("mealTimes", mealTimes);

		Collections.sort(mealNames, new ConversionUtils.ByTotal(amountMap));
		Collections.sort(mealTimes, new ConversionUtils.ByTotal(countMap));

		params.put("amountMap", amountMap);
		params.put("countMap", countMap);
		params.put("curDateTime", DataBaseUtil.timeStampFormatter.format(new java.util.Date()));

		params.put("mealList", new DietaryMasterDAO().listAll());

		Template t = cfg.getTemplate("MealsTrend.ftl");

		return getFtlReport(t, params, format, out);
	}

}
