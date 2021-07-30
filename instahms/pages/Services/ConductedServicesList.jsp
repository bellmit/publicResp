<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title><insta:ltext key="services.ConductedServicesList.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="ajax.js"/>
	<insta:js-bundle prefix="services.serviceconduction"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.services.serviceconduction.toolbar");
		var cpath = '${cpath}';
		var services = <%= request.getAttribute("services") %>;
		var roleId = '${roleId}';
		var userHasRevertConductionRights= '${(roleId == 1) || (roleId == 2) || actionRightsMap.revert_service_conduction == 'A'}';
		var userHasRevertSignOffRights = '${(roleId == 1) || (roleId == 2) || actionRightsMap.revert_service_signoff == 'A'}';
		var serviceToolbar = {}
			serviceToolbar.ServicePrint ={title: toolbarOptions["serviceprint"]["name"], imageSrc: 'icons/Print.png', href: '/Service/ServicesConductionPrint.do?_method=print',target: '_blank'};
			serviceToolbar.PrintSRVDoc  ={title: toolbarOptions["printsrvdocument"]["name"], imageSrc: 'icons/Report.png', href: 'Service/ServiceReportsPrint.do?_method=print', target: '_blank'};
			serviceToolbar.RevertConduction ={title: toolbarOptions["revertconduction"]["name"], imageSrc: 'icons/Edit.png', href: 'Service/Services.do?_method=revertConduction', show : (userHasRevertConductionRights == 'true')};
			serviceToolbar.RevertSignoff ={title: toolbarOptions["revertsignoff"]["name"], imageSrc: 'icons/Edit.png', href: 'Service/Services.do?_method=revertSignOffReport', show : (userHasRevertSignOffRights == 'true')};
		function init() {
			createToolbar(serviceToolbar);
			initMrNoAutoComplete('${cpath}');
			initServiceAutoComplete();
		}

		function checkForSignOffSelected() {
			var checkBox = document.getElementsByName('prescription_id');
			var anyChecked = false;
			for (var i=0; i<checkBox.length; i++) {
				if (!checkBox[i].disabled && checkBox[i].checked) {
					anyChecked = true;
					break;
				}
			}
			if (!anyChecked) {
				showMessage("js.services.serviceconduction.reportssignoff");
				return false;
			}
			document.signOffReports.submit();
		}

		function initServiceAutoComplete() {
			var datasource = new YAHOO.util.XHRDataSource(cpath + "/Service/Services.do");
			datasource.scriptQueryAppend="_method=search";
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "services",
				fields : [  {key : "service_name"},{key : "service_id"} ]
			};
			var sAutoComp = new YAHOO.widget.AutoComplete('service_name','serviceAcContainer', datasource);
			sAutoComp.minQueryLength = 0;
		 	sAutoComp.maxResultsDisplayed = 20;
		 	sAutoComp.forceSelection = false ;
		 	sAutoComp.animVert = false;
		 	sAutoComp.resultTypeList = false;
		 	sAutoComp.typeAhead = false;
		 	sAutoComp.allowBroserAutocomplete = false;
		 	sAutoComp.prehighlightClassname = "yui-ac-prehighlight";
			sAutoComp.autoHighlight = true;
			sAutoComp.useShadow = false;
		 	if (sAutoComp._elTextbox.value != '') {
				sAutoComp._bItemSelected = true;
				sAutoComp._sInitInputValue = sAutoComp._elTextbox.value;
			}
		}
	</script>
	<insta:js-bundle prefix="services.conductedservices"/>
</head>
<body onload="init();ajaxForPrintUrls();">
<c:set var="mrno">
 <insta:ltext key="services.ConductedServicesList.list.mrno"/>
</c:set>
<c:set var="mrno">
 <insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="visitid">
 <insta:ltext key="services.ConductedServicesList.list.visitid"/>
</c:set>
<c:set var="presdate">
 <insta:ltext key="services.ConductedServicesList.list.presdate"/>
</c:set>
<c:set var="type">
 <insta:ltext key="services.ConductedServicesList.search.ip"/>,
 <insta:ltext key="services.ConductedServicesList.search.op"/>
</c:set>
<c:set var="signedoffreport">
 <insta:ltext key="services.ConductedServicesList.search.SignedOff"/>,
 <insta:ltext key="services.ConductedServicesList.search.Conducted"/>
