'use strict';
const TOOLS = [
{id:'write',icon:'burn',color:'green',group:'boot',name:['Write an image','كتابة صورة نظام'],desc:['Flash IMG & hybrid ISO files','كتابة ملفات IMG وISO الهجينة'],body:['Write a bootable disk image directly to the USB, then read it back to verify every byte. Use an uncompressed IMG or a hybrid Linux ISO designed for raw writing. This replaces the existing partitions. A normal Windows ISO, DMG, compressed archive or ordinary ISO may not boot with this method.','اكتب صورة قرص مباشرةً على USB، ثم اقرأها للتحقق من كل بايت. استخدم IMG غير مضغوطة أو Linux ISO هجينة مصممة للكتابة الخام. ستُستبدل الأقسام الحالية. صور Windows ISO العادية وDMG والأرشيفات وISO غير الهجينة قد لا تقلع بهذه الطريقة.']},
{id:'ventoy',icon:'layers',color:'blue',group:'boot',name:['Multi-ISO library','مكتبة أنظمة متعددة'],desc:['Add images to an existing Ventoy USB','أضف صوراً إلى USB عليه Ventoy مسبقاً'],body:['Already have a Ventoy drive? Select its large data partition using Android’s folder picker, then copy multiple ISO files in one task. Ventoy must already be installed using another supported tool. Initial MBR installation is available separately as an experimental tool; updating remains unsupported. Copying ISO files onto an ordinary USB does not make it bootable.','هل لديك وحدة عليها Ventoy؟ اختر قسم البيانات الكبير عبر منتقي المجلدات في أندرويد ثم انسخ عدة ملفات ISO في عملية واحدة. يجب تثبيت Ventoy مسبقاً بأداة تدعمه. التثبيت الأولي بجدول MBR متاح كأداة تجريبية منفصلة؛ التحديث غير مدعوم. نسخ ISO إلى USB عادي لا يجعله قابلاً للإقلاع.']},
{id:'format',icon:'format',color:'amber',group:'disk',name:['Format USB','تهيئة USB'],desc:['14 filesystems • MBR / GPT','١٤ نظام ملفات • MBR / GPT'],body:['Create one FAT12, FAT16, FAT32, exFAT, NTFS, ext2, ext3, ext4 or F2FS partition using MBR or GPT. Experimental UDF, HFS+, APFS, Btrfs and XFS formatters are also available up to 32 GiB with Automatic allocation and ASCII labels (1–15 characters; XFS maximum 12); APFS requires GPT. Minimum: UDF/HFS+ 33 MiB, APFS/Btrfs 256 MiB, XFS 512 MiB. They do not add Android mounting support. FAT12 is for 2–32 MiB devices; FAT16 for 8 MiB–2 GiB. NTFS/ext need at least 33 MiB. ext and F2FS use 4 KiB blocks; F2FS requires 256 MiB and Automatic allocation. Formatting ext/F2FS/NTFS does not make Android able to mount them. Native engines are experimental until tested on disposable media. Choose a label and cluster size. FAT32 supports broad compatibility but limits single files to 4 GiB minus 1 byte. exFAT supports larger files and Unicode labels. Formatting erases the current layout; quick format is not secure erasure. The formatter needs 512-byte sectors.','أنشئ قسماً واحداً بنظام FAT12 أو FAT16 أو FAT32 أو exFAT أو NTFS أو ext2 أو ext3 أو ext4 أو F2FS بجدول MBR أو GPT. تتوفر أيضاً محركات UDF وHFS+ وAPFS وBtrfs وXFS التجريبية حتى 32 GiB مع تخصيص تلقائي واسم ASCII من 1–15 رمزاً (XFS حتى 12)؛ يحتاج APFS إلى GPT. الحد الأدنى: UDF/HFS+ ‏33 MiB وAPFS/Btrfs ‏256 MiB وXFS ‏512 MiB. لا تضيف التهيئة دعماً للتركيب في أندرويد. FAT12 لوحدات 2–32 MiB وFAT16 لوحدات 8 MiB–2 GiB. يحتاج NTFS/ext إلى 33 MiB على الأقل. يستخدم ext وF2FS كتل 4 KiB؛ يحتاج F2FS إلى 256 MiB والتخصيص التلقائي. التهيئة لا تجعل أندرويد قادراً على تركيبها. المحركات الأصلية تجريبية حتى اختبارها على وسائط تجريبية. اختر الاسم وحجم العنقود. FAT32 واسع التوافق لكن حد الملف 4 GiB ناقص بايت. exFAT يدعم ملفات أكبر وأسماء عربية. التهيئة تمحو التخطيط الحالي، والتهيئة السريعة ليست محواً آمناً. يلزم قطاع بحجم 512 بايت.']},
{id:'backup',icon:'backup',color:'blue',group:'disk',name:['Backup & restore','النسخ والاستعادة'],desc:['Save a full, byte-for-byte drive image','احفظ صورة كاملة لكل بايت من الوحدة'],body:['Back up every sector, including unused space, to an IMG file on your phone or another drive. You need free space equal to the full USB capacity. To restore, select the saved IMG in Write an image. The destination must be at least as large as the original drive. Backup files are not encrypted.','انسخ كل القطاعات، بما فيها المساحة غير المستخدمة، إلى IMG على الهاتف أو وحدة أخرى. تحتاج مساحة تساوي سعة USB كاملة. للاستعادة اختر IMG المحفوظة في «كتابة صورة نظام». يجب ألا تقل سعة الوجهة عن الوحدة الأصلية. ملفات النسخ الاحتياطية غير مشفرة.']},
{id:'files',icon:'folder',color:'green',group:'files',name:['Android Files','تطبيق ملفات أندرويد'],desc:['Browse, copy, rename & delete','تصفح وانسخ وأعد التسمية واحذف'],body:['Use Android’s storage picker to grant access to one folder. Browse subfolders, copy multiple files, rename, delete and calculate SHA-256. Filesystem support depends on Android and the storage provider. NTFS/ext drives that Android cannot mount need a separate driver, which is not bundled. Existing names are never overwritten during copy.','امنح الوصول لمجلد عبر منتقي تخزين أندرويد. تصفح المجلدات الفرعية وانسخ عدة ملفات وأعد تسميتها واحذفها واحسب SHA-256. دعم أنظمة الملفات يعتمد على أندرويد ومزوّد التخزين. وحدات NTFS/ext التي لا يقرأها أندرويد تحتاج محركاً منفصلاً غير مضمن. النسخ لا يستبدل ملفات تحمل الاسم نفسه.']},
{id:'info',icon:'info',color:'blue',group:'health',name:['Full USB analyzer','محلل USB الشامل'],desc:['MBR/GPT, filesystems, capacity & space report','تقرير MBR/GPT وأنظمة الملفات والسعة والمساحة'],body:['Read-only USB descriptor, SCSI identity, partition and filesystem analysis. Validates primary/backup GPT CRCs; reads MBR and bounded extended chains. Reports FAT/exFAT/NTFS allocation space where readable, ext stored counters, labels and identities. Unknown or damaged metadata stays unavailable, never zero. Uses at most 64 MiB of drive reads; no files are copied or changed. Report appears in Activity and can be exported. Capacity is reported by the controller, not proven genuine. This is not a full surface scan, SMART test or boot guarantee.','تحليل واصفات USB وهوية SCSI والأقسام وأنظمة الملفات للقراءة فقط. يتحقق من CRC لنسختي GPT ويقرأ MBR وسلاسل ممتدة محدودة. يعرض تخصيص FAT/exFAT/NTFS عند إمكان قراءته وعدادات ext المحفوظة والأسماء والمعرفات. تبقى البيانات المجهولة أو التالفة غير متاحة ولا تعرض كصفر. يقرأ حتى 64 MiB دون نسخ الملفات أو تغييرها. يظهر التقرير في النشاط ويمكن تصديره. السعة مبلغ عنها من المتحكم ولم تثبت صحتها. ليس فحصاً لكل القطاعات أو SMART ولا يضمن الإقلاع.']},
{id:'verify',icon:'shield',color:'green',group:'health',name:['Verify an image','التحقق من الصورة'],desc:['Compare image bytes against USB','قارن بايتات الصورة مع USB'],body:['Read the USB and compare it with your selected source image, byte by byte. This does not write to the USB. A mismatch means the image differs at the reported byte offset. It is not a complete fake-capacity or bad-sector test.','اقرأ USB وقارنه بالصورة المختارة بايتاً ببايت. لا يكتب هذا الاختبار على الوحدة. عدم التطابق يعني اختلاف الصورة عند موضع البايت المعروض. هذا ليس اختباراً شاملاً للسعة المزيفة أو القطاعات التالفة.']},
{id:'benchmark',icon:'speed',color:'amber',group:'health',name:['Read benchmark','قياس سرعة القراءة'],desc:['32 MiB sequential read • no writes','قراءة تسلسلية 32 MiB دون كتابة'],body:['Time a sequential read of up to 32 MiB from the beginning of the USB. Shows MiB/s. Results depend on the cable, phone, power and caching. This short test does not measure write speed, random access or full-drive health.','قِس زمن القراءة التسلسلية لما يصل إلى 32 MiB من بداية الوحدة. النتيجة بوحدة MiB/s. تتأثر بالكابل والهاتف والطاقة والتخزين المؤقت. هذا الاختبار القصير لا يقيس سرعة الكتابة أو الوصول العشوائي أو صحة الوحدة كاملةً.']},
{id:'hash',icon:'hash',color:'blue',group:'files',name:['SHA-256 checksum','بصمة SHA-256'],desc:['Check downloaded images offline','تحقق من صور النظام دون إنترنت'],body:['Calculate a SHA-256 fingerprint of any selected file. Compare all 64 hexadecimal characters with the checksum on the original publisher’s website. A matching checksum verifies the file matches that checksum; it only establishes authenticity when you trust its source.','احسب بصمة SHA-256 لأي ملف تختاره. قارن جميع رموز البصمة الـ64 السداسية مع البصمة في موقع الناشر الأصلي. التطابق يؤكد مطابقة الملف للبصمة، ولا يثبت موثوقيته إلا إذا وثقت بمصدر البصمة.']},
{id:'wipe',icon:'erase',color:'amber',group:'disk',name:['Zero-fill drive','ملء الوحدة بالأصفار'],desc:['Overwrite all addressable sectors','استبدل كل القطاعات المتاحة بالأصفار'],body:['Write zeros across the full drive. All files and partitions are destroyed. This can take hours and wears flash memory. Flash wear leveling and spare cells mean this is not guaranteed secure sanitization. No signed erasure certificate or sanitization claim is made.','اكتب أصفاراً على الوحدة كاملةً. ستُحذف كل الملفات والأقسام. قد تستغرق العملية ساعات وتستهلك عمر الفلاش. موازنة التآكل والخلايا الاحتياطية تعني أن هذه ليست إزالة آمنة مضمونة. لا يوفر الإصدار شهادة محو موقعة أو ادعاءً بتطهير آمن.']},
{id:'zip',icon:'archive',color:'blue',group:'files',name:['Inspect ZIP','عرض محتويات ZIP'],desc:['List archive entries without extracting','اعرض عناصر الأرشيف دون استخراج'],body:['Select a ZIP file and list up to 1,000 entries. Nothing is extracted or written. Password-protected, 7z, RAR, WIM and other formats are not supported in this version. Reading a large ZIP can still take time.','اختر ZIP لعرض ما يصل إلى 1000 عنصر. لا يتم استخراج أو كتابة أي ملف. الأرشيفات المحمية بكلمة مرور و7z وRAR وWIM وغيرها غير مدعومة في هذا الإصدار. قد تستغرق قراءة ZIP كبير وقتاً.']}
];
TOOLS.push(
{id:'windows',icon:'burn',color:'blue',group:'boot',name:['Windows UEFI preparation','تجهيز Windows UEFI'],desc:['Your ISO → verified USB files • experimental','صورتك ISO ← ملفات USB متحقق منها • تجريبي'],body:['Select your own Windows ISO and an EMPTY removable USB root that YOU have confirmed is FAT32. Android cannot reliably report its filesystem here. Streams ISO9660/Joliet or supported UDF files, validates EFI/WIM headers, verifies copied files with SHA-256 and splits a large install.wim into verified SWM files. No whole ISO/WIM phone cache. Does not format, install BIOS boot code, activate Windows or provide Windows To Go. Real PC boot is still untested.','اختر Windows ISO الخاصة بك وجذر USB قابل للإزالة وفارغاً تحققت بنفسك أنه FAT32؛ لا يستطيع أندرويد تحديد النظام هنا بشكل موثوق. يقرأ ISO9660/Joliet أو UDF المدعوم، ويتحقق من ترويسات EFI/WIM، ويتحقق من الملفات المنسوخة ببصمة SHA-256، ويقسم install.wim الكبير إلى SWM متحقق منها. لا يخزن ISO/WIM كاملةً على الهاتف. لا يهيئ الوحدة ولا يثبت إقلاع BIOS ولا يفعّل Windows ولا يوفر Windows To Go. الإقلاع الفعلي غير مختبر بعد.']},
{id:'ventoyInstall',icon:'layers',color:'amber',group:'boot',name:['Install Ventoy · experimental','تثبيت Ventoy · تجريبي'],desc:['Import official package • MBR initial install','استيراد الحزمة الرسمية • تثبيت MBR أولي'],body:['Choose an official Ventoy Linux tar.gz package you downloaded yourself. Import validates and caches only about33 MiB of boot DATA; package scripts and executables are never run. Then select a USB and review initial installation: ERASES every current partition, creates exFAT data and32 MiB EFI partitions, and reads every write back before committing MBR.512-byte sectors,128 MiB minimum. Use the separate GPT and compatible-update tools for those operations. Custom layouts remain unsupported. File-backed tests are available; physical PC/USB and Secure Boot tests are still required. This is an independent experimental integration.','اختر حزمة Ventoy Linux بصيغة tar.gz نزّلتها بنفسك. يتحقق الاستيراد ويحفظ نحو33 MiB من بيانات الإقلاع فقط؛ لا يشغّل سكربتات الحزمة أو برامجها. ثم اختر USB وراجع التثبيت الأولي: يمحو كل الأقسام الحالية وينشئ قسمي بيانات exFAT وEFI بحجم32 MiB، ويقرأ كل كتابة للتحقق قبل اعتماد MBR. يحتاج قطاع 512 بايت وسعة128 MiB على الأقل. استخدم أدوات GPT والتحديث المتوافق المنفصلة لتلك العمليات. التخطيطات المخصصة غير مدعومة. تتوفر اختبارات صور الملفات؛ لا تزال اختبارات الحاسوب وUSB وSecure Boot مطلوبة. هذا تكامل تجريبي مستقل.']},
{id:'optical',icon:'folder',color:'blue',group:'files',name:['Browse & extract ISO / UDF','تصفح واستخراج ISO / UDF'],desc:['Streaming optical-image files + verification','ملفات الصور الضوئية مع تحقق دون تخزين كامل'],body:['List ISO9660, Joliet and direct-partition UDF images, or extract into a NEW folder with per-file SHA-256 read-back verification. Up to50,000 entries, depth32. Requires a seekable local file; no full image cache. UDF virtual, metadata and sparable partitions, encrypted images and generic NTFS/ext/DMG image browsing are unsupported. A list does not prove bootability.','اعرض ISO9660 وJoliet وUDF ذي الأقسام المباشرة، أو استخرج إلى مجلد جديد مع التحقق من كل ملف ببصمة SHA-256 بعد قراءته. الحد50000 عنصر وعمق32. يحتاج ملفاً محلياً يدعم الوصول العشوائي، دون تخزين الصورة كاملةً. أقسام UDF الافتراضية والبيانات الوصفية والقابلة لإعادة التوزيع والصور المشفرة وتصفح صور NTFS/ext/DMG غير مدعومة. عرض الملفات لا يثبت الإقلاع.']},
{id:'tar',icon:'archive',color:'green',group:'files',name:['TAR / GZIP / XZ archives','أرشيفات TAR / GZIP / XZ'],desc:['List or safely extract compressed TAR','عرض TAR المضغوط أو استخراجه بأمان'],body:['Read TAR, tar.gz and tar.xz, including common PAX and GNU long names. Extraction uses a new folder, blocks links/path traversal/duplicate names, and limits10,000 entries and2 GiB expanded data. XZ decoder memory limit32 MiB. Global PAX, sparse files, passwords, standalone .gz/.xz streams and RAR/7z are unsupported. No package code is executed.','اقرأ TAR وtar.gz وtar.xz مع PAX الشائع وأسماء GNU الطويلة. ينشئ الاستخراج مجلداً جديداً ويمنع الروابط وتجاوز المسارات والأسماء المكررة، بحد10000 عنصر و2 GiB بعد الاستخراج. حد ذاكرة فك XZ هو32 MiB. لا يدعم PAX العام والملفات المتناثرة وكلمات المرور وتدفقات .gz/.xz المستقلة وRAR/7z. لا تُشغّل برمجيات الحزم.']},
{id:'wimSplit',icon:'layers',color:'green',group:'files',name:['Split WIM for FAT32','تقسيم WIM من أجل FAT32'],desc:['Real install.swm parts • verify all parts','أجزاء install.swm حقيقية • تحقق من كل الأجزاء'],body:['Select your own single-part WIM and a destination folder. Verifies the input, streams into a new WIM-parts folder with a3800 MiB target part size, reads each write back and verifies the complete SWM set with wimlib. These are real Windows-compatible split WIM parts, unlike .uwp general split containers. No whole WIM temporary copy. Solid/ESD and WIM resources that cannot fit FAT32 limits may be rejected.','اختر WIM أحادي الجزء الخاص بك ومجلد الوجهة. يتحقق من المصدر ثم يكتب إلى مجلد WIM-parts جديد بحجم مستهدف3800 MiB للجزء، ويقرأ كل كتابة للتحقق ويتحقق من مجموعة SWM كاملةً بواسطة wimlib. هذه أجزاء WIM حقيقية متوافقة مع Windows، بخلاف حاويات .uwp العامة. لا ينشئ نسخة WIM مؤقتة كاملةً. قد تُرفض ESD والصور ذات الضغط الصلب وموارد WIM التي لا تناسب حدود FAT32.']}
);
const ROADMAP=[
{name:['Ventoy custom-layout updates','تحديث تخطيطات Ventoy المخصصة'],body:['Experimental initial MBR installation with user-imported official data is available. A recognized MBR updater is available separately; Experimental default-layout GPT installation/update is available separately; custom-layout updates remain unsupported.','التثبيت الأولي التجريبي بجدول MBR وبيانات رسمية يستوردها المستخدم متاح. يتوفر محدث MBR معروف التخطيط كأداة منفصلة؛ يتوفر تثبيت وتحديث GPT الافتراضي كتجربة منفصلة؛ تحديث التخطيطات المخصصة غير مدعوم.']},
{name:['Additional filesystem engines','محركات أنظمة ملفات إضافية'],body:['F2FS, Btrfs, XFS, UDF, HFS+ and APFS are available experimentally in Format USB with explicit size and allocation limits. ReFS and LUKS remain unavailable. Encryption, optical-media layouts and proprietary formats require separate validated implementations.','تتوفر F2FS وBtrfs وXFS وUDF وHFS+ وAPFS تجريبياً في تهيئة USB مع حدود صريحة للسعة والتخصيص. يظل ReFS وLUKS غير متاحين. التشفير وتخطيطات الأقراص الضوئية والأنظمة الاحتكارية تحتاج تطبيقات منفصلة موثقة.']},
{name:['Partition resizing, creation & mounting','تغيير حجم الأقسام وإنشاؤها وتركيبها'],body:['A bounded basic-layout MBR/GPT converter and a GPT-name/MBR-active metadata editor are available separately. Resizing, arbitrary creation/deletion, extended/dynamic layouts and hidden-partition mounting remain unsupported. Read-only MBR/GPT inspection and single-partition GPT formatting are available.','يتوفر محول MBR/GPT محدود ومحرر اسم GPT وراية نشاط MBR كأدوات منفصلة. تغيير الحجم والإنشاء والحذف العام والتخطيطات الممتدة والديناميكية وتركيب الأقسام المخفية غير مدعومة. فحص MBR/GPT للقراءة وتهيئة قسم واحد بجدول GPT متاحان.']},
{name:['Windows BIOS boot & Windows To Go','إقلاع Windows بنظام BIOS وWindows To Go'],body:['Experimental UEFI file preparation and real WIM splitting are available separately. BIOS boot code, portable Windows installs and customization remain unsupported.','تجهيز ملفات UEFI وتقسيم WIM الحقيقي متاحان كتجربة منفصلة. برمجيات إقلاع BIOS وتثبيت Windows المحمول والتخصيص غير مدعومة.']},
{name:['More persistence layouts & Apple codecs','تخطيطات استمرار وترميزات Apple إضافية'],body:['An empty ext4 persistence-file builder and Ventoy template are available. Automatic distro/boot integration still depends on the exact version. XML UDIF DMG raw/zero/ADC/zlib conversion is now available separately. Additional Apple codecs and encrypted/segmented images remain unsupported.','يتوفر منشئ ملف استمرار ext4 فارغ وقالب Ventoy. الدمج التلقائي للتوزيعة والإقلاع يعتمد على الإصدار الدقيق. يتوفر تحويل XML UDIF DMG الخام والأصفار وADC وzlib كأداة منفصلة. الترميزات الإضافية والصور المشفرة أو المجزأة غير مدعومة.']},
{name:['Advanced defect tests & defrag','اختبارات العيوب المتقدمة وإلغاء التجزئة'],body:['A destructive full-drive pattern capacity test is available. Read-only resumable read-error range mapping is now available separately. Destructive multi-pass defect tests, physical block remapping and defrag remain unimplemented. Never trust a short read benchmark as proof of real capacity. Limited NTFS/ext/F2FS metadata checks/repair are available separately. Defragmentation remains unimplemented.','اختبار سعة كامل بأنماط كتابة وقراءة متاح ويمحو البيانات. تتوفر خريطة نطاقات أخطاء قراءة للقراءة فقط قابلة للاستكمال منفصلة. تظل اختبارات العيوب المدمرة متعددة المراحل وإعادة التعيين العتادي غير منفذة. اختبار سرعة قراءة قصير لا يثبت السعة الحقيقية. تتوفر فحوص وإصلاحات NTFS/ext/F2FS المحدودة كأداة منفصلة. لم يُنفذ إلغاء التجزئة.']},
{name:['PS2 / OPL & advanced PS3 utilities','أدوات PS2 وOPL وPS3 المتقدمة'],body:['Standard PS3 ISO.#/.666NN split/join is available separately. USBExtreme part creation and ul.cfg inspection are available separately; library merging, playlists, artwork and PS3 ISO creation/decryption still need engines. No games, firmware or BIOS files are supplied.','يتوفر تقسيم ودمج ISO.# و.666NN لـPS3 كأداة منفصلة. يتوفر إنشاء أجزاء USBExtreme وفحص ul.cfg منفصلاً؛ ما زال دمج المكتبات والقوائم والصور وإنشاء PS3 ISO أو فك تشفيرها يحتاج محركات منفصلة. لا يوفر التطبيق ألعاباً أو برمجيات أجهزة أو BIOS.']},
{name:['CHD encoding / CD tracks & VCD','إنشاء CHD ومسارات CD وVCD'],body:['ISO ↔ CSO v1 is available separately. ZSO v1 is now available separately; Standalone CHDv5 hard-disk/DVD decoding is now available separately. CHD creation, CD/mixed-track reconstruction and VCD still need separate engines.','يتوفر تحويل ISO ↔ CSO v1 كأداة منفصلة. يتوفر ZSO v1 كأداة منفصلة؛ يتوفر فك CHDv5 المستقل للقرص أو DVD كأداة منفصلة. إنشاء CHD وإعادة بناء CD والمسارات المختلطة وVCD تحتاج محركات أخرى.']},
{name:['Password archives & more image browsers','أرشيفات محمية وتصفح صور إضافية'],body:['Validated 7z/RAR/ZIP/TAR extraction is available separately, plus WIM listing/single-file export and FAT16/32 image browsing. NTFS/ext2/3/4 regular-file image browsing/extraction is now available separately. WIM folder export is available separately. Password/multi-volume archives, full Windows metadata restoration and additional filesystems still need engines.','يتوفر استخراج 7z/RAR/ZIP/TAR بعد الفحص كأداة منفصلة، إضافة إلى عرض WIM وتصدير ملف مفرد وتصفح صور FAT16/32. يتوفر تصفح واستخراج الملفات العادية من صور NTFS/ext2/3/4 كأداة منفصلة. يتوفر تصدير مجلد WIM منفصلاً. ما زالت الأرشيفات المحمية ومتعددة الأجزاء واستعادة بيانات Windows الوصفية كاملةً والأنظمة الإضافية تحتاج محركات منفصلة.']},
{name:['Drive encryption & erase certificates','تشفير الوحدة وشهادات المحو'],body:['Needs a reviewed encryption design, interoperable recovery and explicit sanitization guarantees. Zero-fill with full readback and a separate dated zero-verification record are available, but neither proves certified sanitization or hidden/spare flash erasure.','تحتاج تصميماً تشفيرياً مراجعاً واستعادة متوافقة وضمانات تطهير واضحة. يتوفر الملء بالأصفار وإعادة القراءة الكاملة وسجل تحقق مؤرخ منفصل؛ لا تثبت التطهير المعتمد أو محو خلايا الفلاش المخفية أو الاحتياطية.']}
];
const GUIDES=[
{id:'downloads',icon:'book',name:['Get your own system image','تنزيل صورة النظام بنفسك'],desc:['Official links, file size and the right USB tool','روابط رسمية وحجم الملف وأداة USB المناسبة'],time:['4 min','٤ دقائق'],links:[{id:'windows',name:['Windows · Microsoft','Windows · مايكروسوفت']},{id:'ubuntu',name:['Ubuntu · official site','Ubuntu · الموقع الرسمي']},{id:'fedora',name:['Fedora · official site','Fedora · الموقع الرسمي']},{id:'ventoy',name:['Ventoy · official downloads','Ventoy · التنزيلات الرسمية']}],steps:[
['The app contains tools and these offline guides. Windows, Linux and other operating system images are separate files that you choose yourself. They are never included in the APK or downloaded automatically.','يحتوي التطبيق على الأدوات وهذه الشروحات دون إنترنت. صور Windows وLinux والأنظمة الأخرى ملفات مستقلة تختارها بنفسك. لا تُضمّن في APK ولا تُنزّل تلقائياً.'],
['Open an official site below in your browser. Check the download size before starting: an ISO may use several gigabytes of your internet allowance. You can download it on another device and transfer it locally instead.','افتح موقعاً رسمياً أدناه في المتصفح. راجع حجم التنزيل قبل البدء: قد تستهلك ISO عدة غيغابايت من باقتك. يمكنك تنزيلها على جهاز آخر ثم نقلها محلياً.'],
['Choose the image for the COMPUTER you want to boot, not the processor in your Android phone. On Microsoft’s site choose the ISO disk image rather than a Windows .exe assistant. Save the image away from the USB you plan to erase.','اختر الصورة المناسبة للحاسوب الذي ستقلع عليه، وليس لمعالج هاتف أندرويد. اختر صورة قرص ISO من موقع مايكروسوفت بدلاً من مساعد Windows بصيغة .exe. احفظ الصورة خارج USB الذي ستُمحى بياناته.'],
['If the publisher provides SHA-256, copy it and use Checksums & compare before writing. Use the downloaded file directly; you do not need to download the same image again inside this app.','إذا نشر الناشر بصمة SHA-256، انسخها واستخدم «البصمات والمقارنة» قبل الكتابة. استخدم الملف الذي نزّلته مباشرةً؛ لا تحتاج تنزيل الصورة مجدداً داخل التطبيق.'],
['For a supported hybrid Linux ISO or raw IMG, back up the USB, open Write an image, select your file and the correct USB, then follow the erase confirmation and wait for verification. A regular Windows ISO requires a different preparation method; raw writing is not a Windows installer tool.','لصورة Linux ISO هجينة مدعومة أو IMG خام، انسخ بيانات USB ثم افتح «كتابة صورة نظام» واختر ملفك والوحدة الصحيحة. اتبع تأكيد المحو وانتظر التحقق. تحتاج Windows ISO العادية طريقة تجهيز مختلفة؛ الكتابة الخام ليست أداة تجهيز مثبت Windows.'],
['For a USB that already has Ventoy, use Multi-ISO library to copy your images. Experimental MBR Ventoy installation and Windows UEFI preparation have separate tools; read their limits and test with disposable media first. The Ventoy download link supplies its official package, not Windows or Linux images.','لوحدة USB عليها Ventoy مسبقاً، استخدم «مكتبة أنظمة متعددة» لنسخ صورك. تتوفر أداتان تجريبيتان لتثبيت Ventoy بجدول MBR وتجهيز Windows UEFI؛ اقرأ حدودهما واختبر على وسائط تجريبية أولاً. رابط Ventoy يوفر حزمته الرسمية، وليس صور Windows أو Linux.'] ]},
{id:'start',icon:'plug',name:['Your first USB connection','توصيل USB لأول مرة'],desc:['Cable, permission and safe access','الكابل والإذن والوصول الآمن'],time:['3 min','٣ دقائق'],steps:[
['You need a phone or tablet with USB host/OTG support, a data-capable OTG adapter and a USB flash drive. A charging-only cable will not work.','تحتاج هاتفاً أو جهازاً لوحياً يدعم USB OTG، ومحول بيانات OTG ووحدة فلاش. كابل الشحن فقط لن يعمل.'],
['Connect the USB to the adapter, then connect the adapter to your phone. A portable hard drive may need a powered hub.','صل الوحدة بالمحول ثم صل المحول بالهاتف. قد يحتاج القرص الصلب المحمول موزعاً بمصدر طاقة.'],
['On Home, tap Select a USB, choose the correct device and approve Android’s USB permission prompt. Check the device name and VID:PID.','من الرئيسية اضغط «اختيار USB»، وحدد الجهاز الصحيح ووافق على إذن USB من أندرويد. راجع اسم الجهاز وVID:PID.'],
['Drive information reads directly from the USB. Android Files uses Android’s separate folder permission. One permission does not grant the other.','تقرأ «معلومات الوحدة» من USB مباشرةً. يستخدم تطبيق ملفات أندرويد إذن مجلد منفصلاً من أندرويد. أحد الإذنين لا يمنح الآخر.'],
['Before direct access, stop file transfers and safely eject Android’s mounted volume without unplugging. Review the app’s direct USB confirmation: it can release Android’s kernel driver if a normal claim fails. That failure does not prove another app is open. Reconnect afterwards to restore Files access.','قبل الوصول المباشر أنهِ نقل الملفات وأخرج وحدة أندرويد المركبة بأمان دون نزع الكابل. راجع تأكيد USB المباشر؛ يستطيع فصل برنامج تشغيل أندرويد إذا فشل الحجز العادي. هذا الفشل لا يثبت أن تطبيقاً آخر مفتوح. أعد التوصيل لاحقاً لاستعادة وصول تطبيق الملفات.'] ]},
{id:'choose',icon:'format',name:['Choose a file system','اختيار نظام الملفات'],desc:['FAT32, exFAT, NTFS or ext?','FAT32 أم exFAT أم NTFS أم ext؟'],time:['4 min','٤ دقائق'],steps:[
['A file system is how a drive organizes its files. Formatting creates a new one and removes the old file index. Back up important files first.','نظام الملفات هو طريقة تنظيم الملفات على الوحدة. التهيئة تنشئ نظاماً جديداً وتزيل فهرس الملفات القديم. انسخ ملفاتك المهمة أولاً.'],
['FAT32 works with many TVs, cars, consoles and computers. Each file must be smaller than 4 GiB. This version can create FAT32 drives.','FAT32 يعمل مع كثير من التلفزيونات والسيارات وأجهزة الألعاب والحواسيب. يجب أن يقل حجم كل ملف عن 4 GiB. هذا الإصدار يستطيع إنشاء FAT32.'],
['exFAT is useful for large files and often works with modern phones, Windows and macOS. This version can format exFAT.','exFAT مناسب للملفات الكبيرة وغالباً تدعمه الهواتف الحديثة وWindows وmacOS. يمكن لهذا الإصدار تهيئة exFAT.'],
['NTFS suits Windows. ext2/3/4 suit Linux. Your Android device may not mount these formats; formatting to them would not automatically add Android support.','NTFS مناسب لـWindows وext2/3/4 مناسبة لـLinux. قد لا يركب جهاز أندرويد هذه الأنظمة؛ التهيئة بها لا تضيف دعماً لها تلقائياً.'],
['MBR is an older partition table with broad compatibility. GPT is newer. This version can create MBR or GPT with one data partition. Leave cluster size on Automatic unless a device requires something specific.','MBR جدول أقسام قديم واسع التوافق وGPT أحدث منه. ينشئ هذا الإصدار MBR أو GPT بقسم بيانات واحد. اترك حجم العنقود تلقائياً إلا إذا تطلب جهازك إعداداً محدداً.'] ]},
{id:'flash',icon:'burn',name:['Make a bootable Linux USB','إنشاء USB لإقلاع Linux'],desc:['Pick → check → write → verify','اختر ← تحقق ← اكتب ← اختبر'],time:['5 min','٥ دقائق'],steps:[
['Download an uncompressed hybrid Linux ISO or a raw IMG from the operating system’s official website. Store it on your phone, not on the USB you will erase. No OS files are bundled.','نزّل Linux ISO هجينة غير مضغوطة أو IMG خاماً من موقع النظام الرسمي. احفظها على الهاتف وليس USB الذي ستمحوه. التطبيق لا يتضمن أنظمة تشغيل.'],
['Use SHA-256 checksum to compare the file with the publisher’s checksum. If it differs, do not flash that file.','استخدم «بصمة SHA-256» لمقارنة الملف ببصمة الناشر. إذا اختلفت فلا تكتب هذا الملف.'],
['Back up the USB. Open Write an image, choose the source and select the USB. The drive must be large enough for the full image.','انسخ محتويات USB احتياطياً. افتح «كتابة صورة نظام» واختر المصدر وحدد USB. يجب أن تتسع الوحدة للصورة كاملةً.'],
['Review the native confirmation carefully. Tap Confirm only when the displayed USB is the one you intend to overwrite. The app writes the image and then compares it with the USB.','راجع نافذة التأكيد الأصلية بعناية. اضغط تأكيد فقط عندما تكون الوحدة المعروضة هي التي تريد استبدالها. يكتب التطبيق الصورة ثم يقارنها مع USB.'],
['Keep the drive connected until Write & verification complete appears. On a computer, open its one-time boot menu and choose the USB. Secure Boot compatibility depends on the image.','أبقِ الوحدة متصلة حتى تظهر رسالة اكتمال الكتابة والتحقق. افتح قائمة الإقلاع المؤقتة في الحاسوب واختر USB. توافق Secure Boot يعتمد على الصورة.'],
['A Windows ISO usually needs extraction and special boot setup. This app’s raw writer does not yet prepare Windows installers.','صورة Windows ISO تحتاج عادةً استخراجاً وإعداد إقلاع خاصاً. أداة الكتابة الخام هنا لا تجهّز مثبت Windows بعد.'] ]},
{id:'multi',icon:'layers',name:['Keep several ISOs with Ventoy','عدة صور ISO مع Ventoy'],desc:['For a drive already prepared with Ventoy','لوحدة تم تجهيزها مسبقاً بـVentoy'],time:['4 min','٤ دقائق'],steps:[
['Ventoy is a boot menu installed on a USB. Installing it is different from copying ISO files. Use the separate experimental installer for initial MBR setup; updates are not supported.','Ventoy قائمة إقلاع تُثبت على USB. تثبيته يختلف عن نسخ ملفات ISO. استخدم أداة التثبيت التجريبية المنفصلة للتجهيز الأولي بجدول MBR؛ التحديث غير مدعوم.'],
['Prepare the USB once with an official supported Ventoy installation method. Initial installation usually erases the drive; back up before doing that.','جهّز USB مرة واحدة بطريقة تثبيت رسمية مدعومة من Ventoy. التثبيت الأول يمحو الوحدة عادةً؛ انسخ بياناتك قبل ذلك.'],
['Open Multi-ISO library. Choose the large Ventoy data partition using Android’s folder picker. Do not select the small boot partition.','افتح «مكتبة أنظمة متعددة». اختر قسم بيانات Ventoy الكبير بمنتقي مجلدات أندرويد. لا تختر قسم الإقلاع الصغير.'],
['Tap Add ISO files and select several files. They are copied to your chosen folder. Existing names are not overwritten. Android must be able to read and write the partition.','اضغط «إضافة ملفات ISO» وحدد عدة ملفات. ستُنسخ للمجلد المختار دون استبدال الأسماء الموجودة. يجب أن يستطيع أندرويد قراءة القسم والكتابة عليه.'],
['Wait for copy completion, eject safely, then boot the USB on a computer. Ventoy’s menu should show supported images. Compatibility depends on Ventoy and each image.','انتظر اكتمال النسخ ثم أخرج الوحدة بأمان وأقلع بها على الحاسوب. تعرض قائمة Ventoy الصور المدعومة. التوافق يعتمد على Ventoy وكل صورة.'] ]},
{id:'save',icon:'backup',name:['Back up & restore a drive','نسخ وحدة واستعادتها'],desc:['A complete snapshot, including partitions','صورة كاملة تشمل الأقسام'],time:['4 min','٤ دقائق'],steps:[
['A raw backup is the full drive, not just its visible files. A 32 GB USB needs roughly 32 GB of free space even when nearly empty.','النسخة الخام تشمل الوحدة كاملةً لا ملفاتها الظاهرة فقط. USB بسعة 32 GB يحتاج نحو 32 GB من المساحة الحرة ولو كان شبه فارغ.'],
['Select the USB and open Backup & restore. Save the IMG on your phone or another drive. Never save onto the USB being backed up.','حدد USB وافتح «النسخ والاستعادة». احفظ IMG على الهاتف أو وحدة أخرى. لا تحفظها على نفس USB الذي تنسخه.'],
['Keep the phone charged and USB connected. A failed or cancelled backup is incomplete. Do not use it as your only copy.','أبقِ الهاتف مشحوناً والوحدة متصلة. النسخة الفاشلة أو الملغاة غير مكتملة؛ لا تعتمد عليها كنسختك الوحيدة.'],
['To restore, open Write an image and select the backup IMG. Choose a drive of equal or greater capacity. Restoration erases the destination.','للاستعادة افتح «كتابة صورة نظام» واختر IMG الاحتياطية. حدد وحدة مساوية أو أكبر سعةً. الاستعادة تمحو الوجهة.'],
['Backups are not encrypted. Store them somewhere private. Successful read-back verifies written bytes, but a backup is safest when you also test that your files open.','النسخ الاحتياطية غير مشفرة. احفظها في مكان خاص. القراءة اللاحقة تتحقق من البايتات المكتوبة؛ والأفضل أيضاً اختبار فتح ملفاتك.'] ]},
{id:'safe',icon:'shield',name:['Avoid losing your files','تجنب فقدان ملفاتك'],desc:['Simple checks before a destructive task','فحوص بسيطة قبل عملية تمحو البيانات'],time:['3 min','٣ دقائق'],steps:[
['Check the selected USB name and identifier every time. Unplug other storage devices if you are unsure which is which.','راجع اسم USB ومعرّفه كل مرة. افصل وحدات التخزين الأخرى إذا لم تكن متأكداً من هوية كل منها.'],
['Formatting, raw writing, restoration and zero-fill all change the drive. Copy important files elsewhere before confirming.','التهيئة والكتابة الخام والاستعادة والملء بالأصفار كلها تغير الوحدة. انسخ ملفاتك المهمة لمكان آخر قبل التأكيد.'],
['Cancelling does not undo sectors already written. After a cancellation or cable failure, the USB may need a new format or a complete rewrite.','الإلغاء لا يعيد القطاعات التي كُتبت. بعد الإلغاء أو انقطاع الكابل قد تحتاج الوحدة تهيئة جديدة أو إعادة كتابة كاملة.'],
['Wait for completion and safe removal. Do not accept Android’s “format drive” prompt for a newly written boot image unless you intentionally want to erase it again.','انتظر الاكتمال والإخراج الآمن. لا توافق على طلب أندرويد «تهيئة الوحدة» بعد كتابة صورة إقلاع إلا إذا كنت تريد محوها مجدداً.'],
['Zero-fill does not guarantee secure erasure of flash memory. For highly sensitive data use a validated device-specific sanitization method.','الملء بالأصفار لا يضمن المحو الآمن للفلاش. للبيانات شديدة الحساسية استخدم طريقة تطهير موثوقة خاصة بالجهاز.'] ]},
{id:'trouble',icon:'plug',name:['USB not showing up?','لا تظهر وحدة USB؟'],desc:['Power, cables, permissions and formats','الطاقة والكابلات والأذونات والأنظمة'],time:['3 min','٣ دقائق'],steps:[
['Try a known data-capable OTG adapter and a simple flash drive. Some phones have an OTG setting that must be enabled.','جرّب محول OTG معروفاً بنقل البيانات ووحدة فلاش بسيطة. بعض الهواتف تحتاج تفعيل OTG في الإعدادات.'],
['A disk that repeatedly connects and disconnects may lack power. Try a powered USB hub. Never keep writing through repeated disconnects.','القرص الذي يتصل وينقطع باستمرار قد لا تصله طاقة كافية. جرّب موزع USB بمصدر طاقة. لا تواصل الكتابة مع الانقطاع المتكرر.'],
['For raw tasks the device must use USB mass storage, transparent SCSI and Bulk-Only Transport. MTP phones, UAS-only enclosures, optical drives and multi-LUN readers are not supported.','للمهام الخام يجب دعم USB Mass Storage وSCSI الشفاف وBulk-Only Transport. هواتف MTP وحاويات UAS فقط ومحركات الأقراص الضوئية وقارئات LUN المتعددة غير مدعومة.'],
['For file tasks, Android must mount the filesystem and grant folder access. A drive can appear in the USB device list while remaining unavailable in Android’s Files app.','لمهام الملفات يجب أن يركب أندرويد نظام الملفات وأن يمنح إذن المجلد. قد تظهر الوحدة بقائمة USB ولا تتاح في تطبيق الملفات.'],
['If permission was denied, select the USB again. If a saved folder permission stops working, choose the folder again. Test the drive on a computer before attempting any repair.','إذا رُفض الإذن فحدد USB مجدداً. إذا توقف إذن المجلد المحفوظ فأعد اختياره. اختبر الوحدة على الحاسوب قبل أي محاولة إصلاح.'] ]},
{id:'words',icon:'info',name:['USB words, explained simply','مصطلحات USB ببساطة'],desc:['ISO, IMG, booting, partitions and root','ISO وIMG والإقلاع والأقسام والروت'],time:['3 min','٣ دقائق'],steps:[
['ISO: usually a disc image. Only some ISO files are designed to be written directly onto USB. IMG: a raw disk image containing sectors.','ISO: صورة قرص ضوئي غالباً، وبعض ملفاتها فقط مصمم للكتابة مباشرة على USB. IMG: صورة قرص خام تحتوي قطاعات.'],
['Bootable: a computer can start from the USB. Copying an ISO file onto an ordinary drive is not the same as making it bootable.','قابل للإقلاع: يستطيع الحاسوب البدء من USB. نسخ ISO إلى وحدة عادية لا يساوي جعلها قابلة للإقلاع.'],
['Partition: a section of a drive that looks like a separate disk. Formatting changes a filesystem; partitioning changes the disk layout.','القسم: جزء من الوحدة يبدو كقرص منفصل. التهيئة تغير نظام الملفات، والتقسيم يغير تخطيط القرص.'],
['Cluster: a group of sectors used to allocate files. Bigger clusters can waste space with small files. Automatic is the recommended starting point.','العنقود: مجموعة قطاعات تخصص للملفات. العناقيد الكبيرة قد تهدر مساحة مع الملفات الصغيرة. الخيار التلقائي بداية مناسبة.'],
['Root: privileged Android access. This app’s USB host operations do not require root, but it cannot directly format internal phone storage or the internal SD slot.','الروت: وصول مميز لأندرويد. عمليات USB Host هنا لا تحتاج روت، لكن التطبيق لا يهيئ تخزين الهاتف الداخلي أو منفذ SD الداخلي مباشرةً.'] ]}
];

