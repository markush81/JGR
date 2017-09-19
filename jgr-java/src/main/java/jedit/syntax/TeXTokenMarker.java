package jedit.syntax;

import javax.swing.text.Segment;


public class TeXTokenMarker extends TokenMarker {

    public static final byte BDFORMULA = Token.INTERNAL_FIRST;

    public static final byte EDFORMULA = (byte) (Token.INTERNAL_FIRST + 1);

    public byte markTokensImpl(byte token, Segment line, int lineIndex) {
        char[] array = line.array;
        int offset = line.offset;
        int lastOffset = offset;
        int length = line.count + offset;
        boolean backslash = false;
        loop:
        for (int i = offset; i < length; i++) {
            int i1 = (i + 1);

            char c = array[i];


            if (Character.isLetter(c)) {
                backslash = false;
            } else {
                if (backslash) {


                    backslash = false;
                    if (token == Token.KEYWORD2 || token == EDFORMULA) {
                        token = Token.KEYWORD2;
                    }
                    addToken(i1 - lastOffset, token);
                    lastOffset = i1;
                    if (token == Token.KEYWORD1) {
                        token = Token.NULL;
                    }
                    continue;
                } else {


                    if (token == BDFORMULA || token == EDFORMULA) {
                        token = Token.KEYWORD2;
                    }
                    addToken(i - lastOffset, token);
                    if (token == Token.KEYWORD1) {
                        token = Token.NULL;
                    }
                    lastOffset = i;
                }
            }
            switch (c) {
                case '%':
                    if (backslash) {
                        backslash = false;
                        break;
                    }
                    addToken(i - lastOffset, token);
                    addToken(length - i, Token.COMMENT1);
                    lastOffset = length;
                    break loop;
                case '\\':
                    backslash = true;
                    if (token == Token.NULL) {
                        token = Token.KEYWORD1;
                        addToken(i - lastOffset, Token.NULL);
                        lastOffset = i;
                    }
                    break;
                case '$':
                    backslash = false;
                    if (token == Token.NULL) {
                        token = Token.KEYWORD2;
                        addToken(i - lastOffset, Token.NULL);
                        lastOffset = i;
                    } else if (token == Token.KEYWORD1) {
                        token = Token.KEYWORD2;
                        addToken(i - lastOffset, Token.KEYWORD1);
                        lastOffset = i;
                    } else if (token == Token.KEYWORD2) {
                        if (i - lastOffset == 1 && array[i - 1] == '$') {
                            token = BDFORMULA;
                            break;
                        }
                        token = Token.NULL;
                        addToken(i1 - lastOffset, Token.KEYWORD2);
                        lastOffset = i1;
                    } else if (token == BDFORMULA) {
                        token = EDFORMULA;
                    } else if (token == EDFORMULA) {
                        token = Token.NULL;
                        addToken(i1 - lastOffset, Token.KEYWORD2);
                        lastOffset = i1;
                    }
                    break;
            }
        }
        if (lastOffset != length) {
            addToken(length - lastOffset, token == BDFORMULA || token == EDFORMULA ? Token.KEYWORD2 : token);
        }
        return (token != Token.KEYWORD1 ? token : Token.NULL);
    }
}
