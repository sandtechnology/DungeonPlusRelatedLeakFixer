# DungeonPlusRelatedLeakFixer

修复因DungeonPlus的镜像世界特性而带来的其他插件的内存泄漏问题，目前支持MythicMobs和AttitudePlus

### 原理？
在卸载不会再使用的镜像副本世界时删除对应插件内的实体强引用，确保世界实例被正常GC
### 下载？
Release页面下载
### 贡献？
直接PR即可
### 新插件支持/BUG？
请提Issue，附上详细截图和详细的信息
