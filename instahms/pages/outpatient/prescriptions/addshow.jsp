<%@ page pageEncoding="UTF-8"  isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="genericPrefs" value='<%= GenericPreferencesDAO.getAllPrefs().getMap()%>' />

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta name="i18nSupport" content="true"/>
	<title>
		<c:choose>
			<c:when test="${param._method == 'add'}">
				<insta:ltext key="outpatient.pending.prescriptions.addshow.add.title"/>
				<c:set var="heading">
					<insta:ltext key="outpatient.pending.prescriptions.addshow.add.heading"/>
				</c:set>
			</c:when>
			<c:otherwise>
				<insta:ltext key="outpatient.pending.prescriptions.addshow.show.title"/>
				<c:set var="heading">
					<insta:ltext key="outpatient.pending.prescriptions.addshow.show.heading"/>
				</c:set>
			</c:otherwise>
		</c:choose>
	</title>
	<insta:link type="script" file="outpatient/prescriptions/addshow.js"/>

	<insta:js-bundle prefix="outpatient.pending.prescriptions.addshow"/>
	<style>
		.scrolForContainer .yui-ac-content{
			 max-height:20em;overflow:auto;overflow-x:auto; /* scrolling */
		}
	</style>
	<script>
		var TPArequiresPreAuth = '${TPArequiresPreAuth}';
		var addPresc = ${param._method == 'add'};
		var centerId = ${centerId};
		var defaultScreen = ${not empty param.defaultScreen};
		var usersJson = <%= request.getAttribute("users_json") %>;
		var mod_eclaim_preauth = '${preferences.modulesActivatedMap.mod_eclaim_preauth}';
		var conducting_personnel = '${bean.map.conducting_personnel}';
		var multiPlanExists = <%= request.getAttribute("multiPlanExists") %>;
	</script>
