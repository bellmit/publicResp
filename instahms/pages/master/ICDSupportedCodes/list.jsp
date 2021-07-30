<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
	<head>
			<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
			<title>Supported Code Types - Insta HMS</title>
			<insta:link type="css" file="widgets.css"/>
			<insta:link type="js" file="widgets.js"/>
			<insta:link type="script" file="dashboardColors.js"/>
			<insta:link type="js" file="dashboardsearch.js"/>
			<c:set var="cpath" value="${pageContext.request.contextPath}"/>
			<c:set var="pagepath" value="<%=URLRoute.ICD_SUPPORTED_CODES_MASTER_PATH %>"/>
			<script type="text/javascript">
				var toolbar = {
						Edit: {
								title: "View/Edit",
								imageSrc: "icons/Edit.png",
								href: '/master/icdsupportedcodes/show.htm?',
								onclick: null,
								description: "view or edit the content of the supported  code types",
							}
				};

			function init(){
				createToolbar(toolbar);
			}

			function deleteSelected() {
				var deleteEl = document.getElementsByName("delete_ids");
				for (var i=0; i< deleteEl.length; i++) {
					if (deleteEl[i].checked) {
						document.getElementById("c_category").value = document.getElementsByName("code_category")[0].value;
						document.getElementById("c_type").value = document.getElementsByName("code_type")[0].value;
						document.deleteForm.submit();
						return true;
					}
				}
				alert("select at least one to delete");
				return false;
			}

			</script>
	</head>
	<body onload="init();">
		<c:set var="hasResult" value="${not empty pagedList.dtoList ? 'true': 'false'}"/>
		<div class="pageHeader">Supported Codes</div>
		<insta:feedback-panel/>
		<form name="mrdSupportedCodeTypes" method="GET">

			<insta:search-lessoptions form="mrdSupportedCodeTypes" >

			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel">Code Category</div>
					<div class="sboFieldInput">
							<insta:selectdb name="code_category" table="mrd_supported_code_categories"
								valuecol="code_category" displaycol="code_category" value="${param.code_category}"
								dummyvalue="--Select--"/>
					</div>
				</div>
				<div class="sboField">
						<div class="sboFieldLabel">Code Type</div>
						<div class="sboFieldInput">
								<input type="text" name="code_type" value="${ifn:cleanHtmlAttribute(param.code_type)}"/>
						</div>
				</div>
			</div>
			</insta:search-lessoptions>

			<insta:paginate baseUrl="/${pagepath}/list.htm?" curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		</form>
		<form name="deleteForm" action="${cpath}/${pagepath}/delete.htm" method="POST">
			<input type="hidden" id="c_category" name="c_category"/>
			<input type="hidden" id="c_type" name="c_type"/>
			<div class="resultList">
				<table class="resultList" onmouseover="hideToolBar('');" id="resultTable">
					<tr onmouseover="hideToolBar();">
							<th>Select</th>
							<th>Code Category</th>
							<th>Code Type</th>
					</tr>
					<tr>
						<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
							<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index %2 == 0 ? 'even' : 'odd'}"
								id="toolbarRow${st.index}" onclick="showToolbar(${st.index}, event, 'resultTable',
								{id: '${record.id}'}, '')" >
	
								<td>
									<input type="checkbox" name="delete_ids" value="${record.id}"/>
								</td>
								<td>${record.code_category}</td>
								<td>${record.code_type}</td>
							</tr>
						</c:forEach>
					</tr>
				</table>
			</div>
		</form>
		<c:if test="${empty pagedList.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

		<div class="screenActions" style="float: left">
			<input type="button" name="delete" value="Delete" onclick="deleteSelected();"/> |
			<a href="${cpath}/${pagepath}/add.htm">Add New Supported Code</a>
		</div>
	</body>
</html>
