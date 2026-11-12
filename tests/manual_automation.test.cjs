'use strict';
const fs=require('fs'),vm=require('vm'),assert=require('assert/strict');
const html=fs.readFileSync('artifacts/USBtests.html','utf8');const script=[...html.matchAll(/<script(?: [^>]*)?>([\s\S]*?)<\/script>/g)].at(-1)[1];
let cache=null;
function boot(){const nodes={},events={};const node=id=>nodes[id]||(nodes[id]={value:'',textContent:'',innerHTML:'',children:[],dataset:{},hidden:true,focus(){},select(){},scrollIntoView(){},querySelectorAll(){return[];}});node('saved-results').textContent=html.match(/<script id="saved-results" type="application\/json">([\s\S]*?)<\/script>/)[1];const ctx={document:{getElementById:node,querySelectorAll:()=>[],addEventListener:(e,f)=>events[e]=f},localStorage:{getItem:()=>cache,setItem:(k,v)=>cache=v},Date,alert(){}};vm.runInNewContext(script,ctx);return {ctx,node,events};}
function status(b,id,value){b.events.click({target:{closest:k=>k==='button'?{dataset:{case:id,status:value},parentNode:{querySelectorAll:()=>[]}}:null}});}
let b=boot(),cases=b.ctx.SECTIONS.flatMap(g=>g.cases),human=cases.filter(c=>c.humanOnly);
assert.equal(human.length,6);assert.equal(cases.filter(c=>c.developerOwned).length,455);assert.equal((b.node('sections').innerHTML.match(/class="case"/g)||[]).length,5);
assert.ok(!b.node('sections').innerHTML.includes('tool-cso-walkthrough-v2'));assert.ok(b.node('automation-evidence').innerHTML.includes('not prove'));
status(b,'human-provider-v4','pass');status(b,'human-usb-read-v4','blocked');b.events.input({target:{dataset:{notes:'human-usb-read-v4'},value:'Need an OTG adapter — غير متوفر'}});
b=boot();assert.equal((b.node('sections').innerHTML.match(/class="case"/g)||[]).length,4);b.node('nexttest').onclick();assert.equal(b.node('group-human-only-v4').open,true);assert.ok(b.node('case-human-mount-v4'));
b.node('alltests').onclick();assert.equal((b.node('sections').innerHTML.match(/class="case"/g)||[]).length,461);assert.ok(b.node('sections').innerHTML.includes('Developer owns this detailed case'));
b.node('copyreport').onclick();const report=b.node('reporttext').value;assert.ok(report.includes('[human-provider-v4] Worked'));assert.ok(report.includes('Need an OTG adapter — غير متوفر'));assert.ok(report.includes('[tool-cso-walkthrough-v2] Blocked'));assert.ok(report.includes('OWNER: developer / historical'));assert.ok(report.includes('"runs"'));
const saved=JSON.parse(cache);assert.equal(Object.keys(saved.cases).length,452);assert.equal(saved.cases['tool-cso-walkthrough-v2'].status,'blocked');assert.equal(saved.cases['tool-wimExplore-walkthrough-v2'].status,'unsure');
console.log('PASS: five equipment-only sessions plus optional comfort, 455 developer-owned/history cases, no invented manual passes, autosave/reload, blocked-next routing, full historical and automated report.');
