#ifndef RFB_H
#define RFB_H

/*
 *  Copyright (C) 2005 Rohit Kumar <rokumar@novell.com>,
 *                     Johannes E. Schindelin <johannes.schindelin@gmx.de>
 *  Copyright (C) 2002 RealVNC Ltd.
 *  OSXvnc Copyright (C) 2001 Dan McGuirk <mcguirk@incompleteness.net>.
 *  Original Xvnc code Copyright (C) 1999 AT&T Laboratories Cambridge.  
 *  All Rights Reserved.
 *
 *  This is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this software; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 *  USA.
 */

#if(defined __cplusplus)
extern "C"
{
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "rfbproto.h"

#if defined(ANDROID) || defined(LIBVNCSERVER_HAVE_ANDROID)
#include <arpa/inet.h>
#include <sys/select.h>
#endif

#ifdef LIBVNCSERVER_HAVE_SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef __MINGW32__
#undef SOCKET
#include <winsock2.h>
#ifdef LIBVNCSERVER_HAVE_WS2TCPIP_H
#undef socklen_t
#include <ws2tcpip.h>
#endif
#endif

#ifdef LIBVNCSERVER_HAVE_LIBPTHREAD
#include <pthread.h>
#if 0 /* debugging */
#define LOCK(mutex) (rfbLog("%s:%d LOCK(%s,0x%x)\n",__FILE__,__LINE__,#mutex,&(mutex)), pthread_mutex_lock(&(mutex)))
#define UNLOCK(mutex) (rfbLog("%s:%d UNLOCK(%s,0x%x)\n",__FILE__,__LINE__,#mutex,&(mutex)), pthread_mutex_unlock(&(mutex)))
#define MUTEX(mutex) pthread_mutex_t (mutex)
#define INIT_MUTEX(mutex) (rfbLog("%s:%d INIT_MUTEX(%s,0x%x)\n",__FILE__,__LINE__,#mutex,&(mutex)), pthread_mutex_init(&(mutex),NULL))
#define TINI_MUTEX(mutex) (rfbLog("%s:%d TINI_MUTEX(%s)\n",__FILE__,__LINE__,#mutex), pthread_mutex_destroy(&(mutex)))
#define TSIGNAL(cond) (rfbLog("%s:%d TSIGNAL(%s)\n",__FILE__,__LINE__,#cond), pthread_cond_signal(&(cond)))
#define WAIT(cond,mutex) (rfbLog("%s:%d WAIT(%s,%s)\n",__FILE__,__LINE__,#cond,#mutex), pthread_cond_wait(&(cond),&(mutex)))
#define COND(cond) pthread_cond_t (cond)
#define INIT_COND(cond) (rfbLog("%s:%d INIT_COND(%s)\n",__FILE__,__LINE__,#cond), pthread_cond_init(&(cond),NULL))
#define TINI_COND(cond) (rfbLog("%s:%d TINI_COND(%s)\n",__FILE__,__LINE__,#cond), pthread_cond_destroy(&(cond)))
#define IF_PTHREADS(x) x
#else
#if !NONETWORK
#define LOCK(mutex) pthread_mutex_lock(&(mutex));
#define UNLOCK(mutex) pthread_mutex_unlock(&(mutex));
#endif
#define MUTEX(mutex) pthread_mutex_t (mutex)
#define INIT_MUTEX(mutex) pthread_mutex_init(&(mutex),NULL)
#define TINI_MUTEX(mutex) pthread_mutex_destroy(&(mutex))
#define TSIGNAL(cond) pthread_cond_signal(&(cond))
#define WAIT(cond,mutex) pthread_cond_wait(&(cond),&(mutex))
#define COND(cond) pthread_cond_t (cond)
#define INIT_COND(cond) pthread_cond_init(&(cond),NULL)
#define TINI_COND(cond) pthread_cond_destroy(&(cond))
#define IF_PTHREADS(x) x
#endif
#else
#define LOCK(mutex)
#define UNLOCK(mutex)
#define MUTEX(mutex)
#define INIT_MUTEX(mutex)
#define TINI_MUTEX(mutex)
#define TSIGNAL(cond)
#define WAIT(cond,mutex) this_is_unsupported
#define COND(cond)
#define INIT_COND(cond)
#define TINI_COND(cond)
#define IF_PTHREADS(x)
#endif

/* end of stuff for autoconf */

/* if you use pthreads, but don't define LIBVNCSERVER_HAVE_LIBPTHREAD, the structs
   get all mixed up. So this gives a linker error reminding you to compile
   the library and your application (at least the parts including rfb.h)
   with the same support for pthreads. */
#ifdef LIBVNCSERVER_HAVE_LIBPTHREAD
#ifdef LIBVNCSERVER_HAVE_LIBZ
#define rfbInitServer rfbInitServerWithPthreadsAndZRLE
#else
#define rfbInitServer rfbInitServerWithPthreadsButWithoutZRLE
#endif
#else
#ifdef LIBVNCSERVER_HAVE_LIBZ
#define rfbInitServer rfbInitServerWithoutPthreadsButWithZRLE
#else
#define rfbInitServer rfbInitServerWithoutPthreadsAndZRLE
#endif
#endif

struct _rfbClientRec;
struct _rfbScreenInfo;
struct rfbCursor;

enum rfbNewClientAction {
        RFB_CLIENT_ACCEPT,
        RFB_CLIENT_ON_HOLD,
        RFB_CLIENT_REFUSE
};

enum rfbSocketState {
        RFB_SOCKET_INIT,
        RFB_SOCKET_READY,
        RFB_SOCKET_SHUTDOWN
};

typedef void (*rfbKbdAddEventProcPtr) (rfbBool down, rfbKeySym keySym, struct _rfbClientRec* cl);
typedef void (*rfbKbdReleaseAllKeysProcPtr) (struct _rfbClientRec* cl);
typedef void (*rfbPtrAddEventProcPtr) (int buttonMask, int x, int y, struct _rfbClientRec* cl);
typedef void (*rfbSetXCutTextProcPtr) (char* str,int len, struct _rfbClientRec* cl);
typedef struct rfbCursor* (*rfbGetCursorProcPtr) (struct _rfbClientRec* pScreen);
typedef rfbBool (*rfbSetTranslateFunctionProcPtr)(struct _rfbClientRec* cl);
typedef rfbBool (*rfbPasswordCheckProcPtr)(struct _rfbClientRec* cl,const char* encryptedPassWord,int len);
typedef enum rfbNewClientAction (*rfbNewClientHookPtr)(struct _rfbClientRec* cl);
typedef void (*rfbDisplayHookPtr)(struct _rfbClientRec* cl);
typedef void (*rfbDisplayFinishedHookPtr)(struct _rfbClientRec* cl, int result);
typedef int  (*rfbGetKeyboardLedStateHookPtr)(struct _rfbScreenInfo* screen);
typedef rfbBool (*rfbXvpHookPtr)(struct _rfbClientRec* cl, uint8_t, uint8_t);
typedef void (*rfbSetSingleWindowProcPtr) (struct _rfbClientRec* cl, int x, int y);
typedef void (*rfbSetServerInputProcPtr) (struct _rfbClientRec* cl, int status);
typedef int  (*rfbFileTransferPermitted) (struct _rfbClientRec* cl);
typedef void (*rfbSetTextChat) (struct _rfbClientRec* cl, int length, char *string);

typedef struct {
  uint32_t count;
  rfbBool is16; 
  union {
    uint8_t* bytes;
    uint16_t* shorts;
  } data; 
} rfbColourMap;

typedef struct _rfbSecurity {
        uint8_t type;
        void (*handler)(struct _rfbClientRec* cl);
        struct _rfbSecurity* next;
} rfbSecurityHandler;

typedef struct _rfbProtocolExtension {
        rfbBool (*newClient)(struct _rfbClientRec* client, void** data);
        rfbBool (*init)(struct _rfbClientRec* client, void* data);
        int *pseudoEncodings;
        rfbBool (*enablePseudoEncoding)(struct _rfbClientRec* client,
                        void** data, int encodingNumber);
        rfbBool (*handleMessage)(struct _rfbClientRec* client,
                                void* data,
                                const rfbClientToServerMsg* message);
        void (*close)(struct _rfbClientRec* client, void* data);
        void (*usage)(void);
        int (*processArgument)(int argc, char *argv[]);
        struct _rfbProtocolExtension* next;
} rfbProtocolExtension;

typedef struct _rfbExtensionData {
        rfbProtocolExtension* extension;
        void* data;
        struct _rfbExtensionData* next;
} rfbExtensionData;

typedef struct _rfbScreenInfo
{
    struct _rfbScreenInfo *scaledScreenNext;
    int scaledScreenRefCount;

    int width;
    int paddedWidthInBytes;
    int height;
    int depth;
    int bitsPerPixel;
    int sizeInBytes;

    rfbPixel blackPixel;
    rfbPixel whitePixel;

    void* screenData;

    /* additions by libvncserver */

    rfbPixelFormat serverFormat;
    rfbColourMap colourMap; 
    const char* desktopName;
    char thisHost[255];

    rfbBool autoPort;
    int port;
    SOCKET listenSock;
    int maxSock;
    int maxFd;
#ifdef __MINGW32__
    struct fd_set allFds;
#else
    fd_set allFds;
#endif

    enum rfbSocketState socketState;
    SOCKET inetdSock;
    rfbBool inetdInitDone;

    int udpPort;
    SOCKET udpSock;
    struct _rfbClientRec* udpClient;
    rfbBool udpSockConnected;
    struct sockaddr_in udpRemoteAddr;

    int maxClientWait;

    /* http stuff */
    rfbBool httpInitDone;
    rfbBool httpEnableProxyConnect;
    int httpPort;
    char* httpDir;
    SOCKET httpListenSock;
    SOCKET httpSock;

    rfbPasswordCheckProcPtr passwordCheck;
    void* authPasswdData;
    int authPasswdFirstViewOnly;

    int maxRectsPerUpdate;
    int deferUpdateTime;
#ifdef TODELETE
    char* screen;
#endif
    rfbBool alwaysShared;
    rfbBool neverShared;
    rfbBool dontDisconnect;
    struct _rfbClientRec* clientHead;
    struct _rfbClientRec* pointerClient;  
    /* cursor */
    int cursorX, cursorY,underCursorBufferLen;
    char* underCursorBuffer;
    rfbBool dontConvertRichCursorToXCursor;
    struct rfbCursor* cursor;

    char* frameBuffer;
    rfbKbdAddEventProcPtr kbdAddEvent;
    rfbKbdReleaseAllKeysProcPtr kbdReleaseAllKeys;
    rfbPtrAddEventProcPtr ptrAddEvent;
    rfbSetXCutTextProcPtr setXCutText;
    rfbGetCursorProcPtr getCursorPtr;
    rfbSetTranslateFunctionProcPtr setTranslateFunction;
    rfbSetSingleWindowProcPtr setSingleWindow;
    rfbSetServerInputProcPtr  setServerInput;
    rfbFileTransferPermitted  getFileTransferPermission;
    rfbSetTextChat            setTextChat;

    rfbNewClientHookPtr newClientHook;
    rfbDisplayHookPtr displayHook;

    rfbGetKeyboardLedStateHookPtr getKeyboardLedStateHook;

#ifdef LIBVNCSERVER_HAVE_LIBPTHREAD
    MUTEX(cursorMutex);
    rfbBool backgroundLoop;
#endif

    rfbBool ignoreSIGPIPE;

    int progressiveSliceHeight;

    in_addr_t listenInterface;
    int deferPtrUpdateTime;

    rfbBool handleEventsEagerly;

    char *versionString;

    int protocolMajorVersion;
    int protocolMinorVersion;

    rfbBool permitFileTransfer;

    rfbDisplayFinishedHookPtr displayFinishedHook;
    rfbXvpHookPtr xvpHook;
#ifdef LIBVNCSERVER_WITH_WEBSOCKETS
    char *sslkeyfile;
    char *sslcertfile;
#endif
    int ipv6port; 
    char* listen6Interface;
    /* We have an additional IPv6 listen socket since there are systems that
       don't support dual binding sockets under *any* circumstances, for
       instance OpenBSD */
    SOCKET listen6Sock;
    int http6Port;
    SOCKET httpListen6Sock;
} rfbScreenInfo, *rfbScreenInfoPtr;


typedef void (*rfbTranslateFnType)(char *table, rfbPixelFormat *in,
                                   rfbPixelFormat *out,
                                   char *iptr, char *optr,
                                   int bytesBetweenInputLines,
                                   int width, int height);


/* region stuff */

struct sraRegion;
typedef struct sraRegion* sraRegionPtr;

/*
 * Per-client structure.
 */

typedef void (*ClientGoneHookPtr)(struct _rfbClientRec* cl);

typedef struct _rfbFileTransferData {
  int fd;
  int compressionEnabled;
  int fileSize;
  int numPackets;
  int receiving;
  int sending;
} rfbFileTransferData;


typedef struct _rfbStatList {
    uint32_t type;
    uint32_t sentCount;
    uint32_t bytesSent;
    uint32_t bytesSentIfRaw;
    uint32_t rcvdCount;
    uint32_t bytesRcvd;
    uint32_t bytesRcvdIfRaw;
    struct _rfbStatList *Next;
} rfbStatList;

typedef struct _rfbSslCtx rfbSslCtx;
typedef struct _wsCtx wsCtx;

typedef struct _rfbClientRec {

    rfbScreenInfoPtr screen;

     rfbScreenInfoPtr scaledScreen;
     rfbBool PalmVNC;


    void* clientData;
    ClientGoneHookPtr clientGoneHook;

    SOCKET sock;
    char *host;

    /* RFB protocol minor version number */
    int protocolMajorVersion;
    int protocolMinorVersion;

#ifdef LIBVNCSERVER_HAVE_LIBPTHREAD
    pthread_t client_thread;
#endif

    /* Note that the RFB_INITIALISATION_SHARED state is provided to support
       clients that under some circumstances do not send a ClientInit message.
       In particular the Mac OS X built-in VNC client (with protocolMinorVersion
       == 889) is one of those.  However, it only requires this support under
       special circumstances that can only be determined during the initial
       authentication.  If the right conditions are met this state will be
       set (see the auth.c file) when rfbProcessClientInitMessage is called.

       If the state is RFB_INITIALISATION_SHARED we should not expect to recieve
       any ClientInit message, but instead should proceed to the next stage
       of initialisation as though an implicit ClientInit message was received
       with a shared-flag of true.  (There is currently no corresponding
       RFB_INITIALISATION_NOTSHARED state to represent an implicit ClientInit
       message with a shared-flag of false because no known existing client
       requires such support at this time.)

       Note that software using LibVNCServer to provide a VNC server will only
       ever have a chance to see the state field set to
       RFB_INITIALISATION_SHARED if the software is multi-threaded and manages
       to examine the state field during the extremely brief window after the
       'None' authentication type selection has been received from the built-in
       OS X VNC client and before the rfbProcessClientInitMessage function is
       called -- control cannot return to the caller during this brief window
       while the state field is set to RFB_INITIALISATION_SHARED. */

    enum {
        RFB_PROTOCOL_VERSION,   
        RFB_SECURITY_TYPE,      
        RFB_AUTHENTICATION,     
        RFB_INITIALISATION,     
        RFB_NORMAL,             
        /* Ephemeral internal-use states that will never be seen by software
         * using LibVNCServer to provide services: */

        RFB_INITIALISATION_SHARED 
    } state;

    rfbBool reverseConnection;
    rfbBool onHold;
    rfbBool readyForSetColourMapEntries;
    rfbBool useCopyRect;
    int preferredEncoding;
    int correMaxWidth, correMaxHeight;

    rfbBool viewOnly;

    /* The following member is only used during VNC authentication */
    uint8_t authChallenge[CHALLENGESIZE];

    /* The following members represent the update needed to get the client's
       framebuffer from its present state to the current state of our
       framebuffer.

       If the client does not accept CopyRect encoding then the update is
       simply represented as the region of the screen which has been modified
       (modifiedRegion).

       If the client does accept CopyRect encoding, then the update consists of
       two parts.  First we have a single copy from one region of the screen to
       another (the destination of the copy is copyRegion), and second we have
       the region of the screen which has been modified in some other way
       (modifiedRegion).

       Although the copy is of a single region, this region may have many
       rectangles.  When sending an update, the copyRegion is always sent
       before the modifiedRegion.  This is because the modifiedRegion may
       overlap parts of the screen which are in the source of the copy.

       In fact during normal processing, the modifiedRegion may even overlap
       the destination copyRegion.  Just before an update is sent we remove
       from the copyRegion anything in the modifiedRegion. */

    sraRegionPtr copyRegion;    
    int copyDX, copyDY;         
    sraRegionPtr modifiedRegion;

    sraRegionPtr requestedRegion;

      struct timeval startDeferring;
      struct timeval startPtrDeferring;
      int lastPtrX;
      int lastPtrY;
      int lastPtrButtons;

    rfbTranslateFnType translateFn;
    char *translateLookupTable;
    rfbPixelFormat format;

#define UPDATE_BUF_SIZE 30000

    char updateBuf[UPDATE_BUF_SIZE];
    int ublen;

    /* statistics */
    struct _rfbStatList *statEncList;
    struct _rfbStatList *statMsgList;
    int rawBytesEquivalent;
    int bytesSent;

#ifdef LIBVNCSERVER_HAVE_LIBZ
    /* zlib encoding -- necessary compression state info per client */

    struct z_stream_s compStream;
    rfbBool compStreamInited;
    uint32_t zlibCompressLevel;
#endif
#if defined(LIBVNCSERVER_HAVE_LIBZ) || defined(LIBVNCSERVER_HAVE_LIBPNG)

    int tightQualityLevel;

#ifdef LIBVNCSERVER_HAVE_LIBJPEG
    /* tight encoding -- preserve zlib streams' state for each client */
    z_stream zsStruct[4];
    rfbBool zsActive[4];
    int zsLevel[4];
    int tightCompressLevel;
#endif
#endif

    /* Ultra Encoding support */
    rfbBool compStreamInitedLZO;
    char *lzoWrkMem;

    rfbFileTransferData fileTransfer;

    int     lastKeyboardLedState;     
    rfbBool enableSupportedMessages;  
    rfbBool enableSupportedEncodings; 
    rfbBool enableServerIdentity;     
    rfbBool enableKeyboardLedState;   
    rfbBool enableLastRectEncoding;   
    rfbBool enableCursorShapeUpdates; 
    rfbBool enableCursorPosUpdates;   
    rfbBool useRichCursorEncoding;    
    rfbBool cursorWasChanged;         
    rfbBool cursorWasMoved;           
    int cursorX,cursorY;              
    rfbBool useNewFBSize;             
    rfbBool newFBSizePending;         
    struct _rfbClientRec *prev;
    struct _rfbClientRec *next;

#ifdef LIBVNCSERVER_HAVE_LIBPTHREAD

    int refCount;
    MUTEX(refCountMutex);
    COND(deleteCond);

    MUTEX(outputMutex);
    MUTEX(updateMutex);
    COND(updateCond);
#endif

#ifdef LIBVNCSERVER_HAVE_LIBZ
    void* zrleData;
    int zywrleLevel;
    int zywrleBuf[rfbZRLETileWidth * rfbZRLETileHeight];
#endif

    int progressiveSliceY;

    rfbExtensionData* extensions;

    char *zrleBeforeBuf;
    void *paletteHelper;

#ifdef LIBVNCSERVER_HAVE_LIBPTHREAD
#define LIBVNCSERVER_SEND_MUTEX
    MUTEX(sendMutex);
#endif

  /* buffers to hold pixel data before and after encoding.
     per-client for thread safety */
  char *beforeEncBuf;
  int beforeEncBufSize;
  char *afterEncBuf;
  int afterEncBufSize;
  int afterEncBufLen;
#if defined(LIBVNCSERVER_HAVE_LIBZ) || defined(LIBVNCSERVER_HAVE_LIBPNG)
    uint32_t tightEncoding;  /* rfbEncodingTight or rfbEncodingTightPng */
#ifdef LIBVNCSERVER_HAVE_LIBJPEG
    /* TurboVNC Encoding support (extends TightVNC) */
    int turboSubsampLevel;
    int turboQualityLevel;  // 1-100 scale
#endif
#endif

#ifdef LIBVNCSERVER_WITH_WEBSOCKETS
    rfbSslCtx *sslctx;
    wsCtx     *wsctx;
    char *wspath;                          /* Requests path component */
#endif
} rfbClientRec, *rfbClientPtr;

#define FB_UPDATE_PENDING(cl)                                              \
     (((cl)->enableCursorShapeUpdates && (cl)->cursorWasChanged) ||        \
     (((cl)->enableCursorShapeUpdates == FALSE &&                          \
       ((cl)->cursorX != (cl)->screen->cursorX ||                          \
        (cl)->cursorY != (cl)->screen->cursorY))) ||                       \
     ((cl)->useNewFBSize && (cl)->newFBSizePending) ||                     \
     ((cl)->enableCursorPosUpdates && (cl)->cursorWasMoved) ||             \
     !sraRgnEmpty((cl)->copyRegion) || !sraRgnEmpty((cl)->modifiedRegion))

/*
 * Macros for endian swapping.
 */

#define Swap16(s) ((((s) & 0xff) << 8) | (((s) >> 8) & 0xff))

#define Swap24(l) ((((l) & 0xff) << 16) | (((l) >> 16) & 0xff) | \
                   (((l) & 0x00ff00)))

