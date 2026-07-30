# T3 Report: Jest/Vitest JSON 解析器

**Status**: ✅ 完成

## 变更文件
| 文件 | 操作 | 说明 |
|------|------|------|
| `tests/fixtures/jest_pass.json` | 新增 | 1 suite, 2 passing cases, numTotal=2 |
| `tests/fixtures/jest_fail.json` | 新增 | 1 suite, 3 cases: 1 pass, 1 fail(with failureMessages), 1 pending |
| `tests/fixtures/jest_malformed.json` | 新增 | 无效 JSON `{broken` |
| `tests/fixtures/vitest_sample.json` | 新增 | Vitest-style: 1 suite, 2 cases pass, 无 per-case duration |
| `src/parsers/jest_vitest.py` | 新增 | JestVitestParser(ResultParser) — 含 can_parse/parse, 注册 "jest_vitest" |
| `tests/test_jest_parser.py` | 新增 | 7 个单元测试 |

## 测试结果
```
Ran 7 tests in 0.001s
OK
```

## 注意事项
- 使用 `from src.parsers.jest_vitest import ...` 模式（与 T1 test_junit_parser.py 一致）
- Vitest 无 per-case duration 时兜底为 0.0
- failureMessages 首条首行作为 error，剩余行截断 10 行作为 stack
- 状态映射: passed→PASS, failed→FAIL, pending/skipped/todo→SKIP