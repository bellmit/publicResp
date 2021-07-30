<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="/WEB-INF/esapi.tld" prefix="esapi" %>
<html>
<head>
	<title>Error- Insta HMS</title>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script>
		function toggleDetails() {
			var el = document.getElementById('details');
			if (el) {
				var cur = el.style.display;
				if (cur == 'none') { el.style.display = 'block'; }
				else { el.style.display = 'none'; }
			}
		}
	</script>
</head>

<body >

	<div style="margin-bottom:20px; margin-top: 20px; padding:10px 0 10px 10px; background-color:#FFC;" 
			class="brB brT brL brR" >
		<table>
			<tr>
				<td style="padding-right: 10px; vertical-align: top"><img src="${cpath}/images/error.png"/></td>
				<td>
					The requested operation caused an internal system error: <b><c:out value="${Emsg}"/> </b>.<br/>
					<c:if test="${(patientAccess != true) && (doctorAccess != true)}">
						You may contact Customer Support and inform them of the error with the details
						(click on Details button to see the details).<br/>
					</c:if>
					${ifn:cleanHtml(msg)}
				</td>
			</tr>
		</table>
	</div>
	<div>
		<input type="button" name="back" value="Back" onclick="javascript:history.back();">
		<input type="button" value="Details" onclick="toggleDetails()" style="display:${showStackTrace ? 'block': 'none'}">
	</div>

	<div id="details" style="horizontal-align: center; display:none; width: 900px; overflow: auto; "><pre>
<esapi:encodeForHTML>
<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${currentDate}"/> <c:out value="${Exception}"/>
<c:forEach items="${StackTrace}" var="line">
	${line.className}.${line.methodName} (${line.fileName}:${line.lineNumber})</c:forEach>
<c:if test="${not empty NextExceptionMessage}">
	Next Exception: ${NextExceptionMessage}
</c:if>
</esapi:encodeForHTML>
	</pre></div>

</body>
</html>

