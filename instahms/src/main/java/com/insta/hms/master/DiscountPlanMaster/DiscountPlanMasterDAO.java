package com.insta.hms.master.DiscountPlanMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.master.MasterDAO;
import com.insta.hms.mdm.discountplans.DiscountPlanController;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@MigratedTo(value = DiscountPlanController.class)
public class DiscountPlanMasterDAO extends MasterDAO {
	
	protected DiscountPlanMasterDAO() {
		super("discount_plan_main", "discount_plan_id", "discount_plan_name");
	}

/*	public DiscountPlanMasterDAO(){
		super("discount_plan_main");
	}
*/	
/*	public DiscountPlanMasterDAO(String str){
		super(str);
	}
*/	
	
	private static final String GET_ALL_DISCOUNT_PLANS = " SELECT dpm.discount_plan_id as discount_cat_id,dpm.discount_plan_name as discount_cat_name, "+
			" dpm.discount_plan_description , 0 as discount_cat_perc,discount_plan_name as discount_cat, "+
			" dpm.validity_start ,dpm.validity_end,dpcm.center_id  "+
			" FROM discount_plan_main dpm left join discount_plan_center_master "+
			" dpcm on(dpm.discount_plan_id=dpcm.discount_plan_id )  "+
			" where dpm.status='A' and coalesce(dpcm.status,'A')='A' and coalesce(dpm.validity_start,current_date)<=current_date "+
			" and coalesce(dpm.validity_end,current_date)>=current_date and (dpcm.center_id=0 or dpcm.center_id is null "+
			" or dpcm.center_id = ?)";

	public static List getDiscountCategoryNames(int discPlanId) throws SQLException{
		PreparedStatement ps = null;
		List discountCatNames = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			Integer centerID = RequestContext.getCenterId();
			ps = con.prepareStatement(GET_ALL_DISCOUNT_PLANS + ( discPlanId != 0 ? " AND dpm.discount_plan_id = ? " : "") );
			ps.setInt(1,centerID);
			if ( discPlanId != 0 ) {
				ps.setInt(2, discPlanId);//only allowed discount plan
			}
			
			discountCatNames = DataBaseUtil.queryToDynaList(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return discountCatNames;
	}
	
	public static List getDiscountCategoryNames(Connection con, int discPlanId) throws SQLException{
    PreparedStatement ps = null;
    List discountCatNames = null;
    try{
      Integer centerID = RequestContext.getCenterId();
      ps = con.prepareStatement(GET_ALL_DISCOUNT_PLANS + ( discPlanId != 0 ? " AND dpm.discount_plan_id = ? " : "") );
      ps.setInt(1,centerID);
      if ( discPlanId != 0 ) {
        ps.setInt(2, discPlanId);//only allowed discount plan
      }      
      discountCatNames = DataBaseUtil.queryToDynaList(ps);
    }finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return discountCatNames;
  }
	
	private static final String DISCOUNT_FILEDS = "SELECT * ";
	private static  String DISCOUNT_FROM = " FROM (SELECT dpm.*,dpcm.center_id FROM discount_plan_main dpm left join "+
					" discount_plan_center_master  dpcm on(dpm.discount_plan_id=dpcm.discount_plan_id )   # ) as foo ";
	private static final String DISCOUNT_FROM1 = " FROM discount_plan_main ";
	private static final String COUNT = " SELECT count(discount_plan_id) ";

	public static  PagedList getDiscountPlanMainDetails(Map params, Map<LISTING, Object> listingParams) throws SQLException,
		ParseException {
		Connection con = null;
		SearchQueryBuilder qb= null;
		try{
			con =DataBaseUtil.getConnection();
			String centerID[] = (String[])params.get("_center_id");
			int centerId=-1;
			if(centerID != null)
			 centerId = Integer.parseInt(centerID[0]);
			
			if(centerId == -1){
				qb = new SearchQueryBuilder(con, DISCOUNT_FILEDS, COUNT, DISCOUNT_FROM1, listingParams);
			}else{
				String FROM = null;
				if(centerId == 0){
					FROM = DISCOUNT_FROM.replace("#", " where (dpcm.center_id=0 or dpcm.center_id is null)");
				}else{
					FROM = DISCOUNT_FROM.replace("#", " where dpcm.center_id="+centerId);
				}
				qb = new SearchQueryBuilder(con, DISCOUNT_FILEDS, COUNT, FROM, listingParams);
			}
			qb.addFilterFromParamMap(params);
			qb.build();
			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}
	

	private static final String INSURANCE_CATEGORY_LIST = "select insurance_category_id,insurance_category_name from item_insurance_categories "
											+ "where insurance_payable='Y' order by insurance_category_name";

	public List<BasicDynaBean> getInsuranceCategoryList() throws SQLException,ParseException{
			Connection con = null;
			PreparedStatement ps = null;
			try{
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(INSURANCE_CATEGORY_LIST);
				return DataBaseUtil.queryToDynaList(ps);
			}finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}
	
	private static final String DISCOUNT_PLAN_DETAILS_LIST = "select * from discount_plan_details where discount_plan_id = ? order by discount_plan_detail_id";
	
    public List<BasicDynaBean> getDiscountPlanDetailsList(String plan_id) throws SQLException,ParseException{
            Connection con = null;
            PreparedStatement ps = null;
            try{
                 con = DataBaseUtil.getReadOnlyConnection();
                 ps = con.prepareStatement(DISCOUNT_PLAN_DETAILS_LIST);
                 ps.setInt(1, Integer.parseInt(plan_id));
                 return DataBaseUtil.queryToDynaList(ps);
              
            }finally {
               DataBaseUtil.closeConnections(con, ps);
              }
     }
    
    
    
    
    private static final String DISCOUNT_PLAN_LIST = "select discount_plan_name from discount_plan_main where discount_plan_name != ?";
	
    public List<BasicDynaBean> getDiscountPlanList(String str) throws SQLException,ParseException{
            Connection con = null;
            PreparedStatement ps = null;
            try{
                 con = DataBaseUtil.getReadOnlyConnection();
                 ps = con.prepareStatement(DISCOUNT_PLAN_LIST);
                 ps.setString(1, str);
                 return DataBaseUtil.queryToDynaList(ps);
              
            }finally {
               DataBaseUtil.closeConnections(con, ps);
              }
     }

    private static final String INSURANCE_DISCOUNT_PLANS = "SELECT ipm.plan_id as insurance_plan_id, dpm.discount_plan_id, dpm.discount_plan_name "
    		+ " FROM discount_plan_main dpm JOIN insurance_plan_main ipm ON (dpm.discount_plan_id = ipm.discount_plan_id) "
    		+ " WHERE dpm.status='A' AND COALESCE(dpm.validity_start,current_date)<=current_date "
			+ " AND COALESCE(dpm.validity_end,current_date)>=current_date";
    
	public static List<BasicDynaBean> getInsuranceDiscountPlans() throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try{
             con = DataBaseUtil.getReadOnlyConnection();
             ps = con.prepareStatement(INSURANCE_DISCOUNT_PLANS);
             return DataBaseUtil.queryToDynaList(ps);
          
        }finally {
           DataBaseUtil.closeConnections(con, ps);
        }
	}
	
}
