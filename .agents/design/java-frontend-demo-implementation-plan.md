# Java 算法演示 + 前端三 Tab + 导出 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> **Spec source:** `.agents/design/java-frontend-demo-design.md`（clarify 阶段产物，含 R1-R3 需求拆解、D1-D3 自主裁定、2.4 跨库接口契约、2.6 对齐点清单）
>
> **阶段：** 实施计划（plan）
> **技能：** /writing-plans
> **日期：** 2026-07-31

**Goal:** 在 leecode 仓库新建独立 Spring Boot 模块 `algo-demo`，提供 helloworld / 哈希算法 / 冒泡排序三个 REST 接口 + 一个导出接口；在 haikulou1.github.io 仓库新增 `page/algo-demo/` 静态页面，含三个 Tab 分别展示执行结果 + 导出按钮；前后端通过 CORS 跨域协作，端到端可联调。

**Architecture:** 后端为独立 Maven 模块（不纳入 leecode 无根 pom 聚合），Spring Boot 2.7.18 内嵌 Tomcat，端口 8080。前端为原生 HTML + JS 静态页（无框架），通过 `fetch` 调用后端 `/api/*` 接口。导出由后端生成 `text/plain` 文件流，前端用 `<a download>` 触发下载。

**Tech Stack:** Java 8, Spring Boot 2.7.18 (spring-boot-starter-web), Maven 3.6+, 原生 HTML/CSS/JS (fetch API)

---

## Global Constraints

- 后端 groupId 沿用 leecode 现有模块约定 `cn.wy`，artifactId=`algo-demo`
- 新模块独立声明 `maven.compiler.source/target=8`（现有 designmodel 为 JDK1.7，模块级隔离互不影响）
- 所有接口前缀 `/api`，JSON 通信（导出接口除外，为 `text/plain`）
- CORS 由后端 `CorsConfig` 单向配置，允许所有源（演示用途）
- 前端 JS 顶部常量 `const API_BASE = 'http://localhost:8080'`，部署时替换为实际后端域名
- 不修改 leecode / haikulou1.github.io 任何既有文件，全部为新增
- 仓库物理路径（工具调用用绝对路径，文本输出用逻辑前缀）：
  - `[leecode]` = `/root/.agentix/agentic-dev/runs/DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-37c7a163-84c2-4d5b-ad22-5394182a870e/worktree/leecode-master`
  - `[haikulou1.github.io]` = `/root/.agentix/agentic-dev/runs/DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-37c7a163-84c2-4d5b-ad22-5394182a870e/worktree/haikulou1.github.io-master`

---

## File Structure

### [leecode] 后端 algo-demo 模块

| 文件 | 职责 |
|------|------|
| `algo-demo/pom.xml` | Maven 构建配置，Spring Boot 2.7.18 parent + starter-web 依赖，JDK8 |
| `algo-demo/src/main/java/cn/wy/algodemo/AlgoDemoApplication.java` | Spring Boot 启动类 |
| `algo-demo/src/main/java/cn/wy/algodemo/config/CorsConfig.java` | 全局 CORS 跨域配置 |
| `algo-demo/src/main/java/cn/wy/algodemo/controller/AlgoController.java` | 四个接口入口（helloworld/hash/bubble-sort/export） |
| `algo-demo/src/main/java/cn/wy/algodemo/service/HelloWorldService.java` | HelloWorld 业务逻辑 |
| `algo-demo/src/main/java/cn/wy/algodemo/service/HashService.java` | 哈希算法业务逻辑（MD5/SHA-256/SHA-512） |
| `algo-demo/src/main/java/cn/wy/algodemo/service/BubbleSortService.java` | 冒泡排序业务逻辑（含步骤轨迹） |
| `algo-demo/src/main/java/cn/wy/algodemo/service/ExportService.java` | 导出业务逻辑（聚合三接口结果为文本） |
| `algo-demo/src/main/java/cn/wy/algodemo/model/HashRequest.java` | 哈希接口请求体 |
| `algo-demo/src/main/java/cn/wy/algodemo/model/HashResponse.java` | 哈希接口响应体 |
| `algo-demo/src/main/java/cn/wy/algodemo/model/BubbleSortRequest.java` | 冒泡排序接口请求体 |
| `algo-demo/src/main/java/cn/wy/algodemo/model/BubbleSortResponse.java` | 冒泡排序接口响应体 |
| `algo-demo/src/main/resources/application.yml` | 服务端口配置 8080 |

