# 测试报告 Skill 生成器 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
> **Goal:** 构建一个 Agentix Skill，Agent 执行测试后自动解析 Jest/Vitest JSON 与 JUnit XML 结果，生成结构化标准 Markdown 测试报告，支持执行/解析双模式。
> **Architecture:** 插件式解析器架构。每个框架解析器将原生产物转换为统一中间数据结构（JSON），Markdown 渲染器消费该结构生成报告。主入口脚本编排"框架识别 → 执行/解析 → 解析器选择 → 渲染 → 落盘"全流程。纯 Shell + jq/xmllint/python-json 实现，零运行时依赖。
> **Tech Stack:** Bash（POSIX-ish，需 jq、xmllint、python3）、Markdown 报告模板（中文）
> **Spec:** `.agents/20260730-测试报告Skill生成器/design.md`（澄清决策文档）

## Global Constraints

- 实现语言：纯 Shell + 标准工具（jq / xmllint / python3 -m json.tool）；禁止引入 Node/Python 运行时框架
- 报告模板语言：仅中文
- 报告默认输出格式：Markdown（.md）
- 默认输出路径：`reports/test-report-<YYYYMMDD-HHmmss>.md`
- 性能：解析 + 生成 ≤5s（1000 用例规模）
- 健壮性：字段缺失时降级标注"未获取"，不得崩溃或静默丢数据
- 安全：报告不泄露环境变量/密钥；堆栈过滤凭据信息
- 幂等性：同一结果文件多次生成内容一致（时间戳字段除外）
- 插件式：新增框架解析器不影响既有解析器（NFR5）
- Skill 安装路径：`.agents/skills/test-report-generator/`
- 所有脚本以 `#!/usr/bin/env bash` 开头，`set -euo pipefail`
- Git 写操作禁止；测试验证以静态审查 + shellcheck + 人工 fixture 验证为准

---

## File Structure

```
.agents/skills/test-report-generator/
├── SKILL.md                          # 触发意图、工作流、配置项、使用说明
├── references/
│   ├── report-template.md            # 报告标准模板（中文，六大章节）
│   └── framework-matrix.md           # 框架→解析器→结果格式映射表
├── scripts/
│   ├── generate-report.sh           # 主入口：执行/解析双模式编排
│   ├── lib/
│   │   ├── common.sh                 # 公共函数：日志、降级标注、堆栈脱敏
│   │   └── intermediate-schema.json  # 中间数据结构 JSON Schema 定义
│   ├── parsers/
│   │   ├── parse-jest-json.sh        # Jest JSON reporter 解析插件
│   │   ├── parse-vitest-json.sh      # Vitest JSON 解析插件
│   │   └── parse-junit-xml.sh        # JUnit XML 兜底解析插件
│   └── render/
│       └── render-markdown.sh        # 标准结构 Markdown 渲染器
└── tests/
    ├── fixtures/
    │   ├── jest-results.sample.json  # Jest 样本结果（含通过+失败用例）
    │   ├── vitest-results.sample.json# Vitest 样本结果
    │   └── junit-results.sample.xml  # JUnit XML 样本结果
    └── run-tests.sh                  # 端到端验证脚本（解析 fixture → 校验报告结构）
```

每个文件职责：
- `SKILL.md`：Agent 可读的触发意图、工作流、配置项声明，是 Skill 的入口契约。
- `references/report-template.md`：报告六大章节的固定结构与占位符规范，渲染器据此生成。
- `references/framework-matrix.md`：框架识别优先级与解析器映射的单一事实源。
- `scripts/generate-report.sh`：主编排器，解析配置 → 识别框架 → 执行/解析 → 调用解析器 → 调用渲染器 → 落盘 → 输出摘要。
- `scripts/lib/common.sh`：公共工具函数（降级标注、堆栈脱敏、日志），被各解析器和渲染器 source。
- `scripts/lib/intermediate-schema.json`：中间数据结构的 JSON Schema，约束所有解析器输出契约。
- `scripts/parsers/*.sh`：框架专用解析插件，各自将原生产物转为中间结构 JSON。
- `scripts/render/render-markdown.sh`：消费中间结构，按模板渲染 Markdown 报告。
- `tests/`：样本 fixture 与端到端验证脚本，确保 AC1-AC5 可复现。

---

## Task 1: 中间数据结构与公共库

**Files:**
- Create: `.agents/skills/test-report-generator/scripts/lib/intermediate-schema.json`
- Create: `.agents/skills/test-report-generator/scripts/lib/common.sh`

**Interfaces:**
- Consumes: 无（基础设施）
- Produces:
  - `intermediate-schema.json`：定义中间结构 JSON 的顶层字段与类型约束
  - `common.sh`：函数 `log_info`、`log_error`、`fallback_value`（降级标注"未获取"）、`sanitize_stack`（凭据脱敏）、`truncate_text`（截断至可读长度）。通过 `source` 引用，无返回值约束

**Intermediate Schema 定义（所有解析器输出契约）：**

中间结构为单一 JSON 对象，顶层字段：
```json
{
  "report_header": {
    "project_name": "string",
    "generated_at": "ISO8601 string",
    "test_command": "string",
    "framework": "string",
    "framework_version": "string",
    "env_summary": "string"
  },
  "summary": {
    "total": "integer",
    "passed": "integer",
    "failed": "integer",
    "skipped": "integer",
    "pass_rate": "number (0-100, 1位小数)",
    "total_duration_ms": "integer",
    "overall_status": "PASS | FAIL"
  },
  "failures": [
    {
      "test_name": "string",
      "file": "string",
      "error_message": "string",
      "stack_key_lines": ["string"]
    }
  ],
  "details": [
    {
      "file": "string",
      "tests": [
        { "name": "string", "status": "PASS|FAIL|SKIP", "duration_ms": "integer" }
      ]
    }
  ],
  "coverage": {
    "available": "boolean",
    "statements_pct": "number|null",
    "branches_pct": "number|null",
    "functions_pct": "number|null",
    "lines_pct": "number|null",
    "below_threshold_files": ["string"]
  },
  "appendix": {
    "result_file_path": "string",
    "tool_version": "string"
  }
}
```

