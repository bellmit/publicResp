package com.insta.hms.master;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.StringUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public abstract class MasterDAO extends GenericDAO {
	private static final String ADD_FAILURE_MSG_PREFIX = "Failed to add";
	private static final String EDIT_FAILURE_MSG_PREFIX = "Failed to update";
	private static final String DUPLICATE_MSG_SUFFIX = "already exists";
	private static final String DELETE_FAILURE_MSG_PREFIX = "Failed to delete record";

	private static Logger logger = LoggerFactory.getLogger(MasterDAO.class);

	private String idColumnName;
	private String uniqueColumn;
	
	public String getIdColumnName() {
		return idColumnName;
	}
	
	public String getUniqueColumnName() {
		return uniqueColumn;
	}

    protected MasterDAO(String tablename, String idColumn) {
		this(tablename, idColumn, null);
	}

    
	protected MasterDAO(String tablename, String idColumn, String uniqueColumn) {
		super(tablename);
		this.idColumnName = idColumn;
		this.uniqueColumn = uniqueColumn;
	}

	public PagedList search(Map requestParams, Map<LISTING, Object> listingParams) throws SQLException, ParseException {
		String secondarySort = getIdColumnName();
		return super.search(requestParams, listingParams, secondarySort);
	}

	public PagedList search (String selectQuery, String countQuery, String fromTables, 
							Map filterParams, Map<LISTING, Object> listingParams, 
							String secondarySort) throws SQLException, ParseException {

		String sortField = (String)listingParams.get(LISTING.SORTCOL);
		boolean sortReverse = (Boolean)listingParams.get(LISTING.SORTASC);
		int pageSize = (Integer)listingParams.get(LISTING.PAGESIZE);
		int pageNum = (Integer)listingParams.get(LISTING.PAGENUM);

		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con, selectQuery,
					countQuery, fromTables, null, sortField, sortReverse,
					pageSize, pageNum);

			if (filterParams != null)
				qb.addFilterFromParamMap(filterParams);
			if (secondarySort != null && !secondarySort.equals(""))
				qb.addSecondarySort(secondarySort);
			qb.build();
			PagedList l = qb.getMappedPagedList();
			qb.close();
			return l;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public BasicDynaBean insert(BasicDynaBean baseBean, List errors) throws SQLException, IOException {
		return insert(baseBean, true, errors);
	}
	
	public BasicDynaBean insert(BasicDynaBean baseBean, boolean checkDuplicate, List errors) throws SQLException, IOException {
		
		boolean success = false;
		Connection con = null;
		if (!checkDuplicate || !isDuplicate(baseBean, null, errors)) {
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				baseBean.set(getIdColumnName(), getNextSequence());
				success = insert(con, baseBean);
				if (!success) errors.add(ADD_FAILURE_MSG_PREFIX + " " + StringUtil.prettyName(getTable()));
			} 
			finally {
				DataBaseUtil.commitClose(con, success);
			}
		}
		return (success) ? baseBean : null;
	}

	public BasicDynaBean update(BasicDynaBean baseBean, List errors) throws SQLException, IOException {
		return update(baseBean, true, errors);
	}
	
	public BasicDynaBean update(BasicDynaBean baseBean, boolean checkDuplicate, List errors) throws SQLException, IOException {
		Connection con = null;
		Object key = baseBean.get(getIdColumnName());		

		int success = -1;
		if (!checkDuplicate || !isDuplicate(baseBean, key, errors)) {
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				success = updateWithName(con,  baseBean.getMap(), getIdColumnName());
				if (success <= 0) errors.add(EDIT_FAILURE_MSG_PREFIX + " " + StringUtil.prettyName(getTable()));
			} finally {
				DataBaseUtil.commitClose(con, (success > 0));
			}
		}		
		return (success > 0) ? baseBean : null;
	}

    protected BasicDynaBean validateData(BasicDynaBean baseBean, boolean checkDuplicate, 
    		Object ignoreKey, List<String> errors) throws SQLException {
    	return baseBean;
    }
	
	private boolean isDuplicate(BasicDynaBean baseBean, Object ignoreKey, 
			List<String> errors) throws SQLException {
		boolean duplicate = false;
		String uniqueColumnName = getUniqueColumnName();
		if (null != baseBean && null != uniqueColumnName && !uniqueColumnName.isEmpty()) {
			BasicDynaBean existing = findByKey(uniqueColumnName, baseBean.get(uniqueColumnName));
			if (null != existing && !existing.get(getIdColumnName()).equals(ignoreKey)) { 
				errors.add(StringUtil.prettyName(uniqueColumnName) + " " + baseBean.get(uniqueColumnName) + " " + DUPLICATE_MSG_SUFFIX);
				duplicate = true;
			}
		}
		return duplicate;
	}

	public BasicDynaBean delete(BasicDynaBean baseBean, List errors) throws SQLException, IOException {
		
		boolean success = false;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			success = super.delete(con, getIdColumnName(), baseBean.get(getIdColumnName()));
			if (!success) errors.add(DELETE_FAILURE_MSG_PREFIX + " " + getIdColumnName() + "=" + baseBean.get(getIdColumnName()));
		} 
		finally {
			DataBaseUtil.commitClose(con, success);
		}
		return (success) ? baseBean : null;
	}

	public BasicDynaBean inactivate(BasicDynaBean baseBean, List<String> errors) throws SQLException, IOException {
		Connection con = null;
		String statusCol = getStatusColumnName();		
		String status = getDeleteStatusValue();
		int success = -1;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			baseBean.set(statusCol, status);
			success = updateWithName(con,  baseBean.getMap(), getIdColumnName());
			if (success <= 0) errors.add(DELETE_FAILURE_MSG_PREFIX + " " + StringUtil.prettyName(getTable()));
		} finally {
			DataBaseUtil.commitClose(con, (success > 0));
		}
		return (success > 0) ? baseBean : null;
	}

	private String getDeleteStatusValue() {
		return "X";
	}

	protected String getStatusColumnName() {
		return "status";
	}
}
