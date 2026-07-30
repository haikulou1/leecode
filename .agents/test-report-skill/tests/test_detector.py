"""FrameworkDetector 测试。"""

import json
import os
import sys
import tempfile
import unittest
from pathlib import Path

_src_path = os.path.join(os.path.dirname(__file__), "..", "src")
if _src_path not in sys.path:
    sys.path.insert(0, os.path.abspath(_src_path))

from src.detector import FrameworkDetector


class TestFrameworkDetector(unittest.TestCase):
    """FrameworkDetector 单元测试。"""

    def test_user_command_priority(self):
        """user_command 提供时直接返回 framework="user"。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            detector = FrameworkDetector(tmpdir)
            result = detector.detect(user_command="pytest --coverage")
            self.assertEqual(result["framework"], "user")
            self.assertEqual(result["command"], "pytest --coverage")
            self.assertIsNone(result["config_file"])

    def test_package_json_jest(self):
        """package.json 含 jest devDependency → framework="jest"。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            pkg = {"scripts": {"test": "jest"}, "devDependencies": {"jest": "^29.0.0"}}
            with open(os.path.join(tmpdir, "package.json"), "w") as f:
                json.dump(pkg, f)
            detector = FrameworkDetector(tmpdir)
            result = detector.detect()
            self.assertEqual(result["framework"], "jest")
            self.assertEqual(result["command"], "npm test")
            self.assertEqual(result["config_file"], "package.json")

    def test_package_json_vitest(self):
        """package.json 含 vitest devDependency → framework="vitest"。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            pkg = {"scripts": {"test": "vitest"}, "devDependencies": {"vitest": "^1.0.0"}}
            with open(os.path.join(tmpdir, "package.json"), "w") as f:
                json.dump(pkg, f)
            detector = FrameworkDetector(tmpdir)
            result = detector.detect()
            self.assertEqual(result["framework"], "vitest")

    def test_package_json_node(self):
        """package.json 无 jest/vitest → framework="node"。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            pkg = {"scripts": {"test": "mocha test"}, "devDependencies": {"mocha": "^10.0.0"}}
            with open(os.path.join(tmpdir, "package.json"), "w") as f:
                json.dump(pkg, f)
            detector = FrameworkDetector(tmpdir)
            result = detector.detect()
            self.assertEqual(result["framework"], "node")

    def test_pyproject_pytest(self):
        """pyproject.toml 含 [tool.pytest] → framework="pytest"。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            toml = "[tool.pytest.ini_options]\nminversion = \"6.0\"\n"
            with open(os.path.join(tmpdir, "pyproject.toml"), "w") as f:
                f.write(toml)
            detector = FrameworkDetector(tmpdir)
            result = detector.detect()
            self.assertEqual(result["framework"], "pytest")
            self.assertEqual(result["command"], "python -m pytest")
            self.assertEqual(result["config_file"], "pyproject.toml")

    def test_cargo_toml(self):
        """Cargo.toml 存在 → framework="cargo"。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            toml = "[package]\nname = \"mycrate\"\n"
            with open(os.path.join(tmpdir, "Cargo.toml"), "w") as f:
                f.write(toml)
            detector = FrameworkDetector(tmpdir)
            result = detector.detect()
            self.assertEqual(result["framework"], "cargo")

    def test_jest_config_signature(self):
        """特征文件 jest.config.js → framework="jest"。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            Path(tmpdir, "jest.config.js").touch()
            detector = FrameworkDetector(tmpdir)
            result = detector.detect()
            self.assertEqual(result["framework"], "jest")
            self.assertEqual(result["command"], "npx jest --json")
            self.assertEqual(result["config_file"], "jest.config.js")

    def test_vitest_config_signature(self):
        """特征文件 vitest.config.ts → framework="vitest"。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            Path(tmpdir, "vitest.config.ts").touch()
            detector = FrameworkDetector(tmpdir)
            result = detector.detect()
            self.assertEqual(result["framework"], "vitest")

    def test_pytest_ini_signature(self):
        """特征文件 pytest.ini → framework="pytest"。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            Path(tmpdir, "pytest.ini").touch()
            detector = FrameworkDetector(tmpdir)
            result = detector.detect()
            self.assertEqual(result["framework"], "pytest")

    def test_conftest_py_signature(self):
        """特征文件 conftest.py → framework="pytest"。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            Path(tmpdir, "conftest.py").touch()
            detector = FrameworkDetector(tmpdir)
            result = detector.detect()
            self.assertEqual(result["framework"], "pytest")

    def test_no_config(self):
        """空目录 → framework="unknown"。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            detector = FrameworkDetector(tmpdir)
            result = detector.detect()
            self.assertEqual(result["framework"], "unknown")
            self.assertEqual(result["command"], "")
            self.assertIsNone(result["config_file"])

    def test_malformed_config_skipped(self):
        """损坏的 package.json 不应崩溃，降级继续。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            # 损坏的 JSON，但有特征文件
            with open(os.path.join(tmpdir, "package.json"), "w") as f:
                f.write("not valid json {{{")
            Path(tmpdir, "pytest.ini").touch()
            detector = FrameworkDetector(tmpdir)
            result = detector.detect()
            # 应跳过损坏的 package.json，使用特征文件
            self.assertEqual(result["framework"], "pytest")
            self.assertEqual(result["config_file"], "pytest.ini")

    def test_malformed_pyproject_skipped(self):
        """损坏的 pyproject.toml 不应崩溃。"""
        with tempfile.TemporaryDirectory() as tmpdir:
            with open(os.path.join(tmpdir, "pyproject.toml"), "w") as f:
                f.write("not valid toml [[[")
            Path(tmpdir, "conftest.py").touch()
            detector = FrameworkDetector(tmpdir)
            result = detector.detect()
            self.assertEqual(result["framework"], "pytest")


if __name__ == "__main__":
    unittest.main()