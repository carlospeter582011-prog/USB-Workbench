/* Original MIT decoder guard; no upstream modifications. */
#include <lzma.h>
#define USBWB_LZMA_LIMIT (128ULL*1024*1024)
extern lzma_ret __real_lzma_raw_decoder(lzma_stream *,const lzma_filter *);
extern lzma_ret __real_lzma_stream_decoder(lzma_stream *,uint64_t,uint32_t);
extern lzma_ret __real_lzma_alone_decoder(lzma_stream *,uint64_t);
lzma_ret __wrap_lzma_raw_decoder(lzma_stream *s,const lzma_filter *f){uint64_t n=lzma_raw_decoder_memusage(f);if(n==UINT64_MAX)return LZMA_OPTIONS_ERROR;if(n>USBWB_LZMA_LIMIT)return LZMA_MEMLIMIT_ERROR;return __real_lzma_raw_decoder(s,f);}
lzma_ret __wrap_lzma_stream_decoder(lzma_stream *s,uint64_t n,uint32_t flags){return __real_lzma_stream_decoder(s,n<USBWB_LZMA_LIMIT?n:USBWB_LZMA_LIMIT,flags);}
lzma_ret __wrap_lzma_alone_decoder(lzma_stream *s,uint64_t n){return __real_lzma_alone_decoder(s,n<USBWB_LZMA_LIMIT?n:USBWB_LZMA_LIMIT);}
