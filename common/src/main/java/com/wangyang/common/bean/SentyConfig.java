package com.wangyang.common.bean;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SentyConfig {
    //装载配置的map集合，采用线程安全的map集合，因为后面会有变动的需求，比如心跳程序和保障模式2下的哨兵
    private static Map<String,Object> configs = new ConcurrentHashMap<String, Object>(25);

    /**
     * 获取配置，主键类型是string，具体的值需要使用时强转
     */
    public static Object getConf(String key){
        return configs.get(key);
    }

    /**
     * 更新配置
     * @param key-value
     */
    public static void setConf(String key,Object value){
        configs.put(key,value);
    }

    public static Set<String> getKets(){
        return configs.keySet();
    }
}
