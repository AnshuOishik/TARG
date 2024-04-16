		****************************** TARG ****************************
			TARG: an algorithm using two-bit encoding and extended ASCII coding with run-length encoding followed by a general-purpose encoder
					https://github.com/AnshuOishik/TARG
						Copyright (C) 2024 
=============================================================================================================================
Introduction
To utilize the code, please use the Notepad++ editor.
Java has been utilized by us in the implementation.
Please use Linux as your operating system.
Please confirm that the physical memory on your computer is equals or larger than  3 GB.
=============================================================================================================================
# Compilation Command:
> javac -d . *.java

# Execution Command:
Compression:
> java -Xmx3072m targ.TARG compress In.fa

Decompression:
> java targ.TARG decompress 1

Notice:
# In.fa is the input file
# targ is the package name and TARG is the driver class name
# Please use "compress" for compression
# Please use "decompress" for decompression
# Please use flag 1 for DNA file and flag 2 for RNA file
# Encoded.bsc is the final compressed file that the BSC compressor produces.
# The decompressed file name is Decoded.targ
# -Xmx3072m is the mamimum allocation of heap memory (MB) size.
# Please place the executable "bsc" in the main class file's directory.
# Kindly set "chmod 0777" for "bsc" mode.
=============================================================================================================================
Commands for platform dependent "bsc" executable file generation from available code at https://github.com/IlyaGrebnov/libbsc
Compilation commands:
> g++ -c libbsc/adler32/adler32.cpp
> g++ -c libbsc/bwt/libsais/libsais.c
> g++ -c libbsc/bwt/bwt.cpp
> g++ -c libbsc/coder/coder.cpp
> g++ -c libbsc/coder/qlfc/qlfc.cpp
> g++ -c libbsc/coder/qlfc/qlfc_model.cpp
> g++ -c libbsc/filters/detectors.cpp
> g++ -c libbsc/filters/preprocessing.cpp
> g++ -c libbsc/libbsc/libbsc.cpp
> g++ -c libbsc/lzp/lzp.cpp
> g++ -c libbsc/platform/platform.cpp
# Please change the platform.cpp file. In lines 51 and 66, change 'MEM_LARGE_PAGES' in Linux (Ubuntu) to 'MEM_4MB_PAGES' in Windows 10.
> g++ -c libbsc/st/st.cpp
> g++ -c bsc.cpp

Linking command:
> g++ -o bsc bsc.o adler32.o bwt.o coder.o detectors.o libbsc.o libsais.o lzp.o platform.o preprocessing.o qlfc.o qlfc_model.o st.o
=============================================================================================================================
### Contacts 
Please send an email to <subhankar.roy07@gmail.com> if you experience any issues.