<%@ page isELIgnored="false"%>
<%@ page isErrorPage="false"%>
<%@ taglib uri="/WEB-INF/taglibs-datagrid.tld" prefix="ui"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>
<head>
	<title>IP Preferences - Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js" />

	<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<style type="text/css">
			div.helpText{width:300px}
			number{width:80px}
	</style>

	<script type="text/javascript">
		function getChargePosting(){
			if(document.forms[0].bed_charge_posting.value == 'daily_update'){
				document.getElementById("charge_posting_hour").disabled =true;
			}else{
				document.getElementById("charge_posting_hour").disabled =true;
			}

		}

		function validateCharges(){
			if(!checkNumber(document.forms[0].alarm_limit))return false;
			var form = document.forms[0];
			
			if(document.forms[0].bed_charge_posting.value == "daily_update"){
				if(document.getElementById("charge_posting_hour").value == ""){
					alert("Select charge posting time ");
					return false;
				}
			}
			if(parseInt(form.hrly_charge_threshold.value) > parseInt(form.halfday_charge_threshold.value)){
				alert("In allocation Hourly charge can not be more than  Half Day charge ");
				return false;
			}
			if(parseInt(form.halfday_charge_threshold.value) >parseInt(form.fullday_charge_threshold.value)){
				alert("In allocation Half Day charge can not be more than  Full Day charge");
				return false;
			}
			if(parseInt(form.hrly_charge_threshold.value) > parseInt(form.fullday_charge_threshold.value)){
				alert("In allocation Hourly charge can not be more than  Full Day charge");
				return false;
			}
			if(parseInt(form.bedshift_hrly_charge_threshold.value) > parseInt(form.bedshift_halfday_charge_threshold.value)){
				alert("In shifting Hourly charge can not be more than  Half Day charge ");
				return false;
			}
			if(parseInt(form.bedshift_halfday_charge_threshold.value) > parseInt(form.bedshift_fullday_charge_threshold.value)){
				alert("In shifting Half Day charge can not be more than  Full Day charge");
				return false;
			}
			if(parseInt(form.bedshift_hrly_charge_threshold.value) > parseInt(form.bedshift_fullday_charge_threshold.value)){
				alert("In shifting Hourly charge can not be more than  Full Day charge");
				return false;
			}
			if(parseInt(form.daycare_min_duration.value) > parseInt(form.daycare_slab_1_threshold.value)){
				alert("DayCare Min Duration can not be more than DayCare Slab 1 Duration");
				return false;
			}
			if(parseInt(form.daycare_slab_1_threshold.value) > parseInt(form.daycare_slab_2_threshold.value)){
				alert("DayCare Slab 1 Duration can not be more than DayCare Slab 2 Duration");
				return false;
			}
			if(parseInt(form.daycare_slab_2_threshold.value) > (form.max_daycare_hours.value)){
				alert("DayCare Slab 2 Charge can not be more than Max Daycare hours");
				return false;
			}

			if(document.forms[0].slab1_duration.value == '' || document.forms[0].slab1_duration.value == 0){
				alert("Enter valid number for Slab 1 Duration");
				document.forms[0].slab1_duration.focus();
				return false;
			}

			if(document.forms[0].next_slabs_duration.value == '' || document.forms[0].next_slabs_duration.value == 0){
				alert("Enter valid number for Next Slabs Duration");
				document.forms[0].next_slabs_duration.focus();
				return false;
			}

			return true;

		}

		function validateTime(timeField) {
			var strTime = timeField.value;
			var timePattern = /[0-9]:[0-9]/;
			var regExp = new RegExp(timePattern);
			if (strTime == '') {
				return true;
			}
			if (regExp.test(strTime)) {
				var strHours = strTime.split(':')[0];
				var strMinutes = strTime.split(':')[1];
				if (!isInteger(strHours)) {
					alert("Incorrect time format : hour is not a number");
					timeField.focus();
					return false;
				}
				if (!isInteger(strMinutes)) {
					alert("Incorrect time format : minute is not a number");
					timeField.focus();
					return false;
				}
				if ((parseInt(strHours) > 23) || (parseInt(strHours) < 0)) {
					alert("Incorrect hour : please enter 0-23 for hour");
					timeField.focus();
					return false;
				}
				if ((parseInt(strMinutes) > 59) || (parseInt(strMinutes) < 0)) {
					alert("Incorrect minute : please enter 0-59 for minute");
					timeField.focus();
					return false;
				}
				if(strMinutes.length !=2){
					alert("incorrect minutes please enter 2 digit minuts");
					return false;
				}
			} else {
				alert("Incorrect time format : please enter HH:MI");
				timeField.focus();
				return false;
			}
			return true;
		}
		function checkNumber(field){
		var number = false;
			if(field.value == '-'){
				number = true;
			}
			if(!number){
			number = true;

				if (field == null) {
					number=false;
					field.value = 0;
				}
				var userVal = field.value;
				if ( userVal==null || userVal=="") {
					number =  false;
					field.value = 0;
				}
				if (!isInteger(userVal)) {
					number = false;
					field.value = 0;
				}
				if(field.value > 100){
					alert("Percentage can not be more than 100");
					field.value = 0;
				}
			}
			return number;
		}
	</script>
