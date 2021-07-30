#!/usr/bin/perl -w

use strict;
use DBI;
use POSIX qw(strftime);
use File::Basename;
use IO::Socket::INET;
use Getopt::Long;


use lib dirname($0)."/../lib";

use Hl7::Message;
use Hl7::Segment;
use Insta::Util;
use Insta::Logger;

# Database configuration

our $opt_db = 'hms';
our $opt_port = 5432;
our $opt_schema ;
our $opt_host = 'localhost';
our $opt_username = 'postgres';
our $opt_password ='';
our $opt_foreground = 0;

sub usage {
	return "Usage: $0 [OPTIONS] <schema>\n" .
	" OPTIONS: \n" .
	"  -d|--db <database>: which database to connect to (hms)\n" .
	"  -s|--schema <schema>: which schema to operate on (hostname)\n" .
	"  -h|--host <host> : datbase host\n".
	"  -U|--username <username> : database username\n".
	"  -W|--password <password> : database password\n".
	"  -o|--port <port_no> : database port no".
	"  -f|--foreground: print to console instead of logfile\n" ;
}

GetOptions( "port|o=i", "db=s", "schema=s", "host=s", "username|U=s", "password|W=s", "foreground!") or die usage();


unless ($opt_schema) {
	$opt_schema = 'hostname';
	$opt_schema =~ s/instahms-//;
}


my $util = Insta::Util->new();
my $log = Insta::Logger->new({level=>$util->getProperty('log.level'),base_name=>basename($0),db=>$opt_db,
								schema=>$opt_schema,isConsilePrint=>$opt_foreground});
$log->rotateLog(10*1024*1024);

$log->info("#####################>>>>>>>>>>>>>>>>>INIT<<<<<<<<<<<<<<<<<<<<<#################");



#
# fork the main process, so we can do our work undisturbed
#
#defined(my $pid = fork) or die "Can't fork: $!";
#exit if $pid;



#
# Create a lock file and ensure we don't run two instances of ourselves
# at any point in time.
#

my $pidfile = "/tmp/hl7_adt.$opt_db.$opt_schema.pid";

$util->isProcess($pidfile);

#
# Wait a little to ensure all the writing to the DB is in fact over
# (not needed, change from trigger based to cron based)
# sleep(2);

#
# Standard message header attributes for all ADT exports
#
my %ADT4_MSH = (
	sendApp=>'InstaHMS', 	sendFac=>'Export.ADT',
	recvApp=>'HL7IMP', 	recvFac=>'Praxify',
	msgType=>['ADT','A04'], procId=>'P'
);

my %ADT1_MSH = (
	sendApp=>'InstaHMS', 	sendFac=>'Export.ADT',
	recvApp=>'HL7IMP', 	recvFac=>'Praxify',
	msgType=>['ADT','A01'], procId=>'P'
);

my %ADT2_MSH = (
	sendApp=>'InstaHMS', 	sendFac=>'Export.ADT',
	recvApp=>'HL7IMP', 	recvFac=>'Praxify',
	msgType=>['ADT','A02'], procId=>'P'
);

my %ADT3_MSH = (
	sendApp=>'InstaHMS', 	sendFac=>'Export.ADT',
	recvApp=>'HL7IMP', 	recvFac=>'Praxify',
	msgType=>['ADT','A03'], procId=>'P'
);

my %message_type_folder = (
	A01=>'Registration',
	A02=>'Transfer',
	A03=>'Discharge'
);

my $dbh;
my $conn;
my $fh;

$log->info("Getting connection..");

$dbh = DBI->connect("dbi:Pg:dbname=$opt_db;host=$opt_host;port=$opt_port;", "$opt_username", "$opt_password",
	{AutoCommit => 1, RaiseError =>1});

$dbh->do("SET search_path TO $opt_schema");

#
# Get some preferences from the DB
#
my $hl7Prefs = $dbh->selectrow_hashref("SELECT * from hosp_hl7_prefs");

