"""
ParserRegistry — 解析器注册表（插件模式）。

遵循 clarify.md §4.1 方案 A（插件式解析器），支持 NFR5 扩展性：
新增框架 = 新增实现 + 注册，不修改既有解析器。
"""

from __future__ import annotations

from typing import TYPE_CHECKING

from .base import ParseError, ResultParser

if TYPE_CHECKING:
    from models import TestResult


class ParserRegistry:
    """解析器注册表 — 管理所有 ResultParser 插件。"""

    def __init__(self) -> None:
        self._parsers: dict[str, type[ResultParser]] = {}

    def register(self, name: str, parser_cls: type[ResultParser]) -> None:
        """按名称注册一个解析器类。"""
        self._parsers[name] = parser_cls

    def get(self, name: str) -> ResultParser:
        """按名称实例化并返回解析器。"""
        try:
            cls = self._parsers[name]
        except KeyError:
            raise KeyError(f"No parser registered under name: {name!r}")
        return cls()

    def detect_and_parse(self, file_path: str) -> TestResult:
        """自动检测格式并解析文件。

        遍历注册的解析器，调用 can_parse 匹配，使用第一个匹配的 parse。
        若无匹配则抛出 ParseError。
        """
        for name, cls in self._parsers.items():
            parser = cls()
            if parser.can_parse(file_path):
                return parser.parse(file_path)
        raise ParseError(f"No parser matched file: {file_path}")

    def names(self) -> list[str]:
        """返回已注册的解析器名称列表。"""
        return list(self._parsers.keys())


# 模块级单例
_default_registry = ParserRegistry()


def get_registry() -> ParserRegistry:
    """返回全局默认的 ParserRegistry 单例。"""
    return _default_registry