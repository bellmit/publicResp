/**
 *
 */
package com.insta.hms.master.DynaPackageCategory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.util.List;

/**
 * @author lakshmi
 *
 */
public class DynaPackageCategoryMasterDAO extends GenericDAO {

	public DynaPackageCategoryMasterDAO() {
		super("dyna_package_category");
	}

	private static final String GET_CATEGORIES = " SELECT dyna_pkg_cat_id::text, dyna_pkg_cat_name, limit_type " +
	" FROM dyna_package_category ORDER BY dyna_pkg_cat_name ";

	@SuppressWarnings("unchecked")
	public List<BasicDynaBean> getCategories() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_CATEGORIES);
	}

	private static final String GET_A_CATEGORY = "SELECT * FROM dyna_package_category ORDER BY dyna_pkg_cat_id LIMIT 1 ";

	@SuppressWarnings("unchecked")
	public BasicDynaBean getACategory() throws SQLException {
		List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(GET_A_CATEGORY);
		if (list != null && list.size() > 0)
			return list.get(0);
		return null;
	}
}