#
# Register all new MR Nos.
#
eval {
	# send_new_patients();
	# send_new_visits();
	send_admission_data();
	send_transfer_data();
	send_discharge_data();
};

# cleanup and exit

$util->pidFileCleanUp($pidfile);

$dbh->disconnect();
$conn->close() if ($conn);
$fh->close() if ($fh);


#
# Subs follow
#
sub send_new_patients {

	my $qh = $dbh->prepare(qq{
		SELECT mr_no, patient_gender, last_name, patient_name, salutation,
			to_char(dateofbirth,'YYYYMMDD') as dateofbirth,
			to_char(expected_dob,'YYYYMMDD') as expected_dob,
			patient_address, co.country_name, s.state_name, c.city_name, patient_phone,
			nextval('hl7_msgid_sequence') AS hl7_msg_id
		FROM patient_details pd
			JOIN city c ON (c.city_id = pd.patient_city)
			JOIN state_master s ON (s.state_id = pd.patient_state)
			LEFT JOIN country_master co ON (co.country_id = pd.country)
		WHERE mr_no IN
			(SELECT item_id FROM hl7_export_items WHERE item_type = 'PATIENT'
				AND exported_ts IS NULL)
	});

	my $updh = $dbh->prepare(qq{
		UPDATE hl7_export_items SET exported_ts=current_timestamp
		WHERE item_id=? AND item_type='PATIENT'
	});

	$qh->execute();

	while (my $p = $qh->fetchrow_hashref) {
		$log->info("Exporting patient $p->{mr_no}");
		$ADT4_MSH{controlId} = $p->{hl7_msg_id};
		my $msg = new Hl7::Message(\%ADT4_MSH);

		# Add Event, even if it is empty
		$msg->addSegment(new Hl7::Segment('EVN'));

		# Add PID
		my ($line1, $line2) = split('\r{0,1}\n', $p->{patient_address});
		my $pid = new Hl7::Segment('PID', {
				sid=> 1,
				pid=> $p->{patient_id}, pidList=>$p->{mr_no},
				name=> [$p->{last_name}, $p->{patient_name}, $p->{middle_name}, '', $p->{salutation}],
				dob=> defined($p->{dateofbirth}) ? $p->{dateofbirth} : $p->{expected_dob},
				addr=> [$line1 || '', $line2 || '', $p->{city_name}, $p->{state_name}, '',
						$p->{country_name}],
				phHome => $p->{patient_phone}, sex=> $p->{patient_gender}
		});

		if($p->{mother_mr_no} ) {
			$pid->{momId} = $p->{mother_mr_no};
		}
		$msg->addSegment($pid);

		# Add PV1, even if it is empty
		$msg->addSegment(new Hl7::Segment('PV1'));

		my $success = dispatch($msg);

		if ($success) {
			$updh->execute($p->{mr_no});
		} else {
			$log->error("Failed to send patient details: ", $p->{mr_no});
		}
	}
	$qh->finish();
	$updh->finish();
}

