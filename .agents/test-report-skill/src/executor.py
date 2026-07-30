"""
TestExecutor — 测试执行器 (FR1.3).

支持执行模式（前台 subprocess + 超时降级）和解析模式（仅解析已有结果文件）。
"""

from __future__ import annotations

import os
import subprocess
from pathlib import Path
from typing import Optional

from .models import Failure, TestResult
from .parsers import get_registry
from .parsers.base import ParseError


class ExecutorError(Exception):
    """测试执行器错误。"""

    pass


class TestExecutor:
    """测试执行器 — 执行/解析双模式。"""

    # 常见结果文件模式
    _RESULT_FILE_NAMES = [
        "test-results.json",
        "junit.xml",
        "test-report.xml",
        "pytest-report.json",
        "coverage/coverage-final.json",
    ]

    def __init__(self, registry=None) -> None:
        self.registry = registry or get_registry()

    def execute(self, command: str, cwd: str, timeout: int = 300) -> TestResult:
        """在前台执行测试命令并解析结果。

        Args:
            command: 要执行的 shell 命令。
            cwd: 工作目录。
            timeout: 超时秒数。

        Returns:
            TestResult: 归一化测试结果。

        Raises:
            ExecutorError: 命令无法运行时（FileNotFoundError / TimeoutExpired）。
        """
        try:
            proc = subprocess.run(
                command,
                shell=True,
                cwd=cwd,
                capture_output=True,
                timeout=timeout,
                text=True,
            )
        except FileNotFoundError as e:
            raise ExecutorError(f"测试命令执行失败: 命令未找到 — {e}")
        except subprocess.TimeoutExpired as e:
            raise ExecutorError(f"测试命令执行失败: 超时 ({timeout}s) — {e}")

        # shell=True 时命令不存在不会抛出 FileNotFoundError，需检查 stderr
        if proc.returncode == 127 and "not found" in proc.stderr.lower():
            raise ExecutorError(f"测试命令执行失败: 命令未找到 — {proc.stderr.strip()}")

        # 尝试从结果文件解析
        result_files = self.find_result_files(cwd)
        for rf in result_files:
            try:
                result = self.registry.detect_and_parse(rf)
                result.aggregate()
                return result
            except ParseError:
                continue

        # 无结果文件时用 stdout/stderr 构建最小结果
        result = TestResult.empty()
        if proc.returncode != 0:
            result.failures.append(
                Failure(
                    name="command_execution",
                    file="",
                    error=f"命令退出码: {proc.returncode}\nstdout: {proc.stdout[:2000]}\nstderr: {proc.stderr[:2000]}",
                )
            )
            result.summary.failed = 1
            result.summary.total = 1
        else:
            result.summary.passed = 1
            result.summary.total = 1
        result.summary.compute_pass_rate()
        return result

    def parse_only(self, result_file: str) -> TestResult:
        """解析模式 — 仅解析已有结果文件 (US4/AC3)。

        Args:
            result_file: 结果文件路径。

        Returns:
            TestResult: 归一化测试结果。

        Raises:
            ExecutorError: 文件不存在时。
            ParseError: 解析失败时透传。
        """
        if not os.path.isfile(result_file):
            raise ExecutorError(f"结果文件不存在: {result_file}")

        result = self.registry.detect_and_parse(result_file)
        result.aggregate()
        return result

    def find_result_files(self, cwd: str) -> list[str]:
        """在 cwd 下递归查找常见结果文件。

        Args:
            cwd: 搜索根目录。

        Returns:
            匹配的文件路径列表（按发现顺序）。
        """
        found: list[str] = []
        base = Path(cwd).resolve()
        if not base.is_dir():
            return found

        for root, dirs, files in os.walk(base):
            # 排除 node_modules 和 .git
            dirs[:] = [d for d in dirs if d not in ("node_modules", ".git")]

            # 深度限制: 3
            rel = Path(root).relative_to(base)
            if len(rel.parts) > 3:
                dirs.clear()
                continue

            for fname in files:
                if fname in self._RESULT_FILE_NAMES or any(
                    fname == name for name in self._RESULT_FILE_NAMES
                ):
                    full = os.path.join(root, fname)
                    found.append(full)

            # 也检查 coverage/ 子目录下的文件
            for fname in files:
                if fname in self._RESULT_FILE_NAMES:
                    full = os.path.join(root, fname)
                    if full not in found:
                        found.append(full)

        return found