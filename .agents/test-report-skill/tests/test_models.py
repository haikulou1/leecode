"""Tests for .agents/test-report-skill/src/models.py."""

import unittest
import json
import sys
import os

# Ensure the src package is importable
sys.path.insert(
    0,
    os.path.join(
        os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
        "src",
    ),
)

from models import (
    CaseStatus,
    TestCase,
    TestSuite,
    Summary,
    Coverage,
    Failure,
    TestResult,
)


class TestSummaryComputePassRate(unittest.TestCase):
    """Summary.compute_pass_rate 行为测试。"""

    def test_with_failures_status_fail(self):
        """有失败用例时 status='fail', pass_rate 正确计算。"""
        s = Summary(total=10, passed=7, failed=3, skipped=0)
        s.compute_pass_rate()
        self.assertEqual(s.status, "fail")
        self.assertAlmostEqual(s.pass_rate, 70.0)

    def test_all_pass(self):
        """全部通过时 status='pass'。"""
        s = Summary(total=5, passed=5, failed=0, skipped=0)
        s.compute_pass_rate()
        self.assertEqual(s.status, "pass")
        self.assertAlmostEqual(s.pass_rate, 100.0)

    def test_total_zero(self):
        """total=0 时 pass_rate=0.0, status='pass'。"""
        s = Summary(total=0, passed=0, failed=0, skipped=0)
        s.compute_pass_rate()
        self.assertEqual(s.status, "pass")
        self.assertAlmostEqual(s.pass_rate, 0.0)

    def test_mixed_skipped(self):
        """跳过不影响 pass_rate 计算，但 failed>0 仍为 fail。"""
        s = Summary(total=10, passed=6, failed=1, skipped=3)
        s.compute_pass_rate()
        self.assertEqual(s.status, "fail")
        self.assertAlmostEqual(s.pass_rate, 60.0)

    def test_all_skipped_no_failures(self):
        """全部跳过时 total>0, passed=0, failed=0 → status='pass', pass_rate=0.0。"""
        s = Summary(total=3, passed=0, failed=0, skipped=3)
        s.compute_pass_rate()
        self.assertEqual(s.status, "pass")
        self.assertAlmostEqual(s.pass_rate, 0.0)


class TestTestResultAggregate(unittest.TestCase):
    """TestResult.aggregate() 行为测试。"""

    def test_counts_cases_and_collects_failures(self):
        """aggregate 从 suites 正确统计并收集失败。"""
        tr = TestResult(
            suites=[
                TestSuite(
                    file="tests/test_a.py",
                    cases=[
                        TestCase(name="test_ok", status=CaseStatus.PASS.value, duration_ms=10.0),
                        TestCase(
                            name="test_fail",
                            status=CaseStatus.FAIL.value,
                            duration_ms=5.0,
                            error="AssertionError: expected 2",
                            stack=["line 42", "line 43"],
                        ),
                        TestCase(name="test_skip", status=CaseStatus.SKIP.value, duration_ms=0.0),
                    ],
                ),
                TestSuite(
                    file="tests/test_b.py",
                    cases=[
                        TestCase(
                            name="test_error",
                            status=CaseStatus.ERROR.value,
                            duration_ms=3.0,
                            error="ValueError: boom",
                            stack=["line 99"],
                        ),
                        TestCase(name="test_ok2", status=CaseStatus.PASS.value, duration_ms=7.0),
                    ],
                ),
            ]
        )
        tr.aggregate()

        self.assertEqual(tr.summary.total, 5)
        self.assertEqual(tr.summary.passed, 2)
        self.assertEqual(tr.summary.failed, 2)  # 1 FAIL + 1 ERROR
        self.assertEqual(tr.summary.skipped, 1)
        self.assertAlmostEqual(tr.summary.duration_ms, 25.0)
        self.assertEqual(tr.summary.status, "fail")
        self.assertAlmostEqual(tr.summary.pass_rate, 40.0)

        # 验证 failures 收集
        self.assertEqual(len(tr.failures), 2)
        self.assertEqual(tr.failures[0].name, "test_fail")
        self.assertEqual(tr.failures[0].file, "tests/test_a.py")
        self.assertEqual(tr.failures[0].error, "AssertionError: expected 2")
        self.assertEqual(tr.failures[0].stack_excerpt, ["line 42", "line 43"])

        self.assertEqual(tr.failures[1].name, "test_error")
        self.assertEqual(tr.failures[1].file, "tests/test_b.py")
        self.assertEqual(tr.failures[1].error, "ValueError: boom")
        self.assertEqual(tr.failures[1].stack_excerpt, ["line 99"])

    def test_empty_suites(self):
        """无 suites 时 aggregate 产生空统计。"""
        tr = TestResult.empty()
        tr.aggregate()
        self.assertEqual(tr.summary.total, 0)
        self.assertEqual(tr.summary.passed, 0)
        self.assertEqual(tr.summary.failed, 0)
        self.assertEqual(tr.summary.status, "pass")
        self.assertEqual(len(tr.failures), 0)


