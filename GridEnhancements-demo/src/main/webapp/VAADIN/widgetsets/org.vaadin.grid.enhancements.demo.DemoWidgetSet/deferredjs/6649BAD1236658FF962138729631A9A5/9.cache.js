$wnd.org_vaadin_grid_enhancements_demo_DemoWidgetSet.runAsyncCallback9("function yzb(a){return a.g}\nfunction Azb(a,b){Ayb(a,b);--a.i}\nfunction Jdd(){t0b.call(this)}\nfunction KOd(){_Ld.call(this);this.G=HLe}\nfunction On(a){return (cl(),bl).Fc(a,'col')}\nfunction bv(a){var b;b=a.e;if(b){return _u(a,b)}return hp(a.d)}\nfunction Bvb(a,b,c,d){var e;Xnb(b);e=a.Wb.c;a.Gf(b,c,d);pvb(a,b,(Jrb(),a.bc),e,true)}\nfunction Evb(){Fvb.call(this,(Jrb(),Pn($doc)));this.bc.style[$ke]=Qre;this.bc.style[hpe]=mle}\nfunction imc(a,b){NZb(a.a,new lwc(new Bwc(Pab),'openPopup'),tB(pB(tdb,1),Fje,1,3,[(HTd(),b?GTd:FTd)]))}\nfunction zzb(a,b){if(b<0){throw new BTd('Cannot access a row with a negative index: '+b)}if(b>=a.i){throw new BTd(goe+b+hoe+a.i)}}\nfunction Czb(a,b){if(a.i==b){return}if(b<0){throw new BTd('Cannot set number of rows to '+b)}if(a.i<b){Ezb((Jrb(),a.G),b-a.i,a.g);a.i=b}else{while(a.i>b){Azb(a,a.i-1)}}}\nfunction Dvb(a,b,c){var d;d=(Jrb(),a.bc);if(b==-1&&c==-1){Hvb(d)}else{Kp(d.style,$ke,ble);Kp(d.style,Jle,b+Tle);Kp(d.style,Uoe,c+Tle)}}\nfunction Dzb(a,b){jyb();Gyb.call(this);Byb(this,new $yb(this));Eyb(this,new pAb(this));Cyb(this,new eAb(this));Bzb(this,b);Czb(this,a)}\nfunction dAb(a,b,c){var d,e;b=b>1?b:1;e=a.a.childNodes.length;if(e<b){for(d=e;d<b;d++){Qj(a.a,On($doc))}}else if(!c&&e>b){for(d=e;d>b;d--){Zj(a.a,a.a.lastChild)}}}\nfunction Ezb(a,b,c){var d=$doc.createElement('td');d.innerHTML=Cre;var e=$doc.createElement('tr');for(var f=0;f<c;f++){var g=d.cloneNode(true);e.appendChild(g)}a.appendChild(e);for(var h=1;h<b;h++){a.appendChild(e.cloneNode(true))}}\nfunction Idd(a){if((!a.U&&(a.U=_Mb(a)),AB(AB(a.U,6),210)).c&&((!a.U&&(a.U=_Mb(a)),AB(AB(a.U,6),210)).v==null||gWd('',(!a.U&&(a.U=_Mb(a)),AB(AB(a.U,6),210)).v))){return (!a.U&&(a.U=_Mb(a)),AB(AB(a.U,6),210)).a}return (!a.U&&(a.U=_Mb(a)),AB(AB(a.U,6),210)).v}\nfunction Bzb(a,b){var c,d,e,f,g,h,j;if(a.g==b){return}if(b<0){throw new BTd('Cannot set number of columns to '+b)}if(a.g>b){for(c=0;c<a.i;c++){for(d=a.g-1;d>=b;d--){lyb(a,c,d);e=nyb(a,c,d,false);f=mAb(a.G,c);f.removeChild(e)}}}else{for(c=0;c<a.i;c++){for(d=a.g;d<b;d++){g=mAb(a.G,c);h=(j=(Jrb(),qo($doc)),Lk(j,Cre),Jrb(),j);Hrb.nf(g,asb(h),d)}}}a.g=b;dAb(a.I,b,false)}\nvar BLe='popupVisible',CLe='showDefaultCaption',DLe='setColor',ELe='setOpen',FLe='background',GLe='com.vaadin.client.ui.colorpicker',HLe='v-colorpicker',ZLe='v-default-caption-width';djb(1,null,{});_.gC=function Y(){return this.cZ};djb(537,249,Toe,Evb);_.Gf=function Jvb(a,b,c){Dvb(a,b,c)};djb(135,9,Xoe);_.Dd=function _vb(a){return Rnb(this,a,(dv(),dv(),cv))};djb(772,31,kpe);_.Dd=function Hyb(a){return Rnb(this,a,(dv(),dv(),cv))};djb(593,772,kpe,Dzb);_.Qf=function Fzb(a){return yzb(this)};_.Rf=function Gzb(){return this.i};_.Sf=function Hzb(a,b){zzb(this,a);if(b<0){throw new BTd('Cannot access a column with a negative index: '+b)}if(b>=this.g){throw new BTd(ipe+b+jpe+this.g)}};_.Tf=function Izb(a){zzb(this,a)};_.g=0;_.i=0;var oJ=vUd(boe,'Grid',593);djb(104,533,ppe);_.Dd=function Ozb(a){return Rnb(this,a,(dv(),dv(),cv))};djb(400,9,ype);_.Dd=function LAb(a){return Snb(this,a,(dv(),dv(),cv))};djb(952,429,Ope);_.Gf=function eEb(a,b,c){b-=Ao($doc);c-=Bo($doc);Dvb(a,b,c)};djb(761,35,zFe);_.Vg=function Kdd(){return false};_.Yg=function Ldd(){return !this.U&&(this.U=_Mb(this)),AB(AB(this.U,6),210)};_.Fh=function Mdd(){EB(this.$g(),54)&&AB(this.$g(),54).Dd(this)};_.th=function Ndd(a){l0b(this,a);if(a.ji(pre)){this.Ml();(!this.U&&(this.U=_Mb(this)),AB(AB(this.U,6),210)).c&&((!this.U&&(this.U=_Mb(this)),AB(AB(this.U,6),210)).v==null||gWd('',(!this.U&&(this.U=_Mb(this)),AB(AB(this.U,6),210)).v))&&this.Nl((!this.U&&(this.U=_Mb(this)),AB(AB(this.U,6),210)).a)}if(a.ji(Hre)||a.ji($ye)||a.ji(CLe)){this.Nl(Idd(this));(!this.U&&(this.U=_Mb(this)),AB(AB(this.U,6),210)).c&&((!this.U&&(this.U=_Mb(this)),AB(AB(this.U,6),210)).v==null||!(!this.U&&(this.U=_Mb(this)),AB(AB(this.U,6),210)).v.length)&&!(!this.U&&(this.U=_Mb(this)),AB(AB(this.U,6),210)).J.length?this.$g().ye(ZLe):this.$g().De(ZLe)}};var q3=vUd(GLe,'AbstractColorPickerConnector',761);djb(210,6,{6:1,40:1,210:1,3:1},KOd);_.a=null;_.b=false;_.c=false;var Qab=vUd(YHe,'ColorPickerState',210);djb(2349,9,eKe);_.Dd=function see(a){return Rnb(this,a,(dv(),dv(),cv))};djb(325,9,hKe);_.Dd=function Dee(a){return Rnb(this,a,(dv(),dv(),cv))};uje(Xh)(9);\n//# sourceURL=org.vaadin.grid.enhancements.demo.DemoWidgetSet-9.js\n")