</head>
<body>
	<c:if test="${param._method == 'add' && not empty param.defaultScreen}">
		<c:set var="style"	value="style='float: left'"/>
	</c:if>
	<h1 ${style}>${heading}</h1>

	<c:choose>
		<c:when test="${param._method == 'add' && not empty param.defaultScreen}">
			<c:url var="searchUrl" value="${javax.servlet.forward.request_uri}" />
			<insta:patientsearch searchType="visit" searchUrl="${searchUrl}" buttonLabel="Find" searchMethod="add"
				fieldName="patient_id" showStatusField="true"/>
			<insta:feedback-panel/>
		</c:when>
		<c:otherwise>
			<insta:patientdetails patient="${patient}" showClinicalInfo="true"/>
			<form name="presc_addoredit_form" method="POST" action="${javax.servlet.forward.request_uri}?_method=update">
				<input type="hidden" name="_method" value="update">
				<input type="hidden" name="org_id" id="org_id" value="${patient.org_id}"/>
				<input type="hidden" name="tpa_id" id="tpa_id" value="${patient.primary_sponsor_id}"/>
				<input type="hidden" name="patient_presc_id" value="${param.patient_presc_id}"/> <!-- used for editing prescription -->
				<input type="hidden" name="consultation_id" value="${bean.map.consultation_id}"/> <!-- used for editing prescription -->
				<input type="hidden" name="patient_id" value="${patient.patient_id}"/> <!-- used for new prescriptions. -->
				<input type="hidden" name="test_dept_id" id="test_dept_id" value="${bean.map.dept_id}"/>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.presc_type"/>: </td>
						<td colspan="3">
							<c:choose>
								<c:when test="${param._method == 'add' && empty presc_type}">
									<input type="radio" name="presc_type" value="Inv." onchange="onItemChange()">Investigation
									<input type="radio" name="presc_type" value="Service" onchange="onItemChange()">Service
									<input type="radio" name="presc_type" value="Doctor" onchange="onItemChange()">Doctor
									<input type="radio" name="presc_type" value="Operation" onchange="onItemChange()">Operation
								</c:when>
								<c:otherwise>
									<c:choose>
									<c:when test="${param._method == 'add'}">
										<b>${presc_type == 'Inv.' ? 'Investigation' : presc_type}</b>
										<input type="hidden" name="presc_type" value="${presc_type}">
									</c:when>
									<c:otherwise>
										<b>${bean.map.presc_type == 'Inv.' ? 'Investigation' : bean.map.presc_type}</b>
										<input type="hidden" name="presc_type" value="${bean.map.presc_type}">
									</c:otherwise>
									</c:choose>
								</c:otherwise>
							</c:choose>
						</td>
						<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.presc_doctor"/>: </td>
						<td class="forminfo">${bean.map.doctor_full_name}</td>
					</tr>
				</table>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.fs.item_details"/></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.item"/>: </td>
							<td colspan="5">
								<c:choose>
									<c:when test="${param._method == 'add'}">
										<div id="itemAutocomplete" style="padding-bottom: 20px; width: 450px">
											<input type="text" id="itemName" name="itemName" >
											<div id="itemContainer" style="width: 550px" class="scrolForContainer"></div>
										</div>
									</c:when>
									<c:otherwise>
										<b>${bean.map.item_name}</b>
										<input type="hidden" name="itemName" id="itemName" value="${bean.map.item_name}"/>
									</c:otherwise>
								</c:choose>
								<input type="hidden" name="item_id" id="item_id" value="${bean.map.item_id}"/>
								<input type="hidden" name="priorAuth" id="priorAuth" value=""/>
								<input type="hidden" name="tooth_num_required" id="tooth_num_required" value="${bean.map.tooth_num_required}"/>
								<input type="hidden" name="tooth_number" id="tooth_number"
									value="${genericPrefs.tooth_numbering_system == 'U' ? bean.map.tooth_unv_number : bean.map.tooth_fdi_number}"/>
								<input type="hidden" name="ispackage" id="ispackage" value="${bean.map.ispackage}"/>
							</td>
						</tr>
						<c:set var="serviceExtraInfoDisplay" value="display: none"/>
						<c:if test="${param._method == 'show' && bean.map.presc_type == 'Service'}">
							<c:set var="serviceExtraInfoDisplay" value="display: table-row"/>
						</c:if>
						<tr id="serviceExtraInfoRow" style="${serviceExtraInfoDisplay}">
							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.qty"/>: </td>
							<td><input type="text" name="qty" id="qty" value="${bean.map.qty}" class="number"/></td>
							<td class="formlabel" ><insta:ltext key="outpatient.pending.prescriptions.addshow.tooth_number"/>: </td>
							<td colspan="3">
								<div id="toothNumberDiv" style="width: 120px; float: left;">
									${genericPrefs.tooth_numbering_system == 'U' ? bean.map.tooth_unv_number : bean.map.tooth_fdi_number}
								</div>
								<div class="multiInfoEditBtn"
									style="float: left;margin-left: 10px; display: ${bean.map.tooth_num_required == 'Y' ? 'block' : 'none'}" id="toothNumBtnDiv">
									<a href="javascript:void(0);" onclick="return showToothNumberDialog('add', this);"
										title="Select Tooth Numbers">
										<img src="${cpath}/icons/Edit.png" class="button"/>
									</a>
								</div>
								<div id="toothNumDsblBtnDiv" style="float: left;margin-left: 10px;display: ${bean.map.tooth_num_required == 'Y' ? 'none' : 'block'}">
									<img src="${cpath}/icons/Edit1.png" class="button"/>
								</div>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.remarks"/>: </td>
							<td colspan="5"><textarea style="width: 450px" rows="2" id="item_remarks" name="item_remarks"><c:out value="${bean.map.item_remarks}"/></textarea></td>
						</tr>

					</table>
				</fieldset>
				<fieldset class="fieldSetBorder" style="${mod_eclaim_preauth && not empty patient.primary_sponsor_id ? 'block' : none}" id="insFieldSet">
					<legend class="fieldSetLabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.fs.insurance"/></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.send_for_pre_auth"/>: </td>
							<td>
								<input type="checkbox" name="send_for_pre_auth" id="send_for_pre_auth" value="Y"
									${bean.map.preauth_required == 'Y' ? 'checked disabled' : ''} onchange="setPreAuth(this);">
								<input type="hidden" name="requirePriorAuth" id="requirePriorAuth" value="${bean.map.preauth_required}" />
							</td>
							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.pri_pre_auth_no"/>: </td>
							<td><input type="text" name="pri_pre_auth_no" id="pri_pre_auth_no" value="${bean.map.pri_pre_auth_no}"/></td>
							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.pri_pre_auth_mode"/>: </td>
							<td>
								<insta:selectdb  name="pri_pre_auth_mode_id" id="pri_pre_auth_mode_id" table="prior_auth_modes" valuecol="prior_auth_mode_id"
									displaycol="prior_auth_mode_name" filtered="false" dummyvalue="-- Select --" value="${bean.map.pri_pre_auth_mode_id}"/>
							</td>
						</tr>
						<tr id="secPreAuthRow" style="display: none">
							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.sec_pre_auth_no"/>: </td>
							<td><input type="text" name="sec_pre_auth_no" id="sec_pre_auth_no" value="${bean.map.sec_pre_auth_no}"/></td>
							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.sec_pre_auth_mode"/>: </td>
							<td>
								<insta:selectdb  name="sec_pre_auth_mode_id" id="sec_pre_auth_mode_id" table="prior_auth_modes" valuecol="prior_auth_mode_id"
									displaycol="prior_auth_mode_name" filtered="false" dummyvalue="-- Select --" value="${bean.map.sec_pre_auth_mode_id}"/>
							</td>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.fs.conduction"/></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.conducting_personnel"/>: </td>
							<td>
								<select name="conducting_personnel" id="conducting_personnel" class="dropdown">
									<option value="">-- Select --</option>
								</select>
							</td>
							<td class="formlabel"></td>
							<td></td>
							<td class="formlabel"></td>
							<td></td>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.fs.status"/></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.noorder"/>: </td>
							<td>
								<input type="checkbox" name="chk_do_not_order" id="chk_do_not_order" value="Y" ${bean.map.presc_status == 'X' ? 'checked' : ''}
									onchange="setDoNotOrderStatus(this);"/>
								<input type="hidden" name="do_not_order" id="do_not_order" value="${bean.map.presc_status == 'X' ? 'Y' : 'N'}" />
							</td>

							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.reason"/>: </td>
							<td colspan="3"><textarea name="no_order_reason" id="no_order_reason" style="width: 450px" rows="2">${bean.map.no_order_reason}</textarea></td>
						</tr>
						<tr style="display: ${bean.map.presc_status == 'X' ? 'table-row' : 'none'}">
							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.cancelled_by"/>: </td>
							<td class="forminfo">${bean.map.cancelled_by}</td>
							<td class="formlabel"><insta:ltext key="outpatient.pending.prescriptions.addshow.cancelled_datetime"/>: </td>
							<td class="forminfo"><fmt:formatDate pattern="dd-MM-yyyy hh:mm" value="${bean.map.cancelled_datetime}"/></td>
						</tr>
					</table>
				</fieldset>

				<div style="margin-top: 10px">
					<div style="float: left">
						<input type="button" name="save" value="Save" onclick="onSave();" ${bean.map.presc_status == 'O' ? 'disabled' : ''}/>
						<c:choose>
							<c:when test="${empty presc_type}">
								<c:set var="label">
									<insta:ltext key="search.outpatient.pending.prescriptions.heading"/>
								</c:set>
							</c:when>
							<c:when test="${presc_type == 'Inv.' && category == 'DEP_LAB'}">
								<c:set var="label">
									<insta:ltext key="search.outpatient.pending.prescriptions.heading.lab"/>
								</c:set>
								<c:set var="schedulerScreenId" value="dia_scheduler"/>
								<c:set var="extraParams" value="?method=getScheduleDetails"/>
								<c:set var="schedulerLabel" value="Tests Scheduler"/>
							</c:when>
							<c:when test="${presc_type == 'Inv.' && category == 'DEP_RAD'}">
								<c:set var="label">
									<insta:ltext key="search.outpatient.pending.prescriptions.heading.rad"/>
								</c:set>
								<c:set var="schedulerScreenId" value="dia_scheduler"/>
								<c:set var="extraParams" value="?method=getScheduleDetails"/>
								<c:set var="schedulerLabel" value="Tests Scheduler"/>
							</c:when>
							<c:when test="${presc_type == 'Doctor'}">
								<c:set var="label">
									<insta:ltext key="search.outpatient.pending.prescriptions.heading.doctor"/>
								</c:set>
								<c:set var="schedulerScreenId" value="doc_week_scheduler"/>
								<c:set var="extraParams" value="?method=getWeekView&category=DOC&includeResources=${bean.map.item_id}"/>
								<c:set var="schedulerLabel" value="Doctor Scheduler Week View"/>
							</c:when>
							<c:when test="${presc_type == 'Service'}">
								<c:set var="label">
									<insta:ltext key="search.outpatient.pending.prescriptions.heading.service"/>
								</c:set>
								<c:set var="schedulerScreenId" value="snp_scheduler"/>
								<c:set var="extraParams" value="?method=getScheduleDetails"/>
								<c:set var="schedulerLabel" value="Services Scheduler"/>
							</c:when>
							<c:when test="${presc_type == 'Operation'}">
								<c:set var="label">
									<insta:ltext key="search.outpatient.pending.prescriptions.heading.operation"/>
								</c:set>
								<c:set var="schedulerScreenId" value="ope_scheduler"/>
								<c:set var="extraParams" value="?method=getScheduleDetails"/>
								<c:set var="schedulerLabel" value="Surgery Scheduler"/>
							</c:when>
						</c:choose>
						<insta:screenlink screenId="${screenId}"
							extraParam="?_method=list&sortOrder=mr_no&exclude_in_qb_presc_status=P" addPipe="true"
							label="${label}"/>
						<c:if test="${param._method == 'show'}">
							<c:choose>
								<c:when test="${bean.map.presc_type == 'Inv.'}">
									<c:set var="schedulerScreenId" value="dia_scheduler"/>
									<c:set var="extraParams" value="?method=getScheduleDetails"/>
									<c:set var="schedulerLabel" value="Tests Scheduler"/>
								</c:when>
								<c:when test="${bean.map.presc_type == 'Doctor'}">
									<c:set var="schedulerScreenId" value="doc_week_scheduler"/>
									<c:set var="extraParams" value="?method=getWeekView&category=DOC&includeResources=${bean.map.item_id}"/>
									<c:set var="schedulerLabel" value="Doctor Scheduler Week View"/>
								</c:when>
								<c:when test="${bean.map.presc_type == 'Service'}">
									<c:set var="schedulerScreenId" value="snp_scheduler"/>
									<c:set var="extraParams" value="?method=getScheduleDetails"/>
									<c:set var="schedulerLabel" value="Services Scheduler"/>
								</c:when>
								<c:when test="${bean.map.presc_type == 'Operation'}">
									<c:set var="schedulerScreenId" value="ope_scheduler"/>
									<c:set var="extraParams" value="?method=getScheduleDetails"/>
									<c:set var="schedulerLabel" value="Surgery Scheduler"/>
								</c:when>
							</c:choose>
							<insta:screenlink screenId="${schedulerScreenId}"
								extraParam="${extraParams}" addPipe="true" label="${schedulerLabel}" target="_blank"/>
						</c:if>
					</div>
				</div>

				<div id="toothNumDialog" style="display: none">
					<div class="bd">
						<input type="hidden" name="dialog_type" id="dialog_type" value=""/>
						<fieldset class="fieldSetBorder">
							<legend class="fieldSetLabel">Tooth Numbers(${genericPrefs.tooth_numbering_system == 'U' ? 'UNV' : 'FDI'})</legend>
							<table >
								<tr>
									<td colspan="10" style="border-bottom: 1px solid"><insta:ltext key="outpatient.pending.prescriptions.addshow.pediatric"/>: </td>
								</tr>
								<tr>
									<c:forEach items="${pediac_tooth_numbers}" var="entry" varStatus="st">
										<c:if test="${st.index%10 == 0}">
											</tr><tr>
										</c:if>
										<td style="width: 50px">
											<input type="checkbox" name="d_chk_tooth_number" value="${entry}"/> ${entry}
										</td>
									</c:forEach>
								</tr>
							</table>
							<table >
								<tr>
									<td colspan="10" style="border-bottom: 1px solid"><insta:ltext key="outpatient.pending.prescriptions.addshow.adult"/>: </td>
								</tr>
								<tr>
									<c:forEach items="${adult_tooth_numbers}" var="entry" varStatus="st">
										<c:if test="${st.index%10 == 0}">
											</tr><tr>
										</c:if>
										<td style="width: 50px">
											<input type="checkbox" name="d_chk_tooth_number" value="${entry}"/> ${entry}
										</td>
									</c:forEach>
								</tr>
							</table>
							<table style="margin-top: 10px">
								<tr>
									<td><input type="button" name="toothNumDialog_ok" id="toothNumDialog_ok" value="Ok"></td>
									<td><input type="button" name="toothNumDialog_close" id="toothNumDialog_close" value="Close"></td>
								</tr>
							</table>
						</fieldset>
					</div>
				</div>

			</form>
		</c:otherwise>
	</c:choose>
</body>
</html>