common模块的程序入口主类为com.wangyang.SentyBuild，负责装载服务配置，调起所有哨兵程序以及服务节点的心跳守护进程

com.wangyang.senty.base 包下存放着所有哨兵类，用来负责向不同服务端发出请求获取相关数据<br/>
所有的哨兵类会在com.wangyang.senty.SentyExecutor类中被统一启动<br/>

com.wangyang.node.base 包下存放着所有服务心跳进程类，用来负责维护服务的tcp通讯状态<br/>
所有的守护进程类会在com.wangyang.node.NodeStatusExecutor类中被统一启动<br/>

com.wangyang.common包下存放着整个程序公用的资源类<br/>

<hr/>

整个哨兵程序，提供了两种保障模式，用来在服务出现节点崩溃时，采用不同的策略对调度任务做出相应的容灾操作<br/>

```text
senty.safe.mode=1 (忽略模式：如果哨兵程序在调度过程执行中，目标服务发生意外，则放弃本次调度)
senty.safe.mode=2 (舔狗模式：哈哈哈哈！！这个名字怎么样，我真的是个取名天才呢。如果哨兵程序在调度过程执行中
                           目标服务发生意外，会立刻！马上的！
                           寻找存活节点再次访问服务，此时不受心跳守护进程的影响，是并行的)
```

当你将采用默认的 senty.safe.mode=1 模式时，如果集群当前的主节点崩溃了，则视为放弃当前调度周期的任务，直到心跳守护程序将可用节点修正，如下测试日志<br/>

