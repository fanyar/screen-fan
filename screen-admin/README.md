# screen-admin

本地内容管理后台，基于 Java 21、Spring Boot 3.3、H2 文件数据库。

## 启动

```bash
./start-server.sh
```

服务地址：

- 管理后台：http://localhost:8666/admin/
- 公开接口：http://localhost:8666/api/works
- H2 控制台：http://localhost:8666/h2-console

默认账号：`admin`  
默认密码：`admin123`

## 数据

H2 数据文件保存在 `server/data/screen-admin.mv.db`，本机重启后数据会保留。首次启动会把 screen-web 工作台里的作品、漫画、电影、技术分享作为初始数据写入。

封面上传文件保存在 `server/data/uploads`。

## screen-web 联调

保持 Java 服务运行，然后启动 screen-web：

```bash
cd ../screen-web
npm run dev
```

访问 http://localhost:3000/#/platform/works ，工作台数据会从 Java 后台读取。
