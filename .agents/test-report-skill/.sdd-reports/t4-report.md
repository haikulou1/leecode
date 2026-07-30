# T4 Report: pytest 解析器

## Status
✅ 完成

## 变更文件
| 文件 | 操作 | 说明 |
|------|------|------|
| `tests/fixtures/pytest_junit.xml` | 新增 | pytest JUnit XML fixture：1 suite, 4 cases (2 pass, 1 fail with `<failure>`, 1 skip with `<skipped>`) |
| `tests/fixtures/pytest_json.json` | 新增 | pytest-json-report fixture：3 tests (passed, failed with call.longrepr, skipped) + summary |
| `tests/fixtures/pytest_malformed.xml` | 新增 | 格式错误的 XML fixture |
| `src/parsers/pytest.py` | 新增 | PytestParser(ResultParser)：name="pytest"，仅处理 .json（不抢 .xml），按 nodeid 分组为 suite，outcome→status 映射支持 xfailed/xpassed→SKIP，error/stack 从 call.longrepr 提取 |
| `tests/test_pytest_parser.py` | 新增 | 6 个测试用例覆盖全部 AC |

## 测试结果
```
Ran 32 tests in 0.003s
OK
```
全部 32 个测试通过（含 6 个新测试 + 26 个既有测试）。

## 注意事项
- pytest_junit.xml 通过 registry.detect_and_parse 路由到 junit_xml 解析器，验证了 .xml 不被 pytest 解析器拦截
- pytest_malformed.xml 通过 registry.detect_and_parse 抛出 ParseError（无解析器匹配）
- 测试文件需显式 import junit_xml 模块以触发注册，否则 registry 无法路由到该解析器