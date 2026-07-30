"""CoverageAggregator 测试。"""

import json
import os
import sys
import tempfile
import unittest

_src_path = os.path.join(os.path.dirname(__file__), "..", "src")
if _src_path not in sys.path:
    sys.path.insert(0, os.path.abspath(_src_path))

from src.coverage import CoverageAggregator


class TestCoverageAggregator(unittest.TestCase):
    """CoverageAggregator 单元测试。"""

    def test_collect_istanbul(self):
        """临时目录 coverage/coverage-summary.json → Coverage 有值，low_coverage_files 非空。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            fixture_path = os.path.join(
                os.path.dirname(__file__), "fixtures", "coverage_sample.json"
            )
            with open(fixture_path, "r") as f:
                fixture_data = json.load(f)

            cov_dir = os.path.join(tmpdir, "coverage")
            os.makedirs(cov_dir)
            summary_path = os.path.join(cov_dir, "coverage-summary.json")
            with open(summary_path, "w") as f:
                json.dump(fixture_data, f)

            aggregator = CoverageAggregator(tmpdir, mode="auto")
            coverage = aggregator.collect()
            self.assertIsNotNone(coverage)
            self.assertAlmostEqual(coverage.lines, 92.0)
            self.assertAlmostEqual(coverage.branches, 80.0)
            self.assertAlmostEqual(coverage.functions, 90.0)
            self.assertAlmostEqual(coverage.statements, 95.0)
            self.assertGreater(len(coverage.low_coverage_files), 0)
            self.assertIn("src/risky_module.py", coverage.low_coverage_files)
            self.assertIn("src/another_low.py", coverage.low_coverage_files)

    def test_off_mode(self):
        """mode="off" → None。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            aggregator = CoverageAggregator(tmpdir, mode="off")
            self.assertIsNone(aggregator.collect())

    def test_not_found(self):
        """空目录 → None (AC5)。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            aggregator = CoverageAggregator(tmpdir, mode="auto")
            self.assertIsNone(aggregator.collect())

    def test_collect_coverage_final(self):
        """coverage/coverage-final.json 也能被解析。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            fixture_path = os.path.join(
                os.path.dirname(__file__), "fixtures", "coverage_sample.json"
            )
            with open(fixture_path, "r") as f:
                fixture_data = json.load(f)

            cov_dir = os.path.join(tmpdir, "coverage")
            os.makedirs(cov_dir)
            final_path = os.path.join(cov_dir, "coverage-final.json")
            with open(final_path, "w") as f:
                json.dump(fixture_data, f)

            aggregator = CoverageAggregator(tmpdir, mode="auto")
            coverage = aggregator.collect()
            self.assertIsNotNone(coverage)
            self.assertAlmostEqual(coverage.lines, 92.0)

    def test_good_file_not_in_low_coverage(self):
        """覆盖率 >= 80% 的文件不应出现在 low_coverage_files 中。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            data = {
                "total": {
                    "lines": {"pct": 90},
                    "branches": {"pct": 85},
                    "functions": {"pct": 90},
                    "statements": {"pct": 90},
                },
                "src/good_file.py": {
                    "statements": {"pct": 95.0},
                },
            }
            cov_dir = os.path.join(tmpdir, "coverage")
            os.makedirs(cov_dir)
            with open(os.path.join(cov_dir, "coverage-summary.json"), "w") as f:
                json.dump(data, f)

            aggregator = CoverageAggregator(tmpdir, mode="auto")
            coverage = aggregator.collect()
            self.assertIsNotNone(coverage)
            self.assertNotIn("src/good_file.py", coverage.low_coverage_files)


if __name__ == "__main__":
    unittest.main()