/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cysecurity.cspf.jvl.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cysecurity.cspf.jvl.model.DBConnect;
import java.sql.PreparedStatement;
import org.apache.commons.text.StringEscapeUtils;

/**
 *
 * @author breakthesec
 */
public class LoginValidator extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String user = request.getParameter("username").trim();
        String pass = request.getParameter("password").trim();
        try {
            Connection con = new DBConnect().connect(getServletContext().getRealPath("/WEB-INF/config.properties"));
            if (con != null && !con.isClosed()) {
                // *** Second Order SQL Injection found during Checkmarx scan ***
                //      ResultSet rs = null;
                //      Statement stmt = con.createStatement();
                //      rs = stmt.executeQuery("select * from users where username='" + user + "' and password='" + pass + "'");
                // *** Replaced with SQL Injection using PreparedStatement recommended by Checkmarx
                ResultSet rs = null;
                String sql = "select * from users where username=? and password=?";
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, user);
                preparedStatement.setString(2, pass);
                rs = preparedStatement.executeQuery();
                // ***
                if (rs != null && rs.next()) {
                    HttpSession session = request.getSession();
                    // *** Stored XSS found during Checkmarx scan ***
                    //      session.setAttribute("isLoggedIn", "1");
                    //      session.setAttribute("userid", rs.getString("id"));
                    //      session.setAttribute("user", rs.getString("username"));
                    //      session.setAttribute("avatar", rs.getString("avatar"));
                    // *** Replaced with Stored XSS using StringEscapeUtils recommended by Checkmarx
                    String id = StringEscapeUtils.escapeHtml4(rs.getString("id"));
                    String dbusername = StringEscapeUtils.escapeHtml4(rs.getString("username"));
                    String avatar = StringEscapeUtils.escapeHtml4(rs.getString("avatar"));
                    session.setAttribute("isLoggedIn", "1");
                    session.setAttribute("userid", id);
                    session.setAttribute("user", dbusername);
                    session.setAttribute("avatar", avatar);
                    // ***
                    Cookie privilege = new Cookie("privilege", "user");
                    response.addCookie(privilege);
                    if (request.getParameter("RememberMe") != null) {
                        Cookie username = new Cookie("username", user);
                        Cookie password = new Cookie("password", pass);
                        response.addCookie(username);
                        response.addCookie(password);
                    }
                    response.sendRedirect(response.encodeURL("ForwardMe?location=/index.jsp"));
                } else {
                    response.sendRedirect("ForwardMe?location=/login.jsp&err=Invalid Username or Password");
                }

            }
        } catch (Exception ex) {
            response.sendRedirect("login.jsp?err=something went wrong");
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the
    // + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}