"""
TestResult — 归一化测试报告数据契约.

基于 clarify.md §5.2 定义。
所有字段使用 Python stdlib dataclasses，零外部依赖。
"""

from __future__ import annotations

import dataclasses
from dataclasses import dataclass, field
from enum import Enum
from typing import Any, Optional


class CaseStatus(str, Enum):
    """用例执行状态枚举。"""

    PASS = "pass"
    FAIL = "fail"
    SKIP = "skip"
    ERROR = "error"


@dataclass
class TestCase:
    """单个测试用例。"""

    name: str = ""
    status: str = ""
    duration_ms: float = 0.0
    error: str = ""
    stack: list[str] = field(default_factory=list)


@dataclass
class TestSuite:
    """测试套件（一个文件对应一个 suite）。"""

    file: str = ""
    cases: list[TestCase] = field(default_factory=list)


@dataclass
class Summary:
    """测试摘要统计。"""

    total: int = 0
    passed: int = 0
    failed: int = 0
    skipped: int = 0
    pass_rate: float = 0.0
    duration_ms: float = 0.0
    status: str = "pass"

    def compute_pass_rate(self) -> None:
        """根据 total/passed/failed 计算 pass_rate 并设置 status。"""
        if self.total > 0:
            self.pass_rate = (self.passed / self.total) * 100.0
        else:
            self.pass_rate = 0.0
        self.status = "fail" if self.failed > 0 else "pass"


@dataclass
class Coverage:
    """覆盖率数据。"""

    lines: float = 0.0
    branches: float = 0.0
    functions: float = 0.0
    statements: float = 0.0
    low_coverage_files: list = field(default_factory=list)

    @classmethod
    def unknown(cls) -> Coverage:
        """返回表示「未获取」的 Coverage（全零 + 空列表）。"""
        return cls(lines=0.0, branches=0.0, functions=0.0, statements=0.0, low_coverage_files=[])


@dataclass
class Failure:
    """失败用例详情。"""

    name: str = ""
    file: str = ""
    error: str = ""
    stack_excerpt: list[str] = field(default_factory=list)


@dataclass
class TestResult:
    """归一化测试结果 — 顶层数据契约。"""

    summary: Summary = field(default_factory=Summary)
    suites: list[TestSuite] = field(default_factory=list)
    failures: list[Failure] = field(default_factory=list)
    coverage: Optional[Coverage] = None

    @classmethod
    def empty(cls) -> TestResult:
        """返回空的 TestResult（无覆盖率）。"""
        return cls(
            summary=Summary(),
            suites=[],
            failures=[],
            coverage=None,
        )

    def aggregate(self) -> TestResult:
        """从 suites 统计 summary，收集 failures。返回 self 以支持链式调用。"""
        total = 0
        passed = 0
        failed = 0
        skipped = 0
        total_duration = 0.0
        collected_failures: list[Failure] = []

        for suite in self.suites:
            for case in suite.cases:
                total += 1
                total_duration += case.duration_ms
                if case.status == CaseStatus.PASS.value:
                    passed += 1
                elif case.status == CaseStatus.FAIL.value:
                    failed += 1
                    collected_failures.append(
                        Failure(
                            name=case.name,
                            file=suite.file,
                            error=case.error,
                            stack_excerpt=case.stack,
                        )
                    )
                elif case.status == CaseStatus.ERROR.value:
                    failed += 1  # error 归入 failed 计数
                    collected_failures.append(
                        Failure(
                            name=case.name,
                            file=suite.file,
                            error=case.error,
                            stack_excerpt=case.stack,
                        )
                    )
                elif case.status == CaseStatus.SKIP.value:
                    skipped += 1

        self.summary.total = total
        self.summary.passed = passed
        self.summary.failed = failed
        self.summary.skipped = skipped
        self.summary.duration_ms = total_duration
        self.summary.compute_pass_rate()
        self.failures = collected_failures

        return self

    def to_dict(self) -> dict[str, Any]:
        """将 TestResult 序列化为 JSON 兼容 dict。"""
        return dataclasses.asdict(self)

    @classmethod
    def from_dict(cls, d: dict[str, Any]) -> TestResult:
        """从 dict 反序列化回 TestResult。"""
        summary = Summary(**d.get("summary", {}))
        suites = []
        for s in d.get("suites", []):
            cases = [TestCase(**c) for c in s.get("cases", [])]
            suites.append(TestSuite(file=s.get("file", ""), cases=cases))
        failures = [Failure(**f) for f in d.get("failures", [])]
        coverage = None
        if d.get("coverage") is not None:
            coverage = Coverage(**d["coverage"])
        return cls(summary=summary, suites=suites, failures=failures, coverage=coverage)