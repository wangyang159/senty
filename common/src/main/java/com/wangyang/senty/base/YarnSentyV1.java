package com.wangyang.senty.base;

import com.alibaba.fastjson2.JSON;
import com.wangyang.common.base.YarnSentyBaseFactoryV1;
import com.wangyang.common.bean.*;
import com.wangyang.common.utils.FromUtils;
import com.wangyang.common.utils.MysqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * Yarn 集群哨兵
 * V1 代表着第一版，后续随着接口的增多，为了较高的可维护行
 * 新的服务接口监控会写在 Vn 中
 *
 */
public class YarnSentyV1 {

    /**
     * yarn集群概要消息，在第一次直接运行时就开始访问相关数据
     */
    public static void about() {
        /*
        获取执行时间

        获取集群大致信息和资源概要时，这个时间的 "时间语意" 是当下时间

        单其他涉及到 "时间语意" + 1 的调度就需要做对于的判断
        既：如果是默认的 0 则意味着程序刚启动，此时只记录时间，做 "时间语意" 中的起始时间
           如果不为 0 则获取到的，新的当前时间 要和上一次调度时记录的时间组合为
           "时间语意" + 1  的调度逻辑
         */
        YarnSentyBaseFactoryV1.aboutStartTime = System.currentTimeMillis();

        Result result = FromUtils.doFromGetJson(ReqMe.GET, (String) SentyConfig.getConf("hadoop.resourcemaneger.url.about"),null,null, YarnSentyBaseFactoryV1.header);

        //解析结果
        if ( result.getCode()==200 || result.getCode()==307 || result.getCode()==910 ){
            //如果是经过了保障模式，需要输出一下相关消息，910是正常拿到了数据，而900是放弃调度
            if(result.getCode()==910){
                System.err.println(YarnSentyBaseFactoryV1.aboutSD.delete(0, YarnSentyBaseFactoryV1.aboutSD.length())
                        .append(result.getMeg())
                        .append(" -> ")
                        .append(YarnSentyBaseFactoryV1.aboutSDF.format(new Date(YarnSentyBaseFactoryV1.aboutStartTime)))
                        .toString());
            }

            RMAbout rmAbout = JSON.parseObject(JSON.parseObject((String) result.getDate()).getString("clusterInfo"), RMAbout.class);

            PreparedStatement preparedStatement = null;
            Connection connection = null;
            try {
                connection = MysqlUtil.getConnection();
                preparedStatement = connection.prepareStatement(YarnSentyBaseFactoryV1.aboutSQL);
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
                preparedStatement.setLong(12, YarnSentyBaseFactoryV1.aboutStartTime);
                preparedStatement.setLong(13,rmAbout.getId());
                preparedStatement.setString(14, result.getMeg());
                preparedStatement.setString(15, YarnSentyBaseFactoryV1.header.get("hadoopID"));
                int i = preparedStatement.executeUpdate();
                if ( i == 0 ){
                    System.err.println(YarnSentyBaseFactoryV1.aboutSD.delete(0, YarnSentyBaseFactoryV1.aboutSD.length())
                            .append(YarnSentyBaseFactoryV1.aboutSDF.format(new Date()))
                            .append(" rm-about数据写入不在预期内，请确定数据库是否按照说明文档准备完善! 调度时间: ")
                            .append(YarnSentyBaseFactoryV1.aboutSDF.format(new Date(YarnSentyBaseFactoryV1.aboutStartTime)))
                            .toString());
                }
            } catch (SQLException throwables) {
                System.err.println(YarnSentyBaseFactoryV1.aboutSD.delete(0, YarnSentyBaseFactoryV1.aboutSD.length())
                        .append(YarnSentyBaseFactoryV1.aboutSDF.format(new Date()))
                        .append(" rm-about数据写入异常! 调度时间: ")
                        .append(YarnSentyBaseFactoryV1.aboutSDF.format(new Date(YarnSentyBaseFactoryV1.aboutStartTime)))
                        .toString());
                System.err.println(throwables.getMessage());
            } finally {
                if (preparedStatement != null){
                    try {
                        preparedStatement.close();
                    } catch (SQLException throwables) {
                        System.err.println(YarnSentyBaseFactoryV1.aboutSD.delete(0, YarnSentyBaseFactoryV1.aboutSD.length())
                                .append(YarnSentyBaseFactoryV1.aboutSDF.format(new Date()))
                                .append(" rm-about连接池关闭异常! 调度时间: ")
                                .append(YarnSentyBaseFactoryV1.aboutSDF.format(new Date(YarnSentyBaseFactoryV1.aboutStartTime)))
                                .toString());
                        System.err.println(throwables.getMessage());
                    }
                }
                MysqlUtil.closeConnection(connection);
            }

        }else {
            //如果请求出现错误、改代码没有指定类型、保障模式最终走了放弃调度后这里输出关键信息
            System.err.println(YarnSentyBaseFactoryV1.aboutSD.delete(0, YarnSentyBaseFactoryV1.aboutSD.length())
                    .append(result.getMeg())
                    .append(" -> ")
                    .append(YarnSentyBaseFactoryV1.aboutSDF.format(new Date(YarnSentyBaseFactoryV1.aboutStartTime)))
                    .toString());
        }

    }

