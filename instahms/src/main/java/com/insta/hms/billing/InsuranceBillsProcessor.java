package com.insta.hms.billing;

import java.io.IOException;
import java.sql.SQLException;

public interface InsuranceBillsProcessor {
	public boolean process(String submission_batch_id)throws SQLException ,IOException ;

}
