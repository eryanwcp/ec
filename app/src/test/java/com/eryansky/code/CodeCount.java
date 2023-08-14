package com.eryansky.code;

import java.io.*;


/**
 * 简单的代码行数统计
 */
public class CodeCount {

    static long normalLine = 0;
    static long commentLine = 0;
    static long whiteLine = 0;

    public static void main(String[] args) {
        String rootDir = SvnKitUtils.rootDir ;
        String[] modules = SvnKitUtils.modules;

        for(int i =0;i<modules.length;i++){
            count(rootDir,modules[i]);
        }

        p("注释的代码行数:" + commentLine);
        p("空白的代码行数:" + whiteLine);
        p("有效的代码行数:" + normalLine);
        p("代码行数:" + (commentLine +normalLine));
        p("注释率:" + ((float)commentLine/normalLine * 100) + "%");
        long total = commentLine + normalLine + normalLine;
        p("空行率:" + ((float)whiteLine/total * 100) + "%");
        p("总行数:" + total);
    }
    public static void count(String rootDir,String module){
        File commonFiles = new File(rootDir + "common\\src\\main\\java\\");
        File fastweixinFiles = new File(rootDir + "fastweixin\\src\\main\\java\\");
        File j2cacheFiles = new File(rootDir + "j2cache\\src\\main\\java\\");
        File j2cacheSpringBootStarterFiles = new File(rootDir + "j2cache-spring-boot-starter\\src\\main\\java\\");
        File javaFile = new File(rootDir + "app-common\\src\\main\\java\\com\\eryansky\\modules\\"+module);
        File xmlFile = new File(rootDir + "app-common\\src\\main\\resources\\mappings\\modules\\"+module);
        File xmlFile2 = new File(rootDir + "app-common\\src\\main\\resources\\mappings\\extend");
        File jspFile = new File(rootDir + "app-common\\src\\main\\webapp\\WEB-INF\\views\\modules\\"+module);
        File jspFile2 = new File(rootDir + "app-common\\src\\main\\webapp\\WEB-INF\\views\\mobile\\modules\\"+module);
        File jsFile = new File(rootDir + "app-common\\src\\main\\webapp\\static\\app\\modules\\"+module);
        File jsFile2 = new File(rootDir + "app-common\\src\\main\\webapp\\static\\app\\mobile\\modules\\"+module);
        File cssFile = new File(rootDir + "app-common\\src\\main\\webapp\\static\\app\\modules\\"+module);
        File cssFile2 = new File(rootDir + "app-common\\src\\main\\webapp\\static\\app\\mobile\\modules\\"+module);
        r(commonFiles, ".java");
        r(fastweixinFiles, ".java");
        r(j2cacheFiles, ".java");
        r(j2cacheSpringBootStarterFiles, ".java");

        r(javaFile, ".java");
        r(xmlFile, ".xml");
        r(xmlFile2, ".xml");
        r(jspFile, ".jsp");
        r(jspFile2, ".jsp");
        r(jsFile, ".js");
        r(jsFile2, ".js");
        r(cssFile, ".css");
        r(cssFile2, ".css");
    }
    /**
     * 递归遍历
     *
     * @param dir    目录
     * @param suffix 匹配后缀
     */
    public static void r(File dir, String suffix) {
        File[] files = dir.listFiles();
        if(files != null){
            for (File eachfile : files) {
                if (eachfile.isFile()) {
                    if (eachfile.getName().matches(".*\\" + suffix)) {
                        countcode(eachfile);
                    }
                } else {
                    r(eachfile, suffix);
                }
            }
        }

    }


    public static void p(Object obj) {
        System.out.println(obj);
    }

    public static void countcode(File f) {
        BufferedReader br = null;
        boolean bln = false;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = "";
            try {
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.matches("^[\\s&&[^\\n]]*$")) {
                        whiteLine += 1;
                    } else if (line.startsWith("/*") && !line.equals("*/")) {
                        commentLine += 1;
                        bln = true;
                    } else if (bln == true) {
                        commentLine += 1;
                        if (line.endsWith("*/")) {
                            bln = false;
                        }
                    } else if (line.startsWith("/*") && line.endsWith("*/")) {
                        commentLine += 1;
                    } else if (line.startsWith("//")) {
                        commentLine += 1;
                    } else {
                        normalLine += 1;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                    br = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }


}