package tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.ws.spi.http.HttpExchange;

public class FormHandler {
//	long logCnt;
//
//	public FormHandler(long logCnt) {
//		this.logCnt = logCnt;
//	}
//
//	public Map<String, String> getFormData(HttpExchange xchg, String boundary, long fileSize) {
//
//		Map<String, String> params = parse(xchg);
//		return params;
//	}
//
////	public void responseForm(HttpExchange xchg, Coconut responseCoconut, String boundary) throws IOException {
////		JSONObject responseJSON = null;
////
////		responseJSON = responseCoconut.responseData();
////
////		Headers header = xchg.getResponseHeaders();
////		header.add("Content-Type", "application/json");
////		header.set("Access-Control-Allow-Origin", "*");
////		header.set("Access-Control-Allow-Headers", "X-Requested-With");//
////		header.set("Access-Control-Allow-Methods", "POST");
////
////		xchg.sendResponseHeaders(200, responseJSON.toString().length());
////
////		OutputStream os = xchg.getResponseBody();
////		os.write(responseJSON.toString().getBytes("UTF-8"));
////		os.flush();
////		os.close();
////		xchg.close();
////
////		System.out.println("\n-----------------------------------------------------------------------\n");
////		CoconutzLog.clear(logCnt);
////	}
//
//	private void readLine(InputStream is, ByteArrayOutputStream baos) throws IOException {
//		baos.reset();
//		boolean preisr = false;
//		int d;
//		while ((d = is.read()) != -1) {
//			if (d == '\n' && preisr) {
//				return;
//			}
//			if (preisr) {
//				baos.write('\r');
//			}
//			if (!(preisr = d == '\r')) {
//				baos.write(d);
//			}
//		}
//		if (preisr) {
//			baos.write('\r');
//		}
//	}
//
//	private int boundaryEqual(String boundary, ByteArrayOutputStream baos) throws IOException {
//		if (boundary.length() + 2 == baos.size()) {
//			if (("--" + boundary).equals(new String(baos.toByteArray(), "UTF-8"))) {
//				return 1;
//			}
//		} else if (boundary.length() + 4 == baos.size()) {
//			if (("--" + boundary + "--").equals(new String(baos.toByteArray(), "UTF-8"))) {
//				return 2;
//			}
//		}
//		return 0;
//	}
//
//	private Map<String, String> parse(HttpExchange httpExchange) {
//		try {
//			String contentType = httpExchange.getRequestHeaders().getFirst("Content-Type");
//			HashMap<String, String> map = new HashMap<String, String>();
//			if (httpExchange.getRequestMethod().equalsIgnoreCase("post") && contentType != null) {
//				String boundary = contentType.substring("multipart/form-data; boundary=".length());
//				BufferedInputStream is = new BufferedInputStream(httpExchange.getRequestBody());
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//				readLine(is, baos);
//				int r = boundaryEqual(boundary, baos);
//				if (r != 1)
//					return map;
//				loop: while (true) {
//					String name = null;
//					String filename = null;
//					while (true) {
//						readLine(is, baos);
//						if (baos.size() == 0)
//							break;
//						String s = new String(baos.toByteArray(), "UTF-8");
//						if (s.startsWith("Content-Disposition:")) {
//							for (String ss : s.split(";")) {
//								ss = ss.trim();
//								if (ss.startsWith("name=")) {
//									name = ss.substring("name=".length() + 1, ss.length() - 1);
//								} else if (ss.startsWith("filename=")) {
//									filename = ss.substring("filename=".length() + 1, ss.length() - 1);
//								}
//							}
//						}
//					}
//					ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
//					while (true) {
//						readLine(is, baos);
//						r = boundaryEqual(boundary, baos);
//						if (r == 0) {
//							baos.write(13);
//							baos.write(10);
//							baos2.write(baos.toByteArray());
//							continue;
//						}
//						if (name != null) {
//							if (filename != null) {
//								Object files = map.get(name);
//								if (files == null) {
//									String time = "" + System.currentTimeMillis();
//									filename = time + "/" + filename;
//									System.out.println(filename);
//									File file = new File(time);
//									file.mkdirs();
//									FileOutputStream fos = new FileOutputStream(filename);
//									fos.write(baos2.toByteArray(), 0, baos2.toByteArray().length - 2);
//									fos.close();
//									map.put(name, filename);
//								}
//							} else {
//								Object vals = map.get(name);
//								if (vals == null) {
//									byte[] b = baos2.toByteArray();
//									byte[] b2 = new byte[baos2.size() - 2];
//									for (int i = 0; i < baos2.size() - 2; i++)
//										b2[i] = b[i];
//									map.put(name, new String(b2));
//								}
//							}
//						}
//						if (r == 1) {
//							continue loop;
//						} else {
//							break loop;
//						}
//					}
//				}
//			}
//			return map;
//		} catch (IOException e) {
////			CoconutzLog.put(logCnt, e.getMessage());
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public JSONObject toJson(Map<String, String> inputForm) {
//
//		JSONObject requestJSON = new JSONObject();
//		JSONObject parameterJSON = new JSONObject();
//
//		Set<Entry<String, String>> set = inputForm.entrySet();
//		Iterator<Entry<String, String>> it = set.iterator();
//		try {
//			while (it.hasNext()) {
//				Entry<String, String> entry = it.next();
//
//				String key = entry.getKey();
//				String value = entry.getValue();
//				// try {
//				// byte[] bb = entry.getValue().getBytes("8859_1");
//				// String temp = new String(bb);
//				// String Result = new String( bb, "euc-kr" );
//				// byte[] bb1 = entry.getValue().getBytes("EUC-KR");
//				// String temp1 = new String(bb);
//				// String Result1 = new String( bb, "UTF-8" );
//				//
//				// System.out.println(temp);
//				// System.out.println(temp1);
//				// System.out.println(Result);
//				// System.out.println(Result1);
//				//
//				// System.out.println(new String
//				// (entry.getValue().getBytes("UTF-8"), "KSC5601") );
//				// System.out.println(new
//				// String(entry.getValue().getBytes("utf-8"), "euc-kr"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("utf-8"), "ksc5601"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("utf-8"), "x-windows-949"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("utf-8"), "iso-8859-1"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("iso-8859-1"), "euc-kr"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("iso-8859-1"), "ksc5601"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("iso-8859-1"),
//				// "x-windows-949"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("iso-8859-1"), "utf-8"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("euc-kr"), "ksc5601"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("euc-kr"), "utf-8"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("euc-kr"),
//				// "x-windows-949"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("euc-kr"), "iso-8859-1"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("ksc5601"), "euc-kr"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("ksc5601"), "utf-8"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("ksc5601"),
//				// "x-windows-949"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("ksc5601"), "iso-8859-1"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("x-windows-949"),
//				// "euc-kr"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("x-windows-949"), "utf-8"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("x-windows-949"),
//				// "ksc5601"));
//				// System.out.println(new
//				// String(entry.getValue().getBytes("x-windows-949"),
//				// "iso-8859-1"));
//				// } catch (UnsupportedEncodingException e) {
//				// // TODO Auto-generated catch block
//				// e.printStackTrace();
//				// }
//
//				if (key.equals("user") || key.equals("class") || key.equals("function") || key.equals("returntype")) {
//
//					requestJSON.put(key, value);
//				} else {
//					parameterJSON.put(key, value);
//				}
//
//				System.out.println("Key:" + entry.getKey() + "\tValue:" + value);
//			}
//			// System.out.println(requestJSON.toString());
//			// System.out.println(parameterJSON.toString());
//
//			if (parameterJSON.length() != 0) {
//				requestJSON.put("parameter", parameterJSON);
//			}
//		} catch (JSONException e) {
//			CoconutzLog.put(logCnt, e.getMessage());
//		}
//		return requestJSON;
//	}
}