TOOLS.push(
{id:'capacity',icon:'shield',color:'amber',group:'health',name:['Full capacity test','اختبار السعة الكامل'],desc:['Destructive full-drive write → read','كتابة ثم قراءة كامل الوحدة — يمحو البيانات'],body:['Write unique random-seeded patterns across the whole drive, then read them back. This can reveal wrapping fake-capacity controllers and corrupted data. ALL files and partitions are destroyed. It may take hours and stops at the first mismatch or I/O error. A pass is not a guarantee of future drive health.','اكتب أنماطاً فريدة على كامل الوحدة ثم اقرأها. قد يكشف ذلك متحكمات السعة المزيفة والبيانات التالفة. ستُحذف كل الملفات والأقسام. قد يستغرق ساعات ويتوقف عند أول اختلاف أو خطأ نقل. النجاح لا يضمن صحة الوحدة مستقبلاً.']},
{id:'extract',icon:'archive',color:'green',group:'files',name:['Extract ZIP','استخراج ZIP'],desc:['Safe paths • new folder • no overwrite','مسارات آمنة • مجلد جديد • دون استبدال'],body:['Extract an unencrypted ZIP into a newly created folder inside your chosen destination. Rejects path traversal, absolute paths and duplicate names. Limit: 10,000 entries and 2 GiB expanded data. Existing files are not overwritten. Incomplete extraction is removed where the storage provider allows it. RAR, 7z, WIM and password ZIP are unsupported.','استخرج ZIP غير مشفر في مجلد جديد داخل وجهتك المختارة. يرفض تجاوز المسارات والمسارات المطلقة والأسماء المكررة. الحد 10000 عنصر و2 GiB بعد الاستخراج. لا يستبدل الملفات الموجودة. يُحذف الاستخراج غير المكتمل حيث يسمح مزود التخزين. RAR و7z وWIM وZIP المحمي غير مدعومة.']}
);
GUIDES.push({id:'advanced',icon:'shield',name:['Test capacity & extract safely','اختبار السعة والاستخراج الآمن'],desc:['Advanced tools with clear limits','أدوات متقدمة بحدود واضحة'],time:['4 min','٤ دقائق'],steps:[
['Use a disposable or fully backed-up USB for the full capacity test. It overwrites the entire drive before reading it back, so hidden wraparound corruption can be detected.','استخدم USB تجريبياً أو منسوخاً بالكامل لاختبار السعة. يُكتب على الوحدة كاملةً قبل القراءة لكشف فساد الالتفاف المخفي.'],
['A mismatch or I/O error means do not trust the drive. Format it after the test only if you intentionally want to reuse it. A passing test does not prove lifetime reliability.','الاختلاف أو خطأ النقل يعني عدم الثقة بالوحدة. هيئها بعد الاختبار فقط إذا أردت استخدامها مجدداً. النجاح لا يثبت موثوقيتها طوال عمرها.'],
['For ZIP extraction choose a source stored on the phone and grant access to a destination folder. The app creates a new subfolder and will not overwrite existing files.','لاستخراج ZIP اختر مصدراً محفوظاً على الهاتف واسمح بالوصول لمجلد وجهة. ينشئ التطبيق مجلداً فرعياً جديداً ولا يستبدل الملفات.'],
['If an archive exceeds 2 GiB expanded or 10,000 entries, extraction stops. Unsupported, encrypted or damaged archives can fail; the app attempts to remove the new partial folder.','إذا تجاوز الأرشيف 2 GiB بعد الاستخراج أو 10000 عنصر يتوقف الاستخراج. قد تفشل الأرشيفات غير المدعومة أو المشفرة أو التالفة؛ يحاول التطبيق حذف المجلد الجزئي الجديد.']
]});

TOOLS.push(
{id:"randomread",icon:"speed",color:"blue",group:"health",name:["Random read benchmark", "قياس القراءة العشوائية"],desc:["512 random reads • no writes", "٥١٢ قراءة عشوائية • دون كتابة"],body:["Read 512 scattered 64 KiB requests and report throughput and average latency. This measures your entire phone, cable and drive connection; it is not a health guarantee.", "اقرأ ٥١٢ طلباً عشوائياً بحجم 64 KiB واعرض السرعة ومتوسط التأخير. القياس يشمل الهاتف والكابل والوحدة؛ لا يضمن صحة الوحدة."]},
{id:"scan",icon:"shield",color:"blue",group:"health",name:["Read surface scan", "فحص سطح القراءة"],desc:["Read every sector • no writes", "قراءة كل القطاعات • دون كتابة"],body:["Read the entire USB, stopping at the first I/O error. May take hours. Does not detect every fake-capacity device or preserve a bad-block map.", "اقرأ USB كاملاً مع التوقف عند أول خطأ نقل. قد يستغرق ساعات. لا يكشف كل وحدات السعة المزيفة ولا يحفظ خريطة القطاعات التالفة."]},
{id:"hex",icon:"info",color:"blue",group:"health",name:["Sector viewer", "عارض القطاعات"],desc:["First sector in hex • read only", "القطاع الأول بالست عشري • قراءة فقط"],body:["Inspect the first physical sector as hexadecimal and printable ASCII. This viewer cannot edit sectors.", "افحص القطاع الأول بالست عشري ونص ASCII القابل للطباعة. لا يغير العارض القطاعات."]},
{id:"imageinfo",icon:"info",color:"blue",group:"files",name:["Image inspector", "فحص صورة القرص"],desc:["MBR, GPT & ISO9660 signatures", "تواقيع MBR وGPT وISO9660"],body:["Read the first 64 KiB of an IMG or ISO. Identify common signatures and MBR entries. Does not browse files or prove the image will boot.", "اقرأ أول 64 KiB من IMG أو ISO وتعرف على التواقيع وإدخالات MBR. لا يتصفح الملفات ولا يثبت قابلية الإقلاع."]},
{id:"split",icon:"layers",color:"blue",group:"files",name:["Split large file", "تقسيم ملف كبير"],desc:["Checksummed parts • bounded memory", "أجزاء ببصمة تحقق • ذاكرة محدودة"],body:["Split any file into .uwp containers with embedded ordering and SHA-256. Choose a destination folder; a new subfolder is created. Parts cannot be flashed or used directly by Ventoy. Rejoin them first. Requires space for the complete source plus headers and 64 MiB reserve.", "قسم أي ملف إلى حاويات .uwp مع الترتيب وبصمة SHA-256. اختر وجهة لإنشاء مجلد فرعي. لا تصلح الأجزاء للكتابة كصور أو للاستخدام مباشرة في Ventoy؛ ادمجها أولاً. تحتاج مساحة الملف كاملاً مع الترويسات واحتياط 64 MiB."]},
{id:"join",icon:"layers",color:"blue",group:"files",name:["Rejoin split parts", "دمج أجزاء الملف"],desc:["Validate order & SHA-256", "التحقق من الترتيب وSHA-256"],body:["Select every .uwp part created by this app, in any order. Missing, duplicate or damaged parts fail. A new Joined-*.img file is created and partial output is removed on failure where permitted. Restore the original extension if the source was not an IMG.", "حدد كل أجزاء .uwp المنشأة بهذا التطبيق بأي ترتيب. يفشل الدمج عند نقص أو تكرار أو تلف الأجزاء. يُنشأ Joined-*.img جديد وتُحذف النتيجة الجزئية عند الفشل حيث يسمح المزود. أعد الامتداد الأصلي إذا لم يكن المصدر IMG."]},
{id:"createzip",icon:"archive",color:"blue",group:"files",name:["Create ZIP archive", "إنشاء أرشيف ZIP"],desc:["Select files • no overwrites", "حدد الملفات • دون استبدال"],body:["Choose a destination folder, then select files to package in a new ZIP. Flat files only; no folders or encryption. Up to 1,000 files and 2 GiB of source data. Duplicate names are rejected.", "اختر الوجهة ثم الملفات لإنشاء ZIP جديد. ملفات فقط دون مجلدات أو تشفير. الحد ١٠٠٠ ملف و2 GiB من البيانات. تُرفض الأسماء المكررة."]},
{id:"diagnostic",icon:"plug",color:"blue",group:"health",name:["Compatibility check", "فحص التوافق"],desc:["Android, USB host & engine availability", "أندرويد ومضيف USB وتوفر المحركات"],body:["Check Android API level, USB host capability, folder access and bundled native engine availability. This does not write to a drive or inspect personal files.", "افحص مستوى أندرويد ودعم USB Host وإذن المجلد وتوفر المحركات المضمنة. لا يكتب على الوحدة ولا يفحص الملفات الشخصية."]},
{id:"report",icon:"archive",color:"blue",group:"health",name:["Export workshop report", "تصدير تقرير الورشة"],desc:["Save diagnostics & operation history", "حفظ التشخيصات وسجل العمليات"],body:["Save a text report containing compatibility details and the most recent 20 operation records. Reports may contain filenames, identifiers and errors; review before sharing. No automatic upload.", "احفظ تقريراً نصياً بتفاصيل التوافق وآخر ٢٠ عملية. قد يحتوي أسماء الملفات والمعرفات والأخطاء؛ راجعه قبل المشاركة. لا يرفع تلقائياً."]}
);
GUIDES.push({id:'workshop',icon:'format',name:['Choose advanced formats & file tools','اختيار الأنظمة والأدوات المتقدمة'],desc:['Understand compatibility before starting','افهم التوافق قبل البدء'],time:['5 min','٥ دقائق'],steps:[
['Use FAT32 for broad device compatibility, exFAT for large files where supported, NTFS mainly for Windows, and ext mainly for Linux. Android may not mount NTFS/ext even after a successful format.','استخدم FAT32 للتوافق الواسع وexFAT للملفات الكبيرة حيث يُدعم وNTFS غالباً لويندوز وext غالباً للينكس. قد لا يركب أندرويد NTFS/ext بعد نجاح التهيئة.'],
['Use Automatic cluster size. ext always uses 4 KiB blocks. FAT12/FAT16 are for small or legacy devices; larger USB drives should use another supported filesystem.','استخدم حجم العنقود التلقائي. يستخدم ext كتلاً بحجم 4 KiB. FAT12/FAT16 للأجهزة الصغيرة أو القديمة؛ اختر نظاماً آخر للوحدات الكبيرة.'],
['Back up important files. Check the selected device, filesystem and partition table. Formatting destroys the existing layout; cancellation cannot undo earlier writes. Native NTFS/ext engines need disposable-media testing.','انسخ الملفات المهمة. راجع الجهاز والنظام وجدول الأقسام. التهيئة تمحو التخطيط السابق ولا يعكس الإلغاء الكتابات السابقة. تحتاج محركات NTFS/ext اختبار وسائط تجريبية.'],
['For splitting, pick the source and a different destination folder. Select a part size, then split. These .uwp parts are containers; use Rejoin split parts and select ALL parts before flashing or opening the original file.','للتقسيم اختر المصدر ومجلد وجهة مختلفاً وحجم الجزء ثم ابدأ. أجزاء .uwp حاويات؛ استخدم الدمج وحدد كل الأجزاء قبل كتابة الصورة أو فتح الملف الأصلي.'],
['Read scans and speed tests never write to the USB. A readable drive can still have fake capacity. Only the destructive capacity test writes unique patterns across the whole drive.','فحوص القراءة والسرعة لا تكتب على USB. قد تكون السعة مزيفة رغم نجاح القراءة. اختبار السعة الذي يمحو البيانات وحده يكتب أنماطاً فريدة على كامل الوحدة.']
]});

TOOLS.push({id:'clone',icon:'backup',color:'amber',group:'disk',name:['Clone USB','نسخ USB'],desc:['Phone IMG or another USB • full verification','صورة على الهاتف أو USB أخرى • تحقق كامل'],body:['For a phone image, select the source USB and tap Clone USB to phone (.img). Save on internal storage with free space equal to the full USB capacity; it reads and compares every byte. For USB-to-USB, connect both drives through a powered OTG hub, select a different destination and grant its permission. All destination data is destroyed. Equal capacity and sector size are required to preserve partition layouts safely. The source is read twice; the destination is written once and fully compared afterward.','لصورة على الهاتف اختر USB المصدر واضغط نسخ USB إلى الهاتف (.img). احفظ على التخزين الداخلي بمساحة تساوي سعة USB كاملةً؛ يقرأ ويقارن كل بايت. لنسخ USB إلى USB صل الوحدتين بموزع OTG بمصدر طاقة واختر وجهة مختلفة وامنح إذنها. تُحذف كل بيانات الوجهة. يلزم تطابق السعة وحجم القطاع للحفاظ على تخطيط الأقسام. يُقرأ المصدر مرتين وتُكتب الوجهة مرة ثم تُقارن كاملةً.']});

TOOLS.push({id:'checksums',icon:'hash',color:'green',group:'files',name:['Checksums & compare','البصمات والمقارنة'],desc:['MD5, SHA-1, SHA-256 & SHA-512','MD5 وSHA-1 وSHA-256 وSHA-512'],body:['Calculate four checksums in one read. Optionally paste the expected SHA-256 from a trusted source to compare. MD5 and SHA-1 are provided only for legacy file comparison, not security or authenticity. Checksums do not establish who published a file.','احسب أربع بصمات بقراءة واحدة. يمكنك لصق SHA-256 المتوقعة من مصدر موثوق للمقارنة. MD5 وSHA-1 للمقارنة القديمة فقط وليستا للأمان أو إثبات الأصالة. البصمة لا تثبت هوية ناشر الملف.']},{id:'planner',icon:'book',color:'blue',group:'disk',name:['Help me choose','ساعدني في الاختيار'],desc:['A goal-based workshop guide','دليل الورشة حسب هدفك'],body:['Choose what you want to do. This opens the relevant tool with an explanation; it never starts a USB operation automatically. Back up before changing a drive.','اختر هدفك لفتح الأداة المناسبة مع شرح. لا يبدأ أي عملية USB تلقائياً. انسخ بياناتك قبل تغيير الوحدة.']});
GUIDES.push({id:'windowsGuide',icon:'burn',name:['Prepare Windows UEFI step by step','تجهيز Windows UEFI خطوة بخطوة'],desc:['Your ISO, FAT32 and real split WIM files','صورتك ISO وFAT32 وأجزاء WIM حقيقية'],time:['6 min','٦ دقائق'],steps:[
['Download your ISO from Microsoft in your browser. Choose the architecture for the PC, not your phone, and compare SHA-256 when available. The app supplies no Windows image or license.','نزّل ISO من مايكروسوفت في المتصفح. اختر معمارية الحاسوب وليس الهاتف، وقارن SHA-256 إن توفرت. لا يوفر التطبيق صورة Windows أو ترخيصه.'],
['Back up a disposable USB, then format its data partition as FAT32. Formatting erases files. For direct formatting, stop other transfers, safely eject the mounted Android volume without unplugging, review direct-driver access and tap Confirm only for the correct drive.','انسخ بيانات USB تجريبية ثم هيئ قسم بياناتها بنظام FAT32. التهيئة تمحو الملفات. للتهيئة المباشرة أنهِ النقل وأخرج الوحدة المركبة في أندرويد بأمان دون نزع الكابل، وراجع الوصول المباشر واضغط تأكيد فقط للوحدة الصحيحة.'],
['Safely reconnect the USB so Android can mount it again. Select Windows UEFI preparation, choose your ISO from another device/location, then select the EMPTY USB ROOT with the folder picker. A subfolder or phone storage is rejected. You must confirm FAT32 yourself; the picker cannot prove it.','أعد توصيل USB بأمان ليتمكن أندرويد من تركيبها. افتح تجهيز Windows UEFI واختر ISO من جهاز أو موقع آخر، ثم اختر جذر USB الفارغ بمنتقي المجلدات. يُرفض المجلد الفرعي وتخزين الهاتف. يجب أن تتحقق من FAT32 بنفسك؛ المنتقي لا يثبته.'],
['Review and start. The app checks the image layout, copies files and verifies them. If install.wim is over the FAT32 file limit, it streams real install.swm, install2.swm and later parts into sources and verifies the complete set. Keep every part.','راجع وابدأ. يفحص التطبيق تخطيط الصورة وينسخ الملفات ويتحقق منها. إذا تجاوز install.wim حد FAT32، يكتب أجزاء install.swm وinstall2.swm وما بعدها مباشرةً في sources ويتحقق من المجموعة كاملةً. احتفظ بكل الأجزاء.'],
['For a standalone WIM, choose Split WIM for FAT32. The new output folder contains real SWM parts. Copy all parts into your Windows USB sources folder. Generic .uwp split files are different and cannot replace SWM.','لملف WIM مستقل اختر تقسيم WIM من أجل FAT32. يحتوي مجلد الناتج الجديد على أجزاء SWM حقيقية. انسخها كلها إلى مجلد sources في USB الخاص بـWindows. ملفات .uwp العامة مختلفة ولا تستبدل SWM.'],
['Wait until verification finishes, then safely eject and test on the PC using its UEFI USB boot menu. BIOS boot, Windows To Go and activation are not included. This integration is experimental; actual PC/firmware compatibility remains to be tested.','انتظر انتهاء التحقق ثم أخرج الوحدة بأمان واختبر على الحاسوب من قائمة إقلاع UEFI USB. لا يشمل إقلاع BIOS أو Windows To Go أو التفعيل. هذا التكامل تجريبي؛ توافق الحاسوب والبرنامج الثابت يحتاج اختباراً فعلياً.'] ]});
GUIDES.push({id:'ventoyGuide',icon:'layers',name:['Install Ventoy from your package','تثبيت Ventoy من حزمتك'],desc:['Experimental initial MBR installation','تثبيت MBR أولي تجريبي'],time:['5 min','٥ دقائق'],steps:[
['Open the official Ventoy download site from Get your own system image. Download the Linux tar.gz package and compare its published SHA-256. Do not extract or run its programs on your phone.','افتح موقع تنزيل Ventoy الرسمي من دليل تنزيل صورة النظام بنفسك. نزّل حزمة Linux بصيغة tar.gz وقارن SHA-256 المنشورة. لا تفك برامجها ولا تشغّلها على الهاتف.'],
['Open Install Ventoy, choose the package and tap Import. Only validated boot DATA is cached, about33 MiB plus temporary import space; no scripts or imported executables run. The APK does not include Ventoy boot payloads or OS images.','افتح تثبيت Ventoy واختر الحزمة ثم اضغط استيراد. تُحفظ بيانات الإقلاع المتحقق منها فقط، نحو33 MiB إضافةً لمساحة مؤقتة للاستيراد؛ لا تُشغّل سكربتات أو برامج مستوردة. لا يتضمن APK ملفات إقلاع Ventoy أو صور الأنظمة.'],
['Use a disposable USB, back up everything, stop transfers and safely eject the Android-mounted volume without removing the cable. Select the correct USB in the app. You need512-byte sectors and at least128 MiB; leave enough room for your later ISOs.','استخدم USB تجريبية وانسخ كل بياناتها وأنهِ النقل وأخرج الوحدة المركبة في أندرويد بأمان دون نزع الكابل. اختر USB الصحيحة في التطبيق. تحتاج قطاع 512 بايت وسعة128 MiB على الأقل؛ اترك مساحة لصور ISO التي ستضيفها.'],
['Review installation and driver release, then tap Confirm. This removes ALL partitions and creates MBR with exFAT data and32 MiB EFI. Every write is read back; the final table is committed last. Power loss still leaves an incomplete drive.','راجع التثبيت وفصل برنامج التشغيل ثم اضغط تأكيد. يمحو هذا كل الأقسام وينشئ MBR ببيانات exFAT وقسم EFI بحجم32 MiB. تُقرأ كل كتابة للتحقق ويُعتمد الجدول النهائي أخيراً. انقطاع الطاقة يترك الوحدة غير مكتملة.'],
['After completion safely reconnect, then use Multi-ISO library to select the large data partition and copy your own ISO files. Test on a PC. Use the separate GPT/update tools for supported layouts. Data-preserving initial installation and Secure Boot guarantees are not provided; physical boot remains unverified.','بعد الاكتمال أعد التوصيل بأمان ثم استخدم مكتبة أنظمة متعددة لاختيار قسم البيانات الكبير ونسخ صور ISO الخاصة بك. اختبر على حاسوب. استخدم أدوات GPT والتحديث المنفصلة للتخطيطات المدعومة. لا يدعم التثبيت الأولي مع حفظ البيانات أو ضمان Secure Boot؛ الإقلاع الفعلي غير متحقق بعد.'] ]});
GUIDES.push({id:'imagesGuide',icon:'archive',name:['Read images and compressed archives','قراءة الصور والأرشيفات المضغوطة'],desc:['List first, then extract into a new folder','اعرض أولاً ثم استخرج إلى مجلد جديد'],time:['4 min','٤ دقائق'],steps:[
['Choose Browse & extract ISO/UDF for a supported optical image, or TAR/GZIP/XZ for .tar, .tar.gz or .tar.xz. Select your own local file with the source picker. Nothing downloads automatically.','اختر تصفح واستخراج ISO/UDF لصورة ضوئية مدعومة، أو TAR/GZIP/XZ لملفات .tar و.tar.gz و.tar.xz. حدد ملفك المحلي بمنتقي المصدر. لا يبدأ تنزيل تلقائي.'],
['Tap List files to inspect names without creating files. Up to200 names appear in the report; scanning still validates the full bounded tree. Listing does not prove bootability.','اضغط عرض الملفات لفحص الأسماء دون إنشاء ملفات. يعرض التقرير200 اسم على الأكثر؛ يظل الفحص يتحقق من الشجرة الكاملة ضمن الحدود. العرض لا يثبت الإقلاع.'],
['Choose a local destination with enough free space and tap Extract. A new folder is created; existing files are not overwritten. ISO/UDF extraction reads copied files back and checks SHA-256. TAR checks archive headers and compression integrity but does not perform an extra per-file readback pass.','اختر وجهة محلية بمساحة كافية ثم اضغط استخراج. ينشئ مجلداً جديداً دون استبدال الملفات. يقرأ استخراج ISO/UDF الملفات المنسوخة ويتحقق ببصمة SHA-256. يفحص TAR ترويسات الأرشيف وسلامة الضغط لكنه لا يجري قراءة تحقق إضافية لكل ملف.'],
['If unsupported, read the error details. Not every UDF layout, encrypted archive or disk filesystem is supported. TAR extraction is limited to2 GiB and10,000 entries and refuses links, duplicates and unsafe paths. Cancel can leave outputs that the provider refuses to delete; inspect only that new output folder.','إذا كانت الصيغة غير مدعومة، اقرأ تفاصيل الخطأ. ليست كل تخطيطات UDF أو الأرشيفات المشفرة أو أنظمة الأقراص مدعومة. حد استخراج TAR هو2 GiB و10000 عنصر ويرفض الروابط والأسماء المكررة والمسارات الخطرة. قد يترك الإلغاء نواتج يرفض المزود حذفها؛ افحص مجلد الناتج الجديد فقط.'] ]});
const RECIPES=[
{goal:['Share large files','مشاركة ملفات كبيرة'],why:['exFAT supports files larger than4GiB. Check every device supports it first.','يدعم exFAT ملفات أكبر من4GiB. تحقق من دعم كل الأجهزة أولاً.'],tool:'format',fs:'exfat'},
{goal:['Use an older TV or car stereo','استخدام تلفاز أو مسجل سيارة قديم'],why:['FAT32 is widely supported, but each file must be below4GiB. Check the device manual for capacity and partition limits.','FAT32 واسع الدعم لكن يجب أن يقل كل ملف عن4GiB. راجع دليل الجهاز لحدود السعة والأقسام.'],tool:'format',fs:'fat32'},
{goal:['Store files mainly on Windows','تخزين ملفات لويندوز غالباً'],why:['NTFS supports large files. The formatter is experimental; Android may not mount the result.','يدعم NTFS الملفات الكبيرة. محرك التهيئة تجريبي وقد لا يركب أندرويد النتيجة.'],tool:'format',fs:'ntfs'},
{goal:['Use a Linux data drive','استخدام وحدة بيانات للينكس'],why:['ext4 is commonly used by Linux. This formatter uses4KiB blocks. Other devices may not read the drive.','يُستخدم ext4 عادةً في لينكس. تستخدم هذه الأداة كتلاً بحجم4KiB. قد لا تقرأ الأجهزة الأخرى الوحدة.'],tool:'format',fs:'ext4'},
{goal:['Boot a Linux IMG or hybrid ISO','الإقلاع بصورة Linux IMG أو ISO هجينة'],why:['Use raw write only when the publisher says the image supports it. Ordinary Windows ISO files need a different installer engine.','استخدم الكتابة الخام فقط إذا أكد الناشر دعم الصورة لها. تحتاج ISO ويندوز العادية محرك تثبيت مختلفاً.'],tool:'write'},
{goal:['Keep several ISOs on existing Ventoy','حفظ عدة ISO على Ventoy موجود'],why:['Ventoy must already be installed. Choose its data folder; adding files does not install Ventoy.','يجب تثبيت Ventoy مسبقاً. اختر مجلد بياناته؛ إضافة الملفات لا تثبّت Ventoy.'],tool:'ventoy'},
{goal:['Save a complete drive backup','حفظ نسخة كاملة من الوحدة'],why:['A raw backup includes unused sectors and needs free space equal to the full drive plus reserve. Use another destination.','تشمل النسخة الخام القطاعات غير المستخدمة وتحتاج مساحة تساوي كامل الوحدة مع الاحتياط. استخدم وجهة مختلفة.'],tool:'backup'}
];

