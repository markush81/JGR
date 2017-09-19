package org.rosuda.JGR.layout;


public class AnchorConstraint {


    public static final int ANCHOR_NONE = 0;


    public static final int ANCHOR_REL = 1;


    public static final int ANCHOR_ABS = 2;

    public int top;
    public int bottom;
    public int left;
    public int right;
    public int topType;
    public int bottomType;
    public int rightType;
    public int leftType;

    public AnchorConstraint() {
        this(0, 0, 0, 0, ANCHOR_NONE, ANCHOR_NONE, ANCHOR_NONE, ANCHOR_NONE);
    }


    public AnchorConstraint(
            int top,
            int right,
            int bottom,
            int left,
            int topType,
            int rightType,
            int bottomType,
            int leftType) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.topType = topType;
        this.rightType = rightType;
        this.bottomType = bottomType;
        this.leftType = leftType;
    }

}
