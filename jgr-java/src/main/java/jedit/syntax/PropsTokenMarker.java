package jedit.syntax;

import javax.swing.text.Segment;


public class PropsTokenMarker extends TokenMarker {
    public static final byte VALUE = Token.INTERNAL_FIRST;

    public byte markTokensImpl(byte token, Segment line, int lineIndex) {
        char[] array = line.array;
        int offset = line.offset;
        int lastOffset = offset;
        int length = line.count + offset;
        loop:
        for (int i = offset; i < length; i++) {
            int i1 = (i + 1);

            switch (token) {
                case Token.NULL:
                    switch (array[i]) {
                        case '#':
                        case ';':
                            if (i == offset) {
                                addToken(line.count, Token.COMMENT1);
                                lastOffset = length;
                                break loop;
                            }
                            break;
                        case '[':
                            if (i == offset) {
                                addToken(i - lastOffset, token);
                                token = Token.KEYWORD2;
                                lastOffset = i;
                            }
                            break;
                        case '=':
                            addToken(i - lastOffset, Token.KEYWORD1);
                            token = VALUE;
                            lastOffset = i;
                            break;
                    }
                    break;
                case Token.KEYWORD2:
                    if (array[i] == ']') {
                        addToken(i1 - lastOffset, token);
                        token = Token.NULL;
                        lastOffset = i1;
                    }
                    break;
                case VALUE:
                    break;
                default:
                    throw new InternalError("Invalid state: " + token);
            }
        }
        if (lastOffset != length) {
            addToken(length - lastOffset, Token.NULL);
        }
        return Token.NULL;
    }

    public boolean supportsMultilineTokens() {
        return false;
    }
}
