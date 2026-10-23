# Blue Glass Home Implementation Plan

Goal: 为索尼 X9000H 交付原生蓝色玻璃桌面 APK、完整源码及构建说明。

Architecture: DeviceCatalog 读取真实输入和应用；HomeLogic 负责纯逻辑；AmbientView、GlyphView、GlassTile 负责绘制；HomeLayout 和 MainActivity 负责排版、焦点与生命周期。

Tech Stack: Java 17 source、Android API 26+、compile/target 34、AGP 8.5.2、Gradle 8.9、JUnit 4。

- [x] HomeLogicTest 先覆盖左右边界、变长行映射、空行、收藏去重和失效包，确认失败。
- [x] 实现 HomeLogic 并通过测试。
- [x] DeviceCatalog 运行时枚举真实 HDMI/CEC 和应用，输入切换失败时提供系统面板，不虚构成功。
- [x] 原生背景、线条图标、半透明卡片、焦点与按下状态，以真实可聚焦 View 实现。
- [x] MainActivity 整合时钟、网络、全部输入/应用、长按收藏替换、客厅名称和默认桌面设置。
- [x] testReleaseUnitTest lintRelease assembleRelease 后真机安装与 HOME 验证。
- [x] APK、源码 ZIP、README、构建安装脚本及验证记录打包。

## 1.1.0 个性化更新

- [x] 先覆盖 HDMI 数字顺序、名称还原/Unicode 裁剪、纯色色板与无效选择回退，运行确认失败。
- [x] 按物理端口排序与去重；保留全部 CEC 入口；加入长按/设置改名及偏好保存。
- [x] 增加 10 款纯色、原版背景、本地图片选择与有界私有保存；色板支持遥控器操作。
- [x] 独立只读审查，修复多 CEC 卡片挤占和最后一行焦点问题。
- [x] 重新构建及实机验证壁纸、改名、持久化和默认 HOME。
- [x] 更新 APK、源码、截图、校验值及验证记录。
