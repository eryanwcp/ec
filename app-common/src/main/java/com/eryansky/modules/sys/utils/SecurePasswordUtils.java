package com.eryansky.modules.sys.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 安全随机密码生成工具类
 * 满足：至少4位，必须包含数字、小写字母、大写字母、特殊符号(!@#$%&)
 */
public class SecurePasswordUtils {

    // 定义密码字符集（严格限定特殊符号范围）
    private static final String DIGITS = "23456789"; // 数字 去除0 1
    private static final String LOWER_CASE = "abcdefghjklmnpqrstuvwxyz"; // 小写字母
    private static final String UPPER_CASE = "ABCDEFGHJKLMNPQRSTUVWXYZ"; // 大写字母 去除 O I
    private static final String SPECIAL_CHARS = "!@#$%&"; // 指定特殊符号 去除部分特殊字符
    private static final String ALL_CHARS = DIGITS + LOWER_CASE + UPPER_CASE + SPECIAL_CHARS;

    // 使用安全随机数生成器（比Random更安全）
    private static final SecureRandom random = new SecureRandom();

    /**
     * 生成随机安全密码
     * @param length 密码长度（必须4位以上，否则抛IllegalArgumentException）
     * @return 符合规则的随机密码
     */
    public static String generatePassword(int length) {
        // 1. 校验长度合法性
        if (length < 4) {
            throw new IllegalArgumentException("密码长度必须至少4位以上");
        }

        // 2. 初始化密码字符列表（先确保4类字符各至少1个）
        List<Character> passwordChars = new ArrayList<>();
        // 添加1个随机数字
        passwordChars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        // 添加1个随机小写字母
        passwordChars.add(LOWER_CASE.charAt(random.nextInt(LOWER_CASE.length())));
        // 添加1个随机大写字母
        passwordChars.add(UPPER_CASE.charAt(random.nextInt(UPPER_CASE.length())));
        // 添加1个随机特殊符号
        passwordChars.add(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // 3. 填充剩余长度的随机字符（从所有字符集中选）
        for (int i = 4; i < length; i++) {
            passwordChars.add(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // 4. 打乱字符顺序（避免前4位固定是数字、小写、大写、特殊符号）
        Collections.shuffle(passwordChars);

        // 5. 将字符列表拼接为字符串
        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }

    /**
     * 10位密码生成器
     * @return
     */
    public static String generateCommonLangPassword() {
        String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(2);
        String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
        String totalChars = RandomStringUtils.randomAlphanumeric(2);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
                .concat(numbers)
                .concat(specialChar)
                .concat(totalChars);
        List<Character> pwdChars = combinedChars.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        String password = pwdChars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        return password;
    }

    // 测试示例
    public static void main(String[] args) {
        System.out.println("随机密码：" + generateCommonLangPassword());
        System.out.println("8位随机密码：" + generatePassword(8));
        System.out.println("16位随机密码：" + generatePassword(16));
        System.out.println("16位随机密码：" + generatePassword(16));
    }
}