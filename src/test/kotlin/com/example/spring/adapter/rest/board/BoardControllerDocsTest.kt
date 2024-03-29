package com.example.spring.adapter.rest.board

import com.epages.restdocs.apispec.*
import com.example.spring.application.port.out.board.BoardJpaPort
import com.example.spring.application.port.out.member.MemberJpaPort
import com.example.spring.application.service.board.exception.BoardDataNotFoundException
import com.example.spring.application.service.member.JwtService
import com.example.spring.domain.board.Board
import com.fasterxml.jackson.databind.ObjectMapper
import config.RestdocsTestDsl
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class BoardControllerDocsTest : RestdocsTestDsl {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var boardJpaPort: BoardJpaPort

    @Autowired
    private lateinit var memberJpaPort: MemberJpaPort

    @Test
    @Transactional
    fun createBoard() {
        val token = jwtService.createAccessToken(memberJpaPort.findMemberByEmail("test")!!)
        val input = mutableMapOf<String, String>()
        input["name"] = "testBoardCreate"
        input["description"] = "test"

        val responseFields = arrayOf(
            PayloadDocumentation.fieldWithPath("boardId").type(JsonFieldType.NUMBER)
                .description("Unique board ID"),
            PayloadDocumentation.fieldWithPath("name").type(JsonFieldType.STRING)
                .description("Name of board"),
            PayloadDocumentation.fieldWithPath("description")
                .type(JsonFieldType.STRING)
                .description("Description of board"),
            PayloadDocumentation.fieldWithPath("writer").type(JsonFieldType.STRING)
                .description("Writer of board"),
            PayloadDocumentation.fieldWithPath("createdAt")
                .type(JsonFieldType.STRING).description("Created datetime of board"),
            PayloadDocumentation.fieldWithPath("modifier").type(JsonFieldType.STRING)
                .optional().description("Modifier of board"),
            PayloadDocumentation.fieldWithPath("updatedAt").type(JsonFieldType.STRING)
                .optional().description("Updated datetime of board")
        ).toList()

        val requestFields = arrayOf(
            PayloadDocumentation.fieldWithPath("name").type(JsonFieldType.STRING)
                .description("Name of board"),
            PayloadDocumentation.fieldWithPath("description").type(JsonFieldType.STRING)
                .description("Description of board")
        ).toList()

        //when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.post("/v1/boards")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(input))
        )

        //then
        result
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("boardId").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("name").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("description").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("writer").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("createdAt").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("modifier").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("updatedAt").value(null))
            .andDocument(
                "POST-boards",
                snippets = makeSnippets(
                    ResourceSnippetParameters.builder()
                        .tag("boards")
                        .summary("Create board")
                        .description("게시판을 생성합니다. 게시판의 이름은 중복이 불가능합니다.")
                        .requestSchema(Schema("boardCreate.Request"))
                        .responseSchema(Schema("boardCreate.Response"))
                        .requestFields(
                            requestFields
                        )
                        .responseHeaders(
                            header(HttpHeaders.LOCATION, "Location header")
                        )
                        .responseFields(
                            responseFields
                        )
                        .build()
                )
            )
        val jsonNode = ObjectMapper().readTree(result.andReturn().response.contentAsString)
        val createdBoardId = jsonNode["boardId"].asInt()

        boardJpaPort.deleteBoard(createdBoardId)
        Assertions.assertThrows(BoardDataNotFoundException::class.java) {
            boardJpaPort.readBoard(createdBoardId)
        }
    }

    @Test
    fun `GET boards`() {
        val token = jwtService.createAccessToken(memberJpaPort.findMemberByEmail("test")!!)

        //when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get("/v1/boards")
                .header("Authorization", "Bearer $token")
                .param("page", "1")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON)
        )

        val queryParameters = arrayOf(
            ResourceDocumentation.parameterWithName("page").type(SimpleType.INTEGER)
                .description("요청할 페이지 넘버(page > 0)"),
            ResourceDocumentation.parameterWithName("size").type(SimpleType.INTEGER)
                .description("한 페이지에 보여줄 게시판 갯수. min = 1, max = 50")
        ).toList()

        val responseFields = arrayOf(
            PayloadDocumentation.fieldWithPath("currentPage")
                .type(JsonFieldType.NUMBER).description("Number of current page"),
            PayloadDocumentation.fieldWithPath("totalPages")
                .type(JsonFieldType.NUMBER).description("Total count of page"),
            PayloadDocumentation.fieldWithPath("totalElements")
                .type(JsonFieldType.NUMBER).description("Total count of board"),
            PayloadDocumentation.fieldWithPath("size")
                .type(JsonFieldType.NUMBER)
                .description("The number of posts displayed on one page"),
            PayloadDocumentation.fieldWithPath("content[]").type(JsonFieldType.ARRAY).optional()
                .description("Board List"),
            PayloadDocumentation.fieldWithPath("content[].boardId").type(JsonFieldType.NUMBER)
                .description("Unique board ID"),
            PayloadDocumentation.fieldWithPath("content[].name").type(JsonFieldType.STRING)
                .description("Name of board"),
            PayloadDocumentation.fieldWithPath("content[].description")
                .type(JsonFieldType.STRING)
                .description("Description of board"),
            PayloadDocumentation.fieldWithPath("content[].writer").type(JsonFieldType.STRING)
                .description("Writer of board"),
            PayloadDocumentation.fieldWithPath("content[].createdAt")
                .type(JsonFieldType.STRING).description("Created datetime of board"),
            PayloadDocumentation.fieldWithPath("content[].modifier").type(JsonFieldType.STRING)
                .optional().description("Modifier of board"),
            PayloadDocumentation.fieldWithPath("content[].updatedAt").type(JsonFieldType.STRING)
                .optional().description("Updated datetime of board"),
        ).toList()

        //then
        result
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("content").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("currentPage").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("totalPages").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("totalElements").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("size").exists())
            .andDocument(
                "GET-boards",
                snippets = arrayOf(
                    ResourceDocumentation.resource(
                        ResourceSnippetParameters.builder()
                            .tag("boards")
                            .summary("Read board paging list")
                            .description(
                                """
                                    게시판 목록을 조회합니다. 오직 최근 생성순으로 내림차순 정렬됩니다. 요청할 페이지, 한 페이지의 보여줄 게시판의 갯수를 전송해야 합니다.
                                """.trimIndent()
                            )
                            .queryParameters(
                                queryParameters
                            )
                            .responseSchema(Schema("boardList.Response"))
                            .responseFields(
                                responseFields
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun `GET board-{boardId}`() {
        val token = jwtService.createAccessToken(memberJpaPort.findMemberByEmail("test")!!)
        val boardId = 2

        val responseFields = arrayOf(
            PayloadDocumentation.fieldWithPath("boardId").type(JsonFieldType.NUMBER)
                .description("Unique board ID"),
            PayloadDocumentation.fieldWithPath("name").type(JsonFieldType.STRING)
                .description("Name of board"),
            PayloadDocumentation.fieldWithPath("description")
                .type(JsonFieldType.STRING)
                .description("Description of board"),
            PayloadDocumentation.fieldWithPath("writer").type(JsonFieldType.STRING)
                .description("Writer of board"),
            PayloadDocumentation.fieldWithPath("createdAt")
                .type(JsonFieldType.STRING).description("Created datetime of board"),
            PayloadDocumentation.fieldWithPath("modifier").type(JsonFieldType.STRING)
                .optional().description("Modifier of board"),
            PayloadDocumentation.fieldWithPath("updatedAt").type(JsonFieldType.STRING)
                .optional().description("Updated datetime of board")
        ).toList()

        //when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.get("/v1/boards/{boardId}", boardId)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )

        //then
        result
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("boardId").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("name").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("description").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("writer").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("createdAt").exists())
            .andExpect(
                MockMvcResultMatchers.jsonPath("modifier")
                    .value(Matchers.anyOf(Matchers.instanceOf(String::class.java), Matchers.nullValue()))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("updatedAt")
                    .value(Matchers.anyOf(Matchers.instanceOf(String::class.java), Matchers.nullValue()))
            )
            .andDocument(
                "GET-boards-{boardId}",
                snippets = arrayOf(
                    ResourceDocumentation.resource(
                        ResourceSnippetParameters.builder()
                            .tag("boards")
                            .summary("Read board")
                            .description("게시판 정보를 조회합니다.")
                            .responseSchema(Schema("boardRead.Response"))
                            .pathParameters(
                                ResourceDocumentation.parameterWithName("boardId").type(SimpleType.INTEGER)
                                    .description("Unique board ID")
                            )
                            .responseFields(
                                responseFields
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    @Transactional
    fun `PATCH boards-{boardId}`() {
        val token = jwtService.createAccessToken(memberJpaPort.findMemberByEmail("test")!!)
        val input = mutableMapOf<String, String>()
        input["name"] = "testBoardUpdate"
        input["description"] = "test"
        val boardId = 2

        val responseFields = arrayOf(
            PayloadDocumentation.fieldWithPath("boardId").type(JsonFieldType.NUMBER)
                .description("Unique board ID"),
            PayloadDocumentation.fieldWithPath("name").type(JsonFieldType.STRING)
                .description("Name of board"),
            PayloadDocumentation.fieldWithPath("description")
                .type(JsonFieldType.STRING)
                .description("Description of board"),
            PayloadDocumentation.fieldWithPath("writer").type(JsonFieldType.STRING)
                .description("Writer of board"),
            PayloadDocumentation.fieldWithPath("createdAt")
                .type(JsonFieldType.STRING).description("Created datetime of board"),
            PayloadDocumentation.fieldWithPath("modifier").type(JsonFieldType.STRING)
                .optional().description("Modifier of board"),
            PayloadDocumentation.fieldWithPath("updatedAt").type(JsonFieldType.STRING)
                .optional().description("Updated datetime of board")
        ).toList()

        val requestFields = arrayOf(
            PayloadDocumentation.fieldWithPath("name").type(JsonFieldType.STRING)
                .description("Name of board"),
            PayloadDocumentation.fieldWithPath("description")
                .type(JsonFieldType.STRING)
                .description("Description of board")
        ).toList()

        //when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.patch("/v1/boards/{boardId}", boardId)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(input))
        )

        //then
        result
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("boardId").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("name").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("description").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("writer").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("createdAt").exists())
            .andExpect(
                MockMvcResultMatchers.jsonPath("modifier")
                    .value(Matchers.anyOf(Matchers.instanceOf(String::class.java), Matchers.nullValue()))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("updatedAt")
                    .value(Matchers.anyOf(Matchers.instanceOf(String::class.java), Matchers.nullValue()))
            )
            .andDocument(
                "PATCH-boards-{boardId}",
                snippets = arrayOf(
                    ResourceDocumentation.resource(
                        ResourceSnippetParameters.builder()
                            .tag("boards")
                            .summary("Update board")
                            .description("게시판 정보를 업데이트 합니다. 게시판의 이름은 중복이 불가능합니다.")
                            .requestSchema(Schema("boardUpdate.Request"))
                            .requestFields(
                                requestFields
                            )
                            .responseSchema(Schema("boardUpdate.Response"))
                            .pathParameters(
                                ResourceDocumentation.parameterWithName("boardId").type(SimpleType.INTEGER)
                                    .description("Unique board ID")
                            )
                            .responseFields(
                                responseFields
                            )
                            .build()
                    )
                )
            )

        Assertions.assertTrue {
            boardJpaPort.readBoard(boardId).boardId == boardId
        }
    }

    @Test
    @Transactional
    fun `DELETE board-{boardId}`() {
        val token = jwtService.createAccessToken(memberJpaPort.findMemberByEmail("test")!!)
        val board = Board(
            name = "testtest",
            description = "testtest",
            writer = "test",
            modifier = "test"
        )
        val boardId = boardJpaPort.createBoard(board).boardId

        //when
        val result = mockMvc.perform(
            RestDocumentationRequestBuilders.delete("/v1/boards/{boardId}", boardId)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )

        //then
        result
            .andExpect(MockMvcResultMatchers.status().isNoContent)
            .andDocument(
                "DELETE-boards-{boardId}",
                snippets = arrayOf(
                    ResourceDocumentation.resource(
                        ResourceSnippetParameters.builder()
                            .tag("boards")
                            .summary("Delete board")
                            .description("게시판을 제거합니다.")
                            .pathParameters(
                                ResourceDocumentation.parameterWithName("boardId").type(SimpleType.INTEGER)
                                    .description("Unique board ID")
                            )
                            .build()
                    )
                )
            )

        Assertions.assertThrows(BoardDataNotFoundException::class.java) {
            boardJpaPort.readBoard(boardId)
        }
    }
}