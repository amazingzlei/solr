package com.fh.service.impl;

import com.fh.entity.FileDetail;
import com.fh.entity.FileVO;
import com.fh.entity.ResultVo;
import com.fh.mapper.SolrMapper;
import com.fh.service.ISolrService;
import com.fh.util.FtpUtil;
import com.fh.util.ResultVoUtil;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class SolrServiceImpl implements ISolrService {
    private static final String SOLR_URL = "http://47.102.208.222/solr/core1";
    private static final String FTP_ADDRES = "47.102.208.222";
    private static final String FTP_USERNAME = "uftp";
    private static final String FTP_PASSWORD = "123456";
    private static final String FTP_BASEPATH = "public";
    private static final String FTP_FILEPATH = "note";

    @Autowired
    SolrMapper solrMapper;

    @Override
    public ResultVo searchContent(String content, int current, int limit) {
        try {
            if(!StringUtils.isEmpty(content)){
                // 返回信息容器
                Map<String, Object> map = new HashMap<>(2);
                List<FileVO> contentList = new ArrayList<FileVO>();

                // 1.创建连接
                SolrClient client = new HttpSolrClient(SOLR_URL);
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("content_ik:").append(content);
                // 2.查询
                SolrQuery query = new SolrQuery();
                query.setQuery(stringBuffer.toString());
                query.setStart((current-1)*limit);// 设置起始页
                query.setRows(limit);// 设置页容量
                // 设置高亮信息
                query.setHighlight(true);// 这行代码表示开启高亮
                query.addHighlightField("content_ik");
                query.setHighlightSimplePre("<span style='color:red'>");
                query.setHighlightSimplePost("</span>");
                // 3.执行
                QueryResponse response = client.query(query);
                // 4.获取结果集
                SolrDocumentList list = response.getResults();
                // 获取总数
                long totalCount = list.getNumFound();
                Long size = (totalCount%limit)==0?totalCount/limit:totalCount/limit+1;
                // 获取高亮显示信息
                Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
                System.out.println(highlighting);
                for (SolrDocument document:list){
                    FileVO fileVO = new FileVO();
                    fileVO.setId((String) document.get("id"));
                    fileVO.setFileName((String) document.get("name"));
                    fileVO.setFilePath((String) document.get("url"));
                    List<String> docList = highlighting.get(document.get("id")).get("content_ik");
                    fileVO.setFileContent(docList.get(0));
                    contentList.add(fileVO);
                }
                map.put("totalCount",size );
                map.put("contentList", contentList);
                return ResultVoUtil.success(map);
            }else {
                return ResultVoUtil.error(404, "请输入要搜索的内容!");
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultVoUtil.error(502, "发生异常,请联系管理员!");
        }
    }

    @Override
    public ResultVo upload(MultipartFile multipartFile){
        try {
            String content = readFile(multipartFile);
            // 判断是否是正确的文件格式
            if(!StringUtils.isEmpty(content)){
                if(!StringUtils.isEmpty(content)){// 判断内容是否为空
                    // 1.创建连接
                    SolrClient client = new HttpSolrClient(SOLR_URL);
                    // 2.创建文档
                    SolrInputDocument document = new SolrInputDocument();
                    // 3.创建域
                    // 判断文件是否存在
                    FileDetail fileDetail = isFileExist(multipartFile.getOriginalFilename());
                    if(fileDetail!=null){
                        document.addField("id", fileDetail.getId());
                    }else {
                        String id = UUID.randomUUID().toString();
                        // 将文件信息存入数据库
                        solrMapper.insertFile(id, multipartFile.getOriginalFilename());

                        document.addField("id",id);
                    }
                    document.addField("name",multipartFile.getOriginalFilename());
                    document.addField("content_ik",content);
                    // 4.发送文档
                    client.add(document, 1000);
                    client.close();

                    // 将文档发送到ftp服务器
                    FtpUtil.uploadFile(FTP_ADDRES, 21, FTP_USERNAME,
                            FTP_PASSWORD, FTP_BASEPATH, FTP_FILEPATH, multipartFile.getOriginalFilename(),
                            multipartFile.getInputStream() );
                    return ResultVoUtil.success();
                }else {
                    return ResultVoUtil.error(500, "不能上传内容为空的文件!");
                }
            }else {
                return ResultVoUtil.error(501, "请上传word文档或者txt文件!");
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultVoUtil.error(502, "发生异常,请联系管理员!");
        }
    }

    // 读取文件
    private String readFile(MultipartFile multipartFile) throws IOException {
        // 文件内容
        String text = "";
        // 获取文件名称
        String fileName = multipartFile.getOriginalFilename();
        // 获取文件流
        InputStream inputStream = multipartFile.getInputStream();
        //2003
        if(fileName.endsWith(".doc")){
            try {
                WordExtractor word = new WordExtractor(inputStream);
                text = word.getText();
                //去掉word文档中的多个换行
                text = text.replaceAll("(\\r\\n){2,}", "\r\n");
                text = text.replaceAll("(\\n){2,}", "\n");
                System.out.println(text);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(fileName.endsWith(".docx")){       //2007
            try {
                XWPFDocument xwpf = new XWPFDocument(inputStream);
                POIXMLTextExtractor ex = new XWPFWordExtractor(xwpf);
                text = ex.getText();
                //去掉word文档中的多个换行
                text = text.replaceAll("(\\r\\n){2,}", "\r\n");
                text = text.replaceAll("(\\n){2,}", "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(fileName.endsWith(".txt")||fileName.endsWith(".java")||fileName.endsWith(".js")||fileName.endsWith(".css")||fileName.endsWith(".sql")){
            /* 读入TXT文件 */
            InputStreamReader reader = new InputStreamReader(inputStream,"utf-8"); // 建立一个输入流对象reader
            BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
            StringBuffer buffer = new StringBuffer();
            String content ;
            while((content=br.readLine()) != null){
                buffer.append(content.trim());
            }
            text = buffer.toString().trim();
        }else {
            return null;
        }
        return text;
    }

    private FileDetail isFileExist(String filename){
        return solrMapper.getFileByName(filename);
    }
}
