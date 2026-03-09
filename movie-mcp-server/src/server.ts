// 电影MCP服务器实现
// 提供TMDB和豆瓣电影API的MCP工具接口

import axios from 'axios';
import { config } from './config/config.js';

// 导入工具定义
import { 
    tmdbSearchMovies, 
    tmdbMovieDetails, 
    tmdbPopularMovies, 
    tmdbMoviesByGenre, 
    tmdbActorInfo 
} from "./tools/tmdb-tool.js";

interface ToolRequest {
    name: string;
    arguments: any;
}

interface ToolResponseSuccess {
    content: Array<{
        type: string;
        text: string;
    }>;
}

interface ToolResponseError {
    error: {
        code: string;
        message: string;
    };
}

type ToolResponse = ToolResponseSuccess | ToolResponseError;

class MovieMCP {
    
    constructor() {
        try {
            config.validate();
            console.log("Movie MCP Server initialized with configuration:");
            console.log(JSON.stringify(config.getSummary(), null, 2));
        } catch (error: any) {
            console.warn("配置验证警告:", error.message);
            console.log("服务器将以模拟数据模式运行，请配置API密钥以启用真实API调用");
        }
    }
    
    /**
     * 处理工具调用请求
     */
    public async handleToolCall(request: ToolRequest): Promise<ToolResponse> {
        const { name, arguments: args } = request;
        
        try {
            let result: any;
            
            switch (name) {
                case "tmdb_search_movies":
                    result = await this.searchTMDBMovies(args);
                    break;
                case "tmdb_movie_details":
                    result = await this.getTMDBMovieDetails(args);
                    break;
                case "tmdb_popular_movies":
                    result = await this.getTMDBPopularMovies(args);
                    break;
                case "tmdb_movies_by_genre":
                    result = await this.getTMDBMoviesByGenre(args);
                    break;
                case "tmdb_actor_info":
                    result = await this.getTMDBActorInfo(args);
                    break;
                default:
                    return {
                        error: {
                            code: "METHOD_NOT_FOUND",
                            message: `Unknown tool: ${name}`
                        }
                    };
            }
            
            return {
                content: [{
                    type: "text",
                    text: JSON.stringify(result)
                }]
            };
            
        } catch (error: any) {
            return {
                error: {
                    code: "INTERNAL_ERROR",
                    message: `Tool execution failed: ${error.message || error}`
                }
            };
        }
    }
    
    /**
     * 获取可用工具列表
     */
    public getAvailableTools(): any[] {
        return [
            tmdbSearchMovies,
            tmdbMovieDetails,
            tmdbPopularMovies,
            tmdbMoviesByGenre,
            tmdbActorInfo
        ];
    }
    
    private async searchTMDBMovies(args: any): Promise<any> {
        const { query, year, language = "zh-CN", page = 1 } = args;
        
        // 检查API密钥是否配置
        if (config.tmdb.apiKey === 'YOUR_TMDB_API_KEY_HERE') {
            return this.getMockTMDBData('search', { query, year, page });
        }
        
        try {
            const url = `${config.tmdb.baseUrl}/search/movie`;
            const params = new URLSearchParams({
                api_key: config.tmdb.apiKey,
                query: encodeURIComponent(query),
                language: language,
                page: page.toString()
            });
            
            if (year) {
                params.append('year', year.toString());
            }
            
            const response = await axios.get(`${url}?${params.toString()}`, {
                timeout: config.timeout.request
            });
            
            return {
                source: "tmdb",
                query: query,
                page: page,
                results: response.data.results,
                total_results: response.data.total_results,
                total_pages: response.data.total_pages
            };
            
        } catch (error: any) {
            console.error('TMDB搜索API调用失败:', error.message);
            return this.getMockTMDBData('search', { query, year, page });
        }
    }
    
    private async getTMDBMovieDetails(args: any): Promise<any> {
        const { movieId, language = "zh-CN" } = args;
        
        // 检查API密钥是否配置
        if (config.tmdb.apiKey === 'YOUR_TMDB_API_KEY_HERE') {
            return this.getMockTMDBData('details', { movieId });
        }
        
        try {
            const url = `${config.tmdb.baseUrl}/movie/${movieId}`;
            const params = new URLSearchParams({
                api_key: config.tmdb.apiKey,
                language: language,
                append_to_response: 'credits'
            });
            
            const response = await axios.get(`${url}?${params.toString()}`, {
                timeout: config.timeout.request
            });
            
            return response.data;
            
        } catch (error: any) {
            console.error('TMDB电影详情API调用失败:', error.message);
            return this.getMockTMDBData('details', { movieId });
        }
    }
    
