import os
import re

def fix_trailing_spaces(lines):
    return [line.rstrip() + '\n' for line in lines]

def fix_final_params(lines):
    # Regex to find method signatures and add final
    # This is a bit tricky, maybe use a simpler approach
    pass

# We will just run checkstyle, parse the output, and fix line by line automatically.
import subprocess

def run_checkstyle():
    result = subprocess.run(["./mvnw", "checkstyle:check"], capture_output=True, text=True, cwd="backend")
    return result.stdout

def fix_unused_imports(lines, error_lines):
    # error_lines is a list of line numbers to remove
    error_lines = sorted(error_lines, reverse=True)
    for line_num in error_lines:
        idx = line_num - 1
        if "import " in lines[idx]:
            del lines[idx]
    return lines

# Let's just fix the files the user mentioned first.
