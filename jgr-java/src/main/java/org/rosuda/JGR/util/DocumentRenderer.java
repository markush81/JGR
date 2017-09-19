package org.rosuda.JGR.util;


import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.View;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class DocumentRenderer implements Printable {

    protected int currentPage = -1;


    protected JEditorPane jeditorPane;


    protected double pageEndY = 0;


    protected double pageStartY = 0;


    protected boolean scaleWidthToFit = true;


    protected PageFormat pFormat;

    protected PrinterJob pJob;


    public DocumentRenderer() {
        pFormat = new PageFormat();
        pJob = PrinterJob.getPrinterJob();
    }


    public Document getDocument() {
        if (jeditorPane != null) {
            return jeditorPane.getDocument();
        } else {
            return null;
        }
    }

    public void setDocument(JEditorPane jedPane) {
        jeditorPane = new JEditorPane();
        setDocument(jedPane.getContentType(), jedPane.getDocument());
    }

    public void setDocument(PlainDocument plainDocument) {
        jeditorPane = new JEditorPane();
        setDocument("text/rtf", plainDocument);
    }

    public boolean getScaleWidthToFit() {
        return scaleWidthToFit;
    }

    public void setScaleWidthToFit(boolean scaleWidth) {
        scaleWidthToFit = scaleWidth;
    }

    public void pageDialog() {
        pFormat = pJob.pageDialog(pFormat);
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        double scale = 1.0;
        Graphics2D graphics2D;
        View rootView;

        graphics2D = (Graphics2D) graphics;

        jeditorPane.setSize((int) pageFormat.getImageableWidth(), Integer.MAX_VALUE);
        jeditorPane.validate();

        rootView = jeditorPane.getUI().getRootView(jeditorPane);

        if ((scaleWidthToFit) && (jeditorPane.getMinimumSize().getWidth() > pageFormat.getImageableWidth())) {
            scale = pageFormat.getImageableWidth() / jeditorPane.getMinimumSize().getWidth();
            graphics2D.scale(scale, scale);
        }

        graphics2D.setClip((int) (pageFormat.getImageableX() / scale), (int) (pageFormat.getImageableY() / scale), (int) (pageFormat
                .getImageableWidth() / scale), (int) (pageFormat.getImageableHeight() / scale));

        if (pageIndex > currentPage) {
            currentPage = pageIndex;
            pageStartY += pageEndY;
            pageEndY = graphics2D.getClipBounds().getHeight();
        }

        graphics2D.translate(graphics2D.getClipBounds().getX(), graphics2D.getClipBounds().getY());

        Rectangle allocation = new Rectangle(0, (int) -pageStartY, (int) (jeditorPane.getMinimumSize().getWidth()), (int) (jeditorPane
                .getPreferredSize().getHeight()));

        if (printView(graphics2D, allocation, rootView)) {
            return Printable.PAGE_EXISTS;
        } else {
            pageStartY = 0;
            pageEndY = 0;
            currentPage = -1;
            return Printable.NO_SUCH_PAGE;
        }
    }

    public void print(HTMLDocument htmlDocument) {
        setDocument(htmlDocument);
        printDialog();
    }

    public void print(JEditorPane jedPane) {
        setDocument(jedPane);
        printDialog();
    }

    public void print(PlainDocument plainDocument) {
        setDocument(plainDocument);
        printDialog();
    }

    protected void printDialog() {
        if (pJob.printDialog()) {
            pJob.setPrintable(this, pFormat);
            try {
                pJob.print();
            } catch (PrinterException printerException) {
                pageStartY = 0;
                pageEndY = 0;
                currentPage = -1;
            }
        }
    }

    protected boolean printView(Graphics2D graphics2D, Shape allocation, View view) {
        boolean pageExists = false;
        Rectangle clipRectangle = graphics2D.getClipBounds();
        Shape childAllocation;
        View childView;

        if (view.getViewCount() > 0) {
            for (int i = 0; i < view.getViewCount(); i++) {
                childAllocation = view.getChildAllocation(i, allocation);
                if (childAllocation != null) {
                    childView = view.getView(i);
                    if (printView(graphics2D, childAllocation, childView)) {
                        pageExists = true;
                    }
                }
            }
        } else if (allocation.getBounds().getMaxY() >= clipRectangle.getY()) {
            pageExists = true;

            if ((allocation.getBounds().getHeight() > clipRectangle.getHeight()) && (allocation.intersects(clipRectangle))) {
                view.paint(graphics2D, allocation);
            } else if (allocation.getBounds().getY() >= clipRectangle.getY()) {
                if (allocation.getBounds().getMaxY() <= clipRectangle.getMaxY()) {
                    view.paint(graphics2D, allocation);
                } else if (allocation.getBounds().getY() < pageEndY) {
                    pageEndY = allocation.getBounds().getY();
                }
            }
        }
        return pageExists;
    }

    protected void setContentType(String type) {
        jeditorPane.setContentType(type);
    }

    public void setDocument(HTMLDocument htmlDocument) {
        jeditorPane = new JEditorPane();
        setDocument("text/html", htmlDocument);
    }

    protected void setDocument(String type, Document document) {
        setContentType(type);
        jeditorPane.setDocument(document);
    }
}
