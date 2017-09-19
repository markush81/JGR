package jedit.syntax;


public class Token {

    public static final byte NULL = 0;


    public static final byte COMMENT1 = 1;


    public static final byte COMMENT2 = 2;


    public static final byte LITERAL1 = 3;


    public static final byte LITERAL2 = 4;


    public static final byte LABEL = 5;


    public static final byte KEYWORD1 = 6;


    public static final byte KEYWORD2 = 7;


    public static final byte KEYWORD3 = 8;


    public static final byte OPERATOR = 9;


    public static final byte INVALID = 10;


    public static final byte ID_COUNT = 11;


    public static final byte INTERNAL_FIRST = 100;


    public static final byte INTERNAL_LAST = 126;


    public static final byte END = 127;


    public int length;


    public byte id;


    public Token next;


    public Token(int length, byte id) {
        this.length = length;
        this.id = id;
    }


    public String toString() {
        return "[id=" + id + ",length=" + length + "]";
    }
}
