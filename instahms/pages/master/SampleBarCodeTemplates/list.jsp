<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Samples Bar Code Print Templates : Insta HMS</title>
<insta:link type="css" file="hmsNew.css"/>
    <script type="text/javascript">

    		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/SampleBarcodePrintTemplate.do?method=show',
				onclick: null,
				description: "View or Edit template details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}
		function validate() {
			var deleteSamplePrints = document.getElementsByName('deleteSamplePrintList');
			for (var i=0; i<deleteSamplePrints.length; i++) {
				if (deleteSamplePrints[i].checked) {
					return true;
				}
			}
			alert('Please select at least one template to delete');
			return false;
		}

    </script>

</head>
<body onload="init();">
	<div class="pageHeader">Samples Bar Code  Print Templates</div>
	<insta:feedback-panel/>
	<form name="sampleBarCodeForm" method="POST" action="SampleBarcodePrintTemplate.do" >
		<input type="hidden" name="method" value="delete"/>
	<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
		<tr onmouseover="hideToolBar();">
			<th>Select</th>
			<th>Template Name</th>
			<th>User Name</th>
			<th>Customized</th>
			<th>Reason for Customization</th>
		</tr>

		<c:choose>
			<c:when test="${not empty sampleTemplates}">
				<c:forEach items="${sampleTemplates}" var="template" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${st.index}, event, 'resultTable',
								{title: 'Samples Bar Code Template',template_name:'${template.map.template_name}',customized: '${not empty template.map.print_template_content?'true':'false'}'},'');" id="toolbarRow${st.index}">
						<td><input type="checkbox" name="deleteSamplePrintList" value="${template.map.template_name}" /></td>
						<td>${template.map.template_name}</td>
						<td>${template.map.user_name}</td>
						<td>${not empty template.map.print_template_content?'Yes':'No'}</td>
						<td><c:out value="${template.map.reason}"/></td>
					</tr>
				</c:forEach>
			</c:when>
			<c:otherwise>
				<tr>
					<td colspan="3" align="center">Customizable Print Template not found.</td>
				</tr>
			</c:otherwise>
		</c:choose>
	</table>
	<div class="screenActions">
		<input type="submit" name="submit" value="Delete" onclick="return validate()" /> |
		<a href="SampleBarcodePrintTemplate.do?method=add">Add New Samples Barcode Template</a>

	</div>
	</form>
</body>
</html>
