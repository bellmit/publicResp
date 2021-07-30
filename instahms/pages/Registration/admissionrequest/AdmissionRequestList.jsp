<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap() %>"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
<title><insta:ltext key="patient.admissionrequestlist.screen.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
<insta:js-bundle prefix="patient.admissionrequestlist"/>
<script>
	var toolbarOptions = getToolbarBundle("js.patient.admissionrequestlist.toolbar");
	var psAc;
	function init() {
		psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);
		document.getElementById('_mr_no').checked = true;
		createToolbar(toolbar);
		showFilterActive(document.AdmissionRequestSearchFrom);
	}
	var toolbar = {
		Edit: {
			title: toolbarOptions["admissionrequestaddoreedittoolbar"]["name"],
			imageSrc: "icons/Edit.png",
			href: 'pages/registration/admissionrequest.do?_method=addNewAdmissionRequest',
			onclick: null,
			description: toolbarOptions["admissionrequestaddoreedittoolbar"]["description"]
		},
		ConvertToIp: {
			title: toolbarOptions["admissionrequestconvertiptoolbar"]["name"],
			imageSrc: "icons/Edit.png",
			href: 'pages/registration/admissionrequest.do?_method=convertAdmissionRequestToIp',
			onclick: null,
			description: toolbarOptions["admissionrequestconvertiptoolbar"]["description"]
		},
		Cancel: {
			title: toolbarOptions["admissionrequestcanceltoolbar"]["name"],
			imageSrc: "icons/Edit.png",
			href: 'pages/registration/admissionrequest.do?_method=getCancelAdmissionRequest',
			onclick: null,
			description: toolbarOptions["admissionrequestcanceltoolbar"]["description"]
		}
	};

