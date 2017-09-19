package org.rosuda.JGR.robjects;


import java.text.DecimalFormat;
import java.util.Vector;


public class RModel {

    private final Vector info = new Vector();
    private final DecimalFormat dformat = new DecimalFormat("#0.00");
    private Double rsquared, deviance, aic;
    private Integer df = null;
    private String family = null;
    private String call = null;
    private String data = null;
    private String type = "model";

    private String name;

    public RModel(String name, String type) {
        this.name = name;
        if (type != null) {
            this.type = type;
        }
    }


    public String getTypeName() {
        return type;
    }


    public String getName() {
        return name;
    }


    public String getCall() {
        return call;
    }


    public void setCall(String call) {
        this.call = call;
    }


    public String getToolTip() {
        return "<html><pre>" + call + "</pre></html>";
    }


    public void setRsquared(double r) {
        rsquared = new Double(dformat.format(r).replace(',', '.'));
    }


    public void setDeviance(double d) {
        deviance = new Double(dformat.format(d).replace(',', '.'));
    }


    public void setDf(int df) {
        this.df = new Integer(df);
    }


    public void setAic(double a) {
        aic = new Double(dformat.format(a).replace(',', '.'));
    }


    public String getFamily() {
        return family;
    }


    public void setFamily(String f) {
        family = f;
    }


    public String getData() {
        return data;
    }


    public void setData(String d) {
        data = d;
    }


    public Vector getInfo() {
        if (info.size() == 0) {
            info.add(getName());
            info.add(getData());
            info.add(getTypeName());
            info.add(family);
            info.add(df);
            info.add(rsquared);
            info.add(aic);
            info.add(deviance);
        }
        return info;
    }

    public String toString() {
        return getName() + " (model)";
    }
}
