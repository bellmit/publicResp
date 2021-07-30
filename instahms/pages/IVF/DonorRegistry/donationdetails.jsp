<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Patient Donation Details - Insta HMS</title>
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="ivf/donationdetails.js" />
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<body onload="init();" class="yui-skin-sam">
	<div class="pageHeader">Patient Donation Details</div>
	<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true"/>
	<form name="patientDonation" method="post" action="${cpath}/IVF/DonationDetails.do">
	<input type="hidden" name="donor_mr_no" id="donor_mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>

		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Donation Details</legend>
			<table class="detailList" id="donationTbl"  width="100%">
				<tr class="header">
					<th align="left">Recipient MR No</th>
					<th align="left">Date of Donation</th>
					<th align="left">Donation Type</th>
					<th align="left">Donation Status</th>
					<th align="left">Remarks</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
				<c:forEach items="${donationdetails}" var="donation" varStatus="st">
					<tr id="row${st.index}">
						<input type="hidden" name="recipient_mr_no" id="" value="${donation.map.recipient_mr_no}"/>
						<fmt:formatDate var="donation_dt" pattern="dd-MM-yyyy" value="${donation.map.donation_date}"/>
						<input type="hidden" name="donation_date" id="" value="${donation_dt}"/>
						<input type="hidden" name="donation_type" id="" value="${donation.map.donation_type}"/>
						<input type="hidden" name="donation_status" id="" value="${donation.map.donation_status}"/>
						<input type="hidden" name="remarks" id="" value="${donation.map.remarks}"/>
						<input type="hidden" name="selectedrow" id="selectedrow${st.index+1}" value="false"/>
						<input type="hidden" name="added" id="added${st.index+1}" value="N"/>
						<td>${donation.map.recipient_mr_no}</td>
						<td>${donation_dt}</td>
						<td>
							<c:if test="${donation.map.donation_type == 'S'}">Sperm</c:if>
							<c:if test="${donation.map.donation_type == 'E'}">Egg</c:if>
							<c:if test="${donation.map.donation_type == 'W'}">Womb</c:if>
						</td>
						<td>
							<c:if test="${donation.map.donation_status == 'S'}">Success</c:if>
							<c:if test="${donation.map.donation_status == 'F'}">Failure</c:if>
							<c:if test="${donation.map.donation_status == 'D'}">Dont Know</c:if>
						</td>
						<td>${donation.map.remarks}</td>
						<td><img src="${cpath}/icons/Delete.png" onclick="changeElsColor(${st.index+1}, this);"/></td>
						<td><img src="${cpath}/icons/Edit.png" onclick="onEdit(this)"/></td>
					</tr>
					<c:set var="newIndexFORdummyRow" value="${st.index+1}"/>
				</c:forEach>
					<tr id="" style="display: none">
						<input type="hidden" name="recipient_mr_no" id="" value=""/>
						<input type="hidden" name="donation_date" id="" value=""/>
						<input type="hidden" name="donation_type" id="" value=""/>
						<input type="hidden" name="donation_status" id="" value=""/>
						<input type="hidden" name="remarks" id="" value=""/>
						<input type="hidden" name="selectedrow" id="selectedrow0" value="false"/>
						<input type="hidden" name="added" id="added${st.index+1}" value="N"/>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td><img src="${cpath}/icons/Delete.png" onclick="changeElsColor('${newIndexFORdummyRow}', this);"/></td>
						<td><img src="${cpath}/icons/Edit.png" onclick="onEdit(this)" /></td>
					</tr>
					<tr>
						<td colspan="6"></td>
						<td>
							<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="showDialog(this)" >
								<img src="${cpath}/icons/Add.png" align="right"/>
							</button>
						</td>
					</tr>
			</table>
		</fieldset>
		<div class="screenActions">
		<input type="submit" value="Save" class="button" onclick="return funSubmit();" />
		| <a href="${cpath}/IVF/DonorRegistry.do?_method=list">Patient Donor Registry</a>
		</div>
	</form>
	<div name="donationDIV" id="donationDIV" style="visibility: none">
			<div class="bd">
				<fieldSet class="fieldSetBorder">
					<table class="formTable">
						<tr>
							<td>Recipient MR No:</td>
							<td>
								<input type="text" name="recipientMRNo" id="recipientMRNo"/>
							</td>
						</tr>
						<tr>
							<td>Date of Donation:</td>
							<td><insta:datewidget id = "donationDate" name="donationDate"/></td>
						</tr>
						<tr>
							<td>Donation Type:</td>
							<td><insta:selectoptions name="donationType" id="donationType" value="${param.donationType}" opvalues="S,E,W" optexts="Sperm,Egg,Womb" /></td>
						</tr>
						<tr>
							<td>Donation Status:</td>
							<td><insta:selectoptions name="donationStatus" id="donationStatus" value="${param.donationStatus}" opvalues="S,F,D" optexts="Success,Failure,Dont know" /></td>
						</tr>
						<tr>
							<td>Remarks:</td>
							<td><input type="text" name="donationRemarks" id="donationRemarks"/></td>
						</tr>
					</table>
				</fieldSet>
				<div>
					<input type="button" value="Add" onclick="addToTable()"> |
					<input type="button" value="Cancel" onclick="handleCancel()">
				</div>
			</div>
		</div>
	</body>
</html>