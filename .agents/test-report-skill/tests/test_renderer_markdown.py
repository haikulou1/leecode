"""
MarkdownRenderer 渲染器测试.

覆盖场景：
- 报告头含「测试报告」
- 全通过摘要含 pass_rate 值和 ✅
- 失败用例渲染含用例名和错误信息
- 有覆盖率时渲染含数值和低覆盖文件
- 无覆盖率时渲染含「未获取」
"""

import os
import sys
import unittest

_src_path = os.path.join(os.path.dirname(__file__), "..", "src")
if _src_path not in sys.path:
    sys.path.insert(0, os.path.abspath(_src_path))

from src.renderer_markdown import MarkdownRenderer
from src.models import (
    TestResult,
    TestSuite,
    TestCase,
    Coverage,
    Summary,
    Failure,
    CaseStatus,
)


def make_result(failed=False, coverage=None):
    """构造测试用 TestResult。"""
    cases = [TestCase(name="test_add", status=CaseStatus.PASS.value, duration_ms=100)]
    if failed:
        cases.append(
            TestCase(
                name="test_fail",
                status=CaseStatus.FAIL.value,
                duration_ms=50,
                error="AssertionError: expected 5 got 4",
                stack=["at line 15 in test_math.py"],
            )
        )
    suites = [TestSuite(file="test_math.py", cases=cases)]
    failures = []
    if failed:
        failures = [
            Failure(
                name="test_fail",
                file="test_math.py",
                error="AssertionError: expected 5 got 4",
                stack_excerpt=["at line 15 in test_math.py"],
            )
        ]
    summary = Summary(total=len(cases), passed=1, failed=1 if failed else 0, skipped=0)
    summary.compute_pass_rate()
    summary.duration_ms = sum(c.duration_ms for c in cases)
    return TestResult(summary=summary, suites=suites, failures=failures, coverage=coverage)


class TestMarkdownRenderer(unittest.TestCase):
    def setUp(self):
        self.renderer = MarkdownRenderer()

    def test_render_header(self):
        result = make_result()
        output = self.renderer.render(result, project_name="demo")
        self.assertIn("测试报告", output)
        self.assertIn("demo", output)
        # 空参数应显示「未指定」
        self.assertIn("未指定", output)

    def test_render_summary_all_pass(self):
        result = make_result(failed=False)
        output = self.renderer.render(result)
        # pass_rate = 100.0
        self.assertIn("100.0", output)
        self.assertIn("✅", output)

    def test_render_failures(self):
        result = make_result(failed=True)
        output = self.renderer.render(result)
        self.assertIn("test_fail", output)
        self.assertIn("AssertionError: expected 5 got 4", output)

    def test_render_coverage(self):
        coverage = Coverage(
            lines=80.0,
            branches=70.0,
            functions=90.0,
            statements=85.0,
            low_coverage_files=["src/foo.py"],
        )
        result = make_result(coverage=coverage)
        output = self.renderer.render(result)
        self.assertIn("80.0", output)
        self.assertIn("src/foo.py", output)

    def test_render_coverage_none(self):
        result = make_result(coverage=None)
        output = self.renderer.render(result)
        self.assertIn("未获取", output)


if __name__ == "__main__":
    unittest.main()
