"""Sanitizer 测试。"""

import os
import sys
import unittest

_src_path = os.path.join(os.path.dirname(__file__), "..", "src")
if _src_path not in sys.path:
    sys.path.insert(0, os.path.abspath(_src_path))

from src.sanitizer import Sanitizer
from src.models import Failure, Summary, TestCase, TestResult, TestSuite


class TestSanitizer(unittest.TestCase):
    """Sanitizer 单元测试。"""

    def setUp(self):
        self.sanitizer = Sanitizer()

    def test_sanitize_api_key(self):
        """API key 应被脱敏。"""
        text = "Using key sk-abc123def456ghi789jkl012mno"
        result = self.sanitizer.sanitize_text(text)
        self.assertNotIn("sk-abc123def456ghi789jkl012mno", result)
        self.assertIn("***REDACTED***", result)

    def test_sanitize_password(self):
        """password=secret 应被脱敏。"""
        text = "Connection failed: password=secret123 on server"
        result = self.sanitizer.sanitize_text(text)
        self.assertNotIn("secret123", result)
        self.assertIn("***REDACTED***", result)

    def test_sanitize_token(self):
        """token=... 应被脱敏。"""
        text = "Authorization: token=ghp_abc123def456"
        result = self.sanitizer.sanitize_text(text)
        self.assertNotIn("ghp_abc123def456", result)
        self.assertIn("token=***REDACTED***", result)

    def test_sanitize_aws_key(self):
        """AWS access key 应被脱敏。"""
        text = "AKIA1234567890ABCDEF"
        result = self.sanitizer.sanitize_text(text)
        self.assertNotIn("AKIA1234567890ABCDEF", result)
        self.assertIn("***REDACTED***", result)

    def test_sanitize_env_var(self):
        """环境变量赋值应被脱敏。"""
        text = "DATABASE_URL=postgresql://user:pass@localhost:5432/db"
        result = self.sanitizer.sanitize_text(text)
        self.assertNotIn("postgresql", result)
        self.assertIn("***REDACTED***", result)

    def test_clean_text_passthrough(self):
        """无敏感信息的文本应保持不变。"""
        text = "This is a normal test output with no secrets."
        result = self.sanitizer.sanitize_text(text)
        self.assertEqual(result, text)

    def test_empty_text(self):
        """空文本不应崩溃。"""
        result = self.sanitizer.sanitize_text("")
        self.assertEqual(result, "")

    def test_sanitize_result(self):
        """TestResult 中的敏感信息应被脱敏。"""
        suite = TestSuite(
            file="test_auth.py",
            cases=[
                TestCase(
                    name="test_login",
                    status="fail",
                    error="Auth failed with password=secret123",
                    stack=["Traceback: password=secret123"],
                )
            ],
        )
        failure = Failure(
            name="test_login",
            file="test_auth.py",
            error="Env: DATABASE_URL=postgresql://user:pass@host/db",
            stack_excerpt=["Line 1: sk-abc123def456ghi789jkl012mno"],
        )
        result = TestResult(
            summary=Summary(total=1, failed=1),
            suites=[suite],
            failures=[failure],
        )

        cleaned = self.sanitizer.sanitize_result(result)

        # 用例中的敏感信息已脱敏
        self.assertNotIn("secret123", cleaned.suites[0].cases[0].error)
        self.assertIn("***REDACTED***", cleaned.suites[0].cases[0].error)
        self.assertNotIn("secret123", cleaned.suites[0].cases[0].stack[0])

        # 失败中的敏感信息已脱敏
        self.assertNotIn("postgresql", cleaned.failures[0].error)
        self.assertIn("***REDACTED***", cleaned.failures[0].error)
        self.assertNotIn("sk-abc123", cleaned.failures[0].stack_excerpt[0])

    def test_sanitize_result_no_mutation(self):
        """sanitize_result 不应修改原始对象。"""
        suite = TestSuite(
            file="test.py",
            cases=[TestCase(name="t", status="pass", error="password=secret123")],
        )
        result = TestResult(summary=Summary(), suites=[suite], failures=[])

        cleaned = self.sanitizer.sanitize_result(result)
        # 原始对象仍包含敏感信息
        self.assertIn("secret123", result.suites[0].cases[0].error)
        # 清理后的对象不包含
        self.assertNotIn("secret123", cleaned.suites[0].cases[0].error)

    def test_sanitize_api_key_colon(self):
        """API key 用 : 格式也应被脱敏。"""
        text = "api_key:sk-abc123def456ghi789jkl012mno"
        result = self.sanitizer.sanitize_text(text)
        self.assertNotIn("sk-abc123def456ghi789jkl012mno", result)
        self.assertIn("***REDACTED***", result)


if __name__ == "__main__":
    unittest.main()