package com.wangyang.common.bean;

/**
 * 作者: wangyang <br/>
 * 创建时间: 2025/2/15 <br/>
 * 描述: <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;RMAbout
 * hadoop-reousrcemanager-集群信息的承载类
 */
public class RMAbout {
    private Long id;//集群自己的随机ID
    private Long startedOn;//集群启动时间
    private String state;//当前RM的状态 NOTINITED, INITED, STARTED, STOPPED
    private String haState;//RM的高可用状态 INITIALIZING, ACTIVE, STANDBY, STOPPED
    private String rmStateStoreName;//负责RM状态同步的类
    private String resourceManagerVersion;//RM这个组件的版本
    private String resourceManagerBuildVersion;//RM组件的构建信息
    private String resourceManagerVersionBuiltOn;//RM组件该版本构建时的时间戳
    private String hadoopVersion;//当前集群hadoop的整体版本
    private String hadoopBuildVersion;//当前Hadoop版本的构建信息
    private String hadoopVersionBuiltOn;//当前Hadoop版本的构建消息
    private String haZooKeeperConnectionState;//高可用服务的连接状态

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStartedOn() {
        return startedOn;
    }

    public void setStartedOn(Long startedOn) {
        this.startedOn = startedOn;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getHaState() {
        return haState;
    }

    public void setHaState(String haState) {
        this.haState = haState;
    }

    public String getRmStateStoreName() {
        return rmStateStoreName;
    }

    public void setRmStateStoreName(String rmStateStoreName) {
        this.rmStateStoreName = rmStateStoreName;
    }

    public String getResourceManagerVersion() {
        return resourceManagerVersion;
    }

    public void setResourceManagerVersion(String resourceManagerVersion) {
        this.resourceManagerVersion = resourceManagerVersion;
    }

    public String getResourceManagerBuildVersion() {
        return resourceManagerBuildVersion;
    }

    public void setResourceManagerBuildVersion(String resourceManagerBuildVersion) {
        this.resourceManagerBuildVersion = resourceManagerBuildVersion;
    }

    public String getResourceManagerVersionBuiltOn() {
        return resourceManagerVersionBuiltOn;
    }

    public void setResourceManagerVersionBuiltOn(String resourceManagerVersionBuiltOn) {
        this.resourceManagerVersionBuiltOn = resourceManagerVersionBuiltOn;
    }

    public String getHadoopVersion() {
        return hadoopVersion;
    }

    public void setHadoopVersion(String hadoopVersion) {
        this.hadoopVersion = hadoopVersion;
    }

    public String getHadoopBuildVersion() {
        return hadoopBuildVersion;
    }

    public void setHadoopBuildVersion(String hadoopBuildVersion) {
        this.hadoopBuildVersion = hadoopBuildVersion;
    }

    public String getHadoopVersionBuiltOn() {
        return hadoopVersionBuiltOn;
    }

    public void setHadoopVersionBuiltOn(String hadoopVersionBuiltOn) {
        this.hadoopVersionBuiltOn = hadoopVersionBuiltOn;
    }

    public String getHaZooKeeperConnectionState() {
        return haZooKeeperConnectionState;
    }

    public void setHaZooKeeperConnectionState(String haZooKeeperConnectionState) {
        this.haZooKeeperConnectionState = haZooKeeperConnectionState;
    }

    public RMAbout() {
    }

    public RMAbout(Long id, Long startedOn, String state, String haState, String rmStateStoreName, String resourceManagerVersion, String resourceManagerBuildVersion, String resourceManagerVersionBuiltOn, String hadoopVersion, String hadoopBuildVersion, String hadoopVersionBuiltOn, String haZooKeeperConnectionState) {
        this.id = id;
        this.startedOn = startedOn;
        this.state = state;
        this.haState = haState;
        this.rmStateStoreName = rmStateStoreName;
        this.resourceManagerVersion = resourceManagerVersion;
        this.resourceManagerBuildVersion = resourceManagerBuildVersion;
        this.resourceManagerVersionBuiltOn = resourceManagerVersionBuiltOn;
        this.hadoopVersion = hadoopVersion;
        this.hadoopBuildVersion = hadoopBuildVersion;
        this.hadoopVersionBuiltOn = hadoopVersionBuiltOn;
        this.haZooKeeperConnectionState = haZooKeeperConnectionState;
    }

    @Override
    public String toString() {
        return "RMAbout{" +
                "id=" + id +
                ", startedOn=" + startedOn +
                ", state='" + state + '\'' +
                ", haState='" + haState + '\'' +
                ", rmStateStoreName='" + rmStateStoreName + '\'' +
                ", resourceManagerVersion='" + resourceManagerVersion + '\'' +
                ", resourceManagerBuildVersion='" + resourceManagerBuildVersion + '\'' +
                ", resourceManagerVersionBuiltOn='" + resourceManagerVersionBuiltOn + '\'' +
                ", hadoopVersion='" + hadoopVersion + '\'' +
                ", hadoopBuildVersion='" + hadoopBuildVersion + '\'' +
                ", hadoopVersionBuiltOn='" + hadoopVersionBuiltOn + '\'' +
                ", haZooKeeperConnectionState='" + haZooKeeperConnectionState + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RMAbout rmAbout = (RMAbout) o;

        if (!id.equals(rmAbout.id)) return false;
        if (!state.equals(rmAbout.state)) return false;
        if (!haState.equals(rmAbout.haState)) return false;
        if (!resourceManagerVersion.equals(rmAbout.resourceManagerVersion)) return false;
        if (!hadoopVersion.equals(rmAbout.hadoopVersion)) return false;
        return haZooKeeperConnectionState.equals(rmAbout.haZooKeeperConnectionState);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + haState.hashCode();
        result = 31 * result + resourceManagerVersion.hashCode();
        result = 31 * result + hadoopVersion.hashCode();
        result = 31 * result + haZooKeeperConnectionState.hashCode();
        return result;
    }
}
