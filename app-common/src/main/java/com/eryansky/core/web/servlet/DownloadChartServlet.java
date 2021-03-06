/**
 *  Copyright (c) 2012-2020 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.web.servlet;

import com.eryansky.common.web.utils.WebUtils;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * 用于下载highchart图表
 */
public class DownloadChartServlet extends HttpServlet {

	public DownloadChartServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");

		String type = request.getParameter("type");
		String svg = request.getParameter("svg");
		String filename = request.getParameter("filename");
		filename = filename == null ? "chart" : filename;

		if (null == type || null == svg) {// 说明浏览器支持大数据上传，所以这地方需要从大数据流里面取数据
			BufferedReader rader = request.getReader();
			String str;
			int i = 0;
			while ((str = rader.readLine()) != null) {
				i++;
				if (i == 4) {
					filename = str;
					continue;
				}
				if (i == 8) {
					type = str;
					continue;
				}
				if (i == 20) {
					svg = str;
					break;
				}
			}
		}

		ServletOutputStream out = response.getOutputStream();
		if (null != type && null != svg) {
			svg = svg.replaceAll(":rect", "rect");
			String ext = "";
			Transcoder t = null;
			if (type.equals("image/png")) {
				ext = "png";
				t = new PNGTranscoder();
			} else if (type.equals("image/jpeg")) {
				ext = "jpg";
				t = new JPEGTranscoder();
				t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, (float) 0.8);
			} else if (type.equals("application/pdf")) {
				ext = "pdf";
				t = (Transcoder) new PDFTranscoder();
			} else if (type.equals("image/svg+xml"))
				ext = "svg";
            WebUtils.setDownloadableHeader(request,response,filename + "." + ext);
			response.addHeader("Content-Type", type);
			if (null != t) {
				TranscoderInput input = new TranscoderInput(new StringReader(svg));
				TranscoderOutput output = new TranscoderOutput(out);
				try {
					t.transcode(input, output);
				} catch (TranscoderException e) {
					out.print("Problem transcoding stream. See the web logs for more details.");
					e.printStackTrace();
				}
			} else if (ext.equals("svg")) {
				OutputStreamWriter writer= null;
				try {
					writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
					writer.append(svg);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if(null != writer){
						writer.close();
					}
				}
			} else
				out.print("Invalid type: " + type);
		} else {
			response.addHeader("Content-Type", "text/html");
			out.println("Usage:\n\tParameter [svg]: The DOM Element to be converted." + "\n\tParameter [type]: The destination MIME type for the elment to be transcoded.");
		}
		out.flush();
		out.close();
	}

}