</head>
<body onload="getChargePosting()">
<form 	action="ippreference.do" method="POST">
<input type="hidden" name="method" id="method" value="create"/>
<h1>IP Preferences</h1>
<insta:feedback-panel/>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel">Bed Charge Posting Method</legend>
<table class="formtable">
	<tr>
		<td class="formlabel">Bed Charge Posting:</td>
		<td>
			<insta:selectoptions name="bed_charge_posting" value="${ip_preferences.bed_charge_posting}"
				optexts="Manual Update,Automatic Update" opvalues="actual_elapsed,daily_update" onchange="getChargePosting()" disabled="true"/>
				<img class="imgHelpText" src="${cpath}/images/help.png" 
                        title="BedCharge Scheduling is moved to Settings / HOSPITAL PREFERENCES / Cron Job"/>
		</td>
		<fmt:formatDate type="time" value="${ip_preferences.charge_posting_hour}" pattern = "HH:mm" var="cph" />
		<td class="formlabel">Charge posting time:</td>
		<td>
			<select name="charge_posting_hour" id="charge_posting_hour" style="width:6em" class="dropdown">
				<option value=''>--Select--</option>
				<c:forEach var="timeval" begin="1" end="24" step="1">
					<c:set var="optselect" value=""/>
					<c:set var="t" value=""/>
					<c:choose>
						<c:when test="${timeval < 10}">
							<c:set value="0${timeval}:00" var="t"/>
						</c:when>
						<c:when test="${timeval == 24}">
							<c:set value="00:00" var="t"/>
						</c:when>
						<c:otherwise>
							<c:set value="${timeval}:00" var="t"/>
						</c:otherwise>
					</c:choose>
					<c:if test="${cph == t}">
						<c:set var="optselect">selected="selected"</c:set>
					</c:if>
					<option value="${t}" ${optselect}>${t}</option>
				</c:forEach>
			</select> Hr
		</td>
	</tr>
	<tr>
		<td class="formlabel">Merge Beds:</td>
		<td>
			<label class="forminfo">${ip_preferences.merge_beds == 'N' ? 'No' : 'Yes'}</label>
		</td>
		<td class="formlabel">Slab1 Duration:</td>
		<td><input type="text" name="slab1_duration" class="number" value="${ip_preferences.slab1_duration}"/></td>
		<td class="formlabel">Next Slab Duration:</td>
		<td><input type="text" name="next_slabs_duration" class="number" value="${ip_preferences.next_slabs_duration}"/></td>
	</tr>
</table>
</fieldset>

<table class="formtable">

	<tr style="height: 1em;"></tr>
</table>
<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel">Normal Bed Thresholds</legend>
<table class="formtable">
	<tr class="firstRow">
		<td><b>First Slab:</b></td>
		<td>
			Hrly:
			<input type="text" name="hrly_charge_threshold"  id="hrly_charge_threshold" maxlength="2"
				 value="${ip_preferences.hrly_charge_threshold }" onblur="checkNumber(this)" class="number"/></td>
		<td>
			Half Day:
			<input type="text" name="halfday_charge_threshold" id="halfday_charge_threshold" maxlength="2"
				value="${ip_preferences.halfday_charge_threshold }" onblur="checkNumber(this)" class="number"/></td>
		<td>
			Full Day:
			<input type="text" name="fullday_charge_threshold" id="fullday_charge_threshold" maxlength="2"
				value="${ip_preferences.fullday_charge_threshold }" onblur="checkNumber(this)" class="number" /></td>

	</tr>
	<tr>
		<td><b>Subsequent Slabs:</b></td>
		<td>
			Hrly:
			<input type="text" name="bedshift_hrly_charge_threshold" id="bedshift_hrly_charge_threshold" class="number"
				maxlength="2" value="${ip_preferences.bedshift_hrly_charge_threshold }" onblur="checkNumber(this)" /></td>
		<td>
			Half Day:
			<input type="text" name="bedshift_halfday_charge_threshold" id="bedshift_halfday_charge_threshold" class="number"
				 maxlength="2" value="${ip_preferences.bedshift_halfday_charge_threshold }" onblur="checkNumber(this)" /></td>
		<td>
			Full Day:
			<input type="text" name="bedshift_fullday_charge_threshold" id="bedshift_fullday_charge_threshold" class="number"
				 maxlength="2" value="${ip_preferences.bedshift_fullday_charge_threshold }" onblur="checkNumber(this)" /></td>

	</tr>
