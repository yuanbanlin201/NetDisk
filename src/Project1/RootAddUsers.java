package Project1;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class RootAddUsers extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int SpiderState = 0;
        try {

            User_Agent_Check uAC = new User_Agent_Check(request.getHeader("user-agent"));
            Referer_Check RC = new Referer_Check(request.getHeader("referer"), "NetDisk/RAU", "NetDisk/RSU");
            if (uAC.check()) {
                response.sendRedirect("403");
                SpiderState = 1;
            } else if (RC.check()) {
                response.sendRedirect("403");
                SpiderState = 1;
            }
        } catch (NullPointerException e) {
            response.sendRedirect("403");
            SpiderState = 1;
        }
        if (SpiderState==0 && request.getParameter("username")!=null && request.getParameter("password")!=null && request.getParameter("email")!=null){
            HttpSession session = request.getSession(false);//不创建新ID
            if (session != null) { //判断session是否存在
                if (session.getAttribute("username") != null && session.getAttribute("email") != null) {
                    if (session.getAttribute("username").equals("ROOT") && session.getAttribute("email").equals("ROOT@ROOT")) {
                        if (!SpecialCharCheck.check(request.getParameter("username")) && !SpecialCharCheck.check(request.getParameter("password")) && !SpecialCharCheck.check(request.getParameter("email"))){
                            String username = request.getParameter("username");
                            String password = request.getParameter("password");
                            String email = request.getParameter("email");
                            if (username.length() > 20 || username.length() <= 2 || password.length() < 6 || password.length() > 32 || email.length() > 32) {
                                response.sendRedirect("RAU");
                            } else {
                                if (username.equals("ROOT") || email.equals("ROOT@ROOT")){ //不允许注册管理员账户
                                    response.sendRedirect("RootPage");
                                }else {
                                    int register_state;
                                    try {
                                        String path = this.getServletContext().getRealPath("/WEB-INF/classes/DB_Info.properties");
                                        DB_Write try01 = new DB_Write(username, password, email, path);
                                        register_state = try01.write();
                                        switch (register_state) {
                                            //这几个页面不归入ErrorPage1.html页面，防止为登录用户获得session
                                            case 0 -> response.sendRedirect("ServerError");//TODO 如果出现了系统错误，就将用户返回到普通界面，但是此时仍没有退出管理员账号
                                            case 1 -> response.sendRedirect("RootEmailExist");
                                            case 2 -> response.sendRedirect("RootSignUpSuccess");
                                            case 3 -> response.sendRedirect("RootSignUpFail");
                                        }
                                    } catch (Exception e) {
                                        PrintWriter out = response.getWriter();
                                        out.print("<br><h1 style=\"text-align:center;font-size:2.5em;\">配置文件出错，请检查配置文件</h1>");
                                    }
                                }
                            }
                        }else {
                            session.invalidate();
                            response.sendError(403);
                        }
                    } else response.sendError(403);
                } else response.sendError(403);
            } else response.sendRedirect("login.html");//如果session过期
        }
    }
}
