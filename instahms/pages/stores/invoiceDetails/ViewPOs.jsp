<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
	<title><insta:ltext key="storeprocurement.polist.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.procurement"/>
	<script>
	    var toolbarOptions = getToolbarBundle("js.stores.procurement.toolbar");
		var suppArray = '${fn:join(paramValues.supplier_id, ",")}'.split(",");
		var deptId = '${ifn:cleanJavaScript(dept_id)}';
		var gRoleId = '${roleId}';
		var validateReq = '${ genPrefs.poToBeValidated == 'Y'}';
		var paramStoreId = '${fn:join(paramValues.store_id, ",")}'.split(",");
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="stores/view_po.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="POList" value="${pagedList.dtoList}"/>
<c:set var="max_centers_inc_default" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<c:set var="hasResults" value="${not empty POList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="O" value="Open"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>
<c:set target="${statusDisplay}" property="FC" value="Force Closed"/>
<c:set target="${statusDisplay}" property="V" value="Validated"/>
<c:set target="${statusDisplay}" property="A" value="Approved"/>
<c:set target="${statusDisplay}" property="AO" value="Amended Open"/>
<c:set target="${statusDisplay}" property="AV" value="Amended Validated"/>
<c:set target="${statusDisplay}" property="AA" value="Amended Approved"/>
<c:set var="validatePO" value="${(urlRightsMap.stores_validate_po == 'A' || roleId == '1' || roleId == '2')}" />
<c:set var="amendPO" value="${(urlRightsMap.stores_amend_po == 'A' || roleId == '1' || roleId == '2')}" />
<c:set var="validatePOReq" value="${validatePO && genPrefs.poToBeValidated == 'Y'}"/>
<body onload="init();showFilterActive(document.POSearchForm);">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="pono">
<insta:ltext key="storeprocurement.polist.list.pono"/>
</c:set>
<c:set var="podate">
<insta:ltext key="storeprocurement.polist.list.podate"/>
</c:set>
<c:set var="supplier">
<insta:ltext key="storeprocurement.polist.list.supplier"/>
</c:set>
<c:set var="quotationno">
<insta:ltext key="storeprocurement.polist.list.quotationno"/>
</c:set>
<c:set var="status">
<insta:ltext key="storeprocurement.polist.list.open"/>,
<insta:ltext key="storeprocurement.polist.list.validated"/>,
<insta:ltext key="storeprocurement.polist.list.approved"/>,
<insta:ltext key="storeprocurement.polist.list.amended.open"/>,
<insta:ltext key="storeprocurement.polist.list.amended.validated"/>,
<insta:ltext key="storeprocurement.polist.list.amended.approved"/>,
<insta:ltext key="storeprocurement.polist.list.closed"/>,
<insta:ltext key="storeprocurement.polist.list.forceclosed"/>,
<insta:ltext key="storeprocurement.polist.list.cancelled"/>
</c:set>
<c:set var="secondstatus">
<insta:ltext key="storeprocurement.polist.list.open"/>,
<insta:ltext key="storeprocurement.polist.list.approved"/>,
<insta:ltext key="storeprocurement.polist.list.amended.open"/>,
<insta:ltext key="storeprocurement.polist.list.amended.approved"/>,
<insta:ltext key="storeprocurement.polist.list.closed"/>,
<insta:ltext key="storeprocurement.polist.list.forceclosed"/>,
<insta:ltext key="storeprocurement.polist.list.cancelled"/>
</c:set>
<h1><insta:ltext key="storeprocurement.polist.list.polist"/></h1>

<insta:feedback-panel/>
<div>
	<c:choose>
		<c:when test="${param._msg !='' && param._flag=='true'}">
			<div id="hidmsg"><span class="resultMessage"><a href="${pageContext.request.contextPath}/pages/stores/poprint.do?_method=generatePOprint&poNo=${ifn:cleanURL(param._msg)}" target="_blank"/>${ifn:cleanHtml(param._msg)}</a> <insta:ltext key="storeprocurement.polist.list.generatedsuccessfully"/></span></div>
		</c:when>
	</c:choose>
