//Include this file as the first included file in your application .c or .cpp to enable memory leak detection
//Call SRS_ENABLE_AUTO_MEM_LEAK_DETECTION at the beginning of your application

#ifndef _SRS_DEBUG_API_H_
#define _SRS_DEBUG_API_H_

#include "srs_platdefs.h"

#if SRS_PC
#define _CRTDBG_MAP_ALLOC
#include <stdlib.h>
#include <crtdbg.h>

#ifdef _DEBUG   
	#ifndef DBG_NEW
		#define DBG_NEW new ( _NORMAL_BLOCK , __FILE__ , __LINE__ )
		#define new DBG_NEW
	#endif
#endif  // _DEBUG
	#define SRS_ENABLE_AUTO_MEM_LEAK_DETECTION()	_CrtSetDbgFlag ( _CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF )
#else
	#define SRS_ENABLE_AUTO_MEM_LEAK_DETECTION() 
#endif //SRS_PC


#endif //_SRS_DEBUG_API_H
