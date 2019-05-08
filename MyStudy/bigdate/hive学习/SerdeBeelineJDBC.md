## 1. SerDe

* Hive SerDe - Serializer and Deserializer

  —— SerDe 用于序列化和反序列化

  —— 构建在数据存储和执行引擎之间，对两者实现解耦。

  —— Hive通过ROW FORMAT DELIMITED以及SERDE进行内容的读写 

  row_format

  ```java
  : DELIMITED 
            [FIELDS TERMINATED BY char [ESCAPED BY char]] 
            [COLLECTION ITEMS TERMINATED BY char] 
            [MAP KEYS TERMINATED BY char] 
            [LINES TERMINATED BY char] 
  : SERDE serde_name [WITH SERDEPROPERTIES (property_name=property_value, property_name=property_value, ...)]
  ```

  example(数据清洗):

  1. 创建表，row format 用serde方式

  ```java
   hive> CREATE TABLE logtbl (
      host STRING,
      identity STRING,
      t_user STRING,
      time STRING,
      request STRING,
      referer STRING,
      agent STRING)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.RegexSerDe'
    WITH SERDEPROPERTIES (
      "input.regex" = "([^ ]*) ([^ ]*) ([^ ]*) \\[(.*)\\] \"(.*)\" (-|[0-9]*) (-|[0-9]*)"
    )
       STORED AS TEXTFILE
  
  ```

  2.  导入数据

     ```java
     hive> LOAD DATA LOCAL INPATH '/root/data4'  INTO TABLE logtbl;
     ```

  3. 注意点

     row format 建立的规则只有**读时检查**，不是**写时检查**

     

## 2. Beeline

Beeline 要与HiveServer2配合使用

服务端启动hiveserver2

客户的通过beeline两种方式连接到hive

1、beeline -u jdbc:hive2://localhost:10000/default -n root

2、beeline

beeline> !connect jdbc:hive2://<host>:<port>/<db>;auth=noSasl root 123

默认 用户名、密码不验证

## 3. JDBC

1. JDBC必须与HiveServer2配合使用，node07启动 HiveServer2
2. 客户端编写如下代码进行测试（可直接运行）：

```JAVA
package com.sxt.hive;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HiveJdbcClient {

	private static String driverName = "org.apache.hive.jdbc.HiveDriver";

	public static void main(String[] args) throws SQLException {
		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Connection conn = DriverManager.getConnection("jdbc:hive2://node07:10000/default", "root", "");
		Statement stmt = conn.createStatement();
		String sql = "select * from psn5 limit 5";
		ResultSet res = stmt.executeQuery(sql);
		while (res.next()) {
			System.out.println(res.getString(1) + "-" + res.getString("name"));
		}
	}
}
```

## 4. 函数的使用

[函数文档](C:\Users\ljj\Desktop\SVN\MyStudy\bigdate\hive学习\document\hive函数.docx)

测试：

1. 函数代码：

```java
package com.sxt.hive;


import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

public class TuoMin extends UDF {

	public Text evaluate(final Text s) {
		if (s == null) {
			return null;
		}
		String str = s.toString().substring(0, 3) + "***";
		return new Text(str);
	}

}
```

2. 将此代码进行打包，上传到hive客户端

   hive>add jar /root/MyHive.jar;

3. 自定义临时函数（限当前客户端hive窗口有效，关闭重启失效）

   hive>CREATE TEMPORARY FUNCTION tm AS 'com.sxt.hive.TuoMin';