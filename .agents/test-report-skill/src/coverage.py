"""
CoverageAggregator — 覆盖率聚合器 (FR4.2-5).

支持 auto/on/off 三种模式，auto 模式下自动检测覆盖率文件。
"""

from __future__ import annotations

import json
import os
from pathlib import Path
from typing import Optional

from .models import Coverage


class CoverageAggregator:
    """覆盖率聚合器 — 探测并解析覆盖率文件。"""

    def __init__(self, project_dir: str, mode: str = "auto") -> None:
        self.project_dir = Path(project_dir)
        self.mode = mode

    def collect(self) -> Optional[Coverage]:
        """收集覆盖率数据。

        Returns:
            Coverage 对象，或 None（未获取/关闭）。
        """
        if self.mode == "off":
            return None

        # 探测覆盖率文件
        # 1. coverage/coverage-summary.json (Istanbul summary)
        summary_path = self.project_dir / "coverage" / "coverage-summary.json"
        data = self._load_json(summary_path)
        if data is not None:
            return self._parse_istanbul_summary(data)

        # 2. coverage/coverage-final.json (Istanbul full)
        final_path = self.project_dir / "coverage" / "coverage-final.json"
        data = self._load_json(final_path)
        if data is not None:
            return self._parse_istanbul(data)

        # 3. coverage.json (Python coverage.py JSON variant)
        pycov_path = self.project_dir / "coverage" / "coverage.json"
        data = self._load_json(pycov_path)
        if data is not None:
            return self._parse_python_coverage(data)

        return None

    # ── 解析方法 ─────────────────────────────────────────────────

    def _parse_istanbul(self, data: dict) -> Coverage:
        """解析 Istanbul 完整格式 (coverage-final.json)。"""
        total = data.get("total", {})
        lines = self._get_pct(total, "lines")
        branches = self._get_pct(total, "branches")
        functions = self._get_pct(total, "functions")
        statements = self._get_pct(total, "statements")

        low_coverage_files = self._find_low_coverage_files(data)

        return Coverage(
            lines=lines,
            branches=branches,
            functions=functions,
            statements=statements,
            low_coverage_files=low_coverage_files,
        )

    def _parse_istanbul_summary(self, data: dict) -> Coverage:
        """解析 Istanbul summary 格式 — 结构直接是 total。"""
        # summary 格式可能在顶层或 "total" 键下
        total = data if "lines" in data else data.get("total", data)
        lines = self._get_pct(total, "lines")
        branches = self._get_pct(total, "branches")
        functions = self._get_pct(total, "functions")
        statements = self._get_pct(total, "statements")

        # summary 格式通常没有逐文件数据
        low_coverage_files = self._find_low_coverage_files(data)

        return Coverage(
            lines=lines,
            branches=branches,
            functions=functions,
            statements=statements,
            low_coverage_files=low_coverage_files,
        )

    def _parse_python_coverage(self, data: dict) -> Coverage:
        """解析 Python coverage.py JSON 格式。"""
        totals = data.get("totals", {})
        lines = float(totals.get("percent_covered", 0.0))
        branches = float(totals.get("percent_covered", 0.0))
        functions = 0.0
        statements = 0.0
        low_coverage_files: list[str] = []

        # 逐文件检查
        files_data = data.get("files", {})
        threshold = 80.0
        for fpath, finfo in files_data.items():
            if isinstance(finfo, dict):
                summary = finfo.get("summary", {})
                pct = float(summary.get("percent_covered", 100.0))
                if pct < threshold:
                    low_coverage_files.append(fpath)

        return Coverage(
            lines=lines,
            branches=branches,
            functions=functions,
            statements=statements,
            low_coverage_files=low_coverage_files,
        )

    # ── helpers ──────────────────────────────────────────────────

    @staticmethod
    def _get_pct(total: dict, key: str) -> float:
        """从 Istanbul total 中提取 pct 字段。"""
        metric = total.get(key, {})
        if isinstance(metric, dict):
            return float(metric.get("pct", 0.0))
        return 0.0

    @staticmethod
    def _find_low_coverage_files(data: dict, threshold: float = 80.0) -> list[str]:
        """找出 statements.pct < threshold 的文件。"""
        low: list[str] = []
        for key, value in data.items():
            if key == "total":
                continue
            if isinstance(value, dict):
                stmts = value.get("statements", {})
                if isinstance(stmts, dict):
                    pct = float(stmts.get("pct", 100.0))
                    if pct < threshold:
                        low.append(key)
        return sorted(low)

    @staticmethod
    def _load_json(path: Path) -> Optional[dict]:
        """安全加载 JSON 文件。"""
        try:
            with open(path, "r", encoding="utf-8") as f:
                return json.load(f)
        except (FileNotFoundError, json.JSONDecodeError, OSError):
            return None