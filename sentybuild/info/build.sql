create table rmAbout(
    randID int PRIMARY KEY comment '数据表主键,没有实际作用',
    hadoopID text comment 'hadoop集群识别id',
    id bigint comment 'yarn集群自己的随机ID',
    startedOn bigint comment '集群启动时间',
    state varchar(10) comment '当前RM服务的状态 NOTINITED, INITED, STARTED, STOPPED，注意是服务状态，不是请求的节点状态，只要节点不物理死亡，yarn访问任何一个节点都会转发到内部处于激活状态的节点',
    haState varchar(10) comment 'RM的高可用状态 INITIALIZING, ACTIVE, STANDBY, STOPPED',
    rmStateStoreName text comment '负责RM状态同步的类',
    resourceManagerVersion text comment 'RM这个组件的版本',
    resourceManagerBuildVersion text comment 'RM组件的构建信息',
    resourceManagerVersionBuiltOn text comment 'RM组件该版本构建时的时间戳',
    hadoopVersion text comment '当前集群hadoop的整体版本',
    hadoopBuildVersion text comment '当前Hadoop版本的构建信息',
    hadoopVersionBuiltOn text comment '当前Hadoop版本的构建消息',
    haZooKeeperConnectionState text comment '高可用服务的连接状态',
    fromUrl text comment '数据来源信息，记录哨兵请求的路径信息，至于服务内部是否有转发策略，这里不受影响，其他表同样如此',
    dataTime bigint comment '数据调度依据时间'
) comment 'resourcemanager-集群概要数据'
  DEFAULT CHARSET=utf8mb4 ;
insert into rmAbout(randID,hadoopID) value (0,'改成你自己的集群识别id');

create table rmMetric(
    randID int PRIMARY KEY comment '数据表主键,没有实际作用',
    hadoopID text comment 'hadoop集群识别id',
    appsSubmitted int comment '提交过的任务总数量',
    appsCompleted int comment '已完成的任务总数量',
    appsPending int comment '正在排队数量',
    appsRunning int comment '正在运行的数量',
    appsFailed int comment '失败的任务数量',
    appsKilled int comment '被kill的任务数量',
    reservedMB bigint comment '被保留的内存数，取决于虚拟内存比例，以及已有任务的申请情况，由yarn内部自己维护',
    availableMB bigint comment '可用的内存数',
    allocatedMB bigint comment '已经分配出去的内存数',
    totalMB bigint comment '总内存',
    reservedVirtualCores bigint comment '被保留的核数，和内存一样，只不过没有比例影响，通常比较直观，还有多少就是多少，也是由yarn内部自己维护',
    availableVirtualCores bigint comment '可用的核数',
    allocatedVirtualCores bigint comment '已经分配出去的核数',
    totalVirtualCores bigint comment '总核数',
    containersAllocated int comment '在执行的容器个数',
    containersReserved int comment '预留的容器个数',
    containersPending int comment '在排队的容器，这个数据通常和可用资源成反比，排队的越多，可用和保留资源（核数和内存）就越少',
    totalNodes int comment '拥有多少计算节点',
    activeNodes int comment '存活的计算节点数',
    lostNodes int comment '丢失的计算节点数',
    unhealthyNodes int comment '不健康的节点数',
    decommissioningNodes int comment '正在停用的节点数',
    decommissionedNodes int comment '已经停用的节点数',
    rebootedNodes int comment '重新启动的节点数',
    shutdownNodes int comment '关闭的节点数',
    fromUrl text comment '数据来源信息',
    dataTime bigint comment '数据调度依据时间'
) comment 'resourcemanager-集群资源队列数据'
  DEFAULT CHARSET=utf8mb4 ;
insert into rmMetric(randID,hadoopID) value (0,'改成你自己的集群识别id');