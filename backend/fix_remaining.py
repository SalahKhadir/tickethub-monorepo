import re
import os

def fix_file(filepath, errors):
    with open(filepath, 'r') as f:
        lines = f.readlines()
        
    errors = sorted(errors, key=lambda x: x[0], reverse=True)
    
    for line_num, err_type, msg in errors:
        idx = line_num - 1
        
        if err_type == 'FinalParameters':
            param = re.search(r"Parameter (.*?) should be final.", msg).group(1)
            # Find the parameter in the line and prepend final
            line = lines[idx]
            # Replace exactly the param name (as a whole word) if it's not already preceded by final
            # Since checkstyle only reports it if it's not final, we just find it as a whole word,
            # or rather the type before it.
            # Example: "String param" -> "final String param"
            # Since we just want to insert 'final ' before the type, it's safer to just regex replace
            # "([a-zA-Z0-9_<>\?\[\]]+)\s+" + param + r"\b" with "final \1 " + param
            # We must be careful not to match methods.
            lines[idx] = re.sub(r'([A-Za-z0-9_<>?\[\]]+)\s+(' + param + r')\b', r'final \1 \2', line)
            
        elif err_type == 'HiddenField':
            # e.g., 'userRepository' hides a field.
            param = re.search(r"'(.*?)' hides a field.", msg).group(1)
            line = lines[idx]
            # rename param to pParam in the signature
            pParam = 'p' + param[0].upper() + param[1:]
            lines[idx] = re.sub(r'\b' + param + r'\b', pParam, line)
            
            # We also need to change this.param = param to this.param = pParam in the following lines of the constructor
            for i in range(idx + 1, min(idx + 10, len(lines))):
                if 'this.' + param in lines[i] and ('= ' + param + ';' in lines[i] or '=' + param + ';' in lines[i]):
                    lines[i] = lines[i].replace('= ' + param + ';', '= ' + pParam + ';')
                    lines[i] = lines[i].replace('=' + param + ';', '=' + pParam + ';')
                    break
        elif err_type == 'AvoidStarImport':
            # Remove the wildcard import
            # We need to know which ones to import.
            # In AdminUserController, it's org.springframework.web.bind.annotation.*.
            # Let's just remove it and let the user add specific ones if needed, or we hardcode the common ones
            if 'org.springframework.web.bind.annotation.*' in msg:
                lines[idx] = """import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
"""
        elif err_type == 'LineLength':
            pass
        elif err_type == 'OperatorWrap':
            pass

    with open(filepath, 'w') as f:
        f.writelines(lines)

def main():
    log_file = 'checkstyle.log'
    os.system('./mvnw checkstyle:check > checkstyle.log || true')
    with open(log_file, 'r') as f:
        log = f.read()

    errors = re.findall(r'\[ERROR\] (.*?):\[(\d+)(?:,\d+)?\] \((.*?)\) (.*?): (.*)', log)
    
    files_to_fix = {}
    for filepath, line, category, err_type, msg in errors:
        if err_type in ['FinalParameters', 'HiddenField', 'AvoidStarImport']:
            if filepath not in files_to_fix:
                files_to_fix[filepath] = []
            files_to_fix[filepath].append((int(line), err_type, msg))
        
    for filepath, errs in files_to_fix.items():
        if os.path.exists(filepath):
            fix_file(filepath, errs)
            
    print(f"Fixed {len(files_to_fix)} files")

if __name__ == '__main__':
    main()
