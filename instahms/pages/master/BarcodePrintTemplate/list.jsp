<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title> Bar Code Print Templates : Insta HMS</title>
<insta:link type="css" file="hmsNew.css"/>
    <script type="text/javascript">

    		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/BarcodePrintTemplate.do?method=show',
				onclick: null,
				description: "View or Edit template details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}

    </script>

</head>
<body onload="init();">
	<div class="pageHeader">Bar Code  Print Templates</div>
	<insta:feedback-panel/>
	<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
		<tr onmouseover="hideToolBar();">
			<th>Template Name</th>
			<th>User Name</th>
			<th>Customized</th>
			<th>Reason for Customization</th>
		</tr>

		<c:choose>
			<c:when test="${not empty printTemplates}">
				<c:forEach items="${printTemplates}" var="template" varStatus="st">
					<c:choose>
						<c:when test="${template.map.template_type == 'REGBARCODE'}">
							<c:set var="title" value="Registration Bar Code Print Template"/>
						</c:when>
						<c:when test="${template.map.template_type == 'SAMBARCODE'}">
							<c:set var="title" value="Sample Collection Bar Code Print Template"/>
						</c:when>
						<c:when test="${template.map.template_type == 'ITMBARCODE'}">
							<c:set var="title" value="Item Bar Code Print Template"/>
						</c:when>
					</c:choose>
					<c:url value="BarcodePrintTemplate.do" var="printTemplateUrl">
						<c:param name="method" value="show" />
						<c:param name="template_type" value="${template.map.template_type}"/>
						<c:param name="title" value="${title}"/>
						<c:param name="customized" value="${not empty template.map.print_template_content?'true':'false'}"/>
					</c:url>
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${st.index}, event, 'resultTable',
								{template_type: '${template.map.template_type}',title: '${title}',customized: '${not empty template.map.print_template_content?'true':'false'}'},'');" id="toolbarRow${st.index}">
						<td>${title}</td>
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
</body>
</html>
