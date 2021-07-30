#!/usr/bin/perl
package scm_grn_sync;
use 5.10.1;
use strict;
use Try::Tiny;
use File::Basename;
use DBI;
use DBD::Pg qw(:pg_types);
use lib dirname($0)."/../lib";  # find our SCM  module. From current directory eg from bin to ../../lib .
use SCM::ADJTableMap;
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



my $rows_found = 0;
my $rows_i = 0;
my $rows_if = 0;
my $adj_main_i_count = 0;
my $adj_details_i_count = 0;
my $adj_main_ie_count = 0;
my $adj_details_ie_count = 0;
my $stock_update_count = 0;
my $stock_update_err_count = 0;

my $rollback_count = 0;


my %ora_selected_rows = (
		SAM		=> 0,  #No of rows selected from oracle store_adj_main table
		SAD		=> 0,  #No of rows selected from oracle store_adj_details table
		ORA_SAMU	=> 0,  #No of rows processed in oracle store_adjustment_main_update
	);

my %store_adj_main_map = undef;
my %store_adj_details_map = undef;


sub init {

		$p_dbh->do("set search_path to $p_schema") or die $DBI::errstr;

		%store_adj_main_map = SCM::ADJTableMap::getTableFieldMap("store_adj_main");
		%store_adj_details_map = SCM::ADJTableMap::getTableFieldMap("store_adj_details");

		my @ora_store_adj_main_col = join(", ", keys %store_adj_main_map);
		my @pg_store_adj_main_col = join(", ", values %store_adj_details_map);

		my @ora_store_adj_details_map_col = join(", ", keys %store_adj_details_map);
		my @pg_store_adj_details_map_col = join(", ", values %store_adj_details_map);

##   Mapping Oracle  ADJ_NO to adj_no Postgres adj_no which is generating by sequence BY IHS and storing it into ADJ_NO.
##   Original ADJ_NUM which is comming from ERP storing in external_adj_num

		$store_adj_main_map{'ADJ_NO'} 	 = 'adj_no';
		$store_adj_details_map{'ADJ_NO'} = 'adj_no';


=comment
STORE ADJ MAIN QUERY
=cut


		my $ora_store_adj_main_query = qq{
						SELECT to_char(CREATION_DATE,'DD-MM-YYYY HH24:MI:SS') AS CREATION_DATE,@ora_store_adj_main_col
						FROM XXNMC_HIS_ADJ_OUT_MAIN WHERE INTERFACE_STATUS IS NULL ORDER BY CREATION_DATE
					};

		my $ora_store_adj_main_query_update = qq{UPDATE XXNMC_HIS_ADJ_OUT_MAIN SET INTERFACE_STATUS = 'P' WHERE ADJ_NUM = ?};

=comment
STORE ADJ DETAILS QUERY
=cut

		my $ora_store_adj_details_query =  qq{
						SELECT  @ora_store_adj_details_map_col
						FROM XXNMC_HIS_ADJ_OUT_DETAILS WHERE INTERFACE_STATUS IS NULL AND ADJ_NUM = ?
					};



=comment
	Store Item Batch Details Table
=cut



	my $is_item_batch_medicine_exists_query = qq{SELECT batch_no,medicine_id,item_batch_id FROM store_item_batch_details
								where batch_no = :batch_no and medicine_id = :medicine_id
							};

	my $issue_baseunit = qq{select issue_base_unit from store_item_details where medicine_id = :medicine_id};


=coment
	UPDATE QUERY store_stock_details
=cut

	my $max_item_lot_id_query = "select max(item_lot_id) from store_stock_details where package_cp > 0 AND dept_id = :dept_id and item_batch_id = :item_batch_id";

	my $store_stock_details_query_r = qq{
					update store_stock_details set qty=qty - :qty where dept_id = :dept_id and store_stock_id = :store_stock_id
					};

=comment
	my $store_stock_details_query_a = qq{
					UPDATE store_stock_details set qty=qty + :qty 
					WHERE item_lot_id = (select max(item_lot_id) from store_stock_details where package_cp > 0
					      AND dept_id = :dept_id and item_batch_id = :item_batch_id)
					};


=cut


	my $store_stock_details_query_a = qq{
					UPDATE store_stock_details set qty=qty + :qty 
					WHERE item_lot_id = (select max(item_lot_id) from store_stock_details where package_cp > 0
					      AND dept_id = :dept_id and item_batch_id = :item_batch_id)
					};





##Store ADJ MAIN STH


		my $ora_store_adj_main_sth = $o_dbh->prepare($ora_store_adj_main_query);
		my $ora_store_adj_main_update_sth = $o_dbh->prepare($ora_store_adj_main_query_update);


##STORE ADJ Details STH

		my $ora_store_adj_details_sth = $o_dbh->prepare($ora_store_adj_details_query);


#Item Batch Table

		my $is_item_batch_medicine_exists_sth = $p_dbh->prepare($is_item_batch_medicine_exists_query);
		my $issue_baseunit_sth = $p_dbh->prepare($issue_baseunit);
		my $max_item_lot_id_sth = $p_dbh->prepare($max_item_lot_id_query);
		my $store_stock_details_r_sth = $p_dbh->prepare($store_stock_details_query_r);
		my $store_stock_details_a_sth = $p_dbh->prepare($store_stock_details_query_a);
		my $ora_store_adj_main_query_update_sth = $o_dbh->prepare($ora_store_adj_main_query_update);

		my $error = 0;

		try{
			 print "\n".dateTime()."::INIT::SCM_ADJ\n";

			$ora_store_adj_main_sth->execute();
			while(my $store_adj_main_row = $ora_store_adj_main_sth->fetchrow_hashref){
				
				my $adj_sth_seq = $p_dbh->prepare("SELECT nextval('store_adj_details_seq'::regclass)");
				$adj_sth_seq->execute();
				my @adj_seq_arr = $adj_sth_seq->fetchrow_array;				

				my %store_adj_main_hash = %{$store_adj_main_row};
				$store_adj_main_hash{'ADJ_NO'} = $adj_seq_arr[0];
				delete $store_adj_main_hash{"CREATION_DATE"};
				$error = insert(\"store_adj_main",\%store_adj_main_hash,\%store_adj_main_map);

				if( $error gt 1 || $error le 0 ){
                                        print "\n".dateTime()."ERROR :: INSERT_ERROR :: Pg_Table = store_adj_main :: Oracle ADJ_NO = $store_adj_main_hash{'ADJ_NUM'}     Postgres ADJ_NO = $store_adj_main_hash{'ADJ_NO'}\n" ;
					$adj_main_ie_count = $adj_main_ie_count + 1;
					$rollback_count = $rollback_count + 1;

                                        next;

                                }else {

					$ora_store_adj_main_query_update_sth->bind_param(1,$store_adj_main_hash{'ADJ_NUM'});
					my $ora_update_c = $ora_store_adj_main_query_update_sth->execute();
					$adj_main_i_count = $adj_main_i_count + $error;
					$ora_selected_rows{"ORA_SAMU"} => $ora_selected_rows{"ORA_SAMU"} + $ora_update_c ;
				}

				try{
					$ora_store_adj_details_sth->bind_param(1,$store_adj_main_hash{'ADJ_NUM'});
					$ora_store_adj_details_sth->execute();
					while(my $store_adj_details_row = $ora_store_adj_details_sth->fetchrow_hashref){
						$error = 0;
						$is_item_batch_medicine_exists_sth->bind_param(":batch_no",$store_adj_details_row->{"BATCH_NUM"});
                                                $is_item_batch_medicine_exists_sth->bind_param(":medicine_id",$store_adj_details_row->{"MEDICINE_ID"});
                                                $is_item_batch_medicine_exists_sth->execute();
						my $count_batch = 0;
                                                while(my $is_item_batch_medicine_hash_ref =$is_item_batch_medicine_exists_sth->fetchrow_hashref){
                                                   my %item_batch_medicine_hash_ref = %{$is_item_batch_medicine_hash_ref};
                                                   $count_batch = $count_batch + 1;
                                                   $store_adj_details_row->{"ITEM_BATCH_ID"} = $item_batch_medicine_hash_ref{"item_batch_id"};
						   $max_item_lot_id_sth->bind_param(":item_batch_id",$item_batch_medicine_hash_ref{"item_batch_id"});
						   $max_item_lot_id_sth->bind_param(":dept_id",$store_adj_main_row->{'STORE_ID'});

                                                 }

						my %store_adj_details_hash = %{$store_adj_details_row};
						$store_adj_details_hash{'ADJ_NO'} = $adj_seq_arr[0];

						if($count_batch eq 0){
							print "ERROR::INSERT_ERRROR :: ITEM_BATCH_ID is not available for  MEDICINE_ID = $store_adj_details_hash{'MEDICINE_ID'} , BATCH_NO = $store_adj_details_hash{'BATCH_NUM'}   adj_no = $store_adj_main_hash{'ADJ_NUM'}";
							$rollback_count = $rollback_count + 1;

#							print "\n".dateTime()."ERROR :: INSERT_ERROR :: Pg_Table = store_adj_details ::ADJ_NO = $store_adj_details_hash{'ADJ_NUM'}  , MEDICINE_ID = $store_adj_details_hash{'MEDICINE_ID'} , BATCH_NO = $store_adj_details_hash{'BATCH_NUM'}\n" ;
                                                        $adj_details_ie_count = $adj_details_ie_count + 1;
							next;

						}

=comment
			Finding issue_base_unit and multiplying it into qty
=cut

						$issue_baseunit_sth->bind_param(':medicine_id',$store_adj_details_row->{"MEDICINE_ID"});
						$issue_baseunit_sth->execute();
						my @arr_issue_baseunit = $issue_baseunit_sth->fetchrow_array;
							
						if(scalar @arr_issue_baseunit eq 0){
							print "ERROR : issue_base_unit is not available for medicine_id : $store_adj_details_row->{'MEDICINE_ID'}  adj_no = $store_adj_main_hash{'ADJ_NUM'}";
						 $rollback_count = $rollback_count + 1;
						}
						
						$store_adj_details_hash{'QUANTITY'} =  $store_adj_details_hash{'QUANTITY'} * $arr_issue_baseunit[0];

						$error = insert(\"store_adj_details",\%store_adj_details_hash,\%store_adj_details_map);

						 if( $error gt 1 || $error le 0 ){
						      print "\n".dateTime()."ERROR :: INSERT_ERROR :: Pg_Table = store_adj_details ::ADJ_NO = $store_adj_details_hash{'ADJ_NUM'}  , MEDICINE_ID = $store_adj_details_hash{'MEDICINE_ID'} , BATCH_NO = $store_adj_details_hash{'BATCH_NUM'}\n" ;
		                                       $rollback_count = $rollback_count + 1;
							$adj_details_ie_count = $adj_details_ie_count + 1;
							next;
                                		}

						$adj_details_i_count = $adj_details_i_count + $error;
						$max_item_lot_id_sth->execute();
						my @max_item_lot_id = $max_item_lot_id_sth->fetchrow_array;
				
						

						 if($store_adj_details_row->{"TYPE"} eq 'R'){

							my $store_stock_detsils_item_with_id = $p_dbh->prepare("SELECT 	store_stock_id,qty,item_batch_id,item_lot_id,dept_id FROM store_stock_details WHERE qty >0 AND dept_id = $store_adj_main_row->{'STORE_ID'} AND item_batch_id =  $store_adj_details_row->{'ITEM_BATCH_ID'}");


							my $store_stock_detsils_item_with_id_row = $store_stock_detsils_item_with_id->execute();
							
							my $update_status = 0;

							print "INFO_QTY : Quantity to be adjust : $store_adj_details_hash{'QUANTITY'}, adj_num : $store_adj_details_hash{'ADJ_NUM'}, DEPT_ID : $store_adj_main_row->{'STORE_ID'}, ITEM_BATCH_ID : $store_adj_details_row->{'ITEM_BATCH_ID'}";

							while(my  $store_stock_detsils_item_with_id_row_hash_ref  =
									$store_stock_detsils_item_with_id_row->fetchrow_hashref){	

								my $qty = $store_stock_detsils_item_with_id_row_hash_ref->{qty};

								if($qty > $store_adj_details_hash{'QUANTITY'}) {

									$store_stock_details_r_sth->bind_param(':qty',  $store_adj_details_hash{'QUANTITY'});
									$store_stock_details_r_sth->bind_param(':store_stock_id', 
										$store_stock_detsils_item_with_id_row_hash_ref->{store_stock_id});

									$store_stock_details_r_sth->bind_param(':dept_id', 
													$store_adj_main_row->{'STORE_ID'});
									$update_status = $update_status + $store_stock_details_r_sth->execute();
										
								
									$p_dbh->do("INSERT INTO store_transaction_lot_details(transaction_id,transaction_type,item_lot_id,qty) VALUES( $store_adj_details_hash{'ADJ_NO'},'A',$store_stock_detsils_item_with_id_row_hash_ref->{item_lot_id},$store_adj_details_hash{'QUANTITY'})");
									
									$store_adj_details_hash{'QUANTITY'} = 0;

									print "ADJ_NO = $store_adj_details_hash{'ADJ_NUM'}, existing stock = $qty, 
										qty to be reduce : $store_adj_details_hash{'QUANTITY'}), $store_stock_detsils_item_with_id_row_hash_ref->{item_lot_id}";									
									

								}else {
									$store_adj_details_hash{'QUANTITY'} = $store_adj_details_hash{'QUANTITY'} - $qty;
									$store_stock_details_r_sth->bind_param(':qty',  $qty);
									$store_stock_details_r_sth->bind_param(':store_stock_id', 
										$store_stock_detsils_item_with_id_row_hash_ref->{store_stock_id});

									$store_stock_details_r_sth->bind_param(':dept_id', $store_adj_main_row->{'STORE_ID'});
									#my $update_status = 0;
									$update_status = $update_status + $store_stock_details_r_sth->execute();

									print "ADJ_NO = $store_adj_details_hash{'ADJ_NUM'}, existing stock = $qty, 
										qty to be reduce : $store_adj_details_hash{'QUANTITY'}, $store_stock_detsils_item_with_id_row_hash_ref->{item_lot_id})";									

								
									$p_dbh->do("INSERT INTO store_transaction_lot_details(transaction_id,transaction_type,item_lot_id,qty) VALUES( $store_adj_details_hash{'ADJ_NO'},'A',$store_stock_detsils_item_with_id_row_hash_ref->{item_lot_id},$qty)");


	

								  }

								if($store_adj_details_hash{'QUANTITY'} eq 0) {
									break;
								}

																

							}

							if($store_adj_details_hash{'QUANTITY'} > 0) {
								print "\nERROR_QTY :: Insufficient quqntity \n";

								$rollback_count =  $rollback_count + 1;
								
							}

							 if($update_status le 0 ){
                                                                print "\n".dateTime()."ERROR :: UPDATE_ERROR :: Pg_Table = 
								store_stock_details ITEM_BATCH_ID = $store_adj_details_row->{'ITEM_BATCH_ID'}, 
								STORE_ID = $store_adj_main_row->{'STORE_ID'} ,  
								QTY = $store_adj_details_row->{'QUANTITY'} * $arr_issue_baseunit[0] = 
								$store_adj_details_hash{'QUANTITY'} , type = $store_adj_details_row->{'TYPE'} ,  
								item_lot_id = $max_item_lot_id[0] , adj_no = $store_adj_main_hash{'ADJ_NUM'}";

                                                                $stock_update_err_count = $stock_update_err_count + 1;
                                                                $rollback_count = $rollback_count + 1;

                                                        }else{
                                                                $stock_update_count = $stock_update_count + $update_status;

                                                                print "\n Table  :: store_stock_detaisl  Updaeted : $update_status  
								qty =  $store_adj_details_row->{'QUANTITY'}  * $arr_issue_baseunit[0] = 
								$store_adj_details_hash{'QUANTITY'},   item_batch_id =  
								$store_adj_details_row->{'ITEM_BATCH_ID'}   , 
								store_id =  $store_adj_main_row->{'STORE_ID'}  
								type = $store_adj_details_row->{'TYPE'} ,  item_lot_id = $max_item_lot_id[0] , 
								adj_no = $store_adj_main_hash{'ADJ_NUM'}";

                                                        }


						}

						 if($store_adj_details_row->{"TYPE"} eq 'A'){

							
							
							print "INFO_QTY : Quantity to be adjust : $store_adj_details_hash{'QUANTITY'}, adj_num : $store_adj_details_hash{'ADJ_NUM'}, DEPT_ID : $store_adj_main_row->{'STORE_ID'}, ITEM_BATCH_ID : $store_adj_details_row->{'ITEM_BATCH_ID'}";

							$store_stock_details_a_sth->bind_param(':qty', $store_adj_details_hash{'QUANTITY'});
							$store_stock_details_a_sth->bind_param(':item_lot_id', $max_item_lot_id[0]);
							$store_stock_details_a_sth->bind_param(':dept_id', $store_adj_main_row->{'STORE_ID'});
							my $update_status = 0 ;

							$update_status = $store_stock_details_a_sth->execute();

					
							$p_dbh->do("INSERT INTO store_transaction_lot_details(transaction_id,transaction_type,item_lot_id,qty) VALUES( $store_adj_details_hash{'ADJ_NO'},'A',$max_item_lot_id[0],$store_adj_details_hash{'QUANTITY'})");


							print "ADJ_NO = $store_adj_details_hash{'ADJ_NUM'}, item_lot_id = $max_item_lot_id[0], 
										qty to be add : $store_adj_details_hash{'QUANTITY'})";									


	

							if($update_status le 0 ){
								print "\n".dateTime()."ERROR :: UPDATE_ERROR :: Pg_Table = store_stock_details ITEM_BATCH_ID = $store_adj_details_row->{'ITEM_BATCH_ID'} , STORE_ID = $store_adj_main_row->{'STORE_ID'} ,  QTY = $store_adj_details_row->{'QUANTITY'} * $arr_issue_baseunit[0] = $store_adj_details_hash{'QUANTITY'}, type = $store_adj_details_row->{'TYPE'} ,  item_lot_id = $max_item_lot_id[0] , adj_no = $store_adj_main_hash{'ADJ_NUM'}";

								$stock_update_err_count = $stock_update_err_count + 1;
								$rollback_count = $rollback_count + 1;

							}else{

								
                                                                $stock_update_count = $stock_update_count + $update_status;

                                                                 print "\n".dateTime()."S.NO :: $stock_update_count :: Pg_Table = store_stock_details ITEM_BATCH_ID = $store_adj_details_row->{'ITEM_BATCH_ID'} , STORE_ID = $store_adj_main_row->{'STORE_ID'} ,  QTY = $store_adj_details_row->{'QUANTITY'} i* * $arr_issue_baseunit[0] = $store_adj_details_hash{'QUANTITY'} , type = $store_adj_details_row->{'TYPE'} ,  item_lot_id = $max_item_lot_id[0], adj_no = $store_adj_main_hash{'ADJ_NUM'}";

							}


						}


					}
				}catch{

					print "ERROR :: INSERT_ERROR :: $_";
					$rollback_count = $rollback_count + 1;

				}finally{

					$ora_selected_rows{'SAD'}  = $ora_selected_rows{'SAD'} +  scalar $ora_store_adj_details_sth->rows();

				};

			}


		}catch{
			print "ERROR :: INSERT_ERROR :: $_";
			$rollback_count = $rollback_count + 1;

		}finally{

			my $status = 0;
			$ora_selected_rows{'SAM'}  = $ora_selected_rows{'SAM'} +  scalar $ora_store_adj_main_sth->rows() ;
			$rows_found = $ora_selected_rows{'SAM'} + $ora_selected_rows{'SAD'};
			$rows_i =  $adj_main_i_count + $adj_details_i_count;
			$rows_if = $adj_main_ie_count + $adj_details_ie_count;


                        my $table_wise_txn = "\n".dateTime()."Each table wise transaction details :".
                            "\n store_adj_main   : SELECTED = $ora_selected_rows{'SAM'} : INSERTED = $adj_main_i_count ,  INSERT_FAIL = $adj_main_ie_count".
                           "\n store_adj_details : SELECTED = $ora_selected_rows{'SAD'} : INSERTED = $adj_details_i_count ,  INSERT_FAIL = $adj_details_ie_count".
                           "\n store_stock_details: UPDATED = $stock_update_count	: UPDATE_FAIL = $stock_update_err_count";


                        my $all_txn = "\n..........................................................................................................".
                                "\n".dateTime()." All Transaction Details : \n".
                                "               Total Oracle Rows selected : $rows_found\n".
                                "               No of rows inserted : $rows_i\n".
                                "               Insert fails : $rows_if\n".
                                "               Total Oracle Invoice Table Updated : $ora_selected_rows{'ORA_SAMU'}".
                                "\n\n___________________________________________________________________________________________________________\n\n";

                        my $commit_detail_print;
                        if($rollback_count gt 0) {
                                $p_dbh->rollback or $DBI::strerr; $o_dbh->rollback or $DBI::strerr;

                        }else {
#                                $p_dbh->commit or $DBI::strerr;
#                                $o_dbh->commit; $status = 1;
                                $commit_detail_print = dateTime()."Note : All Transaction commited successfully\n\n"

                        }

                        if($status eq 0) {

                                $o_dbh->rollback or $DBI::strerr;
                                $commit_detail_print = dateTime()."Note : All above transaction is rolling back due to previous error !\n\n";
#                                SCM::Email::sendMail("SCM ADJ Sync details",  $table_wise_txn . $all_txn . $commit_detail_print);
                        }

                        print $table_wise_txn . $all_txn . $commit_detail_print;

		};
	}




