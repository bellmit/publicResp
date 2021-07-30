/**
 *
 */
package com.insta.hms.master.GenericImageMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishna.t
 *
 */
public class GenericImageDAO extends GenericDAO {

	private static final String table = "doc_hosp_images";
	public GenericImageDAO(){
		super(table);
	}

	public BasicDynaBean getGeneracImage(Object imageId) throws SQLException, IOException{
		BasicDynaBean bean = getBean();
		loadByteaRecords(bean, "image_id", imageId);
		return bean;
	}

	public BasicDynaBean getGeneracImageUsingName(String imageName) throws SQLException, IOException{
		BasicDynaBean bean = getBean();
		loadByteaRecords(bean, "image_name", imageName);
		return bean;
	}

	private final static String GENERIC_IMAGES_FIELD = "SELECT IMAGE_NAME, IMAGE_ID, CONTENT_TYPE ";
	private final static String GENERIC_IMAGES_TABLE = " FROM DOC_HOSP_IMAGES ";
	private final static String COUNT_QUERY = "SELECT COUNT(IMAGE_NAME) ";

	public PagedList searchGenericImages(Map<LISTING, Object> listingParams) throws SQLException {
		int pageNum = (Integer) listingParams.get(LISTING.PAGENUM);
		int pageSize = (Integer) listingParams.get(LISTING.PAGESIZE);
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder sb = null;
		try {
			 sb = new SearchQueryBuilder(con, GENERIC_IMAGES_FIELD, COUNT_QUERY,
					GENERIC_IMAGES_TABLE, null, null, false, pageSize, pageNum);
			sb.build();
			return sb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (sb != null) sb.close();
		}

	}

	public List getGenericImageUrls() throws SQLException {

		List<Map> allImages = new ArrayList<Map>();

		//List<String> gcolumns = new ArrayList<String>();
		//gcolumns.add("image_id");
		//gcolumns.add("image_name");
		//gcolumns.add("content_type");
		List<BasicDynaBean> genericImagesList = listAll(Arrays.asList(
					new String[] {"image_id", "image_name", "content_type"}));

		for (BasicDynaBean bean : genericImagesList) {
			Map map = new HashMap(bean.getMap());
			map.put("viewUrl", "/master/GenericImageMaster.do?_method=view&image_id="+bean.get("image_id"));
			allImages.add(map);
		}
		return allImages;

	}


}
