"""
FrameworkDetector — 框架检测器 (FR1.1).

优先级: a. 用户显式命令 > b. 配置文件 > c. 特征文件推断.
"""

from __future__ import annotations

import json
import os
from pathlib import Path
from typing import Optional


class FrameworkDetector:
    """检测项目使用的测试框架，返回框架名、命令和配置文件路径。"""

    def __init__(self, project_dir: str) -> None:
        self.project_dir = Path(project_dir)

    def detect(self, user_command: Optional[str] = None) -> dict:
        """按优先级 a→b→c 检测框架。

        Returns:
            dict with keys: framework, command, config_file
        """
        # a. 用户显式命令
        if user_command:
            return {"framework": "user", "command": user_command, "config_file": None}

        # b. 配置文件检测
        result = self._detect_from_config_files()
        if result:
            return result

        # c. 特征文件推断
        result = self._detect_from_signature_files()
        if result:
            return result

        return {"framework": "unknown", "command": "", "config_file": None}

    # ── b. 配置文件 ──────────────────────────────────────────────

    def _detect_from_config_files(self) -> Optional[dict]:
        """按优先级检测配置文件: package.json → pyproject.toml → Cargo.toml."""
        # package.json
        pj = self.project_dir / "package.json"
        data = self._load_json(pj)
        if data is not None and data.get("scripts", {}).get("test"):
            framework = self._detect_from_pkg_json(data)
            return {"framework": framework, "command": "npm test", "config_file": "package.json"}

        # pyproject.toml
        pt = self.project_dir / "pyproject.toml"
        toml_data = self._read_toml(pt)
        if toml_data is not None:
            if "tool" in toml_data and "pytest" in toml_data["tool"]:
                return {"framework": "pytest", "command": "python -m pytest", "config_file": "pyproject.toml"}

        # Cargo.toml
        ct = self.project_dir / "Cargo.toml"
        cargo_data = self._read_toml(ct)
        if cargo_data is not None:
            return {"framework": "cargo", "command": "cargo test", "config_file": "Cargo.toml"}

        return None

    def _detect_from_pkg_json(self, data: dict) -> str:
        """从 package.json 的 devDependencies 推断框架。"""
        dev_deps = data.get("devDependencies", {})
        if "jest" in dev_deps:
            return "jest"
        if "vitest" in dev_deps:
            return "vitest"
        return "node"

    # ── c. 特征文件推断 ──────────────────────────────────────────

    def _detect_from_signature_files(self) -> Optional[dict]:
        """按优先级检测特征文件。"""
        patterns = [
            ("jest.config.*", "jest", "npx jest --json"),
            ("vitest.config.*", "vitest", "npx vitest run --json"),
            ("pytest.ini", "pytest", "python -m pytest"),
            ("conftest.py", "pytest", "python -m pytest"),
        ]
        for pattern, framework, command in patterns:
            matched = self._glob_config(pattern)
            if matched:
                return {"framework": framework, "command": command, "config_file": matched}
        return None

    # ── helpers ──────────────────────────────────────────────────

    def _load_json(self, path: Path) -> Optional[dict]:
        """安全加载 JSON 文件，解析失败返回 None。"""
        try:
            with open(path, "r", encoding="utf-8") as f:
                return json.load(f)
        except (FileNotFoundError, json.JSONDecodeError, OSError):
            return None

    def _read_toml(self, path: Path) -> Optional[dict]:
        """安全读取 TOML 文件，解析失败返回 None。"""
        try:
            import tomllib
        except ImportError:
            return None
        try:
            with open(path, "rb") as f:
                return tomllib.load(f)
        except (FileNotFoundError, OSError, Exception):
            return None

    def _glob_config(self, pattern: str) -> Optional[str]:
        """在当前目录查找匹配 pattern 的第一个文件，返回相对路径。"""
        matches = sorted(self.project_dir.glob(pattern))
        for m in matches:
            if m.name not in ("node_modules", ".git") and not any(
                part in ("node_modules", ".git") for part in m.parts
            ):
                return str(m.relative_to(self.project_dir))
        return None