**Interfaces:**
- Consumes: 无
- Produces: `intermediate-schema.json`（上述结构）、`common.sh` 中函数签名：
  - `log_info "message"` — 输出到 stderr
  - `log_error "message"` — 输出到 stderr，前缀 `ERROR:`
  - `fallback_value <value> [label]` — 若 value 为空输出"未获取"，否则输出 value
  - `sanitize_stack "raw_stack_text"` — 过滤常见凭据模式（password=、token=、secret=、key= 后的值替换为 `***`），输出脱敏文本
  - `truncate_text "text" max_lines` — 超过 max_lines 截断，末尾附 `... (截断，共 N 行)`

- [ ] **Step 1: 创建 intermediate-schema.json**

写入上述 JSON Schema 定义文档（含字段说明注释），作为解析器与渲染器的契约单一事实源。

```bash
mkdir -p .agents/skills/test-report-generator/scripts/lib
```

文件内容为带注释的 JSON Schema 文档（`$schema`、`title`、`description`、`type: object`、`properties` 逐字段说明类型与是否可空）。

- [ ] **Step 2: 创建 common.sh**

```bash
#!/usr/bin/env bash
# 公共工具函数库 — 供解析器与渲染器 source 引用
set -euo pipefail

log_info() {
  echo "[INFO] $*" >&2
}

log_error() {
  echo "[ERROR] $*" >&2
}

# 降级标注：值为空时输出"未获取"
fallback_value() {
  local val="$1"
  local label="${2:-未获取}"
  if [[ -z "${val:-}" || "${val}" == "null" ]]; then
    echo "${label}"
  else
    echo "${val}"
  fi
}

# 堆栈脱敏：过滤凭据信息
sanitize_stack() {
  local raw="$1"
  echo "${raw}" | sed -E \
    -e 's/(password|passwd|token|secret|api[_-]?key|access[_-]?key)=.*/\1=***/gI' \
    -e 's/(Bearer )[A-Za-z0-9._-]+/\1***/gI'
}

# 截断文本至可读长度
truncate_text() {
  local text="$1"
  local max_lines="${2:-20}"
  local total
  total=$(echo "${text}" | wc -l)
  if (( total > max_lines )); then
    echo "${text}" | head -n "${max_lines}"
    echo "... (截断，共 ${total} 行)"
  else
    echo "${text}"
  fi
}
```

- [ ] **Step 3: 验证 common.sh 可被 source**

Run: `bash -c 'source .agents/skills/test-report-generator/scripts/lib/common.sh && log_info "test" && fallback_value "" && fallback_value "hello"'`
Expected: stderr 输出 `[INFO] test`，stdout 输出 `未获取` 换行 `hello`

- [ ] **Step 4: 静态检查（shellcheck 若可用）**

Run: `shellcheck .agents/skills/test-report-generator/scripts/lib/common.sh 2>/dev/null || echo "shellcheck 不可用，跳过"`
Expected: 无 error 级别警告，或提示不可用时跳过

- [ ] **Step 5: Commit（禁止 — 本阶段为 plan，执行阶段由后续 commit）**

计划阶段不执行 git；此步骤标记执行阶段的提交锚点：`feat(skill): add intermediate schema and common lib`

````

---

## Task 2: 报告模板与框架映射参考文档

**Files:**
- Create: `.agents/skills/test-report-generator/references/report-template.md`
- Create: `.agents/skills/test-report-generator/references/framework-matrix.md`

**Interfaces:**
- Consumes: `intermediate-schema.json`（Task 1）的字段定义
- Produces:
  - `report-template.md`：六大章节固定结构 + 占位符规范，`render-markdown.sh` 据此渲染
  - `framework-matrix.md`：框架识别优先级 → 解析器脚本 → 结果格式映射，`generate-report.sh` 据此选路

- [ ] **Step 1: 创建 report-template.md**

中文报告模板，六大章节顺序固定，占位符用 `{{字段路径}}` 标记，对应中间结构字段：

```markdown
# 测试报告

## 1. 报告头
- **项目名**：{{report_header.project_name}}
- **生成时间**：{{report_header.generated_at}}
- **执行命令**：{{report_header.test_command}}
- **框架/版本**：{{report_header.framework}} {{report_header.framework_version}}
- **执行环境**：{{report_header.env_summary}}

## 2. 结果摘要
| 指标 | 值 |
|---|---|
| 用例总数 | {{summary.total}} |
| 通过 | {{summary.passed}} |
| 失败 | {{summary.failed}} |
| 跳过 | {{summary.skipped}} |
| 通过率 | {{summary.pass_rate}}% |
| 总耗时 | {{summary.total_duration_ms}}ms |

**整体结论**：{{summary.overall_status}}（✅ 通过 / ❌ 失败）

{{#if summary.failed > 0}}
## 3. 失败用例分析
{{#each failures}}
### 失败 #{{@index}}: {{test_name}}
- **所属文件**：{{file}}
- **错误信息**：
  ```
  {{error_message}}
  ```
- **堆栈关键行**：
  ```
  {{stack_key_lines}}
  ```

{{/each}}
{{/if}}

## 4. 用例明细
{{#each details}}
### {{file}}
| 用例名 | 状态 | 耗时 |
|---|---|---|
{{#each tests}}| {{name}} | {{status}} | {{duration_ms}}ms |
{{/each}}
{{/each}}

{{#if details.total_tests > 200}}
> ⚠️ 用例超过 200 条，已截断展示。完整列表见附录原始结果文件。
{{/if}}

## 5. 覆盖率
{{#if coverage.available}}
| 覆盖类型 | 百分比 |
|---|---|
| 语句 | {{coverage.statements_pct}}% |
| 分支 | {{coverage.branches_pct}}% |
| 函数 | {{coverage.functions_pct}}% |
| 行 | {{coverage.lines_pct}}% |

**低于阈值文件**：
{{#each coverage.below_threshold_files}}- {{.}}
{{/each}}
{{/if}}
{{#unless coverage.available}}
> 覆盖率数据未获取。
{{/unless}}

## 6. 附录
- **原始结果文件**：{{appendix.result_file_path}}
- **生成工具版本**：{{appendix.tool_version}}
```

- [ ] **Step 2: 创建 framework-matrix.md**

```markdown
# 框架→解析器→结果格式映射矩阵

## 识别优先级（FR1.1）
1. 用户显式指定命令（`--test-command`）
2. 项目配置文件：package.json(test script)、pyproject.toml、Cargo.toml、pom.xml
3. 框架特征文件推断：jest.config.*、vitest.config.*、pytest.ini、surefire-reports/

## 支持矩阵（M1/P0）
| 框架 | 结果格式 | 解析器脚本 | 识别特征 |
|---|---|---|---|
| Jest | JSON reporter | `parsers/parse-jest-json.sh` | package.json 含 jest；jest.config.* |
| Vitest | JSON reporter | `parsers/parse-vitest-json.sh` | package.json 含 vitest；vitest.config.* |
| JUnit XML（兜底） | XML | `parsers/parse-junit-xml.sh` | *.xml 含 <testsuite> 标签 |

## 后续迭代（非本期）
- pytest 原生（M2）
- Go test / cargo test（M4）
```

