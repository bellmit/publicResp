package com.insta.hms.master.Accounting;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.mdm.accountpreferences.AccountingPreferenceRepository;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;

@MigratedTo(value = AccountingPreferenceRepository.class)
public class AccountingPrefsDAO extends GenericDAO {

	public AccountingPrefsDAO() {
		super("hosp_accounting_prefs");
	}

	public BasicDynaBean getPrefs(Connection con) throws SQLException {
		return getRecord(con);
	}
}


