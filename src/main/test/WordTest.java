import org.apache.commons.io.FileUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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

    public static void main(String[] args) {

    }
}
