package com.expensetracker.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tag Model Tests")
class TagTest {

    @Test
    @DisplayName("Default constructor creates empty Tag with default color")
    void testDefaultConstructor() {
        Tag t = new Tag();
        assertEquals(0, t.getId());
        assertEquals(0, t.getUserId());
        assertNull(t.getName());
        assertEquals("#6366f1", t.getColor());
    }

    @Test
    @DisplayName("Parameterized constructor sets all fields")
    void testParameterizedConstructor() {
        Tag t = new Tag(5, 10, "Urgent", "#ff0000");
        assertEquals(5, t.getId());
        assertEquals(10, t.getUserId());
        assertEquals("Urgent", t.getName());
        assertEquals("#ff0000", t.getColor());
    }

    @Test
    @DisplayName("Getters and setters work correctly")
    void testGettersSetters() {
        Tag t = new Tag();
        
        t.setId(3);
        assertEquals(3, t.getId());

        t.setUserId(7);
        assertEquals(7, t.getUserId());

        t.setName("Business");
        assertEquals("Business", t.getName());

        t.setColor("#00ff00");
        assertEquals("#00ff00", t.getColor());
    }
}
