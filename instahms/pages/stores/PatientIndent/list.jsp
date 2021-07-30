<html>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="patientIndentList" value="${pagedList.dtoList}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty patientIndentList}"/>

<c:set var="genPrefs" value="<%= GenericPreferencesDAO.getGenericPreferences() %>" />
<c:set var="blockIpVisit" value="${genPrefs.restrictInactiveIpVisit}" />

<c:set var="storesList" value="${ifn:listAll('stores','dept_name')}"/>
<c:set var="storeExists" value="false"/>
<c:forEach items="${storesList}" var="store">${store.map.auto_fill_indents }
	<c:if test="${ store.map.status == 'A' && store.map.center_id == centerId && store.map.auto_fill_indents}">
		<c:set var="storeExists" value="true"/>
	</c:if>
</c:forEach>
<c:set var="template1">
<insta:ltext key="salesissues.raisepatientindent.addshow.template1"/>
</c:set>
<c:if test="${!storeExists}">
	<c:set var="error" scope="request" value="${template1}"/>
</c:if>
<c:set var="userStoreExists" value="true"/>
<c:if test="${roleId != 1 && roleId != 2}">
	<c:set var="userStoreExists" value="${pharmacyStoreId != ''}"></c:set>
</c:if>
<head>

	<insta:link type="script" file="dashboardsearch.js"/>

	<title><insta:ltext key="salesissues.patientindents.list.patientindentslist"/> </title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="sales.issues.patientindent"/>
	<insta:js-bundle prefix="sales.issues"/>
	<script type="text/javascript">
		var toolbarOptions = getToolbarBundle("js.sales.issues.patientindent.toolbar");
		var cpath = '${cpath}';
		var issueRights = '${urlRightsMap.patient_inventory_issue}';
		var issueReturnRights = '${urlRightsMap.patient_inventory_return}';
		var salesRights = '${urlRightsMap.pharma_sales}';
		var salesReturnRights = '${urlRightsMap.pharma_returns}';

		var toolbar = {}
		toolbar.View= {
			title: toolbarOptions["view"]["name"],
			imageSrc: "icons/Edit.png",
			href: 'stores/PatientIndentView.do?_method=view',
			onclick: 'setHref',
			description: toolbarOptions["view"]["description"]
		};

		toolbar.Edit= {
			title: toolbarOptions["edit"]["name"],
			imageSrc: "icons/Edit.png",
			href: 'stores/PatientIndentEdit.do?_method=show',
			onclick: 'setHref',
			description: toolbarOptions["edit"]["description"]
		};

		toolbar.Sale= {
			title: toolbarOptions["sales"]["name"],
			imageSrc: "icons/Add.png",
			href: 'pages/stores/MedicineSales.do?method=getSalesScreen',
			description: toolbarOptions["sales"]["description"],
			show: (salesRights == 'A')
		};

		toolbar.Issue= {
			title: toolbarOptions["issue"]["name"],
			imageSrc: "icons/Collect.png",
			// href: 'stores/StockPatientIssue.do?_method=getPatientIssueScreen',
			href: 'patientissues/add.htm?',
			onclick: null,
			description: toolbarOptions["issue"]["description"],
			show: (issueRights == 'A')
		};


		toolbar.SaleReturns= {
			title: toolbarOptions["salesreturns"]["name"],
			imageSrc: "icons/Add.png",
			href: 'pages/stores/MedicineSalesReturn.do?method=getSalesScreen',
			description: toolbarOptions["salesreturns"]["description"],
			show: (salesReturnRights == 'A')
		};

		toolbar.IssueReturn={
			title: toolbarOptions["issuereturns"]["name"],
			imageSrc: "icons/Collect.png",
			href: 'stores/StockPatientReturn.do?_method=show&type=patient',
			onclick: null,
			description: toolbarOptions["issuereturns"]["description"],
			show: (issueReturnRights == 'A')
		};

		toolbar.Print={
			title: toolbarOptions["print"]["name"],
			imageSrc: "icons/Report.png",
			href: 'stores/PatientIndentAdd.do?_method=getIndentPrint',
			target: '_blank',
			onclick: null,
			description: toolbarOptions["print"]["description"],
	};

	function setHref( anchor, params ){
	for (var paramname in params) {
		var paramvalue = params[paramname];
			if (paramname == 'indent_type' && paramvalue == 'R'){
				var href = anchor.href;
				href = href.replace('.do','Return.do');
				anchor.href = href;
				break;
			}
		}
		return true;
	}

	function init(){
		createToolbar(toolbar);
		initMrNoAutoComplete(cpath);
	}

	function selectAllIndents(){
		var closeElmts = document.getElementsByName("exclude_in_qb_close");
		if (document.getElementById("closeAll").checked)	{
			for(var i=0;i<closeElmts.length;i++){
				if ( !closeElmts[i].disabled ){
					closeElmts[i].checked=true;
					setClosed(closeElmts[i]);
				}
			}
		} else {
			for(var i=0;i<closeElmts.length;i++){
				if ( !closeElmts[i].disabled ){
					closeElmts[i].checked=false;
					setClosed(closeElmts[i]);
				}
			}
		}
	}

	function setClosed(obj) {
		var row = getThisRow(obj);
		getElementByName(row,"exclude_in_qb_dispense_status").value = obj.checked ? 'C' : getElementByName(row,"exclude_in_qb_dispense_status").value;
		getElementByName(row,"exclude_in_qb_indent_status").value = obj.checked && getElementByName(row,"exclude_in_qb_indent_status").value == 'O'? 'F' : getElementByName(row,"exclude_in_qb_indent_status").value;
	}

	function setIndexedValue(name, index, value) {
		var obj = getIndexedFormElement(document.indentsListForm, name, index);
		if (obj)
			obj.value = value;
		return obj;
	}

	function submit(){
		var valid = false;
		var closeChkBx = document.getElementsByName("exclude_in_qb_close");
		for(var i = 0;i<closeChkBx.length;i++){
			if ( closeChkBx[i].checked ){
				valid = true;
				break;
			}
		}
		if ( valid ) {
			document.indentsListForm._method.value = 'closeIndents';
			document.indentsListForm.submit();
		} else {
			showMessage("js.sales.issues.moreindents.close");
			return false;
		}
	}

	</script>