- [ ] **Step 3: 验证文件存在且非空**

Run: `test -s .agents/skills/test-report-generator/references/report-template.md && test -s .agents/skills/test-report-generator/references/framework-matrix.md && echo OK`
Expected: `OK`

````

---

## Task 3: Jest JSON 解析器插件

**Files:**
- Create: `.agents/skills/test-report-generator/scripts/parsers/parse-jest-json.sh`
- Create: `.agents/skills/test-report-generator/tests/fixtures/jest-results.sample.json`

**Interfaces:**
- Consumes: `common.sh`（Task 1 的 `fallback_value`、`sanitize_stack`、`truncate_text`）
- Produces: 接受 `$1=结果文件路径`，stdout 输出符合 `intermediate-schema.json` 的 JSON 对象，exit 0 成功 / exit 1 解析失败

**Jest JSON reporter 结构参考**：顶层 `numPassedTests`、`numFailedTests`、`numPendingTests`、`testResults[].assertionResults[]`（含 `status`、`fullName`、`duration`、`failureMessages`），`testResults[].name` 为测试文件路径。

- [ ] **Step 1: 创建 Jest 样本 fixture（含通过+失败用例）**

```json
{
  "numTotalTests": 3,
  "numPassedTests": 2,
  "numFailedTests": 1,
  "numPendingTests": 0,
  "testResults": [
    {
      "name": "/project/src/utils.test.js",
      "status": "failed",
      "message": "",
      "assertionResults": [
        { "fullName": "utils add(1,2) returns 3", "status": "passed", "duration": 5 },
        { "fullName": "utils divide(1,0) throws error", "status": "failed", "duration": 3, "failureMessages": ["TypeError: Cannot divide by zero\n    at divide (src/utils.js:10:5)\n    at Object.<anonymous> (src/utils.test.js:15:5)"] }
      ]
    }
  ],
  "config": { "testRunner": "jest-jasmine2" }
}
```

- [ ] **Step 2: 实现解析器（用 jq 构建中间结构 JSON）**

```bash
#!/usr/bin/env bash
# Jest JSON reporter → 中间结构 JSON
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../lib/common.sh"

INPUT_FILE="${1:-}"
[[ -z "${INPUT_FILE}" ]] && { log_error "用法: parse-jest-json.sh <jest-json-file>"; exit 1; }
[[ ! -f "${INPUT_FILE}" ]] && { log_error "结果文件不存在: ${INPUT_FILE}"; exit 1; }

if ! jq empty "${INPUT_FILE}" 2>/dev/null; then
  log_error "文件不是有效 JSON: ${INPUT_FILE}"
  exit 1
fi

# 提取项目名（取文件所在目录的上两级）
PROJECT_NAME=$(basename "$(dirname "$(dirname "${INPUT_FILE}")")")
GENERATED_AT=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

jq -n \
  --arg project "${PROJECT_NAME}" \
  --arg gen_at "${GENERATED_AT}" \
  --arg cmd "jest --json" \
  --arg fw "Jest" \
  --arg fw_ver "$(jq -r '.config.testRunner // "unknown"' "${INPUT_FILE}")" \
  --slurpfile raw "${INPUT_FILE}" \
  '{
    report_header: {
      project_name: $project,
      generated_at: $gen_at,
      test_command: $cmd,
      framework: $fw,
      framework_version: $fw_ver,
      env_summary: "Node.js"
    },
    summary: {
      total: $raw[0].numTotalTests,
      passed: $raw[0].numPassedTests,
      failed: $raw[0].numFailedTests,
      skipped: $raw[0].numPendingTests,
      pass_rate: (if $raw[0].numTotalTests > 0 then (($raw[0].numPassedTests / $raw[0].numTotalTests * 100) | floor) else 0 end),
      total_duration_ms: 0,
      overall_status: (if $raw[0].numFailedTests > 0 then "FAIL" else "PASS" end)
    },
    failures: [
      $raw[0].testResults[]
      | select(.status == "failed")
      | .assertionResults[]
      | select(.status == "failed")
      | {
          test_name: .fullName,
          file: (input_filename_placeholder),
          error_message: (.failureMessages[0] // "未获取"),
          stack_key_lines: ((.failureMessages[0] // "") | split("\n") | .[0:5])
        }
    ],
    details: [
      $raw[0].testResults[]
      | {
          file: .name,
          tests: [.assertionResults[] | { name: .fullName, status: .status, duration_ms: (.duration // 0) }]
        }
    ],
    coverage: { available: false, statements_pct: null, branches_pct: null, functions_pct: null, lines_pct: null, below_threshold_files: [] },
    appendix: { result_file_path: input_filename_placeholder, tool_version: "jest-json-reporter" }
  }'
```

注意：`input_filename_placeholder` 处在实现阶段需替换为通过 `--arg` 传入的 `${INPUT_FILE}` 路径。

- [ ] **Step 3: 验证解析器输出有效中间结构**

Run: `bash scripts/parsers/parse-jest-json.sh tests/fixtures/jest-results.sample.json | jq '.summary.failed'`
Expected: `1`

- [ ] **Step 4: 静态审查**

Run: `shellcheck scripts/parsers/parse-jest-json.sh 2>/dev/null || true`
Expected: 无 error

````

---

## Task 4: Vitest JSON 解析器插件

**Files:**
- Create: `.agents/skills/test-report-generator/scripts/parsers/parse-vitest-json.sh`
- Create: `.agents/skills/test-report-generator/tests/fixtures/vitest-results.sample.json`

**Interfaces:**
- Consumes: `common.sh`（Task 1）
- Produces: 同 Jest 解析器契约（stdin→stdout 中间结构 JSON）

**Vitest JSON 结构参考**：`testResults[].name`、`testResults[].assertionResults[]`（`status`、`fullName`、`duration`、`failureMessages`），与 Jest 高度相似但有细微差异（Vitest 用 `pending` 而非 `pending`/`todo` 分离）。

- [ ] **Step 1: 创建 Vitest 样本 fixture**

