package Insta::DiagImport;

use strict;
use warnings;
use DBI;
use LWP;
use HTTP::Request::Common;
use HTTP::Cookies;
use POSIX qw(strftime);
use MIME::Base64;
use DBD::Pg qw(:pg_types);
use Time::Piece;
use File::Basename;
use lib dirname($0) . "/../../lib";
use Hl7::Templates;
use Hl7::Segment;
use REST::Client;
use JSON;
use URI::Escape;

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
my $obxIdCount = 0;
my $message = '';
my $apiCredentialsMap;
my $consAddObsValue = '';
my $mainReport = 0;
my $addendum = 0;
my $receivePreliminaryReport = '';
my $isRadiology = 0;
my $allowAddendumOverride = 0;

sub processMessage {
	my $self = shift;
	my ($ifDesc, $msg, $apiCredMap) = @_;
	$apiCredentialsMap = $apiCredMap;
	$message = $msg;
	my $ifName = $ifDesc->{interface_name};
	$receivePreliminaryReport = $ifDesc->{receive_preliminary_report};
	print "Interface Name : $ifName\n";
	my $dbh = DBI->connect("dbi:Pg:dbname=".$self->{db}.";host=".$self->{host}.";port=".$self->{db_port}.";", $self->{db_username}, $self->{db_password},
		{AutoCommit => 0, RaiseError =>1});
	$dbh->do("SET search_path TO ". $self->{schema});
	$dbh->do("SET application.username TO '_system'");

	#Get prescription details once and reuse it, instead hitting database multiple times
	my $prescDetails = getPrescrptionDetails($dbh, $msg);
	
	#Check for type of the test(Rad/Lab)
	my $itemDept = $prescDetails->{category};
	if ($itemDept eq 'DEP_RAD'){
		$isRadiology = 1;
	}

	#Check allow addendum override is Yes/No
	if ($ifDesc->{allow_addendum_override} eq 'Y'){
		$allowAddendumOverride = 1;
	}

	print "INFO :: =====Processing $itemDept message in interface $ifName =====\n";
	my @tests = ();		# to store all the tests that are affected by this message
	my %pushed = ();
	my $lastError = "";
	
	$mainReport = 0;
	$addendum = 0;
	
	# Group all segments with the same obsSubId
	my $obxIds = [];
	my $segsForId;
	my $prvsSubId = -1;		# start with invalid one so that first iter hits a difference
	my $prvsValueType = '';
	my $pv1 = $msg->{PV1};
	my $msgType = $msg->{MSH}->{msgType}[0];
	my $conductingDocId = $pv1->{attdDoctor}[0]; 
	my $orderCtrlCode = $msg->{ORC}{ctrl};
	my $conductionFormat = getPlacerOrderNum($msg, 1);
	my %keyFieldsMap = ();
	my $prevObsid_2 = '';
	my $resultSts = '';
	
	my $obr = $msg->{OBR};
	my $resultInterpreter = $obr->{principalIntrprtr};
	my $resultInterpreterSubComp = undef;
	my $test_doc_id;
	if (defined($resultInterpreter) && ref($resultInterpreter) eq 'ARRAY') {
		my $resultInterpreterComp = $resultInterpreter->[0];
		($resultInterpreterSubComp) = split(/&/, $resultInterpreterComp, 0);
	} elsif(defined($resultInterpreter)) {
		($resultInterpreterSubComp) = split(/&/, $resultInterpreter, 0);
	}
	
	my $result_report_date = undef;
	my $OBR_22_field = $obr->{resultTs};
	if(defined($OBR_22_field) && $OBR_22_field ne '') {
		my $in_fmt  = '%Y%m%d%H%M%S';
		my $out_fmt = '%Y-%m-%d %H:%M:%S';
		my $date = Time::Piece->strptime($OBR_22_field, $in_fmt);
		my $out_date = $date->strftime($out_fmt);
		
		$keyFieldsMap{'result_reported_time'} = $out_date;
	} else {
		$keyFieldsMap{'result_reported_time'} = strftime("%Y-%m-%d %H:%M:%S", localtime);
	}
	
	my $access_num;
	if (defined($ifDesc->{custom_field_1}) && $ifDesc->{custom_field_1} ne '') {
		my @parts = split(/\./, $ifDesc->{custom_field_1});
		if (ref(\@parts) eq 'ARRAY') {
			my $filler_field = $msg->{$parts[0]}->{Hl7::Templates::getSegmentTemplate($parts[0])->[$parts[1]-1]};
			if (ref($filler_field) eq 'ARRAY') {
				$access_num = $filler_field->[$parts[2]-1];
			} else {
				$access_num = $filler_field;
			}
		}
	}
	
	if (defined($resultInterpreterSubComp) && $resultInterpreterSubComp ne '' && $resultInterpreterSubComp ne $conductingDocId) {
		my $rows = $dbh->selectcol_arrayref(
		"SELECT doctor_id FROM doctors WHERE doctor_id = ?",
		{}, $resultInterpreterSubComp);
	
		if (@$rows > 0) {
			$conductingDocId = $resultInterpreterSubComp;
		} else {
			print "It seems doctor id from OBR.32 is not a valid doctor, so considering PV1.7 \n";
		}	
	}
	
	if ($ifDesc->{doctor_identifier} eq 'D') {
		$conductingDocId = undef;
	}
	
	#Instead querying we can get from the message itself, But to support old messages, which does not have conduction format in the message.
	if (!defined($conductionFormat) || (defined($conductionFormat) && $conductionFormat eq 'InstaHMS')) {
		$conductionFormat = ($dbh->selectrow_array("SELECT conduction_format FROM tests_prescribed tp 
							JOIN diagnostics d ON (d.test_id = tp.test_id) WHERE prescribed_id=?", {}, $prescDetails->{prescribed_id}))[0];
	}
	
	$keyFieldsMap{'conductionFormat'} = $conductionFormat;
	$keyFieldsMap{'access_num'} = $access_num;
	
	if ($msgType eq 'ORM') {
    # for reconduction, amendmend and revertsignedoff for LIS, we will have ORM with no OBX segments
		my ($errMsg, $test);
		my $isCancelled = isTestCancelled($prescDetails);
		if (!$isCancelled) {
			($errMsg, $test) = $self->processResultSegment($dbh, $ifDesc, $msg, [], $prescDetails, \%keyFieldsMap);		
		} else {
			print "Test with prescribed id : $prescDetails->{prescribed_id} is already cancelled, So not generating report\n";		
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
	} else {
		#Ignores processing ORU message if receive_preliminary_report is Yes and OBR.25th field is empty.
		$resultSts = $msg->{OBR}{resultSts} || '';
		if ($receivePreliminaryReport eq 'Y' && $resultSts eq '') {
			$dbh->disconnect();
			print "Message Ignored as Receive Preliminary Report Set to Yes and the OBR 25 field is Empty\n";
			return ('Message Ignored as Receive Preliminary Report Set to Yes and the OBR 25 field is Empty');
		}
		my $conObsValue = '';
		my $consAddObsValue = '';
		my $read_from_OBX = $ifDesc->{file_in_pid} eq 'N';
		if ($read_from_OBX) {
			my $addendumTitle = '';
			my $responsibleObserver = '';
			my $obsTime = '';
			my $is_GDT_ADT_msg = 0;
			foreach my $seg (@{$msg->{OBXs}}) {
				if ($seg->{obsId}[1] eq 'GDT' || $seg->{obsId}[1] eq 'ADT') {
					$is_GDT_ADT_msg = 1;
				}
				if ($is_GDT_ADT_msg && !($seg->{obsId}[1] eq 'GDT' || $seg->{obsId}[1] eq 'ADT')) {
					$dbh->disconnect();
					print "One or few of the OBX 3.2 field have improper value (GDT/ADT value missing)\n";
					return ('One or few of the OBX 3.2 field have improper value');
				}
				if (not($seg->{obsSubId}) || $seg->{obsSubId} != $prvsSubId
					|| ($seg->{valueType}||'NM' ne $prvsValueType) || 
					((defined($seg->{obsId}[1]) && $seg->{obsId}[1] ne '') ? $seg->{obsId}[1] ne $prevObsid_2 : 0)) {
					print "\$prevObsid_2: $prevObsid_2  \n";
					print "\$seg->{obsId}[1] : $seg->{obsId}[1]  \n";
					# not same as previous, start a new segsForId
					$segsForId = [];
					push @$obxIds, $segsForId;
					if ($ifDesc->{consolidate_multiple_obx} eq 'Y' && $conductionFormat eq 'T') {
						if ($seg->{obsId}[1] ne 'ADT') {
							$mainReport = 1;
							if($seg->{valueType} eq 'ED' || $seg->{valueType} eq 'RP') {
								my $obsValue = $seg->{obsValue};
								$conObsValue =  $obsValue;
							} else {
								my $obsValue = $seg->{obsValue} || '' . '<br/>';
								$conObsValue = $conObsValue . $obsValue;
							}
						} else {
							$addendum = 1;
							if($seg->{valueType} eq 'ED') {
								my $obsValue = $seg->{obsValue};
								$consAddObsValue =  $obsValue;
							} else {
								my $obsValue = $seg->{obsValue} || '' . '<br/>';
								if ($isRadiology && $allowAddendumOverride) {
									$consAddObsValue = $self->appendAddendum($consAddObsValue, $seg->{responsibleObserver}[1] || '', $seg->{obsTime} || '', $obsValue);
								} else {
									$consAddObsValue = $consAddObsValue . $obsValue;
								}
							}
						}
					}
				}
				push @$segsForId, $seg;
				$prvsSubId = $seg->{obsSubId};
				$prvsValueType = $seg->{valueType}||'NM';
				$prevObsid_2 = $seg->{obsId}[1];
			}
		} else {
			print "Read file pointer from PID \n";
			my $obsTime = $msg->{OBR}{obsTime} || strftime("%Y%m%d%H%M%S", localtime);
			my $docName = '';
			my $docType = $prescDetails->{category} eq 'DEP_LAB' ? 'SYS_LR' : 'SYS_RR';
			my $pid11 = $msg->{PID}->{addr};
			my $prescribedID = $prescDetails->{prescribed_id};
			my ($image, $url, $mime, $extn) = getRPdetailsFromPID($pid11, $ifDesc);
	
			unless ($image || $url) {
				$dbh->disconnect();
				return ('\nNo document/pointer found \n');
			}
			$resultSts = $msg->{OBR}{resultSts} || '';
			#$docName = split(/\./, $pid11, 1);
			$docName = $pid11;
			if ($prescribedID) {
				$test_doc_id = $self->saveTestDocument($dbh, $prescribedID, $image, $url, $docType, $mime, $extn, $docName, $obsTime, $resultSts);
                print "Saved Test";

                if ($resultSts eq 'F') {
                	my $sth = $dbh->prepare("UPDATE tests_prescribed SET test_doc_id=? WHERE prescribed_id=?");
					$sth->execute($test_doc_id, $prescribedID);
                }
            }
			
			$dbh->commit();
			$dbh->disconnect();
			
			return;
		}
		
		if ($ifDesc->{consolidate_multiple_obx} eq 'Y' && $conductionFormat eq 'T') {
			if ($conObsValue && $consAddObsValue) {
				if (($isRadiology && $allowAddendumOverride) && (ref @$obxIds[0]->[0]->{obsId} eq 'ARRAY') && 
					((@$obxIds[0]->[0]->{obsId}[1] eq 'GDT') || (@$obxIds[0]->[0]->{obsId}[1] eq 'ADT'))) {
					my $obxCount=0;
					foreach(@$obxIds) {
						if (@$obxIds[$obxCount]->[0]->{obsId}[1] eq 'GDT') {
							@$obxIds[$obxCount]->[0]->{obsValue} = $conObsValue;
						} else {
							@$obxIds[$obxCount]->[0]->{obsValue} = $consAddObsValue;
						}
						$obxCount=$obxCount+1;
					}
				} else {
					@$obxIds[0]->[0]->{obsValue} = $conObsValue;
					@$obxIds[1]->[0]->{obsValue} = $consAddObsValue;
				}
			} else {
				@$obxIds[0]->[0]->{obsValue} = $conObsValue || $consAddObsValue;
			}
		}
		
		my $count = 0;
		my %prescIdDocIdMap = ();
		my %reportIdMap = ();
		#
		# Process each segment list for the same obs sub id
		#
		foreach my $segs (@$obxIds) {
			my $valueType = $segs->[0]{valueType}||'NM';
			my ($errMsg, $test, $testDocId);
			
			if($ifDesc->{consolidate_multiple_obx} eq 'Y' && $conductionFormat eq 'T' 
				&& $count > 0 && !($mainReport && $addendum)) {
				last;
			} elsif ($mainReport && $addendum && (($segs->[0]->{obsId}[1] eq 'GDT' && $prescDetails->{report_id})
				|| ($segs->[0]->{obsId}[1] eq 'ADT' &&  !$prescDetails->{report_id}))) {
				next;
			}
			$count++;
			$obxIdCount++;
			my $rcvSupportingDoc = $ifDesc->{rcv_supporting_doc}; 
			if (($rcvSupportingDoc eq 'N') && ($valueType eq 'TX' || $valueType eq 'NM' || $valueType eq 'FT' || $valueType eq 'ST' || $valueType eq 'NUM')) {
				my $isCancelled = isTestCancelled($prescDetails);
				if (!$isCancelled) {
					($errMsg, $test) = $self->processResultSegment($dbh, $ifDesc, $msg, $segs, $prescDetails, \%keyFieldsMap, $apiCredMap, \%reportIdMap);
				} else {
					print "Test with prescribed id : $prescDetails->{prescribed_id} is already cancelled, So not generating report\n";
				}
			} else {
				print "Processing doc segment\n";
				($errMsg, $test, $testDocId) = $self->processDocumentSegment($dbh, $ifDesc, $msg, $segs);
				$prescIdDocIdMap{$testDocId} = $prescDetails->{prescribed_id};
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

		if((!defined($lastError)) || $lastError eq '') {
			foreach my $key (keys %prescIdDocIdMap) {
				my $value = $prescIdDocIdMap{$key};
				print "Processing Test Documents Map for sending notification with key as testDocId: $key and corresponding prescribed_id: $value\n";
				my $sth = $dbh->prepare("UPDATE tests_prescribed SET test_doc_id=? WHERE prescribed_id=?");
				$sth->execute($key, $value);
				$dbh->commit();
				sendNotification($key, 'Y');
			}
		}
		foreach my $key (keys %reportIdMap) {
			print "Sending notification for addendum reportId: $key\n";
			sendNotification($key, 'N');
		}
	}
	
	if ($lastError) {
		print "Rolling back due to error: $lastError\n";
		$dbh->rollback();
		$dbh->disconnect();
		return $lastError;
	}

	#print "Completed Status for interface $ifName\n";

	# Now, for each test in the list, do the following:
	# 1. Set conducted flag as Y in tests_prescribed, provided all results are done
	# 2. Create a report (one row in test_visit_reports) if it doesnt exist
	# TODO: if conduction format is not V, then we could deal with this differently
	
	my $chargeMap = $dbh->selectrow_hashref(qq{SELECT bac.doctor_id, bac.charge_id 
		FROM bill_activity_charge bac WHERE bac.activity_id = ? AND bac.activity_code = 'DIA'}, {}, $prescDetails->{prescribed_id});
		
	my @reportIdArray=();
	foreach my $test (@tests) {
		my $resultSts = $test->{resultSts} || '';
		my $reportId;
		if($resultSts eq 'RR' ){
			print "Since presId $test->{presId} is an External report, updating conduction status as S \n";
			$self->updateExternalReport($dbh, $test->{visitId}, $test->{presId},'S','Y');
		} else {
			my ($incompleteResults) = $self->setConductedFlag($dbh, $test, $ifDesc, $conductingDocId, $chargeMap, $resultSts, \%keyFieldsMap);		
			$reportId = $self->createOrGetReportId(
				$dbh, $test->{visitId}, $test->{presId}, $ifDesc, $test->{testId}, $msg, $incompleteResults, $prescDetails, $resultSts, \%keyFieldsMap);
			print "Report created in database:$reportId\n";
		}
		if (defined($conductingDocId) && $conductingDocId ne $chargeMap->{doctor_id}) {
			updateConductingDocId($dbh, $test->{presId}, $conductingDocId);
			print "conducting doctor Id $conductingDocId updated for prescribed id : $test->{presId}\n";
		}
		if (defined($conductingDocId) && defined($reportId)) {
			setTestVisitReportSignature($dbh, $test->{presId}, $conductingDocId, $reportId);
		}
		if (defined($consAddObsValue) && $consAddObsValue ne '' && ($mainReport && $addendum && !$prescDetails->{report_id})) {
			$self->updateTestVisitReport($dbh, $prescDetails->{prescribed_id}, decodeResultText($consAddObsValue), undef);
		}
		
		if(!defined($reportId) && defined($test_doc_id)) {
            sendNotification($test_doc_id, 'Y');
        }

		if ($ifDesc->{result_parameter_source} eq 'H' && defined($reportId) 
				 && $ifDesc->{set_completed_status} eq 'S' && $$apiCredentialsMap{'apiUser'} && $$apiCredentialsMap{'apiPwd'}) {
			if (($receivePreliminaryReport eq 'N') || (($receivePreliminaryReport eq 'Y') && ($resultSts eq 'F'))) {
				push(@reportIdArray, $reportId);
			}
		}
	}

	$dbh->commit();
	$dbh->disconnect();

	foreach my $rId (@reportIdArray) {
		sendNotification($rId, 'N');
		print "Triggered notification for report id : $rId\n";
	}

	return $lastError;
}

sub getRPdetailsFromPID {
	
	my $pid11 = shift;
	my $ifDesc = shift;
	if (defined($pid11)) {
		my $urlLink = '';
		my $type = '';
		my $subType = '';
		my $image = '';
	
		if (ref($pid11) eq 'ARRAY') {
			$urlLink =$pid11->[0];
			$type = $pid11->[1];
			$subType = $pid11->[2];
		} else {
			$urlLink = $pid11;	
		}
		$urlLink = (split '/', $urlLink)[-1];
		unless ($urlLink) {
			print "ERROR: no file name given for type RP\n";
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
				print "ERROR: no results import dir, cannot import RP $urlLink\n";
				return (undef, undef, undef, undef);
			}
			$fileName = $filesDir . "/files/" . $urlLink;
			$extension = $urlLink;
			$extension =~ s/.*\.//;
			# read the file into the image
			local $/ = undef;
			my $sts = open (IMG, $fileName);
			unless ($sts) {
				print "Unable to open $fileName: $!\n";
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
		}
		return ($image, $urlLink, $mime, $extension);
	} else {
		print "PID-11 field is empty, provide the file name to save as supporting document.\n";
		return (undef, undef, undef, undef);
	}
}

sub getPrescrptionDetails {
	my ($dbh, $msg) = @_;
	my $orderNum = getPlacerOrderNum($msg, 0);
		
	my ($prescId, $resultCode, $sampleId) = split(/\./, $orderNum, 3);
	print "Getting Details for Prescribed ID: $prescId\n";
	
	my $prescDetails = $dbh->selectrow_hashref(qq{SELECT tp.report_id, d.ddept_id, dd.ddept_name, d.test_name, tp.mr_no, dd.category,
			tp.common_order_id, tp.external_report_ready, tp.pat_id, tp.conducted, tp.prescribed_id, tp.test_id, tp.pat_id AS visit_id,
			tp.outsource_dest_prescribed_id
		FROM tests_prescribed tp
			JOIN diagnostics d USING (test_id)
			JOIN diagnostics_departments dd USING (ddept_id)
		WHERE tp.prescribed_id=?}, {}, $prescId);
		
	return $prescDetails;
}

sub isTestCancelled {
	my ($prescDetails) = @_;
	return($prescDetails->{conducted} eq 'X');
}

sub processResultSegment {
	my $self = shift;
	my ($dbh, $ifDesc, $msg, $segs, $prescDetails, $keyFieldsMap, $apiCredMap, $repIdMap) = @_;


	# assume only single segment for results, ie, not handling multiple segs with same sub ID.
	my $seg = $segs->[0];

	my $orderCtrlCode = $msg->{ORC}{ctrl};
	my $orderStatus = $msg->{ORC}{orderSts};
	#my $responseFlag = $msg->{ORC}{respFlag};	
	
	my $orderNum = getPlacerOrderNum($msg, 0);
	my $conductionFormat = $$keyFieldsMap{'conductionFormat'};
	my $resultSource = $ifDesc->{result_parameter_source};
	my ($prescId, $resultCode);
	my $isAmendedResults = 'N';
	my $normality = '';
	my $value = '';
	
	# Avoiding numeric zero and string zero, to pass, in else condition, as they are false in perl.
	if (defined($seg->{obsValue}) && ($seg->{obsValue} eq '0')) {
		$value = $seg->{obsValue};
	} else {
		$value = decodeResultText($seg->{obsValue}) || '-NA-';
	}
	my $resultSts = $msg->{OBR}{resultSts} || '';
	my $withinNormalVal = $seg->{accessChecks};
	my $units = $seg->{units};
	my $refText = $seg->{refRanges};
	if (ref($seg->{units}) eq 'ARRAY') {
		$units = $seg->{units}[0]
	}
	if (ref($seg->{refRanges}) eq 'ARRAY') {
		$refText = $seg->{refRanges}[0]
	}
	my $resultName;
	
	if ($orderNum) {
		# Fetch prescribed ID based on Order No.
		my $sampleId;
		($prescId, $resultCode, $sampleId) = split(/\./, $orderNum, 3);
	} else {
		# Order num not available: guess it based on MR No, result code.
		$resultCode = $seg->{obsId}[0];		# clinigene, powerlink uses 0,1
		$resultName = $seg->{obsId}[1];
		if (@{$seg->{obsId}} > 3) {
			$resultCode = $seg->{obsId}[3];		# istat uses 3,3 (ie, not universal code)
			$resultName = $seg->{obsId}[3];
		}
		my $obsTime = $seg->{obsTime} || $msg->{OBR}{obsTime} || strftime("%Y%m%d%H%M%S", localtime);

		my $mrNo;
		my $pid = $msg->{PID};
		unless ($pid) {
			return("Missing PID Segment, and no order number.", undef);
		}

		$mrNo = $pid->get('pidList');

		my $errMsg;
		($prescId, $errMsg) =
			$self->guessResultPrescId($dbh, $mrNo, $resultCode, $resultName, $obsTime);

		unless (defined($prescId)) {
			return ($errMsg, undef);
		}
	}
	 
	# if any revertsignedoff, reconducted or ammend order are there, then revert it from signed off with different operations.
	my $reportId = $prescDetails->{report_id};				 
	if (defined($orderCtrlCode) && defined($orderStatus)) {
		print "Order Control Code: $orderCtrlCode , Order Status: $orderStatus\n";
		if ($orderCtrlCode eq 'SC' && ($orderStatus eq 'IP' || $orderStatus eq 'RP')) {
			if ($reportId) {
				if ($orderStatus eq 'IP' || $orderStatus eq 'RP') {
					$self->processRevertSignedoff($dbh, $prescId, $reportId, $prescDetails->{pat_id});
					return(undef, undef);
				} 
			} elsif ($prescDetails->{external_report_ready} eq 'Y' && $orderStatus eq 'RP'){
			    print "Processing Revert signedoff/Amendment for Prescribed Id: $prescId\n";	
			   		 $self->updateExternalReport($dbh, $prescDetails->{pat_id}, $prescId, 'N','N');
			   		 return(undef, undef);
			} else {
				print "No order available for revert signedoff\n";
				return(undef, undef);
			}
		} 
	} elsif (defined($orderCtrlCode) && $orderCtrlCode eq 'RU') {
		# processing, if ORU is for reconducted tests or Amended results
		my $responseFlag = $msg->{ORC}{respFlag};
		my $obsResultStatus = $msg->{OBX}{obsResultSts};
		print "Observation Result Status: $obsResultStatus , Response Flag: $responseFlag\n";
		if (defined($responseFlag) && $responseFlag eq 'E') {
			$isAmendedResults = 'Y';
		} 
	} 
	if ($prescDetails->{conducted} eq 'S' && $conductionFormat eq 'V' && $resultSource eq 'M') {
		print "Order already updated and signedoff for Prescribed Id: $prescId ,Result: $resultCode\n";
		if ($isAmendedResults eq 'N') {
			return ('Order already updated and signedoff for above Prescribed Id', undef);		
		}	
	}
	my $row;
	my $errMsg;
	if ($resultSource eq 'M') {
		($row, $errMsg) = $self->getResultTestDetails($dbh, $prescId, $resultCode, $conductionFormat, $resultSource);
	} else {
		if(defined($prescDetails->{outsource_dest_prescribed_id})) {
			print "ERROR: order may belong to middle hops for prescId $prescId, result code $resultCode\n";
			return (undef, "Order may belong to middle hops");
		}

		if (!defined($prescDetails)) {
			print "ERROR: order not found for prescId $prescId, result code $resultCode\n";
			return (undef, "Order not found");
		}

	    my $resultCode;
	    # Order num not available: guess it based on MR No, result code.
			$resultCode = $seg->{obsId}[0];		# clinigene, powerlink uses 0,1
			$resultName = $seg->{obsId}[1];
			if (@{$seg->{obsId}} > 3) {
				$resultCode = $seg->{obsId}[3];		# istat uses 3,3 (ie, not universal code)
				$resultName = $seg->{obsId}[3];
			}

    	if($resultCode) {
				($row, $errMsg) = $self->getResultTestDetails($dbh, $prescId, $resultCode, $conductionFormat, $resultSource);
			}

		my @keys = keys %$row;
		my $length = scalar @keys;
		if(! defined $length || $length == 0) {
			$row = $prescDetails;
		}
	}
	unless ($row) {
		return ($errMsg, undef);
	}

	if($conductionFormat eq 'V'){
		print "resultCode: ", $resultCode || ''," Visit: ", $row->{visit_id} || ''," prescId: ", $row->{prescribed_id} || '', "\n";
    	print "Sample Date: ", $row->{sample_date} || '', "DOB: ", $row->{dateofbirth} || '', "\n";   ## Not available in row
	}
	
	if ($conductionFormat eq 'V' && $resultSource eq 'M') {	
		my $rangeQueryWithRangeForAllSetNo = qq {
			SELECT * FROM test_result_ranges where resultlabel_id = ? AND  
			(range_for_all = 'N'  AND
			((min_patient_age IS NULL OR (min_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end))::integer <= (?::date - ?::date))) 
			AND (max_patient_age IS NULL OR (?::date - ?::date) <= (max_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end))::integer)
			AND (patient_gender = 'N' OR patient_gender = ?))
			ORDER BY priority LIMIT 1;
		};
	
		my $rangeQueryWithRangeForAllSetYes = qq {
			SELECT * FROM test_result_ranges where resultlabel_id = ? AND 
			range_for_all = 'Y' ORDER BY priority LIMIT 1;
		};
	
		my $range = $dbh->selectrow_hashref($rangeQueryWithRangeForAllSetNo, {}, $row->{resultlabel_id}, 
			$row->{sample_date}, $row->{dateofbirth}, $row->{sample_date}, $row->{dateofbirth}, $row->{patient_gender});
	
		if (!($range)) {
			$range = $dbh->selectrow_hashref($rangeQueryWithRangeForAllSetYes, {}, $row->{resultlabel_id});
		}
		
		# considering reference range always, if its defined in master
		if ($range) {
			$refText = $range->{reference_range_txt};			
		}
		
		$units = $row->{units};
		$resultName = $row->{resultlabel};
		
		if (defined($withinNormalVal) && $withinNormalVal ne '') {	
			print "Within Normal value: $withinNormalVal\n";
			$normality = $withinNormalVal;		
		} else {
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
		
			}
		}	
	} elsif ($conductionFormat eq 'V' && $resultSource eq 'H') {
		my %severityMap = (
			'L' => '*',
			'H' => '#',
			'LL' => '**',
			'HH' => '##',
			'<' => '**',
			'>' => '##',
			'N' => 'Y',
			'A' => '*',
			'AA' => '**',
			'U' => '#',
			'D' => '*',
			'B' => 'Y',
			'W' => '*',
      '' => ''
		);
		
		$resultCode = $seg->{obsId}[0];		# clinigene, powerlink uses 0,1
		$resultName = $seg->{obsId}[1];
		if (@{$seg->{obsId}} > 3) {
			$resultCode = $seg->{obsId}[3];		# istat uses 3,3 (ie, not universal code)
			$resultName = $seg->{obsId}[3];
		}
		$normality = $severityMap{$seg->{abnormalFlags} || ''};
        print "Normality : $normality\n";
		if ((!defined($normality)) || $normality eq '') {
			$normality = '';
		}
			
	}
	
    if($resultSts ne 'RR'){
		if ($isAmendedResults eq 'N') {
			print"Insert/update the result value in test_details\n";
			$receivePreliminaryReport = $ifDesc->{receive_preliminary_report};
			if ($conductionFormat eq 'V') {
				$self->setTestResult($dbh, $row, $prescId, $resultName, $value, $units, $refText, $normality, $resultSource, $resultSts, $receivePreliminaryReport);
			} elsif ($conductionFormat eq 'T') {
				if($resultSts eq "A" || $seg->{obsId}[1] eq 'ADT') {
					# check status of the test, return error if addendum is not allowed    
					my $presId =  $row->{prescribed_id};
					return $self->updateTestVisitReport($dbh, $prescId, $value, $repIdMap); ## Returning (error,test)
			    } else {
					$self->setTestTemplate($dbh, $row, $prescId, $resultCode, $value, $resultSource, $seg, $resultSts, $receivePreliminaryReport);
				}
			}
		} else {
			print "Insert result value in test_details for amended results ORU\n";
			if ($conductionFormat eq 'V') {
				$self->setAmendedTestResult($dbh, $row->{mr_no}, $row->{visit_id}, $prescId, $row->{test_id},
					$row->{resultlabel_id}, $row->{resultlabel}, $value, $units, $refText, $normality, $msg);
			} elsif ($conductionFormat eq 'T') {
				$self->setAmendedTestTemplate($dbh, $row->{mr_no}, $row->{visit_id}, $prescId, $row->{test_id},
					$resultCode, $value, $msg);
			}		
		}
	}
	my $test = {visitId=>$row->{visit_id}, presId=>$prescId, testId=>$row->{test_id}, mrNo=>$row->{mr_no}, resultSts=>$resultSts};
	return ('', $test);
}


sub processDocumentSegment {
	my $self = shift;
	my ($dbh, $ifDesc, $msg, $segs) = @_;
	$receivePreliminaryReport = $ifDesc->{receive_preliminary_report};
	my $seg = $segs->[0];

	my $orderNum = getPlacerOrderNum($msg, 0);     # [123,InstaHMS]

	my $obsTime = $seg->{obsTime} || $msg->{OBR}{obsTime} || strftime("%Y%m%d%H%M%S", localtime);
	my $docName = $seg->{obsId}[1];
	my $docType = $seg->{units} || 'SYS_RR'; # hard-coded to this, earlier the message was sending SYS_LR in units fields seg->{units}
	print "docType :", $docType || '', "\n";

	my $prescId;
	my $ignoreSampleId;
	my $ignoreResultId;

	if ($orderNum) {
		($prescId, $ignoreResultId, $ignoreSampleId) = split(/\./, $orderNum, 3);
		# $prescId = $orderNum;

	} else {
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
	
    if ($ifDesc->{'append_doctor_signature'} eq 'Y' && $msg->{OBR}{ordProvider} && $msg->{OBR}{ordProvider}[1]) {
        print "appending doctor name to the report ...\n";
    	print "Order provider:$msg->{OBR}{ordProvider}[1]\n";
    	$image = $image . "<br/>" . "<br/>" . $msg->{OBR}{ordProvider}[2] . "<br/>";
    }  
	
	unless ($image || $url) {
		return ('No document/pointer found', undef);
	}

	my $resultSts = $msg->{OBR}{resultSts} || '';
	my $testDocId;
	if ($prescId) {
		# store image against the test (cannot store hyperlink against test)
		print "INFO: storing image against order: $prescId\n";
		$testDocId = $self->saveTestDocument($dbh, $prescId, $image, $url, $docType, $mime, $extn, $docName, $obsTime, $resultSts);

	} else {
		my $pid = $msg->{PID};
		my $pv1 = $msg->{PV1};
		my $mrNo = $pid->get('pidList');
		my $visitId = $pv1->get('visitNum') if $pv1;
		$docName = $msg->{OBR}{serviceId}[1];

		unless ($visitId || $mrNo) {
			print "ERROR: no visitId / mrNo: cannot save document\n";
			return (undef, undef);
		}

		if ($visitId) {
			print "INFO: storing image against visit: $visitId\n";
		} else {
			print "INFO: storing image against patient: $mrNo\n";
		}

		$testDocId = $self->savePatientDocument($dbh, $mrNo, $visitId, $image, $url, $docType, $mime, $extn,
			$docName, $obsTime);
	}

	return (undef, undef, $testDocId);		# since no reports are affected, need not return the test.
}

sub getResultTestDetails {
	my $self = shift;
	my ($dbh, $prescId, $resultCode, $conductionFormat, $resultSource) = @_;
	my $rows = undef;
	print " Fetching details for prescribed id: $prescId \n";
	
	if ($conductionFormat eq 'V') {
		$rows = $dbh->selectall_arrayref(qq{
			SELECT trm.resultlabel_id, trm.resultlabel, trm.units, trm.test_id, tp.report_id, trm.code_type, trm.result_code, trm.method_id,
				d.test_name, coalesce(tp.mr_no,isr_table.mr_no) as mr_no, tp.pat_id as visit_id, tp.prescribed_id, tp.outsource_dest_prescribed_id,
				COALESCE(pd.patient_gender, isr_table.patient_gender) AS patient_gender,
				COALESCE (sc.sample_date::date, current_date::date) as sample_date, COALESCE (pd.dateofbirth, pd.expected_dob, 
					isr_table.isr_dateofbirth, (sc.sample_date)::date - (isr_table.age_days)::integer) as dateofbirth
			FROM tests_prescribed tp
				JOIN test_results_master trm ON (trm.test_id = tp.test_id)
	   			JOIN diagnostics d ON (d.test_id = tp.test_id)
				LEFT JOIN (select isr.incoming_visit_id, isr.mr_no, isr.isr_dateofbirth,
					isr.patient_age * (CASE WHEN isr.age_unit = 'Y' THEN 365.25 
						WHEN isr.age_unit = 'M' THEN 30.43 ELSE 1 END) 
					AS age_days,patient_gender from incoming_sample_registration isr )
					AS isr_table ON (isr_table.incoming_visit_id = tp.pat_id)
				LEFT JOIN patient_details pd ON (coalesce(tp.mr_no, isr_table.mr_no) = pd.mr_no)
				LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)
			WHERE tp.prescribed_id=?
				AND coalesce(trm.hl7_export_code, trm.resultlabel_id::text)=?
		}, {Slice=>{}}, $prescId, $resultCode);
	} elsif ($conductionFormat eq 'T') {
		$rows = $dbh->selectall_arrayref(qq{
			SELECT '' AS resultlabel_id, '' AS resultlabel, '' AS units, d.test_id, tp.report_id,
				d.test_name, coalesce(tp.mr_no,isr_table.mr_no) as mr_no, tp.pat_id as visit_id, 
				tp.prescribed_id, tp.outsource_dest_prescribed_id
			FROM tests_prescribed tp
	   			JOIN diagnostics d ON (d.test_id = tp.test_id)
				LEFT JOIN incoming_sample_registration isr_table ON (isr_table.incoming_visit_id = tp.pat_id)
				LEFT JOIN patient_details pd ON (coalesce(tp.mr_no, isr_table.mr_no) = pd.mr_no)
			WHERE tp.prescribed_id=?				
		}, {Slice=>{}}, $prescId);
	}

	if(defined($rows->[0]->{outsource_dest_prescribed_id})) {
		print "ERROR: order may belong to middle hops for prescId $prescId, result code $resultCode\n";
		return (undef, "Order may belong to middle hops");
	}

	if (@$rows < 1) {
		print "ERROR: order not found for prescId $prescId, result code $resultCode\n";
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
		print "ERROR: no orders available for $mrNo - $resultCode [$resultName]\n";
		my $msg = $self->getGuessDiagnostics($dbh, $mrNo, $obsTime);
		return (undef, $msg);
	}

	if (@$rows > 1) {
		my $msg = "Multiple orders found for $mrNo - $resultCode";
		print "ERROR: $msg\n";
		foreach my $row (@$rows) {
			printf "  Result: %s, Test: %s, Pres ID: %s\n", 
				$row->{resultlabel}, $row->{test_name},
				$row->{prescribed_id};
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
		print "ERROR: no orders available for $mrNo - $serviceId [$serviceName]\n";
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
		print "INFO :: Message : $msg\n";
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
		print " Order: ", $row->{prescribed_id}, ", ", $row->{pres_date}, ", ",
			$row->{test_id}, ", ", $row->{test_name}, "\n";
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
		print " Charge ", $row->{date}, ", ", $row->{bill_no}, ", ", $row->{act_description}, "\n";
	}
	return "No orders available";
}

sub processRevertSignedoff {
	my $self = shift;
	my ($dbh, $presId, $reportId, $visitId) = @_;
	print "Processing revert signedoff for Report Id: $reportId and Prescribed Id: $presId\n";
	
	my $rows = $dbh->selectcol_arrayref(
	"SELECT test_details_id FROM test_details WHERE prescribed_id=?",
	{}, $presId);

	if (@$rows == 0) {
		print "No records found for presId: $presId\n";
	} else {	
		my $sth = $dbh->prepare(qq{DELETE from test_details WHERE prescribed_id = ?});
		$sth->execute($presId);
		$sth = $dbh->prepare(qq{DELETE from tests_conducted WHERE prescribed_id = ?});
		$sth->execute($presId);
		$sth = $dbh->prepare(qq{DELETE from test_visit_reports WHERE report_id = ?});
		$sth->execute($reportId);
		$sth = $dbh->prepare(qq{UPDATE tests_prescribed set conducted = 'N', report_id = ? WHERE prescribed_id = ?});
		$sth->execute(undef, $presId);	
		
		# mark conduted staus N for all connected hops
		copyDataToMultipleChain($dbh, $visitId, 'source_test_prescribed_id', 'N');	
	}	    
}

#TODO: Wecan move all parameters into one hash

sub setTestResult {
	my $self = shift;
	my ($dbh, $resultDetails, $presId, $resultLabel, $resultValue, $units, $refRanges, $abnormalFlag, $resultSource, $resultSts, $receivePreliminaryReport, $keyFieldsMap) = @_;
	my $rows = undef;
	my $resultLabelId = $resultDetails->{resultlabel_id};
	my $mrNo = $resultDetails->{mr_no};
	my $patientId = $resultDetails->{visit_id};
	my $testId = $resultDetails->{test_id};
	if ($resultSource eq 'M') {
		$rows = $dbh->selectcol_arrayref(
			"SELECT resultlabel_id FROM test_details WHERE prescribed_id=? AND resultlabel_id=?",
			{}, $presId, $resultLabelId);
	} else {
		$rows = $dbh->selectcol_arrayref(
			"SELECT resultlabel FROM test_details WHERE prescribed_id=? AND resultlabel=?",
			{}, $presId, $resultLabel);
	}
	if (defined($rows) && @$rows > 0) {
		my $existingReportStatus = ($dbh->selectrow_hashref("SELECT result_status,signed_off FROM test_visit_reports tvr 
			LEFT JOIN tests_prescribed tp ON tvr.report_id=tp.report_id WHERE tp.prescribed_id=?",{},$presId))[0];
		if (($receivePreliminaryReport eq 'Y') && ($existingReportStatus->{result_status} eq 'P') && ($existingReportStatus->{signed_off} eq 'N')) {
			my $concatRemarks = '';	
			foreach my $nte (@{$message->{'NTE'.$obxIdCount}}) {
				$concatRemarks = $concatRemarks . $nte->{comment} . '<br/>';
			}
			print "Updating test result ($resultLabel = $resultValue; $abnormalFlag) for presId $presId\n";
			my $codes = $dbh->selectrow_hashref("SELECT code_type, result_code, method_id FROM test_results_master
				WHERE resultlabel_id=?", {}, $resultLabelId);
			my $testDetailsId = ($dbh->selectrow_array("SELECT test_details_id FROM test_details 
				WHERE patient_id=? AND prescribed_id=? AND resultlabel=?",{},$patientId,$presId,$resultLabel))[0];
			
			my $sth = $dbh->prepare(qq{UPDATE test_details set resultlabel=?, report_value=?, units=?, 
				reference_range=?, withinnormal=?,comments=?,user_name=?,mr_no=?, patient_id=?, 
				prescribed_id=?, test_id=?, resultlabel_id=?, code_type=?, result_code=?,
				method_id=? WHERE test_details_id=?});

			$sth->execute($resultLabel, $resultValue, $units||'', $refRanges||'', $abnormalFlag,$concatRemarks, 
				$self->{userId},$mrNo, $patientId, $presId, $testId, $resultLabelId, 
				$resultDetails->{code_type}, $resultDetails->{result_code}, 
				$resultDetails->{method_id}, $testDetailsId);
			$self->updateTestVisitReportStatus($dbh,$resultSts,$presId);
		} else {
			print "Not updating test result, exists: $resultLabel for presId: $presId\n";
		}
	} else {
		my $concatRemarks = '';
		foreach my $nte (@{$message->{'NTE'.$obxIdCount}}) {
			$concatRemarks = $concatRemarks . $nte->{comment} . '<br/>';
		}
		print "Inserting test result ($resultLabel = $resultValue; $abnormalFlag) for presId $presId\n";
		my $codes = $dbh->selectrow_hashref("SELECT code_type, result_code, method_id FROM test_results_master
			WHERE resultlabel_id=?", {}, $resultLabelId);
		my $sth = $dbh->prepare(qq{
			INSERT INTO test_details (resultlabel, report_value, units, reference_range, withinnormal,
				conducted_in_reportformat, comments,user_name,
				mr_no, patient_id, prescribed_id, test_id, resultlabel_id, code_type, result_code,
				test_detail_status, method_id)
			VALUES (?,?,?,?,?,
				'N',?,?,
				?,?,?,?,?,?,?,'P',?)
		});
		$sth->execute($resultLabel, $resultValue, $units||'', $refRanges||'', $abnormalFlag,
			$concatRemarks, $self->{userId},
			$mrNo, $patientId, $presId, $testId, $resultLabelId, $resultDetails->{code_type}, $resultDetails->{result_code}, 
			$resultDetails->{method_id});
	}

}


sub setTestTemplate {
	my $self = shift;
	my ($dbh, $row, $presId, $formatID, $reportFile, $resultSource, $seg, $resultSts, $receivePreliminaryReport) = @_;
	my $rows = $dbh->selectcol_arrayref(
		"SELECT format_name FROM test_details WHERE prescribed_id=?" ,
		{}, $presId);

	if (@$rows > 0) {
		my $existingReportStatus = ($dbh->selectrow_hashref("select result_status,signed_off from test_visit_reports tvr 
			LEFT JOIN tests_prescribed tp ON tvr.report_id=tp.report_id where tp.prescribed_id=?",{},$presId))[0];
		if (($receivePreliminaryReport eq 'Y') && ($existingReportStatus->{result_status} eq 'P') && ($existingReportStatus->{signed_off} eq 'N')) {
			print "Updating test template for presId $presId\n";
			if ($resultSource eq 'M') {
				my $templateMasterRow = $dbh->selectrow_hashref("SELECT format_name FROM test_template_master
					WHERE test_id=?", {}, $row->{test_id});
				$formatID = $templateMasterRow->{format_name};
			} elsif ($resultSource eq 'H') {
				$formatID = 'FORMAT_EXTERNAL';
			}
			my $concatRemarks = '';
			foreach my $nte (@{$message->{'NTE'.$obxIdCount}}) {
				$concatRemarks = $concatRemarks . $nte->{comment} . '<br/>';
			}
			my $testDetailsId = ($dbh->selectrow_array("SELECT test_details_id FROM test_details WHERE patient_id=? AND prescribed_id=?",{},$row->{visit_id},$presId))[0];
			
			my $sth = $dbh->prepare(qq{
				UPDATE test_details SET format_name=?, patient_report_file=?, comments=?, user_name=?,
				mr_no=?, patient_id=?, prescribed_id=?, test_id=? WHERE test_details_id=?
			});
			$sth->execute($formatID, $reportFile, $concatRemarks, $self->{userId}, $row->{mr_no}, $row->{visit_id}, $presId, $row->{test_id},$testDetailsId);
			$self->updateTestVisitReportStatus($dbh,$resultSts,$presId);
		} else {
			print "Not updating template, exists: $formatID for presId: $presId\n";
		}
	} else {
		print "INFO :: Inserting test template for presId $presId\n";
		if ($resultSource eq 'M') {
			my $templateMasterRow = $dbh->selectrow_hashref("SELECT format_name FROM test_template_master
				WHERE test_id=?", {}, $row->{test_id});
			$formatID = $templateMasterRow->{format_name};
		} elsif ($resultSource eq 'H') {
			$formatID = 'FORMAT_EXTERNAL';
		}
		my $concatRemarks = '';
		foreach my $nte (@{$message->{'NTE'.$obxIdCount}}) {
			$concatRemarks = $concatRemarks . $nte->{comment} . '<br/>';
		}
		

		my $sth = $dbh->prepare(qq{
			INSERT INTO test_details (format_name, patient_report_file, conducted_in_reportformat, comments, user_name,
				mr_no, patient_id, prescribed_id, test_id, test_detail_status)
			VALUES (?,?,'Y',?,?,?,?,?,?,'P')
		});
		$sth->execute($formatID, $reportFile, $concatRemarks, $self->{userId}, $row->{mr_no}, $row->{visit_id}, $presId, $row->{test_id});
	}

}

sub updateTestVisitReport{
	my $self = shift;
	my ($dbh,$presId,$value,$reportIds) = @_;
	my $rows = $dbh->selectall_arrayref(qq{select tvr.signed_off,tvr.addendum_signed_off,tvr.report_id from tests_prescribed tp
	JOIN test_visit_reports tvr ON (tp.report_id= tvr.report_id) WHERE tp.prescribed_id=? AND tvr.signed_off = 'Y'}, 
	{Slice=>{}}, $presId);

	if (@$rows < 1) {	
		print "ERROR :: order not found or not signedoff for prescId: $presId \n";
		return ("Order not found or not signedoff for prescId: $presId", "Order not found");
	} else {
		foreach my $row (@$rows) {
			if('Y' ne $row->{signed_off}) {
				print "ERROR: Report is not signed-off \n";
				return (undef, "Report not signed off");
			} elsif ('N' ne $row->{addendum_signed_off}) {
				print "ERROR: addendum is already signed-off \n";
				return (undef, "Addendum signed off");
			} else {
				#update test_visit report
				my $sth;
				if ($isRadiology && $allowAddendumOverride) {
					print "Test is a radiology test and allowing multiple addendums, So not signing off the addendum\n";
					$sth = $dbh->prepare("UPDATE test_visit_reports SET report_addendum=? WHERE report_id=?");
				} else {
					$sth = $dbh->prepare("UPDATE test_visit_reports SET report_addendum=?,addendum_signed_off='Y' WHERE report_id=?");
				}
				$sth->execute($value,$row->{report_id});
				print "INFO :: Updated test visit report for report_id:$row->{report_id}\n";
				$reportIds -> {$row->{report_id}} = $row->{report_id};
			}
		}
	}
}

sub setAmendedTestResult {
	my $self = shift;
	my ($dbh, $mrNo, $patientId, $presId, $testId, $resultLabelId,
		$resultLabel, $resultValue, $units, $refRanges, $abnormalFlag, $msg) = @_;
	print "Processing Amended ORU for result: $resultLabelId\n";	
	my $amendReason = $msg->{NTE}{comment};
	my $obsResultStatus = $msg->{OBX}{obsResultSts};
	my $testDetailId;
	my $amendTestDetailId;
	my $codes = $dbh->selectrow_hashref("SELECT code_type, result_code, method_id FROM test_results_master
		WHERE resultlabel_id=?", {}, $resultLabelId);			
	my $rows = $dbh->selectcol_arrayref(
		"SELECT resultlabel_id FROM test_details td WHERE prescribed_id=? AND resultlabel_id=? 
			AND (td.report_value != '' OR td.report_value IS NOT NULL) AND test_detail_status IN ('P', 'S')",
		{}, $presId, $resultLabelId);

	if (@$rows > 0) {
		print "Not updating test result, exists: $resultLabel for presId $presId\n";
	}
	if ($obsResultStatus eq 'C') {
		print "Inserting test result ($resultLabel = $resultValue; $abnormalFlag) for presId $presId\n";
		$testDetailId = ($dbh->selectrow_array("SELECT nextval('test_details_seq')"))[0];
		my $sth = $dbh->prepare(qq{
			INSERT INTO test_details (test_details_id, resultlabel, units, reference_range, withinnormal,
				conducted_in_reportformat, comments,user_name,
				mr_no, patient_id, prescribed_id, test_id, resultlabel_id, code_type, result_code,
				test_detail_status, method_id, report_value)
			VALUES (?,?,?,?,?,
				'N','',?,
				?,?,?,?,?,?,?,'P',?,?)
		});
		$sth->execute($testDetailId, $resultLabel, $units||'', $refRanges||'', $abnormalFlag,
			$self->{userId},
			$mrNo, $patientId, $presId, $testId, $resultLabelId, $codes->{code_type}, $codes->{result_code}, 
			$codes->{method_id}, $resultValue);
		
		my $testDetails = $dbh->selectrow_hashref(qq{SELECT test_details_id FROM test_details
			WHERE prescribed_id=? AND resultlabel_id=? AND test_detail_status='A' ORDER BY test_details_id DESC LIMIT 1}, {},
				$presId, $resultLabelId);
		
		$amendTestDetailId = $testDetails->{test_details_id};
										
	} elsif($obsResultStatus eq 'D') {
		$amendTestDetailId = ($dbh->selectrow_array("SELECT nextval('test_details_seq')"))[0];
		print "Inserting Amended value for test result ($resultLabel = $resultValue; $abnormalFlag) for presId $presId\n";

		my $sth = $dbh->prepare(qq{
			INSERT INTO test_details (test_details_id, resultlabel, units, reference_range, withinnormal,
				conducted_in_reportformat, comments,user_name,
				mr_no, patient_id, prescribed_id, test_id, resultlabel_id, code_type, result_code,
				test_detail_status, method_id, report_value, amendment_reason)
			VALUES (?,?,?,?,?,
				'N','',?,
				?,?,?,?,?,?,?,'A',?,?,?)
		});
		$sth->execute($amendTestDetailId, $resultLabel, $units||'', $refRanges||'', $abnormalFlag,
			$self->{userId},
			$mrNo, $patientId, $presId, $testId, $resultLabelId, $codes->{code_type}, $codes->{result_code}, $codes->{method_id},
			$resultValue, $amendReason);				
	}
	
	print "updating original test details id for result: $resultLabelId\n";
	my $sth = $dbh->prepare(qq{UPDATE test_details set original_test_details_id = ? 
				WHERE prescribed_id=? AND resultlabel_id=? AND test_detail_status IN ('P', 'S')});
	$sth->execute($amendTestDetailId, $presId, $resultLabelId);				
}

sub setAmendedTestTemplate {
	my $self = shift;
	my ($dbh, $mrNo, $patientId, $presId, $testId, $formatID, $reportFile, $msg) = @_;
	print "Processing Amended ORU for Prescribed Id: $presId\n";	
	my $amendReason = $msg->{NTE}{comment};
	my $obsResultStatus = $msg->{OBX}{obsResultSts};
	my $testDetailId;
	my $amendTestDetailId;
	
	my $rows = $dbh->selectcol_arrayref(
		"SELECT resultlabel_id FROM test_details td WHERE prescribed_id=? 
			AND (td.report_value != '' OR td.report_value IS NOT NULL) AND test_detail_status IN ('P', 'S')",
		{}, $presId);

	if (@$rows > 0) {
		print "Not updating template, exists: $formatID for presId: $presId\n";
	} 
	if ($obsResultStatus eq 'C') {
		print "Inserting test details for presId $presId\n";
		$testDetailId = ($dbh->selectrow_array("SELECT nextval('test_details_seq')"))[0];
		my $sth = $dbh->prepare(qq{
			INSERT INTO test_details (test_details_id, format_name, conducted_in_reportformat, comments, user_name,
				mr_no, patient_id, prescribed_id, test_id, test_detail_status, patient_report_file)
			VALUES (?, ?,'Y','',?,?,?,?,?,'P',?)
		});
		$sth->execute($testDetailId, $formatID, $self->{userId}, $mrNo, $patientId, $presId, $testId, $reportFile);

		my $testDetails = $dbh->selectrow_hashref(qq{SELECT test_details_id FROM test_details
			WHERE prescribed_id=? AND test_detail_status='A' ORDER BY test_details_id DESC LIMIT 1}, {},
				$presId);
		
		$amendTestDetailId = $testDetails->{test_details_id};
		
	} elsif($obsResultStatus eq 'D') {
		print "Inserting Amended value for test details for presId $presId\n";
		$amendTestDetailId = ($dbh->selectrow_array("SELECT nextval('test_details_seq')"))[0];
		my $sth = $dbh->prepare(qq{
			INSERT INTO test_details (test_details_id, format_name, conducted_in_reportformat, comments, user_name,
				mr_no, patient_id, prescribed_id, test_id, test_detail_status, patient_report_file, amendment_reason)
			VALUES (?, ?,'Y','',?,?,?,?,?,'A',?,?)
		});
		$sth->execute($amendTestDetailId, $formatID, $self->{userId}, $mrNo, $patientId, $presId, $testId, $reportFile, $amendReason);
	}
		
	print "updating original test details id for presc Id: $presId\n";
	my $sth = $dbh->prepare(qq{UPDATE test_details set original_test_details_id = ? 
				WHERE prescribed_id=? AND test_detail_status IN ('P', 'S')});
	$sth->execute($amendTestDetailId, $presId);				
	
}

sub setConductedFlag {
	my $self = shift;
	my ($dbh, $test, $ifDesc, $conductingDocId, $chargeMap, $resultSts, $keyFieldsMap) = @_;
	my $setStatus = $ifDesc->{set_completed_status};
	my $centerId = $ifDesc->{center_id} ;
	my $presId = ${test}->{presId};
	my $testId = ${test}->{testId};
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
	
	my $conductedStatus;
	my $incompleteResults;
	my $manualReagents;
	my $resultSource = $ifDesc->{result_parameter_source};
	if ($resultSource eq 'M' && $$keyFieldsMap{'conductionFormat'} eq 'V') {
		 $incompleteResults = $dbh->selectall_arrayref(qq{
			SELECT trm.resultlabel FROM test_results_master trm 
			JOIN test_results_center trc on (trc.resultlabel_id = trm.resultlabel_id)
			LEFT JOIN test_details td ON (td.resultlabel_id=trm.resultlabel_id AND td.prescribed_id=? AND td.test_detail_status != 'A')
			WHERE trm.test_id = ? AND (trc.center_id is null or trc.center_id='0' or trc.center_id=?)
				AND (td.report_value = '' OR td.report_value IS NULL)
			}, undef, $presId, $testId, $centerId);
			
		# In case of HLM, in HIS side set completed status will be 'S' and no need to check reagent here
		if ($setStatus ne 'S') {
			$manualReagents = $dbh->selectall_arrayref(qq{
				SELECT reagent_id FROM diagnostics_reagents
				WHERE test_id=? AND consumption_method = 'M'
			}, undef, $testId);
		}
		
		if (@$incompleteResults || (defined($manualReagents) && @$manualReagents)) {
			if (@$incompleteResults) {
				print "Test is not complete; ";
			} else {
				print "Test requires manual reagent consumption; ";
			}
			$conductedStatus = 'P';
		} else {
			$conductedStatus = $setStatus;
		}
	} else {
		if ($receivePreliminaryReport eq 'Y') {
			if ($resultSts eq '') {
				$conductedStatus = $setStatus;
			} elsif ($resultSts eq 'P') {
				$conductedStatus = 'P';
			} else {
				$conductedStatus = 'S';
			}
		} else {
			$conductedStatus = $setStatus;
		}
	}
	$dbh->do(qq{UPDATE tests_prescribed SET conducted=?, custom_field_1=? WHERE prescribed_id=?}, undef,
		$conductedStatus, $keyFieldsMap->{access_num}, $presId);
	print "Setting conducted status for presId $presId as $conductedStatus\n";
	if ($conductedStatus ne 'P') {
		$dbh->do(qq{UPDATE test_details SET test_detail_status=? WHERE prescribed_id=? AND test_detail_status != 'A'}, undef,
			$conductedStatus, $presId);
	}

	# Updates conducted status in bill_charge and bill_activity_charge table.
	$self->updateActivityConductedInBillCharge($dbh,$presId);

	# a row in tests_conducted is also required, otherwise, we don't see these tests in
	# some report builders.
		
	my $isTestDetailsExist = $dbh->selectrow_hashref(qq{SELECT 1 FROM tests_conducted WHERE prescribed_id=?}, {}, $presId);
	if (!defined($isTestDetailsExist)) {
		my $sth = $dbh->prepare(qq{INSERT INTO tests_conducted
									(prescribed_id, mr_no, patient_id, test_id, conducted_date, conducted_time,
									satisfactory_status, user_name, conducted_by)
									VALUES (?, ?, ?, ?, current_timestamp, current_time, 'Y', 'auto_update', ?)});
		$sth->execute($presId, ${test}->{mrNo}, ${test}->{visitId}, $testId, ${chargeMap}->{doctor_id});
	}

	
	# update conducted_by if conducted by doctor comes in message, since this can be changed value also
	if (defined($conductingDocId) && $conductingDocId ne $chargeMap->{doctor_id}) {
		$dbh->do(qq{UPDATE tests_conducted SET conducted_by = ? WHERE prescribed_id=?}, undef,
			$conductingDocId, $presId);
	}
	
	if ($setStatus ne 'S' && $conductedStatus ne 'P' && $resultSource eq 'M') {
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
			# indicate that stock has been reduced. Next time, we won't reduce it, just in case
			# they move the status from conduction back to partial. We are safe to do this since
			# we would not mark it completed if there were any manual reagents.
			$dbh->do(qq{UPDATE tests_prescribed SET stock_reduced = true WHERE prescribed_id=?},
				undef, $presId);
		}
	}
	
	return ($incompleteResults);
}

sub updateConductingDocId {
    my ($dbh, $prescId, $conductingDocId) = @_;
	print "conducting doc id : $conductingDocId\n";
	# update doctor id that comes with the results	
	my $sth = $dbh->prepare(qq{UPDATE bill_activity_charge set doctor_id = ? WHERE activity_id = ? AND activity_code = 'DIA'});
	$sth->execute($conductingDocId, $prescId);

	$sth = $dbh->prepare(qq{UPDATE bill_charge SET payee_doctor_id = ? 
				WHERE charge_id = (SELECT bac.charge_id
						FROM bill_activity_charge bac 
						WHERE bac.activity_id=?::text AND activity_code = 'DIA')});
	$sth->execute($conductingDocId, $prescId);

}

sub setTestVisitReportSignature {
    my ($dbh, $prescId, $conductingDocId, $reportId) = @_;
    
	my $rows = $dbh->selectcol_arrayref("SELECT report_id FROM test_visit_report_signatures 
					WHERE prescribed_id=? AND report_id=?", {}, $prescId, $reportId);
    if (@$rows > 0) {
		print "Records already exist in test visit report signatures for Prescribed Id: $prescId, Report Id : $reportId\n";
	} else {
		# insert record in test visit report signature to display it in report	
 	  	print "Inserting into Visit Report Signature for Precribed Id : $prescId, Conducting Doctor : $conductingDocId\n";
		my $sth = $dbh->prepare(qq{INSERT INTO test_visit_report_signatures (report_id, prescribed_id, signed_as,
				signatory_username, doctor_id) VALUES (?, ?, 'D', null, ?)});
		$sth->execute($reportId, $prescId, $conductingDocId);	
	}     
}

sub createOrGetReportId {
	my $self = shift;
	my ($dbh, $visitId, $presId, $ifDesc, $testId, $msg, $incompleteResults, $prescDetails, $resultSts, $keyFieldsMap) = @_;
	my $reportGroupMethod = $ifDesc->{report_group_method};
	my $setStatus = $ifDesc->{set_completed_status};
	my $centerId = $ifDesc->{center_id};
	my $reportId;
	my $responseFlag = $msg->{ORC}{respFlag};
	my $dischardReportId;
	my $signOffCenter = undef;
	my $dept_category = $prescDetails->{category};
	my $report_name_prefix = $prescDetails->{category} eq 'DEP_LAB' ? 'LR-' : 'RR-';
	
	# Get singedofff center id
	my $exportItemDetails = $dbh->selectrow_hashref(qq{SELECT center_id
		FROM hl7_export_items
		WHERE item_id=? AND (item_type = 'TEST' OR item_type = 'TESTTEMPLATE')}, {}, $presId);
	
	# In case of HLM, in HIS side set completed status will be 'S' and no need to check reagent here
	my $manualReagents;
	if ($setStatus ne 'S') {
		$manualReagents = $dbh->selectall_arrayref(qq{
			SELECT reagent_id FROM diagnostics_reagents
			WHERE test_id=? AND consumption_method = 'M'
		}, undef, $testId);
	}
	my $signedOffStatus;
	if ($receivePreliminaryReport eq 'Y') {
		if ($resultSts eq 'F') {
			$signedOffStatus = 'Y';
			$signOffCenter = $exportItemDetails->{center_id};
		} else {
			$signedOffStatus = 'N';
		}
	} elsif ((defined($incompleteResults) && @$incompleteResults) || (defined($manualReagents) && @$manualReagents)) {
		print "Test is not complete; ";
		$signedOffStatus = 'N';
	} elsif($setStatus eq 'P' || $setStatus eq 'C') {
		$signedOffStatus = 'N';
	} else {
		$signedOffStatus = 'Y';
		$signOffCenter = $exportItemDetails->{center_id};
	}

	my $namePrefix;
	if ($reportGroupMethod eq 'N') {
		$namePrefix = $report_name_prefix . $prescDetails->{test_name} . "-";
	} else {
		my $toDate = ($dbh->selectrow_array("select to_char(localtimestamp(0)::date,'ddmmyy')"));
		$namePrefix = $report_name_prefix . $toDate . "-";
	}
	my $num = 1;
	my $reportName;
	my $pheaderTmplID = ($dbh->selectrow_array("SELECT pheader_template_id FROM print_templates WHERE template_type='L'"))[0];
	my $isRevisedReportExist = undef;
	$reportId = $prescDetails->{report_id};
	if ($reportId) {
		print "Found existing report ID: ", $reportId, "\n";
		print "Update signedoff status for report ID:", $reportId || '' ,", Signedoff Status:", $signedOffStatus || '', " Signoff Center: ", $signOffCenter|| '', "\n";	
		my $sth = $dbh->prepare("UPDATE test_visit_reports SET signed_off=?, signoff_center = ? WHERE report_id=?");
		$sth->execute($signedOffStatus, $signOffCenter, $reportId);			

		#in case amended ORU create a report with report state 'D'
   	    if (defined($responseFlag) && $responseFlag eq 'E') {				
			$isRevisedReportExist = $dbh->selectall_arrayref(qq{
				SELECT report_id FROM test_visit_reports WHERE revised_report_id=?
				}, undef, $reportId);
			if (@$isRevisedReportExist) {
				print "Discarded Report already exist for report id :$reportId\n";
			} else {
	   	    	$dischardReportId = ($dbh->selectrow_array("SELECT nextval('test_report_sequence')"))[0];
				my $sth = $dbh->prepare(qq{
					INSERT INTO test_visit_reports
					(report_id, patient_id, report_name, category, report_date, user_name, report_mode, signed_off, 
					pheader_template_id, report_state, signoff_center, hl7_obr_segment, result_status)
					VALUES  (?, ?, ?, ?, ?, ?, 'Y', ?, ?, 'D', ?, ?, ?)
					});			
				$sth->execute($dischardReportId, $visitId, $reportName, $dept_category, $keyFieldsMap->{'result_reported_time'}, 
						$self->{userId}, $signedOffStatus, $pheaderTmplID, $signOffCenter, $msg->{OBR}->toString,$msg->{OBR}->{resultSts});
				if ($$keyFieldsMap{'conductionFormat'} eq 'V') {
					$sth = $dbh->prepare(qq{UPDATE test_visit_reports tvr
									SET report_results_severity_status = CASE WHEN
									(EXISTS (SELECT 1 FROM test_details td JOIN tests_prescribed tp USING (prescribed_id)
										WHERE tp.report_id = tvr.report_id AND (td.withinnormal IS NOT NULL AND td.withinnormal != '') AND td.withinnormal != 'Y' LIMIT 1))
									THEN 'H' ELSE 'A' END
									WHERE tvr.report_id = ?
								});
					$sth->execute($dischardReportId);
				}
			}	   	    	
	    }
		if (defined($dischardReportId)) {
			$sth = $dbh->prepare(qq{UPDATE test_visit_reports tvr
				SET revised_report_id = ?, signed_off = ? WHERE tvr.report_id = ?
				});
			$sth->execute($reportId, $signedOffStatus, $dischardReportId);
		}
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
			WHERE pat_id=? AND ddept_id=? AND signed_off='N' AND conducted NOT IN('RAS','RBS')}, {},
				$visitId, $prescDetails->{ddept_id});
	
	} elsif ($reportGroupMethod eq 'O') {
		# find report for the same common_order_id
		$deptReport = $dbh->selectrow_hashref(qq{SELECT report_id
			FROM tests_prescribed
				JOIN test_visit_reports using (report_id)
			WHERE pat_id=? AND common_order_id=? AND signed_off='N' 
			AND conducted NOT IN('RAS','RBS')}, {},
				$visitId, $prescDetails->{common_order_id});

	} elsif ($reportGroupMethod eq 'OD') {
		# find report for the same department + order id
		$deptReport = $dbh->selectrow_hashref(qq{SELECT report_id
			FROM tests_prescribed
				JOIN diagnostics using (test_id)
				JOIN test_visit_reports using (report_id)
			WHERE pat_id=? AND ddept_id=? AND common_order_id=? AND signed_off='N' 
			AND conducted NOT IN('RAS','RBS')}, {},
			$visitId, $prescDetails->{ddept_id}, $prescDetails->{common_order_id});

	} elsif ($reportGroupMethod eq 'N') {
		# force a new report for each test
		$deptReport = undef;

	} else {
		# Add it to any report for the patient
		$deptReport = $dbh->selectrow_hashref(qq{SELECT report_id
			FROM tests_prescribed
				JOIN test_visit_reports using (report_id)
			WHERE pat_id=? AND signed_off='N' AND conducted NOT IN('RAS','RBS')}, {},
				$visitId);
	}

	if ($deptReport && $deptReport->{report_id}) {
		# found a report in the same department, use that to attach to.
		print "Found report for criterion ", $reportGroupMethod, ": ", $deptReport->{report_id}, "\n";
		$reportId = $deptReport->{report_id};

	} else {
		#
		# No existing report that the test can be attached to. Create one.
		# Create a new report for this prescribed Id. Name it based on the department
		# If the same name exists, append -1, -2 etc to the report.
		#
		my @exist;
		do {
			$reportName = $namePrefix . $num++;
			@exist = $dbh->selectrow_array(
				"SELECT report_id FROM test_visit_reports WHERE patient_id=? and report_name=?", {},
				$visitId, $reportName);
		} while (@exist > 0);
		
		# get report_id from sequence
		$reportId = ($dbh->selectrow_array("SELECT nextval('test_report_sequence')"))[0];
		print "Creating new report: id=$reportId, name=$reportName, Signedoff Status=$signedOffStatus\n";

		# insert the report, with no report_data
		my $sth = $dbh->prepare(qq{
			INSERT INTO test_visit_reports
			(report_id, patient_id, report_name, category, report_date, user_name, report_mode, 
				signed_off, pheader_template_id, signoff_center, hl7_obr_segment, result_status)
			VALUES  (?, ?, ?, ?, ?, ?, 'P', ?, ?, ?, ?, ?)
			});

		$sth->execute($reportId, $visitId, $reportName, $dept_category, $keyFieldsMap->{'result_reported_time'}, 
					$self->{userId}, $signedOffStatus, $pheaderTmplID, $signOffCenter, $msg->{OBR}->toString,$msg->{OBR}->{resultSts});
		
		#in case amended ORU create a report with report state 'D', its possible when amended file reads first
   	    if (defined($responseFlag) && $responseFlag eq 'E') {				
			$isRevisedReportExist = $dbh->selectall_arrayref(qq{
				SELECT report_id FROM test_visit_reports WHERE revised_report_id=?
				}, undef, $reportId);
			if (@$isRevisedReportExist) {
				print "Discarded Report already exist for report id :$reportId\n";
			} else {
	   	    	$dischardReportId = ($dbh->selectrow_array("SELECT nextval('test_report_sequence')"))[0];
				my $sth = $dbh->prepare(qq{
					INSERT INTO test_visit_reports
					(report_id, patient_id, report_name, category, report_date, user_name, report_mode, signed_off, 
					pheader_template_id, report_state, signoff_center, hl7_obr_segment, result_status)
					VALUES  (?, ?, ?, ?, ?, ?, 'Y', ?, ?, 'D', ?, ?, ?)
					});			
				$sth->execute($dischardReportId, $visitId, $reportName, $dept_category, $keyFieldsMap->{'result_reported_time'}, 
								$self->{userId}, $signedOffStatus, $pheaderTmplID, $signOffCenter, $msg->{OBR}->toString, $msg->{OBR}->{resultSts});
				if ($$keyFieldsMap{'conductionFormat'} eq 'V') {
					$sth = $dbh->prepare(qq{UPDATE test_visit_reports tvr
									SET report_results_severity_status = CASE WHEN
									(EXISTS (SELECT 1 FROM test_details td JOIN tests_prescribed tp USING (prescribed_id)
											WHERE tp.report_id = tvr.report_id AND (td.withinnormal IS NOT NULL AND td.withinnormal != '') AND td.withinnormal != 'Y' LIMIT 1))
									THEN 'H' ELSE 'A' END
									WHERE tvr.report_id = ?
								});
					$sth->execute($dischardReportId);
				}
			}	   	    	
	    }
		if (defined($dischardReportId)) {
			$sth = $dbh->prepare(qq{UPDATE test_visit_reports tvr
				SET revised_report_id = ?, signed_off = ? WHERE tvr.report_id = ?
				});
			$sth->execute($reportId, $signedOffStatus, $dischardReportId);
		}		
	}
   
	# update the order with the new report Id
	my $sth = $dbh->prepare("UPDATE tests_prescribed SET report_id=?, user_name=? WHERE prescribed_id=?");
	$sth->execute($reportId, $self->{userId}, $presId);
	# Set the report_results_severity_status based on severity for value based tests only
	if ($$keyFieldsMap{'conductionFormat'} eq 'V') {
		$sth = $dbh->prepare(qq{UPDATE test_visit_reports tvr
			SET report_results_severity_status = CASE WHEN
				(EXISTS (SELECT 1 FROM test_details td JOIN tests_prescribed tp USING (prescribed_id)
					WHERE tp.report_id = tvr.report_id AND (td.withinnormal IS NOT NULL AND td.withinnormal != '') AND td.withinnormal != 'Y' LIMIT 1))
				THEN 'H' ELSE 'A' END
			WHERE tvr.report_id = ?
			});
		$sth->execute($reportId);
	}

	#update tests prescribed details for all connected hops if set completed status is signedoff
	if ($setStatus eq 'S') {
		copyDataToMultipleChain($dbh, $visitId, 'source_test_prescribed_id', 'S');
	}
	return $reportId;
}


sub copyDataToMultipleChain {
	my ($dbh, $visitId, $traverseColumn, $traverseColumnValue) = @_;
	print "Visit id: $visitId\n";
	print "updating tests prescribed to all connected hops for visit id : $visitId\n";	
	# gives the source prescribed Id's of the report. 
	my $testPrescList = $dbh->selectall_arrayref(qq{
		SELECT $traverseColumn, tp.report_id
		FROM tests_prescribed tp
			 WHERE tp.pat_id = ? AND tp.conducted != 'RAS'
	}, {Slice=>{}}, $visitId);
	
	my $traverseColId;
	my $prescFlag;
	my $reportId;
	foreach my $testPresc (@$testPrescList) {
		
		if (defined($testPresc->{$traverseColumn})) {

			$traverseColId = $testPresc->{$traverseColumn};
			$reportId = $testPresc->{report_id};
			do {
				$prescFlag = 0;
				# update the source center order
				my $sth = $dbh->prepare("UPDATE tests_prescribed SET report_id=?, conducted = ? WHERE prescribed_id=?");
				$sth->execute($reportId, $traverseColumnValue, $traverseColId);
				
				print "copying report id: $reportId for $traverseColumn: $traverseColId\n";
	
				my $prescDetails = $dbh->selectrow_hashref(qq{SELECT $traverseColumn
					FROM tests_prescribed
					WHERE prescribed_id=?}, {}, $traverseColId);
	
				if ($prescDetails) {
					$traverseColId = $prescDetails->{$traverseColumn};
					$prescFlag = 1;				
				}
			} while ($prescFlag);
		}
	}
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
		return ($image, $urlLink, $type, $subType);
	} elsif ($valueType eq 'TX' || $valueType eq 'FT') {
		$urlLink = '';
		$type = 'text/html';
		$subType = 'txt';
#		print "\n=============================Before Decoding=========================\n";
#		print "$obsValue\n";
#		print "\n---------------------------><---------------------------------------\n";

        if ($obsValue) {
                $obsValueDecoded = decodeResultText($obsValue);
        }
#		print "\n===========================After decoding=============================\n";
#        print "$obsValueDecoded\n";
#		print "\n\n---------------------------------------><--------------------------------\n";
        $image = $obsValueDecoded || '-NA-';

#		my $s = sprintf "%vx", $image;
#		print "$s\n";
		return ($image, $urlLink, $type, $subType);

	} elsif ($valueType eq 'RP') {
		$urlLink = $seg->get('obsValue', 0);
		$type = $seg->get('obsValue', 2);
		$subType = $seg->get('obsValue', 3);

		unless ($urlLink) {
			print "ERROR: no file name given for type RP\n";
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
				print "ERROR: no results import dir, cannot import RP $urlLink\n";
				return (undef, undef, undef, undef);
			}
			$fileName = $filesDir . "/files/" . $urlLink;
			$extension = $urlLink;
			$extension =~ s/.*\.//;
			# read the file into the image
			local $/ = undef;
			my $sts = open (IMG, $fileName);
			unless ($sts) {
				print "Unable to open $fileName: $!\n";
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
			print "ERROR: mime-type cannot be determined for URL Link\n";
			return (undef, undef);
		}

		return ($image, $urlLink, $mime, $extension);
	}

	print "ERROR: uknown value type: $valueType\n";
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
	return $docId;
}

sub saveTestDocument {
	my $self = shift;
	my ($dbh, $prescId, $image, $linkUrl, $docType, $mimeType, $extn, $docName, $obsTime, $resultSts) = @_;
	my $docId='';
	# Overrides supporting document if receive preliminary report preference is Yes 
	my $row = ($dbh->selectrow_hashref("SELECT result_status,doc_id FROM test_documents WHERE prescribed_id=?",{},$prescId));
	if (($receivePreliminaryReport eq 'Y') && ($row->{result_status} eq 'P')) {
		$self->updateGenericDocument($dbh, $image, $linkUrl, $docType, $mimeType, $extn, $row->{doc_id});
		
		my $sth = $dbh->prepare(qq{UPDATE test_documents SET username=?, doc_name=?, doc_date=to_date(?, 'YYYYMMDDHH24MISS'), doc_id=?, result_status=?  WHERE prescribed_id=?});

		$sth->execute($self->{userId}, $docName, $obsTime, $row->{doc_id}, $resultSts, $prescId);
		$docId = $row->{doc_id};
	} else {
		$docId = $self->saveGenericDocument($dbh, $image, $linkUrl, $docType, $mimeType, $extn);

		my $sth = $dbh->prepare(qq{INSERT INTO test_documents
		(username, doc_name, doc_date, doc_id, prescribed_id, result_status)
		VALUES(?, ?, to_date(?, 'YYYYMMDDHH24MISS'), ?, ?, ?)
		});
		print "the user id is: $self->{userId}" ;
		$sth->execute($self->{userId}, $docName, $obsTime, $docId, $prescId, $resultSts);
	}
	return $docId;
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

sub updateGenericDocument {
	my $self = shift;
	my ($dbh, $image, $linkUrl, $docType, $mimeType, $extn, $docId) = @_;
	my $sth = $dbh->prepare(qq{UPDATE patient_documents SET
		doc_content_bytea=?, doc_location=?, doc_type=?, content_type=?, doc_format=?, original_extension=? WHERE doc_id=?
		});

	my $format = ($image) ? 'doc_fileupload' : 'doc_link';
	$sth->bind_param(1, $image, {pg_type => PG_BYTEA});
	$sth->execute($image, $linkUrl, $docType, $mimeType, $format, $extn, $docId);
	
	print "Updated test document for doc_id : $docId\n";
}

sub updateExternalReport {
	my $self = shift;
	my ($dbh, $visitId, $presribedId, $conductedStatus,$externalReport) = @_;
	print "updating External report changes to all connected hops for visit id : $visitId\n";	
	my $sth = $dbh->prepare("UPDATE tests_prescribed SET external_report_ready=?, conducted =? WHERE prescribed_id=?");
	$sth->execute($externalReport,$conductedStatus,$presribedId);
	
	my $testPrescList = $dbh->selectall_arrayref(qq{
		SELECT tp.source_test_prescribed_id, tp.report_id
		FROM tests_prescribed tp
			 WHERE tp.pat_id = ? AND tp.conducted != 'RAS' AND tp.prescribed_id = ? 
	}, {Slice=>{}}, $visitId, $presribedId);
	my $prescFlag;
	my $traverseColId;
	foreach my $testPresc (@$testPrescList) {
		if (defined($testPresc->{source_test_prescribed_id})) {

			$traverseColId = $testPresc->{source_test_prescribed_id};
			print " traverse prescid is $traverseColId \n";
			do {
				$prescFlag = 0;
				# update the source center order
				my $sth = $dbh->prepare("UPDATE tests_prescribed SET  conducted = ?, external_report_ready = ? WHERE prescribed_id=?");
				$sth->execute($conductedStatus,$externalReport,$traverseColId);
	
				my $prescDetails = $dbh->selectrow_hashref(qq{SELECT source_test_prescribed_id
					FROM tests_prescribed
					WHERE prescribed_id=?}, {}, $traverseColId);
	
				if ($prescDetails) {
					$traverseColId = $prescDetails->{source_test_prescribed_id};
					$prescFlag = 1;				
				}
			} while ($prescFlag);
		}
	
	}
	
}

sub getPlacerOrderNum {
    my ($msg, $placeValue) = @_;
    my $orderNum = $msg->{ORC}{placerOrderNum};
    print "ORC placerOrderNum :  $orderNum || '' \n";
    if(!(($orderNum && ref $orderNum ne 'ARRAY') || (ref $orderNum eq 'ARRAY' && @$orderNum))) {
        $orderNum = $msg->{OBR}{placerOrderNum};
        print "\nOBR placerOrderNum: ", $orderNum || '';
    }
    if ($orderNum && (ref($orderNum) eq 'ARRAY') && scalar (@$orderNum) >= $placeValue) {
        print "\nOrderNum :",@$orderNum || ''; 
        $orderNum = $orderNum->[$placeValue];
    } elsif($placeValue > 0){
		## messageFormate is not defined and expecting message formate then undef
        $orderNum = undef;
    }
    return $orderNum;
}

my $restClient = REST::Client->new();

sub loginReq {
##################################################
	# RC : Login request should not be hard coded  
	print "API Credentials: $apiCredentialsMap";
	my $login = "$$apiCredentialsMap{'apiUrl'}/Customer/Login.do";
	my $loginQueryStr = "_method=login&hospital_name=$$apiCredentialsMap{'schema'}&customer_user_id=$$apiCredentialsMap{'apiUser'}&customer_user_password=$$apiCredentialsMap{'apiPwd'}";
	my $url = $login.'?'.$loginQueryStr;
	print "Login Request : $url";
	$restClient->GET($url);
	return $restClient->responseContent();
} 

##################################################
sub notify {
##################################################
	my $queryStr = "$$apiCredentialsMap{'apiUrl'}/api/message/notification.json";
	my $isrQueryStr = undef;
	$isrQueryStr = shift;
	my $query = $queryStr."?".$isrQueryStr;
	print "Status Change URL : ". $queryStr . "\n";	
	print "Sending request to : $queryStr\n";
	$restClient->GET($query);
} 

###################################################
sub sendNotification {
###################################################
	my ($reportId, $isTestDoc) = @_;
	my @formField = ();
	my $loginReqq = loginReq();
	print "\n Value of $loginReqq";
	my $res_l =  decode_json(loginReq());
    if(!defined $res_l->{request_handler_key}) {
       	print "Please enter correct username and password for API login";
        exit 1;
    }
    print "Login Response : $res_l->{return_message}\n";
    print "Login response : " . encode_json($res_l) . "\n";
    ################# API LOGIN SUCCESSFUL ###############
    push(@formField, "request_handler_key=$res_l->{request_handler_key}");
    push(@formField, "reportId=$reportId");
    push(@formField, "isTestDoc=$isTestDoc");
    my $str = join("&",@formField);
	
    notify($str);
}

sub decodeResultText {
        my ($result) = @_;
        $result =~ s/\\X0D\\/<br\/>/g;
        $result =~ s/\\X0A\\/<br\/>/g;
        $result =~ s/\\*\.br\\*/<br\/>/g;
        $result =~ s/\\S\\/^/g;
        $result =~ s/\\T\\/&amp;/g;
        $result =~ s/\\R\\/~/g;
        $result =~ s/\\H\\/<b>/g;
        $result =~ s/\\N\\/<\/b>/g;
        $result =~ s/\\F\\/|/g;
        
        return $result;
}

sub updateTestVisitReportStatus {
	my $self = shift;
	my ($dbh,$resultStatus, $presId) = @_;
	my $reportId = ($dbh->selectrow_array("select tvr.report_id from test_visit_reports tvr 
		LEFT JOIN tests_prescribed tp ON tvr.report_id=tp.report_id where tp.prescribed_id=?",{},$presId))[0];
	my $sth = $dbh->prepare(qq{UPDATE test_visit_reports SET result_status=? WHERE report_id=?});
	$sth->execute($resultStatus,$reportId);
	print "Updated result status:$resultStatus for report id:$reportId\n";
}

sub appendAddendum {
	my $self = shift;
	my ($existingObsValue, $responsibleObserver, $obsTime, $obsValue) = @_;
	if ($obsTime ne '') {
		my $in_fmt  = '%Y%m%d%H%M%S';
		my $out_fmt = '%d-%m-%Y %H:%M:%S';
		my $date = Time::Piece->strptime($obsTime, $in_fmt);
		$obsTime = $date->strftime($out_fmt);
	}
	
	my $addendumTitle = '';				
	if ($responsibleObserver eq '' && $obsTime eq '') {
		$addendumTitle = "Addendum";
	} elsif ($responsibleObserver eq '') {
		$addendumTitle = "Addendum ".$obsTime;
	} elsif ($obsTime eq '') {
		$addendumTitle = "Addendum ".$responsibleObserver;
	} else {
		$addendumTitle = "Addendum ".$responsibleObserver." - ".$obsTime;
	}
								
	if ($existingObsValue eq '') {
		$obsValue = "<b>" .$addendumTitle . "</b><br/>" .$obsValue;
	} else {
		$obsValue = $existingObsValue . "<br/><br/><b>" .$addendumTitle . "</b><br/>" .$obsValue;
	}
	return $obsValue;
}

sub updateActivityConductedInBillCharge {
	my $self = shift;
	my ($dbh,$prescId) = @_;

	# Updates activity conducted value in bill charge and bill charge activity tables for consistency in codification screen 	
	my $sth = $dbh->prepare("UPDATE bill_activity_charge SET activity_conducted='Y' WHERE activity_code='DIA' AND activity_id=?");
	$sth->execute($prescId);

	$sth = $dbh->prepare("UPDATE bill_charge SET activity_conducted='Y' WHERE charge_id=(select charge_id from bill_activity_charge where activity_code='DIA' AND activity_id=?)");
	$sth->execute($prescId);
	print "Updated conducted status for prescription id : $prescId\n";
}

1;