package achecrawler.crawler.async.fetcher;


import java.util.Arrays;
import java.util.List;

import okhttp3.CipherSuite;

public class CustomCipherSuites {

    private static List<CipherSuite> customCipherSuites;

    public CustomCipherSuites(){
        customCipherSuites = Arrays.asList(
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_DHE_DSS_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256,
                CipherSuite.TLS_DHE_DSS_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_DH_anon_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DH_anon_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_DH_anon_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DH_anon_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_DH_anon_WITH_AES_256_CBC_SHA256,
                CipherSuite.TLS_DH_anon_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_NULL_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_RSA_WITH_NULL_SHA,
                CipherSuite.TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA,
                CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384,
                CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDH_ECDSA_WITH_NULL_SHA,
                CipherSuite.TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA,
                CipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384,
                CipherSuite.TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDH_RSA_WITH_NULL_SHA,
                CipherSuite.TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA,
                CipherSuite.TLS_ECDH_anon_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDH_anon_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDH_anon_WITH_NULL_SHA,
                CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV,
                CipherSuite.TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5,
                CipherSuite.TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA,
                CipherSuite.TLS_KRB5_WITH_3DES_EDE_CBC_MD5,
                CipherSuite.TLS_KRB5_WITH_3DES_EDE_CBC_SHA,
                CipherSuite.TLS_KRB5_WITH_DES_CBC_MD5,
                CipherSuite.TLS_KRB5_WITH_DES_CBC_SHA,
                CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256,
                CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_RSA_WITH_NULL_SHA256);
    }


    // Really old and weak Cipher Suites.
    // Will have to download them separately if the
    // server uses them. Not many servers do

//            CipherSuite.SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA,
//            CipherSuite.SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA,
//            CipherSuite.SSL_DHE_DSS_WITH_DES_CBC_SHA,
//            CipherSuite.SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA,
//            CipherSuite.SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA,
//            CipherSuite.SSL_DHE_RSA_WITH_DES_CBC_SHA,
//            CipherSuite.SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA,
//            CipherSuite.SSL_DH_anon_WITH_3DES_EDE_CBC_SHA,
//            CipherSuite.SSL_DH_anon_WITH_DES_CBC_SHA,
//            CipherSuite.SSL_RSA_EXPORT_WITH_DES40_CBC_SHA,
//            CipherSuite.SSL_RSA_WITH_3DES_EDE_CBC_SHA,
//            CipherSuite.SSL_RSA_WITH_DES_CBC_SHA,
//            CipherSuite.SSL_RSA_WITH_NULL_MD5,
//            CipherSuite.SSL_RSA_WITH_NULL_SHA,


    public List<CipherSuite> getCustomCipherSuites() {
        return customCipherSuites;
    }
}
