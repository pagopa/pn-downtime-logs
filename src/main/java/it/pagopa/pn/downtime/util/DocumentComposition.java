package it.pagopa.pn.downtime.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DocumentComposition {

	public enum TemplateType {
		LEGAL_FACT("documents_composition_templates/PdfLegalFact.html");

		private final String htmlTemplate;

		TemplateType(String htmlTemplate) {
			this.htmlTemplate = htmlTemplate;
		}

		public String getHtmlTemplate() {
			return htmlTemplate;
		}
	}

	private final Map<TemplateType, String> baseUris;
	private final Configuration freemarker;

	public DocumentComposition(Configuration freemarker) throws IOException {
		this.freemarker = freemarker;

		log.info("Preload templates START");
		baseUris = new EnumMap<>(TemplateType.class);
		StringTemplateLoader stringLoader = new StringTemplateLoader();

		for (TemplateType templateType : TemplateType.values()) {
			log.info(" - begin to preload template with templateType={}", templateType);
			BaseUriAndTemplateBody info = preloadTemplate(templateType);

			this.baseUris.put(templateType, info.getBaseUri());
			stringLoader.putTemplate(templateType.name(), info.templateBody);
		}
		log.debug("Configure freemarker ... ");
		this.freemarker.setTemplateLoader(stringLoader);
		log.debug(" ... freemarker configured.");
		log.info("Preload templates END");
	}

	@Value
	private static class BaseUriAndTemplateBody {
		private String baseUri;
		private String templateBody;
	}

	private static BaseUriAndTemplateBody preloadTemplate(TemplateType templateType) throws IOException {
		log.debug("Start pre-loading template with templateType={}", templateType);

		String templateResourceName = templateType.getHtmlTemplate();
		URL templateUrl = getClasspathResourceURL(templateResourceName);
		log.debug("Template with templateResourceName={} located at URL={}", templateResourceName, templateUrl);

		String baseUri = templateUrl.toString().replaceFirst("/[^/]*$", "/");
		String templateBody = loadTemplateBody(templateUrl);

		log.debug("Template resources baseUri={}", baseUri);
		return new BaseUriAndTemplateBody(baseUri, templateBody);
	}

	private static String loadTemplateBody(URL templateUrl) throws IOException {

		String templateContent;
		try (InputStream templateIn = templateUrl.openStream()) {
			templateContent = StreamUtils.copyToString(templateIn, StandardCharsets.UTF_8);
		} catch (IOException exc) {
			log.error("Loading Document Composition Template " + templateUrl, exc);
			throw exc;
		}
		return templateContent;
	}

	private static URL getClasspathResourceURL(String resourceName) {
		return Thread.currentThread().getContextClassLoader().getResource(resourceName);
	}

	public String executeTextTemplate(TemplateType templateType, Object model) throws 
			  IOException, TemplateException {
		log.info("Execute templateType={} START", templateType);
		StringWriter stringWriter = new StringWriter();

		Template template = freemarker.getTemplate(templateType.name());
		template.process(model, stringWriter);

		log.info("Execute templateType={} END", templateType);
		return stringWriter.getBuffer().toString();
	}

	public byte[] executePdfTemplate(TemplateType templateType, Object model) throws IOException, TemplateException {
		String html = executeTextTemplate(templateType, model);

		String baseUri = baseUris.get(templateType);
		log.info("Pdf conversion start for templateType={} with baseUri={}", templateType, baseUri);

		byte[] pdf = html2Pdf(baseUri, html);

		log.info("Pdf conversion done");
		return pdf;
	}

	public byte[] html2Pdf(String baseUri, String html) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		PdfRendererBuilder builder = new PdfRendererBuilder();
		builder.usePdfUaAccessbility(true);
		builder.usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_3_A);
		builder.withHtmlContent(html, baseUri);
		builder.toStream(baos);
		builder.run();
		baos.close();

		return baos.toByteArray();
	}

}
