package com.twincoders.twinpush.sdk.communications.security;

import java.math.BigInteger;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.twincoders.twinpush.sdk.logging.Strings;

public final class TwinPushTrustManager implements X509TrustManager {
	
	String publicKey = null;
	Map<String, String> issuerChecks = null;
	Map<String, String> subjectChecks = null;
	
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (chain == null) {
			throw new IllegalArgumentException("checkServerTrusted: X509Certificate array is null");
		}

		if (!(chain.length > 0)) {
			throw new IllegalArgumentException("checkServerTrusted: X509Certificate is empty");
		}

		if (!(null != authType && authType.equalsIgnoreCase("RSA"))) {
			throw new CertificateException("checkServerTrusted: AuthType is not RSA");
		}

		// Perform customary SSL/TLS checks
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
			tmf.init((KeyStore) null);

			for (TrustManager trustManager : tmf.getTrustManagers()) {
				((X509TrustManager) trustManager).checkServerTrusted(chain,
						authType);
			}
		} catch (Exception e) {
			throw new CertificateException(e);
		}

		CertificateInfo certInfo = new CertificateInfo(chain[0]);
		if (publicKey != null) certInfo.checkPublicKey(publicKey);
		if (issuerChecks != null) certInfo.checkIssuerInfo(issuerChecks);
		if (subjectChecks != null) certInfo.checkSubjectInfo(subjectChecks);
	}
	
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		throw new CertificateException("Client certificates not supported!");
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
	
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public void setIssuerChecks(Map<String, String> issuerChecks) {
		this.issuerChecks = issuerChecks;
	}

	public void setSubjectChecks(Map<String, String> subjectChecks) {
		this.subjectChecks = subjectChecks;
	}

	class CertificateInfo {
		
		RSAPublicKey publicKey;
		String encodedPublicKey;
		Map<String, String> issuerInfo;
		Map<String, String> subjectInfo;
		
		public CertificateInfo(X509Certificate cert) {
			// Hack ahead: BigInteger and toString(). We know a DER encoded Public
			// Key begins
			// with 0x30 (ASN.1 SEQUENCE and CONSTRUCTED), so there is no leading
			// 0x00 to drop.
			publicKey = (RSAPublicKey) cert.getPublicKey();
			encodedPublicKey = new BigInteger(1 /* positive */, publicKey.getEncoded()).toString(16);
			issuerInfo = getInfoHash(cert.getIssuerDN().getName());
			subjectInfo = getInfoHash(cert.getSubjectDN().getName());
		}
		
		private Map<String, String> getInfoHash(String name) {
			Map<String, String> info = new HashMap<String, String>();
			for (String field: name.split(",")) {
				String[] values = field.split("=");
				if (values.length == 2) {
					info.put(values[0], values[1]);
				}
			}
			return info;
		}
		
		public void checkPublicKey(String encodedKey) throws CertificateException {
			final boolean expected = Strings.equalsIgnoreCase(encodedKey, encodedPublicKey);
			if (!expected) {
				throw new CertificateException(String.format("checkServerTrusted: Expected public key: %s, got public key: %s", encodedPublicKey, encodedKey));
			}
		}
		
		public void checkIssuerInfo(Map<String, String> fields) throws CertificateException {
			checkInfo(fields, issuerInfo, "issuer");
		}
		
		public void checkSubjectInfo(Map<String, String> fields) throws CertificateException {
			checkInfo(fields, subjectInfo, "subject");
		}
		
		private void checkInfo(Map<String, String> fields, Map<String, String> info, String checkName) throws CertificateException {
			for (String key : fields.keySet()) {
				String expectedValue = fields.get(key);
				String certValue = info.get(key);
				final boolean expected = Strings.equalsIgnoreCase(expectedValue, certValue);
				if (!expected) {
					throw new CertificateException(String.format("checkServerTrusted: Expected %s %s: %s, got: %s", checkName, key, expectedValue, certValue));
				}
			}
		}
	}
}
