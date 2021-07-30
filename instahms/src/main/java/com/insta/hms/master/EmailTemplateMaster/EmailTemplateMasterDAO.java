package com.insta.hms.master.EmailTemplateMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

/**
 * @author kalpana.muvvala
 *
 */

public class EmailTemplateMasterDAO extends GenericDAO {
	Connection con = null;


	public EmailTemplateMasterDAO() {
		super("email_template");
	}

	public String getNextEmailTemplateId() throws SQLException {
		String EmailTemplateId = null;

		EmailTemplateId = AutoIncrementId.getNewIncrId("EMAIL_TEMPLATE_ID","EMAIL_TEMPLATE","EMAIL_TEMPLATE");

		return EmailTemplateId;
	}

	public BasicDynaBean getTemplate(String templateName) throws SQLException {
		return findByKey("template_name", templateName);
	}

	private static String GET_Email_Template = "SELECT email_template_id,template_name,from_address,subject, mail_message FROM EMAIL_TEMPLATE WHERE email_template_id!=?";

	public String getEmailTemplateXmlContent(String email_template_id) {
		PreparedStatement ps = null;
		String stateContent = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_Email_Template);
			ps.setString(1, email_template_id);
			stateContent = DataBaseUtil.getXmlContentWithNoChild(ps, "GET_EMAIL_TEMPLATE");
		}catch(Exception e){
			Logger.log(e);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}return stateContent;
	}

	private static final String EMAIL_TEMPLATE_QUERY_FIELDS = "SELECT email_template_id, template_name, from_address, subject, mail_message," +
			" em.category_name as email_category, to_address ";

	private static final String EMAIL_TEMPLATE_QUERY_COUNT = "SELECT count(et.email_template_id) ";

	private static final String EMAIL_TEMPLATE_QUERY_TABLES =
		" FROM email_template et  LEFT JOIN email_category_master em on (em.category_id = et.email_category) ";

	public static PagedList emailTemplateList(Map filter, Map listing) throws SQLException, ParseException {
		Connection con = DataBaseUtil.getReadOnlyConnection();

		String sortField = (String)listing.get(LISTING.SORTCOL);
		boolean sortReverse = (Boolean)listing.get(LISTING.SORTASC);
		int pageSize = (Integer)listing.get(LISTING.PAGESIZE);
		int pageNum = (Integer)listing.get(LISTING.PAGENUM);

		SearchQueryBuilder qb = new SearchQueryBuilder(con, EMAIL_TEMPLATE_QUERY_FIELDS,
				EMAIL_TEMPLATE_QUERY_COUNT, EMAIL_TEMPLATE_QUERY_TABLES, null, sortField, sortReverse, pageSize, pageNum);
		qb.build();

		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
	}


}