</table>
</fieldset>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel">ICU Bed Thresholds</legend>
<table class="formtable">
	<tr class="firstRow">
		<td><b>First Slab:</b></td>
		<td>
			Hrly:
			<input type="text" name="icu_hrly_charge_threshold"  id="icu_hrly_charge_threshold" maxlength="2" class="number"
				 value="${ip_preferences.icu_hrly_charge_threshold }" onblur="checkNumber(this)"/></td>
		<td>
			Half Day:
			<input type="text" name="icu_halfday_charge_threshold" id="icu_halfday_charge_threshold" class="number"
				maxlength="2" value="${ip_preferences.icu_halfday_charge_threshold }" onblur="checkNumber(this)" /></td>
		<td>
			Full Day:
			<input type="text" name="icu_fullday_charge_threshold" id="icu_fullday_charge_threshold" class="number"
				maxlength="2" value="${ip_preferences.icu_fullday_charge_threshold }" onblur="checkNumber(this)"/></td>

	</tr>
	<tr>
		<td><b>Subsequent Slabs:</b></td>
		<td>
			Hrly:
			<input type="text" name="icu_bedshift_hrly_charge_threshold" id="icu_bedshift_hrly_charge_threshold" class="number"
				maxlength="2" value="${ip_preferences.icu_bedshift_hrly_charge_threshold }" onblur="checkNumber(this)" /></td>
		<td>
			Half Day:
			<input type="text" name="icu_bedshift_halfday_charge_threshold" id="icu_bedshift_halfday_charge_threshold" class="number"
				maxlength="2" value="${ip_preferences.icu_bedshift_halfday_charge_threshold }" onblur="checkNumber(this)" /></td>
		<td>
			Full Day:
			<input type="text" name="icu_bedshift_fullday_charge_threshold" id="icu_bedshift_fullday_charge_threshold" class="number"
				maxlength="2" value="${ip_preferences.icu_bedshift_fullday_charge_threshold }" onblur="checkNumber(this)" /></td>

	</tr>
</table>
</fieldset>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel">Daycare Preferences</legend>
<table class="formtable">
	<tr>
		<td class="formlabel">Min Duration (Hrs):</td>
		<td>
			<input type="text" name="daycare_min_duration" value="${ip_preferences.daycare_min_duration}"
				onblur="checkNumber(this)"/>
		</td>
		<td class="formlabel">Slab 1 Threshold:</td>
		<td>
			<input type="text" name="daycare_slab_1_threshold" value="${ip_preferences.daycare_slab_1_threshold}"
				onblur="checkNumber(this)"/>
		</td>
		<td class="formlabel">Slab 2 Threshold:</td>
		<td>
			<input type="text" name="daycare_slab_2_threshold" value="${ip_preferences.daycare_slab_2_threshold}"
				onblur="checkNumber(this)"/>
		</td>
	</tr>
	<tr>
		<td class="formlabel">Maximum Day Care Hrs:</td>
		<td><input type="text" name="max_daycare_hours" value="${ip_preferences.max_daycare_hours }"
			onkeypress="return enterNumOnlyzeroToNine(event)"/></td>
	</tr>
