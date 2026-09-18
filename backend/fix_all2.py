import re
import os

def fix_file(filepath, errors):
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f:
        lines = f.readlines()
        
    errors = sorted(errors, key=lambda x: x[0], reverse=True)
    
    for line_num, err_type, msg in errors:
        idx = line_num - 1
        
        if err_type == 'javadoc':
            if "Missing a Javadoc comment" in msg:
                # insert above the line
                lines.insert(idx, "    /**\n     * Javadoc.\n     */\n")
            elif "Unused @param tag for" in msg:
                param = re.search(r"Unused @param tag for '(.*?)'", msg).group(1)
                if '@param ' + param in lines[idx]:
                    del lines[idx]
            elif "Expected @param tag for" in msg:
                param = re.search(r"Expected @param tag for '(.*?)'", msg).group(1)
                old_param = param[1].lower() + param[2:] if param.startswith('p') else param
                found = False
                for j in range(idx, max(-1, idx-15), -1):
                    if '@param ' + old_param in lines[j]:
                        lines[j] = lines[j].replace('@param ' + old_param, '@param ' + param)
                        found = True
                        break
                if not found:
                    for j in range(idx, max(-1, idx-15), -1):
                        if '*/' in lines[j]:
                            lines.insert(j, f"     * @param {param} description\n")
                            break
            elif "@return tag should be present and have description" in msg:
                for j in range(idx, max(-1, idx-15), -1):
                    if '*/' in lines[j]:
                        lines.insert(j, f"     * @return description\n")
                        break
        elif err_type == 'misc':
            if 'FinalParameters' in msg:
                param = re.search(r"Parameter (.*?) should be final.", msg).group(1)
                lines[idx] = re.sub(r'([A-Za-z0-9_<>?\[\]]+)\s+(' + param + r')\b', r'final \1 \2', lines[idx])
        elif err_type == 'coding':
            pass
        elif err_type == 'sizes':
            if "LineLength" in msg:
                # find the best place to split
                line = lines[idx]
                indent = re.match(r'^\s*', line).group(0) + "        "
                split_idx = -1
                for j in range(min(len(line)-1, 79), 10, -1):
                    if line[j:j+2] == ', ':
                        split_idx = j + 1
                        break
                    elif line[j:j+4] in [' == ', ' != ', ' && ', ' || ']:
                        split_idx = j + 1
                        break
                    elif line[j:j+3] == ' + ':
                        split_idx = j + 1
                        break
                    elif line[j:j+2] == ' (':
                        split_idx = j + 1
                        break
                    elif line[j] == '(' and line[j-1] != ' ':
                        split_idx = j + 1
                        break
                        
                if split_idx != -1:
                    first_part = line[:split_idx] + '\n'
                    second_part = indent + line[split_idx:].lstrip()
                    lines[idx] = first_part
                    lines.insert(idx+1, second_part)
        elif err_type == 'whitespace':
            if "OperatorWrap" in msg:
                op_match = re.search(r"'(.*?)' should be on a new line", msg)
                if op_match:
                    op = op_match.group(1)
                    line = lines[idx]
                    if op in line:
                        lines[idx] = re.sub(r'\s*' + re.escape(op) + r'\s*$', '\n', line)
                        indent = re.match(r'^\s*', lines[idx+1]).group(0)
                        lines[idx+1] = indent + op + ' ' + lines[idx+1].lstrip()
                        
    with open(filepath, 'w') as f:
        f.writelines(lines)

def main():
    log_file = 'checkstyle.log'
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
            
if __name__ == '__main__':
    main()
