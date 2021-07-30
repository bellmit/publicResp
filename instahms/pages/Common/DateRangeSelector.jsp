<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<%-- JSP for inclusion in other pages: this shows a page-section for
selection of a date range --%>

<c:if test="${param.addTable!='N'}">
<table>
</c:if>
	<c:if test="${param.addTitle!='N'}">
	<tr>
		<td colspan="2"><insta:ltext key="registration.datarangeselector.data.selectadaterange"/></td>
	</tr>
	</c:if>
	<tr>
		<td valign="top">
			<c:if test="${param.skipDay!='Y'}">
				<input checked type="radio" id="pd" name="_sel" onclick="setDateRangeYesterday(fromDate, toDate)">
				<label for="pd"><insta:ltext key="registration.datarangeselector.data.yesterday"/></label>
				<br/>

				<input type="radio" id="td" name="_sel" onclick="setDateRangeToday(fromDate, toDate)">
				<label for="td"><insta:ltext key="registration.datarangeselector.data.today"/></label>
				<br/>
			</c:if>

			<c:if test="${param.skipWeek!='Y'}">
				<input type="radio" id="pw" name="_sel" onclick="setDateRangePreviousWeek(fromDate, toDate)">
				<label for="pw"><insta:ltext key="registration.datarangeselector.data.previousweek"/></label>
				<br/>

				<input type="radio" id="tw" name="_sel" onclick="setDateRangeWeek(fromDate, toDate)">
				<label for="tw"><insta:ltext key="registration.datarangeselector.data.thisweek"/></label>
				<br/>
			</c:if>

			<c:if test="${param.skipMonth!='Y'}">
				<input type="radio" id="pm" name="_sel" onclick="setDateRangePreviousMonth(fromDate, toDate)">
				<label for="pm"><insta:ltext key="registration.datarangeselector.data.previousmonth"/></label>
				<br/>

				<input type="radio" id="tm" name="_sel" onclick="setDateRangeMonth(fromDate, toDate)">
				<label for="tm"><insta:ltext key="registration.datarangeselector.data.thismonth"/></label>
				<br/>
			</c:if>

			<c:if test="${param.skipYear!='Y'}">
				<input type="radio" id="pfy"
							name="_sel" onclick="setDateRangePreviousFinancialYear(fromDate, toDate)">
				<label for="pfy"><insta:ltext key="registration.datarangeselector.data.previousfinancialyear"/></label>
				<br/>

				<input type="radio" id="tfy" name="_sel" onclick="setDateRangeFinancialYear(fromDate, toDate)">
				<label for="tfy"><insta:ltext key="registration.datarangeselector.data.thisfinancialyear"/></label>
				<br/>
			</c:if>
		</td>

		<td valign="top" style="padding-left: 2em; vertical-align: top">
			<table style="white-space:nowrap">
				<tr>
					<td align="right"><insta:ltext key="registration.datarangeselector.data.from"/>:</td>
					<td><insta:datewidget name="fromDate"/></td>
				</tr>
				<tr>
					<td align="right"><insta:ltext key="registration.datarangeselector.data.to"/>:</td>
					<td><insta:datewidget name="toDate"/></td>
				</tr>
			</table>
		</td>
	</tr>
<c:if test="${param.addTable!='N'}">
</table>
</c:if>
<script>
function setSelDateRange() {
	var formEl = document.inputform._sel;
	var len = formEl.length;
	for (var i=0; i<len; i++) {
		if (formEl[i].checked) {
			formEl[i].onclick();
		}
	}
}
</script>
