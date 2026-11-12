"""Beginner test recipes for the exact app tools; no operations are executed."""
RECIPES={}
def add(key,need,why,steps,expected,extra=''):
 RECIPES[key]=dict(need=need,why=why,steps=steps,expected=expected,extra=extra)
add('files','Test pack: small/ folder; no USB needed.','Checks that copying, renaming and deleting affect only the chosen test files.',[
'Open Android Files. Extract USB-Test-Files.zip in Downloads. Keep the original extracted folder unchanged; use copies for editing.',
'In USB Workbench, open Home → File manager. Choose the small folder inside USB-Test-Files and allow access.',
'Open hello.txt. It must say: USB Workbench manual test. Go back and copy it to a new folder called TestOutput.',
'Rename ONLY the copied file to renamed.txt. Open it again; the words must be unchanged.',
'Delete ONLY renamed.txt, read the confirmation and confirm. Return to the original small folder: hello.txt must still exist.'],
'Copied file opens correctly; rename works; delete removes only the copy; the original stays unchanged.')
add('hash','small/hello.txt and HASHES.txt from the test pack.','A checksum is a file fingerprint. Matching it catches missing or changed bytes.',[
'Open HASHES.txt in Android Files. Find the line for small/hello.txt; the 64 characters before the filename are its SHA-256.',
'Open Home → SHA-256 checksum. Choose small/hello.txt and start.',
'Open View details. Compare the entire SHA-256 with HASHES.txt, not just the first few characters.',
'Repeat with small/empty.txt. An empty file is valid and must also give the listed fingerprint.'],
'Both complete fingerprints exactly match HASHES.txt. No file is changed.')
add('checksums','small/hello.txt and HASHES.txt.','Checks that a wrong fingerprint is visibly rejected instead of shown as success.',[
'Open Home → Checksums & compare. Choose small/hello.txt.',
'Paste the SHA-256 from HASHES.txt into the expected SHA-256 field and start.',
'Record the MATCH result. Repeat after changing one character in the pasted fingerprint.',
'Try a short value such as abc instead of 64 characters.'],
'Correct value says MATCH; changed value says MISMATCH; abc is rejected clearly. A mismatch never looks successful.')
add('zip','samples.zip from the pack.','Checks that archive contents can be listed without extracting or losing names.',[
'Open Home → Inspect ZIP. Choose samples.zip and start listing.',
'Look for hello.txt, عربي.txt, empty.txt and nested/inside.txt.',
'Check the Arabic name is readable. Open the TestOutput folder in Android Files: listing alone must not have created extracted copies.'],
'All four paths are listed; the empty file is allowed; listing writes no extracted files.')
add('extract','samples.zip; an empty TestOutput folder in internal storage.','Checks whether the files saved from a ZIP really contain the original data.',[
'Open Home → Extract ZIP. Choose samples.zip and choose TestOutput as the destination.',
'Start extraction and wait for completion. Open TestOutput/samples, or samples (2) if samples already existed. The folder should use the archive name, not a random Extracted-number name.',
'Open hello.txt, عربي.txt and nested/inside.txt; compare them with the small folder in the pack.',
'Use SHA-256 checksum on each extracted file and compare the matching small/ entry in HASHES.txt. Repeat extraction once; it must not overwrite the first output.'],
'All files and the empty file are present, text and fingerprints match, and existing output is preserved.')
add('createzip','Select the four individual files inside small/ (including nested/inside.txt).','Checks that a newly created ZIP can be read by something other than this app.',[
'Open Home → Create ZIP archive. Tap Add files and choose two test files. Tap Add files AGAIN and choose a third. Check the list; remove one using its Remove button. Choose an empty output folder.',
'Tap Create ZIP from selected files and wait for the inline result. Open the new ZIP with Android Files or a computer archive program.',
'Extract into a different folder. Open the text files and compare their SHA-256 fingerprints with the original files.',
'Repeat with an Arabic filename and an empty file included.'],
'The independent archive program opens the ZIP, and extracted file bytes match the originals. Note how selected file paths are stored.')
add('tar','samples.tar.gz and samples.tar.xz; TestOutput folder.','Checks both archive reading and decompression, which can fail differently.',[
'Open Home → TAR / GZIP / XZ archives. Select samples.tar.gz.',
'Choose TestOutput. List the paths, then extract into a new output folder.',
'Compare extracted hello.txt and nested/inside.txt with the small originals using SHA-256 checksum.',
'Repeat the same steps with samples.tar.xz.'],
'Both archives list and extract correctly; all original file fingerprints match.')
add('archives','samples.zip, samples.tar.gz, password.zip; password is fixture-only.','Checks the advanced archive engine and wrong-password handling.',[
'Open Home → 7z / RAR & archive workshop. Choose samples.zip and tap Validate & list.',
'Choose TestOutput, tap Extract all & verify new files, then compare the saved files with small/ using their fingerprints.',
'Choose password.zip. Enter wrong as the password and try extraction. It must fail without claiming success.',
'Choose password.zip again. Enter fixture-only and extract. Read owned.txt: it must contain Owned encrypted test data.',
'Reopen the tool and check that the password field is empty. Inspect Activity/details: the password must not appear there.'],
'Normal archives work; wrong password fails; correct password extracts matching data; password is not saved.',
'Also test your own ordinary single-volume 7z and RAR files using copies. AES ZIP, encrypted 7z/RAR and multipart encryption are unsupported: refusal is expected, not a failed supported feature.')
add('split','raw-8MiB.img from the small pack; a fresh output folder.','A typed 3 MiB size must create multiple correctly sized parts.',[
'Open Split large file. Choose raw-8MiB.img. Type 3 in the custom Part size (MiB) field.',
'Choose a NEW empty output folder and tap Start. Progress and completion should remain on the tool page.',
'Open the output in Android Files. Expect part-00000.uwp, part-00001.uwp and part-00002.uwp. Keep all three; these generic parts do not have a separate manifest.'],
'Three parts are created from 8 MiB using 3 MiB parts; the source is unchanged. Continue with Rejoin below.')
add('join','The three .uwp files from the custom-size split above.','Rejoining proves the typed part size did not lose data.',[
'Open Rejoin split parts. Select ALL three .uwp files from the same run and a NEW output folder.',
'Tap Start and wait. Find the joined .img file in the selected folder.',
'Use SHA-256 checksum on it and compare with raw-8MiB.img in HASHES.txt.',
'Repeat with one part missing; it must refuse instead of claiming success.'],
'Complete set matches the original fingerprint; incomplete set is rejected.')
add('compressedImage','raw-8MiB.img, raw-8MiB.img.gz and raw-8MiB.img.xz.','Checks that compression and decompression preserve every byte.',[
'Open Home → GZIP / XZ image tools. Choose raw-8MiB.img and the GZIP compression direction; select TestOutput and run.',
'Choose the new .gz output and the GZIP unpack direction; unpack into a DIFFERENT empty folder.',
'Compare the unpacked file fingerprint with raw-8MiB.img in HASHES.txt.',
'Choose the supplied raw-8MiB.img.xz, select XZ unpack, run and compare with the same raw fingerprint.'],
'Both restored files exactly match raw-8MiB.img. These are harmless data files, not operating systems.')
for key,label in [('cso','CSO'),('zso','ZSO')]:
 add(key,'raw-8MiB.img in USB-Test-Files; Android Files and two NEW output folders.',
 'You are testing a two-way file conversion, not burning a USB or booting a console.',[
 'In Android Files create TestOutput/'+label+'-packed and TestOutput/'+label+'-restored. Keep the original raw-8MiB.img unchanged.',
 'Open '+('ISO ↔ CSO conversion' if key=='cso' else 'ISO ↔ ZSO conversion')+'. Tap Choose beside Source file and select raw-8MiB.img.',
 'Set Conversion direction to ISO → '+label+' v1. The sample ends in .img, but it is aligned raw data accepted for this byte test. Type 0.5 as the maximum size in GiB.',
 'Choose '+label+'-packed as destination. Tap Convert & verify new file. Wait for the inline completion and details.',
 'In Android Files open '+label+'-packed, then its new Converted-... folder. The output is named image.'+label.lower()+'.',
 'Return to this same conversion tool. Choose that image.'+label.lower()+' as Source file. Change direction to '+label+' v1 → ISO. Choose '+label+'-restored as destination and run.',
 'Open '+label+'-restored and its Converted-... folder: the restored file is image.iso. Run SHA-256 checksum on image.iso; compare with raw-8MiB.img in HASHES.txt.'],
 'Restored image.iso has the EXACT same fingerprint as raw-8MiB.img. Different extensions are normal; matching bytes prove the round trip.',
 'No real operating system/game is required for this test. Actual loader compatibility needs a separate real supported disc and loader test. If stuck, write the step number and exact text shown.')
