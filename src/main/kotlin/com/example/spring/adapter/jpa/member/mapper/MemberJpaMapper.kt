package com.example.spring.adapter.jpa.member.mapper

import com.example.spring.adapter.jpa.member.entity.MemberJpaEntity
import com.example.spring.domain.member.Member
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper
interface MemberJpaMapper {
    companion object {
        val INSTANCE: MemberJpaMapper = Mappers.getMapper(MemberJpaMapper::class.java)
    }

    fun toMember(dto: MemberJpaEntity?): Member

    fun toEntity(member: Member): MemberJpaEntity
}