package org.rosuda.ibase;


public class NotifyMsg {
    Object source;
    int messageID;
    String cmd;
    Object[] par;

    public NotifyMsg(Object src, int msgid, String command, Object[] params) {
        source = src;
        messageID = msgid;
        cmd = command;
        par = params;
    }

    public NotifyMsg(Object src, int msgid, String command) {
        this(src, msgid, command, null);
    }

    public NotifyMsg(Object src, int msgid) {
        this(src, msgid, null, null);
    }

    public NotifyMsg(Object src) {
        this(src, 0, null, null);
    }

    public Object getSource() {
        return source;
    }

    public int getMessageID() {
        return messageID;
    }

    public String getCommand() {
        return cmd;
    }

    public Object[] getParams() {
        return par;
    }

    public int parCount() {
        return (par == null) ? 0 : par.length;
    }

    public Object parAt(int pos) {
        return (par == null || pos < 0 || pos >= par.length) ? null : par[pos];
    }

    public int parI(int pos) {
        return (par == null || pos < 0 || pos >= par.length) ? 0 : (((Number) par[pos]).intValue());
    }

    public double parD(int pos) {
        return (par == null || pos < 0 || pos >= par.length) ? 0 : (((Number) par[pos]).doubleValue());
    }

    public String parS(int pos) {
        return (par == null || pos < 0 || pos >= par.length) ? null : (par[pos].toString());
    }

    public String toString() {
        return "NotifyMsg[" + messageID + "]from[" + source + "]";
    }
}
