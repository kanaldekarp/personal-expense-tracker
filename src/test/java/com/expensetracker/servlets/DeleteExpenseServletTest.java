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
import static org.mockito.ArgumentMatchers.contains;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteExpenseServlet Tests")
class DeleteExpenseServletTest {

    private DeleteExpenseServlet servlet;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new DeleteExpenseServlet();
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
