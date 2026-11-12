"""Build a standalone, offline manual-release checklist. No app bridge or libraries."""
from pathlib import Path
import json,subprocess
from manual_walkthroughs import RECIPES
ROOT=Path(__file__).resolve().parents[1]
subprocess.run(['node','-e',"const fs=require('fs'),vm=require('vm');const c=vm.createContext({});vm.runInContext(fs.readFileSync('ui/catalog.js','utf8')+';globalThis.data={tools:TOOLS,help:TOOL_HELP,roadmap:ROADMAP};',c);fs.writeFileSync('artifacts/manual-catalog.json',JSON.stringify(c.data));"],cwd=ROOT,check=True)
catalog=json.loads((ROOT/'artifacts/manual-catalog.json').read_text());sections=[]
def case(key,title,steps,expected):return dict(id=key,title=title,steps=steps,expected=expected)
def group(key,name,items):sections.append(dict(id=key,name=name,cases=[case(key+'-'+str(i),*x) for i,x in enumerate(items)]))
group('setup','Start here · equipment and evidence',[
('Use disposable test media only','Prepare two spare USB drives, an OTG adapter, a PC, an independent SHA-256 tool, and local/cloud storage providers. Copy every important file elsewhere. Mark the spare drives visibly.','No personal data is used as a destructive target. The tester can identify the USB by capacity and model.'),
('Record the exact candidate','Write APK SHA-256, app version/code, device model, Android/WebView version, USB model/size/sector size, cable, PC OS and test date in the notes. Install the exact signed QA candidate without clearing data.','Every result can be tied to one exact build and environment. Results from earlier releases are not silently reused.'),
('Create known test files','Generate small random and zero-filled files, an empty file, nested folders, Arabic names, long names, a file larger than4GiB, and your own permitted system/test images. Record independent hashes.','Known originals and an untouched backup exist. No OS/game/firmware assets need to be bundled or downloaded by the app.'),
('Preserve checklist results','Set one harmless result and note, reload this file, and check persistence. Export JSON. Open a new copy of this HTML and import JSON. Test Save HTML copy and reopen that copy.','Results and notes survive through the chosen backup method. If browser storage is blocked, a visible notice explains the need for export.'),
('Do not infer universal reliability','Treat every untested or blocked mandatory item as unfinished. Rerun affected tests after a fix. Never intentionally disconnect a drive holding personal data.','A checklist records evidence; it cannot guarantee zero user issues or replace external boot/hardware testing.')])
group('startup','Startup, Home, Learn and saved state',[
('No unsolicited folder-permission banner','Choose a removable/cloud folder, leave the app, revoke its grant or remove the medium, then cold-start Home. Repeat after rotation and process death.','Home opens without scanning the old folder or showing the screenshot\'s unsolicited error. Opening Files explicitly checks access and gives a useful error if permission expired.'),
('Keep real errors visible','Explicitly attempt an operation with a denied source/folder or unplug a disposable target during a test write. Dismiss the banner, then open Activity/details.','The real error remains actionable and accessible in history where applicable. The startup fix does not hide incomplete writes or real failures.'),
('Three navigation destinations','Open Home, Learn, Activity; try Back, rotation and switching languages. Open all eight Home groups and search by English and Arabic names.','There is one Home tools browser, no duplicate Tools tab, correct counts and no automatic operation from browsing.'),
('Wide rows and independent pins','Expand each group on a narrow phone and landscape/tablet. Tap tool text, pin several tools, unpin from Pinned and group rows, search, then restart the app.','Rows use available width. Pin only changes saved favorites, never opens or runs a tool. Pinned states stay synchronized and persist.'),
('Learn search and guides','Search English/Arabic, diacritics, punctuation, no results; clear the search. Open starter links, questions and tutorials; follow links back to tools.','Search is full-width/readable. Starter links are spaced and reachable. Reading never starts a file/USB job.'),
('Language and direction','Cold-start using Arabic and English device locales with/without a saved language. Change language with a completed report and while a progress panel is visible.','English and Arabic, RTL, numbers, filenames, results, confirmations and errors remain readable without mixed-language duplicate prose.'),
('No unwanted repeat banners','Dismiss an old completed result/error; navigate and cold-start again. Start a new real error and inspect it.','Dismissed UI state is respected; new real errors remain visible. An interrupted write is never presented as successful.')])
group('pickers','Custom option pickers · repeat for EVERY dropdown',[
('All option fields use the app picker','Open every tool with a selection field: filesystem/table/cluster, conversion direction, output part size, limits, partition number/mode/persistence presets.','A dark app-owned popup appears; no gray/white Android select popup with unreadable text.'),
('Correct current value and change routing','Open each picker, choose another value, reopen, and inspect the selected marker. Run the operation on an owned fixture and check output.','The actual operation receives the displayed value. Cancel/back never changes the value or starts an operation.'),
('Disabled options and dependent choices','Switch filesystems and inspect cluster options. Try selecting every unavailable filesystem. Change modes that rebuild a tool form.','Unavailable choices cannot run. Automatic-only engines disable cluster selection. Trigger text updates after programmatic changes.'),
('Long lists and search','Use search on a long option list, including Arabic text/no results; clear it; scroll to the final option on a small screen.','No clipped or unreachable options. Search does not change the underlying selection by itself.'),
('Keyboard, TalkBack and Back','With TalkBack and an external keyboard, open a picker, move through choices, use Tab/Shift+Tab, Escape and Android Back.','Label, value, disabled and selected states are announced. Focus stays inside the popup and returns to its trigger. Back closes only the top popup.'),
('Large font and themes','Repeat at200% font/display size, narrow/landscape screens, dark/light system themes, reduced motion and Android6 WebView.','All text has readable contrast, wraps without overlap, and touch targets remain usable. No page behind the modal scrolls unexpectedly.')])
group('safety','USB permissions, confirmation and operation lifecycle',[
('No-write browsing proof','On a disposable disk, record its complete hash; browse all tools/guides/menus and select the USB without confirming a write; hash it independently again.','Every byte is unchanged.'),
('Warning → Confirm / Cancel','For every destructive operation, select a labeled spare USB, review device identity and options, cancel; then repeat and confirm only on the spare.','No writes before Confirm; correct identity/size/options in review. No typed ERASE requirement.'),
('Target changed during review','Open a write review, detach the spare and attach a different spare before confirming. Repeat with the same adapter and another USB.','Stale permission/device epoch/geometry is rejected; the replacement is not written.'),
('Busy guard','Start a long operation on disposable media, then try other tools, another write, file picker and repeated Confirm taps.','Only one operation runs; the active job remains visible. No duplicate destructive jobs.'),
('Permission allow / deny / revoke','Allow and deny USB permissions; revoke folder permissions; cancel source/destination pickers; reconnect after denial.','No crash, unexpected write or permission loop. Recovery instructions identify the required action.'),
('Background / lock / battery','During a long disposable-media job, background the app, lock/unlock the screen, change orientation, apply battery restrictions and return.','Job state and foreground notification stay coherent. The app never starts a second operation automatically.'),
('Process death / reboot','During a disposable fixture operation, stop the app/reboot the test device. Reopen and review history and target externally.','Interrupted/incomplete state is explicit. There is no silent resume or success claim; previous writes are not claimed to be undone.'),
('Cancellation at every phase','Cancel during validation, reading, metadata writing, verification and partition commit where reachable, using disposable media only.','Cancellation waits for current I/O as needed; incomplete output is clearly reported. No automatic retry. No cleanup of unrelated files.'),
('Disconnect / low power / I/O error','Test a spare through a weak/externally powered hub, detach during read and write, then inspect results independently.','Useful error, bounded resources and retained details. No false completion; actual hardware timeout behavior is recorded.'),
('Progress and stalled NTFS','On a spare run NTFS format. Watch validation, metadata writes, verification and commit; compare bytes/requests/time with device behavior.','Unknown progress is indeterminate; known percentages are stage-only. No false0% stall diagnosis or automatic USB operations. Record any actual stall and full error log.'),
('History and reports','Complete, fail and cancel jobs; open Activity/details; export report in both languages; inspect recent20 records after restart.','Result, time, options and available I/O evidence match the operation. Personal filenames/device identifiers are disclosed before manual sharing.')])
for api in ['Android6/API23 (original WebView)','Android8','Android10','Android11 scoped storage','Android13 notification permission','Android14 Samsung','Android15 edge-to-edge','Android16/target36']:
 group('device-'+str(len(sections)),api,[('Full UI / storage / service smoke','Install the same candidate on '+api+'. Open every tool, use custom pickers, browse an authorized folder, run an owned file operation and a disposableUSB read/write; background and return.','No crash, missing native symbol, unreadable control, broken storage grant, notification/receiver or inset issue. Record model/WebView/ABI and exact failures.')])
