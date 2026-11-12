/* Owned ES5 compatibility helpers; no remote code, dependencies or network. */
(function () {
    'use strict';
    if (!Array.prototype.find) Object.defineProperty(Array.prototype, 'find', {value:function (fn, receiver) {for(var i=0;i<this.length;i++){if(fn.call(receiver,this[i],i,this))return this[i];}return undefined;}});
    if (!Array.prototype.includes) Object.defineProperty(Array.prototype, 'includes', {value:function (value, from) {var n=this.length>>>0,i=Number(from)||0;if(i<0)i=Math.max(n+i,0);for(;i<n;i++)if(this[i]===value||(this[i]!==this[i]&&value!==value))return true;return false;}});
    if (!String.prototype.includes) Object.defineProperty(String.prototype, 'includes', {value:function(value,from){return this.indexOf(String(value),from||0)!==-1;}});
    if (!String.prototype.startsWith) Object.defineProperty(String.prototype, 'startsWith', {value:function(value,from){from=from||0;return this.substr(from,String(value).length)===String(value);}});
    if (!String.prototype.endsWith) Object.defineProperty(String.prototype, 'endsWith', {value:function(value,end){value=String(value);var n=end===undefined?this.length:Math.min(this.length,end);return this.substring(n-value.length,n)===value;}});
    if (!String.prototype.padStart) Object.defineProperty(String.prototype, 'padStart', {value:function(size,pad){size=Math.min(10000,Math.max(0,Number(size)||0));var s=String(this);pad=pad===undefined?' ':String(pad);if(!pad)return s;var out='';while(out.length<size-s.length)out+=pad;return out.slice(0,Math.max(0,size-s.length))+s;}});
    if (window.Element) {
        if (!Element.prototype.matches) Element.prototype.matches=Element.prototype.msMatchesSelector||Element.prototype.webkitMatchesSelector;
        if (!Element.prototype.closest) Element.prototype.closest=function(selector){var el=this;while(el&&el.nodeType===1){if(el.matches(selector))return el;el=el.parentElement;}return null;};
    }
    var match=/Chrom(?:e|ium)\/(\d+)/.exec(navigator.userAgent||'');
    if (!window.CSS || !CSS.supports || !CSS.supports('display','grid') || (match&&Number(match[1])<90)) document.documentElement.className+=' legacy-webview';
}());
