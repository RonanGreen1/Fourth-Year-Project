package com.example.android_app

import org.junit.Assert.*
import org.junit.Test

class MainActivityTests {


    private val passwordRegex = Regex(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{8,}\$"
    )
    // Is testing the password regex by comparing valid and valid passwords to the regex
    @Test
    fun `valid passwords match regex`() {
        listOf(
            "Aa1@abcd",
            "StrongP@ssword123!",
            "UPPERlower1\$"
        ).forEach {
            assertTrue("Expected '$it' to be valid", passwordRegex.matches(it))
        }
    }
    // Is testing the password regex by comparing invalid and valid passwords to the regex
    @Test
    fun `invalid passwords do not match regex`() {
        listOf(
            "short1A",      // too short
            "nouppercase1@", // no uppercase
            "NOLOWER1@",     // no lowercase
            "NoNumber!@",    // no digit
            "Has Space1@",   // contains whitespace
            "NoSpecial123"   // no special char
        ).forEach {
            assertFalse("Expected '$it' to be invalid", passwordRegex.matches(it))
        }
    }
}