for abi in ['arm64-v8a','armeabi-v7a','x86','x86_64']:
 group('abi-'+abi,'Native ABI · '+abi,[('Exercise actual native engines','On a device/emulator using '+abi+', run native formatter and archive/image engines on owned fixtures. Merely installing the APK is insufficient.','Engines load and execute without API-level linker errors; results match independent tools. Physical USB tests are separate where emulators lack host access.')])
group('usbmatrix','USB hardware and storage-provider matrix',[(title,steps,expected) for title,steps,expected in [
('512-byte and4096-byte sectors','Test both on spare devices. Run supported read tools and attempt unsupported formatting geometry.','Supported read operations work;512-only formatters refuse4096-sector media before any writes.'),
('Adapters, hubs and capacities','Repeat with OTG USB2/USB3, powered/unpowered hubs, several capacities and USB controllers. Include almost-full phone storage.','No wrong target, unbounded allocation or silent corruption. Document required power and unsupported configurations.'),
('Unsupported transport devices','Connect MTP phone, UAS-only enclosure, multi-LUN reader, read-only medium and a non-storageUSB accessory.','Unsupported transports are refused honestly; no crash or writes to the wrong LUN.'),
('Provider matrix','Repeat source and destination cases using phone storage, removable FAT32/exFAT, a cloud provider, a read-only provider, and a provider without random access/known size.','Supported providers stream correctly. Unsupported capabilities fail before destructive work where feasible and never look like success.'),
('FAT32 large-file limits','Attempt a>4GiB output into FAT32; repeat with supported part-splitting and with exFAT.','Too-large output is refused/failed clearly; split output can be rejoined and externally hash-verified.'),
('Insufficient space / collisions','Use near-full test folders, source=destination, preexisting output names, provider-renamed output and multiple same-name inputs.','No existing file is overwritten or removed. Only newly created partial outputs are cleaned up; failures identify leftover names.')]])
specific={
'write':['Write a supported hybrid Linux ISO and rawIMG; externally hash all written source bytes; boot the exactPC in UEFI/BIOS. A WindowsISO must not be advertised as a raw-write installer.','Direct persistence: use a compatibleMBR hybridISO,256/768/16384MiB and correct distro label. Boot with persistence, create a file, reboot and check it survives. RejectGPT/no freeMBRentry/insufficienttail beforeISOwrites.','Ventoy persistence mode: create ext4+JSON only, merge manually on a backed-upVentoy drive, then verify boot persistence. No rawwrite or automatic JSON merge.'],
'ventoy':['Choose each goal and follow the selected tool link; confirm no operation starts from the assistant or guide.'],
'format':['For EACH enabledfilesystem andMBR/GPT: format a spare, reconnect, use an independentOS checker, create/read/delete files, eject/reconnect and compare hashes. Test minimumsize, labels and Automatic clusters.','Reproduce the reportedNTFS metadata stall only on disposablemedia; record elapsedtime/IO/logs. Successful hosttests do not prove physicalUSB behavior.'],
'backup':['Back up an entire labeledspare and externally hash source versus completeimage; test>4GiB and restore onto another spare.'],
'files':['Create nestedtestfolders; copy/open/rename/delete onlyownedfiles. Test traversal, revokedgrant, missingfile and source=destination.'],
'info':['Compare capacity, sectors, MBR/GPT partitions, labels/free space with independentPC tools, including damaged/unrecognized filesystems.'],
'verify':['Match a knownimage against writtenUSB; flip onebyte on a spare and confirm mismatch; test shortenedsource/target.'],
'benchmark':['Compare sequentialread behavior with aPC without expecting identicalspeeds; confirm sourceUSB hash unchanged.'],
'hash':['Compare SHA256 against independenttool for empty/small/>4GiB/Arabicnamedfiles; test provider shortread.'],
'wipe':['On a disposableUSB, zerofill and independently read everyaddressablesector; cancel midway and verify incompletewarning. No hidden/sparecell erasure claim.'],
'zip':['Inspect standardZIP with Arabic/nested/emptyfiles and corruptcentraldirectory; no writes duringinspection.'],
'windows':['Prepare your own completeWindowsISO usingUEFI workflow; boot actualUEFI PCs and verify installer starts; test>4GiB install.wim splitting. BIOS/ToGo remain separate unlessimplemented.'],
'ventoyInstall':['Import officialVentoy userpackage; install on a spareMBRdrive, inspect partitionsexternally, copy ownISO and bootBIOS/UEFI/SecureBoot asclaimed.'],
'optical':['List/extract actualISO9660/Joliet/UDF files and comparehashes; test hybrid/malformed extents and Unicodefilenames.'],
'tar':['List/extractTAR with nested/emptyfiles; reject traversal, symlink/hardlink/specialfiles and corruptchecksums without unrelatedwrites.'],
'wimSplit':['Split ownWIM intoSWM; recombine/apply using independentwimlib/DISM and comparecontent. Test invalidlimits and insufficientspace.'],
'capacity':['On spares only, run fullpatternwrite/read; compare knownrealUSB and independently preparedfakecapacity fixture. Repeat with the three-pass profile, confirm distinct whole-drive patterns, progress across all six write/read phases, hours/power behavior and increased wear warning. Shortbenchmark never proves capacity.'],
'extract':['Extract normal and hostileZIP fixtures; confirm hashes, nesting, duplicate/pathtraversal/expansionlimits and newlycreatedoutputcleanup.'],
'randomread':['Run512scatteredreads; compare sourcehash before/after; ensure reportedlatency/throughput are plausible and not ahealthguarantee.'],
'scan':['Read a completespare; test injectedreadfailure/hardwarefailure and confirm no writes or inventedbadblockrepair.'],
'hex':['Compare firstsectorhex with independentreader; verify arbitraryeditcontrols do notexist and sourcehash stays unchanged.'],
'imageinfo':['Inspect rawIMG,MBR,GPT,ISO and truncated/unknownimages; compare signatures externally; detection mustnot claim bootability.'],
'split':['Split atboundary and>4GiB withseveralpartlimits; externallyverify rejoin bytes/hash includingparts beyond9.'],
'join':['Select allownedparts outoforder; compare restoredhash; reject missing/duplicate/corruptmanifest orpart before claiming success.'],
'createzip':['Create ZIP with Arabicnames/emptyfiles and compare independentextraction; rejectduplicates and unsupportedfolders/size/filecount limits.'],
'diagnostic':['Compare API/USBhost/engineavailability and savedfoldergrant with actualdevice; no writes or automaticuploads.'],
'report':['Export report with complete/failed/cancelledhistory and bothlanguages; verify exacterrors/options/time and noautomaticsharing.'],
'clone':['Clone a labeledspare tophoneimage; hash wholeoutput, restore on another spare and compare; test insufficientspace and source disconnect.'],
'checksums':['Enter matching/mismatching/malformedexpectedSHA256; compare independently; mismatch mustnot look successful.'],
'planner':['Use source/target/partsize boundaryvalues and compare arithmetic manually; planning must perform no writes.'],
'ventoyUpdate':['Record allpartitionIDs/tablebytes/filehashes on a backedupdefaultMBRVentoy spare; update thencompare andboot. Unsupportedcustomlayouts must reject beforewrites.'],
'convertTable':['On supportedbasic sparelayouts, convertMBR↔GPT and compare filehashes/partitionbounds/IDs externally. Rejectextended,dynamic,overlap and insufficientGPTtail beforewrites.'],
'persistence':['Generateext4image at256/384/16384MiB, ASCIIlabelpresets/custom; run independente2fsck and boot actualdistro using ownVentoysetup. Rejectinvalidsize,label,path and lackofspace.'],
'fileBenchmark':['Benchmark only a newownedtemporaryfile; independentlyverifycontents and cleanup; refusals mustnot delete existingfiles.'],
'wimExplore':['List ownmultiimageWIM and export selectedfile byexactpath/index; comparehashes; test missingindex/path and encrypted/unsupportedmetadata.'],
'cso':['Roundtrip ownISO→CSOv1→ISO and compare independentSHA256; test random,compressible,large and damagedblockindices.'],
'compressedImage':['DecodeGZIP/XZ and encodeGZIP; compare independently; rejectbadCRC/truncation and expansionlimit violations.'],
'ventoyGpt':['Install/update defaultGPTVentoy onspares; verifybothGPTCRCs,partitions,ISOhashes and bootPCs. Rejectcustom/damagedlayouts beforewrites.'],
'fatImage':['Browse/extractFAT16/32 rawvolume andMBR/GPT partitions; compare fragmented,nested,empty,ArabicLFN files withPC; rejectcyclic/sharedchains.'],
'zso':['Roundtrip ownISO→ZSOv1→ISO; externalhashmatch and actualOPLloader compatibility. Test mixedstored/LZ4blocks and shiftedindices.'],
'consoleParts':['Split/rejoinISO.#/.666NN with manifest; compare externalhash and actualPS3manager support. Testpartorder>9,missing/corruptparts and providernamechanges.'],
'filesystemCheck':['For eachsupportedFS, compare completeUSBhash before/afterreadonlycheck. Repair only a deliberatelydamaged spare; compare files andtables and run independentchecker afterward. SevereNTFS requiresPCchkdsk; limitedrepair isnot fullrecovery.'],
'archives':['List/extractZIP/7z/RAR/TAR using independentfixtures; comparefilehashes, solidarchives,unsafeentries,size/entrylimits and corruptedstreams. Password/multiparttests apply onlywhen thosemodes are actuallyimplemented.'],
'dmg':['Convert ownUDIFraw/zero/ADC/zlibimages and compare decompressedhash againstMac/independenttool; rejectencrypted/segmented/unsupportedcodecs honestly.'],
'chd':['Decode standaloneCHDv5HD/DVDfixture; compare independentlywithchdman. Testparentdependent,CDmixedtracks,unsupportedcodecs/oversizedhunks andcorruptCRC refusal. Also create stored CHD from 512/2048-byte-aligned raw sources, verify/extract using independent chdman and compare every byte/SHA256. No compression, CD track layout or console compatibility claim.'],
'verifyZero':['Verify a fullyzeroedspare thenflip a knownbyte; confirm detectednonzerooffset and datedrecord. Neverlabel ascertifiedsanitization.'],
'fsImage':['Browse/extractregularNTFS/ext2/3/4 files fromrawimage/partition; comparehashes includingnested/fragmentedfiles; test sparse/compressed/specialfiles asdocumented.'],
'fsUsb':['Browse/extractfrom exactselectedsparepartition; comparefilehashes and wholeUSBhash unchanged. Detach/replacetarget and confirmstaleidentityrefusal.'],
'wimTree':['Export nestedWIMfolder using exactimageindex/rootpath; compare everyfile independently, Arabicnames,emptydirectories, collisions androllback. FullWindowsACL restoration mustnot beclaimed.'],
'usbExtreme':['Createparts/ul.cfg from ownpermittedPS2ISO; inspectrecord/title/discID, externallyreconstructhash and test exactOPLconsoleversion. RejectinvalidSYSTEM.CNF,ambiguousIDs and collisions.'],
'readMap':['Start/pause/resume/show on a spare with knownreadfault; changegeometry orsampleidentity and confirmresumerefusal. Sourcehash remains unchanged; readerrors arenot physicalremapping.'],
'partitionMeta':['ChangeGPTname orMBRactiveflag onspare; compareeverysector externally and inspectGPTCRCs. Unsupportedlayout/invalidname/cancel mustnotwrite.']}
for tool in catalog['tools']:
 key=tool['id'];help=catalog['help'].get(key,{})
 cases=[]
 for i,step in enumerate(specific.get(key,[])):cases.append(('Tool-specific scenario '+str(i+1),step,'Record exact inputs, independent comparison, output and any defect. Pass only when the stated behavior and published limits are confirmed.'))
 cases += [('Normal supported workflow','Read the tool\'s supported scope below. Follow: '+' → '.join(x[0] for x in help.get('steps',[]))+'. Use only owned fixtures/spares and independently compare the result.','The observed result matches: '+help.get('result',['See scope.'])[0]),('Invalid input / permission / capacity','Try a truncated or unsupported source, denied permission, missing target, and insufficient destination space as applicable. Preserve hashes of all originals.','Clear, localized refusal; no unrelated edits/deletes, no crash and no false success.'),('Cancel and recover','Cancel during each reachable phase using disposable media/owned outputs. Reopen Activity/details and retry only after completion with a newly reviewed target.','No duplicate jobs, automatic retry, unrelated cleanup, stale target write or false completion. Record any partial output.'),('Arabic / English / accessibility','Open this tool in both languages; enlargefont; inspect controls/options/warnings/progress/results on phone andtablet withTalkBack.','Readable text, correctRTL, usable controls and consistent selected-language reports.')]
 group('tool-'+key,tool['name'][0]+' · '+tool['name'][1],cases)
 sections[-1]['scope']=tool['body'];sections[-1]['impact']=help.get('impact',['',''])
 recipe=RECIPES[key]
 sections[-1]['cases'].insert(0,dict(id='tool-'+key+'-walkthrough-v2',title='Try this tool step by step',guided=True,**recipe))
