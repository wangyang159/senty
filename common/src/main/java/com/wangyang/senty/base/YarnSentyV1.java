package com.wangyang.senty.base;

import com.alibaba.fastjson2.JSON;
import com.wangyang.SentyBuild;
import com.wangyang.common.bean.RMAbout;
import com.wangyang.common.bean.RMMetric;
import com.wangyang.common.bean.ReqMe;
import com.wangyang.common.bean.Result;
import com.wangyang.common.utils.FromUtils;
import com.wangyang.common.utils.MysqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Yarn 集群哨兵
 * V1 代表着第一版，后续随着接口的增多，为了较高的可维护行
 * 新的服务接口监控会写在 Vn 中
 *
 */
public class YarnSentyV1 {

    /*
    每一个哨兵类型都有一个map集合，来装载一些后面掉用程序时的细节数据。比如在保障模式2下获取数据，如果节点奔溃后
    用来知道是那个服务需要做可用节点寻找。但是注意只能static装载数据后读取，后面一定不可在程序后续做变更，因为为了快
    采用了HashMap这个线程不安全的容器，而不是ConcurrentHashMap。
    至于有更改需求的配置，在线程任务外定义，虽然代码变多，但是解决了任务执行中的对象创建开销，以及多线程的程序安全问题，和线程锁的限制
    毕竟总不能让多个任务在线程锁的限制下同一时间只能一个线程做操作吧，比如更新调度区间内的时间依据，属于是一弊三利了，有舍有得
     */
    public static HashMap<String,String> header = new HashMap<>(2);

    static {
        //当前哨兵是yarn
        header.put("senty","hadoop.resourcemaneger.url");
        //把集群id放在YarnSenty的消息map中
        Result result = FromUtils.doFromGetJson(ReqMe.GET, (String) SentyBuild.getConf("hadoop.resourcemaneger.url.about"),null,null,header);
        header.put("hadoopID", (String) SentyBuild.getConf("hadoop.id"));
    }

    //总不能写 SB 吧！哈哈哈
    private static StringBuilder aboutSD = new StringBuilder("");
    private static String aboutSQL = aboutSD.delete(0,aboutSD.length())
            .append("update rmAbout set startedOn=?,state=?,haState=?,rmStateStoreName=?,")
            .append("resourceManagerVersion=?,resourceManagerBuildVersion=?,")
            .append("resourceManagerVersionBuiltOn=?,hadoopVersion=?,")
            .append("hadoopBuildVersion=?,hadoopVersionBuiltOn=?,")
            .append("haZooKeeperConnectionState=?,dataTime=?,id=?,fromUrl=? ")
            .append(" where hadoopID=?").toString();
    private static SimpleDateFormat aboutSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static long aboutStartTime = 0L;

    private static StringBuilder metricsSD = new StringBuilder("");
    private static String metricsSQL = metricsSD.delete(0,metricsSD.length())
            .append("update rmMetric set appsSubmitted=?,appsCompleted=?,appsPending=?,appsRunning=?,")
            .append("appsFailed=?,appsKilled=?,reservedMB=?,availableMB=?,allocatedMB=?,")
            .append("totalMB=?,reservedVirtualCores=?,availableVirtualCores=?,")
            .append("allocatedVirtualCores=?,totalVirtualCores=?,containersAllocated=?,")
            .append("containersReserved=?,containersPending=?,totalNodes=?,activeNodes=?,")
            .append("lostNodes=?,unhealthyNodes=?,decommissioningNodes=?,decommissionedNodes=?,")
            .append("rebootedNodes=?,shutdownNodes=?,dataTime=?,fromUrl=? where hadoopID=?")
            .toString();
    private static SimpleDateFormat metricsSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static long metricsStartTime = 0L;

