<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title><insta:ltext key="services.pendingservicelist.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="ajax.js"/>
	<insta:js-bundle prefix="services.serviceconduction"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.services.serviceconduction.toolbar");
		var cpath = '${cpath}';
		var serviceToolbar = {}
		serviceToolbar.Edit = {title: toolbarOptions["edit"]["name"], imageSrc: 'icons/Edit.png', href: 'Service/Services.do?_method=serviceDetails'};
		serviceToolbar.PrintSRVDoc  ={title: toolbarOptions["printsrvdocument"]["name"], imageSrc: 'icons/Report.png', href: 'Service/ServiceReportsPrint.do?_method=print', target: '_blank'};
		serviceToolbar.ServicePrint ={title: toolbarOptions["serviceprint"]["name"], imageSrc: 'icons/Print.png', href: '/Service/ServicesConductionPrint.do?_method=print',target: '_blank'};


		function init() {
			createToolbar(serviceToolbar);
			initMrNoAutoComplete('${cpath}');
			initServiceAutoComplete();
			
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
			sAutoComp.minQueryLength = 2;
		 	sAutoComp.maxResultsDisplayed = 20;
		 	sAutoComp.forceSelection = false ;
		 	sAutoComp.animVert = false;
		 	sAutoComp.resultTypeList = false;
		 	sAutoComp.typeAhead = false;
		 	sAutoComp.allowBroserAutocomplete =false;
		 	sAutoComp.prehighlightClassname = "yui-ac-prehighlight";
			sAutoComp.autoHighlight = true;
			sAutoComp.useShadow = false;
		 	if (sAutoComp._elTextbox.value != '') {
					sAutoComp._bItemSelected = true;
					sAutoComp._sInitInputValue = sAutoComp._elTextbox.value;
			}
		}
		
	</script>
</head>
<body onload="init();ajaxForPrintUrls();">

<c:set var="mrno">
 <insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="visitid">
 <insta:ltext key="services.pendingservicelist.list.visitid"/>
</c:set>
<c:set var="presdate">
 <insta:ltext key="services.pendingservicelist.list.presdate"/>
</c:set>

<c:set var="type">
 <insta:ltext key="services.pendingservicelist.search.ip"/>,
 <insta:ltext key="services.pendingservicelist.search.op"/>
</c:set>

<c:set var="reportstatus">
 <insta:ltext key="services.pendingservicelist.search.pending"/>,
 <insta:ltext key="services.pendingservicelist.search.inprogress"/>
</c:set>
<c:set var="dept">
 <insta:ltext key="services.pendingservicelist.search.dept"/>
</c:set>
<c:set var="conductionStatuses">
 <insta:ltext key="services.ConductedServicesList.search.Pending"/>,
 <insta:ltext key="services.ConductedServicesList.search.Partial"/>,
 <insta:ltext key="services.ConductedServicesList.search.Reopen"/>
</c:set>

