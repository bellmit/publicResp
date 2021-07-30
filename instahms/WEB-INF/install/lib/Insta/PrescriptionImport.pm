package Insta::PrescriptionImport;

use strict;
use warnings;
use DBI;
use LWP;
use HTTP::Request::Common;
use HTTP::Cookies;
use POSIX qw(strftime);
use MIME::Base64;
use DBD::Pg qw(:pg_types);

=head1 NAME	:	Insta::PrescriptionImport -

	PrescriptionImport responsible to insert the message in IHS

=head1 SYNOPSIS	:

	use Insta::PrescriptionImport;


=head1 SUBROUTINES :

=over 4

=item * new([ARGS])

	Creats an "Insta::PrescriptionImport" object , which is a reference to a newely created object.
	new Opetionaly takes an arguments are in key-value pairs.

	db		Database name
	schema		Schema
	appPort		Database Port no
	userId		Database Userid

Example :

	$pimp = Insta::PrescriptionImport->new( {
			db=>$opt_db, schema=>$opt_schema, db_port=>$opt_port,db_username=>$opt_username,db_password=>$opt_password,
			userId=>$userId, host=>$opt_host
	 	});

=item * processMessage -

	processMessage subroutine is common for all Prescription.

	Based on IP or OP Patient Prescription and Prescription type(LIS or RIS, PROC and MED) 

	it is calling corresponding subroutines.

=item * patientPrescription -

	This subroutine is used for inserting data into  patient_prescription table  for OP patient.

	patient_prescription table is a parent table of all prescription for OP Patient.

=item * insertPatientTestPrescriptions -

	insertPatientTestPrescriptions subroutine is used for inserting Lab and Radiology( LIS and RIS ) message into patient_test_prescriptions table.

	patient_test_prescriptions is a sub table of patient_prescription.

=item * patientServicePrescriptions -

 	patientServicePrescriptions subroutine is used for inserting PROC (Services) message into patient_service_prescriptions.
	
	patient_service_prescriptions  is a sub table of patient_prescription.

=item * insertPatientTestPrescriptionsPKG -

	This subroutine is used for inserting PKG messaeg into patient_test_prescriptions.	

	This is same like LIS and RIS only different is that ispackage = true of patient_test_prescriptions table.

	patient_test_prescriptions is a sub table of patient_prescription.

=item * patientMedicinePrescriptions -

	This subroutine is used for inserting  Medicine prescription into 

	patient_prescription and patient_medicine_prescriptions table.


=item * ipPrescription -

	This subroutine is inserting ip_prescription and patient_activities table.

	presc_id is using from ip_prescription_seq table.

=item * validateMsg -

	This subroutine is validating mandatory field of the message.

=back

=head1 BUGS		:	Bug#42510,#42743

=head1 AUTHOR	:	INSTA developer team

=head1 COPYRIGHT	:	@INSTA 2014


