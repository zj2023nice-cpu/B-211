# 211 Grade Management System (学生成绩管理系统)

这是一个基于现代 Web 技术栈构建的学生成绩管理系统，支持多角色（管理员、教师、班主任、学生）权限管理，提供成绩录入、查询、统计分析等功能。

## 🛠 技术栈

- **Frontend**: Vue 3 + Vite + Element Plus + Pinia + ECharts
- **Backend**: Java Spring Boot
- **Database**: MySQL 8.0
- **Infrastructure**: Docker & Docker Compose

## 🚀 快速启动 (Docker)

确保您的开发环境中已安装并运行 **Docker Desktop**。

1. **克隆或下载项目代码**
   ```bash
   git clone <repository_url>
   cd 211GradeManagement
   ```

2. **启动服务**
   在项目根目录下执行以下命令：
   ```bash
   docker compose up --build
   ```
   *注意：首次构建可能需要几分钟时间下载依赖和镜像。*

3. **访问应用**
   等待控制台显示服务启动成功后，即可访问系统。

## 🔌 服务地址

| 服务名称 | 地址 | 说明 |
| :--- | :--- | :--- |
| **前端页面** | http://localhost:3001 | 系统主入口 |
| **后端 API** | http://localhost:8080 | API 接口服务 |
| **API 文档** | http://localhost:8080/swagger-ui.html | (如果后端集成了Swagger) |

## 🧪 测试账号

系统初始化时已预置以下测试账号（密码均为 `123456`）：

| 角色 | 用户名 | 密码 | 说明 |
| :--- | :--- | :--- | :--- |
| **管理员** | `admin` | `123456` | 拥有所有权限，管理用户、课程 |
| **任课教师** | `teacher1` | `123456` | 仅管理自己教授课程的成绩 |
| **班主任** | `teacher2` | `123456` | 管理本班级（三年二班）所有学生成绩 |
| **学生** | `student1` | `123456` | 查询个人成绩 |

## 📸 功能介绍

- **多角色权限控制**：
  - **管理员**：用户管理、课程管理、全局成绩管理。
  - **任课教师**：录入、修改、查询所授课程成绩。
  - **班主任**：查询本班级学生所有课程成绩，进行班级成绩分析。
  - **学生**：查询个人成绩、查看成绩分布。

- **成绩管理**：
  - 支持按学期、课程、学生筛选。
  - 支持**批量录入模式**，提高录入效率。
  - 自动计算补考状态。

- **数据可视化**：
  - 首页仪表盘展示关键指标。
  - 成绩查询页提供成绩区间分布饼图（ECharts）。

- **现代化 UI**：
  - 响应式设计，适配不同屏幕尺寸。
  - 柔和的渐变色主题与交互动画。

## 🔧 工程化质量保障

### 统一命令入口

项目根目录提供了统一的命令入口（`package.json`），无需切换目录即可执行常用操作：

```bash
# 安装依赖（首次使用）
npm install

# 启动前端开发服务器
npm run dev

# 构建前端项目
npm run build:frontend

# 运行后端单元测试
npm run test:backend

# 构建后端项目（跳过测试）
npm run build:backend

# 整体构建（前端 + 后端）
npm run build

# 运行所有质量检查（前端构建 + 后端测试）
npm run verify

# Docker 相关
npm run docker:up    # 启动所有服务
npm run docker:down  # 停止所有服务
```

### CI/CD 持续集成

项目配置了 GitHub Actions 自动流水线，每次提交代码或提交 Pull Request 时会自动执行：

1. **前端构建检查**：确保前端代码可以正常编译
2. **后端单元测试**：运行所有后端单元测试，保证代码质量

触发条件：
- `push` 到 `main` 或 `master` 分支
- 任何 `pull_request` 提交

工作流配置文件位于：`.github/workflows/ci.yml`

## 🔍 Verification - 基本验证方式

1. **服务健康检查**：
   访问 `http://localhost:3001`，应能看到登录页面。

2. **登录验证**：
   使用管理员账号 (`admin` / `123456`) 登录，应能跳转至仪表盘页面，并看到系统概览数据。

3. **数据库连接验证**：
   如果能成功登录并看到数据，说明后端与 MySQL 数据库连接正常。

4. **工程化验证**：
   执行 `npm run verify`，确保前端可构建、后端测试全部通过。
