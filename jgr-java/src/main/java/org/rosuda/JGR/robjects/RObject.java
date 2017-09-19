package org.rosuda.JGR.robjects;


public class RObject {

    private String name = null;

    private String type = null;

    private RObject parent = null;

    private String info = null;

    private boolean realName = false;


    public RObject(String name, String type, RObject parent, boolean b) {
        this.name = name;
        this.type = type;
        this.parent = parent;
        realName = b;
    }


    public RObject(String name, String type, boolean b) {
        this(name, type, null, b);
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getType() {
        return type;
    }


    public RObject getParent() {
        return parent;
    }


    public void setParent(RObject p) {
        parent = p;
    }


    public boolean isAtomar() {
        return type.indexOf("function") >= 0 || type.indexOf("integer") >= 0
                || type.indexOf("numeric") >= 0
                || type.indexOf("character") >= 0
                || type.indexOf("logical") >= 0 || type.indexOf("factor") >= 0
                || type.indexOf("environment") >= 0;
    }


    public boolean isEditable() {
        return type.indexOf("numeric") >= 0 || type.indexOf("integer") >= 0
                || type.indexOf("factor") >= 0
                || type.indexOf("character") >= 0
                || type.indexOf("data.frame") >= 0
                || type.indexOf("matrix") >= 0 || type.indexOf("list") >= 0;
    }


    public String getRName() {
        if (parent != null && parent.getType().equals("matrix")) {
            return parent.getName() + "[," + (realName ? "\"" : "") + getName()
                    + (realName ? "\"" : "") + "]";
        }
        if (parent != null && parent.getType().equals("table")) {
            return "dimnames(" + parent.getRName() + ")[["
                    + (realName ? "\"" : "") + getName()
                    + (realName ? "\"" : "") + "]]";
        }
        return parent == null ? getName() : parent.getRName() + "[["
                + (realName ? "\"" : "") + getName() + (realName ? "\"" : "")
                + "]]";
    }


    public String getInfo() {
        return info;
    }


    public void setInfo(String s) {
        info = s;
    }

    public String toString() {
        return name + "\t (" + type + ") " + (info != null ? info : "");
    }
}
