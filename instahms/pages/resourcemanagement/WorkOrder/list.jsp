<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title><insta:ltext key="resourcemanagement.workorder.list.title"/></title>

    <insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/> 
	<insta:link type="script" file="dashboardsearch.js"/> 
	
	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {}
		toolbar.Report= {
			title: "WO Print",
			imageSrc: "icons/Report.png",
			href: 'resourcemanagement/workorder.do?_method=generateWOprint',
			target: '_blank',
			onclick: null,
			description: "WO Print"
		};

		toolbar.Validate= {
			title: "View/Edit",
			imageSrc: "icons/Edit.png",
			href: 'resourcemanagement/workorder.do?_method=getWOScreen',
			onclick: null,
			description: "View and/or Edit Work Order Details",
		};
		function init()
		{
			createToolbar(toolbar);
		}
		
		function changeClose(closeid){
			if(document.getElementById("_close"+closeid).checked){
				document.getElementById("_hidclose"+closeid).value='Y';
				document.getElementById("_hidcancel"+closeid).value = 'N';
				document.getElementById("_cancel"+closeid).checked = false;
			}else{
				document.getElementById("_hidclose"+closeid).value='N';
			}
		}
		
		function changeCancel(cancelid){
			if(document.getElementById("_cancel"+cancelid).checked){
				document.getElementById("_hidcancel"+cancelid).value='Y';
				document.getElementById("_hidclose"+cancelid).value = 'N';
				document.getElementById("_close"+cancelid).checked = false;
			}else{
				document.getElementById("_hidcancel"+cancelid).value='N';
			}
		} 
		
		function validateForm(){
			document.getElementById("_searchMethod").value='';
			document.WorkOrderForm.submit();
		}
	</script>

</head>
<body onload="init()">
	
	<c:set var="WOList" value="${pagedList.dtoList}"/>
	<c:set var="hasResults" value="${not empty WOList}"/>
	<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
	<c:set target="${statusDisplay}" property="O" value="Open"/>
	<c:set target="${statusDisplay}" property="A" value="Approved"/>
	<c:set target="${statusDisplay}" property="R" value="Rejected"/>
	<c:set target="${statusDisplay}" property="C" value="Closed"/>
	<c:set target="${statusDisplay}" property="FC" value="Force Closed"/>
	<c:set target="${statusDisplay}" property="X" value="Cancelled"/>
		
<form  name="WorkOrderForm" action="workorder.do" method="get">

		<input type="hidden" name="_method" value="getWOs"/>
		<input type="hidden" name="_searchMethod" id="_searchMethod" value="getWOs"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		
<h1><insta:ltext key="resourcemanagement.workorder.list.title"/></h1>

<insta:search form="WorkOrderForm" optionsId="optionalFilter" closed="${hasResults}">
	<div class="searchBasicOpts">

		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="resourcemanagement.workorder.list.wono"/>:</div>
			<div class="sboFieldInput">
				<input type="text" name="wo_no" value="${ifn:cleanHtmlAttribute(param.wo_no)}"/>
			</div>
		</div>
	</div> 
	
	<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
		<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel">Supplier:</div>
				<div class="sfField">
					<select name="supplier_id" id="supplier_id" class="dropdown">
						<option value="">---(All)---</option>
							<c:forEach items="${listcentersforsuppliers}" var="supplier">
								<option value="${supplier.map.supplier_code}"  ${supplier.map.supplier_code == param.supplier_id ? 'selected': ''}>${supplier.map.supplier_name}</option>
							</c:forEach>
					</select>
				</div>
			</td>
			<td>
				<div class="sfLabel"><insta:ltext key="resourcemanagement.workorder.list.fromdate"/>:</div>
				<div class="sfField">
					<insta:datewidget name="wo_date" id="wo_date0" value="${paramValues.wo_date[0]}"/>
			    </div>

			    <div class="sfLabel"><insta:ltext key="resourcemanagement.workorder.list.todate"/>:</div>
				<div class="sfField">
					<insta:datewidget name="wo_date" id="wo_date1" value="${paramValues.wo_date[1]}"/>
					<input type="hidden" name="wo_date@op" value="ge,le"/>
			    </div>
			</td>
			<td>
				<div class="sfLabel">Status:</div>
				<div class="sfField">
					<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="O,A,R,C,FC,X" optexts="Open,Approved,Rejected,Closed,Force Closed,Cancelled"/>
				</div>
			</td>
		</tr>
		</table>
	</div>