sub send_new_visits {
	my $qh = $dbh->prepare(q{
		SELECT ei.*, pr.patient_id, pr.mr_no, pd.patient_name, pd.last_name, pd.patient_gender, pd.middle_name,
			sm.salutation,
			to_char(pd.dateofbirth,'YYYYMMDD') as dateofbirth,
			to_char(pd.expected_dob,'YYYYMMDD') as expected_dob,
			to_char(pr.reg_date+pr.reg_time,'YYYYMMDDhh24mi') as admit_date,
			to_char(pr.discharge_date+pr.discharge_time,'YYYYMMDDhh24mi') as discharge_date,
			pd.patient_address, co.country_name, s.state_name, c.city_name, pd.patient_phone,
			doc.doctor_name, doc.doctor_id,
			coalesce(rdoc.doctor_name, ref.referal_name) AS referer, coalesce(rdoc.doctor_id, ref.referal_no) AS referer_id,
			nextval('hl7_msgid_sequence') AS hl7_msg_id, ''::text as bed_name, ''::text as bed_id, ''::text as ward_name, ''::text as ward_no
		FROM hl7_export_items ei
			JOIN patient_registration pr ON(pr.patient_id = ei.item_id AND ei.item_type = 'VISIT')
			JOIN patient_details pd USING(mr_no)
			JOIN city c ON (c.city_id = pd.patient_city)
			JOIN state_master s ON (s.state_id = pd.patient_state)
			JOIN country_master co ON (co.country_id = pd.country)
			JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
			LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)
			LEFT JOIN doctors rdoc ON (rdoc.doctor_id = pr.reference_docto_id)
			LEFT JOIN referral ref ON (ref.referal_no = pr.reference_docto_id)
		WHERE ei.export_status IN ('N','F')
	});

	my $updh = $dbh->prepare(q{
		UPDATE hl7_export_items SET exported_ts=current_timestamp
		WHERE item_id=? AND item_type='VISIT'
	});

	$qh->execute();

	while (my $p = $qh->fetchrow_hashref) {
		$log->info("Exporting visit $p->{patient_id}");

		$ADT4_MSH{controlId} = $p->{hl7_msg_id};
		my $msg = new Hl7::Message(\%ADT4_MSH);

		#
		# Add Event, even if it is empty
		#
		$msg->addSegment(new Hl7::Segment('EVN'));

		#
		# Add PID
		#
		my ($line1, $line2) = split('\r{0,1}\n', $p->{patient_address});
		my $pid = new Hl7::Segment('PID', {
				sid=> 1,
				pid=> $p->{patient_id}, pidList=>$p->{mr_no},
				name=> [$p->{last_name}, $p->{patient_name}, '', '', $p->{salutation}],
				dob=> defined($p->{dateofbirth}) ? $p->{dateofbirth} : $p->{expected_dob},
				addr=> [$line1 || '', $line2 || '', $p->{city_name}, $p->{state_name}, '',
						$p->{country_name}],
				phHome => $p->{patient_phone}, sex=> $p->{patient_gender}
			});

                if($p->{mother_mr_no} ) {
                        $pid->{momId} = $p->{mother_mr_no};
                }

		$msg->addSegment($pid);

		#
		# Add PV1
		#
		my $pv = new Hl7::Segment('PV1', {
				sid=>1, patientClass=>'O', visitNum => $p->{patient_id},
				admitTimest=>$p->{admit_date}, hospService=>'D01'
		});

		if ($p->{discharge_date}) {
			$pv->{dischTimest} = $p->{discharge_date};
		}

		if ($p->{referer}) {
			$pv->{refDoctor} = [$p->{referer_id}, $p->{referer}];
		}

		if ($p->{doctor_name}) {
			$pv->{refDoctor} = [$p->{doctor_id}, $p->{doctor_name}];
			$pv->{admitDoctor} = [$p->{doctor_id}, $p->{doctor_name}];
			# $pv->{PATIENT_TYPE} = 'R';		# check
		} else {
			$pv->{patientType} = 'HD';
		}

		$msg->addSegment($pv);

		#
		# Add IN1 if the patient is insured
		#
		if ($p->{tpa_id}) {
			my $in = new Hl7::Segment('IN1', {
					sid=>1, insCoId => $p->{tpa_id}, insCoName => $p->{tpa_name}
			});
			$msg->addSegment($in);
		}

		#
		# Dispatch the message
		#
		my $success = dispatch($p, $msg);
		if ($success) {
			$updh->execute($p->{patient_id});
		} else {
			$log->error("Failed to send visit details: ", $p->{patient_id});
		}
	}
	$qh->finish();
	$updh->finish();
}


