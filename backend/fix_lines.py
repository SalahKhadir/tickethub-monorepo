import os
import re

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
        
    print(f"Found errors in {len(files_to_fix)} files")
    for f in sorted(files_to_fix.keys()):
        print(f"  {f}: {len(files_to_fix[f])} errors")

if __name__ == '__main__':
    main()