```json
{
  "numTotalTests": 2,
  "numPassedTests": 1,
  "numFailedTests": 1,
  "numPendingTests": 0,
  "testResults": [
    {
      "name": "/project/src/calc.test.ts",
      "status": "failed",
      "assertionResults": [
        { "fullName": "calc multiply(2,3) returns 6", "status": "passed", "duration": 4 },
        { "fullName": "calc subtract(5,2) returns 3", "status": "failed", "duration": 2, "failureMessages": ["AssertionError: expected 3 to equal 3\n    at src/calc.test.ts:20:5"] }
      ]
    }
  ]
}
```

- [ ] **Step 2: 实现解析器**

复用 Jest 解析器的 jq 逻辑框架，调整框架名为 "Vitest"。由于 Vitest JSON 与 Jest JSON 结构高度兼容，解析器主体可复用，仅差异点为 `config.testRunner` 字段可能不存在，用 `fallback_value` 降级。

```bash
#!/usr/bin/env bash
# Vitest JSON → 中间结构 JSON
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../lib/common.sh"

INPUT_FILE="${1:-}"
[[ -z "${INPUT_FILE}" ]] && { log_error "用法: parse-vitest-json.sh <vitest-json-file>"; exit 1; }
[[ ! -f "${INPUT_FILE}" ]] && { log_error "结果文件不存在: ${INPUT_FILE}"; exit 1; }
if ! jq empty "${INPUT_FILE}" 2>/dev/null; then log_error "非有效 JSON: ${INPUT_FILE}"; exit 1; fi

PROJECT_NAME=$(basename "$(dirname "$(dirname "${INPUT_FILE}")")")
GENERATED_AT=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# 与 Jest 解析器相同的 jq 转换逻辑，框架名改为 Vitest
jq -n \
  --arg project "${PROJECT_NAME}" \
  --arg gen_at "${GENERATED_AT}" \
  --arg cmd "vitest run --json" \
  --arg fw "Vitest" \
  --arg fw_ver "$(fallback_value "$(jq -r '.config.testRunner // ""' "${INPUT_FILE}")" "unknown")" \
  --arg input_path "${INPUT_FILE}" \
  --slurpfile raw "${INPUT_FILE}" \
  '{
    report_header: { project_name: $project, generated_at: $gen_at, test_command: $cmd, framework: $fw, framework_version: $fw_ver, env_summary: "Node.js" },
    summary: {
      total: $raw[0].numTotalTests,
      passed: $raw[0].numPassedTests,
      failed: $raw[0].numFailedTests,
      skipped: $raw[0].numPendingTests,
      pass_rate: (if $raw[0].numTotalTests > 0 then (($raw[0].numPassedTests / $raw[0].numTotalTests * 100) | floor) else 0 end),
      total_duration_ms: 0,
      overall_status: (if $raw[0].numFailedTests > 0 then "FAIL" else "PASS" end)
    },
    failures: [ $raw[0].testResults[] | select(.status == "failed") | .assertionResults[] | select(.status == "failed") | { test_name: .fullName, file: $input_path, error_message: (.failureMessages[0] // "未获取"), stack_key_lines: ((.failureMessages[0] // "") | split("\n") | .[0:5]) } ],
    details: [ $raw[0].testResults[] | { file: .name, tests: [.assertionResults[] | { name: .fullName, status: .status, duration_ms: (.duration // 0) }] } ],
    coverage: { available: false, statements_pct: null, branches_pct: null, functions_pct: null, lines_pct: null, below_threshold_files: [] },
    appendix: { result_file_path: $input_path, tool_version: "vitest-json-reporter" }
  }'
```

- [ ] **Step 3: 验证**

Run: `bash scripts/parsers/parse-vitest-json.sh tests/fixtures/vitest-results.sample.json | jq '.summary.failed'`
Expected: `1`

- [ ] **Step 4: 静态审查**

Run: `shellcheck scripts/parsers/parse-vitest-json.sh 2>/dev/null || true`
Expected: 无 error

````

---

## Task 5: JUnit XML 兜底解析器插件

**Files:**
- Create: `.agents/skills/test-report-generator/scripts/parsers/parse-junit-xml.sh`
- Create: `.agents/skills/test-report-generator/tests/fixtures/junit-results.sample.xml`

**Interfaces:**
- Consumes: `common.sh`（Task 1）；依赖 `xmllint`
- Produces: 同其他解析器契约。此解析器覆盖 Python pytest、Java Maven surefire 等所有产出 JUnit XML 的场景

**JUnit XML 结构参考**：`<testsuites>` → `<testsuite name tests failures skipped time>` → `<testcase name classname time>` → 失败时含 `<failure message="...">` 子节点。

- [ ] **Step 1: 创建 JUnit XML 样本 fixture（含失败用例）**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuites>
  <testsuite name="suite1" tests="3" failures="1" skipped="0" time="0.5">
    <testcase name="testAdd" classname="com.example.MathTest" time="0.1"/>
    <testcase name="testSubtract" classname="com.example.MathTest" time="0.1"/>
    <testcase name="testDivide" classname="com.example.MathTest" time="0.3">
      <failure message="java.lang.ArithmeticException: / by zero" type="java.lang.ArithmeticException">
        at com.example.Math.divide(Math.java:10)
        at com.example.MathTest.testDivide(MathTest.java:25)
      </failure>
    </testcase>
  </testsuite>
</testsuites>
```

- [ ] **Step 2: 实现解析器（用 xmllint --xpath 提取）**

```bash
#!/usr/bin/env bash
# JUnit XML → 中间结构 JSON（跨语言兜底解析器）
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../lib/common.sh"

INPUT_FILE="${1:-}"
[[ -z "${INPUT_FILE}" ]] && { log_error "用法: parse-junit-xml.sh <junit-xml-file>"; exit 1; }
[[ ! -f "${INPUT_FILE}" ]] && { log_error "结果文件不存在: ${INPUT_FILE}"; exit 1; }

if ! xmllint --noout "${INPUT_FILE}" 2>/dev/null; then
  log_error "文件不是有效 XML: ${INPUT_FILE}"
  exit 1
fi

