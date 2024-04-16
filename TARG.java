//TARG Driver File
package targ;
import java.io.*;
class TARG{
	static public void main(String a[])throws IOException{
		double iUsedMem, eUsedMem;
		long startCompTime, endCompTime;
		if(a[0].equals("compress")){
			iUsedMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			startCompTime = System.currentTimeMillis();
			File pathFile = new File(a[1]);
			Compress.compress(pathFile.getAbsolutePath());
			endCompTime = System.currentTimeMillis();
			eUsedMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			System.out.printf("\nMemory Used = %.2f MB",(eUsedMem-iUsedMem)/(1024*1024));
			System.out.println("\nCompression time: " + (1.0*(endCompTime - startCompTime))/1000 + " sec");
		}
		else if(a[0].equals("decompress")){
			iUsedMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			startCompTime = System.currentTimeMillis();
			Decompress.decompress(a[1]); //flag 1 for DNA, 2 for RNA
			endCompTime = System.currentTimeMillis();
			eUsedMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			System.out.printf("\nMemory Used = %.2f MB",(eUsedMem-iUsedMem)/(1024*1024));
			System.out.println("\nDecompression time: " + (1.0*(endCompTime - startCompTime))/1000 + " sec");
		}
		else{
			System.out.println("Error: Please specify the execution mode, compress for compression and decompress for decompression");
		}
	}
}