for fs in ['fat12','fat16','fat32','exfat','ntfs','ext2','ext3','ext4','f2fs','udf','btrfs','xfs','hfsplus','apfs','refs','luks']:
 group('format-'+fs,'Format matrix · '+fs,[(table.upper()+' and labels','For new UDF/HFS+/APFS/Btrfs/XFS: up to 32 GiB, Automatic allocation, 1–15 ASCII letters/digits/spaces/_/- (XFS maximum 12), 512-byte sectors only; APFS MBR must refuse before writes (mark PASS for that refusal). Minimum: UDF/HFS+ 33 MiB, APFS/Btrfs 256 MiB, XFS 512 MiB. If '+fs+' is enabled in the exactcandidate: format a correctly sized disposableUSB with '+table.upper()+', Automaticclusters, ASCIIlabel and a documentedUnicode/boundarylabel. Independently checkpartitionlayout/filesystem, mount on the intendedPC, create/read/deletefiles and reconnect. If unavailable, markBlocked and note the engine gap; never assume it works.','Only supportedoptions are accepted beforewrites. Correctpartitiontype/CRCs/labels; usablefiles survive reconnect. Record exactPCtools andmountresult.') for table in ['mbr','gpt']]+[('Boundaries / cancellation','Test belowminimumsize,toolarge/invalidlabel,unsupportedclusters,4096sectors and early/mid/latecancel onspares only.','Invalidoptions make no writes. Postwritecancel/failure saysincomplete. Existingdata cannot beclaimed restored.')])
