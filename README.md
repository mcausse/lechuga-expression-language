# mel

Model | Expression | Result
--- | --- | ---
 |  | 1+2*3+4 | 11.0
 |  | 1 eq 1 and (1+1 eq 2 or false) | true
 |  | 1 eq 1 and (1+2 eq 2 or false) | false
 |  | null eq null | true
 |  | 'jou'=='juas' || 'a' == 'a' | true
 |  | not false and not(not true) | true
 |  | null | null
 |  | 'jou' | jou
 |  | long (10/3) | 3
 |  | long 10.9 / long 3.27 | 3
 |  | 10%3==1 | true
 |  | typeof short 3 | Short
 |  | typeof int 3 | Integer
 |  | typeof 3 | Double
 |  | typeof 3.4 | Double
 |  | typeof 'jou' | String
 |  | typeof true | Boolean
 |  | typeof false | Boolean
 |  | typeof null | null
 |  |  | null
 | {name=mhc} | name | mhc
 | {name=[mhc, mem], i=1} | name[1] | mem
 | {name=[mhc, mem], i=1} | name[i] | mem
 | {name=[mhc, mem], i=1} | 'alo '+name[i/2]+'!' | alo mhc!
 | {name=mhc} | name.class.simpleName.trim.toUpperCase | STRING
 | {age=32} | age>=32&&age<=45 | true
 | {alive=true} | alive&&true | true
 |  | long 1.2 | 1
 |  | int 1 | 1
 |  | int(1+1) | 2
 |  | (int 1)+1 | 2.0
 |  | (int 1)+int 1 | 2
 |  | (int(3.14159)) | 3
 |  | (int(3.14159*100)) | 314
 |  | -1+-1 | -2.0
 |  | 1---1 | 0.0
 |  | -(-(-1)) | -1.0
 |  | ---1 | -1.0
 |  | null eq null | true
 |  | null eq null && true || false==true | true
 |  | 'jou' eq 'jou' | true
 |  | 'jou ' eq 'jou' | false
 |  | 'jou' | jou
 |  | 'jou'+'juas' | joujuas
 |  | short 2 + short 3 | 5
 |  | int 2 + int 3 | 5
 |  | long 2 + long 3 | 5
 |  | float 2 + float 3 | 5.0
 |  | double 2 + double 3 | 5.0
 |  | string int long 3.14159 | 3
 |  | null?'ok':'ko' | ko
 |  | true?'ok':'ko' | ok
 |  | false?'ok':'ko' | ko
 |  | 1?'ok':'ko' | ok
 |  | 0?'ok':'ko' | ko
 |  | 'a'?'ok':'ko' | ok
 |  | ''?'ok':'ko' | ko
 |  | 3+3==3*2?'is'+' '+'ok':null | is ok
 |  | 3%3!=0?true:null | null
 |  | 3->intValue() | 3
 |  | 3->floatValue() | 3.0
 |  | 'abcdefg'->substring(int 2, int 5) | cde
 |  | 1+3*2+2*3+1 | 14.0
 | {c3=true, c1=true, c2=true} | long(c1 ? (c2?1:2) : (c3?3:4)) | 1
 | {c3=true, c1=true, c2=false} | long(c1 ? (c2?1:2) : (c3?3:4)) | 2
 | {c3=true, c1=false, c2=false} | long(c1 ? (c2?1:2) : (c3?3:4)) | 3
 | {c3=false, c1=false, c2=false} | long(c1 ? (c2?1:2) : (c3?3:4)) | 4
 | {a={b=jou!}} | a['b'] | jou!
 | {a={b=jou!}} | a.b | jou!
 | {a={b=jou!}} | keys a | [b]
 | {a=[[Ljava.lang.Integer;@1a6c5a9e} | a[1][0] | 3
 | {a=[[Ljava.lang.Integer;@1a6c5a9e} | keys a | [0, 1]
