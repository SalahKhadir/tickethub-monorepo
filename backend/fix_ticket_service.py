import re

filepath = "src/main/java/com/tickethub/service/impl/TicketServiceImpl.java"
with open(filepath, 'r') as f:
    lines = f.readlines()

def fix_op(line_idx, op):
    # move op to next line
    if op in lines[line_idx]:
        lines[line_idx] = lines[line_idx].replace(" " + op + "\n", "\n")
        indent = re.match(r'^\s*', lines[line_idx+1]).group(0)
        lines[line_idx+1] = indent + op + " " + lines[line_idx+1].lstrip()

# LineLength at 267
lines[266] = lines[266].replace(" // Status stays ACCEPTED — technician must click \"Start Work\" to move to IN_PROGRESS", 
                                " // Status stays ACCEPTED\n        // technician must click \"Start Work\" to move to IN_PROGRESS")

# OperatorWrap at 304 (==)
fix_op(303, "==")

# LineLength at 315
lines[314] = lines[314].replace("\"Only the assigned technician can start work on thisticket.\"", 
                                "\"Only the assigned technician can start work on \"\n                                + \"thisticket.\"")

# OperatorWrap at 317 (==)
fix_op(316, "==")

# LineLength at 328
lines[327] = lines[327].replace("\"Only the assigned technician can resolve this ticket.\"", 
                                "\"Only the assigned technician can resolve \"\n                                + \"this ticket.\"")

# OperatorWrap at 335 (==)
fix_op(334, "==")

# OperatorWrap at 343 (+)
fix_op(342, "+")

# LineLength at 375
lines[374] = lines[374].replace("// Critical security check: delete requires author ownership or staff role.", 
                                "// Critical security check:\n        // delete requires author ownership or staff role.")

# OperatorWrap at 546 (!=)
fix_op(545, "!=")

# OperatorWrap at 573 (||)
fix_op(572, "||")

with open(filepath, 'w') as f:
    f.writelines(lines)
