#!/usr/bin/perl
package scm_grn_sync;
use 5.10.1;
use strict;
use Try::Tiny;
use File::Basename;
use DBI;
use DBD::Pg qw(:pg_types);
use lib dirname($0)."/../lib";  # find our SCM  module. From current directory eg from bin to ../../lib .
use SCM::GRNTableMap;
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
my $invoice_i_c = 0;
my $grn_main_i_c = 0;
my $grn_details_i_c = 0;
my $invoice_ie_c = 0;
my $grn_main_ie_c = 0;
my $grn_details_ie_c = 0;

my $rollback_count = 0;


my %ora_selected_rows = (
		SI		=> 0,
		SGM		=> 0,
		SGD 		=> 0,
		ORA_SIU		=> 0,
	);

my %store_invoice_map = undef;
my %store_grn_main_map = undef;
my %store_grn_details_map = undef;


sub init {

		$p_dbh->do("set search_path to $p_schema") or die $DBI::errstr;

		%store_invoice_map = SCM::GRNTableMap::getTableFieldMap("store_invoice");
		%store_grn_main_map = SCM::GRNTableMap::getTableFieldMap("store_grn_main");
		%store_grn_details_map = SCM::GRNTableMap::getTableFieldMap("store_grn_details");

		my @ora_invoice_col = join(", ", keys %store_invoice_map);
		my @pg_invoice_col = join(", ", values %store_invoice_map);

		my @ora_grn_main_col = join(", ", keys %store_grn_main_map);
		my @pg_grn_main_col = join(", ", values %store_grn_main_map);

		my @ora_grn_details_col = join(", ", keys %store_grn_details_map);
		my @pg_grn_details_col = join(", ", values %store_grn_details_map);


=comment
Invoice Table Query
=cut


		my $ora_invoice_query = qq{
						SELECT to_char(CREATION_DATE,'DD-MM-YYYY HH24:MI:SS') AS CREATION_DATE, NEW_UPDATE_FLAG, @ora_invoice_col
						FROM XXNMC_STORE_IVOICE_STG WHERE INTERFACE_STATUS IS NULL ORDER BY CREATION_DATE
					};

		my $ora_invoice_query_update = qq{UPDATE XXNMC_STORE_IVOICE_STG SET INTERFACE_STATUS = 'P' WHERE SUPPLIER_INVOICE_ID = ?};

=comment
GRN Main Table Query
=cut

		my $ora_grn_main_query =  qq{
						SELECT NEW_UPDATE_FLAG, @ora_grn_main_col
						FROM XXNMC_STORE_GRN_MAIN_STG WHERE INTERFACE_STATUS IS NULL AND SUPPLIER_INVOICE_ID = ?
					};

		my $ora_grn_main_query_count =  qq{SELECT COUNT(*) FROM XXNMC_STORE_IVOICE_STG WHERE INTERFACE_STATUS IS NULL AND SUPPLIER_INVOICE_ID = ?};



=comment
GRN details Table Query
=cut


	my $ora_grn_details = qq{SELECT NEW_UPDATE_FLAG, @ora_grn_details_col FROM XXNMC_HIS_GRN_LINE_DETAILS_STG WHERE INTERFACE_STATUS IS NULL AND GRN_NO = ? };



=comment
	Store Item Batch Details Table
=cut



		my $is_item_batch_medicine_exists_query = qq{SELECT batch_no,medicine_id,item_batch_id FROM store_item_batch_details where batch_no = :batch_no and medicine_id = :medicine_id };

		my $check_duplicate_medicine = qq{  select count(*) from store_grn_details where grn_no = :grn_no and item_batch_id = :item_batch_id};

		my $item_batch_insert_query = qq{INSERT INTO store_item_batch_details(item_batch_id,batch_no,medicine_id,mrp,exp_dt,username)
						VALUES(:item_batch_id, :batch_no, :medicine_id, :mrp, :exp_dt, :username)};


=comment
Item Lot Table Query
=cut

		my $item_lot_details_query = qq{INSERT INTO store_item_lot_details(item_lot_id,grn_no,item_batch_id,package_cp,lot_source,purchase_type)
							VALUES(:item_lot_id, :grn_no, :item_batch_id, :package_cp, :lot_source, :purchase_type)};


=comment
store_stock_details Table Query
=cut

		my $stock_details_query = qq{INSERT INTO store_stock_details(batch_no, qty, package_sp, package_cp, username,change_source, medicine_id,
						 consignment_stock,asset_approved, dept_id, item_supplier_name, item_supplier_code, item_invoice_no, item_grn_no,
						stock_pkg_size, item_ced_amt,last_cp_grn,max_cp_grn, package_uom, item_lot_id, item_batch_id)
					VALUES(:batch_no,:qty,:package_sp, :package_cp, :username,:change_source, :medicine_id,
						:consignment_stock, :asset_approved, :dept_id, :item_supplier_name, :item_supplier_code, :item_invoice_no, :item_grn_no,
						:stock_pkg_size, :item_ced_amt, :last_cp_grn, :max_cp_grn, :package_uom, :item_lot_id, :item_batch_id)};







##Store Invoice STH


		my $ora_invoice_sth = $o_dbh->prepare($ora_invoice_query);
		my $ora_invoice_query_update_sth = $o_dbh->prepare($ora_invoice_query_update);


##GERN Main STH

		my $ora_grn_main_sth = $o_dbh->prepare($ora_grn_main_query);
		my $ora_grn_main_count_sth = $o_dbh->prepare($ora_grn_main_query_count);


##GRN Details STH

		my $ora_grn_details_sth = $o_dbh->prepare($ora_grn_details);


##Item Batch Table

		my $is_item_batch_medicine_exists_sth = $p_dbh->prepare($is_item_batch_medicine_exists_query);
		my $item_batch_insert_query_sth = $p_dbh->prepare($item_batch_insert_query);
		my $check_duplicate_medicine_sth = $p_dbh->prepare($check_duplicate_medicine);

##Item Lot Table STH

		my $item_lot_insert_query_sth = $p_dbh->prepare( $item_lot_details_query);


## store_stock_details Table STH

		my $stock_details_sth = $p_dbh->prepare($stock_details_query);

		my $error = 0;


		try{
				print "\n".dateTime()."::INIT::SCM_GRN\n";

				$ora_invoice_sth->execute();

			while(my $invoice_row_data = $ora_invoice_sth->fetchrow_hashref ) {
				$error = 0;
				my %invoice_rows = %{$invoice_row_data};

				delete $invoice_rows{"CREATION_DATE"};
				delete $invoice_rows{"NEW_UPDATE_FLAG"};

				$error = insert(\"store_invoice",\%invoice_rows,\%store_invoice_map);

				if( $error gt 1 || $error le 0 ){
					print "\n".dateTime()."ERROR :: INSERT_ERROR :: Pg_Table = store_invoice :: SUPPLIER_INVOICE_ID = $invoice_rows{'SUPPLIER_INVOICE_ID'}\n" ;
					$invoice_ie_c = $invoice_ie_c + 1;
					next;
				}else {
					$invoice_i_c = $invoice_i_c + $error;
					$ora_invoice_query_update_sth->bind_param(1,$invoice_rows{'SUPPLIER_INVOICE_ID'});
					my $ora_update_c = $ora_invoice_query_update_sth->execute();
					$ora_selected_rows{"ORA_SIU"} => $ora_selected_rows{"ORA_SIU"} + $ora_update_c ;
				}

				try{

					$ora_grn_main_count_sth->bind_param(1, $invoice_row_data->{"SUPPLIER_INVOICE_ID"});
					$ora_grn_main_count_sth->execute();
					my $rows = $ora_grn_main_count_sth->fetchrow_array();

					if ($rows gt 1 ) {
						print "\nERROR :: DUPLICATE SUPLIER_INVOICE_ID In store_grn_main Table\n";
						$rollback_count = $rollback_count + 1;
						next;
					}

					$ora_grn_main_sth->bind_param(1, $invoice_row_data->{"SUPPLIER_INVOICE_ID"});
					$ora_grn_main_sth->execute();
					my $grn_main_row = $ora_grn_main_sth->fetchrow_hashref;
					my %grn_main_row_hash = %{$grn_main_row};
					delete $grn_main_row_hash{"NEW_UPDATE_FLAG"};

					$error = insert(\"store_grn_main",\%grn_main_row_hash,\%store_grn_main_map);


					if($error gt 1 || $error le 0) {
						print "\n".dateTime()."ERROR :: INSERT_ERROR :: Pg_Table = store_grn_main , SUPPLIER_INVOICE_ID = $invoice_row_data->{'SUPPLIER_INVOICE_ID'}, AND GRN_NO = $grn_main_row_hash{'GRN_NO'}\n";
						$grn_main_ie_c = $grn_main_ie_c + 1;
						next;

					}else{
						$grn_main_i_c = $grn_main_i_c + $error;
					}

					$ora_grn_details_sth->bind_param(1,$grn_main_row_hash{"GRN_NO"});
					$ora_grn_details_sth->execute();

					while(my $grn_details_rows = $ora_grn_details_sth->fetchrow_hashref){


						try{

							$is_item_batch_medicine_exists_sth->bind_param(":batch_no",$grn_details_rows->{"BATCH_NO"});
							$is_item_batch_medicine_exists_sth->bind_param(":medicine_id",$grn_details_rows->{"MEDICINE_ID"});
							$is_item_batch_medicine_exists_sth->execute();

							my $count_batch = 0;

							while(my $is_item_batch_medicine_hash_ref =$is_item_batch_medicine_exists_sth->fetchrow_hashref){
								my %item_batch_medicine_hash_ref = %{$is_item_batch_medicine_hash_ref};

								$count_batch = $count_batch + 1;
								$grn_details_rows->{"ITEM_BATCH_ID"} = $item_batch_medicine_hash_ref{"item_batch_id"};

							}

							if($count_batch eq 0) {
								my $item_batch_id_seq_sth = $p_dbh->prepare("SELECT nextval('store_item_batch_details_seq'::regclass)");
								$item_batch_id_seq_sth->execute();
								my @item_batch_id_seq_arr = $item_batch_id_seq_sth->fetchrow_array;
								 $item_batch_id_seq_sth->finish;
								$grn_details_rows->{"ITEM_BATCH_ID"} = $item_batch_id_seq_arr[0];

							}

							if($count_batch gt 1) {

								$rollback_count = $rollback_count + 1;
								print "ERROR :: INSERT_ERROR :: UPDATE_ERROR : Tow ITEM_BATCH_ID exists for
									BATCH_NO =$grn_details_rows->{'BATCH_NO'} , MEDICINE_ID = $grn_details_rows->{'MEDICINE_ID'}";
							}

							$grn_details_rows->{BILLED_QTY} = $grn_details_rows->{BILLED_QTY} * $grn_details_rows->{GRN_PKG_SIZE};

							$grn_details_rows->{TOTAL_QTY} = $grn_details_rows->{TOTAL_QTY}  * $grn_details_rows->{GRN_PKG_SIZE};

							my %grn_details_rows_has = %{$grn_details_rows};

							delete $grn_details_rows_has{"NEW_UPDATE_FLAG"};

											 
							if($count_batch eq 0){

								$item_batch_insert_query_sth->bind_param(":item_batch_id",$grn_details_rows_has{"ITEM_BATCH_ID"});
								$item_batch_insert_query_sth->bind_param(":batch_no",$grn_details_rows_has{"BATCH_NO"});
								$item_batch_insert_query_sth->bind_param(":medicine_id",$grn_details_rows_has{"MEDICINE_ID"});
								$item_batch_insert_query_sth->bind_param(":mrp",$grn_details_rows_has{"MRP"});
								$item_batch_insert_query_sth->bind_param(":exp_dt",$grn_details_rows_has{"EXP_DT"});
								$item_batch_insert_query_sth->bind_param(":username","NMC");
								my $item_b_c = $item_batch_insert_query_sth->execute();

							}

							$check_duplicate_medicine_sth->bind_param(':grn_no',$grn_main_row_hash{"GRN_NO"});
						$check_duplicate_medicine_sth->bind_param(':item_batch_id',$grn_details_rows->{"ITEM_BATCH_ID"});

							$check_duplicate_medicine_sth->execute();
							my @medicine_count = $check_duplicate_medicine_sth->fetchrow_array;
							$check_duplicate_medicine_sth->finish;							
							if($medicine_count[0] gt 0 ){
								print "\n\n".dateTime()."ERROR :: INSERT_ERROR :: Medicine all ready exists for GRN_NO =  $grn_main_row_hash{'GRN_NO'} , ITEM_BATCH_ID =  $grn_details_rows->{'ITEM_BATCH_ID'},  BATCH_NO =$grn_details_rows->{'BATCH_NO'} , MEDICINE_ID = $grn_details_rows->{'MEDICINE_ID'}  under store_grn_details\n";
								$rollback_count = $rollback_count + 1;
								 $grn_details_ie_c = $grn_details_ie_c + 1;
								next;
							}

							my $error = insert(\"store_grn_details",\%grn_details_rows_has,\%store_grn_details_map);

							if($error le 0){

								print "\n\n".dateTime()."ERROR :: INSERT_ERROR :: Pg_Table = store_grn_main , SUPPLIER_INVOICE_ID = $invoice_row_data->{'SUPPLIER_INVOICE_ID'}, AND GRN_NO = $grn_main_row_hash{'GRN_NO'}\n";
								$grn_details_ie_c = $grn_details_ie_c + 1;
								next;
							} else{
								$grn_details_i_c = $grn_details_i_c + $error;
							}

							if(($grn_details_rows_has{"BILLED_QTY"} le 0) and  ($grn_details_rows_has{"BONUS_QTY"} le 0) ) {

								print "ERROR :: INSERT_ERROR :: BILLED_QTY = $grn_details_rows_has{'BILLED_QTY'}  AND BONUS_QTY =  $grn_details_rows_has{'BONUS_QTY'} is incorrectly inserted for  GRN = $grn_main_row_hash{'GRN_NO'} and medicine_id :$grn_details_rows_has{'MEDICINE_ID'}";
								$rollback_count = $rollback_count + 1;
								next;

							}


							my $supplier_name_sth = $p_dbh->prepare("SELECT supplier_name from supplier_master where supplier_code = ?");
							    $supplier_name_sth->bind_param(1,$invoice_rows{'SUPPLIER_ID'});
							$supplier_name_sth->execute();
							my @supplier_name_arr = $supplier_name_sth->fetchrow_array;
							$supplier_name_sth->finish;

							if( $grn_details_rows_has{"BILLED_QTY"} > 0 ){

								my $lot_seq_sth = $p_dbh->prepare("SELECT nextval('store_item_lot_details_seq'::regclass)");
								$lot_seq_sth->execute();
								my @lot_seq_arr = $lot_seq_sth->fetchrow_array;
								$lot_seq_sth->finish;

													my $package_cp = (($grn_details_rows_has{"COST_PRICE"} * $grn_details_rows_has{"BILLED_QTY"} ) - $grn_details_rows_has{"DISCOUNT"} ) / $grn_details_rows_has{'BILLED_QTY'};


								$item_lot_insert_query_sth->bind_param(":lot_source","S");
								$item_lot_insert_query_sth->bind_param(":purchase_type","S");
								$item_lot_insert_query_sth->bind_param(":item_lot_id",$lot_seq_arr[0]);
								$item_lot_insert_query_sth->bind_param(":grn_no",$grn_main_row_hash{'GRN_NO'});
								$item_lot_insert_query_sth->bind_param(":item_batch_id",$grn_details_rows_has{"ITEM_BATCH_ID"});
								$item_lot_insert_query_sth->bind_param(":package_cp",$package_cp);

								my $lot_ic = $item_lot_insert_query_sth->execute();

								$stock_details_sth->bind_param(":batch_no",$grn_details_rows_has{"BATCH_NO"});
								$stock_details_sth->bind_param(":qty",$grn_details_rows_has{'BILLED_QTY'});
								$stock_details_sth->bind_param(":package_sp",0);
								$stock_details_sth->bind_param(":package_cp",$package_cp);
								$stock_details_sth->bind_param(":username", "NMC");
								$stock_details_sth->bind_param(":change_source", "Stock Entry");
								$stock_details_sth->bind_param(":medicine_id",$grn_details_rows_has{"MEDICINE_ID"});
								$stock_details_sth->bind_param(":consignment_stock",$grn_main_row_hash{"CONSIGNMENT_STOCK"});
								$stock_details_sth->bind_param(":asset_approved", 'Y');
								$stock_details_sth->bind_param(":dept_id",$grn_main_row_hash{"STORE_ID"});
								$stock_details_sth->bind_param(":item_supplier_name",$supplier_name_arr[0]);
								$stock_details_sth->bind_param(":item_supplier_code",$invoice_rows{"SUPPLIER_ID"});
								$stock_details_sth->bind_param(":item_invoice_no",$grn_main_row_hash{"SUPPLIER_INVOICE_ID"});
								$stock_details_sth->bind_param(":item_grn_no",$grn_main_row_hash{'GRN_NO'});
								$stock_details_sth->bind_param(":stock_pkg_size",$grn_details_rows_has{"GRN_PKG_SIZE"});
								$stock_details_sth->bind_param(":item_ced_amt",$grn_details_rows_has{"ITEM_CED"});
								$stock_details_sth->bind_param(":last_cp_grn",$grn_main_row_hash{'GRN_NO'});
								$stock_details_sth->bind_param(":max_cp_grn",$grn_main_row_hash{'GRN_NO'});
								$stock_details_sth->bind_param(":package_uom",$grn_details_rows_has{"GRN_PACKAGE_UOM"});
								$stock_details_sth->bind_param(":item_lot_id",$lot_seq_arr[0]);
								$stock_details_sth->bind_param(":item_batch_id",$grn_details_rows_has{"ITEM_BATCH_ID"});
		#						$stock_details_sth->bind_param(":store_stock_id",undef);
								my $result_stock_details = $stock_details_sth->execute();
		#						print "\n-----------\nstore_stock_details = $result_stock_details\n---------------\n";



							}
							if($grn_details_rows_has{"BONUS_QTY"} > 0) {


								my $lot_seq_sth = $p_dbh->prepare("SELECT nextval('store_item_lot_details_seq'::regclass)");
								$lot_seq_sth->execute();
								my @lot_seq_arr = $lot_seq_sth->fetchrow_array;

								$item_lot_insert_query_sth->bind_param(":lot_source","S");
								$item_lot_insert_query_sth->bind_param(":purchase_type","B");
								$item_lot_insert_query_sth->bind_param(":item_lot_id",$lot_seq_arr[0]);
								$item_lot_insert_query_sth->bind_param(":grn_no",$grn_main_row_hash{'GRN_NO'});
								$item_lot_insert_query_sth->bind_param(":item_batch_id",$grn_details_rows_has{"ITEM_BATCH_ID"});
								$item_lot_insert_query_sth->bind_param(":package_cp",0);

								my $lot_ic = $item_lot_insert_query_sth->execute();

								$stock_details_sth->bind_param(":batch_no",$grn_details_rows_has{"BATCH_NO"});
								$stock_details_sth->bind_param(":qty",$grn_details_rows_has{"BONUS_QTY"});
								$stock_details_sth->bind_param(":package_sp",0);
								$stock_details_sth->bind_param(":package_cp",0);
								$stock_details_sth->bind_param(":username", "NMC");
								$stock_details_sth->bind_param(":change_source", "Stock Entry");
								$stock_details_sth->bind_param(":medicine_id",$grn_details_rows_has{"MEDICINE_ID"});
								$stock_details_sth->bind_param(":consignment_stock",$grn_main_row_hash{"CONSIGNMENT_STOCK"});
								$stock_details_sth->bind_param(":asset_approved", 'Y');
								$stock_details_sth->bind_param(":dept_id",$grn_main_row_hash{"STORE_ID"});
								$stock_details_sth->bind_param(":item_supplier_name", $supplier_name_arr[0]);
								$stock_details_sth->bind_param(":item_supplier_code",$invoice_rows{"SUPPLIER_ID"});
								$stock_details_sth->bind_param(":item_invoice_no",$grn_main_row_hash{"SUPPLIER_INVOICE_ID"});
								$stock_details_sth->bind_param(":item_grn_no",$grn_main_row_hash{'GRN_NO'});
								$stock_details_sth->bind_param(":stock_pkg_size",$grn_details_rows_has{"GRN_PKG_SIZE"});
								$stock_details_sth->bind_param(":item_ced_amt",$grn_details_rows_has{"ITEM_CED"});
								$stock_details_sth->bind_param(":last_cp_grn",$grn_main_row_hash{'GRN_NO'});
								$stock_details_sth->bind_param(":max_cp_grn",$grn_main_row_hash{'GRN_NO'});
								$stock_details_sth->bind_param(":package_uom",$grn_details_rows_has{"GRN_PACKAGE_UOM"});
								$stock_details_sth->bind_param(":item_lot_id",$lot_seq_arr[0]);
								$stock_details_sth->bind_param(":item_batch_id",$grn_details_rows_has{"ITEM_BATCH_ID"});
		#						$stock_details_sth->bind_param(":store_stock_id",undef);
								my $result_stock_details = $stock_details_sth->execute();
		#						print "\n-----------\nstore_stock_details = $result_stock_details\n---------------\n";

							}


						}catch{

								print "\nERROR : INSERT_ERROR :  $_";
								$rollback_count = $rollback_count + 1;
								$p_dbh->rollback();
								$p_dbh->do("set search_path to $p_schema") or $DBI::errstr;

						};


					}
				}catch{
					print "\nERROR : INSERT_ERROR :  $_";
					$rollback_count = $rollback_count + 1;

				}finally{
					$ora_selected_rows{'SGM'}  = $ora_selected_rows{'SGM'} +  scalar $ora_grn_main_sth->rows() ;
					$ora_selected_rows{'SGD'}  = $ora_selected_rows{'SGD'} +  scalar  $ora_grn_details_sth->rows();
				};

			}

			$rows_i = $invoice_i_c + $grn_main_i_c + $grn_details_i_c;
			$rows_if = $invoice_ie_c + $grn_main_ie_c + $grn_details_ie_c;

		}catch{

			print "\n ERROR :: OR ::  INSERT_ERROR SQL_SELECT_ERROR : $_";
			$rollback_count = $rollback_count + 1;
		}finally{

			my $status = 0;

			$ora_selected_rows{'SI'}  =  $ora_selected_rows{'SI'} + scalar $ora_invoice_sth->rows();
			$rows_found = $ora_selected_rows{'SI'} + $ora_selected_rows{'SGM'} + $ora_selected_rows{'SGD'};

			my $table_wise_txn = "\n".dateTime()."Each table wise transaction details :".
			    "\n store_invoice  	: SELECTED = $ora_selected_rows{'SI'} : INSERTED = $invoice_i_c ,  INSERT_FAIL = $invoice_ie_c".
			   "\n store_grn_main  	: SELECTED = $ora_selected_rows{'SGM'}: INSERTED = $grn_main_i_c ,  INSERT_FAIL = $grn_main_ie_c".
			"\n store_grn_details 	: SELECTED = $ora_selected_rows{'SGD'}: INSERTED = $grn_details_i_c,  INSERT_FAIL = $grn_details_ie_c";


			my $all_txn = "\n..........................................................................................................".
				"\n".dateTime()." All Transaction Details : \n".
				"		Total Oracle Rows selected : $rows_found\n".
				"		No of rows inserted : $rows_i\n".
				"		Insert fails : $rows_if\n".
				"		Total Oracle Invoice Table Updated : $ora_selected_rows{'ORA_SIU'}".
				"\n\n___________________________________________________________________________________________________________\n\n";

			my $commit_detail_print;
			if($rollback_count gt 0) {
				$p_dbh->rollback or $DBI::strerr; $o_dbh->rollback or $DBI::strerr;

			}else {
				$p_dbh->commit or $DBI::strerr;
				$o_dbh->commit;	$status = 1;
				$commit_detail_print = dateTime()."Note : All Transaction commited successfully\n\n"

			}

			if($status eq 0) {

				$o_dbh->rollback or $DBI::strerr;
				$commit_detail_print = dateTime()."Note : All above transaction is rolling back due to previous error !\n\n";
#				SCM::Email::sendMail("SCM GRN Sync details",  $table_wise_txn . $all_txn . $commit_detail_print);
			}

			print $table_wise_txn . $all_txn . $commit_detail_print;


			try{

				$ora_invoice_sth->finish;
				$ora_invoice_query_update_sth->finish;
				$ora_grn_main_sth->finish;
				$ora_grn_main_count_sth->finish;
				$ora_grn_details_sth->finish;
				$item_batch_insert_query_sth->finish;
				$item_lot_insert_query_sth->finish;
				$stock_details_sth->finish;
				$is_item_batch_medicine_exists_sth->finish
			}catch{
				print "ERROR :: Statement close Error : $_";
			};
			$p_dbh->disconnect()  or warn $p_dbh->errstr;
			$o_dbh->disconnect()  or warn $o_dbh->errstr;
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
