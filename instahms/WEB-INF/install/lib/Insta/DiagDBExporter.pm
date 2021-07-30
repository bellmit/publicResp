package Insta::DiagDBExporter;

use strict;
use DBI;
use POSIX qw(strftime);

#
# Exports orders that need to be exported (ie, those that have not been exported yet).
# Supports both socket interface and file export. The choice is made depending on the
# interface to which the export is to be done for each test.
#
#
# SETUP
# ----
# (a) Insert into hl7_lab_interfaces a list of interfaces that will be accepting our orders
#     See \d+ comments on that table.
# (b) For every test that we need to export orders for, set its hl7_interface_name to one
#     of the interfaces in hl7_lab_interfaces (editable from UI)
# (c) If the interface understands our code, nothing to do. We will export our test_id as the
#     service ID in the export. If not, we'll need to set hl7_interface_code as non-blank. This
#     will be used as the service ID now.
# (d) Setup a cron-job to call this script with appropriate cmd line parameters periodically.
#

sub new {
	my $class = shift;
	my $params = shift;

	my @columns = (qw(item_type item_id inserted_ts export_id 
			interface_name export_status export_msg_id export_failure_msg bill_paid 
			op_code patient_id prescribed_id test_name hl7_export_code 
			test_id pres_date common_order_id scheduled_time_start start_date_time 
			scheduled_time_end equipment_code sample_sno sample_date mr_no 
			oldmrno last_name patient_name salutation patient_gender 
			expected_dob patient_address cityname city_code statename 
			state_code country_name country_code patient_phone reg_date 
			doctor doctor_name bed_type ward_name reference_docto_id 
			diagnosis_code diagnosis visit_type refdoctorname conduction_format 
			resultlabel_id resultlabel center_id center_name conducting_doctor_id 
			conducting_doctor_name prescription_doctor_id prescription_doctor_name 
			prescription_department out_house_sample middle_name doctor_mobile referal_mobileno presc_doctor_mobile ));
	
	my @columnsd = (qw(patient_id prescribed_id export_id inserted_ts package_name tpa_name plan_name employee_id 
			order_remarks additional_test_info orig_center_id priority sample_type email_id 
			origin_center_name origin_patient_id out_house_name package_category_id tpa_id sample_type_id));
			
	my @columnsvisit = (qw(mr_no origin_patient_id 
			inserted_ts tpa_name plan_name 
			employee_id patient_details_status));
			
	my @columnspatient = (qw(mr_no oldmrno  patient_name last_name middle_name salutation
			patient_gender expected_dob patient_address cityname city_code statename  
			state_code country_name country_code patient_phone inserted_ts  
		    email_id patient_details_status));

	my $self = {};
	foreach my $key (keys(%$params)) {
		$self->{$key} = $params->{$key};
	}
	$self->{columns} = \@columns;
	$self->{columnsd} = \@columnsd;
	$self->{columnsvisit} = \@columnsvisit;
	$self->{columnspatient} = \@columnspatient;
	
	bless $self, $class;
}


sub open {
# we open the database connection here
	my $self = shift; 
	my ($dbhost, $dbport) = @_;

# initialize all default connection parameters in case it is missing

	$dbhost = '127.0.0.1' unless ($dbhost);
	$dbport = 5432 unless ($dbport);
	
	my $dbname = $self->{db} || 'hms';
	my $dbuser = $self->{db_username} || 'postgres';
	my $dbpassword = $self->{db_password} || '';

	print "Connecting to database with parameters dbname=$dbname;host=$dbhost;port=$dbport, username=$dbuser\n";
	
	my $dbh = DBI->connect("dbi:Pg:dbname=$dbname;host=$dbhost;port=$dbport", 
							"$dbuser", "$dbpassword",
		{AutoCommit => 1, RaiseError =>1});
	$dbh->do("SET search_path TO $self->{schema}");
	$dbh->do("SET application.username TO '_system'");
	return $dbh;
}

sub init {

# we create the prepared statement here

	my $self = shift; 
	my ($dbh) = @_;
	
	my $fieldset = $self->{columns};
	my @fields = @$fieldset;
	
    my $fieldlist = join ", ", @fields;
    my $field_placeholders = join ", ", map {'?'} @fields;	
	my $insert_query = qq{
		INSERT INTO hl7_order_items ( $fieldlist ) values ($field_placeholders)
	};
	
#	print "$insert_query \n";
	my $sth = $dbh->prepare($insert_query);
	return $sth;
}
sub export {
	my $self = shift;
	my ($sth, $order, $isMainTransport) = @_;
	
	my $fieldset = $self->{columns};
	my @fields = @$fieldset;

	$sth->execute(@{$order}{@fields});
	$sth->finish();
}
sub initod {

# we create the prepared statement here

	my $self = shift; 
	my ($dbh) = @_;
	
	my $odfieldset = $self->{columnsd};
	my @odfields = @$odfieldset;
	
    my $odfieldlist = join ", ", @odfields;
    my $odfield_placeholders = join ", ", map {'?'} @odfields;	
	my $insert_query = qq{
		INSERT INTO hl7_order_items_main ( $odfieldlist ) values ($odfield_placeholders)
	};
	
#	print "$insert_query \n";
	my $sth = $dbh->prepare($insert_query);
	return $sth;
}
sub exportd {
	my $self = shift;
	my ($sth, $orderd, $isMainTransport) = @_;
	
	my $odfieldset = $self->{columnsd};
	my @odfields = @$odfieldset;

	$sth->execute(@{$orderd}{@odfields});
	$sth->finish();
}
sub initvisitdetails {

# we create the prepared statement here	
	
	my $self = shift; 
	my ($dbh) = @_;
	
	my $pvdfieldset = $self->{columnsvisit};
	my @pvdfields = @$pvdfieldset;	
    my $pvdfieldlist = join ", ", @pvdfields;
    my $pvdfield_placeholders = join ", ", map {'?'} @pvdfields;	
	my $insert_query = qq{
		INSERT INTO hl7_visit_details ( $pvdfieldlist ) values ($pvdfield_placeholders)
	};
	
#	print "$insert_query \n";
	my $sth = $dbh->prepare($insert_query);
	return $sth;
}
sub exportvisitdetails {
	my $self = shift;
	my ($sth, $orderpd, $isMainTransport) = @_;
	
	my $pvdfieldset = $self->{columnsvisit};
	my @pvdfields = @$pvdfieldset;

	$sth->execute(@{$orderpd}{@pvdfields});
	$sth->finish();
}

sub initpatientdetails {
	my $self = shift; 
	my ($dbh) = @_;
	
	my $pvdfieldset = $self->{columnspatient};
	my @pvdfields = @$pvdfieldset;	
    my $pvdfieldlist = join ", ", @pvdfields;
    my $pvdfield_placeholders = join ", ", map {'?'} @pvdfields;	
	my $insert_query = qq{
		INSERT INTO hl7_patient_details ( $pvdfieldlist ) values ($pvdfield_placeholders)
	};
	
#	print "$insert_query \n";
	my $sth = $dbh->prepare($insert_query);
	return $sth;
}
sub exportpatientdetails {
	my $self = shift;
	my ($sth, $orderpd, $isMainTransport) = @_;
	
	my $pvdfieldset = $self->{columnspatient};
	my @pvdfields = @$pvdfieldset;

	$sth->execute(@{$orderpd}{@pvdfields});
	$sth->finish();
}

sub close {

# we close the database connection here

	my $self = shift; 
	my ($dbh) = @_;
	$dbh->disconnect();
}

1;
