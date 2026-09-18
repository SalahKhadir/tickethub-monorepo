import re
import os

def fix_line_lengths(filepath):
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f:
        lines = f.readlines()
        
    out_lines = []
    i = 0
    while i < len(lines):
        line = lines[i]
        
        # Check if line is too long
        # But skip imports
        if len(line) > 81 and not line.startswith("import "):
            # Let's find the best split point from the end (before 80 chars)
            # good split points: ' (', ', ', ' == ', ' != ', ' && ', ' || ', ' + '
            # For operator wrap, the operator goes to the NEXT line!
            # So if we split at ' == ', we split before '=='.
            match = None
            split_idx = -1
            split_char = ''
            
            # search backwards from 80
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
                indent = re.match(r'^\s*', line).group(0) + "        "
                first_part = line[:split_idx] + '\n'
                second_part = indent + line[split_idx:].lstrip()
                out_lines.append(first_part)
                # the second part might be too long as well, but let's just let checkstyle catch it next time or it's fine for now.
                lines.insert(i+1, second_part)
            else:
                out_lines.append(line)
        else:
            out_lines.append(line)
            
        i += 1
        
    with open(filepath, 'w') as f:
        f.writelines(out_lines)

files = [
    "src/main/java/com/tickethub/security/SecurityConfig.java",
    "src/main/java/com/tickethub/service/NotificationPushService.java",
    "src/main/java/com/tickethub/service/SlaMonitoringService.java",
    "src/main/java/com/tickethub/service/UserService.java",
    "src/main/java/com/tickethub/service/impl/TicketServiceImpl.java",
    "src/main/java/com/tickethub/controller/UserController.java"
]

for f in files:
    fix_line_lengths(f)