</c:set>
<c:set var="dept">
 <insta:ltext key="services.ConductedServicesList.search.dept"/>
</c:set>
<h1><insta:ltext key="services.ConductedServicesList.list.h1"/></h1>
<insta:feedback-panel/>
<form method="GET"  action="ConductedServices.do" name="serviceSearchForm">
	<c:set var="servicesList" value="${pagedList.dtoList}"/>
	<c:set var="hasResults" value="${not empty servicesList}"/>
	<input type="hidden" name="_method" value="conductedList"/>
	<input type="hidden" name="_searchMethod" value="conductedList"/>

	<insta:search form="serviceSearchForm" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="services.ConductedServicesList.list.mrno"/></div>
				<div class="sboFieldInput">
					<div id="mrNoAutocomplete">
						<input type="text" name="mr_no" id="mrno" size="10" value="${ifn:cleanHtmlAttribute(param['mr_no'])}"/>
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabeMR No.l"><b><insta:ltext key="services.ConductedServicesList.search.servicedate"/></b></div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="services.ConductedServicesList.search.from"/></div>
								<insta:datewidget name="presc_date" valid="past" id="presc_date0" value="${paramValues.presc_date[0]}" />
							</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="services.ConductedServicesList.search.to"/></div>
								<insta:datewidget name="presc_date" valid="past"  id="presc_date1" value="${paramValues.presc_date[1]}" />
								<input type="hidden" name="presc_date@op" value="ge,le">
							    <input type="hidden" name="presc_date@type" value="date">
							    <input type="hidden" name="presc_date@cast" value="y">
							</div>
						<div class="sfLabel"><insta:ltext key="services.pendingservicelist.search.conductedDate"/></div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="services.pendingservicelist.search.from"/></div>
								<insta:datewidget name="conductedDate" valid="past" id="conductedDate0" value="${paramValues.conductedDate[0]}" />
							</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="services.pendingservicelist.search.to"/></div>
								<insta:datewidget name="conductedDate" valid="past" id="conductedDate1" value="${paramValues.conductedDate[1]}" />
								<input type="hidden" name="conductedDate@op" value="ge,le">
							    <input type="hidden" name="conductedDate@type" value="date">
							    <input type="hidden" name="conductedDate@cast" value="y">
							</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="services.pendingservicelist.search.department"/></div>
						<div class="sfField">
							<select name="serv_dept_id" id="serv_dept_id"  multiple="multiple" class="listbox" optionTitle="true"
										style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; border-right:1px #ccc solid;">
								
								<c:forEach items="${userServDept}" var="ServDept" varStatus="status" >
										<c:set var="selected" value="" />																
										 <c:forEach items="${paramValues.serv_dept_id}" var="serDeptSelected">										
											<c:if test="${serDeptSelected eq ServDept.map.serv_dept_id}">
												<c:set var="selected" value="selected" />
											</c:if>
										</c:forEach>	 						
										 <c:set var="userselected" value=""/> 									
										 <c:forEach items="${userSelectServDept}" var="SelectServDept" >
											<c:if test="${empty userselected && empty paramValues.serv_dept_id}">												
												<c:set var="userselected" value="${ServDept.map.serv_dept_id == SelectServDept.map.serv_dept_id ? 'selected' : ''}"/>										 
										    </c:if>
										</c:forEach> 																				 
									<option value="${ServDept.map.serv_dept_id}"  ${selected} ${userselected}>${ServDept.map.department}</option> 								
								
								</c:forEach>
							</select>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="services.ConductedServicesList.search.servicename"/></div>
						<div class="sfFieldSub">
							<input type="text" name="service_name" id="service_name" value="${ifn:cleanHtmlAttribute(param.service_name)}" style="width:122px;"/>
							<input type="hidden" name="service_name@op" value="ico"/>
							<div style="width: 600px" id="serviceAcContainer"></div>
						</div>
					</td>
					<td >
						<div class="sfLabel"><insta:ltext key="services.ConductedServicesList.search.type"/></div>
						<div class="sfField">
							<insta:checkgroup name="visit_type" opvalues="i,o" optexts="${type}"
									selValues="${paramValues['visit_type']}"/>
						</div>
					</td>
					<td class="last">
						<div class="sfLabel"><insta:ltext key="ui.label.report.status"/></div>
						<div class="sfField">
							<insta:checkgroup name="signed_off" opvalues="S,C" optexts="${signedoffreport}"
									selValues="${paramValues.signed_off}"/>
							<input type="hidden" name="signed_off@type" value="boolean"/>
							<input type="hidden" name="signed_off@cast" value="true"/>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>

	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
