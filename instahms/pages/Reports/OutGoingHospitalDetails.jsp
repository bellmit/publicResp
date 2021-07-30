<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>
<c:set var = "contextPath" value="${pageContext.request.contextPath}"/>

<html>
	<head>
	<insta:link type="script" file="widgets.jsp"/>
	<insta:link type="css" file="widgets.css"/>
		<title>Out Going Test Details - Hospital wise</title>

		<script type="text/javascript">
		var fromDate,toDate;
		var hospId = "";
		function onInit() {
			fromDate = document.inputform.fromDate;
			toDate = document.inputform.toDate;
			document.getElementById("pd").checked = true;
			setDateRangeYesterday(fromDate, toDate);
			document.inputform.all.checked = true;
			selectOutGoingHospitals();
		}

		function validateOutGoingHospNames(){
			var temp="";
			var hospSelected = false;
			var len = document.forms[0].hospIdArray.length;
			var options = document.forms[0].hospIdArray;

			for (var i=0;i<len;i++){
				if (options[i].selected == true){
					hospSelected=true;
				}
			}

			if(!hospSelected){
				alert("Select atleast one hospital name for report");
				return false;
			}
			return true;
		}

		function selectOutGoingHospitals(){
			var disable = document.forms[0].all.checked;
			var outGoingHospLen = document.forms[0].hospIdArray.length;
			for (i=outGoingHospLen-1;i>=0;i--){
				document.forms[0].hospIdArray[i].selected = disable;
			}
		}

		function deselectAll(){
			document.forms[0].all.checked = false;
		}


		function getTestStatus(){
			if(!validateOutGoingHospNames()){
				return false;
			}
			if(!validateCategory())
				return false;

			if(!validateTestStatus())
				return false;
		}

		function validateTestStatus(){
			if(!document.getElementById("testStatusArray_").checked &&
				!document.getElementById("testStatusArray_C").checked &&
				!document.getElementById("testStatusArray_N").checked &&
				!document.getElementById("testStatusArray_P").checked &&
				!document.getElementById("testStatusArray_X").checked &&
				!document.getElementById("testStatusArray_U").checked &&
				!document.getElementById("testStatusArray_V").checked &&
				!document.getElementById("testStatusArray_S").checked &&
				!document.getElementById("testStatusArray_RP").checked &&
				!document.getElementById("testStatusArray_RC").checked &&
				!document.getElementById("testStatusArray_RV").checked &&
				!document.getElementById("testStatusArray_NRN").checked &&
				!document.getElementById("testStatusArray_MA").checked &&
				!document.getElementById("testStatusArray_TS").checked &&
				!document.getElementById("testStatusArray_CC").checked &&
  				!document.getElementById("testStatusArray_CR").checked &&
				!document.getElementById("testStatusArray_CRN").checked) {
					alert("Please select test status for the report");
					return false;
			}
			return true;
		}
		function validateCategory(){
			var form = document.inputform;
			if (!document.getElementById("categoryArray_").checked &&
				!document.getElementById("categoryArray_DEP_LAB").checked  &&
				!document.getElementById("categoryArray_DEP_RAD").checked){
				alert("Please select test category for the report");
				return false;
			}
			return true;
		}

		</script>
	</head>
	<body onload="onInit();">
		<div class="pageHeader">Out Going Test Details - Hospital wise</div>

		<form method="GET" name="inputform" target="_blank">
			<input type="hidden" name="method" value="getReport">
				<div class="tipText">
						This report shows out going test details - hospital wise within given dates.
						The report displays Date,Patient Name,Test Name,Test Status,Amount.
				</div>
				<table align="center" >
					<tr>
						<td colspan="3">
							<jsp:include page="/pages/Common/DateRangeSelector.jsp">
								<jsp:param name="skipWeek" value="Y"/>
							</jsp:include>
						</td>
					</tr>
				</table>
				<div class="stwMain">
					<table class="search" width="100%" align="center">
						<tr>
								<th style="width: 40%;" >Hospital Name</th>
								<th style="width: 15%;">Test Category</th>
								<th style="width: 15%;">Test Status</th>
								<c:if test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}"><th style="width: 30%;">Center</th></c:if>
						</tr>
						<tr>
							<td>
								<table>
									<tr>
										<td> <input type="checkbox" name="all" onclick="selectOutGoingHospitals();">All
										</td>
										<td>
											<insta:selectdb style="width: 300px;height: 200px;"  name="hospIdArray"  size = "5" onclick="deselectAll()"
												multiple= "true" value=""	table="diag_outsource_view" valuecol="oh_id"
												displaycol="oh_name" filtered="false" />
										</td>
									</tr>
								</table>
							</td>
							<td>
								<table>
									<tr>
										<td><insta:checkgroup name="categoryArray" selValues="${paramValues.categoryArray}"
											opvalues="DEP_LAB,DEP_RAD" optexts="Laboratory,Radiology" />
										</td>
									</tr>
								</table>
							</td>
							<td>
								<table>
									<tr>
										<td>
											<insta:checkgroup name="testStatusArray" selValues="${paramValues.testStatusArray}"
											opvalues="N,P,NRN,C,CRN,V,S,RP,RC,RV,X,MA,TS,CC,CR,U" optexts="New (Results),In Progress,New (No Results),Conducted (Results),Conducted (No Results),Validated,Signed-off,Revision in Progress,Revision Completed,Revision Validated,Cancel,Patient Arrived,Scheduled for Transcriptionist,Conduction Completed,Change Required,Condn. Unnecessary" />
										</td>
									</tr>
								</table>
							</td>
							<c:choose>
								<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
								<td>
									<table>
									<tr>
										<td align="left"> <br />
											<insta:selectdb style="width: 100%;" name="center" id="center" table="hospital_center_master" valuecol="center_id" displaycol="center_name" value="${centerId}"/>
										</td>
									</tr>
									</table>
								</td>
								</c:when>
								<c:otherwise>
										<input type="hidden"  name="center"  id="center" value="${centerId}"/>
								</c:otherwise>

							</c:choose>
						</tr>
					</table>
				</div>
				<table align="center">
					<tr>
						<td>
							<button type="submit" accesskey="G" onclick="return getTestStatus();">
							<b><u>G</u></b>enerate Report</button>
						</td>
					</tr>

				</table>
		</form>
	</body>
</html>