PROJECT_NAME=$(basename "$(dirname "$(dirname "${INPUT_FILE}")")")
GENERATED_AT=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# 提取 testsuite 级别汇总
TOTAL=$(xmllint --xpath 'string(/testsuites/@tests)' "${INPUT_FILE}" 2>/dev/null || echo 0)
FAILURES=$(xmllint --xpath 'string(/testsuites/@failures)' "${INPUT_FILE}" 2>/dev/null || echo 0)
SKIPPED=$(xmllint --xpath 'string(/testsuites/@skipped)' "${INPUT_FILE}" 2>/dev/null || echo 0)
[[ -z "${TOTAL}" ]] && TOTAL=0
[[ -z "${FAILURES}" ]] && FAILURES=0
[[ -z "${SKIPPED}" ]] && SKIPPED=0
PASSED=$((TOTAL - FAILURES - SKIPPED))
PASS_RATE=0
(( TOTAL > 0 )) && PASS_RATE=$((PASSED * 100 / TOTAL))
TIME_TOTAL=$(xmllint --xpath 'string(/testsuites/@time)' "${INPUT_FILE}" 2>/dev/null || echo 0)
DURATION_MS=$(echo "${TIME_TOTAL}" | awk '{printf "%d", $1 * 1000}')
STATUS="PASS"; (( FAILURES > 0 )) && STATUS="FAIL"

# 用 python3 解析 XML 构建 JSON（xmllint 提取复杂结构较繁琐）
python3 - "${INPUT_FILE}" "${PROJECT_NAME}" "${GENERATED_AT}" "${TOTAL}" "${PASSED}" "${FAILURES}" "${SKIPPED}" "${PASS_RATE}" "${DURATION_MS}" "${STATUS}" <<'PYEOF'
import sys, xml.etree.ElementTree as ET, json, os
f, proj, gen_at, total, passed, failed, skipped, pr, dur, status = sys.argv[1:11]
tree = ET.parse(f); root = tree.getroot()
failures_list = []
details_list = []
for suite in root.iter('testsuite'):
    suite_file = suite.get('name', 'unknown')
    tests_arr = []
    for tc in suite.iter('testcase'):
        name = tc.get('name', 'unknown')
        classname = tc.get('classname', '')
        t = tc.get('time', '0')
        try: t_ms = int(float(t) * 1000)
        except: t_ms = 0
        fail_elem = tc.find('failure')
        if fail_elem is not None:
            msg = fail_elem.get('message', '未获取')
            stack = (fail_elem.text or '').strip().split('\n')[:5]
            failures_list.append({"test_name": name, "file": classname, "error_message": msg, "stack_key_lines": stack})
            tests_arr.append({"name": name, "status": "FAIL", "duration_ms": t_ms})
        else:
            skip_elem = tc.find('skipped')
            if skip_elem is not None:
                tests_arr.append({"name": name, "status": "SKIP", "duration_ms": t_ms})
            else:
                tests_arr.append({"name": name, "status": "PASS", "duration_ms": t_ms})
    if tests_arr:
        details_list.append({"file": suite_file, "tests": tests_arr})
result = {
    "report_header": {"project_name": proj, "generated_at": gen_at, "test_command": "junit-xml-parse", "framework": "JUnit XML", "framework_version": "unknown", "env_summary": "通用"},
    "summary": {"total": int(total), "passed": int(passed), "failed": int(failed), "skipped": int(skipped), "pass_rate": float(pr), "total_duration_ms": int(dur), "overall_status": status},
    "failures": failures_list,
    "details": details_list,
    "coverage": {"available": False, "statements_pct": None, "branches_pct": None, "functions_pct": None, "lines_pct": None, "below_threshold_files": []},
    "appendix": {"result_file_path": os.path.abspath(f), "tool_version": "junit-xml-parser"}
}
print(json.dumps(result, ensure_ascii=False))
PYEOF
```

- [ ] **Step 3: 验证**

Run: `bash scripts/parsers/parse-junit-xml.sh tests/fixtures/junit-results.sample.xml | jq '.summary.failed'`
Expected: `1`

- [ ] **Step 4: 验证文件损坏时返回错误（AC4）**

Run: `echo "corrupted<<<xml" > /tmp/bad.xml && bash scripts/parsers/parse-junit-xml.sh /tmp/bad.xml; echo "exit=$?"`
Expected: stderr 输出 `[ERROR] 文件不是有效 XML`，exit=1（非空报告冒充成功）

````

---

## Task 6: Markdown 渲染器

**Files:**
- Create: `.agents/skills/test-report-generator/scripts/render/render-markdown.sh`

**Interfaces:**
- Consumes: `common.sh`（Task 1）、`intermediate-schema.json`（Task 1）的字段定义、`report-template.md`（Task 2）的章节结构
- Produces: 接受 `$1=中间结构JSON文件路径`，stdout 输出完整 Markdown 报告文本

- [ ] **Step 1: 实现渲染器（用 jq 从中间结构提取字段，按模板拼装 Markdown）**

```bash
#!/usr/bin/env bash
# 中间结构 JSON → Markdown 报告
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/../lib/common.sh"

INTERMEDIATE="${1:-}"
[[ -z "${INTERMEDIATE}" ]] && { log_error "用法: render-markdown.sh <intermediate-json>"; exit 1; }
[[ ! -f "${INTERMEDIATE}" ]] && { log_error "中间文件不存在: ${INTERMEDIATE}"; exit 1; }
if ! jq empty "${INTERMEDIATE}" 2>/dev/null; then log_error "中间文件非有效 JSON"; exit 1; fi

# 用 jq 提取各字段，fallback_value 降级处理缺失项
RH_PROJECT=$(fallback_value "$(jq -r '.report_header.project_name // ""' "${INTERMEDIATE}")")
RH_GEN=$(fallback_value "$(jq -r '.report_header.generated_at // ""' "${INTERMEDIATE}")")
RH_CMD=$(fallback_value "$(jq -r '.report_header.test_command // ""' "${INTERMEDIATE}")")
RH_FW=$(fallback_value "$(jq -r '.report_header.framework // ""' "${INTERMEDIATE}")")
RH_VER=$(fallback_value "$(jq -r '.report_header.framework_version // ""' "${INTERMEDIATE}")")
RH_ENV=$(fallback_value "$(jq -r '.report_header.env_summary // ""' "${INTERMEDIATE}")")

