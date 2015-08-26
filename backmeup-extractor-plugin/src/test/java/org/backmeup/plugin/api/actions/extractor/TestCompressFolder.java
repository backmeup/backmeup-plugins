package org.backmeup.plugin.api.actions.extractor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

public class TestCompressFolder {

	@Test
	public void test() throws Exception {
		String sourceFolderName =  "src/test/resources";
        String outputFileName = "resources.zip";
 
        FileOutputStream fos = new FileOutputStream(outputFileName);
        ZipOutputStream zos = new ZipOutputStream(fos);
        //level - the compression level (0-9)
        zos.setLevel(9);
 
        System.out.println("Begin to compress folder : " + sourceFolderName + " to " + outputFileName);
        compressFolder(zos, sourceFolderName, sourceFolderName);
 
        zos.close();
        System.out.println("Program ended successfully!");
	}
	
	private void compressFolder(ZipOutputStream zos,String folderName,String baseFolderName)throws Exception{
        File f = new File(folderName);
        if(f.exists()){
 
            if(f.isDirectory()){
                if(!folderName.equalsIgnoreCase(baseFolderName)){
                    String entryName = folderName.substring(baseFolderName.length()+1,folderName.length()) + File.separatorChar;
                    System.out.println("Adding folder entry " + entryName);
                    ZipEntry ze= new ZipEntry(entryName);
                    zos.putNextEntry(ze);    
                }
                File f2[] = f.listFiles();
                for(int i=0;i<f2.length;i++){
                	compressFolder(zos,f2[i].getAbsolutePath(),baseFolderName);    
                }
            }else{
                String entryName = folderName.substring(baseFolderName.length()+1,folderName.length());
                System.out.print("Adding file entry " + entryName + "...");
                ZipEntry ze= new ZipEntry(entryName);
                zos.putNextEntry(ze);
                FileInputStream in = new FileInputStream(folderName);
                int len;
                byte buffer[] = new byte[1024];
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                in.close();
                zos.closeEntry();
                System.out.println("OK!");
            }
        }else{
            System.out.println("File or directory not found " + folderName);
        }
    }
}
