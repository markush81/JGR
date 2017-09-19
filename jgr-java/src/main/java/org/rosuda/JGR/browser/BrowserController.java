package org.rosuda.JGR.browser;

import java.util.TreeMap;
import java.util.Vector;

public class BrowserController {

    public static int MAX_CHILDREN = 1000;

    protected static boolean initialized = false;

    protected static TreeMap factories = new TreeMap();

    protected static BrowserNodeFactory defaultFactory = new DefaultBrowserNode();

    public static void initialize() {
        if (!initialized) {

            initialized = true;
            setFactory("numeric", new NumericNode());
            setFactory("integer", new NumericNode());
            setFactory("factor", new FactorNode());
            setFactory("character", new FactorNode());
            setFactory("logical", new FactorNode());
            setFactory("data.frame", new DataFrameNode());
            setFactory("matrix", new MatrixNode());
            setFactory("environment", new EnvironmentNode());
            setFactory("function", new FunctionNode());
            setFactory("list", new DefaultBrowserNode());
            setFactory("lm", new LmNode());
            setFactory("glm", new LmNode());
        }
    }

    public static BrowserNode createNode(BrowserNode parent, String rName, String rClass) {
        initialize();
        BrowserNodeFactory fact = (BrowserNodeFactory) factories.get(rClass);
        if (fact == null) {
            return defaultFactory.generate(parent, rName, rClass);
        }

        return fact.generate(parent, rName, rClass);
    }

    public static void setFactory(String className, BrowserNodeFactory factory) {
        initialize();
        factories.put(className, factory);
    }

    public static Vector getClasses() {
        initialize();
        Vector res = new Vector();
        res.addAll(factories.keySet());
        return res;
    }

    public void setDefaultFactory(BrowserNodeFactory factory) {
        initialize();
        defaultFactory = factory;
    }

}
