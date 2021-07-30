<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
<head>
	<title>Rate Plan Overrides - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="masters/packmaster.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="masters/charges_common.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script>
		var bedTypesLength = '${bedTypesLengthJson}';
		var packageItemsLength = '${packageItemsLength}';
		var isMultiVisitPackage = '${isMultiVisitPackageJson}';
		var isMultiVisitPack = '${isMultiVisitPack}';
		function onSave() {
			if(isMultiVisitPack) {
				if(validateAllDiscounts() && validateForm())
					return true;
				else
					return false;
			}else {
				return validateAllDiscounts();
			}
		}
	</script>
</head>
<body onload="calculateRoundOff();">
	<h1>Rate Plan Overrides - ${bean.map.org_name}</h1>
	<insta:feedback-panel/>
	<form method="POST" action="${cpath}/pages/masters/ratePlan.do" name="packageForm" >
		<input type="hidden" name="package_id" id="package_id" value="${bean.map.package_id}">
		<input type="hidden" name="_method" value="overRidePackageCharges">
		<input type="hidden" name="chargeCategory" value="packages"/>
		<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>

		<input type="hidden" name="fromItemMaster" value="${ifn:cleanHtmlAttribute(fromItemMaster)}"/>
		<input type="hidden" name="baseRateSheet" value="${ifn:cleanHtmlAttribute(baseRateSheet)}"/>


		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Package Details</legend>
	   		<table class="formtable">
			   	 <tr>
			   	 	<td class="formlabel">Package Name:</td>
			   	 	<td class="forminfo">
		     			${bean.map.package_name}
		      		</td>
		      		<td class="formlabel">Treatment Code Type:</td>
					<td>
						<insta:selectdb name="code_type" table="mrd_supported_codes" valuecol="code_type"
						displaycol="code_type" filtercol="code_category" filtervalue="Treatment" dummyvalue="--Select--"
						value="${bean.map.code_type}" />
					</td>
			   	 </tr>
			   	 <tr>
			   	 	<td class="formlabel">Rate Plan Code:</td>
				   	 <td><input type="text" name="item_code" id="item_code" value="${bean.map.item_code}"  maxlength="15" onKeyDown="limitText(this.form.item_code,15);"
				   	 		onKeyUp="limitText(this.form.item_code,15);"  onchange="limitText(this.form.item_code,15);"/></td>
			   	 </tr>
		   	 </table>
		  </fieldset>

		<div class="resultList">
			<table class="dataTable" id="packageCharges" align="center">
				<tr>
					<th>Bed Types</th>
					<c:forEach var="bed" items="${bedTypes}">
						<th style="width: 2em; overflow: hidden">${bed}</th>
						<input type="hidden" name="bed_type" value="<c:out value='${bed}'/>"/>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Charge:</td>
					<c:forEach var="bed" items="${bedTypes}" varStatus="k">
						<c:set var="i" value="${k.index}"/>
						<td>
							<input type="text" name="charge" id="charge${i}" class="number validate-decimal"
								value="${ifn:afmt(charges[bed].charge)}" onblur="validateDiscount('charge','discount','${i}')"
								onchange="calculateItemCharges(this,'${i}','packCharge')"/>
						</td>
					</c:forEach>
					<input type="hidden" name="ids" value="${i+1}">
				</tr>
				<tr>
					<td style="text-align: right">Discount:</td>
					<c:forEach var="bed" items="${bedTypes}" varStatus="k">
						<c:set var="i" value="${k.index}"/>
						<td>
							<input type="text" name="discount" id="discount${i}" class="number validate-decimal"
								value="${ifn:afmt(charges[bed].discount)}" onblur="validateDiscount('charge','discount','${i}');"
								onchange="calculateItemCharges(this,'${i}','discount')"/>
						</td>
					</c:forEach>
				</tr>
				<c:if test="${not empty bedTypes}">
					<tr>
						<td>Apply Charges To All</td>
						<td><input type="checkbox" name="checkbox" onclick="fillValues('packageCharges', this);fillPackageItemCharges(this);" /></td>
						<c:forEach begin="2" end="${fn:length (bedTypes)}">
							<td>&nbsp;</td>
						</c:forEach>
					</tr>
				</c:if>
			</table>
			</div>
				<c:if test="${isMultiVisitPack}">
				<div class="resultList">
				<table class="formtable">
					<tr>
						<td colspan="6">
							<table class="dataTable" id="itemChargesTable" cellpadding="0" cellspacing="0">
								<tr>
									<th>Bed Types</th>
									<th>Item Qty</th>
									<c:forEach var="bed" items="${bedTypes}">
										<th>${bed}</th>
										<input type="hidden" name="bed_type_for_item" value="<c:out value='${bed}'/>"/>
									</c:forEach>
								</tr>

								<c:forEach var="item" items="${pkgItemList}" varStatus="p">
									<input type="hidden" name="pack_ob_id" value="${item.pack_ob_id}"/>


									<tr id="${item.pack_ob_id}">
										<td>${item.activity_description}</td>
										<td class="number">${item.activity_qty}
											<input type="hidden" name="multi_visit_package_item_qty" id="multi_visit_package_item_qty${p.index}"
											value="${item.activity_qty}"/>
										</td>
										<c:forEach var="bed" items="${bedTypes}" varStatus="k">
											<c:set var="i" value="${k.index}"/>
											<c:set var="c" value="c"/>
											<td>
												<input type="text" value="${ifn:afmt(pkgItemCharges[bed][item.pack_ob_id].map.charge)}"
													name="pack_item_charge" id="pack_item_charge${p.index}${c.concat(i)}" class="number validate-decimal"
													onkeypress="return enterNumOnlyzeroToNine(event);" onchange="calculateRoundOff();"/>
												<input type="hidden" name="item_charge" id="item_charge${i}"
												value="${pkgItemCharges[bed][item.pack_ob_id].map.charge}"/>

												<input type="hidden" name="multi_visit_package_item_base_charge" id="multi_visit_package_item_base_charge${p.index}${c.concat(i)}"
												value="${pkgItemCharges[bed][item.pack_ob_id].map.charge}">
											</td>
										</c:forEach>
									</tr>
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
										</td>
									</c:forEach>
								</tr>

								<tr>
									<c:if test="${not empty bedTypes}">
									   <td style="text-align: right">Apply Charges To All</td>
									   <td><input type="checkbox" name="checkbox" onclick="fillValuesForItemCharge(this);"/></td>
									   <c:forEach begin="2" end="${fn:length (bedTypes)}">
									     <td>&nbsp;</td>
									   </c:forEach>
									</c:if>
								</tr>
							</table>
						</td>
				   </tr>
				 </table>
				</div>
			</c:if>
				 <table class="screenActions">
				 	<tr>
					   	 <td>
					   	 	<button type="submit" name="save" accesskey="S" onclick="return onSave()"/>
					   	 	<b><u>S</u></b>ave</button>
				   	 	 </td>
				   	 	 <td>
				   	 	 	 <c:choose>
								<c:when test="${fromItemMaster eq 'false'}">
									| <a href="${cpath}/pages/masters/ratePlan.do?_method=getChargesListScreen&chargeCategory=packages&org_id=${ifn:cleanURL(org_id)}&type=P&org_name=${bean.map.org_name}"> Package Charges List</a>
								</c:when>
								<c:otherwise>
									<c:set var="url" value="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?_method=getEditPackageCharges"/>
									| <a href="<c:out value='${url}'/>&packId=${bean.map.package_id}&org_id=${ifn:cleanURL(baseRateSheet)}">Package Charges</a>
								</c:otherwise>
							</c:choose>
				   	 	 </td>
			   	 	 </tr>


			</table>
	</form>
</body>

</html>