import com.fh.util.FtpUtil;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.Map;

public class SolrTest {

    private static final String SOLR_URL = "http://47.102.208.222/solr/core1";
    @Test
    public void testAdd() throws Exception {
        // 1.创建连接
        SolrClient client = new HttpSolrClient(SOLR_URL);
        // 2.创建索引
        SolrInputDocument document = new SolrInputDocument();
        File file = new File("C:\\Users\\admin\\Desktop\\测试数据\\a.txt","gbk");
        // 3.设置域 注意域名必须在solr中存在，不是我们随便起的否则报错
        document.setField("id", "测试");
        document.setField("title_ik", FileUtils.readFileToString(file));
        document.setField("name", file.getName());
        // 注意第一个参数为多长时间后提交，也可以通过client.commit()方法进行手动提交，两者取其一
        // 但是两者都没有则代码不会报错，但是在solr不会有消息
        // 只要是与solr进行交互，必须提交
        client.add(document,1000);
        client.close();
    }

    @Test
    public void testDel() throws Exception {
        String path= "http://localhost:8080/solr/core1";
        // 1.创建连接
        SolrClient client = new LBHttpSolrClient(SOLR_URL);
        // 删除所有
        client.deleteByQuery("*:*",1000);
        client.close();
    }

    // 更新 注意更新实际上就是添加，只要id相同则会更新
    @Test
    public void testUpdate() throws Exception{
        // 1.创建连接
        SolrClient client = new HttpSolrClient(SOLR_URL);
        // 2.创建索引
        SolrInputDocument document = new SolrInputDocument();
        File file = new File("C:\\Users\\admin\\Desktop\\测试数据\\a.txt");
        // 3.设置域 注意域名必须在solr中存在，不是我们随便起的否则报错
        document.setField("id", "测试");
        document.setField("address","aaa");
        document.setField("name", "aaa");
        // 注意第一个参数为多长时间后提交，也可以通过client.commit()方法进行手动提交，两者取其一
        // 但是两者都没有则代码不会报错，但是在solr不会有消息
        // 只要是与solr进行交互，必须提交
        client.add(document,1000);
        client.close();
    }

    // 简单查询
    @Test
    public void testQuery() throws Exception{
        // 1.创建连接
        SolrClient client = new HttpSolrClient(SOLR_URL);
        // 2.查询
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        // 3.执行
        QueryResponse response = client.query(query);
        System.out.println("------>response:"+response);
        // 4.获取结果集
        SolrDocumentList list = response.getResults();
        // 获取总数
        System.out.println("------>总记录条数:"+list.getNumFound());
        for (SolrDocument document:list){
            System.out.println(document.get("address"));
        }
    }

    // 复杂查询
    @Test
    public void testQuery02() throws Exception{
        // 1.创建连接
        SolrClient client = new HttpSolrClient(SOLR_URL);
        // 2.查询
        SolrQuery query = new SolrQuery();
        query.setQuery("address:中华人民共和国浙江省");
        query.set("fq", "host:com");// 设置过滤 过滤就是且的意思
        query.setSort("attempts", SolrQuery.ORDER.desc);// 设置排序
        query.setStart(2);// 设置起始页
        query.setRows(5);// 设置页容量
        query.setFields("address","id");// 设置需要展示的域
//        query.set("df","address");// 设置默认域
        // 设置高亮信息
        query.setHighlight(true);// 这行代码表示开启高亮
        query.addHighlightField("address");
        query.setHighlightSimplePre("<em>");
        query.setHighlightSimplePost("</em>");
        // 3.执行
        QueryResponse response = client.query(query);
        System.out.println("------>response:"+response);
        // 4.获取结果集
        SolrDocumentList list = response.getResults();
        // 获取总数
        System.out.println("------>总记录条数:"+list.getNumFound());
        // 获取高亮显示信息
        Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
        System.out.println(highlighting);
        for (SolrDocument document:list){
            System.out.println("address:"+document.get("address")+",host:"+document.get("host")+",id:"+document.get("id"));

            List<String> docList = highlighting.get(document.get("id")).get("address");

            System.out.println(docList.get(0));
        }
    }

    // 将文件上传到ftp
    @Test
    public void test() throws UnsupportedEncodingException {
        try {
            FileInputStream in=new FileInputStream(new File("C:\\Users\\admin\\Desktop\\资料\\测试数据\\a.txt"));
            boolean flag = FtpUtil.uploadFile("192.168.48.128", 0,
                    "uftp", "123456", "public","test", "aaa.txt", in);
            System.out.println(flag);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 测试从ftp上下载文件
    @Test
    public void testDownLoadFile(){
        FtpUtil.downloadFile("192.168.48.128", 0, "uftp", "123456", "/public/test/", "aaa.txt", "D:\\");
    }

}
