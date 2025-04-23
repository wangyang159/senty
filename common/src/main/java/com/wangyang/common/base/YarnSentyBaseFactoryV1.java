package com.wangyang.common.base;

import com.wangyang.common.bean.ReqMe;
import com.wangyang.common.bean.Result;
import com.wangyang.common.bean.SentyConfig;
import com.wangyang.common.utils.FromUtils;

import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * Yarn 哨兵程序运行所需要的基础信息构造类
 */
public class YarnSentyBaseFactoryV1 {

    /*
    每一个哨兵类型都有一个map集合
    来装载一些后面程序运行时的细节数据。

    比如在保障模式2下获取数据的过程中，如果节点奔溃
    用来知道是那个服务需要做可用节点寻找。

    但是注意只能static代码块装载数据后读取，一定不可在程序后续做变更
    为了快采用了HashMap这个线程不安全的容器，而不是ConcurrentHashMap。
    至于有更改需求的配置，在线程任务外定义，虽然代码变多，但是解决了任务执行中的对象创建开销
    以及多线程的程序安全问题，和线程锁的限制
    毕竟总不能让多个任务在线程锁的限制下同一时间只能一个线程读取需要的数据然后再做操作吧
    属于是一弊三利了，有舍有得
     */
    public static HashMap<String,String> header = new HashMap<>(2);

    static {
        //当前哨兵是yarn
        header.put("senty","hadoop.resourcemaneger.url");
        //把集群id放在YarnSenty的消息map中
        Result result = FromUtils.doFromGetJson(ReqMe.GET, (String) SentyConfig.getConf("hadoop.resourcemaneger.url.about"),null,null,header);
        header.put("hadoopID", (String) SentyConfig.getConf("hadoop.id"));
    }


    //总不能写 SB 吧！哈哈哈
    //日志输出等字符串频繁操作用到的缓冲区
    public static StringBuilder aboutSD = new StringBuilder("");
    //构造数据库存储服务数据的sql
    public static String aboutSQL = aboutSD.delete(0,aboutSD.length())
            .append("update rmAbout set startedOn=?,state=?,haState=?,rmStateStoreName=?,")
            .append("resourceManagerVersion=?,resourceManagerBuildVersion=?,")
            .append("resourceManagerVersionBuiltOn=?,hadoopVersion=?,")
            .append("hadoopBuildVersion=?,hadoopVersionBuiltOn=?,")
            .append("haZooKeeperConnectionState=?,dataTime=?,id=?,fromUrl=? ")
            .append(" where hadoopID=?").toString();
    //需要的时间格式化对象
    public static SimpleDateFormat aboutSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //调度开始时间暂存，默认 0
    public static long aboutStartTime = 0L;

    public static StringBuilder metricsSD = new StringBuilder("");
    public static String metricsSQL = metricsSD.delete(0,metricsSD.length())
            .append("update rmMetric set appsSubmitted=?,appsCompleted=?,appsPending=?,appsRunning=?,")
            .append("appsFailed=?,appsKilled=?,reservedMB=?,availableMB=?,allocatedMB=?,")
            .append("totalMB=?,reservedVirtualCores=?,availableVirtualCores=?,")
            .append("allocatedVirtualCores=?,totalVirtualCores=?,containersAllocated=?,")
            .append("containersReserved=?,containersPending=?,totalNodes=?,activeNodes=?,")
            .append("lostNodes=?,unhealthyNodes=?,decommissioningNodes=?,decommissionedNodes=?,")
            .append("rebootedNodes=?,shutdownNodes=?,dataTime=?,fromUrl=? where hadoopID=?")
            .toString();
    public static SimpleDateFormat metricsSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static long metricsStartTime = 0L;

}
