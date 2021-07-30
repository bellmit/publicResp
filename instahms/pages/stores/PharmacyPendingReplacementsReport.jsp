<%@page import="com.bob.hms.common.RequestContext"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.stores.PharmacymasterDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>Pharmacy Pending Replacement Report - Insta HMS</title>

</head>

<html>
	<body>
		<div class="pageHeader">Pharmacy Pending Replacement Report</div>
		<form name="inputform" method="GET" target="_blank" >
			<input type="hidden" name="report" value="PharmacyPendingReplacementsReport">
			<input type="hidden" name="method" value="getReport" />

			<div class="tipText">
				This report lists supplier and invoice wise pending replacement.

			</div>

			<table align="center">
				<tr>
					<td style="padding-left: 4px">Supplier Name:</td>
					<td>
						<select name="supplier_id">
							<option value="*">(All)</option>
							<c:forEach var="supplier" items="<%= PharmacymasterDAO.getAllCenterSuppliers() %>">
								<option value="${supplier.SUPPLIER_CODE}">${supplier.SUPPLIER_NAME}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
			</table>



			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
					</td>

					<td>
						<button type="submit" accesskey="G"><b><u>G</u></b>enerate Report</button>
					</td>
			</table>

		</form>
	</body>
</html>