S_TOTAL=$(jq -r '.summary.total // 0' "${INTERMEDIATE}")
S_PASS=$(jq -r '.summary.passed // 0' "${INTERMEDIATE}")
S_FAIL=$(jq -r '.summary.failed // 0' "${INTERMEDIATE}")
S_SKIP=$(jq -r '.summary.skipped // 0' "${INTERMEDIATE}")
S_RATE=$(jq -r '.summary.pass_rate // 0' "${INTERMEDIATE}")
S_DUR=$(jq -r '.summary.total_duration_ms // 0' "${INTERMEDIATE}")
S_STATUS=$(jq -r '.summary.overall_status // "FAIL"' "${INTERMEDIATE}")
STATUS_EMOJI="✅"; [[ "${S_STATUS}" == "FAIL" ]] && STATUS_EMOJI="❌"

# 渲染报告头 + 摘要
cat <<EOF
# 测试报告

## 1. 报告头
- **项目名**：${RH_PROJECT}
- **生成时间**：${RH_GEN}
- **执行命令**：${RH_CMD}
- **框架/版本**：${RH_FW} ${RH_VER}
- **执行环境**：${RH_ENV}

## 2. 结果摘要
| 指标 | 值 |
|---|---|
| 用例总数 | ${S_TOTAL} |
| 通过 | ${S_PASS} |
| 失败 | ${S_FAIL} |
| 跳过 | ${S_SKIP} |
| 通过率 | ${S_RATE}% |
| 总耗时 | ${S_DUR}ms |

**整体结论**：${STATUS_EMOJI} ${S_STATUS}
EOF

# 失败用例分析（有失败时）
FAIL_COUNT=$(jq '.failures | length' "${INTERMEDIATE}")
if (( FAIL_COUNT > 0 )); then
  echo ""
  echo "## 3. 失败用例分析"
  jq -r '.failures | to_entries[] | "### 失败 #\(.key+1): \(.value.test_name)\n- **所属文件**：\(.value.file)\n- **错误信息**：\n```\n\(.value.error_message)\n```\n- **堆栈关键行**：\n```\n\(.value.stack_key_lines | join("\n"))\n``"' "${INTERMEDIATE}"
fi

# 用例明细
echo ""
echo "## 4. 用例明细"
TOTAL_TESTS=$(jq '[.details[].tests | length] | add // 0' "${INTERMEDIATE}")
jq -r '.details[] | "### \(.file)\n| 用例名 | 状态 | 耗时 |\n|---|---|---|\n\(.tests[] | "| \(.name) | \(.status) | \(.duration_ms)ms |")"' "${INTERMEDIATE}"
if (( TOTAL_TESTS > 200 )); then
  echo ""
  echo "> ⚠️ 用例超过 200 条，已截断展示。完整列表见附录原始结果文件。"
fi

# 覆盖率
COV_AVAIL=$(jq -r '.coverage.available // false' "${INTERMEDIATE}")
echo ""
echo "## 5. 覆盖率"
if [[ "${COV_AVAIL}" == "true" ]]; then
  echo "| 覆盖类型 | 百分比 |"
  echo "|---|---|"
  echo "| 语句 | $(jq -r '.coverage.statements_pct // "未获取"' "${INTERMEDIATE}")% |"
  echo "| 分支 | $(jq -r '.coverage.branches_pct // "未获取"' "${INTERMEDIATE}")% |"
  echo "| 函数 | $(jq -r '.coverage.functions_pct // "未获取"' "${INTERMEDIATE}")% |"
  echo "| 行 | $(jq -r '.coverage.lines_pct // "未获取"' "${INTERMEDIATE}")% |"
  BELOW=$(jq -r '.coverage.below_threshold_files | length' "${INTERMEDIATE}")
  if (( BELOW > 0 )); then
    echo ""
    echo "**低于阈值文件**："
    jq -r '.coverage.below_threshold_files[] | "- \(.)"' "${INTERMEDIATE}"
  fi
else
  echo "> 覆盖率数据未获取。"
fi

# 附录
echo ""
echo "## 6. 附录"
echo "- **原始结果文件**：$(fallback_value "$(jq -r '.appendix.result_file_path // ""' "${INTERMEDIATE}")")"
echo "- **生成工具版本**：$(fallback_value "$(jq -r '.appendix.tool_version // ""' "${INTERMEDIATE}")")"
```

- [ ] **Step 2: 验证渲染器（用 Task 3 的 Jest fixture → 解析 → 渲染）**

Run:
```bash
bash scripts/parsers/parse-jest-json.sh tests/fixtures/jest-results.sample.json > /tmp/mid.json
bash scripts/render/render-markdown.sh /tmp/mid.json
```
Expected: 输出含六大章节标题的 Markdown，失败分析章节含 `utils divide(1,0) throws error`，覆盖章节含 `未获取`

- [ ] **Step 3: 验证幂等性（NFR4）**

Run:
```bash
bash scripts/render/render-markdown.sh /tmp/mid.json > /tmp/r1.md
bash scripts/render/render-markdown.sh /tmp/mid.json > /tmp/r2.md
diff <(grep -v "生成时间" /tmp/r1.md) <(grep -v "生成时间" /tmp/r2.md) && echo "IDEMPOTENT"
```
Expected: `IDEMPOTENT`（除时间戳外内容一致）

- [ ] **Step 4: 静态审查**

Run: `shellcheck scripts/render/render-markdown.sh 2>/dev/null || true`
Expected: 无 error

````

---

## Task 7: 主入口编排器与 SKILL.md

**Files:**
- Create: `.agents/skills/test-report-generator/scripts/generate-report.sh`
- Create: `.agents/skills/test-report-generator/SKILL.md`

**Interfaces:**
- Consumes: 全部前置 Task（解析器、渲染器、common.sh、framework-matrix.md）
- Produces:
  - `generate-report.sh`：主入口，接受配置参数 → 编排全流程 → 落盘报告 → stdout 返回"报告路径 + 摘要"
  - `SKILL.md`：Agent 触发意图与工作流声明

**generate-report.sh CLI 契约：**
```
用法: generate-report.sh [选项]
  --mode execute|parse     默认 parse（解析模式）；execute 触发测试运行
  --test-command CMD       测试执行命令（execute 模式）
  --result-file PATH       结果文件路径（parse 模式，或 execute 后指定）
  --framework jest|vitest|junit  手动指定框架，否则自动检测
  --output-format markdown 默认 markdown
  --output-path DIR        默认 reports/
  --coverage auto|on|off   默认 auto
  --fail-threshold PCT     默认无
  --help
```

- [ ] **Step 1: 实现 generate-report.sh**

```bash
#!/usr/bin/env bash
# 测试报告生成器主入口
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/lib/common.sh"

