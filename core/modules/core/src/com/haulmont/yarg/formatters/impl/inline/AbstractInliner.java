/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.impl.inline;

import com.haulmont.yarg.exception.ReportFormattingException;
import com.haulmont.yarg.formatters.impl.doc.OfficeComponent;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeResourceProvider;
import com.haulmont.yarg.formatters.impl.xlsx.CellReference;
import com.sun.star.awt.Size;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.graphic.XGraphic;
import com.sun.star.graphic.XGraphicProvider;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lib.uno.adapter.ByteArrayToXInputStreamAdapter;
import com.sun.star.text.HoriOrientation;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextRange;
import com.sun.star.uno.XComponentContext;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.docx4j.dml.*;
import org.docx4j.dml.spreadsheetdrawing.*;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DrawingML.Drawing;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.xlsx4j.sml.Cell;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.haulmont.yarg.formatters.impl.doc.UnoConverter.as;

public abstract class AbstractInliner implements ContentInliner {
    private static final String TEXT_GRAPHIC_OBJECT = "com.sun.star.text.TextGraphicObject";
    private static final String GRAPHIC_PROVIDER_OBJECT = "com.sun.star.graphic.GraphicProvider";
    private static final int IMAGE_FACTOR = 27;

    protected Pattern tagPattern;
    protected int docxUniqueId1, docxUniqueId2;

    protected abstract byte[] getContent(Object paramValue);

    //todo merge cells - set up right anchor for merged cells, cause now image it written only to 1 cell
    public void inlineToXlsx(SpreadsheetMLPackage pkg, WorksheetPart worksheetPart, Cell newCell, Object paramValue, Matcher matcher) {
        try {
            Image image = new Image(paramValue, matcher);
            if (image.isValid()) {
                BinaryPartAbstractImage xlsxImage = null;
                xlsxImage = BinaryPartAbstractImage.createImagePart(pkg, worksheetPart, image.imageContent);
                CTTwoCellAnchor anchor = new CTTwoCellAnchor();
                CTMarker from = new CTMarker();
                CellReference cellReference = new CellReference("", newCell.getR());
                from.setCol(cellReference.getColumn() - 1);
                from.setRow(cellReference.getRow() - 1);
                from.setColOff(0L);
                from.setRowOff(0L);
                CTMarker to = new CTMarker();
                to.setCol(cellReference.getColumn());
                to.setRow(cellReference.getRow());
                to.setColOff(0L);
                to.setRowOff(0L);

                anchor.setFrom(from);
                anchor.setTo(to);


                putImage(worksheetPart, pkg, xlsxImage, image, anchor);
            }
        } catch (Exception e) {
            throw new ReportFormattingException("An error occurred while inserting bitmap to xlsx file", e);
        }
    }

