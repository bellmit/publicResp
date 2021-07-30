<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.dialysismodule.commonvalidations.toolbar");
	</script>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="patient.dialysis.prescriptions.addshow.title"/></title>
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="dialysis/prescriptions.js" />
	<insta:link type="js" file="instaautocomplete.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<insta:js-bundle prefix="dialysismodule.machinedetails"/>
	<script>
		var contextPath = '<%=request.getContextPath()%>';
		var loadStatus = '${presDetails.status}';
		var DialysateDetails = ${DialysateDetails};
		var prescDuration = '${presDetails.duration}';
	</script>

	<insta:js-bundle prefix="clinicaldata.scorecard"/>

</head>
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="statusOptions">
 <insta:ltext key="patient.dialysis.prescriptions.active"/>,
 <insta:ltext key="patient.dialysis.prescriptions.pending"/>,
 <insta:ltext key="patient.dialysis.prescriptions.inactive"/>
</c:set>

<c:set var="saveButton">
 <insta:ltext key="patient.dialysis.prescriptions.save"/>
</c:set>

<c:set var="addButton">
 <insta:ltext key="patient.dialysis.prescriptions.add"/>
</c:set>
<c:set var="cancelButton">
 <insta:ltext key="patient.dialysis.prescriptions.cancel"/>
</c:set>

<c:set var="selectWeekdays">
<insta:ltext key="patient.dialysis.prescriptions.addshow.select"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.mon"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.tue"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.wed"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.thur"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.fri"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.sat"/>,
<insta:ltext key="patient.dialysis.prescriptions.addshow.sun"/>
</c:set>

<c:set var="weekfrequency">

 <insta:ltext key="patient.dialysis.prescriptions.addshow.wf.1"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.wf.2"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.wf.3"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.wf.4"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.wf.5"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.wf.6"/>

</c:set>



<c:set var="prescduration">
<insta:ltext key="patient.dialysis.prescriptions.addshow.0"/>,
<insta:ltext key="patient.dialysis.prescriptions.addshow.1"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.2"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.3"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.4"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.5"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.6"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.7"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.8"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.9"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.10"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.11"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.12"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.24"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.48"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.72"/>
</c:set>





<c:set var="selectDigits">
<insta:ltext key="patient.dialysis.prescriptions.addshow.select"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.1"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.2"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.3"/>
</c:set>

<c:set var="changereason">
 <insta:ltext key="patient.dialysis.prescriptions.addshow.temper.fdeveloped"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.temper.fblocked"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.temper.fstricture"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.temper.finfection"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.temper.others"/>
</c:set>


<c:set var="dialysismem">
<insta:ltext key="patient.dialysis.prescriptions.addshow.hemodialysismembrane"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.polyesterpolymermembrane"/>
</c:set>

<c:set var="heparintype">
 <insta:ltext key="patient.dialysis.prescriptions.addshow.heparin"/>,
 <insta:ltext key="patient.dialysis.prescriptions.addshow.lowmolecularheparin"/>
</c:set>


<c:set var="cpath" value="${pageContext.request.contextPath}" />
<body onload="init();preFillDialysateDetails();setPrescDuration();DisableHeparinType();" class="yui-skin-sam">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="addText"><insta:ltext key="patient.dialysis.common.add"/></c:set>
<c:set var="editText"><insta:ltext key="patient.dialysis.common.edit"/></c:set>
<div class="pageHeader">${method == 'create' ? addText : editText} <insta:ltext key="patient.dialysis.prescriptions.addshow.pageHeader"/></div>
<insta:feedback-panel/>
<form name="searchForm" method="GET" action="${cpath}/dialysis/DialysisPrescriptions.do">
<input type="hidden" name="_method" value="add">
	<table class="search" style="padding-bottom: 12px">
		<tr>
			<td><insta:ltext key="patient.dialysis.prescriptions.addshow.mr.no"/></td>
			<td>
				<div id="mrnoAutoComplete">
					<input type="text" id="mr_no" name="mr_no" style="width: 8em" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
					<div id="mrnoAcDropdown" style="width: 34em"></div>
				</div>
			</td>
		</tr>
	</table>
