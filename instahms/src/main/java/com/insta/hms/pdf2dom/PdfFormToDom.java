package com.insta.hms.pdf2dom;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDComboBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.fit.pdfdom.PDFDomTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

public class PdfFormToDom extends PDFDomTree {

  static Logger logger = LoggerFactory.getLogger(PdfFormToDom.class);

  private static final String[] JUSTIFICATIONS = { "left", "center", "right" };

  /**
   * Instantiates a new pdf form to dom.
   *
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParserConfigurationException
   *           the parser configuration exception
   */
  public PdfFormToDom() throws IOException, ParserConfigurationException {
    super();
    this.defaultStyle = ".page{position:relative; border:1px solid #d4d4dc; margin:20px auto;}\n"
        + ".p,.r{position:absolute;}\n"
        // disable text-shadow fallback for text stroke if stroke supported by browser
        + "@supports(-webkit-text-stroke: 1px black) {" + ".p{text-shadow:none !important;}" + "}";
  }

  // This is overridden to make it display properly in chrome
  @Override
  protected Element createLineElement(float x1, float y1, float x2, float y2) {
    HtmlDivLine line = new HtmlDivLine(x1, y1, x2, y2);
    String color = colorString(getGraphicsState().getStrokingColor());
    // Chrome does not display borders with width less than 1pt.
    float lineStrokeWidth = (line.getLineStrokeWidth() < 1) ? 1 : line.getLineStrokeWidth();

    StringBuilder pstyle = new StringBuilder(50);
    pstyle.append("left:").append(style.formatLength(line.getLeft())).append(';');
    pstyle.append("top:").append(style.formatLength(line.getTop())).append(';');
    pstyle.append("width:").append(style.formatLength(line.getWidth())).append(';');
    pstyle.append("height:").append(style.formatLength(line.getHeight())).append(';');
    pstyle.append(line.getBorderSide()).append(':').append(style.formatLength(lineStrokeWidth))
        .append(" solid ").append(color).append(';');
    if (line.getAngleDegrees() != 0) {
      pstyle.append("transform:").append("rotate(").append(line.getAngleDegrees()).append("deg);");
    }

    Element el = doc.createElement("div");
    el.setAttribute("class", "r");
    el.setAttribute("style", pstyle.toString());
    el.appendChild(doc.createEntityReference("nbsp"));
    return el;
  }