#define Swap32(l) (((l) >> 24) | \
                   (((l) & 0x00ff0000) >> 8)  | \
                   (((l) & 0x0000ff00) << 8)  | \
                   ((l) << 24))


extern char rfbEndianTest;

#define Swap16IfLE(s) (rfbEndianTest ? Swap16(s) : (s))
#define Swap24IfLE(l) (rfbEndianTest ? Swap24(l) : (l))
#define Swap32IfLE(l) (rfbEndianTest ? Swap32(l) : (l))

/* UltraVNC uses some windows structures unmodified, so the viewer expects LittleEndian Data */
#define Swap16IfBE(s) (rfbEndianTest ? (s) : Swap16(s))
#define Swap24IfBE(l) (rfbEndianTest ? (l) : Swap24(l))
#define Swap32IfBE(l) (rfbEndianTest ? (l) : Swap32(l))

/* sockets.c */

extern int rfbMaxClientWait;

extern void rfbInitSockets(rfbScreenInfoPtr rfbScreen);
extern void rfbShutdownSockets(rfbScreenInfoPtr rfbScreen);
extern void rfbDisconnectUDPSock(rfbScreenInfoPtr rfbScreen);
extern void rfbCloseClient(rfbClientPtr cl);
extern int rfbReadExact(rfbClientPtr cl, char *buf, int len);
extern int rfbReadExactTimeout(rfbClientPtr cl, char *buf, int len,int timeout);
extern int rfbPeekExactTimeout(rfbClientPtr cl, char *buf, int len,int timeout);
extern int rfbWriteExact(rfbClientPtr cl, const char *buf, int len);
extern int rfbCheckFds(rfbScreenInfoPtr rfbScreen,long usec);
extern int rfbConnect(rfbScreenInfoPtr rfbScreen, char* host, int port);
extern int rfbConnectToTcpAddr(char* host, int port);
extern int rfbListenOnTCPPort(int port, in_addr_t iface);
extern int rfbListenOnTCP6Port(int port, const char* iface);
extern int rfbListenOnUDPPort(int port, in_addr_t iface);
extern int rfbStringToAddr(char* string,in_addr_t* addr);
extern rfbBool rfbSetNonBlocking(int sock);