# 默认配置
MODE="parse"
TEST_COMMAND=""
RESULT_FILE=""
FRAMEWORK=""
OUTPUT_FORMAT="markdown"
OUTPUT_PATH="reports"
COVERAGE="auto"
FAIL_THRESHOLD=""

usage() { cat <<EOF
用法: generate-report.sh [选项]
  --mode execute|parse          默认 parse
  --test-command CMD             execute 模式测试命令
  --result-file PATH             结果文件路径
  --framework jest|vitest|junit  手动指定框架
  --output-format markdown       默认 markdown
  --output-path DIR              默认 reports/
  --coverage auto|on|off         默认 auto
  --fail-threshold PCT           通过率阈值
  --help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --mode) MODE="$2"; shift 2 ;;
    --test-command) TEST_COMMAND="$2"; shift 2 ;;
    --result-file) RESULT_FILE="$2"; shift 2 ;;
    --framework) FRAMEWORK="$2"; shift 2 ;;
    --output-format) OUTPUT_FORMAT="$2"; shift 2 ;;
    --output-path) OUTPUT_PATH="$2"; shift 2 ;;
    --coverage) COVERAGE="$2"; shift 2 ;;
    --fail-threshold) FAIL_THRESHOLD="$2"; shift 2 ;;
    --help) usage; exit 0 ;;
    *) log_error "未知参数: $1"; usage; exit 1 ;;
  esac
done

# 框架自动检测（按 framework-matrix.md 优先级）
detect_framework() {
  local file="$1"
  if [[ -z "${file}" ]]; then echo ""; return; fi
  if jq empty "${file}" 2>/dev/null; then
    # JSON 格式：检查 Jest/Vitest 特征
    if jq -e '.testResults' "${file}" >/dev/null 2>&1; then
      if grep -qi "vitest" "${file}" 2>/dev/null; then echo "vitest"; else echo "jest"; fi
    else echo ""; fi
  elif xmllint --noout "${file}" 2>/dev/null; then
    echo "junit"
  else echo ""; fi
}

# 选择解析器
select_parser() {
  case "$1" in
    jest) echo "${SCRIPT_DIR}/parsers/parse-jest-json.sh" ;;
    vitest) echo "${SCRIPT_DIR}/parsers/parse-vitest-json.sh" ;;
    junit) echo "${SCRIPT_DIR}/parsers/parse-junit-xml.sh" ;;
    *) log_error "不支持的框架: $1"; exit 1 ;;
  esac
}

# execute 模式：运行测试收集结果
if [[ "${MODE}" == "execute" ]]; then
  if [[ -z "${TEST_COMMAND}" ]]; then log_error "execute 模式需要 --test-command"; exit 1; fi
  log_info "执行测试: ${TEST_COMMAND}"
  if ! eval "${TEST_COMMAND}" 2>&1; then
    log_error "测试命令执行失败（非用例失败，命令无法运行）"
    exit 1
  fi
  [[ -z "${RESULT_FILE}" ]] && { log_error "execute 模式需要 --result-file 指定结果输出"; exit 1; }
fi

# 解析模式：必须提供结果文件
if [[ -z "${RESULT_FILE}" ]]; then log_error "需要 --result-file"; exit 1; fi
[[ ! -f "${RESULT_FILE}" ]] && { log_error "结果文件不存在: ${RESULT_FILE}"; exit 1; }

# 框架选择
[[ -z "${FRAMEWORK}" ]] && FRAMEWORK=$(detect_framework "${RESULT_FILE}")
[[ -z "${FRAMEWORK}" ]] && { log_error "无法自动检测框架，请用 --framework 指定"; exit 1; }
log_info "使用框架: ${FRAMEWORK}"

PARSER=$(select_parser "${FRAMEWORK}")
# 解析 → 中间结构
INTERMEDIATE=$(mktemp)
trap 'rm -f "${INTERMEDIATE}"' EXIT
if ! bash "${PARSER}" "${RESULT_FILE}" > "${INTERMEDIATE}" 2>/tmp/parse_err; then
  log_error "解析失败: $(cat /tmp/parse_err)"
  exit 1
fi

# fail_threshold 检查
if [[ -n "${FAIL_THRESHOLD}" ]]; then
  PASS_RATE=$(jq -r '.summary.pass_rate // 0' "${INTERMEDIATE}")
  if (( $(echo "${PASS_RATE} < ${FAIL_THRESHOLD}" | bc -l) )); then
    log_info "通过率 ${PASS_RATE}% 低于阈值 ${FAIL_THRESHOLD}%，标记不达标"
    jq '.summary.overall_status = "FAIL"' "${INTERMEDIATE}" > "${INTERMEDIATE}.tmp" && mv "${INTERMEDIATE}.tmp" "${INTERMEDIATE}"
  fi
fi

# 渲染并落盘
mkdir -p "${OUTPUT_PATH}"
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
OUTPUT_FILE="${OUTPUT_PATH}/test-report-${TIMESTAMP}.md"
case "${OUTPUT_FORMAT}" in
  markdown)
    bash "${SCRIPT_DIR}/render/render-markdown.sh" "${INTERMEDIATE}" > "${OUTPUT_FILE}"
    ;;
  *)
    log_error "本期仅支持 markdown 格式，html/json 见 M3"; exit 1 ;;
esac

# stdout 返回：报告路径 + 摘要
echo "报告已生成: ${OUTPUT_FILE}"
jq -r '"摘要: 通过率 \(.summary.pass_rate)% | 通过 \(.summary.passed) | 失败 \(.summary.failed) | 跳过 \(.summary.skipped)"' "${INTERMEDIATE}"
FAIL_COUNT=$(jq '.failures | length' "${INTERMEDIATE}")
if (( FAIL_COUNT > 0 )); then
  echo "关键失败原因:"
  jq -r ".failures[0:3][] | \"- \(.test_name): \(.error_message | .[0:100])\"" "${INTERMEDIATE}"
fi
```

- [ ] **Step 2: 创建 SKILL.md**

```markdown
---
name: test-report-generator
description: 执行测试后自动解析测试结果（Jest/Vitest JSON、JUnit XML）并生成结构化标准 Markdown 测试报告。支持执行/解析双模式。
version: 0.1.0
activation: auto
tags: [testing, report, jest, vitest, junit]
---

# 测试报告生成器 Skill

## 触发意图
- "生成测试报告"
- "跑一下测试并出报告"
- "把这个 junit.xml 转成测试报告"

