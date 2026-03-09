# Movie MCP Server - 电影MCP服务器

这是FilmComment项目的AI辅助组件，基于Model Context Protocol (MCP)提供TMDB电影API的标准化接口。

## 🎯 功能特性

- **TMDB API集成** - 支持电影搜索、详情、热门电影、按类型搜索、演员信息
- **环境配置管理** - 模仿Spring Boot的配置管理方式
- **模拟数据模式** - API密钥未配置时自动使用模拟数据
- **MCP协议兼容** - 符合Model Context Protocol标准

## ⚡ 快速配置

### 1. 安装依赖
```bash
npm install
```

### 2. 配置环境变量
复制`.env.example`为`.env`并填入TMDB API密钥：

```env
# TMDB API配置 (申请地址: https://www.themoviedb.org/settings/api)
TMDB_API_KEY=your_actual_tmdb_api_key_here

# 可选配置
SERVER_PORT=3000
LOG_LEVEL=info
```

### 3. 运行服务器
```bash
npm run dev  # 开发模式
npm run build && npm start  # 生产模式
```

## 🔧 可用工具

服务器提供以下MCP工具：

- `tmdb_search_movies` - 搜索电影
- `tmdb_movie_details` - 获取电影详情
- `tmdb_popular_movies` - 获取热门电影
- `tmdb_movies_by_genre` - 按类型搜索电影
- `tmdb_actor_info` - 获取演员信息

## 📁 项目结构

```
movie-mcp-server/
├── src/
│   ├── config/          # 配置管理
│   ├── tools/           # 工具定义
│   └── server.ts        # 主服务器
├── .env.example         # 环境配置模板
└── package.json
```

## 🔗 与FilmComment集成

此MCP服务器为FilmComment项目提供AI辅助功能，通过MCP协议与主应用进行通信。

---

**详细使用说明请参考主项目文档：**[FilmComment README](../README.md)