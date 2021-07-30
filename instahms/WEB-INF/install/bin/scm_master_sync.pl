#!/usr/bin/perl

#package scm_data_dump;
#use strict;
use 5.10.1;
use DBI;
use Try::Tiny;
use File::Basename;
use lib dirname($0)."/../lib";	# find our SCM  module. From current directory eg from bin to ../../lib .
use SCM::MasterTableMap;
use SCM::Log;
use SCM::DBConf;
use SCM::Email;
$ENV{'TNS_ADMIN'} = '/usr/lib/oracle/11.2/client64';
my %oracle_db_details = SCM::DBConf::getOracleDbDetails();
my %pg_db_details = SCM::DBConf::getPgDbDetails();
my $p_schema = $pg_db_details{schema};

my $o_dbh = DBI->connect( "DBI:Oracle:$oracle_db_details{database_name}","$oracle_db_details{user_name}","$oracle_db_details{password}",{
                                                                        PrintError => 0,
                                                                        PrintWarn => 1,
                                                                        RaiseError => 1,
                                                                        AutoCommit => 0,
                                                                        ShowErrorStatement => 1,
                                                                        }) || "\n".die dateTime()."ERROR :: DB_ERROR :: ORA_ERROR : $DBI::errstr";


my $p_dbh = DBI->connect("DBI:Pg:dbname=$pg_db_details{database_name};host=$pg_db_details{host};port=$pg_db_details{port}",
                                                        $pg_db_details{user_name},$pg_db_details{password},{
                                                                                                        PrintError => 0,
                                                                                                        PrintWarn => 1,
                                                                                                        RaiseError => 1,
                                                                                                        AutoCommit => 0,
                                                                                                        ShowErrorStatement => 1,
						                                                                }) || "\n".die dateTime()."ERROR :: DB_ERROR :: PG_ERROR : $DBI::errstr";



my $ir = 0; my $ur = 0; my $if = 0; my $ufe = 0; my $uf = 0; my $I = 0; my $U = 0; my $rows_found = 0;
my $rollback_count = 0;
my $ora_rollback_count = 0;
my @tablerecord = undef;
my @store_rate_plan_arr ;

my $store_item_rate_plan_query = qq{insert into store_item_rates(store_rate_plan_id,medicine_id,selling_price,tax_type,tax_rate) values(:store_rate_plan_id, :medicine_id, 0, 'MB',0)};


my $ha_item_code_type_query = qq{insert into ha_item_code_type(ha_code_type_id,health_authority,medicine_id,code_type) values(:ha_code_type_id,:health_authority,:medicine_id,:code_type)};
my $store_item_codes_query = qq{insert into store_item_codes(code_id,medicine_id,code_type,item_code,desc_name) values(:code_id,:medicine_id,:code_type,:item_code,:desc_name)};

my $store_item_codes_update_query = qq{update store_item_codes set item_code = :item_code,desc_name = :desc_name where medicine_id = :medicine_id and code_type = :code_type};




my $ha_item_code_type_sth = $p_dbh->prepare($ha_item_code_type_query);

my $store_item_codes_sth = $p_dbh->prepare($store_item_codes_query);

my $store_item_codes_update_sth = $p_dbh->prepare($store_item_codes_update_query);

my $store_item_rate_plan_sth = $p_dbh->prepare($store_item_rate_plan_query);


