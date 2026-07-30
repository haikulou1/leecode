"""
Generate Report — 测试报告生成主入口编排模块。

编排流程：
1. parse 模式：ParserRegistry.detect_and_parse 解析结果文件 -> TestResult
2. execute 模式：暂未实现（待 TestExecutor 集成）
3. 覆盖率聚合：CoverageAggregator.collect() 若可获取则附加
4. 安全过滤：Sanitizer.sanitize_result 过滤敏感信息
5. 阈值判定：fail_threshold 低于则标记 below_threshold
6. 渲染落盘：MarkdownRenderer.render 写入 reports/test-report-<timestamp>.md
7. 返回报告路径 + 摘要 + 前 3 条失败
"""

from __future__ import annotations

import dataclasses
import os
from datetime import datetime

# 导入解析器模块，触发模块级自注册（junit_xml / jest_vitest / pytest）
from .parsers import get_registry
from .parsers.junit_xml import JUnitXmlParser  # noqa: F401  触发注册
from .parsers.jest_vitest import JestVitestParser  # noqa: F401  触发注册
from .parsers.pytest import PytestParser  # noqa: F401  触发注册
from .renderer_markdown import MarkdownRenderer
from .sanitizer import Sanitizer
from .coverage import CoverageAggregator
from .models import TestResult


def generate_report(
    mode: str = "parse",
    result_file: str | None = None,
    output_format: str = "markdown",
    output_path: str = "reports",
    project_name: str = "",
    command: str = "",
    framework: str = "",
    framework_version: str = "",
    coverage_dir: str | None = None,
    coverage_mode: str = "auto",
    fail_threshold: float | None = None,
) -> dict:
    """生成测试报告并落盘，返回报告元信息。

    Args:
        mode: "parse" 直接解析结果文件；"execute" 执行测试（暂未实现）。
        result_file: 结果文件路径（parse 模式必填）。
        output_format: 输出格式（当前仅支持 markdown）。
        output_path: 报告输出目录。
        project_name / command / framework / framework_version: 报告头信息。
        coverage_dir: 覆盖率数据目录，None 则不聚合覆盖率。
        coverage_mode: 覆盖率模式 auto / on / off。
        fail_threshold: 通过率阈值，低于则将 summary.status 标记 below_threshold。

    Returns:
        dict: {"report_path", "summary", "failures"}。
    """
    if output_format != "markdown":
        raise NotImplementedError(f"暂不支持的输出格式: {output_format}")

    # 1. 解析 / 执行
    if mode == "execute":
        raise NotImplementedError("Execute mode requires TestExecutor integration")
    if mode != "parse":
        raise NotImplementedError(f"暂不支持的模式: {mode}")

    if result_file is None:
        raise FileNotFoundError(
            "parse 模式必须提供 result_file（结果文件路径）"
        )
    if not os.path.isfile(result_file):
        raise FileNotFoundError(
            f"结果文件不存在: {result_file}（请检查路径或先执行测试生成结果文件）"
        )

    result: TestResult = get_registry().detect_and_parse(result_file)

    # 2. 覆盖率聚合
    if coverage_dir:
        coverage = CoverageAggregator(coverage_dir, coverage_mode).collect()
        if coverage is not None:
            result.coverage = coverage

    # 3. 安全过滤
    result = Sanitizer().sanitize_result(result)

    # 4. 阈值判定
    if fail_threshold is not None and result.summary.pass_rate < fail_threshold:
        result.summary.status = "below_threshold"

    # 5. 渲染落盘
    os.makedirs(output_path, exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    filename = f"test-report-{timestamp}.md"
    report_path = os.path.join(output_path, filename)

    markdown = MarkdownRenderer().render(
        result,
        project_name=project_name,
        command=command or result_file or "",
        framework=framework,
        framework_version=framework_version,
    )
    with open(report_path, "w", encoding="utf-8") as f:
        f.write(markdown)

    # 6. 返回元信息
    summary_dict = dataclasses.asdict(result.summary)
    failures_dict = [dataclasses.asdict(fa) for fa in result.failures[:3]]

    return {
        "report_path": report_path,
        "summary": summary_dict,
        "failures": failures_dict,
    }