sub insert {

	my $pg_table_name = ${$_[0]};

	my %hash_row =  %{@_[1]};

	my %hash = %{@_[2]};

	my $error = 0;

	my $p_sth = undef;

	try{



		my $p_insertrow_sql = preapareInsertStatement(\$pg_table_name, \%hash_row,\%hash);

# 		print "\nSQL_INSERT :  $p_insertrow_sql \n\n";

		$p_sth = $p_dbh->prepare($p_insertrow_sql);

		bind_param(\%hash_row, \$p_sth);

		$error = $p_sth->execute();

		my $rows = $p_sth->rows;
#		print "No of rows inserted :" . $rows. "\n";
		$p_sth->finish();

	}catch {
		print "\n";
		print "\nERROR : INSERT_ERROR : While inserting data in Postgresql,  Error : $_";
		$rollback_count = $rollback_count + 1;
		if (defined $p_sth) {

			$p_dbh->rollback();

			$p_dbh->do("set search_path to $p_schema") or $DBI::errstr;
			$p_sth->finish();
			my $rows = $p_sth->rows;
			$error = -1;
		}


	};

	return $error;
}


sub preapareInsertStatement {

	my $pg_table_name = ${$_[0]};
	my %hash_row = %{ @_[1]};
        my %hash =  %{@_[2]};

	my $field = "INSERT INTO $pg_table_name (";
	my $values = "VALUES(";


	foreach my $keys (keys %hash_row) {

		$field = $field . $hash{uc($keys)} . " , ";
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
        my ($day,$mon,$date,$time,$year) = split /\s+/, $time_string;
        my $date_log = "$year-$mon-$date  $time :: ";
        return $date_log;
}


init

#SCM::Log::rotateLog(2*1024*1024*1024)
