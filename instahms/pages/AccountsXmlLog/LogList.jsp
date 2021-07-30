<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="billing.acountingxmlimport.exportlog.list.accountingxmlloglist.instahms"/></title>
	<insta:link type="js" file="dashboardsearch.js"/>
	<script>
		var toolbar = {
			DetailedMsg : {
				title : "Show Details",
				imageSrc : "icons/Edit.png",
				href : "pages/AccountsXmlLog/Log.do?_method=show",
			}
		}
		function init(){
 			createToolbar(toolbar);
		}

		function regenerateXml(){
			var exportNo = document.getElementsByName("_exportNo");
			for (var i=0; i<exportNo.length; i++) {
				if (exportNo[i].checked) {
					document.searchForm._method.value = 'regenerate';
					document.searchForm.submit();
					return true;
				}
			}
			alert("Please select one or more files which are required to regenerate.");
			return false;
		}
		function deleteSelected() {
			var exportNo = document.getElementsByName("_exportNo");
			for (var i=0; i<exportNo.length; i++) {
				if (exportNo[i].checked) {
					document.searchForm._method.value = 'delete';
					document.searchForm.submit();
					return true;
				}
			}
			alert("Please select one or more files to delete.");
			return false;
		}
		function updateStatus(){
			document.searchForm._method.value = 'update';
			document.searchForm.submit();
			return true;
		}
	</script>
</head>
<body onload="init();showFilterActive(document.searchForm);">
<c:set var="all">
<insta:ltext key="billing.acountingxmlimport.exportlog.list.all"/>
</c:set>
<c:set var="regenerate">
<insta:ltext key="billing.acountingxmlimport.exportlog.list.regenerate"/>
</c:set>
<c:set var="updatestatus">
<insta:ltext key="billing.acountingxmlimport.exportlog.list.updatestatus"/>
</c:set>
<c:set var="delete">
<insta:ltext key="billing.acountingxmlimport.exportlog.list.delete"/>
</c:set>
<c:set var="accountingstatus">
<insta:ltext key="billing.acountingxmlimport.exportlog.list.pending"/>,
<insta:ltext key="billing.acountingxmlimport.exportlog.list.success"/>,
<insta:ltext key="billing.acountingxmlimport.exportlog.list.error"/>,
<insta:ltext key="billing.acountingxmlimport.exportlog.list.empty"/>
</c:set>
<c:set var="filenameheader">
<insta:ltext key="billing.acountingxmlimport.exportlog.list.filename"/>
</c:set>
<c:set var="exporteddataheader">
<insta:ltext key="billing.acountingxmlimport.exportlog.list.exporteddate"/>
</c:set>
	<c:set var="dtoList" value="${pagedList.dtoList}"/>
	<c:set var="results" value="${not empty pagedList.dtoList}"/>
	<h1><insta:ltext key="billing.acountingxmlimport.exportlog.list.accountingxmlimport.exportlog"/></h1>
	<insta:feedback-panel/>
	<form name="searchForm" action="Log.do">
		<input type="hidden" name="_method" value="list">
		<input type="hidden" name="sortOrder" value="exported_date_time"/>
		<input type="hidden" name="sortReverse" value="true"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<insta:search form="searchForm" optionsId="optionalFilter" closed="${results}" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="billing.acountingxmlimport.exportlog.list.filename"/>: </div>
					<div class="sboFieldInput">
						<input type="text" name="file_name" value="${ifn:cleanHtmlAttribute(param.file_name)}"/>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="billing.acountingxmlimport.exportlog.list.schedulename"/>: </div>
					<div class="sboFieldInput">
						<insta:selectdb name="log.schedule_id" table="scheduled_export_prefs" id="log.schedule_id"
							displaycol="schedule_name" valuecol="schedule_id" dummyvalue="${all}" dummyvalueid="" filtered="false" value="${param['log.schedule_id']}"/>
						<input type="hidden" name="log.schedule_id@cast" value="y"/>
					</div>
				</div>
			</div>
			<div id="optionalFilter" style="clear: both; display: ${results ? 'none' : 'block'}" >
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel"><insta:ltext key="billing.acountingxmlimport.exportlog.list.exportdate"/>:</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="billing.acountingxmlimport.exportlog.list.from"/> :</div>
								<insta:datewidget name="exported_date_time" id="exported_date_time0" value="${paramValues.exported_date_time[0]}"/>
							</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="billing.acountingxmlimport.exportlog.list.to"/> :</div>
								<insta:datewidget name="exported_date_time" id="exported_date_time1" value="${paramValues.exported_date_time[1]}"/>
								<input type="hidden" name="exported_date_time@cast" value="y"/>
								<input type="hidden" name="exported_date_time@type" value="date"/>
								<input type="hidden" name="exported_date_time@op" value="ge,le"/>
							</div>
						</td>
						<td class="last">
							<div class="sfLabel"><insta:ltext key="billing.acountingxmlimport.exportlog.list.status"/>: </div>
							<div class="sfField">
								<insta:checkgroup name="status" selValues="${paramValues.status}"
										opvalues="Pending,Success,Error,Empty" optexts="${accountingstatus}"/>
							</div>
						</td>
						<td colspan="3" class="last">&nbsp;</td>
					</tr>
			  	</table>
			  </div>
		</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList" >
		<table width="100%" class="resultList" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
			<tr>
				<th style="padding-top: 0px;padding-bottom: 0px">
					<input type="checkbox" name="_checkAll" onclick="return checkOrUncheckAll('_exportNo', this)"/>
				</th>
				<insta:sortablecolumn name="file_name" title="${filenameheader}"/>
				<th><insta:ltext key="billing.acountingxmlimport.exportlog.list.schedulename"/></th>
				<insta:sortablecolumn name="exported_date_time" title="${exporteddateheader}"/>
				<th><insta:ltext key="billing.acountingxmlimport.exportlog.list.status"/></th>
				<th><insta:ltext key="billing.acountingxmlimport.exportlog.list.response"/></th>
				<th><insta:ltext key="billing.acountingxmlimport.exportlog.list.totalvouchers"/></th>
			</tr>
			<c:forEach items="${dtoList}" var="bean" varStatus="st">
				<c:set var="show" value="${bean.map.message != ''}"/>
				<tr onclick="showToolbar(${st.index}, event, 'resultTable',
						{export_no: '${bean.map.export_no}'}, [${show}]);"
					id="toolbarRow${st.index}">
					<td><input type="checkbox" name="_exportNo" id="_exportNo" value="${bean.map.export_no}"></td>
					<td>${bean.map.file_name}</td>
					<td>${bean.map.schedule_name}</td>
					<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${bean.map.exported_date_time}"/></td>
					<td>${bean.map.status}</td>
					<td><insta:truncLabel value="${bean.map.message}" length="50"/></td>
					<td>${bean.map.total_vouchers}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${results}"/>
	</div>
	<div class="screenActions">
		<input type="button" name="regenerate" value="${regenerate}" onclick="return regenerateXml();" style="display: ${results} ? 'block' : 'none'"/>
		<input type="button" name="update" value="${updatestatus}" onclick="return updateStatus();"/>
		<input type="button" name="update" value="${delete}" onclick="return deleteSelected();"/>
	</div>
	</form>

</body>
</html>