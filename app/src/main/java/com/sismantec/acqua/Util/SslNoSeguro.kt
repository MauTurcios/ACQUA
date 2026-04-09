package com.sismantec.acqua.Util
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class SslNoSeguro {
    //------------------------------------
    //Funcion para Forzar el SSL en las conexion
    //------------------------------------
    fun crearSslInseguro(): SSLContext {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<X509Certificate>, authType: String
                ) {}

                override fun checkServerTrusted(
                    chain: Array<X509Certificate>, authType: String
                ) {}

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext
    }
}