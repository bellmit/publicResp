<%@page import="org.apache.struts.Globals" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="bill" value="${billDetails.bill}"/>
		<table class="formtable" cellpadding="0" cellspacing="0" border="0">
				<tr>
				<td>
					<select name="chargeGroup" id="chargeGroup" onchange="loadChargeHeads(this)" class="dropdown" style="width:11em">
						<option selected value="">..Charge Group..</option>
						<c:if test="${bill.billType !='M'}">
						<c:set var="ptype" value="${bill.visitType}"/>
						<c:forEach items="${chargeGroupConstList}" var="group">
						<c:choose>
								<c:when test="${(not empty group.DEPENDENT_MODULE) && (preferences.modulesActivatedMap[group.DEPENDENT_MODULE]!='Y')}">
								</c:when>
								<c:when test="${(actionRightsMap.addtobill_charges == 'A')||(roleId==1)||(roleId==2)}">
									<c:choose>
									<c:when test="${ptype=='i'}">
									<c:if test="${group.IP_APPLICABLE=='Y'}">
									<c:choose>
										<c:when test="${(group.CHARGEGROUP_ID == 'TAX')}">
											<c:if test="${(not empty patient.tpa_id) && (bill.billType == 'C')}">
											<option value="${group.CHARGEGROUP_ID}">${group.CHARGEGROUP_NAME}</option>
											</c:if>
										</c:when>
										<c:otherwise>
											<option value="${group.CHARGEGROUP_ID}">${group.CHARGEGROUP_NAME}</option>
										</c:otherwise>
									</c:choose>
									</c:if>
									</c:when>
									<c:when test="${ptype=='o'}">
									<c:if test="${group.OP_APPLICABLE=='Y'}">
									<c:choose>
										<c:when test="${(group.CHARGEGROUP_ID == 'TAX')}">
											<c:if test="${(not empty patient.tpa_id) && (bill.billType == 'C')}">
											<option value="${group.CHARGEGROUP_ID}">${group.CHARGEGROUP_NAME}</option>
											</c:if>
										</c:when>
										<c:otherwise>
											<option value="${group.CHARGEGROUP_ID}">${group.CHARGEGROUP_NAME}</option>
										</c:otherwise>
									</c:choose>
									</c:if>
									</c:when>
									</c:choose>
								</c:when>
								<c:when test="${group.ASSOCIATED_MODULE!=null}">
										<c:if test="${preferences.modulesActivatedMap[group.ASSOCIATED_MODULE]!='Y'}">
										<c:choose>
											<c:when test="${ptype=='i'}">
												<c:if test="${group.IP_APPLICABLE=='Y'}">
												<c:choose>
													<c:when test="${(group.CHARGEGROUP_ID == 'TAX')}">
														<c:if test="${(not empty patient.tpa_id) && (bill.billType == 'C')}">
														<option value="${group.CHARGEGROUP_ID}">${group.CHARGEGROUP_NAME}</option>
														</c:if>
													</c:when>
													<c:otherwise>
														<option value="${group.CHARGEGROUP_ID}">${group.CHARGEGROUP_NAME}</option>
													</c:otherwise>
												</c:choose>
												</c:if>
											</c:when>
											<c:when test="${ptype=='o'}">
											<c:if test="${group.OP_APPLICABLE=='Y'}">
												<c:choose>
													<c:when test="${(group.CHARGEGROUP_ID == 'TAX')}">
														<c:if test="${(not empty patient.tpa_id) && (bill.billType == 'C')}">
														<option value="${group.CHARGEGROUP_ID}">${group.CHARGEGROUP_NAME}</option>
														</c:if>
													</c:when>
													<c:otherwise>
														<option value="${group.CHARGEGROUP_ID}">${group.CHARGEGROUP_NAME}</option>
													</c:otherwise>
												</c:choose>
											</c:if>
											</c:when>
											</c:choose>
									</c:if>
								</c:when>
						</c:choose>
						</c:forEach>
						</c:if>
					</select>
				</td>
				<td>
					<select name="chargeHead" id="chargeHead" onchange="loadOption1()" class="dropdown">
						<option selected value="">..Charge Head..</option>
					</select>
				</td>

				<td>
					<select name="chargeDepartment" id="chargeDepartment" onchange="loadOption2()" class="dropdown">
						<option selected value="">..Department..</option>
					</select>
				</td>&nbsp;

				<td>
					<select name="chargeDescription" id="chargeDescription" onchange="displayUnits()" class="dropdown">
						<option selected value="">..Description..</option>
					</select>
				</td>

				<td>
				   <div id="miscDisc" style="display: none">
						Description:<input type="text" name="miscDescription" id="miscDescription" size="10"  />
						Rate: <input type="text" name="miscChargeRate" id="miscChargeRate" value="0" size="5" onkeypress="return enterNumOnly(event);" />
					</div>
				</td>
				<td>
					<div id="discDiscri" style="display: none">
						Description:
						<input type="text" name="discDescription" id="discDescription" size="15"/>
						<input type="text" name="billDiscPer" id="billDiscPer" value="0" size="3" style="text-align: right"
								onkeypress="return enterNumOnly(event);" onchange="onChangeBillDiscPer()" />%
						Amount:
						<input type="text" name="billDiscAmount" id="billDiscAmount" value="0" size="5"
								style="text-align: right" onkeypress="return enterNumOnly(event);" />
					</div>
				</td>

				<td class="formlabel">
					<div id="qtyDiv" style="display: block">
						Qty:
							<input  type="text" name="chargeQty" id="chargeQty" value="1" style="width: 30px"
									onkeypress="return enterNumOnly(event);" />
					</div>
				</td>&nbsp;

				<td>
					<div id="chargeUnitsText" style="display: block"></div>
					 <input type="text" name="chargeUnits" id="chargeUnits" size="2" disabled="disabled"/>
				</td>&nbsp;

				<td style="display: none" id="chargeUnitsSel" >
					<select name="chargeUnitsSelect" style="width:5em" id="chargeUnitsSelect">
						<option value="G" selected>Days</option>
						<option value="H">Hrs</option>
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Remarks</td>
				<td colspan="4">
					<textarea rows="2" cols="50" id="remarkstxt" name="remarkstxt"></textarea>
				</td>
			</tr>

		</table>

