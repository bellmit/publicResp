/**
 *
 */
package com.insta.hms.master.ICDUpload;

import au.com.bytecode.opencsv.CSVReader;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.FlashScope;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author lakshmi.p
 *
 */
public class ICDUploadAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(ICDUploadAction.class);
	private StringBuilder errors = null;
	private StringBuilder message = null;
	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		return mapping.findForward("addshow");
	}

	public ActionForward upload(ActionMapping mapping, ActionForm form, HttpServletRequest request,
		HttpServletResponse response) throws IOException, ServletException, Exception{
		FlashScope flash = FlashScope.getScope(request);
		Connection con = null;
		PreparedStatement ps = null;

		ICDUploadForm uform = (ICDUploadForm)form;

		String ICD_INSERT = "INSERT INTO mrd_codes_master (mrd_code_id, code_type, code, code_desc, status) values (nextval('mrd_codes_master_seq'),?, ?, ?, ?)";

		String code_type = uform.getIcd_type();
		String icd_code = null;
		String icd_description = null;
		int lineCount = 0;

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		try {
			FormFile file = uform.getIcd_upload_file_content();
			InputStream is = file.getInputStream();
			Reader reader = new InputStreamReader(is);

			CSVReader csvReader = new CSVReader(reader);
			String[] line = null;
			int numErrors = 0;
			int insertCount = 0;
			this.errors = new StringBuilder();
			this.message =  new StringBuilder();
			while ( (line = csvReader.readNext()) != null ) {

				try {
					con = DataBaseUtil.getConnection();
					ps = con.prepareStatement(ICD_INSERT);
					con.setAutoCommit(false);
					lineCount++;
					icd_code = line[0];
					icd_description = line[1];
					if (icd_description != null && icd_description != "") {
						String trimedDescription = icd_description.trim();
						if (trimedDescription.endsWith("<") || trimedDescription.endsWith(">")) {
							addError(lineCount,"the description ends with < or > whick is not alllowed : "+lineCount);
							continue;
						}
					}
					ps.setString(1, code_type);
					ps.setString(2, icd_code);
					ps.setString(3, icd_description);
					ps.setString(4, "A");
					ps.executeUpdate();
					insertCount++;
					con.commit();
					con.close();

				} catch (ArrayIndexOutOfBoundsException e) {
					numErrors++;
					addError(lineCount, "Upload Failure... File has line where code and/or description is not available ...at Line number... " + lineCount);


			   } catch (SQLException e) {
				   	numErrors++;
					if (DataBaseUtil.isDuplicateViolation(e)) {
						addError(lineCount, "Upload Failure duplicate ICD Codes...at Line No:" + lineCount);
					}
					else throw (e);
			   }
			}
			message.append("Processed lines: ").append(lineCount).append("<br>");
			message.append("Inserted Codes: ").append(insertCount).append("<br>");

			if (numErrors > 0) {
				message.append("Lines with errors: ").append(numErrors).append("<br>") ;
				message.append("<hr>");
			}
			message.append(this.errors);
			flash.put("info", this.message.toString());

	   } finally {

	   }
	   return redirect;
	}

	private void addError(int line, String msg) {
		if (line > 0) {
			this.errors.append("Line ").append(line).append(": ");
		} else {
			this.errors.append("Error in header: ");
		}
		this.errors.append(msg).append("<br>");
	}
}
