<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<c:if test="${not empty license_data && license_data.status != ''}">
	<fmt:formatDate value="${license_data.due}" var="dueDateFormatted" pattern="MMM dd, yyyy"/>
	<fmt:formatDate value="${license_data.expiry}" var="expiryDateFormatted" pattern="MMM dd, yyyy"/>
	<script data-status="${license_data.status}" id="amcExpiry">
	    <c:choose>
	    	<c:when test="${license_data.status eq 'due' && license_data.type eq 'R'}">
				<fmt:message key="ui.message.instacollection.rental.due">
					<fmt:param value="${dueDateFormatted}"/>
					<fmt:param value="${expiryDateFormatted}"/>
				</fmt:message>
			</c:when>
			<c:when test="${license_data.status eq 'overdue' && license_data.type eq 'R'}">
				<fmt:message key="ui.message.instacollection.rental.overdue">
					<fmt:param value="${expiryDateFormatted}"/>
				</fmt:message>
			</c:when>
			<c:when test="${license_data.status eq 'expired' && license_data.type eq 'R'}">
				<fmt:message key="ui.message.instacollection.rental.expired" />
			</c:when>
			<c:when test="${license_data.status eq 'due' && license_data.type eq 'A'}">
				<fmt:message key="ui.message.instacollection.amc.due">
					<fmt:param value="${dueDateFormatted}"/>
					<fmt:param value="${expiryDateFormatted}"/>
				</fmt:message>
			</c:when>
			<c:when test="${license_data.status eq 'overdue' && license_data.type eq 'A'}">
				<fmt:message key="ui.message.instacollection.amc.overdue">
					<fmt:param value="${expiryDateFormatted}"/>
				</fmt:message>
			</c:when>
			<c:when test="${license_data.status eq 'expired' && license_data.type eq 'A'}">
				<fmt:message key="ui.message.instacollection.amc.expired">
					<fmt:param value="${dueDateFormatted}"/>
				</fmt:message>
			</c:when>
			<c:when test="${license_data.status eq 'due' && license_data.type eq 'L'}">
				<fmt:message key="ui.message.instacollection.license.due">
					<fmt:param value="${dueDateFormatted}"/>
					<fmt:param value="${expiryDateFormatted}"/>
				</fmt:message>
			</c:when>
			<c:when test="${license_data.status eq 'overdue' && license_data.type eq 'L'}">
				<fmt:message key="ui.message.instacollection.license.overdue">
					<fmt:param value="${expiryDateFormatted}"/>
				</fmt:message>
			</c:when>
			<c:when test="${license_data.status eq 'expired' && license_data.type eq 'L'}">
				<fmt:message key="ui.message.instacollection.license.expired">
					<fmt:param value="${dueDateFormatted}"/>
				</fmt:message>
			</c:when>
		</c:choose>
	</script>
</c:if>

