## 1. hive运行方式

- 命令行方式cli：控制台模式
- 脚本运行方式（实际生产环境中用最多）
- JDBC方式：hiveserver2
- web
  GUI接口 （hwi、hue等）

## 2. Hive在CLI模式中

- 与hdfs交互

  执行执行dfs命令

  例：dfs –ls /

- 与Linux交互

  ！开头

  例： !pwd

## 3. hive 脚本运行方式

```java
Hive脚本运行方式：
hive -e ""
hive -e "">aaa
hive -S -e "">aaa
hive -f file
hive -i /home/my/hive-init.sql
hive> source file (在hive cli中运行)

```

