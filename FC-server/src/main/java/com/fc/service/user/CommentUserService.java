package com.fc.service.user;

import com.fc.dto.comment.CommentPublishDTO;
import com.fc.vo.comment.CommentImageUploadVO;
import com.fc.vo.comment.CommentVO;
import org.springframework.web.multipart.MultipartFile;

public interface CommentUserService {

    /**
     * 发表评论
     * @param commentPublishDTO 评论信息
     * @return 评论信息
     */
    CommentVO publishComment(CommentPublishDTO commentPublishDTO);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @return 删除结果
     */
    void deleteComment(Long commentId);


    /**
     * 上传评论图片
     * @param commentId 评论ID
     * @param imageFile 图片文件
     * @param sortOrder 图片排序
     * @return 图片上传信息
     */
    CommentImageUploadVO uploadCommentImage(Long commentId, MultipartFile imageFile, Integer sortOrder);
}