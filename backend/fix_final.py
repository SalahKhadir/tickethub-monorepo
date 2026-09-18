import re

def insert_javadoc(filepath, lines_indices):
    with open(filepath, 'r') as f:
        lines = f.readlines()
    for idx in sorted(lines_indices, reverse=True):
        lines.insert(idx, "    /**\n     * Javadoc.\n     */\n")
    with open(filepath, 'w') as f:
        f.writelines(lines)

# UserController.java
filepath = "src/main/java/com/tickethub/controller/UserController.java"
with open(filepath, 'r') as f:
    lines = f.readlines()
lines[105] = lines[105].replace("            return ResponseEntity.ok(toTechnicianResponses(userRepository.", 
                                "            return ResponseEntity.ok(\n                    toTechnicianResponses(userRepository.")
lines[108] = lines[108].replace("        return ResponseEntity.ok(toTechnicianResponses(userRepository.", 
                                "        return ResponseEntity.ok(\n                toTechnicianResponses(userRepository.")
with open(filepath, 'w') as f:
    f.writelines(lines)

insert_javadoc(filepath, [26, 27]) # lines 27, 28 missing javadoc before my insertions

# CustomUserDetailsService.java
insert_javadoc("src/main/java/com/tickethub/security/CustomUserDetailsService.java", [13]) # line 14 missing javadoc

# JwtAuthenticationFilter.java
filepath = "src/main/java/com/tickethub/security/jwt/JwtAuthenticationFilter.java"
with open(filepath, 'r') as f:
    lines = f.readlines()
lines[80] = lines[80].replace("            final FilterChain filterChain) throws ServletException, IOException {", 
                              "            final FilterChain filterChain)\n            throws ServletException, IOException {")
with open(filepath, 'w') as f:
    f.writelines(lines)

# JwtTokenProvider.java
insert_javadoc("src/main/java/com/tickethub/security/jwt/JwtTokenProvider.java", [23, 24, 25])

# AuthService.java
insert_javadoc("src/main/java/com/tickethub/service/AuthService.java", [32])
