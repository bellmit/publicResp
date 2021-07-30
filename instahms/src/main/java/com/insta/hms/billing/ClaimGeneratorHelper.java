/**
 *
 */
package com.insta.hms.billing;

import com.insta.hms.common.AppInit;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author lakshmi.p
 *
 */
public class ClaimGeneratorHelper {

	static Logger logger = LoggerFactory.getLogger(ClaimGeneratorHelper.class);

	public void addClaimHeader(OutputStream stream, Map ftlMap, String healthAuthority )
			throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("/Eclaim/"+healthAuthority.toLowerCase()+"/EclaimHeader.ftl");
		writeToStream(t, stream, ftlMap);
	}
	public void addClaimHeader(OutputStream stream, Map ftlMap)
			throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("/Eclaim/EclaimHeader.ftl");
		writeToStream(t, stream, ftlMap);
	}

	public void addClaimBody(OutputStream stream, Map ftlMap, String template)
			throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate(template);
		writeToStream(t, stream, ftlMap);
	}
	
	public void addClaimBody(OutputStream stream, Map ftlMap)
			throws IOException, TemplateException {
		addClaimBody(stream, ftlMap, "/Eclaim/EclaimBody.ftl");
	}

	public void addClaimFooter(OutputStream stream, Map ftlMap, String healthAuthority)
			throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("/Eclaim/"+healthAuthority.toLowerCase()+"/EclaimFooter.ftl");
		writeToStream(t, stream, ftlMap);
	}
	public void addClaimFooter(OutputStream stream, Map ftlMap)
			throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("/Eclaim/EclaimFooter.ftl");
		writeToStream(t, stream, ftlMap);
	}
	public void writeToStream(Template t, OutputStream stream, Map ftlMap)
			throws IOException, TemplateException{
		if (t == null)
			return;
		StringWriter sWriter = new StringWriter();
		t.process(ftlMap, sWriter);
		stream.write(sWriter.toString().getBytes());
		stream.flush();
	}

	public void addAccumedClaimHeader(OutputStream stream, Map ftlMap)
		throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("/Accumed/AccumedEclaimHeader.ftl");
		writeToStream(t, stream, ftlMap);
	}

	public void addAccumedClaimBody(OutputStream stream, Map ftlMap)
		throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("/Accumed/AccumedEclaimBody.ftl");
		writeToStream(t, stream, ftlMap);
	}

	public void addAccumedClaimFooter(OutputStream stream, Map ftlMap)
		throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("/Accumed/AccumedEclaimFooter.ftl");
		writeToStream(t, stream, ftlMap);
	}
}
