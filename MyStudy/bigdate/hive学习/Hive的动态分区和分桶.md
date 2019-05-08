## 1. hive的动态分区

### 1.1 hive参数

* **hive参数设置方式**

  1. 修改配置文件 ${HIVE_HOME}/conf/hive-site.xml

  2. 启动hive cli时，通过--hiveconf key=value的方式进行设置

     ```sql
     例：hive --hiveconf hive.cli.print.header=true
     ```

  3. 进入cli之后，通过使用set命令设置

  4. hive set命令
     在hive CLI控制台可以通过set对hive中的参数进行查询、设置
     set设置：

     ```sql
     set hive.cli.print.header=true;
     set查看:
     set hive.cli.print.header
     hive参数初始化配置
     当前用户家目录下的.hiverc文件
     如:   ~/.hiverc
     如果没有，可直接创建该文件，将需要设置的参数写到该文件中，hive启动运行时，会加载改文件中的配置。
     hive历史操作命令集
     ~/.hivehistory
     ```

### 1.2 hive 动态分区原理

​	动态分区：根据一个原始表的数据，对原始表创建新的分区表，条件是根据某一列或几列进行系统自动分区，不需要人工指定。这里的测试是利用原始表中的sex跟age进行动态分区

### 1.3  hive动态 分区数据测试

1. 开启支持动态分区

```sql
- set hive.exec.dynamic.partition=true;
  默认：false

- set hive.exec.dynamic.partition.mode=nostrict;
  默认：strict（至少有一个分区列是静态分区）设置属性hive.mapred.mode 为strict能够阻止以下三种类型的查询：

  ​	1、  除非在where语段中包含了分区过滤，否则不能查询分区了的表。这是因为分区表通常保存的数据量都比较大，没有限定分区查询会扫描所有分区，耗费很多资源。

  Table:  logs(…) partitioned by (day int);

  不允许：select *from logs;

  允许：select *from logs where day=20151212;

  ​	2、  包含order by，但没有limit子句的查询。因为orderby 会将所有的结果发送给单个reducer来执行排序，这样的排序很耗时。

  ​	3、笛卡尔乘积；简单理解就是JOIN没带ON，而是带where的

  - 相关参数
    - set hive.exec.max.dynamic.partitions.pernode;
      每一个执行mr节点上，允许创建的动态分区的最大数量(100)
    - set hive.exec.max.dynamic.partitions;
      所有执行mr节点上，允许创建的所有动态分区的最大数量(1000)
    - set hive.exec.max.created.files;
      所有的mr job允许创建的文件的最大数量(100000)

  

  - 相关参数
    - set hive.exec.max.dynamic.partitions.pernode;
      每一个执行mr节点上，允许创建的动态分区的最大数量(100)
    - set hive.exec.max.dynamic.partitions;
      所有执行mr节点上，允许创建的所有动态分区的最大数量(1000)
    - set hive.exec.max.created.files;
      所有的mr job允许创建的文件的最大数量(100000)
```

2. 创建基础表并加载测试数据

   - 创建普通表

     - 数据准备

     ```java
     1,小明1,man,99,LOL-Book-Movie,beijing:shangxuetang-shanghai:pudong
     2,小明2,man,33,LOL-Book-Movie,beijing:shangxuetang-shanghai:pudong
     3,小明3,man,33,LOL-Book-Movie,beijing:shangxuetang-shanghai:pudong
     4,小明4,man,33,LOL-Book-Movie,beijing:shangxuetang-shanghai:pudong
     5,小明5,boy,99,LOL-Book-Movie,beijing:shangxuetang-shanghai:pudong
     6,小明6,boy,99,LOL-Book-Movie,beijing:shangxuetang-shanghai:pudong
     7,小明7,boy,33,LOL-Book-Movie,beijing:shangxuetang-shanghai:pudong
     8,小明8,boy,33,LOL-Book-Movie,beijing:shangxuetang-shanghai:pudong
     9,小明9,man,99,LOL-Book-Movie,beijing:shangxuetang-shanghai:xxxx
     ```

     - 创建表并且将上面数据加载进表

     ```java
     CREATE  TABLE  psn21(
         id int,
         name string,
         sex string,
         age int,
         likes ARRAY < string >,
         address  MAP < string, string >
     ) 
     
     ROW FORMAT
     DELIMITED FIELDS TERMINATED BY ','
     COLLECTION ITEMS TERMINATED BY '-'
     MAP KEYS TERMINATED BY ':';
     ```

     

