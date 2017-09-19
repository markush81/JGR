package org.rosuda.JGR.browser;

public interface BrowserNodeFactory {

    BrowserNode generate(BrowserNode parent, String rName, String rClass);

}
