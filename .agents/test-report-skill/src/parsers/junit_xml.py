"""
JUnitXmlParser — JUnit XML 格式解析器。

解析标准 Maven Surefire / Gradle 生成的 JUnit XML 报告。
支持单 <testsuite> 和 <testsuites>（多 suite）根元素。
"""

import xml.etree.ElementTree as ET

from ..models import CaseStatus, TestCase, TestResult, TestSuite, Failure
from .base import ParseError, ResultParser
from . import get_registry  # get_registry 定义在 parsers/__init__.py


class JUnitXmlParser(ResultParser):
    """JUnit XML 格式解析器。"""

    name = "junit_xml"

    def can_parse(self, file_path: str) -> bool:
        """判断文件是否为 JUnit XML 格式：.xml 后缀且根标签为 testsuite(s)。"""
        if not file_path.endswith(".xml"):
            return False
        try:
            tree = ET.parse(file_path)
            root = tree.getroot()
            return root.tag in ("testsuite", "testsuites")
        except Exception:
            return False

    def parse(self, file_path: str) -> TestResult:
        """解析 JUnit XML 文件，返回归一化 TestResult。"""
        try:
            tree = ET.parse(file_path)
        except ET.ParseError as e:
            raise ParseError(f"Malformed JUnit XML file: {file_path}\n  {e}")
        except FileNotFoundError:
            raise ParseError(f"File not found: {file_path}")

        root = tree.getroot()
        if root.tag not in ("testsuite", "testsuites"):
            raise ParseError(
                f"Unexpected root element <{root.tag}> in {file_path}; "
                f"expected <testsuite> or <testsuites>"
            )

        suites: list[TestSuite] = []
        if root.tag == "testsuites":
            suite_elements = list(root)
        else:
            suite_elements = [root]

        for suite_elem in suite_elements:
            if suite_elem.tag != "testsuite":
                continue
            suite_name = suite_elem.get("name", "")
            suite = TestSuite(file=suite_name)
            for case_elem in suite_elem:
                if case_elem.tag != "testcase":
                    continue
                case = self._parse_testcase(case_elem, suite_name)
                suite.cases.append(case)
            suites.append(suite)

        result = TestResult(suites=suites)
        result.aggregate()
        return result

    @staticmethod
    def _parse_testcase(case_elem: ET.Element, suite_name: str) -> TestCase:
        """解析单个 testcase 元素。"""
        name = case_elem.get("name", "")
        classname = case_elem.get("classname", "")
        if not name and classname:
            name = classname

        # 解析 duration
        time_str = case_elem.get("time", "0")
        try:
            duration_ms = float(time_str) * 1000.0
        except (ValueError, TypeError):
            duration_ms = 0.0

        # 判定状态
        failure_elem = case_elem.find("failure")
        error_elem = case_elem.find("error")
        skipped_elem = case_elem.find("skipped")
        stack: list[str] = []
        error_msg = ""

        if failure_elem is not None:
            status = CaseStatus.FAIL.value
            error_msg = failure_elem.get("message", "")
            stack = _extract_stack(failure_elem.text)
        elif error_elem is not None:
            status = CaseStatus.ERROR.value
            error_msg = error_elem.get("message", "")
            stack = _extract_stack(error_elem.text)
        elif skipped_elem is not None:
            status = CaseStatus.SKIP.value
        else:
            status = CaseStatus.PASS.value

        return TestCase(
            name=name,
            status=status,
            duration_ms=duration_ms,
            error=error_msg,
            stack=stack,
        )


def _extract_stack(text: str | None) -> list[str]:
    """从 failure/error 元素的文本内容提取堆栈行，截断至 10 行。"""
    if not text:
        return []
    lines = text.strip().split("\n")
    lines = [line.strip() for line in lines]
    return lines[:10]


# 注册到全局解析器注册表
get_registry().register("junit_xml", JUnitXmlParser)