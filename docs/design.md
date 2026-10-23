# 设计与实现

Blue Glass Home 使用纯 Java 原生 View，以 1280×720 逻辑坐标等比适配电视分辨率。静态背景、半透明卡片和统一线条图标构成首页；每张卡片都是真实可聚焦 View，避免仅靠绘制模拟遥控器焦点。

## 输入与应用

`DeviceCatalog` 在后台读取 TvInputManager 的物理 HDMI 端口与 CEC 子设备，以及 PackageManager 提供的电视和普通 Android 启动入口。输入按 HDMI 数字排序；首页按物理端口 ID 去重，全部输入列表仍保留每个 CEC 路由目标。

自定义输入名称以物理端口 ID 保存，不随 CEC 设备 ID 或连接状态变化。空白名称回退系统标签，过长名称按 Unicode 码点裁剪。连接状态只表示系统报告的连接/待机情况；Sony 全端口 connected 的可疑报告不当作真实视频信号确认。

输入切换优先交给 Sony 系统电视播放器，再尝试通用 passthrough URI；不可用时提供系统输入面板入口。收藏按包名保存，更换到已收藏应用时交换位置。

## 绘制与交互

`HomeLayout` 管理逻辑坐标、行列位置和方向导航；`GlassTile` 绘制焦点、按压、文字和矢量图标。`PressLogic` 保证短按、长按和取消互斥，同时兼容实体遥控器及 Android 10 注入按键行为。

顶部时钟按分钟更新；应用与输入枚举在后台线程执行，生命周期和请求代次保护防止过期结果覆盖新状态。输入变化通过 TvInputManager 回调刷新，不持续轮询。

## 壁纸

`WallpaperPalette` 提供原版蓝色玻璃与十款深色纯色；`WallpaperDialog` 提供四列可聚焦色板，方向导航可到达不满一行的末尾选项。

原版渐变着色器按尺寸缓存。纯色直接填充，不叠加渐变。图片居中裁剪并压暗，以保持浅色文字可读，背景不执行持续动画。

本地图片经系统或文件管理器返回的临时 URI 读取，不请求广泛存储权限。`WallpaperImage` 限制输入为 32 MB，后台解码至不超过 1920×1080，使用 AtomicFile 保存 JPEG 私有副本。Android 9+ 使用 ImageDecoder 处理图片方向与缩放；Android 8 使用 BitmapFactory 有界采样。显示中的 Bitmap 由 GC 管理，避免硬件绘制仍引用旧图时主动 recycle。

## 存储与权限

空间名称、收藏、输入名称与壁纸选项使用 SharedPreferences。壁纸副本存放在应用 files 目录，临时文件在 cache 目录，处理结束后清理。应用仅声明网络状态权限，不发送网络请求。

## 验证

JVM 测试覆盖导航边界、变长行映射、收藏去重、失效应用、CEC 目标保留、物理端口去重、数字顺序、名称裁剪及壁纸选择回退。实机验证范围见 [verification.md](verification.md)。
