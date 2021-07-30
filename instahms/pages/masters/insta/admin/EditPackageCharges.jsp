<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="pkg_approval_rights" value="${actionRightsMap.package_approval}"/>

<html>
<head>
	<title>Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="masters/packmaster.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="masters/charges_common.js"/>
	<insta:link type="js" file="masters/editCharges.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var bedTypesLength = '${bedTypesLengthJson}';
		var packageItemsLength = '${packageItemsLength}';
		var isMultiVisitPackage = '${isMultiVisitPackageJson}';
		var masterJobCount = '${masterJobCount}';

	</script>
</head>
<body onload="fillRatePlanDetails('packages','package_id','${ifn:cleanJavaScript(param.packId)}');calculateRoundOff()">
	<h1>Package Charges</h1>
	<insta:feedback-panel/>
	<form action="PackagesMasterAction.do" method="POST" onsubmit="return validateAllDiscounts()">
		<input type="hidden" name="_method" value="editPackageCharges"/>
		<input type="hidden" name="packId" id="packId" value="${ifn:cleanHtmlAttribute(param.packId)}"/>
		<input type="hidden" name="package_id" id="package_id" value="${ifn:cleanHtmlAttribute(param.packId)}"/>
		<input type="hidden" name="multi_visit_package" value="${ifn:cleanHtmlAttribute(param.multi_visit_package)}"/>


		<fieldset class="fieldSetBorder">
	   		<table class="formtable">
			   	 <tr>
			   	 	<td class="formlabel">Package Name:</td>
			   	 	<td class="forminfo">
		     			${packageDetails.map.package_name}
		      		</td>
		      		<td class="formlabel">Rates For Rate Sheet :</td>
		      		<td>
		      			<insta:selectdb name="org_id" id="org_id" value="${param.org_id}"
		      				table="organization_details" valuecol="org_id"
		      				orderby="org_name" displaycol="org_name" onchange="onRatePlanChange();"
		      				filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
		      		</td>
			   	 </tr>
			   	 <tr>
			   	 	<td class="formlabel">Treatment Code Type:</td>
					<td>
						<insta:selectdb name="code_type" table="mrd_supported_codes" valuecol="code_type"
						displaycol="code_type" filtercol="code_category" filtervalue="Treatment" dummyvalue="--Select--"
						value="${packageOrgDetails.map.code_type}" />
					</td>
			   	 	<td class="formlabel">Rate Plan Code:</td>
				   	 <td><input type="text" name="item_code" id="item_code" value="${packageOrgDetails.map.item_code}"  maxlength="15"
				   	 		onKeyDown="limitText(this.form.item_code,15);"
				   	 		onKeyUp="limitText(this.form.item_code,15);"
				   	 		onchange="limitText(this.form.item_code,15);"/></td>
			   	 </tr>
		   	 </table>
		  </fieldset>

		  <div class="resultList">
			<table class="formtable">
				<tr>
					<td colspan="6">
				   	 <table class="dataTable" id="chargesTable">
						<c:forEach var="entry" items="${packageCharges}" varStatus="l">
							<c:choose>
								<c:when test="${entry.key eq 'CHARGES'}">
									<tr>
										<th>BED TYPES</th>
										<c:forEach var="item" items="${entry.value}">
											<c:choose>
												<c:when test="${item == 'GENERAL'}">
													<th>GENERAL/OP</th>
												</c:when>
												<c:otherwise>
													<th>${ifn:cleanHtml(item)}</th>
												</c:otherwise>
											</c:choose>
											<input type="hidden" name="bed_type" value="<c:out value='${item}'/>"/>
										</c:forEach>
									</tr>
								</c:when>
				   	 			<c:when test="${entry.key eq 'PACKAGECHARGE'}">
									<tr>
										<td>PACKAGE CHARGES</td>
										<c:forEach var="item" items="${entry.value}" varStatus="k">
											<c:set var="i" value="${k.index}"/>
											<td><input type="text" value="${ifn:afmt(item)}"
												name="charge" id="charge${i}"
												class="number" onkeypress="return enterNumOnlyzeroToNine(event);"
												onblur="validateDiscount('charge','discount','${i}')" onchange="calculateItemCharges(this,'${i}','packCharge')"/>
											</td>
										</c:forEach>
										<input type="hidden" name="ids" value="${i+1}">
									</tr>
								</c:when>
								<c:when test="${entry.key eq 'DISCOUNT'}">
									<tr>
										<td>DISCOUNT</td>
										<c:forEach var="item" items="${entry.value}" varStatus="k">
											<c:set var="i" value="${k.index}"/>
											<td><input type="text" value="${ifn:afmt(item)}"
												name="discount" id="discount${i}"
												class="number" onkeypress="return enterNumOnlyzeroToNine(event);"
												onblur="validateDiscount('charge','discount','${i}')" onchange="calculateItemCharges(this,'${i}','discount')"/>
											</td>
										</c:forEach>
									</tr>
								</c:when>
							</c:choose>
						</c:forEach>
						<c:if test="${not empty packageCharges}">
							<tr>
								<td>Apply Charges To All</td>
								<td><input type="checkbox" name="checkbox" onclick="fillValues('chargesTable', this);fillPackageItemCharges(this);" /></td>
								<c:forEach begin="1" end="${fn:length (packageCharges)}">
									<td>&nbsp;</td>
								</c:forEach>
							</tr>
						</c:if>
				   	 </table>
				   	</td>
				   </tr>
				  </table>
			</div>
			<c:if test="${isMultiVisitPack}">
				<div class="resultList">
				<table class="formtable">
					<tr>
						<td colspan="6">
					   	 <table class="dataTable" id="itemChargesTable">
							<c:forEach var="entry" items="${packageCharges}">
								<c:choose>
									<c:when test="${entry.key eq 'CHARGES'}">
										<tr>
											<th>BED TYPES</th>
											<th style="text-align: center">Item Qty</th>
											<c:forEach var="item" items="${entry.value}">
												<c:choose>
													<c:when test="${item == 'GENERAL'}">
														<th>GENERAL/OP</th>
													</c:when>
													<c:otherwise>
														<th>${ifn:cleanHtml(item)}</th>
													</c:otherwise>
												</c:choose>
											</c:forEach>
										</tr>
									</c:when>
								</c:choose>
							</c:forEach>
							<c:forEach var="entry" items="${packageItemCostMap['itemcharges']}" varStatus="loop">
								<c:set var="i" value="${loop.index}"/>
								<tr>
									<td>
										${packageItemCostMap['itemdesc'][entry.key]}
									</td>
									<td style="text-align: center">
										${packageItemCostMap['itemQty'][entry.key]}
										<input type="hidden" name="multi_visit_package_item_qty" id="multi_visit_package_item_qty${i}"
											value="${packageItemCostMap['itemQty'][entry.key]}"/>
									</td>

									<c:forEach var="bed" items="${bedTypes}" varStatus="k">
										<c:set var="j" value="${k.index}"/>
										<c:set var="c" value="c"/>
										<td>
											<input type="text" value="${ifn:afmt(pkgItemCharges[bed][entry.key].map.charge)}"
												name="pack_item_charge" id="pack_item_charge${i}${c.concat(j)}" class="number validate-decimal"
												onkeypress="return enterNumOnlyzeroToNine(event);" onchange="calculateRoundOff();"/>

											<input type="hidden" name="package_bed_type" value="${bed}">
											<input type="hidden" name="pack_ob_id" value="${entry.key}"/>
										</td>
									</c:forEach>
								</tr>
							</c:forEach>
							<c:forEach var="entry" items="${packageCharges['CHARGES']}" varStatus="loop" >
								<c:set var="i" value="${loop.index}"/>
								<c:set var="c" value="c"/>
								<c:forEach var="item" items="${packageItemCostMap['itemcharges']}" varStatus="k">
									<c:set var="j" value="${k.index}"/>
									<input type="hidden" name="multi_visit_package_item_base_charge" id="multi_visit_package_item_base_charge${j}${c.concat(i)}"
										value="${packageItemCostMap['packageItemBaseCharge'][item.key][entry]}">
								</c:forEach>
							</c:forEach>

							<tr>
								<td>Unadjusted Amount</td>
								<td>&nbsp;</td>
								<c:forEach begin="0" end="${bedTypesLength-1}" var="k">
									<td>
										<input type="text"
										name="pack_round_off" id="pack_round_off${k}"
										class="number" onkeypress="return enterNumOnlyzeroToNine(event);"
										value="" onchange="isRoundOffEdited()" readonly="readonly"/>
										<input type="hidden" name="round_off_bed_type" value="${packageBedTypesMap[ifn:toString(k)]}">
									</td>
								</c:forEach>
							</tr>
							<c:if test="${not empty packageCharges}">
								<tr>
									<td>Apply Charges To All</td>
									<td><input type="checkbox" name="checkbox1" onclick="fillValuesForItemCharge(this)" /></td>
									<c:forEach begin="0" end="${fn:length (packageCharges)}">
										<td>&nbsp;</td>
									</c:forEach>
								</tr>
							</c:if>
					   	 </table>
					   	</td>
					   </tr>
					  </table>
				</div>
			</c:if>
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
			 <c:if test="${isMultiVisitPack}">
				 <div class="screenActions">
				 	<p><b>Note*: Charges are for the quantity indicated</b></p>
				 </div>
			 </c:if>
			 <table class="screenActions">
			 	<tr>
				   	 <td>
				   	 	<button type="submit" name="save" accesskey="S"
				   	 		${packageDetails.map.type == 'Package' && (roleId != 1 && roleId != 2 && pkg_approval_rights != 'A') && packageDetails.map.approval_status == 'A' ? 'disabled' : ''}
				   	 		onclick="return validateForm();">
				   	 	<b><u>S</u></b>ave</button>
			   	 	 </td>
			   	 	 <td>&nbsp;|&nbsp;</td>
		   	 	 	<td >
			   	 	 	<c:url var="editPackage" value="PackagesMasterAction.do">
							<c:param name="_method" value="show" />
							<c:param name="packId" value="${param.packId}"></c:param>
							<c:param name="org_id" value="${param.org_id}"></c:param>
							<c:param name="multi_visit_package" value="${param.multi_visit_package}"></c:param>
						</c:url>
						<a href="<c:out value='${editPackage}'/>" title="Edit Package Definition">Edit Package Definition</a>
					</td>
					<td>&nbsp;|&nbsp;</td>
					<td>
						<a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?_method=getPackageListScreen&package_active=A&sortReverse=false&approval_status=A">Package List</a>
						<insta:screenlink screenId="mas_packages_applicability" extraParam="?_method=getScreen&packId=${param.packId}&org_id=${param.org_id}&multi_visit_package=${param.multi_visit_package}"
							label="Package Applicability" addPipe="true"/>
					</td>
		   	 	 </tr>
		</table>
	</form>
	<script>
		var derivedRatePlanDetails = ${derivedRatePlanDetails};
	</script>
</body>

</html>