</head>
<body onload="init();" class="yui-skin-sam">
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="openText">
<insta:ltext key="salesissues.patientindents.list.open"/>
</c:set>
<c:set var="partialText">
<insta:ltext key="salesissues.patientindents.list.partial"/>
</c:set>
<c:set var="closedText">
<insta:ltext key="salesissues.patientindents.list.closed"/>
</c:set>
<c:set var="cancelledText">
<insta:ltext key="salesissues.patientindents.list.cancelled"/>
</c:set>
<c:set var="finalizedText">
<insta:ltext key="salesissues.patientindents.list.finalized"/>
</c:set>
<c:set var="expecteddate">
<insta:ltext key="salesissues.patientindents.list.expecteddate"/>
</c:set>
<c:set var="indentno">
<insta:ltext key="salesissues.patientindents.list.indentno"/>
</c:set>
<c:set var="wardno">
<insta:ltext key="salesissues.patientindents.list.ward"/>
</c:set>
<c:set var="indenttype">
<insta:ltext key="salesissues.patientindents.list.indent"/>,
<insta:ltext key="salesissues.patientindents.list.indentreturn"/>
</c:set>
<c:set var="status">
<insta:ltext key="salesissues.patientindents.list.open"/>,
<insta:ltext key="salesissues.patientindents.list.finalized"/>
</c:set>
<c:set var="dispensestatus">
<insta:ltext key="salesissues.patientindents.list.open"/>,
<insta:ltext key="salesissues.patientindents.list.partial"/>,
<insta:ltext key="salesissues.patientindents.list.closed"/>
</c:set>
<c:set var="dispensetype">
<insta:ltext key="salesissues.patientindents.list.sales"/>,
<insta:ltext key="salesissues.patientindents.list.issues"/>
</c:set>
<c:set var="priority">
<insta:ltext key="salesissues.raisepatientindent.addshow.normal"/>,
<insta:ltext key="salesissues.raisepatientindent.addshow.urgent"/>
</c:set>
<c:set var="newpatientIndent">
<insta:ltext key="salesissues.patientindents.list.newpatientindent"/>
</c:set>
<c:set var="newpatientreturnIndent">
<insta:ltext key="salesissues.patientindents.list.newpatient.returnindent"/>
</c:set>
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>

