<%@ tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@attribute name="center_id" required="true" %>
<%--
 Can be used inside the JSP like this,
	<insta:getCenterName center_id="${center_id}"/>;can give centername
--%>
<c:set var="centerId" value="${center_id}" />
<jsp:useBean id="centerId" type="java.lang.String"/>

<%
	String centername = com.bob.hms.common.DataBaseUtil.getStringValueFromDb("select center_name from hospital_center_master where center_id="+centerId);
%>
  <%=centername == null ? "" : com.insta.hms.common.Encoder.cleanHtml(centername)%>
