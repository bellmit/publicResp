package com.insta.hms.common;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * The Class RtlProcessor.
 */
public class RtlProcessor {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(RtlProcessor.class);

  /** The renderer. */
  private ITextRenderer renderer = null;

  /** The process table columns. */
  private boolean processTableColumns = false;

  /** The process position styles. */
  private boolean processPositionStyles = false;

  /** The load unicode fonts. */
  private boolean loadUnicodeFonts = false;

  /** The process rendering. */
  private boolean processRendering = false;

  /**
   * Instantiates a new rtl processor.
   *
   * @param renderer      the renderer
   * @param processText   the process text
   * @param processLayout the process layout
   */
  public RtlProcessor(ITextRenderer renderer, boolean processText, boolean processLayout) {
    this.renderer = renderer;
    processTableColumns = processLayout;
    processPositionStyles = processLayout;
    loadUnicodeFonts = processText;
    processRendering = processText;
  }

  /**
   * Instantiates a new rtl processor.
   *
   * @param renderer the renderer
   */
  public RtlProcessor(ITextRenderer renderer) {
    this(renderer, false, false);
  }

  /**
   * Inits the.
   */
  public void init() {
    setupRtlReplacedElement();
    setupPositionalStyleHandler();
  }

  /**
   * Setup positional style handler.
   */
  private void setupPositionalStyleHandler() {
    if (processPositionStyles) {
      renderer.getSharedContext().setNamespaceHandler(new RtlNamespaceHandler());
    }
  }

  /**
   * Setup rtl replaced element.
   */
  private void setupRtlReplacedElement() {
    if (processRendering) {
      renderer.getSharedContext()
          .setReplacedElementFactory(new RtlTextReplacedElementFactory(renderer.getOutputDevice()));
    }
  }

  /**
   * Pre process.
   *
   * @param doc the doc
   * @return the document
   */
  public Document preProcess(Document doc) {
    Document processedDoc = null;
    try {
      processedDoc = processTable(doc);
    } catch (XPathExpressionException xe) {
      log.error("Unable to pre-process HTML tables for RTL " + xe.getMessage());
    }
    return processedDoc;
  }

  /**
   * Process table.
   *
   * @param doc the doc
   * @return the document
   * @throws XPathExpressionException the x path expression exception
   */
  private Document processTable(Document doc) throws XPathExpressionException {

    if (!processTableColumns) {
      return doc;
    }

    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    xpath.setNamespaceContext(new XHtmlNameSpace());

    XPathExpression exprTable = xpath.compile("//xml:table");
    XPathExpression exprRow = xpath.compile("./xml:tbody/xml:tr | ./xml:tr");
    XPathExpression exprCol = xpath.compile("./xml:td");

    Object result = exprTable.evaluate(doc, XPathConstants.NODESET);
    NodeList tables = (NodeList) result;

    int numTables = (null != tables) ? tables.getLength() : 0;
    for (int tableIndex = 0; tableIndex < numTables; tableIndex++) {
      Node tableNode = tables.item(tableIndex);

      result = exprRow.evaluate(tableNode, XPathConstants.NODESET);
      NodeList rows = (NodeList) result;
      int numRows = ((null != rows) ? rows.getLength() : 0);

      log.debug("No. of table (" + tableIndex + ") rows : " + numRows);
      for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
        Node rowNode = rows.item(rowIndex);
        result = exprCol.evaluate(rowNode, XPathConstants.NODESET);
        NodeList columns = (NodeList) result;
        int numCols = (null != columns) ? columns.getLength() : 0;

        log.debug("No. of row (" + rowIndex + ") colums : " + numCols);
        for (int colIndex = 0; colIndex < numCols / 2; colIndex++) {
          int index1 = colIndex;
          int index2 = numCols - colIndex - 1;

          log.debug("Swapping node : " + index1 + ", " + index2);
          if (index2 > index1) {
            // A nodelist is "live" which means the nodelist will
            // change when the underlying document is changed.
            // Since we are only changing the node position and
            // we are not going to traverse those nodes again
            // anyway, we should be fine this way.
            Node node1 = columns.item(index1);
            Node node2 = columns.item(index2);
            Node nodeRef = node2.getNextSibling();
            node1 = node1.getParentNode().replaceChild(node2, node1);
            nodeRef.getParentNode().insertBefore(node1, nodeRef);

          }
        }
      }
    }
    return doc;
  }
  /*
   * private void debugWrite(String prefix, String content) { try { SimpleDateFormat fmt = new
   * SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"); String fileName =
   * "/home/anupama/InstaHealth/instamain/" + prefix + fmt.format(new Date()); File outFile = new
   * File(fileName); outFile.createNewFile(); FileWriter w = new FileWriter(outFile); if (null !=
   * content) { w.write(content); } w.close(); } catch (IOException ioe) { log.error(
   * "debugwrite failed :" + ioe.getMessage()); } }
   */

  // This is copied over from HtmlConverter to remove any dependency on
  // HtmlConverter class. Ideally this should move into an XMLHelper class
  // which will be used both by HtmlConverter and RtlProcessor

  /**
   * The Class XHtmlNameSpace.
   */
  static class XHtmlNameSpace implements NamespaceContext {

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix) {
      log.debug("http://www.w3.org/1999/xhtml");
      if (prefix.equals("xml")) {
        return "http://www.w3.org/1999/xhtml";
      } else {
        return XMLConstants.NULL_NS_URI;
      }

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    public String getPrefix(String uri) {
      throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    public Iterator getPrefixes(String uri) {
      throw new UnsupportedOperationException();
    }

  }
}