### [haikulou1.github.io] 前端页面

| 文件 | 职责 |
|------|------|
| `page/algo-demo/index.html` | 三 Tab 页面结构 + 导出按钮 |
| `js/algo-demo.js` | 原生 fetch 调用四接口 + Tab 切换 + 导出下载 |

---

## Task 1: 后端模块脚手架（pom.xml + application.yml + 启动类）

**Files:**
- Create: `[leecode] algo-demo/pom.xml`
- Create: `[leecode] algo-demo/src/main/resources/application.yml`
- Create: `[leecode] algo-demo/src/main/java/cn/wy/algodemo/AlgoDemoApplication.java`

**Interfaces:**
- Produces: `AlgoDemoApplication.main(String[] args)` — Spring Boot 启动入口

### Step 1.1: 创建 pom.xml

- [ ] 创建文件 `[leecode] algo-demo/pom.xml`，内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>

    <groupId>cn.wy</groupId>
    <artifactId>algo-demo</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>algo-demo</name>
    <description>Java 算法演示 REST API（helloworld / hash / bubble-sort / export）</description>

    <properties>
        <java.version>8</java.version>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 1.2: 创建 application.yml

- [ ] 创建文件 `[leecode] algo-demo/src/main/resources/application.yml`，内容如下：

```yaml
server:
  port: 8080

spring:
  application:
    name: algo-demo
```

### Step 1.3: 创建启动类

- [ ] 创建文件 `[leecode] algo-demo/src/main/java/cn/wy/algodemo/AlgoDemoApplication.java`，内容如下：

```java
package cn.wy.algodemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AlgoDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlgoDemoApplication.class, args);
    }
}
```

### Step 1.4: 验证编译

- [ ] 在 `[leecode]` worktree 根目录执行编译验证：

```bash
cd /root/.agentix/agentic-dev/runs/DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-37c7a163-84c2-4d5b-ad22-5394182a870e/worktree/leecode-master/algo-demo && mvn compile -q
```

**预期输出：** `BUILD SUCCESS`（首次执行会下载 Spring Boot 依赖，耗时较长）。若依赖下载失败属于跨库环境问题，触发降级协议转静态审查，不阻塞后续任务编写。

---

## Task 2: 后端数据模型（4 个 Model 类）

**Files:**
- Create: `[leecode] algo-demo/src/main/java/cn/wy/algodemo/model/HashRequest.java`
- Create: `[leecode] algo-demo/src/main/java/cn/wy/algodemo/model/HashResponse.java`
- Create: `[leecode] algo-demo/src/main/java/cn/wy/algodemo/model/BubbleSortRequest.java`
- Create: `[leecode] algo-demo/src/main/java/cn/wy/algodemo/model/BubbleSortResponse.java`

**Interfaces:**
- Produces: `HashRequest{String input; String algorithm}` — 哈希接口请求体
- Produces: `HashResponse{String input; String algorithm; String hash; int length}` — 哈希接口响应体
- Produces: `BubbleSortRequest{List<Integer> input}` — 冒泡排序接口请求体
- Produces: `BubbleSortResponse{List<Integer> input; List<Integer> sorted; List<SortStep> steps; int swapCount}` — 冒泡排序接口响应体

### Step 2.1: 创建 HashRequest

- [ ] 创建文件 `[leecode] algo-demo/src/main/java/cn/wy/algodemo/model/HashRequest.java`：

```java
package cn.wy.algodemo.model;

public class HashRequest {
    private String input;
    private String algorithm;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
```

### Step 2.2: 创建 HashResponse

- [ ] 创建文件 `[leecode] algo-demo/src/main/java/cn/wy/algodemo/model/HashResponse.java`：

```java
package cn.wy.algodemo.model;

public class HashResponse {
    private String input;
    private String algorithm;
    private String hash;
    private int length;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
```

### Step 2.3: 创建 BubbleSortRequest

- [ ] 创建文件 `[leecode] algo-demo/src/main/java/cn/wy/algodemo/model/BubbleSortRequest.java`：

