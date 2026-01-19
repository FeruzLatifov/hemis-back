#!/usr/bin/env python3
"""
E2E Test: Captcha Endpoint Comparison
Tests: GET /app/rest/v2/services/captcha/getNumericCaptcha

Compares old-hemis (port 8082) vs new-hemis (port 8081) responses.
"""

import sys
import json
import urllib.request
import urllib.error
from typing import Dict, Any, Tuple

# Configuration
OLD_HEMIS_URL = "http://localhost:8082/app/rest/v2/services/captcha/getNumericCaptcha"
NEW_HEMIS_URL = "http://localhost:8081/app/rest/v2/services/captcha/getNumericCaptcha"
OLD_HEMIS_TOKEN = "zao3XK716L4zcP83DFgQpsdy4Fw"

# ANSI Colors
GREEN = '\033[92m'
RED = '\033[91m'
YELLOW = '\033[93m'
BLUE = '\033[94m'
RESET = '\033[0m'

def fetch_endpoint(url: str, token: str = None) -> Tuple[Dict[Any, Any], int]:
    """
    Fetch endpoint and return (response_json, status_code)
    """
    try:
        req = urllib.request.Request(url)
        req.add_header('Content-Type', 'application/json')

        if token:
            req.add_header('Authorization', f'Bearer {token}')

        with urllib.request.urlopen(req, timeout=10) as response:
            data = response.read()
            return json.loads(data.decode('utf-8')), response.status

    except urllib.error.HTTPError as e:
        return {"error": f"HTTP {e.code}: {e.reason}"}, e.code
    except urllib.error.URLError as e:
        return {"error": f"Connection error: {e.reason}"}, 0
    except Exception as e:
        return {"error": str(e)}, 0

def compare_responses(old: Dict[Any, Any], new: Dict[Any, Any]) -> Tuple[bool, str]:
    """
    Compare two responses and return (is_identical, diff_message)
    """
    if "error" in old:
        return False, f"Old-hemis error: {old['error']}"

    if "error" in new:
        return False, f"New-hemis error: {new['error']}"

    # Check if both have same keys
    old_keys = set(old.keys())
    new_keys = set(new.keys())

    if old_keys != new_keys:
        missing_in_new = old_keys - new_keys
        extra_in_new = new_keys - old_keys

        msg = []
        if missing_in_new:
            msg.append(f"Missing in new-hemis: {missing_in_new}")
        if extra_in_new:
            msg.append(f"Extra in new-hemis: {extra_in_new}")

        return False, " | ".join(msg)

    # Check if all field types match (id=string, image=string starting with data:image)
    for key in old_keys:
        old_val = old[key]
        new_val = new[key]

        # Type check
        if type(old_val) != type(new_val):
            return False, f"Type mismatch for '{key}': old={type(old_val).__name__}, new={type(new_val).__name__}"

        # For 'image' field, just check it starts with data:image
        if key == "image":
            if not old_val.startswith("data:image/png;base64,"):
                return False, f"Old-hemis image format invalid: {old_val[:50]}..."
            if not new_val.startswith("data:image/png;base64,"):
                return False, f"New-hemis image format invalid: {new_val[:50]}..."
            # Don't compare base64 content (it's random every time)
            continue

        # For 'id' field, just check it's a valid UUID format
        if key == "id":
            if len(old_val) < 30 or "-" not in old_val:
                return False, f"Old-hemis id format invalid: {old_val}"
            if len(new_val) < 30 or "-" not in new_val:
                return False, f"New-hemis id format invalid: {new_val}"
            # Don't compare UUID values (they're unique every time)
            continue

    return True, "Responses are structurally identical (keys, types, formats match)"

def print_response_summary(label: str, response: Dict[Any, Any], status: int):
    """
    Print response summary with truncated image
    """
    print(f"\n{BLUE}{'='*70}{RESET}")
    print(f"{BLUE}{label} (HTTP {status}){RESET}")
    print(f"{BLUE}{'='*70}{RESET}")

    if "error" in response:
        print(f"{RED}ERROR: {response['error']}{RESET}")
        return

    for key, value in response.items():
        if key == "image" and isinstance(value, str) and len(value) > 100:
            print(f"  {key}: {value[:80]}... (truncated, {len(value)} chars)")
        else:
            print(f"  {key}: {value}")

def main():
    """
    Main E2E test function
    """
    print(f"\n{BLUE}╔══════════════════════════════════════════════════════════════╗{RESET}")
    print(f"{BLUE}║  🧪 E2E Test: Captcha Endpoint Comparison                   ║{RESET}")
    print(f"{BLUE}╚══════════════════════════════════════════════════════════════╝{RESET}")

    print(f"\n{YELLOW}Testing endpoint:{RESET} GET /app/rest/v2/services/captcha/getNumericCaptcha")
    print(f"{YELLOW}Old-hemis:{RESET} {OLD_HEMIS_URL}")
    print(f"{YELLOW}New-hemis:{RESET} {NEW_HEMIS_URL}")

    # Fetch old-hemis
    print(f"\n{YELLOW}[1/3] Fetching old-hemis...{RESET}")
    old_response, old_status = fetch_endpoint(OLD_HEMIS_URL, OLD_HEMIS_TOKEN)
    print_response_summary("Old-hemis Response", old_response, old_status)

    # Fetch new-hemis
    print(f"\n{YELLOW}[2/3] Fetching new-hemis...{RESET}")
    new_response, new_status = fetch_endpoint(NEW_HEMIS_URL, None)  # PUBLIC endpoint
    print_response_summary("New-hemis Response", new_response, new_status)

    # Compare
    print(f"\n{YELLOW}[3/3] Comparing responses...{RESET}")
    is_identical, diff_msg = compare_responses(old_response, new_response)

    print(f"\n{BLUE}{'='*70}{RESET}")
    if is_identical:
        print(f"{GREEN}✅ TEST PASSED: {diff_msg}{RESET}")
        print(f"{GREEN}Response format: {list(old_response.keys())}{RESET}")
        print(f"{BLUE}{'='*70}{RESET}\n")
        return 0
    else:
        print(f"{RED}❌ TEST FAILED: {diff_msg}{RESET}")
        print(f"{BLUE}{'='*70}{RESET}\n")
        return 1

if __name__ == '__main__':
    sys.exit(main())
