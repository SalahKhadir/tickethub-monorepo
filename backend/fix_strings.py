import re

def fix_file(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()
        
    out = []
    i = 0
    while i < len(lines):
        line = lines[i]
        # Count quotes in the line (excluding escaped quotes)
        quotes = len(re.findall(r'(?<!\\)"', line))
        
        # If odd number of quotes, string is broken across this line and the next
        if quotes % 2 != 0 and i + 1 < len(lines):
            # Join with the next line
            # The next line has leading spaces that should probably be removed or kept?
            # If it's a string, maybe we just keep it or replace the newline with a space
            next_line = lines[i+1].lstrip()
            # Remove the newline from the first line
            line = line.rstrip('\n') + next_line
            out.append(line)
            i += 2
        else:
            out.append(line)
            i += 1
            
    with open(filepath, 'w') as f:
        f.writelines(out)

fix_file('src/main/java/com/tickethub/service/impl/TicketServiceImpl.java')