</script>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
</head>
<body onload="init()">
	<c:set var="hasResults" value="${not empty PagedList.dtoList}"></c:set>
	<h1><insta:ltext key="patient.admissionrequestlist.screen.header"/></h1>

	<insta:feedback-panel/>

	<form name="AdmissionRequestSearchFrom" method="GET">

		<input type="hidden" name="_method" value="getAdmissionRequestList"/>
		<input type="hidden" name="_searchMethod" value="getAdmissionRequestList"/>
		<input type="hidden" name="sortOrder" value="${param.sortOrder}"/>
		<input type="hidden" name="sortReverse" value="${param.sortReverse}"/>

		<insta:search form="AdmissionRequestSearchFrom"  optionsId="optionalFilter" closed="${hasResults}">

			<div class="searchBasicOpts">
				<div class="sboField" style="height: 69px">
					<div class="sboFieldLabel"><insta:ltext key="ui.label.mrno"/></div>
					<div id="mrnoAutoComplete">
						<input type="text" name="par.mr_no" id="mrno" value="${param['par.mr_no']}" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel">&nbsp;
						<div class="sboFieldInput">
							<input type="checkbox" name="_mr_no" id="_mr_no" onclick="changePatientStatus()"/><insta:ltext key="patient.admissionrequestlist.searchlabel.activeonly"/>
						</div>
					</div>
				</div>
			</div>
			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table class="searchFormTable">
						<tr>
							<td>
								<div class="sfLabel"><insta:ltext key="patient.admissionrequestlist.searchlabel.admission.date"/></div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="patient.admissionrequestlist.searchlabel.admission.date.from"/>:</div>
									<div style="clear:both"></div>
									<insta:datewidget name="admission_date" id="admission_date0" value="${paramValues.admission_date[0]}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="patient.admissionrequestlist.searchlabel.admission.date.to"/></div>
									<div style="clear:both"></div>
									<insta:datewidget name="admission_date" id="admission_date1" value="${paramValues.admission_date[1]}"/>
									<input type="hidden" name="admission_date@op" value="ge,le">
									<input type="hidden" name="admission_date@type" value="date">
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="patient.admissionrequestlist.searchlabel.admission.requeted.date"/></div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="patient.admissionrequestlist.searchlabel.admission.requeted.date.from"/>:</div>
									<div style="clear:both"></div>
									<insta:datewidget name="request_date" id="request_date0" value="${paramValues.request_date[0]}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="patient.admissionrequestlist.searchlabel.admission.requeted.date.to"/>:</div>
									<div style="clear:both"></div>
									<insta:datewidget name="request_date" id="request_date1" value="${paramValues.request_date[1]}"/>
									<input type="hidden" name="request_date@op" value="ge,le">
									<input type="hidden" name="request_date@type" value="date">
									<input type="hidden" name="request_date@cast" value="y">
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="patient.admissionrequestlist.searchlabel.status"/></div>
								<div class="sfField">
									<c:set var="statusOptions"><insta:ltext key="patient.admissionrequestlist.search.status.options"/></c:set>
									<insta:checkgroup name="par.status" opvalues="P,I,X" optexts="${statusOptions}" selValues="${paramValues['par.status']}"/>
								</div>
							</td>
						</tr>
						<c:if test="${genericPrefs.max_centers_inc_default > 1 && centerId == 0}">
							<tr>
				 				<td>
				 					<div class="sfLabel"><insta:ltext key="patient.admissionrequestlist.searchlabel.center"/></div>
				 					<div class="sfField">
										<select class="dropdown" name="_center_id" id="_center_id">
											<option value="">-- Select --</option>
											<c:forEach items="${centers}" var="center">
												<option value="${center.map.center_id}" ${center.map.center_id == param._center_id ? 'selected' : ''}>${center.map.center_name}</option>
											</c:forEach>
										</select>
									</div>
								</td>
								<td>&nbsp;</td>
								<td>&nbsp;</td>
							</tr>
						</c:if>
				</table>
			</div>
		</insta:search>

		<insta:paginate curPage="${PagedList.pageNumber}" numPages="${PagedList.numPages}" totalRecords="${PagedList.totalRecords}"/>

		<div class="resultList">
			<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr>
					<c:set var="mrNo">
						<insta:ltext key="ui.label.mrno"/>
					</c:set>
					<c:set var="admRequestedDate">
						<insta:ltext key="patient.admissionrequestlist.tableheader.requested.date"/>
					</c:set>
					<c:set var="admDate">
						<insta:ltext key="patient.admissionrequestlist.tableheader.admission.date"/>
					</c:set>
					<c:set var="centerName">
						<insta:ltext key="patient.admissionrequestlist.tableheader.center.name"/>
					</c:set>
					<c:set var="cancellationDate">
						<insta:ltext key="patient.admissionrequestlist.tableheader.cancellation.date"/>
					</c:set>
					<c:set var="cancelledBy">
						<insta:ltext key="patient.admissionrequestlist.tableheader.cancelled.by"/>
					</c:set>
					<th>#</th>
					<insta:sortablecolumn name="mr_no" title="${mrNo}"/>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<th><insta:ltext key="patient.admissionrequestlist.tableheader.doctor.name"/></th>
					<insta:sortablecolumn name="request_date" title="${admRequestedDate}"/>
					<insta:sortablecolumn name="admission_date" title="${admDate}"/>
					<insta:sortablecolumn name="cancelled_on" title="${cancellationDate}"/>
					<th>${cancelledBy}</th>
					<c:if test="${genericPrefs.max_centers_inc_default > 1 && centerId == 0}">
						<insta:sortablecolumn name="center_id" title="${centerName}"/>
					</c:if>
				</tr>

				<c:forEach var="record" items="${PagedList.dtoList}" varStatus="st">
					<c:set var="isConvertedToIP" value="${record.map.status != 'I' && record.map.status != 'X'}"/>
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{adm_request_id: '${record.map.adm_request_id}', mr_no: '${record.map.mr_no}'},
							[true,${isConvertedToIP},${isConvertedToIP}],'');" id="toolbarRow${st.index}">
						<td>${(PagedList.pageNumber-1) * PagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:choose>
								<c:when test="${record.map.status == 'X'}">
									<c:set var="flagColor" value="red"/>
								</c:when>
								<c:when test="${record.map.status == 'I'}">
									<c:set var="flagColor" value="blue"/>
								</c:when>
								<c:when test="${record.map.status == 'P'}">
									<c:set var="flagColor" value="green"/>
								</c:when>
							</c:choose>
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
							${record.map.mr_no}
						</td>
						<td>${record.map.patient_full_name}</td>
						<td>${record.map.doctor_name}</td>
						<td><fmt:formatDate value="${record.map.request_date}" pattern="dd-MM-yyyy"/></td>
						<td><fmt:formatDate value="${record.map.admission_date}" pattern="dd-MM-yyyy"/></td>
						<td><fmt:formatDate value="${record.map.cancelled_on}" pattern="dd-MM-yyyy"/></td>
						<td>${record.map.cancelled_by}</td>
						<c:if test="${genericPrefs.max_centers_inc_default > 1 && centerId == 0}">
							<td>
								${record.map.center_name}
							</td>
						</c:if>
					</tr>
				</c:forEach>

			</table>

			<c:if test="${param._method == 'getAdmissionRequestList'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>
		<c:url var="url" value="admissionrequest.do">
			<c:param name="_method" value="addNewAdmissionRequest"/>
		</c:url>
		<div style="margin-top: 10px; ">
			<div style="margin-left: 10px">
				<a href="${url}"><insta:ltext key="patient.admissionrequestlist.add.new.admission.request.link"/></a>
			</div>
		</div>
		<div class="legend" style="display: ${ not empty PagedList.dtoList ? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
			<div class="flagText"><insta:ltext key="patient.admissionrequestlist.legend.inprogress"/></div>
			<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
			<div class="flagText"><insta:ltext key="patient.admissionrequestlist.legend.converted.to.ip"/></div>
			<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
			<div class="flagText"><insta:ltext key="patient.admissionrequestlist.legend.flag.red"/></div>
		</div>
	</form>
</body>
</html>