=cut

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
	my $ifName = $ifDesc->{ifName};
	my @tests = ();		# to store all the tests that are affected by this message
	my %pushed = ();
	my $lastError = "";
	# Group all segments with the same obsSubId
	my $obxIds = [];
	my $segsForId;
	my $prvsSubId = -1;		# start with invalid one so that first iter hits a difference
	my $prvsValueType = '';

	my $errMsg = $self->validateMsg($dbh,$msg);

	if($errMsg) {

		print "\nError in mandatory Field Visit_ID = $msg->{PV1}->{visitNum} \n ERROR_MSG = $errMsg\n";
		return $errMsg;
	}

	if($msg->{PV1}->{patientClass} eq "O") {
		if(($msg->{MSH}->{recvApp} eq 'LIS' ) or ($msg->{MSH}->{recvApp} eq 'RIS' )) {

			my $patient_prescription_seq = $dbh->prepare("SELECT nextval('patient_prescription_seq'::regclass)");
			$patient_prescription_seq->execute();
			my @patient_prescription_seq_arr = $patient_prescription_seq->fetchrow_array;
			$self->patientPrescription($dbh,$msg,$patient_prescription_seq_arr[0]);

			$self->insertPatientTestPrescriptions($dbh,$msg,$patient_prescription_seq_arr[0]);
		}

		if($msg->{MSH}->{recvApp} eq 'PROC'){

			my $patient_prescription_seq = $dbh->prepare("SELECT nextval('patient_prescription_seq'::regclass)");
			$patient_prescription_seq->execute();
			my @patient_prescription_seq_arr = $patient_prescription_seq->fetchrow_array;
			$self->patientPrescription($dbh,$msg,$patient_prescription_seq_arr[0]);

			$self->patientServicePrescriptions($dbh,$msg,$patient_prescription_seq_arr[0]);

		}

		if($msg->{MSH}->{recvApp} eq 'PKG') {

			my $patient_prescription_seq = $dbh->prepare("SELECT nextval('patient_prescription_seq'::regclass)");
			$patient_prescription_seq->execute();
			my @patient_prescription_seq_arr = $patient_prescription_seq->fetchrow_array;
			$self->patientPrescription($dbh,$msg,$patient_prescription_seq_arr[0]);

			$self->insertPatientTestPrescriptionsPKG($dbh,$msg,$patient_prescription_seq_arr[0]);
		}

		if($msg->{MSH}->{recvApp} eq 'MED') {

			my $patient_prescription_seq = $dbh->prepare("SELECT nextval('patient_prescription_seq'::regclass)");
			$patient_prescription_seq->execute();
			my @patient_prescription_seq_arr = $patient_prescription_seq->fetchrow_array;

			$self->patientMedicinePrescriptions($dbh, $msg, $patient_prescription_seq_arr[0]);
		}
	}

	if($msg->{PV1}->{patientClass} eq "I") {

		my $patient_prescription_seq = $dbh->prepare("SELECT nextval('ip_prescription_seq'::regclass)");
		$patient_prescription_seq->execute();
		my @patient_prescription_seq_arr = $patient_prescription_seq->fetchrow_array;

		$self->ipPrescription($dbh, $msg, $patient_prescription_seq_arr[0]);
	}

	
	return 0;
}



sub patientPrescription {

	my $self = shift;
	my ( $dbh, $msg,$pres_id ) = @_;
	my $msgOBR = $msg->{OBR};
	my $msgORC = $msg->{ORC};

	my $sth = $dbh->prepare(qq{
		INSERT INTO patient_prescription(patient_presc_id,consultation_id,presc_type,status,prescribed_date,external_order_no) 
				values(?,?,?,?,to_date(?, 'YYYYMMDDHH24MISS'),?)
	});

	if($msg->{MSH}->{recvApp} eq 'LIS' or ($msg->{MSH}->{recvApp} eq 'RIS' )) {
		$sth->execute($pres_id, $msgORC->{fillerOrderNum},'Inv.','P',$msgOBR->{reqTime}, $msgORC->{'placerOrderNum'});
	}

	if($msg->{MSH}->{recvApp} eq 'PROC') {
		$sth->execute($pres_id, $msgORC->{fillerOrderNum},'Service','P',$msgOBR->{reqTime},$msgORC->{'placerOrderNum'} );
	}

	if($msg->{MSH}->{recvApp} eq 'PKG') {
		$sth->execute($pres_id, $msgORC->{fillerOrderNum},'Inv.','P',$msgOBR->{reqTime}, $msgORC->{'placerOrderNum'} );
	}

}


sub insertPatientTestPrescriptions {

	my $self = shift;
	my ( $dbh, $msg, $pres_id ) = @_;
	my	$msgOBR = $msg->{OBR};
	my $sth = $dbh->prepare(qq{
		INSERT INTO patient_test_prescriptions (test_remarks,mod_time,test_id,username,op_test_pres_id) 
			VALUES('',current_timestamp,?,'auto_update',?);
	});

	$sth->execute(@{$msgOBR->{serviceId}}[0],$pres_id);

}