group('development','Unfinished roadmap · verification required before enabling',[(x['name'][0],x['body'][0]+' Implement and validate a real engine before enabling. Record upstream/source/license, supported scope, synthetic fixtures, actual hardware/PC evidence and remaining blockers.','No placeholder button or relabeled existing tool is counted as a completed implementation. Platform/controller/proprietary limits are stated honestly.') for x in catalog['roadmap']])
group('publishing','Release gates · all must have evidence',[
('Build / signatures / payload','Build offline from final frozen sources; verify QA signature, package/version/min23/target36/nondebuggable, native4ABI/16KiBalignment and officialAABvalidation. Inspect finalpayloadsize and compare installedAPKhash/version.','Evidence is for this exactcandidate; production signing and publisher ownership are configured separately beforeupload.'),
('Lint / automated regression / assets','Run relevant engine/integration/UI/language/ES5/payloadtests. Inspect the packagedassets againstsource. Check that all source/licenses correspond to newnativebinaries.','No failing mandatorytests or unexpectedpackagedpayload. Hostfixtures are not labeled physicalhardwaretests.'),
('Privacy and store claims','Review privacy notice/DataSafety/store screenshots and descriptions against actualpermissions,logs,localpins,passwordhandling and everyenabledtool. Check currentGooglePlaypolicies in the publishingconsole.','No fabricatedsupportedformat/boot/security/repair claim; no unnecessarypermissions,secretkeys,analytics,automaticdownloads orOS/gamepayloads.'),
('Native security and date limits','Review upstream security advisories and exact pinned dependencies before publication. On 32-bit Android test timestamp behavior near/after 2038 and document supported dates. Exercise malformed inputs under resource limits; do not bypass native checksum self-tests.','No known applicable unresolved vulnerability ships without an assessed fix. Compatibility limits are reflected in claims and release notes.'),
('Licenses and signing readiness','Check everynewlibrarylicense and corresponding-sourceobligation, exportability, productionkeyownership and finalsignedAAB. Keep secrets outside source archives.','No unlicensedproprietaryengine ormissingrequiredsource. UnsignedAAB isnot calledreadyforupload.'),
('Manual matrix complete','Review everymandatorymanualitem, eachsupportedfilesystem, eachclaimedABI/API/provider/hardwareclass and allPC/consolebootclaims. Attach exactevidence and knownlimitations.','NoFail/Blocked/Not-tested item in a claimedfeature. Unsupportedfeatures staydisabled and storeclaims are narrowed.'),
('Recovery and support','Give another tester only theAPK anduserguides. Ask them to identifytarget,backup,confirm,handlefailure,exportdetails andrecover onto a spare.','Beginner can followinstructions withoutdatamisdirection. Publish supportcontact/knownlimitations andanupdate/rollbackplan.'),
('Final regression after fixes','For each issue recorded in notes: link its fix/build, rerun the failingcase and relevantneighboringflows, then recordretets/evidence.','No openhighseveritywrongdiskwrite,corruption,privacy,crash,unreadablecontrol orfalse-success defect. Passing this list still cannot guarantee zerouserissues.')])
# New guided cases have new IDs, so old generic passes cannot stand in for new walkthroughs.
def guided(key,title,need,why,steps,expected,extra=''):
 return dict(id=key,title=title,need=need,why=why,steps=steps,expected=expected,extra=extra,guided=True)
