package Project1;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class Register extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int SpiderState = 0;
        try {
            User_Agent_Check uAC = new User_Agent_Check(request.getHeader("user-agent"));//确定用户所使用的操作系统版本、CPU 类型、浏览器版本等信息,反爬虫
            Referer_Check RC = new Referer_Check(request.getHeader("referer"), "NetDisk/register.html");//表示请求当前资源的客户端来源
            if (uAC.check()) {
                response.sendRedirect("403.html");
                SpiderState = 1;
            }
//            else if (RC.check()) {//TODO Referer：NetDisk/dist/index.html; Real: NetDisk/register.html
//                response.sendRedirect("403.html");
//                SpiderState = 1;
//            }
        } catch (NullPointerException e) {
            response.sendRedirect("403.html");
            SpiderState = 1;
        }
        if (SpiderState==0 && request.getParameter("username")!=null && request.getParameter("password")!=null && request.getParameter("email")!=null){
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String email = request.getParameter("email");
            if (username.length() > 20 || username.length() <= 2 || password.length() < 6 || password.length() > 32 || email.length() > 32) {
                response.sendRedirect("register.html");//TODO 有了前台的检查，还有必要后台检查嘛？
            } else {
                if (username.equals("ROOT") || email.equals("ROOT@ROOT")){ //不允许注册管理员账户
                    response.sendRedirect("register.html");
                }else {
                    if (SpecialCharCheck.check(username) || SpecialCharCheck.check(password) || SpecialCharCheck.check(email)){//特殊字符检查
                        response.sendError(403);
                    }else {
                        int register_state;
                        try {
                            String path = this.getServletContext().getRealPath("/WEB-INF/classes/DB_Info.properties");//无用
                            DB_Write try01 = new DB_Write(username, password, email, path);
                            register_state = try01.write();//TODO 测试失败,返回0——数据库连不上
                            switch (register_state) {
                                case 0 -> response.sendRedirect("ServerError.html");//这几个页面不归入ErrorPage1.html页面，防止为登录用户获得session
                                case 1 -> response.sendRedirect("EmailExist.html");
                                case 2 -> response.sendRedirect("SignUpSuccess.html");
                                case 3 -> response.sendRedirect("SignUpFail.html");
                            }
                        } catch (Exception e) {
                            PrintWriter out = response.getWriter();
                            out.print("<br><h1 style=\"text-align:center;font-size:2.5em;\">配置文件出错，请检查配置文件</h1>");
                        }
                    }
                }
            }
        } else response.sendError(403);// TODO
    }
}







