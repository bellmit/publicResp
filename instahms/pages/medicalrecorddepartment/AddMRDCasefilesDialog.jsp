<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<div id="mrdCasefileDialog" style="visibility:hidden; display: none">
	<div class="bd">
		<table width="700px">
			<tr>
				<td style="width: 50px" align="right">MR No:</td>
				<td valign="top" style="width: 138px" >
					<div class="autocomplete" id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<input type="hidden" name="mr_no@op" value="ilike" />
						<div id="mrnoContainer" style="width: 30em"></div>
					</div>
				</td>
				<td style="width: 100px">
					<button type="button" name="patDetails" id="patDetails" accesskey="G">
						<b><u>G</u></b>etDetails</button>
				</td>
				<td style="width: 100px;" align="right">Department :</td>
				<td valign="top" style="align: left; width: 300px">
					<div id="add_dept_div">
						<input type="text" name="add_dept_name" id="add_dept_name" value="${ifn:cleanHtmlAttribute(param.add_dept_name)}" 
						style="width: 250px"/>
						<div id="addDeptDropdown" style="width: 250px" ></div>
					</div>
						<input type="hidden" name="add_dept_id" id="add_dept_id" value=""/>
						<input type="hidden" name="add_dept_type" id="add_dept_type" value=""/>
				</td>

			</tr>
		</table>
		<table class="formtable" width="100%" style="display: none" id="resultTable">
			<tr>
				<input type="hidden" name="regdate" id="regdate"/>
				<input type="hidden" name="regtime" id="regtime"/>
				<input type="hidden" name="requestedBy" id="requestedBy"/>
				<input type="hidden" name="deathstatus" id="deathstatus"/>
				<input type="hidden" name="requestedById" id="requestedById"/>
				<td class="formlabel">MR No:</td>
				<td class="forminfo"><label id="mr_no"></td>
				<td class="formlabel">Case file No:</td>	
				<td class="forminfo"><label id="casefile_no"/></td>
			</tr>
			<tr>
				<td class="formlabel">Patient Name:</td>
				<td class="forminfo"><label id="patName"></td>
				<td class="formlabel">Department:</td>
				<td class="forminfo"><label id="deptName"/></td>					
			</tr>			
		</table>
		<table class="screenActions">
			<tr>
				<td>
					<button type="submit" name="btnAddCasefile" id="btnAddCasefile" accesskey="A">
						<b><u>A</u></b>dd</button>
					<button type="button" name="btnClose" id="btnClose" >	Close</button>
				</td>
			</tr>
		</table>
	</div>
</div>