sub send_admission_data {
	my $qry = get_admission_data_query();

	$log->debug($qry);

	my $qh = $dbh->prepare($qry);

	my $updh = $dbh->prepare(q{
		UPDATE hl7_export_items SET exported_ts=current_timestamp
		WHERE item_id=? AND item_type='ADMISSION'
	});

	$qh->execute();
	while (my $p = $qh->fetchrow_hashref) {
		$log->info("Exporting admission $p->{patient_id}");
		my $msg = undef;
	       $msg = get_adt_message(\%ADT1_MSH, $p);
		
		#
		# Dispatch the message
		#
		my $success = dispatch($p, $msg);
		if ($success) {
			$updh->execute($p->{patient_id});
		} else {
			$log->error("Failed to send visit details: ", $p->{patient_id}, "\n");
		}
	}
	$qh->finish();
	$updh->finish();
}

sub get_admission_data_query {
	my $query =  q{
		SELECT ei.*, pr.patient_id, pr.mr_no, pd.patient_name, pd.last_name, pd.patient_gender, pd.middle_name,
			sm.salutation,
			to_char(pd.dateofbirth,'YYYYMMDD') as dateofbirth,
			to_char(pd.expected_dob,'YYYYMMDD') as expected_dob,
			to_char(pr.reg_date+pr.reg_time,'YYYYMMDDhh24mi') as admit_date,
			to_char(pr.discharge_date+pr.discharge_time,'YYYYMMDDhh24mi') as discharge_date,
			pd.patient_address, co.country_name, s.state_name, c.city_name, pd.patient_phone,
			doc.doctor_name, doc.doctor_id,
			coalesce(rdoc.doctor_name, ref.referal_name) AS referer, coalesce(rdoc.doctor_id, ref.referal_no) AS referer_id,
			nextval('hl7_msgid_sequence') AS hl7_msg_id, bn.bed_name, wn.ward_name, bn.bed_id, wn.ward_no, wn.center_id,
			hcm.center_id as current_center_id,hcm.center_name as current_center_name, mpr.mr_no as mother_mr_no
		FROM hl7_export_items ei
			JOIN ip_bed_details ibd ON (ibd.patient_id = ei.item_id AND ei.item_type = 'ADMISSION')
			JOIN bed_names bn ON (ibd.bed_id = bn.bed_id)
			JOIN patient_registration pr ON(pr.patient_id = ibd.patient_id)
			JOIN patient_details pd USING(mr_no)
			JOIN city c ON (c.city_id = pd.patient_city)
			JOIN state_master s ON (s.state_id = pd.patient_state)
			JOIN country_master co ON (co.country_id = pd.country)
			JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
			LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no)
			LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)
			LEFT JOIN doctors rdoc ON (rdoc.doctor_id = pr.reference_docto_id)
			LEFT JOIN referral ref ON (ref.referal_no = pr.reference_docto_id)
			LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
			LEFT JOIN admission a ON (isbaby='Y' and a.patient_id = pr.patient_id)
			LEFT JOIN patient_registration mpr ON (mpr.patient_id = a.parent_id)
		WHERE ei.export_status IN ('N','F') AND ibd.status IN ('A', 'C') AND NOT ibd.is_bystander
	};

	return $query;
}

sub send_transfer_data {

	my $qry = get_transfer_data_query();
	my $qh = $dbh->prepare($qry);

	my $updh = $dbh->prepare(q{
		UPDATE hl7_export_items SET exported_ts=current_timestamp
		WHERE item_id=? AND item_type='TRANSFER'
	});
	$qh->execute();
	while (my $p = $qh->fetchrow_hashref) {
		$log->info("Exporting patient transfer $p->{patient_id}");
		my $msg = get_adt_message(\%ADT2_MSH, $p);
		#
		# Dispatch the message
		#
		my $success = dispatch($p, $msg);
		if ($success) {
			$updh->execute($p->{patient_id});
		} else {
			$log->error("Failed to send visit details: ", $p->{patient_id});
		}
	}
	$qh->finish();
	$updh->finish();
}

