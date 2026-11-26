import shutil
import subprocess
import sys
import tempfile
import unittest
import zipfile
from pathlib import Path


class CollectTests(unittest.TestCase):
    def setUp(self):
        temp = tempfile.TemporaryDirectory()
        self.addCleanup(temp.cleanup)
        root = Path(temp.name)
        self.script = root / ".github" / "scripts" / "targets.py"
        self.script.parent.mkdir(parents=True)
        shutil.copyfile(Path(__file__).with_name("targets.py"), self.script)
        (root / "gradle.properties").write_text("mod_version=1.0\nrelease_channel=test\n")
        self.target = "1.21.1-fabric"
        version = root / "versions" / self.target
        libs = version / "build" / "libs"
        libs.mkdir(parents=True)
        (version / "gradle.properties").touch()
        self.jar = libs / "BuildingWands_1.0_test_fabric_1.21.1.jar"
        self.output = root / "build" / "release-jars"

    def make_jar(self, metadata):
        with zipfile.ZipFile(self.jar, "w") as jar:
            jar.writestr(metadata, "{}")
            jar.writestr("net/nicguzzo/wands/WandsMod.class", b"fixture")
            jar.writestr("assets/wands/lang/en_us.json", "{}")

    def collect(self):
        return subprocess.run([sys.executable, str(self.script), "collect", self.target],
                              capture_output=True, text=True)

    def test_collects_production_jar(self):
        self.make_jar("fabric.mod.json")
        result = self.collect()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout.strip(), self.jar.name)
        self.assertEqual((self.output / self.jar.name).read_bytes(), self.jar.read_bytes())

    def test_rejects_wrong_loader_metadata(self):
        self.make_jar("META-INF/neoforge.mods.toml")
        result = self.collect()
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("Invalid production jar", result.stderr)
        self.assertEqual(list(self.output.glob("*.jar")), [])

    def test_reports_missing_production_jar(self):
        result = self.collect()
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("Missing production jar", result.stderr)
        self.assertEqual(list(self.output.glob("*.jar")), [])


if __name__ == "__main__":
    unittest.main()