#ifdef LIBVNCSERVER_WITH_WEBSOCKETS
/* websockets.c */

extern rfbBool webSocketsCheck(rfbClientPtr cl);
extern rfbBool webSocketCheckDisconnect(rfbClientPtr cl);
extern int webSocketsEncode(rfbClientPtr cl, const char *src, int len, char **dst);
extern int webSocketsDecode(rfbClientPtr cl, char *dst, int len);
#endif

/* rfbserver.c */

/* Routines to iterate over the client list in a thread-safe way.
   Only a single iterator can be in use at a time process-wide. */
typedef struct rfbClientIterator *rfbClientIteratorPtr;

extern void rfbClientListInit(rfbScreenInfoPtr rfbScreen);
extern rfbClientIteratorPtr rfbGetClientIterator(rfbScreenInfoPtr rfbScreen);
extern rfbClientPtr rfbClientIteratorNext(rfbClientIteratorPtr iterator);
extern void rfbReleaseClientIterator(rfbClientIteratorPtr iterator);
extern void rfbIncrClientRef(rfbClientPtr cl);
extern void rfbDecrClientRef(rfbClientPtr cl);

extern void rfbNewClientConnection(rfbScreenInfoPtr rfbScreen,int sock);
extern rfbClientPtr rfbNewClient(rfbScreenInfoPtr rfbScreen,int sock);
extern rfbClientPtr rfbNewUDPClient(rfbScreenInfoPtr rfbScreen);
extern rfbClientPtr rfbReverseConnection(rfbScreenInfoPtr rfbScreen,char *host, int port);
extern void rfbClientConnectionGone(rfbClientPtr cl);
extern void rfbProcessClientMessage(rfbClientPtr cl);
extern void rfbClientConnFailed(rfbClientPtr cl, const char *reason);
extern void rfbNewUDPConnection(rfbScreenInfoPtr rfbScreen,int sock);
extern void rfbProcessUDPInput(rfbScreenInfoPtr rfbScreen);
extern rfbBool rfbSendFramebufferUpdate(rfbClientPtr cl, sraRegionPtr updateRegion);
extern rfbBool rfbSendRectEncodingRaw(rfbClientPtr cl, int x,int y,int w,int h);
extern rfbBool rfbSendUpdateBuf(rfbClientPtr cl);
extern void rfbSendServerCutText(rfbScreenInfoPtr rfbScreen,char *str, int len);
extern rfbBool rfbSendCopyRegion(rfbClientPtr cl,sraRegionPtr reg,int dx,int dy);
extern rfbBool rfbSendLastRectMarker(rfbClientPtr cl);
extern rfbBool rfbSendNewFBSize(rfbClientPtr cl, int w, int h);
extern rfbBool rfbSendSetColourMapEntries(rfbClientPtr cl, int firstColour, int nColours);
extern void rfbSendBell(rfbScreenInfoPtr rfbScreen);

