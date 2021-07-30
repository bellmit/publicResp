<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Referral Payment - Insta HMS</title>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>
	<script>
		function deleteSelected(e) {
			var deleteEl = document.getElementsByName("deleteCharge");
			for (var i=0; i< deleteEl.length; i++) {
				if (deleteEl[i].checked) {
					return true;
				}
			}
			alert("select at least one item to delete");
			YAHOO.util.Event.stopEvent(e);
			return false;
		}

		function getCheckedStatus(i){
		if(document.getElementById("deleteCharge"+i).checked) {
				 document.getElementById("checkedStatus"+i).value = "Y";
			}
		}

	</script>
</head>
<body>
	<form method="POST" action="SplReferralPayPreferences.do">
		<input type="hidden" name="method" value="deleteSplRefPayments"/>
		<div class="pageHeader">Special Referral Payment Preferences </div>
			<table class="formtable" align="center">
							<tr><td align="center"><insta:feedback-panel/></td></tr>
			</table>
			<table class="dashboard" align="center" border="1">

					<tr>
						<th>Referrer Name</th>
						<th>Charge Group</th>
						<th>Percent / Amount</th>
						<th>Amount</th>
						<th>Delete</th>
					</tr>
					<c:forEach var="ref" items="${splRefCharge.dtoList}" varStatus="stat">
						<c:set var="i" value="${stat.index}"/>
					<tr>
						<c:url var="Eurl" value="SplReferralPayPreferences.do">
							<c:param name="method" value="show"/>
							<c:param name="referrer_id" value="${ref.map.referrer_id}"/>
							<c:param name="chargegroup_id" value="${ref.map.chargegroup_id}"/>
							<c:param name="chargegroup_name" value="${ref.map.chargegroup_name}"/>
							<c:param name="referrer_name" value="${ref.map.referrername}"/>
							<c:param name="referrertype" value="${ref.map.referrer_type}"/>
							</c:url>
						<td><a href="${Eurl}">${ref.map.referrername}</a></td>
						<td>${ref.map.chargegroup_name}</td>
						<td>${ref.map.payment_percent == 'Y'?'Percent':'Amount'}</td>
						<td>${ref.map.amount}</td>
						<td><input type="checkbox" name="deleteCharge" id="deleteCharge${i}"
							value="${ref.map.referrer_id}" />
							<input type="hidden" name="chargegrpid" id="chargegrpid${i}" value="${ref.map.chargegroup_id}"/>
							<input type="hidden" name="refId" id="refId" value="${ref.map.referrer_id}"/>
						</td>
					</tr>
					</c:forEach>
					<tr>
						<td align="right" colspan="5">
							<input type="submit" name="delete" value="Delete" onclick="deleteSelected(event)"/></td>
			</tr>
		</table>
		<div>

					<tr>
						<td>
							<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
						</td>
					</tr>
		</div>
		<div>
			<c:url var="Eurl" value="SplReferralPayPreferences.do">
			<c:param name="method" value="add"/>
			</c:url>
			<table align="center">
				<tr>
					<td><a href="${Eurl}">Add</a></td>
				</tr>

				</table>
		</div>
</form>
</body>
</html>