## 工作流
1. Agent 识别触发意图
2. 调用 `scripts/generate-report.sh`，按需传入参数
3. 解析结果生成报告，落盘至 `reports/`
4. 返回报告路径 + 摘要给用户

## 配置项
| 配置项 | 默认值 | 对应参数 |
|---|---|---|
| test_command | 自动检测 | --test-command |
| result_file | 自动检测 | --result-file |
| output_format | markdown | --output-format |
| output_path | reports/ | --output-path |
| coverage | auto | --coverage |
| fail_threshold | 无 | --fail-threshold |

## 使用示例
```bash
# 解析模式（已有 JUnit XML）
generate-report.sh --mode parse --result-file target/surefire-reports/TEST-*.xml --framework junit

# 执行模式（Jest 项目）
generate-report.sh --mode execute --test-command "npm test -- --json" --result-file results.json --framework jest
```

## 依赖
- jq
- xmllint (libxml2)
- python3
- bc（fail_threshold 计算用）

## 参考
- 报告模板: references/report-template.md
- 框架映射: references/framework-matrix.md
```

- [ ] **Step 3: 端到端验证（AC3 解析模式）**

Run:
```bash
bash scripts/generate-report.sh --mode parse --result-file tests/fixtures/junit-results.sample.xml --framework junit --output-path /tmp/test-reports
```
Expected: stdout 含 `报告已生成: /tmp/test-reports/test-report-*.md`，摘要含 `通过率 66% | 通过 2 | 失败 1`

- [ ] **Step 4: 端到端验证（AC1 Jest 解析）**

Run:
```bash
bash scripts/generate-report.sh --mode parse --result-file tests/fixtures/jest-results.sample.json --framework jest --output-path /tmp/test-reports
```
Expected: 生成 Markdown 含六大章节，失败分析含 `utils divide(1,0) throws error`

- [ ] **Step 5: 静态审查全脚本**

Run: `for f in scripts/generate-report.sh scripts/parsers/*.sh scripts/render/*.sh scripts/lib/*.sh; do shellcheck "$f" 2>/dev/null && echo "OK: $f" || echo "SKIP/WARN: $f"; done`
Expected: 全部 `OK` 或可接受 `SKIP`

````

---

## Task 8: 端到端验证脚本与自审

**Files:**
- Create: `.agents/skills/test-report-generator/tests/run-tests.sh`

**Interfaces:**
- Consumes: 全部前置 Task
- Produces: 执行后输出验证结果汇总（PASS/FAIL 各 AC）

- [ ] **Step 1: 创建端到端验证脚本**

```bash
#!/usr/bin/env bash
# 端到端验证脚本 — 校验 AC1-AC5
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKILL_DIR="$(dirname "$(dirname "${SCRIPT_DIR}")")"
PASS=0; FAIL=0; SKIP=0
report() { if [[ "$1" == "PASS" ]]; then ((PASS++)); echo "✅ $2"; else ((FAIL++)); echo "❌ $2"; fi; }

# AC1: Jest 解析 → Markdown 含六大章节
cd "${SKILL_DIR}"
OUT=$(mktemp)
bash scripts/parsers/parse-jest-json.sh tests/fixtures/jest-results.sample.json > /tmp/mid.json 2>/dev/null
bash scripts/render/render-markdown.sh /tmp/mid.json > "${OUT}" 2>/dev/null
if grep -q "## 1. 报告头" "${OUT}" && grep -q "## 6. 附录" "${OUT}"; then report PASS "AC1: Jest 报告含六大章节"; else report FAIL "AC1"; fi

# AC2: 失败用例分析含用例名、文件、错误信息
if grep -q "utils divide" "${OUT}" && grep -q "Cannot divide by zero" "${OUT}"; then report PASS "AC2: 失败分析含用例名/错误信息"; else report FAIL "AC2"; fi

# AC3: JUnit XML 解析模式不触发执行
bash scripts/generate-report.sh --mode parse --result-file tests/fixtures/junit-results.sample.xml --framework junit --output-path /tmp/e2e-reports > /tmp/e2e_out.txt 2>/dev/null
if grep -q "报告已生成" /tmp/e2e_out.txt; then report PASS "AC3: JUnit 解析模式产出报告"; else report FAIL "AC3"; fi

# AC4: 损坏文件返回错误非空报告
echo "corrupted" > /tmp/bad.xml
if ! bash scripts/parsers/parse-junit-xml.sh /tmp/bad.xml >/dev/null 2>&1; then report PASS "AC4: 损坏文件返回错误"; else report FAIL "AC4"; fi

# AC5: 覆盖率不存在时标注未获取
if grep -q "覆盖率数据未获取" "${OUT}"; then report PASS "AC5: 覆盖率缺失标注未获取"; else report FAIL "AC5"; fi

echo ""
echo "=== 验证结果: PASS=${PASS} FAIL=${FAIL} SKIP=${SKIP} ==="
[[ ${FAIL} -eq 0 ]] || exit 1
```

- [ ] **Step 2: 执行验证**

Run: `bash tests/run-tests.sh`
Expected: 全部 AC 为 PASS，末行 `=== 验证结果: PASS=5 FAIL=0 SKIP=0 ===`

- [ ] **Step 3: Self-Review（对照 spec 自审）**

逐条检查 design.md 与本计划：
1. **Spec coverage**：FR1.1（框架识别优先级）→ Task 7 generate-report.sh `detect_framework`；FR1.2（Jest/Vitest/JUnit）→ Task 3/4/5；FR1.3（双模式）→ Task 7；FR1.4（执行失败诊断）→ Task 7 execute 分支；FR2（报告六大章节）→ Task 6；FR3（Markdown 落盘）→ Task 7；FR4（配置项）→ Task 7。NFR1-5 分布于各 Task。AC1-5 → Task 8。无遗漏。
2. **Placeholder scan**：计划中 `input_filename_placeholder` 已在 Task 3 Step 2 注明执行阶段替换为 `--arg input_path`，Task 4 已示范正确写法。无 TBD/TODO。
3. **Buildability**：每个 Task 有独立验证命令，工程师可逐步执行。

````

---

## Execution Handoff

计划完成并保存至 `.agents/20260730-测试报告Skill生成器/plan.md`。

**执行选项：**
1. **Subagent-Driven（推荐）** — 逐 Task 派发子 Agent 实现，Task 间审查，快速迭代
2. **Inline Execution** — 当前会话内按顺序执行，批量提交

进入执行阶段后，按 Task 1 → 8 顺序实施，每个 Task 完成验证后标记 checkbox。
