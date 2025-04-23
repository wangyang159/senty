package com.wangyang.node;

import com.wangyang.SentyBuild;
import com.wangyang.common.bean.SentyConfig;
import com.wangyang.node.base.YarnStatusV1;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 节点状态心跳同步的执行类
 * 所有的心跳同步，第一次会由启动时的SentyBuild类去做，并且是所有节点都做了一次
 * 所以这里的第一次调度和所有往后的调度都是配置文件里面写的时间间隔
 */
public class NodeStatusExecutor {

    //运行调度任务的线程池
    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    //存放定时任务的回掉指针，需要的时候可以在暂停任务
    public static Map<String, ScheduledFuture<?>> runSenties = new ConcurrentHashMap<String,  ScheduledFuture<?>>(2);

    public static void run() {
        StringBuilder stringBuilder = new StringBuilder("");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //yarn的节点状态心跳同步
        ScheduledFuture<?> rmSF = scheduler.scheduleWithFixedDelay(() -> {
            YarnStatusV1.rm();
        }, (long) SentyConfig.getConf("senty.status.syn.time"), (long) SentyConfig.getConf("senty.status.syn.time"), TimeUnit.SECONDS);

        runSenties.put("rmSF",rmSF);

        System.out.println(stringBuilder.delete(0,stringBuilder.length())
                .append(simpleDateFormat.format(new Date()))
                .append(" resourcemanager 心跳程序已经就绪"));

    }

}