    private void putImage(WorksheetPart worksheetPart, SpreadsheetMLPackage pkg, BinaryPartAbstractImage xlsxImage, Image image, CTTwoCellAnchor anchor) throws InvalidFormatException {
        PartName drawingPartName = new PartName(worksheetPart.getPartName().getName().replace("worksheets/sheet", "drawings/drawing"));
        Drawing drawing = (Drawing) pkg.getParts().get(drawingPartName);
        java.util.List<Object> objects = drawing.getJaxbElement().getEGAnchor();
        String rid = "rId" + (objects.size() + 1);

        CTPicture pic = new CTPicture();
        CTPictureNonVisual nvPicPr = new CTPictureNonVisual();
        CTNonVisualDrawingProps nvpr = new CTNonVisualDrawingProps();
        nvpr.setId(objects.size() + 2);
        String name = xlsxImage.getPartName().getName();
        name = name.substring(name.lastIndexOf("/") + 1);
        nvpr.setName(name);
        nvpr.setDescr(name);
        nvPicPr.setCNvPr(nvpr);
        CTPictureLocking ctPictureLocking = new CTPictureLocking();
        ctPictureLocking.setNoChangeAspect(true);
        CTNonVisualPictureProperties nvpp = new CTNonVisualPictureProperties();
        nvpp.setPicLocks(ctPictureLocking);
        nvPicPr.setCNvPicPr(nvpp);
        pic.setNvPicPr(nvPicPr);
        CTBlipFillProperties blipProps = new CTBlipFillProperties();
        CTStretchInfoProperties props = new CTStretchInfoProperties();
        CTRelativeRect rect = new CTRelativeRect();
        props.setFillRect(rect);
        blipProps.setStretch(props);
        CTBlip blip = new CTBlip();
        blip.setEmbed(rid);
        blip.setCstate(STBlipCompression.PRINT);
        blipProps.setBlip(blip);
        pic.setBlipFill(blipProps);
        CTShapeProperties sppr = new CTShapeProperties();
        ImageSize imageSize = new ImageSize(image.width, image.height, 96);//todo this doesn't work unfortunately
        imageSize.calcSizeFromPixels();
        CTPoint2D off = new CTPoint2D();
        off.setX(0);
        off.setY(0);
        CTPositiveSize2D ext = new CTPositiveSize2D();
        ext.setCx(imageSize.getWidthMpt());
        ext.setCy(imageSize.getHeightMpt());
        CTTransform2D xfrm = new CTTransform2D();
        xfrm.setOff(off);
        xfrm.setExt(ext);
        sppr.setXfrm(xfrm);
        CTPresetGeometry2D prstGeom = new CTPresetGeometry2D();
        prstGeom.setPrst(STShapeType.RECT);
        prstGeom.setAvLst(new CTGeomGuideList());
        sppr.setPrstGeom(prstGeom);
        pic.setSpPr(sppr);
        anchor.setPic(pic);
        CTAnchorClientData data = new CTAnchorClientData();
        anchor.setClientData(data);

        drawing.getJaxbElement().getEGAnchor().add(anchor);

        Relationship rel = new Relationship();
        rel.setId(rid);
        rel.setType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/image");
        rel.setTarget("../media/" + name);

        drawing.getRelationshipsPart().addRelationship(rel);
        RelationshipsPart relPart = drawing.getRelationshipsPart();
        pkg.getParts().remove(relPart.getPartName());
        pkg.getParts().put(relPart);
        pkg.getParts().remove(drawing.getPartName());
        pkg.getParts().put(drawing);
    }