extern char *rfbProcessFileTransferReadBuffer(rfbClientPtr cl, uint32_t length);
extern rfbBool rfbSendFileTransferChunk(rfbClientPtr cl);
extern rfbBool rfbSendDirContent(rfbClientPtr cl, int length, char *buffer);
extern rfbBool rfbSendFileTransferMessage(rfbClientPtr cl, uint8_t contentType, uint8_t contentParam, uint32_t size, uint32_t length, const char *buffer);
extern char *rfbProcessFileTransferReadBuffer(rfbClientPtr cl, uint32_t length);
extern rfbBool rfbProcessFileTransfer(rfbClientPtr cl, uint8_t contentType, uint8_t contentParam, uint32_t size, uint32_t length);

void rfbGotXCutText(rfbScreenInfoPtr rfbScreen, char *str, int len);

/* translate.c */

extern rfbBool rfbEconomicTranslate;

extern void rfbTranslateNone(char *table, rfbPixelFormat *in,
                             rfbPixelFormat *out,
                             char *iptr, char *optr,
                             int bytesBetweenInputLines,
                             int width, int height);
extern rfbBool rfbSetTranslateFunction(rfbClientPtr cl);
extern rfbBool rfbSetClientColourMap(rfbClientPtr cl, int firstColour, int nColours);
extern void rfbSetClientColourMaps(rfbScreenInfoPtr rfbScreen, int firstColour, int nColours);

