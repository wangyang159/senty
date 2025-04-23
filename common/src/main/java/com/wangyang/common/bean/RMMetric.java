package com.wangyang.common.bean;

/**
 * resourcemanager的资源队列消息详情
 */
public class RMMetric {
    private Integer appsSubmitted;//提交过的任务总数量
    private Integer appsCompleted;//已完成的任务总数量
    private Integer appsPending;//正在排队数量
    private Integer appsRunning;//正在运行的数量
    private Integer appsFailed;//失败的任务数量
    private Integer appsKilled;//被kill的任务数量
    private Long reservedMB;//被保留的内存数，取决于虚拟内存比例，以及已有任务的申请情况，由yarn内部自己维护
    private Long availableMB;//可用的内存数
    private Long allocatedMB;//已经分配出去的内存数
    private Long totalMB;//总内存
    private Long reservedVirtualCores;//被保留的核数，和内存一样，只不过没有比例影响，通常比较直观，还有多少就是多少，也是由yarn内部自己维护
    private Long availableVirtualCores;//可用的核数
    private Long allocatedVirtualCores;//已经分配出去的核数
    private Long totalVirtualCores;//总核数
    private Integer containersAllocated;//在执行的容器个数
    private Integer containersReserved;//预留的容器个数
    private Integer containersPending;//在排队的容器，这个数据通常和可用资源成反比，排队的越多，可用和保留资源（核数和内存）就越少

    //下面这几个对应了计算节点随着运维或压力的不同状态，一般不常关注
    private Integer totalNodes;//拥有多少计算节点
    private Integer activeNodes;//存活的计算节点数
    private Integer lostNodes;//丢失的计算节点数
    private Integer unhealthyNodes;//不健康的节点数
    private Integer decommissioningNodes;//正在停用的节点数
    private Integer decommissionedNodes;//已经停用的节点数
    private Integer rebootedNodes;//重新启动的节点数
    private Integer shutdownNodes;//关闭的节点数

    public RMMetric() {
    }

    public RMMetric(Integer appsSubmitted, Integer appsCompleted, Integer appsPending, Integer appsRunning, Integer appsFailed, Integer appsKilled, Long reservedMB, Long availableMB, Long allocatedMB, Long totalMB, Long reservedVirtualCores, Long availableVirtualCores, Long allocatedVirtualCores, Long totalVirtualCores, Integer containersAllocated, Integer containersReserved, Integer containersPending, Integer totalNodes, Integer activeNodes, Integer lostNodes, Integer unhealthyNodes, Integer decommissioningNodes, Integer decommissionedNodes, Integer rebootedNodes, Integer shutdownNodes) {
        this.appsSubmitted = appsSubmitted;
        this.appsCompleted = appsCompleted;
        this.appsPending = appsPending;
        this.appsRunning = appsRunning;
        this.appsFailed = appsFailed;
        this.appsKilled = appsKilled;
        this.reservedMB = reservedMB;
        this.availableMB = availableMB;
        this.allocatedMB = allocatedMB;
        this.totalMB = totalMB;
        this.reservedVirtualCores = reservedVirtualCores;
        this.availableVirtualCores = availableVirtualCores;
        this.allocatedVirtualCores = allocatedVirtualCores;
        this.totalVirtualCores = totalVirtualCores;
        this.containersAllocated = containersAllocated;
        this.containersReserved = containersReserved;
        this.containersPending = containersPending;
        this.totalNodes = totalNodes;
        this.activeNodes = activeNodes;
        this.lostNodes = lostNodes;
        this.unhealthyNodes = unhealthyNodes;
        this.decommissioningNodes = decommissioningNodes;
        this.decommissionedNodes = decommissionedNodes;
        this.rebootedNodes = rebootedNodes;
        this.shutdownNodes = shutdownNodes;
    }

    public Integer getAppsSubmitted() {
        return appsSubmitted;
    }

    public void setAppsSubmitted(Integer appsSubmitted) {
        this.appsSubmitted = appsSubmitted;
    }

    public Integer getAppsCompleted() {
        return appsCompleted;
    }

    public void setAppsCompleted(Integer appsCompleted) {
        this.appsCompleted = appsCompleted;
    }

    public Integer getAppsPending() {
        return appsPending;
    }

    public void setAppsPending(Integer appsPending) {
        this.appsPending = appsPending;
    }

    public Integer getAppsRunning() {
        return appsRunning;
    }

    public void setAppsRunning(Integer appsRunning) {
        this.appsRunning = appsRunning;
    }

    public Integer getAppsFailed() {
        return appsFailed;
    }

    public void setAppsFailed(Integer appsFailed) {
        this.appsFailed = appsFailed;
    }

    public Integer getAppsKilled() {
        return appsKilled;
    }

    public void setAppsKilled(Integer appsKilled) {
        this.appsKilled = appsKilled;
    }

