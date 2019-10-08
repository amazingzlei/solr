<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
    String path = request.getContextPath();
%>
<html>
<link rel="stylesheet" href="<%=path%>/css/public/bootstrap.min.css">
<link rel="stylesheet" href="<%=path%>/css/public/datatables.full.min.css">
<script src="<%=path%>/js/public/jquery-2.0.3.min.js"></script>
<script src="<%=path%>/js/public/bootstrap.min.js"></script>
<script src="<%=path%>/js/public/datatables.full.min.js"></script>
<script src="<%=path%>/js/public/bootstrap-paginator.js"></script>
<script src="<%=path%>/js/plupload/plupload.full.min.js"></script>
<script src="<%=path%>/js/public/toastr.min.js"></script>
<script src="<%=path%>/js/vue/vue.js"></script>
<style>
    .toast-center-center {
        top: 50%;
        left: 50%;
        margin-top: -25px;
        margin-left: -150px;
    }
    #medNum-error,#newMedNum-error{
        color:red;
        font-weight: normal;
        margin-left: 10px;
    }
</style>
<body>
<div class="container" style="margin-top: 50px">
    <div class="row">
        <div class="col-md-6 col-md-offset-3">
            <img src="<%=path%>/images/pic.jpg"/>
        </div>
        <div class="col-md-10">
            <input type="text" class="form-control" id="info" placeholder="请输入搜索内容">
        </div>
        <div class="col-md-2">
            <button id="search" type="button" class="btn btn-primary">搜索</button>
            <%--<div>--%>
            <%--<div style="font-size: 14px;color:#2D2F33;padding-bottom: 8px">文件上传</div>--%>
            <button id="choose_file" type="button" class="btn btn-primary">文件上传</button>
            <div style="font-size: 12px;color: #A2AABB;padding-top: 4px">支持扩展名:.doc .docx .txt</div>
            <%--</div>--%>
            <%--<form enctype="multipart/form-data" action="<%=path%>/upload" method="post" οnsubmit="return sub();">--%>
            <%--<input type="file" id="browse" name="file" id="file"/>--%>
            <%--<input type="submit" value="开始上传"/>--%>
            <%--</form>--%>
        </div>
    </div>
    <div class="row" id="app01" style="margin-top: 20px">
        <div v-for="item in list" :key="item.id">
            <div class="panel panel-primary">
                <div class="panel-heading" style="position: relative">
                    文件名:{{item.fileName}}
                    <a class="btn bg-danger" v-bind:href="'http://47.102.208.222:8090/' + item.fileName" style="position: absolute;left: 86%;top:9%">下载</a>

                    <template v-if="item.broName=='Firefox'">
                        <a class="btn bg-danger" @click.prevent="yulan(item)" style="position: absolute;left: 93%;top:9%">预览</a>
                    </template>

                    <%--<a class="btn bg-danger" v-bind:href="'ftp://47.102.208.222/public/note/' + item.fileName" style="position: absolute;left: 93%;top:9%" target="_blank">预览</a>--%>
                </div>
            </div>
            <div>
                <p v-html="item.fileContent"></p>
            </div>
        </div>

        <div id="example" style="text-align: center" hidden="false">
            <ul id="pageLimit"></ul>
        </div>
    </div>