</form>
<insta:patientgeneraldetails  mrno="${mr_no}" addExtraFields="true" showClinicalInfo="true"/>
<form name="detailsForm" method="post" action="${cpath}/dialysis/DialysisPrescriptions.do">
<input type="hidden" name="dialysis_presc_id" value="${presDetails.dialysis_presc_id}">
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(mr_no)}" />
<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}">
<c:set var="makeReadOnly" value="" />

	<fieldSet class="fieldSetBorder" >
		<table width="100%" border="0" class="formtable">
			<tr>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.doctor"/>:</td>
				<td>
					<insta:selectdb name="presc_doctor" table="doctors" valuecol="doctor_id"
								displaycol="doctor_name" filtered="true"  dummyvalue="${dummyvalue}" value="${presDetails.presc_doctor}" /><span class="star">*</span>
				</td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.prescription.date"/></td>
				<td >
					<c:set var="prescDate" ><fmt:formatDate value="${presDetails.presc_date}" pattern="dd-MM-yyyy" /></c:set>
					<div style="float: left;"><insta:datewidget name="presc_date" valid="past" btnPos="left" value="${prescDate}"  /></div>
					<div style="float: left;"><span class="star">*</span></div>
				</td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.status"/></td>
				<td><insta:selectoptions name="status"
								opvalues="A,P,I" optexts="${statusOptions}" value="${presDetails.status}" /></td>
			</tr>
			<tr>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.start.date"/></td>
				<td >
					<c:set var="startDate" ><fmt:formatDate value="${presDetails.start_date}" pattern="dd-MM-yyyy"/></c:set>
					<div style="float: left"><insta:datewidget name="start_date" valid="past"  btnPos="left" value="${startDate}"/></div>
					<div style="float: left" ><span class="star">*</span></div>
				</td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.end.date"/></td>
				<td >
					<c:set var="endDate" ><fmt:formatDate value="${presDetails.end_date}" pattern="dd-MM-yyyy"/></c:set>
					<insta:datewidget name="end_date" btnPos="left" value="${endDate}"  />
				</td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.dry.weight"/></td>
				<td><input type="text" name="target_weight" class="number" id="target_weight" value="${presDetails.target_weight}" onkeypress="return enterNumOnlyANDdot(event)"></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.dry.weight.date"/></td>
				<c:set var="dryWeightDate"><fmt:formatDate value="${presDetails.dry_wt_date}" pattern="dd-MM-yyyy"/></c:set>
				<td><insta:datewidget name="dry_wt_date" valid="past" btnPos="left" value="${dryWeightDate}"/></td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.diagnosis"/></td>
				<td><input type="text" name="diagnosis" id="diagnosis" size="60" value="${presDetails.diagnosis}" ${makeReadOnly} ></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.day1"/></td>
				<td><insta:selectoptions name="day1"
											opvalues="null,1,2,3,4,5,6,7" optexts="${selectWeekdays}"  value="${presDetails.day1}"  />
				</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.shift"/></td>
								<td><insta:selectoptions name="shift1"
											opvalues="null,1,2,3" optexts="${selectDigits}"  value="${presDetails.shift1}"  />
				</td>

             <td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.duration"/></td>
             <td><insta:selectoptions name="prescDuration" id="prescDuration"  class="number"  style="width:60px"
                                        opvalues="0,1,2,3,4,5,6,7,8,9,10,11,12,24,48,72" optexts="${prescduration}" value="${presDetails.duration}" /><span class="star">*</span>
              <input type="hidden" name="duration" id="duration"  class="number" value="${presDetails.duration}" >
              </td>

			</tr>
			<tr>

			  <td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.day2"/></td>
				<td><insta:selectoptions name="day2"
											opvalues="null,1,2,3,4,5,6,7" optexts="${selectWeekdays}"  value="${presDetails.day2}"  />
				</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.shift"/></td>
								<td><insta:selectoptions name="shift2"
											opvalues="null,1,2,3" optexts="${selectDigits}" value="${presDetails.shift2}"  />
				</td>



			<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.weekly.frequency"/></td>
				<td><insta:selectoptions name="weekly_frequency" id="weekly_frequency" style="width:60px"
											opvalues="1,2,3,4,5,6"  optexts="${weekfrequency}" value="${presDetails.weekly_frequency}"  /><span class="star">*</span>
				</td>


            </tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.day3"/></td>
				<td><insta:selectoptions name="day3"
											opvalues="null,1,2,3,4,5,6,7" optexts="${selectWeekdays}" value="${presDetails.day3}"  />
				</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.shift"/></td>
								<td><insta:selectoptions name="shift3"
											opvalues="null,1,2,3" optexts="${selectDigits}" value="${presDetails.shift3}"  />
				</td>



				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.dialysismembrane"/></td>
								<td style="width: 50px;"><insta:selectoptions name="dialysis_membrane" dummyvalue="${dummyvalue}" dummyvalueid=""
											opvalues="h,p" optexts="${dialysismem}" value="${presDetails.dialysis_membrane}" /></td>




			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.day4"/></td>
				<td><insta:selectoptions name="day4"
											opvalues="null,1,2,3,4,5,6,7" optexts="${selectWeekdays}" value="${presDetails.day4}"  />
				</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.shift"/></td>
								<td><insta:selectoptions name="shift4"
											opvalues="null,1,2,3" optexts="${selectDigits}" value="${presDetails.shift4}"  />
				</td>
				<td></td><td></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.day5"/></td>
				<td><insta:selectoptions name="day5"
											opvalues="null,1,2,3,4,5,6,7" optexts="${selectWeekdays}" value="${presDetails.day5}"  />
				</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.shift"/></td>
								<td><insta:selectoptions name="shift5"
											opvalues="null,1,2,3" optexts="${selectDigits}"  value="${presDetails.shift5}"  />
				</td>
				<td></td><td></td>
			</tr>

			<tr>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.dialysatetype"/></td>
				<td>
					<insta:selectdb name="dialysate_type_id" table="dialysate_type" valuecol="dialysate_type_id"
							onchange="preFillDialysateDetails();" displaycol="dialysate_type_name" filtered="true" value="${presDetails.dialysate_type_id}" dummyvalue="${dummyvalue}"  /><span class="star">*</span>
				</td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.dialyzer"/></td>
				<td>
					<insta:selectdb name="dialyzer_type_id" table="dialyzer_types" valuecol="dialyzer_type_id"
								displaycol="dialyzer_type_name" filtered="true" value="${presDetails.dialyzer_type_id}" dummyvalue="${dummyvalue}"/><span class="star">*</span>
				</td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.dialysatetemp"/></td>
				<td><input type="text" name="dialysate_temp" id="dialysate_temp" value="${presDetails.dialysate_temp}" class="number" ${makeReadOnly} onkeypress="return enterNumOnlyANDdot(event)"></td>
			</tr>
			<tr>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.k"/></td>
				<td><input type="text" name="potassium" id="potassium" value="${presDetails.potassium}" class="number" ${makeReadOnly} onkeypress="return enterNumOnlyANDdot(event)"></td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.ca"/></td>
				<td><input type="text" name="calcium" id="calcium" value="${presDetails.calcium}" class="number" ${makeReadOnly} onkeypress="return enterNumOnlyANDdot(event)"></td>
			</tr>
			<tr>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.mg"/></td>
				<td><input type="text" name="magnesium" id="magnesium" value="${presDetails.magnesium}" class="number" ${makeReadOnly} onkeypress="return enterNumOnlyANDdot(event)"></td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.glucose"/></td>
				<td><input type="text" name="glucose" id="glucose" value="${presDetails.glucose}" class="glucose" ${makeReadOnly} onkeypress="return enterNumOnlyANDdot(event)"></td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.target"/></td>
				<td><input type="text" name="dialysate_temp" id="dialysate_temp" value="${presDetails.dialysate_temp}" class="number" ${makeReadOnly} onkeypress="return enterNumOnlyANDdot(event)"></td>
			</tr>
			<tr>
				<td colspan="8">

					<table class="detailList" id="TaccessTbl" width="100%">
						<tr class="header">
							<th align="left" width="15%"><insta:ltext key="patient.dialysis.prescriptions.addshow.tem.access"/></th>
							<th align="left" width="15%"><insta:ltext key="patient.dialysis.prescriptions.addshow.access.site"/></th>
							<th align="left" width="7%"><insta:ltext key="patient.dialysis.prescriptions.addshow.date.initiation"/></th>
							<th align="left" widht="7%"><insta:ltext key="patient.dialysis.prescriptions.addshow.date.removal"/></th>
							<th align="left" width="7%"><insta:ltext key="patient.dialysis.prescriptions.addshow.doctor.name"/></th>
							<th align="left" widht="7%"><insta:ltext key="patient.dialysis.prescriptions.addshow.hospitalname"/></th>
							<th align="left" width="21%"><insta:ltext key="patient.dialysis.prescriptions.addshow.remarks"/></th>
							<th align="left" width="21%"><insta:ltext key="patient.dialysis.prescriptions.addshow.lastreason"/></th>

							<th width="7%">&nbsp;</th>
							<th width="8%">&nbsp;</th>
						</tr>
					<c:forEach items="${temporaryAccesses}" var="temp" varStatus="st">
						<c:set var="changeReason">

						<c:if test="${temp.access_t_change_reason eq 'd'}">
								<insta:ltext key="patient.dialysis.prescriptions.addshow.temper.fdeveloped"/>
							</c:if>
							<c:if test="${temp.access_t_change_reason eq 'b'}">
								<insta:ltext key="patient.dialysis.prescriptions.addshow.temper.fblocked"/>
							</c:if>
							<c:if test="${temp.access_t_change_reason eq 's'}">
								<insta:ltext key="patient.dialysis.prescriptions.addshow.temper.fstricture"/>
							</c:if>
							<c:if test="${temp.access_t_change_reason eq 'i'}">
								<insta:ltext key="patient.dialysis.prescriptions.addshow.temper.finfection"/>
							</c:if>

						<c:if test="${temp.access_t_change_reason eq 'o'}">
								<insta:ltext key="patient.dialysis.prescriptions.addshow.temper.others"/>
							</c:if>


							</c:set>

						<tr id="row${st.index}">
							<c:set var="dateofinit_t"><fmt:formatDate value="${temp.date_of_intiation_t}" pattern="dd-MM-yyyy"/></c:set>
							<c:set var="dateoffail_t"><fmt:formatDate value="${temp.date_of_failure_t}" pattern="dd-MM-yyyy"/></c:set>
							<input type="hidden" name="temporary_access_type_id" id="" value="${temp.temporary_access_type_id}" />
							<input type="hidden" name="access_type_id_t" id="" value="${temp.access_type_id_t}" />
							<input type="hidden" name="date_of_intiation_t" id="" value="${dateofinit_t}" />
							<input type="hidden" name="access_site_t" id="" value="${temp.access_site_t}"/>
							<input type="hidden" name="date_of_failure_t" id="" value="${dateoffail_t}" />
							<input type="hidden" name="hospital_name_t" id="" value="${temp.hospital_name_t}" />
							<input type="hidden" name="reason_t" id="" value="${temp.reason_t}" />
							<input type="hidden" name="access_t_change_reason" id="" value="${temp.access_t_change_reason}" />
							<input type="hidden" name="doctor_name_t" value="${temp.doctor_name_t}" />
							<input type="hidden" name="selectedrow4temp" id="selectedrow4temp${st.index+1}" value="false"/>
							<input type="hidden" name="added4temp" id="added4temp${st.index+1}" value="N"/>

								<td>${temp.access_type}</td>
								<td>${temp.access_site}</td>
								<td>${dateofinit_t}</td>
								<td>${dateoffail_t}</td>
								<td>${temp.doctor_name}</td>
                                <td><insta:truncLabel value="${temp.hospital_name_t}" length="15"/></td>
								<td><insta:truncLabel value="${temp.reason_t}" length="20"/></td>
								<td>${changeReason}</td>
								<td><img src="${cpath}/icons/delete.gif" onclick="changeElsColorT(${st.index+1}, this);"/></td>
								<td><img src="${cpath}/icons/Edit.png" onclick="onEditT(this)"/></td>
						</tr>
						<c:set var="newIndexFORdummyRow" value="${st.index+1}"/>
					</c:forEach>
						<tr id="" style="display: none">
							<input type="hidden" name="temporary_access_type_id" id="" value="" />
							<input type="hidden" name="access_type_id_t" id="" value="" />
							<input type="hidden" name="date_of_intiation_t" id="" value="" />
							<input type="hidden" name="access_site_t" id="" value=""/>
							<input type="hidden" name="date_of_failure_t" id="" value="" />
							<input type="hidden" name="doctor_name_t" value="" />
							<input type="hidden" name="hospital_name_t" id="" value="" />
							<input type="hidden" name="reason_t" id="" value="" />
							<input type="hidden" name="access_t_change_reason" id="" value="" />
							<input type="hidden" name="selectedrow4temp" id="selectedrow0" value="false"/>
							<input type="hidden" name="added4temp" id="added${st.index+1}" value="N"/>

								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<td><img src="${cpath}/icons/delete.gif" onclick="changeElsColorT('${newIndexFORdummyRow}', this);"/></td>
								<td><img src="${cpath}/icons/Edit.png" onclick="onEditT(this)" /></td>

						</tr>
						<tr>
							<td colspan="9"></td>
							<td>
								<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="showDialogT(this)" >
									<img src="${cpath}/icons/Add.png" align="right"/>
								</button>
							</td>
						</tr>
					</table>

				</td>
			</tr>
			<tr>
				<td colspan="8">

					<table class="detailList" id="PaccessTbl" width="100%">
						<tr class="header">
							<th align="left" width="15%"><insta:ltext key="patient.dialysis.prescriptions.addshow.perm.access"/></th>
							<th align="left" width="15%"><insta:ltext key="patient.dialysis.prescriptions.addshow.access.site"/></th>
							<th align="left" width="7%"><insta:ltext key="patient.dialysis.prescriptions.addshow.date.initiation"/></th>
							<th align="left" width="7%"><insta:ltext key="patient.dialysis.prescriptions.addshow.date.faliure"/></th>
							<th align="left" width="7%"><insta:ltext key="patient.dialysis.prescriptions.addshow.doctor.name"/></th>
							<th align="left" widht="7%"><insta:ltext key="patient.dialysis.prescriptions.addshow.hospitalname"/></th>
							<th align="left" width="21%"><insta:ltext key="patient.dialysis.prescriptions.addshow.remarks"/></th>
							<th align="left" width="21%"><insta:ltext key="patient.dialysis.prescriptions.addshow.lastreason"/></th>



							<th width="7%">&nbsp;</th>
							<th width="8%">&nbsp;</th>
						</tr>

					<c:forEach items="${permanentAccesses}" var="permanent" varStatus="st">
                        <c:set var="changeReason">


                            <c:if test="${permanent.access_p_change_reason eq 'd'}">
								<insta:ltext key="patient.dialysis.prescriptions.addshow.temper.fdeveloped"/>
							</c:if>
							<c:if test="${permanent.access_p_change_reason eq 'b'}">
								<insta:ltext key="patient.dialysis.prescriptions.addshow.temper.fblocked"/>
							</c:if>

							<c:if test="${permanent.access_p_change_reason eq 's'}">
								<insta:ltext key="patient.dialysis.prescriptions.addshow.temper.fstricture"/>
							</c:if>
							 <c:if test="${permanent.access_p_change_reason eq 'i'}">
								<insta:ltext key="patient.dialysis.prescriptions.addshow.temper.finfection"/>
							</c:if>

						    <c:if test="${permanent.access_p_change_reason eq 'o'}">
								<insta:ltext key="patient.dialysis.prescriptions.addshow.temper.others"/>
							</c:if>



						</c:set>



						<tr id="row${st.index}">
							<c:set var="dateofinit_p"><fmt:formatDate value="${permanent.date_of_intiation_p}" pattern="dd-MM-yyyy"/></c:set>
							<c:set var="dateoffail_p"><fmt:formatDate value="${permanent.date_of_removal_p}" pattern="dd-MM-yyyy"/></c:set>
							<input type="hidden" name="permanent_access_type_id" id="" value="${permanent.permanent_access_type_id}" />
							<input type="hidden" name="access_type_id_p" id="" value="${permanent.access_type_id_p}" />
							<input type="hidden" name="date_of_intiation_p" id="" value="${dateofinit_p}" />
							<input type="hidden" name="access_site_p" id="" value="${permanent.access_site_p}"/>
							<input type="hidden" name="date_of_removal_p" id="" value="${dateoffail_p}" />
							<input type="hidden" name="hospital_name_p" id="" value="${permanent.hospital_name_p}" />
							<input type="hidden" name="reason_p" id="" value="${permanent.reason_p}" />
							<input type="hidden" name="access_p_change_reason" id="" value="${permanent.access_p_change_reason}" />
							<input type="hidden" name="doctor_name_p" value="${permanent.doctor_name_p}" />

							<input type="hidden" name="selectedrow4per" id="selectedrow4per${st.index+1}" value="false"/>
							<input type="hidden" name="added4per" id="added4per${st.index+1}" value="N"/>

								<td>${permanent.access_type}</td>
								<td>${permanent.access_site}</td>
								<td>${dateofinit_p}</td>
								<td>${dateoffail_p}</td>
								<td>${permanent.doctor_name}</td>
								<td><insta:truncLabel value="${permanent.hospital_name_p}" length="15"/></td>
								<td><insta:truncLabel value="${permanent.reason_p}" length="20"/></td>
								<td>${changeReason}</td>
								<td><img src="${cpath}/icons/delete.gif" onclick="changeElsColorP(${st.index+1}, this);"/></td>
								<td><img src="${cpath}/icons/Edit.png" onclick="onEditP(this)"/></td>
						</tr>
						<c:set var="newIndexFORdummyRow" value="${st.index+1}"/>
					</c:forEach>
						<tr id="" style="display: none">
							<input type="hidden" name="permanent_access_type_id" id="" value="" />
							<input type="hidden" name="access_type_id_p" id="" value="" />
							<input type="hidden" name="date_of_intiation_p" id="" value="" />
							<input type="hidden" name="access_site_p" id="" value=""/>
							<input type="hidden" name="date_of_removal_p" id="" value="" />
							<input type="hidden" name="reason_p" id="" value="" />
							<input type="hidden" name="doctor_name_p" value="" />
							<input type="hidden" name="hospital_name_p" id="" value="" />
							<input type="hidden" id="" name="access_p_change_reason" value="" />
							<input type="hidden" name="selectedrow4per" id="selectedrow0" value="false"/>
							<input type="hidden" name="added4per" id="added${st.index+1}" value="N"/>

								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<td><img src="${cpath}/icons/delete.gif" onclick="changeElsColorP('${newIndexFORdummyRow}', this);"/></td>
								<td><img src="${cpath}/icons/Edit.png" onclick="onEditP(this)" /></td>

						</tr>
						<tr>
							<td colspan="9"></td>
							<td>
								<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="showDialogP(this)" >
									<img src="${cpath}/icons/Add.png" align="right"/>
								</button>
							</td>
						</tr>
					</table>

				</td>
			</tr>
				<%-- <td  class="formlabel">Access Type:</td>
				<td><insta:selectdb name="access_type_id" table="dialysis_access_types" valuecol="access_type_id"
								displaycol="access_type"  filtered="true"  value="${presDetails.access_type_id}" dummyvalue="----select---" /><span class="star">*</span></td>
				<td  class="formlabel">Access Site:</td>
				<td><insta:selectdb name="access_site_id" table="dialysis_access_sites" valuecol="access_site_id"
								displaycol="access_site" filtered="true"  value="${presDetails.access_site_id}" dummyvalue="----select---" /><span class="star">*</span></td>
								--%>

			<tr>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.dialyste.flow"/></td>
				<td><input type="text" name="dialysate_flow" id="dialysate_flow" value="${presDetails.dialysate_flow}" class="number" ${makeReadOnly} onkeypress="return enterNumOnlyANDdot(event)"></td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.profile"/></td>
				<td colspan="3"><input type="text" name="dialysate_flow_profile" id="dialysate_flow_profile" value="${presDetails.dialysate_flow_profile}" ${makeReadOnly} size="60"></td>
			</tr>
			<tr>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.blood.flow"/></td>
				<td><input type="text" name="blood_flow" id="blood_flow" value="${presDetails.blood_flow}" class="number" ${makeReadOnly} onkeypress="return enterNumOnlyANDdot(event)"></td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.profile"/></td>
				<td colspan="3"><input type="text" name="blood_flow_profile" id="blood_flow_profile" value="${presDetails.blood_flow_profile}" ${makeReadOnly} size="60"></td>
			</tr>
			<tr>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.bicarb"/></td>
				<td><input type="text" name="bicarb" id="bicarb" value="${presDetails.bicarb}" class="number" ${makeReadOnly} onkeypress="return enterNumOnlyANDdot(event)"></td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.profile"/></td>
				<td colspan="3"><input type="text" name="bicarb_profile" id="bicarb_profile" value="${presDetails.bicarb_profile}" size="60" ${makeReadOnly}></td>
			</tr>
			<tr>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.na"/></td>
				<td><input type="text" name="sodium" id="sodium" value="${presDetails.sodium}" class="number" ${makeReadOnly} onkeypress="return enterNumOnlyANDdot(event)"></td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.profile"/></td>
				<td colspan="3"><input type="text" name="sodium_profile" id="sodium_profile" value="${presDetails.sodium_profile}" size="60" ${makeReadOnly}></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.uf.rate"/></td>
				<td><input type="text" name="uf_rate" id="uf_rate" value="${presDetails.uf_rate}" class="number" ${makeReadOnly} onkeypress="return enterNumOnlyANDdot(event)" ></td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.ufr.profile"/></td>
				<td colspan="3"><input type="text" name="ufr_profile" id="ufr_profile" value="${presDetails.ufr_profile}" size="60" ${makeReadOnly}></td>
			</tr>


			<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.heparintype"/>:</td>
								<td style="width: 50px;"><insta:selectoptions name="heparin_type" dummyvalue="${dummyvalue}" dummyvalueid=""
								onchange="DisableHeparinType()"	opvalues="h,i" optexts="${heparintype}" value="${presDetails.heparin_type}" />
		   </td>


		   <td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.lowheparininitaldose"/>:</td>
           <td>
			    <input type="text" name="low_heparin_initial_dose" id ="low_heparin_initial_dose" value="${presDetails.low_heparin_initial_dose}"  class="number" onkeypress="return enterNumAndDot(event)" onblur="totalheparindose()"  />
		   </td>
		   <td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.lowheparinintrimdose"/>:</td>
           <td>
			    <input type="text" name="low_heparin_intrim_dose" id ="low_heparin_intrim_dose" value="${presDetails.low_heparin_intrim_dose}"   class="number" onkeypress="return enterNumAndDot(event)" onblur="totalheparindose()" />
		   </td>


			</tr>

			<tr>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.totalheparin"/>:</td>
				<td><input type="text" class="number" name="heparin_bolus" id="heparin_bolus" value="${presDetails.heparin_bolus}" ${makeReadOnly} onkeypress="return enterNumAndDot(event)" >
				</td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.hourly"/></td>
				<td><input type="text" class="number" name="heparin_hourly" id="heparin_hourly" value="${presDetails.heparin_hourly}" ${makeReadOnly} onkeypress="return enterNumOnly(event)"></td>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.cutoff"/></td>
				<td><input type="text" class="number" name="heparin_cutoff" id="heparin_cutoff" value="${presDetails.heparin_cutoff}" ${makeReadOnly} onkeypress="return enterNumOnly(event)"></td>
			</tr>
			<tr>
				<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.heparin.profile"/></td>
				<td colspan="3"><input type="text" name="heparin_profile" id="heparin_profile" value="${presDetails.heparin_profile}" ${makeReadOnly} size="60"></td>
			</tr>
			<tr>
				<td   class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.comment"/></td>
				<td colspan="3"><textarea name="comments" rows="4" cols="52" ${makeReadOnly}>${presDetails.comments}</textarea></td>
				<td valign="top">
					<table  width="100%" height="100%">
						<tr>
							<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.entered"/></td>
						</tr>
						<tr>
							<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.inactivated"/></td>
						</tr>
						<tr>
							<td  class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.inactivated.date"/></td>
						</tr>
					</table>
				</td>
				<td valign="top">
					<table width="100%" height="100%" >
						<tr><td class="formtext">${presDetails.username}</td></tr>
						<tr><td>${presDetails.deactivated_by}</td></tr>
						<tr><td><fmt:formatDate value="${presDetails.deactivated_time}" pattern="dd-MM-yyyy HH:mm"/></td></tr>
					</table>
				</td>
			</tr>
			<tr>
				<td valign="top"><insta:ltext key="patient.dialysis.prescriptions.addshow.notes"/></td>
				<td colspan="3"><textarea name="notes" rows="4" cols="52" ${makeReadOnly}>${presDetails.notes}</textarea></td>
			</tr>
		</table>
	</fieldSet>
	<div class="screenActions">
		<input type="submit" value="${saveButton}" class="button" onclick="return funValidateAndSubmit();" /> |
		<a href="${cpath}/dialysis/DialysisPrescriptions.do?_method=list"><insta:ltext key="patient.dialysis.prescriptions.addshow.prescriptions.list"/></a>
		| <a href="${cpath}/dialysis/auditlog/AuditLogSearch.do?_method=getAuditLogDetails&al_table=dialysis_prescriptions_audit_log&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="patient.dialysis.prescriptions.addshow.prescription.auditlog"/></a>
	</div>