  private void formToDom() {
    PDAcroForm form = document.getDocumentCatalog().getAcroForm();
    List<PDField> fields = form.getFields();
    TreeMap<String, String> sortedMap = new TreeMap<>();
    for (PDField field : fields) {
      for (PDAnnotationWidget widget : field.getWidgets()) {
        try {
          if (pagecnt - 1 == document.getPages().indexOf(widget.getPage())) {
            COSDictionary dict = field.getCOSObject();
            String fieldName = field.getFullyQualifiedName();
            if ("_submit".equals(fieldName)) {
              continue;
            }
            String value = field.getValueAsString();
            if (null == value) {
              value = "";
            }

            if (field instanceof PDRadioButton
                || field instanceof PDCheckBox
                || field instanceof PDTextField
                || field instanceof PDComboBox) {
              float[] rect = transformRectangle(widget.getRectangle());
              float left = rect[0];
              float bottom = rect[1];
              float top = rect[3];
              float right = rect[2];
              String posKey = String.format("%02d", document.getPages().indexOf(widget.getPage()))
                  + "-" + String.format("%08.3f", top) + "-" + String.format("%08.3f", left);
              sortedMap.put(posKey,fieldName);
            }
          }
        } catch (NullPointerException ne) {
          logger.error("{}", ne);
        }
      }
    }
    
    Map<String, String> tabIndexMap = new HashMap<>();
    int idx = 0;
    for (Map.Entry<String,String> entry : sortedMap.entrySet()) {
      tabIndexMap.put(entry.getValue(), String.valueOf((++idx) + 1000));
    }
    for (PDField field : fields) {
      for (PDAnnotationWidget widget : field.getWidgets()) {
        try {
          if (pagecnt - 1 == document.getPages().indexOf(widget.getPage())) {
            COSDictionary dict = field.getCOSObject();
            String fieldName = field.getFullyQualifiedName();
            if ("_submit".equals(fieldName)) {
              continue;
            }
            String value = field.getValueAsString();
            if (null == value) {
              value = "";
            }

            if (field instanceof PDRadioButton || field instanceof PDCheckBox) {
              float[] rect = transformRectangle(widget.getRectangle());
              float left = rect[0];
              float top = rect[3];
              float right = rect[2];
              COSDictionary child = (COSDictionary) widget.getCOSObject().getDictionaryObject("AP");
              COSDictionary temp = (COSDictionary) child.getDictionaryObject("N");
              Set<COSName> keys = temp.keySet();
              for (COSName key : keys) {
                String keyName = key.getName();
                if (!("Off".equals(keyName))) {
                  value = key.getName();
                }
              }
              if (field instanceof PDRadioButton) {
                curpage.appendChild(createRadioButton(left, top, right - left, fieldName, value,
                      tabIndexMap.get(fieldName)));
              } else {
                // Else it is a checkbox
                curpage.appendChild(createCheckboxElement(left, top, fieldName, value,
                      tabIndexMap.get(fieldName)));
              }
            } else if (field instanceof PDTextField) {
              PDTextField textField = (PDTextField) field;
              float[] rect = transformRectangle(widget.getRectangle());
              float left = rect[0];
              float bottom = rect[1];
              float top = rect[3];
              float right = rect[2];
              int justify = textField.getQ();

              String da = ((COSString) textField.getCOSObject().getDictionaryObject("DA"))
                  .toString();
              String[] style = da.split("/")[1].split(" ");
              String font = style[0];
              COSDictionary dr = (COSDictionary) textField.getCOSObject().getDictionaryObject("DR");
              COSDictionary fonts = (COSDictionary) dr.getDictionaryObject("Font").getCOSObject();
              font = ((COSName) ((COSDictionary) fonts.getDictionaryObject(font).getCOSObject())
                  .getDictionaryObject("BaseFont")).getName();
              String size = style[1];

              if (textField.isMultiline()) {
                curpage.appendChild(createTextAreaElement(left, top, right - left, bottom - top,
                    fieldName, value, justify, font, size, tabIndexMap.get(fieldName)));
              } else {
                curpage.appendChild(createInputElement(left, top, right - left, bottom - top,
                    fieldName, value, justify, font, size, tabIndexMap.get(fieldName)));
              }
            } else if (field instanceof PDComboBox) {
              float[] rect = transformRectangle(widget.getRectangle());
              float left = rect[0];
              float bottom = rect[1];
              float top = rect[3];
              float right = rect[2];
              List<String> options = ((PDComboBox) field).getOptions();
              curpage.appendChild(
                  createDropdownElement(left, top, right - left, bottom - top, fieldName, options,
                      tabIndexMap.get(fieldName)));
            }

          }
        } catch (NullPointerException ne) {
          logger.error("{}", ne);
        }
      }
    }
  }

  /**
   * Transform rectangle according to the HTML.
   *
   * @param rect
   *          the rectangle
   * @return the float[left, bottom, right, top]
   */
  private float[] transformRectangle(PDRectangle rect) {
    float left = rect.getLowerLeftX();
    float bottom = rect.getLowerLeftY();
    float right = rect.getUpperRightX();
    float top = rect.getUpperRightY();
    float[] leftTop = transformPosition(left, top);
    float[] rightBottom = transformPosition(right, bottom);
    left = leftTop[0];
    top = leftTop[1];
    right = rightBottom[0];
    bottom = rightBottom[1];
    return new float[] { left, bottom, right, top };
  }

  private Element createInputElement(float positionX, float positionY, float width, float height,
      String fieldName, String value, int justify, String font, String fontSize, String tabIndex) {

    StringBuilder pstyle = new StringBuilder(50);
    pstyle.append("left:").append(style.formatLength(positionX)).append(';');
    pstyle.append("top:").append(style.formatLength(positionY)).append(';');
    pstyle.append("width:").append(style.formatLength(width - 5)).append(';');
    pstyle.append("height:").append(style.formatLength(height)).append(";");
    if (justify < 3) {
      pstyle.append("text-align:").append(JUSTIFICATIONS[justify]).append(";");
    }
    pstyle.append("font-family:").append(font).append(";");
    pstyle.append("font-size:").append(fontSize).append(";");
    Element el = doc.createElement("input");
    el.setAttribute("class", "r");
    el.setAttribute("name", fieldName);
    el.setAttribute("tabindex", tabIndex);
    el.setAttribute("style", pstyle.toString());
    el.setAttribute("value", value);
    return el;
  }

