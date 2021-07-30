<%@ tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@attribute name="store_id" required="true" %>
<%@attribute name="superstore" required="false" %>
<%--
 Can be used inside the JSP like this,
	<insta:getStoreName store_id="${store_id}"/>; will give storename
--%>
<c:set var="storeId" value="${store_id}" />
<jsp:useBean id="storeId" type="java.lang.String"/>
<c:set var="usersStoresList" value="${store_id}" scope="request"/>

<%
	String storename = null;
	storeId = (storeId == null || storeId.isEmpty()) ? null : storeId;
	if(storeId != null) {
		if (superstore != null && !superstore.equals("")) {
			storename = com.bob.hms.common.DataBaseUtil.getStringValueFromDb("select dept_name from stores where is_super_store = 'Y' and dept_id="+storeId);
		}
		else storename = com.bob.hms.common.DataBaseUtil.getStringValueFromDb("select dept_name from stores where dept_id="+storeId);
	}
%>
  <%=storename == null ? "" : com.insta.hms.common.Encoder.cleanHtml(storename)%>
