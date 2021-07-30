<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="org.apache.struts.Globals"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<html>
<c:set var="allowDecimalsForQty" value="<%= GenericPreferencesDAO.getGenericPreferences().getAllowdecimalsforqty()%>" />
<head>

<c:set var="issuetodeptonly"
	value='<%= GenericPreferencesDAO.getAllPrefs().get("issue_to_dept_only") %>'
	scope="request" />

<c:if test="${issuetodeptonly == 'N'}">
	<title><insta:ltext key="salesissues.userreturns.itemlist.userreturns.instahms"/></title>
</c:if>
<c:if test="${issuetodeptonly == 'Y'}">
	<title><insta:ltext key="salesissues.userreturns.itemlist.deptreturns.instahms"/></title>
</c:if>


<style>
	table.detailFormTable { font-family:Verdana,Arial,sans-serif; font-size:9pt; border-collapse: collapse;}
	table.detailFormTable td.label { padding: 0px 2px 0px 2px; overflow: hidden; }
	tr.deleted {background-color: #F2DCDC; color: gray; }

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
</style>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
<script type="text/javascript">
	var storeID = '${ifn:cleanJavaScript(store_id)}';
	var showCharges = null;
	var allowDecimalsForQty = '${allowDecimalsForQty}';
	var issuetodept = '${issuetodeptonly}';
</script>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/storescommon.js" />
	<insta:link type="script" file="stores/stockuserreturns.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
	<insta:js-bundle prefix="sales.issues"/>
	<insta:js-bundle prefix="sales.issues.userreturns"/>

</head>
<c:set var="searchText">
<insta:ltext key="salesissues.userreturns.itemlist.searchText"/>
</c:set>

<body onload="init();getReport('${ifn:cleanJavaScript(message)}','Hospital');enable('hosp_user');checkstoreallocation();" class="yui-skin-sam">

<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<div id="storecheck" style="display: block;" >
<form action="StockUserReturn.do" name="UserReturnsForm" onsubmit="return formSubmit();" method="POST" autocomplete="off">
<input type="hidden" name="_method" id="_method" value="show"/>
<input type="hidden" id="refresh" name="refresh" value="false"/>
<input type="hidden" id="dialogId" value=""/>
<input type="hidden" name="is_user_returns" value="true"/>
<input type="hidden" name="return_from"/>

<c:if test="${issuetodeptonly == 'N'}">
<h1><insta:ltext key="salesissues.userreturns.itemlist.userreturns"/></h1>
</c:if>
<c:if test="${issuetodeptonly == 'Y'}">
<h1><insta:ltext key="salesissues.userreturns.itemlist.deptreturns"/></h1>
</c:if>


		<div>
				<c:choose>
				<c:when test="${message == 0 }">
				<span class="resultMessage"></span>
				</c:when>
		    	<c:when test="${ message != 0}" >
				<span class="resultMessage"><insta:ltext key="salesissues.userreturns.itemlist.itemsreturned"/></span>
				</c:when>
				<c:when test="${message == null }" >
				<span class="resultMessage"><insta:ltext key="salesissues.userreturns.itemlist.transactionfailure"/></span>
				</c:when>
				<c:when test="${message == null }" >
				<span class="resultMessage"><insta:ltext key="salesissues.userreturns.itemlist.transactionfailure"/></span>
				</c:when >
			</c:choose>
	    </div></br>
<c:if test="${issuetodeptonly == 'N'}">
<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.userreturns.itemlist.userissuedetails"/></legend>
</c:if>
<c:if test="${issuetodeptonly == 'Y'}">
<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.userreturns.itemlist.deptissuedetails"/></legend>
</c:if>

				<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
				
				<tr id="userRow">
				<c:choose>
						<c:when test="${issuetodeptonly == 'N'}">
						<td >
							<input type="radio" accesskey="U" name="issueType" id="issueType_user"
							value="u" onclick="onChangeIssueType(this),refreshForm(),resetStore(),resetIssueIds(),resetIssueDates()" checked='checked'/>
							<label for="issueType_user">
							<insta:ltext key="salesissues.userreturns.itemlist.issue.to"/> <b><u><insta:ltext key="salesissues.userreturns.itemlist.u"/></u></b><insta:ltext key="salesissues.userreturns.itemlist.ser"/></label>:
						</td>
						<td class="yui-skin-sam" valign="top">
							<div  id="hosp_user_wrapper">
								<div id="psAutocomplete" style="display: block; float: left; width: 210px">
									<input type="text" name="hosp_user" id="hosp_user" style="width: 200px"/>
									<div id="hosp_user_dropdown" class="scrollingDropDown" style="width: 250px;"></div>
								</div>
							</div>
							<span id="hosp_user_mand" style="display:block;padding-left:210px;" class="star">*</span>
						</td>
						</c:when>
						<c:otherwise>
							<td >
								<input type="radio" name="issueType" accesskey="D" id="issueType_dept"
								value="d" onclick="onChangeIssueType(this),refreshForm(),resetStore(),resetIssueIds(),resetIssueDates()" checked='checked'/>
								<label for="issueType_dept"><insta:ltext key="salesissues.userreturns.itemlist.issue.to"/> <b><u><insta:ltext key="salesissues.userreturns.itemlist.d"/></u></b><insta:ltext key="salesissues.userreturns.itemlist.dept"/></label>:
							</td>
							<td>
								<insta:selectdb id="issue_dept" name="issue_dept" table="department" displaycol="dept_name"
									valuecol="dept_name" value="${hosp_user}" dummyvalue="${dummyvalue}"  
									style="color:black;" maxlength="25"/>
									<span id="issue_dept_mand" style="visibility:visible;" class="star">*</span>
							</td>
						</c:otherwise>
						</c:choose>
						<td class="formlabel"><insta:ltext key="salesissues.userreturns.itemlist.store"/>:</td>
                        <c:choose>
							<c:when test="${(roleId == 1) || (roleId == 2) || (multiStoreAccess == 'A') }">
								<td>
								<insta:userstores username="${userid}" elename="store" onchange="onChangeStore(this.value,'Hospital');" id="store" val="${defStoreVal}"/>
								</td>
							</c:when>
							<c:otherwise>
								<td>
									<b>${ifn:cleanHtmlAttribute(store_name)}</b>
									<input type = "hidden" name="store" id="store" value="${ifn:cleanHtmlAttribute(store_id)}" />
								</td>
							</c:otherwise>
						</c:choose>
						<c:if test="${issuetodeptonly == 'N'}">
						<tr>
							<td >
								<input type="radio" name="issueType" accesskey="D" id="issueType_dept"
								value="d" onclick="onChangeIssueType(this),refreshForm(),resetStore(),resetIssueIds(),resetIssueDates()"/>
								<label for="issueType_dept"><insta:ltext key="salesissues.userreturns.itemlist.issue.to"/> <b><u><insta:ltext key="salesissues.userreturns.itemlist.d"/></u></b><insta:ltext key="salesissues.userreturns.itemlist.dept"/></label>:
							</td>
							<td>
								<insta:selectdb id="issue_dept" name="issue_dept" table="department" displaycol="dept_name"
									valuecol="dept_name" value="${hosp_user}" dummyvalue="${dummyvalue}" disabled="disabled" 
									style="color:black;" maxlength="25"/>
									<span id="issue_dept_mand" style="visibility:hidden" class="star">*</span>
							</td>
							<td class="formlabel"><insta:ltext key="salesissues.userreturns.itemlist.reason"/>:</td>
							<td>
								<input type="text" name="reason" id="reason" maxlength="30"  onblur="upperCase(reason)">
								<input type="hidden" name="inventory" id="inventory" value="issue"/>
							</td>
						</tr>
						</c:if>
						<tr>
						<td>
							<input type="radio" name="issueType" accesskey="W" id="issueType_ward"
							value="w" onclick="onChangeIssueType(this),refreshForm(),resetStore(),resetIssueIds(),resetIssueDates()"/>
							<label for="issueType_ward"><insta:ltext key="salesissues.userreturns.itemlist.issue.to"/> <b><u><insta:ltext key="salesissues.userreturns.itemlist.w"/></u></b><insta:ltext key="salesissues.userreturns.itemlist.ard"/></label>:
						</td>
						<td>
							<insta:selectdb id="issue_ward" name="issue_ward" table="ward_names" displaycol="ward_name" disabled="disabled"
								valuecol="ward_name" value="" dummyvalue="--Select--"  style="color:black;" maxlength="25"/>
								<span id="issue_ward_mand" style="visibility:hidden" class="star">*</span>
						</td>
						<c:choose>
							<c:when test="${(roleId == 1) || (roleId == 2) || (actionRightsMap.allow_backdate == 'A')}">
								<td class="formlabel"><insta:ltext key="salesissues.stockuserissue.details.date"/></td>
								<td><insta:datewidget name="issueDate" valid="past" value="today" btnPos="left" /></td>
							</c:when>
							<c:otherwise>
								<td></td>
							</c:otherwise>
						</c:choose>
						
					<tr>
						<td></td>
						<td></td>
						<td></td>
						<td><input type="button" name="search" id="search" value="${searchText}" onclick="return searchIssues();"/></td>
					</tr>
				</table>
			</fieldset>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.userreturns.itemlist.issueid"/>:</td>
					<td><select name="user_issue_no" id="user_issue_no" onchange="getStockIssue(this,user_issue_date,'issue_no');" class="dropdown">
						<option value=""><insta:ltext key="salesissues.userreturns.itemlist.select"/></option>
						</select><span class="star">*</span>
					</td>
					<td class="formlabel"><insta:ltext key="salesissues.userreturns.itemlist.issuedate"/>:</td>
					<td><select name="user_issue_date" id="user_issue_date" onchange="getStockIssue(user_issue_no,this,'issue_date');" class="dropdown">
						<option value=""><insta:ltext key="salesissues.userreturns.itemlist.select"/></option>
						</select>
					</td>
				</tr>
			</table>
			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.userreturns.itemlist.itemlist"/></legend>
			<table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="itemListtable" border="0">
				<tr >
					<th><insta:ltext key="salesissues.userreturns.itemlist.return"/></th>
					<th><insta:ltext key="salesissues.userreturns.itemlist.itemname"/></th>
					<th><insta:ltext key="salesissues.userreturns.itemlist.batch.or.serial.no"/></th>
					<th><insta:ltext key="salesissues.userreturns.itemlist.exp.date"/></th>
					<th><insta:ltext key="salesissues.userreturns.itemlist.issueqty"/></th>
					<th><insta:ltext key="salesissues.userreturns.itemlist.pkgsize"/></th>
				</tr>
			 </table>
		</fieldset>
		<div id="dialog1" style="visibility:hidden">
			<div class="bd">
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="salesissues.userreturns.itemlist.remaining"/>&nbsp;<insta:ltext key="salesissues.userreturns.itemlist.quantity"/>: </td>
						<td class="forminfo">
							<b><label id="remainingQty" /></b>
							<input type="hidden" name="unit_mrp" id="unit_mrp" value=""/>
						</td>
					</tr>
			     	<tr>
			     		<td class="formlabel"><insta:ltext key="salesissues.userreturns.itemlist.returnquantity"/>:</td>
						<td ><input type="text" name="return_qty" id="return_qty" size="4" onkeypress="return enterNumOnlyANDdot(event)"/></td>
						<td>
							<select name="item_unit" id="item_unit" style="width:11em;"></select>
						</td>
					</tr>
					<tr>
						<td colspan="2">
							<button type="button" id="OK" name="OK" accesskey="A"  style="display: inline;" class="button" onclick="updateReturnQty()";><insta:ltext key="salesissues.userreturns.itemlist.ok"/></label></button>
							<button type="button" id="Cancel" name="Cancel" accesskey="A"  style="display: inline;" class="button" onclick="handleCancel()";><insta:ltext key="salesissues.userreturns.itemlist.cancel"/></button>
						</td>
					</tr>
				</table>
			</div>
		</div>
		<table id="returneditems"></table>
		<table><tr><td align="center">
				<div id="creditbill" style="display: none;">
					<label >Bill Later</label>
					<select name="bill_no" id="bill_no" class="dropdown">
					<option value=${activeCreditBillNo}>${activeCreditBillNo}</option>
					</select>
				</div></td></tr>
			</table>
		<div class="screenActions">
			<button type="button" name="return" id="return" accesskey="R" onclick="return submitReturnForm(this);"><b><u><insta:ltext key="salesissues.userreturns.itemlist.r"/></u></b><insta:ltext key="salesissues.userreturns.itemlist.eturn"/></button>
			<button type="button" name="refresh" id="refresh" onclick="refreshForm(),resetStore(),resetIssueIds(),resetUser(),resetIssueDates()" accesskey="S"><insta:ltext key="salesissues.userreturns.itemlist.re"/><b><u><insta:ltext key="salesissues.userreturns.itemlist.s"/></u></b><insta:ltext key="salesissues.userreturns.itemlist.et"/></button>
		</div>

</form>
</div>
<script type="text/javascript">
var user_type = '${user_type}';
var user = '${ifn:cleanJavaScript(user)}';
var store = '${ifn:cleanJavaScript(store)}';
 var hospuserlist = ${hospuserlist};
var disable = '${disable}';
var enabled = '${enabled}';
var cpath = '${pageContext.request.contextPath}';
var billstatus = '${billStatus}';
var deptId = '${ifn:cleanJavaScript(store_id)}';
var gRoleId = '${roleId}';
var returnissueType = '${param.issueType == null || param.issueType eq '' ? issuetodeptonly eq "N" ? "u" : "d" : param.issueType}';
</script>
</body>
</html>