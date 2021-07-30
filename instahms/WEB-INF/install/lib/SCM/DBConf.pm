package SCM::DBConf;
use strict;
use warnings;


##Oracle Fire and Test database datails :
## database_name => TEST / FIRE
##user_name => xxnmc_his
##password => xxnmc_his

##Oracle Production server details
##database_name => PROD_BALANCE
##user_name => xxnmc_his
##password => xxnmc8030



##Oracle Database details left side is key and right side is value(Its a key value map)

my %ora_db = (
		database_name 	=> "PRODERP",
		user_name		=> "xxnmc_his",
		password		=> "xxnmc8030",
	);

##Postgres Database details left side is key and right side is value(Its a key value map)

my %pg_db = (
		host			=> "127.0.0.1",
		port			=> "5432",
		database_name	=> "hms",
		schema 			=> "nmc",
		user_name		=> "postgres",
		password		=> "",
	);

sub getOracleDbDetails {
	return %ora_db;
}

sub getPgDbDetails {
	return %pg_db;
}
