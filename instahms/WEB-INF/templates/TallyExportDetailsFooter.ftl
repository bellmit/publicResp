		</table> <!-- closing the details table that is opened in TallyExportDetailsHeader.ftl file -->
		<table class="screenActions">
			<tr>
				<td>
					<form method="GET">
						<input type="hidden" name="_method" value="getVouchers"/>
						<input type="hidden" name="format" value="tallyxml"/>
						<input type="hidden" name="fromDate" value="${fromDate!}"/>
						<input type="hidden" name="fromTime" value="${fromTime!}"/>
						<input type="hidden" name="toDate" value="${toDate!}"/>
						<input type="hidden" name="toTime" value="${toTime!}"/>
						<input type="hidden" name="exportFor" value="${exportFor!}"/>
						<input type="hidden" name="voucherDate" value="${voucherDate!}"/>
						<input type="hidden" name="useVoucherDate" value="${useVoucherDate!}"/>
						<input type="hidden" name="voucherFromDate" value="${voucherFromDate}"/>
						<input type="hidden" name="voucherToDate" value="${voucherToDate}"/>
						[#list exportItems as item]
							<input type="hidden" name="exportItems" value="${item}"/>
						[/#list]
						<input type="submit" value="XML Export"/>
					</form>
				</td>

				<td>
					<form method="GET">
						<input type="hidden" name="_method" value="getVouchers"/>
						<input type="hidden" name="format" value="summary"/>
						<input type="hidden" name="fromDate" value="${fromDate!}"/>
						<input type="hidden" name="fromTime" value="${fromTime!}"/>
						<input type="hidden" name="toDate" value="${toDate!}"/>
						<input type="hidden" name="toTime" value="${toTime!}"/>
						<input type="hidden" name="exportFor" value="${exportFor!}"/>
						<input type="hidden" name="voucherDate" value="${voucherDate!}"/>
						<input type="hidden" name="useVoucherDate" value="${useVoucherDate!}"/>
						<input type="hidden" name="voucherFromDate" value="${voucherFromDate}"/>
						<input type="hidden" name="voucherToDate" value="${voucherToDate}"/>
						[#list exportItems as item]
							<input type="hidden" name="exportItems" value="${item}"/>
						[/#list]
						<input type="submit" value="View Summary"/>
					</form>
				</td>

				<td>
					<form method="GET">
						<input type="hidden" name="_method" value="getScreen"/>
						<input type="submit" value="Select Period"/>
					</form>
				</td>
			</tr>
		</table>
	</body> <!-- closing the body tag that is opened in TallyExportDetailsHeader.ftl file -->
</html>  <!-- closing the html tag that is opened in TallyExportDetailsHeader.ftl file -->


