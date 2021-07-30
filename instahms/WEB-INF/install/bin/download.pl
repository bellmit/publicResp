#!/usr/bin/perl -w

use strict;
use DBI;
use Getopt::Long;
use Sys::Hostname;

my $server="localhost";
our $opt_db;
our $opt_schema;

sub usage {
	print
	"Usage: $0 [OPTIONS] <query> \n" .
	"Download the result of a query, useful for getting text/bytea columns from a table\n" .
	"OPTIONS:\n" .
	"  -d|--db <database>: which database to connect to (hms)\n" .
	"  -s|--schema <schema>: schema to connect to (hostname)\n";
}

GetOptions("db=s", "schema=s") or usage and die;

$opt_db = 'hms' unless($opt_db);

unless ($opt_schema) {
	$opt_schema = hostname;
	$opt_schema =~ s/instahms-//;
}

my $query = $ARGV[0];
unless ($query) {
	print "Missing argument: query\n";
	usage();
	exit 1
}

sub getConnection {
	my $dbh = DBI->connect("dbi:Pg:dbname=$opt_db", 'postgres', '', {AutoCommit => 1, RaiseError =>1});
	$dbh->do("SET search_path TO $opt_schema");
	return $dbh;
}

my $dbh = getConnection();

my $result = $dbh->selectall_arrayref($query);

# write out the first column from the first row
print $result->[0][0];

