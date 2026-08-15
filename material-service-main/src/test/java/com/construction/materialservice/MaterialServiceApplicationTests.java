package com.buildmate.material;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MaterialServiceApplicationTests {

    @Test
    void applicationClassIsLoadable() {
        assertDoesNotThrow(() -> Class.forName("com.buildmate.material.MaterialServiceApplication"));
    }
}
