package algoapi.controller;

import algoapi.model.ApiResult;
import algoapi.service.BubbleSortService;
import algoapi.service.ExportService;
import algoapi.service.HashService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AlgoController MockMvc 切片测试
 * 验证 4 接口路径/入参/出参/HTTP 状态码
 * I2: 满足 clarify.md §4.6 "Spring Boot Test 对 4 接口单元+切片测试" 要求
 */
@SpringBootTest
@AutoConfigureMockMvc
class AlgoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HashService hashService;

    @Autowired
    private BubbleSortService bubbleSortService;

    @Autowired
    private ExportService exportService;

    /**
     * /api/hello → 200 + {code:0, message:"ok", data:{result:"HelloWorld"}}
     */
    @Test
    void hello_returnsOkWithHelloWorld() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.result").value("HelloWorld"))
                .andReturn();
        assertNotNull(result.getResponse().getContentAsString());
    }

    /**
     * /api/hash?input=hello → 200 + data.algorithm="SHA-256" + data.input 含转义
     */
    @Test
    void hash_withInput_returnsHashResult() throws Exception {
        mockMvc.perform(get("/api/hash").param("input", "hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.algorithm").value("SHA-256"))
                .andExpect(jsonPath("$.data.hash").isString())
                .andExpect(jsonPath("$.data.input").value("hello"));
    }

    /**
     * B1 验证：XSS 输入被 HTML 转义
     */
    @Test
    void hash_withXssInput_inputIsEscaped() throws Exception {
        String xssPayload = "<img src=x onerror=alert(1)>";
        mockMvc.perform(get("/api/hash").param("input", xssPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.input").value("&lt;img src=x onerror=alert(1)&gt;"));
    }

    /**
     * /api/hash 默认值 → input="hello"
     */
    @Test
    void hash_defaultInput_returnsHelloHash() throws Exception {
        mockMvc.perform(get("/api/hash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.input").value("hello"));
    }

    /**
     * /api/bubble?nums=3,1,2 → 200 + data.sorted=[1,2,3]
     */
    @Test
    void bubble_withValidNums_returnsSorted() throws Exception {
        mockMvc.perform(get("/api/bubble").param("nums", "3,1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.sorted[0]").value(1))
                .andExpect(jsonPath("$.data.sorted[1]").value(2))
                .andExpect(jsonPath("$.data.sorted[2]").value(3));
    }

    /**
     * /api/bubble 非法输入 → 回退默认数组 + warning
     */
    @Test
    void bubble_withInvalidNums_fallbackAndWarning() throws Exception {
        mockMvc.perform(get("/api/bubble").param("nums", "abc,xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.warning").exists())
                .andExpect(jsonPath("$.data.sorted").isArray());
    }

    /**
     * /api/export?type=hello → 200 + text/csv + attachment
     */
    @Test
    void export_hello_returnsCsv() throws Exception {
        mockMvc.perform(get("/api/export").param("type", "hello"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("hello.csv")))
                .andExpect(content().contentType("text/csv"));
    }

    /**
     * /api/export?type=invalid → 400
     */
    @Test
    void export_invalidType_returns400() throws Exception {
        mockMvc.perform(get("/api/export").param("type", "invalid"))
                .andExpect(status().isBadRequest());
    }
}
