<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="itemList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty itemList}"/>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><insta:ltext key="generalmasters.hl7configuration.list.title"></insta:ltext></title>
<insta:js-bundle prefix="generalmasters.hl7configuration.list"/>
<insta:js-bundle prefix="generalmasters.hl7configuration.addShow"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.generalmasters.hl7configuration.list.toolbar");
	</script>
	
	<script>
		var toolBar = {
				Edit : {
					title : toolbarOptions["EditHl7Interface"]["name"],
					imageSrc : "icons/Edit.png",
					href : "master/HL7Configuration.do?_method=show",
					onclick : null,
					description :toolbarOptions["EditHl7Interface"]["description"]
					}
			};

		function init() {

			createToolbar(toolBar);
		}

	</script>
</head>
<body onload="init()">

		
	<div class="pageHeader"><insta:ltext key="generalmasters.hl7configuration.list.label"></insta:ltext></div>
	<form name="HL7Configuration" method="get" >
		
		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		
			<insta:search-lessoptions form="HL7Configuration" >
			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="generalmasters.hl7configuration.list.InterfaceType"></insta:ltext></div>
					<div class="sboFieldInput">
						<input type="text" name="interface_type" value="${ifn:cleanHtmlAttribute(param.interface_type)}" />
						<input type="hidden" name="interface_type@op" value="ico"/>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="generalmasters.hl7configuration.list.InterfaceName"></insta:ltext></div>
					<div class="sboFieldInput">
						<input type="text" name="interface_name" value="${ifn:cleanHtmlAttribute(param.interface_name)}" />
						<input type="hidden" name="interface_name@op" value="ico"/>
					</div>
				</div>
				
				<div class="sboField" style="height:68">
					<div class="sboFieldLabel"> <insta:ltext key="generalmasters.hl7configuration.list.Status"></insta:ltext> </div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in" />
					</div>
				</div>
			</div>
	</insta:search-lessoptions>


		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="interface_name" title="Interface Name"/>
					<insta:sortablecolumn name="interface_type" title="Interface Type"/>
					<th>Set Completed Status</th>
					<th>Report Group Method</th>
					<th>Ack Type</th>
					<th>Sending App</th>
					<th>Sending Facility</th>
					<th>Equipment Code Required</th>
					<th>Conducting Doctor Mandatory</th>
					<th>Append Doctor Signature</th>
					<th>Consolidate Multiple OBX</th>
					<th><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.sendorm"/></th>
					<th><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.resultparametersource"/></th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
											{hl7_lab_interface_id:'${record.hl7_lab_interface_id}'},
											[true,null])" 
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if> 
							${record.interface_name}
						</td>
						<td>${record.interface_type}</td>
						<td>${record.set_completed_status}</td>
						<td>${record.report_group_method}</td>
						<td>${record.ack_type}</td>
						<td>${record.sending_app}</td>
						<td>${record.sending_facility}</td>
						<td>${record.equipment_code_required}</td>
						<td>${record.conducting_doctor_mandatory }</td>
						<td>${record.append_doctor_signature}</td>
						<td>${record.consolidate_multiple_obx}</td>
						<td>
							<c:choose>
								<c:when test="${record.send_orm eq 'T'}">
									<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.sendorm.options.onepertest"/>
								</c:when>
								<c:otherwise>
									<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.sendorm.options.oneperresultlabel"/>
								</c:otherwise>
							</c:choose>
						</td>
						<td>
							<c:choose>
								<c:when test="${record.result_parameter_source eq 'M'}">
									<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.resultparameterresource.option.m"/>
								</c:when>
								<c:otherwise>
									<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.resultparameterresource.option.h"/>
								</c:otherwise>
							</c:choose>
						</td>
					</tr>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>
		<c:url var="url" value="HL7Configuration.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />"> <insta:ltext key="generalmasters.hl7configuration.list.AddNewInterface"></insta:ltext> </a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
			<div class="flagText"> <insta:ltext key="generalmasters.hl7configuration.list.Active"></insta:ltext> </div>
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText"> <insta:ltext key="generalmasters.hl7configuration.list.Inactive"></insta:ltext> </div>
		</div>

	</form>
</body>
</html>