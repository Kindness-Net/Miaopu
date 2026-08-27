# 喵扑

喵扑是一款使用 Kotlin、Jetpack Compose 与 Miuix 构建的 Android 赛事评分客户端。当前收录虎扑有可用赛程的五项电竞赛事，以及 NBA、CBA / 中国篮球、WNBA、CUBAL、英超、足球世界杯、其他足球、网球、乒乓球、羽毛球、F1、斯诺克和排球，专注于赛程、选手评分和虎扑评论。

## 已实现

- 十八个有赛程项目的实时赛程、比分、状态、队标或选手头像和评分人数
- 体育项目会合并专项赛程与 `commonhotsports` 综合热门赛程，并按虎扑 `uniqueKey` 去重
- 赛事页支持按赛事、轮次、战队、选手或日期即时搜索当前项目赛程
- 可持久化的赛事订阅：首页与赛事页只展示用户选择的项目，至少保留一个订阅
- 首页优先从今天开始；今天无比赛时从最近的历史比赛日开始，前 2 天赛事仍保留在上方供回滑查看
- 首页固定标题、操作按钮与项目切换，仅滚动下方比赛列表
- 赛事页保留接口返回的完整赛程列表，并默认定位到当前或下一场
- 完整赛程页固定标题、项目切换与日期导航，仅滚动下方比赛列表
- 完整赛程首次进入以今天为起点并居中日期，进入比赛详情再返回会恢复项目、日期和滚动位置
- 对局评分树、局次、选手评分、热评和分页评论
- 比赛详情参考虎扑赛事评分页的信息层级，只展示可进入的局次卡片；单局详情页独立展示横向选手列表
- BO1 的扁平选手评分结构会直接进入“全场”详情，不再显示选局页
- 原生最亮评论优先展示，普通评论随滚动稳定异步追加并显示点赞数
- 评论卡支持同时原地展开多组完整子回复，每组使用独立的 `getMore` 游标异步分页
- 选手详情参考虎扑原页：选手标签、真实星级分布、亮回复、全部回复、评论图片与回复预览
- 原生 1–5 星评分（按虎扑接口的 2/4/6/8/10 分提交）
- 原生发表评论，发布成功后自动刷新评论列表
- 虎扑官方网页登录；Cookie 通过 Android Keystore 的 AES-GCM 加密后保存在本机
- 网页登录入口只保留在“我的”，赛事与评论页不跳转虎扑网页
- “我的”支持通过 GitHub Release 检查更新，并可进入项目仓库查看关于信息
- 按 Miuix 操作层级统一主按钮、次要按钮与点击卡片，补齐提交/禁用状态、按压反馈和触控语义
- 使用虎扑红渐变与白色猫爪的原生自适应启动图标
- 系统深色模式、空态、加载态、错误态和登录失效状态
- R8 代码压缩和资源裁剪，减少复杂列表滑动时的额外开销

## 数据协议

当前版本根据虎扑官方网页在 2026-08-27 使用的公开协议实现：

- 赛程：`match-api.hupu.com/.../getScheduleListByTagForH5`（专项 `businessId` + `commonhotsports` 补源）
- 评分树：`games.mobileapi.hupu.com/1/8.2.99/.../getCurAndSubNodeByBizKey`
- 最亮评论：`games.mobileapi.hupu.com/1/8.2.99/.../primarySingleRow/hottest`
- 全部评论：`games.mobileapi.hupu.com/1/8.2.99/.../primarySingleRow`（游标分页）
- 子回复展开：`games.mobileapi.hupu.com/1/8.2.99/.../primarySingleRow/getMore`（父评论 ID + 游标分页）
- 打分：`games.mobileapi.hupu.com/.../score/save`
- 发表评论：`games.mobileapi.hupu.com/.../comment/m/publish`

这些是虎扑网页当前使用的接口，不是稳定的第三方 SDK 合约，虎扑可随时调整字段或风控。适配器会把失败统一映射为明确状态，不会把失败写操作显示为成功。

## 构建

要求 JDK 21、Android SDK 37、Build Tools 37.0.0。项目固定使用：

- Android Gradle Plugin 9.3.1
- Kotlin 2.4.10
- Gradle 9.6.1
- Miuix 0.9.4-rc01

```bash
./gradlew testDebugUnitTest assembleRelease
```

测试分发 APK 位于 `app/build/outputs/apk/release/app-release.apk`。当前 release 变体为方便覆盖安装使用调试证书签名，正式发布前必须替换为独立的生产签名。

### 自动发版

推送与 `app/build.gradle.kts` 中 `versionName` 相同的版本标签（例如 `1.0.2`）后，GitHub Actions 会安装 JDK 21、Android SDK 37 与 Build Tools 37.0.0，执行单元测试和 release APK 编译，然后上传 APK 与 SHA-256 校验文件到 GitHub Release。该流程只使用仓库自带的 `GITHUB_TOKEN`，无需额外配置 Secrets。

发版前先更新 `versionCode` 和 `versionName`，提交后创建并推送标签：

```bash
git tag 1.0.2
git push origin 1.0.2
```

## 安全说明

- 登录页只允许 HTTPS 的 `hupu.com` 与 `hoopchina.com.cn` 官方域名导航。
- App 不保存账号或密码，不注入登录表单，不绕过验证码。
- Cookie 不写日志，退出登录会同时清除 WebView Cookie 与加密副本。
- 不支持明文 HTTP 网络请求。

本项目与虎扑官方无隶属关系。发布或分发前请确认虎扑服务条款、商标使用与数据授权要求。

## 项目仓库

源码与 Release：https://github.com/KiritoXDone/Miaopu
