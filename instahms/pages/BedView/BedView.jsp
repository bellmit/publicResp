<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
	<script>
		dietPrescAvailable = '${preferences.modulesActivatedMap.mod_dietary}';
		opOrder = '${urlRightsMap.new_op_order}';
		ipOrder = '${urlRightsMap.new_ip_order}';
		var issueRights = '${urlRightsMap.patient_inventory_issue}';
		var wardNames = <%= request.getAttribute("wardsJSON") %>;
		var doctorlist = <%= request.getAttribute("doctorlist") %>;
		var index = <%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getAttribute("index")) %>;
		var patientsawaiting = <%= request.getAttribute("patientsawaiting") %>;
		var opencreditbills = <%= request.getAttribute("opencreditbills") %>;
		var selectedConsultantIndex = <%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getAttribute("selectedConsultantIndex")) %>;
		var dischargeStatuses = <%=request.getAttribute("dischargeStatuses")%>;
		var billno = '';
		var patientstartdateanddayslist = <%= request.getAttribute("patientstartdateanddayslist") %>;
		var pendingtests = ${pendingtests};
		var bedNames = ${bedNames};
		var DischargeModuleEnabled ='${preferences.modulesActivatedMap.mod_discharge}';
	</script>

	<title>Bed View - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="script" file="dashboardColors.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="BedView/bedview.js" />
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="script" file="ajax.js" />

</head>
<c:set var="patientList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty patientList}"/>

