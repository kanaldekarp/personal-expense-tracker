package com.expensetracker.servlets;

import jakarta.servlet.RequestDispatcher;
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
import static org.mockito.ArgumentMatchers.contains;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardServlet Tests")
class DashboardServletTest {

    private DashboardServlet servlet;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new DashboardServlet();
    }

    @Test
    @DisplayName("No session redirects to login")
    void testNoSession() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect(contains("index.jsp"));
    }

    @Test
    @DisplayName("Session without userId redirects to login")
    void testSessionNoUserId() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect(contains("index.jsp"));
    }
}
