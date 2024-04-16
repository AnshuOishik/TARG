//TARG Decompression Code
package targ;
import java.io.*;
import java.util.*;
class Decompress{
	static String info;
	static StringBuilder sb;
	static int flag;
	
	public static void decompress(String str) { 
		try{
			flag = Integer.parseInt(str);
			//BSC Decompression
			bscDecompress();	
			stringToAsciiRleDecode(); //Convert Compressed sequence to upper case RAW sequence		
			finalDecomp();
		}catch(Exception e){System.out.println(e);}
	}
	
	public static void stringToAsciiRleDecode()throws IOException{
		try{
			File fr = new File("F2.targ");
			BufferedReader br = new BufferedReader(new FileReader(fr));
			File fw = new File("dtemp.fa");
			BufferedWriter bw = new BufferedWriter(new FileWriter(fw));
			while((info = br.readLine()) != null){
				info = modifiedRleDecode(info);
				info = asciiCodeToString(info);
				bw.write(info);
				bw.write("\n");
			}
			br.close();
			bw.flush();
			info = null;
			delFile(fr);
		}catch(IOException e) {
            System.out.println(e);
        }
	}
	
	//Modified RLE 
    static String modifiedRleDecode(String info) { 
		char [] arrInfo = info.toCharArray();
		int len = info.length();
		sb = new StringBuilder(len);
        int i, j, count;
		if (len > 0) { 
			sb.append(arrInfo[0]); 
            for (i = 1; i < len; i++) { 
                if(arrInfo[i] == '1'){
					i++;
					count = arrInfo[i] - 48 + 4;
					for(j = 1; j < count; j++)
						sb.append(arrInfo[i-2]);
				}
				else
					sb.append(arrInfo[i]); 
            }
        }
		return sb.toString();
    }
	
	static String asciiCodeToString(String str){
		int L = str.length();
		StringBuilder sb = new StringBuilder(L);
		int av, r, count, i, j;
		char ch;
        for (i = 0; i < L; i++) {
			ch = str.charAt(i);
			//ASCII value to base 4 convertion
			av=(int)ch;
			if(ch == 'A' || ch == 'C' || ch == 'G' || ch == 'T' || ch == 'U')
				sb.append(ch);
			else{
				count=0;
				while(av>3){ 
					r=av%4; 
					av=av/4;
					if(r == 0)
						sb.append('A');
					else if(r == 1)
						sb.append('C');
					else if(r == 2)
						sb.append('G');
					else if(r == 3 && flag == 1)
						sb.append('T');
					else if(r == 3 && flag == 2)
						sb.append('U');
					count++;
				}
				if(av == 0)
					sb.append('A');
				else if(av == 1)
					sb.append('C');
				else if(av == 2)
					sb.append('G');
				else if(av == 3 && flag == 1)
					sb.append('T');
				else if(av == 3 && flag == 2)
					sb.append('U');
				count++;
				//To make it 4 digit 
				for(j=0;j<4-count;j++)
					sb.append('A');
			}
		}
		return sb.toString();
	}
	
	public static void finalDecomp(){
		try{
			File fr1 = new File("F1.targ");
			BufferedReader br1 = new BufferedReader(new FileReader(fr1));
			File fr2 = new File("dtemp.fa");
			BufferedReader br2 = new BufferedReader(new FileReader(fr2));
			
			File fw0 = new File("dtempSpecial.fa");
			BufferedWriter bw0 = new BufferedWriter(new FileWriter(fw0));
			
			File fw1 = new File("dtempCase.fa");
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(fw1));
			
			File fw2 = new File("Decoded.targ");
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(fw2));
			
			String info2[] = new String[5]; //Read five lines fron F1.targ
			int i = 0, j, k;
			while((info = br1.readLine()) != null){
				info2[i] = info;
				i += 1;
			}

			String id[] = info2[0].split(">"); //Stores no of id & individual ids
			
			String lin_len[] = info2[1].split(" ");
			int line_len[] = new int[lin_len.length];
			for(i = 0; i < lin_len.length; i++) {
				line_len[i] = Integer.parseInt(lin_len[i]);
			}
			
			String blk_len[] = info2[2].split(" ");
			int block_len[] = new int[blk_len.length];
			for(i = 0; i<blk_len.length; i++) {
				block_len[i] = Integer.parseInt(blk_len[i]);
			}
			//Reverse Modified Delta Coding
			for(i = 1; i<blk_len.length; i++) {
				block_len[i] += block_len[0]; 
			}
			
