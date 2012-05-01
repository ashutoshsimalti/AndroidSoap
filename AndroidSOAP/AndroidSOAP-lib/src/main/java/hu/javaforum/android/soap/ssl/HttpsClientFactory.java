/**
 * CC-LGPL 2.1
 * http://creativecommons.org/licenses/LGPL/2.1/
 */
package hu.javaforum.android.soap.ssl;

import hu.javaforum.logger.PerfTracer;
import hu.javaforum.logger.Tracer;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

/**
 * The default HttpsClientFactory with custom keystore and truststore.
 *
 * @author Gábor Auth <gabor.auth@javaforum.hu>
 */
public final class HttpsClientFactory
{

  /**
   * The TRACER instance.
   */
  private static final Tracer TRACER = new Tracer(HttpsClientFactory.class.getSimpleName());
  /**
   * HTTP port number.
   */
  private static final int HTTP_PORT = 80;
  /**
   * HTTPS port number.
   */
  private static final int HTTPS_PORT = 443;
  /**
   * The static instance (singleton pattern).
   */
  public static final HttpsClientFactory INSTANCE = new HttpsClientFactory();

  /**
   * The constructor.
   */
  private HttpsClientFactory()
  {
    super();
  }

  /**
   * Creates a DefaultHttpClient instance.
   *
   * @param params The HttpParams
   * @return The DefaultHttpClient implementation
   */
  public static HttpClient createDefaultInstance(final HttpParams params)
  {
    PerfTracer tracer = new PerfTracer(TRACER);
    tracer.entry();

    try
    {
      return new DefaultHttpClient(params);
    } finally
    {
      tracer.exit();
    }
  }

  /**
   * Creates a DefaultHttpClient implementation with trusts certificates in
   * the trustStore.
   *
   * @param params The HttpParams
   * @param keyStore The keyStore
   * @param trustStore The trustStore
   * @return The DefaultHttpClient implementation
   * @throws KeyManagementException KeyManagementException
   * @throws KeyStoreException KeyStoreException
   * @throws NoSuchAlgorithmException NoSuchAlgorithmException
   * @throws UnrecoverableKeyException UnrecoverableKeyException
   */
  public static HttpClient createTrustStoreInstance(final HttpParams params,
          final KeyStore keyStore, final KeyStore trustStore)
          throws NoSuchAlgorithmException, KeyManagementException,
          KeyStoreException, UnrecoverableKeyException
  {
    PerfTracer tracer = new PerfTracer(TRACER);
    tracer.entry();

    try
    {
      return createClient(new SSLSocketFactory(keyStore, null, trustStore), params);
    } finally
    {
      tracer.exit();
    }
  }

  /**
   * Creates a DefaultHttpClient implementation with trusts all certificate.
   *
   * @param params The HttpParams
   * @return The DefaultHttpClient implementation
   * @throws KeyManagementException KeyManagementException
   * @throws KeyStoreException KeyStoreException
   * @throws NoSuchAlgorithmException NoSuchAlgorithmException
   * @throws UnrecoverableKeyException UnrecoverableKeyException
   */
  public static HttpClient createTrustAllInstance(final HttpParams params)
          throws NoSuchAlgorithmException, KeyManagementException,
          KeyStoreException, UnrecoverableKeyException
  {
    PerfTracer tracer = new PerfTracer(TRACER);
    tracer.entry();

    try
    {
      return createClient(new AllTrustSSLSocketFactory(KeyStore.getInstance(KeyStore.getDefaultType())), params);
    } finally
    {
      tracer.exit();
    }
  }

  /**
   * Create client with the specified SSLSocketFactory.
   *
   * @param sslSocketFactory The factory
   * @param params The HttpParams instanc
   * @return The DefaultHttpClient instance
   */
  private static HttpClient createClient(final SSLSocketFactory sslSocketFactory,
          final HttpParams params)
  {
    PerfTracer tracer = new PerfTracer(TRACER);
    tracer.entry();

    try
    {
      SchemeRegistry registry = new SchemeRegistry();
      registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), HTTP_PORT));
      registry.register(new Scheme("https", sslSocketFactory, HTTPS_PORT));
      return new DefaultHttpClient(new ThreadSafeClientConnManager(params, registry), params);
    } finally
    {
      tracer.exit();
    }
  }
}
