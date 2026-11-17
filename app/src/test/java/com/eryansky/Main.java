package com.eryansky;

import com.eryansky.common.utils.http.HttpCompoents;
import com.eryansky.common.utils.io.FileUtils;
import com.eryansky.common.utils.io.IoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Eryan
 * @date 2020-06-17 
 */
public class Main {
    public static void main(String[] args) throws Exception {
        FileUtils.createDirectory("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosanssc/v37");
        FileUtils.createDirectory("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosanssc/v40");
        FileUtils.createDirectory("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosanshk/v32");
        FileUtils.createDirectory("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosanskr/v36");
        FileUtils.createDirectory("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosansjp/v53");
        FileUtils.createDirectory("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosanstc/v39");
        FileUtils.createDirectory("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosanshk/v35");
        FileUtils.createDirectory("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosansnewtailue/v24");
        FileUtils.createDirectory("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosansyi/v22");
        for(int i=1;i<120;i++){
            String url = "https://fonts.gstatic.com/s/notosanssc/v37/k3kCo84MPvpLmixcA63oeAL7Iqp5IZJF9bmaG9_FnYkldv7JjxkkgFsFSSOPMOkySAZ73y9ViAt3acb8NexQ2w."+i+".woff2";
            int finalI = i;
            HttpCompoents.getInstance().get(url, null, httpResponse -> {
                try {
                    System.out.println(url + " " + httpResponse.getCode());
                    if (httpResponse.getCode() != 200) {
                        return false;
                    }
                    IoUtils.write(httpResponse.getEntity().getContent().readAllBytes(), new FileOutputStream(new File("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosanssc/v37/k3kCo84MPvpLmixcA63oeAL7Iqp5IZJF9bmaG9_FnYkldv7JjxkkgFsFSSOPMOkySAZ73y9ViAt3acb8NexQ2w." + finalI + ".woff2")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }


        for(int i=1;i<120;i++){
            String url = "https://fonts.gstatic.com/s/notosanshk/v32/nKKF-GM_FYFRJvXzVXaAPe97P1KHynJFP716qHB--oD7kYrUzT7-NvA3pTohjc3XVtNXX8A7gG1LO2KAPAw."+i+".woff2";
            int finalI = i;
            HttpCompoents.getInstance().get(url, null, httpResponse -> {
                try {
                    System.out.println(url + " " + httpResponse.getCode());
                    if (httpResponse.getCode() != 200) {
                        return false;
                    }
                    IoUtils.write(httpResponse.getEntity().getContent().readAllBytes(),new FileOutputStream(new File("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosanshk/v32/nKKF-GM_FYFRJvXzVXaAPe97P1KHynJFP716qHB--oD7kYrUzT7-NvA3pTohjc3XVtNXX8A7gG1LO2KAPAw."+ finalI +".woff2")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }

        for(int i=1;i<120;i++){
            String url = "https://fonts.gstatic.com/s/notosanskr/v36/PbyxFmXiEBPT4ITbgNA5Cgms3VYcOA-vvnIzzuoyeLGC5nwuDo-KBTUm6CryotyJROlrnQ."+i+".woff2";
            int finalI = i;
            HttpCompoents.getInstance().get(url, null, httpResponse -> {
                try {
                    System.out.println(url + " " + httpResponse.getCode());
                    if (httpResponse.getCode() != 200) {
                        return false;
                    }
                    IoUtils.write(httpResponse.getEntity().getContent().readAllBytes(),new FileOutputStream(new File("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosanskr/v36/PbyxFmXiEBPT4ITbgNA5Cgms3VYcOA-vvnIzzuoyeLGC5nwuDo-KBTUm6CryotyJROlrnQ."+ finalI +".woff2")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }


        for(int i=1;i<120;i++){
            String url = "https://fonts.gstatic.com/s/notosansjp/v53/-F6jfjtqLzI2JPCgQBnw7HFyzSD-AsregP8VFBEj756wwr4v0qHnANADNsISRDl2PRkiiWsg."+i+".woff2";
            int finalI = i;
            HttpCompoents.getInstance().get(url, null, httpResponse -> {
                try {
                    System.out.println(url + " " + httpResponse.getCode());
                    if (httpResponse.getCode() != 200) {
                        return false;
                    }
                    IoUtils.write(httpResponse.getEntity().getContent().readAllBytes(),new FileOutputStream(new File("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosansjp/v53/-F6jfjtqLzI2JPCgQBnw7HFyzSD-AsregP8VFBEj756wwr4v0qHnANADNsISRDl2PRkiiWsg."+ finalI +".woff2")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }

        for(int i=1;i<120;i++){
            String url = "https://fonts.gstatic.com/s/notosanssc/v40/k3kCo84MPvpLmixcA63oeAL7Iqp5IZJF9bmaG9_FnYkldv7JjxkkgFsFSSOPMOkySAZ73y9ViAt3acb8NexQ2w."+i+".woff2";
            int finalI = i;
            HttpCompoents.getInstance().get(url, null, httpResponse -> {
                try {
                    System.out.println(url + " " + httpResponse.getCode());
                    if (httpResponse.getCode() != 200) {
                        return false;
                    }
                    IoUtils.write(httpResponse.getEntity().getContent().readAllBytes(),new FileOutputStream(new File("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosanssc/v40/k3kCo84MPvpLmixcA63oeAL7Iqp5IZJF9bmaG9_FnYkldv7JjxkkgFsFSSOPMOkySAZ73y9ViAt3acb8NexQ2w."+ finalI +".woff2")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }

        for(int i=1;i<120;i++){
            String url = "https://fonts.gstatic.com/s/notosanstc/v39/-nFuOG829Oofr2wohFbTp9ifNAn722rq0MXz76Cy_C8mrWSt1KeqzFVoizG-KdWhyhvKuGOf8EUcrq3YKp7nxxk."+i+".woff2";
            int finalI = i;
            HttpCompoents.getInstance().get(url, null, httpResponse -> {
                try {
                    System.out.println(url + " " + httpResponse.getCode());
                    if (httpResponse.getCode() != 200) {
                        return false;
                    }
                    IoUtils.write(httpResponse.getEntity().getContent().readAllBytes(),new FileOutputStream(new File("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosanstc/v39/-nFuOG829Oofr2wohFbTp9ifNAn722rq0MXz76Cy_C8mrWSt1KeqzFVoizG-KdWhyhvKuGOf8EUcrq3YKp7nxxk."+ finalI +".woff2")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }

        for(int i=1;i<120;i++){
            String url = "https://fonts.gstatic.com/s/notosanshk/v35/nKKF-GM_FYFRJvXzVXaAPe97P1KHynJFP716qHB--oD7kYrUzT7-NvA3pTohjc3XVtNXX8A7gG1LO2KAPAw."+i+".woff2";
            int finalI = i;
            HttpCompoents.getInstance().get(url, null, httpResponse -> {
                try {
                    System.out.println(url + " " + httpResponse.getCode());
                    if (httpResponse.getCode() != 200) {
                        return false;
                    }
                    IoUtils.write(httpResponse.getEntity().getContent().readAllBytes(),new FileOutputStream(new File("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosanshk/v35/nKKF-GM_FYFRJvXzVXaAPe97P1KHynJFP716qHB--oD7kYrUzT7-NvA3pTohjc3XVtNXX8A7gG1LO2KAPAw."+ finalI +".woff2")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }

        for(int i=1;i<120;i++){
            String url = "https://fonts.gstatic.com/s/notosansnewtailue/v24/H4cKBW-Pl9DZ0Xe_nHUapt7PovLXAhAnY7wqaLy-OJgU3p_pdeXAYUPghFPKzeY."+i+".woff2";
            int finalI = i;
            HttpCompoents.getInstance().get(url, null, httpResponse -> {
                try {
                    System.out.println(url + " " + httpResponse.getCode());
                    if (httpResponse.getCode() != 200) {
                        return false;
                    }
                    IoUtils.write(httpResponse.getEntity().getContent().readAllBytes(),new FileOutputStream(new File("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosansnewtailue/v24/H4cKBW-Pl9DZ0Xe_nHUapt7PovLXAhAnY7wqaLy-OJgU3p_pdeXAYUPghFPKzeY."+ finalI +".woff2")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }

        for(int i=1;i<120;i++){
            String url = "https://fonts.gstatic.com/s/notosansyi/v22/sJoD3LFXjsSdcnzn071rO3apwFDJNVgSNg."+i+".woff2";
            int finalI = i;
            HttpCompoents.getInstance().get(url, null, httpResponse -> {
                try {
                    System.out.println(url + " " + httpResponse.getCode());
                    if (httpResponse.getCode() != 200) {
                        return false;
                    }
                    IoUtils.write(httpResponse.getEntity().getContent().readAllBytes(),new FileOutputStream(new File("/Users/jfit_mac/work/workspace/mp_flutter/web/s/notosansyi/v22/sJoD3LFXjsSdcnzn071rO3apwFDJNVgSNg."+ finalI +".woff2")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }

    }
}