/* httpd.c */

extern void rfbHttpInitSockets(rfbScreenInfoPtr rfbScreen);
extern void rfbHttpShutdownSockets(rfbScreenInfoPtr rfbScreen);
extern void rfbHttpCheckFds(rfbScreenInfoPtr rfbScreen);



/* auth.c */

extern void rfbAuthNewClient(rfbClientPtr cl);
extern void rfbProcessClientSecurityType(rfbClientPtr cl);
extern void rfbAuthProcessClientMessage(rfbClientPtr cl);
extern void rfbRegisterSecurityHandler(rfbSecurityHandler* handler);
extern void rfbUnregisterSecurityHandler(rfbSecurityHandler* handler);

/* rre.c */

extern rfbBool rfbSendRectEncodingRRE(rfbClientPtr cl, int x,int y,int w,int h);


/* corre.c */

extern rfbBool rfbSendRectEncodingCoRRE(rfbClientPtr cl, int x,int y,int w,int h);


/* hextile.c */

extern rfbBool rfbSendRectEncodingHextile(rfbClientPtr cl, int x, int y, int w,
                                       int h);

/* ultra.c */

/* Set maximum ultra rectangle size in pixels.  Always allow at least
 * two scan lines.
 */
#define ULTRA_MAX_RECT_SIZE (128*256)
#define ULTRA_MAX_SIZE(min) ((( min * 2 ) > ULTRA_MAX_RECT_SIZE ) ? \
                            ( min * 2 ) : ULTRA_MAX_RECT_SIZE )

