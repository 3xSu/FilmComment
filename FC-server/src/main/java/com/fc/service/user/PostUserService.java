package com.fc.service.user;

import com.fc.dto.post.CollectionPageQueryDTO;
import com.fc.dto.post.PostPublishDTO;
import com.fc.result.PageResult;
import com.fc.vo.post.PostImageUploadVO;
import com.fc.vo.post.PostVO;
import com.fc.vo.post.PostVideoUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface PostUserService {

    /**
     * 发布帖子
     * @param postPublishDTO 帖子发布信息
     * @return 帖子信息
     */
    PostVO publishPost(PostPublishDTO postPublishDTO);

    /**
     * 点赞帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    void likePost(Long userId, Long postId);

    /**
     * 取消点赞帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    void unlikePost(Long userId, Long postId);

    /**
     * 检查用户是否点赞过帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否点赞
     */
    boolean checkUserLikedPost(Long userId, Long postId);

    /**
     * 收藏帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    void collectPost(Long userId, Long postId);

    /**
     * 取消收藏帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    void uncollectPost(Long userId, Long postId);

    /**
     * 检查用户是否收藏过帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否收藏
     */
    boolean checkUserCollectedPost(Long userId, Long postId);

    /**
     * 获取用户收藏的帖子列表（分页）
     * @param userId 用户ID
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    PageResult getUserCollections(Long userId, CollectionPageQueryDTO pageQueryDTO);

    /**
     * 删除帖子（逻辑删除）
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    void deletePost(Long userId, Long postId);

    /**
     * 上传帖子图片
     * @param userId 用户ID
     * @param postId 帖子ID
     * @param imageFile 图片文件
     * @param sortOrder 图片排序
     * @return 图片上传信息
     */
    PostImageUploadVO uploadPostImage(Long userId, Long postId, MultipartFile imageFile, Integer sortOrder);

    /**
     * 上传帖子视频
     * @param userId 用户ID
     * @param postId 帖子ID
     * @param videoFile 视频文件
     * @return 视频上传信息
     */
    PostVideoUploadVO uploadPostVideo(Long userId, Long postId, MultipartFile videoFile);
}