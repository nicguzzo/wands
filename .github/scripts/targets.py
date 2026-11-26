"""Discover Stonecutter targets and collect their production jars for CI and releases."""
import argparse
import json
import shutil
import zipfile
from pathlib import Path

root = Path(__file__).resolve().parents[2]
targets = sorted(p.parent.name for p in (root / "versions").glob("*/gradle.properties"))
if not targets:
    raise SystemExit("No build targets found")

parser = argparse.ArgumentParser()
parser.add_argument("action", choices=["list", "describe", "collect"])
parser.add_argument("targets", nargs="*", help="Collect just these targets (default: all)")
args = parser.parse_args()
if args.action == "list":
    print(json.dumps(targets))
elif args.action == "describe":
    for target in targets:
        minecraft, loader = target.rsplit("-", 1)
        print(f"- {minecraft} ({loader})")
else:
    properties = dict(line.split("=", 1) for line in (root / "gradle.properties").read_text().splitlines()
                      if "=" in line and not line.startswith("#"))
    output = root / "build" / "release-jars"
    output.mkdir(parents=True, exist_ok=True)
    for target in args.targets or targets:
        if target not in targets:
            raise SystemExit(f"Unknown target: {target}")
        minecraft, loader = target.rsplit("-", 1)
        name = f"BuildingWands_{properties['mod_version']}_{properties['release_channel']}_{loader}_{minecraft}.jar"
        jar = root / "versions" / target / "build" / "libs" / name
        if not jar.is_file():
            raise SystemExit(f"Missing production jar: {jar}")
        with zipfile.ZipFile(jar) as archive:
            entries = set(archive.namelist())
            metadata = {"fabric": "fabric.mod.json", "neoforge": "META-INF/neoforge.mods.toml",
                        "forge": "META-INF/mods.toml"}[loader]
            required = {metadata, "net/nicguzzo/wands/WandsMod.class", "assets/wands/lang/en_us.json"}
            if required - entries or archive.testzip():
                raise SystemExit(f"Invalid production jar: {jar}")
        shutil.copy2(jar, output / name)
        print(name)
