package com.insta.hms.common;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextFSFont;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextReplacedElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;

import java.awt.Color;
import java.awt.Point;

class RtlTextReplacedElement implements ITextReplacedElement {

  static Logger log = LoggerFactory.getLogger(RtlTextReplacedElement.class);

  private int width;
  private int height;
  private String text;

  private int align;
  private Color color;
  private float fontSize = -1;
  private int direction = PdfWriter.RUN_DIRECTION_LTR;

  private Point location = new Point();
  // lower left x, y and upper right x, y for painting the text
  private int llx;
  private int lly;
  private int urx;
  private int ury;

  private float defaultDpp = 0.0f; // device dots per point, passed in by the factory class

  RtlTextReplacedElement(LayoutContext context, BlockBox box, UserAgentCallback uac, int cssWidth,
      int cssHeight, String text, float dotsPerPoint) {

    this.text = text;
    this.defaultDpp = dotsPerPoint;
    initDimensions(context, box, cssWidth, cssHeight);

    fontSize = box.getStyle().getFSFont(context).getSize2D();

    align = com.lowagie.text.Element.ALIGN_LEFT;
    Element element = box.getElement();
    String as = element.getAttribute("align");
    {
      if (as.equalsIgnoreCase("left")) {
        align = com.lowagie.text.Element.ALIGN_LEFT;
      } else if (as.equalsIgnoreCase("center")) {
        align = com.lowagie.text.Element.ALIGN_CENTER;
      } else if (as.equalsIgnoreCase("right")) {
        align = com.lowagie.text.Element.ALIGN_RIGHT;
      }
    }
    as = element.getAttribute("dir");
    {
      if (as.equalsIgnoreCase("ltr")) {
        direction = PdfWriter.RUN_DIRECTION_LTR;
      } else if (as.equals("default")) {
        direction = PdfWriter.RUN_DIRECTION_DEFAULT;
      } else if (as.equals("no-bidi")) {
        direction = PdfWriter.RUN_DIRECTION_NO_BIDI;
      } else if (as.equals("rtl")) {
        direction = PdfWriter.RUN_DIRECTION_RTL;
      }
    }

    String elemColor = element.getAttribute("color");
    if (!elemColor.isEmpty()) {
      color = java.awt.Color.decode(elemColor);
    }

    String elemfontSize = element.getAttribute("font-size");
    if (!elemfontSize.isEmpty()) {
      this.fontSize = Float.parseFloat(elemfontSize);
    }
  }

  @Override
  public int getIntrinsicWidth() {
    return width;
  }

  @Override
  public int getIntrinsicHeight() {
    return height;
  }

  @Override
  public Point getLocation() {
    return location;
  }

  @Override
  public void setLocation(int xval, int yval) {
    location.x = xval;
    location.y = yval;
  }

  @Override
  public void detach(LayoutContext context) {
  }

  @Override
  public boolean isRequiresInteractivePaint() {
    return false;
  }

  @Override
  public boolean hasBaseline() {
    return false;
  }

  @Override
  public int getBaseline() {
    return 0;
  }

  @Override
  public void paint(RenderingContext context, ITextOutputDevice outputDevice, BlockBox box) {
    try {
      PdfWriter writer = outputDevice.getWriter();
      PdfContentByte cb = writer.getDirectContent();

      ITextFSFont font = (ITextFSFont) RtlFontHelper.getApplicableFont(box.getStyle(), context,
          outputDevice.getDotsPerPoint());
      float pdfFontSize = outputDevice.getDeviceLength(font.getSize2D());

      FSColor color = box.getStyle().getColor();
      Color clr = null;
      if (this.color != null) {
        clr = this.color;
      } else if (color instanceof FSRGBColor) {
        FSRGBColor cc = (FSRGBColor) color;
        clr = new Color(cc.getRed(), cc.getGreen(), cc.getBlue());
      }

      ColumnText ct = new ColumnText(cb);
      setupColumnCoordinates(context, outputDevice, box);
      ct.setSimpleColumn(llx, lly, urx, ury);
      ct.setSpaceCharRatio(PdfWriter.NO_SPACE_CHAR_RATIO);
      ct.setLeading(0, 1);
      ct.setRunDirection(direction);
      ct.setAlignment(align);

      if (clr == null) {
        ct.addText(new Phrase(text, new Font(font.getFontDescription().getFont(), pdfFontSize)));
      } else {
        ct.addText(
            new Phrase(text, new Font(font.getFontDescription().getFont(), pdfFontSize, 0, clr)));
      }
      ct.go();

    } catch (DocumentException exception) {
      log.error("error while processing rtl text : " + exception.getMessage());
      exception.printStackTrace();
    }
  }

  private void setupColumnCoordinates(RenderingContext context, ITextOutputDevice outputDevice,
      BlockBox box) {

    // box is the rectangular bounds corresponding to the RTL element that
    // we are painting.

    // get the page bounds - page is the printable area of the document
    PageBox page = context.getPage();

    float dotsPerPoint = outputDevice.getDotsPerPoint();
    float marginBorderPaddingLeft = page.getMarginBorderPadding(context, CalculatedStyle.LEFT);
    float marginBorderPaddingBottom = page.getMarginBorderPadding(context, CalculatedStyle.BOTTOM);

    RectPropertySet margin = box.getMargin(context);
    RectPropertySet padding = box.getPadding(context);

    // from box top to page bottom
    float dist = (page.getBottom() - box.getAbsY() + marginBorderPaddingBottom);

    llx = (int) ((margin.left() + padding.left() + box.getAbsX() + marginBorderPaddingLeft)
        / dotsPerPoint);
    lly = (int) ((dist - box.getHeight()) / dotsPerPoint);

    urx = (int) ((box.getAbsX() + box.getWidth() + marginBorderPaddingLeft) / dotsPerPoint);
    // TODO : I think this should be margin.top() padding.top() not bottom??
    ury = (int) ((dist + margin.bottom() + padding.bottom()) / dotsPerPoint);
    log.debug("(llx, lly, urx, ury) : " + llx + "," + lly + ", " + urx + "," + ury);
  }

