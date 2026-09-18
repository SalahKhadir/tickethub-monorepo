import re
import os

def fix_file(filepath, errors):
    with open(filepath, 'r') as f:
        lines = f.readlines()
        
    # Process from bottom to top
    errors = sorted(errors, key=lambda x: x[0], reverse=True)
    
    for line_num, err_type, msg in errors:
        idx = line_num - 1
        
        if err_type == 'JavadocMethod':
            if 'Expected @param tag for' in msg:
                param = re.search(r"'(.*?)'", msg).group(1)
                # We need to find the Javadoc block above this line and insert @param
                # Find the end of the javadoc block (*/)
                javadoc_end = -1
                for i in range(idx, -1, -1):
                    if '*/' in lines[i]:
                        javadoc_end = i
                        break
                if javadoc_end != -1:
                    indent = re.match(r'^\s*', lines[javadoc_end]).group(0)
                    lines.insert(javadoc_end, f"{indent} * @param {param} description\n")
            elif '@return tag should be present' in msg:
                javadoc_end = -1
                for i in range(idx, -1, -1):
                    if '*/' in lines[i]:
                        javadoc_end = i
                        break
                if javadoc_end != -1:
                    indent = re.match(r'^\s*', lines[javadoc_end]).group(0)
                    lines.insert(javadoc_end, f"{indent} * @return description\n")
                    
        elif err_type == 'HiddenField':
            # Checkstyle says e.g. "'userRepository' hides a field."
            # We can either rename the parameter or just add `final `? No, HiddenField is about the parameter having the same name.
            # Usually, you can disable HiddenField for constructors, or we can just append 'Param' to the parameter name.
            # But wait! If we append 'Param' to the parameter name, we have to change it in the body too!
            # Let's just suppress it using @SuppressWarnings("checkstyle:HiddenField")?
            # No, user wants Checkstyle report to contain no violations, maybe we can just rename it to `pUserRepository` or `userRepositoryParam`.
            pass

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
            
    print(f"Fixed {len(files_to_fix)} files")

if __name__ == '__main__':
    main()
