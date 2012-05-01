/**
 * CC-LGPL 2.1
 * http://creativecommons.org/licenses/LGPL/2.1/
 */
package hu.javaforum.android.soap.ssl;

import hu.javaforum.logger.Logger;
import hu.javaforum.logger.PerfLogger;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 * Manager of the trust all SSL mechanism.
 *
 * Changelog:
 * ANDROIDSOAP-7
 *
 * @author Gábor Auth <gabor.auth@javaforum.hu>
 */
public class AllTrustManager implements X509TrustManager
{

  /**
   * The TRACER instance.
   */
  private static final Logger LOGGER = new Logger(AllTrustManager.class.getSimpleName());

  /**
   * Check client is trusted.
   *
   * @param certificates The certificate chain
   * @param authType The type of auth
   * @throws CertificateException On wrong certificate
   */
  @Override
  public final void checkClientTrusted(final X509Certificate[] certificates, final String authType)
          throws CertificateException
  {
    PerfLogger logger = new PerfLogger(LOGGER);
    logger.info("Certificates: %1$s, authType: %2$s", certificates, authType);
  }

  /**
   * Check server is trusted.
   *
   * @param certificates The certificate chain
   * @param authType The type of auth
   * @throws CertificateException On wrong certificate
   */
  @Override
  public final void checkServerTrusted(final X509Certificate[] certificates, final String authType)
          throws CertificateException
  {
    PerfLogger logger = new PerfLogger(LOGGER);
    logger.info("Certificates: %1$s, authType: %2$s", certificates, authType);
  }

  /**
   * Returns with the list of accepted issuers.
   *
   * @return The list
   */
  @Override
  public final X509Certificate[] getAcceptedIssuers()
  {
    return new X509Certificate[0];
  }
}