sub get_transfer_data_query {

	my $query = q{
		SELECT ei.*, pr.patient_id, pr.mr_no, pd.patient_name, pd.last_name, pd.patient_gender, pd.middle_name,
			sm.salutation,
			to_char(pd.dateofbirth,'YYYYMMDD') as dateofbirth,
			to_char(pd.expected_dob,'YYYYMMDD') as expected_dob,
			to_char(pr.reg_date+pr.reg_time,'YYYYMMDDhh24mi') as admit_date,
			to_char(pr.discharge_date+pr.discharge_time,'YYYYMMDDhh24mi') as discharge_date,
			pd.patient_address, co.country_name, s.state_name, c.city_name, pd.patient_phone,
			doc.doctor_name, doc.doctor_id,
			coalesce(rdoc.doctor_name, ref.referal_name) AS referer, coalesce(rdoc.doctor_id, ref.referal_no) AS referer_id,
			nextval('hl7_msgid_sequence') AS hl7_msg_id, bn.bed_name, wn.ward_name, bn.bed_id, wn.ward_no,
			frmbed.bed_name as prev_bed_name, frmbed.ward_name as prev_ward_name, frmbed.bed_id as prev_bed_id, frmbed.ward_no as prev_ward_no, frmbed.center_id as prev_center_id, wn.center_id,
			hcm.center_id as current_center_id,hcm.center_name as current_center_name,mpr.mr_no as mother_mr_no
		FROM hl7_export_items ei
			JOIN ip_bed_details ibd ON (ibd.patient_id = ei.item_id AND ei.item_type = 'TRANSFER')
			JOIN bed_names bn ON (ibd.bed_id = bn.bed_id)
			JOIN patient_registration pr ON(pr.patient_id = ibd.patient_id)
			JOIN patient_details pd USING(mr_no)
			JOIN city c ON (c.city_id = pd.patient_city)
			JOIN state_master s ON (s.state_id = pd.patient_state)
			JOIN country_master co ON (co.country_id = pd.country)
			JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
			LEFT JOIN
				(SELECT ipb.bed_id, ipb.patient_id, fbn.bed_name, fwn.ward_no, fwn.ward_name, fwn.center_id
				FROM ip_bed_details ipb
				JOIN bed_names fbn ON (ipb.bed_id = fbn.bed_id)
				LEFT JOIN ward_names fwn ON (fwn.ward_no = fbn.ward_no)
				WHERE (NOT is_bystander) AND ipb.status NOT IN ('A','C') ORDER BY ipb.end_date DESC LIMIT 1)
				AS frmbed ON (ibd.patient_id = frmbed.patient_id AND ibd.bed_id != frmbed.bed_id)
			LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no)
			LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)
			LEFT JOIN doctors rdoc ON (rdoc.doctor_id = pr.reference_docto_id)
			LEFT JOIN referral ref ON (ref.referal_no = pr.reference_docto_id)
			LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
			LEFT JOIN admission a ON (isbaby='Y' and a.patient_id = pr.patient_id)
			LEFT JOIN patient_registration mpr ON (mpr.patient_id = a.parent_id)

		WHERE ei.export_status IN ('N','F') AND ibd.status IN ('A', 'C') AND NOT ibd.is_bystander
	};

	return $query;
}

sub send_discharge_data {

	my $qry = get_discharge_data_query();
	my $qh = $dbh->prepare($qry);

	my $updh = $dbh->prepare(q{
		UPDATE hl7_export_items SET exported_ts=current_timestamp
		WHERE item_id=? AND item_type='DISCHARGE'
	});
	$qh->execute();
	while (my $p = $qh->fetchrow_hashref) {
		$log->info("Exporting patient discharge $p->{patient_id}");
		my $msg = get_adt_message(\%ADT3_MSH, $p);
		#
		# Dispatch the message
		#
		my $success = dispatch($p, $msg);
		if ($success) {
			$updh->execute($p->{patient_id});
		} else {
			$log->error("Failed to send visit details: ", $p->{patient_id});;
		}
	}
	$qh->finish();
	$updh->finish();
}