```java
package cn.wy.algodemo.model;

import java.util.List;

public class BubbleSortRequest {
    private List<Integer> input;

    public List<Integer> getInput() {
        return input;
    }

    public void setInput(List<Integer> input) {
        this.input = input;
    }
}
```

### Step 2.4: 创建 BubbleSortResponse 和 SortStep

- [ ] 创建文件 `[leecode] algo-demo/src/main/java/cn/wy/algodemo/model/BubbleSortResponse.java`：

```java
package cn.wy.algodemo.model;

import java.util.List;

public class BubbleSortResponse {
    private List<Integer> input;
    private List<Integer> sorted;
    private List<SortStep> steps;
    private int swapCount;

    public List<Integer> getInput() {
        return input;
    }

    public void setInput(List<Integer> input) {
        this.input = input;
    }

    public List<Integer> getSorted() {
        return sorted;
    }

    public void setSorted(List<Integer> sorted) {
        this.sorted = sorted;
    }

    public List<SortStep> getSteps() {
        return steps;
    }

    public void setSteps(List<SortStep> steps) {
        this.steps = steps;
    }

    public int getSwapCount() {
        return swapCount;
    }

    public void setSwapCount(int swapCount) {
        this.swapCount = swapCount;
    }

    public static class SortStep {
        private int round;
        private int swaps;
        private List<Integer> array;

        public SortStep() {
        }

        public SortStep(int round, int swaps, List<Integer> array) {
            this.round = round;
            this.swaps = swaps;
            this.array = array;
        }

        public int getRound() {
            return round;
        }

        public void setRound(int round) {
            this.round = round;
        }

        public int getSwaps() {
            return swaps;
        }

        public void setSwaps(int swaps) {
            this.swaps = swaps;
        }

        public List<Integer> getArray() {
            return array;
        }

        public void setArray(List<Integer> array) {
            this.array = array;
        }
    }
}
```

---

## Task 3: 后端业务服务（4 个 Service 类）

**Files:**
- Create: `[leecode] algo-demo/src/main/java/cn/wy/algodemo/service/HelloWorldService.java`
- Create: `[leecode] algo-demo/src/main/java/cn/wy/algodemo/service/HashService.java`
- Create: `[leecode] algo-demo/src/main/java/cn/wy/algodemo/service/BubbleSortService.java`
- Create: `[leecode] algo-demo/src/main/java/cn/wy/algodemo/service/ExportService.java`

**Interfaces:**
- Consumes: Task 2 的 Model 类
- Produces: `HelloWorldService.getHelloWorld()` → `LinkedHashMap<String,String>`
- Produces: `HashService.hash(HashRequest)` → `HashResponse`
- Produces: `BubbleSortService.sort(BubbleSortRequest)` → `BubbleSortResponse`
- Produces: `ExportService.exportAll()` → `String`（三段结果文本）

### Step 3.1: 创建 HelloWorldService

- [ ] 创建文件 `[leecode] algo-demo/src/main/java/cn/wy/algodemo/service/HelloWorldService.java`：

```java
package cn.wy.algodemo.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class HelloWorldService {

    public Map<String, String> getHelloWorld() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("result", "Hello, World!");
        result.put("timestamp", Instant.now().toString());
        return result;
    }
}
```

### Step 3.2: 创建 HashService

- [ ] 创建文件 `[leecode] algo-demo/src/main/java/cn/wy/algodemo/service/HashService.java`：

```java
package cn.wy.algodemo.service;

import cn.wy.algodemo.model.HashRequest;
import cn.wy.algodemo.model.HashResponse;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class HashService {

    public HashResponse hash(HashRequest request) {
        String input = request.getInput();
        String algorithm = request.getAlgorithm();
        if (algorithm == null || algorithm.isEmpty()) {
            algorithm = "SHA-256";
        }

        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String hashHex = sb.toString();

            HashResponse response = new HashResponse();
            response.setInput(input);
            response.setAlgorithm(algorithm);
            response.setHash(hashHex);
            response.setLength(hashHex.length());
            return response;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm, e);
        }
    }
}
```

### Step 3.3: 创建 BubbleSortService

- [ ] 创建文件 `[leecode] algo-demo/src/main/java/cn/wy/algodemo/service/BubbleSortService.java`：

