# mel

```
1+2*3+4
=> 11.0

1 eq 1 and (1+1 eq 2 or false)
=> true

1 eq 1 and (1+2 eq 2 or false)
=> false

null eq null
=> true

'jou'=='juas' || 'a' == 'a'
=> true

not false and not(not true)
=> true

null
=> null

'jou'
=> jou

:jou
=> jou

{name=mhc}
name
=> mhc

{name=[mhc, mem]}
name[1]
=> mem

{name=mhc}
name.class.simpleName.trim.toUpperCase
=> STRING

{age=32}
age>=32&&age<=45
=> true

{alive=true}
alive&&true
=> true

long 1.2
=> 1

int 1
=> 1

int(1+1)
=> 2

(int 1)+1
=> 2.0

(int 3.14159)
=> 3

-1+-1
=> -2.0

1---1
=> 0.0

-(-(-1))
=> -1.0

---1
=> -1.0

null eq null
=> true

null eq null && true || false==true
=> true

:jou eq 'jou'
=> true

:jou
=> jou

:jou+:juas
=> joujuas

short 2 + short 3
=> 5

int 2 + int 3
=> 5

long 2 + long 3
=> 5

float 2 + float 3
=> 5.0

double 2 + double 3
=> 5.0

string int long 3.14159
=> 3

{a={b=jou!}}
a['b']
=> jou!

{a={b=jou!}}
a.b
=> jou!

{a=[[Ljava.lang.Integer;@433c675d}
a[1][0]
=> 3


```