    /**
     * yarn集群概要消息，在第一次直接运行时就开始访问相关数据
     */
    public static void about() {
        //获取执行时间
        aboutStartTime = System.currentTimeMillis();

        Result result = FromUtils.doFromGetJson(ReqMe.GET, (String) SentyBuild.getConf("hadoop.resourcemaneger.url.about"),null,null,header);

        //解析结果
        if ( result.getCode()==200 || result.getCode()==307 || result.getCode()==910 ){
            //如果是经过了保障模式，需要输出一下相关消息，910是正常拿到了数据，而900是放弃调度
            if(result.getCode()==910){
                System.err.println(aboutSD.delete(0,aboutSD.length())
                        .append(result.getMeg())
                        .append(" -> ")
                        .append(aboutSDF.format(new Date(aboutStartTime)))
                        .toString());
            }

            RMAbout rmAbout = JSON.parseObject(JSON.parseObject((String) result.getDate()).getString("clusterInfo"), RMAbout.class);

            PreparedStatement preparedStatement = null;
            Connection connection = null;
            try {
                connection = MysqlUtil.getConnection();
                preparedStatement = connection.prepareStatement(aboutSQL);
                preparedStatement.setLong(1,rmAbout.getStartedOn());
                preparedStatement.setString(2,rmAbout.getState());
                preparedStatement.setString(3,rmAbout.getHaState());
                preparedStatement.setString(4,rmAbout.getRmStateStoreName());
                preparedStatement.setString(5,rmAbout.getResourceManagerVersion());
                preparedStatement.setString(6,rmAbout.getResourceManagerBuildVersion());
                preparedStatement.setString(7,rmAbout.getResourceManagerVersionBuiltOn());
                preparedStatement.setString(8,rmAbout.getHadoopVersion());
                preparedStatement.setString(9,rmAbout.getHadoopBuildVersion());
                preparedStatement.setString(10,rmAbout.getHadoopVersionBuiltOn());
                preparedStatement.setString(11,rmAbout.getHaZooKeeperConnectionState());
                preparedStatement.setLong(12,aboutStartTime);
                preparedStatement.setLong(13,rmAbout.getId());
                preparedStatement.setString(14, result.getMeg());
                preparedStatement.setString(15,header.get("hadoopID"));
                int i = preparedStatement.executeUpdate();
                if ( i == 0 ){
                    System.err.println(aboutSD.delete(0,aboutSD.length())
                            .append(aboutSDF.format(new Date()))
                            .append(" rm-about数据写入不在预期内，请确定数据库是否按照说明文档准备完善! 调度时间: ")
                            .append(aboutSDF.format(new Date(aboutStartTime)))
                            .toString());
                }
            } catch (SQLException throwables) {
                System.err.println(aboutSD.delete(0,aboutSD.length())
                        .append(aboutSDF.format(new Date()))
                        .append(" rm-about数据写入异常! 调度时间: ")
                        .append(aboutSDF.format(new Date(aboutStartTime)))
                        .toString());
                System.err.println(throwables.getMessage());
            } finally {
                if (preparedStatement != null){
                    try {
                        preparedStatement.close();
                    } catch (SQLException throwables) {
                        System.err.println(aboutSD.delete(0,aboutSD.length())
                                .append(aboutSDF.format(new Date()))
                                .append(" rm-about连接池关闭异常! 调度时间: ")
                                .append(aboutSDF.format(new Date(aboutStartTime)))
                                .toString());
                        System.err.println(throwables.getMessage());
                    }
                }
                MysqlUtil.closeConnection(connection);
            }

        }else {
            //如果请求出现错误、改代码没有指定类型、保障模式最终走了放弃调度后这里输出关键信息
            System.err.println(aboutSD.delete(0,aboutSD.length())
                    .append(result.getMeg())
                    .append(" -> ")
                    .append(aboutSDF.format(new Date(aboutStartTime)))
                    .toString());
        }

    }