```java
package cn.wy.algodemo.service;

import cn.wy.algodemo.model.BubbleSortRequest;
import cn.wy.algodemo.model.BubbleSortResponse;
import cn.wy.algodemo.model.BubbleSortResponse.SortStep;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BubbleSortService {

    public BubbleSortResponse sort(BubbleSortRequest request) {
        List<Integer> input = request.getInput();
        if (input == null) {
            input = new ArrayList<>();
        }

        List<Integer> arr = new ArrayList<>(input);
        int n = arr.size();
        int totalSwaps = 0;
        List<SortStep> steps = new ArrayList<>();

        for (int i = 0; i < n - 1; i++) {
            int roundSwaps = 0;
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr.get(j) > arr.get(j + 1)) {
                    int tmp = arr.get(j);
                    arr.set(j, arr.get(j + 1));
                    arr.set(j + 1, tmp);
                    roundSwaps++;
                    totalSwaps++;
                }
            }
            steps.add(new SortStep(i + 1, roundSwaps, new ArrayList<>(arr)));
        }

        BubbleSortResponse response = new BubbleSortResponse();
        response.setInput(new ArrayList<>(input));
        response.setSorted(arr);
        response.setSteps(steps);
        response.setSwapCount(totalSwaps);
        return response;
    }
}
```

### Step 3.4: 创建 ExportService

- [ ] 创建文件 `[leecode] algo-demo/src/main/java/cn/wy/algodemo/service/ExportService.java`：

```java
package cn.wy.algodemo.service;

import cn.wy.algodemo.model.BubbleSortRequest;
import cn.wy.algodemo.model.BubbleSortResponse;
import cn.wy.algodemo.model.HashRequest;
import cn.wy.algodemo.model.HashResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
public class ExportService {

    private final HelloWorldService helloWorldService;
    private final HashService hashService;
    private final BubbleSortService bubbleSortService;

    public ExportService(HelloWorldService helloWorldService,
                         HashService hashService,
                         BubbleSortService bubbleSortService) {
        this.helloWorldService = helloWorldService;
        this.hashService = hashService;
        this.bubbleSortService = bubbleSortService;
    }

    public String exportAll() {
        StringBuilder sb = new StringBuilder();

        // ===== HelloWorld =====
        Map<String, String> hw = helloWorldService.getHelloWorld();
        sb.append("===== HelloWorld =====\n");
        sb.append("result: ").append(hw.get("result")).append("\n");
        sb.append("timestamp: ").append(hw.get("timestamp")).append("\n\n");

        // ===== Hash =====
        HashRequest hashReq = new HashRequest();
        hashReq.setInput("abc");
        hashReq.setAlgorithm("SHA-256");
        HashResponse hashResp = hashService.hash(hashReq);
        sb.append("===== Hash =====\n");
        sb.append("input: ").append(hashResp.getInput()).append("\n");
        sb.append("algorithm: ").append(hashResp.getAlgorithm()).append("\n");
        sb.append("hash: ").append(hashResp.getHash()).append("\n");
        sb.append("length: ").append(hashResp.getLength()).append("\n\n");

        // ===== BubbleSort =====
        BubbleSortRequest sortReq = new BubbleSortRequest();
        sortReq.setInput(Arrays.asList(5, 3, 8, 1, 9, 2));
        BubbleSortResponse sortResp = bubbleSortService.sort(sortReq);
        sb.append("===== BubbleSort =====\n");
        sb.append("input: ").append(sortResp.getInput()).append("\n");
        sb.append("sorted: ").append(sortResp.getSorted()).append("\n");
        sb.append("swapCount: ").append(sortResp.getSwapCount()).append("\n");
        sb.append("steps:\n");
        for (BubbleSortResponse.SortStep step : sortResp.getSteps()) {
            sb.append("  round ").append(step.getRound())
              .append(": swaps=").append(step.getSwaps())
              .append(", array=").append(step.getArray()).append("\n");
        }

        return sb.toString();
    }
}
```

---

## Task 4: 后端 CORS 配置 + 控制器

**Files:**
- Create: `[leecode] algo-demo/src/main/java/cn/wy/algodemo/config/CorsConfig.java`
- Create: `[leecode] algo-demo/src/main/java/cn/wy/algodemo/controller/AlgoController.java`

