import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { QueryResult, Insight, Dashboard, ChartConfig, DashboardWidget } from '@/types'

export const useInsightStore = defineStore('insight', () => {
  // State
  const currentQuery = ref('')
  const queryResult = ref<QueryResult | null>(null)
  const isLoading = ref(false)
  const insights = ref<Insight[]>([])
  const dashboards = ref<Dashboard[]>([])
  const currentDashboard = ref<Dashboard | null>(null)
  const chartConfig = ref<ChartConfig>({
    type: 'bar',
    title: 'Chart',
    xField: '',
    yField: ''
  })

  // Demo data for visualization
  const demoData = ref({
    sales: [
      { month: 'Jan', value: 1200 },
      { month: 'Feb', value: 1900 },
      { month: 'Mar', value: 1500 },
      { month: 'Apr', value: 2300 },
      { month: 'May', value: 2800 },
      { month: 'Jun', value: 3200 }
    ],
    categories: [
      { name: 'Electronics', value: 4500 },
      { name: 'Clothing', value: 3200 },
      { name: 'Food', value: 2800 },
      { name: 'Books', value: 1500 }
    ],
    trends: [
      { date: '2024-01', value: 100 },
      { date: '2024-02', value: 120 },
      { date: '2024-03', value: 115 },
      { date: '2024-04', value: 140 },
      { date: '2024-05', value: 160 },
      { date: '2024-06', value: 180 }
    ]
  })

  // Actions
  async function executeQuery(query: string) {
    isLoading.value = true
    currentQuery.value = query
    
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 800))
      
      queryResult.value = {
        columns: ['category', 'value', 'trend'],
        rows: [
          ['Electronics', 4500, '+12%'],
          ['Clothing', 3200, '+5%'],
          ['Food', 2800, '-2%'],
          ['Books', 1500, '+8%']
        ],
        summary: 'Query executed successfully'
      }
    } finally {
      isLoading.value = false
    }
  }

  async function fetchInsights() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 600))
      insights.value = [
        {
          id: '1',
          title: 'Sales Spike Detected',
          description: 'Electronics category shows 15% increase in Q2 compared to previous quarter',
          type: 'anomaly',
          confidence: 0.92,
          timestamp: new Date()
        },
        {
          id: '2',
          title: 'Growing Trend',
          description: 'Online orders continue to grow at 8% monthly rate',
          type: 'trend',
          confidence: 0.88,
          timestamp: new Date()
        },
        {
          id: '3',
          title: 'Correlation Found',
          description: 'Customer satisfaction correlates with delivery speed (r=0.85)',
          type: 'correlation',
          confidence: 0.78,
          timestamp: new Date()
        }
      ]
    } finally {
      isLoading.value = false
    }
  }

  function addWidget(widget: Omit<DashboardWidget, 'id'>) {
    if (!currentDashboard.value) return
    
    const newWidget: DashboardWidget = {
      ...widget,
      id: Date.now().toString()
    }
    currentDashboard.value.widgets.push(newWidget)
  }

  function updateChartConfig(config: Partial<ChartConfig>) {
    chartConfig.value = { ...chartConfig.value, ...config }
  }

  function createDashboard(name: string) {
    const newDashboard: Dashboard = {
      id: Date.now().toString(),
      name,
      widgets: [],
      createdAt: new Date(),
      updatedAt: new Date()
    }
    dashboards.value.push(newDashboard)
    currentDashboard.value = newDashboard
    return newDashboard
  }

  return {
    currentQuery,
    queryResult,
    isLoading,
    insights,
    dashboards,
    currentDashboard,
    chartConfig,
    demoData,
    executeQuery,
    fetchInsights,
    addWidget,
    updateChartConfig,
    createDashboard
  }
})