sections.insert(0,dict(id='start-here-v2',name='1. Start here — prepare once, then test tools',cases=[
 guided('prepare-files-v2','Prepare your files and output folder','Android Files and USB-Test-Files.zip in Downloads.','You need known originals to tell whether an output is correct.',[
 'In Android Files open Downloads. Extract USB-Test-Files.zip. Open its README.txt and HASHES.txt.',
 'Create a DIFFERENT folder in internal Downloads called TestOutput. All app results go there or into new folders inside it; never save over the originals.',
 'Custom split/join now uses raw-8MiB.img with a typed 3 MiB part size. The old large ZIPs are unnecessary for this basic test.',
 'Keep this HTML and the ZIPs outside any USB that you will erase. Our samples contain data only and cannot boot a PC.'],
 'You can open small/hello.txt and see USB Workbench manual test. TestOutput is empty and separate.'),
 guided('save-report-practice-v2','Practice saving and copying a result before real testing','This HTML opened in a normal browser, not private browsing.','This prevents losing an evening of testing.',[
 'For THIS practice case choose Not sure and type a short note containing Arabic or a special character.',
 'Look for Saved on this browser. Close and reopen this same HTML in this same browser. The result and note must still be here.',
 'Press Copy full report for Codex. If the browser blocks copying, use the visible text box to Select all and Copy, or Download report .txt.',
 'Check the report contains this case ID, your Not sure result and your note. Back up results JSON and keep that file.',
 'Now mark this practice Worked if persistence and report worked. If they failed, say which step failed; use JSON/HTML backups after every session.'],
 'Your note survives reopening and appears in the complete report; JSON/HTML backups can preserve it.'),
 guided('home-ui-v2','Check the Home page, pins, Learn and menus','No USB needed.','These are the first things every user sees.',[
 'Open the app, close it normally and reopen it three times. Without opening File manager, no expired-folder banner should appear.',
 'On Home open each category. Tool rows should be wide, with a separate pin button. Pin two tools; reopen the app and check they remain pinned.',
 'Open Learn. Search format, then USB, then nonsense text. Clear search and open each beginner link. Check the search field and buttons are readable.',
 'Open Format USB only to inspect its menus; do NOT run it. Open filesystem, table and allocation choices. Text/background must be readable; choosing APFS forces GPT and Automatic.',
 'Change app language to Arabic and repeat the page/menu checks. Rotate the device and enlarge Android font size; restore your preferred settings afterward.'],
 'No unwanted startup error; pins persist; three navigation tabs; readable Home/Learn/custom choices in both languages.'),
 guided('usb-identity-v2','Identify a spare USB before any destructive test','One EMPTY disposable USB and OTG adapter.','Selecting the wrong disk is the most serious failure to avoid.',[
 'Physically label the spare TEST ONLY. Copy any needed files elsewhere before using it.',
 'Connect only that spare. Tap Select a USB in Home; first deny permission and check there is no crash, then select it again and allow.',
 'Compare the displayed model and capacity with the real device. Disconnect/reconnect and repeat.',
 'For any later erase/write review, read this identity again. If it is wrong, Cancel and report it; do not proceed.'],
 'Permission denial is handled, and the app consistently identifies the actual spare before any operation.')]))
