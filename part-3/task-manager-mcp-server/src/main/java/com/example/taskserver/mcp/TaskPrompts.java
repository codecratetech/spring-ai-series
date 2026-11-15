package com.example.taskserver.mcp;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

@Component
public class TaskPrompts {
    @McpPrompt(name = "weekly_planning",
            description = "Plan the week using the current tasks")
    public String weeklyPlanning(
            @McpArg(name = "focus", description = "What to focus on this week, e.g. 'YouTube channel'", required = false)
            String focus) {

        String topic = (focus == null || focus.isBlank()) ? "my most important work" : focus;
        return """
                Use the list_tasks tool to read my open tasks, then build a day-by-day plan
                for the next 7 days. Prioritise tasks with the nearest due dates and put extra
                emphasis on: %s. Keep the plan short and actionable.
                """.formatted(topic);
    }
}
