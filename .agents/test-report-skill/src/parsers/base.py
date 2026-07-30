"""
ResultParser — 解析器抽象基类（插件契约）。

所有具体解析器（JUnit XML、pytest、Jest/Vitest JSON）都必须继承 ResultParser
并实现 can_parse() 和 parse() 方法。
"""

from abc import ABC, abstractmethod

from models import TestResult


class ParseError(Exception):
    """解析失败时抛出的异常。"""

    pass


class ResultParser(ABC):
    """解析器插件基类。

    子类需设置 name 类属性，并实现 can_parse 与 parse 方法。
    """

    name: str = ""

    @abstractmethod
    def can_parse(self, file_path: str) -> bool:
        """判断该解析器是否能处理指定文件（通过扩展名或内容嗅探）。"""
        ...

    @abstractmethod
    def parse(self, file_path: str) -> TestResult:
        """解析文件并返回归一化 TestResult。

        对格式错误/无法解析的输入，应抛出 ParseError 并附带清晰诊断信息（AC4）。
        对可降级的字段缺失，使用默认值兜底（NFR2）。
        """
        ...