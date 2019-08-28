import org.apache.commons.io.FileUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.Test;

import java.io.*;

public class WordTest {
    @Test
    public void testWord() throws IOException {
        File file = new File("C:\\Users\\admin\\Desktop\\资料\\全文索引\\solr安装.doc");
        String content = FileUtils.readFileToString(file, "UTF-8");
        System.out.println(content);
    }

    @Test
    public void testWord02() throws Exception{
        String text = "";
        String filePath = "C:\\Users\\admin\\Desktop\\资料\\全文索引\\solr安装.doc";
        File file = new File(filePath);
        //2003
        if(file.getName().endsWith(".doc")){
            try {
                FileInputStream stream = new FileInputStream(file);
                WordExtractor word = new WordExtractor(stream);
                text = word.getText();
                //去掉word文档中的多个换行
                text = text.replaceAll("(\\r\\n){2,}", "\r\n");
                text = text.replaceAll("(\\n){2,}", "\n");
                System.out.println(text);
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else if(file.getName().endsWith(".docx")){       //2007
            try {
                OPCPackage oPCPackage = POIXMLDocument.openPackage(filePath);
                XWPFDocument xwpf = new XWPFDocument(oPCPackage);
                POIXMLTextExtractor ex = new XWPFWordExtractor(xwpf);
                text = ex.getText();
                //去掉word文档中的多个换行
                text = text.replaceAll("(\\r\\n){2,}", "\r\n");
                text = text.replaceAll("(\\n){2,}", "\n");
                System.out.println(text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void test03() throws Exception {
        InputStream inputStream = new FileInputStream("C:\\Users\\admin\\Desktop\\以后学啥.txt");
        InputStreamReader reader = new InputStreamReader(
                inputStream,"utf-8"); // 建立一个输入流对象reader
        BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
        StringBuffer buffer = new StringBuffer();
        String content ;
        while((content=br.readLine()) != null){
            buffer.append(content.trim());
        }
        String text = buffer.toString().trim();
        System.out.println(text);
    }
}
