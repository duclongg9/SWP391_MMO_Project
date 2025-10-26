#!/usr/bin/env python3
"""Apply Google OAuth credentials to the MMO Trader Market configuration.

This helper accepts the JSON file downloaded from the Google Cloud Console
(`client_secret_*.json`) and updates the Google-related keys in the
`database.properties` files that power Google Sign-In in the application.
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Dict, Iterable, Optional

DEFAULT_TARGETS = [
    Path("MMO_Trader_Market/src/conf/database.properties"),
    Path("MMO_Trader_Market/conf/database.properties"),
    Path("MMO_Trader_Market/build/web/WEB-INF/classes/conf/database.properties"),
]

PROPERTY_KEYS = {
    "google.clientId": "client_id",
    "google.clientSecret": "client_secret",
}


def load_credentials(path: Path) -> Dict[str, Optional[str]]:
    with path.open("r", encoding="utf-8") as fh:
        payload = json.load(fh)

    if "installed" in payload:
        source = payload["installed"]
    elif "web" in payload:
        source = payload["web"]
    else:
        raise SystemExit("Unsupported JSON structure. Expected 'installed' or 'web' root key.")

    result: Dict[str, Optional[str]] = {}
    for prop, json_key in PROPERTY_KEYS.items():
        result[prop] = source.get(json_key)

    redirect = source.get("redirect_uris")
    if isinstance(redirect, list) and redirect:
        result["google.redirectUri"] = redirect[0]
    else:
        result["google.redirectUri"] = source.get("redirect_uri")

    return result


def apply_updates(path: Path, updates: Dict[str, Optional[str]]) -> bool:
    if not path.exists():
        return False

    original = path.read_text(encoding="utf-8").splitlines()
    found_keys = {key: False for key in updates if updates[key]}
    changed = False
    new_lines = []

    for line in original:
        stripped = line.strip()
        if stripped.startswith("#") or "=" not in stripped:
            new_lines.append(line)
            continue

        key, _, value = line.partition("=")
        key = key.strip()
        if key in updates and updates[key]:
            new_value = updates[key]
            new_line = f"{key}={new_value}"
            found_keys[key] = True
            if new_line != line:
                changed = True
                new_lines.append(new_line)
            else:
                new_lines.append(line)
        else:
            new_lines.append(line)

    for key, was_present in found_keys.items():
        if not was_present and updates.get(key):
            new_lines.append(f"{key}={updates[key]}")
            changed = True

    if changed:
        path.write_text("\n".join(new_lines) + "\n", encoding="utf-8")

    return changed


def parse_args(argv: Iterable[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("json", type=Path, help="Path to the Google OAuth client JSON file")
    parser.add_argument(
        "--target",
        "-t",
        dest="targets",
        type=Path,
        action="append",
        default=[],
        help="database.properties file to update (defaults to the tracked configuration files)",
    )
    parser.add_argument(
        "--redirect",
        dest="redirect",
        help="Override the redirect URI instead of using the first value from redirect_uris",
    )
    return parser.parse_args(argv)


def main(argv: Iterable[str]) -> int:
    args = parse_args(argv)
    credentials = load_credentials(args.json)

    if args.redirect:
        credentials["google.redirectUri"] = args.redirect

    targets = args.targets or DEFAULT_TARGETS
    updated_any = False
    for target in targets:
        updated = apply_updates(target, credentials)
        if updated:
            print(f"Updated {target}")
            updated_any = True
        else:
            if target.exists():
                print(f"No changes required for {target}")
            else:
                print(f"Skipped missing file {target}")

    if not updated_any:
        print("No files were modified. Verify that the JSON contains new values.")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
