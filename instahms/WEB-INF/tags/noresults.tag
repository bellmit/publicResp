<%@tag pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@attribute name="hasResults" required="true" type="java.lang.Boolean"%>
<%@attribute name="message" required="false"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="noResultMessage">
<insta:ltext key="search.noresult.message"/>
</c:set>
<c:if test="${!hasResults}">
	<div style="width: 951px; height: 35px; border: 1px solid #E0E0E0; border-top: none; background-color:#FFC">
		<div style="float: left; width: 25px; margin-top: 10px; margin-left: 3px;">
			<img src="${cpath}/images/alert.png"/>
		</div>
		<div style="float: left; margin-top: 10px">
			${not empty message ? message : noResultMessage}
		</div>
	</div>
</c:if>