extern rfbBool rfbSendRectEncodingUltra(rfbClientPtr cl, int x,int y,int w,int h);


#ifdef LIBVNCSERVER_HAVE_LIBZ
/* zlib.c */

#define VNC_ENCODE_ZLIB_MIN_COMP_SIZE (17)

/* Set maximum zlib rectangle size in pixels.  Always allow at least
 * two scan lines.
 */
#define ZLIB_MAX_RECT_SIZE (128*256)
#define ZLIB_MAX_SIZE(min) ((( min * 2 ) > ZLIB_MAX_RECT_SIZE ) ? \
                            ( min * 2 ) : ZLIB_MAX_RECT_SIZE )

extern rfbBool rfbSendRectEncodingZlib(rfbClientPtr cl, int x, int y, int w,
                                    int h);

#ifdef LIBVNCSERVER_HAVE_LIBJPEG
/* tight.c */

#define TIGHT_DEFAULT_COMPRESSION  6
#define TURBO_DEFAULT_SUBSAMP 0

extern rfbBool rfbTightDisableGradient;

extern int rfbNumCodedRectsTight(rfbClientPtr cl, int x,int y,int w,int h);

extern rfbBool rfbSendRectEncodingTight(rfbClientPtr cl, int x,int y,int w,int h);

#if defined(LIBVNCSERVER_HAVE_LIBPNG)
extern rfbBool rfbSendRectEncodingTightPng(rfbClientPtr cl, int x,int y,int w,int h);
#endif

#endif
#endif


/* cursor.c */

