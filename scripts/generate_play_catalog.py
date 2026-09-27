#!/usr/bin/env python3
import json, os
from pathlib import Path
from google.oauth2 import service_account
from google.auth.transport.requests import AuthorizedSession

ROOT=Path(__file__).resolve().parent.parent
config=json.loads((ROOT/"play_apps.json").read_text(encoding="utf-8"))
raw=os.environ.get("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON")
if not raw:
    print("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON is not set; catalog generation skipped.")
    raise SystemExit(0)

creds=service_account.Credentials.from_service_account_info(
    json.loads(raw),
    scopes=["https://www.googleapis.com/auth/androidpublisher"],
)
session=AuthorizedSession(creds)
base="https://androidpublisher.googleapis.com/androidpublisher/v3"
out=[]

for item in config.get("apps",[]):
    package=item.get("packageName","").strip()
    if not package or package.startswith("PUT_"):
        continue
    row={
        "packageName":package,
        "name":item.get("name",package),
        "description":item.get("description",""),
        "imageUrl":item.get("imageUrl",""),
        "storeUrl":f"https://play.google.com/store/apps/details?id={package}",
        "source":"Google Play"
    }
    try:
        r=session.get(f"{base}/applications/{package}/tracks/production",timeout=30)
        r.raise_for_status()
        releases=r.json().get("releases",[])
        if releases:
            row["version"]=",".join(map(str,releases[0].get("versionCodes",[])))
            row["releaseStatus"]=releases[0].get("status","")
        out.append(row)
    except Exception as e:
        print(f"Could not read {package}: {e}")

(ROOT/"play_catalog.json").write_text(
    json.dumps({"apps":out},ensure_ascii=False,indent=2)+"\n",encoding="utf-8"
)
print(f"Generated {len(out)} Play entries.")
