# InsightAI - AI驱动的数据分析平台

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3-orange.svg)](https://vuejs.org/)
[![AI](https://img.shields.io/badge/AI-BI--Powered-yellow.svg)](https://deepseek.com/)

> 📊 **AI-First智能分析平台** - 自然语言查询、智能可视化、预测分析，让每个人都能用数据说话

## 📋 项目简介

InsightAI 是一套**深度AI融合**的数据分析平台，对标Tableau/Power BI但用自然语言就能分析。

### 核心AI能力

| AI功能 | 描述 | 价值 |
|--------|------|------|
| 💬 **自然语言查询** | 说句话就能查数据，无需写SQL | 数据分析人人可做 |
| 📊 **智能可视化** | AI自动推荐最佳图表类型 | 一键生成专业报表 |
| 🔮 **预测分析** | AI预测未来趋势，提前洞察 | 数据驱动决策 |
| 📝 **自动报告** | AI自动生成分析报告 | 报告撰写效率提升90% |
| 🎯 **归因分析** | AI自动分析数据变化原因 | 快速定位问题根因 |

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Vue 3 前端                            │
│   (自然语言查询 / 可视化看板 / 报表中心 / AI洞察)              │
├─────────────────────────────────────────────────────────────┤
│                   Spring Cloud Alibaba 后端                  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐  │
│  │ Gateway     │ │ DataSource  │ │ Query               │  │
│  │ Service     │ │ Service     │ │ Service             │  │
│  └─────────────┘ └─────────────┘ └─────────────────────┘  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐  │
│  │ Visualization│ │ Report     │ │ AI Service          │  │
│  │ Service     │ │ Service     │ │ (Java)              │  │
│  └─────────────┘ └─────────────┘ └─────────────────────┘  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐  │
│  │ Forecasting │ │ Anomaly     │ │ NL Query            │  │
│  │ Service     │ │ Detection   │ │ Service             │  │
│  └─────────────┘ └─────────────┘ └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                    Java AI 服务层                           │
│  (NL2SQL / VisualizationAI / Forecasting / AttributionAI)   │
│  (AnomalyDetection / NaturalLanguageQuery)                  │
├─────────────────────────────────────────────────────────────┤
│                    DeepSeek API                             │
└─────────────────────────────────────────────────────────────┘
```

## 📂 目录结构

```
InsightAI/
├── ai-service/                    # Java AI 服务层 ⭐
│   ├── src/main/java/
│   │   └── com/insightai/ai/
│   │       ├── controller/         # REST API
│   │       ├── service/            # AI 业务逻辑
│   │       │   ├── NaturalLanguageToSqlService.java  # NL转SQL
│   │       │   ├── VisualizationRecommendationService.java # 可视化推荐
│   │       │   ├── TimeSeriesForecastingService.java  # 时序预测
│   │       │   ├── AutoReportGenerationService.java   # 自动报告
│   │       │   ├── AttributionAnalysisService.java  # 归因分析
│   │       │   └── DeepSeekClient.java               # LLM调用
│   │       └── dto/                # 数据传输对象
│   └── pom.xml
│
├── data-source-service/             # 数据源管理
├── query-service/                   # 查询执行服务
├── visualization-service/           # 可视化服务
├── report-service/                  # 报表服务
├── forecasting-service/             # 预测分析服务 ⭐
│   └── src/main/java/com/insightai/forecasting/
│       ├── controller/             # REST API
│       ├── service/                # 预测业务逻辑
│       └── dto/                    # 数据传输对象
├── anomaly-detection-service/       # 异常检测服务 ⭐
│   └── src/main/java/com/insightai/anomaly/
│       ├── controller/             # REST API
│       ├── service/                # 检测业务逻辑
│       └── dto/                    # 数据传输对象
├── nl-query-service/               # 自然语言查询服务 ⭐
│   └── src/main/java/com/insightai/nlquery/
│       ├── controller/             # REST API
│       ├── service/                # NLP业务逻辑
│       └── dto/                    # 数据传输对象
├── gateway/                        # API网关
├── frontend/                       # Vue 3前端
│   └── src/
│       ├── views/                 # 页面
│       │   ├── QueryView.vue      # 自然语言查询
│       │   ├── DashboardView.vue  # 数据看板
│       │   ├── ReportView.vue     # 报表中心
│       │   └── InsightView.vue    # AI洞察
│       └── components/            # 组件
└── pom.xml
```

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+ (或连接其他数据源)
- Redis
- Nacos

### 1. 启动后端

```bash
git clone https://github.com/nplszfl/InsightAI.git
cd InsightAI

mvn clean install -DskipTests

# AI服务
cd ai-service && mvn spring-boot:run

# 其他微服务
cd ../gateway && mvn spring-boot:run
cd ../query-service && mvn spring-boot:run
cd ../visualization-service && mvn spring-boot:run
```

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 3. 配置

```yaml
# ai-service/src/main/resources/application.yml
spring:
  ai:
    deepseek:
      api-key: your-api-key
      model: deepseek-chat
```

## 📡 核心API

### 自然语言转SQL

```
POST /api/v1/ai/query/convert

{
  "question": "去年每个月的销售额是多少？按月份排序",
  "databaseSchema": {
    "sales": ["id", "amount", "date", "region", "product"]
  }
}

响应：
{
  "sql": "SELECT DATE_FORMAT(date, '%Y-%m') as month, SUM(amount) as total_sales\nFROM sales\nWHERE date >= '2023-01-01' AND date <= '2023-12-31'\nGROUP BY DATE_FORMAT(date, '%Y-%m')\nORDER BY month",
  "explanation": "根据您的问题，我生成了查询去年每月销售额的SQL...",
  "confidence": 0.95
}
```

### 智能可视化推荐

```
POST /api/v1/ai/visualization/recommend

{
  "dataDescription": "每月销售额趋势数据",
  "dataFields": {
    "xAxis": "month",
    "yAxis": "sales_amount"
  },
  "dataSample": [...]
}

响应：
{
  "recommendedChart": "line",
  "confidence": 0.92,
  "reasoning": "您要展示随时间变化的趋势，线图最适合...",
  "alternativeCharts": [
    {"type": "area", "score": 0.78},
    {"type": "bar", "score": 0.65}
  ],
  "chartConfig": {
    "showTrendLine": true,
    "showForecast": true
  }
}
```

### 时序预测

```
POST /api/v1/ai/forecast

{
  "metricName": "销售额",
  "historicalData": [
    {"date": "2024-01", "value": 100000},
    {"date": "2024-02", "value": 120000},
    ...
  ],
  "forecastPeriods": 30,
  "confidenceLevel": 0.95
}

响应：
{
  "forecast": [
    {"date": "2024-07", "value": 180000, "lower": 160000, "upper": 200000},
    {"date": "2024-08", "value": 195000, "lower": 170000, "upper": 220000},
    ...
  ],
  "trend": "increasing",
  "seasonality": "明显季节性波动，Q4为销售旺季",
  "anomalies": ["2024-03出现异常高值，可能有促销影响"],
  "confidence": 0.88
}
```

### 异常检测

```
POST /api/v1/anomaly/detect

{
  "metricName": "CPU使用率",
  "dataPoints": [
    {"timestamp": "2024-01-01 10:00", "value": 45},
    {"timestamp": "2024-01-01 10:05", "value": 48},
    ...
  ],
  "threshold": 0.95,
  "detectionType": "realtime"
}

响应：
{
  "anomalies": [
    {
      "timestamp": "2024-01-01 11:30",
      "value": 98.5,
      "expectedValue": 50.2,
      "severity": "high",
      "description": "CPU使用率异常飙升"
    }
  ],
  "alarmTriggered": true,
  "alarmId": "alarm-2024-001"
}
```

### 自然语言查询

```
POST /api/v1/nl/query

{
  "question": "上个月东北区域的销售额是多少？",
  "databaseSchema": {
    "sales": ["id", "amount", "date", "region", "product"]
  },
  "context": {"timeRange": "last_month", "region": "northeast"}
}

响应：
{
  "sql": "SELECT SUM(amount) FROM sales WHERE region='东北' AND date >= '2024-06-01' AND date <= '2024-06-30'",
  "explanation": "根据您的问题，我理解您想查询东北区域上个月的销售额总和...",
  "visualization": {
    "type": "gauge",
    "title": "东北区域上月销售额"
  },
  "confidence": 0.94
}
```

### 自动报告生成

```
POST /api/v1/ai/report/generate

{
  "topic": "2024年Q2销售分析",
  "dataSources": ["sales", "customers", "products"],
  "includeSections": ["executive_summary", "trends", "comparisons", "recommendations"]
}

响应：
{
  "title": "2024年Q2销售分析报告",
  "executiveSummary": "本季度销售额同比增长25%，完成率达到105%...",
  "sections": {
    "trends": "销售额整体呈上升趋势，6月份表现最佳...",
    "comparisons": "线上渠道增长40%，线下渠道增长15%...",
    "recommendations": "建议加大线上渠道投入..."
  },
  "keyInsights": [
    "新客户获取成本下降15%",
    "客户复购率达到35%",
    "华北区域增长最快"
  ],
  "charts": [...]
}
```

## 🎯 AI分析流程

```
用户提问（自然语言）
    ↓
[NL2SQL] → 生成SQL → 执行查询
    ↓
[可视化推荐] → 最佳图表
    ↓
[时序预测] → 未来趋势
    ↓
[归因分析] → 变化原因
    ↓
[自动报告] → 完整分析报告
    ↓
[异常检测] → 实时告警
```

## 🆕 新增服务模块

### 🔮 forecasting-service (预测分析服务)
- 时间序列预测
- 趋势分析
- 季节性检测
- 多周期预测支持

### ⚠️ anomaly-detection-service (异常检测服务)
- 实时异常检测
- 智能报警
- 历史记录查询
- 多指标监控

### 💬 nl-query-service (自然语言查询服务)
- NLP语义理解
- SQL自动生成
- 查询结果可视化
- 多轮对话支持

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Element Plus + ECharts |
| 后端 | Java 21 + Spring Cloud Alibaba + MyBatis Plus |
| AI服务 | Spring Boot 3.2 + WebClient + DeepSeek |
| 数据库 | MySQL / PostgreSQL (支持多数据源) |
| 缓存 | Redis |
| 注册中心 | Nacos |

## 📊 功能列表

### 自然语言查询
- 语音/文本输入问题
- 自动生成SQL
- 查询结果展示
- 查询历史记录

### 数据看板
- 可视化图表库
- 拖拽式仪表盘
- AI图表推荐
- 实时数据刷新

### 报表中心
- 报表模板管理
- 自动报告生成
- 报表订阅推送
- 导出功能（PDF/Excel）

### AI洞察
- 异常检测预警
- 趋势分析
- 对比分析
- 智能建议

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

## 👨‍💻 作者

**黄辉翔** - [GitHub](https://github.com/nplszfl)

---

⭐ 如果对你有帮助，请给项目一个 Star！
