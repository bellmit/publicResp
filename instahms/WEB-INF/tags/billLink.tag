<%@ tag dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ attribute name="mr_no" required="true" %>
<%@ attribute name="visit_type" required="true" %>
<%@ attribute name="bill_no" required="true" %>
<%@ attribute name="is_new_ux" required="true" %>

<c:choose>
    <c:when test="${is_new_ux == 'Y'}">
        <c:choose>
            <c:when test="${visit_type == 'o'}">
                <c:set var="newUXScreenId" value="new_op_bill" />
            </c:when>
            <c:otherwise >
                <c:set var="newUXScreenId" value="new_ip_bill" />
            </c:otherwise>
        </c:choose>
        <insta:screenlink
            screenId="${newUXScreenId}"
            extraParam="/index.htm#/filter/default/patient/${mr_no}/billing/billNo/${bill_no}?retain_route_params=true"
            label="Bill ${bill_no}"
        />
    </c:when>
    <c:otherwise>
        <insta:screenlink
            screenId="credit_bill_collection"
            extraParam="?_method=getCreditBillingCollectScreen&billNo=${bill_no}"
            label="Bill ${bill_no}"
        />
    </c:otherwise>
</c:choose>


