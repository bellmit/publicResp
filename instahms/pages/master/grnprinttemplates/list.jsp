<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>GRN Print Template</title>

		<insta:link type="css" file="widgets.css"/>
		<c:set var="cpath" value="${pageContext.request.contextPath}"/>
		<c:set var="pagePath" value="<%=URLRoute.GRN_PRINT_TEMPLATE_PATH %>"/>

		<script>
		
			function validateAtleastOne(){
				var deleteEl = document.getElementsByName("deleteGRNPrint");
				for (var i=0; i< deleteEl.length; i++) {
					if (deleteEl[i].checked) {
					return true;
					}
				}
				alert("select at least one template name to delete");
				return false;
			}
			
			function deleteSelected() {
				if ( validateAtleastOne()){	
				document.grnprint.action = "${cpath}/${pagePath}/deletetemplates.htm"
				document.grnprint.submit();
				}
			}

			var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: "${pagePath}/show.htm?",
				onclick: null,
				description: "View and/or Edit GRN Print"
			}
			};
			function init()
			{
				createToolbar(toolbar);
			}
		</script>
	</head>
	<body onload="init();">

		<h1>GRN Print Templates</h1>

		<insta:feedback-panel/>

		<form name="grnprint" action="list.htm" >

			<div class="resultList">
				<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
					<tr onmouseover="hideToolBar();">
						<th>Select </th>
						<th>Template Name</th>
						<th>Template Mode</th>
						<th>User Name</th>
						<th>Reason for Customization</th>
					</tr>

					<c:forEach var="temp" items="${pagedList.dtoList}" varStatus="st">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
							onclick="showToolbar(${st.index}, event, 'resultTable',
							{template_id : '${temp.template_id}'},'');" id="toolbarRow${st.index}">
							<td><input type="checkbox" name="deleteGRNPrint" id="deleteGRNPrint"
								value="${temp.template_id}" /></td>
						 	<td>${temp.template_name}</td>
							<td>${temp.template_mode == 'H'?'Html':'Text'}</td>
							<td>${temp.user_name}</td>
							<td>${temp.reason}</td>
				  		</tr>
					</c:forEach>
				</table>
			</div>

			<c:url var="url" value="${pagePath }/addnewtemplate.htm">
			</c:url>

			<div class="screenActions"><input type="button" name="delete" value="Delete"  onclick="return deleteSelected()"/>
			|<a href="<c:out value='${url}' />"> Add New GRN Print Template</a></div>

		</form>

	</body>
</html>