<body onload="init();">
<div class="pageHeader">Bed View</div>
<form action="BedView.do"	method="GET" name="ipdashboardform">

	<input type="hidden" name="_method" value="getBedView">
	<input type="hidden" name="_searchMethod" value="getBedView"/>
	<input type="hidden" name="mrno" value="">
	<input type="hidden" name="patid" value="">
	<input type="hidden" name="doctorId" value="">
	<input type="hidden" name="pageSize" value="0"/>

	<insta:search form="ipdashboardform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
		<table>
			<tr>
				<td class="sboFieldInput" valign="top">
					<div class="sboFieldLabel">Ward:</div>
					<div class="sfField">
					<select name="ward_no" id="ward_no" onchange="initBedNamesAutocomplete();" class="dropdown">
						<option value="">..All..</option>
						<c:forEach items="${wardName }" var="ward">
							<option value="${ward.map.ward_no }"
									<c:if test="${userWard eq ward.map.ward_no }">selected</c:if>>
								${ward.map.ward_name }
							</option>
						</c:forEach>
					</select>
					</div>
				</td>
				<td>
					<div class="sboFieldLabel">Bed Status:</div>
					<div class="sfField">
						<insta:checkgroup name="occupancy" selValues="${paramValues.occupancy}"
							opvalues="N,Y" optexts="Not Occupied,Occupied"/>
					</div>
				</td>
				<td>
					<div class="sboFieldLabel">Bystander Bed:</div>
					<div class="sfField">
						<insta:checkgroup name="bystander" selValues="${paramValues.bystander}"
							opvalues="true,false" optexts="Yes,No"/>
					</div>
				</td>
			</tr>
		</table>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>

						<div class="searchBasicOpts" >
						<div class="sboField">
							<div class="sboFieldLabel">MR No/Patient Name</div>
							<div class="sboFieldInput">
								<div id="mrnoAutoComplete">
									<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
									<input type="hidden" name="mr_no@op" value="ilike" />
									<div id="mrnoContainer"></div>
								</div>
							</div>
						</div>
					</div>
					</td>
					<td>
						<div class="sfLabel">Doctor</div>
						<div class="sfField">
							<insta:selectdb name="doctor_id" table="doctors" valuecol="doctor_id"
										displaycol="doctor_name" style="width: 11em" dummyvalue="....Select...."
										dummyvalueId=""	values="${paramValues.doctor_id}" orderby="doctor_id"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Admission Date</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="reg_date" valid="past"	id="reg_date0" value="${paramValues.reg_date[0]}" />
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="reg_date" valid="past"	id="reg_date1" value="${paramValues.reg_date[1]}" />
							<input type="hidden" name="reg_date@op" value="ge,le">
						</div>
					</td>
					<td  class="last">
					<div class="searchBasicOpts" >
						<div class="sboField">
							<div class="sfLabel">Bed Name</div>
							<div class="sboFieldInput">
								<div id="bednameAutoComplete">
									<input type="text" name="bed_name" id="bed_name" value="${ifn:cleanHtmlAttribute(param.bed_name)}" />
									<input type="hidden" name="bed_name@op" value="ilike" />
									<div id="bedNamesContainer"></div>
								</div>
							</div>
						</div>
					</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>


	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">

			<tr onmouseover="hideToolBar();">
				<th>Ward Name</th>
				<th>Bed Name</th>
				<th>Bed Status</th>
				<th>Mr No</th>
				<th>Patient Id</th>
				<th>Patient Name</th>
				<th>Admit Date</th>
				<th>Admitting Doctor</th>
				<th>Advance Status</th>
			</tr>
	      <c:forEach var="patient" items="${patientList}" varStatus="st">
    	     <c:set var="flagColor">
   	     		<c:choose>
				<c:when test="${patient.bed_avbl_status == 'Cleaning' || patient.bed_avbl_status == 'Maintainance'}">yellow
				</c:when>
				<c:when test="${patient.bed_avbl_status == 'Occupied' || patient.bed_avbl_status == 'Blocked'}">red
				</c:when>
				<c:otherwise>green</c:otherwise>
				</c:choose>
			 </c:set>

			 <c:set var="bedDetailsEnabled" value="${roleId == '1' || roleId == '2' || actionRightsMap['bed_close'] == 'A'}" />
			 <c:set var="occupiedBed" value="${patient.mr_no != null}"/>
			 <c:set var="blockedBed" value="${patient.status == 'B'}"/>
			 <c:set var="orderEnabled" value="${occupiedBed && (patient.credit_bill_exists == 'true' && patient.bill_status_ok == 'false')}" />
			 <c:set var="dischargeEnabled" value="${occupiedBed && (patient.credit_bill_exists == 'true'
												 	&& patient.bill_status_ok == 'true'
												 	&& urlRightsMap.discharge_summary ne 'N') }" />
			 <c:set var="prescribeDietEnabled" value="${occupiedBed
			 										&& preferences.modulesActivatedMap['mod_dietary'] eq 'Y'}" />
			 <c:set var="availableBed" value="${patient.bed_status == 'A' && patient.occupancy == 'N'}"/>

			 <c:set var="params"
			  value="mrno:'${patient.mr_no}', patient_id:'${patient.patient_id}', patid: '${patient.patient_id}',
					patientid: '${patient.patient_id}' ,billStatusOk:'${patient.bill_status_ok}',
					paymentOk:'${patient.payment_ok}' , bed_id: '${patient.bed_id}', patientId: '${patient.patient_id}'
					,orgid:'${patient.org_id}',visitId: '${patient.patient_id}', visit_type: 'i'" />
			 <fmt:formatDate var="bedAvblDate" value="${patient.avilable_date}" pattern="dd-MM-yyyy"/>
			 <fmt:formatDate var="bedAvblTime" value="${patient.avilable_date}" pattern="HH:mm"/>
			<c:set var="availability"  value="${patient.avilable_date != null? 'Available by':'Available'} ${bedAvblDate} ${bedAvblTime} "/>

				<tr title="${availability}" class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="setOnClickEvent('${patient.mr_no}', '${patient.patient_id}'),
							showToolbar(${st.index}, event, 'resultTable', { ${params}},
							[${!occupiedBed && !blockedBed} , ${bedDetailsEnabled && availableBed } ,${bedDetailsEnabled && occupiedBed } , ${orderEnabled},
							 ${prescribeDietEnabled},
							 ${dischargeEnabled}, true] );"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">

					<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${patient.ward_name}</td>
					<td>${patient.bed_name}</td>
					<td>${patient.bed_avbl_status }</td>
					<td>${patient.mr_no}</td>
					<td>${patient.patient_id}</td>
					<td>${patient.patient_name}</td>
					<td>
						<fmt:formatDate value="${patient.reg_date}" pattern="dd-MM-yyyy" />
						&nbsp;${patient.reg_time }
					</td>
					<td>${patient.doctor_name }</td>
					<c:if test="${patient.credit_bill_exists == 'true'}">
						<c:choose>
							<c:when test="${patient.bill_status_ok == 'true'}">
								<c:if test="${patient.payment_ok == 'true'}">
									<td>Ok to Discharge</td>
								</c:if>
								<c:if test="${patient.payment_ok == 'false'}">
									<td align="left"><font color="#FFA07A">Payment Due</font></td>
								</c:if>
							</c:when>
							<c:otherwise>
								<c:if test="${patient.payment_ok == 'true'}">
									<td>Okay</td>
								</c:if>
								<c:if test="${patient.payment_ok == 'false'}">
									<td align="left"><font color="#FFA07A">Payment Due</font></td>
								</c:if>
							</c:otherwise>
						</c:choose>
					</c:if>
					<c:if test="${patient.credit_bill_exists == 'false'}">
						<td>Okay</td>
					</c:if>
					<c:if test="${patient.credit_bill_exists == null}">
						<td></td>
					</c:if>
				</tr>
		</c:forEach>
	</table>
	<insta:noresults hasResults="${hasResults}"/>
	</div>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></img></div>
		<div class="flagText">Occupied</div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></img></div>
		<div class="flagText">Vacant</div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></img></div>
		<div class="flagText">Maintenance</div>
	</div>
</form>
</body>
</html>

