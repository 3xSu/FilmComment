// 电影MCP服务器配置管理
// 模仿Spring Boot的配置管理方式

import dotenv from 'dotenv';

// 加载环境变量
dotenv.config();

/**
 * 服务器配置接口
 */
interface ServerConfig {
  port: number;
  host: string;
}

/**
 * TMDB API配置接口
 */
interface TMDBConfig {
  apiKey: string;
  baseUrl: string;
  language: string;
}

/**
 * 缓存配置接口
 */
interface CacheConfig {
  ttl: number;
  enabled: boolean;
}

/**
 * 请求限制配置接口
 */
interface RateLimitConfig {
  requests: number;
  window: number;
}

/**
 * 超时配置接口
 */
interface TimeoutConfig {
  request: number;
  retryAttempts: number;
}

/**
 * 应用配置类
 */
export class AppConfig {
  // 服务器配置
  public readonly server: ServerConfig = {
    port: parseInt(process.env.SERVER_PORT || '3000'),
    host: process.env.SERVER_HOST || 'localhost'
  };

  // TMDB API配置
  public readonly tmdb: TMDBConfig = {
    apiKey: process.env.TMDB_API_KEY || 'YOUR_TMDB_API_KEY_HERE',
    baseUrl: 'https://api.themoviedb.org/3',
    language: 'zh-CN'
  };

  // 缓存配置
  public readonly cache: CacheConfig = {
    ttl: parseInt(process.env.CACHE_TTL || '3600'),
    enabled: process.env.CACHE_ENABLED === 'true'
  };

  // 请求限制配置
  public readonly rateLimit: RateLimitConfig = {
    requests: parseInt(process.env.RATE_LIMIT_REQUESTS || '100'),
    window: parseInt(process.env.RATE_LIMIT_WINDOW || '3600')
  };

  // 超时配置
  public readonly timeout: TimeoutConfig = {
    request: parseInt(process.env.REQUEST_TIMEOUT || '5000'),
    retryAttempts: parseInt(process.env.RETRY_ATTEMPTS || '3')
  };

  // 日志级别
  public readonly logLevel: string = process.env.LOG_LEVEL || 'info';

  /**
   * 验证配置是否有效
   */
  public validate(): void {
    const errors: string[] = [];

    // 检查TMDB API密钥
    if (this.tmdb.apiKey === 'YOUR_TMDB_API_KEY_HERE') {
      errors.push('TMDB API密钥未配置，请设置TMDB_API_KEY环境变量');
    }

    // 检查端口范围
    if (this.server.port < 1 || this.server.port > 65535) {
      errors.push(`服务器端口 ${this.server.port} 无效，应在1-65535范围内`);
    }

    if (errors.length > 0) {
      throw new Error(`配置验证失败:\n${errors.join('\n')}`);
    }
  }

  /**
   * 获取配置摘要信息（不包含敏感信息）
   */
  public getSummary(): object {
    return {
      server: {
        port: this.server.port,
        host: this.server.host
      },
      tmdb: {
        baseUrl: this.tmdb.baseUrl,
        language: this.tmdb.language,
        apiKeyConfigured: this.tmdb.apiKey !== 'YOUR_TMDB_API_KEY_HERE'
      },
      cache: this.cache,
      rateLimit: this.rateLimit,
      timeout: this.timeout,
      logLevel: this.logLevel
    };
  }
}

// 创建全局配置实例
export const config = new AppConfig();