**Interfaces:**
- Consumes: Task 1 启动类、Task 3 的四个 Service
- Produces: 四个 HTTP 端点
  - `GET /api/helloworld` → `Map<String,String>` JSON
  - `POST /api/hash` body `HashRequest` → `HashResponse` JSON
  - `POST /api/bubble-sort` body `BubbleSortRequest` → `BubbleSortResponse` JSON
  - `GET /api/export?tab=all` → `text/plain` 文件流

### Step 4.1: 创建 CorsConfig

- [ ] 创建文件 `[leecode] algo-demo/src/main/java/cn/wy/algodemo/config/CorsConfig.java`：

```java
package cn.wy.algodemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
```

### Step 4.2: 创建 AlgoController

- [ ] 创建文件 `[leecode] algo-demo/src/main/java/cn/wy/algodemo/controller/AlgoController.java`：

```java
package cn.wy.algodemo.controller;

import cn.wy.algodemo.model.BubbleSortRequest;
import cn.wy.algodemo.model.BubbleSortResponse;
import cn.wy.algodemo.model.HashRequest;
import cn.wy.algodemo.model.HashResponse;
import cn.wy.algodemo.service.BubbleSortService;
import cn.wy.algodemo.service.ExportService;
import cn.wy.algodemo.service.HashService;
import cn.wy.algodemo.service.HelloWorldService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AlgoController {

    private final HelloWorldService helloWorldService;
    private final HashService hashService;
    private final BubbleSortService bubbleSortService;
    private final ExportService exportService;

    public AlgoController(HelloWorldService helloWorldService,
                         HashService hashService,
                         BubbleSortService bubbleSortService,
                         ExportService exportService) {
        this.helloWorldService = helloWorldService;
        this.hashService = hashService;
        this.bubbleSortService = bubbleSortService;
        this.exportService = exportService;
    }

    @GetMapping("/helloworld")
    public Map<String, String> helloworld() {
        return helloWorldService.getHelloWorld();
    }

    @PostMapping("/hash")
    public HashResponse hash(@RequestBody HashRequest request) {
        return hashService.hash(request);
    }

    @PostMapping("/bubble-sort")
    public BubbleSortResponse bubbleSort(@RequestBody BubbleSortRequest request) {
        return bubbleSortService.sort(request);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(name = "tab", defaultValue = "all") String tab) {
        String content = exportService.exportAll();
        String timestamp = ZonedDateTime.now(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filename = "algo-export-" + timestamp + ".txt";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(content.getBytes(StandardCharsets.UTF_8));
    }
}
```

### Step 4.3: 验证完整编译

- [ ] 在 `[leecode]` algo-demo 目录执行编译：

```bash
cd /root/.agentix/agentic-dev/runs/DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-37c7a163-84c2-4d5b-ad22-5394182a870e/worktree/leecode-master/algo-demo && mvn compile -q
```

**预期输出：** `BUILD SUCCESS`。若编译失败，检查 import 路径与类名是否与 Task 2/3 一致。若同一模块编译 ≥2 次仍失败，触发降级协议转静态审查（对照 design.md 2.4 节入参/出参字段类型匹配）。

---

## Task 5: 前端三 Tab 页面 + 导出按钮

**Files:**
- Create: `[haikulou1.github.io] page/algo-demo/index.html`
- Create: `[haikulou1.github.io] js/algo-demo.js`

**Interfaces:**
- Consumes: Task 4 的四个 HTTP 端点（通过 fetch 调用 `/api/helloworld`, `/api/hash`, `/api/bubble-sort`, `/api/export`）
- Produces: 可独立访问的静态页面 `/page/algo-demo/`

### Step 5.1: 创建 index.html

- [ ] 创建文件 `[haikulou1.github.io] page/algo-demo/index.html`：

