package com.ops.zen.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author xiaoyingnan
 */
public class StringUtils {

    private static final int PAD_LIMIT = 8192;

    /**
     * A String for a space character.
     *
     * @since 3.2
     */
    public static final String SPACE = " ";

    /**
     * The empty String {@code ""}.
     *
     * @since 2.0
     */
    public static final String EMPTY = "";

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static String concate(String[] ay, String concate) {
        if (ay == null)
            return null;
        StringBuilder sb = new StringBuilder();
        for (String a : ay) {
            if (sb.length() == 0) {
                sb.append(a);
            } else {
                sb.append(concate).append(a);
            }
        }
        return sb.toString();
    }

    public static String concate(List<String> list, String concate) {
        return collConcate((Collection) list, concate);
    }

    public static String collConcate(Collection<String> list, String concate) {
        String[] array = list.toArray(new String[0]);
        return concate(array, concate);
    }

    public static String bytes2String(byte[] bs, String charset) {
        try {
            return new String(bs, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException " + charset);
        }
    }

    public static String bytes2UTF8String(byte[] bs) {
        String charset = "UTF-8";
        try {
            return new String(bs, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException " + charset);
        }
    }

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int sz = str.length();
        if (sz == 0) {
            return false;
        }
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static String firstCharUppercase(String str) {
        if (isBlank(str))
            return str;
        char[] charArray = str.toCharArray();
        if (charArray[0] >= 'a' && charArray[0] <= 'z') {
            charArray[0] -= 32;
        }
        return new String(charArray);
    }

    public static String firstCharLowercase(String str) {
        if (isBlank(str))
            return str;
        char[] charArray = str.toCharArray();
        if (charArray[0] >= 'A' && charArray[0] <= 'Z') {
            charArray[0] += 32;
        }
        return new String(charArray);
    }

    /**
     * <p>
     * Capitalizes a String changing the first letter to title case as per
     * {@link Character#toTitleCase(char)}. No other letters are changed.
     * </p>
     *
     * <p>
     * For a word based algorithm, see
     * {@link org.apache.commons.lang3.text.WordUtils#capitalize(String)}. A
     * {@code null} input String returns {@code null}.
     * </p>
     *
     * <pre>
     * StringUtils.capitalize(null)  = null
     * StringUtils.capitalize("")    = ""
     * StringUtils.capitalize("cat") = "Cat"
     * StringUtils.capitalize("cAt") = "CAt"
     * </pre>
     *
     * @param str the String to capitalize, may be null
     * @return the capitalized String, {@code null} if null String input
     * @see org.apache.commons.lang3.text.WordUtils#capitalize(String)
     * @see #uncapitalize(String)
     * @since 2.0
     */
    public static String capitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        final char firstChar = str.charAt(0);
        if (Character.isTitleCase(firstChar)) {
            // already capitalized
            return str;
        }

        return new StringBuilder(strLen).append(Character.toTitleCase(firstChar)).append(str.substring(1)).toString();
    }

    /**
     * <p>
     * Uncapitalizes a String changing the first letter to title case as per
     * {@link Character#toLowerCase(char)}. No other letters are changed.
     * </p>
     *
     * <p>
     * For a word based algorithm, see
     * {@link org.apache.commons.lang3.text.WordUtils#uncapitalize(String)}. A
     * {@code null} input String returns {@code null}.
     * </p>
     *
     * <pre>
     * StringUtils.uncapitalize(null)  = null
     * StringUtils.uncapitalize("")    = ""
     * StringUtils.uncapitalize("Cat") = "cat"
     * StringUtils.uncapitalize("CAT") = "cAT"
     * </pre>
     *
     * @param str the String to uncapitalize, may be null
     * @return the uncapitalized String, {@code null} if null String input
     * @see org.apache.commons.lang3.text.WordUtils#uncapitalize(String)
     * @see #capitalize(String)
     * @since 2.0
     */
    public static String uncapitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        final char firstChar = str.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            // already uncapitalized
            return str;
        }

