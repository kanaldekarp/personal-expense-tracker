package com.expensetracker.servlets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginServlet Tests")
class LoginServletTest {

    private LoginServlet servlet;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new LoginServlet();
    }

    @Test
    @DisplayName("Login with null email redirects with error")
    void testNullEmail() throws Exception {
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("password");

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error="));
    }

    @Test
    @DisplayName("Login with empty email redirects with error")
    void testEmptyEmail() throws Exception {
        when(request.getParameter("email")).thenReturn("  ");
        when(request.getParameter("password")).thenReturn("password");

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error="));
    }

    @Test
    @DisplayName("Login with null password redirects with error")
    void testNullPassword() throws Exception {
        when(request.getParameter("email")).thenReturn("test@test.com");
        when(request.getParameter("password")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error="));
    }

    @Test
    @DisplayName("Login with empty password redirects with error")
    void testEmptyPassword() throws Exception {
        when(request.getParameter("email")).thenReturn("test@test.com");
        when(request.getParameter("password")).thenReturn("");

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error="));
    }

    @Test
    @DisplayName("Login with invalid credentials redirects with error")
    void testInvalidCredentials() throws Exception {
        when(request.getParameter("email")).thenReturn("nonexistent@test.com");
        when(request.getParameter("password")).thenReturn("wrongpassword");

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error="));
    }
}