sub init {

	my $table_wise_txn;
	my $all_txn;
	$p_dbh->do("set search_path to $p_schema") or die dateTime()."ERROR :: SCHEMA_ERROR :: $DBI::errstr";
	my @table_list = (

###Don't un comment			Left side Oracle table name and right side filter name.
### Dont't un comment 			XXNMC_HIS_CATEGORY_MASTER_STG => ITEM , INSURANCE , ASSET, SUPPLIER,



			"ITEM => category_id",  					# Table_1 : store_category_master
			"XXNMC_HIS_MANUF_MASTER_STG => manf_code",   			# Table_2 : manf_master
			"XXNMC_HIS_GENERIC_MASTER_STG => generic_code",    		# Table_3 : generic_name
			"XXNMC_HIS_ITEM_FORM_MTR_STG => item_form_id",    		# Table_4 : item_form_master
			"XXNMC_HIS_ITEM_CONTROL_MTR_STG => control_type_id",   		# Table_5 : store_item_controltype
			"XXNMC_HIS_ROUTE_OF_ADMIN_STG => route_id",  			# Table_6 : medicine_route
			"XXNMC_HIS_ISSUE_UOM_MTR_STG => package_uom",     		# Table_7 : package_issue_uom
			"XXNMC_HIS_STRENGTH_UNIT_STG => unit_id",  			# Table_8 : strength_unit
			"INSURANCE => insurance_category_id",  				# Table_9 : item_insurance_categories
			"XXNMC_HIS_STORE_ITEM_DTLS_STG => medicine_id",  		# Table_10 : store_item_details
##			 ASSET => '',
			"SUPPLIER => supp_category_id",					# Table_11 : supplier_category_master
			"XXNMC_HIS_SUPPLIER_MTR_STG => supplier_code"  			# Table_12 : supplier_master

		);

		try {

			print "\n\n" .dateTime()."::INIT::SCM_MASTER::_________________________________________________________________________\n";
		my $store_rate_plan_sth = $p_dbh->prepare("select store_rate_plan_id from store_rate_plans");
		$store_rate_plan_sth->execute();
		while(my $store_rate_pan_row = $store_rate_plan_sth->fetchrow_hashref) {
			push (@store_rate_plan_arr,values %{$store_rate_pan_row});
		}


#		my @table_keys =  keys %table_list;
		my $table_size = @table_list;
		for(my $temp = 0 ; $temp < $table_size;++$temp) {
			my @t;
			@t = split (/=>/,$table_list[$temp]);
			$t[0] =~ s/^\s+|\s+$//g;
			$t[1] =~ s/^\s+|\s+$//g;
#			print $t[0];
#			print $t[1];
			importData($t[0],$t[1]);
		}
			print "\n".dateTime().".................................................................................................\n";

			$table_wise_txn = dateTime()."Each table wise transaction details :";

			foreach my $pg_table_n (@tablerecord) {
				$table_wise_txn = $table_wise_txn . $pg_table_n."\n";
			}
			$table_wise_txn =  $table_wise_txn . "\n".dateTime()."....................................................................\n";
			$all_txn = "\n".dateTime()."All Transaction Details : \n".
				dateTime()."		Total Oracle Rows selected : $rows_found\n".
				dateTime()."		No of rows inserted : $ir\n".
				dateTime()."		Insert fails : $if\n".
				dateTime()."		No of rows updated : $ur\n".
				dateTime()."		Update fail query error	: $ufe\n".
				dateTime()."		Update fail might be data not available : $uf\n".

				"\n\n".dateTime()."_____________________________________________________________________________________________\n\n";



		}finally {

			$store_item_rate_plan_sth->finish;

			$ha_item_code_type_sth->finish;
			$store_item_codes_sth->finish;
			$store_item_codes_update_sth->finish;

			my $status = 0;
			my $commit_detail_print;
			if(($rollback_count gt 0) or ($ora_rollback_count gt 0) ) {

				$commit_detail_print = dateTime()."Note : Data is rolling back due to previous error.\n";

				$p_dbh->rollback or $DBI::strerr; $o_dbh->rollback or $DBI::strerr;

			}else {

				$p_dbh->commit;
				$o_dbh->commit;	$status = 1;
				$commit_detail_print = dateTime()."Note : All Changeses Commited Successfully\n\n";

			}

			if($status eq 0) {

				$commit_detail_print =  $commit_detail_print . dateTime()."Note : Transaction is not committing , Data will not reflact in database\n";
				$o_dbh->rollback or $DBI::strerr;
#				SCM::Email::sendMail("SCM Master Sync details",  $table_wise_txn . $all_txn . $commit_detail_print);
			}

			 print $table_wise_txn . $all_txn . $commit_detail_print;

			$p_dbh->disconnect()  or warn $p_dbh->errstr;
			$o_dbh->disconnect()  or warn $o_dbh->errstr;

		};
	}






