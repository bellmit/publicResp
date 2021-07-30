package Insta::ServiceImport;

use strict;
use warnings;
use DBI;
use LWP;
use HTTP::Request::Common;
use HTTP::Cookies;
use POSIX qw(strftime);
use MIME::Base64;
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
	print "processMessage ...\n";
	my $self = shift;
	my ($ifDesc, $msg) = @_;
	my $ifName = $ifDesc->{ifName};
print "\n$ifDesc->{interface_name} $ifDesc->{set_completed_status} \n";
	my $dbh = DBI->connect("dbi:Pg:dbname=".$self->{db}.";host=".$self->{host}.";port=".$self->{db_port}.";", $self->{db_username}, $self->{db_password},
		{AutoCommit => 0, RaiseError =>1});
	$dbh->do("SET search_path TO ". $self->{schema});
	$dbh->do("SET application.username TO '_system'");

	my @tests = ();		# to store all the tests that are affected by this message
	my %pushed = ();
	my $lastError = "";

	# Group all segments with the same obsSubId
	my $obxIds = [];
	my $segsForId;
	my $prvsSubId = -1;		# start with invalid one so that first iter hits a difference
	my $prvsValueType = '';
	my $obsValues = '';

	foreach my $seg (@{$msg->{OBXs}}) {
		if (not($seg->{obsSubId}) || $seg->{obsSubId} != $prvsSubId
			|| ($seg->{valueType}||'NM' ne $prvsValueType)) {
			# not same as previous, start a new segsForId
			$segsForId = [];

			my $obsValue =  $seg->{obsValue} || '<br>';
			$obsValues = $obsValues . $obsValue;
			push @$obxIds, $segsForId;
		}
		push @$segsForId, $seg;
		$prvsSubId = $seg->{obsSubId}; $prvsValueType = $seg->{valueType}||'NM';
	}

	if($ifDesc->{consolidate_multiple_obx} eq 'Y') {
		@$obxIds[0]->[0]->{obsValue}=$obsValues;
	}
	#
	# Process each segment list for the same obs sub id
	#
	my $count = 0;
	foreach my $segs (@$obxIds) {
		my $valueType = $segs->[0]{valueType}||'NM';

#		print "\n".$segs->[0]->{sid}."=". $segs->[0]->{obsValue} || '';
#		print "\n\n";
	
		if($ifDesc->{consolidate_multiple_obx} eq 'Y' && $count > 0) {
			last;
		}
		$count++;
		my ($errMsg, $test);
		# This includes TX messages also, since report now comes as TX message for RadSpa and Infinitt
		print "processing document .....\n";
		($errMsg, $test) = $self->processDocumentSegment($dbh, $ifDesc, $msg, $segs, $ifDesc->{set_completed_status});

#		if ($errMsg) {
#			$lastError = $errMsg;
#			last;
#		}
#
#		if ($test) {
#			my $presId = $test->{presId};
#			unless ($pushed{$presId}) {
#				push (@tests, $test);
#				$pushed{$presId} = 1;
#			}
#		}
	}
#
#	if ($lastError) {
#		print "Rolling back due to error: $lastError\n";
#		$dbh->rollback();
#		$dbh->disconnect();
#		return $lastError;
#	}
#
#	# Now, for each test in the list, do the following:
#	# 1. Set conducted flag as Y in tests_prescribed, provided all results are done
#	# 2. Create a report (one row in test_visit_reports) if it doesn't exist
#	# TODO: if conduction format is not V, then we could deal with this differently
#	foreach my $test (@tests) {
#		print "setting conduction status......\n";
#		$self->setConductedFlag($dbh, $test->{presId}, $ifDesc->{set_completed_status}, $test->{testId});
#		print "getting report........\n";
#		my $reportId = $self->createOrGetReportId(
#			$dbh, $test->{visitId}, $test->{presId}, $ifDesc->{report_group_method});
#	}
#
#
#
#

	 my $orderNum = getPlacerOrderNum($msg, 0);

	$self->setConductedFlag($dbh, $orderNum, $ifDesc->{set_completed_status});

	$dbh->commit();
	$dbh->disconnect();
	print "Done processing message ......\n";
	return $lastError;
}


sub processDocumentSegment {
	my $self = shift;
	my ($dbh, $ifDesc, $msg, $segs, $setStatus) = @_;
	print "processing document segment....\n";
	my $seg = $segs->[0];

	my $orderNum = getPlacerOrderNum($msg, 0);     # [123,InstaHMS]


#	$orderNum = $orderNum->[0] if ($orderNum);
	my $obsTime = $seg->{obsTime} || $msg->{OBR}{obsTime} || strftime("%Y%m%d%H%M%S", localtime);
	my $docName = $seg->{obsId}[1];
	my $docType = 'SYS_ST'; # hard-coded to this, earlier the message was sending SYS_LR in units fields seg->{units}

	my $prescId = $orderNum; 

	my ($image, $url, $mime, $extn) = $self->getDocumentData($segs, $ifDesc);
	unless ($image || $url) {
		return ('No document/pointer found', undef);
	}
	print "got document data....\n";

	if ($prescId) {
		print "saving service document .....\n";
		# store image against the test (cannot store hyperlink against test)
		print "INFO: storing image against order: $prescId\n";
		# This saves a record in patient_documents and one in test_documents table
		$self->saveServiceDocument($dbh, $prescId, $image, $url, $docType, $mime, $extn, $docName, $obsTime, $setStatus);

	} 
	return (undef, undef);		# since no reports are affected, need not return the test.
}






