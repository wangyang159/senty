package com.wangyang.common.utils;

import com.wangyang.SentyBuild;
import com.wangyang.common.bean.ReqMe;
import com.wangyang.common.bean.Result;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 请求相关的工具类
 */
public class FromUtils {

    /*
    这里个人用不做具体校验所有的主机名都给过
     */
    private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier()
    {
        public boolean verify(String arg0, SSLSession arg1) {
            return true;
        }
    };

    /*
    校验证书
     */
    private static void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager()
        {

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }

            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

            }

            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

            }
        }

        };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 发出请求的具体实现方法，可向https和http协议的接口发出请求
     * 以json格式返回请求体
     *
     * @param reqMe 请求的类型
     * @param urlPath  接口的路径
     * @param otherReqBody  发出请求需要携带的参数，需要是Json格式
     * @param otherReqHead 其他自定义的请求头
     * @return 返回响应模板，500-代码异常、200-请求成功Data用有请求相应的Json、900-保障模式触发但无数据、910-保障模式且有数据
     */
    public static Result doFromGetJson(ReqMe reqMe, String urlPath, String otherReqBody, HashMap<String,String> otherReqHead,HashMap<String,String> ortherMeAtt) {

        Result result = new Result();
        URL url = null;
        BufferedReader reader = null;
        HttpURLConnection conn = null;

        if (reqMe==null){
            result.setCode(500);
            result.setMeg("未知的请求类型，请联系RD!"+urlPath);
            return result;
        }

        try {
            url = new URL(urlPath);

            /*
            如果协议是 https，则使用 HttpsURLConnection 并设置 HostnameVerifier 为 DO_NOT_VERIFY，
            并掉用trustAllHosts方法受信所有证书
            否则，使用 HttpURLConnection
             */
            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
                httpsConn.setHostnameVerifier(DO_NOT_VERIFY);
                conn = httpsConn;
            }
            else {
                conn = (HttpURLConnection) url.openConnection();
            }

            // 设置请求以那种形式发出
            conn.setRequestMethod(reqMe.toString());
            // 是否携带请求体，POST和GET请求必须为true
            conn.setDoOutput(true);
            // 是否会获取服务端的响应，默认是true
            conn.setDoInput(true);
            //禁用缓存
            conn.setUseCaches(false);
            // Keep-Alive告诉服务端对已有的TCP连接保持一定时间的活性，以保障后续发出的请求不需要建立新的TCP连接，从而减少网络开销
            conn.setRequestProperty("Connection", "Keep-Alive");
            //字符集，这个配置不是标准的请求头，但是有些妖孽系统会读取这个
            conn.setRequestProperty("Charset", "UTF-8");
            // 这个是请求格式，告诉服务端这个请求发送的是Json数据以及字符集
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            // 告诉服务端，相应格式也希望是json格式
            conn.setRequestProperty("accept", "application/json");

            //其他请求头
            if (otherReqHead!=null && otherReqHead.size()!=0){
                Set<String> keys = otherReqHead.keySet();
                for (String key:keys) {
                    conn.setRequestProperty(key,otherReqHead.get(key).replaceAll(".","-"));
                }
            }

            //如果参数不为空则将参数以字节数流的方式写入请求体里面
            if (otherReqBody != null && !otherReqBody.equals("")) {
                byte[] writebytes = otherReqBody.getBytes();
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(otherReqBody.getBytes());
                outwritestream.flush();
                outwritestream.close();
            }

            // 设置连接超时时间
            conn.setConnectTimeout( (int) SentyBuild.getConf("senty.con.timeout"));
            //读取响应吗，至于响应的数据会是一个json
            int responseCode = conn.getResponseCode();

            /*
            当服务内部无问题时返回的是200，但对于大多数集群环境来讲，一定会有备用节点
            比如hadoop的yarn，当主rm物理死亡，数据的返回是依靠其他备用节点返回是
            响应码就是307，按照规范的前端开发习惯来讲，接口遇到307，要从响应头中拿到新的url，在其他访问操作不变的情况下二次请求
            但是在大数据集群技术站中307，当然其他服务可能会有302，无论哪种都意味着在切换主备节点，此时运行的任务理论上会全部失败
            而接口会返回相应的信息，比如rm会返回Can not find any active RM. Will retry in next XX seconds.
            此时你就算向二次访问也没有可用的action节点啊！！！！
            而切换完成后不会影响数据的访问，因此没有二次访问的必要，同时在else分支中输出了状态码，用来及时的发现新的需要做单独处理的返回码
             */
            if ( responseCode == 200 || responseCode == 307 ) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                result.setDate(reader.readLine());
                result.setMeg(urlPath);
            } else if ( responseCode == 500 ) {
                //一般不用到这个分支，所以没考虑用 stringbuffer 等
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    System.err.println(line); // 打印错误信息
                }
                result.setDate("{}");
                result.setMeg(urlPath+" 请求服务数据出现异常，请联系RD");
            } else {
                System.err.println("注意服务出现需要关注的状态码："+responseCode);
                result.setDate("{}");
                result.setMeg(urlPath+" 请求服务出现了新的状态码，请联系RD，新状态码："+responseCode);
            }
            result.setCode(responseCode);
            return result;
        } catch (Exception e) {
            StringBuffer stringBuilder = new StringBuffer("");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            //当请求发生异常的时候根据保障保障模式做出对应的决策
            int i = Integer.parseInt((String) SentyBuild.getConf("senty.safe.mode"));
            if (i==1){
                //默认的 1 不做任何处理，视为放弃本次调度的数据
                result.setCode(900);
                result.setMeg(stringBuilder.delete(0,stringBuilder.length())
                        .append(simpleDateFormat.format(new Date()))
                        .append(" 忽略策略生效，目标服务出现异常，已放弃本次调度区间：")
                        .append(urlPath)
                        .toString());
                return result;
            } else if (i==2){
                //先把旧请求连接流关闭
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                conn.disconnect();

                //保障2，立马获取存活的节点，从新发起请求，如果没有存活节点，放弃本次数据，留下出现问题的调度批次
                Result senty = statusSyn(ortherMeAtt.get("senty"));

                if (senty.getCode()==200){
                    //有新的节点，则向新节点发出请求
                    String[] hp = (String[]) senty.getDate();
                    //解析协议
                    String reAgr =  url.getProtocol().toLowerCase().equals("https") ? "https://" : "http://";
                    //解析出正确的url
                    Pattern compile = Pattern.compile("^(https?://[^:/]+(?::\\d+)?)");
                    Matcher matcher = compile.matcher(urlPath);
                    //由于请求格式没有对外提供更改，格式是安全的，所以不做find结果判断，就可以得出原来路径中的节点地址
                    matcher.find();
                    String oldHp = matcher.group(1);
                    //将存活节点替换，倒着插进去，形成新的urlPath
                    String newUrlPath = stringBuilder.delete(0, stringBuilder.length())
                            .append(urlPath)
                            .delete(0,oldHp.length())
                            .insert(0,hp[1])
                            .insert(0,":")
                            .insert(0,hp[0])
                            .insert(0,reAgr)
                            .toString();
                    //递归自身，拿数据
                    Result newResult = doFromGetJson(reqMe, newUrlPath, otherReqBody, otherReqHead,ortherMeAtt);
                    //这里的910是doFromGetJson的返回情况，不再是statusSyn的了，要做好区分
                    newResult.setCode(910);
                    newResult.setMeg(stringBuilder.delete(0,stringBuilder.length())
                            .append(simpleDateFormat.format(new Date()))
                            .append(" 舔狗策略生效，已通过其他节点恢复调度，本次调度区间：")
                            .append(newUrlPath)
                            .toString());
                    return newResult;
                }else if(senty.getCode()==300){
//                    System.err.println("哨兵输出-异常信息"+e.getMessage());
                    e.printStackTrace();
                    //触发到这里的时候为了给旧连接流关闭或目标服务其他灾备手段留有预留时间，所以暂停三秒
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException interruptedException) {
                        System.err.println(stringBuilder.delete(0,stringBuilder.length())
                                .append(simpleDateFormat.format(new Date()))
                                .append(" 舔狗策略成效，但哨兵程序线程出现意外，请联系RD!")
                                .toString());
                    }

                    /*
                    用旧节点再次请求拿数据的时候，由于HttpURLConnection对象是一次性的，所以只能递归重走方法
                    并且要防止一种很容易发生的极端情况，哨兵程序自身不直接更新配置中，节点对应的接口等相关内容
                    如果是保障模式介入之初进入了发现新节点的策略，哨兵会直接走处理新节点的接口逻辑，从而确保了一致性
                    而反之，走到向旧节点再次访问时，如果不对旧节点和当前被视为无需更新的节点列表，做一致性的强校验
                    那么当节点列表恰巧被心跳机制更新，但哨兵自己还拿着有问题的旧接口路径时，会导致哨兵对这个问题路径进入访问死循环的递归

                    这种情况的根本原因是，想让保障机制执行200的前提，是目标节点崩溃开始算起，心跳守护程序没有调度过
                    此时哨兵程序访问目标服务数据时，才会在调用寻找新节点方法时返回200，可是程序执行中所有的概率都是随机的
                    而且心跳程序的调度间隔一般情况下较短，因此大部分同类情况下基本都是300，即使某一次触发了200
                    但是由于哨兵不直接改配置中的节点相关数据，而导致下一次触发执行任然拿的是问题url，此时心跳机制更新相关配置后
                    哨兵程序自身任然会走300，除非此时心跳机制任然没执行过
                     */

                    System.err.println("哨兵输出-当前调度刚进来的路径"+urlPath);

                    //statusSyn方法在无需跟新节点时，也会返回当前判断为可通信的节点
                    String[] hp = (String[]) senty.getDate();
                    //解析协议
                    String reAgr =  url.getProtocol().toLowerCase().equals("https") ? "https://" : "http://";

                    //根据测试得知startsWith方法在1毫秒内可完成，而拼接即使用了 stringBuilder 也需要1-3毫秒，所以如果一致性是通过的，则沿用旧的url
                    boolean b = urlPath.startsWith(stringBuilder.delete(0, stringBuilder.length()).append(reAgr).append(hp[0]).append(":").append(hp[1]).toString());
                    Result newResult = null;
                    String newUrlPath = null;
                    if (b){
                        newResult = doFromGetJson(reqMe, urlPath, otherReqBody, otherReqHead,ortherMeAtt);
                    }else {
                        //解析出正确的url
                        Pattern compile = Pattern.compile("^(https?://[^:/]+(?::\\d+)?)");
                        Matcher matcher = compile.matcher(urlPath);
                        //由于请求格式没有对外提供更改，格式是安全的，所以不做find结果判断，就可以得出原来路径中的节点地址
                        matcher.find();
                        String oldHp = matcher.group(1);
                        //将存活节点替换，倒着插进去，形成新的urlPath
                        newUrlPath = stringBuilder.delete(0, stringBuilder.length())
                                .append(urlPath)
                                .delete(0,oldHp.length())
                                .insert(0,hp[1])
                                .insert(0,":")
                                .insert(0,hp[0])
                                .insert(0,reAgr)
                                .toString();
                        newResult = doFromGetJson(reqMe, newUrlPath, otherReqBody, otherReqHead,ortherMeAtt);
                    }

                    //这里的910是doFromGetJson的返回情况，不再是statusSyn的了，要做好区分
                    newResult.setCode(910);
                    newResult.setMeg(stringBuilder.delete(0,stringBuilder.length())
                            .append(simpleDateFormat.format(new Date()))
                            .append(" 舔狗策略与心跳守护进程同时成效，已恢复本次调度区间：")
                            .append( b ? urlPath : newUrlPath )
                            .toString());
                    return newResult;
                }else if (senty.getCode()==400){
                    //现在不做任何操作，后续如果有需要会加入套接字，到的400时停止这个哨兵，并通过套接字触发重起
                    result.setCode(900);
                    result.setMeg(stringBuilder.delete(0,stringBuilder.length())
                            .append(simpleDateFormat.format(new Date()))
                            .append(" 舔狗策略生效，但目标服务已完全奔溃，已放弃本次调度区间：")
                            .append(urlPath)
                            .toString());
                    return result;
                }
            }
        } finally {
            //读取相应用的输入流需要在这里关闭，而写入请求参数的输出流是随用随关的
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //关闭网络连接
            conn.disconnect();
        }

        return result;
    }

    /**
     * 服务节点心跳状态同步方法
     * 由于在启动时所有的节点已经做过了校验
     * 因此在哨兵程序执行中采用顺序判断，只要有一个节点存活就不再往下判断
     * 并且返回当前判断为存活的节点，这样做是因为启动时对所有的节点做了心跳检查
     * 后续做心跳就不需要全做了，保障有一个可用的节点就行
     *
     * 反之如果判断到最后没有任何节点存活，则给出错误输出
     * @param key 当前做心跳的服务
     * @return 200-发现需要更新的存活节点、300-服务正常且无需更新存活节点、400-没有任何节点存活
     */
    public static Result statusSyn(String key){
        //准备返回对象、取出要更新的服务url并分隔节点、一个stringshuffer
        Result result = new Result();
        String value = (String) SentyBuild.getConf(key);
        String[] hpArr = value.split(",");
        String[] hp = null;

        for (int i = 0; i < hpArr.length; i++) {
            hp = hpArr[i].split(":");
            try {
                Socket socket = new Socket();
                //socket.connect(new InetSocketAddress(hp[0], Integer.parseInt(hp[1])),3000);
                socket.connect(new InetSocketAddress(hp[0], Integer.parseInt(hp[1])), (int) SentyBuild.getConf("senty.status.syn.timeout"));
                socket.close();

                //如果connect没有触发超时异常，且执行到第一顺位的心跳就是可用的，此时什么也不会发生，则返回300
                if (i == 0) {
                    result.setCode(300);
                    result.setDate(hp);
                    return result;
                }else {
                    //反之如果 i=0 没return，意味着主要节点崩溃了，此时返回存活节点，并把它放在第一顺位
                    result.setCode(200);
                    result.setDate(hp);
                    StringBuffer stringBuffer = new StringBuffer();
                    //由于逻辑需要，try写在了for里面，为了优化异常触发的次数，将存活节点放在第一位
                    stringBuffer.append(hpArr[i]);
                    for (int j = 0; j < hp.length; j++) {
                        if( j != i ){
                            stringBuffer.append(",");
                            stringBuffer.append(hpArr[j]);
                        }
                    }
                    SentyBuild.setConf(key,stringBuffer.toString());
                    return result;
                }
            } catch (Exception e) {
                //忽略异常
            }
        }
        //如果循环到最后没有触发循环中的return，就说明没有任何一个服务节点是存活的
        result.setCode(400);
        return result;
    }

}
