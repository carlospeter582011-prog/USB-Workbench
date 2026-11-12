'use strict';
// Exercises the actual offline UI scripts with a small DOM adapter, not a browser rendering test.
const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const nodes=new Map(),events={},calls=[],storage={};
function node(id){if(!nodes.has(id))nodes.set(id,{innerHTML:'',textContent:'',value:'',hidden:true,style:{},dataset:{},addEventListener(type,fn){events[id+':'+type]=fn;},focus(){},parentElement:{scrollTop:0}});return nodes.get(id);}
const snapshot={devices:[],selected:'',image:'',imageSize:-1,tree:false,folder:'',files:[],running:false,status:'idle',history:[],result:'',error:''};
const document={documentElement:{className:''},body:{style:{}},getElementById:node,querySelector:()=>node('close'),querySelectorAll:()=>[],activeElement:null,addEventListener(type,fn){events[type]=fn;}};
const ctx=vm.createContext({console,document,navigator:{userAgent:'Mozilla/5.0 Chrome/44.0.2403.133 Mobile'},localStorage:{getItem:k=>storage[k]||null,setItem(k,v){storage[k]=v;}},Native:new Proxy({state:()=>JSON.stringify(snapshot)},{get(target,key){return target[key]||((...args)=>calls.push([key,...args]));}}),setInterval(){},setTimeout(){},clearTimeout(){},confirm:()=>true});ctx.window=ctx;ctx.scrollTo=()=>{};
vm.runInContext("delete Array.prototype.find;delete Array.prototype.includes;delete String.prototype.includes;delete String.prototype.startsWith;delete String.prototype.endsWith;delete String.prototype.padStart;",ctx);
for(const path of ['compat.js','catalog.js','app.js'])vm.runInContext(fs.readFileSync('app/src/main/assets/'+path,'utf8'),ctx,{filename:path});
let checks=0;function check(fn){fn();checks++;}function run(code){return vm.runInContext(code,ctx);}
function click(action,data={}){events.click({target:{closest(){return {dataset:{action,...data},disabled:false};}}});}
check(()=>assert.equal(document.documentElement.lang,'en'));
check(()=>assert.match(document.documentElement.className,/legacy-webview/));
check(()=>assert.equal(run("['x','y'].find(function(x){return x==='y';})"),'y'));
check(()=>assert.equal(run("'1'.padStart(2,'0')"),'01'));
check(()=>assert.match(node('content').innerHTML,/Search tools/));
check(()=>assert.match(node('content').innerHTML,/Select a USB/));
for(const tool of run('TOOLS')){click('tool',{id:tool.id});check(()=>assert.ok(node('sheetbody').innerHTML.includes(tool.name[0].replace(/&/g,'&amp;'))));}
for(const guide of run('GUIDES')){click('guide',{id:guide.id});check(()=>assert.match(node('sheetbody').innerHTML,/<ol class="steps">/));}
click('tool',{id:'cso'});check(()=>assert.match(node('sheetbody').innerHTML,/convert-mode/));node('convert-mode').value='isoCso';node('convert-limit').value='4';click('convertImage');check(()=>assert.deepEqual(calls[calls.length-1],['convertImage','isoCso',4]));
click('tool',{id:'compressedImage'});check(()=>assert.match(node('sheetbody').innerHTML,/xzUnpack/));node('convert-mode').value='xzUnpack';node('convert-limit').value='8';click('convertImage');check(()=>assert.deepEqual(calls[calls.length-1],['convertImage','xzUnpack',8]));
click('tool',{id:'fatImage'});node('fat-partition').value='1';click('fatImage',{id:'fatList'});check(()=>assert.deepEqual(calls[calls.length-1],['fatImage',false,1]));click('tool',{id:'fatImage'});node('fat-partition').value='0';click('fatImage',{id:'fatExtract'});check(()=>assert.deepEqual(calls[calls.length-1],['fatImage',true,0]));click('tool',{id:'ventoyGpt'});check(()=>assert.match(node('sheetbody').innerHTML,/data-id="ventoyInstallGpt"/));check(()=>assert.match(node('sheetbody').innerHTML,/data-id="ventoyUpdateGpt"/));check(()=>assert.match(node('sheetbody').innerHTML,/ERASES/));
click('tool',{id:'ventoyInstall'});check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/No GPT install or update yet/));click('guide',{id:'ventoyGuide'});check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/GPT installation, data-preserving updates/));
click('tool',{id:'zso'});check(()=>assert.match(node('sheetbody').innerHTML,/zsoIso/));node('convert-mode').value='isoZso';node('convert-limit').value='8';click('convertImage');check(()=>assert.deepEqual(calls[calls.length-1],['convertImage','isoZso',8]));click('tool',{id:'consoleParts'});node('console-kind').value='iso';node('console-size').value='1024';node('console-name').value='own.iso';click('consoleSplit');check(()=>assert.deepEqual(calls[calls.length-1],['consoleSplit','iso',1024,'own.iso']));click('tool',{id:'consoleParts'});click('consoleJoin');check(()=>assert.deepEqual(calls[calls.length-1],['consoleJoin']));
click('tab',{id:'learn'});check(()=>assert.match(node('content').innerHTML,/What do you want your USB to do/));check(()=>assert.match(node('learn-questions').innerHTML,/boot a computer/));
let beforeLearn=calls.length;click('learnQuestion',{id:'bootcomputer'});check(()=>assert.match(node('sheetbody').innerHTML,/data-id="write"/));check(()=>assert.match(node('sheetbody').innerHTML,/data-id="windows"/));check(()=>assert.equal(calls.length,beforeLearn));
click('learnQuestion',{id:'tool-fileBenchmark'});check(()=>assert.match(node('sheetbody').innerHTML,/data-id="fileBenchmark"/));click('tool',{id:'fileBenchmark'});check(()=>assert.equal(calls.length,beforeLearn));
click('guide',{id:'benchBatch'});check(()=>assert.match(node('sheetbody').innerHTML,/data-id="persistence"/));
click('guide',{id:'downloads'});
check(()=>assert.match(node('sheetbody').innerHTML,/nothing downloads automatically/));
for(const id of ['windows','ubuntu','fedora','ventoy']){click('officialDownload',{id});check(()=>assert.ok(calls.some(x=>x[0]==='officialDownload'&&x[1]===id)));}
click('tab',{id:'tools'});check(()=>assert.match(node('content').innerHTML,/In development/));click('filter',{id:'planned'});check(()=>assert.match(node('toolresults').innerHTML,/NOT YET AVAILABLE/));
click('filter',{id:'boot'});check(()=>assert.match(node('toolresults').innerHTML,/Multi-ISO library/));check(()=>assert.doesNotMatch(node('toolresults').innerHTML,/Zero-fill drive/));
click('tab',{id:'activity'});check(()=>assert.match(node('content').innerHTML,/A fresh start/));
click('tab',{id:'files'});check(()=>assert.equal(run('tab'),'home'));check(()=>assert.equal(run("TOOLS.some(function(x){return x.id==='files';})"),false));
click('devices');check(()=>assert.match(node('sheetbody').innerHTML,/No USB device detected/));
snapshot.devices=[{id:'/dev/usb/1',name:'<img src=x onerror=evil()>',vendor:'1234:5678',supported:true,permission:true}];snapshot.selected='/dev/usb/1';run('poll()');click('devices');check(()=>assert.match(node('sheetbody').innerHTML,/&lt;img src=x onerror=evil\(\)&gt;/));check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/<img src=x/));
click('tool',{id:'write'});snapshot.image='Linux.iso';snapshot.imageSize=1048576;run('poll()');check(()=>assert.equal(node('source-name').textContent,'Linux.iso'));
snapshot.job='write';snapshot.running=true;snapshot.status='running';snapshot.total=100;snapshot.done=30;run('poll()');check(()=>assert.match(node('jobpanel').innerHTML,/30%/));
click('run',{id:'write'});check(()=>assert.match(node('toast').textContent,/already running/));
snapshot.running=false;snapshot.result='WRITE_VERIFIED';snapshot.status='complete';run('poll()');check(()=>assert.match(node('jobpanel').innerHTML,/verification complete/));
events['lang:click']();check(()=>assert.equal(document.documentElement.dir,'rtl'));check(()=>assert.equal(document.documentElement.lang,'ar'));click('tab',{id:'home'});check(()=>assert.match(node('content').innerHTML,/ابحث عن أداة/));
for(const tool of run('TOOLS')){click('tool',{id:tool.id});check(()=>assert.ok(node('sheetbody').innerHTML.includes(tool.name[1])));}
for(const guide of run('GUIDES')){click('guide',{id:guide.id});check(()=>assert.ok(node('sheetbody').innerHTML.includes(guide.name[1])));}
click('learnQuestion',{id:'copyphone'});check(()=>assert.match(node('sheetbody').innerHTML,/كيف أنسخ USB كاملةً إلى هاتفي/));click('tool',{id:'clone'});check(()=>assert.match(node('sheetbody').innerHTML,/data-action="clonePhone"/));click('clonePhone');check(()=>assert.ok(calls.some(x=>x[0]==='clonePhone')));
run("details({job:'diagnostic',error:'',result:'USB host / مضيف USB: true',displayResult:'مضيف USB: نعم'})");check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/USB host \/ مضيف/));check(()=>assert.match(node('sheetbody').innerHTML,/مضيف USB: نعم/));
click('guide',{id:'downloads'});check(()=>assert.match(node('sheetbody').innerHTML,/لا يبدأ أي تنزيل تلقائياً/));
for(let i=0;i<run('ROADMAP.length');i++){click('roadmap',{id:String(i)});check(()=>assert.match(node('sheetbody').innerHTML,/غير متاحة/));}
click('tab',{id:'tools'});check(()=>assert.match(node('content').innerHTML,/قيد التطوير/));click('filter',{id:'planned'});check(()=>assert.match(node('toolresults').innerHTML,/غير متاح بعد/));
check(()=>assert.ok(calls.some(x=>x[0]==='language'&&x[1]===true)));
check(()=>assert.equal(run("esc('<script>\"&')"),'&lt;script&gt;&quot;&amp;'));
click('tool',{id:'format'});for(const fs of ['fat12','fat16','fat32','exfat','ntfs','ext2','ext3','ext4','f2fs'])check(()=>assert.ok(node('sheetbody').innerHTML.includes('value="'+fs+'"')));
check(()=>assert.match(node('sheetbody').innerHTML,/<option disabled>ReFS/));
for(const fs of ['udf','hfsplus','apfs','btrfs','xfs']){check(()=>assert.match(node('sheetbody').innerHTML,new RegExp('value=\"'+fs+'\"')));node('filesystem').value=fs;node('cluster-size').value='8';run('refreshFormat()');check(()=>assert.equal(node('cluster-size').value,'0'));check(()=>assert.equal(node('cluster-size').disabled,true));check(()=>assert.equal(node('volume-label').maxLength,fs==='xfs'?12:15));if(fs==='apfs'){check(()=>assert.equal(node('partition-table').value,'gpt'));check(()=>assert.equal(node('partition-table').disabled,true));}}node('filesystem').value='fat32';run('refreshFormat()');check(()=>assert.equal(node('partition-table').disabled,false));check(()=>assert.equal(node('volume-label').maxLength,32));