sub insertPatientTestPrescriptionsPKG {

	my $self = shift;
	my ( $dbh, $msg, $pres_id ) = @_;
	my	$msgOBR = $msg->{OBR};
	my $sth = $dbh->prepare(qq{
		INSERT INTO patient_test_prescriptions (test_remarks,mod_time,test_id,username,op_test_pres_id,ispackage) 
				VALUES('',current_timestamp,?,'auto_update',?,true);
	});

	$sth->execute(@{$msgOBR->{serviceId}}[0],$pres_id);

}

sub patientServicePrescriptions {

	my $self = shift;
	my ( $dbh, $msg, $pres_id ) = @_;
	my	$msgOBR = $msg->{OBR};


	my $sth = $dbh->prepare(qq{
		INSERT INTO patient_service_prescriptions (service_remarks,mod_time,service_id,username,op_service_pres_id) 
			VALUES('',current_timestamp,?,'auto_update',?);
	});

	$sth->execute(@{$msgOBR->{serviceId}}[0],$pres_id);

}

sub patientMedicinePrescriptions {

	my $self = shift;
	my ( $dbh, $msg, $pres_id ) = @_;
	my $msgRXO = $msg->{RXO};
	my $msgORC = $msg->{ORC};

	my $sth = $dbh->prepare(qq{
		INSERT INTO patient_prescription(patient_presc_id,consultation_id,presc_type,status,prescribed_date,external_order_no) 
			values(?,?,?,?,to_date(?, 'YYYYMMDDHH24MISS'),?)
	});

	$sth->execute($pres_id, $msgORC->{fillerOrderNum},'Medicine','P',$msgORC->{timest},$msgORC->{'placerOrderNum'});

	$sth = $dbh->prepare(qq{
		INSERT INTO patient_medicine_prescriptions (op_medicine_pres_id,medicine_remarks,mod_time,medicine_id)
				VALUES(?,?,current_timestamp,?);
	});

	$sth->execute($pres_id,@{$msgRXO->{treatInstructions}}[1] || '',@{$msgRXO->{giveCode}}[0]);


}


