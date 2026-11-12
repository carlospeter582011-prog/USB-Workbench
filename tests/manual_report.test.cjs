'use strict';
const fs=require('fs'),vm=require('vm'),assert=require('assert/strict');
const html=fs.readFileSync('artifacts/USBtests.html','utf8'),script=[...html.matchAll(/<script(?: [^>]*)?>([\s\S]*?)<\/script>/g)].at(-1)[1];
let saved={},clipboard='',downloads=[];
function boot(){const nodes={},events={};function node(id){return nodes[id]||(nodes[id]={id,value:'',textContent:'',innerHTML:'',children:[],dataset:{},files:[],hidden:true,focus(){},select(){},click(){},remove(){},querySelectorAll(){return[];}});}
const ctx={document:{getElementById:node,querySelectorAll:()=>[],addEventListener:(k,f)=>events[k]=f,body:{appendChild(){}},createElement:()=>({click(){},remove(){}})},localStorage:{getItem:k=>saved[k]||null,setItem:(k,v)=>saved[k]=v},navigator:{clipboard:{writeText(t){clipboard=t;return Promise.resolve();}}},Date,confirm:()=>true,alert(){},Blob:function(parts,opts){this.text=parts.join('');this.type=opts.type;downloads.push(this);},URL:{createObjectURL(){return 'blob:test';},revokeObjectURL(){}},setTimeout(){},FileReader:function(){this.readAsText=f=>{this.result=f.text;this.onload();};}};
node('saved-results').textContent='{}';vm.runInNewContext(script,ctx);return {ctx,node,events};}
function result(b,id,status){b.events.click({target:{closest:selector=>selector==='button'?{dataset:{case:id,status},parentNode:{querySelectorAll:()=>[]}}:null}});}
function input(b,dataset,value){b.events.input({target:{dataset,value}});}
(async()=>{let b=boot();const sections=b.ctx.SECTIONS,all=sections.flatMap(g=>g.cases),tools=sections.filter(g=>g.id.startsWith('tool-')&&!g.retired);
assert.equal(tools.length,51);assert.equal(all.filter(c=>c.guided).length,84);assert.equal(new Set(all.map(c=>c.id)).size,all.length);
for(const g of tools){let c=g.cases.find(c=>c.guided);assert.ok(c.why.length>25&&c.need.length>10&&Array.isArray(c.steps)&&c.steps.length>=3&&c.expected.length>20,g.id);}
assert.ok(b.node('sections').innerHTML.includes('Do these steps:'));assert.ok(b.node('sections').innerHTML.includes('<ol>'));assert.ok(!b.node('sections').innerHTML.includes('Tool-specific scenario'));
input(b,{meta:'candidate'},'0.16.0 exact APK');input(b,{meta:'device'},'Lenovo Android12');input(b,{meta:'notes'},'General note');input(b,{tool:'tool-files'},'Tool note');
result(b,'tool-extract-walkthrough-v2','unsure');input(b,{notes:'tool-extract-walkthrough-v2'},'عربي <script>not code</script> 😀');result(b,'tool-hash-walkthrough-v2','fail');input(b,{notes:'tool-hash-walkthrough-v2'},'Checksum abc != expected def');result(b,'tool-zip-walkthrough-v2','pass');
assert.match(b.node('save-status').textContent,/Saved/);assert.ok(saved['usbworkbench-manual-tests-v1']);
b=boot();b.node('copyreport').onclick();await Promise.resolve();let report=b.node('reporttext').value;
assert.equal(clipboard,report);assert.ok(report.includes('\n=== '));assert.ok(!report.includes('\\n=== '));assert.ok(report.includes('[tool-extract-walkthrough-v2] Not sure'));assert.ok(report.includes('عربي <script>not code</script> 😀'));assert.ok(report.includes('Checksum abc != expected def'));assert.ok(report.includes('General note')&&report.includes('Tool note'));assert.ok(report.includes('Lenovo Android12'));assert.equal((report.match(/^\[/gm)||[]).length,all.length);
// Search/filter cannot truncate the report.
b.node('search').value='nonexistent text';b.events.input({target:b.node('search')});b.node('copyreport').onclick();assert.equal((b.node('reporttext').value.match(/^\[/gm)||[]).length,all.length);
// Every byte survives the automatic chat-sized parts.
let parts=[];b.node('copypart').onclick();await Promise.resolve();let first=b.node('reporttext').value,total=Number(first.match(/PART 1 \/ (\d+)/)[1]);parts.push(first.split('\n').slice(1).join('\n'));
for(let i=1;i<total;i++){b.node('copypart').onclick();await Promise.resolve();let part=b.node('reporttext').value;assert.ok(part.startsWith('USBTESTS REPORT PART '+(i+1)+' / '+total));parts.push(part.split('\n').slice(1).join('\n'));}
const joined=parts.join('');assert.equal((joined.match(/^\[/gm)||[]).length,all.length);assert.ok(joined.includes('عربي <script>not code</script> 😀'));assert.ok(parts.every(x=>x.length<=12000));const normalize=s=>s.replace(/^Generated: .*$/m,'Generated: TIME');assert.equal(normalize(joined),normalize(report));
// JSON backup and import retain Maybe, Unicode, old IDs and notes.
b.node('export').onclick();const backup=downloads.at(-1).text;assert.equal(JSON.parse(backup).cases['tool-extract-walkthrough-v2'].status,'unsure');
result(b,'tool-extract-walkthrough-v2','pass');b.node('importfile').files=[{size:backup.length,text:backup}];b.node('importfile').onchange();b.node('copyreport').onclick();assert.ok(b.node('reporttext').value.includes('[tool-extract-walkthrough-v2] Not sure'));
// Copy fallback still exposes the entire report if Clipboard API is absent.
delete b.ctx.navigator.clipboard;b.node('copyreport').onclick();assert.equal(b.node('reportbox').hidden,false);assert.equal((b.node('reporttext').value.match(/^\[/gm)||[]).length,all.length);
// Saving a portable HTML snapshot cannot inject notes as executable HTML.
b.ctx.document.documentElement={outerHTML:''};b.node('savehtml').onclick();const seed=b.node('saved-results').textContent;assert.ok(!seed.includes('<script>'));assert.equal(JSON.parse(seed).cases['tool-extract-walkthrough-v2'].notes,'عربي <script>not code</script> 😀');
input(b,{meta:'candidate'},'new APK');b.node('copyreport').onclick();assert.ok(b.node('reporttext').value.includes('[tool-zip-walkthrough-v2] Worked |'));assert.ok(!b.node('reporttext').value.includes('[tool-zip-walkthrough-v2] Worked — RETEST REQUIRED'));
// Legacy results remain reportable after the guide update.
const legacy={schema:1,updated:1,meta:{candidate:'legacy'},cases:{'setup-0':{status:'fail',notes:'OLD SAVED NOTE',candidate:'legacy'}},tools:{'tool-files':'OLD TOOL NOTE'}};b.node('importfile').files=[{size:100,text:JSON.stringify(legacy)}];b.node('importfile').onchange();b.node('copyreport').onclick();assert.ok(b.node('reporttext').value.includes('[setup-0] Did not work'));assert.ok(b.node('reporttext').value.includes('OLD SAVED NOTE')&&b.node('reporttext').value.includes('OLD TOOL NOTE'));
// A failed browser save is visible, while the complete in-memory report still works.
b.ctx.localStorage.setItem=()=>{throw Error('quota');};input(b,{notes:'setup-0'},'Unsaved but reportable');assert.match(b.node('save-status').textContent,/save failed/);b.node('copyreport').onclick();assert.ok(b.node('reporttext').value.includes('Unsaved but reportable'));b.node('downloadreport').onclick();assert.ok(downloads.at(-1).text.includes('Unsaved but reportable'));
console.log('PASS: 51 numbered walkthroughs, 84 guided/historical cases, five assigned equipment sessions plus optional comfort, unique IDs, Maybe autosave/reload/import, full unfiltered report, all chat parts, Unicode/HTML safety, clipboard fallback and candidate retest.');
})().catch(e=>{console.error(e);process.exitCode=1;});