```html
<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>算法演示 - HelloWorld / 哈希 / 冒泡排序</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif; background: #f5f7fa; color: #333; padding: 20px; }
        .container { max-width: 800px; margin: 0 auto; }
        h1 { text-align: center; margin-bottom: 20px; color: #2c3e50; }
        .tabs { display: flex; border-bottom: 2px solid #3498db; margin-bottom: 20px; }
        .tab-btn { flex: 1; padding: 12px; text-align: center; cursor: pointer; background: #ecf0f1; border: none; font-size: 15px; color: #7f8c8d; transition: all 0.2s; }
        .tab-btn.active { background: #3498db; color: #fff; font-weight: bold; }
        .tab-content { display: none; background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
        .tab-content.active { display: block; }
        .form-row { margin-bottom: 16px; }
        .form-row label { display: block; margin-bottom: 6px; font-weight: 600; color: #555; }
        .form-row input, .form-row select { width: 100%; padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
        .btn { padding: 10px 24px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; color: #fff; background: #3498db; transition: background 0.2s; }
        .btn:hover { background: #2980b9; }
        .btn-export { background: #27ae60; margin: 20px 0; }
        .btn-export:hover { background: #229954; }
        .result { margin-top: 16px; padding: 16px; background: #f8f9fa; border-radius: 4px; border-left: 4px solid #3498db; white-space: pre-wrap; word-break: break-all; font-family: "SF Mono", "Consolas", monospace; font-size: 13px; line-height: 1.6; }
        .error { border-left-color: #e74c3c; color: #c0392b; }
        .header-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>算法演示</h1>

        <div class="header-bar">
            <span>Java 后端接口演示页面</span>
            <button class="btn btn-export" onclick="exportAll()">导出全部结果</button>
        </div>

        <div class="tabs">
            <button class="tab-btn active" onclick="switchTab('helloworld')">HelloWorld</button>
            <button class="tab-btn" onclick="switchTab('hash')">哈希算法</button>
            <button class="tab-btn" onclick="switchTab('bubble-sort')">冒泡排序</button>
        </div>

        <!-- Tab 1: HelloWorld -->
        <div id="tab-helloworld" class="tab-content active">
            <p style="margin-bottom:16px;color:#7f8c8d;">点击执行按钮，调用后端 /api/helloworld 接口。</p>
            <button class="btn" onclick="callHelloWorld()">执行</button>
            <div id="result-helloworld" class="result" style="display:none;"></div>
        </div>

        <!-- Tab 2: Hash -->
        <div id="tab-hash" class="tab-content">
            <div class="form-row">
                <label>输入文本</label>
                <input type="text" id="hash-input" value="abc" placeholder="请输入要哈希的文本">
            </div>
            <div class="form-row">
                <label>哈希算法</label>
                <select id="hash-algorithm">
                    <option value="SHA-256">SHA-256</option>
                    <option value="MD5">MD5</option>
                    <option value="SHA-512">SHA-512</option>
                </select>
            </div>
            <button class="btn" onclick="callHash()">执行</button>
            <div id="result-hash" class="result" style="display:none;"></div>
        </div>

        <!-- Tab 3: Bubble Sort -->
        <div id="tab-bubble-sort" class="tab-content">
            <div class="form-row">
                <label>输入数组（逗号分隔）</label>
                <input type="text" id="sort-input" value="5,3,8,1,9,2" placeholder="例如: 5,3,8,1,9,2">
            </div>
            <button class="btn" onclick="callBubbleSort()">执行</button>
            <div id="result-bubble-sort" class="result" style="display:none;"></div>
        </div>
    </div>

    <script src="/js/algo-demo.js"></script>
</body>
</html>
```

### Step 5.2: 创建 algo-demo.js

- [ ] 创建文件 `[haikulou1.github.io] js/algo-demo.js`：

