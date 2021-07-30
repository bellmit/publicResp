<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>

<head>
	<title><insta:ltext key="patient.dialysis.sessions.intradialysissessions.title"/></title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.dialysismodule.commonvalidations.toolbar");
		var jscompletionStatus = '${sessionDetails.completion_status}';
	</script>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="dialysis/dialysissessions.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<style type="text/css">
		table.dashboard td table td{
			border: 0px ;
		}
		table.dashboard th {
			background-color: lightgrey;
			padding: 4px 6px 4px 6px;
			font-weight: bold;
			text-align: left;
		}
		.alert { background-color: 	#EAD6BB }

		#dialog_mask.mask {
		    z-index: 1;
		    display:none;
		    position:absolute;
		    top:0;
		    left:0;
		    -moz-opacity: 0.0001;
		    opacity:0.0001;
		    filter: alpha(opacity=50);
		    background-color:#CCC;
		}
		.formtable td.adjCells {
			width: 70px;
		}
	</style>

	<style type="text/css">
	</style>



</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<body onload="initIntra();" class="yui-skin-sam" >
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>

<c:set var="orderedOptions">
<insta:ltext key="patient.dialysis.sessions.intradialysissessions.ordered"/>,
<insta:ltext key="patient.dialysis.sessions.intradialysissessions.prepared"/>,
<insta:ltext key="patient.dialysis.sessions.intradialysissessions.inprogress"/>,
<insta:ltext key="patient.dialysis.sessions.intradialysissessions.completed"/>,
<insta:ltext key="patient.dialysis.sessions.intradialysissessions.closed"/>
</c:set>

<c:set var="alertOptions">
<insta:ltext key="patient.dialysis.sessions.intradialysissessions.info"/>,
<insta:ltext key="patient.dialysis.sessions.intradialysissessions.alert"/>
</c:set>

<c:set var="cancelledOptions">
<insta:ltext key="patient.dialysis.sessions.intradialysissession.discontinued"/>,
<insta:ltext key="patient.dialysis.sessions.intradialysissession.cancelled"/>
</c:set>

<jsp:useBean id="now" class="java.util.Date"/>
<h1 style="float: left"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.header"/></h1>
<div style="float: right; margin-top: 10px">
	<input type="button" name="autoRefreshButton" id="autoRefreshButton" value="Disable Auto Refresh" onclick="toggleAutoRefresh();"/>
</div>
<div style="clear: both"/>
<insta:feedback-panel/>
<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
<form name="intraForm" method="post" action="${cpath}/dialysis/IntraDialysisSessions.do" autocomplete="off">

