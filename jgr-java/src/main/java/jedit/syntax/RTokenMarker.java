package jedit.syntax;

import javax.swing.text.Segment;
import java.util.Vector;


public class RTokenMarker extends TokenMarker {

    private static KeywordMap cKeywords;
    private boolean cpp;
    private KeywordMap keywords;
    private int lastOffset;
    private int lastKeyword;

    public RTokenMarker() {
        this(true, getKeywords());
    }

    public RTokenMarker(boolean cpp, KeywordMap keywords) {
        this.cpp = cpp;
        this.keywords = keywords;
    }

    public static KeywordMap getKeywords() {
        if (cKeywords == null) {
            cKeywords = new KeywordMap(false);


            cKeywords.add("in", Token.KEYWORD1);
            cKeywords.add("else", Token.KEYWORD1);
            cKeywords.add("repeat", Token.KEYWORD1);
            cKeywords.add("next", Token.KEYWORD1);
            cKeywords.add("for", Token.KEYWORD1);
            cKeywords.add("if", Token.KEYWORD1);
            cKeywords.add("while", Token.KEYWORD1);
            cKeywords.add("function", Token.KEYWORD1);

            Vector _keywords = org.rosuda.JGR.JGR.KEYWORDS;
            for (int i = 0; i < _keywords.size(); i++)
                cKeywords.add(_keywords.get(i) + "", Token.KEYWORD1);


            _keywords = org.rosuda.JGR.JGR.OBJECTS;
            for (int i = 0; i < _keywords.size(); i++)
                cKeywords.add(_keywords.get(i) + "", Token.KEYWORD2);


            cKeywords.add("TRUE", Token.LITERAL2);
            cKeywords.add("T", Token.LITERAL2);
            cKeywords.add("FALSE", Token.LITERAL2);
            cKeywords.add("F", Token.LITERAL2);
            cKeywords.add("NULL", Token.LITERAL2);
            cKeywords.add("NA", Token.LITERAL2);
            cKeywords.add("NA_integer_", Token.LITERAL2);
            cKeywords.add("NA_real_", Token.LITERAL2);
            cKeywords.add("NA_complex_", Token.LITERAL2);
            cKeywords.add("NA_character_", Token.LITERAL2);
            cKeywords.add("Inf", Token.LITERAL2);
            cKeywords.add("NaN", Token.LITERAL2);


            cKeywords.add("#", Token.COMMENT1);
        }
        return cKeywords;
    }

    public byte markTokensImpl(byte token, Segment line, int lineIndex) {
        char[] array = line.array;
        int offset = line.offset;
        lastOffset = offset;
        lastKeyword = offset;
        int length = line.count + offset;
        boolean backslash = false;

        loop:
        for (int i = offset; i < length; i++) {
            int i1 = (i + 1);

            char c = array[i];
            if (c == '\\') {
                backslash = !backslash;
                continue;
            }

            switch (token) {
                case Token.NULL:
                    switch (c) {
                        case '#':
                            if (backslash) {
                                backslash = false;
                            } else if (cpp) {
                                if (doKeyword(line, i, c)) {
                                    break;
                                }
                                addToken(i - lastOffset, token);
                                addToken(length - i, Token.COMMENT1);
                                lastOffset = lastKeyword = length;
                                break loop;
                            }
                            break;
                        case '"':
                            doKeyword(line, i, c);
                            if (backslash) {
                                backslash = false;
                            } else {
                                addToken(i - lastOffset, token);
                                token = Token.LITERAL1;
                                lastOffset = lastKeyword = i;
                            }
                            break;
                        case '\'':
                            doKeyword(line, i, c);
                            if (backslash) {
                                backslash = false;
                            } else {
                                addToken(i - lastOffset, token);
                                token = Token.LITERAL2;
                                lastOffset = lastKeyword = i;
                            }
                            break;


                        default:
                            backslash = false;
                            if (!Character.isLetterOrDigit(c) && c != '_' && ".".indexOf(c) == -1) {
                                doKeyword(line, i, c);
                            }
                            break;
                    }
                    break;
                case Token.COMMENT1:
                case Token.COMMENT2:
                    backslash = false;

                    if (array[i1] == '#') {
                        i++;
                        addToken((i + 1) - lastOffset, token);
                        token = Token.NULL;
                        lastOffset = lastKeyword = i + 1;
                    }

                    break;
                case Token.LITERAL1:
                    if (backslash) {
                        backslash = false;
                    } else if (c == '"') {
                        addToken(i1 - lastOffset, token);
                        token = Token.NULL;
                        lastOffset = lastKeyword = i1;
                    }
                    break;
                case Token.LITERAL2:
                    if (backslash) {
                        backslash = false;
                    } else if (c == '\'') {
                        addToken(i1 - lastOffset, Token.LITERAL1);
                        token = Token.NULL;
                        lastOffset = lastKeyword = i1;
                    }
                    break;
                default:
                    throw new InternalError("Invalid state: " + token);
            }
        }

        if (token == Token.NULL) {
            doKeyword(line, length, '\0');
        }

        switch (token) {
            case Token.LITERAL1:
            case Token.LITERAL2:
                addToken(length - lastOffset, Token.INVALID);
                token = Token.NULL;
                break;
            case Token.KEYWORD2:
                addToken(length - lastOffset, token);
                if (!backslash) {
                    token = Token.NULL;
                }
            default:
                addToken(length - lastOffset, token);
                break;
        }

        return token;
    }

    private boolean doKeyword(Segment line, int i, char c) {
        int i1 = i + 1;

        int len = i - lastKeyword;
        byte id = keywords.lookup(line, lastKeyword, len);
        if (id != Token.NULL) {
            if (lastKeyword != lastOffset) {
                addToken(lastKeyword - lastOffset, Token.NULL);
            }
            addToken(len, id);
            lastOffset = i;
        }
        lastKeyword = i1;
        return false;
    }
}
