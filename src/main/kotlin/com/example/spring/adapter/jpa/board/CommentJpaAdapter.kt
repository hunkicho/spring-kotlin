package com.example.spring.adapter.jpa.board

import com.example.spring.adapter.jpa.board.mapper.CommentJpaMapper
import com.example.spring.adapter.jpa.board.repository.CommentJpaRepository
import com.example.spring.application.port.out.board.CommentJpaPort
import com.example.spring.domain.board.Comment
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class CommentJpaAdapter(
    private val commentJpaRepository: CommentJpaRepository,
    private val commentJpaMapper: CommentJpaMapper
) : CommentJpaPort {

    override fun createComment(comment: Comment): Comment =
        commentJpaMapper.toComment(commentJpaRepository.save(commentJpaMapper.toJpaEntity(comment)))

    override fun readComment(boardId: Int, postId: Int, commentId: Int): Comment? =
        commentJpaRepository.findByIdOrNull(commentId)
            ?.let {
                commentJpaMapper.toComment(it)
            }


    override fun updateComment(comment: Comment): Comment =
        commentJpaMapper.toComment(commentJpaRepository.save(commentJpaMapper.toJpaEntity(comment)))

    override fun deleteComment(boardId: Int, postId: Int, commentId: Int) =
        commentJpaRepository.deleteById(commentId)
}