</div>
</body>
<script>
    var broName = '';
    function detection(){
        var is360 = false;
        var isIE = false;
        var isFirefox = false;
        var isCrome = false;
        if(window.navigator.userAgent.indexOf('MSIE')!=-1&&window.navigator.appName.indexOf("Microsoft") != -1){
            isIE = true;
            broName = 'IE';
        }
        if(window.navigator.userAgent.indexOf('Firefox')!=-1){
            isFirefox = true;
            broName = 'Firefox';
        }
        if(window.navigator.userAgent.indexOf('Chrome')!=-1){
            if(window.navigator.webkitPersistentStorage){
                isCrome = true;
                broName = 'Chrome';
            }else{
                is360 = true;
                broName = '360';
            }
        }
    }
    detection();


    var uploader = new plupload.Uploader({
        browse_button:'choose_file',
        filters:{
            mime_types:[
                {title:'Office files',extensions:'doc,docx,txt,java,js,css,sql'}
            ]
        },
        url:'<%=path%>/upload',
        // flash上传组件的url地址
        flash_swf_url : '../js/Moxie.swf',
        // silverlight上传组件的url地址
        silverlight_xap_url : '../js/Moxie.xap',
    });
    uploader.bind('FilesAdded',function (uploader,files) {
        uploader.start();
    });
    uploader.bind('FileUploaded',function (uploader,file,responseObject) {
        alert("上传成功")
    });
    uploader.init();

    var vm = new Vue({
        el:"#app01",
        data:{
            list:[],
            totalCount:1
        },
        methods:{
            yulan:function (item) {
                fileName=item.fileName;

                var suffix=fileName.substring(fileName.lastIndexOf(".")+1).toLocaleLowerCase();

                if("java"==suffix||"css"==suffix||"js"==suffix||"sql"==suffix){
                    // alert("应该使用火狐预览")
                    // window.open('http://47.102.208.222:8090/'+fileName);
                    window.open('ftp://47.102.208.222/public/note/'+fileName);
                    // window.open('http://47.102.208.222:9000/public/note/'+fileName);
                }else {
                    window.open('http://127.0.0.1:8012/onlinePreview?url='+encodeURIComponent('http://47.102.208.222:8090/'+fileName));
                }

            }
        }
    })

    $(function () {
        $("#search").bind("click", function () {
            var info = $("#info").val();
//            $("#info").val("");
            $('tbody').empty();
            $.ajax(
                {
                    url: '<%=path%>/search',
                    type: 'GET',
                    data: {
                        content: info,
                        'current': 1,
                        'limit': 10
                    },
                    dataType: 'JSON',
                    success: function (data) {
                        if (data.code == 200) {
                            var contentList = data.data.contentList;
                            if (contentList.length != 0) {
                                $("#example").removeAttr("hidden");
                                var page_count = data.data.totalCount;

                                for (i = 0; i < contentList.length; i++) {

                                    contentList[i].broName='Firefox';
                                    if('Firefox'!=broName){
                                        var fileName=contentList[i].fileName;
                                        var suffix=fileName.substring(fileName.lastIndexOf(".")+1).toLocaleLowerCase();

                                        if("java"==suffix||"css"==suffix||"js"==suffix||"sql"==suffix){
                                            contentList[i].broName=broName;
                                        }
                                    }

                                }


                                vm.$data.list = contentList;
                                // $('tbody').empty();
                                // for (var i = 0; i < contentList.length; i++) {
                                //     showPageEmployeeData(contentList[i]);
                                // }

                                $('#last_page').text(page_count);
                                $('#pageLimit').bootstrapPaginator({
                                    currentPage: 1,//当前请求页
                                    totalPages: page_count,//一共多少页
                                    size: "normal",//应该是页眉的大小。
                                    bootstrapMajorVersion: 3,//bootstrap的版本要求。
                                    alignment: "right",
                                    numberOfPages: 10,//一页列出多少数据。
                                    itemTexts: function (type, page, current) {
                                        switch (type) {
                                            case "first":
                                                return "首页";
                                            case "prev":
                                                return "上一页";
                                            case "next":
                                                return "下一页";
                                            case "last":
                                                return "末页";
                                            case "page":
                                                return page;
                                        }//默认显示的是第一页。
                                    },
                                    onPageClicked: function (event, originalEvent, type, page) {//给每个页眉绑定一个事件，其实就是ajax请求，其中page变量为当前点击的页上的数字。
                                        $.ajax({
                                            url: '<%=path%>/search',
                                            type: 'GET',
                                            data: {content: info, 'current': page, 'limit': 10},
                                            dataType: 'JSON',
                                            success: function (data) {
                                                if (data.data != null) {
                                                    // $('tbody').empty();
                                                    var page_count = data.data.totalCount;
                                                    var contentList = data.data.contentList;
                                                    for (i = 0; i < contentList.length; i++) {

                                                        contentList[i].broName='Firefox';
                                                        if('Firefox'!=broName){
                                                            var fileName=contentList[i].fileName;
                                                            var suffix=fileName.substring(fileName.lastIndexOf(".")+1).toLocaleLowerCase();

                                                            if("java"==suffix||"css"==suffix||"js"==suffix||"sql"==suffix){
                                                                contentList[i].broName=broName;
                                                            }
                                                        }

                                                    }
                                                    // for (var i = 0; i < contentList.length; i++) {
                                                    //     showPageEmployeeData(contentList[i]);
                                                    // }
                                                    vm.$data.list = contentList;
                                                    $('#last_page').text(page_count)
                                                }
                                            }
                                        })
                                    }
                                });
                            } else {
                                vm.$data.list = [];
                                $('#example').attr("hidden", "true");
                                alert("暂无记录!")
                            }
                        } else {
                            vm.$data.list = [];
                            $('#example').attr("hidden", "true");
                            alert(data.msg);
                        }
                    }
                }
            );
        })
    })

    function sub() {
        return false;
    }

    <%--初始化toastr--%>
    $(function () {
        toastr.options = {

            "closeButton": true, //是否显示关闭按钮

            "debug": false, //是否使用debug模式

            "positionClass": "toast-center-center",//弹出窗的位置

            "showDuration": "300",//显示的动画时间

            "hideDuration": "1000",//消失的动画时间

            "timeOut": "3000", //展现时间

            "extendedTimeOut": "1000",//加长展示时间

            "showEasing": "swing",//显示时的动画缓冲方式

            "hideEasing": "linear",//消失时的动画缓冲方式

            "showMethod": "fadeIn",//显示时的动画方式

            "hideMethod": "fadeOut" //消失时的动画方式

        };
    })
</script>
</html>