</insta:search>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable"  onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<insta:sortablecolumn name="wo_no" title="WO NO"/>
					<th>Raised By</th>
					<insta:sortablecolumn name="supplier_name" title="Supplier"/>
					<th>Status</th>
					<insta:sortablecolumn name="wo_date" title="Raised Date"/>
					<th>Close</th>
					<th>Cancel</th>
				</tr>

				 <c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					 <tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{wo_no: '${record.wo_no}'},'');" id="toolbarRow${st.index}">
							
						<c:set var="flagColor">
							<c:choose>
								<c:when test="${record.status == 'O'}"><insta:ltext key="resourcemanagement.workorder.list.yellow"/></c:when>
								<c:when test="${record.status == 'A'}"><insta:ltext key="resourcemanagement.workorder.list.green"/></c:when>
								<c:when test="${record.status == 'R'}"><insta:ltext key="resourcemanagement.workorder.list.dark_blue"/></c:when>
								<c:when test="${record.status == 'C'}"><insta:ltext key="resourcemanagement.workorder.list.grey"/></c:when>
								<c:when test="${record.status == 'FC'}"><insta:ltext key="resourcemanagement.workorder.list.blue"/></c:when>
								<c:otherwise><insta:ltext key="resourcemanagement.workorder.list.red"/></c:otherwise>
							</c:choose>
						</c:set>
						<td><insta:truncLabel value="${record.wo_no}" length="30"/>
							<input type="hidden" name="_wono" id="_wono${st.index}" value="${record.wo_no }">
						</td>
						<td><insta:truncLabel value="${record.raised_by}" length="30"/></td>
						<td><insta:truncLabel value="${record.supplier_name}" length="30"/></td>
						<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${statusDisplay[record.status]}
						<input type="hidden" name="_woStatus" id="_woStatus${st.index}" value="${record.status}"></td>
						<td><fmt:formatDate value="${record.wo_date}" pattern="dd-MM-yyyy"/></td>
						<td> <c:if test="${record.status == 'O' || record.status =='A' || record.status =='R'}">
						<input type="checkbox" name="_close" id="_close${st.index}" onclick="changeClose(${st.index});" />
						</c:if>
						<input type="hidden" name="_hidclose" id="_hidclose${st.index}" value="N">
						</td>
						<td><c:if test="${record.status == 'O' || record.status =='A' || record.status =='R'}">
						<input type="checkbox" name="_cancel" id="_cancel${st.index}" onclick="changeCancel(${st.index});" />
						</c:if>
						<input type="hidden" name="_hidcancel" id="_hidcancel${st.index}" value="N">
						</td>
						
					</tr> 
				</c:forEach> 
			</table>
		</div>
		
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
			<div class="flagText"><insta:ltext key="resourcemanagement.workorder.list.openwos"/></div>
			<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
			<div class="flagText"><insta:ltext key="resourcemanagement.workorder.list.approvedwos"/></div>
			<div class="flag"><img src='${cpath}/images/dark_blue_flag.gif'></div>
			<div class="flagText"><insta:ltext key="resourcemanagement.workorder.list.rejectedwos"/></div>
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText"><insta:ltext key="resourcemanagement.workorder.list.closedwos"/></div>
			<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
			<div class="flagText"><insta:ltext key="resourcemanagement.workorder.list.forceclosedwos"/></div>
			<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
			<div class="flagText"><insta:ltext key="resourcemanagement.workorder.list.cancelledwos"/></div>
		</div>
		
		<div class="screenActions">
            <button type="button" name="" accesskey="S" class="button" onclick="return validateForm();"><b><u><insta:ltext key="resourcemanagement.workorder.list.s"/></u></b><insta:ltext key="resourcemanagement.workorder.list.ave"/></button>
			<c:url var="url" value="">
					<c:param name="_method" value="getWOScreen"/>
			</c:url>
			| <a href="<c:out value='${url}'/>"><insta:ltext key="resourcemanagement.workorder.list.raisenewworkorder"/></a>
		</div>
		<div class="screenActions">
			<b><insta:ltext key="resourcemanagement.workorder.list.note" />:</b>
			<insta:ltext key="resourcemanagement.workorder.list.checkcancelorclose" />
		</div>

</form>
</body>
</html>