sub setConductedFlag {
	my $self = shift;
	my ($dbh, $presId, $setStatus) = @_;

	# if set_completed status set to 'S', signed off, will reset to 'C' as we don't have singed off state for services
	if ($setStatus eq 'S') {
		$setStatus = 'C';
	}

	$dbh->do(qq{UPDATE services_prescribed SET conducted=? WHERE prescription_id=?}, undef,
		$setStatus, $presId);
	print "Setting conducted status for presId $presId as $setStatus\n";
}

sub createOrGetReportId {
	my $self = shift;
	my ($dbh, $visitId, $presId, $reportGroupMethod) = @_;

	my $reportId;

	#
	# Get the prescId details, including report_id. If report is already made for this test.
	#
	print "getting the report id for the test .....\n";
	my $prescDetails = $dbh->selectrow_hashref(qq{SELECT report_id, ddept_id, ddept_name, common_order_id
		FROM tests_prescribed
			JOIN diagnostics USING (test_id)
			JOIN diagnostics_departments USING (ddept_id)
		WHERE prescribed_id=?}, {}, $presId);

	$reportId = $prescDetails->{report_id};
	if ($reportId) {
		print "Found existing report ID: ", $reportId, "\n";
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
		print "No report group method is set, which means it will go attached to any existing report.\n";
		$deptReport = $dbh->selectrow_hashref(qq{SELECT report_id
			FROM tests_prescribed
				JOIN test_visit_reports using (report_id)
			WHERE pat_id=? AND signed_off='N'}, {},
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
		my $namePrefix = "RR-" . $prescDetails->{ddept_name} . "-";
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
		print "Creating new report: id=$reportId, name=$reportName\n";

		# insert the report, with no report_data
		my $sth = $dbh->prepare(qq{
			INSERT INTO test_visit_reports
			(report_id, patient_id, report_name, category, report_date, user_name, report_mode)
			VALUES  (?, ?, ?, ?, current_timestamp, ?, 'P')
			});

		$sth->execute($reportId, $visitId, $reportName, 'DEP_RAD', $self->{userId});
	}

	# update the order with the new report Id
	my $sth = $dbh->prepare("UPDATE tests_prescribed SET report_id=?, user_name=? WHERE prescribed_id=?");
	$sth->execute($reportId, $self->{userId}, $presId);

	# Set the report_results_severity_status based on severity
	print "setting report severity... should be A always for radiology reports\n";
	$sth = $dbh->prepare(qq{UPDATE test_visit_reports tvr
		SET report_results_severity_status = CASE WHEN
			(EXISTS (SELECT * FROM test_details td JOIN tests_prescribed tp USING (prescribed_id)
				WHERE tp.report_id = tvr.report_id AND td.withinnormal != 'Y'))
			THEN 'H' ELSE 'A' END
		WHERE tvr.report_id = ?
		});
	$sth->execute($reportId);
	print "new report id : $reportId\n";
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
		return ($image, $urlLink, $type, $subType);

	## added TX type to accomodate inline text report for radiology

	} elsif ($valueType eq 'TX') {
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
			$/ = undef;
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

sub saveServiceDocument {
	my $self = shift;
	my ($dbh, $prescId, $image, $linkUrl, $docType, $mimeType, $extn, $docName, $obsTime, $setStatus) = @_;
	my $signedoff='false';
    if ($setStatus eq 'S') {
        $signedoff = 'true';
    }


	my $docId = $self->saveGenericDocument($dbh, $image, $linkUrl, $docType, $mimeType, $extn);

	my $sth = $dbh->prepare(qq{INSERT INTO service_documents
		(username, doc_name, doc_date, doc_id, prescription_id, signed_off)
		VALUES(?, ?, to_date(?, 'YYYYMMDDHH24MISS'), ?, ?, ?)
		});

	$sth->execute($self->{userId}, $docName, $obsTime, $docId, $prescId, $signedoff);
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
        $result =~ s/\\X0D\\/<br\/>/g;
        $result =~ s/\\X0A\\/<br\/>/g;
        $result =~ s/\\S\\/^/g;
        $result =~ s/\\T\\/&/g;
        $result =~ s/\\R\\/~/g;
        $result =~ s/\\H\\/<b>/g;
        $result =~ s/\\N\\/<\/b>/g;
        $result =~ s/\\F\\/|/g;
        
        return $result;
}

sub getPlacerOrderNum {
 my ($msg, $placeValue) = @_;
 my $orderNum = $msg->{ORC}{placerOrderNum};
 print "\nORC placerOrderNum :", $orderNum || '';
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