			//lowr_info[] stores both position and length
			String lowr_info[] = info2[3].split(" ");
			int lower_info[] = new int[lowr_info.length];
			for(i = 0; i < lowr_info.length; i++) {
				lower_info[i] = Integer.parseInt(lowr_info[i]);
			}
			
			//Reverse modified delta coding 
			for(i = 2; i <= lower_info[0]*2 ; i++){
				if(i % 2 == 0)
					lower_info[i] = lower_info[i] + lower_info[i-1] + 1; 
				else
					lower_info[i] = lower_info[i] + lower_info[i-1];
			} 			
			
			//For other characters
			int ol  =0;
			int other_char_index[] = null;
			int other_char[] = null;
			if(info2[4] != null){
				String other_char_info[] = info2[4].split(" ");
				ol = other_char_info.length;
				other_char_index = new int[ol/2];
				other_char = new int[ol/2];
				j = k = 0;
				for(i = 0; i < ol; i++) {
					if(i%2 == 0){
						other_char_index[j] = Integer.parseInt(other_char_info[i]);
						j++;
					}
					else{
						other_char[k] = Integer.parseInt(other_char_info[i]);
						k++;
					}
				}
				//Reverse Modified Delta Coding
				for(i = 1; i<ol/2; i++) {
					other_char_index[i] += other_char_index[i-1]+1; 
				}
			}
			
			//no_of_id stores number of FASTA file in multi-FASTA file
			int no_of_id = Integer.parseInt(id[0]); //same as no_of_block or line_length
						
			StringBuilder target_string;

			//Reading from 'dtemp.fa' and write to 'dtempSpecial.fa'
			while( (info = br2.readLine()) != null){
				j = k = 0;
				target_string = new StringBuilder(info);
				for(i = 0; i<(target_string.length()+ol/2); i++){
					if((j<ol/2) && (i == other_char_index[j])){ 
						bw0.write((char)(other_char[j]+65));
						j++;
					}
					else{
						bw0.write(target_string.charAt(k));
						k++;
					}
				}
				bw0.write("\n");
			}
			bw0.flush();
			br1.close();
			delFile(fr1);
			br2.close();
			delFile(fr2);
			
			//Reading from 'dtempSpecial.fa' and write to 'dtempCase.fa'
			BufferedReader br = new BufferedReader(new FileReader(fw0));
			j = 1;
			while( (info = br.readLine()) != null){
				target_string = new StringBuilder(info);
				for(i = 1; i <= lower_info[0]; i++){
					for (k = lower_info[j]; k < lower_info[j+1]; k++) {
						target_string.setCharAt(k, Character.toLowerCase(target_string.charAt(k)));
					}
					j += 2;
				}
				bw1.write(target_string.toString());
				bw1.write("\n");
			}
			bw1.flush();
			
			br.close();
			delFile(fw0);
			
			//Reading from 'dtempCase.fa' file and write to 'Decoded.targ' file
			br = new BufferedReader(new FileReader(fw1));
			info = br.readLine();  
			int line_start = 0, block_start = 0, c1 = -1, c2;
			for(i = 0; i < info.length(); i += block_len[c1]){ //no_of_block
				c2 = 0;
				bw2.write(">"+id[c1+2]+"\n"); //Write Identifier
				for(j = block_start; j < (i+block_len[c1+1]); j += line_len[1]){
					if((block_len[c1+1] - c2*line_len[1]) >= line_len[1]){
						for(k = line_start; k < line_len[1]; k++){
							bw2.write(info.charAt(j+k)+"");
						}
					}
					else{
						for(k = line_start; k < (block_len[c1+1]%line_len[1]); k++){
							bw2.write(info.charAt(j+k)+"");
						}
					}
					c2++;
					bw2.write("\n");
				}
				c1++;
				block_start += block_len[c1];
			}
			bw2.flush();
			br.close();
			delFile(fw1);
		}catch(Exception e){System.out.println(e);}
	}
	
	public static void bscDecompress() {
        try {
            String bscCommand = "./bsc d " + "Encoded.bsc " + "FinalTar.tar";
            Process p1 = Runtime.getRuntime().exec(bscCommand);
            p1.waitFor();
            String tarCommand = "tar -xf " + "FinalTar.tar";
            Process p2 = Runtime.getRuntime().exec(tarCommand);
            p2.waitFor();
			delFile(new File("FinalTar.tar"));	
        } catch (Exception e) {
            System.out.println(e);
        }
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
}