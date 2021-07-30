<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Print Configurations - Insta HMS</title>
		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="css" file="widgets.css"/>

		<style type="text/css">
			.status_InActive{background-color: #E4C89C}
		</style>
		<script>
		    var centersCount = ${centers_count};
			function showLogo(center_id){
			window.open("${cpath}/showLogo.do?cache=false&center_id=" + center_id);
			}

			function deleteLogo(center_id){
			if (!confirm("Do you want to delete the Logo"))
			return false;
			window.location.href ="${cpath}/master/PrintConfiguration.do?method=delete&center_id=" + center_id;
			}

			var toolBar = {
				PrintDetails : {
					title : "Edit Configuration",
					imageSrc : "icons/Edit.png",
					href : "master/PrintConfiguration.do?method=show",
					onclick : null,
					description : "Edit Bill Print Details"
					}
			};

			function init() {
				createToolbar(toolBar);
			}
			
			function checkAndSubmit() {
				
				var cenArr = new Array(centersCount);
				for(var i=0; i<centersCount; i++) {
			        var uploadFile = document.getElementById ("logo"+i);
			        
			        if (uploadFile == null || (uploadFile != null && uploadFile.value.length == 0)) {
                        document.getElementById("center_id"+i).value = -1;
			        }
				}
			}

		</script>
	</head>
	<body onload="init()">
		<h1>Print Configurations</h1>
		<insta:feedback-panel/>
		<form method="POST" enctype="multipart/form-data" action="PrintConfiguration.do?method=updateLogo">
		<input name="centers_count" type="hidden" value="${centers_count }"/>
		<table>
			<tr>
				<td colspan="2" align="center" class="info">${ifn:cleanHtml(msg)}</td>
			</tr>
		</table>
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<div class="resultList">
			<table class="resultList" cellpadding="0" cellspacing="0" id="resultTable" width="100%" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar('');">
					<th>Configuration Name</th>
					<th>Default Printer Setting</th>
					<th>Header 1</th>
					<th>Footer 1</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable', {printer_id: '${record.map['printer_id']}', print_type:'${record.map['print_type']}'},'');" id="toolbarRow${st.index}">
						<td>
							<c:choose>
								<c:when test="${record.map['print_type']=='Diag'}">
									Diagnostic Print
								</c:when>
								<c:when test="${record.map['print_type']=='Discharge'}">
									Patient Document Print
								</c:when>
								<c:when test="${record.map['print_type']=='Rad'}">
									Radiology Print
								</c:when>
								<c:otherwise>
									${record.map['print_type']} Print
								</c:otherwise>
							</c:choose>
						</td>
						<td>${record.map['printer_definition_name']}</td>
						<td>${fn:substring(record.map['header1'],0,20)}</td>
						<td>${fn:substring(record.map['footer1'],0,20)}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<div>&nbsp;</div>
		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Logo for Prints</legend>
		<table class="detailList" id="logoPrintTbl" style="width:600px">
			<tr class="header">
				<th class="first">Center</th>
				<th>Logo</th>
				<th>&nbsp;</th>
				<th>&nbsp;</th>
			</tr>
			<c:forEach items="${centersAndLogoSizes}" var="cen" varStatus="st">
				<tr id="row${st.index}">
				    <input type="hidden" name="center_id${st.index}" id= "center_id${st.index}" value="${cen.map.center_id }"/>
					<td>${cen.map.center_name}</td>
					<c:choose>
					<c:when test="${max_centers_inc_default > 1 && centerIdSel != 0 && cen.map.center_id == 0}">
					    <c:set var="disabled" value="disabled" />
					</c:when>
					<c:otherwise>
					    <c:set var="disabled" value="" />
					</c:otherwise>
					</c:choose>
					<td><input type="file" name="logo${st.index}" id="logo${st.index}" ${disabled } accept="<insta:ltext key="upload.accept.image"/>"/></td>
					<td>
						<c:choose>
						    <c:when test="${(cen.map.logo_size > 0)}">
					           <button type="button" name="viewLogo" id="viewLogo" accesskey="V" value="" onclick="showLogo(${cen.map.center_id});"><b><u>V</u></b>iew </button>
					        </c:when>
					        <c:otherwise>
					            &nbsp;
					        </c:otherwise>
					    </c:choose>
				    </td>
					<td>
						<c:choose>
						    <c:when test="${(cen.map.logo_size > 0)}">
					           <button type="button" name="delLogo" accesskey="D" value="" onclick="deleteLogo(${cen.map.center_id});" ${disabled }><b><u>D</u></b>elete</button>
					        </c:when>
					        <c:otherwise>
					            &nbsp;
					        </c:otherwise>
					    </c:choose>
				    </td>						
				</tr>
			</c:forEach>
		</table>
		</fieldset>
	<table class="screenActions">
		<tr>
			<td><button type="submit" onclick="checkAndSubmit();" accesskey="S"><b><u>S</u></b>ave</button></td>
		</tr>
	</table>
	</form>
	</body>
</html>