<h1><insta:ltext key="services.pendingservicelist.list.h1"/></h1>
<insta:feedback-panel/>
<form method="GET"  action="Services.do" name="serviceSearchForm">
	<c:set var="servicesList" value="${pagedList.dtoList}"/>
	<c:set var="hasResults" value="${not empty servicesList}"/>
	<input type="hidden" name="_method" value="pendingList"/>
	<input type="hidden" name="_searchMethod" value="pendingList"/>

	<insta:search form="serviceSearchForm" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="services.pendingservicelist.list.mrno"/></div>
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
						<div class="sfLabel"><insta:ltext key="services.pendingservicelist.search.servicedate"/></div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="services.pendingservicelist.search.from"/></div>
								<insta:datewidget name="presc_date" valid="past" id="presc_date0" value="${paramValues.presc_date[0]}" />
							</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="services.pendingservicelist.search.to"/></div>
								<insta:datewidget name="presc_date" valid="past" id="presc_date1" value="${paramValues.presc_date[1]}" />
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
							<input type="hidden" name="serv_dept_id@type" value="y"/>
						</div>
					</td>								
					<td>
						<div class="sfLabel"><insta:ltext key="services.pendingservicelist.search.servicename"/></div>
						<div class="sfFieldSub">
							<input type="text" name="service_name" id="service_name" value="${ifn:cleanHtmlAttribute(param.service_name)}" style="width:122px"/>
							<input type="hidden" name="service_name@op" value="ico"/>
							<div style="width: 600px" id="serviceAcContainer"></div>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="services.pendingservicelist.search.type"/></div>
						<div class="sfField">
							<insta:checkgroup name="visit_type" opvalues="i,o" optexts="${type}"
									selValues="${paramValues['visit_type']}"/>
						</div>
					</td>
					<td class="last">
						<div class="sfLabel"><insta:ltext key="ui.label.report.status"/></div>
						<div class="sfField">
							<insta:checkgroup name="conducted" opvalues="N,P,R" optexts="${conductionStatuses}"
									selValues="${paramValues.conducted}"/>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>

	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" id="resultTable" cellspacing="0" cellpadding="0" style="empty-cells: show">
			<tr>
				<th>#</th>
				<insta:sortablecolumn name="mr_no" title="${mrno}"/>
				<insta:sortablecolumn name="patient_id" title="${visitid}"/>
				<th><insta:ltext key="ui.label.patient.name"/></th>
				<th><insta:ltext key="services.pendingservicelist.list.servicename"/></th>
				<insta:sortablecolumn name="presc_date" title="${presdate}"/>
				<th><insta:ltext key="services.pendingservicelist.list.remarks"/></th>
				<th><insta:ltext key="services.pendingservicelist.list.report"/></th>
			</tr>
			<c:forEach items="${servicesList}" var="service" varStatus="st">
				<c:set var="billPaid" value="${ not (service.bill_type ne 'C' && service.payment_status == 'U')}"/>
				<c:set var="printItem" value="${not empty service.doc_id && service.doc_id != 0}"/>
				<c:choose>
					<c:when test="${billPaid}">
						<c:set var="blockUnpaid" value="false"/>
					</c:when>
					<c:otherwise>
						<c:set var="blockUnpaid" value="${directBillingPrefs.Service.map.block_unpaid == 'Y'}"/>
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${!billPaid}">
						<c:set var="flagColor" value="grey"/>
					</c:when>
					<c:otherwise>
						<c:set var="flagColor" value="empty"/>
					</c:otherwise>
				</c:choose>
				<tr onclick="showToolbar(${st.index}, event, 'resultTable',
						{doc_id: '${service.doc_id}', prescription_id: '${service.prescription_id}'},
						[${!blockUnpaid},${printItem},true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td>${((pagedList.pageNumber-1) * pagedList.pageSize) + st.index + 1}</td>
					<td>${service.mr_no}</td>
					<td>${service.patient_id}</td>
					<td><insta:truncLabel value="${service.patient_full_name}" length="25"/></td>
					<td>
						<c:if test = "${service.conducted == 'R'}">
						<c:set var="flagColor" value="red"/>
						</c:if>
						<c:if test = "${service.conducted == 'P'}">
						<c:set var="flagColor" value="blue"/>
						</c:if>
						<img src="${cpath}/images/${flagColor}_flag.gif"/>
						<c:if test="${not empty service.tooth_unv_number || not empty service.tooth_fdi_number}">
							<c:set var="tooth_number" value="[${empty service.tooth_unv_number ? service.tooth_fdi_number : service.tooth_unv_number}]"/>
						</c:if>
						<insta:truncLabel value="${service.service_name} ${tooth_number}" length="25"/>
					</td>
					<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${service.pres_date}"/></td>
					<td style="white-space: normal;"><insta:truncLabel value="${service.remarks}" length="20"/></td>
					<td><insta:truncLabel value="${service.report_name}" length="10"/></td>
				</tr>
			</c:forEach>
		</table>

	<insta:noresults hasResults="${hasResults}"/>
	<div class="legend" style="margin-top: 10px;">
		<div class="flag"><img src='${cpath}/images/red_flag.gif'/></div>
		<div class="flagText"><insta:ltext key="services.ConductedServicesList.search.Reopen"/></div>
		<div class="flag"><img src='${cpath}/images/blue_flag.gif'/></div>
		<div class="flagText"><insta:ltext key="services.ConductedServicesList.search.Partial"/></div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'/></div>
		<div class="flagText"><insta:ltext key="services.pendingservicelist.list.unpaidbills"/></div>
	</div>

</body>
</html>