```javascript
// 后端 API 地址（开发期 localhost:8080，部署时改为实际后端域名）
const API_BASE = 'http://localhost:8080';

// Tab 切换
function switchTab(tabId) {
    document.querySelectorAll('.tab-content').forEach(function(el) {
        el.classList.remove('active');
    });
    document.querySelectorAll('.tab-btn').forEach(function(el) {
        el.classList.remove('active');
    });
    document.getElementById('tab-' + tabId).classList.add('active');
    event.target.classList.add('active');
}

// 显示结果到指定区域
function showResult(elementId, text, isError) {
    var el = document.getElementById(elementId);
    el.textContent = text;
    el.classList.remove('error');
    if (isError) {
        el.classList.add('error');
    }
    el.style.display = 'block';
}

// 调用 helloworld 接口
function callHelloWorld() {
    fetch(API_BASE + '/api/helloworld')
        .then(function(resp) { return resp.json(); })
        .then(function(data) {
            showResult('result-helloworld',
                'result: ' + data.result + '\ntimestamp: ' + data.timestamp, false);
        })
        .catch(function(err) {
            showResult('result-helloworld', 'Error: ' + err.message, true);
        });
}

// 调用 hash 接口
function callHash() {
    var input = document.getElementById('hash-input').value;
    var algorithm = document.getElementById('hash-algorithm').value;
    fetch(API_BASE + '/api/hash', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ input: input, algorithm: algorithm })
    })
        .then(function(resp) { return resp.json(); })
        .then(function(data) {
            showResult('result-hash',
                'input: ' + data.input + '\nalgorithm: ' + data.algorithm +
                '\nhash: ' + data.hash + '\nlength: ' + data.length, false);
        })
        .catch(function(err) {
            showResult('result-hash', 'Error: ' + err.message, true);
        });
}

// 调用 bubble-sort 接口
function callBubbleSort() {
    var inputStr = document.getElementById('sort-input').value;
    var input = inputStr.split(',').map(function(s) { return parseInt(s.trim(), 10); });
    fetch(API_BASE + '/api/bubble-sort', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ input: input })
    })
        .then(function(resp) { return resp.json(); })
        .then(function(data) {
            var stepsText = data.steps.map(function(s) {
                return '  round ' + s.round + ': swaps=' + s.swaps + ', array=[' + s.array.join(', ') + ']';
            }).join('\n');
            showResult('result-bubble-sort',
                'input: [' + data.input.join(', ') + ']\n' +
                'sorted: [' + data.sorted.join(', ') + ']\n' +
                'swapCount: ' + data.swapCount + '\n' +
                'steps:\n' + stepsText, false);
        })
        .catch(function(err) {
            showResult('result-bubble-sort', 'Error: ' + err.message, true);
        });
}

// 导出全部结果（调用 /api/export，触发文件下载）
function exportAll() {
    fetch(API_BASE + '/api/export?tab=all')
        .then(function(resp) { return resp.blob(); })
        .then(function(blob) {
            var url = window.URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = url;
            a.download = 'algo-export.txt';
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        })
        .catch(function(err) {
            alert('导出失败: ' + err.message);
        });
}
```

---

## Task 6: 端到端联调验证

**Files:** 无新建（验证步骤）

**Interfaces:**
- Consumes: Task 1-5 全部产物

### Step 6.1: 启动后端服务

- [ ] 在 `[leecode]` algo-demo 目录启动 Spring Boot 服务：

```bash
cd /root/.agentix/agentic-dev/runs/DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-37c7a163-84c2-4d5b-ad22-5394182a870e/worktree/leecode-master/algo-demo && mvn spring-boot:run
```

**预期输出：** 控制台出现 `Tomcat started on port(s): 8080` 和 `Started AlgoDemoApplication in x.xxx seconds`。若启动失败且属跨库环境问题（依赖未发布/版本冲突），触发降级协议转静态审查。

### Step 6.2: curl 验证四个接口

- [ ] 新开终端，执行以下 curl 命令逐一验证：

```bash
# 接口 1: helloworld
curl -s http://localhost:8080/api/helloworld
# 预期: {"result":"Hello, World!","timestamp":"2026-07-31T..."}

# 接口 2: hash
curl -s -X POST http://localhost:8080/api/hash \
  -H "Content-Type: application/json" \
  -d '{"input":"abc","algorithm":"SHA-256"}'
# 预期: {"input":"abc","algorithm":"SHA-256","hash":"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad","length":64}

# 接口 3: bubble-sort
curl -s -X POST http://localhost:8080/api/bubble-sort \
  -H "Content-Type: application/json" \
  -d '{"input":[5,3,8,1,9,2]}'
# 预期: {"input":[5,3,8,1,9,2],"sorted":[1,2,3,5,8,9],"steps":[...],"swapCount":8}

# 接口 4: export
curl -s -O http://localhost:8080/api/export?tab=all
# 预期: 下载 algo-export-<timestamp>.txt，内容含三段 ===== 结果文本
```

### Step 6.3: 浏览器验证前端页面

- [ ] 在浏览器访问前端页面（本地文件或静态服务器）：

