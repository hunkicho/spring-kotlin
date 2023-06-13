package com.example.spring.domain.board

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Board {
    var boardId: Int = 0
    var title: String = ""
    var content: String = ""
    var up: Int = 0
    var writer: String = ""
    var createdAt: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    var editedAt: String? = null
}