for section in sections:
 if not section['id'].startswith('format-'):continue
 fs=section['id'][7:];label={'hfsplus':'HFS+','exfat':'exFAT'}.get(fs,fs.upper())
 if fs in ['refs','luks']:
  item=guided('format-'+fs+'-guided-v2',label+' must stay unavailable','No physical formatting is needed.','Unimplemented engines must not look ready to users.',[
   'Open Format USB and open the filesystem choices.',
   'Find '+label+'. It must be marked unavailable and must not be selectable.',
   'Try tapping it, close the menu, and check the previously selected working format is unchanged.'],
   label+' cannot be selected or started. Do not try to create it with this app.')
 else:
  osname='a Linux computer' if fs in ['ext2','ext3','ext4','f2fs','btrfs','xfs'] else 'a Mac' if fs in ['apfs','hfsplus'] else 'a compatible Windows/Linux/macOS computer'
  limit={'fat12':'2–32 MiB only','fat16':'8 MiB–2 GiB only','fat32':'at least 64 MiB','ntfs':'at least 33 MiB','ext2':'at least 33 MiB','ext3':'at least 33 MiB','ext4':'at least 33 MiB','f2fs':'at least 256 MiB','udf':'33 MiB–32 GiB','hfsplus':'33 MiB–32 GiB','apfs':'256 MiB–32 GiB','btrfs':'256 MiB–32 GiB','xfs':'512 MiB–32 GiB'}.get(fs,'a capacity supported by the app')
  item=guided('format-'+fs+'-guided-v2','Format '+label+' and prove files survive reconnect','EMPTY spare USB: '+limit+'; '+osname+'. 512-byte sectors are required.',
   'Android may not mount this filesystem. Its intended computer must actually read and write it.',[
   'Select the TEST ONLY spare. Open Format USB, choose '+label+', label USBTEST and Automatic allocation.',
   'Choose '+('GPT (APFS does not support MBR here)' if fs=='apfs' else 'MBR for the first run')+'. Read the drive name/capacity and erase warning; Confirm only for the spare.',
   'Wait for completion. Safely disconnect and connect to '+osname+'. If Android asks to format a Linux/Mac filesystem, decline that prompt.',
   'On that computer copy small/hello.txt and small/عربي.txt onto the USB. Safely eject, reconnect, and read both files back.',
   'Compare the two files with their originals using their complete SHA-256 fingerprints. On Windows PowerShell: Get-FileHash -Algorithm SHA256 "path-to-file". On Linux: sha256sum "path-to-file". On macOS: shasum -a 256 "path-to-file".',
   ('Repeat using GPT instead of MBR; record MBR and GPT outcomes separately.' if fs!='apfs' else 'Confirm the app forces GPT; MBR must not be selectable for APFS.')],
   'Computer recognizes '+label+'; files remain readable after reconnect and their fingerprints match for each claimed table option.',
   'If you lack a suitable drive or computer, mark Blocked and explain. Do not mark Worked just because the Android formatting message completed.')
 section['cases'].insert(0,item)