    private async getTMDBPopularMovies(args: any): Promise<any> {
        const { language = "zh-CN", page = 1 } = args;
        
        // 检查API密钥是否配置
        if (config.tmdb.apiKey === 'YOUR_TMDB_API_KEY_HERE') {
            return this.getMockTMDBData('popular', { page });
        }
        
        try {
            const url = `${config.tmdb.baseUrl}/movie/popular`;
            const params = new URLSearchParams({
                api_key: config.tmdb.apiKey,
                language: language,
                page: page.toString()
            });
            
            const response = await axios.get(`${url}?${params.toString()}`, {
                timeout: config.timeout.request
            });
            
            return response.data;
            
        } catch (error: any) {
            console.error('TMDB热门电影API调用失败:', error.message);
            return this.getMockTMDBData('popular', { page });
        }
    }
    
    private async getTMDBMoviesByGenre(args: any): Promise<any> {
        const { genreId, language = "zh-CN", page = 1 } = args;
        
        // 检查API密钥是否配置
        if (config.tmdb.apiKey === 'YOUR_TMDB_API_KEY_HERE') {
            const genreNames: { [key: number]: string } = {
                28: "动作",
                35: "喜剧",
                18: "剧情",
                10749: "爱情"
            };
            
            return {
                genre_id: genreId,
                genre_name: genreNames[genreId] || "未知类型",
                page: page,
                results: [
                    {
                        id: "1",
                        title: `${genreNames[genreId] || "类型"}电影1`,
                        release_date: "2024-01-01",
                        vote_average: 7.5,
                        overview: `这是一部${genreNames[genreId] || "类型"}电影的简介`
                    }
                ],
                total_pages: 5,
                total_results: 100
            };
        }
        
        try {
            const url = `${config.tmdb.baseUrl}/discover/movie`;
            const params = new URLSearchParams({
                api_key: config.tmdb.apiKey,
                language: language,
                with_genres: genreId.toString(),
                page: page.toString()
            });
            
            const response = await axios.get(`${url}?${params.toString()}`, {
                timeout: config.timeout.request
            });
            
            return {
                genre_id: genreId,
                page: page,
                results: response.data.results,
                total_pages: response.data.total_pages,
                total_results: response.data.total_results
            };
            
        } catch (error: any) {
            console.error('TMDB按类型搜索API调用失败:', error.message);
            const genreNames: { [key: number]: string } = {
                28: "动作",
                35: "喜剧",
                18: "剧情",
                10749: "爱情"
            };
            
            return {
                genre_id: genreId,
                genre_name: genreNames[genreId] || "未知类型",
                page: page,
                results: [
                    {
                        id: "1",
                        title: `${genreNames[genreId] || "类型"}电影1`,
                        release_date: "2024-01-01",
                        vote_average: 7.5,
                        overview: `这是一部${genreNames[genreId] || "类型"}电影的简介`
                    }
                ],
                total_pages: 5,
                total_results: 100
            };
        }
    }
    
    private async getTMDBActorInfo(args: any): Promise<any> {
        const { actorId, language = "zh-CN" } = args;
        
        // 检查API密钥是否配置
        if (config.tmdb.apiKey === 'YOUR_TMDB_API_KEY_HERE') {
            return {
                id: actorId,
                name: `演员${actorId}`,
                biography: "这是演员的详细简介",
                birthday: "1980-01-01",
                place_of_birth: "北京，中国",
                known_for_department: "Acting",
                popularity: 8.5,
                movie_credits: {
                    cast: [
                        {
                            id: "1",
                            title: "电影A",
                            character: "主角",
                            release_date: "2024-01-01"
                        },
                        {
                            id: "2",
                            title: "电影B",
                            character: "配角",
                            release_date: "2024-02-01"
                        }
                    ]
                }
            };
        }
        
        try {
            const url = `${config.tmdb.baseUrl}/person/${actorId}`;
            const params = new URLSearchParams({
                api_key: config.tmdb.apiKey,
                language: language,
                append_to_response: 'movie_credits'
            });
            
            const response = await axios.get(`${url}?${params.toString()}`, {
                timeout: config.timeout.request
            });
            
            return response.data;
            
        } catch (error: any) {
            console.error('TMDB演员信息API调用失败:', error.message);
            return {
                id: actorId,
                name: `演员${actorId}`,
                biography: "这是演员的详细简介",
                birthday: "1980-01-01",
                place_of_birth: "北京，中国",
                known_for_department: "Acting",
                popularity: 8.5,
                movie_credits: {
                    cast: [
                        {
                            id: "1",
                            title: "电影A",
                            character: "主角",
                            release_date: "2024-01-01"
                        },
                        {
                            id: "2",
                            title: "电影B",
                            character: "配角",
                            release_date: "2024-02-01"
                        }
                    ]
                }
            };
        }
    }
    
