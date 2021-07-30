<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ page
	import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="hijricalendar" value='<%=GenericPreferencesDAO.getAllPrefs().get("hijricalendar")%>'/>
<dl class="accordion" style="margin-left:5px">
  <dt>
    <span><insta:ltext key="patient.resourcescheduler.recurringappointments.recurrence"/></span>
    <div class="clrboth"></div>
  </dt>
  <dd id="recurranceDD">
    <c:if test="${hijricalendar=='Y'}"> 
  <table>
      <tr style="display:none;" id="calendarTypeTr">
          <td align="right"><input type="radio" id="calendarTypeG" name="calendarType" value="G" onclick="selectGregorian(this);" checked>Gregorian</td>
          <td><input type="radio" id="calendarTypeH" name="calendarType" value="H" onclick="selectHijri(this);">Hijri</td>
      </tr>
  </table>
  </c:if>
  <table>
  <c:set var="recurranceOption">
	<insta:ltext key="patient.resourcescheduler.recurringappointments.days"/> ,
	<insta:ltext key="patient.resourcescheduler.recurringappointments.weeks"/>,
	<insta:ltext key="patient.resourcescheduler.recurringappointments.months"/>,
	<insta:ltext key="patient.resourcescheduler.recurringappointments.years"/>
</c:set>
	<tr>
		<!--
		<td>Recurr. From</td>
		<td><insta:datewidget name="recurrenceStartDate"></insta:datewidget></td>
		 -->
		<td><insta:ltext key="patient.resourcescheduler.recurringappointments.every"/></td>
		<td>
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td rowspan="2"><input type="text" style="width:3em;" value="0" name="recurrNo" onkeypress="return enterNumOnlyzeroToNine(event)"/></td>
					<td><img src="${pageContext.request.contextPath}/icons/tablesortup.gif"
							align="absmiddle" style="width:8px;height:8px;" onclick="incrRecurr();"/>
					</td>
					<td>&nbsp;</td>
					<td rowspan="2"><insta:selectoptions name="recurranceOption" optexts="${recurranceOption}" opvalues="D,W,M,Y" value="" style="width:8em;" onchange="showHideWeekMonth(this)"/></td>
				</tr>
				<tr>
					<td>
					<img src="${pageContext.request.contextPath}/icons/tablesortdown.gif"
					align="absmiddle" style="width:8px;height:8px;" onclick="decrRecurr();"/>
					</td>
				</tr>
			</table>
		</td>
		<td>
			<div id="weekTD">
				<table>
					<tr>
						<td><b><insta:ltext key="patient.resourcescheduler.recurringappointments.on"/></b></td>
						<td>
							<input type="checkbox" name="weekCheck" value="1" onclick="assignWeek(this,'sun')"/><insta:ltext key="patient.resourcescheduler.recurringappointments.sun"/>
							<input type="hidden" value="" name="week" id="sun" disabled>
						</td>
						<td>
							<input type="checkbox" name="weekCheck" value="2" onclick="assignWeek(this,'mon')"/><insta:ltext key="patient.resourcescheduler.recurringappointments.mon"/>
							<input type="hidden" value="" name="week" id="mon" disabled>
						</td>
						<td>
							<input type="checkbox" name="weekCheck" value="3" onclick="assignWeek(this,'tue')"/><insta:ltext key="patient.resourcescheduler.recurringappointments.tue"/>
							<input type="hidden" value="" name="week" id="tue" disabled>
						</td>
						<td>
							<input type="checkbox" name="weekCheck" value="4" onclick="assignWeek(this,'wed')"/><insta:ltext key="patient.resourcescheduler.recurringappointments.wed"/>
							<input type="hidden" value="" name="week" id="wed" disabled>
						</td>
						<td>
							<input type="checkbox" name="weekCheck" value="5" onclick="assignWeek(this,'thu')"/><insta:ltext key="patient.resourcescheduler.recurringappointments.thu"/>
							<input type="hidden" value="" name="week" id="thu" disabled>
						</td>
						<td>
							<input type="checkbox" name="weekCheck" value="6" onclick="assignWeek(this,'fri')"/><insta:ltext key="patient.resourcescheduler.recurringappointments.fri"/>
							<input type="hidden" value="" name="week" id="fri" disabled>
						</td>
						<td>
							<input type="checkbox" name="weekCheck" value="7" onclick="assignWeek(this,'sat')"/><insta:ltext key="patient.resourcescheduler.recurringappointments.sat"/>
							<input type="hidden" value="" name="week" id="sat" disabled>
						</td>
					</tr>
				</table>
			</div>
			<div id="monthTD">
				<table>
				<tr>
					<td><b><insta:ltext key="patient.resourcescheduler.recurringappointments.on"/></b></td>
					<td><input id="monthImagePicker" size="10" readonly>&nbsp;</td>
				</tr>
				</table>
			</div>
		</td>
		<td><insta:selectoptions name="repeatOption" optexts="For,Until" opvalues="FOR,UNTIL" value="" style="width:5em;" onchange="showHideForUntil(this);"/></td>
		<td>
		<div id="forTD">
			<table>
				<tr>
					<td rowspan="2"><input type="text" style="width:5em;" value="0" name="occurrNo" onkeypress="return enterNumOnlyzeroToNine(event)"/></td>
					<td><img src="${pageContext.request.contextPath}/icons/tablesortup.gif"
							align="absmiddle" style="width:8px;height:8px;" onclick="incrOccurr();"/>
					</td>
					<td>&nbsp;</td>
					<td rowspan="2"><insta:ltext key="patient.resourcescheduler.recurringappointments.occurrences"/></td>
				</tr>
				<tr>
					<td>
					<img src="${pageContext.request.contextPath}/icons/tablesortdown.gif"
					align="absmiddle" style="width:8px;height:8px;" onclick="decrOccurr();"/>
					</td>
				</tr>
				</table>
		</div>
		<div id="untilTD">
			<table>
				<tr>
					<td><input id="untilImagePicker" size="10" readonly>&nbsp;</td>
					<td>&nbsp;</td>
					<td><insta:ltext key="patient.resourcescheduler.recurringappointments.occurrences"/></td>
				</tr>
			</table>
		</div>
		</td>
		</tr>
	</table>
</dd>
</dl>
