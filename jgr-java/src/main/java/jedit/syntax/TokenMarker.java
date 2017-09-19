package jedit.syntax;

import javax.swing.text.Segment;


public abstract class TokenMarker {

    protected Token firstToken;

    protected Token lastToken;

    protected LineInfo[] lineInfo;

    protected int length;

    protected int lastLine;

    protected boolean nextLineRequested;


    protected TokenMarker() {
        lastLine = -1;
    }


    public Token markTokens(Segment line, int lineIndex) {
        if (lineIndex >= length) {
            throw new IllegalArgumentException("Tokenizing invalid line: " + lineIndex);
        }

        lastToken = null;

        LineInfo info = lineInfo[lineIndex];
        LineInfo prev;
        if (lineIndex == 0) {
            prev = null;
        } else {
            prev = lineInfo[lineIndex - 1];
        }

        byte oldToken = info.token;
        byte token = markTokensImpl(prev == null ? Token.NULL : prev.token, line, lineIndex);

        info.token = token;


        if (!(lastLine == lineIndex && nextLineRequested)) {
            nextLineRequested = (oldToken != token);
        }

        lastLine = lineIndex;

        addToken(0, Token.END);

        return firstToken;
    }


    protected abstract byte markTokensImpl(byte token, Segment line, int lineIndex);


    public boolean supportsMultilineTokens() {
        return true;
    }


    public void insertLines(int index, int lines) {
        if (lines <= 0) {
            return;
        }
        length += lines;
        ensureCapacity(length);
        int len = index + lines;
        System.arraycopy(lineInfo, index, lineInfo, len, lineInfo.length - len);

        for (int i = index + lines - 1; i >= index; i--) {
            lineInfo[i] = new LineInfo();
        }
    }


    public void deleteLines(int index, int lines) {
        if (lines <= 0) {
            return;
        }
        int len = index + lines;
        length -= lines;
        System.arraycopy(lineInfo, len, lineInfo, index, lineInfo.length - len);
    }


    public int getLineCount() {
        return length;
    }


    public boolean isNextLineRequested() {
        return nextLineRequested;
    }


    protected void ensureCapacity(int index) {
        if (lineInfo == null) {
            lineInfo = new LineInfo[index + 1];
        } else if (lineInfo.length <= index) {
            LineInfo[] lineInfoN = new LineInfo[(index + 1) * 2];
            System.arraycopy(lineInfo, 0, lineInfoN, 0, lineInfo.length);
            lineInfo = lineInfoN;
        }
    }


    protected void addToken(int length, byte id) {
        if (id >= Token.INTERNAL_FIRST && id <= Token.INTERNAL_LAST) {
            throw new InternalError("Invalid id: " + id);
        }

        if (length == 0 && id != Token.END) {
            return;
        }

        if (firstToken == null) {
            firstToken = new Token(length, id);
            lastToken = firstToken;
        } else if (lastToken == null) {
            lastToken = firstToken;
            firstToken.length = length;
            firstToken.id = id;
        } else if (lastToken.next == null) {
            lastToken.next = new Token(length, id);
            lastToken = lastToken.next;
        } else {
            lastToken = lastToken.next;
            lastToken.length = length;
            lastToken.id = id;
        }
    }


    public class LineInfo {

        public byte token;

        public Object obj;


        public LineInfo() {
        }


        public LineInfo(byte token, Object obj) {
            this.token = token;
            this.obj = obj;
        }
    }
}