add('chd','raw-8MiB.img and samples/data.chd; empty output folders.','Checks both new CHD creation and decoding with a known original.',[
'Open Home → CHD images. Choose raw-8MiB.img and Create stored CHD · 512-byte data units.',
'Choose TestOutput and tap Run selected CHD operation. Wait for verification.',
'Choose the new CHD, select Decode CHD to a raw image and a different output folder; run.',
'Compare decoded image.img with raw-8MiB.img using SHA-256. Repeat creation with 2048-byte units.',
'Decode samples/data.chd too; compare its output with samples/chd-expected.img.'],
'All decoded fingerprints match. Creation makes an uncompressed CHD, so a larger output is normal.',
'A computer tester should also run chdman verify -i <output.chd> and extractraw, then compare the extracted bytes. CD/audio/mixed-track creation is unsupported.')
add('dmg','samples/data.dmg and samples/dmg-expected.img.','Checks the DMG decoder against known bytes instead of assuming a successful message means correct data.',[
'Open Home → DMG → raw image. Choose samples/data.dmg, select TestOutput and run.',
'Use SHA-256 checksum on the saved raw image and compare it with samples/dmg-expected.img in HASHES.txt.',
'Also try a copy of a real supported unencrypted DMG you own; ask a Mac tester to compare its raw output independently.'],
'Synthetic output fingerprint matches. Unsupported encrypted/segmented variants fail clearly; conversion does not promise Mac boot.')
add('optical','samples/files.iso; TestOutput folder.','Checks ISO folder browsing and file extraction without needing a Windows/Linux download.',[
'Open Home → Browse & extract ISO / UDF. Select samples/files.iso.',
'Choose TestOutput, list the image, and find HELLO.TXT.',
'Extract the files. Open HELLO.TXT; it must contain USB Workbench manual test.',
'Compare its fingerprint with small/hello.txt in HASHES.txt. The test ISO contains text only and must not be used as a boot test.'],
'ISO paths are listed and extracted file bytes match. Test a real UDF disc image separately if you claim that compatibility.')
add('imageinfo','samples/files.iso and samples/fat16.img.','Checks whether image detection gives useful information without claiming bootability.',[
'Open Home → Image inspector. Choose samples/files.iso and tap Start.',
'View details: an ISO9660 signature should be reported. It must not claim that the text-only ISO can boot.',
'Repeat with samples/fat16.img and then small/hello.txt. Record the reported size/type and compare with the selected file.',
'Check the original files still open normally.'],
'File sizes and recognized signatures are sensible; plain text is not invented into a bootable image.')
add('fatImage','samples/fat16.img; TestOutput folder.','Checks image offsets, nested folders, fragmented file data and Arabic names.',[
'Open Home → Browse FAT16 / FAT32 images. Select samples/fat16.img.',
'Set Partition number = 0 because this sample is a raw filesystem without a partition table. Tap List image files.',
'Find ملف عربي.txt, DIR/INNER.TXT and EMPTY.TXT. Tap Extract into new folder & verify.',
'Compare the extracted files against expected-fat/ in the pack, using SHA-256 checksum.',
'If you test a full USB backup image later, use its real partition number, normally 1, instead of 0.'],
'Arabic/nested/empty files are listed and every extracted fingerprint matches expected-fat/.')
add('wimExplore','samples/files.wim; TestOutput folder.','Checks that the selected WIM image number and exact file path are respected.',[
'Open Home → Browse & export WIM files. Select samples/files.wim and WIM image index = 1. Leave the file-path field alone until listing is complete.',
'Tap List WIM paths. Find /folder/hello.txt; tap the /folder/hello.txt button below the listing to fill the path automatically. If typing instead, replace the WHOLE field; use exactly /folder/hello.txt, never / /folder/hello.txt.',
'Choose TestOutput and tap Export the selected file. Open the result and compare its fingerprint with small/hello.txt.',
'Try image index 2 (the sample has only image 1), then a nonexistent path.'],
'Index 1 and the valid path export matching text; missing image/path fail clearly without replacing existing files.')
add('wimTree','samples/files.wim; TestOutput folder.','Checks exporting multiple nested files while leaving files outside the chosen folder out.',[
'Open Home → WIM folder export. Choose samples/files.wim and image index 1.',
'First enter / as Exact folder path and tap List folder paths: it should list the whole image without WIM_TREE_FAILED. Then replace the whole field with /folder and list again; keep these results on the tool page.',
'Choose TestOutput and tap Export folder & verify. Open the new folder.',
'Check hello.txt and عربي.txt and the nested file against small/ fingerprints. outside.txt must not appear inside the exported subtree.',
'Repeat with /missing as the path.'],
'Only the selected subtree is exported, all file bytes match, and a missing folder is rejected.')
add('wimSplit','samples/files.wim for a first check; a WIM larger than 4 GiB for the real large-file test.','Checks the installer path that must split install.wim for FAT32.',[
'Open Home → Split WIM for FAT32. Choose samples/files.wim and an empty output folder. Tap Split & verify WIM.',
'A tiny WIM may produce only one SWM: this checks opening/output but does NOT prove multi-part splitting.',
'For the required large-file case, obtain your own genuine Windows ISO and extract sources/install.wim using the ISO browser. It must exceed 4 GiB.',
'Split that WIM. Keep install.swm, install2.swm and every later part together.',
'On a spare FAT32 installer USB, place the complete set in sources/ and start the installer on a real UEFI PC. Ask a PC tester to verify/apply the SWM set with wimlib or DISM too.'],
'Small sample succeeds; large case produces multiple valid parts and the real installer can read them.',
'If you have no large WIM or PC, mark Not sure or Blocked and say what is missing. Do not mark the large-file case Worked from the tiny sample.')
add('diagnostic','No spare USB required for the first run.','Checks that the app reports its version and capabilities honestly.',[
'Open Home → Compatibility check and tap Start.',
'Open View details. App version must be 0.16.0 for the current installed build. Android version should match Android Settings → About tablet/phone.',
'Run once with no USB connected and again with a spare USB connected. Record the USB host and engine availability values.',
'If any supported engine is reported missing, mark Did not work and copy the exact line into notes.'],
'Report opens without crashing, shows the correct version and does not invent a connected drive.')
add('report','First complete one checksum test and one deliberately wrong-fingerprint comparison.','Checks that a useful support report preserves actual results and errors.',[
'Open Activity and confirm your successful and failed comparison entries are visible.',
'Open Home → Export workshop report. Choose an empty internal TestOutput folder and tap Start.',
'Open the saved text file in Android Files. Find the app version, checksum success and mismatch result.',
'Repeat after changing app language. Check that technical values and fingerprints have not changed.'],
'The exported text opens, accurately includes success/failure details, and is not uploaded automatically.')
add('planner','No USB or files needed.','Checks that advice opens the right tool without accidentally starting an operation.',[
'Open Home → Help me choose. Read each goal card and tap Open this tool.',
'For making a USB installer, check it opens the named writing/preparation tool. For backup/format goals, check it opens the corresponding tool.',
'Go back and repeat the other cards in English, then Arabic.',
'Without confirming any destructive warning, inspect Activity: browsing advice must not have started a job.'],
'Every card opens its intended tool, text is readable, and no operation starts just from reading advice.')
add('format','An EMPTY disposable USB, preferably 1–32 GiB; a computer is needed to verify several formats.','A format-success message alone does not prove another device can use the drive.',[
'Physically label the spare USB TEST ONLY. In Home tap Select a USB and select that exact drive by model/capacity.',
'Open Format USB. Start with FAT32, MBR, label USBTEST and Automatic allocation. Read the warning and Confirm only for the spare.',
'Wait for completion, safely reconnect, then use Android Files or a compatible PC to copy small/hello.txt and عربي.txt onto the USB.',
'Safely eject and reconnect again. Read the files back and compare their fingerprints with HASHES.txt.',
'Repeat using every enabled filesystem and each supported MBR/GPT option. Use the separate Format matrix instructions below to see which computer OS is needed.',
'APFS must force GPT. ReFS and LUKS must stay unavailable. Do not accept Android asking to format a Linux/Mac format during these checks.'],
'Each claimed format is recognized by its intended OS and retains matching files after reconnection; unsupported options cannot run.',
'FAT12 needs a 2–32 MiB device and FAT16 8 MiB–2 GiB. Do not use a large ordinary USB to pretend these size-specific checks passed. Keep untested formats experimental or disabled until validated.')
add('info','A spare USB with known copied test files; no writes by this tool.','Checks wrong-drive mistakes, drive sizes and partition information.',[
'Connect only the spare USB, select it in Home, then open Full USB analyzer and run it.',
'Compare model/capacity with the physical drive and the computer/Android Files. Note GB versus GiB display differences rather than treating rounded units as a fault.',
'After formatting the spare with MBR or GPT, run the analyzer again. It should show that table and the expected partition.',
'For FAT/exFAT/NTFS/ext, compare available-space information with a compatible computer. For the newer formats, an honest unavailable-space message is expected.'],
'Correct drive/table/partition are reported, files remain unchanged, and unknown free space is not presented as zero.')
add('benchmark','Spare USB; no erasing required.','Checks that a simple read job completes and reports a plausible nonzero speed.',[
'Select the spare USB in Home. Open Read benchmark and start.',
'Keep the cable connected until completion. Record speed, drive model, cable and adapter in notes.',
'Repeat once. Speeds may differ because of caching; the tool must not crash or claim a speed when reading failed.',
'Open the USB test text files afterward to check they are unchanged.'],
'Both runs complete with readable speed/time results; no files change. A speed result is not a drive-health certificate.')
add('randomread','Spare USB containing the small test files.','Checks scattered reads, a different workload from sequential reading.',[
'Select the spare USB. Open Random read benchmark and start.',
'Wait for completion and record latency/speed. Repeat using the same cable and then another known-good adapter if available.',
'Read hello.txt from the USB afterward and compare its fingerprint with its original.'],
'It completes without a crash, reports readable results and leaves the file bytes unchanged.')
add('scan','Disposable USB and stable power; can take minutes or hours.','Checks whether the whole addressable drive can be read, not just its beginning.',[
'Select the spare USB. Open Read surface scan and start.',
'Allow one complete scan. Record total size, time and any read-error position exactly.',
'On a second run tap Cancel partway through. Wait for the cancelled result before opening another tool.',
'Check the test files still match their original fingerprints.'],
'Full run reaches the end or reports a real read failure; cancelled run is labelled cancelled, never complete. No files are written.')
add('hex','Spare USB; a known MBR-formatted spare makes the result easier to recognize.','Checks that the sector viewer reads raw bytes without offering dangerous accidental edits.',[
'Select the spare USB. Open Sector viewer and run it.',
'Check there are addresses, hexadecimal bytes and a readable preview, without broken layout.',
'For an MBR-formatted spare, the final two bytes of its first 512-byte sector should be 55 AA. A PC tester can compare the first sector independently.',
'Check there is no arbitrary write/edit action in this read-only viewer.'],
'Readable sector data appears and no drive bytes are changed. A signature alone is not proof the disk is healthy.')
add('fileBenchmark','Internal TestOutput folder with at least 64 MiB free; later repeat on a spare USB folder.','Checks actual temporary-file writes, verification and cleanup.',[
'Open Home → File write & read benchmark. Choose an EMPTY TestOutput folder.',
'Select the smallest offered size, 16 MiB. Tap Write, verify & remove test file.',
'Wait for completion. Open the folder in Android Files; the temporary benchmark file should be gone.',
'Repeat in an empty folder on the spare USB. Original files outside the chosen folder must remain unchanged.'],
'Both read/write measurements and verification succeed; only the newly created temporary file is removed.')
add('backup','Spare USB; INTERNAL free space greater than the USB’s entire capacity.','Checks a full-device backup that can actually be restored.',[
'Copy the small test files onto the spare USB. Save their fingerprints. Select that USB in Home.',
'Open Backup & restore and tap Create full backup. Save the IMG in internal TestOutput, never on the same source USB.',
'Wait for completion. The IMG size should equal the entire USB capacity, not just the space occupied by your files.',
'Use Verify an image with the saved IMG and the unchanged source USB; require a full match.',
'For a restore test, use Write an image to restore onto a SECOND empty spare of equal or greater size; reconnect and compare the restored test files.'],
'Backup matches the source byte for byte; restoring onto a second spare gives readable matching files.',
'A large USB needs equally large phone free space. Use a small spare for a faster first test; that does not replace later large-device testing.')
add('clone','Two disposable USBs and an adapter/hub that exposes both, or internal space for USB-to-phone clone.','Checks that source and destination are not mixed up.',[
'Label the USBs SOURCE and DESTINATION. Put known test text on SOURCE; DESTINATION must be empty and at least as large.',
'Open Clone USB. Choose SOURCE in the source selector and DESTINATION in the second selector. Read the review and confirm only after checking both names.',
'Wait for cloning and verification. Reconnect DESTINATION alone and open the files; compare fingerprints.',
'Also test the USB-to-phone image action if shown; verify that image against SOURCE using Verify an image.',
'Try choosing the same USB for both sides; it must refuse.'],
'DESTINATION matches SOURCE and SOURCE is unchanged. Same-device selection is rejected.')
add('write','Disposable USB; samples/fat16-mbr.img from the pack. A real boot test additionally needs your own supported boot image and PC.','Checks actual writing, target selection and read-back comparison.',[
'Keep samples/fat16-mbr.img in INTERNAL Downloads. Select the TEST ONLY USB in Home.',
'Open Write an image. Choose Write image directly to USB, then select fat16-mbr.img.',
'Read the drive identity and erase warning; Confirm only for the spare. Wait for BOTH writing and verification.',
'Reconnect safely. The USB should show the sample FAT16 files; compare DIR/INNER.TXT and the Arabic file with expected-fat/.',
'Run Verify an image using fat16-mbr.img and this same USB; it must match the source-length range.',
'Booting is separate: write a genuine supported hybrid Linux ISO you obtained yourself, then start it from a real PC boot menu. Do not use the text-only sample ISO as a boot test.'],
'Sample write and verification succeed and files match. Real PC boot must be observed before claiming boot compatibility.',
'For the direct Linux ISO + custom persistence mode: use a supported MBR hybrid ISO, choose its required label and size, write, select its persistence boot option, create a file on the PC, reboot and check the file remains. Unsupported ISO layout must be refused before writing.')
add('verify','The USB just written from samples/fat16-mbr.img; keep the original sample file.','Checks that the app detects mismatches rather than merely showing completion.',[
'Open Verify an image. Choose samples/fat16-mbr.img and the USB written from that exact file. Start comparison.',
'Require the explicit match/completed verification result.',
'For a safe mismatch test, choose raw-8MiB.img as the source while keeping that FAT16 USB selected. Run comparison; these data must differ.',
'Do not write again just to clear a mismatch. Save the exact mismatch details in notes.'],
'The matching source passes; the different source reports a mismatch. Verification does not modify the USB.')
add('wipe','Disposable USB with no needed data; this ERASES the complete drive.','Checks full zero writing and prevents a false promise of secure flash sanitization.',[
'Select the TEST ONLY USB. Open Zero-fill drive, read the warning, Confirm and wait until it completes.',
'Open Verify zero-filled USB and run a complete verification on the same USB.',
'Do NOT format the drive between zero-fill and zero verification: formatting would correctly introduce nonzero metadata.',
'After the verification test, format the spare before normal file use.'],
'Zero verification reports no nonzero bytes after a completed zero-fill. The app does not call this certified secure flash erasure.')
add('verifyZero','The completely zero-filled spare from the previous test.','Checks the difference between an actually zeroed disk and an ordinary formatted disk.',[
'Run Verify zero-filled USB immediately after Zero-fill drive; expect a pass.',
'Now format the spare as FAT32, then run Verify zero-filled USB again.',
'The formatted drive MUST report nonzero data; record its offset. This is expected, not a reason to report the tool broken.'],
'Zero-filled disk passes; formatted disk fails the zero check clearly; this read-only test changes nothing.')
add('capacity','EMPTY disposable USB; stable power and time for repeated full-drive erasure.','Checks whether claimed capacity can hold distinct data and return it correctly.',[
'Select the TEST ONLY USB. Open Full capacity test. Choose the one-pass profile, read the erase warning and Confirm.',
'Wait for the entire write phase AND entire read phase. Record reported size, elapsed time and result.',
'Repeat on the same disposable USB using the three-pass profile. It must complete three writes and three reads with coherent progress.',
'After completion, format the drive before normal use. Do not expect files to survive this test.',
'If a run fails, save the exact error and stop; do not immediately repeat on personal data. A genuine fake-capacity drive or developer fault simulator is needed to prove fake-capacity detection.'],
'Known-good spare completes all requested passes, or reports a real error without claiming success. A short benchmark cannot replace this test.')
add('readMap','Spare USB; no writes. Fault/resume validation needs a technician’s failing spare or fault simulator.','Checks that a saved scan can be shown/resumed without starting on a different drive.',[
'Select the spare USB. Open Read-error map & resume and tap New read-only map.',
'Let it finish; tap Show saved map only. It should show the recorded drive and completed ranges without starting a new scan.',
'On a separate run cancel or wait for a genuine read failure. Reconnect the SAME unchanged drive and review Resume saved map.',
'Select a different spare and try Resume. The previous drive’s map must not silently resume on it.',
'For simulated/read-failure cases, ask the developer; do not damage a drive to create faults.'],
'Saved map identifies the right drive, completed work is coherent, changed drive is refused and no physical repair is claimed.')
add('convertTable','Disposable USB freshly formatted MBR/FAT32 with known test files; backup space on internal storage.','Checks whether changing MBR/GPT preserves file data and partition boundaries.',[
'Format the spare MBR/FAT32. Copy small test files onto it and record fingerprints.',
'Open Convert MBR / GPT, choose GPT and tap Review & save table backup. Save this small backup in INTERNAL storage.',
'Read the selected-drive warning and Confirm. Wait; run Full USB analyzer to check GPT is reported.',
'Reconnect on a compatible PC, open the files and compare fingerprints. Repeat conversion back to MBR and recheck.',
'If the app rejects an unsupported layout, record the refusal; do not force conversion with another tool just to make this test pass.'],
'Supported conversion changes the table type without changing file bytes. The saved table backup is not a full file backup.')
add('partitionMeta','Disposable GPT USB, then a disposable MBR USB; known test files and internal backups.','Checks that editing small partition metadata does not corrupt file contents.',[
'Open Full USB analyzer and record partition number and table type.',
'For GPT: open Partition names & active flag, enter the correct partition number and name USBTEST. Tap Review GPT name change and Confirm for the spare.',
'Analyze again: the partition name should change. Reconnect and compare the original test files by fingerprint.',
'For MBR: repeat using Review MBR active flag. Ask a PC tester to confirm which partition is active. The active flag does not install a bootloader.',
'Try an invalid partition number on the spare; it must refuse without changing data.'],
'Only the intended metadata changes; files match, GPT checks remain valid, invalid selection is refused.')
add('filesystemCheck','Healthy FAT32/exFAT/NTFS/ext/F2FS spares; a technician must prepare deliberately damaged test media for repair.','A checker must be read-only, while repair must warn that it writes.',[
'Create a backup IMG of a healthy spare first. Select that USB, open Filesystem check & limited repair, set the real partition number (usually 1), and tap Review read-only check.',
'Confirm the read-only review, wait, then verify the backup IMG against the USB. It should still match because checking must not write.',
'Check that the repair action has a separate write warning; cancel that review and verify the disk still matches.',
'For actual repair, ask a technician to prepare a disposable damaged copy, run the limited repair, then check it again on a computer and compare recovered test files.',
'Do not deliberately unplug or edit your personal drive to manufacture corruption. NTFS repair is limited and cannot replace Windows chkdsk.'],
'Healthy check succeeds without writes, cancelling repair changes nothing, and any claimed repair has independent computer evidence.')
add('fsUsb','Spare NTFS or ext2/3/4 USB prepared on a compatible PC, containing copies of small/; internal output folder.','Checks direct filesystem access when Android Files cannot mount the drive.',[
'On a compatible PC format a spare NTFS or ext4, copy the small test files and safely eject it. Keep their original fingerprints.',
'Connect it to Android; decline any Android prompt to format it. Select it in USB Workbench.',
'Open Browse NTFS / ext USB files, choose partition 1 for a normal single-partition drive, and tap List filesystem paths.',
'Choose TestOutput in INTERNAL storage. Tap Extract regular files & verify and compare exported fingerprints.',
'Reconnect on the PC and verify the source files are unchanged. Repeat for the other filesystem you claim.'],
'Correct files are listed/exported without mounting support or source modifications. Unsupported special/encrypted paths are omitted honestly.')
add('fsImage','A full IMG backup of the known-file NTFS/ext USB from the previous test.','Checks reading an image file independently of direct USB access.',[
'Back up the NTFS/ext test USB using Backup & restore; save the IMG internally.',
'Open NTFS / ext image browser, choose that IMG and partition 1 (0 only for an image with no partition table).',
'List filesystem paths, then choose TestOutput and extract regular files.',
'Compare every exported test-file fingerprint with small/ and check the original IMG fingerprint is unchanged.'],
'Expected regular files are listed and exported with matching bytes; source IMG is not modified.')
add('persistence','At least 512 MiB internal free space for first check; actual persistence needs your own compatible Linux ISO, Ventoy USB and PC.','An ext4 file existing is not proof that Linux saves changes across reboots.',[
'Open Create Linux persistence file. Choose the smallest size, 256 MiB, and the preset required by your Linux distribution; use its documented label.',
'Choose TestOutput and create the data file. Wait for verification and note the output names.',
'For the real test copy the created data file to the intended Ventoy data partition alongside your own compatible Linux ISO.',
'Use the generated configuration template as a reference and carefully merge the exact ISO/data paths into your existing Ventoy configuration; do not replace another configuration blindly.',
'On the PC choose the persistence boot option, create a recognizable file, shut down, boot the same option again and look for that file.'],
'Data-file creation verifies correctly AND the real Linux reboot preserves the test file before persistence is advertised.',
'If you only created a data file and did not boot/reboot, mark Not sure and write “file creation passed; PC persistence not tested”.')
add('windows','Your own genuine Windows ISO, a compatible UEFI PC and an EMPTY spare USB independently confirmed FAT32.','Only a real PC can prove the prepared installer boots and reads split install files.',[
'Prepare the empty spare FAT32 USB on a computer. Do not rely on Android guessing its filesystem.',
'Open Windows UEFI preparation. Choose your own Windows ISO and select the ROOT of that empty USB in the destination picker.',
'Tap Review Windows preparation, check target/architecture and start. Keep power connected until all verification finishes.',
'Safely eject, connect to the PC and select the USB in the UEFI boot menu.',
'Check that Windows Setup reaches the language/edition/disk-selection screens. STOP before installing or deleting partitions on the PC.',
'Also test an ISO containing install.wim larger than 4 GiB; its complete install.swm/install2.swm set must be readable by Setup.'],
'Preparation verifies and actual UEFI Setup starts and reads its image files. This does not test BIOS boot or Windows To Go.')
for key,gpt,update in [('ventoyInstall',False,False),('ventoyUpdate',False,True),('ventoyGpt',True,False)]:
 table='GPT' if gpt else 'MBR'
 steps=['Use your own official Ventoy package and a disposable USB. Back up test files first; initial installation ERASES the USB.',
 'Open '+('Ventoy GPT · experimental' if gpt else 'Update Ventoy MBR' if update else 'Install Ventoy · experimental')+'. Import the official package and select the spare by name/capacity.',
 ('For an update, first use an existing default '+table+' Ventoy spare. Record its ISO filenames and fingerprints. Tap the compatible update action; do not choose initial installation.' if update else 'Tap the initial '+table+' installation action, review the erase warning and Confirm only for the spare.'),
 'Wait for verification, reconnect safely, then add your own supported ISO to the LARGE data partition.',
 'Boot the spare on a real PC, select that ISO in the Ventoy menu and confirm the selected system starts. Do not install it on the PC.',
 'For the update case, compare every preexisting ISO fingerprint and confirm the same ISOs still boot after updating.']
 if gpt:steps+=['Also test the separate compatible GPT update action on this backed-up default GPT layout. Record install and update outcomes separately in notes.']
 add(key,'Official Ventoy package you provide, disposable USB and real PC.', 'Checks '+table+' '+('updates preserve an existing library.' if update else 'installation and actual boot behavior.'),steps,
 'The expected '+table+' layout is created/recognized, the selected ISO boots, and updating leaves preexisting ISO bytes unchanged.',
 'A successful package import is not a passed USB installation or boot test. Custom layouts may be refused. Record UEFI/BIOS/Secure Boot modes actually tested.')
