package com.insta.hms.master.Accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class AccountingGroupMasterDAO extends GenericDAO{


	public AccountingGroupMasterDAO () {
		super("account_group_master");
	}

	private static final String ASSOCIATED_ACCOUNT_GROUP =
			" SELECT account_group_id, account_group_name, accounting_company_name, account_group_service_reg_no " +
			" FROM account_group_master gm " +
			"  JOIN stores gd on (gd.account_group=gm.account_group_id or gm.account_group_id=1)" +
			" GROUP BY account_group_name, account_group_id, accounting_company_name, account_group_service_reg_no " +
			" Order by account_group_id";
	public List getAssociatedAccountGroups() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(ASSOCIATED_ACCOUNT_GROUP);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	private static final String LIST = "SELECT * FROM account_group_master";
	public List getList(Connection con) throws SQLException{
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(LIST);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String ACC_LIST =
		" SELECT * FROM account_group_master WHERE coalesce(account_group_service_reg_no,'') = '' AND account_group_id != 1 ";
	public static List<BasicDynaBean> getAccountGroupsWithoutServiceReg() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(ACC_LIST);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String ACC_COMP_LIST =
		" SELECT * FROM account_group_master WHERE coalesce(accounting_company_name,'') = '' AND account_group_id != 1 ";
	public static List<BasicDynaBean> getAccountGroupsWithoutCompNames() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(ACC_COMP_LIST);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
