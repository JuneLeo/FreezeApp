package com.john.freezeapp.daemon.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemoryMonitorParse {

    // Pattern to match process header: ** MEMINFO in pid <pid> [<package_name>] **
    private static final Pattern PROCESS_HEADER_PATTERN =
            Pattern.compile("\\*\\* MEMINFO in pid (\\d+) \\[(.+?)\\] \\*\\*");

    // Pattern to match App Summary section values
    private static final Pattern JAVA_HEAP_PATTERN =
            Pattern.compile("Java Heap:\\s+(\\d+)");
    private static final Pattern NATIVE_HEAP_PATTERN =
            Pattern.compile("Native Heap:\\s+(\\d+)");
    private static final Pattern CODE_PATTERN =
            Pattern.compile("Code:\\s+(\\d+)");
    private static final Pattern STACK_PATTERN =
            Pattern.compile("Stack:\\s+(\\d+)");
    private static final Pattern GRAPHICS_PATTERN =
            Pattern.compile("Graphics:\\s+(\\d+)");
    private static final Pattern PRIVATE_OTHER_PATTERN =
            Pattern.compile("Private Other:\\s+(\\d+)");
    private static final Pattern SYSTEM_PATTERN =
            Pattern.compile("System:\\s+(\\d+)");
    private static final Pattern TOTAL_PSS_PATTERN =
            Pattern.compile("TOTAL PSS:\\s+(\\d+)");
    private static final Pattern TOTAL_SWAP_PSS_PATTERN =
            Pattern.compile("TOTAL SWAP PSS:\\s+(\\d+)");

    public static List<MemoryData> parse(String content) {
        List<MemoryData> result = new ArrayList<>();

        if (content == null || content.isEmpty()) {
            return result;
        }

        // Split content by process sections
        String[] sections = content.split("\\*\\* MEMINFO in pid");

        for (String section : sections) {
            if (section.trim().isEmpty()) {
                continue;
            }

            // Add back the header marker for pattern matching
            String fullSection = "** MEMINFO in pid" + section;
            MemoryData data = parseProcessSection(fullSection);

            if (data != null) {
                result.add(data);
            }
        }

        return result;
    }

    private static MemoryData parseProcessSection(String section) {
        try {
            MemoryData data = new MemoryData();

            // Extract PID and package name from header
            Matcher headerMatcher = PROCESS_HEADER_PATTERN.matcher(section);
            if (!headerMatcher.find()) {
                return null;
            }

            data.mPid = Integer.parseInt(headerMatcher.group(1));
            data.mPackageName = headerMatcher.group(2);

            // Extract values from App Summary section
            // Find the App Summary section
            int appSummaryStart = section.indexOf("App Summary");
            if (appSummaryStart == -1) {
                return null;
            }

            // Extract the App Summary block
            int appSummaryEnd = section.indexOf("Objects", appSummaryStart);
            if (appSummaryEnd == -1) {
                appSummaryEnd = section.length();
            }

            String appSummary = section.substring(appSummaryStart, appSummaryEnd);

            // Parse each value
            data.mJavaHeapPssSize = parseValue(appSummary, JAVA_HEAP_PATTERN);
            data.mNativeHeapPssSize = parseValue(appSummary, NATIVE_HEAP_PATTERN);
            data.mCodePssSize = parseValue(appSummary, CODE_PATTERN);
            data.mStackPssSize = parseValue(appSummary, STACK_PATTERN);
            data.mGraphicsPssSize = parseValue(appSummary, GRAPHICS_PATTERN);
            data.mPrivateOtherPssSize = parseValue(appSummary, PRIVATE_OTHER_PATTERN);
            data.mSystemPssSize = parseValue(appSummary, SYSTEM_PATTERN);

            // Parse TOTAL PSS and TOTAL SWAP PSS from the summary line
            // Format: "TOTAL PSS:   815315            TOTAL RSS:  1011596       TOTAL SWAP PSS:       87"
            Matcher totalPssMatcher = TOTAL_PSS_PATTERN.matcher(appSummary);
            if (totalPssMatcher.find()) {
                data.mTotalPssSize = Long.parseLong(totalPssMatcher.group(1));
            }

            Matcher totalSwapPssMatcher = TOTAL_SWAP_PSS_PATTERN.matcher(appSummary);
            if (totalSwapPssMatcher.find()) {
                data.mTotalSwapPssSize = Long.parseLong(totalSwapPssMatcher.group(1));
            }

            return data;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static long parseValue(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
