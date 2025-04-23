package com.wangyang.senty;

import com.wangyang.SentyBuild;
import com.wangyang.common.bean.SentyConfig;
import com.wangyang.senty.base.YarnSentyV1;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 哨兵的执行类
 * 有别于心跳状态同步，哨兵程序对各服务数据的获取，第一次是在被SentyBuild启动时，就做了
 */
public class SentyExecutor {

    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    //存放定时任务的回掉指针，需要的时候可以在暂停任务
    public static Map<String, ScheduledFuture<?>> runSenties = new ConcurrentHashMap<String,  ScheduledFuture<?>>(2);

    public static void run() {
        StringBuilder stringBuilder = new StringBuilder("");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //根据不同的哨兵类型决定启动哪些哨兵
        String[] sentyClass = ( (String) SentyConfig.getConf("senty.class") ).split(",");

        for ( String s : sentyClass ){
            switch (s){
                case "YARN":
                    //yarn的集群消息监控
                    ScheduledFuture<?> aboutSF = scheduler.scheduleWithFixedDelay(() -> {
                        YarnSentyV1.about();
                    }, 0, (long) SentyConfig.getConf("senty.con.ratetime.resourcemanager.about"), TimeUnit.SECONDS);

                    runSenties.put("aboutSF",aboutSF);

                    System.out.println(stringBuilder.delete(0,stringBuilder.length())
                            .append(simpleDateFormat.format(new Date()))
                            .append(" resourcemanager-about 哨兵程序已经就绪！"));

                    //yarn的队列消息监控
                    ScheduledFuture<?> metricsSF = scheduler.scheduleWithFixedDelay(() -> {
                        YarnSentyV1.metrics();
                    }, 0, (long) SentyConfig.getConf("senty.con.ratetime.resourcemanager.metrics"), TimeUnit.SECONDS);

                    runSenties.put("metricsSF",metricsSF);

                    System.out.println(stringBuilder.delete(0,stringBuilder.length())
                            .append(simpleDateFormat.format(new Date()))
                            .append(" resourcemanager-metrics 哨兵程序已经就绪！"));
                    break;
            }
        }

    }

}