sub importData{

	local ($ir_t = 0 , $ur_t = 0 , $if_t = 0, $ufe_t = 0, $uf_t = 0);

	my ($ora_table_name,$check_column) = @_;

	print "\n".dateTime()."Table_Name= $ora_table_name , column = $check_column","\n";

	print "\n".dateTime()."Starting Operation for Tables : $ora_table_name : ";

		print "\n\n";
		if($ora_table_name  eq '' || $ora_table_name eq undef) {
			print "\n Please provide the right table name\n";
			return;
		}

		local  ($pg_table_name , %hash, $src_rows, $dest_rows, $query);

		if ($ora_table_name eq 'ITEM') {

			$pg_table_name = SCM::MasterTableMap::getPgTableName($ora_table_name);

			print "\n".dateTime()."Correspondig Pg_TableName = $pg_table_name\n";

			%hash = SCM::MasterTableMap::getTableFieldMap($pg_table_name);


			if (!keys %hash) {

				print "Empty Field Map for the Table : $pg_table_name\n";
				return ;
			}

			$src_rows = join(",",keys %hash); #oracle column
			$dest_rows = join(",",values %hash); #postgres column

			$query = "SELECT NEW_UPDATE_FLAG, INTERFACE_STATUS, TO_CHAR(LAST_UPDATE_DATE,'DD-MM-YYYY HH24:MI:SS'), $src_rows FROM XXNMC_HIS_CATEGORY_MASTER_STG  where CATEGORY_TYPE = 'ITEM' AND INTERFACE_STATUS IS NULL order by LAST_UPDATE_DATE asc";


#			print "\nSelect query  :  $query\n";


		} elsif ($ora_table_name eq 'INSURANCE') {

			$pg_table_name = SCM::MasterTableMap::getPgTableName($ora_table_name);

			print "\n".dateTime()."Correspondig Pg_TableName = $pg_table_name\n";

			%hash = SCM::MasterTableMap::getTableFieldMap($pg_table_name);


			if (!keys %hash) {

				print "Empty Field Map for the Table : $pg_table_name\n";
				return ;
			}

			$src_rows = join(",",keys %hash);
			$dest_rows = join(",",values %hash);

			$query = "SELECT NEW_UPDATE_FLAG,INTERFACE_STATUS, TO_CHAR(LAST_UPDATE_DATE,'DD-MM-YYYY HH24:MI:SS'), $src_rows FROM XXNMC_HIS_CATEGORY_MASTER_STG where CATEGORY_TYPE = 'INSURANCE' AND INTERFACE_STATUS IS NULL order by LAST_UPDATE_DATE asc";


#			print "\nSelect query  :  $query\n";


		} elsif ($ora_table_name eq 'SUPPLIER') {

			$pg_table_name = SCM::MasterTableMap::getPgTableName($ora_table_name);

			print "\n".dateTime()."Correspondig Pg_TableName = $pg_table_name\n";

			%hash = SCM::MasterTableMap::getTableFieldMap($pg_table_name);


			if (!keys %hash) {

				print "Empty Field Map for the Table : $pg_table_name\n";
				return ;
			}

			$src_rows = join(",",keys %hash);
			$dest_rows = join(",",values %hash);

			$query = "SELECT NEW_UPDATE_FLAG,INTERFACE_STATUS, TO_CHAR(LAST_UPDATE_DATE,'DD-MM-YYYY HH24:MI:SS'), $src_rows FROM XXNMC_HIS_CATEGORY_MASTER_STG  where CATEGORY_TYPE = 'SUPPLIER' AND INTERFACE_STATUS IS NULL order by LAST_UPDATE_DATE asc";


#			print "\nSelect query  :  $query\n";


		} else {
			$pg_table_name = SCM::MasterTableMap::getPgTableName($ora_table_name);

			print "\n".dateTime()."Correspondig Pg_TableName = $pg_table_name\n";

			%hash = SCM::MasterTableMap::getTableFieldMap($pg_table_name);


			if (!keys %hash) {

				print "Empty Field Map for the Table : $pg_table_name\n";
				return ;
			}

			$src_rows = join(",",keys %hash);
			$dest_rows = join(",",values %hash);

			$query = "SELECT NEW_UPDATE_FLAG,INTERFACE_STATUS, TO_CHAR(LAST_UPDATE_DATE,'DD-MM-YYYY HH24:MI:SS'), $src_rows FROM $ora_table_name WHERE INTERFACE_STATUS IS NULL order by LAST_UPDATE_DATE asc";


#			print "\nSelect query  :  $query\n";
		}
			my $o_sth = undef;
			my $error = 0;

		try {
				$o_sth = $o_dbh->prepare($query);

				$o_sth->execute();

		}catch {
			print "\nError in selecting Table from Oracle Table :  $pg_table_name,  Error : $_ \n";
			$error = 1;

		};

			my %hash_row= undef;

			my $NandPorUandP_local = 0;
			while(my $it_row =   $o_sth->fetchrow_hashref()){

			        %hash_row= %{$it_row};

				delete $hash_row{"TO_CHAR(LAST_UPDATE_DATE,'DD-MM-YYYYHH24:MI:SS')"};

		       		if($it_row->{'NEW_UPDATE_FLAG'} eq "N" ) {


					delete $hash_row{'NEW_UPDATE_FLAG'};
					delete $hash_row{'INTERFACE_STATUS'};
					++$I;
					print "\n\n  $I ) Inserting rows in table : $pg_table_name \n\n";

					insert(\$pg_table_name, \%hash_row, \%hash );

					print "\n\n".dateTime()."Insertion over for  : $pg_table_name For id : $check_column = $hash_row{uc($check_column)}  \n\n";

			       }elsif($it_row->{'NEW_UPDATE_FLAG'} eq "U") {
					 delete $hash_row{'NEW_UPDATE_FLAG'};
					 delete $hash_row{'INTERFACE_STATUS'};

					++$U;

					print "\n\n  $U ) Updating  : $pg_table_name \n\n";
					 update(\$pg_table_name, \%hash_row, \%hash , \$check_column);

					print "\n\n".dateTime()."Update operation completed for : $pg_table_name : For id : $check_column = $hash_row{uc($check_column)}\n\n";


			       }

			}


			my $ora_err="SUCCESS";
			try{

				if ($ora_table_name eq 'ITEM'){

					my $o_sth = $o_dbh->prepare("UPDATE XXNMC_HIS_CATEGORY_MASTER_STG SET INTERFACE_STATUS = 'P' where CATEGORY_TYPE = 'ITEM'");
					$o_sth->execute();
					$o_sth->finish();

				}elsif ($ora_table_name eq 'SUPPLIER') {
					my $o_sth = $o_dbh->prepare("UPDATE XXNMC_HIS_CATEGORY_MASTER_STG SET INTERFACE_STATUS = 'P' where CATEGORY_TYPE = 'SUPPLIER'");
					$o_sth->execute();
					$o_sth->finish();
				} elsif ($ora_table_name eq 'INSURANCE') {
					my $o_sth = $o_dbh->prepare("UPDATE XXNMC_HIS_CATEGORY_MASTER_STG SET INTERFACE_STATUS = 'P' where CATEGORY_TYPE = 'INSURANCE'");
					$o_sth->execute();
					$o_sth->finish();
				}else {
					my $o_sth = $o_dbh->prepare("UPDATE $ora_table_name SET INTERFACE_STATUS = 'P'");
					$o_sth->execute();
					$o_sth->finish();
				}
			}catch{
				print "\n\n ORA_UPDATE_ERROR : ERROR : In updating INTERFACE_STATUS column of table $ora_table_name\n\n";
				print "Error : $_\n";
				 $ora_rollback_count = $ora_rollback_count + 1 ;
				$ora_err="FAIL";
				$o_dbh->rollback or $DBI::strerr;
			};

			my $rf = $o_sth->rows;
			$rows_found = $rows_found + $rf;

			print "\nTable = $pg_table_name  :  Number of rows found =  " . $rf ."\n";
#			print dateTime()." INSERTED = $ir_t:UPDATED = $ur_t:INSERT FAIL $if_t:UPDATE FAIL $ufe:UPDATE FAIL BECAUSE DATA MIGHT BE NOT AVAILABLE = $uf_t:ORA_UPDATE = $ora_err \n";

			local @table_rec = ("$pg_table_name : Total Rows Fetched = $rf : INSERTED = $ir_t ,UPDATED = $ur_t ,INSERT_FAIL = $if_t ,UPDATE_FAIL_SQL_ERROR = $ufe , UPDATE_FAIL_DATA_NOT_AVAILABLE = $uf_t, ORA_UPDATE = $ora_err ");
			push(@tablerecord,@table_rec);
			$o_sth->finish;
			$o_sth=undef;
}



