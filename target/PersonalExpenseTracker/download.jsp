<%@ page language="java" contentType="application/octet-stream" pageEncoding="UTF-8" %>
    <%@ page import="java.io.*" %>
        <% // Get the report path from session HttpSession sessionObj=request.getSession(false); if (sessionObj==null ||
            sessionObj.getAttribute("reportPath")==null) { response.sendRedirect("dashboard.jsp?error=No report
            available"); return; } String reportPath=(String) sessionObj.getAttribute("reportPath"); // Ensure we are
            only accessing the reports directory for security if (!reportPath.startsWith("/reports/")) {
            response.sendRedirect("dashboard.jsp?error=Invalid report path"); return; } String
            fullPath=getServletContext().getRealPath("/") + reportPath; File file=new File(fullPath); if (!file.exists()
            || !file.canRead()) { response.sendRedirect("dashboard.jsp?error=Report not found on server"); return; } //
            Force download response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + " \""); // Stream the
            file try (BufferedInputStream inStream=new BufferedInputStream(new FileInputStream(file)); OutputStream
            outStream=response.getOutputStream()) { byte[] buffer=new byte[4096]; int bytesRead; while
            ((bytesRead=inStream.read(buffer)) !=-1) { outStream.write(buffer, 0, bytesRead); } } catch (IOException e)
            { e.printStackTrace(); } %>