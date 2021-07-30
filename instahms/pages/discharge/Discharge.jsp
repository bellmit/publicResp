<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<jsp:useBean id="currentDate" class="java.util.Date"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title><insta:ltext key="registration.patientdischarge.details.discharge.instahms"/></title>
	<meta http-equiv="Content Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="script" file="discharge/discharge.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="css" file="discharge/discharge.css" />
	<script type="text/javascript">
		var regDate = '<fmt:formatDate value="${patient.reg_date}" pattern="dd-MM-yyyy"/>';
		var regTime = '<fmt:formatDate value="${patient.reg_time}" pattern="HH:mm"/>';
		var doctors = ${doctorsJSON};
		var patientId = "${patient.patient_id}";
		var mrno = "${patient.mr_no}";
		var doctorName = "${patient.doctor_name}";
		var patientVisitStatus = "${patient.visit_status}";
		var noPendingOrdersOperations = ${noPendingOrdersOperations};
		var noPendingWardActivites = ${noPendingWardActivites};
		var noOpenPatientIndents = ${noOpenPatientIndents};
		var isInitiateDischarge = '${actionRightsMap.initiate_discharge }' == 'A' || ${roleId} == 1 || ${roleId} == 2;
		var isClinicalDischarge = '${actionRightsMap.clinical_discharge }' == 'A' || ${roleId} == 1 || ${roleId} == 2;
		var dischargeForPendingIndent = '${dischargeForPendingIndent}';
	</script>
	<insta:js-bundle prefix="registration.patient" />
	
	<style type="text/css">

		#disabler  div {
		  background-color: #8888ff;
		}

		div.disabler{
			  background-color: #CCCCCC;
			  opacity: 0.5;
			  -webkit-user-select: none;dropdown
			  -khtml-user-select: none;
			  -moz-user-select: none;
			  -o-user-select: none;
			  user-select: none;
		}

	</style>
	
</head>
<c:set var="discharge" value="" />
<c:if test="${empty patient}">
	<c:set var="discharge" value="disabled" />
