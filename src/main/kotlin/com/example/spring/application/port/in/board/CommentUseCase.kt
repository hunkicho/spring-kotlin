package com.example.spring.application.port.`in`.board

import com.example.spring.domain.board.Comment

interface CommentUseCase {
    /**
     * 댓글 작성
     * */
    fun createComment(commend: Commend.CreateCommend): Comment

    /**
     * 최상위 레벨 댓글 조회
     * */
    fun readTopLevelComment(commend: Commend.ReadTopLevelCommend): Pair<List<Comment>, Int?>

    /**
     * 댓글 조회
     * */
    fun readComment(commend: Commend.ReadCommend): Comment

    /**
     * 대댓글 조회
     * */
    fun readChildComment(commend: Commend.ReadChildCommend): Pair<List<Comment>, Int?>

    /**
     * 댓글 수정
     * */
    fun updateComment(commend: Commend.UpdateCommend): Comment

    /**
     * 댓글 좋아요
     * */
    fun likeComment(commend: Commend.LikeCommend): Comment

    /**
     * 댓글 좋아요 취소
     * */
    fun deleteLikeComment(commend: Commend.LikeCommend): Comment

    /**
     * 댓글 삭제
     * */
    fun deleteComment(commend: Commend.DeleteCommend)

    /**
     * 댓글 여러개 삭제
     * */
    fun deleteCommentAll(commend: Commend.DeleteAllCommend)

    sealed class Commend {
        abstract val boardId: Int
        abstract val postId: Int

        data class CreateCommend(
            override val boardId: Int,
            override val postId: Int,
            val parentCommentId: Int?,
            val level: Int,
            val content: String,
            val writer: String
        ) : Commend()

        data class ReadTopLevelCommend(
            override val boardId: Int,
            override val postId: Int,
            val size: Int,
            val cursor: Int?,
            val orderBy: String,
            val reader: String
        ) : Commend()

        data class ReadChildCommend(
            override val boardId: Int,
            override val postId: Int,
            val parentCommentId: Int,
            val size: Int,
            val cursor: Int?,
            val reader: String
        ) : Commend()

        data class ReadCommend(
            override val boardId: Int,
            override val postId: Int,
            val commentId: Int,
            val reader: String
        ) : Commend()

        data class UpdateCommend(
            override val boardId: Int,
            override val postId: Int,
            val commentId: Int,
            val content: String,
            val modifier: String
        ) : Commend()

        data class LikeCommend(
            override val boardId: Int,
            override val postId: Int,
            val commentId: Int,
            val email: String
        ) : Commend()

        data class DeleteCommend(
            override val boardId: Int,
            override val postId: Int,
            val commentId: Int,
            val modifier: String
        ) : Commend()

        data class DeleteAllCommend(
            override val boardId: Int,
            override val postId: Int
        ) : Commend()

    }
}