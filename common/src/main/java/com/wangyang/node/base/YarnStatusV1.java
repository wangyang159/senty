package com.wangyang.node.base;

import com.wangyang.SentyBuild;
import com.wangyang.common.bean.Result;
import com.wangyang.common.utils.FromUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Yarn的心跳状态更新
 */
public class YarnStatusV1 {

    /**
     * ResourceManager的心跳同步
     * 由于大部分情况下，集群的节点是正常的，所以为了节省开销，只有非300的状态时才创新需要的变量
     */
    public static void rm(){
        Result result = FromUtils.statusSyn("hadoop.resourcemaneger.url");

        if (result.getCode()==200){
            //如果存在新节点需要把对应的接口更新
            String[] hp = (String[]) result.getDate();
            StringBuilder stringBuilder = new StringBuilder();

            String aboutUrl = stringBuilder.delete(0,stringBuilder.length()).append("http://").append(hp[0]).append(":").append(hp[1]).append("/ws/v1/cluster").toString();
            SentyBuild.setConf("hadoop.resourcemaneger.url.about",aboutUrl);

            String metricsUrl = stringBuilder.delete(0,stringBuilder.length()).append("http://").append(hp[0]).append(":").append(hp[1]).append("/ws/v1/cluster/metrics").toString();
            SentyBuild.setConf("hadoop.resourcemaneger.url.metrics",metricsUrl);

        }else if (result.getCode()==300){
            //不做任何操作
        }else if (result.getCode()==400){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            StringBuilder stringBuilder = new StringBuilder();

            System.err.println(stringBuilder.delete(0,stringBuilder.length())
                    .append(simpleDateFormat.format(new Date()))
                    .append(" ResourceManager 所有节点奔溃！请尽快排查"));
        }

    }

}