```text
2025-02-11 22:31:42 ************* 开始加载配置文件 ****************
2025-02-11 22:31:42 ************* 配置文件加载成功 ****************
senty.status.syn.timeout : 3000

----------------这里初始阶段node2为首位服务节点
hadoop.resourcemaneger.url : node2:8088,node3:8088
senty.status.syn.time : 5
senty.con.ratetime.resourcemanager : 5
hadoop.resourcemaneger.url.about : http://node2:8088/ws/v1/cluster
senty.class : YARN
senty.con.timeout : 3000
senty.safe.mode : 1

2025-02-11 22:31:42 ************* 哨兵与心跳状态守护进程开始并行初始化 ****************
2025-02-11 22:31:42 resourcemanager 哨兵程序已经就绪！
2025-02-11 22:31:42 resourcemanager 心跳程序已经就绪

----------这里哨兵程序开始陆续执行
哨兵程序执行时间 2025-02-11 22:31:42
Result{code=200, meg='null', date={"clusterInfo":{"id":1739282892150,"startedOn":1739282892150,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}

-----------这里心跳程序开始陆续执行
node2:8088,node3:8088
http://node2:8088/ws/v1/cluster
哨兵程序执行时间 2025-02-11 22:31:48
Result{code=200, meg='null', date={"clusterInfo":{"id":1739282892150,"startedOn":1739282892150,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}
node2:8088,node3:8088
http://node2:8088/ws/v1/cluster
哨兵程序执行时间 2025-02-11 22:31:53
Result{code=200, meg='null', date={"clusterInfo":{"id":1739282892150,"startedOn":1739282892150,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}
node2:8088,node3:8088
http://node2:8088/ws/v1/cluster
哨兵程序执行时间 2025-02-11 22:31:58
Result{code=200, meg='null', date={"clusterInfo":{"id":1739282892150,"startedOn":1739282892150,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}
node2:8088,node3:8088
http://node2:8088/ws/v1/cluster

------------这里手动kill node2的RM进程，哨兵程序在保障模式为 1 时放弃本次调度任务
哨兵程序执行时间 2025-02-11 22:32:03
Result{code=900, meg='2025-02-11 22:32:05 请求的目标服务出现异常，已放弃本次调度：这里先做个占位后面需要输出本次调度的关键时间', date=null}

------------这里开始心跳程序会将更新首要节点
node3:8088,node2:8088
http://node3:8088/ws/v1/cluster

-------------这里哨兵程序恢复，需要注意的是，程序内部采用上一次完全退出才开启下一次调度的方式，因此如果你的调度周期较短，时间上会由于灾备切换等操作导致有一些延时
哨兵程序执行时间 2025-02-11 22:32:10
Result{code=200, meg='null', date={"clusterInfo":{"id":1739282892150,"startedOn":1739282892150,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}
node3:8088,node2:8088
http://node3:8088/ws/v1/cluster
哨兵程序执行时间 2025-02-11 22:32:15
Result{code=200, meg='null', date={"clusterInfo":{"id":1739282892150,"startedOn":1739282892150,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}
node3:8088,node2:8088
http://node3:8088/ws/v1/cluster
哨兵程序执行时间 2025-02-11 22:32:20
Result{code=200, meg='null', date={"clusterInfo":{"id":1739282892150,"startedOn":1739282892150,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}
```
如果在保障模式 1 下，如果对应服务的所有节点都崩溃了，此时不止哨兵程序会忽略每一次调度，心跳守护进程会输出崩溃提示以及时间，如下测试日志
```text
2025-02-11 22:31:42 ************* 开始加载配置文件 ****************
2025-02-11 22:31:42 ************* 配置文件加载成功 ****************
senty.status.syn.timeout : 3000

----------------这里初始阶段node2为首位服务节点
hadoop.resourcemaneger.url : node2:8088,node3:8088
senty.status.syn.time : 5
senty.con.ratetime.resourcemanager : 5
hadoop.resourcemaneger.url.about : http://node2:8088/ws/v1/cluster
senty.class : YARN
senty.con.timeout : 3000
senty.safe.mode : 1

2025-02-11 22:31:42 ************* 哨兵与心跳状态守护进程开始并行初始化 ****************
2025-02-11 22:31:42 resourcemanager 哨兵程序已经就绪！
2025-02-11 22:31:42 resourcemanager 心跳程序已经就绪

----------这里哨兵程序开始陆续执行
哨兵程序执行时间 2025-02-11 22:31:42
Result{code=200, meg='null', date={"clusterInfo":{"id":1739282892150,"startedOn":1739282892150,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}

-----------这里心跳程序开始陆续执行
node2:8088,node3:8088
http://node2:8088/ws/v1/cluster
哨兵程序执行时间 2025-02-11 22:31:48
Result{code=200, meg='null', date={"clusterInfo":{"id":1739282892150,"startedOn":1739282892150,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}
node2:8088,node3:8088
http://node2:8088/ws/v1/cluster
哨兵程序执行时间 2025-02-11 22:31:53
Result{code=200, meg='null', date={"clusterInfo":{"id":1739282892150,"startedOn":1739282892150,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}
node2:8088,node3:8088
http://node2:8088/ws/v1/cluster
哨兵程序执行时间 2025-02-11 22:31:58
Result{code=200, meg='null', date={"clusterInfo":{"id":1739282892150,"startedOn":1739282892150,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}
node2:8088,node3:8088
http://node2:8088/ws/v1/cluster

-------------------这里手动kill node2-resourcemanager节点，心跳守护进程将主要节点切换为node3
node3:8088,node2:8088
http://node3:8088/ws/v1/cluster

-------------------这里哨兵程序放弃本次调度任务
哨兵程序执行时间 2025-02-11 22:50:30
Result{code=900, meg='2025-02-11 22:50:32 请求的目标服务出现异常，已放弃本次调度：这里先做个占位后面需要输出本次调度的关键时间', date=null}

--------------------使用node3程序正常执行
node3:8088,node2:8088
http://node3:8088/ws/v1/cluster
哨兵程序执行时间 2025-02-11 22:50:37
Result{code=200, meg='null', date={"clusterInfo":{"id":1739285445313,"startedOn":1739285445313,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}

-------------------这里手动kill node3，哨兵程序开始放弃每一次调度
哨兵程序执行时间 2025-02-11 22:50:42
Result{code=900, meg='2025-02-11 22:50:44 请求的目标服务出现异常，已放弃本次调度：这里先做个占位后面需要输出本次调度的关键时间', date=null}

--------------------心跳守护进程将输出所有节点崩溃时间
2025-02-11 22:50:46 ResourceManager 所有节点奔溃！请尽快排查
node3:8088,node2:8088
http://node3:8088/ws/v1/cluster

哨兵程序执行时间 2025-02-11 22:50:49
Result{code=900, meg='2025-02-11 22:50:51 请求的目标服务出现异常，已放弃本次调度：这里先做个占位后面需要输出本次调度的关键时间', date=null}

2025-02-11 22:50:55 ResourceManager 所有节点奔溃！请尽快排查
node3:8088,node2:8088
http://node3:8088/ws/v1/cluster
```