    /**
     * yarn集群资源队列概要
     */
    public static void metrics(){
        //获取执行时间
        YarnSentyBaseFactoryV1.metricsStartTime = System.currentTimeMillis();

        Result result = FromUtils.doFromGetJson(ReqMe.GET, (String) SentyConfig.getConf("hadoop.resourcemaneger.url.metrics"),null,null, YarnSentyBaseFactoryV1.header);

        //解析结果
        if ( result.getCode()==200 || result.getCode()==307 || result.getCode()==910 ){
            //如果是经过了保障模式，需要输出一下相关消息，910是正常拿到了数据，而900算在了错误输出里面
            if(result.getCode()==910){
                System.err.println(YarnSentyBaseFactoryV1.metricsSD.delete(0, YarnSentyBaseFactoryV1.metricsSD.length())
                        .append(result.getMeg())
                        .append(" -> ")
                        .append(YarnSentyBaseFactoryV1.metricsSDF.format(new Date(YarnSentyBaseFactoryV1.metricsStartTime)))
                        .toString());
            }

            //解析数据
            RMMetric clusterMetrics = JSON.parseObject(JSON.parseObject((String) result.getDate()).getString("clusterMetrics"), RMMetric.class);

            PreparedStatement preparedStatement = null;
            Connection connection = null;
            try {
                connection = MysqlUtil.getConnection();
                preparedStatement = connection.prepareStatement(YarnSentyBaseFactoryV1.metricsSQL);
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
                preparedStatement.setLong(26, YarnSentyBaseFactoryV1.metricsStartTime);
                preparedStatement.setString(27, result.getMeg());
                preparedStatement.setString(28, YarnSentyBaseFactoryV1.header.get("hadoopID"));

                int i = preparedStatement.executeUpdate();
                if ( i == 0 ){
                    System.err.println(YarnSentyBaseFactoryV1.metricsSD.delete(0, YarnSentyBaseFactoryV1.metricsSD.length())
                            .append(YarnSentyBaseFactoryV1.metricsSDF.format(new Date()))
                            .append(" rm-metrics数据写入不在预期内，请确定数据库是否按照说明文档准备完善! 调度时间: ")
                            .append(YarnSentyBaseFactoryV1.metricsSDF.format(new Date(YarnSentyBaseFactoryV1.metricsStartTime)))
                            .toString());
                }
            } catch (SQLException throwables) {
                System.err.println(YarnSentyBaseFactoryV1.metricsSD.delete(0, YarnSentyBaseFactoryV1.metricsSD.length())
                        .append(YarnSentyBaseFactoryV1.metricsSDF.format(new Date()))
                        .append(" rm-metrics数据写入异常! 调度时间: ")
                        .append(YarnSentyBaseFactoryV1.metricsSDF.format(new Date(YarnSentyBaseFactoryV1.metricsStartTime)))
                        .toString());
                System.err.println(throwables.getMessage());
            } finally {
                if (preparedStatement != null){
                    try {
                        preparedStatement.close();
                    } catch (SQLException throwables) {
                        System.err.println(YarnSentyBaseFactoryV1.metricsSD.delete(0, YarnSentyBaseFactoryV1.metricsSD.length())
                                .append(YarnSentyBaseFactoryV1.metricsSDF.format(new Date()))
                                .append(" rm-metrics连接池关闭异常! 调度时间: ")
                                .append(YarnSentyBaseFactoryV1.metricsSDF.format(new Date(YarnSentyBaseFactoryV1.metricsStartTime)))
                                .toString());
                        System.err.println(throwables.getMessage());
                    }
                }
                MysqlUtil.closeConnection(connection);
            }

        }else {
            System.err.println(YarnSentyBaseFactoryV1.metricsSD.delete(0, YarnSentyBaseFactoryV1.metricsSD.length())
                    .append(result.getMeg())
                    .append(" -> ")
                    .append(YarnSentyBaseFactoryV1.metricsSDF.format(new Date(YarnSentyBaseFactoryV1.metricsStartTime)))
                    .toString());
        }
    }


}
