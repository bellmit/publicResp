<%@ tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>

<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ attribute name="screenId" required="true" %>
<%@ attribute name="mrNo" required="true" %>
<%@ attribute name="centerId" required="true" type="java.lang.Integer" %>
<%@ attribute name="visitId" required="false" type="java.lang.String" %>
<%@ attribute name="onClickAction" required="false" type="java.lang.String" %>


<%-- Found that if table do not have column name = "status", filtercol will give error i:e "status column does not exit",
To resovle this error pass filtercol attribute also along with other attribute like valuecol and displaycol etc.
For example Table "u_role" have status column with different name as "role_status" which give error
while using this tag. So to make it work pass additional attribute as "filtercol" along with other attribute.

To display a combobox containing a list of only active role queries from u_role, we would specify the tag as:
<insta:selectdb  name="role" value="" table="u_role" valuecol="role_id" displaycol="role_name" filtercol="role_status" ></insta:selectmaster>

--%>
<%
if (visitId == null) {
	visitId = "";
}
java.util.List links = com.insta.hms.common.DatabaseHelper.queryToDynaList(
	"SELECT label, replace(replace(replace(link,'<<mrno>>', ?::text), '<<visitid>>', ?::text), '<<centerid>>', ?::text) as link from external_links WHERE center_id in (0,?) AND screen_id = ? ORDER BY label",
	new Object[] {mrNo, visitId, centerId, centerId, screenId});
if (links == null) {
  links = new java.util.ArrayList();
}
	request.setAttribute("externallinks", links);
%>
<c:forEach items="${externallinks}" var="link">
	<c:choose>
		<c:when test = "${empty onClickAction}">
			&nbsp;|&nbsp;<a href="link.map["link"]"target="_blank">${link.map["label"]}</a>
		</c:when>
		<c:otherwise>
			&nbsp;|&nbsp;<div role="button" target="_blank" onclick="${onClickAction}" class="ui typography inline sc-jTqLGJ eQuLAA small cursor instaBlue small">${link.map["label"]}</div>
		</c:otherwise>
	</c:choose>
</c:forEach>