<input type="hidden" name="_method" value="update">
<input type="hidden" name="actionToDo" value="">
<input type="hidden" name="originalStatus" value="${sessionDetails.status}">
<input type="hidden" name="order_id" value="${ifn:cleanHtmlAttribute(param.order_id) }"/>
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no) }"/>
<input type="hidden" id="dialogId" value=""/>
<input type="hidden" id="rowEditId" value=""/>
<c:set var="makeReadOnly" value="" />
	<c:set var="count" value="${sessionDetails.poll_count > 0 ? sessionDetails.poll_count : 1}"/>
	<fieldSet class="fieldSetBorder" ><legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.session.details"/></legend>
		<table class="formtable" border="0">
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.dialysis.start"/></td>
				<c:set var="dialysis_start_date" ><fmt:formatDate value="${sessionDetails.start_time}" pattern="dd-MM-yyyy" /> </c:set>
				<td class="forminfo">${sessionDetails.start_time}</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.status"/></td>

				<c:choose>
					<c:when test="${sessionDetails.status == 'F' || sessionDetails.status == 'C' }">
						<td class="forminfo">${sessionDetails.status=='F' ? 'Completed' : 'Closed'}</td>
					</c:when>
					<c:otherwise>
						<td>
							<insta:selectoptions name="status" id="status" value="${sessionDetails.status}"
								opvalues="O,P,I,F,C" optexts="${orderedOptions}" onchange="checkCompletionStatus();"/><span class="star">*</span>
						</td>
					</c:otherwise>
				</c:choose>
				<input type="hidden" name="dialysis_status" id="dialysis_status" value="${sessionDetails.status}">
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.start.attendant"/></td>
				<td class="forminfo">${sessionDetails.start_attendant}</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.location"/></td>
				<td class="forminfo">${sessionDetails.location_name}</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.machine"/></td>
				<td class="forminfo">${sessionDetails.machine_name}</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.machine.status"/></td>
				<td class="forminfo">${(sessionDetails.status=='F' || sessionDetails.status=='C')?'-': sessionDetails.machine_status}</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.minbptime"/></td>
				<td class="forminfo">${sessionDetails.min_bp_time}</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.minbp"/></td>
				<td class="forminfo">${sessionDetails.intra_min_bp_high} / ${sessionDetails.intra_min_bp_low}</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.averagepulse"/></td>
				<td class="forminfo"><fmt:formatNumber value="${sessionDetails.total_pulse/count}" pattern="#"/></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.averagevp"/></td>
				<td class="forminfo"><fmt:formatNumber value="${sessionDetails.total_venous_pressure/count}" pattern="#"/></td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.avgdialysatepressure"/></td>
				<td class="forminfo"><fmt:formatNumber value="${sessionDetails.total_dialysate_pressure/count}" pattern="#"/></td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.server.time"/></td>
				<td class="forminfo">
					<fmt:formatDate value="${now}" pattern="HH:mm"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.postequippreparation"/></td>
				<td align="left" style="border:none; padding: 0 2px" >
					<input type="button" name="equipPost" id="equipPost" value=".." title='<insta:ltext key="patient.dialysis.sessions.intradialysissession.postequipment.details"/>'
								onclick="postEquipmentPreparation();"/>
					<div id="dialog2" style="visibility:hidden">
						<div class="bd">
							<table  border="0">
								<c:choose>
								<c:when test="${not empty postPrepDialysisList}">
								<c:forEach var="postDialysisRec" items="${postPrepDialysisList}" varStatus="loop">
									<tr>
										<td>
											<input type="checkbox" name="post_prep_param_name" id="post_prep_param_name${loop.index}" value="N" ${postDialysisRec.map.prep_param_value == 'Y' ? 'checked' : ''}>
											${postDialysisRec.map.prep_param}
											<input type="hidden" name="prep_param_id" id="prep_param_id${loop.index}" value="${postDialysisRec.map.prep_param_id}">
											<input type="hidden" name="prep_param_value" id="prep_param_value${loop.index}" value="N"/>
										</td>
									</tr>
								</c:forEach>
								</c:when>
								<c:otherwise>
									<c:forEach var="postDialysisRec" items="${postPrepDialysisListWithoutOrderId}" varStatus="loop">
									<tr>
										<td>
											<input type="checkbox" name="post_prep_param_name" id="post_prep_param_name${loop.index}" value="N">
											${postDialysisRec.map.prep_param}
											<input type="hidden" name="prep_param_id" id="prep_param_id${loop.index}" value="${postDialysisRec.map.prep_param_id}">
											<input type="hidden" name="prep_param_value" id="prep_param_value${loop.index}" value="N"/>
										</td>
									</tr>
								</c:forEach>
								</c:otherwise>
								</c:choose>
							</table>
						</div>
					</div>
				</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.completion.status"/></td>
				<td>
					<insta:selectoptions id="completion_status" name="completion_status" value="${sessionDetails.completion_status}"
												opvalues="D,X" optexts="${cancelledOptions}" dummyvalue="${dummyvalue}" dummyvalueid="" disabled=""/><span class="star">*</span>
				</td>
			</tr>
		</table>
	</fieldSet>

	<fieldset class="fieldsetborder"><legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.session.notes"/></legend>
		<table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="sessionsTable" border="0">
			<tr>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.time"/></td>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.staffname"/></td>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.plan"/></td>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.intervention"/></td>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.evaluation"/></td>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.discard"/></td>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.edit"/></td>
			</tr>
			<tr id="tableSesRow1">
				<td width="10%">
					<label id="timeSesLabel1"></label>
					<input type="hidden" name="session_time" id="session_time1" value=""></input>
					<input type="hidden" name="session_notes_id" id="session_notes_id1" value=""></input>
				</td>
				<td width="10%">
					<label id="staffNameLabel1"></label>
					<input type="hidden" name="username" id="username1" value=""></input>
				</td>
				<td width="23%">
					<label id="planLabel1"></label>
					<input type="hidden" name="plan" id="plan1"></input>
				</td>
				<td width="23%">
					<label id="interventionLabel1"></label>
					<input type="hidden" name="intervention" id="intervention1"></input>
				</td>
				<td width="24%">
					<label id="evaluationLabel1"></label>
					<input type="hidden" name="evaluation" id="evaluation1"></input>
				</td>
				<td width="10%">
					<label id="discardSesLabel1"></label>
					<input type="hidden" name="discard_sess_ids" id="discard_sess_ids1" value=""></input>
				</td>
				<td width="10%">
					<label id="buttonSesLabel1"></label>
						<img name="addSes" id="addSes1"  src="${cpath}/icons/Add.png" onclick="openSesDialogBox(1);"/>
				</td>
			</tr>
		</table>
	</fieldset>
	 <div id="sesNotesDialog" style="visibility:hidden">
		<div class="bd">
			<fieldSet class="fieldSetBorder">
			<table class="formTable" >
				<tr>
					<td class="formlabel" style="padding-left: 0px"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.time"/>:</td>
					<td><input type="text" name="sessionTime" id="sessionTime" class="number" onblur="checkTimeformat(this);"/></td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.plan"/>:</td>
					<td>
						<textarea name="sessionPlan" id="sessionPlan"></textarea>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.intervention"/>:</td>
					<td><textarea name="interventionField" id="interventionField" ></textarea></td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.evaluation"/>:</td>
					<td><textarea name="evaluationField" id="evaluationField" ></textarea></td>
				</tr>
			</table>
			</fieldSet>
			<table>
				<tr>
					<td><input type="button" value="OK" onclick="handelSesSubmit();"></td>
					<td><input type="button" value="Cancel" onclick="sesDialog.cancel();"></td>
				</tr>
			</table>
		</div>
	</div>


	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.incidents"/></legend>
		<table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="incidentsTable" border="0">
		<input type="hidden" name="presentTime" value="<fmt:formatDate value="${now}" pattern="HH:mm" />" />
			<tr>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.time"/></td>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.type"/></td>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.author"/></td>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.description"/></td>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.unusualevent"/></td>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.discard"/></td>
				<td><insta:ltext key="patient.dialysis.sessions.intradialysissessions.edit"/></td>
			</tr>
			<tr id="tableRow1">
				<td width="10%">
					<label id="timeLabel1"></label>
					<input type="hidden" name="incident_time" id="incident_time1" value="">
					<input type="hidden" name="incident_id" id="incident_id1" value="">
				</td>
				<td width="10%">
					<label id="typeLabel1"></label>
					<input type="hidden" name="incident_type" id="incident_type1" value="">
				</td>
				<td width="10%">
					<label id="authorLabel1"></label>
					<input type="hidden" name="username" id="username1" value="">
				</td>
				<td width="24%">
					<label id="descLabel1"></label>
					<input type="hidden" name="description" id="description1" >
				</td>
				<td width="31%">
					<label id="unusalLabel1"></label>
					<input type="hidden" name="treatment_for_unusual_event" id="treatment_for_unusual_event1" value="" >
				</td>
				<td width="10%">
					<label id="discardLabel1"></label>
					<input type="hidden" name="discard_ids" id="discard_ids1" value="">
				</td>
				<td width="10%">
					<label id="buttonLabel1"></label>
					<img name="add" id="add1" src="${cpath}/icons/Add.png" onclick="openDialogBox(1);"/>
				</td>
			</tr>
		</table>
	</fieldset>
	 <div id="dialog" style="visibility:hidden">
		<div class="bd">
			<fieldSet class="fieldSetBorder">
			<table class="formTable" >
				<tr>
					<td class="formlabel" style="padding-left: 0px"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.time"/>:</td>
					<td><input type="text" name="incidentTime" id="incidentTime" class="number" onblur="checkTimeformat(this);"/></td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.type"/>:</td>
					<td>
						<insta:selectoptions name="incidentType" id="incidentType" value=""
								opvalues="I,A" optexts="${alertOptions}"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.description"/>:</td>
					<td><textarea name="descriptionField" id="descriptionField" ></textarea></td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.unusualevent"/>:</td>
					<td><textarea name="unusaleventField" id="unusaleventField" ></textarea></td>
				</tr>
			</table>
			</fieldSet>
			<table>
				<tr>
					<td><input type="button" value="OK" onclick="handelSubmit();"></td>
					<td><input type="button" value="Cancel" onclick="dialog.cancel();"></td>
				</tr>
			</table>
		</div>
	</div>

	<div class="resultList">
	<table class="dataTable" id="sessionTable" width="100%">

		<tr>
			<td style="width: 10em;"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.time"/>:</td>

			<td title="BP (mmHg)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.bp"/></td>
			<td title="BP Time"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.bpt"/></td>
			<td title="Pulse (bpm)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.pulse"/></td>
			<td title="Total UF Removed (L)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.tuf"/></td>
			<td title="UF Rate (L/hr)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.uf"/></td>
			<td title="Blood Pump Flow Rate (ml/min)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.bpf"/></td>
			<td title="Heparin Pump Infusion Rate (ml/hr)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.hpi"/></td>
			<td title="Dialysate Temp (C)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.dt"/></td>
			<td title="Conductivity (mS/cm)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.c"/></td>
			<td title="Venous Pressure (mmHg)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.vp"/></td>
			<td title="Dialysate Pressure (mmHg)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.dp"/></td>
			<td title="TMP (mmHg)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.tmp"/></td>
			<td title="Dialysis Time (min)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.dt"/></td>
			<td title="Dialysate Flow Rate (ml/min)"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.df"/></td>
			<td title="Discard"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.d"/></td>
			<td style="padding-top: 0px; padding-bottom: 0px" title="Finalize"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.f"/> <input type="checkbox" name="finalizeAllValues" id="finalizeAllValues" onclick="finalizeAll();" /></td>
			<td title="Finalized By"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.fby"/></td>
			<td title="Finalization time"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.ft"/></td>
			<td></td>

		</tr>
		<c:choose>

			<c:when test="${not empty intraSesDetails || sessionDetails.status != 'C'}">
			<c:set var="i" />

				<c:forEach var="ses" items="${intraSesDetails}" varStatus="status">
					<c:set var="i" value="${status.index + 1}"/>

					<tr id="row${i}" bgcolor="">
							<td style="width: 4em"><c:set var="obs_Time"><fmt:formatDate value="${ses.map.obs_time }"  pattern="HH:mm" /></c:set>
							<label id="obstimeLabel${i}">${obs_Time}</label>
							</br>${(ses.map.saved_by_flag=='M'?'(M/C Saved)': (ses.map.saved_by_flag=='U'?'(User Saved)':''))}</td>
							<input type="hidden" name="curent_obs_time" value='<fmt:formatDate value="${now}" pattern="dd-MM-yyyy HH:mm:ss"/>' />
							<input type="hidden" name="obs_time" id="obs_time${i}" value='<fmt:formatDate value="${now}" pattern="dd-MM-yyyy HH:mm:ss"/>'/>

						<td><label id="bphlabel${i}" >${ses.map.bp_high}</label><label id="seperator${i}">/</label><label id="bplowLabel${i}" >${ses.map.bp_low}</label>

						<input type="hidden" id="bp_high${i}" name="bp_high" value="${ses.map.bp_high}"/>
						<input type="hidden" id="bp_low${i}"  name="bp_low" value="${ses.map.bp_low }"/>
						<input type="hidden" id="bp_time${i}" name="bp_time" value="${ses.map.bp_time }"/>
						<input type="hidden" id="pulse_rate${i}" name="pulse_rate" value="${ses.map.pulse_rate}"/>
						<input type="hidden" id="uf_removed${i}" name="uf_removed" value="${ses.map.uf_removed }"/>
						<input type="hidden" id="uf_rate${i}" name="uf_rate" value="${ses.map.uf_rate}"/>
						<input type="hidden" id="observation_id${i}" name="observation_id" value="${ses.map.observation_id}"/>
						<input type="hidden" id="blood_pump_rate${i}" name="blood_pump_rate" value="${ses.map.blood_pump_rate}"/>
						<input type="hidden" id="heparin_rate${i}" name="heparin_rate" value="${ses.map.heparin_rate}"/>
						<input type="hidden" id="dialysate_temp${i}" name="dialysate_temp" value="${ses.map.dialysate_temp }"/>
						<input type="hidden" id="dialysate_cond${i}" name="dialysate_cond" value="${ses.map.dialysate_cond}"/>
						<input type="hidden" id="venous_pressure${i}" name="venous_pressure" value="${ses.map.venous_pressure}"/>
						<input type="hidden" id="dialysate_pressure${i}" name="dialysate_pressure" value="${ses.map.dialysate_pressure}"/>
						<input type="hidden" id="tmp${i}" name="tmp" value="${ses.map.tmp}"/>
						<input type="hidden" id="dialysis_time${i}" name="dialysis_time" value="${ses.map.dialysis_time }"/>
						<input type="hidden" id="dialysate_rate${i}" name="dialysate_rate" value="${ses.map.dialysate_rate }"/>
						<input type="hidden" id="finalforRead${i}" name="" value="${ses.map.finalized}"/>
						<input type="hidden" id="statusforRead${i}" name="" value="${sessionDetails.status}"/>

						</td>
						<c:set var="bpTime"  ><fmt:formatDate value="${ses.map.bp_time}" pattern="HH:mm" /></c:set>
						<td><label  id="bptLabel${i}" >${bpTime}</label></td>

						<td><label id="pulselabel${i}" >${ses.map.pulse_rate}</label></td>

						<td ><label id="ufLabel${i}" >${ses.map.uf_removed }</label>

						</td>
						<td><label id="ufrLabel${i}" >${ses.map.uf_rate }</label></td>
						<td><label id="bloodLabel${i}">${ses.map.blood_pump_rate}</label></td>
						<td><label id="heparinLabel${i}" >${ses.map.heparin_rate }</label></td>
						<td><label id="dialysateLabel${i}" >${ses.map.dialysate_temp }</label></td>
						<td><label id="dialysatecLabel${i}" >${ses.map.dialysate_cond }</label></td>
						<td><label id="venousLabel${i}" >${ses.map.venous_pressure }</label></td>
						<td><label id="dialysatepLabel${i}">${ses.map.dialysate_pressure }</label></td>
						<td><label id="tmpLabel${i}" >${ses.map.tmp }</label></td>
						<td><label id="dialysistLabel${i}" >${ses.map.dialysis_time }</label></td>
						<td><label id="dialysaterLabel${i}" >${ses.map.dialysate_rate }</label></td>
						<td><label id="deleteLabel${i}">
								<c:choose>
									<c:when test="${(ses.map.finalized == 'Y' || sessionDetails.status == 'C')}">
										<img name="discard" src="${cpath}/icons/Deleted.png" />
									</c:when>
									<c:otherwise>
										<img class="imgDelete" name="discard" id="discard${i}" src="${cpath}/icons/Delete.png"
										style="cursor:pointer" onclick="setDiscardValues(this,'${ses.map.observation_id}','${i}');"/>
									</c:otherwise>
								</c:choose>
							</label>
							<input type="hidden" name="discard_obs_id" id="discard_obs_id${i}" value="" />
							<input type="hidden" name="discardValue" id="discardValue${i}" value="false" />
						</td>
						<td><input type="checkbox" tabindex="${((st.index+1)*100)+17}" name="finalized_name"  id="finalized_name${i}"
									${ses.map.finalized == 'Y' ? 'checked' : ''}  ${(ses.map.finalized  == 'Y' || sessionDetails.status == 'C') ? 'disabled' : ''} onclick="setFinalizedValue(${i});"/>
						<c:if test="${ses.map.finalized == 'Y'}">
							<input type="hidden" name="db_finalized" id="db_finalized${i}" value="${ses.map.observation_id}"/>
						</c:if>
						</td>
						<input  type="hidden" name="finalized" id="finalized${i}" value="${ses.map.finalized == 'N' ? ses.map.finalized : ''}"/>
						<input type="hidden" name="noOfCols" value="${i}"/>
						<td>${ses.map.finalized_by}</td>

						<c:set var="dialysis_finalize_date" ><fmt:formatDate value="${ses.map.finalized_time}" pattern="dd-MM-yyyy" /> </c:set>
						<c:choose>
							<c:when test="${dialysis_finalize_date==dialysis_start_date}">
								<td><fmt:formatDate value="${ses.map.finalized_time}" pattern="HH:mm" /></td>
							</c:when>
							<c:otherwise>
								<td><fmt:formatDate value="${ses.map.finalized_time}" pattern="dd-MM-yyyy HH:mm" /></td>
							</c:otherwise>
						</c:choose>
						<td><a href="javaScript:void(0);" name="itemrow" id="itemrow${i}" onclick="getAddDialog(${i});">
						<img id="png${i}" name="edit" src="${cpath}/icons/Edit.png" /></a></td>
					</tr>

				</c:forEach>

		<c:if test="${sessionDetails.status != 'C'}">
			<c:set var="i" value="${i+1}"/>
			<tr id="row${i}">
				<td>Current<br><label id="obstimeLabel${i}"><fmt:formatDate value="${machineStatus.last_polled_time}" pattern="HH:mm"/></label></td>
				<td><label id="bphlabel${i}">${machineStatus.bp_high }</label>
						<label id="seperator${i}"></label>
					<label id="bplowLabel${i}">${machineStatus.bp_low}</label>

					<input type="hidden" name="obs_time" id="obs_time${i}" value='<fmt:formatDate value="${machineStatus.last_polled_time}" pattern="dd-MM-yyyy HH:mm"/>'/>
					<input type="hidden" name="bp_high"  style="width: 3em" id="bp_high${i}" value="${machineStatus.bp_high}" />
					<input type="hidden" name="bp_low"  style="width: 3em" id="bp_low${i}" value="${machineStatus.bp_low}" />
					<input type="hidden" name="observation_id" id="observation_id${i}" value=""/>
					<input type="hidden" name="discard_obs_id" id="discard_obs_id${i}" value="dummy" />
					<input type="hidden" name="machineDetailsIndex" value="${i}">
					<input type="hidden" name="currentMachineTime" value="${machineStatus.last_polled_time}" />
				</td>
					<c:set var="macbpTime"  ><fmt:formatDate value="${machineStatus.bp_time }" pattern="HH:mm" /></c:set>
					<input type="hidden" id="finalforRead${i}" name="" value="Y"/>
					<input type="hidden" id="statusforRead${i}" name="" value="C"/>

					<td><label  id="bptLabel${i}" >${macbpTime }</label>
					<input type="hidden" name="bp_time"  id="bp_time${i}" value="${macbpTime}"  /></td>

					<td><label  id="pulselabel${i}" >${machineStatus.pulse_rate }</label>
					<input type="hidden" name="pulse_rate"  id="pulse_rate${i}" value="${machineStatus.pulse_rate }"  /></td>

					<td><label id="ufLabel${i}" >${machineStatus.uf_removed }</label>
					<input type="hidden" name="uf_removed" id="uf_removed${i}" value="${machineStatus.uf_removed }"  /></td>

					<td><label id="ufrLabel${i}" >${machineStatus.uf_rate }</label>
					<input type="hidden" name="uf_rate"  id="uf_rate${i}" value="${machineStatus.uf_rate }"  /></td>

					<td><label id="bloodLabel${i}">${machineStatus.blood_pump_rate}</label>
					<input type="hidden" name="blood_pump_rate"  id="blood_pump_rate${i}" value="${machineStatus.blood_pump_rate }"  /></td>

					<td><label id="heparinLabel${i}" >${machineStatus.heparin_rate}</label>
					<input type="hidden" name="heparin_rate"  id="heparin_rate${i}" value="${machineStatus.heparin_rate }"  /></td>

					<td><label id="dialysateLabel${i}" >${machineStatus.dialysate_temp }</label>
					<input type="hidden" name="dialysate_temp"  id="dialysate_temp${i}" value="${machineStatus.dialysate_temp }"  /></td>

					<td><label id="dialysatecLabel${i}" >${machineStatus.dialysate_cond }</label>
					<input type="hidden" name="dialysate_cond"  id="dialysate_cond${i}" value="${machineStatus.dialysate_cond }" /></td>

					<td><label id="venousLabel${i}" >${machineStatus.venous_pressure }</label>
					<input type="hidden" name="venous_pressure"  id="venous_pressure${i}" value="${machineStatus.venous_pressure }" /></td>

					<td><label id="dialysatepLabel${i}">${machineStatus.dialysate_pressure }</label>
					<input type="hidden" name="dialysate_pressure"  id="dialysate_pressure${i}" value="${machineStatus.dialysate_pressure }" /></td>

					<td><label id="tmpLabel${i}" >${machineStatus.tmp }</label>
					<input type="hidden" name="tmp"  id="tmp${i}" value="${machineStatus.tmp }" /></td>

					<td><label id="dialysistLabel${i}" >${machineStatus.dialysis_time }</label>
					<input type="hidden" name="dialysis_time"  id="dialysis_time${i}" value="${machineStatus.dialysis_time }"/></td>

					<td><label id="dialysaterLabel${i}" >${machineStatus.dialysate_rate }</label>
					<input type="hidden" name="dialysate_rate"  id="dialysate_rate${i}" value="${machineStatus.dialysate_rate }" /></td>

					<td></td>

					<td>
						<input type="text" name="finalized_name" style="display:none" id="finalized_name${i}" value=""/>
						<input type="hidden" id="finalized${i}" value=""/>
					</td>
					<td></td>
					<td></td>
					<c:choose>
						<c:when test="${sessionDetails.status == 'I'}">
							<td>
								<input type="button" name="saveCurrent" id="saveCurrent" value="Save" onclick="return saveCurrrentValues('Current');">
							</td>
						</c:when>
						<c:when test="${sessionDetails.status == 'F'}">