  private Element createTextAreaElement(float positionX, float positionY, float width, float height,
      String fieldName, String value, int justify, String font, String fontSize, String tabIndex) {
    StringBuilder pstyle = new StringBuilder(50);
    pstyle.append("left:").append(style.formatLength(positionX)).append(';');
    pstyle.append("top:").append(style.formatLength(positionY)).append(';');
    pstyle.append("width:").append(style.formatLength(width - 5)).append(';');
    pstyle.append("height:").append(style.formatLength(height - 2)).append(';');
    if (justify < 3) {
      pstyle.append("text-align:").append(JUSTIFICATIONS[justify]).append(";");
    }
    pstyle.append("font-family:").append(font).append(";");
    pstyle.append("font-size:").append(fontSize).append(";");
    Element el = doc.createElement("textarea");
    el.setAttribute("class", "r");
    el.setAttribute("name", fieldName);
    el.setAttribute("tabindex", tabIndex);
    el.setAttribute("style", pstyle.toString());
    Text textNode = doc.createTextNode(value);
    el.appendChild(textNode);
    return el;
  }

  private Element createRadioButton(float positionX, float positionY, float width, String fieldName,
      String fieldValue, String tabIndex) {
    StringBuilder pstyle = new StringBuilder(50);
    pstyle.append("left:").append(style.formatLength(positionX)).append(';');
    pstyle.append("top:").append(style.formatLength(positionY)).append(';');
    pstyle.append("width:").append(style.formatLength(width - 5)).append(';');
    Element el = doc.createElement("input");
    el.setAttribute("class", "r");
    el.setAttribute("type", "radio");
    el.setAttribute("name", fieldName);
    el.setAttribute("tabindex", tabIndex);
    el.setAttribute("value", fieldValue);
    el.setAttribute("style", pstyle.toString());
    return el;
  }

  private Element createCheckboxElement(float positionX, float posistionY, String fieldName,
      String fieldValue, String tabIndex) {
    StringBuilder pstyle = new StringBuilder(50);
    pstyle.append("left:").append(style.formatLength(positionX)).append(';');
    pstyle.append("top:").append(style.formatLength(posistionY)).append(';');
    pstyle.append("margin:0;");
    Element el = doc.createElement("input");
    el.setAttribute("class", "r");
    el.setAttribute("type", "checkbox");
    el.setAttribute("tabindex", tabIndex);
    el.setAttribute("name", fieldName);
    el.setAttribute("value", fieldValue);
    el.setAttribute("style", pstyle.toString());
    return el;
  }

  private Element createDropdownElement(float positionX, float posistionY, float width,
      float height, String fieldName, List<String> options, String tabIndex) {
    StringBuilder pstyle = new StringBuilder(50);
    pstyle.append("left:").append(style.formatLength(positionX)).append(';');
    pstyle.append("top:").append(style.formatLength(posistionY)).append(';');
    pstyle.append("width:").append(style.formatLength(width - 5)).append(';');
    pstyle.append("height:").append(style.formatLength(height - 5)).append(";");
    Element el = doc.createElement("select");
    el.setAttribute("class", "r");
    el.setAttribute("tabindex", tabIndex);
    el.setAttribute("name", fieldName);
    el.setAttribute("style", pstyle.toString());
    for (String option : options) {
      Element optionElement = doc.createElement("option");
      optionElement.setAttribute("value", option);
      Text textNode = doc.createTextNode(option);
      optionElement.appendChild(textNode);
      el.appendChild(optionElement);
    }
    return el;
  }

  @Override
  public void processPage(PDPage page) throws IOException {
    super.processPage(page);
    formToDom();
  }

  @Override
  protected void createDocument() throws ParserConfigurationException {
    super.createDocument();
    Element form = doc.createElement("form");
    form.setAttribute("id", "form");
    form.setAttribute("method", "post");

    body.appendChild(form);
    body = form;
  }

}
