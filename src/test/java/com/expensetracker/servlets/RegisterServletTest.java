package com.expensetracker.servlets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterServlet Tests")
class RegisterServletTest {

    private RegisterServlet servlet;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        servlet = new RegisterServlet();
    }

    @Test
    @DisplayName("Register with null username redirects with error")
    void testNullUsername() throws Exception {
        when(request.getParameter("username")).thenReturn(null);
        when(request.getParameter("email")).thenReturn("test@test.com");
        when(request.getParameter("password")).thenReturn("password123");

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error="));
    }

    @Test
    @DisplayName("Register with empty email redirects with error")
    void testEmptyEmail() throws Exception {
        when(request.getParameter("username")).thenReturn("testuser");
        when(request.getParameter("email")).thenReturn("");
        when(request.getParameter("password")).thenReturn("password123");

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error="));
    }

    @Test
    @DisplayName("Register with short password redirects with error")
    void testShortPassword() throws Exception {
        when(request.getParameter("username")).thenReturn("testuser");
        when(request.getParameter("email")).thenReturn("test@test.com");
        when(request.getParameter("password")).thenReturn("12345");

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error="));
    }

    @Test
    @DisplayName("Register with null password redirects with error")
    void testNullPassword() throws Exception {
        when(request.getParameter("username")).thenReturn("testuser");
        when(request.getParameter("email")).thenReturn("test@test.com");
        when(request.getParameter("password")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error="));
    }

    @Test
    @DisplayName("Register with all empty fields redirects with error")
    void testAllEmpty() throws Exception {
        when(request.getParameter("username")).thenReturn("");
        when(request.getParameter("email")).thenReturn("");
        when(request.getParameter("password")).thenReturn("");

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error="));
    }
}
