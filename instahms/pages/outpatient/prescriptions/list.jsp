<%@ page pageEncoding="UTF-8"  isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta name="i18nSupport" content="true"/>
	<title>
		<c:choose>
			<c:when test="${empty presc_type}">
				<insta:ltext key="search.outpatient.pending.prescriptions.title"/>
				<c:set var="heading">
					<insta:ltext key="search.outpatient.pending.prescriptions.heading"/>
				</c:set>
			</c:when>
			<c:when test="${presc_type == 'Inv.' && category == 'DEP_LAB'}">
				<insta:ltext key="search.outpatient.pending.prescriptions.title.lab"/>
				<c:set var="heading">
					<insta:ltext key="search.outpatient.pending.prescriptions.heading.lab"/>
				</c:set>
			</c:when>
			<c:when test="${presc_type == 'Inv.' && category == 'DEP_RAD'}">
				<insta:ltext key="search.outpatient.pending.prescriptions.title.rad"/>
				<c:set var="heading">
					<insta:ltext key="search.outpatient.pending.prescriptions.heading.rad"/>
				</c:set>
			</c:when>
			<c:when test="${presc_type == 'Doctor'}">
				<insta:ltext key="search.outpatient.pending.prescriptions.title.doctor"/>
				<c:set var="heading">
					<insta:ltext key="search.outpatient.pending.prescriptions.heading.doctor"/>
				</c:set>
			</c:when>
			<c:when test="${presc_type == 'Service'}">
				<insta:ltext key="search.outpatient.pending.prescriptions.title.service"/>
				<c:set var="heading">
					<insta:ltext key="search.outpatient.pending.prescriptions.heading.service"/>
				</c:set>
			</c:when>
			<c:when test="${presc_type == 'Operation'}">
				<insta:ltext key="search.outpatient.pending.prescriptions.title.operation"/>
				<c:set var="heading">
					<insta:ltext key="search.outpatient.pending.prescriptions.heading.operation"/>
				</c:set>
			</c:when>
		</c:choose>
	</title>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="outpatient/prescriptions/list.js"/>
	<insta:js-bundle prefix="js.search.outpatient.pending.prescriptions"/>
	<insta:js-bundle prefix="search.outpatient.pending.prescriptions"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.search.outpatient.pending.prescriptions.toolbar.option");
		var uriString = <insta:jsString value='<%= (String) request.getAttribute("javax.servlet.forward.request_uri") %>'/>;
		uriString = uriString.replace(cpath+"/", '');
	</script>

