package com.periut.omni;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GLSLTranspiler {
    private GLSLTranspiler() {}

    public static String transpile450to330(String source) {
        String result = source;
        result = changeVersion(result);
        result = removeFragmentOutputLocations(result);
        result = extractUniformBlocks(result);
        result = removeSamplerBindings(result);
        return result;
    }

    private static String changeVersion(String source) {
        return source.replaceAll(
                "#version\\s+450\\s+core",
                "#version 330 core\n#extension GL_ARB_explicit_attrib_location : require"
        );
    }

    private static String removeFragmentOutputLocations(String source) {
        String result = source;
        // Remove layout(location=N) from all 'out' declarations
        result = result.replaceAll(
                "layout\\s*\\(\\s*location\\s*=\\s*\\d+\\s*\\)\\s*(out\\s+)",
                "$1"
        );
        // Remove layout(location=N) from 'in' declarations for varyings (names starting with v)
        // Keep layout(location=N) for vertex attributes (names starting with a)
        result = result.replaceAll(
                "layout\\s*\\(\\s*location\\s*=\\s*\\d+\\s*\\)\\s*(in\\s+\\w+\\s+v)",
                "$1"
        );
        return result;
    }

    public static String extractUniformBlocks(String source) {
        String result = source;
        Pattern blockPattern = Pattern.compile(
                "layout\\s*\\(\\s*binding\\s*=\\s*\\d+\\s*\\)\\s*uniform\\s+\\w+\\s*\\{([^}]*)\\}\\s*;"
        );

        // Collect all replacements
        List<int[]> positions = new ArrayList<>();
        List<String> replacements = new ArrayList<>();

        Matcher matcher = blockPattern.matcher(result);
        while (matcher.find()) {
            positions.add(new int[]{matcher.start(), matcher.end()});
            String blockContents = matcher.group(1);
            StringBuilder uniforms = new StringBuilder();
            for (String line : blockContents.split("\n")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                if (trimmed.contains(";")) {
                    String decl = trimmed;
                    if (decl.endsWith(";")) {
                        decl = decl.substring(0, decl.length() - 1).trim();
                    }
                    if (!decl.isEmpty()) {
                        uniforms.append("uniform ").append(decl).append(";\n");
                    }
                }
            }
            replacements.add(uniforms.toString());
        }

        // Apply in reverse order
        for (int i = positions.size() - 1; i >= 0; i--) {
            result = result.substring(0, positions.get(i)[0]) +
                    replacements.get(i) +
                    result.substring(positions.get(i)[1]);
        }

        return result;
    }

    private static String removeSamplerBindings(String source) {
        return source.replaceAll(
                "layout\\s*\\(\\s*binding\\s*=\\s*\\d+\\s*\\)\\s*(uniform\\s+sampler\\w+)",
                "$1"
        );
    }
}
