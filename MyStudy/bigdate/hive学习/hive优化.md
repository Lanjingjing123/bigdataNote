## 1. Hive优化

### 1.1 Hive优化

- 核心思想：**把hive sql当做Mapreduce程序去优化**
- 以下sql不会转换为Mapreduce任务
  - select仅查询本表字段
  - where仅对本表字段做条件过滤

### 1.2 Explain 显示执行计划

	语法：EXPLAIN [EXTENDED] query

### 1.3 Hive运行方式

- 本地模式
- 集群模式

#### 1.3.1本地模式

- 开启本地模式

  - set hive.exec.mode.local.auto=true;

- 注意：

  hive.exec.mode.local.auto.inputbytes.max默认值为128M

  表示加载文件的最大值，若大于该配置仍会以集群方式来运行

### 1.4 并行计算

- 通过设置以下参数开启并行模式

  ​	set hive.exec.parallel=true;

- 注意：hive.exec.parallel.thread.number

  （一次SQL计算中允许并行执行的job个数的最大值）

```sql
select t1.ct1,t2.ct2 from 
(select count(name) as ct1 from psn4) t1,
(select count(id) as ct2 from psn5) t2;
```





### 1.5 严格模式

- 通过设置以下参数开启严格模式：

  set hive.mapred.mode=strict;

  （默认为：nonstrict非严格模式）



- 查询限制：

  1、对于分区表，必须添加where对于分区字段的条件过滤；

  2、order by语句必须包含limit输出限制；

  3、限制执行笛卡尔积的查询。

### 1.6 Hive排序

- Order By - 对于查询结果做全排序，只允许有一个reduce处理

  （当数据量较大时，应慎用。严格模式下，必须结合limit来使用）

- Sort By - 对于单个reduce的数据进行排序

- Distribute By - 分区排序，经常和Sort By结合使用

- Cluster By - 相当于 Sort By **+** Distribute By

  （Cluster By不能通过asc、desc的方式指定排序规则；

  可通过 distribute by column sort by column asc|desc 的方式）

### 1.7 数据倾斜

- #### 数据倾斜

```
   在做Shuffle阶段的优化过程中，遇到了数据倾斜的问题，造成了对一些情况下优化效果不明显。主要是因为在Job完成后的所得到的Counters是整个Job的总和，优化是基于这些Counters得出的平均值，而由于数据倾斜的原因造成处理数据量的差异过大，使得这些平均值能代表的价值降低。Hive的执行是分阶段的，处理数据量的差异取决于上一个stage的reduce输出，所以如何将数据均匀的分配到各个reduce中，就是解决数据倾斜的根本所在。规避错误来更好的运行比解决错误更高效。
```

#### 1.7.1 数据倾斜的原因

- 操作

| 关键词            | 情形                                        | 后果                                       |
| ----------------- | ------------------------------------------- | ------------------------------------------ |
| Join              | 其中一个表较小，但是key集中                 | 分发到某一个或几个Reduce上的数据远高平均值 |
|                   | 大表与大表，但是分桶的判断字段0值或空值过多 | 这些空值都由一个reduce处理，非常慢         |
| group by group by | 维度过小，某值的数量过多                    | 处理某值的reduce非常耗时                   |
| Count Distinct    | 某特殊值过多                                | 处理此特殊值的reduce耗时                   |

- 原因

  1)、key分布不均匀
  2)、业务数据本身的特性
  3)、建表时考虑不周
  4)、某些SQL语句本身就有数据倾斜

- 表现

  ​	任务进度长时间维持在99%（或100%），查看任务监控页面，发现只有少量（1个或几个）reduce子任务未完成。因为其处理的数据量和其他reduce差异过大。
  单一reduce的记录数与平均记录数差异过大，通常可能达到3倍甚至更多。 最长时长远大于平均时长。

#### 1.7.2 数据倾斜的解决方案

- 参数调节
- SQL语句调节
- 空值产生的数据倾斜

#### 1.7.3 不同数据类型关联产生数据倾斜

```sql
场景：用户表中user_id字段为int，log表中user_id字段既有string类型也有int类型。当按照user_id进行两个表的Join操作时，默认的Hash操作会按int型的id来进行分配，这样会导致所有string类型id的记录都分配到一个Reducer中。
解决方法：把数字类型转换成字符串类型
select * from users a left outer join logs b on a.usr_id = cast(b.user_id as string)
```

- 小表不小不大，怎么用 map join 解决倾斜问题

```sql
使用 map join 解决小表(记录数少)关联大表的数据倾斜问题，这个方法使用的频率非常高，但如果小表很大，大到map join会出现bug或异常，这时就需要特别的处理。 以下例子:
select * from log a left outer join users b on a.user_id = b.user_id;

users 表有 600w+ 的记录，把 users 分发到所有的 map 上也是个不小的开销，而且 map join 不支持这么大的小表。如果用普通的 join，又会碰到数据倾斜的问题。
解决方法：
select /+mapjoin(x)/ from log a left outer join ( select /+mapjoin(c)/d. from ( select distinct user_id from log ) c join users d on c.user_id = d.user_id ) x on a.user_id = b.user_id;

假如，log里user_id有上百万个，这就又回到原来map join问题。所幸，每日的会员uv不会太多，有交易的会员不会太多，有点击的会员不会太多，有佣金的会员不会太多等等。所以这个方法能解决很多场景下的数据倾斜问题。
```

### 1.7.4 总结

​	使map的输出数据更均匀的分布到reduce中去，是我们的最终目标。由于Hash算法的局限性，按key Hash会或多或少的造成数据倾斜。大量经验表明数据倾斜的原因是人为的建表疏忽或业务逻辑可以规避的。在此给出较为通用的步骤：

- 4.1、采样log表，哪些user_id比较倾斜，得到一个结果表tmp1。由于对计算框架来说，所有的数据过来，他都是不知道数据分布情况的，所以采样是并不可少的。
- 4.2、数据的分布符合社会学统计规则，贫富不均。倾斜的key不会太多，就像一个社会的富人不多，奇特的人不多一样。所以tmp1记录数会很少。把tmp1和users做map join生成tmp2,把tmp2读到distribute file cache。这是一个map过程。
- 4.3、map读入users和log，假如记录来自log,则检查user_id是否在tmp2里，如果是，输出到本地文件a,否则生成的key,value对，假如记录来自member,生成的key,value对，进入reduce阶段。
- 4.4、最终把a文件，把Stage3 reduce阶段输出的文件合并起写到hdfs。

### 5.如果确认业务需要这样倾斜的逻辑，考虑以下的优化方案：

5.1、对于join，在判断小表不大于1G的情况下，使用map join
5.2、对于group by或distinct，设定 hive.groupby.skewindata=true
5.3、尽量使用上述的SQL语句调节进行优化