</table>
</fieldset>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel">Bed Allocation Preferences</legend>
<table class="formtable">
	<tr>
		<td class="formlabel">Allocation at Registration:</td>
		<td>
			<insta:selectoptions name="allocate_bed_at_reg" value="${ip_preferences.allocate_bed_at_reg}"
				opvalues="Y,N" optexts="Yes,No"/>
		</td>
		<td class="formlabel">Change Billing Bed Type:</td>
		<td>
			<insta:selectoptions name="current_bed_type_is_bill_bed_type"
				value="${ip_preferences.current_bed_type_is_bill_bed_type}"
				opvalues="N,A,I" optexts="Never,Always,New Bed is not ICU"/>
		</td>
		<td class="formlabel"> Force Duty Doctor For:</td>
		<td>
			<insta:selectoptions name="duty_doctor_selection" value="${ip_preferences.duty_doctor_selection}"
				opvalues="N,A,I" optexts="None,All,ICU"/>
		</td>
	</tr>

	<tr>
		<td class="formlabel">Force Remarks:</td>
		<td>
			<insta:selectoptions name="force_remarks" value="${ip_preferences.force_remarks}"
				opvalues="Y,N" optexts="Yes,No"/>
		</td>

		<td class="formlabel">Bed Alarm(%):</td>
		<td>
			<input type="text" name="alarm_limit" value="${ip_preferences.alarm_limit}"
			onchange="return checkNumber(this)"/>
		</td>

		<td class="formlabel">Retain Bed Charges:</td>
		<td>
			<insta:selectoptions  name="retain_bed_charges"
				value="${ip_preferences.retain_bed_charges}"
				opvalues="B,W" optexts="Bed Charges,Ward Charges"/>
		</td>
	</tr>

	<tr>
		<td class="formlabel">Bystander Bed Charges:</td>
		<td>
			<insta:selectoptions  name="bystander_bed_charges_applicable_on"
				value="${ip_preferences.bystander_bed_charges_applicable_on}"
				opvalues="B,W" optexts="Only Bed Charges,Sum of Bed Charges And Associate charges"/>
		</td>
		<td class="formlabel">Bystander Available For:</td>
		<td>
			<insta:selectoptions name="bystander_availability" value="${ip_preferences.bystander_availability}"
				opvalues="A,I" optexts="All,ICU Only"/>
		</td>
		<td class="formlabel">Cut-Off Required:</td>
		<td>
			<insta:selectoptions name="cut_off_required" value="${ip_preferences.cut_off_required}"
				opvalues="Y,N" optexts="Yes,No"/>
		</td>
	</tr>

</table>
</fieldset>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel">Theatre Charges Preferences</legend>
<table class="formtable">
	<tr>
		<td class="formlabel">Split Theatre Charges:</td>
		<td>
			<insta:selectoptions name="split_theatre_charges" value="${ip_preferences.split_theatre_charges}"
				opvalues="Y,N" optexts="Yes,No"/>
		</td>
		<td class="formlabel">Theatre Charge Code Type:</td>
		<td>
			<insta:selectdb name="theatre_charge_code_type" table="mrd_supported_codes" valuecol="code_type"
				displaycol="code_type"	filtervalue="Treatment" filtercol="code_category" dummyvalue="--Select--"
				value="${ip_preferences.theatre_charge_code_type}"/>
		</td>
		<td class="formlabel">Theatre Daily Charge Code:</td>
		<td>
			<input type="text" name="theatre_daily_charge_code" value="${ip_preferences.theatre_daily_charge_code}" maxlength="50"/>
		</td>
	</tr>
	<tr>
		<td class="formlabel">Theatre Min. Charge Code:</td>
		<td>
			<input type="text" name="theatre_min_charge_code" value="${ip_preferences.theatre_min_charge_code}" maxlength="50"/>

		</td>
		<td class="formlabel">Theatre Slab1 Charge Code:</td>
		<td>
			<input type="text" name="theatre_slab1_charge_code" value="${ip_preferences.theatre_slab1_charge_code}" maxlength="50"/>
		</td>
		<td class="formlabel">Theatre Incr. Charge Code:</td>
		<td>
			<input type="text" name="theatre_incr_charge_code" value="${ip_preferences.theatre_incr_charge_code}" maxlength="50"/>
		</td>
	</tr>
</table>
</fieldset>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel">Others</legend>
<table class="formtable">
	<tr>
		<td class="formlabel">Maximum Billable Consultation Per Day:</td>
		<td>
			<input type="text" name="max_billable_cons_day" value="${ip_preferences.max_billable_cons_day}" onkeypress="return enterNumOnlyzeroToNine(event)" class="number" maxlength="3"/>
		</td>
		<td></td>
		<td></td>
		<td></td>
		<td></td>
	</tr>
</table>
</fieldset>

<div class="screenAction" >
	<button type="submit" name="submit" id="submit" accesskey="S" onclick="return validateCharges()">
	<b><u>S</u></b>ave</button>
</div>
</form>
</body>
</html>
