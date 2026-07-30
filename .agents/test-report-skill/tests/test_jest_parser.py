"""
Tests for JestVitestParser — Jest/Vitest JSON report parser.
"""

import os
import sys
import unittest

# 将 src 目录加入 sys.path，使 from src.parsers.jest_vitest import ... 可工作
_src_path = os.path.join(os.path.dirname(__file__), "..", "src")
if _src_path not in sys.path:
    sys.path.insert(0, os.path.abspath(_src_path))

from src.parsers.jest_vitest import JestVitestParser
from src.parsers.base import ParseError
from src.models import CaseStatus, Failure, TestResult


FIXTURES_DIR = os.path.join(os.path.dirname(__file__), "fixtures")


class TestJestVitestParser(unittest.TestCase):
    """JestVitestParser 单元测试。"""

    def setUp(self):
        self.parser = JestVitestParser()

    def _fixture_path(self, name: str) -> str:
        return os.path.join(FIXTURES_DIR, name)

    def test_parse_pass(self):
        """jest_pass.json → total==2, passed==2, failed==0, status=='pass'"""
        result = self.parser.parse(self._fixture_path("jest_pass.json"))
        self.assertIsInstance(result, TestResult)
        self.assertEqual(result.summary.total, 2)
        self.assertEqual(result.summary.passed, 2)
        self.assertEqual(result.summary.failed, 0)
        self.assertEqual(result.summary.status, "pass")

    def test_parse_fail(self):
        """jest_fail.json → total==3, passed==1, failed==1, skipped==1,
        status=='fail', failures has 1 entry with error+stack"""
        result = self.parser.parse(self._fixture_path("jest_fail.json"))
        self.assertEqual(result.summary.total, 3)
        self.assertEqual(result.summary.passed, 1)
        self.assertEqual(result.summary.failed, 1)
        self.assertEqual(result.summary.skipped, 1)
        self.assertEqual(result.summary.status, "fail")
        self.assertEqual(len(result.failures), 1)
        failure = result.failures[0]
        self.assertIsInstance(failure, Failure)
        self.assertEqual(failure.name, "failing test")
        self.assertEqual(failure.file, "/path/to/failing.test.ts")
        self.assertIn("Error: expected 3 got 2", failure.error)
        self.assertGreater(len(failure.stack_excerpt), 0)
        # stack 截断为 10 行
        self.assertLessEqual(len(failure.stack_excerpt), 10)

    def test_parse_vitest(self):
        """vitest_sample.json → total==2, passed==2"""
        result = self.parser.parse(self._fixture_path("vitest_sample.json"))
        self.assertEqual(result.summary.total, 2)
        self.assertEqual(result.summary.passed, 2)
        self.assertEqual(result.summary.failed, 0)

    def test_malformed_raises_parse_error(self):
        """jest_malformed.json → ParseError (AC4)"""
        with self.assertRaises(ParseError):
            self.parser.parse(self._fixture_path("jest_malformed.json"))

    def test_can_parse_json(self):
        """can_parse 对 jest_pass.json 返回 True"""
        self.assertTrue(self.parser.can_parse(self._fixture_path("jest_pass.json")))

    def test_cannot_parse_nonexistent(self):
        """can_parse 对不存在的文件返回 False"""
        self.assertFalse(self.parser.can_parse("/nonexistent/path/foo.json"))

    def test_cannot_parse_non_json_suffix(self):
        """can_parse 对非 .json 后缀返回 False"""
        self.assertFalse(self.parser.can_parse(__file__))  # .py 文件


if __name__ == "__main__":
    unittest.main()