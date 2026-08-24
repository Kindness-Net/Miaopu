package dev.kiritoxd.miaopu.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HupuSessionAndUrlTest {
    @Test
    fun `recognizes authenticated cookie without exposing value`() {
        assertTrue(HupuCookieSession.isAuthenticated("foo=bar; ua=secret-token; theme=dark"))
        assertFalse(HupuCookieSession.isAuthenticated("foo=bar; ua="))
    }

    @Test
    fun `web navigation is limited to official hupu hosts`() {
        assertTrue(HupuUrls.isTrustedWebUrl("https://passport.hupu.com/v2/login"))
        assertTrue(HupuUrls.isTrustedWebUrl("https://w1.hoopchina.com.cn/games/a.js"))
        assertFalse(HupuUrls.isTrustedWebUrl("http://passport.hupu.com/v2/login"))
        assertFalse(HupuUrls.isTrustedWebUrl("https://hupu.com.example.test/phishing"))
    }

    @Test
    fun `score api uses current official protocol version`() {
        assertTrue(HupuUrls.SCORE_BASE.endsWith("/1/8.2.99"))
    }
}
