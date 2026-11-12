'use strict';
const fs=require('node:fs'),path=require('node:path');
const acorn=require(path.resolve('.downloads/acorn/package/dist/acorn.js'));
let count=0;
for(const file of ['compat.js','catalog.js','app.js']){
 acorn.parse(fs.readFileSync('app/src/main/assets/'+file,'utf8'),{ecmaVersion:5});count++;
}
const css=fs.readFileSync('app/src/main/assets/style.css','utf8');
if(/var\(--/.test(css))throw Error('Unresolved CSS variables break original Android6 WebView');
if(!css.includes('.legacy-webview')||!css.includes('padding-right:43px'))throw Error('Legacy layout/RTL fallback missing');
console.log(`PASS: ${count} packaged scripts parse as ES5; static-color CSS and legacy RTL fallbacks present`);