<!-- 								<input type="button" name="new" id="new" value="+" name="itemrow" id="itemrow${i}" onclick="getAddDialog(${i});">-->
							</td>
						</c:when>
						<c:otherwise></c:otherwise>
					</c:choose>

				</tr>
		</c:if>
		<c:set var="i" value="${i+1}" />
			<tr id="row${i}" >
					<td style="width: 4em"><c:set var="obs_Time"><fmt:formatDate value="${ses.map.obs_time }"  pattern="HH:mm" /></c:set>
						<label id="obstimeLabel${i}">${obs_Time}</label>
					</br>${(ses.map.saved_by_flag=='M'?'(M/C Saved)': (ses.map.saved_by_flag=='U'?'(User Saved)':''))}</td>
					<input type="hidden" name="curent_obs_time" value='<fmt:formatDate value="${now}" pattern="dd-MM-yyyy HH:mm:ss"/>' />
					<input type="hidden" name="obs_time" id="obs_time${i}" value='<fmt:formatDate value="${now}" pattern="dd-MM-yyyy HH:mm:ss"/>'/>

				<td><label   id="bphlabel${i}" ></label>
					<label id="seperator${i}"></label>
					<label   id="bplowLabel${i}" ></label>

					<input type="hidden" id="bp_high${i}" name="bp_high" value=""/>
					<input type="hidden" id="bp_low${i}"  name="bp_low" value=""/>
					<input type="hidden" id="bp_time${i}" name="bp_time" value=""/>
					<input type="hidden" id="pulse_rate${i}" name="pulse_rate" value=""/>
					<input type="hidden" id="uf_removed${i}" name="uf_removed" value=""/>
					<input type="hidden" id="uf_rate${i}" name="uf_rate" value=""/>
					<input type="hidden" id="blood_pump_rate${i}" name="blood_pump_rate" value=""/>
					<input type="hidden" id="heparin_rate${i}" name="heparin_rate" value=""/>
					<input type="hidden" id="dialysate_temp${i}" name="dialysate_temp" value=""/>
					<input type="hidden" id="dialysate_cond${i}" name="dialysate_cond" value=""/>
					<input type="hidden" id="venous_pressure${i}" name="venous_pressure" value=""/>
					<input type="hidden" id="dialysate_pressure${i}" name="dialysate_pressure" value=""/>
					<input type="hidden" id="tmp${i}" name="tmp" value=""/>
					<input type="hidden" id="dialysis_time${i}" name="dialysis_time" value=""/>
					<input type="hidden" id="dialysate_rate${i}" name="dialysate_rate" value=""/>
					<input type="hidden" id="observation_id${i}" name="observation_id" value="new"/>
					<input  type="hidden" name="finalized" id="finalized${i}" value=""/>
					<input type="hidden" id="finalforRead${i}" name="" value=""/>
					<input type="hidden" id="statusforRead${i}" name="" value=""/>


				</td>
				<c:set var="bpTime"  ><fmt:formatDate value="${ses.map.bp_time}" pattern="HH:mm" /></c:set>
				<td><label  id="bptLabel${i}" ></label></td>

				<td><label id="pulselabel${i}" ></label></td>

				<td ><label id="ufLabel${i}" ></label>

				</td>
				<td><label id="ufrLabel${i}" ></label></td>
				<td><label id="bloodLabel${i}"></label></td>
				<td><label id="heparinLabel${i}" ></label></td>
				<td><label id="dialysateLabel${i}" ></label></td>
				<td><label id="dialysatecLabel${i}" ></label></td>
				<td><label id="venousLabel${i}" ></label></td>
				<td><label id="dialysatepLabel${i}"></label></td>
				<td><label id="tmpLabel${i}" ></label></td>
				<td><label id="dialysistLabel${i}" ></label></td>
				<td><label id="dialysaterLabel${i}" ></label></td>
				<td><label id="deleteLabel${i}"/></td>
				<td><label id="finalized_label${i}"/></td>
				<td></td>
				<td></td>

				<td>
					<a href="javaScript:void(0);" name="add" id="itemrow${i}" onclick="getAddDialog(${i});">
					<img id="png${i}" name="add" src="${cpath}/icons/Add.png" /></a>
				</td>
			</tr>

		</c:when>
		<c:otherwise>

			<tr id="row1" >
					<td style="width: 4em"><fmt:formatDate value="${ses.map.obs_time }"  pattern="HH:mm" /></br>${(ses.map.saved_by_flag=='M'?'(M/C Saved)': (ses.map.saved_by_flag=='U'?'(User Saved)':''))}</td>
					<input type="hidden" name="curent_obs_time" id="current_obs_time1" value='<fmt:formatDate value="${now}" pattern="dd-MM-yyyy HH:mm:ss"/>' />
					<input type="hidden" name="obs_time" id="obs_time1" value='<fmt:formatDate value="${now}" pattern="HH:mm"/>'/>
					<label id="obstimeLabel1"></label>
				<td><label   id="bphlabel1" ></label>
						<label id="seperator1"></label>
						<label   id="bplowLabel1" ></label>

						<input type="hidden" id="bp_high1" name="bp_high" value=""/>
						<input type="hidden" id="bp_low1"  name="bp_low" value=""/>
						<input type="hidden" id="bp_time1" name="bp_time" value=""/>
						<input type="hidden" id="pulse_rate1" name="pulse_rate" value=""/>
						<input type="hidden" id="uf_removed1" name="uf_removed" value=""/>
						<input type="hidden" id="uf_rate1" name="uf_rate" value=""/>
						<input type="hidden" id="blood_pump_rate1" name="blood_pump_rate" value=""/>
						<input type="hidden" id="heparin_rate1" name="heparin_rate" value=""/>
						<input type="hidden" id="dialysate_temp1" name="dialysate_temp" value=""/>
						<input type="hidden" id="dialysate_cond1" name="dialysate_cond" value=""/>
						<input type="hidden" id="venous_pressure1" name="venous_pressure" value=""/>
						<input type="hidden" id="dialysate_pressure1" name="dialysate_pressure" value=""/>
						<input type="hidden" id="tmp1" name="tmp" value=""/>
						<input type="hidden" id="dialysis_time1" name="dialysis_time" value=""/>
						<input type="hidden" id="dialysate_rate1" name="dialysate_rate" value=""/>
						<input type="hidden" id="observation_id1" name="observation_id" value="new"/>
						<input  type="hidden" name="finalized" id="finalized1" value=""/>
						<input type="hidden" id="finalforRead1" name="" value=""/>
						<input type="hidden" id="statusforRead1" name="" value=""/>


					</td>
					<c:set var="bpTime"  ><fmt:formatDate value="${ses.map.bp_time}" pattern="HH:mm" /></c:set>
					<td><label  id="bptLabel1" ></label></td>

					<td><label id="pulselabel1" ></label></td>

					<td ><label id="ufLabel1" ></label>

					</td>
					<td><label id="ufrLabel1" ></label></td>
					<td><label id="bloodLabel1"></label></td>
					<td><label id="heparinLabel1" ></label></td>
					<td><label id="dialysateLabel1" ></label></td>
					<td><label id="dialysatecLabel1" ></label></td>
					<td><label id="venousLabel1" ></label></td>
					<td><label id="dialysatepLabel1"></label></td>
					<td><label id="tmpLabel1" ></label></td>
					<td><label id="dialysistLabel1" ></label></td>
					<td><label id="dialysaterLabel1" ></label></td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>

				<td>
					<a href="javaScript:void(0);" name="add" id="itemrow${i}" onclick="getAddDialog(${i});">
					<img id="png${i}" name="add" src="${cpath}/icons/Add.png" /></a>