sections.append(dict(id='finish-report-v2',name='Finish — send the results and decide what remains',cases=[
 guided('send-report-v2','Send everything back without guessing','Your saved results and notes.','The report lets the developer link each problem to an exact test and fix.',[
 'Review Did not work, Not sure and Blocked filters. In every such case write the selected file, USB, options, exact error and what you observed. Mention screenshot filenames if available.',
 'Record app version, device/Android and test USB/computer at the top. Do not change a failure to Worked simply to finish faster.',
 'Press Copy full report for Codex. The report includes EVERY case and tool, even cases hidden by a search/filter or not yet tested.',
 'Paste the complete report into our chat. If too large for one message, send consecutive parts without omitting sections, or attach the downloaded .txt report.',
 'Download results JSON too. After receiving an updated HTML, open it under the same filename/browser; results should remain. If missing, use Restore results JSON. A new app candidate makes old Worked results require retesting.'],
 'The pasted/downloaded report contains all statuses, all notes, general/tool notes and candidate details.'),
 guided('publication-review-v2','Review release blockers before publishing','The report; developer help for specialist checks.','Finishing a small example does not prove every advertised format or device works.',[
 'Complete the guided test for every tool you intend to advertise. Record missing equipment as Blocked, not Worked.',
 'Open All checks, including developer lab. Send unresolved hardware, older Android, repair, interruption and PC/console cases to the developer or a tester who can run them.',
 'Do not release a known wrong-drive write, corruption, crash, privacy leak or false-success bug. Fix and retest affected cases first.',
 'Before uploading, have the developer finish production signing, license/security review, store declarations and accurate feature claims. The existing unsigned AAB is not an upload-ready signed release.',
 'A smaller clearly limited release can be faster than claiming untested features. Unsupported ReFS/LUKS and other unfinished scope must remain unavailable and must not appear as working store claims.'],
 'No unexplained blocking defect in a claimed feature; remaining limits and publication checks are explicitly recorded.')]))
# Keep old File-manager result IDs as an archive; there is no active app tool for it.
retired=json.loads((ROOT/'tools/manual_retired_files.json').read_text());sections.append(retired)
retest={'home-ui-v2','save-report-practice-v2','tool-extract-walkthrough-v2','tool-createzip-walkthrough-v2','tool-tar-walkthrough-v2','tool-zip-walkthrough-v2','tool-archives-walkthrough-v2','tool-wimExplore-walkthrough-v2','tool-wimTree-walkthrough-v2','tool-fileBenchmark-walkthrough-v2','format-fat12-guided-v2','format-fat16-guided-v2'}
for g in sections:
 for c in g['cases']:
  if c['id'] in retest:c['needsRetest']=True
  if c['id'] in ['format-fat12-guided-v2','format-fat16-guided-v2']:c['extra']+=' Your report only mentions the size refusal. That confirms validation, not successful formatting. If you have no spare in this size range, record Blocked rather than claiming file read/write passed.'
