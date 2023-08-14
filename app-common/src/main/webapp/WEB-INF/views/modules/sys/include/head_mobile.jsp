<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<meta http-equiv="Cache-Control" content="no-store" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Expires" content="0" />
<meta name="author" content="Eryan"/>
<link rel="shortcut icon" href="${ctxStatic}/img/favicon.ico" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta name="renderer" content="webkit">
<script src="${ctxStatic}/js/jquery/jquery-1.12.4.min.js" type="text/javascript"></script>
<script src="${ctxStatic}/js/jquery/jquery-migrate-1.4.1.min.js" type="text/javascript"></script>
<script src="${ctxStatic}/js/jquery/jquery-extend.min.js" type="text/javascript"></script>
<link href="${ctxStatic}/js/jquery-validation-1.19.3/dist/extend/jquery-validate-extend.min.css" type="text/css" rel="stylesheet" />
<script src="${ctxStatic}/js/jquery-validation-1.19.3/dist/jquery.validate.min.js" type="text/javascript"></script>
<script src="${ctxStatic}/js/jquery-validation-1.19.3/dist/extend/jquery-validate-extend-methods.min.js" type="text/javascript"></script>
<link href="${ctxStatic}/js/bootstrap/2.3.2/css/bootstrap.min.css" type="text/css" rel="stylesheet" />
<link href="${ctxStatic}/js/bootstrap/2.3.2/css/bootstrap-responsive.css" type="text/css" rel="stylesheet" />
<script src="${ctxStatic}/js/bootstrap/2.3.2/js/bootstrap.min.js" type="text/javascript"></script>
<!--[if lte IE 6]><link href="${ctxStatic}/js/bootstrap/bsie/css/bootstrap-ie6.min.css" type="text/css" rel="stylesheet" />
<script src="${ctxStatic}/js/bootstrap/bsie/js/bootstrap-ie.min.js" type="text/javascript"></script><![endif]-->
<link href="${ctxStatic}/js/select2/select2.min.css" type="text/css" rel="stylesheet" />
<script src="${ctxStatic}/js/select2/select2.min.js" type="text/javascript"></script>
<script src="${ctxStatic}/js/select2/i18n/zh-CN.js" type="text/javascript"></script>
<link href="${ctxStatic}/js/common/common.min.css" type="text/css" rel="stylesheet" />
<script src="${ctxStatic}/js/common/common.min.js" type="text/javascript"></script>
<script src="${ctxStatic}/js/common/mustache.min.js" type="text/javascript"></script>
<script type="text/javascript" src="${ctxStatic}/js/vue/dist/vue.min.js" charset="utf-8"></script>
<script src="${ctxStatic}/js/My97DatePicker/WdatePicker.js" type="text/javascript"></script>
<link href="${ctxStatic}/js/fancyBox/source/jquery.fancybox.css" type="text/css" rel="stylesheet" />
<script src="${ctxStatic}/js/fancyBox/source/jquery.fancybox.js" type="text/javascript"></script>
<script src="${ctxStatic}/js/bootstrap/dropdown/bootstrap-hover-dropdown.js" type="text/javascript"></script>
<!--[if lt IE 7 ]> <script src="${ctxStatic}/js/dd_belatedpng.js"></script> <script> DD_belatedPNG.fix('img, .png_bg'); //fix any <img> or .png_bg background-images </script> <![endif]-->
<link href="${ctxStatic}/js/toastr/toastr.min.css" type="text/css" rel="stylesheet" />
<script src="${ctxStatic}/js/toastr/toastr.min.js" type="text/javascript"></script>
<script type="text/javascript">
    var ctx = "${ctx}";
    var ctxAdmin = "${ctxAdmin}";
    var ctxFront = "${ctxFront}";
    var ctxMobile = "${ctxMobile}";
    var ctxStatic = "${ctxStatic}";
    var appURL = "${appURL}";
    try {
        toastr.options = {"closeButton":true,"positionClass": "toast-top-center"};
        $.ajaxSetup({headers:{'Authorization':'Bearer ${sessionInfo.token}'}});
    } catch (e) {console.error(e); }
</script>
