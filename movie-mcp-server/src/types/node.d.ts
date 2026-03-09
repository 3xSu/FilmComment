// Node.js模块类型声明
// 解决TypeScript中Node.js内置模块的类型问题

declare module 'http' {
    import * as http from 'http';
    export = http;
}

declare module 'url' {
    import * as url from 'url';
    export = url;
}

declare module 'querystring' {
    import * as querystring from 'querystring';
    export = querystring;
}

// 全局类型声明
declare global {
    namespace NodeJS {
        interface ProcessEnv {
            NODE_ENV?: 'development' | 'production' | 'test';
            PORT?: string;
        }
    }
}