<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="js" file="widgets.js"/>
		<insta:link type="js" file="dashboardsearch.js"/>
		<title>Claim Template - Insta HMS</title>
		<script>
			function deleteSelected(e) {
			var deleteEl = document.getElementsByName("deleteClaimTemplate");
			for (var i=0; i< deleteEl.length; i++) {
			if (deleteEl[i].checked) {
			return true;
			}
			}
			alert("select at least one template name to delete");
			YAHOO.util.Event.stopEvent(e);
			return false;
			}


			var toolbar = {
				Edit: {
					title: "View/Edit",
					imageSrc: "icons/Edit.png",
					href: 'master/InsuranceClaimTemplate.do?method=show',
					onclick: null,
					description: "View and/or Edit Incurance Claim details"
					}
				};
			function init()
			{
				createToolbar(toolbar);
			}

		</script>
	</head>
<body onload="init()">
	<jsp:useBean id="editorMode" class="java.util.HashMap"/>
	<c:set target="${editorMode}" property="P" value="tinyMCE"/>
	<c:set target="${editorMode}" property="R" value="text"/>

	<jsp:useBean id="format" class="java.util.HashMap"/>
	<c:set target="${format}" property="P" value="PDF"/>
	<c:set target="${format}" property="R" value="RTF"/>


	<h1>Claim Template</h1>

	<insta:feedback-panel/>

	<form name="insuranceClaimTemplate" method="GET" action="InsuranceClaimTemplate.do">
		<input type="hidden" name="method" value="list"/>
		<insta:search-lessoptions form="insuranceClaimTemplate" >
		<table class="searchBasicOpts" >
			<tr>
				<td class="sboField">
					<div class="sboFieldLabel">Template Name :</div>
					<div class="sboFieldInput">
						<input type="text" name="template_name" value="${ifn:cleanHtmlAttribute(param.template_name)}"/>
					</div>
				</td>
				<td class="sboField">
					<div class="sboFieldLabel">Template Type :</div>
					<div class="sboFieldInput" style="height:50px">
						<select name="template_type" id="template_type" class="dropdown" >
							<option ${ifn:cleanHtmlAttribute(param.template_type=='' ? 'selected' : '')} value="">All</option>
							<option ${ifn:cleanHtmlAttribute(param.template_type=='P' ? 'selected' : '')} value="P">PDF Template</option>
							<option ${ifn:cleanHtmlAttribute(param.template_type=='R' ? 'selected' : '')} value="R">RTF Template</option>
					</select>
					</div>
				</td>
				<td class="sboField">
					<div class="sboFieldLabel">Status :</div>
					<div class="sboFieldInput" style="height:55px">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in" />
					</div>
				</td>
			</tr>
		  </table>
	  </insta:search-lessoptions>

		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<th>Template Name</th>
				<th>Template Type</th>
				<th>&nbsp;</th>
				<th>&nbsp;</th>
				<th>&nbsp;</th>
				<th>&nbsp;</th>
			</tr>

			<c:forEach var="temp" items="${templateList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
					{claim_template_id: '${temp.claim_template_id}', _editorMode: '${editorMode[temp.template_type]}'},'');" id="toolbarRow${st.index}">
				 	<td>${temp.template_name}</td>
				 	<td>${format[temp.template_type]}</td>
				 	<td>&nbsp;</td>
				 	<td>&nbsp;</td>
				 	<td>&nbsp;</td>
				 	<td>&nbsp;</td>
		  		</tr>
			</c:forEach>

		</table>
		<c:url var="url" value="InsuranceClaimTemplate.do">
			<c:param name="method" value="add"/>
		</c:url>
		<table class="screenActions">
			<tr>
				<td>
					<a href="<c:out value='${url}'/>"></b>Add New Claim Template</b></a>
				</td>
			</tr>
		</table>
	</form>

</body>
</html>

