package com.insightai.nlquery.service;

import com.insightai.nlquery.dto.NlQueryRequest;
import com.insightai.nlquery.dto.NlQueryResponse;
import com.insightai.nlquery.mapper.NlQueryHistoryMapper;
import com.insightai.nlquery.mapper.VisualizationResultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NlQueryServiceTest {
    
    @Mock
    private NlQueryHistoryMapper nlQueryHistoryMapper;
    
    @Mock
    private VisualizationResultMapper visualizationResultMapper;
    
    @InjectMocks
    private NlQueryService nlQueryService;
    
    private NlQueryRequest request;
    
    @BeforeEach
    void setUp() {
        request = NlQueryRequest.builder()
                .queryText("Show me total sales by category")
                .dataSource("mysql")
                .tableNames(Arrays.asList("sales_data"))
                .columnMappings(new HashMap<>())
                .createdBy("test_user")
                .build();
    }
    
    @Test
    void testProcessQuery_ShouldReturnQueryResponse() {
        when(nlQueryHistoryMapper.insert(any())).thenAnswer(invocation -> {
            var history = invocation.getArgument(0, com.insightai.nlquery.entity.NlQueryHistory.class);
            history.setId(1L);
            return 1;
        });
        when(visualizationResultMapper.insert(any())).thenAnswer(invocation -> {
            var viz = invocation.getArgument(0, com.insightai.nlquery.entity.VisualizationResult.class);
            viz.setId(1L);
            return 1;
        });
        
        NlQueryResponse result = nlQueryService.processQuery(request);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Show me total sales by category", result.getOriginalQuery());
        assertNotNull(result.getGeneratedSql());
        assertEquals("AGGREGATION", result.getIntent());
        verify(nlQueryHistoryMapper, times(1)).insert(any());
        verify(visualizationResultMapper, times(1)).insert(any());
    }
    
    @Test
    void testProcessQuery_WithTrendQuery_ShouldGenerateCorrectIntent() {
        request.setQueryText("Show me the sales trend over time");
        
        when(nlQueryHistoryMapper.insert(any())).thenAnswer(invocation -> {
            var history = invocation.getArgument(0, com.insightai.nlquery.entity.NlQueryHistory.class);
            history.setId(1L);
            return 1;
        });
        when(visualizationResultMapper.insert(any())).thenAnswer(invocation -> {
            var viz = invocation.getArgument(0, com.insightai.nlquery.entity.VisualizationResult.class);
            viz.setId(1L);
            return 1;
        });
        
        NlQueryResponse result = nlQueryService.processQuery(request);
        
        assertNotNull(result);
        assertEquals("TREND_ANALYSIS", result.getIntent());
    }
}