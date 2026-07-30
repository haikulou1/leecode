"""
MarkdownRenderer — 将 TestResult 渲染为中文 Markdown 测试报告.

基于 FR2 固定章节顺序：
  1. 报告头
  2. 结果摘要
  3. 失败用例分析
  4. 用例明细
  5. 覆盖率
  6. 附录

所有报告头信息（项目名/命令/框架等）从 render() 方法参数获取，
TestResult 本身不含 ReportMeta。
"""

from __future__ import annotations

import platform
import sys
from datetime import datetime, timezone

from .models import CaseStatus, TestResult

__all__ = ["MarkdownRenderer"]

# 用例明细每条用例的截断阈值。
_MAX_DETAIL_CASES = 200
# 失败用例堆栈关键行截断行数。
_MAX_STACK_LINES = 10
# 附录工具版本。
_TOOL_VERSION = "test-report-skill v0.1.0"

# 状态 → 明细行图标。
_STATUS_ICONS = {
    CaseStatus.PASS.value: "[✅]",
    CaseStatus.FAIL.value: "[❌]",
    CaseStatus.SKIP.value: "[⏭️]",
    CaseStatus.ERROR.value: "[⚠️]",
}


class MarkdownRenderer:
    """将归一化 TestResult 渲染为中文 Markdown 测试报告。"""

    def render(
        self,
        result: TestResult,
        *,
        project_name: str = "",
        command: str = "",
        framework: str = "",
        framework_version: str = "",
    ) -> str:
        """渲染完整 Markdown 报告。

        Args:
            result: 归一化测试结果。
            project_name: 项目名（空则显示「未指定」）。
            command: 执行命令（空则显示「未指定」）。
            framework: 测试框架名（空则显示「未指定」）。
            framework_version: 框架版本（空则显示「未指定」）。

        Returns:
            Markdown 字符串。
        """
        sections: list[str] = [
            self._render_header(
                project_name=project_name,
                command=command,
                framework=framework,
                framework_version=framework_version,
            ),
            self._render_summary(result),
            self._render_failures(result),
            self._render_detail(result),
            self._render_coverage(result),
            self._render_appendix(),
        ]
        return "\n\n".join(sections) + "\n"

    # ------------------------------------------------------------------ #
    # 章节渲染
    # ------------------------------------------------------------------ #

    def _render_header(
        self,
        *,
        project_name: str,
        command: str,
        framework: str,
        framework_version: str,
    ) -> str:
        generated_at = datetime.now(timezone.utc).isoformat()
        env_summary = f"Python {self._py_version()} on {platform.platform()}"
        framework_line = self._join_framework(framework, framework_version)
        lines = [
            "# 测试报告",
            "",
            f"- 项目名称：{self._or_unspecified(project_name)}",
            f"- 生成时间：{generated_at}",
            f"- 执行命令：{self._or_unspecified(command)}",
            f"- 测试框架：{self._or_unspecified(framework_line)}",
            f"- 执行环境：{env_summary}",
        ]
        return "\n".join(lines)

    def _render_summary(self, result: TestResult) -> str:
        summary = result.summary
        pass_rate = summary.pass_rate
        conclusion = "✅ 通过" if summary.status == CaseStatus.PASS.value else "❌ 失败"
        lines = [
            "## 结果摘要",
            "",
            "| 指标 | 数值 |",
            "| --- | --- |",
            f"| 用例总数 | {summary.total} |",
            f"| 通过 | {summary.passed} |",
            f"| 失败 | {summary.failed} |",
            f"| 跳过 | {summary.skipped} |",
            f"| 通过率 | {self._fmt_rate(pass_rate)} |",
            f"| 总耗时 | {self._fmt_duration(summary.duration_ms)} |",
            f"| 整体结论 | {conclusion} |",
        ]
        return "\n".join(lines)

    def _render_failures(self, result: TestResult) -> str:
        failures = result.failures
        if not failures:
            return "## 失败用例分析\n\n无失败用例。"

        blocks = ["## 失败用例分析"]
        for idx, failure in enumerate(failures, start=1):
            stack_lines = failure.stack_excerpt[:_MAX_STACK_LINES]
            truncated = len(failure.stack_excerpt) - _MAX_STACK_LINES
            stack_block = "\n".join(
                f"    {line}" for line in stack_lines
            ) if stack_lines else "    （无堆栈信息）"
            blocks.append(
                f"### {idx}. {failure.name}"
            )
            blocks.append("")
            blocks.append(f"- 所属文件：{self._or_unspecified(failure.file)}")
            blocks.append(f"- 错误信息：{self._or_unspecified(failure.error)}")
            if truncated > 0:
                blocks.append(f"- 堆栈关键行（已截断 {truncated} 行）：")
            else:
                blocks.append("- 堆栈关键行：")
            blocks.append("")
            blocks.append("```")
            blocks.append(stack_block)
            blocks.append("```")
        return "\n".join(blocks)

    def _render_detail(self, result: TestResult) -> str:
        blocks = ["## 用例明细"]
        total_cases = sum(len(suite.cases) for suite in result.suites)
        shown = 0
        truncated = False

        for suite in result.suites:
            if not suite.cases:
                continue
            suite_header = f"### {suite.file or '（未知文件）'}"
            blocks.append("")
            blocks.append(suite_header)
            blocks.append("")
            for case in suite.cases:
                if shown >= _MAX_DETAIL_CASES:
                    truncated = True
                    break
                icon = _STATUS_ICONS.get(case.status, "[?]")
                blocks.append(
                    f"- {icon} {case.name} — {self._fmt_duration(case.duration_ms)}"
                )
                shown += 1
            if truncated:
                break

        if total_cases > _MAX_DETAIL_CASES:
            truncated = True

        blocks.append("")
        if truncated:
            omitted = total_cases - _MAX_DETAIL_CASES
            blocks.append(
                f"> 用例数超过 {_MAX_DETAIL_CASES} 条，已截断，省略 {omitted} 条。"
            )
        else:
            blocks.append(f"> 共 {total_cases} 条用例。")
        return "\n".join(blocks)

    def _render_coverage(self, result: TestResult) -> str:
        coverage = result.coverage
        if coverage is None:
            return "## 覆盖率\n\n未获取"

        lines = [
            "## 覆盖率",
            "",
            "| 指标 | 覆盖率 |",
            "| --- | --- |",
            f"| 语句覆盖 | {self._fmt_rate(coverage.statements)} |",
            f"| 分支覆盖 | {self._fmt_rate(coverage.branches)} |",
            f"| 函数覆盖 | {self._fmt_rate(coverage.functions)} |",
            f"| 行覆盖 | {self._fmt_rate(coverage.lines)} |",
        ]
        low_files = coverage.low_coverage_files or []
        if low_files:
            lines.append("")
            lines.append("**低于阈值文件清单**：")
            for path in low_files:
                lines.append(f"- {path}")
        else:
            lines.append("")
            lines.append("**低于阈值文件清单**：无")
        return "\n".join(lines)

    def _render_appendix(self) -> str:
        return f"## 附录\n\n- 生成工具：{_TOOL_VERSION}"

    # ------------------------------------------------------------------ #
    # 格式化辅助
    # ------------------------------------------------------------------ #

    @staticmethod
    def _or_unspecified(value: str) -> str:
        return value if value else "未指定"

    @staticmethod
    def _join_framework(framework: str, framework_version: str) -> str:
        if framework and framework_version:
            return f"{framework} {framework_version}"
        return framework or framework_version

    @staticmethod
    def _fmt_rate(value: float) -> str:
        return f"{value:.1f}%"

    @staticmethod
    def _fmt_duration(ms: float) -> str:
        return f"{ms:.1f} ms"

    @staticmethod
    def _py_version() -> str:
        return ".".join(str(p) for p in sys.version_info[:3])
