package com.wangyang;

import com.wangyang.common.bean.SentyClass;
import com.wangyang.common.utils.MysqlUtil;
import com.wangyang.node.NodeStatusExecutor;
import com.wangyang.senty.SentyExecutor;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SentyBuild 用来在启动这个程序
 * 作为程序的核心主类，负责着重要的工作之一：加载配置文件
 * 配的的加载内部采用了commons-configuration2类库
 * 保障书写的复用符号等可以被正常解析
 */
public class SentyBuild {
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

    public static void main(String[] args) {
        //程序入口字符串处理用的 buffer和时间格式化对象
        StringBuilder stringBuilder = new StringBuilder("");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //从程序配置中获取日志，并加载用于后面解析
        String confdir = System.getProperty("conf.dir");
        System.out.println(stringBuilder.delete(0,stringBuilder.length())
                .append(simpleDateFormat.format(new Date()))
                .append(" 识别到配置文件路径 -> ")
                .append(confdir)
                .append("\n\n")
                .append(simpleDateFormat.format(new Date()))
                .append(" ************* 开始加载配置文件 ****************")
                .toString());

        //装载配置用到的key-value变量
        PropertiesConfiguration properties = null;
        Iterator<String> keys = null;
        String key = null;
        String value = null;
        //在统一前缀的校验逻辑中暂存key
        String tmpKey = null;

        try {
            //正式使用的代码
            //properties = new Configurations().properties(confdir + File.separator + "clustersentinel.properties");

            //苹果电脑测试开发
            //properties = new Configurations().properties("/Users/dxm/Downloads/clustersentinel/common/src/main/resources/clustersentinel.properties");

            //win电脑测试开发
            properties = new Configurations().properties("C:\\Users\\wang\\Desktop\\clustersentinel\\common\\src\\main\\resources\\clustersentinel.properties");
        } catch (ConfigurationException e) {
            System.err.println(stringBuilder.delete(0,stringBuilder.length())
                            .append(simpleDateFormat.format(new Date()))
                            .append(" 配置文件 clustersentinel.properties 读取异常！")
                            .toString());
            e.printStackTrace();
        }

        //将配置文件中的内容做必要的校验，最后放在map集合中
        keys = properties.getKeys();
        while (keys.hasNext()){
            key = keys.next();
            value = properties.getString(key);

            //获取服务数据调度时间的相关配置，采用相同的值约束，因此配置换成前缀后面做统一的校验
            if( key.startsWith("senty.con.ratetime") ){
                tmpKey = key;
                key = "senty.con.ratetime";
            }

            //校验细节
            switch (key){
                case "hadoop.resourcemaneger.url":
                    //启动时对服务的所有节点做tcp通行校验，这里的超时时间定死的3秒
                    String[] rmspall = value.split(",");
                    String[] rmsp = null;

                    //用来检查是否写重复了
                    HashSet<String> hasHp = new HashSet<>(rmspall.length);

                    try {
                        for (int i = 0; i < rmspall.length; i++) {
                            boolean contains = hasHp.contains(rmspall[i]);
                            if (contains){
                                System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                        .append(simpleDateFormat.format(new Date()))
                                        .append(" 请确定 resourcemanager 节点是否书写重复：")
                                        .append(rmspall[i])
                                        .toString());
                                System.exit(1);
                            }else{
                                hasHp.add(rmspall[i]);
                            }

                            //验证tcp连接
                            rmsp = rmspall[i].split(":");
                            Socket socket = new Socket();
                            socket.connect(new InetSocketAddress(rmsp[0], Integer.parseInt(rmsp[1])), 3000);
                            socket.close();

                            //校验顺利执行不触发异常，那么在校验第一个rm服务时将对应的服务接口路径装载到配置集合中
                            if( i == 0 ){
                                String aboutUrl = stringBuilder.delete(0,stringBuilder.length()).append("http://").append(rmsp[0]).append(":").append(rmsp[1]).append("/ws/v1/cluster").toString();
                                configs.put("hadoop.resourcemaneger.url.about",aboutUrl);

                                String metricsUrl = stringBuilder.delete(0,stringBuilder.length()).append("http://").append(rmsp[0]).append(":").append(rmsp[1]).append("/ws/v1/cluster/metrics").toString();
                                configs.put("hadoop.resourcemaneger.url.metrics",metricsUrl);
                            }

                        }
                    } catch (UnknownHostException e) {
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" 未知主机： ")
                                .append(rmsp[0])
                                .toString());
                        e.printStackTrace();
                        System.exit(1);
                    } catch (SocketTimeoutException e){
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" 连接超时，请确定域名和端口的填写是否正确!")
                                .append(rmsp[0])
                                .append(":")
                                .append(rmsp[1])
                                .toString());
                        e.printStackTrace();
                        System.exit(1);
                    } catch (IOException e) {
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" ")
                                .append(rmsp[0])
                                .append(" 端口: ")
                                .append(rmsp[1])
                                .append(" 不可用!")
                                .toString());
                        e.printStackTrace();
                        System.exit(1);
                    }
                    //放入配置数据
                    configs.put(key,value);
                    break;
                case "senty.con.timeout":
                    //哨兵的请求超时时间校验
                    BigInteger sctTmp = new BigInteger(value);
                    if( sctTmp.compareTo(BigInteger.valueOf(0)) < 0 || sctTmp.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0  ){
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" 哨兵请求数据的超时时间，单位为毫秒，不能小于0，或超过int类型最大值! 当前值：")
                                .append(value)
                                .toString());
                        System.exit(1);
                    }
                    //将value的数据类型转换为int类型
                    configs.put(key, Integer.parseInt(value));
                    break;
                case "senty.con.ratetime" :
                    //哨兵的请求调度时间校验
                    BigInteger scrtTmp = new BigInteger(value);
                    if( scrtTmp.compareTo(BigInteger.valueOf(5)) < 0 || scrtTmp.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ){
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" 哨兵获取服务数据的调度时间，单位为秒，不能小于5秒，或超过long类型最大值!异常值：")
                                .append(tmpKey)
                                .append(" -> ")
                                .append(value)
                                .toString());
                        System.exit(1);
                    }
                    //将value的数据类型转换为long类型
                    configs.put(tmpKey,Long.parseLong(value));
                    tmpKey = null;
                    break;
                case "senty.status.syn.timeout":
                    //哨兵对各服务状态的心跳同步超时时间校验
                    BigInteger ssstoTmp = new BigInteger(value);
                    if( ssstoTmp.compareTo(BigInteger.valueOf(0)) < 0 || ssstoTmp.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0  ){
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" 哨兵心跳状态同步的超时时间，单位为毫秒，不能小于0，或超过int类型最大值! 当前值：")
                                .append(value)
                                .toString());
                        System.exit(1);
                    }
                    //将value的数据类型转换为int类型
                    configs.put(key, Integer.parseInt(value));
                    break;
                case "senty.status.syn.time":
                    //对各服务状态同步的心跳时间
                    BigInteger ssstTmp = new BigInteger(value);
                    if( ssstTmp.compareTo(BigInteger.valueOf(30)) < 0 || ssstTmp.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ){
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" 哨兵心跳同步的调度时间，单位为秒，不能小于30秒，或超过long类型最大值!当前值：")
                                .append(value)
                                .toString());
                        System.exit(1);
                    }
                    //将value的数据类型转换为long类型
