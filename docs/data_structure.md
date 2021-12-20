# 数据结构

* instances 用于防止任务用于实例名相同带来的数据混淆。每次新建相关任务会查询是否有重名
  instances：[instancename,...]

## redis 数据结构到 TiKV 的映射关系

总体原则 redis key => instancename^db^type^keyname^description

### string

{instId}_{dbNum}_{commandType}_{keyName}

*{instId}_{dbNum}_{commandType}_{keyName}_{index}

### list

* key格式
  instancename^db^l^keyname^index

  *{instId}_{dbNum}_{commandType}_{keyName}_{index}

### hansh

* key格式
  instancename^db^h^keyname^field

### set

* key格式
  instancename^db^s^keyname^value

### sorted set

* key格式
  instancename^db^z^keyname^value

### Bit arrays

* key格式
  instancename^db^b^keyname  
* value 由0和1组成的string

方案二

* key格式
  instancename^db^b^keyname^index  
* value 8进制或16进制数字 通过计算获得

### HyperLogLogs

### Streams

# ToDo

* 如何scan？rawkv and txn
如何实现prefix scan