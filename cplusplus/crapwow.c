#include <stdint.h>
typedef uint64_t u64;
typedef uint32_t u32;
typedef uint8_t u8;

#define __i686__
inline u32 CrapWow( const u8 *key, u32 len, u32 seed ) {
#if  ( defined(__i686__) )
	// esi = k, ebx = h
	u32 hash;
	asm(
		"leal 0x5052acdb(%%ecx,%%esi), %%esi\n"
		"movl %%ecx, %%ebx\n"
		"cmpl $8, %%ecx\n"
		"jb DW%=\n"
	"QW%=:\n"
		"movl $0x5052acdb, %%eax\n"
		"mull (%%edi)\n"
		"addl $-8, %%ecx\n"
		"xorl %%eax, %%ebx\n"
		"xorl %%edx, %%esi\n"
		"movl $0x57559429, %%eax\n"
		"mull 4(%%edi)\n"
		"xorl %%eax, %%esi\n"
		"xorl %%edx, %%ebx\n"
		"addl $8, %%edi\n"
		"cmpl $8, %%ecx\n"
		"jae QW%=\n"
	"DW%=:\n"
		"cmpl $4, %%ecx\n"
		"jb B%=\n"
		"movl $0x5052acdb, %%eax\n"
		"mull (%%edi)\n"
		"addl $4, %%edi\n"
		"xorl %%eax, %%ebx\n"
		"addl $-4, %%ecx\n"
		"xorl %%edx, %%esi\n"
	"B%=:\n"
		"testl %%ecx, %%ecx\n"
		"jz F%=\n"
		"shll $3, %%ecx\n"
		"movl $1, %%edx\n"
		"movl $0x57559429, %%eax\n"
		"shll %%cl, %%edx\n"
		"addl $-1, %%edx\n"
		"andl (%%edi), %%edx\n"
		"mull %%edx\n"
		"xorl %%eax, %%esi\n"
		"xorl %%edx, %%ebx\n"
	"F%=:\n"
		"leal 0x5052acdb(%%esi), %%edx\n"
		"xorl %%ebx, %%edx\n"
		"movl $0x5052acdb, %%eax\n"
		"mull %%edx\n"
		"xorl %%ebx, %%eax\n"
		"xorl %%edx, %%esi\n"
		"xorl %%esi, %%eax\n"
		: "=a"(hash), "=c"(len), "=S"(len), "=D"(key)
		: "c"(len), "S"(seed), "D"(key)
		: "%ebx", "%edx", "cc" 
	);
	return hash;
#else
	#define cwfold( a, b, lo, hi ) { p = (u32)(a) * (u64)(b); lo ^= (u32)p; hi ^= (u32)(p >> 32); }
	#define cwmixa( in ) { cwfold( in, m, k, h ); }
	#define cwmixb( in ) { cwfold( in, n, h, k ); }
	const u32 m = 0x57559429, n = 0x5052acdb, *key4 = (const u32 *)key;
	u32 h = len, k = len + seed + n;
	u64 p;
	while ( len >= 8 ) { cwmixb(key4[0]) cwmixa(key4[1]) key4 += 2; len -= 8; }
	if ( len >= 4 ) { cwmixb(key4[0]) key4 += 1; len -= 4; }
	if ( len ) { cwmixa( key4[0] & ( ( 1 << ( len * 8 ) ) - 1 ) ) }
	cwmixb( h ^ (k + n) )
	return k ^ h;
#endif
}



int main(int l, char** stat){
    
    //u8 key[4] = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
    u32 len = 4;
    u8* key = malloc(len);
    u32 i = 0;   
    u32 seed = 0xabcdef11;
    u8 shift = 14;
    u32* r = malloc(1<<shift );
    
    for(i = 0;i<(1<<shift);i++)r[i]=0;
    
    for(i = 0;i<(1<<shift);i++){
        key[3]=i&0xff;
        key[2]=(i&0xff00)>>8;
        key[1]=(i&0xff0000)>>16;
        key[0]=(i&0xff000000)>>24;
        r[CrapWow( key, len, seed )&(1<<shift)]++;
        printf("%x%x\n",key[0],key[1]);
    //printf("%x\n",);
    }

    printf("hey\n");
    u32 max = 0;
    u32 argmax = 0;
    for(i = 0;i<(1<<shift);i++){
        if(r[i]>max){
            max=r[i];
            argmax = i;
        }
    }
    printf("%i,%i\n",max,argmax);
    return 1;
}