const LEARN_GOALS=[
{id:'bootcomputer',name:['How can I make a USB boot a computer?','كيف أجعل USB يُقلع حاسوباً؟'],answer:['Download your chosen system image from its official publisher using your browser. For a supported hybrid Linux ISO or raw IMG, use Write an image. For a normal Windows ISO, use Windows UEFI preparation on an empty FAT32 USB. Back up the USB before writing, then test it from the computer’s USB boot menu. The app supplies tools and instructions; you choose your own system image.','نزّل صورة النظام الذي تختاره من ناشره الرسمي عبر المتصفح. لصورة Linux ISO هجينة مدعومة أو IMG خام استخدم كتابة صورة نظام. لصورة Windows ISO العادية استخدم تجهيز Windows UEFI على USB فارغ بنظام FAT32. انسخ بيانات USB قبل الكتابة ثم اختبره من قائمة إقلاع الحاسوب. يوفر التطبيق الأدوات والتعليمات؛ أنت تختار صورة النظام.'],tools:['write','windows'],guide:'downloads'},
{id:'copyphone',name:['How can I clone my USB to my phone?','كيف أنسخ USB كاملةً إلى هاتفي؟'],answer:['Open Clone USB, choose Clone USB to phone (.img), and save the new image on internal storage. Free space must cover the full USB capacity, including unused sectors. The USB is read without changing it. The app reads the saved image back and compares every byte. Restore later with Write an image.','افتح نسخ USB واختر نسخ USB إلى الهاتف (.img)، واحفظ الصورة الجديدة على التخزين الداخلي. يجب أن تكفي المساحة لسعة USB كاملةً، بما فيها القطاعات غير المستخدمة. يُقرأ USB دون تغييره. يقرأ التطبيق الصورة المحفوظة ويقارن كل بايت. للاستعادة لاحقاً استخدم كتابة صورة نظام.'],tools:['clone','write'],guide:'save'},
{id:'largefiles',name:['How can I store a file larger than 4 GiB?','كيف أحفظ ملفاً أكبر من 4 GiB؟'],answer:['Back up the drive before changing its filesystem. exFAT or NTFS can hold large files; FAT32 has a single-file limit of4 GiB minus1 byte. Android may not mount NTFS even after a valid format. Use exFAT when your phone and target devices support it. Formatting erases the current partition layout.','انسخ الوحدة قبل تغيير نظام الملفات. يستطيع exFAT أو NTFS حفظ ملفات كبيرة؛ حد الملف في FAT32 هو4 GiB ناقص بايت. قد لا يركّب أندرويد NTFS حتى بعد تهيئة صحيحة. استخدم exFAT إذا دعمه الهاتف والأجهزة المطلوبة. التهيئة تمحو تخطيط الأقسام الحالي.'],tools:['format'],guide:'choose'},
{id:'manysystems',name:['How can one USB hold several system images?','كيف أحفظ عدة صور أنظمة على USB واحد؟'],answer:['Prepare Ventoy using the separate experimental installer and your official package, or use an already prepared Ventoy drive. Reconnect it safely, open Multi-ISO library, choose the large data partition and copy your own ISO files. Installing Ventoy erases existing partitions; copying ISOs afterwards does not reinstall it.','جهّز Ventoy بالأداة التجريبية المنفصلة وحزمتك الرسمية، أو استخدم وحدة مجهزة مسبقاً. أعد توصيلها بأمان وافتح مكتبة أنظمة متعددة واختر قسم البيانات الكبير وانسخ صور ISO الخاصة بك. تثبيت Ventoy يمحو الأقسام الحالية؛ نسخ ISO لاحقاً لا يعيد تثبيته.'],tools:['ventoyInstall','ventoy'],guide:'ventoyGuide'}
];

TOOLS.push(
{id:'ventoyUpdate',icon:'layers',color:'blue',group:'boot',name:['Update Ventoy MBR','تحديث Ventoy MBR'],desc:['Recognized layouts • boot/EFI only • experimental','تخطيطات معروفة • الإقلاع وEFI فقط • تجريبي'],body:['Import your official package using Install Ventoy first, then select the existing Ventoy USB. This experimental updater supports recognized two-partition MBR layouts with 512-byte sectors. It preserves data-partition sectors, partition entries, disk identity and reserved core sectors; every boot/EFI write is read back. Back up first: interrupted updates can prevent boot. GPT, custom and unknown boot layouts are rejected. Physical boot testing is still required.','استورد حزمتك الرسمية من تثبيت Ventoy أولاً ثم اختر USB عليها Ventoy. يدعم هذا المحدث التجريبي تخطيط MBR معروفاً بقسمين وقطاع 512 بايت. يحفظ قطاعات البيانات وإدخالات الأقسام وهوية القرص وقطاعات الإقلاع المحجوزة، ويتحقق بقراءة كل كتابة للإقلاع وEFI. انسخ بياناتك أولاً؛ التوقف قد يمنع الإقلاع. تُرفض GPT والتخطيطات المخصصة أو غير المعروفة. لا يزال اختبار الإقلاع الفعلي مطلوباً.']},
{id:'convertTable',icon:'format',color:'amber',group:'disk',name:['Convert MBR / GPT','تحويل MBR / GPT'],desc:['Basic data partitions • verified table backup','أقسام بيانات بسيطة • نسخة جدول متحقق منها'],body:['Experimental conversion without moving filesystem sectors. First saves and verifies a small table backup on phone storage. Requires512-byte sectors, supported basic partition types, starts at or beyond LBA2048 and33unused tail sectors. GPT→MBR supports at most4partitions. MBR→GPT cannot shrink full-size filesystems and rejects drives lacking space for GPT metadata. Extended/dynamic/special attributes are unsupported. Boot code and OS boot settings are not converted. Back up important files separately; the small table record is not a full filesystem or bootloader backup.','تحويل تجريبي دون نقل قطاعات أنظمة الملفات. يحفظ أولاً نسخة صغيرة متحققاً منها للجدول على الهاتف. يحتاج قطاع 512 بايت وأنواع أقسام بسيطة مدعومة وبداية عند LBA2048 أو بعده و33قطاعاً غير مستخدم في النهاية. حد GPT إلى MBR أربعة أقسام. لا يصغر MBR إلى GPT أنظمة الملفات الممتدة للنهاية، ويرفض عدم كفاية مساحة بيانات GPT. التخطيطات الممتدة والديناميكية والسمات الخاصة غير مدعومة. لا تُحول برمجيات أو إعدادات إقلاع النظام. انسخ الملفات المهمة بشكل منفصل؛ سجل الجدول الصغير ليس نسخة كاملة للنظام أو محمل الإقلاع.']},
{id:'persistence',icon:'backup',color:'green',group:'boot',name:['Create Linux persistence file','إنشاء ملف استمرار Linux'],desc:['Empty ext4 data + Ventoy JSON template','بيانات ext4 فارغة وقالب إعداد Ventoy'],body:['Generate an empty ext4 persistence.dat and a Ventoy JSON template in a NEW folder. No Linux system is supplied or downloaded. Choose the label required by your specific distro/version; persistence also adds / union to persistence.conf for compatible Debian/Kali images. The filesystem is checked after creation. Copy persistence.dat to your Ventoy data root and merge the template into /ventoy/ventoy.json without replacing existing settings. ISO path must match exactly. Boot/distro compatibility needs testing.','أنشئ persistence.dat بنظام ext4 فارغاً وقالب JSON لـVentoy داخل مجلد جديد. لا يوفر أو ينزل نظام Linux. اختر الاسم الذي تحتاجه توزيعتك وإصدارها؛ يضيف اختيار persistence ملف persistence.conf وفيه / union لصور Debian/Kali المتوافقة. يُفحص النظام بعد الإنشاء. انسخ persistence.dat إلى جذر بيانات Ventoy وادمج القالب في /ventoy/ventoy.json دون استبدال الإعدادات الحالية. يجب أن يطابق مسار ISO تماماً. توافق الإقلاع والتوزيعة يحتاج اختباراً.']},
{id:'fileBenchmark',icon:'speed',color:'green',group:'health',name:['File write & read benchmark','قياس كتابة وقراءة ملف'],desc:['New temporary file • byte verification • cleanup','ملف مؤقت جديد • مقارنة بايتات • تنظيف'],body:['Choose a mounted local folder. Writes a NEW temporary16/64/256MiB file, flushes/syncs it, reads every byte back, reports throughput and removes only that file. Existing files and raw USB sectors are untouched. Requires the chosen size plus64MiB free reserve. Results measure the Android provider path and include OS/cache effects; they are not a certified physical-drive speed or capacity result.','اختر مجلداً محلياً مركباً. يكتب ملفاً مؤقتاً جديداً بحجم16 أو64 أو256MiB ويفرغه ويزامنه، ثم يقرأ كل بايت ويعرض السرعة ويحذف ذلك الملف فقط. لا يغير الملفات الموجودة أو قطاعات USB الخام. يحتاج الحجم المختار مع64MiB احتياطي حرة. تقيس النتيجة مسار مزود أندرويد وتتأثر بالتخزين المؤقت؛ ليست شهادة سرعة أو سعة فعلية للقرص.']},
{id:'wimExplore',icon:'archive',color:'blue',group:'files',name:['Browse & export WIM files','تصفح وتصدير ملفات WIM'],desc:['Image index + exact file path • streaming','رقم الصورة ومسار الملف الدقيق • دون تخزين كامل'],body:['Select a seekable single-part WIM or supported ESD and an image index. List its paths, then enter one exact regular-file path to export into a NEW folder. No wildcard expansion, directory tree or reparse-point extraction. Output is streamed with per-write readback and a SHA256 report, without a whole-WIM cache. Export limit4GiB minus1byte. Split WIM sets need external resources and are not supported here. Large listings are bounded to the native report limit.','اختر WIM أحادي الجزء يدعم الوصول العشوائي أو ESD مدعوماً ورقم صورة. اعرض مساراته ثم أدخل مسار ملف عادي واحد بالضبط لتصديره إلى مجلد جديد. لا يدعم توسعة الأنماط أو استخراج شجرة مجلدات أو نقاط إعادة التحليل. يكتب دون تخزين WIM كاملةً، مع التحقق من كل كتابة وتقرير SHA256. حد التصدير4GiB ناقص بايت. مجموعات WIM المقسمة تحتاج موارد خارجية وليست مدعومة هنا. القوائم الكبيرة مقيدة بحد التقرير الأصلي.']}
);

GUIDES.push({id:'benchBatch',icon:'book',name:['Use the new workshop tools','استخدام أدوات الورشة الجديدة'],desc:['Updates, conversion, persistence, speed and WIM','التحديث والتحويل والاستمرار والسرعة وWIM'],time:['6 min','٦ دقائق'],tools:['ventoyUpdate','convertTable','persistence','fileBenchmark','wimExplore'],steps:[
['Ventoy update: import your official Linux package first. Select a compatible existing MBR Ventoy USB, stop/eject mounted file access, review the boot/EFI update and tap Confirm. Keep backups and power connected. GPT/custom layouts are rejected.','تحديث Ventoy: استورد حزمة Linux الرسمية أولاً. اختر USB متوافقة عليها Ventoy بجدول MBR، وأنهِ الوصول للملفات وأخرج التركيب بأمان، ثم راجع تحديث الإقلاع وEFI واضغط تأكيد. احتفظ بنسخ احتياطية وبالطاقة. تُرفض GPT والتخطيطات المخصصة.'],
['Partition conversion: choose MBR or GPT and review the limits. Save the small table record on phone storage before conversion. A full-size MBR partition may leave no room for GPT’s backup metadata; this tool rejects it and never shrinks your filesystem. It does not make a BIOS installation boot with UEFI.','تحويل الأقسام: اختر MBR أو GPT وراجع الحدود. احفظ سجل الجدول الصغير على الهاتف قبل التحويل. قد لا يترك قسم MBR الممتد للنهاية مساحة لنسخة بيانات GPT؛ ترفضه الأداة ولا تصغّر نظام ملفاتك. لا تجعل تثبيت BIOS يُقلع عبر UEFI.'],
['Persistence: choose a destination, size and distro label, and enter your ISO’s exact Ventoy path. The tool creates an empty ext4 data file and configuration template. Copy persistence.dat to the Ventoy data root and merge the template into /ventoy/ventoy.json. Preserve existing settings and check the distro/version guide. No Linux system is downloaded.','الاستمرار: اختر الوجهة والحجم واسم التوزيعة وأدخل مسار ISO الدقيق على Ventoy. تنشئ الأداة ملف بيانات ext4 فارغاً وقالب إعداد. انسخ persistence.dat إلى جذر بيانات Ventoy وادمج القالب في /ventoy/ventoy.json. حافظ على الإعدادات الحالية وراجع دليل التوزيعة والإصدار. لا يُنزّل نظام Linux.'],
['File benchmark: select a local folder and16,64or256MiB. It creates one new temporary file, writes and verifies every byte, then deletes only that file. Results include Android/provider caching. It does not erase a USB or prove its real capacity.','قياس الملف: اختر مجلداً محلياً وحجم16 أو64 أو256MiB. ينشئ ملفاً مؤقتاً جديداً ويكتب ويتحقق من كل بايت ثم يحذف ذلك الملف فقط. تتأثر النتيجة بتخزين أندرويد والمزود المؤقت. لا يمحو USB ولا يثبت سعتها الحقيقية.'],
['WIM: select a complete local WIM/ESD and image index, then list its paths. Enter one exact regular-file path from the report, choose a destination and export. The new folder contains that file and the result shows SHA256. No whole-WIM cache, wildcard expansion or directory extraction.','WIM: اختر WIM/ESD محلياً كاملاً ورقم صورة ثم اعرض المسارات. أدخل مسار ملف عادي واحد بالضبط من التقرير واختر وجهة ثم صدّره. يحتوي المجلد الجديد على ذلك الملف وتعرض النتيجة SHA256. لا يخزن WIM كاملةً ولا يوسع الأنماط ولا يستخرج المجلدات.'] ]});
const GUIDE_TOOLS={start:['info','files'],choose:['format'],flash:['write','windows','checksums'],save:['backup','clone'],multi:['ventoy','ventoyInstall'],safe:['wipe'],trouble:['diagnostic','info'],words:['imageinfo'],advanced:['capacity','extract'],workshop:['format','split','join'],downloads:['write','windows'],windowsGuide:['windows','wimSplit'],ventoyGuide:['ventoyInstall','ventoyUpdate'],imagesGuide:['optical','tar']};
GUIDES.forEach(g=>{if(GUIDE_TOOLS[g.id])g.tools=GUIDE_TOOLS[g.id];});

TOOLS.push(
{id:'cso',icon:'layers',color:'blue',group:'files',name:['ISO ↔ CSO conversion','تحويل ISO ↔ CSO'],desc:['CSO v1 compression • verified original bytes','ضغط CSO v1 • تحقق من البايتات الأصلية'],body:['Convert a sector-aligned ISO9660 image to CSO v1 or unpack a supported CSO into ISO. Creates a NEW folder and verifies SHA-256 of the original data after reading the output back. Encoding uses two passes to avoid a whole-image cache. Supports 2048-byte blocks, CSO v1 and index shifts0–12; maximum16 GiB original size. Use the separate ZSO tool for that format. No CHD/CSOv2, games or firmware supplied. Compression is a container change, not a way to make a PC image bootable. Use images you are allowed to copy.','حوّل ISO9660 بمحاذاة القطاعات إلى CSO v1 أو فك CSO المدعوم إلى ISO. ينشئ مجلداً جديداً ويتحقق من SHA-256 للبيانات الأصلية بقراءة الناتج. يقرأ المصدر مرتين للضغط دون تخزين صورة كاملة مؤقتة. يدعم كتل2048 بايت وCSO v1 وإزاحة فهرس0–12؛ الحد16 GiB للحجم الأصلي. استخدم أداة ZSO المنفصلة لصيغتها. لا يدعم CHD أو CSOv2 ولا يوفر ألعاباً أو برمجيات أجهزة. التحويل يغيّر الحاوية ولا يجعل صورة الحاسوب قابلة للإقلاع. استخدم صوراً يحق لك نسخها.']},
{id:'compressedImage',icon:'archive',color:'green',group:'files',name:['GZIP / XZ image tools','أدوات صور GZIP / XZ'],desc:['Compress GZIP or unpack a standalone image','ضغط GZIP أو فك صورة مستقلة'],body:['Compress a raw image into GZIP, or unpack a standalone .img.gz/.iso.gz/.img.xz stream into a new raw file. Select the output-size limit before starting; expansion is measured before an output is created. GZIP CRC/XZ checks are validated, then all saved data is read back and compared by SHA-256. XZ decoder memory limit32 MiB; maximum16 GiB original data. This is NOT TAR extraction; .tar.gz becomes a TAR file. No XZ encoding, passwords or OS downloads. Keep source and destination on reliable local storage with enough free space.','اضغط صورة خام إلى GZIP أو فك تدفق صورة مستقل مثل .img.gz أو .iso.gz أو .img.xz إلى ملف خام جديد. اختر حد حجم الناتج قبل البدء؛ يُقاس الحجم بعد الفك قبل إنشاء الناتج. يتحقق من CRC في GZIP وفحوص XZ ثم يقرأ الناتج كاملاً ويقارنه ببصمة SHA-256. حد ذاكرة فك XZ هو32 MiB وحد البيانات الأصلية16 GiB. هذه ليست أداة استخراج TAR؛ يصبح .tar.gz ملف TAR. لا يضغط XZ ولا يدعم كلمات مرور أو تنزيل أنظمة. احفظ المصدر والوجهة في تخزين محلي موثوق ومساحة كافية.']}
);
GUIDES.push({id:'containers',icon:'book',name:['Convert a compressed image safely','تحويل صورة مضغوطة بأمان'],desc:['Choose a container, space limit and verify','اختر الحاوية وحد المساحة وتحقق'],time:['4 min','٤ دقائق'],tools:['cso','compressedImage','write','checksums'],steps:[
['Choose your own complete local source file and a destination folder. These tools create a new Converted folder; they do not erase USB partitions or change the source.','اختر ملف المصدر المحلي الكامل ومجلد الوجهة. تنشئ الأدوات مجلد Converted جديداً ولا تمحو أقسام USB أو تغيّر المصدر.'],
['Use ISO ↔ CSO for supported ISO9660/CSOv1 images. Choose the matching direction. CSO is not a Windows/Linux boot USB format; unpack before using a tool that needs raw ISO data.','استخدم ISO ↔ CSO لصور ISO9660 وCSOv1 المدعومة واختر الاتجاه الصحيح. CSO ليست صيغة إقلاع USB لأنظمة Windows/Linux؛ فكها قبل استخدام أداة تحتاج بيانات ISO الخام.'],
['Use GZIP/XZ image tools for a standalone compressed image. Choose the correct input compression and a maximum expanded size. The first pass checks the stream and required space; large images take multiple reads.','استخدم أدوات صور GZIP/XZ لصورة مضغوطة مستقلة. اختر ضغط المصدر الصحيح والحد الأعلى للحجم بعد الفك. يفحص المرور الأول التدفق والمساحة المطلوبة؛ تحتاج الصور الكبيرة قراءات متعددة.'],
['Wait for conversion and readback verification. The report shows a SHA-256 of the original/unpacked data, not the compressed container. Compression can produce a larger file for incompressible data.','انتظر التحويل والتحقق بالقراءة. يعرض التقرير بصمة SHA-256 للبيانات الأصلية أو المفكوكة وليس الحاوية المضغوطة. قد ينتج الضغط ملفاً أكبر للبيانات غير القابلة للضغط.'],
['For a decompressed ISO saved as image.img, the bytes are unchanged. Rename the extension if your other software requires .iso. Use Write an image only for supported hybrid/raw images; normal Windows ISO uses Windows UEFI preparation. Cancel removes only new outputs, never the source.','عند حفظ ISO المفكوكة باسم image.img تبقى البايتات دون تغيير. غيّر الامتداد إذا طلب برنامجك .iso. استخدم كتابة صورة للصور الخام أو الهجينة المدعومة فقط؛ تحتاج ISO Windows العادية تجهيز Windows UEFI. يحذف الإلغاء النواتج الجديدة فقط ولا يحذف المصدر.']
]});

TOOLS.push(
{id:'ventoyGpt',icon:'layers',color:'amber',group:'boot',name:['Ventoy GPT · experimental','Ventoy GPT · تجريبي'],desc:['Initial install or compatible default-layout update','تثبيت أولي أو تحديث التخطيط الافتراضي المتوافق'],body:['Import your official Ventoy Linux package first. Initial GPT installation ERASES the drive and creates exFAT data plus a32 MiB VTOYEFI partition with validated primary/backup GPT. The separate update accepts only a recognized default two-partition GPT layout and preserves data sectors, partition tables/IDs and reserved boot sectors while replacing boot/EFI data with readback verification.512-byte sectors,128 MiB to2 TiB. No custom layouts or data-preserving initial install. Actual PC boot/Secure Boot remain untested; use disposable media and backups. No OS or package scripts are run/downloaded.','استورد حزمة Ventoy Linux الرسمية أولاً. يمحو تثبيت GPT الأولي الوحدة وينشئ بيانات exFAT وقسم VTOYEFI بحجم32 MiB مع التحقق من GPT الأساسي والاحتياطي. يقبل التحديث المنفصل التخطيط الافتراضي المعروف بقسمين فقط ويحفظ قطاعات البيانات والجداول والهويات وقطاعات الإقلاع المحجوزة عند استبدال الإقلاع وEFI والتحقق بالقراءة. قطاع 512 بايت وسعة128 MiB إلى2 TiB. لا يدعم التخطيطات المخصصة أو التثبيت الأولي مع حفظ البيانات. لم يُختبر إقلاع الحاسوب وSecure Boot؛ استخدم وسائط تجريبية ونسخاً احتياطية. لا يُشغّل أو ينزّل أنظمة أو سكربتات الحزمة.']},
{id:'fatImage',icon:'folder',color:'green',group:'files',name:['Browse FAT16 / FAT32 images','تصفح صور FAT16 / FAT32'],desc:['Raw volume, MBR or GPT • read-only source','وحدة خام أو MBR أو GPT • مصدر للقراءة فقط'],body:['Open your saved image and choose0 for a raw FAT volume, or the partition number in a full MBR/GPT disk image. List paths or extract into a NEW folder with per-file SHA-256 readback. Supports512-byte FAT16/32 sectors, Unicode long filenames and ASCII short names. Bounds:50,000 entries, depth32,2 million walked clusters,250,000 fragmented ranges,64 MiB directory metadata; malformed/shared/cyclic chains are rejected. FAT12/exFAT/NTFS/ext, OEM non-ASCII short names, deleted-file recovery and live hidden-partition mounting are unsupported. The source is never modified.','افتح صورتك المحفوظة واختر0 لوحدة FAT خام أو رقم القسم لصورة قرص MBR/GPT كاملة. اعرض المسارات أو استخرج إلى مجلد جديد مع التحقق من كل ملف ببصمة SHA-256 بعد قراءته. يدعم FAT16/32 بقطاع 512 بايت والأسماء الطويلة Unicode والقصيرة ASCII. الحدود:50000 عنصر وعمق32 ومليونا عنقود و250000 نطاق تجزئة و64 MiB لبيانات المجلدات؛ تُرفض السلاسل التالفة أو المشتركة أو الدائرية. لا يدعم FAT12 أو exFAT أو NTFS أو ext أو الأسماء القصيرة المحلية غير ASCII أو استعادة المحذوفات أو تركيب الأقسام المخفية مباشرة. لا يُعدل المصدر.']}
);
GUIDES.push({id:'gptFat',icon:'book',name:['Ventoy GPT and saved USB files','Ventoy GPT وملفات USB المحفوظة'],desc:['Safe choices for the new bench tools','اختيارات آمنة للأدوات الجديدة'],time:['5 min','٥ دقائق'],tools:['ventoyGpt','fatImage','clone','info','ventoyInstall'],steps:[
['Back up important data. For a saved USB image, open Browse FAT16/FAT32 images, select the image and a partition number.0 means a raw volume with no partition table; most full disk images use partition 1. GPT partition numbers must match Drive information.','انسخ البيانات المهمة. للصورة المحفوظة افتح تصفح صور FAT16/FAT32 واختر الصورة ورقم القسم. يعني 0 وحدة خام دون جدول أقسام؛ تستخدم معظم صور الأقراص الكاملة القسم 1. يجب أن تطابق أرقام GPT معلومات الوحدة.'],
['List the paths first. If the selected partition is FAT16/FAT32, choose a destination and extract. The app reads only the source image and verifies the new files; it does not mount or repair the USB. Other filesystems need their own reader.','اعرض المسارات أولاً. إذا كان القسم FAT16/FAT32 اختر وجهة واستخرج. يقرأ التطبيق الصورة فقط ويتحقق من الملفات الجديدة ولا يركب USB أو يصلحه. تحتاج الأنظمة الأخرى قارئاً خاصاً بها.'],
['For Ventoy GPT, import an official Linux package that you downloaded yourself. Select the USB and choose initial install only if you intend to ERASE every current partition. Review the target and warning, then Confirm. No typing is needed.','لـVentoy GPT استورد حزمة Linux رسمية نزّلتها بنفسك. اختر USB وحدد التثبيت الأولي فقط إذا أردت محو كل الأقسام الحالية. راجع الهدف والتحذير ثم اضغط تأكيد دون كتابة.'],
['For an existing compatible GPT Ventoy drive, choose Update, not initial install. The app rejects custom layouts, damaged tables or unrecognized boot data before writing. Updates preserve data/table sectors but a power failure can still break booting.','لوحدة Ventoy GPT موجودة ومتوافقة اختر تحديثاً وليس تثبيتاً أولياً. يرفض التطبيق التخطيطات المخصصة والجداول التالفة وبيانات الإقلاع غير المعروفة قبل الكتابة. يحفظ التحديث قطاعات البيانات والجداول لكن انقطاع الطاقة قد يعطل الإقلاع.'],
['After success safely reconnect, copy your own ISOs with Multi-ISO library and test booting on the intended computer. Phone verification does not prove PC or Secure Boot compatibility. These tools include no Windows/Linux images.','بعد النجاح أعد التوصيل بأمان وانسخ صورك بمكتبة الأنظمة المتعددة واختبر الإقلاع على الحاسوب المطلوب. تحقق الهاتف لا يثبت توافق الحاسوب أو Secure Boot. لا تتضمن الأدوات صور Windows/Linux.']
]});

TOOLS.push(
{id:'zso',icon:'layers',color:'blue',group:'files',name:['ISO ↔ ZSO conversion','تحويل ISO ↔ ZSO'],desc:['ZSO v1 with LZ4 • verified original bytes','ZSO v1 بضغط LZ4 • تحقق من البيانات الأصلية'],body:['Convert a complete sector-aligned ISO9660 image into ZSO v1 or unpack a supported ZSO into ISO. Uses an original bounded LZ4 block codec and streaming two-pass compression; no full image cache. Supports2048-byte blocks, index shifts0–12 and up to 16 GiB original data. Reads the saved output back and checks SHA-256 of the original data. CSO uses the separate tool. No CHD/VCD/CSOv2, OPL installation, console modifications or games/firmware are included. Actual console/loader compatibility still needs testing.','حوّل صورة ISO9660 كاملة بمحاذاة القطاعات إلى ZSO v1 أو فك ZSO المدعوم إلى ISO. يستخدم محرك LZ4 محدوداً أصلياً وضغطاً بمرورين دون تخزين الصورة كاملة مؤقتاً. يدعم كتل2048 بايت وإزاحة فهرس0–12 وحد16 GiB للبيانات الأصلية. يقرأ الناتج ويقارنه ببصمة SHA-256 للبيانات الأصلية. استخدم أداة CSO المنفصلة لصيغتها. لا يوفر CHD أو VCD أو CSOv2 أو تثبيت OPL أو تعديل المنصة أو ألعاباً أو برمجيات أجهزة. يحتاج توافق المنصة والمشغل اختباراً فعلياً.']},
{id:'consoleParts',icon:'layers',color:'green',group:'files',name:['PS3 raw split files & join','تقسيم ملفات PS3 الخام ودمجها'],desc:['ISO.# / .666NN • FAT32-size parts + manifest','ISO.# و.666NN • أجزاء تناسب FAT32 وملف تحقق'],body:['Split your own ISO into raw image.iso.0, image.iso.1… or split another large file into filename.66600, filename.66601… Parts contain only original bytes, unlike .uwp split containers. Creates a NEW folder, verifies each part and saves a .usbwb checksum manifest. For joining, choose a destination then select ALL numbered files AND that manifest together. Missing/duplicate/corrupt files are rejected and the joined file is fully read back. Up to64 GiB/64 parts;1/2/3.75 GiB parts. Joined files over4 GiB need phone/exFAT/another suitable destination. Third-party parts without this app’s manifest cannot be joined here. No PS2 ul.cfg, game import/decryption, console software or guaranteed console playback. Source files are unchanged.','قسّم ISO الخاصة بك إلى image.iso.0 وimage.iso.1… خام، أو ملفاً كبيراً آخر إلى filename.66600 وfilename.66601… تحتوي الأجزاء البايتات الأصلية فقط، بخلاف حاويات .uwp. ينشئ مجلداً جديداً ويتحقق من كل جزء ويحفظ ملف بصمات .usbwb. للدمج اختر وجهة ثم حدد كل الأجزاء المرقمة وملف التحقق معاً. يرفض الملفات المفقودة أو المكررة أو التالفة ويقرأ الناتج كاملاً للتحقق. الحد64 GiB و64 جزءاً؛ أحجام الأجزاء1 أو2 أو3.75 GiB. يحتاج الناتج فوق4 GiB الهاتف أو exFAT أو وجهة مناسبة أخرى. لا يدمج أجزاء برامج أخرى دون ملف تحقق التطبيق. لا يوفر ul.cfg لـPS2 أو استيراد الألعاب أو فك تشفيرها أو برامج المنصة أو ضمان تشغيلها. لا يغير المصادر.']}
);
GUIDES.push({id:'consoleGuide',icon:'book',name:['ZSO and FAT32 console parts','ZSO وأجزاء المنصة من أجل FAT32'],desc:['Pick the right format and keep the verification file','اختر الصيغة الصحيحة واحتفظ بملف التحقق'],time:['5 min','٥ دقائق'],tools:['zso','consoleParts','cso','checksums'],steps:[
['Use your own permitted image or large file stored locally. These tools do not download games, firmware or systems and do not install a console loader.','استخدم صورتك أو ملفك الكبير المسموح بنسخه والمحلي. لا تنزّل الأدوات ألعاباً أو برمجيات أجهزة أو أنظمة ولا تثبّت مشغلاً للمنصة.'],
['ZSO is compressed ISO data using LZ4. Select the correct direction, a destination and the original-size limit. Wait for readback verification. Check whether your exact loader supports ZSO; this is not a PC USB boot format.','ZSO بيانات ISO مضغوطة بـLZ4. اختر الاتجاه والوجهة وحد الحجم الأصلي وانتظر التحقق بالقراءة. تحقق من دعم مشغلك المحدد لـZSO؛ ليست صيغة إقلاع USB للحاسوب.'],
['For supported PS3 split ISO loaders choose ISO.# and an output name ending .iso. Choose .666NN for a large file that a compatible file manager can rejoin; enter its original filename. These layouts are different.','لمشغلات PS3 الداعمة لـISO المقسمة اختر ISO.# واسماً ينتهي .iso. اختر .666NN لملف كبير يدعم مدير ملفات متوافق دمجه وأدخل اسمه الأصلي. التخطيطان مختلفان.'],
['Choose the part size and split. Every part stays below FAT32’s4 GiB limit. The extra .usbwb file records all sizes and checksums; keep it with the numbered files. It is not a console payload and does not change their raw format.','اختر حجم الجزء وقسّم الملف. يبقى كل جزء دون حد FAT32 البالغ4 GiB. يحفظ .usbwb الأحجام والبصمات؛ احتفظ به مع الأجزاء. ليس ملف تشغيل للمنصة ولا يغير الصيغة الخام للأجزاء.'],
['To rejoin your app-created set, choose a destination with space for the whole original file. Tap Join and multi-select every numbered part plus the .usbwb manifest. The app checks all parts before creating output, then joins and verifies the whole result. Do not rename parts separately.','لدمج مجموعة أنشأها التطبيق اختر وجهة تتسع للملف الأصلي كاملاً. اضغط دمج وحدد كل الأجزاء المرقمة وملف .usbwb معاً. يفحصها قبل إنشاء الناتج ثم يدمج ويتحقق من الملف كاملاً. لا تغيّر أسماء الأجزاء منفردة.'],
['Copy the raw parts to the location your loader documents and test with that loader. Keep the original until independently checking the result. Checksums detect damage but do not prove the publisher or make an unsupported image playable.','انسخ الأجزاء الخام إلى المكان المحدد في وثائق مشغلك واختبره. احتفظ بالأصل حتى تتحقق مستقلاً من الناتج. تكشف البصمات التلف لكنها لا تثبت الناشر ولا تجعل صورة غير مدعومة قابلة للتشغيل.']
]});

