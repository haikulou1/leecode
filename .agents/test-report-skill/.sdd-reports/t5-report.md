# T5 Report — 框架检测 + 执行器 + 覆盖率 + 脱敏模块

## 状态: ✅ 完成

## 变更文件

| 文件 | 操作 | 说明 |
|------|------|------|
| `src/detector.py` | 新增 | FrameworkDetector — 优先级 a→b→c 框架检测 |
| `src/executor.py` | 新增 | TestExecutor + ExecutorError — 执行/解析双模式 |
| `src/coverage.py` | 新增 | CoverageAggregator — auto/on/off 覆盖率聚合 |
| `src/sanitizer.py` | 新增 | Sanitizer — NFR3 敏感信息脱敏 |
| `tests/fixtures/coverage_sample.json` | 新增 | Istanbul 覆盖率 fixture |
| `tests/test_detector.py` | 新增 | 13 个测试用例 |
| `tests/test_executor.py` | 新增 | 9 个测试用例 |
| `tests/test_coverage.py` | 新增 | 5 个测试用例 |
| `tests/test_sanitizer.py` | 新增 | 10 个测试用例 |

## 测试结果

```
Ran 37 tests in 1.020s — OK ✓
```

全部 37 个测试通过，零失败。

## 实现要点

- **detector.py**: 按用户命令 > 配置文件(package.json/pyproject.toml/Cargo.toml) > 特征文件(jest.config.*/vitest.config.*/pytest.ini/conftest.py) 三级优先级检测，配置损坏时降级跳过
- **executor.py**: subprocess 前台执行 + 超时降级；shell=True 下命令不存在时检查 stderr 中的 "not found"；parse_only 独立解析模式；find_result_files 递归查找(排除 node_modules/.git, depth 3)
- **coverage.py**: 支持 Istanbul summary/final 和 Python coverage.py JSON 格式；auto 模式自动探测；mode=off 返回 None；低覆盖率文件阈值 80%
- **sanitizer.py**: 4 种敏感模式(API key/AWS key/通用密钥/环境变量)；sanitize_result 返回新对象不原地修改；所有 Python 3.12 stdlib 零外部依赖

## 风险/关注点

- 无已知风险。所有模块仅使用 Python 3.12 stdlib，不修改现有解析器文件。