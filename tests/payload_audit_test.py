"""Packaging policy must reject renamed images as well as ordinary OS filenames."""
import importlib.util
from pathlib import Path
import tempfile
import unittest
import zipfile

spec = importlib.util.spec_from_file_location('audit_payloads', Path(__file__).resolve().parents[1]/'tools/audit_payloads.py')
audit_payloads = importlib.util.module_from_spec(spec)
spec.loader.exec_module(audit_payloads)


class PayloadAuditTest(unittest.TestCase):
    def test_policy(self):
        for name, payload in [('assets/windows.iso', b''), ('assets/drive.img', b''),
                              ('assets/install.wim', b''), ('assets/install2.swm', b''),
                              ('assets/ventoy-1.1.18-linux.tar.gz', b''),
                              ('assets/disguised.bin', b'MSWIM\0\0\0'),
                              ('assets/renamed.bin', bytes(32769)+b'CD001')]:
            with self.subTest(name=name), self.assertRaises(ValueError):
                audit_payloads.check_member(name, payload)

    def test_normal_package_and_budget(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp)/'app-qa.apk'
            with zipfile.ZipFile(path, 'w') as package:
                package.writestr('assets/catalog.js', 'tutorials only')
                package.writestr('assets/native-corresponding-source.zip', b'licensed source')
            self.assertEqual(audit_payloads.audit(path)['status'], 'PASS')
            with path.open('r+b') as stream:
                stream.truncate(audit_payloads.NORMAL_PACKAGE_LIMIT+1)
            with self.assertRaisesRegex(ValueError, 'budget'):
                audit_payloads.audit(path)


if __name__ == '__main__':
    unittest.main()
