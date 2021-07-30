package com.insta.hms.resourcescheduler;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.orders.TestDocumentDTO;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class ResourceBO {
	static Logger logger = LoggerFactory.getLogger(ResourceBO.class);
	
    private static final GenericDAO schedulerAppointmentsDAO =
        new GenericDAO("scheduler_appointments");
    private static final GenericDAO schResourceAvailabilityDetailsDAO =
        new GenericDAO("sch_resource_availability_details");
    private static final GenericDAO testsPrescribedDAO = new GenericDAO("tests_prescribed");
    private static final GenericDAO servicesPrescribedDAO = new GenericDAO("services_prescribed");
		private static final GenericDAO servicesDAO = new GenericDAO("services");


	/*
	 *  Each doctor,Theatre,service,Test has it own schedle Id.
	 *  ScheduleType indicated what kind of schedule this is.
	 *
	 */
	public static class Schedule{
		public int scheduleId;
		public String scheduleType;
		public String scheduleName;//contains id's like 'DOC0001','OPE0001','THID0001'
		public int defaultDuration;
		public Time startTime;
		public Time endTime;
		public ArrayList<Resource> resourceList ;
		public String description;
		public ArrayList<Time> timeList;

		public java.util.Date scheduleDate;

		public Schedule(int scheduleId,Time startTime, Time endTime,int defaultDuration,String description){
			this.resourceList = new ArrayList<Resource>();
			this.timeList = new ArrayList<Time>();
			this.startTime = startTime;
			this.endTime = endTime;
			this.scheduleId = scheduleId;
			this.defaultDuration = defaultDuration;
			this.description = description;
		}

		public Schedule(String scheduleName, String scheduleType){
			this.scheduleName = scheduleName;
			this.scheduleType = scheduleType;
		}

		public ArrayList<Time> getTimeList() {return timeList;}
		public void setTimeList(ArrayList<Time> timeList) {this.timeList = timeList;}

		public String getDescription() {return description;}
		public void setDescription(String description) {this.description = description;}

		public int getDefaultDuration() {return defaultDuration;}
		public void setDefaultDuration(int defaultDuration) {this.defaultDuration = defaultDuration;}

		public ArrayList<Resource> getResourceList() {return resourceList;}
		public void setResourceList(ArrayList<Resource> resourceList) {this.resourceList = resourceList;}

		public int getScheduleId() {return scheduleId;}
		public void setScheduleId(int scheduleId) {this.scheduleId = scheduleId;}

		public String getScheduleName() {return scheduleName;}
		public void setScheduleName(String scheduleName) {this.scheduleName = scheduleName;}

		public String getScheduleType() {return scheduleType;}
		public void setScheduleType(String scheduleType) {this.scheduleType = scheduleType;}

		public Time getEndTime() {
			return endTime;
		}

		public void setEndTime(Time endTime) {
			this.endTime = endTime;
		}

		public Time getStartTime() {
			return startTime;
		}

		public void setStartTime(Time startTime) {
			this.startTime = startTime;
		}

		public java.util.Date getScheduleDate() {
			return scheduleDate;
		}

		public void setScheduleDate(Date scheduleDate) {
			this.scheduleDate = scheduleDate;
		}
	}

	public static class Resource{
		public int scheduleId;
		public String resouceType;
		public String resouceId;

		public Resource(int scheduleId,String resouceType,String resouceId){
			this.scheduleId = scheduleId;
			this.resouceType = resouceType;
			this.resouceId = resouceId;
		}

		public String getResouceId() {return resouceId;}
		public void setResouceId(String resouceId) {this.resouceId = resouceId;}

		public String getResouceType() {return resouceType;}
		public void setResouceType(String resouceType) {this.resouceType = resouceType;}

		public int getScheduleId() {return scheduleId;}
		public void setScheduleId(int scheduleId) {this.scheduleId = scheduleId;}

	}

	public static class Appointments{
		public String mrNo;
		public Integer contactId;
    public String visitId;
		public String patientName;
		public String phoneNo;
		public String phoneCountryCode;
		public String remarks;
		public String complaint;
		public String complaintName;
		public int appointmentId;
		public int scheduleId;
		public String scheduleName;
		public java.sql.Timestamp appointmentTime;
		public int appointmentDuration;
		public String appointStatus;
		public String bookedBy;
		public String changedBy;
		public java.sql.Timestamp bookedTime;
		public java.sql.Timestamp completedTime;
		public ArrayList<AppointMentResource> appointResourceList;
		public String cancelReason;
		public int consultationTypeId;
		public String resourceType;
		public String schedulerVisitType;
		public boolean bookedAsSecondaryResource;
		public String schPriorAuthId;
		public int schPriorAuthModeId;
		public int centerId;
		public String centerCode;
		public String centerName;
		public String departmentName;
		public String prescDocId;
		public String condDocId;
		public String paymentStatus;
		public String salutationName;
		public Integer apptToken;
		public String paidAtSource;
		public String billType;
		public String rescheduled;
		public java.sql.Timestamp origApptTime;
		public int unique_appt_ind;
		public String prim_res_id;
		private int app_source_id;
		private String practoAppointmentId;
		private String abbreviation;
		private String isPatientGroupAccessible;
		private Integer packageId;
		public Integer waitlist;
		private String visitMode;
		
    public String getVisitMode() {
      return visitMode;
    }

    public void setVisitMode(String visitMode) {
      this.visitMode = visitMode;
    }

    public Integer getContactId() {
      return contactId;
    }

    public void setContactId(Integer contactId) {
      this.contactId = contactId;
    }
    
		public Integer getPackageId() {
      return packageId;
    }

    public void setPackageId(Integer packageId) {
      this.packageId = packageId;
    }

    public String getIsPatientGroupAccessible() {
			return isPatientGroupAccessible;
		}

		public void setIsPatientGroupAccessible(String isPatientGroupAccessible) {
			this.isPatientGroupAccessible = isPatientGroupAccessible;
		}

		public String getAbbreviation() {
			return abbreviation;
		}

		public void setAbbreviation(String abbreviation) {
			this.abbreviation = abbreviation;
		}

		public String getCondDocId() {
			return condDocId;
		}

		public void setCondDocId(String condDocId) {
			this.condDocId = condDocId;
		}

		public String getPrim_res_id() {
			return prim_res_id;
		}

		public void setPrim_res_id(String prim_res_id) {
			this.prim_res_id = prim_res_id;
		}

		public int getUnique_appt_ind() {
			return unique_appt_ind;
		}
		
		public String getPhoneCountryCode(){
			return phoneCountryCode;
		}
		public void setPhoneCountryCode(String code){
			phoneCountryCode = code;
		}

		public void setUnique_appt_ind(int unique_appt_ind) {
			this.unique_appt_ind = unique_appt_ind;
		}

		public String getRescheduled() {
			return rescheduled;
		}

		public void setRescheduled(String rescheduled) {
			this.rescheduled = rescheduled;
		}

		public java.sql.Timestamp getOrigApptTime() {
			return origApptTime;
		}

		public void setOrigApptTime(java.sql.Timestamp origApptTime) {
			this.origApptTime = origApptTime;
		}

		public String getBillType() {
			return billType;
		}

		public void setBillType(String billType) {
			this.billType = billType;
		}

		public String getPaidAtSource() {
			return paidAtSource;
		}

		public void setPaidAtSource(String paidAtSource) {
			this.paidAtSource = paidAtSource;
		}

		public Integer getApptToken() {
			return apptToken;
		}

		public void setApptToken(Integer apptToken) {
			this.apptToken = apptToken;
		}

		public String getPaymentStatus() {
			return paymentStatus;
		}

		public void setPaymentStatus(String paymentStatus) {
			this.paymentStatus = paymentStatus;
		}

		public String getPrescDocId() {
			return prescDocId;
		}

		public void setPrescDocId(String prescDocId) {
			this.prescDocId = prescDocId;
		}

		public String getDepartmentName() {
			return departmentName;
		}

		public void setDepartmentName(String departmentName) {
			this.departmentName = departmentName;
		}

		public String getCenterName() {
			return centerName;
		}

		public void setCenterName(String centerName) {
			this.centerName = centerName;
		}

		public int getCenterId() {
			return centerId;
		}

		public void setCenterId(int centerId) {
			this.centerId = centerId;
		}

		public String getSchPriorAuthId() {
			return schPriorAuthId;
		}

		public void setSchPriorAuthId(String schPriorAuthId) {
			this.schPriorAuthId = schPriorAuthId;
		}

		public int getSchPriorAuthModeId() {
			return schPriorAuthModeId;
		}

		public void setSchPriorAuthModeId(int schPriorAuthModeId) {
			this.schPriorAuthModeId = schPriorAuthModeId;
		}

		public boolean isBookedAsSecondaryResource() {
			return bookedAsSecondaryResource;
		}

		public void setBookedAsSecondaryResource(boolean bookedAsSecondaryResource) {
			this.bookedAsSecondaryResource = bookedAsSecondaryResource;
		}

		public int getConsultationTypeId() {
			return consultationTypeId;
		}

		public void setConsultationTypeId(int consultationTypeId) {
			this.consultationTypeId = consultationTypeId;
		}

		public String getCancelReason() {
			return cancelReason;
		}

		public void setCancelReason(String cancelReason) {
			this.cancelReason = cancelReason;
		}

		public Appointments(int appointmentId){
			this.appointmentId = appointmentId;
			this.appointResourceList = new ArrayList<AppointMentResource>();
		}

		public ArrayList<AppointMentResource> getAppointResourceList() {
			return appointResourceList;
		}
		public void setAppointResourceList(
				ArrayList<AppointMentResource> appointResourceList) {
			this.appointResourceList = appointResourceList;
		}
		public int getAppointmentDuration() {return appointmentDuration;}
		public void setAppointmentDuration(int appointmentDuration) {this.appointmentDuration = appointmentDuration;}

		public int getAppointmentId() {return appointmentId;}
		public void setAppointmentId(int appointmentId) {this.appointmentId = appointmentId;}

		public java.sql.Timestamp getAppointmentTime() {return appointmentTime;}
		public void setAppointmentTime(java.sql.Timestamp appointmentTime) {this.appointmentTime = appointmentTime;}

		public String getAppointStatus() {return appointStatus;}
		public void setAppointStatus(String appointStatus) {this.appointStatus = appointStatus;}

		public String getBookedBy() {return bookedBy;}
		public void setBookedBy(String bookedBy) {this.bookedBy = bookedBy;}

		public java.sql.Timestamp getBookedTime() {return bookedTime;}
		public void setBookedTime(java.sql.Timestamp bookedTime) {this.bookedTime = bookedTime;}

		public String getComplaint() {return complaint;}
		public void setComplaint(String complaint) {this.complaint = complaint;}

		public String getMrNo() {return mrNo;}
		public void setMrNo(String mrNo) {this.mrNo = mrNo;}

		public String getPhoneNo() {return phoneNo;}
		public void setPhoneNo(String phoneNo) {this.phoneNo = phoneNo;}

		public String getPatientName() {return patientName;}
		public void setPatientName(String patientName) {this.patientName = patientName;}

		public int getScheduleId() {return scheduleId;}
		public void setScheduleId(int scheduleId) {this.scheduleId = scheduleId;}

		public String getScheduleName() {return scheduleName;}
		public void setScheduleName(String scheduleName) {this.scheduleName = scheduleName;}

		public String getVisitId() {return visitId;}
		public void setVisitId(String visitId) {this.visitId = visitId;}

		public java.sql.Timestamp getCompletedTime() { return completedTime; }
		public void setCompletedTime(java.sql.Timestamp completedTime) { this.completedTime = completedTime; }

		public String getComplaintName() { return complaintName; }
		public void setComplaintName(String complaintName) { this.complaintName = complaintName; }

		public String getRemarks() { return remarks;}
		public void setRemarks(String remarks) { this.remarks = remarks;}

		public String getChangedBy() {
			return changedBy;
		}
		
		public String getSalutationName() {
			return salutationName;
		}

		public void setSalutationName(String salutationName) {
			this.salutationName = salutationName;
		}

		public void setChangedBy(String changedBy) {
			this.changedBy = changedBy;
		}

		public String getResourceType() {
			return resourceType;
		}

		public void setResourceType(String resourceType) {
			this.resourceType = resourceType;
		}

		public String getSchedulerVisitType() {
			return schedulerVisitType;
		}

		public void setSchedulerVisitType(String schedulerVisitType) {
			this.schedulerVisitType = schedulerVisitType;
		}

		public String getCenterCode() {
			return centerCode;
		}

		public void setCenterCode(String centerCode) {
			this.centerCode = centerCode;
		}

		public String getPractoAppointmentId() {
			return practoAppointmentId;
		}

		public void setPractoAppointmentId(String practoAppointmentId) {
			this.practoAppointmentId = practoAppointmentId;
		}
		public int getApp_source_id() {
			return app_source_id;
		}
		public void setApp_source_id(int app_source_id) {
			this.app_source_id = app_source_id;
		}
		 public void setWaitlist(Integer waitlist) {
	      this.waitlist = waitlist;
	    }
	    
	    public Integer getWaitlist() {
	      return waitlist;
	    }

	}
	
	public static class ChannellingAppt extends Appointments {

		public ChannellingAppt(int appointmentId) {
			super(appointmentId);
			// TODO Auto-generated constructor stub
		}
		private int pat_package_id;
		private String bill_no;
		public String getBill_no() {
			return bill_no;
		}
		public void setBill_no(String bill_no) {
			this.bill_no = bill_no;
		}
		public int getPat_package_id() {
			return pat_package_id;
		}
		public void setPat_package_id(int pat_package_id) {
			this.pat_package_id = pat_package_id;
		}
		
	}


	public static class AppointMentResource{
		public int appointmentId;
		public String resourceType;
		public String resourceId;
		public String resourceName;
		public int appointment_item_id;
		public String user_name;
		public java.sql.Timestamp mod_time;

		public AppointMentResource(int appointmentId,String resourceType,String resourceId){
			this.appointmentId = appointmentId;
			this.resourceType = resourceType;
			this.resourceId = resourceId;
		}

		public int getAppointmentId() {return appointmentId;}
		public void setAppointmentId(int appointmentId) {this.appointmentId = appointmentId;}

		public String getResourceId() {return resourceId;}
		public void setResourceId(String resourceId) {this.resourceId = resourceId;}

		public String getResourceType() {return resourceType;}
		public void setResourceType(String resourceType) {this.resourceType = resourceType;}

		public String getResourceName() {return resourceName;}
		public void setResourceName(String resourceName) {this.resourceName = resourceName;}

		public int getAppointment_item_id() {return appointment_item_id;}
		public void setAppointment_item_id(int appointment_item_id) {this.appointment_item_id = appointment_item_id;}

		public java.sql.Timestamp getMod_time() {
			return mod_time;
		}

		public void setMod_time(java.sql.Timestamp mod_time) {
			this.mod_time = mod_time;
		}

		public String getUser_name() {
			return user_name;
		}

		public void setUser_name(String user_name) {
			this.user_name = user_name;
		}
	}

	public static class Slot {

		public ArrayList<Appointments> appointList;
		public int rowSpan;
		public Schedule schedule;
		public int availble =1;
		public Time time;
		public int completedOrCancelledCount;
		public boolean appointmentApplicable = true;
		public int appointDuration;
		public int defaultDuration;
		public int appointmentApplicableCount;
		public String unavailableRemarks;
		public int resource_availabilty_center_id;
		public int resource_unavail_center_id;

		public int getResource_unavail_center_id() {
			return resource_unavail_center_id;
		}
		public void setResource_unavail_center_id(int resource_unavail_center_id) {
			this.resource_unavail_center_id = resource_unavail_center_id;
		}
		public int getResource_availabilty_center_id() {
			return resource_availabilty_center_id;
		}
		public void setResource_availabilty_center_id(int resource_availabilty_center_id) {
			this.resource_availabilty_center_id = resource_availabilty_center_id;
		}
		public String getUnavailableRemarks() {
			return unavailableRemarks;
		}
		public void setUnavailableRemarks(String unavailableRemarks) {
			this.unavailableRemarks = unavailableRemarks;
		}
		public int getAppointmentApplicableCount() {
			return appointmentApplicableCount;
		}
		public void setAppointmentApplicableCount(int appointmentApplicableCount) {
			this.appointmentApplicableCount = appointmentApplicableCount;
		}
		public int getAppointDuration() {
			return appointDuration;
		}
		public void setAppointDuration(int appointDuration) {
			this.appointDuration = appointDuration;
		}
		public int getDefaultDuration() {
			return defaultDuration;
		}
		public void setDefaultDuration(int defaultDuration) {
			this.defaultDuration = defaultDuration;
		}
		public int getAvailble() {return availble;}
		public void setAvailble(int availble) {this.availble = availble;}

		public int getCompletedOrCancelledCount() {return completedOrCancelledCount;}
		public void setCompletedOrCancelledCount(int completedOrCancelledCount) {this.completedOrCancelledCount = completedOrCancelledCount;}

		public Schedule getSchedule() {return schedule;}
		public void setSchedule(Schedule schedule) {this.schedule = schedule;}

		public int getRowSpan() {return rowSpan;}
		public void setRowSpan(int rowSpan) {this.rowSpan = rowSpan;}

		public ArrayList<Appointments> getAppointList() {return appointList;}
		public void setAppointList(ArrayList<Appointments> appointList) {this.appointList = appointList;}

		public Time getTime() {return time;}
		public void setTime(Time time) {this.time = time;}

		public Slot(){
			this.appointList = new ArrayList<Appointments>();
			this.rowSpan = 1;
		}
		public boolean isAppointmentApplicable() {
			return appointmentApplicable;
		}
		public void setAppointmentApplicable(boolean appointmentApplicable) {
			this.appointmentApplicable = appointmentApplicable;
		}
	}


   public static class Row{
	   public ArrayList<Slot> slots;
	   public Time time;

	   public Time getTime() {return time;}
	   public void setTime(Time time) {this.time = time;}

	   public ArrayList<Slot> getSlots() {return slots;}
	   public void setSlots(ArrayList<Slot> slots) {this.slots = slots;}

	   public Row(Time time){
		   this.time = time;
		   this.slots = new ArrayList<Slot>();
	   }
   }


   public static class ResourceRows {
	   public ArrayList<Slot> slots;
	   public Schedule schedule;
	   public int step;

		public ArrayList<Slot> getSlots() {
			return slots;
		}
		public void setRows(ArrayList<Slot> slots) {
			this.slots = slots;
		}
		public Schedule getSchedule() {
			return schedule;
		}
		public void setSchedule(Schedule schedule) {
			this.schedule = schedule;
		}

		public int getStep() { return step; }
		public void setStep(int step) { this.step = step; }
   }

	public static Map getCalendarList(ResourceCriteria rc, String view,int centerId, String userName)throws Exception{
		
		int max_center = (Integer)new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default");
		LinkedHashMap<String,List> map = new LinkedHashMap<String,List>();
		List l = new ArrayList();
		LinkedList<Map> headers = new LinkedList<Map>();
		ArrayList<Slot> rows = null;
		TreeSet<Integer> listOfAppointments = new TreeSet<Integer>();

		//BasicDynaBean minBean = new GenericDAO("scheduler_master").findByKey("res_sch_id", 6);
		BasicDynaBean minBean = ResourceDAO.getTimeDurations(rc);
		int minSlotDuration = ((Integer)minBean.get("default_duration")).intValue();
		int minHeightInPx = ((Integer)minBean.get("height_in_px")).intValue();
		long slotDuration = (minSlotDuration*60*1000);
		//minHeightInPx = minHeightInPx*4;
		//long slotDuration = (minSlotDuration*60*1000);
		//slotDuration = (5*60*1000);
		//long slotDuration = (minSlotDuration*60*1000);
		logger.debug("minSlotDuration in minutes: "+minSlotDuration + ",long value in millisec :"+slotDuration);

		List<BasicDynaBean> appointmentList = ResourceDAO.getAppointMentList(rc,centerId, userName);
		Slot slot = null;
		Time t = null;
		Appointments ap = null;

		Schedule s = null;

		List ruler = new ArrayList();
		List longRuler = new ArrayList();
		ResourceRows rs = null;
		java.sql.Time rulerBreakTime = null;

		if (view.equals("DayView")) {
			List originalScheduleList = rc.scheduleName;
			//headers
			List<BasicDynaBean> headerList =  ResourceDAO.getScheduleNames(rc);
			for(int i=0; i<originalScheduleList.size();i++){
				for(int j=0; j<headerList.size();j++){
					BasicDynaBean header = headerList.get(j);
				    String id = (String)header.get("id");
				    if (((String)originalScheduleList.get(i)).equals(id)) {
				    	headers.add(header.getMap());
				    	break;
				    }
				}
			}
		}else {
			List<BasicDynaBean> headerList = ResourceDAO.getScheduleNames(rc);
			if(headerList != null &&  headerList.size()>0) {
				BasicDynaBean header = headerList.get(0);
				headers.add(header.getMap());
			}
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(rc.choosendate);
		int weekDayNo = (cal.get(Calendar.DAY_OF_WEEK)-1);
		String resourceType = null;

		if (rc.category.equals("DOC")) {
			resourceType = "DOC";
		} else if (rc.category.equals("DIA")) {
			resourceType = "EQID";
		} else if (rc.category.equals("SNP")) {
			resourceType = "SRID";
		} else if (rc.category.equals("OPE")) {
			resourceType = "THID";
		}

		Map timeMap = getMaxFromAndMinToAppointmentTime(rc,resourceType,weekDayNo,view);
		Long startTime = ((Long)timeMap.get("from_time"));
		Long endTime = ((Long)timeMap.get("to_time"));

		if(startTime != null && endTime != null && !startTime.equals(endTime)) {

			logger.debug("StartTime: "+startTime + ", long value :"+startTime);
			logger.debug("EndTime: "+ endTime + ", long value : "+endTime);

			for(long slotTime=startTime.longValue(); slotTime<endTime.longValue();  slotTime=slotTime+slotDuration ){
			//	if (endTime.longValue() - startTime.longValue() >= slotDuration) {
					Long nextSlotTimeInmillis = slotTime + slotDuration;
					logger.debug( "From :"+new java.sql.Time(slotTime) + " To :"+new java.sql.Time(nextSlotTimeInmillis));
					t = new java.sql.Time(slotTime);
					ruler.add(t); // first add to ruler
					longRuler.add(nextSlotTimeInmillis);
			//	}
			}
		}

		if (headers != null && rc.category.equals("DOC"))
			headers = sortByName(headers);

		Map headerBean = null;
		Map availMap = null;

		if (view.equals("DayView")) {
			for(int j=0;j<headers.size();j++){
				headerBean = (Map)headers.get(j);
				String schedleName = (String)(headerBean).get("id");
			//	int index = 0;

				List rl = ResourceDAO.getResourceList(rc.category, schedleName);
				BasicDynaBean resBean = (BasicDynaBean)rl.get(0);

				int duration = (Integer)resBean.get("default_duration");
			//	int tempDuration = duration;
				int height_in_px = (duration/minSlotDuration) * minHeightInPx;
				s = new Schedule(schedleName, rc.category);
				availMap = getAvaliabilityMap(rc, schedleName,centerId);
				rows = new ArrayList<Slot>();
				rs = new ResourceRows();

				rs.schedule = s;
				rs.step = duration/minSlotDuration;
				if(listOfAppointments != null && listOfAppointments.size() > 0)
					listOfAppointments = new TreeSet<Integer>();

			/*	if(rs.step != 1 && (rs.step % 2) != 0) {
					rs.step = 1;
					duration = minSlotDuration;
				}*/

				for(int r=0;r<longRuler.size();r=r+(rs.step)) {
					slot = new Slot();
					slot.rowSpan = height_in_px;
					//slot.rowSpan = rs.step;
					t = (Time)ruler.get(r);
					slot.time = t;
					slot.defaultDuration = duration;

				/*	if(r == 0) {
						duration = tempDuration;
						slot.rowSpan = minHeightInPx;
					}

					if(index == 1) {
						rs.step = tempDuration/minSlotDuration;
					}

					index++;*/


					if(appointmentList !=null && appointmentList.size()>0){
						 for(int k=0; k<appointmentList.size();k++){
						 	BasicDynaBean bean = appointmentList.get(k);
						 	if(schedleName.equals((String)bean.get("resource_id"))){
                                
						 		listSlotAppointments(t, slot, ap, listOfAppointments, bean);
						 	}
						 }//appointmetns for loop
					 }//appointments if condition
					rows.add(slot);
				}
				//changing the slot's appointmentApplaicable field to false if it is continutaion of appointment
				//(means if appointment duration is greater than default duration).

				changeSlotAppointmentApplicable(rc,rows, duration);
				checkResourceUnAvailbilty(rows,availMap);
				if(max_center > 1 && rc.category.equals("DOC")){
					checkResourceAvailbilty(rows,availMap);
				}
				rs.slots = rows;
				l.add(rs);
			}
			map.put("resources", l);

		}else {

			for(int j=0;j<rc.datesArray.size();j++){

				rc.choosendate = rc.datesArray.get(j);

				List<BasicDynaBean> headerList = ResourceDAO.getScheduleNames(rc);

				if(headerList != null &&  headerList.size()>0) {
					BasicDynaBean header = headerList.get(0);
				    headerBean = header.getMap();
				}

				String schedleName = (String)(headerBean).get("id");

				List rl = ResourceDAO.getResourceList(rc.category, schedleName);
				BasicDynaBean resBean = (BasicDynaBean)rl.get(0);

				int duration = (Integer)resBean.get("default_duration");
				int height_in_px = (duration/minSlotDuration) * minHeightInPx;

				s = new Schedule(schedleName, rc.category);
				s.scheduleDate = rc.datesArray.get(j);
				availMap = getAvaliabilityMap(rc, schedleName,centerId);
				rows = new ArrayList<Slot>();
				rs = new ResourceRows();

				rs.schedule = s;
				rs.step = duration/minSlotDuration;
				appointmentList = ResourceDAO.getAppointMentList(rc,centerId, userName);

				for(int r=0;r<longRuler.size();r=r+(rs.step)) {

					slot = new Slot();
					slot.rowSpan = height_in_px;
					//slot.rowSpan = rs.step;
					t = (Time)ruler.get(r);
					slot.time = t;
					slot.defaultDuration = duration;

					if(appointmentList !=null && appointmentList.size()>0){
						for(int k=0; k<appointmentList.size();k++){
						 	BasicDynaBean bean = appointmentList.get(k);

						 	listSlotAppointments(t, slot, ap, listOfAppointments, bean);

						 }//appointmetns for loop
					}//appointments if condition
				rows.add(slot);
				}
				//changing the slot's appointmentApplaicable field to false if it is continutaion of appointment
				//(means if appointment duration is greater than default duration).

				changeSlotAppointmentApplicable(rc,rows, duration);
				checkResourceUnAvailbilty(rows,availMap);
				if(max_center > 1 && rc.category.equals("DOC")){
					checkResourceAvailbilty(rows,availMap);
				}
				rs.slots = rows;
				l.add(rs);
			}

			map.put("resources", l);
		}

		List a = new ArrayList();
		a.add(minHeightInPx);

		List br = new ArrayList();
		br.add(rulerBreakTime);

		List ri = new ArrayList();
		ri.add(ruler.size());

		map.put("headers", headers);
		map.put("ruler", ruler);
		map.put("rulerIterations", ri);
		map.put("defaultHeightInPx", a);
		map.put("rulerBreakTime", br);

		return map;
	}

	private static Map<String,Long> getMaxFromAndMinToAppointmentTime(ResourceCriteria rc,String resourceType,int weekDayNo,String view) throws Exception{
		Map<String,Long> map = new HashMap<String,Long>();
		BasicDynaBean minAndMaxTimeBean = null;
		Long startTime = null;
		Long endTime = null;
		java.sql.Time defaultStartTime = new Time(DataBaseUtil.parseTime("00:00").getTime());
		java.sql.Time defaultEndTime = new Time(DataBaseUtil.parseTime("23:59").getTime());

		if(view.equals("DayView")) {
				minAndMaxTimeBean = ResourceDAO.schedulerCategoryDefaultMinFromAndMaxToTime(rc,weekDayNo,resourceType);
				if(minAndMaxTimeBean != null && minAndMaxTimeBean.get("from_time") != null
						&& minAndMaxTimeBean.get("to_time") != null) {
					startTime =  ((Time)minAndMaxTimeBean.get("from_time")).getTime() ;
					endTime = ((Time)minAndMaxTimeBean.get("to_time")).getTime();
				}
			if(startTime != null && endTime != null && !startTime.equals("") && !endTime.equals("")) {
				BasicDynaBean appointmentMinAndMaxTimeBean = ResourceDAO.getResourceMaxAndMinAppointmentTime(rc,resourceType);
				if(appointmentMinAndMaxTimeBean != null && appointmentMinAndMaxTimeBean.get("from_time") != null
						&& appointmentMinAndMaxTimeBean.get("from_time") != null && !appointmentMinAndMaxTimeBean.get("from_time").equals("")
						&& !appointmentMinAndMaxTimeBean.get("to_time").equals("")) {

					startTime = ((Time)appointmentMinAndMaxTimeBean.get("from_time")).getTime() < startTime ?
							((Time)appointmentMinAndMaxTimeBean.get("from_time")).getTime() : startTime;

					endTime = ((Time)appointmentMinAndMaxTimeBean.get("to_time")).getTime() > endTime ?
							((Time)appointmentMinAndMaxTimeBean.get("to_time")).getTime() : endTime;

				}
			}

			if((null != startTime && !startTime.equals("")) && (null != endTime && !endTime.equals(""))) {
			} else {
				startTime = defaultStartTime.getTime();
				endTime = defaultEndTime.getTime();
			}

			map.put("from_time",startTime);
			map.put("to_time",endTime);
		} else {
			map = getSchedulerWeeklyAppointmentTimingMap(rc);
		}
		return map;
	}

	private static Map<String,Long> getSchedulerWeeklyAppointmentTimingMap(ResourceCriteria rc) throws Exception{
		BasicDynaBean minAndMaxTimeBean = null;
		Map<String,Long> map = new HashMap<String,Long>();
		Long startTime = null;
		Long endTime = null;
		java.sql.Time defaultStartTime = new Time(DataBaseUtil.parseTime("00:00").getTime());
		java.sql.Time defaultEndTime = new Time(DataBaseUtil.parseTime("23:59").getTime());

			minAndMaxTimeBean = ResourceDAO.schedulerWeeklyCategoryDefaultMinFromAndMaxToTime(rc);

			if(minAndMaxTimeBean != null && minAndMaxTimeBean.get("from_time") != null
					&& minAndMaxTimeBean.get("to_time") != null) {
				startTime =  ((Time)minAndMaxTimeBean.get("from_time")).getTime() ;
				endTime = ((Time)minAndMaxTimeBean.get("to_time")).getTime();
			}

		if(startTime != null && endTime != null && !startTime.equals("") && !endTime.equals("")) {
			BasicDynaBean appointmentMinAndMaxTimeBean = ResourceDAO.getWeeklyResourceMaxAndMinAppointmentTime(rc);
			if(appointmentMinAndMaxTimeBean != null && appointmentMinAndMaxTimeBean.get("from_time") != null
					&& appointmentMinAndMaxTimeBean.get("from_time") != null && !appointmentMinAndMaxTimeBean.get("from_time").equals("")
					&& !appointmentMinAndMaxTimeBean.get("to_time").equals("")) {

				startTime = ((Time)appointmentMinAndMaxTimeBean.get("from_time")).getTime() < startTime ?
						((Time)appointmentMinAndMaxTimeBean.get("from_time")).getTime() : startTime;

				endTime = ((Time)appointmentMinAndMaxTimeBean.get("to_time")).getTime() > endTime ?
						((Time)appointmentMinAndMaxTimeBean.get("to_time")).getTime() : endTime;

			}
		}

		if((null != startTime && !startTime.equals("")) && (null != endTime && !endTime.equals(""))) {
		} else {
			startTime = defaultStartTime.getTime();
			endTime = defaultEndTime.getTime();
		}

		map.put("from_time",startTime);
		map.put("to_time",endTime);

		return map;
	}

	private static BasicDynaBean getSchedulerTimingMap(ResourceCriteria rc,String resourceType) throws SQLException,IOException{
		BasicDynaBean timeBean = ResourceDAO.getResourceMinFromAndMaxToTime(rc,resourceType);
		return timeBean;
	}

	private static void changeSlotAppointmentApplicable(ResourceCriteria rc,ArrayList<Slot>rows, int duration) {
		for(int i=0; i< rows.size(); i++) {
			int index = i;
			Slot s = rows.get(i);
			ArrayList<Appointments> appList = s.appointList;
			if (appList.size() > 0) {
				for(int k=0;k<appList.size();k++) {
					Appointments appt = appList.get(k);
					if (appt != null && (!appt.getAppointStatus().equals("Cancel")
							&& !appt.getAppointStatus().equals("Noshow")) && !appt.getAppointStatus().equals("Completed")) {
						if (!rc.category.equals(appt.resourceType)) {
							appt.bookedAsSecondaryResource = true;
						}
						int appIndex = index;
						int slotDurationCount = 0;
						int appointDuration = appt.appointmentDuration;
						s.appointDuration = appointDuration;
						s.defaultDuration = duration;
						if (appointDuration != duration) {
							slotDurationCount = appointDuration/duration;
						}
						if (slotDurationCount > 1) {
							for(int j=0; j< (slotDurationCount-1); j++) {
								int nextIndex = appIndex+1;
								if (nextIndex < rows.size()) {
									Slot ns = rows.get(nextIndex);
									ns.appointmentApplicable = false;
									ns.appointmentApplicableCount = ns.appointmentApplicableCount+1;
									appIndex++;
								}
							}
						}
					}
				}
			}
		}
	}

	public static LinkedList<Map> sortByName(LinkedList<Map> list) throws Exception{
		return new ResourceDAO().sort(list);
	}

	public static void listSlotAppointments(Time t, Slot slot, Appointments ap,
				TreeSet listOfAppointments, BasicDynaBean bean)  throws SQLException {

		Time appointmentStartTime = (java.sql.Time)bean.get("appointment_time");
 		int appointmentDurationInMin = (Integer)bean.get("appointment_duration");
 		Time appointmentEndTime = new java.sql.Time(appointmentStartTime.getTime()+appointmentDurationInMin* 60*1000 );

 		if( appointmentStartTime!=null && appointmentStartTime.equals(t) ||
 			(t.after(appointmentStartTime) && t.before(appointmentEndTime))) {

 			 int appId = (Integer)bean.get("appointment_id");
 			 if(!listOfAppointments.contains(appId)){
 				listOfAppointments.add(appId);
	 			ap = new Appointments(appId);
	 			ap.mrNo = (String)bean.get("mr_no");
	 			ap.contactId = (Integer)bean.get("contact_id");
	 			ap.visitId = (String)bean.get("visit_id");
	 			ap.patientName = (String)bean.get("patient_name");
	 			ap.phoneNo = (String)bean.get("patient_contact");
	 			ap.appointStatus = (String)bean.get("appointment_status");
	 			ap.scheduleName = (String)bean.get("scheduleitem");
	 			ap.appointmentDuration = (Integer)bean.get("appointment_duration");
	 			ap.complaintName = (String)bean.get("complaint_name");
	 			ap.resourceType = (String)bean.get("res_sch_category");
	 			ap.remarks = (String)bean.get("remarks");
	 			ap.packageId = (Integer)bean.get("package_id");
	 			ap.centerCode = (bean != null && bean.get("center_code") != null &&
	 							!bean.get("center_code").equals("")) ?(String)bean.get("center_code") : "";
				ap.centerName = (bean != null && bean.get("center_name") != null &&
						!bean.get("center_name").equals("")) ?(String)bean.get("center_name") : "";
				ap.centerId = (Integer)bean.get("center_id");
				ap.departmentName = (String)bean.get("dept_name");
				ap.prescDocId = (String)bean.get("presc_doc_id");
                ap.paymentStatus = (String)bean.get("payment_status");
                ap.paidAtSource = (String)bean.get("paid_at_source");
                ap.billType = (String)bean.get("bill_type");
                ap.abbreviation = (String) bean.get("abbreviation");
                ap.isPatientGroupAccessible = (String) bean.get("is_patient_group_accessible");
	 			slot.appointList.add(ap);

	 			if(!ap.appointStatus.equals("Cancel") && !ap.appointStatus.equals("Completed") &&
	 					!ap.appointStatus.equals("Noshow"))
	 				slot.completedOrCancelledCount+=1;

	 			List<BasicDynaBean> appointmentDetails =  ResourceDAO.getAppointmentDetails(appId);
	 			if(appointmentDetails!=null){
	 				BasicDynaBean detailsBean = null;
	 				AppointMentResource appiontResource = null;
	 				for(int i=0;i<appointmentDetails.size();i++){
	 					detailsBean = appointmentDetails.get(i);

	 					appiontResource = new AppointMentResource(appId,(String)detailsBean.get("resource_type"),
	 								(String)detailsBean.get("resource_id"));

	 					appiontResource.resourceName = (String)detailsBean.get("resourcename");
	 					ap.appointResourceList.add(appiontResource);
	 				}
	 			}//appointment details
 			}
 		}//slot checking
	}

	public static Map getAvaliabilityMap(ResourceCriteria rc, String schedleName, Integer centerId) throws Exception {
		Map availMap = null;
		List availabiltyList = new ArrayList();
		Date choosendate = rc.choosendate;
        Calendar cal = Calendar.getInstance();
        cal.setTime(choosendate);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) -1;
        String resourceType = null;
        if (rc.category.equals("DOC"))
			resourceType = rc.category;
		else if(rc.category.equals("DIA"))
			resourceType = "EQID";
		else if(rc.category.equals("SNP"))
			resourceType = "SRID";
		else if(rc.category.equals("OPE"))
			resourceType = "THID";

        availabiltyList = new ResourceDAO().getResourceAvailabilities(resourceType,choosendate,schedleName,null,centerId);

        if(availabiltyList != null && availabiltyList.size() < 1) {
        		availabiltyList = new ResourceDAO().getResourceDefaultAvailabilities(schedleName,dayOfWeek,resourceType,null,centerId);
        }

        if(availabiltyList != null && availabiltyList.size() < 1) {
        		availabiltyList = new ResourceDAO().getResourceDefaultAvailabilities("*",dayOfWeek,resourceType,null,centerId);
        }

        if(availabiltyList != null && availabiltyList.size() > 0) {
        	availMap = new HashMap();
        	availMap.put("availabiltyList", availabiltyList);
        }
        return availMap;
	}
	
	public boolean insertTimings(Connection con,BasicDynaBean bean,Time fromTime,Time toTime,String status,String remarks ,Integer centerId) throws Exception {
		boolean success = false;
		try {
			BasicDynaBean subBean = schResourceAvailabilityDetailsDAO.getBean();
			int resAvailDetId =  schResourceAvailabilityDetailsDAO.getNextSequence();
			subBean.set("res_avail_details_id",resAvailDetId);
			subBean.set("res_avail_id",bean.get("res_avail_id"));
			subBean.set("from_time",fromTime);
			subBean.set("to_time",toTime);
			subBean.set("availability_status",status);
			subBean.set("remarks",remarks);
			subBean.set("center_id",centerId);
			success = schResourceAvailabilityDetailsDAO.insert(con, subBean);
		} finally {
			DataBaseUtil.closeConnections(null, null);
		}
		return success;
	}


	public static void checkResourceUnAvailbilty(ArrayList<Slot>rows, Map availMap) throws SQLException{
		//status = 0 implies default color (grey) ; status=1 implies available color (white); status = 2 implies nonavailable color (brick)
		int status = 0;
		List availabilityList = null;
		BasicDynaBean availBean = null;
		java.sql.Time fromTime = null;
		java.sql.Time toTime = null;
		String availabilityStatus = null;
		String remarks = null;
		int rem = 0;

		if(availMap != null) {
			availabilityList = (List)availMap.get("availabiltyList");
			if (availabilityList != null && availabilityList.size() > 0) {
				for (int j=0; j<availabilityList.size();j++) {
					availBean = (BasicDynaBean)availabilityList.get(j);
					if (availBean != null) {
						availabilityStatus = (String)availBean.get("availability_status");
						if (remarks != null && availBean.get("remarks") != null &&  !remarks.equals((String)availBean.get("remarks"))) {
							rem = 0;
						}
						if ((String)availBean.get("remarks") != null)
							remarks = (String)availBean.get("remarks");

						fromTime = (java.sql.Time)availBean.get("from_time");
						toTime   = (java.sql.Time)availBean.get("to_time");
						
						if (availabilityStatus != null && availabilityStatus.equals("N")) {
							for(int i=0; i< rows.size(); i++) {
								Slot s = rows.get(i);

								if(fromTime !=null && toTime !=null && !fromTime.equals(toTime)){
					            	if(s.time.equals(fromTime) ||
					            		(s.time.after(fromTime) && s.time.before(toTime))){
					            		//s.setResource_unavail_center_id((Integer) availBean.get("center_id"));
					            		status = 2;
					            		s.availble = status;
					            		s.unavailableRemarks = remarks;
					            		rem++;

					            		if (remarks != null && remarks.equals(s.unavailableRemarks) && rem > 1) {
											s.unavailableRemarks = "";
											remarks = "";
										}
					            	}
					            }
							}
						}
					}
				}
			}
		}
	}
//To fill the availability center id of resource in each slot
	public static void checkResourceAvailbilty(ArrayList<Slot>rows, Map availMap) throws SQLException{
		int status = 0;
		List availabilityList = null;
		BasicDynaBean availBean = null;
		java.sql.Time fromTime = null;
		java.sql.Time toTime = null;
		String availabilityStatus = null;
		String remarks = null;
		int rem = 0;

		if(availMap != null) {
			availabilityList = (List)availMap.get("availabiltyList");
			if (availabilityList != null && availabilityList.size() > 0) {
				for (int j=0; j<availabilityList.size();j++) {
					availBean = (BasicDynaBean)availabilityList.get(j);
					if (availBean != null) {
						availabilityStatus = (String)availBean.get("availability_status");
						if (remarks != null && availBean.get("remarks") != null &&  !remarks.equals((String)availBean.get("remarks"))) {
							rem = 0;
						}
						if ((String)availBean.get("remarks") != null)
							remarks = (String)availBean.get("remarks");

						fromTime = (java.sql.Time)availBean.get("from_time");
						toTime   = (java.sql.Time)availBean.get("to_time");
						
						if(availabilityStatus != null && availabilityStatus.equals("A")){
							for(int i=0; i< rows.size(); i++){
								Slot s = rows.get(i);
								//s.setResource_availabilty_center_id((Integer) availBean.get("center_id"));
								if(fromTime !=null && toTime !=null && !fromTime.equals(toTime)){
					            	if(s.time.equals(fromTime) ||
					            		(s.time.after(fromTime) && s.time.before(toTime))){
					            		
					            		s.setResource_availabilty_center_id((Integer) availBean.get("center_id"));
					            	}
					            }
					        }
						}
						
						Integer resAvailCenter = (Integer)availBean.get("center_id");
						//set center_id if for not available slots if exist when select as specific center
						if(availabilityStatus != null && availabilityStatus.equals("N") && resAvailCenter != null){
							for(int i=0; i< rows.size(); i++){
								Slot s = rows.get(i);
								//s.setResource_availabilty_center_id((Integer) availBean.get("center_id"));
								if(fromTime !=null && toTime !=null && !fromTime.equals(toTime)){
					            	if(s.time.equals(fromTime) ||
					            		(s.time.after(fromTime) && s.time.before(toTime))){
					            		s.setResource_unavail_center_id((Integer) availBean.get("center_id"));
					            	}
					            }
					        }
						}
					}
				}
			}
		}
	
	}

	public static Schedule getResourceList(String category,String scheduleName,String scheduleDate)throws SQLException,ParseException{
		   Schedule s =null;

		   List<BasicDynaBean> list = ResourceDAO.getResourceList(category, scheduleName);
		// List<BasicDynaBean> nonavllist = new ResourceDAO().getResourceNonAvailablity(scheduleName,scheduleDate,null);


		   if(list!=null && list.size()>0){
			   BasicDynaBean bean = list.get(0);

			   java.sql.Time scheduleStartTime = new Time(DataBaseUtil.parseTime("00:00").getTime());
			   java.sql.Time scheduleEndTime = new Time(DataBaseUtil.parseTime("23:59").getTime());

			   s = new Schedule((Integer)bean.get("res_sch_id"),scheduleStartTime,
					   scheduleEndTime,(Integer)bean.get("default_duration"),
					   (String)bean.get("description"));

			   int defaultDuration = (Integer)bean.get("default_duration");
			   int scheduleId = (Integer)bean.get("res_sch_id");

			   long slotDuration = (defaultDuration*60*1000);

			   String dateStr = DataBaseUtil.getStringValueFromDb("select to_char(current_timestamp,'dd-MM-yyyy')");

			   SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			   String timeStr = DataBaseUtil.getStringValueFromDb("select to_char(current_timestamp,'hh24:mi:ss')");
			   java.util.Date dt  = timeFormat.parse(timeStr);

			   long current_time = dt.getTime();

			   if(category.equals("DOC")) {
				   if(scheduleStartTime != null && scheduleEndTime != null) {
					   Long startTime =   scheduleStartTime.getTime();
					   Long endTime = scheduleStartTime.getTime();
						for(long j=startTime.longValue();j<endTime.longValue();j=j+slotDuration){
							//	exclude non available time
							s.timeList.add(new java.sql.Time(j));
						}
				   }
			   }else {
				   if(scheduleStartTime != null && scheduleEndTime != null) {
					   Long startTime =   scheduleStartTime.getTime();
					   Long endTime = scheduleEndTime.getTime();
					   for(long j=startTime.longValue();j<endTime.longValue();j=j+slotDuration){
						   s.timeList.add(new java.sql.Time(j));
						}
				   }
			   }
				Resource r =null;
				for(int i=0;i<list.size();i++){
					BasicDynaBean rbean = list.get(i);
					r = new Resource(scheduleId,(String)rbean.get("resource_type"),(String)rbean.get("resource_id"));
					s.resourceList.add(r);
				}
		   }
		   return s;
	   }

   public static Schedule getResourceAndTimingList(String category,String scheduleName,String scheduleDate,String startTime,String endTime,String resourceType,Integer centerId)throws Exception{
	   Schedule s =null;
	   Long fromTime = new Long(startTime);
	   Long toTime = new Long(endTime);
	   int defaultDuration;
    if (resourceType != null && (resourceType.toString().equals("SER")
        || resourceType.toString().equals("TST") || resourceType.toString().equals("SUR"))) {
      defaultDuration = ResourceDAO.getSecondaryDefaultDuration(resourceType, scheduleName);
      s = new Schedule(centerId, null, null, (Integer) defaultDuration, null);
      return s;
    }
	   List<BasicDynaBean> list = ResourceDAO.getResourceListByResourceType(resourceType, scheduleName);
	   List<BasicDynaBean> nonavllist = new ResourceDAO().getResourceNonAvailablity(scheduleName,scheduleDate,"A",resourceType,centerId);
	   Date d = DataBaseUtil.parseDate(scheduleDate);
	   Calendar cal = Calendar.getInstance();
	   cal.setTime(d);
	   int weekDayNo = (cal.get(Calendar.DAY_OF_WEEK)-1);
	   if (nonavllist != null && nonavllist.size() < 1) {
		   nonavllist = new ResourceDAO().getResourceDefaultAvailabilities(scheduleName, weekDayNo, resourceType,null,centerId);
	   }
	   if (nonavllist != null && nonavllist.size() < 1) {
		   nonavllist = new ResourceDAO().getResourceDefaultAvailabilities("*", weekDayNo, resourceType,null,centerId);
	   }


	   if(list!=null && list.size()>0){
		   BasicDynaBean bean = list.get(0);

		   java.sql.Time scheduleStartTime = new Time(fromTime);
		   java.sql.Time scheduleEndTime = new Time(toTime);

		   s = new Schedule((Integer)bean.get("res_sch_id"),scheduleStartTime,
				   scheduleEndTime,(Integer)bean.get("default_duration"),
				   (String)bean.get("description"));

		   defaultDuration = (Integer)bean.get("default_duration");
		   int scheduleId = (Integer)bean.get("res_sch_id");
		   long slotDuration = (defaultDuration*60*1000);

		   if(nonavllist!=null && nonavllist.size()>0) {
			   for(int i=0;i<nonavllist.size();i++) {
					BasicDynaBean nonavlbean = nonavllist.get(i);
					String time_status = (String)nonavlbean.get("availability_status");
					Long resource_from_time = (Long)((Time)nonavlbean.get("from_time")).getTime();
					Long resource_to_time = (Long)((Time)nonavlbean.get("to_time")).getTime();

					//filtering the timlist with primary resources max and min time.

				   if(resource_from_time != null && resource_to_time != null && !resource_from_time.equals(resource_to_time) && !time_status.equals("N")) {
						for(long j=resource_from_time;j<resource_to_time;j=j+slotDuration){
							//	exclude non available time
							s.timeList.add(new java.sql.Time(j));
						}
				   }
			   }
		   }
		   ArrayList<Time> tempTimeList = new ArrayList<Time>();
		   if(fromTime != null && toTime != null) {
			   for(int k=0;k<s.timeList.size();k++) {
				   Time time = (Time)s.timeList.get(k);
				   if(time.getTime() >= toTime) {
					   break;
				   }
				   if(time.getTime() < fromTime) {
				   } else {
					   tempTimeList.add(time);
				   }
			   }
			   s.timeList = tempTimeList;
			   tempTimeList = null;
		   }
			Resource r =null;
			for(int i=0;i<list.size();i++){
				BasicDynaBean rbean = list.get(i);
				r = new Resource(scheduleId,(String)rbean.get("resource_type"),(String)rbean.get("resource_id"));
				s.resourceList.add(r);
			}
	   }
	   return s;
   }

   public static List getPrimaryResourceUnavailTimings(String category,String scheduleName,String scheduleDate)throws Exception{
	   String resourceType = null;
	   if (category.equals("DOC"))
			resourceType = category;
	   else if(category.equals("DIA"))
		resourceType = "EQID";
	   else if(category.equals("SNP"))
		resourceType = "SRID";
	   else if(category.equals("OPE"))
			resourceType = "THID";

	   List<BasicDynaBean> nonavllist = new ResourceDAO().getResourceNonAvailablity(scheduleName,scheduleDate,null,resourceType,null);
	   Date d = DataBaseUtil.parseDate(scheduleDate);
	   Calendar cal = Calendar.getInstance();
	   cal.setTime(d);
	   int weekDayNo = (cal.get(Calendar.DAY_OF_WEEK)-1);
	   if (nonavllist != null && nonavllist.size() < 1) {
		   nonavllist = new ResourceDAO().getResourceDefaultAvailabilities(scheduleName, weekDayNo, resourceType,null,null);
	   }
	   if (nonavllist != null && nonavllist.size() < 1) {
		   nonavllist = new ResourceDAO().getResourceDefaultAvailabilities("*", weekDayNo, resourceType,null,null);
	   }
	   return nonavllist;
   }


	public static Schedule getTimingList(ResourceCriteria rc,String view)throws Exception{

		Map<String,Long> timingMap = new HashMap<String, Long>();
		String scheduleName = null;
		Schedule s = null;
		BasicDynaBean timebean =  ResourceDAO.getTimeDurations(rc);
		Calendar cal = Calendar.getInstance();
		cal.setTime(rc.choosendate);
		int weekDayNo = (cal.get(Calendar.DAY_OF_WEEK)-1);
		String resourceType = null;

		if (rc.category.equals("DOC")) {
			resourceType = "DOC";
		} else if (rc.category.equals("DIA")) {
			resourceType = "EQID";
		} else if (rc.category.equals("SNP")) {
			resourceType = "SRID";
		} else if (rc.category.equals("OPE")) {
			resourceType = "THID";
		}

		if(view.equals("DayView"))
			timingMap = getMaxFromAndMinToAppointmentTime(rc, resourceType, weekDayNo,view);
		else
			timingMap = getSchedulerWeeklyAppointmentTimingMap(rc);

		Long startTime = ((Long)timingMap.get("from_time"));
		Long endTime = ((Long)timingMap.get("to_time"));
		java.sql.Time scheduleStartTime = new Time(startTime);
		java.sql.Time scheduleEndTime = new Time(endTime);

		int defaultDuration = (Integer)timebean.get("default_duration");
		String description = (String)timebean.get("description");
		int scheduleId = (Integer)timebean.get("res_sch_id");

		s = new Schedule(scheduleId,scheduleStartTime,
				scheduleEndTime,defaultDuration,description);

		long slotDuration = (defaultDuration*60*1000);
		s.scheduleName = scheduleName;

		if(startTime != null && endTime != null && !startTime.equals(endTime)) {
			for(long j=startTime.longValue();j<endTime.longValue();j=j+slotDuration){
				s.timeList.add(new java.sql.Time(j));
			}
		}
		return s;
	}

	public boolean updateResourceDetails(int appointment_id, String status, List resourceInsertList,
			List resourceUpdateList,List resourceDeleteList,String userName) throws SQLException, ParseException {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = true;

		try {
			ResourceDAO rdao = new ResourceDAO(con);

			if (!resourceInsertList.isEmpty()) {
				success = rdao.insertResources(resourceInsertList);
			}
			if (!resourceUpdateList.isEmpty()) {
				success = rdao.updateResources(resourceUpdateList);
			}
			if (!resourceDeleteList.isEmpty()) {
				success = rdao.deleteResources(resourceDeleteList);
			}
			success = rdao.updateStatus(status,appointment_id,userName);

		} catch (SQLException e) {
			success = false;
			throw e;
		} finally {
			if (con != null) {
				if (success) {
					con.commit();
					con.close();
				} else {
					con.rollback();
					con.close();
				}
			}
		}
		return success;
	}

	public static String saveDoctorNonavailabilityDetails(List<TimingDTO> timingInsertList,
			List<TimingDTO> timingUpdateList, List<TimingDTO> timingDeleteList) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = false;
		String message = null;

		try {
			ResourceDAO rdao = new ResourceDAO(con);

			if (!timingInsertList.isEmpty()) {
				success = rdao.insertTiming(timingInsertList);
			}

			if (!timingUpdateList.isEmpty()) {
				success = rdao.updateTiming(timingUpdateList);
			}
			if (!timingDeleteList.isEmpty()) {
				success = rdao.deleteTiming(timingDeleteList);
			}
		} catch (SQLException e) {
			success = false;
			throw e;
		} finally {
			if (con != null) {
				if (success) {
					message = "Saved doctor non-availability timing successfully...";
					con.commit();
					con.close();
				} else {
					message = "Failed";
					con.rollback();
					con.close();
				}
			}
		}
		return message;
	}

	public static void addRecurrances(Map<String, ArrayList> recMap, RecurranceDTO recdto, String userName) throws SQLException {

		ArrayList scheduleAppointBeanList = (ArrayList)recMap.get("scheduleAppointBeanList");
		Appointments appt = (Appointments)scheduleAppointBeanList.get(0);

		Timestamp appointmentTime = (java.sql.Timestamp)appt.getAppointmentTime();

		ArrayList scheduleAppointItemBean = (ArrayList)recMap.get("scheduleAppointItemBean");

		ArrayList scheduleAppointItemBeanRecuured =(ArrayList)recMap.get("scheduleAppointItemBeanRecuured");

		int recurranceNo = recdto.getRecurrNo();
		int occuranceNo = 0;

		Calendar cal = Calendar.getInstance();
		cal.setTime(appointmentTime);

		if(recdto.getRepeatOption().equals("FOR")) {
			occuranceNo = recdto.getOccurrNo();

		}else if(recdto.getRepeatOption().equals("UNTIL") && recdto.getUntilDate() != null && !recdto.getUntilDate().equals("")) {

			java.util.Date untilDate = recdto.getUntilDate();
			java.util.Date apptDate = new java.util.Date(appointmentTime.getTime());

			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(apptDate);

			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(untilDate);

			if(recurranceNo > 0) {
				if(recdto.getRecurranceOption().equals("D")) {
					occuranceNo = daysBetween(cal1,cal2);
					occuranceNo = occuranceNo/recurranceNo;
				}else if(recdto.getRecurranceOption().equals("W")) {
					occuranceNo = weeksBetween(cal1,cal2);
					occuranceNo = occuranceNo/recurranceNo;
				}else if(recdto.getRecurranceOption().equals("M")) {
					occuranceNo = monthsBetween(cal1, cal2);
					occuranceNo = occuranceNo/recurranceNo;
				}else if(recdto.getRecurranceOption().equals("Y")) {
					occuranceNo = yearsBetween(cal1, cal2);
					occuranceNo = occuranceNo/recurranceNo;
				}
			}
		}

		if(recurranceNo > 0 && occuranceNo > 0) {

			if(recdto.getRecurranceOption().equals("D")) {

				for(int i=0;i<occuranceNo; i++) {

					int appointmentId = Integer.parseInt(ResourceDAO.getNextAppointMentId());
					cal.add (Calendar.DATE, recurranceNo);

					Timestamp nextAppointmenttime = new java.sql.Timestamp(cal.getTime().getTime());
					
					//setting waitList number
					 Integer waitlist = ResourceDAO.getOverbookCount(appt.getPrim_res_id(), nextAppointmenttime);
					setBeanList(scheduleAppointBeanList,scheduleAppointItemBean,scheduleAppointItemBeanRecuured,appt,appointmentId,nextAppointmenttime, userName,waitlist);
				}
			}else if(recdto.getRecurranceOption().equals("W")) {
				String[] week = recdto.getWeek();
				int daysForRec = (recurranceNo * 7) * occuranceNo;
				int skipToday = 0;
				for(int k=0;k<week.length;k++) {
					if (cal.get(Calendar.DAY_OF_WEEK) == Integer.parseInt(week[k])) {
						skipToday = 1;
						break;
					}
				}

				for(int k=0; k<week.length; k++) {
					int weekDay = Integer.parseInt(week[k]);
					for(int i=1; i<=daysForRec-skipToday; i++) {
						Calendar cl = Calendar.getInstance();
						cl.setTime(appointmentTime);
						cl.add (Calendar.DATE, i);
						int calDay = cl.get(Calendar.DAY_OF_WEEK);
						if(calDay == weekDay) {
							int appointmentId = Integer.parseInt(ResourceDAO.getNextAppointMentId());
							Timestamp nextAppointmenttime = new java.sql.Timestamp(cl.getTime().getTime());
							//setting waitList Number
							 Integer waitlist = ResourceDAO.getOverbookCount(appt.getPrim_res_id(), nextAppointmenttime);
							setBeanList(scheduleAppointBeanList, scheduleAppointItemBean,
							  scheduleAppointItemBeanRecuured, appt, appointmentId, nextAppointmenttime, userName,waitlist);

							if (recurranceNo > 1) {
								i = i+(recurranceNo-1) * 7;
							}
						}
					}
				}
			}else if(recdto.getRecurranceOption().equals("M")) {

				java.util.Date recurrDateAndTime = recdto.getRecurrDate();

				Calendar recMon = Calendar.getInstance();
				recMon.setTime(recurrDateAndTime);
				int monthDate = recMon.get(Calendar.DAY_OF_MONTH);
				int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

				if (dayOfMonth == monthDate) {
					cal.add (Calendar.MONTH, recurranceNo);
				}else {
					cal = recMon;
				}

				for(int i=0;i<occuranceNo; i++) {

					int appointmentId = Integer.parseInt(ResourceDAO.getNextAppointMentId());
					dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

					if(dayOfMonth > monthDate) {
						while (cal.get(Calendar.DAY_OF_MONTH)  != monthDate) {
					    	cal.add(Calendar.DATE, -1);
					    }
					}else if(dayOfMonth < monthDate) {
						while (cal.get(Calendar.DAY_OF_MONTH)  != monthDate) {
					    	cal.add(Calendar.DATE, 1);
					    }
					}else {}

					Timestamp nextAppointmenttime = new java.sql.Timestamp(cal.getTime().getTime());
					
					//setting waitlist
					Integer waitlist = ResourceDAO.getOverbookCount(appt.getPrim_res_id(), nextAppointmenttime);
					setBeanList(scheduleAppointBeanList,scheduleAppointItemBean,scheduleAppointItemBeanRecuured,appt,appointmentId,nextAppointmenttime,userName,waitlist);

					cal.add (Calendar.MONTH, recurranceNo);
				}

			}else if(recdto.getRecurranceOption().equals("Y")) {

				for(int i=0;i<occuranceNo; i++) {

					int appointmentId = Integer.parseInt(ResourceDAO.getNextAppointMentId());
					cal.add (Calendar.YEAR, recurranceNo);

					Timestamp nextAppointmenttime = new java.sql.Timestamp(cal.getTime().getTime());
				  //setting waitlist
          Integer waitlist = ResourceDAO.getOverbookCount(appt.getPrim_res_id(), nextAppointmenttime);
					setBeanList(scheduleAppointBeanList,scheduleAppointItemBean,scheduleAppointItemBeanRecuured,appt,appointmentId,nextAppointmenttime,userName,waitlist);
				}
			}
		}
	}

	 private static void setBeanList(ArrayList scheduleAppointBeanList, ArrayList scheduleAppointItemBean,ArrayList scheduleAppointItemBeanRecuured,
			 Appointments  appt, int appointmentId, Timestamp nextAppointmenttime, String userName, Integer waitList) throws SQLException {


		 Appointments app = new Appointments(appointmentId);

		 app.setAppointmentId(appointmentId);
		 app.setMrNo(appt.getMrNo());
		 app.setPatientName(appt.getPatientName());
		 app.setPhoneNo(appt.getPhoneNo());
		 app.setComplaint(appt.getComplaint());
		 app.setScheduleId(appt.getScheduleId());
		 app.setScheduleName(appt.getScheduleName());
		 app.setAppointStatus(appt.getAppointStatus());
		 app.setBookedBy(appt.getBookedBy());
		 app.setBookedTime(appt.getBookedTime());
		 app.setAppointmentDuration(appt.getAppointmentDuration());
		 app.setAppointmentTime(nextAppointmenttime);
		 app.setCenterId(appt.getCenterId());
		 app.setChangedBy(appt.getChangedBy());
		 app.setRemarks(appt.getRemarks());
		 app.setPrescDocId(appt.getPrescDocId());
		 app.setContactId(appt.getContactId());
		 app.setPrim_res_id(appt.getPrim_res_id());
		 if(appt.getResourceType().equals("DOC")) {
			 app.setConsultationTypeId(appt.getConsultationTypeId());
		 }

		 if(appt.getResourceType().equals("OPE")) {
			 app.setSchPriorAuthId(appt.getSchPriorAuthId());
			 app.setSchPriorAuthModeId(appt.getSchPriorAuthModeId());
		 }
		 app.setWaitlist(waitList);
		 scheduleAppointBeanList.add(app);
		 GenericDAO schedulerAppointmentItemsDAO = new GenericDAO("scheduler_appointment_items");

		 if(scheduleAppointItemBean.size() > 0) {
			Iterator itr = scheduleAppointItemBean.iterator();
			AppointMentResource apptres = null;
			java.sql.Timestamp modTime = DataBaseUtil.getDateandTime();
			while(itr.hasNext()) {
				AppointMentResource res = (AppointMentResource)itr.next();
				apptres = new AppointMentResource(appointmentId,res.getResourceType(),res.getResourceId());
				apptres.setAppointment_item_id(schedulerAppointmentItemsDAO.getNextSequence());
				apptres.setUser_name(userName);
				apptres.setMod_time(modTime);
				scheduleAppointItemBeanRecuured.add(apptres);
			}
		 }
	}

	public static int daysBetween(Calendar c1, Calendar c2){
	     //return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	     int dayCount = 0;
	     while(c1.before(c2)){
	       c1.add(Calendar.DATE, 1);
	       dayCount++;
	     }
	     return dayCount;
	 }

	 public static int weeksBetween(Calendar c1, Calendar c2){
	     //return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24 * 7));
	     int weekCount = 0;
	     while(c1.before(c2)){
	       c1.add(Calendar.WEEK_OF_YEAR, 1);
	       weekCount++;
	     }
	     return weekCount;
	 }

	 public static int monthsBetween(Calendar c1, Calendar c2){
	     int monthCount = 0;
	     while(c1.before(c2)){
	       c1.add(Calendar.MONTH, 1);
	       monthCount++;
	     }
	     return monthCount;
	 }

	 public static int yearsBetween(Calendar c1, Calendar c2){
	     int yearCount = 0;
	     while(c1.before(c2)){
	       c1.add(Calendar.YEAR, 1);
	       yearCount++;
	     }
	     return yearCount;
	 }


	public static boolean updateScheduler(Connection con, int appointmentId, String mrno, String patientId,
			BasicDynaBean patientbean, String ailment,String userName, int consultationTypeId, String remarks, String prescDocId, String screenName)
		throws SQLException, IOException {
		BasicDynaBean appbean = schedulerAppointmentsDAO.findByKey("appointment_id", appointmentId);
		String salutationName ="";
		BasicDynaBean salutationBean=null;
		
		
		if(patientbean.getMap().containsKey("salutation_id" ) && patientbean.getMap().containsKey("salutation" )){
			salutationName = (String)patientbean.get("salutation");
		}else{
			 salutationBean = new GenericDAO("salutation_master").findByKey("salutation_id", patientbean.get("salutation"));
			if(salutationBean !=null) {
				salutationName = (String)salutationBean.get("salutation");
			}
		}
		boolean status = false;
		Map<String,Object> fields = new HashMap<String,Object>();
		Map<String,Object> keys = new HashMap<String,Object>();
		fields.put("mr_no",mrno);
		fields.put("visit_id",patientId);
		fields.put("patient_name",((String)patientbean.get("patient_name"))+" "
				+	(patientbean.get("middle_name") != null ? (String)patientbean.get("middle_name") : "")+" "
				+   (patientbean.get("last_name") != null ? (String)patientbean.get("last_name") : "" ));
		String patientPhone = (String) patientbean.get("patient_phone");
		patientPhone = patientPhone == null ? "" : patientPhone;
		fields.put("patient_contact", !patientPhone.equals("") ? patientPhone : appbean.get("patient_contact"));
		fields.put("arrival_time",DataBaseUtil.getDateandTime());
		if(screenName.equals("Reg"))
			fields.put("appointment_status", "Arrived");
		fields.put("complaint", ailment);
		fields.put("consultation_type_id", consultationTypeId);
		fields.put("changed_by", userName);
		fields.put("presc_doc_id", (prescDocId != null && !prescDocId.equals("")) ? prescDocId : appbean.get("presc_doc_id"));
		fields.put("salutation_name", salutationName);
		if (remarks != null)
			fields.put("remarks", remarks);

		keys.put("appointment_id", appointmentId);
		int updateCount =  schedulerAppointmentsDAO.update(con, fields, keys);
		if(updateCount > 0 ) status = true;
		return status;
	 }

	public static boolean saveAppointmentAndresources(Connection con, ArrayList<Appointments> scheduleAppointBeanList,
				ArrayList<AppointMentResource> scheduleAppointItemBean,ArrayList<AppointMentResource>  scheduleAppointItemBeanRecuured) throws SQLException {
		boolean success = false;
		try {
			ResourceDAO rdao = new ResourceDAO(con);

			if (!scheduleAppointBeanList.isEmpty()) {
				success = rdao.insertAppointments(scheduleAppointBeanList);
			}
			if(success) {
				if (!scheduleAppointItemBean.isEmpty()) {
					success = rdao.insertAppointmentItems(scheduleAppointItemBean);
				}
			}
			if(success) {
				if (!scheduleAppointItemBeanRecuured.isEmpty()) {
					success = rdao.insertAppointmentItems(scheduleAppointItemBeanRecuured);
				}
			}
		} catch (SQLException e) {
			success = false;
			throw e;
		}
		return success;
	}
	
	public static boolean saveChannellingAppointmentAndresources(Connection con, ChannellingAppt appt,
			ArrayList<AppointMentResource> scheduleAppointItemBean) throws SQLException {
	    boolean success = false;
	    try {
		    ResourceDAO rdao = new ResourceDAO(con);

		    if (appt != null) {
			    success = rdao.insertChannellingAppointment(appt);
		    }
		    if(success) {
			    if (!scheduleAppointItemBean.isEmpty()) {
				    success = rdao.insertAppointmentItems(scheduleAppointItemBean);
			    }
		    }
	    } catch (SQLException e) {
		    success = false;
		    throw e;
	    }
	    return success;
    }	

	public boolean updateSchedulerResourceDetails(Connection con, List<ResourceDTO> resourceInsertList,
				List<ResourceDTO> resourceUpdateList, List<ResourceDTO> resourceDeleteList) throws SQLException {
		boolean success = true;
		ResourceDAO rdao = new ResourceDAO(con);

		if (!resourceDeleteList.isEmpty()) {
			success = rdao.deleteResources(resourceDeleteList);
		}
		if (!resourceInsertList.isEmpty()) {
			success = rdao.insertResources(resourceInsertList);
		}
		if (!resourceUpdateList.isEmpty()) {
			success = rdao.updateResources(resourceUpdateList);
		}
		return success;
	}

	 public static boolean updatePatientMrnoAndVisit(int appointmentId, String mrno, String visitId)
		throws SQLException, IOException {
		boolean success = false;
		Map fields = new HashMap();
		Map keys = new HashMap();
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if (visitId != null && !visitId.trim().equals("") && !visitId.equals("None")) {
				mrno = VisitDetailsDAO.getMrno(visitId);
				fields.put("visit_id", visitId);
			}

			if(mrno != null && !mrno.equals("")) {
				Map patientDetMap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
				fields.put("mr_no",mrno);
				fields.put("patient_name",patientDetMap.get("patient_name"));
				keys.put("appointment_id", appointmentId);
				int updateCount =  schedulerAppointmentsDAO.update(con, fields, keys);
				if(updateCount > 0 ) success = true;
				else success = false;
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	 }

	 public static boolean updateAppointments(Connection con,int appointmentId, String appointStatus,String cancelReason,String userName, int unique_appt_ind, Timestamp modTime)
		throws SQLException, IOException {
		 	boolean status = false;
			Map fields = new HashMap();
			Map keys = new HashMap();
		try {
			fields.put("appointment_status",appointStatus);
			if (cancelReason != null){
				fields.put("cancel_reason", cancelReason);
				fields.put("cancel_type", "Other");
			}
			fields.put("changed_by", userName);
			fields.put("unique_appt_ind", unique_appt_ind);
			fields.put("changed_time", modTime);
			keys.put("appointment_id", appointmentId);
			int updateCount =  schedulerAppointmentsDAO.update(con, fields, keys);
			if(updateCount > 0 ) status = true;
			else status = false;
		}finally {
			DataBaseUtil.closeConnections(null, null);
		}
		return status;
	 }
	 
	 public static boolean updateAppointments(Connection con, Map fields, Map keys) 
         throws SQLException, IOException {
		 boolean status = false;
		 try {
			 int updateCount =  schedulerAppointmentsDAO.update(con, fields, keys);
             if(updateCount >=0 ){
                 status = true;
             }
		 } finally {
			 DataBaseUtil.closeConnections(null, null);
		 }
		 return status;
	 }
	 public static boolean isAppointmentCompleted(Connection con,String category,int appointmentId) throws SQLException {
		 boolean appCompleted = false;
		 if (category != null && !category.equals("")) {
			 appCompleted = ResourceDAO.getAppointmentStatus(con,category,appointmentId);
		 }
		 return appCompleted;
	 }

	 public static boolean updateAppointmentStatus(Connection con,String category,int appointmentId,String userName) throws SQLException{
		 boolean success = false;
	     ResourceDAO resdao = new ResourceDAO(con);
	     BasicDynaBean apptBean = schedulerAppointmentsDAO.findByKey("appointment_id", appointmentId);
	     // Test, Service, Surgery
	     if (category.equals("DIA") || category.equals("SNP") || category.equals("OPE")) {
      		boolean conduction = ResourceDAO.getConductionForTestOrServiceOrOperation(
      							category,(String)apptBean.get("res_sch_name"));
          	 if (conduction) {
          		 success = ResourceBO.isAppointmentCompleted(con, category, appointmentId);

          		 if (success) {
	              	 success = resdao.updateStatus(ResourceDTO.APPT_COMPLETED_STATUS, appointmentId,userName);
          		 } else {
	              	 success = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,userName);
          		 }
          	 } else {
          		success = resdao.updateStatus(ResourceDTO.APPT_COMPLETED_STATUS, appointmentId,userName);
          	 }
      	} else {
      		success = ResourceBO.isAppointmentCompleted(con, category, appointmentId);

      		if (success) {
      			success = resdao.updateStatus(ResourceDTO.APPT_COMPLETED_STATUS, appointmentId,userName);
      		} else {
          		success = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,userName);
      		}
      	}
	    return success;
	 }

	 public static Timestamp getAppointmntEndTime(Timestamp appointDate,int duration) throws Exception {
		 long appointTime = appointDate.getTime();
		 long durationlong = duration*60*1000;
		 long completedTime = appointTime+durationlong;
		 Timestamp appointEndDateTimestamp = new Timestamp(completedTime);
		 return appointEndDateTimestamp;
	 }

	 public static Map order(Connection con,int appointmentId,String visitId,
			 						String category,String userName,boolean conduction,Timestamp appointmentTime) throws Exception {
		 Map resultMap = new HashMap();
		 String billNo = null;
		 String visitType = "";
		 Timestamp arrivalTime = DateUtil.getCurrentTimestamp();
		 boolean isVisitActive = false;
			if (visitId != null && !visitId.equals("")) {
				isVisitActive = VisitDetailsDAO.isVisitActive(con, visitId);
			}
		 boolean result = true;
			Bill bill = BillDAO.getVisitCreditBill(visitId, true);
			if (null != bill) {
				BillBO billbo = new BillBO();
				if (billbo.isMultiVisitBill(bill.getBillNo())) {
					bill = null;
				}
			}
			Timestamp dtTime = DataBaseUtil.getDateandTime();

			if (bill != null && bill.getPaymentStatus().equals("U")) {
				billNo = bill.getBillNo();
			}
			BasicDynaBean activePatientBean = new VisitDetailsDAO().findByKey("patient_id", visitId);
			if (activePatientBean != null)
				visitType = (String)activePatientBean.get("visit_type");

			List<BasicDynaBean> resourcelist = ResourceDAO.getAppointmentDetails(con,appointmentId);
			if (resourcelist == null || resourcelist.size() == 0) {
				result = false;
			}
			BasicDynaBean rbean = resourcelist.get(0);
	 		if (category == null) {
	 			result = false;
	 		}

	 		List<String> newPreAuths = new ArrayList<String>();
			List<Integer> newPreAuthModes = new ArrayList<Integer>();
			List<String> newSecPreAuths = new ArrayList<String>();
			List<Integer> newSecPreAuthModeIds = new ArrayList<Integer>();
			List<String> condDoctrsList = new ArrayList<String>();

	 		OrderBO orderBo = new OrderBO();
	 		BasicDynaBean bean = null;
			if (category.equals("DOC")) {
				GenericDAO docDao = new GenericDAO("doctor_consultation");
				String doctorId = (String)rbean.get("prim_res_id");
				String presDocId = (String)rbean.get("presc_doc_id");
				String condDocId = (String) rbean.get("cond_doc_id");
				if (doctorId != null && !doctorId.equals("")
						&& rbean.get("consultation_type_id") != null
						&& ((Integer)rbean.get("consultation_type_id")).intValue() != 0) {
					bean = docDao.getBean();

					bean.set("doctor_name", doctorId);
					bean.set("presc_date", dtTime);
					bean.set("presc_doctor_id", presDocId);
					if(appointmentTime.getTime() < arrivalTime.getTime())
						bean.set("visited_date", arrivalTime);
					else
						bean.set("visited_date", appointmentTime);
					bean.set("remarks", "Scheduler Consultation");
					/*if (visitType.equals("o"))
						bean.set("head", "-1");
					else if (visitType.equals("i"))
						bean.set("head", "-3");*/
					bean.set("head", new String((rbean.get("consultation_type_id")).toString()));
					bean.set("status", "A");
					bean.set("appointment_id", appointmentId);
					orderBo.setBillInfo(con, visitId, billNo, false, userName);
					billNo = (orderBo.getBill() != null) ? (String)((BasicDynaBean)orderBo.getBill()).get("bill_no") : null;if (condDocId !=null && !condDocId.equals("")){
						condDoctrsList.add(condDocId);
					}
				}
			} else if (category.equals("SNP")) {
				String serviceId = (String)rbean.get("res_sch_name");
				String presDocId = (String)rbean.get("presc_doc_id");
				String condDocId = (String) rbean.get("cond_doc_id");
				if (serviceId != null && !serviceId.equals("")) {
					bean = servicesPrescribedDAO.getBean();

					bean.set("service_id",serviceId);
					bean.set("presc_date",dtTime);
					if (!conduction) {
						bean.set("conducted", "C");
					} else {
						bean.set("conducted", "N");
					}
					bean.set("doctor_id",presDocId);
					bean.set("remarks","Scheduler Service");
					bean.set("appointment_id", appointmentId);
					bean.set("quantity", BigDecimal.ONE);
					orderBo.setBillInfo(con, visitId, billNo, false, userName);
					billNo = (orderBo.getBill() != null) ? (String)((BasicDynaBean)orderBo.getBill()).get("bill_no") : null;
					if (condDocId !=null && !condDocId.equals("")){
						condDoctrsList.add(condDocId);
					}
				}
			} else if (category.equals("DIA")) {
				String testId = (String)rbean.get("res_sch_name");
				String presDocId = (String)rbean.get("presc_doc_id");
				String condDocId = (String) rbean.get("cond_doc_id");
				if (testId != null && !testId.equals("")) {
					bean = testsPrescribedDAO.getBean();

					bean.set("test_id", testId);
					bean.set("pres_date", dtTime);
					if (!conduction) {
						bean.set("conducted", "C");
					} else {
						bean.set("conducted", "N");
					}
					bean.set("pres_doctor", presDocId);
					bean.set("remarks", "Scheduler Test");
					bean.set("priority","R");
					bean.set("appointment_id", appointmentId);
					orderBo.setBillInfo(con, visitId, billNo, false, userName);
					billNo = (orderBo.getBill() != null) ? (String)((BasicDynaBean)orderBo.getBill()).get("bill_no") : null;
					if (condDocId !=null && !condDocId.equals("")){
						condDoctrsList.add(condDocId);
					}
						
				}
			} else if (category.equals("OPE")) {
				bean = ResourceBO.setOperationOrderDetails(con, appointmentId, visitId, bean);
				orderBo.setBillInfo(con, visitId, billNo, false, userName);
				billNo = (orderBo.getBill() != null) ? (String)((BasicDynaBean)orderBo.getBill()).get("bill_no") : null;
				String priorAuthNoForOperation = "";
				int priorAuthModeId = 0;
				BasicDynaBean apptBean = schedulerAppointmentsDAO.findByKey("appointment_id", appointmentId);
				String condDocId = (String) rbean.get("cond_doc_id");
				if (condDocId !=null && !condDocId.equals("")){
					condDoctrsList.add(condDocId);
				}
				if(isVisitActive) {
					if (apptBean != null) {
						priorAuthNoForOperation = (String)apptBean.get("scheduler_prior_auth_no");
						priorAuthModeId = (apptBean.get("scheduler_prior_auth_mode_id") != null) ?
									(Integer)apptBean.get("scheduler_prior_auth_mode_id") : 0;
						if(priorAuthNoForOperation != null && !priorAuthNoForOperation.equals("") &&
								priorAuthModeId != 0) {
						} else {
							priorAuthNoForOperation = (activePatientBean.get("prior_auth_id") != null) ?
										(String)activePatientBean.get("prior_auth_id") : "";
							priorAuthModeId = (activePatientBean.get("prior_auth_mode_id")!= null) ?
										(Integer)activePatientBean.get("prior_auth_mode_id") : 1;
						}
					}
				} else {
					if (apptBean != null) {
						priorAuthNoForOperation = (String)apptBean.get("scheduler_prior_auth_no");
						priorAuthModeId = (Integer)apptBean.get("scheduler_prior_auth_mode_id");
					}
				}
				if (priorAuthNoForOperation != null && !priorAuthNoForOperation.equals("")) {
					newPreAuths.add(priorAuthNoForOperation);
				}

				if (priorAuthModeId != 0) {
					newPreAuthModes.add(priorAuthModeId);
				}
 			}


			if (billNo == null) {
				resultMap.put("result", true);
				return resultMap;
			}

			List<BasicDynaBean> orders = new ArrayList<BasicDynaBean>();

			if (bean != null)
				orders.add(bean);

			if (orders.size() == 0) {
				result = false;
			}
			List<String> firstOfCategoryList = new ArrayList<String>();
			
			result  = (orderBo.orderItems(con, orders,newPreAuths,newPreAuthModes,firstOfCategoryList,
					condDoctrsList,null, null, null, null, appointmentId, true, newSecPreAuths, newSecPreAuthModeIds, null,
					new ArrayList<List<TestDocumentDTO>>()) == null);

		resultMap.put("billNo", billNo);
		resultMap.put("result", result);
		return resultMap;
	 }

	 public static Map checkAndOrderItems(Connection con,String mrno, String visitId, String category,
			 	int appointmentId,String userName,Date appointmentDate, String markArrived,String schdlName,Timestamp appointmentTime)throws Exception {
		 boolean status = true;
		 schdlName = (schdlName == null || schdlName.equals("")) ? null : schdlName;
		 Map resultMap = new HashMap();

		 do {
			ResourceDAO resdao = new ResourceDAO(con);

			if(mrno != null && !mrno.equals("")) {

				boolean isVisitActive = false;
				if (visitId != null) {
					isVisitActive = VisitDetailsDAO.isVisitActive(con, visitId);
				}

				// Visit is inactive
				if (!isVisitActive) {
					status = resdao.updateVisitId(null, appointmentId);
					if (!status) break;

				} else {
					// Visit is active
					BasicDynaBean apptBean = schedulerAppointmentsDAO.findByKey(con, "appointment_id", appointmentId);

					BasicDynaBean activePatientBean = new VisitDetailsDAO().findByKey("patient_id", visitId);
					String visitType = (String)activePatientBean.get("visit_type");


					// Update visit id if visit is active.
					status = resdao.updateVisitId(visitId, appointmentId);
					if (!status) break;

					// Doctor Consultation
					if (category.equals("DOC")) {
						// Patient is active (for OP registration date and appointment date is same) then existing visit.
						// So order and mark as arrived and update visit id when user marks as arrived.
						// Otherwise only update the visit id.

						if (markArrived != null && markArrived.equals("on")) {
							resultMap = order(con,appointmentId, visitId, category, userName, true,appointmentTime);
							status = (resultMap != null && resultMap.get("result") != null) ? (Boolean)resultMap.get("result") : false;
							if (!status) break;

							status = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,userName);
							if (!status) break;
						}

					 // Test & Services. updation of visistid is same as doctor for test and services.
					} else if (category.equals("DIA") || category.equals("SNP")) {
						boolean conduction = ResourceDAO.getConductionForTestOrServiceOrOperation(
									category, (String)apptBean.get("res_sch_name"));
						if (markArrived != null && markArrived.equals("on")) {
							if (schdlName != null) {
								resultMap = updateAndOrder(con,visitId, appointmentId,category,userName,conduction,appointmentTime);
								status = (resultMap != null && resultMap.get("result") != null) ? (Boolean)resultMap.get("result") : false;
								if (!status) break;
							} else {
								new ResourceDAO().updateStatus(con, ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,userName);
							}
						}

					} else if (category.equals("OPE")) {
						// Surgery
						BasicDynaBean genPrefs =  GenericPreferencesDAO.getAllPrefs();
						String opApplicableFor =  (String)genPrefs.get("operation_apllicable_for");
						boolean conduction = ResourceDAO.getConductionForTestOrServiceOrOperation(
											category, (String)apptBean.get("res_sch_name"));

						opApplicableFor = opApplicableFor.equals("b") ? visitType :  opApplicableFor;

						if (markArrived != null && markArrived.equals("on")) {
							if ((visitType.equals("i") && opApplicableFor.equals("i")) || (visitType.equals("o") && opApplicableFor.equals("o"))) {
								if (schdlName != null) {
									resultMap = updateAndOrder(con,visitId, appointmentId,category,userName,conduction,appointmentTime);
									status = (resultMap != null && resultMap.get("result") != null) ? (Boolean)resultMap.get("result") : false;
									if (!status) break;
								} else {
									new ResourceDAO().updateStatus(con, ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,userName);
								}
							}else {
								status = false;
								if (!status) break;
							}
						}
					}
				}
			}// mrno check

		 }while(false);

		resultMap.put("result", status);
		resultMap.put("billNo", (resultMap != null && resultMap.get("billNo") != null) ? (String)resultMap.get("billNo") : null);
		return resultMap;
	 }
	 //update visitId in schedulerappointments table,order the item,update scheduler status and check the conduction
	 //for test,services and operation.
	 private static Map updateAndOrder(Connection con, String visitId, int appointmentId,
			 					String category, String userName, boolean conduction,Timestamp appointmentTime) throws Exception {
		 boolean status = false;
		 Map resultMap = new HashMap();
		 ResourceDAO resdao = new ResourceDAO(con);
		 do {
			resultMap = order(con,appointmentId, visitId, category, userName, conduction,appointmentTime);
			status = (resultMap != null && resultMap.get("result") != null) ? (Boolean)resultMap.get("result") : false;
			if (!status) break;

			status = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,userName);
			if (!status) break;

			if (!conduction) {
				status = updateTestOrServiceOrOperationStatus(appointmentId,category);
				if (!status) break;

				status = resdao.updateStatus(ResourceDTO.APPT_COMPLETED_STATUS, appointmentId,userName);
				if (!status) break;
			}
		 } while(false);

		resultMap.put("result", status);
		resultMap.put("billNo", (resultMap != null && resultMap.get("billNo") != null) ? (String)resultMap.get("billNo") : null);
		return resultMap;
	}

	public static BasicDynaBean setOperationOrderDetails(Connection con, int appointmentId, String visitId,
			 				BasicDynaBean bean) throws Exception {

		Map patientDetMap = VisitDetailsDAO.getPatientVisitDetailsMap(con, visitId);
		GenericDAO opdao = new GenericDAO("bed_operation_schedule");
		BasicDynaBean apptBean = schedulerAppointmentsDAO.findByKey(con,"appointment_id", appointmentId);
		boolean conduction = ResourceDAO.getConductionForTestOrServiceOrOperation("OPE", (String)apptBean.get("res_sch_name"));
		bean = opdao.getBean();
		Timestamp endDateTime = null;
		String deptId = null;
 		List<BasicDynaBean> baseOpBean = ResourceDAO.getOperationDetails(con,appointmentId);
 		if (patientDetMap != null) {
	 		deptId = (String)patientDetMap.get("dept_id");
	 		if (deptId == null || deptId.equals("")) {
	 			deptId = (String)patientDetMap.get("admitted_dept");
	 		}
 		}
 		for (int i=0 ;i<baseOpBean.size();i++) {
 			if (baseOpBean.get(i).get("resource_type").equals("Operation Theatre")) {
 				String OpId = (String)baseOpBean.get(i).get("res_sch_name");
 				String prescDocId = (String)baseOpBean.get(i).get("presc_doc_id");
 				Timestamp appointDateTime = (Timestamp)baseOpBean.get(i).get("appointment_time");
					int duration = (Integer)baseOpBean.get(i).get("duration");
					endDateTime = ResourceBO.getAppointmntEndTime(appointDateTime,duration);
 				if (OpId != null && !OpId.equals("")) {
 					bean.set("operation_name", OpId);
 					bean.set("mr_no", (String)patientDetMap.get("mr_no"));
 					bean.set("patient_id", (String)patientDetMap.get("patient_id"));
 					bean.set("consultant_doctor", prescDocId);
 					bean.set("theatre_name", (String)baseOpBean.get(i).get("booked_resource_id"));
 					if (!conduction) {
						bean.set("status", "C");
					} else {
						bean.set("status", "A");
					}
 					bean.set("appointment_id", appointmentId);
 					bean.set("prescribed_id", -1);
 					bean.set("hrly", "checked");
 					bean.set("start_datetime", (Timestamp)baseOpBean.get(i).get("appointment_time"));
 					bean.set("end_datetime", endDateTime);
 					bean.set("prescribed_date", (Timestamp)baseOpBean.get(i).get("booked_time"));
 					bean.set("remarks","Scheduler Operation");
 					bean.set("finalization_status","N");
 					if (deptId != null && !deptId.equals("")) {
 						bean.set("department",deptId);
 					}
 				}
 			} else if (baseOpBean.get(i).get("resource_type").equals("Surgeon")) {
 				bean.set("surgeon", (String)baseOpBean.get(i).get("booked_resource_id"));
 			} else if (baseOpBean.get(i).get("resource_type").equals("Anesthetist")) {
 				bean.set("anaesthetist", (String)baseOpBean.get(i).get("booked_resource_id"));
 			}
 		}
 		return bean;
	 }

	public static String validateScheduler(ResourceCriteria rc) throws Exception{
		String resourcesCommaSeparatedStr = null;
		String isOrAre = "is";
		String resType = null;
		String errorMsg = null;
		if(rc.category.equals("DOC"))
			resType = "DOC";
		else if (rc.category.equals("SNP"))
			resType = "SRID";
		else if (rc.category.equals("OPE"))
			resType = "THID";
		else if (rc.category.equals("DIA"))
			resType = "EQID";

		BasicDynaBean minBean = ResourceDAO.getTimeDurations(rc);
		Integer mDuration = (Integer)minBean.get("default_duration");
		for(int i=0;i<rc.scheduleName.size();i++) {
			String resourceId = rc.scheduleName.get(i);
			BasicDynaBean defaultResourceBean = ResourceDAO.getDefaultAttributesOfResource(rc.category, resourceId);
			if(defaultResourceBean != null && defaultResourceBean.get("default_duration") != null) {
				Integer dDuration = (Integer)defaultResourceBean.get("default_duration");
				if((dDuration >= mDuration || dDuration <= mDuration) && dDuration % mDuration != 0) {
					resourcesCommaSeparatedStr = null == resourcesCommaSeparatedStr ? "" : resourcesCommaSeparatedStr;
					resourcesCommaSeparatedStr = resourcesCommaSeparatedStr.isEmpty() ?
							resourcesCommaSeparatedStr.concat(ResourceDAO.getResourceName(resType, resourceId)) :
							resourcesCommaSeparatedStr.concat(", ").concat(ResourceDAO.getResourceName(resType, resourceId));
				}

			}
		}
		if(resourcesCommaSeparatedStr != null) {
			String resArr[] = resourcesCommaSeparatedStr.split(",");
			isOrAre = (resArr != null && resArr.length > 1) ? "are" : isOrAre;
			errorMsg = "Default Duration setting for "+resourcesCommaSeparatedStr+" "+isOrAre+ "  incorrect.Please correct it";
		}
		return errorMsg;
	}
	
	public static BasicDynaBean getCancelBean(String type, String prescribedId, String userName)
			throws SQLException {
			BasicDynaBean b = null;
			int prescIdInt = Integer.parseInt(prescribedId);

			if (type.equals("DIA")) {
				b = testsPrescribedDAO.getBean();
				b.set("prescribed_id", prescIdInt);
				b.set("conducted", "X");
				b.set("cancelled_by", userName);
				b.set("cancel_date", DateUtil.getCurrentDate());

			} else if (type.equals("SER")) {
				b = servicesPrescribedDAO.getBean();
				b.set("prescription_id", prescIdInt);
				b.set("conducted", "X");
				b.set("cancelled_by", userName);
				b.set("cancel_date", DateUtil.getCurrentDate());

			}
			return b;
	}
	
	//filter doctor resource availability time based on  centerwise 
	public static List<BasicDynaBean> filterAllResourcesAvailability(Integer userCenter, List<BasicDynaBean> resourceAvailabilityList,Integer centerId) throws Exception {
		List<BasicDynaBean> filteredResourceList = new ArrayList<BasicDynaBean>();
		if(resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
			for(BasicDynaBean resBean : resourceAvailabilityList) {
        	 //checking not available slots and adding to list
				if(centerId != null){	
					if ((userCenter.intValue() == 0 && centerId.intValue() == 0)) {
	    			
	    				if (resBean.get("availability_status").equals("N")) {
	    					filteredResourceList.add(resBean);
	    				} else {
	    					if (resBean.get("availability_status").equals("A")) {
	    						filteredResourceList.add(resBean);
	    					}
	    				 }	
				}else if((userCenter.intValue() == 0 && centerId.intValue() != 0)) {
					if (resBean.get("availability_status").equals("N")) {
	    				resBean.set("availability_status", "N");
	    				filteredResourceList.add(resBean);
	    			} else {
	    				if (resBean.get("center_id").equals(0)) {
	    					filteredResourceList.add(resBean);
	    				} else if (centerId.intValue() != (Integer)resBean.get("center_id")) {
	    					resBean.set("availability_status", "N");
	        				filteredResourceList.add(resBean);
	    				} else {
	    					filteredResourceList.add(resBean);
	    				}
	    			}
				}else {
	    			if (resBean.get("availability_status").equals("N")) {
	    				resBean.set("availability_status", "N");
	    				filteredResourceList.add(resBean);
	    			} else {
	    				if (resBean.get("center_id").equals(0)) {
	    					filteredResourceList.add(resBean);
	    				} else if (userCenter.intValue() != (Integer)resBean.get("center_id")) {
	    					resBean.set("availability_status", "N");
	        				filteredResourceList.add(resBean);
	    				}else {
	    					filteredResourceList.add(resBean);
	    				}
	    			}
	    		}
			}//else return null;
    	}
	}
    return filteredResourceList; 
  }

	public static void sendSMS(int appointmentId, String status ,String eventId, boolean asynchronous) throws SQLException, ParseException, IOException  {
		BasicDynaBean modBean = new GenericDAO("modules_activated").findByKey("module_id", "mod_messaging");
		if (modBean != null && modBean.get("activation_status") != null
				&& modBean.get("activation_status").equals("Y")) {
		MessageManager mgr = new MessageManager();
		Map appointmentData = new HashMap();
		appointmentData.put("appointment_id", appointmentId);
		appointmentData.put("status", status);
		mgr.processEvent(eventId, appointmentData,asynchronous);
		}
	}

	public static boolean updateTestOrServiceOrOperationStatus(int appId, String category)
		throws Exception {
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = true;
		try {
			if (category.equals("SNP")) {
				String serviceId =
					(String) schedulerAppointmentsDAO.findByKey("appointment_id", appId).get("res_sch_name");
				boolean conductionApplicable =
					(boolean) servicesDAO.findByKey("service_id", serviceId).get("conduction_applicable");
				success = new ResourceDAO(con)
					.updateTestOrServiceOrOperationStatus(appId, category, conductionApplicable ? "C" : "U");
			} else {
				success = new ResourceDAO(con).updateTestOrServiceOrOperationStatus(appId, category);
			}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	}
	
}
