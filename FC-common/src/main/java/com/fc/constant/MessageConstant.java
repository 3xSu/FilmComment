package com.fc.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    // 登录相关
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String LOGIN_FAILED = "登录失败";

    // 注册相关
    public static final String REGISTER_SUCCESS = "注册成功";
    public static final String REGISTER_FAILED = "注册失败";
    public static final String USERNAME_ALREADY_EXISTS = "用户名已存在";

    // 用户相关
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";
    public static final String PASSWORD_EDIT_SUCCESS = "密码修改成功";
    public static final String PROFILE_UPDATE_SUCCESS = "个人信息更新成功";
    public static final String PROFILE_UPDATE_FAILED = "个人信息更新失败";

    // 电影相关
    public static final String MOVIE_ALREADY_EXISTS = "电影已存在";
    public static final String MOVIE_NOT_FOUND = "电影不存在";
    public static final String MOVIE_RELATION_MARKED = "标记成功";
    public static final String MOVIE_RELATION_UNMARKED = "取消标记成功";

    // 帖子相关
    public static final String POST_NOT_FOUND = "帖子不存在";
    public static final String POST_TYPE_REQUIRED = "帖子类型不能为空";
    public static final String POST_TYPE_INVALID = "非法的帖子类型";
    public static final String POST_PUBLISH_SUCCESS = "发布成功";
    public static final String POST_PUBLISH_FAILED = "发布失败";

    // 收藏相关
    public static final String FAVORITE_ADD_SUCCESS = "收藏成功";
    public static final String FAVORITE_REMOVE_SUCCESS = "取消收藏成功";
    public static final String FAVORITE_ALREADY_EXISTS = "已收藏该电影";

    // 评论相关
    public static final String COMMENT_ADD_SUCCESS = "评论成功";
    public static final String COMMENT_DELETE_SUCCESS = "评论删除成功";
    public static final String COMMENT_NOT_FOUND = "评论不存在";

    // 文件上传
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String UPLOAD_SUCCESS = "文件上传成功";
    public static final String FILE_IS_EMPTY = "文件不能为空";
    public static final String FILE_NAME_INVALID = "文件名无效";
    public static final String FILE_TYPE_NOT_SUPPORTED = "文件类型不支持，仅支持jpg、jpeg、png格式";
    public static final String FILE_SIZE_EXCEEDED = "文件大小不能超过2MB";
    public static final String FILE_UPLOAD_FAILED = "文件上传失败";

    //评分相关
    public static final String RATING_SUBMIT_SUCCESS = "评分提交成功";
    public static final String RATING_DELETE_SUCCESS = "评分删除成功";
    public static final String RATING_ALREADY_EXISTS = "您已经对该电影评过分了";

    // 通用
    public static final String OPERATION_SUCCESS = "操作成功";
    public static final String OPERATION_FAILED = "操作失败";
    public static final String PARAMETER_ERROR = "参数错误";
    public static final String ACCESS_DENIED = "无访问权限";
    public static final String SYSTEM_BUSY = "系统繁忙，请稍后再试";
    public static final String ALREADY_EXISTS = "已存在";
    public static final String SEARCH_SUCCESS = "搜索成功";
    public static final String SEARCH_FAILED = "搜索失败";
}
