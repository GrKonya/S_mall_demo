<%@ page contentType="text/html;charset=UTF-8" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/res/css/fore/fore_nav.css"/>
<script>
    $(function () {
        $(".quick_li").find("li").hover(
            function () {
                $(this).find(".sn_menu").addClass("sn_menu_hover");
                $(this).find(".quick_menu,.quick_qrcode,.quick_DirectPromoDiv,.quick_sitmap_div").css("display", "block");
            }, function () {
                $(this).find(".sn_menu").removeClass("sn_menu_hover");
                $(this).find(".quick_menu,.quick_qrcode,.quick_DirectPromoDiv,.quick_sitmap_div").css("display", "none");
            }
        );
    });
</script>
<div id="nav">
    <div class="nav_main">
        <p id="container_login">
            <c:choose>
                <c:when test="${requestScope.user.user_name==null}">
                    <em>喵，欢迎来到校园电商商城</em>
                    <a href="${pageContext.request.contextPath}/login">请登录</a>
                    <a href="${pageContext.request.contextPath}/register">免费注册</a>
                </c:when>
                <c:otherwise>
                    <em>Hi，</em>
                    <a href="${pageContext.request.contextPath}/userDetails" class="userName"
                       target="_blank">${requestScope.user.user_name}</a>
                    <a href="${pageContext.request.contextPath}/login/logout">退出</a>
                </c:otherwise>
            </c:choose>
        </p>
        <ul class="quick_li">
            <li class="quick_li_MyTaobao">
                <div class="sn_menu">
                    <a href="${pageContext.request.contextPath}/userDetails">我的校园购物车<b></b></a>
                    <div class="quick_menu">
                        <a href="${pageContext.request.contextPath}/order/0/10">已买到的宝贝</a>
                        <a href="#">已卖出的宝贝</a>
                    </div>
                </div>
            </li>
            <li class="quick_li_cart">
                <img src="${pageContext.request.contextPath}/res/images/fore/WebsiteImage/buyCar.png">
                <a href="${pageContext.request.contextPath}/cart">购物车</a>
            </li>
            <li class="quick_li_menuItem">
                <div class="sn_menu">
                    <a href="#">收藏夹<b></b></a>
                    <div class="quick_menu">
                        <a href="#">收藏的宝贝</a>
                        <a href="#">收藏的店铺</a>
                    </div>
                </div>
            </li>
            <li class="quick_li_separator"></li>
            <li class="quick_home"><a href="${pageContext.request.contextPath}">校园网</a></li>
            <li class="quick_home"><a href="http://localhost:9322/#/index">校园二手</a></li>

            <li class="quick_DirectPromo">
                <div class="sn_menu">
                    <a href="http://localhost:8080/schoolmall/admin">商家支持<b></b></a>
                </div>
            </li>
            <li class="quick_sitemap">
                <div class="sn_menu">
                    <a>网站导航<b></b></a>
                    <div class="quick_sitmap_div">
                        <div class="site-hot">
                            <h2>热点推荐<span>Hot</span></h2>
                            <ul>
                                <li><a href="">校园生鲜</a></li>
                                <li><a href="">科技新品</a></li>
                                <li><a href="">女装新品</a></li>
                                <li><a href="">酷玩街</a></li>
                                <li><a href="">试美妆</a></li>
                                <li><a href="">运动新品</a></li>
                                <li><a href="">时尚先生</a></li>
                                <li><a href="">吃乐会</a></li>
                                <li><a href="">班级采购</a></li>
                                <li><a href="">会员积分</a></li>
                                <li><a href="">品质频道</a></li>
                            </ul>
                        </div>
                        <div class="site-market">
                            <h2>行业市场<span>Market</span></h2>
                            <ul>
                                <li><a href="">美妆</a></li>
                                <li><a href="">电器</a></li>
                                <li><a href="">女装</a></li>
                                <li><a href="">男装</a></li>
                                <li><a href="">女鞋</a></li>
                                <li><a href="">男鞋</a></li>
                                <li><a href="">箱包</a></li>
                                <li><a href="">运动</a></li>
                                <li><a href="">家装</a></li>
                                <li><a href="">医药</a></li>
                                <li><a href="">食品</a></li>
                                <li><a href="">配饰</a></li>
                                <li><a href="">汽车</a></li>
                            </ul>
                        </div>
                        <div class="site-brand">
                            <h2>品牌风尚<span>Brand</span></h2>
                            <ul>
                                <li><a href="">大牌聚会</a></li>
                                <li><a href="">潮牌街</a></li>
                                <li><a href="">校园咸鱼</a></li>
                                <li><a href="">校园原创</a></li>
                                <li><a href="">什么牌子好</a></li>
                            </ul>
                        </div>
                        <div class="site-help">
                            <h2>服务指南<span>Help</span></h2>
                            <ul>
                                <li><a href="">帮助中心</a></li>
                                <li><a href="">品质保障</a></li>
                                <li><a href="">特色服务</a></li>
                                <li><a href="">7天退换货</a></li>
                            </ul>
                        </div>
                    </div>
                </div>
            </li>
        </ul>
    </div>
</div>
