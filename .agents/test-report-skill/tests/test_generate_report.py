"""
测试 generate_report 主入口编排模块。

覆盖场景：
- parse 模式从 JUnit XML 生成报告
- 返回 dict 含 summary 且 total 正确
- result_file 不存在抛 FileNotFoundError
"""

import os
import sys
import tempfile
import unittest

# 将 src 目录加入 sys.path，使 from src.generate_report import ... 可工作
_src_path = os.path.join(os.path.dirname(__file__), "..", "src")
if _src_path not in sys.path:
    sys.path.insert(0, os.path.abspath(_src_path))

from src.generate_report import generate_report  # noqa: E402


class TestGenerateReport(unittest.TestCase):
    """generate_report 主入口测试。"""

    def setUp(self):
        self.fixture = os.path.join(
            os.path.dirname(__file__), "fixtures", "junit_pass.xml"
        )
        self.assertTrue(os.path.isfile(self.fixture), f"fixture 缺失: {self.fixture}")
        # 使用临时目录作为输出目录，避免污染 reports/
        self._tmp = tempfile.mkdtemp(prefix="test_report_")
        self.output_path = self._tmp

    def test_generate_report_from_junit_xml(self):
        """parse 模式生成报告：报告文件存在，内容含「测试报告」和用例名。"""
        result = generate_report(
            mode="parse",
            result_file=self.fixture,
            output_path=self.output_path,
            project_name="demo-project",
        )

        report_path = result["report_path"]
        self.assertTrue(os.path.isfile(report_path), f"报告未落盘: {report_path}")

        with open(report_path, "r", encoding="utf-8") as f:
            content = f.read()
        self.assertIn("测试报告", content)
        self.assertIn("testAdd", content)

    def test_generate_report_returns_summary(self):
        """返回 dict 含 summary，且 summary.total == 3（3 个用例全通过）。"""
        result = generate_report(
            mode="parse",
            result_file=self.fixture,
            output_path=self.output_path,
        )

        self.assertIn("summary", result)
        self.assertIsInstance(result["summary"], dict)
        self.assertEqual(result["summary"]["total"], 3)
        self.assertEqual(result["summary"]["passed"], 3)
        self.assertEqual(result["summary"]["failed"], 0)
        self.assertIn("report_path", result)
        self.assertIn("failures", result)
        self.assertEqual(result["failures"], [])

    def test_generate_report_missing_file(self):
        """result_file 不存在时抛 FileNotFoundError。"""
        missing = os.path.join(self._tmp, "no_such_file.xml")
        self.assertFalse(os.path.exists(missing))
        with self.assertRaises(FileNotFoundError):
            generate_report(
                mode="parse",
                result_file=missing,
                output_path=self.output_path,
            )


if __name__ == "__main__":
    unittest.main()