3. 创建分区表并加载测试数据

   - 创建分区表

   ```java
   CREATE  TABLE  psn22(
       id int,
       name string,
       likes ARRAY < string >,
       address  MAP < string, string >
   ) 
   PARTITIONED BY (sex String,age int ) 
   ROW FORMAT
   DELIMITED FIELDS TERMINATED BY ','
   COLLECTION ITEMS TERMINATED BY '-'
   MAP KEYS TERMINATED BY ':';
   ```

   - 导入数据

   ```sql
   from psn21
   insert overwrite table psn22 partition(sex, age)  
   select id, name, likes, address ,sex, age  distribute by sex, age;
   
   ```

   - 结果图

     ![分区测试图](C:\Users\ljj\Desktop\SVN\MyStudy\bigdate\hive学习\image\分区测试图.png)

     ​						

## 2.  hive的分桶

### 2.1 分桶原理

- 分桶表是对列值取哈希值的方式，将不同数据放到不同文件中存储。
- 对于hive中每一个表、分区都可以进一步进行分桶。
- 由列的哈希值除以桶的个数来决定每条数据划分在哪个桶中。（**实际原理便是 ：hash值取模**）
- 试用场景：数据抽样（ sampling ）、map-join

### 2.2 分桶测试

#### 2.2.1 开启支持分桶

```sql
set hive.enforce.bucketing=true;
```

​	默认：false；设置为true之后，mr运行时会根据bucket的个数自动分配reduce task个数。（用户也可以通过	mapred.reduce.tasks自己设置reduce任务个数，但分桶时不推荐使用）

​	注意：一次作业产生的桶（文件数量）和reduce task个数一致。

#### 2.2.2 创建普通表

- 测试数据

```java
1,tom,11
2,cat,22
3,dog,33
4,hive,44
5,hbase,55
6,mr,66
7,alice,77
8,scala,88
```

- 创建普通表表

```sql
CREATE TABLE psn31( id INT, name STRING, age INT)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';
```

- 导入数据

```sql
 LOAD DATA LOCAL INPATH '/root/data6'  INTO TABLE psn31 ;
```

### 2.2.3 创建分区表

- 创建分区表

```sql
CREATE TABLE psnbucket( id INT, name STRING, age INT)
CLUSTERED BY (age) INTO 4 BUCKETS 
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';
```

- 加载数据

```sql
insert into table psnbucket select id, name, age from psn31;
```

- 抽样查看（语法见附语法）

```sql
select id, name, age from psnbucket tablesample(bucket 2 out of 4 on age);--这句sql查询第二个桶的数据
select id, name, age from psnbucket tablesample(bucket 2 out of 2 on age)；--这句sql查询两个桶的数据，分别为 第二个桶 第四个桶
```

- 附语法

```sql
桶表 抽样查询
select * from bucket_table tablesample(bucket 1 out of 4 on columns);

TABLESAMPLE语法：
TABLESAMPLE(BUCKET x OUT OF y)
x：表示从哪个bucket开始抽取数据
y：必须为该表总bucket数的倍数或因子
```

- 结果图（四个分桶）：

  ![1555223390658](C:\Users\ljj\Desktop\SVN\MyStudy\bigdate\hive学习\image\1555223390658.png)

## 3. 个人总结

​	此章节学了动态分区，分桶；

​	**分区的目的是为了方便增加查询的效率**，粗细的粒度需要人为进行判断；切记分的太细，太细文件检索目录也需要遍历也需要耗时，太粗查询较慢；

​	分桶可对分区和表进行分桶，这里只对表进行了分桶测试，**分桶主要是为了进行数据抽样！**