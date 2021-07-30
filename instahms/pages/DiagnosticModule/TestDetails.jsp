<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div style="display:none" id="testDetailsDialog">
<div class="bd">
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Test Details</legend>
			<table cellspacing="0" cellpadding="0" class="formTable" width="100%">
				<input type="hidden" name="editedTest" id="editedTest"/>
					<tr>
						<td>
							<div id="testDetailsDiv">
								<table id="testDetailsTable">
									<tr>
										<td class="formlabel">Test Name:</td>
										<td class="forminfo"></td>
										<td class="formlabel">Doctor:</td>
										<td>
											<select name="tdDoctor" id="tdDoctor" style="width:12em" class="dropdown">
												<option value="">--Select--</option>
												<c:forEach var="doctor" items="${doctors}">
													<option value="${doctor.DOCTOR_ID}">${doctor.DOCTOR_NAME}
													</option>
												</c:forEach>
											</select>
										</td>
									</tr>
									<tr>
										<td class="formlabel">Test Date:</td>
										<td>
											<insta:datewidget name="tdTestDate" id="tdTestDate" value=""/>
											<input type="text" class="timefield" size="4" name="tdTestTime" id="tdTestTime"  />
										</td>

										<td class="formlabel" id="tdLabNoLabel"></td>
										<td>
											<input type="text" size='9' name="tdLabno" id="tdLabno">
										</td>
									</tr>
									<tr>
										 <td class="formlabel" >Conduction Instructions:</td>
										<td colspan="">
											<label class="forminfo" id="tdConductionInstr"></label>
										</td>
										<td class="formlabel">Conducting Personnel</td>
										<td><select class="dropdown" id="technician" name="technician">
												<option value="">-- Select --</option>
											</select>
										</td>
									</tr>
									<tr>

										<td class="formlabel">Order Remarks:</td>
										<td colspan="3">
											<label class="forminfo" id="tdOrderRemarks"></label>
										</td>
									</tr>
									<tr>
										<td class="formlabel">Notes:</td>
										<td colspan="3">
											 <textarea id="tdTestRemarks" cols="40"></textarea>
										</td>
									</tr>
								</table>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<div id="sampleDetailsDiv">
								<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Sample Details</legend>
								<table>
									<tr id="sampleRow">
										<td class="formlabel">Sample Type:</td>
										<td>
											<label class="forminfo" id="tdSampleType"></label>
										</td>
										<td class="formlabel" >Sample Id:</td>
										<td id="tdSampleIdLabel" class="forminfo"></td>
									</tr>
									<tr>
										<td class="formlabel" id="tdSampleDateLabel">Sample Date:</td>
										<td>
										<label id="tdSampleDate" class="forminfo" ></label>
										<label class="forminfo" id="tdSampleTime"></label>
										</td>
										<td class="formlabel" >Sample Source:</td>
									 	<td id="tdSampleSource" class="forminfo" ></td>
									</tr>

									<tr>
										<td class="formlabel">Original Sample No:</td>
										<td id="tdOrigSampleNoLabel" class="forminfo"></td>
										 <td class="formlabel">Sample Condition:</td>
										 <td>
											<input type="text" name="tdSpecimenCondition" id="tdSpecimenCondition" maxlength="100"
												value=""/>
										 </td>
									</tr>
								</table>
								</fieldset>
							</div>
							<div id="sampleDateDiv" style="display:none;">
								<table>
									<tr>
										<td class="formlabel">Sample Date:</td>
										<td>
											<insta:datewidget name="sampleDate" id="sampleDate" btnPos="left" />
										</td>
									</tr>
								</table>
							</div>
						</td>
				</table>
		</fieldset>
		<input type="button" name="add" id="add" value="Ok" onclick="onEdit();" tabindex="4"/>
		<input type="button" name="close" id="close" value="Close" onclick="closeTestDialog();" tabindex="5"/>
</div>
</div>