sub insert {

	my $pg_table_name = ${$_[0]};

	my %hash_row =  %{@_[1]}; ### single row of data from oracle database

	my %hash = %{@_[2]}; ### combination of oracle -> postgres column of given table

	my $error = 0;

	my $ha_item_code_type_seq_sth = $p_dbh->prepare("SELECT nextval('ha_item_code_type_seq'::regclass)");
	my $store_item_codes_seq_sth = $p_dbh->prepare("SELECT nextval('store_item_codes_seq'::regclass)");

	my $p_sth = undef;

	try{


		if($pg_table_name eq "store_item_details") {

		## Insert store_rate plan for store_item_details

                        $store_item_rate_plan_sth->bind_param(":medicine_id",$hash_row{uc("medicine_id")});

                        foreach(@store_rate_plan_arr){
                                $store_item_rate_plan_sth->bind_param(":store_rate_plan_id",$_);
                                $store_item_rate_plan_sth->execute();
                        }

                        my $HAAD_CODE = $hash_row{'HAAD_CODE'};
                        my $MOH_CODE = $hash_row{'MOH_CODE'};
                        my $CODE_TYPE = $hash_row{'CODE_TYPE'};
                        my $CODE_DESC = $hash_row{'CODE_DESC'};
                        print "\n HAAD_CODE = $HAAD_CODE  MOH_CODE=$MOH_CODE  CODE_TYPE=$CODE_TYPE  CODE_DESC = $CODE_DESC\n";
                        delete $hash_row{'HAAD_CODE'};
                        delete $hash_row{'MOH_CODE'};
                        delete $hash_row{'CODE_TYPE'};
                        delete $hash_row{'CODE_DESC'};


		  if($MOH_CODE ne undef) {

                                $store_item_codes_seq_sth->execute();
                                $ha_item_code_type_seq_sth->execute();
                                my @store_item_codes_seq_arr = $store_item_codes_seq_sth->fetchrow_array;
                                my @ha_item_code_type_seq_arr = $ha_item_code_type_seq_sth->fetchrow_array;

                                my $code_type = "$CODE_TYPE DHA";
                                $ha_item_code_type_sth->bind_param(":ha_code_type_id",$ha_item_code_type_seq_arr[0]);
                                $ha_item_code_type_sth->bind_param(":health_authority","DHA");
                                $ha_item_code_type_sth->bind_param(":medicine_id",$hash_row{uc("medicine_id")});
                                $ha_item_code_type_sth->bind_param(":code_type",$code_type);
                                $ha_item_code_type_sth->execute();

                                $store_item_codes_sth->bind_param(":code_id",$store_item_codes_seq_arr[0]);
                                $store_item_codes_sth->bind_param(":medicine_id",$hash_row{uc("medicine_id")});
                                $store_item_codes_sth->bind_param(":code_type",$code_type);
                                $store_item_codes_sth->bind_param(":item_code",$MOH_CODE);
                                $store_item_codes_sth->bind_param(":desc_name",$CODE_DESC);
                                $store_item_codes_sth->execute();
                        }


		if($HAAD_CODE ne undef){

                                $store_item_codes_seq_sth->execute();
                                $ha_item_code_type_seq_sth->execute();
                                my @store_item_codes_seq_arr = $store_item_codes_seq_sth->fetchrow_array;
                                my @ha_item_code_type_seq_arr = $ha_item_code_type_seq_sth->fetchrow_array;

                                my $code_type = "$CODE_TYPE HAAD";
                                $ha_item_code_type_sth->bind_param(":ha_code_type_id",$ha_item_code_type_seq_arr[0]);
                                $ha_item_code_type_sth->bind_param(":health_authority","HAAD");
                                $ha_item_code_type_sth->bind_param(":medicine_id",$hash_row{uc("medicine_id")});
                                $ha_item_code_type_sth->bind_param(":code_type",$code_type);
                                $ha_item_code_type_sth->execute();

                                $store_item_codes_sth->bind_param(":code_id",$store_item_codes_seq_arr[0]);
                                $store_item_codes_sth->bind_param(":medicine_id",$hash_row{uc("medicine_id")});
                                $store_item_codes_sth->bind_param(":code_type",$code_type);
                                $store_item_codes_sth->bind_param(":item_code",$HAAD_CODE);
                                $store_item_codes_sth->bind_param(":desc_name",$CODE_DESC);
                                $store_item_codes_sth->execute();

			 }


        }

		my $p_insertrow_sql = preapareInsertStatement(\$pg_table_name, \%hash_row,\%hash);

#		print "\nSQL_INSERT :  $p_insertrow_sql \n\n";

		$p_sth = $p_dbh->prepare($p_insertrow_sql);

		bind_param(\%hash_row, \$p_sth);


		$p_sth->execute();

		my $rows = $p_sth->rows;
		$ir = $ir + $rows;
		$ir_t = $ir_t + $rows;
		print "No of rows inserted :" . $rows. "\n";
		$p_sth->finish();

	}catch {
		print "\n";
		print "\nERROR : INSERT_ERROR : While inserting data in Postgresql,  Error : $_";
		$rollback_count = $rollback_count + 1;
		$p_dbh->rollback();
		$p_dbh->do("set search_path to $p_schema") or $DBI::errstr;
		my $rows = 1;
		$if = $if + $rows;
		$if_t = $if_t + $rows;
		$error=1;
		if (defined $p_sth) {
			$p_sth->finish();
		}

	}finally{
		$ha_item_code_type_seq_sth->finish;
		$store_item_codes_seq_sth->finish;
	};

	return if $error;

}


