package com.insta.hms.common;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextReplacedElementFactory;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RtlTextReplacedElementFactory implements ReplacedElementFactory {

  private static final Pattern rtlStringPattern = Pattern.compile("\\p{InArabic}");
  private static final Pattern inlineElements = Pattern
      .compile("a|b|code|em|font|i|img|label|s|small|span|strong|u");
  private String cssClassName;

  private ITextReplacedElementFactory defaultFactory;
  private float dotsPerPoint = 0.0f;
  static Logger log = Logger.getLogger(RtlTextReplacedElementFactory.class);

  public RtlTextReplacedElementFactory(ITextOutputDevice outputDevice) {
    this(outputDevice, null);
  }

  /**
   * Constructor.
   * @param outputDevice output device
   * @param cssClassName css class name
   */
  public RtlTextReplacedElementFactory(ITextOutputDevice outputDevice, String cssClassName) {
    defaultFactory = new ITextReplacedElementFactory(outputDevice);
    this.dotsPerPoint = outputDevice.getDotsPerPoint();
    this.cssClassName = cssClassName;
  }

  @Override
  public ReplacedElement createReplacedElement(LayoutContext context, BlockBox box, 
      UserAgentCallback uac,
      int cssWidth, int cssHeight) {

    Element element = box.getElement();
    if (element == null) {
      return null;
    }

    boolean replace = hasRTLClass(element) || hasRTLContent(element);

    if (replace) {
      String text = element.getTextContent().replaceAll("(?m)\\s+", " ");
      log.info("Found node with rtl content :"
          + text);
      return new RtlTextReplacedElement(context, box, uac, cssWidth, cssHeight, text, dotsPerPoint);
    }

    return defaultFactory.createReplacedElement(context, box, uac, cssWidth, cssHeight);
  }

  @Override
  public void reset() {
    // we need to clear any cache. since we do not cache anything, nothing to do.
  }

  @Override
  public void remove(Element el) {
    // we need to remove all references to the element being removed
    // Since we dont do any caching, we dont maintain any references either, so nothing to do
    // Notes : The default implementation does some caching for radio buttons
    // (not sure why though)
  }

  @Override
  public void setFormSubmissionListener(FormSubmissionListener listener) {
    // Nothing to do. Forms are handled by the PDF reader.
  }

  // Method to check if the Element being checked has any text content and does not contain
  // any child element nodes.

  private boolean hasRTLContent(Element element) {
    NodeList nl = element.getChildNodes();
    boolean hasChildElement = false;
    boolean rtlContent = false;
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      short nodeType = node.getNodeType();
      if (nodeType == Node.ELEMENT_NODE) {
        String nodeName = node.getNodeName();
        if (null != nodeName && !inlineElements.matcher(nodeName).matches()) {
          hasChildElement = true;
          break;
        }
      }
    }
    if (!hasChildElement) { // && hasTextContent ) {
      String content = element.getTextContent();
      if (null != content && !content.trim().isEmpty()) {
        Matcher matcher = rtlStringPattern.matcher(content);
        rtlContent = matcher.find();
      }
    }
    return rtlContent;
  }

  // Method to check if the element has a css class that indicates that the content is RTL

  private boolean hasRTLClass(Element element) {
    boolean rtlClass = false;
    if (null != cssClassName) {
      String classAttrib = element.getAttribute("class");
      // TODO : requires better regular expression based match to rule out
      // matching substrings
      if (null != classAttrib && classAttrib.contains(cssClassName)) {
        rtlClass = true;
      }
    }
    return rtlClass;  
  }
}