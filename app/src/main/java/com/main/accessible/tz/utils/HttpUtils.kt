package com.main.accessible.utils

import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HttpUtils {
    fun sha256(time: String): String {
        val apiSecret = "QkCvcH1BjXPz" // 密钥
        // 创建SecretKeySpec对象
        val secretKeySpec =
            SecretKeySpec(apiSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        var mac: Mac? = null
        try {
            mac = Mac.getInstance("HmacSHA256")
            mac.init(secretKeySpec)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException(e)
        }
        val hmac = mac.doFinal(time.toByteArray(StandardCharsets.UTF_8))

        // 将HMAC值转换为十六进制字符串
        val sb = StringBuilder()
        for (b in hmac) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}