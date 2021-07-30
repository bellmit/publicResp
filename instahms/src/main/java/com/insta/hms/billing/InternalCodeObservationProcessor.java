package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class InternalCodeObservationProcessor implements
		InsuranceBillsProcessor {

	public boolean process(String submission_batch_id) throws SQLException ,IOException {

		List<BasicDynaBean> alternateCodesList =getAlternateCodes(submission_batch_id);
		boolean success = updateAllNonStandardCodes(alternateCodesList);
		return success;
	}

	private static final String GET_ALTERNATE_CODES = " select * from charge_alternate_codes_view where last_submission_batch_id=? ";

	public List<BasicDynaBean> getAlternateCodes(String submission_batch_id)throws SQLException ,IOException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps=null;
		try{
			ps = con.prepareStatement(GET_ALTERNATE_CODES);
			ps.setString(1, submission_batch_id);
			List<BasicDynaBean> alternateCodesList = DataBaseUtil.queryToDynaList(ps);
			return alternateCodesList;
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean  updateAllNonStandardCodes(List<BasicDynaBean> alternateCodesList)
	throws SQLException ,IOException{
		boolean mrdObsFlag = true;
		Connection con = DataBaseUtil.getConnection();
		try {
		for (BasicDynaBean b: alternateCodesList) {
			mrdObsFlag = updateNonStdCode(con, b);
			if(!mrdObsFlag)
				return mrdObsFlag;
		}
		return mrdObsFlag;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public boolean updateNonStdCode(Connection con, BasicDynaBean b)throws SQLException ,IOException{
		GenericDAO mrdObsDAO = new GenericDAO("mrd_observations");
		BasicDynaBean mrdObsbean = mrdObsDAO.getBean();

		mrdObsbean.set("observation_id", DataBaseUtil.getNextSequence(con, "mrd_observations_observation_id_seq"));
		mrdObsbean.set("charge_id", b.get("charge_id"));
		mrdObsbean.set("observation_type", "Text");
		mrdObsbean.set("code", "Non-Standard-Code");
		mrdObsbean.set("value", b.get("alternate_code"));
		mrdObsbean.set("value_type", "Non-Standard-Code");
		mrdObsbean.set("value_editable", "Y");
		mrdObsbean.set("sponsor_id", b.get("sponsor_id"));
		return mrdObsDAO.insert(con, mrdObsbean);
	}

}
