"""
PytestParser — pytest 测试报告解析器。

支持两种格式：
- JUnit XML（由 pytest --junitxml 生成）：让 junit_xml 解析器处理，本解析器不抢 .xml。
- JSON report（由 pytest-json-report 生成）：{ "tests": [...], "summary": {...} }
"""

import json
import os

from ..models import CaseStatus, TestCase, TestResult, TestSuite, Failure
from .base import ParseError, ResultParser
from . import get_registry


class PytestParser(ResultParser):
    """pytest 测试报告解析器（JSON report 格式）。"""

    name = "pytest"

    # outcome → CaseStatus 映射
    _OUTCOME_MAP = {
        "passed": CaseStatus.PASS.value,
        "failed": CaseStatus.FAIL.value,
        "skipped": CaseStatus.SKIP.value,
        "xfailed": CaseStatus.SKIP.value,
        "xpassed": CaseStatus.SKIP.value,
    }

    def can_parse(self, file_path: str) -> bool:
        """判断文件是否为 pytest JSON report 格式。

        - .xml 文件：返回 False，让 junit_xml 解析器处理。
        - .json 文件：json.load 成功且有 "tests"（list）和 "summary"（dict）。
        """
        if file_path.endswith(".xml"):
            return False
        if not file_path.endswith(".json"):
            return False
        try:
            with open(file_path, "r") as f:
                data = json.load(f)
            if not isinstance(data, dict):
                return False
            tests = data.get("tests")
            summary = data.get("summary")
            if not isinstance(tests, list):
                return False
            if not isinstance(summary, dict):
                return False
            return True
        except Exception:
            return False

    def parse(self, file_path: str) -> TestResult:
        """解析 pytest JSON report 文件，返回归一化 TestResult。"""
        try:
            with open(file_path, "r") as f:
                raw = json.load(f)
        except json.JSONDecodeError as e:
            raise ParseError(f"Malformed pytest JSON report: {file_path}\n  {e}")
        except FileNotFoundError:
            raise ParseError(f"File not found: {file_path}")

        if not isinstance(raw, dict):
            raise ParseError(
                f"Root value is not a dict in {file_path}; "
                f"expected a pytest JSON report object"
            )

        tests = raw.get("tests", [])
        if not isinstance(tests, list):
            raise ParseError(
                f"'tests' is not a list in {file_path}"
            )

        # 按 nodeid 中的文件路径分组
        file_groups: dict[str, list[dict]] = {}
        for test in tests:
            if not isinstance(test, dict):
                continue
            nodeid = test.get("nodeid", "")
            file_part = self._parse_nodeid_file(nodeid)
            file_groups.setdefault(file_part, []).append(test)

        suites: list[TestSuite] = []
        for file_name, group_tests in file_groups.items():
            suite = TestSuite(file=file_name)
            for t in group_tests:
                case = self._parse_test(t)
                suite.cases.append(case)
            suites.append(suite)

        result = TestResult(suites=suites)
        result.aggregate()
        return result

    @staticmethod
    def _parse_nodeid_file(nodeid: str) -> str:
        """从 nodeid 提取文件路径（:: 之前的部分）。"""
        if "::" in nodeid:
            return nodeid.split("::")[0]
        return nodeid

    @staticmethod
    def _parse_nodeid_name(nodeid: str) -> str:
        """从 nodeid 提取测试名称（:: 之后的部分）。"""
        if "::" in nodeid:
            parts = nodeid.split("::")
            return "::".join(parts[1:])
        return nodeid

    @classmethod
    def _parse_test(cls, test: dict) -> TestCase:
        """解析单个测试条目为 TestCase。"""
        nodeid = test.get("nodeid", "")
        name = cls._parse_nodeid_name(nodeid) or nodeid

        # 状态映射
        raw_outcome = test.get("outcome", "")
        status = cls._OUTCOME_MAP.get(raw_outcome, CaseStatus.PASS.value)

        # 时长（秒 → 毫秒）
        duration = test.get("duration")
        try:
            duration_ms = float(duration) * 1000.0 if duration is not None else 0.0
        except (ValueError, TypeError):
            duration_ms = 0.0

        # 提取 error / stack
        error_msg = ""
        stack: list[str] = []
        if status == CaseStatus.FAIL.value or status == CaseStatus.ERROR.value:
            call = test.get("call")
            if isinstance(call, dict):
                longrepr = call.get("longrepr", "")
                if isinstance(longrepr, str) and longrepr.strip():
                    lines = longrepr.split("\n")
                    error_msg = lines[0].strip()
                    remaining = [l.strip() for l in lines[1:] if l.strip()]
                    stack = remaining[:10]

        return TestCase(
            name=name,
            status=status,
            duration_ms=duration_ms,
            error=error_msg,
            stack=stack,
        )


# 注册到全局解析器注册表
get_registry().register("pytest", PytestParser)