sections[0]['cases'].append(guided('test-environment-v3','Record what your earlier format results actually covered','Your device, USB label and any computer used.','The earlier report has no device/USB/computer details, so a Worked result cannot tell us whether computer mounting was checked.',[
 'Fill the device and USB/computer fields at the top using the equipment you actually used.',
 'For earlier filesystem results, say in notes whether you only saw Android formatting complete, or also copied/read files after reconnecting on a compatible computer.',
 'Keep earlier results as your observations. If the computer part was not checked, write that explicitly; do not guess a PC result.'],
 'The next report identifies the environment and separates formatting completion from external file-read/write evidence.'))
sections.append(dict(id='changed-flows-v3',name='Retest these changed app flows',cases=[
 guided('inline-progress-v3','See progress in the tool, then go Back without stopping it','A backed-up spare USB for a READ-ONLY scan.','The updated app must keep one job running when you leave its form.',[
 'Open Read surface scan, select your spare and start. While it is still running, check progress is inside that tool.',
 'Press Back. The same job should continue in the existing bottom progress panel; elapsed time must not restart.',
 'Open Read surface scan again. It should show the same running progress.',
 'Tap Cancel explicitly and wait for a cancelled result. Back alone must not cancel or launch a second scan.'],
 'One scan continues across Back/reopen; only Cancel stops it. Source data remains unchanged.'),
 guided('custom-sizes-v3','Type a small custom size and verify the result','raw-8MiB.img from the small pack; internal TestOutput folder.','Custom fields must send your typed size without rounding it to an old preset.',[
 'Open Split large file. Choose raw-8MiB.img and type 3 as the part size in MiB. Run into a new output folder; expect three .uwp files.',
 'Rejoin those three parts into another folder. Its SHA-256 must match raw-8MiB.img in HASHES.txt.',
 'Open File write & read benchmark, type 3 MiB, run in TestOutput and check that verification succeeds and its temporary file is removed.',
 'Open ISO ↔ CSO conversion and type 0.5 GiB as the maximum expanded size. Confirm the number remains 0.5; the new CSO walkthrough explains the round trip.',
 'Try zero or an out-of-range value in each changed field. It must refuse, not silently use a preset.'],
 'Custom split and benchmark sizes are respected; fractional GiB is accepted; invalid sizes do not start a job.')]))
# Put quick file-only work before drive erasure and equipment-dependent boot tests.
order=['hash','checksums','zip','extract','createzip','tar','archives','split','join','compressedImage','cso','zso','chd','dmg','optical','imageinfo','fatImage','wimExplore','wimTree','wimSplit','diagnostic','report','planner','fileBenchmark','info','hex','benchmark','randomread','scan','readMap','format','write','verify','backup','clone','convertTable','partitionMeta','filesystemCheck','fsUsb','fsImage','wipe','verifyZero','capacity','ventoyInstall','ventoyGpt','ventoyUpdate','ventoy','persistence','windows','consoleParts','usbExtreme']
assert set(order)=={x['id'] for x in catalog['tools']}
def rank(g):
 if g['id']=='start-here-v2':return -1
 if g.get('retired'):return 3000
 if g['id'].startswith('tool-'):return order.index(g['id'][5:])*10
 if g['id'].startswith('format-'):return order.index('format')*10+1
 if g['id']=='changed-flows-v3':return -0.5
 if g['id']=='finish-report-v2':return 1000
 return 2000
sections.sort(key=rank)
from manual_automation import apply
evidence_path=ROOT/'artifacts/automation-review/evidence.json'
if not evidence_path.exists():evidence_path=ROOT/'publishing/automation-review-0.16.1.json'
evidence=json.loads(evidence_path.read_text()) if evidence_path.exists() else {'status':'not yet recorded','runs':[]}
sections=apply(sections,evidence)
payload=json.dumps(sections,ensure_ascii=False).replace('<','\\u003c')
candidate='0.16.1 / code 20'
qa=ROOT/'artifacts/USB-Workbench-0.16.1-QA.apk'
if qa.exists():
 import hashlib
 candidate+=' / SHA-256 '+hashlib.sha256(qa.read_bytes()).hexdigest()
html=(ROOT/'tools/manual_notebook.html').read_text().replace('__DATA__',payload).replace('__CANDIDATE__',json.dumps(candidate)).replace('__AUTOMATION__',json.dumps(evidence,ensure_ascii=False).replace('<','\\u003c'))
review=ROOT/'artifacts/user-tests-2026-11-07.json'
if review.exists():
 state=json.loads(review.read_text());state['meta']['candidate']=candidate
 html=html.replace('<script id="saved-results" type="application/json">{}</script>','<script id="saved-results" type="application/json">'+json.dumps(state,ensure_ascii=False).replace('<','\\u003c')+'</script>')
path=ROOT/'artifacts/USBtests.html';path.write_text(html);print('Manual checklist:',len(sections),'sections,',sum(len(g['cases']) for g in sections),'cases,',path.stat().st_size,'bytes')
