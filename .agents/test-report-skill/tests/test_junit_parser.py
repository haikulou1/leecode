"""
测试 JUnit XML 解析器 (JUnitXmlParser)。

覆盖场景：
- 全通过
- 混合结果（pass/fail/skip）
- 多 suite
- 格式错误 XML
- can_parse 检测
- can_parse 对不存在文件返回 False
"""

import os
import sys
import unittest

# 将 src 目录加入 sys.path，使 from src.parsers.junit_xml import ... 可工作
_src_path = os.path.join(os.path.dirname(__file__), "..", "src")
if _src_path not in sys.path:
    sys.path.insert(0, os.path.abspath(_src_path))

from src.parsers.junit_xml import JUnitXmlParser
from src.models import CaseStatus, Failure, TestResult


def _fixture(name: str) -> str:
    """返回 fixture 文件的绝对路径。"""
    return os.path.join(os.path.dirname(__file__), "fixtures", name)


class TestJUnitXmlParser(unittest.TestCase):
    """JUnitXmlParser 单元测试。"""

    def setUp(self):
        self.parser = JUnitXmlParser()

    def test_parse_pass(self):
        """全通过场景：3 个 PASS，无失败。"""
        result = self.parser.parse(_fixture("junit_pass.xml"))
        self.assertIsInstance(result, TestResult)
        self.assertEqual(result.summary.total, 3)
        self.assertEqual(result.summary.passed, 3)
        self.assertEqual(result.summary.failed, 0)
        self.assertEqual(result.summary.skipped, 0)
        self.assertEqual(result.summary.status, "pass")
        self.assertEqual(len(result.suites), 1)
        self.assertEqual(result.suites[0].file, "PassingSuite")
        self.assertEqual(len(result.suites[0].cases), 3)
        for case in result.suites[0].cases:
            self.assertEqual(case.status, CaseStatus.PASS.value)
            self.assertGreater(case.duration_ms, 0.0)

    def test_parse_fail(self):
        """混合场景：2 pass, 1 fail, 1 skip, 含 <testsuites> 根包装。"""
        result = self.parser.parse(_fixture("junit_fail.xml"))
        self.assertEqual(result.summary.total, 4)
        self.assertEqual(result.summary.passed, 2)
        self.assertEqual(result.summary.failed, 1)
        self.assertEqual(result.summary.skipped, 1)
        self.assertEqual(result.summary.status, "fail")
        # 验证 failures 列表
        self.assertEqual(len(result.failures), 1)
        failure = result.failures[0]
        self.assertIsInstance(failure, Failure)
        self.assertEqual(failure.name, "testFail")
        self.assertEqual(failure.file, "MixedSuite")
        self.assertEqual(failure.error, "AssertionError")
        self.assertTrue(len(failure.stack_excerpt) > 0)
        # 验证 stack 截断
        self.assertLessEqual(len(failure.stack_excerpt), 10)
        # 验证各 case 状态
        cases = result.suites[0].cases
        self.assertEqual(cases[0].status, CaseStatus.PASS.value)
        self.assertEqual(cases[1].status, CaseStatus.PASS.value)
        self.assertEqual(cases[2].status, CaseStatus.FAIL.value)
        self.assertEqual(cases[3].status, CaseStatus.SKIP.value)

    def test_parse_multi(self):
        """多 suite 场景：<testsuites> 包含 2 个 suite。"""
        result = self.parser.parse(_fixture("junit_multi.xml"))
        self.assertEqual(len(result.suites), 2)
        self.assertEqual(result.suites[0].file, "SuiteOne")
        self.assertEqual(result.suites[1].file, "SuiteTwo")
        self.assertEqual(result.summary.total, 4)
        self.assertEqual(result.summary.passed, 3)
        self.assertEqual(result.summary.failed, 1)
        # SuiteOne 全 PASS
        for case in result.suites[0].cases:
            self.assertEqual(case.status, CaseStatus.PASS.value)
        # SuiteTwo 有 1 个 FAIL
        self.assertEqual(result.suites[1].cases[0].status, CaseStatus.PASS.value)
        self.assertEqual(result.suites[1].cases[1].status, CaseStatus.FAIL.value)

    def test_malformed_raises_parse_error(self):
        """格式错误 XML 应抛出 ParseError (AC4)。"""
        from src.parsers.base import ParseError

        with self.assertRaises(ParseError):
            self.parser.parse(_fixture("junit_malformed.xml"))

    def test_can_parse_xml(self):
        """can_parse 对有效的 JUnit XML 返回 True。"""
        self.assertTrue(self.parser.can_parse(_fixture("junit_pass.xml")))
        self.assertTrue(self.parser.can_parse(_fixture("junit_fail.xml")))
        self.assertTrue(self.parser.can_parse(_fixture("junit_multi.xml")))

    def test_cannot_parse_nonexistent(self):
        """can_parse 对不存在文件返回 False。"""
        self.assertFalse(self.parser.can_parse("/nonexistent/path/report.xml"))
        # 非 .xml 文件
        self.assertFalse(self.parser.can_parse(__file__))  # .py 文件


if __name__ == "__main__":
    unittest.main()