    public Long getReservedMB() {
        return reservedMB;
    }

    public void setReservedMB(Long reservedMB) {
        this.reservedMB = reservedMB;
    }

    public Long getAvailableMB() {
        return availableMB;
    }

    public void setAvailableMB(Long availableMB) {
        this.availableMB = availableMB;
    }

    public Long getAllocatedMB() {
        return allocatedMB;
    }

    public void setAllocatedMB(Long allocatedMB) {
        this.allocatedMB = allocatedMB;
    }

    public Long getTotalMB() {
        return totalMB;
    }

    public void setTotalMB(Long totalMB) {
        this.totalMB = totalMB;
    }

    public Long getReservedVirtualCores() {
        return reservedVirtualCores;
    }

    public void setReservedVirtualCores(Long reservedVirtualCores) {
        this.reservedVirtualCores = reservedVirtualCores;
    }

    public Long getAvailableVirtualCores() {
        return availableVirtualCores;
    }

    public void setAvailableVirtualCores(Long availableVirtualCores) {
        this.availableVirtualCores = availableVirtualCores;
    }

    public Long getAllocatedVirtualCores() {
        return allocatedVirtualCores;
    }

    public void setAllocatedVirtualCores(Long allocatedVirtualCores) {
        this.allocatedVirtualCores = allocatedVirtualCores;
    }

    public Long getTotalVirtualCores() {
        return totalVirtualCores;
    }

    public void setTotalVirtualCores(Long totalVirtualCores) {
        this.totalVirtualCores = totalVirtualCores;
    }

    public Integer getContainersAllocated() {
        return containersAllocated;
    }

    public void setContainersAllocated(Integer containersAllocated) {
        this.containersAllocated = containersAllocated;
    }

    public Integer getContainersReserved() {
        return containersReserved;
    }

    public void setContainersReserved(Integer containersReserved) {
        this.containersReserved = containersReserved;
    }

    public Integer getContainersPending() {
        return containersPending;
    }

    public void setContainersPending(Integer containersPending) {
        this.containersPending = containersPending;
    }

    public Integer getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(Integer totalNodes) {
        this.totalNodes = totalNodes;
    }

    public Integer getActiveNodes() {
        return activeNodes;
    }

    public void setActiveNodes(Integer activeNodes) {
        this.activeNodes = activeNodes;
    }

    public Integer getLostNodes() {
        return lostNodes;
    }

    public void setLostNodes(Integer lostNodes) {
        this.lostNodes = lostNodes;
    }

    public Integer getUnhealthyNodes() {
        return unhealthyNodes;
    }

    public void setUnhealthyNodes(Integer unhealthyNodes) {
        this.unhealthyNodes = unhealthyNodes;
    }

    public Integer getDecommissioningNodes() {
        return decommissioningNodes;
    }

    public void setDecommissioningNodes(Integer decommissioningNodes) {
        this.decommissioningNodes = decommissioningNodes;
    }

    public Integer getDecommissionedNodes() {
        return decommissionedNodes;
    }

    public void setDecommissionedNodes(Integer decommissionedNodes) {
        this.decommissionedNodes = decommissionedNodes;
    }

    public Integer getRebootedNodes() {
        return rebootedNodes;
    }

    public void setRebootedNodes(Integer rebootedNodes) {
        this.rebootedNodes = rebootedNodes;
    }

    public Integer getShutdownNodes() {
        return shutdownNodes;
    }

    public void setShutdownNodes(Integer shutdownNodes) {
        this.shutdownNodes = shutdownNodes;
    }


    @Override
    public String toString() {
        return "RMMetric{" +
                "appsSubmitted=" + appsSubmitted +
                ", appsCompleted=" + appsCompleted +
                ", appsPending=" + appsPending +
                ", appsRunning=" + appsRunning +
                ", appsFailed=" + appsFailed +
                ", appsKilled=" + appsKilled +
                ", reservedMB=" + reservedMB +
                ", availableMB=" + availableMB +
                ", allocatedMB=" + allocatedMB +
                ", totalMB=" + totalMB +
                ", reservedVirtualCores=" + reservedVirtualCores +
                ", availableVirtualCores=" + availableVirtualCores +
                ", allocatedVirtualCores=" + allocatedVirtualCores +
                ", totalVirtualCores=" + totalVirtualCores +
                ", containersAllocated=" + containersAllocated +
                ", containersReserved=" + containersReserved +
                ", containersPending=" + containersPending +
                ", totalNodes=" + totalNodes +
                ", activeNodes=" + activeNodes +
                ", lostNodes=" + lostNodes +
                ", unhealthyNodes=" + unhealthyNodes +
                ", decommissioningNodes=" + decommissioningNodes +
                ", decommissionedNodes=" + decommissionedNodes +
                ", rebootedNodes=" + rebootedNodes +
                ", shutdownNodes=" + shutdownNodes +
                '}';
    }

}