sub ipPrescription {

	my $self = shift;
	my ($dbh, $msg, $pres_id) = @_;


	my $presc_type = undef;

	##Map Data For Patient Activity
	## Below map is common for all messages

	my $patient_activities_map = {
					patient_id	=> $msg->{PV1}->{visitNum},		
					activity_type	=> 'P',  ## P-->  For Prescription Activity and R-> For Regular Activity
					activity_num	=> 1,    ## Needs to be clarify
					prescription_id => $pres_id,
					presc_doctor_id => @{$msg->{PV1}->{consDoctor}}[0],
					activity_status => 'P',  ## P --> For Progress, D --> For Done, X --> For Cancel
					added_by	=> 'auto_update',
				#	mod_time	=> "current_timestamp",
					username	=> 'auto_update',
		
			};

## due_date is required for patient_activities and it is available in ORC so for all LIS or RIS or MED or PROC, due data is mapping 
## due_date is either start time or effectiveTime, if effectiveTime is not defined then it should be start time.


	if(defined $msg->{ORC}->{effectiveTime}) {
                $patient_activities_map->{due_date} = $msg->{ORC}->{effectiveTime};
        } else {
                 $patient_activities_map->{due_date} = $msg->{ORC}->{qty}->[3];
        }


	if($msg->{MSH}->{recvApp} eq 'LIS' or ($msg->{MSH}->{recvApp} eq 'RIS' )) {

		my	$msgOBR = $msg->{OBR};

		my $sth = $dbh->prepare(qq{
				INSERT INTO ip_prescription(prescription_id, patient_id, doctor_id, presc_type, mod_time, 
				username, item_id, item_name,remarks, freq_type, start_datetime)
				values(?,?,?,'I',current_timestamp,'auto_update',?,?,'','F', to_date(?, 'YYYYMMDDHH24MISS'))
			});


		$sth->execute($pres_id, $msg->{PV1}->{visitNum}, @{$msg->{PV1}->{consDoctor}}[0],@{$msgOBR->{serviceId}}[0], 
				@{$msgOBR->{serviceId}}[1] || "", $msg->{ORC}->{qty}->[3]);

##		Patient Activities for LIS and RIS

		$patient_activities_map->{prescription_type} = 'I';
		$patient_activities_map->{external_order_no} = $msgOBR->{placerOrderNum};

	}

	if($msg->{MSH}->{recvApp} eq 'PROC'){

		my	$msgOBR = $msg->{OBR};
		my $sth = $dbh->prepare(qq{
				INSERT INTO ip_prescription(prescription_id, patient_id, doctor_id, presc_type, mod_time, 
							    username, item_id, item_name,remarks, freq_type, start_datetime)

				values(?,?,?,'S',current_timestamp,'auto_update',?,?,'','F', to_date(?, 'YYYYMMDDHH24MISS'))
			});


		$sth->execute($pres_id, $msg->{PV1}->{visitNum}, @{$msg->{PV1}->{consDoctor}}[0], @{$msgOBR->{serviceId}}[0], 
				@{$msgOBR->{serviceId}}[1] || '', $msg->{ORC}->{qty}->[3]);

##		Pateint Activities for services

		$patient_activities_map->{prescription_type} = 'S';
		$patient_activities_map->{external_order_no} = $msgOBR->{placerOrderNum};
	}


	if($msg->{MSH}->{recvApp} eq 'MED') {

		my $msgRXO = $msg->{RXO};
		my $msgORC = $msg->{ORC};

		my $sth = $dbh->prepare(qq{
				INSERT INTO ip_prescription(prescription_id, patient_id, doctor_id, presc_type, mod_time, 
							    username, item_id, item_name,remarks,freq_type, start_datetime)
				values(?,?,?,'M',current_timestamp,'auto_update',?,?,?,'F', to_date(?, 'YYYYMMDDHH24MISS'))
			});

		$sth->execute($pres_id, $msg->{PV1}->{visitNum}, @{$msg->{PV1}->{consDoctor}}[0],@{$msgRXO->{giveCode}}[0], 
			      @{$msgRXO->{giveCode}}[1] || '',@{$msgRXO->{treatInstructions}}[1] || '', $msg->{ORC}->{qty}->[3]);
	
##		Patient Activities for Medicine

		$patient_activities_map->{prescription_type} = 'M';
		$patient_activities_map->{external_order_no} = $msgORC->{placerOrderNum};
	}

	$self->patient_activity($patient_activities_map,$dbh);

}

sub patient_activity {
	
	my $self = shift;
	
	my $patient_activities_map = shift;
	my $dbh = shift;


	 my $patient_activities_seq = $dbh->prepare("SELECT nextval('patient_activities_seq'::regclass)");
         $patient_activities_seq->execute();
         my @patient_activities_seq_arr = $patient_activities_seq->fetchrow_array;

	$patient_activities_map->{'activity_id'} = $patient_activities_seq_arr[0];

	my $sql = $self->preapareInsertStatement('patient_activities',$patient_activities_map);

	$sql =~ s/:due_date/to_date(:due_date, 'YYYYMMDDHH24MISS')/g;

	my $sth = $dbh->prepare($sql);
	
	$self->bind_param($patient_activities_map, \$sth);
	$sth->execute();
}



