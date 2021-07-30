<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page import="com.bob.hms.common.RequestContext"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<insta:link type="js" file="stores/purchasedetails.js"/>
<insta:link type="script" file="stores/storescommon.js" />
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<c:set var="prefCed"
	value="<%= GenericPreferencesDAO.getGenericPreferences().getShowCED() %>" />
<c:set var="prefVat"
	value="<%= GenericPreferencesDAO.getGenericPreferences().getShowVAT() %>" />

<div id="itemDialog" style="visibility:hidden">
	<div class="bd" style="text-align:center;">
		<fieldset class="fieldSetBorder" style="width: 98%;text-align:center;">
			<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.add.item"/></legend>
			<table  class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
					<c:choose>
						<c:when test="${prefbarcode eq 'Y'}">
							<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.itembarcode"/>:</td>
							<td>
								<input type="text" name="barCodeId" onchange="getItemBarCodeDetails(this.value);" >
							</td>
							<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.item"/>:</td>
							<td >
								<div id="medicine_wrapper" style="width: 20em; padding-bottom:2em; ">
									<input type="text" name="medicine" id="medicine" style="width: 20em" maxlength="100"/>
									<div id="medicine_dropdown" class="scrolForContainer"></div>
								</div>
							</td>
							<td>
								<a href="#" onclick="onClickPurchaseDetails(); return false;" id="itemPur"><insta:ltext key="storeprocurement.stockentry.invoicedetails.purchasedetails"/></a>
							</td>
						</c:when>
						<c:otherwise>
							<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.item"/>:</td>
							<td>
								<input type="hidden" name="barCodeId" id="barCodeId">
								<div id="medicine_wrapper" style="width: ${iwidth}; padding-bottom:2em; ">
									<input type="text" name="medicine" id="medicine" style="width: ${iwidth}"
									maxlength="100"/>
									<div id="medicine_dropdown" class="scrolForContainer"></div>
								</div>
							</td>
							<td>
								<a href="#" onclick="onClickPurchaseDetails(); return false;" id="itemPur"><insta:ltext key="storeprocurement.stockentry.invoicedetails.purchasedetails"/></a>
							</td>
						</c:otherwise>
					</c:choose>
				</tr>
				<tr>
					<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.batch.or.serial.no"/>:</td>
					<td valign="top" class="formContent">
						<input type="hidden" id="medicineId" name="medicineId" /> 
						<select name="batch" id="batch"
							style="width: 12em" onchange="getBatchDetails(this.value, false);"
							class="dropdown" tabIndex="2">
							<option value="">..Select..</option>
						</select>
					</td>
			
					<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.exp"/>:</td>
					<td class="forminfo">
						<b><label id="expdate"></label></b></td>
				    	<input type="hidden" name="tax_rate" id="tax_rate" value="0"/>
						<input type="hidden" name="tax" id="tax" value="0"/>
						<input type="hidden" name="bonus_tax" value="0"/>
				    	<input type="hidden" name="item_ced_per" value="0"/>
						<input type="hidden" name="item_ced" value="0"/>
					</td>
				</tr>
				<tr>
					<td class="formLabel" >
						<insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.rate"/>:
					</td>
					<td class="formContent">
						<input type="text" name="rate"  id="rate" maxlength="13" onkeypress="return enterNumAndDot(event);" onchange="makeingDec(this.value,this, decDigits);" />
						<input type="hidden" name="mrp" id="mrp"/>
					</td>
					<td class="formLabel" >
						<insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.rev.rate"/>:
					</td>
					<td class="formContent">
						<input type="text" name="rev_rate" id="rev_rate" maxlength="13" onkeypress="return enterNumAndDot(event);" onchange="onChangeRevRate(this, decDigits);" />
					</td>
				</tr>

					<tr>
						<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.ret.billedqty"/>:</td>
						<td>
							<input type="text" name="return_billedQty" id="return_billedQty" maxlength="8"
							onkeypress="return onKeyPressAddQty(event);" onChange="onChangeQty(this);" />
							<input	type="hidden" id="retQty" value="" />
							<input	type="hidden" id="item_code" value="" />
						</td>

						<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.ret.bonusqty"/>:</td>
						<td><input type="text" name="return_bonusQty" id="return_bonusQty"
							maxlength="8" value="0" onkeypress="return onKeyPressAddQty(event);" onChange="onChangeBonusQty(this);"  />
							<b><label id="UOMDesc"></label></b>
							<input	type="hidden" id="retQty" value="" />
						</td>

					</tr>
					<tr id="stockrow">
						<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.curr.billedstock"/>:</td>
						<td class="forminfo" ><b><label
							id="currentstock"></label></b></td>
						<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.curr.bonusstock"/>:</td>
						<td class="forminfo" ><b><label
							id="currentbonusstock"></label></b></td>
					</tr>
					<%-- <c:choose>
						<c:when test="${prefVat eq 'Y'}">
								<tr id="taxes" >
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.taxbasis"/>:</td>
									<td><insta:selectoptions name="tax_type" value="MB" opvalues="MB,M,CB,C" optexts="${mrpstatus}" onchange="displayMedicineDetails(this.value,null);"/></td>
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.taxrate"/>:</td>
									<td class="formInfo"><label id="lbltax_rate"> </label></td>
								</tr>
						</c:when>
					</c:choose> --%>
					<tr>
						<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.disc"/>(%):</td>
						<td>
							<input type="text" name="discper" id="discper" class="num"  onkeypress="return enterNumAndDot(event);"
							onchange="validateMaxPercent(this.value, this); makeingDec(this.value,this,2); return setRevDiscPer(this);" value="0" />
							<input type="hidden" name="disc" id="disc" class="number" />
						</td>
						<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.rev.disc"/>(%):</td>
						<td>
							<input type="text" name="rev_discper" id="rev_discper" class="num"  onkeypress="return enterNumAndDot(event);"
								onchange="validateMaxPercent(this.value, this); makeingDec(this.value,this,2); onChangeRevDiscount(this);" value="0" />
							<input type="hidden" name="rev_disc" id="rev_disc"/>
							<input type="hidden" name="recdAmt" id="recdAmt"/>
						</td>

					</tr>
					<tr>
						<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.schemedisc"/>(%):</td>
						<td>
							<input type="text" name="schemediscper" id="schemediscper" class="num"  onkeypress="return enterNumAndDot(event);"
							onchange="validateMaxPercent(this.value, this); makeingDec(this.value,this,2); return setSchemeRevDiscPer(this);" value="0" />
							<input type="hidden" name="schemedisc" id="schemedisc" class="number" />
						</td>
						<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.rev.schemedisc"/>(%):</td>
						<td>
							<input type="text" name="rev_schemediscper" id="rev_schemediscper" class="num"  onkeypress="return enterNumAndDot(event);"
							onchange="validateMaxPercent(this.value, this); makeingDec(this.value,this,2); onChangeRevSchemeDiscount(this);" value="0" />
							<input type="hidden" name="rev_schemedisc" id="rev_schemedisc" class="number" />
							<input type="hidden" name="debit_pkg_size" id="debit_pkg_size" value="0"/>
						</td>
					</tr>
					<tr id="pkgrow">
						<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.pkgsize"/>:</td>
						<td class="forminfo" >
							<label id="item_unit"></label>
						</td>
					</tr>
					<%-- <c:if test="${prefCed eq 'Y'}">
					<tr>
						<td class="formLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.ced"/>:</td>
						<td class="forminfo" ><b><label
							id="cedamt">0</label></b></td>
						<!--  <td>%</td> -->
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
					</c:if> --%>
				</table>
				<c:set var="_taxindex" value="0"/>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel" style="text-align:left;">Tax Details</legend>
					<table class="formtable">
						<tr>
							<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.taxtype"/>:</td>
							<td class="formInfo">
								<select name="tax_type" class="dropdown" onChange="onChangeTaxType(this.value);">
									<option value="MB"><insta:ltext key="storeprocurement.stockentry.invoicedetails.mrpbased.with.bonus"/></option>
									<option value="M"><insta:ltext key="storeprocurement.stockentry.invoicedetails.mrpbased.without.bonus"/></option>
									<option value="CB"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cpbased.with.bonus"/></option>
									<option value="C"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cpbased.without.bonus"/></option>
								</select>
							</td>
							<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.taxrate"/>:</td>
							<td class="formInfo"><label id="lbltax_rate">0</label></td>
							<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.vatamt"/>:</td>
							<td class="formInfo"><label id="lbltax_amt">0</label></td>
						</tr>
						<tr style="display:none;">
							<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cstrate.percentage.in.brackets"/>:</td>
							<td>
								<input type="text" name="cst_rate" onkeypress="return enterNumAndDot(event);" onchange="makeingDec(this.value,this);onChangeCST()">
							</td>
						</tr>
						<tr>
							<c:forEach items="${groupList}" var="group">
								<c:set var="_taxindex" value="${_taxindex+1}"/>
								<td class="formLabel">
									<label id="taxname${group.item_group_id}">${group.item_group_name}</label>(<label id="taxrate${group.item_group_id}">0</label>%):
								</td>
								<td class="formInfo">
									<label id="taxamount${group.item_group_id}">0</label>
									<input type="hidden" name="taxname${group.item_group_id}" id="taxname_${group.item_group_id}" value="${group.item_group_name}">
									<input type="hidden" name="taxrate${group.item_group_id}" id="taxrate_${group.item_group_id}" value="0">
									<input type="hidden" name="taxamount${group.item_group_id}" id="taxamount_${group.item_group_id}" value="0">
									<input type="hidden" name="taxsubgroupid${group.item_group_id}" id="taxsubgroupid_${group.item_group_id}" value="0">
								</td>
								<c:if test="${_taxindex%3 == 0}">
									</tr><tr>
								</c:if>
							</c:forEach>
						</tr>
					</table>
				</fieldset>
		</fieldset>
		<table width="100%">
			<tr>
				<td>
					<div style="float: left">
					<button type="button" id="Save" name="Save" accesskey="A"
						style="display: inline;" class="button" onclick="onAddMedicine();"
						tabindex="7"><label><b><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.save"/></b></label></button>
					<button type="button" id="Cancel" name="Cancel" accesskey="A"
						style="display: inline;" class="button" onclick="handleCancel();"
						tabindex="8"><label><b><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.cancel"/></b></label></button>
					</div>
				</td>
			</tr>
		</table>
	</div>
</div>