    /**
     * yarn集群资源队列概要
     */
    public static void metrics(){
        //获取执行时间
        metricsStartTime = System.currentTimeMillis();

        Result result = FromUtils.doFromGetJson(ReqMe.GET, (String) SentyBuild.getConf("hadoop.resourcemaneger.url.metrics"),null,null,header);

        //解析结果
        if ( result.getCode()==200 || result.getCode()==307 || result.getCode()==910 ){
            //如果是经过了保障模式，需要输出一下相关消息，910是正常拿到了数据，而900算在了错误输出里面
            if(result.getCode()==910){
                System.err.println(metricsSD.delete(0,metricsSD.length())
                        .append(result.getMeg())
                        .append(" -> ")
                        .append(metricsSDF.format(new Date(metricsStartTime)))
                        .toString());
            }

            //解析数据
            RMMetric clusterMetrics = JSON.parseObject(JSON.parseObject((String) result.getDate()).getString("clusterMetrics"), RMMetric.class);

            PreparedStatement preparedStatement = null;
            Connection connection = null;
            try {
                connection = MysqlUtil.getConnection();
                preparedStatement = connection.prepareStatement(metricsSQL);
                preparedStatement.setInt(1,clusterMetrics.getAppsSubmitted());
                preparedStatement.setInt(2,clusterMetrics.getAppsCompleted());
                preparedStatement.setInt(3,clusterMetrics.getAppsPending());
                preparedStatement.setInt(4,clusterMetrics.getAppsRunning());
                preparedStatement.setInt(5,clusterMetrics.getAppsFailed());
                preparedStatement.setInt(6,clusterMetrics.getAppsKilled());
                preparedStatement.setLong(7,clusterMetrics.getReservedMB());
                preparedStatement.setLong(8,clusterMetrics.getAvailableMB());
                preparedStatement.setLong(9,clusterMetrics.getAllocatedMB());
                preparedStatement.setLong(10,clusterMetrics.getTotalMB());
                preparedStatement.setLong(11,clusterMetrics.getReservedVirtualCores());
                preparedStatement.setLong(12,clusterMetrics.getAvailableVirtualCores());
                preparedStatement.setLong(13,clusterMetrics.getAllocatedVirtualCores());
                preparedStatement.setLong(14,clusterMetrics.getTotalVirtualCores());
                preparedStatement.setInt(15,clusterMetrics.getContainersAllocated());
                preparedStatement.setInt(16,clusterMetrics.getContainersReserved());
                preparedStatement.setInt(17,clusterMetrics.getContainersPending());
                preparedStatement.setInt(18,clusterMetrics.getTotalNodes());
                preparedStatement.setInt(19,clusterMetrics.getActiveNodes());
                preparedStatement.setInt(20,clusterMetrics.getLostNodes());
                preparedStatement.setInt(21,clusterMetrics.getUnhealthyNodes());
                preparedStatement.setInt(22,clusterMetrics.getDecommissioningNodes());
                preparedStatement.setInt(23,clusterMetrics.getDecommissionedNodes());
                preparedStatement.setInt(24,clusterMetrics.getRebootedNodes());
                preparedStatement.setInt(25,clusterMetrics.getShutdownNodes());
                preparedStatement.setLong(26,metricsStartTime);
                preparedStatement.setString(27, result.getMeg());
                preparedStatement.setString(28,header.get("hadoopID"));

                int i = preparedStatement.executeUpdate();
                if ( i == 0 ){
                    System.err.println(metricsSD.delete(0,metricsSD.length())
                            .append(metricsSDF.format(new Date()))
                            .append(" rm-metrics数据写入不在预期内，请确定数据库是否按照说明文档准备完善! 调度时间: ")
                            .append(metricsSDF.format(new Date(metricsStartTime)))
                            .toString());
                }
            } catch (SQLException throwables) {
                System.err.println(metricsSD.delete(0,metricsSD.length())
                        .append(metricsSDF.format(new Date()))
                        .append(" rm-metrics数据写入异常! 调度时间: ")
                        .append(metricsSDF.format(new Date(metricsStartTime)))
                        .toString());
                System.err.println(throwables.getMessage());
            } finally {
                if (preparedStatement != null){
                    try {
                        preparedStatement.close();
                    } catch (SQLException throwables) {
                        System.err.println(metricsSD.delete(0,metricsSD.length())
                                .append(metricsSDF.format(new Date()))
                                .append(" rm-metrics连接池关闭异常! 调度时间: ")
                                .append(metricsSDF.format(new Date(metricsStartTime)))
                                .toString());
                        System.err.println(throwables.getMessage());
                    }
                }
                MysqlUtil.closeConnection(connection);
            }

        }else {
            System.err.println(metricsSD.delete(0,metricsSD.length())
                    .append(result.getMeg())
                    .append(" -> ")
                    .append(metricsSDF.format(new Date(metricsStartTime)))
                    .toString());
        }
    }


}
