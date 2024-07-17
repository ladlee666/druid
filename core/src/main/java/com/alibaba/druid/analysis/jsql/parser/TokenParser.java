package com.alibaba.druid.analysis.jsql.parser;

/**
 * @author LENOVO
 * @date 2024/5/31 18:04
 */
public class TokenParser {

    private final String openToken;

    private final String closeToken;

    private final TokenHandler tokenHandler;

    public TokenParser(String openToken, String closeToken, TokenHandler tokenHandler) {
        this.openToken = openToken;
        this.closeToken = closeToken;
        this.tokenHandler = tokenHandler;
    }

    public String parse(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        int start = text.indexOf(openToken);
        if (start == -1) {
            return text;
        }
        char[] src = text.toCharArray();
        int offset = 0;
        final StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        do {
            // 搜索到假的#{ ，  \#{ 转化成 #{
            if (start > 0 && src[start - 1] == '\\') {
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
            }
            // 搜索到真实的 #{
            else {
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();
                // 开始搜索 }
                int end = text.indexOf(closeToken, offset);
                while (end > -1) {
                    // 搜索到假的 } ，   \} 转化成 }
                    if (end > offset && src[end - 1] == '\\') {
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        //继续向右搜索 }
                        end = text.indexOf(closeToken, offset);
                    }
                    // 搜索到真实的 }
                    else {
                        expression.append(src, offset, end - offset);
                        break;
                    }
                }
                // 没有搜索到真实的右括号 }
                if (end == -1) {
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                }
                // 搜索到真实的右括号}
                else {
                    builder.append(tokenHandler.handleToken(expression.toString().trim()));
                    offset = end + closeToken.length();
                }
            }
            start = text.indexOf(openToken, offset);
        } while (start > -1);
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }

}
