package com.insta.hms.resourcescheduler;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.OTServices.OperationDetailsBO;
import com.insta.hms.orders.ConsultationTypesDAO;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.orders.OrderDAO;
import com.insta.hms.orders.TestDocumentDTO;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.batchjob.builders.AppointmentStatusChangeSMSJob;
import com.insta.hms.batchjob.builders.DynamicAppointmentReminderJob;
import com.insta.hms.batchjob.pushevent.EventListenerJob;
import com.insta.hms.batchjob.pushevent.Events;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillActivityCharge;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.payment.PaymentEngine;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.editvisitdetails.EditVisitDetailsDAO;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.jobs.JobService;
import com.insta.hms.master.AppointmentSource.AppointmentSourceDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.resourcescheduler.ResourceBO.AppointMentResource;
import com.insta.hms.resourcescheduler.ResourceBO.Appointments;
import com.insta.hms.resourcescheduler.ResourceBO.ChannellingAppt;
import com.insta.hms.resourcescheduler.ResourceBO.Schedule;
import com.insta.hms.usermanager.UserDAO;
import com.insta.hms.util.hijricalender.UmmalquraCalendar;
import com.insta.hms.util.hijricalender.UmmalquraGregorianConverter;

import flexjson.JSONSerializer;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class ResourceAction extends BaseAction {

    static Logger logger = LoggerFactory.getLogger(ResourceAction.class);

    ResourceBO bo = new ResourceBO();
    private static ContactsDAO contactsDao = new ContactsDAO();
    private static PatientDetailsDAO patientDetailsDao = new PatientDetailsDAO();
    private static CenterMasterDAO centerDao = new CenterMasterDAO();
    private static ResourceDAO resourceDAO = new ResourceDAO();
    private static GenericPreferencesDAO genericPreferencesDAO =  new GenericPreferencesDAO();
    JobService jobService = JobSchedulingService.getJobService();
    
    @IgnoreConfidentialFilters
    public ActionForward getWeekView(ActionMapping am,ActionForm af,
            HttpServletRequest req, HttpServletResponse res) throws Exception {

        String choosenWeekDate = req.getParameter("choosenWeekDate");
        String hijriDate = req.getParameter("hijriDate");
        String gregDateStr = req.getParameter("gregDate");
        java.util.Date date = null;
        HttpSession session = req.getSession();
        String incResources = req.getParameter("includeResources");
        String day = req.getParameter("day");
        int roleId = (Integer)req.getSession().getAttribute("roleId");
        String category =  am.getProperty("category");

        String cenName = RequestContext.getCenterName();
        int cenId = RequestContext.getCenterId();
        String userName = (String)(req.getSession(false).getAttribute("userid"));

        String docId = null;
        List docCenters = null;
        Integer docCenterId = null;
        boolean check = true;
        int doctorsCount = 0;
        if (category.equals("DOC")) {
        	if((roleId != 1) && (roleId != 2)) {
        		docId = UserDAO.getLoggingDoctorRecord((String) session.getAttribute("userId"))!=null?
        			(String) UserDAO.getLoggingDoctorRecord((String) session.getAttribute("userId")).get("doctor_id"):null;
        		docCenterId = UserDAO.getLoggingDoctorRecord((String) session.getAttribute("userId"))!=null?
        			(Integer) UserDAO.getLoggingDoctorRecord((String) session.getAttribute("userId")).get("center_id"):null;
        			docCenters = DoctorMasterDAO.getDoctorsCentersAndIds(docId);
        			for ( int i=0; i<docCenters.size();i++) {
        				BasicDynaBean centerIdBean = (BasicDynaBean) docCenters.get(i);
        				if (centerIdBean.get("status").equals("A")) {
        					if (centerIdBean.get("center_id").equals(docCenterId) || (Integer)centerIdBean.get("center_id") == 0
        						   || (Integer)centerIdBean.get("center_id") == cenId ) {
        							doctorsCount++;
        						}
        					}
        					if (doctorsCount !=0 || docCenterId == 0) {
        						check = true;
        				} else {
        					check = false;
        				}
        			}
        			if (docId != null && !docId.equals("") && check) {
        				incResources = docId;
        			}
	        		 req.setAttribute("scheduler_user_doctor", UserDAO.getLoggingDoctorRecord((String) session.getAttribute("userId")));
		        	 req.setAttribute("cenDoctors", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(docCenters)));
		        	 req.setAttribute("cenName", cenName);
		        	 req.setAttribute("doctorsCount", doctorsCount);
		        	 req.setAttribute("centerId", cenId);
        	}
        }

        String allowbackDated = null;

        if((roleId != 1) && (roleId != 2)){
        	allowbackDated = (String)((Map)req.getSession(false).getAttribute("actionRightsMap")).get("allow_backdated_app");
        }
        else {
        	allowbackDated = "A";
        }
        String errorMsg = null;
        ResourceCriteria rc = new ResourceCriteria();
        JSONSerializer json = new JSONSerializer();

        int centerId = (Integer)session.getAttribute("centerId");
        String reqCenterId = req.getParameter("centerId");
        centerId = (reqCenterId != null && !reqCenterId.isEmpty()) ? Integer.parseInt(reqCenterId) : centerId;

        String schDefaultDoctor = (String)req.getSession().getAttribute("sch_default_doctor");
        if (schDefaultDoctor != null)
            schDefaultDoctor = (schDefaultDoctor.equals("undefined") ? null : schDefaultDoctor);

        // If includeResources is not from request param check if any schedulable default doctor for that user.
        if (category.equals("DOC") && (incResources == null || incResources.equals(""))) {
            incResources = schDefaultDoctor;
        }

        // If schedulable default doctor is null check for default department for that user and get any doctor of that dept.
        if (incResources == null || incResources.equals("")) {
            String dept = (String)req.getSession().getAttribute("scheduler_dept_id");
            List defaultList = new ResourceDAO(null).getDefaultHeaders(category, dept, centerId, userName, roleId);

            if (defaultList != null && defaultList.size() > 0)
                incResources = (String)((BasicDynaBean)defaultList.get(0)).get("resource_id");
        }


        // If no doctor in that department show error message.
        if (incResources == null || incResources.equals("")) {
            String error = "";
            if (category.equals("DOC"))
                error = "No Schedulable Doctor Available. Please add a schedulable doctor in doctor master (or)" +
                " update a schedulable doctor for this user";

            else if (category.equals("SNP") || category.equals("DIA"))
                error = "No Schedulable Equipment Available. Please add a schedulable equipment in equipment master";

            else if (category.equals("OPE"))
                error = "No Schedulable Operation Theatre Available. Please add a schedulable theatre in theatre master";

            req.setAttribute("error", error);
            return am.findForward("reportErrors");

        }
        if (incResources.contains(","))
            incResources = incResources.split(",")[0];

        BasicDynaBean schBean = new ResourceDAO(null).getDefaultSchedulerBean(incResources, category);
        req.setAttribute("schBean", schBean);
        if(hijriDate != null && !hijriDate.equals("")) {
        	String[] params = hijriDate.split("-");
            UmmalquraCalendar cal = new UmmalquraCalendar(Integer.parseInt(params[0]), Integer.parseInt(params[1]) - 1, Integer.parseInt(params[2]));
            date = cal.getTime();
        } else if(gregDateStr != null && !gregDateStr.equals("")){
        	String[] params = gregDateStr.split("-");
            Calendar cal = new GregorianCalendar(Integer.parseInt(params[0]), Integer.parseInt(params[1])-1, Integer.parseInt(params[2]));
            int[] hDateInfo = UmmalquraGregorianConverter.toHijri(cal.getTime());
            hijriDate = hDateInfo[0] + "-" + (hDateInfo[1]+1) + "-" + hDateInfo[2];
            date = cal.getTime();
        }
        if(date != null) {
        	choosenWeekDate = DateUtil.formatDate(date);
        } else if(choosenWeekDate == null){
        	date =  DateUtil.getCurrentDate();
        	choosenWeekDate = DateUtil.formatDate(date);
            int[] hDateInfo = UmmalquraGregorianConverter.toHijri(date);
            hijriDate = hDateInfo[0] + "-" + (hDateInfo[1]+1) + "-" + hDateInfo[2];
        } else if(!choosenWeekDate.equals("")) {
        	date = DateUtil.parseDate(choosenWeekDate);
            int[] hDateInfo = UmmalquraGregorianConverter.toHijri(date);
            hijriDate = hDateInfo[0] + "-" + (hDateInfo[1]+1) + "-" + hDateInfo[2];
        }
         calculateWeekDates(choosenWeekDate, day, incResources, rc);

        rc.category =  category;
        rc.scheduleName.add(incResources);
        rc.schName.add((incResources));

        errorMsg = ResourceBO.validateScheduler(rc);

        if(errorMsg != null) {
            req.setAttribute("referer", req.getHeader("Referer"));
            req.setAttribute("error",errorMsg);
            return am.findForward("reportErrors");
        }

        List<BasicDynaBean> schResourceNameList = null;
        if (category.equals("DOC")) {
            schResourceNameList = new DoctorMasterDAO().getSchedulableDoctorDepartmentNames(centerId);
            req.setAttribute("schResourceNameList", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(schResourceNameList)));
        } else {
            schResourceNameList = new TheatreMasterDAO().getSchedulableTheatreName(userName, roleId);
            req.setAttribute("schResourceNameList", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(schResourceNameList)));
        }

        req.setAttribute("datesArray",rc.datesArray);
        //req.setAttribute("daysArray", rc.daysArray);
        date = rc.choosendate;
        int[] hDateInfo = UmmalquraGregorianConverter.toHijri(date);
        hijriDate = hDateInfo[0] + "-" + (hDateInfo[1]+1) + "-" + hDateInfo[2];

        req.setAttribute("date", rc.choosendate);
        String gregDate = DateUtil.formatDateToYearAhead(date);
        String gregoDateStr = DateUtil.formatDateToWeekDay(date);
        req.setAttribute("dateString", gregoDateStr);
        req.setAttribute("gregoDate",gregDate);
        req.setAttribute("hijriCalDate", hijriDate);
        req.setAttribute("includeResources", incResources);
        req.setAttribute("choosenWeekDate", rc.choosendate);
        req.setAttribute("allowbackDated", allowbackDated);
        setAttributes(am, req, rc,"WeekView", centerId);

        Map resourceCalndarList = ResourceBO.getCalendarList(rc, "WeekView",centerId, userName);

        req.setAttribute("canlderList", resourceCalndarList);
        req.setAttribute("availbiltyList" , json.serialize(resourceCalndarList.get("headers")));
        List doctorsList = DoctorMasterDAO.getDoctorDepartmentsDynaList();
		req.setAttribute("doctorsJSON", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(doctorsList)));
        String countryCode = centerDao.getCountryCode(RequestContext.getCenterId());
		if(StringUtil.isNullOrEmpty(countryCode)){
		  countryCode = centerDao.getCountryCode(0);
		}
		req.setAttribute("defaultCountryCode", countryCode);
		req.setAttribute("countryList", PhoneNumberUtil.getAllCountries());
        return am.findForward("showWeekView");
    }



    private void calculateWeekDates(String choosenWeekDate, String day,
                String incResources, ResourceCriteria rc) throws Exception {

        java.util.Date newDate = null;

        Calendar calnder = Calendar.getInstance();
        java.sql.Date date = new Date(calnder.getTimeInMillis());

        if (choosenWeekDate != null && !choosenWeekDate.equals(""))
            date = DataBaseUtil.parseDate(choosenWeekDate);

        calnder.setTime(new java.util.Date(date.getTime()));

        int dayOfWeek = calnder.get(Calendar.DAY_OF_WEEK);
        Calendar calnder1 = Calendar.getInstance();
        calnder1.setTime(new java.util.Date(date.getTime()));

        for(int i=dayOfWeek;i<=(dayOfWeek+7);i++) {
            calnder1.add(Calendar.DATE, 1);
        }

        Date toDate = new Date(calnder1.getTimeInMillis());
        Date fromDate = new Date(calnder.getTimeInMillis());

        List<BasicDynaBean> docAvailList= new ResourceDAO(null).getAvailability(incResources,fromDate,toDate);

        if (docAvailList != null && docAvailList.size() > 0) {

            ArrayList<Integer> noOfDaysAvail = new ResourceDAO(null).getNoOfDaysAvail(fromDate,toDate,docAvailList,incResources);

            int count=0;
            int dayCount=0;

            if(day!=null && !day.equals("")){
                if (day.equals("nextDay")){

                    while (dayCount < 1) {
                        int k = calnder.get(Calendar.DAY_OF_WEEK);
                        for(int j=0;j<noOfDaysAvail.size();j++) {
                            if(k == noOfDaysAvail.get(j)) {
                                dayCount++;
                            }
                        }
                        calnder.add(Calendar.DATE, 1);
                    }

                    while (count < 5) {
                        int k = calnder.get(Calendar.DAY_OF_WEEK);
                        for(int j=0;j<noOfDaysAvail.size();j++) {
                            if(k == noOfDaysAvail.get(j)) {
                                rc.datesArray.add(new java.sql.Date(calnder.getTimeInMillis()));
                                rc.tempDatesArray.add(new java.sql.Date(calnder.getTimeInMillis()));
                                rc.dayOfweek.add(calnder.get(Calendar.DAY_OF_WEEK)-1);
                                count++;
                            }
                        }
                        calnder.add(Calendar.DATE, 1);
                    }

                }else if (day.equals("prevDay")){

                    while (dayCount < 1) {
                        calnder.add(Calendar.DATE, -1);
                        int k = calnder.get(Calendar.DAY_OF_WEEK);
                        for(int j=noOfDaysAvail.size()-1;j>=0;j--) {
                            if(k == noOfDaysAvail.get(j)) {
                                dayCount++;
                            }
                        }
                    }

                    while (count < 5) {
                        int k = calnder.get(Calendar.DAY_OF_WEEK);
                        for(int j=0;j<noOfDaysAvail.size();j++) {
                            if(k == noOfDaysAvail.get(j)) {
                                rc.datesArray.add(new java.sql.Date(calnder.getTimeInMillis()));
                                rc.tempDatesArray.add(new java.sql.Date(calnder.getTimeInMillis()));
                                rc.dayOfweek.add(calnder.get(Calendar.DAY_OF_WEEK)-1);
                                count++;
                            }
                        }
                        calnder.add(Calendar.DATE, 1);
                    }


                }else if(day.equals("nextWeek")){

                    while (dayCount < 5) {
                        int k = calnder.get(Calendar.DAY_OF_WEEK);
                        for(int j=0;j<noOfDaysAvail.size();j++) {
                            if(k == noOfDaysAvail.get(j)) {
                                dayCount++;
                            }
                        }
                        calnder.add(Calendar.DATE, 1);
                    }
                    Calendar cal = Calendar.getInstance();
                    cal = getStartDayOfNextOrPreviousWeek(1, cal, choosenWeekDate);
                    checkWeekAvailability(cal, noOfDaysAvail, rc);

                }else if(day.equals("prevWeek")){

                    while (dayCount < 5) {
                        calnder.add(Calendar.DATE, -1);
                        int k = calnder.get(Calendar.DAY_OF_WEEK);
                        for(int j=noOfDaysAvail.size()-1;j>=0;j--) {
                            if(k == noOfDaysAvail.get(j)) {
                                dayCount++;
                            }
                        }
                    }
                    Calendar cal = Calendar.getInstance();
                    cal = getStartDayOfNextOrPreviousWeek(-1, cal, choosenWeekDate);
                    checkWeekAvailability(cal, noOfDaysAvail, rc);

                }
                else {
                	calnder.setTime(new java.util.Date(Calendar.getInstance().getTimeInMillis()));
        			while (count < 5) {
        				int k = calnder.get(Calendar.DAY_OF_WEEK);
        				for(int j=0;j<noOfDaysAvail.size();j++) {
        					if(k == noOfDaysAvail.get(j)) {
        						rc.datesArray.add(new java.sql.Date(calnder.getTimeInMillis()));
        						rc.tempDatesArray.add(new java.sql.Date(calnder.getTimeInMillis()));
        						rc.dayOfweek.add(calnder.get(Calendar.DAY_OF_WEEK)-1);
        						count++;
        					}
        				}
        				calnder.add(Calendar.DATE, 1);
        			}
                }
            }else {
                while (count < 5) {
                    int k = calnder.get(Calendar.DAY_OF_WEEK);
                    for(int j=0;j<noOfDaysAvail.size();j++) {
                        if(k == noOfDaysAvail.get(j)) {
                            rc.datesArray.add(new java.sql.Date(calnder.getTimeInMillis()));
                            rc.tempDatesArray.add(new java.sql.Date(calnder.getTimeInMillis()));
                            rc.dayOfweek.add(calnder.get(Calendar.DAY_OF_WEEK)-1);
                            count++;
                        }
                    }
                    calnder.add(Calendar.DATE, 1);
                }
            }
        }else {
            if(day!=null && !day.equals("")){
                if (day.equals("nextDay")){
                    calnder.add(Calendar.DATE, 1);
                    newDate =  new Date(calnder.getTimeInMillis());
                }else if (day.equals("prevDay")){
                    calnder.add(Calendar.DATE, -1);
                    newDate = new Date(calnder.getTimeInMillis());
                }else if(day.equals("nextWeek")){
                       Calendar cal = Calendar.getInstance();
                        int week = 1;
                        cal = getStartDayOfNextOrPreviousWeek(week, cal, choosenWeekDate);
                        newDate=new Date(cal.getTimeInMillis());
                }else if(day.equals("prevWeek")){
                      Calendar cal = Calendar.getInstance();
                       int week = -1;
                       cal = getStartDayOfNextOrPreviousWeek(week, cal, choosenWeekDate);
                       newDate=new Date(cal.getTimeInMillis());
                }else {
                    calnder.setTime(new java.util.Date(Calendar.getInstance().getTimeInMillis()));
                    newDate=new Date(calnder.getTimeInMillis());
                }
            }else {
                newDate = date ;
            }

            calnder.setTime(new java.util.Date(newDate.getTime()));
            for(int i=0;i<5;i++) {
                rc.datesArray.add(new java.sql.Date(calnder.getTimeInMillis()));
                rc.tempDatesArray.add(new java.sql.Date(calnder.getTimeInMillis()));
                rc.dayOfweek.add(calnder.get(Calendar.DAY_OF_WEEK)-1);
                calnder.add(Calendar.DATE, 1);
            }
        }

        rc.choosendate =  new Date(rc.datesArray.get(0).getTime());
    }

    private void checkWeekAvailability(Calendar calnder, ArrayList<Integer> noOfDaysAvail, ResourceCriteria rc){

        int count = 0;
        while (count < 5) {
            int k = calnder.get(Calendar.DAY_OF_WEEK);
            for(int j=0;j<noOfDaysAvail.size();j++) {
                if(k == noOfDaysAvail.get(j)) {
                    rc.datesArray.add(new java.sql.Date(calnder.getTimeInMillis()));
                    rc.tempDatesArray.add(new java.sql.Date(calnder.getTimeInMillis()));
                    rc.dayOfweek.add(calnder.get(Calendar.DAY_OF_WEEK)-1);
                    count++;
                }
            }
            calnder.add(Calendar.DATE, 1);
        }

    }

    private Calendar getStartDayOfNextOrPreviousWeek(int week, Calendar calndr, String choosenWeekDate) throws Exception{

        Integer startDay = GenericPreferencesDAO.getCalendarStartDay();
        if(startDay == null){
            startDay = 1;
        }
        calndr.setTime(new java.util.Date(DataBaseUtil.parseDate(choosenWeekDate).getTime()));
        calndr.set(Calendar.WEEK_OF_YEAR, calndr.get(Calendar.WEEK_OF_YEAR)+ week );
        calndr.set(Calendar.DAY_OF_WEEK, startDay+1);
        return calndr;
    }

    @IgnoreConfidentialFilters
    public ActionForward getNextslot(ActionMapping am, ActionForm af,
    		HttpServletRequest req,HttpServletResponse res) throws SQLException,IOException,Exception{

        HttpSession session = req.getSession();
    	int centerId = (Integer)session.getAttribute("centerId");
    	JSONSerializer js = new JSONSerializer();
        String resourceId = req.getParameter("doctor_id");
        //redirect.addParameter("doctor_id", resourceId);
        String reschDate = req.getParameter("date");
        String category = req.getParameter("category");
        String nextSlotStr = null;
        Map<String, String> map = new HashMap<String, String>();
        if(reschDate.equals("")) {
        	nextSlotStr = "";
        	//redirect.addParameter("nextAvailableSlotForReschedule", nextSlotStr);
        	map.put("nextAvailableSlotForReschedule", "");
        } else {
            nextSlotStr = calculateNextAvailableSlotForResource(reschDate, category, resourceId,centerId);
            String[] arr = nextSlotStr.split(" ");
            if(arr != null && arr.length > 0) {
        	    //redirect.addParameter("nextAvailableSlotForReschedule", arr[0]);
            	map.put("nextAvailableSlotForReschedule", arr[0]);
            } else {
        	    //redirect.addParameter("nextAvailableSlotForReschedule", nextSlotStr);
            	map.put("nextAvailableSlotForReschedule", nextSlotStr);
            }
        }
        res.setContentType("application/x-json");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		res.getWriter().write(js.serialize(map));
		res.flushBuffer();
        return null;
    	//return redirect;
    }

    private String calculateNextAvailableSlotForResource(String dateStr, String category, String resourceId,Integer centerId)
    		throws SQLException,IOException,Exception{
        Date d = new java.sql.Date(DateUtil.parseDate(dateStr).getTime());
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int weekDayNo = (cal.get(Calendar.DAY_OF_WEEK)-1);
    	int slotDuration = -1;
    	BasicDynaBean resourceAttrBean = null;
        List<BasicDynaBean> resourceAvailabilityList = null;
    	resourceAvailabilityList = new ResourceDAO(null).getResourceAvailabilities(category, d, resourceId,null,centerId);
    	if (resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
    	 	resourceAttrBean = new ResourceDAO(null).getDefaultAttributesOfResource(category, resourceId);
    	 	if(resourceAttrBean == null) {
    	 		resourceAttrBean = new ResourceDAO(null).getDefaultAttributesOfResource(category, "*");
    	 	}
	 		slotDuration = ((Integer)(resourceAttrBean.get("default_duration"))).intValue();
    	}
    	if (resourceAvailabilityList != null && resourceAvailabilityList.size() < 1) {
            resourceAvailabilityList = new ResourceDAO(null).getResourceDefaultAvailabilities(resourceId, weekDayNo, category,null,centerId);
        }

        if (resourceAvailabilityList != null && resourceAvailabilityList.size() < 1) {
            resourceAvailabilityList = new ResourceDAO(null).getResourceDefaultAvailabilities("*", weekDayNo, category,null,centerId);
        }
        String key = DateUtil.formatDate(d);
        String primaryResourceType = null;
        if(category.equals("DOC")) {
        	primaryResourceType = "OPDOC";
        }
   		String startTimeAndDuration = null;
   		boolean gotNextSlot = false;
        if(resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
            for(BasicDynaBean resBean : resourceAvailabilityList) {
              	if(((String)resBean.get("availability_status")).equalsIgnoreCase("A") ) {
               		Time fromTime = (Time)resBean.get("from_time");
               		Time toTime = (Time)resBean.get("to_time");
               		Integer availCenterId = (Integer) resBean.get("center_id");
               		if(slotDuration == -1) {
                        slotDuration = ((Integer)(resBean.get("default_duration"))).intValue();
               		}
               		for(long l = fromTime.getTime(); l < toTime.getTime(); l = l + (slotDuration*60*1000)) {
               			Time startTime = new Time(l);
               			java.sql.Timestamp startTimestamp = DateUtil.parseTimestamp(key + " " + DateUtil.formatSQlTime(startTime));
               			if(startTimestamp.before(new java.sql.Timestamp(System.currentTimeMillis()))) {
               				continue;
               			}
               			Time endTime = new Time(l + (slotDuration*60*1000));
               			java.sql.Timestamp endTimestamp = DateUtil.parseTimestamp(key + " " + DateUtil.formatSQlTime(endTime));
                	    List<BasicDynaBean> list = new ResourceDAO(null).isSlotBooked(startTimestamp, endTimestamp, resourceId, null, resourceId, primaryResourceType);
                	    if(list == null || (list != null && list.size() < 1)) {
                	    	// check if resource is not booked
                	    	BasicDynaBean bean = new ResourceDAO(null).isResourceBooked(startTimestamp, endTimestamp, resourceId, "DOC", -1, category);
                	    	if(bean == null) {
                	    	    startTimeAndDuration = DateUtil.formatSQlTime(startTime) + " " + slotDuration+"-"+availCenterId;
                	    	    //nextSlotMap.put(key, startTimeAndDuration);
            				    gotNextSlot = true;
                	    	    break;
                	    	}
                	    }
               		}
               		if(gotNextSlot) {
               			break;
               		}
               	}
            }
        }
        if(!gotNextSlot) {
        	startTimeAndDuration = "None ";
        }
        return startTimeAndDuration;
    }

    private String checkAllResourcesAvailability(Timestamp startAppointmentTime, Timestamp endAppointmentTime, String colDate, List resourcesList, List resourceTypesList, Integer appCenId) throws Exception {
        Timestamp startAvailTime = null;
        Timestamp endAvailTime = null;
        String resourceName = null;
        Integer apptCenterId = appCenId;
        Date availDate = DataBaseUtil.parseDate(colDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(availDate);
        int max_center = (Integer)new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default");
        Integer LoggedinCenterId = max_center > 1 ? RequestContext.getCenterId():null;
        int dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK)-1);
        for (int i=0;i<resourcesList.size();i++) {
        	String resId = (String)resourcesList.get(i);
        	String resType = (String)resourceTypesList.get(i);
            if ( resId != null && !resId.equals("")) {
                List resourceAvailList = new ArrayList();
                if(resourceTypesList != null && resType.equals("OPDOC") || resType.equals("SUDOC") ||
                		resType.equals("ANEDOC") || resType.equals("LABTECH") || resType.equals("DOC")) {
                    resType = "DOC";
                } else {
                    resType = (String)resourceTypesList.get(i);
                }
                resourceAvailList = new ResourceDAO(null).getResourceAvailabilities(resType, availDate, resId, null,LoggedinCenterId);
                if (resourceAvailList != null && resourceAvailList.size() < 1) {
                    resourceAvailList = new ResourceDAO(null).getResourceDefaultAvailabilities(resId, dayOfWeek, resType, null,LoggedinCenterId);
                }
                if (resourceAvailList != null && resourceAvailList.size() < 1) {
                    resourceAvailList = new ResourceDAO(null).getResourceDefaultAvailabilities("*", dayOfWeek, resType, null,LoggedinCenterId);
                }

                BasicDynaBean resourceBaen = null;
                String err = null;
                boolean resAvailable = true;
                for(int j=0;j<resourceAvailList.size();j++) {
                    resourceBaen = (BasicDynaBean)resourceAvailList.get(j);
                  //check next available slot is same as appointment center slot
                    if(max_center > 1){
	                    if (resourceBaen.get("availability_status").equals("A")) {
	                    	 Time fromTime = (java.sql.Time)resourceBaen.get("from_time");
	                         SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	                         String from_time = sdf.format(fromTime);
	                         DateUtil dateUtil = new DateUtil();
	                         startAvailTime = new Timestamp(dateUtil.getTimeStampFormatterSecs().parse(colDate + " " + from_time).getTime());
	                         Time toTime = (java.sql.Time)resourceBaen.get("to_time");
	                         String to_time = sdf.format(toTime);
	                         endAvailTime = new Timestamp(dateUtil.getTimeStampFormatter().parse(colDate + " " + to_time).getTime());
	                         Integer dbAvailCenterId = (Integer) resourceBaen.get("center_id");
	                         if(!dbAvailCenterId.equals(0)){

		                         if(!apptCenterId.equals(dbAvailCenterId)){
			                         if ((startAppointmentTime.getTime() <= startAvailTime.getTime() && endAppointmentTime.getTime() > startAvailTime.getTime())
			                                 || (startAppointmentTime.getTime() >= startAvailTime.getTime() && startAppointmentTime.getTime() < endAvailTime.getTime())){


			                        		 resourceName = new ResourceDAO(null).getResourceName(resType,resId);
			                             	resAvailable =  false;
			                                 err = "Not enough vacant appointment slots available for " + " " + resourceName;
			                                 break;
			                         }
		                         }
	                         }
	                    }
                    }

                    if (resourceBaen.get("availability_status").equals("N")) {
                        Time fromTime = (java.sql.Time)resourceBaen.get("from_time");
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        String from_time = sdf.format(fromTime);
                        DateUtil dateUtil = new DateUtil();
                        startAvailTime = new Timestamp(dateUtil.getTimeStampFormatterSecs().parse(colDate + " " + from_time).getTime());
                        Time toTime = (java.sql.Time)resourceBaen.get("to_time");
                        String to_time = sdf.format(toTime);
                        endAvailTime = new Timestamp(dateUtil.getTimeStampFormatter().parse(colDate + " " + to_time).getTime());

                        if ((startAppointmentTime.getTime() <= startAvailTime.getTime() && endAppointmentTime.getTime() > startAvailTime.getTime())
                                   || (startAppointmentTime.getTime() >= startAvailTime.getTime() && startAppointmentTime.getTime() < endAvailTime.getTime())){
                            resourceName = new ResourceDAO(null).getResourceName(resType,resId);
                        	resAvailable =  false;
                            err = "Not enough vacant appointment slots available for " + " " + resourceName;
                            break;
                        }
                    }
                }
                if(!resAvailable)
                	return err;
            }
        }
        return null;
    }

    @IgnoreConfidentialFilters
    public ActionForward getScheduleDetails(ActionMapping am,ActionForm af,
            HttpServletRequest req,HttpServletResponse res) throws SQLException,IOException,Exception{

        String message = null;
        String incResources = req.getParameter("includeResources");
        String choosendate = req.getParameter("date");
        String hijriDate = req.getParameter("hijriDate");
        String gregDateStr = req.getParameter("gregDate");

        java.util.Date date = null;

        String day = req.getParameter("day");

        ResourceCriteria rc = new ResourceCriteria();
        JSONSerializer json = new JSONSerializer();

        String allowbackDated = null;
        int roleId = (Integer)req.getSession().getAttribute("roleId");
        if((roleId != 1) && (roleId != 2)){
        	allowbackDated = (String)((Map)req.getSession(false).getAttribute("actionRightsMap")).get("allow_backdated_app");
        }
        else {
        	allowbackDated = "A";
        }
        List defaultList = null;
        String category =  am.getProperty("category");
        String deptname = req.getParameter("department");
        deptname = (deptname == null) ? "" : deptname;
        HttpSession session = req.getSession();
        int centerId = (Integer)session.getAttribute("centerId");
        String reqCenterId = req.getParameter("centerId");
        centerId = (reqCenterId != null && !reqCenterId.isEmpty()) ? Integer.parseInt(reqCenterId) : centerId;
        String userName = (String)(req.getSession(false).getAttribute("userid"));



        String dept = (String)req.getSession().getAttribute("scheduler_dept_id");
        dept = (dept == null) ? "" : dept;
        Map filterMap = new HashMap();
        filterMap.put("status", "A");

        if (dept.equals("")) {
            req.setAttribute("userDepartments", ConversionUtils.listBeanToListMap(new DepartmentMasterDAO().listAll(Collections.EMPTY_LIST,filterMap,"dept_name")));
        }else {
            req.setAttribute("userDepartments", ConversionUtils.listBeanToListMap(new DepartmentMasterDAO().findAllByKey("dept_id", dept)));
        }

        if (dept == null || dept.equals("")) {
            dept = deptname;
        }
        rc.department = dept;
        if(hijriDate != null && !hijriDate.equals("")) {
        	String[] params = hijriDate.split("-");
            UmmalquraCalendar cal = new UmmalquraCalendar(Integer.parseInt(params[0]), Integer.parseInt(params[1]) - 1, Integer.parseInt(params[2]));
            date = cal.getTime();
        } else if(gregDateStr != null && !gregDateStr.equals("")){
        	String[] params = gregDateStr.split("-");
            Calendar cal = new GregorianCalendar(Integer.parseInt(params[0]), Integer.parseInt(params[1])-1, Integer.parseInt(params[2]));
            int[] hDateInfo = UmmalquraGregorianConverter.toHijri(cal.getTime());
            hijriDate = hDateInfo[0] + "-" + (hDateInfo[1]+1) + "-" + hDateInfo[2];
            date = cal.getTime();
        }
        if(date != null) {
            choosendate = DateUtil.formatDate(date);
        } else if(choosendate == null){
        	date =  DateUtil.getCurrentDate();
            choosendate = DateUtil.formatDate(date);
            int[] hDateInfo = UmmalquraGregorianConverter.toHijri(date);
            hijriDate = hDateInfo[0] + "-" + (hDateInfo[1]+1) + "-" + hDateInfo[2];
        } else {
        	date = DateUtil.parseDate(choosendate);
            int[] hDateInfo = UmmalquraGregorianConverter.toHijri(date);
            hijriDate = hDateInfo[0] + "-" + (hDateInfo[1]+1) + "-" + hDateInfo[2];
        }
        if (day!=null && !day.equals("")){
            java.sql.Date date1 = DataBaseUtil.parseDate(choosendate);
            Calendar calnder = Calendar.getInstance();
            calnder.setTime(new java.util.Date(date1.getTime()));

            if (day.equals("Next")){
                calnder.add(Calendar.DATE, 1);
                java.util.Date newDate =  calnder.getTime();
                rc.choosendate = new java.sql.Date(newDate.getTime());
                date = newDate;
                int[] hDateInfo = UmmalquraGregorianConverter.toHijri(date);
                hijriDate = hDateInfo[0] + "-" + (hDateInfo[1]+1) + "-" + hDateInfo[2];
            }else if (day.equals("Prev")){
                calnder.add(Calendar.DATE, -1);
                java.util.Date newDate =  calnder.getTime();
                rc.choosendate = new java.sql.Date(newDate.getTime());
                date = newDate;
                int[] hDateInfo = UmmalquraGregorianConverter.toHijri(date);
                hijriDate = hDateInfo[0] + "-" + (hDateInfo[1]+1) + "-" + hDateInfo[2];
            }else {
                rc.choosendate =  DateUtil.getCurrentDate();
                date = rc.choosendate;
                int[] hDateInfo = UmmalquraGregorianConverter.toHijri(date);
                hijriDate = hDateInfo[0] + "-" + (hDateInfo[1]+1) + "-" + hDateInfo[2];
            }
        }else {
            java.sql.Date date1 = DataBaseUtil.parseDate(choosendate);
            rc.choosendate = date1;
        }

        String resourcesCommaSeperated ="";
        Set<String> uniqueScheduleName = new LinkedHashSet();
        Set<String> uniqueSchName = new LinkedHashSet<>();
        if (incResources != null && !incResources.equals("")) {
            Object[] includeResources = incResources.split(",");
            for (Object resource : includeResources) {
                if (String.valueOf(resource).trim().isEmpty()) {
                    continue;
                }
                if(!uniqueScheduleName.contains(String.valueOf(resource))) {
                    uniqueScheduleName.add(String.valueOf(resource));
                    uniqueSchName.add(String.valueOf(resource));
                    resourcesCommaSeperated = resourcesCommaSeperated + "," + resource;
                }
            }
            rc.scheduleName.addAll(uniqueScheduleName);
            rc.schName.addAll(uniqueSchName);
        } else if (incResources == null || incResources.equals("")) {
            defaultList = new ResourceDAO(null).getDefaultHeaders(category, dept,centerId, userName, roleId);
            if (defaultList != null && defaultList.size()>0) {
                  for (int i = 0; i < defaultList.size(); i++) {
                    BasicDynaBean dbean = (BasicDynaBean) defaultList.get(i);
                    if (dbean.get("resource_id") != null && !dbean.get("resource_id").equals("")
                        && !dbean.get("resource_id").equals("null")) {
                      uniqueScheduleName.add(String.valueOf(dbean.get("resource_id")));
                      uniqueSchName.add(String.valueOf(dbean.get("resource_id")));
                      resourcesCommaSeperated = resourcesCommaSeperated + "," + dbean.get("resource_id");
                    }
                  }
                rc.scheduleName.addAll(uniqueScheduleName);
                rc.schName.addAll(uniqueSchName);
            } else {
                String error = "";
                if (category.equals("DOC"))
                    error = "No Schedulable Doctor Available. Please add a schedulable doctor in doctor master";

                else if (category.equals("SNP") || category.equals("DIA"))
                    error = "No Schedulable Equipment Available. Please add a schedulable equipment in equipment master";

                else if (category.equals("OPE"))
                    error = "No Schedulable Operation Theatre Available. Please add a schedulable theatre in theatre master";

                req.setAttribute("error", error);
                req.setAttribute("referer", req.getHeader("Referer"));
                return am.findForward("reportErrors");
            }
        }

        rc.category =  category;
        int width = 100/rc.scheduleName.size();
        req.setAttribute("width", width);

        message = ResourceBO.validateScheduler(rc);

        if(message != null) {
            req.setAttribute("referer", req.getHeader("Referer"));
            req.setAttribute("error",message);
            return am.findForward("reportErrors");
        }

        resourcesCommaSeperated=resourcesCommaSeperated.substring(1,resourcesCommaSeperated.length());
        int count = new ResourceDAO(null).getReschedulableAppCount(rc.scheduleName,rc.choosendate,rc.choosendate,rc.category);

        if(count > 0) {
            if(req.getAttribute("info") != null && !req.getAttribute("info").equals("")) {
                message = count + " appointments have to be rescheduled..." + req.getAttribute("info").toString();
                req.removeAttribute("success");
            } else {
                message = count + " appointments have to be rescheduled...";
            }
        }

        if(req.getParameter("info") != null && !req.getParameter("info").equals("")) {
            message = (message == null) ? req.getParameter("info").toString() : message + req.getParameter("info");
            req.setAttribute("info",message);
        }

        if(req.getParameter("error") != null && !req.getParameter("error").equals("")) {
            message = (message == null) ? req.getParameter("error").toString() : message + req.getParameter("error");
            req.setAttribute("error",message);
        }

        req.setAttribute("date", rc.choosendate);
        req.setAttribute("includeResources", resourcesCommaSeperated);
        req.setAttribute("department", rc.department);
        req.setAttribute("allowbackDated", allowbackDated);
        //req.setAttribute("hijriDate", arg1);
        String gregDate = DateUtil.formatDateToYearAhead(date);
        String gregoDateStr = DateUtil.formatDateToWeekDay(date);
        req.setAttribute("dateString", gregoDateStr);
        req.setAttribute("gregoDate",gregDate);
        req.setAttribute("hijriCalDate", hijriDate);
        setAttributes(am, req, rc,"DayView",centerId);

        Map resourceCalndarList = ResourceBO.getCalendarList(rc, "DayView",centerId, userName);

        req.setAttribute("canlderList", resourceCalndarList);
        req.setAttribute("availbiltyList" , json.serialize(resourceCalndarList.get("headers")));
		List doctorsList = DoctorMasterDAO.getDoctorDepartmentsDynaList();
		req.setAttribute("doctorsJSON", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(doctorsList)));
        String countryCode = centerDao.getCountryCode(RequestContext.getCenterId());
		if(StringUtil.isNullOrEmpty(countryCode)){
		    countryCode = centerDao.getCountryCode(0);
		}
		req.setAttribute("defaultCountryCode", countryCode);
		req.setAttribute("countryList", PhoneNumberUtil.getAllCountries());

        return am.findForward("showDayView");
    }

    private void setAttributes(ActionMapping am,HttpServletRequest request, ResourceCriteria rc,String view,Integer centerId) throws Exception {
        JSONSerializer js = new JSONSerializer().exclude("class");
        String category =  am.getProperty("category");
        JSONSerializer json = new JSONSerializer().exclude("class");
        //String dept = (String)request.getSession().getAttribute("scheduler_dept_id");
        List<BasicDynaBean> scheduleResourceList = null;
        BasicDynaBean genPrefs =  GenericPreferencesDAO.getAllPrefs();
        String opApplicableFor =  (String)genPrefs.get("operation_apllicable_for");
        HttpSession session = request.getSession();
        String resourceType = null;
        String userDetail = (String)session.getAttribute("userid");
        int roleId = (Integer)request.getSession().getAttribute("roleId");

        if (rc.category.equals("DOC")) {
            resourceType = "DOC";
        } else if (rc.category.equals("DIA")) {
            resourceType = "EQID";
        } else if (rc.category.equals("SNP")) {
            resourceType = "SRID";
        } else if (rc.category.equals("OPE")) {
            resourceType = "THID";
        }

        //request.setAttribute("complaints", json.serialize(ComplaintMasterDAO.getAllComplaints()));
        request.setAttribute("category", category);

        if(category.equals("DOC")){
            request.setAttribute("title","Doc ");
            request.setAttribute("heading", "Doctor Scheduler");
            scheduleResourceList = new ResourceDAO(null).getScheduleResourceDoctorsList(rc.department,centerId);
            request.setAttribute("consultationTypeForIp", ConversionUtils.copyListDynaBeansToMap(
                    com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO.getConsultationTypes("i")));
            request.setAttribute("consultationTypeForOp", ConversionUtils.copyListDynaBeansToMap(
                    com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO.getConsultationTypes("o")));

            request.setAttribute("primaryResourceCentersList",js.serialize(new ResourceDAO(null).getResourceCentersJsonList("DOC")));

        }else if(category.equals("OPE")){
            request.setAttribute("title","Surg ");
            request.setAttribute("heading", "Surgery Scheduler");
            scheduleResourceList = new ResourceDAO(null).getScheduleResourceTheatresList(RequestContext.getCenterId() == 0 ? RequestContext.getCenterId() : centerId, userDetail, roleId);
            request.setAttribute("opApplicableFor", opApplicableFor);
            request.setAttribute("surgeriesJson",js.serialize(new ResourceDAO(null).getJsonList("SUR")));
            request.setAttribute("primaryResourceCentersList",js.serialize(new ResourceDAO(null).getResourceCentersJsonList("SUR")));

        }else if(category.equals("SNP")){
            request.setAttribute("title","Serv ");
            request.setAttribute("heading", "Services Scheduler");
            scheduleResourceList = new ResourceDAO(null).getScheduleServiceResourceList(rc.department,RequestContext.getCenterId() == 0 ? RequestContext.getCenterId() : centerId);
            request.setAttribute("primaryResourceCentersList",js.serialize(new ResourceDAO(null).getResourceCentersJsonList("SER")));

        }else if(category.equals("DIA")){
            request.setAttribute("title","Tests ");
            request.setAttribute("heading", "Test Scheduler");
            scheduleResourceList = new ResourceDAO(null).getScheduleResourceEquipmentsList(rc.department,RequestContext.getCenterId() == 0 ? RequestContext.getCenterId() : centerId);
            request.setAttribute("primaryResourceCentersList",js.serialize(new ResourceDAO(null).getResourceCentersJsonList("TES")));

        }
        List statusList = new ResourceDAO(null).getStatusList();
        request.setAttribute("statusListJSON", json.serialize(statusList));
        List deptList = DepartmentMasterDAO.getAllDepartmentsList();

        request.setAttribute("scheduleResourceList", scheduleResourceList);
        request.setAttribute("scheduleResourceListJSON", json.serialize(ConversionUtils.listBeanToListMap(scheduleResourceList)));
        request.setAttribute("departmentListJSON", json.serialize(deptList));

        List mappedRes = new ResourceDAO(null).getMappedSecondayResource(category);
        request.setAttribute("mappedRes", js.serialize(ConversionUtils.listBeanToListMap(mappedRes)));
        
        String defaultDuration = DataBaseUtil.getStringValueFromDb("Select default_duration from scheduler_master"
            + " where res_sch_type=? and res_sch_name='*'", resourceType);

        request.setAttribute("defaultDuration",defaultDuration);

        Schedule s = ResourceBO.getTimingList(rc,view);
        request.setAttribute("timings", s);
        request.setAttribute("timingsJson", json.serialize(s));
        request.setAttribute("defaultTimeSlotsJSON", json.serialize(s.getTimeList()) );

        List resourcTypes = new ResourceDAO(null).getSecondoryResourceTypes(rc);
        if(resourcTypes!=null){
            request.setAttribute("resourceTypesJSON", json.serialize(ConversionUtils.copyListDynaBeansToMap(resourcTypes)));
            request.setAttribute("resourceTypes", resourcTypes);
        }

        List resourcesList = new ResourceDAO(null).getResources(rc.category,RequestContext.getCenterId() == 0 ? RequestContext.getCenterId() : centerId);
        request.setAttribute("resourcesList", resourcesList);
        if(resourcesList !=null)
            request.setAttribute("resourcesListJSON", json.serialize(ConversionUtils.copyListDynaBeansToMap(resourcesList)));

        request.setAttribute("filterClosed", true);

        BasicDynaBean mst = (BasicDynaBean) new GenericDAO("master_timestamp").getRecord();
        request.setAttribute("masterTimeStamp", mst.get("master_count"));

        java.sql.Timestamp timestamp = DataBaseUtil.getDateandTime();
        request.setAttribute("currentTime", timestamp);

        RegistrationPreferencesDTO regPrefs = RegistrationPreferencesDAO.getRegistrationPreferences();
        request.setAttribute("regPrefJSON", js.serialize(RegistrationPreferencesDAO.getRegistrationPreferences()));
        request.setAttribute("healthAuthoPrefJSON", new JSONSerializer().exclude("class").serialize
                (HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(CenterMasterDAO.getHealthAuthorityForCenter(centerId))));
        String allowMultipleActiveVisits = (regPrefs.getAllow_multiple_active_visits() != null
                && !regPrefs.getAllow_multiple_active_visits().equals(""))
            ? regPrefs.getAllow_multiple_active_visits() : "N";
        request.setAttribute("allowMultipleActiveVisits", allowMultipleActiveVisits);
        request.setAttribute("centers", centerDao.getAllCentersExceptSuper());
        request.setAttribute("allCenters", centerDao.getAllCentersAndSuperCenterAsFirst());

        HttpSession  ses = request.getSession(false);
        String userName = (String)ses.getAttribute("userid");
        request.setAttribute("userCenterId", (Integer)ses.getAttribute("centerId"));
        request.setAttribute("userCenterName", (String)ses.getAttribute("centerName"));
        request.setAttribute("user_groups", ses.getAttribute("user_groups"));
        
        request.setAttribute("genPrefs", GenericPreferencesDAO.getAllPrefs());
    }


    @IgnoreConfidentialFilters
    public ActionForward getResourceList(ActionMapping am,ActionForm af,
            HttpServletRequest request,HttpServletResponse response)throws IOException,Exception{
    	 ResourceCriteria rc = new ResourceCriteria();
         String category = request.getParameter("category");
         String scheduleName = request.getParameter("scheduleName");
         String scheduleDate = request.getParameter("scheduleDate");
         String startTime = request.getParameter("from_time");
         String endTime = request.getParameter("to_time");
         String resourceType = request.getParameter("resource_type");
         //passed center id to get only availability time center wise in time drop down in scheduler screen.
         HttpSession session = request.getSession();
         String resourceCenterId = request.getParameter("resourceCenterId");
         Schedule s = ResourceBO.getResourceAndTimingList(category, scheduleName,scheduleDate,startTime,endTime,resourceType,Integer.parseInt(resourceCenterId));
         String reposneContent = new JSONSerializer().deepSerialize(s);

         response.setContentType("application/json");
         response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
         response.getWriter().write(reposneContent);
         response.flushBuffer();

        return null;
    }

    public ActionForward getMappedResources(ActionMapping am,ActionForm af,
            HttpServletRequest request,HttpServletResponse response) throws SQLException, IOException {
      JSONSerializer js = new JSONSerializer().exclude("class");
      HttpSession session = request.getSession();
      int centerId = (Integer)session.getAttribute("centerId");
      String userName = (String)session.getAttribute("userid");
      int roleId = (Integer)request.getSession().getAttribute("roleId");
      String schName = request.getParameter("schName");
      String category = request.getParameter("category");
      List<BasicDynaBean> mappedResourceList =  new ResourceDAO(null).getMappedResources(schName, category, centerId, userName, roleId);
      response.setContentType("application/json");
      response.setHeader("Cache-Control", "no-cache");
      response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(mappedResourceList)));
      response.flushBuffer();
      
      return null;
    }

    @IgnoreConfidentialFilters
    public ActionForward getPrimaryResourceUnAvailableTimings(ActionMapping am,ActionForm af,
            HttpServletRequest request,HttpServletResponse response)throws IOException,Exception{

         String category = request.getParameter("category");
         String scheduleName = request.getParameter("scheduleName");
         String scheduleDate = request.getParameter("scheduleDate");

         List timingList = ResourceBO.getPrimaryResourceUnavailTimings(category, scheduleName,scheduleDate);
         String responseContent = new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(timingList));

         response.setContentType("application/json");
         response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
         response.getWriter().write(responseContent);
         response.flushBuffer();

        return null;
    }

    @IgnoreConfidentialFilters
    public ActionForward getScheduleNames(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
        throws IOException, SQLException, ParseException {

        if (null != request.getHeader("If-Modified-Since")) {
            // the browser is just requesting the same thing, only if modified.
            // Since we encode the timestamp in the request, if we get a request,
            // it CANNOT have been modified. So, just say 304 not modified without checking.
            response.setStatus(304);
            return null;
        }
        ResourceDAO dao=resourceDAO;
        List resNames=null;
        JSONSerializer js = new JSONSerializer().exclude("class");

        if(request.getParameter("category").equals("OPE"))
            resNames=dao.getScheduleNamesList("OPE");
        else if(request.getParameter("category").equals("SNP"))
            resNames=dao.getScheduleNamesList("SNP");
        else if(request.getParameter("category").equals("DIA"))
            resNames=dao.getScheduleNamesList("DIA");

        // Last-Modified is required, cache-control is good to have to enable caching
        response.setHeader("Last-modified", "Thu, 1 Jan 2009 00:00:00 GMT");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "public; max-age=360000");
        response.setContentType("text/javascript");

        response.getWriter().write("var resNames = ");
        response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(resNames)));
        response.getWriter().write(";");
        return null;
    }

    // ignored, since its not a POST and making as post redirecting to arrival again. HMS-27815
    @IgnoreConfidentialFilters
    public ActionForward saveAppointment(ActionMapping am,ActionForm af,
            HttpServletRequest request,HttpServletResponse response)throws IOException,Exception {

        String category = (String)request.getParameter("category");
        ActionRedirect redirect =null;
        if (am.getProperty("action_id").endsWith("_week_scheduler"))
            redirect = new ActionRedirect(am.findForward("weekViewRedirect"));
        else
            redirect = new ActionRedirect(am.findForward("dayViewRedirect"));
         redirect.addParameter("category", category);
        FlashScope flash = FlashScope.getScope(request);
        Map map = request.getParameterMap();

        HttpSession session = request.getSession(false);
        Map urlRightsMap = (Map)session.getAttribute("urlRightsMap");
        //Map actionRightsMap = (Map)session.getAttribute("actionRightsMap");
        //String newBillRights = actionRightsMap.get("new_bill_for_order_screen") != null ? (String)actionRightsMap.get("new_bill_for_order_screen") : "N";
        //String orderRights = urlRightsMap.get("order") != null ? (String)urlRightsMap.get("order") : "N";
        String opIpRegScreenRights = urlRightsMap.get("op_ip_conversion") != null ? (String)urlRightsMap.get("op_ip_conversion") : "N";

        String newBillRights = "A";
        String orderRights = "A";

        ResourceCriteria rc = new ResourceCriteria();
        String dept = request.getParameter("department");
        String includeResources = request.getParameter("includeResources");
        String dateStr =(String)(request.getParameter("date").trim());
        String centralResourceId = (String)request.getParameter("centralResource");
        String timeStr=(String)request.getParameter("time");
        String timeStampStr = dateStr + " " + timeStr;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:mm");
        java.util.Date date = (java.util.Date)dateFormat.parse(timeStampStr);
        Timestamp appointmentTime = new java.sql.Timestamp(date.getTime());
        String durationStr = request.getParameter("duration");
        String autoArrival = request.getParameter("auto_arrival");
        String consultationTypeStr = request.getParameter("consultationTypes");
        int consultationTypeId = 0;
        String schedulerVisitType = request.getParameter("scheduler_visit_type");
        String presDocId = request.getParameter("presc_doc_id");
        String condDocId = request.getParameter("cond_doc_id");
        Bill bill = null;
        boolean isVisitActive = false;
        
        if (category.equals("DOC") && consultationTypeStr != null && !consultationTypeStr.equals(""))
            consultationTypeId =Integer.parseInt(request.getParameter("consultationTypes"));
        String mrno = null;
        Integer contactId = null;
        String visitId = null;
        String visitType = null;
        
        String cancelReason = null;
        String appointStatus = request.getParameter("status");
        String schPriorAuthId = request.getParameter("scheduler_prior_auth_no");
        int schPriorAuthModeId = 0;
        String schPriorAuthModeIdStr = request.getParameter("scheduler_prior_auth_mode_id");
        schPriorAuthModeId = (schPriorAuthModeIdStr != null && !schPriorAuthModeIdStr.equals("")) ? Integer.parseInt(schPriorAuthModeIdStr) : 0;
        String centerIdStr = "";
        if(null!= request.getParameter("center_id") && !"".equals(request.getParameter("center_id"))){
        	centerIdStr = request.getParameter("center_id");
        }else{
        	centerIdStr = request.getParameter("ah_center_id");
        }
        int centerId = Integer.parseInt(centerIdStr);

        if (appointStatus != null && appointStatus.equals("Cancel")) {
            cancelReason = request.getParameter("cancel_reason");
        }
        String patientName = request.getParameter("name");
        String salutationName = request.getParameter("salutationName");
        String phoneNo = request.getParameter("contact");
        String phoneCountryCode = request.getParameter("contact_country_code");
        mrno = request.getParameter("mrno");
        contactId = (request.getParameter("contactId") != null && !request.getParameter("contactId").equals("")) ?
            Integer.parseInt(request.getParameter("contactId")) : null;
        if ((contactId == null || contactId.equals("")) && (mrno == null || mrno.equals(""))) {
          BasicDynaBean contactBean = contactsDao.getBean();
          contactBean.set("patient_name",patientName);
          contactBean.set("patient_contact",phoneNo);
          contactBean.set("patient_contact_country_code",phoneCountryCode);
          contactBean.set("preferred_language", (String) genericPreferencesDAO.getAllPrefs().get(
              "contact_pref_lang_code"));
          contactBean.set("vip_status", "N");
          contactBean.set("send_sms", "N");
          contactBean.set("send_email", "N");
          if (patientDetailsDao.checkIfPatientExists(contactBean)) {
            redirect = new ActionRedirect(am.findForward("dayViewRedirect"));
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            flash.error("The patient details entered matches with an existing patient.");
            return redirect;
          } else {
            contactId = contactsDao.getContactIdIfContactExists(contactBean);
            if (contactId == null) {
              contactId = contactsDao.getNextSequence();
              contactBean.set("contact_id",contactId);
              contactBean.set("mod_user", (String)(request.getSession(false).getAttribute("userid")));
              contactsDao.insert(DataBaseUtil.getConnection(), contactBean);
            } else {
              redirect = new ActionRedirect(am.findForward("dayViewRedirect"));
              redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
              flash.error("The patient details entered matches with an existing patient.");
              return redirect;
            }
          }
          
        }
        visitId = request.getParameter("patient_id");
        visitId = (visitId != null && (visitId.equals("None") || visitId.equals(""))) ? null : visitId;
        String remarks = request.getParameter("remarks");

        String complaintName = request.getParameter("complaint");

        String scheduleNameForAppointmentStr = (String)request.getParameter("scheduleNameForAppointment");
        BasicDynaBean genPrefs =  GenericPreferencesDAO.getAllPrefs();
        
        if(category.equals("OPE") 
            && ((String) genPrefs.get("surgery_name_required")).equalsIgnoreCase("M") 
            && (scheduleNameForAppointmentStr == null || scheduleNameForAppointmentStr.equals(""))) {
          redirect = new ActionRedirect(am.findForward("addRedirect"));
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
          flash.error("exception.scheduler.error.surgery.name.mandatory");
          return redirect;
        }
        if(category.equals("SNP") 
            && ((String) genPrefs.get("service_name_required")).equalsIgnoreCase("M") 
            && (scheduleNameForAppointmentStr == null || scheduleNameForAppointmentStr.equals(""))) {
          redirect = new ActionRedirect(am.findForward("addRedirect"));
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
          flash.error("exception.scheduler.error.service.name.mandatory");
          return redirect;
        }
        int scheduleIdForAppointment = 0;
        /*
        boolean appexists = false;
        if(mrno != null && !mrno.equals("")) {
            appexists = PortalAppointmentBO.checkDuplicateAppointmentForDoctor(mrno, scheduleNameForAppointmentStr, dateStr);
        }else {
            appexists = PortalAppointmentBO.checkDuplicateAppointmentForDoctor(patientName, phoneNo, scheduleNameForAppointmentStr, dateStr);
        }

        if(appexists) {
            flash.put("info", "Appointment already exists on "+dateStr);
            redirect.addParameter("info", "Appointment already exists on "+dateStr);
        }else {*/

            String[] resourceNames = (String[])map.get("resourcename");
            String[] resourceValue = (String[])map.get("resourcevalue");
            String[] resourceDelete = (String[])map.get("rDelete");
            Timestamp bookedTime = DataBaseUtil.getDateandTime();

            int appointmentId = 0;
            ArrayList<AppointMentResource> scheduleAppointItemBean = new ArrayList<AppointMentResource>();
            ArrayList<AppointMentResource> scheduleAppointItemBeanRecuured = new ArrayList<AppointMentResource>();
            ArrayList<Appointments> scheduleAppointBeanList = new ArrayList<Appointments>();
            Connection con = null;
            String schedulerBillNo = null;
            Map resultMap = new HashMap();
            boolean status = false;
            String opApplicableFor = null;

            try{
                con = DataBaseUtil.getConnection();
                con.setAutoCommit(false);
                appointmentId = Integer.parseInt(new ResourceDAO(null).getNextAppointMentId());

                if(category.equals("DOC")){
                     centralResourceId = scheduleNameForAppointmentStr ;
                }

                //Schedule Details
                rc.scheduleName.add(scheduleNameForAppointmentStr);
                rc.category = category;
                BasicDynaBean scheduleBean = new ResourceDAO(null).getScheduleDetails(scheduleNameForAppointmentStr, rc.category);
                if (scheduleBean != null) {
                    scheduleIdForAppointment = (Integer)scheduleBean.get("res_sch_id");
                }

                
                int duration = 0;
                if(durationStr!=null && !durationStr.equals("")){
                    duration = Integer.parseInt(durationStr);
                }else if (scheduleBean != null) {
                    duration = ((Integer)scheduleBean.get("default_duration")).intValue();
                } else {

                }
                Integer overbookLimit = new ResourceDAO(null).isResourceOverbooked(con, centralResourceId, category);
                int overBookCount = new ResourceDAO(null).getOverbookCount(centralResourceId, appointmentTime);
                int overbookCount= overBookCount == 1 ? 0 : overBookCount ;
                
                Appointments app = new Appointments(appointmentId);
                
                if(overbookLimit != null && overbookLimit != 0 &&  overbookCount > overbookLimit){               	
              	    flash.error("The number of appointments booked for this slot has hit the overbook Limit.");
              	    redirect.addParameter("category", category);
              	    redirect.addParameter("includeResources", includeResources);
              	    redirect.addParameter("department", dept);
              	    redirect.addParameter("centerId", request.getParameter("centerId"));
                    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                    return redirect;
                }
                boolean overBookAllowed = overbookLimit == null || overbookLimit > 0;
                if (overBookAllowed){
                	app.setUnique_appt_ind(new ResourceDAO(null).getNextUniqueAppointMentInd());
                } else{
                	app.setUnique_appt_ind(0);
                }
                 app.setPrim_res_id(centralResourceId);
                 app.setResourceType(category);
                 app.setAppointmentId(appointmentId);
                 app.setMrNo(mrno);
                 app.setContactId(contactId);
                 app.setVisitId(visitId);
                 app.setPatientName(patientName);
                 app.setPhoneNo(phoneNo);
                 app.setPhoneCountryCode(phoneCountryCode);
                 app.setComplaint(complaintName);
                 app.setRemarks(remarks);
                 app.setScheduleId(scheduleIdForAppointment);
                 app.setScheduleName(scheduleNameForAppointmentStr);
                 app.setAppointStatus(appointStatus);
                 app.setBookedBy((String)(request.getSession(false).getAttribute("userid")));
                 app.setBookedTime(bookedTime);
                 app.setAppointmentDuration(duration);
                 app.setAppointmentTime(appointmentTime);
                 app.setCancelReason(cancelReason);
                 app.setChangedBy((String)(request.getSession(false).getAttribute("userid")));
                 app.setSchedulerVisitType(schedulerVisitType);
                 app.setCenterId(centerId);
                 app.setPrescDocId(presDocId);
                 app.setCondDocId(condDocId);
                 if (category.equals("DOC") && consultationTypeStr != null && !consultationTypeStr.equals("")) {
                         app.setConsultationTypeId(consultationTypeId);
                 }
                 if(category.equals("OPE")) {
                     app.setSchPriorAuthId(schPriorAuthId);
                     app.setSchPriorAuthModeId(schPriorAuthModeId);
                 }
                 app.setSalutationName(salutationName);
               //setting waitlist number
                 Integer waitlist = new ResourceDAO(null).getOverbookCount(centralResourceId, appointmentTime);
                 app.setWaitlist(waitlist);

                scheduleAppointBeanList.add(app);

                RecurranceDTO recdto = new RecurranceDTO();
                recdto.setRecurrNo(new Integer(request.getParameter("recurrNo")).intValue());
                recdto.setRecurranceOption(request.getParameter("recurranceOption"));

                //if checked then only get values
                recdto.setWeek(request.getParameterValues("week"));
                if(request.getParameter("recurrDate") != null && !request.getParameter("recurrDate").equals("")) {
                	String recurrDateStr = request.getParameter("recurrDate");
                    String recurTimeStampStr = recurrDateStr + " " + timeStr;
                    java.util.Date recurDate = (java.util.Date)dateFormat.parse(recurTimeStampStr);
                    recdto.setRecurrDate(new Timestamp(recurDate.getTime()));
                }
                recdto.setRepeatOption(request.getParameter("repeatOption"));
                if(request.getParameter("occurrNo") != null && !request.getParameter("occurrNo").equals(""))
                    recdto.setOccurrNo(new Integer(request.getParameter("occurrNo")).intValue());
                if(request.getParameter("untilDate") != null && !request.getParameter("untilDate").equals(""))
                    recdto.setUntilDate(new java.sql.Date(DataBaseUtil.parseDate(request.getParameter("untilDate")).getTime()));


                AppointMentResource res = null;

                HttpSession  ses = request.getSession(false);
                String UserName = (String)ses.getAttribute("userid");
                java.sql.Timestamp modTime = DataBaseUtil.getDateandTime();

                if(!centralResourceId.equals("")){
                    String resourceType = null;
                    if(category.equals("DOC")){
                        resourceType = "OPDOC";
                    }else if(category.equals("SNP")){
                        resourceType = "SRID";
                    }else if(category.equals("DIA")){
                        resourceType = "EQID";
                    }else if(category.equals("OPE")){
                        resourceType = "THID";
                    }
                    res = new AppointMentResource(appointmentId,resourceType,centralResourceId);
                    res.setAppointment_item_id(new GenericDAO("scheduler_appointment_items").getNextSequence());
                    res.setUser_name(UserName);
                    res.setMod_time(modTime);
                    scheduleAppointItemBean.add(res);
                }

                if(resourceNames !=null){
                    for(int i=0;i<resourceNames.length;i++){
                        if(!resourceDelete[i].equals("") && !resourceDelete[i].equals("Y")) {
                            if(!resourceNames[i].equals("") && !resourceValue[i].equals("")){
                                res = new AppointMentResource(appointmentId,resourceNames[i],resourceValue[i]);
                                res.setAppointment_item_id(new GenericDAO("scheduler_appointment_items").getNextSequence());
                                res.setUser_name(UserName);
                                res.setMod_time(modTime);
                                scheduleAppointItemBean.add(res);
                            }
                        }
                    }
                }


                Map<String, ArrayList> recMap = new HashMap<String, ArrayList>();
                recMap.put("scheduleAppointBeanList", scheduleAppointBeanList);
                recMap.put("scheduleAppointItemBean", scheduleAppointItemBean);
                recMap.put("scheduleAppointItemBeanRecuured", scheduleAppointItemBeanRecuured);

                ResourceBO.addRecurrances(recMap, recdto, UserName);

                scheduleAppointBeanList = recMap.get("scheduleAppointBeanList");
                scheduleAppointItemBean = recMap.get("scheduleAppointItemBean");
                scheduleAppointItemBeanRecuured = recMap.get("scheduleAppointItemBeanRecuured");

                do{
                    status = ResourceBO.saveAppointmentAndresources(con, scheduleAppointBeanList,scheduleAppointItemBean,scheduleAppointItemBeanRecuured);
                    if (!status) break;

                    if (mrno != null && !mrno.equals("")) {

                        if (visitId != null) {
                            BasicDynaBean activePatientBean = new VisitDetailsDAO().findByKey("patient_id", visitId);
                            visitType = (String)activePatientBean.get("visit_type");

                            if (category != null && category.equals("OPE")) {

                                opApplicableFor =  (String)genPrefs.get("operation_apllicable_for");

                                opApplicableFor = opApplicableFor.equals("b") ? visitType :  opApplicableFor;

                                if (visitType.equals("o") && !visitType.equals(opApplicableFor)) {
                                    if (!opIpRegScreenRights.equals("A")) {
                                        flash.error("You are not authorized to convert OP to IP Patient");
                                        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                                        return redirect;
                                    }
                                    redirect = new ActionRedirect(am.findForward("opIpConverstionScreen"));
                                    redirect.addParameter("patient_id", visitId);
                                    redirect.addParameter("appointment_id", appointmentId);
                                    redirect.addParameter("category", category);
                                    redirect.addParameter("mrno", mrno);
                                    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                                    return redirect;
                                }
                            }
                        }

                        if ((category.equals("DOC") && consultationTypeId != 0) || (scheduleNameForAppointmentStr != null && !scheduleNameForAppointmentStr.equals(""))) {
                            if (visitId != null) {
                                bill = BillDAO.getVisitCreditBill(visitId, true);
                                if (null != bill) {
                                    BillBO billbo = new BillBO();
                                    if (billbo.isMultiVisitBill(bill.getBillNo())) {
                                        bill = null;
                                    }
                                }
                                isVisitActive = VisitDetailsDAO.isVisitActive(con, visitId);

                                if (appointStatus != null && !appointStatus.equals("Booked") && !appointStatus.equals("Confirmed")
                                        && isVisitActive && bill == null && !newBillRights.equals("A")) {
                                    status = false;
                                    if (!status) break;
                                }
                            }

                            if (appointStatus != null && !appointStatus.equals("Booked") && !appointStatus.equals("Confirmed")
                                    && !orderRights.equals("A")) {
                                status = false;
                                if (!status) break;
                            }
                        }
                        resultMap = ResourceBO.checkAndOrderItems(con, mrno, visitId, category, appointmentId, UserName, DateUtil.parseDate(dateStr),
                                    autoArrival,scheduleNameForAppointmentStr,appointmentTime);
                        status = (resultMap != null && resultMap.get("result") != null) ? (Boolean)resultMap.get("result") : false;
                        if (!status) break;


                    }
                }while(false);

                schedulerBillNo = (resultMap != null && resultMap.get("billNo") != null) ? (String)resultMap.get("billNo") : null;

            }finally{
                if(status)con.commit();
                else con.rollback();
                if(con!=null)con.close();

                if (status && schedulerBillNo != null && !schedulerBillNo.equals("")) {
                    BillDAO.resetTotalsOrReProcess(schedulerBillNo);
                }
                if (status && PractoBookHelper.isPractoAdvantageEnabled()) {
                	// Add all the doctor appts to Practo
                	for (Appointments appt: scheduleAppointBeanList) {
                    	PractoBookHelper.addDoctorAppointmentsToPracto(appt.getAppointmentId(), true);
                	}
                }
            }
            String msg = null;
            if (!status) {
                if (bill != null) {
                    if (!bill.getStatus().equals("A")) {
                        msg = "Bill is not open, cannot add new items to the bill.";
                    }
                    if (bill.getPaymentStatus().equals("P")) {
                        msg = "Bill is paid, cannot add new items to the bill";
                    }
                }
                if (appointStatus != null && !appointStatus.equals("Booked") && !appointStatus.equals("Confirmed")
                        && isVisitActive && bill == null && !newBillRights.equals("A")) {
                    msg = "There is no/open Primary Credit Bill. You are not authorized to create new bill.";
                }
                if (appointStatus != null && !appointStatus.equals("Booked") && !appointStatus.equals("Confirmed")
                        && !orderRights.equals("A")) {
                    msg = "You are not authorized to order.";
                }
                if (opApplicableFor != null && visitType != null && !opApplicableFor.equals(visitType)) {
                    flash.put("error", "Appointment not saved. Operation is Not Applicable for "+(visitType.equals("o") ? "OP patient" : "IP patient"));
                }else {
                    if (msg != null) {
                        flash.put("error", "Failed to Save Appointment Details. "+msg);
                    } else {
                     flash.put("error", "Failed to Save Appointment Details.");
                    }
                }
            } else {
                //on success of saving appointment and if status is confirmed/booked send sms to patient
                if (null != appointStatus && appointStatus.equalsIgnoreCase("confirmed")
                        && MessageUtil.allowMessageNotification(request,"scheduler_message_send")) {
                  MessageManager mgr = new MessageManager();
                  Map appointmentData = new HashMap();
                  appointmentData.put("appointment_id", new Integer(appointmentId));
                  appointmentData.put("status", appointStatus);
                  mgr.processEvent("appointment_confirmed", appointmentData);
                  if(category.equals("DOC")){
                	  mgr.processEvent("doc_appt_confirmed", appointmentData);
                  }
                }
                else if (null != appointStatus && appointStatus.equalsIgnoreCase("Booked")
                        && MessageUtil.allowMessageNotification(request,"scheduler_message_send")) {
                      MessageManager mgr = new MessageManager();
                      Map appointmentData = new HashMap();
                      appointmentData.put("appointment_id", new Integer(appointmentId));
                      appointmentData.put("status", appointStatus);
                      mgr.processEvent("appointment_booked", appointmentData);
                }
      if (null != appointStatus
          && MessageUtil.allowMessageNotification(request, "scheduler_message_send")) {
        scheduleAppointmentMsg(appointmentId, appointmentTime);
      }
      //push event
      schedulePushEvent(String.valueOf(appointmentId),"APPOINTMENT_"+ appointStatus.toUpperCase());
            }


        redirect.addParameter("category", category);
         redirect.addParameter("includeResources", includeResources);
         redirect.addParameter("choosenWeekDate", request.getParameter("choosenWeekDate"));
         redirect.addParameter("date", dateStr);
         redirect.addParameter("department", dept);
         redirect.addParameter("centerId", request.getParameter("centerId"));

         redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
         redirect.setAnchor(timeStr);
        return redirect;
    }

    public ActionForward getAppointmentDetails(ActionMapping am,ActionForm af,
        HttpServletRequest request,HttpServletResponse response)throws Exception{

        String appointmentStr = request.getParameter("appointmentId");
        int appointmentId = 0;
        if(appointmentStr!=null && !appointmentStr.equals(""))
            appointmentId = Integer.parseInt(appointmentStr);
        Map schPatInfo = new HashMap();
        List<BasicDynaBean> l = new ResourceDAO(null).getAppointmentDetails(appointmentId);
        schPatInfo.put("appntDetailsList",ConversionUtils.copyListDynaBeansToMap(l));
        schPatInfo.put("slotTime", request.getParameter("slotTime"));

        BasicDynaBean schbean = l.get(0);
        String mrno = schbean.get("mr_no") != null && !(schbean.get("mr_no").equals(""))
                            ? (String)schbean.get("mr_no") : null;
        Map prevApptMap = new ResourceDAO(null).getAppointmentDetailsByMrno(mrno, appointmentId);
        schPatInfo.put("prevApptDetails", prevApptMap);

        String responseContent = new JSONSerializer().deepSerialize(schPatInfo);

        response.setContentType("application/json");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.getWriter().write(responseContent);
        response.flushBuffer();
        return null;
    }

    public ActionForward editAppointmentDetails(ActionMapping am,ActionForm af,
        HttpServletRequest request,HttpServletResponse response)throws IOException,Exception{

        String includeResources = request.getParameter("includeResources");
        String dateStr =(String)request.getParameter("date");
        String category = (String)request.getParameter("category");
        String dept = request.getParameter("department") ;
        String timeStr = null;
        if(!"".equals(request.getParameter("slotTime")))
            timeStr=(String)request.getParameter("slotTime");
        else
            timeStr=(String)request.getParameter("time");
        String appStatus = request.getParameter("status");
        boolean status = false;

        String err = appointmentEdit(request);
        Integer appointmentId = (Integer) request.getAttribute("newAppointmentId");

        if (err == null)
            status = true;

        ActionRedirect redirect =null;
        if (am.getProperty("action_id").endsWith("_week_scheduler"))
            redirect = new ActionRedirect(am.findForward("weekViewRedirect"));
        else
            redirect = new ActionRedirect(am.findForward("dayViewRedirect"));
         redirect.addParameter("category", category);

         FlashScope flash = FlashScope.getScope(request);
         redirect.addParameter("includeResources", includeResources);
         redirect.addParameter("choosenWeekDate", request.getParameter("choosenWeekDate"));
         redirect.addParameter("date", dateStr);
         redirect.addParameter("department", dept);
         redirect.addParameter("centerId", request.getParameter("centerId"));
         if(status){
             flash.put("success", "Appointment Details are Updated Successfully");
             schedulePushEvent(String.valueOf(appointmentId),"APPOINTMENT_" + appStatus.toUpperCase());

             // on success of editing the appointment details and if status is confirmed, send sms to patient
             if (null != appStatus && appStatus.equalsIgnoreCase("confirmed")
                    && MessageUtil.allowMessageNotification(request,"scheduler_message_send") && 
                    !(new ResourceDAO(null).getAppointmentSource(appointmentId)!=null && new ResourceDAO(null).getAppointmentSource(appointmentId).equalsIgnoreCase("practo"))) {
              MessageManager mgr = new MessageManager();
              Map appointmentData = new HashMap();
              appointmentData.put("appointment_id", appointmentId);
              appointmentData.put("status", appStatus);
              mgr.processEvent("appointment_confirmed", appointmentData);
              if(category.equals("DOC")){
            	  mgr.processEvent("doc_appt_confirmed", appointmentData);
              }

             }
         } else{
             flash.put("error", err);
         }
         redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
         redirect.setAnchor(timeStr);
         return redirect;
    }
    
	private String appointmentCancelAndCreate(HttpServletRequest request)
			throws SQLException, ParseException, IOException {
		Map<String, String[]> map = request.getParameterMap();
		String dateStr = request.getParameter("date");
		String category = request.getParameter("category");
		int appointmentId = 0;
		String appointmentIdStr = request.getParameter("appointmentId");
		if (!appointmentIdStr.equals(""))
			appointmentId = Integer.parseInt(appointmentIdStr);
		String mrno = request.getParameter("mrno");
        Integer contactId = (request.getParameter("contactId") != null && !request.getParameter("contactId").equals("")) ?
            Integer.parseInt(request.getParameter("contactId")) : null;
		String patientName = request.getParameter("name");
		String phoneNo = request.getParameter("contact");
		String phoneCountryCode = request.getParameter("contact_country_code");
		String complaintName = request.getParameter("complaint");
		String remarks = request.getParameter("remarks");
		String centralResourceId = request.getParameter("centralResource");
		String cancelReason = request.getParameter("cancel_reason");
		String consultationTypeStr = request.getParameter("consultationTypes");
		String schedulerVisitType = request.getParameter("scheduler_visit_type");
		String presDocId = request.getParameter("presc_doc_id");
		String condDocId = request.getParameter("cond_doc_id");
		Integer consultationTypeId = 0;
		String centerIdStr = "";
		if (null != request.getParameter("ah_center_id") && !"".equals(request.getParameter("ah_center_id"))) {
			centerIdStr = request.getParameter("ah_center_id");
		} else {
			centerIdStr = request.getParameter("center_id");
		}
		int centerId = Integer.parseInt(centerIdStr);
		if (category.equals("DOC") && consultationTypeStr != null && !consultationTypeStr.equals(""))
			consultationTypeId = Integer.parseInt(request.getParameter("consultationTypes"));
		String timeStr = null;
		if (!"".equals(request.getParameter("slotTime"))) {
			timeStr = request.getParameter("slotTime");
		} else {
			timeStr = request.getParameter("time");
		}

		String appStatus = request.getParameter("status");
		String schPriorAuthId = request.getParameter("scheduler_prior_auth_no");
		int schPriorAuthModeId = 0;
		String schPriorAuthModeIdStr = request.getParameter("scheduler_prior_auth_mode_id");
		schPriorAuthModeId = (schPriorAuthModeIdStr != null && !schPriorAuthModeIdStr.equals(""))
				? Integer.parseInt(schPriorAuthModeIdStr) : 0;

		if (appStatus == null || appStatus.equals(""))
			appStatus = request.getParameter("editStatus");

		String durationStr = request.getParameter("duration");
		String scheduleNameForAppointmentStr = request.getParameter("scheduleNameForAppointment");

		String[] resourceNames = map.get("resourcename");
		String[] resourceValues = map.get("resourcevalue");
		String[] resourceDelete = map.get("rDelete");

		Connection con = null;
		PreparedStatement ps = null;
		boolean status = false;
		Integer newAppointmentId = null;
		Timestamp appointmentTime = null;
		
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (category.equals("DOC")) {
				centralResourceId = scheduleNameForAppointmentStr;
			}

			BasicDynaBean schItems = new GenericDAO("scheduler_appointments").findByKey("appointment_id",
					appointmentId);

			if (appStatus != null && appStatus.equals("Arrived")
					&& schItems.get("appointment_status").equals("Arrived")) {
				return "patient is already arrived";
			}
			List<ResourceDTO> resourceInsertList = new ArrayList<ResourceDTO>();
			List<ResourceDTO> resourceUpdateList = new ArrayList<ResourceDTO>();
			List<ResourceDTO> resourceDeleteList = new ArrayList<ResourceDTO>();

			// create new appointment
			newAppointmentId = Integer.parseInt(new ResourceDAO(null).getNextAppointMentId());
			request.setAttribute("newAppointmentId", newAppointmentId);
			schItems.set("appointment_id", newAppointmentId);
			
			if (appStatus != null) {
				String userId = (String) request.getSession(false).getAttribute("userId");
				String timeStampStr = null;
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:mm");
				java.util.Date date = null;
				if (timeStr != null && !timeStr.equals(""))
					timeStampStr = dateStr + " " + timeStr;
				if (timeStampStr != null)
					date = dateFormat.parse(timeStampStr);
				if (date != null)
					appointmentTime = new Timestamp(date.getTime());
				schItems.set("booked_time", DateUtil.getCurrentTimestamp());
				schItems.set("booked_by", userId );
				schItems.set("res_sch_name", scheduleNameForAppointmentStr);
				schItems.set("patient_name", patientName);
				schItems.set("patient_contact", phoneNo);
				schItems.set("patient_contact_country_code", phoneCountryCode);
				schItems.set("mr_no", mrno);
				schItems.set("contact_id",contactId);
				schItems.set("complaint", complaintName);
				schItems.set("remarks", remarks);
				schItems.set("appointment_status", appStatus);
				int uniqueApptInd = (Integer) schItems.get("unique_appt_ind");
				int oldUniqueApptInd = uniqueApptInd;
				if (appStatus.equalsIgnoreCase("cancel") || appStatus.equalsIgnoreCase("noshow")) {
					if (uniqueApptInd == 0) {
						uniqueApptInd = new ResourceDAO(null).getNextUniqueAppointMentInd();
					}
				} else {
					Integer overbook = new ResourceDAO(null).isResourceOverbooked(con, centralResourceId, category);
					boolean overBookAllowed = overbook == null || overbook > 0;
					if (overBookAllowed) {
						uniqueApptInd = new ResourceDAO(null).getNextUniqueAppointMentInd();
					} else {
						uniqueApptInd = 0;
					}
				}
				if (appointmentTime != null)
					schItems.set("appointment_time", appointmentTime);
				schItems.set("duration", Integer.parseInt(durationStr));
				schItems.set("cancel_reason", cancelReason);
				schItems.set("scheduler_visit_type", schedulerVisitType);
				schItems.set("center_id", centerId);
				schItems.set("presc_doc_id", presDocId);
				schItems.set("cond_doc_id",condDocId);
				schItems.set("unique_appt_ind", uniqueApptInd);
				schItems.set("prim_res_id", centralResourceId);
				
				if (category.equals("OPE")) {
					schItems.set("scheduler_prior_auth_no", schPriorAuthId);
					schItems.set("scheduler_prior_auth_mode_id", schPriorAuthModeId);
				}

				if (category.equals("DOC")) {
					schItems.set("res_sch_name", centralResourceId);
				}

				if (category.equals("DOC") && !appStatus.equalsIgnoreCase("Channel")) {
					schItems.set("consultation_type_id", consultationTypeId);
				}

				int resources = (resourceNames == null) ? 0 : resourceNames.length;

				Timestamp modTime = DataBaseUtil.getDateandTime();

				for (int i = 0; i < resources; i++) {
					if (resourceDelete[i].equals("N")) {
						if (!resourceNames[i].equals("") && !resourceValues[i].equals("")) {
							ResourceDTO rdto = new ResourceDTO();
							rdto.setAppointmentId(newAppointmentId);
							rdto.setResourceId(resourceValues[i]);
							rdto.setResourceType(resourceNames[i]);
							rdto.setUser_name(userId);
							rdto.setMod_time(modTime);
							resourceInsertList.add(rdto);
						}
					}
				}

				if (centralResourceId != null && !centralResourceId.equals("")) {
					String resourceType = null;
					if (category.equals("DOC")) {
						resourceType = "OPDOC";
					} else if (category.equals("SNP")) {
						resourceType = "SRID";
					} else if (category.equals("DIA")) {
						resourceType = "EQID";
					} else if (category.equals("OPE")) {
						resourceType = "THID";
					}
					ResourceDTO rdto = new ResourceDTO();
					rdto.setAppointmentId(newAppointmentId);
					rdto.setResourceId(centralResourceId);
					rdto.setResourceType(resourceType);
					rdto.setMod_time(modTime);
					rdto.setUser_name(userId);
					resourceInsertList.add(rdto);
				}
				// cancel the old appointment
				Map<String, Object> fields = new HashMap<String, Object>();
				Map<String, Object> keys = new HashMap<String, Object>();
				fields.put("appointment_status", "Cancel");
				fields.put("cancel_reason", "Cancelling as primary resource rescheduled");
				fields.put("cancel_type", "Other");
				fields.put("changed_by", userId);
				fields.put("changed_time", modTime);
				if (oldUniqueApptInd == 0) {
					fields.put("unique_appt_ind", new ResourceDAO(null).getNextUniqueAppointMentInd());
				}
				keys.put("appointment_id", appointmentId);

				status = ResourceBO.updateAppointments(con, fields, keys);
				// create new appointment
				status = status && new GenericDAO("scheduler_appointments").insert(con, schItems);
				// Add new items
				status = status && new ResourceBO().updateSchedulerResourceDetails(con, resourceInsertList,
						resourceUpdateList, resourceDeleteList);
			}

			if (!status) {
				return "Updation failed...";
			}

		} finally {
			if (status) {
				con.commit();
				//delete dynamic appointment reminder job for old appointment 
				unscheduleAppointmentMsg(appointmentId);
				//creating dynamic appointment reminder job for new appointment 
				scheduleAppointmentMsg(newAppointmentId, appointmentTime);
				if(PractoBookHelper.isPractoAdvantageEnabled()) {	
					// add cancelled appt to Practo
					PractoBookHelper.addDoctorAppointmentsToPracto(appointmentId, false);
					// add new appt to Practo
					PractoBookHelper.addDoctorAppointmentsToPracto(newAppointmentId, true);
					
					schedulePushEvent(String.valueOf(appointmentId),Events.APPOINTMENT_CANCEL);
					schedulePushEvent(String.valueOf(newAppointmentId),"APPOINTMENT"+appStatus.toUpperCase());
				}
			}
			else {
				con.rollback();
			}
			DataBaseUtil.closeConnections(con, ps);
		}

		return null;

	}

    public String appointmentEdit(HttpServletRequest request) throws Exception {

        Map<String, String[]> map = request.getParameterMap();
        String dateStr = request.getParameter("date");
        String category = request.getParameter("category");
        int appointmentId = 0;
        String appointmentIdStr = request.getParameter("appointmentId");
        if (!appointmentIdStr.equals(""))
            appointmentId = Integer.parseInt(appointmentIdStr);
        

        String mrno = request.getParameter("mrno");
        Integer contactId = (request.getParameter("contactId") != null && !request.getParameter("contactId").equals("")) ?
            Integer.parseInt(request.getParameter("contactId")) : null;
        String patientName = request.getParameter("name");
        String phoneNo = request.getParameter("contact");
        String phoneCountryCode = request.getParameter("contact_country_code");
        String complaintName = request.getParameter("complaint");
        String remarks = request.getParameter("remarks");
        String centralResourceId = request.getParameter("centralResource");
        String centralResourceSchItemId = request.getParameter("centralResourceSchItemId");
        String cancelReason = request.getParameter("cancel_reason");
        String consultationTypeStr = request.getParameter("consultationTypes");
        String schedulerVisitType = request.getParameter("scheduler_visit_type");
        String presDocId = request.getParameter("presc_doc_id");
        String condDocId = request.getParameter("cond_doc_id");
        Integer consultationTypeId = null;
        String centerIdStr = "";
        Timestamp newAppointmentTime = null;
        boolean appRescheduled=false;
        String oldStatus="";
        String scheduleNameForAppointmentStr = request.getParameter("scheduleNameForAppointment");
        
        BasicDynaBean schItems = new GenericDAO("scheduler_appointments").findByKey("appointment_id", appointmentId);
		if (category.equals("DOC")) {
			centralResourceId = scheduleNameForAppointmentStr;
		}
        String appStatus = request.getParameter("status");
        if (appStatus == null || appStatus.equals(""))
            appStatus = request.getParameter("editStatus");
        boolean status = false;
        boolean isDocChanged=false;
		if (schItems.get("prim_res_id") != null && !schItems.get("prim_res_id").equals(centralResourceId)) {
			// Since the resource is changed, we cancel the existing appointment and create new one
			isDocChanged=true;
			String res = appointmentCancelAndCreate(request);
			if (res != null) {
				return res;
			}
			status = true;
			
		} else {
			request.setAttribute("newAppointmentId", appointmentId);
			if (null != request.getParameter("ah_center_id") && !"".equals(request.getParameter("ah_center_id"))) {
				centerIdStr = request.getParameter("ah_center_id");
			} else {
				centerIdStr = request.getParameter("center_id");
			}
			int centerId = Integer.parseInt(centerIdStr);
			if (category.equals("DOC") && consultationTypeStr != null && !consultationTypeStr.equals(""))
				consultationTypeId = Integer.parseInt(request.getParameter("consultationTypes"));
			String timeStr = null;
			if (!"".equals(request.getParameter("slotTime")))
				timeStr = (String) request.getParameter("slotTime");
			else
				timeStr = (String) request.getParameter("time");

			String schPriorAuthId = request.getParameter("scheduler_prior_auth_no");
			int schPriorAuthModeId = 0;
			String schPriorAuthModeIdStr = request.getParameter("scheduler_prior_auth_mode_id");
			schPriorAuthModeId = (schPriorAuthModeIdStr != null && !schPriorAuthModeIdStr.equals(""))
					? Integer.parseInt(schPriorAuthModeIdStr) : 0;

			String durationStr = request.getParameter("duration");

			String[] resourceNames = (String[]) map.get("resourcename");
			String[] resourceValues = (String[]) map.get("resourcevalue");
			String[] resourceDelete = (String[]) map.get("rDelete");
			String[] appointmentItemIds = (String[]) map.get("item_id");

			Connection con = null;
			PreparedStatement ps = null;
			List<ResourceDTO> resourceInsertList = new ArrayList<ResourceDTO>();
			List<ResourceDTO> resourceUpdateList = new ArrayList<ResourceDTO>();
			List<ResourceDTO> resourceDeleteList = new ArrayList<ResourceDTO>();

			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				Map fields = new HashMap();
				Map keys = new HashMap();

				Timestamp oldAppointmentDateTime = (Timestamp) schItems.get("appointment_time");
				oldStatus=(String) schItems.get("appointment_status");
				if (appStatus != null) {
					String timeStampStr = null;
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:mm");
					java.util.Date date = null;
					Timestamp appointmentTime = null;
					if (timeStr != null && !timeStr.equals(""))
						timeStampStr = dateStr + " " + timeStr;
					if (timeStampStr != null)
						date = (java.util.Date) dateFormat.parse(timeStampStr);
					if (date != null)
						appointmentTime = new Timestamp(date.getTime());

					newAppointmentTime = appointmentTime;
					if (appointmentTime != null && (oldAppointmentDateTime.getTime() != appointmentTime.getTime())) {
						appRescheduled = true;
					}

					String userId = (String) request.getSession(false).getAttribute("userId");
					Timestamp changedTime = DateUtil.getCurrentTimestamp();

					fields.put("res_sch_name", scheduleNameForAppointmentStr);
					fields.put("patient_name", patientName);
					fields.put("patient_contact", phoneNo);
					fields.put("patient_contact_country_code", phoneCountryCode);
					fields.put("mr_no", mrno);
					fields.put("contact_id", contactId);
					fields.put("complaint", complaintName);
					fields.put("remarks", remarks);
          String errorMsg = ValidateAppointmentStatus(appointmentId, appStatus);
          if (errorMsg != null) {
            return errorMsg;
          }
					fields.put("appointment_status", appStatus);
					int unique_appt_ind = (Integer) schItems.get("unique_appt_ind");
					if (appStatus.equalsIgnoreCase("cancel") || appStatus.equalsIgnoreCase("noshow")) {
						if (unique_appt_ind == 0) {
							unique_appt_ind = new ResourceDAO(null).getNextUniqueAppointMentInd();
						}
						if(appStatus.equalsIgnoreCase("cancel")){
							fields.put("cancel_type", "Other");
						}
					} else {
						Integer overbook = new ResourceDAO(null).isResourceOverbooked(con, centralResourceId, category);
						boolean overBookAllowed = overbook == null || overbook > 0;
						if (overBookAllowed && unique_appt_ind == 0) {
							unique_appt_ind = new ResourceDAO(null).getNextUniqueAppointMentInd();
						}
						if (!overBookAllowed) {
							unique_appt_ind = 0;
						}
					}
					if (appointmentTime != null)
						fields.put("appointment_time", appointmentTime);
					fields.put("duration", Integer.parseInt(durationStr));
					fields.put("cancel_reason", cancelReason);
					fields.put("changed_by", userId);
					fields.put("changed_time", changedTime);
					fields.put("scheduler_visit_type", schedulerVisitType);
					fields.put("center_id", centerId);
					fields.put("presc_doc_id", presDocId);
					fields.put("cond_doc_id", condDocId);
					fields.put("unique_appt_ind", unique_appt_ind);
					fields.put("prim_res_id", centralResourceId);
					if (category.equals("OPE")) {
						fields.put("scheduler_prior_auth_no", schPriorAuthId);
						fields.put("scheduler_prior_auth_mode_id", schPriorAuthModeId);
					}

					if (category.equals("DOC")) {
						fields.put("res_sch_name", centralResourceId);
					}

					if (category.equals("DOC") && !appStatus.equalsIgnoreCase("Channel")) {
						fields.put("consultation_type_id", consultationTypeId);
					}
				}
				keys.put("appointment_id", appointmentId);


				int resources = 0;
				if (resourceNames != null) {
					resources = resourceNames.length;
				}

				HttpSession ses = request.getSession(false);
				String UserName = (String) ses.getAttribute("userid");
				Timestamp modTime = DataBaseUtil.getDateandTime();

				List<BasicDynaBean> schResItems = new GenericDAO("scheduler_appointment_items")
						.findAllByKey("appointment_id", appointmentId);

				if (appStatus != null && appStatus.equals("Arrived")
						&& schItems.get("appointment_status").equals("Arrived")) {
					return "patient is already arrived";
				}

				for (int i = 0; i < resources; i++) {
					ResourceDTO rdto = new ResourceDTO();
					if (!resourceNames[i].equals("") && !resourceValues[i].equals("")) {
						rdto.setAppointmentId(appointmentId);
						rdto.setResourceId(resourceValues[i]);
						rdto.setResourceType(resourceNames[i]);
						rdto.setUser_name(UserName);
						rdto.setMod_time(modTime);

						if (appointmentItemIds[i].equals("") || appointmentItemIds[i].equals("0")) {
							if (resourceDelete[i].equals("N")) {
								resourceInsertList.add(rdto);
							} else {
								resourceDeleteList.add(rdto);
							}
						} else {
							rdto.setAppointment_item_id(new Integer(appointmentItemIds[i]).intValue());
							if (resourceDelete[i].equals("N")) {

								resourceUpdateList.add(rdto);
							} else {
								resourceDeleteList.add(rdto);
							}
						}
					}
				}

				if (schResItems != null && schResItems.size() > 0) {
					String resourceType = null;
					if (category.equals("DOC")) {
						resourceType = "OPDOC";
					} else if (category.equals("SNP")) {
						resourceType = "SRID";
					} else if (category.equals("DIA")) {
						resourceType = "EQID";
					} else if (category.equals("OPE")) {
						resourceType = "THID";
					}
					for (BasicDynaBean itembean : schResItems) {
						ResourceDTO rdto = new ResourceDTO();
						rdto.setAppointmentId(appointmentId);
						rdto.setResourceId((String) itembean.get("resource_id"));
						rdto.setResourceType((String) itembean.get("resource_type"));

						boolean exists = false;
						int appItemId = (Integer) itembean.get("appointment_item_id");
						if (appointmentItemIds != null && appointmentItemIds.length > 0) {
							for (int j = 0; j < appointmentItemIds.length; j++) {
								if (!appointmentItemIds[j].equals("") && !appointmentItemIds[j].equals("0")) {
									if (appItemId == new Integer(appointmentItemIds[j])
											&& !resourceValues[j].equals("")) {
										exists = true;
										break;
									}
								}
							}
						}
						if (!exists && !rdto.getResourceType().equals(resourceType)) {
							resourceDeleteList.add(rdto);
						}
					}
				}

				if (centralResourceId != null && !centralResourceId.equals("")) {
					String resourceType = null;
					if (category.equals("DOC")) {
						resourceType = "OPDOC";
					} else if (category.equals("SNP")) {
						resourceType = "SRID";
					} else if (category.equals("DIA")) {
						resourceType = "EQID";
					} else if (category.equals("OPE")) {
						resourceType = "THID";
					}
					ResourceDTO rdto = new ResourceDTO();
					rdto.setAppointmentId(appointmentId);
					rdto.setResourceId(centralResourceId);
					rdto.setResourceType(resourceType);
					rdto.setAppointment_item_id(new Integer(centralResourceSchItemId).intValue());
					rdto.setUser_name(UserName);
					rdto.setMod_time(modTime);
					resourceUpdateList.add(rdto);
				}

				do {

					if (appStatus != null) {
						status = ResourceBO.updateAppointments(con, fields, keys);
						if (!status)
							break;

						status = new ResourceBO().updateSchedulerResourceDetails(con, resourceInsertList,
								resourceUpdateList, resourceDeleteList);

						if (!status)
							break;

					}

				} while (false);

				if (!status) {
					return "Updation failed...";
				}
				
				
			} finally {
				if (status) {
					con.commit();
					if( PractoBookHelper.isPractoAdvantageEnabled()) {
						// Update appointment to Practo
						PractoBookHelper.addDoctorAppointmentsToPracto(appointmentId, false, resourceDeleteList);
					}
				}
				else {
					con.rollback();
				}
				DataBaseUtil.closeConnections(con, ps);
			}
		}
        if(!(new ResourceDAO(null).getAppointmentSource(appointmentId)!=null && new ResourceDAO(null).getAppointmentSource(appointmentId).equalsIgnoreCase("practo"))){
	        if (status && null != appStatus && appStatus.equalsIgnoreCase("Cancel")
	                && MessageUtil.allowMessageNotification(request,"scheduler_message_send")) {
	          MessageManager mgr = new MessageManager();
	          Map appointmentData = new HashMap();
	          appointmentData.put("appointment_id", appointmentIdStr);
	          appointmentData.put("status", appStatus);
	          mgr.processEvent("appointment_cancelled", appointmentData);
	        }
	        if (status && null != appStatus && appStatus.equalsIgnoreCase("Cancel")){
	          unscheduleAppointmentMsg(appointmentId);
	          }
      if (status && null != appStatus
          && (appStatus.equalsIgnoreCase("confirmed") || appStatus.equalsIgnoreCase("booked"))
          && appRescheduled && !isDocChanged) {
        rescheduleAppointmentMsg(appointmentId, newAppointmentTime);
      }
        }
      //push event
        if (appRescheduled) 
          schedulePushEvent(String.valueOf(appointmentId),"APPOINTMENT_RESCHEDULED");
        return null;
    }

    public String appointmentEditFromTodaysScreen(HttpServletRequest request) throws Exception {
        String category = (String)request.getParameter("_category");
        int appointmentId = 0;
        String appointmentIdStr = request.getParameter("appointmentId");
        if (!appointmentIdStr.equals(""))
            appointmentId = Integer.parseInt(appointmentIdStr);
        String consultationTypeStr = request.getParameter("consultationTypes");
        int consultationTypeId = 0;
        if (category.equals("DOC") && consultationTypeStr != null && !consultationTypeStr.equals(""))
            consultationTypeId =Integer.parseInt(request.getParameter("consultationTypes"));

        BasicDynaBean apptBean = new GenericDAO("scheduler_appointments").findByKey("appointment_id",appointmentId);

        String appStatus = (apptBean != null) ? (String)apptBean.get("appointment_status") : null;
        String schPriorAuthId = request.getParameter("scheduler_prior_auth_no");
        int schPriorAuthModeId = 0;
        String schPriorAuthModeIdStr = request.getParameter("scheduler_prior_auth_mode_id");
        schPriorAuthModeId = (schPriorAuthModeIdStr != null && !schPriorAuthModeIdStr.equals("")) ? Integer.parseInt(schPriorAuthModeIdStr) : 0;

        Connection con = null;
        PreparedStatement ps = null;
        boolean status = false;

        try{
            con = DataBaseUtil.getConnection();
            con.setAutoCommit(false);

            Map fields = new HashMap();
            Map keys = new HashMap();

            if (appStatus != null) {
                String userId = (String)request.getSession(false).getAttribute("userId");
                java.util.Date currentDate = new java.util.Date();
                Timestamp changedTime = new java.sql.Timestamp(currentDate.getTime());

                fields.put("appointment_status", appStatus);
                fields.put("changed_by", userId);
                fields.put("changed_time", changedTime);
                if(category.equals("OPE")){
                    fields.put("scheduler_prior_auth_no", schPriorAuthId);
                    fields.put("scheduler_prior_auth_mode_id", schPriorAuthModeId);
                }

                if (category.equals("DOC") && !appStatus.equalsIgnoreCase("Channel")){
                    fields.put("consultation_type_id", consultationTypeId);
                }
            }
            keys.put("appointment_id", appointmentId);

            if (appStatus != null && appStatus.equals("Arrived")) {
                return "patient is already arrived";
            }

            do{
                if (appStatus != null) {
                    int updateCount =  new GenericDAO("scheduler_appointments").update(con, fields, keys);
                    if(updateCount >=0 ){
                        status = true;
                    }

                    if(!status)break;
                }

            }while(false);

            if (!status) {
                return "Updation failed...";
            }

        }finally{
            if(status) {
            	con.commit();
            	if(PractoBookHelper.isPractoAdvantageEnabled()) {
            	    PractoBookHelper.addDoctorAppointmentsToPracto(appointmentId, false);
            	}
            }
            else con.rollback();
            DataBaseUtil.closeConnections(con, ps);
        }
        return null;
    }
    
	public boolean rescheduleAppointmentCancelAndCreate(HttpServletRequest req)
			throws SQLException, ParseException, IOException {

		String category = req.getParameter("category");
		String dateStr = req.getParameter("date");
		String timeStr = "00:00";
		if (req.getParameter("slotTime") != null && !req.getParameter("slotTime").equals(""))
			timeStr = req.getParameter("slotTime");
		String centerIdStr = req.getParameter("appointment_center");
		int centerId = Integer.parseInt(centerIdStr);

		String rescheduleResourceId = req.getParameter("rescheduleResourceId");
		String timeStampStr = dateStr + " " + timeStr;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:mm");
		java.util.Date date = dateFormat.parse(timeStampStr);
		Timestamp appointmentTime = new Timestamp(date.getTime());
		String appointmentId = req.getParameter("appointmentId");
		int appId = Integer.parseInt(appointmentId);
		String userName = (String) req.getSession(false).getAttribute("userid");

		Connection con = DataBaseUtil.getConnection();

		BasicDynaBean appbean = new GenericDAO("scheduler_appointments").findByKey("appointment_id", appId);
		Map<String, Object> fields = new HashMap<String, Object>();
		Map<String, Object> keys = new HashMap<String, Object>();
		// create new appointment
		int newAppointmentId = Integer.parseInt(new ResourceDAO(null).getNextAppointMentId());
		appbean.set("appointment_id", newAppointmentId);
		Timestamp oldAppointmentTime = (Timestamp) appbean.get("appointment_time");
		Integer oldUniqueApptId = (Integer) appbean.get("unique_appt_ind");
		String presDocId = (String) appbean.get("presc_doc_id");
		appbean.set("orig_appt_time", oldAppointmentTime);
		appbean.set("rescheduled", "Y");
		appbean.set("appointment_time", appointmentTime);
		appbean.set("changed_by", userName);
		appbean.set("changed_time", DateUtil.getCurrentTimestamp());
		appbean.set("center_id", centerId);
		appbean.set("presc_doc_id", presDocId);
		if (category.equals("DOC")) {
			appbean.set("res_sch_name", rescheduleResourceId);
		}
		appbean.set("prim_res_id", rescheduleResourceId);

		Integer overbookLimit = new ResourceDAO(null).isResourceOverbooked(con, rescheduleResourceId, category);

		boolean overBookAllowed = overbookLimit == null || overbookLimit > 0;
		if (overBookAllowed) {
			appbean.set("unique_appt_ind", new ResourceDAO(null).getNextUniqueAppointMentInd());
		} else {
			appbean.set("unique_appt_ind", 0);
		}

		String resourceType = null;
		if (rescheduleResourceId != null && !rescheduleResourceId.equals("")) {
			if (category.equals("DOC")) {
				resourceType = "OPDOC";
			} else if (category.equals("SNP")) {
				resourceType = "SRID";
			} else if (category.equals("DIA")) {
				resourceType = "EQID";
			} else if (category.equals("OPE")) {
				resourceType = "THID";
			}
		}
		List<BasicDynaBean> itemBeans = new GenericDAO("scheduler_appointment_items").findAllByKey("appointment_id",
				appId);
		

		List<ResourceDTO> resourceInsertList = new ArrayList<ResourceDTO>();
		List<ResourceDTO> resourceUpdateList = new ArrayList<ResourceDTO>();
		List<ResourceDTO> resourceDeleteList = new ArrayList<ResourceDTO>();
		

		Timestamp modTime = DataBaseUtil.getDateandTime();

		for (BasicDynaBean itemBean : itemBeans) {
			ResourceDTO rdto = new ResourceDTO();
			rdto.setAppointmentId(newAppointmentId);
			rdto.setResourceType((String) itemBean.get("resource_type"));
			if (resourceType.equals(rdto.getResourceType())) {
				rdto.setResourceId(rescheduleResourceId);
			} else {
				rdto.setResourceId((String) itemBean.get("resource_id"));
			}
			
			rdto.setUser_name(userName);
			rdto.setMod_time(modTime);
			resourceInsertList.add(rdto);
		}
		boolean success = false;
		try {
			con.setAutoCommit(false);
			// cancel the exiting appointment
			fields.put("appointment_status", "Cancel");
			fields.put("cancel_reason", "Cancelling as primary resource rescheduled");
			fields.put("changed_by", userName);
			fields.put("changed_time", modTime);
			if (oldUniqueApptId == 0) {
				fields.put("unique_appt_ind", new ResourceDAO(null).getNextUniqueAppointMentInd());
			}
			keys.put("appointment_id", appId);
			success = ResourceBO.updateAppointments(con, fields, keys);
			// create new appointment
			success = success && new GenericDAO("scheduler_appointments").insert(con, appbean);
			// new appointment items
			success = success && new ResourceBO().updateSchedulerResourceDetails(con, resourceInsertList,
					resourceUpdateList, resourceDeleteList);

		} finally {
			DataBaseUtil.commitClose(con, success);
			if (success){
				//delete dynamic appointment reminder job for old appointment 
				unscheduleAppointmentMsg(appId);
				//creating dynamic appointment reminder job for new appointment 
				scheduleAppointmentMsg(newAppointmentId, appointmentTime);
				//push event
				schedulePushEvent(String.valueOf(appId),"APPOINTMENT_CANCEL");
				schedulePushEvent(String.valueOf(newAppointmentId),"APPOINTMENT_" + String.valueOf(appbean.get("appointment_status")).toUpperCase());
			}
			if (success && PractoBookHelper.isPractoAdvantageEnabled()) {
				// Push cancelled appt to Practo
				PractoBookHelper.addDoctorAppointmentsToPracto(appId, false);
	        	// Push new appt to Practo
				PractoBookHelper.addDoctorAppointmentsToPracto(newAppointmentId, true);
			}
		}
		return success;
	}

    public ActionForward rescheduleAppointment(ActionMapping am,
            ActionForm af, HttpServletRequest req, HttpServletResponse res)
    throws Exception {

        String includeResources = req.getParameter("includeResources");
        String category = (String)req.getParameter("category");
        String dept = req.getParameter("department");
        String dateStr = (String)req.getParameter("date");
        String timeStr = "00:00";
        if(req.getParameter("slotTime") != null && !req.getParameter("slotTime").equals(""))
            timeStr = (String)req.getParameter("slotTime");
        String centerIdStr = req.getParameter("appointment_center");
        int centerId = Integer.parseInt(centerIdStr);
        ActionRedirect redirect = null;
        if (am.getProperty("action_id").endsWith("_week_scheduler"))
            redirect = new ActionRedirect(am.findForward("weekViewRedirect"));
        else
            redirect = new ActionRedirect(am.findForward("dayViewRedirect"));

        String rescheduleResourceId = req.getParameter("rescheduleResourceId");
        String timeStampStr = dateStr + " " + timeStr;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:mm");
        java.util.Date date = (java.util.Date)dateFormat.parse(timeStampStr);
        Timestamp appointmentTime = new java.sql.Timestamp(date.getTime());
        String appointmentId = req.getParameter("appointmentId");
        int appId = Integer.parseInt(appointmentId);
        String userName = (String)req.getSession(false).getAttribute("userid");

        Connection con = DataBaseUtil.getConnection();

        BasicDynaBean appbean = new GenericDAO("scheduler_appointments").findByKey("appointment_id", appId);
        Timestamp oldAppointmentTime = (Timestamp)appbean.get("appointment_time");
        Integer oldUniqueApptId = (Integer) appbean.get("unique_appt_ind");
        String presDocId = (String)appbean.get("presc_doc_id");
        String primResId = (String)appbean.get("prim_res_id"); 
        appbean.set("orig_appt_time", oldAppointmentTime);
        appbean.set("rescheduled", "Y");
        appbean.set("appointment_time", appointmentTime);
        appbean.set("changed_by", userName);
        appbean.set("changed_time", DateUtil.getCurrentTimestamp());
        appbean.set("center_id", centerId);
        appbean.set("presc_doc_id", presDocId);
        if(category.equals("DOC")) {
            appbean.set("res_sch_name", rescheduleResourceId);
        }
        appbean.set("prim_res_id", rescheduleResourceId);
        
        Integer overbookLimit = new ResourceDAO(null).isResourceOverbooked(con, rescheduleResourceId, category);
        int overBookCount = new ResourceDAO(null).getOverbookCount(primResId, appointmentTime);
        int overbookCount= overBookCount == 1 ? 0 : overBookCount;
        
        
        if (overbookLimit != null && overbookLimit != 0 &&  overbookCount > overbookLimit){
        	
        	FlashScope flash = FlashScope.getScope(req);
        	flash.error("The number of appointments booked for this slot has hit the overbook Limit.");
        	redirect.addParameter("category", category);
      	    redirect.addParameter("includeResources", includeResources);
          	redirect.addParameter("department", dept);
            redirect.addParameter("centerId", req.getParameter("centerId"));
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
        }
        boolean overBookAllowed = overbookLimit == null || overbookLimit > 0;
        if (overBookAllowed) {
            appbean.set("unique_appt_ind", new ResourceDAO(null).getNextUniqueAppointMentInd());
        } else {
        	if(oldUniqueApptId != null && oldUniqueApptId.intValue() == 0)
        		appbean.set("unique_appt_ind", 0);
        }
        //channel appointment to be rescheduled
        if(appbean.get("pat_package_id") != null) {
        	Map<String, Object>  whereFilter = new HashMap<String, Object>();
            whereFilter.put("res_sch_name", rescheduleResourceId);
            whereFilter.put("appointment_time", DateUtil.getDatePart(appointmentTime));
            appbean.set("appt_token", (new ResourceDAO(null).getToken(con, "appt_token", whereFilter, 1, true)));
        }
        String appStatus=(String)appbean.get("appointment_status");
        boolean success = false;
        boolean isDocChanged=false;
        // Ignore cancel & create for chanelling appt
        if (appbean.get("pat_package_id") == null  &&
        		primResId != null && !primResId.equals(rescheduleResourceId)) {
        	// cancel the existing appt and create new one
        	isDocChanged=true;
        	success = rescheduleAppointmentCancelAndCreate(req);
        	
        } else {
	        Map<String, Integer> key = new HashMap<String, Integer>();
	        key.put("appointment_id", appId);
	
	        String resourceType = null;
	        if(rescheduleResourceId != null && !rescheduleResourceId.equals("")){
	            if(category.equals("DOC")){
	                resourceType = "OPDOC";
	            }else if(category.equals("SNP")){
	                resourceType = "SRID";
	            }else if(category.equals("DIA")){
	                resourceType = "EQID";
	            }else if(category.equals("OPE")){
	                resourceType = "THID";
	            }
	        }
	        BasicDynaBean appItembean = new ResourceDAO(null).findPrimaryResource(appId, resourceType);
	        String oldRescheduleResourceId = (String)appItembean.get("resource_id");
	        appItembean.set("resource_id", rescheduleResourceId);
	        Map keys = new HashMap();
	        keys.put("appointment_id", appId);
	        keys.put("resource_type", resourceType);
	        keys.put("resource_id", oldRescheduleResourceId);
	        try {
	            con.setAutoCommit(false);
	            success = (new GenericDAO("scheduler_appointments").update(con, appbean.getMap(), key)) > 0;
	            if (success) {
	                success = (new GenericDAO("scheduler_appointment_items").update(con, appItembean.getMap(), keys)) > 0;
	            }
	
	        } finally {
	            DataBaseUtil.commitClose(con, success);
	        }
	        if (success && PractoBookHelper.isPractoAdvantageEnabled()) {
	            PractoBookHelper.addDoctorAppointmentsToPracto(appId, false);
	        }
	        //push event
	        if (success) {
	          schedulePushEvent(appointmentId, "APPOINTMENT_RESCHEDULED");
	        }
        }
        
        boolean isPractoAppointment=false;
        
        if(new ResourceDAO(null).getAppointmentSource(appId)!=null && new ResourceDAO(null).getAppointmentSource(appId).equalsIgnoreCase("practo")){
        	isPractoAppointment=true;
        }

        if (success && MessageUtil.allowMessageNotification(req,"scheduler_message_send") && !isPractoAppointment) {
            MessageManager mgr = new MessageManager();
            Map appointmentData = new HashMap();
            appointmentData.put("appointment_id", appId);
            mgr.processEvent("appointment_details_changed", appointmentData);
        }
        if (success && null != appStatus && (appStatus.equalsIgnoreCase("confirmed") || appStatus.equalsIgnoreCase("booked")) && !isPractoAppointment && !isDocChanged){
        	rescheduleAppointmentMsg(Integer.parseInt(appointmentId), appointmentTime);
        }
              
         redirect.addParameter("category", category);
         FlashScope flash = FlashScope.getScope(req);
         redirect.addParameter("includeResources", includeResources);
         redirect.addParameter("choosenWeekDate", req.getParameter("choosenWeekDate"));
         redirect.addParameter("date", dateStr);
         redirect.addParameter("department", dept);
         redirect.addParameter("centerId", req.getParameter("centerId"));
         if(success){
             flash.put("success", "Appointment Rescheduled Successfully...");
         }else{
             flash.put("error", "Failed to Rescheduled Appointment");
         }
         redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
         redirect.setAnchor(timeStr);
         return redirect;
    }

    @IgnoreConfidentialFilters
    public ActionForward getTodaysPatientAppointments(ActionMapping mapping,
            ActionForm form, HttpServletRequest req, HttpServletResponse res)
            throws Exception {

        String message = null;
        String actionId = mapping.getProperty("action_id");
        int userCenterId = (Integer)req.getSession(false).getAttribute("centerId");
        req.setAttribute("actionId", actionId);
        JSONSerializer js = new JSONSerializer().exclude("class");
        Map map = getParameterMap(req);
        DateUtil dateUtil = new DateUtil();
        String fromDate = dateUtil.getDateFormatter().format(new java.util.Date());
        String toDate = dateUtil.getDateFormatter().format(new java.util.Date());
        String startTime = req.getParameter("startTime");
        startTime = startTime == null ? "" : startTime;
        HttpSession ses = req.getSession(false);
        int centerId = (Integer)ses.getAttribute("centerId");
        String paidAtSource = null;
        String userName = (String)ses.getAttribute("userid");
        int roleId = (Integer)req.getSession().getAttribute("roleId");
     	List apptSourcesList = AppointmentSourceDAO.getActiveAppointmentSourceDetails();
    	req.setAttribute("apptSources", apptSourcesList);
    	String app_source_id[] = req.getParameterValues("appt_source");
    	req.setAttribute("appt_source", app_source_id);
    	String appointStatus[] = req.getParameterValues("appoint_status");
        map.remove("startTime");
        if (startTime != null && startTime.equals("today")) {
            map.put("appoint_date", new String[] {fromDate, toDate});
            map.put("appoint_date@op", new String[] {"ge", "le"});
            req.setAttribute("fromdate", fromDate);
            req.setAttribute("todate", toDate);
        }
        if (startTime.equals("") || !startTime.equals("today")) {
            String[] appointDate = req.getParameterValues("appoint_date");
            map.put("appoint_date@op", new String[] {"ge", "le"});
            if (appointDate != null) {
                req.setAttribute("fromdate", appointDate[0]);
                req.setAttribute("todate", appointDate[1]);
            }
        }

        if(req.getParameter("resFilter") !=null && !req.getParameter("resFilter").equals("")) {
            if (req.getParameter("resFilter").equals("DOC")) {

                if (req.getParameter("doctor") != null && !req.getParameter("doctor").equals("")) {
                    map.put("resource", new String[] {req.getParameter("doctor")});
                    map.put("resource@op", new String[] {"ilike"});
                }
                map.remove("doctor_name");
                map.remove("doctor");
            } else if (req.getParameter("resFilter").equals("SNP")) {
                if (req.getParameter("service")!= null && !req.getParameter("service").equals("")) {
                    map.put("resource", new String[] {req.getParameter("service")});
                    map.put("resource@op", new String[] {"ilike"});
                }
                map.remove("service_name");
                map.remove("service");
            } else if (req.getParameter("resFilter").equals("OPE")) {
                if (req.getParameter("surgery") != null && !req.getParameter("surgery").equals("")) {
                    map.put("resource", new String[] {req.getParameter("surgery")});
                    map.put("resource@op", new String[] {"ilike"});
                }
                map.remove("surgery_name");
                map.remove("surgery");
            } else if (req.getParameter("resFilter").equals("DIA")) {
                if (req.getParameter("test") != null && !req.getParameter("test").equals("")) {
                    map.put("resource", new String[] {req.getParameter("test")});
                    map.put("resource@op", new String[] {"ilike"});
                }
                map.remove("test_name");
                map.remove("test");
            } else {
                map.remove("resFilter");
            }
        }
        if(app_source_id != null && app_source_id.length > 0 && !app_source_id[0].equals("")) {
            Integer[] app_source_idInt = new Integer[app_source_id.length];
        	for(int x = 0; x<app_source_id.length; x++) {
        		app_source_idInt[x] = Integer.parseInt(app_source_id[x]);
        	}
        	map.remove("appt_source");
        	map.put("app_source_id", app_source_id);
        	map.put("app_source_id@type", new String[] {"integer"});
        	map.put("app_source_id@op", new String[] {"in"});
        }

        boolean paid = false;
        boolean unpaid = false;

        if(req.getParameter("visit_mode") != null && !req.getParameter("visit_mode").isEmpty()) {
            if (!req.getParameter("resFilter").equals("DOC")) {
            	map.remove("visit_mode");
            }
        }
        if(req.getAttribute("info") != null && !req.getAttribute("info").equals("")) {
            message = (message == null) ? req.getAttribute("info").toString() : message + req.getAttribute("info");
            req.setAttribute("info", message);
        }

        if(req.getAttribute("error") != null && !req.getAttribute("error").equals("")) {
            message = (message == null) ? req.getAttribute("error").toString() : message + req.getAttribute("error");
            req.setAttribute("error", message);
        }

        PagedList pagedList = new ResourceDAO(null).getTodayAppointments(map, ConversionUtils.getListingParameter(req.getParameterMap()), userCenterId, appointStatus, paid, unpaid, userName, roleId);
        if(pagedList != null && pagedList.getDtoList().size() > 0) {
            req.setAttribute("pagedList", pagedList);
        }

        RegistrationPreferencesDTO regPrefs = RegistrationPreferencesDAO.getRegistrationPreferences();
        String allowMultipleActiveVisits = (regPrefs.getAllow_multiple_active_visits() != null
                                                && !regPrefs.getAllow_multiple_active_visits().equals(""))
                                            ? regPrefs.getAllow_multiple_active_visits() : "N";
        req.setAttribute("allowMultipleActiveVisits", allowMultipleActiveVisits);

        req.setAttribute("doctorsJson",js.serialize(new ResourceDAO(null).getDoctorJsonList("DOC")));
        req.setAttribute("testsJson",js.serialize(new ResourceDAO(null).getJsonList("TES")));
        req.setAttribute("servicesJson",js.serialize(new ResourceDAO(null).getJsonList("SER")));
        req.setAttribute("surgeriesJson",js.serialize(new ResourceDAO(null).getJsonList("SUR")));

        req.setAttribute("DoctorsJSON", js.serialize( new ResourceDAO(null).getResourceMasterList("DOC")));

        req.setAttribute("LabTechniciansJSON", js.serialize (new ResourceDAO(null).getResourceMasterList("LABTECH")));

        List theatres = new ResourceDAO(null).getCenterResourceMasterList("THID",centerId,userName,roleId);
        req.setAttribute("TheatresJSON", js.serialize(theatres));

        List serviceResources = new ResourceDAO(null).getServResourceSchedules("SNP",centerId);
        req.setAttribute("serviceResourcesListJson", js.serialize(ConversionUtils.listBeanToListMap(serviceResources)));
        
        // get all mapped service resources for that center
        List mappedServiceResources = new ResourceDAO(null).getAllMappedResources("SNP",centerId,userName,roleId);
        req.setAttribute("mappedServiceResourcesJson", js.serialize(ConversionUtils.listBeanToListMap(mappedServiceResources)));

        List mappedTheatresJson = new ResourceDAO(null).getAllMappedResources("OPE",centerId,userName,roleId);
        req.setAttribute("mappedTheatresJson", js.serialize(ConversionUtils.listBeanToListMap(mappedTheatresJson)));

        List genericResourceListJson = new ResourceDAO(null).getCenterResourceMasterList("GEN",centerId,userName,roleId);
        req.setAttribute("genericResourceListJson", js.serialize(genericResourceListJson));

        List equipments = new ResourceDAO(null).getCenterResourceMasterList("EQID",centerId,userName,roleId);
        req.setAttribute("EquipmentsJSON", js.serialize(equipments));

        // get all mapped equipment resources for that center
        List mappedEquipmentResources = new ResourceDAO(null).getAllMappedResources("EQID",centerId,userName,roleId);
        req.setAttribute("mappedEquipmentResourcesJson", js.serialize(ConversionUtils.listBeanToListMap(mappedEquipmentResources)));

        List beds = new ResourceDAO(null).getResourceMasterList("BED");
        req.setAttribute("BedsJSON", js.serialize(beds));

        List statusList = new ResourceDAO(null).getStatusList();
        req.setAttribute("statusListJSON", js.serialize(statusList));

        setSearchAppointmentsAttributes(req,res);

        //req.setAttribute("complaints", js.serialize(ComplaintMasterDAO.getAllComplaints()));
        return mapping.findForward("todaysappointments");
    }

    public void setSearchAppointmentsAttributes(HttpServletRequest req, HttpServletResponse res) throws Exception {
        Integer centerId = RequestContext.getCenterId();
        boolean needPrimaryResource = true;
        List resourceTypeList = new ResourceDAO(null).getResourceTypes(needPrimaryResource);
        req.setAttribute("resourceTypeListJSON", new JSONSerializer().exclude("class").serialize(resourceTypeList));

        req.setAttribute("consultationTypeForIp", ConversionUtils.copyListDynaBeansToMap(
                com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO.getConsultationTypes("i")));
        req.setAttribute("consultationTypeForOp", ConversionUtils.copyListDynaBeansToMap(
                com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO.getConsultationTypes("o")));

        List scheduleResourceList = new ResourceDAO(null).getScheduledDoctorsList();
        req.setAttribute("scheduleResourceListJSON", new JSONSerializer().exclude("class").serialize(ConversionUtils.listBeanToListMap(scheduleResourceList)));

        req.setAttribute("regPrefJSON", new JSONSerializer().exclude("class").serialize(RegistrationPreferencesDAO.getRegistrationPreferences()));
        req.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
        req.setAttribute("healthAuthoPrefJSON", new JSONSerializer().exclude("class").serialize
                (HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(CenterMasterDAO.getHealthAuthorityForCenter(centerId))));
        req.setAttribute("loggedInCenter", new JSONSerializer().exclude("class").serialize(RequestContext.getCenterId()));
    }

    @IgnoreConfidentialFilters
    public ActionForward getSearchScreen(ActionMapping mapping,
            ActionForm form, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        JSONSerializer js = new JSONSerializer().exclude("class");
        HttpSession ses = req.getSession(false);
        int centerId = (Integer)ses.getAttribute("centerId");

        req.setAttribute("doctorsJson",js.serialize(new ResourceDAO(null).getDoctorJsonList("DOC")));
        req.setAttribute("testsJson",js.serialize(new ResourceDAO(null).getJsonList("TES")));
        req.setAttribute("servicesJson",js.serialize(new ResourceDAO(null).getJsonList("SER")));
        req.setAttribute("surgeriesJson",js.serialize(new ResourceDAO(null).getJsonList("SUR")));
        req.setAttribute("filterValue", null);
        req.setAttribute("TheatresJSON", js.serialize(null));
        req.setAttribute("EquipmentsJSON", js.serialize(null));
        req.setAttribute("LabTechniciansJSON", js.serialize(null));
        req.setAttribute("BedsJSON", js.serialize(null));
        req.setAttribute("statusListJSON", js.serialize(null));
        req.setAttribute("resourceTypeListJSON", js.serialize(null));
        req.setAttribute("mappedServiceResourcesJson", js.serialize(null));
        req.setAttribute("mappedTheatresJson", js.serialize(null));
        req.setAttribute("mappedEquipmentResourcesJson", js.serialize(null));        
        //req.setAttribute("complaints", js.serialize(null));
        req.setAttribute("DoctorsJSON", js.serialize(null));
        List serviceResources = new ResourceDAO(null).getServResourceSchedules("SNP",centerId);
        req.setAttribute("serviceResourcesListJson", js.serialize(ConversionUtils.listBeanToListMap(serviceResources)));
    	List apptSourcesList = AppointmentSourceDAO.getActiveAppointmentSourceDetails();
    	req.setAttribute("apptSources", apptSourcesList);
    	String app_source_id[] = req.getParameterValues("appt_source");
    	req.setAttribute("appt_source", app_source_id);
        setSearchAppointmentsAttributes(req,res);

        return mapping.findForward("todaysappointments");
    }

    @IgnoreConfidentialFilters
    public ActionForward saveAppointmentDetails(ActionMapping mapping,
            ActionForm form, HttpServletRequest req, HttpServletResponse res)
            throws Exception {

        FlashScope flash = FlashScope.getScope(req);
        String message = null;
        boolean result = saveStatusAndResources(req);
        if(result) {
            message = "Appointment details saved...";
            flash.put("success", message);
        }else {
            message = "Failed to save details";
            flash.put("error", message);
        }
        ActionRedirect redirect = new ActionRedirect(req.getHeader("Referer").
                replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
    }

    public ActionForward printAppointments(ActionMapping mapping,
            ActionForm form, HttpServletRequest req, HttpServletResponse res)
            throws Exception {

        String appointmentId = req.getParameter("appointment_id");
        String category = req.getParameter("category");
        List<BasicDynaBean> appList = new ArrayList<BasicDynaBean>();
        List<BasicDynaBean> resourceList = new ArrayList<BasicDynaBean>();

        appList = new ResourceDAO(null).getAppointmentDetailsForPrint(Integer.parseInt(appointmentId),category);
        resourceList = new ResourceDAO(null).getResourceDetailsForPrint(Integer.parseInt(appointmentId));

        Map<String, String> docMap = new HashMap<String, String>();
        Map<String, String> equipmentMapForTest = new HashMap<String, String>();
        Map<String, String> radiologistMap = new HashMap<String, String>();
        Map<String, String> equipmentMapForService = new HashMap<String, String>();
        Map<String, String> docMapForService = new HashMap<String, String>();
        Map<String, String> anesMapForSurgery = new HashMap<String, String>();
        Map<String, String> surgeonMapForSurgery = new HashMap<String, String>();
        Map<String, String> equipmentMapForSurgery = new HashMap<String, String>();


        if (category.equals("DOC")) {
            for(int i= 0;i<resourceList.size();i++) {
                if (resourceList.get(i).get("resource_type").equals("Equipment")) {
                    docMap.put("booked_resource", (String)resourceList.get(i).get("booked_resource"));
                } else if(resourceList.get(i).get("resource_type").equals("Generic Resource")) {

                }
            }
        } else if (category.equals("DIA")) {
            for(int i= 0;i<resourceList.size();i++) {
                if (resourceList.get(i).get("resource_type").equals("Equipment")) {
                    equipmentMapForTest.put("booked_resource", (String)resourceList.get(i).get("booked_resource"));
                } else if (resourceList.get(i).get("resource_type").equals("Technician/Radiologist")) {
                    radiologistMap.put("booked_resource", (String)resourceList.get(i).get("booked_resource"));
                } else if(resourceList.get(i).get("resource_type").equals("Generic Resource")) {

                }
            }
        } else if (category.equals("SNP")) {
            for(int i= 0;i<resourceList.size();i++) {
                if (resourceList.get(i).get("resource_type").equals("Service Resource")) {
                    equipmentMapForService.put("booked_resource", (String)resourceList.get(i).get("booked_resource"));
                } else if (resourceList.get(i).get("resource_type").equals("Doctor")) {
                    docMapForService.put("booked_resource", (String)resourceList.get(i).get("booked_resource"));
                } else if(resourceList.get(i).get("resource_type").equals("Generic Resource")) {

                }
            }
        } else {
            for(int i= 0;i<resourceList.size();i++) {
                if (resourceList.get(i).get("resource_type").equals("Equipment")) {
                    equipmentMapForSurgery.put("booked_resource", (String)resourceList.get(i).get("booked_resource"));
                } else if (resourceList.get(i).get("resource_type").equals("Surgeon")) {
                    surgeonMapForSurgery.put("booked_resource", (String)resourceList.get(i).get("booked_resource"));
                } else if (resourceList.get(i).get("resource_type").equals("Anesthetist")) {
                    anesMapForSurgery.put("booked_resource", (String)resourceList.get(i).get("booked_resource"));
                } else if(resourceList.get(i).get("resource_type").equals("Generic Resource")) {

                }
            }
        }

        Configuration cfg;

        //GenericDocumentsFields.copyPatientDetails(pDetails, null, patientId, false);
        Map templateParamsMap = new HashMap();
        templateParamsMap.put("appointmentDetails", appList);
        templateParamsMap.put("ResourceDetails", resourceList);
        templateParamsMap.put("consultation", docMap);
        templateParamsMap.put("eqtests", equipmentMapForTest);
        templateParamsMap.put("radiotests", radiologistMap);
        templateParamsMap.put("eqservices", equipmentMapForService);
        templateParamsMap.put("docservices", docMapForService);
        templateParamsMap.put("surgsurgery", surgeonMapForSurgery);
        templateParamsMap.put("anessurgery", anesMapForSurgery);
        templateParamsMap.put("eqsurgery", equipmentMapForSurgery);
        templateParamsMap.put("modules_activated", RequestContext.getSession().getAttribute("preferences"));
        templateParamsMap.put("username", RequestContext.getSession().getAttribute("userid"));
        templateParamsMap.put("category",category);

        cfg = AppInit.getFmConfig();
        cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);

        OutputStream os = null;
        HtmlConverter hc = new HtmlConverter();
        int printerId = 0;
        String printerIdStr = req.getParameter("print_type");
        if ((printerIdStr != null) && !printerIdStr.equals("")) {
            printerId = Integer.parseInt(printerIdStr);
        }
        BasicDynaBean printPrefs = PrintConfigurationsDAO.getPageOptions(
                PrintConfigurationsDAO.PRINT_TYPE_APPOINTMENT, printerId);

        String printMode = "P";
        if (printPrefs.get("print_mode") != null) {
            printMode = (String) printPrefs.get("print_mode");
        }

        Template t = null;

        String templateContent=null;
        StringWriter writer = new StringWriter();
        PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
        templateContent = printtemplatedao
            .getCustomizedTemplate(PrintTemplate.Appointment);
        if (templateContent == null || templateContent.equals("")) {
            t = cfg.getTemplate(PrintTemplate.Appointment.getFtlName() + ".ftl");
        } else {
            StringReader reader = new StringReader(templateContent);
            t = new Template("AppointmentPrint.ftl", reader, AppInit.getFmConfig());
        }

        t.process(templateParamsMap, writer);
        if (printMode.equals("P")) {
            os = res.getOutputStream();
            res.setContentType("application/pdf");
            boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info")).equalsIgnoreCase("Y");
            hc.writePdf(os, writer.toString(), "Appointment Details", printPrefs, false,repeatPatientHeader,true,
                    true, true, false);
            os.close();
            return null;
        } else {
            String textReport = new String(hc.getText(writer.toString(),"Appointment Details", printPrefs, true, true));
            req.setAttribute("textReport", textReport);
            req.setAttribute("textColumns", printPrefs.get("text_mode_column"));
            req.setAttribute("printerType", "DMP");
            return mapping.findForward("textPrintApplet");
        }
    }

    public ActionForward setArrivedStatus(ActionMapping am,
            ActionForm af, HttpServletRequest req, HttpServletResponse res)
    throws Exception {

        FlashScope flash = FlashScope.getScope(req);
        ActionRedirect redirect  = null;
        String category = req.getParameter("category");
        if (category == null || category.equals(""))
            category = req.getParameter("_category");
        String appointmentIdStr = req.getParameter("appointmentId");

        int appointmentId = Integer.parseInt(appointmentIdStr);
        String userName = (String)(req.getSession(false).getAttribute("userid"));
        boolean status = false;
        Connection con = null;

        if(req.getHeader("Referer") != null) {
            redirect = new ActionRedirect(req.getHeader("Referer").
                    replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
        } else {
            if (am.getProperty("action_id").endsWith("_week_scheduler"))
                redirect = new ActionRedirect(am.findForward("weekViewRedirect"));
            else if(am.getProperty("action_id").equals("today_resource_scheduler")){
                if(category.equals("DOC")) {
                    redirect = new ActionRedirect(am.findForward("docSchedulerDetails"));
                } else if(category.equals("SNP")) {
                    redirect = new ActionRedirect(am.findForward("snpSchedulerDetails"));
                } else if(category.equals("OPE")) {
                    redirect = new ActionRedirect(am.findForward("opeSchedulerDetails"));
                } else if(category.equals("DIA")) {
                    redirect = new ActionRedirect(am.findForward("diaSchedulerDetails"));
                }
            } else
                redirect = new ActionRedirect(am.findForward("dayViewRedirect"));
        }

        try {
        	con = DataBaseUtil.getConnection();
        	con.setAutoCommit(false);
        	status = new ResourceDAO(null).updateStatus(con, ResourceDTO.APPT_ARRIVED_STATUS, appointmentId, userName);
        } finally {
        	DataBaseUtil.commitClose(con, status);
        }

        if (status) {
        	 flash.put("info", "Appointment details saved...");
        	 schedulePushEvent(appointmentIdStr, Events.APPOINTMENT_ARRIVED);
        }
        else
        	flash.put("error", "Transaction Failure...");
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;

    }

    @IgnoreConfidentialFilters
    public ActionForward getSchedulerRegistration(ActionMapping am,
            ActionForm af, HttpServletRequest req, HttpServletResponse res)
    throws Exception {

    	// on arrived from scheduler screen this method has been called.
        FlashScope flash = FlashScope.getScope(req);
        ActionRedirect redirect  = null;
        String category = req.getParameter("category");
        if (category == null || category.equals(""))
            category = req.getParameter("_category");

        // if patient is an active patinet and arrived is for an active visit from scheduler screen
        // then making the redirect according to category(it willredirect to specific scheduler screen after posting the order.)
        if(req.getHeader("Referer") != null) {
            redirect = new ActionRedirect(req.getHeader("Referer").
                    replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
        } else {
            if (am.getProperty("action_id").endsWith("_week_scheduler"))
                redirect = new ActionRedirect(am.findForward("weekViewRedirect"));
            else if(am.getProperty("action_id").equals("today_resource_scheduler")){
                if(category.equals("DOC")) {
                    redirect = new ActionRedirect(am.findForward("docSchedulerDetails"));
                } else if(category.equals("SNP")) {
                    redirect = new ActionRedirect(am.findForward("snpSchedulerDetails"));
                } else if(category.equals("OPE")) {
                    redirect = new ActionRedirect(am.findForward("opeSchedulerDetails"));
                } else if(category.equals("DIA")) {
                    redirect = new ActionRedirect(am.findForward("diaSchedulerDetails"));
                }
            } else
                redirect = new ActionRedirect(am.findForward("dayViewRedirect"));
        }

        HttpSession session = req.getSession(false);
        Map urlRightsMap = (Map)session.getAttribute("urlRightsMap");
        String opRegScreenRights = urlRightsMap.get("new_op_registration") != null ? (String)urlRightsMap.get("new_op_registration") : "N";
        String ipRegScreenRights = urlRightsMap.get("ip_registration") != null ? (String)urlRightsMap.get("ip_registration") : "N";
        String opIpRegScreenRights = urlRightsMap.get("op_ip_conversion") != null ? (String)urlRightsMap.get("op_ip_conversion") : "N";
        String dialysisOrderScreenRights = urlRightsMap.get("dialysis_order") != null ? (String)urlRightsMap.get("dialysis_order") : "N";
        String modAdvanceOTActive = "Y";
        String modInsExt = "N";
        Preferences pref = (Preferences)session.getAttribute("preferences");
        if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
            modAdvanceOTActive = (String) pref.getModulesActivatedMap().get("mod_advanced_ot");
            if (modAdvanceOTActive == null || "".equals(modAdvanceOTActive)) {
                modAdvanceOTActive = "N";
            }
            if(null != pref.getModulesActivatedMap().get("mod_ins_ext")){
            	modInsExt = (String) pref.getModulesActivatedMap().get("mod_ins_ext");
            	if (modInsExt == null || "".equals(modInsExt)) {
            		modInsExt = "N";
                }
            }

        }

        //Map actionRightsMap = (Map)session.getAttribute("actionRightsMap");
        //String newBillRights = actionRightsMap.get("new_bill_for_order_screen") != null ? (String)actionRightsMap.get("new_bill_for_order_screen") : "N";
        //String orderRights = urlRightsMap.get("order") != null ? (String)urlRightsMap.get("order") : "N";
        String newBillRights = "A";
        String orderRights = "A";

        // depending upon screens saving the appointment details..
        String err = null;
        if (req.getAttribute("screenId") != null && !req.getAttribute("screenId").equals("today_resource_scheduler")) {
        	// got called from scheduler screen...
        	if (req.getParameter("isArrivedDialogOpened") != null &&
        			req.getParameter("isArrivedDialogOpened").equals("Y")) {
        		// if patinet is inactive patinet and user is marking that patient arrived from scheduler then there is no need to save
        		// patient appointment details because appointment details are not modified..
        		// if patient is active then marking a patient arrived then need to save appointment details because appointment details could be modified.
	            err = appointmentEdit(req);
	            if (err != null) {
	                flash.put("error", err);
	                redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
	                return redirect;
	            }
        	}
        } else {
        	// got called from todays appointment screen,so calling specific method..
            err = appointmentEditFromTodaysScreen(req);
            if (err != null) {
                flash.put("error", err);
                redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                return redirect;
            }
        }

        String schedulerBillNo = null;
        Map resultMap = new HashMap();
        boolean result = true;
        String message = "Failed to save details";
        Integer appointmentId;
        String appointmentIdStr;
        if (req.getAttribute("newAppointmentId") != null ) {
        	// newAppointmentId attribute is set in appointmentEdit function
        	appointmentId = (Integer) req.getAttribute("newAppointmentId");
        	appointmentIdStr = String.valueOf(appointmentId);
        } else {
            appointmentIdStr = req.getParameter("appointmentId");
            appointmentId = Integer.parseInt(appointmentIdStr);
        }
        
		String actionId= am.getProperty("action_id");
        /*Condition Link Updation for New Registration Ui */
        boolean redirectNewUi = actionId.equals("snp_scheduler") 
                || actionId.equals("dia_scheduler")
                || actionId.equals("doc_scheduler")
                || category.equals("DOC")
                || category.equals("SNP")
                || category.equals("DIA");
		
        BasicDynaBean appbean = new GenericDAO("scheduler_appointments").findByKey("appointment_id", appointmentId);
        String replaceValueStr = null;
        String userName = (String)(req.getSession(false).getAttribute("userid"));
        String mrno = (String)appbean.get("mr_no");
        boolean isContact = false;
        if (appbean.get("contact_id") != null && !appbean.get("contact_id").equals("")) {
          replaceValueStr = String.valueOf(appbean.get("contact_id"));
          isContact = true;
        } else if (mrno !=null && !mrno.equals("")) {
          replaceValueStr = mrno;
        }
        String visitId = req.getParameter("patient_id");
        Connection con = null;
        Bill bill = null;
        BasicDynaBean apptBean = new GenericDAO("scheduler_appointments").findByKey("appointment_id", appointmentId);
        boolean conduction = false;
        BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
        String opApplicableFor =  (String)genPrefs.get("operation_apllicable_for");
        String scheduleId = (apptBean.get("res_sch_name") == null ||
                            ((String)apptBean.get("res_sch_name")).equals("")) ? null : (String)apptBean.get("res_sch_name");

        String dateStr =(String)req.getParameter("date");
        String timeStr = null;

        if(!"".equals(req.getParameter("slotTime")))
            timeStr=(String)req.getParameter("slotTime");
        else
            timeStr=(String)req.getParameter("time");

        Timestamp appointmentTime = null;
        String timeStampStr = dateStr + " " + timeStr;
        if(dateStr != null && !dateStr.equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:mm");
            java.util.Date date = (java.util.Date)dateFormat.parse(timeStampStr);
            appointmentTime = new java.sql.Timestamp(date.getTime());
        } else {
            appointmentTime = (Timestamp)apptBean.get("appointment_time");
        }

        RegistrationPreferencesDTO regPrefs = RegistrationPreferencesDAO.getRegistrationPreferences();
        String allowMultipleActiveVisits = (regPrefs.getAllow_multiple_active_visits() != null
                                                && !regPrefs.getAllow_multiple_active_visits().equals(""))
                                            ? regPrefs.getAllow_multiple_active_visits() : "N";

        if (apptBean.get("appointment_status").equals("Arrived")) {
            flash.put("error", "patient is already arrived");
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
        }

       // getting item conduction status... if it is service,test or operation..
        if (category.equals("OPE") || category.equals("DIA") || category.equals("SNP")) {
            if (apptBean.get("res_sch_name") != null && !((String)apptBean.get("res_sch_name")).equals("")) {
                conduction = new ResourceDAO(null).getConductionForTestOrServiceOrOperation(category, (String)apptBean.get("res_sch_name"));
            } else {
                conduction = true;
            }
        }

        // doctor does not required any conduction..
        if (category.equals("DOC")) {
            conduction = true;
        }

        /**
         *  Get the visitId from db -- if visit id is empty then get the mrno.
         *  If the visit is active then post order.
         *  If visit is inactive then redirect to registration screen.
         *  If no visit also redirect to registration screen.
         *
         */
        boolean arrived = false;
        try {

            con = DataBaseUtil.getConnection();
            con.setAutoCommit(false);
            ResourceDAO resdao = new ResourceDAO(con);
            String consTypeIdStr = req.getParameter("consultationTypes");
            consTypeIdStr = (consTypeIdStr !=null && !"".equals(consTypeIdStr)) ? consTypeIdStr : "0";
            int consTypeId = Integer.parseInt(consTypeIdStr);

            String schPriorAuthId = req.getParameter("scheduler_prior_auth_no");
            int schPriorAuthModeId = 0;
            String schPriorAuthModeIdStr = req.getParameter("scheduler_prior_auth_mode_id");
            schPriorAuthModeId = (schPriorAuthModeIdStr != null && !schPriorAuthModeIdStr.equals("")) ?
                    Integer.parseInt(schPriorAuthModeIdStr) : 0;

            boolean isVisitActive = false;
            if (visitId != null && !visitId.equals("")) {
                isVisitActive = VisitDetailsDAO.isVisitActive(con, visitId);
            }

            // if scheduled item is an operation then updating prior auth info.
            if (category.equals("OPE")) {
                    new ResourceDAO(null).updateSchedulerPriorInfo(con,appointmentId,schPriorAuthId,schPriorAuthModeId);
            }
            if(mrno != null && !mrno.equals("")) {
                BasicDynaBean patientBean = PatientDetailsDAO.getPatientGeneralDetailsBean(mrno);

                // if multiple visit is not allowed for a active patinet then coming up with error message.
                if (allowMultipleActiveVisits.equals("N")) {
                    String lastVisitId = null;
                    List<BasicDynaBean> activeVisits = VisitDetailsDAO.getPatientVisits(mrno, true);
                    if (activeVisits != null && activeVisits.size() > 0) {
                        BasicDynaBean activeVisitBean = activeVisits.get(0);
                        lastVisitId = (String)activeVisitBean.get("patient_id");
                        if (!isVisitActive || (isVisitActive && visitId.equals(lastVisitId)))
                            result = true;
                        else
                            result = false;
                    }

                    if (!result && lastVisitId != null) {
                        flash.info("Patient is already registered with ID: "+lastVisitId);
                        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                        return redirect;
                    }
                }

                // if visit is inactive redirecting to registration screen(IP/OP depending upon category and operation_applicable flag)
                // Visit is inactive.
                if(!isVisitActive) {
                    if (category != null) {
                        if (category.equals("OPE")) {
                            if (!ipRegScreenRights.equals("A")) {
                                flash.error("You are not authorized to register IP Patient");
                                redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                                return redirect;
                            }
                            redirect = new ActionRedirect(am.findForward("ipSchedulerRegistration"));
                            redirect.addParameter("appointment_id", appointmentIdStr);
                            redirect.addParameter("contact_id", replaceValueStr);
                            redirect.addParameter("category", category);
                            redirect.addParameter("registrationType", "IP");
                            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                        } else {
                        		if (!opRegScreenRights.equals("A")) {
                        			flash.error("You are not authorized to register OP Patient");
                        			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                        			return redirect;
                        		}
                        		if(modInsExt.equals("Y")){
                        			if(!dialysisOrderScreenRights.equals("A")){
                        				flash.error("You are not authorized to access dialysis order screen.");
                        				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                        				return redirect;
                        			}
                        			redirect = new ActionRedirect(am.findForward("dialysisOrder"));
                        			redirect.addParameter("mrNo", mrno);
                        		}else{
                                    String newPath = "/patients/opregistration/index.htm#/filter/1/patient/"
                                        + URLEncoder.encode(replaceValueStr, "UTF-8")
                                        + "/registration/visit/new?retain_route_params=true";
                        			redirect = new ActionRedirect(newPath);
                        		}
                        		redirect.addParameter("appointment_id", appointmentIdStr);
                                if (isContact) {
                                  redirect.addParameter("contact_id", replaceValueStr);                          
                                } else {
                                  redirect.addParameter("mrNo", mrno);
                                }
                        		redirect.addParameter("category", category);
                        		redirect.addParameter("registrationType", "OP");
                        		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                        		return redirect;
                        }
                    }
                } else {
                    // Visit is active
                    Map patientMap = VisitDetailsDAO.getPatientVisitDetailsMap(con, visitId);
                    String visit_type = (String)patientMap.get("visit_type");

                    //	For Surgery redirecting to the opip conversion screen or IP registration screen or order surgery
                    opApplicableFor = opApplicableFor.equals("b") ? visit_type :  opApplicableFor;

                    do{
                    	boolean isChannelingAppt = false;
                        if (visitId != null) {
                            	int pat_package_id  = -1;
                            	if(apptBean.get("pat_package_id") != null) {
                            	    pat_package_id = (Integer)apptBean.get("pat_package_id");
                            	}
                            	//channelling appointment
                            	if(pat_package_id != -1) {
                            		isChannelingAppt = true;
                            		List newPreAuths = new ArrayList();
                            		List secNewPreAuths = new ArrayList();
                            		List firstOfCategoryList = new ArrayList();
                            		List<String> condDoctrsList = new ArrayList<String>();
                            		List<Integer> preAuthModeList = new ArrayList<Integer>();
                            		List<Integer> secPreAuthModeList = new ArrayList<Integer>();
                            		List<Map<String,Object>> operationAnaesTypesList = new ArrayList<Map<String,Object>>();
                            		List<Map<String,Object>> opEditAnaesTypesList = new ArrayList<Map<String,Object>>();
                            		OrderBO order = new OrderBO(); // BO to share the commonOrderId, billNo etc.
                    			    List newOrders = new ArrayList();
                    			    List<Boolean> multiVisitPackageList = new ArrayList<Boolean>();
                    			    order.setBillInfo(con, visitId, null, false, userName);
                    				//billNo = (String)((BasicDynaBean)order.getBill()).get("bill_no");
                        			GenericDAO patPackDao = new GenericDAO("patient_packages");
                        			Map<String,Object> identifiers = new HashMap<String, Object>();
                        			identifiers.put("mr_no", apptBean.get("mr_no"));
                        			identifiers.put("pat_package_id", pat_package_id);
                        			identifiers.put("status", "P");
                        			BasicDynaBean patPackBean = null;
                        			patPackBean = patPackDao.findByKey(identifiers);
                        			int package_id = (Integer)patPackBean.get("package_id");

                        			GenericDAO packPresDao = new GenericDAO("package_prescribed");
                        			identifiers.remove("status");
                        			BasicDynaBean packPrescBeanOld = null;
                        			packPrescBeanOld = packPresDao.findByKey(identifiers);
                        			int common_order_id = (Integer)packPrescBeanOld.get("common_order_id");
                        			BasicDynaBean newMVPOrdersBean = packPresDao.getBean();
                        			newMVPOrdersBean.set("package_id", package_id);
                    				newMVPOrdersBean.set("remarks", "");
                    				order.orderMultiVisitPackageForChannelling(con, newMVPOrdersBean, (Integer)patPackBean.get("pat_package_id"));
                        			List packItemsList = PackageDAO.getPackageComponents(package_id);

                    				for(int i = 0; i < packItemsList.size(); i++) {
                    				    BasicDynaBean itemBean = (BasicDynaBean)packItemsList.get(i);
                    					String itemType = (String)itemBean.get("item_type");
                    					/*if (itemType.equals("Laboratory") || itemType.equals("Radiology")) {
                    					    GenericDAO testDao = new GenericDAO("tests_prescribed");
                    						BasicDynaBean b = testDao.getBean();
                    					}*/
                    					BasicDynaBean bean = null;
                    					Timestamp arrivalTime = DateUtil.getCurrentTimestamp();
                                        if(itemType.equals("Doctor")) {
                                        	GenericDAO doctorDao = new GenericDAO("doctor_consultation");
                                        	identifiers.remove("status");
                                        	identifiers.remove("pat_package_id");
                                        	identifiers.put("common_order_id", common_order_id);
                                        	bean = doctorDao.findByKey(identifiers);
                                        	if(bean == null) {
                                        	    bean = doctorDao.getBean();
                                			    bean.set("doctor_name", apptBean.get("res_sch_name"));
                                			    bean.set("presc_doctor_id", apptBean.get("presc_doc_id"));
                                			    if(appointmentTime.getTime() < arrivalTime.getTime())
                            						bean.set("visited_date", arrivalTime);
                            					else
                            						bean.set("visited_date", appointmentTime);
                                			    bean.set("presc_date", new java.sql.Timestamp(System.currentTimeMillis()));
                                			    bean.set("remarks", itemBean.get("remarks"));
                                			    bean.set("head", ((Integer)itemBean.get("consultation_type_id")).toString());
                                			    bean.set("appointment_id", appointmentId);
                            					bean.set("status", "A");
                                			    newOrders.add(bean);
                                			    multiVisitPackageList.add(new Boolean(true));
                                        	}
                                        }
                    					if(itemType.equalsIgnoreCase("service")) {
                    					    GenericDAO servDao = new GenericDAO("services_prescribed");
                                        	identifiers.remove("status");
                                        	identifiers.remove("pat_package_id");
                                        	identifiers.put("common_order_id", common_order_id);
                                        	bean = servDao.findByKey(identifiers);
                                        	if(bean == null) {
                    						    bean = servDao.getBean();
                    						    bean.set("service_id", (String)itemBean.get("activity_id"));
                    						    bean.set("remarservDaoks", (String)itemBean.get("description"));
                    						    bean.set("quantity", new java.math.BigDecimal((Integer)itemBean.get("activity_qty")));
                    						    bean.set("presc_date", DataBaseUtil.getDateandTime());
                    						    newOrders.add(bean);
                    						    multiVisitPackageList.add(new Boolean("true"));
                                        	}
                    					}
                    				}
                    				err = order.orderItems(con, newOrders,newPreAuths,preAuthModeList,firstOfCategoryList,condDoctrsList,
                    						multiVisitPackageList, null, null, null, appointmentId, true, false, true, secNewPreAuths, secPreAuthModeList,
                    						operationAnaesTypesList, new ArrayList<List<TestDocumentDTO>>());
                    				/*result = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,userName);
                                    if (!result) break;*/
                                    schedulerBillNo = (String)((BasicDynaBean)order.getBill()).get("bill_no");
                            	}
                            bill = BillDAO.getVisitCreditBill(visitId, true);
                            if ((category.equals("DOC") && consTypeId != 0) || (scheduleId != null && !scheduleId.equals(""))) {
                                if (isVisitActive && bill == null && !newBillRights.equals("A")) {
                                    result = false;
                                    flash.put("error", "There is no/open Primary Credit Bill. You are not authorized to create new bill.");
                                    if (!result) break;
                                }
                                if (!orderRights.equals("A")) {
                                    result = false;
                                    flash.put("error", "You are not authorized to order.");
                                    if (!result) break;
                                }
                            }
                        }

                    /*	result = new ResourceDAO(con).updateStatus(ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,userName);
                        if (!result) break;*/

                        // for all category ordering respective items and saveing the appointment details.
                        //	Consultation, Test, Service
                        if(!isChannelingAppt) {
                        if (!category.equals("OPE")) {

                        	if (category.equals("DIA")) {
                        		if (apptBean.get("res_sch_name") != null && !((String)apptBean.get("res_sch_name")).equals("")) {
                        			BasicDynaBean testBean = AddTestDAOImpl.getTestBean((String) apptBean.get("res_sch_name"));
                        			String mandate_additional_info = (String) testBean.get("mandate_additional_info");
                        			if (mandate_additional_info.equals("O")) {
                        				result = false;
                        				flash.put("error", "Direct ordering from scheduler not allowed since Test needs additional document.");
                        				break;
                        			}
                        		}
                        	}

                            resultMap = ResourceBO.order(con, appointmentId, visitId, category, userName, conduction, appointmentTime);
                            result = (resultMap != null && resultMap.get("result") != null) ? (Boolean)resultMap.get("result") : false;
                            if (!result) break;

                            if (category.equals("DIA") && !conduction) {
                                result = new ResourceDAO(con).updateTestOrServiceOrOperationStatus(appointmentId,category);
                                if (result) 
                                  schedulePushEvent(String.valueOf(appointmentId), Events.APPOINTMENT_COMPLETED);
                                else
                                  break;

                                result = resdao.updateStatus(ResourceDTO.APPT_COMPLETED_STATUS, appointmentId,userName);
             
                                if (!result) break;
                            } else if (category.equals("SNP") && scheduleId != null && !conduction) {
                                result = new ResourceDAO(con).updateTestOrServiceOrOperationStatus(appointmentId,category);
                                if (!result) break;

                                result = resdao.updateStatus(ResourceDTO.APPT_COMPLETED_STATUS, appointmentId,userName);
                                if (result) 
                                  schedulePushEvent(String.valueOf(appointmentId), Events.APPOINTMENT_COMPLETED);
                                else
                                  break;
                            } else {
                                result = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,userName);
                                arrived = true;
                                if (arrived) 
                                  schedulePushEvent(String.valueOf(appointmentId), Events.APPOINTMENT_ARRIVED);
                                if (!result) break;
                            }
                        } else if (category.equals("OPE")) {
                            //	Visit is active IP or OP and opApplicableFor is 'i' or 'o'
                            if(visit_type.equals(opApplicableFor)) {
                                if(modAdvanceOTActive.equals("Y")) {
                                	// if advanced ot module is enabled then only saving the appointment details,not ordering an item.
                                } else if (modAdvanceOTActive.equals("N") && scheduleId != null) {
                                    //	if operation is applicable for op patient then order the operation.
                                    resultMap = ResourceBO.order(con, appointmentId, visitId, category, userName, conduction,appointmentTime);
                                    result = (resultMap != null && resultMap.get("result") != null) ? (Boolean)resultMap.get("result") : false;
                                    if (!result) break;

                                    if (!conduction) {
                                        result = new ResourceDAO(con).updateTestOrServiceOrOperationStatus(appointmentId,category);
                                        if (!result) break;

                                        result = resdao.updateStatus(ResourceDTO.APPT_COMPLETED_STATUS, appointmentId,userName);
                                        if (result) 
                                          schedulePushEvent(String.valueOf(appointmentId), Events.APPOINTMENT_COMPLETED);
                                        else
                                          break;
                                    }
                                } else {
                                    result = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,userName);
                                    arrived = true;
                                    if (arrived) 
                                      schedulePushEvent(String.valueOf(appointmentId), Events.APPOINTMENT_ARRIVED);
                                    if (!result) break;
                                }

                            }else if(visit_type.equals("i")) {
                                // for ip patient arrived menu is disabled in scheduler because IP patient is already arrived.
                            }else if(visit_type.equals("o")) {
                                if (!opIpRegScreenRights.equals("A")) {
                                    flash.error("You are not authorized to convert OP to IP Patient");
                                    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                                    return redirect;
                                }
                                redirect = new ActionRedirect(am.findForward("opIpConverstionScreen"));
                                redirect.addParameter("patient_id", visitId);
                                redirect.addParameter("appointment_id", appointmentIdStr);
                                redirect.addParameter("category", category);
                                redirect.addParameter("mrno", mrno);
                                redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                                return redirect;
                            }
                        }

                        schedulerBillNo = (resultMap != null && resultMap.get("billNo") != null) ? (String)resultMap.get("billNo") : null;
                        }
                        if(isChannelingAppt) {
                        	int consType = (Integer)apptBean.get("consultation_type_id");
                        	result = ResourceBO.updateScheduler(con, appointmentId, mrno,
                                    visitId, patientBean, (String)appbean.get("complaint"),userName,
                                    consType,(String)appbean.get("remarks"),(String)appbean.get("presc_doc_id"),"Sche");//updating all scheduler details in scheduler_appointments
                        } else {
                            result = ResourceBO.updateScheduler(con, appointmentId, mrno,
                                visitId, patientBean, (String)appbean.get("complaint"),userName,
                                category.equals("DOC") ? consTypeId : 0,(String)appbean.get("remarks"),(String)appbean.get("presc_doc_id"),"Sche");//updating all scheduler details in scheduler_appointments
                        }
                        if (!result) break;

                        if(category.equals("OPE") && modAdvanceOTActive.equals("Y")) {
                        	// if item is an operation and advanced ot module is enabled then copy all operation appointment realted details to advanced ot module.
                            err = new OperationDetailsBO().saveSurgeryAppointmnetToOpertionDetails(con, appointmentId,
                                    patientMap != null && patientMap.get("org_id") != null ? (String)patientMap.get("org_id") : null);

                            if(err != null) {
                                err = getResources(req).getMessage(err);
                                flash.error(err);
                                redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                                return redirect;
                            } else {
                                result = true;
                            }

                            if(!result) break;
                        }

                        result = new ResourceDAO(con).updateStatus(ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,userName);
                        arrived = true;
                        if (arrived) 
                          schedulePushEvent(String.valueOf(appointmentId), Events.APPOINTMENT_ARRIVED);
                        if (!result) break;

                        String orderMsg = (modAdvanceOTActive.equals("Y") && category.equals("OPE")) ? "" : "and Ordered";
                        if (result) {
                            message = "Appointment details saved...";
                            if (category.equals("DOC")) {
                                if (consTypeId != 0)
                                message += orderMsg;
                            }else if (apptBean.get("res_sch_name")!= null && !apptBean.get("res_sch_name").equals("")) {
                                message += orderMsg;
                            }
                            flash.put("info", message);
                        } else {
                            flash.put("error", message);
                        }
                    } while(false); //end of do.
                }
            } else {
                if (category != null) {
                    if (category.equals("OPE")) {
                        if (!ipRegScreenRights.equals("A")) {
                            flash.error("You are not authorized to register IP Patient");
                            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                            return redirect;
                        }
                        redirect = new ActionRedirect(am.findForward("ipSchedulerRegistration"));
                        redirect.addParameter("appointment_id", appointmentIdStr);
                        if (isContact) {
                          redirect.addParameter("contact_id", replaceValueStr);                          
                        } else {
                          redirect.addParameter("mrNo", mrno);
                        }
                        redirect.addParameter("category", category);
                        redirect.addParameter("registrationType", "IP");
                        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                    } else {
                        	if (!opRegScreenRights.equals("A")) {
                        		flash.error("You are not authorized to register OP Patient");
                        		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                        		return redirect;
                        	}
                        	if(modInsExt.equals("Y")){
                        		if(!dialysisOrderScreenRights.equals("A")){
                        			flash.error("You are not authorized to access dialysis order screen.");
                        			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                        			return redirect;
                        		}
                        		redirect = new ActionRedirect(am.findForward("dialysisOrder"));
                        		redirect.addParameter("mrNo", mrno);
                        	}else{
                        	  String newPath="";
                            if (isContact) {
                              newPath = "/patients/opregistration/index.htm#/filter/1/contact/"
                                  + URLEncoder.encode(replaceValueStr, "UTF-8")
                                  + "/registration/visit/new?retain_route_params=true";
                            } else {
                              newPath = "/patients/opregistration/index.htm#/filter/1/patient/"
                                  + URLEncoder.encode(replaceValueStr, "UTF-8")
                                  + "/registration/visit/new?retain_route_params=true";
                            }
                        		
                        		redirect = new ActionRedirect(newPath);
                        	}
                        	
                        	redirect.addParameter("appointment_id", appointmentIdStr);
                            if (isContact) {
                              redirect.addParameter("contact_id", replaceValueStr);                          
                            } else {
                              redirect.addParameter("mrNo", mrno);
                            }
                        	redirect.addParameter("category", category);
                        	redirect.addParameter("registrationType", "OP");
                        	redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                            return redirect;
                    }
                }
            }
        } finally {
            DataBaseUtil.commitClose(con, result);

            if(result && arrived) {
            	if (PractoBookHelper.isPractoAdvantageEnabled()) {
					// Update cancelled appt to Practo
					PractoBookHelper.addDoctorAppointmentsToPracto(appointmentId, false);
            	}
            }
//          change as per new insurance-3.0 calculator
            if(null != visitId && !visitId.equals("") && !visitId.equals("None")){
            	new SponsorBO().recalculateSponsorAmount(visitId);
            }	
            if (result && schedulerBillNo != null && !schedulerBillNo.equals("")) {
                BillDAO.resetTotalsOrReProcess(schedulerBillNo);
            }
        }
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
    }

    //Todo : remove this method. As the registration_on_arrival preference is made obsolete, this is not required anymore
    @IgnoreConfidentialFilters
    private boolean registrationRedirectionReqd(HttpSession session) throws SQLException {
  		String registrationOnArrival = (String) GenericPreferencesDAO.getAllPrefs().get("registration_on_arrival");
  		return registrationOnArrival.equalsIgnoreCase("Y");
    }
    
	private boolean saveStatusAndResourcesByCancelAndCreate(HttpServletRequest req)
			throws SQLException, ParseException, IOException {

		String[] resourceDelete = req.getParameterValues("_resourceDelete");
		String[] resourceNames = req.getParameterValues("_resourceType");
		String[] resourceValues = req.getParameterValues("_resourceValue");
		String status = req.getParameter("appointment_status");
		String appointmentId = req.getParameter("appointment_id");
		String userName = (String) (req.getSession(false).getAttribute("userid"));

		int resources = 0;
		if (resourceNames != null) {
			resources = resourceNames.length;
		}
		BasicDynaBean apptBean = new GenericDAO("scheduler_appointments").findByKey("appointment_id",
				Integer.parseInt(appointmentId));

		// create new appointment
		int newAppointmentId = Integer.parseInt(new ResourceDAO(null).getNextAppointMentId());
		req.setAttribute("appointmentId", newAppointmentId);
		String category = "";
		if ((Integer) apptBean.get("res_sch_id") == 1) {
			category = "DOC";
		} else if ((Integer) apptBean.get("res_sch_id") == 2) {
			category = "OPE";
		} else if ((Integer) apptBean.get("res_sch_id") == 3) {
			category = "SNP";
		} else if ((Integer) apptBean.get("res_sch_id") == 4) {
			category = "DIA";
		}
		String primResId = getPrimaryResourceId(resourceNames, resourceValues, resourceDelete, category);

		List<ResourceDTO> resourceInsertList = new ArrayList<ResourceDTO>();
		List<ResourceDTO> resourceUpdateList = new ArrayList<ResourceDTO>();
		List<ResourceDTO> resourceDeleteList = new ArrayList<ResourceDTO>();

		Timestamp modTime = DataBaseUtil.getDateandTime();
		for (int i = 0; i < resources; i++) {
			if (resourceDelete[i].equals("N")) {
				ResourceDTO rdto = new ResourceDTO();
				rdto.setAppointmentId(newAppointmentId);
				rdto.setResourceId(resourceValues[i]);
				rdto.setResourceType(resourceNames[i]);
				rdto.setUser_name(userName);
				rdto.setMod_time(modTime);
				resourceInsertList.add(rdto);
			}
		}
		
		Connection con = null;
		boolean result = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map<String, Object> fields = new HashMap<String, Object>();
			Map<String, Object> keys = new HashMap<String, Object>();

			fields.put("appointment_status", "Cancel");
			fields.put("cancel_reason", "Cancelling as primary resource rescheduled");
			fields.put("changed_by", userName);
			fields.put("changed_time", modTime);
			fields.put("cancel_type","Others");
			if ((Integer)apptBean.get("unique_appt_ind") == 0) {
				fields.put("unique_appt_ind", new ResourceDAO(null).getNextUniqueAppointMentInd());
			}
			keys.put("appointment_id", Integer.parseInt(appointmentId));
			// cancel the existing appointment
			result = ResourceBO.updateAppointments(con, fields, keys);
			
			apptBean.set("appointment_id", newAppointmentId);
			apptBean.set("prim_res_id", primResId);
			apptBean.set("booked_time", DateUtil.getCurrentTimestamp());
			apptBean.set("appointment_status", status);
			apptBean.set("booked_by", userName);

			Integer overbook = new ResourceDAO(null).isResourceOverbooked(con, primResId, category);
			boolean overBookAllowed = overbook == null || overbook > 0;
			int uniqueApptInd = 0;
			if (overBookAllowed) {
				uniqueApptInd = new ResourceDAO(null).getNextUniqueAppointMentInd();
			}
			apptBean.set("unique_appt_ind", uniqueApptInd);
			// create new appointment
			result = result && new GenericDAO("scheduler_appointments").insert(con, apptBean);
			// add new items
			result = result && new ResourceBO().updateSchedulerResourceDetails(con, resourceInsertList,
					resourceUpdateList, resourceDeleteList);
		} finally {
			if (result) {
				con.commit();
	      con.close();
				if (PractoBookHelper.isPractoAdvantageEnabled()) {
					// Update cancelled appt to Practo
					PractoBookHelper.addDoctorAppointmentsToPracto(Integer.parseInt(appointmentId), false);
					// Add new appt to Practo
					PractoBookHelper.addDoctorAppointmentsToPracto(newAppointmentId, true);
				}	
				// trigger events
				schedulePushEvent(appointmentId, Events.APPOINTMENT_CANCEL);
				schedulePushEvent(String.valueOf(newAppointmentId),"APPOINTMENT_"+ status.toUpperCase() );
			} else {
				con.rollback();
	      con.close();
			}
		}

		return result;
	}
	
	private String getPrimaryResourceId(String[] resourceNames, String[] resourceValues, String[] resourceDelete,
			String category) throws SQLException {
		if (resourceNames == null)
			return null;

		String primResType = new ResourceDAO(null).getPrimaryResourceType(category);
		for (int i = 0; i < resourceNames.length; i++) {
			if (resourceDelete[i].equals("N") && resourceNames[i].equals(primResType)) {
				return resourceValues[i];

			}
		}
		return null;
	}
	
    private boolean saveStatusAndResources(HttpServletRequest req) throws Exception {

        String[] resourceDelete = req.getParameterValues("_resourceDelete");
        String[] resourceTypes = req.getParameterValues("_resourceType");
        String[] resourceValues = req.getParameterValues("_resourceValue");
        String[] appointmentItemIds = req.getParameterValues("_appointmentItemId");
        String status = req.getParameter("appointment_status");
        String appointment_id = req.getParameter("appointment_id");
        req.setAttribute("appointmentId", Integer.parseInt(appointment_id));
        String userName = (String)(req.getSession(false).getAttribute("userid"));

        List<ResourceDTO> resourceInsertList = new ArrayList<ResourceDTO>();
        List<ResourceDTO> resourceUpdateList = new ArrayList<ResourceDTO>();
        List<ResourceDTO> resourceDeleteList = new ArrayList<ResourceDTO>();

        int resources = 0;
        if (resourceTypes != null) {
            resources = resourceTypes.length;
        }
        BasicDynaBean apptBean = new GenericDAO("scheduler_appointments").findByKey("appointment_id", Integer.parseInt(appointment_id));
        String category = "";
        if((Integer)apptBean.get("res_sch_id") == 1) {
        	category = "DOC";
        }
        if((Integer)apptBean.get("res_sch_id") == 2) {
        	category = "OPE";
        }
        if((Integer)apptBean.get("res_sch_id") == 3) {
        	category = "SNP";
        }
        if((Integer)apptBean.get("res_sch_id") == 4) {
        	category = "DIA";
        }
        String prim_res_id = getPrimaryResourceId(resourceTypes, resourceValues, resourceDelete, category);
        boolean result = true;
    	if (apptBean.get("prim_res_id") != null && !apptBean.get("prim_res_id").equals(prim_res_id)) {
			// Since the resource is changed, we cancel the existing appointment and create new one
    		result = saveStatusAndResourcesByCancelAndCreate(req);
    	} else {
	    	
	        for (int i = 0; i < resources; i++) {
	            ResourceDTO rdto = new ResourceDTO();
	            rdto.setAppointmentId(Integer.parseInt(appointment_id));
	            rdto.setResourceId(resourceValues[i]);
	            rdto.setResourceType(resourceTypes[i]);
	            if (appointmentItemIds[i].equals("")
	                    || appointmentItemIds[i].equals("0")) {
	                if (resourceDelete[i].equals("N")) {
	                    resourceInsertList.add(rdto);
	                } else {
	                    resourceDeleteList.add(rdto);
	                }
	            } else {
	                rdto.setAppointment_item_id(Integer.parseInt(appointment_id));
	                if (resourceDelete[i].equals("N")) {
	
	                    resourceUpdateList.add(rdto);
	                } else {
	                    resourceDeleteList.add(rdto);
	                }
	            }
	        }
	
	        result = new ResourceBO().updateResourceDetails(new Integer(
	                appointment_id).intValue(), status, resourceInsertList,
	                resourceUpdateList, resourceDeleteList,userName);
	        if(!prim_res_id.equals("")) {
	        	 Map fields = new HashMap();
	        	 Map keys = new HashMap();
	        	 fields.put("prim_res_id", prim_res_id);
	        	 if(category.equals("DOC"))
	        		 fields.put("res_sch_name", prim_res_id);
	        	 keys.put("appointment_id", Integer.parseInt(appointment_id));
	        	 Connection con = DataBaseUtil.getConnection();
	        	 int updateCount =  new GenericDAO("scheduler_appointments").update(con, fields, keys);
	             con.close();
	        }
	        if (result && PractoBookHelper.isPractoAdvantageEnabled()) {
	        	PractoBookHelper.addDoctorAppointmentsToPracto(Integer.parseInt(appointment_id), false , resourceDeleteList);
	        }
	        schedulePushEvent(appointment_id, "APPOINTMENT_"+status);
    	}
    	int apptId = (Integer) req.getAttribute("appointmentId");
        if (null != status && status.equalsIgnoreCase("confirmed")
                && MessageUtil.allowMessageNotification(req,"scheduler_message_send") && 
                !(new ResourceDAO(null).getAppointmentSource(apptId)!=null && new ResourceDAO(null).getAppointmentSource(apptId).equalsIgnoreCase("practo"))) {
          MessageManager mgr = new MessageManager();
          Map appointmentData = new HashMap();
          appointmentData.put("appointment_id", apptId);
          appointmentData.put("status", status);
          mgr.processEvent("appointment_confirmed", appointmentData);
          if(category.equals("DOC")){
        	  mgr.processEvent("doc_appt_confirmed", appointmentData);
          }
        }

        return result;
    }

    @IgnoreConfidentialFilters
    public ActionForward getDoctorNonAvailabilityScreen(ActionMapping am,
            ActionForm af, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        if(req.getParameter("doctorId") != null && !req.getParameter("doctorId").equals("")) {
            req.setAttribute("doctorId", req.getParameter("doctorId"));
            req.setAttribute("deptId", DataBaseUtil.getStringValueFromDb("Select dept_id from doctors where doctor_id=?",
                req.getParameter("doctorId")));
        }
        JSONSerializer js = new JSONSerializer().exclude("class");
        req.setAttribute("doctorJSON", js.serialize( new ResourceDAO(null).getResourceMasterList("DOC")));
        return am.findForward("DoctorNonAvailabilityScreen");
    }

