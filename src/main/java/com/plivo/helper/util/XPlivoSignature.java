package com.plivo.helper.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.plivo.helper.exception.PlivoException;

public class XPlivoSignature {

	private static String CHARSET = "UTF-8";

	public static Boolean verify(String uri, LinkedHashMap<String, String> parameters, String xsignature,
			String authToken) throws PlivoException {
		boolean isMatch = false;
		if (xsignature != null) {
			isMatch = xsignature.equals(calculate(uri, parameters, authToken));
		}
		return isMatch;
	}

	public static String calculate(String uri, LinkedHashMap<String, String> parameters, String authToken)
			throws PlivoException {
		String signature = null;
		Map<String, String> sortedParams = new TreeMap<String, String>(parameters);
		for (Entry<String, String> pair : sortedParams.entrySet()) {
			uri += pair.getKey() + pair.getValue();
		}

		try {
			byte[] keyBytes = authToken.getBytes();
			byte[] textBytes = uri.getBytes(CHARSET);
			Mac hmac = Mac.getInstance("HmacSHA1");
			SecretKeySpec macKey = new SecretKeySpec(keyBytes, "HmacSHA1");
			hmac.init(macKey);
			byte[] signBytes = hmac.doFinal(textBytes);
			signature = new String(Base64.encodeBase64(signBytes));

		} catch (Exception e) {
			PlivoException newException = new PlivoException(e.getLocalizedMessage());
			newException.setStackTrace(e.getStackTrace());
			throw newException;
		}

		return signature;
	}
	}
}
