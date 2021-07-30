<%@ tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>

<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ attribute name="name" required="true" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="value" required="false" %>
<%@ attribute name="jsvar" required="true"%>
<%@ attribute name="common_datasource" required="false"%>

<!-- usage <insta:autocomplete-combobox id="testnameac1" name="testnameac1"  jsvar="complaints" common_datasource="true" /> -->

<div id="autocomplete_${id}" class="autocomplete_combo_widget">
        <input type="text" id="${id}" jsvar="${jsvar}"
        <c:choose>
                <c:when test="${not empty common_datasource}">common_datasource="${common_datasource}"</c:when>
                <c:otherwise >common_datasource="true"</c:otherwise>
        </c:choose>

        displayValue="${value}" />
        <div id="container_${id}"></div>
</div>
<input id="autocomplete_hiidden_${id}" type="hidden" name="${name}" value="${value}"/>
