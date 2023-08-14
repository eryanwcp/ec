/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils;
import com.eryansky.common.web.utils.DownloadUtils;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Pinyin4js {

    private final static Logger logger = LoggerFactory.getLogger(Pinyin4js.class);

    /**
     * 常见特殊字符过滤
     *
     * @param str
     * @return
     */
    public static String filterSpecialCharacter(String str) {
        String regEx = "[`~!@#$%^&*()+=|{}:;\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？']";
        str = Pattern.compile(regEx).matcher(str).replaceAll("").trim();
        return str;
    }


    /**
     * 字符串集合转换字符串(逗号分隔)
     * @author wyh
     * @param stringSet
     * @return
     */
    public static String makeStringByStringSet(Set<String> stringSet){
        StringBuilder str = new StringBuilder();
        int i=0;
        for(String s : stringSet){
            if(i == stringSet.size() - 1){
                str.append(s);
            }else{
                str.append(s).append(",");
            }
            i++;
        }
        return str.toString().toLowerCase();
    }

    /**
     * 获取拼音集合
     * @author wyh
     * @param src
     * @return Set<String>
     */
    public static Set<String> getPinyin(String src){
        if(src!=null && !src.trim().equalsIgnoreCase("")){
            char[] srcChar ;
            srcChar=src.toCharArray();
            //汉语拼音格式输出类
            HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();

//输出设置，大小写，音标方式等
            hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

            String[][] temp = new String[src.length()][];
            for(int i=0;i<srcChar.length;i++){
                char c = srcChar[i];
                //是中文或者a-z或者A-Z转换拼音(我的需求，是保留中文或者a-z或者A-Z)
                if(String.valueOf(c).matches("[\\u4E00-\\u9FA5]+")){
                    try{
                        temp[i] = PinyinHelper.toHanyuPinyinStringArray(srcChar[i], hanYuPinOutputFormat);
                    }catch(BadHanyuPinyinOutputFormatCombination e) {
                        logger.error(e.getMessage(),e);
                    }
                }else if(((int)c>=65 && (int)c<=90) || ((int)c>=97 && (int)c<=122)){
                    temp[i] = new String[]{String.valueOf(srcChar[i])};
                }else{
                    temp[i] = new String[]{""};
                }
            }
            String[] pingyinArray = exchange(temp);
            Set<String> pinyinSet = new HashSet<>();
            pinyinSet.addAll(Arrays.asList(pingyinArray));
            return pinyinSet;
        }
        return null;
    }

    /**
     * 递归
     * @author wyh
     * @param strJaggedArray
     * @return
     */
    public static String[] exchange(String[][] strJaggedArray){
        String[][] temp = doExchange(strJaggedArray);
        return temp[0];
    }

    /**
     * 递归
     * @author wyh
     * @param strJaggedArray
     * @return
     */
    private static String[][] doExchange(String[][] strJaggedArray){
        int len = strJaggedArray.length;
        if(len >= 2){
            int len1 = strJaggedArray[0].length;
            int len2 = strJaggedArray[1].length;
            int newlen = len1*len2;
            String[] temp = new String[newlen];
            int Index = 0;
            for(int i=0;i<len1;i++){
                for(int j=0;j<len2;j++){
                    temp[Index] = strJaggedArray[0][i] + strJaggedArray[1][j];
                    Index ++;
                }
            }
            String[][] newArray = new String[len-1][];
            System.arraycopy(strJaggedArray, 2, newArray, 1, len - 2);
            newArray[0] = temp;
            return doExchange(newArray);
        }else{
            return strJaggedArray;
        }
    }

    public static String getPinYinHeadChar(String str) {
        StringBuilder convert = new StringBuilder();
        for (int j = 0; j < str.length(); j++) {
            char word = str.charAt(j);
            // 提取汉字的首字母
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
            if (pinyinArray != null) {
                convert.append(pinyinArray[0].charAt(0));
            } else {
                convert.append(word);
            }
        }
        return convert.toString();
    }

        /**
         * @param args
         */
    public static void main(String[] args) {
        String str = "单田芳";
        System.out.println(makeStringByStringSet(getPinyin(str)));

    }

}
