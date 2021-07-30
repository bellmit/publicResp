<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@page import="com.insta.hms.master.FixedAssetMaster.FixedAssetMasterDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>New Maint Activity - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="ajax.js"/>
<insta:link type="js" file="maintcont/maintactivity.js"/>


<script>
	var assets = <%=FixedAssetMasterDAO.getTableDataInJSON
		(FixedAssetMasterDAO.GET_ACTIVE_ASSETS, (String)request.getSession().getAttribute("userid"),
				(Integer)request.getSession().getAttribute("roleId"),(String)request.getSession().getAttribute("multiStoreAccess"),
				(String)request.getSession().getAttribute("pharmacyStoreId"))%>;
	var centerId = '${centerId}';
</script>
</head>
<body onload="init();">
<form action="NewMaintActivity.do" method="POST" name="maintactivityform">

<input type="hidden" name="method" value="${operation == 'update' ? 'update' : 'create' }">
<input type="hidden" name="maint_activity_id" value="${ifn:cleanHtmlAttribute(maint_activity_id)}">
<input type="hidden" id="dialogId" value=""/>
<c:set var="maintenance_date" value=""/>
	<h1>Add Maintenance Activity</h1>
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Activity</legend>
	<table class="formtable" align="center" width="100%">
		<tr>
			<td colspan="2">
						<table  width="100%" >
							<tr>
								<td class="formlabel">Asset:</td>
								<td valign="top">
									<div id="item_wrapper" style="width: 23em; padding-bottom:0.2em; ">
			    						<input type="text" name="asset_name" id="asset_name" style="width: 22em" maxlength="100" value="${componentDetials[0].map.asset_str }" tabindex="1"/>
				    					<span class="star">*</span><div id="asset_dropdown"></div>
				     				</div>

				     				<input type="hidden" id="asset_id" name="asset_id" value="${componentDetials[0].map.asset_id }"/>
				     				<input type="hidden" id="batch_no" name="batch_no" value="${componentDetials[0].map.asset_serial_no }"/>

								</td>
								<td class="formlabel">Maintenance By:</td>
								<td><input type="text" name="maint_by" value="${componentDetials[0].map.maint_by}" tabindex="2"/></td>
							</tr>
							<tr>
								<td class="formlabel">Scheduled Date: </td>
								<td>
								<c:set var="sched_date" value=""/>
								<c:set var="schdt" value=""/>
								<c:choose>
									<c:when test="${componentDetials[0].map.scheduled_date eq null || componentDetials[0].map.scheduled_date eq ''}">
										<c:set var="schdt" value="today"/>
									</c:when>
									<c:otherwise>
										<c:set var="sched_date" value="${componentDetials[0].map.scheduled_date}"/>
										<fmt:formatDate value="${sched_date}" pattern="dd-MM-yyyy" var="schdt"/>
									</c:otherwise>
								</c:choose>
								<insta:datewidget id = "scheduled_date" name="scheduled_date" value="${schdt}" valid="future" tabindex="3"/>
								</td>

								<td class="formlabel">Description:</td>
								<td><input type="text" id="maintname="maint_description" value="${componentDetials[0].map.maint_description}" tabindex="4"/></td>
							</tr>
							<tr>
									<td class="formlabel">Completed Date: </td>
								<td><fmt:formatDate value="${componentDetials[0].map.maint_date}" pattern="dd-MM-yyyy" var="maintdt" />
								<insta:datewidget id = "maint_date" name="maint_date" value="${maintdt}" tabindex="5"/></td>



							</tr>

						</table>


			</td>
		</tr>
		</table>
		</fieldset>
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Activity List</legend>
		<div id="dialog" style="visibility:hidden">
				<div class="bd">
					<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Add Activity</legend>
					 <table cellpadding="0" cellspacing="0" width="100%" id="newtable">
						<tr>
							<td>Component</td>
							<td>Description</td>
							<td>Labour Cost</td>
							<td>Material Cost</td>
						</tr>

						<tr>
							<td class="forminfo" style="width:10em">
								<input type="text" name="componentac" id="componentac" tabindex="6" style="width: 16em" tabindex="6"/><span class="star">*</span>
							</td>
							<td class="forminfo" style="width:10em">
								<input type="text" name="descriptionac" id="descriptionac" style="width: 12em" tabindex="7"/>
							<td class="forminfo" style="width:10em">
								<input type="text" name="labourCostac" id="labourCostac" style="width: 12em" tabindex="8" onkeypress="return enterNumAndDot(event);"/></td>
							<td class="forminfo" style="width:10em">
								<input type="text" name="costac" id="costac" style="width: 12em" tabindex="9" onkeypress="return enterNumAndDot(event);"/></td>
						</tr>


						<tr><td>&nbsp;</td></tr>
							<tr>
								<td>
									<button type="button" id="OK" name="OK" accesskey="A"  style="display: inline;" class="button" onclick="addActivity();"tabindex="6"><label><b>OK</b></label></button>
									<button type="button" id="Cancel" name="Cancel" accesskey="A"  style="display: inline;" class="button" onclick="handleCancel();"tabindex="7"><label><b>Cancel</b></label></button>
								</td>
							</tr>
						</table>
					</fieldset>
				</div>
				</div>


			<table class="resultList" width="100%" cellspacing="0" cellpadding="0" id="activityTbl" >
				<tr >
					<th style="width:15em">Component</th>
					<th style="width:30em">Description</th>
					<th style="width:10em;text-align:right;">Labour Cost</th>
					<th style="width:10em;text-align:right;">Material Cost</th>
					<th></th>
					<th></th>
				</tr>
				<c:choose>
		        <c:when test="${not empty componentDetials && not empty componentDetials[0].map.component}">
		        <c:set var="i"/>
				<c:forEach items="${componentDetials}" var="item" varStatus="status">
				<c:set var="i" value="${status.index+1}"/>
				<tr id="tableRow${i}">
					<td style="width:15em;padding-left:0.8em;">
						<div id="componentac${i}">
							<label id="componentLabel${i}">${item.map.component }</label>
							<input type="hidden" name="component" id="component${i}" tabindex="355" value="${item.map.component}"/>
							<input type="hidden" name="item_id" id="item_id${i}" tabindex="356" value="${item.map.item_id}"/>
							<div id="componentcontainer1"></div>
						</div>
					</td>
					<td style="width:30em;">
						<label id="descriptionLabel${i}">${item.map.description }</label><div id="descriptionac1">
							<input type="hidden" name="description" id="description${i}" tabindex="360" value="${item.map.description}" />
							<div id="descriptioncontainer1"></div>
						</div>
					</td>
					<td style="width:10em;text-align:right;"><label id="labourCostLabel${i}">${ifn:afmt(item.map.labor_cost) }</label>
					<input type="hidden" name="labourCost" id="labourCost${i}" onkeypress="return enterNumOnly(event);" value="${item.map.labor_cost}"/></td>
					<td style="width:10em;text-align:right;"><label id="costLabel${i}"><c:out value="${ifn:afmt(item.map.part_cost) }"/></label>
					<input type="hidden" name="cost" id="cost${i}" onkeypress="return enterNumOnly(event);" value="${item.map.part_cost}"  /></td>
					<td style="text-align:right;">
						<label id="itemRow${i}">
						<img class="button" src="../icons/Delete.png" id="imgDelete${i}"	style="cursor:pointer;" onclick="cancelRow(this.id, ${i})"/></label>
						<input type="hidden" name="componentCheckBox" id="componentCheckBox${i}" value="false"/>
					</td>
					<td><button name="addbut" id="addbut${i}" class="imgButton"	onclick="openDialogBox(${i}); return false;"
							accesskey="+" title="Add New Item">
						<img class="button" name="add" id="add${i}" src="../icons/Edit.png"	style="cursor:pointer;" />
						</button>
					</td>
				</tr>


				</c:forEach>
				 <c:set var="i" value="${i+1}"/>
				<tr id="tableRow${i}">
					<td style="width:15em;padding-left:0.8em;">
						<div id="componentac1">
							<label id="componentLabel${i}"></label>
							<input type="hidden" name="component" id="component${i}" tabindex="355" value='' />
							<input type="hidden" name="item_id" id="item_id${i}" tabindex="356" value=""/>
							<div id="componentcontainer1"></div>
						</div>
					</td>
					<td style="width:30em">
						<label id="descriptionLabel${i}"><c:out value=""/></label><div id="descriptionac1">
							<input type="hidden" name="description" id="description${i}" tabindex="360" value='' />
							<div id="descriptioncontainer1"></div>
						</div>
					</td>
					<td style="width:10em;text-align:right;"><label id="labourCostLabel${i}"></label>
					<input type="hidden" name="labourCost" id="labourCost${i}" onkeypress="return enterNumOnly(event);" value=""/></td>
					<td style="width:10em;text-align:right;"><label id="costLabel${i}"></label>
					<input type="hidden" name="cost" id="cost${i}" onkeypress="return enterNumOnly(event);" value=""  /></td>
					<td style="text-align:right;">
						<label id="itemRow${i}"></label>
						<input type="hidden" name="componentCheckBox" id="componentCheckBox${i}"/>
					</td>
					<td><button name="addbut" id="addbut${i}" class="imgButton"	onclick="openDialogBox(${i}); return false;"
							accesskey="+" title="Add New Item">
						<img class="button" name="add" id="add${i}" src="../icons/Add.png"	style="cursor:pointer;" />
						</button>
					</td>
				</tr>
				</c:when>
				<c:otherwise>
				<tr id="tableRow1">
					<td style="width:15em">
						<div id="componentac1">
							<label id="componentLabel1"></label>
							<input type="hidden" name="component" id="component1" tabindex="355" value='' />
							<div id="componentcontainer1"></div>
						</div>
					</td>
					<td style="width:30em">
						<label id="descriptionLabel1"></label><div id="descriptionac1">
							<input type="hidden" name="description" id="description1" tabindex="360" value='' />
							<div id="descriptioncontainer1"></div>
						</div>
					</td>
					<td style="width:10em;text-align:right;"><label id="labourCostLabel1"></label>
					<input type="hidden" name="labourCost" id="labourCost1" onkeypress="return enterNumOnly(event);" value=""/></td>
					<td style="width:10em;text-align:right;"><label id="costLabel1"><c:out value=""/></label>
					<input type="hidden" name="cost" id="cost1" onkeypress="return enterNumOnly(event);" value=""  /></td>
					<td style="text-align:right;">
						<label id="itemRow1"></label>
						<input type="hidden" name="componentCheckBox" id="componentCheckBox1"/>
					</td>
					<td><button name="addbut" id="addbut1" class="imgButton"	onclick="openDialogBox(1); return false;"
							accesskey="+" title="Add New Item">
						<img class="button" name="add" id="add1" src="../icons/Add.png"	style="cursor:pointer;" />
						</button>
					</td>
				</tr>
				</c:otherwise>
				</c:choose>



		</table>
				<c:if test="${empty componentDetials[0].map.maint_date}">
					<button type="submit" name="save" onclick="return validate();" accesskey="S" ><b><u>S</u></b>ave</button>
				</c:if>
				<a href="${cpath }/resourcemanagement/NewMaintActivity.do?method=show">Back To DashBoard</a>


	</form>
	<script type="text/javascript">
		var popurl = '${pageContext.request.contextPath}';
		var maintDate = '${componentDetials[0].map.maint_date}';
	</script>
</body>
</html>