sub get_discharge_data_query {

	my $query = q{
		SELECT ei.*, pr.patient_id, pr.mr_no, pd.patient_name, pd.last_name, pd.patient_gender, pd.middle_name,
			sm.salutation,
			to_char(pd.dateofbirth,'YYYYMMDD') as dateofbirth,
			to_char(pd.expected_dob,'YYYYMMDD') as expected_dob,
			to_char(pr.reg_date+pr.reg_time,'YYYYMMDDhh24mi') as admit_date,
			to_char(pr.discharge_date+pr.discharge_time,'YYYYMMDDhh24mi') as discharge_date,
			pd.patient_address, co.country_name, s.state_name, c.city_name, pd.patient_phone,
			doc.doctor_name, doc.doctor_id,
			coalesce(rdoc.doctor_name, ref.referal_name) AS referer, coalesce(rdoc.doctor_id, ref.referal_no) AS referer_id,
			nextval('hl7_msgid_sequence') AS hl7_msg_id,
			''::text as bed_name, ''::text as bed_id, ''::text as ward_name, ''::text as ward_no,
			frmbed.bed_name::text as prev_bed_name, frmbed.bed_id::text as prev_bed_id, frmbed.ward_name::text as prev_ward_name,
			frmbed.ward_no::text as prev_ward_no, frmbed.center_id,
			hcm.center_id as current_center_id,hcm.center_name as current_center_name, mpr.mr_no as mother_mr_no
		FROM hl7_export_items ei
			JOIN ip_bed_details ibd ON (ibd.patient_id = ei.item_id AND ei.item_type = 'DISCHARGE')
			JOIN patient_registration pr ON(pr.patient_id = ei.item_id AND ei.item_type = 'DISCHARGE')
			JOIN patient_details pd USING(mr_no)
			JOIN city c ON (c.city_id = pd.patient_city)
			JOIN state_master s ON (s.state_id = pd.patient_state)
			JOIN country_master co ON (co.country_id = pd.country)
			JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
			LEFT JOIN
				(SELECT ipb.bed_id, ipb.patient_id, fbn.bed_name, fwn.ward_no, fwn.ward_name, fwn.center_id
				FROM ip_bed_details ipb
				JOIN bed_names fbn ON (ipb.bed_id = fbn.bed_id)
				LEFT JOIN ward_names fwn ON (fwn.ward_no = fbn.ward_no)
				WHERE (NOT is_bystander) AND ipb.status NOT IN ('A','C') ORDER BY ipb.end_date DESC LIMIT 1)
				AS frmbed ON (pr.patient_id = frmbed.patient_id)
			LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)
			LEFT JOIN doctors rdoc ON (rdoc.doctor_id = pr.reference_docto_id)
			LEFT JOIN referral ref ON (ref.referal_no = pr.reference_docto_id)
			LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
			LEFT JOIN admission a ON (isbaby='Y' and a.patient_id = pr.patient_id)
			LEFT JOIN patient_registration mpr ON (mpr.patient_id = a.parent_id)
		WHERE ei.export_status IN ('N','F') AND ibd.status IN ('P') AND NOT ibd.is_bystander
	};

	return $query;
}

