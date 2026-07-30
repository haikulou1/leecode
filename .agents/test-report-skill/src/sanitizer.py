"""
Sanitizer — 敏感信息脱敏 (NFR3).

过滤测试输出中的凭据、密钥、环境变量等敏感信息。
"""

from __future__ import annotations

import dataclasses
import re

from .models import Failure, TestCase, TestResult, TestSuite


class Sanitizer:
    """敏感信息脱敏器。"""

    # ── 敏感模式 ─────────────────────────────────────────────────
    _REDACTED = "***REDACTED***"

    PATTERNS: list[tuple[re.Pattern, str]] = [
        # OpenAI API key: sk-...
        (re.compile(r"sk-[A-Za-z0-9]{20,}"), "sk-" + _REDACTED),
        # AWS access key: AKIA...
        (re.compile(r"AKIA[0-9A-Z]{16}"), _REDACTED),
        # Generic secret assignment: password=..., secret=..., token=..., api_key=...
        (
            re.compile(
                r"(password|passwd|secret|token|api_key|apikey)\s*[=:]\s*\S+",
                re.IGNORECASE,
            ),
            r"\1=" + _REDACTED,
        ),
        # Environment variable assignment in stack: VAR=long_value
        (re.compile(r"([A-Z_]{3,}=)(?:\S{8,})"), _REDACTED),
    ]

    def sanitize_text(self, text: str) -> str:
        """对文本应用所有脱敏规则，返回清理后的文本。"""
        if not text:
            return text
        result = text
        for pattern, replacement in self.PATTERNS:
            result = pattern.sub(replacement, result)
        return result

    def sanitize_result(self, result: TestResult) -> TestResult:
        """对 TestResult 中的敏感信息逐项脱敏，返回新对象（不原地修改）。

        Args:
            result: 原始 TestResult。

        Returns:
            脱敏后的新 TestResult。
        """
        # 复制 suites
        new_suites: list[TestSuite] = []
        for suite in result.suites:
            new_cases: list[TestCase] = []
            for case in suite.cases:
                new_case = TestCase(
                    name=case.name,
                    status=case.status,
                    duration_ms=case.duration_ms,
                    error=self.sanitize_text(case.error),
                    stack=[self.sanitize_text(s) for s in case.stack],
                )
                new_cases.append(new_case)
            new_suites.append(
                TestSuite(file=suite.file, cases=new_cases)
            )

        # 复制 failures
        new_failures: list[Failure] = []
        for f in result.failures:
            new_failures.append(
                Failure(
                    name=f.name,
                    file=f.file,
                    error=self.sanitize_text(f.error),
                    stack_excerpt=[self.sanitize_text(s) for s in f.stack_excerpt],
                )
            )

        # 构建新 TestResult
        new_result = TestResult(
            summary=dataclasses.replace(result.summary),
            suites=new_suites,
            failures=new_failures,
            coverage=result.coverage,
        )
        return new_result