</form>
	<div name="TempAccessDIV" id="TempAccessDIV" style="visibility: none">
	<div class="bd">
	<fieldset class="fieldSetBorder">
		<table class="formTable">
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.tem.access"/>:</td>
				<td><insta:selectdb name="TaccessTypes" id="TaccessTypes" table="dialysis_access_types" valuecol="access_type_id"
					displaycol="access_type" filtercol="access_mode" filtervalue="T" dummyvalue="${dummyvalue}" dummyvalueId="" onChange="Tempaccesschange()"/></td>


				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.date.initiation"/>:</td>
				<td><insta:datewidget name="Tdate" id="Tdate" /></td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.doctor"/>:</td>
				<td><insta:selectdb name="Tdoctor" id="Tdoctor" table="doctors" valuecol="doctor_id" displaycol="doctor_name"
						dummyvalue="${dummyvalue}" dummyvalueId=""/></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.access.site"/>:</td>
				<td><insta:selectdb name="Tsite" id="Tsite" table="dialysis_access_sites" valuecol="access_site_id" displaycol="access_site" dummyvalue="---Select---" dummyvalueId=""/></td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.date.removal"/>:</td>
				<td><insta:datewidget name="TdateRemoval" id="TdateRemoval"/></td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.remarks"/>:</td>
				<td><textarea name="Tarea" id="Tarea"></textarea></td>
			</tr>

            <tr>
               <td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.hospitalname"/>:</td>
				<td colspan="3" ><input type="text" name="Thname" id="Thname" style="width:370px;" maxlength="200" /></td>
            </tr>
			<tr id="TaccesschangeTR" style="display:none">
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.temper.acr"/></td>
				<td><insta:selectoptions name="Taccesschange" id="Taccesschange" dummyvalue="${dummyvalue}" dummyvalueId=""
								opvalues="d,b,s,i,o" optexts="${changereason}"  value="${access_t_change_reason}" /></td>
            </tr>


		</table>
	</fieldset>
		<div>
			<input type="button" value="${addButton}" onclick="addToTableT()"> |
			<input type="button" value="${cancelButton}" onclick="handleCancelT()">
		</div>
	</div>
	</div>
	<div name="PerAccessDIV" id="PerAccessDIV" style="visibility: none">
	<div class="bd" >
	<fieldset class="fieldSetBorder">
		<table class="formTable">
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.perm.access"/>:</td>
				<td>
					<select class="dropdown" name="PaccessTypes" id="PaccessTypes" onchange="Permanentaccesschange();"> 
						<option value="">-- Select --</option> 
						<c:forEach var="pactiveaccesstypes" items="${permanentAccessTypes}"> 
						<option value="${pactiveaccesstypes.map.access_type_id}">${pactiveaccesstypes.map.access_type}</option> 
						</c:forEach> 
					</select> 
				</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.date.initiation"/>:</td>
				<td><insta:datewidget name="Pdate" id="Pdate"/></td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.doctor"/>:</td>
				<td><insta:selectdb name="Pdoctor" id="Pdoctor" table="doctors" valuecol="doctor_id" displaycol="doctor_name"
						dummyvalue="${dummyvalue}" dummyvalueId=""/></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.access.site"/>:</td>
				<td><insta:selectdb name="Psite" id="Psite" table="dialysis_access_sites" valuecol="access_site_id" displaycol="access_site"
						dummyvalue="${dummyvalue}" dummyvalueId=""/></td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.date.faliure"/>:</td>
				<td><insta:datewidget name="PdateRemoval" id="PdateRemoval" /></td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.remarks"/>:</td>
				<td><textarea name="Parea" id="Parea"></textarea></td>
			</tr>
			<tr>
               <td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.hospitalname"/>:</td>
				<td colspan="3" ><input type="text" name="Phname" id="Phname" style="width:370px;" maxlength="200" /></td>
            </tr>

			<tr id="PaccesschangePer" style="display:none">
				<td class="formlabel"><insta:ltext key="patient.dialysis.prescriptions.addshow.temper.acr"/></td>
				<td><insta:selectoptions name="access_p_change_reason" id="Paccesschange" dummyvalue="${dummyvalue}" dummyvalueId=""
								opvalues="d,b,s,i,o" optexts="${changereason}"  value="${access_p_change_reason}" /></td>

			</tr>
		</table>
	</fieldset>
		<div>
			<input type="button" value="${addButton}" onclick="addToTableP()"> |
			<input type="button" value="${cancelButton}" onclick="handleCancelP()">
		</div>
	</div>
	</div>
</body>
</html>
