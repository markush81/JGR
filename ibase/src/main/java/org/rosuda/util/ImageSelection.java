package org.rosuda.util;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


public class ImageSelection implements Transferable {
    private Image image;


    public ImageSelection(Image image) {
        this.image = image;
    }


    public static ImageSelection setClipboard(Image image) {
        ImageSelection imgSel = new ImageSelection(image);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
        return imgSel;
    }

    public static ImageSelection copyComponent(Component c, boolean whiteBg, boolean antiAliased) {
        Dimension d = c.getSize();
        Image img = c.createImage(d.width, d.height);
        Graphics g = img.getGraphics();
        Graphics2D g2 = (Graphics2D) g;
        if (antiAliased) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        if (whiteBg) {
            g2.setColor(Color.white);
            g2.fillRect(0, 0, d.width, d.height);
        }
        c.paint(g2);
        return setClipboard(img);
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.imageFlavor.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!DataFlavor.imageFlavor.equals(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return image;
    }
}
