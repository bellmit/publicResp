package com.insta.hms.master;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CenterAssociationDAO extends GenericDAO {

	private String entityTable;
	private String entityKey;
	private String assocKey;
	private String seqName;

	public String getEntityTable() {
		return entityTable;
	}

	public String getEntityKey() {
		return entityKey;
	}

	public CenterAssociationDAO(String assocTable, String assocKey,
			String entityTable, String entityKey, String seqName) {
		super(assocTable);
		this.assocKey = assocKey;
		this.entityTable = entityTable;
		this.entityKey = entityKey;
		this.seqName = seqName;
	}

	public static final String GET_CENTERS_FIELDS = " SELECT * ";
	public static final String GET_CENTERS_JOINS =
			  "	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=acm.center_id) "
			+ "	LEFT JOIN city c ON (c.city_id=hcm.city_id) "
			+ "	LEFT JOIN state_master s ON (s.state_id=c.state_id) ";

	public static final String GET_CENTERS_ORDER_BY = " ORDER BY s.state_name, c.city_name, hcm.center_name";

	public List getAssociatedCenters(Object entityId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		String table = getTable();
		StringBuilder query = new StringBuilder(GET_CENTERS_FIELDS);
		try {
			query.append(","+entityKey);
			query.append(" FROM "+ table + " acm ");
			query.append(GET_CENTERS_JOINS);
			query.append(" WHERE "+entityKey+" = ? ");
			query.append(GET_CENTERS_ORDER_BY);

			ps = con.prepareStatement(query.toString());
			ps.setObject(1, entityId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean deleteAssociation(Connection con, Object entityId)
			throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String table = getTable();
		try {
			ps = con.prepareStatement("SELECT * FROM "+ table +" where "+this.entityKey+"=? and center_id != -1");
			ps.setObject(1, entityId);
			rs = ps.executeQuery();
			if (!rs.next())
				return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM "+ table +" where "+this.entityKey+"=? and center_id != -1");
			ps.setObject(1, entityId);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean deleteAssociation(Connection con, Integer centerId,
			Object entityId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String table = getTable();
		try {
			ps = con.prepareStatement("SELECT * FROM "+ table +" where "+this.entityKey+"=? and center_id = ?");
			ps.setObject(1, entityId);
			ps.setInt(2, centerId);
			rs = ps.executeQuery();
			if (!rs.next())
				return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM "+ table +" where "+this.entityKey+"=? and center_id = ?");
			ps.setObject(1, entityId);
			ps.setInt(2, centerId);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean insertAssocation(Connection con, Integer centerId,
			Object entityId) throws SQLException, IOException {

		if (findByKey(con, entityKey, entityId) == null) {
			BasicDynaBean bean = getBean();
			bean.set(entityKey, entityId);
			bean.set("center_id", 0);
			bean.set("status", "A");
			if (!insert(con, bean)) {
				String error = "Failed to update center association for all centers..";
				return false;
			}
		}
		return true;
	}

	public boolean updateAssociations(Connection con, Object entityId,
			String[] centerIds, String[] assocIds, String[] assocStatus,
			String[] assocDeleted, String[] assocEdited) throws SQLException,
			IOException {
		String error = "";
		for (int i = 0; i < centerIds.length - 1; i++) {
			BasicDynaBean bean = getBean();
			bean.set(entityKey, entityId);
			bean.set("center_id", Integer.parseInt(centerIds[i]));
			bean.set("status", assocStatus[i]);

			if (assocIds[i].equals("_")) {
				bean.set(this.assocKey, DataBaseUtil.getNextSequence(this.seqName));
				if (!insert(con, bean)) {
					error = "Failed to insert center association for selected centers..";
					return false;
				}
			} else if (new Boolean(assocDeleted[i])) {
				if (!delete(con, this.assocKey, Integer.parseInt(assocIds[i]))) {
					error = "Failed to delete center association for selected center..";
					return false;
				}
			} else if (new Boolean(assocEdited[i])) {
				if (update(con, bean.getMap(),this.assocKey,
						Integer.parseInt(assocIds[i])) != 1) {
					error = "Failed to update center association for selected center..";
					return false;
				}
			}
		}
		return true;
	}

	public BasicDynaBean getAssociatedBean(Object entityId) throws SQLException {
		GenericDAO entityDao = new GenericDAO(entityTable);
		BasicDynaBean entityBean = entityDao.findByKey(entityKey, entityId);
		return entityBean;
	}

	public String getAssociationKey() {
		return assocKey;
	}

}
