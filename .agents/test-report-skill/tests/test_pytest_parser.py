"""
测试 pytest 解析器 (PytestParser)。

覆盖场景：
- 通过 registry 路由 pytest JUnit XML 到 junit_xml 解析器
- 解析 pytest JSON report
- 格式错误 XML 通过 registry 抛出 ParseError
- can_parse 检测
- pytest 解析器不抢 .xml 文件
"""

import os
import sys
import unittest

# 将 src 目录加入 sys.path
_src_path = os.path.join(os.path.dirname(__file__), "..", "src")
if _src_path not in sys.path:
    sys.path.insert(0, os.path.abspath(_src_path))

from src.parsers import get_registry
from src.parsers.pytest import PytestParser
from src.parsers.base import ParseError
from src.parsers.junit_xml import JUnitXmlParser  # noqa: F401 — 触发注册，使 registry 能路由到它
from src.models import CaseStatus, Failure, TestResult


def _fixture(name: str) -> str:
    """返回 fixture 文件的绝对路径。"""
    return os.path.join(os.path.dirname(__file__), "fixtures", name)


class TestPytestParser(unittest.TestCase):
    """PytestParser 单元测试。"""

    def setUp(self):
        self.parser = PytestParser()

    def test_parse_junit_xml(self):
        """通过 registry，pytest_junit.xml 应被 junit_xml parser 解析（total==4）。"""
        result = get_registry().detect_and_parse(_fixture("pytest_junit.xml"))
        self.assertIsInstance(result, TestResult)
        self.assertEqual(result.summary.total, 4)
        # 验证 junit_xml 正确解析了 pytest 风格的 JUnit XML
        self.assertEqual(result.summary.passed, 2)
        self.assertEqual(result.summary.failed, 1)
        self.assertEqual(result.summary.skipped, 1)
        self.assertEqual(result.summary.status, "fail")
        self.assertEqual(len(result.suites), 1)
        self.assertEqual(result.suites[0].file, "pytest")
        cases = result.suites[0].cases
        self.assertEqual(cases[0].status, CaseStatus.PASS.value)
        self.assertEqual(cases[1].status, CaseStatus.PASS.value)
        self.assertEqual(cases[2].status, CaseStatus.FAIL.value)
        self.assertEqual(cases[3].status, CaseStatus.SKIP.value)

    def test_parse_json(self):
        """pytest JSON report 解析验证。"""
        result = self.parser.parse(_fixture("pytest_json.json"))
        self.assertIsInstance(result, TestResult)
        self.assertEqual(result.summary.total, 3)
        self.assertEqual(result.summary.passed, 1)
        self.assertEqual(result.summary.failed, 1)
        self.assertEqual(result.summary.skipped, 1)
        self.assertEqual(result.summary.status, "fail")
        # 验证 failures 列表
        self.assertEqual(len(result.failures), 1)
        failure = result.failures[0]
        self.assertIsInstance(failure, Failure)
        self.assertEqual(failure.name, "test_fail")
        self.assertEqual(failure.file, "tests/test_foo.py")
        self.assertEqual(failure.error, "AssertionError: assert 1 == 2")
        self.assertTrue(len(failure.stack_excerpt) > 0)
        self.assertLessEqual(len(failure.stack_excerpt), 10)
        # 验证各 case 状态
        cases = result.suites[0].cases
        self.assertEqual(cases[0].status, CaseStatus.PASS.value)
        self.assertEqual(cases[1].status, CaseStatus.FAIL.value)
        self.assertEqual(cases[2].status, CaseStatus.SKIP.value)

    def test_malformed_raises_parse_error(self):
        """pytest_malformed.xml 通过 registry.detect_and_parse 应抛 ParseError。"""
        with self.assertRaises(ParseError):
            get_registry().detect_and_parse(_fixture("pytest_malformed.xml"))

    def test_can_parse_json(self):
        """can_parse 对有效的 pytest JSON report 返回 True。"""
        self.assertTrue(self.parser.can_parse(_fixture("pytest_json.json")))

    def test_cannot_parse_xml(self):
        """can_parse 对 pytest JUnit XML 返回 False（不抢 xml）。"""
        self.assertFalse(self.parser.can_parse(_fixture("pytest_junit.xml")))

    def test_cannot_parse_nonexistent(self):
        """can_parse 对不存在文件返回 False。"""
        self.assertFalse(self.parser.can_parse("/nonexistent/path/report.json"))
        # 非 .json 文件
        self.assertFalse(self.parser.can_parse(__file__))  # .py 文件


if __name__ == "__main__":
    unittest.main()