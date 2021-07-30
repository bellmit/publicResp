package Insta::Import;

use strict;
use warnings;
use DBI;
#use LWP;
#use HTTP::Request::Common;
#use HTTP::Cookies;
#use POSIX qw(strftime);
#use MIME::Base64;
use DBD::Pg qw(:pg_types);


#
# Functions that are used to process a single HL7 lab import. The message can be
# received in any fashion (which is handled by the caller.) Once we have a message,
# the process() function will import the results into the Insta tables.
#

sub new {
	my $class = shift;
	my $params = shift;

	my $self = {};
	foreach my $key (keys(%$params)) {
		$self->{$key} = $params->{$key};
	}
	bless $self, $class;
}


sub processMessage {

	my $self = shift;
	my ($ifDesc, $msg,$dbh) = @_;

	my @tests = ();		# to store all the tests that are affected by this message
	my %pushed = ();
	my $lastError = "";

	# Group all segments with the same obsSubId
	my $obxIds = [];
	my $segsForId;
	my $prvsSubId = -1;		# start with invalid one so that first iter hits a difference
	my $prvsValueType = '';

	foreach my $seg (@{$msg->{OBXs}}) {
		if (not($seg->{obsSubId}) || $seg->{obsSubId} != $prvsSubId
			|| ($seg->{valueType}||'NM' ne $prvsValueType)) {
			# not same as previous, start a new segsForId
			$segsForId = [];
			push @$obxIds, $segsForId;
		}
		push @$segsForId, $seg;
		$prvsSubId = $seg->{obsSubId}; $prvsValueType = $seg->{valueType}||'NM';
	}

	#
	# Process each segment list for the same obs sub id
	#
	foreach my $segs (@$obxIds) {
		my $valueType = $segs->[0]{valueType}||'NM';
		my ($errMsg, $test);

		if ($valueType eq 'NM' || $valueType eq 'ST' || $valueType eq 'TX') {
			$self->{'log'}->info("Procesing result segment of [$ifDesc->{interface_name}] interface...");
			($errMsg, $test) = $self->processResultSegment($dbh, $ifDesc, $msg, $segs);
		} else {
			# This includes TX messages also, since report now comes as TX message for RadSpa and Infinitt
			$self->{'log'}->info("Processing document segment  of [$ifDesc->{interface_name}] interface.....");
			($errMsg, $test) = $self->processDocumentSegment($dbh, $ifDesc, $msg, $segs);
  
		}

		if ($errMsg) {
			$lastError = $errMsg;
			last;
		}

		if ($test) {
			my $presId = $test->{presId};
			unless ($pushed{$presId}) {
				push (@tests, $test);
				$pushed{$presId} = 1;
			}
		}
	}

	if ($lastError) {

#		$self->{'log'}->error("Rolling back due to error: $lastError\n");
#		$dbh->rollback();
#		$dbh->disconnect();

		return $lastError;
	}

	# Now, for each test in the list, do the following:
	# 1. Set conducted flag as Y in tests_prescribed, provided all results are done
	# 2. Create a report (one row in test_visit_reports) if it doesn't exist
	# TODO: if conduction format is not V, then we could deal with this differently
	foreach my $test (@tests) {
		$self->{'log'}->info("Setting conduction status......");
		$self->setConductedFlag($dbh, $test->{presId}, $ifDesc->{set_completed_status}, $test->{testId});
		$self->{'log'}->info("Getting report........");
		my $reportId = $self->createOrGetReportId(
			$dbh, $test->{visitId}, $test->{presId}, $ifDesc->{report_group_method});
			 
		$self->{'log'}->info("Report created in database: $reportId\n");    
		
		$self->{'log'}->info("prescription_id =  $test->{presId}, test_id = $test->{testId}, visit_id = $test->{visitId}, 
				set_completed_status =  $ifDesc->{set_completed_status}, report_group_method = $ifDesc->{report_group_method}");
	}

#	$dbh->commit();
#	$dbh->disconnect();


	$self->{'log'}->info("Done processing message   \n");
	return $lastError;
}



sub processResultSegment {
	my $self = shift;
	my ($dbh, $ifDesc, $msg, $segs) = @_;

	$self->{'log'}->info("Trying to process result segment which is unsupported currently");

	# assume only single segment for results, ie, not handling multiple segs with same sub ID.
	my $seg = $segs->[0];

	my $orderNum = $msg->{ORC}{placerOrderNum} || $msg->{OBR}{placerOrderNum};     # [123.901.BL0123,InstaHMS]

	if ($orderNum && (ref($orderNum) eq 'ARRAY')) {
		$orderNum = $orderNum->[0];
	}

	my ($prescId, $resultCode);

	if ($orderNum) {
		# Fetch prescribed ID based on Order No.
		my $sampleId;
		($prescId, $resultCode, $sampleId) = split(/\./, $orderNum, 3);

	} else {
		# Order num not available: guess it based on MR No, result code.
		
		my $resultCode = $seg->{obsId}[0];		# clinigene, powerlink uses 0,1
		my $resultName = $seg->{obsId}[1];
		if (@{$seg->{obsId}} > 3) {
			$resultCode = $seg->{obsId}[3];		# istat uses 3,3 (ie, not universal code)
			$resultName = $seg->{obsId}[3];
		}
		my $obsTime = $seg->{obsTime} || $msg->{OBR}{obsTime};

		my $mrNo;
		my $pid = $msg->{PID};
		unless ($pid) {
			return("Missing PID Segment, and no Order Number (OBR-2 or ORC-2) ", undef);
		}

		$mrNo = $pid->get('pidList');

		my $errMsg;
		($prescId, $errMsg) =
			$self->guessResultPrescId($dbh, $mrNo, $resultCode, $resultName, $obsTime);

		unless (defined($prescId)) {
			return ($errMsg, undef);
		}
	}

	my ($row, $errMsg) = $self->getResultTestDetails($dbh, $prescId, $resultCode);
	unless ($row) {
		return ($errMsg, undef);
	}

  	$self->{'log'}->info("resultCode: $resultCode, Visit: $row->{visit_id}, prescId: $row->{prescribed_id} ");

	my $normality = '';
	my $refText = '';
	my $value = $seg->{obsValue};

	# Get the result ranges if defined, and calculate result normality
	my $range = $dbh->selectrow_hashref(qq{SELECT * FROM test_result_ranges
		WHERE resultlabel_id=?
			AND (range_for_all = 'Y' OR
			CASE WHEN age_unit='Y' THEN (
				((min_patient_age IS NULL OR ?>=min_patient_age) AND
				 (max_patient_age IS NULL OR ?<=max_patient_age) AND
				 (patient_gender='N' OR patient_gender=?)) )
			ELSE
				(
				((min_patient_age IS NULL OR ?>=min_patient_age) AND
				 (max_patient_age IS NULL OR ?<=max_patient_age) AND
				 (patient_gender='N' OR patient_gender=?)) ) END
				)
		ORDER BY priority ASC LIMIT 1
		}, {}, $row->{resultlabel_id}, $row->{age}, $row->{age}, $row->{patient_gender}, $row->{age_days}, $row->{age_days}, $row->{patient_gender});

	if ($range) {
		if (defined($range->{max_improbable_value}) && ($value > $range->{max_improbable_value})) {
			$normality = '###';
		} elsif (defined($range->{max_critical_value}) && ($value > $range->{max_critical_value})) {
			$normality = '##';
		} elsif (defined($range->{max_normal_value}) && ($value > $range->{max_normal_value})) {
			$normality = '#';

		} elsif (defined($range->{min_improbable_value}) && ($value < $range->{min_improbable_value})) {
			$normality = '***';
		} elsif (defined($range->{min_critical_value}) && ($value < $range->{min_critical_value})) {
			$normality = '**';
		} elsif (defined($range->{min_normal_value}) && ($value < $range->{min_normal_value})) {
			$normality = '*';
		} elsif (defined($range->{min_normal_value}) || defined($range->{max_normal_value})) {
			$normality = 'Y';
		}
		# else, no ranges have been defined at all: normality is undefined. user must set it.

		$refText = $range->{reference_range_txt};
	}

	# Insert/update the result value in test_details
	$self->setTestResult($dbh, $row->{mr_no}, $row->{visit_id}, $row->{prescribed_id}, $row->{test_id},
		$row->{resultlabel_id}, $row->{resultlabel}, $value, $row->{units}, $refText, $normality);

	my $test = {visitId=>$row->{visit_id}, presId=>$row->{prescribed_id}, testId=>$row->{test_id}};
	return ('', $test);
}


sub processDocumentSegment {
	my $self = shift;
	my ($dbh, $ifDesc, $msg, $segs) = @_;
	$self->{'log'}->info("Processing document segment....");
	my $seg = $segs->[0];

	my $orderNum = $msg->{ORC}{placerOrderNum} || $msg->{OBR}{placerOrderNum};     # [123,InstaHMS]

	if ($orderNum && (ref($orderNum) eq 'ARRAY')) {
		$orderNum = $orderNum->[0];
	}

#	$orderNum = $orderNum->[0] if ($orderNum);
	my $obsTime = $seg->{obsTime} || $msg->{OBR}{obsTime};
	my $docName = $seg->{obsId}[1];
	my $docType = '';
	if(defined $seg->{units} and $self->{'test_dept'} eq 'DEPT_LAB') {
	      #This is always for laboratory, if same will defined for Radiology then it will take from units.
	      my $docType = $seg->{units};
	}elsif(	$self->{'test_dept'} eq 'DEPT_RAD'){
	      my $docType = 'SYS_RR'; # hard-coded to this, earlier the message  was sending SYS_LR in units fields seg->{units}, THIS is only for RAD  
	}
	my $prescId;
	my $ignoreSampleId = '';
	my $ignoreResultId = '';

	if ($orderNum) {
		($prescId, $ignoreResultId, $ignoreSampleId) = split(/\./, $orderNum, 3);
		$self->{'log'}->info("prescription id : $prescId");
		# $prescId = $orderNum;

	} else {
		$self->{'log'}->error("Missing order number (OBR-2 or ORC-2 segment ): This is not supported currently. Please provide the right data!!!");
		my $obsId = $seg->{obsId}[0];
		my $pid = $msg->{PID};
		unless ($pid) {
			return("Missing PID Segment, and no order number.", undef);
		}

		my $mrNo = $pid->get('pidList');
		if ($obsId) {
			# guess the order from the service ID
			my $errMsg;
			my $serviceName = $docName || $msg->{OBR}{serviceId}[1];
			($prescId, $errMsg) = $self->guessTestPrescId($dbh, $mrNo, $obsId, $serviceName, $obsTime);
			unless (defined($prescId)) {
				return ($errMsg, undef);
			}
		}
	}

	my ($image, $url, $mime, $extn) = $self->getDocumentData($segs, $ifDesc);
	unless ($image || $url) {
		return ('No document/pointer found', undef);
	}
	$self->{'log'}->info("Got document data....");

	if ($prescId) {
		$self->{'log'}->info("Saving test document .....");
		# store image against the test (cannot store hyperlink against test)
		$self->{'log'}->info("Storing image against order: $prescId");
		# This saves a record in patient_documents and one in test_documents table
		$self->saveTestDocument($dbh, $prescId, $image, $url, $docType, $mime, $extn, $docName, $obsTime);

	} else {
		$self->{'log'}->info("No prescription Id: This is not supported currently (OBR-2 or ORC-2 is having wrong data. Pls provide the right data!!)");
		my $pid = $msg->{PID};
		my $pv1 = $msg->{PV1};
		my $mrNo = $pid->get('pidList');
		my $visitId = $pv1->get('visitNum') if $pv1;
		$docName = $msg->{OBR}{serviceId}[1];

		unless ($visitId || $mrNo) {
			$self->{'log'}->error(" No visitId / mrNo: cannot save document. (PID-3 or PV1-19 ) segment is missing Pls provide the right data !!!");
			return (undef, undef);
		}

		if ($visitId) {
			$self->{'log'}->info("Storing image against visit(PV1-19 segment): $visitId");
		} else {
			$self->{'log'}->info("Storing image against patient (PID-3 segment ) : $mrNo");
		}

		$self->savePatientDocument($dbh, $mrNo, $visitId, $image, $url, $docType, $mime, $extn,
			$docName, $obsTime);
	}

	return (undef, undef);		# since no reports are affected, need not return the test.
}


sub getResultTestDetails {
	my $self = shift;
	my ($dbh, $prescId, $resultCode) = @_;

	my $rows = $dbh->selectall_arrayref(qq{
		SELECT trm.resultlabel_id, trm.resultlabel, trm.units, trm.test_id,
			d.test_name, coalesce(tp.mr_no,isr.mr_no) as mr_no, tp.pat_id as visit_id, tp.prescribed_id,
			floor((current_date - coalesce(dateofbirth, expected_dob))/365.24) as age, pd.patient_gender,
			floor(((current_date - coalesce(dateofbirth, expected_dob))/365)*365) as age_days
		FROM tests_prescribed tp
			JOIN test_results_master trm ON (trm.test_id = tp.test_id)
   			JOIN diagnostics d ON (d.test_id = tp.test_id)
  			JOIN patient_details pd ON (tp.mr_no = pd.mr_no)
  			LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
		WHERE tp.conducted IN ('N','P') AND tp.prescribed_id=?
			AND coalesce(trm.hl7_export_code, trm.resultlabel_id::text)=?
	}, {Slice=>{}}, $prescId, $resultCode);

	if (@$rows < 1) {
		$self->{'log'}->error("Order not found for prescId = $prescId, result code =  $resultCode");
		return (undef, "Order not found");
	}
	return ($rows->[0], undef);
}


#
# Guess the prescribed ID of the test given the MR No and the result code
#
# Since there is no order No, it is an approximation, and may result in multiple prescribed IDs.
# This can happen if the same result is part of two prescriptions.
# To narrow down, we only look at not-conducted tests, and the date of conduction
# is within a day of the date of order. If multiple rows are returned, we will
# return undef, since it is ambiguous as to which prescribed_id this result
# belongs to.
#
sub guessResultPrescId {
	my $self = shift;
	my ($dbh, $mrNo, $resultCode, $resultName, $obsTime) = @_;

	my $rows = $dbh->selectall_arrayref(qq{
		SELECT tp.prescribed_id, trm.resultlabel, d.test_name
		FROM test_results_master trm
   			JOIN tests_prescribed tp ON (tp.test_id = trm.test_id)
			JOIN diagnostics d ON (tp.test_id = d.test_id)
		WHERE tp.conducted IN ('N','P') AND coalesce(trm.hl7_export_code, trm.resultlabel_id::text)=?
			AND tp.mr_no=? AND date(tp.pres_date) BETWEEN (to_date(?, 'YYYYMMDDHH24MISS') - 2)
  			AND to_date(?, 'YYYYMMDDHH24MISS')
	}, {Slice=>{}}, $resultCode, $mrNo, $obsTime, $obsTime);

	if (@$rows < 1) {
		$self->{'log'}->error("No orders available for $mrNo - $resultCode [$resultName]");
		my $msg = $self->getGuessDiagnostics($dbh, $mrNo, $obsTime);
		return (undef, $msg);
	}

	if (@$rows > 1) {
		my $msg = "Multiple orders found for $mrNo - $resultCode";
		$self->{'log'}->error( $msg);
		foreach my $row (@$rows) {
			$self->{'log'}->info(" Result: $row->{resultlabel},Test :  $row->{test_name}, Pres Id :	$row->{prescribed_id}");
		}
		return (undef, $msg);
	}

	return ($rows->[0]->{prescribed_id}, undef);
}

sub guessTestPrescId {
	my $self = shift;
	my ($dbh, $mrNo, $serviceId, $serviceName, $obsTime) = @_;

	my $rows = $dbh->selectall_arrayref(qq{
		SELECT tp.prescribed_id
		FROM tests_prescribed tp
			JOIN diagnostics d ON (tp.test_id = d.test_id)
		WHERE tp.conducted IN ('N','P') AND coalesce(tp.hl7_export_code, tp.test_id)=?
			AND tp.mr_no=? AND date(tp.pres_date) BETWEEN (to_date(?, 'YYYYMMDDHH24MISS') - 2)
  			AND to_date(?, 'YYYYMMDDHH24MISS')
	}, {Slice=>{}}, $serviceId, $mrNo, $obsTime, $obsTime);

	if (@$rows < 1) {
		$self->{'log'}->error("No orders available for $mrNo - $serviceId [$serviceName]");
		my $msg = $self->getGuessDiagnostics($dbh, $mrNo, $obsTime);
		return (undef, $msg);
	}
}

sub getGuessDiagnostics {
	my $self = shift;
	my ($dbh, $mrNo, $obsTime) = @_;

	# Is the MR No. valid?
	my $rows = $dbh->selectall_arrayref(qq{SELECT mr_no FROM patient_details WHERE mr_no=?},
		{Slice=>{}}, $mrNo);
	if (@$rows == 0) {
		my $msg = "Invalid MR No: $mrNo";
		$self->{'log'}->info(" $msg");
		return $msg;
	}

	# Print all orders for patient within given date range: help diagnose
	$rows = $dbh->selectall_arrayref(qq{
		SELECT prescribed_id, test_id, test_name, pres_date FROM tests_prescribed
			JOIN diagnostics d USING (test_id)
		WHERE mr_no=?
			AND date(pres_date) BETWEEN
				(to_date(?, 'YYYYMMDDHH24MISS') - 1) AND to_date(?, 'YYYYMMDDHH24MISS')
		}, {Slice=>{}}, $mrNo, $obsTime, $obsTime);

	foreach my $row (@$rows) {
		$self->{'log'}->info(" Order: ", $row->{prescribed_id}, ", ", $row->{pres_date}, ", ",
			$row->{test_id}, ", ", $row->{test_name});
	}

	# Print all charges related to diag for patient within given date range:
	# Maybe they did direct billing instead of ordering.
	$rows = $dbh->selectall_arrayref(qq{
		SELECT bill_no, date(posted_date) as date, act_description FROM bill_charge
			JOIN bill b using(bill_no)
			JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
		WHERE charge_head = 'LTDIA' AND hasactivity = false AND pr.mr_no = ? AND
			date(posted_date) BETWEEN (to_date(?, 'YYYYMMDDHH24MISS') - 2)
				AND to_date(?, 'YYYYMMDDHH24MISS')
		}, {Slice=>{}}, $mrNo, $obsTime, $obsTime);

	foreach my $row (@$rows) {
		$self->{'log'}->info(" Charge ", $row->{date}, ", ", $row->{bill_no}, ", ", $row->{act_description} );
	}
	return "No orders available";
}

sub setTestResult {
	my $self = shift;
	my ($dbh, $mrNo, $patientId, $presId, $testId, $resultLabelId,
		$resultLabel, $resultValue, $units, $refRanges, $abnormalFlag) = @_;

	my $rows = $dbh->selectcol_arrayref(
		"SELECT resultlabel_id FROM test_details WHERE prescribed_id=? AND resultlabel_id=?",
		{}, $presId, $resultLabelId);

	if (@$rows > 0) {
		$self->{'log'}->error("Not updating test result, exists: $resultLabel for presId $presId");

	} else {
		$self->{'log'}->info("Inserting test result ($resultLabel = $resultValue; $abnormalFlag) for presId $presId");
		my $codes = $dbh->selectrow_hashref("SELECT code_type, result_code FROM test_results_master
			WHERE resultlabel_id=?", {}, $resultLabelId);
		my $sth = $dbh->prepare(qq{
			INSERT INTO test_details (resultlabel, report_value, units, reference_range, withinnormal,
				conducted_in_reportformat, comments,user_name,
				mr_no, patient_id, prescribed_id, test_id, resultlabel_id, code_type, result_code,
				test_detail_status)
			VALUES (?,?,?,?,?,
				'N','',?,
				?,?,?,?,?,?,?,'P')
		});
		$sth->execute($resultLabel, $resultValue, $units||'', $refRanges||'', $abnormalFlag,
			$self->{userId},
			$mrNo, $patientId, $presId, $testId, $resultLabelId, $codes->{code_type}, $codes->{result_code});
	}
}

sub setConductedFlag {
	my $self = shift;
	my ($dbh, $presId, $setStatus, $testId) = @_;

	#
	# After all results have been received, we set the conducted status to setStatus value.
	# If all results are not in, we only mark it as P always. No checks for severity
	# is done, it is expected to be done ony by the validator using UI
	#

	#
	# Check for whether all result values have been received
	# Need to join trm so that all valid values for a test are considered, with
	# a left join on test details to check whether it has the value
	#
	my $incompleteResults = $dbh->selectall_arrayref(qq{
		SELECT trm.resultlabel FROM tests_prescribed tp
		JOIN test_results_master trm ON (trm.test_id = tp.test_id)
		LEFT JOIN test_details td
		ON (td.resultlabel_id=trm.resultlabel_id AND td.prescribed_id=tp.prescribed_id)
		WHERE tp.prescribed_id=?
			AND (td.report_value = '' OR td.report_value IS NULL)
		}, undef, $presId);
	# We will not have any results from the above qry for a radiology test
	my $manualReagents = $dbh->selectall_arrayref(qq{
		SELECT reagent_id FROM diagnostics_reagents
		WHERE test_id=? AND consumption_method = 'M'
	}, undef, $testId);

	# We will not have any results from the above qry for a radiology test

	my $conductedStatus;
	if (@$incompleteResults || @$manualReagents) {
		if (@$incompleteResults) {
			$self->{'log'}->info("Test is not complete ");
		} else {
			$self->{'log'}->info("Test requires manual reagent consumption ");
		}
		$conductedStatus = 'P';
	} else {
		$self->{'log'}->info("setting conduction status to $setStatus");
		$conductedStatus = $setStatus;
	}

	$dbh->do(qq{UPDATE tests_prescribed SET conducted=? WHERE prescribed_id=?}, undef,
		$conductedStatus, $presId);
		$self->{'log'}->info("Setting conducted status for presId $presId as $conductedStatus");
	if ($conductedStatus ne 'P') {
		$dbh->do(qq{UPDATE test_details SET test_detail_status='C' WHERE prescribed_id=?}, undef,
			$presId);
	}

	# a row in tests_conducted is also required, otherwise, we don't see these tests in
	# some report builders.
	$dbh->do(qq{
		INSERT INTO tests_conducted
			(prescribed_id, mr_no, patient_id, test_id, conducted_date, conducted_time,
				satisfactory_status, user_name)
		SELECT prescribed_id, mr_no, pat_id, test_id, current_timestamp, current_time, 'Y', 'auto_update'
		FROM tests_prescribed tp
		WHERE prescribed_id = ?
			AND NOT EXISTS (SELECT * FROM tests_conducted WHERE prescribed_id = tp.prescribed_id)}, undef,
		$presId);

	if ($conductedStatus ne 'P') {
		# if we are completing the test, then, let us also reduce stock if set up to do so

		# Get a list of reagents and stores from where reduction is required for this test
		my $reagents = $dbh->selectall_arrayref(qq{
			SELECT dds.store_id, dr.reagent_id, dr.quantity_needed
			FROM tests_prescribed tp
				JOIN diagnostics d ON (d.test_id = tp.test_id)
				JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
				JOIN diagnostics_reagents dr ON (dr.test_id = tp.test_id)
				JOIN diagnostic_department_stores dds ON (dds.center_id = pr.center_id
					AND dds.ddept_id = d.ddept_id)
			WHERE tp.prescribed_id=? AND dr.consumption_method = 'A' and NOT tp.stock_reduced
		}, {Slice=>{}}, $presId);

		foreach my $reagent (@$reagents) {

			my $storeId = $reagent->{store_id};
			my $medId = $reagent->{reagent_id};
			my $remQty = $reagent->{quantity_needed};
			$self->{'log'}->info("reducing reagents: This is not expected...");
			# get available batches in the store: order by such that positive qty comes
			# before negative quantity, and increasing order of qty available.
			my $batches = $dbh->selectall_arrayref(qq{
				SELECT batch_no, qty FROM store_stock_details
				WHERE medicine_id=? AND dept_id=?
				ORDER BY sign(qty) DESC, exp_dt, qty
			}, {Slice=>{}}, $reagent->{reagent_id}, $reagent->{store_id});

			# reduce each batch till we're out of qty, let it go negative on the last one
			# or the first one where we are already negative
			for (my $i = 0; $i < @$batches; $i++) {
				my $batch = $batches->[$i];
				my $qtyInBatch = $batch->{qty};
				my $qty;
				if ($qtyInBatch < 0) {
					# already negative, let us go further negative fully in this batch.
					$qty = $remQty;
				} elsif ($i == (@$batches - 1)) {
					# last batch, consume everything, even if it goes negative
					$qty = $remQty;
				} else {
					$qty = $qtyInBatch < $remQty ? $qtyInBatch : $remQty;
				}
				$dbh->do(qq{
					UPDATE store_stock_details SET qty = qty - ?
					WHERE medicine_id =? AND batch_no = ? AND dept_id = ?
				}, undef, $qty, $medId, $batch->{batch_no}, $storeId);

				$remQty = $remQty - $qty;
				last unless ($remQty > 0);
			}
		}
		if (@$reagents) {
			$self->{'log'}->info("Setting the stock reduced flag, this is not expected....");
			# indicate that stock has been reduced. Next time, we won't reduce it, just in case
			# they move the status from conduction back to partial. We are safe to do this since
			# we would not mark it completed if there were any manual reagents.
			$dbh->do(qq{UPDATE tests_prescribed SET stock_reduced = true WHERE prescribed_id=?},
				undef, $presId);
		}
	}
}

sub createOrGetReportId {
	my $self = shift;
	my ($dbh, $visitId, $presId, $reportGroupMethod) = @_;

	my $reportId;

	#
	# Get the prescId details, including report_id. If report is already made for this test.
	#
	$self->{'log'}->info("getting the report id for the test .....");
	my $prescDetails = $dbh->selectrow_hashref(qq{SELECT report_id, ddept_id, ddept_name, common_order_id
		FROM tests_prescribed
			JOIN diagnostics USING (test_id)
			JOIN diagnostics_departments USING (ddept_id)
		WHERE prescribed_id=?}, {}, $presId);

	$reportId = $prescDetails->{report_id};
	if ($reportId) {
		$self->{'log'}->info("Found existing report ID: ", $reportId) ;
		return $reportId;
	}

	#
	# Check if there is a report within the same dept that is yet to be signed off,
	# for this patient_id. If so, add this test to that report.
	#
	my $deptReport;
	if ($reportGroupMethod eq 'D') {
		# find report in the same department
		$deptReport = $dbh->selectrow_hashref(qq{SELECT report_id
			FROM tests_prescribed
				JOIN diagnostics using (test_id)
				JOIN test_visit_reports using (report_id)
			WHERE pat_id=? AND ddept_id=? AND signed_off='N'}, {},
				$visitId, $prescDetails->{ddept_id});

	} elsif ($reportGroupMethod eq 'O') {
		# find report for the same common_order_id
		$deptReport = $dbh->selectrow_hashref(qq{SELECT report_id
			FROM tests_prescribed
				JOIN test_visit_reports using (report_id)
			WHERE pat_id=? AND common_order_id=? AND signed_off='N'}, {},
				$visitId, $prescDetails->{common_order_id});

	} elsif ($reportGroupMethod eq 'OD') {
		# find report for the same department + order id
		$deptReport = $dbh->selectrow_hashref(qq{SELECT report_id
			FROM tests_prescribed
				JOIN diagnostics using (test_id)
				JOIN test_visit_reports using (report_id)
			WHERE pat_id=? AND ddept_id=? AND common_order_id=? AND signed_off='N'}, {},
				$visitId, $prescDetails->{ddept_id}, $prescDetails->{common_order_id});

	} elsif ($reportGroupMethod eq 'N') {
		# force a new report for each test
		$deptReport = undef;

	} else {
		# Add it to any report for the patient
		$self->{'log'}->info("No report group method is set, which means it will go attached to any existing report.");
		$deptReport = $dbh->selectrow_hashref(qq{SELECT report_id
			FROM tests_prescribed
				JOIN test_visit_reports using (report_id)
			WHERE pat_id=? AND signed_off='N'}, {},
				$visitId);
	}

	if ($deptReport && $deptReport->{report_id}) {
		# found a report in the same department, use that to attach to.
		$self->{'log'}->info("Found report for criterion ", $reportGroupMethod, ": ", $deptReport->{report_id});
		$reportId = $deptReport->{report_id};

	} else {
		#
		# No existing report that the test can be attached to. Create one.
		# Create a new report for this prescribed Id. Name it based on the department
		# If the same name exists, append -1, -2 etc to the report.
		#
		my $namePrefix = '';
		if($self->{'test_dept'} eq 'DEP_RAD') {
			my $namePrefix = "RR-" . $prescDetails->{ddept_name} . "-";
		}elsif ( $self->{'test_dept'} eq 'DEPT_LAB' ) {
			my $namePrefix = "LR-" . $prescDetails->{ddept_name} . $prescDetails->{ddept_name} . "-";  
		}

		my $num = 1;
		my $reportName;
		my @exist;
		do {
			$reportName = $namePrefix . $num++;
			@exist = $dbh->selectrow_array(
				"SELECT report_id FROM test_visit_reports WHERE patient_id=? and report_name=?", {},
				$visitId, $reportName);
		} while (@exist > 0);

		# get report_id from sequence
		$reportId = ($dbh->selectrow_array("SELECT nextval('test_report_sequence')"))[0];
		$self->{'log'}->info("Creating new report: id=$reportId, name=$reportName");

		# insert the report, with no report_data
		my $sth = $dbh->prepare(qq{
			INSERT INTO test_visit_reports
			(report_id, patient_id, report_name, category, report_date, user_name, report_mode)
			VALUES  (?, ?, ?, ?, current_timestamp, ?, 'P')
			});

		$sth->execute($reportId, $visitId, $reportName, $self->{'test_dept'} , $self->{userId});
	}

	# update the order with the new report Id
	my $sth = $dbh->prepare("UPDATE tests_prescribed SET report_id=?, user_name=? WHERE prescribed_id=?");
	$sth->execute($reportId, $self->{userId}, $presId);

	# Set the report_results_severity_status based on severity
	$self->{'log'}->info("setting report severity... should be A always for radiology reports");
	$sth = $dbh->prepare(qq{UPDATE test_visit_reports tvr
		SET report_results_severity_status = CASE WHEN
			(EXISTS (SELECT * FROM test_details td JOIN tests_prescribed tp USING (prescribed_id)
				WHERE tp.report_id = tvr.report_id AND td.withinnormal != 'Y'))
			THEN 'H' ELSE 'A' END
		WHERE tvr.report_id = ?
		});
	$sth->execute($reportId);
	$self->{'log'}->info("new report id : $reportId");
	return $reportId;
}

sub getDocumentData {
	my $self = shift;
	my ($segs, $ifDesc) = @_;
	my $seg = $segs->[0];
	my $valueType = $seg->{valueType}||'NM';
	my $obsValue = $seg->{obsValue};
	my $obsValueDecoded;
	my ($image, $urlLink, $type, $subType);

	if ($valueType eq 'ED') {
		# Base64 embedded data
		$image = '';
		foreach my $subIdSeg (@$segs) {
			$image = $image . decode_base64($subIdSeg->{obsValue}[4]);
		}
		$type = $obsValue->[1];
		$subType = $obsValue->[2];

	## added TX type to accomodate inline text report for radiology

	} elsif ($valueType eq 'TX') {
		$urlLink = '';
		$type = 'text/html';
		$subType = 'txt';
 		if ($obsValue) {
                        $obsValueDecoded = decodeResultText($obsValue);
                }

		$image = $obsValueDecoded || '-NA-';
#		my $s = sprintf "%vx", $image;
#		print "$s\n";
		return ($image, $urlLink, $type, $subType);

	} elsif ($valueType eq 'RP') {
		$urlLink = $seg->get('obsValue', 0);
		$type = $seg->get('obsValue', 2);
		$subType = $seg->get('obsValue', 3);

		unless ($urlLink) {
			$self->{'log'}->error("No file name given for type RP");
			return (undef, undef, undef, undef);
		}

		my ($fileName, $extension);
		if ($urlLink =~ /:\/\// ) {
			# truly a link, import it as such.
			$image = undef;
		} else {
			# file in our files directory, read it in.
			my $filesDir = $ifDesc->{results_import_dir};
			unless ($filesDir) {
				$self->{'log'}->error("No results import dir, cannot import RP $urlLink");
				return (undef, undef, undef, undef);
			}
			$fileName = $filesDir . "/files/" . $urlLink;
			$extension = $urlLink;
			$extension =~ s/.*\.//;
			# read the file into the image
			$/ = undef;
			my $sts = open (IMG, $fileName);
			unless ($sts) {
				$self->{'log'}->error("Unable to open $fileName: $!");
				return (undef, undef, undef, undef);
			}
			$image = <IMG>;
			close IMG;
			$urlLink = undef;
		}

		my $mime;
		if ($type && $subType) {
			# mime type is given:
			if ($type eq 'Image') {
				# JPEG / GIF/ TIFF/ PNG
				$mime = "image/" . lc($subType);
			} else {
				# application data: use sub-type itself as the mime
				$mime = $subType;
			}
		} elsif ($fileName) {
			# auto-detect
			$mime = `/usr/bin/file -b --mime-type "$fileName"`;
			$mime =~ s/\s//;
		} else {
			$self->{'log'}->error(" mime-type cannot be determined for URL Link");
			return (undef, undef);
		}

		return ($image, $urlLink, $mime, $extension);
	}

	$self->{'log'}->error("Uknown value type: $valueType");
	return (undef, undef, undef, undef);
}

sub savePatientDocument {
	my $self = shift;
	my ($dbh, $mrNo, $visitId, $image, $linkUrl, $docType, $mimeType, $extn, $docName, $obsTime) = @_;

	my $docId = $self->saveGenericDocument($dbh, $image, $linkUrl, $docType, $mimeType, $extn);

	if (not(defined $mrNo) && defined($visitId)) {
		# find the mr no from the visit ID itself
		$mrNo = ($dbh->selectrow_array(
				"SELECT mr_no FROM patient_registration WHERE patient_id=?", {}, $visitId))[0];
	}

	my $sth = $dbh->prepare(qq{INSERT INTO patient_general_docs
		(username, doc_name, doc_date, doc_id, patient_id, mr_no)
		VALUES (?, ?, to_date(?, 'YYYYMMDDHH24MISS'), ?, ?, ?)
		});

	$sth->execute($self->{userId}, $docName, $obsTime, $docId, $visitId||'', $mrNo);
}

sub saveTestDocument {
	my $self = shift;
	my ($dbh, $prescId, $image, $linkUrl, $docType, $mimeType, $extn, $docName, $obsTime) = @_;

	my $docId = $self->saveGenericDocument($dbh, $image, $linkUrl, $docType, $mimeType, $extn);

	my $sth = $dbh->prepare(qq{INSERT INTO test_documents
		(username, doc_name, doc_date, doc_id, prescribed_id)
		VALUES(?, ?, to_date(?, 'YYYYMMDDHH24MISS'), ?, ?)
		});

	$sth->execute($self->{userId}, $docName, $obsTime, $docId, $prescId);
}

sub saveGenericDocument {
	my $self = shift;
	my ($dbh, $image, $linkUrl, $docType, $mimeType, $extn) = @_;

	my $docId = ($dbh->selectrow_array("SELECT nextval('patient_documents_seq')"))[0];

	my $sth = $dbh->prepare(qq{INSERT INTO patient_documents
		(doc_content_bytea, doc_location, doc_id, doc_type, content_type, doc_format, original_extension)
		VALUES (?, ?, ?, ?, ?, ?, ?)
		});

	my $format = ($image) ? 'doc_fileupload' : 'doc_link';
	$sth->bind_param(1, $image, {pg_type => PG_BYTEA});
	$sth->execute($image, $linkUrl, $docId, $docType, $mimeType, $format, $extn);

	return $docId;
}


sub decodeResultText {
        my ($result) = @_;
        $result =~ s/\\X0D\\/<br\/>/;
        $result =~ s/\\X0A\\/<br\/>/;
        $result =~ s/\\S\\/^/;
        $result =~ s/\\T\\/&/;
        $result =~ s/\\R\\/~/;
        $result =~ s/\\H\\/<b>/;
        $result =~ s/\\N\\/<\/b>/;
        $result =~ s/\\F\\/|/;
        
        return $result;
}


=comment

\H\  start highlighting  			:  <b>

\N\  normal text (end highlighting) :  </b>

\F\ field separator    				:	|   

\S\ component separator   			:	^      

\T\ subcomponent separator   		:	&

\R\ repetition separator  			:	~

\E\  escape character  				:	\\


\Xdddd...\   hexadecimal data

\Zdddd...\    locally defined escape sequence

=cut



1;