</head>
<body onload="init()">
	<h1>${heading}</h1>
	<form name="patpendingprescform">
		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<c:set var="selectPrompt">
			<insta:ltext key="selectdb.dummy.value"/>
		</c:set>
		<insta:search form="patpendingprescform" optionsId="optionalFilter" closed="${hasResults}">
				<div class="searchBasicOpts" >
					<div class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="search.outpatient.pending.prescriptions.mr.no.patient.name"/>:</div>
						<div class="sboFieldInput">
							<div id="mrnoAutoComplete">
								<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
								<input type="hidden" name="mr_no@op" value="ilike" />
								<div id="mrnoContainer"></div>
							</div>
						</div>
					</div>
					<div class="sboField">
						<div class="sboFieldLabel">&nbsp;
							<div class="sboFieldInput">
								<input type="checkbox" name="_mr_no" id="_mr_no" onclick="changeStatus()"/><insta:ltext key="search.outpatient.pending.prescriptions.mr.no.active.only"/>
							</div>
						</div>
					</div>
				</div>
				<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
							<c:choose >
								<c:when test="${empty presc_type}">
									<td>
										<div class="sfLabel"><insta:ltext key="search.outpatient.pending.prescriptions.presc_type"/></div>
										<div class="sfField">
											<insta:selectoptions name="presc_type" opvalues="Inv.,Service,Doctor,Operation"
												optexts="Inv.,Service,Doctor Consultation,Operation"
												dummyvalue="${selectPrompt}" value="${param.presc_type}"/>
										</div>
									</td>
								</c:when>
								<c:otherwise>
									<input type="hidden" name="presc_type" value="${presc_type}"/>
								</c:otherwise>
							</c:choose>
							<td>
								<div class="sfLabel"><insta:ltext key="search.outpatient.pending.prescriptions.department"/>:</div>
								<div class="sfField">
									<insta:selectdb name="dept_id" table="department" valuecol="dept_id" displaycol="dept_name"
										dummyvalue="${selectPrompt}" value="${param.dept_id}" orderby="dept_name"/>
								</div>
							</td>
							<td style="display: ${not empty presc_type ? 'table-cell' : 'none'}">
								<div class="sfLabel"><insta:ltext key="search.outpatient.pending.prescriptions.cond.department"/>:</div>
								<div class="sfField">
									<c:choose>
										<c:when test="${presc_type == 'Inv.'}">
											<insta:selectdb name="cond_dept_id" table="diagnostics_departments" valuecol="ddept_id"
												displaycol="ddept_name" value="${empty param.cond_dept_id ? userDept : param.cond_dept_id}"
											    dummyvalue="${selectPrompt}" filtered="true" filtercol="category,status"
											    filtervalue="${category},A"/>
										</c:when>
										<c:when test="${presc_type == 'Service'}">
											<insta:selectdb name="serv_dept_id" table="services_departments" dummyvalue="${selectPrompt}"
												valuecol="serv_dept_id"  displaycol="department"
												value="${param.cond_dept_id}" orderby="department"/>
											<input type="hidden" name="cond_dept_id@cast" value="y">
										</c:when>
										<c:when test="${presc_type == 'Doctor' || presc_type == 'Operation'}">
											<insta:selectdb name="cond_dept_id" table="department" valuecol="dept_id" displaycol="dept_name"
												dummyvalue="${selectPrompt}" value="${param.cond_dept_id}" orderby="dept_name"/>
										</c:when>
									</c:choose>
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="search.outpatient.pending.prescriptions.presc_status"/>:</div>
								<div class="sfField">
									<c:set var="presc_status_label">
										<insta:ltext key="search.outpatient.pending.prescriptions.presc_status.donotorder"/>,
										<insta:ltext key="search.outpatient.pending.prescriptions.presc_status.ordered"/>,
										<insta:ltext key="search.outpatient.pending.prescriptions.presc_status.inprogress"/>
									</c:set>
									<insta:checkgroup name="exclude_in_qb_presc_status" opvalues="X,O,P" optexts="${presc_status_label}" selValues="${paramValues['exclude_in_qb_presc_status']}"/>
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="search.outpatient.pending.prescriptions.doctor"/>:</div>
								<div class="sfField">
									<insta:selectdb name="doctor_name" table="doctors" valuecol="doctor_id" displaycol="doctor_name"
										dummyvalue="${selectPrompt}" value="${param.doctor_name}" orderby="doctor_name"/>
								</div>
							</td>
							<td class="last">
								<div class="sfLabel"><insta:ltext key="search.outpatient.pending.prescriptions.presc.date"/>:</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.outpatient.pending.prescriptions.presc.date.from"/>:</div>
									<div style="clear:both"></div>
									<insta:datewidget name="prescribed_date" id="prescribed_date0" value="${paramValues.prescribed_date[0]}"/>
									<input type="hidden" name="prescribed_date@type" value="date"/>
									<input type="hidden" name="prescribed_date@op" value="ge,le"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.outpatient.pending.prescriptions.presc.date.to"/>:</div>
									<div style="clear:both"></div>
									<insta:datewidget name="prescribed_date" id="prescribed_date1" value="${paramValues.prescribed_date[1]}"/>
								</div>
							</td>
						</tr>
					</table>
				</div>
		</insta:search>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}"
		totalRecords="${pagedList.totalRecords}" showRecordCount="false"/>
	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable">
			<tr>
				<c:set var="visitMRNo">
					<insta:ltext key="ui.label.mrno"/>
				</c:set>
				<c:set var="visitId">
					<insta:ltext key="search.outpatient.pending.prescriptions.results.visit.id"/>
				</c:set>
				<insta:sortablecolumn name="mr_no" title="${visitMRNo}"/>
				<insta:sortablecolumn name="patient_id" title="${visitId}"/>
				<th><insta:ltext key="ui.label.patient.name"/></th>
				<th><insta:ltext key="search.outpatient.pending.prescriptions.results.doctor"/></th>
				<th><insta:ltext key="search.outpatient.pending.prescriptions.results.presc_type"/></th>
				<th><insta:ltext key="search.outpatient.pending.prescriptions.results.item_name"/></th>
			</tr>
			<c:set var="consId" value="0"/>
			<c:set var="rowIndex" value="0"/>
			<c:set var="patId" value=""/>
			<c:forEach items="${pagedList.dtoList}" var="presc" varStatus="st">
				<c:if test="${presc.map.consultation_id != consId || presc.map.patient_id != patId}">
					<c:set var="rowIndex" value="${rowIndex+1}"/>
					<tr class="${st.first ? 'firstRow' : ''}"
						onclick="showToolbar(${rowIndex}, event, 'resultTable',
						{patient_id: '${presc.map.patient_id}', mr_no: '${presc.map.mr_no}',
							consultation_id: '${presc.map.consultation_id}', prescType : '${presc_type}',
							category: '${ifn:cleanJavaScript(category)}'},
						[true, true], 'patient');"
						onmouseover="hideToolBar(${rowIndex})" id="toolbarRow${rowIndex}">

						<td>${presc.map.mr_no}</td>
						<td>${presc.map.patient_id}</td>
						<td><c:set var="patientFullName" value="${presc.map.patient_full_name}"/>
								<insta:truncLabel value="${patientFullName}" length="20"/></td>
						<td><insta:truncLabel value="${presc.map.doctor_full_name}" length="15"/></td>
						<td></td>
						<td></td>
					</tr>
				</c:if>
				<c:set var="rowIndex" value="${rowIndex + 1}"/>
				<tr class="secondary" onclick="showToolbar(${rowIndex}, event, 'resultTable',
					{patient_id: '${presc.map.patient_id}', mr_no: '${presc.map.mr_no}', patient_presc_id: '${presc.map.patient_presc_id}'},
					[true], 'item');"
					onmouseover="hideToolBar(${rowIndex})" id="toolbarRow${rowIndex}">

						<td class="indent" colspan="4">&nbsp;</td>

						<td>
							<input type="hidden" name="_presc_type" value="${presc.map.presc_type}"/>
							<input type="checkbox" name="_presc_id_${presc.map.patient_id}_${presc.map.consultation_id}" value="${presc.map.patient_presc_id}"/> ${presc.map.presc_type}
						</td>
						<c:set var="itemName">
							${presc.map.item_name}-[<fmt:formatDate pattern="dd-MM-yyyy" value="${presc.map.prescribed_date}"/>]-[${presc.map.qty}]
						</c:set>
						<td>
							<c:choose>
								<c:when test="${presc.map.presc_status == 'X'}">
									<c:set var="flagColor" value="grey"/>
								</c:when>
								<c:when test="${presc.map.presc_status == 'O'}">
									<c:set var="flagColor" value="green"/>
								</c:when>
								<c:when test="${presc.map.presc_status == 'P'}">
									<c:choose>
										<c:when test="${presc.map.prior_auth_required == 'A'}">
											<c:set var="flagColor" value="yellow"/>
										</c:when>
										<c:when test="${presc.map.prior_auth_required == 'S'}">
											<c:set var="flagColor" value="blue"/>
										</c:when>
										<c:otherwise>
											<c:set var="flagColor" value="empty"/>
										</c:otherwise>
									</c:choose>
								</c:when>
							</c:choose>
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
							<insta:truncLabel value="${itemName}" length="60"/>
						</td>
				</tr>
				<c:set var="consId" value="${presc.map.consultation_id}"/>
				<c:set var="patId" value="${presc.map.patient_id}"/>
			</c:forEach>
		</table>
	</div>
	<insta:noresults hasResults="${not empty pagedList.dtoList}"/>
	<div style="margin-top: 10px; ">
		<div style="margin-left: 10px">
			<a href="${javax.servlet.forward.request_uri}?_method=add&defaultScreen=true" title="Add Prescription for the patient">Add New Prescription</a>
		</div>
	</div>
	<div class="legend" style="display: ${ not empty pagedList.dtoList ? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"><insta:ltext key="search.outpatient.pending.prescriptions.flag.donotorder"/></div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText"><insta:ltext key="search.outpatient.pending.prescriptions.flag.ordered"/></div>
		<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
		<div class="flagText"><insta:ltext key="search.outpatient.pending.prescriptions.flag.preauth_maybe_required"/></div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"><insta:ltext key="search.outpatient.pending.prescriptions.flag.required"/></div>
	</div>

</body>
</html>