sub update {


	my $size = @_;
	my $pg_table_name = ${$_[0]};
	my %hash_row = %{@_[1]};
	my %hash =  %{@_[2]};
	my $check_column = ${$_[3]};

        my $error = 0;

	if($size lt 4) {
		print "Insufficient data for insert : parameter for update : ". $size . "Table : $pg_table_name";
		return ;
	}

	 if($pg_table_name eq "store_item_details") {

                my $HAAD_CODE = $hash_row{'HAAD_CODE'};
                my $MOH_CODE = $hash_row{'MOH_CODE'};
                my $CODE_TYPE = $hash_row{'CODE_TYPE'};
                my $CODE_DESC = $hash_row{'CODE_DESC'};

                delete $hash_row{'HAAD_CODE'};
                delete $hash_row{'MOH_CODE'};
                delete $hash_row{'CODE_TYPE'};
                delete $hash_row{'CODE_DESC'};


               $store_item_codes_update_sth->bind_param(":desc_name",$CODE_DESC);
               $store_item_codes_update_sth->bind_param(":medicine_id",$hash_row{uc("medicine_id")});

                if($MOH_CODE ne undef) {
                    my $code_type = "$CODE_TYPE DHA";
                    $store_item_codes_update_sth->bind_param(":item_code",$MOH_CODE);
                    $store_item_codes_update_sth->bind_param(":code_type",$code_type);
                	$store_item_codes_update_sth->execute();
                }

                if($HAAD_CODE ne undef){

                    my $code_type = "$CODE_TYPE HAAD";
                     $store_item_codes_update_sth->bind_param(":item_code",$HAAD_CODE);
                    $store_item_codes_update_sth->bind_param(":code_type",$code_type);
                	$store_item_codes_update_sth->execute();
                }
	}


	my $update_query = "update $pg_table_name set ";
	foreach my $key (keys %hash_row) {

		$update_query = $update_query .  $hash{lc("$key")} . " = :$key , ";
	}

	chop($update_query);
	chop($update_query);

#	print "\nSQL_UPDATE : $update_query\n\n";
	$update_query = $update_query . " where ". $hash{"$check_column"} ." = :c_$check_column  ";

	my $p_sth = undef;

	my $uerr=0;

	try{
		$p_sth = $p_dbh->prepare($update_query);

		bind_param(\%hash_row, \$p_sth);

		$p_sth->bind_param(":c_$check_column",$hash_row{uc($check_column)});

		$p_sth->execute();

		my $no_of_rows_updated = $p_sth->rows;

		if($no_of_rows_updated eq 0) {

			$uf = $uf + 1;
			$uf_t = $uf_t + 1;
			print "\n ERROR : UPDATE_ERROR : Fail to update the table :  $pg_table_name   For id : $check_column = $hash_row{uc($check_column)}  :   Rows is not available in database or somthing happen wrong.\n";
			$uerr = 1;

		}

		print " \n\nNo of rows updated = ".  $no_of_rows_updated. "\n";


		$p_sth->finish();

		return if $uerr;

		$ur = $ur + $no_of_rows_updated;
		$ur_t = $ur_t + $no_of_rows_updated;


	} catch{
		print "\n ERROR : UPDATE_ERROR : Table Update faile : $pg_table_name  : Error : $_\n";
		print "\n\n\n";
		$error = 1;
		$p_dbh->rollback();
		$rollback_count = $rollback_count + 1;
		$p_dbh->do("set search_path to $p_schema") or $DBI::errstr;
		my $no_of_rows_updated = 1;
		$ufe = $ufe + $no_of_rows_updated;
		$ufe_t = $ufe_t + $no_of_rows_updated;
		if (defined $p_sth ) {
			$p_sth->finish();
		}
	};

}

sub preapareInsertStatement {

	my $pg_table_name = ${$_[0]};
	my %hash_row = %{ @_[1]};  ### single row of data from oracle database
        my %hash =  %{@_[2]};  ### combination of oracle -> postgres column of given table

	my $field = "INSERT INTO $pg_table_name (";
	my $values = "VALUES(";



	foreach my $keys (keys %hash_row) {

		$field = $field . $hash{lc($keys)} . " , ";
		$values = $values . ":$keys , ";
	}

	chop($field);chop($field);
	chop($values);chop($values);

	return ( $field . " ) " . $values . " ) ");

}

sub bind_param {
	my %hash_row = %{@_[0]};

	foreach my $key (keys %hash_row) {
		${@_[1]}->bind_param(":$key",$hash_row{uc($key)});
	}
}

sub dateTime {

	 my $time_string = scalar localtime();
        ($day,$mon,$date,$time,$year) = split /\s+/, $time_string;
        my $date_log = "$year-$mon-$date  $time :: ";
        return $date_log;
}

init


#SCM::Log::rotateLog(2*1024*1024*1024)

