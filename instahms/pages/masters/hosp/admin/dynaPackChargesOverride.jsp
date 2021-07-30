<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Rate Plan Overrides - Insta HMS</title>
	<insta:link type="js" file="dynaPkgExprCommon.js" />
	<insta:link type="js" file="master/DynaPackage/DynaPackage.js" />
	<insta:link type="js" file="masters/charges_common.js" />
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	
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

<body onload="fillAllValuesForAdd();">
	<form method="POST" action="${cpath}/pages/masters/ratePlan.do" name="dynaPackageForm" >
		<h1>Rate Plan Overrides - ${bean.map.org_name}</h1>
		<input type="hidden" name="dyna_package_id" id="dyna_package_id" value="${bean.map.dyna_package_id}">
		<input type="hidden" name="_method" value="overRideDynaPackCharges">
		<input type="hidden" name="chargeCategory" value="dynapackages"/>
		<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>

		<input type="hidden" name="fromItemMaster" value="${ifn:cleanHtmlAttribute(fromItemMaster)}"/>
		<input type="hidden" name="baseRateSheet" value="${ifn:cleanHtmlAttribute(baseRateSheet)}"/>

	<insta:feedback-panel/>

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Dynamic Package Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Package Name:</td>
					<td class="forminfo">${bean.map.dyna_package_name}</td>
					<td class="formlabel">Rate Plan Code:</td>
					<td><input type="text" name="item_code" maxlength="20" value="${bean.map.item_code}"/></td>
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
													<input type="text" name="charge" id="charge${i}" class="number validate-decimal"
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
														<insta:truncLabel length="15" value="${cat.dyna_pkg_cat_name} (Included)"/>
													</td>
												</c:when>
												<c:otherwise>
													<td>
														<c:set var="t1">
															${charges[firstBedType][cat.dyna_pkg_cat_id].map.dyna_pkg_cat_name} (Included ${(charges[firstBedType][cat.dyna_pkg_cat_id].map.limit_type eq 'U') ? '<br/> - Unlimited)' : ')'}
														</c:set>
														<insta:truncLabel length="15" value="${t1}"/>
															
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
												<input type="text" name="${cat.dyna_pkg_cat_id}.amount_limit" id="${cat.dyna_pkg_cat_id}.amount_limit${i}" class="number validate-decimal"
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
											<input type="text" name="${cat.dyna_pkg_cat_id}.qty_limit" id="${cat.dyna_pkg_cat_id}.qty_limit${i}" class="number validate-decimal"
												 value="${ifn:afmt(charges[bed][cat.dyna_pkg_cat_id].map.qty_limit)}"
												 ${(charges[bed][cat.dyna_pkg_cat_id].map.pkg_included eq 'Y' && charges[bed][cat.dyna_pkg_cat_id].map.limit_type eq 'Q') ? '' : 'readOnly' }>
										</td>
									</c:forEach>
								</tr>
						</c:forEach>
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
		<table class="screenActions">
			<tr>
				<td>
					<button type="submit" name="Save" accesskey="S" ><b><u>S</u></b>ave</button>
						<c:choose>
							<c:when test="${fromItemMaster eq 'false'}">
								| <a href="${cpath}/pages/masters/ratePlan.do?_method=getChargesListScreen&chargeCategory=dynapackages&org_id=${ifn:cleanURL(org_id)}&org_name=${bean.map.org_name}">Dyna Package Charges List</a>
							</c:when>
							<c:otherwise>
								<c:set var="url" value="${cpath}/master/DynaPackage.do?_method=show"/>
								| <a href="<c:out value='${url}&org_id=${ifn:cleanURL(baseRateSheet)}&dyna_package_id=${bean.map.dyna_package_id}'/>">Dyna Package Charges</a>
							</c:otherwise>
						</c:choose>
				</td>

			</tr>
		</table>
	</form>
</body>

</html>
