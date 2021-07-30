<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page import="com.insta.hms.adminmasters.bedmaster.BedMasterDAO"%>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>
<html>
	<head>
		<title>IP Outstanding Detailed Report Insta HMS </title>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
		<insta:link type="script" file="hmsvalidation.js"/>
		<script>
			var centerId = ${centerId};
			function init(){
				document.getElementById('pharAmtTxt').value="";
				document.getElementById('hospAmtTxt').value="";
				document.getElementById('totAmtTxt').value="";
			}
			function onSubmit(){
					var displayName="";
					for(i =0; i<document.forms[0].tpaName.length;i++){
						if(document.forms[0].tpaName.options[i].selected){
							displayName = document.forms[0].tpaName.options[i].text;
						}
					}

					var centerDisplayName="";
					for(i =0; i<document.forms[0].centerFilter.length;i++){
						if(document.forms[0].centerFilter.options[i].selected){
							centerDisplayName = document.forms[0].centerFilter.options[i].text;
						}
					}
					document.getElementById("centerName").value = centerDisplayName;
					setCenterClause();
					setAmtDueClause();
					displayName= displayName=='(All)'?"":displayName;
 					document.getElementById("tpaDisplayName").value = displayName;
 					if(document.forms[0].printerType.value=='csv'){
						document.forms[0].action = "IPOutstandCSVReport.do";
						document.forms[0].method.value="getCsv";
					}else{
						document.forms[0].action = "IPOutstandReport.do";
						document.forms[0].method.value="getReport";
					}
				return true;
			}
			function getWard(){
				var ward = document.getElementById("ward_no");

			}

			function setCenterClause() {
				var selectedCenter = document.getElementById("centerFilter").value;
				if ( selectedCenter == 0 ) {
					document.getElementById("centerClause").value = ( " AND cen_id = "+selectedCenter );
				} else {
					document.getElementById("centerClause").value = ( " AND cen_id = "+selectedCenter );
				}
			}
			function setAmtDueClause() {
				var pharAmtObj = document.getElementById("pharAmtTxt");
				if(pharAmtObj && pharAmtObj.value !='') {
					document.getElementById("pharmacyAmtDueClause").value = ( " AND (pharmacy_amt-pharmacy_advance) > "+pharAmtObj.value );
				} else {
					document.getElementById("pharmacyAmtDueClause").value ="";
				}
				
				var hospAmtObj = document.getElementById("hospAmtTxt");
				if(hospAmtObj && hospAmtObj.value != '') {
					document.getElementById("hospitalAmtDueClause").value = ( " AND (total_amt-pharmacy_amt-hospital_advance) > "+hospAmtObj.value );
				} else {
					document.getElementById("hospitalAmtDueClause").value ="";
				}
				
				var totAmtObj = document.getElementById("totAmtTxt");
				if(totAmtObj && totAmtObj.value != '') {
					document.getElementById("totalAmtDueClause").value = ( " AND (total_amt-pharmacy_advance-hospital_advance-deposit_set_off) > "+totAmtObj.value );
				}else {
					document.getElementById("totalAmtDueClause").value ="";
				}
			}
		</script>
	</head>
	<%
		int centerId = (Integer)request.getSession().getAttribute("centerId");
		boolean multiCentered = GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1;

		request.setAttribute("wards",BedMasterDAO.getAllWardNames( centerId,multiCentered ));
	%>
	<body onload="init()">
		<form method="GET" action="IPOutstandReport.do" target="_blank">
			<input type="hidden" name="report_name" value="IPOutstandingDetailedReport"/>
			<input type="hidden" name="method"  value="getReport" />
			<input type="hidden" name="tpaDisplayName" id="tpaDisplayName" value="" />
			<input type="hidden" name="centerName" id="centerName" value="" />
			<input type="hidden" name="center_id" id="center_id" value="${centerId }"/>
			<input type="hidden" name="centerClause" id="centerClause" value=""/>
			<div class="pageHeader"> IP Outstanding Detailed Report</div>
			<div class="tipText">
					This report generates patient details with bill (amount , advance , due ) of hospital and
					pharmacy group by ward .
			</div>
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Patient Status </div>
						<div class="sfField">
							 <insta:checkgroup name="visitStatusArray" opvalues="A,I" optexts="Active,Inactive"
							 selValues="${paramValues.visit_status}" />
						</div>
					</td>
					<td>
								<div class="sfLabel">Insurance</div>
								<div class="sfField">
									<insta:checkgroup name="insuranceArray" opvalues="A,I" optexts="Insured,Non-Insured"
									selValues="${paramValues.insuranceArray}"/>
								</div>
					</td>
					<td>
								<div class="sfLabel">Ward Name</div>
								<div class="sfField">
									<select name="wardName" id="wardName" class="dropdown">
										<option value="">(All)</option>
										<c:forEach items="${wards }" var="ward">
											<option value="${ward.map.ward_no }">${ward.map.ward_name }</option>
										</c:forEach>
									</select>
								</div>
								<div class="sfLabel">TPA Name</div>
								<div class="sfField">
									<insta:selectdb name="tpaName" value="" table="tpa_master"
										valuecol="tpa_id" displaycol="tpa_name" dummyvalue="(All)"/>
								</div>
								<c:choose>
									<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
										<div class="sfLabel">Center Name</div>
										<div class="sfField">
												<insta:selectdb name="centerFilter" id="centerFilter" table="hospital_center_master"
												valuecol="center_id" displaycol="center_name" value="${centerId}" />
										</div>
									</c:when>
									<c:otherwise>
											<input type="hidden"  name="centerFilter" id="centerFilter"  value="${centerId}"/>
									</c:otherwise>
								</c:choose>
					</td>
					<td class="last"align="left" style="width:1px">
						<input type="hidden" name="pharmacyAmtDueClause" id="pharmacyAmtDueClause" value=""/>
						<input type="hidden" name="hospitalAmtDueClause" id="hospitalAmtDueClause" value=""/>
						<input type="hidden" name="totalAmtDueClause" id="totalAmtDueClause" value=""/>
						<table width="80%">
							<tr>
								<td class="last" style="width:75%;text-align:right">
									<br/>
									<div class="sfLabel" style="border-bottom:medium none;"  title="Pharmacy Amount greater than">
									Pharmacy Amt. Due Greater Than :
									</div>
								</td>
								<td  class="last" style="text-align:left;padding-left:0;">
									<br/>
									<div class="sfLabel"  style="border-bottom:medium none;" title="Pharmacy Amount greater than">
									 <input type="text" value="" class="number"  name="pharAmtTxt" id="pharAmtTxt" maxlength="15" onkeypress="return enterNumOnlyANDdot(event);">
									</div>
								</td>
							</tr>
							<tr>
								<td class="last" style="text-align:right">
									<br/>
									<div class="sfLabel"  style="border-bottom:medium none;" title="Hospital Amount greater than">
									Hospital Amt. Due Greater Than :
									</div>
								</td>
								<td  class="last" style="text-align:left;padding-left:0;">
									<br/>
									<div class="sfLabel" style="border-bottom:medium none;"  title="Hospital Amount greater than">
									 <input type="text" id="hospAmtTxt" name="hospAmtTxt" class="number" value="" maxlength="15" onkeypress="return enterNumOnlyANDdot(event);">
									</div>
								</td>
							</tr>
							<tr>
								<td class="last" style="text-align:right;">
									<br/>
									<div class="sfLabel" style="border-bottom:medium none;" title="Total Amount (i.e. Hospital+Pharmacy) greater than">
									Total Amt. Due Greater Than :
									</div>
								</td>
								<td  class="last" style="text-align:left;padding-left:0;">
									<br/>
									<div class="sfLabel" style="border-bottom:medium none;" title="Total Amount (i.e. Hospital+Pharmacy) greater than">
									<input type="text"  class="number" style="width:60px;" id="totAmtTxt" name="totAmtTxt" value="" maxlength="15" onkeypress="return enterNumOnlyANDdot(event);">
									</div>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text,csv" optexts="PDF,TEXT,CSV" style="width: 5em" />
					</td>

					<td>
							<button type="submit" accesskey="G" onclick="return onSubmit();"><b><u>G</u></b>enerate Report</button>
					</td>
					</tr>
				</table>
		</form>
	</body>
</html>