//                    configs.put(key,Long.parseLong(value));
                    configs.put(key,6L);
                    break;
                case "senty.class":
                    //哨兵类型
                    String[] split = value.split(",");
                    stringBuilder.delete(0,stringBuilder.length());

                    //将所有支持的类型拼接成为字符串，最后一个 "," 需要删掉
                    for (SentyClass t : SentyClass.values()){
                        stringBuilder.append(t.name().toUpperCase()).append(",");
                    }
                    String sentyAll = stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length()).toString();

                    //检查配置文件中的内容，并作为后续启动哨兵类型的依据，使用HashSet过滤重复配置
                    HashSet<String> strings = new HashSet<>(SentyClass.values().length);
                    for (String s:split) {
                        if (sentyAll.indexOf(s.trim().toUpperCase())<0){
                            System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                    .append(simpleDateFormat.format(new Date()))
                                    .append(" 未知哨兵类型：")
                                    .append(s)
                                    .toString());
                            System.exit(1);
                        }else {
                            //这里会全部保存为大写，后面程序在使用的时候就不需要再转换了
                            strings.add(s.trim().toUpperCase());
                        }
                    }
                    configs.put(key,String.join(",",strings));
                    break;
                case "senty.safe.mode":
                    //保障模式
                    Pattern compile = Pattern.compile("^[1|2]$");
                    Matcher matcher = compile.matcher(value.trim());
                    if ( !matcher.matches() ){
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" 未知的保障模式：")
                                .append(value)
                                .toString());
                        System.exit(1);
                    }else {
                        configs.put(key,value.trim());
                    }
                    break;
                case "hikari.pool.maxsize":
                    //数据库连接池校验
                    BigInteger hpm = new BigInteger(value);
                    if( hpm.compareTo(BigInteger.valueOf(10)) < 0 || hpm.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0  ){
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" 数据库连接池的总大小，不能小于10，或超过int类型最大值! 当前值：")
                                .append(value)
                                .toString());
                        System.exit(1);
                    }
                    //将value的数据类型转换为int类型
                    configs.put(key, Integer.parseInt(value));
                    break;
                case "hikari.pool.minidle":
                    //数据库连接池校验
                    BigInteger hpmd = new BigInteger(value);
                    if( hpmd.compareTo(BigInteger.valueOf(2)) < 0 || hpmd.compareTo(BigInteger.valueOf( Integer.parseInt(properties.getString("hikari.pool.maxsize")) / 2 )) > 0  ){
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" 数据库连接池的最小空闲连接数，不能小于2，或超过线程池最大值的二分之一(向下取整)! 当前值：")
                                .append(value)
                                .toString());
                        System.exit(1);
                    }
                    //将value的数据类型转换为int类型
                    configs.put(key, Integer.parseInt(value));
                    break;
                case "hikari.pool.idle.timeout":
                    //数据库连接池校验
                    BigInteger hpit = new BigInteger(value);
                    if( hpit.compareTo(BigInteger.valueOf(0)) < 0 || hpit.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0  ){
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" 数据库连接池的连接最大空闲时间，不能小于0，或超过long类型最大值! 当前值：")
                                .append(value)
                                .toString());
                        System.exit(1);
                    }
                    //将value的数据类型转换为long类型
                    configs.put(key, Long.parseLong(value));
                    break;
                case "hikari.pool.max.lifetime":
                    //数据库连接池校验
                    BigInteger hpml = new BigInteger(value);
                    if( hpml.compareTo(BigInteger.valueOf(Long.parseLong(properties.getString("hikari.pool.idle.timeout")))) < 0 ||
                         hpml.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ){
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" 数据库连接池的连接最大存在时间，不能小于最大空闲时间，或超过long类型最大值! 当前值：")
                                .append(value)
                                .toString());
                        System.exit(1);
                    }
                    //将value的数据类型转换为long类型
                    configs.put(key, Long.parseLong(value));
                    break;
                default:
                    //其他的配置，比如数据库的连接配置等不做校验，以字符串装载
                    configs.put(key, value);
                    break;
            }

        }

        //上面的流程结束后，回显当前配置
        stringBuilder.delete(0,stringBuilder.length())
                .append(simpleDateFormat.format(new Date()))
                .append(" ************* 配置文件加载成功 ****************\n");
        Set<String> confKeys = configs.keySet();
        for ( String t : confKeys ){
            stringBuilder.append(t).append(" : ").append(getConf(t)).append("\n");
        }
        stringBuilder.append("\n")
                .append(simpleDateFormat.format(new Date()))
                .append(" ************* 哨兵与心跳状态守护进程开始并行初始化 ****************");
        System.out.println(stringBuilder.toString());
        //清空字符串缓冲区
        stringBuilder.delete(0,stringBuilder.length());

        //最后：启动数据库线程池、哨兵程序和状态同步心跳守护进程
        MysqlUtil.build();
        SentyExecutor.run();
        NodeStatusExecutor.run();

    }


}
