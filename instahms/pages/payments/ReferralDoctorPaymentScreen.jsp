<%@page import="org.apache.struts.Globals"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
	<head>
		<title>Referral Payments - Insta HMS</title>
		<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">

		<insta:link type="js" file="hmsvalidation.js"/>
		<insta:link type="js" file="ajax.js" />
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="js" file="widgets.js"/>
		<insta:link type="js" file="dashboardsearch.js"/>
		<insta:link type="script" file="dashboardColors.js"/>
		<insta:link type="script" file="/payments/referraldoctorpayments.js"/>

		<c:set var="cpath" value="${pageContext.request.contextPath}"/>
			<style>
				#dialog1_mask.mask {
				z-index: 1;
				display:none;
				position:absolute;
				top:0;
				left:0;
				-moz-opacity: 0.0001;
				opacity:0.0001;
				filter: alpha(opacity=50);
				background-color:#CCC;
				}
				.scrollcontainer .yui-ac-content{
				max-height:11em;overflow:auto;overflow-x:hdden;
				_height:11em;
				}
				.insured{background-color: #DDDA8A}
				.doctorPayMore{background-color: #E9CFEC;}
		</style>
	</head>
	<c:set var="method_name" value= "getReferralDoctorPaymentScreen"/>
	<c:set var="charge" value="${chargeList.dtoList}"/>
	<c:set var="doctorpayment" value="${paymentChargeList.dtoList}"/>
	<body onload="init()" class="yui-skin-sam">
		<div class="pageHeader">Referral Doctor Payments</div>
		<form method="GET" action="${cpath}/pages/payments/ReferralDoctorPayments.do"
			name="RefDoctorPaymentSearchForm">
			<input type="hidden" name="_method" value="getReferralDoctorSearch" id="method"/>
			<input type="hidden" name="_searchMethod" value="getReferralDoctorSearch" id="method"/>
			<c:set var="hasResult" value="${(not empty charge) || (not empty doctorpayment)}"/>
			<div>${ifn:cleanHtml(msg)}</div>
			<div id="dialog1" style="visibility:hidden">
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Edit Doctor Fees</legend>
					<input type="hidden" id="editRowId" value=""/>
					<table class="formtable">
						<tr>
							<td class="formlabel">Doctor Fees :</td>
							<td><input type="text" name="_dialog_doctorFees" size="5" class="number"
									id="_dialog_doctorFees" onkeypress="return enterNumOnly(event);" onblur="updateStatus();"/>
							</td>
						</tr>
						<tr>
							<td class="formlabel">TPA Name :</td>
							<td class="forminfo" ><label id="tpaname"></label></td>
						<tr>
						<tr>
							<td class="formlabel">Bill Amount :</td>
							<td class="forminfo"><label id="billAmt"></label></td>
						<tr>
						<tr>
							<td class="formlabel">Claim Amount :</td>
							<td class="forminfo"><label id="claimAmt"></label></td>
						<tr>
						<tr>
							<td class="formlabel">TPA Received Status :</td>
							<td class="forminfo"><label id="tapRecStatus"></td>
						<tr>
						<tr>
							<td class="formlabel">Received Amount :</td>
							<td class="forminfo"><label id="receAmt"></label></td>
						<tr>
					</table>
					<table style="margin-top: 10px;">
						<tr>
							<td><input type="button" id="editOk" name="editOk" value="Ok" /></td>
							<td><input type="button" id="editDialogPrevious" name="editDialogPrevious" value="<<Previous"/></td>
							<td><input type="button" id="editDialogNext" name="editDialogNext" value="Next>>"/></td>
							<td><input type="button" id="editDialogCancel" name="editDialogCancel" value="Close" /></td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
		<insta:search form="RefDoctorPaymentSearchForm" optionsId="optionalFilter" closed="${hasResult}"
		validateFunction="getDoctorCharges()">
		<div class="searchBasicOpts">
		  <div class="sboField">
			  <div class="sboFieldLabel">Referral Doctor:</div>
			  <div class="sboFieldInput">
			 		<div id="refdoctor" >
						<input type="text" name="_doctorName" id="refDoctors" value="${ifn:cleanHtmlAttribute(param._doctorName)}"/>
						<div id="refDoctorList" style="width:30em"></div>
					</div>
				</div>
			</div>
			<input type="hidden" name="reference_docto_id" id="doctor" value="${ifn:cleanHtmlAttribute(param.reference_docto_id)}"/>
		</div>	<!-- end basic-->
		<div id="optionalFilter" style="clear: both; display : ${hasResult ? 'none' : 'block'}">
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Patient Name/MR No:</div>
						<div class="sfField">
							<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
							<input type="hidden" name="mr_no@op" value="like"/>
							<div id="mrnoContainer"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Charge Posted Date:</div>
							<div class="sfField">
								<div class="sfFieldSub">From:</div>
									<insta:datewidget name="posted_date" id="posted_date0"
										value="${paramValues.posted_date[0]}"/>
							</div>
							<div class="sfField">
								<div class="sfFieldSub">To:</div>
									<insta:datewidget name="posted_date" id="posted_date1"
										value="${paramValues.posted_date[1]}"/>
									<input type="hidden" name="posted_date@op" value="ge,le"/>
									<input type="hidden" name="posted_date@type" value="date"/>
									<input type="hidden" name="posted_date@cast" value="y"/>
							</div>
							<div class="sfLabel">Bill Status</div>
								<div class="sfField">
									<insta:checkgroup name="bill_status" opvalues="F,C" optexts="Finalized,Closed"
									selValues="${paramValues.bill_status}"/>
								</div>
					</td>
					<td>
				  	<div class="sfLabel">Patient Type:</div>
						<div class="sfField">
								<insta:checkgroup name="visit_type" opvalues="i,o" optexts="IP,OP"
									selValues="${paramValues.visit_type}"/>
						</div>
						<div class="sfLabel">Insurance:</div>
						<div class="sfField">
							<insta:checkgroup name="insurancestatus" opvalues="Y,N"
								selValues="${paramValues.insurancestatus}"  optexts="Insurance,Non Insurance" />
						</div>
					</td>
					<td>
						<div class="sfLabel">Charge Group:</div>
							<div class="sfField" style="white-space: nowrap">
									<c:forEach var="group" items="${chargeGroups}">
											<c:set var="groupId" value="${groupId},${group.map.chargegroup_id}"/>
											<c:set var="groupName" value="${groupName},${group.map.chargegroup_name}"/>
									</c:forEach>
									<insta:checkgroup name="charge_group" opvalues="${groupId}"
										optexts="${groupName}" 	selValues="${paramValues.charge_group}"/>
							</div>
					</td>
					<td class="last">
						<div class="sfLabel">Activity:</div>
							<div class="sfField">
								<insta:selectdb name="chargehead_id" table="chargehead_constants" dummyvalue="... Select ..."
								valuecol="chargehead_id" displaycol="chargehead_name" value="${paramValues.chargehead_id}" filtered="false" onchange="getCharges(this.value);"/>
								</div>
							</div>
							<div class="sfLabel">Description:</div>
							<div class="sfField">
								<div id="chargesDiv" class="autoComplete">
									<input type="text" name="act_description" id ="chargeHeads"
									value="${ifn:cleanHtmlAttribute(param.act_description)}"/>
									<input type="hidden" name="act_description@op" value="ico"/>
									<div id="chargeHeadList" style="width: 25em" ></div>
								</div>
							</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>
</form>
<insta:paginate curPage="${chargeList.pageNumber}" numPages="${chargeList.numPages}" pageNumParam="_chargePageNum" totalRecords="${chargeList.totalRecords}"/>
<form name="referralDoctorPaymentForm" action="${cpath}/pages/payments/ReferralDoctorPayments.do"
	method="POST">
	<input type="hidden" name="_method" value="createPaymentDetails"	id="method"/>
	<input type="hidden" name="_refPayment" id="refPayment" value="${refPayment}" />
	<input type="hidden" name="_noOfCharges" id="noOfCharges" value="0"/>
	<input type="hidden" name="_deleteRows" id="deleteRows" value="0" />
	<input type="hidden" name="_doctorName" id="doctorName" />
	<input type="hidden" name="_allCharges" id="allCharges" />
	<input type="hidden" name="_activityId" id="activityId" value="" />
	<input type="hidden" name="_screen" value="${ifn:cleanHtmlAttribute(screen)}"/>
	<input type="hidden" name="reference_docto_id" value="${ifn:cleanHtmlAttribute(param.reference_docto_id)}"/>
	<input type="hidden" name="_chargeheadName" value="${ifn:cleanHtmlAttribute(param._chargeName)}"/>
	<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
	<input type="hidden" name="_referal_amount" value="${ifn:cleanHtmlAttribute(param.referal_amount)}"/>
	<input type="hidden" name="referal_amount@op" value="gt"/>
	<input type="hidden" name="referal_amount@type" value="integer"/>
	<div id="payment_content" class="resultList">
			<table class="resultList" width="100%" cellspacing="0" cellpadding="0" id="payResultTable" align="center" onmouseover="hideToolBar();" >
				<tr onmouseover="hideToolBar()">
					<th>Select</th>
					<th>MR No</th>
					<th>Bill No</th>
					<th>Patient Name</th>
					<th>Date</th>
					<th>Charge Head</th>
					<th>Desc</th>
					<th class="number">Billed Amt</th>
					<th class="number">TPA Amt</th>
					<th class="number">Discount</th>
					<th class="number">Doctor Fees</th>
					<th></th>
				</tr>
				<c:forEach items="${charge}" var="dc" varStatus="st">
						<c:set var="i" value="${st.index+1}"/>
						<c:set var="refDocAmount" value="${(dc.amount) lt ((dc.prescribing_dr_amount)+(dc.referal_amount)+(dc.doctor_amount))}"/>

				<c:choose>
						<c:when test="${refDocAmount == true && not empty dc.primary_sponsor_id}">
							<c:set var="flagColor" value="red"/>
						</c:when>
						<c:when test="${(dc.amount) lt ((dc.prescribing_dr_amount)+(dc.referal_amount)+(dc.doctor_amount))}">
							<c:set var="flagColor" value="red"/>
							<c:set var="title">
								Doctor Amount= ${dc.doctor_amount}	Referal Amount = ${dc.referal_amount}	Prescribing Amount = ${dc.prescribing_dr_amount}
							</c:set>
						</c:when>
						<c:when test="${not empty dc.primary_sponsor_id}">
							<c:set var="flagColor" value="blue"/>
						</c:when>
						<c:otherwise>
							<c:set var="flagColor" value="empty"/>
						</c:otherwise>
				</c:choose>
				<tr class="${st.index == 0 ? 'firstRow': ''} ${st.index % 2 == 0 ? 'even': 'odd'}"
					onclick="showToolbar(${st.index}, event, 'payResultTable', {billNo:'${dc.bill_no}'})"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td>
						<input type="checkbox" name="_statusCheck" id="statusCheck${i}" value="${i}" onclick="editDoctorFees(this,${i})" >
						<input type="hidden" name="_packageCharge" value="${dc.package_charge}"/>
				  	</td>
					<td>${dc.mr_no}
						<input type="hidden" name="_mrNo" value="${dc.mr_no}" />
					</td>
					<td>
						<div style="width: 15px; float: left"><img src="${cpath}/images/${flagColor}_flag.gif"/></div>
						<label name="bill" title="${dc.billtype}/${dc.billstatus}/${dc.visittype}">${dc.bill_no}</label>
						<input type="hidden" name="_billNo" id="billNo${i}" value="${dc.bill_no}" size="7" readonly>
						<input type="hidden" name="_chargeId" id="chargeId${i}" value="${dc.charge_id}" >
					</td>
					<td>
						${dc.patient_name}&nbsp;${dc.last_name}
					</td>
					<td><fmt:formatDate value="${dc.finalized_date}" pattern="dd-MM-yyyy"/>
						<input type="hidden" name="_postedDate"  value="${dc.finalized_date}" />
					</td>
					<td><insta:truncLabel value="${dc.chargehead_name}" length="15"/>
						<input type="hidden" name="_chargeHeadName"  value="${dc.chargehead_name}" />
					</td>
					<td>
						<insta:truncLabel value="${dc.act_description}" length="15"/>
						<input type="hidden" name="_actDescription" value="${dc.act_description}" />
					</td>
					<td class="number">${dc.amount}
						<input type="hidden" name="_amount" value="${dc.amount}"/>
					</td>
					<td class="number" >${dc.claim_recd_amount}</td>
						<c:set var="title" value=""/>
						<c:if test="${dc.overall_discount_auth != 0 && dc.overall_discount_amt > 0}">
						<c:set var="title" >
						Overall Discount :${dc.overall_discount_auth_name}--${dc.overall_discount_amt}
						</c:set>
						</c:if>
						<c:if test="${dc.discount_auth_dr != null && dc.dr_discount_amt > 0}">
						<c:set var="title">
						${title}
						Doctor Discount :${dc.discount_auth_dr_name}--${dc.dr_discount_amt}
						</c:set>
						</c:if>
						<c:if test="${dc.discount_auth_ref != null && dc.ref_discount_amt > 0}">
						<c:set var="title">
						${title}
						Referral Doctor : ${dc.discount_auth_ref_name}---
						${dc.ref_discount_amt}
						</c:set>
						</c:if>
						<c:if test="${dc.discount_auth_pres_dr != null && dc.pres_dr_discount_amt > 0}">
						<c:set var="title">
						${title}
						Prescribing Doctor : ${dc.discount_auth_pres_dr_name}--
						${dc.pres_dr_discount_amt}
						</c:set>
						</c:if>
						<c:if test="${dc.discount_auth_hosp != null && dc.hosp_discount_amt > 0}">
						<c:set var="title">
						${title}
						Hospital User: ${dc.overall_discount_auth_name} --
						${dc.hosp_discount_amt} </c:set>
						</c:if>
						<td class="number">
							<label title="${title}">${dc.discount}</label>
						</td>
						<td class="number">
							<label id="doctorFee">${dc.referal_amount}</label>
							<input type="hidden" name="_doctorFees" value="${dc.referal_amount}"	id="doctorFees${i}" />
							<input type="hidden" id="originalDoctorFees${i}" value="${dc.referal_amount}"/>
							<input type="hidden" name="_billAmount" id="billAmount${i}" value="${dc.amount}"/>
							<input type="hidden" name="_centerId" value="${dc.center_id}" />
						</td>
						<td>
							<a name="_editAnchor" id="editAnchor${i}" href="javascript:Edit" onclick="return showEditChargeDialog(this);"
								title="Edit Item Details">
								<img src="${cpath}/icons/Edit.png" class="button noToolbar" />
							</a>
							<input type="hidden" name="_billTotalAmount" id="billTotalAmount${i}" value="${dc.total_amount}"/>
							<input type="hidden" name="_actualClaimAmount" id="actualClaimAmount${i}" value="${dc.actual_claim_amt}"/>
							<input type="hidden" name="_claimStatus" id="claimStatus${i}" value="${dc.primary_claim_status}"/>
							<input type="hidden" name="_claimReceivedAmount" id="claimReceivedAmount${i}" value="${dc.claim_recd_amount}"/>
							<input type="hidden" name="_tpaName" id="tpaName${i}" value="${dc.tpa_name}"/>
					  	</td>
				  </tr>
			</c:forEach>
		</table>
		<c:if test="${param._method != 'getReferralDoctorPaymentScreen'}">
				<insta:noresults hasResults="${not empty charge}"/>
		</c:if>
		<c:if test="${not empty charge}">
		<table width="100%" align="center" style="padding-top:10">
			<tr>
				<td>
					<div style="float: left">
						<input type="radio" name="_selectItems" id="singleItem" value="item" checked="true"
						onclick="onCheckRadio(this.value)">Select Single Item
					</div>
					<div style="float: left">
						<input type="radio" name="_selectItems" id="pgItems" value="pageItems"
						onclick="onCheckRadio(this.value)">Select Page Items
					</div>
					<div style="float: left;">
						<input type="radio" name="_selectItems" id="allItems" value="all"  onclick="onCheckRadio(this.value);">Select All Items
					</div>
					<div style="float: right;">Items Total:
						<label id="allTotAmt" style="font-weight: bold;">0</label>
					</div>
				</td>
			</tr>
		</table>
		<div class="screenActions">
			<input type="button" name="save" value="Save" onclick="onPostPayments();"/>
		</div>
		</c:if>
	</div>
<dl class="accordion">
	<dt style="margin-top:10px">
		<span class="clrboth">View Posted Payments</span>
	</dt>
	<dd class="${not empty doctorpayment ? 'open' : ''}" style="width:952px">
		<div class="bd">
			<insta:paginate curPage="${paymentChargeList.pageNumber}" numPages="${paymentChargeList.numPages}"
			pageNumParam="_paymentPageNum" totalRecords="${paymentChargeList.totalRecords}"/>
			<div id="viewDiv" class="resultList">
					<table class="resultList" cellpadding="0" cellspacing="0" width="100%" id="paidResultTable"
						onmouseover="hideToolBar('');">
						<tr onmouseover="hideToolBar('')";>
								<th><input type="checkbox" name="deleteAll" onclick="deleteAllCharges()"/>Delete</th>
								<th>MR No</th>
								<th>Bill No</th>
								<th>Patient Name</th>
								<th>Date</th>
								<th>Charge Head</th>
								<th>Desc</th>
								<th class="number">Billed Amount</th>
								<th class="number">Discount</th>
								<th class="number">Referral Doctor Fees</th>
						</tr>
						<c:forEach items="${doctorpayment}" var="dp" varStatus="st">
							<c:set var="j" value="${st.index+1}"/>
							<c:set var="dRefDocAmt" value="${(dp.amount) lt ((dp.prescribing_dr_amount)+(dp.referal_amount)+(dp.doctor_amount))}"/>
						<c:choose>
								<c:when test="${dRefDocAmt == true && not empty dp.primary_sponsor_id}"	>
										<c:set var="flagColor" value="red"/>
								</c:when>
								<c:when test="${(dp.amount) lt ((dp.prescribing_dr_amount)+(dp.referal_amount)+(dp.doctor_amount))}">
										<c:set var="flagColor" value="red"/>
										<c:set var="title">
											Doctor Amount= ${dp.doctor_amount}	Referal Amount = ${dp.referal_amount}	Prescribing Amount = ${dp.prescribing_dr_amount}
										</c:set>
								</c:when>
						 		<c:when test="${not empty dp.primary_sponsor_id}">
										<c:set var="flagColor" value="blue"/>
								</c:when>
								<c:otherwise>
										<c:set var="flagColor" value="empty"/>
								</c:otherwise>
						</c:choose>
						<tr class="${st.index == 0 ? 'firstRow': ''}${st.index % 2 == 0 ? 'even': 'odd'}"
								onclick="showToolbar(${st.index+10}${j}, event, 'paidResultTable', {billNo:'${dp.bill_no}'})"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index+10}${j}" >
								<td>
										<input type="checkbox" name="deleteCharge" id="deleteCharge${j}" value="${j}"
											onclick="deleteDoctorCharge(this,${j});"/>
										<input type="hidden" name="_delpackageCharge" value="${dp.package_charge}"/>
								</td>
								<td>
										${dp.mr_no}
										<input type="hidden" name="_delmrNo"  value="${dp.mr_no}" />
								</td>
								<td>
										<div  style="width: 15px; float: left"><img src="${cpath}/images/${flagColor}_flag.gif"/></div>
										<label name="bill" title="${dp.billtype}/${dp.billstatus}/${dp.visittype}">
											${dp.bill_no}
										</label>
										<input type="hidden" name="_delbillNo" value="${dp.bill_no}" />
										<input type="hidden" name="_delchargeId" value="${dp.charge_id}"/>
										<input type="hidden" name="_delPaymentId" value="${dp.ref_payment_id}"/>
								</td>
								<td>
										${dp.patient_name}&nbsp;${dp.last_name}
								</td>
								<td>
										<fmt:formatDate value="${dp.finalized_date}" pattern="dd-MM-yyyy"/>
										<input type="hidden" name="_delpostedDate" value="${dp.finalized_date}" />
								</td>
								<td>
									<insta:truncLabel value="${dp.chargehead_name}" length="15"/>
										<input type="hidden" name="_delchargeHeadName" value="${dp.chargehead_name}" />
								</td>
								<td>
									<insta:truncLabel value="${dp.act_description}" length="15"/>
									<input type="hidden" name="_actDescription" value="${dp.act_description}" />
								</td>
								<td class="number">${dp.amount}
										<input type="hidden" name="_delamount" value="${dp.amount}" />
								</td>
											<c:set var="title" value=""/>
											<c:if test="${dp.overall_discount_auth != 0 && dp.overall_discount_amt > 0}">
													<c:set var="title" >
														Overall Discount :${dp.overall_discount_auth_name}--${dp.overall_discount_amt}
													</c:set>
											</c:if>
											<c:if test="${dp.discount_auth_dr != null && dp.dr_discount_amt > 0}">
													<c:set var="title">
														${title}Doctor Discount :${dp.discount_auth_dr_name}--${dp.dr_discount_amt}
													</c:set>
											</c:if>
											<c:if test="${dp.discount_auth_ref != null && dp.ref_discount_amt > 0}">
														<c:set var="title">
															${title}	Referral Doctor : ${dp.discount_auth_ref_name}--- ${dp.ref_discount_amt}
														</c:set>
											</c:if>
											<c:if test="${dp.discount_auth_pres_dr != null && dp.pres_dr_discount_amt > 0}">
														<c:set var="title">
															${title} Prescribing Doctor : ${dp.discount_auth_pres_dr_name}-- 	${dp.pres_dr_discount_amt}
													</c:set>
											</c:if>
											<c:if test="${dp.discount_auth_hosp != null && dp.hosp_discount_amt > 0}">
														<c:set var="title">
															${title} Hospital User: ${dp.overall_discount_auth_name} --	${dp.hosp_discount_amt}
														</c:set>
											</c:if>
								<td class="number">
										<label title="${title}">${dp.discount}</label>
								</td>
								<td class="number">${dp.referal_amount}
										<input type="hidden" name="_deldoctorFees" value="${dp.referal_amount}" />
								</td>
						</tr>
						</c:forEach>
					</table>
					<c:if test="${param._method !='getReferralDoctorPaymentScreen'}">
							<insta:noresults hasResults="${not empty doctorpayment}"/>
					</c:if>
					<c:if test="${not empty doctorpayment}">
					<table align="right" style="padding-top : 10">
						<tr>
							<td>Total Amount:&nbsp;</td>
							<td>
								<label id="_paidAmount" style="font-weight: bold;">${refPaidAmount}</label>
							</td>
						</tr>
					</table>
					<div class="screenActions" style="float:left">
						<input type="button" name="delete" value="Delete" onclick="return deleteCharges()"/>
					</div>
					</c:if>
				</div>
			</div>
		</dd>
	</dl>
</form>
<div class="legend" style="display:${not empty doctorpayment or not empty charge?'block':'none' }">
	<div class="flag"><img src='${cpath}/images/blue_flag.gif'/></div>
	<div class="flagText">Insured</div>
	<div class="flag"><img src='${cpath}/images/red_flag.gif'/></div>
	<div class="flagText">Total doctor amount exceeds billed amount</div>
</div>
<script>
		var refDoctorList = ${doctorlist};
		var cpath = '${cpath}';
		var totalRecords = '${chargeList.totalRecords}';
		var totalAmount = '${chargeAmount}';
		var paidAmount = '${refPaidAmount}';
</script>
</body>
</html>
