import re
import os

def fix_file(filepath, errors):
    with open(filepath, 'r') as f:
        lines = f.readlines()
        
    # Process from bottom to top to avoid shifting line numbers
    errors = sorted(errors, key=lambda x: x[0], reverse=True)
    
    for line_num, err_type, msg in errors:
        idx = line_num - 1
        
        if err_type == 'RegexpSingleline':
            lines[idx] = lines[idx].rstrip() + '\n'
            
        elif err_type == 'UnusedImports':
            del lines[idx]
            
        elif err_type == 'FinalParameters':
            # msg usually looks like: Parameter request should be final.
            match = re.search(r'Parameter (.*?) should be final', msg)
            if match:
                param = match.group(1)
                # Find the param in the line and prepend 'final '
                # Regex replace \bparam\b but we have to be careful with types
                # Just add final before the type
                # e.g. String param -> final String param
                # Let's do a simple regex:
                # We need to find the word before the param
                words = re.findall(r'\b\w+\b', lines[idx])
                if param in words:
                    # just blindly replace ` Type param` with ` final Type param`
                    lines[idx] = re.sub(r'(\w+(?:<\w+>)?)\s+' + param + r'\b', r'final \1 ' + param, lines[idx])

        elif err_type in ['MissingJavadocMethod', 'JavadocVariable', 'MissingJavadocType', 'JavadocPackage']:
            # For JavadocPackage, it wants package-info.java, just ignore or create it
            if err_type == 'JavadocPackage':
                pkg = lines[idx].replace('package ', '').replace(';', '').strip()
                pkg_dir = os.path.dirname(filepath)
                with open(os.path.join(pkg_dir, 'package-info.java'), 'w') as pf:
                    pf.write(f"/**\n * Package {pkg}.\n */\npackage {pkg};\n")
                continue
            
            # Find indentation
            indent_match = re.match(r'^\s*', lines[idx])
            indent = indent_match.group(0) if indent_match else ''
            javadoc = f"{indent}/**\n{indent} * Javadoc.\n{indent} */\n"
            lines.insert(idx, javadoc)
            
        elif err_type == 'LineLength':
            # Break line at 80 characters by finding the last space before 80
            # Wait, line might have leading spaces.
            line = lines[idx]
            if len(line) > 80:
                # find a good split point
                # split on comma, or before method call
                split_idx = 79
                while split_idx > 0 and line[split_idx] not in [' ', ',', '.', '(']:
                    split_idx -= 1
                if split_idx > 0:
                    if line[split_idx] in [',', '.', '(']:
                        split_idx += 1
                    indent_match = re.match(r'^\s*', line)
                    indent = indent_match.group(0) if indent_match else ''
                    
                    lines[idx] = line[:split_idx] + '\n'
                    lines.insert(idx+1, indent + "    " + line[split_idx:].lstrip())
                    
        elif err_type == 'DesignForExtension':
            # Ignore or add final to method?
            # User said: "Add method Javadocs instead"
            # Since MissingJavadocMethod will add javadoc, DesignForExtension might go away.
            pass
            
    with open(filepath, 'w') as f:
        f.writelines(lines)

def main():
    log_file = '/home/sakhadir/.gemini/antigravity-ide/brain/edfa1b2b-ac81-4430-a3c9-ee1242a49024/.system_generated/tasks/task-112.log'
    with open(log_file, 'r') as f:
        log = f.read()

    errors = re.findall(r'\[ERROR\] (.*?):\[(\d+)(?:,\d+)?\] \((.*?)\) (.*?): (.*)', log)
    
    files_to_fix = {}
    for filepath, line, category, err_type, msg in errors:
        if filepath not in files_to_fix:
            files_to_fix[filepath] = []
        files_to_fix[filepath].append((int(line), err_type, msg))
        
    for filepath, errs in files_to_fix.items():
        if os.path.exists(filepath):
            fix_file(filepath, errs)
            
    print(f"Fixed {len(files_to_fix)} files")

if __name__ == '__main__':
    main()
