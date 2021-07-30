<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
  <title>Insta HMS</title>
</head>

<body>
  <p>Parameters received
  <table border="1" cellspacing="0" cellpadding="5">
    <tr>
      <th>Param Name</th>
      <th>Param value(s)</th>
    </tr>
    <%-- note: ${paramValues} instead of ${param} gives access to an array rather
       than a single value --%>
    <c:forEach items="${paramValues}" var="par">
      <tr>
        <td>${ifn:cleanHtml(par.key)}</td>
				<td>
					<c:forEach items="${par.value}" var="value" varStatus="sts">
						<c:if test="${sts.index > 0}">,</c:if>
        		<%-- using c:out escapes the characters if required, uses JSTL --%>
						<c:out value="${value}"/>
					</c:forEach>
				</td>
      </tr>
    </c:forEach>
  </table>

</body>

</html>