```
file://haikulou1.github.io-worktree/page/algo-demo/index.html
```
或用静态服务器：
```bash
cd /root/.agentix/agentic-dev/runs/DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-37c7a163-84c2-4d5b-ad22-5394182a870e/worktree/haikulou1.github.io-master && python3 -m http.server 3000
```
然后访问 `http://localhost:3000/page/algo-demo/`

**预期验证项：**
- 三个 Tab 可切换，每个 Tab 点击"执行"按钮后结果区展示对应接口返回
- 哈希 Tab：输入 `abc` + 选 `SHA-256`，结果显示 64 位十六进制哈希
- 冒泡排序 Tab：输入 `5,3,8,1,9,2`，结果含 `sorted: [1, 2, 3, 5, 8, 9]` 和步骤轨迹
- 点击"导出全部结果"按钮，浏览器下载 `algo-export.txt` 文件

> **降级协议触发条件：** 若后端启动 ≥2 次仍失败，或单次启动 >120s 且失败，或属跨库环境问题（Maven 依赖下载失败/JDK 版本不满足 8+），停止构建，以 `[降级说明]` 开头输出原因，转为审查跨仓对齐点入参/出参类型匹配（对照 design.md 2.4 节契约字段逐项核对代码实现）。

---

## Self-Review

### 1. Spec coverage（对照 design.md）

| 设计文档章节 | 覆盖任务 | 状态 |
|-------------|---------|------|
| 2.3 后端模块设计目录结构 | Task 1-4 全部文件 | ✅ 覆盖 |
| 2.4 接口 1 helloworld | Task 3.1 HelloWorldService + Task 4.2 AlgoController | ✅ 覆盖 |
| 2.4 接口 2 hash | Task 2.1-2.2 Model + Task 3.2 HashService + Task 4.2 | ✅ 覆盖 |
| 2.4 接口 3 bubble-sort | Task 2.3-2.4 Model + Task 3.3 BubbleSortService + Task 4.2 | ✅ 覆盖 |
| 2.4 接口 4 export | Task 3.4 ExportService + Task 4.2 | ✅ 覆盖 |
| 2.5 前端三 Tab + 导出按钮 | Task 5.1 index.html + Task 5.2 algo-demo.js | ✅ 覆盖 |
| 2.6 CORS 跨域 | Task 4.1 CorsConfig | ✅ 覆盖 |
| D1 Spring Boot REST | Task 1 pom.xml + 启动类 | ✅ 覆盖 |
| D2 哈希函数 MD5/SHA-256/SHA-512 | Task 3.2 HashService（MessageDigest） | ✅ 覆盖 |
| D3 导出单文件含三段文本 | Task 3.4 ExportService.exportAll() | ✅ 覆盖 |

### 2. Placeholder scan

- ✅ 无 TBD / TODO / 待补充 / fill in details
- ✅ 每个 Step 含完整代码块
- ✅ 命令含预期输出
- ✅ 路径均为精确物理路径

### 3. 落盘路径检查

- 后端模块：`[leecode] worktree/algo-demo/` ✅ 在 worktree 内
- 前端页面：`[haikulou1.github.io] worktree/page/algo-demo/` + `worktree/js/` ✅ 在 worktree 内
- 本计划文档：`[leecode] worktree/.agents/design/` ✅ 在 worktree 内

### 4. plan 阶段约束

- ✅ 本计划文档为唯一产物，无 .java/.html/.js/.xml/.yml 代码文件被修改
- ✅ 后续编码阶段（Task 1-6）将在「编码实现」阶段执行

---

## Execution Handoff

计划已保存至 `[leecode] .agents/design/java-frontend-demo-implementation-plan.md`。

**两种执行选项：**

1. **Subagent-Driven（推荐）** — 按 Task 1→6 逐个派发子代理执行，每个 Task 间设审查检查点，快速迭代
2. **Inline Execution** — 在当前会话使用 executing-plans 技能逐步执行，批量执行带检查点

> 进入编码阶段后，按 Task 顺序执行：后端脚手架（Task 1）→ 数据模型（Task 2）→ 业务服务（Task 3）→ 控制器+CORS（Task 4）→ 前端页面（Task 5）→ 端到端联调（Task 6）。每完成一个 Task 后提交 git commit（遵循 Co-authored-by trailer 约定）。
