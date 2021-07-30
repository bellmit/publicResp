<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title>Invalid/No File - Insta HMS</title>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
</head>

<body>
	<br/><br/>
	<div style="margin-bottom:20px; padding:10px 0 10px 10px; background-color:#FFC;"
			class="brB brT brL brR">
		<table>
			<tr>
				<td style="padding-right: 10px; vertical-align: top"><img src="${cpath}/images/error.png"/></td>
				<td>
					The upload file trying to view is invalid/not available.
					Please upload correct file again and view the file<br/>
					${ifn:cleanHtml(msg)}
				</td>
			</tr>
		</table>
	</div>
</body>

</html>
