"""
JestVitestParser — Jest/Vitest JSON 报告格式解析器。

解析 Jest JSON reporter 或 Vitest JSON 输出文件。
格式: {"testResults": [{"name": "suite", "assertionResults": [...]}]}
"""

import json
import os

from ..models import CaseStatus, TestCase, TestResult, TestSuite, Failure
from .base import ParseError, ResultParser
from . import get_registry


class JestVitestParser(ResultParser):
    """Jest/Vitest JSON 格式解析器。"""

    name = "jest_vitest"

    def can_parse(self, file_path: str) -> bool:
        """判断文件是否为 Jest/Vitest JSON 格式。

        检查: .json 后缀，json.load 成功，有 "testResults" 键（list），
        且第一个元素包含 "assertionResults"。
        """
        if not file_path.endswith(".json"):
            return False
        try:
            with open(file_path, "r") as f:
                data = json.load(f)
            if not isinstance(data, dict):
                return False
            test_results = data.get("testResults")
            if not isinstance(test_results, list) or len(test_results) == 0:
                return False
            first = test_results[0]
            if not isinstance(first, dict):
                return False
            if "assertionResults" not in first:
                return False
            return True
        except Exception:
            return False

    def parse(self, file_path: str) -> TestResult:
        """解析 Jest/Vitest JSON 文件，返回归一化 TestResult。"""
        try:
            with open(file_path, "r") as f:
                raw = json.load(f)
        except json.JSONDecodeError as e:
            raise ParseError(f"Malformed JSON file: {file_path}\n  {e}")
        except FileNotFoundError:
            raise ParseError(f"File not found: {file_path}")

        if not isinstance(raw, dict) or "testResults" not in raw:
            raise ParseError(
                f"Missing 'testResults' key in {file_path}; "
                f"not a valid Jest/Vitest JSON report"
            )

        test_results = raw["testResults"]
        if not isinstance(test_results, list):
            raise ParseError(
                f"'testResults' is not a list in {file_path}"
            )

        suites: list[TestSuite] = []

        for entry in test_results:
            if not isinstance(entry, dict):
                continue
            suite_file = entry.get("name", "")
            suite = TestSuite(file=suite_file)

            assertion_results = entry.get("assertionResults", [])
            if not isinstance(assertion_results, list):
                assertion_results = []

            for assertion in assertion_results:
                if not isinstance(assertion, dict):
                    continue
                case = self._parse_assertion(assertion)
                suite.cases.append(case)

            suites.append(suite)

        result = TestResult(suites=suites)
        result.aggregate()
        return result

    @staticmethod
    def _parse_assertion(assertion: dict) -> TestCase:
        """解析单个 assertionResult 为 TestCase。"""
        # 名称: 优先用 fullName，回退到 name
        name = assertion.get("fullName") or assertion.get("name", "")

        # 状态映射
        raw_status = assertion.get("status", "")
        status_map = {
            "passed": CaseStatus.PASS.value,
            "failed": CaseStatus.FAIL.value,
            "pending": CaseStatus.SKIP.value,
            "skipped": CaseStatus.SKIP.value,
            "todo": CaseStatus.SKIP.value,
        }
        status = status_map.get(raw_status, CaseStatus.PASS.value)

        # 时长: 优先 assertion.duration，缺失则 0.0
        duration = assertion.get("duration")
        if duration is not None:
            try:
                duration_ms = float(duration)
            except (ValueError, TypeError):
                duration_ms = 0.0
        else:
            duration_ms = 0.0

        # 提取 failureMessages -> error + stack
        error_msg = ""
        stack: list[str] = []
        failure_messages = assertion.get("failureMessages", [])
        if failure_messages and isinstance(failure_messages, list) and len(failure_messages) > 0:
            first_msg = failure_messages[0]
            if isinstance(first_msg, str) and first_msg.strip():
                lines = first_msg.split("\n")
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
get_registry().register("jest_vitest", JestVitestParser)