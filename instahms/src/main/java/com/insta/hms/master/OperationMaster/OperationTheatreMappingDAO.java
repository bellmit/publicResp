package com.insta.hms.master.OperationMaster;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;

public class OperationTheatreMappingDAO extends GenericDAO{
	static Logger logger = LoggerFactory.getLogger(OperationChargeDAO.class);

	public OperationTheatreMappingDAO() {
		super("operation_theatre_mapping");
	}

}
