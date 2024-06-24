package com.eryansky.encrypt.cron;

/**
 * The type Cron server.
 *
 * @author : 尔演@Eryan

 * @description : 可以用它来定时生产密钥
 * 
 */
public abstract class CronServer {

    /**
     * #\u52A0\u5BC6\u5206\u7EC4
     * [com.eryansky.encrypt.cron]
     * CronServer.run = 1 * * * * *
     * 定时任务 在resource目录下创建config文件夹 跟config.setting文件 config.setting是文件名
     */
    public void run(){
        System.out.println("定时任务执行");
    }
}