typedef struct rfbCursor {
    rfbBool cleanup, cleanupSource, cleanupMask, cleanupRichSource;
    unsigned char *source;                      
    unsigned char *mask;                        
    unsigned short width, height, xhot, yhot;   
    unsigned short foreRed, foreGreen, foreBlue; 
    unsigned short backRed, backGreen, backBlue; 
    unsigned char *richSource; 
    unsigned char *alphaSource; 
    rfbBool alphaPreMultiplied; 
} rfbCursor, *rfbCursorPtr;
extern unsigned char rfbReverseByte[0x100];

extern rfbBool rfbSendCursorShape(rfbClientPtr cl/*, rfbScreenInfoPtr pScreen*/);
extern rfbBool rfbSendCursorPos(rfbClientPtr cl);
extern void rfbConvertLSBCursorBitmapOrMask(int width,int height,unsigned char* bitmap);
extern rfbCursorPtr rfbMakeXCursor(int width,int height,char* cursorString,char* maskString);
extern char* rfbMakeMaskForXCursor(int width,int height,char* cursorString);
extern char* rfbMakeMaskFromAlphaSource(int width,int height,unsigned char* alphaSource);
extern void rfbMakeXCursorFromRichCursor(rfbScreenInfoPtr rfbScreen,rfbCursorPtr cursor);
extern void rfbMakeRichCursorFromXCursor(rfbScreenInfoPtr rfbScreen,rfbCursorPtr cursor);
extern void rfbFreeCursor(rfbCursorPtr cursor);
extern void rfbSetCursor(rfbScreenInfoPtr rfbScreen,rfbCursorPtr c);

extern void rfbDefaultPtrAddEvent(int buttonMask,int x,int y,rfbClientPtr cl);

/* zrle.c */
#ifdef LIBVNCSERVER_HAVE_LIBZ
extern rfbBool rfbSendRectEncodingZRLE(rfbClientPtr cl, int x, int y, int w,int h);
#endif

/* stats.c */

extern void rfbResetStats(rfbClientPtr cl);
extern void rfbPrintStats(rfbClientPtr cl);

/* font.c */

typedef struct rfbFontData {
  unsigned char* data;
  int* metaData;
} rfbFontData,* rfbFontDataPtr;

int rfbDrawChar(rfbScreenInfoPtr rfbScreen,rfbFontDataPtr font,int x,int y,unsigned char c,rfbPixel colour);
void rfbDrawString(rfbScreenInfoPtr rfbScreen,rfbFontDataPtr font,int x,int y,const char* string,rfbPixel colour);
int rfbDrawCharWithClip(rfbScreenInfoPtr rfbScreen,rfbFontDataPtr font,int x,int y,unsigned char c,int x1,int y1,int x2,int y2,rfbPixel colour,rfbPixel backColour);
void rfbDrawStringWithClip(rfbScreenInfoPtr rfbScreen,rfbFontDataPtr font,int x,int y,const char* string,int x1,int y1,int x2,int y2,rfbPixel colour,rfbPixel backColour);
int rfbWidthOfString(rfbFontDataPtr font,const char* string);
int rfbWidthOfChar(rfbFontDataPtr font,unsigned char c);
void rfbFontBBox(rfbFontDataPtr font,unsigned char c,int* x1,int* y1,int* x2,int* y2);
void rfbWholeFontBBox(rfbFontDataPtr font,int *x1, int *y1, int *x2, int *y2);

rfbFontDataPtr rfbLoadConsoleFont(char *filename);
void rfbFreeFont(rfbFontDataPtr font);

/* draw.c */

void rfbFillRect(rfbScreenInfoPtr s,int x1,int y1,int x2,int y2,rfbPixel col);
void rfbDrawPixel(rfbScreenInfoPtr s,int x,int y,rfbPixel col);
void rfbDrawLine(rfbScreenInfoPtr s,int x1,int y1,int x2,int y2,rfbPixel col);

/* selbox.c */

typedef void (*SelectionChangedHookPtr)(int _index);
extern int rfbSelectBox(rfbScreenInfoPtr rfbScreen,
                        rfbFontDataPtr font, char** list,
                        int x1, int y1, int x2, int y2,
                        rfbPixel foreColour, rfbPixel backColour,
                        int border,SelectionChangedHookPtr selChangedHook);

/* cargs.c */

extern void rfbUsage(void);
extern void rfbPurgeArguments(int* argc,int* position,int count,char *argv[]);
extern rfbBool rfbProcessArguments(rfbScreenInfoPtr rfbScreen,int* argc, char *argv[]);
extern rfbBool rfbProcessSizeArguments(int* width,int* height,int* bpp,int* argc, char *argv[]);

/* main.c */

extern void rfbLogEnable(int enabled);
typedef void (*rfbLogProc)(const char *format, ...);
extern rfbLogProc rfbLog, rfbErr;
extern void rfbLogPerror(const char *str);

void rfbScheduleCopyRect(rfbScreenInfoPtr rfbScreen,int x1,int y1,int x2,int y2,int dx,int dy);
void rfbScheduleCopyRegion(rfbScreenInfoPtr rfbScreen,sraRegionPtr copyRegion,int dx,int dy);

void rfbDoCopyRect(rfbScreenInfoPtr rfbScreen,int x1,int y1,int x2,int y2,int dx,int dy);
void rfbDoCopyRegion(rfbScreenInfoPtr rfbScreen,sraRegionPtr copyRegion,int dx,int dy);