</div> 
<div id="storecheck" style="display: block;" >
<form name="POSearchForm" method="GET">
	 <input type="hidden" name="_method" value="getPOs">
	<input type="hidden" name="_searchMethod" value="getPOs"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<insta:search form="POSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storeprocurement.polist.list.pono"/></div>
				<div class="sboFieldInput">
					<input type="text" name="po_no" value="${ifn:cleanHtmlAttribute(param.po_no)}" onkeypress="return onKeyPressPOno(event);" onchange="return onChangePOno();">
				</div>
	    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.polist.list.supplier"/></div>
						<div class="sfField">
							<select name="supplier_id" id="supplier_id" multiple="true" class="listbox">
								<c:forEach items="${suppliers}" var="supp">
						            <option value=${supp.SUPPLIER_CODE }>${supp.SUPPLIER_NAME}</option>
						        </c:forEach>
						    </select>
						</div>
					</td>
					<td> 
						<div class="sfLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.allotedto"/>:</div>
				        <div class="sfField" >
				        	<insta:selectoptions name="po_alloted_to" value="${param.po_alloted_to}" opvalues="${store_users}" 
				        	optexts="${store_users}" dummyvalue="${dummyvalue}"/>
				        </div>
						<input type="hidden" name="po_alloted_to@op" value="like"/>
					
						<div class="sfLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.Raisedby"/>:</div> 
				        <div class="sfField" >
   				        	<insta:selectoptions name="user_id" value="${param.user_id}" 
   				        	opvalues="${store_usersWithAutoPO}" optexts="${store_usersWithAutoPO}" dummyvalue="${dummyvalue}"/>
						</div>
						<input type="hidden" name="user_id@op" value="like"/>
					</td> 
					<td>
					<c:choose>
						<c:when test="${max_centers_inc_default > 1 && centerId == 0}">
								<div class="sfLabel"><insta:ltext key="registration.patient.label.center"/>:</div>
								<div class="sfField">
									<select name="_center_id" id="_center_id" class="dropdown" onchange="filterStores(this);">
										<option value="">-- All --</option>
										<c:forEach var="center" items="${centers}">
											<option value="${center.map.center_id}"
												${param._center_id == center.map.center_id ? 'selected' : ''}>
												${center.map.center_name}
											</option>
										</c:forEach>
									</select>
								</div>
						</c:when>
						<c:otherwise>
							<input type="hidden" name="_center_id" value="${centerId}"/>
						</c:otherwise>
					</c:choose>
						<div class="sfLabel"><insta:ltext key="storeprocurement.polist.list.store"/></div>
						<div class="sfField">
							<c:choose>
							<c:when test="${roleId ==1 || roleId==2 || (multiStoreAccess == 'A')}">
								<insta:userstores username="${userid}" elename="store_id" multipleSelect="5"
									 id="store_id" onlySuperStores="Y" defaultVal="${pharmacyStoreId}" val="${param.store_id}" />
							</c:when>
							<c:otherwise>
									<insta:getStoreName store_id="${pharmacyStoreId}"/>
									<input type = "hidden" name="store_id" id="store_id" value="${pharmacyStoreId}" />
							</c:otherwise>
						</c:choose>
							<input type="hidden" name="store_id@type" value="integer"/>
						</div>

						<div class="sfLabel"><insta:ltext key="storeprocurement.polist.list.quotationno"/></div>
						<div class="sfField">
							<input type="text" name="qut_no" value="${ifn:cleanHtmlAttribute(param.qut_no)}"/>
							<input type="hidden" name="qut_no@op" value="like"/>
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.polist.list.fromdate"/></div>
						<div class="sfField">
							<insta:datewidget name="po_date" id="po_date0" value="${paramValues.po_date[0]}"/>
					    </div>

					    <div class="sfLabel"><insta:ltext key="storeprocurement.polist.list.todate"/></div>
						<div class="sfField">
							<insta:datewidget name="po_date" id="po_date1" value="${paramValues.po_date[1]}"/>
							<input type="hidden" name="po_date@op" value="ge,le"/>
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.polist.list.status"/></div>
						<div class="sfField">
							<c:choose>
								<c:when test="${validatePOReq}">
									<insta:checkgroup name="status" selValues="${paramValues.status}"
									opvalues="O,V,A,AO,AV,AA,C,FC,X" optexts="${status}"/>
								</c:when>
								<c:otherwise>
									<insta:checkgroup name="status" selValues="${paramValues.status}"
									opvalues="O,A,AO,AA,C,FC,X" optexts="${secondstatus}"/>
								</c:otherwise>
							</c:choose>
						</div>
					</td>
				</tr>
		</table>
	  </div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	 <div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				
			    <insta:sortablecolumn name="po_no" title="${pono}"/>
				<insta:sortablecolumn name="po_date" title="${podate}"/>
				<insta:sortablecolumn name="supplier_name" title="${supplier}"/>
				<insta:sortablecolumn name="qut_no" title="${quotationno}"/>
				<th><insta:ltext key="storeprocurement.polist.list.alloted"/></th> 
				<th><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.Raisedby"/></th> 
			    <th><insta:ltext key="storeprocurement.polist.list.value"/></th>
			    <th><insta:ltext key="storeprocurement.polist.list.grns"/></th>
				<th><insta:ltext key="storeprocurement.polist.list.status"/></th>
				<th><insta:ltext key="storeprocurement.polist.list.close"/></th>
				<th><insta:ltext key="storeprocurement.polist.list.cancel"/></th>
			        
			</tr>

			<c:forEach var="po" items="${POList}" varStatus="st">
			<c:set var="stockEntryEnabled" value="${po.status == 'A' || po.status == 'AA'}"/>
			<c:set var="validateEnabled" value="${validatePOReq && (po.status eq 'O' || po.status eq 'AO')}"/>
			<c:set var="amendEnabled" value="${amendPO && ( po.status == 'A'|| po.status == 'AA' ||po.status == 'AO' || po.status == 'AV' ) }"/> 
			<c:set var="EditEnabled" value="true"/>
			<c:set var="CopyEnabled" value="true" />
			<c:set var="flagColor">
					<c:choose>
						<c:when test="${po.status == 'O'}"><insta:ltext key="storeprocurement.polist.list.yellow"/></c:when>
						<c:when test="${po.status == 'C'}"><insta:ltext key="storeprocurement.polist.list.grey"/></c:when>
						<c:when test="${po.status == 'V'}"><insta:ltext key="storeprocurement.polist.list.dark_blue"/></c:when>
						<c:when test="${po.status == 'FC'}"><insta:ltext key="storeprocurement.polist.list.blue"/></c:when>
						<c:when test="${po.status == 'A'}"><insta:ltext key="storeprocurement.polist.list.green"/></c:when>
						<c:when test="${po.status == 'AO'}"><insta:ltext key="storeprocurement.polist.list.yellow"/></c:when>
						<c:when test="${po.status == 'AV'}"><insta:ltext key="storeprocurement.polist.list.dark_blue"/></c:when>
						<c:when test="${po.status == 'AA'}"><insta:ltext key="storeprocurement.polist.list.green"/></c:when>
						<c:otherwise><insta:ltext key="storeprocurement.polist.list.red"/></c:otherwise>
					</c:choose>
			</c:set>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{poNo: '${po.po_no}',poStoreId:${po.store_id},hospitaltin: '${hosp_tin}',hospitalpan: '${hosp_pan}',hospitalSerRegno: '${hosp_ser_reg_no}',grn_count:'${po.grn_count}', printType:document.POSearchForm._printerType.value,temp_name:document.POSearchForm._template_name.value},
						[${po.po_total != null },${validateEnabled},${EditEnabled && entryRights[st.index] eq 'Y'},${stockEntryEnabled && entryRights[st.index] eq 'Y'},${CopyEnabled},${amendEnabled && po.grn_count <= 0  }]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					
	                <td>${po.po_no }</td>
				    <td><fmt:formatDate value="${po.po_date }" pattern="dd-MM-yyyy"/></td>
					<td>${po.supplier_name }</td>
					<td>${po.qut_no }</td>

					<td>${po.po_alloted_to}</td>	
					<td>${po.user_id}</td>		
					<td><fmt:formatNumber value="${po.po_total }" minFractionDigits="${prefDecimalDigits}" maxFractionDigits="${prefDecimalDigits}" /></td>
					<td>${po.grn_count }<input type="hidden" name="_postatus" id="_postatus${st.index}" value="${po.status }">
						<input type="hidden" name="_count" id="_count${st.index}" value="${po.grn_count }">
						<input type="hidden" name="_pono" id="_pono${st.index}" value="${po.po_no }"></td>
					<td>
						<img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${statusDisplay[po.status]}
					</td>
					<c:choose>
						   <c:when test="${po.status =='O' || po.status =='AO'}">
						   		<td><input type="checkbox" name="_close" id="_close${st.index}"  onclick="changeClose(${st.index});"/><input type="hidden" name="_hidclose" id="_hidclose${st.index}" value="N">
						  		</td>
						   </c:when>
						    <c:otherwise>
							   <td><input type="hidden" name="_hidclose" id="_hidclose${st.index}" value="N"></td>
						   </c:otherwise>
					</c:choose>
					<c:choose>
						   <c:when test="${(po.status =='C' || po.status =='FC' || po.status =='X') || (po.grn_count gt 0)}">
						   		<td><input type="hidden" name="_hidcancel" id="_hidcancel${st.index}" value="N"></td>
						   </c:when>
						   <c:otherwise>
						   		<td><input type="checkbox" name="_cancel" id="_cancel${st.index}" value="N" onclick="changeCancel(${st.index});"/><input type="hidden" name="_hidcancel" id="_hidcancel${st.index}" value="N"></td>
						   </c:otherwise>
					</c:choose> 
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getPOs'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div> 
    
    <div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.polist.list.openpos"/></div>
		<div class="flag"><img src='${cpath}/images/dark_blue_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.polist.list.validatedpos"/></div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.polist.list.approvedpos"/></div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.polist.list.closedpos"/></div>
		<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.polist.list.forceclosedpos"/></div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.polist.list.cancelledpos"/></div>

	</div>
	<div class="screenActions">
            <button type="button" name="" accesskey="S" class="button" onclick="onSave();"><b><u><insta:ltext key="storeprocurement.polist.list.s"/></u></b><insta:ltext key="storeprocurement.polist.list.ave"/></button>
    </div>
    <div class="screenActions" style="float:right">
	<insta:selectdb name="_printerType" table="printer_definition"
		valuecol="printer_id" displaycol="printer_definition_name" orderby="printer_definition_name" 
		value="${bean.map.printer_id}"/>
    <select name="_template_name" id="template_name" class="dropdown">
		<option value="BUILTIN_HTML" ${default_po_print_template eq 'BUILTIN_HTML'? 'selected': ''}><insta:ltext key="storeprocurement.polist.list.built_indefaulttemplate"/></option>
		<option value="BUILTIN_TEXT" ${default_po_print_template eq 'BUILTIN_TEXT'? 'selected': ''}><insta:ltext key="storeprocurement.polist.list.built_intexttemplate"/></option>
		<c:forEach var="t" items="${templates}">
			<option value="${t.map.template_name}" ${default_po_print_template eq t.map.template_name? 'selected': ''}>${t.map.template_name}</option>
		</c:forEach>
		</select>
	</div>
    <div class="screenActions">
    	<b><insta:ltext key="storeprocurement.polist.list.note"/>:</b> <insta:ltext key="storeprocurement.polist.list.checkcancelorclose"/>
    </div> 
</form>
</div>

<script type="text/javascript">

	var userstoresList = [];
	<c:if test="${not empty usersStoresList}">
		userstoresList = JSON.parse('${ifn:cleanJavaScript(usersStoresList)}');
	</c:if>
	var max_centers_inc_default = ${max_centers_inc_default};
</script>
</body>
</html>
