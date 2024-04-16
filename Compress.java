//TARG Compression Code
package targ;
import java.io.*;
import java.util.*;
class Compress{	
	static final int vec_size1 = 1018891; 
	static final int vec_size2 = 3232;  
	static final int vec_size3 = 3736738;
	public static int[] seqLowVecBeg; //Lower vector begin
	public static int[] seqLowVecLen; //Lower vector length
	public static String[] seqId; //Store sequence IDs, array for multi-FASTA file
	public static int[] seqLineLen; //Store FASTA file first line length
	public static int[] seqBlockLen; //Store FASTA file  block length
	public static int[] seqSpecialIndex; //Store FASTA file  other char index
	public static byte[] seqSpecialChar; //Store FASTA file  other char
	static int iden = 0; //Store number of id = line length = sequence block length
	static int lowVecLen = 0;
	static int seqCodeLen = 0;
	static int seqSpecialLen = 0;
	static int charLen = 0; //Stores total number of chraracters in a file
	static String info;
	static StringBuilder sb; //As there is no multi-threading, we are using it because it is faster due to not being thread-safe.
		
	public static void compress(String path) { 
		try{
			File f = new File("F1.targ");
			FileWriter fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);		
			seqExtraction(path);
			saveIdName(bw);
			rleForLineLen(bw);
			saveSeqBlockLen(bw);
			saveLowerVec(bw);
			saveSpecialChar(bw);
			tarbEncode();
		}catch(Exception e){
			System.out.println("e1... =  "+e);
		}
	}
	
	public static void seqExtraction(String path) {
        seqLowVecBeg = new int[vec_size1];  
        seqLowVecLen = new int[vec_size1];
		seqId = new String[vec_size2];  
		seqLineLen = new int[vec_size2]; 
		seqBlockLen = new int[vec_size2];
		seqSpecialIndex = new int[vec_size3];
		seqSpecialChar = new byte[vec_size3];
		BufferedReader br = null;
		File file = new File(path);

		Boolean flag = true;
		int lettersLen = 0, mark=0, count=0;
		char ch;
        try {
			File f = new File("temp.fa");
			FileWriter fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);
            br = new BufferedReader(new FileReader(file));
			int index = 0; 
			while ((info = br.readLine()) != null) {
				if(info.charAt(0) == '>'){ //Sequence Id start by '>' symbol
					seqId[iden] = info;
					mark = 1;
					seqBlockLen[iden] = seqCodeLen; 
					seqCodeLen = 0;
				}
				else{
					if(mark == 1){
						seqLineLen[iden++] = info.length();
						mark = 0;
					}
					for (int i = 0; i < info.length(); i++) {
						charLen++;
						ch = info.charAt(i);
						if (Character.isLowerCase(ch)) {
							if (flag) {
								flag = false;
								seqLowVecBeg[lowVecLen] = lettersLen;
								lettersLen = 0;
							}
							ch = Character.toUpperCase(ch);
						} else {
							if (!flag) {
								flag = true;
								seqLowVecLen[lowVecLen++] = lettersLen - 1; //static entropy encoding with continuous tolerance 1
								lettersLen = 0;
							}
						}
						lettersLen++;
						if (ch == 'A' || ch == 'C' || ch == 'G' || ch == 'T' || ch == 'U') {
							bw.write(ch);
							count++;
							if(count == (Integer.MAX_VALUE-2)){ 
								bw.write("\n");
								count = 0;
							}
						}
						else{//If FASTA file contain special characters except A,C,G,T/U
							seqSpecialIndex[seqSpecialLen] = (charLen - 1) - index; 
							seqSpecialChar[seqSpecialLen] = (byte)(ch - 65);
							seqSpecialLen++;
							index = charLen;
						}
						seqCodeLen++;
					}
				}
			}
			if (!flag) {
                seqLowVecLen[lowVecLen++] = lettersLen;
            }
			seqBlockLen[iden] = seqCodeLen;
			for(int i = 2;i <= iden;i++){ //Modified Delta Coding
				seqBlockLen[i] = seqBlockLen[i] - seqBlockLen[1];
			}
			br.close();
			bw.flush();
		}catch(Exception e){
			System.out.println("e2 =  "+e);
		}
	}
	
	//Identifier
	public static void saveIdName(BufferedWriter bw) {
        try {
			bw.write(iden+"");
            for (int i = 0; i < iden; i++) {
                bw.write(seqId[i]);
            }
			bw.write("\n");
			seqId = null;
        } catch (IOException e) {
            System.out.println("e3 =  "+e);
        }
    }
	
	//RLE
	public static void rleForLineLen(BufferedWriter bw) { 
        List<Integer> rleCode = new ArrayList<>(2); 
		int count = 1,codeLen; 
        if (iden > 0) { 
            rleCode.add(seqLineLen[0]);   
            for (int i = 1; i < iden; i++) { 
                if (seqLineLen[i] == seqLineLen[i - 1]) { 
                    count++;
                } else {
                    rleCode.add(count);
                    rleCode.add(seqLineLen[i]);
                    count = 1;
                }
            }
            rleCode.add(count);
			seqLineLen = null;
        }
        codeLen = rleCode.size(); 
        try {
            bw.write(codeLen + " "); 
            for (int i = 0; i < codeLen; i++) { 
                bw.write(rleCode.get(i) + " "); 
            }
			bw.write("\n");
        } catch (IOException e) {
            System.out.println("e4 =  "+e);
        }
    }
	
	//Encoded using modified delta coding 
	public static void saveSeqBlockLen(BufferedWriter bw) {
		try {
			//'iden' store number of blocks
            for (int i = 1; i <= iden; i++) {
                bw.write(seqBlockLen[i] + " ");
            }
			bw.write("\n");
			seqBlockLen = null;
        } catch (IOException e) {
            System.out.println("e5 =  "+e);
        }
    }
	
	public static void saveLowerVec(BufferedWriter bw) {
        try {
			bw.write(lowVecLen+" ");
            for (int i = 0; i < lowVecLen; i++) {
                bw.write(seqLowVecBeg[i]+" "+seqLowVecLen[i] + " ");
            }
			bw.write("\n");
			seqLowVecBeg = seqLowVecLen = null;
        } catch (IOException e) {
            System.out.println("e6 =  "+e);
        }
    }
	
	//Save Special Characters Information
	public static void saveSpecialChar(BufferedWriter bw) {
		try {
            for (int i = 0; i < seqSpecialLen; i++) {
                bw.write(seqSpecialIndex[i] + " ");
				bw.write(seqSpecialChar[i] + " ");
            }
			bw.flush();
			seqSpecialIndex = null;
			seqSpecialChar = null;
        } catch (IOException e) {
            System.out.println("e7 =  "+e);
        }
	}
	
	//Encoding the preprocessed sequence 
	public static void tarbEncode()throws IOException{
		//2-bit integer coding, extended ASCII and modified RLE
		stringToAsciiRleEncode();		
		bscCompress();	//BSC Compression
	}
	
	//Strings to extended ASCII to modified RLE code
	static void stringToAsciiRleEncode(){
		int len, code_L = 0, i, k, step_len, code_ascii_value, rem_len;
		char ch;
		int[] two_bit_code;
		try{
			File f = new File("temp.fa");
			BufferedReader br = new BufferedReader(new FileReader(f));
			File f1 = new File("F2.targ");
			BufferedWriter bw = new BufferedWriter(new FileWriter(f1));
			while((info = br.readLine()) != null){
				len = info.length();
				two_bit_code = new int[len];
				//Converting to two_bit_code
				for (i = 0; i < len; i++) {
					ch = info.charAt(i);
					two_bit_code[code_L++] = twoBitIntCoding(ch);
				}
				sb = new StringBuilder(len);
		
				//Converting two_bit_code to extended ASCII code
				step_len = code_L/4;
				code_ascii_value = 0;
				for (i =0; i < step_len; i++) {
					code_ascii_value = 0;
					for (k = 3; k >= 0; k--) {
						code_ascii_value <<= 2;
						code_ascii_value += two_bit_code[4*i+k];
					}
					//For A, C, G, T, U and 1, and for value 10 and 13 
					if(code_ascii_value == 10 || code_ascii_value == 13|| code_ascii_value == 65 || code_ascii_value == 67 || code_ascii_value == 71 || code_ascii_value == 84|| code_ascii_value == 85 || code_ascii_value == 49) 
						for (k = 0; k <= 3; k++){
							if(two_bit_code[4*i+k] == 0)
								sb.append('A');
							else if(two_bit_code[4*i+k] == 1)
								sb.append('C');
							else if(two_bit_code[4*i+k] == 2)
								sb.append('G');
							else if(two_bit_code[4*i+k] == 3)
								sb.append('T');
						}
					else
						sb.append((char)code_ascii_value);
				}
				//If odd length i.e. 1,2,3 remains
				rem_len = code_L%4; 
				for (i = code_L-rem_len; i < code_L ; i++){
					if(two_bit_code[i] == 0)
						sb.append('A');
					else if(two_bit_code[i] == 1)
						sb.append('C');
					else if(two_bit_code[i] == 2)
						sb.append('G');
					else if(two_bit_code[i] == 3)
						sb.append('T');
				}
				info = sb.toString();
				//modified RLE
				info = modifiedRleEncode(info);
				bw.write(info);
				bw.write("\n");
			}
			br.close();
			bw.flush();
			info = null;
			delFile(f);
		}catch(IOException e) {
            System.out.println(e);
        }	
	}
	
	//Character to 2-bit Integer Coding {A - 0, C - 1, G - 2, T/U - 3}
	static int twoBitIntCoding(char c) {
		int r;
        switch (c) {
            case 'A':	r = 0; break;
            case 'C':	r = 1; break;
            case 'G':	r = 2; break;
            case 'T':	
			case 'U':	r = 3; break;
            default:	r = -1;
        }
		return r;
    }
	
	//Modified RLE 
    static String modifiedRleEncode(String info) { 
		char [] arrInfo = info.toCharArray();
		int len = info.length();
		sb = new StringBuilder(len);
        int i, j, count = 1;
		if (len > 0) { 
			sb.append(info.charAt(0)); 
			count = 1;
            for (i = 1; i < len; i++) { 
                if (arrInfo[i-1] == arrInfo[i]) {
                    count++; 
				}
                else {
					if(count >= 4 && count<= 13){
						sb.append('1').append(count - 4); 
					}else if(count > 13){
						sb.append("19");
						for(j = 0; j < (count-13); j++)
							sb.append(arrInfo[i-1]);
					}else{
						for(j = 1; j < count; j++)
							sb.append(arrInfo[i-1]); 
					}
					sb.append(arrInfo[i]);
					count = 1;
                }
            }
			if(count >= 4 && count<= 13){
				sb.append('1').append(count - 4); 
			}else if(count > 13){
				sb.append("19");
				for(j = 0; j < (count-13); j++)
					sb.append(arrInfo[i-1]);
			}else{
				for(j = 1; j < count; j++)
					sb.append(arrInfo[i-1]); 
			}
        }
		return sb.toString();
    }
	
	public static boolean delFile(File f) {
        if (!f.exists()) {
            return false;
        }
		if (f.isDirectory()) {
            File[] fs = f.listFiles();
            for (File f1 : fs) 
                delFile(f1);
        }
        return f.delete();
    }
	
	//BSC Compression
	public static void bscCompress() { 
		try {
            String tarCommand = "tar -cf " + "FinalTar.tar "+ "F1.targ " + "F2.targ";
			Process p1 = Runtime.getRuntime().exec(tarCommand);
            p1.waitFor();
            String bscCommand = "./bsc e " + "FinalTar.tar " + "Encoded.bsc -e2";
            Process p2 = Runtime.getRuntime().exec(bscCommand);
            p2.waitFor();

            delFile(new File("FinalTar.tar"));
            delFile(new File("F1.targ"));
            delFile(new File("F2.targ"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}