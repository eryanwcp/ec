package com.eryansky.utils;


import com.eryansky.j2cache.util.AntPathMatcher;
import com.eryansky.j2cache.util.PatternMatcher;

public class PatternMatcherTest {

    public static void main(String[] args) {
        PatternMatcher matcher = new AntPathMatcher();
        System.out.println(matcher.matches("abc_*","abc_sdfsd"));
        System.out.println(matcher.matches("asf_*","dsffsd"));
        System.out.println(matcher.matches("default","default"));
    }
}