add('ventoy','A working Ventoy spare from the previous tests; your own real ISO; PC.','Checks file copying into the correct Ventoy partition, separately from installing Ventoy.',[
'Open Multi-ISO library. In Android’s folder picker select the LARGE Ventoy data partition, not the small EFI partition.',
'Add a copy of your own ISO and wait for copying to finish. Compare its SHA-256 on the USB with its original.',
'Reconnect the USB on the PC, start Ventoy and choose that ISO.',
'Check the chosen system actually starts. Repeat with another supported ISO if you advertise a multi-image library.'],
'Copied ISO bytes match and both chosen images appear/boot in the actual Ventoy menu.')
add('consoleParts','raw-8MiB.img from the small pack; 128 MiB free. Real compatibility additionally needs your owned disc image and PS3/manager.','Checks raw part naming and reassembly; a console is needed to verify manager compatibility.',[
'Open PS3 raw split files & join. Choose raw-8MiB.img. Type 3 MiB as part size so the test produces three parts; set Original/output filename to test.iso.',
'Choose ISO.#, an output name ending .iso and TestOutput. Tap Split & verify new parts. Keep every part AND its generated manifest.',
'Tap Select parts + manifest to join, choose the complete matching set and a different output folder.',
'Compare the joined fingerprint with raw-8MiB.img in HASHES.txt. Record the result, then remove only this run’s new test parts/joined copy to free space; keep the original. Repeat with .666NN naming.',
'Try omitting one part; it must refuse. For real compatibility, repeat with your own permitted disc image and test the exact PS3 manager you claim.'],
'Both layouts round-trip exactly; missing parts fail clearly. Console operation is recorded separately from file correctness.')
add('usbExtreme','Your own legally usable PS2 ISO; a compatible PS2/OPL setup for real loader testing.','Checks the game identifier, catalog and parts that the real loader needs.',[
'Open PS2 USBExtreme workshop. Select your own valid PS2 ISO, set a short ASCII title and its correct DVD/CD media type.',
'Choose an EMPTY output folder. Tap Create new USBExtreme set and wait for verification.',
'Choose the new ul.cfg and tap Inspect ul.cfg only. Check title, media type and disc ID against your source.',
'Keep ul.cfg together with ALL generated ul.* parts. Copy them to an EMPTY compatible OPL library, never over an existing library catalog.',
'Test it on your exact PS2/OPL version and record whether it appears and starts.',
'Try samples/files.iso as an invalid source: it is only a text ISO and must be refused, not accepted as a PS2 game.'],
'Valid catalog/parts verify, invalid data ISO is refused, and actual OPL behavior is observed before claiming support.')