sub get_adt_message {
	my ($msh, $p) = @_;
	$msh->{controlId} = $p->{hl7_msg_id};
	my $msg = new Hl7::Message($msh);
	#
	# Add Event, even if it is empty
	#
	if($p->{current_center_id} != 0) {
		$msg->{MSH}{sendFac} = $p->{current_center_name};
	}
	$msg->addSegment(new Hl7::Segment('EVN'));

	#
	# Add PID
	#
	my ($line1, $line2) = split('\r{0,1}\n', $p->{patient_address});
	my $pid = new Hl7::Segment('PID', {
			sid=> 1,
			pid=> $p->{patient_id}, pidList=>$p->{mr_no},
			name=> [$p->{last_name}, $p->{patient_name}, $p->{middle_name} , '', $p->{salutation}],
			dob=> defined($p->{dateofbirth}) ? $p->{dateofbirth} : $p->{expected_dob},
			addr=> [$line1 || '', $line2 || '', $p->{city_name}, $p->{state_name}, '',
					$p->{country_name}],
			phHome => $p->{patient_phone}, sex=> $p->{patient_gender}
		});
                if($p->{mother_mr_no} ) {
                        $pid->{momId} = $p->{mother_mr_no};
                }

	$msg->addSegment($pid);

	#
	# Add PV1
	#
	my $pv = new Hl7::Segment('PV1', {
			sid=>1, patientClass=>'I', visitNum => $p->{patient_id},
			admitTimest=>$p->{admit_date}
	});

	if ($p->{discharge_date}) {
		$pv->{dischTimest} = $p->{discharge_date};
	}

	if ($p->{referer}) {
		$pv->{refDoctor} = [$p->{referer_id}, $p->{referer}];
	}

	if ($p->{bed_name} && $p->{ward_name}) {
		$pv->{assgndLoc} = ['', $p->{ward_name}, $p->{bed_name}, $p->{center_id}];
	} else {
		if ($p->{bed_name}) {
			$pv->{assgndLoc} = ['', '', $p->{bed_name}, $p->{center_id}];
		} else {
			$pv->{assgndLoc} = ['', $p->{ward_name}, '',$p->{center_id}];
		}
	}

	if ($p->{prev_bed_name} && $p->{prev_ward_name}) {
		$pv->{priorLoc} = ['', $p->{prev_ward_name}, $p->{prev_bed_name}, $p->{prev_center_id}];
	} else {
		if ($p->{prev_bed_name}) {
			$pv->{priorLoc} = ['', '', $p->{prev_bed_name}, $p->{prev_center_id}];
		} else {
			if($p->{prev_ward_name}) {
				$pv->{priorLoc} = ['', $p->{prev_ward_name}, '', $p->{prev_center_id}];
			}
		}
	}

	if ($p->{doctor_name}) {
		$pv->{refDoctor} = [$p->{doctor_id}, $p->{doctor_name}];
		$pv->{admitDoctor} = [$p->{doctor_id}, $p->{doctor_name}];
		# $pv->{PATIENT_TYPE} = 'R';		# check
	} else {
		# $pv->{patientType} = 'HD';
	}

	$msg->addSegment($pv);

	#
	# Add IN1 if the patient is insured
	#
	if ($p->{tpa_id}) {
		my $in = new Hl7::Segment('IN1', {
				sid=>1, insCoId => $p->{tpa_id}, insCoName => $p->{tpa_name}
		});
		$msg->addSegment($in);
	}

	return $msg;
}
#
# Send the message, according to the preferences
#
sub dispatch {
	my ($p, $msg) = @_;

	my $exportType = $hl7Prefs->{adt_send_type};

	if (($exportType eq 'F') || ($exportType eq 'B')) {
		exportMessage($p, $msg, $hl7Prefs->{adt_send_filename}, $exportType eq 'F');
	}

	if (($exportType eq 'S') || ($exportType eq 'B')) {
		# this could fork and do the job in a different process, so don't rely on return value
		sendMessage($p, $msg, $hl7Prefs->{adt_send_ip}, $hl7Prefs->{adt_send_port});
	}
}

