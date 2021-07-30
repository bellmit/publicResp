package com.insta.hms.master.DietaryMaster;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.lowagie.text.DocumentException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

public class PrescribedMealFtlHealper {



	private Configuration cfg = null;
	public PrescribedMealFtlHealper() {
		cfg = AppInit.getFmConfig();
	}

	public enum return_type {PDF, PDF_BYTES, TEXT_BYTES};

	public PrescribedMealFtlHealper(Configuration cfg) {
		this.cfg = cfg;
	}


	public byte[] getPrescriptionFtlReport(String visitId, return_type enumType, BasicDynaBean prefs,
			OutputStream os) throws SQLException, DocumentException,
			TemplateException, IOException, XPathExpressionException, TransformerException{

		byte[] bytes = null;
		 PatientDietPrescriptionsDAO pDao = new PatientDietPrescriptionsDAO();

		Map pDetails = new HashMap();
		GenericDocumentsFields.copyPatientDetails(pDetails, null, visitId,false);
		Map ftlParamMap = new HashMap();
		ftlParamMap.put("visitdetails", pDetails);

		List presMeals = pDao.getPrescribedMealsForPatient(visitId);

		ftlParamMap.put("modules_activated",
				((Preferences) RequestContext.getSession().getAttribute("preferences")).getModulesActivatedMap());
		ftlParamMap.put("presMeals", presMeals);

		Template t = cfg.getTemplate("PrescribedMeal.ftl");
		StringWriter writer = new StringWriter();
		t.process(ftlParamMap, writer);
		HtmlConverter hc = new HtmlConverter();
		if (enumType.equals(return_type.PDF)) {

			hc.writePdf(os, writer.toString(), "Prescribed Meal", prefs, false, false, true, true, true, false);
			os.close();

		} else if (enumType.equals(return_type.PDF_BYTES)) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			hc.writePdf(stream, writer.toString(), "Prescribed Meal", prefs, false, false, true, true, true, false);
			bytes = stream.toByteArray();
			stream.close();

		} else if (enumType.equals(return_type.TEXT_BYTES)) {
			bytes = hc.getText(writer.toString(), "Prescribed Meal",
					prefs, true, true);

		} else {

		}
		return bytes;
	}

}