    @Override
    public void inlineToDocx(WordprocessingMLPackage wordPackage, Text text, Object paramValue, Matcher paramsMatcher) {
        try {
            Image image = new Image(paramValue, paramsMatcher);
            if (image.isValid()) {
                BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordPackage, image.imageContent);
                Inline inline = imagePart.createImageInline("", "", docxUniqueId1++, docxUniqueId2++, false);
                ImageSize oldSize = imagePart.getImageInfo().getSize();
                double widthExtent = (double) image.width / oldSize.getWidthPx();
                double heightExtent = (double) image.height / oldSize.getHeightPx();
                inline.getExtent().setCx((long) (inline.getExtent().getCx() * widthExtent));
                inline.getExtent().setCy((long) (inline.getExtent().getCy() * heightExtent));
                org.docx4j.wml.Drawing drawing = new org.docx4j.wml.ObjectFactory().createDrawing();
                R run = (R) text.getParent();
                run.getContent().add(drawing);
                drawing.getAnchorOrInline().add(inline);
                text.setValue("");
            }
        } catch (Exception e) {
            throw new ReportFormattingException("An error occurred while inserting bitmap to docx file", e);
        }
    }

    @Override
    public void inlineToXls(HSSFPatriarch patriarch, HSSFCell resultCell, Object paramValue, Matcher paramsMatcher) {
        try {
            Image image = new Image(paramValue, paramsMatcher);
            if (image.isValid()) {
                resultCell.getRow().setHeightInPoints(image.height);
                HSSFSheet sheet = resultCell.getSheet();
                HSSFWorkbook workbook = sheet.getWorkbook();

                int pictureIdx = workbook.addPicture(image.imageContent, Workbook.PICTURE_TYPE_JPEG);

                CreationHelper helper = workbook.getCreationHelper();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(resultCell.getColumnIndex());
                anchor.setRow1(resultCell.getRowIndex());
                anchor.setCol2(resultCell.getColumnIndex());
                anchor.setRow2(resultCell.getRowIndex());
                if (patriarch == null) {
                    throw new IllegalArgumentException(String.format("No HSSFPatriarch object provided. Charts on this sheet could cause this effect. Please check sheet %s", resultCell.getSheet().getSheetName()));
                }
                HSSFPicture picture = patriarch.createPicture(anchor, pictureIdx);
                Dimension imageDimension = picture.getImageDimension();
                double actualHeight = imageDimension.getHeight();
                picture.resize((double) image.height / actualHeight);
            }
        } catch (IllegalArgumentException e) {
            throw new ReportFormattingException("An error occurred while inserting bitmap to xls file", e);
        }
    }

    @Override
    public void inlineToDoc(OfficeComponent officeComponent, XTextRange textRange, XText destination, Object paramValue, Matcher paramsMatcher) throws Exception {
        try {
            if (paramValue != null) {
                Image image = new Image(paramValue, paramsMatcher);

                if (image.isValid()) {
                    XComponent xComponent = officeComponent.getOfficeComponent();
                    insertImage(xComponent, officeComponent.getOfficeResourceProvider(), destination, textRange, image);
                }
            }
        } catch (Exception e) {
            throw new ReportFormattingException("An error occurred while inserting bitmap to doc file", e);
        }
    }

    protected void insertImage(XComponent document, OfficeResourceProvider officeResourceProvider, XText destination, XTextRange textRange,
                               Image image) throws Exception {
        XMultiServiceFactory xFactory = as(XMultiServiceFactory.class, document);
        XComponentContext xComponentContext = officeResourceProvider.getXComponentContext();
        XMultiComponentFactory serviceManager = xComponentContext.getServiceManager();

        Object oImage = xFactory.createInstance(TEXT_GRAPHIC_OBJECT);
        Object oGraphicProvider = serviceManager.createInstanceWithContext(GRAPHIC_PROVIDER_OBJECT, xComponentContext);

        XGraphicProvider xGraphicProvider = as(XGraphicProvider.class, oGraphicProvider);

        XPropertySet imageProperties = buildImageProperties(xGraphicProvider, oImage, image.imageContent);
        XTextContent xTextContent = as(XTextContent.class, oImage);
        destination.insertTextContent(textRange, xTextContent, true);
        setImageSize(image.width, image.height, oImage, imageProperties);
    }

    protected void setImageSize(int width, int height, Object oImage, XPropertySet imageProperties)
            throws Exception {
        Size aActualSize = (Size) imageProperties.getPropertyValue("ActualSize");
        aActualSize.Height = height * IMAGE_FACTOR;
        aActualSize.Width = width * IMAGE_FACTOR;
        XShape xShape = as(XShape.class, oImage);
        xShape.setSize(aActualSize);
    }

    protected XPropertySet buildImageProperties(XGraphicProvider xGraphicProvider, Object oImage, byte[] imageContent)
            throws Exception {
        XPropertySet imageProperties = as(XPropertySet.class, oImage);

        PropertyValue[] propValues = new PropertyValue[]{new PropertyValue()};
        propValues[0].Name = "InputStream";
        propValues[0].Value = new ByteArrayToXInputStreamAdapter(imageContent);

        XGraphic graphic = xGraphicProvider.queryGraphic(propValues);
        if (graphic != null) {
            imageProperties.setPropertyValue("Graphic", graphic);

            imageProperties.setPropertyValue("HoriOrient", HoriOrientation.NONE);
            imageProperties.setPropertyValue("VertOrient", HoriOrientation.NONE);

            imageProperties.setPropertyValue("HoriOrientPosition", 0);
            imageProperties.setPropertyValue("VertOrientPosition", 0);
        }

        return imageProperties;
    }

    protected class Image {
        byte[] imageContent = null;
        int width = 0;
        int height = 0;

        public Image(Object paramValue, Matcher paramsMatcher) {
            if (paramValue == null) {
                return;
            }

            imageContent = getContent(paramValue);
            if (imageContent.length == 0) {
                imageContent = null;
                return;
            }

            width = Integer.parseInt(paramsMatcher.group(1));
            height = Integer.parseInt(paramsMatcher.group(2));
        }

        boolean isValid() {
            return imageContent != null;
        }
    }

}