/*	public ActionForward saveDoctorNonAvailability(ActionMapping am,
            ActionForm af, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        JSONSerializer js = new JSONSerializer().exclude("class");
        String doctor = req.getParameter("doctor");
        String fromdate = req.getParameter("fromdate");
        String todate = req.getParameter("todate");

        String[] weekday = req.getParameterValues("weekday");
        String[] dayDelete = req.getParameterValues("deleteDay");

        java.sql.Date fdate = new java.sql.Date(DataBaseUtil.dateFormatter.parse(fromdate).getTime());
        java.sql.Date tdate = new java.sql.Date(DataBaseUtil.dateFormatter.parse(todate).getTime());

        //Note: ListIterator has previous(),hasPrevious() and remove() methods which are needed for manipulating the generatedList values so
        // used ListIterator instead of List.
        ListIterator generatedDatesList = new ResourceDAO(null).getGeneratedDates(fdate, tdate).listIterator();

        List timingList = new ResourceDAO(null).getTimingBetweenDates(doctor,fdate,tdate);


        List<TimingDTO> timingInsertList = new ArrayList<TimingDTO>();
        List<TimingDTO> timingUpdateList = new ArrayList<TimingDTO>();
        List<TimingDTO> timingDeleteList = new ArrayList<TimingDTO>();

        while(generatedDatesList.hasNext()) {
            TimingDTO tdto = new TimingDTO();
            tdto.setDoctor(doctor);
            BasicDynaBean genDateBean = (BasicDynaBean)generatedDatesList.next();
            Date genDt = (java.sql.Date)genDateBean.get("dates");
            int weekDayNo = new Integer((String)genDateBean.get("week_no")).intValue();
            if(timingList != null && timingList.size() > 0) {
                for (int t = 0; t < timingList.size(); t++) {
                    BasicDynaBean tBean = (BasicDynaBean)timingList.get(t);
                    Date existingDt = (java.sql.Date)tBean.get("non_available_date");
                    if(genDt.equals(existingDt)) {
                        for(int d=0;d<weekday.length;d++) {
                            if(weekDayNo == Integer.parseInt(weekday[d])) {
                                tdto.setNonAvailDate(genDt);
                                tdto.setWeekDay(weekDayNo);
                                setTimingDTOValues(tdto,req,weekDayNo);

                                if (dayDelete[d].equals("false")) {
                                    timingUpdateList.add(tdto);
                                } else {
                                    timingDeleteList.add(tdto);
                                }
                            }
                        }
                        generatedDatesList.remove();
                    }
                }
            }
        }
        while(generatedDatesList.hasPrevious()) {
            TimingDTO tdto = new TimingDTO();
            tdto.setDoctor(doctor);
            BasicDynaBean genDateBean = (BasicDynaBean)generatedDatesList.previous();
            Date genDt = (java.sql.Date)genDateBean.get("dates");
            int weekDayNo = new Integer((String)genDateBean.get("week_no")).intValue();
            for(int d=0;d<weekday.length;d++) {
                if(weekDayNo == Integer.parseInt(weekday[d])) {
                    tdto.setNonAvailDate(genDt);
                    tdto.setWeekDay(weekDayNo);
                    setTimingDTOValues(tdto,req,weekDayNo);
                    if (dayDelete[d].equals("false")) {
                        timingInsertList.add(tdto);
                    } else {
                        timingDeleteList.add(tdto);
                    }
                }
            }
        }
        String message = ResourceBO.saveDoctorNonavailabilityDetails(timingInsertList,timingUpdateList,timingDeleteList);

        req.setAttribute("doctorJSON", js.serialize(new ResourceDAO(null).getResourceMasterList("DOC")));

         FlashScope flash = FlashScope.getScope(req);
         flash.put("msg",message);
         List l = new ArrayList();
         l.add(doctor);
         int count = new ResourceDAO(null).getReschedulableAppCount(l,fdate,tdate,category);
         if (count > 0) {
            flash.put("rescheduleMsg", count + " appointments have to be rescheduled...");
         }
         ActionRedirect redirect = new ActionRedirect(am.findForward("redirectPage"));
         redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
         redirect.addParameter("method","getEditDoctorTimingScreen");
         redirect.addParameter("doctorId", doctor);
         redirect.addParameter("doctorName",req.getParameter("doctorName"));
         redirect.addParameter("deptName",req.getParameter("deptName"));
         return redirect;
    }*/


    private void setTimingDTOValues(TimingDTO tdto, HttpServletRequest req, int weekDayNo) throws ParseException {
        if(weekDayNo == 1) {
            tdto.setSun1(formatTime(req.getParameter("sun1")));
            tdto.setSun2(formatTime(req.getParameter("sun2")));
            tdto.setSun3(formatTime(req.getParameter("sun3")));
            tdto.setSun4(formatTime(req.getParameter("sun4")));
        }else if(weekDayNo == 2) {
            tdto.setMon1(formatTime(req.getParameter("mon1")));
            tdto.setMon2(formatTime(req.getParameter("mon2")));
            tdto.setMon3(formatTime(req.getParameter("mon3")));
            tdto.setMon4(formatTime(req.getParameter("mon4")));
        }else if(weekDayNo == 3) {
            tdto.setTue1(formatTime(req.getParameter("tue1")));
            tdto.setTue2(formatTime(req.getParameter("tue2")));
            tdto.setTue3(formatTime(req.getParameter("tue3")));
            tdto.setTue4(formatTime(req.getParameter("tue4")));
        }else if(weekDayNo == 4) {
            tdto.setWed1(formatTime(req.getParameter("wed1")));
            tdto.setWed2(formatTime(req.getParameter("wed2")));
            tdto.setWed3(formatTime(req.getParameter("wed3")));
            tdto.setWed4(formatTime(req.getParameter("wed4")));
        }else if(weekDayNo == 5) {
            tdto.setThu1(formatTime(req.getParameter("thu1")));
            tdto.setThu2(formatTime(req.getParameter("thu2")));
            tdto.setThu3(formatTime(req.getParameter("thu3")));
            tdto.setThu4(formatTime(req.getParameter("thu4")));
        }else if(weekDayNo == 6) {
            tdto.setFri1(formatTime(req.getParameter("fri1")));
            tdto.setFri2(formatTime(req.getParameter("fri2")));
            tdto.setFri3(formatTime(req.getParameter("fri3")));
            tdto.setFri4(formatTime(req.getParameter("fri4")));
        }else if(weekDayNo == 7) {
            tdto.setSat1(formatTime(req.getParameter("sat1")));
            tdto.setSat2(formatTime(req.getParameter("sat2")));
            tdto.setSat3(formatTime(req.getParameter("sat3")));
            tdto.setSat4(formatTime(req.getParameter("sat4")));
        }
    }

    public ActionForward markResourceNonAvailable(ActionMapping am,
            ActionForm af, HttpServletRequest req, HttpServletResponse res)
    throws Exception {

        String resourceName = req.getParameter("nonAvailableDoctor");
        String category = req.getParameter("category");
        String dateStr = req.getParameter("date");
        String dept = req.getParameter("department");
        String noAvlRemarks = req.getParameter("remarks");
        Time slotFromTime = DataBaseUtil.parseTime(req.getParameter("firstSlotFromTime"));
        Time slotToTime = DataBaseUtil.parseTime(req.getParameter("firstSlotToTime"));
        List<BasicDynaBean> resourceAvailabilityList = null;
        BasicDynaBean overrideBean = null;
        BasicDynaBean nonAvailBean = null;
        Time s_fromTime = null;
        Time s_toTime = null;
        Connection con = null;
        Date d = DataBaseUtil.parseDate(dateStr);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int max_center = (Integer)new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default");
        int weekDayNo = (cal.get(Calendar.DAY_OF_WEEK)-1);
        BasicDynaBean mainBean = null;
        boolean success = false;
        String resourceType = null;
        if (category.equals("DOC")){
            resourceType = category;
            if(max_center > 1){
            	return markResourceNonAvailableCenterWise(am,af,req,res);
            }
        }
        else if(category.equals("DIA"))
            resourceType = "EQID";
        else if(category.equals("SNP"))
            resourceType = "SRID";
        else if(category.equals("OPE"))
            resourceType = "THID";
        if(slotFromTime != null && slotToTime != null) {
            try {
                con = DataBaseUtil.getConnection();
                con.setAutoCommit(false);
                resourceAvailabilityList = new ResourceDAO(null).getResourceAvailabilities(resourceType, d, resourceName,null,null);
                if (resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
                    overrideBean = (BasicDynaBean)resourceAvailabilityList.get(0);
                }
                if (resourceAvailabilityList != null && resourceAvailabilityList.size() < 1) {
                    resourceAvailabilityList = new ResourceDAO(null).getResourceDefaultAvailabilities(resourceName, weekDayNo, resourceType,null,null);
                }

                if (resourceAvailabilityList != null && resourceAvailabilityList.size() < 1) {
                    resourceAvailabilityList = new ResourceDAO(null).getResourceDefaultAvailabilities("*", weekDayNo, resourceType,null,null);
                }
                if (overrideBean != null) {
                    int resAvailId = (Integer)overrideBean.get("res_avail_id");
                    mainBean = new GenericDAO("sch_resource_availability").getBean();
                    mainBean.set("res_avail_id", resAvailId);
                    new GenericDAO("sch_resource_availability_details").delete(con, "res_avail_id", resAvailId);

                    int index = 0;
                    boolean isDone = false;
                    if (resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
                        for (int i=0;i<resourceAvailabilityList.size();i++) {
                            nonAvailBean = (BasicDynaBean)resourceAvailabilityList.get(i);
                            Time fromTime = (Time)nonAvailBean.get("from_time");
                            Time toTime = (Time)nonAvailBean.get("to_time");
                            String availabilityStatus = (String)nonAvailBean.get("availability_status");
                            String remarks = (String)nonAvailBean.get("remarks");
                            if (fromTime != null && toTime != null) {
                                if(resourceAvailabilityList.size() == 1 ) {

                                    if(!slotFromTime.equals(fromTime))
                                    	bo.insertTimings(con, mainBean, fromTime, slotFromTime, "A",remarks,null);

                                    bo.insertTimings(con, mainBean, slotFromTime, slotToTime, "N",noAvlRemarks,null);

                                    if(!slotToTime.equals(toTime))
                                    	bo.insertTimings(con, mainBean, slotToTime, toTime, "A",null,null);

                                } else if(availabilityStatus.equals("A") && !isDone && i != resourceAvailabilityList.size()-1
                                        && resourceAvailabilityList.get(i+1).get("availability_status").equals("A")) {

                                    s_fromTime = index == 0 ? fromTime : s_fromTime;
                                    index++;

                                } else {
                                    if(!isDone && (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime))
                                            && (slotToTime.equals(toTime) || slotToTime.before(toTime))) {

                                        s_fromTime = index == 0 ? fromTime : s_fromTime;
                                        index++;
                                    }
                                    if(index != 0) {

                                            if(!slotFromTime.equals(s_fromTime))
                                            	bo.insertTimings(con, mainBean, s_fromTime, slotFromTime, "A",null,null);

                                            bo.insertTimings(con, mainBean, slotFromTime, slotToTime, "N",noAvlRemarks,null);

                                            if(i != resourceAvailabilityList.size()-1)
                                                s_toTime = (Time)resourceAvailabilityList.get(i+1).get("from_time");
                                            else
                                                s_toTime = toTime;

                                            if(!slotToTime.equals(s_toTime))
                                            	bo.insertTimings(con, mainBean, slotToTime, s_toTime, "A",null,null);

                                            index = 0;
                                            isDone = true;

                                    } else {
                                    	bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,null);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    int genResAvailId = new GenericDAO("sch_resource_availability").getNextSequence();
                    mainBean = new GenericDAO("sch_resource_availability").getBean();
                    mainBean.set("res_avail_id",genResAvailId);
                    mainBean.set("res_sch_name", resourceName);
                    mainBean.set("res_sch_type", resourceType);
                    mainBean.set("availability_date", d);
                    new GenericDAO("sch_resource_availability").insert(con, mainBean);
                    int index = 0;
                    boolean isDone = false;

                    if (resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
                        for (int i=0;i<resourceAvailabilityList.size();i++) {
                            nonAvailBean = (BasicDynaBean)resourceAvailabilityList.get(i);
                            Time fromTime = (Time)nonAvailBean.get("from_time");
                            Time toTime = (Time)nonAvailBean.get("to_time");
                            String availabilityStatus = (String)nonAvailBean.get("availability_status");
                            String remarks = (String)nonAvailBean.get("remarks");
                            if (fromTime != null && toTime != null) {
                                if(resourceAvailabilityList.size() == 1 ) {

                                    if(!slotFromTime.equals(fromTime))
                                    	bo.insertTimings(con, mainBean, fromTime, slotFromTime, "A",null,null);

                                    bo.insertTimings(con, mainBean, slotFromTime, slotToTime, "N",noAvlRemarks,null);

                                    if(!slotToTime.equals(toTime))
                                    	bo.insertTimings(con, mainBean, slotToTime, toTime, "A",null,null);

                                } else if(availabilityStatus.equals("A") && !isDone && i != resourceAvailabilityList.size()-1
                                        && resourceAvailabilityList.get(i+1).get("availability_status").equals("A")) {

                                    s_fromTime = index == 0 ? fromTime : s_fromTime;
                                    index++;

                                } else {
                                    if(!isDone && (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime))
                                            && (slotToTime.equals(toTime) || slotToTime.before(toTime))) {

                                        s_fromTime = index == 0 ? fromTime : s_fromTime;
                                        index++;
                                    }
                                    if(index != 0) {

                                            if(!slotFromTime.equals(s_fromTime))
                                            	bo.insertTimings(con, mainBean, s_fromTime, slotFromTime, "A",null,null);

                                            bo.insertTimings(con, mainBean, slotFromTime, slotToTime, "N",noAvlRemarks,null);

                                            if(i != resourceAvailabilityList.size()-1)
                                                s_toTime = (Time)resourceAvailabilityList.get(i+1).get("from_time");
                                            else
                                                s_toTime = toTime;

                                            if(!slotToTime.equals(s_toTime))
                                            	bo.insertTimings(con, mainBean, slotToTime, s_toTime, "A",null,null);

                                            index = 0;
                                            isDone = true;

                                    } else {
                                    	bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,null);
                                    }
                                }
                            }
                        }
                    }
                }

                List<BasicDynaBean> appointmentIds = new ArrayList<BasicDynaBean>();
                String fromTimestampStr = dateStr+" "+req.getParameter("firstSlotFromTime");
                String toTimestampStr = dateStr+" "+req.getParameter("firstSlotToTime");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:mm");
                java.util.Date fromdate = (java.util.Date)dateFormat.parse(fromTimestampStr);
                java.util.Date todate = (java.util.Date)dateFormat.parse(toTimestampStr);
                Timestamp fromUnavailableTime = new Timestamp(fromdate.getTime());
                Timestamp toUnavailableTime = new Timestamp(todate.getTime());
                appointmentIds = new ResourceDAO(null).getAppointments(fromUnavailableTime,toUnavailableTime,resourceName,category);

                if(appointmentIds != null && appointmentIds.size() > 0) {
                    for(BasicDynaBean appts : appointmentIds) {
                        if(appts != null) {
                            String appointStatus = (String)appts.get("appointment_status");
                            int appointmnetId = (Integer)appts.get("appointment_id");
                            if (MessageUtil.allowMessageNotification(req,"scheduler_message_send") && category.equals("DOC") && null != appointStatus
                                    && (appointStatus.equalsIgnoreCase("Booked") || appointStatus.equalsIgnoreCase("Confirmed")) && 
                                    !(new ResourceDAO(null).getAppointmentSource(appointmnetId)!=null && new ResourceDAO(null).getAppointmentSource(appointmnetId).equalsIgnoreCase("practo"))) {
                                // Send the message to all the affected appointments
                              MessageManager mgr = new MessageManager();
                              Map resourceUnavailability = new HashMap();
                              resourceUnavailability.put("appointment_id", appointmnetId);
                              resourceUnavailability.put("status", appointStatus);
                              mgr.processEvent("doctor_unavailable", resourceUnavailability);
                              if(appointStatus.equalsIgnoreCase("Confirmed") || appointStatus.equalsIgnoreCase("booked")){
                              	unscheduleAppointmentMsg(appointmnetId);
                              }
                            }
                        }
                    }
                }

                success = true;

            } finally {
                DataBaseUtil.commitClose(con, success);
                if(success && PractoBookHelper.isPractoAdvantageEnabled()) {
            	    PractoBookHelper.addUpdateOverridesToPracto(resourceName, d, d, RequestContext.getCenterId());
    			}
            }
        }

        String includeResources = req.getParameter("includeResources");
        String timeStr = (String)req.getParameter("firstSlotFromTime");

        ActionRedirect redirect =null;
        if (am.getProperty("action_id").endsWith("_week_scheduler"))
            redirect = new ActionRedirect(am.findForward("weekViewRedirect"));
        else
            redirect = new ActionRedirect(am.findForward("dayViewRedirect"));
         redirect.addParameter("category", category);

         FlashScope flash = FlashScope.getScope(req);
         redirect.addParameter("includeResources", includeResources);
         redirect.addParameter("choosenWeekDate", req.getParameter("choosenWeekDate"));
         redirect.addParameter("date", dateStr);
         redirect.addParameter("department", dept);
         redirect.addParameter("centerId", req.getParameter("centerId"));
         if(success){
             flash.put("success", "Doctor Non availability details are updated sucessfully...");
         }else{
             flash.put("error", "Failed to Update doctor non availability details... ");
         }
         redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
         redirect.setAnchor(timeStr);
         return redirect;
    }

    public ActionForward markResourceAvailable(ActionMapping am,
            ActionForm af, HttpServletRequest req, HttpServletResponse res)
    throws Exception {

        String resourceName = req.getParameter("resourceId");
        String dateStr = req.getParameter("date");
        String dept = req.getParameter("department");
        String category = req.getParameter("category");
        Time slotTime = formatTime(req.getParameter("slotTime"));
        int max_center = (Integer)new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default");
        String resourceType = null;
        if (category.equals("DOC")){
            resourceType = category;
        	if(max_center > 1){
        		return markResourceAvailableCenterWise(am,af,req,res);
        	}
        }
        else if(category.equals("DIA"))
            resourceType = "EQID";
        else if(category.equals("SNP"))
            resourceType = "SRID";
        else if(category.equals("OPE"))
            resourceType = "THID";

        Connection con = null;

        Date d = DataBaseUtil.parseDate(dateStr);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        int weekDayNo = (cal.get(Calendar.DAY_OF_WEEK)-1);

        List resourceAvailNonAvailList = null;
        BasicDynaBean nonAvailBean = null;
        BasicDynaBean overrideBean = null;
        BasicDynaBean mainBean = null;

        boolean success = false;

        try {
            con = DataBaseUtil.getConnection();
            con.setAutoCommit(false);
            resourceAvailNonAvailList = new ResourceDAO(null).getResourceAvailabilities(resourceType, d, resourceName,null,null);
            if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() > 0) {
                overrideBean = (BasicDynaBean)resourceAvailNonAvailList.get(0);
            }
            if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() < 1) {
                resourceAvailNonAvailList = new ResourceDAO(null).getResourceDefaultAvailabilities(resourceName, weekDayNo, resourceType,null,null);
            }

            if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() < 1) {
                resourceAvailNonAvailList = new ResourceDAO(null).getResourceDefaultAvailabilities("*", weekDayNo, resourceType,null,null);
            }
            if (overrideBean != null) {
                mainBean = new GenericDAO("sch_resource_availability").getBean();
                int resAvailId = (Integer)overrideBean.get("res_avail_id");
                mainBean.set("res_avail_id", resAvailId);
                if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() > 0) {
                    for (int i=0;i<resourceAvailNonAvailList.size();i++) {
                        nonAvailBean = (BasicDynaBean)resourceAvailNonAvailList.get(i);
                        if (nonAvailBean != null ) {
                            Time fromTime = (Time)nonAvailBean.get("from_time");
                            Time toTime = (Time)nonAvailBean.get("to_time");
                            String availabilityStatus = (String)nonAvailBean.get("availability_status");
                            String remarks = (String)nonAvailBean.get("remarks");
                            if (resourceAvailNonAvailList.size() > 1) {
                                if (fromTime != null && toTime != null && availabilityStatus.equals("N")) {
                                    if(slotTime.equals(fromTime) || slotTime.equals(toTime)
                                        || (slotTime.after(fromTime) && slotTime.before(toTime))) {

                                        new GenericDAO("sch_resource_availability_details").delete(con, "from_time", fromTime, "res_avail_id", resAvailId);
                                        bo.insertTimings(con, mainBean, fromTime, toTime, "A",null,null);
                                    }
                                }
                            } else {
                                new GenericDAO("sch_resource_availability_details").delete(con, "from_time", fromTime, "res_avail_id", resAvailId);
                                bo.insertTimings(con, mainBean, fromTime, toTime, "A",null,null);
                            }
                        }
                    }
                }
            }else {
                int genResAvailId = new GenericDAO("sch_resource_availability").getNextSequence();
                mainBean = new GenericDAO("sch_resource_availability").getBean();
                mainBean.set("res_avail_id",genResAvailId);
                mainBean.set("res_sch_name", resourceName);
                mainBean.set("res_sch_type", resourceType);
                mainBean.set("availability_date", d);
                new GenericDAO("sch_resource_availability").insert(con, mainBean);

                if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() > 0) {
                    for (int i=0;i<resourceAvailNonAvailList.size();i++) {
                        nonAvailBean = (BasicDynaBean)resourceAvailNonAvailList.get(i);
                        if (nonAvailBean != null ) {
                            Time fromTime = (Time)nonAvailBean.get("from_time");
                            Time toTime = (Time)nonAvailBean.get("to_time");
                            String availabilityStatus = (String)nonAvailBean.get("availability_status");
                            String remarks = (String)nonAvailBean.get("remarks");
                            if (resourceAvailNonAvailList.size() > 1) {
                                if (fromTime != null && toTime != null && availabilityStatus.equals("N")) {
                                    if(slotTime.equals(fromTime) || slotTime.equals(toTime)
                                        || (slotTime.after(fromTime) && slotTime.before(toTime))) {

                                    	bo.insertTimings(con, mainBean, fromTime, toTime, "A",null,null);
                                    } else {
                                    	bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,null);
                                    }
                                } else {
                                	bo.insertTimings(con, mainBean, fromTime, toTime, "A",null,null);
                                }
                            } else {
                            	bo.insertTimings(con, mainBean, fromTime, toTime, "A",null,null);
                            }
                        }
                    }
                }
            }
            success = true;
        } finally {
            DataBaseUtil.commitClose(con, success);
            if (success && PractoBookHelper.isPractoAdvantageEnabled()) {
               PractoBookHelper.addUpdateOverridesToPracto(resourceName, d, d, RequestContext.getCenterId());
            }
        }

        String includeResources = req.getParameter("includeResources");
        String timeStr = (String)req.getParameter("slotTime");

        ActionRedirect redirect =null;
        if (am.getProperty("action_id").endsWith("_week_scheduler"))
            redirect = new ActionRedirect(am.findForward("weekViewRedirect"));
        else
            redirect = new ActionRedirect(am.findForward("dayViewRedirect"));
         redirect.addParameter("category", category);

         FlashScope flash = FlashScope.getScope(req);
         redirect.addParameter("includeResources", includeResources);
         redirect.addParameter("choosenWeekDate", req.getParameter("choosenWeekDate"));
         redirect.addParameter("date", dateStr);
         redirect.addParameter("department", dept);
         redirect.addParameter("centerId", req.getParameter("centerId"));
         if(success){
             flash.put("success", "Updated Reource availability details sucessfully...");
         }else{
             flash.put("error", "Failed to update Reource availability details... ");
         }
         redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
         redirect.setAnchor(timeStr);
         return redirect;
    }

    public boolean calculateAndInsertAvailAndNonAvailTimings(Connection con,BasicDynaBean bean,Time fromTime,Time toTime,Time s_fromTime,Time s_toTime,String nonAvailremarks) throws Exception {
        boolean success = false;
        if (fromTime != null && toTime != null) {
            if(s_fromTime.equals(fromTime) &&  s_toTime.equals(toTime)) {
                success = bo.insertTimings(con, bean, fromTime, toTime, "N",nonAvailremarks,null);

            } else if(s_fromTime.equals(fromTime) && toTime.before(s_toTime)){
                success = bo.insertTimings(con, bean, fromTime, toTime, "N",nonAvailremarks,null);

                if (success)
                    success = bo.insertTimings(con, bean, toTime, s_toTime, "A",null,null);

            } else if (s_toTime.equals(toTime) && fromTime.after(s_fromTime)) {
                success = bo.insertTimings(con, bean, s_fromTime, fromTime, "A",null,null);

                if(success)
                    success = bo.insertTimings(con, bean, fromTime, toTime, "N",nonAvailremarks,null);

            } else {
                success = bo.insertTimings(con, bean, s_fromTime, fromTime, "A",null,null);

                if(success)
                    success = bo.insertTimings(con, bean, fromTime, toTime, "N",nonAvailremarks,null);

                if (success)
                    success = bo.insertTimings(con, bean, toTime, s_toTime, "A",null,null);
            }
        }
        return success;
    }


    @IgnoreConfidentialFilters
    public ActionForward getResDowntimeDashboard(ActionMapping am, ActionForm af,
            HttpServletRequest req, HttpServletResponse res) throws SQLException, ParseException
    {
        ResourceDAO dao = resourceDAO;

        PagedList pl = dao.getDownTimeResourceList(req.getParameterMap(), ConversionUtils.getListingParameter(req.getParameterMap()));
        req.setAttribute("pagedList", pl);
        req.setAttribute("resource_type",req.getParameter("resource_type"));
        req.setAttribute("downtime_start", req.getParameter("downtime_start"));
        req.setAttribute("downtime_end", req.getParameter("downtime_end"));
        return am.findForward("DownTimeDashboard");
    }

    @IgnoreConfidentialFilters
    public ActionForward addResourceDowntime(ActionMapping am,
            ActionForm af, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        return am.findForward("ResourceDowntimeScreen");
    }

    @IgnoreConfidentialFilters
    public ActionForward showResourceDowntime(ActionMapping am,
            ActionForm af, HttpServletRequest req, HttpServletResponse res) throws Exception {

        List l=new ResourceDAO(null).getDownTimeDetails(req.getParameter("resource_id"), req.getParameter("downtime_start"), req.getParameter("downtime_end"));

        req.setAttribute("resultlist",l);

        return am.findForward("ResourceDowntimeScreen");

    }

    @IgnoreConfidentialFilters
    public ActionForward getDowntimeResource(ActionMapping am,
            ActionForm af, HttpServletRequest req, HttpServletResponse res)
            throws Exception {

        JSONSerializer js = new JSONSerializer().exclude("class");
        List resourceList = null;
        if (req.getParameter("resource_type") != null) {
            if(req.getParameter("resource_type").equals("THID")) {
                resourceList = new ResourceDAO(null).getResourceMasterList("THID");
            }else if(req.getParameter("resource_type").equals("EQID")) {
                resourceList =   new ResourceDAO(null).getResourceMasterList("EQID");
            }else if(req.getParameter("resource_type").equals("BED")) {
                resourceList = new ResourceDAO(null).getResourceMasterList("BED");
            }
        }
        res.setContentType("application/json");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(js.serialize(resourceList));
        res.flushBuffer();
        return null;
    }

    public ActionForward saveResourceDowntime(ActionMapping am, ActionForm af,
            HttpServletRequest req, HttpServletResponse res) throws Exception {

        SimpleDateFormat timeStampFormatter = new SimpleDateFormat("dd-MM-yyyy");

        Map requestParams = req.getParameterMap();
        Map<String, Object[]> params = new HashMap<String, Object[]>(requestParams);
        List errors = new ArrayList();
        BasicDynaBean bean=null;
        boolean success = false;

        String fromdate = ((Object[]) params.get("downtime_start"))[0].toString();
        String todate = ((Object[]) params.get("downtime_end"))[0].toString();

        params.put("downtime_start", new Object[] { new java.sql.Timestamp(
                timeStampFormatter.parse(fromdate).getTime()) });
        params.put("downtime_end", new Object[] { new java.sql.Timestamp(
                timeStampFormatter.parse(todate).getTime()) });
        Connection con = null;
        try {
            con = DataBaseUtil.getConnection();
            con.setAutoCommit(false);

            GenericDAO dao = new GenericDAO("scheduler_resource_downtime");

            bean = dao.getBean();
            ConversionUtils.copyToDynaBean(params, bean, errors);
            if (errors.isEmpty()) {
                success = dao.insert(con, bean);
                if (success) {
                    FlashScope flash = FlashScope.getScope(req);
                    flash.put("success", "Downtime details inserted successfully..");
                    ActionRedirect redirect = new ActionRedirect(am.findForward("redirectPage"));
                    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                    redirect.addParameter("_method", "showResourceDowntime");
                    redirect.addParameter("resource_type", req.getParameter("resource_type"));
                    redirect.addParameter("resource_id", req.getParameter("resource_id"));
                    redirect.addParameter("downtime_start", bean.get("downtime_start"));
                    redirect.addParameter("downtime_end",bean.get("downtime_end"));
                    redirect.addParameter("reason",req.getParameter("reason"));
                    return redirect;
                } else {
                    req.setAttribute("error","May be there is active entry for the machine");
                }
            } else {
                req.setAttribute("error", "Incorrectly formatted values supplied");
            }
        } finally {
            DataBaseUtil.commitClose(con, success);
        }
        return am.findForward("ResourceDowntimeScreen");
    }

    public ActionForward updateResourceDowntime(ActionMapping am, ActionForm af,
            HttpServletRequest req, HttpServletResponse res) throws Exception {

        String resource_id=req.getParameter("res_id");
        String downtime_start=req.getParameter("time_start");
        String downtime_end=req.getParameter("time_end");
        boolean success=false;
        ActionRedirect redirect= new ActionRedirect(am.findForward("redirectPage"));

        success=new ResourceDAO(null).updateDownTimeDetails(req.getParameter("status"),resource_id ,downtime_start ,downtime_end);
        FlashScope flash = FlashScope.getScope(req);
        if(success)
            flash.put("success", "Downtime details updated successfully..");
        else
            flash.put("error", "Downtime details updation failed..");

        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        redirect.addParameter("_method", "showResourceDowntime");
        redirect.addParameter("resource_id", resource_id);
        redirect.addParameter("downtime_start", downtime_start);
        redirect.addParameter("downtime_end",downtime_end);

        return redirect;
    }
    
    public ActionForward getContactDetailsJSON(ActionMapping mapping,
        ActionForm form, HttpServletRequest request, HttpServletResponse res) throws Exception{
       
      res.setContentType("application/x-json");
      res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
      
      String contactId = request.getParameter("contactId");
      
      Map contactInfo = PatientDetailsDAO.getContactBean(Integer.parseInt(contactId)).getMap();
      JSONSerializer js = new JSONSerializer().exclude("class");
      res.getWriter().write(js.deepSerialize(contactInfo));
      res.flushBuffer();
      return null;
    }

    public ActionForward getPatientDetailsJSON(ActionMapping mapping,
            ActionForm form, HttpServletRequest request, HttpServletResponse res)
            throws IOException, SQLException, ParseException {

        res.setContentType("application/x-json");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

        String mrno = request.getParameter("mrno");
        String patientId = request.getParameter("patient_id");
        Bill bill = null;
        String billNo = null;
        Map pd = null;
        List activeVisits = VisitDetailsDAO.getPatientVisits(mrno, true);

        if (patientId != null && !patientId.equals("") && !patientId.equals("None")) {
            pd = VisitDetailsDAO.getPatientVisitDetailsMap(patientId);
            bill = BillDAO.getLatestOpenBillLaterElseBillNow(patientId);
        } else {
            pd = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
            String latestActiveVisitId = pd.get("visit_id") != null ? (String)pd.get("visit_id") : null;
            if (latestActiveVisitId != null && !latestActiveVisitId.equals("")) {
                pd = VisitDetailsDAO.getPatientVisitDetailsMap(latestActiveVisitId);
                bill = BillDAO.getLatestOpenBillLaterElseBillNow(latestActiveVisitId);
            }
        }

        Map prevApptMap = new ResourceDAO(null).getAppointmentDetailsByMrno(mrno, 0);

        if(bill != null)
            billNo = bill.getBillNo();
        Map billDetailsMap = null;
        if(bill != null)
            billDetailsMap = BillDAO.getBillBean(billNo).getMap();

        Map schPatInfo = new HashMap();
        schPatInfo.put("patientDetails", pd);
        schPatInfo.put("prevApptDetails", prevApptMap);
        schPatInfo.put("billDetails", billDetailsMap);
        schPatInfo.put("activeVisits", ConversionUtils.listBeanToListMap(activeVisits));

        JSONSerializer js = new JSONSerializer().exclude("class");
        res.getWriter().write(js.deepSerialize(schPatInfo));
        res.flushBuffer();

        return null;
    }

    public ActionForward getPatientVisitDetailsJSON(ActionMapping mapping,
            ActionForm form, HttpServletRequest request, HttpServletResponse res)
            throws IOException, SQLException, ParseException {

        res.setContentType("application/x-json");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

        Map appointmentDet = null;
        String mrno = request.getParameter("mrno");
        String appointmentId = request.getParameter("appointment_id");
        if(appointmentId != null && !appointmentId.isEmpty()) {
            BasicDynaBean apptBean = new GenericDAO("scheduler_appointments").findByKey("appointment_id", Integer.parseInt(appointmentId));
            appointmentDet = apptBean != null ? apptBean.getMap() : null;
        }
        Map pd = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
        List activeVisits = VisitDetailsDAO.getPatientVisits(mrno, true);

        HashMap schedulerPatientInfo = new HashMap();
        schedulerPatientInfo.put("activeVisits", ConversionUtils.listBeanToListMap(activeVisits));
        schedulerPatientInfo.put("patientDetails", pd);
        schedulerPatientInfo.put("appointmentDetails", appointmentDet);

        JSONSerializer js = new JSONSerializer().exclude("class");
        res.getWriter().write(js.deepSerialize(schedulerPatientInfo));
        res.flushBuffer();

        return null;
    }

    @IgnoreConfidentialFilters
    public ActionForward getDoctorTimingDashboard(ActionMapping mapping,
            ActionForm form, HttpServletRequest request, HttpServletResponse res)
            throws Exception {

        List<BasicDynaBean> list = new ResourceDAO(null).getSchedulerTimingAvailableDoctors();
        request.setAttribute("Doctorlist", list);
        return mapping.findForward("DoctorTimingDashboard");
    }

     public ActionForward deleteDoctorAvailableTimings(ActionMapping mapping,
            ActionForm form, HttpServletRequest request, HttpServletResponse res)
            throws Exception {

        boolean success = false;
        String msg = "Delete doctor timing failure... ";
        String[] deleteDoctor = request.getParameterValues("deleteDoctor");
        for (String doc : deleteDoctor) {
            success = new GenericDAO("scheduler_doctor_availability").delete(DataBaseUtil.getConnection(), "doctor_id", doc);
        }
        if (success)
            msg = "Deleted doctor timing ... ";

        FlashScope flash = FlashScope.getScope(request);
        flash.put("msg", msg);
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("redirectPage"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        redirect.addParameter("method", "getDoctorTimingDashboard");
        return redirect;
    }

     @IgnoreConfidentialFilters
     public ActionForward getEditDoctorTimingScreen(ActionMapping mapping,
            ActionForm form, HttpServletRequest request, HttpServletResponse res)
            throws Exception {

        request.setAttribute("doctorsJSON", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(new ResourceDAO(null).getSchedulerNotinDashboardDoctors())));

        String doctorId = request.getParameter("doctorId");

        List<BasicDynaBean> timingList = new GenericDAO("scheduler_doctor_availability").findAllByKey("doctor_id", doctorId);
        if (timingList.size() > 0) {
            request.setAttribute("timingList", timingList);
        } else {
            BasicDynaBean bean = new GenericDAO("scheduler_doctor_availability").getBean();
            timingList.add(bean);
            request.setAttribute("timingList", timingList);
        }

        String weekNumber = DataBaseUtil.getStringValueFromDb("SELECT EXTRACT(week from current_date)");
        request.setAttribute("weekNumber", weekNumber);
        List<BasicDynaBean> nonAvailabilityTiming = new ResourceDAO(null).getDoctorNonAvailabilityTiming(doctorId,Integer.parseInt(weekNumber));
        request.setAttribute("nonAvailabilityTiming", nonAvailabilityTiming);

        request.setAttribute("doctorId", doctorId);
        request.setAttribute("doctorName", request.getParameter("doctorName"));
        request.setAttribute("deptName", request.getParameter("deptName"));

        return mapping.findForward("EditDoctorTiming");
    }

     @IgnoreConfidentialFilters
     public ActionForward getDoctorNonAvailabilityTiming(ActionMapping mapping,
                ActionForm form, HttpServletRequest request, HttpServletResponse res)
        throws Exception {

         String doctorId = request.getParameter("docID");
         String weekNo = request.getParameter("weekNo");

         List l = new ResourceDAO(null).getDoctorNonAvailabilityTiming(doctorId,Integer.parseInt(weekNo));
         String responseContent = new JSONSerializer().serialize(ConversionUtils.copyListDynaBeansToMap(l));

         res.setContentType("application/json");
         res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
         res.getWriter().write(responseContent);
         res.flushBuffer();

         return null;
     }

     public ActionForward saveDoctorAvailableTimings(ActionMapping mapping, ActionForm form,
                 HttpServletRequest request, HttpServletResponse res) throws Exception {
         String msg = "Failed to save doctor availability timing...";

         Map fields = new HashMap();
         Map keys = new HashMap();

         List l = new GenericDAO("scheduler_doctor_availability").findAllByKey("doctor_id", request.getParameter("doctor"));

         BasicDynaBean bean = new GenericDAO("scheduler_doctor_availability").getBean();

         bean.set("doctor_id", request.getParameter("doctor"));
         keys.put("doctor_id", request.getParameter("doctor"));

         bean.set("appt_type", "DOC");

         if(!request.getParameter("mon1").equals("HH:MM")) {
             java.sql.Time  mon1 = formatTime(request.getParameter("mon1"));
             bean.set("mon_slot1_from", mon1);
             fields.put("mon_slot1_from", mon1);
         }else {
             fields.put("mon_slot1_from", null);
         }
         if(!request.getParameter("mon2").equals("HH:MM")) {
             java.sql.Time  mon2 = formatTime(request.getParameter("mon2"));
             bean.set("mon_slot1_to", mon2);
             fields.put("mon_slot1_to", mon2);
         }else {
             fields.put("mon_slot1_to", null);
         }
         if(!request.getParameter("mon3").equals("HH:MM")) {
             java.sql.Time  mon3 = formatTime(request.getParameter("mon3"));
             bean.set("mon_slot2_from", mon3);
             fields.put("mon_slot2_from", mon3);
         }else {
             fields.put("mon_slot2_from", null);
         }
         if(!request.getParameter("mon4").equals("HH:MM")) {
             java.sql.Time  mon4 = formatTime(request.getParameter("mon4"));
             bean.set("mon_slot2_to", mon4);
             fields.put("mon_slot2_to", mon4);
         }else {
             fields.put("mon_slot2_to", null);
         }

         if(!request.getParameter("tue1").equals("HH:MM")) {
             java.sql.Time  tue1 = formatTime(request.getParameter("tue1"));
             bean.set("tue_slot1_from", tue1);
             fields.put("tue_slot1_from", tue1);
         }else {
             fields.put("tue_slot1_from", null);
         }
         if(!request.getParameter("tue2").equals("HH:MM")) {
             java.sql.Time  tue2 = formatTime(request.getParameter("tue2"));
             bean.set("tue_slot1_to", tue2);
             fields.put("tue_slot1_to", tue2);
         }else {
             fields.put("tue_slot1_to", null);
         }
         if(!request.getParameter("tue3").equals("HH:MM")) {
             java.sql.Time  tue3 = formatTime(request.getParameter("tue3"));
             bean.set("tue_slot2_from", tue3);
             fields.put("tue_slot2_from", tue3);
         }else {
             fields.put("tue_slot2_from", null);
         }
         if(!request.getParameter("tue4").equals("HH:MM")) {
             java.sql.Time  tue4 = formatTime(request.getParameter("tue4"));
             bean.set("tue_slot2_to", tue4);
             fields.put("tue_slot2_to", tue4);
         }else {
             fields.put("tue_slot2_to", null);
         }

         if(!request.getParameter("wed1").equals("HH:MM")) {
             java.sql.Time  wed1 = formatTime(request.getParameter("wed1"));
             bean.set("wed_slot1_from", wed1);
             fields.put("wed_slot1_from", wed1);
         }else {
             fields.put("wed_slot1_from", null);
         }
         if(!request.getParameter("wed2").equals("HH:MM")) {
             java.sql.Time  wed2 = formatTime(request.getParameter("wed2"));
             bean.set("wed_slot1_to", wed2);
             fields.put("wed_slot1_to", wed2);
         }else {
             fields.put("wed_slot1_to", null);
         }
         if(!request.getParameter("wed3").equals("HH:MM")) {
             java.sql.Time  wed3 = formatTime(request.getParameter("wed3"));
             bean.set("wed_slot2_from", wed3);
             fields.put("wed_slot2_from", wed3);
         }else {
             fields.put("wed_slot2_from", null);
         }
         if(!request.getParameter("wed4").equals("HH:MM")) {
             java.sql.Time  wed4 = formatTime(request.getParameter("wed4"));
             bean.set("wed_slot2_to", wed4);
             fields.put("wed_slot2_to", wed4);
         }else {
             fields.put("wed_slot2_to", null);
         }

         if(!request.getParameter("thu1").equals("HH:MM")) {
             java.sql.Time  thu1 = formatTime(request.getParameter("thu1"));
             bean.set("thu_slot1_from", thu1);
             fields.put("thu_slot1_from", thu1);
         }else {
             fields.put("thu_slot1_from", null);
         }
         if(!request.getParameter("thu2").equals("HH:MM")) {
             java.sql.Time  thu2 = formatTime(request.getParameter("thu2"));
             bean.set("thu_slot1_to", thu2);
             fields.put("thu_slot1_to", thu2);
         }else {
             fields.put("thu_slot1_to", null);
         }
         if(!request.getParameter("thu3").equals("HH:MM")) {
             java.sql.Time  thu3 = formatTime(request.getParameter("thu3"));
             bean.set("thu_slot2_from", thu3);
             fields.put("thu_slot2_from", thu3);
         }else {
             fields.put("thu_slot2_from", null);
         }
         if(!request.getParameter("thu4").equals("HH:MM")) {
             java.sql.Time  thu4 = formatTime(request.getParameter("thu4"));
             bean.set("thu_slot2_to", thu4);
             fields.put("thu_slot2_to", thu4);
         }else {
             fields.put("thu_slot2_to", null);
         }

         if(!request.getParameter("fri1").equals("HH:MM")) {
             java.sql.Time  fri1 = formatTime(request.getParameter("fri1"));
             bean.set("fri_slot1_from", fri1);
             fields.put("fri_slot1_from", fri1);
         }else {
             fields.put("fri_slot1_from", null);
         }
         if(!request.getParameter("fri2").equals("HH:MM")) {
             java.sql.Time  fri2 = formatTime(request.getParameter("fri2"));
             bean.set("fri_slot1_to", fri2);
             fields.put("fri_slot1_to", fri2);
         }else {
             fields.put("fri_slot1_to", null);
         }
         if(!request.getParameter("fri3").equals("HH:MM")) {
             java.sql.Time  fri3 = formatTime(request.getParameter("fri3"));
             bean.set("fri_slot2_from", fri3);
             fields.put("fri_slot2_from", fri3);
         }else {
             fields.put("fri_slot2_from", null);
         }
         if(!request.getParameter("fri4").equals("HH:MM")) {
             java.sql.Time  fri4 = formatTime(request.getParameter("fri4"));
             bean.set("fri_slot2_to", fri4);
             fields.put("fri_slot2_to", fri4);
         }else {
             fields.put("fri_slot2_to", null);
         }

         if(!request.getParameter("sat1").equals("HH:MM")) {
             java.sql.Time  sat1 = formatTime(request.getParameter("sat1"));
             bean.set("sat_slot1_from", sat1);
             fields.put("sat_slot1_from", sat1);
         }else {
             fields.put("sat_slot1_from", null);
         }
         if(!request.getParameter("sat2").equals("HH:MM")) {
             java.sql.Time  sat2 = formatTime(request.getParameter("sat2"));
             bean.set("sat_slot1_to", sat2);
             fields.put("sat_slot1_to", sat2);
         }else {
             fields.put("sat_slot1_to", null);
         }
         if(!request.getParameter("sat3").equals("HH:MM")) {
             java.sql.Time  sat3 = formatTime(request.getParameter("sat3"));
             bean.set("sat_slot2_from", sat3);
             fields.put("sat_slot2_from", sat3);
         }else {
             fields.put("sat_slot2_from", null);
         }
         if(!request.getParameter("sat4").equals("HH:MM")) {
             java.sql.Time  sat4 = formatTime(request.getParameter("sat4"));
             bean.set("sat_slot2_to", sat4);
             fields.put("sat_slot2_to", sat4);
         }else {
             fields.put("sat_slot2_to", null);
         }

         if(!request.getParameter("sun1").equals("HH:MM")) {
             java.sql.Time  sun1 = formatTime(request.getParameter("sun1"));
             bean.set("sun_slot1_from", sun1);
             fields.put("sun_slot1_from", sun1);
         }else {
             fields.put("sun_slot1_from", null);
         }
         if(!request.getParameter("sun2").equals("HH:MM")) {
             java.sql.Time  sun2 = formatTime(request.getParameter("sun2"));
             bean.set("sun_slot1_to", sun2);
             fields.put("sun_slot1_to", sun2);
         }else {
             fields.put("sun_slot1_to", null);
         }
         if(!request.getParameter("sun3").equals("HH:MM")) {
             java.sql.Time  sun3 = formatTime(request.getParameter("sun3"));
             bean.set("sun_slot2_from", sun3);
             fields.put("sun_slot2_from", sun3);
         }else {
             fields.put("sun_slot2_from", null);
         }
         if(!request.getParameter("sun4").equals("HH:MM")) {
             java.sql.Time  sun4 = formatTime(request.getParameter("sun4"));
             bean.set("sun_slot2_to",sun4);
             fields.put("sun_slot2_to", sun4);
         }else {
             fields.put("sun_slot2_to", null);
         }

         if(l.size() > 0) {
             int i = new GenericDAO("scheduler_doctor_availability").update(DataBaseUtil.getConnection(), fields, keys);
             if(i > 0) msg = "Doctor availability timing updated...";
         }
         else {
             boolean success = new GenericDAO("scheduler_doctor_availability").insert(DataBaseUtil.getConnection(), bean);
             if(success) msg = "Doctor availability timing saved...";
         }

         FlashScope flash = FlashScope.getScope(request);
         flash.put("msg",msg);
         ActionRedirect redirect = new ActionRedirect(mapping.findForward("redirectPage"));
         redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
         redirect.addParameter("method","getEditDoctorTimingScreen");
         redirect.addParameter("doctorId", request.getParameter("doctor"));
         redirect.addParameter("doctorName", request.getParameter("doctorName"));
         redirect.addParameter("deptName", request.getParameter("deptName"));
         return redirect;
     }

     private java.sql.Time  formatTime(String param) throws ParseException {

         SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
         if(param != null & !param.equals(""))
             return new java.sql.Time(timeFormatter.parse(param).getTime());
         else
            return null;

     }

     public ActionForward ajaxTimingBetweenDates(ActionMapping mapping, ActionForm form,
                 HttpServletRequest request, HttpServletResponse res) throws Exception {
         String fromdate = request.getParameter("fromdate");
         String todate = request.getParameter("todate");
         String docid = request.getParameter("docid");

         java.sql.Date fdate = new java.sql.Date(DataBaseUtil.dateFormatter.parse(fromdate).getTime());
         java.sql.Date tdate = new java.sql.Date(DataBaseUtil.dateFormatter.parse(todate).getTime());

         List l = new ResourceDAO(null).getTimingBetweenDates(docid,fdate,tdate);
         String responseContent = new JSONSerializer().serialize(ConversionUtils.copyListDynaBeansToMap(l));

         res.setContentType("application/json");
         res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
         res.getWriter().write(responseContent);
         res.flushBuffer();
         return null;
     }

     public ActionForward updateVisitAndPatientMrno(ActionMapping mapping, ActionForm form,
                 HttpServletRequest request, HttpServletResponse res) throws Exception {
         String mrno = request.getParameter("mrno");
         String visitId = request.getParameter("patient_id");
         String appointmentId = request.getParameter("appointment_id");
         String responseContent = null;
         int apptId = Integer.parseInt(appointmentId);
         if(ResourceBO.updatePatientMrnoAndVisit(apptId, mrno, visitId)) {
             responseContent = "Updated";
             if(PractoBookHelper.isPractoAdvantageEnabled()) {
            	 PractoBookHelper.addDoctorAppointmentsToPracto(apptId, false);
             }
         }
         new ResourceDAO(null).flushContact(mrno, Integer.parseInt(appointmentId));
         res.setContentType("application/json");
         res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
         res.getWriter().write(responseContent);
         res.flushBuffer();
         return null;
     }

     public ActionForward updateAppointmentCancelReason(ActionMapping mapping, ActionForm form,
                 HttpServletRequest request, HttpServletResponse res) throws Exception {
         String appointmentId = request.getParameter("appointment_id");
         String userName = (String)request.getSession(false).getAttribute("userid");
         boolean success = true;
         String[] appointmentIds = null;
         if (appointmentId.contains(",")) {
             String [] arrayElements = appointmentId.split(",");
             appointmentIds = new String[arrayElements.length];
             appointmentIds = arrayElements;
         } else {
            appointmentIds = new String[1];
            appointmentIds[0] = appointmentId;
         }
         String responseContent = null;
         Connection con = null;
         String appointmentStatus = request.getParameter("appointment_status");
         String cancelReason = request.getParameter("cancel_reason");
         try{
        	 con = DataBaseUtil.getConnection();
             con.setAutoCommit(false);
             if (appointmentId != null) {
                 for(int i=0; i<appointmentIds.length;i++) {
                     BasicDynaBean bean = new GenericDAO("scheduler_appointments").findByKey("appointment_id", Integer.parseInt(appointmentIds[i]));
      			     int unique_appt_ind = (Integer)bean.get("unique_appt_ind");
    			     if(unique_appt_ind == 0) {
    				     unique_appt_ind = new ResourceDAO(null).getNextUniqueAppointMentInd();
    			     }
    			     java.sql.Timestamp modTime = DataBaseUtil.getDateandTime();
                     success = ResourceBO.updateAppointments(con, Integer.parseInt(appointmentIds[i]), appointmentStatus,cancelReason,userName, unique_appt_ind, modTime);
                     if (!success) {
                         break;
                     }
                 }
             }

         } finally {
             DataBaseUtil.commitClose(con, success);
         }
         if (appointmentId != null && success) {
        	 if (null != appointmentStatus && appointmentStatus.equalsIgnoreCase("Cancel")) {
    			Map<String,Object> jobData = new HashMap<String, Object>();
 				jobData.put("schema", RequestContext.getSchema());
 				jobData.put("eventId", "appointment_cancelled");
 				jobData.put("eventData", appointmentIds);
 				jobData.put("userName", userName);
 				jobData.put("newStatus", appointmentStatus);
 				jobService.scheduleImmediate(buildJob("AppointmentStatusChangeSMSJob_"+appointmentStatus+""+appointmentIds,
 						AppointmentStatusChangeSMSJob.class, jobData));
               }
        	 schedulePushEvent(appointmentIds, Events.APPOINTMENT_CANCEL);
        	 for(int i=0; i<appointmentIds.length;i++) {
				if (PractoBookHelper.isPractoAdvantageEnabled()) {
					PractoBookHelper.addDoctorAppointmentsToPracto(
							Integer.parseInt(appointmentIds[i]), false);
				}
        	 }
         }
         if (success) {
             responseContent = "Updated";
         }
         res.setContentType("application/json");
         res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
         res.getWriter().write(responseContent);
         res.flushBuffer();
         return null;
     }

     @IgnoreConfidentialFilters
     public ActionForward channelingApptsSelected(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse res) throws Exception {
     String appointmentId = request.getParameter("appointment_id");
     String userName = (String)request.getSession(false).getAttribute("userid");
     boolean success = true;
     String[] appointmentIds = null;
     if (appointmentId.contains(",")) {
         String [] arrayElements = appointmentId.split(",");
         appointmentIds = new String[arrayElements.length];
         appointmentIds = arrayElements;
     } else {
        appointmentIds = new String[1];
        appointmentIds[0] = appointmentId;
     }
     res.setContentType("application/json");
     res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
     String responseContent = null;
     responseContent = "NoChannel";
     success = true;
     res.getWriter().write(responseContent);
     res.flushBuffer();
     return null;
 }

     public ActionForward updateAppointmentStatus(ActionMapping mapping, ActionForm form,
                 HttpServletRequest request, HttpServletResponse res) throws Exception {
         String appointmentId = request.getParameter("appointment_id");
         String userName = (String)request.getSession(false).getAttribute("userid");
         boolean success = true;
         String[] appointmentIds = null;
         String apptStatus = null;
         Connection con = null;
         if (appointmentId.contains(",")) {
             String [] arrayElements = appointmentId.split(",");
             appointmentIds = new String[arrayElements.length];
             appointmentIds = arrayElements;
         } else {
            appointmentIds = new String[1];
            appointmentIds[0] = appointmentId;
         }
         String appointmentStatus = request.getParameter("appointment_status");
         String responseContent = null;
         try{
             con = DataBaseUtil.getConnection();
             con.setAutoCommit(false);
             if (appointmentStatus != null && appointmentStatus.equalsIgnoreCase("noshow")) {
                 if (appointmentId != null) {
                     for(int i=0; i<appointmentIds.length;i++) {
                         apptStatus = new ResourceDAO(null).getAppointmentStatus(con,Integer.parseInt(appointmentIds[i]));
                         if (apptStatus != null && apptStatus.equalsIgnoreCase("arrived")) {
                             responseContent = "arrived";
                             success = false;
                             break;
                         }
                         GenericDAO schedulerApptDAO = new GenericDAO("scheduler_appointments");
                         BasicDynaBean schedulerApptBean = schedulerApptDAO.findByKey(con, "appointment_id", Integer.parseInt(appointmentIds[i]));
          			     int unique_appt_ind = (Integer)schedulerApptBean.get("unique_appt_ind");
        			     if(unique_appt_ind == 0) {
        				     unique_appt_ind = new ResourceDAO(null).getNextUniqueAppointMentInd();
        			     }
        			     java.sql.Timestamp modTime = DataBaseUtil.getDateandTime();
                         success = ResourceBO.updateAppointments(con, Integer.parseInt(appointmentIds[i]), appointmentStatus,null,userName, unique_appt_ind, modTime);
                         if (!success) {
                             break;
                         }
                     }
                 }
             }
         } finally {
             DataBaseUtil.commitClose(con, success);
         }
         if (success) {
             responseContent = "Updated";
			if (PractoBookHelper.isPractoAdvantageEnabled()) {
				for (String apppointmentId: appointmentIds) {
					PractoBookHelper.addDoctorAppointmentsToPracto(Integer.parseInt(appointmentId), false);
				}
			}
			schedulePushEvent(appointmentIds, Events.APPOINTMENT_NOSHOW);
         }
         res.setContentType("application/json");
         res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
         res.getWriter().write(responseContent);
         res.flushBuffer();
         return null;
     }

     public ActionForward confirmAppointments(ActionMapping mapping,
                ActionForm form, HttpServletRequest req, HttpServletResponse res)
                throws Exception {
         String appointmentId = req.getParameter("appointment_id");
         String userName = (String)req.getSession(false).getAttribute("userid");
         String apptStatus = null;
         boolean success = true;
         String[] appointmentIds = null;
         Connection con = null;
         if (appointmentId.contains(",")) {
             String [] arrayElements = appointmentId.split(",");
             appointmentIds = new String[arrayElements.length];
             appointmentIds = arrayElements;
         } else {
            appointmentIds = new String[1];
            appointmentIds[0] = appointmentId;
         }
         String appointmentStatus = req.getParameter("appointment_status");
         String responseContent = null;
         try{
             con = DataBaseUtil.getConnection();
             con.setAutoCommit(false);
             if (appointmentId != null) {
                 for(int i=0; i<appointmentIds.length;i++) {
                     apptStatus = new ResourceDAO(null).getAppointmentStatus(con,Integer.parseInt(appointmentIds[i]));
                     if (apptStatus != null && apptStatus.equalsIgnoreCase("confirmed")) {
                         responseContent = "alreadyConfirmed";
                         success = false;
                         break;
                     } else if (apptStatus != null && apptStatus.equalsIgnoreCase("arrived")) {
                         responseContent = "arrived";
                         success = false;
                         break;
                      } else {
                         GenericDAO schedulerApptDAO = new GenericDAO("scheduler_appointments");
                         BasicDynaBean schedulerApptBean = schedulerApptDAO.findByKey(con, "appointment_id", Integer.parseInt(appointmentIds[i]));
           			     int unique_appt_ind = (Integer)schedulerApptBean.get("unique_appt_ind");
           			     java.sql.Timestamp modTime = DataBaseUtil.getDateandTime();
                         success = ResourceBO.updateAppointments(con, Integer.parseInt(appointmentIds[i]), appointmentStatus,null,userName, unique_appt_ind, modTime);
                         if (!success) {
                            break;
                        }
                    }
                }
            }
        } finally {
            DataBaseUtil.commitClose(con, success);
        }
        if (success) {
            responseContent = "Updated";
            if (appointmentId != null) {
                boolean isPractoAdvantageEnabled = PractoBookHelper.isPractoAdvantageEnabled();
                Map<String,Object> jobData = new HashMap<String, Object>();
				jobData.put("schema", RequestContext.getSchema());
				jobData.put("eventId", "appointment_confirmed");
				jobData.put("eventData", appointmentIds);
				jobData.put("userName", userName);
				jobData.put("newStatus", appointmentStatus);
				if (null != appointmentStatus && appointmentStatus.equalsIgnoreCase("confirmed")){
					jobService.scheduleImmediate(buildJob("AppointmentStatusChangeSMSJob_"+appointmentStatus+""+appointmentIds,
							AppointmentStatusChangeSMSJob.class, jobData));
					
					schedulePushEvent(appointmentIds, Events.APPOINTMENT_CONFIRMED);
					
					//sms doctor appointment confirmation
					jobData.put("eventId", "doc_appt_confirmed");
					jobService.scheduleImmediate(buildJob("AppointmentStatusChangeSMSJob_doc_appt_confirmed"+appointmentIds,
							AppointmentStatusChangeSMSJob.class, jobData));
				}
                for (String appointmentIdStr : appointmentIds) {
                    int appointmentIdInt = Integer.parseInt(appointmentIdStr);
                    // update the appointment to Practo
                    if (isPractoAdvantageEnabled) {
                        PractoBookHelper.addDoctorAppointmentsToPracto(appointmentIdInt, false);
                    }
                }
            }
        }
        res.setContentType("application/json");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(responseContent);
        res.flushBuffer();
        return null;
    }
     
     @IgnoreConfidentialFilters
     public ActionForward isRatePlanApplicable(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse res) throws Exception {
         String category = request.getParameter("category");
         String orgId = request.getParameter("orgId");
         String scheduleId = request.getParameter("schedule_id");
         String responseContent = "";
         if (scheduleId != null && !scheduleId.equals(""))
             responseContent = new ResourceDAO(null).isRatePlanApplicable(category, orgId, scheduleId);
         res.setContentType("application/json");
         res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
         res.getWriter().write(responseContent);
         res.flushBuffer();
         return null;
     }

     @IgnoreConfidentialFilters
     public ActionForward isSlotBooked(ActionMapping mapping, ActionForm form,
                 HttpServletRequest request, HttpServletResponse res) throws Exception {
         String startAppointmentTimeStr = request.getParameter("startTime");
         String endAppointmentTimeStr = request.getParameter("endTime");
         String schName = request.getParameter("schName");
         String appointmentId = request.getParameter("appointmentId");
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
         String primaryResource = request.getParameter("primary_resource");
         String primaryResourceType = request.getParameter("primary_resource_type");
         java.util.Date startDate = (java.util.Date)dateFormat.parse(startAppointmentTimeStr);
         java.util.Date endDate = (java.util.Date)dateFormat.parse(endAppointmentTimeStr);
         Timestamp startAppointmentTime = new java.sql.Timestamp(startDate.getTime());
         Timestamp endAppointmentTime = new java.sql.Timestamp(endDate.getTime());
         BasicDynaBean bean = null;
         String responseContent = "false";
         List<BasicDynaBean> appsList =
             new ResourceDAO(null).isSlotBooked(startAppointmentTime,endAppointmentTime,schName,appointmentId,primaryResource,primaryResourceType);
         bean = (appsList != null && appsList.size() >0) ? appsList.get(0) : null;

         if (bean != null) {
             responseContent = "true";
         }
         res.setContentType("application/json");
         res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
         res.getWriter().write(responseContent);
         res.flushBuffer();
         return null;
     }

     @IgnoreConfidentialFilters
     public ActionForward isResourceBooked(ActionMapping mapping, ActionForm form,
                 HttpServletRequest request, HttpServletResponse res) throws Exception {
         JSONSerializer js = new JSONSerializer().exclude("class");
         String startAppointmentTimeStr = request.getParameter("startTime");
         String endAppointmentTimeStr = request.getParameter("endTime");
         String resourceIds = request.getParameter("resource_ids");
         String resourceTypes = request.getParameter("resource_types");
         String [] resources = null;
         String [] resourcesType = null;
         String appointmentId = request.getParameter("appointment_id");
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
         java.util.Date startDate = (java.util.Date)dateFormat.parse(startAppointmentTimeStr);
         java.util.Date endDate = (java.util.Date)dateFormat.parse(endAppointmentTimeStr);
         Timestamp startAppointmentTime = new java.sql.Timestamp(startDate.getTime());
         Timestamp endAppointmentTime = new java.sql.Timestamp(endDate.getTime());
         int duration = Integer.parseInt(request.getParameter("duration"));
         String category = request.getParameter("category");
         String resourceName = null;
         boolean flag = false;
         int apptId = -1;
         String responseContent = "";
         Map<String,String> appointmentMap = new HashMap<String,String>();
         if (resourceIds != null && resourceIds.contains(",")) {
             String [] resArr = resourceIds.split(",");
             resources = new String[resArr.length];
             resources = resArr;

         } else {
             resources = new String[1];
             resources[0] = resourceIds;
         }

         if (resourceTypes != null && resourceTypes.contains(",")) {
             String [] resArr = resourceTypes.split(",");
             resourcesType = new String[resArr.length];
             resourcesType = resArr;

         } else {
             resourcesType = new String[1];
             resourcesType[0] = resourceTypes;
         }

         if (appointmentId != null && !appointmentId.equals("")) {
             apptId = Integer.parseInt(appointmentId);
         }
         
         String alreadyBooked = getResources(request).getMessage("js.scheduler.todaysappointment.alreadybooked");
         String overBookCountReached = getResources(request).getMessage("js.scheduler.todaysappointment.overBookCountReached");
         String overBookNotAllowed = getResources(request).getMessage("js.scheduler.overBookAppt.not.allowed");
         if (resources != null) {
             for (int i=0;i<resources.length;i++) {
                 if (resources[i] != null && !resources[i].equals("")) {
                 if (!new ResourceDAO(null).isOverBookingAllowedForTheUser(resources[i],category,startAppointmentTime)) {
                     responseContent = responseContent + overBookNotAllowed;
                 } else {
                     BasicDynaBean firstBean = null;
                    
                     firstBean = new ResourceDAO(null).isResourceBooked(startAppointmentTime,endAppointmentTime,resources[i],resourcesType[i],apptId,category);
                     if (firstBean != null) {
                    	 Integer overbook_limit = (Integer) firstBean.get("overbook_limit");
                         String msg = overbook_limit == 0 ? alreadyBooked : overBookCountReached;
                         String resourceId = (String) firstBean.get("resource_id");
                         if (resourcesType != null && !resourcesType[i].equals("")) {
                             resourceName = new ResourceDAO(null).getResourceName(resourcesType[i],resourceId);
                             if (responseContent.equals("")){
                                 responseContent =responseContent+(resourceName == null ? "" : (resourceName +": "+ msg));
                             }
                             else
                                 if (resourceName != null) {
                                	 responseContent =responseContent + (resourceName == null ? "" : (resourceName +": "+ msg));
                                 }
                         } else{
                             resourceName = new ResourceDAO(null).getResourceName(resourcesType[i],resourceId);
                             if (responseContent.equals(""))
                                 responseContent =responseContent+(resourceName == null ? "" : (resourceName +": "+ msg));
                             else
                                 if (resourceName != null) {
                                     responseContent =responseContent+(resourceName == null ? "" : (resourceName +": "+  msg));
                                 }
                         }   
                     }
                 }
                 }
             }
         }
         res.setContentType("application/json");
         res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
         res.getWriter().write(responseContent);
         res.flushBuffer();
         return null;
     }

     @IgnoreConfidentialFilters
     public ActionForward isResourceUnavailableWithinGivenTime(ActionMapping mapping, ActionForm form,
                 HttpServletRequest request, HttpServletResponse res) throws Exception {

         String startAppointmentTimeStr = request.getParameter("startTime");
         String endAppointmentTimeStr = request.getParameter("endTime");
         String resourceIds = request.getParameter("resource_ids");
         String resourceCenterId = request.getParameter("schedulableCenterId");
         String [] resources = null;
         String [] resourcesType = null;
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
         java.util.Date startDate = (java.util.Date)dateFormat.parse(startAppointmentTimeStr);
         java.util.Date endDate = (java.util.Date)dateFormat.parse(endAppointmentTimeStr);
         Timestamp startAppointmentTime = new java.sql.Timestamp(startDate.getTime());
         Timestamp endAppointmentTime = new java.sql.Timestamp(endDate.getTime());
         String colDate = request.getParameter("colDate");
         String resourceTypes = request.getParameter("resourceTypes");
         Timestamp startAvailTime = null;
         Timestamp endAvailTime = null;
         String resourceName = null;
         Date availDate = DataBaseUtil.parseDate(colDate);
         Calendar cal = Calendar.getInstance();
         cal.setTime(availDate);
         int dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK)-1);
         String resType = null;
         String responseContent = "";
         String category = request.getParameter("category");

         if (resourceIds != null && resourceIds.contains(",")) {
             String [] resArr = resourceIds.split(",");
             resources = new String[resArr.length];
             resources = resArr;

         } else {
             resources = new String[1];
             resources[0] = resourceIds;
         }

         if (resourceTypes != null && resourceTypes.contains(",")) {
             String [] resArr = resourceTypes.split(",");
             resourcesType = new String[resArr.length];
             resourcesType = resArr;

         } else {
             resourcesType = new String[1];
             resourcesType[0] = resourceTypes;
         }
         int max_center = (Integer)new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default");


         if (resources != null) {
             for (int i=0;i<resources.length;i++) {
                 if (resources[i] != null && !resources[i].equals("")) {
                     List resourceAvailList = new ArrayList();
                     if(resourceTypes!= null && resourcesType[i].equals("OPDOC") || resourcesType[i].equals("SUDOC") ||
                             resourcesType[i].equals("ANEDOC") || resourcesType[i].equals("LABTECH") || resourcesType[i].equals("DOC")) {
                         resType = "DOC";
                     } else {
                         resType = resourcesType[i];
                     }
                     //pass center id only for doctor
                     Integer centerId = null;
                     if(max_center>1)
                    	 centerId = (resType.equals("DOC")) ? Integer.parseInt(resourceCenterId) : null;
                    	 else
                    		 centerId = null;

                     resourceAvailList = new ResourceDAO(null).getResourceAvailabilities(resType, availDate, resources[i], null,centerId);
                     if (resourceAvailList != null && resourceAvailList.size() < 1) {
                         resourceAvailList = new ResourceDAO(null).getResourceDefaultAvailabilities(resources[i], dayOfWeek, resType, null,centerId);
                     }
                     if (resourceAvailList != null && resourceAvailList.size() < 1) {
                         resourceAvailList = new ResourceDAO(null).getResourceDefaultAvailabilities("*", dayOfWeek, resType, null,centerId);
                     }

                     BasicDynaBean resourceBaen = null;
                     resourceName = new ResourceDAO(null).getResourceName(resourcesType[i],resources[i]);

                     for(int j=0;j<resourceAvailList.size();j++) {
                         resourceBaen = (BasicDynaBean)resourceAvailList.get(j);
                         if (resourceBaen.get("availability_status").equals("N")) {
                             Time fromTime = (java.sql.Time)resourceBaen.get("from_time");
                             SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                             String from_time = sdf.format(fromTime);
                             DateUtil dateUtil = new DateUtil();
                             startAvailTime = new Timestamp(dateUtil.getTimeStampFormatterSecs().parse(colDate + " " + from_time).getTime());
                             Time toTime = (java.sql.Time)resourceBaen.get("to_time");
                             String to_time = sdf.format(toTime);
                             endAvailTime = new Timestamp(dateUtil.getTimeStampFormatter().parse(colDate + " " + to_time).getTime());

                             if ((startAppointmentTime.getTime() <= startAvailTime.getTime() && endAppointmentTime.getTime() > startAvailTime.getTime())
                                        || (startAppointmentTime.getTime() >= startAvailTime.getTime() && startAppointmentTime.getTime() < endAvailTime.getTime())){

                                 if (responseContent.equals(""))
                                     responseContent =responseContent+(resourceName == null ? "" : resourceName);
                                 else
                                     responseContent =(resourceName!=null && !resourceName.equals("")) ? (responseContent+","+resourceName) : responseContent;

                                 break;
                             }
                         }
                     }
                 }
             }
         }

         res.setContentType("application/json");
         res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
         res.getWriter().write(responseContent);
         res.flushBuffer();

         return null;
     }

     public ActionForward updateAppointmentStatusAsCompleted(ActionMapping mapping, ActionForm form,
                 HttpServletRequest request, HttpServletResponse res) throws Exception {

         String appointmentId = request.getParameter("appointment_id");
         String appointmentStatus = request.getParameter("appointment_status");
         String category = request.getParameter("category");
         String screenmethod = request.getParameter("screenmethod");
         String includeresources = request.getParameter("includeResources");
         String date = request.getParameter("date");
         String dept = request.getParameter("department");
         FlashScope flash = FlashScope.getScope(request);
         String userName = (String)(request.getSession(false).getAttribute("userid"));
         String message = null;
         BasicDynaBean appBean = new GenericDAO("scheduler_appointments").findByKey("appointment_id", Integer.parseInt(appointmentId));
         boolean success = false;
         ActionRedirect redirect = null;
         Connection con = DataBaseUtil.getConnection();
         try {
              /*if (appBean != null && appBean.get("res_sch_name") != null && !appBean.get("res_sch_name").equals("")) {
                  if (ResourceBO.isAppointmentCompleted(con, category, Integer.parseInt(appointmentId))) {
                     success = new ResourceDAO(con).updateStatus(ResourceDTO.APPT_COMPLETED_STATUS, Integer.parseInt(appointmentId),userName);
                 } else {
                     message = "Appointment is not completed yet.";
                     flash.put("error", message);
                     success = true;
                 }
              } else {
                 success = new ResourceDAO(con).updateStatus(ResourceDTO.APPT_COMPLETED_STATUS, Integer.parseInt(appointmentId),userName);
              }*/

             success = new ResourceDAO(con).updateStatus(ResourceDTO.APPT_COMPLETED_STATUS, Integer.parseInt(appointmentId),userName);
         } finally {
             DataBaseUtil.closeConnections(con, null);
        }
         if (success) {
             if( screenmethod.equals("getScheduleDetails")) {
                 if (category.equals("DOC") ) {
                     redirect = new ActionRedirect(mapping.findForward("docSchedulerDetails"));
                     redirect.addParameter("includeResources", includeresources);
                     redirect.addParameter("date", date);
                     redirect.addParameter("department", dept);
                     redirect.addParameter("category", category);
                     redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                 } else if (category.equals("OPE")) {
                     redirect = new ActionRedirect(mapping.findForward("opeSchedulerDetails"));
                     redirect.addParameter("includeResources", includeresources);
                     redirect.addParameter("date", date);
                     redirect.addParameter("department", dept);
                     redirect.addParameter("category", category);
                     redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                 } else if (category.equals("SNP")) {
                     redirect = new ActionRedirect(mapping.findForward("snpSchedulerDetails"));
                     redirect.addParameter("includeResources", includeresources);
                     redirect.addParameter("date", date);
                     redirect.addParameter("department", dept);
                     redirect.addParameter("category", category);
                     redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                 } else if (category.equals("DIA")) {
                     redirect = new ActionRedirect(mapping.findForward("diaSchedulerDetails"));
                     redirect.addParameter("includeResources", includeresources);
                     redirect.addParameter("date", date);
                     redirect.addParameter("department", dept);
                     redirect.addParameter("category", category);
                     redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                 }
             } else {
            	 if(category.equals("DOC")) {
 					redirect = new ActionRedirect(mapping
 							.findForward("weekViewDetails"));
 					redirect.addParameter("includeResources", includeresources);
 					redirect.addParameter("date", date);
 					redirect.addParameter("department", dept);
 					redirect.addParameter("category", category);
 					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
 				} else if (category.equals("OPE")) {
 					redirect = new ActionRedirect(mapping
 							.findForward("opeWeekViewDetails"));
 					redirect.addParameter("includeResources", includeresources);
 					redirect.addParameter("date", date);
 					redirect.addParameter("department", dept);
 					redirect.addParameter("category", category);
 					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
 				}

             }
           //push event
             schedulePushEvent(String.valueOf(appointmentId),"APPOINTMENT_COMPLETED");
         }
        return  redirect;
     }

     public ActionForward getDuplicateAppDetails(ActionMapping mapping,
             ActionForm form, HttpServletRequest request, HttpServletResponse res) throws Exception {

        String patientId = request.getParameter("patient_id");
        BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", patientId);
        List docConsList = new OrderDAO().consultationsEncounterWise((String)visitBean.get("main_visit_id"));

        String responseContent = new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(docConsList));
        res.setContentType("application/json");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(responseContent);
        res.flushBuffer();

        return null;
     }
     
     @IgnoreConfidentialFilters
     public ActionForward getRescheduleAppDetails(ActionMapping mapping,
             ActionForm form, HttpServletRequest request, HttpServletResponse res) throws Exception {

        String appointmentId = request.getParameter("appointment_id");
        List<BasicDynaBean> appointmentList = new ResourceDAO(null).getRescheduleAppDetails(Integer.parseInt(appointmentId));

        String responseContent = new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(appointmentList));
        res.setContentType("application/json");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(responseContent);
        res.flushBuffer();

        return null;
     }

     public ActionForward editResourceTimings(ActionMapping mapping,
             ActionForm form, HttpServletRequest request, HttpServletResponse res) throws Exception {

        FlashScope flash = FlashScope.getScope(request);
        ActionRedirect redirect = null;

        if(request.getHeader("Referer") != null)
            redirect = new ActionRedirect(request.getHeader("Referer").
                    replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));

        String resourceName = request.getParameter("res_sch_name");
        String category = request.getParameter("res_sch_type");
        String resourceType = null;
        if (category.equals("DOC"))
            resourceType = category;
        else if(category.equals("DIA"))
            resourceType = "EQID";
        else if(category.equals("SNP"))
            resourceType = "SRID";
        else if(category.equals("OPE"))
            resourceType = "THID";
        String screenName = request.getParameter("_screenName");
        String colDate = request.getParameter("_col_date");
        BasicDynaBean bean = null;
        if (colDate != null && !colDate.equals("")) {
            java.sql.Date date = DateUtil.parseDate(colDate);
            bean = new ResourceAvailabilityDAO().getResourceByAvailDate(resourceType,resourceName, date);
        }
        if (bean != null) {
            int availId = (Integer)bean.get("res_avail_id");
            redirect = new ActionRedirect(mapping.findForward("showRedirect"));
            redirect.addParameter("res_avail_id", availId);
        } else {
            redirect = new ActionRedirect(mapping.findForward("listRedirect"));
        }
        redirect.addParameter("_screen_name", screenName);
        redirect.addParameter("res_sch_name", resourceName);
        redirect.addParameter("res_sch_type", resourceType);
        redirect.addParameter("_referer", request.getHeader("Referer"));

        return redirect;
     }

     @IgnoreConfidentialFilters
     public ActionForward getAvlNonAvlTimingList(ActionMapping mapping,
             ActionForm form, HttpServletRequest request, HttpServletResponse res) throws Exception {

         String scheduleName = request.getParameter("scheduleName");
         String category = request.getParameter("category");
         String schedulerType = null;
         if (category != null) {
             if (category.equals("DOC")) {
                 schedulerType = "DOC";
             } else if (category.equals("SNP")) {
                 schedulerType = "SRID";
             } else if (category.equals("DIA")) {
                 schedulerType = "EQID";
             } else if (category.equals("OPE")) {
                 schedulerType = "THID";
             }
         }
         String resourceCenterId = request.getParameter("resourceCenterId");
         String schedulerDate = request.getParameter("scheduleDate");
         Date d = DataBaseUtil.parseDate(schedulerDate);
         Calendar cal = Calendar.getInstance();
         cal.setTime(d);
         //added resource center id only for doctor category
         Integer centerId;
         int max_center = (Integer)new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default");
         if(max_center >1 && category.equals("DOC"))
           centerId = schedulerType.equals("DOC") ? Integer.parseInt(resourceCenterId) : null;
          else
        	  centerId= null;

         int dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK)-1);

         List timingList = new ResourceDAO(null).getResourceAvailabilities(schedulerType, d,scheduleName,null,centerId);

         if (timingList != null && timingList.size()< 1) {
             timingList = new ResourceDAO(null).getResourceDefaultAvailabilities(scheduleName, dayOfWeek, schedulerType,null,centerId);
         }

         if (timingList != null && timingList.size()< 1) {
             timingList = new ResourceDAO(null).getResourceDefaultAvailabilities("*", dayOfWeek, schedulerType,null,centerId);
         }

         String responseContent = new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(timingList));
         res.setContentType("application/json");
         res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
         res.getWriter().write(responseContent);
         res.flushBuffer();

         return null;

     }

     @IgnoreConfidentialFilters
     public ActionForward getConsultationTypesForScheduler(ActionMapping mapping, ActionForm form,
                HttpServletRequest request, HttpServletResponse response)throws IOException, SQLException{

             String orgId = request.getParameter("orgId");
            Integer centerId = Integer.parseInt(request.getParameter("centerId"));
            String patientType = request.getParameter("patientType");

            String healthAuthorityForCenter = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
            String healthAuthority = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthorityForCenter).getHealth_authority();
            JSONSerializer js = new JSONSerializer();
            response.setContentType("text/plain");
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
            response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(
                    ConsultationTypesDAO.getConsultationTypes(patientType, orgId,healthAuthority))));
            response.flushBuffer();

            return null;

    }
     /*
      * This method is use to get existing appointment record of a patient.
      * This ajax call use in javascript to avoid multiple appointments scenario.
      */
     public ActionForward getAppointmentDetailsForDuplication(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)throws IOException, SQLException ,Exception{

			response.setContentType("application/x-json");
			response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
			Map patAppInfo =  new HashMap();
			String mrno = request.getParameter("mrno");
			String patientName = request.getParameter("patName");
			String mobileNo = request.getParameter("mobNo");
			String category = request.getParameter("category");
			String appointmentId = request.getParameter("appointment_id");
			String contactIdStr = request.getParameter("contactId");
			Integer contactId = (contactIdStr != null 
			    && !(contactIdStr.equals("")) 
			    && !(contactIdStr.equals("null")))
			    ? Integer.parseInt(request.getParameter("contactId")) : null;
			String res_sch_name = null;
			String prim_res_id = null;
			Integer res_sch_id = null;
			String resourceName = null;

			String startAppointmentTimeStr = request.getParameter("startTime");
			String endAppointmentTimeStr = request.getParameter("endTime");
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			java.util.Date startDate = (java.util.Date) dateFormat.parse(startAppointmentTimeStr);
			java.util.Date endDate = (java.util.Date) dateFormat.parse(endAppointmentTimeStr);
			Timestamp startAppointmentTime = new java.sql.Timestamp(startDate.getTime());
			Timestamp endAppointmentTime = new java.sql.Timestamp(endDate.getTime());
			BasicDynaBean bean = null;
			String responseContent = "false";
			int apptId = -1;
			Map appInfo = null;

			  if (appointmentId != null && !appointmentId.equals("")) {
		             apptId = Integer.parseInt(appointmentId);
		         }
            List<BasicDynaBean> appsList = new ResourceDAO(null).IsExitsAppointment(startAppointmentTime, endAppointmentTime, apptId, mrno, patientName, mobileNo, contactId);
            bean = (appsList != null && appsList.size() >0) ? appsList.get(0) : null;
            if(bean != null) {
            	appInfo = new HashMap(bean.getMap());
              res_sch_id = (Integer)bean.get("res_sch_id");
            	if (res_sch_id == 1) {
                prim_res_id = (String)bean.get("prim_res_id");
                resourceName = new ResourceDAO(null).getResourceName(res_sch_id,prim_res_id);
            	} else {
            	  res_sch_name = (String)bean.get("res_sch_name");
                resourceName = new ResourceDAO(null).getResourceName(res_sch_id,res_sch_name);
            	}
            	appInfo.put("resource", resourceName);
            }

            if (bean != null) {
                responseContent = "true";
                appInfo.put("responseContent", responseContent);
                patAppInfo.put("appInfo", appInfo);

            } else {
            appInfo = new HashMap();
            appInfo.put("responseContent", responseContent);
            patAppInfo.put("appInfo", appInfo);
          }
			JSONSerializer js = new JSONSerializer().exclude("class");
	        response.getWriter().write(js.deepSerialize(patAppInfo));
	        response.flushBuffer();


    	 return null;
     }

     /*
      * This method is use to get existing appointment record of a patient.
      * This ajax call use in javascript to check when we are marking a rsource as not available, that time any booked or
      * confirmed appointments are there or not.
      */
     @IgnoreConfidentialFilters
     public ActionForward getResourceAppointments(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)throws IOException, SQLException ,Exception{

    	 String startTimeStr = request.getParameter("start_non_available_time");
    	 String endTimeStr = request.getParameter("end_non_available_time");
    	 String resourceId = request.getParameter("resource_id");
    	 String resourceType = request.getParameter("resource_type");
    	 Timestamp unavailableStartDateTime = null;
    	 Timestamp unavailableEndDateTime = null;
    	 Map appInfo = new HashMap();
    	 if (startTimeStr != null) {
    		 unavailableStartDateTime = DateUtil.parseTimestamp(startTimeStr);
    	 }
    	 if (endTimeStr != null) {
    		 unavailableEndDateTime = DateUtil.parseTimestamp(endTimeStr);
    	 }
    	 BasicDynaBean doctorAppBean = null;
    	 String responseContent = "false";

    	 List<BasicDynaBean> resourceAppList = null;
    	 if (unavailableStartDateTime != null && unavailableEndDateTime != null) {
    		 resourceAppList = new ResourceDAO(null).getResourceAppointments(resourceId,resourceType,unavailableStartDateTime, unavailableEndDateTime);
    	 }
    	 if (resourceAppList != null && resourceAppList.size() > 0) {
    		 doctorAppBean = resourceAppList.get(0);
    	 }

    	 if (doctorAppBean != null) {
    		 responseContent = "true";
    	 }
    	 appInfo.put("success", responseContent);
    	 JSONSerializer js = new JSONSerializer().exclude("class");
    	 response.setContentType("application/x-json");
		 response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    	 response.getWriter().write(js.deepSerialize(appInfo));
    	 response.flushBuffer();

    	 return null;
     }

     public ActionForward getPrescribedDoctors(ActionMapping mapping, ActionForm form,
    		 HttpServletRequest request, HttpServletResponse response)throws IOException, SQLException {

    	 String prescribingDoctor = null;
    	 String visitID = request.getParameter("visitId");
    	 List<String> columns = new ArrayList<String>();
    	 columns.add("doctor");
    	 columns.add("visit_type");
    	 Map<String, Object> identifiers = new HashMap<String, Object>();
    	 identifiers.put("patient_id", visitID);
    	 BasicDynaBean regBean = new GenericDAO("patient_registration").findByKey(columns, identifiers);
    	 List<Map> prescDoctors = OrderDAO.getPrescDoctorListForOrderedItems(visitID);
    	 Map resultMap = new HashMap();
    	 resultMap.put("patient", regBean.getMap());
    	 resultMap.put("prescDocList", prescDoctors);
    	 JSONSerializer json = new JSONSerializer().exclude("class");
    	 response.setContentType("application/x-json");
		 response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		 response.getWriter().write(json.deepSerialize(resultMap));
    	 return null;

     }

   @IgnoreConfidentialFilters
   public ActionForward getResourseCenter(ActionMapping mapping,ActionForm form,
		   HttpServletRequest req,HttpServletResponse res) throws IOException,SQLException {
	   Connection con = null;
	   String doctorID = req.getParameter("doctor_id");
	   //con = DataBaseUtil.getConnection();
	   List<Map> resourseCenter = new ResourceDAO(null).getPrescDoctorListForCenter(doctorID);
	   Map resultMap = new HashMap();
	   resultMap.put("resourseCenterList", resourseCenter);
	   JSONSerializer json = new JSONSerializer().exclude("class");
  	   res.setContentType("application/x-json");
  	   res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
  	   res.getWriter().write(json.deepSerialize(resultMap));
	  return null;

   }

   public ActionForward getHijriToGregDate(ActionMapping mapping,ActionForm form,
		   HttpServletRequest req,HttpServletResponse res) throws IOException,SQLException {
	   Connection con = null;
	   String date = req.getParameter("date");
	   String[] params = date.split("-");
       UmmalquraCalendar cal = new UmmalquraCalendar(Integer.parseInt(params[0]), Integer.parseInt(params[1]) - 1, Integer.parseInt(params[2]));
       java.util.Date gregDate = cal.getTime();
       String gregDateStr = DateUtil.formatDate(gregDate);
	   Map resultMap = new HashMap();
	   resultMap.put("gregDate", gregDateStr);
	   JSONSerializer json = new JSONSerializer().exclude("class");
  	   res.setContentType("application/x-json");
  	   res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
  	   res.getWriter().write(json.deepSerialize(resultMap));
	  return null;

   }

   public ActionForward showCancelAppointmentScreen(ActionMapping mapping, ActionForm form,
		   HttpServletRequest request, HttpServletResponse response)throws SQLException {

	   String appointmentID = request.getParameter("appointmentId");
	   if (appointmentID != null && !appointmentID.equals("")) {
		   request.setAttribute("channelingItems", new ResourceDAO(null).getChannelingItems(Integer.parseInt(appointmentID)));
		   request.setAttribute("schedularBean", new GenericDAO("scheduler_appointments").findByKey("appointment_id", Integer.parseInt(appointmentID)));
	   }
	   if (request.getParameter("bill_number") != null && !request.getParameter("bill_number").equals(""))
		   request.setAttribute("cancelledItemsList", new GenericDAO("bill_charge").findAllByKey("bill_no", (request.getParameter("bill_number"))));

	   return mapping.findForward("showCancelApptScreen");
   }

   public ActionForward cancelChannelingAppointment(ActionMapping mapping, ActionForm form,
		   HttpServletRequest request, HttpServletResponse response)throws SQLException, IOException, Exception {

	   String[] cancel = request.getParameterValues("cancel");
	   String[] activityIDs = request.getParameterValues("activity_id");
	   String[] activityCodes = request.getParameterValues("activity_code");
	   String[] itemIDs = request.getParameterValues("act_description_id");
	   String appointmentID = request.getParameter("appointmentId");
	   String[] packageIDs = request.getParameterValues("package_id");
	   String[] chargeIDs = request.getParameterValues("charge_id");
	   String cancelReason = request.getParameter("cancel_reason");

	   List<BasicDynaBean> cancelItemOrders = new ArrayList<BasicDynaBean>();
	   List<BasicDynaBean> cancelItemChargeOrders = new ArrayList<BasicDynaBean>();
	   List<BasicDynaBean> editOrders = new ArrayList<BasicDynaBean>();
	   List<String> editOrCancelOrderBills = new ArrayList<String>();
	   List<String> cancelledChgIDs = new ArrayList<String>();
	   BasicDynaBean bean = null;

	   GenericDAO schedulerApptDAO = new GenericDAO("scheduler_appointments");
	   GenericDAO billDAO = new GenericDAO("bill");

	   ActionRedirect redirect = new ActionRedirect(mapping.findForward("showCancelApptScreenRedirect"));
	   FlashScope flash = FlashScope.getScope(request);
	   redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
	   redirect.addParameter("appointmentId", appointmentID);
	   redirect.addParameter("mrno", request.getParameter("mrno"));
	   redirect.addParameter("doctor_id", request.getParameter("doctor_id"));
	   redirect.addParameter("doctor_name", request.getParameter("doctor_name"));
	   redirect.addParameter("department", request.getParameter("department"));

	   String userName = (String)request.getSession(false).getAttribute("userid");
	   Connection con = null;
	   String err = null;
	   OrderBO orderBO = new OrderBO();
	   boolean success = true;
	   boolean statusUpdate = true;
	   GenericDAO serviceDAO = new GenericDAO("services_prescribed");

	   List newOrders = new ArrayList();
	   List<Boolean> multiVisitPackageList = new ArrayList<Boolean>();
	   String billNumber = null;
	   BasicDynaBean billBean = null;

	   try {
		   con = DataBaseUtil.getConnection();
		   con.setAutoCommit(false);

		   BasicDynaBean schedulerApptBean = schedulerApptDAO.findByKey(con, "appointment_id", Integer.parseInt(appointmentID));
		   BasicDynaBean bill = billDAO.findByKey(con, "bill_no", (String)schedulerApptBean.get("bill_no"));

		   if (activityIDs != null && activityIDs.length > 0) {
			   for (int i=0; i<activityIDs.length; i++) {
				   bean = ResourceBO.getCancelBean(activityCodes[i], activityIDs[i], userName);
				   cancelItemOrders.add(bean);
			   }
		   }
		   String paymentStatus = new GenericDAO("bill").findByKey("bill_no",
				   schedulerApptBean.get("bill_no")).get("payment_status").toString();
		   boolean unlinkActivity = paymentStatus.equals("U");

		   err = orderBO.updateOrders(con, cancelItemOrders, true, false, unlinkActivity, editOrCancelOrderBills, Collections.EMPTY_LIST,
				   Collections.EMPTY_LIST);
		   if (err == null && cancelItemOrders.size() > 0 && unlinkActivity)
			   err = updateBillStatus(con, schedulerApptBean.get("bill_no"), userName, cancelReason);

		   if (err == null && cancelItemOrders.size() > 0) {
			   int unique_appt_ind = (Integer)schedulerApptBean.get("unique_appt_ind");
			   if(unique_appt_ind == 0) {
				   unique_appt_ind = new ResourceDAO(null).getNextUniqueAppointMentInd();
			   }
			   java.sql.Timestamp modTime = DataBaseUtil.getDateandTime();
			   statusUpdate = ResourceBO.updateAppointments(
			       con, Integer.parseInt(appointmentID), "Cancel", cancelReason, userName, 
			       unique_appt_ind, modTime);
		   }
		   if (!statusUpdate)
			   err = "Failed to update the appointment status";

		   if (null == err && null != cancel && !cancel.equals("")) {

			   for (int j=0; j<cancel.length; j++) {
				   if (cancel[j].equals("Y")) {
					   if (bill.get("status").equals("A")) {
						   err = "Bill is opened";
						   break;
					   }
					   if (activityCodes[j].equals("SER")) {

						    Map<String, String> map = new HashMap<String, String>();
						    map.put("activity_id", activityIDs[j]);
						    map.put("acrivity_code", activityCodes[j]);

						    BasicDynaBean b = serviceDAO.getBean();
							b.set("service_id", itemIDs[j]);
							b.set("quantity", new java.math.BigDecimal(1));
							b.set("presc_date", DataBaseUtil.getDateandTime());

							newOrders.add(b);
							multiVisitPackageList.add(true);
							cancelledChgIDs.add(chargeIDs[j]);
					   }
				   }
			   }
			   if (newOrders != null && newOrders.size() > 0) {
				   orderBO.setPackageId(packageIDs[0]);
				   err = orderBO.setBillInfo(con, (String)bill.get("visit_id"), null, false, userName);
				   err = orderBO.orderItems(con, newOrders, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
							multiVisitPackageList, 0, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
							Collections.EMPTY_LIST, Collections.EMPTY_LIST);
				   billBean = orderBO.getBill();

				   if (err == null) {
					   DataBaseUtil.commitClose(con, true);
					   con = DataBaseUtil.getConnection();
					   con.setAutoCommit(false);
				   }
			   }

			   if (billBean != null && billBean.get("bill_no") != null && !billBean.get("bill_no").toString().equals("")) {
				   redirect.addParameter("bill_number", (String)billBean.get("bill_no"));
				   err = setNegativeAmounts(con, (String)billBean.get("bill_no"));
			   }

			   if (err == null) {
				   err = postConductingDrPayments(con, cancelledChgIDs, appointmentID, request.getParameter("doctor_id"));
			   }
		   }

		   if (err != null) {
			   flash.error(err);
			   success = false;
		   }

	   } finally {
		   DataBaseUtil.commitClose(con, success);
	   }

	   return redirect;
   }

   private String setNegativeAmounts(Connection con, String billNumber)throws SQLException, IOException {

	   GenericDAO billDAO = new GenericDAO("bill");
	   GenericDAO billChgDAO = new GenericDAO("bill_charge");
	   boolean success = false;

	   BasicDynaBean billBean = billDAO.findByKey(con, "bill_no", billNumber);
	   billBean.set("total_amount", ((BigDecimal)billBean.get("total_amount")).negate());
	   success = billDAO.updateWithName(con, new String[] {"total_amount"}, billBean.getMap(), "bill_no") > 0;


	   List<BasicDynaBean> chargeItems = billChgDAO.findAllByKey("bill_no", billNumber);

	   if (chargeItems != null && chargeItems.size() > 0) {
		   for (BasicDynaBean chgBean : chargeItems) {
			   chgBean.set("amount", ((BigDecimal)chgBean.get("amount")).negate());
			   success &= billChgDAO.updateWithName(con, new String[] {"amount"}, chgBean.getMap(), "charge_id") > 0;
		   }
	   }

	   if (!success) {
		   return "Failed to cancel the channeling appointments";
	   }
	   return null;

   }

   public static String postConductingDrPayments(Connection con, List<String> cancelledChgIDs,
		   							String appointmentID, String doctorID)throws SQLException, IOException, Exception {

	   BasicDynaBean schedulerApptBean = new GenericDAO("scheduler_appointments").
			   				findByKey(con, "appointment_id", Integer.parseInt(appointmentID));
	   List<BasicDynaBean> activitiesList = new ResourceDAO(null).getActivities(con, (String)schedulerApptBean.get("bill_no"));
	   GenericDAO billChgDAO = new GenericDAO("bill_charge");
	   BasicDynaBean billChgBean = billChgDAO.getBean();
	   boolean success = true;
	   String errorMsg = null;

	   try {
		   for (BasicDynaBean bean : activitiesList) {
			   if (!cancelledChgIDs.contains(bean.get("charge_id").toString())) {
				   billChgBean.set("payee_doctor_id", doctorID);
				   billChgBean.set("charge_id", bean.get("charge_id"));

				   success &= billChgDAO.updateWithName(con, billChgBean.getMap(), "charge_id") > 0;
				   success &= PaymentEngine.updatePayoutAmounts(con, (String)bean.get("charge_id"), true, false, false);

				   if(!success)
					   break;
			   }
		   }
	   } finally {
		   if (!success)
			   errorMsg = "Failed post the payments for the doctor.";

	   }

	   return errorMsg;
   }

   private String updateBillStatus(Connection con, Object billNumber, String username,
		   String cancelReason)throws IOException, SQLException {

	   Map<String, Object> map = new HashMap<String, Object>();
	   map.put("status", "X");
       map.put("finalized_date", DateUtil.getCurrentTimestamp());
       map.put("last_finalized_at", DateUtil.getCurrentTimestamp());
	   map.put("mod_time", DateUtil.getCurrentTimestamp());
	   map.put("closed_by", username);
	   map.put("username", username);
	   map.put("cancel_reason", cancelReason);
	   map.put("bill_no", billNumber);

	   return new GenericDAO("bill").update(con, map, "bill_no", billNumber) > 0 ? null : "Failed to cancel the bill";

   }

  private ActionForward markResourceNonAvailableCenterWise(ActionMapping am,
           ActionForm af, HttpServletRequest req, HttpServletResponse res)
   throws Exception {
	   HttpSession session = req.getSession();
	   FlashScope flash = FlashScope.getScope(req);
	   ActionRedirect redirect =null;
	   String screenmethod = req.getParameter("screenmethod");
	   String includeResources = req.getParameter("includeResources");
       int loggedIncenter = (Integer)session.getAttribute("centerId");
	   String resourceName = req.getParameter("nonAvailableDoctor");
       String category = req.getParameter("category");
       String dateStr = req.getParameter("date");
       String dept = req.getParameter("department");
       String noAvlRemarks = req.getParameter("remarks");
       String resCenterId = req.getParameter("centerId");
       int centerId = resCenterId.equals("") ? 0 : Integer.parseInt(resCenterId);
       Time slotFromTime = DataBaseUtil.parseTime(req.getParameter("firstSlotFromTime"));
       Time slotToTime = DataBaseUtil.parseTime(req.getParameter("firstSlotToTime"));
       List<BasicDynaBean> resourceAvailabilityList = null;
       BasicDynaBean overrideBean = null;
       BasicDynaBean nonAvailBean = null;
       Time s_fromTime = null;
       Time s_toTime = null;
       Connection con = null;
       String errorMsg = null;
       Date d = DataBaseUtil.parseDate(dateStr);
       Calendar cal = Calendar.getInstance();
       cal.setTime(d);
       int weekDayNo = (cal.get(Calendar.DAY_OF_WEEK)-1);
       BasicDynaBean mainBean = null;
       boolean success = false;
       String resourceType = null;
       if (category.equals("DOC"))
           resourceType = category;
       if(slotFromTime != null && slotToTime != null) {
    	   try{
    		   con = DataBaseUtil.getConnection();
               con.setAutoCommit(false);
               resourceAvailabilityList = new ResourceDAO(null).getResourceAvailabilities(resourceType, d, resourceName,null,centerId);
               if (resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
                   overrideBean = (BasicDynaBean)resourceAvailabilityList.get(0);
               }
               if (resourceAvailabilityList != null && resourceAvailabilityList.size() < 1) {
                   resourceAvailabilityList = new ResourceDAO(null).getResourceDefaultAvailabilities(resourceName, weekDayNo, resourceType,null,centerId);
               }

               if (resourceAvailabilityList != null && resourceAvailabilityList.size() < 1) {
                   resourceAvailabilityList = new ResourceDAO(null).getResourceDefaultAvailabilities("*", weekDayNo, resourceType,null,centerId);
               }
               if (overrideBean != null) {
            	   int resAvailId = (Integer)overrideBean.get("res_avail_id");
            	   mainBean = new GenericDAO("sch_resource_availability").getBean();
            	   mainBean.set("res_avail_id", resAvailId);
            	   new GenericDAO("sch_resource_availability_details").delete(con, "res_avail_id", resAvailId);

            	   int index = 0;
                   boolean isDone = false;
                   if (resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
                       for (int i=0;i<resourceAvailabilityList.size();i++) {
                           nonAvailBean = (BasicDynaBean)resourceAvailabilityList.get(i);
                           Time fromTime = (Time)nonAvailBean.get("from_time");
                           Time toTime = (Time)nonAvailBean.get("to_time");
                           String availabilityStatus = (String)nonAvailBean.get("availability_status");
                           String remarks = (String)nonAvailBean.get("remarks");
                           Integer resAvailCenterId = (Integer)nonAvailBean.get("center_id");
                           if (fromTime != null && toTime != null) {
                        		   if(resourceAvailabilityList.size() == 1) {
                        			 //check for specific login center
                        			   if(loggedIncenter != 0){
                        				   if(resAvailCenterId.equals(0)) {
                        					   //show error flash message on submit
                        						   errorMsg = "Not Allowed , since doctor availability mapped to default center";
    	                            				 break;
                        				   }
                        			   }
                        			   //allow mark resource non available
                        			   if(!slotFromTime.equals(fromTime) )
                        				   bo.insertTimings(con, mainBean, fromTime, slotFromTime, "A",remarks,resAvailCenterId);

                        			   bo.insertTimings(con, mainBean, slotFromTime, slotToTime, "N",noAvlRemarks,null);

	                                   if(!slotToTime.equals(toTime))
	                                	   bo.insertTimings(con, mainBean, slotToTime, toTime, "A",null,resAvailCenterId);

                        		   }else if(availabilityStatus.equals("A") && !isDone && i != resourceAvailabilityList.size()-1
                                           && resourceAvailabilityList.get(i+1).get("availability_status").equals("A")
                                           && resourceAvailabilityList.get(i+1).get("center_id").equals(resAvailCenterId)
                                           && (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime) || slotFromTime.before(toTime))
                                           && ((slotToTime.equals(toTime) || slotToTime.before(toTime))
                                        		|| slotFromTime.before(toTime)) ) {


                                       		s_fromTime = index == 0 ? fromTime : s_fromTime;
                                       		index++;

		                               }
                        		   else {
		                            	   if(!isDone && (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime))
		                                          && (slotToTime.equals(toTime) || slotToTime.before(toTime))) {

		                            		   s_fromTime = index == 0 ? fromTime : s_fromTime;
		                                       index++;
		                                    }
		                                    if(index != 0) {
		                                    	if(loggedIncenter != 0){
		                                    		if(resAvailCenterId.equals(0)) {
		                                    			//show error flash message on submit
		                                    				errorMsg = "Not Allowed , since doctor availability mapped to default center";
		      	                            				break;
		      	                            		 }
		                                  	   	}

		                                        if(!slotFromTime.equals(s_fromTime))
		                                        	bo.insertTimings(con, mainBean, s_fromTime, slotFromTime, "A",null,resAvailCenterId);

		                                        bo.insertTimings(con, mainBean, slotFromTime, slotToTime, "N",noAvlRemarks,null);

		                                        if(i != resourceAvailabilityList.size()-1)
		                                        	s_toTime = (Time)resourceAvailabilityList.get(i+1).get("from_time");
		                                        else
		                                        	s_toTime = toTime;

		                                        if(!slotToTime.equals(s_toTime))
		                                        	bo.insertTimings(con, mainBean, slotToTime, s_toTime, "A",null,resAvailCenterId);

		                                        index = 0;
		                                        isDone = true;
		                                      }else{
		                                    	  if(availabilityStatus.equals("A")){
		                                    		  bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,resAvailCenterId);
			                                      }
			                                      else{
			                                    	  if(resAvailCenterId != null && resAvailCenterId != 0){
			                                    		  //make availability status as "A" and insert it
			                                    		  availabilityStatus = "A";
			                                    		  bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,resAvailCenterId);
			                                    	  }else
			                                    		  bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,null);
			                                    }
			                                }
		                             }
                           	}
                       }
                   }
  //when default timing display execute this part >>>>>
               }else{
                   int genResAvailId = new GenericDAO("sch_resource_availability").getNextSequence();
                   mainBean = new GenericDAO("sch_resource_availability").getBean();
                   mainBean.set("res_avail_id",genResAvailId);
                   mainBean.set("res_sch_name", resourceName);
                   mainBean.set("res_sch_type", resourceType);
                   mainBean.set("availability_date", d);
                   new GenericDAO("sch_resource_availability").insert(con, mainBean);
                   int index = 0;
                   boolean isDone = false;

                   if (resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
                       for (int i=0;i<resourceAvailabilityList.size();i++) {
                           nonAvailBean = (BasicDynaBean)resourceAvailabilityList.get(i);
                           Time fromTime = (Time)nonAvailBean.get("from_time");
                           Time toTime = (Time)nonAvailBean.get("to_time");
                           String availabilityStatus = (String)nonAvailBean.get("availability_status");
                           String remarks = (String)nonAvailBean.get("remarks");
                           Integer resAvailCenterId = (Integer)nonAvailBean.get("center_id");
                           if (fromTime != null && toTime != null) {
                               if(resourceAvailabilityList.size() == 1 ) {
                            	   //check for specific login center
                            	   if(loggedIncenter != 0){
	                            		  if(resAvailCenterId.equals(0)) {
	                            			  //show error flash message on submit
	                            				  errorMsg = "Not Allowed , since doctor availability mapped to default center";
	                            				  break;
	                            		  }
                            	   	}
		                                   if(!slotFromTime.equals(fromTime))
		                                	   bo.insertTimings(con, mainBean, fromTime, slotFromTime, "A",null,resAvailCenterId);

		                                   bo.insertTimings(con, mainBean, slotFromTime, slotToTime, "N",noAvlRemarks,null);

		                                   if(!slotToTime.equals(toTime))
		                                	   bo.insertTimings(con, mainBean, slotToTime, toTime, "A",null,resAvailCenterId);

                               }else if(availabilityStatus.equals("A") && !isDone && i != resourceAvailabilityList.size()-1
                                       && resourceAvailabilityList.get(i+1).get("availability_status").equals("A")
                                       && resourceAvailabilityList.get(i+1).get("center_id").equals(resAvailCenterId)
                                       && (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime) || slotFromTime.before(toTime))
                                       && ((slotToTime.equals(toTime) || slotToTime.before(toTime))
                                    		|| slotFromTime.before(toTime)) ) {

                                   s_fromTime = index == 0 ? fromTime : s_fromTime;
                                   index++;

                               }else {
                                   if(!isDone && (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime))
                                           && (slotToTime.equals(toTime) || slotToTime.before(toTime))) {

                                       s_fromTime = index == 0 ? fromTime : s_fromTime;
                                       index++;
                                   }
                                   if(index != 0) {

                                	   if(loggedIncenter != 0){
 	                            		  if(resAvailCenterId.equals(0)) {
 	                            			  //show error flash message on submit
 	                            				  errorMsg = "Not Allowed , since doctor availability mapped to default center";
 	                            				 break;
 	                            		  }
                             	   	}

                                           if(!slotFromTime.equals(s_fromTime))
                                            bo.insertTimings(con, mainBean, s_fromTime, slotFromTime, "A",null,resAvailCenterId);

                                           bo.insertTimings(con, mainBean, slotFromTime, slotToTime, "N",noAvlRemarks,null);

                                           if(i != resourceAvailabilityList.size()-1)
                                               s_toTime = (Time)resourceAvailabilityList.get(i+1).get("from_time");
                                           else
                                               s_toTime = toTime;

                                           if(!slotToTime.equals(s_toTime))
                                        	   bo.insertTimings(con, mainBean, slotToTime, s_toTime, "A",null,resAvailCenterId);

                                           index = 0;
                                           isDone = true;


                                   } else {
                                	   if(availabilityStatus.equals("A")){
                                		   bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,resAvailCenterId);
                                	   }
                                	   else{
                                		   if(resAvailCenterId != null && resAvailCenterId != 0 ){
                                			   //make availability status as "A" and insert it
                                			   availabilityStatus = "A";
                                			   bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,resAvailCenterId);
                                		   }else
                                			   bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,null);
                                	   }

                                   }
                               }
                           }
                       }
                   }
               }
           // for scheduler messaging
               List<BasicDynaBean> appointmentIds = new ArrayList<BasicDynaBean>();
               String fromTimestampStr = dateStr+" "+req.getParameter("firstSlotFromTime");
               String toTimestampStr = dateStr+" "+req.getParameter("firstSlotToTime");
               SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:mm");
               java.util.Date fromdate = (java.util.Date)dateFormat.parse(fromTimestampStr);
               java.util.Date todate = (java.util.Date)dateFormat.parse(toTimestampStr);
               Timestamp fromUnavailableTime = new Timestamp(fromdate.getTime());
               Timestamp toUnavailableTime = new Timestamp(todate.getTime());
               appointmentIds = new ResourceDAO(null).getAppointments(fromUnavailableTime,toUnavailableTime,resourceName,category);

               if(appointmentIds != null && appointmentIds.size() > 0 && errorMsg==null) {
                   for(BasicDynaBean appts : appointmentIds) {
                       if(appts != null) {
                           String appointStatus = (String)appts.get("appointment_status");
                           int appointmnetId = (Integer)appts.get("appointment_id");
                           if (MessageUtil.allowMessageNotification(req,"scheduler_message_send") && category.equals("DOC") && null != appointStatus
                                   && (appointStatus.equalsIgnoreCase("Booked") || appointStatus.equalsIgnoreCase("Confirmed")) && 
                                   !(new ResourceDAO(null).getAppointmentSource(appointmnetId)!=null && new ResourceDAO(null).getAppointmentSource(appointmnetId).equalsIgnoreCase("practo"))) {
                               // Send the message to all the affected appointments
                             MessageManager mgr = new MessageManager();
                             Map resourceUnavailability = new HashMap();
                             resourceUnavailability.put("appointment_id", appointmnetId);
                             resourceUnavailability.put("status", appointStatus);
                             mgr.processEvent("doctor_unavailable", resourceUnavailability);
                             if(appointStatus.equalsIgnoreCase("Confirmed") || appointStatus.equalsIgnoreCase("booked")){
                               	unscheduleAppointmentMsg(appointmnetId);
                               }
                           }
                       }
                   }
               }
               if(errorMsg == null || errorMsg.isEmpty())
            	   success = true;
        } finally {
                DataBaseUtil.commitClose(con, success);
                if (success && PractoBookHelper.isPractoAdvantageEnabled()) {
                    PractoBookHelper.addUpdateOverridesToPracto(resourceName, d, d,
                       loggedIncenter == 0 ? null : loggedIncenter);
                 }
         }
       }

       String timeStr = (String)req.getParameter("firstSlotFromTime");
       if (errorMsg != null)
    	   flash.put("error", errorMsg);
       if (am.getProperty("action_id").endsWith("_week_scheduler"))
           redirect = new ActionRedirect(am.findForward("weekViewRedirect"));
       else
           redirect = new ActionRedirect(am.findForward("dayViewRedirect"));
        redirect.addParameter("category", category);

        redirect.addParameter("includeResources", includeResources);
        redirect.addParameter("choosenWeekDate", req.getParameter("choosenWeekDate"));
        redirect.addParameter("date", dateStr);
        redirect.addParameter("department", dept);
        redirect.addParameter("centerId", req.getParameter("centerId"));
        /*if(success){
            flash.put("success", "Doctor Non availability details are updated sucessfully...");
        }else{
            flash.put("error", "Failed to Update doctor non availability details... ");
        }*/
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        redirect.setAnchor(timeStr);
        return redirect;
   }


   public ActionForward markResourceAvailableCenterWise(ActionMapping am,
           ActionForm af, HttpServletRequest req, HttpServletResponse res)
   throws Exception {


	   HttpSession session = req.getSession();
	   FlashScope flash = FlashScope.getScope(req);
	   ActionRedirect redirect =null;
	   String screenmethod = req.getParameter("screenmethod");
	   String includeResources = req.getParameter("includeResources");
       int loggedIncenter = (Integer)session.getAttribute("centerId");
	   String resourceName = req.getParameter("availableDoctor");
       String category = req.getParameter("category");
       String dateStr = req.getParameter("date");
       String dept = req.getParameter("department");
       String avlRemarks = req.getParameter("remarks");
       String availAllCenter = req.getParameter("_dialog_center");
       String availBelongCenter = req.getParameter("dialog_center");
       int availResourceCenter;
       if(availAllCenter != null && !availAllCenter.equals(""))
    	   availResourceCenter = loggedIncenter == 0 ? Integer.parseInt(availAllCenter) : loggedIncenter;
    	else
    		availResourceCenter = loggedIncenter == 0 ? Integer.parseInt(availBelongCenter) : loggedIncenter;

       String resCenterId = req.getParameter("centerId");
       int centerId = resCenterId.equals("") ? 0 : Integer.parseInt(resCenterId);
       Time slotFromTime = DataBaseUtil.parseTime(req.getParameter("avFirstSlotFromTime"));
       Time slotToTime = DataBaseUtil.parseTime(req.getParameter("avFirstSlotToTime"));
       List<BasicDynaBean> resourceAvailNonAvailList = null;
       BasicDynaBean overrideBean = null;
       BasicDynaBean nonAvailBean = null;
       Time s_fromTime = null;
       Time s_toTime = null;
       Connection con = null;
       String errorMsg = null;
       Date d = DataBaseUtil.parseDate(dateStr);
       Calendar cal = Calendar.getInstance();
       cal.setTime(d);
       int weekDayNo = (cal.get(Calendar.DAY_OF_WEEK)-1);
       BasicDynaBean mainBean = null;
       boolean success = false;
       String resourceType = null;

       if (category.equals("DOC"))
           resourceType = category;


      if(slotFromTime != null && slotToTime != null) {

       try {
           con = DataBaseUtil.getConnection();
           con.setAutoCommit(false);
           resourceAvailNonAvailList = new ResourceDAO(null).getResourceAvailabilities(resourceType, d, resourceName,null,centerId);
           if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() > 0) {
               overrideBean = (BasicDynaBean)resourceAvailNonAvailList.get(0);
           }
           if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() < 1) {
               resourceAvailNonAvailList = new ResourceDAO(null).getResourceDefaultAvailabilities(resourceName, weekDayNo, resourceType,null,centerId);
           }

           if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() < 1) {
               resourceAvailNonAvailList = new ResourceDAO(null).getResourceDefaultAvailabilities("*", weekDayNo, resourceType,null,centerId);
           }
           if (overrideBean != null) {
        	   int resAvailId = (Integer)overrideBean.get("res_avail_id");
        	   mainBean = new GenericDAO("sch_resource_availability").getBean();
        	   mainBean.set("res_avail_id", resAvailId);
        	   new GenericDAO("sch_resource_availability_details").delete(con, "res_avail_id", resAvailId);

        	   int index = 0;
               boolean isDone = false;
               if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() > 0) {
                   for (int i=0;i<resourceAvailNonAvailList.size();i++) {
                       nonAvailBean = (BasicDynaBean)resourceAvailNonAvailList.get(i);
                       Time fromTime = (Time)nonAvailBean.get("from_time");
                       Time toTime = (Time)nonAvailBean.get("to_time");
                       String availabilityStatus = (String)nonAvailBean.get("availability_status");
                       String remarks = (String)nonAvailBean.get("remarks");
                       Integer dbAvailCenterId = (Integer)nonAvailBean.get("center_id");

                       if (fromTime != null && toTime != null) {
                		   if(resourceAvailNonAvailList.size() == 1) {
                			 //check for specific login center
                			   if((loggedIncenter != 0 && dbAvailCenterId !=null) && (availabilityStatus.equals("N") && dbAvailCenterId != 0)){
                				   if(!dbAvailCenterId.equals(loggedIncenter)) {
                         			  //show error flash message on submit
                         				  errorMsg = "Doctor available in other centers in this slot";
                         				  break;
                				   }
                     	   	 	}
                			   //allow mark resource non available
                    			if(!slotFromTime.equals(fromTime)){
                    				if(dbAvailCenterId != null && availabilityStatus.equals("N") && dbAvailCenterId != 0)
                    					bo.insertTimings(con, mainBean, fromTime, slotFromTime, "A",remarks,dbAvailCenterId);
                    				else
                    					bo.insertTimings(con, mainBean, fromTime, slotFromTime, "N",remarks,null);
                    			}

                    			bo.insertTimings(con, mainBean, slotFromTime, slotToTime, "A",null,availResourceCenter);

                                if(!slotToTime.equals(toTime)){
                                	if(dbAvailCenterId != null && availabilityStatus.equals("N") && dbAvailCenterId != 0)
                                		bo.insertTimings(con, mainBean, slotToTime, toTime, "A",remarks,dbAvailCenterId);
                                	else
                                		bo.insertTimings(con, mainBean, slotToTime, toTime, "N",remarks,null);
                                }

                		   }else if(availabilityStatus.equals("N") && dbAvailCenterId == null && !isDone && i != resourceAvailNonAvailList.size()-1
                                   && resourceAvailNonAvailList.get(i+1).get("availability_status").equals("N")
                                   && resourceAvailNonAvailList.get(i+1).get("center_id") == null
                                   && (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime) || slotFromTime.before(toTime))
                                   && ((slotToTime.equals(toTime) || slotToTime.before(toTime)) || slotFromTime.before(toTime)) ){

                               s_fromTime = index == 0 ? fromTime : s_fromTime;
                               index++;
                		   }else {
                            if(!isDone && (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime))
                                       && (slotToTime.equals(toTime) || slotToTime.before(toTime))) {

                            	s_fromTime = index == 0 ? fromTime : s_fromTime;
                                index++;
                              }
                              if(index != 0) {
                            	  if((loggedIncenter != 0 && dbAvailCenterId !=null) && (availabilityStatus.equals("N") && dbAvailCenterId != 0)){
                            		  if(!dbAvailCenterId.equals(loggedIncenter)) {
                             			  //show error flash message on submit
                             				  errorMsg = "Doctor available in other centers in this slot";
                             				  break;
                    				   }
                         	   	 	}
                                    if(!slotFromTime.equals(s_fromTime)){
                                    	if(dbAvailCenterId != null && availabilityStatus.equals("N") && dbAvailCenterId != 0)
                                    		bo.insertTimings(con, mainBean, s_fromTime, slotFromTime, "A",remarks,dbAvailCenterId);
                                    	else
                                    		bo.insertTimings(con, mainBean, s_fromTime, slotFromTime, "N",remarks,null);
                                    }

                                    bo.insertTimings(con, mainBean, slotFromTime, slotToTime, "A",null,availResourceCenter);

                                    if(i != resourceAvailNonAvailList.size()-1)
                                    	s_toTime = (Time)resourceAvailNonAvailList.get(i+1).get("from_time");
                                    else
                                    	s_toTime = toTime;

                                    if(!slotToTime.equals(s_toTime)){
                                    	if(dbAvailCenterId != null && availabilityStatus.equals("N") && dbAvailCenterId != 0)
                                    		bo.insertTimings(con, mainBean, slotToTime, s_toTime, "A",remarks,dbAvailCenterId);
                                    	else
                                    		bo.insertTimings(con, mainBean, slotToTime, s_toTime, "N",remarks,null);
                                    }

                                    index = 0;
                                    isDone = true;

                                   }else{
                                	   if(availabilityStatus.equals("A")){
                                		   bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,dbAvailCenterId);
                                	   }
                                	   else{
                                		   if(dbAvailCenterId != null && dbAvailCenterId != 0){
                                			   //make availability status as "A" and insert it
                                			   availabilityStatus = "A";
                                			   bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,dbAvailCenterId);
                                		   }else
                                			   bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,null);
                                	   }
                                   }
                            }
                       }
                   }
               }
           //when default timing display execute this part >>>
           }else {
               int genResAvailId = new GenericDAO("sch_resource_availability").getNextSequence();
               mainBean = new GenericDAO("sch_resource_availability").getBean();
               mainBean.set("res_avail_id",genResAvailId);
               mainBean.set("res_sch_name", resourceName);
               mainBean.set("res_sch_type", resourceType);
               mainBean.set("availability_date", d);
               new GenericDAO("sch_resource_availability").insert(con, mainBean);
               int index = 0;
               boolean isDone = false;


               if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() > 0) {
                   for (int i=0;i<resourceAvailNonAvailList.size();i++) {
                       nonAvailBean = (BasicDynaBean)resourceAvailNonAvailList.get(i);
                       if (nonAvailBean != null ) {
                           Time fromTime = (Time)nonAvailBean.get("from_time");
                           Time toTime = (Time)nonAvailBean.get("to_time");
                           String availabilityStatus = (String)nonAvailBean.get("availability_status");
                           String remarks = (String)nonAvailBean.get("remarks");
                           Integer dbAvailCenterId = (Integer)nonAvailBean.get("center_id");

                           if (fromTime != null && toTime != null) {
                               if(resourceAvailNonAvailList.size() == 1 ) {
                            	   //check for specific login center
                            	   if((loggedIncenter != 0 && dbAvailCenterId !=null) && (availabilityStatus.equals("N") && dbAvailCenterId != 0)){
	                            		  if(!dbAvailCenterId.equals(loggedIncenter)) {
	                            			  //show error flash message on submit
	                            				  errorMsg = "Doctor available in other centers in this slot";
	                            				  break;
	                            		  }
                            	   	}
	                                   if(!slotFromTime.equals(fromTime)){
	                                	   if(dbAvailCenterId != null && availabilityStatus.equals("N") && dbAvailCenterId != 0)
	                                		   bo.insertTimings(con, mainBean, fromTime, slotFromTime, "A",null,dbAvailCenterId);
	                                	   else
	                                		   bo.insertTimings(con, mainBean, fromTime, slotFromTime, "N",remarks,null);
	                                   }
	                                 //put request center id for available status
	                                   bo.insertTimings(con, mainBean, slotFromTime, slotToTime, "A",null,availResourceCenter);

	                                   if(!slotToTime.equals(toTime)){
	                                	   if(dbAvailCenterId != null && availabilityStatus.equals("N") && dbAvailCenterId != 0)
	                                		   bo.insertTimings(con, mainBean, slotToTime, toTime, "A",remarks,dbAvailCenterId);
	                                	   else
	                                		   bo.insertTimings(con, mainBean, slotToTime, toTime, "N",remarks,null);
	                                   }

                               } else if(availabilityStatus.equals("N") && dbAvailCenterId == null && !isDone && i != resourceAvailNonAvailList.size()-1
                                       && resourceAvailNonAvailList.get(i+1).get("availability_status").equals("N")
                                       && resourceAvailNonAvailList.get(i+1).get("center_id") == null
                                       && (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime) || slotFromTime.before(toTime))
                                       && ((slotToTime.equals(toTime) || slotToTime.before(toTime)) || slotFromTime.before(toTime)) ){

                                   s_fromTime = index == 0 ? fromTime : s_fromTime;
                                   index++;


                    		   }else {
                                   if(!isDone && (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime))
                                           && (slotToTime.equals(toTime) || slotToTime.before(toTime))) {

                                       s_fromTime = index == 0 ? fromTime : s_fromTime;
                                       index++;
                                   }
                                   if(index != 0) {

                                	   if((loggedIncenter != 0 && dbAvailCenterId !=null) && (availabilityStatus.equals("N") && dbAvailCenterId != 0)){
                                 		  if(!dbAvailCenterId.equals(loggedIncenter)) {
                                  			  //show error flash message on submit
                                  				  errorMsg = "Doctor available in other centers in this slot";
                                  				  break;
                         				   }
                              	   	 	}

                                    if(!slotFromTime.equals(s_fromTime)){
                                    	if(dbAvailCenterId != null && availabilityStatus.equals("N") && dbAvailCenterId != 0)
                                    		bo.insertTimings(con, mainBean, s_fromTime, slotFromTime, "A",remarks,dbAvailCenterId);
                                    	else
                                    		bo.insertTimings(con, mainBean, s_fromTime, slotFromTime, "N",remarks,null);
                                    }

                                      bo.insertTimings(con, mainBean, slotFromTime, slotToTime, "A",null,availResourceCenter);

                                      if(i != resourceAvailNonAvailList.size()-1)
                                    	  s_toTime = (Time)resourceAvailNonAvailList.get(i+1).get("from_time");
                                      else
                                    	  s_toTime = toTime;

                                      if(!slotToTime.equals(s_toTime)){
                                    	  if(dbAvailCenterId != null && availabilityStatus.equals("N") && dbAvailCenterId != 0)
                                    		  bo.insertTimings(con, mainBean, slotToTime, s_toTime, "A",remarks,dbAvailCenterId);
                                    	  else
                                    		  bo.insertTimings(con, mainBean, slotToTime, s_toTime, "N",remarks,null);
                                      }


                                      index = 0;
                                      isDone = true;
                                   } else {
                                	   if(availabilityStatus.equals("A")){
                                		   bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,dbAvailCenterId);
                                	   }
                                	   else{
                                		   if(dbAvailCenterId != null && dbAvailCenterId != 0){
                                			   //make availability status as "A" and insert it
                                			   availabilityStatus = "A";
                                			   bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,dbAvailCenterId);
                                		   }else
                                			   bo.insertTimings(con, mainBean, fromTime, toTime, availabilityStatus,remarks,null);
                                  	   }
                                   }
                               }
                           }
                      }
                   }
               }
           }
           if(errorMsg == null || errorMsg.isEmpty())
        	   success = true;
       } finally {
    	   DataBaseUtil.commitClose(con, success);
    	   if (success && PractoBookHelper.isPractoAdvantageEnabled()) {
    		   PractoBookHelper.addUpdateOverridesToPracto(resourceName, d, d,
    				   loggedIncenter == 0 ? Integer.parseInt(availBelongCenter) : loggedIncenter);
    		   }
       }
   }

       String timeStr = (String)req.getParameter("avFirstSlotFromTime");
       if (errorMsg != null)
     	   flash.put("error", errorMsg);
       if (am.getProperty("action_id").endsWith("_week_scheduler"))
           redirect = new ActionRedirect(am.findForward("weekViewRedirect"));
       else
           redirect = new ActionRedirect(am.findForward("dayViewRedirect"));
        redirect.addParameter("category", category);

        redirect.addParameter("includeResources", includeResources);
        redirect.addParameter("choosenWeekDate", req.getParameter("choosenWeekDate"));
        redirect.addParameter("date", dateStr);
        redirect.addParameter("department", dept);
        redirect.addParameter("centerId", req.getParameter("centerId"));
        /*if(success){
            flash.put("success", "Updated Reource availability details sucessfully...");
        }else{
            flash.put("error", "Failed to update Reource availability details... ");
        }*/
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        redirect.setAnchor(timeStr);
        return redirect;

   }
   
   private void scheduleAppointmentMsg(Integer uniqueId, Timestamp appointmentTime) {
		String uniString= String.valueOf(uniqueId);
		Timestamp msgSendinTimeStamp=msgSendingTime(appointmentTime);
		
		if(msgSendinTimeStamp.getTime() > System.currentTimeMillis()){
			Map<String, Object> jobData = new HashMap<String, Object>();
			jobData.put("params", uniString);
			jobData.put("schema", RequestContext.getSchema());
			jobService.scheduleAt(buildJob("DynamicAppointmentReminderJob"+uniString,
					DynamicAppointmentReminderJob.class, jobData), msgSendinTimeStamp);
		}
	}
	
   private void unscheduleAppointmentMsg(Integer uniqueId) {
		String uniString= String.valueOf(uniqueId);
		Map<String, Object> jobData = new HashMap<String, Object>();
		jobData.put("params", uniString);
		jobData.put("schema", RequestContext.getSchema());
		JobDetail jobDetail = buildJob("DynamicAppointmentReminderJob"+uniString,
				DynamicAppointmentReminderJob.class, jobData);
		jobService.deleteJob(jobDetail);
	}
	
	private void rescheduleAppointmentMsg(Integer uniqueId,Timestamp appointmentTime)  {
		String uniString= String.valueOf(uniqueId);
		Timestamp msgSendinTimeStamp=msgSendingTime(appointmentTime);
		Map<String, Object> jobData = new HashMap<String, Object>();
		jobData.put("params", uniString);
		jobData.put("schema", RequestContext.getSchema());
		
		if(msgSendinTimeStamp.getTime() > System.currentTimeMillis()){
			jobService.scheduleAt(buildJob("DynamicAppointmentReminderJob"+uniString, DynamicAppointmentReminderJob.class, jobData), msgSendinTimeStamp);
		}
		else{			
			JobDetail jobDetail = buildJob("DynamicAppointmentReminderJob"+uniString,DynamicAppointmentReminderJob.class, jobData);
			jobService.deleteJob(jobDetail);
		}
	}
	
	/*
	 *  Adjust time at which message need to be send.
	 */
	private Timestamp msgSendingTime(Timestamp jobTime) {
		Integer HOUR_MILLI_SEC = 60*60*1000;
		BasicDynaBean configBean;
		int bufferHour=2;
		List columns = new ArrayList();
    columns.add("param_value");
    Map<String, Object> identifiers = new HashMap<String, Object>();
    identifiers.put("message_type_id", "sms_dynamic_appointment_reminder");
    identifiers.put("param_name", "buffer_hours");
		try {
		  configBean = new GenericDAO("message_config").findByKey(columns, identifiers);
		  bufferHour=Integer.parseInt(configBean.get("param_value").toString());
		} catch (SQLException e) {
			logger.error("", e);
		}
		Timestamp msgTime = new Timestamp(jobTime.getTime() - bufferHour*HOUR_MILLI_SEC);
		return msgTime;
	}
	

  public void schedulePushEvent(String[] appointmentId, String eventId) {
    String schema = RequestContext.getSchema();
    Map<String, Object> eventData = new HashMap<>();
    eventData.put("appointment_ids", appointmentId);
    eventData.put("schema", schema);
    eventData.put("eventId", eventId);

    Map<String, Object> jobData = new HashMap<>();
    jobData.put("schema", schema);
    jobData.put("eventId", eventId);
    jobData.put("eventData", eventData);
    jobService.scheduleImmediate(
        buildJob("PushEventJob_" + appointmentId, EventListenerJob.class, jobData));
  }
  
  public void schedulePushEvent( String appointmentId, String eventId) {
    String schema =  RequestContext.getSchema();
    Map<String, Object> eventData = new HashMap<>();
    eventData.put("appointment_id", appointmentId);
    eventData.put("schema", schema);
    eventData.put("eventId", eventId);
    
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("schema", schema);
    jobData.put("eventId", eventId);
    jobData.put("eventData", eventData);
    jobService
        .scheduleImmediate(buildJob("PushEventJob_" + appointmentId,
            EventListenerJob.class, jobData));
  }

  /**
   * Validate appointment status.
   *
   * @param appointmentId the appointment id
   * @param appStatus the app status
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String ValidateAppointmentStatus(int appointmentId, String appStatus)
      throws SQLException {
    String errorMsg = null;
    if (appStatus != null
        && !Arrays.asList("Cancel", "Confirmed", "Booked", "Noshow", "Completed", "Arrived")
            .contains(appStatus)) {
      return "Invalid appointment status.";
    }
    BasicDynaBean app = new GenericDAO("scheduler_appointments").findByKey("appointment_id",
        appointmentId);
    if (app == null) {
      return "Valid Appointment ID is required";
    }
    String oldStatus = (String) app.get("appointment_status");
    if (oldStatus.equalsIgnoreCase("Cancel") || oldStatus.equalsIgnoreCase("NoShow")
        || oldStatus.equalsIgnoreCase("Completed")
        || (oldStatus.equalsIgnoreCase("Arrived") && !(appStatus.equalsIgnoreCase("Completed")))) {
      return ("Update appointment is not possible,Appointment is already marked as " + oldStatus);
    } else if ((oldStatus.equalsIgnoreCase("Booked") || oldStatus.equalsIgnoreCase("Confirmed"))
        && (appStatus.equalsIgnoreCase("Completed"))) {
      return "Appointment status should be in arrived to mark completed.";
    }
    return errorMsg;
  }

}
