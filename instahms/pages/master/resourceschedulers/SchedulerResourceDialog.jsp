<%@page import="com.insta.hms.master.URLRoute"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<c:set var="pagePath" value="<%=URLRoute.RESOURCE_AVAILABILITY_PATH %>"/>
<c:if test="${max_centers_inc_default > 1}">
	<div id="appt_center_div">
		<table style="margin-left:5px;">
			<tr>
				<td><insta:ltext key="patient.resourcescheduler.schedulerresourcedialog.td"/></td>
				<td><label id="appt_center"></label></td>
				<td>&nbsp;</td>
			</tr>
		</table>
	</div>
</c:if>
<table id="resourceMainTable" border="0" style="margin:5px 0 10px 5px">
	<tr>
		<td>
			<table width="100%" id="resourceTable" cellspacing="0" cellpadding="0" class="delActionTable">
				<tr class="header">
					<td class="first"><insta:ltext key="patient.resourcescheduler.schedulerresourcedialog.resourcetype"/></td>
					<td><insta:ltext key="patient.resourcescheduler.schedulerresourcedialog.resource"/></td>
					<td>&nbsp;</td>
				</tr>
			</table>
		</td>
		<td valign="bottom" style="padding-left:5px"><input type="button" value="+" name="addresource" onclick="addResourceRow()" disabled class="plus" /></td>
	</tr>
</table>