sub exportMessage {
	my ($p, $msg, $file, $isMainTransport) = @_;
	my $msgId = $msg->{MSH}{controlId};
	my $msgType =$msg->{MSH}{msgType};
	my $patientId = $msg->{PID}{pid};
	#my $subFolder = $message_type_folder->{$msgType};
	$log->info("Exporting message type @{$msgType}[1]");
	$log->info("Exporting message into file =  $file.adt.$patientId.@{$msgType}[1].$msgId.hl7");
	unless (open(FH, "> $file.adt.$patientId.@{$msgType}[1].$msgId.hl7")) {
		my $failureMsg = "Could not open $file.adt.$patientId.@{$msgType}[1].$msgId.hl7: $!";
		$log->error($failureMsg);
		updateExportedFailure($p->{export_id}, $msgId, $failureMsg) if ($isMainTransport);
		return;
	}

	print FH $msg->toString($hl7Prefs->{adt_send_pretty});
	close FH;

	updateExportedSuccess($p->{export_id}, $msgId) if ($isMainTransport);
}

sub sendMessage {
	my ($p, $msg, $ipAddr, $port) = @_;
	my $msgId = $msg->{MSH}{controlId};

	$log->info("Sending message: $msgId\n" . $msg->toString());
	my $conn = IO::Socket::INET->new (
		Proto    => "tcp",
		PeerAddr => $ipAddr,
		PeerPort => $port,
		timeout  => 10
	);

	unless ($conn) {
		my $failureMsg = "Unable to connect to host $ipAddr at port $port: $!";
		$log->error($failureMsg);
		updateExportedFailure($p->{export_id}, $msgId, $failureMsg);
		return;
	}

	$log->info($conn ,"\x0B", $msg->toString(), "\x1C\x0D");
	$conn->flush();

	# Read the response:
	$/ = "\x1C\x0D";	# file separator, carriage return
	my $resp;
	my $respMsg;
	eval {
		local $SIG{ALRM} = sub { die "Timed Out" };
		alarm 10;
		$resp = <$conn>;
		alarm 0;
	};
	if ($@) {
		my $failureMsg;
		if ($@ =~ /Timed Out/) {
			$failureMsg = "ERROR: Reading response timed out";
		} else {
			$failureMsg = "ERROR: Failed to read response: $@";
		}
		$log->error($failureMsg);
		updateExportedFailure($p->{export_id}, $msgId, $failureMsg);
		$conn->close();
		return 0;
	}

	eval {
		$respMsg = Hl7::Message->new($resp);
		$log->info("Recd commit message acknowledgement:");
		$log->info($respMsg->toString(1));
	};
	if ($@) {
		my $clean = $resp;
		$clean =~ tr/\x0B\x0D\x1C/\n\n./;
		my $failureMsg = "ERROR: Could not parse response as HL7: $clean";
		$log->error($failureMsg);
		updateExportedFailure($p->{export_id}, $msgId, $failureMsg);
		return 0;
	}
	$conn->close();

	if ($respMsg->{MSA}{code} ne 'AA') {
		my $failureMsg = "Receiver rejected the message due to: " . $respMsg->{MSA}{msgText};
		$log->error($failureMsg);
		updateExportedFailure($p->{export_id}, $msgId, $failureMsg);
		return 0;
	}

	$log->info("Sending message succeeded: ", $respMsg->{MSA}{code}, ", updating success.");
	updateExportedSuccess($p->{export_id}, $msgId);
	return 1;
}

sub updateExportedSuccess {
	my ($exportId, $msgId) = @_;

	my $sth = $dbh->prepare(qq{
		UPDATE hl7_export_items set exported_ts=current_timestamp, export_status='S', export_msg_id=?
		WHERE export_id=?});
	$sth->execute($msgId, $exportId);
}

sub updateExportedFailure {
	my ($exportId, $msgId, $failureMsg) = @_;

	my $sth = $dbh->prepare(qq{
		UPDATE hl7_export_items set export_status='F', export_msg_id=?, export_failure_msg=?
		WHERE export_id=?});
	$sth->execute($msgId, $failureMsg, $exportId);
}



END { $log->info("#####################<<<<<<<<<<<<<<<<<END>>>>>>>>>>>>>>>>>>>>>>#################\n");}