for(const id of ['split','join','imageinfo','diagnostic','report']){click('tool',{id});check(()=>assert.match(node('sheetbody').innerHTML,/data-action="workshop"/));click('workshop',{id});check(()=>assert.ok(calls.some(x=>x[0]==='workshop'&&x[1]===id)));}
for(const id of ['randomread','scan','hex','checksums','planner','clone']){click('tool',{id});check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/undefined/));}
for(const id of ['windows','ventoyInstall','optical','tar']){click('tool',{id});check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/undefined/));check(()=>assert.match(node('sheetbody').innerHTML,/data-action="imageTool"/));}
for(const mode of ['windows','ventoyImport','opticalList','opticalExtract','tarList','tarExtract','wimSplit']){click('imageTool',{id:mode});check(()=>assert.ok(calls.some(x=>x[0]==='imageTool'&&x[1]===mode)));}
check(()=>assert.match(run("errorText('USB_CLAIM_FAILED')"),/هذا ليس دليلاً/));
check(()=>assert.match(run("errorText('TAR_CHECKSUM')"),/أرشيف TAR/));
check(()=>assert.match(run("errorText('ISO_ENDIAN')"),/تخطيط الصورة/));
check(()=>assert.match(run("errorText('WINDOWS_WIM_HEADER')"),/ملفات الإقلاع/));
node('filesystem').value='ext4';run('refreshFormat()');check(()=>assert.equal(node('cluster-size').disabled,true));check(()=>assert.equal(node('cluster-size').value,'0'));
click('recipe',{id:'0'});check(()=>assert.equal(node('filesystem').value,'exfat'));check(()=>assert.equal(node('cluster-size').disabled,false));
snapshot.job='checksums';snapshot.result='Checksums\nSHA-256 MISMATCH / عدم تطابق';snapshot.error='';snapshot.running=false;run('poll()');check(()=>assert.match(node('jobpanel').innerHTML,/بصمة غير متطابقة/));check(()=>assert.match(node('jobpanel').innerHTML,/jobbox error/));
node('filesystem').value='f2fs';run('refreshFormat()');check(()=>assert.equal(node('cluster-size').disabled,true));check(()=>assert.equal(node('cluster-size').value,'0'));
click('tool',{id:'filesystemCheck'});check(()=>assert.match(node('sheetbody').innerHTML,/fs-partition/));node('fs-partition').value='2';click('filesystemTool',{id:'check'});check(()=>assert.ok(calls.some(x=>x[0]==='filesystemTool'&&x[1]===false&&x[2]===2)));click('tool',{id:'filesystemCheck'});node('fs-partition').value='1';click('filesystemTool',{id:'repair'});check(()=>assert.ok(calls.some(x=>x[0]==='filesystemTool'&&x[1]===true&&x[2]===1)));
click('tool',{id:'archives'});node('archive-limit').value='8';click('archiveTool',{id:'list'});check(()=>assert.ok(calls.some(x=>x[0]==='archiveTool'&&x[1]===false&&x[2]===8)));click('tool',{id:'archives'});node('archive-limit').value='4';click('archiveTool',{id:'extract'});check(()=>assert.ok(calls.some(x=>x[0]==='archiveTool'&&x[1]===true&&x[2]===4)));click('tool',{id:'dmg'});check(()=>assert.match(node('sheetbody').innerHTML,/dmgRaw/));node('convert-mode').value='dmgRaw';node('convert-limit').value='4';click('convertImage');check(()=>assert.ok(calls.some(x=>x[0]==='convertImage'&&x[1]==='dmgRaw'&&x[2]===4)));click('tool',{id:'chd'});node('chd-limit').value='4';click('chdTool');check(()=>assert.ok(calls.some(x=>x[0]==='chdTool'&&x[1]===4)));
click('tool',{id:'fsImage'});node('fsimage-partition').value='0';node('fsimage-limit').value='8';click('fsImage',{id:'extract'});check(()=>assert.ok(calls.some(x=>x[0]==='fsImage'&&x[1]===true&&x[2]===0&&x[3]===8)));click('tool',{id:'verifyZero'});check(()=>assert.match(node('sheetbody').innerHTML,/data-id="verifyZero"/));
click('tool',{id:'fsUsb'});check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/undefined/));node('fsimage-partition').value='1';node('fsimage-limit').value='4';click('fsUsb',{id:'list'});check(()=>assert.ok(calls.some(x=>x[0]==='fsUsb'&&x[1]===false&&x[2]===1&&x[3]===4)));click('tool',{id:'verifyZero'});check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/undefined/));
// Actual new-card controls and bridge argument routing, both directions.
for(const language of [false,true]){
 ctx.ar=language;vm.runInContext("tool('wimTree')",ctx);assert.match(node('sheetbody').innerHTML,/tree-root/);assert.match(node('sheetbody').innerHTML,/archive-limit/);assert.match(node('sheetbody').innerHTML,/data-action="wimTree"/);
 vm.runInContext("tool('usbExtreme')",ctx);assert.match(node('sheetbody').innerHTML,/ul-title/);assert.match(node('sheetbody').innerHTML,/ul-media/);assert.match(node('sheetbody').innerHTML,/data-id="inspect"/);
}
node('tree-index').value='2';node('tree-root').value='/Owned';node('archive-limit').value='4';events.click({target:{closest:()=>({dataset:{action:'wimTree',id:'extract'},disabled:false})}});assert.deepEqual(calls.at(-1),['wimTree',true,2,4,'/Owned']);
node('ul-title').value='Owned Demo';node('ul-media').value='DVD';events.click({target:{closest:()=>({dataset:{action:'usbExtreme',id:'create'},disabled:false})}});assert.deepEqual(calls.at(-1),['usbExtreme',false,'Owned Demo','DVD']);
checks+=14;
// Home is the workshop; browsing and persistence controls must never trigger raw writes.
run("ar=false;query='';filter='all';openGroups={};go('home')");
check(()=>assert.ok(node('content').innerHTML.indexOf('devicehome')<node('content').innerHTML.indexOf('id="search"')));
check(()=>assert.doesNotMatch(node('content').innerHTML,/class="hero"/));
check(()=>assert.equal((node('toolresults').innerHTML.match(/aria-expanded="false"/g)||[]).length,8));
let browsingCalls=calls.length;click('group',{id:'boot'});
check(()=>assert.match(node('toolresults').innerHTML,/id="group-boot"[^>]*aria-expanded="true"/));
check(()=>assert.equal(calls.length,browsingCalls));
click('group',{id:'boot'});check(()=>assert.match(node('toolresults').innerHTML,/id="group-boot"[^>]*aria-expanded="false"/));
run("query='NTFS';renderToolResults()");check(()=>assert.match(node('toolresults').innerHTML,/aria-expanded="true"/));
check(()=>assert.doesNotMatch(node('toolresults').innerHTML,/aria-expanded="false"/));
run("query='zzzz-no-tool';renderToolResults()");check(()=>assert.match(node('toolresults').innerHTML,/No matching tools/));
run("query='';tool('format')");check(()=>assert.match(node('sheetbody').innerHTML,/<details class="tool-learn"><summary>Learn more/));
check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/<details class="tool-learn" open/));
check(()=>assert.match(node('sheetbody').innerHTML,/data-id="format"/));
run("burnMode='raw';tool('write')");check(()=>assert.match(node('sheetbody').innerHTML,/data-action="run" data-id="write"/));
node('burn-mode').value='ventoy';events.change({target:{id:'burn-mode',value:'ventoy'}});
check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/data-action="run"/));
check(()=>assert.match(node('sheetbody').innerHTML,/createPersistence/));
check(()=>assert.match(node('sheetbody').innerHTML,/id="persistence-size"[^>]*min="256"[^>]*max="16384"/));
check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/id="target-detail"/));
const guardBefore=calls.length;click('run',{id:'write'});check(()=>assert.equal(calls.length,guardBefore));
check(()=>assert.equal(calls.length,browsingCalls));
node('persistence-label').value='casper-rw';events.change({target:{id:'persistence-preset',value:'persistence'}});check(()=>assert.equal(node('persistence-label').value,'persistence'));
node('persistence-label').value='my-live';events.change({target:{id:'persistence-preset',value:'custom'}});check(()=>assert.equal(node('persistence-label').value,'my-live'));
for(const invalid of ['255','16385','12.5','NaN','Infinity']){node('persistence-size').value=invalid;node('persistence-iso').value='/ISO/own.iso';const before=calls.length;click('createPersistence');check(()=>assert.equal(calls.length,before));}
node('persistence-size').value='768';node('persistence-label').value='my-live';node('persistence-iso').value='/ISO/own.iso';click('createPersistence');check(()=>assert.deepEqual(calls.at(-1),['benchTool','persistence',768,'my-live','/ISO/own.iso']));
for(const path of ['/ISO/../x.iso','ISO/x.iso','/ISO/x.img','/ISO//x.iso']){run("tool('write')");node('persistence-size').value='768';node('persistence-label').value='persistence';node('persistence-iso').value=path;const before=calls.length;click('createPersistence');check(()=>assert.equal(calls.length,before));}
run("ar=true;query='';filter='all';go('home')");check(()=>assert.match(node('content').innerHTML,/ابحث عن أداة/));check(()=>assert.match(node('toolresults').innerHTML,/إدارة القرص/));
run("tool('write')");check(()=>assert.match(node('sheetbody').innerHTML,/معرفة المزيد/));check(()=>assert.match(node('sheetbody').innerHTML,/حجم الاستمرار المخصص/));
// Direct persistence is distinct from Ventoy and always routes through native review.
run("ar=false;state.running=false;burnMode='raw';tool('write')");node('burn-mode').value='persistent';events.change({target:{id:'burn-mode',value:'persistent'}});
check(()=>assert.match(node('sheetbody').innerHTML,/data-action="writePersistent"/));check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/data-action="run"/));
check(()=>assert.match(node('sheetbody').innerHTML,/id="target-detail"/));check(()=>assert.doesNotMatch(node('sheetbody').innerHTML,/id="persistence-iso"/));check(()=>assert.match(node('sheetbody').innerHTML,/MBR hybrid ISOs only/));
node('persistence-size').value='768';node('persistence-label').value='persistence';click('writePersistent');check(()=>assert.deepEqual(calls.at(-1),['writePersistent',768,'persistence']));
for(const size of ['255','16385','1.5']){run("tool('write')");node('persistence-size').value=size;node('persistence-label').value='persistence';const before=calls.length;click('writePersistent');check(()=>assert.equal(calls.length,before));}
run("ar=true;tool('write')");check(()=>assert.match(node('sheetbody').innerHTML,/قسم ext4/));check(()=>assert.match(node('sheetbody').innerHTML,/مراجعة ISO والاستمرار/));
// Home consolidation and independent persistent pin controls run the real packaged UI.
run("ar=false;query='';go('home')");const pinCalls=calls.length;
check(()=>assert.equal((node('nav').innerHTML.match(/data-action="tab"/g)||[]).length,3));
check(()=>assert.doesNotMatch(node('nav').innerHTML,/data-id="tools"/));
click('pin',{id:'write'});check(()=>assert.deepEqual(JSON.parse(storage.pinnedTools),['write']));
check(()=>assert.match(node('toolresults').innerHTML,/pinned-group/));
check(()=>assert.equal((node('toolresults').innerHTML.match(/data-action="pin" data-id="write" aria-pressed="true"/g)||[]).length,2));
check(()=>assert.deepEqual(JSON.parse(run('JSON.stringify(loadPins())')),['write']));
run("query='zzzz-no-tool';renderToolResults()");check(()=>assert.doesNotMatch(node('toolresults').innerHTML,/pinned-group/));
run("query='';go('tools')");check(()=>assert.equal(run('tab'),'home'));check(()=>assert.match(node('content').innerHTML,/devicehome/));
run("ar=true;renderToolResults()");check(()=>assert.match(node('toolresults').innerHTML,/إلغاء تثبيت/));
click('pin',{id:'write'});check(()=>assert.deepEqual(JSON.parse(storage.pinnedTools),[]));check(()=>assert.doesNotMatch(node('toolresults').innerHTML,/pinned-group/));
click('pin',{id:'unknown'});check(()=>assert.deepEqual(JSON.parse(storage.pinnedTools),[]));check(()=>assert.equal(calls.length,pinCalls));
for(const value of ['bad','{}','["write","write","unknown",null]']){storage.pinnedTools=value;check(()=>assert.equal(run('loadPins().length'),value.charAt(0)==='['?1:0));}
storage.pinnedTools='[]';run("ar=false;go('learn')");
check(()=>assert.match(node('content').innerHTML,/class="search" type="search" id="learn-search"/));
check(()=>assert.equal((node('content').innerHTML.match(/class="learning-shortcut"/g)||[]).length,3));
run("ar=false;state.running=false;tool('capacity')");check(()=>assert.match(node('sheetbody').innerHTML,/capacity-passes/));node('capacity-passes').value='3';click('run',{id:'capacity'});check(()=>assert.equal(calls.at(-1)[1],'capacityDeep'));
run("tool('archives')");node('archive-password').value='fixture-only';node('archive-limit').value='4';click('archiveTool',{id:'list'});check(()=>assert.deepEqual(calls.at(-1),['archivePassword',false,4,'fixture-only']));check(()=>assert.equal(node('archive-password').value,''));
run("tool('chd')");node('chd-mode').value='stored2048';node('chd-limit').value='4';click('chdTool');check(()=>assert.deepEqual(calls.at(-1),['createChd',2048,4]));

