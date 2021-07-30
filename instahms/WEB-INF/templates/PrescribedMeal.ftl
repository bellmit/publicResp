
<table align="center">
	<tr align="center">
		<th>Prescribed Meals Report</th>
	</tr>
	<tr>
		<td style="height : 2em"></td>
	</tr>
</table>

<div class="patientHeader">
  	[#include "VisitDetailsHeader.ftl"]
 </div>
<div>
	<!-- [#assign empty=[] ] -->
	<!-- [#escape x as x?html] -->
	<table cellspacing='0' cellpadding='1' width='100%' align='center' style='margin-top:20px'>

		<!-- [#if ((presMeals!empty)[0]!'')!=''] -->
		<tr>
			<td>
				<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
					<tr>
						<th>Dietician Name</th>
						<th>Prescribed Date</th>
						<th>Time</th>
						<th>Meal Name</th>
						<th>Spl Ins</th>
					</tr>
					<!-- [#list presMeals as meal] -->
					<tr>
						<td>${(meal.doctor_name)}</td>
						<td>${(meal.meal_date)?date?string("dd-MM-yyyy")!}</td>
						<td>${(meal.meal_timing)}</td>
						<td>${(meal.meal_name)!}</td>
						<td>${(meal.special_instructions)!}</td>
					</tr>
					<!-- [/#list] -->
				</table>
			</td>
		</tr>
		<!--[/#if] -->
	</table>
	<!-- [/#escape] -->
</div>