sub validateMsg {

	my $self = shift;
	my $dbh = shift;
	my $msg = shift;

	my $errMsg="";

## Validate Active and In-Active Patient.

	my $is_patient_active = "select status from patient_registration where patient_id = '$msg->{PV1}->{'visitNum'}'";

	my $is_patient_active_sth = $dbh->prepare($is_patient_active);

	$is_patient_active_sth->execute();

	my $patient_row_hash = $is_patient_active_sth->fetchrow_hashref();

	if($patient_row_hash->{'status'} eq 'I'){
		$errMsg = "\n Note : Patient is not active in application\n";
		return $errMsg;
	}
	
				

	if($msg->{PV1}->{patientClass} eq "O") {

		if($msg->{MSH}->{recvApp} eq 'LIS' or ($msg->{MSH}->{recvApp} eq 'RIS' ) or 
			($msg->{MSH}->{recvApp} eq 'PROC') or ($msg->{MSH}->{recvApp} eq 'PKG') ) {

			if((! defined @{$msg->{OBR}->{serviceId}}[0]) or (@{$msg->{OBR}->{serviceId}}[0] eq '') or
				(! defined $msg->{ORC}->{fillerOrderNum}) or ($msg->{ORC}->{fillerOrderNum} eq '') ) {

				    $errMsg = $errMsg . " patientClass = 'O', recvApp = $msg->{MSH}->{recvApp}, 
					ORDER_NO = $msg->{ORC}->{fillerOrderNum} , is not having OBR->fillerOrderNum or OBR->serviceId\n";
				}
			}

		if($msg->{MSH}->{recvApp} eq 'MED') {

			if((! defined $msg->{ORC}->{fillerOrderNum}) or ($msg->{ORC}->{fillerOrderNum} eq '') or 
				(! defined @{$msg->{RXO}->{giveCode}}[0]) or (@{$msg->{RXO}->{giveCode}}[0] eq '') ) {

				$errMsg = $errMsg . "patientClass = 'O', recvApp = $msg->{MSH}->{recvApp} ,  
				ORDER_NO = $msg->{ORC}->{fillerOrderNum} , is not having ORC->fillerOrderNum or ORC->giveCode";
			}
		}

	}

	if($msg->{PV1}->{patientClass} eq "I") {

		if(($msg->{MSH}->{recvApp} eq 'LIS') or ($msg->{MSH}->{recvApp} eq 'RIS' ) or 
				($msg->{MSH}->{recvApp} eq 'PROC')) {

			if((! defined @{$msg->{PV1}->{consDoctor}}[0]) or (@{$msg->{PV1}->{consDoctor}}[0] eq  '') or 
					(! defined @{$msg->{OBR}->{serviceId}}[0]) or (@{$msg->{OBR}->{serviceId}}[0] eq '') ){

					$errMsg = $errMsg . "  patientClass = 'I', recvApp= $msg->{MSH}->{recvApp},  
					order_no = $msg->{OBR}->{placerOrderNum}  is not having PV1->consDoctor or OBR->serviceId\n";
			}

		}

		if($msg->{MSH}->{recvApp} eq 'MED') {

			if((! defined @{$msg->{PV1}->{consDoctor}}[0]) or (@{$msg->{PV1}->{consDoctor}}[0] eq '') or 
				(! defined @{$msg->{RXO}->{giveCode}}[0]) or (@{$msg->{RXO}->{giveCode}}[0] eq '')){

				$errMsg = $errMsg . "patientClass = I ,  recvApp = $msg->{MSH}->{recvApp}, 
					ORDER_NO = $msg->{ORC}->{placerOrderNum} , is not having RXO->consDoctor or RXO->giveCode\n";
			}
		}

	}

	return $errMsg;
}



sub preapareInsertStatement {
	my $self = shift;
	my $table_name = shift;
	my $patient_activities_map = shift;
	my %hash_row = %$patient_activities_map;
	
	my $field = "INSERT INTO $table_name (";
	my $values = "VALUES(";

	foreach my $keys (keys %hash_row) {

		$field = $field . $keys . " , ";
		$values = $values . ":$keys , ";
	}

	chop($field);chop($field);
	chop($values);chop($values);

	return ( $field . " ) " . $values . " ) ");
}


sub bind_param{
	my $self = shift;
	my $hash_row = shift;

	foreach my $key (keys %$hash_row) {
		${$_[0]}->bind_param(":$key",$hash_row->{$key});
	}
}

sub dateTime {

	 my $time_string = scalar localtime();
         my ($day,$mon,$date,$time,$year) = split /\s+/, $time_string;
         my $date_log = "$year-$mon-$date  $time :: ";
         return $date_log;
}


1;