<!--					<input type="button" name="new" id="new" value="+" name="itemrow" id="itemrow1" onclick="getAddDialog(1);">-->
				</td>
			</tr>

		</c:otherwise>
		</c:choose>

	</table>
	</div>

	<div id="addDilog" style="visibility: hidden">
	<div class="bd">
		<div id="intraSessionFields">
		<fieldSet class="fieldSetBorder">
		<table class="formTable">
			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.time"/>:</td>
				<td class="adjCells"><input type="text" name="obsTime" id="obsTime" class="number" tabindex="1"  onblur="checkTimeformat(this);" maxlength="5" readonly/></td>
			</tr>
			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.bp.mmhg"/></td>
				<td class="adjCells"><input type="text" value="" tabindex="2" name="bpHigh" id="bpHigh"  class="number" onkeypress="return enterNumOnly(event)" readonly="readonly"/></td>
				<td><input type="text"  tabindex="3" name="bpLow" id="bpLow" class="number" onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.bptime"/></td>
				<td class="adjCells"><input type="text" tabindex="4" name="bpTime"  class="timefield" id="bpTime" readonly/></td>
			</tr>
			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.pulsebpm"/></td>
				<td class="adjCells"><input type="text" name="pulseRate"  tabindex="5" class="number" id="pulseRate"  onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>
			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissessions.totaluf"/></td>
				<td class="adjCells"><input type="text" name="ufRemoved"  tabindex="6" class="number" id="ufRemoved"  onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>
			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissession.ufrate"/></td>
				<td class="adjCells"><input type="text" name="ufRate"  tabindex="7" class="number" id="ufRate"  onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>
			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissession.bloodpump"/></td>
				<td class="adjCells"><input type="text" name="bloodPumpRate"  tabindex="8" class="number" id="bloodPumpRate"  onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>

			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissession.heparinpump"/></td>
				<td class="adjCells"><input type="text" name="heparinRate"  tabindex="9" class="number" id="heparinRate"  onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>
			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissession.dialysistemp"/></td>
				<td class="adjCells"><input type="text" name="dialysateTemp"  tabindex="10" class="number" id="dialysateTemp"  onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>

			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissession.conductivity"/></td>
				<td class="adjCells"><input type="text" name="dialysateCond"  tabindex="11" class="number" id="dialysateCond"  onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>
			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissession.venouspressure"/></td>
				<td class="adjCells"><input type="text" name="venousPressure"  tabindex="12" class="number" id="venousPressure"  onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>
			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissession.dialysatepressure"/></td>
				<td class="adjCells"><input type="text" name="dialysatePressure"  tabindex="13" class="number" id="dialysatePressure"  onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>
			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissession.tmpmmhg"/></td>
				<td class="adjCells"><input type="text" name="tmP"  tabindex="14" class="number" id="tmp"  onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>
			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissession.dialysistime"/></td>
				<td class="adjCells"><input type="text" name="dialysisTime"  tabindex="15" class="number" id="dialysisTime"  onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>
			<tr>
				<td class="formLabel"><insta:ltext key="patient.dialysis.sessions.intradialysissession.dialysateflowrate"/></td>
				<td class="adjCells"><input type="text" name="dialysateRate"  tabindex="16" class="number" id="dialysateRate"  onkeypress="return enterNumOnly(event)" readonly/></td>
			</tr>
		</table>
		</fieldSet>
		</div>
		<table>
			<tr>
				<td><input type="button" name="addDilog" value="OK" onclick="intraAddSubmit();"></td>
				<td><input type="button" name="cancelDilog" value="Cancel" onclick="intradialog.cancel();"></td>
			</tr>
		</table>
	</div>
	</div>

	<table>
		<tr><td>&nbsp;</td></tr>
		<tr><td>&nbsp;</td></tr>
	</table>
	<c:url value="DialysisMachineShift.do" var="shifturl">
		<c:param name="_method" value="show"/>
		<c:param name="mr_no" value="${param.mr_no}"/>
		<c:param name="order_id" value="${param.order_id}"/>
		<c:param name="dialysis_presc_id" value="${sessionDetails.prescription_id}"/>
	</c:url>
	<c:url value="DialysisPrescriptions.do" var="prescrurl">
		<c:param name="_method" value="show"/>
		<c:param name="mr_no" value="${mr_no}"/>
	</c:url>
	<div class="screenActions">
		<input type="button" name="save" id="save" tabindex="2000"  value="Save" onclick="return intraSubmitValues();">
		| <a href="javascript:void(0)" onclick="funIntraCancel();"><insta:ltext key="patient.dialysis.sessions.intradialysissession.dialysissessions"/></a>
		| <a href='<c:out value="${prescrurl}"/>'><insta:ltext key="patient.dialysis.sessions.intradialysissession.prescription"/></a>
		| <a href="PreDialysisSessions.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}&dialysisprescId=${sessionDetails.prescription_id}&order_id=${ifn:cleanURL(param.order_id)}&visit_center=${visit_center}"><insta:ltext key="patient.dialysis.sessions.intradialysissession.pre"/></a>
		<c:if test="${sessionDetails.status == 'I'}">
		| <a href='<c:out value="${shifturl}"/>'><insta:ltext key="patient.dialysis.sessions.intradialysissession.machineshift"/></a>
		</c:if>
		<c:if test="${sessionDetails.status == 'F' || sessionDetails.status == 'C'}">
		| <a href="PostDialysisSessions.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}&order_id=${ifn:cleanURL(param.order_id)}"><insta:ltext key="patient.dialysis.sessions.intradialysissession.post"/></a>
		</c:if>
	</div>
</form>
<script>
	var loggedInUser = '${ifn:cleanJavaScript(logedin_user)}';
	var incidents = ${incidents};
	var sessionNts = ${sessionNotes};
	var sessionStatus = '${sessionDetails.status}';
	var isFinalized = '${isFinalized}';
	var cpath = '${cpath}';
	var currentDate = '<fmt:formatDate pattern="dd-MM-yyyy" value="${now}" />';
	var currentDateandTime = '<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${now}" />' ;
</script>
</body>
</html>