class TestTestResultRoundTrip(unittest.TestCase):
    """TestResult.to_dict / from_dict 往返测试。"""

    def test_round_trip(self):
        """to_dict 后 from_dict 得到相同内容。"""
        original = TestResult(
            suites=[
                TestSuite(
                    file="tests/test_x.py",
                    cases=[
                        TestCase(name="test_pass", status=CaseStatus.PASS.value, duration_ms=1.5),
                        TestCase(
                            name="test_fail",
                            status=CaseStatus.FAIL.value,
                            duration_ms=2.0,
                            error="fail!",
                            stack=["trace1"],
                        ),
                    ],
                )
            ],
            coverage=Coverage(lines=80.0, branches=70.0, functions=90.0, statements=85.0),
        )
        original.aggregate()

        d = original.to_dict()
        restored = TestResult.from_dict(d)

        # 检查 summary
        self.assertEqual(restored.summary.total, original.summary.total)
        self.assertEqual(restored.summary.passed, original.summary.passed)
        self.assertEqual(restored.summary.failed, original.summary.failed)
        self.assertEqual(restored.summary.status, original.summary.status)
        self.assertAlmostEqual(restored.summary.pass_rate, original.summary.pass_rate)

        # 检查 suites
        self.assertEqual(len(restored.suites), 1)
        self.assertEqual(restored.suites[0].file, "tests/test_x.py")
        self.assertEqual(len(restored.suites[0].cases), 2)
        self.assertEqual(restored.suites[0].cases[0].name, "test_pass")
        self.assertEqual(restored.suites[0].cases[1].error, "fail!")

        # 检查 failures
        self.assertEqual(len(restored.failures), 1)
        self.assertEqual(restored.failures[0].name, "test_fail")

        # 检查 coverage
        self.assertIsNotNone(restored.coverage)
        self.assertAlmostEqual(restored.coverage.lines, 80.0)  # type: ignore[union-attr]
        self.assertAlmostEqual(restored.coverage.branches, 70.0)  # type: ignore[union-attr]

    def test_round_trip_no_coverage(self):
        """coverage=None 时也能正确往返。"""
        original = TestResult.empty()
        d = original.to_dict()
        restored = TestResult.from_dict(d)
        self.assertIsNone(restored.coverage)
        self.assertEqual(restored.summary.total, 0)

    def test_to_dict_json_serializable(self):
        """to_dict 的结果应可被 json.dump 序列化。"""
        tr = TestResult(
            suites=[
                TestSuite(
                    file="test.py",
                    cases=[TestCase(name="t1", status=CaseStatus.PASS.value)],
                )
            ],
            coverage=Coverage.unknown(),
        )
        tr.aggregate()
        d = tr.to_dict()
        # 不应抛出异常
        json_str = json.dumps(d, ensure_ascii=False)
        self.assertIn("test.py", json_str)
        self.assertIn("pass", json_str)


class TestCoverageUnknown(unittest.TestCase):
    """Coverage.unknown() 行为测试。"""

    def test_unknown_all_zero(self):
        """unknown() 返回全零值。"""
        c = Coverage.unknown()
        self.assertAlmostEqual(c.lines, 0.0)
        self.assertAlmostEqual(c.branches, 0.0)
        self.assertAlmostEqual(c.functions, 0.0)
        self.assertAlmostEqual(c.statements, 0.0)
        self.assertEqual(c.low_coverage_files, [])


class TestCaseStatusValues(unittest.TestCase):
    """CaseStatus 枚举值正确性。"""

    def test_values(self):
        self.assertEqual(CaseStatus.PASS.value, "pass")
        self.assertEqual(CaseStatus.FAIL.value, "fail")
        self.assertEqual(CaseStatus.SKIP.value, "skip")
        self.assertEqual(CaseStatus.ERROR.value, "error")


class TestTestResultEmpty(unittest.TestCase):
    """TestResult.empty() 行为测试。"""

    def test_empty(self):
        tr = TestResult.empty()
        self.assertEqual(tr.summary.total, 0)
        self.assertEqual(tr.suites, [])
        self.assertEqual(tr.failures, [])
        self.assertIsNone(tr.coverage)


if __name__ == "__main__":
    unittest.main()