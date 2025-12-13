# FreezeApp
项目最早使用了WebSocket通信，后来发现[Shizuku](https://github.com/RikkaApps/Shizuku)的原理更简单。因此移植了[Shizuku](https://github.com/RikkaApps/Shizuku)中的部分代码。

## 已有能力
* APP冻结管理
* APP电池记录
* App使用记录
* APP权限管理
* APP实时监控
* APP存储管理
* APP粘贴板管理
* APP待机分桶
* APP休眠模式（白名单配置）
* APP外屏配置（小米 MIX FLIP）
* APP文件服务器(支持根目录和App内置沙盒目录)
* APP流量监控（Daemon进程监控移动数据流量超出设置的阈值）
* APP AutoGLM  将[Open-AutoGLM](https://github.com/zai-org/Open-AutoGLM) 翻译为了Java代码并进行了集成。