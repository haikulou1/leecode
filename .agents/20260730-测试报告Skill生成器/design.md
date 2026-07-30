# 测试报告 Skill 生成器 — 需求澄清决策文档

> 阶段：clarify（需求澄清）｜技能：brainstorming
> 日期：2026-07-30
> 状态：澄清结论已自主决策（用户拒绝交互式澄清，按全流水线模式静默接管决策权）

---

## 1. 背景与目标

团队在完成测试执行后，测试结果散落在终端输出、CI 日志或框架原生产物（JUnit XML、coverage 目录）中。痛点：
- 测试结果需人工收集汇总，耗时且易遗漏；
- 缺乏统一格式测试报告，跨项目/团队沟通成本高；
- 失败用例上下文（错误信息、堆栈、关联代码）需人工回溯；
- 覆盖率、通过率等质量指标无法沉淀为可追踪历史数据。

**目标**：提供一个 Agentix Skill，Agent 执行测试后能自动解析测试结果并生成结构化、可读性强的标准测试报告。

## 2. 澄清决策（开放问题自主裁定）

用户拒绝了交互式澄清提问。依据需求文档默认值 + 跨仓现状证据，对三个开放问题及关键设计决策作如下裁定：

| 开放问题/决策点 | 决策结论 | 裁定依据 |
|---|---|---|
| Q1 首期目标项目栈 | **TS/Node 为主，JUnit XML 兜底同时覆盖 Java/Maven** | 需求文档按 TS/Node 假设制定 P0；naiss/preprocess 为 Maven 模块（pom.xml），JUnit XML 兜底解析器可同时验证 Java 场景，零额外成本扩大覆盖 |
| Q2 报告模板语言 | **仅中文** | 需求默认；国内团队同步场景（US3）；实现最简，后续如需 i18n 可扩展 |
| Q3 IM/邮件推送 | **非目标，不做** | 需求第 2.2 节明确列为非目标 |
| Skill 落盘仓库 | **leecode 仓库** | leecode 已建立 `.agents/<日期-功能名>/design.md` 约定，模块多元（web-demo/rpc/leecode）更接近通用工具库 |
| Skill 安装路径 | **.agents/skills/test-report-generator/** | 遵循 Agentix skill 目录形态（SKILL.md + references/ + scripts/） |
| 解析器实现语言 | **纯 Shell + 标准工具（jq / xmllint / python-json）** | 零运行时依赖；契合 Agentix skill 常见形态；NFR5 插件式结构易扩展 |

## 3. 首期范围（M1, P0）

### 3.1 支持的框架与结果格式
- **JavaScript/TypeScript**：Jest、Vitest（JSON reporter）
- **通用兜底**：JUnit XML（跨语言，覆盖 Python pytest、Java Maven surefire 等）

### 3.2 工作模式
- **执行模式**：Skill 触发测试运行并收集结果
- **解析模式**：跳过执行，直接解析用户指定的已有结果文件（US4 CI 场景）

### 3.3 报告标准结构（顺序固定）
1. 报告头：项目名、生成时间、执行命令、框架/版本、执行环境摘要
2. 结果摘要：用例总数、通过/失败/跳过数、通过率、总耗时；整体结论 ✅ / ❌
3. 失败用例分析（有失败时必选）：用例名、所属文件、错误信息、堆栈关键行（截断至可读长度）
4. 用例明细：按测试文件分组，各自耗时；超 200 条截断并注明
5. 覆盖率（若可获取）：语句/分支/函数/行覆盖率总表，低于阈值文件清单
6. 附录：原始结果文件路径、生成工具版本

### 3.4 输出与落盘
- 默认 Markdown（.md）；P1 支持 HTML；JSON 作为可选伴随产物
- 默认路径 `reports/test-report-<YYYYMMDD-HHmmss>.md`，允许用户指定
- 生成后返回：报告路径 + 结果摘要（通过率、失败数、关键失败原因 1~3 条）

## 4. 可配置项（均有默认值）

| 配置项 | 默认值 | 说明 |
|---|---|---|
| test_command | 自动检测 | 测试执行命令 |
| result_file | 自动检测 | 解析模式下结果文件路径 |
| output_format | markdown | markdown / html / json |
| output_path | reports/ | 报告输出目录 |
| coverage | auto | auto / on / off |
| fail_threshold | 无 | 通过率低于该值标记不达标 |

## 5. 测试框架自动识别优先级（FR1.1）
a. 用户显式指定命令
b. 项目配置：package.json scripts(test)、pyproject.toml、Cargo.toml、pom.xml
c. 框架特征文件推断：jest.config.*、vitest.config.*、pytest.ini、surefire-reports 目录

## 6. 非功能需求要点
- NFR1 性能：解析+生成 ≤5s（1000 用例）
- NFR2 健壮性：格式异常/字段缺失时降级标注"未获取"，不崩溃不丢数据
- NFR3 安全：不泄露环境变量/密钥；堆栈过滤凭据
- NFR4 幂等性：同结果多次生成内容一致（时间戳除外）
- NFR5 可维护性：框架解析器插件式，新增不影响既有

## 7. 验收标准
- AC1：含 Jest/Vitest 的 TS 项目执行"生成测试报告"，产出符合 §3.3 的 Markdown，摘要与原始输出一致
- AC2：失败用例时，失败分析含用例名、文件路径、错误信息
- AC3：JUnit XML 走解析模式，不触发执行即可产出报告
- AC4：结果文件损坏时返回明确错误说明，非空报告
- AC5：覆盖率数据存在时正确呈现，不存在时标注"未获取"且其余章节正常

## 8. Skill 目录结构预案

```
.agents/skills/test-report-generator/
├── SKILL.md                      # 触发意图、工作流、配置项
├── references/
│   ├── report-template.md        # 报告标准模板（中文）
│   └── framework-matrix.md       # 框架→解析器→结果格式映射
└── scripts/
    ├── generate-report.sh        # 主入口：执行/解析双模式编排
    ├── parsers/
    │   ├── parse-jest-json.sh    # Jest JSON reporter 解析插件
    │   ├── parse-vitest-json.sh  # Vitest JSON 解析插件
    │   └── parse-junit-xml.sh    # JUnit XML 兜底解析插件
    └── render/
        └── render-markdown.sh    # 标准结构渲染器
```

## 9. 后续迭代（非本期）
- M2：pytest 原生支持、覆盖率章节、fail_threshold
- M3：HTML 输出、JSON 伴随产物
- M4：历史趋势对比、Go test / cargo test

## 10. 跨仓对齐点
- 本 Skill 为通用工具，落 leecode 仓库，naiss 仓库可直接复用（无跨库代码依赖）
- JUnit XML 兜底解析器可同时服务 naiss/preprocess（Maven surefire）与 leecode Java 模块
- 接口契约：解析器插件统一输出中间数据结构（JSON），渲染器消费该结构 → 报告，插件式解耦