TOOLS.push({id:'filesystemCheck',icon:'shield',color:'amber',group:'disk',name:['Filesystem check & limited repair','فحص نظام الملفات وإصلاحه المحدود'],desc:['FAT / exFAT / NTFS / ext2–4 / F2FS','FAT وexFAT وNTFS وext2–4 وF2FS'],body:['Select a USB and partition number, then start a read-only check.0 is only for a raw filesystem WITHOUT a partition table; most app-formatted drives use partition 1. Auto-detects FAT12/16/32, exFAT, NTFS, ext2/3/4 or 4 KiB-block F2FS on basic MBR/GPT layouts with 512-byte sectors. Repair may remove damaged directory entries or truncate files, and automatic mode can refuse severe errors. Repair is separate and WRITES metadata: fsck.fat/exfat automatic repairs, limited ntfsfix, e2fsck automatic preen or fsck.f2fs, followed by another check. Back up first and stop/eject mounted access. NTFS repair does not replace Windows chkdsk; serious damage may need a PC. No defrag, deleted recovery, live mounting or hardware-health guarantee. Experimental until tested on your disposable media.','اختر USB ورقم القسم ثم ابدأ فحصاً للقراءة فقط. يعني 0 نظام ملفات خاماً دون جدول أقسام؛ تستخدم أغلب الوحدات التي يهيئها التطبيق القسم 1. يكتشف FAT12/16/32 أو exFAT أو NTFS أو ext2/3/4 أو F2FS بكتل 4 KiB على تخطيطات MBR/GPT بسيطة بقطاع 512 بايت. قد يحذف الإصلاح إدخالات دليل تالفة أو يقصر ملفات، وقد يرفض الوضع التلقائي الأخطاء الشديدة. الإصلاح منفصل ويكتب البيانات الوصفية: fsck.fat/exfat التلقائي أو ntfsfix المحدود أو e2fsck التلقائي أو fsck.f2fs ثم يعيد الفحص. انسخ البيانات أولاً وأوقف الوصول المركب وأخرجه بأمان. لا يستبدل إصلاح NTFS أداة chkdsk في Windows؛ قد يحتاج التلف الشديد حاسوباً. لا يلغي التجزئة أو يستعيد المحذوفات أو يركب الوحدة أو يضمن سلامة العتاد. تجريبي حتى اختبار وسائطك القابلة للمحو.']});
GUIDES.push({id:'filesystemCare',icon:'book',name:['Check a drive before repair','فحص الوحدة قبل إصلاحها'],desc:['Understand the filesystem, backups and limits','فهم نظام الملفات والنسخ الاحتياطي والحدود'],time:['5 min','٥ دقائق'],tools:['filesystemCheck','format','clone','info'],steps:[
['Back up important files or clone the USB to a phone image first. A filesystem repair changes metadata and cannot guarantee that every damaged file will be recovered. Close transfers and safely eject Android’s mounted access while leaving the cable attached.','انسخ الملفات المهمة أو انسخ USB إلى صورة على الهاتف أولاً. يغيّر الإصلاح البيانات الوصفية ولا يضمن استعادة كل ملف تالف. أغلق النقل وأخرج الوصول المركب في أندرويد بأمان مع بقاء الكابل متصلاً.'],
['Open Filesystem check & limited repair, select the correct USB and partition number from Drive information. Use 1 for the usual single-partition layout. Use 0 only for a raw filesystem with no MBR/GPT table; partitioned drives reject 0.','افتح فحص نظام الملفات وإصلاحه المحدود وحدد USB الصحيح ورقم القسم من معلومات الوحدة. استخدم1 للتخطيط المعتاد بقسم واحد. استخدم0 فقط لنظام خام دون جدول MBR/GPT؛ ترفض الوحدات المقسمة الرقم0.'],
['Start Read-only check first. A reported issue may be filesystem damage rather than a broken tool. Read the result/details. The check does not test full capacity, flash lifetime or every file’s contents.','ابدأ بالفحص للقراءة فقط. قد تعني المشكلة المبلّغ عنها تلف نظام الملفات وليس تعطل الأداة. اقرأ النتيجة والتفاصيل. لا يختبر الفحص السعة الكاملة أو عمر الفلاش أو محتوى كل ملف.'],
['If backed up, review Limited repair and confirm the target. FAT/exFAT use upstream automatic repair and can refuse severe damage. Repair may truncate files or remove broken entries. NTFS uses basic ntfsfix; use Windows chkdsk for full repairs. ext uses automatic preen and can refuse severe errors. F2FS uses its own checker/repair utility. Interrupting repair can damage data.','بعد النسخ الاحتياطي راجع الإصلاح المحدود وأكد الهدف. يستخدم FAT/exFAT الإصلاح التلقائي وقد يرفض التلف الشديد. قد يقصر الإصلاح الملفات أو يحذف إدخالات تالفة. يستخدم NTFS أداة ntfsfix الأساسية؛ استخدم Windows chkdsk للإصلاح الكامل. يستخدم ext الإصلاح التلقائي وقد يرفض الأخطاء الشديدة. يستخدم F2FS أداة الفحص والإصلاح الخاصة به. قد يضر الانقطاع البيانات.'],
['F2FS formatting is a separate ERASE operation in Format USB. It needs at least 256 MiB,512-byte sectors and Automatic allocation; it creates 4 KiB blocks and verifies both superblock copies plus a read-only filesystem check. Android mounting support still depends on your device.','تهيئة F2FS عملية محو منفصلة في تهيئة USB. تحتاج256 MiB على الأقل وقطاع 512 بايت والتخصيص التلقائي؛ تنشئ كتل 4 KiB وتتحقق من نسختي الكتلة الفائقة ثم تفحص النظام للقراءة. يبقى دعم التركيب في أندرويد معتمداً على جهازك.'],
['After a repair, check your files and keep the backup. For ongoing errors, failing hardware, encrypted or unsupported filesystems, preserve the source and use appropriate computer recovery tools. Do not repeatedly format a drive that contains needed data.','بعد الإصلاح تحقق من ملفاتك واحتفظ بالنسخة. للأخطاء المستمرة أو العتاد المتعطل أو الأنظمة المشفرة أو غير المدعومة احفظ المصدر واستخدم أدوات الاستعادة المناسبة على حاسوب. لا تكرر تهيئة وحدة تحتوي بيانات مطلوبة.']
]});
TOOLS.push({id:'archives',icon:'layers',color:'green',group:'files',name:['7z / RAR & archive workshop','ورشة 7z وRAR والأرشيفات'],desc:['Read, validate and extract into a new folder','قراءة وفحص واستخراج إلى مجلد جديد'],body:['Choose your own single-volume archive. Traditional ZipCrypto password ZIP is supported with a transient password; AES ZIP and encrypted 7z/RAR are not. Supports 7z (LZMA/LZMA2 and supported filters), RAR4/5, ZIP and TAR with gzip/XZ/LZMA where supported. Validates the archive before creating a destination folder, rejects links and unsafe or case-conflicting paths, then streams extraction and verifies saved files with SHA-256 readback. Up to 10,000 entries and your chosen 1–16 GiB expanded limit; listing displays the first 200 paths. Sources up to 64 GiB, decoder memory limit 128 MiB. Unsupported encryption, split volumes and codecs are rejected. The password is not saved in history or output manifests. No archive is executed or copied whole to app storage.','اختر أرشيفك أحادي الجزء. يدعم ZIP بكلمة مرور من نوع ZipCrypto بكلمة مرور مؤقتة؛ لا يدعم AES ZIP أو 7z/RAR المشفر. يدعم 7z بترميز LZMA/LZMA2 والمرشحات المدعومة، وRAR4/5 وZIP وTAR مع gzip/XZ/LZMA حيث تدعمها الأداة. يفحص الأرشيف قبل إنشاء مجلد الوجهة ويرفض الروابط والمسارات الخطرة أو المتعارضة في حالة الأحرف، ثم يستخرج بالتدفق ويتحقق من الملفات المحفوظة ببصمة SHA-256 وإعادة القراءة. الحد ١٠٬٠٠٠ إدخال وحد التوسع الذي تختاره من 1 إلى 16 GiB؛ تعرض القائمة أول ٢٠٠ مسار. حجم المصدر حتى 64 GiB وحد ذاكرة مفكك الترميز 128 MiB. يرفض التشفير غير المدعوم والأجزاء المنقسمة والترميزات غير المدعومة. لا يحفظ كلمة المرور في السجل أو ملفات بيانات الناتج. لا يشغل الأرشيف أو ينسخه كاملاً إلى تخزين التطبيق.']});
TOOLS.push({id:'dmg',icon:'layers',color:'amber',group:'files',name:['DMG → raw image','تحويل DMG إلى صورة خام'],desc:['UDIF raw / zero / ADC / zlib • verify output','UDIF خام وأصفار وADC وzlib • تحقق من الناتج'],body:['Convert a user-supplied, unencrypted, single-segment XML UDIF DMG into image.img. Supports raw, zero/ignored, ADC and zlib blocks, with bounded block maps, block-table CRC32 where present and SHA-256 output readback. Choose an expanded-size limit up to 16 GiB. Unsupported BZIP2, LZFSE, encrypted or segmented images stop during preflight before an output folder is made. This preserves disk bytes; it does not create a bootloader, change HFS/APFS into another filesystem or guarantee Mac/PC boot. You may use Write image for a compatible raw result.','حوّل ملف DMG الذي توفره من نوع XML UDIF غير المشفر وأحادي الجزء إلى image.img. يدعم الكتل الخام والأصفار أو المتجاهلة وADC وzlib، مع حدود لخريطة الكتل وCRC32 لخريطة الكتل حين توجد وإعادة قراءة الناتج ببصمة SHA-256. اختر حد الحجم بعد الفك حتى 16 GiB. تتوقف صور BZIP2 وLZFSE والمشفرة أو المجزأة غير المدعومة أثناء الفحص الأولي قبل إنشاء مجلد ناتج. يحفظ بايتات القرص؛ لا ينشئ محمل إقلاع أو يحول HFS/APFS إلى نظام ملفات آخر أو يضمن إقلاع Mac أو الحاسوب. استخدم كتابة الصورة للناتج الخام المتوافق.']});
GUIDES.push({id:'archiveImages',icon:'book',name:['Open archives and DMG images','فتح الأرشيفات وصور DMG'],desc:['Pick your file, check space, save and verify','اختيار الملف وفحص المساحة والحفظ والتحقق'],time:['6 min','٦ دقائق'],tools:['archives','dmg','chd','fsImage','fsUsb','write','hash'],steps:[
['Choose a source file you already have. The app supplies tools, not operating systems, games or firmware. Archive extraction and DMG conversion create new files and do not format a USB.','اختر ملف مصدر تملكه مسبقاً. يوفر التطبيق أدوات ولا يوفر أنظمة تشغيل أو ألعاباً أو برمجيات أجهزة. ينشئ استخراج الأرشيف وتحويل DMG ملفات جديدة ولا يهيئ USB.'],
['For 7z/RAR/ZIP/TAR, choose a safe expansion limit and start Validate & list. Compressed files can expand much larger than their download size. Passwords and multi-volume sets are unsupported. A failed validation means extraction will not start.','لـ7z وRAR وZIP وTAR اختر حد توسع آمناً وابدأ الفحص والعرض. قد تصبح الملفات المضغوطة أكبر كثيراً من حجم تنزيلها. كلمات المرور والمجموعات متعددة الأجزاء غير مدعومة. يمنع فشل الفحص بدء الاستخراج.'],
['Choose a destination folder with enough free space, then Extract all. The app creates its own new subfolder and checks file contents after writing. Sources remain read-only; do not unplug the destination. Unsafe names and links are rejected.','اختر مجلد وجهة بمساحة حرة كافية ثم استخراج الكل. ينشئ التطبيق مجلداً فرعياً جديداً ويتحقق من المحتوى بعد الكتابة. تبقى المصادر للقراءة فقط؛ لا تفصل الوجهة. تُرفض الأسماء الخطرة والروابط.'],
['For DMG, select a supported XML UDIF image and a destination, then convert. Unsupported encryption/codecs are rejected before saving. The result is raw disk data, not a new ISO or an automatically bootable system.','لـDMG اختر صورة XML UDIF مدعومة ووجهة ثم حوّل. تُرفض طرق التشفير والترميز غير المدعومة قبل الحفظ. الناتج بيانات قرص خام وليست ISO جديدة أو نظاماً يصبح قابلاً للإقلاع تلقائياً.'],
['For CHD, use a standalone CHDv5 hard-disk/DVD image within the selected limit. CD/audio/mixed tracks and parent images are refused; this tool decodes data and does not create CHD files or install a console loader.','لـCHD استخدم صورة CHDv5 مستقلة للقرص أو DVD ضمن الحد المختار. ترفض الأداة CD والصوت والمسارات المختلطة والصور التابعة لأصل؛ تفك البيانات ولا تنشئ CHD أو تثبت محمل ألعاب.'],
['For an NTFS/ext image, choose its partition number (0 only for a raw filesystem) and a safe extraction limit. D means directory, F means regular file, and S marks an unsupported path/subtree that will not be followed or extracted. Extraction copies file contents, not a full bootable OS with its permissions and links.','لصورة NTFS/ext اختر رقم القسم (0 للنظام الخام فقط) وحد استخراج آمناً. يعني D مجلداً وF ملفاً عادياً وS مساراً أو شجرة غير مدعومة لا تتبع أو تستخرج. ينسخ الاستخراج محتوى الملفات وليس نظام تشغيل قابلاً للإقلاع بصلاحياته وروابطه.'],
['For a real NTFS/ext USB, use Browse NTFS/ext USB files. Close transfers and eject Android’s mounted access, choose the partition and list paths. To copy files, select a folder in this device’s internal storage with enough space. It reads files directly and does not first fill the phone with a complete USB backup. S paths are omitted; this is not a full OS restore.','لوحدة USB فعلية بنظام NTFS/ext استخدم تصفح ملفات USB بنظام NTFS وext. أغلق النقل وأخرج الوصول المركب في أندرويد ثم اختر القسم واعرض المسارات. للنسخ اختر مجلداً في التخزين الداخلي لهذا الجهاز بمساحة كافية. يقرأ الملفات مباشرة ولا يملأ الهاتف أولاً بنسخة USB كاملة. لا تستخرج مسارات S؛ ليست استعادة نظام تشغيل كاملة.'],
['Keep the source until you independently check the result. If you later use Write image, review the actual target: that separate action erases its data. Test computer boot on the intended machine.','احتفظ بالمصدر حتى تتحقق من الناتج بشكل مستقل. إذا استخدمت كتابة الصورة لاحقاً فراجع الهدف الفعلي: تلك العملية المنفصلة تمحو بياناته. اختبر إقلاع الحاسوب على الجهاز المقصود.']
]});
TOOLS.push({id:'chd',icon:'layers',color:'green',group:'files',name:['CHD → raw data image','تحويل CHD إلى صورة بيانات خام'],desc:['Standalone CHDv5 • hard disk / DVD units','CHDv5 مستقل • وحدات قرص أو DVD'],body:['Decode your standalone CHDv5 data image with 512-byte hard-disk or 2048-byte DVD units into image.img. Uses libchdr with block CRC checks where present, source-change detection, SHA-256 output readback and the CHD raw SHA-1 when supplied. Maximum 16 GiB decoded, 1 MiB hunks and 4,194,304 hunks. CD/mixed/audio track reconstruction, parent-dependent images, old CHD versions, AVHuff and compressed CHD creation are unsupported and rejected. No games or firmware are supplied. Decoding preserves data; it does not make a game/system compatible or guarantee boot.','فك صورة بيانات CHDv5 المستقلة بوحدات قرص 512 بايت أو DVD بحجم 2048 بايت إلى image.img. يستخدم libchdr مع CRC للكتل حين توجد وكشف تغير المصدر وإعادة قراءة الناتج ببصمة SHA-256 وبصمة SHA-1 الخام في CHD حين توفرها الصورة. الحد 16 GiB بعد الفك وكتل 1 MiB وعدد ٤٬١٩٤٬٣٠٤ كتلة. إعادة بناء مسارات CD أو الصوت أو المسارات المختلطة والصور التابعة لأصل وإصدارات CHD القديمة وAVHuff وإنشاء CHD المضغوط غير مدعومة وتُرفض. لا يوفر ألعاباً أو برمجيات أجهزة. يحفظ الفك البيانات ولا يجعل اللعبة أو النظام متوافقاً أو يضمن الإقلاع.']});
TOOLS.push({id:'verifyZero',icon:'shield',color:'amber',group:'disk',name:['Verify zero-filled USB','التحقق من USB المملوءة بالأصفار'],desc:['Read every byte • dated local verification record','قراءة كل بايت • سجل تحقق محلي مؤرخ'],body:['Read-only check for a drive you previously zero-filled. Reads every addressable sector and stops at the first nonzero byte, reporting its sector and offset. A normal formatted drive contains nonzero metadata and is expected to fail this particular check; that does not indicate damage. Successful checks produce a dated local report you can export through Activity. Zero-fill now also performs this full readback before reporting success. This cannot prove erasure of hidden/spare flash cells, genuine physical capacity or certified sanitization. It does not erase or repair anything.','فحص للقراءة فقط لوحدة ملأتها بالأصفار مسبقاً. يقرأ كل قطاع قابل للعنونة ويتوقف عند أول بايت غير صفري، مع عرض قطاعه وإزاحته. تحتوي الوحدة المهيأة العادية بيانات وصفية غير صفرية ومن المتوقع ألا تجتاز هذا الفحص المحدد؛ لا يدل ذلك على تلفها. ينتج نجاح الفحص تقريراً محلياً مؤرخاً يمكن تصديره عبر النشاط. ينفذ الملء بالأصفار الآن هذه القراءة الكاملة أيضاً قبل إعلان النجاح. لا يثبت محو خلايا الفلاش المخفية أو الاحتياطية أو السعة المادية الحقيقية أو التطهير المعتمد. لا يمحو أو يصلح شيئاً.']});
TOOLS.push({id:'fsImage',icon:'layers',color:'green',group:'files',name:['NTFS / ext image browser','تصفح صور NTFS وext'],desc:['Read-only files • no Android mount needed','ملفات للقراءة فقط • دون تركيب في أندرويد'],body:['Browse or extract regular files from your NTFS or ext2/3/4 image, without changing it or mounting it in Android. Select partition 1 for a usual MBR/GPT image, or 0 for a raw filesystem image. Supports validated basic 512-byte-sector layouts and a filesystem view up to 64 GiB, 10,000 paths and 16 GiB regular-file content. Extracts into a new folder with per-file SHA-256 readback and source-change detection. Links, reparse points, encrypted files and special paths/subtrees are marked S and omitted, not followed; the result reports their count. It does not restore Unix permissions, ACLs, links, a complete operating system or deleted files. Dirty journals, damaged metadata and unsupported features may be refused.','تصفح أو استخرج الملفات العادية من صورتك NTFS أو ext2/3/4 دون تغييرها أو تركيبها في أندرويد. اختر القسم 1 لصورة MBR/GPT المعتادة أو 0 لصورة نظام ملفات خام. يدعم تخطيطات بسيطة موثقة بقطاع 512 بايت ومنظور نظام ملفات حتى 64 GiB و١٠٬٠٠٠ مسار ومحتوى ملفات عادية حتى 16 GiB. يستخرج إلى مجلد جديد ويتحقق من كل ملف ببصمة SHA-256 وإعادة القراءة مع كشف تغير المصدر. توسم الروابط ونقاط إعادة التحليل والملفات المشفرة والمسارات أو الأشجار الخاصة بالحرف S ولا تتبع أو تستخرج؛ يذكر الناتج عددها. لا يعيد صلاحيات Unix أو ACL أو الروابط أو نظام تشغيل كاملاً أو المحذوفات. قد تُرفض السجلات غير النظيفة والبيانات التالفة والميزات غير المدعومة.']});
TOOLS.push({id:'fsUsb',icon:'usb',color:'green',group:'disk',name:['Browse NTFS / ext USB files','تصفح ملفات USB بنظام NTFS وext'],desc:['Direct read-only access • export to internal storage','قراءة مباشرة • تصدير إلى التخزين الداخلي'],body:['Read NTFS or ext2/3/4 files directly over supported USB mass-storage, even if Android does not mount that filesystem. No root, kernel driver or whole-drive temporary image is needed. Choose the actual partition number (usually 1; 0 only with no partition table). Listing reads metadata rather than hashing the entire drive. Extraction copies regular files into a new INTERNAL-storage folder, checks free space and reads saved files back with SHA-256; external/cloud destinations are refused here to prevent writing to the source USB. Close transfers and safely eject Android’s mounted access first. Direct USB browsing is not an atomic snapshot and has no whole-source checksum pass. Links, encrypted and special paths/subtrees are marked S and omitted. Current limits: 512-byte sectors, filesystem view up to 64 GiB, 10,000 paths, up to 16 GiB regular-file content. No mounting, repair, permissions/link restoration or boot guarantee.','اقرأ ملفات NTFS أو ext2/3/4 مباشرة عبر تخزين USB المدعوم حتى إذا لم يركب أندرويد النظام. لا يحتاج روت أو برنامج تشغيل للنواة أو صورة مؤقتة كاملة. اختر رقم القسم الفعلي (عادة 1؛ و0 دون جدول أقسام فقط). تقرأ القائمة البيانات الوصفية بدلاً من حساب بصمة الوحدة كاملة. ينسخ الاستخراج الملفات العادية إلى مجلد جديد في التخزين الداخلي ويفحص المساحة ويعيد قراءة الملفات المحفوظة ببصمة SHA-256؛ ترفض الوجهات الخارجية والسحابية هنا لمنع الكتابة إلى USB المصدر. أغلق النقل وأخرج الوصول المركب في أندرويد بأمان أولاً. ليس التصفح المباشر لقطة ذرية ولا يحسب بصمة المصدر الكامل. توسم الروابط والمسارات أو الأشجار المشفرة والخاصة بالحرف S ولا تستخرج. الحدود الحالية: قطاع 512 بايت ومنظور نظام ملفات حتى 64 GiB و١٠٬٠٠٠ مسار ومحتوى ملفات عادية حتى 16 GiB. لا يركب أو يصلح النظام أو يعيد الصلاحيات والروابط أو يضمن الإقلاع.']});

TOOLS.push(
{id:'wimTree',icon:'folder',color:'blue',group:'files',name:['WIM folder export','تصدير مجلد WIM'],desc:['Choose image + folder • verified new files','اختر الصورة والمجلد • ملفات جديدة متحقق منها'],body:['Select your standalone WIM/ESD, image index and an exact folder path (or / for the root). List first, then export regular files into a NEW folder. Paths retain their full in-image hierarchy. Each file is decompressed with wimlib integrity checks and fully read back with SHA-256; the source is hashed before and after. Limits: 10,000 paths, 1,000 regular files, depth128, 1/4/8/16 GiB total, 64 GiB input. Choose a smaller folder if limits are exceeded. S marks links/reparse, encrypted, special or named-stream entries that are omitted; no ACLs, ownership, alternate streams or hard-link relationships are restored. This is file export, not Windows installation or Windows To Go. Split SWM sets/passwords are unsupported.','اختر WIM أو ESD مستقلاً ورقم الصورة ومسار المجلد الدقيق (أو / للجذر). اعرض القائمة أولاً ثم صدّر الملفات العادية إلى مجلد جديد. تحتفظ المسارات بتسلسلها الكامل داخل الصورة. يُفك كل ملف بفحص سلامة wimlib وتُعاد قراءته ببصمة SHA-256 وتُحسب بصمة المصدر قبل وبعد. الحدود: ١٠٬٠٠٠ مسار و١٬٠٠٠ ملف عادي وعمق١٢٨ وإجمالي 1/4/8/16 GiB ومدخل64 GiB. اختر مجلداً أصغر عند تجاوز الحد. يوسم S الروابط ونقاط إعادة التحليل والعناصر المشفرة والخاصة وذات التدفقات المسماة التي لا تستخرج؛ لا تعاد الصلاحيات أو الملكية أو التدفقات البديلة أو علاقات الروابط الصلبة. هذا تصدير ملفات وليس تثبيت Windows أو Windows To Go. مجموعات SWM المقسمة وكلمات المرور غير مدعومة.']},
{id:'usbExtreme',icon:'layers',color:'amber',group:'files',name:['PS2 USBExtreme workshop','ورشة PS2 USBExtreme'],desc:['ISO → 1 GiB parts + ul.cfg • inspect catalogs','ISO إلى أجزاء 1 GiB وul.cfg • فحص الفهارس'],body:['Choose your own legal, unencrypted 2048-byte-sector PS2 ISO. The tool reads BOOT2 in root SYSTEM.CNF for its disc ID. Enter a 1–32-character printable ASCII title, choose CD or DVD, and select a destination folder. It creates standard ul.CRC.DISCID.00 parts (1 GiB maximum) and a single-entry ul.cfg in a NEW subfolder, verifying every saved part and catalog. Input limit16 GiB. To inspect an existing catalog, choose ul.cfg as the source and use Inspect only. Existing libraries are never merged or overwritten: move the generated files to an empty library root or manage merging separately. Title CRC names are not content integrity checks; saved files use SHA-256. No loader, games, firmware, disc decryption or console modification is included. A correctly prepared PS2/OPL and actual console testing are still needed.','اختر صورة PS2 ISO قانونية غير مشفرة بقطاعات2048بايت. تقرأ الأداة BOOT2 من SYSTEM.CNF في الجذر لمعرفة رقم القرص. أدخل عنواناً من١ إلى٣٢ رمز ASCII قابل للطباعة واختر CD أوDVD ومجلد الوجهة. تنشئ أجزاء قياسية ul.CRC.DISCID.00 بحد1 GiB للجزء وفهرس ul.cfg بإدخال واحد داخل مجلد فرعي جديد، مع التحقق من كل جزء والفهرس المحفوظ. حد المدخل16 GiB. لفحص فهرس موجود اختر ul.cfg كمصدر واضغط فحص فقط. لا تدمج المكتبات الحالية ولا تستبدلها: انقل الملفات الناتجة إلى جذر مكتبة فارغ أو أدر الدمج منفصلاً. بصمة العنوان لتسمية الأجزاء وليست فحصاً للمحتوى؛ تستخدم الملفات المحفوظة SHA-256. لا يتضمن التطبيق مشغلاً أو ألعاباً أو برنامجاً ثابتاً أو فك تشفير القرص أو تعديل الجهاز. يلزم PS2 وOPL مجهزان واختبار فعلي.']});
GUIDES.push({id:'folderConsole',name:['Export a folder or prepare PS2 files','تصدير مجلد أو تجهيز ملفات PS2'],desc:['Small steps for WIM and USBExtreme','خطوات بسيطة لـWIM وUSBExtreme'],time:['6 min','٦ دقائق'],steps:[
['For WIM: choose your WIM/ESD, enter its image number (usually1), and an exact folder such as /Windows/Boot. List it first. If the folder is too large, choose a smaller one. Choose an empty destination area with enough free space and export. S paths are omitted, not restored.','لـWIM: اختر WIM أوESD وأدخل رقم الصورة (عادة١) ومجلداً دقيقاً مثل /Windows/Boot. اعرضه أولاً. إذا كان كبيراً اختر مجلداً أصغر. اختر وجهة بها مساحة كافية وصدّر. مسارات S لا تُستخرج أو تُستعاد.'],
['For PS2: supply your own legal ISO, enter a short ASCII title and choose CD/DVD matching the disc. Create files into a new folder. Keep ALL numbered ul.* parts together with ul.cfg.','لـPS2: اختر ISO قانونية خاصة بك وأدخل عنوان ASCII قصيراً واختر CD/DVD حسب القرص. أنشئ الملفات في مجلد جديد. احتفظ بكل أجزاء ul.* مع ul.cfg.'],
['To use a new USBExtreme set, move its files to the root of an EMPTY compatible OPL USB library. Do not replace another library’s ul.cfg: it may hide other entries. This app does not merge catalogs or install OPL. Back up first and test on your prepared console.','لاستخدام مجموعة USBExtreme جديدة انقل ملفاتها إلى جذر مكتبة USB فارغة متوافقة مع OPL. لا تستبدل ul.cfg لمكتبة أخرى فقد تختفي إدخالاتها. لا يدمج التطبيق الفهارس ولا يثبت OPL. انسخ احتياطياً واختبر على جهازك المجهز.'],
['Use Inspect ul.cfg to read titles, disc IDs, media and part counts without changing anything. It validates catalog structure only, not the existence, contents, fragmentation or playability of its parts. No systems/games are downloaded by these tools.','استخدم فحص ul.cfg لقراءة العناوين وأرقام الأقراص والأنواع وعدد الأجزاء دون تغيير شيء. يفحص بنية الفهرس فقط وليس وجود الأجزاء أو محتواها أو تجزئتها أو قابليتها للتشغيل. لا تنزّل هذه الأدوات أنظمة أو ألعاباً.']
]});

