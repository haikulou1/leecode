"""TestExecutor 测试。"""

import os
import sys
import tempfile
import unittest

_src_path = os.path.join(os.path.dirname(__file__), "..", "src")
if _src_path not in sys.path:
    sys.path.insert(0, os.path.abspath(_src_path))

from src.executor import ExecutorError, TestExecutor

# 触发解析器注册 (module-level registration)
import src.parsers.junit_xml  # noqa: F401 — 注册 junit_xml 解析器


class TestExecutorSuite(unittest.TestCase):
    """TestExecutor 单元测试。"""

    def setUp(self):
        self.executor = TestExecutor()

    def test_parse_only_xml(self):
        """parse_only 解析 junit_fail.xml fixture → TestResult 正确 (AC3)。"""
        fixture = os.path.join(
            os.path.dirname(__file__), "fixtures", "junit_fail.xml"
        )
        result = self.executor.parse_only(fixture)
        self.assertIsNotNone(result)
        self.assertGreaterEqual(result.summary.total, 1)
        # 有失败用例
        self.assertGreaterEqual(result.summary.failed, 1)

    def test_parse_only_missing_file(self):
        """不存在文件 → ExecutorError。"""
        with self.assertRaises(ExecutorError) as ctx:
            self.executor.parse_only("/nonexistent/file.xml")
        self.assertIn("不存在", str(ctx.exception))

    def test_execute_simple_command(self):
        """execute("echo hello", tempdir) → 不抛异常。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            result = self.executor.execute("echo hello", tmpdir, timeout=10)
            self.assertIsNotNone(result)
            # 没有测试文件，返回 minimal result
            self.assertIsNotNone(result.summary)

    def test_execute_timeout(self):
        """execute("sleep 5", tempdir, timeout=1) → ExecutorError with timeout reason。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            with self.assertRaises(ExecutorError) as ctx:
                self.executor.execute("sleep 5", tmpdir, timeout=1)
            self.assertIn("超时", str(ctx.exception))

    def test_execute_command_not_found(self):
        """execute(不存在的命令) → ExecutorError。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            with self.assertRaises(ExecutorError) as ctx:
                self.executor.execute("nonexistent_command_xyz", tmpdir, timeout=5)
            self.assertIn("命令未找到", str(ctx.exception))

    def test_find_result_files(self):
        """临时目录放 test-results.json → 能找到。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            rf = os.path.join(tmpdir, "test-results.json")
            with open(rf, "w") as f:
                f.write("{}")
            found = self.executor.find_result_files(tmpdir)
            self.assertIn(rf, found)

    def test_find_result_files_junit(self):
        """临时目录放 junit.xml → 能找到。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            rf = os.path.join(tmpdir, "junit.xml")
            with open(rf, "w") as f:
                f.write("<xml/>")
            found = self.executor.find_result_files(tmpdir)
            self.assertIn(rf, found)

    def test_find_result_files_empty(self):
        """空目录 → 找不到。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            found = self.executor.find_result_files(tmpdir)
            self.assertEqual(found, [])

    def test_find_result_files_excludes_node_modules(self):
        """node_modules 下的结果文件应被排除。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            nm = os.path.join(tmpdir, "node_modules")
            os.makedirs(nm)
            rf = os.path.join(nm, "junit.xml")
            with open(rf, "w") as f:
                f.write("<xml/>")
            found = self.executor.find_result_files(tmpdir)
            self.assertNotIn(rf, found)


if __name__ == "__main__":
    unittest.main()