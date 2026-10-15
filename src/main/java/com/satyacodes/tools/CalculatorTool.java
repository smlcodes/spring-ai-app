package com.satyacodes.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * @author satyakaveti on 15/10/26
 */
public class CalculatorTool {

    @Tool(description = "Add two numbers")
    public int add(int a, int b) {
        return a + b;
    }

    @Tool(description = "Add two numbers")
    public int multiply(int a, int b) {
        return a * b;
    }
}