  protected void initDimensions(LayoutContext context, BlockBox box, int cssWidth, int cssHeight) {

    CalculatedStyle style = box.getStyle();

    Element element = box.getElement();
    float scalex = 1.0f;
    float scaley = 1.0f;

    int lines = 1;
    {
      String lines1 = element.getAttribute("lines");
      if (!lines1.isEmpty()) {
        lines = Integer.parseInt(lines1);
      }
    }

    String sx = element.getAttribute("scale-x");
    if (!sx.isEmpty()) {
      try {
        scalex = Float.parseFloat(sx);
      } catch (Exception exception) {
        log.error("Bad scale-x attribute value: " + sx);
        // do nothing
      }
    }

    String sy = element.getAttribute("scale-y");
    if (!sy.isEmpty()) {
      try {
        scaley = Float.parseFloat(sy);
      } catch (Exception exception) {
        log.error("Bad scale-y attribute value: " + sy);
        // do nothing
      }
    }

    width = (int) (getEffectiveWidth(context, box, cssWidth, cssHeight) * scalex);
    height = (int) (getEffectiveHeight(context, box, cssWidth, cssHeight) * scaley);

  }

  private int getEffectiveHeight(LayoutContext context, BlockBox box, int cssWidth, int cssHeight) {

    int calculatedHeight = -1;
    int lines = 1;

    Element element = box.getElement();
    CalculatedStyle style = box.getStyle();

    String elemHeight = element.getAttribute("height");
    String elemLines = element.getAttribute("lines");

    if (null != elemLines && !elemLines.trim().isEmpty()) {
      try {
        lines = Integer.parseInt(elemLines.trim());
      } catch (NumberFormatException nfe) {
        log.error("Invalid value for lines attribute " + elemLines + nfe.getMessage());
      }
    }

    if (null != elemHeight && !elemHeight.isEmpty()) {
      elemHeight = elemHeight.trim();
      if (elemHeight.endsWith("%")) {
        // process percentage values
        int relHeight = Integer.parseInt(elemHeight.replace("%", "").trim());
        int parentHeight = box.getContainingBlock().getHeight();
        calculatedHeight = (int) (relHeight * parentHeight / 100.0f);
      } else if (elemHeight.endsWith("px")) {
        try {
          calculatedHeight = context.getDotsPerPixel()
              * Integer.parseInt(elemHeight.replace("px", "").trim());
        } catch (NumberFormatException nfe) {
          log.error("Invalid / Unsupported width specification " + elemHeight + nfe.getMessage());
        }
      } else {
        try {
          calculatedHeight = context.getDotsPerPixel() * Integer.parseInt(elemHeight);
        } catch (NumberFormatException nfe) {
          log.error("Invalid / Unsupported width specification " + elemHeight + nfe.getMessage());
        }
      }
      // Height was specified as a css style
    } else if (cssHeight != -1) {
      calculatedHeight = cssHeight;
    } else { // Height was not specified explicitly, we calculate it based on line height
      calculatedHeight = ((int) (style.getLineHeight(context) * lines));
    }
    return calculatedHeight;
  }

  private int getEffectiveWidth(LayoutContext context, BlockBox box, int cssWidth, int cssHeight) {

    int calculatedWidth = -1;

    Element element = box.getElement();
    CalculatedStyle style = box.getStyle();

    String elemWidth = element.getAttribute("width");

    // We have a width attribute on the element. This has highest priority
    // and we process that first

    if (null != elemWidth && !elemWidth.trim().isEmpty()) {

      elemWidth = elemWidth.trim();
      if (elemWidth.endsWith("%")) {
        // process percentage values
        int relWidth = Integer.parseInt(elemWidth.replace("%", "").trim());
        int parentWidth = box.getContainingBlock().getContentWidth();
        calculatedWidth = (int) (relWidth * parentWidth / 100.0f);
      } else if (elemWidth.endsWith("px")) {
        // process px values. return value should be expressed in "dots"
        try {
          calculatedWidth = context.getDotsPerPixel()
              * Integer.parseInt(elemWidth.replace("px", "").trim());
        } catch (NumberFormatException nfe) {
          log.error("Invalid / Unsupported width specification " + elemWidth + nfe.getMessage());
        }
      } else {
        // We assume we have absolute number. Absolute no without a unit means pixels
        // we convert it to dots.
        try {
          calculatedWidth = context.getDotsPerPixel() * Integer.parseInt(elemWidth);
        } catch (NumberFormatException nfe) {
          log.error("Invalid / Unsupported width specification " + elemWidth + nfe.getMessage());
        }
      }
      // We did not find a attribute, but the width is specified via css style which is already
      // processed. We simply return the same.
    } else if (cssWidth != -1) {
      calculatedWidth = cssWidth;
    }

    // The width was not specified via an attribute or through css style OR we could not process it
    if (-1 == calculatedWidth) {
      calculatedWidth = (context.getTextRenderer().getWidth(context.getFontContext(),
          RtlFontHelper.getApplicableFont(style, context, this.defaultDpp), text));
    }
    return calculatedWidth;
  }
}
