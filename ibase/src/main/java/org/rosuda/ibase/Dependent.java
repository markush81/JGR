package org.rosuda.ibase;

import java.util.Vector;


public interface Dependent {

    void Notifying(NotifyMsg msg, Object src, Vector path);
}
