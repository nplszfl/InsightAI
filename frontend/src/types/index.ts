export interface ChartConfig {
  type: 'bar' | 'line' | 'pie' | 'scatter' | 'area'
  title: string
  xField: string
  yField: string
  colors?: string[]
}

export interface QueryResult {
  columns: string[]
  rows: any[][]
  summary?: string
}

export interface Insight {
  id: string
  title: string
  description: string
  type: 'anomaly' | 'trend' | 'correlation' | 'summary'
  confidence: number
  timestamp: Date
}

export interface DashboardWidget {
  id: string
  title: string
  chartType: ChartConfig['type']
  query: string
  position: { x: number, y: number, w: number, h: number }
}

export interface Dashboard {
  id: string
  name: string
  widgets: DashboardWidget[]
  createdAt: Date
  updatedAt: Date
}