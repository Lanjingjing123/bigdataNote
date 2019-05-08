# 1. Protobuf的安装

## 1.1 安装开发环境

```vb
# yum grouplist          --查看可安装的组
# yum groupinstall Development tools   --安装开发工具组

 tar -xzf protobuf-2.1.0.tar.gz 
 cd protobuf-2.1.0 
./configure --prefix=$INSTALL_DIR --直接用 ./configure
 make 
make check  ---不用此命令（耗时太长）
 make install
```

## 1.2 Example

### 1.2.1 书写 .proto文件

- 将下面文件写入phone.proto中

```java
package com.bjsxt.hbase;
message PhoneDetail 
 { 
    required string     dnum = 1;  
    required string    length = 2;   
    required string     type = 3;   
 	required string 	date=4;
 }

```

### 1.2.2 编译.proto文件

```vb
# protoc --java_out=/root/ phone.proto
```

### 1.2.3 编写java的writer和reader

```java
	@Test
	public void insertDb3() throws Exception, InterruptedIOException {
		
		List<Put> puts = new ArrayList<Put>();
		for(int i=0;i<10;i++) {
			String phoneNum = getPhoneNum("186");
			for(int j=0;j<100;j++) {
				String dnum = getPhoneNum("158");
				String length = random.nextInt(99)+"";
				String type = random.nextInt(2)+"";
				String dateStr = getDate("2018");
				String rowkey = phoneNum+"_"+(Long.MAX_VALUE- sdf.parse(dateStr).getTime());
				Phone.PhoneDetail.Builder phoneDetail = Phone.PhoneDetail.newBuilder();
				phoneDetail.setDnum(dnum);
				phoneDetail.setLength(length);
				phoneDetail.setType(type);
				phoneDetail.setDate(dateStr);
				Put put = new Put(rowkey.getBytes());
				put.add("cf".getBytes(), "phoneDetail".getBytes(), 		phoneDetail.build().toByteArray());
				puts.add(put);
			}
		}
		htable.put(puts);
	}
```

## 1.3 Example2

```java
package com.bjsxt.hbase;
message PhoneDetail 
 { 
    required string     dnum = 1;  
    required string    length = 2;   
    required string     type = 3;   
 	required string 	date=4;
 }
message dayPhoneDetail
{
    repeated PhoneDetail dayPhone = 1;
}
```