void rfbMarkRectAsModified(rfbScreenInfoPtr rfbScreen,int x1,int y1,int x2,int y2);
void rfbMarkRegionAsModified(rfbScreenInfoPtr rfbScreen,sraRegionPtr modRegion);
void rfbDoNothingWithClient(rfbClientPtr cl);
enum rfbNewClientAction defaultNewClientHook(rfbClientPtr cl);
void rfbRegisterProtocolExtension(rfbProtocolExtension* extension);
void rfbUnregisterProtocolExtension(rfbProtocolExtension* extension);
struct _rfbProtocolExtension* rfbGetExtensionIterator();
void rfbReleaseExtensionIterator();
rfbBool rfbEnableExtension(rfbClientPtr cl, rfbProtocolExtension* extension,
        void* data);
rfbBool rfbDisableExtension(rfbClientPtr cl, rfbProtocolExtension* extension);
void* rfbGetExtensionClientData(rfbClientPtr cl, rfbProtocolExtension* extension);

rfbBool rfbCheckPasswordByList(rfbClientPtr cl,const char* response,int len);

/* functions to make a vnc server */
extern rfbScreenInfoPtr rfbGetScreen(int* argc,char** argv,
 int width,int height,int bitsPerSample,int samplesPerPixel,
 int bytesPerPixel);
extern void rfbInitServer(rfbScreenInfoPtr rfbScreen);
extern void rfbShutdownServer(rfbScreenInfoPtr rfbScreen,rfbBool disconnectClients);
extern void rfbNewFramebuffer(rfbScreenInfoPtr rfbScreen,char *framebuffer,
 int width,int height, int bitsPerSample,int samplesPerPixel,
 int bytesPerPixel);

extern void rfbScreenCleanup(rfbScreenInfoPtr screenInfo);
extern void rfbSetServerVersionIdentity(rfbScreenInfoPtr screen, char *fmt, ...);

/* functions to accept/refuse a client that has been put on hold
   by a NewClientHookPtr function. Must not be called in other
   situations. */
extern void rfbStartOnHoldClient(rfbClientPtr cl);
extern void rfbRefuseOnHoldClient(rfbClientPtr cl);

/* call one of these two functions to service the vnc clients.
 usec are the microseconds the select on the fds waits.
 if you are using the event loop, set this to some value > 0, so the
 server doesn't get a high load just by listening.
 rfbProcessEvents() returns TRUE if an update was pending. */

extern void rfbRunEventLoop(rfbScreenInfoPtr screenInfo, long usec, rfbBool runInBackground);
extern rfbBool rfbProcessEvents(rfbScreenInfoPtr screenInfo,long usec);
extern rfbBool rfbIsActive(rfbScreenInfoPtr screenInfo);

/* TightVNC file transfer extension */
void rfbRegisterTightVNCFileTransferExtension();
void rfbUnregisterTightVNCFileTransferExtension();

/* Statistics */
extern char *messageNameServer2Client(uint32_t type, char *buf, int len);
extern char *messageNameClient2Server(uint32_t type, char *buf, int len);
extern char *encodingName(uint32_t enc, char *buf, int len);

extern rfbStatList *rfbStatLookupEncoding(rfbClientPtr cl, uint32_t type);
extern rfbStatList *rfbStatLookupMessage(rfbClientPtr cl, uint32_t type);

/* Each call to rfbStatRecord* adds one to the rect count for that type */
extern void rfbStatRecordEncodingSent(rfbClientPtr cl, uint32_t type, int byteCount, int byteIfRaw);
extern void rfbStatRecordEncodingSentAdd(rfbClientPtr cl, uint32_t type, int byteCount); /* Specifically for tight encoding */
extern void rfbStatRecordEncodingRcvd(rfbClientPtr cl, uint32_t type, int byteCount, int byteIfRaw);
extern void rfbStatRecordMessageSent(rfbClientPtr cl, uint32_t type, int byteCount, int byteIfRaw);
extern void rfbStatRecordMessageRcvd(rfbClientPtr cl, uint32_t type, int byteCount, int byteIfRaw);
extern void rfbResetStats(rfbClientPtr cl);
extern void rfbPrintStats(rfbClientPtr cl);

extern int rfbStatGetSentBytes(rfbClientPtr cl);
extern int rfbStatGetSentBytesIfRaw(rfbClientPtr cl);
extern int rfbStatGetRcvdBytes(rfbClientPtr cl);
extern int rfbStatGetRcvdBytesIfRaw(rfbClientPtr cl);
extern int rfbStatGetMessageCountSent(rfbClientPtr cl, uint32_t type);
extern int rfbStatGetMessageCountRcvd(rfbClientPtr cl, uint32_t type);
extern int rfbStatGetEncodingCountSent(rfbClientPtr cl, uint32_t type);
extern int rfbStatGetEncodingCountRcvd(rfbClientPtr cl, uint32_t type);

extern void rfbSetProtocolVersion(rfbScreenInfoPtr rfbScreen, int major_, int minor_);

extern rfbBool rfbSendTextChatMessage(rfbClientPtr cl, uint32_t length, char *buffer);


/*
 * Additions for Qt event loop integration
 * Original idea taken from vino.
 */
rfbBool rfbProcessNewConnection(rfbScreenInfoPtr rfbScreen);
rfbBool rfbUpdateClient(rfbClientPtr cl);


#if(defined __cplusplus)
}
#endif

#endif
