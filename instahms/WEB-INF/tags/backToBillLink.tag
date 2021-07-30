<%@ tag dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<%@ attribute name="mr_no" required="true" %>
<%@ attribute name="visit_type" required="true" %>
<%@ attribute name="bill_no" required="true" %>
<%@ attribute name="is_new_ux" required="true" %>
<%@ attribute name="bill_label_prefix_key" required="true" %>

<c:choose>
    <c:when test="${is_new_ux == 'Y'}">
        <c:choose>
            <c:when test="${visit_type == 'o'}">
                <c:set var="flow_type" value="opflow" />
            </c:when>
            <c:otherwise >
                <c:set var="flow_type" value="ipflow" />
            </c:otherwise>
        </c:choose>
        <c:set var="billScreenLink" value="${cpath}/billing/${flow_type}/index.htm#/filter/default/patient/${mr_no}/billing/billNo/${bill_no}?retain_route_params=true"/>
    </c:when>
    <c:otherwise>
        <c:set
            var="billScreenLink"
            value="${cpath}/billing/BillAction.do?_method=getCreditBillingCollectScreen&amp;billNo=${bill_no}"
        />
    </c:otherwise>
</c:choose>
<a onclick="" title="" target="" href="${billScreenLink}"><insta:ltext key="${bill_label_prefix_key}"/> ${param.billNo}</a>