        return new StringBuilder(strLen).append(Character.toLowerCase(firstChar)).append(str.substring(1)).toString();
    }

    /**
     * <p>
     * Left pad a String with a specified String.
     * </p>
     *
     * <p>
     * Pad to a size of {@code size}.
     * </p>
     *
     * <pre>
     * StringUtils.leftPad(null, *, *)      = null
     * StringUtils.leftPad("", 3, "z")      = "zzz"
     * StringUtils.leftPad("bat", 3, "yz")  = "bat"
     * StringUtils.leftPad("bat", 5, "yz")  = "yzbat"
     * StringUtils.leftPad("bat", 8, "yz")  = "yzyzybat"
     * StringUtils.leftPad("bat", 1, "yz")  = "bat"
     * StringUtils.leftPad("bat", -1, "yz") = "bat"
     * StringUtils.leftPad("bat", 5, null)  = "  bat"
     * StringUtils.leftPad("bat", 5, "")    = "  bat"
     * </pre>
     *
     * @param str    the String to pad out, may be null
     * @param size   the size to pad to
     * @param padStr the String to pad with, null or empty treated as single space
     * @return left padded String or original String if no padding is necessary,
     * {@code null} if null String input
     */
    public static String leftPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }

    /**
     * <p>
     * Left pad a String with a specified character.
     * </p>
     *
     * <p>
     * Pad to a size of {@code size}.
     * </p>
     *
     * <pre>
     * StringUtils.leftPad(null, *, *)     = null
     * StringUtils.leftPad("", 3, 'z')     = "zzz"
     * StringUtils.leftPad("bat", 3, 'z')  = "bat"
     * StringUtils.leftPad("bat", 5, 'z')  = "zzbat"
     * StringUtils.leftPad("bat", 1, 'z')  = "bat"
     * StringUtils.leftPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str     the String to pad out, may be null
     * @param size    the size to pad to
     * @param padChar the character to pad with
     * @return left padded String or original String if no padding is necessary,
     * {@code null} if null String input
     * @since 2.0
     */
    public static String leftPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return repeat(padChar, pads).concat(str);
    }

    /**
     * <p>
     * Repeat a String {@code repeat} times to form a new String.
     * </p>
     *
     * <pre>
     * StringUtils.repeat(null, 2) = null
     * StringUtils.repeat("", 0)   = ""
     * StringUtils.repeat("", 2)   = ""
     * StringUtils.repeat("a", 3)  = "aaa"
     * StringUtils.repeat("ab", 2) = "abab"
     * StringUtils.repeat("a", -2) = ""
     * </pre>
     *
     * @param str    the String to repeat, may be null
     * @param repeat number of times to repeat str, negative treated as zero
     * @return a new String consisting of the original String repeated,
     * {@code null} if null String input
     */
    public static String repeat(final String str, final int repeat) {
        // Performance tuned for 2.0 (JDK1.4)

        if (str == null) {
            return null;
        }
        if (repeat <= 0) {
            return EMPTY;
        }
        final int inputLength = str.length();
        if (repeat == 1 || inputLength == 0) {
            return str;
        }
        if (inputLength == 1 && repeat <= PAD_LIMIT) {
            return repeat(str.charAt(0), repeat);
        }

        final int outputLength = inputLength * repeat;
        switch (inputLength) {
            case 1:
                return repeat(str.charAt(0), repeat);
            case 2:
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char[] output2 = new char[outputLength];
                for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
                    output2[i] = ch0;
                    output2[i + 1] = ch1;
                }
                return new String(output2);
            default:
                final StringBuilder buf = new StringBuilder(outputLength);
                for (int i = 0; i < repeat; i++) {
                    buf.append(str);
                }
                return buf.toString();
        }
    }

    /**
     * <p>
     * Repeat a String {@code repeat} times to form a new String, with a String
     * separator injected each time.
     * </p>
     *
     * <pre>
     * StringUtils.repeat(null, null, 2) = null
     * StringUtils.repeat(null, "x", 2)  = null
     * StringUtils.repeat("", null, 0)   = ""
     * StringUtils.repeat("", "", 2)     = ""
     * StringUtils.repeat("", "x", 3)    = "xxx"
     * StringUtils.repeat("?", ", ", 3)  = "?, ?, ?"
     * </pre>
     *
     * @param str       the String to repeat, may be null
     * @param separator the String to inject, may be null
     * @param repeat    number of times to repeat str, negative treated as zero
     * @return a new String consisting of the original String repeated,
     * {@code null} if null String input
     * @since 2.5
     */
    public static String repeat(final String str, final String separator, final int repeat) {
        if (str == null || separator == null) {
            return repeat(str, repeat);
        }
        // given that repeat(String, int) is quite optimized, better to rely on
        // it than try and splice this into it
        final String result = repeat(str + separator, repeat);
        return removeEnd(result, separator);
    }

    /**
     * <p>
     * Returns padding using the specified delimiter repeated to a given length.
     * </p>
     *
     * <pre>
     * StringUtils.repeat('e', 0)  = ""
     * StringUtils.repeat('e', 3)  = "eee"
     * StringUtils.repeat('e', -2) = ""
     * </pre>
     *
     * <p>
     * Note: this method doesn't not support padding with
     * <a href="http://www.unicode.org/glossary/#supplementary_character">
     * Unicode Supplementary Characters</a> as they require a pair of
     * {@code char}s to be represented. If you are needing to support full I18N
     * of your applications consider using {@link #repeat(String, int)} instead.
     * </p>
     *
     * @param ch     character to repeat
     * @param repeat number of times to repeat char, negative treated as zero
     * @return String with repeated character
     * @see #repeat(String, int)
     */
    public static String repeat(final char ch, final int repeat) {
        final char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    /**
     * <p>
     * Removes a substring only if it is at the end of a source string,
     * otherwise returns the source string.
     * </p>
     *
     * <p>
     * A {@code null} source string will return {@code null}. An empty ("")
     * source string will return the empty string. A {@code null} search string
     * will return the source string.
     * </p>
     *
     * <pre>
     * StringUtils.removeEnd(null, *)      = null
     * StringUtils.removeEnd("", *)        = ""
     * StringUtils.removeEnd(*, null)      = *
     * StringUtils.removeEnd("www.domain.com", ".com.")  = "www.domain.com"
     * StringUtils.removeEnd("www.domain.com", ".com")   = "www.domain"
     * StringUtils.removeEnd("www.domain.com", "domain") = "www.domain.com"
     * StringUtils.removeEnd("abc", "")    = "abc"
     * </pre>
     *
     * @param str    the source String to search, may be null
     * @param remove the String to search for and remove, may be null
     * @return the substring with the string removed if found, {@code null} if
     * null String input
     * @since 2.1
     */
    public static String removeEnd(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.endsWith(remove)) {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
    }

    /**
     * 数据库字段转javabean字段
     */
    public static String dbField2Camel(String fieldName, boolean firstCharUpper) {
        if (isBlank(fieldName))
            return fieldName;
        fieldName = fieldName.toLowerCase();
        if (fieldName.indexOf("_") >= 0) {
            String[] split = fieldName.split("_");
            StringBuilder sbsplit = new StringBuilder();
            for (String s : split) {
                if (s.length() == 0) {
                    continue;
                }
                if (sbsplit.length() == 0) {
                    sbsplit.append(s);
                } else {
                    sbsplit.append(StringUtils.firstCharUppercase(s));
                }
            }
            fieldName = sbsplit.toString();
        }
        if (firstCharUpper) {
            fieldName = StringUtils.firstCharUppercase(fieldName);
        }
        return fieldName;
    }

    public static String trim(String v) {
        if (isNotEmpty(v)) {
            return v.trim();
        }
        return v;
    }

    public static boolean hasText(String pattern1) {
        return !isNotBlank(pattern1);
    }


    private static final String[] EMPTY_STRING_ARRAY = {};


    /**
     * Tokenize the given {@code String} into a {@code String} array via a
     * {@link StringTokenizer}.
     * <p>The given {@code delimiters} string can consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using {@link #delimitedListToStringArray}.
     *
     * @param str               the {@code String} to tokenize (potentially {@code null} or empty)
     * @param delimiters        the delimiter characters, assembled as a {@code String}
     *                          (each of the characters is individually considered as a delimiter)
     * @param trimTokens        trim the tokens via {@link String#trim()}
     * @param ignoreEmptyTokens omit empty tokens from the result array
     *                          (only applies to tokens that are empty after trimming; StringTokenizer
     *                          will not consider subsequent delimiters as token in the first place).
     * @return an array of the tokens
     * @see StringTokenizer
     * @see String#trim()
     * @see #delimitedListToStringArray
     */
    public static String[] tokenizeToStringArray(
            String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

        if (str == null) {
            return EMPTY_STRING_ARRAY;
        }

        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }

    private static String[] toStringArray(List<String> collection) {
        return (!isEmpty(collection) ? collection.toArray(EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY);
    }

    /**
     * Return {@code true} if the supplied Collection is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static String bytes2OriginString(byte[] output) {
        if (output == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : output) {
            if (sb.length() != 0) {
                sb.append(',');
            }
            sb.append(Byte.toString(b));
        }
        return sb.insert(0, '[').append(']').toString();
    }

    public static byte[] string2Bytes(String str) {
        if (str != null) {
            return str.getBytes(StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * TODO 性能？
     * 截断字符串
     *
     * @param str
     * @param size
     * @param unit   默认为Byte
     * @param suffix 截断的字符串后追加的后缀
     * @return
     */
    public static String substring(String str, int size, SizeUnit unit, String suffix) {
        if (isEmpty(str)) {
            return str;
        }
        if (unit == null) {
            unit = SizeUnit.BYTE;
        }
        int byteSize = SizeUnit.getByteSize(size, unit);
        if (str.length() < byteSize) {
            return str;
        } else {
            if (isNotEmpty(suffix))
                return str.substring(0, byteSize).concat(suffix);
            else {
                return str.substring(0, byteSize);
            }
        }
    }

    /**
     * @param bytes
     * @param size
     * @param unit
     * @param suffix
     * @return
     */
    public static byte[] subbytes(byte[] bytes, int size, SizeUnit unit, String suffix) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        if (unit == null) {
            unit = SizeUnit.BYTE;
        }
        int byteSize = SizeUnit.getByteSize(size, unit);
        int suffixLength = suffix != null ? suffix.getBytes(StandardCharsets.UTF_8).length : 0;
        byteSize += suffixLength;// 让出suffix length的数量，以便让后缀体现在新的字节数组中
        if (bytes.length > byteSize) {
            bytes = Arrays.copyOfRange(bytes, 0, byteSize);
            // 处理后缀
            if (suffixLength > 0) {
                byte[] suffixBytes = suffix.getBytes(StandardCharsets.UTF_8);
                int startIdx = bytes.length - suffixLength;
                for (int i = 0; i < suffixLength; i++) {
                    bytes[startIdx + i] = suffixBytes[i];
                }
            }
        }
        return bytes;
    }

    public static String substring(byte[] bytes, int size, SizeUnit unit, String suffix) {
        return bytes2UTF8String(subbytes(bytes, size, unit, suffix));
    }

    /**
     * str左侧删除所有的removed
     *
     * @param str
     * @param removed
     * @return
     */
    public static String leftRemoveAll(String str, String removed) {
        return str.replaceAll("^(" + removed + ")+", "");
    }

    public static String rightRemoveAll(String str, String removed) {
        return str.replaceAll("(" + removed + ")+$", "");
    }

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{}
     * \\需要第一个替换，否则replace方法替换时会有逻辑bug
     */
    public static String makeQueryStringAllRegExp(String str) {
        if (isBlank(str)) {
            return str;
        }
        return str.replaceAll("\\\\", "\\\\\\").replaceAll("\\*", "\\\\*")
                .replaceAll("\\+", "\\\\+").replaceAll("\\|", "\\\\|")
                .replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}")
                .replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)")
                .replaceAll("\\^", "\\\\^").replaceAll("\\$", "\\\\$")
                .replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")
                .replaceAll("\\?", "\\\\?").replaceAll("\\,", "\\\\,")
                .replaceAll("\\.", "\\\\.").replaceAll("\\&", "\\\\&");
    }

    public static List<String> readLines(String metricRawText) throws IOException {
        if (metricRawText == null) {
            return null;
        }
        BufferedReader reader = new BufferedReader(new StringReader(metricRawText));
        String line = null;
        List<String> lines = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    public enum SizeUnit {
        BYTE, KB, MB, Mb, Kb;

        /**
         * 将size根据unit换算成单位是byte的size
         *
         * @param size
         * @param unit
         * @return
         */
        public static int getByteSize(int size, SizeUnit unit) {
            int byteSize = size;
            switch (unit) {
                case BYTE:
                    break;
                case KB:
                    byteSize *= 1024;
                    break;
                case MB:
                    byteSize *= 1024 * 1024;
                    break;
                case Mb:
                    byteSize *= 1024 * 128;
                    break;
                case Kb:
                    byteSize *= 128;
                    break;
            }
            return byteSize;
        }

    }

    /**
     * 如果originString为null返回replacedString否则返回originString
     *
     * @param originString
     * @param replacedString
     * @return
     */
    public static String ifNullReplaced(String originString, String replacedString) {
        return originString == null ? replacedString : originString;
    }
}