<form name="signOffReports" action="ConductedServices.do" method="POST">
	<input type="hidden" name="_method" value="signOffReports"/>
	<div class="resultList">
		<table class="resultList" id="resultTable" cellspacing="0" cellpadding="0" style="empty-cells: show">
			<tr>
				<th><input type="checkbox" name="_checkAllForSignOff" onclick="return checkOrUncheckAll('prescription_id', this)"/></th>
				<insta:sortablecolumn name="mr_no" title="${mrno}"/>
				<insta:sortablecolumn name="patient_id" title="${visitid}"/>
				<th><insta:ltext key="ui.label.patient.name"/></th>
				<insta:sortablecolumn name="presc_date" title="${presdate}"/>
				<th><insta:ltext key="services.ConductedServicesList.list.servicename"/></th>
				<th><insta:ltext key="services.ConductedServicesList.list.report"/></th>
			</tr>
			<c:set var="itemsEnabled" value="false"/>
			<c:forEach items="${servicesList}" var="service" varStatus="st">
				<c:set var="printItem" value="${not empty service.doc_id && service.doc_id != 0}"/>
				<c:set var="noDocument" value="${!printItem}"/>
				<c:set var="revertSignOffEnabled" value="${!noDocument && service.signed_off}"/>
				<c:if test="${printItem && !itemsEnabled && !service.signed_off}">
					<c:set var="itemsEnabled" value="true"/>
				</c:if>
				<c:set var="billPaid" value="${ service.payment_status != 'U' || (service.bill_type != 'P' && service.bill_type != 'M') }"/>
				<c:choose>
					<c:when test="${noDocument}">
						<c:set var="flagColor" value="grey"/>
					</c:when>
					<c:otherwise>
						<c:set var="flagColor" value="empty"/>
					</c:otherwise>
				</c:choose>
				<tr onclick="showToolbar(${st.index}, event, 'resultTable',
						{doc_id: '${service.doc_id}',prescription_id: '${service.prescription_id}'},
						[true,${printItem},${!service.signed_off },${(service.signed_off && !noDocument)}]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td><input type="checkbox" name="prescription_id" id="prescription_id" value="${service.prescription_id}" ${service.signed_off or noDocument ? 'disabled' : ''}></td>
					<td>${service.mr_no}</td>
					<td>${service.patient_id}</td>
					<td><insta:truncLabel value="${service.patient_full_name}" length="15"/></td>
					<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${service.pres_date}"/></td>
					<td>
						<c:if test = "${service.signed_off}">
						<c:set var="flagColor" value="green"/>
						</c:if>
						<img src="${cpath}/images/${flagColor}_flag.gif"/>
						<c:if test="${not empty service.tooth_unv_number || not empty service.tooth_fdi_number}">
							<c:set var="tooth_number" value="[${empty service.tooth_unv_number ? service.tooth_fdi_number : service.tooth_unv_number}]"/>
						</c:if>
						<insta:truncLabel value="${service.service_name} ${tooth_number}" length="15"/>
					</td>
					<td><insta:truncLabel value="${service.report_name}" length="10"/></td>
				</tr>
			</c:forEach>
		</table>
	</div>
	<c:set var="userHasSignOffRights" value="${(roleId == 1) || (roleId == 2) || (actionRightsMap['sign_off_lab_reports'] eq 'A')}"/>
	<div class="screenActions" style="display: ${(userHasSignOffRights && itemsEnabled) ? 'block' : 'none'}">
		<input type="button" name="signeOffReports" value="SignOff Reports" onclick="return checkForSignOffSelected();">
	</div>
	<insta:noresults hasResults="${hasResults}"/>
	<div class="legend" style="margin-top: 10px;">
		<div class="flag"><img src='${cpath}/images/green_flag.gif'/></div>
		<div class="flagText"><insta:ltext key="services.ConductedServicesList.list.signedoffreport"/></div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'/></div>
		<div class="flagText"><insta:ltext key="services.ConductedServicesList.list.noreport"/></div>
	</div>

</body>
</html>