// Reported regressions: a listing/progress stays in its original form; Back never cancels.
if(run('ar'))events['lang:click']();
snapshot.running=false;snapshot.status='idle';snapshot.result='';snapshot.error='';snapshot.job='';run('poll()');
click('tool',{id:'tar'});click('imageTool',{id:'tarList'});check(()=>assert.equal(run('modal.id'),'tar'));check(()=>assert.equal(node('overlay').hidden,false));
snapshot.running=true;snapshot.status='running';snapshot.job='tarList';snapshot.done=50;snapshot.total=100;run('poll()');check(()=>assert.match(node('tool-job').innerHTML,/50%/));check(()=>assert.equal(node('jobpanel').hidden,true));
snapshot.running=false;snapshot.status='complete';snapshot.result='TAR\nhello.txt';run('poll()');check(()=>assert.match(node('tool-job').innerHTML,/hello.txt/));check(()=>assert.equal(run('modal.id'),'tar'));
click('imageTool',{id:'tarExtract'});snapshot.running=true;snapshot.status='running';snapshot.job='tarExtract';run('poll()');const cancels=calls.filter(x=>x[0]==='cancel').length;click('close');check(()=>assert.equal(node('jobpanel').hidden,false));check(()=>assert.equal(run('state.running'),true));check(()=>assert.equal(calls.filter(x=>x[0]==='cancel').length,cancels));
click('tool',{id:'tar'});check(()=>assert.equal(node('jobpanel').hidden,true));check(()=>assert.match(node('tool-job').innerHTML,/progressbar/));
snapshot.running=false;snapshot.status='complete';snapshot.zipInputs=['first.txt','second.txt'];run('poll()');click('tool',{id:'createzip'});check(()=>assert.match(node('zip-selection').innerHTML,/first.txt/));check(()=>assert.match(node('sheetbody').innerHTML,/Add files/));click('addZipFiles');check(()=>assert.deepEqual(calls.at(-1),['addZipFiles']));click('removeZipInput',{id:'1'});check(()=>assert.deepEqual(calls.at(-1),['removeZipInput',1]));click('createSelectedZip');check(()=>assert.deepEqual(calls.at(-1),['createSelectedZip']));check(()=>assert.equal(run('modal.id'),'createzip'));
click('tool',{id:'split'});node('part-size').value='3';click('workshop',{id:'split'});check(()=>assert.deepEqual(calls.at(-1),['workshop','split',3*1048576]));
click('tool',{id:'fileBenchmark'});node('bench-size').value='3';click('benchTool',{id:'fileBenchmark'});check(()=>assert.deepEqual(calls.at(-1),['benchTool','fileBenchmark',3,'','']));
click('tool',{id:'cso'});check(()=>assert.match(node('sheetbody').innerHTML,/<input id="convert-limit" type="number"/));node('convert-mode').value='isoCso';node('convert-limit').value='0.5';let queryAll=document.querySelectorAll;const numeric={value:'0.5',min:'0.0009765625',max:'16',step:'any',focus(){}};document.querySelectorAll=q=>q==='#sheetbody input[type=number]'?[numeric]:[];click('convertImage');check(()=>assert.deepEqual(calls.at(-1),['convertImage','isoCso',0.5]));numeric.value='NaN';let numCalls=calls.length;click('convertImage');check(()=>assert.equal(calls.length,numCalls));check(()=>assert.match(node('toast').textContent,/number/));document.querySelectorAll=queryAll;
click('tool',{id:'wimSplit'});node('wim-part-size').value='17';click('splitWim');check(()=>assert.deepEqual(calls.at(-1),['splitWim',17]));
click('tool',{id:'wimExplore'});node('wim-index').value='1';click('benchTool',{id:'wimList'});snapshot.running=false;snapshot.status='complete';snapshot.job='wimList';snapshot.result='WIM_LIST\n/folder\n/folder/hello.txt';run('poll()');check(()=>assert.match(node('tool-job').innerHTML,/data-action="chooseWimPath"/));click('chooseWimPath',{path:'/folder/hello.txt'});check(()=>assert.equal(node('wim-path').value,'/folder/hello.txt'));check(()=>assert.equal(run('modal.id'),'wimExplore'));
click('tool',{id:'format'});node('filesystem').value='fat32';run('refreshFormat()');node('cluster-size').value='custom';node('custom-cluster-bytes').value='8192';run('customCluster()');check(()=>assert.equal(node('custom-cluster-bytes').hidden,false));node('volume-label').value='USBTEST';node('partition-table').value='mbr';click('run',{id:'format'});check(()=>assert.deepEqual(calls.at(-1),['usbAdvanced','format','USBTEST',16,'fat32','mbr']));node('custom-cluster-bytes').value='1536';let beforeCluster=calls.length;click('run',{id:'format'});check(()=>assert.equal(calls.length,beforeCluster));check(()=>assert.match(node('toast').textContent,/power of two/));
// A repeated fast listing may complete entirely between polls; a new history timestamp must finish the inline waiting state.
snapshot.error='';snapshot.running=false;snapshot.status='complete';snapshot.job='tarList';snapshot.result='TAR\nhello.txt';snapshot.history=[{time:1}];run('poll()');click('tool',{id:'tar'});click('imageTool',{id:'tarList'});snapshot.history=[{time:2}];run('poll()');check(()=>assert.equal(run('toolPending'),false));check(()=>assert.match(node('tool-job').innerHTML,/hello.txt/));
console.log(`PASS: ${checks} UI logic, English/Arabic, tool/guide rendering, progress and escaping checks`);
