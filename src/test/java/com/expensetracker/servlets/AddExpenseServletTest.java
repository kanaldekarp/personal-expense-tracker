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
@DisplayName("AddExpenseServlet Tests")
class AddExpenseServletTest {

    private AddExpenseServlet servlet;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new AddExpenseServlet();
    }

    @Test
    @DisplayName("No session redirects to login")
    void testNoSession() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("index.jsp"));
    }

    @Test
    @DisplayName("Session without userId redirects to login")
    void testSessionNoUserId() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("index.jsp"));
    }

    @Test
    @DisplayName("Invalid userId in session redirects to login")
    void testInvalidUserId() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn("not_a_number");

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("index.jsp"));
    }
}