</c:if>
<body onload="init()">
	<c:set var="saveText">
		<insta:ltext key="registration.patientdischarge.details.save" />
	</c:set>
	<c:set var="dischargeText">
		<insta:ltext key="registration.patientdischarge.details.discharge" />
	</c:set>
	<c:set var="dischargedText">
		<insta:ltext key="registration.patientdischarge.details.discharged" />
	</c:set>
	<c:set var="activeText">
		<insta:ltext key="registration.patientdischarge.details.active" />
	</c:set>
	<c:set var="bedView">
		<insta:ltext key="registration.beddetails.details.bedview" />
	</c:set>
	<c:set var="bedDetails">
		<insta:ltext key="registration.beddetails.details.beddetails" />
	</c:set>
	<c:set var="dummyvalue">
		<insta:ltext key="selectdb.dummy.value" />
	</c:set>
	<c:set var="yesnolabel">
		<insta:ltext key="registration.patient.preregistration.details.no" />,
		<insta:ltext key="registration.patient.preregistration.details.yes" />
	</c:set>
	<h1 style="float: left">
		<insta:ltext key="registration.patientdischarge.details.patientdischarge" />
	</h1>

	<insta:patientsearch searchType="visit" searchUrl="DischargePatient.do"
		buttonLabel="Find" searchMethod="getDischargeDetails"
		fieldName="patientid" activeOnly="false" showStatusField="true" />

	<insta:feedback-panel />
	<insta:patientdetails patient="${patient}" showClinicalInfo="true" />
	
	<fieldset class="fieldSetBorder"
		style="display : ${not empty patient.patient_id ? 'block' : 'none'} ">
		<legend class="fieldSetLabel">
			<insta:ltext key="registration.patientdischarge.details.patientdischargestatus"/>
		</legend>
		<div class="checkholder" style="box-sizing: content-box">
			<div class="bar"></div>
			<div class="circlecontainer">
				<c:choose>
					<c:when test="${not empty initiateDischargeDetails.map && initiateDischargeDetails.map.initiate_discharge_status == 'true'}">
					 	<c:set var="initiateDischargeCheckmarkClass" value="checkmark"/>
					 	<c:set var="initiateDischargeLabelClass" value="label"/>
					 	<c:set var="initiateDischargeDisplayTimeStamp" value="true" />
					 	<c:set var="initiateDischargeDate" value="${initiateDischargeDetails.map.initiate_discharging_date}"/>
					 	<c:set var="initiateDischargeTime" value="${initiateDischargeDetails.map.initiate_discharging_time}"/>
					</c:when>
					<c:otherwise>
						<c:set var="initiateDischargeCheckmarkClass" value="uncheckmark"/>
					 	<c:set var="initiateDischargeLabelClass" value="unlabel"/>
					 	<c:set var="initiateDischargeDisplayTimeStamp" value="false" />
					</c:otherwise>
				</c:choose>
				<div class="${initiateDischargeCheckmarkClass}" id="initiateDischargeCheck">
					<div class="checkmark_circle"></div>
					<div class="checkmark_stem"></div>
					<div class="checkmark_kick"></div>
				</div>
				<div class="${initiateDischargeLabelClass}" id="initiateDischargeLabel">
					<span>
						<insta:ltext key="registration.patientdischarge.details.initiatedischarge"/>
					</span>
					<br>
					<span id="initiatediscargedatetime" class="labeldatetime">
						<c:if test="${initiateDischargeDisplayTimeStamp == 'true'}">
							<fmt:formatDate pattern="dd-MM-yyyy" value="${initiateDischargeDate}" />, <fmt:formatDate pattern="HH:mm" value="${initiateDischargeTime}" />
						</c:if>
 					</span>  
					<br>
					<span id="initiatedischargeusername" class="labeluser">
						<c:if test="${initiateDischargeDisplayTimeStamp == 'true'}">
							${initiateDischargeDetails.map.temp_username}
						</c:if>
					</span>
				</div>
			</div>
			<div class="circlecontainer">
				<c:choose>
					<c:when test="${not empty clinicalDischargeDetails.map && clinicalDischargeDetails.map.clinical_discharge_flag == true}">
						<c:set var="clinicalDischargeCheckmarkClass" value="checkmark"/>
					 	<c:set var="clinicalDischargeLabelClass" value="label"/>
					 	<c:set var="clinicalDischargeDisplayTimeStamp" value="true" />
					 	<c:set var="clinicalDischargeDate" value="${clinicalDischargeDetails.map.clinical_discharging_date}"/>
					 	<c:set var="clinicalDischargeTime" value="${clinicalDischargeDetails.map.clinical_discharging_time}"/>	
					</c:when>
					<c:otherwise>
						<c:set var="clinicalDischargeCheckmarkClass" value="uncheckmark"/>
					 	<c:set var="clinicalDischargeLabelClass" value="unlabel"/>
					 	<c:set var="clinicalDischargeDisplayTimeStamp" value="false" />
					</c:otherwise>
				</c:choose>
				<div id="clinicaldischargecheckmark" class="${clinicalDischargeCheckmarkClass}">
					<div class="checkmark_circle"></div>
					<div class="checkmark_stem"></div>
					<div class="checkmark_kick"></div>
				</div>
				<div id="clinicaldischargelabel" class="${clinicalDischargeLabelClass}">
					<span>
						<insta:ltext key="registration.patientdischarge.details.clinicaldischarge"/>
					</span>
					<br> 
					<span id="clinicaldiscargedatetime" class="labeldatetime">
						<c:if test="${clinicalDischargeDisplayTimeStamp == 'true'}">
							<fmt:formatDate pattern="dd-MM-yyyy" value="${clinicalDischargeDate}" />, <fmt:formatDate pattern="HH:mm" value="${clinicalDischargeTime}" />
						</c:if>
					</span>
					<br>
					<span id="clinicaldischargeusername" class="labeluser">
						<c:if test="${clinicalDischargeDisplayTimeStamp == 'true'}">
							${clinicalDischargeDetails.map.temp_username}
						</c:if>
					</span>
				</div>
			</div>
			<div class="circlecontainer">
				<c:choose>
					<c:when test="${not empty financialDischargeDetails.map && financialDischargeDetails.map.financial_discharge_status == 'true'}">
					 	<c:set var="financialDischargeCheckmarkClass" value="checkmark"/>
					 	<c:set var="financialDischargeLabelClass" value="label"/>
					 	<c:set var="financialDischargeDisplayTimeStamp" value="true" />
					 	<c:set var="financialDischargeDate" value="${financialDischargeDetails.map.financial_discharge_date}"/>
					 	<c:set var="financialDischargeTime" value="${financialDischargeDetails.map.financial_discharge_time}"/>
					</c:when>
					<c:otherwise>
						<c:set var="financialDischargeCheckmarkClass" value="uncheckmark"/>
					 	<c:set var="financialDischargeLabelClass" value="unlabel"/>
					 	<c:set var="financialDischargeDisplayTimeStamp" value="false" />
					</c:otherwise>
				</c:choose>
				<div class="${financialDischargeCheckmarkClass}">
					<div class="checkmark_circle"></div>
					<div class="checkmark_stem"></div>
					<div class="checkmark_kick"></div>
				</div>
				<div class="${financialDischargeLabelClass}">
					<span>
						<insta:ltext key="registration.patientdischarge.details.financialdischarge"/>
					</span>
					<br>
					<c:if test="${financialDischargeDisplayTimeStamp == 'true'}">
						<span class="labeldatetime">
							<fmt:formatDate pattern="dd-MM-yyyy" value="${financialDischargeDate}" />, <fmt:formatDate pattern="HH:mm" value="${financialDischargeTime}" />
						</span>
						<br>
						<span class="labeluser">
							${financialDischargeDetails.map.temp_username}
						</span>
					</c:if>
				</div>
			</div>
			<div class="circlecontainer">
				<c:choose>
					<c:when test="${reportFinalizedDetails.map.discharge_finalized_user != null && reportFinalizedDetails.map.discharge_finalized_user != ''}">
						<c:set var="reportFinalizedCheckmarkClass" value="checkmark"/>
					 	<c:set var="reportFinalizedLabelClass" value="label"/>
					 	<c:set var="reportFinalizedDisplayTimeStamp" value="true" />
					 	<c:set var="reportFinalizedDate" value="${reportFinalizedDetails.map.discharge_finalized_date}"/>
					 	<c:set var="reportFinalizedTime" value="${reportFinalizedDetails.map.discharge_finalized_time}"/>
					</c:when>
					<c:otherwise>
						<c:set var="reportFinalizedCheckmarkClass" value="uncheckmark"/>
					 	<c:set var="reportFinalizedLabelClass" value="unlabel"/>
					 	<c:set var="reportFinalizedDisplayTimeStamp" value="false" />
					</c:otherwise>
				</c:choose>
				<div class="${reportFinalizedCheckmarkClass}">
					<div class="checkmark_circle"></div>
					<div class="checkmark_stem"></div>
					<div class="checkmark_kick"></div>
				</div>
				<div class="${reportFinalizedLabelClass}">
					<span>
						<insta:ltext key="registration.patientdischarge.details.reportfinalized"/>
					</span>
					<br>
					<c:if test="${reportFinalizedDisplayTimeStamp == 'true'}">
						<span class="labeldatetime">
							<fmt:formatDate pattern="dd-MM-yyyy" value="${reportFinalizedDate}" />, <fmt:formatDate pattern="HH:mm" value="${reportFinalizedTime}" />
						</span>
						<br>
						<span class="labeluser">
							${reportFinalizedDetails.map.temp_username}
						</span>
					</c:if>
				</div>
			</div>
			<div class="circlecontainer">
				<c:choose>
					<c:when test="${physicalDischargeDetails.map.discharge_flag == 'D'}">
						<c:set var="physicalDischargeCheckmarkClass" value="checkmark"/>
					 	<c:set var="physicalDischargeLabelClass" value="label"/>
					 	<c:set var="physicalDischargeDisplayTimeStamp" value="true" />
					 	<c:set var="physicalDischargeDate" value="${physicalDischargeDetails.map.discharge_date}"/>
					 	<c:set var="physicalDischargeTime" value="${physicalDischargeDetails.map.discharge_time}"/>
					</c:when>
					<c:otherwise>
						<c:set var="physicalDischargeCheckmarkClass" value="uncheckmark"/>
					 	<c:set var="physicalDischargeLabelClass" value="unlabel"/>
					 	<c:set var="physicalDischargeDisplayTimeStamp" value="false" />
					</c:otherwise>
				</c:choose>
				<div class="${physicalDischargeCheckmarkClass}">
					<div class="checkmark_circle"></div>
					<div class="checkmark_stem"></div>
					<div class="checkmark_kick"></div>
				</div>
				<div class="${physicalDischargeLabelClass}">
					<span>
						<insta:ltext key="registration.patientdischarge.details.physicaldischarge"/>
					</span>
					<br />
					<c:if test="${physicalDischargeDisplayTimeStamp == 'true'}">
 						<span class="labeldatetime">
							<fmt:formatDate pattern="dd-MM-yyyy" value="${physicalDischargeDate}" />, <fmt:formatDate pattern="HH:mm" value="${physicalDischargeTime}" />
 						</span>
						<br>
						<span class="labeluser">
							${physicalDischargeDetails.map.temp_username}
						</span>
					</c:if>
				</div>
			</div>
		</div>
	</fieldset>
	<form method="post" name="initiatedischargeform" id="initiatedischargeform" autocomplete="off">
		<input type="hidden" name="_method" id="_method" value="saveOrUpdateInitiateDischargeDetails" />
		<input type="hidden" name="mrNo" id="mrNo" value="${patient.mr_no }" />
		<input type="hidden" name="patient_id" value="${patient.patient_id  }" />
		<input type="hidden" name="visit_type" value="${patient.visit_type  }" />
		<input type="hidden" id="isDoctorId" value="${docId}"/>
		<input type="hidden" id="isDoctorFlag" value="${userIsDoc}"/>
		<input type="hidden" id="isDoctorName" value="${docName}"/>
		<input type="hidden" name="nok_contact_i" value="${patient.patcontactperson}" />
		<fieldset class="fieldSetBorder"
            style="display : ${not empty patient.patient_id ? 'block' : 'none'} ">
            <legend class="fieldSetLabel">
              <insta:ltext key="registration.patientdischarge.details.initiatedischarge"/>
            </legend>
            <div style="height:116px;" id="initiateDischargeDiv">
	            <div style="height:89px;">
		            <div class="detailsdiv">
		                <table class="formtable">
		                    <tr>
		                        <td class="formlabel"><insta:ltext key="registration.patientdischarge.details.initiatedischarge"/>:
		                        </td>
		                        <c:choose>
		                    		<c:when test="${not empty initiateDischargeDetails.map}">
		                    			<c:choose>
			                    			<c:when test="${initiateDischargeDetails.map.initiate_discharge_status == true}">
			                    				<c:set var="initiatedischargechecked" value="checked"/>
			                    			</c:when>
			                    			<c:otherwise>
			                    				<c:set var="initiatedischargechecked" value=""/>
			                    			</c:otherwise>
		                    			</c:choose>
		                    			<c:set var="initiatedischargeonclick" value="return false;"/>
		                    			<c:set var="initiatedisabled" value="disabled"/>
		                    			<c:set var="initiatebutton" value="edit"></c:set>
		                    			<c:set var="initiateflagvalue" value="${initiateDischargeDetails.map.initiate_discharge_status == true}"/>
		                    		</c:when>
		                    		<c:otherwise>
		                    			<c:set var="initiatedischargeonclick" value="checkFunction()"/>
		                    			<c:set var="initiatedisabled" value=""/>
		                    			<c:set var="initiatedischargechecked" value=""/>
		                    			<c:set var="initiatebutton" value="save"></c:set>
		                    			<c:set var="initiateflagvalue" value="false"/>
		                    		</c:otherwise>
		                    	</c:choose>
		                    	<input type="hidden" name="initiateflag" id="initiateflag" value="${initiateflagvalue}"/>
		                        <td class="forminfo"><input type="checkbox" id="initiate_check" name="initiate_check" value="yes" onclick="${initiatedischargeonclick}" ${initiatedischargechecked} ${initiatedisabled}/></td>
		                        <td class="formlabel"><insta:ltext key="registration.patientdischarge.details.expecteddischargedate"/>:</td>
		                        <td>
		                        	<fmt:formatDate var="disdate" pattern="dd-MM-yyyy"
		                                value="${initiateDischargeDetails.map.expected_discharge_date}" />
		                            <fmt:formatDate var="distime" pattern="HH:mm"
		                                value="${initiateDischargeDetails.map.expected_discharge_time}" />
		                            <insta:datewidget name="initiate_expected_discharge_date" id="initiate_expected_discharge_date"
		                               	 value="${disdate}" btnPos="left" disabled="disabled"/> <span class="star">*</span>
		                           	<input type="text" size="4" id="initiate_expected_discharge_time" name="initiate_expected_discharge_time" value="${distime}"
		                            	 class="timefield" disabled/> <span class="star">*</span>
		                       </td>
		                    </tr>
		                    <tr>
		                    	<td class="formlabel">
		                    		<insta:ltext key="registration.patientdischarge.details.dischargingdoctorininitiate" />:
		                    	</td>
								<c:choose>
									<c:when test="${userIsDoc == true}">
										<c:set var="disabled" value="disabled"/>
									</c:when>
									<c:otherwise>
										<c:set var="disabled" value=""/>
									</c:otherwise>
								</c:choose>
								<td class="forminfo" style="padding-bottom: 10px;"">
									<div id="myAutoComplete" style="width: 140px;">
										<input type="text" id="initiate_discharge_doc_ac"
											name="initiate_discharge_doc_ac" maxlength="50" class="field"
											value="${initiatedischargechecked != 'checked'? docName : initiateDischargeDetails.map.doctor_name}" disabled/>
										<div id="initiateDisDocContainer" style="width: 250px">
										</div>
									</div> 
									<input type="hidden" name="initiate_discharge_doctor_id"
										id="initiate_discharge_doctor_id"
										value="${userIsDoc == true ? docId : initiateDischargeDetails.map.initiate_discharging_doctor}" />
									<span style="position: relative;left: 145px;color: red;">*</span>
								</td>
		                        <td class="formlabel"><insta:ltext key="registration.patientdischarge.details.initiatedischargeremarks"/>:</td>
		                        <td class="forminfo">
		                       	 	<textarea cols=30 rows=3 style="max-width: 552px;"
										name="initiate_discharge_remarks" id="initiate_discharge_remarks" disabled>${initiateDischargeDetails.map.initiate_discharge_comments}</textarea>
		                        </td>
		                        <input type="hidden"  id="initiate_dischargeuser" name="initiate_dischargeuser" value="${userid}"/>
		                    </tr>
		                </table>
		            </div>
		            <div class="buttondiv">
		                <input class="savebutton" onclick="${ (initiatebutton == 'edit') ? 'editInitiateDischarge()' : 'initiateDischarge()'}" id="initiatedischargebutton" 
		                	type="button" value="${ (initiatebutton == 'edit') ? 'Edit' : 'Save'}"/>
		            </div>
	            </div>
	    	</div> 
        </fieldset>
	</form>
	<form method="post" name="clinicaldischargeform" id="clinicaldischargeform" action="DischargePatient.do" autocomplete="off">
		<input type="hidden" name="_method" id="_method" value="saveOrUpdateClinicalDischargeDetails" />
		<input type="hidden" name="mrNo" id="mrNo" value="${patient.mr_no }" />
		<input type="hidden" name="patient_id" value="${patient.patient_id  }" />
		<fieldset class="fieldSetBorder"
            style="display : ${not empty patient.patient_id ? 'block' : 'none'} ">
            <legend class="fieldSetLabel">
                <insta:ltext key="registration.patientdischarge.details.clinicaldischarge"/>
            </legend>
            <div style="height:80px;" id="clinicalDischargeDiv">
	            <div style="height:90px;">
	                <div class="detailsdiv">
	                    <div>
	                    	<c:choose>
	                    		<c:when test="${not empty clinicalDischargeDetails.map}">
	                    			<c:choose>
		                    			<c:when test="${clinicalDischargeDetails.map.clinical_discharge_flag == true}">
		                    				<c:set var="clinicaldischargechecked" value="checked"/>
		                    			</c:when>
		                    			<c:otherwise>
		                    				<c:set var="clinicaldischargechecked" value=""/>
		                    			</c:otherwise>
	                    			</c:choose>
	                    			<c:set var="clinicaldischargedisabled" value="disabled"/>
	                    			<c:set var="clinicaldischargecommentsdisabled" value="disabled"/>
	                    			<c:set var="clinicalflagvalue" value="${clinicalDischargeDetails.map.clinical_discharge_flag == true}"/>
	                    		</c:when>
	                    		<c:otherwise>
	                    			<c:set var="clinicaldischargedisabled" value=""/>
	                    			<c:set var="clinicaldischargecommentsdisabled" value="disabled"/>
	                    			<c:set var="clinicalflagvalue" value="false"/>
	                    		</c:otherwise>
	                    	</c:choose>
	                    	<c:choose>
	                    		<c:when test="${not empty initiateDischargeDetails.map && initiateDischargeDetails.map.initiate_discharge_status == 'true'}">
	                    			<c:set var="clinicaldischargesectiondisabled" value=""/>
		            			</c:when>
                    			<c:otherwise>
                    				<c:set var="clinicaldischargesectiondisabled" value="disabled"/>
                    			</c:otherwise>
		                    </c:choose>
	                    	<table class="formtable">
	                    		<tr>
	                    			<td class="formlabel"><insta:ltext key="registration.patientdischarge.details.clinicaldischarge"/>:</td>
	                    			<input type="hidden" name="clinicalflag" id="clinicalflag" value="${clinicalflagvalue}"/>
	                    			<input type="hidden" name="clinicaldischargeuser" value="${userid}"/>
			                        <td class="forminfo">
			                        	<input type="checkbox" id="clinicaldischarge" name="clinicaldischarge" value="yes" onclick="clinicalDischargeOnClick();" ${clinicaldischargechecked} ${clinicaldischargedisabled} ${clinicaldischargesectiondisabled}/>
			                        </td>
	                        		<td class="formlabel">
	                        			<insta:ltext key="registration.patientdischarge.details.clinicaldischargeremarks"/>:
	                        		</td>
	                        		<td class="forminfo">
	                        			<textarea cols=30 rows=3 style="max-width: 680px;" id="clinicaldischargecomments" name="clinicaldischargecomments" id="clinical_discharge_comments" ${clinicaldischargecommentsdisabled } ${clinicaldischargesectiondisabled} >${clinicalDischargeDetails.map.clinical_discharge_comments}</textarea>
	                        		</td>
	                        	</tr>
	                        </table>
	                    </div>
	                </div>
	                <div class="buttondiv" style="height: 60px;">
	                	<c:choose>
		                	<c:when test="${not empty clinicalDischargeDetails.map}">
		                		<c:set var="clinicaldischargebuttonvalue" value="Edit"/>
		                		<c:set var="clinicaldischargebuttononclick" value="editClinicalDischarge()"/>
		                	</c:when>
		                	<c:otherwise>
		                		<c:set var="clinicaldischargebuttonvalue" value="Save"/>
		                		<c:set var="clinicaldischargebuttononclick" value="clinicalDischarge()"/>
		                	</c:otherwise>
		                </c:choose>
	                    <input type="button" id="clinicaldischargebutton" class="savebutton" onclick="${clinicaldischargebuttononclick}" value="${clinicaldischargebuttonvalue}" ${clinicaldischargesectiondisabled}/>
	                </div>
	            </div>
            </div>
            <div style="clear:both"></div>
            <div class="divline"></div>
            <div style="margin: 0px 0px 5px 0px;padding: 1px 6px 4px 6px;">
                <legend class="feildSetLabel"><insta:ltext key="registration.patientdischarge.details.pendingactivities"/>: </legend>
                <div class="pendingContainer">
                    <div class="pending">
                        <span class="exclam">!</span> <span class="activity"><insta:ltext key="registration.patientdischarge.details.orderedactivities"/>:</span> <span class="num">${noPendingOrdersOperations}</span>
                    </div>
                    <br />
                    <div class="viewContainer">
											<c:if test="${patient.visit_type == 'o'}">
												<insta:screenlink addPipe="false" screenId="new_op_order" label="View" extraParam="/index.htm#/filter/default/patient/${ifn:encodeUriComponent(patient.mr_no)}/order/visit/${ifn:encodeUriComponent(patient.patient_id)}?retain_route_params=true" />
											</c:if>
											<c:if test="${patient.visit_type == 'i'}">
													<insta:screenlink addPipe="false" screenId="new_ip_order" label="View" extraParam="/index.htm#/filter/default/patient/${ifn:encodeUriComponent(patient.mr_no)}/order/visit/${ifn:encodeUriComponent(patient.patient_id)}?retain_route_params=true" />
											</c:if>
                    </div>
                </div>
                <div class="pendingContainer">
                    <div class="pending">
                        <span class="exclam">!</span> <span class="activity"><insta:ltext key="registration.patientdischarge.details.wardactivities"/>:</span> <span class="num">${noPendingWardActivites}</span>
                    </div>
                    <br />
                    <div class="viewContainer">
                    	<c:if test="${patient.visit_type != 'o'}">
                    	<insta:screenlink addPipe="false" screenId="activities_list" label="View" extraParam="?_method=list&patient_id=${patient.patient_id}"/>
                    	</c:if>
                    </div>
                </div>
                <div class="pendingContainer">
                    <div class="pending">
                        <span class="exclam">!</span> <span class="activity"><insta:ltext key="registration.patientdischarge.details.patientindents"/>:</span> <span class="num">${noOpenPatientIndents}</span>
                    </div>
                    <br />
                    <div class="viewContainer">
                    	<insta:screenlink addPipe="false" screenId="stores_patient_indent_list" label="View" extraParam="?_method=list&mr_no=${patient.mr_no}"/>
                    </div>
                </div>
            </div>
        </fieldset>
    </form>
	<form method="post" name="dischargeform" action="DischargePatient.do">
		<input type="hidden" name="_method" id="_method" value="discharge" />
		<input type="hidden" name="mrNo" id="mrNo" value="${patient.mr_no }" />
		<input type="hidden" name="patient_id" value="${patient.patient_id  }" />
		<input type="hidden" name="bed_name" value="${patient.alloc_bed_type}" />
		<input type="hidden" name="ward_name" value="${patient.reg_ward_name}" />
		<input type="hidden" name="deptName" value="${patient.dept_name}" />
		<input type="hidden" name="nok_contact_d" value="${patient.patcontactperson}" />
		<fieldset class="fieldSetBorder"
			style="display : ${not empty patient.patient_id ? 'block' : 'none'} ">
			<legend class="fieldSetLabel">
				<insta:ltext key="registration.patientdischarge.details.physicaldischarge" />
			</legend>
			<div style="height:120px;">
				<div class="detailsdiv">
					<table class="formtable">
						<jsp:useBean id="now" class="java.util.Date" />
						<c:choose>
							<c:when test="${canDischarge || patient.visit_status eq 'I'}">
								<tr>
									<td class="formlabel">
										<insta:ltext key="registration.patientdischarge.details.dischargestatus"/>:
									</td>
									<td>
										<b>${patient.visit_status eq 'I' ?dischargedText : activeText }</b>
									</td>
								</tr>
								<tr>
									<td class="formlabel"><insta:ltext
											key="registration.patientdischarge.details.dischargetype" />:
									</td>
									<td>
										<insta:selectdb name="discharge_type"
											id="discharge_type"
											value="${discharge_details.discharge_type_id}"
											table="discharge_type_master" valuecol="discharge_type_id"
											displaycol="discharge_type" orderby="discharge_type"
											filterby="status" filtervalue="A" dummyvalue="${dummyvalue}"
											onchange="displayReferredTo(this);" /> <span class="star">*</span>
									</td>
									<td class="formlabel">
										<insta:ltext key="registration.patientdischarge.details.dischargedate" />:
									</td>
									<td>
										<fmt:formatDate var="disdate" pattern="dd-MM-yyyy"
											value="${patient.discharge_date == null ?currentDate : patient.discharge_date }" />
										<fmt:formatDate var="distime" pattern="HH:mm"
											value="${patient.discharge_time == null ? currentDate : patient.discharge_time }" />
										<insta:datewidget name="discharge_date" id="discharge_date"
											value="${disdate}" btnPos="left" /> <span class="star">*</span>
										<input type="text"
										size="4" id="discharge_time" name="discharge_time"
										value="${distime}" onblur="validateDischaregDateTime()"
										class="timefield" /> <span class="star">*</span>
									</td>
								</tr>
								<tr id="refToHospDiv" style="display: ${discharge_details.discharge_type=='Referred To'? 'table-row' : 'none'}">
									<td class="formlabel">
										<label>
											<insta:ltext
												key="registration.patientdischarge.details.hospital" />:
										<label>
									</td>
									<td>
										<div>
											<input type="text" name="referred_to" id="referred_to"
												value="${discharge_details.referred_to}"
												style="width: 13em;" />
										</div>
										
									</td>
								</tr>
								<tr id="transferToHospDiv" style="display: ${discharge_details.discharge_type=='Referred To'? 'table-row' : 'none'}">
									<c:if test="${preferences.modulesActivatedMap['mod_adv_ins'] eq 'Y'}">
										<td class="formlabel">
											<label>
												<insta:ltext
													key="registration.patientdischarge.details.transferredto" />:
											<label>
										</td>
										<td>
											<div>
												<insta:selectdb name="transfer_destination"
													table="transfer_hospitals" valuecol="transfer_hospital_id"
													value="${discharge_details.transfer_destination}"
													displaycol="transfer_hospital_name"
													dummyvalue="${dummyvalue}" dummyvalueId=""
													orderby="transfer_hospital_name" />
											</div>
										</td>
										<td class="formlabel">
											<label>
												<insta:ltext
													key="ui.label.reason.for.referral" />:
											<label>
										</td>
										<td>
											<div>
												<insta:selectdb name="reason_for_referral"
													table="reason_for_referral" valuecol="id"
													value="${discharge_details.reason_for_referral_id}"
													displaycol="reason"
													dummyvalue="${dummyvalue}" dummyvalueId=""
													orderby="reason" />
											</div>
										</td>
									</c:if>
								</tr>
								<tr id="deathDateDiv"
									style="display: ${discharge_details.discharge_type == 'Death' ? 'table-row' : 'none'}">
									<td class="formlabel">
											<insta:ltext
												key="registration.patientdischarge.details.deathdate" />:
									</td>
									<td class="forminfo">
										<c:set var="deathDate"
											value="${empty discharge_details.death_date ? now : discharge_details.death_date}" /> 
											<insta:datewidget name="death_date" id="death_date"
												valueDate="${deathDate}" calButton="true" tabindex="10" />
										<c:set var="deathTime"
											value="${empty discharge_details.death_time ? now : discharge_details.death_time}" />
											<c:set var="deathTimeStr">
												<fmt:formatDate value="${deathTime}" pattern="HH:mm" />
											</c:set> 
											<input type="text" class="timefield" id="death_time"
												name="death_time" value="${deathTimeStr}" tabindex="15" />
									</td>
									<td class="formlabel">
										<insta:ltext
											key="registration.patientdischarge.details.deadonarrival" />:</td>
									<td class="forminfo">
										<insta:selectoptions name="dead_on_arrival"
											value="${discharge_details.dead_on_arrival }"
											class="dropdown" opvalues="N,Y"
											optexts="${yesnolabel}"
											disabled="${discharge_details.discharge_type != 'Death'}" />
									</td>
								</tr>
								<tr id="deathReasonDiv" style="display: ${discharge_details.discharge_type == 'Death' ? 'table-row' : 'none'}">
									<td class="formlabel">
										<insta:ltext
											key="registration.patientdischarge.details.deathreason" />:
									</td>
									<td>
										<insta:selectdb name="death_reason_id" id="death_reason_id"
											table="death_reason_master" displaycol="reason"
											valuecol="reason_id"
											value="${discharge_details.death_reason_id}"
											dummyvalue="${dummyvalue}" filtered="true" orderby="reason" />
									</td>
									<td class="formlabel">
										<insta:ltext
											key="ui.label.stillborn" />:</td>
									<td class="forminfo">
										<insta:selectoptions name="stillborn"
											value="${discharge_details.stillborn }"
											class="dropdown" opvalues="N,Y"
											optexts="${yesnolabel}"
											disabled="${discharge_details.discharge_type != 'Death'}" />
									</td>
									<td class="formlabel"><insta:ltext
											key="ui.label.cause.of.death.icdcode" />:</td>

									<td class="yui-skin-sam forminfo" style="padding-bottom:12px">
										<input type="text" name="cause_of_death_icdcode"
											id="cause_of_death_icdcode"
											value="${discharge_details.cause_of_death_icdcode}"
											disabled="${discharge_details.discharge_type != 'Death'}" />											
									</td>
								</tr>
								<tr>
									<td class="formlabel"><insta:ltext
											key="registration.patientdischarge.details.dischargingdoctor" />:</td>

									<td class="yui-skin-sam forminfo" style="padding-bottom:12px">
										<div id="myAutoComplete" style="width: 140px">
											<input type="text" id="discharge_doc_ac"
												name="discharge_doc_ac" maxlength="50" class="field"
												value="${not empty discharge_details.doctor_name ? discharge_details.doctor_name : patient.doctor_name}" />
											<div id="disDocContainer" style="width: 250px">
											</div>
										</div>
										<input type="hidden" name="discharge_doctor_id"
											id="discharge_doctor_id"
											value="${not empty discharge_details.doctor_id ? discharge_details.doctor_id : patient.doctor}" />
											<span style="position: relative;left: 145px;color: red;">*</span>
									</td>

									<td class="formlabel"><insta:ltext
											key="registration.patientdischarge.details.dischargeRemarks" />:</td>
									<td class="forminfo" colspan="3">
										<textarea cols=30 rows=3 style="max-width: 510px;" name="discharge_remarks" id="discharge_remarks">${discharge_details.discharge_remarks}</textarea>
									</td>
								</tr>
							</c:when>
							<c:otherwise>
								<c:set var="discharge" value="disabled" />
								<tr>
									<td class="forminfo"><b><insta:ltext
										key="registration.patientdischarge.details.oneormorebillsareopen" /></b>
									</td>
								</tr>
							</c:otherwise>
						</c:choose>
					</table>
				</div>
				<div class="buttondiv">
					<input type="button" name="discharge" class="savebutton"
						value="${patient.visit_status eq 'I' ?saveText : dischargeText }"
						${multiCentered && centerId == 0 ? 'disabled' : '' }
						onclick="return onDischarge();" ${discharge } />
				</div>
			</div>
		</fieldset>
	</form>
	<insta:screenlink addPipe="false" screenId="bed_view" label="${bedView}"
		extraParam="?_method=getBedView" />
	<insta:screenlink addPipe="true" screenId="adt" label="ADT"
		extraParam="?_method=getADTScreen" />
	<c:if
		test="${not empty patient.alloc_bed_name && patient.visit_status == 'A'}">
		<insta:screenlink addPipe="true" screenId="ip_bed_details"
			label="${bedDetails}"
			extraParam="?method=getIpBedDetailsScreen&patientid=${patient.patient_id}" />
	</c:if>
</body>
</html>