当你将采用可选的 senty.safe.mode=2 模式，当服务的节点发生不同程度崩溃，哨兵程序会有三种不同的状态，如下测试日志
```text
 2025-02-12 21:30:35 ************* 开始加载配置文件 ****************
2025-02-12 21:30:35 ************* 配置文件加载成功 ****************
senty.status.syn.timeout : 3000
hadoop.resourcemaneger.url : node2:8088,node3:8088
senty.status.syn.time : 5
senty.con.ratetime.resourcemanager : 5
hadoop.resourcemaneger.url.about : http://node2:8088/ws/v1/cluster
senty.class : YARN
senty.con.timeout : 3000
senty.safe.mode : 2

2025-02-12 21:30:35 ************* 哨兵与心跳状态守护进程开始并行初始化 ****************
2025-02-12 21:30:35 resourcemanager 哨兵程序已经就绪！
2025-02-12 21:30:35 resourcemanager 心跳程序已经就绪

node2:8088,node3:8088
http://node2:8088/ws/v1/cluster

哨兵程序执行时间 2025-02-12 21:30:35
Result{code=200, meg='null', date={"clusterInfo":{"id":1739367058641,"startedOn":1739367058641,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}

node2:8088,node3:8088
http://node2:8088/ws/v1/cluster

哨兵程序执行时间 2025-02-12 21:30:49
Result{code=200, meg='null', date={"clusterInfo":{"id":1739367058641,"startedOn":1739367058641,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}

node2:8088,node3:8088
http://node2:8088/ws/v1/cluster

------------------这里手动kill node2，心跳守护进程切换主节点
node3:8088,node2:8088
http://node3:8088/ws/v1/cluster

------------------当首要服务节点崩溃，哨兵程序会立刻主动寻找可用节点保证服务数据的抽取
哨兵程序执行时间 2025-02-12 21:30:54
Result{code=900, meg='2025-02-12 21:30:59 保障策略成效，调度已恢复，本次调度：这里先做个占位后面需要输出本次调度的关键时间', date={"clusterInfo":{"id":1739367058641,"startedOn":1739367058641,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}

-----------------心跳守护进程不受影响正常触发
node3:8088,node2:8088
http://node3:8088/ws/v1/cluster

哨兵程序执行时间 2025-02-12 21:31:04
Result{code=200, meg='null', date={"clusterInfo":{"id":1739367058641,"startedOn":1739367058641,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}

node3:8088,node2:8088
http://node3:8088/ws/v1/cluster
哨兵程序执行时间 2025-02-12 21:31:09
Result{code=200, meg='null', date={"clusterInfo":{"id":1739367058641,"startedOn":1739367058641,"state":"STARTED","haState":"ACTIVE","rmStateStoreName":"org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore","resourceManagerVersion":"3.2.3","resourceManagerBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum cd5fc22f993469a5d67fe8bb2902e43","resourceManagerVersionBuiltOn":"2022-03-20T01:27Z","hadoopVersion":"3.2.3","hadoopBuildVersion":"3.2.3 from abe5358143720085498613d399be3bbf01e0f131 by ubuntu source checksum 39bb14faec14b3aa25388a6d7c345fe8","hadoopVersionBuiltOn":"2022-03-20T01:18Z","haZooKeeperConnectionState":"CONNECTED"}}}

-----------------------手动kill node3
2025-02-12 21:31:27 ResourceManager 所有节点奔溃！请尽快排查
node3:8088,node2:8088
http://node3:8088/ws/v1/cluster

----------------------此时开始随着心跳输出崩溃关键信息，哨兵程序本身会放弃调度的任务
哨兵程序执行时间 2025-02-12 21:31:24
Result{code=900, meg='2025-02-12 21:31:30 触发保障模式，但目标服务已完全奔溃，已放弃本次调度：这里先做个占位后面需要输出本次调度的关键时间', date=null}
哨兵程序执行时间 2025-02-12 21:31:35
node3:8088,node2:8088
http://node3:8088/ws/v1/cluster
2025-02-12 21:31:36 ResourceManager 所有节点奔溃！请尽快排查
Result{code=900, meg='2025-02-12 21:31:41 触发保障模式，但目标服务已完全奔溃，已放弃本次调度：这里先做个占位后面需要输出本次调度的关键时间', date=null}

-----------!!!!!重点：保障模式 2 时，如果哨兵程序触发了保障模式，可是在寻找可用节点之前！！这段时间内，如果目标集群拥有其他保障和容灾策略能够再次吊起首要节点，哨兵会再次访问对应节点
```

<hr/>

1、以上的输出日志是测试效果特意做出的输出展示，在正式的程序执行中相关信息回输出在标准错误输出中，你可以在log路径下看到<br/>
2、同样的相比较于两种保障模式，通常用 1 即可<br/>
3、程序内部存留了哨兵和心跳程序的回调指针，如有需要可以自己改成节点奔溃后注销自身，但是这意味着要开发Socket<br/>
   来再次调起程序，考虑到集群自身全面奔溃的概率不大，所以现在没有做这方面的细化开发，如果你有需要可以在SentyExecutor中看到存放回调指针的容器<br/>
4、想要完整的理解执行流程，建议的看代码流程是：SentyBuild-》MysqlUtil-》SentyExecutor-》FromUtils-》NodeStatusExecutor<br/>
   在代码中yarn相关哨兵流程中，留有相当详细的注释，帮助你理解代码逻辑，并在此基础上掉用你自己的集群监听接口<br/>
5、需要格外注意的是，目前只有目标服务返回码非200、500、307三种，且服务请求时出现致命异常时才会触发保障模式<br/>
   200、307将正常处理调度任务，500回在错误输出中输出报错，而其它的会在错误文件中输出有需要关注的新状态码提示，具体逻辑见FromUtils<br/>