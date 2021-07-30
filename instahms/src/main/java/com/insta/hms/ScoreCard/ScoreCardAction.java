/**
 *
 */
package com.insta.hms.ScoreCard;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.lowagie.text.DocumentException;
import flexjson.JSONSerializer;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ScoreCardAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(ScoreCardAction.class);
	private static final GenericDAO scoreCardMainDAO = new GenericDAO("score_card_main");
	private static final GenericDAO scoreCardDetailsDAO = new GenericDAO("score_card_details");

	JSONSerializer json = new JSONSerializer().exclude("class");

	@IgnoreConfidentialFilters
	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, Exception {
		return mapping.findForward("show");
	}
	/***
	 * Inner class "Score" to capture the variations/dependencies in score range calculation.
	 * > Some ranges are gender dependent
	 * > Some are Age dependent
	 * > Some are dependent only on the min-max values
	 * > Some are dependent on all of the above
	 *
	 */
	public static class Score {
		// age and gender independent range score
		public Score(String name, String severity, BigDecimal minVal, BigDecimal maxVal) {
			this.name = name;
			this.severity = severity;
			this.minValue = minVal;
			this.maxValue = maxVal;
			this.ageIgnored = true;
			this.genderIgnored = true;
		}

		//Score independent of gender, but dependent on age
		public Score(String name, String severity, Integer minAge, Integer maxAge, BigDecimal minVal,
				BigDecimal maxVal) {
			this(name, severity, minVal, maxVal);
			this.minAge = minAge;
			this.maxAge = maxAge;
			this.ageIgnored = false;
			this.genderIgnored = true;
		}

		//Score independent of age, but dependent on gender
		public Score(String name, String severity, String gender, BigDecimal minVal, BigDecimal maxVal) {
			this(name, severity, minVal, maxVal);
			this.gender = gender;
			this.ageIgnored = true;
			this.genderIgnored = false;
		}

		//Score dependent on gender and age
		public Score(String name, String severity, Integer minAge, Integer maxAge, String gender,
				BigDecimal minVal, BigDecimal maxVal) {
			this(name, severity, minVal, maxVal);
			this.minAge = minAge;
			this.maxAge = maxAge;
			this.gender = gender;
			this.ageIgnored = false;
			this.genderIgnored = false;
		}

		private String name;
		private String severity;
		private Integer minAge;
		private Integer maxAge;
		private Boolean ageIgnored = true;
		private Boolean genderIgnored = true;
		private String gender;
		private BigDecimal minValue;
		private BigDecimal maxValue;

		String getGender() {
			return gender;
		}

		void setGender(String gender) {
			this.gender = gender;
		}

		Integer getMaxAge() {
			return maxAge;
		}

		void setMaxAge(Integer maxAge) {
			this.maxAge = maxAge;
		}

		BigDecimal getMaxValue() {
			return maxValue;
		}

		void setMaxValue(BigDecimal maxValue) {
			this.maxValue = maxValue;
		}

		Integer getMinAge() {
			return minAge;
		}

		void setMinAge(Integer minAge) {
			this.minAge = minAge;
		}

		BigDecimal getMinValue() {
			return minValue;
		}

		void setMinValue(BigDecimal minValue) {
			this.minValue = minValue;
		}

		String getName() {
			return name;
		}

		void setName(String name) {
			this.name = name;
		}

		String getSeverity() {
			return severity;
		}

		void setSeverity(String severity) {
			this.severity = severity;
		}
	}

	public static final String Acheived = "green";
	public static final String BorderLine = "yellow";
	public static final String Higher = "red";
	public static final String NotConducted = "NotConducted";

	/***
	 * Intitializes the range map for dialysis card.
	 */

	static Map<String, ArrayList> rangeMap = new HashMap<String, ArrayList>();

	public static void initializeScoreMap() {

		ArrayList<Score> ktv = new ArrayList<Score>();
		// initialize ktv values
		ktv.add(new Score("ktv", Acheived, new BigDecimal("1.2"), new BigDecimal("2.5")));
		ktv.add(new Score("ktv", BorderLine, new BigDecimal("0"), new BigDecimal("1.199")));
		ktv.add(new Score("ktv", Higher, new BigDecimal("2.51"), new BigDecimal("9999")));
		rangeMap.put("ktv", ktv);

		ArrayList<Score> urr = new ArrayList<Score>();
		urr.add(new Score("urr", Acheived, new BigDecimal("65"), new BigDecimal("9999")));
		urr.add(new Score("urr", BorderLine, new BigDecimal("0"), new BigDecimal("64.99")));
		rangeMap.put("urr", urr);

		ArrayList<Score> blood = new ArrayList<Score>();
		blood.add(new Score("blood", Acheived, new BigDecimal("250"), new BigDecimal("500")));
		blood.add(new Score("blood", BorderLine, new BigDecimal("0"), new BigDecimal("249.99")));
		blood.add(new Score("blood", Higher, new BigDecimal("500.1"), new BigDecimal("9999")));
		rangeMap.put("blood", blood);

		ArrayList<Score> hb = new ArrayList<Score>();
		hb.add(new Score("hb", Acheived, "M",new BigDecimal("13"), new BigDecimal("9999")));
		hb.add(new Score("hb", BorderLine,"M", new BigDecimal("9.1"), new BigDecimal("12.99")));
		hb.add(new Score("hb", Higher, "M",new BigDecimal("0"), new BigDecimal("9")));
		hb.add(new Score("hb", Acheived, "F",new BigDecimal("12"), new BigDecimal("9999")));
		hb.add(new Score("hb", BorderLine,"F", new BigDecimal("9.1"), new BigDecimal("11.99")));
		hb.add(new Score("hb", Higher, "F",new BigDecimal("0"), new BigDecimal("9")));
		rangeMap.put("hb", hb);

		ArrayList<Score> weight = new ArrayList<Score>();
		weight.add(new Score("weight", Acheived, new BigDecimal("0"), new BigDecimal("4.99")));
		weight.add(new Score("weight", Higher, new BigDecimal("5"), new BigDecimal("9999")));
		rangeMap.put("weight", weight);

		// no range for blood pressure, thereby set to null
		rangeMap.put("bp", null);

		ArrayList<Score> albumin = new ArrayList<Score>();
		albumin.add(new Score("albumin", Acheived, new BigDecimal("4.0"), new BigDecimal("9999")));
		albumin.add(new Score("albumin", BorderLine, new BigDecimal("0"), new BigDecimal("3.5")));
		rangeMap.put("albumin", albumin);

		ArrayList<Score> protein = new ArrayList<Score>();
		protein.add(new Score("protein", Acheived, new BigDecimal("6"), new BigDecimal("8.5")));
		protein.add(new Score("protein", Higher, new BigDecimal("9"), new BigDecimal("9999")));
		rangeMap.put("protein", protein);

		ArrayList<Score> potassium = new ArrayList<Score>();
		potassium.add(new Score("potassium", Acheived, new BigDecimal("3.5"), new BigDecimal("5.5")));
		potassium.add(new Score("potassium", Higher, new BigDecimal("6"), new BigDecimal("9999")));
		potassium.add(new Score("potassium", BorderLine, new BigDecimal("0"), new BigDecimal("3.4")));
		rangeMap.put("potassium", potassium);

		ArrayList<Score> caxpo = new ArrayList<Score>();
		caxpo.add(new Score("caxpo", Acheived, new BigDecimal("0"), new BigDecimal("70")));
		caxpo.add(new Score("caxpo", Higher, new BigDecimal("70.1"), new BigDecimal("9999")));
		rangeMap.put("caxpo", caxpo);

		ArrayList<Score> pth = new ArrayList<Score>();
		pth.add(new Score("pth", Acheived, new BigDecimal("150"), new BigDecimal("300")));
		pth.add(new Score("pth", Higher, new BigDecimal("300.1"), new BigDecimal("9999")));
		pth.add(new Score("pth", BorderLine, new BigDecimal("0"), new BigDecimal("149.9")));
		rangeMap.put("pth", pth);

		ArrayList<Score> ca = new ArrayList<Score>();
		ca.add(new Score("ca", Acheived, new BigDecimal("8.8"), new BigDecimal("9.5")));
		ca.add(new Score("ca", Higher, new BigDecimal("9.6"), new BigDecimal("9999")));
		ca.add(new Score("ca", BorderLine, new BigDecimal("0"), new BigDecimal("8.7")));
		rangeMap.put("ca", ca);

	}

	public static Map<String, Object> valueMap = new HashMap<String, Object>();

	public static Map<String, String> severityMap = new HashMap<String, String>();

	static String[] targetNames = { "ktv", "urr", "blood", "hb", "weight", "bp", "albumin", "protein",
			"potassium", "caxpo", "pth", "ca" };

	/***
	 * This fetches the recorded score value of a patient based on mr_no and month
	 */

	public static Map getDBScoresForMrNo(String mrNo, java.sql.Date fd, java.sql.Date td) throws Exception {
		initializeScoreMap();
		valueMap = new HashMap();
		valueMap.put(targetNames[0], ScoreCardDAO.getKtv(mrNo, fd, td));
		valueMap.put(targetNames[1], ScoreCardDAO.getUrr(mrNo, fd, td));
		valueMap.put(targetNames[2], ScoreCardDAO.getBloodFlow(mrNo, fd, td));
		valueMap.put(targetNames[3], ScoreCardDAO.getHB(mrNo, fd, td));
		valueMap.put(targetNames[4], ScoreCardDAO.getWeightLoss(mrNo, fd, td));
		valueMap.put(targetNames[5], ScoreCardDAO.getPostBP(mrNo, fd, td));
		valueMap.put(targetNames[6], ScoreCardDAO.getAlbumin(mrNo, fd, td));
		valueMap.put(targetNames[7], ScoreCardDAO.getTotalProtein(mrNo, fd, td));
		valueMap.put(targetNames[8], ScoreCardDAO.getPotassium(mrNo, fd, td));
		valueMap.put(targetNames[9], ScoreCardDAO.getCaxpo4(mrNo, fd, td));
		valueMap.put(targetNames[10], ScoreCardDAO.getPth(mrNo, fd, td));
		valueMap.put(targetNames[11], ScoreCardDAO.getCorrectedCalcium(mrNo, fd, td));
		return valueMap;
	}


	public static void setValueMapForMrno(String mrNo, Integer age, String gender, java.sql.Date fd,
			java.sql.Date td) throws Exception {
		severityMap = new HashMap();
		valueMap = getDBScoresForMrNo(mrNo, fd, td);
		for (int i = 0; i < targetNames.length; i++) {
			ArrayList<Score> scores = rangeMap.get(targetNames[i]);
			Object value = valueMap.get(targetNames[i]);
			Boolean severityFound = false;
			if (value != null && scores != null && !targetNames[i].equals("bp")) {
				for (int j = 0; j < scores.size(); j++) {
					Score current = scores.get(j);
					// check if age criteria is met
					if (!current.ageIgnored) {
						if (!(age > current.minAge && age < current.maxAge))
							continue;
					}
					// check if gender criteria is also met
					if (!current.genderIgnored) {
						if (!gender.equals(current.gender)) {
							continue;
						}
					}

					if ((((BigDecimal) value).compareTo(current.minValue) >= 0 && ((BigDecimal) value)
							.compareTo(current.maxValue) <= 0)
							) {
						severityMap.put(targetNames[i] + "Severity", current.severity);
						severityFound = true;

					}
				}
			}
			if (!severityFound || value == null || targetNames[i].equals("bp")) {

				if (value == null) {
					valueMap.put(targetNames[i], "NOT DONE");
					severityMap.put(targetNames[i] + "Severity", NotConducted);
				} else {
					severityMap.put(targetNames[i] + "Severity", NotConducted);
				}
			}

		}
	}
	/***
	 * Takes in a given month and date, to give the min and max dates of that particular month
	 */
	public java.sql.Date[] getDatesMonthRange(Integer month, Integer year) throws Exception {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, 1);
		int maxday = cal.getActualMaximum(cal.DAY_OF_MONTH);
		int minday = cal.getActualMinimum(cal.DAY_OF_MONTH);
		cal.set(cal.get(cal.YEAR), cal.get(Calendar.MONTH), minday);
		java.sql.Date fd = new java.sql.Date((cal.getTime()).getTime());
		cal.set(cal.get(cal.YEAR), cal.get(cal.MONTH), maxday);
		java.sql.Date td = new java.sql.Date((cal.getTime()).getTime());
		java.sql.Date[] dateArray = { fd, td };
		return dateArray;
	}

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String mrno = (String) request.getParameter("mr_no");
		Map patmap = new HashMap();
		if(mrno == null  || mrno.equals("")) {
			return mapping.findForward("list");
		} else {
			patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
			if (patmap == null) {
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", mrno + " does not exist...");
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		}
		HttpSession session = request.getSession(false);
		java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
		int roleid = (Integer) session.getAttribute("roleId");
		if ((roleid == 1 || roleid == 2) || urlRightsMap.get("score_card").equals("A")){
			request.setAttribute("editEnabled", "true");
		}

		Map paramsMap= new HashMap();
		paramsMap.putAll(request.getParameterMap());
		paramsMap.put("mr_no", new String[] {mrno});
		PagedList pList = ScoreCardDAO.searchScoreCardMain(paramsMap, ConversionUtils.getListingParameter(paramsMap));
		request.setAttribute("pagedList", pList);
		request.setAttribute("mr_no", mrno);

		return mapping.findForward("list");
	}

	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String mrno = (String) request.getParameter("mr_no");
		Map patmap = new HashMap();
		if(mrno == null  || mrno.equals("")) {
			return mapping.findForward("list");
		} else {
			patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
			if (patmap == null) {
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", mrno + " does not exist...");
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("list"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		}

		boolean deleteSuccess = false;
		boolean cardFound = false;

		Integer scoreCardId = Integer.parseInt(request.getParameter("_score_card_id_to_delete"));
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean cardMain = scoreCardMainDAO.findByKey(con, "score_card_id", scoreCardId);
			cardFound = cardMain != null;
			if(cardFound) {
				deleteSuccess = scoreCardMainDAO.delete(con, "score_card_id", scoreCardId);
			}
			if(deleteSuccess) {
				deleteSuccess &= scoreCardDetailsDAO.delete(con, "score_card_id", scoreCardId);
			}
			if(!cardFound || !deleteSuccess) {
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", "Score card could not be deleted...");
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("list"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}

			HttpSession session = request.getSession(false);
			java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
			int roleid = (Integer) session.getAttribute("roleId");
			if ((roleid == 1 || roleid == 2) || urlRightsMap.get("score_card").equals("A")){
				request.setAttribute("editEnabled", "true");
			}

		} finally{
			DataBaseUtil.commitClose(con, deleteSuccess);
		}

		Map paramsMap= new HashMap();
		paramsMap.putAll(request.getParameterMap());
		paramsMap.put("mr_no", new String[]{mrno});

		PagedList pList = ScoreCardDAO.searchScoreCardMain(paramsMap, ConversionUtils.getListingParameter(paramsMap));
		request.setAttribute("pagedList", pList);
		request.setAttribute("mr_no", mrno);
		return mapping.findForward("list");
	}


	public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String mrno = (String) request.getParameter("mr_no");
		Integer cardId = request.getParameter("score_card_id") == null
					|| request.getParameter("score_card_id").equals("")? null: Integer.parseInt(request.getParameter("score_card_id"));
		boolean isUpdate = false;

		if(cardId != null && cardId!= 0){
			isUpdate = true;
		}

		Map patmap = new HashMap();
		//Check If Mr no is valid
		if (mrno != null && !mrno.equals("")) {
			patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
			if (patmap == null) {
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", mrno + " does not exist...");
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		}

		Integer monthNum = request.getParameter("month")!= null? Integer.parseInt(request.getParameter("month")) : Calendar.getInstance().get(Calendar.MONTH)+1;
		Integer year = request.getParameter("year") == null ? Calendar.getInstance().get(Calendar.YEAR)
				: Integer.parseInt(request.getParameter("year"));
		Timestamp currentDate = DataBaseUtil.getDateandTime();
		String nephrologist = request.getParameter("nephrologist");

		HttpSession session=request.getSession();
		String username = (String)session.getAttribute("userid");

		ScoreCardDAO dao = new ScoreCardDAO();
		Integer newCardId = 0;

		BasicDynaBean mainBean = scoreCardMainDAO.getBean();
		if(!isUpdate) {
			newCardId = scoreCardMainDAO.getNextSequence();
			mainBean.set("score_card_id", newCardId);
			mainBean.set("mr_no", mrno);
			mainBean.set("card_year",BigDecimal.valueOf(year) );
			mainBean.set("card_month", BigDecimal.valueOf(monthNum));
		}
		mainBean.set("save_date", currentDate);
		mainBean.set("username", username);
		mainBean.set("nephrologist", nephrologist);

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = false;
		try{
			if(isUpdate) {
				success = scoreCardMainDAO.update(con, mainBean.getMap(), "score_card_id", cardId) > 0;
			} else {
				success = scoreCardMainDAO.insert(con, mainBean);
			}

			if(!success) {
				con.rollback();
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", "Score card details could not be inserted...");
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}

			int scoreCardId = isUpdate? cardId : newCardId;

			Map<String, String> rangeMap = new HashMap<String, String>();
			Map<String, String> severityMap = new HashMap<String, String>();
			Map<String, String> valueMap = new HashMap<String, String>();

			for(int i=0; i<targetNames.length; i++) {
				String range = request.getParameter(targetNames[i]+"Range");
				String value = request.getParameter(targetNames[i]);
				String severity = request.getParameter(targetNames[i]+"Severity");

				BasicDynaBean detailBean = scoreCardDetailsDAO.getBean();

				detailBean.set("score_card_id", scoreCardId);
				detailBean.set("attribute", targetNames[i]);
				detailBean.set("severity", severity);
				detailBean.set("range", range);
				detailBean.set("value", value);

				valueMap.put(targetNames[i], value);
				severityMap.put(targetNames[i]+"Severity", severity);
				rangeMap.put(targetNames[i]+"Range", range);

				if(isUpdate) {
					success = scoreCardDetailsDAO.updateWithNames(con, detailBean.getMap(), new String[]{"score_card_id","attribute"}) > 0;
				} else {
					success = scoreCardDetailsDAO.insert(con, detailBean);
				}

				if(!success) {
					con.rollback();
					FlashScope flash = FlashScope.getScope(request);
					flash.put("error", "Score card details could not be inserted...");
					ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}

			}
			// After save, the same data should be populated throughtout the screen
			request.setAttribute("patMap", patmap);
			request.setAttribute("valMap", valueMap);
			request.setAttribute("sevMap", severityMap);
			request.setAttribute("rangeMap", rangeMap);
			request.setAttribute("score_card_id", scoreCardId);
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		return mapping.findForward("edit");
	}


	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, Exception {
		Map patmap = new HashMap();
		String mrno = (String) request.getParameter("mr_no");
		Integer monthNum = request.getParameter("month")!= null? Integer.parseInt(request.getParameter("month")) : Calendar.getInstance().get(Calendar.MONTH)+1;
		Integer year = request.getParameter("year") == null ? Calendar.getInstance().get(Calendar.YEAR)
				: Integer.parseInt(request.getParameter("year"));

		java.sql.Date[] dates = getDatesMonthRange(monthNum - 1, year);

		if (dates == null) {
			FlashScope flash = FlashScope.getScope(request);
			flash.put("error", "Date could not be parsed...");
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		if (mrno != null && !mrno.equals("")) {
			patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
			if (patmap == null) {
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", mrno + " does not exist...");
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		}
		setValueMapForMrno(mrno, (Integer) patmap.get("age"), (String) patmap.get("patient_gender"),
				dates[0], dates[1]);
		request.setAttribute("patMap", patmap);
		request.setAttribute("valMap", valueMap);
		request.setAttribute("sevMap", severityMap);
		request.setAttribute("rangeMap", Collections.EMPTY_MAP);
		return mapping.findForward("show");
	}


	public ActionForward getPatientDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
		String mrno = (String) request.getParameter("mr_no");
		Map patmap = new HashMap();
		if (mrno != null && !mrno.equals("")) {
			patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
			if (patmap == null) {
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", mrno + " does not exist...");
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		}
		ActionRedirect redirect = new ActionRedirect("/clinical/ScoreCard.do?_method=add");
		redirect.addParameter("mr_no", mrno);
		return redirect;
	}

	public String getParamDefault(HttpServletRequest req, String paramName, String defaultValue) {
		String value = req.getParameter(paramName);
		if ((value == null) || value.equals(""))
			value = defaultValue;
		return value;
	}

	public byte[] getReportPDFBytes(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws IOException, TemplateException, DocumentException, SQLException,
			ParseException {
		String reportName = m.getProperty("report-name");
		FtlReportGenerator fg = new FtlReportGenerator(reportName);
		fg.setParamsFromParamMap(req.getParameterMap());
		fg.setParam("cpath", req.getContextPath());
		fg.setParam("currtime", DataBaseUtil.getDateandTime());
		return fg.getPdfBytes();
	}

	public ActionForward printScoreCard(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, Exception {
		resp.setContentType("application/pdf");
		resp.setHeader("Content-disposition", "attachment;filename=ScoreCard.pdf");
		byte[] reportBytes = getReportPDFBytes(m, f, req, resp);
		resp.setContentLength(reportBytes.length);
		OutputStream os = resp.getOutputStream();
		os.write(reportBytes);
		os.close();
		return null;
	}

	public ActionForward printScoreCardFromID(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, Exception {
		Integer scoreCardId = Integer.parseInt(req.getParameter("score_card_id"));
		if(scoreCardId == null){
			return null;
		}
		String reportName = m.getProperty("report-name");
		FtlReportGenerator fg = new FtlReportGenerator(reportName);
		fg.setParamsFromParamMap(req.getParameterMap());

		ScoreCardDAO dao = new ScoreCardDAO();

		BasicDynaBean mainBean =  scoreCardMainDAO.findByKey("score_card_id", scoreCardId);
		Map<String, Object> mainMap = mainBean.getMap();

		for(String key: mainMap.keySet()){
			fg.setParam(key, mainMap.get(key));
		}
		String[] monthArray = {"January","February","March","April","May","June",
									"July","August","September","October","November","December"};
		int monthNum = ((BigDecimal)mainMap.get("card_month")).intValue();


		Map patmap = new HashMap();
		patmap = PatientDetailsDAO.getPatientGeneralDetailsMap((String)mainBean.get("mr_no"));
		fg.setParam("patname", patmap.get("full_name"));
		fg.setParam("fullname", patmap.get("full_name"));
		fg.setParam("ageGender",  patmap.get("age_text")+" /"+patmap.get("patient_gender"));

		fg.setParam("monthyear", monthArray[monthNum-1]+" "+ String.valueOf(((BigDecimal)mainMap.get("card_year")).intValue()));

		List<BasicDynaBean> detailBeanList =  scoreCardDetailsDAO.findAllByKey("score_card_id", scoreCardId);

		for(int i=0; i<detailBeanList.size(); i++) {
			BasicDynaBean detailBean =  detailBeanList.get(i);
			if(detailBean != null) {
				Map<String, Object> detailMap = detailBean.getMap();
				String targetName = (String)detailMap.get("attribute");
				fg.setParam(targetName+"Severity", detailMap.get("severity"));
				fg.setParam(targetName, detailMap.get("value"));
				fg.setParam(targetName+"Range", detailMap.get("range"));
			}
		}

		fg.setParam("cpath", req.getContextPath());
		fg.setParam("currtime", mainMap.get("save_date"));

		resp.setContentType("application/pdf");
		Boolean inline = new Boolean(req.getParameter("inline"));
		resp.setHeader("Content-disposition", (inline ? "inline" : "attachment") + ";filename=ScoreCard.pdf");
		fg.runPdfReport(resp.getOutputStream());

		return null;
	}

	public static byte[] printScoreCardFromID(int  scoreCardId) throws Exception{
		String reportName = "Scorecard";
		FtlReportGenerator fg = new FtlReportGenerator(reportName);

		ScoreCardDAO dao = new ScoreCardDAO();

		BasicDynaBean mainBean =  scoreCardMainDAO.findByKey("score_card_id", scoreCardId);
		Map<String, Object> mainMap = mainBean.getMap();

		for(String key: mainMap.keySet()){
			fg.setParam(key, mainMap.get(key));
		}
		String[] monthArray = {"January","February","March","April","May","June",
									"July","August","September","October","November","December"};
		int monthNum = ((BigDecimal)mainMap.get("card_month")).intValue();


		Map patmap = new HashMap();
		patmap = PatientDetailsDAO.getPatientGeneralDetailsMap((String)mainBean.get("mr_no"));
		fg.setParam("patname", patmap.get("full_name"));
		fg.setParam("fullname", patmap.get("full_name"));
		fg.setParam("ageGender",  patmap.get("age_text")+" /"+patmap.get("patient_gender"));

		fg.setParam("monthyear", monthArray[monthNum-1]+" "+ String.valueOf(((BigDecimal)mainMap.get("card_year")).intValue()));

		List<BasicDynaBean> detailBeanList =  scoreCardDetailsDAO.findAllByKey("score_card_id", scoreCardId);

		for(int i=0; i<detailBeanList.size(); i++) {
			BasicDynaBean detailBean =  detailBeanList.get(i);
			if(detailBean != null) {
				Map<String, Object> detailMap = detailBean.getMap();
				String targetName = (String)detailMap.get("attribute");
				fg.setParam(targetName+"Severity", detailMap.get("severity"));
				fg.setParam(targetName, detailMap.get("value"));
				fg.setParam(targetName+"Range", detailMap.get("range"));
			}
		}

		fg.setParam("currtime", mainMap.get("save_date"));
		return fg.getPdfBytes();
	}


	public ActionForward edit(ActionMapping m, ActionForm f, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, Exception {

		int scoreCardId = Integer.parseInt(request.getParameter("score_card_id"));
		String mrno = (String) request.getParameter("mr_no");
		Map patmap = new HashMap();
		if (mrno != null && !mrno.equals("")) {
			patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
			if (patmap == null) {
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", mrno + " does not exist...");
				ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		}

		ScoreCardDAO dao = new ScoreCardDAO();

		Map<String, String> rangeMap = new HashMap<String, String>();
		Map<String, String> severityMap = new HashMap<String, String>();
		Map<String, String> valueMap = new HashMap<String, String>();

		BasicDynaBean mainBean = scoreCardMainDAO.findByKey("score_card_id", scoreCardId);
		List<BasicDynaBean> detailBeanList = scoreCardDetailsDAO.findAllByKey("score_card_id", scoreCardId);

		Map<String,Object> mainMap = mainBean.getMap();
		for(String key: mainMap.keySet()){
			request.setAttribute(key, mainMap.get(key));
		}


		for(int i=0; i<detailBeanList.size(); i++) {
			BasicDynaBean detailBean =  detailBeanList.get(i);
			if(detailBean != null) {
				Map<String, Object> detailMap = detailBean.getMap();
				String targetName = (String)detailMap.get("attribute");
				severityMap.put(targetName+"Severity", (String)detailMap.get("severity"));
				valueMap.put(targetName, (String)detailMap.get("value"));
				rangeMap.put(targetName+"Range", (String)detailMap.get("range"));
			}
		}

		// After save, the same data should be populated throughtout the screen
		request.setAttribute("patMap", patmap);
		request.setAttribute("valMap", valueMap);
		request.setAttribute("sevMap", severityMap);
		request.setAttribute("rangeMap", rangeMap);
		request.setAttribute("score_card_id", scoreCardId);
		return m.findForward("edit");
	}



}
