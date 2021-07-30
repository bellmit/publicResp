<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Dynamic Package</title>
	<insta:link type="js" file="dynaPkgExprCommon.js" />
	<insta:link type="js" file="master/DynaPackage/DynaPackage.js" />
	<insta:link type="js" file="masters/editCharges.js"/>
	<insta:link type="js" file="masters/charges_common.js" />
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style>
		#dynaPackageCharges td {
			width: 120px;
			white-space: wrap;
			padding: 5px 5px 5px 10px;
		}
		.dataTable td {
			white-space: normal;
			padding: 0px;
		}
	</style>
	
</head>

<body onload="fillAllValuesForAdd(); fillRatePlanDetails('dynapackages','dyna_package_id','${bean.map.dyna_package_id}');itemsubgroupinit();">
	<form method="POST" action="DynaPackage.do" name="dynaPackageForm" >
		<c:if test="${param._method == 'add'}"><h1>Add New Package</h1></c:if>
		<c:if test="${param._method == 'show'}"><h1>Edit Package</h1></c:if>
		<input type="hidden" name="dyna_package_id" id="dyna_package_id" value="${bean.map.dyna_package_id}">
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">

	<insta:feedback-panel/>

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Dynamic Package Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Package Name:</td>
					<td>
						<input type="text" name="dyna_package_name" class="field"
							value="${bean.map.dyna_package_name}" id="dyna_package_name" maxlength="100">
					</td>
					<td class="formlabel">Status:</td>
					<td>
						<insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I"
							optexts="Active,Inactive"/>
					</td>
					<td></td>
					<td></td>
				</tr>
			</table>
		</fieldset>

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Rate Details</legend>
			<table class="formtable">
				<tr >
					<td class="formlabel" >Rate Sheet:</td>
					<td >
						<c:choose>
							<c:when test="${param._method eq 'show'}">
								<insta:selectdb name="org_id" value="${bean.map.org_id}"
									table="organization_details" valuecol="org_id" orderby="org_name"
									displaycol="org_name" onchange="getChargesForNewRatePlan();"
									filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y" />
							</c:when>
							<c:otherwise>
								<label class="forminfo">GENERAL</label>
								<input type="hidden" name="org_id" value="ORG0001">
							</c:otherwise>
						</c:choose>
					</td>
					<td class="formlabel">Rate Plan Code:</td>
					<td><input type="text" name="item_code" maxlength="20" value="${bean.map.item_code}"/></td>
					<!-- <td class="formlabel">Excluded Amount Claimable: </td>
					<td><Select name="excluded_amt_claimable" class= "dropdown" >
							<option value="Y" ${bean.map.excluded_amt_claimable == 'Y' ? 'selected' : ''}>Yes</option>
							<option value="N" ${bean.map.excluded_amt_claimable == 'N' ? 'selected' : ''}>No</option>
						</Select>
					</td>
					<td></td> -->
				</tr>
			</table>
		</fieldset>

		<div class="resultList">
			<c:set var="catCount" value="1"/>
			<table class="formtable dataTable" cellspacing="0" cellpadding="0" border="1">
				<tr>
					<th style="min-width: 100px; max-width: 100px;" align="left">Categories</th>
						<c:forEach var="bed" items="${bedTypes}" varStatus="loop">
							<c:if test="${loop.count % 7 eq 0}">
								<c:set var="catCount" value="${ catCount + 1}"/>
								<th style="min-width: 100px; max-width: 100px;" align="left">Categories</th>
							</c:if>
							<th style="min-width: 100px; max-width: 100px" align="left"><insta:truncLabel value="${bed}" length="15"/></th>
							<input type="hidden" name="bed_type" value="<c:out value='${bed}'/>"/>								
						</c:forEach>
					</tr>
					<tr>
						<td colspan="${fn:length (bedTypes) + catCount} " style="padding: 0px">
							<div style="width: 100%; overflow: auto; height: 500px"   >
								<table cellspacing="0" cellpadding="0" border="0" style="width: 100%" id="dynaPackageCharges">
									<tr style="display: none"></tr>
									<tr>
										<td>Charge </td>
											<c:forEach var="bed" items="${bedTypes}" varStatus="k">
												<c:set var="i" value="${k.index}"/>
												<c:if test="${k.count % 7 eq 0}">
													<td>Charge </td>
												</c:if>
												<td>
													<input type="text" name="charge" id="charge${i}" class="number validate-positive-decimal"
														value="${ifn:afmt(empty categories ? (charges[bed]['0'].map.charge) : (charges[bed][categories[0].dyna_pkg_cat_id].map.charge))}">
												</td>
											</c:forEach>
											<input type="hidden" name="ids" value="${i+1}">
									</tr>

									<c:forEach var="cat" items="${categories}">
										<input type="hidden" name="dyna_pkg_cat_id" value="${cat.dyna_pkg_cat_id}"/>
										<c:set var="firstBedType" value="GENERAL"/>

										<tr id="${cat.dyna_pkg_cat_id}">
											<c:choose>
												<c:when test="${param._method == 'add'}">
													<td>
														<insta:truncLabel value="${cat.dyna_pkg_cat_name} (Included)" length="15"/>
													</td>
													<%-- <td>${cat.dyna_pkg_cat_name} (Included)</td> --%>
												</c:when>
												<c:otherwise>
													<td>
														<c:set var="t1" scope="page">
															${charges[firstBedType][cat.dyna_pkg_cat_id].map.dyna_pkg_cat_name} (Included ${(charges[firstBedType][cat.dyna_pkg_cat_id].map.limit_type eq 'U') ? '<br/> - Unlimited)' : ')'}	
														</c:set>
														
														<insta:truncLabel value="${t1}" length="15"/>
													
													</td>
													
												</c:otherwise>
											</c:choose>

											<c:forEach var="bed" items="${bedTypes}" varStatus="k">
												<c:set var="i" value="${k.index}"/>
												<c:if test="${k.count % 7 eq 0}">
													<c:choose>
														<c:when test="${param._method == 'add'}">
															<td>
																<insta:truncLabel length="15" value="${cat.dyna_pkg_cat_name} (Included)"/>
															</td>
														</c:when>
														<c:otherwise>
															<td>
																<c:set var="t2">
																		${charges[firstBedType][cat.dyna_pkg_cat_id].map.dyna_pkg_cat_name} (Included ${(charges[firstBedType][cat.dyna_pkg_cat_id].map.limit_type eq 'U') ? '<br/> - Unlimited)' : ')'}	
																</c:set>
																<insta:truncLabel length="15" value="${t2}"/>															
															</td>
														</c:otherwise>
													</c:choose>
												</c:if>
												<td>
													<select name="${cat.dyna_pkg_cat_id}.pkg_included" id="${cat.dyna_pkg_cat_id}.pkg_included${i}"
														class="dropdown" style="width:62px;" onchange="enableDisableFields(this, '${i}', '${cat.dyna_pkg_cat_id}');">
														<option value="Y" ${charges[bed][cat.dyna_pkg_cat_id].map.pkg_included eq 'Y' ? 'selected' : '' }>Yes</option>
														<option value="N" ${charges[bed][cat.dyna_pkg_cat_id].map.pkg_included eq 'N' ? 'selected' : '' }>No</option>
													</select>
													<input type="hidden" id="${cat.dyna_pkg_cat_id}limitType${i}"
														value="${param._method == 'add' ? cat.limit_type : charges[bed][cat.dyna_pkg_cat_id].map.limit_type}" >
												</td>
											</c:forEach>
									</tr>

									<tr>
										<c:choose>
											<c:when test="${param._method == 'add'}">
												<td>
													<insta:truncLabel length="15" value="${cat.dyna_pkg_cat_name}(Amt Limit)"/>
												</td>
											</c:when>
											<c:otherwise>
												<td>
													<insta:truncLabel length="15" value="${charges[firstBedType][cat.dyna_pkg_cat_id].map.dyna_pkg_cat_name} (Amt Limit)"/>
												</td>
											</c:otherwise>
										</c:choose>

										<c:forEach var="bed" items="${bedTypes}" varStatus="k">
											<c:set var="i" value="${k.index}"/>
											<c:if test="${k.count % 7 eq 0}">
												<c:choose>
													<c:when test="${param._method == 'add'}">
														<td>
															<insta:truncLabel length="15" value="${cat.dyna_pkg_cat_name}(Amt Limit)"/>
														</td>
													</c:when>
													<c:otherwise>
														<td>
															<insta:truncLabel length="15" value="${charges[firstBedType][cat.dyna_pkg_cat_id].map.dyna_pkg_cat_name} (Amt Limit)"/>
														</td>
													</c:otherwise>
												</c:choose>
											</c:if>
											<td>
												<input type="text" name="${cat.dyna_pkg_cat_id}.amount_limit" id="${cat.dyna_pkg_cat_id}.amount_limit${i}" class="number validate-positive-decimal"
													value="${ifn:afmt(charges[bed][cat.dyna_pkg_cat_id].map.amount_limit)}"
													${(charges[bed][cat.dyna_pkg_cat_id].map.pkg_included eq 'Y' && charges[bed][cat.dyna_pkg_cat_id].map.limit_type eq 'A') ? '' : 'readOnly' }>
											</td>
										</c:forEach>
								</tr>

								<tr>
									<c:choose>
										<c:when test="${param._method == 'add'}">
											<td>
												<insta:truncLabel length="15" value="${cat.dyna_pkg_cat_name}(Qty Limit)"/>
											</td>
										</c:when>
										<c:otherwise>
											<td>
												<insta:truncLabel length="15" value="${charges[firstBedType][cat.dyna_pkg_cat_id].map.dyna_pkg_cat_name} (Qty Limit)"/>
											</td>
										</c:otherwise>
									</c:choose>

									<c:forEach var="bed" items="${bedTypes}" varStatus="k">
										<c:set var="i" value="${k.index}"/>
										<c:if test="${k.count % 7 eq 0}">
											<c:choose>
										<c:when test="${param._method == 'add'}">
											<td>
												<insta:truncLabel length="15" value="${cat.dyna_pkg_cat_name}(Qty Limit)"/>
											</td>
										</c:when>
										<c:otherwise>
											<td>
												<insta:truncLabel length="15" value="${charges[firstBedType][cat.dyna_pkg_cat_id].map.dyna_pkg_cat_name} (Qty Limit)"/>
											</td>
										</c:otherwise>
									</c:choose>
										</c:if>
										<td>
											<input type="text" name="${cat.dyna_pkg_cat_id}.qty_limit" id="${cat.dyna_pkg_cat_id}.qty_limit${i}" class="number validate-positive-decimal"
												 value="${ifn:afmt(charges[bed][cat.dyna_pkg_cat_id].map.qty_limit)}"
												 ${(charges[bed][cat.dyna_pkg_cat_id].map.pkg_included eq 'Y' && charges[bed][cat.dyna_pkg_cat_id].map.limit_type eq 'Q') ? '' : 'readOnly' }>
										</td>
									</c:forEach>
								</tr>
								<tr id="audit_limit_log_row">
									<td>&nbsp;</td>
										<c:forEach var="bed" items="${bedTypes}" varStatus="k">
										<c:if test="${k.count % 7 eq 0}">
											<td></td>
										</c:if>
											<td style="overflow: hidden">
												<insta:screenlink screenId="dyna_package_category_limits_audit_log"
												extraParam="?_method=getAuditLogDetails&dyna_package_id=${bean.map.dyna_package_id}&dyna_pkg_cat_id=${cat.dyna_pkg_cat_id}&bed_type=${bed}&org_id=${bean.map.org_id}&dyna_package_name=${ifn:encodeUriComponent(bean.map.dyna_package_name)}&al_table=dyna_package_category_limits_audit_log_view" label="Li Audit Log" title="Limit Audit Log" />
											</td>
										</c:forEach>
								</tr>
						</c:forEach>
							<tr id="audit_log_row">
								<td>&nbsp;</td>
									<c:forEach var="bed" items="${bedTypes}" varStatus="k">
										<c:if test="${k.count % 7 eq 0}">
											<td></td>
										</c:if>
										<td style="overflow: hidden">
											<insta:screenlink screenId="dyna_package_charges_audit_log"
												extraParam="?_method=getAuditLogDetails&dyna_package_id=${bean.map.dyna_package_id}&bed_type=${bed}&org_id=${bean.map.org_id}&dyna_package_name=${ifn:encodeUriComponent(bean.map.dyna_package_name)}&al_table=dyna_package_charges_audit_log_view" label="Ch Audit Log" title="Charge Audit Log"/>
										</td>
									</c:forEach>
							</tr>
							<tr>
								<c:if test="${not empty bedTypes}">
								   <td style="text-align: right">Apply Charges To All</td>
								   <td><input type="checkbox" name="checkbox" onclick="fillAllValues('dynaPackageCharges', this);"/></td>
								   <c:forEach begin="2" end="${fn:length (bedTypes)}">
								     <td>&nbsp;</td>
								   </c:forEach>
								</c:if>
							</tr>
						</table>
					</div>
				</td>
			</tr>
		</table> 
	</div>
		<div id="ratePlanDiv" style="display:none">
			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Rate Plan List</legend>
				<table class="dashBoard" id="ratePlanTbl">
					<tr class="header">
						<td>Include</td>
						<td>Rate Plan</td>
						<td>Discount / Markup</td>
						<td>Variation %</td>
						<td>&nbsp;</td>
					</tr>
					<tr id="" style="display: none">
				</table>
				<table class="screenActions" width="100%">
					<tr>
						<td align="right">
							<img src='${cpath}/images/blue_flag.gif'>Overridden
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
<!-- <insta:taxations/> -->

		<c:url var="url" value="DynaPackage.do">
			<c:param name="_method" value="list"/>
			<c:param name="sortReverse" value="false"/>
			<c:param name="status" value="A"/>
		</c:url>
		<table class="screenActions">
			<tr>
				<td>
					<button type="button" name="Save" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button> |
					<a href="<c:out value='${url}'/>">Dynamic Package List</a>
				</td>
			</tr>
		</table>
	</form>
	<script>
		var derivedRatePlanDetails = ${derivedRatePlanDetails};
		var itemGroupList = ${itemGroupListJson};
		var itemSubGroupList = ${itemSubGroupListJson};
	</script>
</body>

</html>