    /**
     * 获取模拟TMDB数据（当API密钥未配置时使用）
     */
    private getMockTMDBData(type: string, args: any): any {
        switch (type) {
            case 'search':
                const { query, year, page: searchPage = 1 } = args;
                return {
                    source: "tmdb",
                    query: query,
                    page: searchPage,
                    results: [
                        {
                            id: "12345",
                            title: `搜索结果：${query}`,
                            release_date: year ? `${year}-01-01` : "2024-01-01",
                            vote_average: 7.5,
                            overview: `这是关于"${query}"的搜索结果`,
                            genre_ids: [28, 12],
                            poster_path: "/path/to/poster.jpg"
                        }
                    ],
                    total_results: 1,
                    total_pages: 1
                };
            
            case 'details':
                const { movieId } = args;
                return {
                    id: movieId,
                    title: `电影详情 - ID: ${movieId}`,
                    overview: "这是一部电影的详细描述，包含剧情、演员、导演等信息",
                    release_date: "2024-01-01",
                    runtime: 120,
                    genres: [
                        { id: 28, name: "动作" },
                        { id: 12, name: "冒险" }
                    ],
                    vote_average: 7.5,
                    vote_count: 1000,
                    production_companies: [
                        { name: "华纳兄弟", id: 123 }
                    ],
                    credits: {
                        cast: [
                            { name: "演员A", character: "主角" },
                            { name: "演员B", character: "配角" }
                        ],
                        crew: [
                            { name: "导演A", job: "导演" }
                        ]
                    }
                };
            
            case 'popular':
                const { page: popularPage = 1 } = args;
                return {
                    page: popularPage,
                    results: [
                        {
                            id: "1",
                            title: "热门电影1",
                            release_date: "2024-01-01",
                            vote_average: 8.0,
                            overview: "热门电影1的简介",
                            genre_ids: [28, 12]
                        },
                        {
                            id: "2",
                            title: "热门电影2",
                            release_date: "2024-02-01",
                            vote_average: 7.8,
                            overview: "热门电影2的简介",
                            genre_ids: [35, 18]
                        }
                    ],
                    total_pages: 10,
                    total_results: 200
                };
            
            default:
                return { error: "未知的模拟数据类型" };
        }
    }


    
    /**
     * 启动HTTP服务器
     */
    public async start(port: number = 8081): Promise<void> {
        // 创建简单的HTTP服务器
        const http = await import('http');
        
        const server = http.createServer(async (req: any, res: any) => {
            if (req.method === 'POST' && req.url === '/tools/call') {
                let body = '';
                req.on('data', (chunk: any) => {
                    body += chunk.toString();
                });
                
                req.on('end', async () => {
                    try {
                        const request = JSON.parse(body) as ToolRequest;
                        const response = await this.handleToolCall(request);
                        
                        res.writeHead(200, { 'Content-Type': 'application/json' });
                        res.end(JSON.stringify(response));
                    } catch (error: any) {
                        res.writeHead(400, { 'Content-Type': 'application/json' });
                        res.end(JSON.stringify({
                            error: {
                                code: 'BAD_REQUEST',
                                message: 'Invalid request format'
                            }
                        }));
                    }
                });
            } else if (req.method === 'GET' && req.url === '/tools/list') {
                const tools = this.getAvailableTools();
                
                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ tools }));
            } else {
                res.writeHead(404, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({
                    error: {
                        code: 'NOT_FOUND',
                        message: 'Endpoint not found'
                    }
                }));
            }
        });
        
        server.listen(port, () => {
            console.log(`Movie MCP Server started on port ${port}`);
            console.log('Available endpoints:');
            console.log(`  POST http://localhost:${port}/tools/call`);
            console.log(`  GET  http://localhost:${port}/tools/list`);
        });
        
        return new Promise((resolve) => {
            server.on('listening', () => {
                resolve();
            });
        });
    }
}

// 启动服务器
const server = new MovieMCP();
server.start().catch(console.error);