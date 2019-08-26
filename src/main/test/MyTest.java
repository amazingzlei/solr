import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.junit.Test;
import org.lionsoul.jcseg.analyzer.JcsegAnalyzer;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.nio.file.Paths;

import static org.apache.lucene.document.Field.*;
import static org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig.*;

public class    MyTest {

    @Test
    public void test() throws Exception {
        System.out.println(FileUtils.readFileToString(
                new File("C:\\Users\\admin\\Desktop\\实习笔记\\文件上传\\FormData+ajax上传文件.java"), "utf-8"));
    }

    // 创建索引
    @Test
    public void test01() throws Exception {
        // 1.创建IndexWriter，用于创建文档的索引
        // 第一个参数索引库的位置 第二个参数分词器
        Directory directory = NIOFSDirectory.open(Paths.get("F:\\index"));
        Analyzer analyzer = new IKAnalyzer();
//        Analyzer analyzer = new IKAnalyzer();
//        Analyzer analyzer = new SmartChineseAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
//        File file = new File("C:\\Users\\admin\\Desktop\\测试数据");
        File file = new File("C:\\Users\\admin\\Desktop\\资料\\测试数据");
        File[] files = file.listFiles();
        for (File f : files) {
            // 2.构建文档
            Document document = new Document();
            String fileName = f.getName();
            // 3.创建field
//            String fileContent = FileUtils.readFileToString(f, "gbk");
            String fileContent = FileUtils.readFileToString(f, "utf-8");
            Field field1 = new StringField("fileName", fileName, Store.YES);
            document.add(field1);

            Field field2 = new TextField("fileContent", fileContent, Store.YES);
            document.add(field2);

            String filePath = f.getPath();
            Field field3 = new StringField("filePath", filePath, Store.YES);
            document.add(field3);

            Long length = f.length();
            Field field4 = new LongPoint("fileLength", length);
//            Field field4 = new LongField("fileLength", length,Store.YES);
            document.add(field4);
            // 6版本后 设置是否存储通过以下方式
            document.add(new StoredField("fileLength", 1));
            // 4.创建索引
            indexWriter.addDocument(document);
        }
        // 5.关闭IndexWriter
        indexWriter.close();
    }