// Beginner recipes are source data, shared by Tools and Learn.
const TOOL_HELP={
  "write": {
    "goal": [
      "Put an image onto a USB so a compatible computer can use it.",
      "اكتب صورة على USB ليستخدمها حاسوب متوافق."
    ],
    "needs": [
      "A raw IMG or supported hybrid ISO, a USB large enough, OTG and a separate backup.",
      "IMG خام أو ISO هجينة مدعومة و USB تتسع لها و OTG ونسخة احتياطية منفصلة."
    ],
    "steps": [
      [
        "Save the image on your phone or another drive, away from the USB being erased.",
        "احفظ الصورة على الهاتف أو وحدة أخرى بعيداً عن USB الذي سيُمحى."
      ],
      [
        "Choose the image and USB, then compare the displayed name and capacity with the physical drive.",
        "اختر الصورة و USB ثم طابق الاسم والسعة المعروضين مع الوحدة التي تحملها."
      ],
      [
        "Read the warning, confirm, and wait for writing AND verification before reconnecting safely.",
        "اقرأ التحذير وأكّد ثم انتظر الكتابة والتحقق معاً قبل إعادة التوصيل بأمان."
      ]
    ],
    "result": [
      "A verified write; actual boot must be tested on the computer. Normal Windows ISOs use the separate Windows tool.",
      "كتابة متحقق منها؛ يجب اختبار الإقلاع على الحاسوب. تستخدم صور Windows العادية أداة Windows المنفصلة."
    ],
    "guide": "flash",
    "impact": [
      "Erases the selected USB. Back up and identify the target before confirming.",
      "يمحو USB المحددة. انسخ بياناتك وتأكد من الهدف قبل التأكيد."
    ]
  },
  "format": {
    "goal": [
      "Make a USB ready to store ordinary files again.",
      "اجعل USB جاهزاً لحفظ الملفات العادية مجدداً."
    ],
    "needs": [
      "Your USB and another place for every file you want to keep.",
      "USB ومكان آخر لكل ملف تريد الاحتفاظ به."
    ],
    "steps": [
      [
        "Back up your files, then choose the correct USB. Formatting removes its current layout.",
        "انسخ ملفاتك احتياطياً ثم اختر USB الصحيح. التهيئة تزيل تخطيطه الحالي."
      ],
      [
        "Choose a filesystem for the device that will use it. Leave allocation size Automatic unless that device requires otherwise.",
        "اختر نظام الملفات حسب الجهاز الذي سيستخدمه. اترك حجم التخصيص تلقائياً إلا إذا تطلب الجهاز غير ذلك."
      ],
      [
        "Review the name, capacity and format, confirm, then safely reconnect when complete.",
        "راجع الاسم والسعة والنظام وأكّد ثم أعد التوصيل بأمان بعد الاكتمال."
      ]
    ],
    "result": [
      "A new empty filesystem. Android may still be unable to mount NTFS/ext/F2FS; this is separate from successful formatting.",
      "نظام ملفات جديد فارغ. قد لا يركّب أندرويد NTFS أو ext أو F2FS ؛ هذا منفصل عن نجاح التهيئة."
    ],
    "guide": "choose",
    "impact": [
      "Erases the selected USB. Back up and identify the target before confirming.",
      "يمحو USB المحددة. انسخ بياناتك وتأكد من الهدف قبل التأكيد."
    ]
  },
  "backup": {
    "goal": [
      "Save a complete copy of a USB, including its partition layout and unused space.",
      "احفظ نسخة كاملة من USB تشمل الأقسام والمساحة غير المستخدمة."
    ],
    "needs": [
      "The source USB, OTG, and a DIFFERENT destination with space for the whole drive.",
      "USB المصدر و OTG ووجهة مختلفة تتسع للوحدة كاملة."
    ],
    "steps": [
      [
        "Select the source USB and tap Create full backup; save the IMG on internal storage or another drive.",
        "اختر USB المصدر واضغط إنشاء نسخة كاملة؛ احفظ IMG على التخزين الداخلي أو وحدة أخرى."
      ],
      [
        "Allow space equal to the ENTIRE USB capacity plus reserve. Keep the source connected until readback finishes.",
        "اترك مساحة تساوي سعة USB كاملة مع احتياط وأبقِ المصدر موصولاً حتى اكتمال القراءة اللاحقة."
      ],
      [
        "To restore later, open Write an image with that IMG. To clone directly to a second USB, use Clone USB instead.",
        "للاستعادة لاحقاً افتح كتابة صورة باستخدام IMG. للنسخ المباشر إلى USB ثانية استخدم نسخ USB."
      ]
    ],
    "result": [
      "A full IMG backup or verified clone. Restore an IMG later with Write an image; the restore destination is erased.",
      "نسخة IMG كاملة أو استنساخ متحقق منه. استعد IMG لاحقاً بكتابة صورة؛ ستُمحى وحدة الاستعادة."
    ],
    "guide": "save",
    "impact": [
      "Phone backup reads the source; direct cloning/restoring overwrites the destination USB.",
      "نسخة الهاتف تقرأ المصدر؛ النسخ المباشر أو الاستعادة يستبدل محتوى USB الوجهة."
    ]
  },
  "clone": {
    "goal": [
      "Save a complete copy of a USB, including its partition layout and unused space.",
      "احفظ نسخة كاملة من USB تشمل الأقسام والمساحة غير المستخدمة."
    ],
    "needs": [
      "The source USB, OTG, and a DIFFERENT destination with space for the whole drive.",
      "USB المصدر و OTG ووجهة مختلفة تتسع للوحدة كاملة."
    ],
    "steps": [
      [
        "Choose USB to phone for an IMG file, or choose a second USB if you want a direct drive clone.",
        "اختر USB إلى الهاتف لإنشاء IMG أو اختر USB ثانية للنسخ المباشر بين الوحدتين."
      ],
      [
        "A 32 GiB drive needs about 32 GiB plus reserve even if it holds only a few files. Use Android Files for files only.",
        "تحتاج وحدة 32 GiB نحو 32 GiB مع احتياط ولو احتوت ملفات قليلة. استخدم تطبيق ملفات أندرويد لنسخ الملفات فقط."
      ],
      [
        "Review source and destination, start, and wait for the saved-image or destination readback comparison.",
        "راجع المصدر والوجهة وابدأ ثم انتظر مقارنة الصورة المحفوظة أو الوحدة الوجهة بالقراءة اللاحقة."
      ]
    ],
    "result": [
      "A full IMG backup or verified clone. Restore an IMG later with Write an image; the restore destination is erased.",
      "نسخة IMG كاملة أو استنساخ متحقق منه. استعد IMG لاحقاً بكتابة صورة؛ ستُمحى وحدة الاستعادة."
    ],
    "guide": "save",
    "impact": [
      "Phone backup reads the source; direct cloning/restoring overwrites the destination USB.",
      "نسخة الهاتف تقرأ المصدر؛ النسخ المباشر أو الاستعادة يستبدل محتوى USB الوجهة."
    ]
  },
  "files": {
    "goal": [
      "Manage files in a folder you explicitly allow Android to share with the app.",
      "أدر ملفات داخل مجلد تسمح لأندرويد بمشاركته مع التطبيق."
    ],
    "needs": [
      "A folder visible in Android’s folder picker; this permission differs from direct USB permission.",
      "مجلد ظاهر في منتقي مجلدات أندرويد؛ إذنه مختلف عن إذن USB المباشر."
    ],
    "steps": [
      [
        "Choose the phone or USB location in Android’s picker and allow access to the intended folder.",
        "اختر مكان الهاتف أو USB في منتقي أندرويد واسمح بالوصول إلى المجلد المقصود."
      ],
      [
        "Open folders to inspect files. Use the file actions for copy, rename, checksum or delete.",
        "افتح المجلدات لتفقد الملفات. استخدم أزرار الملف للنسخ أو إعادة التسمية أو البصمة أو الحذف."
      ],
      [
        "Check the destination before copying. Read delete confirmations carefully; deletion has no undo here.",
        "راجع الوجهة قبل النسخ. اقرأ تأكيد الحذف بعناية؛ لا يوجد تراجع عن الحذف هنا."
      ]
    ],
    "result": [
      "Files saved in the folder you chose. If Android cannot show an NTFS/ext USB, try the separate direct read-only browser.",
      "ملفات محفوظة في المجلد الذي اخترته. إذا لم يعرض أندرويد USB بنظام NTFS/ext فجرّب المتصفح المباشر المنفصل للقراءة فقط."
    ],
    "guide": "fileLocations",
    "impact": [
      "Browsing reads files; copy/rename/delete can change the selected folder. Deletion is permanent.",
      "التصفح يقرأ الملفات؛ النسخ والتسمية والحذف قد تغيّر المجلد. الحذف نهائي."
    ]
  },
  "info": {
    "goal": [
      "Analyze the selected USB’s layout, filesystems, identities and storage usage without changing it.",
      "حلل تخطيط USB المحدد وأنظمة ملفاته وهوياته ومساحته دون تغيير محتواه."
    ],
    "needs": [
      "An attached USB and Android’s USB-access permission.",
      "USB موصول وإذن الوصول إليه من أندرويد."
    ],
    "steps": [
      [
        "Choose the USB by name and capacity; grant the permission prompt.",
        "اختر USB حسب الاسم والسعة ووافق على الإذن."
      ],
      [
        "Run Full USB analyzer. Review each partition and the source of its free-space result.",
        "شغّل محلل USB الشامل. راجع كل قسم ومصدر نتيجة مساحته الحرة."
      ],
      [
        "Save or review the report. A detected signature does not prove every file or sector is healthy.",
        "احفظ التقرير أو راجعه. اكتشاف توقيع لا يثبت سلامة كل ملف أو قطاع."
      ]
    ],
    "result": [
      "A read-only report; nothing was formatted or repaired.",
      "تقرير للقراءة فقط؛ لم تتم تهيئة شيء أو إصلاحه."
    ],
    "guide": "words",
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "hex": {
    "goal": [
      "Analyze the selected USB’s layout, filesystems, identities and storage usage without changing it.",
      "حلل تخطيط USB المحدد وأنظمة ملفاته وهوياته ومساحته دون تغيير محتواه."
    ],
    "needs": [
      "An attached USB and Android’s USB-access permission.",
      "USB موصول وإذن الوصول إليه من أندرويد."
    ],
    "steps": [
      [
        "Choose the USB by name and capacity; grant the permission prompt.",
        "اختر USB حسب الاسم والسعة ووافق على الإذن."
      ],
      [
        "Run Full USB analyzer. Review each partition and the source of its free-space result.",
        "شغّل محلل USB الشامل. راجع كل قسم ومصدر نتيجة مساحته الحرة."
      ],
      [
        "Save or review the report. A detected signature does not prove every file or sector is healthy.",
        "احفظ التقرير أو راجعه. اكتشاف توقيع لا يثبت سلامة كل ملف أو قطاع."
      ]
    ],
    "result": [
      "A read-only report; nothing was formatted or repaired.",
      "تقرير للقراءة فقط؛ لم تتم تهيئة شيء أو إصلاحه."
    ],
    "guide": "words",
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "verify": {
    "goal": [
      "Compare your image file with bytes already written on a USB.",
      "قارن ملف الصورة بالبايتات المكتوبة على USB."
    ],
    "needs": [
      "The SAME original image and the USB you wrote it to.",
      "الصورة الأصلية نفسها و USB التي كُتبت عليها."
    ],
    "steps": [
      [
        "Choose the original file and the matching USB. Do not select a different downloaded version.",
        "اختر الملف الأصلي و USB المطابقة ولا تختر إصداراً منزلاً مختلفاً."
      ],
      [
        "Start comparison and keep the file, cable and drive available until it ends.",
        "ابدأ المقارنة وأبقِ الملف والكابل والوحدة متاحة حتى تنتهي."
      ],
      [
        "A mismatch means the compared bytes differ; check the file, target and connection before writing again.",
        "عدم التطابق يعني اختلاف البايتات المقارنة؛ تحقق من الملف والوجهة والاتصال قبل إعادة الكتابة."
      ]
    ],
    "result": [
      "Match or a precise comparison failure. It does not prove publisher authenticity or that a computer will boot.",
      "تطابق أو فشل مقارنة محدد. لا يثبت أصالة الناشر أو إقلاع الحاسوب."
    ],
    "guide": "flash",
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "benchmark": {
    "goal": [
      "Read the USB to measure speed or find read failures without overwriting data.",
      "اقرأ USB لقياس السرعة أو اكتشاف فشل القراءة دون استبدال البيانات."
    ],
    "needs": [
      "A stable USB connection; stop other transfers for a meaningful measurement.",
      "اتصال USB مستقر؛ أوقف النقل الآخر للحصول على قياس مفيد."
    ],
    "steps": [
      [
        "Choose sequential benchmark for sustained speed, random benchmark for small scattered reads, or surface scan to read the full addressable drive.",
        "اختر القياس التسلسلي للسرعة المتواصلة أو العشوائي للقراءات الصغيرة المتفرقة أو فحص السطح لقراءة كامل الوحدة المتاحة."
      ],
      [
        "Keep the drive connected. A full scan may take much longer than a short benchmark.",
        "أبقِ الوحدة موصولة. قد يستغرق الفحص الكامل أطول بكثير من قياس قصير."
      ],
      [
        "Compare results only under similar cables, hubs and conditions. A slow or failed read needs investigation, not immediate formatting.",
        "قارن النتائج مع كابلات وموزعات وظروف متشابهة. القراءة البطيئة أو الفاشلة تحتاج فحص السبب وليس التهيئة فوراً."
      ]
    ],
    "result": [
      "Speed or read-scan results. Read-only tests do not prove real flash capacity, repair damage or predict lifetime.",
      "نتائج سرعة أو فحص قراءة. لا تثبت اختبارات القراءة السعة الحقيقية للفلاش ولا تصلح التلف ولا تتنبأ بالعمر."
    ],
    "guide": "advanced",
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "randomread": {
    "goal": [
      "Read the USB to measure speed or find read failures without overwriting data.",
      "اقرأ USB لقياس السرعة أو اكتشاف فشل القراءة دون استبدال البيانات."
    ],
    "needs": [
      "A stable USB connection; stop other transfers for a meaningful measurement.",
      "اتصال USB مستقر؛ أوقف النقل الآخر للحصول على قياس مفيد."
    ],
    "steps": [
      [
        "Choose sequential benchmark for sustained speed, random benchmark for small scattered reads, or surface scan to read the full addressable drive.",
        "اختر القياس التسلسلي للسرعة المتواصلة أو العشوائي للقراءات الصغيرة المتفرقة أو فحص السطح لقراءة كامل الوحدة المتاحة."
      ],
      [
        "Keep the drive connected. A full scan may take much longer than a short benchmark.",
        "أبقِ الوحدة موصولة. قد يستغرق الفحص الكامل أطول بكثير من قياس قصير."
      ],
      [
        "Compare results only under similar cables, hubs and conditions. A slow or failed read needs investigation, not immediate formatting.",
        "قارن النتائج مع كابلات وموزعات وظروف متشابهة. القراءة البطيئة أو الفاشلة تحتاج فحص السبب وليس التهيئة فوراً."
      ]
    ],
    "result": [
      "Speed or read-scan results. Read-only tests do not prove real flash capacity, repair damage or predict lifetime.",
      "نتائج سرعة أو فحص قراءة. لا تثبت اختبارات القراءة السعة الحقيقية للفلاش ولا تصلح التلف ولا تتنبأ بالعمر."
    ],
    "guide": "advanced",
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "scan": {
    "goal": [
      "Read the USB to measure speed or find read failures without overwriting data.",
      "اقرأ USB لقياس السرعة أو اكتشاف فشل القراءة دون استبدال البيانات."
    ],
    "needs": [
      "A stable USB connection; stop other transfers for a meaningful measurement.",
      "اتصال USB مستقر؛ أوقف النقل الآخر للحصول على قياس مفيد."
    ],
    "steps": [
      [
        "Choose sequential benchmark for sustained speed, random benchmark for small scattered reads, or surface scan to read the full addressable drive.",
        "اختر القياس التسلسلي للسرعة المتواصلة أو العشوائي للقراءات الصغيرة المتفرقة أو فحص السطح لقراءة كامل الوحدة المتاحة."
      ],
      [
        "Keep the drive connected. A full scan may take much longer than a short benchmark.",
        "أبقِ الوحدة موصولة. قد يستغرق الفحص الكامل أطول بكثير من قياس قصير."
      ],
      [
        "Compare results only under similar cables, hubs and conditions. A slow or failed read needs investigation, not immediate formatting.",
        "قارن النتائج مع كابلات وموزعات وظروف متشابهة. القراءة البطيئة أو الفاشلة تحتاج فحص السبب وليس التهيئة فوراً."
      ]
    ],
    "result": [
      "Speed or read-scan results. Read-only tests do not prove real flash capacity, repair damage or predict lifetime.",
      "نتائج سرعة أو فحص قراءة. لا تثبت اختبارات القراءة السعة الحقيقية للفلاش ولا تصلح التلف ولا تتنبأ بالعمر."
    ],
    "guide": "advanced",
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "hash": {
    "goal": [
      "Check whether a file matches a trusted checksum or another copy.",
      "تحقق هل يطابق الملف بصمة موثوقة أو نسخة أخرى."
    ],
    "needs": [
      "Your file and, for authenticity checking, a checksum obtained from its official publisher.",
      "ملفك وبصمة من ناشره الرسمي إذا أردت التحقق من المصدر."
    ],
    "steps": [
      [
        "Choose the file. Use SHA-256 for modern comparisons.",
        "اختر الملف واستخدم SHA-256 للمقارنات الحديثة."
      ],
      [
        "Paste the publisher’s full SHA-256 if the tool offers comparison; do not compare SHA-256 with an MD5 value.",
        "ألصق SHA-256 الكاملة للناشر إذا توفر حقل المقارنة؛ لا تقارنها بقيمة MD5."
      ],
      [
        "Wait for every byte to be read, then compare the complete value rather than only its first characters.",
        "انتظر قراءة كل البايتات ثم قارن القيمة كاملة وليس أحرفها الأولى فقط."
      ]
    ],
    "result": [
      "A fingerprint and optional match. A hash without a trusted reference is not proof of who created the file.",
      "بصمة مع تطابق اختياري. البصمة دون مرجع موثوق ليست إثباتاً لمن أنشأ الملف."
    ],
    "guide": "fileBasics",
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "checksums": {
    "goal": [
      "Check whether a file matches a trusted checksum or another copy.",
      "تحقق هل يطابق الملف بصمة موثوقة أو نسخة أخرى."
    ],
    "needs": [
      "Your file and, for authenticity checking, a checksum obtained from its official publisher.",
      "ملفك وبصمة من ناشره الرسمي إذا أردت التحقق من المصدر."
    ],
    "steps": [
      [
        "Choose the file. Use SHA-256 for modern comparisons.",
        "اختر الملف واستخدم SHA-256 للمقارنات الحديثة."
      ],
      [
        "Paste the publisher’s full SHA-256 if the tool offers comparison; do not compare SHA-256 with an MD5 value.",
        "ألصق SHA-256 الكاملة للناشر إذا توفر حقل المقارنة؛ لا تقارنها بقيمة MD5."
      ],
      [
        "Wait for every byte to be read, then compare the complete value rather than only its first characters.",
        "انتظر قراءة كل البايتات ثم قارن القيمة كاملة وليس أحرفها الأولى فقط."
      ]
    ],
    "result": [
      "A fingerprint and optional match. A hash without a trusted reference is not proof of who created the file.",
      "بصمة مع تطابق اختياري. البصمة دون مرجع موثوق ليست إثباتاً لمن أنشأ الملف."
    ],
    "guide": "fileBasics",
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "wipe": {
    "goal": [
      "Choose the right kind of test or zero check before touching a drive.",
      "اختر نوع الاختبار أو فحص الأصفار الصحيح قبل التعامل مع الوحدة."
    ],
    "needs": [
      "For wiping/capacity testing, a disposable or fully backed-up USB. Zero verification only reads.",
      "للمسح أو اختبار السعة USB تجريبية أو منسوخة بالكامل. فحص الأصفار يقرأ فقط."
    ],
    "steps": [
      [
        "Zero-fill overwrites addressable bytes with zeros; Full capacity test writes patterns. BOTH destroy existing data.",
        "الملء بالأصفار يستبدل البايتات المتاحة بأصفار؛ اختبار السعة الكامل يكتب أنماطاً. كلاهما يمحو البيانات الحالية."
      ],
      [
        "Verify zero-filled USB only checks an already zeroed drive. A normal formatted drive is expected to contain nonzero bytes.",
        "فحص USB المصفّرة يفحص وحدة سبق ملؤها بالأصفار فقط. من الطبيعي أن تحتوي الوحدة المهيأة بايتات غير صفرية."
      ],
      [
        "Review the selected drive and action, confirm destructive modes only when intended, and wait for complete readback.",
        "راجع الوحدة والإجراء وأكّد الأوضاع المدمرة فقط إذا قصدتها وانتظر القراءة اللاحقة الكاملة."
      ]
    ],
    "result": [
      "A full test/readback result or first mismatch. Zero reports are local records, not certified hidden-flash sanitization.",
      "نتيجة اختبار أو قراءة لاحقة كاملة أو أول اختلاف. تقارير الأصفار سجلات محلية وليست تعقيماً معتمداً لمساحة الفلاش المخفية."
    ],
    "guide": "safe",
    "impact": [
      "Erases the selected USB. Back up and identify the target before confirming.",
      "يمحو USB المحددة. انسخ بياناتك وتأكد من الهدف قبل التأكيد."
    ]
  },
  "capacity": {
    "goal": [
      "Choose the right kind of test or zero check before touching a drive.",
      "اختر نوع الاختبار أو فحص الأصفار الصحيح قبل التعامل مع الوحدة."
    ],
    "needs": [
      "For wiping/capacity testing, a disposable or fully backed-up USB. Zero verification only reads.",
      "للمسح أو اختبار السعة USB تجريبية أو منسوخة بالكامل. فحص الأصفار يقرأ فقط."
    ],
    "steps": [
      [
        "Zero-fill overwrites addressable bytes with zeros; Full capacity test writes patterns. BOTH destroy existing data.",
        "الملء بالأصفار يستبدل البايتات المتاحة بأصفار؛ اختبار السعة الكامل يكتب أنماطاً. كلاهما يمحو البيانات الحالية."
      ],
      [
        "Verify zero-filled USB only checks an already zeroed drive. A normal formatted drive is expected to contain nonzero bytes.",
        "فحص USB المصفّرة يفحص وحدة سبق ملؤها بالأصفار فقط. من الطبيعي أن تحتوي الوحدة المهيأة بايتات غير صفرية."
      ],
      [
        "Review the selected drive and action, confirm destructive modes only when intended, and wait for complete readback.",
        "راجع الوحدة والإجراء وأكّد الأوضاع المدمرة فقط إذا قصدتها وانتظر القراءة اللاحقة الكاملة."
      ]
    ],
    "result": [
      "A full test/readback result or first mismatch. Zero reports are local records, not certified hidden-flash sanitization.",
      "نتيجة اختبار أو قراءة لاحقة كاملة أو أول اختلاف. تقارير الأصفار سجلات محلية وليست تعقيماً معتمداً لمساحة الفلاش المخفية."
    ],
    "guide": "safe",
    "impact": [
      "Erases the selected USB. Back up and identify the target before confirming.",
      "يمحو USB المحددة. انسخ بياناتك وتأكد من الهدف قبل التأكيد."
    ]
  },
  "verifyZero": {
    "goal": [
      "Choose the right kind of test or zero check before touching a drive.",
      "اختر نوع الاختبار أو فحص الأصفار الصحيح قبل التعامل مع الوحدة."
    ],
    "needs": [
      "For wiping/capacity testing, a disposable or fully backed-up USB. Zero verification only reads.",
      "للمسح أو اختبار السعة USB تجريبية أو منسوخة بالكامل. فحص الأصفار يقرأ فقط."
    ],
    "steps": [
      [
        "Zero-fill overwrites addressable bytes with zeros; Full capacity test writes patterns. BOTH destroy existing data.",
        "الملء بالأصفار يستبدل البايتات المتاحة بأصفار؛ اختبار السعة الكامل يكتب أنماطاً. كلاهما يمحو البيانات الحالية."
      ],
      [
        "Verify zero-filled USB only checks an already zeroed drive. A normal formatted drive is expected to contain nonzero bytes.",
        "فحص USB المصفّرة يفحص وحدة سبق ملؤها بالأصفار فقط. من الطبيعي أن تحتوي الوحدة المهيأة بايتات غير صفرية."
      ],
      [
        "Review the selected drive and action, confirm destructive modes only when intended, and wait for complete readback.",
        "راجع الوحدة والإجراء وأكّد الأوضاع المدمرة فقط إذا قصدتها وانتظر القراءة اللاحقة الكاملة."
      ]
    ],
    "result": [
      "A full test/readback result or first mismatch. Zero reports are local records, not certified hidden-flash sanitization.",
      "نتيجة اختبار أو قراءة لاحقة كاملة أو أول اختلاف. تقارير الأصفار سجلات محلية وليست تعقيماً معتمداً لمساحة الفلاش المخفية."
    ],
    "guide": "safe",
    "hint": [
      "This mode is read-only. Ordinary filesystems contain nonzero data, so a nonzero result is normal.",
      "هذا الوضع للقراءة فقط. تحتوي أنظمة الملفات العادية بيانات غير صفرية؛ لذا هذه النتيجة طبيعية."
    ],
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "zip": {
    "goal": [
      "See what is inside an archive, then copy supported contents into a new folder.",
      "اعرف محتويات الأرشيف ثم انسخ المحتويات المدعومة إلى مجلد جديد."
    ],
    "needs": [
      "A supported archive and a different destination with enough space for EXPANDED files.",
      "أرشيف مدعوم ووجهة أخرى تتسع للملفات بعد فكها."
    ],
    "steps": [
      [
        "Choose the archive. Listing and extraction are different actions; listing does not save its files.",
        "اختر الأرشيف. العرض والاستخراج إجراءان مختلفان؛ العرض لا يحفظ ملفاته."
      ],
      [
        "Choose a destination and an expansion limit where offered. A small compressed file can unpack into much more data.",
        "اختر وجهة وحداً لحجم الفك إن توفر. قد يتمدد ملف مضغوط صغير إلى بيانات أكبر بكثير."
      ],
      [
        "Extract into the new subfolder and wait for completion. Password, multipart or unsupported codecs can be refused.",
        "استخرج إلى المجلد الفرعي الجديد وانتظر الاكتمال. قد تُرفض كلمات المرور أو الأجزاء المتعددة أو الترميزات غير المدعومة."
      ]
    ],
    "result": [
      "A listing or a new extracted folder. Original archive paths cannot write outside the app-created folder.",
      "قائمة أو مجلد جديد مستخرج. لا تستطيع مسارات الأرشيف الأصلية الكتابة خارج مجلد التطبيق الجديد."
    ],
    "guide": "archiveImages",
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "extract": {
    "goal": [
      "See what is inside an archive, then copy supported contents into a new folder.",
      "اعرف محتويات الأرشيف ثم انسخ المحتويات المدعومة إلى مجلد جديد."
    ],
    "needs": [
      "A supported archive and a different destination with enough space for EXPANDED files.",
      "أرشيف مدعوم ووجهة أخرى تتسع للملفات بعد فكها."
    ],
    "steps": [
      [
        "Choose the archive. Listing and extraction are different actions; listing does not save its files.",
        "اختر الأرشيف. العرض والاستخراج إجراءان مختلفان؛ العرض لا يحفظ ملفاته."
      ],
      [
        "Choose a destination and an expansion limit where offered. A small compressed file can unpack into much more data.",
        "اختر وجهة وحداً لحجم الفك إن توفر. قد يتمدد ملف مضغوط صغير إلى بيانات أكبر بكثير."
      ],
      [
        "Extract into the new subfolder and wait for completion. Password, multipart or unsupported codecs can be refused.",
        "استخرج إلى المجلد الفرعي الجديد وانتظر الاكتمال. قد تُرفض كلمات المرور أو الأجزاء المتعددة أو الترميزات غير المدعومة."
      ]
    ],
    "result": [
      "A listing or a new extracted folder. Original archive paths cannot write outside the app-created folder.",
      "قائمة أو مجلد جديد مستخرج. لا تستطيع مسارات الأرشيف الأصلية الكتابة خارج مجلد التطبيق الجديد."
    ],
    "guide": "archiveImages",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "archives": {
    "goal": [
      "See what is inside an archive, then copy supported contents into a new folder.",
      "اعرف محتويات الأرشيف ثم انسخ المحتويات المدعومة إلى مجلد جديد."
    ],
    "needs": [
      "A supported archive and a different destination with enough space for EXPANDED files.",
      "أرشيف مدعوم ووجهة أخرى تتسع للملفات بعد فكها."
    ],
    "steps": [
      [
        "Choose the archive. Listing and extraction are different actions; listing does not save its files.",
        "اختر الأرشيف. العرض والاستخراج إجراءان مختلفان؛ العرض لا يحفظ ملفاته."
      ],
      [
        "Choose a destination and an expansion limit where offered. A small compressed file can unpack into much more data.",
        "اختر وجهة وحداً لحجم الفك إن توفر. قد يتمدد ملف مضغوط صغير إلى بيانات أكبر بكثير."
      ],
      [
        "Extract into the new subfolder and wait for completion. Password, multipart or unsupported codecs can be refused.",
        "استخرج إلى المجلد الفرعي الجديد وانتظر الاكتمال. قد تُرفض كلمات المرور أو الأجزاء المتعددة أو الترميزات غير المدعومة."
      ]
    ],
    "result": [
      "A listing or a new extracted folder. Original archive paths cannot write outside the app-created folder.",
      "قائمة أو مجلد جديد مستخرج. لا تستطيع مسارات الأرشيف الأصلية الكتابة خارج مجلد التطبيق الجديد."
    ],
    "guide": "archiveImages",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "tar": {
    "goal": [
      "See what is inside an archive, then copy supported contents into a new folder.",
      "اعرف محتويات الأرشيف ثم انسخ المحتويات المدعومة إلى مجلد جديد."
    ],
    "needs": [
      "A supported archive and a different destination with enough space for EXPANDED files.",
      "أرشيف مدعوم ووجهة أخرى تتسع للملفات بعد فكها."
    ],
    "steps": [
      [
        "Choose the archive. Listing and extraction are different actions; listing does not save its files.",
        "اختر الأرشيف. العرض والاستخراج إجراءان مختلفان؛ العرض لا يحفظ ملفاته."
      ],
      [
        "Choose a destination and an expansion limit where offered. A small compressed file can unpack into much more data.",
        "اختر وجهة وحداً لحجم الفك إن توفر. قد يتمدد ملف مضغوط صغير إلى بيانات أكبر بكثير."
      ],
      [
        "Extract into the new subfolder and wait for completion. Password, multipart or unsupported codecs can be refused.",
        "استخرج إلى المجلد الفرعي الجديد وانتظر الاكتمال. قد تُرفض كلمات المرور أو الأجزاء المتعددة أو الترميزات غير المدعومة."
      ]
    ],
    "result": [
      "A listing or a new extracted folder. Original archive paths cannot write outside the app-created folder.",
      "قائمة أو مجلد جديد مستخرج. لا تستطيع مسارات الأرشيف الأصلية الكتابة خارج مجلد التطبيق الجديد."
    ],
    "guide": "archiveImages",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "windows": {
    "goal": [
      "Prepare supported Windows setup files for UEFI computers.",
      "جهّز ملفات إعداد Windows المدعومة لحواسيب UEFI."
    ],
    "needs": [
      "Your own supported Windows ISO and an empty FAT32 destination visible to Android’s folder picker.",
      "Windows ISO مدعومة خاصة بك ووجهة FAT32 فارغة تظهر في منتقي أندرويد."
    ],
    "steps": [
      [
        "Get the ISO from its official publisher yourself. This app does not supply or download Windows.",
        "احصل على ISO من ناشرها الرسمي بنفسك؛ لا يوفر التطبيق Windows ولا ينزّله."
      ],
      [
        "Choose the ISO and the empty USB folder. Review the detected architecture and preparation details.",
        "اختر ISO ومجلد USB الفارغ وراجع المعمارية المكتشفة وتفاصيل التجهيز."
      ],
      [
        "Start preparation; large supported install.wim files are split for FAT32. Wait for verification, then test the PC’s UEFI boot menu.",
        "ابدأ التجهيز؛ تُقسم install.wim الكبيرة المدعومة لتناسب FAT32. انتظر التحقق ثم اختبر قائمة إقلاع UEFI في الحاسوب."
      ]
    ],
    "result": [
      "Prepared setup files, still experimental until PC-tested. This is not BIOS boot setup, Windows To Go or Windows running on Android.",
      "ملفات إعداد مجهزة تظل تجريبية حتى اختبار الحاسوب. ليست تجهيز BIOS أو Windows To Go أوتشغيل Windows على أندرويد."
    ],
    "guide": "windowsGuide",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "ventoyInstall": {
    "goal": [
      "Install supported Ventoy boot data or update a recognized existing layout.",
      "ثبّت بيانات إقلاع Ventoy المدعومة أو حدّث تخطيطاً موجوداً معروفاً."
    ],
    "needs": [
      "Your imported official Ventoy package, a supported USB, and a separate backup.",
      "حزمة Ventoy الرسمية التي تستوردها و USB مدعومة ونسخة احتياطية منفصلة."
    ],
    "steps": [
      [
        "Import your official package first. Importing alone changes no USB and does not run downloaded programs.",
        "استورد الحزمة الرسمية أولاً. الاستيراد وحده لا يغيّر USB ولا يشغّل برامج منزلة."
      ],
      [
        "Initial installation erases the USB. Updating is for recognized existing default layouts only; choose the matching MBR/GPT action.",
        "التثبيت الأولي يمحو USB. التحديث للتخطيطات الافتراضية الموجودة المعروفة فقط؛ اختر إجراء MBR/GPT المطابق."
      ],
      [
        "Review the target, confirm the intended operation and wait for readback. Reconnect safely, then add your own ISOs to the data partition.",
        "راجع الهدف وأكّد العملية المقصودة وانتظر القراءة اللاحقة. أعد التوصيل بأمان ثم أضف ISO الخاصة بك إلى قسم البيانات."
      ]
    ],
    "result": [
      "Experimental installed/updated boot data. Test on a PC; custom layouts and universal boot compatibility are not promised.",
      "بيانات إقلاع مثبتة أو محدثة تجريبياً. اختبر على حاسوب؛ لا يُضمن دعم التخطيطات المخصصة أو كل الأجهزة."
    ],
    "guide": "ventoyGuide",
    "impact": [
      "Erases the selected USB. Back up and identify the target before confirming.",
      "يمحو USB المحددة. انسخ بياناتك وتأكد من الهدف قبل التأكيد."
    ]
  },
  "ventoyGpt": {
    "goal": [
      "Install supported Ventoy boot data or update a recognized existing layout.",
      "ثبّت بيانات إقلاع Ventoy المدعومة أو حدّث تخطيطاً موجوداً معروفاً."
    ],
    "needs": [
      "Your imported official Ventoy package, a supported USB, and a separate backup.",
      "حزمة Ventoy الرسمية التي تستوردها و USB مدعومة ونسخة احتياطية منفصلة."
    ],
    "steps": [
      [
        "Import your official package first. Importing alone changes no USB and does not run downloaded programs.",
        "استورد الحزمة الرسمية أولاً. الاستيراد وحده لا يغيّر USB ولا يشغّل برامج منزلة."
      ],
      [
        "Initial installation erases the USB. Updating is for recognized existing default layouts only; choose the matching MBR/GPT action.",
        "التثبيت الأولي يمحو USB. التحديث للتخطيطات الافتراضية الموجودة المعروفة فقط؛ اختر إجراء MBR/GPT المطابق."
      ],
      [
        "Review the target, confirm the intended operation and wait for readback. Reconnect safely, then add your own ISOs to the data partition.",
        "راجع الهدف وأكّد العملية المقصودة وانتظر القراءة اللاحقة. أعد التوصيل بأمان ثم أضف ISO الخاصة بك إلى قسم البيانات."
      ]
    ],
    "result": [
      "Experimental installed/updated boot data. Test on a PC; custom layouts and universal boot compatibility are not promised.",
      "بيانات إقلاع مثبتة أو محدثة تجريبياً. اختبر على حاسوب؛ لا يُضمن دعم التخطيطات المخصصة أو كل الأجهزة."
    ],
    "guide": "ventoyGuide",
    "impact": [
      "Initial installation erases the drive; updates change recognized boot areas. Back up before either action.",
      "التثبيت الأولي يمحو الوحدة؛ التحديث يغيّر مناطق إقلاع معروفة. انسخ بياناتك قبل أي منهما."
    ]
  },
  "ventoyUpdate": {
    "goal": [
      "Install supported Ventoy boot data or update a recognized existing layout.",
      "ثبّت بيانات إقلاع Ventoy المدعومة أو حدّث تخطيطاً موجوداً معروفاً."
    ],
    "needs": [
      "Your imported official Ventoy package, a supported USB, and a separate backup.",
      "حزمة Ventoy الرسمية التي تستوردها و USB مدعومة ونسخة احتياطية منفصلة."
    ],
    "steps": [
      [
        "Import your official package first. Importing alone changes no USB and does not run downloaded programs.",
        "استورد الحزمة الرسمية أولاً. الاستيراد وحده لا يغيّر USB ولا يشغّل برامج منزلة."
      ],
      [
        "Initial installation erases the USB. Updating is for recognized existing default layouts only; choose the matching MBR/GPT action.",
        "التثبيت الأولي يمحو USB. التحديث للتخطيطات الافتراضية الموجودة المعروفة فقط؛ اختر إجراء MBR/GPT المطابق."
      ],
      [
        "Review the target, confirm the intended operation and wait for readback. Reconnect safely, then add your own ISOs to the data partition.",
        "راجع الهدف وأكّد العملية المقصودة وانتظر القراءة اللاحقة. أعد التوصيل بأمان ثم أضف ISO الخاصة بك إلى قسم البيانات."
      ]
    ],
    "result": [
      "Experimental installed/updated boot data. Test on a PC; custom layouts and universal boot compatibility are not promised.",
      "بيانات إقلاع مثبتة أو محدثة تجريبياً. اختبر على حاسوب؛ لا يُضمن دعم التخطيطات المخصصة أو كل الأجهزة."
    ],
    "guide": "ventoyGuide",
    "impact": [
      "Initial installation erases the drive; updates change recognized boot areas. Back up before either action.",
      "التثبيت الأولي يمحو الوحدة؛ التحديث يغيّر مناطق إقلاع معروفة. انسخ بياناتك قبل أي منهما."
    ]
  },
  "ventoy": {
    "goal": [
      "Copy several of your ISO files onto a USB that already has Ventoy installed.",
      "انسخ عدة ملفات ISO خاصة بك إلى USB مثبّت عليها Ventoy مسبقاً."
    ],
    "needs": [
      "An existing Ventoy USB data partition and your own ISO files.",
      "قسم بيانات USB مجهزةبـ Ventoy وملفات ISO الخاصة بك."
    ],
    "steps": [
      [
        "Choose the large Ventoy data partition in Android’s folder picker, not its small EFI partition.",
        "اختر قسم بيانات Ventoy الكبير في منتقي أندرويد وليس قسم EFI الصغير."
      ],
      [
        "Add your ISO files and keep enough free space for all of them. Copying does not install Ventoy.",
        "أضف ملفات ISO واترك مساحة تكفيها جميعاً. النسخ لا يثبت Ventoy."
      ],
      [
        "Wait for copying, then safely reconnect/test the USB on the intended computer.",
        "انتظر النسخ ثم أعد التوصيل بأمان واختبر USB على الحاسوب المقصود."
      ]
    ],
    "result": [
      "An ISO library on an already prepared drive. Each ISO’s boot support depends on Ventoy and the target computer.",
      "مكتبة ISO على وحدة مجهزة مسبقاً. يعتمد دعم إقلاع كل ISO على Ventoy والحاسوب الهدف."
    ],
    "guide": "multi",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "optical": {
    "goal": [
      "Browse files inside a supported disk/filesystem image or directly on a supported USB.",
      "تصفح ملفات داخل صورة قرص أو نظام ملفات مدعوم أو مباشرة على USB مدعومة."
    ],
    "needs": [
      "Choose the matching reader: ISO/UDF, FAT16/32 image, NTFS/ext image, or direct NTFS/ext USB.",
      "اختر القارئ المطابق: ISO/UDF أوصورة FAT16/32 أوصورة NTFS/ext أو USB NTFS/ext مباشرة."
    ],
    "steps": [
      [
        "For image tools select the image file; for direct USB choose the physical drive and grant USB permission.",
        "لأدوات الصور اختر ملف الصورة؛ ولـ USB المباشرة اختر الوحدة الفعلية وامنح إذن USB."
      ],
      [
        "Where requested choose the actual partition number (usually1;0 only without a partition table). List files before extraction.",
        "اختر رقم القسم الفعلي عند طلبه (عادة١؛ و٠ دون جدول أقسام فقط). اعرض الملفات قبل استخراجها."
      ],
      [
        "Choose a new-folder destination and extract. Direct NTFS/ext USB exports require primary INTERNAL storage.",
        "اختر وجهة لمجلد جديد واستخرج. يتطلب تصدير USB NTFS/ext المباشر التخزين الداخلي الرئيسي."
      ]
    ],
    "result": [
      "Listed files or a new folder of supported extracted files. No live mounting, deleted recovery or source repair is performed.",
      "قائمة ملفات أومجلد جديد للملفات المستخرجة المدعومة. لا تُركب الوحدة ولا تُستعاد المحذوفات ولا يُصلح المصدر."
    ],
    "guide": "archiveImages",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "fatImage": {
    "goal": [
      "Browse files inside a supported disk/filesystem image or directly on a supported USB.",
      "تصفح ملفات داخل صورة قرص أو نظام ملفات مدعوم أو مباشرة على USB مدعومة."
    ],
    "needs": [
      "Choose the matching reader: ISO/UDF, FAT16/32 image, NTFS/ext image, or direct NTFS/ext USB.",
      "اختر القارئ المطابق: ISO/UDF أوصورة FAT16/32 أوصورة NTFS/ext أو USB NTFS/ext مباشرة."
    ],
    "steps": [
      [
        "For image tools select the image file; for direct USB choose the physical drive and grant USB permission.",
        "لأدوات الصور اختر ملف الصورة؛ ولـ USB المباشرة اختر الوحدة الفعلية وامنح إذن USB."
      ],
      [
        "Where requested choose the actual partition number (usually1;0 only without a partition table). List files before extraction.",
        "اختر رقم القسم الفعلي عند طلبه (عادة١؛ و٠ دون جدول أقسام فقط). اعرض الملفات قبل استخراجها."
      ],
      [
        "Choose a new-folder destination and extract. Direct NTFS/ext USB exports require primary INTERNAL storage.",
        "اختر وجهة لمجلد جديد واستخرج. يتطلب تصدير USB NTFS/ext المباشر التخزين الداخلي الرئيسي."
      ]
    ],
    "result": [
      "Listed files or a new folder of supported extracted files. No live mounting, deleted recovery or source repair is performed.",
      "قائمة ملفات أومجلد جديد للملفات المستخرجة المدعومة. لا تُركب الوحدة ولا تُستعاد المحذوفات ولا يُصلح المصدر."
    ],
    "guide": "gptFat",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "fsImage": {
    "goal": [
      "Browse files inside a supported disk/filesystem image or directly on a supported USB.",
      "تصفح ملفات داخل صورة قرص أو نظام ملفات مدعوم أو مباشرة على USB مدعومة."
    ],
    "needs": [
      "Choose the matching reader: ISO/UDF, FAT16/32 image, NTFS/ext image, or direct NTFS/ext USB.",
      "اختر القارئ المطابق: ISO/UDF أوصورة FAT16/32 أوصورة NTFS/ext أو USB NTFS/ext مباشرة."
    ],
    "steps": [
      [
        "For image tools select the image file; for direct USB choose the physical drive and grant USB permission.",
        "لأدوات الصور اختر ملف الصورة؛ ولـ USB المباشرة اختر الوحدة الفعلية وامنح إذن USB."
      ],
      [
        "Where requested choose the actual partition number (usually1;0 only without a partition table). List files before extraction.",
        "اختر رقم القسم الفعلي عند طلبه (عادة١؛ و٠ دون جدول أقسام فقط). اعرض الملفات قبل استخراجها."
      ],
      [
        "Choose a new-folder destination and extract. Direct NTFS/ext USB exports require primary INTERNAL storage.",
        "اختر وجهة لمجلد جديد واستخرج. يتطلب تصدير USB NTFS/ext المباشر التخزين الداخلي الرئيسي."
      ]
    ],
    "result": [
      "Listed files or a new folder of supported extracted files. No live mounting, deleted recovery or source repair is performed.",
      "قائمة ملفات أومجلد جديد للملفات المستخرجة المدعومة. لا تُركب الوحدة ولا تُستعاد المحذوفات ولا يُصلح المصدر."
    ],
    "guide": "archiveImages",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "fsUsb": {
    "goal": [
      "Browse files inside a supported disk/filesystem image or directly on a supported USB.",
      "تصفح ملفات داخل صورة قرص أو نظام ملفات مدعوم أو مباشرة على USB مدعومة."
    ],
    "needs": [
      "Choose the matching reader: ISO/UDF, FAT16/32 image, NTFS/ext image, or direct NTFS/ext USB.",
      "اختر القارئ المطابق: ISO/UDF أوصورة FAT16/32 أوصورة NTFS/ext أو USB NTFS/ext مباشرة."
    ],
    "steps": [
      [
        "For image tools select the image file; for direct USB choose the physical drive and grant USB permission.",
        "لأدوات الصور اختر ملف الصورة؛ ولـ USB المباشرة اختر الوحدة الفعلية وامنح إذن USB."
      ],
      [
        "Where requested choose the actual partition number (usually1;0 only without a partition table). List files before extraction.",
        "اختر رقم القسم الفعلي عند طلبه (عادة١؛ و٠ دون جدول أقسام فقط). اعرض الملفات قبل استخراجها."
      ],
      [
        "Choose a new-folder destination and extract. Direct NTFS/ext USB exports require primary INTERNAL storage.",
        "اختر وجهة لمجلد جديد واستخرج. يتطلب تصدير USB NTFS/ext المباشر التخزين الداخلي الرئيسي."
      ]
    ],
    "result": [
      "Listed files or a new folder of supported extracted files. No live mounting, deleted recovery or source repair is performed.",
      "قائمة ملفات أومجلد جديد للملفات المستخرجة المدعومة. لا تُركب الوحدة ولا تُستعاد المحذوفات ولا يُصلح المصدر."
    ],
    "guide": "archiveImages",
    "hint": [
      "This reads directly without Android mounting; extraction must go to primary internal storage.",
      "تقرأ مباشرة دون تركيب أندرويد؛ يجب أن يذهب التصدير إلى التخزين الداخلي الرئيسي."
    ],
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "wimSplit": {
    "goal": [
      "Split a supported large install.wim into parts small enough for FAT32.",
      "قسّم install.wim الكبيرة المدعومة إلى أجزاء تناسب FAT32."
    ],
    "needs": [
      "A standalone supported WIM and space for all output parts plus reserve.",
      "WIM مستقلة مدعومة ومساحة تكفي كل الأجزاء الناتجة مع احتياط."
    ],
    "steps": [
      [
        "Choose the WIM itself, not its containing ISO; use ISO extraction first if necessary.",
        "اختر ملف WIM نفسه وليس ISO التي تحتويه؛ استخدم استخراج ISO أولاً عند الحاجة."
      ],
      [
        "Choose the destination and split. Keep the generated install.swm, install2.swm and other parts together.",
        "اختر الوجهة وقسّم. احتفظ بـ install.swm و install2.swm وباقي الأجزاء معاً."
      ],
      [
        "Wait for verification of all parts before copying them into a compatible installer’s sources folder.",
        "انتظر التحقق من جميع الأجزاء قبل نسخها إلى مجلد sources لمثبت متوافق."
      ]
    ],
    "result": [
      "Verified SWM parts. Splitting alone does not create USB boot code or a complete Windows installer.",
      "أجزاء SWM متحقق منها. التقسيم وحده لا ينشئ بيانات إقلاع USB أو مثبت Windows كاملاً."
    ],
    "guide": "windowsGuide",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "wimExplore": {
    "goal": [
      "Read one file or a bounded folder from a WIM/ESD without restoring Windows.",
      "اقرأ ملفاً واحداً أو مجلداً محدوداً من WIM/ESD دون استعادة Windows."
    ],
    "needs": [
      "A standalone supported WIM/ESD and an exact image index/path; begin with index1 if appropriate.",
      "WIM/ESD مستقلة مدعومة ورقم صورة ومسار دقيق؛ ابدأ بالصورة ١ إن كانت مناسبة."
    ],
    "steps": [
      [
        "List the selected image or folder to find the exact paths. An image index is a numbered edition inside the WIM.",
        "اعرض الصورة أو المجلد لمعرفة المسارات الدقيقة. رقم الصورة يعني إصداراً مرقماً داخل WIM."
      ],
      [
        "Use the single-file tool for one regular file; use WIM folder export for a subtree such as /Windows/Boot.",
        "استخدم أداة الملف الواحد لملف عادي واحد، وأداة تصدير المجلد لشجرة مثل /Windows/Boot."
      ],
      [
        "Choose a destination and export. Select a smaller subtree if the folder tool exceeds1000 regular files or its size limit.",
        "اختر الوجهة وصدّر. اختر مجلداً أصغر إذا تجاوزت أداة المجلد ١٠٠٠ ملف عادي أو حد الحجم."
      ]
    ],
    "result": [
      "Verified new data files, not Windows installation. Unsupported S entries and Windows metadata are not restored.",
      "ملفات بيانات جديدة متحقق منها وليست تثبيت Windows. لا تُستعاد عناصر S غير المدعومة أو بيانات Windows الوصفية."
    ],
    "guide": "folderConsole",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "wimTree": {
    "goal": [
      "Read one file or a bounded folder from a WIM/ESD without restoring Windows.",
      "اقرأ ملفاً واحداً أو مجلداً محدوداً من WIM/ESD دون استعادة Windows."
    ],
    "needs": [
      "A standalone supported WIM/ESD and an exact image index/path; begin with index1 if appropriate.",
      "WIM/ESD مستقلة مدعومة ورقم صورة ومسار دقيق؛ ابدأ بالصورة ١ إن كانت مناسبة."
    ],
    "steps": [
      [
        "List the selected image or folder to find the exact paths. An image index is a numbered edition inside the WIM.",
        "اعرض الصورة أو المجلد لمعرفة المسارات الدقيقة. رقم الصورة يعني إصداراً مرقماً داخل WIM."
      ],
      [
        "Use the single-file tool for one regular file; use WIM folder export for a subtree such as /Windows/Boot.",
        "استخدم أداة الملف الواحد لملف عادي واحد، وأداة تصدير المجلد لشجرة مثل /Windows/Boot."
      ],
      [
        "Choose a destination and export. Select a smaller subtree if the folder tool exceeds1000 regular files or its size limit.",
        "اختر الوجهة وصدّر. اختر مجلداً أصغر إذا تجاوزت أداة المجلد ١٠٠٠ ملف عادي أو حد الحجم."
      ]
    ],
    "result": [
      "Verified new data files, not Windows installation. Unsupported S entries and Windows metadata are not restored.",
      "ملفات بيانات جديدة متحقق منها وليست تثبيت Windows. لا تُستعاد عناصر S غير المدعومة أو بيانات Windows الوصفية."
    ],
    "guide": "folderConsole",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "cso": {
    "goal": [
      "Convert a supported image container into another file, without writing a USB.",
      "حوّل حاوية صورة مدعومة إلى ملف آخر دون الكتابة على USB."
    ],
    "needs": [
      "Your supported source image and destination space for the complete expanded result plus reserve.",
      "صورة مصدر مدعومة ومساحة وجهة تكفي الناتج الكامل بعد الفك مع احتياط."
    ],
    "steps": [
      [
        "Choose the matching tool: CSO/ZSO for supported ISO conversions, GZIP/XZ unpacking, supported DMG, or standalone CHDv5 HD/DVD.",
        "اختر الأداة المطابقة: CSO/ZSO لتحويل ISO المدعوم، أو فك GZIP/XZ ، أو DMG المدعوم، أو CHDv5 HD/DVD مستقل."
      ],
      [
        "Select the direction and expansion limit where offered. Read format limits before starting; not every variant is supported.",
        "اختر اتجاه التحويل وحد الفك إن توفر. اقرأ حدود الصيغة قبل البدء؛ لا تُدعم كل المتغيرات."
      ],
      [
        "Choose a destination, convert and wait for saved-output verification. Use Write an image separately if the output is suitable for a USB.",
        "اختر الوجهة وحوّل وانتظر التحقق من الناتج المحفوظ. استخدم كتابة صورة منفصلة إذا كان الناتج مناسباً لـ USB."
      ]
    ],
    "result": [
      "A new verified converted file. Conversion does not add boot support, decrypt media or prove the publisher’s identity.",
      "ملف جديد محوّل متحقق منه. لا يضيف التحويل دعماً للإقلاع ولا يفك التشفير ولا يثبت هوية الناشر."
    ],
    "guide": "containers",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "zso": {
    "goal": [
      "Convert a supported image container into another file, without writing a USB.",
      "حوّل حاوية صورة مدعومة إلى ملف آخر دون الكتابة على USB."
    ],
    "needs": [
      "Your supported source image and destination space for the complete expanded result plus reserve.",
      "صورة مصدر مدعومة ومساحة وجهة تكفي الناتج الكامل بعد الفك مع احتياط."
    ],
    "steps": [
      [
        "Choose the matching tool: CSO/ZSO for supported ISO conversions, GZIP/XZ unpacking, supported DMG, or standalone CHDv5 HD/DVD.",
        "اختر الأداة المطابقة: CSO/ZSO لتحويل ISO المدعوم، أو فك GZIP/XZ ، أو DMG المدعوم، أو CHDv5 HD/DVD مستقل."
      ],
      [
        "Select the direction and expansion limit where offered. Read format limits before starting; not every variant is supported.",
        "اختر اتجاه التحويل وحد الفك إن توفر. اقرأ حدود الصيغة قبل البدء؛ لا تُدعم كل المتغيرات."
      ],
      [
        "Choose a destination, convert and wait for saved-output verification. Use Write an image separately if the output is suitable for a USB.",
        "اختر الوجهة وحوّل وانتظر التحقق من الناتج المحفوظ. استخدم كتابة صورة منفصلة إذا كان الناتج مناسباً لـ USB."
      ]
    ],
    "result": [
      "A new verified converted file. Conversion does not add boot support, decrypt media or prove the publisher’s identity.",
      "ملف جديد محوّل متحقق منه. لا يضيف التحويل دعماً للإقلاع ولا يفك التشفير ولا يثبت هوية الناشر."
    ],
    "guide": "consoleGuide",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "compressedImage": {
    "goal": [
      "Convert a supported image container into another file, without writing a USB.",
      "حوّل حاوية صورة مدعومة إلى ملف آخر دون الكتابة على USB."
    ],
    "needs": [
      "Your supported source image and destination space for the complete expanded result plus reserve.",
      "صورة مصدر مدعومة ومساحة وجهة تكفي الناتج الكامل بعد الفك مع احتياط."
    ],
    "steps": [
      [
        "Choose the matching tool: CSO/ZSO for supported ISO conversions, GZIP/XZ unpacking, supported DMG, or standalone CHDv5 HD/DVD.",
        "اختر الأداة المطابقة: CSO/ZSO لتحويل ISO المدعوم، أو فك GZIP/XZ ، أو DMG المدعوم، أو CHDv5 HD/DVD مستقل."
      ],
      [
        "Select the direction and expansion limit where offered. Read format limits before starting; not every variant is supported.",
        "اختر اتجاه التحويل وحد الفك إن توفر. اقرأ حدود الصيغة قبل البدء؛ لا تُدعم كل المتغيرات."
      ],
      [
        "Choose a destination, convert and wait for saved-output verification. Use Write an image separately if the output is suitable for a USB.",
        "اختر الوجهة وحوّل وانتظر التحقق من الناتج المحفوظ. استخدم كتابة صورة منفصلة إذا كان الناتج مناسباً لـ USB."
      ]
    ],
    "result": [
      "A new verified converted file. Conversion does not add boot support, decrypt media or prove the publisher’s identity.",
      "ملف جديد محوّل متحقق منه. لا يضيف التحويل دعماً للإقلاع ولا يفك التشفير ولا يثبت هوية الناشر."
    ],
    "guide": "containers",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "dmg": {
    "goal": [
      "Convert a supported image container into another file, without writing a USB.",
      "حوّل حاوية صورة مدعومة إلى ملف آخر دون الكتابة على USB."
    ],
    "needs": [
      "Your supported source image and destination space for the complete expanded result plus reserve.",
      "صورة مصدر مدعومة ومساحة وجهة تكفي الناتج الكامل بعد الفك مع احتياط."
    ],
    "steps": [
      [
        "Choose the matching tool: CSO/ZSO for supported ISO conversions, GZIP/XZ unpacking, supported DMG, or standalone CHDv5 HD/DVD.",
        "اختر الأداة المطابقة: CSO/ZSO لتحويل ISO المدعوم، أو فك GZIP/XZ ، أو DMG المدعوم، أو CHDv5 HD/DVD مستقل."
      ],
      [
        "Select the direction and expansion limit where offered. Read format limits before starting; not every variant is supported.",
        "اختر اتجاه التحويل وحد الفك إن توفر. اقرأ حدود الصيغة قبل البدء؛ لا تُدعم كل المتغيرات."
      ],
      [
        "Choose a destination, convert and wait for saved-output verification. Use Write an image separately if the output is suitable for a USB.",
        "اختر الوجهة وحوّل وانتظر التحقق من الناتج المحفوظ. استخدم كتابة صورة منفصلة إذا كان الناتج مناسباً لـ USB."
      ]
    ],
    "result": [
      "A new verified converted file. Conversion does not add boot support, decrypt media or prove the publisher’s identity.",
      "ملف جديد محوّل متحقق منه. لا يضيف التحويل دعماً للإقلاع ولا يفك التشفير ولا يثبت هوية الناشر."
    ],
    "guide": "archiveImages",
    "hint": [
      "Only the supported single-segment XML UDIF raw/zero/ADC/zlib variants; a DMG is not automatically an ISO.",
      "فقط متغيرات XML UDIF أحادية الجزء المدعومة raw/zero/ADC/zlib ؛ DMG ليست ISO تلقائياً."
    ],
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "chd": {
    "goal": [
      "Convert a supported image container into another file, without writing a USB.",
      "حوّل حاوية صورة مدعومة إلى ملف آخر دون الكتابة على USB."
    ],
    "needs": [
      "Your supported source image and destination space for the complete expanded result plus reserve.",
      "صورة مصدر مدعومة ومساحة وجهة تكفي الناتج الكامل بعد الفك مع احتياط."
    ],
    "steps": [
      [
        "Choose the matching tool: CSO/ZSO for supported ISO conversions, GZIP/XZ unpacking, supported DMG, or standalone CHDv5 HD/DVD.",
        "اختر الأداة المطابقة: CSO/ZSO لتحويل ISO المدعوم، أو فك GZIP/XZ ، أو DMG المدعوم، أو CHDv5 HD/DVD مستقل."
      ],
      [
        "Select the direction and expansion limit where offered. Read format limits before starting; not every variant is supported.",
        "اختر اتجاه التحويل وحد الفك إن توفر. اقرأ حدود الصيغة قبل البدء؛ لا تُدعم كل المتغيرات."
      ],
      [
        "Choose a destination, convert and wait for saved-output verification. Use Write an image separately if the output is suitable for a USB.",
        "اختر الوجهة وحوّل وانتظر التحقق من الناتج المحفوظ. استخدم كتابة صورة منفصلة إذا كان الناتج مناسباً لـ USB."
      ]
    ],
    "result": [
      "A new verified converted file. Conversion does not add boot support, decrypt media or prove the publisher’s identity.",
      "ملف جديد محوّل متحقق منه. لا يضيف التحويل دعماً للإقلاع ولا يفك التشفير ولا يثبت هوية الناشر."
    ],
    "guide": "archiveImages",
    "hint": [
      "Standalone CHDv5 HD/DVD only; CD/mixed tracks, parents and compressed CHD creation are unsupported; stored raw-data creation is available.",
      "CHDv5 HD/DVD مستقل فقط؛ لا تُدعم مسارات CD المختلطة أو الصور التابعة أو إنشاء CHD المضغوط. يتوفر إنشاء حاوية بيانات خام دون ضغط."
    ],
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "consoleParts": {
    "goal": [
      "Split a large file into smaller pieces, or rebuild the matching supported set.",
      "قسّم ملفاً كبيراً إلى قطع أصغر أو أعد تجميع مجموعة مدعومة مطابقة."
    ],
    "needs": [
      "The original file for splitting; ALL matching parts and the required app manifest for rejoining.",
      "الملف الأصلي للتقسيم، أو كل الأجزاء المطابقة وملف التحقق المطلوب من التطبيق للدمج."
    ],
    "steps": [
      [
        "Choose the correct tool: generic split/join or PS3 raw ISO.#/.666NN parts. Their manifests are not interchangeable.",
        "اختر الأداة الصحيحة: التقسيم والدمج العام أو أجزاء PS3 الخام ISO.#/.666NN. ملفات التحقق الخاصة بها غير قابلة للتبادل."
      ],
      [
        "Choose part size and destination. Preserve the generated manifest beside all numbered parts.",
        "اختر حجم الجزء والوجهة، واحتفظ بملف التحقق الناتج مع كل الأجزاء المرقمة."
      ],
      [
        "To join, select the entire matching set and a destination that supports the full original size. Wait for hash verification.",
        "للدمج حدّد المجموعة المطابقة كاملة ووجهة تدعم حجم الملف الأصلي الكامل، وانتظر تحقق البصمات."
      ]
    ],
    "result": [
      "Smaller parts or a verified rejoined file. Splitting is not compression, game creation or a console playback guarantee.",
      "أجزاء أصغر أو ملف مدمج متحقق منه. التقسيم ليس ضغطاً أو إنشاء ألعاب أو ضمان تشغيل على منصة."
    ],
    "guide": "consoleGuide",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "split": {
    "goal": [
      "Split a large file into smaller pieces, or rebuild the matching supported set.",
      "قسّم ملفاً كبيراً إلى قطع أصغر أو أعد تجميع مجموعة مدعومة مطابقة."
    ],
    "needs": [
      "The original file for splitting; ALL matching parts and the required app manifest for rejoining.",
      "الملف الأصلي للتقسيم، أو كل الأجزاء المطابقة وملف التحقق المطلوب من التطبيق للدمج."
    ],
    "steps": [
      [
        "Choose the correct tool: generic split/join or PS3 raw ISO.#/.666NN parts. Their manifests are not interchangeable.",
        "اختر الأداة الصحيحة: التقسيم والدمج العام أو أجزاء PS3 الخام ISO.#/.666NN. ملفات التحقق الخاصة بها غير قابلة للتبادل."
      ],
      [
        "Choose part size and destination. Preserve the generated manifest beside all numbered parts.",
        "اختر حجم الجزء والوجهة، واحتفظ بملف التحقق الناتج مع كل الأجزاء المرقمة."
      ],
      [
        "To join, select the entire matching set and a destination that supports the full original size. Wait for hash verification.",
        "للدمج حدّد المجموعة المطابقة كاملة ووجهة تدعم حجم الملف الأصلي الكامل، وانتظر تحقق البصمات."
      ]
    ],
    "result": [
      "Smaller parts or a verified rejoined file. Splitting is not compression, game creation or a console playback guarantee.",
      "أجزاء أصغر أو ملف مدمج متحقق منه. التقسيم ليس ضغطاً أو إنشاء ألعاب أو ضمان تشغيل على منصة."
    ],
    "guide": "consoleGuide",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "join": {
    "goal": [
      "Split a large file into smaller pieces, or rebuild the matching supported set.",
      "قسّم ملفاً كبيراً إلى قطع أصغر أو أعد تجميع مجموعة مدعومة مطابقة."
    ],
    "needs": [
      "The original file for splitting; ALL matching parts and the required app manifest for rejoining.",
      "الملف الأصلي للتقسيم، أو كل الأجزاء المطابقة وملف التحقق المطلوب من التطبيق للدمج."
    ],
    "steps": [
      [
        "Choose the correct tool: generic split/join or PS3 raw ISO.#/.666NN parts. Their manifests are not interchangeable.",
        "اختر الأداة الصحيحة: التقسيم والدمج العام أو أجزاء PS3 الخام ISO.#/.666NN. ملفات التحقق الخاصة بها غير قابلة للتبادل."
      ],
      [
        "Choose part size and destination. Preserve the generated manifest beside all numbered parts.",
        "اختر حجم الجزء والوجهة، واحتفظ بملف التحقق الناتج مع كل الأجزاء المرقمة."
      ],
      [
        "To join, select the entire matching set and a destination that supports the full original size. Wait for hash verification.",
        "للدمج حدّد المجموعة المطابقة كاملة ووجهة تدعم حجم الملف الأصلي الكامل، وانتظر تحقق البصمات."
      ]
    ],
    "result": [
      "Smaller parts or a verified rejoined file. Splitting is not compression, game creation or a console playback guarantee.",
      "أجزاء أصغر أو ملف مدمج متحقق منه. التقسيم ليس ضغطاً أو إنشاء ألعاب أو ضمان تشغيل على منصة."
    ],
    "guide": "consoleGuide",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "createzip": {
    "goal": [
      "Pack selected files into one ZIP for easier storage or copying.",
      "اجمع ملفات محددة داخل ZIP واحد لتسهيل حفظها أو نسخها."
    ],
    "needs": [
      "Files you can open and a destination with room for the archive plus reserve.",
      "ملفات يمكنك فتحها ووجهة تتسع للأرشيف مع احتياط."
    ],
    "steps": [
      [
        "Choose the files you want to include, then choose a different output name/location.",
        "اختر الملفات التي تريد تضمينها ثم اختر اسماً وموقعاً مختلفين للناتج."
      ],
      [
        "Create the ZIP and wait for completion. Original input files stay where they were.",
        "أنشئ ZIP وانتظر الاكتمال. تبقى ملفات الإدخال الأصلية في مكانها."
      ],
      [
        "Inspect the ZIP afterward; already compressed images may not become much smaller.",
        "افحص ZIP بعد ذلك؛ قد لا تصغر الصور المضغوطة مسبقاً بشكل ملحوظ."
      ]
    ],
    "result": [
      "A new ZIP archive, not a bootable system image.",
      "أرشيف ZIP جديد وليس صورة نظام قابلة للإقلاع."
    ],
    "guide": "fileBasics",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "imageinfo": {
    "goal": [
      "Understand your file/device or choose a suitable tool before changing anything.",
      "افهم ملفك أو جهازك أو اختر أداة مناسبة قبل تغيير أي شيء."
    ],
    "needs": [
      "For image inspection choose a file; for device compatibility attach/select the USB; the chooser needs neither.",
      "لفحص الصورة اختر ملفاً، ولفحص توافق الجهاز صل USB وحددها؛ أداة الاختيار لا تحتاج أياً منهما."
    ],
    "steps": [
      [
        "Open the relevant report or describe your goal in Learn.",
        "افتح التقرير المناسب أو ابحث عن هدفك في تعلّم."
      ],
      [
        "Look for detected type, permissions and current limitations. A signature is a clue, not complete validation.",
        "ابحث عن النوع المكتشف والأذونات والحدود الحالية. التوقيع قرينة وليس تحققاً كاملاً."
      ],
      [
        "Follow the suggested guide or tool. Reading guidance never starts a disk operation.",
        "اتبع الدليل أو الأداة المقترحة. قراءة الإرشاد لا تبدأ عملية على القرص."
      ]
    ],
    "result": [
      "A report or explanation that helps you choose the next step.",
      "تقرير أو شرح يساعدك على اختيار الخطوة التالية."
    ],
    "guide": "fileBasics",
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "diagnostic": {
    "goal": [
      "Understand your file/device or choose a suitable tool before changing anything.",
      "افهم ملفك أو جهازك أو اختر أداة مناسبة قبل تغيير أي شيء."
    ],
    "needs": [
      "For image inspection choose a file; for device compatibility attach/select the USB; the chooser needs neither.",
      "لفحص الصورة اختر ملفاً، ولفحص توافق الجهاز صل USB وحددها؛ أداة الاختيار لا تحتاج أياً منهما."
    ],
    "steps": [
      [
        "Open the relevant report or describe your goal in Learn.",
        "افتح التقرير المناسب أو ابحث عن هدفك في تعلّم."
      ],
      [
        "Look for detected type, permissions and current limitations. A signature is a clue, not complete validation.",
        "ابحث عن النوع المكتشف والأذونات والحدود الحالية. التوقيع قرينة وليس تحققاً كاملاً."
      ],
      [
        "Follow the suggested guide or tool. Reading guidance never starts a disk operation.",
        "اتبع الدليل أو الأداة المقترحة. قراءة الإرشاد لا تبدأ عملية على القرص."
      ]
    ],
    "result": [
      "A report or explanation that helps you choose the next step.",
      "تقرير أو شرح يساعدك على اختيار الخطوة التالية."
    ],
    "guide": "fileBasics",
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "planner": {
    "goal": [
      "Understand your file/device or choose a suitable tool before changing anything.",
      "افهم ملفك أو جهازك أو اختر أداة مناسبة قبل تغيير أي شيء."
    ],
    "needs": [
      "For image inspection choose a file; for device compatibility attach/select the USB; the chooser needs neither.",
      "لفحص الصورة اختر ملفاً، ولفحص توافق الجهاز صل USB وحددها؛ أداة الاختيار لا تحتاج أياً منهما."
    ],
    "steps": [
      [
        "Open the relevant report or describe your goal in Learn.",
        "افتح التقرير المناسب أو ابحث عن هدفك في تعلّم."
      ],
      [
        "Look for detected type, permissions and current limitations. A signature is a clue, not complete validation.",
        "ابحث عن النوع المكتشف والأذونات والحدود الحالية. التوقيع قرينة وليس تحققاً كاملاً."
      ],
      [
        "Follow the suggested guide or tool. Reading guidance never starts a disk operation.",
        "اتبع الدليل أو الأداة المقترحة. قراءة الإرشاد لا تبدأ عملية على القرص."
      ]
    ],
    "result": [
      "A report or explanation that helps you choose the next step.",
      "تقرير أو شرح يساعدك على اختيار الخطوة التالية."
    ],
    "guide": "fileBasics",
    "impact": [
      "Read-only inspection/comparison; this action does not overwrite the drive.",
      "فحص أو مقارنة للقراءة فقط؛ لا يستبدل هذا الإجراء محتوى الوحدة."
    ]
  },
  "report": {
    "goal": [
      "Save a local record of the app’s recent tasks and technical results.",
      "احفظ سجلاً محلياً لمهام التطبيق الأخيرة ونتائجها التقنية."
    ],
    "needs": [
      "A destination folder. Review filenames/device details before sharing a report with anyone.",
      "مجلد وجهة. راجع أسماء الملفات وتفاصيل الجهاز قبل مشاركة التقرير مع أي شخص."
    ],
    "steps": [
      [
        "Choose the folder where the report should be saved.",
        "اختر المجلد الذي سيُحفظ فيه التقرير."
      ],
      [
        "Export and open the saved text to check the exact action, result and error code.",
        "صدّر وافتح النص المحفوظ لمراجعة الإجراء والنتيجة ورمز الخطأ بالضبط."
      ],
      [
        "When asking for help, include the app version, action and exact error rather than guessing its cause.",
        "عند طلب المساعدة أرفق إصدار التطبيق والإجراء والخطأ الدقيق بدلاً من تخمين السبب."
      ]
    ],
    "result": [
      "A local report in the selected language. Nothing is uploaded automatically.",
      "تقرير محلي باللغة المختارة. لا يُرفع شيء تلقائياً."
    ],
    "guide": "troubleshooting",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "convertTable": {
    "goal": [
      "Convert a supported basic MBR/GPT partition table while preserving data sectors.",
      "حوّل جدول أقسام MBR/GPT بسيطاً مدعوماً مع الحفاظ على قطاعات البيانات."
    ],
    "needs": [
      "A complete separate backup and internal space for the app’s table record.",
      "نسخة احتياطية كاملة منفصلة ومساحة داخلية لسجل جدول الأقسام."
    ],
    "steps": [
      [
        "Inspect the current layout first. This is not a resize, repair or arbitrary partition editor.",
        "افحص التخطيط الحالي أولاً. هذه ليست أداة تغيير حجم أو إصلاح أو محرر تقسيم عام."
      ],
      [
        "Choose the desired table and save/review the table record before confirming the write.",
        "اختر الجدول المطلوب واحفظ سجل الجدول وراجعه قبل تأكيد الكتابة."
      ],
      [
        "If the app refuses unsupported/overlapping/encrypted layouts, do not try to force conversion.",
        "إذا رفض التطبيق تخطيطاً غير مدعوم أو متداخلاً أو مشفراً فلا تحاول فرض التحويل."
      ]
    ],
    "result": [
      "A converted supported table or a refusal. The table record is not a full data backup and conversion may affect boot.",
      "جدول مدعوم محوّل أو رفض للعملية. سجل الجدول ليس نسخة كاملة للبيانات وقد يؤثر التحويل على الإقلاع."
    ],
    "guide": "benchBatch",
    "impact": [
      "Changes partition metadata and may affect boot. A table record is not a data backup.",
      "يغيّر جدول الأقسام وقد يؤثر على الإقلاع. سجل الجدول ليس نسخة للبيانات."
    ]
  },
  "persistence": {
    "goal": [
      "Create an empty Linux persistence data file for a compatible distribution.",
      "أنشئ ملف بيانات استمرار Linux فارغاً لتوزيعة متوافقة."
    ],
    "needs": [
      "Your distro’s required label/settings, a destination and the exact ISO path if using a Ventoy template.",
      "الاسم والإعدادات المطلوبة لتوزيعتك، ووجهة ومسار ISO الدقيق عند استخدام قالب Ventoy."
    ],
    "steps": [
      [
        "Check your distro/version documentation; persistence support differs between releases.",
        "راجع توثيق التوزيعة والإصدار؛ يختلف دعم الاستمرار بين الإصدارات."
      ],
      [
        "Choose the supported data size and label, and create the new file/template.",
        "اختر حجم البيانات والاسم المدعومين وأنشئ الملف والقالب الجديدين."
      ],
      [
        "Apply the distro’s matching setup instructions and test whether a harmless saved file survives reboot.",
        "اتبع إعدادات التوزيعة المطابقة واختبر هل يبقى ملف تجريبي بعد إعادة التشغيل."
      ]
    ],
    "result": [
      "An empty data filesystem/template, not an automatically integrated or downloaded Linux system.",
      "نظام ملفات بيانات فارغ أو قالب، وليس Linux منزلاً أو مدمجاً تلقائياً."
    ],
    "guide": "benchBatch",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  },
  "fileBenchmark": {
    "goal": [
      "Measure writing and reading through the Android folder you choose.",
      "قس كتابة وقراءة الملفات عبر مجلد أندرويد الذي تختاره."
    ],
    "needs": [
      "An authorized folder with space for the selected16/64/256 MiB test file plus reserve.",
      "مجلد مسموح ومساحة لملف الاختبار المختار 16/64/256 MiB مع احتياط."
    ],
    "steps": [
      [
        "Choose the actual location you want to measure; internal storage and USB are different destinations.",
        "اختر المكان الفعلي الذي تريد قياسه؛ التخزين الداخلي و USB وجهتان مختلفتان."
      ],
      [
        "Choose test size and start. The tool creates, reads back and removes only its own temporary file.",
        "اختر حجم الاختبار وابدأ. تنشئ الأداة ملفها المؤقت فقط وتعيد قراءته ثم تحذفه."
      ],
      [
        "Stop other transfers and compare results under similar conditions.",
        "أوقف النقل الآخر وقارن النتائج في ظروف متشابهة."
      ]
    ],
    "result": [
      "Folder write/read speeds and verification, not a full USB capacity or lifetime test.",
      "سرعات كتابة وقراءة المجلد والتحقق، وليست اختباراً لكامل سعة USB أو عمرها."
    ],
    "guide": "benchBatch",
    "impact": [
      "Writes and removes its own temporary test file in your selected folder.",
      "يكتب ملف الاختبار المؤقت الخاص به ويحذفه داخل المجلد المختار."
    ]
  },
  "filesystemCheck": {
    "goal": [
      "Check filesystem metadata first; consider limited repair only after a backup.",
      "افحص بيانات نظام الملفات الوصفية أولاً، وفكّر في الإصلاح المحدود بعد نسخة احتياطية فقط."
    ],
    "needs": [
      "The selected USB/partition and a backup before any repair attempt.",
      "USB والقسم المحددان ونسخة احتياطية قبل أي محاولة إصلاح."
    ],
    "steps": [
      [
        "Stop transfers and select the correct partition. Use0 only for a raw volume without a table.",
        "أوقف النقل واختر القسم الصحيح. استخدم ٠ فقط لوحدة خام دون جدول أقسام."
      ],
      [
        "Run the read-only check first and read its findings. Do not format simply because errors were reported.",
        "شغّل فحص القراءة فقط أولاً واقرأ النتائج. لا تهيئ لمجرد ظهور أخطاء."
      ],
      [
        "If you accept the backed-up risk, review and confirm limited repair; wait for the separate post-repair check.",
        "إذا قبلت المخاطرة بعد النسخ، راجع وأكّد الإصلاح المحدود وانتظر الفحص المنفصل بعده."
      ]
    ],
    "result": [
      "A metadata check/limited-repair result. NTFS repair is basic ntfsfix, not full Windows chkdsk or deleted-file recovery.",
      "نتيجة فحص أو إصلاح محدود للبيانات الوصفية. إصلاح NTFS هو ntfsfix أساسي وليس Windows chkdsk كاملاً أو استعادة للمحذوفات."
    ],
    "guide": "filesystemCare",
    "impact": [
      "Check only is read-only; confirmed repair can change filesystem metadata.",
      "الفحص وحده للقراءة فقط؛ قد يغيّر الإصلاح المؤكد بيانات نظام الملفات الوصفية."
    ]
  },
  "usbExtreme": {
    "goal": [
      "Prepare a new standard PS2 USBExtreme file set or inspect a catalog.",
      "جهّز مجموعة ملفات PS2 USBExtreme قياسية جديدة أو افحص فهرساً."
    ],
    "needs": [
      "Your legal supported PS2 ISO, an ASCII title and CD/DVD choice; for inspection select ul.cfg instead.",
      "PS2 ISO قانونية مدعومة خاصة بك وعنوان ASCII ونوع CD/DVD ؛ للفحص اختر ul.cfg بدلاً منها."
    ],
    "steps": [
      [
        "For creation choose your ISO, a short printable ASCII title and the correct media type.",
        "للإنشاء اختر ISO وعنوان ASCII قصيراً قابلاً للطباعة ونوع الوسيط الصحيح."
      ],
      [
        "Create the set in a new folder and wait for every part/catalog to be verified. Keep all ul.* parts with ul.cfg.",
        "أنشئ المجموعة في مجلد جديد وانتظر تحقق كل جزء والفهرس. احتفظ بكل أجزاء ul.* مع ul.cfg."
      ],
      [
        "Move the files to an EMPTY compatible OPL library root. Never replace another library’s ul.cfg.",
        "انقل الملفات إلى جذر مكتبة OPL فارغة متوافقة. لا تستبدل ul.cfg لمكتبة أخرى."
      ]
    ],
    "result": [
      "1 GiB parts plus a single-entry catalog, or a catalog listing. No library merge, OPL install, games or playback guarantee.",
      "أجزاء 1 GiB وفهرس بإدخال واحد أو قائمة فهرس. لا دمج مكتبات أو تثبيت OPL أو ألعاب أو ضمان تشغيل."
    ],
    "guide": "folderConsole",
    "impact": [
      "Reads the source and creates new files in the chosen destination. Check free space and the output location.",
      "يقرأ المصدر وينشئ ملفات جديدة في الوجهة المختارة. راجع المساحة الحرة ومكان الناتج."
    ]
  }
};

GUIDES.push({"id": "fileBasics", "icon": "book", "name": ["Files, images and booting: the difference", "الملفات والصور والإقلاع: ما الفرق؟"], "desc": ["Choose copy, extract, convert or write", "اختر النسخ أو الاستخراج أو التحويل أو الكتابة"], "time": ["5 min", "٥ دقائق"], "steps": [["Copy moves ordinary files into a folder. It does not install a bootloader; copying an ISO to a normal USB usually is not enough to boot.", "النسخ ينقل الملفات العادية إلى مجلد ولا يثبت برنامج إقلاع؛ نسخ ISO إلى USB عادية لا يكفي عادة للإقلاع."], ["An image represents disk or installer contents. A raw/hybrid image can use Write an image; a normal Windows ISO uses supported Windows UEFI preparation.", "تمثل الصورة محتوى قرص أو مثبت. تستخدم الصورة الخام أو الهجينة كتابة صورة؛ وتستخدم Windows ISO العادية تجهيز Windows UEFI المدعوم."], ["Extraction opens a container and saves its files. Conversion creates a different file format. Neither automatically writes a USB.", "الاستخراج يفتح الحاوية ويحفظ ملفاتها. التحويل ينشئ ملفاً بصيغة أخرى. لا يكتب أي منهما USB تلقائياً."], ["A matching hash proves equality with that reference. Verification of a write checks copying, not who published the file or whether the PC can boot.", "البصمة المطابقة تثبت المساواة بذلك المرجع. التحقق من الكتابة يفحص النسخ وليس هوية ناشر الملف أو إقلاع الحاسوب."]]},{"id": "fileLocations", "icon": "folder", "name": ["Choose source and destination safely", "اختر المصدر والوجهة بوضوح"], "desc": ["Phone, USB, folders and free space", "الهاتف وUSB والمجلدات والمساحة الحرة"], "time": ["5 min", "٥ دقائق"], "steps": [["Source means where data is read. Destination means where new data is saved or written. Read both names before starting.", "المصدر مكان قراءة البيانات والوجهة مكان حفظها أو كتابتها. اقرأ الاسمين قبل البدء."], ["The image picker selects one input file; the folder picker allows access to an output folder. USB permission grants direct device access and is separate.", "يختار منتقي الصور ملف إدخال واحداً ويسمح منتقي المجلدات بالوصول إلى مجلد ناتج. إذن USB يمنح الوصول المباشر للجهاز وهو منفصل."], ["Do not keep the only source/backup on the USB you will overwrite. Raw backups need room for the whole drive; unpacked images can be much larger than compressed input.", "لا تضع المصدر أو النسخة الوحيدة على USB التي ستستبدلها. تحتاج النسخة الخام مساحة لكامل الوحدة وقد تكون الصور المفكوكة أكبر بكثير من مدخلها المضغوط."], ["A cloud location in Android’s picker may transfer data using its provider. Choose local internal storage to avoid that provider traffic.", "قد ينقل الموقع السحابي في منتقي أندرويد بيانات عبر مزوده. اختر التخزين الداخلي المحلي لتجنب نقل المزود."], ["After a successful task open the reported new folder and check files before removing anything from the source.", "بعد نجاح المهمة افتح المجلد الجديد المبلغ عنه وافحص الملفات قبل حذف أي شيء من المصدر."]]},{"id": "troubleshooting", "icon": "info", "name": ["When a task fails: what to do next", "عند فشل المهمة: ما الخطوة التالية؟"], "desc": ["Find the stage; protect the original", "حدّد المرحلة واحمِ الأصل"], "time": ["6 min", "٦ دقائق"], "steps": [["Stop and read the action and exact error in Activity/View details. A failed task can have a different cause from the previous one.", "توقف واقرأ الإجراء والخطأ الدقيق في النشاط وعرض التفاصيل. قد يختلف سبب المهمة الفاشلة عن سابقتها."], ["No device: check OTG/data cable/power. Permission failure: reconnect and grant the correct prompt. Folder access and direct USB access are separate.", "لا يوجد جهاز: افحص OTG وكابل البيانات والطاقة. فشل الإذن: أعد التوصيل وامنح الإذن الصحيح. وصول المجلد وUSB المباشر منفصلان."], ["Source/format error: confirm the file type and supported variant. Space error: choose a destination with enough expanded capacity. Do not reformat just to retry.", "خطأ مصدر أو صيغة: تأكد من نوع الملف والمتغير المدعوم. خطأ مساحة: اختر وجهة تكفي الناتج بعد الفك. لا تهيئ لمجرد إعادة المحاولة."], ["After interrupted USB writing/format/repair, previous writes cannot be undone. Treat that destination as incomplete, keep the original source/backup, and inspect before choosing a fresh operation.", "بعد انقطاع كتابة USB أو التهيئة أو الإصلاح لا يمكن التراجع عن الكتابات السابقة. اعتبر الوجهة غير مكتملة واحتفظ بالمصدر أو النسخة وافحص قبل اختيار عملية جديدة."], ["For help, export a local report and share only the details you intend: app version, action, drive model and exact error. No report uploads automatically.", "للمساعدة صدّر تقريراً محلياً وشارك التفاصيل المقصودة فقط: إصدار التطبيق والإجراء وطراز الوحدة والخطأ الدقيق. لا يُرفع التقرير تلقائياً."]]});
const GUIDE_NOTES={
  "downloads": {
    "needs": [
      "A browser and space for the publisher’s image; check its download size first.",
      "متصفح ومساحة لصورة الناشر؛ راجع حجم التنزيل أولاً."
    ],
    "example": [
      "Download your chosen Linux ISO yourself, compare the official SHA-256, then return to Write an image.",
      "نزّل Linux ISO المختارة بنفسك وقارن SHA-256 الرسمية ثم عد إلى كتابة صورة."
    ],
    "done": [
      "You have the intended image and a matching trusted checksum, not just a similarly named file.",
      "لديك الصورة المقصودة وبصمة موثوقة مطابقة وليس مجرد ملف باسم مشابه."
    ],
    "tools": [
      "hash",
      "write",
      "windows"
    ]
  },
  "start": {
    "needs": [
      "An OTG-capable Android device, data-capable adapter/cable and a USB drive.",
      "جهاز أندرويد يدعم OTG ومحول أو كابل بيانات ووحدة USB."
    ],
    "example": [
      "First connect a drive you recognize and run Drive information before choosing any writing tool.",
      "صل أولاً وحدة تعرفها وشغّل معلومات الوحدة قبل اختيار أي أداة كتابة."
    ],
    "done": [
      "You can identify the correct drive and understand USB permission versus folder permission.",
      "يمكنك تمييز الوحدة الصحيحة وفهم الفرق بين إذن USB وإذن المجلد."
    ],
    "tools": [
      "info",
      "diagnostic"
    ]
  },
  "choose": {
    "needs": [
      "A separate backup and the requirements of the TV, computer or console that will read the USB.",
      "نسخة احتياطية منفصلة ومتطلبات التلفاز أو الحاسوب أو المنصة التي ستقرأ USB."
    ],
    "example": [
      "For a single6 GiB video, FAT32 is unsuitable. Consider exFAT only if the receiving device supports it.",
      "لملف فيديو واحد حجمه6 GiB لا يناسب FAT32؛ فكّر في exFAT إذا دعمه الجهاز المستقبل."
    ],
    "done": [
      "You know which filesystem the target device supports and accept that formatting erases existing data.",
      "تعرف نظام الملفات الذي يدعمه الجهاز الهدف وتدرك أن التهيئة تمحو البيانات الحالية."
    ],
    "tools": [
      "format"
    ]
  },
  "flash": {
    "needs": [
      "Your own supported hybrid ISO/raw IMG and a backed-up USB large enough for it.",
      "ISO هجينة أوIMG خام مدعومة خاصة بك وUSB منسوخة احتياطياً تتسع لها."
    ],
    "example": [
      "A2 GiB raw image can be written to a larger drive; verification compares the image area, not unused extra capacity.",
      "يمكن كتابة IMG بحجم2 GiB على وحدة أكبر؛ يقارن التحقق منطقة الصورة وليس السعة الزائدة غير المستخدمة."
    ],
    "done": [
      "Writing and comparison both complete; actual boot is checked separately on the computer.",
      "تكتمل الكتابة والمقارنة معاً؛ يُختبر الإقلاع الفعلي منفصلاً على الحاسوب."
    ],
    "tools": [
      "write",
      "verify",
      "hash"
    ]
  },
  "multi": {
    "needs": [
      "An existing supported Ventoy installation and free space for your own ISO files.",
      "تثبيت Ventoy مدعوم موجود ومساحة لملفات ISO الخاصة بك."
    ],
    "example": [
      "Copy Linux.iso and Rescue.iso to the large data partition; do not copy them to the small EFI partition.",
      "انسخ Linux.iso وRescue.iso إلى قسم البيانات الكبير وليس قسم EFI الصغير."
    ],
    "done": [
      "The files are in the data partition; copying alone did not install or update Ventoy.",
      "الملفات في قسم البيانات؛ النسخ وحده لم يثبت Ventoy أو يحدثه."
    ],
    "tools": [
      "ventoy",
      "ventoyInstall"
    ]
  },
  "save": {
    "needs": [
      "A different destination with space for the full drive, even unused sectors.",
      "وجهة أخرى تتسع للوحدة كاملة بما فيها القطاعات غير المستخدمة."
    ],
    "example": [
      "A32 GiB USB with only1 GiB of files still needs about32 GiB plus reserve for a raw backup.",
      "USB سعتها32 GiB وبها ملفات1 GiB فقط ما زالت تحتاج نحو32 GiB مع احتياط للنسخة الخام."
    ],
    "done": [
      "The backup finished readback verification, and you know restoring it erases the destination.",
      "انتهت مقارنة النسخة بالقراءة اللاحقة وتعرف أن استعادتها تمحو الوجهة."
    ],
    "tools": [
      "backup",
      "clone",
      "write",
      "files"
    ]
  },
  "safe": {
    "needs": [
      "A clear source/destination plan and a copy of everything you cannot replace.",
      "خطة واضحة للمصدر والوجهة ونسخة من كل ما لا يمكنك تعويضه."
    ],
    "example": [
      "Inspect first, back up second, and only then confirm the drive-erasing action on the intended USB.",
      "افحص أولاً وانسخ احتياطياً ثانياً ثم أكّد إجراء محو الوحدة على USB المقصودة فقط."
    ],
    "done": [
      "You can say which action reads, creates files, changes metadata or erases a drive.",
      "يمكنك تحديد هل الإجراء يقرأ أو ينشئ ملفات أو يغيّر بيانات وصفية أو يمحو وحدة."
    ],
    "tools": [
      "info",
      "backup",
      "verifyZero"
    ]
  },
  "trouble": {
    "needs": [
      "The exact action and error code; do not assume another app caused USB access to fail.",
      "الإجراء ورمز الخطأ بالضبط؛ لا تفترض أن تطبيقاً آخر سبب فشل الوصول."
    ],
    "example": [
      "If a drive is absent, check OTG/cable/power and permission before trying a format.",
      "إذا غابت الوحدة افحص OTG والكابل والطاقة والإذن قبل تجربة التهيئة."
    ],
    "done": [
      "You have narrowed the failure to detection, permission, source file, destination space or operation.",
      "حددت مرحلة الفشل: اكتشاف الجهاز أو الإذن أو ملف المصدر أو مساحة الوجهة أو العملية."
    ],
    "tools": [
      "diagnostic",
      "info",
      "report"
    ]
  },
  "words": {
    "needs": [
      "No USB is needed to read this glossary.",
      "لا تحتاج USB لقراءة هذا المسرد."
    ],
    "example": [
      "An ISO is a file containing an image; a filesystem organizes files; a partition is an area inside a disk.",
      "ISO ملف يحتوي صورة؛ نظام الملفات ينظم الملفات؛ القسم مساحة داخل القرص."
    ],
    "done": [
      "You can distinguish a file, an image, a filesystem and a whole drive before choosing a tool.",
      "يمكنك تمييز الملف والصورة ونظام الملفات والوحدة كاملة قبل اختيار أداة."
    ],
    "tools": [
      "planner",
      "imageinfo"
    ]
  },
  "advanced": {
    "needs": [
      "A stable connection. Capacity writing tests require a disposable or fully backed-up USB.",
      "اتصال مستقر. تتطلب اختبارات كتابة السعة USB تجريبية أو منسوخة بالكامل."
    ],
    "example": [
      "Use a read surface scan for read failures. Use Full capacity test only if erasing the drive is acceptable.",
      "استخدم فحص السطح لفشل القراءة واختبار السعة الكامل فقط إذا كان محو الوحدة مقبولاً."
    ],
    "done": [
      "You understand that a short speed benchmark is not a capacity or lifetime guarantee.",
      "تعرف أن قياس السرعة القصير ليس ضماناً للسعة أو العمر."
    ],
    "tools": [
      "scan",
      "benchmark",
      "capacity"
    ]
  },
  "workshop": {
    "needs": [
      "Your target device’s requirements and a clear distinction between disk and file operations.",
      "متطلبات الجهاز الهدف وتمييز واضح بين عمليات القرص وعمليات الملفات."
    ],
    "example": [
      "To hold a large file, choose a compatible filesystem; to keep FAT32, split it with the matching tool instead.",
      "لحفظ ملف كبير اختر نظام ملفات متوافقاً؛ وللاحتفاظ بـFAT32 قسّم الملف بالأداة المطابقة."
    ],
    "done": [
      "You chose an implemented tool whose input, output and limits match your goal.",
      "اخترت أداة منفذة يتوافق مدخلها وناتجها وحدودها مع هدفك."
    ],
    "tools": [
      "planner",
      "format",
      "split"
    ]
  },
  "windowsGuide": {
    "needs": [
      "Your own supported Windows ISO and an empty compatible FAT32 destination for file preparation.",
      "Windows ISO مدعومة خاصة بك ووجهة FAT32 فارغة متوافقة لتجهيز الملفات."
    ],
    "example": [
      "Use Windows UEFI preparation for a normal Windows ISO; raw writing is not a general Windows ISO installer.",
      "استخدم تجهيز Windows UEFI لصورة Windows عادية؛ الكتابة الخام ليست مثبتاً عاماً لصور Windows."
    ],
    "done": [
      "Prepared files are verified, then tested in the PC’s UEFI boot menu; BIOS/ToGo are separate unfinished engines.",
      "تُفحص الملفات المجهزة ثم تُختبر في قائمة إقلاع UEFI للحاسوب؛ BIOS وToGo محركات منفصلة غير مكتملة."
    ],
    "tools": [
      "windows",
      "wimSplit"
    ]
  },
  "ventoyGuide": {
    "needs": [
      "Your official imported package and a backup; initial installation erases the USB.",
      "الحزمة الرسمية التي تستوردها ونسخة احتياطية؛ التثبيت الأولي يمحو USB."
    ],
    "example": [
      "Import the package, review the initial MBR/GPT install, reconnect, then add your own ISOs.",
      "استورد الحزمة وراجع تثبيت MBR/GPT الأولي وأعد التوصيل ثم أضف ISO الخاصة بك."
    ],
    "done": [
      "Boot data passes readback; you still test the target PC and preserve an external backup.",
      "تجتاز بيانات الإقلاع القراءة اللاحقة؛ ما زال يلزم اختبار الحاسوب والاحتفاظ بنسخة خارجية."
    ],
    "tools": [
      "ventoyInstall",
      "ventoyGpt",
      "ventoy"
    ]
  },
  "imagesGuide": {
    "needs": [
      "A supported file and destination space for its extracted contents.",
      "ملف مدعوم ومساحة وجهة لمحتوياته المستخرجة."
    ],
    "example": [
      "Open an ISO to extract a document or install.wim. Extraction copies files; it does not write boot sectors.",
      "افتح ISO لاستخراج مستند أو install.wim. الاستخراج ينسخ الملفات ولا يكتب قطاعات الإقلاع."
    ],
    "done": [
      "You know whether you wanted archive extraction, image conversion or a raw USB write.",
      "تعرف هل أردت استخراج أرشيف أو تحويل صورة أو كتابة USB خاماً."
    ],
    "tools": [
      "optical",
      "tar",
      "imageinfo"
    ]
  },
  "benchBatch": {
    "needs": [
      "Choose the exact operation first: file benchmark, table conversion or persistence creation have different requirements.",
      "اختر الإجراء الدقيق أولاً؛ قياس الملفات وتحويل الجدول وإنشاء الاستمرار لها متطلبات مختلفة."
    ],
    "example": [
      "A table record protects table metadata only; use a full IMG backup when you need a copy of all drive bytes.",
      "سجل الجدول يحفظ بيانات الجدول فقط؛ استخدم نسخة IMG كاملة إذا أردت كل بايتات الوحدة."
    ],
    "done": [
      "You recognize the output and remaining limits of your selected operation.",
      "تميّز الناتج والحدود المتبقية للعملية المختارة."
    ],
    "tools": [
      "fileBenchmark",
      "convertTable",
      "persistence"
    ]
  },
  "containers": {
    "needs": [
      "Supported source format, chosen conversion direction and room for the expanded image.",
      "صيغة مصدر مدعومة واتجاه التحويل المختار ومساحة للصورة بعد فكها."
    ],
    "example": [
      "A700 MiB compressed image may expand to4 GiB or more. The expansion limit caps output, not the download size.",
      "قد تتمدد صورة مضغوطة700 MiB إلى4 GiB أو أكثر. حد الفك يقيد الناتج وليس حجم التنزيل."
    ],
    "done": [
      "The new file passes output verification; conversion alone does not make it bootable.",
      "يجتاز الملف الجديد تحقق الناتج؛ التحويل وحده لا يجعله قابلاً للإقلاع."
    ],
    "tools": [
      "cso",
      "compressedImage",
      "imageinfo"
    ]
  },
  "gptFat": {
    "needs": [
      "For Ventoy use a supported USB/package/backup; for FAT browsing use a saved FAT16/32 image.",
      "لـVentoy استخدم USB وحزمة ونسخة احتياطية مدعومة؛ ولتصفح FAT استخدم صورة FAT16/32 محفوظة."
    ],
    "example": [
      "Choose partition1 for a typical partitioned image; use0 only for a raw volume without a table.",
      "اختر القسم١ لصورة مقسمة معتادة واستخدم٠ فقط لوحدة خام دون جدول."
    ],
    "done": [
      "You used the correct source and mode; browsing an image did not format a physical USB.",
      "استخدمت المصدر والوضع الصحيحين؛ تصفح الصورة لم يهيئ USB فعلية."
    ],
    "tools": [
      "ventoyGpt",
      "fatImage"
    ]
  },
  "consoleGuide": {
    "needs": [
      "Your own legal supported image/file and all required parts/manifests for rejoining.",
      "صورة أو ملف قانوني مدعوم خاص بك وكل الأجزاء وملفات التحقق المطلوبة للدمج."
    ],
    "example": [
      "Keep Game.iso.0, Game.iso.1 and the app’s matching manifest together; unrelated parts cannot be joined safely.",
      "احتفظ بـGame.iso.0 وGame.iso.1 وملف تحقق التطبيق المطابق معاً؛ لا تُدمج الأجزاء غير المرتبطة بأمان."
    ],
    "done": [
      "Parts/conversion pass hash checks. Actual loader/console compatibility still needs testing.",
      "تجتاز الأجزاء أو الصورة المحولة فحوص البصمات. يبقى اختبار توافق المشغل والمنصة ضرورياً."
    ],
    "tools": [
      "zso",
      "consoleParts"
    ]
  },
  "filesystemCare": {
    "needs": [
      "The right USB/partition and a separate backup before repair.",
      "USB والقسم الصحيحان ونسخة منفصلة قبل الإصلاح."
    ],
    "example": [
      "Run Check only first. A check result reporting issues is not permission to erase or force repair.",
      "شغّل الفحص فقط أولاً. ظهور مشكلات لا يعني ضرورة المحو أو فرض الإصلاح."
    ],
    "done": [
      "Read-only check findings are understood; any attempted repair is followed by a separate check.",
      "فهمت نتائج فحص القراءة فقط؛ ويلي أي إصلاح فحص منفصل."
    ],
    "tools": [
      "filesystemCheck",
      "backup"
    ]
  },
  "archiveImages": {
    "needs": [
      "A supported source and enough destination space; direct NTFS/ext USB exports require internal storage.",
      "مصدر مدعوم ومساحة وجهة كافية؛ يتطلب تصدير USB NTFS/ext المباشر التخزين الداخلي."
    ],
    "example": [
      "If Android Files cannot mount NTFS, try Browse NTFS/ext USB files to list and export supported files read-only.",
      "إذا لم يركّب تطبيق الملفات NTFS فجرّب تصفح ملفات USB NTFS/ext لعرض الملفات المدعومة وتصديرها للقراءة فقط."
    ],
    "done": [
      "The selected source stays unchanged and supported exported files pass verification; unsupported variants may be refused.",
      "يبقى المصدر المختار دون تغيير وتجتاز الملفات المدعومة التحقق؛ قد تُرفض المتغيرات غير المدعومة."
    ],
    "tools": [
      "archives",
      "dmg",
      "chd",
      "fsUsb",
      "fsImage"
    ]
  },
  "folderConsole": {
    "needs": [
      "Choose WIM export or PS2 USBExtreme; they use different input files and do not install an OS/loader.",
      "اختر تصدير WIM أو PS2 USBExtreme؛ مدخلاتهما مختلفة ولا تثبتان نظاماً أو مشغلاً."
    ],
    "example": [
      "Export /Windows/Boot from a WIM, or create a new PS2 set from your ISO. Never replace an existing library ul.cfg.",
      "صدّر /Windows/Boot من WIM أو أنشئ مجموعة PS2 جديدة من ISO الخاصة بك. لا تستبدل ul.cfg لمكتبة موجودة."
    ],
    "done": [
      "Your new folder has verified data files; PC/console installation/playback is a separate task.",
      "يحتوي مجلدك الجديد ملفات بيانات متحققاً منها؛ تثبيت أو تشغيل الحاسوب والمنصة مهمة منفصلة."
    ],
    "tools": [
      "wimTree",
      "usbExtreme"
    ]
  },
  "fileBasics": {
    "needs": [
      "No drive is needed for this explanation.",
      "لا تحتاج وحدة لهذا الشرح."
    ],
    "example": [
      "To copy photos use Android Files; to write a supported boot image use Write an image.",
      "لنسخ الصور العادية استخدم تطبيق ملفات أندرويد ولكتابة صورة إقلاع مدعومة استخدم كتابة صورة."
    ],
    "done": [
      "You chose the operation by its result, not only the file extension.",
      "اخترت العملية حسب نتيجتها وليس امتداد الملف فقط."
    ],
    "tools": [
      "files",
      "imageinfo",
      "write",
      "windows"
    ]
  },
  "fileLocations": {
    "needs": [
      "Know the source, destination and available space before opening a writing tool.",
      "اعرف المصدر والوجهة والمساحة المتاحة قبل فتح أداة كتابة."
    ],
    "example": [
      "Read a USB into an IMG on the phone, not onto the same USB you are copying.",
      "اقرأ USB إلى IMG على الهاتف وليس إلى USB نفسها التي تنسخها."
    ],
    "done": [
      "Source/destination names and free-space requirements are clear.",
      "أسماء المصدر والوجهة ومتطلبات المساحة واضحة."
    ],
    "tools": [
      "files",
      "backup",
      "fsUsb"
    ]
  },
  "troubleshooting": {
    "needs": [
      "The original source/backup and the exact reported action/error.",
      "المصدر أو النسخة الأصلية والإجراء والخطأ المبلغ عنهما بدقة."
    ],
    "example": [
      "A failed normal interface claim does not prove another app is open; inspect the error instead of closing unrelated apps blindly.",
      "فشل حجز الواجهة العادية لا يثبت أن تطبيقاً آخر مفتوح؛ افحص الخطأ بدلاً من إغلاق تطبيقات غير مرتبطة عشوائياً."
    ],
    "done": [
      "You can report the failure stage and choose a matching next step without unnecessary erasure.",
      "يمكنك تحديد مرحلة الفشل واختيار خطوة مناسبة دون محو غير ضروري."
    ],
    "tools": [
      "diagnostic",
      "report",
      "info"
    ]
  }
};
Object.keys(GUIDE_NOTES).forEach(function(id){var g=GUIDES.find(function(x){return x.id===id;});if(g)g.tools=GUIDE_NOTES[id].tools;});
LEARN_GOALS.push({"id": "ntfsFiles", "name": ["NTFS formatted successfully. Why does Android Files not open it?", "نجحت تهيئة NTFS؛ لماذا لا يفتحه تطبيق ملفات أندرويد؟"], "answer": ["Formatting creates a filesystem; it does not add an Android kernel driver. Use the app’s read-only NTFS/ext USB browser to list/export supported files to internal storage, or use a computer that supports the filesystem. Ordinary Android folder browsing depends on that device’s mounting support.", "تنشئ التهيئة نظام ملفات ولا تضيف برنامج تشغيل لنواة أندرويد. استخدم متصفح USB NTFS/ext للقراءة فقط لعرض الملفات المدعومة وتصديرها داخلياً أو استخدم حاسوباً يدعم النظام. يعتمد تصفح مجلدات أندرويد العادي على دعم الجهاز للتركيب."], "tools": ["fsUsb", "format"], "guide": "choose"});
LEARN_GOALS.push({"id": "backupRoom", "name": ["Why is my backup larger than the files on the USB?", "لماذا نسختي الاحتياطية أكبر من ملفات USB؟"], "answer": ["A raw IMG backup includes the whole drive, its layout and unused sectors. For a32 GiB USB allow about32 GiB plus reserve. If you need only photos/documents, copy those files with Android Files instead.", "تشمل نسخة IMG الخام الوحدة كاملة وتخطيطها والقطاعات غير المستخدمة. لوحدة32 GiB اترك نحو32 GiB مع احتياط. إذا أردت الصور والمستندات فقط فانسخ تلك الملفات بتطبيق ملفات أندرويد."], "tools": ["backup", "files"], "guide": "save"});
LEARN_GOALS.push({"id": "copyIso", "name": ["Is copying an ISO the same as making a bootable USB?", "هل نسخ ISO مثل إنشاء USB قابلة للإقلاع؟"], "answer": ["No. Copying saves a file; raw writing replaces disk contents. A supported hybrid Linux ISO/raw IMG uses Write an image. A normal Windows ISO uses supported Windows UEFI preparation. Copy ISO files directly only onto an already installed compatible Ventoy drive.", "لا. النسخ يحفظ ملفاً والكتابة الخام تستبدل محتوى القرص. تستخدم Linux ISO الهجينة أوIMG الخام المدعومة كتابة صورة. تستخدم Windows ISO العادية تجهيز Windows UEFI المدعوم. انسخ ISO مباشرة فقط إلى وحدة Ventoy متوافقة مثبتة مسبقاً."], "tools": ["write", "windows", "ventoy"], "guide": "fileBasics"});
LEARN_GOALS.push({"id": "largeFile", "name": ["Why does a large file fail on FAT32 despite free space?", "لماذا يفشل ملف كبير على FAT32 رغم المساحة الحرة؟"], "answer": ["FAT32 limits each file to4 GiB minus1 byte (4,294,967,295 bytes), regardless of free space. A decimal4 GB file is smaller than this limit, so check the exact size and error. Back up before changing filesystem, or use the matching split tool and preserve its manifest. Rejoining needs a destination that holds the full file.", "يحد FAT32 كل ملف إلى4 GiB ناقص بايت (4,294,967,295 بايت) بصرف النظر عن المساحة الحرة. ملف4 GB بالنظام العشري أصغر من هذا الحد؛ لذا راجع الحجم الدقيق والخطأ. انسخ احتياطياً قبل تغيير النظام أو استخدم أداة التقسيم المطابقة واحفظ ملف التحقق. يحتاج الدمج وجهة تتسع للملف الكامل."], "tools": ["format", "split", "consoleParts"], "guide": "choose"});
LEARN_GOALS.push({"id": "expandedRoom", "name": ["Why does a small archive need so much free space?", "لماذا يحتاج أرشيف صغير مساحة كبيرة؟"], "answer": ["Compressed size is not expanded size. A small download may contain gigabytes of data. Choose an expansion limit and a local destination with enough free space plus reserve. The app may refuse a file before creating output when its size/codec is unsupported.", "حجم الضغط ليس حجم الفك. قد يحتوي تنزيل صغير بيانات بعدة جيجابايت. اختر حد فك ووجهة محلية بها مساحة كافية مع احتياط. قد يرفض التطبيق ملفاً قبل إنشاء الناتج إذا لم يدعم حجمه أو ترميزه."], "tools": ["archives", "compressedImage"], "guide": "containers"});
LEARN_GOALS.push({"id": "twoPermissions", "name": ["Why does USB permission not give folder access?", "لماذا لا يمنح إذن USB الوصول للمجلدات؟"], "answer": ["Direct USB permission allows device commands. Android’s folder picker grants access to a specific folder through its storage provider. Grant the permission required by the selected tool; one does not replace the other.", "يسمح إذن USB المباشر بأوامر الجهاز. يمنح منتقي مجلدات أندرويد وصولاً إلى مجلد محدد عبر مزود التخزين. امنح الإذن المطلوب للأداة المختارة؛ لا يستبدل أحدهما الآخر."], "tools": ["files", "info"], "guide": "fileLocations"});
LEARN_GOALS.push({"id": "phoneComputer", "name": ["Can this turn my Android phone into a Windows or Linux PC?", "هل يحوّل هذا هاتفي الأندرويد إلى حاسوب Windows أوLinux؟"], "answer": ["These tools prepare files/drives for compatible computers; they do not replace Android or run the downloaded operating system on your phone. You supply an image, prepare the USB using a supported method, then boot the computer from it.", "تجهز هذه الأدوات ملفات ووحدات لحواسيب متوافقة؛ لا تستبدل أندرويد ولا تشغّل النظام المنزّل على هاتفك. تختار الصورة وتجهز USB بطريقة مدعومة ثم تُقلع الحاسوب منها."], "tools": ["write", "windows"], "guide": "fileBasics"});
LEARN_GOALS.push({"id": "interrupted", "name": ["I unplugged the USB during a task. What now?", "نزعت USB أثناء المهمة؛ ماذا أفعل؟"], "answer": ["Treat a destination being written/formatted/repaired as incomplete. Previous writes cannot be undone. Keep the original source/backup, read Activity details, reconnect and inspect. A read-only task can usually be started again after resolving access; do not blindly retry destructive tasks.", "اعتبر الوجهة التي كانت تُكتب أو تُهيأ أو تُصلح غير مكتملة. لا يمكن التراجع عن الكتابات السابقة. احتفظ بالمصدر أو النسخة واقرأ تفاصيل النشاط وأعد التوصيل وافحص. يمكن عادة بدء مهمة قراءة من جديد بعد حل الوصول؛ لا تكرر المهام المدمرة عشوائياً."], "tools": ["info", "report"], "guide": "troubleshooting"});
LEARN_GOALS.push({"id": "checkedNotBoot", "name": ["Verification passed. Why might the computer still not boot?", "نجح التحقق؛ لماذا قد لا يُقلع الحاسوب؟"], "answer": ["Readback proves copied bytes match the selected input, not that the input is suitable for that computer. Check the publisher’s checksum, image type, CPU architecture and the PC’s boot method/settings. Use the supported preparation tool for that image.", "تثبت القراءة اللاحقة مطابقة البايتات للمدخل المختار ولا تثبت ملاءمته للحاسوب. راجع بصمة الناشر ونوع الصورة ومعمارية المعالج وطريقة وإعدادات الإقلاع. استخدم أداة التجهيز المدعومة لتلك الصورة."], "tools": ["hash", "imageinfo", "write", "windows"], "guide": "flash"});
LEARN_GOALS.push({"id": "offlineUse", "name": ["Will using the tools consume my internet allowance?", "هل يستهلك استخدام الأدوات باقة الإنترنت؟"], "answer": ["The app has no internet permission and processes files locally. Opening official links uses your browser, and choosing a cloud folder may use its provider’s data connection. Image downloads are your separate choice. Choose local files/folders for offline work.", "لا يملك التطبيق إذن الإنترنت ويعالج الملفات محلياً. تستخدم الروابط الرسمية متصفحك وقد يستخدم المجلد السحابي اتصال مزوده. تنزيل الصور اختيار منفصل منك. اختر ملفات ومجلدات محلية للعمل دون إنترنت."], "tools": ["files"], "guide": "fileLocations"});
LEARN_GOALS.push({"id": "ps2Catalog", "name": ["Where do I put USBExtreme parts and ul.cfg?", "أين أضع أجزاء USBExtreme وul.cfg؟"], "answer": ["Keep all generated ul.* parts with the generated ul.cfg. For a new set, move those files to the root of an EMPTY compatible OPL USB library. Do not replace an existing library’s ul.cfg: this tool does not merge catalogs or install a loader. Test on your prepared console.", "احتفظ بكل أجزاء ul.* مع ul.cfg الناتج. للمجموعة الجديدة انقل تلك الملفات إلى جذر مكتبة USB فارغة متوافقة مع OPL. لا تستبدل ul.cfg لمكتبة موجودة؛ الأداة لا تدمج الفهارس ولا تثبت مشغلاً. اختبر على منصتك المجهزة."], "tools": ["usbExtreme"], "guide": "folderConsole"});
LEARN_GOALS.push({"id": "language", "name": ["How is the first language chosen, and can I change it?", "كيف تُختار اللغة أول مرة وهل يمكنني تغييرها؟"], "answer": ["On a fresh first launch an Arabic device starts in Arabic; other device languages start in English. Use the language button at the top any time. Your saved choice wins on later launches, even if the device language changes.", "في أول تشغيل جديد يبدأ الجهاز ذو اللغة العربية بالعربية وتبدأ لغات الأجهزة الأخرى بالإنجليزية. استخدم زر اللغة بالأعلى في أي وقت. يتقدم اختيارك المحفوظ في التشغيل اللاحق حتى لو تغيرت لغة الجهاز."], "tools": [], "guide": "start"});

TOOLS.push({id:'readMap',icon:'shield',color:'blue',group:'health',name:['Read-error map & resume','خريطة أخطاء القراءة والاستكمال'],desc:['Read-only ranges • saved checkpoints','نطاقات للقراءة فقط • نقاط استكمال محفوظة'],body:['Scan supported 512-byte-sector USB mass storage up to2 TiB without writing drive data. Progress is saved in a small private checksummed checkpoint every64 MiB and on cancellation/read failure. Resume validates geometry plus sampled content/vendor/product/available serial identity; verify the physical drive yourself because samples are not proof of uniqueness. A failed request (up to1 MiB) is recorded as a range and the scan stops without automatic retries. Reconnect and review Resume to continue AFTER that range. Up to4096 failure ranges are tracked; the report shows the first100 and total count. A new scan replaces the one saved map. This is not per-sector defect proof, repair, hardware remapping, fake-capacity verification or defrag. Identity samples must be readable.','افحص تخزين USB المدعوم بقطاع512بايت حتى2 TiB دون كتابة بيانات الوحدة. يُحفظ التقدم في نقطة استكمال صغيرة خاصة ببصمة تحقق كل64 MiB وعند الإلغاء أو فشل القراءة. يفحص الاستكمال الهندسة وعينات المحتوى ومعرّف الشركة والمنتج والرقم التسلسلي المتاح؛ تأكد من الوحدة الفعلية لأن العينات ليست إثباتاً للهوية الفريدة. يُسجل طلب القراءة الفاشل (حتى1 MiB) كنطاق ويتوقف الفحص دون إعادة محاولات تلقائية. أعد التوصيل وراجع الاستكمال للمتابعة بعد ذلك النطاق. يُتتبع حتى4096نطاقاً فاشلاً ويعرض التقرير أول١٠٠ والعدد الكلي. يستبدل الفحص الجديد الخريطة الوحيدة المحفوظة. ليس إثباتاً لتلف كل قطاع أو إصلاحاً أو إعادة تعيين عتادي أو تحققاً للسعة المزيفة أو إلغاء تجزئة. يجب أن تكون عينات الهوية قابلة للقراءة.']});
TOOL_HELP.readMap={goal:['Keep a resumable map of drive ranges that could not be read.','احتفظ بخريطة قابلة للاستكمال لنطاقات الوحدة التي تعذرت قراءتها.'],needs:['A stable selected USB and permission. Back up a failing drive before repeated testing.','USB محددة باتصال مستقر وإذن. انسخ الوحدة المتعطلة قبل الاختبارات المتكررة.'],impact:['Reads USB only; saves a small private progress file. New scan replaces the previous saved map.','يقرأ USB فقط ويحفظ ملف تقدم صغيراً خاصاً. يستبدل الفحص الجديد الخريطة السابقة.'],steps:[['Choose New scan for a fresh map, or Resume only for the same unchanged physical drive.','اختر فحصاً جديداً لخريطة جديدة أو الاستكمال للوحدة الفعلية نفسها دون تغيير.'],['After a failed read request the scan pauses. Reconnect the USB and review Resume to continue beyond the recorded range.','يتوقف الفحص عند طلب قراءة فاشل. أعد توصيل USB وراجع الاستكمال للمتابعة بعد النطاق المسجل.'],['Use Show saved map to inspect progress and the first100 error ranges without starting a scan.','استخدم عرض الخريطة المحفوظة لفحص التقدم وأول١٠٠ نطاق خطأ دون بدء فحص.']],result:['A complete/partial map with successful block count and failed-request ranges. Nothing is repaired or remapped.','خريطة كاملة أو جزئية بعدد القطاعات المقروءة ونطاقات الطلبات الفاشلة. لا يُصلح شيء أو تُعاد تعيين قطاعات.'],guide:'readMapGuide'};
GUIDES.push({id:'readMapGuide',icon:'shield',name:['Map read errors and resume safely','ارسم أخطاء القراءة واستكمل بوضوح'],desc:['Saved progress and failed ranges, without repair','تقدم محفوظ ونطاقات فاشلة دون إصلاح'],time:['5 min','٥ دقائق'],tools:['readMap','backup','scan'],steps:[['Start with a separate backup if data matters. Repeated testing is not recovery and may stress an already failing drive.','ابدأ بنسخة منفصلة إذا كانت البيانات مهمة. الاختبار المتكرر ليس استعادة وقد يجهد وحدة متعطلة.'],['Choose the USB, open Read-error map, select New scan and review the exact target. No drive data is written.','اختر USB وافتح خريطة أخطاء القراءة واختر فحصاً جديداً وراجع الهدف الدقيق. لا تُكتب بيانات على الوحدة.'],['A failed request records a range up to1 MiB and pauses. It does not prove every sector inside the range is damaged.','يسجل الطلب الفاشل نطاقاً حتى1 MiB ويتوقف. لا يثبت أن كل قطاع داخله تالف.'],['Reconnect, choose the same drive and Resume to continue after that saved range. Do not format or swap drives between sessions. Identity mismatch refuses the resume.','أعد التوصيل واختر الوحدة نفسها واستكمل بعد النطاق المحفوظ. لا تهيئ أو تبدل الوحدة بين الجلسات. يُرفض الاستكمال إذا اختلفت الهوية.'],['Cancel saves progress. After an unexpected process stop, up to64 MiB since the last checkpoint may be read again. Show saved map works without scanning; the first100 ranges are displayed.','يحفظ الإلغاء التقدم. بعد توقف العملية فجأة قد تُعاد قراءة حتى64 MiB منذ آخر حفظ. يعرض زر الخريطة المحفوظة التقدم دون فحص وأول١٠٠ نطاق.']]});
GUIDE_NOTES.readMapGuide={needs:['The selected USB, a backup and a clear distinction between a failed read request and a proven physical defect.','USB المحددة ونسخة احتياطية وتمييز بين طلب قراءة فاشل وعيب عتادي مثبت.'],example:['If a1 MiB read fails, reconnect and Resume after reviewing that recorded request. The app does not silently retry a broken connection.','إذا فشلت قراءة1 MiB فأعد التوصيل واستكمل بعد مراجعة الطلب المسجل. لا يعيد التطبيق محاولة اتصال معطوب بصمت.'],done:['You know where the scan stopped and what the saved map does—and does not—prove.','تعرف أين توقف الفحص وما الذي تثبته الخريطة المحفوظة وحدودها.'],tools:['readMap','backup','scan']};
LEARN_GOALS.push({id:'readMapMeaning',name:['Does a read-error map repair bad sectors?','هل تصلح خريطة أخطاء القراءة القطاعات التالفة؟'],answer:['No. It records failed read requests and saves progress for a later reviewed resume. A failed request can reflect a cable, power, transport or media problem; it is not proof that every sector in that range is physically bad. The app does not remap hardware blocks or defragment files.','لا. تسجل طلبات القراءة الفاشلة وتحفظ التقدم لاستكمال تراجعه لاحقاً. قد يعكس الفشل مشكلة كابل أو طاقة أو نقل أو وسيط؛ ليس إثباتاً لتلف كل قطاع في النطاق. لا يعيد التطبيق تعيين قطاعات العتاد أو يلغي تجزئة الملفات.'],tools:['readMap','backup'],guide:'readMapGuide'});
const TOOL_SECTIONS=[
{id:'connect',name:['Connect & inspect','الاتصال والفحص'],desc:['Identify the drive and choose the right workflow.','عرّف الوحدة واختر المسار المناسب.'],tools:['info','diagnostic','hex','planner']},
{id:'boot',name:['Bootable & image writing','الإقلاع وكتابة الصور'],desc:['Images, Windows UEFI and supported Ventoy workflows.','الصور وWindows UEFI ومسارات Ventoy المدعومة.'],tools:['write','verify','windows','ventoyInstall','ventoyGpt','ventoyUpdate','ventoy','wimSplit','persistence']},
{id:'disk',name:['Disk management','إدارة القرص'],desc:['Formatting, backup, filesystem care and direct USB browsing.','التهيئة والنسخ والعناية بالنظام وتصفح USB مباشرة.'],tools:['format','backup','clone','filesystemCheck','fsUsb','partitionMeta','convertTable','wipe','verifyZero']},
{id:'files',name:['Files & archives','الملفات والأرشيفات'],desc:['Folders, checksums, splitting and supported archives.','المجلدات والبصمات والتقسيم والأرشيفات المدعومة.'],tools:['files','hash','checksums','split','join','createzip','zip','extract','tar','archives']},
{id:'images',name:['Image readers & converters','قارئات الصور ومحولات الصيغ'],desc:['Read saved image files or create verified converted output.','اقرأ صوراً محفوظة أو أنشئ ناتج تحويل متحققاً منه.'],tools:['imageinfo','optical','fatImage','fsImage','wimExplore','wimTree','cso','compressedImage','dmg','chd']},
{id:'console',name:['Gaming tools','أدوات الألعاب'],desc:['User-supplied console images; no games or loaders included.','صور منصات يختارها المستخدم؛ لا ألعاب أو مشغلات مضمّنة.'],tools:['zso','consoleParts','usbExtreme']},
{id:'health',name:['Diagnostics & speed','التشخيص والسرعة'],desc:['Read-only checks, resumable maps and separately reviewed capacity writing.','فحوص قراءة وخرائط استكمال وكتابة سعة بمراجعة منفصلة.'],tools:['benchmark','randomread','scan','readMap','fileBenchmark','capacity']},
{id:'reports',name:['Reports','التقارير'],desc:['Save local results and diagnostic details.','احفظ النتائج وتفاصيل التشخيص محلياً.'],tools:['report']}
];
TOOL_SECTIONS.sort(function(a,b){return ['boot','disk','console','files','images','health','connect','reports'].indexOf(a.id)-['boot','disk','console','files','images','health','connect','reports'].indexOf(b.id);});
TOOL_SECTIONS.forEach(function(section){section.tools.forEach(function(id){var tool=TOOLS.find(function(x){return x.id===id;});if(tool)tool.group=section.id;});});

GUIDES.push({id:'analyzerGuide',icon:'info',name:['Understand a full USB report','فهم تقرير USB الشامل'],desc:['Capacity, partitions, free space and warnings','السعة والأقسام والمساحة الحرة والتحذيرات'],time:['5 min','٥ دقائق'],tools:['info','report','readMap'],steps:[
['Connect your USB with a data/OTG adapter, select its name, grant USB permission, then open Full USB analyzer. Finish file transfers and safely eject Android’s mounted volume before confirming direct access. Leave the cable connected.','صل USB بمحول بيانات أو OTG واختر اسمها وامنح إذن USB ثم افتح محلل USB الشامل. أنه نقل الملفات وأخرج الوحدة المركبة في أندرويد بأمان قبل تأكيد الوصول المباشر. أبق الكابل متصلاً.'],
['GPT and MBR describe how the drive is divided. FAT32, exFAT, NTFS and ext describe how a volume stores files. A raw volume can have a filesystem without a partition table.','تصف GPT وMBR تقسيم الوحدة. تصف FAT32 وexFAT وNTFS وext طريقة حفظ الملفات داخل القسم. قد تحتوي الوحدة الخام نظام ملفات دون جدول أقسام.'],
['Read each partition separately. Whole-drive capacity includes partitions and gaps. Space outside listed volumes is not free space for saving files inside a volume. GB and GiB use different units, so their numbers differ.','اقرأ كل قسم منفرداً. تشمل سعة الوحدة الأقسام والفجوات. المساحة خارج الوحدات المدرجة ليست مساحة لحفظ الملفات داخل قسم. تستخدم GB وGiB وحدات مختلفة ولذلك تختلف أرقامهما.'],
['Free space from FAT/exFAT/NTFS allocation data is a live snapshot. ext counters can be stale; reserved blocks may reduce usable space. Unknown or incomplete means unavailable, not empty or zero. The analyzer stops after 64 MiB of reads.','المساحة الحرة من تخصيص FAT/exFAT/NTFS لقطة حية. قد تكون عدادات ext قديمة وتقلل الكتل المحجوزة المساحة المتاحة. مجهول أو غير مكتمل يعني غير متاح وليس فارغاً أو صفراً. يتوقف المحلل عند 64 MiB من القراءات.'],
['Warnings about CRC, overlapping partitions or unreadable metadata need attention. Back up important files before repair. Analysis does not certify hardware health, genuine capacity or booting. Use a read scan or error map separately for readable-sector testing.','تحتاج تحذيرات CRC أو تداخل الأقسام أو تعذر قراءة البيانات الوصفية إلى انتباه. انسخ الملفات المهمة قبل الإصلاح. لا يعتمد التحليل سلامة العتاد أو السعة الحقيقية أو الإقلاع. استخدم فحص القراءة أو خريطة الأخطاء منفرداً لاختبار قراءة القطاعات.'],
['Open Activity to view the result. Export a report to a local folder if you want a copy. It can contain USB serials, names and partition IDs; share only what you intend. Switching app language changes report labels. Nothing uploads automatically.','افتح النشاط لعرض النتيجة. صدر تقريراً إلى مجلد محلي للاحتفاظ بنسخة. قد يشمل أرقام USB وأسماءها ومعرفات الأقسام؛ شارك فقط ما تقصده. يغير تبديل لغة التطبيق عناوين التقرير. لا يرفع شيء تلقائياً.']
]});
GUIDE_NOTES.analyzerGuide={needs:['A recognized USB and permission; no image file or output folder is needed to analyze.','USB معروفة وإذن الوصول؛ لا يحتاج التحليل ملف صورة أو مجلد ناتج.'],example:['A 64 GB drive may show about 59.6 GiB. Its partition free space is a separate number.','قد تظهر وحدة 64 GB بنحو 59.6 GiB. المساحة الحرة داخل قسمها رقم منفصل.'],done:['Review each volume, the space source and every warning before choosing another action.','راجع كل وحدة ومصدر المساحة وكل تحذير قبل اختيار إجراء آخر.']};
GUIDE_NOTES.analyzerGuide.tools=['info','report','readMap'];
TOOL_HELP.info.guide='analyzerGuide';

TOOLS.push({id:'partitionMeta',icon:'layers',color:'amber',group:'disk',name:['Partition names & active flag','أسماء الأقسام وراية النشاط'],desc:['GPT name / MBR active • writes metadata','اسم GPT / نشاط MBR • كتابة بيانات وصفية'],body:['Rename one GPT partition or mark one MBR primary partition active. Supports only matching standard 128-entry GPT copies or a basic MBR without extended/dynamic/protective entries, with 512-byte sectors. Preserves partition boundaries and filesystem data; it does not rename filesystem labels, resize, create/delete, mount or install boot code. GPT names allow 1–35 BMP characters (English/Arabic, no controls/emoji). GPT commits backup first then primary with CRCs and readback verification. Back up first: interruption can leave inconsistent metadata and names/active flags can affect boot paths.','غيّر اسم قسم GPT أو علّم قسم MBR أولياً واحداً كنشط. يدعم نسختي GPT قياسيتين متطابقتين من 128 خانة أو MBR بسيطاً دون إدخالات ممتدة أو ديناميكية أو واقية وقطاع 512 بايت. يحفظ حدود الأقسام وبيانات نظام الملفات؛ لا يغير اسم نظام الملفات ولا الحجم ولا ينشئ أو يحذف أو يركب أو يثبت كود إقلاع. يسمح اسم GPT من 1 إلى35 رمز BMP بالعربية أو الإنجليزية دون تحكم أو رموز تعبيرية. يكتب GPT الاحتياطية ثم الأساسية مع CRC والتحقق بإعادة القراءة. انسخ احتياطياً أولاً؛ قد يترك الانقطاع بيانات غير متسقة وقد تؤثر الأسماء ورايات النشاط في الإقلاع.']});
TOOL_HELP.partitionMeta={goal:['Edit a supported GPT partition name or MBR active flag while preserving volume boundaries.','عدّل اسم قسم GPT أو راية نشاط MBR مدعومة مع حفظ حدود الوحدة.'],needs:['A backed-up USB, USB permission and its exact partition number from Full USB analyzer.','USB منسوخة احتياطياً وإذن USB ورقم القسم الدقيق من محلل USB الشامل.'],steps:[['Run Full USB analyzer and note the table type, partition number and warnings.','شغل محلل USB الشامل وسجل نوع الجدول ورقم القسم والتحذيرات.'],['Enter the partition number, then choose GPT name or MBR active flag. Review the named physical USB before Confirm.','أدخل رقم القسم ثم اختر اسم GPT أو راية نشاط MBR. راجع USB الفعلية المسماة قبل التأكيد.'],['Keep the cable connected until verified. Analyze again; check any boot paths that depend on the old name or active flag.','أبق الكابل متصلاً حتى التحقق. حلل مجدداً وافحص مسارات الإقلاع التي تعتمد على الاسم القديم أو راية النشاط.']],result:['A verified metadata edit; no partition was resized or filesystem renamed. Boot compatibility still needs its own test.','تعديل بيانات وصفية متحقق منه دون تغيير حجم قسم أو اسم نظام الملفات. يحتاج توافق الإقلاع اختباراً منفصلاً.'],impact:['Writes partition metadata. Back up first; wrong targets or interruption can prevent mounting/boot.','يكتب بيانات الأقسام الوصفية. انسخ احتياطياً؛ قد يمنع الهدف الخاطئ أو الانقطاع التركيب أو الإقلاع.'],guide:'partitionGuide'};
GUIDES.push({id:'partitionGuide',icon:'book',name:['Partition names, active flags & ADC images','أسماء الأقسام ورايات النشاط وصور ADC'],desc:['Two newly available workshop features','ميزتان جديدتان متاحتان في الورشة'],time:['6 min','٦ دقائق'],tools:['info','partitionMeta','dmg','backup'],steps:[['A GPT partition name is a table label, different from a filesystem label. Renaming it does not move files, but a boot setup using PARTLABEL may need updating.','اسم قسم GPT اسم في الجدول يختلف عن اسم نظام الملفات. لا ينقل تغييره الملفات لكن قد يحتاج إعداد الإقلاع الذي يستخدم PARTLABEL إلى تحديث.'],['The MBR active flag selects a primary partition for some existing BIOS boot code. It does not install boot code or make an ordinary USB a Windows installer. Choose only a boot partition you understand.','تختار راية نشاط MBR قسماً أولياً لبعض كود إقلاع BIOS الموجود. لا تثبت كود إقلاع ولا تجعل USB عادية مثبت Windows. اختر فقط قسم إقلاع تعرفه.'],['Back up, analyze the drive, choose the exact partition, review the warning and Confirm. Use stable power and leave the cable connected. GPT is written backup first then primary, and every changed region is read back.','انسخ احتياطياً وحلل الوحدة واختر القسم الدقيق وراجع التحذير ثم أكد. استخدم طاقة مستقرة وأبق الكابل متصلاً. تكتب GPT الاحتياطية ثم الأساسية وتقرأ كل منطقة تغيرت للتحقق.'],['If a metadata edit is interrupted, stop. Do not retry blindly or format to fix it. Keep the report and backup; inspect the GPT copies or use a trusted computer recovery tool.','إذا انقطع تعديل البيانات الوصفية فتوقف. لا تعد المحاولة عشوائياً ولا تهيئ لإصلاحه. احفظ التقرير والنسخة وافحص نسختي GPT أو استخدم أداة استعادة موثوقة على حاسوب.'],['For an unencrypted XML UDIF DMG using ADC, select your file in DMG → raw image and a local destination with enough expanded space. The app now decodes ADC as well as raw/zero/ADC/zlib, checks supplied block-table CRCs and verifies saved output.','لملف XML UDIF DMG غير مشفر يستخدم ADC اختره في تحويل DMG إلى صورة خام وحدد وجهة محلية تكفي الحجم بعد الفك. يفك التطبيق الآن ADC إضافة إلى الخام والأصفار وzlib ويفحص CRC لخريطة الكتل عند توفره ويتحقق من الناتج المحفوظ.'],['Conversion preserves image bytes; it does not convert HFS/APFS to another filesystem or guarantee boot. BZIP2/LZFSE/encrypted/segmented variants remain unsupported; no Apple system is downloaded.','يحفظ التحويل بايتات الصورة ولا يحول HFS/APFS إلى نظام آخر ولا يضمن الإقلاع. لا تدعم BZIP2 أو LZFSE أو الصور المشفرة أو المجزأة؛ لا ينزل أي نظام Apple.']]});
GUIDE_NOTES.partitionGuide={needs:['A full backup for metadata edits; your own supported DMG and enough output space for conversion.','نسخة كاملة لتعديل البيانات الوصفية؛ وملف DMG مدعوم توفره ومساحة ناتج كافية للتحويل.'],example:['Rename GPT partition 1 to DATA only when no boot script depends on its old name.','غيّر اسم قسم GPT رقم1 إلى DATA فقط عندما لا يعتمد برنامج إقلاع على اسمه القديم.'],done:['Reanalyze metadata after an edit; check the verified image.img after conversion. No automatic boot claim.','أعد تحليل البيانات الوصفية بعد تعديلها وافحص image.img المتحقق منه بعد التحويل. لا يوجد ضمان إقلاع تلقائي.'],tools:['info','partitionMeta','dmg','backup']};

// Keep partition editing and image conversion in their own learning routes.
var partitionLesson=GUIDES.find(function(g){return g.id==='partitionGuide';});
partitionLesson.name=['Edit partition names and active flags','تعديل أسماء الأقسام ورايات النشاط'];
partitionLesson.desc=['Metadata safety, backup and boot limits','سلامة البيانات الوصفية والنسخ وحدود الإقلاع'];
partitionLesson.tools=['info','partitionMeta','backup'];
var adcLessons=partitionLesson.steps.splice(4);
var imageLesson=GUIDES.find(function(g){return g.id==='archiveImages';});
if(imageLesson)adcLessons.forEach(function(step){imageLesson.steps.push(step);});
GUIDE_NOTES.partitionGuide.needs=['A fully backed-up USB and its exact table type and partition number from the analyzer.','USB منسوخة احتياطياً بالكامل ونوع جدولها ورقم قسمها الدقيق من المحلل.'];
GUIDE_NOTES.partitionGuide.done=['Run the analyzer again and check any boot paths that depend on partition names or active flags.','شغّل المحلل مجدداً وافحص مسارات الإقلاع التي تعتمد على أسماء الأقسام أو رايات النشاط.'];
GUIDE_NOTES.partitionGuide.tools=['info','partitionMeta','backup'];

var ntfsCare=GUIDES.find(function(g){return g.id==='filesystemCare';});
if(ntfsCare)ntfsCare.steps.push(['NTFS formatting uses the bundled userspace formatter through direct USB access; Android does not need to mount NTFS. Quick format creates metadata, not a full-drive zero-fill. A percentage may be unavailable: watch the current stage, elapsed time and completed USB read/write counters. Small clusters and slow devices can take longer. If stopping, use Cancel and wait for the operation to end before unplugging, retrying or installing an update. Writes already made cannot be undone.','تستخدم تهيئة NTFS المحرك المرفق عبر وصول USB المباشر؛ لا يحتاج أندرويد إلى تركيب NTFS. تنشئ التهيئة السريعة البيانات الوصفية ولا تملأ الوحدة كاملة بالأصفار. قد لا تتوفر نسبة؛ راقب المرحلة والوقت وعدادات قراءة وكتابة USB المكتملة. قد تطول المدة مع العناقيد الصغيرة أو الوحدات البطيئة. للإيقاف استخدم إلغاء وانتظر انتهاء العملية قبل الفصل أو الإعادة أو تثبيت تحديث. لا يمكن التراجع عن الكتابات السابقة.']);

TOOL_HELP.write.hint=['Custom persistence: choose the Ventoy workflow to create a separate empty ext4 data file (256–16384 MiB) and JSON template. Copy the data to the Ventoy data root and merge the template into /ventoy/ventoy.json. This does not append a partition to a directly written ISO. Check the exact distro/version label and boot support.','الاستمرار المخصص: اختر مسار Ventoy لإنشاء ملف بيانات ext4 فارغ منفصل (256–16384 MiB) وقالب JSON. انسخ البيانات إلى جذر بيانات Ventoy وادمج القالب في /ventoy/ventoy.json. لا يضيف هذا قسماً إلى ISO مكتوبة مباشرة. راجع الاسم وتوافق الإقلاع للتوزيعة والإصدار الدقيقين.'];
TOOL_HELP.persistence.hint=TOOL_HELP.write.hint;
TOOLS.find(function(x){return x.id==='persistence';}).body[0]+=' Custom size: any whole MiB from256 to16384. Use a destination with large-file support for4GiB or more. Linux labels accept1–16 ASCII letters, digits, underscores or hyphens; custom labels must match distro documentation.';
TOOLS.find(function(x){return x.id==='persistence';}).body[1]+=' الحجم المخصص: عدد صحيح من256 إلى16384 MiB. استخدم وجهة تدعم الملفات الكبيرة لحجم4GiB أو أكثر. يقبل الاسم1–16 حرفاً لاتينياً أو رقماً أو شرطة سفلية أو شرطة؛ يجب أن يطابق الاسم المخصص وثائق التوزيعة.';

const DIRECT_PERSISTENCE_HELP=['Direct ISO persistence writes and verifies your supplied Linux ISO, creates a new ext4 partition after all original image extents and then commits its MBR entry with physical readback. Choose a whole size256–16384MiB and the exact distro-required label. The persistence label adds / union in persistence.conf for Debian/Kali-style live images. Requires512-byte sectors, supported non-GPT MBR ISO9660 hybrid image, boot code/active entry, a free primary entry and enough space. GPT/protective/extended/dynamic/unknown layouts are rejected before ISO writes. It does not patch bootloaders: choose the Live USB Persistence menu or the distro-required persistent/persistence kernel option when starting the PC. Existing boot/EFI entries are preserved. Cancellation, power loss or errors can leave incomplete media; no automatic rollback. The new MBR entry deliberately differs from the original ISO, so a later full ISO-byte comparison reports a mismatch. No system download or universal boot guarantee.','يكتب الاستمرار المباشر صورة Linux ISO التي توفرها ويتحقق منها، ثم ينشئ قسم ext4 بعد جميع نطاقات الصورة ويعتمد خانته في MBR مع التحقق بإعادة القراءة. اختر حجماً صحيحاً256–16384MiB والاسم المطلوب للتوزيعة. يضيف الاسم persistence ملف persistence.conf وفيه / union لصور Debian/Kali الحية. يحتاج قطاع512 بايت وISO9660 هجينة بجدول MBR مدعوم دون GPT وكود إقلاع وخانة نشطة وخانة أولية فارغة ومساحة كافية. تُرفض GPT والجداول الواقية والممتدة والديناميكية والمجهولة قبل كتابة ISO. لا يعدل برنامج الإقلاع: اختر Live USB Persistence أو خيار النواة persistent أو persistence المطلوب للتوزيعة عند تشغيل الحاسوب. يحافظ على إدخالات الإقلاع وEFI الموجودة. قد يترك الإلغاء أو انقطاع الطاقة أو الخطأ وسيطاً غير مكتمل؛ لا يوجد تراجع تلقائي. تختلف خانة MBR الجديدة عمداً عن ISO الأصلية ولذلك تبلغ المقارنة الحرفية اللاحقة عن اختلاف. لا ينزل نظاماً ولا يضمن كل أنواع الإقلاع.'];
TOOL_HELP.write.hint=DIRECT_PERSISTENCE_HELP;
TOOL_HELP.write.guide='directPersistenceGuide';
GUIDES.push({id:'directPersistenceGuide',icon:'burn',name:['Write a Linux ISO with persistence','كتابة Linux ISO مع الاستمرار'],desc:['Custom storage for changes across live boots','مساحة مخصصة لحفظ تغييرات النظام الحي'],time:['5 min','٥ دقائق'],tools:['write','info','backup'],steps:[['Back up the USB. Download your own Linux live ISO from its official source onto the phone/tablet, not this USB. Check the distro’s persistence documentation.','انسخ USB احتياطياً ونزل Linux ISO حية من مصدرها الرسمي على الهاتف أو اللوحي وليس USB نفسها. راجع وثائق الاستمرار للتوزيعة.'],['Open ISO writing and choose Write Linux ISO + custom persistence. Select the ISO and target USB. Enter256–16384MiB and the distro-required label. Debian/Kali-style live images normally use persistence with / union.','افتح كتابة ISO واختر كتابة Linux ISO مع استمرار مخصص. اختر ISO وUSB الهدف وأدخل256–16384MiB والاسم المطلوب للتوزيعة. تستخدم صور Debian/Kali الحية عادة الاسم persistence وإعداد / union.'],['Review the named drive and Confirm. The layout and native tools are checked before writing. Unsupported GPT/protective/extended/unknown layouts, no free primary entry or insufficient space are refused. Ordinary raw writing is separate.','راجع اسم الوحدة وأكد. يُفحص التخطيط والأدوات قبل الكتابة. تُرفض GPT والجداول الواقية والممتدة والمجهولة أو غياب خانة أولية فارغة أو نقص المساحة. كتابة الصورة الخام مسار منفصل.'],['Keep power and USB connected until ISO verification, ext4 checks and partition commit finish. If interrupted, keep View details and stop; a partially written drive may not boot.','أبق الطاقة وUSB متصلتين حتى اكتمال التحقق من ISO وفحص ext4 واعتماد القسم. عند الانقطاع احتفظ بالتفاصيل وتوقف؛ قد لا تقلع الوحدة المكتوبة جزئياً.'],['Boot the computer and choose Live USB Persistence, or the kernel option required by that exact distro/version. Create a harmless file, reboot using persistence and check it remains. The added MBR entry intentionally differs from the source ISO; byte-for-byte ISO verification reports that change.','أقلع الحاسوب واختر Live USB Persistence أو خيار النواة المطلوب للتوزيعة والإصدار الدقيقين. أنشئ ملفاً بسيطاً وأعد الإقلاع مع الاستمرار وتحقق من بقائه. تختلف خانة MBR المضافة عمداً عن ISO المصدر وتبلغ المقارنة الحرفية عن ذلك.']]});
GUIDE_NOTES.directPersistenceGuide={needs:['Your supported MBR hybrid Linux live ISO, a fully backed-up USB with spare space, stable power and the distro’s instructions.','Linux ISO حية هجينة بجدول MBR مدعوم وUSB منسوخة احتياطياً بمساحة إضافية وطاقة مستقرة وتعليمات التوزيعة.'],example:['For a compatible Debian/Kali-style live ISO, choose768MiB and label persistence; use the persistence boot menu on the PC. GPT layouts are refused.','لصورة Debian/Kali حية متوافقة اختر768MiB والاسم persistence واستخدم قائمة إقلاع الاستمرار على الحاسوب. تُرفض GPT.'],done:['ISO and ext4 checks complete, then a manual PC reboot confirms the saved file survives. The app’s check does not prove boot compatibility.','تكتمل فحوص ISO وext4 ثم تؤكد إعادة إقلاع الحاسوب بقاء الملف. لا يثبت فحص التطبيق توافق الإقلاع.'],tools:['write','info','backup']};

// Stored CHDv5 creation is a separate, uncompressed file-only workflow.
{const x=TOOLS.find(x=>x.id==='chd');x.name=['CHD images','صور CHD'];x.desc=['Decode or create stored CHDv5 data','فك CHDv5 أو إنشاؤه دون ضغط'];x.body[0]+=' Stored creation accepts raw 512/2048-byte-aligned files up to 16 GiB, writes an uncompressed CHDv5 container and verifies its decoded SHA-256. Output is larger; no CD/mixed-track metadata or parent images are created.';x.body[1]+=' يقبل الإنشاء دون ضغط ملفات خاماً بمحاذاة512 أو2048 بايت حتى16 GiB وينشئ حاوية CHDv5 ويتحقق من SHA-256 بعد فكها. الناتج أكبر ولا ينشئ بيانات مسارات CD المختلطة أو صوراً تابعة.';TOOL_HELP.chd.goal=['Decode CHD data or create an uncompressed data container.','فك بيانات CHD أو إنشاء حاوية بيانات دون ضغط.'];}

{const x=TOOLS.find(x=>x.id==='capacity');x.desc=['One or three full write/read passes · erases drive','دورة أو ثلاث دورات كتابة وقراءة كاملة · تمحو الوحدة'];x.body[0]+=' Extended mode runs three complete random-pattern write/read passes with a new seed for every pass. It takes about three times the I/O and increases flash wear; it does not remap blocks, defragment, repair hardware or guarantee lifetime.';x.body[1]+=' ينفذ الوضع الموسع ثلاث دورات كاملة لكتابة وقراءة أنماط عشوائية ببذرة جديدة لكل دورة. يحتاج نحو ثلاثة أضعاف نقل البيانات ويزيد استهلاك الفلاش؛ لا يعيد تعيين القطاعات ولا يلغي التجزئة ولا يصلح العتاد أو يضمن العمر.';}
ROADMAP[5].body=['One-pass and extended three-pass full-drive capacity tests are available. Read-only resumable error mapping is available. Physical block remapping and filesystem defragmentation remain unsupported; controller remapping cannot be promised through ordinary USB mass storage.','يتوفر اختبار السعة بدورة واحدة أو ثلاث دورات كاملة، وتتوفر خريطة أخطاء قراءة قابلة للاستكمال. لا تدعم إعادة تعيين القطاعات العتادية وإلغاء تجزئة أنظمة الملفات؛ لا يمكن ضمان إعادة التعيين الداخلية عبر USB العادي.'];
ROADMAP[7].body=['Stored CHDv5 creation and standalone HD/DVD decoding are available. Creation is uncompressed and slightly larger than the raw input. Compressed encoding, CD/mixed-track reconstruction and VCD still need separate implementations and loader tests.','يتوفر إنشاء CHDv5 دون ضغط وفك بيانات القرص وDVD المستقلة. الإنشاء دون ضغط ويزيد الحجم قليلاً عن الملف الخام. ما زال الإنشاء المضغوط وإعادة بناء CD والمسارات المختلطة وVCD يحتاج تطبيقات أخرى واختبارات المشغلات.'];
ROADMAP[8].body[0]+=' Traditional ZipCrypto password-protected ZIP is now available. AES ZIP, encrypted 7z/RAR and multipart volumes remain unsupported.';
ROADMAP[8].body[1]+=' يتوفر الآن ZIP المحمي بكلمة مرور بالتشفير التقليدي ZipCrypto. لا تدعم AES ZIP أو 7z وRAR المشفرين أو الأجزاء المتعددة.';


// File management uses Android's document picker; the duplicate manager was retired.
var removedManager=TOOLS.findIndex(function(x){return x.id==='files';});if(removedManager>=0)TOOLS.splice(removedManager,1);
TOOL_SECTIONS.forEach(function(x){x.tools=x.tools.filter(function(id){return id!=='files';});});
GUIDES.forEach(function(x){if(x.tools)x.tools=x.tools.filter(function(id){return id!=='files';});});
LEARN_GOALS.forEach(function(x){if(x.tools)x.tools=x.tools.filter(function(id){return id!=='files';});});

// Current custom-size bounds; the old preset examples no longer define the controls.
var customHelp={
 split:['Type a custom whole-number part size from 1 to 2047 MiB. Creates numbered .uwp containers with sizes and SHA-256 checks; keep all parts for Rejoin split parts. These generic containers are not raw console parts or boot images. Source files stay unchanged; a new output folder prevents overwriting existing files.','اكتب حجم جزء مخصصاً صحيحاً من 1 إلى 2047 MiB. ينشئ حاويات .uwp مرقمة مع الأحجام وبصمات SHA-256؛ احتفظ بكل الأجزاء للدمج. ليست أجزاء منصات خاماً أو صور إقلاع. لا يغير المصدر وينشئ مجلد ناتج جديداً دون استبدال الملفات.'],
 fileBenchmark:['Type a custom temporary-file size from 1 to 1024 MiB. Writes a new test file, syncs, reads and compares every byte, then removes only that temporary file. Needs enough destination space. Provider/OS caching affects speeds; this is not a full-drive capacity or health test.','اكتب حجم الملف المؤقت من 1 إلى 1024 MiB. يكتب ملف اختبار جديداً ويزامنه ويقرأه ويقارن كل البايتات ثم يحذف ملف الاختبار فقط. يحتاج مساحة كافية. تؤثر ذاكرة النظام والمزود المؤقتة في السرعة؛ ليس اختبار سعة أو صحة للوحدة كاملة.'],
 consoleParts:['Type a custom part size from 1 to 3840 MiB for ISO.# or .666NN raw naming. Up to 64 parts and 64 GiB input. ISO.# requires 2048-byte alignment. Parts and their .usbwb manifest are verified; select the complete matching set to rejoin and compare with the source. Console/manager compatibility requires separate real-loader testing.','اكتب حجم جزء مخصصاً من 1 إلى 3840 MiB لتسمية ISO.# أو .666NN الخام. الحد 64 جزءاً ومصدر 64 GiB. تحتاج ISO.# محاذاة 2048 بايت. يتحقق من الأجزاء وملف .usbwb؛ اختر المجموعة الكاملة للدمج والمقارنة بالأصل. يحتاج توافق المنصة أو المدير اختباراً فعلياً منفصلاً.'],
 wimSplit:['Choose your standalone WIM and a new destination, then type a target part size from 1 to 3800 MiB (default 3800). Verifies the source, streams real install.swm/install2.swm parts with write readback, and verifies the whole SWM set using wimlib. No whole-WIM phone cache. ESD/solid variants or resources too large for the chosen target can be refused. Keep every part together; actual Windows installer boot is a separate PC test.','اختر WIM مستقلة ووجهة جديدة ثم اكتب الحجم المستهدف للجزء من 1 إلى 3800 MiB (الافتراضي 3800). يتحقق من المصدر ويكتب install.swm وinstall2.swm بالتدفق مع قراءة الكتابات للتحقق ثم يفحص المجموعة بـwimlib. لا يخزن WIM كاملة على الهاتف. قد ترفض ESD أو الضغط الصلب أو الموارد الأكبر من الحجم المستهدف. احتفظ بكل الأجزاء معاً؛ إقلاع مثبت Windows اختبار منفصل على الحاسوب.']
};
Object.keys(customHelp).forEach(function(id){var tool=TOOLS.find(function(x){return x.id===id;});if(tool)tool.body=customHelp[id];});
