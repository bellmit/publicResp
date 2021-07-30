package com.insta.hms.emr;

import com.insta.hms.ScoreCard.ScoreCardAction;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreCardProviderImpl implements EMRInterface {

  private static final GenericDAO scoreCardMainDAO = new GenericDAO("score_card_main");
  
	public ScoreCardProviderImpl() {
		// TODO Auto-generated constructor stub
	}

	public byte[] getPDFBytes(String docid, int pritnerId) throws Exception {

		BasicDynaBean bean = scoreCardMainDAO.findByKey("score_card_id", Integer.parseInt(docid));
		return ScoreCardAction.printScoreCardFromID(Integer.parseInt(docid));

	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return Collections.emptyList();
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		return getScoreCardForEMR(mrNo);

	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return Collections.emptyList();
	}


	public static List<EMRDoc> getScoreCardForEMR(String mrNo)
			throws SQLException {
		List<BasicDynaBean> list = scoreCardMainDAO.findAllByKey("mr_no", mrNo);
		if (list == null || list.isEmpty())
			return null;

		List<EMRDoc> emrdoclist = new ArrayList<EMRDoc>();
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		int printerId = (Integer) printpref.get("printer_id");
		for (BasicDynaBean bean: list) {
			EMRDoc doc = new EMRDoc();
			String[] monthArray = {"January","February","March","April","May","June",
					"July","August","September","October","November","December"};
			doc.setTitle("ScoreCard("+monthArray[((BigDecimal)bean.get("card_month")).intValue()- 1]+" "+bean.get("card_year")+")");
			doc.setDate(new java.sql.Date(((Timestamp)bean.get("save_date")).getTime()));

			doc.setDoctor("");

			String docId = bean.get("score_card_id").toString();
			doc.setDocid(docId);

			String userName = (String) bean.get("username");
			doc.setUserName(userName);
			doc.setPdfSupported(true);
			doc.setType("SYS_CLINICAL");
			doc.setAuthorized(true);

			doc.setUpdatedBy((String) bean.get("username"));
			doc.setPrinterId(printerId);
			doc.setProvider(EMRInterface.Provider.ScoreCardProvider);

			String displayUrl = "/clinical/ScoreCardList.do?_method=printScoreCardFromID";
				displayUrl += "&score_card_id=" + docId;
				displayUrl += "&inline=true";
			doc.setDisplayUrl(displayUrl);
			emrdoclist.add(doc);
		}
		return emrdoclist;

	}


}
