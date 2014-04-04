#
# Generated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add customized code.
#
# This makefile implements configuration specific macros and targets.


# Environment
MKDIR=mkdir
CP=cp
GREP=grep
NM=nm
CCADMIN=CCadmin
RANLIB=ranlib
CC=gcc
CCC=g++
CXX=g++
FC=gfortran
AS=as

# Macros
CND_PLATFORM=MinGW-Windows
CND_DLIB_EXT=dll
CND_CONF=Debug
CND_DISTDIR=dist
CND_BUILDDIR=build

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/openvcdiffjavajni.o


# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=-mno-cygwin -Wl,--add-stdcall-alias -shared -m32
CXXFLAGS=-mno-cygwin -Wl,--add-stdcall-alias -shared -m32

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=../../../../../Windows/libvcdcom-0.dll ../../../../../Windows/libvcddec-0.dll ../../../../../Windows/libvcdenc-0.dll

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk dist/openvcdiffjavajni.dll

dist/openvcdiffjavajni.dll: ../../../../../Windows/libvcdcom-0.dll

dist/openvcdiffjavajni.dll: ../../../../../Windows/libvcddec-0.dll

dist/openvcdiffjavajni.dll: ../../../../../Windows/libvcdenc-0.dll

dist/openvcdiffjavajni.dll: ${OBJECTFILES}
	${MKDIR} -p dist
	${LINK.cc} -o dist/openvcdiffjavajni.dll ${OBJECTFILES} ${LDLIBSOPTIONS} -shared

${OBJECTDIR}/openvcdiffjavajni.o: openvcdiffjavajni.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -g -I../../../../../Program\ Files/Java/jdk1.7.0_45/include -I../../../../../Program\ Files/Java/jdk1.7.0_45/include/win32 -I/G/Lab/open-vcdiff-0.8.3/src  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/openvcdiffjavajni.o openvcdiffjavajni.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} dist/openvcdiffjavajni.dll

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
