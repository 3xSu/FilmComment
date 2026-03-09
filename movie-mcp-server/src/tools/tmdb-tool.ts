import { Tool } from "@modelcontextprotocol/sdk/types.js";

/**
 * TMDB电影搜索工具
 * 通过TMDB API搜索电影信息，获取最新电影数据
 */
export const tmdbSearchMovies: Tool = {
    name: "tmdb_search_movies",
    description: "通过TMDB API搜索电影信息，获取最新电影数据",
    inputSchema: {
        type: "object",
        properties: {
            query: { 
                type: "string", 
                description: "搜索关键词，可以是电影名称、演员、导演等" 
            },
            year: { 
                type: "number", 
                description: "发行年份，用于精确搜索" 
            },
            language: { 
                type: "string", 
                description: "语言代码，默认zh-CN" 
            },
            page: {
                type: "number",
                description: "页码，用于分页查询"
            }
        },
        required: ["query"]
    }
};

/**
 * TMDB电影详情工具
 * 获取电影的详细信息，包括演员、导演、评分、简介等
 */
export const tmdbMovieDetails: Tool = {
    name: "tmdb_movie_details",
    description: "获取电影的详细信息，包括演员、导演、评分、简介等",
    inputSchema: {
        type: "object",
        properties: {
            movieId: { 
                type: "string", 
                description: "TMDB电影ID" 
            },
            language: { 
                type: "string", 
                description: "语言代码，默认zh-CN" 
            }
        },
        required: ["movieId"]
    }
};

/**
 * TMDB热门电影工具
 * 获取当前热门电影列表
 */
export const tmdbPopularMovies: Tool = {
    name: "tmdb_popular_movies",
    description: "获取当前热门电影列表",
    inputSchema: {
        type: "object",
        properties: {
            language: { 
                type: "string", 
                description: "语言代码，默认zh-CN" 
            },
            page: {
                type: "number",
                description: "页码，用于分页查询"
            }
        }
    }
};

/**
 * TMDB电影类型工具
 * 根据电影类型搜索电影
 */
export const tmdbMoviesByGenre: Tool = {
    name: "tmdb_movies_by_genre",
    description: "根据电影类型搜索电影",
    inputSchema: {
        type: "object",
        properties: {
            genreId: { 
                type: "number", 
                description: "电影类型ID，如28(动作)、35(喜剧)等" 
            },
            language: { 
                type: "string", 
                description: "语言代码，默认zh-CN" 
            },
            page: {
                type: "number",
                description: "页码，用于分页查询"
            }
        },
        required: ["genreId"]
    }
};

/**
 * TMDB演员信息工具
 * 获取演员的详细信息及其参演电影
 */
export const tmdbActorInfo: Tool = {
    name: "tmdb_actor_info",
    description: "获取演员的详细信息及其参演电影",
    inputSchema: {
        type: "object",
        properties: {
            actorId: { 
                type: "string", 
                description: "TMDB演员ID" 
            },
            language: { 
                type: "string", 
                description: "语言代码，默认zh-CN" 
            }
        },
        required: ["actorId"]
    }
};