<c:set var="notdischarged">
	<insta:ltext key="patient.discharge.status.common.notdischarged"/>
</c:set>
<c:set var="dischargeInitiated">
	<insta:ltext key="patient.discharge.status.common.dischargeinitiated"/>
</c:set>
<c:set var="clinicalDischarge" >
	<insta:ltext key="patient.discharge.status.common.clinicaldischarge"/>
</c:set>
<c:set var="financialDischarge">
	<insta:ltext key="patient.discharge.status.common.financialdischarge"/>
</c:set>
<c:set var="physicalDischarge">
	<insta:ltext key="patient.discharge.status.common.physicaldischarge"/>
</c:set>

<jsp:useBean id="dischargeStatusMap" class="java.util.HashMap"/>
<c:set target="${dischargeStatusMap}" property="N" value="${notdischarged}"/>
<c:set target="${dischargeStatusMap}" property="I" value="${dischargeInitiated}"/>
<c:set target="${dischargeStatusMap}" property="C" value="${clinicalDischarge}"/>
<c:set target="${dischargeStatusMap}" property="F" value="${financialDischarge}"/>
<c:set target="${dischargeStatusMap}" property="D" value="${physicalDischarge}"/>

	<h1><insta:ltext key="salesissues.patientindents.list.patientindents"/></h1>

	<insta:feedback-panel/>
	<form name="indentsListForm" method="GET">
		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<c:set var="wards" value="${ifn:listAll('ward_names','ward_name')}"/>
		<insta:search form="indentsListForm" optionsId="optionalFilter" closed="${hasResults}" >


	  <div class="searchBasicOpts" >

	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="salesissues.patientindents.list.indentstore"/></div>
				<div class="sboFieldInput">
					<select name="indent_store" class="dropdown">
						<option value="">${dummyvalue}</option>
						<c:forEach items="${storesList}" var="store">${store.map.auto_fill_indents }
							<c:if test="${ store.map.status == 'A' && store.map.center_id == centerId && store.map.auto_fill_indents}">
								<option value="${store.map.dept_id }"
								${store.map.dept_id == ( param.indent_store == null ? pharmacyStoreId : param.indent_store ) ? 'selected' : ''}
								>${store.map.dept_name }</option>
							</c:if>
						</c:forEach>
					</select>
					<input type="hidden" name="indent_store@cast" value="y"/>
				</div>
	  		</div>
	  		<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="salesissues.patientindents.list.patientward"/></div>
					<div class="sboFieldInput">
						<select name="ward_no" id="ward_no" class="dropdown" >
							<option value=""><insta:ltext key="salesissues.patientindents.list.ward.select"/></option>
							<c:forEach items="${wards }" var="ward">
								<c:if test="${ward.map.center_id == centerId && ward.map.status == 'A'}">
									<option value="${ward.map.ward_no }"
										${param.ward_no == ward.map.ward_no ? 'selected' : ''}
									>${ward.map.ward_name }</option>
								</c:if>
							</c:forEach>
						</select>
						<input type="hidden" name="ward_no@cast" value="y"/>
					</div>
		    	</div>
		  	</div>
	  	 <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.patientindents.list.expecteddate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="salesissues.patientindents.list.from"/>:</div>
							<insta:datewidget name="expected_date" id="expected_date0" value="${paramValues.expected_date[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="salesissues.patientindents.list.to"/>:</div>
							<insta:datewidget name="expected_date" id="expected_date1" value="${paramValues.expected_date[1]}"/>
							<input type="hidden" name="expected_date@op" value="ge,le"/>
							<input type="hidden" name="expected_date@cast" value="y"/>
						</div>
						<div class="sfLabel"><insta:ltext key="salesissues.patientindents.list.dispensetype"/></div>
						<div class="sfField">
						<insta:checkgroup name="process_type" selValues="${paramValues.process_type}"
							opvalues="S,I" optexts="${dispensetype}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.patientindents.list.indenttype"/></div>
						<div class="sfField">
						<insta:checkgroup name="indent_type" selValues="${paramValues.indent_type}"
							opvalues="I,R" optexts="${indenttype}"/>
						</div>
						<div class="sfLabel"><insta:ltext key="patient.discharge.status.common.dischargestatus" />:</div>
						<div class="sfField">
							<insta:checkgroup name="patient_discharge_status" selValues="${paramValues.patient_discharge_status}"
								opvalues="N,I,C,F,D" optexts="${notdischarged},${dischargeInitiated},${clinicalDischarge},${financialDischarge},${physicalDischarge}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.patientindents.list.status"/></div>
						<div class="sfField">
						<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="O,F" optexts="${status}"/>
						</div>
						
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.patientindents.list.dispensestatus"/></div>
						<div class="sfField">
						<insta:checkgroup name="dispense_status" selValues="${paramValues.dispense_status}"
							opvalues="O,P,C" optexts="${dispensestatus}"/>
						</div>
						
					</td>
					<td class="last">
						<div class="sboFieldLabel"><insta:ltext key="salesissues.patientindents.list.mrno.or.patientname"/>:</div>
							<div class="sboFieldInput">
								<div id="mrnoAutoComplete">
									<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
									<div id="mrnoContainer" style="width: 300px"></div>
								</div>
							</div>
						<div class="sboFieldLabel"><insta:ltext key="salesissues.patientindents.list.patientindentno"/>:</div>
							<div class="sboFieldInput">
								<div>
									<input type="text" name="patient_indent_no" id="patient_indent_no" value="${ifn:cleanHtmlAttribute(param.patient_indent_no)}" />
								</div>
							</div>
						<div class="formlabel"><insta:ltext key="salesissues.raisepatientindent.addshow.priority"/>:</div>
						<div>
							<insta:selectoptions name="priority" value="${param.priority}" opvalues="N,U" optexts="${priority}" dummyvalue="(All)"/>
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
					<th><input type="checkbox" onclick="selectAllIndents()" id="closeAll" style="padding-bottom: 11px"/></th>
					<insta:sortablecolumn name="patient_indent_no" title="${indentno}"/>
					<insta:sortablecolumn name="mr_no" title="${mrno}"/>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<insta:sortablecolumn name="ward_no" title="${wardno}"/>
					<th><insta:ltext key="salesissues.patientindents.list.bed"/></th>
					<th><insta:ltext key="salesissues.patientindents.list.status"/></th>
					<th><insta:ltext key="salesissues.patientindents.list.dispensestatus"/></th>
					<insta:sortablecolumn name="expected_date" title="${expecteddate}"/>
					<th><insta:ltext key="patient.discharge.status.common.dischargestatus"/></th>

				</tr>
				<c:forEach var="indent" items="${patientIndentList}" varStatus="st">

					<c:forEach items="${storesList}" var="store">
						<c:if test="${store.map.dept_id == indent.indent_store }">
							<c:set var="isIndetnStoreSalesStore" value="${store.map.is_sales_store == 'Y'}"/>
						</c:if>
					</c:forEach>
					<c:if test="${blockIpVisit =='I'}">
						<c:set var="allowIpInactiveVisit" value="${not (indent.visit_status == 'I' && indent.visit_type == 'i')}"/>
					</c:if>
					<c:if test="${blockIpVisit =='O'}">
					    <c:set var="allowIpInactiveVisit" value="${not (indent.visit_status == 'I' && indent.visit_type == 'o')}"/>
					</c:if>
					<c:if test="${blockIpVisit =='B'}">
						<c:set var="allowIpInactiveVisit" value="${not (blockIpVisit =='B' && indent.visit_status == 'I')}"/>
					</c:if>
					<c:if test="${blockIpVisit =='N'}">
						<c:set var="allowIpInactiveVisit" value="${true}"/>
					</c:if>
					
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{patient_indent_no:'${indent.patient_indent_no }',indent_type:'${indent.indent_type }',
							 visit_id:'${indent.visit_id }',patstatus: 'A',phStore:${indent.indent_store },
							 visitId:'${indent.visit_id }',indentStore:${indent.indent_store },
							 patient_id:'${indent.visit_id }',storeId:${indent.indent_store }, stop_doctor_orders : 'true'},
							[true,${indent.dispense_status != 'C'},${(isIndetnStoreSalesStore && indent.indent_type == 'I' && indent.dispense_status != 'C' && userStoreExists) && allowIpInactiveVisit},
							${indent.indent_type == 'I' && indent.dispense_status != 'C' && userStoreExists && allowIpInactiveVisit},
							${isIndetnStoreSalesStore && indent.indent_type == 'R' && indent.dispense_status != 'C'  && allowIpInactiveVisit && userStoreExists },
							${indent.indent_type == 'R' && indent.dispense_status != 'C' && allowIpInactiveVisit && userStoreExists },true]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
						<td>
							<input type="checkbox" name="exclude_in_qb_close" onclick="return setClosed(this);" ${indent.dispense_status == 'C' ? 'disabled' : '' } />
							<input type="hidden" name="exclude_in_qb_dispense_status" value="${indent.dispense_status}"/>
							<input type="hidden" name="exclude_in_qb_indent_status" value="${indent.status}"/>
							<input type="hidden" name="exclude_in_qb_patient_indent_no" value="${indent.patient_indent_no}"/>
						</td>

						<c:set var="flagColor" value="empty"/>
						<c:set var="flagColor" value="${indent.priority == 'U' ? 'red' : 'empty'}"/>
						<c:if test="${indent.indent_type == 'R'}">
							<c:set var="flagColor" value="blue"/>
						</c:if>

						<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${indent.patient_indent_no }</td>
						<td>${indent.mr_no }</td>
						<td>${indent.patient_full_name }</td>
						<td>${indent.ward_name }</td>
						<td>${indent.bed_name }</td>
						<td>${indent.status == 'O' ? openText :  indent.status == 'F' ? finalizedText : cancelledText}</td>
						<td>${indent.dispense_status == 'O' ? openText :  indent.dispense_status == 'P' ? partialText : closedText}</td>
						<td><fmt:formatDate value="${indent.expected_date }" pattern="dd-MM-yyyy HH:mm"/></td>
						<td>${dischargeStatusMap[indent.patient_discharge_status]}</td>
					</tr>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${hasResults}"/>
		</div>

	</form>
	<input type="submit" name="close" value="Close" onclick="submit();" ${!hasResults || !storeExists ? 'disabled' : ''}/>
	<insta:screenlink addPipe="true" screenId="stores_patient_indent_add" label="${newpatientIndent}" extraParam="?_method=addshow"/>
	<insta:screenlink addPipe="true" screenId="stores_patient_indent_add_return" label="${newpatientreturnIndent}" extraParam="?_method=addshow"/>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="salesissues.patientindents.list.urgentindent"/></div>
		<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
		<div class="flagText"><insta:ltext key="salesissues.patientindents.list.returnindent"/></div>
	</div>

</body>
</html>
