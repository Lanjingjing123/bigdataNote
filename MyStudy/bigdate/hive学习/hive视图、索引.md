## 1. Hive Lateral View 

### 1.1 Hive Lateral View 的作用和语法

```java
hive Lateral View
Lateral View用于和UDTF函数（explode、split）结合来使用。
首先通过UDTF函数拆分成多行，再将多行结果组合成一个支持别名的虚拟表。
主要解决在select使用UDTF做查询过程中，查询只能包含单个UDTF，不能包含其他字段、以及多个UDTF的问题

语法：
LATERAL VIEW udtf(expression) tableAlias AS columnAlias (',' columnAlias)

```

### 1.2 案例

统计人员表中共有多少种爱好、多少个城市?

```sql
select count(distinct(myCol1)), count(distinct(myCol2)) from psn4 
LATERAL VIEW explode(likes) myTable1 AS myCol1 
LATERAL VIEW explode(address) myTable2 AS myCol2, myCol3;

```



## 2. 视图

### 2.1 View 语法

```sql
创建视图：
CREATE VIEW [IF NOT EXISTS] [db_name.]view_name 
  [(column_name [COMMENT column_comment], ...) ]
  [COMMENT view_comment]
  [TBLPROPERTIES (property_name = property_value, ...)]
  AS SELECT ... ;
查询视图：
select colums from view;
删除视图：
DROP VIEW [IF EXISTS] [db_name.]view_name;

```



## 3. 索引

### 3.1 索引语法

目的：优化查询以及检索性能

索引创建及其查询：

```sql
create index t1_index on table psn4(name) 
as 'org.apache.hadoop.hive.ql.index.compact.CompactIndexHandler' with deferred rebuild 
in table t1_index_table;

as：指定索引器；
in table：指定索引表，若不指定默认生成在default__psn2_t1_index__表中

create index t1_index on table psn4(name) 
as 'org.apache.hadoop.hive.ql.index.compact.CompactIndexHandler' with deferred rebuild


查询索引
show index on psn4;

重建索引（建立索引之后必须重建索引才能生效）
ALTER INDEX t1_index ON psn4 REBUILD;

删除索引
DROP INDEX IF EXISTS t1_index ON psn4;

```

