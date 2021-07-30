<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<div class="sample-specific-details">
	<div class="sample-specific-details-header card">
		<div class="card-header">
			<h2>
				<insta:ltext key="laboratory.receivesamples.list.sampledetails" />: <span style="font-weight: normal; color: black;"
					id="sample-no">-</span>
			</h2>
		</div>
		<div class="card-details">
			<div class="row">
				<div class="card-details-element card-details-first-col truncate">
					<insta:ltext key="ui.label.patient.name" />: 
					<span id="patient-name">-</span>
				</div>
				<div class="card-details-element card-details-second-col truncate">
					<insta:ltext key="ui.label.mrno" />: 
					<span id="mr-no">-</span>
				</div>
				<div class="card-details-element card-details-third-col truncate">
					<insta:ltext key="laboratory.receivesamples.list.visitid" />: 
					<span id="visit-id">-</span>
				</div>
			</div>
			<div class="row">
				<div class="card-details-element card-details-first-col truncate">
					<insta:ltext key="laboratory.receivesamples.list.collectioncenter" />: 
					<span id="collection-center">-</span>
				</div>
				<div class="card-details-element card-details-second-col truncate">
					<insta:ltext key="laboratory.receivesamples.list.samplesource" />: 
					<span id="source-center">-</span>
				</div>
				<div class="card-details-element card-details-third-col truncate">
					<insta:ltext key="laboratory.receivesamples.list.ageGender" />: 
					<span id="age-gender">-</span>
				</div>
			</div>
			<div class="row">
				<div class="card-details-element card-details-first-col truncate">
					<insta:ltext key="laboratory.receivesamples.list.transferdate" />: 
					<span id="transferred-date">-</span>
				</div>
				<div class="card-details-element card-details-second-col truncate">
					<insta:ltext key="laboratory.receivesamples.list.collectiondate" />: 
					<span id="collection-date">-</span>
				</div>
				<div class="card-details-element card-details-third-col truncate">
					<insta:ltext key="laboratory.receivesamples.list.sampletype" />: 
					<span id="sample-type">-</span>
				</div>
			</div>
			<div class="row">
				<div class="card-details-element truncate">
					<insta:ltext key="laboratory.receivesamples.list.batchid" />: 
					<span id="transfer-batch-id">-</span>
				</div>
			</div>
		</div>
	</div>
	<div id="sample-specific-details-body">
		<form name="splitSampleForm" method="POST" class="hidden">
			<input type="hidden" name="parent_sample_no" id="parent_sample_field" />
			<input type="hidden" name="outsource_dest_id" id="outsource_dest_id_field" /> 
			<input type="hidden" name="deduct_total_destinations" id="deduct_total_destinations_field" />
			<input type="hidden" name="total_destinations" id="total_destinations_field" />

			<!-- split sample confirmation dialog starts -->
		 	<div id="split-sample-confirmation-dialog" style="display: none; max-width: 400px;">
				<div class="bd">
					<fieldset class="fieldSetBorder" style="cursor: pointer; overflow: auto;">
						<legend class="fieldSetLabel">
							<insta:ltext key="laboratory.receivesamples.list.splittests" />
						</legend>
						<div id="split-sample-dialog-content">
							<p class="dialog-content-list-header">
								<insta:ltext key="laboratory.receivesamples.list.selectedtests" />:
							</p>
							<div id="test-list"></div>
						</div>
					</fieldset>
					<button type="button" id="split-confirm" class="lab-receive-button lab-receive-dialog-button">
						<b><insta:ltext key="laboratory.receivesamples.list.createnewsample" /></b>
					</button>
					<button type="button" id="split-abort" value="Cancel" class="lab-receive-button lab-receive-dialog-button">
						<insta:ltext key="laboratory.receivesamples.list.cancel" />
					</button>
				</div>
			</div>
			<!-- split sample confirmation dialog ends -->

			<div id="parent-sample-card"></div>
		</form>
		<div id="child-samples"></div>
	</div>
</div>