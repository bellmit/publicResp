package com.insta.hms.Registration;

import com.insta.hms.common.AppInit;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;

public class PatDataExportUtility {
	public static void addClaimHeader(OutputStream stream, Map ftlMap)
	throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("ExportRegDataHeader.ftl");
		if (t == null)
			return;
		StringWriter sWriter = new StringWriter();
		t.process(ftlMap, sWriter);
		stream.write(sWriter.toString().getBytes());
		stream.flush();
	}
	public static void addClaimBody(OutputStream stream, Map ftlMap)
	throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("ExportRegDataBody.ftl");
		if (t == null)
			return;
		StringWriter sWriter = new StringWriter();
		t.process(ftlMap, sWriter);
		stream.write(sWriter.toString().getBytes());
		stream.flush();
	}
	public static void addClaimFooter(OutputStream stream, Map ftlMap)
	throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("ExportRegDataFooter.ftl");
		if (t == null)
			return;
		StringWriter sWriter = new StringWriter();
		t.process(ftlMap, sWriter);
		stream.write(sWriter.toString().getBytes());
		stream.flush();
	}
}
