package com.eryansky.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.pinyin4j.PinyinHelper;

public class ChineseUtils {

    /**
     * 判断中文字符
     *
     * @param c
     * @return
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
                ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
                ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS ||
                ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    /**
     * 包含中文字符
     *
     * @param str
     * @return
     */
    public static boolean containsChinese(String str) {
        char[] ch = str.toCharArray();
        for (char c : ch) {
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否中文（非标点符号）
     *
     * @param c
     * @return
     */
    public static boolean isChineseWord(char c) {
        char[] ch = {c};
        Pattern pattern = Pattern.compile("([\u4E00-\uFA29]|[\uE7C7-\uE7F3])");
        Matcher matcher = pattern.matcher(new String(ch));
        return matcher.find();
    }

    /**
     * 包含汉字（非标点符号）
     *
     * @param name
     * @return
     */
    public static boolean containsChineseWords(String name) {
        Pattern pattern = Pattern.compile("^.*([\u4E00-\uFA29]|[\uE7C7-\uE7F3])+.*$");
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }

    /**
     * 转换成拼音字母缩写
     *
     * @param str
     * @return abbr
     */
    public static String toPinyinAbbr(String str) {
        String abbr = "";
        for (char c : str.toCharArray()) {
            if (ChineseUtils.isChineseWord(c)) {
                abbr += PinyinHelper.toHanyuPinyinStringArray(c)[0].substring(0, 1).toUpperCase();
            }
        }
        return abbr;
    }


    public static void main(String[] args) {
        System.out.println(isChinese('め'));// false
        System.out.println(isChineseWord('我'));// true
        System.out.println(containsChinese("a我b"));// true
        System.out.println(containsChineseWords("a，b")); // false
    }
}