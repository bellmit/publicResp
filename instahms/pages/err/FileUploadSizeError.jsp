<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
	<title>Error- Insta HMS</title>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
</head>

<body >
	<div style="margin-bottom:20px; margin-top: 20px; padding:10px 0 10px 10px; background-color:#FFC;"
			class="brB brT brL brR" >
		<table>
			<tr>
				<td style="padding-right: 10px; vertical-align: top"><img src="${cpath}/images/error.png"/></td>
				<td><b>Unable to upload the file: file size greater than 10 MB.
					<br> Please upload other file.</b></td>
			</tr>
		</table>
	</div>
	<div >
		<input type="button" name="back" value="Back" onclick="javascript:history.back();">
	</div>

</body>
</html>

