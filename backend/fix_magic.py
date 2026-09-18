import re
import os

def fix_register_request():
    path = "src/main/java/com/tickethub/dto/request/RegisterRequest.java"
    with open(path, 'r') as f:
        content = f.read()
    
    # insert constants
    constants = """    private static final int MAX_STRING_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_TEL_LENGTH = 30;
"""
    content = content.replace("public class RegisterRequest {", "public class RegisterRequest {\n" + constants)
    content = content.replace("@Size(max = 100", "@Size(max = MAX_STRING_LENGTH")
    content = content.replace("@Size(max = 150", "@Size(max = MAX_EMAIL_LENGTH")
    content = content.replace("@Size(min = 8, max = 100", "@Size(min = MIN_PASSWORD_LENGTH, max = MAX_STRING_LENGTH")
    content = content.replace("@Size(max = 30", "@Size(max = MAX_TEL_LENGTH")
    
    with open(path, 'w') as f:
        f.write(content)
        
def fix_ticket_request():
    path = "src/main/java/com/tickethub/dto/request/TicketRequest.java"
    with open(path, 'r') as f:
        content = f.read()
    constants = """    private static final int MAX_TITLE_LENGTH = 200;
"""
    content = content.replace("public class TicketRequest {", "public class TicketRequest {\n" + constants)
    content = content.replace("@Size(max = 200", "@Size(max = MAX_TITLE_LENGTH")
    
    with open(path, 'w') as f:
        f.write(content)

def fix_jwt_filter():
    path = "src/main/java/com/tickethub/security/jwt/JwtAuthenticationFilter.java"
    with open(path, 'r') as f:
        content = f.read()
    constants = """    private static final int BEARER_PREFIX_LENGTH = 7;
"""
    content = content.replace("public final class JwtAuthenticationFilter extends OncePerRequestFilter {", "public final class JwtAuthenticationFilter extends OncePerRequestFilter {\n" + constants)
    content = content.replace("substring(7)", "substring(BEARER_PREFIX_LENGTH)")
    
    with open(path, 'w') as f:
        f.write(content)

def fix_jwt_provider():
    path = "src/main/java/com/tickethub/security/jwt/JwtTokenProvider.java"
    with open(path, 'r') as f:
        content = f.read()
    constants = """    private static final int TOKEN_SUBSTRING_START = 3;
"""
    content = content.replace("public class JwtTokenProvider {", "public class JwtTokenProvider {\n" + constants)
    content = content.replace("split(\"\\.\")[3]", "split(\"\\.\")[TOKEN_SUBSTRING_START]")
    
    with open(path, 'w') as f:
        f.write(content)

def fix_push_service():
    path = "src/main/java/com/tickethub/service/NotificationPushService.java"
    with open(path, 'r') as f:
        content = f.read()
    constants = """    private static final int NOTIFICATION_TIMEOUT_MS = 15000;
"""
    content = content.replace("public class NotificationPushService {", "public class NotificationPushService {\n" + constants)
    content = content.replace("timeout(15000)", "timeout(NOTIFICATION_TIMEOUT_MS)")
    
    with open(path, 'w') as f:
        f.write(content)
        
if __name__ == '__main__':
    fix_register_request()
    fix_ticket_request()
    fix_jwt_filter()
    fix_jwt_provider()
    fix_push_service()
