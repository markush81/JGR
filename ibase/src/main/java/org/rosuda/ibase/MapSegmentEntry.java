package org.rosuda.ibase;


public class MapSegmentEntry {

    public double xp[], yp[];

    public boolean isLake;

    public boolean hasBorder;


    public double minX, maxX, minY, maxY;


    public MapSegmentEntry() {
        isLake = false;
        hasBorder = true;
    }


    public MapSegmentEntry(final double[] xpt, final double[] ypt, boolean lake, boolean border) {
        isLake = lake;
        hasBorder = border;
        xp = new double[xpt.length];
        yp = new double[xpt.length];
        int i = 0;
        while (i < xpt.length) {
            xp[i] = xpt[i];
            yp[i] = (i < ypt.length) ? ypt[i] : 0;
            if (i == 0) {
                minX = maxX = xpt[0];
                minY = maxY = ypt[0];
            }
            if (xpt[i] < minX) {
                minX = xpt[i];
            }
            if (xpt[i] > maxX) {
                maxX = xpt[i];
            }
            if (ypt[i] < minY) {
                minY = ypt[i];
            }
            if (ypt[i] > maxY) {
                maxY = ypt[i];
            }
            i++;
        }
    }


    public MapSegmentEntry(final double[] xpt, final double[] ypt, int offset, int length, boolean lake, boolean border) {
        isLake = lake;
        hasBorder = border;
        if (xpt.length < offset + length) {
            length = xpt.length - offset;
        }
        if (length < 0) {
            length = 0;
        }
        xp = new double[length];
        yp = new double[length];
        int i = offset;
        while (i < offset + length) {
            xp[i - offset] = xpt[i];
            yp[i - offset] = (i < ypt.length) ? ypt[i] : 0;
            if (i == offset) {
                minX = maxX = xpt[i];
                minY = maxY = ypt[i];
            }
            if (xpt[i] < minX) {
                minX = xpt[i];
            }
            if (xpt[i] > maxX) {
                maxX = xpt[i];
            }
            if (ypt[i] < minY) {
                minY = ypt[i];
            }
            if (ypt[i] > maxY) {
                maxY = ypt[i];
            }
            i++;
        }
    }


    public void fixBoundingBox() {
        int i = 0;
        minX = maxX = minY = maxY = 0;
        while (i < xp.length) {
            if (i == 0) {
                minX = maxX = xp[0];
                minY = maxY = yp[0];
            }
            if (xp[i] < minX) {
                minX = xp[i];
            }
            if (xp[i] > maxX) {
                maxX = xp[i];
            }
            if (yp[i] < minY) {
                minY = yp[i];
            }
            if (yp[i] > maxY) {
                maxY = yp[i];
            }
            i++;
        }
    }
}
