<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Print Templates List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<script>
		function checkAllocation(printTemplateId){
			var ajaxobj = newXMLHttpRequest();
			var printTempId = '';
			var url = cpath + "/master/CommonPrintTemplates.do?_method=getTemplateId&printTemplateId=" + printTemplateId;
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					printTempId = (ajaxobj.responseText);
					if(printTempId != null && printTempId!=''){
						if(printTemplateId == printTempId){
							var deleteEl = document.getElementsByName("deletePrintTemplate");
							for (var i=0; i< deleteEl.length; i++) {
								if (deleteEl[i].checked && deleteEl[i].value == printTempId) {
									alert("You can not delete the template linked with the form");
									deleteEl[i].checked = false;
									deleteEl[i].disabled = true;
								}else {
									deleteEl[i].disabled = false;
								}	
							}
							return false;
						}
					}
				}
			}
		}
		function deleteSelected(e) {
			var deleteEl = document.getElementsByName("deletePrintTemplate");
			var isSelected = false;
			for (var j=0; j< deleteEl.length; j++) {
				if (deleteEl[j].checked ) {
					isSelected = true;
					break;
				}
			}
			if (!isSelected) {
				alert("select at least one template name to delete");
				YAHOO.util.Event.stopEvent(e);
				return false;
			}
			var canDelete = false;
			for (var i=0; i< deleteEl.length; i++) {
				var printTemplateId = deleteEl[i].value;
				checkAllocation(printTemplateId);
				if (deleteEl[i].checked) {
					canDelete = true;
				}
			}
			if (!canDelete) {
				YAHOO.util.Event.stopEvent(e);
			}
			return canDelete;
			
		}
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/CommonPrintTemplates.do?_method=show',
				onclick: null,
				description: "View and/or Edit Template details"
				}
		};
		function init() {
			createToolbar(toolbar);
		}
	</script>
</head>
<body onload="init();">
	<h1>Print Templates List - (Html/Text)</h1>
	<insta:feedback-panel/>
	<form method="GET" action="CommonPrintTemplates.do" name="searchForm">
		<input type="hidden" name="_method" value="list"/>
		<insta:search-lessoptions form="searchForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Template Type: </div>
						<div class="sboFieldInput">
							<select class="dropdown" name="template_type">
								<option value="">-- All --</option>
								<c:forEach items="${template_types}" var="temp_type">
									<option value="${temp_type.type}" ${param.template_type == temp_type.type ? 'selected' : ''}>
										${temp_type.type}
									</option>
								</c:forEach>
							</select>
						</div>
					</td>
					<td class="sboField">
						<div class="sboFieldLabel">Template Name: </div>
						<div class="sboFieldInput">
							<input type="text" name="template_name" value="${ifn:cleanHtmlAttribute(param.template_name)}"/>
							<input type="hidden" name="template_name@op" value="ilike"/>
						</div>
					</td>
				</tr>
			  </table>
		</insta:search-lessoptions>
	</form>
	<form name="deletePrintTemplateForm">
		<input type="hidden" name="_method" value="delete"/>
		<insta:paginate curPage="${templateList.pageNumber}" numPages="${templateList.numPages}" totalRecords="${templateList.totalRecords}" />
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" onmouseover="hideToolBar('');" id="resultTable">
				<tr onmouseover="hideToolBar();">
					<th>Select</th>
					<th>Template Name</th>
					<th>Template Type</th>
					<th>Template Mode</th>
					<th>User Name</th>
					<th>Reason for customization</th>
				</tr>
				<c:forEach var="temp" items="${templateList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : '' } ${st.index %2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{template_name: '${temp.template_name}', template_type: '${temp.template_type}'},'')"; id="toolbarRow${st.index}">
						<td>
							<input type="checkbox" name="deletePrintTemplate" id="deletePrintTemplate"
								value="${temp.print_template_id}"/>
						</td>
						<td><insta:truncLabel value="${temp.template_name}" length="30"/></td>
						<td><insta:truncLabel value="${temp.template_type}" length="20"/></td>
						<td>${temp.template_mode == 'T' ? 'Text' : 'HTML' }</td>
						<td><insta:truncLabel value="${temp.user_name}" length="20"/></td>
						<td><insta:truncLabel value="${temp.reason}" length="50"/></td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<c:url var="url" value="CommonPrintTemplates.do">
			<c:param name="_method" value="templateMode"/>
		</c:url>

		<div class="screenActions">
			<c:if test="${not empty templateList}">
				<input type="submit" name="Delete" value="Delete" onclick="deleteSelected(event)">&nbsp;|
			</c:if>
				<a href="<c:out value='${url}' />"> Add New Print template</a>
		</div>

	</form>

</body>
</html>