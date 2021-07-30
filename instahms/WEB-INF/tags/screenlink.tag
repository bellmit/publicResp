<%@ tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="screenId" required="true" %>
<%@ attribute name="extraParam" required="false" %>
<%@ attribute name="target" required="false" %>
<%@ attribute name="title" required="false" %>
<%@ attribute name="onClickValidation" required="false" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="addPipe" required="false"%>
<%@ attribute name="type" required="false" %>
<%@ attribute name="accessKey" required="false" %>
<%@ attribute name="accessKeyLabel" required="false" %>
<%@ attribute name="buttonId" required="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}/"/>
<%--
	Generates an anchor tag if screenId having access rights .
	Example Usage:
			<insta:screenlink screenId="emr_screen" extraParam="?method=list&MrNo=${mrNo}" label="EMR" />

    This will produce the following html output
			<a href="{cpath}/emr/EMRMainDisplay.do?method=list&MrNo=${mrNo}">EMR</a>
--%>

<%--
The parameter is reatianed as screen id although in reality it is the action id associated with the url.
Once we have all the action-id to screen-id is place we can change the name of the variable to action-id.
--%>

<c:set var="screeId" value="${screenId}" />
<jsp:useBean id="screeId" type="java.lang.String"/>

<%
String url = null;

java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext().getAttribute("actionUrlMap");
if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get(screeId).equals("A")) { // we have the rights.
	url = (String)actionUrlMap.get(screeId); // lookup the url for the action.
}

if(url != null) {%>

	<c:set var="otherAttributes">
		<c:forEach items="${dynattrs}" var="attribute">
			${attribute.key}=${attribute.value}
		</c:forEach>
	</c:set>
	
	<c:choose>
		<c:when test="${not empty type && type eq 'button'}">
			<button type="button" onclick="${onClickValidation}" accessKey="${accessKey} ${otherAttributes}" id="${buttonId}"><b><u><insta:ltext key="${accessKeyLabel}"/></u></b><insta:ltext key="${label}"/></button>
			<c:if test="${screeId == 'patient_writeoff'}">
				<img class="imgHelpText" title="Patient Due Write Off" src="${cpath}/images/help.png"/>
			</c:if>
		</c:when>
		<c:otherwise>
			<c:set var="screen_url" value="<%=url%>" />
			<c:if test="${not empty addPipe && addPipe == 'true'}"> | </c:if>
			<a href="<c:out  value="${cpath}${screen_url}${extraParam}" />" target="${target}" title="${title}" onClick="${onClickValidation}" ${otherAttributes}>${ifn:cleanHtml(label)}</a>
		</c:otherwise>
	</c:choose>
<%}%>
