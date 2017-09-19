package jedit.syntax;

import javax.swing.text.Segment;


public class KeywordMap {

    protected int mapLength;
    private Keyword[] map;
    private boolean ignoreCase;


    public KeywordMap(boolean ignoreCase) {
        this(ignoreCase, 52);
        this.ignoreCase = ignoreCase;
    }


    public KeywordMap(boolean ignoreCase, int mapLength) {
        this.mapLength = mapLength;
        this.ignoreCase = ignoreCase;
        map = new Keyword[mapLength];
    }


    public byte lookup(Segment text, int offset, int length) {
        if (length == 0) {
            return Token.NULL;
        }
        Keyword k = map[getSegmentMapKey(text, offset, length)];
        while (k != null) {
            if (length != k.keyword.length) {
                k = k.next;
                continue;
            }
            if (SyntaxUtilities.regionMatches(ignoreCase, text, offset, k.keyword)) {
                return k.id;
            }
            k = k.next;
        }
        return Token.NULL;
    }


    public void add(String keyword, byte id) {
        int key = getStringMapKey(keyword);
        map[key] = new Keyword(keyword.toCharArray(), id, map[key]);
    }


    public boolean getIgnoreCase() {
        return ignoreCase;
    }


    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    protected int getStringMapKey(String s) {
        return (Character.toUpperCase(s.charAt(0)) + Character.toUpperCase(s.charAt(s.length() - 1))) % mapLength;
    }

    protected int getSegmentMapKey(Segment s, int off, int len) {
        return (Character.toUpperCase(s.array[off]) + Character.toUpperCase(s.array[off + len - 1])) % mapLength;
    }


    class Keyword {
        public char[] keyword;
        public byte id;
        public Keyword next;

        public Keyword(char[] keyword, byte id, Keyword next) {
            this.keyword = keyword;
            this.id = id;
            this.next = next;
        }
    }
}
