---
name: test-report-skill
description: 测试执行后自动解析结果并生成结构化测试报告（Markdown/HTML/JSON）
version: 0.1.0
trigger:
  - 生成测试报告
  - 跑一下测试并出报告
  - 把这个 junit.xml 转成测试报告
  - test report
---

# Test Report Skill

测试报告生成 Skill：测试执行后自动解析结果文件，经覆盖率聚合、安全过滤、阈值判定后，
渲染为结构化 Markdown 测试报告并落盘返回报告路径与摘要。

## 工作流

1. 检测框架：FrameworkDetector.detect(project_dir) 识别 Jest/Vitest/pytest/JUnit XML
2. 执行或解析：执行模式 TestExecutor.run(command) / 解析模式跳过执行直接解析
3. 解析结果：ParserRegistry.detect_and_parse(file_path) 输出 TestResult
4. 覆盖率聚合：CoverageAggregator.collect() 若可获取
5. 安全过滤：Sanitizer.sanitize_result(result) 过滤敏感信息
6. 渲染报告：MarkdownRenderer.render(result) 生成 Markdown 报告
7. 落盘返回：写入 reports/test-report-<timestamp>.md，返回报告路径+摘要

## 入口

`src/generate_report.py` 提供 `generate_report(...)` 主入口函数，按上述工作流编排
解析器注册表、覆盖率聚合器、安全过滤、阈值判定与 Markdown 渲染器。

## 数据契约

- `TestResult.summary` 为 `Summary` dataclass（属性访问 `summary.total`，非 dict）
- `TestResult.failures` 为 `list[Failure]` dataclass
- 无 ReportMeta：报告头信息通过 `MarkdownRenderer.render()` 关键字参数传递
- 解析器在模块导入时自注册：junit_xml / jest_vitest / pytest 末尾调用
  `get_registry().register(...)`；generate_report 需 import 这些模块触发注册
- `MarkdownRenderer.render(result, *, project_name="", command="", framework="", framework_version="")`
- `Sanitizer().sanitize_result(result) -> TestResult`（返回新对象）
- `CoverageAggregator(project_dir, mode="auto").collect() -> Optional[Coverage]`

## 配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| test_command | 自动检测 | 测试执行命令 |
| result_file | 自动检测 | 解析模式下的结果文件路径 |
| output_format | markdown | markdown / html / json |
| output_path | reports/ | 报告输出目录 |
| coverage | auto | auto / on / off |
| fail_threshold | 无 | 通过率低于该值标记不达标 |
