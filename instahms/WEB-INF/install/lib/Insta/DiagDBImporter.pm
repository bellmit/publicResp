package Insta::DiagDBImporter;

use strict;
use DBI;
use POSIX qw(strftime);

#
# Import reports that need to be imported (ie, those that have not been imported yet).
# Supports both socket interface and file import. The choice is made depending on the
# interface to which the export is to be done for each test.
#
#
# SETUP
# ----
# (a) Query the table integ_diag_in from staging database
#     See \d+ comments on that table.
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


sub importData {
	my $self = shift;
	my ($dbh,$interface_name) = @_;
	my $sql = (qq{ select *  from integ_diag_in where status IN ('N','RR','SC') });
	my $reports = $dbh->selectall_arrayref($sql,{Slice=>{}});
	return $reports;
}

sub updateSuccess {
	my $class = shift;
	my $dbh = shift;
	my $id = shift;

	my $sql = qq{
			UPDATE integ_diag_in set status = 'S', read_ts= current_timestamp where id = ?  
		};
		
	my $sth = $dbh->prepare($sql);
	$sth->execute($id);

}

sub updateFailure {
	my $class = shift;
	my $dbh = shift;
	my $id = shift;
	my $error = shift;

	my $sql = qq{
			UPDATE integ_diag_in set status = 'F', read_ts= current_timestamp, export_failure_msg =?  where id = ?
		};
		
	my $sth = $dbh->prepare($sql);
	$sth->execute($error, $id);

}

sub close {

# we close the database connection here

	my $self = shift; 
	my ($dbh) = @_;
	$dbh->disconnect();
}

1;
