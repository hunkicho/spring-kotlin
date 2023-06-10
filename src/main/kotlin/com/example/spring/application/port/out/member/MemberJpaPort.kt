package com.example.spring.application.port.out.member

import com.example.spring.adapter.jpa.member.entity.MemberJpaEntity
import com.example.spring.domain.member.Member

interface MemberJpaPort {
    /**
     * Member 찾기
     * */
    fun findMemberById(id: String): MemberJpaEntity?

    /**
     * Member 찾기
     * */
    fun findMemberByMemberId(memberId: Int): Member?

    /**
     * Member 등록
     * */
    fun registerMember(member: Member): Member?

    /**
     * ID,PW 확인
     * */
    fun checkAuth(member: Member): Member?

    /**
     * Refresh 토큰 저장
     * */
    fun saveRefreshToken(id: String, token: String)

    /**
     * Member 찾기
     * */
    fun findMemberByRefreshToken(token: String): MemberJpaEntity
}