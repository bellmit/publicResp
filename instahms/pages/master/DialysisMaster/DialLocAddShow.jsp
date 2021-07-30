<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="hospitaladminmasters.locationmaster.addoredit.locationlist"/></title>
<insta:link type="css" file="hmsNew.css"/>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function doClose() {
			window.location.href = "${cpath}/master/locationMaster.do?_method=list&sortOrder=location_name" +
							"&sortReverse=false&lm.status=A";
		}

		<c:if test="${param._method != 'add'}">
		      Insta.masterData=${locationsLists};
		</c:if>

		function validate() {
			var center = document.getElementById('center_id');
			if (center && center.value == '') {
				showMessage("js.dialysismodule.commonvalidations.center");
				center.focus();
				return false;
			}
		}

	</script>

	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>

</head>
<body>

<c:choose>
    <c:when test="${param._method != 'add'}">
        <h1 style="float:left"><insta:ltext key="hospitaladminmasters.locationmaster.addoredit.editlocation"/></h1>
        <c:url var="searchUrl" value="/master/locationMaster.do"/>
        <insta:findbykey keys="location_name,location_id" fieldName="location_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1><insta:ltext key="hospitaladminmasters.locationmaster.addoredit.addlocation"/></h1>
    </c:otherwise>


</c:choose>
<c:set var="status">
 <insta:ltext key="generalmasters.dialyzertypes.list.active"/>,
 <insta:ltext key="generalmasters.dialyzertypes.list.inactive"/>
</c:set>
	<form action="locationMaster.do" method="POST">
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}"/>

		<insta:feedback-panel/>
		<c:if test="${param._method == 'show'}">
			<input type="hidden" name="location_id" value="${bean.map.location_id}"/>

		</c:if>
		<fieldset class="fieldSetBorder">
		<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="hospitaladminmasters.locationmaster.addoredit.locationname"/></td>
				<td>
					 <input type="text" name="location_name" value="${bean.map.location_name}"
					 	class="required" title='<insta:ltext key="hospitaladminmasters.locationmaster.addoredit.locationname"/>' maxlength="30"/>
					 <td>&nbsp;</td>
					 <td>&nbsp;</td>
					 <td>&nbsp;</td>
					 <td>&nbsp;</td>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="hospitaladminmasters.locationmaster.addoredit.status"/></td>
				<td>
					 <insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="${status}"/>
				</td>
			</tr>
			<tr>
				<c:choose>
					<c:when test="${max_centers_inc_default == 1}">
						<input type="hidden" name="center_id" id="center_id" value="0"/>
					</c:when>
					<c:otherwise>
						<td class="formlabel"><insta:ltext key="hospitaladminmasters.locationmaster.addoredit.center"/></td>
						<td class="forminfo">
							<c:choose>
								<c:when test="${param._method == 'add'}">
									<select class="dropdown" name="center_id" id="center_id">
										<option value="">-- Select --</option>
										<c:forEach items="${centers}" var="center">
											<option value="${center.map.center_id}">${center.map.center_name}</option>
										</c:forEach>
									</select>
								</c:when>
								<c:otherwise>
									<input type="hidden" name="center_id" id="center_id" value="${bean.map.center_id}"/>
									${bean.map.center_name}
								</c:otherwise>
							</c:choose>
						</td>
					</c:otherwise>
				</c:choose>
			</tr>

			<tr>
				<td class="formlabel"><insta:ltext key="hospitaladminmasters.locationmaster.addoredit.remarks"/></td>
				<td>
					 <textarea name="remarks" cols="20" rows="5"/>${bean.map.remarks}</textarea>
				</td>
			</tr>

		</table>
		</fieldset>

		<div class="screenActions">
			<button type="submit" accesskey="S" onclick="return validate();"><b><u><insta:ltext key="hospitaladminmasters.locationmaster.addoredit.s"/></u></b><insta:ltext key="hospitaladminmasters.locationmaster.addoredit.ave"/></button>
			|
			<c:if test="${param._method != 'add'}">
				<a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/locationMaster.do?_method=add'"><insta:ltext key="hospitaladminmasters.locationmaster.addoredit.add"/></a>
			|
			</c:if>
			<a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="hospitaladminmasters.locationmaster.addoredit.locationslist"/></a>
	  </div>

	</form>
</body>
</html>