    // 查询索引
    @Test
    public void test02() throws Exception {
        // 1.创建Directory 即索引库
        Directory directory = NIOFSDirectory.open(Paths.get("F:\\index"));
        // 2.创建IndexReader用于和索引库建立连接,该对象是一个流
        IndexReader indexReader = DirectoryReader.open(directory);
        // 3.创建IndexSearcher
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        // 4.创建搜索搜索term，query对象可以看做是数据库中的where条件
        Query query = new TermQuery(new Term("fileContent", "导入Jquery和bootstrap库"));
        // 5.执行
        TopDocs topDocs = indexSearcher.search(query, 10);
        // 6.展示结果
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            // 获取文档的id
            int id = scoreDoc.doc;
            // 通过id查找文档
            Document doc = indexSearcher.doc(id);
            String fileName = doc.get("fileName");
            StringBuffer stringBuffer = new StringBuffer();
            String fileContent = doc.get("fileContent");
            if (fileContent.length() > 30) {
                stringBuffer.append(fileContent.substring(0, 30));
                stringBuffer.append("...");
            } else {
                stringBuffer.append(fileContent);
            }
            String filePath = doc.get("filePath");
            String fileLength = doc.get("fileLength");
            System.out.println("fileName:" + fileName + "\nfileContent:" +
                    stringBuffer.toString() + "\nfilePath:" + filePath + "\nfileLength:" + fileLength + "\n");
        }
        indexReader.close();
    }


    //删除所有索引
    @Test
    public void test03() throws Exception {
        IndexWriter indexWriter = getIndexWriter();
        // 删除所有索引
        indexWriter.deleteAll();
        indexWriter.close();
    }

    //删除指定索引
    @Test
    public void test04() throws Exception {
        IndexWriter indexWriter = getIndexWriter();
        // 删除指定索引
        Query query = new TermQuery(new Term("fileName", "杂录.java"));
        indexWriter.deleteDocuments(query);
        indexWriter.close();
    }

    // 更新索引
    @Test
    public void test05() throws Exception {
        IndexWriter indexWriter = getIndexWriter();
        Query query = new TermQuery(new Term("fileName", "杂录.java"));
        // 更新即 先删除再插入 第一个参数为需要删除的doc  第二个参数为新插入的doc
        Document document = new Document();
        document.add(new StringField("fileName", "更新后的杂录.java", Store.YES));
        document.add(new TextField("fileContent", "更新", Store.YES));
        document.add(new StringField("filePath", "aaaa", Store.YES));
        indexWriter.updateDocument(new Term("fileName", "杂录.java"), document);
        indexWriter.close();
    }

    // 查询所有
    @Test
    public void test06() throws Exception {
        IndexSearcher indexSearcher = getIndexSearcher();
        Query query = new MatchAllDocsQuery();
        getResult(indexSearcher, query);
        indexSearcher.getIndexReader().close();
    }

    // 范围查询
    @Test
    public void test07() throws Exception {
        IndexSearcher indexSearcher = getIndexSearcher();
//        Query query = LongPoint.newRangeQuery("fileLength", 0, 200000L);
//        Query query = new NumericRangeQuery("fileLength", 0, 200000L,true,true);
//        getResult(indexSearcher, query);
//        indexSearcher.getIndexReader().close();
    }

    // 组合查询
    @Test
    public void test08() throws Exception {
        IndexSearcher indexSearcher = getIndexSearcher();
        BooleanQuery.Builder booQuery = new BooleanQuery.Builder();
        Query query1 = new TermQuery(new Term("fileContent", "解决"));
        Query query2 = new TermQuery(new Term("fileContent", "方案"));

        // BooleanClause.Occur.MUST表示必须有这个条件
        booQuery.add(query1, BooleanClause.Occur.MUST);
        booQuery.add(query2, BooleanClause.Occur.MUST);

        Query query = booQuery.build();
        getResult(indexSearcher, query);
        indexSearcher.getIndexReader().close();
    }


    // 通配符查询
    @Test
    public void test09() throws Exception {
        IndexSearcher indexSearcher = getIndexSearcher();
        Query query = new WildcardQuery(new Term("fileContent", "*花"));
        getResult(indexSearcher, query);
        indexSearcher.getIndexReader().close();
    }

    // 模糊查询
    @Test
    public void test10() throws Exception {
        IndexSearcher indexSearcher = getIndexSearcher();
        // 第二个参数表示最大可编辑数，取值范围0，1，2
        //  * 允许我的查询条件的值，可以错误几个字符
        Query query = new FuzzyQuery(new Term("fileContent", "花"), 1);
        getResult(indexSearcher, query);
        indexSearcher.getIndexReader().close();
    }

    // 字符串
    @Test
    public void test11() throws Exception {
        IndexSearcher indexSearcher = getIndexSearcher();
        // 第一个参数表示默认域 第二个表示分词方式
        QueryParser queryParser = new QueryParser("fileContent", new IKAnalyzer());
        Query query = queryParser.parse("解决方案");
        getResult(indexSearcher, query);
        indexSearcher.getIndexReader().close();
    }

    // 多域查询
    @Test
    public void test12() throws Exception {
        IndexSearcher indexSearcher = getIndexSearcher();
        String[] field = {"fileContent", "fileName"};
        QueryParser queryParser = new MultiFieldQueryParser(field, new JcsegAnalyzer(COMPLEX_MODE));
        Query query = queryParser.parse("json");
        getResult(indexSearcher, query);
        indexSearcher.getIndexReader().close();
    }

    // 短语查询
    /*@param slop:设置两个短语之间的最大间隔数，设置的间隔数越大，他能匹配的结果就越多，性能就越慢
     * @param field:设置查找的字段
     * @param terms:设置查找的短语，是一个可变长的数组
     * */
    @Test
    public void test13() throws Exception {
        IndexSearcher indexSearcher = getIndexSearcher();
        PhraseQuery query = new PhraseQuery(100, "fileContent", new String[]{"input", "文件"});
        getResult(indexSearcher, query);
        indexSearcher.getIndexReader().close();
    }


    public IndexWriter getIndexWriter() throws Exception {
        Directory directory = NIOFSDirectory.open(Paths.get("F:\\index"));
        Analyzer analyzer = new JcsegAnalyzer(COMPLEX_MODE);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        return indexWriter;
    }

    public IndexSearcher getIndexSearcher() throws Exception {
        // 1.创建Directory 即索引库
        Directory directory = NIOFSDirectory.open(Paths.get("F:\\index"));
        // 2.创建IndexReader用于和索引库建立连接,该对象是一个流
        IndexReader indexReader = DirectoryReader.open(directory);
        // 3.创建IndexSearcher
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        return indexSearcher;
    }

    public void getResult(IndexSearcher indexSearcher, Query query) throws Exception {
        // 5.执行
        TopDocs topDocs = indexSearcher.search(query, 10);
            // 返回返回记录
            // TopDocs topDocs = indexSearcher.search(query, indexSearcher.getIndexReader().maxDoc());
        // 6.展示结果
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            // 获取文档的id
            int id = scoreDoc.doc;
            // 通过id查找文档
            Document doc = indexSearcher.doc(id);
            String fileName = doc.get("fileName");
            StringBuffer stringBuffer = new StringBuffer();
            String fileContent = doc.get("fileContent");
            if (fileContent.length() > 30) {
                stringBuffer.append(fileContent.substring(0, 30));
                stringBuffer.append("...");
            } else {
                stringBuffer.append(fileContent);
            }
            String filePath = doc.get("filePath");
            System.out.println("fileName:" + fileName + "\nfileContent:" + stringBuffer.toString() + "\nfilePath:" + filePath + "\n");
        }
    }

}
